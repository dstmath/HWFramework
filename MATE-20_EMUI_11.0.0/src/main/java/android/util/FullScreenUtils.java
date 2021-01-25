package android.util;

import android.content.pm.AbsApplicationInfo;
import com.huawei.android.content.pm.HwPackageManager;

public class FullScreenUtils {
    public static boolean setApplicationMaxAspectRatio(String packageName, float ar) {
        return HwPackageManager.setApplicationAspectRatio(packageName, AbsApplicationInfo.MAX_ASPECT_RATIO, ar);
    }
}
