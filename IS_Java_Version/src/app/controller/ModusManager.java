package app.controller;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;


import app.model.Card;
import app.model.Metadata;
import app.model.ModusBuffer;
import app.util.*;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import app.modus.*;

/**
 * The ModusManager class provides support to the sylladex in managing the modi in the modus package. This enables users
 * to take a valid and {@link app.modus.Modus Modus} conforming java class and "plug" it into the sylladex for use.
 * <br><br> This class also tracks what modus is currently in use, and what modi are currently available, as well as
 * metadata about the particular modus, to help let the sylladex "know" how to handle certain events and actions.
 * <p> Author's note: originally the direction of the Modus Manager was to allow users to add
 * a java file that complied with Modus interface to a file directory and then the sylladex could be refreshed and load
 * the code given, but this form of plug-and-play of adding "valid" modus java files to to a given directory for the
 * manager to pick up for the sylladex to use is a bit too insecure for my preference. it would be better to simply
 * allow for a person to mod the open-source version of this by adding those java files mentioned above to the modus
 * package and then compile it into a jar. Later, once the modusValidation function can be more robust and check for
 * malicious behavior, then this direction can be resumed and utilize the files through a URL filestream from
 * getResourcesAsStream to temp files for the jar to use until the sylladex is closed, or use file chooser which would
 * allow the jar to keep track of the various modus files wherever they happen to reside. would require re- enabling the
 * tracking of File paths through Metadata class.
 *
 * @author Triston Scallan
 * @see app.modus.Modus
 */
class ModusManager {
    private ModusBuffer                  modusBuffer;
    /** Tracks the current active Modus as an index of {@link #modusClassList}. -1 means no active Modus. */
    private Metadata                     currentModusMetadata;
    /** Tracks all available Fetch Modi for the Sylladex */
    private List<Class<? extends Modus>> modusClassList;

