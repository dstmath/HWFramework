package android.graphics;

import android.graphics.PorterDuff.Mode;

public class PorterDuffColorFilter extends ColorFilter {
    private int mColor;
    private Mode mMode;

    private static native long native_CreatePorterDuffFilter(int i, int i2);

    public PorterDuffColorFilter(int color, Mode mode) {
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

    public Mode getMode() {
        return this.mMode;
    }

    public void setMode(Mode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("mode must be non-null");
        }
        this.mMode = mode;
        discardNativeInstance();
    }

    long createNativeInstance() {
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
        return (this.mMode.hashCode() * 31) + this.mColor;
    }
}
