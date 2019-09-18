package sun.nio.ch;

public abstract class AbstractPollArrayWrapper {
    static final short EVENT_OFFSET = 4;
    static final short FD_OFFSET = 0;
    static final short REVENT_OFFSET = 6;
    static final short SIZE_POLLFD = 8;
    protected AllocatedNativeObject pollArray;
    protected long pollArrayAddress;
    protected int totalChannels = 0;

    /* access modifiers changed from: package-private */
    public int getEventOps(int i) {
        return this.pollArray.getShort((8 * i) + 4);
    }

    /* access modifiers changed from: package-private */
    public int getReventOps(int i) {
        return this.pollArray.getShort((8 * i) + 6);
    }

    /* access modifiers changed from: package-private */
    public int getDescriptor(int i) {
        return this.pollArray.getInt((8 * i) + 0);
    }

    /* access modifiers changed from: package-private */
    public void putEventOps(int i, int event) {
        this.pollArray.putShort((8 * i) + 4, (short) event);
    }

    /* access modifiers changed from: package-private */
    public void putReventOps(int i, int revent) {
        this.pollArray.putShort((8 * i) + 6, (short) revent);
    }

    /* access modifiers changed from: package-private */
    public void putDescriptor(int i, int fd) {
        this.pollArray.putInt((8 * i) + 0, fd);
    }
}
