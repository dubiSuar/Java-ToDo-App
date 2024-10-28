package com.example.taskly;

public class Task {
    private String title;
    private String description;
    private String priority;
    private String date;

    public Task() {
        // Required empty constructor for Firebase
    }

    public Task(String title, String description, String priority, String date) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.date = date;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPriority() { return priority; }
    public String getDate() { return date; }
}
