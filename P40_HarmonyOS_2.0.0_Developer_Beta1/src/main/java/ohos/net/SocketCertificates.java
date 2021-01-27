package ohos.net;

public class SocketCertificates {
    private final int mGid;
    private final int mPid;
    private final int mUid;

    public SocketCertificates(int i, int i2, int i3) {
        this.mPid = i;
        this.mUid = i2;
        this.mGid = i3;
    }

    public int getPid() {
        return this.mPid;
    }

    public int getUid() {
        return this.mUid;
    }

    public int getGid() {
        return this.mGid;
    }
}
