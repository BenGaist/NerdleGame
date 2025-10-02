package com.example.nerdlegame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import android.os.Handler;

public class GameActivity extends AppCompatActivity {

    private TextView tvGreeting, tvTimer;
    private Handler timerHandler = new Handler();
    private int secondsElapsed = 0;
    private boolean timerRunning = false;
    private GameManager gameManager;
    private StringBuilder currentGuess = new StringBuilder();
    private int currentRow = 0; // which attempt row the user is on
    private GridLayout gridBoard;
    private String username;
    // ✅ cells must be class-level
    private TextView[][] cells = new TextView[6][8];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);


        tvGreeting = findViewById(R.id.tvGreeting);
        gridBoard = findViewById(R.id.gridBoard);

        username = getIntent().getStringExtra("USERNAME");
        tvGreeting.setText("Hello, " + username + "! Let's play Nerdle!");

        // ✅ Start game
        gameManager = new GameManager();

        // ✅ Build dynamic board
        // ✅ Start game
        gameManager = new GameManager();
        setupKeyboard();
        setupBoard();   // <-- this was missing


        // ✅ Setup keyboard
        setupKeyboard();

        tvTimer = findViewById(R.id.tvTimer);
        startTimer();

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


    // ✅ Board setup method
    private void setupBoard() {
        gridBoard.removeAllViews(); // clear just in case
        int rows = 6, cols = 8;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                TextView cell = new TextView(this);
                cell.setText("");
                cell.setGravity(Gravity.CENTER);
                cell.setTextSize(18);
                cell.setBackgroundResource(R.drawable.cell_background);

                // Make it expand equally
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
            if (currentGuess.length() < 8) { // ✅ prevent typing more than 8 chars
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




    private void checkGuess(String guess) {
        String solution = gameManager.getSolution();

        for (int i = 0; i < guess.length(); i++) {
            char g = guess.charAt(i);

            if (g == solution.charAt(i)) {
                cells[currentRow][i].setBackgroundColor(Color.GREEN);
            } else if (solution.contains(String.valueOf(g))) {
                cells[currentRow][i].setBackgroundColor(Color.YELLOW);
            } else {
                cells[currentRow][i].setBackgroundColor(Color.GRAY);
            }
        }

        // ✅ Check win first
        if (guess.equals(solution)) {
            showResultPopup(true, solution); // WIN
            return;
        }

        // ✅ If last row (row 5), it's a loss
        if (currentRow == 5) {
            showResultPopup(false, solution); // LOSE
            return;
        }

        // Otherwise, go to next row
        currentRow++;
        currentGuess.setLength(0);
    }




    private void updateBoard() {
        // Clear the current row first
        for (int c = 0; c < 8; c++) {
            cells[currentRow][c].setText("");
        }

        // Fill with current guess
        for (int i = 0; i < currentGuess.length(); i++) {
            cells[currentRow][i].setText(String.valueOf(currentGuess.charAt(i)));
        }
    }
    private void showResultPopup(boolean isWin, String equation) {
        stopTimer(); // ✅ stop counting when game ends

        LayoutInflater inflater = getLayoutInflater();
        View popupView = inflater.inflate(R.layout.popup_result, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(popupView);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        // UI references
        TextView title = popupView.findViewById(R.id.popupTitle);
        TextView eqText = popupView.findViewById(R.id.popupEquation);
        TextView popupTimer = popupView.findViewById(R.id.popupTimerText);

        // ✅ Set values depending on win/lose
        if (isWin) {
            title.setText("You Win! 🎉");
            eqText.setText("Equation: " + equation);

            String username = getIntent().getStringExtra("USERNAME");
            String equationText = equation;
            String timeText = tvTimer.getText().toString();
            long now = System.currentTimeMillis();

            // calculate totalSeconds if you added that field
            String[] parts = timeText.split(":");
            int minutes = Integer.parseInt(parts[0]);
            int seconds = Integer.parseInt(parts[1]);
            int totalSeconds = minutes * 60 + seconds;

            Result result = new Result(username, equationText, timeText, now, totalSeconds);
            AppDatabase db = AppDatabase.getInstance(this);
            db.resultDao().insert(result);
        }
        else {
            title.setText("Game Over");
            eqText.setText("Solution: " + equation);
        }


        // ✅ Always show the timer
        popupTimer.setText(tvTimer.getText().toString());

        // ✅ Button handling
        Button btnQuit = popupView.findViewById(R.id.btnQuit);
        Button btnPlayAgain = popupView.findViewById(R.id.btnPlayAgain);
        Button btnResults = popupView.findViewById(R.id.btnResults);

        btnQuit.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        btnPlayAgain.setOnClickListener(v -> {
            dialog.dismiss();
            recreate(); // restart activity
        });

        btnResults.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(this, ResultsActivity.class);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
        });


        dialog.show();
    }

}
