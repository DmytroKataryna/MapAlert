package com.example.dmytro.mapalert.utils;


import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesUtils {

    private static PreferencesUtils sUtils;
    private SharedPreferences sharedPref;

    // *****************  preferences data *****************
    private static final String KEY_SHARED_PREF = "ANDROID_MAP_ALERT";


    //  *************** Service ********************
    private static final String SERVICE_STATE = "boolean_service_state";
    private static final String SERVICE_DATA_CHANGED = "boolean_service_data_changed";


    public PreferencesUtils(Context context) {
        sharedPref = context.getApplicationContext().getSharedPreferences(KEY_SHARED_PREF,
                Context.MODE_MULTI_PROCESS);
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

    public void setServiceDataChanged(boolean serviceDataChanged) {
        sharedPref.edit().putBoolean(SERVICE_DATA_CHANGED, serviceDataChanged).apply();
    }

    public boolean isDataChanged() {
        return sharedPref.getBoolean(SERVICE_DATA_CHANGED, true);
    }
}
