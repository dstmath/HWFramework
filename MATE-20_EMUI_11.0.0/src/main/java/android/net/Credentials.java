package android.net;

public class Credentials {
    private final int gid;
    private final int pid;
    private final int uid;

    public Credentials(int pid2, int uid2, int gid2) {
        this.pid = pid2;
        this.uid = uid2;
        this.gid = gid2;
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
