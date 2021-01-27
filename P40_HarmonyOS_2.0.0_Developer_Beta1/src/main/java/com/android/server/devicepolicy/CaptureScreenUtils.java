package com.android.server.devicepolicy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.SurfaceControl;
import android.view.WindowManager;

public class CaptureScreenUtils {
    private static final float DEGREES_FOR_ROTATION_0 = 0.0f;
    private static final float DEGREES_FOR_ROTATION_180 = 180.0f;
    private static final float DEGREES_FOR_ROTATION_270 = 270.0f;
    private static final float DEGREES_FOR_ROTATION_360 = 360.0f;
    private static final float DEGREES_FOR_ROTATION_90 = 90.0f;
    private static final String TAG = "CaptureScreenUtils";

    private CaptureScreenUtils() {
    }

    public static Bitmap captureScreen(Context context) {
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        Matrix matrix = new Matrix();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getRealMetrics(displayMetrics);
        float[] dims = {(float) displayMetrics.widthPixels, (float) displayMetrics.heightPixels};
        float degrees = getDegreesForRotation(display.getRotation());
        boolean isRequiresRotation = degrees > DEGREES_FOR_ROTATION_0;
        if (isRequiresRotation) {
            matrix.reset();
            matrix.preRotate(-degrees);
            matrix.mapPoints(dims);
            dims[0] = Math.abs(dims[0]);
            dims[1] = Math.abs(dims[1]);
        }
        Bitmap bmp = SurfaceControl.screenshot(new Rect(), (int) dims[0], (int) dims[1], 0);
        if (!isRequiresRotation || bmp == null) {
            return bmp;
        }
        matrix.setRotate(degrees);
        Bitmap ss = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        bmp.recycle();
        return ss;
    }

    private static float getDegreesForRotation(int rotation) {
        if (rotation == 1) {
            return DEGREES_FOR_ROTATION_270;
        }
        if (rotation == 2) {
            return DEGREES_FOR_ROTATION_180;
        }
        if (rotation != 3) {
            return DEGREES_FOR_ROTATION_0;
        }
        return DEGREES_FOR_ROTATION_90;
    }
}
