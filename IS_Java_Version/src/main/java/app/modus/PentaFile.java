package app.modus;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import app.model.*;
import app.util.*;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.util.Pair;

/**
 * This fetch modus is the PentaFile pfModus, a modus designed for use with a sylladex. Using a structure comprised of 5
 * arrays containing 5 cards, and busting if a single array is overfilled. <br> This is likened to a File Cabinet. 5
 * folders that can hold 5 files each.
 * <dt> Note: </dt>
 * <dd>
 * 1. The inventory only holds 25 cards, 5 cards in 5 folders. <br> 2. If 6 items are placed into a folder, 5 are
 * ejected and the 6th is pushed <br> 3. The current PentaFile can have all its information saved and loaded to a text
 * file. <br>
 * </dd>
 *
 * @author Triston Scallan
 */
@ModusMetatagRunStatus(true)
public class PentaFile implements Modus {
    /**
     * provides information about this modus
     */
    private final Metadata METADATA;

    //5 arrays, each with 5 elements
    private Card[] weapons     = new Card[5];
    private Card[] survival    = new Card[5];
    private Card[] misc        = new Card[5];
    private Card[] info        = new Card[5];
    private Card[] keyCritical = new Card[5];

    //***************************** INITIALIZE ***********************************/

    /**
     * The constructor of a fetch Modus should save the reference to the sylladex so that it can functionally return a
     * list of the Modus' functionality to the ModusManager, specifically passing modusMetadata.
     */
    public PentaFile() {
        //initialize the METADATA
        this.METADATA = new Metadata(this.getClass().getSimpleName(), this.createFunctionMap(), this);

        //attempt to initialize the modus space
        Arrays.fill(weapons, Card.EMPTY);
        Arrays.fill(survival, Card.EMPTY);
        Arrays.fill(misc, Card.EMPTY);
        Arrays.fill(info, Card.EMPTY);
        Arrays.fill(keyCritical, Card.EMPTY);
    }

