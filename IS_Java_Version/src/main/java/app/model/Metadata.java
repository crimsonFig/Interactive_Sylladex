package app.model;

import app.modus.Modus;
import app.util.ModusCommandMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The Metadata class is a container for information about a Modus' functionality and abilities.
 * <p> Contains the NAME (String), REFERENCE (Modus), COMMAND_MAP (String, Integer),
 * and modusFile (File) of a given modus java file.
 *
 * @author Triston Scallan
 */
public class Metadata {
    /**
     * the String name of the Modus' class name
     */
    public final String          NAME;
    /**
     * a copy of the associated Modus' reference.
     */
    public final Modus           REFERENCE;
    /**
     * a mapping between a modus' command name and the associated command lambda.
     */
    public final ModusCommandMap COMMAND_MAP;

    /**
     * Constructor for the Metadata class. establishes information and initialization of the constants.
     *
     * @param name
     *         {@link #NAME}
     * @param modusCommandMap
     *         {@link #COMMAND_MAP}
     * @param reference
     *         {@link #REFERENCE}
     */
    public Metadata(String name, ModusCommandMap modusCommandMap, Modus reference) {
        this.NAME = name;
        this.COMMAND_MAP = modusCommandMap;
        this.REFERENCE = reference;
    }

    /**
     * Checks if the supplied metadata object is valid.
     *
     * @param metadata
     *         the object to check validation against.
     * @return true if and only if the metadata object is not null, the REFERENCE is not null and is a subclass instance of Modus, the NAME
     *         is not null and matches the simple class name of the REFERENCE object's class, and the COMMAND_MAP is considered valid.
     *
     * @see ModusCommandMap#isValid(ModusCommandMap)
     */
    public static boolean isValid(@Nullable Metadata metadata) {
        //reference is not null, the name equals class name, and command map is not null and valid
        return (metadata != null &&
                metadata.REFERENCE != null &&
                metadata.NAME != null &&
                metadata.NAME.equals(metadata.REFERENCE.getClass().getSimpleName()) &&
                ModusCommandMap.isValid(metadata.COMMAND_MAP));
    }

    /**
     * Checks if the supplied metadata object is valid according to {@link Metadata#isValid(Metadata)} as well as enables checking if the
     * reference instance is actually the same class as the one caller intended to instantiate.
     *
     * @param metadata
     *         the object to check validation against.
     * @param clazz
     *         the Class object to check against the REFERENCE object.
     * @return true if and only if the metadata object is valid according to {@link Metadata#isValid(Metadata)} and the REFERENCE object's
     *         simple class name matches the simple class name of the class object passed in.
     */
    public static boolean isValid(@Nullable Metadata metadata, @Nonnull Class<? extends Modus> clazz) {
        //reference object is the same class as the clazz supplied
        return Metadata.isValid(metadata) && metadata.REFERENCE.getClass().getCanonicalName().equals(clazz.getCanonicalName());
    }
}
