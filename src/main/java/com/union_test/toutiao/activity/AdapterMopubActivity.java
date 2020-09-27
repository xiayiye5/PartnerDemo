package com.union_test.toutiao.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.union_test.toutiao.utils.UIUtils;
import com.mopub.actiivity.MopubBannerActivity;
import com.mopub.actiivity.MopubFullVideoActivity;
import com.mopub.actiivity.MopubInterstitialActivity;
import com.mopub.actiivity.MopubRewardedVideoActivity;
import com.union_test.toutiao.R;



/**
 * created by wuzejian on 2019-11-29
 */
public class AdapterMopubActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置锁屏下可展示，此配置仅限测试调试使用，正式代码慎用
        UIUtils.setShowOnLocked(this);
        setContentView(R.layout.mopub_activity_adapter);

        findViewById(R.id.btn_mopub_reward_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdapterMopubActivity.this, MopubRewardedVideoActivity.class);
                AdapterMopubActivity.this.startActivity(intent);
            }
        });

        findViewById(R.id.btn_mopub_full_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdapterMopubActivity.this, MopubFullVideoActivity.class);
                AdapterMopubActivity.this.startActivity(intent);
            }
        });


        findViewById(R.id.btn_mopub_banner).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdapterMopubActivity.this, MopubBannerActivity.class);
                AdapterMopubActivity.this.startActivity(intent);
            }
        });

        findViewById(R.id.btn_mopub_interstitial).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdapterMopubActivity.this, MopubInterstitialActivity.class);
                AdapterMopubActivity.this.startActivity(intent);
            }
        });
    }
}
