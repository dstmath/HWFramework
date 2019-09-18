package com.android.server.pm.dex;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageParser;
import android.database.ContentObserver;
import android.os.Build;
import android.os.FileUtils;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Slog;
import android.util.jar.StrictJarFile;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.ArrayUtils;
import com.android.server.pm.Installer;
import com.android.server.pm.InstructionSets;
import com.android.server.pm.PackageDexOptimizer;
import com.android.server.pm.PackageManagerServiceUtils;
import com.android.server.pm.dex.PackageDexUsage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;

public class DexManager {
    private static final boolean DEBUG = false;
    private static final PackageDexUsage.PackageUseInfo DEFAULT_USE_INFO = new PackageDexUsage.PackageUseInfo();
    /* access modifiers changed from: private */
    public static int DEX_SEARCH_FOUND_PRIMARY = 1;
    /* access modifiers changed from: private */
    public static int DEX_SEARCH_FOUND_SECONDARY = 3;
    /* access modifiers changed from: private */
    public static int DEX_SEARCH_FOUND_SPLIT = 2;
    /* access modifiers changed from: private */
    public static int DEX_SEARCH_NOT_FOUND = 0;
    private static final String PROPERTY_NAME_PM_DEXOPT_PRIV_APPS_OOB = "pm.dexopt.priv-apps-oob";
    private static final String PROPERTY_NAME_PM_DEXOPT_PRIV_APPS_OOB_LIST = "pm.dexopt.priv-apps-oob-list";
    private static final String TAG = "DexManager";
    private final Context mContext;
    private final Object mInstallLock;
    @GuardedBy("mInstallLock")
    private final Installer mInstaller;
    private final Listener mListener;
    @GuardedBy("mPackageCodeLocationsCache")
    private final Map<String, PackageCodeLocations> mPackageCodeLocationsCache = new HashMap();
    private final PackageDexOptimizer mPackageDexOptimizer;
    private final PackageDexUsage mPackageDexUsage = new PackageDexUsage();
    private final IPackageManager mPackageManager;

    private class DexSearchResult {
        /* access modifiers changed from: private */
        public int mOutcome;
        /* access modifiers changed from: private */
        public String mOwningPackageName;

        public DexSearchResult(String owningPackageName, int outcome) {
            this.mOwningPackageName = owningPackageName;
            this.mOutcome = outcome;
        }

        public String toString() {
            return this.mOwningPackageName + "-" + this.mOutcome;
        }
    }

    public interface Listener {
        void onReconcileSecondaryDexFile(ApplicationInfo applicationInfo, PackageDexUsage.DexUseInfo dexUseInfo, String str, int i);
    }

    private static class PackageCodeLocations {
        private final Map<Integer, Set<String>> mAppDataDirs;
        private String mBaseCodePath;
        /* access modifiers changed from: private */
        public final String mPackageName;
        private final Set<String> mSplitCodePaths;

        public PackageCodeLocations(ApplicationInfo ai, int userId) {
            this(ai.packageName, ai.sourceDir, ai.splitSourceDirs);
            mergeAppDataDirs(ai.dataDir, userId);
        }

        public PackageCodeLocations(String packageName, String baseCodePath, String[] splitCodePaths) {
            this.mPackageName = packageName;
            this.mSplitCodePaths = new HashSet();
            this.mAppDataDirs = new HashMap();
            updateCodeLocation(baseCodePath, splitCodePaths);
        }

        public void updateCodeLocation(String baseCodePath, String[] splitCodePaths) {
            this.mBaseCodePath = baseCodePath;
            this.mSplitCodePaths.clear();
            if (splitCodePaths != null) {
                for (String split : splitCodePaths) {
                    this.mSplitCodePaths.add(split);
                }
            }
        }

        public void mergeAppDataDirs(String dataDir, int userId) {
            ((Set) DexManager.putIfAbsent(this.mAppDataDirs, Integer.valueOf(userId), new HashSet())).add(dataDir);
        }

        public int searchDex(String dexPath, int userId) {
            Set<String> userDataDirs = this.mAppDataDirs.get(Integer.valueOf(userId));
            if (userDataDirs == null) {
                return DexManager.DEX_SEARCH_NOT_FOUND;
            }
            if (this.mBaseCodePath.equals(dexPath)) {
                return DexManager.DEX_SEARCH_FOUND_PRIMARY;
            }
            if (this.mSplitCodePaths.contains(dexPath)) {
                return DexManager.DEX_SEARCH_FOUND_SPLIT;
            }
            for (String dataDir : userDataDirs) {
                if (dexPath.startsWith(dataDir)) {
                    return DexManager.DEX_SEARCH_FOUND_SECONDARY;
                }
            }
            return DexManager.DEX_SEARCH_NOT_FOUND;
        }
    }

