package com.example.thechesslearninggame.model;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.thechesslearninggame.modules.ChessGame;
import com.example.thechesslearninggame.modules.ChessMoveParser;
import com.example.thechesslearninggame.modules.StockfishManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StockfishViewModel extends AndroidViewModel {
    private final String TAG = "StockfishViewModel";
    private final StockfishManager stockfishManager;
    private final ChessGame chessGame;

    private final MutableLiveData<String[][]> boardState = new MutableLiveData<>();
    private final MutableLiveData<List<Integer>> validMoves = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPlayerTurn = new MutableLiveData<>(true);
    private final MutableLiveData<String> engineMoveDescription = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isEngineThinking = new MutableLiveData<>(false);

    private int selectedRow = -1;
    private int selectedCol = -1;
    private final List<Integer> redForCheck = new ArrayList<>();

    public StockfishViewModel(@NonNull Application application, StockfishManager stockfishManager) {
        super(application);
        this.stockfishManager = stockfishManager;
        this.chessGame = new ChessGame();
        boardState.setValue(chessGame.getBoard());
        validMoves.setValue(new ArrayList<>());
    }

    public LiveData<String[][]> getBoardState() {
        return boardState;
    }

    public LiveData<List<Integer>> getValidMoves() {
        return validMoves;
    }

    public LiveData<Boolean> getIsPlayerTurn() {
        return isPlayerTurn;
    }

    public LiveData<String> getEngineMoveDescription() {
        return engineMoveDescription;
    }

    public LiveData<Boolean> getIsEngineThinking() {
        return isEngineThinking;
    }

    public void onSquareClicked(int row, int col) {
        // Do not process if engine is thinking or it's not player's turn
        if (Boolean.FALSE.equals(isPlayerTurn.getValue()) || Boolean.TRUE.equals(isEngineThinking.getValue())) {
            return;
        }

        if (selectedRow == -1) {
            if (isValidSelection(row, col)) {
                selectedRow = row;
                selectedCol = col;
                computeValidMovesForSquare(row, col);
            }
        } else {
            if (validMoves.getValue() != null && validMoves.getValue().contains(row * 8 + col)) {
                chessGame.movePiece(selectedRow, selectedCol, row, col);
                updateBoardState();
                clearSelection();
                isPlayerTurn.postValue(false);
                triggerEngineMove();
            } else {
                clearSelection();
                updateBoardState();
            }
        }
    }

    private boolean isValidSelection(int row, int col) {
        String piece = chessGame.getBoard()[row][col];
        if (piece.isEmpty()) return false;
        // Assumes uppercase for white and lowercase for black.
        return chessGame.isWhiteTurn() == Character.isUpperCase(piece.charAt(0));
    }

    private void computeValidMovesForSquare(int fromRow, int fromCol) {
        List<Integer> moves = new ArrayList<>();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (chessGame.isValidMove(fromRow, fromCol, row, col)) {
                    moves.add(row * 8 + col);
                }
            }
        }
        validMoves.postValue(moves);
    }

    private void clearSelection() {
        selectedRow = -1;
        selectedCol = -1;
        validMoves.postValue(new ArrayList<>());
    }

    public void triggerEngineMove() {
        isEngineThinking.postValue(true);
        stockfishManager.getBestMove(chessGame.getFenString(), 2000, new StockfishManager.MoveCallback() {
            @Override
            public void onMoveReceived(String uciMove) {
                // Process engine move on main thread
                applyEngineMove(uciMove);
                String moveText = ChessMoveParser.toSpokenDescription(uciMove, chessGame, Locale.getDefault().getLanguage());
                engineMoveDescription.postValue(moveText);
                isEngineThinking.postValue(false);
                isPlayerTurn.postValue(true);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Engine error: " + error);
                isEngineThinking.postValue(false);
                isPlayerTurn.postValue(true);
            }
        });
    }

    private void applyEngineMove(String uciMove) {
        // Convert UCI move format (e.g., "e2e4") to board coordinates.
        int fromCol = uciMove.charAt(0) - 'a';
        int fromRow = 8 - Character.getNumericValue(uciMove.charAt(1));
        int toCol = uciMove.charAt(2) - 'a';
        int toRow = 8 - Character.getNumericValue(uciMove.charAt(3));

        if (chessGame.isValidMove(fromRow, fromCol, toRow, toCol)) {
            chessGame.movePiece(fromRow, fromCol, toRow, toCol);
            updateBoardState();
        } else {
            Log.e(TAG, "Invalid engine move: " + uciMove);
        }
    }

    private void updateBoardState() {
        redForCheck.clear();
        if (chessGame.isBlackInCheck()) {
            int[] blackKingPos = chessGame.getBlackKingPosition();
            redForCheck.add(blackKingPos[0] * 8 + blackKingPos[1]);
        } else if (chessGame.isWhiteInCheck()) {
            int[] whiteKingPos = chessGame.getWhiteKingPosition();
            redForCheck.add(whiteKingPos[0] * 8 + whiteKingPos[1]);
        }
        boardState.postValue(chessGame.getBoard());
        chessGame.updateFEN();
    }

    /**
     * Resets the game.
     */
    public void resetGame() {
        chessGame = new ChessGame();
        clearSelection();
        updateGameStatus();
        chessGame.updateFEN();
        chessGame.resetGame();
        clearSelection();
        updateBoardState();
        isPlayerTurn.postValue(true);
    }
}
