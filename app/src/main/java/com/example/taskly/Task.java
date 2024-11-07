package com.example.taskly;

public class Task {
    private String title;
    private String description;
    private String priority;
    private String date;
    private String id; // Assuming you have an id field

    // No-argument constructor
    public Task() {
        // Default constructor required for calls to DataSnapshot.getValue(Task.class)
    }

    // Parameterized constructor
    public Task(String title, String description, String priority, String date, String id) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.date = date;
        this.id = id;
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
