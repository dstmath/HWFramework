package com.huawei.android.graphics;

import android.graphics.BitmapFactory;

public class BitmapFactoryEx {
    public static void setInThumbnailMode(BitmapFactory.Options options, boolean inThumbnailMode) {
        if (options != null) {
            options.inThumbnailMode = inThumbnailMode;
        }
    }
}
