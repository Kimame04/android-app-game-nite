package com.example.gamenite.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamenite.R;
import com.example.gamenite.adapters.EventUpdateAdapter;
import com.example.gamenite.helpers.Database;
import com.example.gamenite.models.Event;

import java.util.ArrayList;
import java.util.Collections;

public class EventUpdateFragment extends Fragment {
    private Context context;
    private ArrayList<ArrayList<String>> updates = new ArrayList<>();
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_updates, container, false);
        context = getContext();
        recyclerView = view.findViewById(R.id.notification_event_update_rv);
        generateUpdates();
        return view;
    }

    private void generateUpdates() {
        if (Database.getCurrentUser() != null) {
            updates.clear();
            ArrayList<Event> events = Database.findEventsByUid(Database.getCurrentUser().getUid());
            for (Event event : events) {
                for (String string : event.getUpdates()) {
                    ArrayList<String> temp = new ArrayList<>();
                    temp.add(event.getTitle());
                    temp.add(string);
                    updates.add(temp);
                }
            }
            Collections.reverse(updates);
            EventUpdateAdapter adapter = new EventUpdateAdapter(updates);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(adapter);
        }

    }
}
