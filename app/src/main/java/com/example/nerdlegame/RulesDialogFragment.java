package com.example.nerdlegame;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 * A DialogFragment that displays the game rules.
 * Handles playing specific "Rules" music while open and notifying the parent Activity on dismissal.
 */
public class RulesDialogFragment extends DialogFragment {

    private MediaPlayer rulesMusicPlayer;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Inflate custom layout
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_rules, null);

        Button btnClose = view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dismiss());

        // Set HTML text for rules
        android.widget.TextView rulesText = view.findViewById(R.id.rulesText);
        rulesText.setText(android.text.Html.fromHtml(getString(R.string.rules_text), android.text.Html.FROM_HTML_MODE_LEGACY));

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if music is allowed
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("NerdlePrefs", Context.MODE_PRIVATE);
        boolean isMusicOn = prefs.getBoolean("music_on", true);

        if (isMusicOn) {
            // Start playing rules music when dialog is shown
            rulesMusicPlayer = MediaPlayer.create(getContext(), R.raw.rulesmusic);
            if (rulesMusicPlayer != null) {
                rulesMusicPlayer.setLooping(true);
                rulesMusicPlayer.start();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // Stop rules music
        if (rulesMusicPlayer != null) {
            rulesMusicPlayer.stop();
            rulesMusicPlayer.release();
            rulesMusicPlayer = null;
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        // Notify MainActivity to resume background music
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onRulesDismissed();
        }
    }
}
