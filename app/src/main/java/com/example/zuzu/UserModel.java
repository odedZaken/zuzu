package com.example.zuzu;

import android.net.Uri;

import com.google.firebase.database.DatabaseReference;

import java.util.Date;
import java.util.UUID;

public class UserModel {

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    private String firstName, lastName, email, phoneNo, password, dob, gender, id;
    private UserPreferences userPreferences;
    private Uri profilePicUri;



    public UserModel(String firstName, String lastName, String email, String phoneNo, String password, String dob, String gender, UserPreferences userPreferences) {
        this.id = UUID.randomUUID().toString();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNo = phoneNo;
        this.password = password;
        this.dob = dob;
        this.gender = gender;
        this.profilePicUri = null;
        this.userPreferences = userPreferences;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserPreferences getUserPreferences() { return userPreferences; }

    public void setUserPreferences(UserPreferences userPreferences) { this.userPreferences = userPreferences; }

    public Uri getProfilePicUri() {return this.profilePicUri; }

    public void setProfilePicUri(Uri uri) {this.profilePicUri = uri; }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() { return firstName + " " + lastName; }

    public String getDisplayName() { return firstName + " " + lastName.charAt(0) + "."; }
}
