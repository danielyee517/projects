/**
 * Prefix-Trie. Supports linear time find() and insert(). 
 * Should support determining whether a word is a full word in the 
 * Trie or a prefix.
 * @author Daniel Yee
 */

import java.util.HashMap;
import java.util.Scanner;
import java.util.LinkedList;
import java.util.HashSet;

/**
 * Data Structure for a trie that stores strings and can process input files to find the longest compound words.
 * @author Daniel Yee
 */

public class Trie {
    private Scanner sc;
    private Node root = new Node();

    // Constructor to give a Trie a scanner.
    public Trie(Scanner sc) {
        this.sc = sc;
    }

    // Wrapper class for nodes in the trie
    private class Node {
        boolean exists;
        char letter;
        HashMap<Character, Node> links;

        // Constructor for a node that gives it a hashmap to subsequent characters.
        public Node() {
            links = new HashMap<Character, Node>();
            exists = false;
        }
    }

    /**
     * 1) Prints the longest compound word read by the list input to the scanner
     * 2) Prints the 2nd longest word found read by the list input to the scanner
     * 3) Prints the total count of how many of the words in the list can be constructed from other words.
     */

    private void findCompoundsAndPrint() {
        String word;
        String alphabet;
        String prefixSoFar;
        String suffix;
        String[] suffixWordPair;
        String longestWord = "";
        String secondLongestWord = "";
        int compoundWordCount = 0;
        LinkedList potentialSuffixes = new LinkedList();
        HashSet compoudWordsSoFar = new HashSet(); // so you never check the same compound word twice
        String suffixOfSuffix;
        if (!sc.hasNextLine()) {
            throw new  IllegalArgumentException("No words");
        } else {
            while (sc.hasNextLine()) {          // process all the words
                word = sc.nextLine();
                insert(word);
                prefixSoFar = "";
                for (int i = 0; i < word.length() - 1; i++){    // check for all prefixes
                    char c = word.charAt(i);
                    prefixSoFar += c;
                    if (find(prefixSoFar, true)) {
                        suffixWordPair = new String[2];
                        suffix = word.substring(i + 1, word.length()); // find the corresponding suffix
                        suffixWordPair[0] = suffix;
                        suffixWordPair[1] = word;
                        // System.out.println("prefix " + prefixSoFar);
                        // System.out.println("suffix " + suffix);
                        // System.out.println("word " + word);
                        potentialSuffixes.add(suffixWordPair); // add the suffix + the original word to list
                    }
                }
            }
            // it's time to process the LinkedList of potential compound words
            while (!potentialSuffixes.isEmpty()) {
                suffixWordPair = (String[]) potentialSuffixes.poll();
                suffix = suffixWordPair[0];
                // System.out.println("Possible suffix " + suffix);
                word = suffixWordPair[1];
                // System.out.println("Possible compound word " + word);
                if (compoudWordsSoFar.contains(word)) { // if the word is already found, no need to check again
                    continue;
                }
                if (find(suffix, true)) {
                    compoundWordCount++; // found a compoundWord
                    // System.out.println("compound is " + word);
                    compoudWordsSoFar.add(word);
                    if (word.length() >= longestWord.length() && word.length() >= secondLongestWord.length()) {
                        secondLongestWord = longestWord;
                        longestWord = word;
                    } else if (word.length() <= longestWord.length() && word.length() >= secondLongestWord.length()) {
                        secondLongestWord = word;
                    }
                } else {
                    prefixSoFar = "";
                    for (int i = 0; i < suffix.length() - 1; i++) {    // check for all prefixes
                        char c = suffix.charAt(i);
                        prefixSoFar += c;
                        if (find(prefixSoFar, true)) {
                            suffixWordPair = new String[2];
                            suffixOfSuffix = suffix.substring(i + 1, suffix.length()); // find the corresponding suffix
                            suffixWordPair[0] = suffixOfSuffix;
                            suffixWordPair[1] = word;
                            potentialSuffixes.add(suffixWordPair); // add the suffix + the original word to list
                        }
                }
            }
        }
        System.out.println("The longest compound word is " + longestWord);
        System.out.println("The 2nd longest compound word is " + secondLongestWord);
        System.out.println("Number of compound words: " + compoundWordCount);
        }
    }

    /**
     * Returns whether the given string is in the Trie
     * @param s String to find in the Trie
     * @param isFullWord boolean to tell you if you should look for a fullWord or simply a prefix.
     * @return boolean returns true if the word is in the trie
     */
    public boolean find(String s, boolean isFullWord) {
        if (s == null || s.length() == 0) {
            return false;
        }
        Node currentNode = root;
        for (int i = 0; i < s.length(); i++) {
            if (currentNode.links.get(s.charAt(i)) == null) {
                return false;
            } 
            currentNode = currentNode.links.get(s.charAt(i));
            if (i == s.length() - 1 && !currentNode.exists && !isFullWord) {
                return true;
            }
            if (i == s.length() - 1 && currentNode.exists) {
                return true;
            }
        }
        return false;
    }

    /**
     * Inserts the given String into the trie
     * @param s the word to enter into the trie
     */
    public void insert(String s) {
        if (s != null && s.length() != 0) {
            insert(root, s, 0);
        }
    }

    /**
     * Helper method to insert each character of key into the Trie using nodes
     * @param x The Node to insert into the Trie
     * @param key The word that we are inserting into the Trie
     * @param d the depth we have gone into the Trie that will tell us what char to put
     * @return Node Returns the node so the previous Node can enter it into its links
     */
    private Node insert(Node x, String key, int d) {
        if (x == null) {
            x = new Node();
        }

        if (d == key.length()) {
            x.exists = true;
            return x;
        }
        char c = key.charAt(d);
        x.links.put(c, insert(x.links.get(c), key, d + 1));
        return x;
    }

    /**
     * Main method for testing purposes.
     * @param args String array for arguments
     */
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        Scanner sc = new Scanner(System.in);
        Trie t = new Trie(sc);
        t.findCompoundsAndPrint();
        sc.reset(); 
        long endTime = System.nanoTime();
        System.out.println("Took "+(endTime - startTime) + " ns");
    }
}
