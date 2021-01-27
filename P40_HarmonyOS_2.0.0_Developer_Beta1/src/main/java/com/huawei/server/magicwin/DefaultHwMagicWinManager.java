package com.huawei.server.magicwin;

import android.graphics.Rect;
import android.view.animation.Animation;
import com.android.server.wm.AppWindowTokenExt;

public class DefaultHwMagicWinManager {
    private static DefaultHwMagicWinManager sInstance = new DefaultHwMagicWinManager();

    public static DefaultHwMagicWinManager getInstance() {
        return sInstance;
    }

    public Rect getHwMagicWinMiddleBounds(int type) {
        return new Rect(0, 0, 0, 0);
    }

    public boolean getHwMagicWinEnabled(int type, String packageName) {
        return false;
    }

    public Animation getHwMagicWinAnimation(Animation animation, boolean enter, int transit, AppWindowTokenExt appWindowToken, Rect frame, boolean isAppLauncher) {
        return animation;
    }

    public boolean isSupportMagicRotatingScreen(String packageName) {
        return false;
    }
}
