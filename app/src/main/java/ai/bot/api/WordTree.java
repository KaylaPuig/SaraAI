package ai.bot.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class WordTree {
    // totalWordsAdded is the number of words added, including repeats
    private long totalWordsAdded;

    private HashMap<String, WordNode> words;

    public WordTree()
    {
        this.totalWordsAdded = 0;
        this.words = new HashMap<>();
    }

    // Load from file named after the guild this particular wordtree came from
    @SuppressWarnings("unchecked")
    public WordTree(File wordFile)
    {
        this.totalWordsAdded = 0;
        this.words = new HashMap<>();

        StringBuilder jsonContents = new StringBuilder();
        try {
            for (String s : Files.readAllLines(wordFile.toPath()))
            {
                jsonContents.append(s);
            }
        } catch (IOException e) {
            errorConstruct();
            return;
        }

        JSONObject treeJson = (JSONObject) JSONValue.parse(jsonContents.toString());

        JSONArray jsonWords = (JSONArray) treeJson.getOrDefault("words", null);
        if (jsonWords == null)
        {
            errorConstruct();
            return;
        }

        for (Object o : jsonWords)
        {
            JSONObject obj = (JSONObject) o;

            String jsonWord = (String) obj.getOrDefault("word", null);
            if (jsonWord == null)
            {
                errorConstruct();
                return;
            }

            Long jsonFrequency = (Long) obj.getOrDefault("frequency", null);
            if (jsonFrequency == null)
            {
                errorConstruct();
                return;
            }
            this.totalWordsAdded += jsonFrequency;

            WordNode node = new WordNode(jsonWord, jsonFrequency);
            words.put(jsonWord, node);

            JSONArray jsonNextWords = (JSONArray) obj.getOrDefault("nextWords", null);
            if (jsonNextWords == null)
            {
                errorConstruct();
                return;
            }
            for (Object o2 : jsonNextWords)
            {
                JSONObject nwObj = (JSONObject) o2;

                String nwWord = (String) nwObj.getOrDefault("word", null);
                if (nwWord == null)
                {
                    errorConstruct();
                    return;
                }

                Long nwFrequency = (Long) nwObj.getOrDefault("frequency", null);
                if (nwFrequency == null)
                {
                    errorConstruct();
                    return;
                }

                node.mapWordAndFrequency(nwWord, nwFrequency);
            }
        }
    }

    private void errorConstruct()
    {
        this.totalWordsAdded = 0;
        this.words = new HashMap<>();
    }

    public void addSentence(String sentence)
    {
        String[] sentenceWords = sentence.split(" ");
        for (String word : sentenceWords)
        {
            addWord(word);
        }
        words.get(sentenceWords[sentenceWords.length - 1]).addWord("");

        for (int i = 1; i < sentenceWords.length; i++)
        {
            words.get(sentenceWords[i-1]).addWord(sentenceWords[i]);
        }
    }

    private void addWord(String word)
    {
        WordNode node = words.getOrDefault(word, null);
        if (node == null)
        {
            node = new WordNode(word, 1);
            words.put(word, node);
        }
        else
        {
            node.incrementFrequency();
        }
        totalWordsAdded++;
    }

    public String generateSentence()
    {
        StringBuilder result = new StringBuilder();
        int rand = (int) (Math.random() * totalWordsAdded);

        // Pick starting word
        String startWord = "";
        for (WordNode node : words.values())
        {
            if (rand < node.getFrequency())
            {
                startWord = node.getWord();
                if (!startWord.equals(""))
                    break;
            }
            else
            {
                rand -= node.getFrequency();
            }
        }
        result.append(startWord);
        result.append(" ");

        // Pick next words based on starting words until one return value is ""
        String nextWord = words.get(startWord).getNextRandomWord();
        while (!nextWord.equals(""))
        {
            result.append(nextWord);
            result.append(" ");
            nextWord = words.get(nextWord).getNextRandomWord();
        }

        return result.toString();
    }

    public boolean isEmpty()
    {
        return words.isEmpty();
    }

    // Returns all data from this tree in JSON format
    @SuppressWarnings("unchecked")
    public String toData()
    {
        JSONObject outData = new JSONObject();

        JSONArray jsonWords = new JSONArray();
        for (WordNode node : words.values())
        {
            JSONObject jsonNode = new JSONObject();
            jsonNode.put("word", node.getWord());
            jsonNode.put("frequency", node.getFrequency());

            JSONArray jsonNextWords = new JSONArray();
            for (Entry<String, Long> nodeEntry : node.copyOfNextWords().entrySet())
            {
                JSONObject jsonNextWord = new JSONObject();
                jsonNextWord.put("word", nodeEntry.getKey());
                jsonNextWord.put("frequency", nodeEntry.getValue());

                jsonNextWords.add(jsonNextWord);
            }

            jsonNode.put("nextWords", jsonNextWords);
            jsonWords.add(jsonNode);
        }

        outData.put("words", jsonWords);
        return outData.toJSONString();
    }
}
