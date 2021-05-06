package com.example.zuzu;

import java.util.UUID;

public class EventModel {

    private String id, title, description, type, time, location, creatorEmail;
    private int maxParticipants;

    public EventModel() {
    }

    public EventModel(String title, String description, String type, String time, String location, String creatorEmail, int maxParticipants) {
        this.id = UUID.randomUUID().toString();         //Creates a unique Id for every new event
        this.title = title;
        this.description = description;
        this.type = type;
        this.time = time;
        this.location = location;
        this.creatorEmail = creatorEmail;
        this.maxParticipants = maxParticipants;
    }

    public String getId() {
        return id;
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCreatorEmail() {
        return creatorEmail;
    }

    public void setCreatorEmail(String creatorEmail) {
        this.creatorEmail = creatorEmail;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }
}
