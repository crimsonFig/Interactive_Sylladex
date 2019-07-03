package app.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileControllerTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    // todo: -- tests for write deck to file
    // check for nullity failure (parameters must not be null)
    // check for file acceptance - only normal files (of any path) should be permitted, reject anything else
    // check that file extension is enforced correctly (none, different, same, multiple, only)
    // check that file written is correctly named, contents are as expected, and was written to the expected directory
    // check for list acceptance - fail if list is not all Cards, contains a null, or the list is null

    // todo: -- tests for load deck from file
    // check for file acceptance correctness
    // - exception if file is not found or file is not a normal file
    // check for content integrity
    // - exception if deck is not all Cards or contains a null object, no exception thrown if deck is truly all cards
    // - exception if num of cards expected mismatches cards read, no exception if num of cards expected matches cards read
    // - (potentially?) check if cards are valid
    // - check that returned deck is deeply equal to the deck saved (and correctly fails cases when it is not, if possible?)
}