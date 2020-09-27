package com.locker.activity;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.DownloadStatusController;
import com.bytedance.sdk.openadsdk.FilterWord;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTFeedAd;
import com.bytedance.sdk.openadsdk.TTImage;
import com.bytedance.sdk.openadsdk.TTNativeAd;
import com.union_test.toutiao.utils.UIUtils;
import com.locker.util.DateUtils;
import com.locker.util.PowerUtil;
import com.locker.util.ViewUtils;
import com.locker.widget.TouchToUnLockView;
import com.union_test.toutiao.R;
import com.union_test.toutiao.config.TTAdManagerHolder;
import com.union_test.toutiao.utils.TToast;
import com.xdandroid.hellodaemon.IntentWrapper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Start
 * <p/>
 * User:Rocky(email:1247106107@qq.com)
 * Created by Rocky on 2017/09/17  16:49
 * PACKAGE_NAME com.eagle.locker.activity
 * PROJECT_NAME LockerScreen
 * TODO:
 * Description:
 * <p/>
 * Done
 */
public class LockerActivity extends AppCompatActivity {

    private final static String TAG = "LockerActivity";
    //    private TouchPullDownView mPullDownView;
    private TouchToUnLockView mUnlockView;

    private View mChargeContainer;

    private TextView mLockTime, mLockDate, mChargePercent;
    private ImageView mBatteryIcon;

    private View mContainerView;

