package com.example.scanner.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.Toast;
import com.example.scanner.R;
import com.example.scanner.adapter.BarcodePagerAdapter;

/**
 Created by alatta on 3/2/17.
 */

public class ScanFragment extends Fragment {

    private int page;
    private String barcodeType;

    public static ScanFragment newInstance(int page, String barcodeType) {
        ScanFragment fragment = new ScanFragment();
        Bundle args = new Bundle();
        args.putInt(BarcodePagerAdapter.PAGE, page);
        args.putString(BarcodePagerAdapter.BARCODE_TYPE, barcodeType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt(BarcodePagerAdapter.PAGE);
        barcodeType = getArguments().getString(BarcodePagerAdapter.BARCODE_TYPE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_scan, container, false);
        Toast.makeText(getActivity(), barcodeType, Toast.LENGTH_LONG).show();
        return view;
    }
}
