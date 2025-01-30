package com.example.thechesslearninggame;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;

public class ChessSquareAdapter extends BaseAdapter {
    private final List<Integer> colors;
    private final Context context;
    private String[][] chessBoardState;

    public ChessSquareAdapter(Context context, List<Integer> colors, String[][] chessBoardState) {
        this.context = context;
        this.colors = colors;
        this.chessBoardState = chessBoardState;
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
        View square = convertView;
        if (square == null) {
            square = android.view.LayoutInflater.from(context).inflate(R.layout.square, parent, false);

            // Calculate the size of each square dynamically
            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            int squareSize = screenWidth / 8; // Divide the screen width into 8 equal parts

            // Set the size of the square
            square.setLayoutParams(new GridView.LayoutParams(squareSize, squareSize));
        }
        square.setBackgroundColor(colors.get(position));

        // Calculate row and column from position
        int row = position / 8;
        int col = position % 8;

        // Get the piece at this position
        String piece = chessBoardState[row][col];

        // Set the piece image
        ImageView squareImage = square.findViewById(R.id.square_image);
        if (!piece.isEmpty()) {
            // Get the drawable resource ID for the piece
            int resId = getDrawableResourceForPiece(piece);
            squareImage.setImageResource(resId);
        } else {
            // Clear the image if the square is empty
            squareImage.setImageDrawable(null);
        }

        return square;
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

    // Update the chessboard state
    public void updateChessBoardState(String[][] newChessBoardState) {
        this.chessBoardState = newChessBoardState;
        notifyDataSetChanged(); // Refresh the adapter
    }
}