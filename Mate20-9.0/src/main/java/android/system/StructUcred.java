package android.system;

import libcore.util.Objects;

public final class StructUcred {
    public final int gid;
    public final int pid;
    public final int uid;

    public StructUcred(int pid2, int uid2, int gid2) {
        this.pid = pid2;
        this.uid = uid2;
        this.gid = gid2;
    }

    public String toString() {
        return Objects.toString(this);
    }
}
