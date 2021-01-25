package android.graphics;

public final class BlendModeColorFilter extends ColorFilter {
    final int mColor;
    private final BlendMode mMode;

    private static native long native_CreateBlendModeFilter(int i, int i2);

    public BlendModeColorFilter(int color, BlendMode mode) {
        this.mColor = color;
        this.mMode = mode;
    }

    public int getColor() {
        return this.mColor;
    }

    public BlendMode getMode() {
        return this.mMode;
    }

    /* access modifiers changed from: package-private */
    @Override // android.graphics.ColorFilter
    public long createNativeInstance() {
        return native_CreateBlendModeFilter(this.mColor, this.mMode.getXfermode().porterDuffMode);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        if (((BlendModeColorFilter) object).mMode == this.mMode) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return (this.mMode.hashCode() * 31) + this.mColor;
    }
}
