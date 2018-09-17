package android.hardware.camera2.params;

import android.hardware.camera2.utils.HashCodeHelpers;
import android.util.Size;
import com.android.internal.util.Preconditions;

public final class StreamConfiguration {
    private final int mFormat;
    private final int mHeight;
    private final boolean mInput;
    private final int mWidth;

    public StreamConfiguration(int format, int width, int height, boolean input) {
        this.mFormat = StreamConfigurationMap.checkArgumentFormatInternal(format);
        this.mWidth = Preconditions.checkArgumentPositive(width, "width must be positive");
        this.mHeight = Preconditions.checkArgumentPositive(height, "height must be positive");
        this.mInput = input;
    }

    public final int getFormat() {
        return this.mFormat;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public Size getSize() {
        return new Size(this.mWidth, this.mHeight);
    }

    public boolean isInput() {
        return this.mInput;
    }

    public boolean isOutput() {
        return this.mInput ^ 1;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StreamConfiguration)) {
            return false;
        }
        StreamConfiguration other = (StreamConfiguration) obj;
        if (this.mFormat != other.mFormat || this.mWidth != other.mWidth || this.mHeight != other.mHeight) {
            z = false;
        } else if (this.mInput != other.mInput) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int i = 1;
        int[] iArr = new int[4];
        iArr[0] = this.mFormat;
        iArr[1] = this.mWidth;
        iArr[2] = this.mHeight;
        if (!this.mInput) {
            i = 0;
        }
        iArr[3] = i;
        return HashCodeHelpers.hashCode(iArr);
    }
}
