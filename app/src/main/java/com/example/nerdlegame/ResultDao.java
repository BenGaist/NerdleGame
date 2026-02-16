package com.example.nerdlegame;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object (DAO) for accessing {@link Result} data in the database.
 */
@Dao
public interface ResultDao {
    /**
     * Inserts a new game result into the database.
     * @param result The result object to insert.
     */
    @Insert
    void insert(Result result);

    /**
     * Retrieves all game results, ordered by duration (fastest times first).
     * @return A list of all Result objects.
     */
    @Query("SELECT * FROM results ORDER BY durationSeconds ASC")
    List<Result> getAllResults();
}
