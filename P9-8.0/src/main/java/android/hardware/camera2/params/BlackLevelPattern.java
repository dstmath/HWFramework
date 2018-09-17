package android.hardware.camera2.params;

import com.android.internal.util.Preconditions;
import java.util.Arrays;

public final class BlackLevelPattern {
    public static final int COUNT = 4;
    private final int[] mCfaOffsets;

    public BlackLevelPattern(int[] offsets) {
        if (offsets == null) {
            throw new NullPointerException("Null offsets array passed to constructor");
        } else if (offsets.length < 4) {
            throw new IllegalArgumentException("Invalid offsets array length");
        } else {
            this.mCfaOffsets = Arrays.copyOf(offsets, 4);
        }
    }

    public int getOffsetForIndex(int column, int row) {
        if (row >= 0 && column >= 0) {
            return this.mCfaOffsets[((row & 1) << 1) | (column & 1)];
        }
        throw new IllegalArgumentException("column, row arguments must be positive");
    }

    public void copyTo(int[] destination, int offset) {
        Preconditions.checkNotNull(destination, "destination must not be null");
        if (offset < 0) {
            throw new IllegalArgumentException("Null offset passed to copyTo");
        } else if (destination.length - offset < 4) {
            throw new ArrayIndexOutOfBoundsException("destination too small to fit elements");
        } else {
            for (int i = 0; i < 4; i++) {
                destination[offset + i] = this.mCfaOffsets[i];
            }
        }
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof BlackLevelPattern) {
            return Arrays.equals(((BlackLevelPattern) obj).mCfaOffsets, this.mCfaOffsets);
        }
        return false;
    }

    public int hashCode() {
        return Arrays.hashCode(this.mCfaOffsets);
    }

    public String toString() {
        return String.format("BlackLevelPattern([%d, %d], [%d, %d])", new Object[]{Integer.valueOf(this.mCfaOffsets[0]), Integer.valueOf(this.mCfaOffsets[1]), Integer.valueOf(this.mCfaOffsets[2]), Integer.valueOf(this.mCfaOffsets[3])});
    }
}
