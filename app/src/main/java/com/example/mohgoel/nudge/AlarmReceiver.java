package com.example.mohgoel.nudge;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

/**
 * class implementing BroadcastReceiver to receive Broadcast,
 * for starting services and perform other operations like sending notification
 */
public class AlarmReceiver extends BroadcastReceiver {
    public static final int NOTIFICATION_REQUEST_CODE = 1;
    public static final int REMINDER_NOTIFICATION_ID = 2200;
    public static final String TASK_NAME = "taskName";

    public static final String NOTIFICATION_CHANNEL_ID = "my_notification_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskName = intent.getStringExtra(TASK_NAME);
        Intent notIntent = new Intent(context, HomeActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(context, NOTIFICATION_REQUEST_CODE,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        builder.setContentIntent(pIntent)
                .setSmallIcon(R.drawable.ic_no_reminder)
                .setTicker(context.getString(R.string.notification_ticker))
                .setContentTitle(context.getString(R.string.notification_ticker))
                .setAutoCancel(true)
                .setContentText(taskName);

        Notification not = builder.build();

        NotificationManager notifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID, "Basic Notifications", NotificationManager.IMPORTANCE_DEFAULT);

            // configure the notification channel.
            notificationChannel.setDescription("Sample app for basic notification");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notifyMgr.createNotificationChannel(notificationChannel);
        }

        notifyMgr.notify(REMINDER_NOTIFICATION_ID, not);
        Toast.makeText(context, "Alarm recieved", Toast.LENGTH_SHORT).show();
    }
}
