package modus;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import app.controller.Sylladex;
import app.model.Card;
import app.model.CardNode;
import app.model.Metadata;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;

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
	public String entry(int functionCode, String...args) {
		TextArea textOutput = sylladexReference.getTextOutput();
		switch (functionCode) {
		case 1: //save
			save();
			textOutput.appendText("Deck was saved to sylladex.\n");
			break;
		case 2: //load <mode>
			//based on mode as objects[0], use that load mode. if doesn't match 0, 1, 2, or 3 then invoke entry(-1, "command name") to display help to the output
			if (args.length == 1) {
				int mode;
				textOutput.appendText("Loading from sylladex deck in mode "+ args[0] +"...");
				try {
					mode = Integer.valueOf(args[0]);
					if (0 <= mode && mode <= 3) {
						load(mode);
						drawToDisplay();
						textOutput.appendText("success.\n");
						return "0";
					}
				} catch (NumberFormatException e) {
					//do nothing...
				}
			} 
			entry(0, "load");
			return "-1";
		case 3: //capture
			if (args.length == 1) {
				textOutput.appendText("Attempting to capture " + args[0] + "...");
				if(! capture(args[0])) return "-1";
				textOutput.appendText("success.\n");
				save();
				drawToDisplay();
				return "0";
			} 
			entry(0, "capture");
			return "-1";
		case 4: //takeOutCard
			if (args.length == 2) {
				Card[] folder = findFolderByName(args[1]);
				Integer index = 0;
				try {
					index = Integer.valueOf(args[0]);
				} catch (NumberFormatException e) {
					//do nothing...
				}
				if (index >= 1 && index <= 5) {
					textOutput.appendText("Retrieving card at index " + args[0] + " in folder " + args[1] + "...");
					Card card = takeOutCard(index - 1, folder);
					textOutput.appendText("success.\n");
					save();
					drawToDisplay();
					//return a non-empty card to hand, but its not an error if it was empty.
					if (card.getInUse()) sylladexReference.addToOpenHand(Arrays.asList(card));
					return "0";
				}
				textOutput.appendText("ERROR: " + args[0] + " is not a valid index.\n");
			}
			entry(0, "takeOutCard");
			return "-1";
		case 5: //captureByFolder
			if (args.length == 2) {
				Card[] folder = findFolderByName(args[1]);
				textOutput.appendText("Capturing " + args[0] + " and placing into folder " + args[1] + "...");
				if(! captureByFolder(args[0], folder)) return "-1";
				textOutput.appendText("success.\n");
				save();
				drawToDisplay();
				return "0";
			}
			entry(0, "captureByFolder");
			return "-1";
		case 6: //takeOutCardByName
			if (args.length == 1) {
				textOutput.appendText("Retrieving " + args[0] + "...");
				Card card = takeOutCardByName(args[0]);
				if (card.getInUse()) { 
					sylladexReference.addToOpenHand(Arrays.asList(card));
					textOutput.appendText("success.\n");
					save();
					drawToDisplay();
				}
				else textOutput.appendText("card " + args[0] + " either doesn't exist or match failed.\n");
				return "0";
			}
			entry(0, "takeOutCardByName");
			return "-1";
		default: //[help <commandName>] [<isReturnString(optional)]>
			//attempt to parse command name and select that help description. `help load` should display info about all modes.
			//if a "1" is present as the second argument after the commandName then return the description as a string instead of
			//	printing to output. if 1 is not present, then simply print to textOutput.
			//if no commandName matches then print "command provided was not understood."
			
			//test if this was a purposeful matched help case (case == 0)
			if (functionCode == 0) {
				//case 1: help is called with both a command and then a "1"
				//case 2: help is called with a single command, invoked from either the modus or sylladex
				//case 3: help is called with a command and possibly additional info 
				
				int returnStringFlag = 0; //flag to distinguish from pure case 1 and 2
				
				//isolate the first word of the args string, expected to be the command
				String[] splitArgs = args[0].split(" ", 2);
				String commandName = splitArgs[0].toLowerCase();
				System.out.println("Providing modus command help on: " + commandName);
				
				String result;
				//determine the cases
				if (args.length == 2 && args[1].equals("1")) { //case 1
					returnStringFlag = 1;
				} 
				if (splitArgs.length > 1) { //case 3
					textOutput.appendText("help command invoked. disregarding additional args.\n");
				}
				switch (commandName) { //case 2
				case "save":
					result = "syntax: save\n\t saves the current inventory to the sylladex's deck. "
							+ "This command is called at the end of every other command except load.";
					break;
				case "load":
					result = "syntax: load <mode>\n\t loads the inventory from the sylladex, which may differ."
							+ "\n\tmode 0 will simply reset the inventory."
							+ "\n\tmode 1 will auto load the inventory, based on card positions in the deck."
							+ "\n\tmode 2 will manually load the inv. you will choose where items go."
							+ "\n\tmode 3 will fast load the inventory. disregards saved card positions.";
					break;
				case "capture":
					result = "syntax: capture <item>\n\t captchalogues the item. the item can have "
							+ "spaces when you type its name. puts in first available spot.";
					break;
				case "takeoutcard":
					result = "syntax: takeOutCard <index>, <folder>\n\t takes out the card at "
							+ "the index within the folder. index is from 1 to 5.";
					break;
				case "capturebyfolder":
					result = "syntax: captureByFolder <item>, <folder>\n\t captchalogues the item. the item can have "
							+ "spaces when you type its name. puts in the specified folder.";
					break;
				case "takeoutcardbyname":
					result = "syntax: takeOutCard <item>\n\t attempts to take out a card based on the given "
							+ "item name you gave. item can have spaces in its name.";
					break;
				default: 
					result = "syntax: help <command>\n\t provides help information about the "
							+ "given command. syntax is the form you input a complete command. "
							+ "if a command has multiple arguments they need to be seperated by a comma.";
				}
				
				if (returnStringFlag == 1) {
					return result;
				} 
				textOutput.appendText(result + "\n");
				return "0";
			}
			textOutput.appendText("command provided not understood.\n");
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
	
	/**
	 * {@inheritDoc}
	 * <p>parameter is Objects{Integer index, Card[] folder}
	 */
	@Override
	public Card takeOutCard(Object...objects) {
		//
		if (!objects[0].getClass().equals(Integer.class) || !objects[1].getClass().equals(Card[].class))
			return new Card();
		
		int index = (int) objects[0];
		Card[] folder = (Card[]) objects[1];
		Card result = folder[index];
		folder[index] = new Card();
		return result;
	}
	
	/**
	 * Uses the name of an item as a key to search for it's card.
	 * Because it calls {@link #findItemName(String)}, it may have
	 * 	undesired affects if the name given is misspelled. The
	 * 	function will attempt to get the closest match, but an 
	 * 	exact match is not guaranteed.
	 * <p> If the modus space is empty, this function will short circuit
	 * 	and return an "empty" Card.
	 * @param itemName the item key
	 * @return a card matching the key
	 */
	public Card takeOutCardByName(String itemName) {
		if (isEmpty()) return new Card();
		String match = findItemName(itemName);
		Card[] omniFolder = createOmniFolder();
		int i = 0;
		for(Card card : omniFolder) {
			if (card.getItem().equals(match)) { 
				int index = i % 5;
				Card[] folder = findFolderFromOmniIndex(i);
				folder[index] = new Card();
				return card;
			}
			i++;
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
			System.out.println("folder requested wasn't found. returning `weapons` folder as default.");
			return weapons;
		}
		Card[] folder = (
				(i == 0) ? weapons : 
				((i == 1) ? survival : 
				((i == 2) ? misc : 
				((i == 3) ? info : keyCritical))));
		return folder;
	}
	
	/**
	 * returns a folder based off of an index that relates to a position in a omni-folder
	 * @param i the index to use
	 * @return a Card[]
	 * @see {@link #createOmniFolder()}
	 */
	private Card[] findFolderFromOmniIndex(int i) {
		i = i / 5; //collapse the index into the 5 buckets
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
		System.arraycopy(survival, 0, omniFolder, 5, 5);
		System.arraycopy(misc, 0, omniFolder, 10, 5);
		System.arraycopy(info, 0, omniFolder, 15, 5);
		System.arraycopy(keyCritical, 0, omniFolder, 20, 5);
		return omniFolder;
	}

	/* (non-Javadoc)
	 * @see modus.Modus#drawToDisplay()
	 */
	@Override
	public void drawToDisplay() {
		//variable data constants
		StackPane display = sylladexReference.getDisplay();
		double dWidth = display.getMaxWidth();
		double dHeight = display.getMaxHeight();
		CardNode cardExample = Sylladex.createCardNode(new Card());
		double scaleFactor = 0.5;
		double X_OFFSET = cardExample.cardFace.getMaxWidth() + dWidth/4; //card width + padding
		double X_MARGIN = 128;
		double Y_OFFSET = cardExample.cardFace.getMaxHeight() + dHeight/4; //card height + padding
		double Y_MARGIN = 4;
		Card[] omnifolder = createOmniFolder();
		Paint[] folderColors = {
				Paint.valueOf(String.format("#%06x", Color.RED.getRGB() & 0x00FFFFFF)),
				Paint.valueOf(String.format("#%06x", Color.ORANGE.getRGB() & 0x00FFFFFF)),
				Paint.valueOf(String.format("#%06x", Color.GREEN.getRGB() & 0x00FFFFFF)),
				Paint.valueOf(String.format("#%06x", Color.BLUE.getRGB() & 0x00FFFFFF)),
				Paint.valueOf(String.format("#%06x", Color.decode("#A030F0").getRGB() & 0x00FFFFFF))
		};
		
		//clear the display and then start adding nodes
		sylladexReference.clearDisplay();
		
		//get list of folder names
		List<Label> folderNames = new ArrayList<Label>();
		for (Field f : PentaFile.class.getDeclaredFields()) {
			if (f.getType().isAssignableFrom(Card[].class)) {
				Label folderLabel = new Label(f.getName());
				folderLabel.setFont(new Font("Courier", 10));
				folderNames.add(folderLabel);
			} 
		} //assert only 5 folder names were added
		assert (folderNames.size() == 5);
		
		//loop of 5 folders within the modus
		for (int i = 0; i < 5; i++) { 
			//loop of 5 cards within a folder
			for (int j = 0; j < 5; j++) { 
				//set the coordinates this loop's card should be placed at
				double xCardCoord = i * X_OFFSET + i*X_MARGIN + (j*15) + 4; 	//per-folder offset + margin + per-card offset + scalable constant offset
				double yCardCoord = j * Y_OFFSET + j*Y_MARGIN; 				//per-card offset, margin
				//create the card node to draw
				CardNode node = Sylladex.createCardNode(omnifolder[i*5 + j]); //i = folder, j = card
				
				//account for the translation difference caused by scaling from the node's center
				double widthDiff  = cardExample.cardFace.getMaxWidth()/2;
				double heightDiff = cardExample.cardFace.getMaxHeight()/2;
				double apparentWidthDifference = widthDiff*scaleFactor - widthDiff;
				double apparentHeightDifference = heightDiff*scaleFactor - heightDiff;
				double finalCoordX = xCardCoord * scaleFactor + apparentWidthDifference;
				double finalCoordY = yCardCoord * scaleFactor + apparentHeightDifference;
				node.cardFace.setTranslateX(finalCoordX);
				node.cardFace.setTranslateY(finalCoordY + 12); //add a non-scalable constant offset for the folder labels
				node.setCardScaleFactor(scaleFactor);
				//create label for the folder column
				if (j == 0) {
					folderNames.get(i).setTranslateX((xCardCoord - 4) * scaleFactor);
					display.getChildren().add(folderNames.get(i));
				}
				
				//re-color the node based on folder
				((SVGPath) node.cardFace.getChildren().get(1)).setFill(folderColors[i]);
				
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
		builder.append("PentaFile [")
				.append("\n\tweapons=").append(Arrays.toString(weapons))
				.append("\n\tsurvival=").append(Arrays.toString(survival))
				.append("\n\tmisc=").append(Arrays.toString(misc))
				.append("\n\tinfo=").append(Arrays.toString(info))
				.append("\n\tkeyCritical=").append(Arrays.toString(keyCritical))
				.append("\n]");
		return builder.toString();
	}

	/* (non-Javadoc)
	 * @see modus.Modus#description()
	 */
	@Override
	public String description() {
		StringBuilder value = new StringBuilder();
		value.append("The PentaFile Fetch Modus is designed to simulate a Filing Cabinet.\n")
				.append("It comprises 5 folders that each store exactly 5 cards. ")
				.append("You can store items to a specific folder or retrieve from either ")
				.append("just the item name or from a folder and index. \n")
				.append("the notable quirk of this modus is that if a 6th item is placed into a filled ")
				.append("folder, the contents of the folder will be ejected to the sylladex and then the ")
				.append("6th item will be placed into the now empty folder.\n");
		return value.toString();
	}
}
