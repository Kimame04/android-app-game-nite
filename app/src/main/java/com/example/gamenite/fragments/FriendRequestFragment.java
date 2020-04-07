package com.example.gamenite.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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
import com.example.gamenite.adapters.FriendRequestAdapter;
import com.example.gamenite.helpers.FirebaseInfo;
import com.example.gamenite.models.Database;
import com.example.gamenite.models.User;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FriendRequestFragment extends Fragment {
    private RecyclerView recyclerView;
    private FriendRequestAdapter adapter;
    private Context context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_requests, container, false);
        context = getContext();
        recyclerView = view.findViewById(R.id.notification_friend_request_rv);
        generateRequests(recyclerView);
        return view;
    }

    private void generateRequests(RecyclerView recyclerView) {
        User currentUser = Database.getCurrentUser();
        ArrayList<String> requests = Database.getCurrentUser().getFriendRequestList();
        Log.d("test", requests.size() + "");
        Log.d("test", currentUser.getFriendList().size() + "");
        DatabaseReference toUser = FirebaseInfo.getFirebaseDatabase().getReference().child("Users").child(currentUser.getFirebaseId());
        adapter = new FriendRequestAdapter(requests, (position, view1) -> {
            String request = requests.get(position);
            switch (view1.getId()) {
                case R.id.notifications_close:
                    Map<String, Object> map = new HashMap<>();
                    requests.remove(request);
                    map.put("friendRequestList", currentUser.getFriendRequestList());
                    toUser.updateChildren(map);
                    generateRequests(recyclerView);
                    Snackbar.make(getView(), "Friend request declined.", BaseTransientBottomBar.LENGTH_SHORT).show();
                case R.id.notifications_check:
                    User user = Database.findUserbyUid(request);
                    Map<String, Object> meMap = new HashMap<>();
                    currentUser.getFriendList().add(request);
                    requests.remove(request);
                    meMap.put("friendRequestList", currentUser.getFriendRequestList());
                    meMap.put("friendList", currentUser.getFriendList());
                    meMap.put("numFriends", currentUser.getNumFriends() + 1);
                    toUser.updateChildren(meMap);
                    DatabaseReference toFriend = FirebaseInfo.getFirebaseDatabase().getReference().child("Users").child(user.getFirebaseId());
                    Map<String, Object> friendMap = new HashMap<>();
                    user.getFriendList().add(currentUser.getUid());
                    friendMap.put("friendList", user.getFriendList());
                    friendMap.put("numFriends", user.getNumFriends() + 1);
                    toFriend.updateChildren(friendMap);
                    generateRequests(recyclerView);
                    Snackbar.make(getView(), "Friend request accepted.", BaseTransientBottomBar.LENGTH_SHORT).show();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }
}
