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

    private TextView tvGreeting, tvTimer, tvStart, tvTopToast;
    private View loadingContainer;
    private Runnable hideToastRunnable;
    private Handler timerHandler = new Handler();
    private int secondsElapsed = 0;
    private boolean timerRunning = false;


    private StringBuilder currentGuess = new StringBuilder();
    private int currentRow = 0;

    private GridLayout gridBoard;
    private TextView[][] cells = new TextView[6][8]; // ✅ FIXED

    private String username;

    private int green, yellow, gray, darkGray;
    private GameManager gameManager; // ✅ add this line
    private boolean isEquationReady = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // --- Load colors ---
        green = ContextCompat.getColor(this, R.color.nerdle_green);
        yellow = ContextCompat.getColor(this, R.color.nerdle_yellow);
        gray = ContextCompat.getColor(this, R.color.nerdle_gray);
        darkGray = ContextCompat.getColor(this, R.color.nerdle_dark_gray);

        // --- Find views ---
        tvGreeting = findViewById(R.id.tvGreeting);
        tvTimer = findViewById(R.id.tvTimer);
        gridBoard = findViewById(R.id.gridBoard);
        loadingContainer = findViewById(R.id.loadingContainer);
        tvStart = findViewById(R.id.tvStart);
        tvTopToast = findViewById(R.id.tvTopToast);

        // Start state
        loadingContainer.setVisibility(View.VISIBLE);
        tvStart.setVisibility(View.GONE);

        // --- Get username ---
        username = getIntent().getStringExtra("USERNAME");
        tvGreeting.setText("Hello, " + username + "! Let's play Nerdle!");

        // --- Create GameManager ---
        gameManager = new GameManager();

        // --- Setup board first (but no keyboard yet) ---
        setupBoard();

        // --- Disable keyboard until equation is ready ---
        // --- Disable keyboard until equation is ready ---
        setKeyboardEnabled(false);
        showTopToast("Generating puzzle...");

        // --- Generate equation using Gemini ---
        // --- Generate equation using Gemini ---
        gameManager.generateEquationGemini(new GameManager.EquationCallback() {
            @Override
            public void onEquationGenerated(String equation, String message, boolean isSuccess) {
                runOnUiThread(() -> {
                    isEquationReady = true;
                    
                    // Show detailed status to user
                    showTopToast(message);

                    // Hide loader, show Start
                    loadingContainer.setVisibility(View.GONE);
                    tvStart.setVisibility(View.VISIBLE);

                    // Hide Start after 5 seconds
                    new Handler().postDelayed(() -> {
                        tvStart.setVisibility(View.GONE);
                    }, 5000);

                    // Now we can safely start the game
                    setupKeyboard();
                    setKeyboardEnabled(true);
                    startTimer();
                });
            }
        });

        // --- Menu buttons ---
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
        if (!isEquationReady) {
            showTopToast("Please wait, generating puzzle...");
            return;
        }

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
        if (gameManager.getSolution() == null) {
            showTopToast("Please wait, generating puzzle...");
            return;
        }

        String guess = currentGuess.toString();

        if (guess.length() == 8) {
            if (gameManager.isValidEquation(guess)) {
                gameManager.addAttempt(guess);
                checkGuess(guess);
            } else {
                showTopToast("Invalid equation!");
            }
        }
    }


    // ---------------- CHECK GUESS ----------------
    private void checkGuess(String guess) {
        String solution = gameManager.getSolution();
        int[] solutionCharCounts = new int[256]; // Frequency map for solution chars
        int[] guessColors = new int[8]; // Store colors locally first: 0=Gray, 1=Green, 2=Yellow

        // Initialize colors to Gray.
        // 0 -> Gray
        // 1 -> Green
        // 2 -> Yellow

        // Pass 1: Identify Greens and count available solution characters
        // First, count all chars in solution
        for (char c : solution.toCharArray()) {
            solutionCharCounts[c]++;
        }

        // Now find Greens and decrement counts from solution for those matches
        for (int i = 0; i < 8; i++) {
            char g = guess.charAt(i);
            char s = solution.charAt(i);

            if (g == s) {
                guessColors[i] = 1; // Green
                solutionCharCounts[g]--;
            }
        }

        // Pass 2: Identify Yellows
        for (int i = 0; i < 8; i++) {
            if (guessColors[i] == 1) continue; // Skip already green

            char g = guess.charAt(i);
            if (solutionCharCounts[g] > 0) {
                guessColors[i] = 2; // Yellow
                solutionCharCounts[g]--;
            } else {
                guessColors[i] = 0; // Gray
            }
        }

        // Apply colors to UI
        for (int i = 0; i < 8; i++) {
            char g = guess.charAt(i);
            int tileColor;
            int keyColor;

            if (guessColors[i] == 1) {
                tileColor = green;
                keyColor = green;
            } else if (guessColors[i] == 2) {
                tileColor = yellow;
                keyColor = yellow;
            } else {
                tileColor = gray;
                keyColor = darkGray;
            }

            cells[currentRow][i].setBackgroundColor(tileColor);
            updateKeyboardKey(g, keyColor);
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
        // Determine priority of incoming color
        // 3: Green
        // 2: Yellow
        // 1: DarkGray/Gray (Miss)
        // 0: None
        int priority = 0;
        if (color == green) priority = 3;
        else if (color == yellow) priority = 2;
        else if (color == darkGray) priority = 1;

        GridLayout keyboard = findViewById(R.id.keyboard);

        for (int i = 0; i < keyboard.getChildCount(); i++) {
            View child = keyboard.getChildAt(i);
            if (child instanceof Button) {
                Button btn = (Button) child;
                if (btn.getText().toString().equals(String.valueOf(key))) {
                    
                    int currentPriority = 0;
                    if (btn.getTag() != null && btn.getTag() instanceof Integer) {
                        currentPriority = (Integer) btn.getTag();
                    }

                    // Only update if new priority is higher (e.g. don't overwrite Green with Yellow)
                    if (priority > currentPriority) {
                        btn.setBackgroundColor(color);
                        btn.setTag(priority);
                        
                        if (color == darkGray) {
                            btn.setTextColor(Color.BLACK);
                        } else {
                             // Reset text color to default (usually white) for Green/Yellow if needed, 
                             // but since we only upgrade, we might not need to revert from Black.
                             // However, if we went from Gray -> Yellow (logic forbids, but say restart), 
                             // we should be careful. But here we just persist forward.
                             // Let's assume default text color handles fine for G/Y.
                             // Actually, if a key was Gray (Black text) and becomes Yellow (Upgrade impossible in standard Nerdle but let's be safe),
                             // we should probably reset text color. 
                             // But wait, if logic is correct, a char can't go from Gray(NotInWord) to Yellow(InWord).
                             // So Gray is terminal unless we restart game. 
                             // Restarting game recreates Activity or resets board? 
                             // `recreate()` is called in `showResultPopup`. This reloads everything so state is cleared.
                        }
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

    private void setKeyboardEnabled(boolean enabled) {
        GridLayout keyboard = findViewById(R.id.keyboard);
        for (int i = 0; i < keyboard.getChildCount(); i++) {
            View child = keyboard.getChildAt(i);
            if (child instanceof Button) {
                child.setEnabled(enabled);
            }
        }
    }

    private void showTopToast(String message) {
        if (tvTopToast == null) return;
        
        tvTopToast.setText(message);
        tvTopToast.setVisibility(View.VISIBLE);
        
        // Remove any pending hide calls
        if (hideToastRunnable != null) {
            tvTopToast.removeCallbacks(hideToastRunnable);
        }
        
        hideToastRunnable = () -> tvTopToast.setVisibility(View.GONE);
        tvTopToast.postDelayed(hideToastRunnable, 3500); // Show for 3.5 seconds
    }
}
