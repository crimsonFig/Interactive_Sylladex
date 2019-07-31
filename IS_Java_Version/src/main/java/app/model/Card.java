package app.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.RegEx;
import java.io.Serializable;

/**
 * The Card class is a data structure designed to hold information about a given item.
 * <br> This class provides functionality to manipulate item information and whether
 * or not this object is currently holding an actual item.
 *
 * @author Triston Scallan
 */
public class Card implements Serializable {
    /**
     * The class constant for an empty CARD.
     */
    public static final         Card   EMPTY             = new Card();
    private static final        long   serialVersionUID  = 1L;
    private static final        int    MAX_ITEM_LENGTH   = 16;
    @RegEx private static final String REQ_CAPTCHA_REGEX = "^[\\w\\d]{0,7}$";
    private static final        Logger LOGGER            = LogManager.getLogger(Card.class);

    ///// Variables
    /** item name, max size should be 16 */
    private final String  item;
    /** 7 alphanumeric code */
    private final String  captchaCode;
    /** track if this CARD is empty or not */
    private final Boolean inUse;

    ///// Constructors

    /** Constructor for empty CARD */
    public Card() {
        this.item = "EMPTY";
        this.captchaCode = "0000000";
        this.inUse = false;
    }

    /**
     * Constructor with item string
     *
     * @param item
     *         The name of the item stored
     * @throws IllegalArgumentException
     *         if the item parameter is null or too long
     */
    //TODO: consider replacing with a simple Card.of(itemName) and Card.EMPTY()? type safety isn't a worry since this isn't a generic.
    public Card(String item) throws IllegalArgumentException {
        if (item == null || item.trim().length() > MAX_ITEM_LENGTH) throw LOGGER.throwing(new IllegalArgumentException());
        this.item = item.trim().toUpperCase();
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
    public Boolean isInUse() {
        return inUse;
    }

    ///// Methods

    /**
     * Validates the CARD to make sure it doesn't have bad data
     *
     * @return true if valid, false otherwise
     */
    public Boolean isValid() {
        return this.item != null && this.item.length() <= MAX_ITEM_LENGTH && this.captchaCode.matches(REQ_CAPTCHA_REGEX);
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
        return "Card details -> captured item: " + item + ", Captchacode: " + captchaCode + ", 'in use' state: " + inUse + ".";
    }


}
