package com.android.server.pm;

import android.common.HwFrameworkFactory;
import android.common.HwFrameworkMonitor;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import com.android.internal.util.ArrayUtils;
import com.android.server.BatteryService;
import com.android.server.backup.UserBackupManagerService;
import huawei.android.bootanimation.IBootAnmation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public final class HwPackageManagerServiceUtils {
    private static final String ACTION_UPDATING_PACKAGES_STATUS = "com.huawei.intent.action.UPDATING_PACKAGES_STATUS";
    private static final boolean ANTIMAL_PROTECTION = "true".equalsIgnoreCase(SystemProperties.get("ro.product.antimal_protection", "true"));
    private static final String CHARACTERISTICS = SystemProperties.get("ro.build.characteristics", "");
    public static final boolean DEBUG_FLAG = SystemProperties.get("ro.dbg.pms_log", "0").equals("on");
    public static final int EVENT_APK_LOST_EXCEPTION = 907400026;
    public static final int EVENT_SETTINGS_EXCEPTION = 907400024;
    public static final int EVENT_UNINSTALLED_APPLICATION = 907400027;
    public static final int EVENT_UNINSTALLED_DELAPP_EXCEPTION = 907400025;
    private static final String FLAG_APK_NOSYS = "nosys";
    public static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final boolean IS_CHINA = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    private static final boolean IS_CHINA_AREA = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    private static final int MILLISECONDS_TO_SECONDS = 1000;
    private static final int MSG_LAN_ENGLISH = 256;
    private static final int MSG_OPER_DATA_UPDATE = 2;
    private static final int MSG_OPER_STOP = 1;
    private static final String[] NOT_ALLOWED_UNINSTALL_WHITELIST = {"com.huawei.android.launcher", SYSTEMMANAGER, UserBackupManagerService.SETTINGS_PACKAGE};
    private static final String PACKAGE_MIME_TYPE = "application/vnd.android.package-archive";
    private static final String PACKAGE_SCHEME = "package";
    private static final String[] PROTECT_APK_NOT_ALLOWED_UNINSTALL_WHITELIST = {"com.huawei.parentcontrol", "com.huawei.webview"};
    private static final String SECURITYPLUGINBASE = "com.huawei.securitypluginbase";
    private static final String[] SECURITYPLUGINBASE_PLUGINS = {"qihooplugin", "aviplugin", "avastplugin", "avlplugin"};
    private static final String SYSTEMMANAGER = "com.huawei.systemmanager";
    private static final String[] SYSTEMMANAGER_PLUGINS = {"antimalplugin", "aiprotection", "wifisecure"};
    private static final String TAG = "HwPackageManagerServiceUtils";
    private static IHwPackageManagerServiceEx mHwPMSEx;
    private static HashMap<String, HashSet<String>> sAutoInstallMap = null;
    private static HashMap<String, HashSet<String>> sCotaDelInstallMap = null;
    private static HashMap<String, HashSet<String>> sDelMultiInstallMap = null;
    private static IBootAnmation sIBootAnmation = null;
    private static boolean sIsConnectedBootAnimation = false;
    private static HwFrameworkMonitor sMonitor = HwFrameworkFactory.getHwFrameworkMonitor();

    public static boolean isDisableStatus(int state) {
        if (state == 2 || state == 3 || state == 4) {
            return true;
        }
        return false;
    }

    public static boolean isInAntiFillingWhiteList(String pkg, boolean isSupportHomeScreen) {
        if (Arrays.asList(PROTECT_APK_NOT_ALLOWED_UNINSTALL_WHITELIST).contains(pkg)) {
            return true;
        }
        if (!ANTIMAL_PROTECTION) {
            Slog.d(TAG, "AntimalProtection control is close!");
            return false;
        } else if (!isSupportHomeScreen) {
            return Arrays.asList(NOT_ALLOWED_UNINSTALL_WHITELIST).contains(pkg);
        } else {
            Slog.d(TAG, "Support home screen.");
            return false;
        }
    }

    public static boolean isNotAllowUninstallAndDisable(int userId, String packageName) {
        return Arrays.asList(PROTECT_APK_NOT_ALLOWED_UNINSTALL_WHITELIST).contains(packageName) && (userId == 0 || !mHwPMSEx.isInDelAppList(packageName));
    }

    public static boolean isForbidUninstallSplitPlugin(String packageName, String splitName) {
        if (SYSTEMMANAGER.equals(packageName) && ArrayUtils.contains(SYSTEMMANAGER_PLUGINS, splitName)) {
            return true;
        }
        if (!SECURITYPLUGINBASE.equals(packageName) || !ArrayUtils.contains(SECURITYPLUGINBASE_PLUGINS, splitName)) {
            return false;
        }
        return true;
    }

    public static void initHwPMSEx(IHwPackageManagerServiceEx hwPMSEx) {
        mHwPMSEx = hwPMSEx;
    }

    public static void reportException(int eventId, String message) {
        IHwPackageManagerServiceEx iHwPackageManagerServiceEx = mHwPMSEx;
        if (iHwPackageManagerServiceEx != null) {
            iHwPackageManagerServiceEx.reportEventStream(eventId, message);
        }
    }

    public static void updateFlagsForMarketSystemApp(PackageParser.Package pkg) {
        if (pkg != null && pkg.isUpdatedSystemApp() && pkg.mAppMetaData != null && pkg.mAppMetaData.getBoolean("android.huawei.MARKETED_SYSTEM_APP", false)) {
            Slog.i(TAG, "updateFlagsForMarketSystemApp" + pkg.packageName + " has MetaData HUAWEI_MARKETED_SYSTEM_APP, add FLAG_MARKETED_SYSTEM_APP");
            ApplicationInfo applicationInfo = pkg.applicationInfo;
            applicationInfo.hwFlags = applicationInfo.hwFlags | 536870912;
            if (pkg.mPersistentApp) {
                Slog.i(TAG, "updateFlagsForMarketSystemApp " + pkg.packageName + " is a persistent updated system app!");
                ApplicationInfo applicationInfo2 = pkg.applicationInfo;
                applicationInfo2.flags = applicationInfo2.flags | 8;
            }
        }
    }

    public static void setDelMultiInstallMap(HashMap<String, HashSet<String>> delMultiInstallMap) {
        if (delMultiInstallMap == null) {
            Slog.w(TAG, "DelMultiInstallMap is null!");
        }
        sDelMultiInstallMap = delMultiInstallMap;
    }

    public static void setCotaDelInstallMap(HashMap<String, HashSet<String>> cotaDelInstallMap) {
        if (cotaDelInstallMap == null) {
            Slog.w(TAG, "CotaDelInstallMap is null!");
        }
        sCotaDelInstallMap = cotaDelInstallMap;
    }

    public static void setAutoInstallMapForDelApps(HashMap<String, HashSet<String>> autoInstallMap) {
        if (autoInstallMap == null) {
            Slog.w(TAG, "autoInstallMap is null!");
        }
        sAutoInstallMap = autoInstallMap;
    }

    public static boolean isNoSystemPreApp(String codePath) {
        String path;
        if (TextUtils.isEmpty(codePath)) {
            Slog.w(TAG, "CodePath is null when check isNoSystemPreApp!");
            return false;
        }
        if (codePath.endsWith(".apk")) {
            path = getCustPackagePath(codePath);
        } else {
            path = codePath;
        }
        if (path == null) {
            return false;
        }
        HashMap<String, HashSet<String>> hashMap = sDelMultiInstallMap;
        boolean normalDelNoSysApp = hashMap != null && hashMap.get(FLAG_APK_NOSYS).contains(path);
        HashMap<String, HashSet<String>> hashMap2 = sCotaDelInstallMap;
        boolean cotaNoBootDelNoSysApp = hashMap2 != null && hashMap2.get(FLAG_APK_NOSYS).contains(path);
        HashMap<String, HashSet<String>> hashMap3 = sAutoInstallMap;
        boolean autoInstallNoSysApp = hashMap3 != null && hashMap3.get(FLAG_APK_NOSYS).contains(path);
        if (normalDelNoSysApp || cotaNoBootDelNoSysApp || autoInstallNoSysApp) {
            return true;
        }
        return false;
    }

    public static String getCustPackagePath(String codePath) {
        if (TextUtils.isEmpty(codePath)) {
            Slog.w(TAG, "CodePath is null when getCustPackagePath!");
            return null;
        }
        int lastIndex = codePath.lastIndexOf(47);
        if (lastIndex > 0) {
            return codePath.substring(0, lastIndex);
        }
        Log.e(TAG, "getCustPackagePath ERROR:  " + codePath);
        return null;
    }

    public static void addFlagsForRemovablePreApk(PackageParser.Package pkg, int hwFlags) {
        if ((hwFlags & DumpState.DUMP_APEX) != 0) {
            ApplicationInfo applicationInfo = pkg.applicationInfo;
            applicationInfo.hwFlags = 33554432 | applicationInfo.hwFlags;
        }
    }

    public static void addFlagsForUpdatedRemovablePreApk(PackageParser.Package pkg, int hwFlags) {
        if ((hwFlags & DumpState.DUMP_HANDLE) != 0) {
            ApplicationInfo applicationInfo = pkg.applicationInfo;
            applicationInfo.hwFlags = 67108864 | applicationInfo.hwFlags;
        }
    }

    public static boolean hwlocationIsVendor(String codePath) {
        if (!TextUtils.isEmpty(codePath)) {
            return codePath.startsWith("/data/hw_init/vendor/");
        }
        Slog.w(TAG, "CodePath is null when check is vendor!");
        return false;
    }

    public static boolean hwlocationIsProduct(String codePath) {
        if (!TextUtils.isEmpty(codePath)) {
            return codePath.startsWith("/data/hw_init/product/");
        }
        Slog.w(TAG, "CodePath is null when check is product!");
        return false;
    }

    public static long timingsBegin() {
        return SystemClock.uptimeMillis();
    }

    public static long timingsBeginWithTag(String tag, String msg) {
        Slog.i(tag, msg);
        return SystemClock.uptimeMillis();
    }

    public static void timingsEnd(String tag, String op, long begin) {
        Slog.i(tag, "TimerCounter **** " + op + " ************ Time to elapsed:" + (SystemClock.uptimeMillis() - begin) + " ms");
    }

    public static long hwTimingsBegin() {
        if (HWFLOW) {
            return SystemClock.uptimeMillis();
        }
        return 0;
    }

    public static long hwTimingsBeginWithTag(String tag, String msg) {
        if (HWFLOW) {
            return timingsBeginWithTag(tag, msg);
        }
        return 0;
    }

    public static void hwTimingsEnd(String tag, String op, long begin) {
        if (HWFLOW) {
            timingsEnd(tag, op, begin);
        }
    }

    public static boolean isSkipPreferredSetCheck(Intent intent) {
        boolean isSkip = true;
        boolean audioType = (intent.getAction() == null || !intent.getAction().equals("android.intent.action.VIEW") || intent.getData() == null || intent.getData().getScheme() == null || (!intent.getData().getScheme().equals("file") && !intent.getData().getScheme().equals("content")) || intent.getType() == null || !intent.getType().startsWith("audio/")) ? false : true;
        boolean hwSystemHome = intent.hasCategory("android.intent.category.HOME") && (intent.getFlags() & 512) != 0;
        if (!audioType && !hwSystemHome) {
            isSkip = false;
        }
        if (isSkip) {
            Slog.i(TAG, "skip preferred set check of preferred activity for " + intent.toString() + ", do not dropping preferred activity");
        }
        return isSkip;
    }

    public static boolean verifyValidVerifierInstall(String installerPackageName, String pkgName, int userId, int appId, PackageManagerService packageManagerService) {
        if (!SystemProperties.get("ro.config.hw_optb", "0").equals("156") || !pkgName.equals("com.android.vending") || ((!TextUtils.isEmpty(installerPackageName) && (installerPackageName.equals("com.android.packageinstaller") || installerPackageName.equals("com.huawei.appmarket"))) || !pkgName.equals("com.android.vending") || ((!TextUtils.isEmpty(installerPackageName) && installerPackageName.equals("com.android.vending")) || packageManagerService.checkPermission("android.permission.INSTALL_PACKAGES", installerPackageName, userId) == -1 || appId == 0 || appId == 2000 || appId == 1000))) {
            return true;
        }
        return false;
    }

    public static String isAppUpdateAllowed(PackageParser.Package oldPackage, PackageParser.Package pkg, PackageManagerService packageManagerService) {
        String pkgName = pkg.packageName;
        if (oldPackage.applicationInfo != null && oldPackage.applicationInfo.isSystemApp() && pkgName != null && !"com.android.vending".equals(pkgName) && !pkgName.equals(packageManagerService.mRequiredVerifierPackage) && !pkgName.equals(packageManagerService.mRequiredInstallerPackage) && !pkgName.equals(packageManagerService.mRequiredUninstallerPackage)) {
            Iterator it = pkg.activities.iterator();
            while (it.hasNext()) {
                Iterator it2 = ((PackageParser.Activity) it.next()).intents.iterator();
                while (true) {
                    if (it2.hasNext()) {
                        PackageParser.ActivityIntentInfo filter = (PackageParser.ActivityIntentInfo) it2.next();
                        boolean hasInstallAction = filter.matchAction("android.intent.action.INSTALL_PACKAGE");
                        boolean hasDefaultCategory = filter.hasCategory("android.intent.category.DEFAULT");
                        boolean hasPackageMimeType = filter.hasDataType(PACKAGE_MIME_TYPE);
                        if (hasInstallAction && hasDefaultCategory && hasPackageMimeType) {
                            return "Detect dangerous App with ACTION_INSTALL_PACKAGE, may cause system problem!";
                        }
                        boolean hasUninstallAction = filter.matchAction("android.intent.action.UNINSTALL_PACKAGE");
                        boolean hasPackageScheme = filter.hasDataScheme("package");
                        if (hasUninstallAction && hasDefaultCategory && hasPackageScheme) {
                            return "Detect dangerous App with ACTION_UNINSTALL_PACKAGE, may cause system problem!";
                        }
                    }
                }
            }
            Iterator it3 = pkg.receivers.iterator();
            while (it3.hasNext()) {
                Iterator it4 = ((PackageParser.Activity) it3.next()).intents.iterator();
                while (true) {
                    if (it4.hasNext()) {
                        PackageParser.ActivityIntentInfo filter2 = (PackageParser.ActivityIntentInfo) it4.next();
                        boolean hasVerifierAction = filter2.matchAction("android.intent.action.PACKAGE_NEEDS_VERIFICATION");
                        boolean hasPackageMimeType2 = filter2.hasDataType(PACKAGE_MIME_TYPE);
                        if (hasVerifierAction && hasPackageMimeType2) {
                            return "Detect dangerous App with ACTION_PACKAGE_NEEDS_VERIFICATION, may cause system problem!";
                        }
                    }
                }
            }
        }
        return "";
    }

    private static void connectBootAnimation() {
        IBinder binder = ServiceManager.getService("BootAnimationBinderServer");
        if (binder != null) {
            sIBootAnmation = IBootAnmation.Stub.asInterface(binder);
        } else {
            Slog.w(TAG, "BootAnimationBinderServer not found; can not display dexoat process!");
        }
    }

    public static void showBootAnimationMessage(int numberOfPackagesVisited, int numberOfPackagesToDexopt) {
        int i = 1;
        if (!sIsConnectedBootAnimation) {
            connectBootAnimation();
            sIsConnectedBootAnimation = true;
        }
        if (sIBootAnmation != null) {
            int inputData = 0;
            if (numberOfPackagesVisited != 1) {
                inputData = 0 | DumpState.DUMP_APEX;
            }
            int data = (numberOfPackagesVisited * 100) / numberOfPackagesToDexopt;
            if (data != 0) {
                i = data;
            }
            try {
                sIBootAnmation.notifyProcessing(inputData | (i << 16) | (IS_CHINA_AREA ? 0 : 256));
            } catch (RemoteException e) {
                Slog.w(TAG, "show boot dexoat process error," + e.getMessage());
            }
        }
    }

    public static void stopBootAnimationMessage(PackageManagerService packageManagerService) {
        packageManagerService.getPackageHandler().postDelayed(new Runnable() {
            /* class com.android.server.pm.HwPackageManagerServiceUtils.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                if (HwPackageManagerServiceUtils.sIBootAnmation != null) {
                    try {
                        HwPackageManagerServiceUtils.sIBootAnmation.notifyProcessing((int) DumpState.DUMP_SERVICE_PERMISSIONS);
                    } catch (RemoteException e) {
                        Slog.w(HwPackageManagerServiceUtils.TAG, "finish boot dexoat process error," + e.getMessage());
                    }
                }
                IBootAnmation unused = HwPackageManagerServiceUtils.sIBootAnmation = null;
            }
        }, 1000);
    }

    public static boolean isPreRemovableApp(PackageSetting ps) {
        return (ps == null || ps.pkg.applicationInfo == null || ((ps.pkg.applicationInfo.hwFlags & DumpState.DUMP_APEX) == 0 && (ps.pkg.applicationInfo.hwFlags & DumpState.DUMP_HANDLE) == 0)) ? false : true;
    }

    public static boolean isUnsupportedFeatrue(String name) {
        if (!IS_CHINA || !"android.software.home_screen".equals(name)) {
            return false;
        }
        if (!BatteryService.HealthServiceWrapper.INSTANCE_VENDOR.equals(CHARACTERISTICS) && !"tablet".equals(CHARACTERISTICS)) {
            return false;
        }
        Slog.w(TAG, "<feature> android.software.home_screen is disabled in china area");
        return true;
    }

    public static void reportPmsParseFileException(String fileName, String exceptionName, int userId, String areaName) {
        Bundle data = new Bundle();
        data.putString("FILE_NAME", fileName);
        data.putString("EXCEPTION_NAME", exceptionName);
        if (areaName != null) {
            data.putString("AREA_NAME", areaName);
        }
        if (userId > -1) {
            data.putInt("USER_ID", userId);
        }
        HwFrameworkMonitor hwFrameworkMonitor = sMonitor;
        if (hwFrameworkMonitor == null || !hwFrameworkMonitor.monitor(907400029, data)) {
            Slog.i(TAG, "upload bigdata fail for PMS parse file: " + fileName);
            return;
        }
        Slog.i(TAG, "upload bigdata success for PMS parse file: " + fileName);
    }

    public static void reportPmsInitException(String fileName, int rebootTimes, String exceptionName, String exceptionInfo) {
        Bundle data = new Bundle();
        data.putInt("REBOOT_TIMES", rebootTimes);
        data.putString("EXCEPTION_NAME", exceptionName);
        if (fileName != null) {
            data.putString("FILE_NAME", fileName);
        }
        if (exceptionInfo != null) {
            data.putString("EXCEPTION_INFO", exceptionInfo);
        }
        HwFrameworkMonitor hwFrameworkMonitor = sMonitor;
        if (hwFrameworkMonitor == null || !hwFrameworkMonitor.monitor(907400030, data)) {
            Slog.i(TAG, "upload bigdata fail for PMS Init Exception: " + exceptionName);
            return;
        }
        Slog.i(TAG, "upload bigdata success for PMS Init Exception: " + exceptionName);
    }

    public static void reportPmsDeleteDataApp(int packagesNum, int deleteNum, long elapsedRealtime) {
        Bundle data = new Bundle();
        data.putInt("PACKAGE_NUM", packagesNum);
        data.putInt("DELETE_NUM", deleteNum);
        data.putInt("ELAPSED_REALTIME", (int) (elapsedRealtime / 1000));
        HwFrameworkMonitor hwFrameworkMonitor = sMonitor;
        if (hwFrameworkMonitor == null || !hwFrameworkMonitor.monitor(907400031, data)) {
            Slog.i(TAG, "upload bigdata fail for PMS delete orphaned app: " + deleteNum);
            return;
        }
        Slog.i(TAG, "upload bigdata success for PMS delete orphaned app: " + deleteNum);
    }

    public static void reportPmsDeleteDataApp(int errorCode, String fileName, long elapsedRealtime) {
        Bundle data = new Bundle();
        data.putInt("ERROR_CODE", errorCode);
        data.putString("APP_FILE_NAME", fileName);
        data.putInt("ELAPSED_REALTIME", (int) (elapsedRealtime / 1000));
        HwFrameworkMonitor hwFrameworkMonitor = sMonitor;
        if (hwFrameworkMonitor == null || !hwFrameworkMonitor.monitor(907400031, data)) {
            Slog.i(TAG, "upload bigdata fail for PMS delete invalid userdata: " + fileName);
            return;
        }
        Slog.i(TAG, "upload bigdata success for PMS delete invalid userdata: " + fileName);
    }

    public static void reportPmsDeleteDataApp(int callingUid, String callingName, int deleteNum, long elapsedRealtime) {
        Bundle data = new Bundle();
        data.putInt("CALLING_UID", callingUid);
        data.putString("CALLING_NAME", callingName);
        data.putInt("DELETE_NUM", deleteNum);
        data.putInt("ELAPSED_REALTIME", (int) (elapsedRealtime / 1000));
        HwFrameworkMonitor hwFrameworkMonitor = sMonitor;
        if (hwFrameworkMonitor == null || !hwFrameworkMonitor.monitor(907400031, data)) {
            Slog.i(TAG, "upload bigdata fail for PMS frequently delete apps apps: " + deleteNum);
            return;
        }
        Slog.i(TAG, "upload bigdata success for PMS frequently delete apps: " + deleteNum);
    }

    public static void sendUpdateBroadcast(Handler callingHandler, int status, String updatePackageName, String intentPackageName, Context context) {
        Slog.i(TAG, "sendUpdateBroadCast updatePackageName = " + updatePackageName + ", status = " + status + " , intentPackageName = " + intentPackageName);
        if (callingHandler == null || updatePackageName == null || intentPackageName == null || context == null) {
            Slog.i(TAG, "don't send update broadcast because some param was null !");
            return;
        }
        Intent updateIntent = new Intent();
        updateIntent.setAction(ACTION_UPDATING_PACKAGES_STATUS);
        updateIntent.putExtra("status", status);
        updateIntent.putExtra("updatePackageName", updatePackageName);
        updateIntent.setPackage(intentPackageName);
        updateIntent.setFlags(67108864 | updateIntent.getFlags());
        callingHandler.post(new Runnable(context, updateIntent) {
            /* class com.android.server.pm.$$Lambda$HwPackageManagerServiceUtils$zVKqOwsFnQ9NvV8UJJTcpl4NwRo */
            private final /* synthetic */ Context f$0;
            private final /* synthetic */ Intent f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                HwPackageManagerServiceUtils.lambda$sendUpdateBroadcast$0(this.f$0, this.f$1);
            }
        });
    }
}
