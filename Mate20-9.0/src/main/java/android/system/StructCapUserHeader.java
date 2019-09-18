package android.system;

import libcore.util.Objects;

public final class StructCapUserHeader {
    public final int pid;
    public int version;

    public StructCapUserHeader(int version2, int pid2) {
        this.version = version2;
        this.pid = pid2;
    }

    public String toString() {
        return Objects.toString(this);
    }
}
