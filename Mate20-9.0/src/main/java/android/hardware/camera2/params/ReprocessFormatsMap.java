package android.hardware.camera2.params;

import android.hardware.camera2.utils.HashCodeHelpers;
import com.android.internal.util.Preconditions;
import java.util.Arrays;

public final class ReprocessFormatsMap {
    private final int[] mEntry;
    private final int mInputCount;

    public ReprocessFormatsMap(int[] entry) {
        Preconditions.checkNotNull(entry, "entry must not be null");
        int left = entry.length;
        int numInputs = 0;
        int i = 0;
        while (i < entry.length) {
            int inputFormat = StreamConfigurationMap.checkArgumentFormatInternal(entry[i]);
            int left2 = left - 1;
            int i2 = i + 1;
            if (left2 >= 1) {
                int length = entry[i2];
                left = left2 - 1;
                i = i2 + 1;
                for (int j = 0; j < length; j++) {
                    StreamConfigurationMap.checkArgumentFormatInternal(entry[i + j]);
                }
                if (length > 0) {
                    if (left >= length) {
                        i += length;
                        left -= length;
                    } else {
                        throw new IllegalArgumentException(String.format("Input %x had too few output formats listed (actual: %d, expected: %d)", new Object[]{Integer.valueOf(inputFormat), Integer.valueOf(left), Integer.valueOf(length)}));
                    }
                }
                numInputs++;
            } else {
                throw new IllegalArgumentException(String.format("Input %x had no output format length listed", new Object[]{Integer.valueOf(inputFormat)}));
            }
        }
        this.mEntry = entry;
        this.mInputCount = numInputs;
    }

    public int[] getInputs() {
        int[] inputs = new int[this.mInputCount];
        int i = 0;
        int left = this.mEntry.length;
        int j = 0;
        while (i < this.mEntry.length) {
            int format = this.mEntry[i];
            int left2 = left - 1;
            int i2 = i + 1;
            if (left2 >= 1) {
                int length = this.mEntry[i2];
                left = left2 - 1;
                i = i2 + 1;
                if (length > 0) {
                    if (left >= length) {
                        i += length;
                        left -= length;
                    } else {
                        throw new AssertionError(String.format("Input %x had too few output formats listed (actual: %d, expected: %d)", new Object[]{Integer.valueOf(format), Integer.valueOf(left), Integer.valueOf(length)}));
                    }
                }
                inputs[j] = format;
                j++;
            } else {
                throw new AssertionError(String.format("Input %x had no output format length listed", new Object[]{Integer.valueOf(format)}));
            }
        }
        return StreamConfigurationMap.imageFormatToPublic(inputs);
    }

    public int[] getOutputs(int format) {
        int left = this.mEntry.length;
        int i = 0;
        while (i < this.mEntry.length) {
            int inputFormat = this.mEntry[i];
            int left2 = left - 1;
            int i2 = i + 1;
            if (left2 >= 1) {
                int length = this.mEntry[i2];
                int left3 = left2 - 1;
                int i3 = i2 + 1;
                if (length > 0 && left3 < length) {
                    throw new AssertionError(String.format("Input %x had too few output formats listed (actual: %d, expected: %d)", new Object[]{Integer.valueOf(format), Integer.valueOf(left3), Integer.valueOf(length)}));
                } else if (inputFormat == format) {
                    int[] outputs = new int[length];
                    for (int k = 0; k < length; k++) {
                        outputs[k] = this.mEntry[i3 + k];
                    }
                    return StreamConfigurationMap.imageFormatToPublic(outputs);
                } else {
                    i = i3 + length;
                    left = left3 - length;
                }
            } else {
                throw new AssertionError(String.format("Input %x had no output format length listed", new Object[]{Integer.valueOf(format)}));
            }
        }
        throw new IllegalArgumentException(String.format("Input format %x was not one in #getInputs", new Object[]{Integer.valueOf(format)}));
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof ReprocessFormatsMap) {
            return Arrays.equals(this.mEntry, ((ReprocessFormatsMap) obj).mEntry);
        }
        return false;
    }

    public int hashCode() {
        return HashCodeHelpers.hashCode(this.mEntry);
    }
}
