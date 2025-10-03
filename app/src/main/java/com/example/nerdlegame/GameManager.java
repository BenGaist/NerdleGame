package com.example.nerdlegame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameManager {
    private String solution;
    private int maxAttempts = 6;
    private List<String> attempts;
    private Random random = new Random();

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

    // ✅ Random equation generator (always 8 chars long)
    private String generateEquation() {
        String equation = "";
        boolean valid = false;

        while (!valid) {
            int a = random.nextInt(90) + 10;  // 10–99 (2 digits)
            int b = random.nextInt(90) + 10;  // 10–99
            int opType = random.nextInt(4);   // 0:+, 1:-, 2:*, 3:/
            int result = 0;
            String op = "";

            switch (opType) {
                case 0: // addition
                    result = a + b;
                    op = "+";
                    break;
                case 1: // subtraction (no negatives)
                    if (a < b) { int tmp = a; a = b; b = tmp; }
                    result = a - b;
                    op = "-";
                    break;
                case 2: // multiplication
                    result = a * b;
                    op = "*";
                    break;
                case 3: // division (must divide evenly)
                    if (b == 0 || a % b != 0) continue;
                    result = a / b;
                    op = "/";
                    break;
            }

            equation = a + op + b + "=" + result;

            // ✅ Must be exactly 8 characters
            if (equation.length() == 8) {
                valid = true;
            }
        }

         return equation;
    }
    public boolean isValidEquation(String guess) {
        // Must be exactly 8 chars
        if (guess.length() != 8) return false;

        // Split into left and right
        String[] parts = guess.split("=");
        if (parts.length != 2) return false;

        try {
            int lhs = evaluate(parts[0]); // compute left side
            int rhs = Integer.parseInt(parts[1]); // right side

            return lhs == rhs; // ✅ equation must hold true
        } catch (Exception e) {
            return false; // if parsing/evaluation fails
        }
    }

    // Helper: evaluate simple expressions like "23*07"
    private int evaluate(String expr) {
        if (expr.contains("+")) {
            String[] nums = expr.split("\\+");
            return Integer.parseInt(nums[0]) + Integer.parseInt(nums[1]);
        } else if (expr.contains("-")) {
            String[] nums = expr.split("-");
            return Integer.parseInt(nums[0]) - Integer.parseInt(nums[1]);
        } else if (expr.contains("*")) {
            String[] nums = expr.split("\\*");
            return Integer.parseInt(nums[0]) * Integer.parseInt(nums[1]);
        } else if (expr.contains("/")) {
            String[] nums = expr.split("/");
            int a = Integer.parseInt(nums[0]);
            int b = Integer.parseInt(nums[1]);
            if (b == 0 || a % b != 0) throw new ArithmeticException("Invalid division");
            return a / b;
        }
        throw new IllegalArgumentException("No operator found");
    }

}
