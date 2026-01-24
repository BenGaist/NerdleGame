package com.example.nerdlegame;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "results")
public class Result {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String username;
    public String equation;
    public String time;   // formatted "m:ss"
    public long date;     // timestamp
    public int duration;  // total seconds  NEW

    public Result(String username, String equation, String time, long date, int duration) {
        this.username = username;
        this.equation = equation;
        this.time = time;
        this.date = date;
        this.duration = duration;
    }
}


