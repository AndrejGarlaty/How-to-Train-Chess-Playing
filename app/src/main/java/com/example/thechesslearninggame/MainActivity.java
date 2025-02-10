package com.example.thechesslearninggame;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private GridView chessboard;
    private ChessSquareAdapter adapter;
    private ChessGame chessGame;
    private TextView turnIndicator;
    private TextView checkStatus;
    private Button resetButton;

    // Selection and valid move tracking
    private int selectedRow = -1;
    private int selectedCol = -1;
    private final List<Integer> validMoves = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        turnIndicator = findViewById(R.id.turnIndicator);
        checkStatus = findViewById(R.id.checkStatus);
        resetButton = findViewById(R.id.resetButton);
        chessboard = findViewById(R.id.chessboard);

        // Initialize game
        chessGame = new ChessGame();
        setupBoard();

        // Reset button click listener
        resetButton.setOnClickListener(v -> resetGame());

        // Chessboard click handler
        chessboard.setOnItemClickListener((parent, view, position, id) -> {
            int row = position / 8;
            int col = position % 8;
            handleSquareClick(row, col);
        });
    }

    private void setupBoard() {
        // Create board colors and initialize adapter
        List<Integer> colors = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                colors.add(getSquareColor(row, col));
            }
        }
        adapter = new ChessSquareAdapter(this, colors, chessGame.getBoard(), validMoves);
        chessboard.setAdapter(adapter);
        updateGameStatus();
    }

    private int getSquareColor(int row, int col) {
        return (row + col) % 2 == 0 ?
                Color.parseColor("#EEEEEE") : // Light
                Color.parseColor("#3F51B5");  // Dark
    }

    private void handleSquareClick(int row, int col) {
        if (selectedRow == -1) {
            // Select piece
            if (isValidSelection(row, col)) {
                selectedRow = row;
                selectedCol = col;
                showValidMoves(row, col);
            }
        } else {
            // Move piece
            if (validMoves.contains(row * 8 + col)) {
                chessGame.movePiece(selectedRow, selectedCol, row, col);
                clearSelection();
            } else {
                clearSelection();
            }
            updateGameStatus();
        }
    }

    private boolean isValidSelection(int row, int col) {
        String piece = chessGame.getBoard()[row][col];
        if (piece.isEmpty()) return false;
        return chessGame.isWhiteTurn() == Character.isUpperCase(piece.charAt(0));
    }

    private void showValidMoves(int fromRow, int fromCol) {
        validMoves.clear();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (chessGame.isValidMove(fromRow, fromCol, row, col)) {
                    validMoves.add(row * 8 + col);
                }
            }
        }
        adapter.updateValidMoves(validMoves);
    }

    private void clearSelection() {
        selectedRow = -1;
        selectedCol = -1;
        validMoves.clear();
        adapter.updateValidMoves(validMoves);
    }

    private void updateGameStatus() {
        turnIndicator.setText(chessGame.isWhiteTurn() ? "White's Turn" : "Black's Turn");

        if (chessGame.isWhiteIsInCheck()) {
            checkStatus.setText(R.string.white_is_in_check);
        } else if (chessGame.isBlackIsInCheck()) {
            checkStatus.setText(R.string.black_is_in_check);
        } else {
            checkStatus.setText("");
        }

        // Update board display
        adapter.updateChessBoardState(chessGame.getBoard());
    }

    private void resetGame() {
        chessGame = new ChessGame();
        clearSelection();
        updateGameStatus();
    }
}