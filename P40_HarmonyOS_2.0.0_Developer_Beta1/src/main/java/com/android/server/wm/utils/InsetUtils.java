package com.android.server.wm.utils;

import android.graphics.Rect;

public class InsetUtils {
    private InsetUtils() {
    }

    public static void rotateInsets(Rect inOutInsets, int rotationDelta) {
        if (rotationDelta == 0) {
            return;
        }
        if (rotationDelta == 1) {
            inOutInsets.set(inOutInsets.top, inOutInsets.right, inOutInsets.bottom, inOutInsets.left);
        } else if (rotationDelta == 2) {
            inOutInsets.set(inOutInsets.right, inOutInsets.bottom, inOutInsets.left, inOutInsets.top);
        } else if (rotationDelta == 3) {
            inOutInsets.set(inOutInsets.bottom, inOutInsets.left, inOutInsets.top, inOutInsets.right);
        } else {
            throw new IllegalArgumentException("Unknown rotation: " + rotationDelta);
        }
    }

    public static void addInsets(Rect inOutInsets, Rect insetsToAdd) {
        inOutInsets.left += insetsToAdd.left;
        inOutInsets.top += insetsToAdd.top;
        inOutInsets.right += insetsToAdd.right;
        inOutInsets.bottom += insetsToAdd.bottom;
    }

    public static void insetsBetweenFrames(Rect outerFrame, Rect innerFrame, Rect outInsets) {
        if (innerFrame == null) {
            outInsets.setEmpty();
            return;
        }
        int w = outerFrame.width();
        int h = outerFrame.height();
        outInsets.set(Math.min(w, Math.max(0, innerFrame.left - outerFrame.left)), Math.min(h, Math.max(0, innerFrame.top - outerFrame.top)), Math.min(w, Math.max(0, outerFrame.right - innerFrame.right)), Math.min(h, Math.max(0, outerFrame.bottom - innerFrame.bottom)));
    }
}
