package ai.bot.api;

import java.util.HashMap;
import java.util.Map.Entry;

class WordNode implements Comparable<WordNode> {
    //public static final WordNode NULLNODE = new WordNode("", 0);

    private String word;
    private long frequency;

    private long totalWordsAdded;
    private HashMap<String, Long> nextWords;

    public WordNode(String word, long frequency) {
        this.word = word;
        this.frequency = frequency;
        this.totalWordsAdded = 0;
        this.nextWords = new HashMap<String, Long>();
    }

    /* Getters */
    @SuppressWarnings("unchecked")
    public HashMap<String, Long> copyOfNextWords()
    {
        return (HashMap<String, Long>) nextWords.clone();
    }

    public String getWord() {
        return word;
    }

    public long getFrequency() {
        return frequency;
    }

    public String getNextRandomWord()
    {
        long rand = (long) (Math.random() * totalWordsAdded);

        for (Entry<String, Long> entry : nextWords.entrySet())
        {
            if (rand < entry.getValue())
            {
                return entry.getKey();
            }
            else
            {
                rand -= entry.getValue();
            }
        }

        return "";
    }

    /* Setters */
    protected void mapWordAndFrequency(String word, long frequency)
    {
        this.nextWords.put(word, frequency);
        this.totalWordsAdded += frequency;
    }

    public void addWord(String word) {
        Long frequency = nextWords.getOrDefault(word, null);
        if (frequency == null)
        {
            nextWords.put(word, 1L);
        }
        else
        {
            nextWords.put(word, frequency + 1);
        }
        this.totalWordsAdded++;
    }

    public void setFrequency(long frequency) {
        this.frequency = frequency;
    }

    public void incrementFrequency()
    {
        this.frequency++;
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

        res += ")";

        return res;
    }

    @Override
    /* A compareTo implementation which prioritizes frequency over lexicographic order of words */
    public int compareTo(WordNode arg0) {
        long cmp = frequency - arg0.getFrequency();
        if(cmp != 0)
            return (int) Math.min(Math.max(cmp, -1l), 1l); /* Clamp to -1 or 1 as return value */
        return word.compareTo(arg0.getWord());
    }

}
