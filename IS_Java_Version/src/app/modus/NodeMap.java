package app.modus;

import java.util.LinkedHashMap;

import app.model.Card;
import app.model.Metadata;

/**
 * @author Triston Scallan
 *
 */
public class NodeMap implements Modus {

	
	/* (non-Javadoc)
	 * @see app.modus.Modus#createFunctionMap()
	 */
	@Override
	public LinkedHashMap<String, Integer> createFunctionMap() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see app.modus.Modus#entry(int, java.lang.Object[])
	 */
	@Override
	public String entry(int functionCode, String... args) {
		return "0";
	}

	/* (non-Javadoc)
	 * @see app.modus.Modus#getMETADATA()
	 */
	@Override
	public Metadata getMETADATA() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see app.modus.Modus#save()
	 */
	@Override
	public void save() {
	}

	/* (non-Javadoc)
	 * @see app.modus.Modus#load(int)
	 */
	@Override
	public void load(int mode) throws Exception {
	}

	/* (non-Javadoc)
	 * @see app.modus.Modus#capture(java.lang.String)
	 */
	@Override
	public Boolean capture(String item) {
		return null;
	}

	/* (non-Javadoc)
	 * @see app.modus.Modus#addCard(app.model.Card)
	 */
	@Override
	public Boolean addCard(Card card) {
		return null;
	}

	/* (non-Javadoc)
	 * @see app.modus.Modus#takeOutCard(java.lang.Object[])
	 */
	@Override
	public Card takeOutCard(Object... objects) {
		return null;
	}

	/* (non-Javadoc)
	 * @see app.modus.Modus#isFull()
	 */
	@Override
	public Boolean isFull() {
		return null;
	}

	/* (non-Javadoc)
	 * @see app.modus.Modus#isEmpty()
	 */
	@Override
	public Boolean isEmpty() {
		return null;
	}

	/* (non-Javadoc)
	 * @see app.modus.Modus#findItemName(java.lang.String)
	 */
	@Override
	public String findItemName(String givenName) {
		return null;
	}
	//Structures in java are classes, so a node would simply be a small class that contained information and then 
	//a public variable `Node.next` that was null but could be assigned to hold another Node object
	//to traverse, you'd create an independent Node variable, and assign it to hold each object and then equal the object's stored Node object.
	//instead of pointers to structures, it'd just be stored references of objects.

	/* (non-Javadoc)
	 * @see app.modus.Modus#drawToDisplay()
	 */
	@Override
	public void drawToDisplay() {
	}

	/* (non-Javadoc)
	 * @see app.modus.Modus#description()
	 */
	@Override
	public String description() {
		return null;
	}


}