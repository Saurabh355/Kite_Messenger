package com.example.kitemessenger;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private final String CHANNEL_ID_1 = "Channel";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
      NotificationManager notificationManager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        String notification_title = remoteMessage.getNotification().getTitle();
        String notification_message = remoteMessage.getNotification().getBody();
        String click_action =remoteMessage.getNotification().getClickAction();
        String from_user_id= remoteMessage.getData().get("from_user_id");

        //-----------------------------------------------------------------------------------------------------

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID_1,
                    "Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );

             notificationManager.createNotificationChannel(channel);
            //-----------------------------------------------------------------------


            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this, CHANNEL_ID_1)
                            .setSmallIcon(R.mipmap.ic_launcher_round)
                            .setContentTitle(notification_title)
                            .setContentText(notification_message)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setAutoCancel(true);

            //   notificationManager.createNotificationChannel(channel);

            Intent resultIntent = new Intent(click_action);
            resultIntent.putExtra("user_id", from_user_id);
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);

            int mNotificationId = (int) System.currentTimeMillis();
            notificationManager.notify(mNotificationId, mBuilder.build());



        }

    }
}

