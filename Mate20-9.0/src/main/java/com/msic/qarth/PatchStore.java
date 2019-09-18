package com.msic.qarth;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Process;
import android.text.TextUtils;
import com.msic.qarth.Utils.CommonUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PatchStore {
    private static final String PACKAGE_DISABLE_PATCH_OVERLAY_ROOT = "/data/hotpatch/fwkpatchdir/";
    private static final String PACKAGE_PATCH_OVERLAY_ROOT = Constants.FWK_HOT_PATCH_PATH;
    private static final String SYSTEM_DISABLE_PATCH_OVERLAY_ROOT = "/data/hotpatch/fwkpatchdir/system/";
    private static final String TAG = PatchStore.class.getSimpleName();

    public static void check(Context context, String packageName) {
        if (Constants.DEBUG) {
            String str = TAG;
            QarthLog.d(str, "Qarth check app : " + packageName);
        }
        checkOrRunAll(context, packageName);
    }

    public static void check(Context context, String packageName, ClassLoader classLoader) {
        if (Constants.DEBUG) {
            String str = TAG;
            QarthLog.d(str, "Qarth check System server : " + packageName);
        }
        checkSystemRunAll(context, packageName, classLoader);
    }

    public static void checkOrRunAll(Context context, String packageName) {
        String qarthPath = PACKAGE_PATCH_OVERLAY_ROOT + "all";
        File qarthPathFile = new File(qarthPath);
        if (qarthPathFile.exists()) {
            File[] files = qarthPathFile.listFiles();
            if (!(files == null || files.length == 0)) {
                checkAndLoad(context, Constants.COMMON_PATCH_PKG_NAME, qarthPath, context.getClassLoader());
            }
        }
        checkAndLoad(context, packageName, PACKAGE_PATCH_OVERLAY_ROOT + packageName, context.getClassLoader());
    }

    private static void checkSystemRunAll(Context context, String packageName, ClassLoader classLoader) {
        if (!checkAndLoad(context, packageName, PACKAGE_PATCH_OVERLAY_ROOT + packageName, classLoader)) {
            checkAndLoad(context, packageName, PACKAGE_PATCH_OVERLAY_ROOT + packageName, classLoader);
        }
    }

    private static boolean checkAndLoad(Context context, String packageName, String path, ClassLoader loader) {
        PatchDisFileFilter disFileFilter;
        Iterator<QarthContext> it;
        PatchDisFileFilter disFileFilterOwn;
        Context context2 = context;
        String str = packageName;
        String str2 = path;
        if (Constants.DEBUG) {
            QarthLog.d(TAG, "------------------------------------------------------");
            QarthLog.d(TAG, "start load patch. packageName : " + str + ", patchPath : " + str2);
            QarthLog.d(TAG, "------------------------------------------------------");
        }
        long start = System.currentTimeMillis();
        File pkgDir = new File(str2);
        if (!pkgDir.exists()) {
            QarthLog.d(TAG, "patch path " + str2 + " not exist");
            return false;
        }
        PackageInfo info = null;
        if (context2 != null) {
            try {
                PackageManager pm = context.getPackageManager();
                if (pm != null && !Constants.COMMON_PATCH_PKG_NAME.equals(str) && !"systemserver".equals(str)) {
                    info = pm.getPackageInfo(str, 128);
                }
            } catch (PackageManager.NameNotFoundException e) {
                QarthLog.d(TAG, "packageName " + str + "not find in system");
            }
        }
        if (info != null && Constants.DEBUG) {
            QarthLog.d(TAG, "package: " + info.packageName + ", versionName: " + info.versionName + ", versionCode: " + info.versionCode);
        }
        if (TextUtils.isEmpty(path)) {
            QarthLog.d(TAG, "empty patch path");
            return false;
        }
        if (Constants.DEBUG) {
            QarthLog.d(TAG, "\t==> elapse time 1: " + (System.currentTimeMillis() - start) + " ms");
        }
        PatchFileFilter fileFilter = info == null ? new PatchFileFilter() : new PatchFileFilter(info);
        pkgDir.listFiles(fileFilter);
        List<PatchFile> patchFiles = fileFilter.getPatchFiles();
        String pkgDisPath = PACKAGE_DISABLE_PATCH_OVERLAY_ROOT + str;
        if (Constants.COMMON_PATCH_PKG_NAME.equals(str)) {
            pkgDisPath = "/data/hotpatch/fwkpatchdir/system/all";
        } else if (str.equals("systemserver")) {
            pkgDisPath = "/data/hotpatch/fwkpatchdir/system/systemserver";
        } else {
            QarthLog.d(TAG, "checkAndLoad single package: " + str);
        }
        File pkgDisDir = new File(pkgDisPath);
        if (info == null) {
            disFileFilter = new PatchDisFileFilter(str);
        } else {
            disFileFilter = new PatchDisFileFilter(info, str);
        }
        if (pkgDisDir.exists()) {
            pkgDisDir.listFiles(disFileFilter);
            List<PatchFile> disPatchFiles = disFileFilter.getDisPatchFiles();
            if (Constants.COMMON_PATCH_PKG_NAME.equals(str) && context2 != null && (context.getApplicationInfo().flags & 1) == 0) {
                File pkgDisDirOwn = new File(PACKAGE_DISABLE_PATCH_OVERLAY_ROOT + context.getPackageName());
                if (pkgDisDirOwn.exists()) {
                    if (info == null) {
                        disFileFilterOwn = new PatchDisFileFilter(str);
                    } else {
                        disFileFilterOwn = new PatchDisFileFilter(info, str);
                    }
                    pkgDisDirOwn.listFiles(disFileFilterOwn);
                    List<PatchFile> disPatchFilesOwn = disFileFilterOwn.getDisPatchFiles();
                    disPatchFiles.removeAll(disPatchFilesOwn);
                    disPatchFiles.addAll(disPatchFilesOwn);
                }
            }
            patchFiles.removeAll(disPatchFiles);
        }
        if (patchFiles.size() == 0) {
            QarthLog.e(TAG, "no patch file in " + str2);
            return false;
        }
        if (Constants.DEBUG) {
            QarthLog.d(TAG, "\t==> elapse time 1.1: " + (System.currentTimeMillis() - start) + " ms");
        }
        if (Constants.DEBUG) {
            QarthLog.d(TAG, "patch files : " + patchFiles);
        }
        List<QarthContext> loadList = new ArrayList<>();
        for (PatchFile patchFile : patchFiles) {
            QarthContext qc = new QarthContext();
            qc.packageName = str;
            qc.context = context2;
            qc.cpuAbi = CommonUtil.determineCpuAbi(context);
            qc.qarthClassLoader = loader;
            qc.patchFile = patchFile;
            qc.qarthFile = qc.patchFile.getFile();
            loadList.add(qc);
            fileFilter = fileFilter;
            context2 = context;
        }
        ClassLoader classLoader = loader;
        PatchFileFilter patchFileFilter = fileFilter;
        if (Constants.DEBUG) {
            QarthLog.d(TAG, "\t==> elapse time 2: " + (System.currentTimeMillis() - start) + " ms");
        }
        Iterator<QarthContext> it2 = loadList.iterator();
        while (it2.hasNext()) {
            QarthContext qc2 = it2.next();
            if (!PatchLoader.getInstance().load(qc2)) {
                String str3 = TAG;
                StringBuilder sb = new StringBuilder();
                it = it2;
                sb.append("\t\tload patch [");
                sb.append(qc2.qarthFile.getName());
                sb.append("] failed");
                QarthLog.e(str3, sb.toString());
            } else {
                it = it2;
            }
            it2 = it;
        }
        if (Constants.DEBUG) {
            QarthLog.d(TAG, "\t==> elapse time 3: " + (System.currentTimeMillis() - start) + " ms");
        }
        return true;
    }

    public static boolean createDisableExceptionQarthFile(Throwable throwInfor) {
        QarthLog.d(TAG, "createDisableExceptionQarthFile");
        QarthDisFileCreator qarthDisFileCreator = QarthDisFileCreator.getQarthDisFileCreator();
        if (qarthDisFileCreator != null) {
            return qarthDisFileCreator.disableExceptionQarthPatch(throwInfor);
        }
        return false;
    }

    public static void killCrashApplication() {
        QarthLog.d(TAG, "killCrashApplication");
        Process.killProcess(Process.myPid());
        System.exit(10);
    }
}
