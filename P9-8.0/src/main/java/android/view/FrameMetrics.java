package android.view;

public final class FrameMetrics {
    public static final int ANIMATION_DURATION = 2;
    public static final int COMMAND_ISSUE_DURATION = 6;
    public static final int DRAW_DURATION = 4;
    private static final int[] DURATIONS = new int[]{1, 5, 5, 6, 6, 7, 7, 8, 8, 9, 10, 11, 11, 12, 12, 13, 1, 13};
    public static final int FIRST_DRAW_FRAME = 9;
    private static final int FRAME_INFO_FLAG_FIRST_DRAW = 1;
    public static final int INPUT_HANDLING_DURATION = 1;
    public static final int INTENDED_VSYNC_TIMESTAMP = 10;
    public static final int LAYOUT_MEASURE_DURATION = 3;
    public static final int SWAP_BUFFERS_DURATION = 7;
    public static final int SYNC_DURATION = 5;
    public static final int TOTAL_DURATION = 8;
    public static final int UNKNOWN_DELAY_DURATION = 0;
    public static final int VSYNC_TIMESTAMP = 11;
    final long[] mTimingData;

    public FrameMetrics(FrameMetrics other) {
        this.mTimingData = new long[16];
        System.arraycopy(other.mTimingData, 0, this.mTimingData, 0, this.mTimingData.length);
    }

    FrameMetrics() {
        this.mTimingData = new long[16];
    }

    /* JADX WARNING: Missing block: B:3:0x000b, code:
            return -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long getMetric(int id) {
        int i = 1;
        if (id < 0 || id > 11 || this.mTimingData == null) {
            return -1;
        }
        if (id == 9) {
            if ((this.mTimingData[0] & 1) == 0) {
                i = 0;
            }
            return (long) i;
        } else if (id == 10) {
            return this.mTimingData[1];
        } else {
            if (id == 11) {
                return this.mTimingData[2];
            }
            int durationsIdx = id * 2;
            return this.mTimingData[DURATIONS[durationsIdx + 1]] - this.mTimingData[DURATIONS[durationsIdx]];
        }
    }
}
