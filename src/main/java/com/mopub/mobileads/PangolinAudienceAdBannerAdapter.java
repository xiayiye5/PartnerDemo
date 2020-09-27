package com.mopub.mobileads;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.mopub.common.logging.MoPubLog;
import com.union_test.toutiao.config.TTAdManagerHolder;

import java.util.List;
import java.util.Map;

import static com.mopub.mobileads.ErrorCode.mapErrorCode;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.CUSTOM;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.LOAD_FAILED;

/**
 * created by wuzejian on 2019-11-29
 */
public class PangolinAudienceAdBannerAdapter extends CustomEventBanner {

    private static final String ADAPTER_NAME = "PangolinAudienceAdBanner";


    /**
     * pangolin audience network Mobile Ads rewarded video ad unit ID.
     */
    private String mCodeId = "901121246";

    public final static String EXPRESS_VIEW_WIDTH = "express_view_width";
    public final static String EXPRESS_VIEW_HEIGHT = "express_view_height";

    private PangolinAudienceAdAdapterConfiguration mPangolinAudienceAdAdapterConfiguration;

    private TTNativeExpressAd mTTNativeExpressAd;

    private CustomEventBannerListener customEventBannerListener;

    private Context mContent;

    public PangolinAudienceAdBannerAdapter() {
        mPangolinAudienceAdAdapterConfiguration = new PangolinAudienceAdAdapterConfiguration();
        MoPubLog.log(CUSTOM, ADAPTER_NAME, "PangolinAudienceAdBannerAdapter has been create ....");
    }


    @Override
    protected void loadBanner(Context context, CustomEventBannerListener customEventBannerListener, Map<String, Object> localExtras, Map<String, String> serverExtras) {
        MoPubLog.log(CUSTOM, ADAPTER_NAME, "loadBanner method execute ......");
        mPangolinAudienceAdAdapterConfiguration.setCachedInitializationParameters(context,serverExtras);
        this.customEventBannerListener = customEventBannerListener;
        TTAdManager mTTAdManager = TTAdManagerHolder.get();
        TTAdNative mTTAdNative = mTTAdManager.createAdNative(context.getApplicationContext());
        //获取宽高参数
        float expressViewWidth = 0;
        float expressViewHeight = 350;

        if (localExtras != null && !localExtras.isEmpty()) {
            expressViewWidth = (float) localExtras.get(EXPRESS_VIEW_WIDTH);
            expressViewHeight = (float) localExtras.get(EXPRESS_VIEW_HEIGHT);
        }

        if (expressViewWidth <= 0) {
            expressViewWidth = 350;
            expressViewHeight = 0;//0自适应
        }

        if (expressViewHeight < 0) {
            expressViewHeight = 0;
        }
        MoPubLog.log(CUSTOM, ADAPTER_NAME, "expressViewHeight ="+expressViewHeight +"，expressViewWidth="+expressViewWidth);

        //step4:创建广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(mCodeId) //广告位id
                .setSupportDeepLink(true)
                .setAdCount(1) //请求广告数量为1到3条
                .setExpressViewAcceptedSize(expressViewWidth, expressViewHeight) //期望模板广告view的size,单位dp
                .setImageAcceptedSize((int) expressViewWidth, (int) expressViewHeight)
                .build();
        //step5:请求广告，对请求回调的广告作渲染处理
        mTTAdNative.loadBannerExpressAd(adSlot, mTTNativeExpressAdListener);

    }

    /**
     * banner 广告加载回调监听
     */
    private TTAdNative.NativeExpressAdListener mTTNativeExpressAdListener = new TTAdNative.NativeExpressAdListener() {
        @SuppressLint("LongLogTag")
        @Override
        public void onError(int code, String message) {
            Log.e(ADAPTER_NAME, "MoPubView onBannerFailed.-code=" + code + "," + message);
            if (customEventBannerListener != null) {
                customEventBannerListener.onBannerFailed(mapErrorCode(code));
            }
            MoPubLog.log(LOAD_FAILED, ADAPTER_NAME,
                    MoPubErrorCode.NETWORK_NO_FILL.getIntCode(),
                    MoPubErrorCode.NETWORK_NO_FILL);
        }

        @Override
        public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
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
            if (customEventBannerListener != null) {
                customEventBannerListener.onBannerClicked();
            }
        }

        @Override
        public void onAdShow(View view, int type) {
            if (customEventBannerListener != null) {
                customEventBannerListener.onBannerImpression();
            }
        }

        @Override
        public void onRenderFail(View view, String msg, int code) {
            if (customEventBannerListener != null) {
                customEventBannerListener.onBannerFailed(MoPubErrorCode.RENDER_PROCESS_GONE_UNSPECIFIED);
            }
            MoPubLog.log(LOAD_FAILED, ADAPTER_NAME,
                    MoPubErrorCode.RENDER_PROCESS_GONE_UNSPECIFIED.getIntCode(),
                    MoPubErrorCode.RENDER_PROCESS_GONE_UNSPECIFIED);
        }

        @Override
        public void onRenderSuccess(View view, float width, float height) {
            if (customEventBannerListener != null) {
                //render success add view to mMoPubView
                customEventBannerListener.onBannerLoaded(view);
            }
        }
    };


    @Override
    protected void onInvalidate() {
        if (mTTNativeExpressAd != null) {
            mTTNativeExpressAd.destroy();
            mTTNativeExpressAd = null;
        }
    }


}
