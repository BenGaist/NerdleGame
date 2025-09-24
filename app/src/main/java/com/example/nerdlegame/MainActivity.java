package com.example.nerdlegame;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    EditText etUsername;
    Button btnStart;

    SharedPreferences sharedPreferences;
    public static final String PREFS_NAME = "NerdlePrefs";
    public static final String KEY_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUsername = findViewById(R.id.etUsername);
        btnStart = findViewById(R.id.btnStart);

        // Load SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // If username is already saved, load it into EditText
        String savedUsername = sharedPreferences.getString(KEY_USERNAME, "");
        etUsername.setText(savedUsername);

        btnStart.setOnClickListener(v -> {
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
    }
}
