package com.beebeeoii.snapsolvesudoku;

import android.app.Activity;
import android.graphics.Bitmap;

import androidx.fragment.app.FragmentActivity;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.List;

import static com.beebeeoii.snapsolvesudoku.OCR.progressBar;
import static com.beebeeoii.snapsolvesudoku.OCR.progressStatus;

public class ImageProcessor {

    private Bitmap rawImage, processedImage = null;
    private Mat rawImageMat, initialPoints, finalPoints;

    private Mat largestContourMat, denoisedLargestContourMat;

    private int line = 0;
    private int digits[][];
    private int noErrors;
    private int noDigits;
    private int noIterations = 0;

    private Activity activity;

    public ImageProcessor(Bitmap image, Activity activity) {
        setRawImage(image);
        this.activity = activity;
        boardExtract();
    }

    public void setRawImage(Bitmap rawImage) {
        this.rawImage = rawImage;
    }

    public Bitmap getProcessedImage() {
        return processedImage;
    }

    public int[][] getDigits() {
        return digits;
    }

    public int getNoErrors() {
        return noErrors;
    }

    public int getNoDigits() {
        return noDigits;
    }

    private void boardExtract () {

        noIterations = 0;

        DigitRecognisor digitRecognisor = new DigitRecognisor(activity);

        rawImageMat = new Mat();
        Utils.bitmapToMat(rawImage, rawImageMat);

        do {
            noIterations ++;
            largestContourMat = contourGridExtract(processImage(rawImage)); //with gridlines for boardpreview (SOLELY TO EXTRACT BOARD FROM DENOISED MAT)

           progressUpdate("add", 5);

            denoisedLargestContourMat = new Mat();
            Mat transform = Imgproc.getPerspectiveTransform(initialPoints, finalPoints);
            Imgproc.warpPerspective(processImageDenoise(rawImage), denoisedLargestContourMat, transform, processImageDenoise(rawImage).size());

            //remove gridlines
            Mat lines = new Mat();
            Imgproc.HoughLinesP(largestContourMat, lines, 1, Math.PI / 180, 150, 200, 25);
            Mat denoisedWOLines = removeGridLines(denoisedLargestContourMat, lines);

            progressUpdate("add", 4);

            Bitmap denoisedWOLinesBit = matToBitmap(denoisedWOLines);
            Bitmap denoisedDigits[][] = splitBitmap(denoisedWOLinesBit, 9, 9);

            //check if board captured correctly
            digits = digitRecognisor.getDigits(denoisedDigits);

            BoardValidator boardValidator = new BoardValidator(digits);
            noErrors = boardValidator.getErrors().size();
            noDigits = boardValidator.getNoDigits();

            progressUpdate("add", 3);

            //in case the largestContour is not the board...
            Imgproc.warpPerspective(rawImageMat, rawImageMat, transform, rawImageMat.size());
            rawImage = matToBitmap(rawImageMat);

            progressUpdate("add", 2);

            switch (noIterations) {
                case 1:
                    progressUpdate("set", 15);
                    break;
                case 2:
                    progressUpdate("set", 30);
                    break;
                case 3:
                    progressUpdate("set", 45);
                    break;
                case 4:
                    progressUpdate("set", 60);
                    break;
                case 5:
                    progressUpdate("set", 75);
                    break;
            }

        }
        while ((noErrors > 5 || noDigits <= 15) && noIterations < 6);

        processedImage = matToBitmap(largestContourMat);
        progressUpdate("set", 80);
    }

    private Mat processImage(Bitmap bitmap) {
        Mat oMat = new Mat();
        Utils.bitmapToMat(bitmap, oMat);

        Mat gray = new Mat();
        Imgproc.cvtColor(oMat, gray, Imgproc.COLOR_BGR2GRAY, 1);

        Mat blur = new Mat();
        Imgproc.GaussianBlur(gray, blur, new org.opencv.core.Size(11,11), 0);

        Mat thresh = new Mat();
        Imgproc.adaptiveThreshold(blur, thresh, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 2);

        Mat swop = new Mat();
        Core.bitwise_not(thresh, swop);

        return swop;
    }

