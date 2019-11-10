package com.beebeeoii.snapsolvesudoku;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

public class DigitRecognisor {

    private final String TESS_LANGUAGE = "eng";
    private final String TESS_WHITELIST = "123456789";

    private TessBaseAPI tessBaseAPI;

    public DigitRecognisor(Activity activity) {
        tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(activity.getExternalFilesDir(null).toString(), TESS_LANGUAGE, TessBaseAPI.OEM_DEFAULT);
        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, TESS_WHITELIST);
        tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_CHAR);
        Log.wtf("TESS VERSION", tessBaseAPI.getVersion());
    }

    private String getDigit(Bitmap digit) {

        tessBaseAPI.setImage(digit);
        String retStr = tessBaseAPI.getUTF8Text();

        tessBaseAPI.clear();
        return retStr;

//        if (tessBaseAPI.meanConfidence() > 10) {
//            tessBaseAPI.clear();
//            return retStr;
//        } else {
//            return "";
//        }
    }

    public int[][] getDigits(Bitmap digits[][]) {
        int solutions[][] = new int[9][9];

        for (int row = 0; row < 9; row ++) {
            for (int col = 0; col < 9; col ++) {
                tessBaseAPI.setImage(digits[row][col]);

                try {
                    solutions[row][col] = Integer.valueOf(tessBaseAPI.getUTF8Text());
                } catch (NumberFormatException e) {
                    solutions[row][col] = 0;
                }

                tessBaseAPI.clear();
            }
        }

        return solutions;


    }
}
