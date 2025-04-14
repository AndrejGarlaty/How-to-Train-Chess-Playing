package com.example.thechesslearninggame.model.pieces;

import androidx.annotation.NonNull;

import com.example.thechesslearninggame.model.Board.Board;
import com.example.thechesslearninggame.model.Board.Position;
import com.example.thechesslearninggame.model.enums.PieceColor;
import com.example.thechesslearninggame.model.enums.PieceType;

public class Bishop extends Piece {

    public Bishop(PieceColor pieceColor) {
        super(pieceColor);
    }

    @Override
    public boolean isValidMove(Board board, Position from, Position to) {
        if (Math.abs(to.row() - from.row()) != Math.abs(to.col() - from.col())) return false;
        return board.isPathClear(from, to);
    }

    public PieceType getPieceType() {
        return PieceType.BISHOP;
    }

    @NonNull
    @Override
    public Piece clone() {
        return new Bishop(this.getPieceColor());
    }
}
