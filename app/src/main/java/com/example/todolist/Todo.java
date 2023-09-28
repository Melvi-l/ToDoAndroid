package com.example.todolist;

public class Todo {
    private int id;
    private String title;

    public Todo(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public Todo(String title) {
        this(-1, title);
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String toString() {
        return id + ":" + title;
    }

    public static Todo fromString(String s) {
        String[] parts = s.split(":");
        if (parts.length == 2) {
            int id = Integer.parseInt(parts[0]);
            String title = parts[1];
            return new Todo(id, title);
        } else {
            return null;
        }
    }
}
