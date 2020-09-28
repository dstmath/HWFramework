package android.widget.sr;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class DebugUtil {
    private static int BITMAP_PIXEL_COLOR = -23296;
    private static final boolean DEBUG_SWITCHER = false;
    private static int INFO_COLOR = -65536;
    private static int PAINT_STROKE_WIDTH = 10;
    private static int TEXT_SIZE = 50;

    public static void drawCircle(Bitmap bitmap, int color, float r) {
    }

    public static void drawBitmapPixel(Bitmap bitmap) {
    }

    public static void drawText(Bitmap bitmap, String info) {
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(INFO_COLOR);
        paint.setStrokeWidth((float) PAINT_STROKE_WIDTH);
        paint.setTextSize((float) TEXT_SIZE);
        canvas.drawText(info, 0.0f, (((float) bitmap.getHeight()) / 2.0f) + 80.0f, paint);
    }

    public static void debugTimeout(Bitmap bitmap, boolean started) {
    }

    public static void debugDone(Bitmap bitmap, float scale, long startTime, boolean success) {
    }

    public static void debugPixelFail(Bitmap bitmap) {
    }
}
