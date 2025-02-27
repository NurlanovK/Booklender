package Models;

import java.util.List;

public class Employees {
    private int id;
    private String fullName;
    private String email;
    private String password;
    private List<String> currentBooks;
    private List<String> pastBooks;

    public Employees(int id, String fullName, String email, String password, List<String> currentBooks, List<String> pastBooks) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.currentBooks = currentBooks;
        this.pastBooks = pastBooks;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public List<String> getCurrentBooks() {
        return currentBooks;
    }

    public List<String> getPastBooks() {
        return pastBooks;
    }
}


