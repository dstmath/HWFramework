package android_maps_conflict_avoidance.com.google.common.graphics.android;

import android.graphics.Bitmap;
import android_maps_conflict_avoidance.com.google.common.graphics.GoogleGraphics;
import android_maps_conflict_avoidance.com.google.common.graphics.GoogleImage;

class ScaledAndroidImage extends AndroidImage {
    private AndroidImage cache = null;
    private final int dh;
    private final int dw;
    private final AndroidImage parent;
    private final int sh;
    private final int sw;
    private final int sx;
    private final int sy;

    public ScaledAndroidImage(AndroidImage parent, int dw, int dh, int sx, int sy, int sw, int sh) {
        super(null);
        parent.pin();
        this.parent = parent;
        this.dw = dw;
        this.dh = dh;
        this.sx = sx;
        this.sy = sy;
        this.sw = sw;
        this.sh = sh;
    }

    private AndroidImage getCache() {
        if (this.cache == null) {
            this.cache = new AndroidImage(this.dw, this.dh);
            if (!this.cache.getGraphics().drawScaledImage(this.parent, 0, 0, this.dw, this.dh, this.sx, this.sy, this.sw, this.sh)) {
                throw new UnsupportedOperationException("Graphics cannot scale image: " + this.cache.getGraphics());
            }
        }
        return this.cache;
    }

    public GoogleImage createScaledImage(int srcX, int srcY, int srcWidth, int srcHeight, int newWidth, int newHeight) {
        return getCache().createScaledImage(srcX, srcY, srcWidth, srcHeight, newWidth, newHeight);
    }

    public void drawImage(GoogleGraphics g, int x, int y) {
        if (this.cache != null) {
            this.cache.drawImage(g, x, y);
            return;
        }
        g.drawScaledImage(this.parent, x, y, this.dw, this.dh, this.sx, this.sy, this.sw, this.sh);
    }

    public Bitmap getBitmap() {
        return getCache().getBitmap();
    }

    public GoogleGraphics getGraphics() {
        return getCache().getGraphics();
    }

    public int getHeight() {
        return this.dh;
    }

    public int getWidth() {
        return this.dw;
    }
}
