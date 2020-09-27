package com.union_test.toutiao.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.bytedance.sdk.openadsdk.downloadnew.core.ExitInstallListener;
import com.union_test.toutiao.utils.UIUtils;
import com.union_test.toutiao.R;
import com.union_test.toutiao.config.TTAdManagerHolder;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置锁屏下可展示，此配置仅限测试调试使用，正式代码慎用
        UIUtils.setShowOnLocked(this);
        setContentView(R.layout.activity_main);

        TextView tvVersion = findViewById(R.id.tv_version);
        String ver = getString(R.string.main_sdk_version_tip, TTAdManagerHolder.get().getSDKVersion());
        tvVersion.setText(ver);

        bindButton(R.id.btn_adapter, AdapterActivity.class);
        bindButton(R.id.btn_main_feed_lv, FeedListActivity.class);
        bindButton(R.id.btn_main_feed_rv, FeedRecyclerActivity.class);
        bindButton(R.id.btn_mian_splash, SplashActivity.class);
        bindButton(R.id.btn_mian_reward, RewardVideoActivity.class);
        bindButton(R.id.btn_main_full, FullScreenVideoActivity.class);
        bindButton(R.id.btn_main_banner_native, NativeBannerActivity.class);
        bindButton(R.id.btn_main_interstitial_native, NativeInteractionActivity.class);
        bindButton(R.id.btn_main_draw_native, DrawNativeVideoActivity.class);
        bindButton(R.id.btn_locker, com.locker.activity.LockerActivity.class);
        bindButton(R.id.btn_test_tool, AllTestToolActivity.class);
        bindButton(R.id.btn_express_ad, AllExpressAdActivity.class);
        bindButton(R.id.btn_waterfall,NativeWaterfallActivity.class);
        // 申请部分权限,建议在sdk初始化前申请,如：READ_PHONE_STATE、ACCESS_COARSE_LOCATION及ACCESS_FINE_LOCATION权限，
        // 以获取更好的广告推荐效果，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        TTAdManagerHolder.get().requestPermissionIfNecessary(this);

    }

    private void bindButton(@IdRes int id, final Class clz) {
        findViewById(id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, clz);
                //全屏视频代码位id
                if (v.getId() == R.id.btn_main_full) {
                    intent.putExtra("horizontal_rit","901121184");
                    intent.putExtra("vertical_rit","901121375");
                }
                //激励视频代码位id
                if (v.getId() == R.id.btn_mian_reward) {
                    intent.putExtra("horizontal_rit","901121430");
                    intent.putExtra("vertical_rit","901121365");
                }

                //开屏代码位id
                if (v.getId() == R.id.btn_mian_splash) {
                    intent.putExtra("splash_rit","801121648");
                    intent.putExtra("is_express", false);
                }
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        boolean isShowInstallDialog = TTAdManagerHolder.get().tryShowInstallDialogWhenExit(this, new ExitInstallListener() {
            @Override
            public void onExitInstall() {
                finish();
            }
        });

        if (!isShowInstallDialog) {
            //没有弹出安装对话框时交由系统处理或者自己的业务逻辑
            super.onBackPressed();
        }
    }
}
