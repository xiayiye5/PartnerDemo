package com.mopub.mobileads;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;
import com.mopub.common.LifecycleListener;
import com.mopub.common.MoPubReward;
import com.mopub.common.logging.MoPubLog;
import com.union_test.toutiao.config.TTAdManagerHolder;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mopub.mobileads.ErrorCode.mapErrorCode;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.CUSTOM;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.LOAD_ATTEMPTED;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.LOAD_FAILED;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.LOAD_SUCCESS;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.SHOW_ATTEMPTED;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.SHOW_FAILED;

/**
 * created by wuzejian on 2019/11/29
 */
@SuppressLint("LongLogTag")
public class PangolinAudienceAdRewardedVideoAdapter extends CustomEventRewardedVideo {
    private static final String ADAPTER_NAME = "PangolinAdapter";

    /**
     * Flag to determine whether or not the adapter has been initialized.
     */
    private static AtomicBoolean sIsInitialized;

    /**
     * Pangolin audience network
     */
    private String mCodeId = "901121593";//TTAdConstant.VERTICAL

    private PangolinAudienceAdAdapterConfiguration mPangolinAudienceAdAdapterConfiguration;

    private WeakReference<Activity> mWeakActivity;

    private TTRewardVideoAd mTTRewardVideoAd;

    private static final String TAG = "MopubRewardedVideoActivity";

    public PangolinAudienceAdRewardedVideoAdapter() {
        sIsInitialized = new AtomicBoolean(false);
        mPangolinAudienceAdAdapterConfiguration = new PangolinAudienceAdAdapterConfiguration();
        MoPubLog.log(CUSTOM, ADAPTER_NAME, "PangolinAudienceAdRewardedVideoAdapter has been create ....");
    }


    @Override
    protected boolean hasVideoAvailable() {
        return mTTRewardVideoAd != null && mIsLoaded;
    }

    @Override
    protected void showVideo() {
        MoPubLog.log(SHOW_ATTEMPTED, ADAPTER_NAME);
        if (hasVideoAvailable() && mWeakActivity != null && mWeakActivity.get() != null) {
            mTTRewardVideoAd.setRewardAdInteractionListener(mRewardAdInteractionListener);
            mTTRewardVideoAd.showRewardVideoAd(mWeakActivity.get());
        } else {
            MoPubLog.log(SHOW_FAILED, ADAPTER_NAME,
                    MoPubErrorCode.NETWORK_NO_FILL.getIntCode(),
                    MoPubErrorCode.NETWORK_NO_FILL);

            MoPubRewardedVideoManager.onRewardedVideoPlaybackError(
                    PangolinAudienceAdRewardedVideoAdapter.class,
                    getAdNetworkId(), MoPubErrorCode.NETWORK_NO_FILL);
        }
    }


    @Nullable
    @Override
    protected LifecycleListener getLifecycleListener() {
        return null;
    }

    /**
     * 初始化穿山甲SDK
     *
     * @param launcherActivity
     * @param localExtras
     * @param serverExtras     MoPub 管理后台配置的json信息串
     * @return
     * @throws Exception
     */
    @Override
    protected boolean checkAndInitializeSdk(@NonNull Activity launcherActivity, @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) throws Exception {
        if (MoPubLog.getLogLevel() == MoPubLog.LogLevel.DEBUG) {
            Set<Map.Entry<String, Object>> set = localExtras.entrySet();
            //本地的配置参数包括返回结果response中的参数
            for (Map.Entry<String, Object> entry : set) {
                Log.d(TAG, "localExtras => key=" + entry.getKey() + ",value=" + entry.getValue());
                MoPubLog.log(CUSTOM, ADAPTER_NAME, "localExtras => key=" + entry.getKey() + ",value=" + entry.getValue());
            }

            //MoPub 管理后台配置的json信息串
            Set<Map.Entry<String, String>> set2 = serverExtras.entrySet();
            for (Map.Entry<String, String> entry : set2) {
                Log.d(TAG, "serverExtras set2 => key=" + entry.getKey() + ",value=" + entry.getValue());
                MoPubLog.log(CUSTOM, ADAPTER_NAME, "serverExtras => key=" + entry.getKey() + ",value=" + entry.getValue());
            }
        }

        if (!sIsInitialized.getAndSet(true)) {
            TTAdManagerHolder.init(launcherActivity.getApplicationContext());
            mPangolinAudienceAdAdapterConfiguration.setCachedInitializationParameters(launcherActivity, serverExtras);
            return true;
        }

        return false;
    }

