package com.huawei.android.feature.install.signature;

import java.util.HashMap;

public class FeatureSignatureCompat {
    private static volatile FeatureSignatureCompat sInstance;
    private HashMap<String, String> mSignatureInfos = new HashMap<>();

    public static FeatureSignatureCompat getInstance() {
        if (sInstance == null) {
            synchronized (FeatureSignatureCompat.class) {
                if (sInstance == null) {
                    sInstance = new FeatureSignatureCompat();
                }
            }
        }
        return sInstance;
    }

    public void addExceptSignInfo(String str, String str2) {
        this.mSignatureInfos.put(str, str2);
    }

    public String getExceptSignInfo(String str) {
        return this.mSignatureInfos.get(str);
    }
}
