package com.example.scanner.adapter;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.*;
import android.widget.ImageView;
import com.example.scanner.R;

import java.util.ArrayList;

/**
 Created by alatta on 3/1/17.
 */

public class BarcodeImagePagerAdapter extends PagerAdapter {

    private ArrayList<Integer> mResources;
    private LayoutInflater mLayoutInflater;
    private Context mContext;

    public BarcodeImagePagerAdapter(Context context, ArrayList<Integer> resources) {
        this.mContext = context;
        this.mResources = resources;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

//    @Override
//    public int getIconResId(int index) {
//        return mResources.get(index % mResources.size());
//    }

    @Override
    public int getCount() {
        return mResources.size();
    }

    @Override
    public Object instantiateItem(ViewGroup view, int position) {
        View imageLayout = mLayoutInflater.inflate(R.layout.pager_item, view, false);

        assert imageLayout != null;
        final ImageView imageView = (ImageView) imageLayout.findViewById(R.id.barcodeView);

        imageView.setImageResource(mResources.get(position));

        view.addView(imageLayout, 0);

        return imageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

}


