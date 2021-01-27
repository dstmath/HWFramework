package com.android.server.wm.utils;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.DisplayInfo;

public class CoordinateTransforms {
    private CoordinateTransforms() {
    }

    public static void transformPhysicalToLogicalCoordinates(int rotation, int physicalWidth, int physicalHeight, Matrix out) {
        if (rotation == 0) {
            out.reset();
        } else if (rotation == 1) {
            out.setRotate(270.0f);
            out.postTranslate(0.0f, (float) physicalWidth);
        } else if (rotation == 2) {
            out.setRotate(180.0f);
            out.postTranslate((float) physicalWidth, (float) physicalHeight);
        } else if (rotation == 3) {
            out.setRotate(90.0f);
            out.postTranslate((float) physicalHeight, 0.0f);
        } else {
            throw new IllegalArgumentException("Unknown rotation: " + rotation);
        }
    }

    public static void transformLogicalToPhysicalCoordinates(int rotation, int physicalWidth, int physicalHeight, Matrix out) {
        if (rotation == 0) {
            out.reset();
        } else if (rotation == 1) {
            out.setRotate(90.0f);
            out.preTranslate(0.0f, (float) (-physicalWidth));
        } else if (rotation == 2) {
            out.setRotate(180.0f);
            out.preTranslate((float) (-physicalWidth), (float) (-physicalHeight));
        } else if (rotation == 3) {
            out.setRotate(270.0f);
            out.preTranslate((float) (-physicalHeight), 0.0f);
        } else {
            throw new IllegalArgumentException("Unknown rotation: " + rotation);
        }
    }

    public static void transformToRotation(int oldRotation, int newRotation, DisplayInfo info, Matrix out) {
        boolean flipped = true;
        if (!(info.rotation == 1 || info.rotation == 3)) {
            flipped = false;
        }
        int h = flipped ? info.logicalWidth : info.logicalHeight;
        int w = flipped ? info.logicalHeight : info.logicalWidth;
        Matrix tmp = new Matrix();
        transformLogicalToPhysicalCoordinates(oldRotation, w, h, out);
        transformPhysicalToLogicalCoordinates(newRotation, w, h, tmp);
        out.postConcat(tmp);
    }

    public static void transformToRotation(int oldRotation, int newRotation, int newWidth, int newHeight, Matrix out) {
        boolean flipped = true;
        if (!(newRotation == 1 || newRotation == 3)) {
            flipped = false;
        }
        int h = flipped ? newWidth : newHeight;
        int w = flipped ? newHeight : newWidth;
        Matrix tmp = new Matrix();
        transformLogicalToPhysicalCoordinates(oldRotation, w, h, out);
        transformPhysicalToLogicalCoordinates(newRotation, w, h, tmp);
        out.postConcat(tmp);
    }

    public static void transformRect(Matrix transform, Rect inOutRect, RectF tmp) {
        if (tmp == null) {
            tmp = new RectF();
        }
        tmp.set(inOutRect);
        transform.mapRect(tmp);
        inOutRect.set((int) tmp.left, (int) tmp.top, (int) tmp.right, (int) tmp.bottom);
    }
}
