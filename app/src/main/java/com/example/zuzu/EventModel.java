package com.example.zuzu;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;

public class EventModel implements Serializable {

    private String id, title, description, type, time, creatorId, date;
    private int maxParticipants, currParticipants, distance;
    private LatLng location;
    private ArrayList<String> usersIDs;

    public EventModel() {
    }

    public EventModel(String title, String description, String type, String time, String date, String creatorId, int maxParticipants, LatLng location) {
        this.id = UUID.randomUUID().toString();         //Creates a unique Id for every new event
        this.title = title;
        this.date = date;
        this.description = description;
        this.type = type;
        this.time = time;
        this.creatorId = creatorId;
        this.maxParticipants = maxParticipants;
        this.location = location;
        this.currParticipants = 1;
        this.distance = 100;
        usersIDs = new ArrayList<>();
        usersIDs.add(this.creatorId);
    }

    public ArrayList<String> getUsersIDs() {
        return usersIDs;
    }

    public void setUsersIDs(ArrayList<String> usersIDs) {
        this.usersIDs = usersIDs;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
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

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
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

    public int getCurrParticipants() {
        return currParticipants;
    }

    public void setCurrParticipants(int currParticipants) {
        this.currParticipants = currParticipants;
    }

    public String getParticipantsStr() {
        return currParticipants + "/" + maxParticipants;
    }

    public String getDistanceStr() {
        String result;
        DecimalFormat df = new DecimalFormat("0.0");
        if (distance >= 1000) {
            //Distance to event is above 1000 meters
            float f = distance / 1000f;
            result = df.format(f) + "km";
        } else {
            result = distance + "m";
        }
        return result;
    }
}
