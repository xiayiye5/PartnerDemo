package com.google.ads.mediation.sample.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.union_test.toutiao.utils.UIUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.union_test.toutiao.R;

/**
 * created by wuzejian on 2019/11/29
 */
@SuppressLint("LongLogTag")
public class AdmobRewardVideoActivity extends Activity {
    private static final String TAG = "AdmobRewardVideoActivity";
    private Button mShowButton;
    private RewardedAd rewardedVideoAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置锁屏下可展示，此配置仅限测试调试使用，正式代码慎用
        UIUtils.setShowOnLocked(this);
        setContentView(R.layout.mopub_reward_activity);
        mShowButton = findViewById(R.id.showRewardAd);
        mShowButton.setEnabled(false);
        MobileAds.initialize(this, getResources().getString(R.string.admob_app_id));

        findViewById(R.id.loadRewardAd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rewardedVideoAd = loadRewardedAd(rewardedVideoAd, getResources().getString(R.string.admob_reward_video_id));
            }
        });


        mShowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRewardedVideo();
                mShowButton.setEnabled(false);
            }
        });


    }

    private RewardedAd loadRewardedAd(RewardedAd ad, String adUnit) {
        Toast.makeText(this, "Start Loading", Toast.LENGTH_SHORT).show();
        ad = new RewardedAd(AdmobRewardVideoActivity.this, adUnit);
        AdRequest adRequest = new AdRequest.Builder().build();
        ad.loadAd(adRequest, adLoadCallback);

        return ad;
    }

    private void showRewardedVideo() {
        if (rewardedVideoAd != null && rewardedVideoAd.isLoaded()) {
            rewardedVideoAd.show(AdmobRewardVideoActivity.this, rewardedAdCallback);
        } else {
            Toast.makeText(this, "No Ad to Show, Please Load Ad", Toast.LENGTH_SHORT).show();
        }
    }

    private RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
        @Override
        public void onRewardedAdLoaded() {
            mShowButton.setEnabled(true);
            // Ad successfully loaded.
            Toast.makeText(getApplicationContext(), "激励视频广告加载成功", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "On Rewarded Video Ad Loaded");

            if (rewardedVideoAd.isLoaded()) {
                Log.e(TAG, "RewardedVideo is Loaded" + rewardedVideoAd.getMediationAdapterClassName());
            }

        }

        @Override
        public void onRewardedAdFailedToLoad(int errorCode) {
            // Ad failed to load.
            Toast.makeText(getApplicationContext(), "激励视频广告加载失败:" + errorCode, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "On Rewarded Video Ad Failed To Load:" + errorCode);

        }
    };

    private RewardedAdCallback rewardedAdCallback = new RewardedAdCallback() {

        @Override
        public void onRewardedAdOpened() {
            super.onRewardedAdOpened();
            Toast.makeText(getApplicationContext(), "广告展示", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "On Rewarded Video Ad Opened");

        }

        @Override
        public void onRewardedAdClosed() {
            super.onRewardedAdClosed();
            Toast.makeText(getApplicationContext(), "广告关闭", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "On Rewarded Video Ad Closed");
        }

        @Override
        public void onUserEarnedReward(com.google.android.gms.ads.rewarded.RewardItem rewardItem) {
            super.onUserEarnedReward(rewardItem);
            Toast.makeText(getApplicationContext(), "激励发放：rewardItem=" + rewardItem.getAmount(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "On User Rewarded");
        }

        @Override
        public void onRewardedAdFailedToShow(int i) {
            super.onRewardedAdFailedToShow(i);
            Toast.makeText(getApplicationContext(), "onRewardedVideoAdFailedToShow,error=" + i, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "On Rewarded Video Ad Failed To Show");
        }
    };

    public void onDestroy() {
        super.onDestroy();
    }
}
