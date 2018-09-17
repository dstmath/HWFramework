package com.google.android.maps;

import android.graphics.Canvas;
import android.view.KeyEvent;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class OverlayBundle {
    private final List<Overlay> mOverlays = Collections.synchronizedList(new ArrayList());

    OverlayBundle() {
    }

    boolean draw(Canvas canvas, MapView mapView, long drawTime) {
        boolean again = false;
        for (Overlay overlay : this.mOverlays) {
            again |= overlay.draw(canvas, mapView, true, drawTime);
        }
        for (Overlay overlay2 : this.mOverlays) {
            again |= overlay2.draw(canvas, mapView, false, drawTime);
        }
        return again;
    }

    boolean onTouchEvent(MotionEvent e, MapView mapView) {
        for (int i = this.mOverlays.size() - 1; i >= 0; i--) {
            if (((Overlay) this.mOverlays.get(i)).onTouchEvent(e, mapView)) {
                return true;
            }
        }
        return false;
    }

    boolean onTap(GeoPoint p, MapView mapView) {
        for (int i = this.mOverlays.size() - 1; i >= 0; i--) {
            if (((Overlay) this.mOverlays.get(i)).onTap(p, mapView)) {
                return true;
            }
        }
        return false;
    }

    boolean onKeyDown(int keyCode, KeyEvent event, MapView mapView) {
        for (int i = this.mOverlays.size() - 1; i >= 0; i--) {
            if (((Overlay) this.mOverlays.get(i)).onKeyDown(keyCode, event, mapView)) {
                return true;
            }
        }
        return false;
    }

    boolean onKeyUp(int keyCode, KeyEvent event, MapView mapView) {
        for (int i = this.mOverlays.size() - 1; i >= 0; i--) {
            if (((Overlay) this.mOverlays.get(i)).onKeyUp(keyCode, event, mapView)) {
                return true;
            }
        }
        return false;
    }

    boolean onTrackballEvent(MotionEvent event, MapView mapView) {
        for (int i = this.mOverlays.size() - 1; i >= 0; i--) {
            if (((Overlay) this.mOverlays.get(i)).onTrackballEvent(event, mapView)) {
                return true;
            }
        }
        return false;
    }

    public List<Overlay> getOverlays() {
        return this.mOverlays;
    }
}
