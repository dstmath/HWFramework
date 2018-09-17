package com.huawei.android.view;

import android.view.AbsLayoutParams;
import android.view.WindowManager.LayoutParams;

public class LayoutParamsEx extends AbsLayoutParams {
    public static final int FLAG_MMI_TEST_DEFAULT_SHAPE = 16384;
    public static final int FLAG_NOTCH_SUPPORT = 65536;
    public static final int FLAG_SECURE_SCREENCAP = 8192;
    public static final int FLAG_SECURE_SCREENSHOT = 4096;
    LayoutParams attrs;

    public LayoutParamsEx(LayoutParams lp) {
        this.attrs = lp;
    }

    public int getHwFlags() {
        return this.attrs.hwFlags;
    }

    public void addHwFlags(int hwFlags) {
        setHwFlags(hwFlags, hwFlags);
    }

    public void clearHwFlags(int hwFlags) {
        setHwFlags(0, hwFlags);
    }

    private void setHwFlags(int hwFlags, int mask) {
        this.attrs.hwFlags = (this.attrs.hwFlags & (~mask)) | (hwFlags & mask);
    }
}
