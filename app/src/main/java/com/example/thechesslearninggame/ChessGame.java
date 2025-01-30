package com.example.thechesslearninggame;

public class ChessGame {
    private String[][] board = new String[8][8]; // Piece positions (e.g., "R", "p")
    private boolean isWhiteTurn = true;
    private boolean whiteCastleKingside = true; // Castling rights
    private boolean whiteCastleQueenside = true;
    private boolean blackCastleKingside = true;
    private boolean blackCastleQueenside = true;
    private int[] enPassantSquare = {-1, -1}; // En passant target (e.g., [3, 4] for a5)
    private int halfMoveClock = 0; // For 50-move rule
    private int fullMoveNumber = 1;


    public ChessGame() {
        initializeChessBoard();
    }

    private void initializeChessBoard() {
        // Initialize the starting position of the chessboard
        board[0] = new String[]{"R", "N", "B", "Q", "K", "B", "N", "R"};
        board[1] = new String[]{"P", "P", "P", "P", "P", "P", "P", "P"};
        for (int i = 2; i < 6; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = ""; // Empty squares
            }
        }
        board[6] = new String[]{"p", "p", "p", "p", "p", "p", "p", "p"};
        board[7] = new String[]{"r", "n", "b", "q", "k", "b", "n", "r"};
    }



    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        String piece = board[fromRow][fromCol];
        if (piece.isEmpty()) return false; // No piece to move

        // Check turn (uppercase = white, lowercase = black)
        if (isWhiteTurn != Character.isUpperCase(piece.charAt(0))) return false;

        // Validate based on piece type
        return switch (piece.toUpperCase()) {
            case "P" -> validatePawnMove(fromRow, fromCol, toRow, toCol);
            case "R" -> validateRookMove(fromRow, fromCol, toRow, toCol);
            case "N" -> validateKnightMove(fromRow, fromCol, toRow, toCol);
            case "B" -> validateBishopMove(fromRow, fromCol, toRow, toCol);
            case "Q" -> validateQueenMove(fromRow, fromCol, toRow, toCol);
            case "K" -> validateKingMove(fromRow, fromCol, toRow, toCol);
            default -> false;
        };
    }


    private boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol) {
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
        int direction = isWhiteTurn ? 1 : -1;
        int startRow = isWhiteTurn ? 1 : 6;

        // Basic forward move (one step)
        if (fromCol == toCol && board[toRow][toCol].isEmpty()) {
            if (toRow == fromRow + direction) return true;
            if (fromRow == startRow && toRow == fromRow + 2 * direction && board[fromRow + direction][toCol].isEmpty()) {
                enPassantSquare = new int[]{toRow - direction, toCol}; // Fix: Correct en passant target
                return true;
            }
        }

        // Capture (diagonal move)
        if (Math.abs(toCol - fromCol) == 1 && toRow == fromRow + direction) {
            if (!board[toRow][toCol].isEmpty()) {
                return true; // Regular capture
            }
            // En Passant check
            return enPassantSquare != null && toRow == enPassantSquare[0] && toCol == enPassantSquare[1] &&
                    board[fromRow][toCol].equals(isWhiteTurn ? "p" : "P");
        }

        return false;
    }

    private boolean validateKingMove(int fromRow, int fromCol, int toRow, int toCol) {
        // Regular king move
        if (Math.abs(toRow - fromRow) <= 1 && Math.abs(toCol - fromCol) <= 1) {
            return true;
        }

        // Castling
        if (fromRow == toRow && Math.abs(toCol - fromCol) == 2) {
            return validateCastling(fromRow, fromCol, toCol);
        }
        return false;
    }

    private boolean validateCastling(int row, int fromCol, int toCol) {
        boolean isWhite = isWhiteTurn;
        boolean kingside = (toCol == 6); // Kingside: g1/g8; Queenside: c1/c8

        // Check castling rights and path
        if (isWhite) {
            if ((kingside && !whiteCastleKingside) || (!kingside && !whiteCastleQueenside)) return false;
        } else {
            if ((kingside && !blackCastleKingside) || (!kingside && !blackCastleQueenside)) return false;
        }

        // Check if path is clear and not under attack
        int colStep = kingside ? 1 : -1;
        for (int col = fromCol + colStep; col != (kingside ? 7 : 0); col += colStep) {
            if (!board[row][col].isEmpty()) return false;
        }
        return !isSquareUnderAttack(row, fromCol + colStep); // Check for attacks on path
    }

    private boolean isKingInCheck(boolean isWhiteKing) {
        int[] kingPos = findKing(isWhiteKing);
        return isSquareUnderAttack(kingPos[0], kingPos[1]);
    }

    private boolean isSquareUnderAttack(int row, int col) {
        // Check all opponent pieces attacking this square
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                String piece = board[r][c];
                if (piece.isEmpty() || (Character.isUpperCase(piece.charAt(0)) == isWhiteTurn)) continue;

                // Simulate if this piece can attack the square
                if (isValidMove(r, c, row, col)) return true;
            }
        }
        return false;
    }

    public boolean isCheckmate() {
        if (!isKingInCheck(isWhiteTurn)) return false;

        // Check if any valid move escapes check
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

    private int[] findKing(boolean isWhiteKing) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                String piece = board[row][col];
                if (piece.equalsIgnoreCase("K") &&
                        (Character.isUpperCase(piece.charAt(0)) == isWhiteKing)) {
                    return new int[]{row, col};
                }
            }
        }
        return new int[]{-1, -1}; // Should never happen
    }

    private boolean validateRookMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow != toRow && fromCol != toCol) return false; // Must move in a straight line
        return isPathClear(fromRow, fromCol, toRow, toCol);
    }

    private boolean validateKnightMove(int fromRow, int fromCol, int toRow, int toCol) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }

    private boolean validateBishopMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (Math.abs(toRow - fromRow) != Math.abs(toCol - fromCol)) return false; // Must move diagonally
        return isPathClear(fromRow, fromCol, toRow, toCol);
    }

    private boolean validateQueenMove(int fromRow, int fromCol, int toRow, int toCol) {
        return validateRookMove(fromRow, fromCol, toRow, toCol) ||
                validateBishopMove(fromRow, fromCol, toRow, toCol);
    }

    public String[][] getBoard() {
        return board;
    }

    public void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        String piece = board[fromRow][fromCol];
        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = ""; // Clear the old square

        // Handle special moves (e.g., castling, en passant, pawn promotion)
        if (piece.equalsIgnoreCase("K")) {
            // Update castling rights
            if (piece.equals("K")) {
                whiteCastleKingside = false;
                whiteCastleQueenside = false;
            } else if (piece.equals("k")) {
                blackCastleKingside = false;
                blackCastleQueenside = false;
            }

            // Handle castling
            if (Math.abs(toCol - fromCol) == 2) {
                if (toCol == 6) { // Kingside castling
                    board[toRow][5] = board[toRow][7]; // Move rook
                    board[toRow][7] = "";
                } else if (toCol == 2) { // Queenside castling
                    board[toRow][3] = board[toRow][0]; // Move rook
                    board[toRow][0] = "";
                }
            }
        }

        // Handle en passant
        if (piece.equalsIgnoreCase("P") && toRow == enPassantSquare[0] && toCol == enPassantSquare[1]) {
            board[fromRow][toCol] = ""; // Remove the captured pawn
        }

        // Update en passant target square
        if (piece.equalsIgnoreCase("P") && Math.abs(toRow - fromRow) == 2) {
            enPassantSquare = new int[]{(fromRow + toRow) / 2, fromCol};
        } else {
            enPassantSquare = new int[]{-1, -1};
        }

        // Handle pawn promotion (to-do: implement UI for promotion choice)
        if (piece.equalsIgnoreCase("P") && (toRow == 0 || toRow == 7)) {
            board[toRow][toCol] = "Q"; // Promote to queen by default
        }

        // Switch turns
        isWhiteTurn = !isWhiteTurn;
    }
}