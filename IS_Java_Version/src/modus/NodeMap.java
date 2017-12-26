package modus;

import app.controller.Sylladex;
import app.model.Card;

/**
 * @author Triston Scallan
 *
 */
public class NodeMap implements Modus {

	/* (non-Javadoc)
	 * @see modus.Modus#entry(int, java.lang.Object[])
	 */
	@Override
	public int entry(int functionCode, Object... objects) {
		return 0;
	}

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
	public void load(int mode) throws Exception {
	}

	/* (non-Javadoc)
	 * @see modus.Modus#capture(java.lang.String)
	 */
	@Override
	public Boolean capture(String item) {
		return null;
	}

	/* (non-Javadoc)
	 * @see modus.Modus#addCard(app.model.Card)
	 */
	@Override
	public Boolean addCard(Card card) {
		return null;
	}

	/* (non-Javadoc)
	 * @see modus.Modus#takeOutCard(java.lang.Object[])
	 */
	@Override
	public Card takeOutCard(Object... objects) {
		return null;
	}

	/* (non-Javadoc)
	 * @see modus.Modus#getSylladexReference()
	 */
	@Override
	public Sylladex getSylladexReference() {
		return null;
	}

	/* (non-Javadoc)
	 * @see modus.Modus#isFull()
	 */
	@Override
	public Boolean isFull() {
		return null;
	}

	/* (non-Javadoc)
	 * @see modus.Modus#isEmpty()
	 */
	@Override
	public Boolean isEmpty() {
		return null;
	}

	/* (non-Javadoc)
	 * @see modus.Modus#findItemName(java.lang.String)
	 */
	@Override
	public String findItemName(String givenName) {
		return null;
	}
	//Structures in java are classes, so a node would simply be a small class that contained information and then 
	//a public variable `Node.next` that was null but could be assigned to hold another Node object
	//to traverse, you'd create an independent Node variable, and assign it to hold each object and then equal the object's stored Node object.
	//instead of pointers to structures, it'd just be stored references of objects.


}
