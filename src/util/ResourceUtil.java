package util;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ResourceUtil {
  private final static String RSRC_PATH_FROM_CURRENT_DIR = "style";

  public static URL getResourceURL(String inResourcesPath) {
    var FileString = (RSRC_PATH_FROM_CURRENT_DIR + "/resources/" + inResourcesPath).replace("/", File.separator);
    try {
      return new File(FileString).getCanonicalFile().toURI().toURL();
    } catch (IOException e) {
      System.err.println("Cannot fetch URL for '" + inResourcesPath + "'");
      System.err.println("""
          If the path is correct, try to adapt the
                  RSRC_PATH_FROM_CURRENT_DIR constant in class
                        ResourceUtil""".stripIndent()); // 裁剪缩进
      e.printStackTrace(System.err);
      return null;
    }
  }

  public static String getResourceURLStr(String inResourcesPath) {
    return getResourceURL(inResourcesPath).toString();
  }

  public static String getResource(String inResourcesPath) {
    var FileString = (RSRC_PATH_FROM_CURRENT_DIR + "/resources/" + inResourcesPath).replace("/", File.separator);
    return new File(FileString).getAbsolutePath();
  }
}