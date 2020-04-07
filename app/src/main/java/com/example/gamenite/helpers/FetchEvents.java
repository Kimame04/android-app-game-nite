package com.example.gamenite.helpers;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.example.gamenite.models.Database;
import com.example.gamenite.models.Event;

import java.util.ArrayList;

public class FetchEvents extends AsyncTask<Void, Void, ArrayList<Event>> {
    private int position;
    private ProgressDialog dialog;
    private Context context;

    public FetchEvents(int position, Context context) {
        this.context = context;
        this.position = position;
    }

    public FetchEvents(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(context);
        dialog.setMessage("Fetching events, please wait...");
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected ArrayList<Event> doInBackground(Void... voids) {
        ArrayList<Event> events = Database.getEvents();
        while (events == null)
            events = Database.getEvents();
        return events;
    }

    protected ProgressDialog getDialog() {
        return dialog;
    }

    protected int getPosition() {
        return position;
    }

}

