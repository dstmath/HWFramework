package com.huawei.android.feature.compat.adapter;

public class V26 implements VersionApi {
    @Override // com.huawei.android.feature.compat.adapter.VersionApi
    public boolean isDexOptNeeded(String str, String str2) {
        return CompatUtilsV1.isDexOptNeeded(str, str2);
    }
}
