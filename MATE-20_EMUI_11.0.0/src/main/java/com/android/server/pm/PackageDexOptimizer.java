package com.android.server.pm;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManagerInternal;
import android.content.pm.PackageParser;
import android.content.pm.SharedLibraryInfo;
import android.content.pm.dex.ArtManager;
import android.content.pm.dex.DexMetadataHelper;
import android.os.FileUtils;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.WorkSource;
import android.util.Log;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.ZygoteInit;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.LocalServices;
import com.android.server.pm.CompilerStats;
import com.android.server.pm.Installer;
import com.android.server.pm.dex.ArtManagerService;
import com.android.server.pm.dex.DexManager;
import com.android.server.pm.dex.DexoptOptions;
import com.android.server.pm.dex.DexoptUtils;
import com.android.server.pm.dex.PackageDexUsage;
import dalvik.system.DexFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PackageDexOptimizer {
    public static final int DEX_OPT_FAILED = -1;
    public static final int DEX_OPT_PERFORMED = 1;
    public static final int DEX_OPT_SKIPPED = 0;
    private static final long DEX_OPT_TIMEOUT_MS = 30000;
    static final String OAT_DIR_NAME = "oat";
    private static final String TAG = "PackageManager.DexOptimizer";
    private static final long WAKELOCK_TIMEOUT_MS = 660000;
    private long mDexOptTotalTime = 0;
    @GuardedBy({"mInstallLock"})
    private final PowerManager.WakeLock mDexoptWakeLock;
    private final Object mInstallLock;
    @GuardedBy({"mInstallLock"})
    private final Installer mInstaller;
    private final Object mMplDexOptLock = new Object();
    private PackageManagerInternal mPackageManagerInt;
    private ArrayList<String> mPatchoatNeededApps = new ArrayList<>();
    private volatile boolean mSystemReady;

    PackageDexOptimizer(Installer installer, Object installLock, Context context, String wakeLockTag) {
        this.mInstaller = installer;
        this.mInstallLock = installLock;
        this.mDexoptWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, wakeLockTag);
        MplDexOptAdaptor.getInstance().setInstaller(this, this.mInstaller);
    }

    protected PackageDexOptimizer(PackageDexOptimizer from) {
        this.mInstaller = from.mInstaller;
        this.mInstallLock = from.mInstallLock;
        this.mDexoptWakeLock = from.mDexoptWakeLock;
        this.mSystemReady = from.mSystemReady;
        MplDexOptAdaptor.getInstance().setInstaller(this, this.mInstaller);
    }

    static boolean canOptimizePackage(PackageParser.Package pkg) {
        if ((pkg.applicationInfo.flags & 4) == 0) {
            return false;
        }
        if (pkg.applicationInfo.uid != -1) {
            return true;
        }
        Log.i(TAG, "invalid uid can not Optimize, Package:" + pkg.packageName);
        return false;
    }

    /* access modifiers changed from: package-private */
    public int performDexOpt(PackageParser.Package pkg, String[] instructionSets, CompilerStats.PackageStats packageStats, PackageDexUsage.PackageUseInfo packageUseInfo, DexoptOptions options) {
        int performDexOptLI;
        if (pkg.applicationInfo.uid == -1) {
            throw new IllegalArgumentException("Dexopt for " + pkg.packageName + " has invalid uid.");
        } else if (!canOptimizePackage(pkg)) {
            return 0;
        } else {
            synchronized (this.mInstallLock) {
                long acquireTime = acquireWakeLockLI(pkg.applicationInfo.uid);
                try {
                    synchronized (this.mMplDexOptLock) {
                        performDexOptLI = performDexOptLI(pkg, instructionSets, packageStats, packageUseInfo, options);
                    }
                } finally {
                    releaseWakeLockLI(acquireTime);
                }
            }
            return performDexOptLI;
        }
    }

    public int performDexOptWrapper(PackageParser.Package pkg, String[] instructionSets, CompilerStats.PackageStats packageStats, PackageDexUsage.PackageUseInfo packageUseInfo, DexoptOptions options) {
        int performDexOptLI;
        synchronized (this.mMplDexOptLock) {
            performDexOptLI = performDexOptLI(pkg, instructionSets, packageStats, packageUseInfo, options);
        }
        return performDexOptLI;
    }

    /* JADX WARNING: Removed duplicated region for block: B:73:0x01c5  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x02c1  */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x02b5 A[SYNTHETIC] */
    @GuardedBy({"mInstallLock"})
    private int performDexOptLI(PackageParser.Package pkg, String[] targetInstructionSets, CompilerStats.PackageStats packageStats, PackageDexUsage.PackageUseInfo packageUseInfo, DexoptOptions options) {
        int sharedGid;
        int result;
        List<SharedLibraryInfo> sharedLibraries;
        List<String> paths;
        int i;
        int sharedGid2;
        boolean[] pathsWithCode;
        String[] classLoaderContexts;
        String[] dexCodeInstructionSets;
        char c;
        String str;
        String dexMetadataPath;
        boolean isUsedByOtherApps;
        int length;
        int i2;
        List<SharedLibraryInfo> sharedLibraries2;
        List<String> paths2;
        String str2;
        String compilerFilter;
        int i3;
        int sharedGid3;
        boolean[] pathsWithCode2;
        int i4;
        String[] classLoaderContexts2;
        int i5;
        String path;
        String profileName;
        boolean isUsedByOtherApps2;
        String[] dexCodeInstructionSets2;
        int result2;
        PackageDexOptimizer packageDexOptimizer = this;
        PackageParser.Package r14 = pkg;
        List<SharedLibraryInfo> sharedLibraries3 = r14.usesLibraryInfos;
        String[] dexCodeInstructionSets3 = InstructionSets.getDexCodeInstructionSets(targetInstructionSets != null ? targetInstructionSets : InstructionSets.getAppDexInstructionSets(r14.applicationInfo));
        List<String> paths3 = pkg.getAllCodePaths();
        int sharedGid4 = UserHandle.getSharedAppGid(r14.applicationInfo.uid);
        char c2 = 65535;
        String str3 = TAG;
        if (sharedGid4 == -1) {
            Slog.wtf(str3, "Well this is awkward; package " + r14.applicationInfo.name + " had UID " + r14.applicationInfo.uid, new Throwable());
            sharedGid = 9999;
        } else {
            sharedGid = sharedGid4;
        }
        boolean[] pathsWithCode3 = new boolean[paths3.size()];
        pathsWithCode3[0] = (r14.applicationInfo.flags & 4) != 0;
        for (int i6 = 1; i6 < paths3.size(); i6++) {
            pathsWithCode3[i6] = (r14.splitFlags[i6 + -1] & 4) != 0;
        }
        String[] classLoaderContexts3 = DexoptUtils.getClassLoaderContexts(r14.applicationInfo, sharedLibraries3, pathsWithCode3);
        if (paths3.size() != classLoaderContexts3.length) {
            String[] splitCodePaths = r14.applicationInfo.getSplitCodePaths();
            StringBuilder sb = new StringBuilder();
            sb.append("Inconsistent information between PackageParser.Package and its ApplicationInfo. pkg.getAllCodePaths=");
            sb.append(paths3);
            sb.append(" pkg.applicationInfo.getBaseCodePath=");
            sb.append(r14.applicationInfo.getBaseCodePath());
            sb.append(" pkg.applicationInfo.getSplitCodePaths=");
            sb.append(splitCodePaths == null ? "null" : Arrays.toString(splitCodePaths));
            throw new IllegalStateException(sb.toString());
        }
        boolean isMapleProcess = ZygoteInit.sIsMygote;
        if (isMapleProcess) {
            MplDexOptAdaptor.getInstance().dexOptParamPrepare();
        }
        int result3 = 0;
        long startTime = System.currentTimeMillis();
        int i7 = 0;
        while (true) {
            if (i7 >= paths3.size()) {
                result = result3;
                break;
            }
            if (pathsWithCode3[i7]) {
                if (options.isOnlyPerformBaseDexopt() && i7 != 0) {
                    Slog.i(str3, "OnlyPerformBaseDexopt:" + r14.applicationInfo.name);
                    result = result3;
                    break;
                } else if (classLoaderContexts3[i7] != null) {
                    String path2 = paths3.get(i7);
                    if (options.getSplitName() == null || options.getSplitName().equals(new File(path2).getName())) {
                        String profileName2 = ArtManager.getProfileName(i7 == 0 ? null : r14.splitNames[i7 - 1]);
                        if (options.isDexoptInstallWithDexMetadata()) {
                            File dexMetadataFile = DexMetadataHelper.findDexMetadataForFile(new File(path2));
                            dexMetadataPath = dexMetadataFile == null ? null : dexMetadataFile.getAbsolutePath();
                        } else {
                            dexMetadataPath = null;
                        }
                        if (!options.isDexoptAsSharedLibrary()) {
                            if (!packageUseInfo.isUsedByOtherApps(path2)) {
                                isUsedByOtherApps = false;
                                String compilerFilter2 = packageDexOptimizer.getRealCompilerFilter(r14.applicationInfo, options.getCompilerFilter(), isUsedByOtherApps);
                                boolean profileUpdated = !options.isCheckForProfileUpdates() && packageDexOptimizer.isProfileUpdated(r14, sharedGid, profileName2, compilerFilter2);
                                int dexoptFlags = packageDexOptimizer.getDexFlags(r14, compilerFilter2, options);
                                length = dexCodeInstructionSets3.length;
                                int result4 = result3;
                                i2 = 0;
                                while (i2 < length) {
                                    String dexCodeIsa = dexCodeInstructionSets3[i2];
                                    if (isMapleProcess) {
                                        MplDexOptAdaptor.getInstance().dexOptParamAdd(path2, dexCodeIsa, compilerFilter2, classLoaderContexts3[i7], profileName2, dexMetadataPath, profileUpdated, options.isDowngrade(), sharedGid, options.getCompilationReason(), dexoptFlags);
                                        isUsedByOtherApps2 = isUsedByOtherApps;
                                        profileName = profileName2;
                                        path = path2;
                                        i3 = i7;
                                        classLoaderContexts2 = classLoaderContexts3;
                                        pathsWithCode2 = pathsWithCode3;
                                        sharedGid3 = sharedGid;
                                        str2 = str3;
                                        compilerFilter = compilerFilter2;
                                        paths2 = paths3;
                                        dexCodeInstructionSets2 = dexCodeInstructionSets3;
                                        sharedLibraries2 = sharedLibraries3;
                                        i4 = i2;
                                        i5 = length;
                                        result2 = result4;
                                    } else {
                                        isUsedByOtherApps2 = isUsedByOtherApps;
                                        profileName = profileName2;
                                        path = path2;
                                        i5 = length;
                                        classLoaderContexts2 = classLoaderContexts3;
                                        pathsWithCode2 = pathsWithCode3;
                                        sharedGid3 = sharedGid;
                                        i3 = i7;
                                        compilerFilter = compilerFilter2;
                                        str2 = str3;
                                        paths2 = paths3;
                                        dexCodeInstructionSets2 = dexCodeInstructionSets3;
                                        sharedLibraries2 = sharedLibraries3;
                                        i4 = i2;
                                        int newResult = dexOptPath(pkg, path, dexCodeIsa, compilerFilter2, profileUpdated, classLoaderContexts2[i7], dexoptFlags, sharedGid, packageStats, options.isDowngrade(), profileName, dexMetadataPath, options.getCompilationReason(), getDexoptNeeded(path2, dexCodeIsa, compilerFilter2, classLoaderContexts3[i7], profileUpdated, options.isDowngrade()));
                                        result2 = result4;
                                        if (!(result2 == -1 || newResult == 0)) {
                                            result4 = newResult;
                                            i2 = i4 + 1;
                                            dexCodeInstructionSets3 = dexCodeInstructionSets2;
                                            isUsedByOtherApps = isUsedByOtherApps2;
                                            profileName2 = profileName;
                                            path2 = path;
                                            length = i5;
                                            classLoaderContexts3 = classLoaderContexts2;
                                            pathsWithCode3 = pathsWithCode2;
                                            sharedGid = sharedGid3;
                                            i7 = i3;
                                            compilerFilter2 = compilerFilter;
                                            str3 = str2;
                                            paths3 = paths2;
                                            sharedLibraries3 = sharedLibraries2;
                                        }
                                    }
                                    result4 = result2;
                                    i2 = i4 + 1;
                                    dexCodeInstructionSets3 = dexCodeInstructionSets2;
                                    isUsedByOtherApps = isUsedByOtherApps2;
                                    profileName2 = profileName;
                                    path2 = path;
                                    length = i5;
                                    classLoaderContexts3 = classLoaderContexts2;
                                    pathsWithCode3 = pathsWithCode2;
                                    sharedGid = sharedGid3;
                                    i7 = i3;
                                    compilerFilter2 = compilerFilter;
                                    str3 = str2;
                                    paths3 = paths2;
                                    sharedLibraries3 = sharedLibraries2;
                                }
                                i = i7;
                                classLoaderContexts = classLoaderContexts3;
                                pathsWithCode = pathsWithCode3;
                                sharedGid2 = sharedGid;
                                paths = paths3;
                                dexCodeInstructionSets = dexCodeInstructionSets3;
                                sharedLibraries = sharedLibraries3;
                                c = 65535;
                                if (System.currentTimeMillis() - startTime < 30000) {
                                    Log.e(str3, "DexToOpt has run out of time 30000");
                                    result = result4;
                                    break;
                                }
                                str = str3;
                                result3 = result4;
                                i7 = i + 1;
                                r14 = pkg;
                                str3 = str;
                                c2 = c;
                                dexCodeInstructionSets3 = dexCodeInstructionSets;
                                classLoaderContexts3 = classLoaderContexts;
                                pathsWithCode3 = pathsWithCode;
                                sharedGid = sharedGid2;
                                paths3 = paths;
                                sharedLibraries3 = sharedLibraries;
                                packageDexOptimizer = this;
                            }
                        }
                        isUsedByOtherApps = true;
                        String compilerFilter22 = packageDexOptimizer.getRealCompilerFilter(r14.applicationInfo, options.getCompilerFilter(), isUsedByOtherApps);
                        if (!options.isCheckForProfileUpdates()) {
                        }
                        int dexoptFlags2 = packageDexOptimizer.getDexFlags(r14, compilerFilter22, options);
                        length = dexCodeInstructionSets3.length;
                        int result42 = result3;
                        i2 = 0;
                        while (i2 < length) {
                        }
                        i = i7;
                        classLoaderContexts = classLoaderContexts3;
                        pathsWithCode = pathsWithCode3;
                        sharedGid2 = sharedGid;
                        paths = paths3;
                        dexCodeInstructionSets = dexCodeInstructionSets3;
                        sharedLibraries = sharedLibraries3;
                        c = 65535;
                        if (System.currentTimeMillis() - startTime < 30000) {
                        }
                    }
                } else {
                    throw new IllegalStateException("Inconsistent information in the package structure. A split is marked to contain code but has no dependency listed. Index=" + i7 + " path=" + paths3.get(i7));
                }
            }
            i = i7;
            classLoaderContexts = classLoaderContexts3;
            pathsWithCode = pathsWithCode3;
            sharedGid2 = sharedGid;
            str = str3;
            c = c2;
            paths = paths3;
            dexCodeInstructionSets = dexCodeInstructionSets3;
            sharedLibraries = sharedLibraries3;
            i7 = i + 1;
            r14 = pkg;
            str3 = str;
            c2 = c;
            dexCodeInstructionSets3 = dexCodeInstructionSets;
            classLoaderContexts3 = classLoaderContexts;
            pathsWithCode3 = pathsWithCode;
            sharedGid = sharedGid2;
            paths3 = paths;
            sharedLibraries3 = sharedLibraries;
            packageDexOptimizer = this;
        }
        if (isMapleProcess) {
            return MplDexOptAdaptor.getInstance().dexOptParamProcess(pkg, packageStats);
        }
        return result;
    }

    @GuardedBy({"mInstallLock"})
    private int dexOptPath(PackageParser.Package pkg, String path, String isa, String compilerFilter, boolean profileUpdated, String classLoaderContext, int dexoptFlags, int uid, CompilerStats.PackageStats packageStats, boolean downgrade, String profileName, String dexMetadataPath, int compilationReason, int dexoptNeeded) {
        String str;
        Installer.InstallerException e;
        if (Math.abs(dexoptNeeded) == 0) {
            return 0;
        }
        String oatDir = createOatDirIfSupported(pkg, isa);
        Log.i(TAG, "Running dexopt (dexoptNeeded=" + dexoptNeeded + ") on: " + path + " pkg=" + pkg.applicationInfo.packageName + " isa=" + isa + " dexoptFlags=" + printDexoptFlags(dexoptFlags) + " targetFilter=" + compilerFilter + " oatDir=" + oatDir + " classLoaderContext=" + classLoaderContext);
        try {
            long startTime = System.currentTimeMillis();
            Installer installer = this.mInstaller;
            String str2 = pkg.packageName;
            String str3 = pkg.volumeUuid;
            String str4 = pkg.applicationInfo.seInfo;
            int i = pkg.applicationInfo.targetSdkVersion;
            try {
                String augmentedReasonName = getAugmentedReasonName(compilationReason, dexMetadataPath != null);
                str = TAG;
                try {
                    installer.dexopt(path, uid, str2, isa, dexoptNeeded, oatDir, dexoptFlags, compilerFilter, str3, classLoaderContext, str4, false, i, profileName, dexMetadataPath, augmentedReasonName);
                    if (packageStats != null) {
                        try {
                            packageStats.setCompileTime(path, (long) ((int) (System.currentTimeMillis() - startTime)));
                        } catch (Installer.InstallerException e2) {
                            e = e2;
                        }
                    }
                    return 1;
                } catch (Installer.InstallerException e3) {
                    e = e3;
                    Slog.w(str, "Failed to dexopt", e);
                    return -1;
                }
            } catch (Installer.InstallerException e4) {
                e = e4;
                str = TAG;
                Slog.w(str, "Failed to dexopt", e);
                return -1;
            }
        } catch (Installer.InstallerException e5) {
            e = e5;
            str = TAG;
            Slog.w(str, "Failed to dexopt", e);
            return -1;
        }
    }

    private String getAugmentedReasonName(int compilationReason, boolean useDexMetadata) {
        String annotation = useDexMetadata ? ArtManagerService.DEXOPT_REASON_WITH_DEX_METADATA_ANNOTATION : "";
        return PackageManagerServiceCompilerMapping.getReasonName(compilationReason) + annotation;
    }

    public int dexOptSecondaryDexPath(ApplicationInfo info, String path, PackageDexUsage.DexUseInfo dexUseInfo, DexoptOptions options) {
        int dexOptSecondaryDexPathLI;
        if (info.uid != -1) {
            synchronized (this.mInstallLock) {
                long acquireTime = acquireWakeLockLI(info.uid);
                try {
                    dexOptSecondaryDexPathLI = dexOptSecondaryDexPathLI(info, path, dexUseInfo, options);
                } finally {
                    releaseWakeLockLI(acquireTime);
                }
            }
            return dexOptSecondaryDexPathLI;
        }
        throw new IllegalArgumentException("Dexopt for path " + path + " has invalid uid.");
    }

    @GuardedBy({"mInstallLock"})
    private long acquireWakeLockLI(int uid) {
        if (!this.mSystemReady) {
            return -1;
        }
        this.mDexoptWakeLock.setWorkSource(new WorkSource(uid));
        this.mDexoptWakeLock.acquire(WAKELOCK_TIMEOUT_MS);
        return SystemClock.elapsedRealtime();
    }

    @GuardedBy({"mInstallLock"})
    private void releaseWakeLockLI(long acquireTime) {
        if (acquireTime >= 0) {
            try {
                if (this.mDexoptWakeLock.isHeld()) {
                    this.mDexoptWakeLock.release();
                }
                long duration = SystemClock.elapsedRealtime() - acquireTime;
                if (duration >= WAKELOCK_TIMEOUT_MS) {
                    Slog.wtf(TAG, "WakeLock " + this.mDexoptWakeLock.getTag() + " time out. Operation took " + duration + " ms. Thread: " + Thread.currentThread().getName());
                }
            } catch (Exception e) {
                Slog.wtf(TAG, "Error while releasing " + this.mDexoptWakeLock.getTag() + " lock", e);
            }
        }
    }

    @GuardedBy({"mInstallLock"})
    private int dexOptSecondaryDexPathLI(ApplicationInfo info, String path, PackageDexUsage.DexUseInfo dexUseInfo, DexoptOptions options) {
        int dexoptFlags;
        String compilerFilter;
        String classLoaderContext;
        String str;
        Installer.InstallerException e;
        String str2;
        if (options.isDexoptOnlySharedDex() && !dexUseInfo.isUsedByOtherApps()) {
            return 0;
        }
        String compilerFilter2 = getRealCompilerFilter(info, options.getCompilerFilter(), dexUseInfo.isUsedByOtherApps());
        int dexoptFlags2 = getDexFlags(info, compilerFilter2, options) | 32;
        String str3 = info.deviceProtectedDataDir;
        String str4 = TAG;
        if (str3 == null || !FileUtils.contains(info.deviceProtectedDataDir, path)) {
            if (info.credentialProtectedDataDir == null) {
                str2 = str4;
            } else if (FileUtils.contains(info.credentialProtectedDataDir, path)) {
                dexoptFlags = dexoptFlags2 | 128;
            } else {
                str2 = str4;
            }
            Slog.e(str2, "Could not infer CE/DE storage for package " + info.packageName);
            return -1;
        }
        dexoptFlags = dexoptFlags2 | 256;
        if (dexUseInfo.isUnknownClassLoaderContext() || dexUseInfo.isVariableClassLoaderContext()) {
            compilerFilter = "extract";
            classLoaderContext = null;
        } else {
            compilerFilter = compilerFilter2;
            classLoaderContext = dexUseInfo.getClassLoaderContext();
        }
        int reason = options.getCompilationReason();
        Log.d(str4, "Running dexopt on: " + path + " pkg=" + info.packageName + " isa=" + dexUseInfo.getLoaderIsas() + " reason=" + PackageManagerServiceCompilerMapping.getReasonName(reason) + " dexoptFlags=" + printDexoptFlags(dexoptFlags) + " target-filter=" + compilerFilter + " class-loader-context=" + classLoaderContext);
        try {
            for (String isa : dexUseInfo.getLoaderIsas()) {
                str = str4;
                try {
                    this.mInstaller.dexopt(path, info.uid, info.packageName, isa, 0, null, dexoptFlags, compilerFilter, info.volumeUuid, classLoaderContext, info.seInfo, options.isDowngrade(), info.targetSdkVersion, null, null, PackageManagerServiceCompilerMapping.getReasonName(reason));
                    classLoaderContext = classLoaderContext;
                    compilerFilter = compilerFilter;
                    dexoptFlags = dexoptFlags;
                    str4 = str;
                } catch (Installer.InstallerException e2) {
                    e = e2;
                    Slog.w(str, "Failed to dexopt", e);
                    return -1;
                }
            }
            return 1;
        } catch (Installer.InstallerException e3) {
            e = e3;
            str = str4;
            Slog.w(str, "Failed to dexopt", e);
            return -1;
        }
    }

    /* access modifiers changed from: protected */
    public int adjustDexoptNeeded(int dexoptNeeded) {
        return dexoptNeeded;
    }

    /* access modifiers changed from: protected */
    public int adjustDexoptFlags(int dexoptFlags) {
        return dexoptFlags;
    }

    /* access modifiers changed from: package-private */
    public void dumpDexoptState(IndentingPrintWriter pw, PackageParser.Package pkg, PackageDexUsage.PackageUseInfo useInfo) {
        String[] dexCodeInstructionSets = InstructionSets.getDexCodeInstructionSets(InstructionSets.getAppDexInstructionSets(pkg.applicationInfo));
        for (String path : pkg.getAllCodePathsExcludingResourceOnly()) {
            pw.println("path: " + path);
            pw.increaseIndent();
            int length = dexCodeInstructionSets.length;
            for (int i = 0; i < length; i++) {
                String isa = dexCodeInstructionSets[i];
                try {
                    DexFile.OptimizationInfo info = DexFile.getDexFileOptimizationInfo(path, isa);
                    pw.println(isa + ": [status=" + info.getStatus() + "] [reason=" + info.getReason() + "]");
                } catch (IOException ioe) {
                    pw.println(isa + ": [Exception]: " + ioe.getMessage());
                }
            }
            if (useInfo.isUsedByOtherApps(path)) {
                pw.println("used by other apps: " + useInfo.getLoadingPackages(path));
            }
            Map<String, PackageDexUsage.DexUseInfo> dexUseInfoMap = useInfo.getDexUseInfoMap();
            if (!dexUseInfoMap.isEmpty()) {
                pw.println("known secondary dex files:");
                pw.increaseIndent();
                for (Map.Entry<String, PackageDexUsage.DexUseInfo> e : dexUseInfoMap.entrySet()) {
                    PackageDexUsage.DexUseInfo dexUseInfo = e.getValue();
                    pw.println(e.getKey());
                    pw.increaseIndent();
                    pw.println("class loader context: " + dexUseInfo.getClassLoaderContext());
                    if (dexUseInfo.isUsedByOtherApps()) {
                        pw.println("used by other apps: " + dexUseInfo.getLoadingPackages());
                    }
                    pw.decreaseIndent();
                }
                pw.decreaseIndent();
            }
            pw.decreaseIndent();
        }
    }

    private String getRealCompilerFilter(ApplicationInfo info, String targetCompilerFilter, boolean isUsedByOtherApps) {
        if (info.isEmbeddedDexUsed()) {
            return "verify";
        }
        if (info.isPrivilegedApp() && DexManager.isPackageSelectedToRunOob(info.packageName)) {
            return "verify";
        }
        if (((info.flags & DumpState.DUMP_KEYSETS) == 0 && (info.flags & 2) == 0) ? false : true) {
            return DexFile.getSafeModeCompilerFilter(targetCompilerFilter);
        }
        if (!DexFile.isProfileGuidedCompilerFilter(targetCompilerFilter) || !isUsedByOtherApps) {
            return targetCompilerFilter;
        }
        return PackageManagerServiceCompilerMapping.getCompilerFilterForReason(6);
    }

    private int getDexFlags(PackageParser.Package pkg, String compilerFilter, DexoptOptions options) {
        return getDexFlags(pkg.applicationInfo, compilerFilter, options);
    }

    private boolean isAppImageEnabled() {
        return SystemProperties.get("dalvik.vm.appimageformat", "").length() > 0;
    }

    private int getDexFlags(ApplicationInfo info, String compilerFilter, DexoptOptions options) {
        int hiddenApiFlag;
        boolean generateAppImage = true;
        int i = 0;
        boolean debuggable = (info.flags & 2) != 0;
        boolean isProfileGuidedFilter = DexFile.isProfileGuidedCompilerFilter(compilerFilter);
        boolean isPublic = !isProfileGuidedFilter || options.isDexoptInstallWithDexMetadata();
        int profileFlag = isProfileGuidedFilter ? 16 : 0;
        if (info.getHiddenApiEnforcementPolicy() == 0) {
            hiddenApiFlag = 0;
        } else {
            hiddenApiFlag = 1024;
        }
        int compilationReason = options.getCompilationReason();
        boolean generateCompactDex = true;
        int i2 = 2;
        if (compilationReason == 0 || compilationReason == 1 || compilationReason == 2) {
            generateCompactDex = false;
        }
        if (!isProfileGuidedFilter || ((info.splitDependencies != null && info.requestsIsolatedSplitLoading()) || !isAppImageEnabled())) {
            generateAppImage = false;
        }
        if (!isPublic) {
            i2 = 0;
        }
        int i3 = i2 | (debuggable ? 4 : 0) | profileFlag | (options.isBootComplete() ? 8 : 0) | (options.isDexoptIdleBackgroundJob() ? 512 : 0) | (generateCompactDex ? 2048 : 0);
        if (generateAppImage) {
            i = 4096;
        }
        return adjustDexoptFlags(i | i3 | hiddenApiFlag);
    }

    private int getDexoptNeeded(String path, String isa, String compilerFilter, String classLoaderContext, boolean newProfile, boolean downgrade) {
        try {
            return adjustDexoptNeeded(DexFile.getDexOptNeeded(path, isa, compilerFilter, classLoaderContext, newProfile, downgrade));
        } catch (IOException ioe) {
            Slog.w(TAG, "IOException reading apk: " + path, ioe);
            return -1;
        }
    }

    private boolean isProfileUpdated(PackageParser.Package pkg, int uid, String profileName, String compilerFilter) {
        if (!DexFile.isProfileGuidedCompilerFilter(compilerFilter)) {
            return false;
        }
        try {
            return this.mInstaller.mergeProfiles(uid, pkg.packageName, profileName);
        } catch (Installer.InstallerException e) {
            Slog.w(TAG, "Failed to merge profiles", e);
            return false;
        }
    }

    private String createOatDirIfSupported(PackageParser.Package pkg, String dexInstructionSet) {
        if (this.mPackageManagerInt == null) {
            this.mPackageManagerInt = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        }
        Boolean isSystemAppGrantByMdmAndNonPreload = Boolean.valueOf(this.mPackageManagerInt.isSystemAppGrantByMdmAndNonPreload(pkg.packageName));
        if (!pkg.canHaveOatDir() && !isSystemAppGrantByMdmAndNonPreload.booleanValue()) {
            return null;
        }
        File codePath = new File(pkg.codePath);
        if (!codePath.isDirectory()) {
            return null;
        }
        File oatDir = getOatDir(codePath);
        try {
            this.mInstaller.createOatDir(oatDir.getAbsolutePath(), dexInstructionSet);
            return oatDir.getAbsolutePath();
        } catch (Installer.InstallerException e) {
            Slog.w(TAG, "Failed to create oat dir", e);
            return null;
        }
    }

    static File getOatDir(File codePath) {
        return new File(codePath, OAT_DIR_NAME);
    }

    /* access modifiers changed from: package-private */
    public void systemReady() {
        this.mSystemReady = true;
    }

    private String printDexoptFlags(int flags) {
        ArrayList<String> flagsList = new ArrayList<>();
        if ((flags & 8) == 8) {
            flagsList.add("boot_complete");
        }
        if ((flags & 4) == 4) {
            flagsList.add("debuggable");
        }
        if ((flags & 16) == 16) {
            flagsList.add("profile_guided");
        }
        if ((flags & 2) == 2) {
            flagsList.add("public");
        }
        if ((flags & 32) == 32) {
            flagsList.add("secondary");
        }
        if ((flags & 64) == 64) {
            flagsList.add("force");
        }
        if ((flags & 128) == 128) {
            flagsList.add("storage_ce");
        }
        if ((flags & 256) == 256) {
            flagsList.add("storage_de");
        }
        if ((flags & 512) == 512) {
            flagsList.add("idle_background_job");
        }
        if ((flags & 1024) == 1024) {
            flagsList.add("enable_hidden_api_checks");
        }
        return String.join(",", flagsList);
    }

    public static class ForcedUpdatePackageDexOptimizer extends PackageDexOptimizer {
        public ForcedUpdatePackageDexOptimizer(Installer installer, Object installLock, Context context, String wakeLockTag) {
            super(installer, installLock, context, wakeLockTag);
        }

        public ForcedUpdatePackageDexOptimizer(PackageDexOptimizer from) {
            super(from);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.pm.PackageDexOptimizer
        public int adjustDexoptNeeded(int dexoptNeeded) {
            if (dexoptNeeded == 0) {
                return -3;
            }
            return dexoptNeeded;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.server.pm.PackageDexOptimizer
        public int adjustDexoptFlags(int flags) {
            return flags | 64;
        }
    }

    public long getDexOptTotalTime() {
        return this.mDexOptTotalTime;
    }

    public ArrayList<String> getPatchoatNeededApps() {
        return this.mPatchoatNeededApps;
    }

    /* access modifiers changed from: package-private */
    public int mplAdjustDexoptNeeded(int dexoptNeeded) {
        return adjustDexoptNeeded(dexoptNeeded);
    }

    /* access modifiers changed from: package-private */
    public int mplDexOptPath(PackageParser.Package pkg, String path, String isa, String compilerFilter, boolean isProfileUpdated, String classLoaderContext, int dexoptFlags, int uid, CompilerStats.PackageStats packageStats, boolean isDowngrade, String profileName, String dexMetadataPath, int compilationReason, int dexoptNeeded) {
        return dexOptPath(pkg, path, isa, compilerFilter, isProfileUpdated, classLoaderContext, dexoptFlags, uid, packageStats, isDowngrade, profileName, dexMetadataPath, compilationReason, dexoptNeeded);
    }

    /* access modifiers changed from: package-private */
    public String mplGetRealCompilerFilter(ApplicationInfo info, String targetCompilerFilter, boolean isUsedByOtherApps) {
        return getRealCompilerFilter(info, targetCompilerFilter, isUsedByOtherApps);
    }

    /* access modifiers changed from: package-private */
    public boolean mplIsProfileUpdated(PackageParser.Package pkg, int uid, String profileName, String compilerFilter) {
        return isProfileUpdated(pkg, uid, profileName, compilerFilter);
    }

    /* access modifiers changed from: package-private */
    public int mplGetDexFlags(PackageParser.Package pkg, String compilerFilter, DexoptOptions options) {
        return getDexFlags(pkg, compilerFilter, options);
    }
}
