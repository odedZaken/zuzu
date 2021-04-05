package com.example.zuzu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;
    final long MAX_SIZE = 1024 * 1024 * 10;     //Set to 10 Megabytes

    private TextView textViewFullName;
    private EditText editTextFirstName, editTextLastName, editTextEmail, editTextPhone;
    private ImageView imageViewProfileImage;

    private DatabaseReference databaseReference;
    private StorageReference storageProfilePicsRef;
    private Uri imageUri;
    private UserModel currUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        currUser = LoginActivity.getCurrentUser();
        initializeEditProfileForm();
        retrieveUserData();
        retrieveProfilePic();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageViewProfileImage:
                createImageView();
                break;
            default:
                break;
        }
    }

    private void initializeEditProfileForm() {
        storageProfilePicsRef = FirebaseStorage.getInstance().getReference().child("profile_pics");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        textViewFullName = findViewById(R.id.textViewFullName);
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        imageViewProfileImage = findViewById(R.id.imageViewProfileImage);
        imageViewProfileImage.setOnClickListener(this);
        imageViewProfileImage.setVisibility(View.VISIBLE);
    }

    //Retrieve user data from current user (local memory)
    private void retrieveUserData() {
        String fullName = currUser.getFirstName() + ' ' + currUser.getLastName();
        textViewFullName.setText(fullName);
        editTextFirstName.setText(currUser.getFirstName());
        editTextLastName.setText(currUser.getLastName());
        editTextEmail.setText(currUser.getEmail());
        editTextPhone.setText(currUser.getPhoneNo());
    }

    //Retrieve user profile picture from database and show in ImageView using Glide
    private void retrieveProfilePic() {

        StorageReference userProfilePic = storageProfilePicsRef.child(currUser.getEmail());
        userProfilePic.getBytes(MAX_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageViewProfileImage.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProfileActivity.this, "Failed to fetch!", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void createImageView() {
        //checks if user already gave permission
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
            requestPermissions(permissions, PERMISSION_CODE);
        }
        else {
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
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
        super.onActivityResult(requestCode,resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            //set image to image view
            imageUri = data.getData();
            imageViewProfileImage.setImageURI(imageUri);
            uploadProfilePic(imageUri,currUser.getEmail());
        }
    }

    private void uploadProfilePic(Uri imageUri, String email) {
        UploadTask uploadProfilePicTask = storageProfilePicsRef.child(email).putFile(imageUri);
        uploadProfilePicTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(EditProfileActivity.this, "pic upload successful!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProfileActivity.this, "pic upload failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}