package ohos.media.audio;

public final class Timestamp {
    private long framePosition;
    private long nanoTimestamp;

    public long getFramePosition() {
        return this.framePosition;
    }

    public void setFramePosition(long j) {
        this.framePosition = j;
    }

    public long getNanoTimestamp() {
        return this.nanoTimestamp;
    }

    public void setNanoTimestamp(long j) {
        this.nanoTimestamp = j;
    }

    public enum Timebase {
        MONOTONIC(0),
        BOOTTIME(1);
        
        private final int timebase;

        private Timebase(int i) {
            this.timebase = i;
        }

        public int getValue() {
            return this.timebase;
        }
    }
}
