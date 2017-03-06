package com.example.scanner;

import android.app.Application;
import com.squareup.leakcanary.LeakCanary;

/**
 Created by alatta on 3/6/17.
 */

public class ScannerApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if(LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }
}
