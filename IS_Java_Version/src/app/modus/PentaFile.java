package app.modus;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import app.model.*;
import app.util.CommandMap;
import app.util.ModusCommandMap;
import commandline_utils.Searcher;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.util.Pair;

/**
 * This fetch app.modus is the PentaFile pfModus, a app.modus designed for use with a sylladex. Using a structure comprised of 5
 * arrays containing 5 cards, and busting if a single array is overfilled. <br> This is likened to a File Cabinet. 5
 * folders that can hold 5 files each.
 *
 * @author Triston Scallan
 *         <dt> Note: </dt> <dd>
 *         1. The inventory only holds 25 cards, 5 cards in 5 folders. <br> 2. If 6 items are placed into a folder, 5
 *         are ejected and the 6th is pushed <br> 3. The current PentaFile can have all its information saved and loaded
 *         to a text file. <br>
 *         </dd>
 */
@ModusMetatagRunStatus(true)
public class PentaFile implements Modus {
    /**
     * provides information about this app.modus
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

        //attempt to initialize the app.modus space
        Card card = new Card();    //empty CARD
        Arrays.fill(weapons, card);
        Arrays.fill(survival, card);
        Arrays.fill(misc, card);
        Arrays.fill(info, card);
        Arrays.fill(keyCritical, card);
    }

    private ModusCommandMap createFunctionMap() {
        ModusCommandMap commandMap = new ModusCommandMap(CommandMap.Case.SENSITIVE);

        commandMap.put("save", new Pair<>((args, modusBuffer) -> {
            synchronized (modusBuffer.getDeck()) {
                modusBuffer.getDeck().clear();
                modusBuffer.getDeck().addAll(save());
            }
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
                                  "\n\u2022 mode 3 will fast load the inventory. disregards saved CARD positions.")); //mode = 0, 1, 2, or 3

        commandMap.put("capture", new Pair<>((args, modusBuffer) -> {
            TextArea textOutput = modusBuffer.getTextOutput();
            if (args.length == 1) {
                textOutput.appendText("Attempting to capture " + args[0] + "...");
                if (!capture(args[0])) {
                    textOutput.appendText("ERROR: captured card invalid.\n");
                    return;
                }
                textOutput.appendText("success.\n");
                synchronized (modusBuffer.getDeck()) {
                    modusBuffer.getDeck().clear();
                    modusBuffer.getDeck().addAll(save());
                }
                drawToDisplay(modusBuffer);
                return;
            }
            textOutput.appendText("ERROR-\n" + this.METADATA.COMMAND_MAP.desc("capture") + "\n");
        },
                                             "syntax: capture <item>\n\u2022 captchalogues the item. the item can have " +
                                             "spaces when you type its name. puts in first available spot."));

        commandMap.put("takeOutCard", new Pair<>((args, modusBuffer) -> {
            TextArea textOutput = modusBuffer.getTextOutput();
            if (args.length == 2) {
                Card[] folder = findFolderByName(args[1]);
                Integer index = -1;
                try {
                    index = Integer.valueOf(args[0]);
                } catch (NumberFormatException e) {
                    //do nothing...
                }
                if (index >= 1 && index <= 5) {
                    textOutput.appendText("Retrieving CARD at index " + args[0] + " in folder " + args[1] + "...");
                    Card card = takeOutCard(index - 1, folder);
                    textOutput.appendText("success.\n");
                    synchronized (modusBuffer.getDeck()) {
                        modusBuffer.getDeck().clear();
                        modusBuffer.getDeck().addAll(save());
                    }
                    drawToDisplay(modusBuffer);
                    //return a non-empty CARD to hand, but its not an error if it was empty.
                    if (card.getInUse()) modusBuffer.getOpenHand().add(card.getItem());
                    return;
                }
                textOutput.appendText("ERROR: " + args[0] + " is not a valid index.\n");
            }
            textOutput.appendText("ERROR-\n" + this.METADATA.COMMAND_MAP.desc("takeOutCard") + "\n");
        },
                                                 "syntax: takeOutCard <index>, <folder>\n\u2022 takes out the CARD at " +
                                                 "the index within the folder. index is from 1 to 5."));

        commandMap.put("captureByFolder", new Pair<>((args, modusBuffer) -> {
            TextArea textOutput = modusBuffer.getTextOutput();
            if (args.length == 2) {
                Card[] folder = findFolderByName(args[1]);
                textOutput.appendText("Capturing " + args[0] + " and placing into folder " + args[1] + "...");
                if (!captureByFolder(args[0], folder, modusBuffer)) {
                    textOutput.appendText("ERROR: captured card invalid.\n");
                    return;
                }
                textOutput.appendText("success.\n");
                synchronized (modusBuffer.getDeck()) {
                    modusBuffer.getDeck().clear();
                    modusBuffer.getDeck().addAll(save());
                }
                drawToDisplay(modusBuffer);
                return;
            }
            textOutput.appendText("ERROR-\n" + this.METADATA.COMMAND_MAP.desc("captureByFolder") + "\n");
        },
                                                     "syntax: captureByFolder <item>, <folder>\n\u2022 captchalogues the item. the item can have " +
                                                     "spaces when you type its name. puts in the specified folder."));

        commandMap.put("takeOutCardByName", new Pair<>((args, modusBuffer) -> {
            TextArea textOutput = modusBuffer.getTextOutput();
            if (args.length == 1) {
                textOutput.appendText("Retrieving " + args[0] + "...");
                Card card = takeOutCardByName(args[0]);
                if (card.getInUse()) {
                    modusBuffer.getOpenHand().add(card.getItem());
                    textOutput.appendText("success.\n");
                    synchronized (modusBuffer.getDeck()) {
                        modusBuffer.getDeck().clear();
                        modusBuffer.getDeck().addAll(save());
                    }
                    drawToDisplay(modusBuffer);
                } else textOutput.appendText("CARD `" + args[0] + "` either doesn't exist or match failed.\n");
                return;
            }
            textOutput.appendText("ERROR-\n" + this.METADATA.COMMAND_MAP.desc("takeOutCardByName") + "\n");
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
                System.out.println("Providing app.modus command help on: " + args[0]);
                textOutput.appendText(this.METADATA.COMMAND_MAP.desc(args[0]) + "\n");
            } else {
                textOutput.appendText("command entered not understood.\n");
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
    /* (non-Javadoc)
     * @see app.modus.Modus#save()
     */
    @Override
    public List<Card> save() {
        return Arrays.asList(convertToDeck());
    }

