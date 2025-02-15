package com.example.thechesslearninggame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Button btnLocalGame = findViewById(R.id.btn_local_game);
        Button btnStockfish = findViewById(R.id.btn_stockfish);
        Button btnExit = findViewById(R.id.btn_exit);

        // Local multiplayer (existing game)
        btnLocalGame.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, ChessActivity.class);
            startActivity(intent);
        });

        // Play against AI (Stockfish)
        btnStockfish.setOnClickListener(v -> {
        //    Intent intent = new Intent(MainMenuActivity.this, StockfishActivity.class);
         //   startActivity(intent);
        });

        btnExit.setOnClickListener(v -> finish());
    }
}
