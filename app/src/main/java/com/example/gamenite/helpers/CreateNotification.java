package com.example.gamenite.helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.gamenite.MainActivity;
import com.example.gamenite.R;

public class CreateNotification {

    public static final String CHANNEL_ONE = "channel_one";
    public static final String CHANNEL_TWO = "channel_two";
    public static final String CHANNEL_THREE = "channel_three";
    public static final String GROUP_ONE = "group_one";
    public static final String GROUP_TWO = "group_two";
    public static final int EVENT_FRAGMENT = 1;
    public static final int UPDATES_FRAGMENT = 2;
    public static final int OTHERS = 3;
    public static final String TO_EVENT_FRAGMENT = "event_fragment";
    public static final String TO_UPDATES_FRAGMENT = "updates_fragment";

    public static void createNotificationChannel(String id, String name, String description, Context context) {
        NotificationChannel notificationChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.BLACK);
        notificationChannel.setShowBadge(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        notificationChannel.setDescription(description);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    public static void createNotification(String channelId, Context context,
                                          String title, String message,
                                          String bigMessage, int notificationId, String groupId, int action) {
        Intent intent = new Intent(context, MainActivity.class);
        switch (action) {
            case EVENT_FRAGMENT:
                intent.putExtra("notification_fire", TO_EVENT_FRAGMENT);
                break;
            case UPDATES_FRAGMENT:
                intent.putExtra("notification_fire", TO_UPDATES_FRAGMENT);
                break;
            default:
                intent.putExtra("notification_fire", "just_start_activity");
                break;
        }
        Log.d("test", intent.getStringExtra("notification_fire") + "");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
                channelId).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(bigMessage))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setGroup(groupId)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(notificationId, builder.build());
    }

    public static void createNotificationGroup(String groupId, CharSequence groupName,
                                               Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannelGroup group = new NotificationChannelGroup(groupId, groupName);
        manager.createNotificationChannelGroup(group);
    }
}
