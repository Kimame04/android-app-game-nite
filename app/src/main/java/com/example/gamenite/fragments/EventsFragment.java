package com.example.gamenite.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamenite.R;
import com.example.gamenite.adapters.EventAdapter;
import com.example.gamenite.adapters.ParticipantsAdapter;
import com.example.gamenite.helpers.CheckConnection;
import com.example.gamenite.helpers.Database;
import com.example.gamenite.helpers.FirebaseInfo;
import com.example.gamenite.models.Event;
import com.example.gamenite.models.User;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EventsFragment extends Fragment implements OnMapReadyCallback {
    private Context context;

    private boolean hasSelectedLocation;
    private GoogleMap googleMap;
    private static int pos;
    private Marker marker;

    private LinearLayout linearLayout;
    private AlertDialog dialog;
    private EditText name;
    private EditText description;
    private EditText quota;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private LocalDateTime localDateTime;
    private LinearLayout fragmentLl;
    private RecyclerView recyclerView;
    private ArrayList<Event> toBeAdded = new ArrayList<>();
    private Spinner.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
            pos = position;
            new EventsFragment.FetchEvents(position, getContext()).execute();
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
            dialog.dismiss();
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
                Snackbar.make(fragmentLl, "Success!", BaseTransientBottomBar.LENGTH_LONG).show();
                dialog.dismiss();
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
                new FetchEvents(pos, getContext()).execute();
            }
        }
    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);
        setHasOptionsMenu(true);
        getActivity().setTitle("My Events");
        context = getContext();
        Spinner spinner = view.findViewById(R.id.event_spinner);
        recyclerView = view.findViewById(R.id.events_rv);
        fragmentLl = view.findViewById(R.id.event_ll);
        spinner.setOnItemSelectedListener(onItemSelectedListener);
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_events_add:
                generateAlertDialog(context, getLayoutInflater());
                break;
            case R.id.menu_events_refresh:
                new FetchEvents(pos, getContext()).execute();
                Snackbar.make(getView(), "Events refreshed.", BaseTransientBottomBar.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_events, menu);
    }

    private void generateAlertDialog(Context context, LayoutInflater layoutInflater) {
        dialog = new AlertDialog.Builder(context).create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View view = layoutInflater.inflate(R.layout.dialog_create_event, null);
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
        MapView mapView = view.findViewById(R.id.dialog_event_create_maps);
        if (mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }
        dialog.setView(view);
        dialog.show();
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

    private boolean isValid(String[] strings) {
        localDateTime = LocalDateTime.of(datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth(), timePicker.getHour(), timePicker.getMinute());
        LocalDateTime now = LocalDateTime.now();
        for (String string : strings) {
            if (string.equals(""))
                return false;
        }
        return strings[2].matches("[0-9]+") && !now.isAfter(localDateTime) && hasSelectedLocation && !localDateTime.minusHours(24).isBefore(now);
    }

    private class FetchEvents extends com.example.gamenite.helpers.FetchEvents {

        FetchEvents(int position, Context context) {
            super(position, context);
        }

        @Override
        protected void onPostExecute(ArrayList<Event> events) {
            if (getDialog().isShowing())
                getDialog().dismiss();
            String myUid = Database.getCurrentUser().getUid();
            toBeAdded.clear();
            switch (this.getPosition()) {
                case 0: //interested
                    for (Event event : events) {
                        if (event.getParticipants().contains(myUid))
                            toBeAdded.add(event);
                    }
                    break;
                case 1: //by the user
                    for (Event event : events) {
                        if (event.getUid().equals(myUid))
                            toBeAdded.add(event);
                    }
                    break;
                case 2: //unrated
                    for (Event event : events) {
                        if (LocalDateTime.parse(event.getDeadline(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).isBefore(LocalDateTime.now())
                                && (event.getParticipants().contains(myUid)) && !event.getUid().equals(myUid))
                            toBeAdded.add(event);
                    }
                    break;
            }
            User currentUser = Database.getCurrentUser();
            EventAdapter eventAdapter = new EventAdapter(toBeAdded, (position1, view) -> {
                Event event = toBeAdded.get(position1);
                User organiser = Database.findUserbyUid(event.getUid());
                DatabaseReference eventsReference = FirebaseInfo.getFirebaseDatabase().getReference().child("Events").child(event.getFirebaseId());
                DatabaseReference userReference = FirebaseInfo.getFirebaseDatabase().getReference().child("Users").child(Database.getCurrentUser().getFirebaseId());
                DatabaseReference organiserReference = FirebaseInfo.getFirebaseDatabase().getReference().child("Users").child(Database.findUserbyUid(event.getUid()).getFirebaseId());
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime deadline = LocalDateTime.parse(event.getDeadline(), dateTimeFormatter);
                boolean isOrganiser = event.getUid().equals(currentUser.getUid());
                boolean hasPassed = deadline.isBefore(now);
                switch (view.getId()) {
                    case R.id.events_update:
                        AlertDialog updateDialog = new AlertDialog.Builder(context)
                                .create();
                        updateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View view1 = inflater.inflate(R.layout.dialog_change_bio, null);
                        TextView title = view1.findViewById(R.id.dialog_bio_tv);
                        EditText editText = view1.findViewById(R.id.dialog_name_et);
                        Button button = view1.findViewById(R.id.dialog_name_btn);
                        title.setText(R.string.update);
                        editText.setHint("Keep it short and concise!");
                        button.setText(R.string.submit);
                        button.setOnClickListener(v1 -> {
                            if (editText.getText().toString().length() == 0)
                                Snackbar.make(v1, "Cannot submit an empty field", BaseTransientBottomBar.LENGTH_SHORT).show();
                            else {
                                Map<String, Object> map = new HashMap<>();
                                event.getUpdates().add(editText.getText().toString() + "\nTimestamp: " + now.format(dateTimeFormatter));
                                map.put("updates", event.getUpdates());
                                eventsReference.updateChildren(map);
                                Snackbar.make(getView(), "Success!", BaseTransientBottomBar.LENGTH_SHORT).show();
                                updateDialog.dismiss();
                            }
                        });
                        new FetchEvents(pos, getContext()).execute();
                        updateDialog.setView(view1);
                        updateDialog.show();
                        break;
                    case R.id.events_interested:
                        if (hasPassed && isOrganiser) {
                            Map<String, Object> toOrganiser = new HashMap<>();
                            toOrganiser.put("numHosted", currentUser.getNumHosted() + 1);
                            toOrganiser.put("numParticipated", currentUser.getNumParticipated() + 1);
                            organiserReference.updateChildren(toOrganiser);
                            Map<String, Object> toEvent = new HashMap<>();
                            event.removeParticipant(currentUser.getUid());
                            toEvent.put("participants", event.getParticipants());
                            eventsReference.updateChildren(toEvent);
                            if (event.getParticipants().size() == 0)
                                eventsReference.removeValue();
                            new FetchEvents(pos, getContext()).execute();
                        } else if (hasPassed) { //shown when the event has concluded
                            AlertDialog rateDialog = new AlertDialog.Builder(context).create();
                            rateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            View view3 = layoutInflater.inflate(R.layout.dialog_rate_event, null);
                            Button submit = view3.findViewById(R.id.dialog_rate_submit);
                            RatingBar ratingBar = view3.findViewById(R.id.dialog_rate_ratingbar);
                            TextView number = view3.findViewById(R.id.dialog_rate_number);
                            submit.setOnClickListener(v1 -> {
                                Map<String, Object> toOrganiser = new HashMap<>();
                                Map<String, Object> toUser = new HashMap<>();
                                Map<String, Object> toEvent = new HashMap<>();
                                toOrganiser.put("numReviews", organiser.getNumReviews() + 1);
                                toOrganiser.put("totalRating", organiser.getTotalRating() + (double) ratingBar.getRating());
                                organiserReference.updateChildren(toOrganiser);
                                toUser.put("numParticipated", currentUser.getNumParticipated() + 1);
                                userReference.updateChildren(toUser);
                                event.removeParticipant(currentUser.getUid());
                                toEvent.put("participants", event.getParticipants());
                                eventsReference.updateChildren(toEvent);
                                if (event.getParticipants().size() == 0)
                                    eventsReference.removeValue();
                                new FetchEvents(pos, getContext()).execute();
                                Snackbar.make(getView(), "Event rated successfully!", BaseTransientBottomBar.LENGTH_SHORT).show();
                                rateDialog.setView(view3);
                                rateDialog.dismiss();
                            });
                            ratingBar.setOnRatingBarChangeListener((ratingBar1, rating, fromUser) -> number.setText(rating + ""));
                            rateDialog.setView(view3);
                            rateDialog.show();

                        } else if (isOrganiser) { //shown if organiser wants to cancel the event
                            new MaterialAlertDialogBuilder(context)
                                    .setTitle("Cancel event")
                                    .setMessage("You will get a 0-star rating. Continue?")
                                    .setPositiveButton("Yes", ((dialog, which) -> {
                                        eventsReference.removeValue();
                                        Map<String, Object> toUser = new HashMap<>();
                                        toUser.put("numReviews", currentUser.getNumReviews() + 1);
                                        userReference.updateChildren(toUser);
                                        dialog.dismiss();
                                        new FetchEvents(pos, getContext()).execute();
                                        Snackbar.make(linearLayout, "Event cancelled.", BaseTransientBottomBar.LENGTH_LONG).show();
                                    })).setNegativeButton("No", null).show();
                        } else if (deadline.minusHours(24).isBefore(now)) { //shown if user backs out 24 hours before date
                            new MaterialAlertDialogBuilder(context).setTitle("Backing out last-minute")
                                    .setMessage("This will result in a 0-star rating for you. Do you wish to continue?")
                                    .setPositiveButton("Yes", ((dialog, which) -> {
                                        Map<String, Object> toUser = new HashMap<>();
                                        toUser.put("numReviews", currentUser.getNumReviews() + 1);
                                        userReference.updateChildren(toUser);
                                        Map<String, Object> toEvent = new HashMap<>();
                                        event.removeParticipant(currentUser.getUid());
                                        toEvent.put("interested", event.getParticipants().size());
                                        toEvent.put("participants", event.getParticipants());
                                        eventsReference.updateChildren(toEvent);
                                        new FetchEvents(pos, getContext()).execute();
                                        Snackbar.make(linearLayout, "Backed out successfully.", BaseTransientBottomBar.LENGTH_SHORT).show();
                                    })).setNegativeButton("No", null).show();
                        } else { //show regular quit procedure
                            new MaterialAlertDialogBuilder(context).setTitle("Quit event")
                                    .setMessage("Are you sure you want to leave?")
                                    .setPositiveButton("Yes", ((dialog, which) -> {
                                        Map<String, Object> toEvent = new HashMap<>();
                                        event.removeParticipant(currentUser.getUid());
                                        toEvent.put("interested", event.getParticipants().size());
                                        toEvent.put("participants", event.getParticipants());
                                        eventsReference.updateChildren(toEvent);
                                        new FetchEvents(pos, getContext()).execute();
                                        Snackbar.make(getView(), "You have successfully left the event.", BaseTransientBottomBar.LENGTH_SHORT).show();
                                    })).setNegativeButton("No", null).show();
                        }
                        break;
                    case R.id.events_location:
                        Uri uri = Uri.parse("geo:" + event.getLat() + ", " + event.getLongi() + "?q=" + event.getLat() + ", " + event.getLongi());
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        context.startActivity(mapIntent);
                        break;
                    case R.id.event_cv:
                        AlertDialog participantsDialog = new AlertDialog.Builder(context).create();
                        participantsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View view2 = layoutInflater.inflate(R.layout.dialog_participants, null);
                        RecyclerView rv = view2.findViewById(R.id.dialog_participants_rv);
                        ParticipantsAdapter adapter = new ParticipantsAdapter(event.getParticipants());
                        rv.setLayoutManager(new LinearLayoutManager(context));
                        rv.setItemAnimator(new DefaultItemAnimator());
                        rv.setAdapter(adapter);
                        Button close = view2.findViewById(R.id.dialog_participants_btn);
                        close.setOnClickListener(v1 -> participantsDialog.dismiss());
                        participantsDialog.setView(view2);
                        participantsDialog.show();
                        break;
                }
            });
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(eventAdapter);
        }
    }

}
