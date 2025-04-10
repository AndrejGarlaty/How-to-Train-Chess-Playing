package com.example.thechesslearninggame.model;

import java.util.Arrays;

public class ChessGame {
    private String[][] board = new String[8][8];
    private boolean isWhiteTurn = true;
    private boolean whiteCastleKingside = true;
    private boolean whiteCastleQueenside = true;
    private boolean blackCastleKingside = true;
    private boolean blackCastleQueenside = true;
    private int[] enPassantSquare = {-1, -1};
    private int halfMoveClock = 0;
    private int fullMoveNumber = 1;
    private int whiteKingRow = 7, whiteKingCol = 4;
    private int blackKingRow = 0, blackKingCol = 4;
    private boolean whiteInCheck = false;
    private boolean blackInCheck = false;
    private String fenString;

    public ChessGame() {
        initializeChessBoard();
    }

    private void initializeChessBoard() {
        board[0] = new String[]{"r", "n", "b", "q", "k", "b", "n", "r"};
        board[1] = new String[]{"p", "p", "p", "p", "p", "p", "p", "p"};
        for (int i = 2; i < 6; i++) {
            Arrays.fill(board[i], "");
        }
        board[6] = new String[]{"P", "P", "P", "P", "P", "P", "P", "P"};
        board[7] = new String[]{"R", "N", "B", "Q", "K", "B", "N", "R"};
    }

    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        String piece = board[fromRow][fromCol];
        if (piece.isEmpty()) return false;
        if (isWhiteTurn != Character.isUpperCase(piece.charAt(0))) return false;

        String targetPiece = board[toRow][toCol];
        if (!targetPiece.isEmpty()) {
            if (Character.isUpperCase(targetPiece.charAt(0)) == isWhiteTurn) return false;
        }

        boolean isValid = switch (piece.toUpperCase()) {
            case "P" -> validatePawnMove(fromRow, fromCol, toRow, toCol);
            case "R" -> validateRookMove(fromRow, fromCol, toRow, toCol);
            case "N" -> validateKnightMove(fromRow, fromCol, toRow, toCol);
            case "B" -> validateBishopMove(fromRow, fromCol, toRow, toCol);
            case "Q" -> validateQueenMove(fromRow, fromCol, toRow, toCol);
            case "K" -> validateKingMove(fromRow, fromCol, toRow, toCol);
            default -> false;
        };

        if (isValid) {
            String[][] tempBoard = copyBoard();
            tempBoard[toRow][toCol] = tempBoard[fromRow][fromCol];
            tempBoard[fromRow][fromCol] = "";

            int kingRow = isWhiteTurn ? whiteKingRow : blackKingRow;
            int kingCol = isWhiteTurn ? whiteKingCol : blackKingCol;
            if (piece.equalsIgnoreCase("K")) {
                kingRow = toRow;
                kingCol = toCol;
            }

            if (piece.equalsIgnoreCase("P") && toRow == enPassantSquare[0] && toCol == enPassantSquare[1]) {
                int capturedPawnRow = isWhiteTurn ? toRow + 1 : toRow - 1;
                tempBoard[capturedPawnRow][toCol] = "";
            }

            boolean isStillInCheck = isSquareUnderAttack(kingRow, kingCol, tempBoard, !isWhiteTurn);

            isValid = !isStillInCheck;
        }

