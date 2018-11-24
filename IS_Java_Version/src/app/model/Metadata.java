package app.model;

import modus.Modus;

/**
 * The Metadata class is a container for information about a Modus' functionality
 * 	and abilities.
 * <p> Contains the NAME (String), REFERENCE (Modus), COMMAND_MAP (String, Integer),
 * 	and modusFile (File) of a given modus java file.
 * @author Triston Scallan
 *
 */
public class Metadata {
	/**
	 * the String name of the Modus' class name
	 */
	public final String NAME;
	/**
	 * a copy of the associated Modus' reference.
	 */
	public final Modus REFERENCE;
	/**
	 * a mapping between a modus' command name and the associated command lambda.
	 */
	public final CommandMap COMMAND_MAP;
	/**
	 * Constructor for the Metadata class. establishes information
	 * 	and initialization of the constants.
	 * @param name {@link #NAME}
	 * @param commandMap {@link #COMMAND_MAP}
	 * @param reference {@link #REFERENCE}
	 */
	public Metadata(
			String name, 
			CommandMap commandMap,
			Modus reference ) {
		this.NAME = name;
		this.COMMAND_MAP = commandMap;
		this.REFERENCE = reference;
	}

    public static boolean isValid(Metadata metadata) {
	    //reference is not null, the name equals class name, and command map is not null and valid
        return (metadata != null &&
                metadata.REFERENCE != null &&
                metadata.NAME.equals(metadata.REFERENCE.getClass().getSimpleName()) &&
                CommandMap.isValid(metadata.COMMAND_MAP));
    }
}
