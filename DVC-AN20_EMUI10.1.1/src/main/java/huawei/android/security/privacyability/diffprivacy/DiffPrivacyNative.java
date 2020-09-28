package huawei.android.security.privacyability.diffprivacy;

import android.util.Log;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class DiffPrivacyNative {
    private static final String TAG = "DiffPrivacyNative";

    @HwSystemApi
    public static native String nativeDiffPrivacy(String str, String str2);

    @HwSystemApi
    public static native String nativeDiffPrivacy(int[] iArr, String str);

    static {
        try {
            System.loadLibrary("diffprivacy");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Load libdiffprivacy library failed" + e.getMessage());
        }
    }
}
