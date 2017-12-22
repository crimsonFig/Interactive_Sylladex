package modus;

/**
 * @author Triston Scallan
 *
 */
public interface Modus {

	///// Initialize
	/**
	 * 	Access point for the modus. This method should take control of the sylladex.
	 */
	public abstract void entry();
	
	///// Save and Load
	/**
	 * 	Save the current state of the modus.
	 */
	public abstract void save();
	
	/**
	 *	Load the modus from a previous save state.
	 */
	public abstract void load();
	
	///// IO
	/**
	 *	Defines the method responsible for taking an item and pushing the card.
	 */
	public abstract void capture();
	
	/**
	 * 	Defines the method responsible for adding the card into the modus space.
	 * 	This is usually called by `capture`, but can be used by any other method.
	 */
	public abstract void addCard();
	
	/**
	 *	Defines the method responsible for retrieving the card.
	 *	There can be multiple ways of retrieval, but those should
	 *	be called through this method.
	 */
	public abstract void takeOutCard();
	
	///// Utility
	/**
	 * 	Determines if the modus has no empty cards AND can not add any more cards.
	 * 	If there is an empty card OR a card can be added, return FALSE.
	 */
	public abstract void isFull();
	
	/**
	 *	Determines if the modus has no cards OR all cards are empty.
	 */
	public abstract void isEmpty();
	
}
