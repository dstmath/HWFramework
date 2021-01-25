package com.huawei.android.graphics;

import android.graphics.drawable.AnimatedRotateDrawable;
import android.graphics.drawable.Drawable;

public class AnimatedRotateDrawableEx {
    public static boolean setFramesCount(Drawable drawable, int framesCount) {
        if (!(drawable instanceof AnimatedRotateDrawable)) {
            return false;
        }
        ((AnimatedRotateDrawable) drawable).setFramesCount(framesCount);
        return true;
    }

    public static boolean setFramesDuration(Drawable drawable, int framesDuration) {
        if (!(drawable instanceof AnimatedRotateDrawable)) {
            return false;
        }
        ((AnimatedRotateDrawable) drawable).setFramesDuration(framesDuration);
        return true;
    }
}
