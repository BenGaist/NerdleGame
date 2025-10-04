package com.example.nerdlegame;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.util.concurrent.Executors;

public class GameActivity extends AppCompatActivity {

    private TextView tvGreeting, tvTimer;
    private Handler timerHandler = new Handler();
    private int secondsElapsed = 0;
    private boolean timerRunning = false;

    private GameManager gameManager;
    private StringBuilder currentGuess = new StringBuilder();
    private int currentRow = 0;

    private GridLayout gridBoard;
    private TextView[][] cells = new TextView[6][8]; // ✅ FIXED

    private String username;

    private int green, yellow, gray, darkGray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);



        // Load colors
        green = ContextCompat.getColor(this, R.color.nerdle_green);
        yellow = ContextCompat.getColor(this, R.color.nerdle_yellow);
        gray = ContextCompat.getColor(this, R.color.nerdle_gray);
        darkGray = ContextCompat.getColor(this, R.color.nerdle_dark_gray);

        // UI
        tvGreeting = findViewById(R.id.tvGreeting);
        tvTimer = findViewById(R.id.tvTimer);
        gridBoard = findViewById(R.id.gridBoard);

        // Username
        username = getIntent().getStringExtra("USERNAME");
        tvGreeting.setText("Hello, " + username + "! Let's play Nerdle!");

        // Start game
        gameManager = new GameManager();

        // Build board + keyboard
        setupBoard();
        setupKeyboard();

        // Timer
        startTimer();

        // Menu buttons
        Button btnMenu = findViewById(R.id.btnMenu);
        Button btnResults = findViewById(R.id.btnResults);

        btnMenu.setOnClickListener(v -> {
            Intent intent = new Intent(GameActivity.this, MainActivity.class);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
            finish();
        });

        btnResults.setOnClickListener(v -> {
            Intent intent = new Intent(GameActivity.this, ResultsActivity.class);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
        });


    }

    // ---------------- TIMER ----------------
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            secondsElapsed++;
            updateTimerText();
            timerHandler.postDelayed(this, 1000);
        }
    };

    private void startTimer() {
        secondsElapsed = 0;
        timerRunning = true;
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void stopTimer() {
        timerRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void updateTimerText() {
        int minutes = secondsElapsed / 60;
        int seconds = secondsElapsed % 60;
        String time = String.format("%d:%02d", minutes, seconds);
        tvTimer.setText(time);
    }

    // ---------------- BOARD ----------------
    private void setupBoard() {
        gridBoard.removeAllViews();
        int rows = 6, cols = 8;
        gridBoard.setRowCount(rows);
        gridBoard.setColumnCount(cols);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                TextView cell = new TextView(this);
                cell.setText("");
                cell.setGravity(Gravity.CENTER);
                cell.setTextSize(18);
                cell.setBackgroundResource(R.drawable.cell_background);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = 0;
                params.rowSpec = GridLayout.spec(r, 1, 1f);
                params.columnSpec = GridLayout.spec(c, 1, 1f);
                params.setMargins(4, 4, 4, 4);

                cell.setLayoutParams(params);

                gridBoard.addView(cell);
                cells[r][c] = cell;
            }
        }
    }

    // ---------------- KEYBOARD ----------------
    private void setupKeyboard() {
        GridLayout keyboard = findViewById(R.id.keyboard);

        for (int i = 0; i < keyboard.getChildCount(); i++) {
            View child = keyboard.getChildAt(i);
            if (child instanceof Button) {
                Button btn = (Button) child;
                btn.setOnClickListener(v -> onKeyPress(btn.getText().toString()));
            }
        }
    }

    private void onKeyPress(String key) {
        if (key.equals("Enter")) {
            handleEnter();
        } else if (key.equals("⌫")) {
            if (currentGuess.length() > 0) {
                currentGuess.deleteCharAt(currentGuess.length() - 1);
            }
        } else {
            if (currentGuess.length() < 8) {
                currentGuess.append(key);
            }
        }
        updateBoard();
    }

    private void handleEnter() {
        String guess = currentGuess.toString();

        if (guess.length() == gameManager.getSolution().length()) {
            if (gameManager.isValidEquation(guess)) {
                gameManager.addAttempt(guess);
                checkGuess(guess);
            } else {
                Toast.makeText(this, "Invalid equation!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ---------------- CHECK GUESS ----------------
    private void checkGuess(String guess) {
        String solution = gameManager.getSolution();

        for (int i = 0; i < guess.length(); i++) {
            char g = guess.charAt(i);

            if (g == solution.charAt(i)) {
                cells[currentRow][i].setBackgroundColor(green);
                updateKeyboardKey(g, green);

            } else if (solution.contains(String.valueOf(g))) {
                cells[currentRow][i].setBackgroundColor(yellow);
                updateKeyboardKey(g, yellow);

            } else {
                cells[currentRow][i].setBackgroundColor(gray);
                updateKeyboardKey(g, darkGray);
            }
        }

        if (guess.equals(solution)) {
            playWinAnimation(solution); // ✅ play animation first
            return;
        }

        if (currentRow == 5) {
            showResultPopup(false, solution);
            return;
        }

        currentRow++;
        currentGuess.setLength(0);
    }

    private void updateKeyboardKey(char key, int color) {
        GridLayout keyboard = findViewById(R.id.keyboard);

        for (int i = 0; i < keyboard.getChildCount(); i++) {
            View child = keyboard.getChildAt(i);
            if (child instanceof Button) {
                Button btn = (Button) child;
                if (btn.getText().toString().equals(String.valueOf(key))) {
                    btn.setBackgroundColor(color);
                    if (color == darkGray) {
                        btn.setTextColor(Color.BLACK);
                    }
                }
            }
        }
    }

    private void updateBoard() {
        // Clear row
        for (int c = 0; c < 8; c++) {
            cells[currentRow][c].setText("");
        }

        // Fill guess
        for (int i = 0; i < currentGuess.length(); i++) {
            cells[currentRow][i].setText(String.valueOf(currentGuess.charAt(i)));
        }
    }

    // ---------------- RESULT POPUP ----------------
    private void showResultPopup(boolean isWin, String equation) {
        stopTimer();

        LayoutInflater inflater = getLayoutInflater();
        View popupView = inflater.inflate(R.layout.popup_result, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(popupView);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        TextView title = popupView.findViewById(R.id.popupTitle);
        TextView eqText = popupView.findViewById(R.id.popupEquation);
        TextView popupTimer = popupView.findViewById(R.id.popupTimerText);

        if (isWin) {
            title.setText("You Win! 🎉");
            eqText.setText("Equation: " + equation);

            String timeText = tvTimer.getText().toString();
            long now = System.currentTimeMillis();

            String[] parts = timeText.split(":");
            int minutes = Integer.parseInt(parts[0]);
            int seconds = Integer.parseInt(parts[1]);
            int totalSeconds = minutes * 60 + seconds;

            Result result = new Result(username, equation, timeText, now, totalSeconds);
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase db = AppDatabase.getInstance(GameActivity.this);
                db.resultDao().insert(result);
            });
        } else {
            title.setText("Game Over");
            eqText.setText("Solution: " + equation);
        }

        popupTimer.setText(tvTimer.getText().toString());

        Button btnQuit = popupView.findViewById(R.id.btnQuit);
        Button btnPlayAgain = popupView.findViewById(R.id.btnPlayAgain);

        // Thread for auto-dismiss
        Thread autoDismissThread = new Thread(() -> {
            try {
                Thread.sleep(10_000); // wait 10 seconds
                if (dialog.isShowing()) {
                    runOnUiThread(() -> {
                        dialog.dismiss();
                        Intent intent = new Intent(this, ResultsActivity.class);
                        intent.putExtra("USERNAME", username);
                        startActivity(intent);
                    });
                }
            } catch (InterruptedException e) {
                // Thread was interrupted (user clicked button), do nothing
            }
        });
        autoDismissThread.start();

        btnQuit.setOnClickListener(v -> {
            autoDismissThread.interrupt(); // stop the thread
            dialog.dismiss();
            Intent intent = new Intent(this, ResultsActivity.class);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
        });

        btnPlayAgain.setOnClickListener(v -> {
            autoDismissThread.interrupt(); // stop the thread
            dialog.dismiss();
            recreate();
        });

        dialog.show();
    }


    private void playWinAnimation(String solution) {
        stopTimer(); // ✅ stop the timer immediately when animation starts

        int duration = 3000; // total animation time (3 sec)
        int interval = 300;  // each wiggle cycle
        long startTime = System.currentTimeMillis();

        Handler animHandler = new Handler();

        Runnable animator = new Runnable() {
            boolean right = true; // toggle direction

            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;

                // Stop after 3 sec
                if (elapsed >= duration) {
                    // reset all tiles to normal rotation
                    for (int r = 0; r < 6; r++) {
                        for (int c = 0; c < 8; c++) {
                            if (cells[r][c] != null) {
                                cells[r][c].setRotation(0f);
                            }
                        }
                    }
                    // ✅ finally show popup
                    showResultPopup(true, solution);
                    return;
                }

                // Animate all filled cells
                for (int r = 0; r < 6; r++) {
                    for (int c = 0; c < 8; c++) {
                        if (cells[r][c] != null && !cells[r][c].getText().toString().isEmpty()) {
                            cells[r][c].animate()
                                    .rotation(right ? 15f : -15f) // tilt right/left
                                    .setDuration(interval / 2)
                                    .start();
                        }
                    }
                }

                right = !right; // flip direction
                animHandler.postDelayed(this, interval);
            }
        };

        animHandler.post(animator);
    }

}
