package android.graphics;

import android.graphics.PorterDuff;

public class PorterDuffColorFilter extends ColorFilter {
    private int mColor;
    private PorterDuff.Mode mMode;

    private static native long native_CreatePorterDuffFilter(int i, int i2);

    public PorterDuffColorFilter(int color, PorterDuff.Mode mode) {
        this.mColor = color;
        this.mMode = mode;
    }

    public int getColor() {
        return this.mColor;
    }

    public void setColor(int color) {
        if (this.mColor != color) {
            this.mColor = color;
            discardNativeInstance();
        }
    }

    public PorterDuff.Mode getMode() {
        return this.mMode;
    }

    public void setMode(PorterDuff.Mode mode) {
        if (mode != null) {
            this.mMode = mode;
            discardNativeInstance();
            return;
        }
        throw new IllegalArgumentException("mode must be non-null");
    }

    /* access modifiers changed from: package-private */
    public long createNativeInstance() {
        return native_CreatePorterDuffFilter(this.mColor, this.mMode.nativeInt);
    }

    public boolean equals(Object object) {
        boolean z = true;
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        PorterDuffColorFilter other = (PorterDuffColorFilter) object;
        if (!(this.mColor == other.mColor && this.mMode.nativeInt == other.mMode.nativeInt)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (31 * this.mMode.hashCode()) + this.mColor;
    }
}
