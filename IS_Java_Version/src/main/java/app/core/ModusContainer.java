package app.core;

import app.model.Card;
import app.model.Metadata;
import app.model.ModusBuffer;
import app.modus.Modus;
import app.modus.ModusMetatagRunStatus;
import app.util.*;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Pane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * The ModusContainer class provides support to the sylladex in managing the modi in the modus package. This enables users to take a valid
 * and {@link app.modus.Modus Modus} conforming java class and "plug" it into the sylladex for use.
 * <br><br> This class also tracks what modus is currently in use, and what modi are currently available, as well as
 * metadata about the particular modus, to help let the sylladex "know" how to handle certain events and actions.
 * <p> Author's note: originally the direction of the Modus Manager was to allow users to add
 * a java file that complied with Modus interface to a file directory and then the sylladex could be refreshed and load the code given, but
 * this form of plug-and-play of adding "valid" modus java files to to a given directory for the manager to pick up for the sylladex to use
 * is a bit too insecure for my preference. it would be better to simply allow for a person to mod the open-source version of this by adding
 * those java files mentioned above to the modus package and then compile it into a jar. Later, once the modusValidation function can be
 * more robust and check for malicious behavior, then this direction can be resumed and utilize the files through a URL filestream from
 * getResourcesAsStream to temp files for the jar to use until the sylladex is closed, or use file chooser which would allow the jar to keep
 * track of the various modus files wherever they happen to reside. would require re- enabling the tracking of File paths through Metadata
 * class.
 *
 * @author Triston Scallan
 * @see app.modus.Modus
 */
class ModusContainer {
    static final         String                          MODUS_PREFIX = "modus.";
    private static final Logger                          LOGGER       = LogManager.getLogger(ModusContainer.class);
    /** Tracks the current active Modus as an index of {@link #modusClassList}. -1 means no active Modus. */
    private final        ReadOnlyObjectWrapper<Metadata> currentModusMetadata;
    private final        StringProperty                  modusInput;
    private              ModusBuffer                     modusBuffer;
    /** Tracks all available Fetch Modi for the Sylladex */
    private              List<Class<? extends Modus>>    modusClassList;

    /**
     * Constructor
     *
     * @param submittedInputSubscriberProperty
     *         the property for adding listeners to submitted input
     * @param displayProperty
     *         the property for the current display
     * @param outputProperty
     *         the property for the current text output
     * @param deckProperty
     *         the property for the master deck
     * @param openHandProperty
     *         the property for the open hand
     */
    ModusContainer(ReadOnlyObjectProperty<Consumer<ChangeListener<String>>> submittedInputSubscriberProperty,
                   ReadOnlyObjectProperty<? extends Pane> displayProperty,
                   ReadOnlyObjectProperty<? extends TextInputControl> outputProperty,
                   ListProperty<Card> deckProperty,
                   ListProperty<String> openHandProperty) {
        this.modusInput = new SimpleStringProperty(this, "modus_input", "");
        this.modusBuffer = new ModusBuffer(modusInput, displayProperty, outputProperty, deckProperty, openHandProperty);
        // todo: create a listener for current modus metadata - if value is set to null, handle null-pointer prevention
        // todo: create a listener for display property. if display's height or width changes, request a re-draw.
        this.currentModusMetadata = new ReadOnlyObjectWrapper<>(this, "current_modus_metadata", null);

        submittedInputSubscriberProperty.getValue().accept(this::handleModusInput);

        //populate a list with the class names in the modus package
        List<String> modusNameList = createClassNameList();

        //convert class names to class objects, filter out invalid modus classes, and collect
        modusClassList = modusNameList.stream()
                                      //convert the names into class objects
                                      .map(className -> {
                                          Class<? extends Modus> modusClass = null;
                                          try {
                                              modusClass = Class.forName(className, false, Modus.class.getClassLoader())
                                                                .asSubclass(Modus.class);
                                          } catch (ClassNotFoundException e) {
                                              System.err.print("ClassListing: " +
                                                               className +
                                                               " was listed as a class but no definition was found.\n");
                                          } catch (ClassCastException e) {
                                              System.err.print("ClassListing: ignoring found non-Modus derived class '" +
                                                               className +
                                                               "'.\n");
                                          }
                                          return modusClass;
                                      }).filter(this::validateModusFile).collect(Collectors.toList());
        System.out.println("ClassListing: complete.");

        //if modusClassList is empty, warn the user
        if (modusClassList.isEmpty()) {
            throw new RuntimeException("Final result of modusClassList in ModusContainer constructor is empty.");
        }
    }

