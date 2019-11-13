package com.beebeeoii.snapsolvesudoku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.CameraX;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import nl.psdcompany.duonavigationdrawer.views.DuoDrawerLayout;
import nl.psdcompany.duonavigationdrawer.widgets.DuoDrawerToggle;

import static com.beebeeoii.snapsolvesudoku.Manual.hideSnackbar;
import static com.beebeeoii.snapsolvesudoku.Manual.isBoardSolved;
import static com.beebeeoii.snapsolvesudoku.Manual.isSnackbarShown;
import static com.beebeeoii.snapsolvesudoku.OCR.board;
import static com.beebeeoii.snapsolvesudoku.OCR.boardProcessed;

public class MainActivity extends AppCompatActivity {

    private final String FILE_NAME = "eng.traineddata";
    private final String[] CAMERA_STORAGE_PERM = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private final int CAMERA_STORAGE_PERM_CODE = 0;
    private final String[] CAMERA_PERM = {Manifest.permission.CAMERA};
    private final int CAMERA_CODE = 1;
    private final String[] STORAGE_PERM = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private final int STORAGE_CODE = 2;
    private final String FIRST_RUN = "firstrun";

    DuoDrawerLayout drawerLayout;
    DuoDrawerToggle drawerToggle;
    Toolbar toolbar;

    SmartTabLayout viewPagerTab;
    ViewPager viewPager;

    Fragment ocr, manual;

    Bitmap original = null;

    SharedPreferences sharedPreferences;

    private int backPress = 0;

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPagerTab = (SmartTabLayout) findViewById(R.id.tab_layout);
        drawerLayout = (DuoDrawerLayout) findViewById(R.id.drawer);

        sharedPreferences = getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        drawerToggle = new DuoDrawerToggle(this, drawerLayout, toolbar,
                R.string.drawer_open,
                R.string.drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add(R.string.fragment_ocr, OCR.class)
                .add(R.string.fragment_manual, Manual.class)
                .create());

        viewPager.setAdapter(adapter);
        viewPagerTab.setViewPager(viewPager);

        viewPagerTab.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                ocr = adapter.getPage(0);
                manual = adapter.getPage(1);
                Log.wtf("TEST","TEST");

                if (sharedPreferences.getBoolean(FIRST_RUN, true)) {
                    tutorial();
                    sharedPreferences.edit().putBoolean(FIRST_RUN, false).commit();
                }

                if (manual.getView().findViewById(R.id.solve).getBackground().getConstantState() == getResources().getDrawable(R.drawable.solve_selected_background, null).getConstantState()) {
                    manual.getView().findViewById(R.id.solve).setBackgroundResource(R.drawable.solve_background);
                    ((Button) manual.getView().findViewById(R.id.solve)).setTextColor(Color.BLACK);
                }

                if (manual.getView().findViewById(R.id.clear_all).getBackground().getConstantState() == getResources().getDrawable(R.drawable.clear_selected_background, null).getConstantState()) {
                    manual.getView().findViewById(R.id.clear_all).setBackgroundResource(R.drawable.clear_background);
                    ((Button) manual.getView().findViewById(R.id.clear_all)).setTextColor(Color.BLACK);
                }

                if (ocr.getView().findViewById(R.id.capture_button).getAlpha() == 0.5f) {
                    ocr.getView().findViewById(R.id.capture_button).setAlpha(1.0f);
                }

                if (manual.getView().findViewById(R.id.b0).getBackground().getConstantState() == getResources().getDrawable(R.drawable.input_button_selected, null).getConstantState()) {
                    manual.getView().findViewById(R.id.b0).setBackgroundResource(R.drawable.input_button);
                    ((Button) manual.getView().findViewById(R.id.b0)).setTextColor(Color.BLACK);
                }

                if (manual.getView().findViewById(R.id.b1).getBackground().getConstantState() == getResources().getDrawable(R.drawable.input_button_selected, null).getConstantState()) {
                    manual.getView().findViewById(R.id.b1).setBackgroundResource(R.drawable.input_button);
                    ((Button) manual.getView().findViewById(R.id.b1)).setTextColor(Color.BLACK);
                }

                if (manual.getView().findViewById(R.id.b2).getBackground().getConstantState() == getResources().getDrawable(R.drawable.input_button_selected, null).getConstantState()) {
                    manual.getView().findViewById(R.id.b2).setBackgroundResource(R.drawable.input_button);
                    ((Button) manual.getView().findViewById(R.id.b2)).setTextColor(Color.BLACK);
                }

