package com.android.server.pm;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser.Package;
import android.os.FileUtils;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.WorkSource;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.connectivity.LingerMonitor;
import com.android.server.pm.Installer.InstallerException;
import dalvik.system.DexFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;

public class PackageDexOptimizer {
    public static final int DEX_OPT_FAILED = -1;
    public static final int DEX_OPT_PERFORMED = 1;
    public static final int DEX_OPT_SKIPPED = 0;
    static final String OAT_DIR_NAME = "oat";
    public static final String SKIP_SHARED_LIBRARY_CHECK = "&";
    private static final String TAG = "PackageManager.DexOptimizer";
    private static final long WAKELOCK_TIMEOUT_MS = (PackageManagerService.WATCHDOG_TIMEOUT + LingerMonitor.DEFAULT_NOTIFICATION_RATE_LIMIT_MILLIS);
    private long mDexOptTotalTime = 0;
    @GuardedBy("mInstallLock")
    private final WakeLock mDexoptWakeLock;
    private final Object mInstallLock;
    @GuardedBy("mInstallLock")
    private final Installer mInstaller;
    ArrayList<String> mPatchoatNeededApps = new ArrayList();
    private volatile boolean mSystemReady;

    public static class ForcedUpdatePackageDexOptimizer extends PackageDexOptimizer {
        public ForcedUpdatePackageDexOptimizer(Installer installer, Object installLock, Context context, String wakeLockTag) {
            super(installer, installLock, context, wakeLockTag);
        }

        public ForcedUpdatePackageDexOptimizer(PackageDexOptimizer from) {
            super(from);
        }

        protected int adjustDexoptNeeded(int dexoptNeeded) {
            if (dexoptNeeded == 0) {
                return -3;
            }
            return dexoptNeeded;
        }

        protected int adjustDexoptFlags(int flags) {
            return flags | 64;
        }
    }

