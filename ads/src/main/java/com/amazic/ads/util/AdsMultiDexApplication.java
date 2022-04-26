package com.amazic.ads.util;

import androidx.multidex.MultiDexApplication;

import java.util.List;

public abstract class AdsMultiDexApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        AdManager.getInstance().initAdManager(this, getListTestDeviceId());
        if (enableAdsResume()) {
            AppOpenManager.getInstance().initAd(this, getOpenAppAdId());
        }
    }

    public abstract boolean enableAdsResume();

    public abstract List<String> getListTestDeviceId();

    public abstract String getOpenAppAdId();
    
}