    //final Map<String, Object> localExtras = new TreeMap<>();
    //     *             localExtras.put(DataKeys.AD_UNIT_ID_KEY, adUnitId);
    //     *             localExtras.put(DataKeys.REWARDED_AD_CURRENCY_NAME_KEY,
    //     *                     adResponse.getRewardedVideoCurrencyName());
    //     *             localExtras.put(DataKeys.REWARDED_AD_CURRENCY_AMOUNT_STRING_KEY,
    //     *                     adResponse.getRewardedVideoCurrencyAmount());
    //     *             localExtras.put(DataKeys.REWARDED_AD_DURATION_KEY,
    //     *                     adResponse.getRewardedDuration());
    //     *             localExtras.put(DataKeys.SHOULD_REWARD_ON_CLICK_KEY,
    //     *                     adResponse.shouldRewardOnClick());
    //     *             localExtras.put(DataKeys.AD_REPORT_KEY,
    //     *                     new AdReport(adUnitId, ClientMetadata.getInstance(mContext), adResponse));
    //     *             localExtras.put(DataKeys.BROADCAST_IDENTIFIER_KEY, Utils.generateUniqueId());
    //     *
    //     *             localExtras.put(DataKeys.REWARDED_AD_CUSTOMER_ID_KEY,
    //     *                     mRewardedAdData.getCustomerId());

    /**
     * 加载激励视频广告
     *
     * @param activity
     * @param localExtras
     * @param serverExtras MoPub 后台手动设置的 json 参数
     * @throws Exception
     */
    @Override
    protected void loadWithSdkInitialized(@NonNull Activity activity, @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) throws Exception {
        MoPubLog.log(CUSTOM, ADAPTER_NAME, "loadWithSdkInitialized method execute ......");
        mWeakActivity = new WeakReference<>(activity);
        TTAdManager mTTAdManager = TTAdManagerHolder.get();
        TTAdNative mTTAdNative = mTTAdManager.createAdNative(activity.getApplicationContext());
        //step4:Create a parameter AdSlot for reward ad request type,
        //      refer to the document for meanings of specific parameters
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(mCodeId)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(1080, 1920)
                .setRewardName("gold coin") //Parameter for rewarded video ad requests, name of the reward
                .setRewardAmount(3)  // The number of rewards in rewarded video ad
                .setUserID("user123")//User ID, a required parameter for rewarded video ads
                .setMediaExtra("media_extra") //optional parameter
                .setOrientation(TTAdConstant.VERTICAL) //Set how you wish the video ad to be displayed, choose from TTAdConstant.HORIZONTAL or TTAdConstant.VERTICAL
                .build();

        //load ad
        mTTAdNative.loadRewardVideoAd(adSlot, mLoadRewardVideoAdListener);
        MoPubLog.log(getAdNetworkId(), LOAD_ATTEMPTED, ADAPTER_NAME);
    }

    @NonNull
    @Override
    protected String getAdNetworkId() {
        return mCodeId;
    }

    @Override
    protected void onInvalidate() {
        if (mTTRewardVideoAd != null) {
            MoPubLog.log(CUSTOM, ADAPTER_NAME, "Performing cleanup tasks...");
            mTTRewardVideoAd = null;
        }
    }

    /**
     * Flag to determine whether or not the Google Rewarded Video Ad instance has loaded.
     */
    private boolean mIsLoaded;

    private TTAdNative.RewardVideoAdListener mLoadRewardVideoAdListener = new TTAdNative.RewardVideoAdListener() {

        @Override
        public void onError(int code, String message) {
            MoPubRewardedVideoManager.onRewardedVideoLoadFailure(PangolinAudienceAdRewardedVideoAdapter.class, getAdNetworkId(), mapErrorCode(code));
            MoPubLog.log(LOAD_FAILED, ADAPTER_NAME, "Loading Rewarded Video creative encountered an error: " + mapErrorCode(code).toString() + ",error message:" + message);
        }

        @Override
        public void onRewardVideoAdLoad(TTRewardVideoAd ad) {
            MoPubLog.log(LOAD_SUCCESS, ADAPTER_NAME, " TTRewardVideoAd ：" + ad);
            if (ad != null) {
                mIsLoaded = true;
                mTTRewardVideoAd = ad;
                MoPubLog.log(LOAD_SUCCESS, ADAPTER_NAME);
                MoPubRewardedVideoManager.onRewardedVideoLoadSuccess(
                        PangolinAudienceAdRewardedVideoAdapter.class,
                        getAdNetworkId());
            } else {
                MoPubRewardedVideoManager.onRewardedVideoLoadFailure(PangolinAudienceAdRewardedVideoAdapter.class, getAdNetworkId(), MoPubErrorCode.NETWORK_NO_FILL);
                MoPubLog.log(LOAD_FAILED, ADAPTER_NAME, " TTRewardVideoAd is null !");
            }
        }

        @Override
        public void onRewardVideoCached() {
            MoPubLog.log(CUSTOM, ADAPTER_NAME, "TTRewardVideoAd onRewardVideoCached...");
            if (mWeakActivity != null && mWeakActivity.get() != null)
                Toast.makeText(mWeakActivity.get(), "onRewardVideoCached.....", Toast.LENGTH_SHORT).show();
        }
    };