                if (manual.getView().findViewById(R.id.b3).getBackground().getConstantState() == getResources().getDrawable(R.drawable.input_button_selected, null).getConstantState()) {
                    manual.getView().findViewById(R.id.b3).setBackgroundResource(R.drawable.input_button);
                    ((Button) manual.getView().findViewById(R.id.b3)).setTextColor(Color.BLACK);
                }

                if (manual.getView().findViewById(R.id.b4).getBackground().getConstantState() == getResources().getDrawable(R.drawable.input_button_selected, null).getConstantState()) {
                    manual.getView().findViewById(R.id.b4).setBackgroundResource(R.drawable.input_button);
                    ((Button) manual.getView().findViewById(R.id.b4)).setTextColor(Color.BLACK);
                }

                if (manual.getView().findViewById(R.id.b5).getBackground().getConstantState() == getResources().getDrawable(R.drawable.input_button_selected, null).getConstantState()) {
                    manual.getView().findViewById(R.id.b5).setBackgroundResource(R.drawable.input_button);
                    ((Button) manual.getView().findViewById(R.id.b5)).setTextColor(Color.BLACK);
                }

                if (manual.getView().findViewById(R.id.b6).getBackground().getConstantState() == getResources().getDrawable(R.drawable.input_button_selected, null).getConstantState()) {
                    manual.getView().findViewById(R.id.b6).setBackgroundResource(R.drawable.input_button);
                    ((Button) manual.getView().findViewById(R.id.b6)).setTextColor(Color.BLACK);
                }

                if (manual.getView().findViewById(R.id.b7).getBackground().getConstantState() == getResources().getDrawable(R.drawable.input_button_selected, null).getConstantState()) {
                    manual.getView().findViewById(R.id.b7).setBackgroundResource(R.drawable.input_button);
                    ((Button) manual.getView().findViewById(R.id.b7)).setTextColor(Color.BLACK);
                }

                if (manual.getView().findViewById(R.id.b8).getBackground().getConstantState() == getResources().getDrawable(R.drawable.input_button_selected, null).getConstantState()) {
                    manual.getView().findViewById(R.id.b8).setBackgroundResource(R.drawable.input_button);
                    ((Button) manual.getView().findViewById(R.id.b8)).setTextColor(Color.BLACK);
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    LinearLayout clearAllSolve = (LinearLayout) manual.getView().findViewById(R.id.manual_linear);
                    LinearLayout solNavigator = (LinearLayout) manual.getView().findViewById(R.id.manual_multi_sols);
                    LinearLayout saveBoardOption = (LinearLayout) manual.getView().findViewById(R.id.save_board_option);
                    ConstraintLayout imgButton = (ConstraintLayout) manual.getView().findViewById(R.id.manual_constraint);
                    ImageView boardPreview = (ImageView) manual.getView().findViewById(R.id.board_preview);
                    Button preSol = (Button) manual.getView().findViewById(R.id.previous_sol);
                    Button nextSol = (Button) manual.getView().findViewById(R.id.next_sol);
                    Button editBoard = (Button) manual.getView().findViewById(R.id.edit_board_button);

                    clearAllSolve.setVisibility(View.VISIBLE);
                    solNavigator.setVisibility(View.INVISIBLE);
                    saveBoardOption.setVisibility(View.INVISIBLE);
                    imgButton.setVisibility(View.VISIBLE);
                    boardPreview.setVisibility(View.GONE);
                    preSol.setVisibility(View.GONE);
                    nextSol.setVisibility(View.GONE);
                    editBoard.setVisibility(View.GONE);

                    TextView tvArray[][] = new TextView[9][9];
                    for (int row = 0; row < 9; row ++) {
                        for (int col = 0; col < 9; col ++) {
                            String textViewID = "g" + row + col;
                            int tvResID = getResources().getIdentifier(textViewID, "id", getPackageName());
                            tvArray[row][col] = (TextView) manual.getView().findViewById(tvResID);
                            tvArray[row][col].setText("");
                            tvArray[row][col].setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
                            tvArray[row][col].setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                            tvArray[row][col].setTextColor(Color.GRAY);
                            tvArray[row][col].setBackgroundResource(0);
                        }
                    }

                    isBoardSolved = false;
                    original = null;

                    if (isSnackbarShown()) {
                        hideSnackbar();
                    }

                }

