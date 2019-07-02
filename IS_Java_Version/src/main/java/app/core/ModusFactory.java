package app.core;

import app.model.Metadata;
import app.modus.Modus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
class ModusFactory {
    private static final Logger LOGGER = LogManager.getLogger(ModusFactory.class);

    /**
     * Factory method for instantiating a Modus object from a Class object.
     *
     * @param modusClass
     *         the class object to instantiate the Modus subclassed object
     * @return a Metadata object containing a reference to an instance of the passed in class object
     *
     * @throws RuntimeException
     *         if the given class is unable to be accessed from this scope, if the instantiation process fails due to a dependent class
     */
    @Nonnull
    static Metadata getModusMetadata(Class<? extends Modus> modusClass) throws RuntimeException {
        Modus modusInstance;
        try {
            modusInstance = modusClass.newInstance();
        } catch (InstantiationException e) {
            LOGGER.error("Could not instantiate the given modus class: " + modusClass.getSimpleName(), e);
            throw new RuntimeException("Could not instantiate the given modus class: " + modusClass.getSimpleName(), e);
        } catch (SecurityException | IllegalAccessException e) {
            LOGGER.error("Class to be instantiated is out of access scope: " + modusClass.getSimpleName(), e);
            throw new RuntimeException("Class to be instantiated is out of access scope: " + modusClass.getSimpleName(), e);
        }
        Metadata result = modusInstance.getMETADATA();
        if (!Metadata.isValid(result, modusClass)) {
            LOGGER.error("Modus's Metadata object failed validation: " + modusClass.getSimpleName());
            throw new RuntimeException("Failed Metadata validation for modus class: " + modusClass.getSimpleName());
        }
        LOGGER.info("ClassLoading: classSuccess = " + modusClass.getSimpleName());
        return result;
    }
}
