package com.example.nerdlegame;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity class representing a game result record in the database.
 * Stores details about a completed game, including the user, equation, and time taken.
 */
@Entity(tableName = "results")
public class Result {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String username;
    public String equation;
    public String timeFormatted; // e.g., "02:15"
    public long timestamp;       // When the game finished
    public int durationSeconds;  // For sorting best times

    /**
     * Constructor for creating a new result entry.
     * @param username The player's username.
     * @param equation The equation that was solved.
     * @param timeFormatted The formatted string representation of the time taken.
     * @param timestamp The exact timestamp when the game was completed.
     * @param durationSeconds The total duration in seconds (useful for sorting).
     */
    public Result(String username, String equation, String timeFormatted, long timestamp, int durationSeconds) {
        this.username = username;
        this.equation = equation;
        this.timeFormatted = timeFormatted;
        this.timestamp = timestamp;
        this.durationSeconds = durationSeconds;
    }
}
