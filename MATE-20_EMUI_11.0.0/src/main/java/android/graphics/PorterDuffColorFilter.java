package android.graphics;

import android.annotation.UnsupportedAppUsage;
import android.graphics.PorterDuff;

public class PorterDuffColorFilter extends ColorFilter {
    private int mColor;
    private PorterDuff.Mode mMode;

    private static native long native_CreateBlendModeFilter(int i, int i2);

    public PorterDuffColorFilter(int color, PorterDuff.Mode mode) {
        this.mColor = color;
        this.mMode = mode;
    }

    @UnsupportedAppUsage
    public int getColor() {
        return this.mColor;
    }

    @UnsupportedAppUsage
    public PorterDuff.Mode getMode() {
        return this.mMode;
    }

    /* access modifiers changed from: package-private */
    @Override // android.graphics.ColorFilter
    public long createNativeInstance() {
        return native_CreateBlendModeFilter(this.mColor, this.mMode.nativeInt);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        PorterDuffColorFilter other = (PorterDuffColorFilter) object;
        if (this.mColor == other.mColor && this.mMode.nativeInt == other.mMode.nativeInt) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (this.mMode.hashCode() * 31) + this.mColor;
    }
}
