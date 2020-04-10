package com.example.gamenite.helpers;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.example.gamenite.models.User;

import java.util.ArrayList;

public class FetchUser extends AsyncTask<Void, Void, User> {
    private Context context;
    private ProgressDialog dialog;
    private ArrayList<User> users;

    public FetchUser(Context context) {
        this.context = context;
    }

    private User fetchCurrentUser() {
        /*for(Iterator<User> iterator = users.iterator();iterator.hasNext();){
            User user = iterator.next();
            if (user.getUid().equals(FirebaseInfo.getFirebaseUser().getUid()))
                return user;
        }*/
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (user.getUid().equals(FirebaseInfo.getFirebaseUser().getUid()))
                return user;
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(context);
        dialog.setMessage("Loading user, please wait...");
        dialog.setCancelable(true);
        dialog.show();
        users = Database.getUsers();
    }

    @Override
    protected User doInBackground(Void... voids) {
        User user = fetchCurrentUser();
        while (user == null)
            user = fetchCurrentUser();
        Database.setCurrentUser(user);
        return user;
    }

    @Override
    protected void onPostExecute(User user) {
        if (this.dialog.isShowing())
            this.dialog.dismiss();
    }

    public ProgressDialog getDialog() {
        return dialog;
    }
}

