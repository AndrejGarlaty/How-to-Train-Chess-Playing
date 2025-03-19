package com.example.thechesslearninggame;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StockfishActivity extends BaseActivity {
    private final String TAG = "StockfishActivity";
    private GridView chessboard;
    private ChessSquareAdapter adapter;
    private ChessGame chessGame;
    private TextView turnIndicator;
    private Button resetButton;
    private Button voiceButton;
    private Language language;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 191;
    private VoiceOutputManager voiceOutputManager;
    private VoiceInputManager voiceInputManager;

    private boolean isEngineThinking = false;
    private StockfishManager stockfishManager;
    private boolean isPlayerTurn = true;
    private boolean isGameActive = false;

    private int selectedRow = -1;
    private int selectedCol = -1;
    private final List<Integer> validMoves = new ArrayList<>();
    private final List<Integer> redForCheck = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stockfish);

        String lang = this.getSharedPreferences("AppSettings", MODE_PRIVATE)
                .getString("selected_language", Locale.getDefault().getLanguage());
        language = lang.equals(Language.SLOVAK.getCode()) ? Language.SLOVAK : Language.ENGLISH;

        turnIndicator = findViewById(R.id.turnIndicator);
        resetButton = findViewById(R.id.resetButton);
        chessboard = findViewById(R.id.chessboard);
        voiceButton = findViewById(R.id.voiceButton);

        chessGame = new ChessGame();
        setupBoard();
        stockfishManager = new StockfishManager(this);
        initializeEngine();

        resetButton.setOnClickListener(v -> resetGame());
        chessboard.setOnItemClickListener((parent, view, position, id) -> {
            int row = position / 8;
            int col = position % 8;
            handleSquareClick(row, col);
        });

        voiceOutputManager = new VoiceOutputManager(this);
        voiceOutputManager.setOnTtsCompleteListener(
                () -> new Handler(Looper.getMainLooper()).postDelayed(
                        this::startListening, 0));

        voiceInputManager = new VoiceInputManager(this, new VoiceInputManager.VoiceInputCallback() {
            @Override
            public void onVoiceInputResult(String text) {
                Toast.makeText(StockfishActivity.this, text, Toast.LENGTH_SHORT).show();
                voiceButton.setText(R.string.voice_button);
                voiceButton.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimary));
                String uciMove = ChessMoveParser.parseToUCI(text, chessGame);
                if (uciMove != null) {
                    applyEngineMove(uciMove);
                    onPlayerMoveMade();
                } else {
                    String msg = getMessage();
                    Toast.makeText(StockfishActivity.this, msg, Toast.LENGTH_SHORT).show();
                    voiceOutputManager.speak(msg);
                }
            }
            @Override
            public void onVoiceInputError(String error, int errorCode) {
                if (errorCode==7) { //restart listening due to timeout
                    startListening();
                } else if (errorCode==8) { //recognition busy
                    stopListening();
                } else {
                    Toast.makeText(StockfishActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }
        });

        voiceButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(StockfishActivity.this,
                    Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                startListening();
            } else {
                ActivityCompat.requestPermissions(StockfishActivity.this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_RECORD_AUDIO_PERMISSION);
            }
        });
    }

    private String getMessage() {
        return this.getString(R.string.invalid_move);
    }

    private void startListening() {
        voiceInputManager.startListening();
        startProgressBarAnimation();
        voiceButton.setText(R.string.speak);
        voiceButton.setBackgroundColor(ContextCompat.getColor(this, R.color.voiceBtnActive));
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

    private void initializeEngine() {
        stockfishManager.initialize(new StockfishManager.InitCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> Log.i(TAG, getBaseContext().getString(R.string.engine_ready)));
                isGameActive = true;
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(StockfishActivity.this, error, Toast.LENGTH_LONG).show();
                    finish(); //cant continue
                });
            }
        });
    }

    private void onPlayerMoveMade() {
        if (!isGameActive || !isPlayerTurn) return;
        isPlayerTurn = false;
        triggerEngineMove();
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
        if (isEngineThinking || !isPlayerTurn) return;


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
                onPlayerMoveMade();
                clearSelection();
            } else {
                clearSelection();
                updateGameStatus();
            }
        }
    }

    private void triggerEngineMove() {
        isEngineThinking = true;
        Toast.makeText(StockfishActivity.this, R.string.engine_thinking, Toast.LENGTH_SHORT).show();

        stockfishManager.getBestMove(chessGame.getFenString(), 2000, new StockfishManager.MoveCallback() {
            @Override
            public void onMoveReceived(String uciMove) {
                runOnUiThread(() -> {
                    isEngineThinking = false;
                    isPlayerTurn = true;
                    Toast.makeText(StockfishActivity.this, uciMove, Toast.LENGTH_SHORT).show(); //debug

                    String moveText = ChessMoveParser.toSpokenDescription(uciMove, chessGame, language.getCode());
                    applyEngineMove(uciMove);

                    boolean isCheck = chessGame.isWhiteInCheck() || chessGame.isBlackInCheck();
                    String check = null;
                    if (isCheck) {
                        check = ChessMoveParser.checkToSpokenDescription(language.getCode(), chessGame.isCheckmate());
                    }
                    voiceOutputManager.speak(check!=null ? moveText+check : moveText);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    isEngineThinking = false;
                    Toast.makeText(StockfishActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void applyEngineMove(String uciMove) {
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
            Toast.makeText(StockfishActivity.this, "an error occurred, please restart the game", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleGameEnd() {
        if (chessGame.isCheckmate()) {
            isGameActive = false;
            if (chessGame.isWhiteTurn()) {
                turnIndicator.setText(R.string.game_over_black_wins);
            } else {
                turnIndicator.setText(R.string.game_over_white_wins);
            }
        } //todo draw?
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stockfishManager.shutdown();
        Log.i(TAG, "onDestroy: engine shut down");
        voiceInputManager.destroy();
        voiceOutputManager.shutdown();
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

    private void resetGame() {
        chessGame = new ChessGame();
        clearSelection();
        updateGameStatus();
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

}