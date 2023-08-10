package wordfrequency;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import org.mozilla.universalchardet.UniversalDetector;

public class WordCount {
  ConcurrentHashMap<String, Integer> overallStats = new ConcurrentHashMap<>();
  Path tempPath = null;

  public List<Map.Entry<String, Integer>> count(String file, int nParts, boolean order)
      throws IOException, MalformedInputException {
    // split file into n
    List<File> parts = splitFile(file, nParts);

    // create n tasks
    List<Thread> threads = new ArrayList<>();
    for (File part : parts) {
      Thread t = new Thread(new CountTask(part, overallStats));
      threads.add(t);
    }

    // start all task threads
    for (Thread t : threads) {
      t.start();
    }

    // wait for all task threads finishing
    for (Thread t : threads) {
      try {
        t.join();
      } catch (InterruptedException e) {
        // reset the interrupted sign
        Thread.currentThread().interrupt();
      }
    }

    // sourt out the result
    List<Map.Entry<String, Integer>> result = sortByValue(overallStats, order);

    // delete temp files if they exist
    if (tempPath != null) {
      deleteFolder(new File(tempPath.toString()));
    }
    return result;
  }

  public void deleteFolder(File folder) {
    File[] files = folder.listFiles();
    if (files != null) {
      for (File f : files) {
        if (f.isDirectory()) {
          deleteFolder(f);
        } else {
          f.delete();
        }
      }
    }
    folder.delete();
  }

  private List<File> splitFile(String file, int nParts) throws IOException {
    // check the file's encoding
    Charset charset = Charset.forName("UTF-8");
    try {
      UniversalDetector detector = new UniversalDetector(null);
      BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
      byte[] buf = new byte[4096];
      int nread;
      while ((nread = bis.read(buf)) > 0 && !detector.isDone()) {
        detector.handleData(buf, 0, nread);
      }
      bis.close();
      detector.dataEnd();
      String encoding = detector.getDetectedCharset();
      if (encoding != null) {
        System.out.println("Detected encoding = " + encoding);
        charset = Charset.forName(encoding);
      } else {
        System.out.println("No encoding detected.");
      }

      detector.reset();
    } catch (IOException e) {
      e.printStackTrace();
    }

    // count the number of all lines of the files
    long totalLines = Files.lines(Paths.get(file), charset).count();
    long perSize = totalLines / nParts;
    List<File> parts = new ArrayList<>();
    String fileName = new File(file).getName();
    Path parentDirectory = Paths.get(".");
    Path tempDirectory = Files.createTempDirectory(parentDirectory, "temp");
    tempPath = tempDirectory;
    for (int i = 1; i <= nParts; i++) {
      File part = tempDirectory.resolve(fileName + "_" + i).toFile();
      parts.add(part);
    }

    int currentLine = 0;
    int currentPart = 0;

    try (BufferedReader reader = Files.newBufferedReader(Paths.get(file), charset)) {
      while (true) {
        // prevent from out of the bounds
        if (currentPart == nParts) {
          break;
        }

        String line = reader.readLine();
        // current line write out to the current partial file
        writeLine(parts.get(currentPart), line);

        // increase the number of lines then check out whether it should change to next
        // file
        currentLine++;
        if (currentLine % perSize == 0) {
          currentPart++;
        }
      }
    }

    return parts;
  }

  private void writeLine(File partFile, String line) throws IOException {
    // open specified partial file's PrintWriter
    try (PrintWriter writer = new PrintWriter(new FileWriter(partFile, true))) {

      // output current line into the partial file
      writer.println(line);
    }
  }

  private List<Map.Entry<String, Integer>> sortByValue(Map<String, Integer> wordFrequence,
      boolean order) {
    // sort and output
    List<Map.Entry<String, Integer>> entryList = new ArrayList<>(wordFrequence.entrySet());

    // sort entryList
    Collections.sort(entryList, new Comparator<Map.Entry<String, Integer>>() {
      @Override
      public int compare(Map.Entry<String, Integer> entry1, Map.Entry<String, Integer> entry2) {
        return entry2.getValue().compareTo(entry1.getValue());
      }
    });

    if (order == false) {
      Collections.reverse(entryList);
    }

    return entryList;
  }

  class CountTask implements Runnable {
    private File filePart;
    private ConcurrentHashMap<String, Integer> stats;

    public CountTask(File filePart, ConcurrentHashMap<String, Integer> stats) {
      this.filePart = filePart;
      this.stats = stats;
    }

    public void run() {
      // count the word frequency of the partial file
      try {
        ConcurrentHashMap<String, Integer> partStats = countWords(filePart);
        // merge new value into old value
        for (Map.Entry<String, Integer> entry : partStats.entrySet()) {
          // add new value to old value
          stats.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }
      } catch (IOException e) {
        System.out.println("`countWords()` method reads File exception.");
        e.printStackTrace();
      }
    }

    private ConcurrentHashMap<String, Integer> countWords(File filePart) throws IOException {

      // get text
      StringBuilder stringBuilder = new StringBuilder();

      try (BufferedReader br = new BufferedReader(new FileReader(filePart))) {
        String line;
        while ((line = br.readLine()) != null) {
          stringBuilder.append(line).append(System.lineSeparator());
        }
      }

      // Split the content of the StringBuilder into words
      String[] wordsStr = stringBuilder.toString().split("\\b");

      List<String> words = new LinkedList<>(Arrays.asList(wordsStr));
      // ignoring cap case, clear the repeating element of string array
      Set<String> wordsSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
      wordsSet.addAll(words);

      ConcurrentHashMap<String, Integer> wordFrequence = new ConcurrentHashMap<>();
      for (String s1 : wordsSet) {
        int count = 0;
        // match all punctuation
        String pattern = "[\\p{P}\\s\\d\\p{script=Han}\\|]+";
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
        words.remove(s1);
      }
      return wordFrequence;
    }
  }
}