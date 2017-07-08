package android.media;

public final class AudioTimestamp {
    public static final int TIMEBASE_BOOTTIME = 1;
    public static final int TIMEBASE_MONOTONIC = 0;
    public long framePosition;
    public long nanoTime;
}
