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
        if (elements.length != COUNT) {
            throw new IllegalArgumentException("elements must be 9 length");
        }
        this.mElements = new int[COUNT_INT];
        for (int i = OFFSET_NUMERATOR; i < elements.length; i += OFFSET_DENOMINATOR) {
            Preconditions.checkNotNull(elements, "element[" + i + "] must not be null");
            this.mElements[(i * RATIONAL_SIZE) + OFFSET_NUMERATOR] = elements[i].getNumerator();
            this.mElements[(i * RATIONAL_SIZE) + OFFSET_DENOMINATOR] = elements[i].getDenominator();
        }
    }

    public ColorSpaceTransform(int[] elements) {
        Preconditions.checkNotNull(elements, "elements must not be null");
        if (elements.length != COUNT_INT) {
            throw new IllegalArgumentException("elements must be 18 length");
        }
        for (int i = OFFSET_NUMERATOR; i < elements.length; i += OFFSET_DENOMINATOR) {
            Preconditions.checkNotNull(elements, "element " + i + " must not be null");
        }
        this.mElements = Arrays.copyOf(elements, elements.length);
    }

    public Rational getElement(int column, int row) {
        if (column < 0 || column >= ROWS) {
            throw new IllegalArgumentException("column out of range");
        } else if (row >= 0 && row < ROWS) {
            return new Rational(this.mElements[(((row * ROWS) + column) * RATIONAL_SIZE) + OFFSET_NUMERATOR], this.mElements[(((row * ROWS) + column) * RATIONAL_SIZE) + OFFSET_DENOMINATOR]);
        } else {
            throw new IllegalArgumentException("row out of range");
        }
    }

    public void copyElements(Rational[] destination, int offset) {
        Preconditions.checkArgumentNonnegative(offset, "offset must not be negative");
        Preconditions.checkNotNull(destination, "destination must not be null");
        if (destination.length - offset < COUNT) {
            throw new ArrayIndexOutOfBoundsException("destination too small to fit elements");
        }
        int i = OFFSET_NUMERATOR;
        int j = OFFSET_NUMERATOR;
        while (i < COUNT) {
            destination[i + offset] = new Rational(this.mElements[j + OFFSET_NUMERATOR], this.mElements[j + OFFSET_DENOMINATOR]);
            i += OFFSET_DENOMINATOR;
            j += RATIONAL_SIZE;
        }
    }

    public void copyElements(int[] destination, int offset) {
        Preconditions.checkArgumentNonnegative(offset, "offset must not be negative");
        Preconditions.checkNotNull(destination, "destination must not be null");
        if (destination.length - offset < COUNT_INT) {
            throw new ArrayIndexOutOfBoundsException("destination too small to fit elements");
        }
        for (int i = OFFSET_NUMERATOR; i < COUNT_INT; i += OFFSET_DENOMINATOR) {
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
        int i = OFFSET_NUMERATOR;
        int j = OFFSET_NUMERATOR;
        while (i < COUNT) {
            if (!new Rational(this.mElements[j + OFFSET_NUMERATOR], this.mElements[j + OFFSET_DENOMINATOR]).equals(new Rational(other.mElements[j + OFFSET_NUMERATOR], other.mElements[j + OFFSET_DENOMINATOR]))) {
                return false;
            }
            i += OFFSET_DENOMINATOR;
            j += RATIONAL_SIZE;
        }
        return true;
    }

    public int hashCode() {
        return HashCodeHelpers.hashCode(this.mElements);
    }

    public String toString() {
        Object[] objArr = new Object[OFFSET_DENOMINATOR];
        objArr[OFFSET_NUMERATOR] = toShortString();
        return String.format("ColorSpaceTransform%s", objArr);
    }

    private String toShortString() {
        StringBuilder sb = new StringBuilder("(");
        int i = OFFSET_NUMERATOR;
        for (int row = OFFSET_NUMERATOR; row < ROWS; row += OFFSET_DENOMINATOR) {
            sb.append("[");
            int col = OFFSET_NUMERATOR;
            while (col < ROWS) {
                int numerator = this.mElements[i + OFFSET_NUMERATOR];
                int denominator = this.mElements[i + OFFSET_DENOMINATOR];
                sb.append(numerator);
                sb.append("/");
                sb.append(denominator);
                if (col < RATIONAL_SIZE) {
                    sb.append(", ");
                }
                col += OFFSET_DENOMINATOR;
                i += RATIONAL_SIZE;
            }
            sb.append("]");
            if (row < RATIONAL_SIZE) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }
}
