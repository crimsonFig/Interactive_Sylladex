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
import java.util.function.Function;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import app.model.Card;
import app.model.CardNode;
import app.model.Metadata;
import commandline_utils.*;

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
public class Sylladex extends CmdListeners implements Parser{
	/**
	 * a List of item(s) that represent items that exist but aren't in a Card.
	 * <p>This field utilizes {@link Collections#synchronizedList} and may
	 * 	become synchronized when modified.
	 */
	private static List<String> openHand = new ArrayList<String>();
	private static List<Card> deck = new ArrayList<Card>();
	private ModusManager modiMgr;
	private static final LinkedHashSet<String> SYLL_CMD_STRING_LIST = initializeSyllCmdStringList();
	private static final String SAVE_FILE_NAME = "sylladexDeck.sav";
	private static final String OUT_PATH = "";
	///// GUI references ////////////
	@FXML
	private BorderPane root;
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
	private Tab cmdTab;
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
	private static StackPane staticDisplay;
	@FXML
	private TextArea textOutput;
	private static TextArea staticTextOutput;
	
	//////////////////////////////////////////////////////
	
	
	@FXML
	private void initialize() {
		//// initialize some pane parameters
		//cmdAcc.setExpandedPane(syllCmdPane); //set Sylladex Commands pane as defaulted open
		bRefresh.setDisable(true); //keep disabled until ModusManager can add modi generically (not explicitly)
		bModusScan.setDisable(true);
		bInputButton.setDisable(true);
		bReset.setDisable(true);
		//submit key is not disabled if a modus is chosen and the field isnt empty.
		textInput.setOnKeyTyped((ev) -> {
			if (textInput.getText().isEmpty() || modiMgr.getCurrentModus() == -1) {
				bInputButton.setDisable(true);
			} else {
				bInputButton.setDisable(false);
			}
		});
		//if the enter key is typed, fire the submit button, else focus on the field.
		root.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getCode() == KeyCode.ENTER) {
				bInputButton.fire();
				keyEvent.consume();
			} 
			else {
				textInput.requestFocus();
			}
			
		});
		
		//set the instance references to the class variables
		staticDisplay = display;
		staticTextOutput = textOutput;
		
		//should initialize a ModusManager
		modiMgr = new ModusManager();
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
			button.setOnAction((event) -> handleModusSelection(e, event)); //set event handler
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
	public static List<Card> getDeck() {
		return deck;
	}

	/**
	 * @param deck the deck to set
	 */
	public static void setDeck(List<Card> deck) {
		Sylladex.deck = deck;
	}
	
	/**
	 * @return the display
	 */
	public static StackPane getDisplay() {
		return staticDisplay;
	}
	
	/**
	 * @return the textOutput
	 */
	public static TextArea getTextOutput() {
		return staticTextOutput;
	}
	
	/**
	 * adds a collection of Card(s) to {@link #openHand}
	 * <p>Note: This block will synchronize when in use
	 * @param tempDeck a List of Card(s)
	 */
	public static void addToOpenHand(List<Card> tempDeck) {
		synchronized (openHand) {
			for (Card card : tempDeck) {
				openHand.add(card.getItem());
			}
		}
	}
	
	/**
	 * If the parameter item matches an item in {@link #openHand}
	 * then remove it. Only a single match will be removed if the 
	 * openHand contains duplicates.
	 * @param item
	 * @return true if item was removed. false otherwise.
	 */
	public static Boolean removeFromHand(String item) {
		//TODO: use this to remove an openHand item during capture.
		Boolean result = false;
		for (String handItem : openHand) {
			if (handItem.equalsIgnoreCase(item)) {
				openHand.remove(handItem);
				result = true;
				break;
			}
		}
		return result;
	}
	