    PackageDexOptimizer(Installer installer, Object installLock, Context context, String wakeLockTag) {
        this.mInstaller = installer;
        this.mInstallLock = installLock;
        this.mDexoptWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, wakeLockTag);
    }

    protected PackageDexOptimizer(PackageDexOptimizer from) {
        this.mInstaller = from.mInstaller;
        this.mInstallLock = from.mInstallLock;
        this.mDexoptWakeLock = from.mDexoptWakeLock;
        this.mSystemReady = from.mSystemReady;
    }

    static boolean canOptimizePackage(Package pkg) {
        return (pkg.applicationInfo.flags & 4) != 0;
    }

    int performDexOpt(Package pkg, String[] sharedLibraries, String[] instructionSets, boolean checkProfiles, String targetCompilationFilter, PackageStats packageStats, boolean isUsedByOtherApps) {
        if (!canOptimizePackage(pkg)) {
            return 0;
        }
        int performDexOptLI;
        synchronized (this.mInstallLock) {
            long acquireTime = acquireWakeLockLI(pkg.applicationInfo.uid);
            try {
                performDexOptLI = performDexOptLI(pkg, sharedLibraries, instructionSets, checkProfiles, targetCompilationFilter, packageStats, isUsedByOtherApps);
                releaseWakeLockLI(acquireTime);
            } catch (Throwable th) {
                releaseWakeLockLI(acquireTime);
            }
        }
        return performDexOptLI;
    }

    @GuardedBy("mInstallLock")
    private int performDexOptLI(Package pkg, String[] sharedLibraries, String[] targetInstructionSets, boolean checkForProfileUpdates, String targetCompilerFilter, PackageStats packageStats, boolean isUsedByOtherApps) {
        boolean profileUpdated;
        String[] dexCodeInstructionSets = InstructionSets.getDexCodeInstructionSets(targetInstructionSets != null ? targetInstructionSets : InstructionSets.getAppDexInstructionSets(pkg.applicationInfo));
        List<String> paths = pkg.getAllCodePaths();
        int sharedGid = UserHandle.getSharedAppGid(pkg.applicationInfo.uid);
        String compilerFilter = getRealCompilerFilter(pkg.applicationInfo, targetCompilerFilter, isUsedByOtherApps);
        if (checkForProfileUpdates) {
            profileUpdated = isProfileUpdated(pkg, sharedGid, compilerFilter);
        } else {
            profileUpdated = false;
        }
        String sharedLibrariesPath = getSharedLibrariesPath(sharedLibraries);
        int dexoptFlags = getDexFlags(pkg, compilerFilter);
        String[] splitDependencies = getSplitDependencies(pkg);
        int result = 0;
        int i = 0;
        while (i < paths.size()) {
            if (!(i == 0 && (pkg.applicationInfo.flags & 4) == 0) && (i == 0 || (pkg.splitFlags[i - 1] & 4) != 0)) {
                String path = (String) paths.get(i);
                String sharedLibrariesPathWithSplits = (sharedLibrariesPath == null || splitDependencies[i] == null) ? splitDependencies[i] != null ? splitDependencies[i] : sharedLibrariesPath : sharedLibrariesPath + ":" + splitDependencies[i];
                int i2 = 0;
                int length = dexCodeInstructionSets.length;
                while (true) {
                    int i3 = i2;
                    if (i3 >= length) {
                        break;
                    }
                    int newResult = dexOptPath(pkg, path, dexCodeInstructionSets[i3], compilerFilter, profileUpdated, sharedLibrariesPathWithSplits, dexoptFlags, sharedGid, packageStats);
                    if (!(result == -1 || newResult == 0)) {
                        result = newResult;
                    }
                    i2 = i3 + 1;
                }
            }
            i++;
        }
        return result;
    }

    @GuardedBy("mInstallLock")
    private int dexOptPath(Package pkg, String path, String isa, String compilerFilter, boolean profileUpdated, String sharedLibrariesPath, int dexoptFlags, int uid, PackageStats packageStats) {
        int dexoptNeeded = getDexoptNeeded(path, isa, compilerFilter, profileUpdated);
        if (Math.abs(dexoptNeeded) == 0) {
            return 0;
        }
        String oatDir = createOatDirIfSupported(pkg, isa);
        Log.i(TAG, "Running dexopt (dexoptNeeded=" + dexoptNeeded + ") on: " + path + " pkg=" + pkg.applicationInfo.packageName + " isa=" + isa + " dexoptFlags=" + printDexoptFlags(dexoptFlags) + " target-filter=" + compilerFilter + " oatDir=" + oatDir + " sharedLibraries=" + sharedLibrariesPath);
        try {
            long startTime = System.currentTimeMillis();
            this.mInstaller.dexopt(path, uid, pkg.packageName, isa, dexoptNeeded, oatDir, dexoptFlags, compilerFilter, pkg.volumeUuid, sharedLibrariesPath, pkg.applicationInfo.seInfo);
            if (packageStats != null) {
                packageStats.setCompileTime(path, (long) ((int) (System.currentTimeMillis() - startTime)));
            }
            return 1;
        } catch (Throwable e) {
            Slog.w(TAG, "Failed to dexopt", e);
            return -1;
        }
    }

    public int dexOptSecondaryDexPath(ApplicationInfo info, String path, Set<String> isas, String compilerFilter, boolean isUsedByOtherApps) {
        int dexOptSecondaryDexPathLI;
        synchronized (this.mInstallLock) {
            long acquireTime = acquireWakeLockLI(info.uid);
            try {
                dexOptSecondaryDexPathLI = dexOptSecondaryDexPathLI(info, path, isas, compilerFilter, isUsedByOtherApps);
                releaseWakeLockLI(acquireTime);
            } catch (Throwable th) {
                releaseWakeLockLI(acquireTime);
            }
        }
        return dexOptSecondaryDexPathLI;
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
    private int dexOptSecondaryDexPathLI(ApplicationInfo info, String path, Set<String> isas, String compilerFilter, boolean isUsedByOtherApps) {
        compilerFilter = getRealCompilerFilter(info, compilerFilter, isUsedByOtherApps);
        int dexoptFlags = getDexFlags(info, compilerFilter) | 32;
        if (info.deviceProtectedDataDir != null && FileUtils.contains(info.deviceProtectedDataDir, path)) {
            dexoptFlags |= 256;
        } else if (info.credentialProtectedDataDir == null || !FileUtils.contains(info.credentialProtectedDataDir, path)) {
            Slog.e(TAG, "Could not infer CE/DE storage for package " + info.packageName);
            return -1;
        } else {
            dexoptFlags |= 128;
        }
        Log.d(TAG, "Running dexopt on: " + path + " pkg=" + info.packageName + " isa=" + isas + " dexoptFlags=" + printDexoptFlags(dexoptFlags) + " target-filter=" + compilerFilter);
        try {
            for (String isa : isas) {
                String str = path;
                this.mInstaller.dexopt(str, info.uid, info.packageName, isa, 0, null, dexoptFlags, compilerFilter, info.volumeUuid, SKIP_SHARED_LIBRARY_CHECK, info.seInfoUser);
            }
            return 1;
        } catch (Throwable e) {
            Slog.w(TAG, "Failed to dexopt", e);
            return -1;
        }
    }

    protected int adjustDexoptNeeded(int dexoptNeeded) {
        return dexoptNeeded;
    }

    protected int adjustDexoptFlags(int dexoptFlags) {
        return dexoptFlags;
    }

    void dumpDexoptState(IndentingPrintWriter pw, Package pkg) {
        String[] dexCodeInstructionSets = InstructionSets.getDexCodeInstructionSets(InstructionSets.getAppDexInstructionSets(pkg.applicationInfo));
        List<String> paths = pkg.getAllCodePathsExcludingResourceOnly();
        for (String instructionSet : dexCodeInstructionSets) {
            pw.println("Instruction Set: " + instructionSet);
            pw.increaseIndent();
            for (String path : paths) {
                String status;
                try {
                    status = DexFile.getDexFileStatus(path, instructionSet);
                } catch (IOException ioe) {
                    status = "[Exception]: " + ioe.getMessage();
                }
                pw.println("path: " + path);
                pw.println("status: " + status);
            }
            pw.decreaseIndent();
        }
    }

    private String getRealCompilerFilter(ApplicationInfo info, String targetCompilerFilter, boolean isUsedByOtherApps) {
        if ((info.flags & 16384) != 0) {
            return DexFile.getSafeModeCompilerFilter(targetCompilerFilter);
        }
        if (DexFile.isProfileGuidedCompilerFilter(targetCompilerFilter) && isUsedByOtherApps) {
            return DexFile.getNonProfileGuidedCompilerFilter(targetCompilerFilter);
        }
        return targetCompilerFilter;
    }

    private int getDexFlags(Package pkg, String compilerFilter) {
        return getDexFlags(pkg.applicationInfo, compilerFilter);
    }

    private int getDexFlags(ApplicationInfo info, String compilerFilter) {
        int i;
        int i2 = 0;
        boolean debuggable = (info.flags & 2) != 0;
        boolean isProfileGuidedFilter = DexFile.isProfileGuidedCompilerFilter(compilerFilter);
        int isPublic = !info.isForwardLocked() ? isProfileGuidedFilter ^ 1 : 0;
        int profileFlag = isProfileGuidedFilter ? 16 : 0;
        if (isPublic != 0) {
            i = 2;
        } else {
            i = 0;
        }
        if (debuggable) {
            i2 = 4;
        }
        return adjustDexoptFlags(((i2 | i) | profileFlag) | 8);
    }

    private int getDexoptNeeded(String path, String isa, String compilerFilter, boolean newProfile) {
        try {
            return adjustDexoptNeeded(DexFile.getDexOptNeeded(path, isa, compilerFilter, newProfile));
        } catch (IOException ioe) {
            Slog.w(TAG, "IOException reading apk: " + path, ioe);
            return -1;
        }
    }

    private String getSharedLibrariesPath(String[] sharedLibraries) {
        if (sharedLibraries == null || sharedLibraries.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String lib : sharedLibraries) {
            if (sb.length() != 0) {
                sb.append(":");
            }
            sb.append(lib);
        }
        return sb.toString();
    }

    private String[] getSplitDependencies(Package pkg) {
        int i;
        String baseCodePath = new File(pkg.baseCodePath).getParent();
        List<String> paths = pkg.getAllCodePaths();
        String[] splitDependencies = new String[paths.size()];
        for (i = 0; i < paths.size(); i++) {
            File pathFile = new File((String) paths.get(i));
            paths.set(i, pathFile.getName());
            String basePath = pathFile.getParent();
            if (!basePath.equals(baseCodePath)) {
                Slog.wtf(TAG, "Split paths have different base paths: " + basePath + " and " + baseCodePath);
            }
        }
        SparseArray<int[]> dependencies = pkg.applicationInfo.splitDependencies;
        if (dependencies == null) {
            for (i = 1; i < paths.size(); i++) {
                splitDependencies[i] = (String) paths.get(0);
            }
            return splitDependencies;
        }
        for (i = 1; i < dependencies.size(); i++) {
            getParentDependencies(dependencies.keyAt(i), paths, dependencies, splitDependencies);
        }
        return splitDependencies;
    }

    private String getParentDependencies(int index, List<String> paths, SparseArray<int[]> dependencies, String[] splitDependencies) {
        if (index == 0) {
            return null;
        }
        if (splitDependencies[index] != null) {
            return splitDependencies[index];
        }
        String path;
        int parent = ((int[]) dependencies.get(index))[0];
        String parentDependencies = getParentDependencies(parent, paths, dependencies, splitDependencies);
        if (parentDependencies == null) {
            path = (String) paths.get(parent);
        } else {
            path = parentDependencies + ":" + ((String) paths.get(parent));
        }
        splitDependencies[index] = path;
        return path;
    }

    private boolean isProfileUpdated(Package pkg, int uid, String compilerFilter) {
        if (!DexFile.isProfileGuidedCompilerFilter(compilerFilter)) {
            return false;
        }
        try {
            return this.mInstaller.mergeProfiles(uid, pkg.packageName);
        } catch (InstallerException e) {
            Slog.w(TAG, "Failed to merge profiles", e);
            return false;
        }
    }

    private String createOatDirIfSupported(Package pkg, String dexInstructionSet) {
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
        } catch (InstallerException e) {
            Slog.w(TAG, "Failed to create oat dir", e);
            return null;
        }
    }

    static File getOatDir(File codePath) {
        return new File(codePath, OAT_DIR_NAME);
    }

    void systemReady() {
        this.mSystemReady = true;
    }

    private String printDexoptFlags(int flags) {
        ArrayList<String> flagsList = new ArrayList();
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
        return String.join(",", flagsList);
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x001a A:{Splitter: B:1:0x0002, ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x004b A:{Splitter: B:3:0x0007, ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0041 A:{SYNTHETIC, Splitter: B:24:0x0041} */
    /* JADX WARNING: Missing block: B:13:0x001a, code:
            r3 = e;
     */
    /* JADX WARNING: Missing block: B:15:?, code:
            android.util.Slog.w(TAG, "Exception reading apk: " + r8, r3);
     */
    /* JADX WARNING: Missing block: B:16:0x0035, code:
            if (r0 != null) goto L_0x0037;
     */
    /* JADX WARNING: Missing block: B:18:?, code:
            r0.close();
     */
    /* JADX WARNING: Missing block: B:22:0x003e, code:
            r4 = th;
     */
    /* JADX WARNING: Missing block: B:31:0x004b, code:
            r3 = e;
     */
    /* JADX WARNING: Missing block: B:32:0x004c, code:
            r0 = r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean dexEntryExists(String path) {
        boolean z = false;
        ZipFile apkFile = null;
        try {
            ZipFile apkFile2 = new ZipFile(path);
            try {
                if (apkFile2.getEntry("classes.dex") != null) {
                    z = true;
                }
                if (apkFile2 != null) {
                    try {
                        apkFile2.close();
                    } catch (IOException e) {
                    }
                }
                return z;
            } catch (IOException e2) {
            } catch (Throwable th) {
                Throwable th2 = th;
                apkFile = apkFile2;
                if (apkFile != null) {
                    try {
                        apkFile.close();
                    } catch (IOException e3) {
                    }
                }
                throw th2;
            }
        } catch (IOException e4) {
        }
        return false;
    }

    public long getDexOptTotalTime() {
        return this.mDexOptTotalTime;
    }

    public ArrayList<String> getPatchoatNeededApps() {
        return this.mPatchoatNeededApps;
    }
}
