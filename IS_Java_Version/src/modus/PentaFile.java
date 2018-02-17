package modus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import app.controller.Sylladex;
import app.model.Card;
import app.model.CardNode;
import app.model.Metadata;
import javafx.scene.layout.StackPane;

/**
 * This fetch modus is the PentaFile pfModus, a modus designed for use with a sylladex.
 *   Using a structure comprised of 5 arrays containing 5 cards, and busting if a
 *   single array is overfilled. <br>
 *   This is likened to a File Cabinet. 5 folders that can hold 5 files each.
 * @author Triston Scallan
 * <dt> Note: </dt> <dd>
 *   1. The inventory only holds 25 cards, 5 cards in 5 folders. <br>
 *   2. If 6 items are placed into a folder, 5 are ejected and the 6th is pushed <br>
 *   3. The current PentaFile can have all its information saved and loaded to a text file. <br>
 *   </dd>
 */
public class PentaFile implements Modus {
	/**
	 * A reference to the Sylladex that called the given modus. <br>
	 * This is used to pass information back to the caller.
	 */
	private Sylladex sylladexReference;
	/**
	 * provides information about this modus
	 */
	protected final Metadata METADATA;
	
	//5 arrays, each with 5 elements
	private Card[] weapons = new Card[5];
	private Card[] survival = new Card[5];
	private Card[] misc = new Card[5];
	private Card[] info = new Card[5];
	private Card[] keyCritical = new Card[5];
	
	//***************************** INITIALIZE ***********************************/
	/**
	 * The constructor of a fetch Modus should save the reference to the sylladex
	 * 	so that it can functionally return a list of the Modus' functionality to
	 * 	the ModusManager, specifically passing modusMetadata.
	 * @param sylladex a reference to the caller, a Sylladex
	 */
	public PentaFile(Sylladex sylladex) {
		this.sylladexReference = sylladex;
		
		//initialize the METADATA
		this.METADATA = new Metadata(this.getClass().getSimpleName(), this.createFunctionMap(), this);
		
		//attempt to initialize the modus space
		Card card = new Card();	//empty card
		Arrays.fill(weapons, card);
		Arrays.fill(survival, card);
		Arrays.fill(misc, card);
		Arrays.fill(info, card);
		Arrays.fill(keyCritical, card);
	}
	
	/* (non-Javadoc)
	 * @see modus.Modus#createFunctionMap()
	 */
	public LinkedHashMap<String, Integer> createFunctionMap() {
		LinkedHashMap<String, Integer> functionMap = new LinkedHashMap<String, Integer>();
		functionMap.put("save", 1);
		functionMap.put("load #", 2); //mode = 0, 1, 2, or 3
		functionMap.put("capture", 3);
		functionMap.put("takeOutCard", 4);
		functionMap.put("captureByFolder", 5);
		functionMap.put("takeOutCardByName", 6);
		return functionMap;
	}
	
