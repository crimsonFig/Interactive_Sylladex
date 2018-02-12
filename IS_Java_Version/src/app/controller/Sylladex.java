package app.controller;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import modus.Metadata;
import app.model.Card;
import app.model.CardNode;

//NOTE: This class acts as the main controller for the MVC format.
/**
 * The Sylladex acts as the framework for hosting a fetch Modus, a collection 
 * 	of Card, and managing interactions with the user in respect to a
 * 	selected Modus. <br>
 * <br>
 * The Sylladex should hold an ArrayList of Card as a "deck". This helps
 * 	support the switching of modi, among other functionality.
 * <br><br>
 * This Sylladex is the main focal point for the application, this being
 *   a personal project that is an interactive inventory management
 *   system for entertainment purposes. The application is meant to be 
 *   controlled through a text console to remain true to the 
 *   "==> Text Adventure" style. 
 *   
 * @author Triston Scallan
 *
 */
public class Sylladex {
	/**
	 * a List of item(s) that represent items that exist but aren't in a Card.
	 * <p>This field utilizes {@link Collections#synchronizedList} and may
	 * 	become synchronized when modified.
	 */
	private List<String> openHand = Collections.synchronizedList(new ArrayList<String>());
	private List<Card> deck = Collections.synchronizedList(new ArrayList<Card>());
	private ModusManager modiMgr;
	private static final LinkedHashSet<String> SYLL_CMD_STRING_LIST = initializeSyllCmdStringList();
	private static final String SAVE_FILE_NAME = "sylladexDeck.sav";
	private static final String OUT_PATH = "../../../";
	/**
	 * The maximum number of {@value #MAX_COST} edits/mutations a string can have for fuzzy searching.
	 */
	private final static int MAX_COST = 3;
	
	///// GUI references ////////////
	//BOTTOM
	@FXML
	private Button bInputButton;
	@FXML
	private TextField textInput;
	
	//RIGHT
	///tab - Commands
	@FXML
	private Accordion cmdAcc;
	@FXML
	private VBox syllCmdList;
	@FXML
	private TitledPane syllCmdPane;
	@FXML
	private VBox moduCmdList;
	@FXML
	private VBox miscList;
	///tab - Modus List
	@FXML
	private VBox modusList;
	@FXML
	private Button bModusScan;
	@FXML
	private Button bRefresh;
	///tab - Help
	@FXML
	private Button bReset;
	
	//TOP
	@FXML
	private MenuItem mAbout;
	
	//CENTER
	@FXML
	private StackPane display;
	@FXML
	private TextArea textOutput;
	//////////////////////////////////////////////////////
	
	//TODO: create a function to reset the current deck.
	
	@FXML
	private void initialize() {
		//// initialize some pane parameters
		cmdAcc.setExpandedPane(syllCmdPane); //set Sylladex Commands pane as defaulted open
		bRefresh.setDisable(true); //keep disabled until ModusManager can add modi generically (not explicitly)
		bModusScan.setDisable(true);
		//should initialize a ModusManager
		modiMgr = new ModusManager(this);
		//add modus nodes to the Modus List tab from ModiMgr#modusList.
		for (Metadata e : modiMgr.getModusList()) {
			
			//create a gridpane object
			GridPane node = new GridPane();
			//add a column and two rows
			node.getColumnConstraints().add(new ColumnConstraints(10, 100, Region.USE_COMPUTED_SIZE, Priority.SOMETIMES, null, false));
			node.getRowConstraints().add(new RowConstraints(10, 30, Region.USE_COMPUTED_SIZE, Priority.SOMETIMES, null, false));
			node.getRowConstraints().add(new RowConstraints(10, 30, Region.USE_COMPUTED_SIZE, Priority.SOMETIMES, null, false));
			node.setPrefWidth(257.0);
			node.setPrefHeight(40.0);
			node.setPadding(new Insets(0,5,5,5));
			//create a label in the GridPane
			Label name = new Label(e.REFERENCE.getClass().getSimpleName());
			name.setFont(new Font("Courier", 13));
			node.add(name, 0, 0);
			//create a button in the GridePane
			Button button = new Button("Select");
			button.setFont(new Font("Courier", 11));
			button.setOnAction((event) -> handleModusSelection(e.NAME)); //set event handler
			GridPane.setHalignment(button, HPos.RIGHT);
			node.add(button, 0, 1);
			
			//add the GridPane to the modusList
			modusList.getChildren().add(node);
			
			//create and add a Separator between this and next node.
			Separator hLine = new Separator();
			hLine.setPrefWidth(200);
			modusList.getChildren().add(hLine);
		}
		//should attempt to prompt the user to equip a modus
		VBox noModusBox = new VBox(150);
		noModusBox.setAlignment(Pos.CENTER);
		Label noModusPrompt_1 = new Label("No modus selected.");
		Label noModusPrompt_2 = new Label("Please see the \"Modus List\" tab.");
		noModusPrompt_1.setFont(new Font("Courier", 18));
		noModusPrompt_2.setFont(new Font("Courier", 18));
		noModusBox.getChildren().addAll(noModusPrompt_1, noModusPrompt_2);
		display.getChildren().add(noModusBox);
		
		//TODO: initialize this command list and then create parseCommands, and then create syllCommandSwitch that is invoked by submit
		
		//initialize the syllCmdList
		for (String command : SYLL_CMD_STRING_LIST) {
			Label label = new Label(command);
			Separator line = new Separator();
			syllCmdList.getChildren().addAll(label, line);
		}
	}
	
