package wordfrequency;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class WordFrequency {
    public WordFrequency() {
    }

    public List<Map.Entry<String, Integer>> getWordFrequency(String textPath, boolean order) throws IOException {
        String text = Files.readString(Paths.get(textPath));

        // split word text into a string array
        String[] words = text.split("\\b");
        // ignoring cap case, clear the repeating element of string array
        Set<String> wordsSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        wordsSet.addAll(Arrays.asList(words));
        Map<String, Integer> wordFrequence = new HashMap<>();
        for (String s1 : wordsSet) {
            int count = 0;
            // match all punctuation
            String pattern = "[\\p{P}\\s]+";
            for (String s2 : words) {
                // do not count the frequence of punctuation
                if (s1.matches(pattern)) {
                    continue;
                }
                if (s1.toLowerCase().equals(s2.toLowerCase())) {
                    count++;
                }
            }
            if (count != 0) {
                wordFrequence.put(s1, count);
            }
        }
        List<Map.Entry<String, Integer>> entryList = sortFrequency(wordFrequence, order);
        return entryList;
    }

    public List<Map.Entry<String, Integer>> sortFrequency(Map<String, Integer> wordFrequence, boolean order) {
        // sort and output
        List<Map.Entry<String, Integer>> entryList = new ArrayList<>(wordFrequence.entrySet());

                // sort entryList
        if (order == true) {
            Collections.sort(entryList, new Comparator<Map.Entry<String, Integer>>() {
                @Override
                public int compare(Map.Entry<String, Integer> entry1, Map.Entry<String, Integer> entry2) {
                    return entry2.getValue().compareTo(entry1.getValue());
                }
            });
        } else {
            Collections.sort(entryList, new Comparator<Map.Entry<String, Integer>>() {
                @Override
                public int compare(Map.Entry<String, Integer> entry1, Map.Entry<String, Integer> entry2) {
                    return entry1.getValue().compareTo(entry2.getValue());
                }
            });
        }
        return entryList;
    }
}
