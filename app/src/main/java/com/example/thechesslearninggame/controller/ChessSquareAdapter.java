package com.example.thechesslearninggame.controller;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.thechesslearninggame.R;
import com.example.thechesslearninggame.model.Board.Board;
import com.example.thechesslearninggame.model.enums.PieceColor;
import com.example.thechesslearninggame.model.pieces.Piece;

import java.util.ArrayList;
import java.util.List;

public class ChessSquareAdapter extends BaseAdapter {
    private final List<Integer> colors;
    private final Context context;
    private Board chessBoardState;
    private List<Integer> validMoves;
    private List<Integer> checkRed = new ArrayList<>();

    public ChessSquareAdapter(Context context, List<Integer> colors, Board chessBoardState, List<Integer> validMoves) {
        this.context = context;
        this.colors = colors;
        this.chessBoardState = chessBoardState;
        this.validMoves = validMoves;
    }

    @Override
    public int getCount() {
        return colors.size();
    }

    @Override
    public Object getItem(int position) {
        return colors.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = android.view.LayoutInflater.from(context).inflate(R.layout.square, parent, false);
            //na dynamicke zobrazenie pozdlz celeho displeja
            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            int squareSize = screenWidth / 8;
            view.setLayoutParams(new GridView.LayoutParams(squareSize, squareSize));
        }
        view.setBackgroundColor(colors.get(position));
        int row = position / 8;
        int col = position % 8;

        TextView coordLeft = view.findViewById(R.id.coord_right);
        TextView coordBottom = view.findViewById(R.id.coord_bottom);

        if (col == 7) {
            coordLeft.setText(String.valueOf(8 - row));
            coordLeft.setVisibility(View.VISIBLE);
        } else {
            coordLeft.setVisibility(View.GONE);
        }
        if (row == 7) {
            coordBottom.setText(String.valueOf((char) ('a' + col)));
            coordBottom.setVisibility(View.VISIBLE);
        } else {
            coordBottom.setVisibility(View.GONE);
        }

        boolean isLightSquare = (row + col) % 2 == 0;
        int textColor = isLightSquare ?
                ContextCompat.getColor(context, R.color.colorPrimary) :
                ContextCompat.getColor(context, R.color.chessBoardLight);

        coordLeft.setTextColor(textColor);
        coordBottom.setTextColor(textColor);

        Piece piece = chessBoardState.getSquareAt(row, col).getPiece();

        ImageView squareImage = view.findViewById(R.id.square_image);
        View validMoveIndicator = view.findViewById(R.id.validMoveIndicator);
        View checkBackground = view.findViewById(R.id.checkBackground);

        if (piece==null) {
            squareImage.setImageDrawable(null);
        } else {
            int resId = getDrawableResourceForPiece(piece);
            squareImage.setImageResource(resId);
        }

        if (validMoves.contains(position)) {
            validMoveIndicator.setVisibility(View.VISIBLE);
        } else {
            validMoveIndicator.setVisibility(View.GONE);
        }

        if (checkRed!=null && !checkRed.isEmpty() && checkRed.contains(position)) {
            checkBackground.setVisibility(View.VISIBLE);
        } else {
            checkBackground.setVisibility(View.GONE);
        }

        return view;
    }

    private int getDrawableResourceForPiece(Piece piece) {
        if (piece.getPieceColor().equals(PieceColor.WHITE)) {
            return switch (piece.getPieceType()) {
                case PAWN: yield R.drawable.wp;
                case ROOK: yield R.drawable.wr;
                case KNIGHT: yield R.drawable.wn;
                case BISHOP: yield R.drawable.wb;
                case QUEEN: yield R.drawable.wq;
                case KING: yield R.drawable.wk;
            };
        } else {
            return switch (piece.getPieceType()) {
                case PAWN: yield R.drawable.bp;
                case ROOK: yield R.drawable.br;
                case KNIGHT: yield R.drawable.bn;
                case BISHOP: yield R.drawable.bb;
                case QUEEN: yield R.drawable.bq;
                case KING: yield R.drawable.bk;
            };
        }

    }

    public void updateChessBoardState(Board newChessBoardState) {
        this.chessBoardState = newChessBoardState;
    }

    public void updateValidMoves(List<Integer> validMoves) {
        this.validMoves = validMoves;
        notifyDataSetChanged();
    }

    public void updateCheckBackgroundHighlighting(List<Integer> checkRed) {
        this.checkRed = checkRed;
        notifyDataSetChanged();
    }

}