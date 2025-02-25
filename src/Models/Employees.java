package Models;

import java.util.List;

public class Employees {
    private int id;
    private String fullName;
    private List<String> currentBooks;
    private List<String> pastBooks;

    public Employees(int id, String fullName, List<String> currentBooks, List<String> pastBooks) {
        this.id = id;
        this.fullName = fullName;
        this.currentBooks = currentBooks;
        this.pastBooks = pastBooks;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public List<String> getCurrentBooks() {
        return currentBooks;
    }

    public List<String> getPastBooks() {
        return pastBooks;
    }
}