    private Calendar calendar = GregorianCalendar.getInstance();
    private SimpleDateFormat weekFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
    private SimpleDateFormat monthFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
    private TTAdNative mTTAdNative;
    private Map<AdViewHolder, TTAppDownloadListener> mTTAppDownloadListenerMap = new WeakHashMap<>();
    private View mAdView;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置锁屏下可展示，此配置仅限测试调试使用，正式代码慎用
        UIUtils.setShowOnLocked(this);
        setLockerWindow(getWindow());
        //step1:初始化sdk
        TTAdManager ttAdManager = TTAdManagerHolder.get();
        //step2:创建TTAdNative对象,用于调用广告请求接口
        mTTAdNative = ttAdManager.createAdNative(getApplicationContext());
        //step3:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        TTAdManagerHolder.get().requestPermissionIfNecessary(this);
        registerLockerReceiver();
        setContentView(R.layout.activity_locker);
        initView();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadListAd();
            }
        }, 500);
    }

    private void loadListAd() {
        //step4:创建feed广告请求类型参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId("901121737")
                .setSupportDeepLink(true)
                .setImageAcceptedSize(640, 320)
                .setAdCount(1) //请求广告数量为1到3条
                .build();
        //step5:请求广告，调用feed广告异步请求接口，加载到广告后，拿到广告素材自定义渲染
        mTTAdNative.loadFeedAd(adSlot, new TTAdNative.FeedAdListener() {
            @Override
            public void onError(int code, String message) {

                TToast.show(LockerActivity.this, message);
            }

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onFeedAdLoad(List<TTFeedAd> ads) {

                if (ads == null || ads.isEmpty()) {
                    TToast.show(LockerActivity.this, "on FeedAdLoaded: ad is null!");
                    return;
                }
                Log.i("LockerActivity", "LockerActivity#onFeedAdLoad->ads:" + ads.size());
                for (TTFeedAd ad : ads) {
                    ad.setActivityForDownloadApp(LockerActivity.this);
                }

                int viewType = ads.get(0).getImageMode();
                Log.i("LockerActivity", "LockerActivity#onFeedAdLoad->viewType(5-video):" + viewType);

                if (mUnlockView != null) {
                    if (mAdView != null) {
                        mUnlockView.removeView(mAdView);
                    }
                    mAdView = getViewAd(ads.get(0), mUnlockView);
                    if (mAdView != null) {
                        mAdView.setBackgroundColor(getResources().getColor(R.color.white));
                        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        lp.gravity = Gravity.CENTER;
                        mUnlockView.addView(mAdView, 0, lp);
                    }
                }

            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterLockerReceiver();
        mHandler.removeCallbacksAndMessages(null);
    }

    private void initView() {

        mChargeContainer = ViewUtils.get(this, R.id.linel_ChargeContainer);
        mContainerView = ViewUtils.get(this, R.id.relel_ContentContainer);

        mLockTime = ViewUtils.get(this, R.id.txtv_LockTime);
        mLockDate = ViewUtils.get(this, R.id.txtv_LockDate);
        mBatteryIcon = ViewUtils.get(this, R.id.imgv_BatteryIcon);
        mChargePercent = ViewUtils.get(this, R.id.txtv_ChargePercent);


//        mPullDownView = ViewUtils.get(this, R.id.tpdv_PullDownView);
//        mPullDownView.setOnTouchPullDownListener(new TouchPullDownView.OnTouchPullDownListener() {
//            @Override
//            public void onTouchGiftBoxArea() {
//
//            }
//
//            @Override
//            public void onPullPercent(float percent) {
//
//            }
//
//            @Override
//            public void onPullCanceled() {
//                Toast.makeText(getApplication(), R.string.pull_canceled, Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onGiftBoxPulled() {
////                particleSystem.addBurst(random.nextInt(DimenUtils.getScreenWidth(getBaseContext())), random.nextInt(DimenUtils.getScreenHeight(getBaseContext())));
//            }
//
//            @Override
//            public void onGiftBoxClick() {
////                particleSystem.addBurst(random.nextInt(DimenUtils.getScreenWidth(getBaseContext())), random.nextInt(DimenUtils.getScreenHeight(getBaseContext())));
//            }
//        });


        mUnlockView = ViewUtils.get(this, R.id.tulv_UnlockView);
        mUnlockView.setOnTouchToUnlockListener(new TouchToUnLockView.OnTouchToUnlockListener() {
            @Override
            public void onTouchLockArea() {
                if (mContainerView != null) {
                    mContainerView.setBackgroundColor(Color.parseColor("#66000000"));
                }
            }

            @Override
            public void onSlidePercent(float percent) {
                if (mContainerView != null) {
                    mContainerView.setAlpha(1 - percent < 0.05f ? 0.05f : 1 - percent);
                    mContainerView.setScaleX(1 + (percent > 1f ? 1f : percent) * 0.08f);
                    mContainerView.setScaleY(1 + (percent > 1f ? 1f : percent) * 0.08f);
                }
            }

            @Override
            public void onSlideToUnlock() {
                Log.d("LockerActivity", "onSlideToUnlock.....onSlideToUnlock....");
                finish();
            }

            @Override
            public void onSlideAbort() {
                if (mContainerView != null) {
                    mContainerView.setAlpha(1.0f);
                    mContainerView.setBackgroundColor(0);
                    mContainerView.setScaleX(1f);
                    mContainerView.setScaleY(1f);
                }
            }
        });

//        particleSystem = new BurstParticleSystem();
//        pv_ParticleView.setTextureAtlasFactory(new MyTextureAtlasFactory(getResources()));
//        pv_ParticleView.setParticleSystem(particleSystem);

        if (PowerUtil.isCharging(this)) {
            mChargeContainer.setVisibility(View.VISIBLE);
        } else {
            mChargeContainer.setVisibility(View.GONE);
        }
        updateTimeUI();
        updateBatteryUI();
    }

    public void onBackPressed() {
        IntentWrapper.onBackPressed(this);
    }


    protected UIChangingReceiver mUIChangingReceiver;

    public void registerLockerReceiver() {
        if (mUIChangingReceiver != null) {
            return;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);

        mUIChangingReceiver = new UIChangingReceiver();
        registerReceiver(mUIChangingReceiver, filter);
    }

    public void unregisterLockerReceiver() {
        if (mUIChangingReceiver == null) {
            return;
        }
        unregisterReceiver(mUIChangingReceiver);
        mUIChangingReceiver = null;
    }


    private class UIChangingReceiver extends BroadcastReceiver {

        public UIChangingReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                onActionReceived(action);
            }
        }
    }

    private void updateTimeUI() {
        mLockTime.setText(DateUtils.getHourString(this, System.currentTimeMillis()));
        mLockDate.setText(weekFormat.format(calendar.getTime()) + "    " + monthFormat.format(calendar.getTime()));
    }


    private void updateBatteryUI() {
        int level = PowerUtil.getLevel(this);
        mChargePercent.setText(level + "%");

        if (level <= 30) {
            mBatteryIcon.setImageResource(R.drawable.lock_battery_charging_30);
        } else if (level <= 60) {
            mBatteryIcon.setImageResource(R.drawable.lock_battery_charging_60);
        } else if (level < 100) {
            mBatteryIcon.setImageResource(R.drawable.lock_battery_charging_90);
        } else if (level == 100) {
            mBatteryIcon.setImageResource(R.drawable.ic_lock_charge_four);
        }

        if (level < 100 && mBatteryIcon.getDrawable() instanceof Animatable) {
            Animatable animatable = (Animatable) mBatteryIcon.getDrawable();
            if (PowerUtil.isCharging(this)) {
                animatable.start();
            } else {
                animatable.stop();
            }
        }
    }


    protected void onActionReceived(String action) {
        if (!TextUtils.isEmpty(action)) {
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                updateBatteryUI();
            } else if (action.equals(Intent.ACTION_TIME_TICK)) {
                updateTimeUI();
            } else if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
                mChargeContainer.setVisibility(View.VISIBLE);
            } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                mChargeContainer.setVisibility(View.GONE);
            } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mUnlockView.startAnim();
    }

    @Override
    public void onPause() {
        super.onPause();
        mUnlockView.stopAnim();
    }

    public static void startActivity(Context context) {
        Intent screenIntent = getIntent(context);
        context.startActivity(screenIntent);
    }


    @NonNull
    private static Intent getIntent(Context context) {
        Intent screenIntent = new Intent();
        screenIntent.setClass(context, LockerActivity.class);
        screenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        screenIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        screenIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        return screenIntent;
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void setLockerWindow(Window window) {
        WindowManager.LayoutParams lp = window.getAttributes();
        if (lp != null && Build.VERSION.SDK_INT > 18) {
            //简单处理透明的状态栏，android 4.4以上
            lp.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            window.setAttributes(lp);
        }

        //隐藏状态栏，同时Activity会伸展全屏显示
        window.getDecorView().setSystemUiVisibility(View.INVISIBLE);
        //显示锁屏之上
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        //关闭系统锁屏
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }


    private View getViewAd(TTFeedAd ad, ViewGroup parent) {
        if (ad == null) {
            return null;
        } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_SMALL_IMG) {
            return getSmallAdView(parent, ad);
        } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_LARGE_IMG) {
            return getLargeAdView(parent, ad);
        } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_GROUP_IMG) {
            return getGroupAdView(parent, ad);
        } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_VIDEO) {
            return getVideoView(parent, ad);
        } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_VERTICAL_IMG) {
            return getVerticalAdView(ad);
        }
        return null;
    }


    private View getVerticalAdView(@NonNull final TTFeedAd ad) {
        View convertView = LayoutInflater.from(this).inflate(R.layout.listitem_ad_vertical_pic, null, false);
        VerticalAdViewHolder adViewHolder = new VerticalAdViewHolder();
        adViewHolder.mTitle = (TextView) convertView.findViewById(R.id.tv_listitem_ad_title);
        adViewHolder.mSource = (TextView) convertView.findViewById(R.id.tv_listitem_ad_source);
        adViewHolder.mDescription = (TextView) convertView.findViewById(R.id.tv_listitem_ad_desc);
        adViewHolder.mVerticalImage = (ImageView) convertView.findViewById(R.id.iv_listitem_image);
        adViewHolder.mIcon = (ImageView) convertView.findViewById(R.id.iv_listitem_icon);
        adViewHolder.mDislike = (ImageView) convertView.findViewById(R.id.iv_listitem_dislike);
        adViewHolder.mCreativeButton = (Button) convertView.findViewById(R.id.btn_listitem_creative);
        adViewHolder.mStopButton = (Button) convertView.findViewById(R.id.btn_listitem_stop);
        adViewHolder.mRemoveButton = (Button) convertView.findViewById(R.id.btn_listitem_remove);

        bindData(convertView, adViewHolder, ad);
        if (ad.getImageList() != null && !ad.getImageList().isEmpty()) {
            TTImage image = ad.getImageList().get(0);
            if (image != null && image.isValid()) {
                Glide.with(this).load(image.getImageUrl()).into(adViewHolder.mVerticalImage);
            }
        }

        return convertView;
    }

    //渲染视频广告，以视频广告为例，以下说明
    @SuppressWarnings("RedundantCast")
    private View getVideoView(ViewGroup parent, @NonNull final TTFeedAd ad) {
        View convertView = LayoutInflater.from(this).inflate(R.layout.listitem_ad_large_video, parent, false);
        VideoAdViewHolder adViewHolder = new VideoAdViewHolder();
        adViewHolder.mTitle = (TextView) convertView.findViewById(R.id.tv_listitem_ad_title);
        adViewHolder.mDescription = (TextView) convertView.findViewById(R.id.tv_listitem_ad_desc);
        adViewHolder.mSource = (TextView) convertView.findViewById(R.id.tv_listitem_ad_source);
        adViewHolder.videoView = (FrameLayout) convertView.findViewById(R.id.iv_listitem_video);
        adViewHolder.mIcon = (ImageView) convertView.findViewById(R.id.iv_listitem_icon);
        adViewHolder.mDislike = (ImageView) convertView.findViewById(R.id.iv_listitem_dislike);
        adViewHolder.mCreativeButton = (Button) convertView.findViewById(R.id.btn_listitem_creative);
        adViewHolder.mStopButton = (Button) convertView.findViewById(R.id.btn_listitem_stop);
        adViewHolder.mRemoveButton = (Button) convertView.findViewById(R.id.btn_listitem_remove);
        convertView.setTag(adViewHolder);


        //视频广告设置播放状态回调（可选）
        ad.setVideoAdListener(new TTFeedAd.VideoAdListener() {
            @Override
            public void onVideoLoad(TTFeedAd ad) {

            }

            @Override
            public void onVideoError(int errorCode, int extraCode) {

            }

            @Override
            public void onVideoAdStartPlay(TTFeedAd ad) {

            }

            @Override
            public void onVideoAdPaused(TTFeedAd ad) {

            }

            @Override
            public void onVideoAdContinuePlay(TTFeedAd ad) {

            }

            @Override
            public void onProgressUpdate(long current, long duration) {

            }

            @Override
            public void onVideoAdComplete(TTFeedAd ad) {

            }
        });
        //绑定广告数据、设置交互回调
        bindData(convertView, adViewHolder, ad);
        if (adViewHolder.videoView != null) {
            //获取视频播放view,该view SDK内部渲染，在媒体平台可配置视频是否自动播放等设置。
            View video = ad.getAdView();
            if (video != null) {
                if (video.getParent() == null) {
                    adViewHolder.videoView.removeAllViews();
                    adViewHolder.videoView.addView(video);
                }
            }
        }


        return convertView;
    }

    @SuppressWarnings("RedundantCast")
    private View getLargeAdView(ViewGroup parent, @NonNull final TTFeedAd ad) {
        View convertView = LayoutInflater.from(this).inflate(R.layout.listitem_ad_large_pic, parent, false);
        LargeAdViewHolder adViewHolder = new LargeAdViewHolder();
        adViewHolder.mTitle = (TextView) convertView.findViewById(R.id.tv_listitem_ad_title);
        adViewHolder.mDescription = (TextView) convertView.findViewById(R.id.tv_listitem_ad_desc);
        adViewHolder.mSource = (TextView) convertView.findViewById(R.id.tv_listitem_ad_source);
        adViewHolder.mLargeImage = (ImageView) convertView.findViewById(R.id.iv_listitem_image);
        adViewHolder.mIcon = (ImageView) convertView.findViewById(R.id.iv_listitem_icon);
        adViewHolder.mDislike = (ImageView) convertView.findViewById(R.id.iv_listitem_dislike);
        adViewHolder.mCreativeButton = (Button) convertView.findViewById(R.id.btn_listitem_creative);
        adViewHolder.mStopButton = (Button) convertView.findViewById(R.id.btn_listitem_stop);
        adViewHolder.mRemoveButton = (Button) convertView.findViewById(R.id.btn_listitem_remove);
        convertView.setTag(adViewHolder);
        bindData(convertView, adViewHolder, ad);
        if (ad.getImageList() != null && !ad.getImageList().isEmpty()) {
            TTImage image = ad.getImageList().get(0);
            if (image != null && image.isValid()) {
                Glide.with(this).load(image.getImageUrl()).into(adViewHolder.mLargeImage);
            }
        }
        return convertView;
    }

    @SuppressWarnings("RedundantCast")
    private View getGroupAdView(ViewGroup parent, @NonNull final TTFeedAd ad) {
        View convertView = LayoutInflater.from(this).inflate(R.layout.listitem_ad_group_pic, parent, false);
        GroupAdViewHolder adViewHolder = new GroupAdViewHolder();
        adViewHolder.mTitle = (TextView) convertView.findViewById(R.id.tv_listitem_ad_title);
        adViewHolder.mSource = (TextView) convertView.findViewById(R.id.tv_listitem_ad_source);
        adViewHolder.mDescription = (TextView) convertView.findViewById(R.id.tv_listitem_ad_desc);
        adViewHolder.mGroupImage1 = (ImageView) convertView.findViewById(R.id.iv_listitem_image1);
        adViewHolder.mGroupImage2 = (ImageView) convertView.findViewById(R.id.iv_listitem_image2);
        adViewHolder.mGroupImage3 = (ImageView) convertView.findViewById(R.id.iv_listitem_image3);
        adViewHolder.mIcon = (ImageView) convertView.findViewById(R.id.iv_listitem_icon);
        adViewHolder.mDislike = (ImageView) convertView.findViewById(R.id.iv_listitem_dislike);
        adViewHolder.mCreativeButton = (Button) convertView.findViewById(R.id.btn_listitem_creative);
        adViewHolder.mStopButton = (Button) convertView.findViewById(R.id.btn_listitem_stop);
        adViewHolder.mRemoveButton = (Button) convertView.findViewById(R.id.btn_listitem_remove);
        convertView.setTag(adViewHolder);

        bindData(convertView, adViewHolder, ad);
        if (ad.getImageList() != null && ad.getImageList().size() >= 3) {
            TTImage image1 = ad.getImageList().get(0);
            TTImage image2 = ad.getImageList().get(1);
            TTImage image3 = ad.getImageList().get(2);
            if (image1 != null && image1.isValid()) {
                Glide.with(this).load(image1.getImageUrl()).into(adViewHolder.mGroupImage1);
            }
            if (image2 != null && image2.isValid()) {
                Glide.with(this).load(image2.getImageUrl()).into(adViewHolder.mGroupImage2);
            }
            if (image3 != null && image3.isValid()) {
                Glide.with(this).load(image3.getImageUrl()).into(adViewHolder.mGroupImage3);
            }
        }
        return convertView;
    }


    @SuppressWarnings("RedundantCast")
    private View getSmallAdView(ViewGroup parent, @NonNull final TTFeedAd ad) {
        View convertView = LayoutInflater.from(this).inflate(R.layout.listitem_ad_small_pic, parent, false);
        SmallAdViewHolder adViewHolder = new SmallAdViewHolder();
        adViewHolder.mTitle = (TextView) convertView.findViewById(R.id.tv_listitem_ad_title);
        adViewHolder.mSource = (TextView) convertView.findViewById(R.id.tv_listitem_ad_source);
        adViewHolder.mDescription = (TextView) convertView.findViewById(R.id.tv_listitem_ad_desc);
        adViewHolder.mSmallImage = (ImageView) convertView.findViewById(R.id.iv_listitem_image);
        adViewHolder.mIcon = (ImageView) convertView.findViewById(R.id.iv_listitem_icon);
        adViewHolder.mDislike = (ImageView) convertView.findViewById(R.id.iv_listitem_dislike);
        adViewHolder.mCreativeButton = (Button) convertView.findViewById(R.id.btn_listitem_creative);
        adViewHolder.mStopButton = (Button) convertView.findViewById(R.id.btn_listitem_stop);
        adViewHolder.mRemoveButton = (Button) convertView.findViewById(R.id.btn_listitem_remove);
        convertView.setTag(adViewHolder);

        bindData(convertView, adViewHolder, ad);
        if (ad.getImageList() != null && !ad.getImageList().isEmpty()) {
            TTImage image = ad.getImageList().get(0);
            if (image != null && image.isValid()) {
                Glide.with(this).load(image.getImageUrl()).into(adViewHolder.mSmallImage);
            }
        }

        return convertView;
    }

    private void bindDislikeCustom(View dislike, final TTFeedAd ad) {
        final TTAdDislike ttAdDislike = ad.getDislikeDialog(this);
        if (ttAdDislike != null) {
            ttAdDislike.setDislikeInteractionCallback(new TTAdDislike.DislikeInteractionCallback() {
                @Override
                public void onSelected(int position, String value) {
                    if (mAdView != null) {
                        mUnlockView.removeView(mAdView);
                    }
                    loadListAd();
                }

                @Override
                public void onCancel() {

                }

                @Override
                public void onRefuse() {

                }
            });
        }
        dislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ttAdDislike != null)
                    ttAdDislike.showDislikeDialog();
            }
        });


