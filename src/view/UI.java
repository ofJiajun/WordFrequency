package view;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import util.ResourceUtil;
import wordfrequency.WordFrequency;

public class UI extends GridPane {
  public Stage stage;
  private HostServices hostServices;
  private ListView<String> resultLv;
  List<Map.Entry<String, Integer>> countingResult;
  private WordFrequency wf = new WordFrequency();
  private StringProperty importPath = new SimpleStringProperty("");
  private StringProperty exportPath = new SimpleStringProperty("");

  public UI(HostServices hostServices) {
    this.hostServices = hostServices;
    layoutForm();
  }

  private void layoutForm() {
    // right controls
    Button importBtn = new Button("导入");
    importBtn.setOnAction(e -> addSelectionBox(true, importPath));
    TextField importPathTfld = new TextField();
    importPathTfld.setPromptText("鍵入文件路徑或點擊“导入”按钮");
    importPathTfld.textProperty().bindBidirectional(importPath);

    VBox rightPane = new VBox();
    rightPane.getChildren().addAll(importBtn, importPathTfld);

    // left controls
    Button startBtn = new Button("开始计算");
    startBtn.setOnAction(e -> showOrderDialog());
    Button exportBtn = new Button("导出");
    exportBtn.setOnAction(e -> showExportDialog());
    VBox leftPane = new VBox();
    leftPane.getChildren().addAll(startBtn, exportBtn);

    // central controls
    Label listLbl = new Label("结果");
    resultLv = new ListView<>();
    resultLv.setAccessibleText("结果");
    searchWord(resultLv);
    VBox centralPane = new VBox();
    centralPane.getChildren().addAll(listLbl, resultLv);

    // add a image and a button clearing the list
    var url = ResourceUtil.getResourceURLStr("resources/rabbity.ico");
    Image image = new Image(url);
    ImageView imageView = new ImageView(image);
    Button clearBtn = new Button("点击清除列表");
    clearBtn.setOnAction(e -> resultLv.getItems().clear());
    VBox upCentralPane = new VBox();
    upCentralPane.getChildren().addAll(imageView, clearBtn);

    // Set properties to achieve image stretching
    imageView.setPreserveRatio(true);
    imageView.fitWidthProperty().bind(upCentralPane.widthProperty()); // Bind fitWidth to pane's width
    imageView.fitHeightProperty().bind(upCentralPane.heightProperty()); // Bind fitHeight to pane's height

    // add All three VBox into this GridPane
    this.add(leftPane, 0, 0);
    this.add(rightPane, 2, 0);
    this.add(upCentralPane, 1, 0);
    // make vb1, vb2 and vb4 fill tthe rest space vertically
    GridPane.setVgrow(rightPane, Priority.ALWAYS);
    GridPane.setVgrow(leftPane, Priority.ALWAYS);
    GridPane.setVgrow(upCentralPane, Priority.ALWAYS);

    // add vb3 to a alone line which is accross three columns
    this.add(centralPane, 0, 1, 2, 1);
    GridPane.setValignment(centralPane, VPos.CENTER);
    // make vb3 fills the rest space vertically
    GridPane.setVgrow(centralPane, Priority.ALWAYS);
  }

  private void addSelectionBox(boolean os, StringProperty path) {
    FileChooser fileChooser = new FileChooser();
    // fileChooser.setTitle("Select File");
    fileChooser.setInitialDirectory(new File("."));
    FileChooser.ExtensionFilter fileFilter = new FileChooser.ExtensionFilter("All Files", "*.*");
    fileChooser.getExtensionFilters().add(fileFilter);
    if (os == true) {
      File file = fileChooser.showOpenDialog(stage);
      if (file != null) {
        path.set(file.getAbsolutePath());
      }
    } else {
      File file = fileChooser.showSaveDialog(stage);
      if (file != null) {
        path.set(file.getAbsolutePath());
      }
    }
  }

