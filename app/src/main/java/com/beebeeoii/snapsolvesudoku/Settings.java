package com.beebeeoii.snapsolvesudoku;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class Settings extends AppCompatActivity {

    Button backButton;
    LinearLayout help, about, changelog;
    ConstraintLayout minHints, maxSols;
    ScrollView scrollView;

    SharedPreferences sharedPreferences;
    final private String KEY_MAX_SOLS = "MAX_SOLS";
    final private String KEY_MIN_HINTS = "MIN_HINTS";
    final private int MAX_SOLS = 500;
    final private int MIN_HINTS = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAnimation();
        setContentView(R.layout.activity_settings);

        backButton = (Button) findViewById(R.id.settings_back);
        help = (LinearLayout) findViewById(R.id.settings_help);
        about = (LinearLayout) findViewById(R.id.settings_about);
        changelog = (LinearLayout) findViewById(R.id.settings_changelog);
        minHints = (ConstraintLayout) findViewById(R.id.settings_min_hints);
        maxSols = (ConstraintLayout) findViewById(R.id.settings_max_sols);
        scrollView = (ScrollView) findViewById(R.id.settings_scroll_view);

        sharedPreferences = getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        scrollView.setOnTouchListener((View v, MotionEvent event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    minHints.setBackgroundColor(Color.WHITE);
                    maxSols.setBackgroundColor(Color.WHITE);
                    help.setBackgroundColor(Color.WHITE);
                    about.setBackgroundColor(Color.WHITE);
                    changelog.setBackgroundColor(Color.WHITE);
                    break;
            }
            return false;

        });

        backButton.setOnClickListener((View v) -> onBackPressed());

        minHints.setOnTouchListener((View v, MotionEvent event) -> {
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

        minHints.setOnClickListener((View v) -> {
            View dialogView = getLayoutInflater().inflate(R.layout.pref_sols_hint_dialog, null);
            TextView title = (TextView) dialogView.findViewById(R.id.pref_title);
            EditText input = (EditText) dialogView.findViewById(R.id.pref_edittext);
            Button cancel = (Button) dialogView.findViewById(R.id.pref_cancel);
            Button save = (Button) dialogView.findViewById(R.id.pref_save);

            int hints = sharedPreferences.getInt(KEY_MIN_HINTS,MIN_HINTS);

            title.setText("Hints required for valid board:");
            input.setText(String.valueOf(hints));
            input.setHint("Number of Hints");

            Dialog dialog = new Dialog(this);
            dialog.setContentView(dialogView);
            dialog.show();

            cancel.setOnClickListener((View v1) -> dialog.cancel());
            save.setOnClickListener((View v2) -> {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(KEY_MIN_HINTS, Integer.parseInt(input.getText().toString()));
                editor.commit();
                dialog.cancel();
                Toast.makeText(this, "[Minimum Hints] - " + sharedPreferences.getInt(KEY_MIN_HINTS,MIN_HINTS), Toast.LENGTH_SHORT).show();
            });

        });

        maxSols.setOnTouchListener((View v, MotionEvent event) -> {
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

        maxSols.setOnClickListener((View v) -> {
            View dialogView = getLayoutInflater().inflate(R.layout.pref_sols_hint_dialog, null);
            TextView title = (TextView) dialogView.findViewById(R.id.pref_title);
            EditText input = (EditText) dialogView.findViewById(R.id.pref_edittext);
            Button cancel = (Button) dialogView.findViewById(R.id.pref_cancel);
            Button save = (Button) dialogView.findViewById(R.id.pref_save);

            int sols = sharedPreferences.getInt(KEY_MAX_SOLS,MAX_SOLS);

            title.setText("Maximum possible solutions to be found:");
            input.setText(String.valueOf(sols));
            input.setHint("Number of Solutions");

            Dialog dialog = new Dialog(this);
            dialog.setContentView(dialogView);
            dialog.show();

            cancel.setOnClickListener((View v1) -> dialog.cancel());
            save.setOnClickListener((View v2) -> {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(KEY_MAX_SOLS, Integer.parseInt(input.getText().toString()));
                editor.commit();
                dialog.cancel();
                Toast.makeText(this, "[Maximum Solutions] - " + sharedPreferences.getInt(KEY_MAX_SOLS,MAX_SOLS), Toast.LENGTH_SHORT).show();
            });
        });

        help.setOnTouchListener((View v, MotionEvent event) -> {
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

        help.setOnClickListener((View v) -> {
            v.setBackgroundColor(Color.WHITE);

            Intent help = new Intent(this, Help.class);
            startActivity(help);
        });

        about.setOnTouchListener((View v, MotionEvent event) -> {
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

        about.setOnClickListener((View v) -> {
            v.setBackgroundColor(Color.WHITE);
            Intent about = new Intent(this, About.class);
            startActivity(about);
        });

        changelog.setOnTouchListener((View v, MotionEvent event) -> {
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

        changelog.setOnClickListener((View v) -> {
            v.setBackgroundColor(Color.WHITE);

            // using BottomSheetDialog
            View dialogView = getLayoutInflater().inflate(R.layout.changelog, null);
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            dialog.setContentView(dialogView);
            dialog.setCanceledOnTouchOutside(true);

            dialog.show();
        });
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