    public static class RegisterDexModuleResult {
        public final String message;
        public final boolean success;

        public RegisterDexModuleResult() {
            this(false, null);
        }

        public RegisterDexModuleResult(boolean success2, String message2) {
            this.success = success2;
            this.message = message2;
        }
    }

    public DexManager(Context context, IPackageManager pms, PackageDexOptimizer pdo, Installer installer, Object installLock, Listener listener) {
        this.mContext = context;
        this.mPackageManager = pms;
        this.mPackageDexOptimizer = pdo;
        this.mInstaller = installer;
        this.mInstallLock = installLock;
        this.mListener = listener;
    }

    public void systemReady() {
        registerSettingObserver();
    }

    public void notifyDexLoad(ApplicationInfo loadingAppInfo, List<String> classLoadersNames, List<String> classPaths, String loaderIsa, int loaderUserId) {
        try {
            notifyDexLoadInternal(loadingAppInfo, classLoadersNames, classPaths, loaderIsa, loaderUserId);
        } catch (Exception e) {
            Slog.w(TAG, "Exception while notifying dex load for package " + loadingAppInfo.packageName, e);
        }
    }

    private void notifyDexLoadInternal(ApplicationInfo loadingAppInfo, List<String> classLoaderNames, List<String> classPaths, String loaderIsa, int loaderUserId) {
        int i;
        int i2;
        String classLoaderContext;
        ApplicationInfo applicationInfo = loadingAppInfo;
        List<String> list = classPaths;
        if (classLoaderNames.size() != classPaths.size()) {
            Slog.wtf(TAG, "Bad call to noitfyDexLoad: args have different size");
        } else if (classLoaderNames.isEmpty()) {
            Slog.wtf(TAG, "Bad call to notifyDexLoad: class loaders list is empty");
        } else if (!PackageManagerServiceUtils.checkISA(loaderIsa)) {
            Slog.w(TAG, "Loading dex files " + list + " in unsupported ISA: " + loaderIsa + "?");
        } else {
            String str = loaderIsa;
            String[] dexPathsToRegister = list.get(0).split(File.pathSeparator);
            String[] classLoaderContexts = DexoptUtils.processContextForDexLoad(classLoaderNames, classPaths);
            int length = dexPathsToRegister.length;
            int i3 = 0;
            int dexPathIndex = 0;
            while (i3 < length) {
                String dexPath = dexPathsToRegister[i3];
                int i4 = loaderUserId;
                DexSearchResult searchResult = getDexPackage(applicationInfo, dexPath, i4);
                if (searchResult.mOutcome != DEX_SEARCH_NOT_FOUND) {
                    boolean z = true;
                    boolean isUsedByOtherApps = !applicationInfo.packageName.equals(searchResult.mOwningPackageName);
                    if (!(searchResult.mOutcome == DEX_SEARCH_FOUND_PRIMARY || searchResult.mOutcome == DEX_SEARCH_FOUND_SPLIT)) {
                        z = false;
                    }
                    boolean primaryOrSplit = z;
                    if (!primaryOrSplit || isUsedByOtherApps) {
                        if (classLoaderContexts == null) {
                            classLoaderContext = "=UnsupportedClassLoaderContext=";
                        } else {
                            classLoaderContext = classLoaderContexts[dexPathIndex];
                        }
                        DexSearchResult dexSearchResult = searchResult;
                        String str2 = dexPath;
                        i2 = i3;
                        i = length;
                        if (this.mPackageDexUsage.record(searchResult.mOwningPackageName, dexPath, i4, str, isUsedByOtherApps, primaryOrSplit, applicationInfo.packageName, classLoaderContext)) {
                            this.mPackageDexUsage.maybeWriteAsync();
                        }
                    } else {
                        i2 = i3;
                        i = length;
                        i3 = i2 + 1;
                        length = i;
                    }
                } else {
                    String str3 = dexPath;
                    i2 = i3;
                    i = length;
                }
                dexPathIndex++;
                i3 = i2 + 1;
                length = i;
            }
        }
    }

