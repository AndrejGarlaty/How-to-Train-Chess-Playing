package com.example.thechesslearninggame.model.Board;

import com.example.thechesslearninggame.model.enums.PieceColor;
import com.example.thechesslearninggame.model.pieces.Piece;
import com.example.thechesslearninggame.model.pieces.PieceFactory;

public class Board {
    private Square[][] board;
    private int[] enPassantSquare = {-1, -1};
    private boolean isWhiteTurn = true;

    public Board(Square[][] copiedSquares) {
        this.board = copiedSquares;
    }

    public Board(){
        initializeBoard();
    }

    public void initializeBoard() {
        board = new Square[8][8];
        board[0][0] = new Square(PieceFactory.createRook(PieceColor.BLACK), new Position(0, 0));
        board[0][1] = new Square(PieceFactory.createKnight(PieceColor.BLACK), new Position(0, 1));
        board[0][2] = new Square(PieceFactory.createBishop(PieceColor.BLACK), new Position(0, 2));
        board[0][3] = new Square(PieceFactory.createQueen(PieceColor.BLACK), new Position(0, 3));
        board[0][4] = new Square(PieceFactory.createKing(PieceColor.BLACK), new Position(0, 4));
        board[0][5] = new Square(PieceFactory.createBishop(PieceColor.BLACK), new Position(0, 5));
        board[0][6] = new Square(PieceFactory.createKnight(PieceColor.BLACK), new Position(0, 6));
        board[0][7] = new Square(PieceFactory.createRook(PieceColor.BLACK), new Position(0, 7));
        for (int col = 0; col < 8; col++) {
            board[1][col] = new Square(PieceFactory.createPawn(PieceColor.BLACK), new Position(1, col));
        }

        for (int row = 2; row < 6; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = new Square(null, new Position(row, col));
            }
        }

        for (int col = 0; col < 8; col++) {
            board[6][col] = new Square(PieceFactory.createPawn(PieceColor.WHITE), new Position(6, col));
        }
        board[7][0] = new Square(PieceFactory.createRook(PieceColor.WHITE), new Position(7, 0));
        board[7][1] = new Square(PieceFactory.createKnight(PieceColor.WHITE), new Position(7, 1));
        board[7][2] = new Square(PieceFactory.createBishop(PieceColor.WHITE), new Position(7, 2));
        board[7][3] = new Square(PieceFactory.createQueen(PieceColor.WHITE), new Position(7, 3));
        board[7][4] = new Square(PieceFactory.createKing(PieceColor.WHITE), new Position(7, 4));
        board[7][5] = new Square(PieceFactory.createBishop(PieceColor.WHITE), new Position(7, 5));
        board[7][6] = new Square(PieceFactory.createKnight(PieceColor.WHITE), new Position(7, 6));
        board[7][7] = new Square(PieceFactory.createRook(PieceColor.WHITE), new Position(7, 7));
    }

    public Square[][] getBoard() {
        return board;
    }

    public void setBoard(Square[][] board) {
        this.board = board;
    }

    public Square getSquareAt(int row, int col) {
        return board[row][col];
    }

    public boolean isPathClear(Position from, Position to) {
        int rowStep = Integer.compare(to.row(), from.row());
        int colStep = Integer.compare(to.col(), from.col());
        int steps = Math.max(Math.abs(to.row() - from.row()), Math.abs(to.col() - from.col()));

        for (int i = 1; i < steps; i++) {
            int row = from.row() + i * rowStep;
            int col = from.col() + i * colStep;
            if (!board[row][col].isEmpty()) return false;
        }
        return true;
    }

    public boolean isSquareUnderAttack(Square targetSquare, boolean isAttackerWhite) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Square square = getSquareAt(r, c);
                if (square.getPiece()==null) continue;
                boolean isWhitePiece = square.getPiece().getPieceColor().equals(PieceColor.WHITE);
                if (isWhitePiece != isAttackerWhite) continue;

                int rowDiff = Math.abs(r - targetSquare.getPosition().row());
                int colDiff = Math.abs(c - targetSquare.getPosition().col());

                switch (square.getPiece().getPieceType()) {
                    case PAWN:
                        int pawnDirection = isWhitePiece ? -1 : 1;
                        if (colDiff == 1 && r + pawnDirection == targetSquare.getPosition().row()) return true;
                        break;
                    case KNIGHT:
                        if ((rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2)) return true;
                        break;
                    case BISHOP:
                        if (rowDiff == colDiff && isPathClear(square.getPosition(), targetSquare.getPosition())) return true;
                        break;
                    case ROOK:
                        if ((r == targetSquare.getPosition().row() || c == targetSquare.getPosition().col()) &&
                                isPathClear(square.getPosition(), targetSquare.getPosition())) return true;
                        break;
                    case QUEEN:
                        if ((rowDiff == colDiff || r == targetSquare.getPosition().row() || c == targetSquare.getPosition().col()) &&
                                isPathClear(square.getPosition(), targetSquare.getPosition())) return true;
                        break;
                    case KING:
                        if (rowDiff <= 1 && colDiff <= 1) return true;
                        break;
                }
            }
        }
        return false;
    }

    public void makeMove(Square from, Square to) {
        Square square = getSquareAt(to.getPosition().row(), to.getPosition().col());
        square.setPiece(from.getPiece());
        from.setPiece(null);
    }

    public Board deepCopy() {
        Square[][] copiedSquares = new Square[8][8];
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Square originalSquare = getSquareAt(row, col);
                Piece pieceCopy = null;
                if (originalSquare.getPiece() != null) {
                    pieceCopy = originalSquare.getPiece().clone();
                }
                copiedSquares[row][col] = new Square(pieceCopy, originalSquare.getPosition());
            }
        }
        return new Board(copiedSquares);
    }

    public int[] getEnPassantSquare() {
        return enPassantSquare;
    }

    public void setEnPassantSquare(int[] enPassantSquare) {
        this.enPassantSquare = enPassantSquare;
    }

    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    public void setWhiteTurn(boolean whiteTurn) {
        isWhiteTurn = whiteTurn;
    }
}