    //************** GETTERS/SETTERS *******************/

    /**
     * @return the currentModusMetadata
     */
    @Nullable
    Metadata getCurrentModusMetadata() {
        return currentModusMetadata.getValue();
    }

    public ReadOnlyObjectProperty<Metadata> currentModusMetadataProperty() {
        return currentModusMetadata.getReadOnlyProperty();
    }

    /**
     * Instantiates the given <code>Modus</code> class and checks for instance validation. Will clear the
     * <code>modusBuffer</code> input and redirector fields, even if an exception is thrown.
     *
     * @param modusClass
     *         the modus to update the current selection for interfacing with
     * @throws IllegalAccessException
     *         if the given class is unable to be accessed from this scope
     * @throws InstantiationException
     *         if the instantiation process fails
     * @throws IllegalArgumentException
     *         is the class fails Metadata validation
     * @see Metadata#isValid(Metadata)
     */
    <T extends Modus> void updateCurrentModus(Class<T> modusClass) throws
                                                                   IllegalAccessException,
                                                                   InstantiationException,
                                                                   IllegalArgumentException {
        //instantiate the desired class and set it to this#currentModusMetadata
        try {
            Modus clazzInstance = modusClass.newInstance();
            if (!Metadata.isValid(clazzInstance.getMETADATA()))
                throw new IllegalArgumentException("Class '" + modusClass.getSimpleName() + "' failed validation as a Modus");
            System.out.println("ClassLoading: classSuccess = " + modusClass.getSimpleName());
            //replace old instance so it may be GC'd
            currentModusMetadata.setValue(clazzInstance.getMETADATA());
        } finally {
            //reset previous modus specific data in modusBuffer
            modusBuffer.clearModusInputRedirector();
            modusInput.setValue("");
        }
    }


    /**
     * @return the modusClassList
     */
    List<Class<? extends Modus>> getModusClassList() {
        return modusClassList;
    }

    //*************** UTILITY **************************/

    private void handleModusInput(ObservableValue<? extends String> bean, String oldInput, @Nullable String newInput) {
        LOGGER.traceEntry("handleSyllInput(bean={}, oldInput={}, newInput={}", bean, oldInput, newInput);
        if (newInput == null) {
            LOGGER.warn("New input for submitted input handler was null. " +
                        "Input should not be set to null or changed outside of the input component controller. " +
                        "Please review and revise code. ");
            return;
        } else if (getCurrentModusMetadata() == null) {
            LOGGER.warn("New input while modus is not selected. Please review and revise code to prevent this warning.");
            return;
        }

        Optional<Consumer<ModusBuffer>> redirector = modusBuffer.getAndResetModusInputRedirector();
        //if a redirection is present then run it's method, otherwise parse input and execute command.
        if (redirector.isPresent()) {
            try {
                // run the modus lambda that is pointed to by the redirection
                modusInput.setValue(newInput);
                redirector.get().accept(modusBuffer);
            } catch (FatalModusException e) {
                e.printStackTraceLess(4);
                System.err.print("restarting modus.");
                modusBuffer.getTextOutput().appendText("Resetting modus to recover from a fatal error. Please reload deck.\n");
                recoverFromModusFailure();
            } catch (ModusRuntimeException e) {
                e.printStackTraceLess(4);
                modusBuffer.getTextOutput().appendText("ERROR - " + e + ".\n");
                modusBuffer.clearModusInputRedirector();
            }
        } else {
            // if input does not have modus prefix, input is not for this handler
            if (!newInput.trim().toUpperCase().startsWith(MODUS_PREFIX.toUpperCase())) return;

            //split the input into ["command", "arg1 arg2 arg3..."]
            String[] splitInput = newInput.trim().substring(MODUS_PREFIX.length()).split(" ", 2);
            String   command    = splitInput[0];

            //split the args string into a list, if any, then run the commandSwitch
            if (splitInput.length > 1) {
                String[] inputArgs = splitInput[1].split(",");
                for (int i = 0; i < inputArgs.length; i++) {
                    inputArgs[i] = inputArgs[i].trim();
                }
                execModusCmd(command, inputArgs);
            } else {
                execModusCmd(command);
            }
        }
        // reset modus input to a 'no input' state.
        modusInput.setValue("");
        LOGGER.traceExit();
    }

