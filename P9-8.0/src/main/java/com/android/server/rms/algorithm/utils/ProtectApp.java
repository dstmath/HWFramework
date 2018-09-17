package com.android.server.rms.algorithm.utils;

public class ProtectApp {
    private String mAppPkgName;
    private int mAppType;
    private float mAvgUsedFrequency;
    private int mDeletedTag;
    private String mRecentUsed;

    public ProtectApp(String appPkgName, int appType, int deleted, float avgUsedFrequency) {
        this.mAppPkgName = appPkgName;
        this.mAppType = appType;
        this.mDeletedTag = deleted;
        this.mAvgUsedFrequency = avgUsedFrequency;
    }

    public ProtectApp(String appPkgName, int appType, String recentUsed, float avgUsedFrequency) {
        this.mAppPkgName = appPkgName;
        this.mAppType = appType;
        this.mRecentUsed = recentUsed;
        this.mAvgUsedFrequency = avgUsedFrequency;
    }

    public ProtectApp(String appPkgName, int appType, int deleted) {
        this.mAppPkgName = appPkgName;
        this.mAppType = appType;
        this.mDeletedTag = deleted;
    }

    public String getAppPkgName() {
        return this.mAppPkgName;
    }

    public void setAppPkgName(String appPkgName) {
        this.mAppPkgName = appPkgName;
    }

    public float getAvgUsedFrequency() {
        return this.mAvgUsedFrequency;
    }

    public void setAvgUsedFrequency(float avgUsedFrequency) {
        this.mAvgUsedFrequency = avgUsedFrequency;
    }

    public String getRecentUsed() {
        return this.mRecentUsed;
    }

    public void setRecentUsed(String recentUsed) {
        this.mRecentUsed = recentUsed;
    }

    public int getAppType() {
        return this.mAppType;
    }

    public void setAppType(int appType) {
        this.mAppType = appType;
    }

    public int getDeletedTag() {
        return this.mDeletedTag;
    }

    public void setDeletedTag(int deletedTag) {
        this.mDeletedTag = deletedTag;
    }
}
