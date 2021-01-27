package com.android.server.wm;

import android.content.Context;

public final class IntelliServiceManagerEx {
    public static void setKeepPortrait(Context context, boolean isKeepPortrait) {
        IntelliServiceManager.getInstance(context).setKeepPortrait(isKeepPortrait);
    }

    public static void setDisplayRotation(int displayRotation) {
        IntelliServiceManager.setDisplayRotation(displayRotation);
    }
}
