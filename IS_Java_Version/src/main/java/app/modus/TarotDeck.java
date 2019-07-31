package app.modus;

import app.model.*;
import app.util.*;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Stream;

/**
 * This fetch app.modus is based on the idea of a deck of cards. The intended usage is to insert item filled Cards into the {@link #deck}
 * and then to retrieve an item, you shuffle the deck and pull a CARD from the top of the deck. The success for retrieving the
 * <i>desired</i> item is fully luck based. Ofcourse, one could cheat and look at each CARD at a time to find their desired item and pull
 * from that index; if cheating <i>is</i> desired then see the extension of this app.modus: {@link TrueSightDeck}.
 *
 * <dt> Notes: </dt> <dd>
 * Functionality for replacing the deck with items of a complete tarot deck can be accomplished with the method: (link method)
 * convertToTarot
 *
 * @author Triston Scallan
 * @version 2.0
 * @see app.model Cards
 * @see TrueSightDeck
 */
@ModusMetatagRunStatus(true)
public class TarotDeck implements Modus {
    private static final Logger      LOGGER      = LogManager.getLogger(TarotDeck.class);
    /**
     * provides information about this app.modus. private so that inherited classes don't clash with this
     */
    protected final      Metadata    METADATA;
    /**
     * Describes how many times {@link #shuffleDeck(ModusBuffer)} will fully shuffle the cards when invoked.
     */
    protected            int         SHUFFLE_VAL = 9;
    /** A Stack based data structure */
    @Nonnull protected   Deque<Card> deck        = new ArrayDeque<>();

    /**
     * Constructor for TarotDeck class
     */
    public TarotDeck() {
        //initialize the METADATA
        this.METADATA = new Metadata(this.getClass().getSimpleName(), this.createFunctionMap(), this);
    }

    //***************************** INITIALIZE ***********************************/

    protected ModusCommandMap createFunctionMap() {
        ModusCommandMap commandMap = new ModusCommandMap(CommandMap.Case.INSENSITIVE);
        commandMap.put("save",
                       (args, modusBuffer) -> {
                           LOGGER.traceEntry("modus lambda: save -> args={}, modusBuffer={}", args, modusBuffer);
                           if (args.length > 0) LOGGER.warn("modus save lambda was given extra arguments!");
                           save(modusBuffer);
                           LOGGER.traceExit();
                       },
                       "syntax: save\n\u2022 saves the current inventory to the sylladex's deck. " +
                       "This command is called at the end of every other command except load.");

        commandMap.put("load",
                       (args, modusBuffer) -> {
                           LOGGER.traceEntry("modus lambda: load -> args={}, modusBuffer={}", args, modusBuffer);
                           if (args.length > 0) LOGGER.warn("modus load lambda was given extra arguments!");
                           load(modusBuffer);
                           LOGGER.traceExit();
                       },
                       "syntax: load\n\u2022 loads the inventory from the sylladex, which may differ." +
                       "\n\u2022 mode `clear` will reset the modus inventory to an empty deck." +
                       "\n\u2022 mode `as-is` will load the inventory, based on card order in the deck, *including* empty cards." +
                       "\n\u2022 mode `non-empty` will load the inventory, based on card order in the deck, *without* any empty cards.");

        commandMap.put("capture",
                       (args, modusBuffer) -> {
                           LOGGER.traceEntry("modus lambda: capture -> args={}, modusBuffer={}", args, modusBuffer);
                           if (args.length != 1) throw LOGGER.throwing(IllegalSyntaxException.ofArgLength(args.length));

                           TextArea textOutput = modusBuffer.getTextOutput();
                           String   itemName   = args[0];
                           textOutput.appendText("Attempting to capture " + itemName + "...");
                           try {
                               capture(args[0]);
                               textOutput.appendText("success.\n");
                           } catch (IllegalArgumentException e) {
                               throw LOGGER.throwing(new CommandRuntimeException(e.getMessage(), e));
                           }
                           save(modusBuffer);
                           drawToDisplay(modusBuffer);
                           LOGGER.traceExit();
                       },
                       "syntax: capture <item>" +
                       "\n\u2022 captchalogues the item. the item can have spaces when you type its name. puts at the top of the deck.");

        commandMap.put("drawCard", (args, modusBuffer) -> {
            LOGGER.traceEntry("modus lambda: drawCard -> args={}, modusBuffer={}", args, modusBuffer);
            TextArea textOutput = modusBuffer.getTextOutput();
            if (this.deck.isEmpty()) {
                textOutput.appendText("Deck is empty...nothing to draw from!\n");
                LOGGER.traceExit("exit without doing anything...empty deck");
                return;
            }
            textOutput.appendText("Shuffling deck...");
            textOutput.appendText(shuffleDeck(modusBuffer) ? "success.\n" : "not enough cards to shuffle.\n");

            Card drawnCard = this.deck.pop();
            if (drawnCard.isInUse()) {
                modusBuffer.getOpenHand().add(drawnCard.getItem());
                textOutput.appendText("Retrieved item " + drawnCard.getItem() + " from card.\n");
            }

            save(modusBuffer);
            drawToDisplay(modusBuffer);
            LOGGER.traceExit("drawn card={}", drawnCard);
        }, "syntax: drawCard\n\u2022 shuffles the deck " + this.SHUFFLE_VAL + " times, then draws the top card from the deck.");

        commandMap.put("shuffle", (args, modusBuffer) -> {
            LOGGER.traceEntry("modus lambda: shuffle -> args={}, modusBuffer={}", args, modusBuffer);
            if (args.length > 0) LOGGER.warn("modus shuffle lambda was given extra arguments!");
            TextArea textOutput = modusBuffer.getTextOutput();
            textOutput.appendText("Shuffling deck...");
            if (!this.shuffleDeck(modusBuffer)) {
                textOutput.appendText("not enough cards to shuffle.\n");
                LOGGER.traceExit("exit without doing anything...shuffle failed.");
                return;
            }
            textOutput.appendText("success.\n");

            save(modusBuffer);
            drawToDisplay(modusBuffer);
            LOGGER.traceExit();
        }, "syntax: shuffle\\n\\u2022 shuffles the deck " + this.SHUFFLE_VAL + " times.");
        return commandMap;
    }

