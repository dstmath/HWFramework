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
    long[] mFrameInfo;

    FrameInfo() {
        this.mFrameInfo = new long[9];
    }

    public void setVsync(long intendedVsync, long usedVsync) {
        this.mFrameInfo[INTENDED_VSYNC] = intendedVsync;
        this.mFrameInfo[VSYNC] = usedVsync;
        this.mFrameInfo[OLDEST_INPUT_EVENT] = Long.MAX_VALUE;
        this.mFrameInfo[NEWEST_INPUT_EVENT] = 0;
        this.mFrameInfo[FLAGS] = 0;
    }

    public void updateInputEventTime(long inputEventTime, long inputEventOldestTime) {
        if (inputEventOldestTime < this.mFrameInfo[OLDEST_INPUT_EVENT]) {
            this.mFrameInfo[OLDEST_INPUT_EVENT] = inputEventOldestTime;
        }
        if (inputEventTime > this.mFrameInfo[NEWEST_INPUT_EVENT]) {
            this.mFrameInfo[NEWEST_INPUT_EVENT] = inputEventTime;
        }
    }

    public void markInputHandlingStart() {
        this.mFrameInfo[HANDLE_INPUT_START] = System.nanoTime();
    }

    public void markAnimationsStart() {
        this.mFrameInfo[ANIMATION_START] = System.nanoTime();
    }

    public void markPerformTraversalsStart() {
        this.mFrameInfo[PERFORM_TRAVERSALS_START] = System.nanoTime();
    }

    public void markDrawStart() {
        this.mFrameInfo[DRAW_START] = System.nanoTime();
    }

    public void addFlags(long flags) {
        long[] jArr = this.mFrameInfo;
        jArr[FLAGS] = jArr[FLAGS] | flags;
    }
}