    public void load(Map<Integer, List<PackageInfo>> existingPackages) {
        try {
            loadInternal(existingPackages);
        } catch (Exception e) {
            this.mPackageDexUsage.clear();
            Slog.w(TAG, "Exception while loading package dex usage. Starting with a fresh state.", e);
        }
    }

    public void notifyPackageInstalled(PackageInfo pi, int userId) {
        if (userId == -1) {
            throw new IllegalArgumentException("notifyPackageInstalled called with USER_ALL");
        } else if (pi == null) {
            Slog.i(TAG, "notifyPackageInstalled-> pi is null! return directly");
        } else {
            cachePackageInfo(pi, userId);
        }
    }

    public void notifyPackageUpdated(String packageName, String baseCodePath, String[] splitCodePaths) {
        cachePackageCodeLocation(packageName, baseCodePath, splitCodePaths, null, -1);
        if (this.mPackageDexUsage.clearUsedByOtherApps(packageName)) {
            this.mPackageDexUsage.maybeWriteAsync();
        }
    }

    public void notifyPackageDataDestroyed(String packageName, int userId) {
        boolean updated;
        if (userId == -1) {
            updated = this.mPackageDexUsage.removePackage(packageName);
        } else {
            updated = this.mPackageDexUsage.removeUserPackage(packageName, userId);
        }
        if (updated) {
            this.mPackageDexUsage.maybeWriteAsync();
        }
    }

    private void cachePackageInfo(PackageInfo pi, int userId) {
        ApplicationInfo ai = pi.applicationInfo;
        cachePackageCodeLocation(pi.packageName, ai.sourceDir, ai.splitSourceDirs, new String[]{ai.dataDir, ai.deviceProtectedDataDir, ai.credentialProtectedDataDir}, userId);
    }

    private void cachePackageCodeLocation(String packageName, String baseCodePath, String[] splitCodePaths, String[] dataDirs, int userId) {
        synchronized (this.mPackageCodeLocationsCache) {
            PackageCodeLocations pcl = (PackageCodeLocations) putIfAbsent(this.mPackageCodeLocationsCache, packageName, new PackageCodeLocations(packageName, baseCodePath, splitCodePaths));
            pcl.updateCodeLocation(baseCodePath, splitCodePaths);
            if (dataDirs != null) {
                for (String dataDir : dataDirs) {
                    if (dataDir != null) {
                        pcl.mergeAppDataDirs(dataDir, userId);
                    }
                }
            }
        }
    }

    private void loadInternal(Map<Integer, List<PackageInfo>> existingPackages) {
        HashMap hashMap = new HashMap();
        HashMap hashMap2 = new HashMap();
        for (Map.Entry<Integer, List<PackageInfo>> entry : existingPackages.entrySet()) {
            int userId = entry.getKey().intValue();
            for (PackageInfo pi : entry.getValue()) {
                cachePackageInfo(pi, userId);
                ((Set) putIfAbsent(hashMap, pi.packageName, new HashSet())).add(Integer.valueOf(userId));
                Set<String> codePaths = (Set) putIfAbsent(hashMap2, pi.packageName, new HashSet());
                codePaths.add(pi.applicationInfo.sourceDir);
                if (pi.applicationInfo.splitSourceDirs != null) {
                    Collections.addAll(codePaths, pi.applicationInfo.splitSourceDirs);
                }
            }
        }
        this.mPackageDexUsage.read();
        this.mPackageDexUsage.syncData(hashMap, hashMap2);
    }

    public PackageDexUsage.PackageUseInfo getPackageUseInfoOrDefault(String packageName) {
        PackageDexUsage.PackageUseInfo useInfo = this.mPackageDexUsage.getPackageUseInfo(packageName);
        return useInfo == null ? DEFAULT_USE_INFO : useInfo;
    }

    /* access modifiers changed from: package-private */
    public boolean hasInfoOnPackage(String packageName) {
        return this.mPackageDexUsage.getPackageUseInfo(packageName) != null;
    }

