package android.util;

final class ERecoveryNative {
    public static native long eRecoveryReport(ERecoveryEvent eRecoveryEvent);

    ERecoveryNative() {
    }

    static {
        try {
            Log.d(ERecovery.TAG, "Load library erecovery_jni");
            System.loadLibrary("erecovery_jni");
        } catch (UnsatisfiedLinkError e) {
            Log.e(ERecovery.TAG, "Library erecovery_jni not found");
        }
    }
}
