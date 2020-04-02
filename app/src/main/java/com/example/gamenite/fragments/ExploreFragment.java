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
import com.example.gamenite.helpers.Database;
import com.example.gamenite.models.Event;
import com.example.gamenite.adapters.ExploreAdapter;
import com.example.gamenite.helpers.FirebaseInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ExploreFragment extends Fragment {
    private DatabaseReference databaseReference;
    private ArrayList<Event> eventArrayList;
    private ArrayList<Event> toShowList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ExploreAdapter exploreAdapter;
    private Context context;
    private ChildEventListener eventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Event event = dataSnapshot.getValue(Event.class);
            eventArrayList.add(event);
            generateEvents(recyclerView);
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            recyclerView.removeAllViews();
            generateEvents(recyclerView);
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { dataSnapshot.getRef().child(dataSnapshot.getKey()).removeValue(); }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) { }
    };

    public void generateEvents(RecyclerView recyclerView){
        toShowList.clear();
        for(Event event: eventArrayList){
            if(shouldBeShown(event))
                toShowList.add(event);
        }
        exploreAdapter = new ExploreAdapter(toShowList);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(exploreAdapter);
    }

    public boolean shouldBeShown(Event event){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime eventDeadline = LocalDateTime.parse(event.getDeadline(),dateTimeFormatter);
        if(eventDeadline.minusHours(24).isBefore(now))
            return false;
        if(event.getInterested()==event.getQuota())
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
        eventArrayList = Database.getEvents();
        recyclerView = view.findViewById(R.id.explore_recycler_view);
        context = getContext();
        databaseReference = FirebaseInfo.getFirebaseDatabase().getReference().child("Events");
        databaseReference.addChildEventListener(eventListener);
        return view;
    }
}
