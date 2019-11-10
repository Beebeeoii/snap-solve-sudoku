package com.beebeeoii.snapsolvesudoku;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HistoryDetail extends AppCompatActivity {

    TextView title;
    Button backButton;
    ImageView boardView;
    TextView noOfSols, noOfHints;
    RecyclerView recyclerView;
    DetailsRAdapter detailsRAdapter;
    List<DetailBoards> boardData;

    public static char[][] boardArray = new char[9][9];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);

        title = (TextView) findViewById(R.id.history_detail_title);
        backButton = (Button) findViewById(R.id.history_detail_back);
        boardView = (ImageView) findViewById(R.id.historyDetailsBoard);
        noOfSols = (TextView) findViewById(R.id.historyDetailNoSols);
        noOfHints = (TextView) findViewById(R.id.historyDetailNoHints);
        recyclerView = (RecyclerView) findViewById(R.id.historyDetailsRV);
        boardData = new ArrayList<>();

        String boardString = getIntent().getStringExtra("board");
        String dateDay = getIntent().getStringExtra("dateDay");
        int noSols = getIntent().getIntExtra("noSols", 0);
        byte[] boardBitmapByteArray = getIntent().getByteArrayExtra("boardBitmapByteArray");
        Bitmap boardBitmap = BitmapFactory.decodeByteArray(boardBitmapByteArray, 0, boardBitmapByteArray.length);

        int noHints = boardString.replace("0", "").length();

        //convert boardString to boardArray to find positions of hints
        for (int counter = 0; counter < 9; counter++) {
            boardString.getChars(counter * 9, (counter * 9) + 9, boardArray[counter], 0);
        }

        final String SOL_DIR = getExternalFilesDir(null).toString() + "/scanned_boards_solutions";
        File file = new File(SOL_DIR);
        File[] list = file.listFiles();

        for (File f : list) {
            String fileName = f.getName();
            String date = dateDay.substring(0, 6);
            String time = dateDay.substring(9, 13);

            if (fileName.contains(boardString) && fileName.contains(date) && fileName.contains(time)) {
                StringBuilder solution = new StringBuilder();

                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        solution.append(line);
                    }
                    bufferedReader.close();
                }
                catch (IOException e) {
                    //You'll need to add proper error handling here
                }

                String[] solutions = solution.toString().split(" ");

                for (String sol : solutions) {
                    char[][] solChar = new char[9][9];
                    for (int counter = 0; counter < 9; counter++) {
                        sol.getChars(counter * 9, (counter * 9) + 9, solChar[counter], 0);
                    }

                    boardData.add(new DetailBoards(solChar)); //noOfSOlutions, date, bitmap, board
                }

            }

        }

        detailsRAdapter = new DetailsRAdapter(getApplicationContext(), boardData);
        recyclerView.setAdapter(detailsRAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        boardView.setImageBitmap(boardBitmap);
        noOfSols.setText(String.valueOf(noSols));
        noOfHints.setText(String.valueOf(noHints));

        title.setText(dateDay);

        backButton.setOnClickListener((View v) -> {
            onBackPressed();
        });
    }
}
