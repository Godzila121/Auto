package com.example.myapp;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class SharedPreferencesHelper {

    private static final String PREFS_NAME = "favorite_cars_prefs";
    private static final String FAVORITES_KEY = "favorites";

    public static void addFavorite(Context context, String carName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> favorites = prefs.getStringSet(FAVORITES_KEY, new HashSet<>());
        favorites.add(carName);
        prefs.edit().putStringSet(FAVORITES_KEY, favorites).apply();
    }

    public static void removeFavorite(Context context, String carName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> favorites = prefs.getStringSet(FAVORITES_KEY, new HashSet<>());
        favorites.remove(carName);
        prefs.edit().putStringSet(FAVORITES_KEY, favorites).apply();
    }

    public static Set<String> getFavorites(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getStringSet(FAVORITES_KEY, new HashSet<>());
    }
}
