package view;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import util.ResourceUtil;

public class Test {
  public static void main(String[] args) throws IOException {
    String url = ResourceUtil.getResourceURLStr("1.txt");
    System.out.println("url: " + url);
  }
}