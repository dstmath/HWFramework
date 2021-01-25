package com.huawei.android.feature.module;

public class DynamicParams {
    public Class<?> mClassType;
    public Object mParam;

    public DynamicParams(Class<?> cls, Object obj) {
        this.mClassType = cls;
        this.mParam = obj;
    }
}
