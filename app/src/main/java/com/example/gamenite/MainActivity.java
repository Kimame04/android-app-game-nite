package com.example.gamenite;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.gamenite.fragments.EventsFragment;
import com.example.gamenite.fragments.ExploreFragment;
import com.example.gamenite.fragments.NotificationsFragment;
import com.example.gamenite.fragments.ProfileFragment;
import com.example.gamenite.fragments.SettingsFragment;
import com.example.gamenite.helpers.Database;
import com.example.gamenite.models.Event;
import com.example.gamenite.helpers.FirebaseInfo;
import com.example.gamenite.helpers.GenerateDialog;
import com.example.gamenite.models.User;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.seismic.ShakeDetector;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements GenerateDialog {
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

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commitAllowingStateLoss();
                    return true;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

        frameLayout = findViewById(R.id.fragment_container);
        relativeLayout = findViewById(R.id.main_rl);

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = firebaseAuth -> {
            if (firebaseAuth.getCurrentUser() != null) {
                firebaseUser = firebaseAuth.getCurrentUser();
                firebaseInfo = new FirebaseInfo(firebaseUser,firebaseDatabase);
                Database.initDatabase();
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                switch (sharedPreferences.getString("settings_startup_fragment","")){
                    case "Profile":
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new ProfileFragment()).commitAllowingStateLoss();
                        bottomNavigationView.setSelectedItemId(R.id.profile);
                        break;
                    case "My Events":
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new EventsFragment()).commitAllowingStateLoss();
                        bottomNavigationView.setSelectedItemId(R.id.calendar);
                        break;
                    case "Settings":
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new SettingsFragment()).commitAllowingStateLoss();
                        bottomNavigationView.setSelectedItemId(R.id.settings);
                        break;
                    case "Notifications":
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new NotificationsFragment());
                    default:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new ExploreFragment()).commitAllowingStateLoss();
                        bottomNavigationView.setSelectedItemId(R.id.explore);
                        break;
                }
            } else {
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder().setIsSmartLockEnabled(false)
                                .setAvailableProviders(Arrays.asList(
                                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                                        new AuthUI.IdpConfig.EmailBuilder().build()))
                                .setTheme(R.style.LoginTheme)
                                .build(),
                        RC_SIGN_IN);
            }
        };
        firebaseAuth.addAuthStateListener(authStateListener);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        shakeDetector = new ShakeDetector(() -> {
            if(generateConfirmationDialog(context,"Shake detected","Send feedback via email?")){
                String[] TO = {"h1710095@nushigh.edu.sg"};
                String[] CC = {};
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL,TO);
                emailIntent.putExtra(Intent.EXTRA_CC,CC);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT,"Put a meaningful subject name here");
                emailIntent.putExtra(Intent.EXTRA_TEXT,"Insert your feedback here. Provide feedback constructively!");
                try{
                    startActivity(Intent.createChooser(emailIntent, "Send mail"));
                } catch(android.content.ActivityNotFoundException e){
                    Snackbar.make(relativeLayout,"You have no email client installed.", BaseTransientBottomBar.LENGTH_SHORT).show(); }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                toUser = FirebaseInfo.getFirebaseDatabase().getReference().child("Users");
                boolean alreadyRegistered = false;
                ArrayList<User> users = Database.getUsers();
                for(User user: users){
                    if(user.getUid().equals(FirebaseInfo.getFirebaseUser().getUid())){
                        alreadyRegistered = true;
                        break;
                    }
                }
                if(!alreadyRegistered){
                    User user = new User();
                    user.setFirebaseId(toUser.push().getKey());
                    toUser.push().setValue(user);
                }
                Snackbar.make(relativeLayout, "Successfully signed in!", BaseTransientBottomBar.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Snackbar.make(relativeLayout, "Sign in cancelled.", BaseTransientBottomBar.LENGTH_SHORT).show();
                finish();
            }
        }
    }

}