	private static LinkedHashSet<String> initializeSyllCmdStringList() {
		LinkedHashSet<String> tSet = new LinkedHashSet<String>();
		tSet.add("syll save deck");
		tSet.add("syll load deck");
		tSet.add("syll delete saved deck");
		tSet.add("syll refresh modus");
		tSet.add("syll show loose items");
		tSet.add("syll help <command name>");
		return tSet;
	}
	

///// GETTERS AND SETTERS /////
	/**
	 * @return the modiMgr
	 */
	public ModusManager getModiMgr() {
		return modiMgr;
	}

	/**
	 * @param modiMgr the modiMgr to set
	 */
	public void setModiMgr(ModusManager modiMgr) {
		this.modiMgr = modiMgr;
	}
	
	/**
	 * @return the deck
	 */
	public List<Card> getDeck() {
		return deck;
	}

	/**
	 * @param deck the deck to set
	 */
	public void setDeck(List<Card> deck) {
		synchronized (this.deck) {
			this.deck = Collections.synchronizedList(deck);
		}
	}
	
	/**
	 * @return the display
	 */
	public StackPane getDisplay() {
		return display;
	}
	
	/**
	 * adds a collection of Card(s) to {@link #openHand}
	 * <p>Note: This block will synchronize when in use
	 * @param tempDeck a List of Card(s)
	 */
	public void addToOpenHand(List<Card> tempDeck) {
		synchronized (openHand) {
			for (Card card : tempDeck) {
				openHand.add(card.getItem());
			}
		}
	}
	
///// UTILITY /////
	
	/**
	 * Takes a word and searches for the closest match in the list.
	 * <p> Uses the Levenshtein Distance algorithm to compute the edit
	 * 	distance where a deletion or addition of a char counts as 1 and
	 * 	a char mutation counts as 1.
	 * <p> Allows only a maximum of {@value #MAX_COST} edits, otherwise it fails.
	 * @param wordList The list to search
	 * @param givenWord The word to be matched
	 * @return an Object array of format {@code [int index, String match]}. If failed,
	 * 	returns {@code [-1,""]}.
	 */
	public static final Object[] fuzzyStringSearch(List<String> wordList, String givenWord) {
		String guessedWord = "";
		int score = 999;	//the lower the score, the better
		int index;		//index of the word in the word list.
		for(index = 0; index < wordList.size(); index++) {
			String testWord = wordList.get(index);
			//if a match is found, return it.
			if (givenWord.equals(testWord)) return new Object[]{index, testWord};
			
			//create copies of the item names to prevent confusion if swapped later
			String left = givenWord;
			String right = testWord;
			int lenGiven = left.length(); 	// length of first string
	        int lenCard = right.length(); 	// length of second string
	
	        //if the given is empty and card is lower than score, set guessed to card.
	        if (lenGiven == 0 && lenCard < score) { 
	        		score = lenCard;
	        		guessedWord = testWord;
	            continue;
	        //if the card is empty and the given is lower than the score, set guessed to card 
	        } else if (lenCard == 0) {
	        		score = lenGiven;
	        		guessedWord = testWord;
	            continue;
	        }
	        //if the given item is longer than the card item
	        if (lenGiven > lenCard) {
	            //swap the strings to use less memory
	            final String tmp = left;
	            left = right;
	            right = tmp;
	            lenGiven = lenCard;	
	            lenCard = right.length();
	        }
	        
	        final int[] d = new int[lenGiven + 1];	//cost array, "(d)istance"
	        int i; // iterates through left string
	        int j; // iterates through right string
	        int upperLeft;
	        int upper;	
	        char rightJ; // jth character of right
	        int cost; 
	        
	        //initialize the array
	        for (i = 0; i <= lenGiven; i++) {
	            d[i] = i;
	        } //[0,1,2,3,...,i-1]
	
	        for (j = 1; j <= lenCard; j++) {
	            upperLeft = d[0]; 
	            rightJ = right.charAt(j - 1);
	            d[0] = j; 
	
	            for (i = 1; i <= lenGiven; i++) {
	                upper = d[i];
	                cost = left.charAt(i - 1) == rightJ ? 0 : 1;
	                // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
	                d[i] = Math.min(Math.min(d[i - 1] + 1, d[i] + 1), upperLeft + cost);
	                upperLeft = upper;
	            }
	        }
	        cost = d[lenGiven];
	        if (cost < score) { //if cost is lower than the current score, update the best guessed name
	        		score = cost;
	        		guessedWord = testWord;
	        }
	    }
		//if the score is within the threshold, return info, otherwise return a fail state.
		return (score > MAX_COST) ? new Object[] {index, guessedWord} : new Object[] {-1, new String()};
	}
	
