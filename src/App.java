
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;
import util.ResourceUtil;
import view.UI;

public class App extends Application {
  public static HostServices hostServices;

  public static void main(String[] args) {
    Application.launch(args);
  }

  public void start(Stage stage) {
    // play a sound when the app starts
    var soundUrl = ResourceUtil.getResourceURLStr("appear.mp3");
    AudioClip startAudio = new AudioClip(soundUrl);
    startAudio.play();

    // initialize the main controls
    hostServices = getHostServices();
    UI mainUI = new UI(hostServices);
    mainUI.stage = stage;

    // add an icon image
    var icoPath = ResourceUtil.getResourceURLStr("rabbity.ico");
    Image image = new Image(icoPath);
    stage.getIcons().add(image);

    // add the css for main controls
    var cssUrl = ResourceUtil.getResourceURLStr("css/styles.css");
    Scene scene = new Scene(mainUI);
    scene.getStylesheets().add(cssUrl);

    // other operations
    stage.setScene(scene);
    stage.setTitle("词频统计");
    stage.show();
  }
}
