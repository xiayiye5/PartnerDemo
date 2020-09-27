package com.google.ads.mediation.sample.customevent;

import android.os.Bundle;

/**
 * created by wuzejian on 2019-12-03
 */
public final class BundleBuilder {
    private int width;
    private int height;
    private String codeId;

    public BundleBuilder setWidth(int width) {
        this.width = width;
        return this;
    }

    public BundleBuilder setHeight(int height) {
        this.height = height;
        return this;
    }

    public BundleBuilder setCodeId(String codeId) {
        this.codeId = codeId;
        return this;
    }

    public Bundle build() {
        Bundle bundle = new Bundle();
        bundle.putInt("height", height);
        bundle.putInt("width", width);
        bundle.putString("codeId", codeId);
        return bundle;
    }
}