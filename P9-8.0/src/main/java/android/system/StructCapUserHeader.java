package android.system;

import libcore.util.Objects;

public final class StructCapUserHeader {
    public final int pid;
    public int version;

    public StructCapUserHeader(int version, int pid) {
        this.version = version;
        this.pid = pid;
    }

    public String toString() {
        return Objects.toString(this);
    }
}
