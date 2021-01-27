package com.android.server.wm.utils;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import com.android.internal.annotations.VisibleForTesting;

public class DisplayRotationUtil {
    private final Matrix mTmpMatrix = new Matrix();

    private static int getRotationToBoundsOffset(int rotation) {
        if (rotation == 0) {
            return 0;
        }
        if (rotation == 1) {
            return -1;
        }
        if (rotation == 2) {
            return 2;
        }
        if (rotation != 3) {
            return 0;
        }
        return 1;
    }

    @VisibleForTesting
    static int getBoundIndexFromRotation(int i, int rotation) {
        return Math.floorMod(getRotationToBoundsOffset(rotation) + i, 4);
    }

    public Rect[] getRotatedBounds(Rect[] bounds, int rotation, int initialDisplayWidth, int initialDisplayHeight) {
        if (bounds.length != 4) {
            throw new IllegalArgumentException("bounds must have exactly 4 elements: bounds=" + bounds);
        } else if (rotation == 0) {
            return bounds;
        } else {
            CoordinateTransforms.transformPhysicalToLogicalCoordinates(rotation, initialDisplayWidth, initialDisplayHeight, this.mTmpMatrix);
            Rect[] newBounds = new Rect[4];
            for (int i = 0; i < bounds.length; i++) {
                Rect rect = bounds[i];
                if (!rect.isEmpty()) {
                    RectF rectF = new RectF(rect);
                    this.mTmpMatrix.mapRect(rectF);
                    rectF.round(rect);
                }
                newBounds[getBoundIndexFromRotation(i, rotation)] = rect;
            }
            return newBounds;
        }
    }
}
