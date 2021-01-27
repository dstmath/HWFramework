package com.huawei.android.feature.install;

import android.content.Context;
import com.huawei.android.feature.module.DynamicModuleInfo;

public interface IFeatureInstall {
    int installFeatureFromUnverifyIfNeed(Context context, DynamicModuleInfo dynamicModuleInfo);

    boolean installFeatureFromVerify(Context context, DynamicModuleInfo dynamicModuleInfo);

    int moveToVerifyIfNeed(Context context, DynamicModuleInfo dynamicModuleInfo);
}
