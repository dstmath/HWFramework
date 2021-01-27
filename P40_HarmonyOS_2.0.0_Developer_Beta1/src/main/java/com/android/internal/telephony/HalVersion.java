package com.android.internal.telephony;

import java.util.Objects;

public class HalVersion implements Comparable<HalVersion> {
    public static final HalVersion UNKNOWN = new HalVersion(-1, -1);
    public final int major;
    public final int minor;

    public HalVersion(int major2, int minor2) {
        this.major = major2;
        this.minor = minor2;
    }

    public int compareTo(HalVersion ver) {
        int i;
        int i2;
        if (ver == null || (i = this.major) > (i2 = ver.major)) {
            return 1;
        }
        if (i < i2) {
            return -1;
        }
        int i3 = this.minor;
        int i4 = ver.minor;
        if (i3 > i4) {
            return 1;
        }
        if (i3 < i4) {
            return -1;
        }
        return 0;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.major), Integer.valueOf(this.minor));
    }

    @Override // java.lang.Object
    public boolean equals(Object o) {
        return (o instanceof HalVersion) && (o == this || compareTo((HalVersion) o) == 0);
    }

    public boolean greater(HalVersion ver) {
        return compareTo(ver) > 0;
    }

    public boolean less(HalVersion ver) {
        return compareTo(ver) < 0;
    }

    public boolean greaterOrEqual(HalVersion ver) {
        return greater(ver) || equals(ver);
    }

    public boolean lessOrEqual(HalVersion ver) {
        return less(ver) || equals(ver);
    }

    @Override // java.lang.Object
    public String toString() {
        return this.major + "." + this.minor;
    }
}