    private ModusCommandMap createFunctionMap() {
        ModusCommandMap commandMap = new ModusCommandMap(CommandMap.Case.INSENSITIVE);
        commandMap.put("save", new Pair<>((args, modusBuffer) -> {
            save(modusBuffer);
            modusBuffer.getTextOutput().appendText("Deck was saved to sylladex.\n");
        },
                                          "syntax: save\n\u2022 saves the current inventory to the sylladex's deck. " +
                                          "This command is called at the end of every other command except load."));

        commandMap.put("load",
                       new Pair<>((args, modusBuffer) -> load(modusBuffer),
                                  "syntax: load\n\u2022 loads the inventory from the sylladex, which may differ." +
                                  "\n\u2022 mode 0 will simply reset the inventory." +
                                  "\n\u2022 mode 1 will auto load the inventory, based on CARD positions in the deck." +
                                  "\n\u2022 mode 2 will manually load the inv. you will choose where items go." +
                                  "\n\u2022 mode 3 will fast load the inventory. disregards saved CARD positions."));

        commandMap.put("capture", new Pair<>((args, modusBuffer) -> {
            TextArea textOutput = modusBuffer.getTextOutput();
            if (args.length != 1) throw IllegalSyntaxException.ofArgLength(args.length);

            String itemName = args[0];
            textOutput.appendText("Attempting to capture " + itemName + "...");
            try {
                capture(args[0]);       //<< may throw IllegalArgumentException, IllegalStateException
                textOutput.appendText("success.\n");

                save(modusBuffer);
                drawToDisplay(modusBuffer);
            } catch (IllegalArgumentException | IllegalStateException e) {
                throw new CommandRuntimeException(e.getMessage(), e);
            }
        },
                                             "syntax: capture <item>\n\u2022 captchalogues the item. the item can have " +
                                             "spaces when you type its name. puts in first available spot."));

        commandMap.put("takeOutCard", new Pair<>((args, modusBuffer) -> {
            TextArea textOutput = modusBuffer.getTextOutput();
            if (args.length != 2) throw IllegalSyntaxException.ofArgLength(args.length);

            String indexString = args[0];
            String folderName = args[1];
            textOutput.appendText("Retrieving CARD at index " + indexString + " in folder " + folderName + "...");
            try {
                Integer index = Integer.valueOf(indexString);           //<< may throw NumberFormatException
                Card[] folder = findFolderByName(folderName);           //<< may throw NoSuchElementException
                Card card = takeOutCard(index - 1, folder);      //<< may throw IndexOutOfBoundsException
                // -- card will be not in use if EMPTY, which a client can legally ask for but wont be added to OpenHand
                if (card.getInUse()) modusBuffer.getOpenHand().add(card.getItem());
                textOutput.appendText("success.\n");

                save(modusBuffer);
                drawToDisplay(modusBuffer);
            } catch (NoSuchElementException e) {
                throw new CommandRuntimeException(e.getMessage(), e);
            } catch (NumberFormatException e) {
                throw new CommandRuntimeException(indexString + " is not a number", e);
            } catch (IndexOutOfBoundsException e) {
                throw new CommandRuntimeException(indexString + " is not a valid index", e);
            }
        },
                                                 "syntax: takeOutCard <index>, <folder>\n\u2022 takes out the CARD at " +
                                                 "the index within the folder. index is from 1 to 5."));

        commandMap.put("captureByFolder", new Pair<>((args, modusBuffer) -> {
            TextArea textOutput = modusBuffer.getTextOutput();
            if (args.length != 2) throw IllegalSyntaxException.ofArgLength(args.length);

            String itemName = args[0];
            String folderName = args[1];
            textOutput.appendText("Capturing " + itemName + " and placing into folder " + folderName + "...");
            try {
                Card[] folder = findFolderByName(folderName);   //<< may throw NoSuchElementException
                captureByFolder(itemName, folder, modusBuffer); //<< may throw IllegalArgumentException
                textOutput.appendText("success.\n");

                save(modusBuffer);
                drawToDisplay(modusBuffer);
            } catch (NoSuchElementException | IllegalArgumentException e) {
                throw new CommandRuntimeException(e.getMessage(), e);
            }
        },
                                                     "syntax: captureByFolder <item>, <folder>\n\u2022 captchalogues the item. the item can have " +
                                                     "spaces when you type its name. puts in the specified folder."));

        commandMap.put("takeOutCardByName", new Pair<>((args, modusBuffer) -> {
            TextArea textOutput = modusBuffer.getTextOutput();
            if (args.length != 1) throw IllegalSyntaxException.ofArgLength(args.length);

            String itemName = args[0].toUpperCase();
            textOutput.appendText("Retrieving " + itemName + "...");
            try {
                Card card = takeOutCardByName(itemName);        //<< may throw NoSuchElementException
                // -- card will be not in use if EMPTY, which a client can legally ask for but wont be added to OpenHand
                if (card.getInUse()) modusBuffer.getOpenHand().add(card.getItem());
                textOutput.appendText("success.\n");

                save(modusBuffer);
                drawToDisplay(modusBuffer);
            } catch (NoSuchElementException e) {
                throw new CommandRuntimeException(e.getMessage(), e);
            }
        },
                                                       "syntax: takeOutCardByName <item>\n\u2022 attempts to take out a CARD based on the given " +
                                                       "item name you gave. item can have spaces in its name."));

        commandMap.put("help", new Pair<>((args, modusBuffer) -> {
            TextArea textOutput = modusBuffer.getTextOutput();
            if (args.length == 0) {
                textOutput.appendText(this.METADATA.COMMAND_MAP.desc("help") + "\n");
                return;
            } else if (args.length > 1) {
                textOutput.appendText("help command invoked. disregarding additional args.\n");
            }
            //output the description of the specified command args
            if (this.METADATA.COMMAND_MAP.get(args[0]) != null) {
                System.out.println("Providing modus command help on: " + args[0]);
                textOutput.appendText(this.METADATA.COMMAND_MAP.desc(args[0]) + "\n");
            }
        },
                                          "syntax: help <command>\n\u2022 provides help information about the " +
                                          "given command. syntax is the form you input a complete command. " +
                                          "if a command has multiple arguments they need to be seperated by a comma."));

        commandMap.put(CommandMap.CMD_ERR,
                       new Pair<>((args, modusBuffer) -> modusBuffer.getTextOutput()
                                                                    .appendText("command entered not understood.\n"),
                                  "ERROR"));

        return commandMap;
    }

    //***************************** ACCESS **************************************

    /**
     * @return the METADATA
     */
    public Metadata getMETADATA() {
        return METADATA;
    }

    //**************************** SAVE & LOAD ********************************/
    @Override
    public List<Card> save(ModusBuffer modusBuffer) {
        List<Card> deck = this.toDeck();
        synchronized (modusBuffer.getDeck()) {
            modusBuffer.getDeck().clear();
            modusBuffer.getDeck().addAll(deck);
        }
        return deck;
    }

