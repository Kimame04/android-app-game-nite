package com.example.gamenite.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.gamenite.R;
import com.example.gamenite.helpers.FirebaseInfo;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingsFragment extends PreferenceFragmentCompat {

    private Context context;
    private static Preference.OnPreferenceChangeListener onPreferenceChangeListener =
            (preference, newValue) -> {
                String value = newValue.toString();
                if (preference instanceof ListPreference) {
                    ListPreference listPreference = (ListPreference) preference;
                    int index = listPreference.findIndexOfValue(value);
                    if (listPreference.getValue() == null)
                        listPreference.setValueIndex(1);
                    preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
                }
                return true;
            };

    private static void bindSummaryValue(Preference preference){
        preference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        onPreferenceChangeListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(),""));
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings,rootKey);
        getActivity().setTitle("Settings");
        setHasOptionsMenu(true);
        context = getContext();
        bindSummaryValue(findPreference("settings_startup_fragment"));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_settings,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.settings_menu_logout:
                new MaterialAlertDialogBuilder(context).setTitle("Logout")
                        .setMessage("Sign out from this account?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            FirebaseInfo.logoutUser(context);
                        }).setNegativeButton("No", null).show();
        }
        return true;
    }
}
