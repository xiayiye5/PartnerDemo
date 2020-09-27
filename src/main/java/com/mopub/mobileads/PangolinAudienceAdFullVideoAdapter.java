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
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;

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
public class PangolinAudienceAdFullVideoAdapter extends CustomEventRewardedVideo {
    private static final String TAG= "MopubFullVideoActivity";

    private static final String ADAPTER_NAME = "PangolinAudienceAdFullVideoAdapter";

    /**
     * Flag to determine whether or not the adapter has been initialized.
     */
    private static AtomicBoolean sIsInitialized;

    /**
     * pangolin audience network Mobile Ads rewarded video ad unit ID.
     */
    private String mCodeId = "901121073";//TTAdConstant.VERTICAL

    private PangolinAudienceAdAdapterConfiguration mPangolinAudienceAdAdapterConfiguration;

    private WeakReference<Activity> mWeakActivity;

    private TTFullScreenVideoAd mTTFullScreenVideoAd;


    public PangolinAudienceAdFullVideoAdapter() {
        sIsInitialized = new AtomicBoolean(false);
        mPangolinAudienceAdAdapterConfiguration = new PangolinAudienceAdAdapterConfiguration();
        Log.d(TAG,"PangolinAudienceAdFullVideoAdapter has been create ....");
        MoPubLog.log(CUSTOM, ADAPTER_NAME, "PangolinAudienceAdFullVideoAdapter has been create ....");
    }


    @Override
    protected boolean hasVideoAvailable() {
        return mTTFullScreenVideoAd != null && mIsLoaded;
    }

    @Override
    protected void showVideo() {
        MoPubLog.log(SHOW_ATTEMPTED, ADAPTER_NAME);
        if (hasVideoAvailable() && mWeakActivity != null && mWeakActivity.get() != null) {
            mTTFullScreenVideoAd.setFullScreenVideoAdInteractionListener(mFullScreenVideoAdInteractionListener);
            mTTFullScreenVideoAd.showFullScreenVideoAd(mWeakActivity.get());
        } else {
            MoPubLog.log(SHOW_FAILED, ADAPTER_NAME,
                    MoPubErrorCode.NETWORK_NO_FILL.getIntCode(),
                    MoPubErrorCode.NETWORK_NO_FILL);

            MoPubRewardedVideoManager.onRewardedVideoPlaybackError(
                    PangolinAudienceAdFullVideoAdapter.class,
                    getAdNetworkId(), MoPubErrorCode.NETWORK_NO_FILL);
        }
    }


    @Nullable
    @Override
    protected LifecycleListener getLifecycleListener() {
        return null;
    }

