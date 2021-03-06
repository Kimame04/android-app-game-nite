package com.example.gamenite.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamenite.R;
import com.example.gamenite.adapters.ParticipantsAdapter;
import com.example.gamenite.helpers.CheckConnection;
import com.example.gamenite.helpers.Database;
import com.example.gamenite.helpers.FetchUser;
import com.example.gamenite.helpers.FirebaseInfo;
import com.example.gamenite.models.Chip;
import com.example.gamenite.models.User;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {
    private TextView bio;
    private TextView profileName;
    private TextView avgRating;
    private TextView email;
    private LinearLayout linearLayout;
    private LinearLayout dialogLl;
    private LinearLayout friendLl;
    private RatingBar ratingBar;
    private TextView numReviews;
    private TextView friendCode;
    private TextView numFriends;
    private TextView numHosted;
    private TextView numPartipated;
    private TextView charCounter;
    private TextView friendsTitle;
    private Button editBio;
    private EditText changeBio;
    private Context context;
    private LayoutInflater layoutInflater;
    private AlertDialog dialog;
    private AlertDialog friendDialog;
    private EditText friendCodeEt;
    private static final int maxTagChars = 20;
    private static final int maxChars = 240;
    private Button addTag;
    private View.OnClickListener addTagListener = v -> {
        androidx.appcompat.app.AlertDialog addTagDialog = new androidx.appcompat.app.AlertDialog.Builder(getContext()).create();
        addTagDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View view = getLayoutInflater().inflate(R.layout.dialog_change_bio, null);
        TextView addTagTv = view.findViewById(R.id.dialog_bio_tv);
        TextView addTagChars = view.findViewById(R.id.dialog_name_chars);
        addTagTv.setText("Add New Tag");
        EditText addTagEt = view.findViewById(R.id.dialog_name_et);
        addTagEt.setHint("Keep it short!");
        addTagEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addTagChars.setText(maxTagChars - s.length() + "");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        Button submitTagBtn = view.findViewById(R.id.dialog_name_btn);
        submitTagBtn.setText("Submit!");
        submitTagBtn.setOnClickListener(v1 -> {
            boolean doesTagExist = false;
            for (Chip chip : Database.getChips()) {
                if (chip.getName().toLowerCase().trim().equals(addTagEt.getText().toString().toLowerCase().trim())) {
                    doesTagExist = true;
                    break;
                }
            }
            if (addTagEt.getText().toString().equals(""))
                Snackbar.make(view, "Cannot submit empty input.", BaseTransientBottomBar.LENGTH_SHORT).show();
            else if (doesTagExist)
                Snackbar.make(view, "Tag already exists.", BaseTransientBottomBar.LENGTH_SHORT).show();
            else {
                Chip chip = new Chip(addTagEt.getText().toString());
                DatabaseReference toChip = FirebaseInfo.getFirebaseDatabase().getReference().child("Chips");
                toChip.push().setValue(chip);
                addTagDialog.dismiss();
                Snackbar.make(getView(), "Tag added successfully!", BaseTransientBottomBar.LENGTH_SHORT).show();
            }
        });
        addTagDialog.setView(view);
        addTagDialog.show();
    };
    private View.OnClickListener viewFriendListener = v -> {
        androidx.appcompat.app.AlertDialog friendsDialog = new androidx.appcompat.app.AlertDialog.Builder(getContext()).create();
        friendsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View view = getLayoutInflater().inflate(R.layout.dialog_participants, null);
        RecyclerView rv = view.findViewById(R.id.dialog_participants_rv);
        TextView title = view.findViewById(R.id.dialog_participants_title);
        title.setText("Friends");
        ParticipantsAdapter adapter = new ParticipantsAdapter(Database.getCurrentUser().getFriendList());
        rv.setLayoutManager(new LinearLayoutManager(context));
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setAdapter(adapter);
        Button close = view.findViewById(R.id.dialog_participants_btn);
        close.setOnClickListener(v1 -> friendsDialog.dismiss());
        friendsDialog.setView(view);
        friendsDialog.show();
    };

    private View.OnClickListener requestFriendListener = v -> {
        String input = friendCodeEt.getText().toString();
        User currentUser = Database.getCurrentUser();
        User otherUser = Database.findUser(input);
        if (otherUser == null || input.length() == 0)
            Snackbar.make(friendLl, "User doesn't exist", BaseTransientBottomBar.LENGTH_SHORT).show();
        else {
            ArrayList<String> friends = otherUser.getFriendRequestList();
            if (!CheckConnection.isConnectedToInternet(context))
                CheckConnection.showNoConnectionSnackBar(friendLl);
            else if (friends.contains(currentUser.getUid()))
                Snackbar.make(friendLl, "You already sent them a request.", BaseTransientBottomBar.LENGTH_SHORT).show();
            else if (currentUser.getUid().equals(otherUser.getUid()))
                Snackbar.make(friendLl, "You cannot send a friend request to yourself!", BaseTransientBottomBar.LENGTH_SHORT).show();
            else {
                DatabaseReference toOtherUser = FirebaseInfo.getFirebaseDatabase().getReference().child("Users").child(otherUser.getFirebaseId());
                Map<String, Object> map = new HashMap<>();
                otherUser.getFriendRequestList().add(currentUser.getUid());
                map.put("friendRequestList", otherUser.getFriendRequestList());
                toOtherUser.updateChildren(map);
                friendDialog.dismiss();
                Snackbar.make(linearLayout, "Request sent to " + otherUser.getDisplayName(), BaseTransientBottomBar.LENGTH_SHORT).show();
            }
        }
    };
    private View.OnClickListener bioListener = v -> {
        generateEditBioDialog(context, layoutInflater, R.layout.dialog_change_bio);
    };
    private View.OnClickListener changeBioListener = v -> {
        if (!changeBio.getText().toString().equals("")) {
            if (Integer.parseInt(charCounter.getText().toString()) >= 0) {
                Map<String, Object> map = new HashMap<>();
                map.put("bio", changeBio.getText().toString());
                DatabaseReference toUser = FirebaseInfo.getFirebaseDatabase().getReference().child("Users").child(Database.getCurrentUser().getFirebaseId());
                toUser.updateChildren(map);
                dialog.dismiss();
                Snackbar.make(linearLayout, "Biography updated!", BaseTransientBottomBar.LENGTH_SHORT).show();
            }
            bio.setText(changeBio.getText().toString());
        } else
            Snackbar.make(dialogLl, "Invalid parameters detected.", BaseTransientBottomBar.LENGTH_SHORT).show();
    };
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            charCounter.setText(maxChars - s.length() + "");
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        getActivity().setTitle("My Profile");
        setHasOptionsMenu(true);
        linearLayout = view.findViewById(R.id.profile_ll);
        bio = view.findViewById(R.id.profile_bio);
        profileName = view.findViewById(R.id.profile_name);
        avgRating = view.findViewById(R.id.profile_rating_num);
        ratingBar = view.findViewById(R.id.profile_rating);
        numReviews = view.findViewById(R.id.profile_rating_reviews);
        editBio = view.findViewById(R.id.profile_edit_bio);
        email = view.findViewById(R.id.profile_email);
        friendCode = view.findViewById(R.id.profile_code);
        numFriends = view.findViewById(R.id.profile_friends);
        numPartipated = view.findViewById(R.id.profile_participated);
        numHosted = view.findViewById(R.id.profile_host);
        context = getContext();
        layoutInflater = getLayoutInflater();
        friendsTitle = view.findViewById(R.id.profile_friends_title);
        addTag = view.findViewById(R.id.profile_add_tag);
        new GenerateUsers(getContext()).execute();
        return view;
    }

    private void generateUserDetails() {
        if (!CheckConnection.isConnectedToInternet(context))
            CheckConnection.showNoConnectionSnackBar(getView());
        else {
            User user = Database.getCurrentUser();
            bio.setText(user.getBio());
            profileName.setText(user.getDisplayName());
            email.setText(user.getEmail());
            float rating = (float) (user.getTotalRating() / user.getNumReviews());
            avgRating.setText(String.format("%.2f", rating));
            ratingBar.setRating(rating);
            numReviews.setText("(" + user.getNumReviews() + ")");
            friendCode.setText("Code: " + user.getFriendCode());
            numFriends.setText(user.getNumFriends() + "");
            numHosted.setText(user.getNumHosted() + "");
            numPartipated.setText(user.getNumParticipated() + "");
            editBio.setOnClickListener(bioListener);
            friendsTitle.setOnClickListener(viewFriendListener);
            addTag.setOnClickListener(addTagListener);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile_menu_refresh:
                new GenerateUsers(getContext()).execute();
                break;
            case R.id.profile_menu_add_friend:
                generateAddFriendDialog(context,layoutInflater,R.layout.dialog_add_friend);
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile, menu);
    }

    private void generateEditBioDialog(Context context, LayoutInflater layoutInflater, int layout) {
        dialog = new AlertDialog.Builder(context).create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View view = layoutInflater.inflate(layout, null);
        changeBio = view.findViewById(R.id.dialog_name_et);
        Button changeBioBtn = view.findViewById(R.id.dialog_name_btn);
        charCounter = view.findViewById(R.id.dialog_name_chars);
        dialogLl = view.findViewById(R.id.dialog_bio_ll);
        changeBio.addTextChangedListener(textWatcher);
        changeBioBtn.setOnClickListener(changeBioListener);
        dialog.setView(view);
        dialog.show();
    }

    private void generateAddFriendDialog(Context context, LayoutInflater layoutInflater, int layout){
        friendDialog = new AlertDialog.Builder(context).create();
        friendDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View view = layoutInflater.inflate(layout, null);
        friendLl = view.findViewById(R.id.dialog_friend_ll);
        friendCodeEt = view.findViewById(R.id.dialog_friend_et);
        Button requestFriendBtn = view.findViewById(R.id.dialog_friend_btn);
        requestFriendBtn.setOnClickListener(requestFriendListener);
        friendDialog.setView(view);
        friendDialog.show();
    }

    private class GenerateUsers extends FetchUser {

        GenerateUsers(Context context) {
            super(context);
        }

        @Override
        protected void onPostExecute(User user) {
            if (this.getDialog().isShowing())
                this.getDialog().dismiss();
            generateUserDetails();
            Snackbar.make(getView(), "User refreshed.", BaseTransientBottomBar.LENGTH_SHORT).show();
        }
    }
}
