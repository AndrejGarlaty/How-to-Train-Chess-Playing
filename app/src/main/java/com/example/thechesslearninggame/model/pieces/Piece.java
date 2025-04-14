package com.example.thechesslearninggame.model.pieces;

import androidx.annotation.NonNull;

import com.example.thechesslearninggame.model.Board.Board;
import com.example.thechesslearninggame.model.Board.Position;
import com.example.thechesslearninggame.model.enums.PieceColor;
import com.example.thechesslearninggame.model.enums.PieceType;

public abstract class Piece {
    private final PieceColor pieceColor;


    public Piece(PieceColor pieceColor) {
        this.pieceColor = pieceColor;
    }

    public PieceColor getPieceColor() {
        return this.pieceColor;
    }

    public abstract boolean isValidMove(Board board, Position from, Position to);

    public abstract PieceType getPieceType();

    @Override
    @NonNull
    public abstract Piece clone();
}
