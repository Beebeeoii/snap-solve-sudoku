package com.beebeeoii.snapsolvesudoku;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class About extends AppCompatActivity {

    Button backButton;
    LinearLayout donate, rate, contact, osl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        backButton = (Button) findViewById(R.id.about_back);
        donate = (LinearLayout) findViewById(R.id.about_donate);
        rate = (LinearLayout) findViewById(R.id.about_rate);
        contact = (LinearLayout) findViewById(R.id.about_contact);
        osl = (LinearLayout) findViewById(R.id.about_osl);

        donate.setOnTouchListener((View v, MotionEvent event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setBackgroundColor(getResources().getColor(R.color.light_purple));
                    break;
                case MotionEvent.ACTION_UP:
                    v.setBackgroundColor(getResources().getColor(R.color.white));
                    break;
            }

            return false;
        });

        donate.setOnClickListener((View v) -> {
            v.setBackgroundColor(getResources().getColor(R.color.white));

            Intent donate = new Intent(this, Donate.class);
            startActivity(donate);
        });

        rate.setOnTouchListener((View v, MotionEvent event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setBackgroundColor(getResources().getColor(R.color.light_purple));
                    break;
                case MotionEvent.ACTION_UP:
                    v.setBackgroundColor(getResources().getColor(R.color.white));
                    break;
            }

            return false;
        });

        rate.setOnClickListener((View v) -> {
            v.setBackgroundColor(getResources().getColor(R.color.white));

            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
            }
        });

        contact.setOnTouchListener((View v, MotionEvent event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setBackgroundColor(getResources().getColor(R.color.light_purple));
                    break;
                case MotionEvent.ACTION_UP:
                    v.setBackgroundColor(getResources().getColor(R.color.white));
                    break;
            }

            return false;
        });

        contact.setOnClickListener((View v) -> {
            v.setBackgroundColor(getResources().getColor(R.color.white));

            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"b33b33o11@gmail.com"});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "[Snap Solve Sudoku]");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "[Feedback regarding the app]");
            emailIntent.setType("message/rfc822");

            if (emailIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(Intent.createChooser(emailIntent, "Choose your preferred client:"));
            }
        });

        osl.setOnTouchListener((View v, MotionEvent event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setBackgroundColor(getResources().getColor(R.color.light_purple));
                    break;
                case MotionEvent.ACTION_UP:
                    v.setBackgroundColor(getResources().getColor(R.color.white));
                    break;
            }

            return false;
        });

        osl.setOnClickListener((View v) -> {
            v.setBackgroundColor(getResources().getColor(R.color.white));

            // using BottomSheetDialog
            View dialogView = getLayoutInflater().inflate(R.layout.osl, null);
            TextView librariesTv = (TextView) dialogView.findViewById(R.id.osl_libraries);
            TextView iconTv = (TextView) dialogView.findViewById(R.id.osl_icons);
            TextView testerTv = (TextView) dialogView.findViewById(R.id.osl_design);

            int flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
            SpannableString duonavigation = new SpannableString("Duo Navigation Drawer - Alexander Pot\n");
            SpannableString smarttablayout = new SpannableString("SmartTabLayout - ogaclejapan\n");
            SpannableString slidingpanel = new SpannableString("Android Sliding Up Panel - umano\n");
            SpannableString androidviewanimations = new SpannableString("Android View Animations - Dai Ma Jia\n");
            SpannableString inappbilling = new SpannableString("Android In-App Billing v3 Library - AnjLab\n");
            SpannableString opencv = new SpannableString("OpenCV - OpenCV Team\n");
            SpannableString tess = new SpannableString("tess-two - Robert Theis\n");
            SpannableString tourguide = new SpannableString("TapTargetView - KeepSafe Team");

            duonavigation.setSpan(new URLSpan("https://github.com/PSD-Company/duo-navigation-drawer"), 0, 21, flag);
            smarttablayout.setSpan(new URLSpan("https://github.com/ogaclejapan/SmartTabLayout"), 0, 14, flag);
            slidingpanel.setSpan(new URLSpan("https://github.com/umano/AndroidSlidingUpPanel"),0, 24, flag);
            androidviewanimations.setSpan(new URLSpan("https://github.com/daimajia/AndroidViewAnimations"), 0, 23, flag);
            inappbilling.setSpan(new URLSpan("https://github.com/anjlab/android-inapp-billing-v3"), 0, 33, flag);
            opencv.setSpan(new URLSpan("https://opencv.org/"), 0, 6, flag);
            tess.setSpan(new URLSpan("https://github.com/rmtheis/tess-two"), 0, 8, flag);
            tourguide.setSpan(new URLSpan("https://github.com/KeepSafe/TapTargetView"), 0, 13, flag);

            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(duonavigation);
            builder.append(smarttablayout);
            builder.append(slidingpanel);
            builder.append(androidviewanimations);
            builder.append(inappbilling);
            builder.append(opencv);
            builder.append(tess);
            builder.append(tourguide);

            SpannableString icon = new SpannableString("Icons - Noun Project and Material Resources");

            icon.setSpan(new URLSpan("https://thenounproject.com/"), 8, 20, flag);
            icon.setSpan(new URLSpan("https://material.io/design/"), 25, 43, flag);

            SpannableStringBuilder builder1 = new SpannableStringBuilder();
            builder1.append(icon);

            SpannableString tester = new SpannableString("KG and Tim");

            librariesTv.setText(builder);
            librariesTv.setMovementMethod(LinkMovementMethod.getInstance());

            iconTv.setText(builder1);
            iconTv.setMovementMethod(LinkMovementMethod.getInstance());

            testerTv.setText(tester);

            BottomSheetDialog dialog = new BottomSheetDialog(this);
            dialog.setContentView(dialogView);
            dialog.setCanceledOnTouchOutside(true);

            dialog.show();
        });

        backButton.setOnClickListener((View v) -> onBackPressed());

    }
}
