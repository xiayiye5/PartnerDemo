package com.google.ads.mediation.sample.customevent.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventBanner;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;
import com.union_test.toutiao.config.TTAdManagerHolder;

import java.util.List;

/**
 * created by wuzejian on 2019-12-02
 */
public class SampleCustomEventBanner implements CustomEventBanner {
    private static final String ADAPTER_NAME = "BannerAdapterForGoogle";

    private String mCodeId = "901121246";

    private TTNativeExpressAd mTTNativeExpressAd;

    private CustomEventBannerListener mCustomEventBannerListener;


    /**
     * @param context
     * @param listener
     * @param serverParameter    服务器附加信息
     * @param size
     * @param mediationAdRequest 包含一些常用的定位信息，可供广告定位时使用
     * @param customEventExtras
     */
    @Override
    public void requestBannerAd(Context context,
                                CustomEventBannerListener listener,
                                String serverParameter,
                                AdSize size,
                                MediationAdRequest mediationAdRequest,
                                Bundle customEventExtras) {
        Log.e(ADAPTER_NAME, " requestBannerAd.-code-------=");

        this.mCustomEventBannerListener = listener;
        TTAdManagerHolder.init(context);
        TTAdManager mTTAdManager = TTAdManagerHolder.get();
        TTAdNative mTTAdNative = mTTAdManager.createAdNative(context.getApplicationContext());

        //服务后台配置获取才使用这个
        //if (serverParameters.containsKey(SAMPLE_AD_UNIT_KEY)) {
//            mCodeId = serverParameters.getString(SAMPLE_AD_UNIT_KEY);
//        } else {
//            mediationBannerListener.onAdFailedToLoad(this, AdRequest.ERROR_CODE_INVALID_REQUEST);
//        }

        int expressViewWidth = 300;
        int expressViewHeight = 240;

        if (customEventExtras != null) {
            expressViewWidth = customEventExtras.getInt("width", 300);
            expressViewHeight = customEventExtras.getInt("height", 240);
            mCodeId = customEventExtras.getString("codeId");
            Log.e(ADAPTER_NAME, " requestBannerAd.mCodeId =" + mCodeId + ",expressViewWidth=" + expressViewWidth + ",expressViewHeight=" + expressViewHeight);
        }

        // Assumes that the serverParameter is the AdUnit for the Sample Network.
        //step4:创建广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(mCodeId) //广告位id
                .setSupportDeepLink(true)
                .setAdCount(1) //请求广告数量为1到3条
                .setExpressViewAcceptedSize(expressViewWidth, expressViewHeight) //期望模板广告view的size,单位dp
                .setImageAcceptedSize(expressViewWidth, expressViewHeight)
                .build();
        //step5:请求广告，对请求回调的广告作渲染处理
        mTTAdNative.loadBannerExpressAd(adSlot, mTTBannerNativeExpressAdListener);
        Log.d(ADAPTER_NAME, "loadBannerExpressAd.....");
    }

    @Override
    public void onDestroy() {
        if (mTTNativeExpressAd != null) {
            mTTNativeExpressAd.destroy();
        }
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onResume() {
    }

    /**
     * banner 广告加载回调监听
     */
    private TTAdNative.NativeExpressAdListener mTTBannerNativeExpressAdListener = new TTAdNative.NativeExpressAdListener() {
        @SuppressLint("LongLogTag")
        @Override
        public void onError(int code, String message) {
            Log.e(ADAPTER_NAME, " onBannerFailed.-code=" + code + "," + message);
            if (mCustomEventBannerListener != null) {
                mCustomEventBannerListener.onAdFailedToLoad(code);
            }
        }

        @Override
        public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
            Log.e(ADAPTER_NAME, " onNativeExpressAdLoad.-code=");

            if (ads == null || ads.size() == 0) {
                return;
            }
            mTTNativeExpressAd = ads.get(0);
            mTTNativeExpressAd.setSlideIntervalTime(30 * 1000);
            mTTNativeExpressAd.setExpressInteractionListener(mExpressAdInteractionListener);
            mTTNativeExpressAd.render();
        }
    };

    /**
     * banner 渲染回调监听
     */
    private TTNativeExpressAd.ExpressAdInteractionListener mExpressAdInteractionListener = new TTNativeExpressAd.ExpressAdInteractionListener() {
        @Override
        public void onAdClicked(View view, int type) {
            if (mCustomEventBannerListener != null) {
                mCustomEventBannerListener.onAdClicked();
            }
        }

        @Override
        public void onAdShow(View view, int type) {
            if (mCustomEventBannerListener != null) {
                mCustomEventBannerListener.onAdOpened();
            }
        }

        @Override
        public void onRenderFail(View view, String msg, int code) {
            Log.e(ADAPTER_NAME, " onBannerFailed.-code=" + code + "," + msg);
            if (mCustomEventBannerListener != null) {
                mCustomEventBannerListener.onAdFailedToLoad(code);
            }
        }

        @Override
        public void onRenderSuccess(View view, float width, float height) {
            Log.e(ADAPTER_NAME, " onRenderSuccess.-code=");
            if (mCustomEventBannerListener != null) {
                //render success add view to google view
                mCustomEventBannerListener.onAdLoaded(view);
            }

        }
    };
}
