package com.example.scanner;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.*;
import android.support.annotation.*;
import android.support.design.widget.Snackbar;
import android.support.v4.app.*;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.widget.*;
import butterknife.*;
import com.example.scanner.adapter.BarcodeImagePagerAdapter;
import com.example.scanner.camera.*;
import com.google.android.gms.common.*;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.*;


import java.io.IOException;
import java.util.ArrayList;

/**
 Created by alatta on 3/2/17.
 */

public class ScanActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private static final String TAG = "Scan-Fragment";
    public static final int REQUEST_CODE = 9001;
    private static final int CAMERA_PERM = 2;
    private int format = 0;
    private static final Integer[] imgResources= {R.drawable.datamatrix, R.drawable.qr_code, R.drawable.pdf417};
    private ArrayList<Integer> mResources = new ArrayList<>();
    private CameraSource mCameraSource;
    private CameraSourcePreview mCameraSourcePreview;
    private Handler mHandler;


    @BindView(R.id.content) FrameLayout mContent;
    @BindView(R.id.preview) CameraSourcePreview mPreview;
    @BindView(R.id.overlay) GraphicOverlay<ScannerGraphic> mGraphicOverlay;
   // @BindView(R.id.iconIndicator) IconPageIndicator pagerIndicator;
    @BindView(R.id.pager) ViewPager mPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
        setContentView(R.layout.activity_scan);
        ButterKnife.bind(this);

        mHandler = new Handler(getMainLooper());
        for(int i = 0; i < imgResources.length;i++) {
            mResources.add(imgResources[i]);
        }
        mPager.setAdapter(new BarcodeImagePagerAdapter(ScanActivity.this, mResources));
        mPager.addOnPageChangeListener(this);
        mPager.setCurrentItem(1,true);

       // pagerIndicator.setViewPager(pager);

        int reqPerm = ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA);
        if (reqPerm != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        }
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
            buildCameraSource(format);
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

    private void buildCameraSource(int format) {
        if(mCameraSource != null) {
            mCameraSource.stop();
        }

        Context context = getApplicationContext();

        BarcodeDetector mBarCodeDetector = new BarcodeDetector.Builder(context).setBarcodeFormats(format).build();

        ScannerTrackerFactory scannerTrackerFactory = new ScannerTrackerFactory(mGraphicOverlay);
        mBarCodeDetector.setProcessor(new MultiProcessor.Builder<>(scannerTrackerFactory).build());

        CameraSource.Builder builder = new CameraSource.Builder(context, mBarCodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .setRequestedFps(15.0f)
                .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        mCameraSource = builder.build();
        startCamera();

    }

    private void startCamera() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int playServicesAvailability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                                    getApplicationContext());

                            if (playServicesAvailability != ConnectionResult.SUCCESS) {
                                Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getParent(),
                                                                                                   playServicesAvailability,
                                                                                                   REQUEST_CODE);
                                dialog.show();
                            }
                            if (mCameraSource != null) {
                                try {
                                    mPreview.start(mCameraSource, mGraphicOverlay);
                                } catch (IOException | SecurityException e) {
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

//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                if (mCameraSource != null) {
//                    try {
//                        mPreview.start(mCameraSource, mGraphicOverlay);
//                    } catch (IOException e) {
//                        Log.e(TAG, "Failed to start camera", e);
//                        mCameraSource.release();
//                        mCameraSource = null;
//                    }
//                }
//            }
//        };
//        mHandler.post(runnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
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

    private void showSnackBar(String format) {
        Snackbar snackbar = Snackbar.make(mGraphicOverlay, format,
                                          Snackbar.LENGTH_LONG);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
        snackbar.show();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        switch (position) {
            case 0:
                buildCameraSource(Barcode.DATA_MATRIX);
                showSnackBar("Data Matrix");
                break;
            case 1:
                buildCameraSource(Barcode.QR_CODE);
                showSnackBar("QR Code");
                break;
            case 2:
                buildCameraSource(Barcode.PDF417);
                showSnackBar("PDF-417");
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

