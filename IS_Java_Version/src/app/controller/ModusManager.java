package app.controller;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;



import app.model.Metadata;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import modus.*;

/**
 * The ModusManager class provides support to the sylladex in
 * 	managing the modi in the modus package. This enables users
 * 	to take a valid and {@link modus.Modus Modus} conforming
 * 	java class and "plug" it into the sylladex for use. <br><br>
 * 	This class also tracks what modus is currently in use, and 
 * 	what modi are currently available, as well as metadata
 * 	about the particular modus, to help let the sylladex "know"
 * 	how to handle certain events and actions.
 * <p> Author's note: originally the direction of the Modus Manager was to allow users to add
 * a java file that complied with Modus interface to a file directory and then 
 * the sylladex could be refreshed and load the code given, but this form of 
 * plug-and-play of adding "valid" modus java files to
 * to a given directory for the manager to pick up for the sylladex to use
 * is a bit too insecure for my preference. it would be better to simply allow 
 * for a person to mod the open-source version of this by adding those java
 * files mentioned above to the modus package and then compile it into a jar.
 * Later, once the modusValidation function can be more robust and check for 
 * malicious behavior, then this direction can be resumed and utilize the files
 * through a URL filestream from getResourcesAsStream to temp files for the jar
 * to use until the sylladex is closed, or use file chooser which would allow the jar to keep track
 * of the various modus files wherever they happen to reside. would require re-
 * enabling the tracking of File paths through Metadata class. 
 * @author Triston Scallan
 * @see {@link modus.Modus modus.Modus}
 * @see {@link modus modus}
 */
public class ModusManager {
	/** Tracks the current active Modus as an index of {@link #modusList}. -1 means no active Modus. */
	private int currentModus = -1;
	/** Tracks all available Fetch Modi for the Sylladex */
	private List<Metadata> modusList = new ArrayList<Metadata>();
	
	/**
	 * Constructor
	 * @param syll reference to the Sylladex object
	 */
	public ModusManager(Sylladex syll) {
		//populate modusList
			/* This is done in a modular and generalized way utilizing reflection. 
			 * First it will attempt to get the name of the modus package.
			 * after acquiring the name it will then convert it into a resource to find
			 * the URL and URI path of the modus package so that it can convert it to a directory.
			 * it will then iterate through the directory and create a list of the class files contained.
			 * this will first be performed for a file system and then for a jarfile system.
			 * 
			 * then it will attempt to load each class as a Modus(interface) subclass. If it
			 * is successfully initialized using a specific constructor and casted then it will
			 * be added to the `modusList` field. Otherwise it will be ignored. 
			 * 
			 * NOTE: This means that a compiled non-jar form of java can have a third-party 
			 * Modus subclass Class file added to the modus package directory and possibly contain
			 * malicious code. Only the original, non-edited class files will be tested to work 
			 * earnestly. Use modded and third-party classes at your own risk. 
			 * TODO: the above note could be resolved using a function that scans a file and check
			 * it's validity. This could potentially be done by checking that it only performs
			 * certain function calls from a whitelisted list of allowed function calls. This 
			 * function would be performed after the classNameList is populated and before 
			 * the classes are loaded, performed on a filestream of the class file.
			 */
		String pkgname = Modus.class.getPackage().getName();
		List<String> classNameList = new ArrayList<String>();
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
		    throw new RuntimeException(pkgname + " (" + resource + ") does not appear to be a valid URL / URI.  Strange, since we got it from the system...", e);
		} catch (IllegalArgumentException e) {
		    directory = null;
		}
		System.out.println("ClassDiscovery: Directory = " + directory);
		
		if (directory != null && directory.exists()) {
		    // Get the list of the files contained in the package
		    List<String> files = Arrays.asList(directory.list());
		    files.sort((f1, f2) -> f1.compareTo(f2));
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
		    try (JarFile jarFile = new JarFile(jarPath)){       
		        Enumeration<JarEntry> entries = jarFile.entries();
		        while(entries.hasMoreElements()) {
		            JarEntry entry = entries.nextElement();
		            String entryName = entry.getName();
		            if(entryName.startsWith(pkgname) && entryName.length() > (pkgname.length() + "/".length())) {
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
		
		//TODO: attempt to validate the classes via filestream and match against whitelisted calls
		
		//classNameList should now be populated, attempt to load and cast each class.
		Modus modusObject = null;
		for(String className : classNameList) {
			try {
				Class<?> classObject = Class.forName(className, false, Modus.class.getClassLoader());
				Constructor<?> classConstructor = classObject.getConstructor(Sylladex.class);
				Object instanceObject = classConstructor.newInstance(syll);
				if (Modus.class.isInstance(instanceObject)) { //(this test auto checks against null too)
					modusObject = Modus.class.cast(instanceObject);
					modusList.add(modusObject.getMETADATA());
				}
			} catch (ClassCastException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException | LinkageError e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				continue;
			}
		}
		
		//if modusList is empty, warn the user
		if (modusList.isEmpty()) {
			Alert alert = new Alert(AlertType.WARNING);
		        	alert.setTitle("No Modi Found");
		        alert.setHeaderText("Modus list empty");
		        alert.setContentText("No modi was added to the modus list. This may be because the directory is empty or there was an issue adding modi to the list. Please refresh the list.");
	        alert.showAndWait();
		}
	}
	
	///// GETTERS/SETTERS
	/**
	 * @return the currentModus
	 */
	public Integer getCurrentModus() {
		return currentModus;
	}
	/**
	 * @param currentModus the currentModus to set
	 */
	public void setCurrentModus(Integer currentModus) {
		this.currentModus = currentModus;
	}
	/**
	 * @return the modusList
	 */
	public List<Metadata> getModusList() {
		return modusList;
	}
	/**
	 * @param modusList the modusList to set
	 */
	public void setModusList(List<Metadata> modusList) {
		this.modusList = modusList;
	}
	
	
	//TODO: function to update the modus tracker, based on "new" modus and scanned package
	
	//TODO: function to validate a modus file
	
	

}
