package app.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;

/**
 * Describes the graphical representation of a Card object.
 * To use this CARD, simply reference it's constants.
 * @author Triston Scallan
 *
 */
public class CardNode {
	public static final CardNode EMPTY = new CardNode(Card.EMPTY);
	/**
	 * 
	 */
	public final Card CARD;
	/**
	 * 
	 */
	public final GridPane CARD_FACE; 
	/**
	 * 
	 */
	public final GridPane CARD_BACK;
	/**
	 * Scaling factor to resize the CARD on the fly.
	 */
	private double cardScaleFactor;
	
	/**
	 * @param CARD
	 */
	public CardNode(Card card) {
		this.CARD = card;
		this.CARD_FACE = createCardFront();
		this.CARD_BACK = createCardBack();
	}

	/**
	 * @return the cardScaleFactor
	 */
	public double getCardScaleFactor() {
		return cardScaleFactor;
	}

	/**
	 * Rescales the CARD to the given factor based on it's X and Y values.<br>
	 * This only changes the apparent height and width when displayed 
	 * without changing the actual height and width properties. 
	 * @param cardScaleFactor the cardScaleFactor to set
	 */
	public void setCardScaleFactor(double cardScaleFactor) {
		this.cardScaleFactor = cardScaleFactor;
		CARD_FACE.setScaleX(cardScaleFactor);
		CARD_FACE.setScaleY(cardScaleFactor);
		CARD_BACK.setScaleX(cardScaleFactor);
		CARD_BACK.setScaleY(cardScaleFactor);
	}

	/**
	 * @return node
	 */
	private GridPane createCardFront() {
		GridPane node = new GridPane();
		//needs 3 columns
		node.getColumnConstraints().add(new ColumnConstraints(10, 40, Region.USE_PREF_SIZE, null, HPos.LEFT, false));
		node.getColumnConstraints().add(new ColumnConstraints(10, 120, Region.USE_PREF_SIZE, null, HPos.LEFT, false));
		node.getColumnConstraints().add(new ColumnConstraints(10, 40, Region.USE_PREF_SIZE, null, null, false));
		//needs 4 rows
		node.getRowConstraints().add(new RowConstraints(10, 30, Region.USE_PREF_SIZE, null, VPos.TOP, false));
		node.getRowConstraints().add(new RowConstraints(10, 131.6, Region.USE_PREF_SIZE, null, null, false));
		node.getRowConstraints().add(new RowConstraints(10, 33.5, Region.USE_PREF_SIZE, null, null, false));
		node.getRowConstraints().add(new RowConstraints(10, 27.5, Region.USE_PREF_SIZE, null, null, false));
		node.setPrefSize(160, 200);
		node.setMaxSize(160, 200);
		node.setMinSize(160, 200);
		
		//create children of GridPane node
		SVGPath cardShadow = new SVGPath();
		cardShadow.setContent("M 40 0 L 160 0 L 160 0 L 160 200 L 0 200 L 0 40 L 20 40 L 20 20 L 40 20 Z");
		cardShadow.setFill(Paint.valueOf("#757575"));
		cardShadow.setStroke(Paint.valueOf("#000000"));
		cardShadow.setStrokeWidth(0.25);
		cardShadow.setTranslateX(-4.0);
		cardShadow.setTranslateY(2.0);
		node.add(cardShadow, 0, 0);
		
		SVGPath cardFace = new SVGPath();
		cardFace.setContent("M 40 0 L 160 0 L 160 0 L 160 200 L 0 200 L 0 40 L 20 40 L 20 20 L 40 20 Z");
		cardFace.setFill(Paint.valueOf("#FF0000"));
		node.add(cardFace, 0, 0);
		
		SVGPath pageShadow = new SVGPath();
		pageShadow.setContent("M 40 20 L 130 20 L 130 150 L 20 150 L 20 40 L 40 40 Z");
		pageShadow.setFill(Paint.valueOf("#9e0000"));
		pageShadow.setTranslateX(-1.0);
		pageShadow.setTranslateY(2.0);
		GridPane.setValignment(pageShadow, VPos.TOP);
		node.add(pageShadow, 1, 1);
		
		SVGPath pageFace = new SVGPath();
		pageFace.setContent("M 40 20 L 130 20 L 130 150 L 20 150 L 20 40 L 40 40 Z");
		pageFace.setFill(Paint.valueOf("#FFFFFF"));
		pageFace.setTranslateX(4.0);
		GridPane.setValignment(pageFace, VPos.TOP);
		node.add(pageFace, 1, 1);
		
		Label itemName = new Label(CARD.getItem());
		itemName.setFont(new Font("Courier", 12));
		itemName.setTextFill(Paint.valueOf("#FFFFFF"));
		itemName.setPadding(new Insets(0, 2, 2, 2));
		GridPane.setHalignment(itemName, HPos.CENTER);
		GridPane.setValignment(itemName, VPos.BOTTOM);
		node.add(itemName, 0, 2, 5, 1);
		
		Rectangle captchaBG = new Rectangle(80, 20);
		captchaBG.setFill(Paint.valueOf("#FFFFFF"));
		GridPane.setHalignment(captchaBG, HPos.CENTER);
		GridPane.setValignment(captchaBG, VPos.BOTTOM);
		node.add(captchaBG, 1, 3);
		
		Label captcha = new Label(CARD.getCaptchaCode());
		captcha.setFont(new Font("CourierStd", 13));
		GridPane.setHalignment(captcha, HPos.CENTER);
		GridPane.setValignment(captcha, VPos.CENTER);
		node.add(captcha, 1, 3);
		
		node.setTranslateX(4);
		return node;
	}

