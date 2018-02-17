package modus;

import java.util.LinkedHashMap;
import java.util.ListIterator;

import app.controller.Sylladex;
import app.model.Card;

/**
 * @author Triston Scallan
 *
 */
public class TrueSightDeck extends TarotDeck {

	//inherited Sylladex sylladexReference = null;
	//inherited List<Card> deck = new Stack<Card>(); 	
	//inherited final static int SHUFFLE_VAL;
	
	//***************************** INITIALIZE ***********************************/		
	/**	
	 * Constructor for the TrueSightDeck
	 * @param sylladexReference
	 */
	public TrueSightDeck(Sylladex sylladexReference) {
		super(sylladexReference);
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
	public String entry(int functionCode, Object...objects) {
		//TODO: finish the entry function and the drawToDisplay
		return "0";
	}
	
	//********************************** IO ***************************************/
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
	
	//****************************** UTILITY ************************************/	
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

	/* (non-Javadoc)
	 * @see modus.Modus#drawToDisplay()
	 */
	@Override
	public void drawToDisplay() {
		//TODO:Finish this
	}
}
