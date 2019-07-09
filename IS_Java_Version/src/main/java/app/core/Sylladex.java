package app.core;

import app.model.Card;
import app.ui.GuiPropertyMap;
import app.ui.ModusSelectComponent;
import app.util.CommandMap;
import app.util.RequestException;
import app.util.SyllCommandMap;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * The Sylladex acts as the framework for hosting a fetch Modus, a collection of Card, and managing interactions with the user in respect to
 * a selected Modus. <br>
 * <br>
 * The Sylladex should hold an ArrayList of Card as a "deck". This helps support the switching of modi, among other functionality.
 * <br><br>
 * This Sylladex is the main focal point for the application, this being a personal project that is an interactive inventory management
 * system for entertainment purposes. The application is meant to be controlled through a text console to remain true to the "==> Text
 * Adventure" style.
 * <br><br>
 * <b>Architecture</b>
 *
 * @author Triston Scallan
 */
@ParametersAreNonnullByDefault
public class Sylladex {
    private static final Logger               LOGGER         = LogManager.getLogger(Sylladex.class);
    private static final String               SYLL_PREFIX    = "syll.";
    private static final String               SAVE_FILE_NAME = "sylladexDeck.sav";
    private static final String               OUT_PATH       = "";
    private final        SyllCommandMap       SYLL_CMD_MAP; //depended on by handleSyllInput
    private final        ListProperty<String> openHandProperty;
    private final        ListProperty<Card>   deckProperty;

    public Sylladex(GuiPropertyMap guiPropertyMap) {
        openHandProperty = new SimpleListProperty<>(this, "open_hand", FXCollections.observableArrayList());
        deckProperty = new SimpleListProperty<>(this, "deck", FXCollections.observableArrayList());
        ModusContainer modusContainer = new ModusContainer(guiPropertyMap.submittedInputSubscriberProperty(),
                                                           guiPropertyMap.displayProperty(),
                                                           guiPropertyMap.textOutputProperty(),
                                                           guiPropertyMap.textInputProperty(),
                                                           deckProperty,
                                                           openHandProperty);
        SYLL_CMD_MAP = initSyllCmdMap(guiPropertyMap.textOutputProperty(), modusContainer);

        //should attempt to prompt the user to equip a modus
        Label noModusPrompt_line1 = new Label("No modus selected.");
        Label noModusPrompt_line2 = new Label("Please see the \"Modus List\" tab.");
        noModusPrompt_line1.setFont(new Font("Courier", 18));
        noModusPrompt_line2.setFont(new Font("Courier", 18));
        VBox noModusPromptVBox = new VBox(150, noModusPrompt_line1, noModusPrompt_line2);
        noModusPromptVBox.setAlignment(Pos.CENTER);
        guiPropertyMap.getDisplay().getChildren().add(noModusPromptVBox);

        //initialize the syllCmdList
        for (String command : SYLL_CMD_MAP.keySet()) {
            guiPropertyMap.getSyllCmdListChildren().addAll(new Label(SYLL_PREFIX + command), new Separator());
        }

        //add a handler to the submitted input subscriber
        guiPropertyMap.getSubmittedInputSubscriber().accept(this::handleSyllInput);
        //provide the handler for the modus selection
        guiPropertyMap.modusSelectionHandlerProperty()
                      .setValue((event) -> handleModusSelection(modusContainer,
                                                                guiPropertyMap.textOutputProperty(),
                                                                guiPropertyMap.modusCmdListChildrenProperty(),
                                                                this.deckProperty,
                                                                event));

        guiPropertyMap.setAllModusMenuSelectableClassList(modusContainer.getModusClassList());
    }