                if (position == 1) {
                    if (boardProcessed != null) {
                        ImageView boardPreview = (ImageView) manual.getView().findViewById(R.id.board_preview);
                        boardPreview.setVisibility(View.VISIBLE);
                        boardPreview.setImageBitmap(boardProcessed);
                        TextView tvArray[][] = new TextView[9][9];
                        for (int row = 0; row < 9; row ++) {
                            for (int col = 0; col < 9; col ++) {
                                String textViewID = "g" + row + col;
                                int tvResID = getResources().getIdentifier(textViewID, "id", getPackageName());
                                tvArray[row][col] = (TextView) manual.getView().findViewById(tvResID);

                                if (board[row][col] != 0) {
                                    tvArray[row][col].setText(String.valueOf(board [row][col]));
                                    tvArray[row][col].setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                                    tvArray[row][col].setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
                                    tvArray[row][col].setTextColor(Color.DKGRAY);
                                }
                            }
                        }

                        boardProcessed = null;
                        board = new int[9][9];
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        toolbar.setNavigationOnClickListener((View v) -> {
            SlidingUpPanelLayout slidingUpPanelLayout = (SlidingUpPanelLayout) ocr.getView().findViewById(R.id.sliding_panel_layout);
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

            drawerLayout.openDrawer();

        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, baseLoaderCallback);
        } else {
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void go_home(View v) {
        drawerLayout.closeDrawer();
    }

    public void go_history(View v) {
        Intent history = new Intent(this, History.class);

        if(Build.VERSION.SDK_INT > 20) {
            ActivityOptions options =
                    ActivityOptions.makeSceneTransitionAnimation(this);
            startActivity(history, options.toBundle());
        } else {
            startActivity(history);
        }

    }

    public void go_settings(View v) {
        Intent settings = new Intent(this, Settings.class);
        if(Build.VERSION.SDK_INT > 20) {
            ActivityOptions options =
                    ActivityOptions.makeSceneTransitionAnimation(this);
            startActivity(settings, options.toBundle());
        } else {
            startActivity(settings);
        }
    }

    private boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAMERA_STORAGE_PERM_CODE:
                Map<String, Integer> perms = new HashMap<>();
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);

                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; ++ i) {
                        perms.put(permissions[i], grantResults[i]);
                    }
                }

                int write_perm = perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                int read_perm = perms.get(Manifest.permission.READ_EXTERNAL_STORAGE);
                int cam_perm = perms.get(Manifest.permission.CAMERA);

