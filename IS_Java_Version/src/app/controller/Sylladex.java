package app.controller;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
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
import app.model.Card;
import app.model.CardNode;
import app.model.Metadata;

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
	static final Object LOCK = new Object();
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
	
	
	@FXML
	private void initialize() {
		//// initialize some pane parameters
		//cmdAcc.setExpandedPane(syllCmdPane); //set Sylladex Commands pane as defaulted open
		bRefresh.setDisable(true); //keep disabled until ModusManager can add modi generically (not explicitly)
		bModusScan.setDisable(true);
		bInputButton.setDisable(true);
		textInput.setOnKeyTyped((ev) -> {
			if (textInput.getText().isEmpty() || modiMgr.getCurrentModus() == -1) {
				bInputButton.setDisable(true);
			} else {
				bInputButton.setDisable(false);
			}
		});
		
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
		
		//initialize the syllCmdList
		Iterator<String> list = SYLL_CMD_STRING_LIST.iterator();
		while (list.hasNext()) {
			String command = list.next();
			if (list.hasNext()) {
				Label label = new Label(command);
				Separator line = new Separator();
				syllCmdList.getChildren().addAll(label, line);
			} else {
				Label label = new Label(command);
				syllCmdList.getChildren().add(label);
			}
		}
		
	}
	
	private static LinkedHashSet<String> initializeSyllCmdStringList() {
		LinkedHashSet<String> tSet = new LinkedHashSet<String>();
		tSet.add("syll.saveDeck");
		tSet.add("syll.loadDeck");
		tSet.add("syll.deleteDeck");
		tSet.add("syll.deleteSaveFile");
		tSet.add("syll.refreshModus");
		tSet.add("syll.showLooseItems");
		tSet.add("syll.help <command name>");
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
	 * @return the textOutput
	 */
	public TextArea getTextOutput() {
		return textOutput;
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
		int guessedIndex = 0; //index of the guessedWord
		for(index = 0; index < wordList.size(); index++) {
			String testWord = wordList.get(index);
			//if a match is found, return it.
			if (givenWord.equals(testWord)) return new Object[]{index, testWord};
			
			//create copies of the item names to prevent confusion if swapped later
			String left = givenWord;
			String right = testWord;
			int lenGiven = left.length(); 	// length of first string
	        int lenTest = right.length(); 	// length of second string
	
	        //if the given is empty and card is lower than score, set guessed to test.
	        if (lenGiven == 0 && lenTest < score) { 
	        		//score = lenTest;
	        		//guessedWord = testWord;
	            continue;
	        //if the test is empty and the given is lower than the score, set guessed to test 
	        } else if (lenTest == 0) {
	        		//score = lenGiven;
	        		//guessedWord = testWord;
	            continue;
	        }
	        //if the given item is longer than the card item
	        if (lenGiven > lenTest) {
	            //swap the strings to use less memory
	            final String tmp = left;
	            left = right;
	            right = tmp;
	            lenGiven = lenTest;	
	            lenTest = right.length();
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
	
	        for (j = 1; j <= lenTest; j++) {
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
	        		guessedIndex = index;
	        }
	    }
		//if the score is within the threshold, return info, otherwise return a fail state.
		return (score < MAX_COST) ? new Object[] {guessedIndex, guessedWord} : new Object[] {-1, new String()};
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
		List<Object> keyList = Arrays.asList(modiMgr.getModusList().get(modiMgr.getCurrentModus()).FUNCTION_MAP.keySet().toArray());
		List<String> tCommandList = new ArrayList<String>();
		for (Object e : keyList) {
			tCommandList.add((String) e);
		}
		for (String e : SYLL_CMD_STRING_LIST) {
			tCommandList.add(e);
		}
		Object[] container = fuzzyStringSearch(tCommandList, inputString);
		String result = (String) container[1];
		return result;
	}
	
	private void commandSwitch(String command, String...args) {
		//if true then process as sylladex command, otherwise process as modus command
		if (command.contains("syll.")) {
			String syllCommand = command.substring(5); //chop off the "syll." prefix
			switch (syllCommand) {
			case "saveDeck":
				textOutput.appendText("Saving deck to file... ");
				try {
					writeDeckToFile(SAVE_FILE_NAME, OUT_PATH);
					textOutput.appendText("save sucessful.\n");
				} catch (Exception e) {
					textOutput.appendText("save failed.\n");
					return;
				}
				break;
			case "loadDeck":
				textOutput.appendText("Loading deck from file... ");
				try {
					loadDeckFromFile(SAVE_FILE_NAME, OUT_PATH);
					textOutput.appendText("load sucessful.\n");
				} catch (Exception e) {
					textOutput.appendText("load failed.\n");
					return;
				}
				break;
			case "deleteDeck":
				textOutput.appendText("Deleting deck...");
				synchronized (deck) {
					deck.clear();
				}
				textOutput.appendText("deletion sucessful.\n");
				break;
			case "deleteSaveFile":
				textOutput.appendText("Deleting save file...");
				try {
					Files.deleteIfExists(Paths.get(OUT_PATH + SAVE_FILE_NAME));
					textOutput.appendText("deletion sucessful.\n");
				} catch (IOException e) {
					e.printStackTrace();
					textOutput.appendText("deletion failed.\n");
				}
				break;
			case "refreshModus":
				textOutput.appendText("Refreshing the modus...");
				modiMgr.refreshModus(this);
				modiMgr.getModusList().get(modiMgr.getCurrentModus()).REFERENCE.drawToDisplay();
				textOutput.appendText("success. Consider using the modus' load command before continueing.\n ");
				break;
			case "showLooseItems":
				textOutput.appendText("Items in the hand are currently: \n");
				Iterator<String> hand = openHand.iterator();
				while (hand.hasNext()) {
					String item = hand.next();
					if (hand.hasNext())
						textOutput.appendText(item + ", ");
					else
						textOutput.appendText(item + ".\n");
				}
				break;
			default: //"help <command name>"
				//TODO: finish this command
				break;
			}
		} else {
			int functionCode = modiMgr.getModusList().get(modiMgr.getCurrentModus()).FUNCTION_MAP.get(command);
			System.out.println("invoking " + command + ":" + functionCode + " with args = " + Arrays.toString(args));
			String resultCode = modiMgr.getModusList().get(modiMgr.getCurrentModus()).REFERENCE.entry(functionCode, args);
			//if resultCode is -1 and functionCode matches 1, 2, 3, or 4, give a relative diagnostic
			if (resultCode.equals("-1")) {
				//function exited abnormally! 
				switch (functionCode) {
				case 1: //save failed
					textOutput.appendText("modus save failed.\n");
					break;
				case 2: //load failed
					textOutput.appendText("modus load failed.\n");
					break;
				case 3: //capture failed
					textOutput.appendText("attempt to capture " + args[0] + " failed.\n");
					break;
				case 4: //takeOutCard failed
					textOutput.appendText("attempt to extract " + args[0] + " failed.\n");
					break;
				default: //something unknown failed
					textOutput.appendText("something failed.\n");
					break;	
				}
			}
		}
		textOutput.appendText("\n");
	}
	
	/**
	 * Writes the {@link #deck} out to a binary file.
	 * @param fileName the name of the file to be created
	 * @param outPath the relative pathway to a folder for the file to be created in
	 */
	private void writeDeckToFile(String fileName, String outPath) throws Exception {
		String fullOutPath = outPath + fileName;
		synchronized(LOCK) {
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
				throw e;
			} catch (SecurityException e) {
				Alert alert = new Alert(AlertType.WARNING);
			        	alert.setTitle("Insufficient Permission");
			        alert.setHeaderText("Unable to create save file.");
			        alert.setContentText("Sylladex was unable to create save file "
			        		+ "because the save directory had insufficient permission. \n"
			        		+ "Please change directory permissions to allow file"
			        		+ "creation and then try again. \n");
		        alert.showAndWait();
		        throw e;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				throw e;
			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			} 
		}
	}
	
	/**
	 * Loads a binary file to extract a List of Card from. Replaces
	 * 	the current instance of {@link #deck} with the one found in
	 * 	the file.
	 * @param fileName the file to be loaded
	 * @param outPath relative or absolute path where file is
	 */
	private void loadDeckFromFile(String fileName, String outPath) throws Exception {
		String fullInPath = outPath + fileName;
		List<Card> tempDeck = new ArrayList<Card>();
		final File file = new File(fullInPath);
		synchronized(LOCK) {
			try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
				Integer numOfCards = ois.readInt();
				for (int i = 0; i < numOfCards; i++ ) {
					Object o = ois.readObject();
					if (o instanceof Card && o != null) tempDeck.add((Card) o);
				}
				setDeck(tempDeck);
			} catch (EOFException e) {
				setDeck(tempDeck);
			} catch (NullPointerException e) {
				e.printStackTrace();
				throw e;
			} catch (SecurityException e) {
				Alert alert = new Alert(AlertType.WARNING);
			        	alert.setTitle("Insufficient Permission");
			        alert.setHeaderText("Unable to create save file.");
			        alert.setContentText("Sylladex was unable to read the save file "
			        		+ "because the save file had insufficient permission. \n"
			        		+ "Please change file permissions to allow file "
			        		+ "read and then try again. \n");
		        alert.showAndWait();
		        throw e;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				throw e;
			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw e;
			} catch (ClassCastException e) {
				e.printStackTrace();
				throw e;
			}
		}
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
	 * Clears the display node
	 */
	public void clearDisplay() {
		display.getChildren().clear();
	}
	
///// LISTENERS /////
	@FXML
	void displayClick(ActionEvent event) {
		
	}
	
	//TODO: create a listener for if someone clicks on node in the commands list. if a command-label is clicked
		//then it should set the textInput contents equal to the label
	
	@FXML
	void submit(ActionEvent event) {
		//consume the textInput field
		String inputRawString = textInput.getText();
		textInput.clear();
		if (inputRawString.isEmpty()) return;
		
		//split the raw input into ["command", "arg1 arg2 arg3..."]
		String[] splitRawInput = inputRawString.toLowerCase().split(" ", 2);
		
		//parse the raw input command against the sylladex and modus commands
		String rawInputCommand = splitRawInput[0];
		String parsedCommand = parseCommands(rawInputCommand);
		
		//check if the command was "help" (from raw input), if so then invoke entry with the args
		if (fuzzyStringSearch(Arrays.asList("help"), rawInputCommand)[1].equals("help") && splitRawInput.length > 1) {
			modiMgr.getModusList().get(modiMgr.getCurrentModus()).REFERENCE.entry(0, splitRawInput[1].trim());
			return;
		}
		
		//check if the command was actually matched to the cmd lists. if not, notify the user by the terminal.
		if (parsedCommand.isEmpty()) {
			textOutput.appendText("Command \"" + rawInputCommand + "\" wasn't recognized. Please try again.\n");
			return;
		}
		
		//split the args string into a list, if any, then run the commandSwitch
		if (splitRawInput.length > 1) { 
			String[] inputArgs = splitRawInput[1].split(",");
			for (int i = 0; i < inputArgs.length; i++) {
				inputArgs[i] = inputArgs[i].trim();
			}
			
			commandSwitch(parsedCommand, inputArgs);
		} else {
			commandSwitch(parsedCommand);
		}
		
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
		
		//if the new or ok button was pushed, finish with those specific deck actions
		modiMgr.setCurrentModus(i);
		textOutput.appendText("Modus selected: " + modusName + "\n");
		//textOutput.appendText(e.FUNCTION_MAP.toString() + "\n");
		if (deckAction == 0) {
			textOutput.appendText("Saving deck to file... ");
			try {
				writeDeckToFile(SAVE_FILE_NAME, OUT_PATH);
				textOutput.appendText("save sucessful.\n");
				synchronized (deck) {
					deck.clear();
				}
				textOutput.appendText("Deck has been refresh.\n\n");
			} catch (Exception e) {
				textOutput.appendText("Cancelling modus change.\n");
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
		
		//reset the display
		modiMgr.getModusList().get(modiMgr.getCurrentModus()).REFERENCE.drawToDisplay();
		textOutput.appendText(modiMgr.getModusList().get(modiMgr.getCurrentModus()).REFERENCE.description());
		
		//TODO: check if there is a save file. if so, ask if the user would like to load from it.
			//optionally, i could also just prompt the user indirectly by dropping a hint to load in the text area.
		
		//TODO: set the view to be on the commands tab instead of the modusList tab
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
