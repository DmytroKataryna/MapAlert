package com.example.dmytro.mapalert.geofencing.v2;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.example.dmytro.mapalert.R;
import com.example.dmytro.mapalert.activities.ListActivity;


public class NotificationReceiver extends BroadcastReceiver {

    protected static final String TAG = "geofence-broadcast";

    @Override
    public void onReceive(Context context, Intent intent) {
        sendNotification(context, "LEAVE AREA");
    }

    private void sendNotification(Context context, String notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(context, ListActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Add the main Activity to the task stack as the parent.
         stackBuilder.addParentStack(ListActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        // Define the notification settings.
        builder.setSmallIcon(R.mipmap.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails) //title
                .setContentText(context.getString(R.string.geofence_transition_notification_text)) //description
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }
}
//    public void sendNotification(Context context) {
//        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        Intent x = new Intent(context, ListActivity.class);
//        Notification notification = new Notification.Builder(context)
//                .setContentIntent(PendingIntent.getActivity(context, 0, x, PendingIntent.FLAG_UPDATE_CURRENT))
//                .setDefaults(Notification.DEFAULT_ALL)
//                .setAutoCancel(true).setWhen(System.currentTimeMillis())
//                .setContentTitle("AREA ENTER / EXIT  ")
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setContentText("You leave area ")
//                .build();
//        notification.flags |= Notification.FLAG_INSISTENT;
//        nm.notify(0, notification);
//    }
