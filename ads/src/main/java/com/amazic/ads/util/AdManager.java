package com.amazic.ads.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.BuildConfig;

import com.amazic.ads.callback.*;
import com.amazic.ads.dialog.*;
import com.amazic.ads.R;
import com.amazic.ads.callback.InterCallback;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class AdManager {
    private LoadingAdsDialog dialog;
    private boolean openActivityAfterShowInterAds = true;
    private int currentClicked = 0;
    private int numShowAds = 3;
    private Handler handlerTimeout;
    private Runnable rdTimeout;
    private int maxClickAds = 100;
    private boolean isShowLoadingSplash = false;
    private Context context;
    AdManagerInterstitialAd mInterstitialSplash;
    boolean isTimeDelay = false; //xử lý delay time show ads, = true mới show ads
    private boolean isTimeout; // xử lý timeout show ads
    private static AdManager INSTANCE;
    public static AdManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AdManager();
        }
        return INSTANCE;
    }
    private static final String TAG = "AdManager";


    public void initAdManager(Context context, List<String> testDeviceList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = Application.getProcessName();
            String packageName = context.getPackageName();
            if (!packageName.equals(processName)) {
                WebView.setDataDirectorySuffix(processName);
            }
        }
        MobileAds.initialize(context, initializationStatus -> {
        });
        MobileAds.setRequestConfiguration(new RequestConfiguration.Builder().setTestDeviceIds(testDeviceList).build());
        this.context = context;
    }


    public void loadBanner(final Activity mActivity, String id) {
        final FrameLayout adContainer = mActivity.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = mActivity.findViewById(R.id.shimmer_container_banner);
        loadBanner(mActivity, id, adContainer, containerShimmer, false);
    }
    private void loadBanner(final Activity mActivity, String id, final FrameLayout adContainer, final ShimmerFrameLayout containerShimmer, Boolean useInlineAdaptive) {
        containerShimmer.setVisibility(View.VISIBLE);
        containerShimmer.startShimmer();
        try {
            AdManagerAdView adView = new AdManagerAdView(mActivity);
            adView.setAdUnitId(id);
            adContainer.addView(adView);
            AdSize adSize = getAdSize(mActivity, useInlineAdaptive);
            adView.setAdSize(adSize);
            adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            adView.loadAd(getAdRequest());
            adView.setAdListener(new AdListener() {

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    containerShimmer.stopShimmer();
                    adContainer.setVisibility(View.GONE);
                    containerShimmer.setVisibility(View.GONE);
                    Log.e("xxx","banner load fall +"+loadAdError.toString());
                }

                @Override
                public void onAdLoaded() {
                    Log.d(TAG, "Banner adapter class name: " + adView.getResponseInfo().getMediationAdapterClassName());
                    containerShimmer.stopShimmer();
                    containerShimmer.setVisibility(View.GONE);
                    adContainer.setVisibility(View.VISIBLE);
                    Log.e("xxx","banner onAdLoaded");
                }

            });


        } catch (Exception e) {
            e.printStackTrace();
            Log.e("xxx","banner Exception"+ e.toString());
        }
    }

    public void loadBannerFragment(final Activity mActivity, String id, final View rootView) {
        final FrameLayout adContainer = rootView.findViewById(R.id.banner_container);
        final ShimmerFrameLayout containerShimmer = rootView.findViewById(R.id.shimmer_container_banner);
        loadBanner(mActivity, id, adContainer, containerShimmer, false);
    }

    private AdSize getAdSize(Activity mActivity, Boolean useInlineAdaptive) {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = mActivity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        if (useInlineAdaptive) {
            return AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(mActivity, adWidth);
        }
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(mActivity, adWidth);

    }
    public AdManagerAdRequest getAdRequest() {
        AdManagerAdRequest.Builder builder = new AdManagerAdRequest.Builder();
        return builder.build();
    }

    private AdManagerAdRequest getAdRequestTimeOut(int timeOut) {
        if(timeOut<5000) timeOut = 5000;
        return (AdManagerAdRequest) new AdManagerAdRequest.Builder().setHttpTimeoutMillis(timeOut).build();
    }



    // inter

    /**
     Load ads inter
     Return 1 inter ads
     */
    public void loadInterAds(Context context, String id, InterCallback adCallback) {
        AdManagerInterstitialAd.load(context, id, getAdRequest(),
                new AdManagerInterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AdManagerInterstitialAd  interstitialAd) {
                        if(adCallback!=null){
                            adCallback.onInterstitialLoad(interstitialAd);
                        }
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i(TAG, loadAdError.getMessage());
                        if(adCallback!=null)
                            adCallback.onAdFailedToLoad(loadAdError);
                    }

                });

    }
    /**
     load and show ads inter
     */
    public void loadAndShowInter(AppCompatActivity activity, String idInter, int timeDelay,int timeOut,InterCallback callback){
        if (!isNetworkConnected()) {
            callback.onAdClosed();
            return;
        }
        if (AppOpenManager.getInstance().isInitialized()) {
            AppOpenManager.getInstance().disableAppResumeWithActivity(activity.getClass());
        }
        dialog = new LoadingAdsDialog(activity);
        dialog.show();
        AdManagerInterstitialAd.load(activity, idInter, getAdRequestTimeOut(timeOut), new AdManagerInterstitialAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                dialog.dismiss();
                callback.onAdFailedToLoad(loadAdError);
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().enableAppResumeWithActivity(activity.getClass());
                }
            }

            @Override
            public void onAdLoaded(@NonNull AdManagerInterstitialAd adManagerInterstitialAd) {
                super.onAdLoaded(adManagerInterstitialAd);
                if(adManagerInterstitialAd!=null){
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        adManagerInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                dialog.dismiss();
                                callback.onAdClosed();
                                if (AppOpenManager.getInstance().isInitialized()) {
                                    AppOpenManager.getInstance().enableAppResumeWithActivity(activity.getClass());
                                }
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                dialog.dismiss();
                                callback.onAdClosed();
                                if (AppOpenManager.getInstance().isInitialized()) {
                                    AppOpenManager.getInstance().enableAppResumeWithActivity(activity.getClass());
                                }
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                Log.d("TAG", "The ad was shown.");
                            }
                        });
                        if (activity.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED) && adManagerInterstitialAd != null) {
                        //   dialog.dismiss();
                            adManagerInterstitialAd.show(activity);
                        } else {
                            if (AppOpenManager.getInstance().isInitialized()) {
                                AppOpenManager.getInstance().enableAppResumeWithActivity(activity.getClass());
                            }
                         //   dialog.dismiss();
                        }
                    },timeDelay);
                }
            }
        });

    }


    /**
     Show ads inter
     */
    public void showInterAds(Context context, AdManagerInterstitialAd mInterstitialAd, final InterCallback callback) {
        showInterAds(context, mInterstitialAd, callback, true);
    }
    private void showInterAds(Context context, AdManagerInterstitialAd mInterstitialAd, final InterCallback callback, boolean shouldReload) {
        currentClicked = numShowAds;
        showInterAdByTimes(context, mInterstitialAd, callback, shouldReload);
    }

    private void showInterAdByTimes(Context context, AdManagerInterstitialAd mInterstitialAd, InterCallback callback, boolean shouldReload) {
        if (mInterstitialAd == null) {
            if (callback != null) {
                callback.onAdClosed();
            }
            return;
        }

        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();
                // Called when fullscreen content is dismissed.
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().enableAppResume();
                }
                if (callback != null) {
                    if (!openActivityAfterShowInterAds) {
                        callback.onAdClosed();
                    }

                    if (dialog != null) {
                        dialog.dismiss();
                    }

                }
                Log.e(TAG, "onAdDismissedFullScreenContent");
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                super.onAdFailedToShowFullScreenContent(adError);
                Log.e(TAG, "onAdFailedToShowFullScreenContent: " + adError.getMessage());
                // Called when fullscreen content failed to show.
                if (callback != null) {
                    if (!openActivityAfterShowInterAds) {
                        callback.onAdClosed();
                    }

                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            }

            @Override
            public void onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent();
            }
        });
        if (Helper.getNumClickAdsPerDay(context, mInterstitialAd.getAdUnitId()) < maxClickAds) {
            showInterstitialAd(context, mInterstitialAd, callback);
            return;
        }
        if (callback != null) {
            callback.onAdClosed();
        }
    }

    private void showInterstitialAd(Context context, AdManagerInterstitialAd mInterstitialAd, InterCallback callback) {
        currentClicked++;
        if (currentClicked >= numShowAds && mInterstitialAd != null) {
            if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                try {
                    if (dialog != null && dialog.isShowing())
                        dialog.dismiss();
                    dialog = new LoadingAdsDialog(context);
                    try {
                        dialog.show();
                    } catch (Exception e) {
                        callback.onAdClosed();
                        return;
                    }
                } catch (Exception e) {
                    dialog = null;
                    e.printStackTrace();
                }
                new Handler().postDelayed(() -> {
                    if (AppOpenManager.getInstance().isInitialized()) {
                        AppOpenManager.getInstance().disableAppResume();
                    }

                    if (openActivityAfterShowInterAds && callback != null) {
                        callback.onAdClosed();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (dialog != null && dialog.isShowing() && !((Activity) context).isDestroyed())
                                    dialog.dismiss();
                            }
                        }, 1500);
                    }

                    mInterstitialAd.show((Activity) context);

                }, 800);

            }
            currentClicked = 0;
        } else if (callback != null) {
            if (dialog != null) {
                dialog.dismiss();
            }
            callback.onAdClosed();
        }
    }



    /* =============================  Native Ads ==========================================*/

    public void loadNativeAd(Context context, String id, final NativeCallback callback) {
        if(isNetworkConnected()){
            VideoOptions videoOptions = new VideoOptions.Builder()
                    .setStartMuted(true)
                    .build();

            NativeAdOptions adOptions = new NativeAdOptions.Builder()
                    .setVideoOptions(videoOptions)
                    .build();
            AdLoader adLoader = new AdLoader.Builder(context, id)
                    .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {

                        @Override
                        public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                            callback.onNativeAdLoaded(nativeAd);
                        }
                    })
                    .withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(LoadAdError error) {
                            Log.e(TAG, "NativeAd onAdFailedToLoad: " + error.getMessage());
                            callback.onAdFailedToLoad();
                        }

                        @Override
                        public void onAdClicked() {
                            super.onAdClicked();
                            if (callback != null) {
                                Log.d(TAG, "onAdClicked");
                            }
                        }
                    })
                    .withNativeAdOptions(adOptions)
                    .build();
            adLoader.loadAd(getAdRequest());
        }else{
            callback.onAdFailedToLoad();
        }

    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public void pushAdsToViewCustom(NativeAd nativeAd, NativeAdView adView) {
        adView.setMediaView(adView.findViewById(R.id.ad_media));
        if (adView.getMediaView() != null) {
            adView.getMediaView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (context != null && BuildConfig.DEBUG) {
                        float sizeMin = TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                120,
                                context.getResources().getDisplayMetrics()
                        );
                        Log.e(TAG, "Native sizeMin: " + sizeMin);
                        Log.e(TAG, "Native w/h media : " + adView.getMediaView().getWidth() + "/" + adView.getMediaView().getHeight());
                        if (adView.getMediaView().getWidth() < sizeMin || adView.getMediaView().getHeight() < sizeMin) {
                            Toast.makeText(context, "Size media native not valid", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }, 1000);

        }
        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline is guaranteed to be in every UnifiedNativeAd.
        try {
            ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        try {
            if (nativeAd.getBody() == null) {
                adView.getBodyView().setVisibility(View.INVISIBLE);
            } else {
                adView.getBodyView().setVisibility(View.VISIBLE);
                ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (nativeAd.getCallToAction() == null) {
                Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.INVISIBLE);
            } else {
                Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.VISIBLE);
                ((TextView) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (nativeAd.getIcon() == null) {
                Objects.requireNonNull(adView.getIconView()).setVisibility(View.GONE);
            } else {
                ((ImageView) adView.getIconView()).setImageDrawable(
                        nativeAd.getIcon().getDrawable());
                adView.getIconView().setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (nativeAd.getPrice() == null) {
                Objects.requireNonNull(adView.getPriceView()).setVisibility(View.INVISIBLE);
            } else {
                Objects.requireNonNull(adView.getPriceView()).setVisibility(View.VISIBLE);
                ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (nativeAd.getStore() == null) {
                Objects.requireNonNull(adView.getStoreView()).setVisibility(View.INVISIBLE);
            } else {
                Objects.requireNonNull(adView.getStoreView()).setVisibility(View.VISIBLE);
                ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (nativeAd.getStarRating() == null) {
                Objects.requireNonNull(adView.getStarRatingView()).setVisibility(View.INVISIBLE);
            } else {
                ((RatingBar) Objects.requireNonNull(adView.getStarRatingView()))
                        .setRating(nativeAd.getStarRating().floatValue());
                adView.getStarRatingView().setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (nativeAd.getAdvertiser() == null) {
                adView.getAdvertiserView().setVisibility(View.INVISIBLE);
            } else {
                ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
                adView.getAdvertiserView().setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad. The SDK will populate the adView's MediaView
        // with the media content from this native ad.
        adView.setNativeAd(nativeAd);

    }




    /**
     * Load ads in Splash
     */
    public void loadSplashInterAds(final Context context, String id, long timeOut, long timeDelay, final InterCallback adListener) {
        isTimeDelay = false;
        isTimeout = false;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //check delay show ad splash
                if (mInterstitialSplash != null) {
                    Log.d(TAG, "loadSplashInterAds:show ad on delay ");
                    onShowSplash((Activity) context, adListener);
                    return;
                }
                Log.d(TAG, "loadSplashInterAds: delay validate");
                isTimeDelay = true;
            }
        }, timeDelay);


        if (timeOut > 0) {
            handlerTimeout = new Handler();
            rdTimeout = new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "loadSplashInterstitalAds: on timeout");
                    isTimeout = true;
                    if (mInterstitialSplash != null) {
                        Log.i(TAG, "loadSplashInterstitalAds:show ad on timeout ");
                        onShowSplash((Activity) context, adListener);
                        return;
                    }
                    if (adListener != null) {
                        adListener.onAdClosed();
                        isShowLoadingSplash = false;
                    }
                }
            };
            handlerTimeout.postDelayed(rdTimeout, timeOut);
        }

        isShowLoadingSplash = true;
        loadInterAds(context, id, new InterCallback() {
            @Override
            public void onInterstitialLoad(AdManagerInterstitialAd interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                Log.e(TAG, "loadSplashInterstitalAds  end time loading success:" + Calendar.getInstance().getTimeInMillis() + "     time limit:" + isTimeout);
                if (isTimeout)
                    return;
                if (interstitialAd != null) {
                    mInterstitialSplash = interstitialAd;
                    if (isTimeDelay) {
                        onShowSplash((Activity) context, adListener);
                        Log.i(TAG, "loadSplashInterstitalAds:show ad on loaded ");
                    }
                }
            }

            @Override
            public void onAdFailedToLoad(LoadAdError i) {
                super.onAdFailedToLoad(i);
                Log.e(TAG, "loadSplashInterstitalAds  end time loading error:" + Calendar.getInstance().getTimeInMillis() + "     time limit:" + isTimeout);
                if (isTimeout)
                    return;
                if (adListener != null) {
                    if (handlerTimeout != null && rdTimeout != null) {
                        handlerTimeout.removeCallbacks(rdTimeout);
                    }
                    if (i != null)
                        Log.e(TAG, "loadSplashInterstitalAds: load fail " + i.getMessage());
                    adListener.onAdFailedToLoad(i);
                }
            }
        });
    }
    private void onShowSplash(Activity activity, InterCallback adListener) {
        isShowLoadingSplash = true;
        if (handlerTimeout != null && rdTimeout != null) {
            handlerTimeout.removeCallbacks(rdTimeout);
        }

        if (adListener != null) {
            adListener.onAdLoaded();
        }

        mInterstitialSplash.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdShowedFullScreenContent() {
                isShowLoadingSplash = false;
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().enableAppResume();
                }
                if (adListener != null) {
                    if (!openActivityAfterShowInterAds) {
                        adListener.onAdClosed();
                    }

                    if (dialog != null) {
                        dialog.dismiss();
                    }

                }
                mInterstitialSplash = null;
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                mInterstitialSplash = null;
                isShowLoadingSplash = false;
                if (adListener != null) {
                    if (!openActivityAfterShowInterAds) {
                        adListener.onAdFailedToShow(adError);
                    }

                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }

            }
        });
        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            try {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
                dialog = new LoadingAdsDialog(activity);
                try {
                    dialog.show();
                } catch (Exception e) {
                    adListener.onAdClosed();
                    return;
                }
            } catch (Exception e) {
                dialog = null;
                e.printStackTrace();
            }
            new Handler().postDelayed(() -> {
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().disableAppResume();
                }

                if (openActivityAfterShowInterAds && adListener != null) {
                    adListener.onAdClosed();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            if (dialog != null && dialog.isShowing() && !activity.isDestroyed())
                                dialog.dismiss();
                        }
                    }, 1500);
                }

                if(activity!=null){
                    mInterstitialSplash.show(activity);
                }
                isShowLoadingSplash = false;
            }, 800);

        }
    }
    public void setOpenActivityAfterShowInterAds(boolean openActivityAfterShowInterAds) {
        this.openActivityAfterShowInterAds = openActivityAfterShowInterAds;
    }

}
