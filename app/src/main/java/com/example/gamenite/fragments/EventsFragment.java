package com.example.gamenite.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.gamenite.R;
import com.example.gamenite.helpers.CheckConnection;
import com.example.gamenite.helpers.Database;
import com.example.gamenite.models.Event;
import com.example.gamenite.helpers.FirebaseInfo;
import com.example.gamenite.helpers.GenerateDialog;
import com.example.gamenite.helpers.Valid;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class EventsFragment extends Fragment implements GenerateDialog, OnMapReadyCallback, Valid {
    private Context context;
    private LayoutInflater layoutInflater;

    private String[] categories;
    private boolean hasSelectedLocation;
    private GoogleMap googleMap;
    private AlertDialog alertDialog;
    private Marker marker;

    private MapView mapView;
    private Spinner spinner;
    private FrameLayout frameLayout;
    private LinearLayout linearLayout;
    private EditText name;
    private EditText description;
    private EditText quota;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private LocalDateTime localDateTime;

    private ArrayList<Event> interestedEvents = new ArrayList<>();
    private ArrayList<Event> myEvents =  new ArrayList<>();
    private ArrayList<Event> history = new ArrayList<>();
    private Spinner.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
                case 0: //interested
                    String myUid = FirebaseInfo.getFirebaseUser().getUid();
                    for(Event event: Database.getEvents()){
                        ArrayList<String> uids = event.getParticipants();
                        for (String uid: uids){
                            if(uid.equals(myUid)){
                                interestedEvents.add(event);break;
                            }
                        }
                    }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            if (!hasSelectedLocation) {
                marker = googleMap.addMarker(new MarkerOptions()
                        .position(latLng).icon(BitmapDescriptorFactory.defaultMarker()));
                marker.setTag(latLng);
                Snackbar snackbar = Snackbar.make(linearLayout, "Location selected", BaseTransientBottomBar.LENGTH_SHORT);
                snackbar.setAction("Undo", v -> {
                    googleMap.clear();
                    hasSelectedLocation = false;
                    snackbar.dismiss();
                });
                snackbar.show();
                hasSelectedLocation = true;
            }
        }
    };
    private View.OnClickListener clearBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            googleMap.clear();
            hasSelectedLocation = false;
        }
    };
    private View.OnClickListener cancelBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            alertDialog.dismiss();
        }
    };
    private View.OnClickListener submitBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String[] strings = {name.getText().toString(), description.getText().toString(), quota.getText().toString()};
            if (!CheckConnection.isConnectedToInternet(context))
                CheckConnection.showNoConnectionSnackBar(linearLayout);
            else if (!isValid(strings))
                Snackbar.make(linearLayout, "Invalid parameters detected.", BaseTransientBottomBar.LENGTH_LONG).show();
            else {
                DatabaseReference toEvents = FirebaseInfo.getFirebaseDatabase().getReference().child("Events").push();
                Snackbar.make(frameLayout, "Success!", BaseTransientBottomBar.LENGTH_LONG).show();
                alertDialog.dismiss();
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                Event event = new Event(name.getText().toString(),
                        description.getText().toString(),
                        Integer.parseInt(quota.getText().toString()),
                        (LatLng) marker.getTag(),
                        localDateTime.format(dateTimeFormatter),
                        FirebaseInfo.getFirebaseUser().getUid());
                String key = toEvents.getKey();
                event.setFirebaseId(key);
                toEvents.setValue(event);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);
        setHasOptionsMenu(true);
        getActivity().setTitle("My Events");
        spinner = view.findViewById(R.id.event_spinner);
        frameLayout = view.findViewById(R.id.events_container);
        categories = getResources().getStringArray(R.array.events_category);
        context = getContext();
        layoutInflater = getActivity().getLayoutInflater();
        spinner.setOnItemSelectedListener(onItemSelectedListener);
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_events_add:
                generateAlertDialog(context, layoutInflater, R.layout.dialog_create_event);
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_events, menu);
    }

    private void generateAlertDialog(Context context, LayoutInflater layoutInflater, int layout) {
        alertDialog = new AlertDialog.Builder(context).create();
        View view = layoutInflater.inflate(layout, null);
        hasSelectedLocation = false;
        linearLayout = view.findViewById(R.id.events_create_ll);
        Button cancel = view.findViewById(R.id.dialog_event_cancel);
        cancel.setOnClickListener(cancelBtnListener);
        Button submit = view.findViewById(R.id.dialog_event_submit);
        submit.setOnClickListener(submitBtnListener);
        Button clearMap = view.findViewById(R.id.dialog_event_clear);
        clearMap.setOnClickListener(clearBtnListener);
        name = view.findViewById(R.id.dialog_event_name);
        description = view.findViewById(R.id.dialog_event_description);
        quota = view.findViewById(R.id.dialog_event_quota);
        datePicker = view.findViewById(R.id.dialog_event_datepicker);
        timePicker = view.findViewById(R.id.dialog_event_timepicker);
        mapView = view.findViewById(R.id.dialog_event_create_maps);
        if (mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }
        alertDialog.setView(view);
        alertDialog.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(context);
        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        CameraPosition cameraPosition = CameraPosition.builder().target(new LatLng(1.357437, 103.819313)).zoom(11).bearing(0).tilt(0).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        googleMap.setOnMapClickListener(onMapClickListener);
    }

    @Override
    public boolean isValid(String[] strings) {
        localDateTime = LocalDateTime.of(datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth(), timePicker.getHour(), timePicker.getMinute());
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < strings.length; i++) {
            if (strings[i].equals(""))
                return false;
        }
        if (!strings[2].matches("[0-9]+"))
            return false;
        if (now.isAfter(localDateTime))
            return false;
        if (!hasSelectedLocation)
            return false;
        if (localDateTime.minusHours(24).isBefore(now))
            return false;
        return true;
    }
}
