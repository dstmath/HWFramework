package huawei.com.android.server.security.fileprotect;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.os.Binder;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.pm.UserManagerService;
import com.huawei.android.os.SystemPropertiesEx;
import huawei.android.security.fileprotect.HwAppAuth;
import huawei.android.security.fileprotect.HwaaPackageInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HwAppAuthManager {
    private static final String HISTORY_PACKAGES = "hwaa_prot_his_packages";
    private static final String HISTORY_PKG_SEPERATOR = "#";
    private static final String HISTORY_VERSION = "hwaa_prot_his_version";
    private static final int HISTORY_VERSION_CURRENT = 1;
    private static final int HISTORY_VERSION_DEFAULT = 0;
    private static final boolean IS_FORBIDDEN_AREA = isForbiddenArea();
    private static final String META_DATA_POLICY_DEFAULT = "default";
    private static final String META_DATA_POLICY_KEY = "huawei.app.fileprotect";
    private static final String OPTA_FORBIDDEN_CHANNEL = "185";
    private static final String OPTA_FORBIDDEN_EXHIBIT = "819";
    private static final String OPTB_FORBIDDEN_AREA = "999";
    private static final int PACKAGE_QUERY_FLAGS = 786560;
    private static final String TAG = "HwAppAuthManager";
    private final Set<String> mBootedPackages;
    private Context mContext;
    private boolean mIsPMSReady;
    private final Set<String> mPackageHistory;

    private static final class HwaaDisabledManager extends HwAppAuthManager {
        private HwaaDisabledManager() {
            super();
        }

        public void checkFileProtect(PackageParser.Package pkg) {
        }

        public void notifyPMSReady(Context context) {
        }

        public void preSendPackageBroadcast(String action, String packageName, String targetPkg) {
        }
    }

    private static class SingletonHolder {
        /* access modifiers changed from: private */
        public static final HwAppAuthManager INSTANCE = (IS_INIT_SUCCESS ? new HwAppAuthManager() : new HwaaDisabledManager());
        private static final boolean IS_FEATURE_SUPPORTED = HwAppAuth.isFeatureSupported();
        private static final boolean IS_INIT_SUCCESS = (IS_FEATURE_SUPPORTED && HwAppAuth.prepare());

        private SingletonHolder() {
        }

        static {
            if (IS_INIT_SUCCESS) {
                ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
                singleThreadExecutor.execute($$Lambda$HwAppAuthManager$SingletonHolder$kJEOD9ngRlEvXxAZshwZ8xChaE.INSTANCE);
                singleThreadExecutor.shutdown();
            }
        }

        static /* synthetic */ void lambda$static$0() {
            long startTime = System.nanoTime();
            HwAppAuth.initTee();
            Slog.i(HwAppAuthManager.TAG, "PerformanceTag(initTee:" + (System.nanoTime() - startTime) + ")");
        }
    }

    private HwAppAuthManager() {
        this.mIsPMSReady = false;
        this.mBootedPackages = new HashSet();
        this.mPackageHistory = new HashSet();
    }

    public static HwAppAuthManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static boolean isForbiddenArea() {
        String opta = SystemPropertiesEx.get("ro.config.hw_opta", "0");
        String optb = SystemPropertiesEx.get("ro.config.hw_optb", "0");
        boolean isArea1 = OPTA_FORBIDDEN_CHANNEL.equals(opta) && OPTB_FORBIDDEN_AREA.equals(optb);
        boolean isArea2 = OPTA_FORBIDDEN_EXHIBIT.equals(opta) && OPTB_FORBIDDEN_AREA.equals(optb);
        if (isArea1 || isArea2) {
            return true;
        }
        return false;
    }

    public void checkFileProtect(PackageParser.Package pkg) {
        if (pkg != null && pkg.mAppMetaData != null) {
            synchronized (this.mBootedPackages) {
                if (!this.mIsPMSReady) {
                    if ("default".equals(pkg.mAppMetaData.getString(META_DATA_POLICY_KEY))) {
                        Slog.i(TAG, "Default policy is supported for : " + pkg.packageName);
                        this.mBootedPackages.add(pkg.packageName);
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0030, code lost:
        loadHistoryPackages();
        r2 = syncToHistory(r3);
        r4 = new java.util.ArrayList<>(r2.size());
        r5 = r2.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0048, code lost:
        if (r5.hasNext() == false) goto L_0x005a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004a, code lost:
        r7 = getHwaaPackageInfoByPkg(r5.next());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0054, code lost:
        if (r7 == null) goto L_0x0044;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0056, code lost:
        r4.add(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x005e, code lost:
        if (r4.isEmpty() == false) goto L_0x0068;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0060, code lost:
        android.util.Slog.d(TAG, "No App is protected");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0067, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0068, code lost:
        huawei.android.security.fileprotect.HwAppAuth.syncInstalledPackages((huawei.android.security.fileprotect.HwaaPackageInfo[]) r4.toArray(new huawei.android.security.fileprotect.HwaaPackageInfo[0]));
        android.util.Slog.i(TAG, "PerformanceTag(notifyPMSReady:" + (java.lang.System.nanoTime() - r0) + ")");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0094, code lost:
        return;
     */
    public void notifyPMSReady(Context context) {
        long startTime = System.nanoTime();
        if (context == null) {
            Slog.e(TAG, "PMS context is null!");
            return;
        }
        this.mContext = context;
        synchronized (this.mBootedPackages) {
            if (this.mIsPMSReady) {
                Slog.i(TAG, "notifyPMSReady already!");
                return;
            }
            List<String> bootedPackages = new ArrayList<>(this.mBootedPackages);
            this.mBootedPackages.clear();
            this.mIsPMSReady = true;
        }
    }

    public void preSendPackageBroadcast(String action, String packageName, String targetPkg) {
        if (this.mContext == null) {
            Slog.e(TAG, "PMS is null, invoke notifyPMSReady first!");
        } else if (!isActionConcerned(action) || TextUtils.isEmpty(packageName)) {
        } else {
            if (!TextUtils.isEmpty(targetPkg)) {
                Slog.d(TAG, "Not target for all, ignore it");
                return;
            }
            if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                installPackage(packageName);
            } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                uninstallPackage(packageName);
            } else {
                Slog.w(TAG, "You will never reach here, if so, check the code!");
            }
        }
    }

    private void loadHistoryPackages() {
        if (this.mContext != null) {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            if (contentResolver != null && Settings.Secure.getInt(contentResolver, HISTORY_VERSION, 0) == 1) {
                String packageHistory = Settings.Secure.getString(contentResolver, HISTORY_PACKAGES);
                if (!TextUtils.isEmpty(packageHistory)) {
                    String[] pkgs = packageHistory.split("#");
                    if (pkgs != null && pkgs.length >= 1) {
                        synchronized (this.mPackageHistory) {
                            this.mPackageHistory.addAll(Arrays.asList(pkgs));
                        }
                    }
                }
            }
        }
    }

    private void saveHistoryPackages() {
        if (this.mContext != null) {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            if (contentResolver != null) {
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
                            packagesBuilder.append("#");
                        } else {
                            hasPackage = true;
                        }
                        packagesBuilder.append(pkg);
                    }
                }
                long identity = Binder.clearCallingIdentity();
                saveToSettingsSecure(contentResolver, 1, packagesBuilder.toString());
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    private void saveToSettingsSecure(ContentResolver contentResolver, int currentVersion, String packages) {
        if (!Settings.Secure.putInt(contentResolver, HISTORY_VERSION, currentVersion)) {
            Slog.e(TAG, "save version failed for : " + currentVersion);
            return;
        }
        if (!Settings.Secure.putString(contentResolver, HISTORY_PACKAGES, packages)) {
            Slog.e(TAG, "save settings failed!");
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
            HwaaPackageInfo packageInfo = getHwaaPackageInfoByPkg(packageName);
            if (packageInfo == null) {
                Slog.w(TAG, "Cannot find pacakge : " + packageName);
                return;
            }
            HwAppAuth.installPackage(packageInfo);
            addToHistory(packageName);
            Slog.i(TAG, "PerformanceTag(installPackage:" + (System.nanoTime() - startTime) + ")");
        }
    }

    private void uninstallPackage(String packageName) {
        long startTime = System.nanoTime();
        if (isInHistory(packageName)) {
            if (getHwaaPackageInfoByPkg(packageName) != null) {
                Slog.i(TAG, "Package : " + packageName + ", still exists in other user");
                return;
            }
            HwaaPackageInfo.Builder builder = new HwaaPackageInfo.Builder();
            builder.setPackageName(packageName);
            HwAppAuth.uninstallPackage(builder.build());
            removeFromHistory(packageName);
            Slog.i(TAG, "PerformanceTag(uninstallPackage:" + (System.nanoTime() - startTime) + ")");
        }
    }

    private HwaaPackageInfo getHwaaPackageInfoByPkg(String packageName) {
        long nanoTime = System.nanoTime();
        PackageInfo pkgInfo = getPackageInfoAnyUser(packageName);
        if (pkgInfo == null) {
            return null;
        }
        ApplicationInfo appInfo = pkgInfo.applicationInfo;
        if (appInfo == null) {
            return null;
        }
        if (!(shouldProtect(appInfo) || isInHistory(packageName))) {
            return null;
        }
        int appId = UserHandle.getAppId(appInfo.uid);
        HwaaPackageInfo.Builder builder = new HwaaPackageInfo.Builder();
        builder.setAppId(appId).setPackageName(packageName).setSharedUserId(pkgInfo.sharedUserId).addDefaultPolicy(appInfo.credentialProtectedDataDir);
        return builder.build();
    }

    private boolean shouldProtect(ApplicationInfo appInfo) {
        if ((appInfo.flags & 1) == 0) {
            return false;
        }
        Bundle metaData = appInfo.metaData;
        if (metaData == null) {
            return false;
        }
        if ("default".equals(metaData.getString(META_DATA_POLICY_KEY))) {
            return true;
        }
        Slog.w(TAG, "For now, only default is supported");
        return false;
    }

    private PackageInfo getPackageInfoAnyUser(String packageName) {
        if (this.mContext == null || TextUtils.isEmpty(packageName)) {
            return null;
        }
        PackageManager packageManager = this.mContext.getPackageManager();
        if (packageManager == null) {
            Slog.w(TAG, "PackageManager is null!");
            return null;
        }
        PackageInfo pkgInfo = null;
        int[] userIds = UserManagerService.getInstance().getUserIds();
        int length = userIds.length;
        int i = 0;
        while (i < length) {
            try {
                pkgInfo = packageManager.getPackageInfoAsUser(packageName, PACKAGE_QUERY_FLAGS, userIds[i]);
                if (pkgInfo != null) {
                    break;
                }
                i++;
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        return pkgInfo;
    }
}
