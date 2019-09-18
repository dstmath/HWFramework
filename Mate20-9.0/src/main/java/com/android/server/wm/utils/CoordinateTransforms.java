package com.android.server.wm.utils;

import android.graphics.Matrix;

public class CoordinateTransforms {
    private CoordinateTransforms() {
    }

    public static void transformPhysicalToLogicalCoordinates(int rotation, int physicalWidth, int physicalHeight, Matrix out) {
        switch (rotation) {
            case 0:
                out.reset();
                return;
            case 1:
                out.setRotate(270.0f);
                out.postTranslate(0.0f, (float) physicalWidth);
                return;
            case 2:
                out.setRotate(180.0f);
                out.postTranslate((float) physicalWidth, (float) physicalHeight);
                return;
            case 3:
                out.setRotate(90.0f);
                out.postTranslate((float) physicalHeight, 0.0f);
                return;
            default:
                throw new IllegalArgumentException("Unknown rotation: " + rotation);
        }
    }
}
