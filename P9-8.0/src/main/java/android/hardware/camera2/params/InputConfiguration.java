package android.hardware.camera2.params;

import android.hardware.camera2.utils.HashCodeHelpers;

public final class InputConfiguration {
    private final int mFormat;
    private final int mHeight;
    private final int mWidth;

    public InputConfiguration(int width, int height, int format) {
        this.mWidth = width;
        this.mHeight = height;
        this.mFormat = format;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public int getFormat() {
        return this.mFormat;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof InputConfiguration)) {
            return false;
        }
        InputConfiguration otherInputConfig = (InputConfiguration) obj;
        if (otherInputConfig.getWidth() == this.mWidth && otherInputConfig.getHeight() == this.mHeight && otherInputConfig.getFormat() == this.mFormat) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return HashCodeHelpers.hashCode(this.mWidth, this.mHeight, this.mFormat);
    }

    public String toString() {
        return String.format("InputConfiguration(w:%d, h:%d, format:%d)", new Object[]{Integer.valueOf(this.mWidth), Integer.valueOf(this.mHeight), Integer.valueOf(this.mFormat)});
    }
}