    @Override
    public void load(ModusBuffer modusBuffer) throws ModusRuntimeException {
        // reset the modus space
        Card freshCard = Card.EMPTY;    //empty CARD
        Arrays.fill(weapons, freshCard);
        Arrays.fill(survival, freshCard);
        Arrays.fill(misc, freshCard);
        Arrays.fill(info, freshCard);
        Arrays.fill(keyCritical, freshCard);

        Optional<String> modusInput = Optional.ofNullable(modusBuffer.getAndResetModusInput());
        //if load was called without modus input then require input and set the redirector back here
        if (!modusInput.isPresent()) {
            modusBuffer.getTextOutput().appendText("Please submit a loading mode number: `1`, `2`, or `3`.\n");
            modusBuffer.setModusInputRedirector(this::load);
            return;
        }

        //Otherwise, try to extract the mode argument and run loadByMode
        String modeString = modusInput.get().trim();
        TextArea textOutput = modusBuffer.getTextOutput();
        int mode;
        textOutput.appendText("Loading from sylladex deck in mode `" + modeString + "`...");
        try {
            mode = Integer.valueOf(modeString);
            if (0 <= mode && mode <= 3) {
                loadByMode(mode, modusBuffer);
                drawToDisplay(modusBuffer);
                textOutput.appendText("success.\n");
            }
        } catch (NumberFormatException e) {
            throw new ModusRuntimeException(modeString + " is not a number", e);
        }
    }

