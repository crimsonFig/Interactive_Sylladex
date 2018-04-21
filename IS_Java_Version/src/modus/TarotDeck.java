package modus;

import java.util.*;

import app.controller.Sylladex;
import app.model.Card;
import app.model.Metadata;
import commandline_utils.Searcher;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;

/**
 * This fetch modus is based on the idea of a deck of cards. The intended usage is to insert 
 * 	item filled Cards into the {@link #deck} and then to retrieve an item, you shuffle the
 * 	deck and pull a card from the top of the deck. The success for retrieving the <i>desired</i>
 * 	item is fully luck based. Ofcourse, one could cheat and look at each card at a time to 
 * 	find their desired item and pull from that index; if cheating <i>is</i> desired then see the
 * 	extension of this modus: {@link TrueSightDeck}. 
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
	protected Sylladex sylladexReference;
	/**
	 * provides information about this modus.
	 * private so that inherited classes don't clash with this
	 */
	protected final Metadata METADATA; 
	/**
	 * Describes how many times {@link #shuffleDeck()} will fully shuffle the cards when invoked.
	 * Currently set to {@value #SHUFFLE_VAL}. 
	 */
	protected final static int SHUFFLE_VAL = 9;
	
	/** A Stack based data structure */
	protected Stack<Card> deck = new Stack<Card>(); //use either Stack or Deque
	
	//***************************** INITIALIZE ***********************************/
	/**
	 * Constructor for TarotDeck class
	 * @param sylladexReference
	 */
	public TarotDeck(Sylladex sylladexReference) {
		this.sylladexReference = sylladexReference;
		
		//initialize the METADATA
		this.METADATA = new Metadata(this.getClass().getSimpleName(), this.createFunctionMap(), this);
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
		functionMap.put("shuffle", 4);
		return functionMap;
	}
	
	//***************************** ACCESS *************************************/
	/* (non-Javadoc)
	 * @see modus.Modus#entry()
	 */
	@Override
	public String entry(int functionCode, String...args) {
		switch (functionCode) {
		case 0: //save
			save();
			break;
		case 1: //load <mode>
			//based on mode as objects[0], use that load mode. if doesn't match 0, 1, 2, or 3 then invoke entry(-1, "command name") to display help to the output
			if (args.length == 1 && args[0] instanceof String) {
				switch ((String) args[0]) {
				case "0":
					load(0);
					drawToDisplay();
					break;
				case "1":
					load(1);
					drawToDisplay();
					break;
				case "2":
					load(2);
					drawToDisplay();
					break;
				default:
					entry(-1, "help load");
					return "-1";
				}
			} else {
				entry(-1, "help load");
				return "-1";
			}
			break;
		case 2: //capture <item name>
			if (args.length == 1 && args[0] instanceof String) {
				if(! capture((String) args[0])) return "-1";
				save();
				drawToDisplay();
			} else {
				entry(-1, "help capture");
				return "-1";
			}
			break;
		case 3: //takeOutCard
			List<Card> tempDeck = new ArrayList<Card>();
			Card tempCard = takeOutCard();
			if (! tempCard.getInUse()) return "-1";
			tempDeck.add(tempCard);
			sylladexReference.addToOpenHand(tempDeck);
			save();
			drawToDisplay();
			break;
		case 4: //shuffle
			shuffleDeck();
			drawToDisplay();
			break;
		}
		return "0";
	}

	/**
	 * @return the METADATA
	 */
	public Metadata getMETADATA() {
		return METADATA;
	}
	
	/*
	 * (non-Javadoc)
	 * @see modus.Modus#getSylladexReference()
	 */
	public Sylladex getSylladexReference() {
		return sylladexReference;
	}
	
	//**************************** SAVE & LOAD ********************************/
	/* (non-Javadoc)
	 * @see modus.Modus#save()
	 */
	@Override
	public void save() {
		sylladexReference.setDeck(this.deck);
	}
	/* (non-Javadoc)
	 * @see modus.Modus#load(int)
	 */
	@Override
	public void load(int mode) {
		List<Card> _deck = sylladexReference.getDeck();
		//reset the modus space. if mode 0, return after this step.
		this.deck = new Stack<Card>();
		////automatic loading mode
		if (mode == 1) {
			//if sylladex's deck is invalid, skip the assignment.
			//otherwise, iterate through the sylladex deck and push all valid cards (or empty cards if invalid)
			if (_deck == null || _deck.isEmpty()) return;
			for (Card card : _deck) {
				this.deck.push(card.validateCard() ? card : new Card());
			}
		////manual loading mode
		} else if (mode == 2) {
			//if sylladex's deck is invalid, skip the assignment.
			//otherwise, iterate through the sylladex deck and push cards that the user accepts
			if (_deck == null || _deck.isEmpty()) return;
			for (Card card : _deck) {
				if (!card.validateCard()) continue;
				//ask the user about the card
				Alert alert = new Alert(AlertType.CONFIRMATION);
			        	alert.setTitle("Card Selection");
			        alert.setHeaderText("Add Card to Deck?");
			        alert.setContentText(card.toString());	
		        //create buttons for alert
		        ButtonType buttonYes = new ButtonType("Yes", ButtonData.YES);
		        ButtonType buttonNo = new ButtonType("No", ButtonData.NO);
		        ButtonType buttonCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		        alert.getButtonTypes().setAll(buttonYes, buttonNo, buttonCancel);
		        
		        Optional<ButtonType> result = alert.showAndWait();
		        if (result.get().getButtonData().equals(ButtonData.CANCEL_CLOSE)) {
		        		break;
		        } else if (result.get() == buttonYes) {
		        		this.deck.push(card);
		        } else {
		        		continue;
		        }
			}
		}
	}
	
	//********************************** IO ***************************************/
	/* (non-Javadoc)
	 * @see modus.Modus#capture(java.lang.String)
	 */
	@Override
	public Boolean capture(String item) {
		//create card from item
		Card card = new Card(item);
		//if invalid card
		if (! card.validateCard()) return false;
		//this call will not cause the side effect as described by Note #2
		if (! addCard(card)) return false;
		return true;
	}
	/* (non-Javadoc)
	 * @see modus.Modus#addCard(app.model.Card)
	 */
	@Override
	public Boolean addCard(Card card) {
		if (this.deck.push(card) == card) return true;
		return false;
	}
	/* (non-Javadoc)
	 * @see modus.Modus#takeOutCard(java.lang.Object[])
	 */
	@Override
	public Card takeOutCard(Object... objects) {
		if (this.deck == null || this.deck.isEmpty()) return new Card();
		return this.deck.pop();
	}
	
	//****************************** UTILITY ************************************/
	/* (non-Javadoc)
	 * @see modus.Modus#isFull()
	 */
	@Override
	public Boolean isFull() {
		//deck structures can take as many Card(s) as memory allows
		//so the boolean is based on if this.deck has any empty cards
		for (Card card : deck) {
			if (! card.getInUse() ) return false;
		}
		return true;
	}
	/* (non-Javadoc)
	 * @see modus.Modus#isEmpty()
	 */
	@Override
	public Boolean isEmpty() {
		return deck.isEmpty();
	}
	/* (non-Javadoc)
	 * @see modus.Modus#findItemName(java.lang.String)
	 */
	@Override
	public String findItemName(String givenItem) {
		//iterate through the array and scrape the item names into a list
		List<String> itemList = new ArrayList<String>();
		for (Card card : deck) { itemList.add(card.getItem()); }
			
		//perform a fuzzy string search
		return Searcher.fuzzyStringSearch(itemList, givenItem).getValue();
	}
	
	/**
	 * Shuffles the deck fully {@value #SHUFFLE_VAL} times.
	 * Creates two temporary Stack decks, respectively holding the top half and lower half of {@link #deck this.deck},
	 * then randomly draws from either temporary deck to rebuild this.deck. 
	 * <p> Uses {@link Stack#pop()} and {@link Stack#push(Card)} which is a synchronized action. 
	 * <p> If this.deck contains only one card or is empty/null, it will return without any changes made.
	 */
	protected void shuffleDeck() {
		//test if the deck is 1 or less, if so, then return the deck unchanged.
		if (deck == null || deck.size() < 2) return;
		
		//shuffle the deck
		for (int loop = 0; loop < SHUFFLE_VAL; loop++) {
			//split this deck into two sides, then randomly re-stack from the top of either side.
			Stack<Card> leftSide = new Stack<Card>();
		    Stack<Card> rightSide = new Stack<Card>();
		    int a = (deck.size() / 2);	//split the deck in half
		    int b = deck.size() - a;		//give the rest to rightSide
		    for (int i = 0; i < a + b; i++) {
		    		//if we're on the top half of the deck, pop+push to leftSide
		    		if (i < a) leftSide.push(deck.pop());
		    		//if we're on the bottom half of the deck, pop+push to rightSide
		    		else rightSide.push(deck.pop());
		    }
			
		    assert deck.isEmpty();
		    
		    //use rng to determine if it should pull from the left or right hand deck
		    int r; //rng int of either 0 or 1
		    Random rand = new Random();
		    while(! leftSide.isEmpty() || ! rightSide.isEmpty()) //run until both decks are empty
	        {
	            //randomly take from the top of the two decks and push to the new deck.
	            r = rand.nextInt(2); //random int of either 0 or 1
	            if(!leftSide.isEmpty() && (r == 0 || rightSide.isEmpty()))	//pull from LeftSide
	            {
	            		deck.push(leftSide.pop());
	                System.out.print("/");
	            }
	            else //r == 1 && !rightSide.isEmpty(), pull from RightSide
	            {
	                deck.push(rightSide.pop());
	                System.out.print("\\");
	            }
	            System.out.print("\n");
	        }
		}
	}

	/* (non-Javadoc)
	 * @see modus.Modus#drawToDisplay()
	 */
	@Override
	public void drawToDisplay() {
		//TODO: finish this
	}

	/* (non-Javadoc)
	 * @see modus.Modus#description()
	 */
	@Override
	public String description() {
		return " ";
	}
	
	//Arrays.sort(setFolder, Comparator.comparing((Card card) -> card.getItem()));
}
