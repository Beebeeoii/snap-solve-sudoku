package com.beebeeoii.snapsolvesudoku;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class Help extends AppCompatActivity {

    Button backButton;

    private LinearLayout tutorial;

    private final String FIRST_RUN = "firstrun";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        backButton = (Button) findViewById(R.id.help_back);
        tutorial = (LinearLayout) findViewById(R.id.help_show_tutorial);

        tutorial.setOnTouchListener((View v, MotionEvent event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setBackgroundColor(Color.LTGRAY);
                    break;
                case MotionEvent.ACTION_UP:
                    v.setBackgroundColor(Color.WHITE);
                    break;
            }

            return false;
        });

        tutorial.setOnClickListener((View v) -> {
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
            sharedPreferences.edit().putBoolean(FIRST_RUN, true).commit();
            Intent home = new Intent(this, MainActivity.class);
            startActivity(home);
        });

        backButton.setOnClickListener((View v) -> onBackPressed());

    }
}
