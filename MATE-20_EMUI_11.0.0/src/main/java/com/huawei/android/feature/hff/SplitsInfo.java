package com.huawei.android.feature.hff;

public class SplitsInfo {
    public int armeabiType;
    public String splitName;
    public String splitPath;
    public int splitVersion;

    public SplitsInfo(String str, String str2, int i) {
        this.splitName = str;
        this.splitPath = str2;
        this.splitVersion = i;
    }

    public SplitsInfo(String str, String str2, int i, int i2) {
        this.splitName = str;
        this.splitPath = str2;
        this.splitVersion = i;
        this.armeabiType = i2;
    }
}