    public boolean dexoptSecondaryDex(DexoptOptions options) {
        PackageDexOptimizer pdo;
        if (options.isForce()) {
            pdo = new PackageDexOptimizer.ForcedUpdatePackageDexOptimizer(this.mPackageDexOptimizer);
        } else {
            pdo = this.mPackageDexOptimizer;
        }
        String packageName = options.getPackageName();
        PackageDexUsage.PackageUseInfo useInfo = getPackageUseInfoOrDefault(packageName);
        if (useInfo.getDexUseInfoMap().isEmpty()) {
            return true;
        }
        boolean success = true;
        for (Map.Entry<String, PackageDexUsage.DexUseInfo> entry : useInfo.getDexUseInfoMap().entrySet()) {
            String dexPath = entry.getKey();
            PackageDexUsage.DexUseInfo dexUseInfo = entry.getValue();
            try {
                boolean z = false;
                PackageInfo pkg = this.mPackageManager.getPackageInfo(packageName, 0, dexUseInfo.getOwnerUserId());
                if (pkg == null) {
                    Slog.d(TAG, "Could not find package when compiling secondary dex " + packageName + " for user " + dexUseInfo.getOwnerUserId());
                    this.mPackageDexUsage.removeUserPackage(packageName, dexUseInfo.getOwnerUserId());
                } else {
                    int result = pdo.dexOptSecondaryDexPath(pkg.applicationInfo, dexPath, dexUseInfo, options);
                    if (success && result != -1) {
                        z = true;
                    }
                    success = z;
                }
            } catch (RemoteException e) {
                throw new AssertionError(e);
            }
        }
        return success;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00e5, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00e6, code lost:
        r19 = r3;
        r20 = r4;
        r21 = r5;
        r22 = r7;
        r18 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00f1, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00f2, code lost:
        r19 = r3;
        r20 = r4;
        r21 = r5;
        r22 = r7;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00f1 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:34:0x00b8] */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x012b  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0021 A[SYNTHETIC] */
    public void reconcileSecondaryDexFiles(String packageName) {
        int flags;
        Object obj;
        boolean z;
        String str = packageName;
        PackageDexUsage.PackageUseInfo useInfo = getPackageUseInfoOrDefault(packageName);
        if (!useInfo.getDexUseInfoMap().isEmpty()) {
            boolean updated = false;
            for (Map.Entry<String, PackageDexUsage.DexUseInfo> entry : useInfo.getDexUseInfoMap().entrySet()) {
                String dexPath = entry.getKey();
                PackageDexUsage.DexUseInfo dexUseInfo = entry.getValue();
                PackageInfo pkg = null;
                try {
                    pkg = this.mPackageManager.getPackageInfo(str, 0, dexUseInfo.getOwnerUserId());
                } catch (RemoteException e) {
                }
                PackageInfo pkg2 = pkg;
                boolean updated2 = true;
                if (pkg2 == null) {
                    Slog.d(TAG, "Could not find package when compiling secondary dex " + str + " for user " + dexUseInfo.getOwnerUserId());
                    if (!this.mPackageDexUsage.removeUserPackage(str, dexUseInfo.getOwnerUserId()) && !updated) {
                        updated2 = false;
                    }
                    updated = updated2;
                } else {
                    ApplicationInfo info = pkg2.applicationInfo;
                    if (info.deviceProtectedDataDir != null && FileUtils.contains(info.deviceProtectedDataDir, dexPath)) {
                        flags = 0 | 1;
                    } else if (info.credentialProtectedDataDir == null || !FileUtils.contains(info.credentialProtectedDataDir, dexPath)) {
                        PackageInfo packageInfo = pkg2;
                        Slog.e(TAG, "Could not infer CE/DE storage for path " + dexPath);
                        if (!this.mPackageDexUsage.removeDexFile(str, dexPath, dexUseInfo.getOwnerUserId()) && !updated) {
                            updated2 = false;
                        }
                        updated = updated2;
                    } else {
                        flags = 0 | 2;
                    }
                    int flags2 = flags;
                    if (this.mListener != null) {
                        this.mListener.onReconcileSecondaryDexFile(info, dexUseInfo, dexPath, flags2);
                    }
                    boolean dexStillExists = true;
                    Object obj2 = this.mInstallLock;
                    synchronized (obj2) {
                        try {
                            obj = obj2;
                            ApplicationInfo applicationInfo = info;
                            PackageInfo packageInfo2 = pkg2;
                            z = false;
                            try {
                                dexStillExists = this.mInstaller.reconcileSecondaryDexFile(dexPath, str, info.uid, (String[]) dexUseInfo.getLoaderIsas().toArray(new String[0]), info.volumeUuid, flags2);
                            } catch (Installer.InstallerException e2) {
                                e = e2;
                                try {
                                    Slog.e(TAG, "Got InstallerException when reconciling dex " + dexPath + " : " + e.getMessage());
                                    if (dexStillExists) {
                                    }
                                } catch (Throwable th) {
                                    th = th;
                                    throw th;
                                }
                            }
                        } catch (Installer.InstallerException e3) {
                            e = e3;
                            obj = obj2;
                            int i = flags2;
                            ApplicationInfo applicationInfo2 = info;
                            PackageInfo packageInfo3 = pkg2;
                            z = false;
                            Slog.e(TAG, "Got InstallerException when reconciling dex " + dexPath + " : " + e.getMessage());
                            if (dexStillExists) {
                            }
                        } catch (Throwable th2) {
                        }
                        if (dexStillExists) {
                            if (!this.mPackageDexUsage.removeDexFile(str, dexPath, dexUseInfo.getOwnerUserId()) && !updated) {
                                updated2 = z;
                            }
                            updated = updated2;
                        }
                    }
                }
            }
            if (updated) {
                this.mPackageDexUsage.maybeWriteAsync();
            }
        }
    }

