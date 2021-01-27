package com.huawei.hwtransition;

import android.view.View;
import android.view.ViewGroup;

public class TransitionUtil {
    private TransitionUtil() {
    }

    public static float getScrollProgress(ViewGroup targetView, int transitionX, View view, int page, int pageSpacing) {
        float minValue = 2.0f;
        if (view == null || targetView == null) {
            return 0.0f;
        }
        float totalDistance = (((float) view.getWidth()) * view.getScaleX()) + ((float) pageSpacing);
        float delta = (float) (transitionX - getChildOffset(targetView, page, pageSpacing));
        if (totalDistance == 0.0f) {
            return 0.0f;
        }
        float scrollProgress = delta / totalDistance;
        if (scrollProgress < 2.0f) {
            minValue = scrollProgress;
        }
        if (minValue > -2.0f) {
            return minValue;
        }
        return -2.0f;
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
