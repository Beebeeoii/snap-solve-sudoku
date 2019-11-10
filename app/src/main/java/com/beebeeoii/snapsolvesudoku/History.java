package com.beebeeoii.snapsolvesudoku;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class History extends AppCompatActivity {

    Toolbar toolbar;
    Button backButton;

    RecyclerView historyRecycler;
    RecyclerAdapter historyAdapter;
    List<CardItems> historyData;

    ConstraintLayout historyNoHistoryLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAnimation();
        setContentView(R.layout.activity_history);

        final String IMAGE_DIR = getExternalFilesDir(null).toString() + "/scanned_boards";
        final String SOL_DIR = getExternalFilesDir(null).toString() + "/scanned_boards_solutions";

        final File boardDir = new File(IMAGE_DIR);
        final File solDir = new File(SOL_DIR);
        if (!boardDir.exists()) {
            boardDir.mkdirs();
        }

        if (!solDir.exists()) {
            solDir.mkdirs();
        }

        toolbar = (Toolbar) findViewById(R.id.history_toolbar);
        backButton = (Button) findViewById(R.id.history_back);
        historyRecycler = (RecyclerView) findViewById(R.id.history_recycler);
        historyNoHistoryLayout = (ConstraintLayout) findViewById(R.id.history_no_history_layout);
        historyData = new ArrayList<>();


        Thread thread = new Thread(() -> {
            File[] list = boardDir.listFiles();
            String[] info;
            Bitmap boardPic;

            if (list.length == 0) {
                historyNoHistoryLayout.setVisibility(View.VISIBLE);
                historyRecycler.setVisibility(View.GONE);
                return;
            } else {
                historyNoHistoryLayout.setVisibility(View.GONE);
                historyRecycler.setVisibility(View.VISIBLE);
            }

            for (File f : list) {
                info = f.getName().substring(0, f.getName().length() - 4).split("_");
                boardPic = BitmapFactory.decodeFile(f.getAbsolutePath());
                String dateDay = info[0] + " - " + info[1].substring(0, 4) + ", " + info[2].substring(0, 3); //date format: "210919 - 1507, Thu"
                historyData.add(new CardItems(Integer.parseInt(info[4]), dateDay, boardPic, info[3])); //noOfSOlutions, date, bitmap, board
            }

            historyAdapter = new RecyclerAdapter(getApplicationContext(), historyData);

            runOnUiThread(() -> {
                historyRecycler.setAdapter(historyAdapter);
                historyRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                DividerItemDecoration itemDecor = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
                historyRecycler.addItemDecoration(itemDecor);
            });
        });

        thread.start();

        backButton.setOnClickListener((View v) -> onBackPressed());
    }

    private void setAnimation() {
        if(Build.VERSION.SDK_INT>20) {
            Slide slide = new Slide();
            slide.setSlideEdge(Gravity.LEFT);
            slide.setDuration(400);
            slide.setInterpolator(new DecelerateInterpolator());
            getWindow().setExitTransition(slide);
            getWindow().setEnterTransition(slide);
        }
    }
}
