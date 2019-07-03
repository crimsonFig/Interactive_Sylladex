package app.core;

import app.model.Card;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.*;
import java.util.*;

@ParametersAreNonnullByDefault
class FileController {
    private static final Logger      LOGGER            = LogManager.getLogger(FileController.class);
    private static final String      DEFAULT_FILE_NAME = "sylladex_save_file";
    private static final String      DEFAULT_FILE_EXT  = "deck";
    private static final FileChooser fileChooser;

    static {
        fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("").getAbsoluteFile());
        fileChooser.setInitialFileName(DEFAULT_FILE_NAME + "." + DEFAULT_FILE_EXT);
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("sylladex deck files", DEFAULT_FILE_EXT));
    }

    /**
     * Writes the deck out to a binary file. Will create a new file if it doesn't exist.
     *
     * @param deck
     *         the deck to be written out
     * @param destination
     *         the local file to write to
     * @throws SecurityException
     *         thrown by {@link FileOutputStream}
     * @throws IOException
     *         thrown by {@link ObjectOutputStream}
     */
    static synchronized void writeDeckToFile(List<Card> deck, File destination) throws SecurityException, IOException {
        // create a copy of the deck with no null references, this helps ensure deck doesn't mutate during write
        List<Card> saveDeck = deck.stream().filter(Objects::nonNull).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        File       saveFile;
        // enforce file extension
        if (destination.getName().endsWith("." + DEFAULT_FILE_EXT)) {
            if (!destination.isFile() && !destination.createNewFile()) throw LOGGER.throwing(new IOException("could not create save file"));
            saveFile = destination;
        } else {
            saveFile = new File(destination.getPath().replaceFirst("\\.[^.]+$", "") + "." + DEFAULT_FILE_EXT);
            if (saveFile.exists() && !saveFile.isFile())
                throw LOGGER.throwing(new IOException(saveFile.getName() + " exists, but is not a valid file."));
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile))) {
            //write deck size to the front of the file,
            oos.writeInt(saveDeck.size());
            for (Card card : deck) {
                oos.writeObject(card);
            }
            //finally write a null to mark the end of the file.
            oos.writeByte((byte) '\0');
            oos.flush();
        }
        LOGGER.info("Saved deck to location: " + saveFile.getCanonicalPath());
    }

    static Optional<File> selectFileSave(Window window) {
        return Optional.ofNullable(fileChooser.showSaveDialog(window));
    }

    static Optional<File> selectFileLoad(Window window) {
        return Optional.ofNullable(fileChooser.showOpenDialog(window));
    }

    /**
     * Loads a binary file to extract a List of Card from.
     *
     * @param destination
     *         the local file to read from
     * @return a deck of Card loaded from the file
     *
     * @throws ClassNotFoundException
     *         thrown by {@link ObjectInputStream#readObject()}
     * @throws ClassCastException
     *         thrown if the found class in file isn't {@link Card}
     * @throws IOException
     *         thrown by {@link ObjectInputStream}
     */
    @Nonnull
    static List<Card> loadDeckFromFile(File destination) throws ClassNotFoundException, ClassCastException, IOException {
        List<Card> deck = new ArrayList<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(destination))) {
            int numOfCards = ois.readInt();
            for (int i = 0; i < numOfCards; i++) {
                Object o = ois.readObject();
                deck.add((Card) o);
            }
        } catch (EOFException ignore) {
        } catch (FileNotFoundException e) {
            LOGGER.error("sylladex load failed - ERROR: file not found at " + destination.getCanonicalPath(), e);
            throw e;
        }
        LOGGER.info("Loaded deck from location: " + destination.getCanonicalPath());
        return deck;
    }
}
