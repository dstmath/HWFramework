package com.huawei.android.immersion;

import android.content.Context;
import android.content.res.Resources;
import android.hwcontrol.HwWidgetFactory;

public class ImmersionStyle {
    public static int getPrimaryColor(Context context) {
        if (context != null) {
            return HwWidgetFactory.getPrimaryColor(context);
        }
        throw new NullPointerException("getPrimaryColor called with null Context.");
    }

    public static int getSuggestionForgroundColorStyle(int colorBackground) {
        return HwWidgetFactory.getSuggestionForgroundColorStyle(colorBackground);
    }

    public static int getControlColor(Context context) {
        if (context != null) {
            Resources res = context.getResources();
            if (res != null) {
                return HwWidgetFactory.getControlColor(res);
            }
        }
        return 0;
    }
}