    private void loadByMode(int mode, ModusBuffer modusBuffer) {
        List<Card> deck = modusBuffer.getDeck();
        ///// automatic loading
        if (mode == 1) {
            Card[][] folderArray = {weapons, survival, misc, info, keyCritical};

            //load from the deck based as the pattern
            for (int i = 0; i < 25 && i < deck.size(); i++) {
                Card card = deck.get(i);
                folderArray[i/5][i%5] = card;
            }
            if (deck.size() > 25) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Manual modus deck loading");
                alert.setHeaderText("Sylladex deck is larger than modus deck");
                alert.setContentText("Remaining cards were ignored since they didn't fit into the modus...");
                alert.show();
            }
            ///// manual loading
        } else if (mode == 2) {
            for (Card card : deck) {
                if (card.isValid() && card.getInUse()) {
                    //ask which folder to place the CARD in (or none at all)
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Manual modus deck loading");
                    alert.setHeaderText("Select a folder.");
                    alert.setContentText("Please select a folder to save \"" + card.getItem() + "\" into.");

                    ButtonType buttonF1 = new ButtonType("weapons");
                    ButtonType buttonF2 = new ButtonType("survival");
                    ButtonType buttonF3 = new ButtonType("misc");
                    ButtonType buttonF4 = new ButtonType("info");
                    ButtonType buttonF5 = new ButtonType("keyCritical");
                    ButtonType buttonSkip = new ButtonType("skip");
                    ButtonType buttonCancel = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                    alert.getButtonTypes()
                         .setAll(buttonF1, buttonF2, buttonF3, buttonF4, buttonF5, buttonSkip, buttonCancel);

                    Optional<ButtonType> result = alert.showAndWait();
                    if (!result.isPresent()) {
                        modusBuffer.getTextOutput().appendText("Error in getting your choice. Ending load().");
                        return;
                    }
                    if (result.get() == buttonCancel) {
                        return;
                    } else if (result.get() != buttonSkip) {    //if option then place in the folder
                        captureByFolder(card.getItem(), findFolderByName(result.get().getText()), modusBuffer);
                    }
                }
            }
            ///// fast loading
        } else if (mode == 3) {
            //Continually fill up the the modus space with valid cards from deck until out of cards or space
            for (Card card : deck) if (!(card.isValid() && addCard(card))) break;
        }
    }

    //********************************** IO ***************************************/

    /**
     * Creates a card object from the item and then tries {@link #addCard} with the new card if valid.
     *
     * @param item
     *         the item to create a card from
     * @throws IllegalArgumentException
     *         if the item results in an invalid card
     * @throws IllegalStateException
     *         if there is no available index in the folder to add the new card to
     */
    private void capture(String item) throws IllegalArgumentException, IllegalStateException {
        Card card = new Card(item);
        if (!card.isValid()) throw new IllegalArgumentException(String.format("item '%s' created invalid card", item));
        if (!addCard(card)) throw new IllegalStateException("cannot capture at this time. no free space for item");
    }

    /**
     * Adds a card to the first empty slot found within a folder, in order of array field.
     *
     * @param card
     *         the card to add
     * @return true if the card was added to a folder, false if all folders are full
     */
    private Boolean addCard(Card card) {
        Card[] omniFolder = convertToSingleArray();
        for (int i = 0; i < 25; i++) {
            //add card to the location of the first found empty card
            if (omniFolder[i] == Card.EMPTY) {
                (new Card[][]{weapons, survival, misc, info, keyCritical})[i/5][i%5] = card;
                return true;
            }
        }
        return false;
    }

    /**
     * adds the CARD to the modus through a specific folder
     *
     * @param item
     *         the item to be added
     * @param folder
     *         the Card array to be inserted into
     * @param modusBuffer
     *         the modusBuffer to interface with
     * @throws IllegalArgumentException
     *         if the item creates and invalid card
     */
    private void captureByFolder(String item, Card[] folder, ModusBuffer modusBuffer) throws IllegalArgumentException {
        Card card = new Card(item);
        if (!card.isValid()) throw new IllegalArgumentException(String.format("item '%s' created invalid card", item));

        int index = findFolderSpace(folder);
        if (index == -1) {
            List<String> ejectedItems = explodeFolder(folder).stream().map(Card::getItem).collect(Collectors.toList());
            folder[0] = card;
            modusBuffer.getOpenHand().addAll(ejectedItems);
        } else {
            folder[index] = card;
        }
    }

    /**
     * pops a Card object at index within the folder
     *
     * @param index
     *         the index of the Card array
     * @param folder
     *         the folder to pop the Card object from
     * @return the found Card object
     *
     * @throws IndexOutOfBoundsException
     *         if index is less than zero or greater than 4
     */
    private Card takeOutCard(Integer index, Card[] folder) throws IndexOutOfBoundsException {
        Card result = folder[index];
        folder[index] = Card.EMPTY;
        return result;
    }

    /**
     * Returns the first card that matches the given item name, leaving an empty card in it's place.
     *
     * @param itemName
     *         the item key
     * @return a CARD matching the key
     *
     * @throws NoSuchElementException
     *         if no card was found
     */
    private Card takeOutCardByName(String itemName) throws NoSuchElementException {
        Card[] omniFolder = convertToSingleArray();
        for (int i = 0; i < 25; i++) {
            if (omniFolder[i].getItem().equals(itemName)) {
                //retrieve the card and replace it with an empty card.
                Card result = omniFolder[i];
                (new Card[][]{weapons, survival, misc, info, keyCritical})[i/5][i%5] = Card.EMPTY;
                return result;
            }
        }
        throw new NoSuchElementException("item '" + itemName + "' does not exist");
    }

    //****************************** UTILITY ************************************/

    /**
     * Searches a Card array for the first empty Card and then returns the index of that empty Card. If the entire array
     * is full, then return {@code -1}.
     *
     * @param folder
     *         the Card array to search
     * @return the index, if no spot available then {@code -1}
     */
    private int findFolderSpace(Card[] folder) {
        for (int i = 0; i < 5; i++) {
            if (!folder[i].getInUse()) return i;
        }
        return -1;
    }

    /**
     * "Pops" all the cards in a folder and places them into a temporary deck.
     * <br><br>
     * The temporary deck should be handed off to the sylladex's "open hand" thread to be unraveled by the calling
     * method.
     *
     * @param folder
     *         the Card array to "explode"
     * @return an ArrayList of Card to be given to the Sylladex
     */
    private List<Card> explodeFolder(Card[] folder) {
        List<Card> tempDeck = new ArrayList<>();
        Collections.addAll(tempDeck, folder);
        Arrays.fill(folder, Card.EMPTY);
        return tempDeck;
    }

    /**
     * retrieves the folder Card array based on a string name given to search with.
     *
     * @param givenFolder
     *         the name of the folder to obtain
     * @return the Card array "folder" based on given folder name
     */
    private Card[] findFolderByName(String givenFolder) throws NoSuchElementException {
        HashMap<String, Card[]> folderMap = new HashMap<>();
        folderMap.put("weapons", weapons);
        folderMap.put("survival", survival);
        folderMap.put("misc", misc);
        folderMap.put("info", info);
        folderMap.put("keyCritical", keyCritical);

        return Optional.ofNullable(folderMap.get(givenFolder))
                       .orElseThrow(() -> new NoSuchElementException("'" + givenFolder + "' is not a valid folder"));
    }

    /**
     * merges the 5 folders into a single array.
     *
     * @return a {@code Card[25]} array
     */
    private Card[] convertToSingleArray() {
        Card[] omniFolder = new Card[25];
        System.arraycopy(weapons, 0, omniFolder, 0, 5);
        System.arraycopy(survival, 0, omniFolder, 5, 5);
        System.arraycopy(misc, 0, omniFolder, 10, 5);
        System.arraycopy(info, 0, omniFolder, 15, 5);
        System.arraycopy(keyCritical, 0, omniFolder, 20, 5);
        return omniFolder;
    }

    @Override
    public void drawToDisplay(ModusBuffer modusBuffer) {
        //variable data constants
        StackPane display = modusBuffer.getDisplay();
        double dWidth = display.getMaxWidth();
        double dHeight = display.getMaxHeight();
        CardNode cardExample = CardNode.EMPTY;
        double scaleFactor = 0.5;
        double X_OFFSET = cardExample.CARD_FACE.getMaxWidth() + dWidth/4; //CARD width + padding
        double X_MARGIN = 128;
        double Y_OFFSET = cardExample.CARD_FACE.getMaxHeight() + dHeight/4; //CARD height + padding
        double Y_MARGIN = 4;
        Card[] omnifolder = convertToSingleArray();
        Paint[] folderColors = {Paint.valueOf(String.format("#%06x", Color.RED.getRGB() & 0x00FFFFFF)), Paint.valueOf(
                String.format("#%06x", Color.ORANGE.getRGB() & 0x00FFFFFF)), Paint.valueOf(String.format("#%06x",
                                                                                                         Color.GREEN.getRGB() &
                                                                                                         0x00FFFFFF)), Paint.valueOf(
                String.format("#%06x", Color.BLUE.getRGB() & 0x00FFFFFF)), Paint.valueOf(String.format("#%06x",
                                                                                                       Color.decode(
                                                                                                               "#A030F0")
                                                                                                            .getRGB() &
                                                                                                       0x00FFFFFF))};

        //clear the display and then start adding nodes
        display.getChildren().clear();

        //get list of folder names
        List<Label> folderNames = new ArrayList<>();
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
                //set the coordinates this loop's CARD should be placed at
                double xCardCoord = i*X_OFFSET +
                                    i*X_MARGIN +
                                    (j*15) +
                                    4;    //per-folder offset + margin + per-CARD offset + scalable constant offset
                double yCardCoord = j*Y_OFFSET + j*Y_MARGIN;                //per-CARD offset, margin
                //create the CARD node to draw
                CardNode node = new CardNode(omnifolder[i*5 + j]); //i = folder, j = CARD

                //account for the translation difference caused by scaling from the node's center
                double widthDiff = cardExample.CARD_FACE.getMaxWidth()/2;
                double heightDiff = cardExample.CARD_FACE.getMaxHeight()/2;
                double apparentWidthDifference = widthDiff*scaleFactor - widthDiff;
                double apparentHeightDifference = heightDiff*scaleFactor - heightDiff;
                double finalCoordX = xCardCoord*scaleFactor + apparentWidthDifference;
                double finalCoordY = yCardCoord*scaleFactor + apparentHeightDifference;
                node.CARD_FACE.setTranslateX(finalCoordX);
                node.CARD_FACE.setTranslateY(finalCoordY +
                                             12); //add a non-scalable constant offset for the folder labels
                node.setCardScaleFactor(scaleFactor);
                //create label for the folder column
                if (j == 0) {
                    folderNames.get(i).setTranslateX((xCardCoord - 4)*scaleFactor);
                    display.getChildren().add(folderNames.get(i));
                }

                //re-color the node based on folder
                ((SVGPath) node.CARD_FACE.getChildren().get(1)).setFill(folderColors[i]);

                //draw the node to display
                display.getChildren().add(node.CARD_FACE);
            }
        }
    }

    @Override
    public String toString() {
        return "PentaFile [" +
               "\n\tweapons=" +
               Arrays.toString(weapons) +
               "\n\tsurvival=" +
               Arrays.toString(survival) +
               "\n\tmisc=" +
               Arrays.toString(misc) +
               "\n\tinfo=" +
               Arrays.toString(info) +
               "\n\tkeyCritical=" +
               Arrays.toString(keyCritical) +
               "\n]";
    }

    @Override
    public String description() {
        return "The PentaFile Fetch Modus is designed to simulate a Filing Cabinet.\n" +
               "It comprises 5 folders that each store exactly 5 cards. " +
               "You can store items to a specific folder or retrieve from either " +
               "just the item name or from a folder and index. \n" +
               "the notable quirk of this modus is that if a 6th item is placed into a filled " +
               "folder, the contents of the folder will be ejected to the sylladex and then the " +
               "6th item will be placed into the now empty folder.\n";
    }

    @Override
    public List<Card> toDeck() {
        return Arrays.asList(convertToSingleArray());
    }
}
