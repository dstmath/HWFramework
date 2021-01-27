package android.graphics;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class FrameInfo {
    private static final int ANIMATION_START = 6;
    private static final int DRAW_START = 8;
    private static final int FLAGS = 0;
    public static final long FLAG_SURFACE_CANVAS = 4;
    public static final long FLAG_WINDOW_LAYOUT_CHANGED = 1;
    private static final int HANDLE_INPUT_START = 5;
    private static final int INTENDED_VSYNC = 1;
    private static final int NEWEST_INPUT_EVENT = 4;
    private static final int OLDEST_INPUT_EVENT = 3;
    private static final int PERFORM_TRAVERSALS_START = 7;
    private static final int VSYNC = 2;
    public long[] frameInfo = new long[9];

    @Retention(RetentionPolicy.SOURCE)
    public @interface FrameInfoFlags {
    }

    public void setVsync(long intendedVsync, long usedVsync) {
        long[] jArr = this.frameInfo;
        jArr[1] = intendedVsync;
        jArr[2] = usedVsync;
        jArr[3] = Long.MAX_VALUE;
        jArr[4] = 0;
        jArr[0] = 0;
    }

    public void updateInputEventTime(long inputEventTime, long inputEventOldestTime) {
        long[] jArr = this.frameInfo;
        if (inputEventOldestTime < jArr[3]) {
            jArr[3] = inputEventOldestTime;
        }
        long[] jArr2 = this.frameInfo;
        if (inputEventTime > jArr2[4]) {
            jArr2[4] = inputEventTime;
        }
    }

    public void markInputHandlingStart() {
        this.frameInfo[5] = System.nanoTime();
    }

    public void markAnimationsStart() {
        this.frameInfo[6] = System.nanoTime();
    }

    public void markPerformTraversalsStart() {
        this.frameInfo[7] = System.nanoTime();
    }

    public void markDrawStart() {
        this.frameInfo[8] = System.nanoTime();
    }

    public void addFlags(long flags) {
        long[] jArr = this.frameInfo;
        jArr[0] = jArr[0] | flags;
    }

    public long getIntendedVsync() {
        return this.frameInfo[1];
    }
}
