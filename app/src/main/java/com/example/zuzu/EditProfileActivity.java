package com.example.zuzu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener, MaterialButtonToggleGroup.OnButtonCheckedListener,
        NavigationView.OnNavigationItemSelectedListener {

    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;
    public static final long MAX_SIZE = 1024 * 1024 * 10;     //Set to 10 Megabytes

    private TextView textViewFullName, navDrawerEmail, navDrawerFullName;
    private EditText editTextFirstName, editTextLastName, editTextPhone;
    private TextInputLayout editTextFirstNameLayout, editTextLastNameLayout, editTextPhoneLayout;
    private ImageView imageViewProfileImage, navDrawerProfilePic;
    private ImageButton editPropertiesButton;
    private boolean isEditState;      //configures the state of 'editPropertiesButton'
    private MaterialButtonToggleGroup toggleGroupInterests;
    private CardView imageCardView;

    private DatabaseReference databaseReference;
    private StorageReference storageProfilePicsRef;
    //    private Uri imageUri;
    private UserModel currUser;
    private UserPreferences userPreferences;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        currUser = ApplicationGlobal.getCurrentUser();
        if (currUser != null) {
            userPreferences = currUser.getUserPreferences();
        }
        initializeEditProfileForm();
        retrieveUserData();
//        retrieveProfilePic();
        retrieveUserPreferences();
        initializeNavDrawer();
    }

    private void initializeNavDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();
        navigationView.setCheckedItem(R.id.nav_preferences);
        //Get header layout for the navigation drawer and initialize header views
        View navHeaderLayout = navigationView.getHeaderView(0);
        navDrawerFullName = navHeaderLayout.findViewById(R.id.navDrawerFullName);
        navDrawerEmail = navHeaderLayout.findViewById(R.id.navDrawerEmail);
        navDrawerProfilePic = navHeaderLayout.findViewById(R.id.navDrawerProfilePic);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_closed);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        if (currUser != null) {
            navDrawerFullName.setText(currUser.getFullName());
            navDrawerEmail.setText(currUser.getEmail());
            Bitmap profilePic = ApplicationGlobal.getUserProfilePic();
            if(profilePic != null) {
                imageViewProfileImage.setImageBitmap(profilePic);
                navDrawerProfilePic.setImageBitmap(profilePic);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isOpen()) {
            drawerLayout.close();
        } else {
            super.onBackPressed();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.nav_home:
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                drawerLayout.close();
                finish();
                break;
            case R.id.nav_preferences:
                drawerLayout.close();
                break;
            case R.id.nav_logout:
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                ApplicationGlobal.setCurrentUser(null);
                ApplicationGlobal.setUserProfilePic(null);
                Toast.makeText(this, "User logged out..", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case R.id.nav_invite_friends:
            case R.id.nav_about:
                Toast.makeText(this, "Coming soon..", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profileImageCard:
                createImageView();
                break;
            case R.id.imageButtonEditProperties:
                editPropertiesHandler();
                break;
            default:
                break;
        }
    }

    private void initializeEditProfileForm() {
        storageProfilePicsRef = FirebaseStorage.getInstance().getReference().child("profile_pics");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        toggleGroupInterests = findViewById(R.id.toggleGroupInterests);
        toggleGroupInterests.addOnButtonCheckedListener(this);
        textViewFullName = findViewById(R.id.textViewFullName);
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextPhone = findViewById(R.id.editTextPhone);
        imageViewProfileImage = findViewById(R.id.imageViewProfileImage);
        imageCardView = findViewById(R.id.profileImageCard);
        imageCardView.setOnClickListener(this);
        editPropertiesButton = findViewById(R.id.imageButtonEditProperties);
        editPropertiesButton.setOnClickListener(this);
        isEditState = false;
        editTextFirstNameLayout = findViewById(R.id.editTextFirstNameLayout);
        editTextLastNameLayout = findViewById(R.id.editTextLastNameLayout);
        editTextPhoneLayout = findViewById(R.id.editTextPhoneLayout);
    }

    //Retrieve user data from current user (local memory)
    private void retrieveUserData() {
        textViewFullName.setText(currUser.getFullName());
        editTextFirstName.setText(currUser.getFirstName());
        editTextLastName.setText(currUser.getLastName());
        editTextPhone.setText(currUser.getPhoneNo());
    }

    private void retrieveUserPreferences() {
        userPreferences = currUser.getUserPreferences();
        checkInterestButton(R.id.soccerButton, userPreferences.getPrefSoccer());
        checkInterestButton(R.id.basketballButton, userPreferences.getPrefBasketball());
        checkInterestButton(R.id.volleyballButton, userPreferences.getPrefVolleyball());
        checkInterestButton(R.id.runningButton, userPreferences.getPrefRunning());
        checkInterestButton(R.id.tennisButton, userPreferences.getPrefTennis());
        checkInterestButton(R.id.exerciseButton, userPreferences.getPrefExercise());
    }

    private void checkInterestButton(int buttonId, boolean isPref) {
        if (isPref) {
            toggleGroupInterests.check(buttonId);
        }
    }

    //This method handles 'edit properties' button
    private void editPropertiesHandler() {
        editPropertiesButton.setEnabled(false);
        if (!isEditState) {
            editPropertiesToggle(true);
            editPropertiesButton.setImageResource(R.drawable.ic_baseline_check_24);
        } else if (isValidPropertiesChange()) {
            editPropertiesToggle(false);
            dismissAllWarnings();
            uploadNewProperties();
            textViewFullName.setText(currUser.getFullName());
            editPropertiesButton.setImageResource(R.drawable.ic_action_edit);
            Toast.makeText(this, "Changes saved successfully!", Toast.LENGTH_SHORT).show();
        }
        editPropertiesButton.setEnabled(true);
    }


    //Upload new properties to database and save in 'currentUser'
    private void uploadNewProperties() {
        DatabaseReference userReference = databaseReference.child(currUser.getId());
        userReference.child("firstName").setValue(editTextFirstName.getText().toString());
        userReference.child("lastName").setValue(editTextLastName.getText().toString());
        userReference.child("phoneNo").setValue(editTextPhone.getText().toString());
        currUser.setFirstName(editTextFirstName.getText().toString());
        currUser.setLastName(editTextLastName.getText().toString());
        currUser.setPhoneNo(editTextPhone.getText().toString());
    }

    //Hide all warnings in form
    private void dismissAllWarnings() {
        editTextFirstNameLayout.setErrorEnabled(false);
        editTextLastNameLayout.setErrorEnabled(false);
        editTextPhoneLayout.setErrorEnabled(false);
    }

    //Check if all editTexts are legal
    private boolean isValidPropertiesChange() {
        boolean isError;
        isError = validateEditText(editTextFirstName, editTextFirstNameLayout);
        isError |= validateEditText(editTextLastName, editTextLastNameLayout);
        isError |= validateEditText(editTextPhone, editTextPhoneLayout);
        return !isError;
    }

    private boolean validateEditText(EditText editText, TextInputLayout layout) {
        if (editText.getText().length() < 2) {
            layout.setError("Field can't be empty");
            return true;
        }
        return false;
    }

    //Change the state of the form (editable or not)
    private void editPropertiesToggle(boolean newState) {
        isEditState = newState;
        editTextFirstName.setEnabled(newState);
        editTextLastName.setEnabled(newState);
        editTextPhone.setEnabled(newState);
    }


    private void createImageView() {
        //checks if user already gave permission
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            requestPermissions(permissions, PERMISSION_CODE);
        } else {
            pickImageFromGallery();
        }
    }

    private void pickImageFromGallery() {
        //intent to pick an image
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    //handle result of runtime permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                                            && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    //handle result of picked image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            //set image to image view
            if (data != null) {
                Uri imageUri = data.getData();
                imageViewProfileImage.setImageURI(imageUri);
                navDrawerProfilePic.setImageURI(imageUri);
                uploadProfilePic(imageUri, currUser.getEmail());
            }
        }
    }

    private void uploadProfilePic(Uri imageUri, String email) {
        //Show loading progress bar
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        byte[] bitmapData = getCompressedBitmapData(imageUri);
        if(bitmapData != null) {
            UploadTask uploadProfilePicTask = storageProfilePicsRef.child(email).putBytes(bitmapData);
            uploadProfilePicTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(EditProfileActivity.this, "Picture change successful!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfileActivity.this, "Picture change failed!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        } else {
            Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }


    private byte[] getCompressedBitmapData(Uri imageUri){
        //Compress image and convert to byte[]
        Bitmap fullSizeBitmap = null;
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                fullSizeBitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.getContentResolver(),imageUri));
            } else {
                fullSizeBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(fullSizeBitmap != null) {
            Bitmap reducedBitmap = ImageResizer.reduceBitmapSize(fullSizeBitmap, 240000);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            reducedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            return baos.toByteArray();
        } else {
            Toast.makeText(this, "Error compressing image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Override
    public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
        switch (checkedId) {
            case R.id.soccerButton:
                userPreferences.setPrefSoccer(isChecked);
                break;
            case R.id.basketballButton:
                userPreferences.setPrefBasketball(isChecked);
                break;
            case R.id.volleyballButton:
                userPreferences.setPrefVolleyball(isChecked);
                break;
            case R.id.runningButton:
                userPreferences.setPrefRunning(isChecked);
                break;
            case R.id.tennisButton:
                userPreferences.setPrefTennis(isChecked);
                break;
            case R.id.exerciseButton:
                userPreferences.setPrefExercise(isChecked);
                break;
            default:
                break;
        }
        databaseReference.child(currUser.getId()).child("userPreferences").setValue(userPreferences);
    }
}