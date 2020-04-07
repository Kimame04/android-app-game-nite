package com.example.gamenite.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamenite.R;
import com.example.gamenite.interfaces.EventClickListener;
import com.example.gamenite.models.Database;
import com.example.gamenite.models.User;

import java.lang.ref.WeakReference;
import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {

    private Context context;
    private List<String> requests;
    private EventClickListener listener;

    public FriendRequestAdapter(List<String> requests, EventClickListener listener) {
        this.requests = requests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.cards_notification, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String request = requests.get(position);
        User user = Database.findUserbyUid(request);
        float rating = (float) (user.getTotalRating() / user.getNumReviews());
        holder.requesterRating.setText(String.format("%.2f", rating));
        holder.requester.setText(user.getDisplayName());
        holder.description.setText("wants to be your friend");
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView requesterRating;
        private TextView requester;
        private TextView description;
        private Button close;
        private Button accept;
        private CardView cardView;
        private WeakReference<EventClickListener> listenerWeakReference;

        ViewHolder(View itemView, EventClickListener listener) {
            super(itemView);
            requesterRating = itemView.findViewById(R.id.notification_requester_rating);
            requester = itemView.findViewById(R.id.notification_requester);
            description = itemView.findViewById(R.id.notification_description);
            close = itemView.findViewById(R.id.notifications_close);
            accept = itemView.findViewById(R.id.notifications_check);
            cardView = itemView.findViewById(R.id.notifications_cv);

            listenerWeakReference = new WeakReference<>(listener);
            close.setOnClickListener(this);
            accept.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listenerWeakReference.get().onPositionClicked(getAdapterPosition(), v);
        }
    }
}
