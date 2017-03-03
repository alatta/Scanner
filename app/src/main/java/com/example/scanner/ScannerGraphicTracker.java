package com.example.scanner;

import com.example.scanner.camera.GraphicOverlay;
import com.google.android.gms.vision.*;
import com.google.android.gms.vision.barcode.Barcode;

/**
 Created by alatta on 3/2/17.
 */

public class ScannerGraphicTracker extends Tracker<Barcode> {

    private GraphicOverlay<ScannerGraphic> mOverlay;
    private ScannerGraphic mGraphic;

    ScannerGraphicTracker(GraphicOverlay<ScannerGraphic> overlay, ScannerGraphic graphic) {
        mOverlay = overlay;
        mGraphic = graphic;
    }

    /**
     Start tracking the detected item instance within the item overlay.
     */
    @Override
    public void onNewItem(int id, Barcode item) {
        mGraphic.setmId(id);
    }

    /**
     Update the position if item in overlay
     */
    @Override
    public void onUpdate(Detector.Detections<Barcode> detectionResults, Barcode item) {
        mOverlay.add(mGraphic);
        mGraphic.updateItem(item);
    }

    /**
     hide graphic if object is not visible
     */
    @Override
    public void onMissing(Detector.Detections<Barcode> detectionResults) {
        mOverlay.remove(mGraphic);
    }

    /**
     Remove graphic from overlay
     */
    @Override
    public void onDone() {
        mOverlay.remove(mGraphic);
    }
}
