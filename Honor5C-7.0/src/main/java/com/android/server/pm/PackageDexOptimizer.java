package com.android.server.pm;

import android.content.Context;
import android.content.pm.PackageParser.Package;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.WorkSource;
import android.util.Log;
import android.util.Slog;
import com.android.internal.os.InstallerConnection.InstallerException;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;
import dalvik.system.DexFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

class PackageDexOptimizer {
    static final int DEX_OPT_FAILED = -1;
    static final int DEX_OPT_PERFORMED = 1;
    static final int DEX_OPT_SKIPPED = 0;
    static final String OAT_DIR_NAME = "oat";
    private static final String TAG = "PackageManager.DexOptimizer";
    private long mDexOptTotalTime;
    private final WakeLock mDexoptWakeLock;
    private final Object mInstallLock;
    private final Installer mInstaller;
    ArrayList<String> mPatchoatNeededApps;
    private volatile boolean mSystemReady;

    public static class ForcedUpdatePackageDexOptimizer extends PackageDexOptimizer {
        public /* bridge */ /* synthetic */ long getDexOptTotalTime() {
            return super.getDexOptTotalTime();
        }

        public /* bridge */ /* synthetic */ ArrayList getPatchoatNeededApps() {
            return super.getPatchoatNeededApps();
        }

        public ForcedUpdatePackageDexOptimizer(Installer installer, Object installLock, Context context, String wakeLockTag) {
            super(installer, installLock, context, wakeLockTag);
        }

        public ForcedUpdatePackageDexOptimizer(PackageDexOptimizer from) {
            super(from);
        }

        protected int adjustDexoptNeeded(int dexoptNeeded) {
            return PackageDexOptimizer.DEX_OPT_PERFORMED;
        }
    }

    PackageDexOptimizer(Installer installer, Object installLock, Context context, String wakeLockTag) {
        this.mPatchoatNeededApps = new ArrayList();
        this.mDexOptTotalTime = 0;
        this.mInstaller = installer;
        this.mInstallLock = installLock;
        this.mDexoptWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(DEX_OPT_PERFORMED, wakeLockTag);
    }

    protected PackageDexOptimizer(PackageDexOptimizer from) {
        this.mPatchoatNeededApps = new ArrayList();
        this.mDexOptTotalTime = 0;
        this.mInstaller = from.mInstaller;
        this.mInstallLock = from.mInstallLock;
        this.mDexoptWakeLock = from.mDexoptWakeLock;
        this.mSystemReady = from.mSystemReady;
    }

    static boolean canOptimizePackage(Package pkg) {
        return (pkg.applicationInfo.flags & 4) != 0;
    }

