package com.example.gamenite.helpers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gamenite.models.Event;
import com.example.gamenite.models.User;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class Database {
    private static ArrayList<User> users = new ArrayList<>();
    private static ArrayList<Event> events = new ArrayList<>();
    private static User currentUser;
    private static ChildEventListener userListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            User user = dataSnapshot.getValue(User.class);
            users.add(user);
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            User user = dataSnapshot.getValue(User.class);
            int index = 0;
            for (User user1 : users) {
                if (user.getFirebaseId().equals(dataSnapshot.getKey())) {
                    index = users.indexOf(user1);
                    break;
                }
            }
            users.set(index, user);
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            for(User user: users){
                if(user.getFirebaseId().equals(dataSnapshot.getKey())){
                    users.remove(user);break;}
            }
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) { }
    };
    private static ChildEventListener eventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Event event = dataSnapshot.getValue(Event.class);
            events.add(event);
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Event event = dataSnapshot.getValue(Event.class);
            int index = 0;
            for (Event event1 : events) {
                if (event1.getFirebaseId().equals(dataSnapshot.getKey())) {
                    index = events.indexOf(event1);
                    break;
                }
            }
            events.set(index, event);
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            for (Event event: events){
                if(event.getFirebaseId().equals(dataSnapshot.getKey())){
                    events.remove(event);break;
                }
            }
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) { }
    };
    public static void initDatabase(){
        users.clear();
        events.clear();
        DatabaseReference userReference = FirebaseInfo.getFirebaseDatabase().getReference().child("Users");
        DatabaseReference eventReference = FirebaseInfo.getFirebaseDatabase().getReference().child("Events");
        userReference.addChildEventListener(userListener);
        eventReference.addChildEventListener(eventListener);
    }
    public static User getCurrentUser(){
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User findUser(String friendCode) {
        for(User user: users){
            if (user.getFriendCode().equals(friendCode))
                return user;
        }
        return null;
    }

    public static User findUserbyUid(String uid) {
        for(User user: users){
            if (user.getUid().equals(uid))
                return user;
        }
        return null;
    }

    public static ArrayList<Event> findEventsByUid(String uid) {
        ArrayList<Event> participatingEvents = new ArrayList<>();
        for (Event event : events) {
            if (event.getParticipants().contains(uid))
                participatingEvents.add(event);
        }
        return participatingEvents;
    }
    public static ArrayList<User> getUsers(){return users;}
    public static ArrayList<Event> getEvents(){return events;}

}
