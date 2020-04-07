package com.example.gamenite.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamenite.R;

import java.util.ArrayList;

public class EventUpdateAdapter extends RecyclerView.Adapter<EventUpdateAdapter.ViewHolder> {
    private Context context;
    private ArrayList<ArrayList<String>> updates;

    public EventUpdateAdapter(ArrayList<ArrayList<String>> updates) {
        this.updates = updates;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.cards_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ArrayList<String> update = updates.get(position);
        holder.close.setVisibility(View.GONE);
        holder.accept.setVisibility(View.GONE);
        holder.requesterRating.setVisibility(View.GONE);
        holder.host.setPadding(20, 0, 0, 15);
        holder.description.setPadding(20, 15, 0, 15);
        holder.host.setText("Event: " + update.get(0));
        holder.description.setText(update.get(1));
    }

    @Override
    public int getItemCount() {
        return updates.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView requesterRating;
        private TextView host;
        private TextView description;
        private Button close;
        private Button accept;
        private CardView cardView;
        private LinearLayout linearLayout;

        ViewHolder(View itemView) {
            super(itemView);
            requesterRating = itemView.findViewById(R.id.notification_requester_rating);
            host = itemView.findViewById(R.id.notification_requester);
            description = itemView.findViewById(R.id.notification_description);
            close = itemView.findViewById(R.id.notifications_close);
            accept = itemView.findViewById(R.id.notifications_check);
            cardView = itemView.findViewById(R.id.notifications_cv);
            linearLayout = itemView.findViewById(R.id.notifications_ll);
        }

    }
}
