package android.app;

import android.content.res.Configuration;
import android.content.res.Resources;
import com.huawei.utils.reflect.EasyInvokeUtils;

public class ActivityThreadUtils extends EasyInvokeUtils {
    @Deprecated
    public static Resources getTopLevelResources(ActivityThread mainThread, String resDir, int displayId, Configuration overrideConfiguration, LoadedApk pkgInfo) {
        return mainThread.getTopLevelResources(resDir, null, null, null, displayId, pkgInfo);
    }

    public static Resources getTopLevelResources(ActivityThread mainThread, String resDir, String[] splitResDirs, String[] overlayDirs, String[] libDirs, int displayId, Configuration overrideConfiguration, LoadedApk pkgInfo) {
        return mainThread.getTopLevelResources(resDir, splitResDirs, overlayDirs, libDirs, displayId, pkgInfo);
    }
}
