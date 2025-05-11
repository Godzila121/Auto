package com.example.myapp;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
// Видалено імпорти Firebase, оскільки цей клас не повинен працювати з Firebase напряму

public class SharedPreferencesHelper {

    private static final String PREFS_NAME = "UserPrefs"; // Назва вашого файлу SharedPreferences
    private static final String KEY_EMAIL = "userEmailKey"; // Ключ для збереження email
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    // private static final String KEY_PASSWORD = "userPassword"; // ВИДАЛЕНО - не зберігайте паролі!
    // private static final String KEY_REGISTERED = "isRegistered"; // Розгляньте, чи потрібен цей ключ, якщо є Firebase Auth

    // Метод для збереження email користувача (наприклад, після успішного входу/реєстрації)
    public static void saveUserEmail(Context context, String email) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    public static String getSavedEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_EMAIL, null); // Повертати null, якщо email не знайдено
    }

    // Метод для збереження статусу входу
    public static void saveLoginStatus(Context context, boolean isLoggedIn) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        if (isLoggedIn && FirebaseAuth.getInstance().getCurrentUser() != null) {
            // Якщо користувач увійшов, автоматично зберігаємо його email
            saveUserEmail(context, FirebaseAuth.getInstance().getCurrentUser().getEmail());
        }
        editor.apply();
    }

    public static boolean getLoginStatus(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Додаткова перевірка: якщо Firebase каже, що користувач не увійшов, а SP каже, що увійшов - це розсинхронізація
        // Але для простоти поки що покладаємося на SP, якщо AuthStateListener не використовується активно для оновлення SP
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Метод для очищення email користувача (наприклад, при виході)
    public static void clearUserEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_EMAIL);
        editor.apply();
    }

    /**
     * Цей метод замінює ваш старий clearUserCredentials.
     * Він очищує статус входу та email.
     * Попередній clearUserCredentials з editor.clear() був небезпечний, бо міг видалити ВСІ дані з файлу.
     */
    public static void clearUserSessionData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove(KEY_EMAIL);
        // editor.remove(KEY_REGISTERED); // Якщо ви вирішили його залишити і очищати
        editor.apply();
    }


    // Старий метод saveUserCredentials потрібно замінити.
    // НЕ ЗБЕРІГАЙТЕ ПАРОЛЬ. Зберігайте лише email та статус реєстрації/входу.
    // Якщо вам потрібен метод для збереження інформації після реєстрації:
    public static void saveUserRegistrationInfo(Context context, String email) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        // editor.putBoolean(KEY_REGISTERED, true); // Якщо цей прапорець ще потрібен
        editor.putString(KEY_EMAIL, email); // Зберігаємо email
        editor.putBoolean(KEY_IS_LOGGED_IN, true); // Після реєстрації користувач зазвичай одразу входить
        editor.apply();
    }


    // ----------------------------------------------------------------------------------
    // ВСІ МЕТОДИ, ЩО СТОСУЮТЬСЯ FIRESTORE (getUserFavoritesCollection, addFavorite,
    // removeFavorite, getFavoriteCarIds, getCarsByIds, saveCarList, getCarList)
    // ПОВИННІ БУТИ ПЕРЕНЕСЕНІ У ВАШ КЛАС FirebaseHelper.java.
    // Вони не належать до SharedPreferencesHelper.
    // Ваш поточний SharedPreferencesHelper.java містить їх, і це неправильно.
    // ----------------------------------------------------------------------------------

}