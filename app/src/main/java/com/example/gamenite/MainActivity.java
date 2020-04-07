package com.example.gamenite;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.gamenite.fragments.EventsFragment;
import com.example.gamenite.fragments.ExploreFragment;
import com.example.gamenite.fragments.NotificationsFragment;
import com.example.gamenite.fragments.ProfileFragment;
import com.example.gamenite.fragments.SettingsFragment;
import com.example.gamenite.helpers.FetchUser;
import com.example.gamenite.helpers.FirebaseInfo;
import com.example.gamenite.models.Database;
import com.example.gamenite.models.User;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.seismic.ShakeDetector;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static FrameLayout frameLayout;
    private RelativeLayout relativeLayout;

    private Context context;

    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser firebaseUser;
    private FirebaseInfo firebaseInfo;
    private DatabaseReference toUser;

    private SensorManager sensorManager;
    private ShakeDetector shakeDetector;

    private static final int RC_SIGN_IN = 1;
    private BottomNavigationView bottomNavigationView;

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener =
            item -> {
                if (bottomNavigationView.getSelectedItemId() == item.getItemId())
                    return true;
                Fragment selectedFragment = null;
                switch (item.getItemId()) {
                    case R.id.profile:
                        selectedFragment = new ProfileFragment();
                        break;
                    case R.id.explore:
                        selectedFragment = new ExploreFragment();
                        break;
                    case R.id.calendar:
                        selectedFragment = new EventsFragment();
                        break;
                    case R.id.settings:
                        selectedFragment = new SettingsFragment();
                        break;
                    case R.id.notifications:
                        selectedFragment = new NotificationsFragment();
                        break;
                }
                if (androidx.preference.PreferenceManager.getDefaultSharedPreferences(context).getBoolean("settings_animation", true))
                    getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left).replace(R.id.fragment_container, selectedFragment).commitAllowingStateLoss();
                else
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commitAllowingStateLoss();
                return true;
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

        frameLayout = findViewById(R.id.fragment_container);
        relativeLayout = findViewById(R.id.main_rl);

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = firebaseAuth -> {
            if (firebaseAuth.getCurrentUser() != null) {
                firebaseUser = firebaseAuth.getCurrentUser();
                firebaseInfo = new FirebaseInfo(firebaseUser,firebaseDatabase);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                if (!sharedPreferences.getBoolean("onboarding_complete", false)) {
                    startActivity(new Intent(this, OnboardingActivity.class));
                    finish();
                } else {
                    Database.initDatabase();
                    new FetchUser(this).execute();
                    switch (sharedPreferences.getString("settings_startup_fragment", "")) {
                        case "Profile":
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commitAllowingStateLoss();
                            bottomNavigationView.setSelectedItemId(R.id.profile);
                            break;
                        case "My Events":
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new EventsFragment()).commitAllowingStateLoss();
                            bottomNavigationView.setSelectedItemId(R.id.calendar);
                            break;
                        case "Settings":
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commitAllowingStateLoss();
                            bottomNavigationView.setSelectedItemId(R.id.settings);
                            break;
                        case "Notifications":
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new NotificationsFragment());
                        default:
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ExploreFragment()).commitAllowingStateLoss();
                            bottomNavigationView.setSelectedItemId(R.id.explore);
                            break;
                    }
                }
            } else {
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder().setIsSmartLockEnabled(false)
                                .setAvailableProviders(Arrays.asList(
                                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                                        new AuthUI.IdpConfig.EmailBuilder().build()))
                                .setTheme(R.style.LoginTheme)
                                .setLogo(R.mipmap.ic_launcher)
                                .build(),
                        RC_SIGN_IN);
            }
        };
        firebaseAuth.addAuthStateListener(authStateListener);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        shakeDetector = new ShakeDetector(() -> new MaterialAlertDialogBuilder(this)
                .setTitle("Shake detected").setMessage("Send feedback via email?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    String[] TO = {"h1710095@nushigh.edu.sg"};
                    String[] CC = {};
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setType("text/plain");
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
                    emailIntent.putExtra(Intent.EXTRA_CC, CC);
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Put a meaningful subject name here");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "Insert your feedback here. Provide feedback constructively!");
                    try {
                        startActivity(Intent.createChooser(emailIntent, "Send mail"));
                    } catch (android.content.ActivityNotFoundException e) {
                        Snackbar.make(relativeLayout, "You have no email client installed.", BaseTransientBottomBar.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("No", null).show());
        shakeDetector.start(sensorManager);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                toUser = FirebaseInfo.getFirebaseDatabase().getReference().child("Users");
                if ((Database.findUserbyUid(FirebaseInfo.getFirebaseUser().getUid()) == null)) {
                    User user = new User();
                    String key = toUser.push().getKey();
                    user.setFirebaseId(key);
                    toUser.child(key).setValue(user);
                }
                Snackbar.make(relativeLayout, "Successfully signed in!", BaseTransientBottomBar.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Snackbar.make(relativeLayout, "Sign in cancelled.", BaseTransientBottomBar.LENGTH_SHORT).show();
                finish();
            }
        }
    }


}
