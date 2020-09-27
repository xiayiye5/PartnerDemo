package com.union_test.toutiao.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.DownloadStatusController;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTFeedAd;
import com.bytedance.sdk.openadsdk.TTImage;
import com.bytedance.sdk.openadsdk.TTNativeAd;
import com.union_test.toutiao.utils.UIUtils;
import com.union_test.toutiao.R;
import com.union_test.toutiao.config.TTAdManagerHolder;
import com.union_test.toutiao.utils.TToast;
import com.union_test.toutiao.view.ILoadMoreListener;
import com.union_test.toutiao.view.LoadMoreListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Feed广告使用示例,使用ListView
 */
@SuppressWarnings("ALL")
public class FeedListActivity extends Activity {
    private static final String TAG = "FeedListActivity";

    private static final int AD_POSITION = 3;
    private static final int LIST_ITEM_COUNT = 30;
    private LoadMoreListView mListView;
    private MyAdapter myAdapter;
    private List<TTFeedAd> mData;

    private TTAdNative mTTAdNative;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置锁屏下可展示，此配置仅限测试调试使用，正式代码慎用
        UIUtils.setShowOnLocked(this);
        setContentView(R.layout.activity_feed_listview);
        //step1:初始化sdk
        TTAdManager ttAdManager = TTAdManagerHolder.get();
        //step2:创建TTAdNative对象,用于调用广告请求接口
        mTTAdNative = ttAdManager.createAdNative(getApplicationContext());
        //step3:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        TTAdManagerHolder.get().requestPermissionIfNecessary(this);
        initListView();
    }

    @SuppressWarnings("RedundantCast")
    private void initListView() {
        mListView = (LoadMoreListView) findViewById(R.id.my_list);
        mData = new ArrayList<>();
        myAdapter = new MyAdapter(this, mData);
        mListView.setAdapter(myAdapter);
        mListView.setLoadMoreListener(new ILoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadListAd();
            }
        });

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadListAd();
            }
        }, 500);
    }

    /**
     * 加载feed广告
     */
    private void loadListAd() {
        //step4:创建feed广告请求类型参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId("901121737")
                .setSupportDeepLink(true)
                .setImageAcceptedSize(640, 320)
                .setAdCount(3) //请求广告数量为1到3条
                .build();
        //step5:请求广告，调用feed广告异步请求接口，加载到广告后，拿到广告素材自定义渲染
        mTTAdNative.loadFeedAd(adSlot, new TTAdNative.FeedAdListener() {
            @Override
            public void onError(int code, String message) {
                if (mListView != null) {
                    mListView.setLoadingFinish();
                }
                TToast.show(FeedListActivity.this, message);
            }

            @Override
            public void onFeedAdLoad(List<TTFeedAd> ads) {
                if (mListView != null) {
                    mListView.setLoadingFinish();
                }

                if (ads == null || ads.isEmpty()) {
                    TToast.show(FeedListActivity.this, "on FeedAdLoaded: ad is null!");
                    return;
                }


                for (int i = 0; i < LIST_ITEM_COUNT; i++) {
                    mData.add(null);
                }

                int count = mData.size();
                for (TTFeedAd ad : ads) {
                    ad.setActivityForDownloadApp(FeedListActivity.this);
                    int random = (int) (Math.random() * LIST_ITEM_COUNT) + count - LIST_ITEM_COUNT;
                    mData.set(random, ad);
                }

                myAdapter.notifyDataSetChanged();
            }
        });
    }

    @SuppressWarnings("CanBeFinal")
    private static class MyAdapter extends BaseAdapter {

        private static final int ITEM_VIEW_TYPE_NORMAL = 0;
        private static final int ITEM_VIEW_TYPE_GROUP_PIC_AD = 1;
        private static final int ITEM_VIEW_TYPE_SMALL_PIC_AD = 2;
        private static final int ITEM_VIEW_TYPE_LARGE_PIC_AD = 3;
        private static final int ITEM_VIEW_TYPE_VIDEO = 4;
        private static final int ITEM_VIEW_TYPE_VERTICAL_IMG = 5;//竖版图片

        private int mVideoCount = 0;


        private List<TTFeedAd> mData;
        private Context mContext;

        private Map<AdViewHolder, TTAppDownloadListener> mTTAppDownloadListenerMap = new WeakHashMap<>();

        public MyAdapter(Context context, List<TTFeedAd> data) {
            this.mContext = context;
            this.mData = data;
        }

        @Override
        public int getCount() {
            return mData.size(); // for test
        }

        @Override
        public TTFeedAd getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        //信息流广告的样式，有大图、小图、组图和视频，通过ad.getImageMode()来判断
        @Override
        public int getItemViewType(int position) {
            TTFeedAd ad = getItem(position);
            if (ad == null) {
                return ITEM_VIEW_TYPE_NORMAL;
            } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_SMALL_IMG) {
                return ITEM_VIEW_TYPE_SMALL_PIC_AD;
            } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_LARGE_IMG) {
                return ITEM_VIEW_TYPE_LARGE_PIC_AD;
            } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_GROUP_IMG) {
                return ITEM_VIEW_TYPE_GROUP_PIC_AD;
            } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_VIDEO) {
                return ITEM_VIEW_TYPE_VIDEO;
            } else if (ad.getImageMode() == TTAdConstant.IMAGE_MODE_VERTICAL_IMG) {
                return ITEM_VIEW_TYPE_VERTICAL_IMG;
            } else {
                TToast.show(mContext, "图片展示样式错误");
                return ITEM_VIEW_TYPE_NORMAL;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 6;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TTFeedAd ad = getItem(position);
            switch (getItemViewType(position)) {
                case ITEM_VIEW_TYPE_SMALL_PIC_AD:
                    return getSmallAdView(convertView, parent, ad);
                case ITEM_VIEW_TYPE_LARGE_PIC_AD:
                    return getLargeAdView(convertView, parent, ad);
                case ITEM_VIEW_TYPE_GROUP_PIC_AD:
                    return getGroupAdView(convertView, parent, ad);
                case ITEM_VIEW_TYPE_VIDEO:
                    return getVideoView(convertView, parent, ad);
                case ITEM_VIEW_TYPE_VERTICAL_IMG:
                    return getVerticalAdView(convertView, parent, ad);
                default:
                    return getNormalView(convertView, parent, position);
            }
        }

        /**
         * @param convertView
         * @param parent
         * @param ad
         * @return
         */
        private View getVerticalAdView(View convertView, ViewGroup parent, @NonNull final TTFeedAd ad) {
            VerticalAdViewHolder adViewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_ad_vertical_pic, parent, false);
                adViewHolder = new VerticalAdViewHolder();
                adViewHolder.mTitle = (TextView) convertView.findViewById(R.id.tv_listitem_ad_title);
                adViewHolder.mSource = (TextView) convertView.findViewById(R.id.tv_listitem_ad_source);
                adViewHolder.mDescription = (TextView) convertView.findViewById(R.id.tv_listitem_ad_desc);
                adViewHolder.mVerticalImage = (ImageView) convertView.findViewById(R.id.iv_listitem_image);
                adViewHolder.mIcon = (ImageView) convertView.findViewById(R.id.iv_listitem_icon);
                adViewHolder.mDislike = (ImageView) convertView.findViewById(R.id.iv_listitem_dislike);
                adViewHolder.mCreativeButton = (Button) convertView.findViewById(R.id.btn_listitem_creative);
                adViewHolder.mStopButton = (Button) convertView.findViewById(R.id.btn_listitem_stop);
                adViewHolder.mRemoveButton = (Button) convertView.findViewById(R.id.btn_listitem_remove);
                convertView.setTag(adViewHolder);
            } else {
                adViewHolder = (VerticalAdViewHolder) convertView.getTag();
            }
            ArrayList<View> images = new ArrayList<>();
            images.add(adViewHolder.mVerticalImage);
            bindData(convertView, adViewHolder,images, ad);
            if (ad.getImageList() != null && !ad.getImageList().isEmpty()) {
                TTImage image = ad.getImageList().get(0);
                if (image != null && image.isValid()) {
                    Glide.with(mContext).load(image.getImageUrl()).into(adViewHolder.mVerticalImage);
                }
            }

            return convertView;
        }

        //渲染视频广告，以视频广告为例，以下说明
        @SuppressWarnings("RedundantCast")
        private View getVideoView(View convertView, ViewGroup parent, @NonNull final TTFeedAd ad) {
            final VideoAdViewHolder adViewHolder;
            try {
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_ad_large_video, parent, false);
                    adViewHolder = new VideoAdViewHolder();
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
                } else {
                    adViewHolder = (VideoAdViewHolder) convertView.getTag();
                }

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
                        Log.e("VideoAdListener","===onProgressUpdate current:"+current+" duration:"+duration);
                    }

                    @Override
                    public void onVideoAdComplete(TTFeedAd ad) {
                        Log.e("VideoAdListener","===onVideoAdComplete");
                    }
                });
                Log.e("VideoAdListener","video ad duration:"+ad.getVideoDuration());
                //绑定广告数据、设置交互回调
                ArrayList<View> images = new ArrayList<>();
                images.add(adViewHolder.videoView);
                bindData(convertView, adViewHolder,images, ad);
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
            } catch (Exception e) {
                e.printStackTrace();
            }

            return convertView;
        }

        @SuppressWarnings("RedundantCast")
        private View getLargeAdView(View convertView, ViewGroup parent, @NonNull final TTFeedAd ad) {
            final LargeAdViewHolder adViewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_ad_large_pic, parent, false);
                adViewHolder = new LargeAdViewHolder();
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
            } else {
                adViewHolder = (LargeAdViewHolder) convertView.getTag();
            }
            ArrayList<View> images = new ArrayList<>();
            images.add(adViewHolder.mLargeImage);
            bindData(convertView, adViewHolder,images, ad);
            if (ad.getImageList() != null && !ad.getImageList().isEmpty()) {
                TTImage image = ad.getImageList().get(0);
                if (image != null && image.isValid()) {
                    Glide.with(mContext).load(image.getImageUrl()).into(adViewHolder.mLargeImage);
                }
            }
            return convertView;
        }

        @SuppressWarnings("RedundantCast")
        private View getGroupAdView(View convertView, ViewGroup parent, @NonNull final TTFeedAd ad) {
            GroupAdViewHolder adViewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_ad_group_pic, parent, false);
                adViewHolder = new GroupAdViewHolder();
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
            } else {
                adViewHolder = (GroupAdViewHolder) convertView.getTag();
            }
            ArrayList<View> images = new ArrayList<>();
            images.add(adViewHolder.mGroupImage1);
            images.add(adViewHolder.mGroupImage2);
            images.add(adViewHolder.mGroupImage3);
            bindData(convertView, adViewHolder,images, ad);
            if (ad.getImageList() != null && ad.getImageList().size() >= 3) {
                TTImage image1 = ad.getImageList().get(0);
                TTImage image2 = ad.getImageList().get(1);
                TTImage image3 = ad.getImageList().get(2);
                if (image1 != null && image1.isValid()) {
                    Glide.with(mContext).load(image1.getImageUrl()).into(adViewHolder.mGroupImage1);
                }
                if (image2 != null && image2.isValid()) {
                    Glide.with(mContext).load(image2.getImageUrl()).into(adViewHolder.mGroupImage2);
                }
                if (image3 != null && image3.isValid()) {
                    Glide.with(mContext).load(image3.getImageUrl()).into(adViewHolder.mGroupImage3);
                }
            }
            return convertView;
        }


        @SuppressWarnings("RedundantCast")
        private View getSmallAdView(View convertView, ViewGroup parent, @NonNull final TTFeedAd ad) {
            SmallAdViewHolder adViewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_ad_small_pic, parent, false);
                adViewHolder = new SmallAdViewHolder();
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
            } else {
                adViewHolder = (SmallAdViewHolder) convertView.getTag();
            }
            ArrayList<View> images = new ArrayList<>();
            images.add(adViewHolder.mSmallImage);
            bindData(convertView, adViewHolder,images, ad);
            if (ad.getImageList() != null && !ad.getImageList().isEmpty()) {
                TTImage image = ad.getImageList().get(0);
                if (image != null && image.isValid()) {
                    Glide.with(mContext).load(image.getImageUrl()).into(adViewHolder.mSmallImage);
                }
            }

            //接入网盟的dislike 逻辑