  private void showOrderDialog() {
    TextField orderTfld = new TextField();
    orderTfld.setPromptText("請輸入詞頻排序（0 為升序， 1 為降序)");
    orderTfld.setTextFormatter(orderFormatter());
    Button okBtn = new Button("确认");
    okBtn.setOnAction(e -> countFrequency(importPath.get(), orderTfld));

    HBox hb = new HBox();
    hb.getChildren().addAll(orderTfld, okBtn);
    Scene scene = new Scene(hb);
    var url = ResourceUtil.getResourceURLStr("css/styles.css");
    scene.getStylesheets().add(url);
    stage = new Stage();
    stage.setScene(scene);
    stage.setTitle("设置词频排序");
    stage.showAndWait();
  }

  private boolean countFrequency(String path, TextField orderTfld) {
    // ask user to input a correct path
    if (!path.matches("^[A-Za-z]:\\\\([^\\*|\"<>/?]*\\\\)*[^\\*|\"<>/?]*$")) {

      resultLv.getItems().clear();
      resultLv.getItems().add("請指定正確的文件来源路徑");
      resultLv.requestFocus();
      stage.close();
      return false;
    }
    // ask user to input a correct order
    String order = orderTfld.getText();
    if (!order.matches("[01]")) {
      resultLv.getItems().clear();
      resultLv.getItems().add("请指定正确的词频排列顺序");
      resultLv.requestFocus();
      stage.close();
      return false;
    }
    stage.close();
    if (new File(path).isFile()) {
      // run i/o operation in background thread
      resultLv.getItems().clear();
      resultLv.getItems().add("正在计算，请稍后...");
      Thread ioThread = new Thread(() -> {
        // count the result and sort out it
        try {
          if (order.equals("0")) {
            countingResult = wf.getWordFrequency(path, false);
          } else {
            countingResult = wf.getWordFrequency(path, true);
          }
        } catch (IOException e) {
          System.out.println("IO1:" + e);
        }

        // after I/O, using Platform.runLater() to update UI
        Platform.runLater(() -> {
          long a = System.currentTimeMillis();
          resultLv.getItems().clear();
          for (Map.Entry<String, Integer> entry : countingResult) {
            String word = entry.getKey();
            int frequency = entry.getValue();
            String item = word + ": " + frequency;
            resultLv.getItems().add(item);
          }
          long b = System.currentTimeMillis();
          System.out.println("添加用时: " + (b - a));
        });
      });
      ioThread.start();
    } else {
      resultLv.getItems().clear();
      resultLv.getItems().add("路徑錯誤或文件不存在");
      resultLv.requestFocus();
      stage.close();
      return false;
    }
    return true;
  }

