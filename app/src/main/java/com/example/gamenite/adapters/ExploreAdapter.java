package com.example.gamenite.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamenite.R;
import com.example.gamenite.helpers.Database;
import com.example.gamenite.interfaces.EventClickListener;
import com.example.gamenite.models.Event;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ExploreAdapter extends RecyclerView.Adapter<ExploreAdapter.ViewHolder> implements Filterable {

    private List<Event> eventList;
    private List<Event> eventListFull;
    private Context context;
    private EventClickListener listener;
    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Event> filteredEvents = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredEvents.addAll(eventListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Event event : eventListFull) {
                    if (event.getTitle().toLowerCase().contains(filterPattern)
                            || event.getDescription().toLowerCase().contains(filterPattern))
                        filteredEvents.add(event);
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredEvents;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            eventList.clear();
            eventList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public ExploreAdapter(List<Event> eventList, EventClickListener listener) {
        this.eventList = eventList;
        this.eventListFull = new ArrayList<>(eventList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExploreAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.cards_event, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final ExploreAdapter.ViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.title.setText(event.getTitle());
        holder.description.setText(event.getDescription());
        holder.status.setText("Date: " + event.getDeadline() + "\n" + (event.getQuota() - event.getInterested()) + " slots remaining" + "\n" + "By: " + Database.findUserbyUid(event.getUid()).getDisplayName());
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView title;
        private TextView description;
        private TextView status;
        private RelativeLayout relativeLayout;
        private com.google.android.material.button.MaterialButton interested;
        private com.google.android.material.button.MaterialButton location;
        private CardView cardView;
        private WeakReference<EventClickListener> listenerWeakReference;

        ViewHolder(View view, EventClickListener listener) {
            super(view);
            relativeLayout = view.findViewById(R.id.events_rl);
            title = view.findViewById(R.id.events_title);
            description = view.findViewById(R.id.events_description);
            status = view.findViewById(R.id.events_status);
            interested = view.findViewById(R.id.events_interested);
            location = view.findViewById(R.id.events_location);
            cardView = view.findViewById(R.id.event_cv);
            listenerWeakReference = new WeakReference<>(listener);

            interested.setOnClickListener(this);
            location.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listenerWeakReference.get().onPositionClicked(getAdapterPosition(), v);
        }
    }

}
