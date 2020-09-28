package android.media;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class AudioTimestamp {
    public static final int TIMEBASE_BOOTTIME = 1;
    public static final int TIMEBASE_MONOTONIC = 0;
    public long framePosition;
    public long nanoTime;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Timebase {
    }
}
