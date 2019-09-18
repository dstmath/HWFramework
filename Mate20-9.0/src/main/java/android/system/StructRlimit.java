package android.system;

import libcore.util.Objects;

public final class StructRlimit {
    public final long rlim_cur;
    public final long rlim_max;

    public StructRlimit(long rlim_cur2, long rlim_max2) {
        this.rlim_cur = rlim_cur2;
        this.rlim_max = rlim_max2;
    }

    public String toString() {
        return Objects.toString(this);
    }
}
