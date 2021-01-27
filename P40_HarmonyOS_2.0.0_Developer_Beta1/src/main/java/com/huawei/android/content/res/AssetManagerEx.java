package com.huawei.android.content.res;

import android.content.res.AssetManager;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class AssetManagerEx {
    private AssetManagerEx() {
    }

    public static int getDeepType(AssetManager asset) {
        return asset.getDeepType();
    }
}
