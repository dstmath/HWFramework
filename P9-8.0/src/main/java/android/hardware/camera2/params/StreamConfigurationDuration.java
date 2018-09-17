package android.hardware.camera2.params;

import android.hardware.camera2.utils.HashCodeHelpers;
import android.util.Size;
import com.android.internal.util.Preconditions;

public final class StreamConfigurationDuration {
    private final long mDurationNs;
    private final int mFormat;
    private final int mHeight;
    private final int mWidth;

    public StreamConfigurationDuration(int format, int width, int height, long durationNs) {
        this.mFormat = StreamConfigurationMap.checkArgumentFormatInternal(format);
        this.mWidth = Preconditions.checkArgumentPositive(width, "width must be positive");
        this.mHeight = Preconditions.checkArgumentPositive(height, "height must be positive");
        this.mDurationNs = Preconditions.checkArgumentNonnegative(durationNs, "durationNs must be non-negative");
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

    public long getDuration() {
        return this.mDurationNs;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StreamConfigurationDuration)) {
            return false;
        }
        StreamConfigurationDuration other = (StreamConfigurationDuration) obj;
        if (this.mFormat != other.mFormat || this.mWidth != other.mWidth || this.mHeight != other.mHeight) {
            z = false;
        } else if (this.mDurationNs != other.mDurationNs) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return HashCodeHelpers.hashCode(this.mFormat, this.mWidth, this.mHeight, (int) this.mDurationNs, (int) (this.mDurationNs >>> 32));
    }
}
