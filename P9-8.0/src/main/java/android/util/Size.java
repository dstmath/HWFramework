package android.util;

import com.android.internal.util.Preconditions;

public final class Size {
    private final int mHeight;
    private final int mWidth;

    public Size(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Size)) {
            return false;
        }
        Size other = (Size) obj;
        if (!(this.mWidth == other.mWidth && this.mHeight == other.mHeight)) {
            z = false;
        }
        return z;
    }

    public String toString() {
        return this.mWidth + "x" + this.mHeight;
    }

    private static NumberFormatException invalidSize(String s) {
        throw new NumberFormatException("Invalid Size: \"" + s + "\"");
    }

    public static Size parseSize(String string) throws NumberFormatException {
        Preconditions.checkNotNull(string, "string must not be null");
        int sep_ix = string.indexOf(42);
        if (sep_ix < 0) {
            sep_ix = string.indexOf(120);
        }
        if (sep_ix < 0) {
            throw invalidSize(string);
        }
        try {
            return new Size(Integer.parseInt(string.substring(0, sep_ix)), Integer.parseInt(string.substring(sep_ix + 1)));
        } catch (NumberFormatException e) {
            throw invalidSize(string);
        }
    }

    public int hashCode() {
        return this.mHeight ^ ((this.mWidth << 16) | (this.mWidth >>> 16));
    }
}