  private void showExportDialog() {
    // set the category of export
    ToggleGroup exportGroup = new ToggleGroup();
    RadioButton exportAll = new RadioButton("全部导出");
    exportAll.setToggleGroup(exportGroup);
    RadioButton exportRange = new RadioButton("指定范围导出");
    exportRange.setToggleGroup(exportGroup);
    VBox vb1 = new VBox();
    vb1.getChildren().addAll(exportAll, exportRange);

    // if the category is exportRange radio button, ask user input the range thich
    // is expected
    TextField minTfld = new TextField();
    TextField maxTfld = new TextField();
    minTfld.setPromptText("最小值：請指定希望導出的詞頻範圍（x-y, 包含 x 和 y）");
    maxTfld.setPromptText("最大值：請指定希望導出的詞頻範圍（x-y, 包含 x 和 y）");

    // bind two TextFields' visible property to the selected property of the
    // exportRange radio button
    minTfld.visibleProperty().bind(exportRange.selectedProperty());
    maxTfld.visibleProperty().bind(exportRange.selectedProperty());

    // set the input of TextFields to be only number
    minTfld.setTextFormatter(numberFormatter());
    maxTfld.setTextFormatter(numberFormatter());

    // a path TextField and a button which browses and check out the path saving
    Button browseBtn = new Button("浏览导出位置");
    browseBtn.setOnAction(e -> addSelectionBox(false, exportPath));
    TextField exportPathTfld = new TextField();
    exportPathTfld.setPromptText("鍵入導出路徑或點擊 Browse button");
    exportPathTfld.textProperty().bindBidirectional(exportPath);
    VBox vb2 = new VBox();
    vb2.getChildren().addAll(browseBtn, exportPathTfld);

    // a TextField sets the ascending or descending order
    TextField orderTfld = new TextField();
    orderTfld.setPromptText("請輸入詞頻排序（0 為升序， 1 為降序)");
    orderTfld.setTextFormatter(orderFormatter());
    // a button executes the exporting logic

    Button okBtn = new Button("确认");

    // add clicked event to the OK button according to the radio button above
    exportAll.selectedProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal) {
        okBtn.setOnAction(e -> exportFrequency(exportPath.get(), orderTfld));
      }
    });
    exportRange.selectedProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal) {
        okBtn.setOnAction(e -> exportFrequency(exportPath.get(), orderTfld, minTfld, maxTfld));
      }
    });
    // show the all controls
    HBox hb = new HBox();
    hb.getChildren().addAll(vb1, minTfld, maxTfld, vb2, orderTfld, okBtn);
    Scene scene = new Scene(hb);
    var url = ResourceUtil.getResourceURLStr("css/styles.css");
    scene.getStylesheets().add(url);
    stage = new Stage();
    stage.setScene(scene);
    stage.setTitle("設置導出類型");
    stage.showAndWait();
  }

  private TextFormatter<String> numberFormatter() {
    TextFormatter<String> formatter = new TextFormatter<>(change -> {
      /*
       * 1. 获取本次变化的输入文本内容(change.getText())
       * 2. 使用replaceAll方法,找出其中所有非数字的字符([^0-9])
       * 3. 用空字符串替换匹配到的非数字字符
       * 4. 返回只含有数字的字符串
       */
      change.setText(change.getText().replaceAll("[^0-9]", ""));
      return change;
    });
    return formatter;
  }

  private TextFormatter<String> orderFormatter() {
    TextFormatter<String> formatter = new TextFormatter<>(change -> {
      change.setText(change.getText().replaceAll("[^01]", ""));
      // constrain the length of TextField input to 1 charac 5tter
      if (change.getControlNewText().length() > 1) {
        return null;
      }
      return change;
    });
    return formatter;
  }

  private boolean exportFrequency(String path, TextField orderTfld) {
    // match a correct path
    // ^[A-Za-z]:\\([^*\|"<>?]*\\)*[^*\|"<>?]*$
    // java style ： "^[A-Za-z]:\\\\([^\\*|\"<>/?]*\\\\)*[^\\*|\"<>/?]*$"
    if (!path.matches("^[A-Za-z]:\\\\([^\\*|\"<>/?]*\\\\)*[^\\*|\"<>/?]*$")) {
      resultLv.getItems().clear();
      resultLv.getItems().add("請指定正確的文件導出路徑");
      resultLv.requestFocus();
      stage.close();
      return false;
    }
    // ask user to input a correct order
    String order = orderTfld.getText();
    if (!order.matches("[01]")) {
      resultLv.getItems().clear();
      resultLv.getItems().add("请指定正確的導出順序");
      resultLv.requestFocus();
      stage.close();
      return false;
    }
    stage.close();
    if (new File(importPath.get()).isFile()) {
      resultLv.getItems().clear();
      resultLv.getItems().add("正在导出，请稍后...");
      Thread ioThread = new Thread(() -> {
        try {
          if (order.equals("0")) {
            countingResult = wf.getWordFrequency(importPath.get(), false);
          } else {
            countingResult = wf.getWordFrequency(importPath.get(), true);
          }
        } catch (IOException e) {
          System.out.println("IO2: " + e);
        }

        // output the result
        try {
          long a = System.currentTimeMillis();
          String word;
          int frequency;
          String result;
          BufferedWriter writer = Files.newBufferedWriter(Paths.get(exportPath.get()));
          writer.write("word|frequency");
          writer.newLine();
          for (Map.Entry<String, Integer> entry : countingResult) {
            word = entry.getKey();
            frequency = entry.getValue();
            result = word + ": " + frequency;
            writer.write(result);
            writer.newLine();
          }
          writer.close();
          long b = System.currentTimeMillis();
          System.out.println("输出用时: " + (b - a));
        } catch (IOException e) {
          System.out.println("output error1: " + e);
        }
        Platform.runLater(() -> {
          long a = System.currentTimeMillis();
          resultLv.getItems().clear();
          resultLv.getItems().add("導出成功！路徑： " + exportPath.get());
          for (Map.Entry<String, Integer> entry : countingResult) {
            String word = entry.getKey();
            int frequency = entry.getValue();
            String item = word + ": " + frequency;
            resultLv.getItems().add(item);
          }
          long b = System.currentTimeMillis();
          System.out.println("添加用时: " + (b - a));
        });
      });
      ioThread.start();
    } else {
      resultLv.getItems().clear();
      resultLv.getItems().add("請指定正確的文件來源路徑");
      resultLv.requestFocus();
      stage.close();
      return false;
    }
    return true;
  }

  private boolean exportFrequency(String path, TextField orderTfld, TextField minTfld, TextField maxTfld) {
    // match a correct path
    // ^[A-Za-z]:\\([^*\|"<>?]*\\)*[^*\|"<>?]*$
    // java style ： "^[A-Za-z]:\\\\([^\\*|\"<>/?]*\\\\)*[^\\*|\"<>/?]*$"
    if (!path.matches("^[A-Za-z]:\\\\([^\\*|\"<>/?]*\\\\)*[^\\*|\"<>/?]*$")) {
      resultLv.getItems().clear();
      resultLv.getItems().add("請指定正確的文件導出路徑");
      resultLv.requestFocus();
      stage.close();
      return false;
    }
    // ask user to input the frequency of min and max, which user wants to output
    String minStr = minTfld.getText();
    String maxStr = maxTfld.getText();
    if (!minStr.matches("^[1-9][0-9]*$") || !maxStr.matches("^[1-9][0-9]*$")
        || (Integer.parseInt(minStr) > Integer.parseInt(maxStr))) {
      resultLv.getItems().clear();
      resultLv.getItems().add("請正確指定預導出的詞頻範圍");
      resultLv.requestFocus();
      stage.close();
      return false;
    }
    // ask user to input a correct order
    String order = orderTfld.getText();
    if (!order.matches("[01]")) {
      resultLv.getItems().clear();
      resultLv.getItems().add("请指定正確的導出順序");
      resultLv.requestFocus();
      stage.close();
      return false;
    }
    stage.close();
    if (new File(importPath.get()).isFile()) {
      resultLv.getItems().clear();
      resultLv.getItems().add("正在导出，请稍后...");
      Thread ioThread = new Thread(() -> {
        try {
          if (order.equals("0")) {
            countingResult = wf.getWordFrequency(importPath.get(), false);
          } else {
            countingResult = wf.getWordFrequency(importPath.get(), true);
          }
        } catch (IOException e) {
          System.out.println("IO3: " + e);
        }

        // output the result
        List<String> rangeList = new ArrayList<>(); // store the counting result which outputs into the list
        try {
          String word;
          int frequency;
          String result;
          BufferedWriter writer = Files.newBufferedWriter(Paths.get(exportPath.get()));
          writer.write("word|frequency");
          writer.newLine();
          for (Map.Entry<String, Integer> entry : countingResult) {
            word = entry.getKey();
            frequency = entry.getValue();
            if (frequency >= Integer.parseInt(minStr) && frequency <= Integer.parseInt(maxStr)) {
              result = word + "|" + frequency;
              writer.write(result);
              writer.newLine();
              rangeList.add(result);
            }
          }
          writer.close();
        } catch (IOException e) {
          System.out.println("output error2: " + e);
        }
        Platform.runLater(() -> {
          long a = System.currentTimeMillis();
          resultLv.getItems().clear();
          resultLv.getItems().add("導出成功！路徑： " + exportPath.get());
          for (String result : rangeList) {
            resultLv.getItems().add(result);
          }
          long b = System.currentTimeMillis();
          System.out.println("添加用时: " + (b - a));
        });
      });
      ioThread.start();
    } else {
      resultLv.getItems().clear();
      resultLv.getItems().add("請指定正確的文件來源路徑");
      resultLv.requestFocus();
      stage.close();
      return false;
    }

    return true;

  }

  private void searchWord(ListView<String> lv) {
    lv.setOnKeyPressed(event -> {
      String selectedItem = resultLv.getSelectionModel().getSelectedItem();
      if (event.getCode() == KeyCode.ENTER && selectedItem != null) {
        String word = selectedItem.split("\\:|\\|")[0].trim();
        if (word.matches("\\b\\w+\\b")) {
          String url = "https://www.youdao.com/result?word=" + word + "&lang=en";
          hostServices.showDocument(url);
        }
      }
    });
  }
}