///// UTILITY /////
	@Override
	public void commandSwitch(String inputCommand, String...args) {
		
		//parse the raw input command against the sylladex and modus commands
		String command = Searcher.parseCommands(inputCommand, () -> {
			List<Object> keyList = Arrays.asList(modiMgr.getModusList().get(modiMgr.getCurrentModus()).FUNCTION_MAP.keySet().toArray());
			List<String> tCommandList = new ArrayList<String>();
			keyList.forEach(key -> tCommandList.add((String) key));
			SYLL_CMD_STRING_LIST.forEach(string -> tCommandList.add(string));
			return tCommandList;
		});
		
		//check if the command was "help" (from raw input), if so then invoke entry with the args
		if (Searcher.fuzzyStringSearch(Arrays.asList("help"), inputCommand).getValue().equals("help") && args.length > 0) {
			modiMgr.getModusList().get(modiMgr.getCurrentModus()).REFERENCE.entry(0, args[0].trim());
			return;
		}
		
		//check if the command was actually matched to the cmd lists. if not, notify the user by the terminal.
		if (command.isEmpty()) {
			textOutput.appendText("Command \"" + inputCommand + "\" wasn't recognized. Please try again.\n");
			return;
		}
		
		//if true then process as sylladex command, otherwise process as modus command
		if (command.contains("syll.")) {
			String syllCommand = command.substring(5); //chop off the "syll." prefix
			switch (syllCommand) {
			case "saveDeck":
				textOutput.appendText("Saving deck to file... ");
				try {
					writeDeckToFile(SAVE_FILE_NAME, OUT_PATH);
					textOutput.appendText("save sucessful at location: " + java.nio.file.Paths.get(OUT_PATH, SAVE_FILE_NAME).toString() + ".\n");
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
				deck.clear();
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
		textOutput.appendText("\n"); //TODO: shift textoutput dialogues to have \n appear at the front instead of end
	}
	
	/**
	 * Writes the {@link #deck} out to a binary file.
	 * <br>
	 * Not thread-safe.
	 * @param fileName the name of the file to be created
	 * @param outPath the relative pathway to a folder for the file to be created in
	 */
	private void writeDeckToFile(String fileName, String outPath) throws Exception {
		String fullOutPath = outPath + fileName;
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(fullOutPath)))) {
			//write deck size to the front of the file, 
			oos.writeInt(Integer.valueOf(deck.size()));
			for (Card card : deck) {
				oos.writeObject(card);
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
		
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
			Integer numOfCards = ois.readInt();
			for (int i = 0; i < numOfCards; i++ ) {
				Object o = ois.readObject();
				if (o instanceof Card && o != null) tempDeck.add((Card) o);
			}
			setDeck(tempDeck);
		} catch (EOFException e) {
			setDeck(tempDeck);
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
			System.out.println("sylladex load failed ERROR: file not found at " + file.getPath());
			throw e;
		} catch (NullPointerException | IOException| ClassNotFoundException | ClassCastException e) {
			e.printStackTrace();
			throw e;
		} 
	}
	
	
	
	/**
	 * Creates a CardNode. Using this method is preferred over creating an instance
	 * so that the sylladex is able to regulate and observe production.
	 * @param card A CARD
	 * @return a graphical CardNode derived from the CARD
	 */
	public static CardNode createCardNode(Card card) {
		CardNode node = new CardNode(card);
		return node;
	}
	
	/**
	 * Clears the display node
	 */
	public static void clearDisplay() {
		staticDisplay.getChildren().clear();
	}
	
///// LISTENERS /////
	@FXML
	void displayClick(ActionEvent event) {
		//TODO: consider displaying info on click?
	}
	
	//TODO: create a listener for if someone clicks a label in the commands list. if a command-label is clicked
		//then it should set the textInput contents equal to the label
	
	@FXML
	void submit(ActionEvent event) {
		//consume the textInput field
		String inputRawString = textInput.getText();
		textInput.clear();
		if (inputRawString.isEmpty()) return;
		
		//split the raw input into ["command", "arg1 arg2 arg3..."]
		String[] splitRawInput = inputRawString.split(" ", 2);
		String inputCommand = splitRawInput[0];
		
		//split the args string into a list, if any, then run the commandSwitch
		if (splitRawInput.length > 1) { 
			String[] inputArgs = splitRawInput[1].split(",");
			for (int i = 0; i < inputArgs.length; i++) {
				inputArgs[i] = inputArgs[i].trim();
			}
			
			commandSwitch(inputCommand, inputArgs);
		} else {
			commandSwitch(inputCommand);
		}
		
	}
	
	/**
	 * Sets the current modus to the modus selected, updates the view 
	 * to represent the modus changes, and handles logic related to 
	 * selecting and switching modi from the {@code #modusList} node.
	 * @param event 
	 * @param metadata
	 */
	void handleModusSelection(Metadata metadata, ActionEvent event) {
		int i = -1;
		Boolean bFound = false;
		Boolean isAnyModusActive = modiMgr.getCurrentModus() != -1;
		for (Metadata e : modiMgr.getModusList()) {
			i++;
			if (e.equals(metadata)) {
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
	        //if cancel, return, otherwise perform deck actions
	        if (result.get() == buttonSave) {
				textOutput.appendText("Saving deck to file... ");
				try {
					writeDeckToFile(SAVE_FILE_NAME, OUT_PATH);
					textOutput.appendText("save successful.\n");
					Sylladex.deck.clear();
					textOutput.appendText("Deck has been refreshed.\n\n");
				} catch (Exception e) {
					textOutput.appendText("Cancelling modus change.\n\n");
			        return;
				}
	        } else if (result.get() == buttonNew) {
	        		Sylladex.deck.clear();
	        		textOutput.appendText("Deck has been refreshed without saving.\n\n");
	        } else {
	        		return;
	        }
		}
		
		//set the new active modus
		modiMgr.setCurrentModus(i);
		textOutput.appendText("Modus selected: " + metadata.NAME + "\n");
		
		//set all buttons in this list as not disabled, then disable only this modus' button.
		modusList.getChildren().stream() 
			.filter(node -> GridPane.class.equals(node.getClass()))
			.map((Function<? super Node, ? extends Button>) node ->
					(Button) ((GridPane) node).getChildren().stream()
											  .filter(subnode -> Button.class.equals(subnode.getClass()))
											  .findFirst().orElse(new Button()))
			.forEach(node -> node.setDisable(false));
		((Button) event.getSource()).setDisable(true);
		
		//clear the moduCmdList and set the moduCmdList to the selected modus FUNCTION_MAP
		moduCmdList.getChildren().clear();
		for(String function : modiMgr.getModusList().get(modiMgr.getCurrentModus()).FUNCTION_MAP.keySet()) {
			//for each function, create a node to be inserted into the moduCmdList
			Label functionName = new Label(function);
			//add a description label to each command by pinging the `help <commandName> <1>` in Modus#entry method
			Label functionDesc = new Label();
			functionDesc.setText(modiMgr.getModusList().get(modiMgr.getCurrentModus()).REFERENCE.entry(0, function, "1"));
			functionDesc.setWrapText(true);
			functionDesc.setPadding(new Insets(0, 0, 0, 5)); 
			Separator line = new Separator();
			moduCmdList.getChildren().addAll(functionName, functionDesc, line);
		}
		Label functionName = new Label("help <command name>");
		moduCmdList.getChildren().add(functionName);
		
		//reset the display
		modiMgr.getModusList().get(modiMgr.getCurrentModus()).REFERENCE.drawToDisplay();
		textOutput.appendText(modiMgr.getModusList().get(modiMgr.getCurrentModus()).REFERENCE.description());
		
		//set the view to be on the commands tab from the modusList tab
		((TabPane) cmdTab.getStyleableParent()).getSelectionModel().select(cmdTab);
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
