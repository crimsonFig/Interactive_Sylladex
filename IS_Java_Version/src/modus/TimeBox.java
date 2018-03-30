  package modus;

import java.util.*;
import java.util.stream.*;

import app.controller.Sylladex;
import app.model.*;
import javafx.scene.control.TextArea;

/**
 * The time box is a safe-like container with an inside that has no anchor to
 *   the current time, allowing items placed inside to shift back and forth
 *   through time. From the user's perception, opening the safe causes the
 *   inside to present whatever item now exists at that time. items may appear
 *   before they were placed in, or appear a long time after it was placed
 *   within, if the safe is opened and nothing is inside then it is at a state
 *   when all items were removed and nothing was ever put in.
 * @author Triston Scallan
 *	<dt> Notes: </dt>
 *	<dd> 
 *	Due to the nature of the timeBox existing seperate to timelines, {@link #load() loading}
 *      with this method will be considered as opening the safe, putting a
 *      card into the safe, and then closing the safe, repeated until all
 *      cards from the save file are exhausted (with each item querying for a
 *      size of a given item). {@link #save() Saving} will strip the size and temporal data
 *      off of the items, thus loading directly after saving is considered as
 *      using a new safe to replace the old safe.
 *  		<br>
 *  Any item placed in will have an absolute timestamp of when it was placed
 *      within, and a relative timestamp to allow for shifts within it's
 *      inner dimensional time. every time the door is opened, all entities
 *      will have their relative timestamp changed as they have now been
 *      observed and must decide at what times does it exist in relation to
 *      the real world (if it lands on a time before the current one, then it
 *      exists in a closed or opened safe of a parrelel world). if the
 *      current time overlaps the relative time + absolute timestamp of a
 *      given item, it may appear within the box for the user to interact
 *      with. several items may appear in the box at once, but each item are
 *      considered uniquely different if they have a different absolute time.
 *      items placed together at the same time are considered as one entity
 *      now, but a given entity may be several items. Each item would still
 *      have their own card, but an entity would exist as a linked list of
 *      cards. an entity may be a single card of a linked list, or several
 *      cards chained together, in an acycled fashion. opening the safe also
 *      breaks the entity up into single cards, breaking up any multitude of
 *      seperate entities. Closing the safe updates the current items that
 *      was inside at the moment of closing so that their absolute time
 *      reflects the new current time (and thus combining those item into a
 *      single entitity within time). This creates a side effect of items
 *      converging temporally together until all items always appear together
 *      simultaneously.  an item's relative timestamp will appear as a span
 *      of an hour, so an overlap (and the subsequent given access) is when
 *      the relative timeshift/timestamp coincides within an hour of the
 *      current time. 
 *      <br>
 *  The only limit to how many items can be placed within the box is based
        only on the "largeness" of an item. you can only close the box if the
        current items within the box does not exceed the safe's spacial
        threshold (if it all fits in the safe, its good). An anomoly can
        occur if several items overlap at a single point in time, where
        opening the box results in the accessable items to exceed the safe's
        threshold; in this case, two random items will converge into a single
        item and being reduced to the size of the largest of the two. this
        convergence will continue until all accessable items are within the
        safe's threshold. The largeness of items will be tagged in the nodes,
        with the nodes always belonging to a given entity.
        <br>
 *  This paragragh will briefly describe the properties and nature of the
        temoral dimension within the safe. Each entity will exist on its own
        temporal timeline, so two seperate entities will be independent in
        their relativeTime shifts. the temporal dimension within the safe is
        also about the number of entities within the safe, about n * 5 hours,
        so that opening the safe usually results in a 1/5 chance of finding
        something within the safe, changing this number will change the
        chances. this gives n*5 hours of time for the items within to shift
        within relatively, as in, even if the absolute time of an item is 20
        hours ago, it's relative time will always land it somewhere in the n*5
        hours boundry (and the center of this boundry is the present time, so
        e.g. 2.5 hours into the past, 2.5 hours into the future, with the
        entitiy appearing in the present if it exists in our safe within .5
        hour of the past or .5 hour of the future). Lore wise, if an item's
        relative shift lands it in the future or the past when the safe door
        is open, it is considered to be true at that time in some given
        universe/dimension, not specifically our universe/dimension. it is
        only in our universe for certain if the time shift lands it in our
        temporally present window of conciousness (which leads to various
        phylosophical ideas about conciousness over time when compared to
        other's perspectives.) 
        </dd>
 */
