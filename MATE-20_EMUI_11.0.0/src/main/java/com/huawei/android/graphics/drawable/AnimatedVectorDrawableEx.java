package com.huawei.android.graphics.drawable;

import android.graphics.drawable.AnimatedVectorDrawable;

public class AnimatedVectorDrawableEx {
    public static void forceAnimationOnUI(AnimatedVectorDrawable drawable) {
        if (drawable != null) {
            drawable.forceAnimationOnUI();
        }
    }
}
