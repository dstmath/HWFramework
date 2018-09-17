package android.view;

final class FrameInfo {
    private static final int ANIMATION_START = 6;
    private static final int DRAW_START = 8;
    private static final int FLAGS = 0;
    public static final long FLAG_WINDOW_LAYOUT_CHANGED = 1;
    private static final int HANDLE_INPUT_START = 5;
    private static final int INTENDED_VSYNC = 1;
    private static final int NEWEST_INPUT_EVENT = 4;
    private static final int OLDEST_INPUT_EVENT = 3;
    private static final int PERFORM_TRAVERSALS_START = 7;
    private static final int VSYNC = 2;
    long[] mFrameInfo = new long[9];

    FrameInfo() {
    }

    public void setVsync(long intendedVsync, long usedVsync) {
        this.mFrameInfo[1] = intendedVsync;
        this.mFrameInfo[2] = usedVsync;
        this.mFrameInfo[3] = Long.MAX_VALUE;
        this.mFrameInfo[4] = 0;
        this.mFrameInfo[0] = 0;
    }

    public void updateInputEventTime(long inputEventTime, long inputEventOldestTime) {
        if (inputEventOldestTime < this.mFrameInfo[3]) {
            this.mFrameInfo[3] = inputEventOldestTime;
        }
        if (inputEventTime > this.mFrameInfo[4]) {
            this.mFrameInfo[4] = inputEventTime;
        }
    }

    public void markInputHandlingStart() {
        this.mFrameInfo[5] = System.nanoTime();
    }

    public void markAnimationsStart() {
        this.mFrameInfo[6] = System.nanoTime();
    }

    public void markPerformTraversalsStart() {
        this.mFrameInfo[7] = System.nanoTime();
    }

    public void markDrawStart() {
        this.mFrameInfo[8] = System.nanoTime();
    }

    public void addFlags(long flags) {
        long[] jArr = this.mFrameInfo;
        jArr[0] = jArr[0] | flags;
    }

    public long getIntendedVsync() {
        return this.mFrameInfo[1];
    }
}
