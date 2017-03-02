package com.example.scanner.adapter;

import android.support.v4.app.*;
import com.example.scanner.fragment.ScanFragment;

/**
 Created by alatta on 3/1/17.
 */

public class BarcodePagerAdapter extends FragmentPagerAdapter {

    public static final String PAGE = "page";
    public static final String BARCODE_TYPE = "barcode_type";
    private static int NUM_ITEMS = 3;

    public BarcodePagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return ScanFragment.newInstance(0, "DATA_MATRIX");
            case 1:
                return ScanFragment.newInstance(1, "QR");
            case 2:
                return ScanFragment.newInstance(2, "PDF417");
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }
}
