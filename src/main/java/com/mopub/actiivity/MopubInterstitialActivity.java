package com.mopub.actiivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.union_test.toutiao.utils.UIUtils;
import com.mopub.mobileads.PangolinAudienceAdAdapterConfiguration;
import com.mopub.mobileads.PangolinAudienceAdAdapterInterstitial;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.union_test.toutiao.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.mopub.actiivity.Utils.logToast;


/**
 * created by wuzejian on 2019/11/29
 */
@SuppressLint("LongLogTag")
public class MopubInterstitialActivity extends Activity implements MoPubInterstitial.InterstitialAdListener {
    private static final String TAG = "MopubBannerActivity";

    @Nullable
    private String mAdUnitId = "cb5e783967d44624b808891a8db8e2c1";
    private EditText mEtWidth;
    private EditText mEtHeight;
    private Button mShowButton;

    private MoPubInterstitial mMoPubInterstitial;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置锁屏下可展示，此配置仅限测试调试使用，正式代码慎用
        UIUtils.setShowOnLocked(this);
        setContentView(R.layout.mopub_activity_interstitial_express);

        Map<String, String> mediatedNetworkConfiguration1 = new HashMap<>();
        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(Objects.requireNonNull(mAdUnitId))
                .withMediatedNetworkConfiguration(PangolinAudienceAdAdapterConfiguration.class.toString(), mediatedNetworkConfiguration1)
                .withLogLevel(MoPubLog.LogLevel.DEBUG)//Log级别
                .withLegitimateInterestAllowed(false)//是否允许收集经纬度信息
                .build();

        //init MoPub SDK
        MoPub.initializeSdk(this, sdkConfiguration, initSdkListener());

        initView();

    }

    private void initView() {
        mEtHeight = findViewById(R.id.express_height);
        mEtWidth = findViewById(R.id.express_width);
        mShowButton = findViewById(R.id.btn_express_show);
        mShowButton.setEnabled(false);
        //load ad
        findViewById(R.id.btn_express_load).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float expressViewWidth = 350;
                float expressViewHeight = 280;
                try {
                    expressViewWidth = Float.parseFloat(mEtWidth.getText().toString());
                    expressViewHeight = Float.parseFloat(mEtHeight.getText().toString());
                } catch (Exception e) {
                    expressViewHeight = 0; //高度设置为0,则高度会自适应
                }

                Map<String, Object> localExtras = new HashMap<>();
                localExtras.put(PangolinAudienceAdAdapterInterstitial.EXPRESS_VIEW_WIDTH, expressViewWidth);
                localExtras.put(PangolinAudienceAdAdapterInterstitial.EXPRESS_VIEW_HEIGHT, expressViewHeight);
                localExtras.put(PangolinAudienceAdAdapterInterstitial.EXPRESS_ACTIVITY_PARAM, MopubInterstitialActivity.this);

                mShowButton.setEnabled(false);
                if (mMoPubInterstitial == null) {
                    mMoPubInterstitial = new MoPubInterstitial(MopubInterstitialActivity.this, mAdUnitId);
                    mMoPubInterstitial.setInterstitialAdListener(MopubInterstitialActivity.this);
                }
                mMoPubInterstitial.setLocalExtras(localExtras);
                mMoPubInterstitial.load();
            }
        });


        mShowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMoPubInterstitial != null) {
                    mMoPubInterstitial.show();
                }
            }
        });
    }


    private SdkInitializationListener initSdkListener() {
        return new SdkInitializationListener() {
            @Override
            public void onInitializationFinished() {

                Log.d("PangolinAdapter", "onInitializationFinished////");
           /* MoPub SDK initialized.
           Check if you should show the consent dialog here, and make your ad requests. */
            }
        };
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMoPubInterstitial != null) {
            mMoPubInterstitial.destroy();
            mMoPubInterstitial = null;
        }
    }

    @Override
    public void onInterstitialLoaded(MoPubInterstitial interstitial) {
        mShowButton.setEnabled(true);
        logToast(MopubInterstitialActivity.this, "Interstitial onInterstitialLoaded.");
    }

    @Override
    public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
        mShowButton.setEnabled(false);
        logToast(MopubInterstitialActivity.this, "Interstitial onInterstitialFailed.");
    }

    @Override
    public void onInterstitialShown(MoPubInterstitial interstitial) {
        logToast(MopubInterstitialActivity.this, "Interstitial shown.");
    }

    @Override
    public void onInterstitialClicked(MoPubInterstitial interstitial) {
        logToast(MopubInterstitialActivity.this, "Interstitial clicked.");
    }

    @Override
    public void onInterstitialDismissed(MoPubInterstitial interstitial) {
        logToast(MopubInterstitialActivity.this, "Interstitial dismissed.");
    }
}
