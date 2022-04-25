package com.example.admanagerlib;

import android.app.Application;
import com.example.ads.util.*;
import java.util.List;

public class App extends AdsApplication {
    public App(){
        AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity.class);
    }
    @Override
    public boolean enableAdsResume() {
        return true;
    }

    @Override
    public List<String> getListTestDeviceId() {
        return null;
    }

    @Override
    public String getResumeAdId() {
        return "ca-app-pub-3940256099942544/3419835294";
    }
}
