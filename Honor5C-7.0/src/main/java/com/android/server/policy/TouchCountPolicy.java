package com.android.server.policy;

import android.util.SparseIntArray;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class TouchCountPolicy {
    private static final int DAY_COUNT = 7;
    private static final int HOUR_COUNT = 24;
    private static final int TOTAL_TOUCH_INFO_COUNT = 168;
    private static final SparseIntArray sDayIndexMap = null;
    private int mLastIndex;
    private long mTouchCount;
    private int[] mTouchInfo;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.policy.TouchCountPolicy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.policy.TouchCountPolicy.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.policy.TouchCountPolicy.<clinit>():void");
    }

    public TouchCountPolicy() {
        this.mTouchInfo = new int[TOTAL_TOUCH_INFO_COUNT];
        this.mTouchCount = 0;
        this.mLastIndex = 0;
    }

    public void updateTouchCountInfo() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(System.currentTimeMillis());
        int index = (sDayIndexMap.get(calendar.get(DAY_COUNT), 0) * HOUR_COUNT) + calendar.get(11);
        if (index < TOTAL_TOUCH_INFO_COUNT) {
            if (this.mLastIndex != index) {
                this.mTouchCount = 1;
                this.mLastIndex = index;
            } else {
                this.mTouchCount++;
            }
            this.mTouchInfo[index] = mapTouchCountInfo(this.mTouchCount);
        }
    }

    private int mapTouchCountInfo(long count) {
        if (count == 0) {
            return 0;
        }
        if (count < 10) {
            return 1;
        }
        if (count < 100) {
            return 2;
        }
        return 3;
    }

    public int[] getTouchCountInfo() {
        int[] copy = Arrays.copyOf(this.mTouchInfo, this.mTouchInfo.length);
        Arrays.fill(this.mTouchInfo, 0);
        this.mTouchCount = 0;
        return copy;
    }

    public int[] getDefaultTouchCountInfo() {
        int[] defaultInfo = new int[TOTAL_TOUCH_INFO_COUNT];
        Arrays.fill(defaultInfo, -1);
        return defaultInfo;
    }
}
