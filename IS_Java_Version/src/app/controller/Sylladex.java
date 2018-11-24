package app.controller;

import app.model.*;
import commandline_utils.CmdListeners;
import commandline_utils.Parser;
import commandline_utils.Searcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.util.Pair;
import modus.Modus;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

//NOTE: This class acts as the main controller for the MVC format.

/**
 * The Sylladex acts as the framework for hosting a fetch Modus, a collection of Card, and managing interactions with
 * the user in respect to a selected Modus. <br>
 * <br>
 * The Sylladex should hold an ArrayList of Card as a "deck". This helps support the switching of modi, among other
 * functionality.
 * <br><br>
 * This Sylladex is the main focal point for the application, this being a personal project that is an interactive
 * inventory management system for entertainment purposes. The application is meant to be controlled through a text
 * console to remain true to the "==> Text Adventure" style.
 * <br><br>
 * <b>Architecture</b>
 *
 * @author Triston Scallan
 */
public class Sylladex extends CmdListeners implements Parser {
    /**
     * a List of item(s) that represent items that exist but aren't in a Card.
     * <p>This field utilizes {@link Collections#synchronizedList} and may
     * become synchronized when modified.
     */
    //TODO: consider refactoring static references to utilize a singleton instead?
    private static final List<String> openHand         = new ArrayList<>();
    private static       List<Card>   deck             = new ArrayList<>();
    private              ModusManager modiMgr;
    private final        List<String> totalCommandList = new ArrayList<>();
    private final        CommandMap   SYLL_CMD_MAP     = initSyllCmdMap();
    private static final String       SAVE_FILE_NAME   = "sylladexDeck.sav";
    private static final String       OUT_PATH         = "";
    ///// GUI references ////////////
    @FXML
    private              BorderPane   root;
    //BOTTOM
    @FXML
    private              Button       bInputButton;
    @FXML
    private              TextField    textInput;

    //RIGHT
    ///tab - Commands
    @FXML
    private Accordion cmdAcc;
    @FXML
    private VBox      syllCmdList;
    @FXML
    private Tab       cmdTab;
    @FXML
    private VBox      modusCmdList;
    @FXML
    private VBox      miscList;
    ///tab - Modus List
    @FXML
    private VBox      modusMenuList;
    @FXML
    private Button    bRefresh;
    ///tab - Help
    @FXML
    private Button    bReset;

    //TOP
    @FXML
    private MenuItem mAbout;

    //CENTER
    @FXML
    private        StackPane display;
    private static StackPane staticDisplay;
    @FXML
    private        TextArea  textOutput;
    private static TextArea  staticTextOutput;

    //////////////////////////////////////////////////////


