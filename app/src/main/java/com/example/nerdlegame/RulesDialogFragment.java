package com.example.nerdlegame;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
            rulesMusicPlayer.setLooping(true);
            rulesMusicPlayer.start();
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

        // Restart main background music if music is ON
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("NerdlePrefs", Context.MODE_PRIVATE);
        boolean isMusicOn = prefs.getBoolean("music_on", true);

        if (isMusicOn) {
            Intent serviceIntent = new Intent(requireContext(), MusicService.class);
            requireActivity().startService(serviceIntent);
        }
    }
}
