package com.example.zuzu.ui.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.zuzu.ApplicationGlobal;
import com.example.zuzu.EventModel;
import com.example.zuzu.LoginActivity;
import com.example.zuzu.MainActivity;
import com.example.zuzu.MainEventActivity;
import com.example.zuzu.R;
import com.example.zuzu.UserModel;
import com.example.zuzu.UserPreferences;
import com.google.android.gms.internal.maps.zzx;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;


public class DiscoverFragment extends Fragment implements GoogleMap.OnMarkerClickListener, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {


    private ArrayList<EventModel> eventList;
    private ArrayList<EventModel> myEvents;
    private DatabaseReference databaseReference;
    private EventModel markedEventOnMap;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Location lastKnownLocation;
    private boolean locationPermissionGranted;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private ExtendedFloatingActionButton gotoEventFab;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyListMsg;
    private String tabTitle;
    private UserModel currUser;
    private UserPreferences currUserPref;
    private ProgressBar progressBarEventList;
    private GoogleMap googleMap;
    private Context context;
    private HashMap<String, Bitmap> iconMarkers;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 14;


    public DiscoverFragment() {
        //Empty constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("events");

        currUser = ApplicationGlobal.getCurrentUser();
        if (currUser != null) {
            currUserPref = currUser.getUserPreferences();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Initialize view and inflate the layout for this fragment
        View eventListView = inflater.inflate(R.layout.fragment_discover, container, false);
        View eventMapView = inflater.inflate(R.layout.fragment_map_events, container, false);

        //Get tab title
        tabTitle = this.getArguments().getString("title");

        //Initialize and assign variables
        context = getActivity();
        locationPermissionGranted = false;
        getLocationPermission();
        eventList = new ArrayList<>();
        myEvents = new ArrayList<>();
        emptyListMsg = eventListView.findViewById(R.id.emptyListMsg);
        progressBarEventList = eventListView.findViewById(R.id.progressBarEventList);
        progressBarEventList.setVisibility(View.VISIBLE);
        recyclerView = eventListView.findViewById(R.id.eventsRecycleView);
        recyclerView.setHasFixedSize(true);
        swipeRefreshLayout = eventListView.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        //Get Device location and Initialize events List
        getDeviceLocation();

        //Set layout by tab title
        if (tabTitle.equals("Map")) {
            buildMarkersList();
            InitializeMapFragment(eventMapView);
            return eventMapView;
        } else {
            return eventListView;
        }
    }


    private void InitializeMapFragment(View eventMapView) {
        gotoEventFab = eventMapView.findViewById(R.id.gotoEventFab);
        gotoEventFab.setOnClickListener(DiscoverFragment.this);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.googleMapEvents);
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap map) {
                //When map is loaded
                DiscoverFragment.this.googleMap = map;
                // Turn on the My Location layer and the related control on the map.
                updateLocationUI();
                googleMap.setOnMapClickListener(latLng -> gotoEventFab.hide());
                googleMap.setOnMarkerClickListener(DiscoverFragment.this);
                googleMap.setOnCameraMoveStartedListener(i -> gotoEventFab.shrink());
                googleMap.setOnCameraIdleListener(() -> gotoEventFab.extend());
                if(lastKnownLocation != null) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void updateLocationUI() {
        if (googleMap != null) {
            try {
                googleMap.getUiSettings().setMapToolbarEnabled(false);
                if (locationPermissionGranted) {
                    googleMap.setMyLocationEnabled(true);
                    googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                    googleMap.getUiSettings().setZoomControlsEnabled(true);
                } else {
                    googleMap.setMyLocationEnabled(false);
                    googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                }
            } catch (SecurityException e) {
                Log.e("Exception: %s", e.getMessage());
            }
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        gotoEventFab.show();
        markedEventOnMap = (EventModel) marker.getTag();
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gotoEventFab:
                Intent intent = new Intent(getActivity(), MainEventActivity.class);
                intent.putExtra("title", markedEventOnMap.getTitle());
                EventFragment.setEvent(markedEventOnMap);
                EventChatFragment.setEvent(markedEventOnMap);
                startActivity(intent);
                break;
        }
    }

    private void initializeEventList() {
        //       --------------Creating Event List From Database--------
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Clear all fragments from old data
                clearViews();
                for (DataSnapshot event : dataSnapshot.getChildren()) {
                    getEventFromDB(event);
                }
                checkEmptyLists();
                progressBarEventList.setVisibility(View.INVISIBLE);
                if (tabTitle.equals("Discover")) {
                    mAdapter = new RecyclerViewAdapter(eventList, getActivity());
                } else if (tabTitle.equals("My Events")) {
                    mAdapter = new RecyclerViewAdapter(myEvents, getActivity());
                }
                recyclerView.setAdapter(mAdapter);
                swipeRefreshLayout.clearAnimation();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    private void clearViews() {
        emptyListMsg.setVisibility(View.GONE);
        eventList.clear();
        myEvents.clear();
        if (googleMap != null) {
            googleMap.clear();
        }
    }

    private void getEventFromDB(DataSnapshot event) {
        ArrayList<String> usersIds;
        String creatorId = event.child("creatorId").getValue(String.class);
        String date = event.child("date").getValue(String.class);
        String description = event.child("description").getValue(String.class);
        String id = event.child("id").getValue(String.class);
        int maxParticipants = event.child("maxParticipants").getValue(int.class);
        int currParticipants = event.child("currParticipants").getValue(int.class);
        double latitude = event.child("location").child("latitude").getValue(double.class);
        double longitude = event.child("location").child("longitude").getValue(double.class);
        usersIds = getEventIdList(event);
        LatLng location = new LatLng(latitude, longitude);
        String time = event.child("time").getValue(String.class);
        String title = event.child("title").getValue(String.class);
        String type = event.child("type").getValue(String.class);
        EventModel eventModel = new EventModel(title, description, type, time, date, creatorId, maxParticipants, location);
        eventModel.setId(id);
        eventModel.setCurrParticipants(currParticipants);
        eventModel.setUsersIDs(usersIds);

        if (lastKnownLocation != null) {
            eventModel.setDistance(calculateDistanceMeters(latitude, longitude));
        }
        //Determine the list the event will be added to, and check by preferences
        sortEventToList(eventModel);
    }

    private void checkEmptyLists() {
        if (eventList.isEmpty() && tabTitle.equals("Discover")) {
            emptyListMsg.setText("There are no events that \nmatches your preferences...");
            emptyListMsg.setVisibility(View.VISIBLE);
        }
        if (myEvents.isEmpty() && tabTitle.equals("My Events")) {
            emptyListMsg.setText("There are no events that \nyou are participating in...");
            emptyListMsg.setVisibility(View.VISIBLE);
        }
    }


    private void addEventMarkerOnMap(EventModel event) {
        //Convert vector image type to bitmap for sport type marker
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconMarkers.get(event.getType())));
        markerOptions.position(event.getLocation());
        markerOptions.title(event.getTitle());
        markerOptions.snippet("Date & Time: " + event.getDate() + " " + event.getTime());

        if (googleMap != null) {
            googleMap.addMarker(markerOptions).setTag(event);
        }
    }