    /**
     * Constructor
     */
    ModusManager(AtomicReference<String> wrappedModusInput,
                 AtomicReference<StackPane> wrappedDisplay,
                 AtomicReference<TextArea> wrappedTextOutput,
                 AtomicReference<List<Card>> wrappedDeck,
                 AtomicReference<List<String>> wrappedOpenHand) {
        this.modusBuffer = new ModusBuffer(wrappedModusInput,
                                           wrappedDisplay,
                                           wrappedTextOutput,
                                           wrappedDeck,
                                           wrappedOpenHand);

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
            throw new RuntimeException("Final result of modusClassList in ModusManager constructor is empty.");
        }
    }

    //************** GETTERS/SETTERS *******************/

    /**
     * @return the currentModusMetadata
     */
    Metadata getCurrentModus() {
        return currentModusMetadata;
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
    <T extends Modus> void updateCurrentModus(Class<T> modusClass) throws IllegalAccessException, InstantiationException, IllegalArgumentException {
        //instantiate the desired class and set it to this#currentModusMetadata
        try {
            Modus clazzInstance = modusClass.newInstance();
            if (!Metadata.isValid(clazzInstance.getMETADATA())) throw new IllegalArgumentException("Class '" +
                                                                                                   modusClass.getSimpleName() +
                                                                                                   "' failed validation as a Modus");
            System.out.println("ClassLoading: classSuccess = " + modusClass.getSimpleName());
            //replace old instance so it may be GC'd
            currentModusMetadata = clazzInstance.getMETADATA();
        } finally {
            //reset previous modus specific data in modusBuffer
            modusBuffer.clearModusInput();
            modusBuffer.clearModusInputRedirector();
        }
    }


    /**
     * @return the modusClassList
     */
    List<Class<? extends Modus>> getModusClassList() {
        return modusClassList;
    }

    //*************** UTILITY **************************/

    void handleModusInput() {
        //get inputModusRedirector from modusBuffer.
        Optional<Consumer<ModusBuffer>> redirector = Optional.ofNullable(modusBuffer.getAndResetModusInputRedirector());

        //if redirector is present then run it's method, otherwise parse input and execute command.
        if (redirector.isPresent()) {
            try {
                redirector.get().accept(modusBuffer);
            } catch (FatalModusException e) {
                e.printStackTraceLess(4);
                System.err.print("restarting modus.");
                modusBuffer.getTextOutput()
                           .appendText("Resetting modus to recover from a fatal error. Please reload deck.\n");
                recoverFromModusFailure();
            } catch (ModusRuntimeException e) {
                e.printStackTraceLess(4);
                modusBuffer.getTextOutput().appendText("ERROR - " + e + ".\n");
                modusBuffer.clearModusInput();
                modusBuffer.clearModusInputRedirector();
            }

        } else {
            String input = modusBuffer.getAndResetModusInput();
            //split the input into ["command", "arg1 arg2 arg3..."]
            String[] splitInput = input.split(" ", 2);
            String command = splitInput[0].trim();

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
            currentModusMetadata.COMMAND_MAP.command(command, args, modusBuffer);
        } catch (NoSuchCommandException e) {
            System.err.print("ModusManager#execModusCmd: " + e + ".\n");
            modusBuffer.getTextOutput().appendText("ERROR - " + e + ".\n");
        } catch (IllegalSyntaxException e) {
            System.err.print("ModusManager#execModusCmd: " + e + ".\n");
            String errMsg = "ERROR - " + e + ".\n" + currentModusMetadata.COMMAND_MAP.desc(command) + "\n";
            modusBuffer.getTextOutput().appendText(errMsg);
        } catch (CommandRuntimeException e) {
            e.printStackTraceLess(4);
            modusBuffer.getTextOutput().appendText("ERROR - " + e + ".\n");
            modusBuffer.clearModusInput();
            modusBuffer.clearModusInputRedirector();
        } catch (FatalModusException e) {
            e.printStackTraceLess(4);
            System.err.print("restarting modus.");
            modusBuffer.getTextOutput()
                       .appendText("Resetting modus to recover from a fatal error. Please reload deck.\n");
            recoverFromModusFailure();
        }
    }

    void requestSave() {
        currentModusMetadata.REFERENCE.save(modusBuffer);
    }

    void requestLoad() {
        currentModusMetadata.REFERENCE.load(modusBuffer);
    }

    void requestDrawToDisplay() {
        currentModusMetadata.REFERENCE.drawToDisplay(modusBuffer);
    }

    String requestDescription() {
        return Optional.of(currentModusMetadata.REFERENCE.description()).orElse("");
    }

    /**
     * Initializes a new object of the current modus class, then effectively replaces the old object's reference.
     * Includes (transitively) a side effect of resetting the modusBuffer's input and redirector fields.
     *
     * @throws RuntimeException
     *         if the modus' constructor cannot be accessed or fails
     */
    void resetModus() throws RuntimeException {
        try {
            updateCurrentModus(getCurrentModus().REFERENCE.getClass());
        } catch (SecurityException | IllegalAccessException e) {
            throw new RuntimeException("Access to " +
                                       getCurrentModus().REFERENCE.getClass().getSimpleName() +
                                       " constructor was prevented.", e);
        } catch (IllegalArgumentException | InstantiationException e) {
            throw new RuntimeException(getCurrentModus().REFERENCE.getClass().getSimpleName() +
                                       " modus constructor failed.", e);
        }
    }

    /**
     * Attempts to recover the current <code>Modus</code> instance back into a working state.
     *
     * @implNote resets the modus and on failure of that sets the <code>currentModusMetadata</code> to null and
     *         clears display.
     * @see #resetModus()
     */
    //TODO: needs more sophisticated work to properly fix without just resetting, but this should do for now
    private void recoverFromModusFailure() {
        try {
            resetModus();
        } catch (RuntimeException e) {
            e.printStackTrace();
            //catastrophic failure. unload modus and reset the view.
            currentModusMetadata = null;        //setting to null will utilize the sylladex's submit prevention
            modusBuffer.getDisplay().getChildren().clear();
            modusBuffer.getTextOutput()
                       .appendText("!!!catastrophic modus error!!!\nPlease select a different modus.\n");
        }
    }

    /**
     * gets a list of all the modus class names. This is done in a modular and generalized way utilizing reflection.
     * First it will attempt to get the name of the modus package. after acquiring the name it will then convert it into
     * a resource to find the URL and URI path of the modus package so that it can convert it to a directory. it will
     * then iterate through the directory and create a list of the class files contained. this will first be performed
     * for a file system and then for a jarfile system.
     */
    private List<String> createClassNameList() {
        String pkgname = Modus.class.getPackage().getName();
        List<String> classNameList = new ArrayList<>();

        // Get a File object for the package
        File directory = null;
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
                                       ") does not appear to be a valid URL / URI.  Strange, since we got it from the system...",
                                       e);
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
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();
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
