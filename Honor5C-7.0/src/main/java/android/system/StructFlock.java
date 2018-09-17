package android.system;

import libcore.util.Objects;

public final class StructFlock {
    public long l_len;
    public int l_pid;
    public long l_start;
    public short l_type;
    public short l_whence;

    public String toString() {
        return Objects.toString(this);
    }
}
