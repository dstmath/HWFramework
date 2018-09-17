package android.graphics.drawable;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class HwRippleForegroundProxy extends HwRippleForeground {
    HwRippleForegroundImpl mHwRippleForegroundImpl;

    public HwRippleForegroundProxy(RippleDrawable owner, Rect bounds, boolean isBounded, boolean forceSoftware, int type) {
        super(owner, bounds, isBounded, forceSoftware, type);
        this.mHwRippleForegroundImpl = new HwRippleForegroundImpl(owner, bounds, isBounded, forceSoftware, type);
    }

    public void end() {
        if (this.mHwRippleForegroundImpl != null) {
            this.mHwRippleForegroundImpl.end();
        }
    }

    public void onBoundsChange() {
        if (this.mHwRippleForegroundImpl != null) {
            this.mHwRippleForegroundImpl.onBoundsChange();
        }
    }

    public final void setup(float maxRadius, int densityDpi) {
        if (this.mHwRippleForegroundImpl != null) {
            this.mHwRippleForegroundImpl.setup(maxRadius, densityDpi);
        }
    }

    public final void enter(boolean fast) {
        if (this.mHwRippleForegroundImpl != null) {
            this.mHwRippleForegroundImpl.enter(fast);
        }
    }

    public final void exit() {
        if (this.mHwRippleForegroundImpl != null) {
            this.mHwRippleForegroundImpl.exit();
        }
    }

    protected final void onHotspotBoundsChanged() {
        if (this.mHwRippleForegroundImpl != null) {
            this.mHwRippleForegroundImpl.onHotspotBoundsChanged();
        }
    }

    public boolean draw(Canvas c, Paint p) {
        if (this.mHwRippleForegroundImpl != null) {
            return this.mHwRippleForegroundImpl.draw(c, p);
        }
        return false;
    }

    public void getBounds(Rect bounds) {
        if (this.mHwRippleForegroundImpl != null) {
            this.mHwRippleForegroundImpl.getBounds(bounds);
        }
    }
}
