package com.huawei.internal.app;

import android.app.ActionBar;
import com.android.internal.app.WindowDecorActionBar;

public class WindowDecorActionBarEx {
    public static void setShowHideAnimationEnabled(ActionBar actionBar, boolean enabled) {
        if (actionBar instanceof WindowDecorActionBar) {
            ((WindowDecorActionBar) actionBar).setShowHideAnimationEnabled(enabled);
        }
    }

    public static void setAnimationEnable(ActionBar actionBar, boolean enabled) {
        if (actionBar instanceof WindowDecorActionBar) {
            ((WindowDecorActionBar) actionBar).setAnimationEnable(enabled);
        }
    }
}
