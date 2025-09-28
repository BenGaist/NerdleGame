package com.example.nerdlegame;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;


@Dao
public interface ResultDao {
    @Insert
    void insert(Result result);

    @Query("SELECT * FROM results ORDER BY duration ASC") // ✅ numeric sort
    List<Result> getAllResults();
}


