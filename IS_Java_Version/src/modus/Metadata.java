package modus;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashMap;

/**
 * The Metadata class holds important information about a Modus' functionality
 * 	and abilities.
 * <p> Contains the NAME (String), REFERENCE (Modus), FUNCTION_MAP (String, Integer),
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
	 * a HashMap of all the functions for the associated Modus.
	 * 	Providing the function name (key) gives an integer (value)
	 *  that can be used by {@link Modus#entry()} to invoke the 
	 * 	associated function.
	 */
	public final LinkedHashMap<String, Integer> FUNCTION_MAP;
	/**
	 * a File reference to the directory of the associated Modus class file
	 */
	private File modusFile;
	
	/**
	 * Constructor for the Metadata class. establishes information
	 * 	and initialization of the constants.
	 * @param name
	 * @param functionMap
	 * @param reference
	 */
	public Metadata(
			String name, 
			LinkedHashMap<String, Integer> functionMap,
			Modus reference ) {
		this.NAME = name;
		this.FUNCTION_MAP = functionMap;
		this.REFERENCE = reference;
	}

	/**
	 * @return the modusFile
	 */
	public File getModusPath() {
		return modusFile;
	}

	/**
	 * @param modusFile the modusFile to set
	 */
	public void setModusPath(File modusFile) {
		this.modusFile = modusFile;
	}
	
	/**
	 * Used to allow the ModusManager to reach the modus file and load it in from outside
	 * of the jar file as a usable resource. Meant to facilitate in plug-and-play of modus 
	 * files while the sylladex was running.
	 * @return File object of a given Modus
	 * @throws URISyntaxException
	 */
	@Deprecated
	public File trackModusFile() throws URISyntaxException {
		//change the below code to use the path of a provided "resource folder" for the user to use OR
		//  open file chooser and select the given file directly.
		URL urlName = getClass().getResource(this.NAME + ".java"); 
		File result = new File(urlName.toURI());
		return result;
	}
}
