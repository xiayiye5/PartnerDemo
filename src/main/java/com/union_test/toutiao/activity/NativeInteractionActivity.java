package com.union_test.toutiao.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTImage;
import com.bytedance.sdk.openadsdk.TTNativeAd;
import com.union_test.toutiao.utils.UIUtils;
import com.union_test.toutiao.R;
import com.union_test.toutiao.config.TTAdManagerHolder;
import com.union_test.toutiao.utils.TToast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 插屏广告Activity示例
 */
public class NativeInteractionActivity extends Activity implements View.OnClickListener {

    private TTAdNative mTTAdNative;
    private Button mShow_InterstitialAd_btn;
    private Button mShow_InterstitialAd_btn_ladingpage;
    private Context mContext;
    private ImageView mAdImageView;
    private ImageView mCloseImageView;
    private Dialog mAdDialog;
    private ViewGroup mRootView;
    private TextView mDislikeView;
    private boolean mIsLoading = false;
    private RequestManager mRequestManager;

    @SuppressLint("SetTextI18n")
    @SuppressWarnings("RedundantCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置锁屏下可展示，此配置仅限测试调试使用，正式代码慎用
        UIUtils.setShowOnLocked(this);
        setContentView(R.layout.activity_insert_ad);
        mContext = this;
        mRequestManager = Glide.with(this);
        //step2:创建TTAdNative对象,用于调用广告请求接口
        mTTAdNative = TTAdManagerHolder.get().createAdNative(this);
        //step3:可选，申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        TTAdManagerHolder.get().requestPermissionIfNecessary(this);
        mShow_InterstitialAd_btn = (Button) findViewById(R.id.show_ad_dialog);
        mShow_InterstitialAd_btn.setText("展示原生INTERSTITIAL广告");
        mShow_InterstitialAd_btn_ladingpage = (Button) findViewById(R.id.show_ad_dialog_landingpage);
        mShow_InterstitialAd_btn_ladingpage.setVisibility(View.GONE);
        mShow_InterstitialAd_btn.setOnClickListener(this);
        mShow_InterstitialAd_btn_ladingpage.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (mIsLoading) {
            return;
        }
        if (v.getId() == R.id.show_ad_dialog) {
            loadInteractionAd("901121435");
        }
    }

    /**
     * 加载插屏广告
     */
    @SuppressWarnings({"ALL", "SameParameterValue"})
    private void loadInteractionAd(String codeId) {
        mIsLoading = true;
        //step4:创建广告请求参数AdSlot,注意其中的setNativeAdtype方法，具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(1080, 1920)
                .setNativeAdType(AdSlot.TYPE_INTERACTION_AD)//请求原生广告时候，请务必调用该方法，设置参数为TYPE_BANNER或TYPE_INTERACTION_AD
                .build();

        //step5:请求广告，对请求回调的广告作渲染处理
        mTTAdNative.loadNativeAd(adSlot, new TTAdNative.NativeAdListener() {
            @Override
            public void onError(int code, String message) {
                mIsLoading = false;
                TToast.show(NativeInteractionActivity.this, "load error : " + code + ", " + message);
            }

            @Override
            public void onNativeAdLoad(List<TTNativeAd> ads) {
                mIsLoading = false;
                if (ads.get(0) == null) {
                    return;
                }
                showAd(ads.get(0));
            }
        });
    }

