package app.controller;

import app.model.*;
import app.util.CommandMap;
import app.util.SyllCommandMap;
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
import modus.Modus;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

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
public class Sylladex {
    private final        AtomicReference<String>       wrappedModusInput = new AtomicReference<>();
    private final        AtomicReference<StackPane>    wrappedDisplay    = new AtomicReference<>();
    private final        AtomicReference<TextArea>     wrappedTextOutput = new AtomicReference<>();
    private final        AtomicReference<List<String>> wrappedOpenHand   = new AtomicReference<>();
    private final        AtomicReference<List<Card>>   wrappedDeck       = new AtomicReference<>();
    private              ModusManager                  modiMgr;
    private final        SyllCommandMap                SYLL_CMD_MAP      = initSyllCmdMap();
    private static final String                        SYLL_PREFIX       = "SYLL.";
    private static final String                        SAVE_FILE_NAME    = "sylladexDeck.sav";
    private static final String                        OUT_PATH          = "";

    ///// GUI references ////////////
    @FXML
    private BorderPane root;

    //BOTTOM
    @FXML
    private Button    bInputButton;
    @FXML
    private TextField textInput;

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
    private StackPane display;
    @FXML
    private TextArea  textOutput;

    //////////////////////////////////////////////////////


    @FXML
    private void initialize() {
        //// initialize pane parameters
        //cmdAcc.setExpandedPane(syllCmdPane); //set Sylladex Commands pane as defaulted open
        bRefresh.setDisable(true); //keep disabled until ModusManager can add modi generically (not explicitly)
        bInputButton.setDisable(true);
        bReset.setDisable(true);
        //submit key is not disabled if a modus is chosen and the field isn't empty.
        textInput.setOnKeyTyped((ev) -> {
            if (textInput.getText().isEmpty() || modiMgr.getCurrentModus() == null) {
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

        //initialize wrapped objects
        wrappedModusInput.set(null);
        wrappedDisplay.set(display);
        wrappedTextOutput.set(textOutput);
        wrappedDeck.set(Collections.synchronizedList(new ArrayList<>()));
        wrappedOpenHand.set(Collections.synchronizedList(new ArrayList<>()));
        //initialize the ModusManager
        modiMgr = new ModusManager(wrappedModusInput, wrappedDisplay, wrappedTextOutput, wrappedDeck, wrappedOpenHand);


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
            syllCmdList.getChildren().addAll(new Label(SYLL_PREFIX + command), new Separator());
        }

    }

    /**
     * Create a command map of short functions by utilizing consumer lambdas.
     *
     * @return map of the sylladex's user callable commands
     */
    private SyllCommandMap initSyllCmdMap() {
        SyllCommandMap commandMap = new SyllCommandMap(CommandMap.Case.SENSITIVE);

        commandMap.put("saveDeckToFile", () -> {
            textOutput.appendText("Saving deck to file... ");
            try {
                modiMgr.requestSave();
                synchronized (getDeck()) {
                    FileController.writeDeckToFile(getDeck(), OUT_PATH + SAVE_FILE_NAME);
                }
                textOutput.appendText(String.format("save successful at location: %s.\n",
                                                    java.nio.file.Paths.get(OUT_PATH, SAVE_FILE_NAME).toString()));
            } catch (SecurityException | FileNotFoundException e) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Unable To Create Or Open File");
                alert.setHeaderText("Unable to open a save file.");
                alert.setContentText("Sylladex was unable to create save file " +
                                     "because this program could not open the file, likely as a security issue. \n" +
                                     "Please change directory/file permissions to allow file " +
                                     "creation and then try again. \n");
                alert.showAndWait();
                textOutput.appendText("save failed.\n");
            } catch (IOException e) {
                e.printStackTrace();
                textOutput.appendText("ERROR saving file - save failed. Please try again.\n");
            }
        });
        commandMap.put("loadDeckFromFile", () -> {
            textOutput.appendText("Loading deck from file... ");
            try {
                setDeck(FileController.loadDeckFromFile(OUT_PATH + SAVE_FILE_NAME));
                textOutput.appendText("load successful.\n");
                //TODO: consider if this command should request the modus to load
            } catch (SecurityException | FileNotFoundException e) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Insufficient Permission");
                alert.setHeaderText("Unable to create save file.");
                alert.setContentText("Sylladex was unable to read the save file " +
                                     "because the save file had insufficient permission. \n" +
                                     "Please change file permissions to allow file " +
                                     "read and then try again. \n");
                alert.showAndWait();
                textOutput.appendText("load failed.\n");
            } catch (ClassNotFoundException | ClassCastException e) {
                textOutput.appendText("ERROR - found file is corrupted or invalid. load failed. \n");
            } catch (IOException e) {
                textOutput.appendText("ERROR loading file - load failed. Please try again.\n");
            }
        });
        commandMap.put("deleteDeck", () -> {
            textOutput.appendText("Deleting deck...");
            setDeck(Collections.emptyList());
            textOutput.appendText("deletion successful.\n");
        });
        commandMap.put("deleteSaveFile", () -> {
            textOutput.appendText("Deleting save file...");
            try {
                Files.deleteIfExists(Paths.get(OUT_PATH + SAVE_FILE_NAME));
                textOutput.appendText("deletion successful.\n");
            } catch (IOException e) {
                e.printStackTrace();
                textOutput.appendText("deletion failed.\n");
            }
        });
        commandMap.put("resetModus", () -> {
            textOutput.appendText("Refreshing the modus...");
            modiMgr.resetModus();
            modiMgr.requestDrawToDisplay();
            textOutput.appendText("success. Consider using the modus' load command before continuing.\n ");
        });
        commandMap.put("showLooseItems", () -> {
            textOutput.appendText("Items in the hand are currently: \n");
            synchronized (getOpenHand()) {
                for (ListIterator<String> hand = getOpenHand().listIterator(); hand.hasNext();){
                    String item = hand.next();
                    if (hand.hasNext()) textOutput.appendText(item + ", ");
                    else textOutput.appendText(item + ".\n");
                }
            }
        });

        return commandMap;
    }


