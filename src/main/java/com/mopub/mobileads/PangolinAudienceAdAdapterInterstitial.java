package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.mopub.common.logging.MoPubLog;
import com.union_test.toutiao.config.TTAdManagerHolder;
import com.union_test.toutiao.utils.TToast;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.CUSTOM;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.LOAD_FAILED;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.SHOW_ATTEMPTED;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.SHOW_FAILED;

public class PangolinAudienceAdAdapterInterstitial extends CustomEventInterstitial {

    private static final String ADAPTER_NAME = "PangolinAdAdapterInterstitial";

    //for pangolin ad network key
    public final static String EXPRESS_VIEW_WIDTH = "express_view_width";
    public final static String EXPRESS_VIEW_HEIGHT = "express_view_height";
    public final static String EXPRESS_ACTIVITY_PARAM = "activity_param";
    private String mCodeId = "901121133";

    private PangolinAudienceAdAdapterConfiguration mPangolinAudienceAdAdapterConfiguration;

    private CustomEventInterstitialListener mInterstitialListener;

    private TTNativeExpressAd mTTInterstitialExpressAd;

    private Context mContext;
    private Activity mActivity;

    private AtomicBoolean isRenderLoaded = new AtomicBoolean(false);

    public PangolinAudienceAdAdapterInterstitial() {
        mPangolinAudienceAdAdapterConfiguration = new PangolinAudienceAdAdapterConfiguration();
        MoPubLog.log(CUSTOM, ADAPTER_NAME, "PangolinAudienceAdAdapterInterstitial has been create ....");
    }

    @Override
    protected void loadInterstitial(
            final Context context,
            final CustomEventInterstitialListener customEventInterstitialListener,
            final Map<String, Object> localExtras,
            final Map<String, String> serverExtras) {
        mPangolinAudienceAdAdapterConfiguration.setCachedInitializationParameters(context,serverExtras);
        setAutomaticImpressionAndClickTracking(false);
        this.mInterstitialListener = customEventInterstitialListener;
        this.mContext = context;
        //创建TTAdManager
        TTAdManager ttAdManager = TTAdManagerHolder.get();
        TTAdNative ttAdNative = ttAdManager.createAdNative(context.getApplicationContext());

        //获取宽高参数
        float expressViewWidth = 0;
        float expressViewHeight = 0;

        if (localExtras != null && !localExtras.isEmpty()) {
            expressViewWidth = (float) localExtras.get(EXPRESS_VIEW_WIDTH);
            expressViewHeight = (float) localExtras.get(EXPRESS_VIEW_HEIGHT);
            this.mActivity = (Activity) localExtras.get(EXPRESS_ACTIVITY_PARAM);
        }

        if (expressViewWidth <= 0) {
            expressViewWidth = 350;
            expressViewHeight = 0;//0自适应
        }

        if (expressViewHeight < 0) {
            expressViewHeight = 0;
        }

        //step4:创建广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(mCodeId) //广告位id
                .setSupportDeepLink(true)
                .setAdCount(1) //请求广告数量为1到3条
                .setExpressViewAcceptedSize(expressViewWidth, expressViewHeight) //期望模板广告view的size,单位dp
                .build();
        //step5:请求广告，对请求回调的广告作渲染处理
        ttAdNative.loadInteractionExpressAd(adSlot, mInterstitialAdExpressAdListener);

    }

    /**
     * pangolin ad 动态布局插屏请求监听器
     */
    private TTAdNative.NativeExpressAdListener mInterstitialAdExpressAdListener = new TTAdNative.NativeExpressAdListener() {
        @Override
        public void onError(int code, String message) {
            MoPubLog.log(LOAD_FAILED, ADAPTER_NAME, ErrorCode.mapErrorCode(code), message);
            if (mInterstitialListener != null) {
                mInterstitialListener.onInterstitialFailed(ErrorCode.mapErrorCode(code));
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
            TToast.show(mContext, "广告关闭");
            if (mInterstitialListener != null){
                mInterstitialListener.onInterstitialDismissed();
            }
        }

        @Override
        public void onAdClicked(View view, int type) {
            TToast.show(mContext, "广告被点击");
            if (mInterstitialListener != null){
                mInterstitialListener.onInterstitialClicked();
            }
        }

        @Override
        public void onAdShow(View view, int type) {
            TToast.show(mContext, "广告展示");
            if (mInterstitialListener != null){
                mInterstitialListener.onInterstitialImpression();
            }
        }

        @Override
        public void onRenderFail(View view, String msg, int code) {
            TToast.show(mContext, msg + " code:" + code);
            MoPubLog.log(SHOW_FAILED, ADAPTER_NAME,
                    MoPubErrorCode.RENDER_PROCESS_GONE_UNSPECIFIED.getIntCode(),
                    msg);
            if (mInterstitialListener != null) {
                mInterstitialListener.onInterstitialFailed(MoPubErrorCode.RENDER_PROCESS_GONE_UNSPECIFIED);
            }
        }

        @Override
        public void onRenderSuccess(View view, float width, float height) {
            //返回view的宽高 单位 dp
            TToast.show(mContext, "渲染成功");
            isRenderLoaded.set(true);
            if (mInterstitialListener != null) {
                mInterstitialListener.onInterstitialLoaded();
            }
        }
    };


    @Override
    protected void showInterstitial() {
        MoPubLog.log(SHOW_ATTEMPTED, ADAPTER_NAME);
        if (mTTInterstitialExpressAd != null && isRenderLoaded.get()) {
            mTTInterstitialExpressAd.showInteractionExpressAd(mActivity);
        } else {
            MoPubLog.log(SHOW_FAILED, ADAPTER_NAME,
                    MoPubErrorCode.NETWORK_NO_FILL.getIntCode(),
                    MoPubErrorCode.NETWORK_NO_FILL);

            if (mInterstitialListener != null) {
                mInterstitialListener.onInterstitialFailed(MoPubErrorCode.NETWORK_NO_FILL);
            }
        }
    }

    @Override
    protected void onInvalidate() {
        if (mTTInterstitialExpressAd != null) {
            mTTInterstitialExpressAd.setExpressInteractionListener(null);
            mTTInterstitialExpressAd.setVideoAdListener(null);
            mTTInterstitialExpressAd.destroy();
            mTTInterstitialExpressAd = null;
        }
    }

}
