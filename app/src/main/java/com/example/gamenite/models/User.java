package com.example.gamenite.models;

import com.example.gamenite.helpers.FirebaseInfo;

import java.util.ArrayList;
import java.util.Random;

public class User {
    private String displayName;
    private String uid;
    private String bio;
    private String email;
    private String firebaseId;
    private double totalRating;
    private int numReviews;
    private String friendCode;
    private int numFriends;
    private int numHosted;
    private int numParticipated;
    private ArrayList<String> tagsList = new ArrayList<>();
    private ArrayList<String> friendRequestList = new ArrayList<>();
    private ArrayList<String> friendList = new ArrayList<>();
    public User(){
        this.displayName = FirebaseInfo.getFirebaseUser().getDisplayName();
        this.uid = FirebaseInfo.getFirebaseUser().getUid();
        this.email = FirebaseInfo.getFirebaseUser().getEmail();
        this.bio = "hello";
        this.totalRating = 5.0;
        this.numReviews = 1;
        this.friendCode = generateFriendCode();
        this.numFriends = 0;
        this.numHosted = 0;
        this.numParticipated = 0;
    }

    private String generateFriendCode(){
        Random random = new Random();
        String result = "";
        for(int i = 0; i<4;i++){
            for(int j = 0; j<4; j++)
                result+=random.nextInt(10);
            if(i!=3)
                result+="-";
        }
        return result;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public double getTotalRating() {
        return totalRating;
    }

    public void setTotalRating(double totalRating) {
        this.totalRating = totalRating;
    }

    public int getNumReviews() {
        return numReviews;
    }

    public void setNumReviews(int numReviews) {
        this.numReviews = numReviews;
    }

    public String getFriendCode() {
        return friendCode;
    }

    public void setFriendCode(String friendCode) {
        this.friendCode = friendCode;
    }

    public int getNumFriends() {
        return numFriends;
    }

    public void setNumFriends(int numFriends) {
        this.numFriends = numFriends;
    }

    public int getNumHosted() {
        return numHosted;
    }

    public void setNumHosted(int numHosted) {
        this.numHosted = numHosted;
    }

    public int getNumParticipated() {
        return numParticipated;
    }

    public void setNumParticipated(int numParticipated) {
        this.numParticipated = numParticipated;
    }

    public ArrayList<String> getFriendRequestList() {
        return friendRequestList;
    }

    public void setFriendRequestList(ArrayList<String> friendRequestList) {
        this.friendRequestList = friendRequestList;
    }

    public ArrayList<String> getFriendList() {
        return friendList;
    }

    public void setFriendList(ArrayList<String> friendList) {
        this.friendList = friendList;
    }
}