    /**
     * Messages a modus command statement to the currently selected modus for execution
     *
     * @param command
     *         a modus command associated with a ModusCommandMap entry
     * @param args
     *         the arguments to supply the command with
     */
    //TODO: add logging for thrown exceptions instead of printing to err or out
    private void execModusCmd(String command, String... args) {
        System.out.println("invoking `" + command + "` with args = " + Arrays.toString(args));
        try {
            currentModusMetadata.get().COMMAND_MAP.command(command, args, modusBuffer);
        } catch (NoSuchCommandException e) {
            System.err.print("ModusContainer#execModusCmd: " + e + ".\n");
            modusBuffer.getTextOutput().appendText("ERROR - " + e + ".\n");
        } catch (IllegalSyntaxException e) {
            System.err.print("ModusContainer#execModusCmd: " + e + ".\n");
            String errMsg = "ERROR - " + e + ".\n" + currentModusMetadata.get().COMMAND_MAP.desc(command) + "\n";
            modusBuffer.getTextOutput().appendText(errMsg);
        } catch (CommandRuntimeException e) {
            e.printStackTraceLess(4);
            modusBuffer.getTextOutput().appendText("ERROR - " + e + ".\n");
            modusBuffer.clearModusInputRedirector();
        } catch (FatalModusException e) {
            e.printStackTraceLess(4);
            System.err.print("restarting modus.");
            modusBuffer.getTextOutput().appendText("Resetting modus to recover from a fatal error. Please reload deck.\n");
            recoverFromModusFailure();
        }
    }

    void requestSave() throws RequestException {
        if (getCurrentModusMetadata() == null) throw new RequestException("No modus selected");
        getCurrentModusMetadata().REFERENCE.save(modusBuffer);
    }

    void requestLoad() throws RequestException {
        if (getCurrentModusMetadata() == null) throw new RequestException("No modus selected");
        getCurrentModusMetadata().REFERENCE.load(modusBuffer);
    }

    void requestDrawToDisplay() throws RequestException {
        if (getCurrentModusMetadata() == null) throw new RequestException("No modus selected");
        getCurrentModusMetadata().REFERENCE.drawToDisplay(modusBuffer);
    }

    @Nonnull
    String requestDescription() throws RequestException {
        if (getCurrentModusMetadata() == null) throw new RequestException("No modus selected");
        return Optional.ofNullable(getCurrentModusMetadata().REFERENCE.description()).orElse("");
    }

    /**
     * Initializes a new object of the current modus class, then effectively replaces the old object's reference. Includes (transitively) a
     * side effect of resetting the modusBuffer's input and redirector fields.
     *
     * @throws RuntimeException
     *         if the modus' constructor cannot be accessed or fails
     */
    void resetModus() throws RuntimeException {
        Modus modus = Objects.requireNonNull(getCurrentModusMetadata()).REFERENCE;
        try {
            updateCurrentModus(modus.getClass());
        } catch (SecurityException | IllegalAccessException e) {
            throw new RuntimeException("Access to " + modus.getClass().getSimpleName() + " constructor was prevented.", e);
        } catch (IllegalArgumentException | InstantiationException e) {
            throw new RuntimeException(modus.getClass().getSimpleName() + " modus constructor failed.", e);
        }
    }

