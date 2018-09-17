package com.android.server.power;

import huawei.com.android.server.policy.fingersense.CustomGestureDetector;

public class HwDisplayPowerController {
    private static final int MAX_RETRY_COUNT = 4;
    private static final int MIN_LUX_VALUE = 3;
    private static final String TAG = "HwDisplayPowerController";
    private static boolean mCoverClose;
    private static int mSensorCount;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.power.HwDisplayPowerController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.power.HwDisplayPowerController.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.power.HwDisplayPowerController.<clinit>():void");
    }

    public static void setIfCoverClosed(boolean isClosed) {
        mCoverClose = isClosed;
        mSensorCount = 0;
    }

    public static boolean isCoverClosed() {
        return mCoverClose;
    }

    public static boolean shouldFilteInvalidSensorVal(float lux) {
        if (mCoverClose) {
            return true;
        }
        if (MAX_RETRY_COUNT <= mSensorCount || lux >= CustomGestureDetector.TOUCH_TOLERANCE) {
            mSensorCount = MAX_RETRY_COUNT;
            return false;
        }
        mSensorCount++;
        return true;
    }
}
