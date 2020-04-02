package com.example.gamenite.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamenite.R;
import com.example.gamenite.helpers.Database;
import com.example.gamenite.models.Event;
import com.example.gamenite.helpers.FirebaseInfo;
import com.example.gamenite.helpers.GenerateDialog;
import com.example.gamenite.models.User;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExploreAdapter extends RecyclerView.Adapter<ExploreAdapter.ViewHolder> implements GenerateDialog {

    private List<Event> eventList;
    private Context context;

    public ExploreAdapter(List<Event>eventList){ this.eventList = eventList; }

    @NonNull
    @Override
    public ExploreAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.cards_event,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ExploreAdapter.ViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.title.setText(event.getTitle());
        holder.description.setText(event.getDescription());
        ArrayList<User> users = Database.getUsers();
        User currentUser = Database.getCurrentUser();
        String displayName="";
        for (User user : users){
            if(user.getUid().equals(event.getUid()))displayName = user.getDisplayName();
        }
        DatabaseReference eventsReference = FirebaseInfo.getFirebaseDatabase().getReference().child("Events").child(event.getFirebaseId());
        holder.status.setText("Date: "+ event.getDeadline()+"\n"+(event.getQuota()-event.getInterested()) + " slots remaining"+"\n"+"By: "+displayName);
        holder.interested.setOnClickListener(v -> {
            Map<String,Object> updateEvent = new HashMap<>();
            int updatedInterested = event.getInterested() + 1;
            updateEvent.put("interested",updatedInterested);
            ArrayList<String> strings = event.getParticipants();
            strings.add(currentUser.getUid());
            updateEvent.put("participants",strings);
            eventsReference.updateChildren(updateEvent);
            holder.cardView.setVisibility(View.GONE);
            Snackbar.make(holder.relativeLayout,"Added to your events tab.", BaseTransientBottomBar.LENGTH_SHORT).show();
        });
        holder.location.setOnClickListener(v -> {
            Uri uri = Uri.parse("geo:"+event.getLat()+", "+event.getLongi()+"?q="+event.getLat()+", "+event.getLongi());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW,uri);
            mapIntent.setPackage("com.google.android.apps.maps");
            context.startActivity(mapIntent);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView title;
        private TextView description;
        private TextView status;
        private RelativeLayout relativeLayout;
        private com.google.android.material.button.MaterialButton interested;
        private com.google.android.material.button.MaterialButton location;
        private CardView cardView;
        public ViewHolder(View view){
            super(view);
            relativeLayout = view.findViewById(R.id.events_rl);
            title = view.findViewById(R.id.events_title);
            description = view.findViewById(R.id.events_description);
            status = view.findViewById(R.id.events_status);
            interested = view.findViewById(R.id.events_interested);
            location = view.findViewById(R.id.events_location);
            cardView = view.findViewById(R.id.event_cv);
        }
    }

}
