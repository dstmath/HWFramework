package android.app;

public class HwApsInterface {
    public static native void nativeFpsControllerRelease(long j);

    public static native void nativeFpsRequestRelease(long j);

    public static native int nativeGetCurFPS(long j);

    public static native float nativeGetCurrentSdrRatio();

    public static native String nativeGetGTCCResult();

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

    public static native void nativeSetNeedGTCCResult(int i);

    public static native void nativeSetNonplayFrame(int i);

    public static native void nativeSetOpenglGameType(int i);

    public static native void nativeSetPowerKitFrame(int i);

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

    static {
        System.loadLibrary("hwaps");
    }
}
