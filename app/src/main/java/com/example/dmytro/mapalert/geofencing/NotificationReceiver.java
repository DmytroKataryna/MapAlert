package com.example.dmytro.mapalert.geofencing;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;

import com.example.dmytro.mapalert.R;
import com.example.dmytro.mapalert.activities.ListActivity;

import java.util.Random;


public class NotificationReceiver extends BroadcastReceiver {

    protected static final String TAG = "geofence-broadcast";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra(BackgroundLocationService.NOTIF_TITLE_EXTRA);
        String description = intent.getStringExtra(BackgroundLocationService.NOTIF_DESCRIPTION_EXTRA);
        String imagePath = intent.getStringExtra(BackgroundLocationService.NOTIF_IMAGE_PATH_EXTRA);
        sendNotification(context, title, description, imagePath);
    }

    private void sendNotification(Context context, String notificationTitle, String notificationDescription, String imagePath) {
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

//        Notification notif = new NotificationCompat.Builder(context)
//                .setContentTitle(notificationTitle)
//                .setContentText(notificationDescription)
//                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setGroup("MapAlert")
//                .setContentIntent(notificationPendingIntent)
//                .addPerson("personMap")
//                .build();
//
//        NotificationManagerCompat notificationManager =
//                NotificationManagerCompat.from(context);
//        notificationManager.notify(new Random().nextInt(1000), notif);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        // Define the notification settings.
        builder.setSmallIcon(R.mipmap.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeFile(imagePath))
                .setColor(Color.RED)
                .setGroup("MapAlert")
                .setGroupSummary(true)
                .setContentTitle(notificationTitle) //title
                .setContentText("Tap here to see your location details information")
                        //.setContentText(notificationDescription) //description
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

//        Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        // Issue the notification
        mNotificationManager.notify(new Random().nextInt(1000), builder.build());
    }
}