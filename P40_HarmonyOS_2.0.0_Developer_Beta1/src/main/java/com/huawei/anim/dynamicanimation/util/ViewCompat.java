package com.huawei.anim.dynamicanimation.util;

import android.os.Build;
import android.view.View;

public class ViewCompat {
    private static final int a = 21;

    private ViewCompat() {
    }

    public static void setTranslationZ(View view, float f) {
        if (Build.VERSION.SDK_INT >= 21) {
            view.setTranslationZ(f);
        }
    }

    public static float getTranslationZ(View view) {
        if (Build.VERSION.SDK_INT >= 21) {
            return view.getTranslationZ();
        }
        return 0.0f;
    }

    public static void setZ(View view, float f) {
        if (Build.VERSION.SDK_INT >= 21) {
            view.setZ(f);
        }
    }

    public static float getZ(View view) {
        if (Build.VERSION.SDK_INT >= 21) {
            return view.getZ();
        }
        return 0.0f;
    }
}
