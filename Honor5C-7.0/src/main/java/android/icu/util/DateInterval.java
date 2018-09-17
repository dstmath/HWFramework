package android.icu.util;

import java.io.Serializable;

public final class DateInterval implements Serializable {
    private static final long serialVersionUID = 1;
    private final long fromDate;
    private final long toDate;

    public DateInterval(long from, long to) {
        this.fromDate = from;
        this.toDate = to;
    }

    public long getFromDate() {
        return this.fromDate;
    }

    public long getToDate() {
        return this.toDate;
    }

    public boolean equals(Object a) {
        boolean z = false;
        if (!(a instanceof DateInterval)) {
            return false;
        }
        DateInterval di = (DateInterval) a;
        if (this.fromDate == di.fromDate && this.toDate == di.toDate) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return (int) (this.fromDate + this.toDate);
    }

    public String toString() {
        return String.valueOf(this.fromDate) + " " + String.valueOf(this.toDate);
    }
}
