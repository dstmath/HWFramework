package android.hardware.camera2.params;

import android.hardware.camera2.utils.HashCodeHelpers;
import com.android.internal.util.Preconditions;

public final class OisSample {
    private final long mTimestampNs;
    private final float mXShift;
    private final float mYShift;

    public OisSample(long timestamp, float xShift, float yShift) {
        this.mTimestampNs = timestamp;
        this.mXShift = Preconditions.checkArgumentFinite(xShift, "xShift must be finite");
        this.mYShift = Preconditions.checkArgumentFinite(yShift, "yShift must be finite");
    }

    public long getTimestamp() {
        return this.mTimestampNs;
    }

    public float getXshift() {
        return this.mXShift;
    }

    public float getYshift() {
        return this.mYShift;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OisSample)) {
            return false;
        }
        OisSample other = (OisSample) obj;
        if (this.mTimestampNs == other.mTimestampNs && this.mXShift == other.mXShift && this.mYShift == other.mYShift) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return HashCodeHelpers.hashCode(this.mXShift, this.mYShift, (float) HashCodeHelpers.hashCode((float) this.mTimestampNs));
    }

    public String toString() {
        return String.format("OisSample{timestamp:%d, shift_x:%f, shift_y:%f}", new Object[]{Long.valueOf(this.mTimestampNs), Float.valueOf(this.mXShift), Float.valueOf(this.mYShift)});
    }
}