public class TimeBox implements Modus {
	/**
	 * A reference to the Sylladex that called the given modus. <br>
	 * This is used to pass information back to the caller.
	 */
	private Sylladex sylladexReference;
	/**
	 * provides information about this modus
	 */
	protected final Metadata METADATA;
	/**
	 * Collection of all Timeline held by TimeBox. Should be a set due to 
	 * duplicate timelines being a paradox.
	 */
	private Set<Timeline> timelines = new HashSet<Timeline>();
	/**
	 * The total number of slots within the timeline. This would be is 
	 * the "length" of the timeline, and therefore is 0 to this value
	 * where 0 is inclusive and the value is exclusive.
	 */
	private final static int TIMELINE_SIZE = 25;
	/**
	 * The safe that holds all cards that exist in the present moment while the safe is open.
	 */
	private List<Card> timeBox;
	/**
	 * tracks whether the timeBox is "opened"(true) or "closed"(false)
	 */
	private Boolean boxState;
	/**
	 * Represents the temporal offset the box considers itself in. 
	 * This value is used in determining when a timeline is in sync with the box in
	 * order for that timeline's cards to appear in the box when the box is opened. 
	 * i.e. if boxChronalState == timelineObject#slot when 
	 * Timeline.chronoCollapse(boxChronalState) is invoked to actually get it's cards.
	 * <p>
	 * The default value is TIMELINE_SIZE/2 (a.k.a. "present time"). 
	 */
	private int boxChronalState = TIMELINE_SIZE / 2;
	
	
	/**
	 * Class to be used by the TimeBox class.
	 * Metaphorically represents a timeline in which a collection of 
	 * Card exist in.
	 * <p> A timeline has incremental slots in which the collection will 
	 * occupy and act as the nth temporal increment into the future or past.
	 * For example, if the collection exists in slot n and there are m total slots
	 * then the collection would metaphorically exist (n - m/2) minutes into the future.
	 * 
	 * @author Triston Scallan
	 *
	 */
	class Timeline {
		//the range of temporal slots. starts from 0 up to TIMELINE_SIZE (exclusive).
		private final int range = TIMELINE_SIZE;
		//the collection of items within this timeline
		private final List<Card> timelineDeck;
		//the absolute temporal slot that the collection exists on.
		private int slot = -1;
		
		Timeline(List<Card> deck) {
			timelineDeck = deck;
			chronoShift();
		}
		
		Optional<List<Card>> chronoCollapse(int chosenSlot) {
			return Optional.ofNullable((this.slot == chosenSlot) ? timelineDeck : null);
		}
		
		void chronoShift() {
			slot = new Random().nextInt(range);
		}
		
		//the temporally relative slot. i.e. how many slots into the future or past.
		int getTimeOffset() {
			return slot - range / 2;
		}

		//Make sure that a timeline set only considers the range and deck (the distinguishing keys)
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + range;
			result = prime * result + ((timelineDeck == null) ? 0 : timelineDeck.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Timeline other = (Timeline) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (range != other.range)
				return false;
			if (timelineDeck == null) {
				if (other.timelineDeck != null)
					return false;
			} else if (!timelineDeck.equals(other.timelineDeck))
				return false;
			return true;
		}

		private TimeBox getOuterType() {
			return TimeBox.this;
		}
				
	}
	
	
	
	//***************************** INITIALIZE *********************************/
	/**
	 * The constructor of a fetch Modus should save the reference to the sylladex
	 * 	so that it can functionally return a list of the Modus' functionality to
	 * 	the ModusManager, specifically passing modusMetadata.
	 * @param sylladex a reference to the caller, a Sylladex
	 */
	public TimeBox(Sylladex sylladex) {
		this.sylladexReference = sylladex;
		
		//initialize the METADATA
		this.METADATA = new Metadata(this.getClass().getSimpleName(), this.createFunctionMap(), this);
	}
	
