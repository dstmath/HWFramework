package android.widget.sr;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class DebugUtil {
    private static final int BITMAP_PIXEL_COLOR = -23296;
    private static final boolean DEBUG_SWITCHER = false;
    private static final float DICHOTOMY_SIZE = 2.0f;
    private static final int INFO_COLOR = -65536;
    private static final int PAINT_STROKE_WIDTH = 10;
    private static final int TEXT_SIZE = 50;

    public static void drawCircle(Bitmap bitmap, int color, float radius) {
    }

    public static void drawBitmapPixel(Bitmap bitmap) {
    }

    public static void drawText(Bitmap bitmap, String info) {
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(INFO_COLOR);
        paint.setStrokeWidth(10.0f);
        paint.setTextSize(50.0f);
        canvas.drawText(info, 0.0f, (((float) bitmap.getHeight()) / 2.0f) + 80.0f, paint);
    }

    public static void debugTimeout(Bitmap bitmap, boolean started) {
    }

    public static void debugDone(Bitmap bitmap, float scale, long startTime, boolean success) {
    }

    public static void debugPixelFail(Bitmap bitmap) {
    }
}
