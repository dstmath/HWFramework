package android.widget.sr;

import android.os.Binder;
import android.os.UserHandle;
import android.util.Log;

public class Utils {
    private static final String LIB_AI_CLIENT_SO = "ai_client";
    private static final String TAG = "Utils";
    private static boolean sAlreadyChecked = false;
    private static boolean sIsSupport = false;

    public static boolean isSuperResolutionSupport() {
        if (sAlreadyChecked) {
            return sIsSupport;
        }
        try {
            System.loadLibrary(LIB_AI_CLIENT_SO);
            Log.i(TAG, "Utils_isSuperResolutionSupport [load libai_client.success]");
            sIsSupport = true;
        } catch (UnsatisfiedLinkError e) {
            Log.w(TAG, "Utils_isSuperResolutionSupport [load libai_client.so failed]");
            sIsSupport = false;
        } catch (Throwable th) {
            sIsSupport = false;
            sAlreadyChecked = true;
        }
        sAlreadyChecked = true;
        return sIsSupport;
    }

    public static boolean isDeviceOwner() {
        if (UserHandle.getUserId(Binder.getCallingUid()) == 0) {
            return true;
        }
        return false;
    }
}
