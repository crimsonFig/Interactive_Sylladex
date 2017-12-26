package modus;

import java.util.ListIterator;

import app.controller.Sylladex;
import app.model.Card;

/**
 * @author Triston Scallan
 *
 */
public class TrueSightDeck extends TarotDeck implements Modus {

	//inherited Sylladex sylladexReference = null;
	//inherited List<Card> deck = new Stack<Card>(); 
		
		
	/**
	 * @param sylladexReference
	 * 
	 */
	public TrueSightDeck(Sylladex sylladexReference) {
		super(sylladexReference);
	}
	
	/* (non-Javadoc)
	 * @see modus.Modus#entry()
	 */
	@Override
	public int entry(int functionCode, Object...objects) {
		return -1;
	}
	
	/**
	 * Draws the index'th card from the deck.
	 * @param index the desired index
	 * @return the card at index, an empty card if unable to.
	 */
	public Card takeOutCardByIndex(int index) {
		if (index > deck.size() - 1 || index < 0) return new Card(); //if invalid index, return an empty card
		
		Card card = null;
		ListIterator<Card> deckTop = this.deck.listIterator();
		for (int i = 0; deckTop.hasNext(); i++) {
			card = deckTop.next();
			if (i == index) return card;	
		}
		return new Card();
	}
	
	/**
	 * Draws the card matching the given name
	 * @param itemName the given name to find
	 * @return the closestnmatching card, an empty card otherwise
	 */
	public Card takeOutCardByName(String itemName) {
		String match = findItemName(itemName);
		for (Card card : deck) {
			if (card.getItem().equals(match)) return card;
		}
		return new Card();
	}
	
	/**
	 * given an item's name, finds the index location within the deck of a card that matches the name.
	 * @param itemName the given name to find
	 * @return the card location's index, -1 if not found
	 */
	public int findCardIndexByName(String itemName) {
		int i = 0;
		Card card = null;
		String match = findItemName(itemName);
		for(ListIterator<Card> deckTop = this.deck.listIterator(); deckTop.hasNext(); card = deckTop.next()) {
			if (card.getItem().equals(match)) return i;
			i++;
		}
		return -1;
	}

}
