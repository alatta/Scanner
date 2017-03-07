package com.example.scanner;

import com.example.scanner.camera.GraphicOverlay;
import com.google.android.gms.vision.*;
import com.google.android.gms.vision.barcode.Barcode;

/**
 Created by alatta on 3/2/17.
 */

/**
 For every new barcode this factory will create a new tracker and associate the graphic
 */
public class ScannerTrackerFactory implements MultiProcessor.Factory<Barcode> {
    private GraphicOverlay<ScannerGraphic> mGraphicOverlay;
    private ScanActivity mScanActivity;

    public ScannerTrackerFactory(GraphicOverlay<ScannerGraphic> barcodeGraphicOverlay, ScanActivity scanActivity) {
        mGraphicOverlay = barcodeGraphicOverlay;
        mScanActivity = scanActivity;
    }

    @Override
    public Tracker<Barcode> create(Barcode barcode) {
        ScannerGraphic graphic = new ScannerGraphic(mGraphicOverlay, mScanActivity);
        return new ScannerGraphicTracker(mGraphicOverlay, graphic);
    }

}