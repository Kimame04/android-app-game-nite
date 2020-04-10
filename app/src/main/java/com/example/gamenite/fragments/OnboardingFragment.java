package com.example.gamenite.fragments;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.leanback.app.OnboardingSupportFragment;
import androidx.preference.PreferenceManager;

import com.example.gamenite.MainActivity;
import com.example.gamenite.R;

public class OnboardingFragment extends OnboardingSupportFragment {

    private static final int numPages = 5;


    @Override
    protected int getPageCount() {
        return numPages;
    }

    @Override
    protected CharSequence getPageTitle(int pageIndex) {
        switch (pageIndex) {
            case 0:
                return "Welcome to Game Nite!";
            case 1:
                return "Explore new events!";
            case 2:
                return "Create your event";
            case 3:
                return "Real-time updates";
            default:
                return "Enjoy Game Nite!";
        }
    }

    @Override
    protected CharSequence getPageDescription(int pageIndex) {
        switch (pageIndex) {
            case 0:
                return "Bond with your community through games!";
            case 1:
                return "Check out events by the community, for the community!";
            case 2:
                return "Organise your own events in under 5 minutes!";
            case 3:
                return "Turn your notifications on for updates";
            default:
                return "To send feedback, shake your phone!";
        }
    }

    @Nullable
    @Override
    protected View onCreateBackgroundView(LayoutInflater inflater, ViewGroup container) {
        return null;
    }

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, ViewGroup container) {
        setArrowColor(Color.RED);
        setArrowBackgroundColor(Color.BLACK);
        setStartButtonText("Get started!");
        setDotBackgroundColor(Color.GRAY);
        ImageView content = new ImageView(getContext());
        content.setImageResource(R.mipmap.ic_launcher);
        content.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        content.setPadding(0, 32, 0, 32);
        return content;
    }

    @Override
    protected void onPageChanged(int newPage, int previousPage) {
        super.onPageChanged(newPage, previousPage);
    }

    @Nullable
    @Override
    protected View onCreateForegroundView(LayoutInflater inflater, ViewGroup container) {
        return null;
    }

    @Nullable
    @Override
    protected Animator onCreateLogoAnimation() {
        return AnimatorInflater.loadAnimator(getContext(),
                R.animator.lb_onboarding_logo_enter);
    }

    @Override
    protected void onFinishFragment() {
        super.onFinishFragment();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext())
                .edit();
        editor.putBoolean("onboarding_complete", true).apply();
        startActivity(new Intent(getContext(), MainActivity.class));
        getActivity().finish();
    }

}
