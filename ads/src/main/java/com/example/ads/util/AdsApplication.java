package com.example.ads.util;
import android.app.Application;
import java.util.List;

public abstract class AdsApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
       AdManager.getInstance().initAdManager(this, getListTestDeviceId());
        if(enableAdsResume()) {
            AppOpenManager.getInstance().initAd(this, getResumeAdId());
        }
    }
    public abstract boolean enableAdsResume();

    public abstract List<String> getListTestDeviceId();

    public abstract String getResumeAdId();
}
