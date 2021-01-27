package android.rms.iaware;

import android.util.Log;

public class IAwareDecryptNative {
    private static final String TAG = "IAwareDecryptNative";

    private static native byte[] nativeAwareDecrypt(byte[] bArr, byte[] bArr2);

    static {
        try {
            System.loadLibrary("iawarewb_jni");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "load iawarewb failed");
        }
    }

    public static byte[] getNativeComponent(byte[] iv, byte[] component) {
        return nativeAwareDecrypt(iv, component);
    }
}
