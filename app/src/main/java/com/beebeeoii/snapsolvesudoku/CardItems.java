package com.beebeeoii.snapsolvesudoku;

import android.graphics.Bitmap;

public class CardItems {

    int historyNoSols;
    String historyDate, historyBoard;
    Bitmap historyPic;

    public CardItems(int historyNoSols, String historyDate, Bitmap historyPic, String historyBoard) {
        this.historyNoSols = historyNoSols;
        this.historyDate = historyDate;
        this.historyPic = historyPic;
        this.historyBoard = historyBoard;
    }

    public int getHistoryNoSols() {
        return historyNoSols;
    }

    public String getHistoryDate() {
        return historyDate;
    }

    public Bitmap getHistoryPic() {
        return historyPic;
    }

    public String getHistoryBoard() {
        return historyBoard;
    }

}
