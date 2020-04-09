package com.example.gamenite.helpers;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.example.gamenite.models.User;

public class FetchUser extends AsyncTask<Void, Void, User> {
    private Context context;
    private ProgressDialog dialog;

    public FetchUser(Context context) {
        this.context = context;
    }

    private User fetchCurrentUser() {
        return Database.findUserbyUid(FirebaseInfo.getFirebaseUser().getUid());
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(context);
        dialog.setMessage("Loading user, please wait...");
        dialog.setCancelable(true);
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
        if (this.dialog.isShowing())
            this.dialog.dismiss();
    }

    public ProgressDialog getDialog() {
        return dialog;
    }
}

