package com.beebeeoii.snapsolvesudoku;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.CardHolder> {

    Context context;
    List<CardItems> historyData;

    public RecyclerAdapter(Context context, List<CardItems> historyData) {
        this.context = context;
        this.historyData = historyData;
    }

    @NonNull
    @Override
    public CardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.history_card, parent, false);

        view.setOnClickListener((View v) -> {

            TextView dateDayTv = v.findViewById(R.id.card_date);
            String dateDay = dateDayTv.getText().toString();

            TextView boardTv = v.findViewById(R.id.card_board);
            String board = boardTv.getText().toString();

            TextView noSolsTv = v.findViewById(R.id.card_noSols);
            int noSols = Integer.parseInt(noSolsTv.getText().toString());

            //Convert to byte array
            ImageView boardBitmapIv = v.findViewById(R.id.card_image);
            Bitmap boardBitmap = ((BitmapDrawable) boardBitmapIv.getDrawable()).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            boardBitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
            byte[] byteArray = stream.toByteArray();

            Intent cardDetails = new Intent(context, HistoryDetail.class);

            cardDetails.putExtra("dateDay", dateDay);
            cardDetails.putExtra("board", board);
            cardDetails.putExtra("noSols", noSols);
            cardDetails.putExtra("boardBitmapByteArray", byteArray);
            cardDetails.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(cardDetails);

        });

        return new CardHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardHolder holder, int position) {
        holder.rawBoardIv.setImageBitmap(historyData.get(position).getHistoryPic());
        holder.noSolsTV.setText(historyData.get(position).getHistoryNoSols() + "");
        holder.dateTv.setText(historyData.get(position).getHistoryDate());
        holder.boardTv.setText(historyData.get(position).getHistoryBoard());
    }

    @Override
    public int getItemCount() {
        return historyData.size();
    }

    public class CardHolder extends RecyclerView.ViewHolder {
        TextView dateTv, noSolsTV, boardTv;
        ImageView rawBoardIv;

        public CardHolder(@NonNull View itemView) {
            super(itemView);
            dateTv = itemView.findViewById(R.id.card_date);
            noSolsTV = itemView.findViewById(R.id.card_noSols);
            rawBoardIv = itemView.findViewById(R.id.card_image);
            boardTv = itemView.findViewById(R.id.card_board);
        }
    }

}
