package android.view;

import android.annotation.UnsupportedAppUsage;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class FrameMetrics {
    public static final int ANIMATION_DURATION = 2;
    public static final int COMMAND_ISSUE_DURATION = 6;
    public static final int DRAW_DURATION = 4;
    private static final int[] DURATIONS = {1, 5, 5, 6, 6, 7, 7, 8, 8, 9, 10, 11, 11, 12, 12, 13, 1, 13};
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
    @UnsupportedAppUsage
    final long[] mTimingData;

    @Retention(RetentionPolicy.SOURCE)
    private @interface Index {
        public static final int ANIMATION_START = 6;
        public static final int DRAW_START = 8;
        public static final int FLAGS = 0;
        public static final int FRAME_COMPLETED = 13;
        public static final int FRAME_STATS_COUNT = 16;
        public static final int HANDLE_INPUT_START = 5;
        public static final int INTENDED_VSYNC = 1;
        public static final int ISSUE_DRAW_COMMANDS_START = 11;
        public static final int NEWEST_INPUT_EVENT = 4;
        public static final int OLDEST_INPUT_EVENT = 3;
        public static final int PERFORM_TRAVERSALS_START = 7;
        public static final int SWAP_BUFFERS = 12;
        public static final int SYNC_QUEUED = 9;
        public static final int SYNC_START = 10;
        public static final int VSYNC = 2;
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Metric {
    }

    public FrameMetrics(FrameMetrics other) {
        this.mTimingData = new long[16];
        long[] jArr = other.mTimingData;
        long[] jArr2 = this.mTimingData;
        System.arraycopy(jArr, 0, jArr2, 0, jArr2.length);
    }

    FrameMetrics() {
        this.mTimingData = new long[16];
    }

    public long getMetric(int id) {
        long[] jArr;
        if (id < 0 || id > 11 || (jArr = this.mTimingData) == null) {
            return -1;
        }
        if (id == 9) {
            return (jArr[0] & 1) != 0 ? 1 : 0;
        }
        if (id == 10) {
            return jArr[1];
        }
        if (id == 11) {
            return jArr[2];
        }
        int durationsIdx = id * 2;
        int[] iArr = DURATIONS;
        return jArr[iArr[durationsIdx + 1]] - jArr[iArr[durationsIdx]];
    }
}
