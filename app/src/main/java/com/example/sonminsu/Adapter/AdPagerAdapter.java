package com.example.sonminsu.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class AdPagerAdapter extends PagerAdapter {
    private static final int NUM_PAGES = 5;
    private Context context;
    private String adUnitId;

    public AdPagerAdapter(Context context, String adUnitId) {
        this.context = context;
        this.adUnitId = adUnitId;
    }

    @Override
    public int getCount() {
        return NUM_PAGES; // 광고 페이지 수
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        AdView adView = new AdView(context);
        adView.setAdSize(AdSize.SMART_BANNER);
        adView.setAdUnitId(adUnitId);

        container.addView(adView);

        AdRequest adRequest;
        adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        return adView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}

