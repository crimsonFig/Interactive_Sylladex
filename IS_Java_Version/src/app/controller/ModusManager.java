package app.controller;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;


import app.model.Metadata;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import modus.*;

/**
 * The ModusManager class provides support to the sylladex in managing the modi in the modus package. This enables users
 * to take a valid and {@link modus.Modus Modus} conforming java class and "plug" it into the sylladex for use. <br><br>
 * This class also tracks what modus is currently in use, and what modi are currently available, as well as metadata
 * about the particular modus, to help let the sylladex "know" how to handle certain events and actions.
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
 * @see modus.Modus
 */
class ModusManager {
    /** Tracks the current active Modus as an index of {@link #modusClassList}. -1 means no active Modus. */
    private Metadata                     currentModusMetadata;
    /** Tracks all available Fetch Modi for the Sylladex */
    private List<Class<? extends Modus>> modusClassList;

    /**
     * Constructor
     */
    ModusManager() {
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
                                              System.err.print("ModusManager construction: " +
                                                               className +
                                                               " was listed as a class but no definition was found.\n");
                                          } catch (ClassCastException e) {
                                              System.err.print("ModusManager construction: ignoring cast for '" +
                                                               className +
                                                               "'.\n");
                                          }
                                          return modusClass;
                                      })
                                      .filter(this::validateModusFile)
                                      .collect(Collectors.toList());
        System.out.println("ClassListing: complete.");

        //if modusClassList is empty, warn the user
        if (modusClassList.isEmpty()) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("No Modi Found");
            alert.setHeaderText("Modus list empty");
            alert.setContentText(
                    "No modi was added to the modus list. This may be because the directory is empty or there was an issue adding modi to the list. Exiting program.");
            alert.showAndWait();
            System.exit(-1);
        }
    }

    //************** GETTERS/SETTERS *******************/


    /**
     * @return the currentModusMetadata
     */
    Metadata getCurrentModusMetadata() {
        return currentModusMetadata;
    }

    /**
     * @return the modusClassList
     */
    List<Class<? extends Modus>> getModusClassList() {
        return modusClassList;
    }

    /**
     * @param modusClass
     *         the modus to update the selection to
     */
    <T extends Modus> void updateCurrentSelectedModus(Class<T> modusClass) throws IllegalAccessException, InstantiationException, IllegalArgumentException {
        //instantiate the desired class and set it to this#currentModusMetadata
        Modus clazzInstance = modusClass.newInstance();
        if (Metadata.isValid(clazzInstance.getMETADATA())) {
            System.out.println("ClassLoading: classSuccess = " + modusClass.getSimpleName());
            //replace old instance so it may be GC'd
            currentModusMetadata = clazzInstance.getMETADATA();
        } else {
            throw new IllegalArgumentException();
        }
    }


    //*************** UTILITY **************************/

    /**
     * Messages a modus command statement to the currently selected modus for execution
     *
     * @param command
     *         a modus command associated with a CommandMap entry
     * @param args
     *         the arguments to supply the command with
     */
    void execModusCmd(String command, String... args) {
        System.out.println("invoking `" + command + "` with args = " + Arrays.toString(args));
        currentModusMetadata.COMMAND_MAP.command(command, args);

    }

    /**
     * Initializes a new object of the current modus class, then effectively replaces the old object's reference.
     */
    void refreshModus() {
        Class<? extends Modus> modusClassObject = getCurrentModusMetadata().REFERENCE.getClass();
        try {
            Modus modusInstance = modusClassObject.newInstance();
            currentModusMetadata = modusInstance.getMETADATA();
        } catch (SecurityException | IllegalAccessException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (IllegalArgumentException | InstantiationException e) {
            //These exceptions should never happen if it didn't happen in during it's first initialization
            //I would consider this a fatal issue that needs to be addressed by debugging.
            e.printStackTrace();
            System.out.println("An unusual and fatal error that should never happen has occurred.");
            System.exit(-1);
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
        URL resource = ClassLoader.getSystemClassLoader().getResource(pkgname);
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
