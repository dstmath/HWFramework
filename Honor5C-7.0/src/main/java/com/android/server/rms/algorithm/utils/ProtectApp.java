package com.android.server.rms.algorithm.utils;

public class ProtectApp {
    private String mAppPkgName;
    private int mAppType;
    private float mAvgUsedFrequency;
    private int mDeletedTag;
    private String mRecentUsed;
    private int mUserId;

    public ProtectApp(String str, int i, int i2, float f, int i3) {
        this.mAppPkgName = str;
        this.mAppType = i;
        this.mDeletedTag = i2;
        this.mAvgUsedFrequency = f;
        this.mUserId = i3;
    }

    public ProtectApp(String str, int i, String str2, float f) {
        this.mAppPkgName = str;
        this.mAppType = i;
        this.mRecentUsed = str2;
        this.mAvgUsedFrequency = f;
    }

    public ProtectApp(String str, int i, int i2, int i3) {
        this.mAppPkgName = str;
        this.mAppType = i;
        this.mDeletedTag = i2;
        this.mUserId = i3;
    }

    public String getAppPkgName() {
        return this.mAppPkgName;
    }

    public void setAppPkgName(String str) {
        this.mAppPkgName = str;
    }

    public float getAvgUsedFrequency() {
        return this.mAvgUsedFrequency;
    }

    public void setAvgUsedFrequency(float f) {
        this.mAvgUsedFrequency = f;
    }

    public String getRecentUsed() {
        return this.mRecentUsed;
    }

    public void setRecentUsed(String str) {
        this.mRecentUsed = str;
    }

    public int getAppType() {
        return this.mAppType;
    }

    public void setAppType(int i) {
        this.mAppType = i;
    }

    public int getDeletedTag() {
        return this.mDeletedTag;
    }

    public void setDeletedTag(int i) {
        this.mDeletedTag = i;
    }

    public int getUserId() {
        return this.mUserId;
    }

    public void setUserId(int i) {
        this.mUserId = i;
    }
}
