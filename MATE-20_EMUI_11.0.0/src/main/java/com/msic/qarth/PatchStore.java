package com.msic.qarth;

import android.app.ActivityThread;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Process;
import android.text.TextUtils;
import com.msic.qarth.Utils.CommonUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PatchStore {
    private static final int INVALID_UID = -1;
    private static final String PACKAGE_DISABLE_PATCH_OVERLAY_ROOT = "/data/hotpatch/fwkpatchdir/";
    private static final String PACKAGE_PATCH_OVERLAY_ROOT = Constants.FWK_HOT_PATCH_PATH;
    private static final String SYSTEM_DISABLE_PATCH_OVERLAY_ROOT = "/data/hotpatch/fwkpatchdir/system/";
    private static final String TAG = PatchStore.class.getSimpleName();
    private static final String THREAD_PATCH_STORE = "THREAD_PATCH_STORE";
    private static Set<String> loadedPatchPaths = new HashSet();

    public static void check(Context context, String packageName) {
        String str = TAG;
        QarthLog.i(str, "Qarth check app : " + packageName);
        checkOrRunAll(context, packageName);
    }

    public static void check(Context context, String packageName, ClassLoader classLoader) {
        String str = TAG;
        QarthLog.i(str, "Qarth check System server : " + packageName);
        checkSystemRunAll(context, packageName, classLoader);
    }

    private static void checkOrRunAll(Context context, String packageName) {
        File[] files;
        String qarthPath = PACKAGE_PATCH_OVERLAY_ROOT + "all";
        File qarthPathFile = new File(qarthPath);
        if (!(!qarthPathFile.exists() || (files = qarthPathFile.listFiles()) == null || files.length == 0)) {
            checkAndLoad(context, Constants.COMMON_PATCH_PKG_NAME, qarthPath, context.getClassLoader());
        }
        checkAndLoad(context, packageName, PACKAGE_PATCH_OVERLAY_ROOT + packageName, context.getClassLoader());
    }

    private static void checkSystemRunAll(Context context, String packageName, ClassLoader classLoader) {
        File[] files;
        if (!checkAndLoad(context, packageName, PACKAGE_PATCH_OVERLAY_ROOT + packageName, classLoader)) {
            checkAndLoad(context, packageName, PACKAGE_PATCH_OVERLAY_ROOT + packageName, classLoader);
        }
        String allPath = PACKAGE_PATCH_OVERLAY_ROOT + "all";
        File qarthPathFile = new File(allPath);
        if (qarthPathFile.exists() && (files = qarthPathFile.listFiles()) != null && files.length != 0) {
            checkAndLoad(context, Constants.COMMON_PATCH_PKG_NAME, allPath, classLoader);
        }
    }

    private static boolean checkAndLoad(Context context, String packageName, String path, ClassLoader loader) {
        PatchFileFilter fileFilter;
        PatchDisFileFilter disFileFilter;
        List<QarthContext> loadList;
        PatchDisFileFilter disFileFilterOwn;
        Context context2 = context;
        QarthLog.i(TAG, "------------------------------------------------------");
        QarthLog.i(TAG, "start load patch. packageName : " + packageName + ", patchPath : " + path);
        QarthLog.i(TAG, "------------------------------------------------------");
        long start = System.currentTimeMillis();
        File pkgDir = new File(path);
        if (!pkgDir.exists()) {
            QarthLog.d(TAG, "patch path " + path + " not exist");
            return false;
        }
        PackageInfo info = null;
        if (context2 != null) {
            try {
                PackageManager pm = context.getPackageManager();
                if (pm != null && !Constants.COMMON_PATCH_PKG_NAME.equals(packageName) && !"systemserver".equals(packageName)) {
                    info = pm.getPackageInfo(packageName, 128);
                }
            } catch (PackageManager.NameNotFoundException e) {
                QarthLog.d(TAG, "packageName " + packageName + "not find in system");
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
        if (info != null) {
            fileFilter = new PatchFileFilter(info);
        }
        pkgDir.listFiles(fileFilter);
        List<PatchFile> patchFiles = fileFilter.getPatchFiles();
        String pkgDisPath = PACKAGE_DISABLE_PATCH_OVERLAY_ROOT + packageName;
        if (Constants.COMMON_PATCH_PKG_NAME.equals(packageName)) {
            pkgDisPath = "/data/hotpatch/fwkpatchdir/system/all";
        } else if (packageName.equals("systemserver")) {
            pkgDisPath = "/data/hotpatch/fwkpatchdir/system/systemserver";
        } else {
            QarthLog.d(TAG, "checkAndLoad single package: " + packageName);
        }
        File pkgDisDir = new File(pkgDisPath);
        if (info == null) {
            disFileFilter = new PatchDisFileFilter(packageName);
        } else {
            disFileFilter = new PatchDisFileFilter(info, packageName);
        }
        if (pkgDisDir.exists()) {
            pkgDisDir.listFiles(disFileFilter);
            List<PatchFile> disPatchFiles = disFileFilter.getDisPatchFiles();
            if (Constants.COMMON_PATCH_PKG_NAME.equals(packageName) && context2 != null) {
                if (!"android".equals(context.getPackageName()) && (context.getApplicationInfo().flags & 1) == 0) {
                    File pkgDisDirOwn = new File(PACKAGE_DISABLE_PATCH_OVERLAY_ROOT + context.getPackageName());
                    if (pkgDisDirOwn.exists()) {
                        if (info == null) {
                            disFileFilterOwn = new PatchDisFileFilter(packageName);
                        } else {
                            disFileFilterOwn = new PatchDisFileFilter(info, packageName);
                        }
                        pkgDisDirOwn.listFiles(disFileFilterOwn);
                        List<PatchFile> disPatchFilesOwn = disFileFilterOwn.getDisPatchFiles();
                        disPatchFiles.removeAll(disPatchFilesOwn);
                        disPatchFiles.addAll(disPatchFilesOwn);
                    }
                }
            }
            patchFiles.removeAll(disPatchFiles);
        }
        if (patchFiles.size() == 0) {
            QarthLog.e(TAG, "no patch file in " + path);
            return false;
        }
        if (Constants.DEBUG) {
            QarthLog.d(TAG, "\t==> elapse time 1.1: " + (System.currentTimeMillis() - start) + " ms");
        }
        if (Constants.DEBUG) {
            QarthLog.d(TAG, "patch files : " + patchFiles);
        }
        List<QarthContext> loadList2 = new ArrayList<>();
        for (PatchFile patchFile : patchFiles) {
            QarthContext qc = new QarthContext();
            qc.packageName = packageName;
            qc.context = context2;
            qc.cpuAbi = CommonUtil.determineCpuAbi(context);
            qc.qarthVersion = QarthVersion.QARTH_VERSION;
            qc.qarthClassLoader = loader;
            qc.patchFile = patchFile;
            qc.qarthFile = qc.patchFile.getFile();
            loadList2.add(qc);
            context2 = context;
        }
        if (Constants.DEBUG) {
            QarthLog.d(TAG, "\t==> elapse time 2: " + (System.currentTimeMillis() - start) + " ms");
        }
        for (QarthContext qc2 : loadList2) {
            if (loadedPatchPaths.contains(qc2.patchFile.getPath())) {
                loadList = loadList2;
                QarthLog.d(TAG, "patch file : " + qc2.patchFile.getPath() + ", repeated loading");
            } else if (!loadedPatchPaths.add(qc2.patchFile.getPath())) {
                QarthLog.e(TAG, "add patch [" + qc2.qarthFile.getName() + "] to set failed");
                return false;
            } else if (!PatchLoader.getInstance().load(qc2)) {
                String str = TAG;
                StringBuilder sb = new StringBuilder();
                loadList = loadList2;
                sb.append("load patch [");
                sb.append(qc2.qarthFile.getName());
                sb.append("] failed");
                QarthLog.e(str, sb.toString());
                loadedPatchPaths.remove(qc2.patchFile.getPath());
            } else {
                loadList = loadList2;
            }
            loadList2 = loadList;
        }
        if (Constants.DEBUG) {
            QarthLog.d(TAG, "\t==> elapse time 3: " + (System.currentTimeMillis() - start) + " ms");
        }
        return true;
    }

    public static boolean createDisableExceptionQarthFile(Throwable throwInfor) {
        QarthLog.i(TAG, "createDisableExceptionQarthFile");
        if (ActivityThread.currentApplication() == null || ActivityThread.currentApplication().getApplicationInfo() == null) {
            QarthLog.e(TAG, "current thread application info is null");
            return false;
        }
        ApplicationInfo appInfo = ActivityThread.currentApplication().getApplicationInfo();
        String packageName = appInfo.packageName;
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        if (appInfo.uid != 1000) {
            String str = TAG;
            QarthLog.i(str, "create disable file for " + packageName + " uid is " + appInfo.uid);
            return false;
        }
        QarthDisFileCreator qarthDisFileCreator = QarthDisFileCreator.getQarthDisFileCreator();
        if (qarthDisFileCreator != null) {
            return qarthDisFileCreator.disableExceptionQarthPatch(throwInfor, packageName);
        }
        return false;
    }

    public static void killCrashApplication() {
        QarthLog.i(TAG, "killCrashApplication");
        Process.killProcess(Process.myPid());
        System.exit(10);
    }

    public static void handleApplicationCrashForThirdParty(final String exceptionMessage, final String packageName) {
        if (TextUtils.isEmpty(exceptionMessage) || TextUtils.isEmpty(packageName)) {
            QarthLog.e(TAG, "the parameter is invalid to create disable file for third party");
            return;
        }
        final QarthDisFileCreator qarthDisFileCreator = QarthDisFileCreator.getQarthDisFileCreator();
        if (qarthDisFileCreator == null) {
            QarthLog.e(TAG, "the qarth creator is null");
        } else {
            new Thread(new Runnable() {
                /* class com.msic.qarth.PatchStore.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    QarthDisFileCreator.this.disableExceptionQarthPatch(new Throwable(exceptionMessage), packageName);
                }
            }, THREAD_PATCH_STORE).start();
        }
    }

    public static boolean isSystemApp(String packageName) {
        PackageManager packageManager;
        if (ActivityThread.currentApplication() == null || ActivityThread.currentApplication().getApplicationContext() == null || (packageManager = ActivityThread.currentApplication().getApplicationContext().getPackageManager()) == null) {
            return false;
        }
        try {
            if ((packageManager.getApplicationInfo(packageName, 0).flags & 1) != 0) {
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            String str = TAG;
            QarthLog.e(str, "package info is not found for " + packageName + " to judge is system app");
            return false;
        }
    }
}
