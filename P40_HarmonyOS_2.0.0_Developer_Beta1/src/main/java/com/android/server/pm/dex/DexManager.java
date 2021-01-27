package com.android.server.pm.dex;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.os.FileUtils;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.DeviceConfig;
import android.util.Log;
import android.util.Slog;
import android.util.jar.StrictJarFile;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.HwServiceExFactory;
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
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final PackageDexUsage.PackageUseInfo DEFAULT_USE_INFO = new PackageDexUsage.PackageUseInfo();
    private static int DEX_SEARCH_FOUND_PRIMARY = 1;
    private static int DEX_SEARCH_FOUND_SECONDARY = 3;
    private static int DEX_SEARCH_FOUND_SPLIT = 2;
    private static int DEX_SEARCH_NOT_FOUND = 0;
    private static final String PRIV_APPS_OOB_ENABLED = "priv_apps_oob_enabled";
    private static final String PRIV_APPS_OOB_WHITELIST = "priv_apps_oob_whitelist";
    private static final String PROPERTY_NAME_PM_DEXOPT_PRIV_APPS_OOB = "pm.dexopt.priv-apps-oob";
    private static final String PROPERTY_NAME_PM_DEXOPT_PRIV_APPS_OOB_LIST = "pm.dexopt.priv-apps-oob-list";
    private static final String TAG = "DexManager";
    private final Context mContext;
    private final DynamicCodeLogger mDynamicCodeLogger;
    private final IHwPackageDynamicCodeLoading mHwPackageDynamicCodeLoading;
    private final Object mInstallLock;
    @GuardedBy({"mInstallLock"})
    private final Installer mInstaller;
    @GuardedBy({"mPackageCodeLocationsCache"})
    private final Map<String, PackageCodeLocations> mPackageCodeLocationsCache = new HashMap();
    private final PackageDexOptimizer mPackageDexOptimizer;
    private final PackageDexUsage mPackageDexUsage = new PackageDexUsage();
    private final IPackageManager mPackageManager;

    public DexManager(Context context, IPackageManager pms, PackageDexOptimizer pdo, Installer installer, Object installLock) {
        this.mContext = context;
        this.mPackageManager = pms;
        this.mPackageDexOptimizer = pdo;
        this.mInstaller = installer;
        this.mInstallLock = installLock;
        this.mDynamicCodeLogger = new DynamicCodeLogger(pms, installer);
        this.mHwPackageDynamicCodeLoading = HwServiceExFactory.getHwPackageDynamicCodeLoading();
    }

    public DynamicCodeLogger getDynamicCodeLogger() {
        return this.mDynamicCodeLogger;
    }

    public void notifyDexLoad(ApplicationInfo loadingAppInfo, List<String> classLoadersNames, List<String> classPaths, String loaderIsa, int loaderUserId) {
        try {
            notifyDexLoadInternal(loadingAppInfo, classLoadersNames, classPaths, loaderIsa, loaderUserId);
        } catch (Exception e) {
            Slog.w(TAG, "Exception while notifying dex load for package " + loadingAppInfo.packageName, e);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void notifyDexLoadInternal(ApplicationInfo loadingAppInfo, List<String> classLoaderNames, List<String> classPaths, String loaderIsa, int loaderUserId) {
        String[] dexPathsToRegister;
        int i;
        int i2;
        String firstClassPath;
        ApplicationInfo applicationInfo = loadingAppInfo;
        if (classLoaderNames.size() != classPaths.size()) {
            Slog.wtf(TAG, "Bad call to noitfyDexLoad: args have different size");
        } else if (classLoaderNames.isEmpty()) {
            Slog.wtf(TAG, "Bad call to notifyDexLoad: class loaders list is empty");
        } else if (!PackageManagerServiceUtils.checkISA(loaderIsa)) {
            Slog.w(TAG, "Loading dex files " + classPaths + " in unsupported ISA: " + loaderIsa + "?");
        } else {
            boolean z = false;
            String firstClassPath2 = classPaths.get(0);
            if (firstClassPath2 != null) {
                String[] dexPathsToRegister2 = firstClassPath2.split(File.pathSeparator);
                String[] classLoaderContexts = DexoptUtils.processContextForDexLoad(classLoaderNames, classPaths);
                if (classLoaderContexts == null) {
                    if (DEBUG) {
                        Slog.i(TAG, applicationInfo.packageName + " uses unsupported class loader in " + classLoaderNames);
                    }
                }
                int length = dexPathsToRegister2.length;
                int dexPathIndex = 0;
                int i3 = 0;
                while (i3 < length) {
                    String dexPath = dexPathsToRegister2[i3];
                    DexSearchResult searchResult = getDexPackage(applicationInfo, dexPath, loaderUserId);
                    if (DEBUG) {
                        Slog.i(TAG, applicationInfo.packageName + " loads from " + searchResult + " : " + loaderUserId + " : " + dexPath);
                    }
                    if (searchResult.mOutcome != DEX_SEARCH_NOT_FOUND) {
                        boolean isUsedByOtherApps = !applicationInfo.packageName.equals(searchResult.mOwningPackageName);
                        boolean primaryOrSplit = (searchResult.mOutcome == DEX_SEARCH_FOUND_PRIMARY || searchResult.mOutcome == DEX_SEARCH_FOUND_SPLIT) ? true : z;
                        if (!primaryOrSplit || isUsedByOtherApps) {
                            if (!primaryOrSplit) {
                                this.mDynamicCodeLogger.recordDex(loaderUserId, dexPath, searchResult.mOwningPackageName, applicationInfo.packageName);
                            }
                            if (classLoaderContexts != null) {
                                i2 = i3;
                                i = length;
                                dexPathsToRegister = dexPathsToRegister2;
                                firstClassPath = firstClassPath2;
                                if (this.mPackageDexUsage.record(searchResult.mOwningPackageName, dexPath, loaderUserId, loaderIsa, isUsedByOtherApps, primaryOrSplit, applicationInfo.packageName, classLoaderContexts[dexPathIndex])) {
                                    this.mPackageDexUsage.maybeWriteAsync();
                                }
                            } else {
                                i2 = i3;
                                i = length;
                                dexPathsToRegister = dexPathsToRegister2;
                                firstClassPath = firstClassPath2;
                            }
                        } else {
                            i2 = i3;
                            i = length;
                            dexPathsToRegister = dexPathsToRegister2;
                            firstClassPath = firstClassPath2;
                            i3 = i2 + 1;
                            applicationInfo = loadingAppInfo;
                            firstClassPath2 = firstClassPath;
                            length = i;
                            dexPathsToRegister2 = dexPathsToRegister;
                            z = false;
                        }
                    } else {
                        i2 = i3;
                        i = length;
                        dexPathsToRegister = dexPathsToRegister2;
                        firstClassPath = firstClassPath2;
                        if (DEBUG) {
                            Slog.i(TAG, "Could not find owning package for dex file: " + dexPath);
                        }
                    }
                    dexPathIndex++;
                    i3 = i2 + 1;
                    applicationInfo = loadingAppInfo;
                    firstClassPath2 = firstClassPath;
                    length = i;
                    dexPathsToRegister2 = dexPathsToRegister;
                    z = false;
                }
            }
        }
    }

    public void load(Map<Integer, List<PackageInfo>> existingPackages) {
        try {
            loadInternal(existingPackages);
        } catch (Exception e) {
            this.mPackageDexUsage.clear();
            this.mDynamicCodeLogger.clear();
            this.mHwPackageDynamicCodeLoading.clear();
            Slog.w(TAG, "Exception while loading. Starting with a fresh state.", e);
        }
    }

    public void notifyPackageInstalled(PackageInfo pi, int userId) {
        if (userId != -1) {
            cachePackageInfo(pi, userId);
            return;
        }
        throw new IllegalArgumentException("notifyPackageInstalled called with USER_ALL");
    }

    public void notifyPackageUpdated(String packageName, String baseCodePath, String[] splitCodePaths) {
        cachePackageCodeLocation(packageName, baseCodePath, splitCodePaths, null, -1);
        if (this.mPackageDexUsage.clearUsedByOtherApps(packageName)) {
            this.mPackageDexUsage.maybeWriteAsync();
        }
    }

    public void notifyPackageDataDestroyed(String packageName, int userId) {
        if (userId == -1) {
            if (this.mPackageDexUsage.removePackage(packageName)) {
                this.mPackageDexUsage.maybeWriteAsync();
            }
            this.mDynamicCodeLogger.removePackage(packageName);
            this.mHwPackageDynamicCodeLoading.removePackage(packageName);
            return;
        }
        if (this.mPackageDexUsage.removeUserPackage(packageName, userId)) {
            this.mPackageDexUsage.maybeWriteAsync();
        }
        this.mDynamicCodeLogger.removeUserPackage(packageName, userId);
        this.mHwPackageDynamicCodeLoading.removeUserPackage(packageName, userId);
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
        Map<String, Set<Integer>> packageToUsersMap = new HashMap<>();
        Map<String, Set<String>> packageToCodePaths = new HashMap<>();
        for (Map.Entry<Integer, List<PackageInfo>> entry : existingPackages.entrySet()) {
            int userId = entry.getKey().intValue();
            for (PackageInfo pi : entry.getValue()) {
                cachePackageInfo(pi, userId);
                ((Set) putIfAbsent(packageToUsersMap, pi.packageName, new HashSet())).add(Integer.valueOf(userId));
                Set<String> codePaths = (Set) putIfAbsent(packageToCodePaths, pi.packageName, new HashSet());
                codePaths.add(pi.applicationInfo.sourceDir);
                if (pi.applicationInfo.splitSourceDirs != null) {
                    Collections.addAll(codePaths, pi.applicationInfo.splitSourceDirs);
                }
            }
        }
        try {
            this.mPackageDexUsage.read();
            this.mPackageDexUsage.syncData(packageToUsersMap, packageToCodePaths);
        } catch (Exception e) {
            this.mPackageDexUsage.clear();
            Slog.w(TAG, "Exception while loading package dex usage. Starting with a fresh state.", e);
        }
        try {
            this.mDynamicCodeLogger.readAndSync(packageToUsersMap);
        } catch (Exception e2) {
            this.mDynamicCodeLogger.clear();
            Slog.w(TAG, "Exception while loading package dynamic code usage. Starting with a fresh state.", e2);
        }
        this.mHwPackageDynamicCodeLoading.readAndSync(packageToUsersMap);
    }

    public PackageDexUsage.PackageUseInfo getPackageUseInfoOrDefault(String packageName) {
        PackageDexUsage.PackageUseInfo useInfo = this.mPackageDexUsage.getPackageUseInfo(packageName);
        return useInfo == null ? DEFAULT_USE_INFO : useInfo;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean hasInfoOnPackage(String packageName) {
        return this.mPackageDexUsage.getPackageUseInfo(packageName) != null;
    }

    public boolean dexoptSecondaryDex(DexoptOptions options) {
        PackageDexOptimizer pdo;
        String loadingPackageName;
        if (options.isForce()) {
            pdo = new PackageDexOptimizer.ForcedUpdatePackageDexOptimizer(this.mPackageDexOptimizer);
        } else {
            pdo = this.mPackageDexOptimizer;
        }
        String packageName = options.getPackageName();
        PackageDexUsage.PackageUseInfo useInfo = getPackageUseInfoOrDefault(packageName);
        if (useInfo.getDexUseInfoMap().isEmpty()) {
            if (DEBUG) {
                Slog.d(TAG, "No secondary dex use for package:" + packageName);
            }
            return true;
        }
        boolean success = true;
        for (Map.Entry<String, PackageDexUsage.DexUseInfo> entry : useInfo.getDexUseInfoMap().entrySet()) {
            String dexPath = entry.getKey();
            PackageDexUsage.DexUseInfo dexUseInfo = entry.getValue();
            try {
                boolean success2 = false;
                PackageInfo pkg = this.mPackageManager.getPackageInfo(packageName, 0, dexUseInfo.getOwnerUserId());
                if (pkg == null) {
                    Slog.d(TAG, "Could not find package when compiling secondary dex " + packageName + " for user " + dexUseInfo.getOwnerUserId());
                    this.mPackageDexUsage.removeUserPackage(packageName, dexUseInfo.getOwnerUserId());
                } else {
                    int result = pdo.dexOptSecondaryDexPath(pkg.applicationInfo, dexPath, dexUseInfo, options);
                    if (success && result != -1) {
                        success2 = true;
                    }
                    if (success2) {
                        if (!dexUseInfo.getLoadingPackages().isEmpty()) {
                            loadingPackageName = dexUseInfo.getLoadingPackages().iterator().next();
                        } else {
                            Slog.w(TAG, "Could not find loading package, from owning package: " + packageName);
                            loadingPackageName = packageName;
                        }
                        this.mHwPackageDynamicCodeLoading.recordDynamic(packageName, dexPath, dexUseInfo.getOwnerUserId(), loadingPackageName, 3);
                    }
                    success = success2;
                }
            } catch (RemoteException e) {
                throw new AssertionError(e);
            }
        }
        return success;
    }

    /* JADX WARNING: Removed duplicated region for block: B:49:0x012e  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x003b A[SYNTHETIC] */
    public void reconcileSecondaryDexFiles(String packageName) {
        PackageInfo pkg;
        int flags;
        Object obj;
        Throwable th;
        boolean z;
        Installer.InstallerException e;
        boolean z2;
        PackageDexUsage.PackageUseInfo useInfo = getPackageUseInfoOrDefault(packageName);
        if (!useInfo.getDexUseInfoMap().isEmpty()) {
            boolean updated = false;
            for (Map.Entry<String, PackageDexUsage.DexUseInfo> entry : useInfo.getDexUseInfoMap().entrySet()) {
                String dexPath = entry.getKey();
                PackageDexUsage.DexUseInfo dexUseInfo = entry.getValue();
                try {
                    pkg = this.mPackageManager.getPackageInfo(packageName, 0, dexUseInfo.getOwnerUserId());
                } catch (RemoteException e2) {
                    pkg = null;
                }
                boolean z3 = true;
                if (pkg == null) {
                    Slog.d(TAG, "Could not find package when compiling secondary dex " + packageName + " for user " + dexUseInfo.getOwnerUserId());
                    if (!this.mPackageDexUsage.removeUserPackage(packageName, dexUseInfo.getOwnerUserId()) && !updated) {
                        z3 = false;
                    }
                    updated = z3;
                } else {
                    ApplicationInfo info = pkg.applicationInfo;
                    if (info.deviceProtectedDataDir == null || !FileUtils.contains(info.deviceProtectedDataDir, dexPath)) {
                        if (info.credentialProtectedDataDir == null) {
                            z2 = false;
                        } else if (FileUtils.contains(info.credentialProtectedDataDir, dexPath)) {
                            flags = 0 | 2;
                        } else {
                            z2 = false;
                        }
                        Slog.e(TAG, "Could not infer CE/DE storage for path " + dexPath);
                        if (!this.mPackageDexUsage.removeDexFile(packageName, dexPath, dexUseInfo.getOwnerUserId()) && !updated) {
                            z3 = z2;
                        }
                        updated = z3;
                    } else {
                        flags = 0 | 1;
                    }
                    boolean dexStillExists = true;
                    Object obj2 = this.mInstallLock;
                    synchronized (obj2) {
                        try {
                            obj = obj2;
                            z = false;
                            try {
                                dexStillExists = this.mInstaller.reconcileSecondaryDexFile(dexPath, packageName, info.uid, (String[]) dexUseInfo.getLoaderIsas().toArray(new String[0]), info.volumeUuid, flags);
                            } catch (Installer.InstallerException e3) {
                                e = e3;
                                try {
                                    Slog.e(TAG, "Got InstallerException when reconciling dex " + dexPath + " : " + e.getMessage());
                                    if (!dexStillExists) {
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    throw th;
                                }
                            }
                        } catch (Installer.InstallerException e4) {
                            e = e4;
                            obj = obj2;
                            z = false;
                            Slog.e(TAG, "Got InstallerException when reconciling dex " + dexPath + " : " + e.getMessage());
                            if (!dexStillExists) {
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            obj = obj2;
                            throw th;
                        }
                        if (!dexStillExists) {
                            if (!this.mPackageDexUsage.removeDexFile(packageName, dexPath, dexUseInfo.getOwnerUserId()) && !updated) {
                                z3 = z;
                            }
                            updated = z3;
                        }
                    }
                }
            }
            if (updated) {
                this.mPackageDexUsage.maybeWriteAsync();
            }
        } else if (DEBUG) {
            Slog.d(TAG, "No secondary dex use for package:" + packageName);
        }
    }

    public RegisterDexModuleResult registerDexModule(ApplicationInfo info, String dexPath, boolean isUsedByOtherApps, int userId) {
        DexSearchResult searchResult = getDexPackage(info, dexPath, userId);
        if (searchResult.mOutcome == DEX_SEARCH_NOT_FOUND) {
            return new RegisterDexModuleResult(false, "Package not found");
        }
        if (!info.packageName.equals(searchResult.mOwningPackageName)) {
            return new RegisterDexModuleResult(false, "Dex path does not belong to package");
        }
        if (searchResult.mOutcome == DEX_SEARCH_FOUND_PRIMARY || searchResult.mOutcome == DEX_SEARCH_FOUND_SPLIT) {
            return new RegisterDexModuleResult(false, "Main apks cannot be registered");
        }
        String[] appDexInstructionSets = InstructionSets.getAppDexInstructionSets(info);
        boolean update = false;
        int i = 0;
        for (int length = appDexInstructionSets.length; i < length; length = length) {
            update |= this.mPackageDexUsage.record(searchResult.mOwningPackageName, dexPath, userId, appDexInstructionSets[i], isUsedByOtherApps, false, searchResult.mOwningPackageName, "=UnknownClassLoaderContext=");
            i++;
        }
        if (update) {
            this.mPackageDexUsage.maybeWriteAsync();
        }
        if (this.mPackageDexOptimizer.dexOptSecondaryDexPath(info, dexPath, this.mPackageDexUsage.getPackageUseInfo(searchResult.mOwningPackageName).getDexUseInfoMap().get(dexPath), new DexoptOptions(info.packageName, 2, 0)) != -1) {
            Slog.e(TAG, "Failed to optimize dex module " + dexPath);
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
                    return new DexSearchResult(pcl.mPackageName, outcome2);
                }
            }
        }
        if (DEBUG) {
            try {
                String dexPathReal = PackageManagerServiceUtils.realpath(new File(dexPath));
                if (!dexPath.equals(dexPathReal)) {
                    Slog.d(TAG, "Dex loaded with symlink. dexPath=" + dexPath + " dexPathReal=" + dexPathReal);
                }
            } catch (IOException e) {
            }
        }
        return new DexSearchResult(null, DEX_SEARCH_NOT_FOUND);
    }

    /* access modifiers changed from: private */
    public static <K, V> V putIfAbsent(Map<K, V> map, K key, V newValue) {
        V existingValue = map.putIfAbsent(key, newValue);
        return existingValue == null ? newValue : existingValue;
    }

    public void writePackageDexUsageNow() {
        this.mPackageDexUsage.writeNow();
        this.mDynamicCodeLogger.writeNow();
        this.mHwPackageDynamicCodeLoading.writeNow();
    }

    public static boolean isPackageSelectedToRunOob(String packageName) {
        return isPackageSelectedToRunOob(Arrays.asList(packageName));
    }

    public static boolean isPackageSelectedToRunOob(Collection<String> packageNamesInSameProcess) {
        return isPackageSelectedToRunOobInternal(SystemProperties.getBoolean(PROPERTY_NAME_PM_DEXOPT_PRIV_APPS_OOB, false), SystemProperties.get(PROPERTY_NAME_PM_DEXOPT_PRIV_APPS_OOB_LIST, "ALL"), DeviceConfig.getProperty("dex_boot", PRIV_APPS_OOB_ENABLED), DeviceConfig.getProperty("dex_boot", PRIV_APPS_OOB_WHITELIST), packageNamesInSameProcess);
    }

    @VisibleForTesting
    static boolean isPackageSelectedToRunOobInternal(boolean isDefaultEnabled, String defaultWhitelist, String overrideEnabled, String overrideWhitelist, Collection<String> packageNamesInSameProcess) {
        boolean enabled;
        if (overrideEnabled != null) {
            enabled = overrideEnabled.equals("true");
        } else {
            enabled = isDefaultEnabled;
        }
        if (!enabled) {
            return false;
        }
        String whitelist = overrideWhitelist != null ? overrideWhitelist : defaultWhitelist;
        if ("ALL".equals(whitelist)) {
            return true;
        }
        for (String oobPkgName : whitelist.split(",")) {
            if (packageNamesInSameProcess.contains(oobPkgName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean auditUncompressedDexInApk(String fileName) {
        StrictJarFile jarFile = null;
        try {
            StrictJarFile jarFile2 = new StrictJarFile(fileName, false, false);
            Iterator<ZipEntry> it = jarFile2.iterator();
            boolean allCorrect = true;
            while (it.hasNext()) {
                ZipEntry entry = it.next();
                if (entry.getName().endsWith(".dex")) {
                    if (entry.getMethod() != 0) {
                        allCorrect = false;
                        Slog.w(TAG, "APK " + fileName + " has compressed dex code " + entry.getName());
                    } else if ((entry.getDataOffset() & 3) != 0) {
                        allCorrect = false;
                        Slog.w(TAG, "APK " + fileName + " has unaligned dex code " + entry.getName());
                    }
                }
            }
            try {
                jarFile2.close();
            } catch (IOException e) {
            }
            return allCorrect;
        } catch (IOException e2) {
            Slog.wtf(TAG, "Error when parsing APK " + fileName);
            if (0 != 0) {
                try {
                    jarFile.close();
                } catch (IOException e3) {
                }
            }
            return false;
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    jarFile.close();
                } catch (IOException e4) {
                }
            }
            throw th;
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

    /* access modifiers changed from: private */
    public static class PackageCodeLocations {
        private final Map<Integer, Set<String>> mAppDataDirs;
        private String mBaseCodePath;
        private final String mPackageName;
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

    /* access modifiers changed from: private */
    public class DexSearchResult {
        private int mOutcome;
        private String mOwningPackageName;

        public DexSearchResult(String owningPackageName, int outcome) {
            this.mOwningPackageName = owningPackageName;
            this.mOutcome = outcome;
        }

        public String toString() {
            return this.mOwningPackageName + "-" + this.mOutcome;
        }
    }
}
