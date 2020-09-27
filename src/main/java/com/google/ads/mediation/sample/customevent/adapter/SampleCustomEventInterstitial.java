package com.google.ads.mediation.sample.customevent.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitial;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitialListener;
import com.union_test.toutiao.config.TTAdManagerHolder;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * created by wuzejian on 2019-12-03
 */
public class SampleCustomEventInterstitial implements CustomEventInterstitial {

    private static final String ADAPTER_NAME = "AdapterForGoogle";
    private static final String SAMPLE_AD_UNIT_KEY = "ad_unit";
    private String mCodeId = "901121133";
    private CustomEventInterstitialListener mCustomEventInterstitialListener;
    private TTNativeExpressAd mTTInterstitialExpressAd;
    private Activity mActivity;
    private AtomicBoolean isRenderLoaded = new AtomicBoolean(false);

    @Override
    public void requestInterstitialAd(Context context,
                                      CustomEventInterstitialListener listener,
                                      String serverParameter,
                                      MediationAdRequest mediationAdRequest,
                                      Bundle customEventExtras) {

        Log.e(ADAPTER_NAME, " requestInterstitialAd.-requestInterstitialAd=");
        if (context instanceof Activity) {
            Log.e(ADAPTER_NAME, " requestInterstitialAd.-context is Activity ");
            mActivity = (Activity) context;
        }
        mCustomEventInterstitialListener = listener;
        TTAdManagerHolder.init(context);
        TTAdManager mTTAdManager = TTAdManagerHolder.get();
        TTAdNative mTTAdNative = mTTAdManager.createAdNative(context.getApplicationContext());
        //服务后台配置获取才使用这个
        //if (serverParameters.containsKey(SAMPLE_AD_UNIT_KEY)) {
//            mCodeId = serverParameters.getString(SAMPLE_AD_UNIT_KEY);
//        } else {
//            mediationBannerListener.onAdFailedToLoad(this, AdRequest.ERROR_CODE_INVALID_REQUEST);
//        }

        int expressViewWidth = 350;
        int expressViewHeight = 0;

        if (customEventExtras != null) {
            expressViewWidth = customEventExtras.getInt("width", 300);
//            expressViewHeight = customEventExtras.getInt("height", 240);
            mCodeId = customEventExtras.getString("codeId");
            Log.e(ADAPTER_NAME, " requestInterstitialAd.mCodeId =" + mCodeId + ",expressViewWidth=" + expressViewWidth + ",expressViewHeight=" + expressViewHeight);
        }

        //step4:创建广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(mCodeId) //广告位id
                .setSupportDeepLink(true)
                .setAdCount(1) //请求广告数量为1到3条
                .setExpressViewAcceptedSize(expressViewWidth, expressViewHeight) //期望模板广告view的size,单位dp
                .build();
        //step5:请求广告，对请求回调的广告作渲染处理
        mTTAdNative.loadInteractionExpressAd(adSlot, mInterstitialAdExpressAdListener);
        Log.e(ADAPTER_NAME, " requestInterstitialAd.-loadInteractionExpressAd=");

    }

    @Override
    public void showInterstitial() {
        if (mTTInterstitialExpressAd != null && isRenderLoaded.get() && mActivity != null) {
            mTTInterstitialExpressAd.showInteractionExpressAd(mActivity);
        }
    }

    @Override
    public void onDestroy() {
        if (mTTInterstitialExpressAd != null) {
            mTTInterstitialExpressAd.destroy();
        }
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onResume() {
    }


    /**
     * pangolin ad 动态布局插屏请求监听器
     */
    private TTAdNative.NativeExpressAdListener mInterstitialAdExpressAdListener = new TTAdNative.NativeExpressAdListener() {
        @Override
        public void onError(int code, String message) {
            if (mCustomEventInterstitialListener != null) {
                mCustomEventInterstitialListener.onAdFailedToLoad(code);
            }
        }

        @Override
        public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
            if (ads == null || ads.size() == 0) {
                return;
            }
            mTTInterstitialExpressAd = ads.get(0);
            mTTInterstitialExpressAd.setSlideIntervalTime(30 * 1000);
            mTTInterstitialExpressAd.setExpressInteractionListener(mInterstitialExpressAdInteractionListener);
            mTTInterstitialExpressAd.render();
        }
    };

    /**
     * 渲染回调监听器
     */
    private TTNativeExpressAd.AdInteractionListener mInterstitialExpressAdInteractionListener = new TTNativeExpressAd.AdInteractionListener() {
        @Override
        public void onAdDismiss() {
            if (mCustomEventInterstitialListener != null) {
                mCustomEventInterstitialListener.onAdClosed();
            }
        }

        @Override
        public void onAdClicked(View view, int type) {
            if (mCustomEventInterstitialListener != null) {
                mCustomEventInterstitialListener.onAdClicked();
            }
        }

        @Override
        public void onAdShow(View view, int type) {
            if (mCustomEventInterstitialListener != null) {
                mCustomEventInterstitialListener.onAdOpened();
            }
        }

        @Override
        public void onRenderFail(View view, String msg, int code) {
            if (mCustomEventInterstitialListener != null) {
                mCustomEventInterstitialListener.onAdFailedToLoad(code);
            }
        }

        @Override
        public void onRenderSuccess(View view, float width, float height) {
            //返回view的宽高 单位 dp
            isRenderLoaded.set(true);
            if (mCustomEventInterstitialListener != null) {
                mCustomEventInterstitialListener.onAdLoaded();
            }
        }
    };
}
