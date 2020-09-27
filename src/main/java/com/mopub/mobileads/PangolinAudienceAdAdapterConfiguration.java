package com.mopub.mobileads;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.mopub.common.BaseAdapterConfiguration;
import com.mopub.common.OnNetworkInitializationFinishedListener;
import com.mopub.common.Preconditions;
import com.mopub.common.logging.MoPubLog;
import com.union_test.toutiao.config.TTAdManagerHolder;

import java.util.Map;

import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.CUSTOM;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.CUSTOM_WITH_THROWABLE;

/**
 * created by wuzejian on 2019/11/29
 */
public class PangolinAudienceAdAdapterConfiguration extends BaseAdapterConfiguration {
    private static final String TAG = "PangolinAudienceAdAdapter";

    private static final String ADAPTER_VERSION = "2.7.0.0";

    private static final String MOPUB_NETWORK_NAME = "pangolin_audience_network";

    @NonNull
    @Override
    public String getAdapterVersion() {
        return ADAPTER_VERSION;
    }

    @Nullable
    @Override
    public String getBiddingToken(@NonNull Context context) {
        return null;
    }

    /**
     * returns a lowercase String that represents your ad network name. Use underscores if the String needs to contain spaces
     *
     * @return
     */
    @NonNull
    @Override
    public String getMoPubNetworkName() {
        return MOPUB_NETWORK_NAME;
    }

    /**
     * returns the version number of your ad network SDK.
     *
     * @return
     */
    @NonNull
    @Override
    public String getNetworkSdkVersion() {
        final String adapterVersion = getAdapterVersion();
        return (!TextUtils.isEmpty(adapterVersion)) ?
                adapterVersion : "";
    }

    /**
     * MoPub 初始化时被调用
     * @param context
     * @param configuration
     * @param listener
     */
    @SuppressLint("LongLogTag")
    @Override
    public void initializeNetwork(@NonNull Context context, @Nullable Map<String, String> configuration, @NonNull OnNetworkInitializationFinishedListener listener) {
        MoPubLog.log(CUSTOM, TAG, "PangolinAudienceAdAdapterConfiguration#initializeNetwork....");
        if (configuration != null) {
            for (Map.Entry<String, String> entry : configuration.entrySet()) {
                MoPubLog.log(CUSTOM, TAG, "initializeNetwork-->entry:key=" + entry.getKey() + ",entry:value=" + entry.getValue());
            }
        }

        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(listener);
        boolean networkInitializationSucceeded = false;
        synchronized (PangolinAudienceAdAdapterConfiguration.class) {
            try {
                TTAdManagerHolder.init(context);
                networkInitializationSucceeded = true;
            } catch (Exception e) {
                MoPubLog.log(CUSTOM_WITH_THROWABLE, "Initializing AdMob has encountered " +
                        "an exception.", e);
            }
        }

        if (networkInitializationSucceeded) {
            listener.onNetworkInitializationFinished(PangolinAudienceAdAdapterConfiguration.class,
                    MoPubErrorCode.ADAPTER_INITIALIZATION_SUCCESS);
        } else {
            listener.onNetworkInitializationFinished(PangolinAudienceAdAdapterConfiguration.class,
                    MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
        }
    }
}
