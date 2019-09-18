package com.android.server.wm.utils;

import android.graphics.Rect;

public class InsetUtils {
    private InsetUtils() {
    }

    public static void addInsets(Rect inOutInsets, Rect insetsToAdd) {
        inOutInsets.left += insetsToAdd.left;
        inOutInsets.top += insetsToAdd.top;
        inOutInsets.right += insetsToAdd.right;
        inOutInsets.bottom += insetsToAdd.bottom;
    }
}