	/**
	 * @return
	 */
	private GridPane createCardBack() {
		GridPane node = new GridPane();
		//needs 1 columns
		node.getColumnConstraints().add(new ColumnConstraints(10, 200, Region.USE_PREF_SIZE, null, HPos.LEFT, false));
		//needs 1 rows
		node.getRowConstraints().add(new RowConstraints(10, 160, Region.USE_PREF_SIZE, null, VPos.TOP, false));
		node.setPrefSize(160, 200);
		node.setMaxSize(160, 200);
		node.setMinSize(160, 200);
		
		//create children of GridPane node
		SVGPath cardShadow = new SVGPath();
		cardShadow.setContent("M 40 0 L 160 0 L 160 0 L 160 200 L 0 200 L 0 40 L 20 40 L 20 20 L 40 20 Z");
		cardShadow.setFill(Paint.valueOf("#757575"));
		cardShadow.setStroke(Paint.valueOf("#FFFFFF"));
		cardShadow.setStrokeWidth(0.25);
		cardShadow.setTranslateX(-4.0);
		cardShadow.setTranslateY(2.0);
		cardShadow.setScaleX(-1);
		node.add(cardShadow, 0, 0);
		
		SVGPath cardBack = new SVGPath();
		cardBack.setContent("M 40 0 L 160 0 L 160 0 L 160 200 L 0 200 L 0 40 L 20 40 L 20 20 L 40 20 Z");
		cardBack.setFill(Paint.valueOf("#FF0000"));
		cardBack.setScaleX(-1);
		node.add(cardBack, 0, 0);
		
		SVGPath cardGraphic = new SVGPath(); //the gradient design of the CARD's back
		cardGraphic.setContent("M 40 0 L 160 0 L 160 0 L 160 200 L 0 200 L 0 40 L 20 40 L 20 20 L 40 20 Z");
		List<Stop> stops = new ArrayList<Stop>();
				stops.add(new Stop(0.0, Color.rgb(70, 39, 39)));
				stops.add(new Stop(0.43959418632119457, Color.rgb(235, 47, 47)));
				stops.add(new Stop(0.7525456602553742, Color.rgb(255, 64, 185)));
				stops.add(new Stop(1.0, Color.rgb(252, 197, 197)));
		Random rng = new Random();
		//Classic default design below.
		//RadialGradient graphic = new RadialGradient(-90, -0.18, 1.0, 0, 0.476, true, CycleMethod.REPEAT, stops);
		double r;//value of the radius
		double n;//value of the focus distance
		double x = 1; //probability multiplier for the radius. x>1=less lean, x<1=more lean
		RadialGradient graphic = new RadialGradient(
				-120 + rng.nextInt(61),		//-120..-60
				n = rng.nextDouble() * 2 - 1,	//-1..1
				rng.nextInt(2),				//0, 1
				rng.nextInt(2),				//0, 1
				(n < 0.4) ? 
						((((r = x*Math.abs(rng.nextGaussian()) * -1 + 1) < 0.4) ? r : 0.4)) //if focus distance is below 0.4, lean to 1 and limit at 0.4. 0.4 .. 1
						: (((r = x*Math.abs(rng.nextGaussian()) - 0.05) > 1) ? r : 1), //if above 0.4, lean to 0.05 and limit at 1. 0.05 .. 1
				true, CycleMethod.REPEAT, stops);
		cardGraphic.setFill(Paint.valueOf(graphic.toString()));
		cardGraphic.setScaleX(-0.9);
		cardGraphic.setScaleY(0.9);
		cardGraphic.setTranslateX(-2.0);
		node.add(cardGraphic, 0, 0);
		
		node.setTranslateX(4);
		return node;
	}
}
