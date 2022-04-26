package com.example.admanagerlib;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.example.ads.util.AdManager;
import com.example.ads.callback.*;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

public class MainActivity extends AppCompatActivity {
    private AdManagerInterstitialAd adManagerInterstitialAd;
    FrameLayout fr_ads1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AdManager.getInstance().loadBanner(this, "/6499/example/banner");
        loadInter();


        findViewById(R.id.clickInter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInter();
            }
        });

        findViewById(R.id.loadAndShow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AdManager.getInstance().loadAndShowInter(MainActivity.this, "/6499/example/interstitial",0,5000,new InterCallback(){
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        startActivity(new Intent(MainActivity.this,MainActivity2.class));
                    }
                    @Override
                    public void onAdFailedToLoad(LoadAdError i) {
                        super.onAdFailedToLoad(i);
                        startActivity(new Intent(MainActivity.this,MainActivity2.class));
                    }

                    @Override
                    public void onAdFailedToShow(AdError adError) {
                        super.onAdFailedToShow(adError);
                        startActivity(new Intent(MainActivity.this,MainActivity2.class));
                    }
                });
            }
        });

        fr_ads1 = findViewById(R.id.fr_ads1);
        AdManager.getInstance().loadNativeAd(this, "/6499/example/native", new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                NativeAdView adView = (NativeAdView) LayoutInflater.from(MainActivity.this).inflate(R.layout.ads_native, null);
                fr_ads1.removeAllViews();
                fr_ads1.addView(adView);
                AdManager.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }

            @Override
            public void onAdFailedToLoad() {
                fr_ads1.removeAllViews();
            }
        });



    }

    public void showInter(){
        AdManager.getInstance().showInterAds(MainActivity.this, adManagerInterstitialAd, new InterCallback() {
            @Override
            public void onAdClosed() {
                startActivity(new Intent(MainActivity.this,MainActivity2.class));
                loadInter();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError i) {
                startActivity(new Intent(MainActivity.this,MainActivity2.class));
                loadInter();
            }

        });
    }

    public void loadInter(){
        AdManager.getInstance().loadInterAds(this, "/6499/example/interstitial", new InterCallback() {
            @Override
            public void onInterstitialLoad(AdManagerInterstitialAd interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                adManagerInterstitialAd = interstitialAd;
            }
        });
    }





}