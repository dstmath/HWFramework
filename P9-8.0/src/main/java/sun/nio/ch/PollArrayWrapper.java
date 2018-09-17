package sun.nio.ch;

public class PollArrayWrapper extends AbstractPollArrayWrapper {
    int interruptFD;

    private static native void interrupt(int i);

    private native int poll0(long j, int i, long j2);

    PollArrayWrapper(int newSize) {
        this.pollArray = new AllocatedNativeObject((newSize + 1) * 8, false);
        this.pollArrayAddress = this.pollArray.address();
        this.totalChannels = 1;
    }

    void initInterrupt(int fd0, int fd1) {
        this.interruptFD = fd1;
        putDescriptor(0, fd0);
        putEventOps(0, Net.POLLIN);
        putReventOps(0, 0);
    }

    void release(int i) {
    }

    void free() {
        this.pollArray.free();
    }

    void addEntry(SelChImpl sc) {
        putDescriptor(this.totalChannels, IOUtil.fdVal(sc.getFD()));
        putEventOps(this.totalChannels, 0);
        putReventOps(this.totalChannels, 0);
        this.totalChannels++;
    }

    static void replaceEntry(PollArrayWrapper source, int sindex, PollArrayWrapper target, int tindex) {
        target.putDescriptor(tindex, source.getDescriptor(sindex));
        target.putEventOps(tindex, source.getEventOps(sindex));
        target.putReventOps(tindex, source.getReventOps(sindex));
    }

    void grow(int newSize) {
        PollArrayWrapper temp = new PollArrayWrapper(newSize);
        for (int i = 0; i < this.totalChannels; i++) {
            replaceEntry(this, i, temp, i);
        }
        this.pollArray.free();
        this.pollArray = temp.pollArray;
        this.pollArrayAddress = this.pollArray.address();
    }

    int poll(int numfds, int offset, long timeout) {
        return poll0(this.pollArrayAddress + ((long) (offset * 8)), numfds, timeout);
    }

    public void interrupt() {
        interrupt(this.interruptFD);
    }
}
