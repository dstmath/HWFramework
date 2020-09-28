package com.huawei.android.graphics.drawable;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;

public class IconEx {
    public static int getType(Icon icon) {
        return icon.getType();
    }

    public static int getTypeResource() {
        return 2;
    }

    public static String getResPackage(Icon icon) {
        return icon.getResPackage();
    }

    public static int getResId(Icon icon) {
        return icon.getResId();
    }

    public static int getTypeUri() {
        return 4;
    }

    public static String getUriString(Icon icon) {
        return icon.getUriString();
    }

    public static Drawable loadDrawableAsUser(Icon icon, Context context, int userId) {
        return icon.loadDrawableAsUser(context, userId);
    }

    public static boolean sameAs(Icon icon1, Icon icon2) {
        return icon1.sameAs(icon2);
    }
}
