package com.example.myapp;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPreferencesHelper {
    private static final String PREF_NAME = "MY_APP_PREFS";
    private static final String FAVORITES_KEY = "favorite_cars";

    public static void saveFavorites(Context context, List<Car> favoriteCars) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(favoriteCars);
        editor.putString(FAVORITES_KEY, json);
        editor.apply();
    }

    public static List<Car> getFavorites(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(FAVORITES_KEY, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Car>>() {}.getType();
        return gson.fromJson(json, type);
    }
}
