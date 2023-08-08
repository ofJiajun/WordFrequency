package wordfrequency;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
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

public class WordCount {
  ConcurrentHashMap<String, Integer> overallStats = new ConcurrentHashMap<>();

  public List<Map.Entry<String, Integer>> count(String file, int nParts, boolean order) throws IOException {
    // 分割文件
    long a = System.currentTimeMillis();
    List<File> parts = splitFile(file, nParts);
    long b = System.currentTimeMillis();
    System.out.println("文件切割用时： " + (b - a));

    // 创建N个任务
    List<Thread> threads = new ArrayList<>();
    for (File part : parts) {
      Thread t = new Thread(new CountTask(part, overallStats));
      threads.add(t);
    }

    // 启动所有任务线程
    for (Thread t : threads) {
      t.start();
    }

    // 等待所有任务线程结束
    for (Thread t : threads) {
      try {
        t.join();
      } catch (InterruptedException e) {
        // 重新设置中断标志
        Thread.currentThread().interrupt();
      }
    }

    // 对结果排序;
    List<Map.Entry<String, Integer>> result = sortByValue(overallStats, order);

    return result;
  }

  private List<File> splitFile(String file, int nParts) throws IOException {
    long totalLines = Files.lines(Paths.get(file)).count();
    long perSize = totalLines / nParts;
    List<File> parts = new ArrayList<>();
    String fileName = new File(file).getName();
    Files.createDirectory(Paths.get("./temp"));
    for (int i = 1; i <= nParts; i++) {
      File part = new File("./temp/" + fileName + "_" + i);
      parts.add(part);
    }

    int currentLine = 0;
    int currentPart = 0;

    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line = null;
      while ((line = reader.readLine()) != null) {
        if (currentPart == nParts) {
          break;
        }
        // 当前行写到当前部分文件
        writeLine(parts.get(currentPart), line);

        // 行数++,检查是否该换文件了
        currentLine++;
        if (currentLine % perSize == 0) {
          currentPart++;
        }
      }
    }

    return parts;

  }

  private void writeLine(File partFile, String line) throws IOException {
    try {
      // 打开指定部分文件的PrintWriter
      PrintWriter writer = new PrintWriter(new FileWriter(partFile, true));

      // 将行写入该部分文件
      writer.println(line);

      // 关闭writer
      writer.close();
    } catch (IOException e) {
      System.out.println("`writeLine()` method exception: ");
      e.printStackTrace();
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
      // 对该部分文件统计词频
      try {
        long a = System.currentTimeMillis();
        ConcurrentHashMap<String, Integer> partStats = countWords(filePart);
        // 添加到总结果中
        for (Map.Entry<String, Integer> entry : partStats.entrySet()) {
          // 同已存在的值累加
          stats.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }
        long b = System.currentTimeMillis();
        System.out.println("单次任务用时: " + (b - a));
      } catch (IOException e) {
        System.out.println("`countWords()` method reads File exception.");
        e.printStackTrace();
      }
    }

    private ConcurrentHashMap<String, Integer> countWords(File filePart) throws IOException {
      // count the time of the program running
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
        String pattern = "[\\p{P}\\s\\d\\\\p{Han}\\|]+";
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