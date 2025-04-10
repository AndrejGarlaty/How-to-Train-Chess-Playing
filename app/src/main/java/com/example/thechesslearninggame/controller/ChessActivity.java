package com.example.thechesslearninggame.controller;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.thechesslearninggame.model.ChessGame;
import com.example.thechesslearninggame.model.ChessMoveParser;
import com.example.thechesslearninggame.model.enums.Preferences;
import com.example.thechesslearninggame.R;
import com.example.thechesslearninggame.model.enums.VoiceInput;
import com.example.thechesslearninggame.model.VoiceInputManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class ChessActivity extends BaseActivity {
    private final String TAG = "ChessActivity";
    private GridView chessboard;
    private SquareAdapter adapter;
    private ChessGame chessGame;
    private TextView turnIndicator;
    private Button voiceButton;
    private VoiceInput voiceInput;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 191;
    private VoiceInputManager voiceInputManager;

    private int selectedRow = -1;
    private int selectedCol = -1;
    private final List<Integer> validMoves = new ArrayList<>();
    private final List<Integer> redForCheck = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess);
        setPreferences();

        turnIndicator = findViewById(R.id.turnIndicator);
        ImageButton helpButton = findViewById(R.id.btn_help);
        chessboard = findViewById(R.id.chessboard);
        voiceButton = findViewById(R.id.voiceButton);

        chessGame = new ChessGame();
        setupBoard();

        helpButton.setOnClickListener(v -> showHelpDialog());
        chessboard.setOnItemClickListener((parent, view, position, id) -> {
            int row = position / 8;
            int col = position % 8;
            handleSquareClick(row, col);
        });


        if (!voiceInput.equals(VoiceInput.NONE)) {
            voiceInputManager = new VoiceInputManager(this, new VoiceInputManager.VoiceInputCallback() {
                @Override
                public void onVoiceInputResult(String text) {
                 //   Toast.makeText(ChessActivity.this, text, Toast.LENGTH_SHORT).show(); debug only
                    voiceButton.setText(R.string.voice_button);
                    voiceButton.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimary));
                    String uciMove = ChessMoveParser.parseToUCI(text, chessGame);
                    if (uciMove != null) {
                        applyMove(uciMove);
                        clearSelection();
                    } else {
                        String msg = getMessage();
                        Toast.makeText(ChessActivity.this, msg+": "+text, Toast.LENGTH_SHORT).show();
                    }
                    if (voiceInput.equals(VoiceInput.CONTINOUS)) {
                        startListening();
                    }
                }
                @Override
                public void onVoiceInputError(String error, int errorCode) {
                    if (voiceInput.equals(VoiceInput.CONTINOUS)) {
                        if (errorCode==7) { //restart listening due to timeout
                            startListening();
                            return;
                        }
                    } else if (voiceInput.equals(VoiceInput.PUSH_TO_TALK)) {
                        voiceButton.setText(R.string.voice_button);
                        voiceButton.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimary));
                    }
                     if (errorCode==8) { //recognition busy
                        stopListening();
                    } else {
                        Toast.makeText(ChessActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            voiceButton.setOnClickListener(v -> {
                if (ContextCompat.checkSelfPermission(ChessActivity.this,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    startListening();
                } else {
                    ActivityCompat.requestPermissions(ChessActivity.this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            REQUEST_RECORD_AUDIO_PERMISSION);
                }
            });
        } else {
            voiceButton.setVisibility(TextView.GONE);
        }
    }

    private String getMessage() {
        return this.getString(R.string.invalid_move);
    }

    private void startListening() {
        if (!chessGame.isCheckmate()) {
            voiceInputManager.startListening();
            startProgressBarAnimation();
            voiceButton.setText(R.string.speak);
            voiceButton.setBackgroundColor(ContextCompat.getColor(this, R.color.voiceBtnActive));
        }
    }

    private void stopListening() {
        voiceInputManager.stopListening();
        voiceButton.setText(R.string.voice_button);
        voiceButton.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
    }

    private void startProgressBarAnimation() {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress(100);
        ObjectAnimator animator = ObjectAnimator.ofInt(
                progressBar,
                "progress",
                100,
                0
        );
        animator.setDuration(1500);
        animator.start();
    }

    private void setupBoard() {
        List<Integer> colors = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                colors.add(getSquareColor(row, col));
            }
        }
        adapter = new SquareAdapter(this, colors, chessGame.getBoard(), validMoves);
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
                updateGameStatus();
                clearSelection();
            } else {
                clearSelection();
                updateGameStatus();
            }
        }
    }

    private void applyMove(String uciMove) {
        // Convert UCI format (e7e5) to board coordinates
        int fromCol = uciMove.charAt(0) - 'a';
        int fromRow = 8 - Character.getNumericValue(uciMove.charAt(1));
        int toCol = uciMove.charAt(2) - 'a';
        int toRow = 8 - Character.getNumericValue(uciMove.charAt(3));

        if (chessGame.isValidMove(fromRow, fromCol, toRow, toCol)) {
            chessGame.movePiece(fromRow, fromCol, toRow, toCol);
            updateGameStatus();
        } else {
            Log.e(TAG, "invalidMove: " + uciMove);
            Toast.makeText(ChessActivity.this, "an error occurred, please restart the game", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleGameEnd() {
        if (chessGame.isCheckmate()) {
            if (chessGame.isWhiteTurn()) {
                turnIndicator.setText(R.string.game_over_black_wins);
            } else {
                turnIndicator.setText(R.string.game_over_white_wins);
            }
            if (voiceInputManager!=null) {
                voiceInputManager.stopListening();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: engine shut down");
        if (voiceInputManager != null) {
            voiceInputManager.destroy();
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
        handleGameEnd();
        adapter.updateChessBoardState(chessGame.getBoard());
        chessGame.updateFEN();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening();
            } else {
                String msg = this.getString(R.string.permission_denied);
                Log.i(TAG, "onRequestPermissionsResult: " + msg);
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setPreferences() {
        SharedPreferences preferences = this.getSharedPreferences(Preferences.NAME.getValue(), MODE_PRIVATE);
        String vi = preferences.getString(Preferences.VOICE_INPUT.getValue(), VoiceInput.NONE.name());
        voiceInput = VoiceInput.valueOf(vi);
    }

    private void showHelpDialog() {
        AlertDialog alertDialog =  new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.helpTitle)
                .setMessage(Html.fromHtml(getString(R.string.help_dialog_message), Html.FROM_HTML_MODE_LEGACY))
                .setPositiveButton(R.string.help_positive_button, (dialog, which) -> dialog.dismiss())
                .show();
        Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(ContextCompat.getColor(this, R.color.white));
    }

}