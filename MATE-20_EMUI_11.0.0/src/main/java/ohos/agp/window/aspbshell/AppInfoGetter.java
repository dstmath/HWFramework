package ohos.agp.window.aspbshell;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import java.util.Optional;
import ohos.aafwk.utils.log.LogDomain;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class AppInfoGetter {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "AppInfoGetter");

    private static native void nativeSetAppName(String str);

    public static String setAppNameToNative(Context context) {
        String str = "";
        if (context == null) {
            HiLog.error(LABEL, "setAppNameToNative context is null.", new Object[0]);
            return str;
        }
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        if (!(packageManager == null || applicationInfo == null)) {
            str = ((CharSequence) Optional.ofNullable(packageManager.getApplicationLabel(applicationInfo)).orElse(str)).toString();
        }
        nativeSetAppName(str);
        return str;
    }
}
