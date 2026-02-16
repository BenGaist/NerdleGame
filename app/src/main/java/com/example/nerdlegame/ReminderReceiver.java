package com.example.nerdlegame;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;

/**
 * BroadcastReceiver responsible for sending daily reminders to play the game.
 * Checks if the user has played recently before sending a notification.
 */
public class ReminderReceiver extends BroadcastReceiver {

    /**
     * Called when the BroadcastReceiver is receiving an Intent broadcast.
     * This method checks the last played time and sends a daily reminder notification
     * if the user hasn't played for more than 24 hours.
     *
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // Daily Reminder Logic
        SharedPreferences prefs = context.getSharedPreferences("NerdlePrefs", Context.MODE_PRIVATE);
        long lastPlayed = prefs.getLong("last_played", 0);

        // If played more than 24 hours ago, send notification
        if (System.currentTimeMillis() - lastPlayed > 24 * 60 * 60 * 1000) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "nerdleDaily")
                    .setSmallIcon(R.drawable.ic_launcher_foreground) // Make sure this is a valid notification icon (white/transparent)
                    .setContentTitle("Daily Nerdle Reminder")
                    .setContentText("Have you solved today's equation yet?")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.notify(200, builder.build());
            }
        }
    }
}
