package com.huawei.android.feature.fingerprint;

import android.content.Context;
import com.huawei.android.feature.module.DynamicModuleInfo;

public interface SignVerifyStrategy {
    boolean verifyFingerPrint(Context context, DynamicModuleInfo dynamicModuleInfo);
}
