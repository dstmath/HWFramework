package sun.nio.ch;

abstract class AbstractPollArrayWrapper {
    static final short EVENT_OFFSET = (short) 4;
    static final short FD_OFFSET = (short) 0;
    static final short POLLERR = (short) 8;
    static final short POLLHUP = (short) 16;
    static final short POLLIN = (short) 1;
    static final short POLLNVAL = (short) 32;
    static final short POLLOUT = (short) 4;
    static final short POLLREMOVE = (short) 2048;
    static final short REVENT_OFFSET = (short) 6;
    static final short SIZE_POLLFD = (short) 8;
    protected AllocatedNativeObject pollArray;
    protected long pollArrayAddress;
    protected int totalChannels;

    AbstractPollArrayWrapper() {
        this.totalChannels = 0;
    }

    int getEventOps(int i) {
        return this.pollArray.getShort((i * 8) + 4);
    }

    int getReventOps(int i) {
        return this.pollArray.getShort((i * 8) + 6);
    }

    int getDescriptor(int i) {
        return this.pollArray.getInt((i * 8) + 0);
    }

    void putEventOps(int i, int event) {
        this.pollArray.putShort((i * 8) + 4, (short) event);
    }

    void putReventOps(int i, int revent) {
        this.pollArray.putShort((i * 8) + 6, (short) revent);
    }

    void putDescriptor(int i, int fd) {
        this.pollArray.putInt((i * 8) + 0, fd);
    }
}