    ///// GETTERS AND SETTERS /////
    private List<Card> getDeck() {
        return wrappedDeck.get();
    }

    private void setDeck(List<Card> deck) {
        synchronized (wrappedDeck.get()) {
            wrappedDeck.set(Collections.synchronizedList(deck));
        }
    }

    private List<String> getOpenHand() {
        return wrappedOpenHand.get();
    }

    private void setModusInput(String modusInput) {
        wrappedModusInput.set(modusInput);
    }

    ///// HANDLERS /////
    private void handleSyllInput(String input) throws NoSuchElementException {
        String command = input.trim();
        Runnable runnableCmd = SYLL_CMD_MAP.get(command);
        if (runnableCmd == null) {
            throw new NoSuchElementException(String.format("given sylladex command `%s` does not exist.\n", command));
        }
        System.out.format("running sylladex command `%s`.\n", command);
        SYLL_CMD_MAP.command(command);
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
        String rawInputString = textInput.getText();
        textInput.clear();
        if (rawInputString.isEmpty()) return;

        //determine where to send input based on prefix. no prefix means its for modus.
        if (rawInputString.toUpperCase().startsWith(SYLL_PREFIX)) {
            handleSyllInput(rawInputString.substring(SYLL_PREFIX.length()));
        }
        else {
            if(wrappedModusInput.getAndSet(rawInputString) != null)
                System.err.println("WARNING - Modus failed to consume the wrappedModusInput since last submit event");
            modiMgr.handleModusInput();
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
        if (modiMgr.getCurrentModus() != null) {
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
                    FileController.writeDeckToFile(getDeck(), OUT_PATH + SAVE_FILE_NAME);
                    textOutput.appendText("save successful.\n");
                    getDeck().clear();
                    textOutput.appendText("Deck has been refreshed.\n\n");
                } catch (Exception e) {
                    textOutput.appendText("Cancelling modus change.\n\n");
                    return;
                }
            } else if (result.get() == buttonNew) {
                getDeck().clear();
                textOutput.appendText("Deck has been refreshed without saving.\n\n");
            } else {
                return;
            }
        }

        //set the new active modus
        try {
            modiMgr.updateCurrentModus(modusClass);
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
        for (String command : modiMgr.getCurrentModus().COMMAND_MAP.keySet()) {
            if (command == null) continue;
            //for each command, create a node of the command name and description to be inserted into the modusCmdList
            Label commandName = new Label(command);
            Label commandDesc = new Label();
            commandDesc.setText(modiMgr.getCurrentModus().COMMAND_MAP.desc(command));
            commandDesc.setWrapText(true);
            commandDesc.setPadding(new Insets(0, 0, 0, 5));
            Separator line = new Separator();
            modusCmdList.getChildren().addAll(commandName, commandDesc, line);
        }

        //reset the display
        modiMgr.requestDrawToDisplay();
        //display this modus' description to screen
        textOutput.appendText(modiMgr.requestDescription());

        //set the view to be on the commands tab from the modusMenuList tab
        ((TabPane) cmdTab.getStyleableParent()).getSelectionModel().select(cmdTab);
    }

    @FXML
    void reset(ActionEvent event) {
        //TODO: set currentmodus to -1 and reset reinitialize the sylladex
    }

}
