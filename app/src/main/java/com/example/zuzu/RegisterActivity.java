package com.example.zuzu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class RegisterActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {


    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;


    private TextView editDate;
    private Button setDateButton, registerButton;
    private DatePickerDialog datePickerDialog;
    private ImageView imageViewUser;
    private UserModel.Gender gender;
    private String dayOfBirth;
    private EditText editTextFirstName, editTextLastName, editTextEmail, editTextPassword, editTextConfirmPassword, editTextPhone;
    private TextView editTextFirstNameWarning, editTextLastNameWarning, editTextEmailWarning, editTextPasswordWarning;
    private TextView editDateWarning, genderWarning, editTextConfirmPasswordWarning, editTextPhoneWarning;
    private RadioGroup genderRadioGroup;
    private ProgressDialog progressDialog;

    private Uri profileImageUri;
    private FirebaseDatabase rootNode;
    private DatabaseReference reference;
    private StorageReference storageProfilePics;


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String dateStr = dayOfMonth + "/" + (month + 1) + "/" + year;
        editDate.setText(dateStr);
        dayOfBirth = editDate.getText().toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        rootNode = FirebaseDatabase.getInstance();
        storageProfilePics = FirebaseStorage.getInstance().getReference("profile_pics");
        initializeRegistrationForm();
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setDateButton:
                datePickerDialog = new DatePickerDialog(RegisterActivity.this);
                datePickerDialog.setOnDateSetListener(this);
                datePickerDialog.show();
                break;
            case R.id.registerButton:
                dismissWarnings();
                validationAndRegistration();
                break;
            case R.id.imageViewUser:
                createImageView();
            default:
                break;
        }
    }

    private void initializeRegistrationForm () {
        editDate = findViewById(R.id.editDate);
        editDate.setKeyListener(null);
        setDateButton = findViewById(R.id.setDateButton);
        setDateButton.setOnClickListener(this);
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextPhone = findViewById(R.id.editTextPhone);
        genderRadioGroup = findViewById(R.id.radioGroup2);
        editTextFirstNameWarning = findViewById(R.id.editTextFirstNameWarning);
        editTextLastNameWarning = findViewById(R.id.editTextLastNameWarning);
        editTextEmailWarning = findViewById(R.id.editTextEmailWarning);
        editTextPasswordWarning = findViewById(R.id.editTextPasswordWarning);
        editTextConfirmPasswordWarning = findViewById(R.id.editTextConfirmPasswordWarning);
        editTextPhoneWarning = findViewById(R.id.editTextPhoneWarning);
        editDateWarning = findViewById(R.id.editDateWarning);
        genderWarning = findViewById(R.id.genderWarning);
        registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(this);
        imageViewUser = findViewById(R.id.imageViewUser);
        imageViewUser.setOnClickListener(this);
    }

    private boolean isRegistrationFormValid() {
        boolean isError;
        editTextPasswordWarning.setText("You Must Choose a Password (At least 5 Characters)");
        editTextPhoneWarning.setText("You Must Enter a Phone Number");
        isError = validateEditText((EditText) editDate, editDateWarning);
        isError |= validateEditText(editTextFirstName, editTextFirstNameWarning);
        isError |= validateEditText(editTextLastName, editTextLastNameWarning);
        isError |= validateEditText(editTextEmail, editTextEmailWarning);
        isError |= validateEditText(editTextPassword, editTextPasswordWarning);
        isError |= validateEditText(editTextConfirmPassword, editTextConfirmPasswordWarning);
        isError |= validateEditText(editTextPhone, editTextPhoneWarning);
        isError |= validatePassword(editTextPassword, editTextConfirmPassword);
        if (genderRadioGroup.getCheckedRadioButtonId() == -1) {
            genderWarning.setVisibility(View.VISIBLE);
            genderWarning.requestFocus();
            isError = true;
        }
        return !isError;    //TODO: CHANGE RETURN VALUE TO '!isError' AFTER TESTING
    }

    private void dismissWarnings() {
        editTextFirstNameWarning.setVisibility(View.GONE);
        editTextLastNameWarning.setVisibility(View.GONE);
        editTextEmailWarning.setVisibility(View.GONE);
        editTextPasswordWarning.setVisibility(View.GONE);
        editTextConfirmPasswordWarning.setVisibility(View.GONE);
        editTextPhoneWarning.setVisibility(View.GONE);
        editDateWarning.setVisibility(View.GONE);
        genderWarning.setVisibility(View.GONE);
    }

    private boolean validatePassword(EditText password, EditText confirmedPassword) {
        if(password.getText().toString().equals(confirmedPassword.getText().toString())) {
            return false;
        }
        else {
            editTextConfirmPasswordWarning.setVisibility(View.VISIBLE);
            confirmedPassword.requestFocus();
            return true;
        }
    }

    public static boolean validateEditText(EditText editText, TextView warning) {
        if (editText.getText().length() < 2) {
            warning.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }

    private void validationAndRegistration() {
        if (isRegistrationFormValid()) {
            checkGender();
            isUserExist();
        }
        else {
            Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show();
        }
    }

    //initialize 'gender' attribute according to user input
    private void checkGender() {
        switch (genderRadioGroup.getCheckedRadioButtonId()) {
            case R.id.radioButtonMale:
                gender = UserModel.Gender.MALE;
                break;
            case R.id.radioButtonFemale:
                gender = UserModel.Gender.FEMALE;
                break;
            case R.id.radioButtonOther:
                gender = UserModel.Gender.OTHER;
                break;
            default:
                break;
        }
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
        }
    }

    //handle result of picked image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode,resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            //set image in imageView
            if (data != null && data.getData() != null) {
                profileImageUri = data.getData();
                imageViewUser.setImageURI(profileImageUri);
            }
        }
    }

    private void isUserExist() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating User...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        final String enteredEmail = editTextEmail.getText().toString().trim();
        DatabaseReference newReference = rootNode.getReference("users");
        Query checkUser = newReference.orderByChild("email").equalTo(enteredEmail);

        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //Check if user already exist in system
                    Toast.makeText(RegisterActivity.this, "Registration failed: This email is already registered", Toast.LENGTH_LONG).show();
                }
                else {
                    //Create a new User and continue:
                    createNewUser();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RegisterActivity.this, "Read from DB failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createNewUser() {
        reference = rootNode.getReference("users");
        String firstName = editTextFirstName.getText().toString();
        String lastName = editTextLastName.getText().toString();
        String email = editTextEmail.getText().toString().toLowerCase();
        String phoneNo = editTextPhone.getText().toString();
        String password = editTextPassword.getText().toString();
        UserModel newUser = new UserModel(firstName, lastName, email, phoneNo, password, dayOfBirth, gender.toString(), new UserPreferences());
        reference.child(newUser.getId()).setValue(newUser);

        if(profileImageUri != null) {
            newUser.setProfilePicUri(profileImageUri);
            uploadProfilePic(profileImageUri, email);
        } else {
            finishRegistration();
        }
        ApplicationGlobal.setCurrentUser(newUser);
    }

    private void uploadProfilePic(Uri imageUri, String email) {
        //Show loading progress bar

        byte[] bitmapData = getCompressedBitmapData(imageUri);

        if(bitmapData != null) {
            UploadTask uploadProfilePicTask = storageProfilePics.child(email).putBytes(bitmapData);
            uploadProfilePicTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    finishRegistration();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(RegisterActivity.this, "Picture upload failed!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        } else {
            Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    private byte[] getCompressedBitmapData(Uri imageUri){

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

    private void finishRegistration() {
        progressDialog.dismiss();
        Toast.makeText(this, "User created successfully", Toast.LENGTH_SHORT).show();
        this.finish();
    }
}