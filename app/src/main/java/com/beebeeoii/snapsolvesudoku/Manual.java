package com.beebeeoii.snapsolvesudoku;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Manual extends Fragment {

    private static TextView[][] tvArray = new TextView[9][9];
    private Button[] numberInput = new Button[9];
    private ImageView boardPreview;
    private Button clearAll, solve, preSol, nextSol, editBoard, forgetBoard, saveBoard;
    private LinearLayout clearAllSolve, solNavigator, saveBoardOption;
    private ConstraintLayout imgButton;
    private TextView solutionCounterTv;

    private View previousSelected = null;

    private int[][] board = new int[9][9];
    private String[] solSeparated;
    private int totalNoSols = 0;
    private String solutions = "";
    private ArrayList<char[][]> allSols = new ArrayList<char[][]>();
    private int solTracker = 0;

    private static Snackbar invalidBoard = null;

    SolverBF solverBF;
    public static TextView progressStatus;

    public static boolean isBoardSolved = false;

    private SharedPreferences sharedPreferences;
    final private String KEY_MAX_SOLS = "MAX_SOLS";
    final private String KEY_MIN_HINTS = "MIN_HINTS";
    final private int MAX_SOLS = 500;
    final private int MIN_HINTS = 15;

    private final String[] STORAGE_PERM = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private final int STORAGE_CODE = 2;

    public Manual() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_manual, container, false);

        sharedPreferences = getActivity().getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);

        //linking tv
        for (int row = 0; row < 9; row ++) {
            for (int col = 0; col < 9; col ++) {
                String textViewID = "g" + row + col;
                int tvResID = getResources().getIdentifier(textViewID, "id", getActivity().getPackageName());
                tvArray[row][col] = (TextView) rootView.findViewById(tvResID);
                tvArray[row][col].setOnClickListener(tvOnClick);
                tvArray[row][col].setOnLongClickListener(tvOnLongClick);
            }
        }

        //linking button
        for (int i = 0; i < 9; i ++) {
            String buttonID = "b" + i;
            int buttonResID = getResources().getIdentifier(buttonID, "id", getActivity().getPackageName());
            numberInput[i] = (Button) rootView.findViewById(buttonResID);
            numberInput[i].setOnTouchListener(buttonOnTouch);
            numberInput[i].setOnClickListener(buttonOnClick);
        }

        clearAll = (Button) rootView.findViewById(R.id.clear_all);
        solve = (Button) rootView.findViewById(R.id.solve);
        boardPreview = (ImageView) rootView.findViewById(R.id.board_preview);
        preSol = (Button) rootView.findViewById(R.id.previous_sol);
        nextSol = (Button) rootView.findViewById(R.id.next_sol);
        editBoard = (Button) rootView.findViewById(R.id.edit_board_button);
        forgetBoard = (Button) rootView.findViewById(R.id.forget_board_button);
        saveBoard = (Button) rootView.findViewById(R.id.save_board_button);
        clearAllSolve = (LinearLayout) rootView.findViewById(R.id.manual_linear);
        solNavigator = (LinearLayout) rootView.findViewById(R.id.manual_multi_sols);
        saveBoardOption = (LinearLayout) rootView.findViewById(R.id.save_board_option);
        imgButton = (ConstraintLayout) rootView.findViewById(R.id.manual_constraint);
        solutionCounterTv = (TextView) rootView.findViewById(R.id.solution_counter_tv);

        preSol.setOnClickListener((View v) -> {
            solTracker --;

            if (solTracker == 1) {
                v.setVisibility(View.INVISIBLE);
            }

            if (solTracker != allSols.size()) {
                nextSol.setVisibility(View.VISIBLE);
            }

            //printing to board
            for (int row = 0; row < 9; row ++) {
                for (int col = 0; col < 9; col ++) {
                    tvArray[row][col].setText(String.valueOf(allSols.get(solTracker - 1)[row][col]));
                }
            }

            solutionCounterTv.setText(solTracker + " / " + allSols.size());

        });

        nextSol.setOnClickListener((View v) -> {
            solTracker ++;

            if (solTracker == allSols.size()) {
                v.setVisibility(View.INVISIBLE);
            }

            if (solTracker != 1) {
                preSol.setVisibility(View.VISIBLE);
            }

            //printing to board
            for (int row = 0; row < 9; row ++) {
                for (int col = 0; col < 9; col ++) {
                    tvArray[row][col].setText(String.valueOf(allSols.get(solTracker - 1)[row][col]));
                }
            }

            solutionCounterTv.setText(solTracker + " / " + allSols.size());

        });

        editBoard.setOnTouchListener((View v, MotionEvent event) -> {

            Button button = (Button) v;

            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    button.setBackgroundResource(R.drawable.edit_board_selected_background);
                    button.setTextColor(Color.WHITE);
                    break;
                case MotionEvent.ACTION_UP:
                    button.setBackgroundResource(R.drawable.edit_board_background);
                    button.setTextColor(Color.BLACK);
                    break;
            }

            return false;
        });

        editBoard.setOnClickListener((View v) -> {
            isBoardSolved = false;

            clearAllSolve.setVisibility(View.VISIBLE);
            imgButton.setVisibility(View.VISIBLE);
            solNavigator.setVisibility(View.INVISIBLE);
            saveBoardOption.setVisibility(View.INVISIBLE);

            for (int row = 0; row < 9; row ++) {
                for (int col = 0; col < 9; col ++) {
                    if (!tvArray[row][col].getTypeface().isBold()) {
                        tvArray[row][col].setText("");
                        board[row][col] = 0;
                    }
                }
            }

            editBoard.setVisibility(View.INVISIBLE);
        });

        forgetBoard.setOnTouchListener((View v, MotionEvent event) -> {
            Button button = (Button) v;

            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    button.setBackgroundResource(R.drawable.clear_selected_background);
                    button.setTextColor(Color.WHITE);
                    break;
                case MotionEvent.ACTION_UP:
                    button.setBackgroundResource(R.drawable.clear_background);
                    button.setTextColor(Color.BLACK);
                    break;
            }

            return false;
        });

        forgetBoard.setOnClickListener((View v) -> {
            ((MainActivity) getActivity()).viewPager.setCurrentItem(0, true);
            Snackbar.make(getView(), "Board forgotten (for a very very long time)", Snackbar.LENGTH_SHORT).show();
        });

        saveBoard.setOnTouchListener((View v, MotionEvent event) -> {
            Button button = (Button) v;

            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    button.setBackgroundResource(R.drawable.solve_selected_background);
                    button.setTextColor(Color.WHITE);
                    break;
                case MotionEvent.ACTION_UP:
                    button.setBackgroundResource(R.drawable.solve_background);
                    button.setTextColor(Color.BLACK);
                    break;
            }

            return false;
        });

        saveBoard.setOnClickListener((View v) -> {

            if (hasPermissions(getActivity().getApplicationContext(), STORAGE_PERM)) {
                if (((MainActivity) getActivity()).original != null) {
                    Bitmap original = ((MainActivity) getActivity()).original;
                    saveTempBitmap(original, totalNoSols);
                } else {
                    Bitmap boardIcon = BitmapFactory.decodeResource(getResources(), R.drawable.manual_input_history_pic);
                    saveTempBitmap(boardIcon, totalNoSols);
                }

                ((MainActivity) getActivity()).viewPager.setCurrentItem(0, true);

                Snackbar.make(getView(), "Board saved to history!", Snackbar.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(getActivity(), STORAGE_PERM, STORAGE_CODE);
            }

        });

        clearAll.setOnTouchListener((View v, MotionEvent event) -> {

            Button button = (Button) v;

            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    button.setBackgroundResource(R.drawable.clear_selected_background);
                    button.setTextColor(Color.WHITE);
                    break;
                case MotionEvent.ACTION_UP:
                    button.setBackgroundResource(R.drawable.clear_background);
                    button.setTextColor(Color.BLACK);
                    break;
            }

            return false;
        });

        clearAll.setOnClickListener((View v) -> {
            int empty = 0;

            if (previousSelected != null) {
                previousSelected = null;

            }

            for (int row = 0; row < 9; row ++) {
                for (int col = 0; col < 9; col ++) {

                    if (tvArray[row][col].getText() == "") {
                        empty ++;
                        tvArray[row][col].setBackgroundResource(0);
                    } else {
                        tvArray[row][col].setText("");
                        board[row][col] = 0;

                        tvArray[row][col].setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
                        tvArray[row][col].setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                        tvArray[row][col].setTextColor(Color.GRAY);
                        tvArray[row][col].setBackgroundResource(0);
                    }
                }
            }

            if (empty == 81) {
                Snackbar clearCellSb = Snackbar.make(getView(), "Cells are already empty :)", Snackbar.LENGTH_SHORT);
                View sbView = clearCellSb.getView();
                sbView.setBackgroundColor(Color.GREEN);
                clearCellSb.show();
            } else {
                Snackbar clearCellSb = Snackbar.make(getView(), "All cells cleared successfully!", Snackbar.LENGTH_SHORT);
                View sbView = clearCellSb.getView();
                sbView.setBackgroundColor(Color.GREEN);
                clearCellSb.show();
            }

        });

        solve.setOnTouchListener((View v, MotionEvent event) -> {

            Button button = (Button) v;

            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    button.setBackgroundResource(R.drawable.solve_selected_background);
                    button.setTextColor(Color.WHITE);
                    break;
                case MotionEvent.ACTION_UP:
                    button.setBackgroundResource(R.drawable.solve_background);
                    button.setTextColor(Color.BLACK);
                    break;
            }

            return false;
        });

        solve.setOnClickListener((View v) -> {
            reset();

            int noHints = 0;
            for (int row = 0; row < 9; row ++) {
                for (int col = 0; col < 9; col ++) {

                    if (tvArray[row][col].getText() == "") {
                        board[row][col] = 0;
                    } else {
                        board[row][col] = Integer.parseInt(tvArray[row][col].getText().toString());
                        noHints ++;
                    }
                }
            }

            if (noHints < sharedPreferences.getInt(KEY_MIN_HINTS, MIN_HINTS)) {
                Snackbar lessHintsSb = Snackbar.make(getView(), "Board incomplete: Less than " + sharedPreferences.getInt(KEY_MIN_HINTS,15) + " hints", Snackbar.LENGTH_SHORT);
                View sbView = lessHintsSb.getView();
                sbView.setBackgroundColor(Color.RED);
                lessHintsSb.show();
                return;
            }

            BoardValidator boardValidator = new BoardValidator(board);
            ArrayList<int[]> errors = boardValidator.getErrors();
            if (errors.size() > 0) {
                for (int counter = 0; counter < errors.size(); counter ++) {
                    tvArray[errors.get(counter)[0]][errors.get(counter)[1]].setBackgroundResource(R.drawable.grid_wrong);
                }
                return;
            }

            clearAllSolve.setVisibility(View.GONE);
            imgButton.setVisibility(View.GONE);

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            View dialogView = getLayoutInflater().inflate(R.layout.solving_progress_dialog, null);
            progressStatus = (TextView) dialogView.findViewById(R.id.solving_progress_status);

            dialogBuilder.setView(dialogView);
            dialogBuilder.setCancelable(false);
            AlertDialog dialog = dialogBuilder.create();
            dialog.show();

            Thread thread = new Thread(() -> {

                solverBF = new SolverBF(board, sharedPreferences.getInt(KEY_MAX_SOLS, MAX_SOLS), getActivity());
                totalNoSols = solverBF.getSolCounter();
                solutions = solverBF.getSolutions();

                if (solutions.length() == 0) {
                    dialog.cancel();
                    invalidBoard = Snackbar.make(getView(), "Board cannot be solved!", Snackbar.LENGTH_INDEFINITE);
                    View sbView = invalidBoard.getView();
                    sbView.setBackgroundColor(Color.RED);
                    invalidBoard.setAction("Edit Board", (View button) -> {

                        isBoardSolved = false;

                        getActivity().runOnUiThread(() -> {
                            clearAllSolve.setVisibility(View.VISIBLE);
                            imgButton.setVisibility(View.VISIBLE);

                        });

                        invalidBoard.dismiss();
                    });

                    invalidBoard.setActionTextColor(Color.WHITE);
                    invalidBoard.show();
                    isBoardSolved = true;

                    return;
                }

                getActivity().runOnUiThread(() -> {
                    solNavigator.setVisibility(View.VISIBLE);
                    saveBoardOption.setVisibility(View.VISIBLE);
                    editBoard.setVisibility(View.VISIBLE);
                });

                //breaks up string into groups of 81 (each group represent 1 solution)
                solSeparated = solutions.split(" ");

                //only 1 solution
                if (solSeparated.length == 1) {

                    final char[][] array = new char[9][9];
                    for (int counter = 0; counter < 9; counter++) {
                        solSeparated[0].getChars(counter * 9, (counter * 9) + 9, array[counter], 0);
                    }

                    //printing to board
                    for (int row = 0; row < 9; row ++) {
                        for (int col = 0; col < 9; col ++) {
                            updateTv(row, col, Character.toString(array[row][col]));
//                            tvArray[row][col].setText(Character.toString(array[row][col]));
                        }
                    }
                }

                //more than 1 solution
                if (solSeparated.length > 1) {
                    for (int noOfSolution = 1; noOfSolution <= solSeparated.length; noOfSolution ++) {

                        char[][] array = new char[9][9];
                        for (int counter = 0; counter < 9; counter++) {
                            solSeparated[noOfSolution - 1].getChars(counter * 9, (counter * 9) + 9, array[counter], 0);
                        }

                        allSols.add(array);

                    }

                    //printing to board
                    for (int row = 0; row < 9; row ++) {
                        for (int col = 0; col < 9; col ++) {
                            updateTv(row, col, Character.toString(allSols.get(0)[row][col]));
//                            tvArray[row][col].setText(Character.toString(allSols.get(0)[row][col]));
                        }
                    }

                    getActivity().runOnUiThread(() -> {
                        preSol.setVisibility(View.INVISIBLE);
                        solutionCounterTv.setText("1 / " + allSols.size());
                        nextSol.setVisibility(View.VISIBLE);
                    });
                }

                isBoardSolved = true;

                dialog.cancel();
            });

            thread.start();

        });

        return rootView;
    }

    private void updateTv(int row, int col, String number) {
        tvArray[row][col].setText(number);
    }

    private View.OnTouchListener buttonOnTouch = (View v, MotionEvent event) -> {

        Button button = (Button) v;

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                button.setBackgroundResource(R.drawable.input_button_selected);
                button.setTextColor(Color.WHITE);
                break;
            case MotionEvent.ACTION_UP:
                button.setBackgroundResource(R.drawable.input_button);
                button.setTextColor(Color.BLACK);
                break;
        }

        return false;
    };

    private View.OnClickListener buttonOnClick = (View v) -> {
        if (previousSelected == null) {
            Snackbar selectCellSb = Snackbar.make(getView(), "Select a cell!", Snackbar.LENGTH_SHORT);
            View sbView = selectCellSb.getView();
            sbView.setBackgroundColor(Color.RED);
            selectCellSb.show();
        } else {
            TextView selectedTv = (TextView) previousSelected;
            selectedTv.setBackgroundResource(R.drawable.grid_seelcted_bg);
            selectedTv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            selectedTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
            selectedTv.setTextColor(Color.DKGRAY);

            Button input = (Button) v;

            String inputIndex = selectedTv.getResources().getResourceEntryName(selectedTv.getId());
            int fIndex = Character.getNumericValue(inputIndex.charAt(1));
            int sIndex = Character.getNumericValue(inputIndex.charAt(2));

            tvArray[fIndex][sIndex].setText(input.getText());
            board[fIndex][sIndex] = Integer.parseInt(selectedTv.getText().toString());
        }
    };

    public View.OnClickListener tvOnClick = (View v) -> {
        if (!isBoardSolved) {
            if (previousSelected == null) {
                v.setBackgroundResource(R.drawable.grid_seelcted_bg);
                previousSelected = v;
            } else {

                previousSelected.setBackgroundResource(0);

                if (previousSelected == v) {
                    previousSelected = null;
                } else {
                    v.setBackgroundResource(R.drawable.grid_seelcted_bg);
                    previousSelected = v;
                }
            }
        }
    };

    public View.OnLongClickListener tvOnLongClick = (View v) -> {
        if (!isBoardSolved) {
            String idString = v.getResources().getResourceEntryName(v.getId());

            int fIndex = Character.getNumericValue(idString.charAt(1));
            int sIndex = Character.getNumericValue(idString.charAt(2));

            if (tvArray[fIndex][sIndex].getText() == "") {
                Snackbar selectCellSb = Snackbar.make(getView(), "Selected cell is already empty!", Snackbar.LENGTH_SHORT);
                View sbView = selectCellSb.getView();
                sbView.setBackgroundColor(Color.RED);
                selectCellSb.show();

                if (previousSelected != null) {
                    previousSelected.setBackgroundResource(0);
                    previousSelected = null;
                }

            } else {
                tvArray[fIndex][sIndex].setText("");
                tvArray[fIndex][sIndex].setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
                tvArray[fIndex][sIndex].setTextColor(Color.GRAY);
                tvArray[fIndex][sIndex].setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);

                Snackbar clearCellSb = Snackbar.make(getView(), "Cell cleared successfully!", Snackbar.LENGTH_SHORT);
                View sbView = clearCellSb.getView();
                sbView.setBackgroundColor(Color.GREEN);
                clearCellSb.show();

                board[fIndex][sIndex] = 0;

                if (v.getBackground() != null) {
                    if (v.getBackground().getConstantState() == getResources().getDrawable(R.drawable.grid_wrong, null).getConstantState()) {
                        v.setBackgroundResource(0);
                    }
                }

                if (previousSelected != null) {
                    previousSelected.setBackgroundResource(0);
                    previousSelected = null;
                }
            }
        }

        return true;
    };

    private void reset() {
        for (int row = 0; row < 9; row ++) {
            for (int col = 0; col < 9; col ++) {
                tvArray[row][col].setBackgroundResource(0);
            }
        }

        if (previousSelected != null) {
            previousSelected = null;
        }

        solSeparated = null;
        board = new int[9][9];
        totalNoSols = 0;
        solutions = "";
        allSols = new ArrayList<char[][]>();
        solTracker = 0;
        solutionCounterTv.setText("1 / 1");
        preSol.setVisibility(View.GONE);
        nextSol.setVisibility(View.GONE);
    }

    public static boolean isSnackbarShown() {
        if (invalidBoard == null) {
            return false;
        }
        return invalidBoard.isShown();
    }

    public static void hideSnackbar() {
        invalidBoard.dismiss();
    }

    private void saveTempBitmap(Bitmap bitmap, int noSols) {
        if (isExternalStorageWritable()) {
            saveImage(bitmap, noSols);
        }else{
            //prompt the user or do something
        }
    }

    private void saveImage(Bitmap finalBitmap, int noSols) {

        String root = getActivity().getExternalFilesDir(null).toString();
        File myDir = new File(root + "/scanned_boards");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }

        String timeStamp = new SimpleDateFormat("ddMMyy_HHmmss").format(new Date());

        String day;
        try {
            SimpleDateFormat inFormat = new SimpleDateFormat("ddMMyy");
            Date date = inFormat.parse(timeStamp.split("_")[0]);
            SimpleDateFormat outFormat = new SimpleDateFormat("EEEE");
            day = outFormat.format(date);
        } catch (ParseException e) {
            day = "ERR";
        }

        String board = "";
        for (int row = 0; row < 9; row ++) {
            for (int col = 0; col < 9; col ++) {
                board += this.board[row][col];
            }
        }

        String fname = timeStamp + "_" + day + "_" + board + "_" + noSols; //file name will be like: 010119_133122_board_1.jpg
        String boardFName = fname + ".jpg";
        String solFName = fname + ".txt";

        File file = new File(myDir, boardFName);
        if (file.exists()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        saveSolutionsFile(solFName, solutions);
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private void saveSolutionsFile(String fileName, String boardSolution) {
        try {
            File root = new File(getActivity().getExternalFilesDir(null) + "/scanned_boards_solutions");
            if (!root.exists()) {
                root.mkdirs();
            }

            File solution = new File(root, fileName);
            FileWriter writer = new FileWriter(solution);
            writer.append(boardSolution);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
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


}
