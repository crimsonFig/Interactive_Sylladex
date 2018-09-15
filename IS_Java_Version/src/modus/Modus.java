package modus;

import java.util.LinkedHashMap;

import app.controller.Sylladex;
import app.model.Card;
import app.model.Metadata;

/**
 * A "Fetch Modus" is a module that tells the sylladex how to manage it's
 * Card(s). <br>
 * This interface provides the bare minimum required for proper manipulation of
 * Card(s) with respect to the Sylladex <br>
 * <br>
 * A Modus needs to be able to perform Card manipulation for item storage and
 * retrieval. In a sense, a Fetch Modus is a controller that is associated with 
 * a very specific model. Said model is simply stored in said controller for ease of
 * modularity.
 * 
 * @author Triston Scallan
 *
 */
public interface Modus {
	/*
	 * A reference to the Sylladex that called the given modus. <br> This is used to
	 * pass information back to the caller.
	 */
	// private Sylladex sylladexReference = null;
	/**
	 * provides information about this modus
	 */
	static final Metadata modusMetadata = null;

	///// Initialize

	/* public Modus(Sylladex sylladexReference);
	 * The constructor of a fetch Modus should save the reference to the sylladex so
	 * that it can functionally interact with the view and the sylladex.  
	 */

	/** 
	 * Creates a HashMap of the functions associated to this specific class. 
	 *  Subclasses need to override this method and reassign the functions associated
	 *  with the new class.
	 *  <p> IMPORTANT - for proper interactions with sylladex, the first four associations
	 *  should be as follows:
	 *  <br>(key) : (value)
	 *  <br>{@code save : 1}
	 *  <br>{@code load # : 2}
	 *  <br>{@code capture : 3}
	 *  <br>{@code takeOutCard : 4}
	 *  
	 *  <br> Any other function association should follow after.
	 *  the value `0` and `-1` is reserved for use in accessing the help functionality.
	 *  See {@link Modus#entry(int, Object...) entry()} method for more information regarding 
	 *  this. 
	 * @return a HashMap of the function name and the entry code
	 */
	public abstract LinkedHashMap<String, Integer> createFunctionMap();
	
	///// Access
	/**
	 * Access point for the modus. This method should act as a switch for the
	 *  sylladex to call functions from. the objects parameter will contain any args given
	 *  after a matching command.
	 * <p> The default case in the switch-case should be assumed that either the "help"
	 *  command was given or that a non-matching command was given. In all cases where
	 *  default would be invoked, a description of a command should be given.
	 *  The default case should follow this behavior:
	 *  <p> - if no matching commands causes the default case to be invoked, (case == -1) 
	 *  then send a message to output describing this situation, as well as a 
	 *  description of the "help" command's syntax. 
	 *  <p> - if the help command was matched, (case == 0), then assume `objects[0]` be
	 *  a string that is meant to contain the name of a command. A description of the
	 *  matching command's syntax and its arguments functionality, should be provided.
	 *  <p> - if the help command was match, (case == 0), and `objects[1]` is present and
	 *  is an integer equal to `1`, then follow the same behavior directly above but 
	 *  return the description as a string instead of printing to output. This is because
	 *  {@link app.controller.Sylladex#handleModusSelection Sylladex#handleModusSelection } 
	 *  will invoke this behavior to produce description labels in the modus command list.
	 * 
	 * @param functionCode
	 *            an integer associated with a function
	 * @param args
	 *            additional args
	 * @return an integer as an exit code
	 */
	public abstract String entry(int functionCode, String... args);
	
	/**
	 * Retrieve the {@link Sylladex} associated with this {@link Modus}
	 * 
	 * @return The {@code Sylladex} that owns {@code this} fetch modus
	 */
	public abstract Sylladex getSylladexReference();
	
	/**
	 * @return the METADATA
	 */
	public abstract Metadata getMETADATA();

	///// Save and Load
	/**
	 * Save the current state of the modus. <br>
	 * Saving the modus should create an ArrayList deck of Card to be given to the
	 * sylladex for storage.
	 * 
	 * @return an ArrayList of Card
	 */
	public abstract void save();

	/**
	 * Load the modus from a previous save state. <br>
	 * Load takes a deck from the sylladex and reads it into the modus' data
	 * structure for holding Card(s). <br>
	 * <br>
	 * Loading from a deck may have unintended behavior when swapping modi. It is up
	 * to the specific modus to handle reading a potentially foreign deck.
	 * 
	 * @param mode
	 *            an integer, 1 for automatic loading, 2 for manual loading, 0 for a
	 *            reset and no loading. 3 and beyond are for special loading.
	 * @throws Exception
	 *             anything that could go wrong from loading
	 */
	public abstract void load(int mode) throws Exception;

	///// IO
	/**
	 * Defines the method responsible for taking an item and pushing the CARD.
	 * 
	 * @param item
	 *            the item to be added
	 * @return True if successful
	 */
	public abstract Boolean capture(String item);

	/**
	 * Defines the method responsible for adding the CARD into the modus space. This
	 * is usually called by {@link #capture(String) #capture}, but can be used by
	 * any other method.
	 * 
	 * @param CARD
	 *            The Card to be added
	 * @return True if it successfully added the CARD
	 */
	public abstract Boolean addCard(Card card);

	/**
	 * Defines the method responsible for retrieving the CARD. There can be multiple
	 * ways of retrieval, but those should be called through this method.
	 * 
	 * @param objects
	 *            an array of needed inputs for the method
	 * @return a Card from the Modus space. If failure, Card will be an empty CARD.
	 */
	public abstract Card takeOutCard(Object... objects);

	///// Utility
	/**
	 * Determines if the modus has no empty cards AND can not add any more cards. If
	 * there is an empty CARD OR a CARD can be added, return FALSE.
	 * 
	 * @return state of deck
	 */
	public abstract Boolean isFull();

	/**
	 * Determines if the modus has no cards OR all cards are empty.
	 * 
	 * @return state of deck
	 */
	public abstract Boolean isEmpty();

	/**
	 * Takes a string of the desired item, and then attempts to match it to the
	 * closest matching string of {@link app.model.Card#item Card.item}.
	 * 
	 * @param givenItem
	 *            Item name that is desired to be retrieved
	 * @return the closest estimated name of an item currently stored.
	 */
	public abstract String findItemName(String givenItem);
	
	/**
	 * Draws the current inventory to the display. Should first clear the display.
	 */
	public abstract void drawToDisplay();
	
	/**
	 * Returns a string that provides a description of what the fetch modus is and what
	 * its storage quirk is. Should be implemented as a static method.
	 * @return a String description.
	 */
	public abstract String description();

}