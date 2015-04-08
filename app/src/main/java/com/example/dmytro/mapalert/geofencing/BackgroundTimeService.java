package com.example.dmytro.mapalert.geofencing;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.dmytro.mapalert.pojo.CursorLocation;
import com.example.dmytro.mapalert.pojo.LocationTime;
import com.example.dmytro.mapalert.utils.LocationDataSource;
import com.example.dmytro.mapalert.utils.PreferencesUtils;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class BackgroundTimeService extends IntentService {

    private static final String TAG = "BackgroundTimeServiceTAG";
    public static final String LOCATION_DATA = "location_data_from_data_base";

    public static final String BROADCAST_TIME_ACTION = "com.example.broadcast.time.action";

    private PreferencesUtils utils;
    private LocationDataSource dataSource;
    private LocationTime locationItems;

    AlarmManager alarmManager;
    NotificationManager nm;
    Intent i;

    public BackgroundTimeService() {
        super("BackgroundTimeServiceName");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        utils = PreferencesUtils.get(getApplicationContext());
        dataSource = LocationDataSource.get(getApplicationContext());

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        i = new Intent(BROADCAST_TIME_ACTION);
    }

    //get all location , and add all time to AlarmManager
    @Override
    protected void onHandleIntent(Intent intent) {
        locationItems = convertCursorItemLocationToLocationTime((CursorLocation) intent.getSerializableExtra(LOCATION_DATA));

        String s = locationItems.getHour() + "";
        String s2 = locationItems.getMinute() + "";

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, locationItems.getHour());
        //calendar.set(Calendar.DAY_OF_WEEK, 4);  set different alarm managers treeMap iterate
        calendar.set(Calendar.MINUTE, locationItems.getMinute());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, locationItems.getDataBaseID(), i, PendingIntent.FLAG_UPDATE_CURRENT);
        //alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 7 * 24 * 60 * 60 * 1000, pendingIntent);  //send broadcast every week at certain time

    }


    public LocationTime convertCursorItemLocationToLocationTime(CursorLocation cursorLocations) {


        String[] time = cursorLocations.getItem().getTime().split(" : ");
        LocationTime loc = new LocationTime(cursorLocations.getId(), Integer.valueOf(time[0]), Integer.valueOf(time[1]), cursorLocations.getItem().getRepeat());

        return loc;
    }
}
