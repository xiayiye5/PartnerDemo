package com.google.ads.mediation.sample.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.union_test.toutiao.utils.UIUtils;
import com.google.ads.mediation.sample.customevent.BundleBuilder;
import com.google.ads.mediation.sample.customevent.adapter.SampleCustomEventInterstitial;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.union_test.toutiao.R;
import com.union_test.toutiao.utils.TToast;


/**
 * created by wuzejian on 2019/11/29
 */
@SuppressLint("LongLogTag")
public class AdmobInterstitialActivity extends Activity {
    private static final String TAG = "AdmobInterstitialActivity";
    @Nullable
    private EditText mEtWidth;
    private EditText mEtHeight;
    private InterstitialAd mInterstitialAd;
    private String mCodeId = "901121133";//穿山甲代码位id
    private Context mContext;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置锁屏下可展示，此配置仅限测试调试使用，正式代码慎用
        UIUtils.setShowOnLocked(this);
        mContext = this;
        setContentView(R.layout.google_activity_interstitial_express);
        MobileAds.initialize(AdmobInterstitialActivity.this, getResources().getString(R.string.admob_app_id));
        mInterstitialAd = new InterstitialAd(AdmobInterstitialActivity.this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstitial_id));
        initView();
    }

    private void initView() {
        mEtHeight = findViewById(R.id.express_height);
        mEtWidth = findViewById(R.id.express_width);
        final Button mShowButton = findViewById(R.id.btn_express_show);
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

                BundleBuilder bundleBuilder = new BundleBuilder();
                bundleBuilder.setHeight((int) expressViewHeight);
                bundleBuilder.setWidth((int) expressViewWidth);
                bundleBuilder.setCodeId(mCodeId);
                AdRequest adRequest = new AdRequest.Builder().
                        addCustomEventExtrasBundle(SampleCustomEventInterstitial.class, bundleBuilder.build())
                        .build();


                mInterstitialAd.loadAd(adRequest);
                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int i) {
                        super.onAdFailedToLoad(i);
                        Log.d(TAG, "onAdFailedToLoad....error=" + i);
                        TToast.show(mContext, "广告加载失败！i=" + i);

                    }

                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        mShowButton.setEnabled(true);
                        TToast.show(mContext, "广告加载成功！");
                        Log.d(TAG, "....onAdLoaded=onAdLoaded");

                    }

                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        TToast.show(mContext, "广告关闭！！");

                    }

                    @Override
                    public void onAdOpened() {
                        super.onAdOpened();
                        TToast.show(mContext, "广告展开了！！");

                    }

                    @Override
                    public void onAdClicked() {
                        super.onAdClicked();
                        TToast.show(mContext, "广告被点击！！");

                    }
                });
                mInterstitialAd.loadAd(adRequest);


            }
        });


        mShowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInterstitialAd != null) {
                    mInterstitialAd.show();
                    mShowButton.setEnabled(false);
                }
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

    }


}
