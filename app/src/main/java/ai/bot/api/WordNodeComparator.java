package ai.bot.api;

import java.util.Comparator;

public class WordNodeComparator implements Comparator<WordNode> {

    @Override
    public int compare(WordNode arg0, WordNode arg1) {
        return arg0.compareTo(arg1);
    }
    
}
