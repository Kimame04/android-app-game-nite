package com.example.gamenite.helpers;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.example.gamenite.models.Database;
import com.example.gamenite.models.User;

public class FetchUser extends AsyncTask<Void, Void, User> {
    private Context context;
    private ProgressDialog dialog;

    public FetchUser(Context context) {
        this.context = context;
    }

    private User fetchCurrentUser() {
        for (User user : Database.getUsers()) {
            if (user.getUid().equals(FirebaseInfo.getFirebaseUser().getUid()))
                return user;
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(context);
        dialog.setMessage("Loading user, please wait...");
        dialog.setCancelable(false);
        dialog.show();
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
        if (dialog.isShowing())
            dialog.dismiss();
    }

    public ProgressDialog getDialog() {
        return dialog;
    }
}

