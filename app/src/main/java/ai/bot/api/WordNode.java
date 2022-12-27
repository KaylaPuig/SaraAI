package ai.bot.api;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WordNode implements Comparable<WordNode> {
    private Random random;
    private String word;
    private int frequency;

    private List<WordNode> nextWords;

    public WordNode() {
        this.word = "";
        this.frequency = 0;
        random = new Random();
        nextWords = new ArrayList<>();
        nextWords.add(new WordNode("", 1));
    }

    /* Use only for the root node, as this loads all subnodes */
    public WordNode(File file) {
        this.word = "";
        this.frequency = 0;
        random = new Random();
        nextWords = new ArrayList<>();
        nextWords.add(new WordNode("", 1));
    }

    public WordNode(String word, int frequency) {
        this.word = word;
        this.frequency = frequency;
        random = new Random();
        nextWords = new ArrayList<>();
    }

    public WordNode pickNextRandom() {
        // Get scale of random word to pick
        int length = 0;
        List<Integer> ranges = new ArrayList<>();
        for(WordNode node : nextWords) {
            length += node.getFrequency();
            int prevIndex = ranges.size() - 1;
            if(prevIndex >= 0)
                ranges.add(node.getFrequency() + ranges.get(prevIndex));
            else
                ranges.add(node.getFrequency());
        }
        length = Math.max(1, length);

        assert(ranges.size() == nextWords.size());

        // Generate random scaled index
        int randInt = 1 + (Math.abs(random.nextInt()) % length);
        randInt = Math.max(1, randInt);
        for(int i = ranges.size() - 1; i >= 0; i--) {
            if(randInt >= ranges.get(i)) {
                return nextWords.get(i);
            }
        }

        return null; // TODO remove this statement when done
    }

    @Override 
    public String toString() {
        String res = word + "," + frequency;

        if(nextWords.size() > 0) {
            res += "(";
        }
        else {
            return res + "()";
        }

        for(WordNode wNode : nextWords) {
            res += wNode;
        }

        res += ")";

        return res;
    }

    public String toReadableString() {
        return toReadableString(0);
    }

    private String toReadableString(int depth) {
        String res = "";
        for(int i = 0; i < depth; i++) {
            res += "\t";
        }
        res += word + "," + frequency + "\n";
        for(WordNode node : nextWords) {
            res += node.toReadableString(depth + 1);
        }
        return res;
    }

    /* Getters */
    public String getWord() {
        return word;
    }

    public int getFrequency() {
        return frequency;
    }

    public boolean hasWord(String word) {
        for(WordNode node : nextWords) {
            if(node.getWord().equals(word)) {
                return true;
            }
        }
        return false;
    }

    public WordNode getWordNode(String word) {
        for(WordNode node : nextWords) {
            if(node.getWord().equals(word)) {
                return node;
            }
        }
        return null;
    }

    public List<WordNode> getNextWords() {
        return nextWords;
    }

    /* true iff nextWords.size() == 1, as this means the only string is blank placeholder text */
    public boolean isEmpty() {
        return nextWords.size() == 1;
    }

    /* Setters */
    public void addWordNode(String word, int frequency) {
        // Find the specified word
        WordNode matchingNode = null;
        for(WordNode node : nextWords) {
            if(node.getWord().equals(word)) {
                matchingNode = node;
                break;
            }
        }

        if(matchingNode == null) {
            // Add word to list, return
            matchingNode = new WordNode(word, frequency);
            nextWords.add(matchingNode);
            return;
        }

        // Add frequency to existing word
        matchingNode.setFrequency(frequency + matchingNode.getFrequency());
    }

    public void addWordNode(String word) {
        addWordNode(word, 1);
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    @Override
    /* A compareTo implementation which prioritizes frequency over lexicographic order of words */
    public int compareTo(WordNode arg0) {
        int cmp = frequency - arg0.getFrequency();
        if(cmp != 0)
            return Math.min(Math.max(cmp, -1), 1); /* Clamp to -1 or 1 as return value */
        return word.compareTo(arg0.getWord());
    }

}