    /**
     * Create a command map of short functions by utilizing consumer lambdas.
     *
     * @param textOutputProperty
     *         the property that holds the current text control object
     * @return map of the sylladex's user callable commands
     */
    @Nonnull
    private SyllCommandMap initSyllCmdMap(final ReadOnlyObjectProperty<? extends TextInputControl> textOutputProperty,
                                          final ModusContainer modiMgr) {
        SyllCommandMap commandMap = new SyllCommandMap(CommandMap.Case.INSENSITIVE);
        commandMap.put("saveDeckToFile", () -> {
            TextInputControl textOutput = textOutputProperty.getValue();
            textOutput.appendText("Saving deck to file... ");
            try {
                Optional<File> saveFile = FileController.selectFileSave(textOutput.getScene().getWindow());
                if (saveFile.isPresent()) {
                    modiMgr.requestSave();
                    FileController.writeDeckToFile(getDeck(), saveFile.get());
                    textOutput.appendText("Save successful.\n");
                } else {
                    LOGGER.info("Save cancelled.");
                    textOutput.appendText("save cancelled.\n");
                }
            } catch (SecurityException | FileNotFoundException e) {
                LOGGER.error(e);
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Unable To Create Or Open File");
                alert.setHeaderText("Unable to open a save file.");
                alert.setContentText("Sylladex was unable to create save file " +
                                     "because this program could not open the file, likely as a security issue. \n" +
                                     "Please change directory/file permissions to allow file " +
                                     "creation and then try again. \n");
                alert.showAndWait();
                textOutput.appendText("save failed.\n");
            } catch (IOException | RequestException e) {
                LOGGER.error(e);
                textOutput.appendText("ERROR saving file - could not save. Please try again in a bit.\n");
            }
        });
        commandMap.put("loadDeckFromFile", () -> {
            TextInputControl textOutput = textOutputProperty.getValue();
            textOutput.appendText("Loading deck from file... ");
            try {
                Optional<File> loadFile = FileController.selectFileLoad(textOutput.getScene().getWindow());
                if (loadFile.isPresent()) {
                    setDeck(FileController.loadDeckFromFile(loadFile.get()));
                    textOutput.appendText("load successful.\n");
                    modiMgr.requestLoad();
                } else {
                    LOGGER.info("Load cancelled.");
                    textOutput.appendText("load cancelled.\n");
                }
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
            TextInputControl textOutput = textOutputProperty.getValue();
            textOutput.appendText("Deleting deck...");
            setDeck(Collections.emptyList());
            textOutput.appendText("deletion successful.\n");
        });
        commandMap.put("deleteSaveFile", () -> {
            TextInputControl textOutput = textOutputProperty.getValue();
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
            TextInputControl textOutput = textOutputProperty.getValue();
            textOutput.appendText("Refreshing the modus...");
            modiMgr.resetModus();
            modiMgr.requestDrawToDisplay();
            textOutput.appendText("success. Consider using the modus' load command before continuing.\n ");
        });
        commandMap.put("showLooseItems", () -> {
            TextInputControl textOutput = textOutputProperty.getValue();
            textOutput.appendText("Items in the hand are currently: \n");
            synchronized (getOpenHand()) {
                for (ListIterator<String> hand = getOpenHand().listIterator(); hand.hasNext(); ) {
                    String item = hand.next();
                    if (hand.hasNext()) textOutput.appendText(item + ", ");
                    else textOutput.appendText(item + ".\n");
                }
            }
        });

        return commandMap;
    }

    ///// HANDLERS /////
    void handleSyllInput(ObservableValue<? extends String> bean, String oldInput, @Nullable String newInput) throws NoSuchElementException {
        LOGGER.traceEntry("handleSyllInput(bean={}, oldInput={}, newInput={}", bean, oldInput, newInput);

        if (newInput == null) {
            LOGGER.warn("New input for submitted input handler was null. " +
                        "Input should not be set to null or changed outside of the input component controller. " +
                        "Please review and revise code. ");
            return;
        }
        String inputString = newInput.trim();
        // if no match, then the input is not directed to this handler.
        if (!inputString.toUpperCase().startsWith(SYLL_PREFIX.toUpperCase())) return;

        String   command     = inputString.substring(SYLL_PREFIX.length());
        Runnable runnableCmd = SYLL_CMD_MAP.get(command);
        if (runnableCmd == null) {
            LOGGER.error("Invalid sylladex command string supplied as input: " + command);
            return;
        }
        LOGGER.info("running sylladex command {}.", command);
        SYLL_CMD_MAP.command(command);
        LOGGER.traceExit();
    }

    /**
     * Sets the current modus to the modus selected, updates the view to represent the modus changes, and handles logic related to selecting
     * and switching modi from the {@code #modusMenuList} node.
     * <p>
     * Called by a modusMenuList button with its assigned metadata argument.
     *
     * @param event
     *         the button push event
     */
    private static void handleModusSelection(ModusContainer modusContainer,
                                             ReadOnlyObjectProperty<? extends TextInputControl> textOutputProperty,
                                             ReadOnlyListProperty<Node> modusCmdListChildrenProperty,
                                             ListProperty<Card> deckProperty,
                                             ActionEvent event) {
        TextInputControl textOutput = textOutputProperty.getValue();

        //if there was a previous modus selected, prompt if they want to save or reset their deck
        if (modusContainer.getCurrentModusMetadata() != null) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Changing Modus Confirmation");
            alert.setHeaderText("Are you Sure?");
            alert.setContentText("There is a modus currently active, would you like to " +
                                 "save your deck to file and refresh deck, only refresh to a new deck, or cancel?");
            //create buttons for alert
            ButtonType buttonSave   = new ButtonType("Save");
            ButtonType buttonNew    = new ButtonType("New");
            ButtonType buttonCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(buttonSave, buttonNew, buttonCancel);
            Optional<ButtonType> result = alert.showAndWait();
            //if cancel, return, otherwise perform deck actions
            if (!result.isPresent()) {
                return;
            } else if (result.get() == buttonSave) {
                textOutput.appendText("Saving deck to file... ");
                try {
                    Optional<File> saveFile = FileController.selectFileSave(textOutput.getScene().getWindow());
                    if (saveFile.isPresent()) {
                        FileController.writeDeckToFile(deckProperty.getValue(), saveFile.get());
                        textOutput.appendText("save successful.\n");
                    } else {
                        LOGGER.info("Save cancelled.");
                        textOutput.appendText("save cancelled.\n");
                    }
                    deckProperty.getValue().clear();
                    textOutput.appendText("Deck has been refreshed.\n\n");
                } catch (Exception e) {
                    textOutput.appendText("Cancelling modus change.\n\n");
                    return;
                }
            } else if (result.get() == buttonNew) {
                deckProperty.getValue().clear();
                textOutput.appendText("Deck has been refreshed without saving.\n\n");
            } else {
                return;
            } //todo: add the option to just reload the new modus with current deck instead of refreshing deck
        }

        //set the new active modus
        String modusClass = ((Node) event.getSource()).getId().substring(ModusSelectComponent.BUTTON_ID_PREFIX.length());
        try {
            modusContainer.updateCurrentModus(modusClass);
        } catch (RuntimeException e) {
            //if the given class is invalid
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Modus Selection Error!");
            alert.setHeaderText("The selected Modus was unable to be used!");
            alert.setContentText("Error in #handleModusSelection, button's assigned class could not be validly instantiated. \n" +
                                 "Modus selection will be aborted.");
            alert.showAndWait();
            return;
        }
        textOutput.appendText("Modus selected: " + modusClass + "\n");

        //clear the lists and update them to the selected modus' COMMAND_MAP
        modusCmdListChildrenProperty.getValue().clear();
        for (String command : modusContainer.getCurrentModusMetadata().COMMAND_MAP.keySet()) {
            if (command == null) continue;
            //for each command, create a node of the command name and description to be inserted into the modusCmdList
            Label commandName = new Label(ModusContainer.MODUS_PREFIX + command);
            Label commandDesc = new Label();
            commandDesc.setText(modusContainer.getCurrentModusMetadata().COMMAND_MAP.desc(command));
            commandDesc.setWrapText(true);
            commandDesc.setPadding(new Insets(0, 0, 0, 5));
            Separator line = new Separator();
            modusCmdListChildrenProperty.getValue().addAll(commandName, commandDesc, line);
        }
        //reset the display
        modusContainer.requestDrawToDisplay();
        //display this modus' description to screen
        textOutput.appendText(modusContainer.requestDescription());
    }

    ///// GETTERS AND SETTERS /////
    @Nonnull
    private List<Card> getDeck() {
        return deckProperty.get();
    }

    private void setDeck(List<Card> deck) {
        deckProperty.setValue(FXCollections.observableList(deck));
    }

    @Nonnull
    private List<String> getOpenHand() {
        return openHandProperty.get();
    }
}
