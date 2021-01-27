package ohos.utils;

import java.util.Objects;

public final class FloatDimension {
    private static final float EQUAL_SIZE = 1.0E-6f;
    private final float heightSize;
    private final float widthSize;

    public FloatDimension(float f, float f2) {
        this.widthSize = f;
        this.heightSize = f2;
    }

    public float getWidthSize() {
        return this.widthSize;
    }

    public float getHeightSize() {
        return this.heightSize;
    }

    public static FloatDimension parseFloatDimension(String str) throws NumberFormatException {
        Objects.requireNonNull(str, "Argument floatDimension must not be null");
        int indexOf = str.indexOf("*");
        if (indexOf >= 0) {
            try {
                return new FloatDimension(Float.parseFloat(str.substring(0, indexOf)), Float.parseFloat(str.substring(indexOf + 1)));
            } catch (NumberFormatException unused) {
                throw new NumberFormatException("Parse floatDimension failed: " + str);
            }
        } else {
            throw new NumberFormatException("Input invalid floatDimension: " + str);
        }
    }

    public int hashCode() {
        return Objects.hash(Float.valueOf(this.widthSize), Float.valueOf(this.heightSize));
    }

    public boolean equals(Object obj) {
        if (Double.isNaN((double) this.widthSize) || Double.isNaN((double) this.heightSize) || Double.isInfinite((double) this.widthSize) || Double.isInfinite((double) this.heightSize) || obj == null || !(obj instanceof FloatDimension)) {
            return false;
        }
        FloatDimension floatDimension = (FloatDimension) obj;
        if (Float.compare(floatDimension.widthSize, this.widthSize) == 0 && Float.compare(floatDimension.heightSize, this.heightSize) == 0) {
            return true;
        }
        return false;
    }

    public String toString() {
        return this.widthSize + "*" + this.heightSize;
    }
}
