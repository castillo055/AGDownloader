package loc.inhouse.agdownloader.visual;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import loc.inhouse.agdownloader.courses.Course;
import loc.inhouse.agdownloader.courses.CourseFile;
import loc.inhouse.agdownloader.scrapper.AGHandler;
import loc.inhouse.cjconfigmgr.ConfigManager;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Visual implements Initializable {
    public TextField userPrompt;
    public PasswordField passPrompt;
    public ListView classList;
    public ListView fileList;
    public Button syncButton;
    public ProgressBar loadingBar;
    public Label statusIndic;
    public TextField saveToDirLabel;
    public Button cancelBtn;

    private AGHandler agHandler;

    private Thread agThread;

    private SimpleObjectProperty<Course> selectedCourse = new SimpleObjectProperty<>(null);
    private SimpleObjectProperty<CourseFile> selectedCourseFile = new SimpleObjectProperty<>(null);

    private String saveToDir;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        saveToDir = ConfigManager.getConfigVariable("file-dir");
        saveToDirLabel.setText(saveToDir);

        initLists();
    }

    private void resetAGHandler(){
        agHandler = new AGHandler();

        loadingBar.progressProperty().unbind();
        statusIndic.textProperty().unbind();
        loadingBar.progressProperty().bind(agHandler.loadingProgress);
        statusIndic.textProperty().bind(agHandler.statusProperty);
    }

    private void initLists(){
        classList.setCellFactory(listView -> new CourseListCell());
        fileList.setCellFactory(listView -> new CourseFileListCell());

        selectedCourse.addListener((observableValue, course, t1) -> {
            fileList.getItems().clear();
            if(t1!=null) fileList.getItems().addAll(t1.getFilesOfType(CourseFile.Type.FILE));
        });
        /*selectedCourseFile.addListener((observableValue, course, t1) -> {
            agHandler.download(t1);
        });*/

        selectedCourse.bind(classList.getSelectionModel().selectedItemProperty());
        selectedCourseFile.bind(fileList.getSelectionModel().selectedItemProperty());
    }

    public void validateLogin(ActionEvent actionEvent) {
        if(agThread != null && agThread.isAlive()) return;

        resetAGHandler();

        String user = userPrompt.getText();
        String pass = passPrompt.getText();

        agThread = new Thread(() -> {
            Platform.runLater(()->cancelBtn.setDisable(false));
            agHandler.setLogin(user, pass);
            int result = agHandler.login();
            if (result == 0) {
                Platform.runLater(()->{
                    userPrompt.setStyle("");
                    passPrompt.setStyle("");
                });
                agHandler.loadAGPage();
                Platform.runLater(this::displayCourses);
            } else if(result == -1){
                Platform.runLater(()->{
                    userPrompt.setStyle("-fx-control-inner-background: #ff9696");
                    passPrompt.setStyle("-fx-control-inner-background: #ff9696");
                });
            }
            Platform.runLater(()->cancelBtn.setDisable(true));
        });
        agThread.setName("AG-Scrapper");
        agThread.start();
    }

    private void displayCourses(){
        if(agThread.isAlive()) {
            try {
                agThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Course[] courses = agHandler.getLoadedCourses();

        String syncCoursesStr = ConfigManager.getConfigVariable("sync-courses");
        if(syncCoursesStr != null && !syncCoursesStr.equals("none")){
            String[] sC = syncCoursesStr.split(";");
            for(String s: sC) {
                for (Course c : courses) {
                    if(s.trim().equals(c.getName())) c.sync.setSelected(true);
                }
            }
        }

        classList.getItems().clear();
        classList.refresh();

        classList.getItems().addAll(courses);
        classList.refresh();
    }

    private Thread AGDownload = new Thread("AG Download");
    public void syncCourses(ActionEvent actionEvent) {
        if(AGDownload.isAlive()) return;
        if(saveToDir.equals("Select destination folder")){
            saveToDirLabel.setStyle("-fx-control-inner-background: #ff9696");
        }

        AGDownload = new Thread(()-> {
            Platform.runLater(()->cancelBtn.setDisable(false));

            ConfigManager.setConfigVariable("sync-courses", "none");
            ArrayList<Course> coursesToSync = new ArrayList<>();
            for (Course c : (ObservableList<Course>) classList.getItems())
                if (c.sync.isSelected()){
                    coursesToSync.add(c);
                    ConfigManager.setConfigVariable("sync-courses", ConfigManager.getConfigVariable("sync-courses") + "; " + c.getName());
                }
            agHandler.sync(coursesToSync.toArray(new Course[coursesToSync.size()]), saveToDir);

            Platform.runLater(()->cancelBtn.setDisable(true));
        });
        AGDownload.start();
    }

    public void chooseDirectory(ActionEvent actionEvent) {
        DirectoryChooser dC = new DirectoryChooser();

        File saveToDirFile;
        try{
            dC.setInitialDirectory(new File(saveToDir));
            saveToDirFile = dC.showDialog(new Stage(StageStyle.UTILITY));
        } catch (IllegalArgumentException ignored){
            dC.setInitialDirectory(new File(System.getProperty("user.dir")));
            saveToDirFile = dC.showDialog(new Stage(StageStyle.UTILITY));
        }

        if(saveToDirFile==null) return;

        saveToDir = saveToDirFile.toString();
        saveToDirLabel.setText(saveToDir);
        saveToDirLabel.setStyle("");

        ConfigManager.setConfigVariable("file-dir", saveToDir);
    }

    public void cancelOperation(ActionEvent actionEvent) {
        if(agThread.isAlive()) agThread.stop();
        if(AGDownload.isAlive()) AGDownload.stop();

        loadingBar.progressProperty().unbind();
        loadingBar.setProgress(0.0);
        statusIndic.textProperty().unbind();
        statusIndic.setText("CANCELED");
        cancelBtn.setDisable(true);
    }
}

class CourseListCell extends ListCell<Course>{

    protected void updateItem(Course course, boolean empty) {
        super.updateItem(course, empty);
        //setBackground(Background.EMPTY);

        if(empty || course == null) {
            setText(null);
            setGraphic(null);
        } else {
            setTextFill(Color.BLACK);

            setText(course.getDirName());

//            Text icon = GlyphsDude.createIcon(FontAwesomeIcon.BOOK, "15");
//            icon.setFill(Color.valueOf("#fcae1e"));
            setGraphic(course.sync);
        }
    }
}

class CourseFileListCell extends ListCell<CourseFile>{
    protected void updateItem(CourseFile file, boolean empty) {
        super.updateItem(file, empty);
        setBackground(Background.EMPTY);

        if(empty || file == null) {
            setText(null);
            setGraphic(null);
        } else {
            setTextFill(Color.BLACK);

            setText(file.getName());

            Text icon = new Text("");
            switch (file.getType()){
                case FILE:
                    icon = GlyphsDude.createIcon(FontAwesomeIcon.FILE_ALT , "15");
                    break;
                case URL:
                    icon = GlyphsDude.createIcon(FontAwesomeIcon.LINK , "15"); // EXTERNAL LINK ??
                    break;
                case ASSIGNMENT:
                    icon = GlyphsDude.createIcon(FontAwesomeIcon.ARCHIVE , "15"); // EXTERNAL LINK ??
                    break;
                case QUIZ:
                    icon = GlyphsDude.createIcon(FontAwesomeIcon.FILE_ARCHIVE_ALT , "15"); // EXTERNAL LINK ??
                    break;
            }


            icon.setFill(Color.valueOf("#fcae1e"));
            setGraphic(icon);
        }
    }
}
