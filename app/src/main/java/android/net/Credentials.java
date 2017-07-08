package android.net;

public class Credentials {
    private final int gid;
    private final int pid;
    private final int uid;

    public Credentials(int pid, int uid, int gid) {
        this.pid = pid;
        this.uid = uid;
        this.gid = gid;
    }

    public int getPid() {
        return this.pid;
    }

    public int getUid() {
        return this.uid;
    }

    public int getGid() {
        return this.gid;
    }
}
