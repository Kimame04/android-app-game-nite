package com.example.gamenite.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamenite.R;
import com.example.gamenite.adapters.ExploreAdapter;
import com.example.gamenite.helpers.Database;
import com.example.gamenite.helpers.FetchEvents;
import com.example.gamenite.helpers.FirebaseInfo;
import com.example.gamenite.models.Event;
import com.example.gamenite.models.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExploreFragment extends Fragment {
    private RecyclerView recyclerView;
    private ExploreAdapter exploreAdapter;
    private ArrayList<Event> toShowList = new ArrayList<>();
    private ArrayList<Event> events = new ArrayList<>();

    private void generateEvents(RecyclerView recyclerView) {
        User currentUser = Database.getCurrentUser();
        toShowList.clear();
        for (Event event : events) {
            if(shouldBeShown(event))
                toShowList.add(event);
        }
        exploreAdapter = new ExploreAdapter(toShowList, (position, view) -> {
            Event event = toShowList.get(position);
            DatabaseReference eventsReference = FirebaseInfo.getFirebaseDatabase().getReference().child("Events").child(event.getFirebaseId());
            switch (view.getId()) {
                case R.id.events_interested:
                    new MaterialAlertDialogBuilder(getContext()).setTitle("Add to events").setMessage("Indicate interest in the event?")
                            .setPositiveButton("Yes", ((dialog, which) -> {
                                event.addParticipant(currentUser.getUid());
                                Map<String, Object> updateEvent = new HashMap<>();
                                updateEvent.put("interested", event.getParticipants().size());
                                updateEvent.put("participants", event.getParticipants());
                                eventsReference.updateChildren(updateEvent);
                                generateEvents(recyclerView);
                                Snackbar.make(getView(), "Added to your events tab.", BaseTransientBottomBar.LENGTH_SHORT).show();
                            })).setNegativeButton("No", null).show();
                    break;
                case R.id.events_location:
                    Uri uri = Uri.parse("geo:" + event.getLat() + ", " + event.getLongi() + "?q=" + event.getLat() + ", " + event.getLongi());
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    getContext().startActivity(mapIntent);
                    break;
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(exploreAdapter);
    }

    private boolean shouldBeShown(Event event) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime eventDeadline = LocalDateTime.parse(event.getDeadline(),dateTimeFormatter);
        if(eventDeadline.minusHours(24).isBefore(now))
            return false;
        if (event.getInterested() >= event.getQuota())
            return false;
        for(String uid: event.getParticipants()){
            if(uid.equals(FirebaseInfo.getFirebaseUser().getUid()))
                return false;
        }
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore,container,false);
        getActivity().setTitle("Explore");
        setHasOptionsMenu(true);
        recyclerView = view.findViewById(R.id.explore_recycler_view);
        new FetchEvent(getContext()).execute();
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_explore_refresh:
                new FetchEvent(getContext()).execute();
                Snackbar.make(getView(), "Events refreshed.", BaseTransientBottomBar.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_explore, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_explore_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (exploreAdapter != null)
                    exploreAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    private class FetchEvent extends FetchEvents {

        FetchEvent(Context context) {
            super(context);
        }

        @Override
        protected void onPostExecute(ArrayList<Event> events1) {
            if (this.getDialog().isShowing())
                this.getDialog().dismiss();
            events = events1;
            generateEvents(recyclerView);
        }
    }
}