    public RegisterDexModuleResult registerDexModule(ApplicationInfo info, String dexPath, boolean isUsedByOtherApps, int userId) {
        ApplicationInfo applicationInfo = info;
        String str = dexPath;
        int i = userId;
        DexSearchResult searchResult = getDexPackage(applicationInfo, str, i);
        if (searchResult.mOutcome == DEX_SEARCH_NOT_FOUND) {
            return new RegisterDexModuleResult(false, "Package not found");
        }
        if (!applicationInfo.packageName.equals(searchResult.mOwningPackageName)) {
            return new RegisterDexModuleResult(false, "Dex path does not belong to package");
        }
        if (searchResult.mOutcome == DEX_SEARCH_FOUND_PRIMARY || searchResult.mOutcome == DEX_SEARCH_FOUND_SPLIT) {
            return new RegisterDexModuleResult(false, "Main apks cannot be registered");
        }
        String[] appDexInstructionSets = InstructionSets.getAppDexInstructionSets(info);
        boolean update = false;
        int i2 = 0;
        for (int length = appDexInstructionSets.length; i2 < length; length = length) {
            update |= this.mPackageDexUsage.record(searchResult.mOwningPackageName, str, i, appDexInstructionSets[i2], isUsedByOtherApps, false, searchResult.mOwningPackageName, "=UnknownClassLoaderContext=");
            i2++;
        }
        if (update) {
            this.mPackageDexUsage.maybeWriteAsync();
        }
        if (this.mPackageDexOptimizer.dexOptSecondaryDexPath(applicationInfo, str, this.mPackageDexUsage.getPackageUseInfo(searchResult.mOwningPackageName).getDexUseInfoMap().get(str), new DexoptOptions(applicationInfo.packageName, 2, 0)) != -1) {
            Slog.e(TAG, "Failed to optimize dex module " + str);
        }
        return new RegisterDexModuleResult(true, "Dex module registered successfully");
    }

    public Set<String> getAllPackagesWithSecondaryDexFiles() {
        return this.mPackageDexUsage.getAllPackagesWithSecondaryDexFiles();
    }

    private DexSearchResult getDexPackage(ApplicationInfo loadingAppInfo, String dexPath, int userId) {
        if (dexPath.startsWith("/system/framework/")) {
            return new DexSearchResult("framework", DEX_SEARCH_NOT_FOUND);
        }
        PackageCodeLocations loadingPackageCodeLocations = new PackageCodeLocations(loadingAppInfo, userId);
        int outcome = loadingPackageCodeLocations.searchDex(dexPath, userId);
        if (outcome != DEX_SEARCH_NOT_FOUND) {
            return new DexSearchResult(loadingPackageCodeLocations.mPackageName, outcome);
        }
        synchronized (this.mPackageCodeLocationsCache) {
            for (PackageCodeLocations pcl : this.mPackageCodeLocationsCache.values()) {
                int outcome2 = pcl.searchDex(dexPath, userId);
                if (outcome2 != DEX_SEARCH_NOT_FOUND) {
                    DexSearchResult dexSearchResult = new DexSearchResult(pcl.mPackageName, outcome2);
                    return dexSearchResult;
                }
            }
            return new DexSearchResult(null, DEX_SEARCH_NOT_FOUND);
        }
    }

