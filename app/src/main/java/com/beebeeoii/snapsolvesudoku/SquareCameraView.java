package com.beebeeoii.snapsolvesudoku;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

public class SquareCameraView extends TextureView {
    public SquareCameraView(Context context) {
        super(context);
    }

    public SquareCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    int squareDimension = 1000000000;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = this.getMeasuredHeight();
        int width = this.getMeasuredWidth();
        int currentSquareDim = Math.min(width, height);

        if (currentSquareDim == 0) {
            currentSquareDim = Math.max(width, height);
        }

        if (currentSquareDim < squareDimension) {
            squareDimension = currentSquareDim;
        }

        setMeasuredDimension(currentSquareDim, currentSquareDim);
    }
}
