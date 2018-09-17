package android.hardware.camera2.params;

import android.hardware.camera2.utils.HashCodeHelpers;
import com.android.internal.util.Preconditions;
import java.util.Arrays;

public final class LensShadingMap {
    public static final float MINIMUM_GAIN_FACTOR = 1.0f;
    private final int mColumns;
    private final float[] mElements;
    private final int mRows;

    public LensShadingMap(float[] elements, int rows, int columns) {
        this.mRows = Preconditions.checkArgumentPositive(rows, "rows must be positive");
        this.mColumns = Preconditions.checkArgumentPositive(columns, "columns must be positive");
        this.mElements = (float[]) Preconditions.checkNotNull(elements, "elements must not be null");
        if (elements.length != getGainFactorCount()) {
            throw new IllegalArgumentException("elements must be " + getGainFactorCount() + " length, received " + elements.length);
        }
        Preconditions.checkArrayElementsInRange(elements, 1.0f, Float.MAX_VALUE, "elements");
    }

    public int getRowCount() {
        return this.mRows;
    }

    public int getColumnCount() {
        return this.mColumns;
    }

    public int getGainFactorCount() {
        return (this.mRows * this.mColumns) * 4;
    }

    public float getGainFactor(int colorChannel, int column, int row) {
        if (colorChannel < 0 || colorChannel > 4) {
            throw new IllegalArgumentException("colorChannel out of range");
        } else if (column < 0 || column >= this.mColumns) {
            throw new IllegalArgumentException("column out of range");
        } else if (row >= 0 && row < this.mRows) {
            return this.mElements[(((this.mColumns * row) + column) * 4) + colorChannel];
        } else {
            throw new IllegalArgumentException("row out of range");
        }
    }

    public RggbChannelVector getGainFactorVector(int column, int row) {
        if (column < 0 || column >= this.mColumns) {
            throw new IllegalArgumentException("column out of range");
        } else if (row < 0 || row >= this.mRows) {
            throw new IllegalArgumentException("row out of range");
        } else {
            int offset = ((this.mColumns * row) + column) * 4;
            return new RggbChannelVector(this.mElements[offset + 0], this.mElements[offset + 1], this.mElements[offset + 2], this.mElements[offset + 3]);
        }
    }

    public void copyGainFactors(float[] destination, int offset) {
        Preconditions.checkArgumentNonnegative(offset, "offset must not be negative");
        Preconditions.checkNotNull(destination, "destination must not be null");
        if (destination.length + offset < getGainFactorCount()) {
            throw new ArrayIndexOutOfBoundsException("destination too small to fit elements");
        }
        System.arraycopy(this.mElements, 0, destination, offset, getGainFactorCount());
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LensShadingMap)) {
            return false;
        }
        LensShadingMap other = (LensShadingMap) obj;
        if (this.mRows == other.mRows && this.mColumns == other.mColumns) {
            z = Arrays.equals(this.mElements, other.mElements);
        }
        return z;
    }

    public int hashCode() {
        int elemsHash = HashCodeHelpers.hashCode(this.mElements);
        return HashCodeHelpers.hashCode(this.mRows, this.mColumns, elemsHash);
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("LensShadingMap{");
        String[] channelPrefix = new String[]{"R:(", "G_even:(", "G_odd:(", "B:("};
        for (int ch = 0; ch < 4; ch++) {
            str.append(channelPrefix[ch]);
            for (int r = 0; r < this.mRows; r++) {
                str.append("[");
                for (int c = 0; c < this.mColumns; c++) {
                    str.append(getGainFactor(ch, c, r));
                    if (c < this.mColumns - 1) {
                        str.append(", ");
                    }
                }
                str.append("]");
                if (r < this.mRows - 1) {
                    str.append(", ");
                }
            }
            str.append(")");
            if (ch < 3) {
                str.append(", ");
            }
        }
        str.append("}");
        return str.toString();
    }
}
