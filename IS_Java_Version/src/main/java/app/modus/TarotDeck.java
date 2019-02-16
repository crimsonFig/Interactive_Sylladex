package app.modus;

import java.util.*;

import app.model.Card;
import app.model.Metadata;
import app.model.ModusBuffer;
import app.util.ModusCommandMap;

/**
 * This fetch app.modus is based on the idea of a deck of cards. The intended usage is to insert
 * 	item filled Cards into the {@link #deck} and then to retrieve an item, you shuffle the
 * 	deck and pull a CARD from the top of the deck. The success for retrieving the <i>desired</i>
 * 	item is fully luck based. Ofcourse, one could cheat and look at each CARD at a time to 
 * 	find their desired item and pull from that index; if cheating <i>is</i> desired then see the
 * 	extension of this app.modus: {@link TrueSightDeck}.
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
	 * provides information about this app.modus.
	 * private so that inherited classes don't clash with this
	 */
	protected final Metadata METADATA; 
	/**
	 * Describes how many times {@link #shuffleDeck()} will fully shuffle the cards when invoked.
	 */
	protected int SHUFFLE_VAL = 9;
	
	/** A Stack based data structure */
	protected Stack<Card> deck = new Stack<>(); //use either Stack or Deque
	
	//***************************** INITIALIZE ***********************************/
	/**
	 * Constructor for TarotDeck class
	 */
	public TarotDeck() {
		//initialize the METADATA
		this.METADATA = new Metadata(this.getClass().getSimpleName(), this.createFunctionMap(), this);
	}

	protected ModusCommandMap createFunctionMap() {
		return null;
	}
	
	//***************************** ACCESS *************************************/
	/**
	 * @return the METADATA
	 */
	public Metadata getMETADATA() {
		return METADATA;
	}
	
	//**************************** SAVE & LOAD ********************************/
	@Override
	public List<Card> save(ModusBuffer modusBuffer) {
        return null;
	}

	@Override
	public void load(ModusBuffer modusBuffer) {

	}
	
	//******************************** IO ***************************************/

	
	//****************************** UTILITY ************************************/
	
	/**
	 * Shuffles the deck fully {@link #SHUFFLE_VAL} times.
	 * Creates two temporary Stack decks, respectively holding the top half and lower half of {@link #deck this.deck},
	 * then randomly draws from either temporary deck to rebuild this.deck much like one does in real world shuffling
	 * <p> Uses {@link Stack#pop()} and {@link Stack#push} which is a synchronized action.
	 * <p> If this.deck contains only one CARD or is empty/null, it will return without any changes made.
	 */
	protected void shuffleDeck() {
		//test if the deck is 1 or less, if so, then return the deck unchanged.
		if (deck == null || deck.size() < 2) return;
		
		//shuffle the deck
		for (int loop = 0; loop < SHUFFLE_VAL; loop++) {
			//split this deck into two sides, then randomly re-stack from the top of either side.
			Stack<Card> leftSide = new Stack<>();
		    Stack<Card> rightSide = new Stack<>();
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

    @Override
    public void drawToDisplay(ModusBuffer modusBuffer) {

    }

    @Override
    public String description() {
        return null;
    }

    @Override
    public List<Card> toDeck() {
	    return null;
    }
}