	//***************************** ACCESS *************************************/
	/* (non-Javadoc)
	 * @see modus.Modus#entry()
	 */
	@Override
	public String entry(int functionCode, Object...objects) {
		switch (functionCode) {
		case 1: //save
			save();
			break;
		case 2: //load <mode>
			//based on mode as objects[0], use that load mode. if doesn't match 0, 1, 2, or 3 then invoke entry(-1, "command name") to display help to the output
			if (objects.length == 1 && objects[0] instanceof String) {
				switch ((String) objects[0]) {
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
				case "3":
					load(3);
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
		case 3: //capture
			if (objects.length == 1 && objects[0] instanceof String) {
				if(! capture((String) objects[0])) return "-1";
				save();
				drawToDisplay();
			} else {
				entry(-1, "help capture");
				return "-1";
			}
			break;
		case 4: //takeOutCard
			//TODO finish the entry function
			break;
		case 5: //captureByFolder
			if (objects.length == 2 && objects[0] instanceof String && objects[1] instanceof String) {
				Card[] folder = findFolderByName((String) objects[1]);
				if(! captureByFolder((String) objects[0], folder)) return "-1";
				save();
				drawToDisplay();
			} else {
				entry(-1, "help captureByFolder");
				return "-1";
			}
			break;
		case 6: //takeOutCardByName
			break;	
		default: //help <commandName> <isReturnString(optional)>
			//TODO: attempt to parse command name and select that help description. `help load` should display info about all modes
			//	if a "1" is present as the second argument after the commandName then return the description as a string instead of
			//	printing to output. if 1 is not present, then simply print to textOutput.
			//if no commandName matches then print "command provided was not understood."
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
		List<Card> deck = Arrays.asList(createOmniFolder());
		sylladexReference.setDeck(deck);
	}

	/* (non-Javadoc)
	 * @see modus.Modus#load(java.util.List)
	 */
	@Override
	public void load(int mode) {
		List<Card> deck = sylladexReference.getDeck();
		// reset the modus space
		Card freshCard = new Card();	//empty card
		Arrays.fill(weapons, freshCard);
		Arrays.fill(survival, freshCard);
		Arrays.fill(misc, freshCard);
		Arrays.fill(info, freshCard);
		Arrays.fill(keyCritical, freshCard);
		///// automatic loading
		if (mode == 1) {
			//load from the deck based as the pattern 
			for (int i = 0; i < 25; i++ ) {
				Card card = deck.get(i);
				if (i < 5 ) weapons[i] = card;
				else if (i < 10) survival[i%5] = card;
				else if (i < 15) misc[i%5] = card;
				else if (i < 20) info[i%5] = card;
				else keyCritical[i%5] = card;
			}
		///// manual loading
		} else if (mode == 2) {
			for (Card card : deck) {
				if (card.validateCard() ? card.getInUse() : false) {
					//TODO: present the card's item through the GUI
						
					//ask which folder to place the card in (or none at all)
					String givenFolder = null; //TODO: prompt the user for input
					Card[] folder = findFolderByName(givenFolder);
					//place it in the folder
					if (! captureByFolder(card.getItem(), folder)) throw new IllegalStateException();
				}
			}
		///// fast loading
		} else if (mode == 3) {
			//Continually fill up the the modus space with cards until 
			for (Card card : deck) {
				if (! ( (card.validateCard()) ? addCard(card) : addCard(new Card()) ) ) break;
			}
		}
		
	}
	
	//********************************** IO ***************************************/
	/* (non-Javadoc)
	 * @see modus.Modus#capture(java.lang.String)
	 */
	@Override
	public Boolean capture(String item) {
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
		int index;
		if ((index = findFolderSpace(weapons)) != -1) {
			weapons[index] = card;
			return true;
		} else if ((index = findFolderSpace(survival)) != -1) {
			survival[index] = card;
			return true;
		} else if ((index = findFolderSpace(misc)) != -1) {
			misc[index] = card;
			return true;
		} else if ((index = findFolderSpace(info)) != -1) {
			info[index] = card;
			return true;
		} else if ((index = findFolderSpace(survival)) != -1) {
			keyCritical[index] = card;
			return true;
		} else 
			return false;
	}

	/**
	 * adds the card to the modus through a specific folder
	 * @param item the item to be added
	 * @param folder the Card array to be inserted into
	 * @return {@code true} if successful, {@code false} otherwise
	 */
	public Boolean captureByFolder(String item, Card[] folder) {
		int index = 0;
		Card card = new Card(item);
		//if invalid card
		if (! card.validateCard()) return false;
		
		if ((index = findFolderSpace(folder)) == -1) {
			List<Card> tempDeck = explodeFolder(folder);
			folder[0] = card;
			//hand off tempDeck to the Sylladex
			sylladexReference.addToOpenHand(tempDeck);
			return true;
		} else if ((index = findFolderSpace(folder)) != -1 && index < 5) {
			folder[index] = card;
			return true;
		} else {
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see modus.Modus#takeOutCard()
	 */
	@Override
	public Card takeOutCard(Object...objects) {
		//objects should be {Integer index, Card[] folder}
		assert(objects[0].getClass().equals(Integer.class)) : "assert that objects[0] is Integer in PentaFile failed.";
		assert(objects[1].getClass().equals(Card[].class)) : "assert that objects[1] is Card[] in PentaFile failed.";
		
		int index = (int) objects[0];
		Card[] folder = (Card[]) objects[1];
		return folder[index];
	}
	
	/**
	 * Uses the name of an item as a key to search for it's card.
	 * Because it calls {@link #findItemName(String)}, it may have
	 * 	undesired affects if the name given is misspelled. The
	 * 	function will attempt to get the closest match, but an 
	 * 	exact match is not guarenteed.
	 * <p> If the modus space is empty, this function will short circuit
	 * 	and return an "empty" Card.
	 * @param itemName the item key
	 * @return a card matching the key
	 */
	public Card takeOutCardByName(String itemName) {
		if (isEmpty()) return new Card();
		String match = findItemName(itemName);
		Card[] omniFolder = createOmniFolder();
		for(Card card : omniFolder) {
			if (card.getItem().equals(match)) return card;
		}
		return new Card();
	}

	//****************************** UTILITY ************************************/	
	/* (non-Javadoc)
	 * @see modus.Modus#isFull()
	 */
	@Override
	public Boolean isFull() {
		for (Card card : weapons) {
			if (! card.getInUse()) 
				return false;
		} for (Card card : survival) {
			if (! card.getInUse()) 
				return false;
		} for (Card card : misc) {
			if (! card.getInUse()) 
				return false;
		} for (Card card : info) {
			if (! card.getInUse()) 
				return false;
		} for (Card card : keyCritical) {
			if (! card.getInUse()) 
				return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see modus.Modus#isEmpty()
	 */
	@Override
	public Boolean isEmpty() {
		for (Card card : weapons) {
			if (card.getInUse()) 
				return false;
		} for (Card card : survival) {
			if (card.getInUse()) 
				return false;
		} for (Card card : misc) {
			if (card.getInUse()) 
				return false;
		} for (Card card : info) {
			if (card.getInUse()) 
				return false;
		} for (Card card : keyCritical) {
			if (card.getInUse()) 
				return false;
		}
		return true;
	}

	/**
	 * Searches a Card array for the first empty Card and then
	 * 	returns the index of that empty Card. If the entire array
	 * 	is full, then return {@code -1}.
	 * @param folder the Card array to search
	 * @return the index, if no spot available then {@code -1}
	 */
	private int findFolderSpace(Card[] folder) {
		for (int i = 0; i < 5; i++) {
			if (! folder[i].getInUse())
				return i;
		}
		return -1;
	}
	
	/**
	 * "Pops" all the cards in a folder and places them into a temporary deck.
	 * <br><br>
	 * The temporary deck should be handed off to the sylladex's "open hand"
	 * 	thread to be unraveled by the calling method.
	 * @param folder the Card array to "explode"
	 * @return an ArrayList of Card to be given to the Sylladex
	 */
	private List<Card> explodeFolder(Card[] folder) {
		List<Card> tempDeck = new ArrayList<Card>();
		for (Card card : folder) {
			tempDeck.add(card);
			card = new Card();
		}
		return tempDeck;
	}
	
	/**
	 * retrieves the folder Card array based on a string name given to search with.
	 * <p>identical to calling {@link app.controller.Sylladex#fuzzyStringSearch(List, String) fuzzyStringSearch} using a list of the folder names and the
	 * input, respectively.
	 * @param givenFolder the name of the folder to obtain
	 * @return the Card array "folder" based on given folder name
	 */
	private Card[] findFolderByName(String givenFolder) {
		List<String> folderList = new ArrayList<String>();
		folderList.add("weapons");
		folderList.add("survival");
		folderList.add("misc");
		folderList.add("info");
		folderList.add("keyCritical");
		
		Object[] result = Sylladex.fuzzyStringSearch(folderList, givenFolder);
		int i = (int) result[0];
		if (i == -1) {
			return weapons;
		}
		Card[] folder = (
				(i == 0) ? weapons : 
				((i == 1) ? survival : 
				((i == 2) ? misc : 
				((i == 3) ? info : keyCritical))));
		return folder;
	}
	
	/* (non-Javadoc)
	 * @see modus.Modus#translateItemName(java.lang.String)
	 */
	@Override
	public String findItemName(String givenItem) {
		Card[] omniFolder = createOmniFolder();
		//iterate through the array and scrape the item names into a list
		List<String> itemList = new ArrayList<String>();
		for (int i = 0; i < 25; i++) { itemList.add(omniFolder[i].getItem()); }
			
		//perform a fuzzy string search
		Object[] results = Sylladex.fuzzyStringSearch(itemList, givenItem);
		assert(results[1].getClass().equals(String.class));
		return (String) results[1];
	}
	
	/**
	 * merges the 5 folders into a single array.
	 * @return a {@code Card[25]} array
	 */
	public final Card[] createOmniFolder() {
		Card[] omniFolder = new Card[25];
		System.arraycopy(weapons, 0, omniFolder, 0, 5);
		System.arraycopy(survival, 0, omniFolder, 5, 10);
		System.arraycopy(misc, 0, omniFolder, 10, 15);
		System.arraycopy(info, 0, omniFolder, 15, 20);
		System.arraycopy(keyCritical, 0, omniFolder, 20, 25);
		return omniFolder;
	}

	/* (non-Javadoc)
	 * @see modus.Modus#drawToDisplay()
	 */
	@Override
	public void drawToDisplay() {
		//variable data constants
		StackPane display = sylladexReference.getDisplay();
		double dWidth = display.getWidth();
		double dHeight = display.getHeight();
		CardNode cardExample = Sylladex.createCardNode(new Card());
		double X_OFFSET = cardExample.cardFace.getWidth() + dWidth/6; //card width + padding
		double X_MARGIN = 5;
		double Y_OFFSET = cardExample.cardFace.getHeight() + dHeight/20; //card height + padding
		double Y_MARGIN = 5;
		Card[] omnifolder = createOmniFolder();
		
		//loop of 5 folders within the modus
		for (int i = 0; i < 5; i++) { 
			//loop of 5 cards within a folder
			for (int j = 0; j < 5; j++) { 
				//set the coordinates this loop's card should be placed at
				double xCardCoord = i * X_OFFSET + (X_MARGIN + i*5);
				double yCardCoord = j * Y_OFFSET + Y_MARGIN;
				//create the card node to draw
				CardNode node = Sylladex.createCardNode(omnifolder[i*5 + j]); //i = folder, j = card
				node.cardFace.setLayoutX(xCardCoord);
				node.cardFace.setLayoutY(yCardCoord);
				//draw the node to display
				display.getChildren().add(node.cardFace);
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PentaFile [weapons=").append(Arrays.toString(weapons)).append(", survival=")
				.append(Arrays.toString(survival)).append(", misc=").append(Arrays.toString(misc)).append(", info=")
				.append(Arrays.toString(info)).append(", keyCritical=").append(Arrays.toString(keyCritical))
				.append("]");
		return builder.toString();
	}
}
