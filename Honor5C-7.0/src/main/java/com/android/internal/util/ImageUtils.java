package com.android.internal.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.WindowManager.LayoutParams;
import com.android.internal.logging.MetricsProto.MetricsEvent;

public class ImageUtils {
    private static final int ALPHA_TOLERANCE = 50;
    private static final int COMPACT_BITMAP_SIZE = 64;
    private static final int TOLERANCE = 20;
    private int[] mTempBuffer;
    private Bitmap mTempCompactBitmap;
    private Canvas mTempCompactBitmapCanvas;
    private Paint mTempCompactBitmapPaint;
    private final Matrix mTempMatrix;

    public ImageUtils() {
        this.mTempMatrix = new Matrix();
    }

    public boolean isGrayscale(Bitmap bitmap) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        if (height > COMPACT_BITMAP_SIZE || width > COMPACT_BITMAP_SIZE) {
            if (this.mTempCompactBitmap == null) {
                this.mTempCompactBitmap = Bitmap.createBitmap(COMPACT_BITMAP_SIZE, COMPACT_BITMAP_SIZE, Config.ARGB_8888);
                this.mTempCompactBitmapCanvas = new Canvas(this.mTempCompactBitmap);
                this.mTempCompactBitmapPaint = new Paint(1);
                this.mTempCompactBitmapPaint.setFilterBitmap(true);
            }
            this.mTempMatrix.reset();
            this.mTempMatrix.setScale(64.0f / ((float) width), 64.0f / ((float) height), 0.0f, 0.0f);
            this.mTempCompactBitmapCanvas.drawColor(0, Mode.SRC);
            this.mTempCompactBitmapCanvas.drawBitmap(bitmap, this.mTempMatrix, this.mTempCompactBitmapPaint);
            bitmap = this.mTempCompactBitmap;
            height = COMPACT_BITMAP_SIZE;
            width = COMPACT_BITMAP_SIZE;
        }
        int size = height * width;
        ensureBufferSize(size);
        bitmap.getPixels(this.mTempBuffer, 0, width, 0, 0, width, height);
        for (int i = 0; i < size; i++) {
            if (!isGrayscale(this.mTempBuffer[i])) {
                return false;
            }
        }
        return true;
    }

    private void ensureBufferSize(int size) {
        if (this.mTempBuffer == null || this.mTempBuffer.length < size) {
            this.mTempBuffer = new int[size];
        }
    }

    public static boolean isGrayscale(int color) {
        boolean z = true;
        if (((color >> 24) & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE) < ALPHA_TOLERANCE) {
            return true;
        }
        int r = (color >> 16) & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
        int g = (color >> 8) & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
        int b = color & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
        if (Math.abs(r - g) >= TOLERANCE || Math.abs(r - b) >= TOLERANCE) {
            z = false;
        } else if (Math.abs(g - b) >= TOLERANCE) {
            z = false;
        }
        return z;
    }

    public static Bitmap buildScaledBitmap(Drawable drawable, int maxWidth, int maxHeight) {
        if (drawable == null) {
            return null;
        }
        int originalWidth = drawable.getIntrinsicWidth();
        int originalHeight = drawable.getIntrinsicHeight();
        if (originalWidth <= maxWidth && originalHeight <= maxHeight && (drawable instanceof BitmapDrawable)) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        if (originalHeight <= 0 || originalWidth <= 0) {
            return null;
        }
        float ratio = Math.min(LayoutParams.BRIGHTNESS_OVERRIDE_FULL, Math.min(((float) maxWidth) / ((float) originalWidth), ((float) maxHeight) / ((float) originalHeight)));
        int scaledWidth = (int) (((float) originalWidth) * ratio);
        int scaledHeight = (int) (((float) originalHeight) * ratio);
        Bitmap result = Bitmap.createBitmap(scaledWidth, scaledHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        drawable.setBounds(0, 0, scaledWidth, scaledHeight);
        drawable.draw(canvas);
        return result;
    }
}
