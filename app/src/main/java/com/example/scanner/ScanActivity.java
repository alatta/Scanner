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
import android.support.v7.widget.*;
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

public class ScanActivity extends AppCompatActivity {

    private static final String TAG = "Scan-Fragment";
    public static final int REQUEST_CODE = 9001;
    private static final int CAMERA_PERM = 2;
    private int format = 0;
    private static final Integer[] imgResources= {R.drawable.datamatrix, R.drawable.qr_code, R.drawable.pdf417};
    private ArrayList<Integer> mResources = new ArrayList<>();
    private CameraSource mCameraSource;
    private CameraSourcePreview mCameraSourcePreview;

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

        for(int i = 0; i < imgResources.length;i++) {
            mResources.add(imgResources[i]);
        }
        mPager.setAdapter(new BarcodeImagePagerAdapter(ScanActivity.this, mResources));
        mPager.setCurrentItem(1);
       // pagerIndicator.setViewPager(pager);

        int reqPerm = ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA);
        if (reqPerm == PackageManager.PERMISSION_GRANTED) {
            buildCameraSource(format);
        } else {
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
            // we have permission, so create the camerasource
            buildCameraSource(format);
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scanner").setMessage("No camera permission granted").setPositiveButton("OK", listener).show();
    }

    private void buildCameraSource(int format) {

        Context context = getApplicationContext();

        BarcodeDetector detector = new BarcodeDetector.Builder(context).build();

        ScannerTrackerFactory scannerTrackerFactory = new ScannerTrackerFactory(mGraphicOverlay);
        detector.setProcessor(new MultiProcessor.Builder<>(scannerTrackerFactory).build());

        CameraSource.Builder builder = new CameraSource.Builder(context, detector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .setRequestedFps(15.0f)
                .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        mCameraSource = builder.build();
    }

    private void startCamera() {

        int playServicesAvailability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this
                .getApplicationContext());

        if (playServicesAvailability != ConnectionResult.SUCCESS) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this,
                                                                               playServicesAvailability,
                                                                               REQUEST_CODE);
            dialog.show();
        }

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
}

