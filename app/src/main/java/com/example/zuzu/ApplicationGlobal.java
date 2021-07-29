package com.example.zuzu;

import android.app.Application;
import android.graphics.Bitmap;

public class ApplicationGlobal extends Application {

    private static UserModel currentUser;
    private static Bitmap userProfilePic;

    public ApplicationGlobal() {
        //Empty Constructor
    }

    public static UserModel getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(UserModel currentUser) {
        ApplicationGlobal.currentUser = currentUser;
    }

    public static Bitmap getUserProfilePic() {
        return userProfilePic;
    }

    public static void setUserProfilePic(Bitmap userProfilePic) {
        ApplicationGlobal.userProfilePic = userProfilePic;
    }
}
