package com.example.zuzu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;

    private TextView textViewFullName;
    private EditText editTextFirstName, editTextLastName, editTextEmail, editTextPhone;
    private ImageView imageViewProfileImage;

    private DatabaseReference databaseReference;

    private Uri imageUri;
    private String myUri = "";
    private StorageTask uploadTask;
    private StorageReference storageProfilePicsReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initializeEditProfileForm();
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
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        storageProfilePicsReference = FirebaseStorage.getInstance().getReference().child("profile_pics");
        textViewFullName = findViewById(R.id.textViewFullName);
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        imageViewProfileImage = findViewById(R.id.imageViewProfileImage);
        imageViewProfileImage.setOnClickListener(this);
        imageViewProfileImage.setVisibility(View.VISIBLE);
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
        }
    }

    private void uploadProfileImage() {
        final StorageReference fileReference = storageProfilePicsReference.child(LoginActivity.getCurrentUser().getEmail() +".jpg");        //todo: change to .png
        uploadTask = fileReference.putFile(imageUri);

        uploadTask.continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileReference;
            }
        });
    }
}