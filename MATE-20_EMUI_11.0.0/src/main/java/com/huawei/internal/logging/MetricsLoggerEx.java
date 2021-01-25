package com.huawei.internal.logging;

import android.content.Context;
import com.android.internal.logging.MetricsLogger;

public class MetricsLoggerEx {
    public static void action(Context context, int category) {
        MetricsLogger.action(context, category);
    }

    public static void action(Context context, int category, int value) {
        MetricsLogger.action(context, category, value);
    }

    public static void visible(Context context, int category) throws IllegalArgumentException {
        MetricsLogger.visible(context, category);
    }
}
