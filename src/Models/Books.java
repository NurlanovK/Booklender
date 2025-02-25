package Models;


public class Books {
    private int id;
    private String title;
    private String author;
    private String description;
    private String status;
    private String cover;

    public Books(int id, String title, String author, String description, String status, String cover) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.description = description;
        this.status = status;
        this.cover = cover;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getCover() { return cover; }
}