    /* (non-Javadoc)
     * @see app.modus.Modus#save()
     */
    @Override
    public void load(ModusBuffer modusBuffer) {
        // reset the app.modus space
        Card freshCard = Card.EMPTY;    //empty CARD
        Arrays.fill(weapons, freshCard);
        Arrays.fill(survival, freshCard);
        Arrays.fill(misc, freshCard);
        Arrays.fill(info, freshCard);
        Arrays.fill(keyCritical, freshCard);

        Optional<String> modusInput = Optional.ofNullable(modusBuffer.getAndResetModusInput());
        //if load was called without app.modus input then require input and set the redirector back here
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
            textOutput.appendText("ERROR-NOT A NUMBER\n" + this.METADATA.COMMAND_MAP.desc("load") + "\n");
        }
    }

    private void loadByMode(int mode, ModusBuffer modusBuffer) {
        List<Card> deck = modusBuffer.getDeck();
        ///// automatic loading
        if (mode == 1) {
            //load from the deck based as the pattern
            for (int i = 0; i < 25 && i < deck.size(); i++) {
                Card card = deck.get(i);
                if (i < 5) weapons[i] = card;
                else if (i < 10) survival[i%5] = card;
                else if (i < 15) misc[i%5] = card;
                else if (i < 20) info[i%5] = card;
                else keyCritical[i%5] = card;
            }
            if (deck.size() > 25) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Manual app.modus deck loading");
                alert.setHeaderText("Sylladex deck is larger than app.modus deck");
                alert.setContentText("Remaining cards were ignored since they didn't fit into the app.modus...");
            }
            ///// manual loading
        } else if (mode == 2) {
            for (Card card : deck) {
                if (card.validateCard() ? card.getInUse() : false) {    //TODO: refactor logic of when validation occurs

                    //ask which folder to place the CARD in (or none at all)
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Manual app.modus deck loading");
                    alert.setHeaderText("Select a folder.");
                    alert.setContentText("Please select a folder to save \"" + card.getItem() + "\" into.");

                    //create buttons for alert
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
                    //if cancel, return, otherwise change deckAction and continue
                    Card[] folder;
                    if (!result.isPresent()) {    //if error
                        modusBuffer.getTextOutput().appendText("Error in getting your choice. Ending load().");
                        return;
                    }
                    if (result.get() == buttonSkip) {    //if skip then continue
                        continue;
                    } else if (result.get() != buttonCancel) {    //if option then fill the folder variable
                        folder = findFolderByName(result.get().getText());
                    } else {    //if cancel then return
                        return;
                    }

                    //place it in the folder
                    if (!captureByFolder(card.getItem(), folder, null)) throw new IllegalStateException();
                }
            }
            ///// fast loading
        } else if (mode == 3) {
            //Continually fill up the the app.modus space with cards until
            for (Card card : deck) {
                if (!((card.validateCard()) ? addCard(card) : addCard(new Card()))) break;
            }
        }
    }

    //********************************** IO ***************************************/
    private Boolean capture(String item) {
        Card card = new Card(item);
        //if invalid CARD
        if (!card.validateCard()) return false;
        //this call will not cause the side effect as described by Note #2
        return addCard(card);
    }

    private Boolean addCard(Card card) {
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
        } else return false;
    }

    /**
     * adds the CARD to the app.modus through a specific folder
     *
     * @param item
     *         the item to be added
     * @param folder
     *         the Card array to be inserted into
     * @return {@code true} if successful, {@code false} otherwise
     */
    private Boolean captureByFolder(String item,
                                    Card[] folder,
                                    ModusBuffer modusBuffer) { //TODO: Replace boolean return values for exception throwing
        int index;
        Card card = new Card(item);
        //if invalid CARD
        if (!card.validateCard()) return false;

        if (findFolderSpace(folder) == -1) {
            List<String> ejectedItems = explodeFolder(folder).stream().map(Card::getItem).collect(Collectors.toList());
            folder[0] = card;
            //hand off ejectedItems to modusBuffer
            modusBuffer.getOpenHand().addAll(ejectedItems);
            return true;
        } else if ((index = findFolderSpace(folder)) != -1 && index < 5) {
            folder[index] = card;
            return true;
        } else {
            return false;
        }
    }

    /**
     * <p>parameter is Objects{Integer index, Card[] folder}
     */
    private Card takeOutCard(Object... objects) {
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
     * Uses the name of an item as a key to search for it's CARD. Because it calls {@link #findItemName(String)}, it may
     * have undesired affects if the name given is misspelled. The function will attempt to get the closest match, but
     * an exact match is not guaranteed.
     * <p> If the app.modus space is empty, this function will short circuit
     * and return an "empty" Card.
     *
     * @param itemName
     *         the item key
     * @return a CARD matching the key
     */
    private Card takeOutCardByName(String itemName) {
        if (isEmpty()) return new Card();
        String match = findItemName(itemName);
        Card[] omniFolder = convertToDeck();
        int i = 0;
        for (Card card : omniFolder) {
            if (card.getItem().equals(match)) {
                int index = i%5;
                Card[] folder = findFolderFromOmniIndex(i);
                folder[index] = new Card();
                return card;
            }
            i++;
        }
        return new Card();
    }

    //****************************** UTILITY ************************************/

    private Boolean isEmpty() {
        for (Card card : weapons) {
            if (card.getInUse()) return false;
        }
        for (Card card : survival) {
            if (card.getInUse()) return false;
        }
        for (Card card : misc) {
            if (card.getInUse()) return false;
        }
        for (Card card : info) {
            if (card.getInUse()) return false;
        }
        for (Card card : keyCritical) {
            if (card.getInUse()) return false;
        }
        return true;
    }

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
     * <p>identical to calling {@link commandline_utils.Searcher#fuzzyStringSearch fuzzyStringSearch} using a list of
     * the folder names and the input, respectively.
     *
     * @param givenFolder
     *         the name of the folder to obtain
     * @return the Card array "folder" based on given folder name
     */
    private Card[] findFolderByName(String givenFolder) {
        List<String> folderList = new ArrayList<>();
        folderList.add("weapons");
        folderList.add("survival");
        folderList.add("misc");
        folderList.add("info");
        folderList.add("keyCritical");

        int i = Searcher.fuzzyStringSearch(givenFolder, folderList).getKey();
        if (i == -1) {
            System.out.println("folder requested wasn't found. returning `weapons` folder as default.");
            return weapons;
        }
        return ((i == 0) ? weapons : ((i == 1) ? survival : ((i == 2) ? misc : ((i == 3) ? info : keyCritical))));
    }

    /**
     * returns a folder based off of an index that relates to a position in a omni-folder
     *
     * @param i
     *         the index to use
     * @return a Card[]
     *
     * @see #convertToDeck()
     */
    private Card[] findFolderFromOmniIndex(int i) {
        i = i/5; //collapse the index into the 5 buckets
        return ((i == 0) ? weapons : ((i == 1) ? survival : ((i == 2) ? misc : ((i == 3) ? info : keyCritical))));
    }

    private String findItemName(String givenItem) {
        Card[] omniFolder = convertToDeck();
        //iterate through the array and scrape the item names into a list
        List<String> itemList = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            itemList.add(omniFolder[i].getItem());
        }

        //perform a fuzzy string search
        return Searcher.fuzzyStringSearch(givenItem, itemList)
                       .getValue(); //TODO: remove fuzzyStringSearch from app.modus classes
    }

    /**
     * merges the 5 folders into a single array.
     *
     * @return a {@code Card[25]} array
     */
    private Card[] convertToDeck() {
        Card[] omniFolder = new Card[25];
        System.arraycopy(weapons, 0, omniFolder, 0, 5);
        System.arraycopy(survival, 0, omniFolder, 5, 5);
        System.arraycopy(misc, 0, omniFolder, 10, 5);
        System.arraycopy(info, 0, omniFolder, 15, 5);
        System.arraycopy(keyCritical, 0, omniFolder, 20, 5);
        return omniFolder;
    }

    /* (non-Javadoc)
     * @see app.modus.Modus#drawToDisplay()
     */
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
        Card[] omnifolder = convertToDeck();
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

        //loop of 5 folders within the app.modus
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
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

    /* (non-Javadoc)
     * @see app.modus.Modus#description()
     */
    @Override
    public String description() {
        return "The PentaFile Fetch Modus is designed to simulate a Filing Cabinet.\n" +
               "It comprises 5 folders that each store exactly 5 cards. " +
               "You can store items to a specific folder or retrieve from either " +
               "just the item name or from a folder and index. \n" +
               "the notable quirk of this app.modus is that if a 6th item is placed into a filled " +
               "folder, the contents of the folder will be ejected to the sylladex and then the " +
               "6th item will be placed into the now empty folder.\n";
    }
}
