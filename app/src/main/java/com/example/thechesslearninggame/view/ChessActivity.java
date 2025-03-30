package com.example.thechesslearninggame.view;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.example.thechesslearninggame.modules.ChessGame;
import com.example.thechesslearninggame.R;

import java.util.ArrayList;
import java.util.List;

public class ChessActivity extends BaseActivity {

    private GridView chessboard;
    private ChessSquareAdapter adapter;
    private ChessGame chessGame;
    private TextView turnIndicator;
    private Button resetButton;

    private int selectedRow = -1;
    private int selectedCol = -1;
    private final List<Integer> validMoves = new ArrayList<>();
    private final List<Integer> redForCheck = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess);

        turnIndicator = findViewById(R.id.turnIndicator);
        resetButton = findViewById(R.id.resetButton);
        chessboard = findViewById(R.id.chessboard);

        chessGame = new ChessGame();
        setupBoard();
        //chessGame.generateFEN();

        resetButton.setOnClickListener(v -> resetGame());
        chessboard.setOnItemClickListener((parent, view, position, id) -> {
            int row = position / 8;
            int col = position % 8;
            handleSquareClick(row, col);
        });
    }

    private void setupBoard() {
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
            if (isValidSelection(row, col)) {
                selectedRow = row;
                selectedCol = col;
                showValidMoves(row, col);
            }
        } else {
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
        if (chessGame.isBlackInCheck()) {
            int[] blackKingPos = chessGame.getBlackKingPosition();
            redForCheck.add(blackKingPos[0] * 8 + blackKingPos[1]);
            adapter.updateCheckBackgroundHighlighting(redForCheck);
        } else if (chessGame.isWhiteInCheck()) {
            int[] whiteKingPosition = chessGame.getWhiteKingPosition();
            redForCheck.add(whiteKingPosition[0] * 8 + whiteKingPosition[1]);
            adapter.updateCheckBackgroundHighlighting(redForCheck);
        } else {
            redForCheck.clear();
            adapter.updateCheckBackgroundHighlighting(null);
        }

        turnIndicator.setText(chessGame.isWhiteTurn() ? R.string.white_s_turn : R.string.black_s_turn);
        if (chessGame.isCheckmate()) {
            if (chessGame.isWhiteTurn()) {
                turnIndicator.setText(R.string.game_over_black_wins);
            } else {
                turnIndicator.setText(R.string.game_over_white_wins);
            }
        } //todo draw?
        adapter.updateChessBoardState(chessGame.getBoard());
        chessGame.updateFEN();
    }

    private void resetGame() {
        chessGame = new ChessGame();
        clearSelection();
        updateGameStatus();
    }
}