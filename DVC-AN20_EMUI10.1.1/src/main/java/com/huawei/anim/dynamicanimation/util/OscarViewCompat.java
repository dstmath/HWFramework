package com.huawei.anim.dynamicanimation.util;

import android.os.Build;
import android.view.View;

public class OscarViewCompat {
    public static void setTranslationZ(View view, float translationZ) {
        if (Build.VERSION.SDK_INT >= 21) {
            view.setTranslationZ(translationZ);
        }
    }

    public static float getTranslationZ(View view) {
        if (Build.VERSION.SDK_INT >= 21) {
            return view.getTranslationZ();
        }
        return 0.0f;
    }

    public static void setZ(View view, float z) {
        if (Build.VERSION.SDK_INT >= 21) {
            view.setZ(z);
        }
    }

    public static float getZ(View view) {
        if (Build.VERSION.SDK_INT >= 21) {
            return view.getZ();
        }
        return 0.0f;
    }
}
