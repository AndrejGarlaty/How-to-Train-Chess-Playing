package com.example.thechesslearninggame.model.pieces;

import androidx.annotation.NonNull;

import com.example.thechesslearninggame.model.Board.Board;
import com.example.thechesslearninggame.model.Board.Position;
import com.example.thechesslearninggame.model.Board.Square;
import com.example.thechesslearninggame.model.enums.PieceColor;
import com.example.thechesslearninggame.model.enums.PieceType;

public class Pawn extends Piece {

    public Pawn(PieceColor pieceColor) {
        super(pieceColor);
    }

    @Override
    public boolean isValidMove(Board board, Position from, Position to) {
        int direction = getPieceColor().equals(PieceColor.WHITE) ? -1 : 1;
        int startRow = getPieceColor().equals(PieceColor.WHITE) ? 6 : 1;
        //basic move
        if (from.col() == to.col() && board.getSquareAt(to.row(), to.col()).isEmpty()) {
            if (to.row() == from.row() + direction) return true;
            if (from.row() == startRow && to.row() == from.row() + 2 * direction && board.getSquareAt(from.row() + direction, to.col()).isEmpty()) {
                board.setEnPassantSquare(new int[]{to.row() - direction, to.col()});
                return true;
            }
        }
        //capture
        if (Math.abs(to.col() - from.col()) == 1 && to.row() == from.row() + direction) {
            if (!board.getSquareAt(to.row(), to.col()).isEmpty()) {
                return true;
            }
            //en passant
            Square square = board.getSquareAt(from.row(), to.col());
            boolean isEnemyPawn = square.getPiece().getPieceType().equals(PieceType.PAWN) &&
                    board.isWhiteTurn() ? square.getPiece().getPieceColor().equals(PieceColor.BLACK) : square.getPiece().getPieceColor().equals(PieceColor.WHITE);

            return board.getEnPassantSquare() != null && to.row() == board.getEnPassantSquare()[0] && to.col() == board.getEnPassantSquare()[1] &&
                    isEnemyPawn;
        }
        return false;
    }

    public PieceType getPieceType() {
        return PieceType.PAWN;
    }

    @NonNull
    @Override
    public Piece clone() {
        return new Pawn(this.getPieceColor());
    }


}
