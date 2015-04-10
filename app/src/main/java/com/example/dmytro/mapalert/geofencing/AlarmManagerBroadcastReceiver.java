package com.example.dmytro.mapalert.geofencing;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.example.dmytro.mapalert.R;
import com.example.dmytro.mapalert.activities.ListActivity;

import java.util.Random;

public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {

    final public static String ONE_TIME = "onetime";
    final public static String NOTIF_TITLE = "notification_title";
    final public static String NOTIF_IMG_PATH = "notification_image_path";


    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra(NOTIF_TITLE);
        String imagePAth = intent.getStringExtra(NOTIF_IMG_PATH);

        sendNotification(context, title, imagePAth);

    }

    private void sendNotification(Context context, String title, String imagePath) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager
                .newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "MyWakelockTag");

        wakeLock.acquire();

        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(context, ListActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(ListActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setDefaults(Notification.DEFAULT_ALL)
                .setVibrate(new long[]{0, 100, 1000, 300, 200, 100, 500, 200, 100})
                .setColor(Color.RED)
                .setContentTitle("Check your " + title + " actions") //title
                .setContentText("Tap here to see your location details information")
                .setLargeIcon(BitmapFactory.decodeFile(imagePath))
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

//        Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        // Issue the notification
        mNotificationManager.notify(new Random().nextInt(1000), builder.build());

        wakeLock.release();
    }
}
