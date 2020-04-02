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
import com.google.firebase.database.DatabaseReference;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> implements GenerateDialog {

    private List<Event> eventList;
    private Context context;

    public EventAdapter(List<Event>eventList){ this.eventList = eventList; }

    @NonNull
    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.cards_event,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final EventAdapter.ViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.title.setText(event.getTitle());
        holder.description.setText(event.getDescription());
        ArrayList<User> users = Database.getUsers();
        User currentUser = Database.getCurrentUser();
        String displayName="";
        for (User user : users){
            if(user.getUid().equals(event.getUid()))displayName = user.getDisplayName();
        }
        if(!event.getUid().equals(currentUser.getUid()))
            holder.cancel.setText("Not interested");
        DatabaseReference eventsReference = FirebaseInfo.getFirebaseDatabase().getReference().child("Events").child(event.getFirebaseId());
        holder.status.setText("Date: "+ event.getDeadline()+"\n"+(event.getQuota()-event.getInterested()) + " slots remaining"+"\n"+"By: "+displayName);
        holder.cancel.setOnClickListener(v -> {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime deadline = LocalDateTime.parse(event.getDeadline(),dateTimeFormatter);
            if(deadline.minusHours(24).isBefore(now)){
                if (generateConfirmationDialog(context, "Cancellation of event","This will result in a 0-star rating. Do you still want to continue?")){
                    //TODO Handle rating change
                }
            }
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
        private com.google.android.material.button.MaterialButton cancel;
        private com.google.android.material.button.MaterialButton location;
        private CardView cardView;
        public ViewHolder(View view){
            super(view);
            relativeLayout = view.findViewById(R.id.events_rl);
            title = view.findViewById(R.id.events_title);
            description = view.findViewById(R.id.events_description);
            status = view.findViewById(R.id.events_status);
            cancel = view.findViewById(R.id.events_interested);
            location = view.findViewById(R.id.events_location);
            cardView = view.findViewById(R.id.event_cv);
        }
    }

}
