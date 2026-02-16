package com.example.nerdlegame;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.EditText;

/**
 * Main entry point for the NerdleGame application.
 * Handles user login, settings, game start, and background music management.
 */
public class MainActivity extends AppCompatActivity {

    private EditText etUsername;
    private Button btnStart, btnSettings, btnRules;

    private SharedPreferences sharedPreferences;
    public static final String PREFS_NAME = "NerdlePrefs";
    public static final String KEY_USERNAME = "username";
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

    // Music Service Binding
    private MusicService musicService;
    private boolean isBound = false;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            musicService = binder.getService();
            isBound = true;

            // Check preferences and start music if enabled
            if (sharedPreferences != null && sharedPreferences.getBoolean("music_on", true)) {
                musicService.resumeMusic();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

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

        // Bind to MusicService (Auto-create but don't auto-start playback)
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);


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
            Intent gameIntent = new Intent(MainActivity.this, GameActivity.class);
            gameIntent.putExtra("USERNAME", username);
            startActivity(gameIntent);
        });

        btnSettings.setOnClickListener(v -> {
            showSettingsDialog();
        });

        btnRules.setOnClickListener(v -> {
            // Pause background music before showing rules
            if (isBound && musicService != null) {
                musicService.pauseMusic();
            }

            RulesDialogFragment dialog = new RulesDialogFragment();
            dialog.show(getSupportFragmentManager(), "RulesDialog");
        });
    }

    /**
     * Called by RulesDialogFragment when it is dismissed.
     * Resumes the background music if it's enabled in settings.
     */
    public void onRulesDismissed() {
        boolean isMusicOn = sharedPreferences.getBoolean("music_on", true);
        if (isMusicOn && isBound && musicService != null) {
            musicService.resumeMusic();
        }
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
                if (isBound && musicService != null) {
                    musicService.resumeMusic();
                }
            } else {
                // Music OFF
                editor.putBoolean("music_on", false);
                editor.apply();
                if (isBound && musicService != null) {
                    musicService.pauseMusic();
                }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }
}
