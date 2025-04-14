package com.example.thechesslearninggame.model.pieces;

import androidx.annotation.NonNull;

import com.example.thechesslearninggame.model.Board.Board;
import com.example.thechesslearninggame.model.Board.Position;
import com.example.thechesslearninggame.model.enums.PieceColor;
import com.example.thechesslearninggame.model.enums.PieceType;

public class Queen extends Piece {

    public Queen(PieceColor pieceColor) {
        super(pieceColor);
    }

    @Override
    public boolean isValidMove(Board board, Position from, Position to) {
        return isValidMoveLikeBishop(board, from, to) || isValidMoveLikeRook(board, from, to);

    }

    public PieceType getPieceType() {
        return PieceType.QUEEN;
    }

    private boolean isValidMoveLikeBishop(Board board, Position from, Position to) {
        if (Math.abs(to.row() - from.row()) != Math.abs(to.col() - from.col())) return false;
        return board.isPathClear(from, to);
    }

    public boolean isValidMoveLikeRook(Board board, Position from, Position to) {
        if (from.row() != to.row() && from.col() != to.row()) return false;
        return board.isPathClear(from, to);
    }

    @NonNull
    @Override
    public Piece clone() {
        return new Queen(this.getPieceColor());
    }
}
