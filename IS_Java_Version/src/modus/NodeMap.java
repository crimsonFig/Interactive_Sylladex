package modus;

/**
 * @author Triston Scallan
 *
 */
public class NodeMap implements Modus {
	//Structures in java are classes, so a node would simply be a small class that contained information and then 
	//a public variable `Node.next` that was null but could be assigned to hold another Node object
	//to traverse, you'd create an independent Node variable, and assign it to hold each object and then equal the object's stored Node object.
	//instead of pointers to structures, it'd just be stored references of objects.

	/* (non-Javadoc)
	 * @see modus.Modus#entry()
	 */
	@Override
	public void entry() {
	}

	/* (non-Javadoc)
	 * @see modus.Modus#save()
	 */
	@Override
	public void save() {
	}

	/* (non-Javadoc)
	 * @see modus.Modus#load()
	 */
	@Override
	public void load() {
	}

	/* (non-Javadoc)
	 * @see modus.Modus#capture()
	 */
	@Override
	public void capture() {
	}

	/* (non-Javadoc)
	 * @see modus.Modus#addCard()
	 */
	@Override
	public void addCard() {
	}

	/* (non-Javadoc)
	 * @see modus.Modus#takeOutCard()
	 */
	@Override
	public void takeOutCard() {
	}

	/* (non-Javadoc)
	 * @see modus.Modus#isFull()
	 */
	@Override
	public void isFull() {
	}

	/* (non-Javadoc)
	 * @see modus.Modus#isEmpty()
	 */
	@Override
	public void isEmpty() {
	}

}
