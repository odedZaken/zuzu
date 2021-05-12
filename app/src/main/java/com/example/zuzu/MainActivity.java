package com.example.zuzu;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import com.example.zuzu.ui.main.DiscoverFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.zuzu.ui.main.SectionsPagerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener , NavigationView.OnNavigationItemSelectedListener {

    private FloatingActionButton addEventFab;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private ImageView navDrawerProfilePic;
    private TextView navDrawerFullName, navDrawerEmail;
    private UserModel currUser;
    private ArrayList<EventModel> eventList;

    private DatabaseReference databaseReference;
    private StorageReference storageProfilePicsRef;
//    private Location lastKnownLocation;
//    private boolean locationPermissionGranted;
//    private FusedLocationProviderClient fusedLocationProviderClient;
//
//    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currUser = LoginActivity.getCurrentUser();
        storageProfilePicsRef = FirebaseStorage.getInstance().getReference().child("profile_pics");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("events");
        initializeMainActivity();
        initializeNavDrawer();
    }


    private void initializeMainActivity() {
//        lastKnownLocation = null;
//        getLocationPermission();
//        getDeviceLocation();
        ArrayList<String> tabText = new ArrayList<>();
        //Add tab names to array
        tabText.add("Discover");
        tabText.add("My Events");


        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        addEventFab = findViewById(R.id.addEventFab);
        addEventFab.setOnClickListener(this);
        prepareViewPager(tabText);
        viewPager.setAdapter(sectionsPagerAdapter);       //Supply the viewpager with fragments added to the adapter
        tabLayout.setupWithViewPager(viewPager);      //Links the tab layout with viewpager
    }


    private void initializeNavDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();
        navigationView.setCheckedItem(R.id.nav_home);
        //Get header layout for the navigation drawer and initialize header views
        View navHeaderLayout = navigationView.getHeaderView(0);
        navDrawerFullName = navHeaderLayout.findViewById(R.id.navDrawerFullName);
        navDrawerEmail = navHeaderLayout.findViewById(R.id.navDrawerEmail);
        navDrawerProfilePic = navHeaderLayout.findViewById(R.id.navDrawerProfilePic);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_closed);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        if (currUser != null) {
            navDrawerFullName.setText(currUser.getFullName());
            navDrawerEmail.setText(currUser.getEmail());
            getProfilePicFromDB();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch(item.getItemId()) {
            case R.id.nav_home:
                break;
            case R.id.nav_preferences:
                intent = new Intent(this, EditProfileActivity.class);
                startActivity(intent);
                drawerLayout.close();
                break;
            case R.id.nav_logout:
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                LoginActivity.setCurrentUser(null);
                Toast.makeText(this, "User logged out..", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case R.id.nav_invite_friends:
                Toast.makeText(this, "Coming soon..", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_about:
                Toast.makeText(this, "Coming soon..", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {

        if (drawerLayout.isOpen()) {
            drawerLayout.close();
        } else {
            super.onBackPressed();
        }
    }


    private void getProfilePicFromDB() {
        final String currUserEmail = currUser.getEmail();
        storageProfilePicsRef.child(currUserEmail).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                StorageReference userProfilePic = storageProfilePicsRef.child(currUserEmail);
                userProfilePic.getBytes(EditProfileActivity.MAX_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        navDrawerProfilePic.setImageBitmap(bitmap);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to fetch profile picture..", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }



    private void prepareViewPager(ArrayList<String> tabText) {
        //Initialize main adapter
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        //Initialize discover events fragment
        DiscoverFragment fragment = new DiscoverFragment();

        for (int i=0; i < tabText.size(); i++) {
            Bundle bundle = new Bundle();       //This bundle contains parameters for the fragment
            bundle.putString("title",tabText.get(i));
//            bundle.putParcelable("userLocation", lastKnownLocation);
            fragment.setArguments(bundle);
            sectionsPagerAdapter.addFragment(fragment, tabText.get(i));
            fragment = new DiscoverFragment();      //Create a new fragment for 'My Events'
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this,CreateEventActivity.class);
        startActivity(intent);
    }


//    private void getDeviceLocation() {
//        try {
//            //Get last known location
//            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
//            if (locationPermissionGranted) {
//                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
//                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Location> task) {
//                        if (task.isSuccessful()) {
//                            lastKnownLocation = task.getResult();
//                        }
//                    }
//                });
//            }
//        } catch(SecurityException e) {
//            Log.e("Exception: %s", e.getMessage(), e);
//        }
//    }
//
//    private void getLocationPermission() {
//
//        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
//            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
//            requestPermissions(permissions, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
//            locationPermissionGranted = false;
//        } else {
//            locationPermissionGranted = true;
//        }
//    }
}