    @SuppressWarnings("RedundantCast")
    private void showAd(final TTNativeAd ad) {
        mAdDialog = new Dialog(mContext, R.style.native_insert_dialog);
        mAdDialog.setCancelable(false);
        mAdDialog.setContentView(R.layout.native_insert_ad_layout);
        mRootView = mAdDialog.findViewById(R.id.native_insert_ad_root);
        mAdImageView = (ImageView) mAdDialog.findViewById(R.id.native_insert_ad_img);
        //限制dialog 的最大宽度不能超过屏幕，宽高最小为屏幕宽的 1/3
        DisplayMetrics dm = this.getResources().getDisplayMetrics();
        int maxWidth = (dm == null) ? 0 : dm.widthPixels;
        int minWidth = maxWidth / 3;
        mAdImageView.setMaxWidth(maxWidth);
        mAdImageView.setMinimumWidth(minWidth);
        //noinspection SuspiciousNameCombination
        mAdImageView.setMinimumHeight(minWidth);
        mCloseImageView = (ImageView) mAdDialog.findViewById(R.id.native_insert_close_icon_img);
        mDislikeView = mAdDialog.findViewById(R.id.native_insert_dislike_text);

        ImageView iv = mAdDialog.findViewById(R.id.native_insert_ad_logo);

        //绑定关闭按钮
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ad.getAdLogo().compress(Bitmap.CompressFormat.PNG, 100, stream);
            mRequestManager
                    .load(stream.toByteArray())
                    .asBitmap()
                    .into(iv);
        }catch (Exception e){

        }finally {
            try {
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        bindCloseAction();
        //绑定网盟dislike逻辑，有助于精准投放
        bindDislikeAction(ad);
        //绑定广告view事件交互
        bindViewInteraction(ad);
        //加载ad 图片资源
        loadAdImage(ad);
    }

    private void bindCloseAction() {
        mCloseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdDialog.dismiss();
            }
        });
    }

    //接入网盟的dislike 逻辑，有助于提示广告精准投放度
    private void bindDislikeAction(TTNativeAd ad) {
        final TTAdDislike ttAdDislike = ad.getDislikeDialog(this);
        if (ttAdDislike != null) {
            ttAdDislike.setDislikeInteractionCallback(new TTAdDislike.DislikeInteractionCallback() {
                @Override
                public void onSelected(int position, String value) {
                    mAdDialog.dismiss();
                }

                @Override
                public void onCancel() {

                }

                @Override
                public void onRefuse() {

                }
            });
        }
        mDislikeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ttAdDislike != null)
                    ttAdDislike.showDislikeDialog();
            }
        });
    }

    boolean mHasShowDownloadActive = false;
    private void bindViewInteraction(TTNativeAd ad) {
        //可以被点击的view, 比如标题、icon等,点击后尝试打开落地页，也可以把nativeView放进来意味整个广告区域可被点击
        List<View> clickViewList = new ArrayList<>();
        clickViewList.add(mAdImageView);

        //触发创意广告的view（点击下载或拨打电话），比如可以设置为一个按钮，按钮上文案根据广告类型设定提示信息
        List<View> creativeViewList = new ArrayList<>();
        //如果需要点击图文区域也能进行下载或者拨打电话动作，请将图文区域的view传入
        //creativeViewList.add(nativeView);
        creativeViewList.add(mAdImageView);
        List<View> imageViewList = new ArrayList<>();
        imageViewList.add(mAdImageView);
        //重要! 这个涉及到广告计费，必须正确调用。convertView必须使用ViewGroup。
        ad.registerViewForInteraction(mRootView, imageViewList,clickViewList, creativeViewList, mDislikeView, new TTNativeAd.AdInteractionListener() {
            @Override
            public void onAdClicked(View view, TTNativeAd ad) {
                if (ad != null) {
                    TToast.show(mContext, "广告" + ad.getTitle() + "被点击");
                }
            }

            @Override
            public void onAdCreativeClick(View view, TTNativeAd ad) {
                if (ad != null) {
                    TToast.show(mContext, "广告" + ad.getTitle() + "被创意按钮被点击");
                }
            }

            @Override
            public void onAdShow(TTNativeAd ad) {
                if (ad != null) {
                    TToast.show(mContext, "广告" + ad.getTitle() + "展示");
                }
            }
        });


        ad.setDownloadListener(new TTAppDownloadListener() {
            @Override
            public void onIdle() {
                mHasShowDownloadActive = false;
            }

            @Override
            public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                Log.d("DML", "onDownloadActive==totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);

                if (!mHasShowDownloadActive) {
                    mHasShowDownloadActive = true;
                    TToast.show(NativeInteractionActivity.this, "下载中，点击下载区域暂停", Toast.LENGTH_LONG);
                }
            }

            @Override
            public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                Log.d("DML", "onDownloadPaused===totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);
                TToast.show(NativeInteractionActivity.this, "下载暂停，点击下载区域继续", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                Log.d("DML", "onDownloadFailed==totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);
                TToast.show(NativeInteractionActivity.this, "下载失败，点击下载区域重新下载", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                Log.d("DML", "onDownloadFinished==totalBytes=" + totalBytes + ",fileName=" + fileName + ",appName=" + appName);
                TToast.show(NativeInteractionActivity.this, "下载完成，点击下载区域重新下载", Toast.LENGTH_LONG);
            }

            @Override
            public void onInstalled(String fileName, String appName) {
                Log.d("DML", "onInstalled==" + ",fileName=" + fileName + ",appName=" + appName);
                TToast.show(NativeInteractionActivity.this, "安装完成，点击下载区域打开", Toast.LENGTH_LONG);
            }
        });

    }

    private void loadAdImage(TTNativeAd ad) {
        if (ad.getImageList() != null && !ad.getImageList().isEmpty()) {
            TTImage image = ad.getImageList().get(0);
            if (image != null && image.isValid()) {
                mRequestManager.load(image.getImageUrl()).into(mAdImageView);
            }
        }

        TTImage image = ad.getImageList().get(0);
        int width = image.getWidth();
        String url = image.getImageUrl();
        mRequestManager.load(url).into(new SimpleTarget<GlideDrawable>() {
            @Override
            public void onResourceReady(GlideDrawable glideDrawable, GlideAnimation<? super GlideDrawable> glideAnimation) {
                if (mAdImageView != null) {
                    mAdImageView.setImageDrawable(glideDrawable);
                    showAd();
                }
            }
        });
    }

    private void showAd() {
        if (this.isFinishing()) {
            return;
        }
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new IllegalStateException("不能在子线程调用 TTInteractionAd.showInteractionAd");
        }
        mAdDialog.show();
    }

}
