package loc.inhouse.agdownloader;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.stage.Stage;
import loc.inhouse.agdownloader.visual.Visual;

public class Launcher extends Application {

    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/visual.fxml"));
        Parent root = loader.load();
        Visual controller = loader.getController();

        Scene scene = new Scene(root, 1080, 620, false, SceneAntialiasing.BALANCED);
        /*PerformanceTracker tracker = PerformanceTracker.getSceneTracker(scene);
        new Thread(() -> {
            while(true){
                System.out.println(tracker.getInstantPulses());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //tracker.resetAverageFPS();
            }
        }).start();*/

        primaryStage.setTitle("AG Downloader");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e->{
            //controller.exitApplication();
            Platform.exit();
            System.exit(0);
        });
    }
}
