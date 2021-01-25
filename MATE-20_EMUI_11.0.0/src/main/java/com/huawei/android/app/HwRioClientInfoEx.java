package com.huawei.android.app;

import android.app.HwRioClientInfo;

public class HwRioClientInfoEx {
    private HwRioClientInfo mHwRioClientInfo;

    public HwRioClientInfoEx(HwRioClientInfo info) {
        this.mHwRioClientInfo = info;
    }

    public HwRioClientInfoEx() {
        this.mHwRioClientInfo = new HwRioClientInfo();
    }

    public void setScreenWidth(int width) {
        HwRioClientInfo hwRioClientInfo = this.mHwRioClientInfo;
        if (hwRioClientInfo != null) {
            hwRioClientInfo.setScreenWidth(width);
        }
    }

    public int getScreenWidth() {
        HwRioClientInfo hwRioClientInfo = this.mHwRioClientInfo;
        if (hwRioClientInfo != null) {
            return hwRioClientInfo.getScreenWidth();
        }
        return 0;
    }

    public void setScreenHeight(int height) {
        HwRioClientInfo hwRioClientInfo = this.mHwRioClientInfo;
        if (hwRioClientInfo != null) {
            hwRioClientInfo.setScreenHeight(height);
        }
    }

    public int getScreenHeight() {
        HwRioClientInfo hwRioClientInfo = this.mHwRioClientInfo;
        if (hwRioClientInfo != null) {
            return hwRioClientInfo.getScreenHeight();
        }
        return 0;
    }

    public void setUiMode(int mode) {
        HwRioClientInfo hwRioClientInfo = this.mHwRioClientInfo;
        if (hwRioClientInfo != null) {
            hwRioClientInfo.setUiMode(mode);
        }
    }

    public int getUiMode() {
        HwRioClientInfo hwRioClientInfo = this.mHwRioClientInfo;
        if (hwRioClientInfo != null) {
            return hwRioClientInfo.getUiMode();
        }
        return 0;
    }

    public void setDpi(int dpi) {
        HwRioClientInfo hwRioClientInfo = this.mHwRioClientInfo;
        if (hwRioClientInfo != null) {
            hwRioClientInfo.setDpi(dpi);
        }
    }

    public int getDpi() {
        HwRioClientInfo hwRioClientInfo = this.mHwRioClientInfo;
        if (hwRioClientInfo != null) {
            return hwRioClientInfo.getDpi();
        }
        return 0;
    }

    public void setPackageName(String packageName) {
        HwRioClientInfo hwRioClientInfo = this.mHwRioClientInfo;
        if (hwRioClientInfo != null) {
            hwRioClientInfo.setPackageName(packageName);
        }
    }

    public String getPackageName() {
        HwRioClientInfo hwRioClientInfo = this.mHwRioClientInfo;
        if (hwRioClientInfo != null) {
            return hwRioClientInfo.getPackageName();
        }
        return null;
    }

    public long getPackageVersion() {
        HwRioClientInfo hwRioClientInfo = this.mHwRioClientInfo;
        if (hwRioClientInfo != null) {
            return hwRioClientInfo.getPackageVersion();
        }
        return -1;
    }

    public void setPackageVersion(long packageVersion) {
        HwRioClientInfo hwRioClientInfo = this.mHwRioClientInfo;
        if (hwRioClientInfo != null) {
            hwRioClientInfo.setPackageVersion(packageVersion);
        }
    }

    public void setWindowTitle(String title) {
        HwRioClientInfo hwRioClientInfo = this.mHwRioClientInfo;
        if (hwRioClientInfo != null) {
            hwRioClientInfo.setWindowTitle(title);
        }
    }

    public String getWindowTitle() {
        HwRioClientInfo hwRioClientInfo = this.mHwRioClientInfo;
        if (hwRioClientInfo != null) {
            return hwRioClientInfo.getWindowTitle();
        }
        return null;
    }

    public HwRioClientInfo getRioClientInfo() {
        return this.mHwRioClientInfo;
    }

    public String toString() {
        HwRioClientInfo hwRioClientInfo = this.mHwRioClientInfo;
        if (hwRioClientInfo != null) {
            return hwRioClientInfo.toString();
        }
        return "null";
    }
}
