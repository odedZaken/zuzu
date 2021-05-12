package com.example.zuzu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;

import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;


public class MapActivity extends AppCompatActivity implements GoogleMap.OnMapClickListener, View.OnClickListener {


    private GoogleMap googleMap;
    private Location lastKnownLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean locationPermissionGranted;
    private FloatingActionButton clearEventLocation, saveEventLocation;
    private LatLng eventCurrentLocation;

    private static final int DEFAULT_ZOOM = 16;
    private static final int CLICK_MAP_ZOOM = 17;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;


    @Override
    public void onMapClick(LatLng latLng) {
        //When clicked on map
        //Initialize marker options
        MarkerOptions markerOptions = new MarkerOptions();
        //Set position of marker
        markerOptions.position(latLng);
        markerOptions.title("Event Location");

        if (lastKnownLocation != null) {
            float[] result = new float[3];
            Location.distanceBetween(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude(),latLng.latitude,latLng.longitude, result);
            markerOptions.snippet("Distance to marker: " + (int)result[0] + "m");
        }

        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        googleMap.clear();
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, CLICK_MAP_ZOOM));
        googleMap.addMarker(markerOptions);
        eventCurrentLocation = latLng;
    }

    public MapActivity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        this.setTitle("Choose event location");
        initializeMapForm();
        getLocationPermission();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //Initialize map fragment
        SupportMapFragment supportMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.googleMap);
        //Async map
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NotNull GoogleMap googleMap) {
                //When map is loaded
                googleMap.setOnMapClickListener(MapActivity.this);
                MapActivity.this.googleMap = googleMap;
                // Turn on the My Location layer and the related control on the map.
                updateLocationUI();
                // Get the current location of the device and set the position of the map.
                getDeviceLocation();
            }
        });
    }

    private void initializeMapForm() {
        eventCurrentLocation = null;
        clearEventLocation = findViewById(R.id.clearEventLocation);
        clearEventLocation.setOnClickListener(this);
        saveEventLocation = findViewById(R.id.saveEventLocation);
        saveEventLocation.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.saveEventLocation:
                if(eventCurrentLocation != null) {
                    CreateEventActivity.setEventLocation(eventCurrentLocation);
                    Toast.makeText(this, "Location Saved!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Choose a point on map first", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.clearEventLocation:
                googleMap.clear();
                break;
        }
    }

    @SuppressLint("MissingPermission")
    private void updateLocationUI() {
        if (googleMap != null) {
            try {
                if(locationPermissionGranted) {
                    googleMap.setMyLocationEnabled(true);
                    googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                    googleMap.getUiSettings().setZoomControlsEnabled(true);
                } else {
                    googleMap.setMyLocationEnabled(false);
                    googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                    lastKnownLocation = null;
                }
            } catch (SecurityException e) {
                Log.e("Exception: %s", e.getMessage());
            }
        }
    }

    @Override
    public boolean onNavigateUp() {
        Toast.makeText(this, "Back button pressed!", Toast.LENGTH_SHORT).show();
        return super.onNavigateUp();
    }

    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            lastKnownLocation = task.getResult();
                            if(lastKnownLocation != null) {
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        }
                    }
                });
            }
        } catch(SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void getLocationPermission() {

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
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
                } else {
                    locationPermissionGranted = false;
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}