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

import java.util.ArrayList;
import java.util.List;

public class SquareAdapter extends BaseAdapter {
    private final List<Integer> colors;
    private final Context context;
    private String[][] chessBoardState;
    private List<Integer> validMoves;
    private List<Integer> checkRed = new ArrayList<>();

    public SquareAdapter(Context context, List<Integer> colors, String[][] chessBoardState, List<Integer> validMoves) {
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

        String piece = chessBoardState[row][col];

        ImageView squareImage = view.findViewById(R.id.square_image);
        View validMoveIndicator = view.findViewById(R.id.validMoveIndicator);
        View checkBackground = view.findViewById(R.id.checkBackground);

        if (piece.isEmpty()) {
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

    private int getDrawableResourceForPiece(String piece) {
        return switch (piece) {
            case "P": yield R.drawable.wp;
            case "p": yield R.drawable.bp;
            case "R": yield R.drawable.wr;
            case "r": yield R.drawable.br;
            case "N": yield R.drawable.wn;
            case "n": yield R.drawable.bn;
            case "B": yield R.drawable.wb;
            case "b": yield R.drawable.bb;
            case "Q": yield R.drawable.wq;
            case "q": yield R.drawable.bq;
            case "K": yield R.drawable.wk;
            case "k": yield R.drawable.bk;
            default: yield 0;
        };
    }

    public void updateChessBoardState(String[][] newChessBoardState) {
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