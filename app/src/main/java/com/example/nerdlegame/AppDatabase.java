package com.example.nerdlegame;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * The Room Database for the application.
 * Manages the {@link Result} table and provides access to the {@link ResultDao}.
 * Uses a singleton pattern to ensure only one instance exists.
 */
@Database(entities = {Result.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ResultDao resultDao();

    private static AppDatabase instance;

    /**
     * Returns the singleton instance of the AppDatabase.
     * @param context The application context.
     * @return The AppDatabase instance.
     */
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "nerdle_database")
                    .fallbackToDestructiveMigration()
                    // Allowing main thread queries for simplicity in this project.
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}