    private TTRewardVideoAd.RewardAdInteractionListener mRewardAdInteractionListener = new TTRewardVideoAd.RewardAdInteractionListener() {
        @Override
        public void onAdShow() {
            MoPubRewardedVideoManager.onRewardedVideoStarted(PangolinAudienceAdRewardedVideoAdapter.class, mCodeId);
            if (mWeakActivity != null && mWeakActivity.get() != null)
                Toast.makeText(mWeakActivity.get(), "TTRewardVideoAd onAdShow.....", Toast.LENGTH_SHORT).show();
            MoPubLog.log(CUSTOM, ADAPTER_NAME, "TTRewardVideoAd onAdShow...");
        }

        @Override
        public void onAdVideoBarClick() {
            MoPubRewardedVideoManager.onRewardedVideoClicked(PangolinAudienceAdRewardedVideoAdapter.class, mCodeId);
            if (mWeakActivity != null && mWeakActivity.get() != null)
                Toast.makeText(mWeakActivity.get(), "TTRewardVideoAd onAdVideoBarClick.....", Toast.LENGTH_SHORT).show();
            MoPubLog.log(CUSTOM, ADAPTER_NAME, "TTRewardVideoAd onAdVideoBarClick...");
        }

        @Override
        public void onAdClose() {
            MoPubRewardedVideoManager.onRewardedVideoClosed(PangolinAudienceAdRewardedVideoAdapter.class, mCodeId);
            if (mWeakActivity != null && mWeakActivity.get() != null)
                Toast.makeText(mWeakActivity.get(), "TTRewardVideoAd onAdClose.....", Toast.LENGTH_SHORT).show();
            MoPubLog.log(CUSTOM, ADAPTER_NAME, "TTRewardVideoAd onAdClose...");
        }

        @Override
        public void onVideoComplete() {
            MoPubRewardedVideoManager.onRewardedVideoCompleted(PangolinAudienceAdRewardedVideoAdapter.class, mCodeId, MoPubReward.success(MoPubReward.NO_REWARD_LABEL, MoPubReward.DEFAULT_REWARD_AMOUNT));
            if (mWeakActivity != null && mWeakActivity.get() != null)
                Toast.makeText(mWeakActivity.get(), "TTRewardVideoAd onVideoComplete.....", Toast.LENGTH_SHORT).show();
            MoPubLog.log(CUSTOM, ADAPTER_NAME, "TTRewardVideoAd onVideoComplete...");
        }

        @Override
        public void onVideoError() {
            MoPubRewardedVideoManager.onRewardedVideoPlaybackError(PangolinAudienceAdRewardedVideoAdapter.class, mCodeId, MoPubErrorCode.UNSPECIFIED);
            if (mWeakActivity != null && mWeakActivity.get() != null)
                Toast.makeText(mWeakActivity.get(), "TTRewardVideoAd onVideoError.....", Toast.LENGTH_SHORT).show();
            MoPubLog.log(CUSTOM, ADAPTER_NAME, "TTRewardVideoAd onVideoError...");
        }

        @Override
        public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName) {
            if (mWeakActivity != null && mWeakActivity.get() != null)
                Toast.makeText(mWeakActivity.get(), "TTRewardVideoAd onRewardVerify.....", Toast.LENGTH_SHORT).show();
            MoPubLog.log(CUSTOM, ADAPTER_NAME, "TTRewardVideoAd onRewardVerify...rewardVerify：" + rewardVerify + "，rewardAmount=" + rewardAmount + "，rewardName=" + rewardName);
        }

        @Override
        public void onSkippedVideo() {
            //mopub 没有此方法
            if (mWeakActivity != null && mWeakActivity.get() != null)
                Toast.makeText(mWeakActivity.get(), "TTRewardVideoAd onSkippedVideo.....", Toast.LENGTH_SHORT).show();
            MoPubLog.log(CUSTOM, ADAPTER_NAME, "TTRewardVideoAd onSkippedVideo...");
        }
    };


}
