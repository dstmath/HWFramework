package android.view;

public final class FrameMetrics {
    public static final int ANIMATION_DURATION = 2;
    public static final int COMMAND_ISSUE_DURATION = 6;
    public static final int DRAW_DURATION = 4;
    private static final int[] DURATIONS = null;
    public static final int FIRST_DRAW_FRAME = 9;
    private static final int FRAME_INFO_FLAG_FIRST_DRAW = 1;
    public static final int INPUT_HANDLING_DURATION = 1;
    public static final int LAYOUT_MEASURE_DURATION = 3;
    public static final int SWAP_BUFFERS_DURATION = 7;
    public static final int SYNC_DURATION = 5;
    public static final int TOTAL_DURATION = 8;
    public static final int UNKNOWN_DELAY_DURATION = 0;
    final long[] mTimingData;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.FrameMetrics.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.FrameMetrics.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.FrameMetrics.<clinit>():void");
    }

    public FrameMetrics(FrameMetrics other) {
        this.mTimingData = new long[14];
        System.arraycopy(other.mTimingData, 0, this.mTimingData, 0, this.mTimingData.length);
    }

    FrameMetrics() {
        this.mTimingData = new long[14];
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long getMetric(int id) {
        int i = 0;
        if (id < 0 || id > FIRST_DRAW_FRAME || this.mTimingData == null) {
            return -1;
        }
        if (id == FIRST_DRAW_FRAME) {
            if ((this.mTimingData[0] & 1) != 0) {
                i = INPUT_HANDLING_DURATION;
            }
            return (long) i;
        }
        int durationsIdx = id * ANIMATION_DURATION;
        return this.mTimingData[DURATIONS[durationsIdx + INPUT_HANDLING_DURATION]] - this.mTimingData[DURATIONS[durationsIdx]];
    }
}
