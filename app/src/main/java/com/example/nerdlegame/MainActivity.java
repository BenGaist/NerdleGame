package com.example.nerdlegame;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    EditText etUsername;
    Button btnStart, btnSettings, btnRules;

    SharedPreferences sharedPreferences;
    public static final String PREFS_NAME = "NerdlePrefs";
    public static final String KEY_USERNAME = "username";
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            }
        }
        scheduleDailyReminder();
        createNotificationChannel();


        etUsername = findViewById(R.id.etUsername);
        btnStart = findViewById(R.id.btnStart);
        btnSettings = findViewById(R.id.btnSettings);
        btnRules = findViewById(R.id.btnRules);


        // Load SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Auto-load username
        String savedUsername = sharedPreferences.getString(KEY_USERNAME, "");
        etUsername.setText(savedUsername);

        // Auto-start music only if ON
        boolean isMusicOn = sharedPreferences.getBoolean("music_on", true);
        if (isMusicOn) {
            startService(new Intent(this, MusicService.class));
        }

        btnStart.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("NerdlePrefs", MODE_PRIVATE);
            prefs.edit().putLong("last_played", System.currentTimeMillis()).apply();

            String username = etUsername.getText().toString().trim();

            if (username.isEmpty()) {
                username = "Player";
            }

            // Save username in SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_USERNAME, username);
            editor.apply();

            // Send to GameActivity
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> {
            showSettingsDialog();
        });

        btnRules.setOnClickListener(v -> {
            stopService(new Intent(this, MusicService.class));

            RulesDialogFragment dialog = new RulesDialogFragment();
            dialog.show(getSupportFragmentManager(), "RulesDialog");
        });


    }

    private void showSettingsDialog() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        boolean isMusicOn = sharedPreferences.getBoolean("music_on", true);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Settings");

        String[] options = {"Music On", "Music Off"};
        int checkedItem = isMusicOn ? 0 : 1;

        builder.setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
            if (which == 0) {
                // Music ON
                editor.putBoolean("music_on", true);
                editor.apply();
                startService(new Intent(this, MusicService.class));
            } else {
                // Music OFF
                editor.putBoolean("music_on", false);
                editor.apply();
                stopService(new Intent(this, MusicService.class));
            }
        });

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    //notification
    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "NerdleDailyChannel";
            String description = "Channel for daily Nerdle notifications";
            int importance = android.app.NotificationManager.IMPORTANCE_DEFAULT;

            android.app.NotificationChannel channel =
                    new android.app.NotificationChannel("nerdleDaily", name, importance);
            channel.setDescription(description);

            android.app.NotificationManager notificationManager =
                    getSystemService(android.app.NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void scheduleDailyReminder() {
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // Trigger once every 24 hours
        long interval = AlarmManager.INTERVAL_DAY;

        // Start first reminder 24 hours from now
        long triggerTime = System.currentTimeMillis() + interval;

        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                interval,
                pendingIntent
        );
    }


    private void showRulesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("How to Play Nerdle");

        // Put the rules text into a scrollable TextView
        final android.widget.ScrollView scrollView = new android.widget.ScrollView(this);
        final android.widget.TextView textView = new android.widget.TextView(this);

        textView.setText(getString(R.string.rules_text));
        textView.setPadding(40, 30, 40, 30);
        textView.setTextSize(16f);

        scrollView.addView(textView);
        builder.setView(scrollView);

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }









    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, MusicService.class));
    }
}
