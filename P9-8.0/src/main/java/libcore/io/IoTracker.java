package libcore.io;

import dalvik.system.BlockGuard;

public final class IoTracker {
    private boolean isOpen = true;
    private Mode mode = Mode.READ;
    private int opCount;
    private int totalByteCount;

    public enum Mode {
        READ,
        WRITE
    }

    public void trackIo(int byteCount) {
        this.opCount++;
        this.totalByteCount += byteCount;
        if (this.isOpen && this.opCount > 10 && this.totalByteCount < 5120) {
            BlockGuard.getThreadPolicy().onUnbufferedIO();
            this.isOpen = false;
        }
    }

    public void trackIo(int byteCount, Mode mode) {
        if (this.mode != mode) {
            reset();
            this.mode = mode;
        }
        trackIo(byteCount);
    }

    public void reset() {
        this.opCount = 0;
        this.totalByteCount = 0;
    }
}
