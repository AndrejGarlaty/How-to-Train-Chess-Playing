package com.example.thechesslearninggame.model;

import com.example.thechesslearninggame.model.Board.Board;
import com.example.thechesslearninggame.model.Board.Square;
import com.example.thechesslearninggame.model.enums.PieceColor;
import com.example.thechesslearninggame.model.enums.PieceType;
import com.example.thechesslearninggame.model.pieces.Piece;
import com.example.thechesslearninggame.model.pieces.PieceFactory;

public class ChessGame {
    //private String[][] board = new String[8][8];
    private Board board;
    private boolean isWhiteTurn = true;
    private boolean whiteCastleKingside = true;
    private boolean whiteCastleQueenside = true;
    private boolean blackCastleKingside = true;
    private boolean blackCastleQueenside = true;
    private Square enPassantSquare;
    private int halfMoveClock = 0;
    private int fullMoveNumber = 1;
  //  private int whiteKingRow = 7, whiteKingCol = 4;
  //  private int blackKingRow = 0, blackKingCol = 4;
    private Square whiteKingPos;
    private Square blackKingPos;
    private boolean whiteInCheck = false;
    private boolean blackInCheck = false;
    private String fenString;

    public ChessGame() {
        board = new Board();
        whiteKingPos = board.getSquareAt(7, 4);
        blackKingPos = board.getSquareAt(0, 4);
    }

    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        Square from = board.getSquareAt(fromRow, fromCol);
        Square to = board.getSquareAt(toRow, toCol);
        return isValidMove(from, to);
    }

    public boolean isValidMove(Square from, Square to) {
        Piece piece = from.getPiece();
        if (piece==null) return false;
        if (isWhiteTurn != piece.getPieceColor().equals(PieceColor.WHITE)) return false;

        Piece targetPiece = to.getPiece();
        if (targetPiece!=null) {
            if (targetPiece.getPieceColor().equals(PieceColor.WHITE) == isWhiteTurn) return false;
        }

        boolean isValid = piece.isValidMove(board, from.getPosition(), to.getPosition());

        if (isValid) {
            Board tempBoard = board.deepCopy();
            tempBoard.makeMove(from, to);

            Square kingsPosition = isWhiteTurn ? whiteKingPos : blackKingPos;

            if (piece.getPieceType().equals(PieceType.KING)) {
                kingsPosition = to;
            }

            if (enPassantSquare!=null) {
                if (piece.getPieceType().equals(PieceType.PAWN) && to.getPosition().row() == enPassantSquare.getPosition().row() && to.getPosition().col() == enPassantSquare.getPosition().col()) {
                    int capturedPawnRow = isWhiteTurn ? to.getPosition().row() + 1 : to.getPosition().row() - 1;
                    tempBoard.getSquareAt(capturedPawnRow,to.getPosition().col()).setPiece(null);
                }
            }
            boolean isStillInCheck = tempBoard.isSquareUnderAttack(tempBoard.getSquareAt(kingsPosition.getPosition().row(), kingsPosition.getPosition().col()), !isWhiteTurn);

            isValid = !isStillInCheck;
        }
        return isValid;
    }



    public boolean isCheckmate() {
        boolean inCheck = isWhiteTurn ? whiteInCheck : blackInCheck;
        if (!inCheck) return false;

        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                Square iterSquare = board.getSquareAt(fromRow, fromCol);
                if (iterSquare.isEmpty() ||
                        (iterSquare.getPiece().getPieceColor().equals(PieceColor.WHITE) != isWhiteTurn)) continue;

                for (int toRow = 0; toRow < 8; toRow++) {
                    for (int toCol = 0; toCol < 8; toCol++) {
                        if (isValidMove(board.getSquareAt(fromRow, fromCol), board.getSquareAt(toRow, toCol))) return false;
                    }
                }
            }
        }
        return true;
    }

    public Board getBoard() {
        return board;
    }

    public void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        Square from = board.getSquareAt(fromRow, fromCol);
        Square to = board.getSquareAt(toRow, toCol);
        movePiece(from, to);
    }

    public void movePiece(Square from, Square to) {
        if (!isValidMove(from, to)) return;

        board.makeMove(from, to);

        //for FEN generation
        boolean wasWhiteTurn = isWhiteTurn;
        boolean isPawnMove = from.getPiece().getPieceType().equals(PieceType.PAWN);
        boolean isCapture = to.getPiece()!=null;

        if (from.getPiece().getPieceType().equals(PieceType.KING)) {
            if (isWhiteTurn) {
                whiteKingPos = board.getSquareAt(to.getPosition().row(), to.getPosition().col());
                whiteCastleKingside = false;
                whiteCastleQueenside = false;
            } else {
                blackKingPos = board.getSquareAt(to.getPosition().row(), to.getPosition().col());
                blackCastleKingside = false;
                blackCastleQueenside = false;
            }

            if (Math.abs(to.getPosition().col() - from.getPosition().col()) == 2) {
                int rookFromCol = (to.getPosition().col() == 6) ? 7 : 0;
                int rookToCol = (to.getPosition().col() == 6) ? 5 : 3;
                board.makeMove(board.getSquareAt(to.getPosition().row(), rookFromCol), board.getSquareAt(to.getPosition().row(), rookToCol));
            }
        }

        if (from.getPiece().getPieceType().equals(PieceType.ROOK)) {
            if (isWhiteTurn) {
                if (from.getPosition().row() == 7 && from.getPosition().col() == 0) whiteCastleQueenside = false;
                else if (from.getPosition().row() == 7 && from.getPosition().col() == 7) whiteCastleKingside = false;
            } else {
                if (from.getPosition().row() == 0 && from.getPosition().col() == 0) blackCastleQueenside = false;
                else if (from.getPosition().row() == 0 && from.getPosition().col() == 7) blackCastleKingside = false;
            }
        }

        if (from.getPiece().getPieceType().equals(PieceType.PAWN) && to.getPosition().row() == enPassantSquare.getPosition().row() && to.getPosition().col() == enPassantSquare.getPosition().col()) {
            board.getSquareAt(from.getPosition().row(), to.getPosition().col()).setPiece(null);
        }

        enPassantSquare = (from.getPiece().getPieceType().equals(PieceType.PAWN) && Math.abs(to.getPosition().row() - from.getPosition().row()) == 2)
                ? board.getSquareAt((from.getPosition().row() + to.getPosition().row()) / 2, from.getPosition().col())
                : null;

        if (from.getPiece().getPieceType().equals(PieceType.PAWN) && (to.getPosition().row() == 0 || to.getPosition().row() == 7)) {
            if (isWhiteTurn()) {
                board.getSquareAt(to.getPosition().row(), to.getPosition().col()).setPiece(PieceFactory.createQueen(PieceColor.WHITE));
            } else {
                board.getSquareAt(to.getPosition().row(), to.getPosition().col()).setPiece(PieceFactory.createQueen(PieceColor.BLACK));
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
        whiteInCheck = board.isSquareUnderAttack(whiteKingPos, false);
        blackInCheck = board.isSquareUnderAttack(blackKingPos, true);
    }

    public Square getWhiteKingPosition() {
        return whiteKingPos;
    }

    public Square getBlackKingPosition() {
        return blackKingPos;
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
                Square square = board.getSquareAt(rank, file);
                Piece piece = square.getPiece();
                if (piece == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        placement.append(emptyCount);
                        emptyCount = 0;
                    }//todo this
                 //   placement.append(piece.getSymbol());
                    placement.append("r");
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
        if (enPassantSquare == null) return "-";
        char file = (char) ('a' + enPassantSquare.getPosition().col());
        int rank = 8 - enPassantSquare.getPosition().row();
        return "" + file + rank;
    }

    public String getFenString() {
        return fenString;
    }
}