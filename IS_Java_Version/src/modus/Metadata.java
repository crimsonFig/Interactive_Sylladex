package modus;

import java.io.File;
import java.util.HashMap;

/**
 * The Metadata class holds important information about a Modus' functionality
 * 	and abilities.
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
	 * a HashMap of all the functions for the associated Modus.
	 * 	Providing the function name (key) gives an integer (value)
	 *  that can be used by {@link Modus#entry()} to invoke the 
	 * 	associated function.
	 */
	public final HashMap<String, Integer> FUNCTION_MAP;
	/**
	 * a File reference to the directory of the associated Modus class file
	 */
	private File modusPath;
	
	/**
	 * Constructor for the Metadata class. establishes information
	 * 	and initialization of the constants.
	 * @param name
	 * @param functionMap
	 * @param reference
	 */
	public Metadata(
			String name, 
			HashMap<String, Integer> functionMap,
			Modus reference ) {
		this.NAME = name;
		this.FUNCTION_MAP = functionMap;
		this.REFERENCE = reference;
	}

	/**
	 * @return the modusPath
	 */
	public File getModusPath() {
		return modusPath;
	}

	/**
	 * @param modusPath the modusPath to set
	 */
	public void setModusPath(File modusPath) {
		this.modusPath = modusPath;
	}
	
	//TODO: create a function to determine the file path of this modus.
}
