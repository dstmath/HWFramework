package com.huawei.securitycenter;

import java.util.Arrays;

public class HwVirusAppInfo {
    private int mApkCategory;
    private String mApkPackageName;
    private byte[] mApkSha256;

    public HwVirusAppInfo(int apkCategory, String apkPackageName, byte[] apkSha256) {
        this.mApkCategory = apkCategory;
        this.mApkPackageName = apkPackageName;
        this.mApkSha256 = new byte[apkSha256.length];
        System.arraycopy(apkSha256, 0, this.mApkSha256, 0, apkSha256.length);
    }

    public int getApkCategory() {
        return this.mApkCategory;
    }

    public String getApkPackageName() {
        return this.mApkPackageName;
    }

    public byte[] getApkSha256() {
        byte[] bArr = this.mApkSha256;
        byte[] apksha256 = new byte[bArr.length];
        System.arraycopy(bArr, 0, apksha256, 0, bArr.length);
        return apksha256;
    }

    public String toString() {
        return "HwVirusAppInfo{mApkCategory=" + this.mApkCategory + ", mApkPackageName='" + this.mApkPackageName + "', mApkSha256=" + Arrays.toString(this.mApkSha256) + '}';
    }
}
