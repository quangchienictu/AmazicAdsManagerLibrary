package com.example.admanagerlib;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import com.example.ads.callback.*;
import com.example.ads.util.AdManager;
import com.google.android.gms.ads.LoadAdError;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        AdManager.getInstance().loadSplashInterAds(this,"/6499/example/interstitial",25000,5000, new InterCallback(){
            @Override
            public void onAdClosed() {
                startActivity(new Intent(SplashActivity.this,MainActivity.class));
                finish();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError i) {
                super.onAdFailedToLoad(i);
                startActivity(new Intent(SplashActivity.this,MainActivity.class));
                finish();
            }
        });

    }
}