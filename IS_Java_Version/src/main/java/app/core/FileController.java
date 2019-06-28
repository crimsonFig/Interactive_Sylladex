package app.core;

import app.model.Card;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

class FileController {
    /**
     * Writes the deck out to a binary file. Will create a new file if it doesn't exist.
     * @param deck the deck to be written out
     * @param fullOutPath the local path to write to
     * @throws SecurityException thrown by {@link FileOutputStream}
     * @throws IOException thrown by {@link ObjectOutputStream}
     */
    static void writeDeckToFile(List<Card> deck, String fullOutPath) throws SecurityException, IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(fullOutPath)))) {
            //write deck size to the front of the file,
            oos.writeInt(deck.size());
            for (Card card : deck) {
                oos.writeObject(card);
            }
            //finally write a null to mark the end of the file.
            oos.writeByte((byte) '\0');
            oos.flush();
        }
    }

    /**
     * Loads a binary file to extract a List of Card from.
     * @param fullInPath the local path to read from
     * @return a deck of Card loaded from the file
     * @throws ClassNotFoundException thrown by {@link ObjectInputStream#readObject()}
     * @throws ClassCastException thrown if the found class in file isn't {@link Card}
     * @throws IOException thrown by {@link ObjectInputStream}
     */
    static List<Card> loadDeckFromFile(String fullInPath) throws ClassNotFoundException, ClassCastException, IOException {
        List<Card> deck = new ArrayList<>();
        final File file = new File(fullInPath);

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Integer numOfCards = ois.readInt();
            for (int i = 0; i < numOfCards; i++) {
                Object o = ois.readObject();
                deck.add(Card.class.cast(o));
            }
            return deck;
        } catch (EOFException e) {
            return deck;
        } catch (FileNotFoundException e) {
            System.err.println("sylladex load failed - ERROR: file not found at " + file.getPath());
            throw e;
        }
    }
}
