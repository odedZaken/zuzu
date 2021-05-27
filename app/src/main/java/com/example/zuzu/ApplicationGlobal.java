package com.example.zuzu;

import android.app.Application;

public class ApplicationGlobal extends Application {

    private static UserModel currentUser;

    public ApplicationGlobal() {
        //Empty Constructor
    }

    public static UserModel getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(UserModel currentUser) {
        ApplicationGlobal.currentUser = currentUser;
    }
}
