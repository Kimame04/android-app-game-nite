package com.example.gamenite.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamenite.R;
import com.example.gamenite.helpers.Database;
import com.example.gamenite.helpers.FirebaseInfo;
import com.example.gamenite.models.User;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.ViewHolder> {

    private List<String> participants;
    private Context context;
    private boolean isRating;
    private DatabaseReference userReference;

    public ParticipantsAdapter(List<String> uids) {
        this.participants = uids;
        isRating = false;
    }

    public ParticipantsAdapter(List<String> uids, boolean isRating) {
        this.participants = uids;
        this.isRating = isRating;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.cards_participants, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = Database.findUserbyUid(participants.get(position));
        holder.userName.setText(user.getDisplayName());
        userReference = FirebaseInfo.getFirebaseDatabase().getReference().child("Users").child(user.getFirebaseId());
        float rating = (float) user.getTotalRating() / user.getNumReviews();
        holder.userRating.setText(String.format("%.2f", rating) + "");
        holder.sn.setText(position + 1 + ".");
        /*if(isRating){
            holder.ratingBar.setVisibility(View.VISIBLE);
            holder.submit.setVisibility(View.VISIBLE);
            holder.submit.setOnClickListener(v -> {
                Map <String,Object> toUser = new HashMap<>();
                toUser.put("numReviews",user.getNumReviews()+1);
                toUser.put("totalRating",user.getTotalRating()+(double)holder.ratingBar.getRating());
                userReference.updateChildren(toUser);
                holder.cardView.setVisibility(View.GONE);
            });
        }*/
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView userRating;
        private TextView userName;
        private TextView sn;
        private RatingBar ratingBar;
        private Button submit;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            sn = itemView.findViewById(R.id.participants_sn);
            cardView = itemView.findViewById(R.id.participants_cv);
            userRating = itemView.findViewById(R.id.participants_rating);
            userName = itemView.findViewById(R.id.participants_name);
            /*ratingBar = itemView.findViewById(R.id.participants_rating_bar);
            submit = itemView.findViewById(R.id.participants_submit_btn);*/
        }
    }
}
