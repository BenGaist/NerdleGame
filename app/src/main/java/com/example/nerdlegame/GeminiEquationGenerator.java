package com.example.nerdlegame;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

/**
 * Helper class to generate equations using the Google Gemini API.
 * Handles the API call, response parsing, and validation.
 */
public class GeminiEquationGenerator {

    private static final String TAG = "GeminiGenerator";
    private static final String API_KEY = BuildConfig.GOOGLE_API_KEY; // Ensure this is set in local.properties

    /**
     * Callback interface for equation generation results.
     */
    public interface EquationCallback {
        void onEquationGenerated(String equation, String message, boolean isSuccess);
    }

    /**
     * Generates a random equation using Gemini.
     * Starts with 'gemini-2.5-flash' and falls back to 'gemini-1.5-flash' on failure.
     * @param callback The callback to receive the result.
     */
    public void generateEquation(final EquationCallback callback) {
        generateEquationWithModel("gemini-2.5-flash", callback, true);
    }

    private void generateEquationWithModel(String model, final EquationCallback callback, boolean retryOnFailure) {
        new Thread(() -> {
            try {
                Client client = Client.builder().apiKey(API_KEY).build();

                String prompt = "Generate a simple math equation with exactly 8 characters (including the equals sign). " +
                        "Example: 10+20=30 or 2*4+1=9. " +
                        "The equation must be mathematically correct. " +
                        "It must contain ONLY numbers and the symbols + - * / =. " +
                        "Do NOT use spaces. " +
                        "Reply with ONLY the equation, nothing else.";

                GenerateContentResponse response = client.models.generateContent(model, prompt, null);
                String equation = response.text().trim();
                Log.d(TAG, "Gemini generated (" + model + "): " + equation);

                if (isValidEquation(equation)) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            callback.onEquationGenerated(equation, "Puzzle Ready!", true));
                } else {
                    Log.e(TAG, "Invalid equation generated: " + equation);
                    if (retryOnFailure) {
                        generateEquationWithModel("gemini-1.5-flash", callback, false);
                    } else {
                        new Handler(Looper.getMainLooper()).post(() ->
                                callback.onEquationGenerated(null, "AI Error: Invalid Format", false));
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Gemini API Error (" + model + "): " + e.getMessage());
                if (retryOnFailure) {
                    Log.d(TAG, "Retrying with fallback model...");
                    generateEquationWithModel("gemini-1.5-flash", callback, false);
                } else {
                    new Handler(Looper.getMainLooper()).post(() ->
                            callback.onEquationGenerated(null, "Connection Error: " + e.getMessage(), false));
                }
            }
        }).start();
    }

    /**
     * Validates if the generated equation string meets the game rules AND is mathematically correct.
     * @param equation The equation string to check.
     * @return True if valid, false otherwise.
     */
    public boolean isValidEquation(String equation) {
        if (equation == null || equation.length() != 8) return false;
        if (!equation.matches("[0-9+\\-*/=]+")) return false;
        if (!equation.contains("=")) return false;
        
        String[] parts = equation.split("=");
        if (parts.length != 2) return false;
        
        try {
            double leftValue = evaluateExpression(parts[0]);
            double rightValue = evaluateExpression(parts[1]);
            // Check if equal closely enough for doubles
            return Math.abs(leftValue - rightValue) < 1e-9;
        } catch (Exception e) {
            return false;
        }
    }

    private double evaluateExpression(String expr) throws Exception {
        if (expr.isEmpty()) throw new Exception("Empty expression");
        java.util.List<String> postfix = infixToPostfix(expr);
        return evaluatePostfix(postfix);
    }

    private java.util.List<String> infixToPostfix(String expr) throws Exception {
        java.util.List<String> output = new java.util.ArrayList<>();
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
        if (number.length() > 0) output.add(number.toString());
        while (!ops.isEmpty()) output.add(String.valueOf(ops.pop()));
        return output;
    }

    private double evaluatePostfix(java.util.List<String> postfix) throws Exception {
        java.util.Stack<Double> stack = new java.util.Stack<>();
        for (String token : postfix) {
            if (token.matches("\\d+")) {
                stack.push(Double.parseDouble(token));
            } else if (token.length() == 1 && "+-*/".contains(token)) {
                if (stack.size() < 2) throw new Exception("Bad expression");
                double b = stack.pop();
                double a = stack.pop();
                switch (token.charAt(0)) {
                    case '+': stack.push(a + b); break;
                    case '-': stack.push(a - b); break;
                    case '*': stack.push(a * b); break;
                    case '/': if (b == 0) throw new Exception("Div/0"); stack.push(a / b); break;
                }
            } else throw new Exception("Invalid token");
        }
        if (stack.size() != 1) throw new Exception("Malformed");
        return stack.pop();
    }

    private int precedence(char op) {
        if (op == '+' || op == '-') return 1;
        if (op == '*' || op == '/') return 2;
        return 0;
    }
}
