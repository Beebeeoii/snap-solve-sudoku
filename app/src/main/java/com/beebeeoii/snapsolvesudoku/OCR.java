package com.beebeeoii.snapsolvesudoku;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;

import androidx.camera.core.CameraX;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import android.util.Size;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.material.snackbar.Snackbar;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;

public class OCR extends Fragment {

    TextureView viewFinder;
    Button captureButton;

    ConstraintLayout ocr_main;
    LinearLayout slide_panel;
    SlidingUpPanelLayout slidingUpPanelLayout;

    Bitmap original = null;

    public static ProgressBar progressBar;
    public static TextView progressStatus;

    public static int[][] board = new int[9][9];
    public static Bitmap boardProcessed = null;
    private ImageProcessor imageProcessor;
    private Thread imageProcessingThread;

    private final String[] CAMERA_STORAGE_PERM = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private final int CAMERA_STORAGE_PERM_CODE = 0;
    private final String[] CAMERA_PERM = {Manifest.permission.CAMERA};
    private final int CAMERA_CODE = 1;
    private final String[] STORAGE_PERM = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private final int STORAGE_CODE = 2;

    private final String FILE_NAME = "eng.traineddata";

    public OCR() {

    }

    public Bitmap getOriginal() {
        return original;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ocr, container, false);

        viewFinder = (TextureView) rootView.findViewById(R.id.view_finder);
        captureButton = (Button) rootView.findViewById(R.id.capture_button);

        slidingUpPanelLayout = (SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_panel_layout);
        ocr_main = (ConstraintLayout) rootView.findViewById(R.id.ocr_main);
        slide_panel = (LinearLayout) rootView.findViewById(R.id.panel);

        if(!hasPermissions(getActivity().getApplicationContext(), CAMERA_STORAGE_PERM)){
            ActivityCompat.requestPermissions(getActivity(), CAMERA_STORAGE_PERM, CAMERA_STORAGE_PERM_CODE);

        } else {
            if (hasPermissions(getActivity().getApplicationContext(), CAMERA_PERM)) {
                startCamera();
            }
        }

        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

                if (slideOffset >= 0.3) {
                    slide_panel.setAlpha(slideOffset);
                }