    private void buildMarkersList() {
        iconMarkers = new HashMap<>();
        iconMarkers.put("Basketball", Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.basketball_marker_cropped_small), 90, 122, false));
        iconMarkers.put("Tennis", Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.tennis_marker_cropped_small), 90, 122, false));
        iconMarkers.put("Volleyball", Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.volleyball_marker_cropped_small), 90, 122, false));
        iconMarkers.put("Running", Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.running_marker_cropped_small), 90, 122, false));
        iconMarkers.put("Exercise", Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.exercise_marker_cropped_small), 90, 122, false));
        iconMarkers.put("Soccer", Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.soccer_marker_cropped_small), 90, 122, false));
    }


    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    //Parse the list of users ids into an array
    private ArrayList<String> getEventIdList(DataSnapshot dataSnapshot) {
        ArrayList<String> usersId = new ArrayList<>();
        for (DataSnapshot id : dataSnapshot.child("usersIDs").getChildren()) {
            usersId.add(id.getValue(String.class));
        }
        return usersId;
    }

    //Add event to 'My Event' tab if user participate in this event
    private void sortEventToList(EventModel event) {
        String currUserId = currUser.getId();
        for (String id : event.getUsersIDs()) {
            if (id.equals(currUserId)) {
                myEvents.add(event);
            }
        }
        //Add to general event list according to preference
        if (currUserPref.isPrefByString(event.getType())) {
            eventList.add(event);
            if (tabTitle.equals("Map")) {
                addEventMarkerOnMap(event);
            }
        }
    }

    private int calculateDistanceMeters(double latitude, double longitude) {
        float[] result = new float[3];
        Location.distanceBetween(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), latitude, longitude, result);
        //  Return result distance in meters
        return (int) result[0];
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
                            if (lastKnownLocation != null && googleMap != null) {
                                //Set map camera on user location
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Toast.makeText(getActivity(), "Location Error", Toast.LENGTH_SHORT).show();
                        }
                        //After user location is determined, Show event list
                        initializeEventList();
                    }
                });
            } else {
                initializeEventList();
            }
        } catch (SecurityException e) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                    getDeviceLocation();
                } else {
                    locationPermissionGranted = false;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onRefresh() {
        getDeviceLocation();
    }

    public void sortLists(MainActivity.SortOption sortOption) {
        if (sortOption == MainActivity.SortOption.sortByDistance) {
            eventList.sort(EventModel.DistanceComparator);
            myEvents.sort(EventModel.DistanceComparator);
        } else if (sortOption == MainActivity.SortOption.sortByDate) {
            eventList.sort(EventModel.DateComparator);
            myEvents.sort(EventModel.DateComparator);
        }
        mAdapter.notifyDataSetChanged();
    }
}

