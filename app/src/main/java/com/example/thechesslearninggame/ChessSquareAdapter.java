package com.example.thechesslearninggame;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

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

            // Dynamically set the size of the square to match the screen width
            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            int squareSize = screenWidth / 8; // Divide the screen width into 8 equal parts
            square.setLayoutParams(new GridView.LayoutParams(squareSize, squareSize));
        }

        square.setBackgroundColor(colors.get(position));

        // Calculate row and column from position
        int row = position / 8;
        int col = position % 8;

        // Get the piece at this position
        String piece = chessBoardState[row][col];

        // Set the piece text
        TextView squareText = square.findViewById(R.id.square_text);
        squareText.setText(piece);

        return square;
    }

    // Update the chessboard state
    public void updateChessBoardState(String[][] newChessBoardState) {
        this.chessBoardState = newChessBoardState;
        notifyDataSetChanged(); // Refresh the adapter
    }
}