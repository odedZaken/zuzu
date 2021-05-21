package com.example.zuzu.ui.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zuzu.EventModel;
import com.example.zuzu.LoginActivity;
import com.example.zuzu.R;
import com.example.zuzu.UserModel;
import com.example.zuzu.UserPreferences;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class DiscoverFragment extends Fragment {


    private ArrayList<EventModel> eventList;
    private ArrayList<EventModel> myEvents;
    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Location lastKnownLocation;
    private boolean locationPermissionGranted;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private String tabTitle;
    private UserModel currUser;
    private UserPreferences currUserPref;
    private ProgressBar progressBarEventList;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;


    public DiscoverFragment() {
        //Empty constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("events");
        currUser = LoginActivity.getCurrentUser();
        currUserPref = currUser.getUserPreferences();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Initialize view and inflate the layout for this fragment
        View eventListView = inflater.inflate(R.layout.fragment_discover, container, false);
        View eventMapView = inflater.inflate(R.layout.activity_map, container, false);

        //Initialize and assign variable
        getLocationPermission();
        eventList = new ArrayList<>();
        myEvents = new ArrayList<>();
        progressBarEventList = eventListView.findViewById(R.id.progressBarEventList);
        progressBarEventList.setVisibility(View.VISIBLE);
        recyclerView = eventListView.findViewById(R.id.eventsRecycleView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        //Get Device location and Initialize events List
        getDeviceLocation();

        //Get tab title
        tabTitle = this.getArguments().getString("title");

        //Set layout by tab title
        if(tabTitle.equals("Map")) {
            return eventMapView;
        } else {
            return eventListView;
        }
    }

    private void initializeEventList() {
        //       --------------Creating Event List From Database--------
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot event : dataSnapshot.getChildren()) {
                    getEventFromDB(event);
                }
                progressBarEventList.setVisibility(View.INVISIBLE);
                if(tabTitle.equals("Discover")) {
                    mAdapter = new RecyclerViewAdapter(eventList, getActivity());
                }
                else {
                    mAdapter = new RecyclerViewAdapter(myEvents, getActivity());
                }
                recyclerView.setAdapter(mAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getEventFromDB(DataSnapshot event) {
        ArrayList<String> usersIds = new ArrayList<>();
        String creatorEmail = event.child("creatorEmail").getValue(String.class);
        String date = event.child("date").getValue(String.class);
        String description = event.child("description").getValue(String.class);
        String id = event.child("id").getValue(String.class);
        int maxParticipants = event.child("maxParticipants").getValue(int.class);
        int currParticipants = event.child("currParticipants").getValue(int.class);
        double latitude = event.child("location").child("latitude").getValue(double.class);
        double longitude = event.child("location").child("longitude").getValue(double.class);
        usersIds = getEventIdList(event);
        LatLng location = new LatLng(latitude,longitude);
        String time = event.child("time").getValue(String.class);
        String title = event.child("title").getValue(String.class);
        String type = event.child("type").getValue(String.class);
        EventModel eventModel = new EventModel(title, description, type, time, date,creatorEmail, maxParticipants, location);
        eventModel.setId(id);
        eventModel.setCurrParticipants(currParticipants);
        eventModel.setUsersIDs(usersIds);
        if(lastKnownLocation != null) {
            int result = calculateDistanceMeters(latitude, longitude);
            eventModel.setDistance(result);
        }
        //Determine the list the event will be added to, and check by preferences
        sortEventToList(eventModel);
    }


    //Parse the list of users ids into an array
    private ArrayList<String> getEventIdList (DataSnapshot dataSnapshot) {
        ArrayList<String> usersId = new ArrayList<>();
        for (DataSnapshot id : dataSnapshot.child("usersIDs").getChildren()) {
            usersId.add(id.getValue(String.class));
        }
        return usersId;
    }

    //Add event to 'My Event' tab if user participate in this event
    private void sortEventToList(EventModel event) {
        String currUserId = currUser.getId();
        for(String id : event.getUsersIDs()) {
            if(id.equals(currUserId)) {
                myEvents.add(event);
            }
        }
        //Add to general event list according to preference
        if(currUserPref.isPrefByString(event.getType())) {
            eventList.add(event);
        }
    }

    private int calculateDistanceMeters(double latitude, double longitude) {
        float[] result = new float[3];
        Location.distanceBetween(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude(),latitude,longitude, result);
         //  Return result distance in meters
        return (int)result[0];
    }

    private void getDeviceLocation() {
        try {
            //Get last known location
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            lastKnownLocation = task.getResult();
                        }
                        else {
                            Toast.makeText(getActivity(), "Location Error", Toast.LENGTH_SHORT).show();
                        }
                        //After user location is determined, Show event list
                        initializeEventList();
                    }
                });
            }
            else {
                Toast.makeText(getActivity(), "Location Permission Denied", Toast.LENGTH_SHORT).show();
            }
        } catch(SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void getLocationPermission() {

        if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(permissions, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            locationPermissionGranted = false;
        } else {
            locationPermissionGranted = true;
        }
    }

    private void showEventsMap() {
        //recyclerView.setVisibility(View.GONE);
        //Show all events on a google map
    }
}

