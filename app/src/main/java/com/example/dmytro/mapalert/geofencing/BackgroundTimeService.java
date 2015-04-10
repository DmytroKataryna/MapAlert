package com.example.dmytro.mapalert.geofencing;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.dmytro.mapalert.pojo.CursorLocation;
import com.example.dmytro.mapalert.pojo.LocationTime;

import java.util.Calendar;
import java.util.TreeSet;


public class BackgroundTimeService extends IntentService {

    private static final String TAG = "TimeServiceTAG";
    public static final String LOCATION_DATA = "location_data_from_data_base";
    public static final String BOOLEAN_DELETE_ALARM = "boolean_delete_alarms";

    public static final String BROADCAST_TIME_ACTION = "com.example.broadcast.time.action";

    private LocationTime locationItems;
    private boolean deleteAlarms;

    AlarmManager alarmManager;
    NotificationManager nm;
    Intent i;

    public BackgroundTimeService() {
        super("BackgroundTimeServiceName");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        i = new Intent(BROADCAST_TIME_ACTION);
    }

    //get all location , and add all time to AlarmManager
    @Override
    protected void onHandleIntent(Intent intent) {
        locationItems = convertCursorItemLocationToLocationTime((CursorLocation) intent.getSerializableExtra(LOCATION_DATA));
        deleteAlarms = intent.getBooleanExtra(BOOLEAN_DELETE_ALARM, false);

        for (Integer day : locationItems.getDays()) {

            if (deleteAlarms) {
                alarmManager.cancel(PendingIntent.getBroadcast(this, Integer.valueOf(locationItems.getDataBaseID() + "" + day), i, PendingIntent.FLAG_UPDATE_CURRENT));
            } else {
                Calendar calendar = Calendar.getInstance();
                if (day.equals(6))
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                else
                    calendar.set(Calendar.DAY_OF_WEEK, day + 2);

                calendar.set(Calendar.HOUR_OF_DAY, locationItems.getHour());
                calendar.set(Calendar.MINUTE, locationItems.getMinute());

                i.putExtra(AlarmManagerBroadcastReceiver.NOTIF_TITLE, locationItems.getTitle());
                i.putExtra(AlarmManagerBroadcastReceiver.NOTIF_IMG_PATH, locationItems.getImagePath());

                //pending id i set like DB id + Day number
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, Integer.valueOf(locationItems.getDataBaseID() + "" + day), i, PendingIntent.FLAG_UPDATE_CURRENT);

                long now = System.currentTimeMillis();
                if (now > calendar.getTimeInMillis()) {
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY * 7, AlarmManager.INTERVAL_DAY * 7, pendingIntent);  //send broadcast every week at certain time
                } else {
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);  //send broadcast every week at certain time
                }
            }
        }
    }


    public LocationTime convertCursorItemLocationToLocationTime(CursorLocation cursorLocations) {

        String[] time;
        TreeSet<Integer> selectedItems;

        if (cursorLocations.getItem().getTime() == null)
            time = new String[]{"0", "0"};
        else
            time = cursorLocations.getItem().getTime().split(" : ");

        if (cursorLocations.getItem().getRepeat() == null)
            selectedItems = new TreeSet<>();
        else
            selectedItems = cursorLocations.getItem().getRepeat();

        return new LocationTime(cursorLocations.getId(),
                Integer.valueOf(time[0]),
                Integer.valueOf(time[1]),
                selectedItems,
                cursorLocations.getItem().getTitle(),
                cursorLocations.getItem().getImagePath());
    }
}
