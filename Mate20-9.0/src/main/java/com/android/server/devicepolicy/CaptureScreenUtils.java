package com.android.server.devicepolicy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.SurfaceControl;
import android.view.WindowManager;
import com.android.server.gesture.GestureNavConst;

public class CaptureScreenUtils {
    public static Bitmap captureScreen(Context context) {
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        Matrix matrix = new Matrix();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getRealMetrics(displayMetrics);
        float[] dims = {(float) displayMetrics.widthPixels, (float) displayMetrics.heightPixels};
        float degrees = getDegreesForRotation(display.getRotation());
        boolean requiresRotation = degrees > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        if (requiresRotation) {
            matrix.reset();
            matrix.preRotate(-degrees);
            matrix.mapPoints(dims);
            dims[0] = Math.abs(dims[0]);
            dims[1] = Math.abs(dims[1]);
        }
        Bitmap bmp = SurfaceControl.screenshot(new Rect(), (int) dims[0], (int) dims[1], 0);
        if (!requiresRotation || bmp == null) {
            return bmp;
        }
        matrix.setRotate(degrees);
        Bitmap ss = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        bmp.recycle();
        return ss;
    }

    private static float getDegreesForRotation(int rotation) {
        switch (rotation) {
            case 1:
                return 270.0f;
            case 2:
                return 180.0f;
            case 3:
                return 90.0f;
            default:
                return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
    }
}
