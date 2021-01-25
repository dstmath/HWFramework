package ohos.system;

import android.system.StructTimeval;
import java.util.Objects;

public final class TimeValGroup {
    private static final int MAXIMUM_USEC_TIME = 999999;
    private static final int MINIMUM_USEC_TIME = 0;
    public final long secTime;
    public final long usecTime;

    private TimeValGroup(long j, long j2) {
        this.secTime = j;
        this.usecTime = j2;
        if (j2 < 0 || j2 > 999999) {
            throw new IllegalArgumentException("usecTime's value must be >=0 and <=999999");
        }
    }

    public static TimeValGroup fromMillis(long j) {
        StructTimeval fromMillis = StructTimeval.fromMillis(j);
        return new TimeValGroup(fromMillis.tv_sec, fromMillis.tv_usec);
    }

    public long toMillis() {
        return (this.usecTime / 1000) + (this.secTime * 1000);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TimeValGroup timeValGroup = (TimeValGroup) obj;
        return this.secTime == timeValGroup.secTime && this.usecTime == timeValGroup.usecTime;
    }

    public int hashCode() {
        return Objects.hash(Long.valueOf(this.secTime), Long.valueOf(this.usecTime));
    }
}
