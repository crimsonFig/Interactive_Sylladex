package app.modus;

import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import app.model.Card;
import app.model.ModusBuffer;
import app.util.ModusCommandMap;

/**
 * @author Triston Scallan
 *
 */
public class TrueSightDeck extends TarotDeck {

	//inherited List<Card> deck = new Stack<Card>(); 	
	//inherited int SHUFFLE_VAL;
	
	//***************************** INITIALIZE ***********************************/		
	/**	
	 * Constructor for the TrueSightDeck
	 */
	public TrueSightDeck() {
		super();
	}

	@Override
	protected ModusCommandMap createFunctionMap() {
		return null;
	}
	
	//***************************** ACCESS *************************************/	

	
	//********************************** IO ***************************************/
	/**
	 * Draws the index'th CARD from the deck.
	 * @param index the desired index
	 * @return the CARD at index, an empty CARD if unable to.
	 */
	public Card takeOutCardByIndex(int index) {
		if (index > deck.size() - 1 || index < 0) return new Card(); //if invalid index, return an empty CARD
		
		Card card;
		ListIterator<Card> deckTop = this.deck.listIterator();
		for (int i = 0; deckTop.hasNext(); i++) {
			card = deckTop.next();
			if (i == index) {
				deck.remove(i);
				return card;
			}
		}
		return new Card();
	}
	
	//****************************** UTILITY ************************************/	
	/**
	 * given an item's name, finds the index location within the deck of a CARD that matches the name.
	 * @param itemName the given name to find
	 * @return the CARD location's index, -1 if not found
	 */
	private int findCardIndexByName(String itemName) {
		int i = 0;
		for(Card card : deck) {
			if (card.getItem().equals(itemName)) return i;
			i++;
		}
		return -1;
	}

	@Override
	public void drawToDisplay(ModusBuffer modusBuffer) {
		//TODO:Finish this
	}

	@Override
	public String description() {
		return null;
	}
}
