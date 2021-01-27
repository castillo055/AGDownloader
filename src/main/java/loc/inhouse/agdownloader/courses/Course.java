package loc.inhouse.agdownloader.courses;

import javafx.scene.control.CheckBox;

import java.util.ArrayList;

public class Course {
    private String name, id;
    private ArrayList<CourseFile> files = new ArrayList<>();

    public CheckBox sync = new CheckBox();

    public Course(String name, String id){
        this.name = name;
        this.id = id;
    }

    public CourseFile[] getFiles(){
        return files.toArray(new CourseFile[files.size()]);
    }
    public CourseFile[] getFilesOfType(CourseFile.Type type){
        return files.stream().filter(cF -> cF.getType().equals(type)).toArray(CourseFile[]::new);
    }
    public String getId(){return id;}
    public String getName(){return name;}
    public String getDirName(){
        try{
            return name.substring(name.indexOf(' ')+1, name.indexOf('/')-3);
        } catch (IndexOutOfBoundsException e){
            return name;
        }
    }
    public void addFile(CourseFile cF){ files.add(cF);}
}
