package com.huawei.android.view.animation;

import android.graphics.Rect;
import android.view.animation.Transformation;

public class TransformationEx {
    public static boolean hasClipRect(Transformation transformation) {
        return transformation.hasClipRect();
    }

    public static Rect getClipRect(Transformation transformation) {
        return transformation.getClipRect();
    }
}
