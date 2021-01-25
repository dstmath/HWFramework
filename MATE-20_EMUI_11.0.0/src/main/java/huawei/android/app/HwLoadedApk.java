package huawei.android.app;

import android.common.IHwLoadedApk;
import android.content.pm.ApplicationInfo;
import android.os.Trace;
import android.text.TextUtils;
import android.util.Slog;
import dalvik.system.VMRuntime;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HwLoadedApk implements IHwLoadedApk {
    private static final int LIB_PATH_SIZE = 32;
    private static final int SPLIT_APK_PATH_SIZE = 16;
    private static final String TAG = "HwLoadedApk";

    private HwLoadedApk() {
    }

    private static class HwLoadedApkHolder {
        private static HwLoadedApk sInstance = new HwLoadedApk();

        private HwLoadedApkHolder() {
        }
    }

    public static final HwLoadedApk getDefault() {
        return HwLoadedApkHolder.sInstance;
    }

    public void addBaseConfigsLibPaths(ApplicationInfo appInfo, List<String> outLibPaths) {
        if (!(appInfo == null || !appInfo.requestsIsolatedSplitLoading() || appInfo.splitSourceDirs == null || appInfo.splitDependencies == null || appInfo.splitDependencies.size() == 0 || appInfo.primaryCpuAbi == null || outLibPaths == null)) {
            int[] depVals = (int[]) appInfo.splitDependencies.get(0);
            if (!(depVals == null || depVals.length == 0)) {
                Trace.traceBegin(64, "addBaseConfigsLibPaths");
                int length = depVals.length;
                for (int i = 0; i < length; i++) {
                    int idx = depVals[i] - 1;
                    if (idx >= 0 && idx < appInfo.splitSourceDirs.length) {
                        outLibPaths.add(appInfo.splitSourceDirs[idx] + "!/lib/" + appInfo.primaryCpuAbi);
                    }
                }
                Trace.traceEnd(64);
            }
        }
    }

    public String makeSplitLibPaths(ApplicationInfo appInfo, int splitIdx) {
        if (appInfo == null || appInfo.splitSourceDirs == null || splitIdx < 0 || splitIdx > appInfo.splitSourceDirs.length) {
            return null;
        }
        String libSearchPath = null;
        Trace.traceBegin(64, "makeSplitLibPaths");
        List<String> libPaths = new ArrayList<>(32);
        boolean isBundledApp = false;
        if (appInfo.primaryCpuAbi != null) {
            List<String> splitApkPaths = new ArrayList<>(16);
            String splitApkPath = appInfo.splitSourceDirs[splitIdx - 1];
            splitApkPaths.add(splitApkPath);
            makeSplitConfigsLibPaths(appInfo, splitIdx, splitApkPaths);
            Iterator<String> it = splitApkPaths.iterator();
            while (it.hasNext()) {
                libPaths.add(it.next() + "!/lib/" + appInfo.primaryCpuAbi);
            }
            StringBuffer buf = new StringBuffer(splitApkPath.substring(0, splitApkPath.lastIndexOf(File.separator)));
            buf.append(File.separator);
            buf.append("lib");
            buf.append(File.separator);
            buf.append(VMRuntime.getInstructionSet(appInfo.primaryCpuAbi));
            libPaths.add(buf.toString());
        } else if (appInfo.nativeLibraryDir != null) {
            libPaths.add(appInfo.nativeLibraryDir);
        } else {
            Slog.w(TAG, "nativeLibraryDir is null, and primaryCpuAbi is null");
        }
        if ((appInfo.isSystemApp() && !appInfo.isUpdatedSystemApp()) || (appInfo.hwFlags & 536870912) != 0) {
            isBundledApp = true;
        }
        if (isBundledApp) {
            libPaths.add(System.getProperty("java.library.path"));
        }
        if (!libPaths.isEmpty()) {
            libSearchPath = TextUtils.join(File.pathSeparator, libPaths);
        }
        Trace.traceEnd(64);
        Slog.d(TAG, "libSearchPath: " + libSearchPath);
        return libSearchPath;
    }

    private void makeSplitConfigsLibPaths(ApplicationInfo appInfo, int splitIdx, List<String> splitApkPaths) {
        int[] depVals;
        if (!(appInfo.splitDependencies == null || appInfo.splitDependencies.size() == 0 || (depVals = (int[]) appInfo.splitDependencies.get(splitIdx)) == null || depVals.length == 0)) {
            int length = depVals.length;
            for (int i = 0; i < length; i++) {
                int idx = depVals[i] - 1;
                if (idx >= 0 && idx < appInfo.splitSourceDirs.length) {
                    splitApkPaths.add(appInfo.splitSourceDirs[idx]);
                }
            }
        }
    }
}
