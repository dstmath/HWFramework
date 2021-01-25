package ohos.system;

import java.util.Objects;

public class TimeSpecGroup implements Comparable<TimeSpecGroup> {
    private static final int MAXIMUM_NANO_SEC_TIME = 999999999;
    private static final int MINIMUM_NANO_SEC_TIME = 0;
    public final long nanoSecTime;
    public final long secTime;

    public TimeSpecGroup(long j, long j2) {
        this.secTime = j;
        this.nanoSecTime = j2;
        if (j2 < 0 || j2 > 999999999) {
            throw new IllegalArgumentException("nanoSecTime's value must be >= 0 and <= 999999999");
        }
    }

    public int compareTo(TimeSpecGroup timeSpecGroup) {
        long j = this.secTime;
        long j2 = timeSpecGroup.secTime;
        if (j > j2) {
            return 1;
        }
        if (j < j2) {
            return -1;
        }
        long j3 = this.nanoSecTime;
        long j4 = timeSpecGroup.nanoSecTime;
        if (j3 > j4) {
            return 1;
        }
        if (j3 < j4) {
            return -1;
        }
        return 0;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TimeSpecGroup timeSpecGroup = (TimeSpecGroup) obj;
        return this.secTime == timeSpecGroup.secTime && this.nanoSecTime == timeSpecGroup.nanoSecTime;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Objects.hash(Long.valueOf(this.secTime), Long.valueOf(this.nanoSecTime));
    }

    @Override // java.lang.Object
    public String toString() {
        return libcore.util.Objects.toString(this);
    }
}
