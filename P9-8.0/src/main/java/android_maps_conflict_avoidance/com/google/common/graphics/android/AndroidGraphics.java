package android_maps_conflict_avoidance.com.google.common.graphics.android;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android_maps_conflict_avoidance.com.google.common.graphics.GoogleGraphics;
import android_maps_conflict_avoidance.com.google.common.graphics.GoogleImage;

public class AndroidGraphics implements GoogleGraphics {
    private static final Rect clipRect = new Rect();
    private static final Rect destRect = new Rect();
    private static final RectF oval = new RectF();
    private static final Rect sourceRect = new Rect();
    private Canvas canvas;
    private final Paint paint = new Paint();

    public AndroidGraphics(Canvas c) {
        this.paint.setStrokeWidth(1.0f);
        this.canvas = c;
    }

    public void setCanvas(Canvas c) {
        this.canvas = c;
    }

    public Canvas getCanvas() {
        return this.canvas;
    }

    public void setColor(int color) {
        this.paint.setColor(-16777216 | color);
    }

    public void fillRect(int x, int y, int width, int height) {
        this.paint.setStyle(Style.FILL);
        this.canvas.drawRect((float) x, (float) y, (float) (x + width), (float) (y + height), this.paint);
    }

    public void drawImage(GoogleImage img, int x, int y) {
        if (img != null) {
            img.drawImage(this, x, y);
        }
    }

    public boolean drawScaledImage(GoogleImage image, int dx, int dy, int dw, int dh, int sx, int sy, int sw, int sh) {
        if (image == null) {
            return false;
        }
        Bitmap bitmap = ((AndroidImage) image).getBitmap();
        if (bitmap == null) {
            return false;
        }
        sourceRect.set(sx, sy, sx + sw, sy + sh);
        destRect.set(dx, dy, dx + dw, dy + dh);
        this.canvas.drawBitmap(bitmap, sourceRect, destRect, null);
        return true;
    }
}
