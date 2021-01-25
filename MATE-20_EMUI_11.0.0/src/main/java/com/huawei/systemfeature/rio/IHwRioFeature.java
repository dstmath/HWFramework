package com.huawei.systemfeature.rio;

import android.content.Context;
import android.view.Display;
import android.view.View;
import com.huawei.featurelayer.featureframework.IFeature;

public interface IHwRioFeature extends IFeature {
    public static final String RIO_CLASS = "com.huawei.systemfeature.rio.HwRio";
    public static final String RIO_PACKAGE = "com.huawei.systemfeature.rio";
    public static final String RIO_UTILS_CLASS = "com.huawei.systemfeature.rio.HwRioTools";

    void attachRio(Context context, View view, CharSequence charSequence, Display display);

    void detachRio();

    void focusChange(boolean z);

    void hookAttribute();
}
