package com.example.nerdlegame;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

import java.util.ArrayList;
import java.util.List;

public class GameManager {

    private static final String TAG = "GameManager";
    private final Client client;
    private String solution;
    private final int maxAttempts = 6;
    private final List<String> attempts = new ArrayList<>();
    private boolean isEquationReady = false;


    // ✅ Callback interface for async Gemini result
    public interface EquationCallback {
        void onEquationGenerated(String equation);

        void onError(Exception e);
    }

    // ✅ Constructor
    public GameManager() {
        String apiKey = BuildConfig.GOOGLE_API_KEY;

        if (apiKey == null || apiKey.isEmpty()) {
            Log.w(TAG, "GOOGLE_API_KEY is empty – did you set it in local.properties?");
        } else {
            Log.d(TAG, "Loaded GOOGLE_API_KEY (length=" + apiKey.length() + ")");
        }

        client = Client.builder().apiKey(apiKey).build();
    }

    // ✅ Generate equation with Gemini and store it in 'solution'
    public void generateEquationGemini(final EquationCallback callback) {
        final String model = "gemini-1.5-flash"; // Fixed model name
        final String nerdlePrompt =
                "Generate ONE random valid math equation exactly 8 characters long, " +
                        "like in the game Nerdle. " +
                        "Use only digits (0–9) and the symbols + - * / =. " +
                        "The equation must be mathematically correct and balanced. " +
                        "Example format: 91-34=67 or 56/8+1=8 or 12+34=46. " +
                        "Return only the equation, with no extra words.";

        new Thread(new Runnable() {
            @Override
            public void run() {
                long t0 = System.nanoTime();
                try {
                    GenerateContentResponse response = client.models.generateContent(
                            model,
                            nerdlePrompt,
                            null
                    );

                    String text = response.text().trim();
                    long durMs = (System.nanoTime() - t0) / 1_000_000;
                    Log.d(TAG, "Gemini equation: " + text + " (" + durMs + " ms)");

                    // Validate generated equation
                    if (!isValidSolution(text)) {
                        Log.w(TAG, "Gemini generated invalid equation: " + text + ". Using fallback.");
                        text = getRandomFallback();
                    }

                    final String finalSolution = text;
                    solution = finalSolution;

                    // Return to main thread
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onEquationGenerated(finalSolution);
                        }
                    });

                } catch (final Exception e) {
                    long durMs = (System.nanoTime() - t0) / 1_000_000;
                    Log.e(TAG, "Request failed after " + durMs + " ms: " + e.getMessage(), e);

                    // Use fallback on error
                    final String fallback = getRandomFallback();
                    solution = fallback;

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            // We can still call onEquationGenerated with a fallback instead of error
                            // to keep the game going seamlessly, or pass error if UI wants to know.
                            // Given user request "use a fallback equation", we provide a valid equation.
                            callback.onEquationGenerated(fallback);
                        }
                    });
                }
            }
        }).start();
    }

    private boolean isValidSolution(String eq) {
        if (eq == null || eq.length() != 8) return false;
        // Check regex for allowed chars
        if (!eq.matches("[0-9+\\-*/=]+")) return false;
        return isValidEquation(eq);
    }

    private String getRandomFallback() {
        String[] fallbacks = {
                "10+20=30",
                "12+34=46",
                "99-55=44",
                "50+40=90",
        };
        int idx = (int) (Math.random() * fallbacks.length);
        return fallbacks[idx];
    }

    // ✅ Game logic
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

    // ✅ Validate equation correctness
    // ✅ Flexible equation validator using expression parsing
    public boolean isValidEquation(String equation) {
        if (equation == null || !equation.contains("=")) return false;

        String[] parts = equation.split("=");
        if (parts.length != 2) return false;

        try {
            double leftValue = evaluateExpression(parts[0]);
            double rightValue = evaluateExpression(parts[1]);

            // Use small tolerance for floating-point division cases
            return Math.abs(leftValue - rightValue) < 1e-9;
        } catch (Exception e) {
            Log.e(TAG, "Invalid equation: " + equation + " (" + e.getMessage() + ")");
            return false;
        }
    }


    // ✅ Evaluate the left side of an equation
    // ✅ Evaluate a full mathematical expression safely
    private double evaluateExpression(String expr) throws Exception {
        expr = expr.replaceAll("\\s+", ""); // remove spaces
        if (expr.isEmpty()) throw new Exception("Empty expression");

        // Convert infix (e.g. "2+3*4") to postfix using Shunting Yard algorithm
        List<String> postfix = infixToPostfix(expr);
        return evaluatePostfix(postfix);
    }

    // --- Convert infix to postfix (Shunting Yard algorithm)
    private List<String> infixToPostfix(String expr) throws Exception {
        List<String> output = new ArrayList<>();
        java.util.Stack<Character> ops = new java.util.Stack<>();
        StringBuilder number = new StringBuilder();

        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);

            if (Character.isDigit(c)) {
                number.append(c);
            } else if ("+-*/".indexOf(c) >= 0) {
                if (number.length() > 0) {
                    output.add(number.toString());
                    number.setLength(0);
                }

                while (!ops.isEmpty() && precedence(ops.peek()) >= precedence(c)) {
                    output.add(String.valueOf(ops.pop()));
                }
                ops.push(c);
            } else {
                throw new Exception("Invalid char: " + c);
            }
        }

        if (number.length() > 0) {
            output.add(number.toString());
        }
        while (!ops.isEmpty()) {
            output.add(String.valueOf(ops.pop()));
        }

        return output;
    }

    // --- Evaluate postfix expression
    private double evaluatePostfix(List<String> postfix) throws Exception {
        java.util.Stack<Double> stack = new java.util.Stack<>();

        for (String token : postfix) {
            if (token.matches("\\d+")) {
                stack.push(Double.parseDouble(token));
            } else if (token.length() == 1 && "+-*/".contains(token)) {
                if (stack.size() < 2) throw new Exception("Bad expression");
                double b = stack.pop();
                double a = stack.pop();
                switch (token.charAt(0)) {
                    case '+':
                        stack.push(a + b);
                        break;
                    case '-':
                        stack.push(a - b);
                        break;
                    case '*':
                        stack.push(a * b);
                        break;
                    case '/':
                        if (b == 0) throw new Exception("Division by zero");
                        stack.push(a / b);
                        break;
                }
            } else {
                throw new Exception("Invalid token: " + token);
            }
        }

        if (stack.size() != 1) throw new Exception("Malformed expression");
        return stack.pop();
    }

    // --- Operator precedence
    private int precedence(char op) {
        if (op == '+' || op == '-') return 1;
        if (op == '*' || op == '/') return 2;
        return 0;
    }
}