//        List<FilterWord> words = ad.getFilterWords();
//        if (words == null || words.isEmpty()) {
//            return;
//        }
//
//        final DislikeDialog dislikeDialog = new DislikeDialog(this, words);
//        dislikeDialog.setOnDislikeItemClick(new DislikeDialog.OnDislikeItemClick() {
//            @Override
//            public void onItemClick(FilterWord filterWord) {
//                //屏蔽广告
//                if (mAdView != null) {
//                    mUnlockView.removeView(mAdView);
//                }
//                loadListAd();
//            }
//        });
//        final TTAdDislike ttAdDislike = ad.getDislikeDialog(dislikeDialog);
//
//        dislike.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //展示dislike可以自行调用dialog
//                dislikeDialog.show();
//
//                //也可以使用接口来展示
//                //ttAdDislike.showDislikeDialog();
//            }
//        });
    }

    private void bindData(View convertView, final AdViewHolder adViewHolder, TTFeedAd ad) {
        //设置dislike弹窗，这里展示自定义的dialog
        bindDislikeCustom(adViewHolder.mDislike, ad);

        //可以被点击的view, 也可以把convertView放进来意味item可被点击
        List<View> clickViewList = new ArrayList<>();
        clickViewList.add(convertView);
        //触发创意广告的view（点击下载或拨打电话）
        List<View> creativeViewList = new ArrayList<>();
        creativeViewList.add(adViewHolder.mCreativeButton);
        //如果需要点击图文区域也能进行下载或者拨打电话动作，请将图文区域的view传入
//            creativeViewList.add(convertView);
        //重要! 这个涉及到广告计费，必须正确调用。convertView必须使用ViewGroup。
        ad.registerViewForInteraction((ViewGroup) convertView, clickViewList, creativeViewList, new TTNativeAd.AdInteractionListener() {
            @Override
            public void onAdClicked(View view, TTNativeAd ad) {
                if (ad != null) {
                    TToast.show(LockerActivity.this, "广告" + ad.getTitle() + "被点击");
                }
            }

            @Override
            public void onAdCreativeClick(View view, TTNativeAd ad) {
                if (ad != null) {
                    TToast.show(LockerActivity.this, "广告" + ad.getTitle() + "被创意按钮被点击");
                }
            }

            @Override
            public void onAdShow(TTNativeAd ad) {
                if (ad != null) {
                    TToast.show(LockerActivity.this, "广告" + ad.getTitle() + "展示");
                }
            }
        });
        adViewHolder.mTitle.setText(ad.getTitle()); //title为广告的简单信息提示
        adViewHolder.mDescription.setText(ad.getDescription()); //description为广告的较长的说明
        adViewHolder.mSource.setText(ad.getSource() == null ? "广告来源" : ad.getSource());
        TTImage icon = ad.getIcon();
        if (icon != null && icon.isValid()) {
            Glide.with(LockerActivity.this).load(icon.getImageUrl()).into(adViewHolder.mIcon);
        }
        Button adCreativeButton = adViewHolder.mCreativeButton;
        switch (ad.getInteractionType()) {
            case TTAdConstant.INTERACTION_TYPE_DOWNLOAD:
                //如果初始化ttAdManager.createAdNative(getApplicationContext())没有传入activity 则需要在此传activity，否则影响使用Dislike逻辑
                ad.setActivityForDownloadApp(LockerActivity.this);
                adCreativeButton.setVisibility(View.VISIBLE);
                if (adViewHolder.mStopButton != null) {
                    adViewHolder.mStopButton.setVisibility(View.VISIBLE);
                }
                adViewHolder.mRemoveButton.setVisibility(View.VISIBLE);
                bindDownloadListener(adCreativeButton, adViewHolder, ad);
//                绑定下载状态控制器
                bindDownLoadStatusController(adViewHolder, ad);
                break;
            case TTAdConstant.INTERACTION_TYPE_DIAL:
                adCreativeButton.setVisibility(View.VISIBLE);
                adCreativeButton.setText("立即拨打");
                if (adViewHolder.mStopButton != null) {
                    adViewHolder.mStopButton.setVisibility(View.GONE);
                }
                adViewHolder.mRemoveButton.setVisibility(View.GONE);
                break;
            case TTAdConstant.INTERACTION_TYPE_LANDING_PAGE:
            case TTAdConstant.INTERACTION_TYPE_BROWSER:
//                    adCreativeButton.setVisibility(View.GONE);
                adCreativeButton.setVisibility(View.VISIBLE);
                adCreativeButton.setText("查看详情");
                if (adViewHolder.mStopButton != null) {
                    adViewHolder.mStopButton.setVisibility(View.GONE);
                }
                adViewHolder.mRemoveButton.setVisibility(View.GONE);
                break;
            default:
                adCreativeButton.setVisibility(View.GONE);
                if (adViewHolder.mStopButton != null) {
                    adViewHolder.mStopButton.setVisibility(View.GONE);
                }
                adViewHolder.mRemoveButton.setVisibility(View.GONE);
                TToast.show(LockerActivity.this, "交互类型异常");
        }
    }

    private void bindDownloadListener(final Button adCreativeButton, final AdViewHolder adViewHolder, TTFeedAd ad) {
        TTAppDownloadListener downloadListener = new TTAppDownloadListener() {
            @Override
            public void onIdle() {
                if (!isValid()) {
                    return;
                }
                adCreativeButton.setText("开始下载");
                if (adViewHolder.mStopButton != null) {
                    adViewHolder.mStopButton.setText("开始下载");
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                if (!isValid()) {
                    return;
                }
                if (totalBytes <= 0L) {
                    adCreativeButton.setText("0%");
                } else {
                    adCreativeButton.setText((currBytes * 100 / totalBytes) + "%");
                }
                if (adViewHolder.mStopButton != null) {
                    adViewHolder.mStopButton.setText("下载中");
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                if (!isValid()) {
                    return;
                }
                if (totalBytes <= 0L) {
                    adCreativeButton.setText("0%");
                } else {
                    adCreativeButton.setText((currBytes * 100 / totalBytes) + "%");
                }
                if (adViewHolder.mStopButton != null) {
                    adViewHolder.mStopButton.setText("下载暂停");
                }
            }

            @Override
            public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                if (!isValid()) {
                    return;
                }
                adCreativeButton.setText("重新下载");
                if (adViewHolder.mStopButton != null) {
                    adViewHolder.mStopButton.setText("重新下载");
                }
            }

            @Override
            public void onInstalled(String fileName, String appName) {
                if (!isValid()) {
                    return;
                }
                adCreativeButton.setText("点击打开");
                if (adViewHolder.mStopButton != null) {
                    adViewHolder.mStopButton.setText("点击打开");
                }
            }

            @Override
            public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                if (!isValid()) {
                    return;
                }
                adCreativeButton.setText("点击安装");
                if (adViewHolder.mStopButton != null) {
                    adViewHolder.mStopButton.setText("点击安装");
                }
            }

            @SuppressWarnings("BooleanMethodIsAlwaysInverted")
            private boolean isValid() {
                return mTTAppDownloadListenerMap.get(adViewHolder) == this;
            }
        };
        //一个ViewHolder对应一个downloadListener, isValid判断当前ViewHolder绑定的listener是不是自己
        ad.setDownloadListener(downloadListener); // 注册下载监听器
        mTTAppDownloadListenerMap.put(adViewHolder, downloadListener);
    }

    private void bindDownLoadStatusController(AdViewHolder adViewHolder, final TTFeedAd ad) {
        final DownloadStatusController controller = ad.getDownloadStatusController();
        adViewHolder.mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (controller != null) {
                    controller.changeDownloadStatus();
                    TToast.show(LockerActivity.this, "改变下载状态");
                    Log.d(TAG, "改变下载状态");
                }
            }
        });

        adViewHolder.mRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (controller != null) {
                    controller.cancelDownload();
                    TToast.show(LockerActivity.this, "取消下载");
                    Log.d(TAG, "取消下载");
                }
            }
        });
    }


    private static class VideoAdViewHolder extends AdViewHolder {
        FrameLayout videoView;
    }

    private static class LargeAdViewHolder extends AdViewHolder {
        ImageView mLargeImage;
    }

    private static class SmallAdViewHolder extends AdViewHolder {
        ImageView mSmallImage;
    }

    private static class VerticalAdViewHolder extends AdViewHolder {
        ImageView mVerticalImage;
    }

    private static class GroupAdViewHolder extends AdViewHolder {
        ImageView mGroupImage1;
        ImageView mGroupImage2;
        ImageView mGroupImage3;
    }

    private static class AdViewHolder {
        ImageView mIcon;
        ImageView mDislike;
        Button mCreativeButton;
        TextView mTitle;
        TextView mDescription;
        TextView mSource;
        Button mStopButton;
        Button mRemoveButton;

    }
}
