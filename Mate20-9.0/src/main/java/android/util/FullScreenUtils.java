package android.util;

import com.huawei.android.content.pm.HwPackageManager;

public class FullScreenUtils {
    public static boolean setApplicationMaxAspectRatio(String packageName, float ar) {
        return HwPackageManager.setApplicationMaxAspectRatio(packageName, ar);
    }
}
