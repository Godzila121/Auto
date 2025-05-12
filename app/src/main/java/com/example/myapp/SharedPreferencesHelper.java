package com.example.myapp;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;

public class SharedPreferencesHelper {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_EMAIL = "userEmailKey";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    // private static final String KEY_REGISTERED = "isRegistered";

    public static void saveUserEmail(Context context, String email) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    public static String getSavedEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_EMAIL, null);
    }

    public static void saveLoginStatus(Context context, boolean isLoggedIn) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        if (isLoggedIn && FirebaseAuth.getInstance().getCurrentUser() != null) {
            saveUserEmail(context, FirebaseAuth.getInstance().getCurrentUser().getEmail());
        }
        editor.apply();
    }

    public static boolean getLoginStatus(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public static void clearUserEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_EMAIL);
        editor.apply();
    }


    public static void clearUserSessionData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove(KEY_EMAIL);
        // editor.remove(KEY_REGISTERED);
        editor.apply();
    }

    public static void saveUserRegistrationInfo(Context context, String email) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        // editor.putBoolean(KEY_REGISTERED, true); // Якщо цей прапорець ще потрібен
        editor.putString(KEY_EMAIL, email); // Зберігаємо email
        editor.putBoolean(KEY_IS_LOGGED_IN, true); // Після реєстрації користувач зазвичай одразу входить
        editor.apply();
    }
}