package com.example.nerdlegame;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        RecyclerView recyclerView = findViewById(R.id.recyclerResults);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        AppDatabase db = AppDatabase.getInstance(this);
        List<Result> results = db.resultDao().getAllResults();

        System.out.println("✅ Results found: " + results.size());

        ResultAdapter adapter = new ResultAdapter(results);
        recyclerView.setAdapter(adapter);
    }

}
