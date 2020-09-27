package com.mopub.mobileads;

/**
 * created by wuzejian on 2019-12-01
 */
public class ErrorCode {
    public static MoPubErrorCode mapErrorCode(int error) {
        switch (error) {
//            case com.bytedance.sdk.openadsdk.core.ErrorCode.CONTENT_TYPE:
//            case com.bytedance.sdk.openadsdk.core.ErrorCode.REQUEST_PB_ERROR:
//                return MoPubErrorCode.NO_CONNECTION;
//            case com.bytedance.sdk.openadsdk.core.ErrorCode.NO_AD:
//                return MoPubErrorCode.NETWORK_NO_FILL;
//            case com.bytedance.sdk.openadsdk.core.ErrorCode.ADSLOT_EMPTY:
//            case com.bytedance.sdk.openadsdk.core.ErrorCode.ADSLOT_ID_ERROR:
//                return MoPubErrorCode.MISSING_AD_UNIT_ID;
//            case com.bytedance.sdk.openadsdk.core.ErrorCode.SYS_ERROR:
//                return MoPubErrorCode.SERVER_ERROR;
            default:
                return MoPubErrorCode.UNSPECIFIED;
        }
    }
}
