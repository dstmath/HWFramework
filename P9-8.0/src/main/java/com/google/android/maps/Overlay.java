package com.google.android.maps;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.MotionEvent;

public abstract class Overlay {
    protected static final float SHADOW_X_SKEW = -0.9f;
    protected static final float SHADOW_Y_SCALE = 0.5f;

    public interface Snappable {
        boolean onSnapToItem(int i, int i2, Point point, MapView mapView);
    }

    protected static void drawAt(Canvas canvas, Drawable drawable, int x, int y, boolean shadow) {
        if (x <= 16000 && y <= 16000 && x >= -16000 && y >= -16000) {
            if (shadow) {
                drawable.setColorFilter(2130706432, Mode.SRC_IN);
            }
            canvas.save();
            canvas.translate((float) x, (float) y);
            if (shadow) {
                canvas.skew(SHADOW_X_SKEW, 0.0f);
                canvas.scale(1.0f, SHADOW_Y_SCALE);
            }
            drawable.draw(canvas);
            if (shadow) {
                drawable.clearColorFilter();
            }
            canvas.restore();
        }
    }

    public boolean onTouchEvent(MotionEvent e, MapView mapView) {
        return false;
    }

    public boolean onTrackballEvent(MotionEvent event, MapView mapView) {
        return false;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event, MapView mapView) {
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event, MapView mapView) {
        return false;
    }

    public boolean onTap(GeoPoint p, MapView mapView) {
        return false;
    }

    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
    }

    public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
        draw(canvas, mapView, shadow);
        return false;
    }
}
