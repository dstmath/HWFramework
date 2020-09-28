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
        HwRippleForegroundImpl hwRippleForegroundImpl = this.mHwRippleForegroundImpl;
        if (hwRippleForegroundImpl != null) {
            hwRippleForegroundImpl.end();
        }
    }

    public void onBoundsChange() {
        HwRippleForegroundImpl hwRippleForegroundImpl = this.mHwRippleForegroundImpl;
        if (hwRippleForegroundImpl != null) {
            hwRippleForegroundImpl.onBoundsChange();
        }
    }

    public final void setup(float maxRadius, int densityDpi) {
        HwRippleForegroundImpl hwRippleForegroundImpl = this.mHwRippleForegroundImpl;
        if (hwRippleForegroundImpl != null) {
            hwRippleForegroundImpl.setup(maxRadius, densityDpi);
        }
    }

    public final void enter(boolean fast) {
        HwRippleForegroundImpl hwRippleForegroundImpl = this.mHwRippleForegroundImpl;
        if (hwRippleForegroundImpl != null) {
            hwRippleForegroundImpl.enter();
        }
    }

    public final void exit() {
        HwRippleForegroundImpl hwRippleForegroundImpl = this.mHwRippleForegroundImpl;
        if (hwRippleForegroundImpl != null) {
            hwRippleForegroundImpl.exit();
        }
    }

    /* access modifiers changed from: protected */
    public final void onHotspotBoundsChanged() {
        HwRippleForegroundImpl hwRippleForegroundImpl = this.mHwRippleForegroundImpl;
        if (hwRippleForegroundImpl != null) {
            hwRippleForegroundImpl.onHotspotBoundsChanged();
        }
    }

    public boolean draw(Canvas c, Paint p) {
        HwRippleForegroundImpl hwRippleForegroundImpl = this.mHwRippleForegroundImpl;
        if (hwRippleForegroundImpl == null) {
            return false;
        }
        hwRippleForegroundImpl.draw(c, p);
        return true;
    }

    public void getBounds(Rect bounds) {
        HwRippleForegroundImpl hwRippleForegroundImpl = this.mHwRippleForegroundImpl;
        if (hwRippleForegroundImpl != null) {
            hwRippleForegroundImpl.getBounds(bounds);
        }
    }
}
