package modus;

import java.util.LinkedHashMap;
import java.util.ListIterator;

import app.model.Card;

/**
 * @author Triston Scallan
 *
 */
public class TrueSightDeck extends TarotDeck {

	//inherited List<Card> deck = new Stack<Card>(); 	
	//inherited final static int SHUFFLE_VAL;
	
	//***************************** INITIALIZE ***********************************/		
	/**	
	 * Constructor for the TrueSightDeck
	 * @param sylladexReference
	 */
	public TrueSightDeck() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see modus.Modus#createFunctionMap()
	 */
	@Override
	public LinkedHashMap<String, Integer> createFunctionMap() {
		LinkedHashMap<String, Integer> functionMap = new LinkedHashMap<String, Integer>();
		functionMap.put("save", 0);
		functionMap.put("load #", 1); //mode = 0, 1, 2
		functionMap.put("capture", 2);
		functionMap.put("takeOutCard", 3);
		functionMap.put("takeOutCardByIndex", 4);
		functionMap.put("takeOutCardByName", 5);
		functionMap.put("findCardIndexByName", 6);
		return functionMap;
	}
	
	//***************************** ACCESS *************************************/	
	/* (non-Javadoc)
	 * @see modus.Modus#entry()
	 */
	@Override
	public String entry(int functionCode, String...args) {
		//TODO: finish the entry function and the drawToDisplay
		return "0";
	}
	
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
	
	/**
	 * Draws the CARD matching the given name
	 * @param itemName the given name to find
	 * @return the closest matching CARD, an empty CARD otherwise
	 */
	public Card takeOutCardByName(String itemName) {
		String match = findItemName(itemName);
		for (Card card : deck) {
			if (card.getItem().equals(match)) {
			    deck.remove(card);
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
	public int findCardIndexByName(String itemName) {
		int i = 0;
		Card card = Card.EMPTY;
		String match = findItemName(itemName);
		for(ListIterator<Card> deckTop = this.deck.listIterator(); deckTop.hasNext(); card = deckTop.next()) {
			if (card.getItem().equals(match)) return i;
			i++;
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see modus.Modus#drawToDisplay()
	 */
	@Override
	public void drawToDisplay() {
		//TODO:Finish this
	}
}
