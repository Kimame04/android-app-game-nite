package com.example.gamenite.helpers;

import android.content.Context;

import com.example.gamenite.models.User;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseInfo {
    private static FirebaseDatabase firebaseDatabase;
    private static FirebaseUser firebaseUser;
    private static User currentUser;

    public FirebaseInfo(FirebaseUser firebaseUser, FirebaseDatabase firebaseDatabase){
        FirebaseInfo.firebaseDatabase = firebaseDatabase;
        FirebaseInfo.firebaseUser = firebaseUser;
        Database.initDatabase();
    }

    public static FirebaseDatabase getFirebaseDatabase() {
        return firebaseDatabase;
    }

    public static void setFirebaseDatabase(FirebaseDatabase firebaseDatabase) {
        FirebaseInfo.firebaseDatabase = firebaseDatabase;
    }

    public static FirebaseUser getFirebaseUser() {
        return firebaseUser;
    }

    public static void setFirebaseUser(FirebaseUser firebaseUser) {
        FirebaseInfo.firebaseUser = firebaseUser;
    }

    public static void logoutUser(Context context){
        AuthUI.getInstance().signOut(context);
    }
}
