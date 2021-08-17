package com.example.zuzu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

//This is the default page when opening the app
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText textUsername, textPassword;
    private TextInputLayout usernameLayout, passwordLayout;
    private Button loginButton;
    private Button registerButtonLogin;
    private ProgressBar progressBarLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.setTitle("Login");
        initializeLoginForm();


        //Todo: Delete after testing!
        textUsername.setText("odedza@gmail.com");
        textPassword.setText("123");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.registerButtonLogin:
                onRegisterButtonClick();
                break;
            case R.id.loginButton:
                onLoginButtonClick();
                break;
        }
    }

    private void initializeLoginForm() {
        usernameLayout = findViewById(R.id.usernameLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        textUsername = findViewById(R.id.username);
        textPassword = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);
        registerButtonLogin = findViewById(R.id.registerButtonLogin);
        registerButtonLogin.setOnClickListener(this);
        progressBarLogin = findViewById(R.id.progressBarLogin);
    }

    public void onLoginButtonClick() {
        if (isValidLogin()) {
            progressBarLogin.setVisibility(View.VISIBLE);
            isUserExist();
        } else {
            Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void onRegisterButtonClick() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }


    private boolean isValidLogin() {
        boolean isValid = true;

        clearWarnings();

        if (textUsername.getText().length() < 1) {
            usernameLayout.setError("Oopsi! Your email is missing");
            isValid = false;
        }
        if (!textUsername.getText().toString().toLowerCase().contains("@")) {
            usernameLayout.setError("Oopsi! Email address is invalid");
            isValid = false;
        }
        if (textPassword.getText().length() < 1) {
            passwordLayout.setError("Oopsi! Your password is missing");
            isValid = false;
        }

        return isValid;
    }

    private void clearWarnings() {
        usernameLayout.setErrorEnabled(false);
        passwordLayout.setErrorEnabled(false);
    }

    //Check if the user input match the user in the database, if true, opens the main activity else it will show an error
    private void isUserExist() {
        final String enteredEmail = textUsername.getText().toString().trim().toLowerCase();
        final String enteredPassword = textPassword.getText().toString().trim();

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserExist = usersRef.orderByChild("email").equalTo(enteredEmail);    //Create new query of the user

        checkUserExist.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userIdFromDB = null;

                if (dataSnapshot.exists()) {
                    for (DataSnapshot user : dataSnapshot.getChildren()) {
                        userIdFromDB = user.child("id").getValue(String.class);
                    }
                    String passwordFromDB = dataSnapshot.child(userIdFromDB).child("password").getValue(String.class);
                    if (passwordFromDB != null && passwordFromDB.equals(enteredPassword)) {
                        getUserFromDB(dataSnapshot, userIdFromDB, passwordFromDB);

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        passwordLayout.setError("Wrong password");
                        textPassword.requestFocus();
                    }
                } else {
                    usernameLayout.setError("No user found with this address");
                    textUsername.requestFocus();
                }
                progressBarLogin.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getUserFromDB(DataSnapshot dataSnapshot, String userIdFromDB, String passwordFromDB) {
        String firstNameFromDB = dataSnapshot.child(userIdFromDB).child("firstName").getValue(String.class);
        String lastNameFromDB = dataSnapshot.child(userIdFromDB).child("lastName").getValue(String.class);
        String emailFromDB = dataSnapshot.child(userIdFromDB).child("email").getValue(String.class);
        String phoneNoFromDB = dataSnapshot.child(userIdFromDB).child("phoneNo").getValue(String.class);
        String dobFromDB = dataSnapshot.child(userIdFromDB).child("dob").getValue(String.class);
        String genderFromDB = dataSnapshot.child(userIdFromDB).child("gender").getValue(String.class);
        Boolean isPrefSoccer = dataSnapshot.child(userIdFromDB).child("userPreferences").child("prefSoccer").getValue(boolean.class);
        Boolean isPrefVolleyball = dataSnapshot.child(userIdFromDB).child("userPreferences").child("prefVolleyball").getValue(boolean.class);
        Boolean isPrefBasketball = dataSnapshot.child(userIdFromDB).child("userPreferences").child("prefBasketball").getValue(boolean.class);
        Boolean isPrefRunning = dataSnapshot.child(userIdFromDB).child("userPreferences").child("prefRunning").getValue(boolean.class);
        Boolean isPrefTennis = dataSnapshot.child(userIdFromDB).child("userPreferences").child("prefTennis").getValue(boolean.class);
        Boolean isPrefExercise = dataSnapshot.child(userIdFromDB).child("userPreferences").child("prefExercise").getValue(boolean.class);

        UserPreferences userPreferences = new UserPreferences(isPrefSoccer, isPrefBasketball, isPrefVolleyball, isPrefRunning, isPrefTennis, isPrefExercise);
        UserModel user = new UserModel(firstNameFromDB, lastNameFromDB, emailFromDB, phoneNoFromDB, passwordFromDB, dobFromDB, genderFromDB, userPreferences);
        user.setId(userIdFromDB);
        ApplicationGlobal.setCurrentUser(user);
    }
}
