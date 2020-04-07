package com.example.gamenite.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.gamenite.fragments.EventUpdateFragment;
import com.example.gamenite.fragments.FriendRequestFragment;

public class NotificationsPagerAdapter extends FragmentStateAdapter {
    public NotificationsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new EventUpdateFragment();
            default:
                return new FriendRequestFragment();
        }
    }


    @Override
    public int getItemCount() {
        return 2;
    }
}