    //***************************** ACCESS *************************************/

    /**
     * @return the METADATA
     */
    public Metadata getMETADATA() {
        return METADATA;
    }

    //**************************** SAVE & LOAD ********************************/
    @Override
    public List<Card> save(ModusBuffer modusBuffer) {
        List<Card> modusSourceDeck = this.toDeck();
        List<Card> syllTargetDeck  = modusBuffer.getDeck();
        syllTargetDeck.clear();
        syllTargetDeck.addAll(modusSourceDeck);
        return modusSourceDeck;
    }

    @Override
    public void load(ModusBuffer modusBuffer) {
        modusBuffer.getTextOutput().appendText("Please submit a loading mode: `clear`, `as-is`, or `no-empty`.\n");
        modusBuffer.setInputRedirector(this::loadByMode);
    }

    protected void loadByMode(ModusBuffer modusBuffer) {
        String       rawInput         = modusBuffer.getAndResetModusInput();
        String       cleanedInput     = rawInput.trim().replace("`", "");
        Stream<Card> deckToLoadStream = modusBuffer.getDeck().stream();
        /*
         * manipulate the stream according to our desired result.
         * the cases cascade since they can build off each other, last case must always break.
         * default case should return and report 'no changes made' as no matching mode was found.
         */
        switch (cleanedInput) {
            case "clear":
                deckToLoadStream = Stream.empty();
            case "no-empty":
                deckToLoadStream = deckToLoadStream.filter(Card::isInUse);
            case "as-is":
                deckToLoadStream = deckToLoadStream.filter(Card::isValid);
                break;
            default:
                modusBuffer.getTextOutput().appendText("No matching mode found from given input: " + rawInput);
                return;
        }
        this.deck = deckToLoadStream.collect(ArrayDeque::new, ArrayDeque::push, ArrayDeque::addAll);
        drawToDisplay(modusBuffer);
    }

    //******************************** IO ***************************************/

    private void capture(String itemName) throws IllegalArgumentException {
        deck.push(new Card(itemName));
    }


    //****************************** UTILITY ************************************/

    /**
     * Shuffles the deck fully {@link #SHUFFLE_VAL} times. Creates two temporary decks, respectively holding the top half and lower half of
     * {@link #deck this.deck}, then randomly draws from either temporary deck to rebuild this.deck much like a real-world riffle shuffle.
     *
     * @return False if deck has less than 2 cards (it will also return without any changes made since no permutations can be made). True,
     *         otherwise.
     *
     * @implNote assumes that the deck is non-null and modifiable. Short circuits by checking if enough cards exist for a shuffle to
     *         make any changes in card order. Riffle shuffle implementation is used to keep in the spirit of simulating real world
     *         storage.
     */
    protected boolean shuffleDeck(ModusBuffer modusBuffer) {
        //test if the deck is 1 or less, if so, then leave deck unchanged.
        if (deck.size() < 2) return LOGGER.traceExit("Deck size too low to shuffle. returning={}", Boolean.FALSE);

        //shuffle the deck
        for (int loop = 0; loop < SHUFFLE_VAL; loop++) {
            //split this deck into two sides, then randomly re-stack from the top of either side.
            Stack<Card> leftSide  = new Stack<>();
            Stack<Card> rightSide = new Stack<>();
            int         a         = (deck.size()/2);    //split the deck in half
            int         b         = deck.size() - a;        //give the rest to rightSide
            for (int i = 0; i < a + b; i++) {
                //if we're on the top half of the deck, pop+push to leftSide
                if (i < a) leftSide.push(deck.pop());
                    //if we're on the bottom half of the deck, pop+push to rightSide
                else rightSide.push(deck.pop());
            }

            assert deck.isEmpty();

            //use rng to determine if it should pull from the left or right hand deck
            // int      r; //rng int of either 0 or 1
            Random   rand       = new Random();
            TextArea textOutput = modusBuffer.getTextOutput();
            while (!leftSide.isEmpty() || !rightSide.isEmpty()) //run until both decks are empty
            {
                //randomly take from the top of the two decks and push to the new deck.
                int pOfLeft = (int) ((leftSide.size()/(leftSide.size() + rightSide.size() + 0.0))*100 + 0.5);
                if (!leftSide.isEmpty() && (rand.nextInt(100) < pOfLeft || rightSide.isEmpty())) {
                    deck.push(leftSide.pop());
                    textOutput.appendText("/");
                } else {
                    deck.push(rightSide.pop());
                    textOutput.appendText("\\");
                }
            }
            textOutput.appendText("_");
        }
        return true;
    }

