package com.example.thechesslearninggame;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private GridView chessboard;
    private ChessSquareAdapter adapter;
    private ChessGame chessGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the GridView
        chessboard = findViewById(R.id.chessboard);
        chessGame = new ChessGame();
        // Create a list of colors for the chessboard squares
        List<Integer> squareColors = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if ((row + col) % 2 == 0) {
                    squareColors.add(Color.parseColor("#E0E0E0")); // Light squares
                } else {
                    squareColors.add(Color.parseColor("#3F51B5")); // Dark squares
                }
            }
        }

        // Set up the adapter
        adapter = new ChessSquareAdapter(this, squareColors, chessGame.getBoard());

        chessboard.setAdapter(adapter);

        // Handle square clicks
        chessboard.setOnItemClickListener((parent, view, position, id) -> {
            int row = position / 8;
            int col = position % 8;
            handleSquareClick(row, col);
        });
    }

    private int selectedRow = -1;
    private int selectedCol = -1;
    private boolean isWhiteTurn = true;

    private void handleSquareClick(int row, int col) {
        if (selectedRow == -1 && selectedCol == -1) {
            // Select a piece
            if (!chessGame.getBoard()[row][col].isEmpty()) {
                selectedRow = row;
                selectedCol = col;
            }
        } else {
            // Move the selected piece
            if (chessGame.isValidMove(selectedRow, selectedCol, row, col)) {
                chessGame.movePiece(selectedRow, selectedCol, row, col);
                adapter.updateChessBoardState(chessGame.getBoard());

                // Check for checkmate
                if (chessGame.isCheckmate()) {
                    // Handle game over
                    Log.d("ChessApp", "Checkmate!");
                }
            }
            selectedRow = -1;
            selectedCol = -1;
        }
    }
}