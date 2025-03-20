package com.example.thechesslearninggame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;

public class MainMenuActivity extends BaseActivity {
    private ActivityResultLauncher<Intent> settingsLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Button btnLocalGame = findViewById(R.id.btn_local_game);
        Button btnStockfish = findViewById(R.id.btn_stockfish);
        Button btnExit = findViewById(R.id.btn_exit);

        settingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) recreate();
                }
        );

        // Local multiplayer (existing game)
        btnLocalGame.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, ChessActivity.class);
            startActivity(intent);
        });

        // Play against AI (Stockfish)
        btnStockfish.setOnClickListener(v -> {
            showDifficultyDialog();
        });

        btnExit.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, SettingsActivity.class);
            settingsLauncher.launch(intent);
        });
    }

    private void showDifficultyDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_difficulty, null);

        Slider slider = dialogView.findViewById(R.id.sliderDifficulty);
        TextView tvDifficultyValue = dialogView.findViewById(R.id.tvDifficultyValue);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        MaterialButton btnPlay = dialogView.findViewById(R.id.btnPlay);

        slider.addOnChangeListener((slider1, value, fromUser) -> {
            String diffValue = getString(R.string.difficulty) + (int) value;
            tvDifficultyValue.setText(diffValue);
                }
        );

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnPlay.setOnClickListener(v -> {
            int difficulty = (int) slider.getValue();
            Intent intent = new Intent(MainMenuActivity.this, StockfishActivity.class);
            intent.putExtra("difficulty", difficulty);
            startActivity(intent);
            dialog.dismiss();
        });

        dialog.show();
    }

}
