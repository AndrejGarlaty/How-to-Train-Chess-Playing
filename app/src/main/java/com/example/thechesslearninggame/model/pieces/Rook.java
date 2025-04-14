package com.example.thechesslearninggame.model.pieces;

import androidx.annotation.NonNull;

import com.example.thechesslearninggame.model.Board.Board;
import com.example.thechesslearninggame.model.Board.Position;
import com.example.thechesslearninggame.model.enums.PieceColor;
import com.example.thechesslearninggame.model.enums.PieceType;

public class Rook extends Piece {

    public Rook(PieceColor pieceColor) {
        super(pieceColor);
    }

    @Override
    public boolean isValidMove(Board board, Position from, Position to) {
        if (from.row() != to.row() && from.col() != to.row()) return false;
        return board.isPathClear(from, to);
    }

    public PieceType getPieceType() {
        return PieceType.ROOK;
    }

    @NonNull
    @Override
    public Piece clone() {
        return new Rook(this.getPieceColor());
    }
}
