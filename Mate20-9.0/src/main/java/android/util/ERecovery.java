package android.util;

public class ERecovery {
    public static final String TAG = "ERecovery";

    public static long eRecoveryReport(ERecoveryEvent eventdata) {
        try {
            return ERecoveryNative.eRecoveryReport(eventdata);
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "erecovery_report failed for no implementation of native");
            return -1;
        }
    }
}
