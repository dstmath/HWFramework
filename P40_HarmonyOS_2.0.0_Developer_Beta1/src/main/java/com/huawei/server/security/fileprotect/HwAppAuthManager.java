package com.huawei.server.security.fileprotect;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.content.pm.ApplicationInfoEx;
import com.huawei.android.content.pm.PackageManagerExt;
import com.huawei.android.content.pm.PackageParserEx;
import com.huawei.android.content.pm.UserInfoExt;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.os.UserManagerExt;
import com.huawei.security.fileprotect.HwDataProtect;
import com.huawei.security.fileprotect.HwdpsPackageInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HwAppAuthManager extends DefaultHwAppAuthManager {
    private static final String HISTORY_PACKAGES = "hwdps_prot_his_packages";
    private static final String HISTORY_PKG_SEPERATOR = "#";
    private static final String HISTORY_VERSION = "hwdps_prot_his_version";
    private static final int HISTORY_VERSION_CURRENT = 1;
    private static final int HISTORY_VERSION_DEFAULT = 0;
    private static final boolean IS_FORBIDDEN_AREA = isForbiddenArea();
    private static final String META_DATA_POLICY_DEFAULT = "default";
    private static final String META_DATA_POLICY_KEY = "huawei.app.fileprotect";
    private static final String OPTA_FORBIDDEN_CHANNEL = "185";
    private static final String OPTA_FORBIDDEN_EXHIBIT = "819";
    private static final String OPTB_FORBIDDEN_AREA = "999";
    private static final int PACKAGE_QUERY_FLAGS = 786560;
    private static final String PROTECT_FILE_DIRECTORY = "/databases";
    private static final String TAG = "HwAppAuthManager";
    private final Set<String> mBootedPackages;
    private Context mContext;
    private final Map<String, String> mInstalledApp;
    private boolean mIsPMSReady;
    private final Set<String> mPackageHistory;

    private HwAppAuthManager() {
        this.mIsPMSReady = false;
        this.mBootedPackages = new HashSet();
        this.mPackageHistory = new HashSet();
        this.mInstalledApp = new HashMap();
    }

    public static HwAppAuthManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static boolean isForbiddenArea() {
        String opta = SystemPropertiesEx.get("ro.config.hw_opta", "0");
        String optb = SystemPropertiesEx.get("ro.config.hw_optb", "0");
        return (OPTA_FORBIDDEN_CHANNEL.equals(opta) && OPTB_FORBIDDEN_AREA.equals(optb)) || (OPTA_FORBIDDEN_EXHIBIT.equals(opta) && OPTB_FORBIDDEN_AREA.equals(optb));
    }

    private static class SingletonHolder {
        private static final HwAppAuthManager INSTANCE = (IS_INIT_SUCCESS ? new HwAppAuthManager() : new HwDpsDisabledManager());
        private static final boolean IS_FEATURE_SUPPORTED = HwDataProtect.isFeatureSupported();
        private static final boolean IS_INIT_SUCCESS = (IS_FEATURE_SUPPORTED && HwDataProtect.prepare());

        private SingletonHolder() {
        }

        static {
            if (IS_INIT_SUCCESS) {
                ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
                singleThreadExecutor.execute(new Runnable() {
                    /* class com.huawei.server.security.fileprotect.HwAppAuthManager.SingletonHolder.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        long startTime = System.nanoTime();
                        HwDataProtect.initTee();
                        Log.i(HwAppAuthManager.TAG, "PerformanceTag(initTee:" + (System.nanoTime() - startTime) + ")");
                    }
                });
                singleThreadExecutor.shutdown();
            }
        }
    }

    public void checkFileProtect(PackageParserEx.Package pkg) {
        if (pkg != null && pkg.getAppMetaData() != null) {
            synchronized (this.mBootedPackages) {
                if (!this.mIsPMSReady) {
                    if (META_DATA_POLICY_DEFAULT.equals(pkg.getAppMetaData().getString(META_DATA_POLICY_KEY))) {
                        Log.i(TAG, "Default policy is supported for : " + pkg.getPackageName());
                        this.mBootedPackages.add(pkg.getPackageName());
                    }
                }
            }
        }
    }

    public void notifyPMSReady(Context context) {
        List<String> bootedPackages;
        long startTime = System.nanoTime();
        if (context == null) {
            Log.e(TAG, "PMS context is null!");
            return;
        }
        this.mContext = context;
        synchronized (this.mBootedPackages) {
            if (this.mIsPMSReady) {
                Log.i(TAG, "notifyPMSReady already!");
                return;
            }
            bootedPackages = new ArrayList<>(this.mBootedPackages);
            this.mBootedPackages.clear();
            this.mIsPMSReady = true;
        }
        loadHistoryPackages();
        Set<String> syncPkgs = syncToHistory(bootedPackages);
        List<HwdpsPackageInfo> protectedPkgInfos = new ArrayList<>(syncPkgs.size());
        for (String packageName : syncPkgs) {
            HwdpsPackageInfo packageInfo = getHwdpsPackageInfoByPkg(packageName);
            if (packageInfo != null) {
                protectedPkgInfos.add(packageInfo);
                this.mInstalledApp.put(packageName, packageInfo.getProtectPolicys());
            }
        }
        if (protectedPkgInfos.isEmpty()) {
            Log.d(TAG, "No App is protected");
            return;
        }
        HwDataProtect.syncInstalledPackages((HwdpsPackageInfo[]) protectedPkgInfos.toArray(new HwdpsPackageInfo[0]));
        Log.i(TAG, "PerformanceTag(notifyPMSReady:" + (System.nanoTime() - startTime) + ")");
    }

    public void preSendPackageBroadcast(String action, String packageName, String targetPkg) {
        if (this.mContext == null) {
            Log.e(TAG, "PMS is null, invoke notifyPMSReady first!");
        } else if (!isActionConcerned(action) || TextUtils.isEmpty(packageName)) {
        } else {
            if (!TextUtils.isEmpty(targetPkg)) {
                Log.d(TAG, "Not target for all, ignore it");
            } else if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                installPackage(packageName);
            } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                uninstallPackage(packageName);
            } else {
                Log.w(TAG, "You will never reach here, if so, check the code!");
            }
        }
    }

    private void loadHistoryPackages() {
        ContentResolver contentResolver;
        String[] pkgs;
        Context context = this.mContext;
        if (context != null && (contentResolver = context.getContentResolver()) != null && Settings.Secure.getInt(contentResolver, HISTORY_VERSION, 0) == 1) {
            String packageHistory = Settings.Secure.getString(contentResolver, HISTORY_PACKAGES);
            if (!TextUtils.isEmpty(packageHistory) && (pkgs = packageHistory.split(HISTORY_PKG_SEPERATOR)) != null && pkgs.length >= 1) {
                synchronized (this.mPackageHistory) {
                    this.mPackageHistory.addAll(Arrays.asList(pkgs));
                }
            }
        }
    }

    private void saveHistoryPackages() {
        ContentResolver contentResolver;
        Context context = this.mContext;
        if (context != null && (contentResolver = context.getContentResolver()) != null) {
            List<String> pkgList = new ArrayList<>();
            synchronized (this.mPackageHistory) {
                pkgList.addAll(this.mPackageHistory);
            }
            boolean hasPackage = false;
            StringBuilder packagesBuilder = new StringBuilder();
            int pkgListSize = pkgList.size();
            for (int i = 0; i < pkgListSize; i++) {
                String pkg = pkgList.get(i);
                if (!TextUtils.isEmpty(pkg)) {
                    if (hasPackage) {
                        packagesBuilder.append(HISTORY_PKG_SEPERATOR);
                    } else {
                        hasPackage = true;
                    }
                    packagesBuilder.append(pkg);
                }
            }
            if (!Settings.Secure.putInt(contentResolver, HISTORY_VERSION, 1)) {
                Log.e(TAG, "save version failed for : 1");
            } else if (!Settings.Secure.putString(contentResolver, HISTORY_PACKAGES, packagesBuilder.toString())) {
                Log.e(TAG, "save settings failed!");
            }
        }
    }

    private boolean isInHistory(String pkgName) {
        boolean contains;
        synchronized (this.mPackageHistory) {
            contains = this.mPackageHistory.contains(pkgName);
        }
        return contains;
    }

    private void addToHistory(String pkgName) {
        boolean addSuccess;
        synchronized (this.mPackageHistory) {
            addSuccess = this.mPackageHistory.add(pkgName);
        }
        if (addSuccess) {
            saveHistoryPackages();
        }
    }

    private Set<String> syncToHistory(List<String> syncPkgs) {
        Set<String> result = new HashSet<>();
        boolean syncSuccess = false;
        synchronized (this.mPackageHistory) {
            if (!IS_FORBIDDEN_AREA) {
                syncSuccess = this.mPackageHistory.addAll(syncPkgs);
            }
            result.addAll(this.mPackageHistory);
        }
        if (syncSuccess) {
            saveHistoryPackages();
        }
        return result;
    }

    private void removeFromHistory(String pkgName) {
        boolean removeSuccess;
        synchronized (this.mPackageHistory) {
            removeSuccess = this.mPackageHistory.remove(pkgName);
        }
        if (removeSuccess) {
            saveHistoryPackages();
        }
    }

    private boolean isActionConcerned(String action) {
        return !TextUtils.isEmpty(action) && ("android.intent.action.PACKAGE_ADDED".equals(action) || "android.intent.action.PACKAGE_REMOVED".equals(action));
    }

    private void installPackage(String packageName) {
        long startTime = System.nanoTime();
        if (!IS_FORBIDDEN_AREA && !isInHistory(packageName)) {
            HwdpsPackageInfo packageInfo = getHwdpsPackageInfoByPkg(packageName);
            if (packageInfo == null) {
                Log.w(TAG, "Cannot find pacakge : " + packageName);
                return;
            }
            HwDataProtect.installPackage(packageInfo);
            this.mInstalledApp.put(packageName, packageInfo.getProtectPolicys());
            Log.i(TAG, "hwdps installed package is " + packageName + " " + this.mInstalledApp.get(packageName));
            addToHistory(packageName);
            Log.i(TAG, "PerformanceTag(installPackage:" + (System.nanoTime() - startTime) + ")");
        }
    }

    private void uninstallPackage(String packageName) {
        long startTime = System.nanoTime();
        if (isInHistory(packageName)) {
            if (getHwdpsPackageInfoByPkg(packageName) != null) {
                Log.i(TAG, "Package : " + packageName + ", still exists in other user");
                return;
            }
            HwDataProtect.uninstallPackage(new HwdpsPackageInfo(0, this.mInstalledApp.get(packageName)));
            removeFromHistory(packageName);
            Log.i(TAG, "hwdps uninstalled package is " + packageName + " " + this.mInstalledApp.get(packageName));
            this.mInstalledApp.remove(packageName);
            Log.i(TAG, "PerformanceTag(uninstallPackage:" + (System.nanoTime() - startTime) + ")");
        }
    }

    private HwdpsPackageInfo getHwdpsPackageInfoByPkg(String packageName) {
        ApplicationInfo appInfo;
        System.nanoTime();
        PackageInfo pkgInfo = getPackageInfoAnyUser(packageName);
        if (pkgInfo == null || (appInfo = pkgInfo.applicationInfo) == null) {
            return null;
        }
        if (!(shouldProtect(appInfo) || isInHistory(packageName))) {
            return null;
        }
        int appId = UserHandleEx.getAppId(appInfo.uid);
        HwdpsPackageInfo.Builder builder = new HwdpsPackageInfo.Builder();
        HwdpsPackageInfo.Builder appId2 = builder.setAppId(appId);
        appId2.addDefaultPolicy(ApplicationInfoEx.getCredentialProtectedDataDir(appInfo) + PROTECT_FILE_DIRECTORY);
        return builder.build();
    }

    private boolean shouldProtect(ApplicationInfo appInfo) {
        Bundle metaData;
        if ((appInfo.flags & 1) == 0 || (metaData = appInfo.metaData) == null) {
            return false;
        }
        if (META_DATA_POLICY_DEFAULT.equals(metaData.getString(META_DATA_POLICY_KEY))) {
            return true;
        }
        Log.w(TAG, "For now, only default is supported");
        return false;
    }

    private PackageInfo getPackageInfoAnyUser(String packageName) {
        if (this.mContext == null || TextUtils.isEmpty(packageName)) {
            return null;
        }
        PackageManager packageManager = this.mContext.getPackageManager();
        if (packageManager == null) {
            Log.w(TAG, "PackageManager is null!");
            return null;
        }
        PackageInfo pkgInfo = null;
        for (UserInfoExt user : UserManagerExt.getUsers(UserManagerExt.get(this.mContext), false)) {
            try {
                pkgInfo = PackageManagerExt.getPackageInfoAsUser(packageManager, packageName, (int) PACKAGE_QUERY_FLAGS, user.getUserId());
                if (pkgInfo != null) {
                    break;
                }
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        return pkgInfo;
    }

    private static final class HwDpsDisabledManager extends HwAppAuthManager {
        private HwDpsDisabledManager() {
            super();
        }

        @Override // com.huawei.server.security.fileprotect.HwAppAuthManager
        public void checkFileProtect(PackageParserEx.Package pkg) {
        }

        @Override // com.huawei.server.security.fileprotect.HwAppAuthManager
        public void notifyPMSReady(Context context) {
        }

        @Override // com.huawei.server.security.fileprotect.HwAppAuthManager
        public void preSendPackageBroadcast(String action, String packageName, String targetPkg) {
        }
    }
}
