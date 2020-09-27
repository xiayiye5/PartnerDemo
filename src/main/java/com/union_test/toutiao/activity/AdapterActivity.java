package com.union_test.toutiao.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.union_test.toutiao.utils.UIUtils;
import com.union_test.toutiao.R;

/**
 * created by wuzejian on 2019-12-03
 */
public class AdapterActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置锁屏下可展示，此配置仅限测试调试使用，正式代码慎用
        UIUtils.setShowOnLocked(this);
        setContentView(R.layout.adapter_activity);

        findViewById(R.id.btn_admob).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdapterActivity.this, AdapterGoogleMainActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.btn_mopub).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdapterActivity.this, AdapterMopubActivity.class);
                startActivity(intent);
            }
        });

    }
}
