package com.mopub.actiivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.union_test.toutiao.utils.UIUtils;
import com.mopub.mobileads.PangolinAudienceAdBannerAdapter;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;
import com.union_test.toutiao.R;

import java.util.HashMap;
import java.util.Map;

import static com.mopub.actiivity.Utils.logToast;

/**
 * created by wuzejian on 2019/11/29
 */
@SuppressLint("LongLogTag")
public class MopubBannerActivity extends Activity implements MoPubView.BannerAdListener {
    private static final String TAG= "MopubBannerActivity";

    @Nullable
    private String mAdUnitId = "f1c07b4e194040e7b172b051ca3cb233";

    private MoPubView mMoPubView;
    private EditText mEtWidth;
    private EditText mEtHeight;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置锁屏下可展示，此配置仅限测试调试使用，正式代码慎用
        UIUtils.setShowOnLocked(this);
        setContentView(R.layout.mopub_activity_banner_express);

        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(mAdUnitId)
                .withLogLevel(MoPubLog.LogLevel.DEBUG)
                .build();

        //init MoPub SDK
        MoPub.initializeSdk(this, sdkConfiguration, initSdkListener());

        initView();

    }

    private void initView() {
        mMoPubView = findViewById(R.id.adview);
        mEtHeight = findViewById(R.id.express_height);
        mEtWidth =  findViewById(R.id.express_width);

        findViewById(R.id.btn_express_load).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float expressViewWidth = 350;
                float expressViewHeight = 280;
                try{
                    expressViewWidth = Float.parseFloat(mEtWidth.getText().toString());
                    expressViewHeight = Float.parseFloat(mEtHeight.getText().toString());
                }catch (Exception e){
                }

                if (expressViewWidth == 0) expressViewWidth =350;
                if (expressViewHeight == 0) expressViewHeight =280;


                Map<String, Object> localExtras = new HashMap<>();
                localExtras.put(PangolinAudienceAdBannerAdapter.EXPRESS_VIEW_WIDTH,expressViewWidth);
                localExtras.put(PangolinAudienceAdBannerAdapter.EXPRESS_VIEW_HEIGHT,expressViewHeight);
                mMoPubView.setAdUnitId(mAdUnitId); // Enter your Ad Unit ID from www.mopub.com
                mMoPubView.setLocalExtras(localExtras);
                mMoPubView.loadAd();
            }
        });
        mMoPubView.setBannerAdListener(this);

    }

    @Override
    public void onBannerLoaded(MoPubView banner) {
        logToast(MopubBannerActivity.this, "MoPubView onBannerLoaded.");
    }

    @Override
    public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
        logToast(MopubBannerActivity.this, "MoPubView onBannerFailed.-"+errorCode.toString());
    }

    @Override
    public void onBannerClicked(MoPubView banner) {
        logToast(MopubBannerActivity.this, "MoPubView onBannerClicked.");

    }

    @Override
    public void onBannerExpanded(MoPubView banner) {
    }

    @Override
    public void onBannerCollapsed(MoPubView banner) {

    }


    private SdkInitializationListener initSdkListener() {
        return new SdkInitializationListener() {
            @Override
            public void onInitializationFinished() {

                Log.d("PangolinAdapter","onInitializationFinished////");
           /* MoPub SDK initialized.
           Check if you should show the consent dialog here, and make your ad requests. */
            }
        };
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMoPubView != null) {
            mMoPubView.destroy();
            mMoPubView = null;
        }
    }
}
