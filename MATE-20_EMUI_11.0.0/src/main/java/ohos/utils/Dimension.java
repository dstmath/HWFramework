package ohos.utils;

import java.util.Objects;

public final class Dimension {
    private final int heightSize;
    private final int widthSize;

    public Dimension(int i, int i2) {
        this.widthSize = i;
        this.heightSize = i2;
    }

    public int getWidthSize() {
        return this.widthSize;
    }

    public int getHeightSize() {
        return this.heightSize;
    }

    public static Dimension parseDimension(String str) throws NumberFormatException {
        Objects.requireNonNull(str, "Argument dimension must not be null");
        int indexOf = str.indexOf("*");
        if (indexOf >= 0) {
            try {
                return new Dimension(Integer.parseInt(str.substring(0, indexOf)), Integer.parseInt(str.substring(indexOf + 1)));
            } catch (NumberFormatException unused) {
                throw new NumberFormatException("Parse dimension failed: " + str);
            }
        } else {
            throw new NumberFormatException("Input invalid dimension: " + str);
        }
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.widthSize), Integer.valueOf(this.heightSize));
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Dimension)) {
            return false;
        }
        Dimension dimension = (Dimension) obj;
        if (dimension.widthSize == this.widthSize && dimension.heightSize == this.heightSize) {
            return true;
        }
        return false;
    }

    public String toString() {
        return this.widthSize + "*" + this.heightSize;
    }
}
