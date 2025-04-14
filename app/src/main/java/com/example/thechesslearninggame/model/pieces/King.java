package com.example.thechesslearninggame.model.pieces;

import androidx.annotation.NonNull;

import com.example.thechesslearninggame.model.Board.Board;
import com.example.thechesslearninggame.model.Board.Position;
import com.example.thechesslearninggame.model.enums.PieceColor;
import com.example.thechesslearninggame.model.enums.PieceType;

public class King extends Piece {

    public King(PieceColor pieceColor) {
        super(pieceColor);
    }

    @Override
    public boolean isValidMove(Board board, Position from, Position to) {
        if (Math.abs(to.row() - from.row()) <= 1 && Math.abs(to.col() - from.col()) <= 1) {
            return true;
        }/* TODO castling
        if (fromRow == toRow && Math.abs(toCol - fromCol) == 2) {
            return validateCastling(fromRow, fromCol, toCol);
        }*/
        return false;
    }

    public PieceType getPieceType() {
        return PieceType.KING;
    }

    @NonNull
    @Override
    public Piece clone() {
        return new King(this.getPieceColor());
    }


}
