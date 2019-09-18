package com.huawei.android.feature.compat.adapter;

public class V24 implements VersionApi {
    public boolean isDexOptNeeded(String str, String str2) {
        return CompatUtils.isDexOptNeeded(str, str2);
    }
}
