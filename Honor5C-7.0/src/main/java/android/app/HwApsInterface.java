package android.app;

public class HwApsInterface {
    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.HwApsInterface.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.HwApsInterface.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.app.HwApsInterface.<clinit>():void");
    }

    public static native void nativeFpsControllerRelease(long j);

    public static native void nativeFpsRequestRelease(long j);

    public static native int nativeGetCurFPS(long j);

    public static native float nativeGetCurrentSdrRatio();

    public static native int nativeGetGameRoundDuration();

    public static native int nativeGetOpenglGameType();

    public static native int nativeGetResultJudgedByFps();

    public static native int nativeGetTargetFPS(long j);

    public static native long nativeInitFpsController();

    public static native long nativeInitFpsRequest(long j);

    public static native int nativeIsDepthGame();

    public static native boolean nativeIsSdrCase();

    public static native void nativePowerCtroll(long j);

    public static native void nativeSetApsVersion(String str);

    public static native void nativeSetCtrlBattery(String str, int i);

    public static native void nativeSetGamePid(int i);

    public static native void nativeSetNonplayFrame(int i);

    public static native void nativeSetOpenglGameType(int i);

    public static native void nativeSetSceneFixed(String str, int i);

    public static native void nativeSetSceneFps(String str, int i, int i2);

    public static native void nativeSetSceneRatio(String str, int i, double d);

    public static native void nativeSetSdrRatio(float f);

    public static native void nativeSetTouchGameType(int i);

    public static native void nativeSetTouchState(int i);

    public static native void nativeStart(long j, int i);

    public static native void nativeStartFeedback(long j, int i);

    public static native void nativeStartSdr(float f);

    public static native void nativeStop(long j);

    public static native void nativeStopSdr();

    public static native void nativeStopSdrImmediately();
}