    /**
     * Attempts to recover the current <code>Modus</code> instance back into a working state.
     *
     * @implNote resets the modus and on failure of that sets the <code>currentModusMetadata</code> to null, clears display.
     *         Transitively, this will also reset any redirection in the modus buffer, as the call to updateCurrentModus will be called
     *         eventually.
     * @see #resetModus()
     * @see #updateCurrentModus(Class)
     * @see ModusBuffer#clearModusInputRedirector()
     */
    //TODO: needs more sophisticated work to properly fix without just resetting, but this should do for now
    private void recoverFromModusFailure() {
        try {
            resetModus();
        } catch (RuntimeException e) {
            e.printStackTrace();
            //catastrophic failure. unload modus and reset the view.
            currentModusMetadata.setValue(null);
            modusBuffer.getDisplay().getChildren().clear();
            modusBuffer.getTextOutput().appendText("!!!catastrophic modus error!!!\nPlease select a different modus.\n");
        }
    }

    /**
     * gets a list of all the modus class names. This is done in a modular and generalized way utilizing reflection. First it will attempt
     * to get the name of the modus package. after acquiring the name it will then convert it into a resource to find the URL and URI path
     * of the modus package so that it can convert it to a directory. it will then iterate through the directory and create a list of the
     * class files contained. this will first be performed for a file system and then for a jarfile system.
     */
    private List<String> createClassNameList() {
        String       pkgname       = Modus.class.getPackage().getName();
        List<String> classNameList = new ArrayList<>();

        // Get a File object for the package
        File   directory = null;
        String fullPath;
        System.out.println("ClassDiscovery: Package: " + pkgname);
        URL resource = ClassLoader.getSystemClassLoader().getResource(pkgname.replace('.', File.separatorChar));
        System.out.println("ClassDiscovery: Resource = " + resource);
        if (resource == null) {
            throw new RuntimeException("No resource for " + pkgname);
        }
        fullPath = resource.getFile();
        System.out.println("ClassDiscovery: FullPath = " + resource);

        try {
            directory = new File(resource.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(pkgname +
                                       " (" +
                                       resource +
                                       ") does not appear to be a valid URL / URI.  Strange, since we got it from the system...", e);
        } catch (IllegalArgumentException ignored) {
        }
        System.out.println("ClassDiscovery: Directory = " + directory);

        if (directory != null && directory.exists()) {
            // Get the list of the files contained in the package
            List<String> files = Arrays.asList(Objects.requireNonNull(directory.list()));
            files.sort(String::compareTo);
            for (String file : files) {
                // we are only interested in .class files
                if (file.endsWith(".class")) {
                    // removes the .class extension
                    String className = pkgname + '.' + file.substring(0, file.length() - 6);
                    System.out.println("ClassDiscovery: className = " + className);
                    classNameList.add(className);
                }
            }
        }
        //attempt to try it as a jarfile path instead
        else {
            String jarPath = fullPath.replaceFirst("[.]jar[!].*", ".jar").replaceFirst("file:", "");
            try (JarFile jarFile = new JarFile(jarPath)) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry     = entries.nextElement();
                    String   entryName = entry.getName();
                    if (entryName.startsWith(pkgname) && entryName.length() > (pkgname.length() + "/".length())) {
                        System.out.println("ClassDiscovery: JarEntry: " + entryName);
                        String className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "");
                        System.out.println("ClassDiscovery: className = " + className);
                        classNameList.add(className);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(pkgname + " (" + directory + ") does not appear to be a valid package", e);
            }
        }
        System.out.println("ClassDiscovery: complete.");
        return classNameList;
    }

    /**
     * Validates a class as an instantiable subclass of Modus
     *
     * @param modusClass
     *         the modus class literal to be drawn from
     * @param <M>
     *         a subclass of Modus
     * @return True if modus can have a running instance created via reflection.
     */
    private <M extends Modus> boolean validateModusFile(Class<M> modusClass) {
        if (modusClass == null) return false;
        ModusMetatagRunStatus runStatusAnnot = modusClass.getAnnotation(ModusMetatagRunStatus.class);
        //currently, checking the run status tag is sufficient for validation. add more as needed/thought of.
        return (runStatusAnnot != null) && runStatusAnnot.value();
    }


}
