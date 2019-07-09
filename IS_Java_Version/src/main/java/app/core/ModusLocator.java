package app.core;

import app.modus.Modus;
import app.modus.ModusMetatagRunStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

class ModusLocator {
    private static final Logger LOGGER = LogManager.getLogger(ModusLocator.class);


    @Nonnull
    static List<Class<? extends Modus>> getModiAsClassList() throws RuntimeException {
        return getModiAsClassList(createClassNameList());
    }

    //convert class names to class objects, filter out invalid modus classes, and collect
    @Nonnull
    static List<Class<? extends Modus>> getModiAsClassList(List<String> classNameList) {
        if (classNameList == null || classNameList.isEmpty()) return Collections.emptyList();
        return classNameList.stream()
                            //convert the names into class objects
                            .map(ModusLocator::getModiAsClass).filter(ModusLocator::validateModusFile).collect(Collectors.toList());
    }

    @CheckForNull
    static Class<? extends Modus> getModiAsClass(String className) {
        Class<? extends Modus> modusClass = null;
        try {
            modusClass = Class.forName(className, false, Modus.class.getClassLoader())
                              .asSubclass(Modus.class);
        } catch (ClassNotFoundException e) {
            LOGGER.warn("ClassListing: {} was listed as a class but no definition was found.", className);
        } catch (ClassCastException e) {
            LOGGER.warn("ClassListing: ignoring found non-Modus derived class `{}`.", className);
        }
        return modusClass;
    }

    /**
     * gets a list of all the modus class names. This is done in a modular and generalized way utilizing reflection. First it will attempt
     * to get the name of the modus package. after acquiring the name it will then convert it into a resource to find the URL and URI path
     * of the modus package so that it can convert it to a directory. it will then iterate through the directory and create a list of the
     * class files contained. this will first be performed for a file system and then for a jarfile system.
     */
    @Nonnull
    static List<String> createClassNameList() throws RuntimeException {
        // Get a File object for the package
        String pkgname = Modus.class.getPackage().getName();
        LOGGER.info("ClassDiscovery: Package = " + pkgname);
        URL resource = ClassLoader.getSystemClassLoader().getResource(pkgname.replace('.', File.separatorChar));
        if (resource == null) throw new RuntimeException("No resource for " + pkgname);
        LOGGER.info("ClassDiscovery: Resource = " + resource);
        String fullPath = resource.getFile();
        LOGGER.info("ClassDiscovery: FullPath = " + fullPath);

        File directory = null;
        try {
            directory = new File(resource.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(pkgname +
                                       " (" +
                                       resource +
                                       ") does not appear to be a valid URL / URI.  Strange, since we got it from the system...", e);
        } catch (IllegalArgumentException ignorable) {
            LOGGER.catching(ignorable);
        }
        LOGGER.info("ClassDiscovery: Directory = " + directory);

        List<String> classNameList = new ArrayList<>();
        if (directory != null && directory.exists()) {
            // Get the list of the files contained in the package
            List<String> files = Arrays.asList(Objects.requireNonNull(directory.list()));
            files.sort(String::compareTo);
            for (String file : files) {
                // we are only interested in .class files
                if (file.endsWith(".class")) {
                    // removes the .class extension
                    String className = pkgname + '.' + file.substring(0, file.length() - 6);
                    LOGGER.info("ClassDiscovery: className = " + className);
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
                        LOGGER.info("ClassDiscovery: JarEntry: " + entryName);
                        String className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "");
                        LOGGER.info("ClassDiscovery: className = " + className);
                        classNameList.add(className);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(pkgname + " (" + directory + ") does not appear to be a valid package", e);
            }
        }
        LOGGER.info("ClassDiscovery: complete.");
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
    private static <M extends Modus> boolean validateModusFile(Class<M> modusClass) {
        if (modusClass == null) return false;
        ModusMetatagRunStatus runStatusAnnot = modusClass.getAnnotation(ModusMetatagRunStatus.class);
        //currently, checking the run status tag is sufficient for validation. add more as needed/thought of.
        return (runStatusAnnot != null) && runStatusAnnot.value();
    }
}
