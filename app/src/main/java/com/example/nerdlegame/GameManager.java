package com.example.nerdlegame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameManager {
    private String solution;
    private int maxAttempts = 6;
    private List<String> attempts;

    public GameManager() {
        attempts = new ArrayList<>();
        solution = generateEquation();
    }

    public String getSolution() {
        return solution;
    }

    public List<String> getAttempts() {
        return attempts;
    }

    public boolean canGuess() {
        return attempts.size() < maxAttempts;
    }

    public void addAttempt(String guess) {
        if (canGuess()) {
            attempts.add(guess);
        }
    }

    // ✅ Simple random equation generator
    private String generateEquation() {
        Random r = new Random();
        int a = r.nextInt(10) + 1; // 1–10
        int b = r.nextInt(10) + 1;
        int sum = a + b;
        return a + "+" + b + "=" + sum;
    }
}
