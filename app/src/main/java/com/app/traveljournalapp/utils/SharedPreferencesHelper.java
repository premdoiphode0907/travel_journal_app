package com.app.traveljournalapp.utils;


import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {

    private static final String PREF_NAME = "TravelJournalPrefs";
    private static final String IS_LOGGED_IN_KEY = "isLoggedIn";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setUserId(int userId) {
        editor.putInt("userId", userId);
        editor.apply();
    }

    public int getUserId() {
        return sharedPreferences.getInt("userId", -1);  // Default value is -1 if not found
    }

    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(IS_LOGGED_IN_KEY, isLoggedIn);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(IS_LOGGED_IN_KEY, false);
    }
}

