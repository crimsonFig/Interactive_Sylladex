package modus;

import java.util.*;

import app.controller.Sylladex;
import app.model.Card;

/**
 * This fetch modus is based on the idea of a deck of cards. The intended usage is to insert 
 * 	item filled Cards into the {@link #deck} and then to retrieve an item, you shuffle the
 * 	deck and pull a card from the top of the deck. The success for retrieving the <i>desired</i>
 * 	item is fully luck based. Ofcourse, one could cheat and look at each card at a time to 
 * 	find their desired item and pull from that index; if <i>cheating</i> is desired, see the
 * 	extension of this modus: (see also link)TrueSightDeck. Stack
 * @author Triston Scallan
 * @see app.model Cards
 * @see TrueSightDeck 
 * @version 1.0
 * <dt> Notes: </dt> <dd>
 * Functionality for replacing the deck with items of a complete tarot deck can be accomplished
 * 	with the method: (link method)convertToTarot
 *
 */
public class TarotDeck implements Modus {
	/**
	 * A reference to the Sylladex that called the given modus. <br>
	 * This is used to pass information back to the caller.
	 */
	protected Sylladex sylladexReference = null;
	/** A stack based data structure */
	protected List<Card> deck = new Stack<Card>(); //use either Stack or Deque
	
	
	/**
	 * @param sylladexReference
	 */
	public TarotDeck(Sylladex sylladexReference) {
		this.sylladexReference = sylladexReference;
	}
	
	/* (non-Javadoc)
	 * @see modus.Modus#entry()
	 */
	@Override
	public int entry(int functionCode, Object...objects) {
		return 0;
	}
	/* (non-Javadoc)
	 * @see modus.Modus#save()
	 */
	@Override
	public void save() {
	}
	/* (non-Javadoc)
	 * @see modus.Modus#load(int)
	 */
	@Override
	public void load(int mode) throws Exception {
	}
	/* (non-Javadoc)
	 * @see modus.Modus#capture(java.lang.String)
	 */
	@Override
	public Boolean capture(String item) {
		return null;
	}
	/* (non-Javadoc)
	 * @see modus.Modus#addCard(app.model.Card)
	 */
	@Override
	public Boolean addCard(Card card) {
		return null;
	}
	/* (non-Javadoc)
	 * @see modus.Modus#takeOutCard(java.lang.Object[])
	 */
	@Override
	public Card takeOutCard(Object... objects) {
		return null;
	}
	/* (non-Javadoc)
	 * @see modus.Modus#getSylladexReference()
	 */
	@Override
	public Sylladex getSylladexReference() {
		return null;
	}
	/* (non-Javadoc)
	 * @see modus.Modus#isFull()
	 */
	@Override
	public Boolean isFull() {
		return null;
	}
	/* (non-Javadoc)
	 * @see modus.Modus#isEmpty()
	 */
	@Override
	public Boolean isEmpty() {
		return null;
	}
	/* (non-Javadoc)
	 * @see modus.Modus#findItemName(java.lang.String)
	 */
	@Override
	public String findItemName(String givenName) {
		return null;
	}
	
	//Arrays.sort(setFolder, Comparator.comparing((Card card) -> card.getItem()));
}
