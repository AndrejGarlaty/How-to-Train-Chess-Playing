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
    private String[][] chessBoardState = new String[8][8];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the GridView
        chessboard = findViewById(R.id.chessboard);

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

        // Log the number of squares
        Log.d("ChessApp", "Number of squares: " + squareColors.size());

        // Initialize the chessboard state
        initializeChessBoard();

        // Set up the adapter
        adapter = new ChessSquareAdapter(this, squareColors, chessBoardState);
        chessboard.setAdapter(adapter);

        // Handle square clicks
        chessboard.setOnItemClickListener((parent, view, position, id) -> {
            int row = position / 8;
            int col = position % 8;
            handleSquareClick(row, col);
        });
    }

    private void initializeChessBoard() {
        // Initialize the starting position of the chessboard
        chessBoardState[0] = new String[]{"R", "N", "B", "Q", "K", "B", "N", "R"};
        chessBoardState[1] = new String[]{"P", "P", "P", "P", "P", "P", "P", "P"};
        for (int i = 2; i < 6; i++) {
            for (int j = 0; j < 8; j++) {
                chessBoardState[i][j] = ""; // Empty squares
            }
        }
        chessBoardState[6] = new String[]{"p", "p", "p", "p", "p", "p", "p", "p"};
        chessBoardState[7] = new String[]{"r", "n", "b", "q", "k", "b", "n", "r"};
    }

    private int selectedRow = -1;
    private int selectedCol = -1;
    private boolean isWhiteTurn = true;

    private void handleSquareClick(int row, int col) {
        if (selectedRow == -1 && selectedCol == -1) {
            // No piece selected yet, select this piece
            if (!chessBoardState[row][col].isEmpty()) {
                if ((isWhiteTurn && Character.isUpperCase(chessBoardState[row][col].charAt(0))) ||
                        (!isWhiteTurn && Character.isLowerCase(chessBoardState[row][col].charAt(0)))) {
                    selectedRow = row;
                    selectedCol = col;
                }
            }
        } else {
            // Move the selected piece to this square
            chessBoardState[row][col] = chessBoardState[selectedRow][selectedCol];
            chessBoardState[selectedRow][selectedCol] = "";
            selectedRow = -1;
            selectedCol = -1;

            // Switch turns
            isWhiteTurn = !isWhiteTurn;

            // Update the adapter to refresh the board
            adapter.updateChessBoardState(chessBoardState);
        }
    }
}