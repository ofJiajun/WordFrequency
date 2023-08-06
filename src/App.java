
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import view.UI;
import util.ResourceUtil;

public class App extends Application {
  public static HostServices hostServices;

  public static void main(String[] args) {
    Application.launch(args);
  }

  public void start(Stage stage) {
    hostServices = getHostServices();
    UI mainUI = new UI(hostServices);
    mainUI.stage = stage;
    var icoPath = ResourceUtil.getResourceURLStr("rabbity.ico");
    Image image = new Image(icoPath);
    Scene scene = new Scene(mainUI);
    var url = ResourceUtil.getResourceURLStr("css/styles.css");
    scene.getStylesheets().add(url);
    stage.setScene(scene);
    stage.getIcons().add(image);
    stage.setTitle("词频统计");
    stage.show();
  }
}
