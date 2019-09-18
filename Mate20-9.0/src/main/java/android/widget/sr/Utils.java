package android.widget.sr;

import android.os.Binder;
import android.os.UserHandle;
import android.util.Log;

public class Utils {
    private static final String LIB_SR_CLIENT_SO = "sr_client";
    private static final String TAG = "Utils";
    private static boolean sAlreadyChecked = false;
    private static boolean sIsSupport = false;

    public static boolean isSuperResolutionSupport() {
        if (sAlreadyChecked) {
            return sIsSupport;
        }
        boolean ret = false;
        try {
            System.loadLibrary(LIB_SR_CLIENT_SO);
            Log.i(TAG, "Utils_isSuperResolutionSupport [load libsr_client.success]");
            ret = true;
        } catch (UnsatisfiedLinkError e) {
            Log.w(TAG, "Utils_isSuperResolutionSupport [load libsr_client.so failed]");
        } catch (Throwable th) {
            sIsSupport = false;
            sAlreadyChecked = true;
            throw th;
        }
        sIsSupport = ret;
        sAlreadyChecked = true;
        return sIsSupport;
    }

    public static boolean isDeviceOwner() {
        return UserHandle.getUserId(Binder.getCallingUid()) == 0;
    }
}
