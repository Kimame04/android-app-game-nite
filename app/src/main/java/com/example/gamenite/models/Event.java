package com.example.gamenite.models;

public class Event {
    private String deadline;
    private String title;
    private String description;
    private int stage;
    private int quota;
    private int interested;

    public Event(String deadline, String title, String description, int stage, int quota, int interested) {
        this.deadline = deadline;
        this.title = title;
        this.description = description;
        this.stage = stage;
        this.quota = quota;
        this.interested = interested;
    }

    public Event(){}

    public String getDeadline() {
        return deadline;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getStage() {
        return stage;
    }

    public int getQuota() {
        return quota;
    }

    public int getInterested() {
        return interested;
    }
}