        return isValid;
    }

    private boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol, String[][] board) {
        int rowStep = Integer.compare(toRow, fromRow);
        int colStep = Integer.compare(toCol, fromCol);
        int steps = Math.max(Math.abs(toRow - fromRow), Math.abs(toCol - fromCol));

        for (int i = 1; i < steps; i++) {
            int row = fromRow + i * rowStep;
            int col = fromCol + i * colStep;
            if (!board[row][col].isEmpty()) return false;
        }
        return true;
    }

    private boolean validatePawnMove(int fromRow, int fromCol, int toRow, int toCol) {
        int direction = isWhiteTurn ? -1 : 1;
        int startRow = isWhiteTurn ? 6 : 1;
        //basic move
        if (fromCol == toCol && board[toRow][toCol].isEmpty()) {
            if (toRow == fromRow + direction) return true;
            if (fromRow == startRow && toRow == fromRow + 2 * direction && board[fromRow + direction][toCol].isEmpty()) {
                enPassantSquare = new int[]{toRow - direction, toCol};
                return true;
            }
        }
        //capture
        if (Math.abs(toCol - fromCol) == 1 && toRow == fromRow + direction) {
            if (!board[toRow][toCol].isEmpty()) {
                return true;
            }
            //en passant
            return enPassantSquare != null && toRow == enPassantSquare[0] && toCol == enPassantSquare[1] &&
                    board[fromRow][toCol].equals(isWhiteTurn ? "p" : "P");
        }
        return false;
    }

    private boolean validateKingMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (Math.abs(toRow - fromRow) <= 1 && Math.abs(toCol - fromCol) <= 1) {
            return true;
        }
        if (fromRow == toRow && Math.abs(toCol - fromCol) == 2) {
            return validateCastling(fromRow, fromCol, toCol);
        }
        return false;
    }

    private boolean validateCastling(int row, int fromCol, int toCol) {
        boolean isWhite = isWhiteTurn;
        boolean kingside = (toCol == 6);

        if (isWhite) {
            if ((kingside && !whiteCastleKingside) || (!kingside && !whiteCastleQueenside)) return false;
        } else {
            if ((kingside && !blackCastleKingside) || (!kingside && !blackCastleQueenside)) return false;
        }

        if (isWhite ? whiteInCheck : blackInCheck) return false;
        int colStep = kingside ? 1 : -1;
        int currentCol = fromCol + colStep;
        while (currentCol != (kingside ? 7 : 0)) {
            if (!board[row][currentCol].isEmpty()) return false;
            currentCol += colStep;
        }

        currentCol = fromCol;
        for (int i = 0; i < 2; i++) {
            currentCol += colStep;
            if (isSquareUnderAttack(row, currentCol, board, !isWhiteTurn)) return false;
        }
        return true;
    }

    private boolean isSquareUnderAttack(int row, int col, String[][] board, boolean isAttackerWhite) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                String piece = board[r][c];
                if (piece.isEmpty() || (Character.isUpperCase(piece.charAt(0)) != isAttackerWhite)) continue;

                boolean isWhitePiece = Character.isUpperCase(piece.charAt(0));
                if (isWhitePiece != isAttackerWhite) continue;

                int rowDiff = Math.abs(r - row);
                int colDiff = Math.abs(c - col);

                switch (piece.toUpperCase()) {
                    case "P":
                        int pawnDirection = isWhitePiece ? -1 : 1;
                        if (colDiff == 1 && r + pawnDirection == row) return true;
                        break;
                    case "N":
                        if ((rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2)) return true;
                        break;
                    case "B":
                        if (rowDiff == colDiff && isPathClear(r, c, row, col, board)) return true;
                        break;
                    case "R":
                        if ((r == row || c == col) && isPathClear(r, c, row, col, board)) return true;
                        break;
                    case "Q":
                        if ((rowDiff == colDiff || r == row || c == col) && isPathClear(r, c, row, col, board)) return true;
                        break;
                    case "K":
                        if (rowDiff <= 1 && colDiff <= 1) return true;
                        break;
                }
            }
        }
        return false;
    }

    public boolean isCheckmate() {
        boolean inCheck = isWhiteTurn ? whiteInCheck : blackInCheck;
        if (!inCheck) return false;

        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                if (board[fromRow][fromCol].isEmpty() ||
                        (Character.isUpperCase(board[fromRow][fromCol].charAt(0)) != isWhiteTurn)) continue;

                for (int toRow = 0; toRow < 8; toRow++) {
                    for (int toCol = 0; toCol < 8; toCol++) {
                        if (isValidMove(fromRow, fromCol, toRow, toCol)) return false;
                    }
                }
            }
        }
        return true;
    }

    private String[][] copyBoard() {
        String[][] copy = new String[8][8];
        for (int i = 0; i < 8; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, 8);
        }
        return copy;
    }

    private boolean validateRookMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow != toRow && fromCol != toCol) return false;
        return isPathClear(fromRow, fromCol, toRow, toCol, board);
    }

    private boolean validateKnightMove(int fromRow, int fromCol, int toRow, int toCol) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }

    private boolean validateBishopMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (Math.abs(toRow - fromRow) != Math.abs(toCol - fromCol)) return false;
        return isPathClear(fromRow, fromCol, toRow, toCol, board);
    }

    private boolean validateQueenMove(int fromRow, int fromCol, int toRow, int toCol) {
        return validateRookMove(fromRow, fromCol, toRow, toCol) || validateBishopMove(fromRow, fromCol, toRow, toCol);
    }

    public String[][] getBoard() {
        return board;
    }

    public void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        if (!isValidMove(fromRow, fromCol, toRow, toCol)) return;

        String piece = board[fromRow][fromCol];
        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = "";

        //for FEN generation
        boolean wasWhiteTurn = isWhiteTurn;
        String movedPiece = board[fromRow][fromCol];
        String capturedPiece = board[toRow][toCol];
        boolean isPawnMove = movedPiece.equalsIgnoreCase("P");
        boolean isCapture = !capturedPiece.isEmpty();

        if (piece.equalsIgnoreCase("K")) {
            if (isWhiteTurn) {
                whiteKingRow = toRow;
                whiteKingCol = toCol;
                whiteCastleKingside = false;
                whiteCastleQueenside = false;
            } else {
                blackKingRow = toRow;
                blackKingCol = toCol;
                blackCastleKingside = false;
                blackCastleQueenside = false;
            }

            if (Math.abs(toCol - fromCol) == 2) {
                int rookFromCol = (toCol == 6) ? 7 : 0;
                int rookToCol = (toCol == 6) ? 5 : 3;
                board[toRow][rookToCol] = board[toRow][rookFromCol];
                board[toRow][rookFromCol] = "";
            }
        }

        if (piece.equalsIgnoreCase("R")) {
            if (isWhiteTurn) {
                if (fromRow == 7 && fromCol == 0) whiteCastleQueenside = false;
                else if (fromRow == 7 && fromCol == 7) whiteCastleKingside = false;
            } else {
                if (fromRow == 0 && fromCol == 0) blackCastleQueenside = false;
                else if (fromRow == 0 && fromCol == 7) blackCastleKingside = false;
            }
        }

        if (piece.equalsIgnoreCase("P") && toRow == enPassantSquare[0] && toCol == enPassantSquare[1]) {
            board[fromRow][toCol] = "";
        }

        enPassantSquare = (piece.equalsIgnoreCase("P") && Math.abs(toRow - fromRow) == 2)
                ? new int[]{(fromRow + toRow) / 2, fromCol}
                : new int[]{-1, -1};

        if (piece.equalsIgnoreCase("P") && (toRow == 0 || toRow == 7)) {
            if (isWhiteTurn()) {
                board[toRow][toCol] = "Q";
            } else {
                board[toRow][toCol] = "q";
            }

        }

        if (isPawnMove || isCapture) {
            halfMoveClock = 0;
        } else {
            halfMoveClock++;
        }

        if (!wasWhiteTurn) {
            fullMoveNumber++;
        }
        updateCheckStatus();
        isWhiteTurn = !isWhiteTurn;
    }

    private void updateCheckStatus() {
        whiteInCheck = isSquareUnderAttack(whiteKingRow, whiteKingCol, board, false);
        blackInCheck = isSquareUnderAttack(blackKingRow, blackKingCol, board, true);
    }

    public int[] getWhiteKingPosition() {
        return new int[]{whiteKingRow, whiteKingCol};
    }

    public int[] getBlackKingPosition() {
        return new int[]{blackKingRow, blackKingCol};
    }

    public boolean isWhiteInCheck() {
        return whiteInCheck;
    }

    public boolean isBlackInCheck() {
        return blackInCheck;
    }

    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    public void updateFEN() {
        fenString = getPiecePlacement() + " " +
                getActiveColor() + " " +
                getCastlingRights() + " " +
                getEnPassant() + " " +
                halfMoveClock + " " +
                fullMoveNumber;
    }

    private String getPiecePlacement() {
        StringBuilder placement = new StringBuilder();
        for (int rank = 0; rank < 8; rank++) {
            int emptyCount = 0;
            for (int file = 0; file < 8; file++) {
                String piece = board[rank][file];
                if (piece.isEmpty()) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        placement.append(emptyCount);
                        emptyCount = 0;
                    }
                    placement.append(piece);
                }
            }
            if (emptyCount > 0) {
                placement.append(emptyCount);
            }
            if (rank < 7) {
                placement.append("/");
            }
        }
        return placement.toString();
    }

    private String getActiveColor() {
        return isWhiteTurn ? "w" : "b";
    }

    private String getCastlingRights() {
        StringBuilder rights = new StringBuilder();
        if (whiteCastleKingside) rights.append('K');
        if (whiteCastleQueenside) rights.append('Q');
        if (blackCastleKingside) rights.append('k');
        if (blackCastleQueenside) rights.append('q');
        return rights.length() > 0 ? rights.toString() : "-";
    }

    private String getEnPassant() {
        if (enPassantSquare[0] == -1) return "-";
        char file = (char) ('a' + enPassantSquare[1]);
        int rank = 8 - enPassantSquare[0];
        return "" + file + rank;
    }

    public String getFenString() {
        return fenString;
    }
}