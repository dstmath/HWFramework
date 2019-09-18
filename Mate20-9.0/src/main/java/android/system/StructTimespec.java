package android.system;

import libcore.util.Objects;

public final class StructTimespec implements Comparable<StructTimespec> {
    public final long tv_nsec;
    public final long tv_sec;

    public StructTimespec(long tv_sec2, long tv_nsec2) {
        this.tv_sec = tv_sec2;
        this.tv_nsec = tv_nsec2;
        if (tv_nsec2 < 0 || tv_nsec2 > 999999999) {
            throw new IllegalArgumentException("tv_nsec value " + tv_nsec2 + " is not in [0, 999999999]");
        }
    }

    public int compareTo(StructTimespec other) {
        if (this.tv_sec > other.tv_sec) {
            return 1;
        }
        if (this.tv_sec < other.tv_sec) {
            return -1;
        }
        if (this.tv_nsec > other.tv_nsec) {
            return 1;
        }
        if (this.tv_nsec < other.tv_nsec) {
            return -1;
        }
        return 0;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StructTimespec that = (StructTimespec) o;
        if (this.tv_sec != that.tv_sec) {
            return false;
        }
        if (this.tv_nsec != that.tv_nsec) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (31 * ((int) (this.tv_sec ^ (this.tv_sec >>> 32)))) + ((int) (this.tv_nsec ^ (this.tv_nsec >>> 32)));
    }

    public String toString() {
        return Objects.toString(this);
    }
}
