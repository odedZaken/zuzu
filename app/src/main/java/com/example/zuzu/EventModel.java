package com.example.zuzu;

import com.google.android.gms.maps.model.LatLng;

import java.util.UUID;

public class EventModel {

    private String id, title, description, type, time, creatorEmail, date;
    private int maxParticipants;
    private LatLng location;

    public EventModel() {
    }

    public EventModel(String title, String description, String type, String time, String date, String creatorEmail, int maxParticipants, LatLng location) {
        this.id = UUID.randomUUID().toString();         //Creates a unique Id for every new event
        this.title = title;
        this.date = date;
        this.description = description;
        this.type = type;
        this.time = time;
        this.creatorEmail = creatorEmail;
        this.maxParticipants = maxParticipants;
        this.location = location;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
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

    public void setId(String id) { this.id = id; }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }
}
