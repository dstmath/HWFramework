package com.huawei.screenrecorder.activities;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.IBinder;
import android.view.Surface;
import android.view.SurfaceControl;

public class SurfaceControlEx {
    public static Bitmap screenshot(Rect sourceCrop, int width, int height, int minLayer, int maxLayer, boolean useIdentityTransform, int rotation) {
        return SurfaceControl.screenshot(sourceCrop, width, height, minLayer, maxLayer, useIdentityTransform, rotation);
    }

    @Deprecated
    public static Bitmap screenshot(int width, int height) {
        return null;
    }

    public static Bitmap screenshot(Rect sourceCrop, int width, int height, int rotation) {
        return SurfaceControl.screenshot(sourceCrop, width, height, rotation);
    }

    public static IBinder createDisplay(String name, boolean secure) {
        return SurfaceControl.createDisplay(name, secure);
    }

    public static void destroyDisplay(IBinder displayToken) {
        SurfaceControl.destroyDisplay(displayToken);
    }

    public static void openTransaction() {
        SurfaceControl.openTransaction();
    }

    public static void setDisplaySurface(IBinder displayToken, Surface surface) {
        SurfaceControl.setDisplaySurface(displayToken, surface);
    }

    public static void setDisplayLayerStack(IBinder displayToken, int layerStack) {
        SurfaceControl.setDisplayLayerStack(displayToken, layerStack);
    }

    public static void setDisplayProjection(IBinder displayToken, int orientation, Rect layerStackRect, Rect displayRect) {
        SurfaceControl.setDisplayProjection(displayToken, orientation, layerStackRect, displayRect);
    }

    public static void closeTransaction() {
        SurfaceControl.closeTransaction();
    }

    public static int isRogSupport() {
        return SurfaceControl.isRogSupport();
    }

    public static Bitmap screenshotHW(Rect sourceCrop, int width, int height, int minLayer, int maxLayer, boolean useIdentityTransform, int rotation) {
        return SurfaceControl.screenshot_ext_hw(sourceCrop, width, height, minLayer, maxLayer, useIdentityTransform, rotation);
    }

    public static Bitmap screenshotHW(Rect sourceCrop, int width, int height, int rotation) {
        return SurfaceControl.screenshot_ext_hw(sourceCrop, width, height, rotation);
    }
}
