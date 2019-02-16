package app.modus;

import java.util.List;

import app.model.Card;
import app.model.Metadata;
import app.model.ModusBuffer;

/**
 * @author Triston Scallan
 *
 */
public class BTree implements Modus {

	@Override
	public Metadata getMETADATA() {
		return null;
	}

	@Override
	public List<Card> save(ModusBuffer modusBuffer) {
		return null;
	}

	@Override
	public void load(ModusBuffer modusBuffer) {

	}

	@Override
	public void drawToDisplay(ModusBuffer modusBuffer) {

	}

	@Override
	public String description() {
		return null;
	}

	@Override
	public List<Card> toDeck() {
		return null;
	}
}
