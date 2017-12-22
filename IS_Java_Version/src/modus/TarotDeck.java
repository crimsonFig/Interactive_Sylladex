package modus;

import java.util.*;

import app.model.Card;

/**
 * This fetch modus is based on the idea of a deck of cards. The intended usage is to insert 
 * 	item filled Cards into the {@link #deck} and then to retrieve an item, you shuffle the
 * 	deck and pull a card from the top of the deck. The success for retrieving the <i>desired</i>
 * 	item is fully luck based. Ofcourse, one could cheat and look at each card at a time to 
 * 	find their desired item and pull from that index; if <i>cheating</i> is desired, see the
 * 	extension of this modus: (see also link)TrueSightDeck. 
 * @author Triston Scallan
 * @see Cards
 * @see TrueSightDeck 
 * @version 1.0
 * <dt> Notes: </dt> <dd>
 * Functionality for replacing the deck with items of a complete tarot deck can be accomplished
 * 	with the method: (link method)convertToTarot
 *
 */
public class TarotDeck implements Modus {
	/** A stack based data structure */
	List<Card> deck = new LinkedList<Card>();
	ListIterator<Card> deckTop = deck.listIterator();

	/* (non-Javadoc)
	 * @see modus.Modus#entry()
	 */
	@Override
	public void entry() {
		
	}

	/* (non-Javadoc)
	 * @see modus.Modus#save()
	 */
	@Override
	public void save() {
	}

	/* (non-Javadoc)
	 * @see modus.Modus#load()
	 */
	@Override
	public void load() {
	}

	/* (non-Javadoc)
	 * @see modus.Modus#capture()
	 */
	@Override
	public void capture() {
	}

	/* (non-Javadoc)
	 * @see modus.Modus#addCard()
	 */
	@Override
	public void addCard() {
	}

	/* (non-Javadoc)
	 * @see modus.Modus#takeOutCard()
	 */
	@Override
	public void takeOutCard() {
	}

	/* (non-Javadoc)
	 * @see modus.Modus#isFull()
	 */
	@Override
	public void isFull() {
	}

	/* (non-Javadoc)
	 * @see modus.Modus#isEmpty()
	 */
	@Override
	public void isEmpty() {
	}

}
