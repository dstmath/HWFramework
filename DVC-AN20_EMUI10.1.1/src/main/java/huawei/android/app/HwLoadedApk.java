package huawei.android.app;

import android.common.IHwLoadedApk;
import android.content.pm.ApplicationInfo;
import android.os.Trace;
import android.rms.iaware.AppTypeInfo;
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

    public void addBaseConfigsLibPaths(ApplicationInfo aInfo, List<String> outLibPaths) {
        if (!(aInfo == null || !aInfo.requestsIsolatedSplitLoading() || aInfo.splitSourceDirs == null || aInfo.splitDependencies == null || aInfo.splitDependencies.size() == 0 || aInfo.primaryCpuAbi == null || outLibPaths == null)) {
            int[] depVals = (int[]) aInfo.splitDependencies.get(0);
            if (!(depVals == null || depVals.length == 0)) {
                Trace.traceBegin(64, "addBaseConfigsLibPaths");
                int length = depVals.length;
                for (int i = 0; i < length; i++) {
                    int idx = depVals[i] - 1;
                    if (idx >= 0 && idx < aInfo.splitSourceDirs.length) {
                        outLibPaths.add(aInfo.splitSourceDirs[idx] + "!/lib/" + aInfo.primaryCpuAbi);
                    }
                }
                Trace.traceEnd(64);
            }
        }
    }

    public String makeSplitLibPaths(ApplicationInfo aInfo, int splitIdx) {
        if (aInfo == null || aInfo.splitSourceDirs == null || splitIdx < 0 || splitIdx > aInfo.splitSourceDirs.length) {
            return null;
        }
        String libSearchPath = null;
        Trace.traceBegin(64, "makeSplitLibPaths");
        List<String> libPaths = new ArrayList<>(32);
        boolean isBundledApp = false;
        if (aInfo.primaryCpuAbi != null) {
            List<String> splitApkPaths = new ArrayList<>(16);
            String splitApkPath = aInfo.splitSourceDirs[splitIdx - 1];
            splitApkPaths.add(splitApkPath);
            makeSplitConfigsLibPaths(aInfo, splitIdx, splitApkPaths);
            Iterator<String> it = splitApkPaths.iterator();
            while (it.hasNext()) {
                libPaths.add(it.next() + "!/lib/" + aInfo.primaryCpuAbi);
            }
            StringBuffer sBuffer = new StringBuffer(splitApkPath.substring(0, splitApkPath.lastIndexOf(File.separator)));
            sBuffer.append(File.separator);
            sBuffer.append("lib");
            sBuffer.append(File.separator);
            sBuffer.append(VMRuntime.getInstructionSet(aInfo.primaryCpuAbi));
            libPaths.add(sBuffer.toString());
        } else if (aInfo.nativeLibraryDir != null) {
            libPaths.add(aInfo.nativeLibraryDir);
        } else {
            Slog.w(TAG, "nativeLibraryDir is null, and primaryCpuAbi is null");
        }
        if ((aInfo.isSystemApp() && !aInfo.isUpdatedSystemApp()) || (aInfo.hwFlags & AppTypeInfo.APP_ATTRIBUTE_FORCE_DARK) != 0) {
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

    private void makeSplitConfigsLibPaths(ApplicationInfo aInfo, int splitIdx, List<String> splitApkPaths) {
        int[] depVals;
        if (!(aInfo.splitDependencies == null || aInfo.splitDependencies.size() == 0 || (depVals = (int[]) aInfo.splitDependencies.get(splitIdx)) == null || depVals.length == 0)) {
            int length = depVals.length;
            for (int i = 0; i < length; i++) {
                int idx = depVals[i] - 1;
                if (idx >= 0 && idx < aInfo.splitSourceDirs.length) {
                    splitApkPaths.add(aInfo.splitSourceDirs[idx]);
                }
            }
        }
    }
}