	/**
	 * Creates a CardNode. Using this method is preferred over creating an instance
	 * so that the sylladex is able to regulate and observe production.
	 * @param card A card
	 * @return a graphical CardNode derived from the card
	 */
	public static CardNode createCardNode(Card card) {
		CardNode node = new CardNode(card);
		return node;
	}
	
	/**
	 * Writes the {@link #deck} out to a binary file.
	 * @param fileName the name of the file to be created
	 * @param outPath the relative pathway to a folder for the file to be created in
	 */
	private void writeDeckToFile(String fileName, String outPath) throws SecurityException {
		String fullOutPath = outPath + fileName;
		//TODO: consider locking the file when saving
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(fullOutPath)))) {
			synchronized (this.deck) {
				//write deck size to the front of the file, 
				oos.writeInt(Integer.valueOf(this.deck.size()));
				for (Card card : deck) {
					oos.writeObject(card);
				}
			}
			//finally write a null to mark the end of the file.
			oos.writeByte((byte) '\0');
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			throw e;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * Loads a binary file to extract a List of Card from. Replaces
	 * 	the current instance of {@link #deck} with the one found in
	 * 	the file.
	 * @param fileName the file to be loaded
	 * @param outPath relative or absolute path where file is
	 */
	private void loadDeckFromFile(String fileName, String outPath) throws SecurityException {
		String fullInPath = outPath + fileName;
		List<Card> tempDeck = new ArrayList<Card>();
		//TODO: consider locking the file somehow when loading
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(fullInPath)))) {
			Integer numOfCards = ois.readInt();
			for (int i = 0; i < numOfCards; i++ ) {
				Object o = ois.readObject();
				if (o instanceof Card && o != null) tempDeck.add((Card) o);
			}
			setDeck(tempDeck);
		} catch (EOFException e) {
			this.deck = tempDeck;
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			throw e;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Takes a raw string that is expected to be a command, 
	 * 	and matches it against the current modus list of 
	 * 	commands and the sylladex's list of commands. 
	 * <p> functionality would be the same as invoking 
	 * {@link #fuzzyStringSearch(List, String)} with a List of
	 * the commands and the inputString, respectively.
	 * @param inputString The given command to parse
	 * @return matching string result of a command
	 */
	public String parseCommands(String inputString) {
		List<String> tCommandList = Arrays.asList((String[]) modiMgr.getModusList().get(modiMgr.getCurrentModus()).FUNCTION_MAP.keySet().toArray());
		for (String e : SYLL_CMD_STRING_LIST) {
			tCommandList.add(e);
		}
		Object[] container = fuzzyStringSearch(tCommandList, inputString);
		String result = (String) container[1];
		return result;
	}
	
	/**
	 * Clears the display node
	 */
	public void clearDisplay() {
		display.getChildren().clear();
	}
	
///// LISTENERS /////
	@FXML
	void displayClick(ActionEvent event) {
		
	}
	
	/**If the TextField textInput changes textually, check if it's empty or if their is submittable text
	 * if there is, then activate the submit button, if it's empty then deactivate the submit button*/
	@FXML
	void checkSendable(ActionEvent event) {
		//TODO: test why this method doesn't seem to respond
		if (textInput.getCharacters().equals("")) textInput.setDisable(true);
		else if (textInput.isDisabled()) textInput.setDisable(false);
	}
	
	@FXML
	void submit(ActionEvent event) {
		//TODO: check if textInput is empty, if not, then sanitize the input and try to parse the command against a command list.
			//commands should be given as a single line and shouldn't query any special info through through submission box
			//	each modus should include a `help <command name>` path set as the `default` case within the `entry()` switch-case
	}
	
	/**
	 * Sets the current modus to the modus selected, updates the view 
	 * to represent the modus changes, and handles logic related to 
	 * selecting and switching modi from the {@code #modusList} node.
	 * @param modusName
	 */
	void handleModusSelection(String modusName) {
		int i = -1;
		Boolean bFound = false;
		Boolean isAnyModusActive = (modiMgr.getCurrentModus() != -1) ? true : false;
		for (Metadata e : modiMgr.getModusList()) {
			i++;
			if (e.NAME.equals(modusName)) {
				bFound = true;
				break;
			}
		}
		if (!bFound) {
			Alert alert = new Alert(AlertType.ERROR);
		        	alert.setTitle("Metadata error!");
		        alert.setHeaderText("The selected Modus was not found!");
		        alert.setContentText("Error in handleModusSelection, button's assigned name "
		        		+ "does not match any names in the ModusManager's ModusList.");
	        alert.showAndWait();
			return; 
		}
		
		//if there was a previous modus selected, prompt if they want to save or reset their deck
		int deckAction = -1; //0=save deck, 1=new deck
		if (isAnyModusActive) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
		        	alert.setTitle("Changing Modus Confirmation");
		        alert.setHeaderText("Are you Sure?");
		        alert.setContentText("There is a modus currently active, would you like to "
		        		+ "save your deck to file, reset to a new deck, or cancel?");
	        //create buttons for alert
	        ButtonType buttonSave = new ButtonType("Save");
	        ButtonType buttonNew = new ButtonType("New");
	        ButtonType buttonCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
	        alert.getButtonTypes().setAll(buttonSave, buttonNew, buttonCancel);
	        Optional<ButtonType> result = alert.showAndWait();
	        //if cancel, return, otherwise change deckAction and continue
	        if (result.get() == buttonSave) {
	        		deckAction = 0;
	        } else if (result.get() == buttonNew) {
	        		deckAction = 1;
	        } else {
	        		return;
	        }
		}
		//clear the display
		clearDisplay();
		//if the new or ok button was pushed, finish with those specific deck actions
		modiMgr.setCurrentModus(i);
		textOutput.appendText("Modus selected: " + modusName + "\n");
		//textOutput.appendText(e.FUNCTION_MAP.toString() + "\n");
		if (deckAction == 0) {
			textOutput.appendText("Saving deck to file... ");
			try {
				writeDeckToFile(SAVE_FILE_NAME, OUT_PATH);
				textOutput.appendText("save sucessful.\n");
				this.deck.clear();
				textOutput.appendText("Deck has been refresh.\n\n");
			} catch (SecurityException e) {
				Alert alert = new Alert(AlertType.WARNING);
			        	alert.setTitle("Insufficient Permission");
			        alert.setHeaderText("Unable to create save file.");
			        alert.setContentText("Sylladex was unable to create save file "
			        		+ "because the save directory had insufficient permission. "
			        		+ "Please change directory permissions to allow file"
			        		+ "creation and then try again. \n Cancelling modus change.");
		        alert.showAndWait();
		        return;
			}
		} else if (deckAction == 1) {
			this.deck.clear();
			textOutput.appendText("Deck has been refreshed without saving.\n\n");
		} //else, do nothing.
		
		//clear the moduCmdList and set the moduCmdList to the selected modus FUNCTION_MAP
		moduCmdList.getChildren().clear();
		for(String function : modiMgr.getModusList().get(modiMgr.getCurrentModus()).FUNCTION_MAP.keySet()) {
			//for each function, create a node to be inserted into the moduCmdList
			Label functionName = new Label(function);
			//TODO: add a description label to each command by pinging the `help <commandName> <1>` path, and use
			//	the returned string for the label.
			Separator line = new Separator();
			moduCmdList.getChildren().addAll(functionName, line);
		}
		Label functionName = new Label("help <command name>");
		moduCmdList.getChildren().add(functionName);
		
		//TODO: check if there is a save file. if so, ask if the user would like to load from it.
			//optionally, i could also just prompt the user indirectly by dropping a hint to load in the text area.
		
	}

	/**Scan the modus package for modi, return a File list of the available modi. 
	 * Should update {@link ModusManager#setModusList(List)} and {@link #modusList} modusList */
	@FXML
	void scan(ActionEvent event) {
		
	}
	
	@FXML
	void reset(ActionEvent event) {
		//TODO: set currentmodus to -1 and reset reinitialize the sylladex
	}
}