    private Mat processImageDenoise(Bitmap bitmap) {
        Mat oMat = new Mat();
        Utils.bitmapToMat(bitmap, oMat);

//        Photo.fastNlMeansDenoisingColored(oMat, oMat, 10,10,7, 21);

        Mat gray = new Mat();
        Imgproc.cvtColor(oMat, gray, Imgproc.COLOR_BGR2GRAY, 1);

        Photo.fastNlMeansDenoising(gray, gray, 10, 7, 21);

        Mat blur = new Mat();
        Imgproc.GaussianBlur(gray, blur, new org.opencv.core.Size(11, 11), 0);

        Mat thresh = new Mat();
        Imgproc.adaptiveThreshold(blur, thresh, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 2);

        Mat swop = new Mat();
        Core.bitwise_not(thresh, swop);

        return swop;
    }

    private Mat contourGridExtract(Mat processed) {
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(processed, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        MatOfPoint largestArea = contours.get(0);

        for (int counter = 0; counter < contours.size(); counter ++) {

            if (Imgproc.contourArea(largestArea) <= Imgproc.contourArea(contours.get(counter))) {
                largestArea = contours.get(counter);
            }
        }

        Point[] corners = identifyCorners(largestArea.toArray());

        //List of original corners
        List<Point> oCorners = new ArrayList<Point>();
        oCorners.add(corners[0]);
        oCorners.add(corners[1]);
        oCorners.add(corners[2]);
        oCorners.add(corners[3]);

        //List of final corners
        List<Point> fCorners = new ArrayList<Point>();
        fCorners.add(new Point(0, 0));
        fCorners.add(new Point(processed.width(), 0));
        fCorners.add(new Point(0, processed.height()));
        fCorners.add(new Point(processed.width(), processed.height()));

        //Perspective Transform
        initialPoints = Converters.vector_Point2f_to_Mat(oCorners);
        finalPoints = Converters.vector_Point2f_to_Mat(fCorners);

        Mat transform = Imgproc.getPerspectiveTransform(initialPoints, finalPoints);
        Mat destImg = new Mat();
        Imgproc.warpPerspective(processed, destImg, transform, processed.size());

        //Transforming rawBit also in case largest area != board
//        board = new Mat();
//        Utils.bitmapToMat(rawBit, board);
//        Imgproc.warpPerspective(board, board, transform, board.size());

        return destImg;
    }

    private Point[] identifyCorners(Point[] contourPoints) {
        Point leftTopCorner = new Point();
        Point rightTopCorner = new Point();
        Point leftBottomCorner = new Point();
        Point rightBottomCorner = new Point();

        double xplusy, xminusy;
        double smallestxplusy = 10000000, smallestxminusy = 10000000, largestxminusy = 0, largestxplusy = 0;

        for (int counter = 0 ; counter < contourPoints.length; counter ++) {

            xplusy = contourPoints[counter].x + contourPoints[counter].y;
            xminusy = contourPoints[counter].x - contourPoints[counter].y;

            //finding left top corner - smallest x+y value
            if (xplusy < smallestxplusy) {
                smallestxplusy = xplusy;
                leftTopCorner.x = contourPoints[counter].x;
                leftTopCorner.y = contourPoints[counter].y;
            }

            //finding left bottom corner - smallest x-y value
            if (xminusy < smallestxminusy) {
                smallestxminusy = xminusy;
                leftBottomCorner.x = contourPoints[counter].x;
                leftBottomCorner.y = contourPoints[counter].y;
            }

            //finding right top corner - largest x-y value
            if (xminusy > largestxminusy) {
                largestxminusy = xminusy;
                rightTopCorner.x = contourPoints[counter].x;
                rightTopCorner.y = contourPoints[counter].y;
            }

            //finding right bottom corner - largest x+y value
            if (xplusy > largestxplusy) {
                largestxplusy = xplusy;
                rightBottomCorner.x = contourPoints[counter].x;
                rightBottomCorner.y = contourPoints[counter].y;
            }
        }

        return new Point[]{leftTopCorner, rightTopCorner, leftBottomCorner, rightBottomCorner};
    }

    private Mat removeGridLines(Mat grid, Mat lines) {
//
//        Mat lines = new Mat();
//        Imgproc.HoughLinesP(grid, lines, 1, Math.PI / 180, 150, 200, 25);

        final double LINE_POSITIONS = ((double) grid.rows() / 9.0);
        final double MIN_THRESHOLD = (LINE_POSITIONS / 100.0 ) * 15.0;
        final double MAX_THRESHOLD = (LINE_POSITIONS / 100.0 ) * 85.0;
        final double SAME_LINE = (((double) grid.rows() / 100.0) * 1.0);
        final int LINE_THICKNESS = (int) ((grid.rows() / 100.0) * 1.0);

        for (int i = 0; i < lines.rows(); i ++) {
            double[] vec = lines.get(i, 0);
            double x1 = vec[0], y1 = vec[1], x2 = vec[2], y2 = vec[3];
            Point startLine = new Point(x1, y1);
            Point endLine = new Point(x2, y2);

            boolean isHorizontal = (((startLine.y - endLine.y) < SAME_LINE) && (startLine.y - endLine.y > -SAME_LINE));
            boolean isVertical = (((startLine.x - endLine.x) < SAME_LINE) && (startLine.x - endLine.x > -SAME_LINE));

            if (isHorizontal && ((startLine.y % LINE_POSITIONS <= MIN_THRESHOLD) || startLine.y % LINE_POSITIONS >= MAX_THRESHOLD)) {
                startLine.x = 0;
                endLine.x = grid.rows();
                line ++;
            } else if (isVertical && ((startLine.x % LINE_POSITIONS <= MIN_THRESHOLD) || startLine.x % LINE_POSITIONS >= MAX_THRESHOLD)) {
                startLine.y = 0;
                endLine.y = grid.cols();
                line ++;
            } else {
                startLine.x = 0;
                startLine.y = 0;
                endLine.x = 0;
                endLine.y = 0;
            }

            Imgproc.line(grid, startLine, endLine, new Scalar(0, 0, 0), LINE_THICKNESS);
        }

        return grid;
    }

    private Bitmap[][] splitBitmap(Bitmap bitmap, int xCount, int yCount) {

        Bitmap[][] bitmaps = new Bitmap[xCount][yCount];

        int width, height;
        width = bitmap.getWidth() / xCount;
        height = bitmap.getHeight() / yCount;

        for (int x = 0; x < xCount; x ++) {
            for (int y = 0; y < yCount; y ++) {
                bitmaps[y][x] = Bitmap.createBitmap(bitmap, x * width, y * height, width, height);
            }
        }

        return bitmaps;
    }

    private Mat digitExtract(Bitmap sliced) {
        Mat digitFloodfill = new Mat();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Utils.bitmapToMat(sliced, digitFloodfill);

        Imgproc.cvtColor(digitFloodfill, digitFloodfill, Imgproc.COLOR_BGRA2GRAY);
        Imgproc.findContours(digitFloodfill, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);

//        Rect bound;

        if (contours.size() == 0) {
            return null;
        }

        //finding largest contour - largest contour signifies the grid
        try {
            MatOfPoint largestArea = contours.get(0);

            for (int counter = 0; counter < contours.size(); counter ++) {

                if (Imgproc.contourArea(largestArea) <= Imgproc.contourArea(contours.get(counter))) {
                    largestArea = contours.get(counter);
                }
            }

            if (Imgproc.contourArea(largestArea) > (((digitFloodfill.width() * digitFloodfill.height()) / 100.0) * 1.0)) {
//                Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new org.opencv.core.Size(1, 1));
//                Imgproc.erode(digitFloodfill, digitFloodfill, kernel);

//                bound = Imgproc.boundingRect(largestArea);
//                Mat temp = new Mat(digitFloodfill, bound);
//                int borderSizeTop = (int) ((temp.height() / 100.0) * 100.0);
//                int borderSizeBottomm = (int) ((temp.height() / 100.0) * 200.0);
//                int borderSizeSides = (int) ((temp.height() / 100.0) * 40.0);
//
//                Core.copyMakeBorder(new Mat(digitFloodfill, bound), digitFloodfill, borderSizeTop, borderSizeBottomm, borderSizeSides, borderSizeSides, Core.BORDER_CONSTANT, new Scalar(0, 0, 0));

                return new Mat(digitFloodfill, new Rect(5, 5, digitFloodfill.width() - 10, digitFloodfill.height() - 10));
            }

        } catch (IndexOutOfBoundsException e) {
            return null;
        }

        return null;
    }

    private void progressUpdate(String mode, int progress) {
        activity.runOnUiThread(() -> {

            if (mode == "add") {
                progressBar.setProgress(progressBar.getProgress() + progress);
                progressStatus.setText(progressBar.getProgress() + " %");
            }

            if (mode == "set") {
                progressBar.setProgress(progress);
                progressStatus.setText(progressBar.getProgress() + " %");
            }

        });
    }

//    private void ocrScan(final Bitmap imageCaptured, final int iteration) {
//
//        final Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                rawBit = imageCaptured;
//                String scannedBoardString;
//                Bitmap digitBit;
//                int noIterations = 0;
//
//                do {
//                    try {
//                        noIterations ++;
//                        progressDialog.setMessage("Board (" + noIterations + ") Processing...");
//                        scannedBoardString = "";
//                        Mat gridMat = contourGridExtract(imageProcessor(rawBit)); //with gridlines for boardpreview
//
//                        Mat realBoard = new Mat();
//                        Mat transform = Imgproc.getPerspectiveTransform(initialPoints, finalPoints);
//                        Imgproc.warpPerspective(imageProcessorBoard(rawBit), realBoard, transform, imageProcessorBoard(rawBit).size());
//
//                        gridBit = Bitmap.createBitmap(gridMat.cols(), gridMat.rows(), Bitmap.Config.ARGB_8888);
//                        realBoardBit = Bitmap.createBitmap(realBoard.cols(), realBoard.rows(), Bitmap.Config.ARGB_8888);
//                        Utils.matToBitmap(gridMat, gridBit);
//                        Utils.matToBitmap(realBoard, realBoardBit);
//
//                        Mat gridWOMat = removeGridLines(realBoard); //used for OCR
//                        gridWOLinesBit = Bitmap.createBitmap(gridWOMat.cols(), gridWOMat.rows(), Bitmap.Config.ARGB_8888);
//                        Utils.matToBitmap(gridWOMat, gridWOLinesBit);
//
//                        indiGrids = splitBitmap(gridWOLinesBit, 9, 9);
//
//                        for (int row = 0; row < indiGrids.length; row ++) {
//                            for (int col = 0; col < indiGrids[row].length; col ++) {
//                                progressDialog.setMessage("Extracting Digits: " + ((row + 1) * (col + 1)) + "/81");
//
//                                if (tvArray[row][col].getBackground() != null) {
//                                    tvArray[row][col].setBackgroundResource(0);
//                                }
//
//                                Mat digitMat = digitExtract(indiGrids[row][col]);
//
//                                if (digitMat != null) {
//                                    digitBit = Bitmap.createBitmap(digitMat.cols(), digitMat.rows(), Bitmap.Config.ARGB_8888);
//                                    Utils.matToBitmap(digitMat, digitBit);
//                                    indiGrids[row][col] = digitBit;
//
//                                    try {
//                                        scannedBoard[row][col] = Integer.parseInt(getText(indiGrids[row][col]));
//                                    } catch (NumberFormatException e) {
//                                        scannedBoard[row][col] = 0;
//                                    }
//
//                                } else {
//                                    scannedBoard[row][col] = 0;
//                                }
//
//                                scannedBoardString += scannedBoard[row][col];
//
//                            }
//                        }
//
//                        isValid(scannedBoard, false);
//
//                        if (scannedBoardString.replace("0", "").length() <= 15 || noErrors > 5) {
//
//                            if (noIterations == 5) {
//                                getActivity().runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        //clear memory of scanned failed board
//                                        changeToManual();
//                                        changeToOCR();
//
//                                        Toast.makeText(getActivity().getApplicationContext(), "Unable to detect any board. Please try again!", Toast.LENGTH_LONG).show();
//                                    }
//                                });
//                                progressDialog.dismiss();
//                            }
//
//                            Utils.matToBitmap(board, rawBit);
//                        } else {
//                            getActivity().runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    changeToManual();
//
//                                    if (noErrors > 4) {
//                                        changeToOCR();
//                                        Snackbar.make(getView(), "Board not detected satisfactorily: Too many errors (" + noErrors + ") Please try again!", Snackbar.LENGTH_LONG).show();
//                                    }
//                                }
//                            });
//
//                            progressDialog.dismiss();
//
//                        }
//
//                    } catch (Exception e) {
//                        getActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                //clear memory of scanned failed board
//                                changeToManual();
//                                changeToOCR();
//
//                                Toast.makeText(getActivity().getApplicationContext(), "Unable to detect any board. Please try again!", Toast.LENGTH_LONG).show();
//                            }
//                        });
//                        progressDialog.dismiss();
//                        break;
//                    }
//                } while (scannedBoardString.replace("0","").length() <= 15 || noErrors > 5);
//
//            }
//        });
//        thread.start();
//    }

    private Bitmap matToBitmap (Mat mat) {

        Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);

        return bitmap;
    }

}
