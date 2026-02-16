package com.example.nerdlegame;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the core game logic, including equation generation (via helper),
 * maintaining the list of attempts, and validating guesses.
 */
public class GameManager {

    private static final String TAG = "GameManager";
    private final GeminiEquationGenerator equationGenerator;
    private String solution;
    private final int maxAttempts = 6;
    private final List<String> attempts = new ArrayList<>();

    //  Constructor
    public GameManager() {
        equationGenerator = new GeminiEquationGenerator();
    }

    /**
     * Resets the game state for a new game.
     * Clears attempts and solution.
     */
    public void reset() {
        attempts.clear();
        solution = null;
    }

    /**
     * Initiates the generation of a new equation using Gemini.
     * @param callback Callback to handle the result (success or failure).
     */
    public void generateEquationGemini(final GeminiEquationGenerator.EquationCallback callback) {
        equationGenerator.generateEquation((equation, message, isSuccess) -> {
            if (isSuccess) {
                solution = equation;
            }
            callback.onEquationGenerated(equation, message, isSuccess);
        });
    }

    //  Game logic
    public String getSolution() {
        return solution;
    }
    public void setSolution(String equation) {
        this.solution = equation;
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

    //  Validate equation correctness
    //  Flexible equation validator using expression parsing
    public boolean isValidEquation(String equation) {
        return equationGenerator.isValidEquation(equation);
    }

    public GuessResult checkGuess(String guess) {
        if (solution == null) {
            throw new IllegalStateException("Solution not set");
        }

        int[] solutionCharCounts = new int[256];
        int[] guessColors = new int[8]; // 0=Gray, 1=Green, 2=Yellow

        // Count solution chars
        for (char c : solution.toCharArray()) {
            solutionCharCounts[c]++;
        }

        // Pass 1: Green
        for (int i = 0; i < 8; i++) {
            char g = guess.charAt(i);
            char s = solution.charAt(i);

            if (g == s) {
                guessColors[i] = 1; // Green
                solutionCharCounts[g]--;
            }
        }

        // Pass 2: Yellow
        for (int i = 0; i < 8; i++) {
            if (guessColors[i] == 1) continue;

            char g = guess.charAt(i);
            if (solutionCharCounts[g] > 0) {
                guessColors[i] = 2; // Yellow
                solutionCharCounts[g]--;
            } else {
                guessColors[i] = 0; // Gray
            }
        }

        boolean isWin = guess.equals(solution);
        return new GuessResult(guessColors, isWin);
    }
}