                if (slideOffset <= 0.5) {
                    ocr_main.setAlpha(1 - slideOffset);
                }

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

            }
        });

        ocr_main.setOnClickListener((View v) -> slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED));

        captureButton.setOnTouchListener((View v, MotionEvent event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setAlpha(0.5f);
                    break;
                case MotionEvent.ACTION_UP:
                    v.setAlpha(1.0f);
                    break;
            }

            return false;
        });

        captureButton.setOnClickListener((View v)-> {
            YoYo.with(Techniques.FadeOut).duration(700).playOn(v);

            if (!(new File(getActivity().getExternalFilesDir(null).toString() + "/tessdata" + "/eng.traineddata")).exists()) {
                YoYo.with(Techniques.FadeIn).duration(700).playOn(v);


                if (!hasPermissions(getActivity().getApplicationContext(), STORAGE_PERM)) {
                    Toast.makeText(getActivity().getApplicationContext(), "Data file for OCR unavailable due to no Read Write Access!", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(getActivity(), STORAGE_PERM, STORAGE_CODE);

                } else {
                    ((MainActivity) getActivity()).copyFile();
                }

                if ((new File(getActivity().getExternalFilesDir(null).toString() + "/tessdata" + "/eng.traineddata")).exists()) {
                    Toast.makeText(getActivity().getApplicationContext(), "Please try again.", Toast.LENGTH_SHORT).show();
                }

                return;

            }

            if (hasPermissions(getActivity().getApplicationContext(), CAMERA_STORAGE_PERM)) {

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

                View dialogView = getLayoutInflater().inflate(R.layout.dialog, null);
                progressStatus = (TextView) dialogView.findViewById(R.id.ocr_progress_status);
                progressBar = (ProgressBar) dialogView.findViewById(R.id.ocr_progress_bar);
                progressBar.setProgress(0);
                progressBar.setIndeterminate(false);
                progressBar.setMax(100);
                progressStatus.setText(progressBar.getProgress() + " %");

                AlertDialog dialog;
                dialogBuilder.setView(dialogView);
                dialogBuilder.setNegativeButton("Cancel", (DialogInterface dialogInterface, int which) -> {
                    if (imageProcessingThread.isAlive()) {
                        imageProcessingThread.interrupt();
                    }
                });
                dialogBuilder.setCancelable(false);
                dialog = dialogBuilder.create();
                dialog.show();

                imageProcessingThread = new Thread(() -> {


                    original = viewFinder.getBitmap();

                    try {
//                        int width = viewFinder.getBitmap().getWidth();
//                        int height = viewFinder.getBitmap().getHeight();
//                        float scaleWidth = ((float) viewFinder.getBitmap().getWidth() / 2) / width;
//                        float scaleHeight = ((float) viewFinder.getBitmap().getHeight() / 2) / height;
//                        // CREATE A MATRIX FOR THE MANIPULATION
//                        Matrix matrix = new Matrix();
//                        // RESIZE THE BIT MAP
//                        matrix.postScale(scaleWidth, scaleHeight);
//
//                        // "RECREATE" THE NEW BITMAP
//                        Bitmap resizedBitmap = Bitmap.createBitmap(
//                                viewFinder.getBitmap(), 0, 0, width, height, matrix, false);
                        imageProcessor = new ImageProcessor(viewFinder.getBitmap(), getActivity());
                    } catch (IndexOutOfBoundsException e) {
                        getActivity().runOnUiThread(() -> {
                            Snackbar boardFailSb = Snackbar.make(getView(), "Board detection failed :( Please try again.", Snackbar.LENGTH_SHORT);
                            View sbView = boardFailSb.getView();
                            sbView.setBackgroundColor(Color.RED);
                            boardFailSb.show();
                            YoYo.with(Techniques.FadeIn).duration(700).playOn(v);
                            dialog.dismiss();
                        });
                        return;
                    }

                    if (imageProcessingThread.isInterrupted()) {
                        onThreadInterrupted(v);
                        return;
                    }

                    progressUpdate(85);

                    if (imageProcessor.getNoErrors() > 5 || imageProcessor.getNoDigits() <= 15) {

                        getActivity().runOnUiThread(() -> {
                            Snackbar boardFailSb = Snackbar.make(getView(), "Board detection failed :( Please try again.", Snackbar.LENGTH_SHORT);
                            View sbView = boardFailSb.getView();
                            sbView.setBackgroundColor(Color.RED);
                            boardFailSb.show();
                            YoYo.with(Techniques.FadeIn).duration(700).playOn(v);
                            progressUpdate(100);
                            dialog.dismiss();
                        });

                        return;

                    } else {
                        board = imageProcessor.getDigits();
                        boardProcessed = imageProcessor.getProcessedImage();
                    }

                    if (imageProcessingThread.isInterrupted()) {
                        onThreadInterrupted(v);
                        return;
                    }

                    getActivity().runOnUiThread(() -> {
                        progressUpdate(100);

                        ((MainActivity) getActivity()).viewPager.setCurrentItem(1, true);
                        ((MainActivity) getActivity()).original = original;
                        YoYo.with(Techniques.FadeIn).duration(700).playOn(v);
                        dialog.dismiss();

                    });

                });

                imageProcessingThread.start();

            } else {
                YoYo.with(Techniques.FadeIn).duration(700).playOn(v);
                ActivityCompat.requestPermissions(getActivity(), CAMERA_STORAGE_PERM, CAMERA_STORAGE_PERM_CODE);
            }

        });

        return rootView;
    }

    private void onThreadInterrupted(View v) {
        getActivity().runOnUiThread(() -> YoYo.with(Techniques.FadeIn).duration(700).playOn(v));
    }

    private void progressUpdate(int progress) {
        getActivity().runOnUiThread(() -> {
            progressBar.setProgress(progress);
            progressStatus.setText(progressBar.getProgress() + " %");
        });
    }

    private void startCamera() {
        CameraX.unbindAll();

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
