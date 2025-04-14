package com.example.thechesslearninggame.model.pieces;

import com.example.thechesslearninggame.model.enums.PieceColor;

public class PieceFactory {

    public static Pawn createPawn(PieceColor color) {
        return new Pawn(color);
    }

    public static Rook createRook(PieceColor color) {
        return new Rook(color);
    }

    public static Knight createKnight(PieceColor color) {
        return new Knight(color);
    }

    public static Bishop createBishop(PieceColor color) {
        return new Bishop(color);
    }

    public static Queen createQueen(PieceColor color) {
        return new Queen(color);
    }

    public static King createKing(PieceColor color) {
        return new King(color);
    }
}
