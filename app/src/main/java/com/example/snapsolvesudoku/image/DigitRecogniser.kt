package com.example.snapsolvesudoku.image

import android.graphics.Bitmap
import android.util.Log
import com.googlecode.tesseract.android.TessBaseAPI

class DigitRecogniser constructor(filePath : String) {

    private val TESS_LANGUAGE = "eng"
    private val TESS_WHITELIST = "123456789"
    private val TESS_ENGINE = TessBaseAPI.OEM_TESSERACT_LSTM_COMBINED

    var tessBaseAPI: TessBaseAPI = TessBaseAPI()

    init {
        tessBaseAPI.init(filePath, TESS_LANGUAGE, TESS_ENGINE)
        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, TESS_WHITELIST)
        tessBaseAPI.pageSegMode = TessBaseAPI.PageSegMode.PSM_SINGLE_LINE

        Log.wtf("TESS VERSION", tessBaseAPI.version)
    }

    fun getDigits(pic : Bitmap) : String {
        tessBaseAPI.setImage(pic)
        return tessBaseAPI.utF8Text
    }
}