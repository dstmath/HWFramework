package com.android.server.pm;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser;
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
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.pm.CompilerStats;
import com.android.server.pm.Installer;
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
    static final String OAT_DIR_NAME = "oat";
    public static final String SKIP_SHARED_LIBRARY_CHECK = "&";
    private static final String TAG = "PackageManager.DexOptimizer";
    private static final long WAKELOCK_TIMEOUT_MS = 660000;
    private long mDexOptTotalTime = 0;
    @GuardedBy("mInstallLock")
    private final PowerManager.WakeLock mDexoptWakeLock;
    private final Object mInstallLock;
    @GuardedBy("mInstallLock")
    private final Installer mInstaller;
    ArrayList<String> mPatchoatNeededApps = new ArrayList<>();
    private volatile boolean mSystemReady;

    public static class ForcedUpdatePackageDexOptimizer extends PackageDexOptimizer {
        public ForcedUpdatePackageDexOptimizer(Installer installer, Object installLock, Context context, String wakeLockTag) {
            super(installer, installLock, context, wakeLockTag);
        }

        public ForcedUpdatePackageDexOptimizer(PackageDexOptimizer from) {
            super(from);
        }

        /* access modifiers changed from: protected */
        public int adjustDexoptNeeded(int dexoptNeeded) {
            if (dexoptNeeded == 0) {
                return -3;
            }
            return dexoptNeeded;
        }

        /* access modifiers changed from: protected */
        public int adjustDexoptFlags(int flags) {
            return flags | 64;
        }
    }

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
        return true;
    }

    /* access modifiers changed from: package-private */
    public int performDexOpt(PackageParser.Package pkg, String[] sharedLibraries, String[] instructionSets, CompilerStats.PackageStats packageStats, PackageDexUsage.PackageUseInfo packageUseInfo, DexoptOptions options) {
        int performDexOptLI;
        if (pkg.applicationInfo.uid == -1) {
            throw new IllegalArgumentException("Dexopt for " + pkg.packageName + " has invalid uid.");
        } else if (!canOptimizePackage(pkg)) {
            return 0;
        } else {
            synchronized (this.mInstallLock) {
                long acquireTime = acquireWakeLockLI(pkg.applicationInfo.uid);
                try {
                    performDexOptLI = performDexOptLI(pkg, sharedLibraries, instructionSets, packageStats, packageUseInfo, options);
                } finally {
                    releaseWakeLockLI(acquireTime);
                }
            }
            return performDexOptLI;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:68:0x0195  */
    @GuardedBy("mInstallLock")
    private int performDexOptLI(PackageParser.Package pkg, String[] sharedLibraries, String[] targetInstructionSets, CompilerStats.PackageStats packageStats, PackageDexUsage.PackageUseInfo packageUseInfo, DexoptOptions options) {
        int i;
        List<String> paths;
        String[] instructionSets;
        String[] dexCodeInstructionSets;
        int sharedGid;
        boolean[] pathsWithCode;
        String[] classLoaderContexts;
        char c;
        String dexMetadataPath;
        boolean isUsedByOtherApps;
        int length;
        int i2;
        int i3;
        List<String> paths2;
        int i4;
        String[] instructionSets2;
        String[] dexCodeInstructionSets2;
        String compilerFilter;
        int sharedGid2;
        boolean[] pathsWithCode2;
        int i5;
        String path;
        String profileName;
        boolean isUsedByOtherApps2;
        String[] classLoaderContexts2;
        int i6;
        PackageDexOptimizer packageDexOptimizer = this;
        PackageParser.Package packageR = pkg;
        String[] instructionSets3 = targetInstructionSets != null ? targetInstructionSets : InstructionSets.getAppDexInstructionSets(packageR.applicationInfo);
        String[] dexCodeInstructionSets3 = InstructionSets.getDexCodeInstructionSets(instructionSets3);
        List<String> paths3 = pkg.getAllCodePaths();
        int sharedGid3 = UserHandle.getSharedAppGid(packageR.applicationInfo.uid);
        char c2 = 65535;
        if (sharedGid3 == -1) {
            Slog.wtf(TAG, "Well this is awkward; package " + packageR.applicationInfo.name + " had UID " + packageR.applicationInfo.uid, new Throwable());
            sharedGid3 = 9999;
        }
        int sharedGid4 = sharedGid3;
        boolean[] pathsWithCode3 = new boolean[paths3.size()];
        pathsWithCode3[0] = (packageR.applicationInfo.flags & 4) != 0;
        for (int i7 = 1; i7 < paths3.size(); i7++) {
            pathsWithCode3[i7] = (packageR.splitFlags[i7 + -1] & 4) != 0;
        }
        String[] classLoaderContexts3 = DexoptUtils.getClassLoaderContexts(packageR.applicationInfo, sharedLibraries, pathsWithCode3);
        if (paths3.size() != classLoaderContexts3.length) {
            String[] splitCodePaths = packageR.applicationInfo.getSplitCodePaths();
            StringBuilder sb = new StringBuilder();
            sb.append("Inconsistent information between PackageParser.Package and its ApplicationInfo. pkg.getAllCodePaths=");
            sb.append(paths3);
            sb.append(" pkg.applicationInfo.getBaseCodePath=");
            sb.append(packageR.applicationInfo.getBaseCodePath());
            sb.append(" pkg.applicationInfo.getSplitCodePaths=");
            sb.append(splitCodePaths == null ? "null" : Arrays.toString(splitCodePaths));
            throw new IllegalStateException(sb.toString());
        }
        boolean mapleProcess = PackageManagerService.sIsMygote;
        if (mapleProcess) {
            MplDexOptAdaptor.getInstance().dexOptParamPrepare();
        }
        int result = 0;
        int result2 = 0;
        while (true) {
            int i8 = result2;
            if (i8 < paths3.size()) {
                if (pathsWithCode3[i8]) {
                    if (classLoaderContexts3[i8] != null) {
                        String path2 = paths3.get(i8);
                        if (options.getSplitName() == null || options.getSplitName().equals(new File(path2).getName())) {
                            String profileName2 = ArtManager.getProfileName(i8 == 0 ? null : packageR.splitNames[i8 - 1]);
                            if (options.isDexoptInstallWithDexMetadata()) {
                                File dexMetadataFile = DexMetadataHelper.findDexMetadataForFile(new File(path2));
                                dexMetadataPath = dexMetadataFile == null ? null : dexMetadataFile.getAbsolutePath();
                                String str = dexMetadataPath;
                            } else {
                                dexMetadataPath = null;
                            }
                            if (options.isDexoptAsSharedLibrary()) {
                                PackageDexUsage.PackageUseInfo packageUseInfo2 = packageUseInfo;
                            } else if (!packageUseInfo.isUsedByOtherApps(path2)) {
                                isUsedByOtherApps = false;
                                int result3 = result;
                                String compilerFilter2 = packageDexOptimizer.getRealCompilerFilter(packageR.applicationInfo, options.getCompilerFilter(), isUsedByOtherApps);
                                boolean profileUpdated = !options.isCheckForProfileUpdates() && packageDexOptimizer.isProfileUpdated(packageR, sharedGid4, profileName2, compilerFilter2);
                                int dexoptFlags = packageDexOptimizer.getDexFlags(packageR, compilerFilter2, options);
                                String[] instructionSets4 = instructionSets3;
                                length = dexCodeInstructionSets3.length;
                                List<String> paths4 = paths3;
                                i2 = 0;
                                int result4 = result3;
                                while (i2 < length) {
                                    String dexCodeIsa = dexCodeInstructionSets3[i2];
                                    if (mapleProcess) {
                                        sharedGid2 = sharedGid4;
                                        MplDexOptAdaptor.getInstance().dexOptParamAdd(path2, dexCodeIsa, compilerFilter2, classLoaderContexts3[i8], profileName2, dexMetadataPath, profileUpdated, options.isDowngrade(), sharedGid2, options.getCompilationReason(), dexoptFlags);
                                        isUsedByOtherApps2 = isUsedByOtherApps;
                                        profileName = profileName2;
                                        path = path2;
                                        i3 = i8;
                                        classLoaderContexts2 = classLoaderContexts3;
                                        pathsWithCode2 = pathsWithCode3;
                                        compilerFilter = compilerFilter2;
                                        i5 = i2;
                                        dexCodeInstructionSets2 = dexCodeInstructionSets3;
                                        instructionSets2 = instructionSets4;
                                        paths2 = paths4;
                                        i6 = result4;
                                        i4 = length;
                                    } else {
                                        isUsedByOtherApps2 = isUsedByOtherApps;
                                        PackageDexOptimizer packageDexOptimizer2 = packageDexOptimizer;
                                        profileName = profileName2;
                                        path = path2;
                                        String path3 = classLoaderContexts3[i8];
                                        int i9 = length;
                                        int i10 = i8;
                                        boolean z = profileUpdated;
                                        classLoaderContexts2 = classLoaderContexts3;
                                        pathsWithCode2 = pathsWithCode3;
                                        sharedGid2 = sharedGid4;
                                        compilerFilter = compilerFilter2;
                                        i5 = i2;
                                        paths2 = paths4;
                                        dexCodeInstructionSets2 = dexCodeInstructionSets3;
                                        i3 = i10;
                                        instructionSets2 = instructionSets4;
                                        i4 = i9;
                                        int result5 = packageDexOptimizer2.dexOptPath(packageR, path, dexCodeIsa, compilerFilter2, z, classLoaderContexts2[i10], dexoptFlags, sharedGid4, packageStats, options.isDowngrade(), profileName, dexMetadataPath, options.getCompilationReason(), packageDexOptimizer2.getDexoptNeeded(path2, dexCodeIsa, compilerFilter2, path3, z, options.isDowngrade()));
                                        i6 = result4;
                                        if (!(i6 == -1 || result5 == 0)) {
                                            result4 = result5;
                                            i2 = i5 + 1;
                                            String[] strArr = sharedLibraries;
                                            PackageDexUsage.PackageUseInfo packageUseInfo3 = packageUseInfo;
                                            DexoptOptions dexoptOptions = options;
                                            classLoaderContexts3 = classLoaderContexts2;
                                            isUsedByOtherApps = isUsedByOtherApps2;
                                            profileName2 = profileName;
                                            path2 = path;
                                            pathsWithCode3 = pathsWithCode2;
                                            sharedGid4 = sharedGid2;
                                            compilerFilter2 = compilerFilter;
                                            dexCodeInstructionSets3 = dexCodeInstructionSets2;
                                            length = i4;
                                            paths4 = paths2;
                                            i8 = i3;
                                            packageR = pkg;
                                            packageDexOptimizer = this;
                                            instructionSets4 = instructionSets2;
                                        }
                                    }
                                    result4 = i6;
                                    i2 = i5 + 1;
                                    String[] strArr2 = sharedLibraries;
                                    PackageDexUsage.PackageUseInfo packageUseInfo32 = packageUseInfo;
                                    DexoptOptions dexoptOptions2 = options;
                                    classLoaderContexts3 = classLoaderContexts2;
                                    isUsedByOtherApps = isUsedByOtherApps2;
                                    profileName2 = profileName;
                                    path2 = path;
                                    pathsWithCode3 = pathsWithCode2;
                                    sharedGid4 = sharedGid2;
                                    compilerFilter2 = compilerFilter;
                                    dexCodeInstructionSets3 = dexCodeInstructionSets2;
                                    length = i4;
                                    paths4 = paths2;
                                    i8 = i3;
                                    packageR = pkg;
                                    packageDexOptimizer = this;
                                    instructionSets4 = instructionSets2;
                                }
                                i = i8;
                                classLoaderContexts = classLoaderContexts3;
                                pathsWithCode = pathsWithCode3;
                                sharedGid = sharedGid4;
                                dexCodeInstructionSets = dexCodeInstructionSets3;
                                instructionSets = instructionSets4;
                                paths = paths4;
                                result = result4;
                                c = 65535;
                                result2 = i + 1;
                                String[] strArr3 = sharedLibraries;
                                c2 = c;
                                classLoaderContexts3 = classLoaderContexts;
                                pathsWithCode3 = pathsWithCode;
                                sharedGid4 = sharedGid;
                                dexCodeInstructionSets3 = dexCodeInstructionSets;
                                instructionSets3 = instructionSets;
                                paths3 = paths;
                                packageR = pkg;
                                packageDexOptimizer = this;
                            }
                            isUsedByOtherApps = true;
                            int result32 = result;
                            String compilerFilter22 = packageDexOptimizer.getRealCompilerFilter(packageR.applicationInfo, options.getCompilerFilter(), isUsedByOtherApps);
                            if (!options.isCheckForProfileUpdates()) {
                            }
                            int dexoptFlags2 = packageDexOptimizer.getDexFlags(packageR, compilerFilter22, options);
                            String[] instructionSets42 = instructionSets3;
                            length = dexCodeInstructionSets3.length;
                            List<String> paths42 = paths3;
                            i2 = 0;
                            int result42 = result32;
                            while (i2 < length) {
                            }
                            i = i8;
                            classLoaderContexts = classLoaderContexts3;
                            pathsWithCode = pathsWithCode3;
                            sharedGid = sharedGid4;
                            dexCodeInstructionSets = dexCodeInstructionSets3;
                            instructionSets = instructionSets42;
                            paths = paths42;
                            result = result42;
                            c = 65535;
                            result2 = i + 1;
                            String[] strArr32 = sharedLibraries;
                            c2 = c;
                            classLoaderContexts3 = classLoaderContexts;
                            pathsWithCode3 = pathsWithCode;
                            sharedGid4 = sharedGid;
                            dexCodeInstructionSets3 = dexCodeInstructionSets;
                            instructionSets3 = instructionSets;
                            paths3 = paths;
                            packageR = pkg;
                            packageDexOptimizer = this;
                        }
                    } else {
                        int i11 = result;
                        String[] strArr4 = classLoaderContexts3;
                        boolean[] zArr = pathsWithCode3;
                        int i12 = sharedGid4;
                        String[] strArr5 = dexCodeInstructionSets3;
                        String[] strArr6 = instructionSets3;
                        throw new IllegalStateException("Inconsistent information in the package structure. A split is marked to contain code but has no dependency listed. Index=" + i + " path=" + paths3.get(i));
                    }
                }
                i = i8;
                classLoaderContexts = classLoaderContexts3;
                pathsWithCode = pathsWithCode3;
                sharedGid = sharedGid4;
                c = c2;
                paths = paths3;
                dexCodeInstructionSets = dexCodeInstructionSets3;
                instructionSets = instructionSets3;
                result2 = i + 1;
                String[] strArr322 = sharedLibraries;
                c2 = c;
                classLoaderContexts3 = classLoaderContexts;
                pathsWithCode3 = pathsWithCode;
                sharedGid4 = sharedGid;
                dexCodeInstructionSets3 = dexCodeInstructionSets;
                instructionSets3 = instructionSets;
                paths3 = paths;
                packageR = pkg;
                packageDexOptimizer = this;
            } else {
                int result6 = result;
                String[] strArr7 = classLoaderContexts3;
                boolean[] zArr2 = pathsWithCode3;
                int i13 = sharedGid4;
                List<String> list = paths3;
                String[] strArr8 = dexCodeInstructionSets3;
                String[] strArr9 = instructionSets3;
                if (mapleProcess) {
                    return MplDexOptAdaptor.getInstance().dexOptParamProcess(pkg, packageStats);
                }
                CompilerStats.PackageStats packageStats2 = packageStats;
                PackageParser.Package packageR2 = pkg;
                return result6;
            }
        }
    }

    @GuardedBy("mInstallLock")
    private int dexOptPath(PackageParser.Package pkg, String path, String isa, String compilerFilter, boolean profileUpdated, String classLoaderContext, int dexoptFlags, int uid, CompilerStats.PackageStats packageStats, boolean downgrade, String profileName, String dexMetadataPath, int compilationReason, int dexoptNeeded) {
        PackageParser.Package packageR = pkg;
        String str = path;
        String str2 = isa;
        CompilerStats.PackageStats packageStats2 = packageStats;
        if (Math.abs(dexoptNeeded) == 0) {
            return 0;
        }
        String oatDir = createOatDirIfSupported(packageR, str2);
        StringBuilder sb = new StringBuilder();
        sb.append("Running dexopt (dexoptNeeded=");
        int i = dexoptNeeded;
        sb.append(i);
        sb.append(") on: ");
        sb.append(str);
        sb.append(" pkg=");
        sb.append(packageR.applicationInfo.packageName);
        sb.append(" isa=");
        sb.append(str2);
        sb.append(" dexoptFlags=");
        sb.append(printDexoptFlags(dexoptFlags));
        sb.append(" targetFilter=");
        sb.append(compilerFilter);
        sb.append(" oatDir=");
        sb.append(oatDir);
        sb.append(" classLoaderContext=");
        sb.append(classLoaderContext);
        Log.i(TAG, sb.toString());
        try {
            long startTime = System.currentTimeMillis();
            Installer installer = this.mInstaller;
            String str3 = packageR.packageName;
            String oatDir2 = packageR.volumeUuid;
            String str4 = packageR.applicationInfo.seInfo;
            String str5 = oatDir;
            String str6 = str;
            try {
                installer.dexopt(str, uid, str3, str2, i, oatDir, dexoptFlags, compilerFilter, oatDir2, classLoaderContext, str4, false, packageR.applicationInfo.targetSdkVersion, profileName, dexMetadataPath, PackageManagerServiceCompilerMapping.getReasonName(compilationReason));
                CompilerStats.PackageStats packageStats3 = packageStats;
                if (packageStats3 != null) {
                    try {
                        packageStats3.setCompileTime(str6, (long) ((int) (System.currentTimeMillis() - startTime)));
                    } catch (Installer.InstallerException e) {
                        e = e;
                    }
                }
                return 1;
            } catch (Installer.InstallerException e2) {
                e = e2;
                CompilerStats.PackageStats packageStats4 = packageStats;
                Slog.w(TAG, "Failed to dexopt", e);
                return -1;
            }
        } catch (Installer.InstallerException e3) {
            e = e3;
            String str7 = oatDir;
            CompilerStats.PackageStats packageStats5 = packageStats2;
            String str8 = str;
            Slog.w(TAG, "Failed to dexopt", e);
            return -1;
        }
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

    @GuardedBy("mInstallLock")
    private long acquireWakeLockLI(int uid) {
        if (!this.mSystemReady) {
            return -1;
        }
        this.mDexoptWakeLock.setWorkSource(new WorkSource(uid));
        this.mDexoptWakeLock.acquire(WAKELOCK_TIMEOUT_MS);
        return SystemClock.elapsedRealtime();
    }

    @GuardedBy("mInstallLock")
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

    @GuardedBy("mInstallLock")
    private int dexOptSecondaryDexPathLI(ApplicationInfo info, String path, PackageDexUsage.DexUseInfo dexUseInfo, DexoptOptions options) {
        int dexoptFlags;
        ApplicationInfo applicationInfo = info;
        String str = path;
        if (options.isDexoptOnlySharedDex() && !dexUseInfo.isUsedByOtherApps()) {
            return 0;
        }
        String compilerFilter = getRealCompilerFilter(applicationInfo, options.getCompilerFilter(), dexUseInfo.isUsedByOtherApps());
        int dexoptFlags2 = getDexFlags(applicationInfo, compilerFilter, options) | 32;
        if (applicationInfo.deviceProtectedDataDir != null && FileUtils.contains(applicationInfo.deviceProtectedDataDir, str)) {
            dexoptFlags = dexoptFlags2 | 256;
        } else if (applicationInfo.credentialProtectedDataDir == null || !FileUtils.contains(applicationInfo.credentialProtectedDataDir, str)) {
            Slog.e(TAG, "Could not infer CE/DE storage for package " + applicationInfo.packageName);
            return -1;
        } else {
            dexoptFlags = dexoptFlags2 | 128;
        }
        int dexoptFlags3 = dexoptFlags;
        Log.d(TAG, "Running dexopt on: " + str + " pkg=" + applicationInfo.packageName + " isa=" + dexUseInfo.getLoaderIsas() + " dexoptFlags=" + printDexoptFlags(dexoptFlags3) + " target-filter=" + compilerFilter);
        int reason = options.getCompilationReason();
        try {
            for (String isa : dexUseInfo.getLoaderIsas()) {
                Installer installer = this.mInstaller;
                int i = applicationInfo.uid;
                String str2 = applicationInfo.packageName;
                String str3 = applicationInfo.volumeUuid;
                String compilerFilter2 = applicationInfo.seInfoUser;
                String str4 = str3;
                int i2 = dexoptFlags3;
                int reason2 = reason;
                int dexoptFlags4 = dexoptFlags3;
                String str5 = str4;
                String compilerFilter3 = compilerFilter;
                try {
                    installer.dexopt(str, i, str2, isa, 0, null, i2, compilerFilter, str5, SKIP_SHARED_LIBRARY_CHECK, compilerFilter2, options.isDowngrade(), applicationInfo.targetSdkVersion, null, null, PackageManagerServiceCompilerMapping.getReasonName(reason));
                    str = path;
                    DexoptOptions dexoptOptions = options;
                    compilerFilter = compilerFilter3;
                    reason = reason2;
                    dexoptFlags3 = dexoptFlags4;
                } catch (Installer.InstallerException e) {
                    e = e;
                    Slog.w(TAG, "Failed to dexopt", e);
                    return -1;
                }
            }
            int i3 = dexoptFlags3;
            String str6 = compilerFilter;
            return 1;
        } catch (Installer.InstallerException e2) {
            e = e2;
            int i4 = reason;
            int i5 = dexoptFlags3;
            String str7 = compilerFilter;
            Slog.w(TAG, "Failed to dexopt", e);
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
            for (String isa : dexCodeInstructionSets) {
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
        boolean vmSafeMode = (info.flags & 16384) != 0;
        if (info.isPrivilegedApp() && DexManager.isPackageSelectedToRunOob(info.packageName)) {
            return "verify";
        }
        if (vmSafeMode) {
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
        return SystemProperties.get("dalvik.vm.appimageformat", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS).length() > 0;
    }

    private int getDexFlags(ApplicationInfo info, String compilerFilter, DexoptOptions options) {
        boolean generateAppImage = true;
        int i = 0;
        boolean debuggable = (info.flags & 2) != 0;
        boolean isProfileGuidedFilter = DexFile.isProfileGuidedCompilerFilter(compilerFilter);
        boolean isPublic = !info.isForwardLocked() && (!isProfileGuidedFilter || options.isDexoptInstallWithDexMetadata());
        int profileFlag = isProfileGuidedFilter ? 16 : 0;
        int hiddenApiFlag = info.getHiddenApiEnforcementPolicy() == 0 ? 0 : 1024;
        boolean generateCompactDex = true;
        switch (options.getCompilationReason()) {
            case 0:
            case 1:
            case 2:
                generateCompactDex = false;
                break;
        }
        if (!isProfileGuidedFilter || ((info.splitDependencies != null && info.requestsIsolatedSplitLoading()) || !isAppImageEnabled())) {
            generateAppImage = false;
        }
        int i2 = (isPublic ? 2 : 0) | (options.isForce() ? 64 : 0) | (debuggable ? 4 : 0) | profileFlag | (options.isBootComplete() ? 8 : 0) | (options.isDexoptIdleBackgroundJob() ? 512 : 0) | (generateCompactDex ? 2048 : 0);
        if (generateAppImage) {
            i = 4096;
        }
        return adjustDexoptFlags(i | i2 | hiddenApiFlag);
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
        if (!pkg.canHaveOatDir()) {
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
    public int mplDexOptPath(PackageParser.Package pkg, String path, String isa, String compilerFilter, boolean profileUpdated, String classLoaderContext, int dexoptFlags, int uid, CompilerStats.PackageStats packageStats, boolean downgrade, String profileName, String dexMetadataPath, int compilationReason, int dexoptNeeded) {
        return dexOptPath(pkg, path, isa, compilerFilter, profileUpdated, classLoaderContext, dexoptFlags, uid, packageStats, downgrade, profileName, dexMetadataPath, compilationReason, dexoptNeeded);
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
