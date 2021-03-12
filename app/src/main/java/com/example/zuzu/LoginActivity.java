package com.example.zuzu;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private static UserModel currentUser;
    private EditText textUsername;
    private EditText textPassword;
    private TextView usernameWarning, passwordWarning;
    private Button buttonLogin;         //todo: switch to listener button clicking
    private Button buttonRegister;
    private ProgressBar progressBarLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeLoginForm();
    }

    private void initializeLoginForm() {
        textUsername = findViewById(R.id.username);
        textPassword = findViewById(R.id.password);
        usernameWarning = findViewById(R.id.usernameWarning);
        passwordWarning = findViewById(R.id.passwordWarning);
        buttonLogin = findViewById(R.id.login);
        buttonRegister = findViewById(R.id.register);
        progressBarLogin = findViewById(R.id.progressBarLogin);
        currentUser = null;
    }

    public void onLoginButtonClick(View view) {
        if (isValidLogin()) {
            progressBarLogin.setVisibility(View.VISIBLE);
            isUserExist();
        }
        else {
            Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void onRegisterButtonClick(View view) {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }

    private boolean isValidLogin() {
        boolean isValid = true;
        clearWarnings();
        if (textUsername.getText().length() < 1) {
            usernameWarning.setText("You Must Enter an Email");
            usernameWarning.setVisibility(View.VISIBLE);
            isValid = false;
        }
        if (textPassword.getText().length() < 1) {
            passwordWarning.setText("You Must Enter a Password");
            passwordWarning.setVisibility(View.VISIBLE);
            isValid = false;
        }

        return isValid;
    }

    private void clearWarnings() {
        usernameWarning.setVisibility(View.GONE);
        passwordWarning.setVisibility(View.GONE);
    }

    private void isUserExist() {
        final String enteredEmail = textUsername.getText().toString().trim();
        final String enteredPassword = textPassword.getText().toString().trim();

        FirebaseDatabase rootDB = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = rootDB.getReference("users");
        Query checkUserExist = usersRef.orderByChild("email").equalTo(enteredEmail);

        checkUserExist.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String passwordFromDB = dataSnapshot.child(enteredEmail).child("password").getValue(String.class);
                    if(passwordFromDB!= null && passwordFromDB.equals(enteredPassword)) {
                        String firstNameFromDB = dataSnapshot.child(enteredEmail).child("firstName").getValue(String.class);
                        String lastNameFromDB = dataSnapshot.child(enteredEmail).child("lastName").getValue(String.class);
                        String emailFromDB = dataSnapshot.child("email").getValue(String.class);
                        String phoneNoFromDB = dataSnapshot.child("phoneNo").getValue(String.class);
                        String dobFromDB = dataSnapshot.child("dob").getValue(String.class);
                        String genderFromDB = dataSnapshot.child("gender").getValue(String.class);

                        currentUser = new UserModel(firstNameFromDB,lastNameFromDB,emailFromDB, phoneNoFromDB, passwordFromDB, dobFromDB, genderFromDB);

                        Toast.makeText(LoginActivity.this, "Welcome Back " + firstNameFromDB +" " + lastNameFromDB + ", Login Successful!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, EditProfileActivity.class);      //todo: Check if correct
                        startActivity(intent);
                    }
                    else {
                        passwordWarning.setText("Wrong Password");
                        passwordWarning.setVisibility(View.VISIBLE);
                        textPassword.requestFocus();
                    }
                }
                else {
                    usernameWarning.setText("No Such User Found");
                    usernameWarning.setVisibility(View.VISIBLE);
                    textUsername.requestFocus();
                }
                progressBarLogin.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public static UserModel getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(UserModel currentUser) {
        LoginActivity.currentUser = currentUser;
    }
}
