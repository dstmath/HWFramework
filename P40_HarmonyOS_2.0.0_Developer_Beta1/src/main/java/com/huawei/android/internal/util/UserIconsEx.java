package com.huawei.android.internal.util;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import com.android.internal.util.UserIcons;

public class UserIconsEx {
    public static Bitmap convertToBitmap(Drawable icon) {
        return UserIcons.convertToBitmap(icon);
    }
}