    int performDexOpt(Package pkg, String[] sharedLibraries, String[] instructionSets, boolean checkProfiles, String targetCompilationFilter) {
        int performDexOptLI;
        synchronized (this.mInstallLock) {
            boolean useLock = this.mSystemReady;
            if (useLock) {
                this.mDexoptWakeLock.setWorkSource(new WorkSource(pkg.applicationInfo.uid));
                this.mDexoptWakeLock.acquire();
            }
            try {
                performDexOptLI = performDexOptLI(pkg, sharedLibraries, instructionSets, checkProfiles, targetCompilationFilter);
                if (useLock) {
                    this.mDexoptWakeLock.release();
                }
            } catch (Throwable th) {
                if (useLock) {
                    this.mDexoptWakeLock.release();
                }
            }
        }
        return performDexOptLI;
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
        int length = dexCodeInstructionSets.length;
        for (int i = DEX_OPT_SKIPPED; i < length; i += DEX_OPT_PERFORMED) {
            String instructionSet = dexCodeInstructionSets[i];
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

    private int performDexOptLI(Package pkg, String[] sharedLibraries, String[] targetInstructionSets, boolean checkProfiles, String targetCompilerFilter) {
        String[] instructionSets = targetInstructionSets != null ? targetInstructionSets : InstructionSets.getAppDexInstructionSets(pkg.applicationInfo);
        if (!canOptimizePackage(pkg)) {
            return DEX_OPT_SKIPPED;
        }
        List<String> paths = pkg.getAllCodePathsExcludingResourceOnly();
        int sharedGid = UserHandle.getSharedAppGid(pkg.applicationInfo.uid);
        boolean isProfileGuidedFilter = DexFile.isProfileGuidedCompilerFilter(targetCompilerFilter);
        if (isProfileGuidedFilter && isUsedByOtherApps(pkg)) {
            checkProfiles = false;
            targetCompilerFilter = PackageManagerServiceCompilerMapping.getNonProfileGuidedCompilerFilter(targetCompilerFilter);
            if (DexFile.isProfileGuidedCompilerFilter(targetCompilerFilter)) {
                throw new IllegalStateException(targetCompilerFilter);
            }
            isProfileGuidedFilter = false;
        }
        boolean vmSafeMode = (pkg.applicationInfo.flags & DumpState.DUMP_KEYSETS) != 0;
        boolean debuggable = (pkg.applicationInfo.flags & 2) != 0;
        if (vmSafeMode) {
            targetCompilerFilter = PackageManagerServiceCompilerMapping.getNonProfileGuidedCompilerFilter(targetCompilerFilter);
            isProfileGuidedFilter = false;
        }
        boolean newProfile = false;
        if (checkProfiles && isProfileGuidedFilter) {
            try {
                newProfile = this.mInstaller.mergeProfiles(sharedGid, pkg.packageName);
            } catch (InstallerException e) {
                Slog.w(TAG, "Failed to merge profiles", e);
            }
        }
        boolean performedDexOpt = false;
        boolean successfulDexOpt = true;
        String[] dexCodeInstructionSets = InstructionSets.getDexCodeInstructionSets(instructionSets);
        int length = dexCodeInstructionSets.length;
        for (int i = DEX_OPT_SKIPPED; i < length; i += DEX_OPT_PERFORMED) {
            String dexCodeInstructionSet = dexCodeInstructionSets[i];
            for (String path : paths) {
                try {
                    String dexoptType;
                    int dexoptNeeded = adjustDexoptNeeded(DexFile.getDexOptNeeded(path, dexCodeInstructionSet, targetCompilerFilter, newProfile));
                    String str = null;
                    switch (dexoptNeeded) {
                        case DEX_OPT_SKIPPED /*0*/:
                            break;
                        case DEX_OPT_PERFORMED /*1*/:
                            if (dexEntryExists(path)) {
                                dexoptType = "dex2oat";
                                str = createOatDirIfSupported(pkg, dexCodeInstructionSet);
                                break;
                            }
                            return DEX_OPT_FAILED;
                        case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                            dexoptType = "patchoat";
                            break;
                        case H.REPORT_LOSING_FOCUS /*3*/:
                            dexoptType = "self patchoat";
                            break;
                        default:
                            throw new IllegalStateException("Invalid dexopt:" + dexoptNeeded);
                    }
                    String str2 = null;
                    if (!(sharedLibraries == null || sharedLibraries.length == 0)) {
                        StringBuilder sb = new StringBuilder();
                        int length2 = sharedLibraries.length;
                        for (int i2 = DEX_OPT_SKIPPED; i2 < length2; i2 += DEX_OPT_PERFORMED) {
                            String lib = sharedLibraries[i2];
                            if (sb.length() != 0) {
                                sb.append(":");
                            }
                            sb.append(lib);
                        }
                        str2 = sb.toString();
                    }
                    Log.i(TAG, "Running dexopt (" + dexoptType + ") on: " + path + " pkg=" + pkg.applicationInfo.packageName + " isa=" + dexCodeInstructionSet + " vmSafeMode=" + vmSafeMode + " debuggable=" + debuggable + " target-filter=" + targetCompilerFilter + " oatDir = " + str + " sharedLibraries=" + str2);
                    boolean isPublic = (pkg.isForwardLocked() || isProfileGuidedFilter) ? false : true;
                    int dexFlags = adjustDexoptFlags((((debuggable ? 8 : DEX_OPT_SKIPPED) | ((isPublic ? 2 : DEX_OPT_SKIPPED) | (vmSafeMode ? 4 : DEX_OPT_SKIPPED))) | (isProfileGuidedFilter ? 32 : DEX_OPT_SKIPPED)) | 16);
                    try {
                        long dexoptStartTime = SystemClock.uptimeMillis();
                        this.mInstaller.dexopt(path, sharedGid, pkg.packageName, dexCodeInstructionSet, dexoptNeeded, str, dexFlags, targetCompilerFilter, pkg.volumeUuid, str2);
                        this.mDexOptTotalTime += SystemClock.uptimeMillis() - dexoptStartTime;
                        performedDexOpt = true;
                    } catch (InstallerException e2) {
                        Slog.w(TAG, "Failed to dexopt", e2);
                        successfulDexOpt = false;
                    }
                } catch (Throwable ioe) {
                    Slog.w(TAG, "IOException reading apk: " + path, ioe);
                    return DEX_OPT_FAILED;
                }
            }
        }
        if (!successfulDexOpt) {
            return DEX_OPT_FAILED;
        }
        return performedDexOpt ? DEX_OPT_PERFORMED : DEX_OPT_SKIPPED;
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

    public static boolean isUsedByOtherApps(Package pkg) {
        if (pkg.isForwardLocked()) {
            return false;
        }
        for (String apkPath : pkg.getAllCodePathsExcludingResourceOnly()) {
            try {
                String useMarker = PackageManagerServiceUtils.realpath(new File(apkPath)).replace('/', '@');
                int[] currentUserIds = UserManagerService.getInstance().getUserIds();
                for (int i = DEX_OPT_SKIPPED; i < currentUserIds.length; i += DEX_OPT_PERFORMED) {
                    if (new File(Environment.getDataProfilesDeForeignDexDirectory(currentUserIds[i]), useMarker).exists()) {
                        return true;
                    }
                }
                continue;
            } catch (IOException e) {
                Slog.w(TAG, "Failed to get canonical path", e);
            }
        }
        return false;
    }

    private static boolean dexEntryExists(String path) {
        Exception e;
        Throwable th;
        boolean z = false;
        ZipFile zipFile = null;
        try {
            ZipFile apkFile = new ZipFile(path);
            try {
                if (apkFile.getEntry("classes.dex") != null) {
                    z = true;
                }
                if (apkFile != null) {
                    try {
                        apkFile.close();
                    } catch (IOException e2) {
                    }
                }
                return z;
            } catch (IOException e3) {
                e = e3;
                zipFile = apkFile;
                try {
                    Slog.w(TAG, "Exception reading apk: " + path, e);
                    if (zipFile != null) {
                        try {
                            zipFile.close();
                        } catch (IOException e4) {
                        }
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (zipFile != null) {
                        try {
                            zipFile.close();
                        } catch (IOException e5) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                zipFile = apkFile;
                if (zipFile != null) {
                    zipFile.close();
                }
                throw th;
            }
        } catch (IOException e6) {
            e = e6;
            Slog.w(TAG, "Exception reading apk: " + path, e);
            if (zipFile != null) {
                zipFile.close();
            }
            return false;
        }
    }

    public long getDexOptTotalTime() {
        return this.mDexOptTotalTime;
    }

    public ArrayList<String> getPatchoatNeededApps() {
        return this.mPatchoatNeededApps;
    }
}
