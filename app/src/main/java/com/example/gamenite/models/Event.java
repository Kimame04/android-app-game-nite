package com.example.gamenite.models;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class Event {
    private String deadline;
    private String title;
    private double longi;
    private double lat;
    private String description;
    private String uid;
    private int quota;
    private int interested;
    private String firebaseId;
    private ArrayList<String> participants = new ArrayList<>();

    public Event(String title, String description, int quota, LatLng latLng, String deadline, String uid){
        this.deadline = deadline;
        this.title = title;
        this.description = description;
        this.quota = quota;
        this.interested = 1;
        this.longi = latLng.longitude;
        this.lat = latLng.latitude;
        this.uid = uid;
        participants.add(uid);
    }

    public Event(){}

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getLongi() {
        return longi;
    }

    public void setLongi(double longi) {
        this.longi = longi;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getQuota() {
        return quota;
    }

    public void setQuota(int quota) {
        this.quota = quota;
    }

    public int getInterested() {
        return interested;
    }

    public void setInterested(int interested) {
        this.interested = interested;
    }

    public String getFirebaseId(){return this.firebaseId;}

    public void setFirebaseId(String firebaseId){this.firebaseId = firebaseId;}

    public ArrayList<String> getParticipants() {
        return participants;
    }

    public void setParticipants(ArrayList<String> participants) {
        this.participants = participants;
    }
}
