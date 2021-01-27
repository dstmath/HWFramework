package com.huawei.android.content.res;

import android.content.res.AssetManager;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class AssetManagerExt {
    public static String getApkAssetsCookieName(AssetManager asset, int cookie) {
        if (asset == null || asset.getApkAssets() == null || asset.getApkAssets().length < cookie) {
            return null;
        }
        return asset.getApkAssets()[cookie - 1].getAssetPath();
    }
}
