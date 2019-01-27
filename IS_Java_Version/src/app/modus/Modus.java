package app.modus;

import app.model.Card;
import app.model.Metadata;
import app.model.ModusBuffer;

import java.util.List;

/**
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
 *     Insurance of compatibility of the deck's state between app.modus' data structures.
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
	 * The {@link Metadata} object associated with this app.modus.
	 * Contains the command map, reference to this app.modus object, and app.modus class name.
	 *
	 * @return the METADATA
	 */
	Metadata getMETADATA();

	///// Save and Load
	/**
	 * Save the current state of the app.modus. <br>
	 * Saving the app.modus should create a List deck of Card to be given to the
	 * sylladex for storage.
	 *
	 */
	//TODO: consider change to where saving contracts a 'save' to ModusBuffer. A list should be retrieved by `toDeck()`
	List<Card> save();

	/**
	 * Load the app.modus from a previous save state. <br> Load takes a deck from the sylladex and reads it into the app.modus'
	 * data structure for holding Card(s). <br>
	 * <br>
	 * Loading from a deck may have unintended behavior when swapping modi. It is up to the specific app.modus to handle
	 * reading a potentially foreign deck.
	 *
	 * @param modusBuffer
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
	 * Returns a string that provides a description of what the fetch app.modus is and what
	 * its storage quirk is.
	 * @return a String description.
	 */
	String description();

}
