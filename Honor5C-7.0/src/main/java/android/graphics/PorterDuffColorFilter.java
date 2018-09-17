package android.graphics;

import android.graphics.PorterDuff.Mode;

public class PorterDuffColorFilter extends ColorFilter {
    private int mColor;
    private Mode mMode;

    private static native long native_CreatePorterDuffFilter(int i, int i2);

    public PorterDuffColorFilter(int color, Mode mode) {
        this.mColor = color;
        this.mMode = mode;
        update();
    }

    public int getColor() {
        return this.mColor;
    }

    public void setColor(int color) {
        this.mColor = color;
        update();
    }

    public Mode getMode() {
        return this.mMode;
    }

    public void setMode(Mode mode) {
        this.mMode = mode;
        update();
    }

    private void update() {
        ColorFilter.destroyFilter(this.native_instance);
        this.native_instance = native_CreatePorterDuffFilter(this.mColor, this.mMode.nativeInt);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        PorterDuffColorFilter other = (PorterDuffColorFilter) object;
        return this.mColor == other.mColor && this.mMode == other.mMode;
    }

    public int hashCode() {
        return (this.mMode.hashCode() * 31) + this.mColor;
    }
}
