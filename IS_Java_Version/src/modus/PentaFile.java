package modus;

import app.model.Card;

/**
 * This fetch modus is the PentaFile pfModus, a modus designed for use with a sylladex.
 *   Using a structure comprised of 5 arrays containing 5 cards, and busting if a
 *   single array is overfilled. <br>
 *   This is likened to a File Cabinet. 5 folders that can hold 5 files each.
 * @author Triston Scallan
 * <dt> Note: </dt> <dd>
 *   1. The inventory only holds 25 cards, 5 cards in 5 folders. <br>
 *   2. If 6 items are placed into a folder, 5 are ejected and the 6th is pushed <br>
 *   3. The current PentaFile can have all its information saved and loaded to a text file. <br>
 *   </dd>
 */
public class PentaFile implements Modus {
	//5 arrays, each with 5 elements
	Card[] weapons = new Card[5];
	Card[] survival = new Card[5];
	Card[] misc = new Card[5];
	Card[] info = new Card[5];
	Card[] keyCritical = new Card[5];
	
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
