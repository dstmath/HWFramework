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
        if (elements.length == getGainFactorCount()) {
            Preconditions.checkArrayElementsInRange(elements, 1.0f, Float.MAX_VALUE, "elements");
            return;
        }
        throw new IllegalArgumentException("elements must be " + getGainFactorCount() + " length, received " + elements.length);
    }

    public int getRowCount() {
        return this.mRows;
    }

    public int getColumnCount() {
        return this.mColumns;
    }

    public int getGainFactorCount() {
        return this.mRows * this.mColumns * 4;
    }

    public float getGainFactor(int colorChannel, int column, int row) {
        int i;
        if (colorChannel < 0 || colorChannel > 4) {
            throw new IllegalArgumentException("colorChannel out of range");
        } else if (column < 0 || column >= (i = this.mColumns)) {
            throw new IllegalArgumentException("column out of range");
        } else if (row >= 0 && row < this.mRows) {
            return this.mElements[(((i * row) + column) * 4) + colorChannel];
        } else {
            throw new IllegalArgumentException("row out of range");
        }
    }

    public RggbChannelVector getGainFactorVector(int column, int row) {
        int i;
        if (column < 0 || column >= (i = this.mColumns)) {
            throw new IllegalArgumentException("column out of range");
        } else if (row < 0 || row >= this.mRows) {
            throw new IllegalArgumentException("row out of range");
        } else {
            int offset = ((i * row) + column) * 4;
            float[] fArr = this.mElements;
            return new RggbChannelVector(fArr[offset + 0], fArr[offset + 1], fArr[offset + 2], fArr[offset + 3]);
        }
    }

    public void copyGainFactors(float[] destination, int offset) {
        Preconditions.checkArgumentNonnegative(offset, "offset must not be negative");
        Preconditions.checkNotNull(destination, "destination must not be null");
        if (destination.length + offset >= getGainFactorCount()) {
            System.arraycopy(this.mElements, 0, destination, offset, getGainFactorCount());
            return;
        }
        throw new ArrayIndexOutOfBoundsException("destination too small to fit elements");
    }

    public boolean equals(Object obj) {
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
        if (this.mRows == other.mRows && this.mColumns == other.mColumns && Arrays.equals(this.mElements, other.mElements)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return HashCodeHelpers.hashCode(this.mRows, this.mColumns, HashCodeHelpers.hashCode(this.mElements));
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("LensShadingMap{");
        String[] channelPrefix = {"R:(", "G_even:(", "G_odd:(", "B:("};
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
