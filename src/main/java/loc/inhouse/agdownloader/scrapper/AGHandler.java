package loc.inhouse.agdownloader.scrapper;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import loc.inhouse.agdownloader.courses.Course;
import loc.inhouse.agdownloader.courses.CourseFile;
import loc.inhouse.agdownloader.scrapper.http.PersistentCookieJar;
import loc.inhouse.agdownloader.scrapper.http.CookieCache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class AGHandler {
    private static final int HTTP_TIMEOUT = 60;

    private static final String UC3M_URL = "https://login.uc3m.es";
    private static final String AG_URL = "https://aulaglobal.uc3m.es";

    private final uc3mAPI uc3mAPI;
    private final AulaGlobalAPI agAPI;

    private OkHttpClient httpClient;
    private PersistentCookieJar cookieJar;

    private String loginTicket;

    private String username, password;

    private ArrayList<Course> courses = new ArrayList<>();

    public SimpleDoubleProperty loadingProgress = new SimpleDoubleProperty(0.0);
    public StringProperty statusProperty = new SimpleStringProperty("");
    private int numCourses = 0;

    public AGHandler(){
        this.cookieJar = new PersistentCookieJar(new CookieCache());

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .followRedirects(false)
                .followSslRedirects(false)
                .writeTimeout(HTTP_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(HTTP_TIMEOUT, TimeUnit.SECONDS);
        httpClient = builder.build();

        Retrofit uc3mApiRest = new Retrofit.Builder()
                .baseUrl(UC3M_URL)
                .client(httpClient)
                .build();
        uc3mAPI = uc3mApiRest.create(uc3mAPI.class);

        Retrofit agApiRest = new Retrofit.Builder()
                .baseUrl(AG_URL)
                .client(httpClient)
                .build();
        agAPI = agApiRest.create(AulaGlobalAPI.class);
    }

    /*public void loadMainUc3mPage(){
        try {
            Response<ResponseBody> response = this.uc3mAPI.requestMainPage().execute();
            if(response.code() == 302){
                System.out.println("Page Loaded");
            } else {
                System.err.printf("Error %d while loading page", response.code());
            }
        } catch (IOException | RuntimeException e){
            e.printStackTrace();
        }
    }*/

    public void setLogin(String user, String pass){
        this.username = user;
        this.password = pass;
    }
    public int login(){
        try {
            Response<ResponseBody> uc3mResponse = this.uc3mAPI.senLoginInfo(
                    "https://aulaglobal.uc3m.es/login/index.php",
                    "es", "authn", username, password).execute();
            if(uc3mResponse.code() == 200){
                System.out.println("[[ WRONG PASSWORD ]]");
                return -1;
            } else if (uc3mResponse.code() != 302) {
                System.err.printf("Error %d while loging in", uc3mResponse.code());
                return -2;
            }

            System.out.println("[[ ATTEMPTING LOGIN ]]");
            loginTicket = uc3mResponse.headers().get("Location");
            loginTicket = loginTicket.substring(loginTicket.indexOf('=')+1);
            System.out.println("[LOGIN TICKET] " + loginTicket);

            Response<ResponseBody> agResponse = this.agAPI.sendLoginTicket(loginTicket).execute();
            if(agResponse.code()!=302) {
                System.err.println("Ticket not Accepted " + agResponse.code());
                return -3;
            }

            System.out.println("[TICKET ACCEPTED]");

            Response<ResponseBody> agResponse2 = this.agAPI.sendTestsessionRq().execute();
            if(agResponse2.code()==303) {
                System.out.println("[[ LOGIN COMPLETE ]]");
                return 0;
            }else {
                System.err.println("[[ LOGIN FAILED ]]");
                return -4;
            }
        } catch (IOException | RuntimeException e){
            e.printStackTrace();
        }

        return -5;
    }

    public void loadAGPage(){
        this.courses.clear();
        try {
            Response<ResponseBody> response = this.agAPI.requestMainPage().execute();
            ResponseBody responseBody = response.body();
            if(response.isSuccessful() && responseBody != null){
                System.out.println("Page Loaded");

                Document doc = Jsoup.parse(responseBody.string());
                String query = "p.coursename";
                Elements courses = doc.select(query);
                numCourses = courses.size();

                int i = 0;
                for(Element element: courses){
                    Node courseNode = element.childNode(0);
                    String courseID = courseNode.attr("href").substring(46);
                    String courseName = courseNode.unwrap().toString();

                    //====================================================
                    //System.out.printf("%s -> %s\n", courseName, courseID);
                    Course course = new Course(courseName, courseID);
                    this.courses.add(0, course);

                    final int j = i++;
                    Platform.runLater(()->{
                        statusProperty.set(course.getDirName() + " <- CHECKING...");
                        loadingProgress.setValue((double)j/(double)numCourses);
                    });

                    loadCourse(courseID);
                    //====================================================
                }

                Platform.runLater(()->{
                    statusProperty.set("");
                    loadingProgress.set(0.0);
                });
            } else {
                System.err.printf("Error %d while loading page", response.code());
            }
        } catch (IOException | RuntimeException e){
            e.printStackTrace();
        }
    }

    public void loadCourse(String id){
        try {
            Response<ResponseBody> response = this.agAPI.requestCoursePage(id).execute();
            ResponseBody responseBody = response.body();
            if(response.isSuccessful() && responseBody != null){
                //System.out.println("Course Loaded");

                Document doc = Jsoup.parse(responseBody.string());
                String query = "div.activityinstance";
                Elements activities = doc.select(query);
                int numActivities = activities.size();

                for(Element element: activities){
                    String[] url = element.getElementsByAttribute("href").attr("href").split("/");
                    String urlType = url[4];
                    String urlID = url[5].substring(url[5].indexOf("=")+1);

                    Elements entry = element.getElementsByClass("instancename");
                    String name = entry.textNodes().get(0).text();
                    String type = entry.get(0).childrenSize()>0? entry.get(0).child(0).text() : "";

                    //====================================================
                    //System.out.println(type + "\t" + urlID);
                    //System.out.printf("\t%70S\t%S\t\t%s\n", name, type, urlID);
                    Course course = courses.get(0);

                    CourseFile file = new CourseFile(course, name, urlID, type);

                    course.addFile(file);
                    //====================================================

                    Platform.runLater(()->loadingProgress.setValue(loadingProgress.getValue() + 1.0/(double)(numActivities*numCourses)));
                }
            } else {
                System.err.printf("Error %d while loading page", response.code());
            }
        } catch (IOException | RuntimeException e){
            e.printStackTrace();
        }
    }

    public void download(CourseFile file, String dest){
        if(file == null || file.getType() != CourseFile.Type.FILE) return;

        String id = file.getId();
        String ref = AG_URL + "/course/view.php?id=" + file.getCourse().getId();

        try {
            Response<ResponseBody> fileLocResponse = this.agAPI.requestResourceLoc(ref, id).execute();
            if(fileLocResponse.code() == 303){
                //System.out.println("Course Loaded");
            } else {
                System.err.printf("Error %d while loading page\n", fileLocResponse.code());
                return;
            }

            String fileLocation = fileLocResponse.headers().get("Location");

            String extension = fileLocation.substring(fileLocation.lastIndexOf('.'));

            Request fileRequest = new Request.Builder().url(fileLocation).build();
            okhttp3.Response fileResponse = httpClient.newCall(fileRequest).execute();
            if(!fileResponse.isSuccessful()){
                System.err.printf("Error %d while downloading file\n", fileLocResponse.code());
                return;
            }

            FileOutputStream fos = new FileOutputStream(dest + extension);
            fos.write(fileResponse.body().bytes());
            fos.close();

            fileResponse.close();
        } catch (IOException | RuntimeException e){
            e.printStackTrace();
        }
    }

    public void sync(Course[] courses, String destDir){
        int courseCount = courses.length;
        int i = 0;

        File localDir = new File(destDir);
        if(!localDir.exists()) localDir.getAbsoluteFile().mkdirs();

        for(Course c: courses){
            CourseFile[] files = c.getFiles();

            int fileCount = files.length;

            final int j = i++;
            Platform.runLater(()-> loadingProgress.setValue((double)(j)/(double)(courseCount)));

            File courseDir = new File(localDir.getAbsolutePath() + "/" + c.getDirName().replace('/', '_'));
            if(!courseDir.exists()) courseDir.getAbsoluteFile().mkdir();

            for(CourseFile cF: c.getFiles()){
                if(!cF.getType().equals(CourseFile.Type.FILE)) continue;

                final String cfName = cF.getName();
                Platform.runLater(()->{
                    statusProperty.setValue(cfName + " <- DOWNLOADING...");
                    loadingProgress.setValue(loadingProgress.getValue() + 1.0/(double)(courseCount*fileCount));
                });

                File file = new File(courseDir.getAbsolutePath() + "/" + cF.getName().replaceAll("/", "_").replaceAll(":", "-"));

                download(cF, file.getAbsolutePath());
            }
        }
        Platform.runLater(()->{
            loadingProgress.setValue(0.0);
            statusProperty.setValue("");
        });
    }

    public Course[] getLoadedCourses(){
        return courses.toArray(new Course[courses.size()]);
    }
}