//            final TTAdDislike ttAdDislike = ad.getDislikeDialog();
//            if (ttAdDislike != null) {
//                ttAdDislike.setDislikeInteractionCallback(new TTAdDislike.DislikeInteractionCallback() {
//                    @Override
//                    public void onSelected(int position, String value) {
//
//                    }
//
//                    @Override
//                    public void onCancel() {
//
//                    }
//                });
//                dislikeIcon.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        ttAdDislike.showDislikeDialog();
//                    }
//                });
//            }
            return convertView;
        }
        /**
         * 非广告list
         *
         * @param convertView
         * @param parent
         * @param position
         * @return
         */
        @SuppressWarnings("RedundantCast")
        @SuppressLint("SetTextI18n")
        private View getNormalView(View convertView, ViewGroup parent, int position) {
            NormalViewHolder normalViewHolder;
            if (convertView == null) {
                normalViewHolder = new NormalViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_normal, parent, false);
                normalViewHolder.idle = (TextView) convertView.findViewById(R.id.text_idle);
                convertView.setTag(normalViewHolder);
            } else {
                normalViewHolder = (NormalViewHolder) convertView.getTag();
            }
            normalViewHolder.idle.setText("ListView item " + position);
            return convertView;
        }

        private void bindDislikeCustom(View dislike, final TTFeedAd ad) {
            final TTAdDislike ttAdDislike = ad.getDislikeDialog((Activity) mContext);
            if (ttAdDislike != null) {
                ad.getDislikeDialog((Activity) mContext).setDislikeInteractionCallback(new TTAdDislike.DislikeInteractionCallback() {
                    @Override
                    public void onSelected(int position, String value) {
                        mData.remove(ad);
                        notifyDataSetChanged();
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

//            List<FilterWord> words = ad.getFilterWords();
//            if (words == null || words.isEmpty()) {
//                return;
//            }
//
//            final DislikeDialog dislikeDialog = new DislikeDialog(mContext, words);
//            dislikeDialog.setOnDislikeItemClick(new DislikeDialog.OnDislikeItemClick() {
//                @Override
//                public void onItemClick(FilterWord filterWord) {
//                    //屏蔽广告
//                    mData.remove(ad);
//                    notifyDataSetChanged();
//                }
//            });
//            final TTAdDislike ttAdDislike = ad.getDislikeDialog(dislikeDialog);
//
//            dislike.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    //展示dislike可以自行调用dialog
//                    dislikeDialog.show();
//
//                    //也可以使用接口来展示
//                    //ttAdDislike.showDislikeDialog();
//                }
//            });
        }

        private void bindData(View convertView, final AdViewHolder adViewHolder,List<View> images, TTFeedAd ad) {
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
            ad.registerViewForInteraction((ViewGroup) convertView, images, clickViewList, creativeViewList,adViewHolder.mDislike,new TTNativeAd.AdInteractionListener() {
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
            adViewHolder.mTitle.setText(ad.getTitle()); //title为广告的简单信息提示
            adViewHolder.mDescription.setText(ad.getDescription()); //description为广告的较长的说明
            adViewHolder.mSource.setText(ad.getSource() == null ? "广告来源" : ad.getSource());
            TTImage icon = ad.getIcon();
            if (icon != null && icon.isValid()) {
                Glide.with(mContext).load(icon.getImageUrl()).into(adViewHolder.mIcon);
            }
            Button adCreativeButton = adViewHolder.mCreativeButton;
            switch (ad.getInteractionType()) {
                case TTAdConstant.INTERACTION_TYPE_DOWNLOAD:
                    //如果初始化ttAdManager.createAdNative(getApplicationContext())没有传入activity 则需要在此传activity，否则影响使用Dislike逻辑
                    if (mContext instanceof Activity) {
                        ad.setActivityForDownloadApp((Activity) mContext);
                    }
                    adCreativeButton.setVisibility(View.VISIBLE);
                    if (adViewHolder.mStopButton != null) {
                        adViewHolder.mStopButton.setVisibility(View.VISIBLE);
                    }
                    adViewHolder.mRemoveButton.setVisibility(View.VISIBLE);
                    bindDownloadListener(adCreativeButton, adViewHolder, ad);
                    //绑定下载状态控制器
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
                    TToast.show(mContext, "交互类型异常");
            }
        }

        private void bindDownLoadStatusController(AdViewHolder adViewHolder, final TTFeedAd ad) {
            final DownloadStatusController controller = ad.getDownloadStatusController();
            if (adViewHolder.mStopButton != null) {
                adViewHolder.mStopButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (controller != null) {
                            controller.changeDownloadStatus();
                            TToast.show(mContext, "改变下载状态");
                            Log.d(TAG, "改变下载状态");
                        }
                    }
                });
            }

            adViewHolder.mRemoveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (controller != null) {
                        controller.cancelDownload();
                        TToast.show(mContext, "取消下载");
                        Log.d(TAG, "取消下载");
                    }
                }
            });
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

        private static class NormalViewHolder {
            TextView idle;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TToast.reset();
        mHandler.removeCallbacksAndMessages(null);
    }
}