                if (write_perm == PackageManager.PERMISSION_GRANTED && (read_perm == PackageManager.PERMISSION_GRANTED && cam_perm == PackageManager.PERMISSION_GRANTED)) {
                    copyFile();
                    startCamera();
                } else if (cam_perm == PackageManager.PERMISSION_GRANTED && (write_perm == PackageManager.PERMISSION_DENIED && read_perm == PackageManager.PERMISSION_DENIED)) {
                    startCamera();

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        showRationale("Read Write Access","Read Write Access is required for\n    - OCR board recognition\n    - Saving of boards to history", (DialogInterface dialog, int which) -> {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    if(!hasPermissions(this.getApplicationContext(), STORAGE_PERM)){
                                        ActivityCompat.requestPermissions(this, STORAGE_PERM, STORAGE_CODE);
                                    }
                                    break;
                            }
                        });
                    }
                } else if (cam_perm == PackageManager.PERMISSION_DENIED && (write_perm == PackageManager.PERMISSION_GRANTED && read_perm == PackageManager.PERMISSION_GRANTED)) {
                    copyFile();

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                        showRationale("Camera Access","Camera Access is required for\n    - OCR board recognition", (DialogInterface dialog, int which) -> {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    if(!hasPermissions(this.getApplicationContext(), CAMERA_PERM)){
                                        ActivityCompat.requestPermissions(this, CAMERA_PERM, CAMERA_CODE);
                                    }
                                    break;
                            }
                        });
                    }
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                        showRationale("Camera Access","Camera Access is required for\n    - OCR board recognition", (DialogInterface dialog, int which) -> {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    if(!hasPermissions(this.getApplicationContext(), CAMERA_PERM)){
                                        ActivityCompat.requestPermissions(this, CAMERA_PERM, CAMERA_CODE);
                                    }
                                    break;
                            }
                        });
                    }

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        showRationale("Read Write Access","Read Write Access is required for\n    - OCR board recognition\n    - Saving of boards to history", (DialogInterface dialog, int which) -> {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    if(!hasPermissions(this.getApplicationContext(), STORAGE_PERM)){
                                        ActivityCompat.requestPermissions(this, STORAGE_PERM, STORAGE_CODE);
                                    }
                                    break;
                            }
                        });
                    }
                }

                break;
            case CAMERA_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera();
                } else {
                    showRationale("Camera Access","OCR function will not work", null);
                }
                break;

            case STORAGE_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    copyFile();
                } else {
                    showRationale("Read Write Access","OCR and history functions will not work", null);
                }
                break;
        }

    }

    public void copyFile() {
        try {
//            File dir = new File(Environment.getExternalStorageDirectory().toString() + "/tessdata");
            File dir = new File(getExternalFilesDir(null).toString() + "/tessdata");

            if (!dir.exists()) {
                dir.mkdir();
            }

            String pathToDataFile = getExternalFilesDir(null).toString() + "/tessdata" + "/eng.traineddata";
            if (!(new File(pathToDataFile)).exists()) {
                InputStream in = getAssets().open(FILE_NAME);
                OutputStream out = new FileOutputStream(pathToDataFile);
                byte[] buffer = new byte[1024];
                int read = in.read(buffer);
                while (read != -1) {
                    out.write(buffer, 0, read);
                    read = in.read(buffer);
                }
                in.close();
                out.flush();
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showRationale(String title, String message, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(message)
                .setTitle(title)
                .setPositiveButton("I understand", listener)
                .create()
                .show();
    }

    @Override
    public void onBackPressed() {

        switch (viewPager.getCurrentItem()) {
            case 0:
                backPress ++;

                if (backPress == 2) {
                    backPress = 0;
                    super.onBackPressed();
                } else if (backPress == 1) {
                    Toast.makeText(getApplicationContext(), "Press back once more to exit", Toast.LENGTH_SHORT).show();
                    Handler handler = new Handler();
                    handler.postDelayed(() -> backPress = 0, 3000);
                }

                break;
            case 1:
                viewPager.setCurrentItem(0, true);
                break;
        }
    }

    private void tutorial() {
        TapTargetSequence sequence = new TapTargetSequence(this)
                .targets(
                        TapTarget.forView(ocr.getView().findViewById(R.id.view_finder), "So You Have A Board.", "You Place It Here.")
                            .outerCircleColor(R.color.dark_purple)
                            .titleTextSize(24)
                            .transparentTarget(true)
                            .targetRadius(ocr.getView().findViewById(R.id.view_finder).getMeasuredWidth() / 4)
                            .cancelable(false)
                            .id(1),
                        TapTarget.forView(ocr.getView().findViewById(R.id.capture_button), "And You Simply Snap.")
                            .outerCircleColor(R.color.dark_purple)
                            .titleTextSize(24)
                            .transparentTarget(true)
                            .tintTarget(true)
                            .cancelable(false)
                            .id(2),
                        TapTarget.forView(manual.getView().findViewById(R.id.sudoku_board), "The Board Fills up.", "Automatically.")
                            .outerCircleColor(R.color.dark_purple)
                            .titleTextSize(24)
                            .transparentTarget(true)
                            .targetRadius(manual.getView().findViewById(R.id.sudoku_board).getWidth() / 4)
                            .cancelable(false)
                            .id(3),
                        TapTarget.forView(manual.getView().findViewById(getResources().getIdentifier("g87", "id", getPackageName())), "Wrongly Inputted?", "Hold to Clear.")
                            .outerCircleColor(R.color.dark_purple)
                            .titleTextSize(24)
                            .transparentTarget(true)
                            .cancelable(false)
                            .id(4),
                        TapTarget.forView(manual.getView().findViewById(R.id.clear_all), "Clear All Maybe?", "Just One Click. Or Swipe Right. But Let's Not Do That Now")
                            .outerCircleColor(R.color.dark_purple)
                            .titleTextSize(24)
                            .transparentTarget(true)
                            .cancelable(false)
                            .id(5),
                        TapTarget.forView(manual.getView().findViewById(R.id.solve), "Let's Solve.", "As Easy As That.")
                            .outerCircleColor(R.color.dark_purple)
                            .titleTextSize(24)
                            .transparentTarget(true)
                            .cancelable(false)
                            .id(6),
                        TapTarget.forBounds(new Rect(getResources().getDisplayMetrics().widthPixels,getResources().getDisplayMetrics().heightPixels,getResources().getDisplayMetrics().widthPixels,getResources().getDisplayMetrics().heightPixels), "That's It.", "Enjoy :)")
                            .outerCircleColor(R.color.dark_purple)
                            .titleTextSize(24)
                            .outerCircleAlpha(1.0f)
                            .cancelable(false)
                            .id(7)
                ).listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {

                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                        switch (lastTarget.id()) {
                            case 2:
                                viewPager.setCurrentItem(1, true);

                                TextView[][] tvArray = new TextView[9][9];
                                //linking tv
                                for (int row = 0; row < 9; row ++) {
                                    for (int col = 0; col < 9; col ++) {
                                        String textViewID = "g" + row + col;
                                        int tvResID = getResources().getIdentifier(textViewID, "id", getPackageName());
                                        tvArray[row][col] = (TextView) manual.getView().findViewById(tvResID);
                                    }
                                }
                                String exBoard = "876900000010006000040305800400000210090500000050040306029000008004690173000001074";

                                final char[][] array = new char[9][9];
                                for (int counter = 0; counter < 9; counter++) {
                                    exBoard.getChars(counter * 9, (counter * 9) + 9, array[counter], 0);
                                }

                                for (int row = 0; row < 9; row ++) {
                                    for (int col = 0; col < 9; col ++) {
                                        if (array[row][col] != '0') {
                                            tvArray[row][col].setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                                            tvArray[row][col].setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
                                            tvArray[row][col].setTextColor(Color.DKGRAY);
                                            tvArray[row][col].setText(String.valueOf(array[row][col]));
                                        }
                                    }
                                }

                                tvArray[7][7].setBackgroundResource(R.drawable.grid_wrong);
                                tvArray[8][7].setBackgroundResource(R.drawable.grid_wrong);

                                break;
                            case 4:
                                TextView tv = (TextView) manual.getView().findViewById(getResources().getIdentifier("g87", "id", getPackageName()));
                                tv.setText("");
                                tv.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
                                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                                tv.setTextColor(Color.GRAY);
                                tv.setBackgroundResource(0);
                                break;
                            case 6:
                                TextView[][] tvArray1 = new TextView[9][9];
                                //linking tv
                                for (int row = 0; row < 9; row ++) {
                                    for (int col = 0; col < 9; col ++) {
                                        String textViewID = "g" + row + col;
                                        int tvResID = getResources().getIdentifier(textViewID, "id", getPackageName());
                                        tvArray1[row][col] = (TextView) manual.getView().findViewById(tvResID);
                                    }
                                }
                                String exSol = "876914532315286749942375861438769215691523487257148396129437658584692173763851924";

                                final char[][] arraySol = new char[9][9];
                                for (int counter = 0; counter < 9; counter++) {
                                    exSol.getChars(counter * 9, (counter * 9) + 9, arraySol[counter], 0);
                                }

                                for (int row = 0; row < 9; row ++) {
                                    for (int col = 0; col < 9; col ++) {
                                        tvArray1[row][col].setText(String.valueOf(arraySol[row][col]));
                                    }
                                }

                                tvArray1[7][7].setBackgroundResource(0);

                                break;
                            case 7:
                                viewPager.setCurrentItem(0, true);
                                break;
                        }
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {

                    }
                });

        sequence.start();
    }

    private void startCamera() {
        CameraX.unbindAll();

        TextureView viewFinder = (TextureView) ocr.getView().findViewById(R.id.view_finder);

//        Rational aspectRatio = new Rational(1,1);
//        Size screen = new Size(manual.getView().findViewById(R.id.view_finder).getMeasuredWidth(), manual.getView().findViewById(R.id.view_finder).getMeasuredHeight());

        PreviewConfig previewConfig = new PreviewConfig.Builder()./*setTargetAspectRatio(AspectRatio.RATIO_4_3).*/setTargetResolution(new Size(200,200)).build();
        Preview preview = new Preview(previewConfig);

        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(Preview.PreviewOutput output) {
                ViewGroup parent = (ViewGroup) viewFinder.getParent();
                parent.removeView(viewFinder);
                parent.addView(viewFinder, 0);

                viewFinder.setSurfaceTexture(output.getSurfaceTexture());
                updateTransform();
            }
        });

        CameraX.bindToLifecycle((LifecycleOwner) this, preview);
    }

    private void updateTransform() {
        TextureView viewFinder = (TextureView) ocr.getView().findViewById(R.id.view_finder);

        Matrix matrix = new Matrix();
        float w = viewFinder.getMeasuredWidth();
        float h = viewFinder.getMeasuredHeight();

        float cX = w / 2f;
        float cY = h / 2f;

        int rotationDgr;
        int rotation = (int) viewFinder.getRotation();

        switch(rotation){
            case Surface.ROTATION_0:
                rotationDgr = 0;
                break;
            case Surface.ROTATION_90:
                rotationDgr = 90;
                break;
            case Surface.ROTATION_180:
                rotationDgr = 180;
                break;
            case Surface.ROTATION_270:
                rotationDgr = 270;
                break;
            default:
                return;
        }

        matrix.postRotate((float)rotationDgr, cX, cY);

        viewFinder.setTransform(matrix);
    }
}
