package com.huawei.android.app;

public final class HepPackageInfo {
    private static final String TAG = "HepPackageInfo";
    private String packageName;
    private String packagePath;
    private long status;
    private long versionCode;

    public void setPackageName(String packageName2) {
        this.packageName = packageName2;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setVersionCode(long versionCode2) {
        this.versionCode = versionCode2;
    }

    public long getVersionCode() {
        return this.versionCode;
    }

    public void setPackagePath(String packagePath2) {
        this.packagePath = packagePath2;
    }

    public String getPackagePath() {
        return this.packagePath;
    }

    public long getStatus() {
        return this.status;
    }

    public void setStatus(long status2) {
        this.status = status2;
    }

    public String toString() {
        return "HepPackageInfo: packageName:" + this.packageName + " packagePath:" + this.packagePath + " versionCode:" + this.versionCode;
    }
}