    /* access modifiers changed from: private */
    public static <K, V> V putIfAbsent(Map<K, V> map, K key, V newValue) {
        V existingValue = map.putIfAbsent(key, newValue);
        return existingValue == null ? newValue : existingValue;
    }

    public void writePackageDexUsageNow() {
        this.mPackageDexUsage.writeNow();
    }

    private void registerSettingObserver() {
        final ContentResolver resolver = this.mContext.getContentResolver();
        ContentObserver privAppOobObserver = new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                SystemProperties.set(DexManager.PROPERTY_NAME_PM_DEXOPT_PRIV_APPS_OOB, Settings.Global.getInt(resolver, "priv_app_oob_enabled", 0) == 1 ? "true" : "false");
            }
        };
        resolver.registerContentObserver(Settings.Global.getUriFor("priv_app_oob_enabled"), false, privAppOobObserver, 0);
        privAppOobObserver.onChange(true);
        ContentObserver privAppOobListObserver = new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                String oobList = Settings.Global.getString(resolver, "priv_app_oob_list");
                if (oobList == null) {
                    oobList = "ALL";
                }
                SystemProperties.set(DexManager.PROPERTY_NAME_PM_DEXOPT_PRIV_APPS_OOB_LIST, oobList);
            }
        };
        resolver.registerContentObserver(Settings.Global.getUriFor("priv_app_oob_list"), false, privAppOobListObserver, 0);
        privAppOobListObserver.onChange(true);
    }

    public static boolean isPackageSelectedToRunOob(String packageName) {
        return isPackageSelectedToRunOob((Collection<String>) Arrays.asList(new String[]{packageName}));
    }

    public static boolean isPackageSelectedToRunOob(Collection<String> packageNamesInSameProcess) {
        if (!SystemProperties.getBoolean(PROPERTY_NAME_PM_DEXOPT_PRIV_APPS_OOB, false)) {
            return false;
        }
        String oobListProperty = SystemProperties.get(PROPERTY_NAME_PM_DEXOPT_PRIV_APPS_OOB_LIST, "ALL");
        if ("ALL".equals(oobListProperty)) {
            return true;
        }
        for (String oobPkgName : oobListProperty.split(",")) {
            if (packageNamesInSameProcess.contains(oobPkgName)) {
                return true;
            }
        }
        return false;
    }

    public static void maybeLogUnexpectedPackageDetails(PackageParser.Package pkg) {
        if (Build.IS_DEBUGGABLE && pkg.isPrivileged() && isPackageSelectedToRunOob(pkg.packageName)) {
            logIfPackageHasUncompressedCode(pkg);
        }
    }

    private static void logIfPackageHasUncompressedCode(PackageParser.Package pkg) {
        logIfApkHasUncompressedCode(pkg.baseCodePath);
        if (!ArrayUtils.isEmpty(pkg.splitCodePaths)) {
            for (String logIfApkHasUncompressedCode : pkg.splitCodePaths) {
                logIfApkHasUncompressedCode(logIfApkHasUncompressedCode);
            }
        }
    }

    private static void logIfApkHasUncompressedCode(String fileName) {
        StrictJarFile jarFile = null;
        try {
            StrictJarFile jarFile2 = new StrictJarFile(fileName, false, false);
            Iterator<ZipEntry> it = jarFile2.iterator();
            while (it.hasNext()) {
                ZipEntry entry = it.next();
                if (entry.getName().endsWith(".dex")) {
                    if (entry.getMethod() != 0) {
                        Slog.w(TAG, "APK " + fileName + " has compressed dex code " + entry.getName());
                    } else if ((entry.getDataOffset() & 3) != 0) {
                        Slog.w(TAG, "APK " + fileName + " has unaligned dex code " + entry.getName());
                    }
                } else if (entry.getName().endsWith(".so")) {
                    if (entry.getMethod() != 0) {
                        Slog.w(TAG, "APK " + fileName + " has compressed native code " + entry.getName());
                    } else if ((entry.getDataOffset() & 4095) != 0) {
                        Slog.w(TAG, "APK " + fileName + " has unaligned native code " + entry.getName());
                    }
                }
            }
            try {
                jarFile2.close();
            } catch (IOException e) {
            }
        } catch (IOException e2) {
            Slog.wtf(TAG, "Error when parsing APK " + fileName);
            if (jarFile != null) {
                jarFile.close();
            }
        } catch (Throwable th) {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e3) {
                }
            }
            throw th;
        }
    }
}