    @Override
    protected boolean checkAndInitializeSdk(@NonNull Activity launcherActivity, @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) throws Exception {
        if (MoPubLog.getLogLevel() == MoPubLog.LogLevel.DEBUG) {
            Set<Map.Entry<String, Object>> set = localExtras.entrySet();
            for (Map.Entry<String, Object> entry : set) {
                MoPubLog.log(CUSTOM, ADAPTER_NAME, "localExtras => key=" + entry.getKey() + ",value=" + entry.getValue());
            }

            Set<Map.Entry<String, String>> set2 = serverExtras.entrySet();
            for (Map.Entry<String, String> entry : set2) {
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

    @Override
    protected void loadWithSdkInitialized(@NonNull Activity activity, @NonNull Map<String, Object> localExtras, @NonNull Map<String, String> serverExtras) throws Exception {
        MoPubLog.log(CUSTOM, ADAPTER_NAME, "loadWithSdkInitialized method execute ......");
        mWeakActivity = new WeakReference<>(activity);
        TTAdManager mTTAdManager = TTAdManagerHolder.get();
        TTAdNative mTTAdNative = mTTAdManager.createAdNative(activity.getApplicationContext());
        //Create a parameter AdSlot for reward ad request type,
        //refer to the document for meanings of specific parameters
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(mCodeId)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(1080, 1920)
                .setOrientation(TTAdConstant.VERTICAL)//required parameter ，Set how you wish the video ad to be displayed ,choose from TTAdConstant.HORIZONTAL 或 TTAdConstant.VERTICAL
                .build();
        //load ad
        mTTAdNative.loadFullScreenVideoAd(adSlot, mLoadFullVideoAdListener);
        MoPubLog.log(getAdNetworkId(), LOAD_ATTEMPTED, ADAPTER_NAME);
    }

    @NonNull
    @Override
    protected String getAdNetworkId() {
        return mCodeId;
    }

    @Override
    protected void onInvalidate() {
        if (mTTFullScreenVideoAd != null) {
            MoPubLog.log(CUSTOM, ADAPTER_NAME, "Performing cleanup tasks...");
            mTTFullScreenVideoAd = null;
        }
    }

    /**
     * Flag to determine whether or not the Pangolin Rewarded Video Ad instance has loaded.
     */
    private boolean mIsLoaded;

    private TTAdNative.FullScreenVideoAdListener mLoadFullVideoAdListener = new TTAdNative.FullScreenVideoAdListener() {
        @Override
        public void onError(int code, String message) {
            MoPubRewardedVideoManager.onRewardedVideoLoadFailure(PangolinAudienceAdFullVideoAdapter.class, getAdNetworkId(), mapErrorCode(code));
            Log.d(TAG,"Loading Full Video creative encountered an error: " + mapErrorCode(code).toString() + ",error message:" + message);
            MoPubLog.log(LOAD_FAILED, ADAPTER_NAME, "Loading Full Video creative encountered an error: " + mapErrorCode(code).toString() + ",error message:" + message);
        }

        @Override
        public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ad) {
            Log.d(TAG,"onFullScreenVideoAdLoad method execute ......ad = " + ad);
            if (ad != null) {
                mIsLoaded = true;
                mTTFullScreenVideoAd = ad;
                MoPubLog.log(LOAD_SUCCESS, ADAPTER_NAME);
                MoPubRewardedVideoManager.onRewardedVideoLoadSuccess(
                        PangolinAudienceAdFullVideoAdapter.class,
                        getAdNetworkId());
                if (mWeakActivity != null && mWeakActivity.get() != null)
                    Toast.makeText(mWeakActivity.get(), "The FullScreenVideo load success.....", Toast.LENGTH_SHORT).show();
            } else {
                MoPubRewardedVideoManager.onRewardedVideoPlaybackError(
                        PangolinAudienceAdFullVideoAdapter.class,
                        getAdNetworkId(), MoPubErrorCode.NETWORK_NO_FILL);
                MoPubLog.log(LOAD_FAILED, ADAPTER_NAME, " mTTFullScreenVideoAd is null !");
            }
        }

        @Override
        public void onFullScreenVideoCached() {
            MoPubLog.log(CUSTOM, ADAPTER_NAME, " mTTFullScreenVideoAd onFullScreenVideoCached invoke !");
            if (mWeakActivity != null && mWeakActivity.get() != null)
                Toast.makeText(mWeakActivity.get(), "The FullScreenVideo has Cached.....", Toast.LENGTH_SHORT).show();
        }
    };

    private TTFullScreenVideoAd.FullScreenVideoAdInteractionListener mFullScreenVideoAdInteractionListener = new TTFullScreenVideoAd.FullScreenVideoAdInteractionListener() {

        @Override
        public void onAdShow() {
            MoPubRewardedVideoManager.onRewardedVideoStarted(PangolinAudienceAdFullVideoAdapter.class, mCodeId);
            if (mWeakActivity != null && mWeakActivity.get() != null)
                Toast.makeText(mWeakActivity.get(), "TTFullScreenVideoAd onAdShow.....", Toast.LENGTH_SHORT).show();
            MoPubLog.log(CUSTOM, ADAPTER_NAME, "TTFullScreenVideoAd onAdShow...");
        }

        @Override
        public void onAdVideoBarClick() {
            MoPubRewardedVideoManager.onRewardedVideoClicked(PangolinAudienceAdFullVideoAdapter.class, mCodeId);
            if (mWeakActivity != null && mWeakActivity.get() != null)
                Toast.makeText(mWeakActivity.get(), "TTFullScreenVideoAd onAdVideoBarClick.....", Toast.LENGTH_SHORT).show();
            MoPubLog.log(CUSTOM, ADAPTER_NAME, "TTFullScreenVideoAd onAdVideoBarClick...");
        }

        @Override
        public void onAdClose() {
            MoPubRewardedVideoManager.onRewardedVideoClosed(PangolinAudienceAdFullVideoAdapter.class, mCodeId);
            if (mWeakActivity != null && mWeakActivity.get() != null)
                Toast.makeText(mWeakActivity.get(), "TTFullScreenVideoAd onAdClose.....", Toast.LENGTH_SHORT).show();
            MoPubLog.log(CUSTOM, ADAPTER_NAME, "TTFullScreenVideoAd onAdClose...");
        }

        @Override
        public void onVideoComplete() {
            MoPubRewardedVideoManager.onRewardedVideoCompleted(PangolinAudienceAdFullVideoAdapter.class,mCodeId, MoPubReward.success(MoPubReward.NO_REWARD_LABEL, MoPubReward.DEFAULT_REWARD_AMOUNT));
            if (mWeakActivity != null && mWeakActivity.get() != null)
                Toast.makeText(mWeakActivity.get(), "TTFullScreenVideoAd onVideoComplete.....", Toast.LENGTH_SHORT).show();
            MoPubLog.log(CUSTOM, ADAPTER_NAME, "TTFullScreenVideoAd onVideoComplete...");
        }

        @Override
        public void onSkippedVideo() {
            if (mWeakActivity != null && mWeakActivity.get() != null)
                Toast.makeText(mWeakActivity.get(), "TTFullScreenVideoAd onSkippedVideo.....", Toast.LENGTH_SHORT).show();
            MoPubLog.log(CUSTOM, ADAPTER_NAME, "TTFullScreenVideoAd onSkippedVideo...");
        }
    };




}
