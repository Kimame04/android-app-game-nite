package com.example.gamenite.models;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gamenite.helpers.CreateNotification;
import com.example.gamenite.helpers.Database;
import com.example.gamenite.helpers.FirebaseInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class CheckUpdateService extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public CheckUpdateService() {
        super("updateService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Context context = this;
        ArrayList<Event> participatingEvents = Database.findEventsByUid(Database.getCurrentUser().getUid());
        for (Event event : participatingEvents) {
            CreateNotification.createNotificationChannel(CreateNotification.CHANNEL_TWO, "Participant Updates", "Notify user of attendance changes", this);
            CreateNotification.createNotificationChannel(CreateNotification.CHANNEL_THREE, "Event Updates", "Notify user of updates by event organiser", this);
            DatabaseReference participants = FirebaseInfo.getFirebaseDatabase().getReference().child("Events").child(event.getFirebaseId()).child("participants");
            participants.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    CreateNotification.createNotification(CreateNotification.CHANNEL_TWO, context
                            , "New participant!", "Someone new " +
                                    "joined " + event.getTitle() + "!", "Someone new " +
                                    "joined " + event.getTitle() + "!", 2, CreateNotification.GROUP_ONE, CreateNotification.EVENT_FRAGMENT);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    CreateNotification.createNotification(CreateNotification.CHANNEL_TWO, context
                            , "Someone left :/!", "Let's have an F.",
                            "Let's have an F.", 3, CreateNotification.GROUP_ONE, CreateNotification.EVENT_FRAGMENT);
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
            DatabaseReference updates = FirebaseInfo.getFirebaseDatabase().getReference().child("Events").child(event.getFirebaseId()).child("updates");
            updates.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    CreateNotification.createNotification(CreateNotification.CHANNEL_THREE,
                            context, "Event Update",
                            "Click me to check it out!",
                            "The organiser has updated event " + event.getTitle()
                            , 4, CreateNotification.GROUP_TWO, CreateNotification.UPDATES_FRAGMENT);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }
}
