package com.huawei.server.magicwin;

import android.view.animation.Animation;

public class DefaultHwMagicWinAnimation {
    private static DefaultHwMagicWinAnimation sInstance = new DefaultHwMagicWinAnimation();

    public static DefaultHwMagicWinAnimation getInstance() {
        return sInstance;
    }

    public Animation getMwWallpaperCloseAnimation() {
        return null;
    }
}
