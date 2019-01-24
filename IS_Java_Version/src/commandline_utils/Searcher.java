package commandline_utils;

import java.util.*

import javafx.util.Pair;

/**
 * @author Triston Scallan
 *
 */
public class Searcher {
	/**
	 * The maximum number of {@value #MAX_COST} edits/mutations a string can have for fuzzy searching.
	 */
	public final static int MAX_COST = 3;
	
	/**
	 * Takes a word and searches for the closest match in the list.
	 * <p> Uses the Levenshtein Distance algorithm to compute the edit
	 * 	distance where a deletion or addition of a char counts as 1 and
	 * 	a char mutation counts as 1. This comparison is case insensitive.
	 * <p> Allows only a maximum of {@value Searcher#MAX_COST} edits, otherwise it fails.
	 * @param givenWord The word to be matched
	 * @param wordBag The collection to search
	 * @return an Object array of format {@code [int index, String match]}. If failed,
	 * 	returns {@code [-1,""]}.
	 */
	@Deprecated
	public static Pair<Integer, String> fuzzyStringSearch(String givenWord, Collection<String> wordBag) {
		String guessedWord = "";
		int score = 999;	//the lower the score, the better
		int index;		//index of the word in the word list.
		int guessedIndex = 0; //index of the guessedWord
		Iterator<String> wordList = wordBag.iterator();
		
		for(index = 0; index < wordBag.size(); index++) {
			
			String testWord = wordList.next();
			
			//if word is null, skip. if a match is found, return it.
			if (testWord == null) {
				continue;
			} else if (givenWord.toLowerCase().equals(testWord.toLowerCase())) {
				return new Pair<>(index, testWord);
			}
				
			//create copies of the item names to prevent confusion if swapped later.
				//also make the comparisons case insensitive.
			String left = givenWord.toLowerCase();
			String right = testWord.toLowerCase();
			int lenGiven = left.length(); 	// length of first string
	        int lenTest = right.length(); 	// length of second string
	
	        //if the given is empty and CARD is lower than score, set guessed to test.
	        if (lenGiven == 0 && lenTest < score) { 
	        		//score = lenTest;
	        		//guessedWord = testWord;
	            continue;
	        //if the test is empty and the given is lower than the score, set guessed to test 
	        } else if (lenTest == 0) {
	        		//score = lenGiven;
	        		//guessedWord = testWord;
	            continue;
	        }
	        //if the given item is longer than the CARD item
	        if (lenGiven > lenTest) {
	            //swap the strings to use less memory
	            final String tmp = left;
	            left = right;
	            right = tmp;
	            lenGiven = lenTest;	
	            lenTest = right.length();
	        }
	        
	        final int[] d = new int[lenGiven + 1];	//cost array, "(d)istance"
	        int i; // iterates through left string
	        int j; // iterates through right string
	        int upperLeft;
	        int upper;	
	        char rightJ; // jth character of right
	        int cost; 
	        
	        //initialize the array
	        for (i = 0; i <= lenGiven; i++) {
	            d[i] = i;
	        } //[0,1,2,3,...,i-1]
	
	        for (j = 1; j <= lenTest; j++) {
	            upperLeft = d[0]; 
	            rightJ = right.charAt(j - 1);
	            d[0] = j; 
	
	            for (i = 1; i <= lenGiven; i++) {
	                upper = d[i];
	                cost = left.charAt(i - 1) == rightJ ? 0 : 1;
	                // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
	                d[i] = Math.min(Math.min(d[i - 1] + 1, d[i] + 1), upperLeft + cost);
	                upperLeft = upper;
	            }
	        }
	        cost = d[lenGiven];
	        if (cost < score) { //if cost is lower than the current score, update the best guessed name
	        		score = cost;
	        		guessedWord = testWord;
	        		guessedIndex = index;
	        }
	    }
		//if the score is within the threshold, return info, otherwise return a fail state.
		return (score < Searcher.MAX_COST) ? new Pair<>(guessedIndex, guessedWord) : new Pair<>(-1, "");
	}
}
