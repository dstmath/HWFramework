package android.system;

import libcore.util.Objects;

public final class StructUcred {
    public final int gid;
    public final int pid;
    public final int uid;

    public StructUcred(int pid, int uid, int gid) {
        this.pid = pid;
        this.uid = uid;
        this.gid = gid;
    }

    public String toString() {
        return Objects.toString(this);
    }
}
