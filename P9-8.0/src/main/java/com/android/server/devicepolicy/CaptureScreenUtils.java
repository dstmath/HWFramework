package com.android.server.devicepolicy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.SurfaceControl;
import android.view.WindowManager;

public class CaptureScreenUtils {
    public static Bitmap captureScreen(Context context) {
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        Matrix matrix = new Matrix();
        display.getRealMetrics(new DisplayMetrics());
        float[] dims = new float[]{(float) displayMetrics.widthPixels, (float) displayMetrics.heightPixels};
        float degrees = getDegreesForRotation(display.getRotation());
        boolean requiresRotation = degrees > 0.0f;
        if (requiresRotation) {
            matrix.reset();
            matrix.preRotate(-degrees);
            matrix.mapPoints(dims);
            dims[0] = Math.abs(dims[0]);
            dims[1] = Math.abs(dims[1]);
        }
        Bitmap bmp = SurfaceControl.screenshot((int) dims[0], (int) dims[1]);
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
                return 0.0f;
        }
    }
}
