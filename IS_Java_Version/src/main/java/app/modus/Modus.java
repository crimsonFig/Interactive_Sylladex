package app.modus;

import app.model.Card;
import app.model.Metadata;
import app.model.ModusBuffer;

import java.util.List;

/**
 * A "Fetch Modus" is a module that tells the sylladex how to manage it's Card(s). <br/> This interface provides the bare minimum required
 * for proper manipulation of Card(s) with respect to the Sylladex <br/> <br/> A Modus needs to be able to perform Card manipulation for
 * item storage and retrieval. In a sense, a Fetch Modus is a controller that is associated with a very specific model. Said model is simply
 * stored in said controller for ease of modularity. <br/>
 * <h2>Implementation Guidelines</h2>
 * Sub-classes hould include the following:
 * <ul>
 * Insurance of compatibility of the deck's state between modus' data structures.
 * <li>A save method that converts this data structure to a card list</li>
 * <li>A load method that reads the cards from the list into this data structure</li>
 * </ul>
 *
 * @author Triston Scallan
 * @implSpec user should be allowed to ask for a card that is not-in-use, if the ability to select such a card is legal in the modus
 *         - however not-in-use cards should not be added to the empty hand. Items removed/ejected from cards should be passed to the open
 *         hand.
 */
@ModusMetatagRunStatus
public interface Modus {
    ///// Access

    /**
     * The {@link Metadata} object associated with this modus. Contains the command map, reference to this modus object, and modus class
     * name.
     *
     * @return the METADATA
     */
    Metadata getMETADATA();

    ///// Save and Load

    /**
     * Save the current state of the modus. <br> Saving the modus should create a List deck of Card to be given to the sylladex for
     * storage.
     *
     * @param modusBuffer
     *         the modus buffer to save to
     * @implSpec The List should be in the universal list format to be compliant with the sylladex. The modus should not add nulls.
     *         Not-in-use cards are permitted to be saved, however.
     */
    List<Card> save(ModusBuffer modusBuffer);

    /**
     * Load the modus from a previous save state. <br> Load takes a deck from the sylladex and reads it into the modus' data structure for
     * holding Card(s). <br>
     * <br>
     * Loading from a deck may have unintended behavior when swapping modi. It is up to the specific modus to handle reading a potentially
     * foreign deck.
     *
     * @param modusBuffer
     *         an integer, 1 for automatic loading, 2 for manual loading, 0 for a reset and no loading. 3 and beyond are for special
     *         loading.
     */
    void load(ModusBuffer modusBuffer);

    ///// Utility

    /**
     * Converts the current inventory state of the modus into a Modus compatible one dimensional deck of Cards
     *
     * @return a list of Cards derived from the current inventory state
     */
    List<Card> toDeck();

    /**
     * Draws the current inventory to the display. Should first clear the display.
     */
    void drawToDisplay(ModusBuffer modusBuffer);

    /**
     * Returns a string that provides a description of what the fetch modus is and what its storage quirk is.
     *
     * @return a String description.
     */
    String description();

}
