package com.example.scanner;

import android.graphics.*;
import com.example.scanner.camera.GraphicOverlay;
import com.google.android.gms.vision.barcode.Barcode;

/**
 Created by alatta on 3/2/17.
 */

public class ScannerGraphic extends GraphicOverlay.Graphic {

    private int mId;

    private static final int COLOR_CHOICES[] = {Color.CYAN, Color.BLUE, Color.GREEN};

    private static int mCurrColorIndex = 0;
    private Paint mRectPaint;
    private Paint mTextPaint;
    private volatile Barcode mBarcode;

    ScannerGraphic(GraphicOverlay overlay) {
        super(overlay);

        mCurrColorIndex = (mCurrColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrColorIndex];

        mRectPaint = new Paint();
        mRectPaint.setColor(selectedColor);
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setStrokeWidth(5.0f);

        mTextPaint = new Paint();
        mTextPaint.setColor(selectedColor);
        mTextPaint.setTextSize(42.0f);
    }

    public int getmId() {
        return mId;
    }

    public void setmId(int mId) {
        this.mId = mId;
    }

    public Barcode getmBarcode() {
        return mBarcode;
    }

    void updateItem(Barcode barcode) {
        mBarcode = barcode;
        postInvalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        Barcode barcode = mBarcode;
        if (barcode == null) {
            return;
        }

        RectF rect = new RectF(barcode.getBoundingBox());
        rect.left = translateX(rect.left);
        rect.top = translateY(rect.top);
        rect.right = translateX(rect.right);
        rect.bottom = translateX(rect.bottom);
        canvas.drawRect(rect, mRectPaint);

        canvas.drawText(barcode.rawValue, rect.left, rect.bottom, mTextPaint);
    }
}
