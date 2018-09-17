package com.huawei.hwtransition;

import android.graphics.Rect;
import android.view.View;

public class AlgorithmUtil {
    public static float transformPivotX(View view, float pivotX) {
        float basePivotX = view.getPivotX();
        float sourcePivotX = pivotX;
        return ((pivotX - basePivotX) * view.getScaleX()) + basePivotX;
    }

    public static float transformPivotY(View view, float pivotY) {
        float basePivotY = view.getPivotY();
        float sourcePivotY = pivotY;
        return ((pivotY - basePivotY) * view.getScaleY()) + basePivotY;
    }

    public static void getTransformRect(View child, Rect rect) {
        rect.set((int) (transformPivotX(child, 0.0f) + child.getTranslationX()), (int) (transformPivotY(child, 0.0f) + child.getTranslationY()), (int) (transformPivotX(child, (float) child.getWidth()) + child.getTranslationX()), (int) (transformPivotY(child, (float) child.getHeight()) + child.getTranslationY()));
    }
}
