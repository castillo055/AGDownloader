package loc.inhouse.agdownloader.courses;

public class CourseFile {
    public enum Type {
        FILE, QUIZ, URL, FORUM, FEEDBACK, ASSIGNMENT, EXTERNAL_TOOL, UNDEFINED;
    }

    private String name, id;
    private Type type;

    private Course parent;

    public CourseFile(Course parent, String name, String id, String type){
        this.parent = parent;
        this.name = name;
        this.id = id;
        switch (type){
            case "File":
            case "Archivo":
                this.type = Type.FILE;
                break;
            case "URL":
                this.type = Type.URL;
                break;
            case "Forum":
            case "Foro":
                this.type = Type.FORUM;
                break;
            case "Feedback":
                this.type = Type.FEEDBACK;
                break;
            case "Assignment":
                this.type = Type.ASSIGNMENT;
                break;
            case "Herramienta Externa":
            case "External tool":
                this.type = Type.EXTERNAL_TOOL;
                break;
            case "Quiz":
                this.type = Type.QUIZ;
                break;
            default:
                this.type = Type.UNDEFINED;
        }
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public Course getCourse(){
        return parent;
    }
}
