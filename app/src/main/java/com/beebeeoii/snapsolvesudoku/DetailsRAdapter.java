package com.beebeeoii.snapsolvesudoku;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import static com.beebeeoii.snapsolvesudoku.HistoryDetail.boardArray;

public class DetailsRAdapter extends RecyclerView.Adapter<DetailsRAdapter.SudokuBoard> {

    Context context;
    List<DetailBoards> boardData;

    public DetailsRAdapter(Context context, List<DetailBoards> boardData) {
        this.context = context;
        this.boardData = boardData;
    }

    @NonNull
    @Override
    public SudokuBoard onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.sudoku_board, parent, false);

        return new SudokuBoard(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SudokuBoard holder, int position) {
        for (int row = 0; row < 9; row ++) {
            for (int col = 0; col < 9; col ++) {
                holder.tvArray[row][col].setText(boardData.get(position).getBoard()[row][col] + "");
                holder.tvArray[row][col].setClickable(false);

                if (boardArray[row][col] != '0') {
                    holder.tvArray[row][col].setTypeface(null, Typeface.BOLD);
                    holder.tvArray[row][col].setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
                    holder.tvArray[row][col].setTextColor(Color.DKGRAY);
                }
            }
        }

    }

    @Override
    public int getItemCount() {
        return boardData.size();
    }

    public class SudokuBoard extends RecyclerView.ViewHolder {

        TextView[][] tvArray = new TextView[9][9];

        public SudokuBoard(@NonNull View itemView) {

            super(itemView);

            for (int row = 0; row < 9; row ++) {
                for (int column = 0; column < 9; column ++) {
                    String textViewID = "g" + row + column;
                    int resID = context.getApplicationContext().getResources().getIdentifier(textViewID, "id", context.getApplicationContext().getPackageName());
                    Log.wtf("NAME", resID + "");
                    tvArray[row][column] = (TextView) itemView.findViewById(resID);
                }
            }
        }
    }
}