    @Override
    public void drawToDisplay(ModusBuffer modusBuffer) {
        //EXPECTED FINAL END RESULT: all backs of cards from deck displayed from first to last - farthest to closest, each slightly offset

        //variable data constants
        Pane     display           = modusBuffer.getDisplay();
        double   dWidth            = display.getWidth();
        double   dHeight           = display.getHeight();
        CardNode cardExample       = CardNode.EMPTY;
        int      N_CARDS           = deck.size();
        double   SCALE_FACTOR      = (dHeight > 520) ? 0.5 : 0.3; // if dHeight is <520, reduce scale factor again.
        double   NTH_CARD_OFFSET_X = SCALE_FACTOR*cardExample.CARD_BACK.getMaxWidth()/5;
        double   NTH_CARD_OFFSET_Y = SCALE_FACTOR*cardExample.CARD_BACK.getMaxHeight()/5;
        double   X_TRANSLATE       = 4; //translate rightward for left hand 'margin'; considered un-scale-able
        double   Y_TRANSLATE       = 4; //translate downward for top hand 'margin'; considered un-scale-able
        double SQUISH_ADJUST_X = (N_CARDS < 2 || NTH_CARD_OFFSET_X*N_CARDS <= dWidth - X_TRANSLATE*2)
                                 ? 0
                                 : ((cardExample.CARD_BACK.getMaxWidth() - NTH_CARD_OFFSET_X) +
                                    NTH_CARD_OFFSET_X*(N_CARDS - 1) +
                                    X_TRANSLATE*2 - dWidth)/N_CARDS;
        double SQUISH_ADJUST_Y = (N_CARDS < 2 || NTH_CARD_OFFSET_Y*N_CARDS <= dHeight - Y_TRANSLATE*2)
                                 ? 0
                                 : ((cardExample.CARD_BACK.getMaxHeight() - NTH_CARD_OFFSET_Y) +
                                    NTH_CARD_OFFSET_Y*(N_CARDS - 1) +
                                    Y_TRANSLATE*2 - dWidth)/N_CARDS;

        GridPane[]     deckAsNodes  = new GridPane[deck.size()];
        int            index        = 0;
        Iterator<Card> deckIterator = deck.descendingIterator();
        while (deckIterator.hasNext()) {
            Card   card       = deckIterator.next();
            double xCardCoord = index*(NTH_CARD_OFFSET_X - SQUISH_ADJUST_X);
            double yCardCoord = index*(NTH_CARD_OFFSET_Y - SQUISH_ADJUST_Y);

            //create the CARD node to draw
            CardNode node = new CardNode(card);

            //account for the translation difference caused by scaling from the node's center
            double widthDiff                = cardExample.CARD_BACK.getMaxWidth()/2; //radius from node to x-planar edge
            double heightDiff               = cardExample.CARD_BACK.getMaxHeight()/2; //radius from node to y-planar edge
            double apparentWidthDifference  = widthDiff*SCALE_FACTOR - widthDiff;
            double apparentHeightDifference = heightDiff*SCALE_FACTOR - heightDiff;
            double adjustedCardCoordX       = xCardCoord + apparentWidthDifference;
            double adjustedCardCoordY       = yCardCoord + apparentHeightDifference;
            node.CARD_FACE.setTranslateX(adjustedCardCoordX + X_TRANSLATE);
            node.CARD_FACE.setTranslateY(adjustedCardCoordY + Y_TRANSLATE);
            node.CARD_BACK.setTranslateX(adjustedCardCoordX + X_TRANSLATE);
            node.CARD_BACK.setTranslateY(adjustedCardCoordY + Y_TRANSLATE);
            node.setCardScaleFactor(SCALE_FACTOR);

            deckAsNodes[index] = (index + 1 < deck.size()) ? node.CARD_BACK : node.CARD_FACE;
            index++;
        }

        display.getChildren().setAll(deckAsNodes);
    }

    @Override
    public String description() {
        return "The TarotDeck Fetch Modus is designed to simulate a mystical deck of cards.\n" +
               "The deck may start with no cards and will fill and lose cards as you capture and draw cards, respectively. " +
               "Capturing items will place them at the top of the deck. \n" +
               "The notable quirk of this modus is that the deck must be shuffled before you draw a card, " +
               "and you must draw only from the top of the deck. \n" +
               "The deck will be shuffled " +
               SHUFFLE_VAL +
               " times with the riffle shuffle for each time it is to be shuffled.";
    }

    @Override
    public List<Card> toDeck() {
        return new ArrayList<>(deck);
    }
}
