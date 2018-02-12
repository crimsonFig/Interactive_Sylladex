package app.controller;

import java.util.*;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import modus.*;
import modus.Metadata;

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
			//the proper way to generalize this to work for any and all modus is to save the metadata information
			//  as plaintext set within comments on a specific line of each modus file. Then the iterate over all 
			//  files within the directory of the modus package, look for that metadata information, validate it, 
			//  and if valid then create a metadata object from the file and add to our modusList.
			//  For ease sake of prototyping, explicitly call each modus below. vvv
		modusList.add((new PentaFile(syll)).getMETADATA());
		modusList.add((new TarotDeck(syll)).getMETADATA());
		modusList.add((new TrueSightDeck(syll)).getMETADATA());
	  //modusList.add((new TimeBox(syll)).getMETADATA());
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
	
	
	//TODO: function for reading in a new modus file 
		//should return a List<object> containing a File and metadata object. Should use getResourceAsStream. See class comments.
	
	//TODO: function for scanning the current modus package 
		//should return a list of URI/URL/File/path names to each modus
	
	//TODO: function to update the modus tracker, based on "new" modus and scanned package
	
	//TODO: function to validate a modus file
	
	

}
