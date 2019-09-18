package android.app;

public class HwApsInterface {
    public static native void nativeFpsControllerRelease(long j);

    public static native void nativeFpsRequestRelease(long j);

    public static native int nativeGetCurFPS(long j);

    public static native float nativeGetCurrentSdrRatio();

    public static native String nativeGetGTCCResult();

    public static native int nativeGetTargetFPS(long j);

    public static native long nativeInitFpsController();

    public static native long nativeInitFpsRequest(long j);

    public static native boolean nativeIsSdrCase();

    public static native void nativePowerCtroll(long j);

    public static native void nativeSetGamePid(int i);

    public static native void nativeSetGameTextureQuality(int i);

    public static native void nativeSetNeedGTCCResult(int i);

    public static native void nativeSetSdrRatio(float f);

    public static native void nativeStart(long j, int i);

    public static native void nativeStartFeedback(long j, int i);

    public static native void nativeStop(long j);

    static {
        System.loadLibrary("hwaps");
    }
}
