package android.hardware.camera2.params;

import android.hardware.camera2.utils.HashCodeHelpers;
import android.util.Rational;
import com.android.internal.util.Preconditions;
import java.util.Arrays;

public final class ColorSpaceTransform {
    private static final int COLUMNS = 3;
    private static final int COUNT = 9;
    private static final int COUNT_INT = 18;
    private static final int OFFSET_DENOMINATOR = 1;
    private static final int OFFSET_NUMERATOR = 0;
    private static final int RATIONAL_SIZE = 2;
    private static final int ROWS = 3;
    private final int[] mElements;

    public ColorSpaceTransform(Rational[] elements) {
        Preconditions.checkNotNull(elements, "elements must not be null");
        if (elements.length != 9) {
            throw new IllegalArgumentException("elements must be 9 length");
        }
        this.mElements = new int[18];
        for (int i = 0; i < elements.length; i++) {
            Preconditions.checkNotNull(elements, "element[" + i + "] must not be null");
            this.mElements[(i * 2) + 0] = elements[i].getNumerator();
            this.mElements[(i * 2) + 1] = elements[i].getDenominator();
        }
    }

    public ColorSpaceTransform(int[] elements) {
        Preconditions.checkNotNull(elements, "elements must not be null");
        if (elements.length != 18) {
            throw new IllegalArgumentException("elements must be 18 length");
        }
        for (int i = 0; i < elements.length; i++) {
            Preconditions.checkNotNull(elements, "element " + i + " must not be null");
        }
        this.mElements = Arrays.copyOf(elements, elements.length);
    }

    public Rational getElement(int column, int row) {
        if (column < 0 || column >= 3) {
            throw new IllegalArgumentException("column out of range");
        } else if (row >= 0 && row < 3) {
            return new Rational(this.mElements[(((row * 3) + column) * 2) + 0], this.mElements[(((row * 3) + column) * 2) + 1]);
        } else {
            throw new IllegalArgumentException("row out of range");
        }
    }

    public void copyElements(Rational[] destination, int offset) {
        Preconditions.checkArgumentNonnegative(offset, "offset must not be negative");
        Preconditions.checkNotNull(destination, "destination must not be null");
        if (destination.length - offset < 9) {
            throw new ArrayIndexOutOfBoundsException("destination too small to fit elements");
        }
        int i = 0;
        int j = 0;
        while (i < 9) {
            destination[i + offset] = new Rational(this.mElements[j + 0], this.mElements[j + 1]);
            i++;
            j += 2;
        }
    }

    public void copyElements(int[] destination, int offset) {
        Preconditions.checkArgumentNonnegative(offset, "offset must not be negative");
        Preconditions.checkNotNull(destination, "destination must not be null");
        if (destination.length - offset < 18) {
            throw new ArrayIndexOutOfBoundsException("destination too small to fit elements");
        }
        for (int i = 0; i < 18; i++) {
            destination[i + offset] = this.mElements[i];
        }
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ColorSpaceTransform)) {
            return false;
        }
        ColorSpaceTransform other = (ColorSpaceTransform) obj;
        int i = 0;
        int j = 0;
        while (i < 9) {
            if (!new Rational(this.mElements[j + 0], this.mElements[j + 1]).equals(new Rational(other.mElements[j + 0], other.mElements[j + 1]))) {
                return false;
            }
            i++;
            j += 2;
        }
        return true;
    }

    public int hashCode() {
        return HashCodeHelpers.hashCode(this.mElements);
    }

    public String toString() {
        return String.format("ColorSpaceTransform%s", new Object[]{toShortString()});
    }

    private String toShortString() {
        StringBuilder sb = new StringBuilder("(");
        int i = 0;
        for (int row = 0; row < 3; row++) {
            sb.append("[");
            int col = 0;
            while (col < 3) {
                int numerator = this.mElements[i + 0];
                int denominator = this.mElements[i + 1];
                sb.append(numerator);
                sb.append("/");
                sb.append(denominator);
                if (col < 2) {
                    sb.append(", ");
                }
                col++;
                i += 2;
            }
            sb.append("]");
            if (row < 2) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }
}
