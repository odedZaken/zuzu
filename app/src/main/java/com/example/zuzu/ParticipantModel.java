package com.example.zuzu;

import android.graphics.Bitmap;
import android.net.Uri;
import java.util.Calendar;

public class ParticipantModel {

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    private String firstName, lastName, dob, gender, userID, email;
    private Bitmap profilePic;

    public ParticipantModel(String firstName, String lastName, String dob, String gender, String userID, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.gender = gender;
        this.userID = userID;
        this.email = email;
        this.profilePic = null;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        String editGender = gender.substring(0,1).toUpperCase();
        return editGender + gender.substring(1).toLowerCase();
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public Bitmap getProfilePic() { return profilePic; }

    public void setProfilePic(Bitmap profilePic) { this.profilePic = profilePic; }

    public String getDisplayName() { return firstName + " " + lastName.charAt(0) + "."; }

    public String getAge() {
        String[] splitDOB = this.dob.split("/");
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(Integer.parseInt(splitDOB[2]), Integer.parseInt(splitDOB[1]), Integer.parseInt(splitDOB[0]));

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        Integer ageInt = new Integer(age);
        String ageS = ageInt.toString();

        return ageS;
    }

    public String getGenderAgeString() {
        return getGender() + ", " + getAge();
    }

}
