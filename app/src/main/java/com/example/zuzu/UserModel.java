package com.example.zuzu;

import android.net.Uri;

import com.google.firebase.database.DatabaseReference;

import java.util.Date;

public class UserModel {

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    private String firstName, lastName, email, phoneNo, password, dob, gender;
    private Boolean isPrefSoccer, isPrefBasketball, isPrefVolleyball, isPrefRunning, isPrefTennis, isPrefExercise;
    private Uri profilePicUri;



    public UserModel(String firstName, String lastName, String email, String phoneNo, String password, String dob, String gender) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNo = phoneNo;
        this.password = password;
        this.dob = dob;
        this.gender = gender;
        this.isPrefSoccer = Boolean.FALSE;
        this.isPrefBasketball = Boolean.FALSE;
        this.isPrefVolleyball = Boolean.FALSE;
        this.isPrefRunning = Boolean.FALSE;
        this.isPrefTennis = Boolean.FALSE;
        this.isPrefExercise = Boolean.FALSE;
        this.profilePicUri = null;
    }

    public Uri getProfilePicUri() {return this.profilePicUri; }

    public void setProfilePicUri(Uri uri) {this.profilePicUri = uri; }

    public Boolean getIsPrefSoccer() {return this.isPrefSoccer;}

    public void setIsPrefSoccer(Boolean pref) {this.isPrefSoccer = pref;}

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

}
