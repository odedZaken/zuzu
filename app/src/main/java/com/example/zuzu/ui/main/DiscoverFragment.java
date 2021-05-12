package com.example.zuzu.ui.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zuzu.EventModel;
import com.example.zuzu.R;
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


public class DiscoverFragment extends Fragment {

    //private static final String ARG_SECTION_NUMBER = "section_number";
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//    private String mParam1;
//    private String mParam2;
    private ArrayList<EventModel> eventList;
    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Location lastKnownLocation;
    private boolean locationPermissionGranted;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;


    //private PageViewModel pageViewModel;
    public DiscoverFragment() {
        //Empty constructor
    }

//    public static DiscoverFragment newInstance(String param1, String param2) {
////        DiscoverFragment fragment = new DiscoverFragment();
////        Bundle bundle = new Bundle();
////        bundle.putInt(ARG_SECTION_NUMBER, index);
////        fragment.setArguments(bundle);
////        return fragment;
//        DiscoverFragment fragment = new DiscoverFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM1, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("events");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

//        final TextView textView = root.findViewById(R.id.section_label);
//        pageViewModel.getText().observe(this, new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
        //Initialize view and inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        //Initialize and assign variable
        getLocationPermission();
        getDeviceLocation();
        eventList = new ArrayList<>();
        TextView textView = view.findViewById(R.id.section_label);
        recyclerView = view.findViewById(R.id.eventsRecycleView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());   //todo: This might not Work!
        recyclerView.setLayoutManager(layoutManager);



        //Get title
        String sTitle = this.getArguments().getString("title");

        //if(sTitle.equals("Discover")) {
        initializeEventList();
        //}

        //Set title on text view
        textView.setText(sTitle);

        return view;
    }

    private void initializeEventList() {
        //       --------------Creating Event List From Database--------
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot event : dataSnapshot.getChildren()) {

                    String creatorEmail = event.child("creatorEmail").getValue(String.class);
                    String date = event.child("date").getValue(String.class);
                    String description = event.child("description").getValue(String.class);
                    String id = event.child("id").getValue(String.class);
                    int maxParticipants = event.child("maxParticipants").getValue(int.class);
                    int currParticipants = event.child("currParticipants").getValue(int.class);
                    double latitude = event.child("location").child("latitude").getValue(double.class);
                    double longitude = event.child("location").child("longitude").getValue(double.class);
                    LatLng location = new LatLng(latitude,longitude);
                    String time = event.child("time").getValue(String.class);
                    String title = event.child("title").getValue(String.class);
                    String type = event.child("type").getValue(String.class);
                    //Toast.makeText(MainActivity.this, creatorEmail, Toast.LENGTH_SHORT).show();
                    EventModel eventModel = new EventModel(title, description, type, time, date,creatorEmail, maxParticipants, location);
                    eventModel.setId(id);
                    eventModel.setCurrParticipants(currParticipants);
                    if(lastKnownLocation != null) {
                        int result = calculateDistanceMeters(latitude, longitude);
                        eventModel.setDistance(result);
                    }
                    eventList.add(eventModel);
                }
                mAdapter = new RecyclerViewAdapter(eventList, getActivity());
                recyclerView.setAdapter(mAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
                    }
                });
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
}

