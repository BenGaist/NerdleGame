package com.example.nerdlegame;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResultsActivity extends AppCompatActivity {

    String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        username = getIntent().getStringExtra("USERNAME");

        RecyclerView recyclerView = findViewById(R.id.recyclerResults);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        AppDatabase db = AppDatabase.getInstance(this);
        List<Result> results = db.resultDao().getAllResults();

        System.out.println(" Results found: " + results.size());

        ResultAdapter adapter = new ResultAdapter(results);
        recyclerView.setAdapter(adapter);

        Button btnMenu = findViewById(R.id.btnMenu);
        Button btnGame = findViewById(R.id.btnGame);

        btnMenu.setOnClickListener(v -> {
            Intent intent = new Intent(ResultsActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // optional: close ResultsActivity
        });

        btnGame.setOnClickListener(v -> {
            Intent intent = new Intent(ResultsActivity.this, GameActivity.class);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
            finish(); // optional
        });
    }

}
