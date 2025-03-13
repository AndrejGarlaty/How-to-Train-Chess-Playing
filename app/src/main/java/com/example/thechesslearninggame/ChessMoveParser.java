package com.example.thechesslearninggame;

import java.util.*;
import java.util.regex.*;

public class ChessMoveParser {
    private static final Map<String, String> pieceAbbreviations;
    static {
        pieceAbbreviations = Map.ofEntries(Map.entry("pawn", ""), Map.entry("pešiak", ""),
                Map.entry("knight", "N"), Map.entry("horse", "N"), Map.entry("jazdec", "N"), Map.entry("kôň", "N"),
                Map.entry("bishop", "B"), Map.entry("strelec", "B"),
                Map.entry("rook", "R"), Map.entry("veža", "R"),
                Map.entry("queen", "Q"), Map.entry("dáma", "Q"), Map.entry("kráľovná", "Q"),
                Map.entry("king", "K"), Map.entry("kráľ", "K"));
    }

    public static String parseToUCI(String spokenText, ChessGame chessGame) {
        String cleanedText = normalizeInput(spokenText);
        Matcher movematcher = Pattern.compile(
                "^\\s*(?:" +
                        // pešiak na c4 alebo iba c4
                        "(?:(knight|jazdec|kôň|bishop|strelec|rook|veža|queen|dáma|kráľovná|king|kráľ|pawn|pešiak)\\s+)?" +
                        "(?:to|na)?\\s*([a-hA-H])\\s*([1-8])" +
                        "|" +
                        // veža berie koňa na D5
                        "(?:(knight|jazdec|kôň|bishop|strelec|rook|veža|queen|dáma|kráľovná|king|kráľ|pawn|pešiak)\\s+)" +
                        "(?:takes|berie)\\s+" +
                        "(?:(knight|jazdca|koňa|bishop|strelca|rook|vežu|queen|dámu|kráľovnú|king|kráľa|krála|pawn|pešiaka)\\s+)?" +
                        "(?:to|na|on)?\\s*([a-hA-H])\\s*([1-8])" +
                        "|" +
                        // pešiak E4
                        "(?:(knight|jazdec|kôň|bishop|strelec|rook|veža|queen|dáma|kráľovná|king|kráľ|pawn|pešiak))\\s*([a-hA-H])\\s*([1-8])" +
                        "|" +
                        // D1 D3
                        "([a-hA-H])\\s*([1-8])\\s+([a-hA-H])\\s*([1-8])" +
                        ")\\s*$"
        ).matcher(cleanedText);

        if (!movematcher.find()) {
            return null; // No valid move pattern
        }

        String pieceName = null;
        String capturedPiece = null; //todo
        String sourceSquare;
        String targetSquare = null;

        // pešiak na c4 alebo iba c4
        if (movematcher.group(1) != null || (movematcher.group(2) != null && movematcher.group(3) != null)) {
            pieceName = movematcher.group(1);
            if (pieceName == null) {
                pieceName = "pawn";
            }
            targetSquare = movematcher.group(2).toLowerCase() + movematcher.group(3);
        }
        // veža berie koňa na D5
        else if (movematcher.group(4) != null) {
            pieceName = movematcher.group(4);
            if (pieceName == null) {
                pieceName = "pawn";
            }
            capturedPiece = movematcher.group(5);
            targetSquare = movematcher.group(6).toLowerCase() + movematcher.group(7);
        }
        // pešiak E4
        else if (movematcher.group(8) != null) {
            pieceName = movematcher.group(8);
            if (pieceName == null) {
                pieceName = "pawn";
            }
            targetSquare = movematcher.group(9).toLowerCase() + movematcher.group(10);
        }

        // D1 D3
        else if (movematcher.group(11) != null) {
            String sourceFile = movematcher.group(11).toLowerCase();
            String sourceRank = movematcher.group(12);
            sourceSquare = sourceFile + sourceRank;
            targetSquare = movematcher.group(13).toLowerCase() + movematcher.group(14);
            return sourceSquare+targetSquare;
        }

        String pieceAbbreviation = pieceAbbreviations.getOrDefault(pieceName.toLowerCase(), "");
        List<String> possibleSources = findPossibleSources(pieceAbbreviation, targetSquare, chessGame);
        if (possibleSources.isEmpty()) {
            return null; // No legal move found
        }
        String bestSourceSquare = resolveMoveAmbiguity(possibleSources, chessGame);
        return bestSourceSquare + targetSquare;
    }

    private static String normalizeInput(String spokenText) {
        return spokenText
                .toLowerCase()
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static List<String> findPossibleSources(String pieceAbbreviation, String targetSquare, ChessGame chessGame) {
        List<String> possibleMoves = new ArrayList<>();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                String piece = chessGame.getBoard()[row][col];
                if (piece.isEmpty())  {
                    continue;
                }
                if (!pieceAbbreviation.isEmpty() && !piece.equalsIgnoreCase(pieceAbbreviation)) {
                    continue;
                }
                if (pieceAbbreviation.isEmpty() && !piece.equalsIgnoreCase("P") && !piece.equalsIgnoreCase("p")) {
                    continue;
                }
                int destRow = 8 - Character.getNumericValue(targetSquare.charAt(1));
                int destCol = targetSquare.charAt(0) - 'a';

                if (chessGame.isValidMove(row, col, destRow, destCol)) {
                    possibleMoves.add(toUCIFormat(row, col));
                }
            }
        }
        return possibleMoves;
    }

    private static String resolveMoveAmbiguity(List<String> possibleSources, ChessGame chessGame) {
        if (possibleSources.size() == 1) {
            return possibleSources.get(0);
        }
        //todo logic
        return possibleSources.get(0);
    }

    private static String toUCIFormat(int row, int col) {
        char file = (char) ('a' + col);
        int rank = 8 - row;
        return "" + file + rank;
    }

    public static String toSpokenDescription(String uciMove, ChessGame chessGame, String language) {
        if (uciMove == null || uciMove.length() < 4) {
            return "";
        }
        String fromSquare = uciMove.substring(0, 2);
        String toSquare = uciMove.substring(2, 4);

        int fromCol = fromSquare.charAt(0) - 'a';
        int fromRow = 8 - Character.getNumericValue(fromSquare.charAt(1));

        String piece = chessGame.getBoard()[fromRow][fromCol];
        String spokenPiece = mapPieceToSpokenName(piece, language);
        String preposition = language.equals("sk") ? "na" : "to";
        return spokenPiece + " " + preposition + " " + toSquare;
    }

    public static String checkToSpokenDescription(String language, boolean isCheckmate) {
        String chess;
        if (isCheckmate) {
            chess = language.equals("sk") ? ", šachmat. Koniec hry." : ", checkmate. Game Over.";
        } else {
                chess = language.equals("sk") ? ", šach." : ", check.";
        }
        return chess;
    }

    private static String mapPieceToSpokenName(String piece, String language) {
        if (piece == null || piece.isEmpty()) {
            return "";
        }

        char letter = Character.toUpperCase(piece.charAt(0));
        return switch (letter) {
            case 'P' ->
                    language.equals("sk") ? "pešiak" : "pawn";
            case 'N' -> language.equals("sk") ? "jazdec" : "knight";
            case 'B' -> language.equals("sk") ? "strelec" : "bishop";
            case 'R' -> language.equals("sk") ? "veža" : "rook";
            case 'Q' -> language.equals("sk") ? "dáma" : "queen";
            case 'K' -> language.equals("sk") ? "kráľ" : "king";
            default -> "";
        };
    }

}
