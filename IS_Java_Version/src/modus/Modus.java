package modus;

import app.model.Card;
import app.model.Metadata;
import javafx.scene.layout.StackPane;

import java.util.List;

/** TODO: reduce interface by pruning methods not to be called by other classes. instead comment desired functionality.
 *  	(perhaps be composed of abstract classes, which the class may provide implementation for but allow it to
 *  	signal types for it's functionality. "can do x" and such.)
 * A "Fetch Modus" is a module that tells the sylladex how to manage it's
 * Card(s). <br/>
 * This interface provides the bare minimum required for proper manipulation of
 * Card(s) with respect to the Sylladex <br/>
 * <br/>
 * A Modus needs to be able to perform Card manipulation for item storage and
 * retrieval. In a sense, a Fetch Modus is a controller that is associated with 
 * a very specific model. Said model is simply stored in said controller for ease of
 * modularity.
 * <br/>
 * <h2>Implementation Guidelines</h2>
 * Sub-classes hould include the following:
 * <ul>
 *     Insurance of compatibility of the deck's state between modus' data structures.
 *     <li>A save method that converts this data structure to a card list</li>
 *     <li>A load method that reads the cards from the list into this data structure</li>
 * </ul>
 * @author Triston Scallan
 *
 */
@ModusMetatagRunStatus
public interface Modus {
	///// Access
	/**
	 * The {@link Metadata} object associated with this modus.
	 * Contains the command map, reference to this modus object, and modus class name.
	 *
	 * @return the METADATA
	 */
	Metadata getMETADATA();

	///// Save and Load
	/**
	 * Save the current state of the modus. <br>
	 * Saving the modus should create a List deck of Card to be given to the
	 * sylladex for storage.
	 *
	 */
	List<Card> save();

	/**
	 * Load the modus from a previous save state. <br> Load takes a deck from the sylladex and reads it into the modus'
	 * data structure for holding Card(s). <br>
	 * <br>
	 * Loading from a deck may have unintended behavior when swapping modi. It is up to the specific modus to handle
	 * reading a potentially foreign deck.
	 *
	 * @param mode
	 *         an integer, 1 for automatic loading, 2 for manual loading, 0 for a reset and no loading. 3 and beyond are
	 *         for special loading.
	 */
	void load(ModusBuffer modusBuffer);

	///// Utility
	
	/**
	 * Draws the current inventory to the display. Should first clear the display.
	 */
	void drawToDisplay(ModusBuffer modusBuffer);
	
	/**
	 * Returns a string that provides a description of what the fetch modus is and what
	 * its storage quirk is.
	 * @return a String description.
	 */
	String description();

}