    @FXML
    private void initialize() {
        //// initialize some pane parameters
        //cmdAcc.setExpandedPane(syllCmdPane); //set Sylladex Commands pane as defaulted open
        bRefresh.setDisable(true); //keep disabled until ModusManager can add modi generically (not explicitly)
        bInputButton.setDisable(true);
        bReset.setDisable(true);
        //submit key is not disabled if a modus is chosen and the field isn't empty.
        textInput.setOnKeyTyped((ev) -> {
            if (textInput.getText().isEmpty() || modiMgr.getCurrentModusMetadata() == null) {
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
            } else {
                textInput.requestFocus();
            }

        });

        //set the instance references to the class variables
        staticDisplay = display;
        staticTextOutput = textOutput;

        //initialize a ModusManager
        modiMgr = new ModusManager();

        //add modus nodes to the modusMenuList VBox in modus selection tab, using Metadata from ModiMgr#modusMenuList.
        for (Class<? extends Modus> e : modiMgr.getModusClassList()) {
            //create a GridPane object
            GridPane node = new GridPane();
            //add a column and two rows
            node.getColumnConstraints()
                .add(new ColumnConstraints(10, 100, Region.USE_COMPUTED_SIZE, Priority.SOMETIMES, null, false));
            node.getRowConstraints()
                .add(new RowConstraints(10, 30, Region.USE_COMPUTED_SIZE, Priority.SOMETIMES, null, false));
            node.getRowConstraints()
                .add(new RowConstraints(10, 30, Region.USE_COMPUTED_SIZE, Priority.SOMETIMES, null, false));
            node.setPrefWidth(257.0);
            node.setPrefHeight(40.0);
            node.setPadding(new Insets(0, 5, 5, 5));
            //create a label in the GridPane
            Label name = new Label(e.getSimpleName());
            name.setFont(new Font("Courier", 13));
            node.add(name, 0, 0);
            //create a button in the GridPane
            Button button = new Button("Select");
            button.setFont(new Font("Courier", 11));
            button.setOnAction((event) -> handleModusSelection(e, event)); //set event handler
            GridPane.setHalignment(button, HPos.RIGHT);
            node.add(button, 0, 1);

            //add the GridPane to the modusMenuList
            modusMenuList.getChildren().add(node);

            //create and add a Separator between this and next node.
            Separator hLine = new Separator();
            hLine.setPrefWidth(200);
            modusMenuList.getChildren().add(hLine);
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
        for (String command : SYLL_CMD_MAP.keySet()) {
            syllCmdList.getChildren().addAll(new Label(command), new Separator());
        }

    }

    /**
     * Create a CommandMap of short functions by utilizing consumer lambdas. Also initializes the {@link
     * #totalCommandList} variable.
     *
     * @return CommandMap of the sylladex's user callable commands
     */
    private CommandMap initSyllCmdMap() {
        CommandMap commandMap = new CommandMap();

        commandMap.put("syll.saveDeck", new Pair<>((args) -> {
            textOutput.appendText("Saving deck to file... ");
            try {
                writeDeckToFile();
                textOutput.appendText("save successful at location: " +
                                      java.nio.file.Paths.get(OUT_PATH, SAVE_FILE_NAME).toString() +
                                      ".\n");
            } catch (Exception e) {
                textOutput.appendText("save failed.\n");
            }
        }, ""));

        commandMap.put("syll.loadDeck", new Pair<>((args) -> {
            textOutput.appendText("Loading deck from file... ");
            try {
                loadDeckFromFile();
                textOutput.appendText("load successful.\n");
            } catch (Exception e) {
                textOutput.appendText("load failed.\n");
            }
        }, ""));

        commandMap.put("syll.deleteDeck", new Pair<>((args) -> {
            textOutput.appendText("Deleting deck...");
            deck.clear();
            textOutput.appendText("deletion successful.\n");
        }, ""));

        commandMap.put("syll.deleteSaveFile", new Pair<>((args) -> {
            textOutput.appendText("Deleting save file...");
            try {
                Files.deleteIfExists(Paths.get(OUT_PATH + SAVE_FILE_NAME));
                textOutput.appendText("deletion successful.\n");
            } catch (IOException e) {
                e.printStackTrace();
                textOutput.appendText("deletion failed.\n");
            }
        }, ""));

        commandMap.put("syll.refreshModus", new Pair<>((args) -> {
            textOutput.appendText("Refreshing the modus...");
            modiMgr.refreshModus();
            modiMgr.getCurrentModusMetadata().REFERENCE.drawToDisplay();
            textOutput.appendText("success. Consider using the modus' load command before continuing.\n ");
        }, ""));

        commandMap.put("syll.showLooseItems", new Pair<>((args) -> {
            textOutput.appendText("Items in the hand are currently: \n");
            Iterator<String> hand = openHand.iterator();
            while (hand.hasNext()) {
                String item = hand.next();
                if (hand.hasNext()) textOutput.appendText(item + ", ");
                else textOutput.appendText(item + ".\n");
            }
        }, ""));

        commandMap.put("syll.help", new Pair<>((args) -> {
            //TODO: finish the help method
        }, ""));

        return commandMap;
    }


    ///// GETTERS AND SETTERS /////

    // --Commented out by Inspection START (11/3/18, 2:21 AM):
    //    /**
    //     * @return the modiMgr
    //     */
    //    public ModusManager getModiMgr() {
    //        return modiMgr;
    //    }
    // --Commented out by Inspection STOP (11/3/18, 2:21 AM)

    // --Commented out by Inspection START (11/3/18, 2:22 AM):
    //    /**
    //     * @param modiMgr
    //     *         the modiMgr to set
    //     */
    //    public void setModiMgr(ModusManager modiMgr) {
    //        this.modiMgr = modiMgr;
    //    }
    // --Commented out by Inspection STOP (11/3/18, 2:22 AM)

    /**
     * @return the deck
     */
    public static List<Card> getDeck() {
        return deck;
    }

    /**
     * @param deck
     *         the deck to set
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
     *
     * @param tempDeck
     *         a List of Card(s)
     */
    public static void addToOpenHand(List<Card> tempDeck) {
        synchronized (openHand) {
            for (Card card : tempDeck) {
                openHand.add(card.getItem());
            }
        }
    }

    /**
     * If the parameter item matches an item in {@link #openHand} then remove it. Only a single match will be removed if
     * the openHand contains duplicates.
     *
     * @param item
     *         item to be removed
     * @return true if item was removed. false otherwise.
     */
    @SuppressWarnings("UnusedReturnValue")
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
    public void commandSwitch(String inputCommand, String... args) {

        //parse the raw input command against a list containing both the sylladex and modus commands
        String command = Searcher.parseCommands(inputCommand, () -> totalCommandList);

        //check if the command wasn't matched to the cmd lists. if so, notify the user by the terminal.
        if (command.isEmpty()) {
            textOutput.appendText("Command \"" + inputCommand + "\" wasn't recognized. Please try again.\n");
            return;
        }

        //if true then process as sylladex command, otherwise process as modus command.
        if (command.startsWith("syll.")) {
            SYLL_CMD_MAP.command(command, args);
        } else {
            modiMgr.execModusCmd(command, args);
        }
        textOutput.appendText("\n"); //TODO: shift textOutput dialogues to have \n appear at the front instead of end
    }

    /**
     * Writes the {@link #deck} out to a binary file.
     * <br>
     * Not thread-safe.
     */
    private void writeDeckToFile() throws Exception {
        String fullOutPath = Sylladex.OUT_PATH + Sylladex.SAVE_FILE_NAME;
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(fullOutPath)))) {
            //write deck size to the front of the file,
            oos.writeInt(deck.size());
            for (Card card : deck) {
                oos.writeObject(card);
            }
            //finally write a null to mark the end of the file.
            oos.writeByte((byte) '\0');
        } catch (NullPointerException | FileNotFoundException e) {
            e.printStackTrace();
            throw e;
        } catch (SecurityException e) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Insufficient Permission");
            alert.setHeaderText("Unable to create save file.");
            alert.setContentText("Sylladex was unable to create save file " +
                                 "because the save directory had insufficient permission. \n" +
                                 "Please change directory permissions to allow file" +
                                 "creation and then try again. \n");
            alert.showAndWait();
            throw e;
        }
    }

    /**
     * Loads a binary file to extract a List of Card from. Replaces the current instance of {@link #deck} with the one
     * found in the file.
     */
    private void loadDeckFromFile() throws Exception {
        String fullInPath = Sylladex.OUT_PATH + Sylladex.SAVE_FILE_NAME;
        List<Card> tempDeck = new ArrayList<>();
        final File file = new File(fullInPath);

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Integer numOfCards = ois.readInt();
            for (int i = 0; i < numOfCards; i++) {
                Object o = ois.readObject();
                if (o instanceof Card) tempDeck.add((Card) o);
            }
            setDeck(tempDeck);
        } catch (EOFException e) {
            setDeck(tempDeck);
        } catch (SecurityException e) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Insufficient Permission");
            alert.setHeaderText("Unable to create save file.");
            alert.setContentText("Sylladex was unable to read the save file " +
                                 "because the save file had insufficient permission. \n" +
                                 "Please change file permissions to allow file " +
                                 "read and then try again. \n");
            alert.showAndWait();
            throw e;
        } catch (FileNotFoundException e) {
            System.out.println("sylladex load failed ERROR: file not found at " + file.getPath());
            throw e;
        } catch (NullPointerException | IOException | ClassNotFoundException | ClassCastException e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * Creates a CardNode. Using this method is preferred over creating an instance so that the sylladex is able to
     * regulate and observe production.
     *
     * @param card
     *         A CARD
     * @return a graphical CardNode derived from the CARD
     */
    public static CardNode createCardNode(Card card) {
        return new CardNode(card);
    }

    /**
     * Clears the display node
     */
    public static void clearDisplay() {
        staticDisplay.getChildren().clear();
    }

    ///// LISTENERS /////
    @FXML
    void displayClick(MouseEvent event) {
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
        String inputCommand = splitRawInput[0].trim();

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
     * Sets the current modus to the modus selected, updates the view to represent the modus changes, and handles logic
     * related to selecting and switching modi from the {@code #modusMenuList} node.
     * <p>
     * Called by a modusMenuList button with its assigned metadata argument.
     *
     * @param modusClass
     *         the object information of a given modus
     * @param event
     *         the button push event
     */
    private <M extends Modus> void handleModusSelection(Class<M> modusClass, ActionEvent event) {

        //if there was a previous modus selected, prompt if they want to save or reset their deck
        if (modiMgr.getCurrentModusMetadata() != null) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Changing Modus Confirmation");
            alert.setHeaderText("Are you Sure?");
            alert.setContentText("There is a modus currently active, would you like to " +
                                 "save your deck to file and refresh deck, only refresh to a new deck, or cancel?");
            //create buttons for alert
            ButtonType buttonSave = new ButtonType("Save");
            ButtonType buttonNew = new ButtonType("New");
            ButtonType buttonCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(buttonSave, buttonNew, buttonCancel);
            Optional<ButtonType> result = alert.showAndWait();
            //if cancel, return, otherwise perform deck actions
            if (!result.isPresent()) {
                return;
            } else if (result.get() == buttonSave) {
                textOutput.appendText("Saving deck to file... ");
                try {
                    writeDeckToFile();
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
        try {
            modiMgr.updateCurrentSelectedModus(modusClass);
        } catch (IllegalArgumentException e) {
            //if the given class is invalid
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Modus Selection Error!");
            alert.setHeaderText("The selected Modus was invalid!");
            alert.setContentText("Error in #handleModusSelection, button's assigned class did not pass validation. \n" +
                                 "Modus selection will be aborted.");
            alert.showAndWait();
            return;
        } catch (IllegalAccessException | InstantiationException e) {
            //if the given class couldn't be instantiated.
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Modus Selection Error!");
            alert.setHeaderText("The selected Modus is corrupted or incomplete!");
            alert.setContentText("Error in #handleModusSelection, button's assigned class couldn't be instantiated. \n" +
                                 "Modus selection will be aborted.");
            alert.showAndWait();
            return;
        }
        textOutput.appendText("Modus selected: " + modusClass.getSimpleName() + "\n");

        //set all buttons in this list as not disabled, then disable only this modus' button.
        modusMenuList.getChildren()
                     .stream()
                     .filter(node -> node instanceof GridPane)
                     .forEach(node -> ((GridPane) node).getChildren()
                                                       .stream()
                                                       .filter(subnode -> subnode instanceof Button)
                                                       .findFirst()
                                                       .orElse(new Button())
                                                       .setDisable(false));
        ((Button) event.getSource()).setDisable(true);

        //clear the lists and update them to the selected modus' COMMAND_MAP
        modusCmdList.getChildren().clear();
        totalCommandList.clear();
        totalCommandList.addAll(SYLL_CMD_MAP.keySet());
        for (String command : modiMgr.getCurrentModusMetadata().COMMAND_MAP.keySet()) {
            if (command == null) continue;
            else totalCommandList.add(command);
            //for each command, create a node of the command name and description to be inserted into the modusCmdList
            Label commandName = new Label(command);
            Label commandDesc = new Label();
            commandDesc.setText(modiMgr.getCurrentModusMetadata().COMMAND_MAP.desc(command));
            commandDesc.setWrapText(true);
            commandDesc.setPadding(new Insets(0, 0, 0, 5));
            Separator line = new Separator();
            modusCmdList.getChildren().addAll(commandName, commandDesc, line);
        }

        //reset the display
        modiMgr.getCurrentModusMetadata().REFERENCE.drawToDisplay();
        //display this modus' description to screen
        textOutput.appendText(modiMgr.getCurrentModusMetadata().REFERENCE.description());

        //set the view to be on the commands tab from the modusMenuList tab
        ((TabPane) cmdTab.getStyleableParent()).getSelectionModel().select(cmdTab);
    }

    @FXML
    void reset(ActionEvent event) {
        //TODO: set currentmodus to -1 and reset reinitialize the sylladex
    }

}
