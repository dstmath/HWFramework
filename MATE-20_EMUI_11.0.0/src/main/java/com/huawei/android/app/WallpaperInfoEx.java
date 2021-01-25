package com.huawei.android.app;

import android.app.WallpaperInfo;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class WallpaperInfoEx {
    private WallpaperInfoEx() {
    }

    public static int getThumbnailResource(WallpaperInfo wallpaperInfo) {
        return wallpaperInfo.getThumbnailResource();
    }
}
