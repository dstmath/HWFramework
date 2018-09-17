package com.huawei.android.content;

import android.content.ClipData;
import android.graphics.Bitmap;

public class ClipDataEx {
    public static Bitmap getIcon(ClipData clipData) {
        return clipData.getIcon();
    }
}
