package com.example.dmytro.mapalert.utils;


import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesUtils {

    private static PreferencesUtils sUtils;
    private SharedPreferences sharedPref;

    // *****************  preferences data *****************
    private static final String KEY_SHARED_PREF = "ANDROID_MOVIE_LIST";
    private static final int KEY_MODE_PRIVATE = 0;

    //  *************** Service ********************
    public static final String SERVICE_STATE = "boolean_service_state";

    public PreferencesUtils(Context context) {
        sharedPref = context.getSharedPreferences(KEY_SHARED_PREF,
                KEY_MODE_PRIVATE);
    }

    public static PreferencesUtils get(Context c) {
        if (sUtils == null) {
            sUtils = new PreferencesUtils(c.getApplicationContext());
        }
        return sUtils;
    }

    public void setServiceState(boolean serviceState) {
        sharedPref.edit().putBoolean(SERVICE_STATE, serviceState).apply();
    }

    public boolean isServiceAlive() {
        return sharedPref.getBoolean(SERVICE_STATE, false);
    }
}
