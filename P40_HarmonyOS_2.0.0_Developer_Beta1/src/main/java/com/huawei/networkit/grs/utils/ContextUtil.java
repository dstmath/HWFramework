package com.huawei.networkit.grs.utils;

import android.content.Context;
import com.huawei.networkit.grs.common.CheckParamUtils;

public class ContextUtil {
    private static Context sContext;

    public static Context getContext() {
        return sContext;
    }

    public static void setContext(Context context) {
        CheckParamUtils.checkNotNull(context, "grs'context == null");
        if (context != null) {
            sContext = context.getApplicationContext();
        }
    }
}
