package com.example.nerdlegame;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences("NerdlePrefs", Context.MODE_PRIVATE);
        long lastPlayed = prefs.getLong("last_played", 0);

        long now = System.currentTimeMillis();

        // Only show notification if user has played before AND it's been 24+ hours
        if (lastPlayed > 0 && (now - lastPlayed >= 24 * 60 * 60 * 1000)) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "nerdleDaily")
                    .setSmallIcon(R.drawable.ic_launcher_foreground) // replace with your custom icon if possible
                    .setContentTitle("Nerdle Daily")
                    .setContentText("The Nerdle of the day is waiting!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}
