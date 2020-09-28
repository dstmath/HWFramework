package com.huawei.android.internal.util;

import android.content.Context;
import android.os.Handler;
import com.android.internal.util.ScreenshotHelper;

public class ScreenshotHelperEx {
    private ScreenshotHelper mScreenshotHelper;

    public ScreenshotHelperEx(Context context) {
        this.mScreenshotHelper = new ScreenshotHelper(context);
    }

    public void takeScreenshot(int screenshotType, boolean hasStatus, boolean hasNav, Handler handler) {
        this.mScreenshotHelper.takeScreenshot(screenshotType, hasStatus, hasNav, handler);
    }
}
