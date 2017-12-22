package app.model;

/**
 * @author Triston Scallan
 *
 */
public class Card {
	///// Variables
	/** item name, max size should be 16 */
	private String item;			
	/** 7 alphanumeric code */
	private String captchaCode; 
	/** track if this card is empty or not */
	private Boolean inUse;
	
	///// Constructors
	/** Constructor for empty card */
	public Card() {
		this.item = "EMPTY";
		this.captchaCode = "0000000";
		this.inUse = false;
	}
	
	/** Constructor with item
	 * @param item The name of the item stored
	 */
	public Card(String item) {
		this.item = item;
		this.captchaCode = captchaHash(item);
		this.inUse = true;
	}
	
	///// Getters & Setters
	/**
	 * @return the item
	 */
	public String getItem() {
		return item;
	}

	/**
	 * @param item the item to set
	 */
	public void setItem(String item) {
		this.item = item;
	}

	/**
	 * @return the captchaCode
	 */
	public String getCaptchaCode() {
		return captchaCode;
	}

	/**
	 * @param captchaCode the captchaCode to set
	 */
	public void setCaptchaCode(String captchaCode) {
		this.captchaCode = captchaCode;
	}

	/**
	 * @return the inUse
	 */
	public Boolean getInUse() {
		return inUse;
	}

	/**
	 * @param inUse the inUse to set
	 */
	public void setInUse(Boolean inUse) {
		this.inUse = inUse;
	}
	
	///// Methods

	/**
	 * Validates the card to make sure it doesn't have bad data
	 * @param card the card to validate
	 * @return the validity of the card
	 */
	public Boolean validateCard() {
		if(this.item.length() <= 16 && this.captchaCode.matches("^[\\w\\d]{,7}$"))  
			return true;
		return false;
	}
	
	/**
	 * Creates a captchacode by using a hash function on the item.
	 * @param item the item's name
	 * @return a captchacode derived from the item name
	 */
	public String captchaHash(String item) {
		return "0000000"; //TODO: add hashing function
	}
}
