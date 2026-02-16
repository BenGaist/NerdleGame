package com.example.nerdlegame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Activity for displaying the leaderboard/history of game results.
 * Retrieves data from the local database and displays it in a RecyclerView.
 */
public class ResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        RecyclerView rvResults = findViewById(R.id.rvResults);
        rvResults.setLayoutManager(new LinearLayoutManager(this));

        // Load results from DB on main thread (simple approach for this project)
        AppDatabase db = AppDatabase.getInstance(this);
        List<Result> results = db.resultDao().getAllResults();

        ResultAdapter adapter = new ResultAdapter(results);
        rvResults.setAdapter(adapter);

        Button btnMenu = findViewById(R.id.btnMenu);
        Button btnPlayAgain = findViewById(R.id.btnPlayAgain);

        // Get username if passed (to keep it for next game)
        String username = getIntent().getStringExtra("USERNAME");

        btnMenu.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            // Clear back stack so we don't pile up activities
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnPlayAgain.setOnClickListener(v -> {
            Intent intent = new Intent(this, GameActivity.class);
            if (username != null) {
                intent.putExtra("USERNAME", username);
            }
            startActivity(intent);
            finish();
        });
    }
}
