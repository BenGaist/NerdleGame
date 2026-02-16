package com.example.nerdlegame;

/**
 * Data class encapsulating the result of a single guess.
 * Contains the color feedback array and a boolean indicating if the guess was a win.
 */
public class GuessResult {
    private final int[] colors;
    private final boolean isWin;

    /**
     * Constructor for GuessResult.
     * @param colors Array of color integers representing feedback for each character.
     * @param isWin True if the guess is exactly correct, false otherwise.
     */
    public GuessResult(int[] colors, boolean isWin) {
        this.colors = colors;
        this.isWin = isWin;
    }

    public int[] getColors() {
        return colors;
    }

    public boolean isWin() {
        return isWin;
    }
}
