package com.huawei.android.statistical;

import android.content.Context;
import com.huawei.bd.Reporter;

public class StatisticalUtils {
    private static final String TAG = "StatisticalUtils";

    public static void reportc(Context context, int eventID) {
        Reporter.c(context, eventID);
    }

    public static void reporte(Context context, int eventID, String eventMsg) {
        Reporter.e(context, eventID, eventMsg);
    }
}
