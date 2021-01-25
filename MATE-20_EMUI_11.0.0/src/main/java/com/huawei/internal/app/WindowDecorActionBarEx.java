package com.huawei.internal.app;

import android.app.ActionBar;
import android.util.Log;
import com.android.internal.app.WindowDecorActionBar;

public class WindowDecorActionBarEx {
    public static void setShowHideAnimationEnabled(ActionBar actionBar, boolean enabled) {
        if (actionBar instanceof WindowDecorActionBar) {
            ((WindowDecorActionBar) actionBar).setShowHideAnimationEnabled(enabled);
        }
    }

    @Deprecated
    public static void setAnimationEnable(ActionBar actionBar, boolean enabled) {
        Log.e("WindowDecorActionBarEx", "setAnimationEnable this method does not need in 9.x");
    }

    public static void setShoudTransition(ActionBar actionBar, boolean enabled) {
        if (actionBar instanceof WindowDecorActionBar) {
            ((WindowDecorActionBar) actionBar).setShoudTransition(enabled);
        }
    }

    public static void setScrollTabAnimEnable(ActionBar actionBar, boolean enabled) {
        if (actionBar instanceof WindowDecorActionBar) {
            ((WindowDecorActionBar) actionBar).setScrollTabAnimEnable(enabled);
        }
    }
}
