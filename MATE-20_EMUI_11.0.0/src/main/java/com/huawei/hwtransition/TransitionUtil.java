package com.huawei.hwtransition;

import android.view.View;
import android.view.ViewGroup;

public class TransitionUtil {
    private TransitionUtil() {
    }

    public static float getScrollProgress(ViewGroup targetView, int transitionX, View v, int page, int pageSpacing) {
        return Math.max(Math.min(((float) (transitionX - getChildOffset(targetView, page, pageSpacing))) / ((((float) v.getWidth()) * v.getScaleX()) + ((float) pageSpacing)), 2.0f), -2.0f);
    }

    static int getChildOffset(ViewGroup targetView, int index, int pageSpacing) {
        float offset = 0.0f;
        for (int i = 0; i < index; i++) {
            View child = targetView.getChildAt(i);
            offset += (((float) child.getMeasuredWidth()) * child.getScaleX()) + ((float) pageSpacing);
        }
        return (int) offset;
    }
}