	/* (non-Javadoc)
	 * @see modus.Modus#createFunctionMap()
	 */
	@Override
	public LinkedHashMap<String, Integer> createFunctionMap() {
		LinkedHashMap<String, Integer> functionMap = new LinkedHashMap<String, Integer>();
		functionMap.put("save", 1);
		functionMap.put("load #", 2); //mode = 0, 1, 2, or 3
		functionMap.put("capture", 3);
		functionMap.put("takeOutCard", 4);
		functionMap.put("openBox", 5);
		functionMap.put("closeBox", 6);
		return functionMap;
	}
	
	//***************************** ACCESS *************************************/
	/* (non-Javadoc)
	 * @see modus.Modus#entry(int, java.lang.Object[])
	 */
	@Override
	public String entry(int functionCode, String... args) {
		TextArea textOutput = sylladexReference.getTextOutput();
		switch (functionCode) {
		case 1: //save
			save();
			textOutput.appendText("Deck was saved to sylladex.\n");
			break;
		case 2: //load <mode>
			//based on mode as objects[0], use that load mode. if doesn't match 0, 1, or 2 then invoke entry(-1, "command name") to display help to the output
			if (args.length == 1) {
				int mode;
				textOutput.appendText("Loading from sylladex deck in mode "+ args[0] +"...");
				try {
					mode = Integer.valueOf(args[0]);
					if (0 <= mode && mode <= 2) {
						load(mode);
						drawToDisplay();
						textOutput.appendText("success.\n");
						return "0";
					}
				} catch (NumberFormatException e) {
					//do nothing, cascade to fail state.
				}
			} 
			entry(0, "load");
			return "-1";
		case 3: //capture
			if (args.length == 1) {
				textOutput.appendText("Attempting to capture " + args[0] + "...");
				sylladexReference.removeFromHand(args[0]);
				if(! capture(args[0])) return "-1";
				textOutput.appendText("success.\n");
				save();
				drawToDisplay();
				return "0";
			} 
			entry(0, "capture");
			return "-1";
		case 4: //takeOutCard
			if (args.length == 1) {
				textOutput.appendText("Retrieving " + args[0] + "...");
				Card card = takeOutCard(args[0]);
				if (card.getInUse()) { 
					sylladexReference.addToOpenHand(Arrays.asList(card));
					textOutput.appendText("success.\n");
					save();
					drawToDisplay();
				}
				else textOutput.appendText("card " + args[0] + " either doesn't exist or match failed.\n");
				return "0";
			}
			entry(0, "takeOutCard");
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
				String[] splitArgs = args[0].toLowerCase().split(" ", 2);
				String commandName = splitArgs[0];
				
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
					result = "syntax: save\n\u2022 saves the current inventory to the sylladex's deck. "
							+ "This command is called at the end of every other command except load.";
					break;
				case "load":
					result = "syntax: load <mode>\n\u2022 loads the inventory from the sylladex, which may differ."
							+ "\n\u2022 mode 0 will simply reset the inventory."
							+ "\n\u2022 mode 1 will auto load the inventory, based on card positions in the deck."
							+ "\n\u2022 mode 2 will manually load the inv. you will choose where items go."
							+ "\n\u2022 mode 3 will fast load the inventory. disregards saved card positions.";
					break;
				case "capture":
					result = "syntax: capture <item>\n\u2022 captchalogues the item. the item can have "
							+ "spaces when you type its name. puts in first available spot.";
					break;
				case "takeoutcard":
					result = "syntax: takeOutCard <index>, <folder>\n\u2022 takes out the card at "
							+ "the index within the folder. index is from 1 to 5.";
					break;
				default: 
					result = "syntax: help <command>\n\u2022 provides help information about the "
							+ "given command. syntax is the form you input a complete command. "
							+ "if a command has multiple arguments they need to be seperated by a comma.";
				}
				
				if (returnStringFlag == 1) {
					return result;
				} 
				System.out.println("Providing modus command help on: " + commandName);
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
	
	/* (non-Javadoc)
	 * @see modus.Modus#getSylladexReference()
	 */
	@Override
	public Sylladex getSylladexReference() {
		return sylladexReference;
	}
	
	//**************************** SAVE & LOAD ********************************/
	/* (non-Javadoc)
	 * @see modus.Modus#save()
	 */
	@Override
	public void save() {
	}

	/* (non-Javadoc)
	 * @see modus.Modus#load(int)
	 */
	@Override
	public void load(int mode) {
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
		if (! addCard(card)) return false;
		return true;
	}

	
	/* (non-Javadoc)
	 * @see modus.Modus#addCard(app.model.Card)
	 */
	@Override
	public Boolean addCard(Card card) {
		//if the box door is open, attempt to add card
		if (!isBoxOpen()) return false;
		return timeBox.add(card);
	}

	/**
	 * {@inheritDoc}
	 * <p>parameter is `Objects{String itemname}`.
	 * Uses the name of an item as a key to search for it's card.
	 * Because it calls {@link #findItemName(String)}, it may have
	 * 	undesired affects if the name given is misspelled. The
	 * 	function will attempt to get the closest match, but an 
	 * 	exact match is not guaranteed.
	 * <p> If the modus space is empty, this function will short circuit
	 * 	and return an "empty" Card.
	 */
	@Override
	public Card takeOutCard(Object... objects) {
		//if the box door is open and arg is a String, attempt to retrieve card
		if (!isBoxOpen() && String.class.isInstance(objects[0])) return new Card();
		String itemName = findItemName((String) objects[0]);
		if (itemName.isEmpty()) return new Card();
		for (Card card : timeBox) {
			//if the itemName matches, remove card from the box and return it
			if (card.getItem().equals(itemName)) {
				Card result = card;
				timeBox.remove(card);
				return result;
			}
		}
		return new Card();
	}

	//****************************** UTILITY ************************************/
	
	private Boolean isBoxOpen() {
		return boxState;
	}
	
	private void openBox() {
		//filter out all timelines that collapsed upon opening the box
		timelines = timelines.stream().filter(timeline -> {
			Optional<List<Card>> result = timeline.chronoCollapse(boxChronalState);
			if (result.isPresent()) {
				//timeline was collapsed, add its cards to the timeBox container
				timeBox.addAll(result.get());
				return false; //don't continue the pipeline. 
			} //otherwise, keep the non-collapsed timeline in the collection
			return true;
			}).collect(Collectors.toSet());
	}
	
	private void closeBox() {
		timelines.add(new Timeline(timeBox)); //pass our timeBox list to a new timeline
		timeBox = new ArrayList<Card>(); //set our timeBox variable to a new object reference.
	}
	
	/* (non-Javadoc)
	 * @see modus.Modus#isFull()
	 */
	@Override
	public Boolean isFull() {
		//currently, there is no maximum number of cards that can be stored
		return false;
	}

	/* (non-Javadoc)
	 * @see modus.Modus#isEmpty()
	 */
	@Override
	public Boolean isEmpty() {
		return timelines.isEmpty() && timeBox.isEmpty();
	}

	/* (non-Javadoc)
	 * @see modus.Modus#findItemName(java.lang.String)
	 */
	@Override
	public String findItemName(String givenName) {
		List<String> itemList = timeBox.stream().collect(ArrayList::new, (r, e) -> r.add(e.getItem()), ArrayList::addAll);
		return Sylladex.fuzzyStringSearch(itemList, givenName).getValue();
	}

	/* (non-Javadoc)
	 * @see modus.Modus#drawToDisplay()
	 */
	@Override
	public void drawToDisplay() {
	}

	/* (non-Javadoc)
	 * @see modus.Modus#description()
	 */
	@Override
	public String description() {
		return null;
	}

}
