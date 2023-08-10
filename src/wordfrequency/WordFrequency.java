package wordfrequency;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class WordFrequency {
    public List<Map.Entry<String, Integer>> getWordFrequency(String textPath, boolean order)
            throws IOException {
        WordCount wc = new WordCount();
        List<Map.Entry<String, Integer>> entryList;
        long size = Files.size(Paths.get(textPath)) / 1024;
        if (size <= 50) {
            entryList = wc.count(textPath, 1, order);
        } else if ((size > 50) && (size < Integer.MAX_VALUE)) {
            // more files will ask more new threads
            entryList = wc.count(textPath, 10, order);
        } else {
            return null;
        }

        // clear temp files

        return entryList;
    }
}
