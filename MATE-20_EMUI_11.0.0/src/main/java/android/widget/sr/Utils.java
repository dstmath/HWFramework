package android.widget.sr;

import android.os.Binder;
import android.os.UserHandle;

public class Utils {
    private static final String LIB_SR_CLIENT_SO = "sr_client";
    private static final String TAG = "Utils";
    private static boolean sIsAlreadyChecked = false;
    private static boolean sIsSupport = false;

    private Utils() {
    }

    public static boolean isSuperResolutionSupport() {
        return false;
    }

    public static boolean isDeviceOwner() {
        return UserHandle.getUserId(Binder.getCallingUid()) == 0;
    }
}
