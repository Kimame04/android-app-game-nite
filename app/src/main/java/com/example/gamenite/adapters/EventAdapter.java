package com.example.gamenite.adapters;

import android.content.Context;
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
import com.example.gamenite.interfaces.EventClickListener;
import com.example.gamenite.models.Chip;
import com.example.gamenite.models.Event;
import com.example.gamenite.models.User;
import com.google.android.material.chip.ChipGroup;

import java.lang.ref.WeakReference;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private List<Event> eventList;
    private Context context;

    private final EventClickListener listener;
    private Event event;
    private User currentUser;

    public EventAdapter(List<Event> eventList, EventClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.cards_event,parent,false);
        ViewHolder viewHolder = new ViewHolder(view, listener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull EventAdapter.ViewHolder holder, int position) {
        event = eventList.get(position);
        holder.title.setText(event.getTitle());
        holder.description.setText(event.getDescription());
        for (Chip chip : event.getChips()) {
            com.google.android.material.chip.Chip c = new com.google.android.material.chip.Chip(context);
            c.setText(chip.getName());
            c.setClickable(false);
            c.setCheckable(false);
            holder.chipGroup.addView(c);
        }
        currentUser = Database.getCurrentUser();
        String displayName = Database.findUserbyUid(event.getUid()).getDisplayName();
        boolean isOrganiser = event.getUid().equals(currentUser.getUid());
        boolean hasPassed = LocalDateTime.parse(event.getDeadline(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).isBefore(LocalDateTime.now());
        if (hasPassed && isOrganiser) {
            holder.cancel.setText("Conclude this event");
            holder.location.setVisibility(View.GONE);
        } else if (hasPassed) {
            holder.cancel.setText("Rate this event");
            holder.location.setVisibility(View.GONE);
        } else if (isOrganiser) {
            holder.cancel.setText("Cancel event");
            holder.makeUpdate.setVisibility(View.VISIBLE);
        } else
            holder.cancel.setText("Not interested");
        holder.status.setText("Date: "+ event.getDeadline()+"\n"+(event.getQuota()-event.getInterested()) + " slots remaining"+"\n"+"By: "+displayName);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView title;
        private TextView description;
        private TextView status;
        private RelativeLayout relativeLayout;
        private com.google.android.material.button.MaterialButton cancel;
        private com.google.android.material.button.MaterialButton location;
        private CardView cardView;
        private com.google.android.material.button.MaterialButton makeUpdate;
        private WeakReference<EventClickListener> listenerWeakReference;
        private ChipGroup chipGroup;

        ViewHolder(View view, EventClickListener listener) {
            super(view);
            listenerWeakReference = new WeakReference<>(listener);
            relativeLayout = view.findViewById(R.id.events_rl);
            title = view.findViewById(R.id.events_title);
            description = view.findViewById(R.id.events_description);
            status = view.findViewById(R.id.events_status);
            cancel = view.findViewById(R.id.events_interested);
            location = view.findViewById(R.id.events_location);
            cardView = view.findViewById(R.id.event_cv);
            makeUpdate = view.findViewById(R.id.events_update);
            chipGroup = view.findViewById(R.id.events_cg);

            makeUpdate.setOnClickListener(this);
            cancel.setOnClickListener(this);
            location.setOnClickListener(this);
            cardView.setOnClickListener(this);
        }

        public void onClick(View view) {
            listenerWeakReference.get().onPositionClicked(getAdapterPosition(), view);
        }
    }

}
