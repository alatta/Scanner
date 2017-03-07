package com.example.scanner;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.*;
import android.support.annotation.*;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.*;
import android.view.*;
import android.widget.*;
import butterknife.*;
import com.example.scanner.adapter.BarcodeImagePagerAdapter;
import com.example.scanner.camera.*;
import com.example.scanner.camera.CameraSource;
import com.facebook.rebound.*;
import com.google.android.gms.vision.*;
import com.google.android.gms.vision.barcode.*;


import java.io.IOException;
import java.util.ArrayList;

/**
 Created by alatta on 3/2/17.
 */

public class ScanActivity extends AppCompatActivity {

    private static final String TAG = ScanActivity.class.getSimpleName();
    private static final int CAMERA_PERM = 2;
    private static final Integer[] imgResources= {R.drawable.datamatrix, R.drawable.qr_code, R.drawable.pdf417};
    private ArrayList<Integer> mResources = new ArrayList<>();
    private CameraSource mCameraSource;
    private CameraSourcePreview mCameraSourcePreview;
    private boolean mCameraPermissionGranted = false;
    private Spring mSpring;
    private int mBarcodeFormat;
    private BarcodeDetector mBarcodeDetector;

    @BindView(R.id.content) FrameLayout mContent;
    @BindView(R.id.preview) CameraSourcePreview mPreview;
    @BindView(R.id.overlay) GraphicOverlay<ScannerGraphic> mGraphicOverlay;
   // @BindView(R.id.iconIndicator) IconPageIndicator pagerIndicator;
    @BindView(R.id.pager) ViewPager mPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE);
        setContentView(R.layout.activity_scan);
        ButterKnife.bind(this);


        SpringSystem springSystem = SpringSystem.create();
        mSpring = springSystem.createSpring();
        mSpring.addListener(new MySpringListener());

        for (int i = 0; i < imgResources.length; i++) {
            mResources.add(imgResources[i]);
        }

        int cameraPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        } else {
            mCameraPermissionGranted = true;
            buildCameraSource();
        }
        mPager.setAdapter(new BarcodeImagePagerAdapter(ScanActivity.this, mResources));
        mPager.addOnPageChangeListener(new MyPageChangeListener());
        mPager.setCurrentItem(1, true);
        // pagerIndicator.setViewPager(pager);

        mContent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // When pressed start solving the spring to 1.
                        mSpring.setEndValue(1);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // When released start solving the spring to 0.
                        mSpring.setEndValue(0);
                        break;
                }
                return true;
            }
        });
    }


    /**
     Request Camera Permission
     */
    private void requestCameraPermission() {
        Log.d(TAG, "Requesting camera permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, CAMERA_PERM);
            return;
        }

        final Activity scanActivity = this;
        View.OnClickListener permListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(scanActivity, permissions, CAMERA_PERM);
            }
        };

        mContent.setOnClickListener(permListener);
    }

    /**
     Handle Camera Permission request result
     @param requestCode
     @param permissions
     @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so build the camerasource
            mCameraPermissionGranted = true;
            buildCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted:" + " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scanner").setMessage("No camera permission granted").setPositiveButton("OK", listener).show();
    }

    /**
     Builds Camera Source by initializing a new BarcodeDetector instance with specified barcode format, This detector
     receives frames from the CameraSource and will ignore all barcode formats except the specified one.
     */
    private void buildCameraSource() {

        if (mPreview != null) {
            mPreview.stop();
        }

        if (mCameraPermissionGranted) {

            Context context = getApplicationContext();

            mBarcodeDetector = new BarcodeDetector.Builder(context).build();

            ScannerTrackerFactory scannerTrackerFactory = new ScannerTrackerFactory(mGraphicOverlay, this);
            mBarcodeDetector.setProcessor(new MultiProcessor.Builder<>(scannerTrackerFactory).build());

            CameraSource.Builder builder = new CameraSource.Builder(context, mBarcodeDetector)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1600, 1024)
                    .setRequestedFps(15.0f)
                    .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

            mCameraSource = builder.build();
            startCamera();
        }
    }

    /**

     */
    private void startCamera() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    ScanActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() throws SecurityException  {
                            if (mCameraSource != null) {
                                try {
                                    mPreview.start(mCameraSource, mGraphicOverlay);
                                } catch (IOException e) {
                                    Log.e(TAG, "Failed to start camera", e);
                                    mCameraSource.release();
                                    mCameraSource = null;
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Failed to start camera", e);
                }
            }
        }.start();
    }

    public int getmBarcodeFormat() {
        return mBarcodeFormat;
    }

    /**

     */

    @Override
    public void onResume() {
        super.onResume();
        mSpring.addListener(new MySpringListener());
        startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mSpring.removeAllListeners();
        if (mPreview != null) {
            mPreview.stop();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mPreview != null) {
            mPreview.release();
        }
    }

    class MyPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
              switch (position) {
                       case 0:
                            mBarcodeFormat = Barcode.DATA_MATRIX;
                          // mScannerGraphic.setmFormat(Barcode.DATA_MATRIX);
                           //  buildCameraSource(Barcode.DATA_MATRIX);
                           break;
                       case 1:
                           mBarcodeFormat = Barcode.QR_CODE;
                          // mScannerGraphic.F
                           //  buildCameraSource(Barcode.QR_CODE);
                           break;
                       case 2:
                           mBarcodeFormat = Barcode.PDF417;
                         //  mScannerGraphic.setmFormat(Barcode.PDF417);
                           //buildCameraSource(Barcode.PDF417);
                           break;
                       default:
                           break;
                   }
        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    class MySpringListener extends SimpleSpringListener {

        @Override
        public void onSpringUpdate(Spring spring) {
            super.onSpringUpdate(spring);
            float scale = (float) SpringUtil.mapValueFromRangeToRange(spring.getCurrentValue(), 0, 1, 1, 0.5);
            mPager.setScaleX(scale);
            mPager.setScaleY(scale);
        }
    }

}

