package app.model;

import java.io.Serializable;

/**
 * The Card class is a data structure designed to hold information about a given item.
 * <br> This class provides functionality to manipulate item information and whether
 * or not this object is currently holding an actual item.
 *
 * @author Triston Scallan
 */
public class Card implements Serializable {
    public static final  Card EMPTY            = new Card();
    private static final long serialVersionUID = 1L;

    ///// Variables
    /** item name, max size should be 16 */
    private final String  item;
    /** 7 alphanumeric code */
    private final String  captchaCode;
    /** track if this card is empty or not */
    private final Boolean inUse;

    ///// Constructors

    /** Constructor for empty card */
    public Card() {
        this.item = "EMPTY";
        this.captchaCode = "0000000";
        this.inUse = false;
    }

    /**
     * Constructor with item
     *
     * @param item
     *         The name of the item stored
     */
    public Card(String item) {
        this.item = item.toUpperCase();
        this.captchaCode = captchaHash(item);
        this.inUse = true;
    }

    ///// Getters

    /**
     * @return the item
     */
    public String getItem() {
        return item;
    }

    /**
     * @return the captchaCode
     */
    public String getCaptchaCode() {
        return captchaCode;
    }


    /**
     * @return the inUse
     */
    public Boolean getInUse() {
        return inUse;
    }

    ///// Methods

    /**
     * Validates the card to make sure it doesn't have bad data
     *
     * @return true if valid, false otherwise
     */
    public Boolean validateCard() {
        return this.item.length() <= 16 && this.captchaCode.matches("^[\\w\\d]{0,7}$");
    }

    /**
     * Creates a captchacode by using a hash function on the item.
     *
     * @param item
     *         the item's name
     * @return a captchacode derived from the item name
     */
    public String captchaHash(String item) {
        return "0000000"; //TODO: add hashing function
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Card details -> captured item: " +
               item +
               ", Captchacode: " +
               captchaCode +
               ", 'in use' state: " +
               inUse +
               ".";
    }


}
