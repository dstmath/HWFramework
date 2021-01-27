package com.android.server.pm;

import android.content.Context;
import android.content.pm.IOtaDexopt;
import android.content.pm.PackageParser;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.storage.StorageManager;
import android.util.Log;
import android.util.Slog;
import com.android.internal.logging.MetricsLogger;
import com.android.server.pm.Installer;
import com.android.server.pm.PackageDexOptimizer;
import com.android.server.pm.dex.DexoptOptions;
import com.android.server.slice.SliceClientPermissions;
import java.io.File;
import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OtaDexoptService extends IOtaDexopt.Stub {
    private static final long BULK_DELETE_THRESHOLD = 1073741824;
    private static final boolean DEBUG_DEXOPT = true;
    private static final String TAG = "OTADexopt";
    private long availableSpaceAfterBulkDelete;
    private long availableSpaceAfterDexopt;
    private long availableSpaceBefore;
    private int completeSize;
    private int dexoptCommandCountExecuted;
    private int dexoptCommandCountTotal;
    private int importantPackageCount;
    private final Context mContext;
    private List<String> mDexoptCommands;
    private final PackageManagerService mPackageManagerService;
    private final MetricsLogger metricsLogger = new MetricsLogger();
    private long otaDexoptTimeStart;
    private int otherPackageCount;

    public OtaDexoptService(Context context, PackageManagerService packageManagerService) {
        this.mContext = context;
        this.mPackageManagerService = packageManagerService;
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.pm.OtaDexoptService, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public static OtaDexoptService main(Context context, PackageManagerService packageManagerService) {
        ?? otaDexoptService = new OtaDexoptService(context, packageManagerService);
        ServiceManager.addService("otadexopt", (IBinder) otaDexoptService);
        otaDexoptService.moveAbArtifacts(packageManagerService.mInstaller);
        return otaDexoptService;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: com.android.server.pm.OtaDexoptService */
    /* JADX WARN: Multi-variable type inference failed */
    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        new OtaDexoptShellCommand(this).exec(this, in, out, err, args, callback, resultReceiver);
    }

    public synchronized void prepare() throws RemoteException {
        List<PackageParser.Package> important;
        List<PackageParser.Package> others;
        if (this.mDexoptCommands == null) {
            synchronized (this.mPackageManagerService.mPackages) {
                important = PackageManagerServiceUtils.getPackagesForDexopt(this.mPackageManagerService.mPackages.values(), this.mPackageManagerService, true);
                others = new ArrayList<>((Collection<? extends PackageParser.Package>) this.mPackageManagerService.mPackages.values());
                others.removeAll(important);
                this.mDexoptCommands = new ArrayList((this.mPackageManagerService.mPackages.size() * 3) / 2);
            }
            for (PackageParser.Package p : important) {
                this.mDexoptCommands.addAll(generatePackageDexopts(p, 4));
            }
            for (PackageParser.Package p2 : others) {
                if (!p2.coreApp) {
                    this.mDexoptCommands.addAll(generatePackageDexopts(p2, 0));
                } else {
                    throw new IllegalStateException("Found a core app that's not important");
                }
            }
            this.completeSize = this.mDexoptCommands.size();
            long spaceAvailable = getAvailableSpace();
            if (spaceAvailable < BULK_DELETE_THRESHOLD) {
                Log.i(TAG, "Low on space, deleting oat files in an attempt to free up space: " + PackageManagerServiceUtils.packagesToString(others));
                for (PackageParser.Package pkg : others) {
                    this.mPackageManagerService.deleteOatArtifactsOfPackage(pkg.packageName);
                }
            }
            prepareMetricsLogging(important.size(), others.size(), spaceAvailable, getAvailableSpace());
            try {
                Log.d(TAG, "A/B OTA: lastUsed time = " + ((PackageParser.Package) Collections.max(important, $$Lambda$OtaDexoptService$ZaCsBw0Yn3yN1RRrIRZVKyDrWE.INSTANCE)).getLatestForegroundPackageUseTimeInMills());
                Log.d(TAG, "A/B OTA: deprioritized packages:");
                for (PackageParser.Package pkg2 : others) {
                    Log.d(TAG, "  " + pkg2.packageName + " - " + pkg2.getLatestForegroundPackageUseTimeInMills());
                }
            } catch (Exception e) {
            }
        } else {
            throw new IllegalStateException("already called prepare()");
        }
    }

    public synchronized void cleanup() throws RemoteException {
        Log.i(TAG, "Cleaning up OTA Dexopt state.");
        this.mDexoptCommands = null;
        this.availableSpaceAfterDexopt = getAvailableSpace();
        performMetricsLogging();
    }

    public synchronized boolean isDone() throws RemoteException {
        if (this.mDexoptCommands != null) {
        } else {
            throw new IllegalStateException("done() called before prepare()");
        }
        return this.mDexoptCommands.isEmpty();
    }

    public synchronized float getProgress() throws RemoteException {
        if (this.completeSize == 0) {
            return 1.0f;
        }
        return ((float) (this.completeSize - this.mDexoptCommands.size())) / ((float) this.completeSize);
    }

    public synchronized String nextDexoptCommand() throws RemoteException {
        if (this.mDexoptCommands == null) {
            throw new IllegalStateException("dexoptNextPackage() called before prepare()");
        } else if (this.mDexoptCommands.isEmpty()) {
            return "(all done)";
        } else {
            String next = this.mDexoptCommands.remove(0);
            if (getAvailableSpace() > 0) {
                this.dexoptCommandCountExecuted++;
                Log.d(TAG, "Next command: " + next);
                return next;
            }
            Log.w(TAG, "Not enough space for OTA dexopt, stopping with " + (this.mDexoptCommands.size() + 1) + " commands left.");
            this.mDexoptCommands.clear();
            return "(no free space)";
        }
    }

    private long getMainLowSpaceThreshold() {
        long lowThreshold = StorageManager.from(this.mContext).getStorageLowBytes(Environment.getDataDirectory());
        if (lowThreshold != 0) {
            return lowThreshold;
        }
        throw new IllegalStateException("Invalid low memory threshold");
    }

    private long getAvailableSpace() {
        return Environment.getDataDirectory().getUsableSpace() - getMainLowSpaceThreshold();
    }

    private synchronized List<String> generatePackageDexopts(PackageParser.Package pkg, int compilationReason) {
        final List<String> commands;
        commands = new ArrayList<>();
        new OTADexoptPackageDexOptimizer(new Installer(this.mContext, true) {
            /* class com.android.server.pm.OtaDexoptService.AnonymousClass1 */

            @Override // com.android.server.pm.Installer
            public void dexopt(String apkPath, int uid, String pkgName, String instructionSet, int dexoptNeeded, String outputPath, int dexFlags, String compilerFilter, String volumeUuid, String sharedLibraries, String seInfo, boolean downgrade, int targetSdkVersion, String profileName, String dexMetadataPath, String dexoptCompilationReason) throws Installer.InstallerException {
                StringBuilder builder = new StringBuilder();
                builder.append("10 ");
                builder.append("dexopt");
                encodeParameter(builder, apkPath);
                encodeParameter(builder, Integer.valueOf(uid));
                encodeParameter(builder, pkgName);
                encodeParameter(builder, instructionSet);
                encodeParameter(builder, Integer.valueOf(dexoptNeeded));
                encodeParameter(builder, outputPath);
                encodeParameter(builder, Integer.valueOf(dexFlags));
                encodeParameter(builder, compilerFilter);
                encodeParameter(builder, volumeUuid);
                encodeParameter(builder, sharedLibraries);
                encodeParameter(builder, seInfo);
                encodeParameter(builder, Boolean.valueOf(downgrade));
                encodeParameter(builder, Integer.valueOf(targetSdkVersion));
                encodeParameter(builder, profileName);
                encodeParameter(builder, dexMetadataPath);
                encodeParameter(builder, dexoptCompilationReason);
                commands.add(builder.toString());
            }

            private void encodeParameter(StringBuilder builder, Object arg) {
                builder.append(' ');
                if (arg == null) {
                    builder.append('!');
                    return;
                }
                String txt = String.valueOf(arg);
                if (txt.indexOf(0) == -1 && txt.indexOf(32) == -1 && !"!".equals(txt)) {
                    builder.append(txt);
                    return;
                }
                throw new IllegalArgumentException("Invalid argument while executing " + arg);
            }
        }, this.mPackageManagerService.mInstallLock, this.mContext).performDexOpt(pkg, null, null, this.mPackageManagerService.getDexManager().getPackageUseInfoOrDefault(pkg.packageName), new DexoptOptions(pkg.packageName, compilationReason, 4));
        return commands;
    }

    public synchronized void dexoptNextPackage() throws RemoteException {
        throw new UnsupportedOperationException();
    }

    private void moveAbArtifacts(Installer installer) {
        if (this.mDexoptCommands != null) {
            throw new IllegalStateException("Should not be ota-dexopting when trying to move.");
        } else if (!this.mPackageManagerService.isDeviceUpgrading()) {
            Slog.d(TAG, "No upgrade, skipping A/B artifacts check.");
        } else {
            Collection<PackageParser.Package> pkgs = this.mPackageManagerService.getPackages();
            int packagePaths = 0;
            int pathsSuccessful = 0;
            for (PackageParser.Package pkg : pkgs) {
                if (pkg != null && PackageDexOptimizer.canOptimizePackage(pkg)) {
                    if (pkg.codePath == null) {
                        Slog.w(TAG, "Package " + pkg + " can be optimized but has null codePath");
                    } else if (!pkg.codePath.startsWith("/system") && !pkg.codePath.startsWith("/vendor") && !pkg.codePath.startsWith("/product") && !pkg.codePath.startsWith("/product_services")) {
                        String[] instructionSets = InstructionSets.getAppDexInstructionSets(pkg.applicationInfo);
                        List<String> paths = pkg.getAllCodePathsExcludingResourceOnly();
                        String[] dexCodeInstructionSets = InstructionSets.getDexCodeInstructionSets(instructionSets);
                        int length = dexCodeInstructionSets.length;
                        int i = 0;
                        while (i < length) {
                            String dexCodeInstructionSet = dexCodeInstructionSets[i];
                            for (String path : paths) {
                                int packagePaths2 = packagePaths + 1;
                                try {
                                    installer.moveAb(path, dexCodeInstructionSet, PackageDexOptimizer.getOatDir(new File(pkg.codePath)).getAbsolutePath());
                                    pathsSuccessful++;
                                } catch (Installer.InstallerException e) {
                                }
                                packagePaths = packagePaths2;
                                pkgs = pkgs;
                            }
                            i++;
                            pkgs = pkgs;
                        }
                        pkgs = pkgs;
                    }
                }
                pkgs = pkgs;
            }
            Slog.i(TAG, "Moved " + pathsSuccessful + SliceClientPermissions.SliceAuthority.DELIMITER + packagePaths);
        }
    }

    private void prepareMetricsLogging(int important, int others, long spaceBegin, long spaceBulk) {
        this.availableSpaceBefore = spaceBegin;
        this.availableSpaceAfterBulkDelete = spaceBulk;
        this.availableSpaceAfterDexopt = 0;
        this.importantPackageCount = important;
        this.otherPackageCount = others;
        this.dexoptCommandCountTotal = this.mDexoptCommands.size();
        this.dexoptCommandCountExecuted = 0;
        this.otaDexoptTimeStart = System.nanoTime();
    }

    private static int inMegabytes(long value) {
        long in_mega_bytes = value / 1048576;
        if (in_mega_bytes <= 2147483647L) {
            return (int) in_mega_bytes;
        }
        Log.w(TAG, "Recording " + in_mega_bytes + "MB of free space, overflowing range");
        return Integer.MAX_VALUE;
    }

    private void performMetricsLogging() {
        long finalTime = System.nanoTime();
        this.metricsLogger.histogram("ota_dexopt_available_space_before_mb", inMegabytes(this.availableSpaceBefore));
        this.metricsLogger.histogram("ota_dexopt_available_space_after_bulk_delete_mb", inMegabytes(this.availableSpaceAfterBulkDelete));
        this.metricsLogger.histogram("ota_dexopt_available_space_after_dexopt_mb", inMegabytes(this.availableSpaceAfterDexopt));
        this.metricsLogger.histogram("ota_dexopt_num_important_packages", this.importantPackageCount);
        this.metricsLogger.histogram("ota_dexopt_num_other_packages", this.otherPackageCount);
        this.metricsLogger.histogram("ota_dexopt_num_commands", this.dexoptCommandCountTotal);
        this.metricsLogger.histogram("ota_dexopt_num_commands_executed", this.dexoptCommandCountExecuted);
        this.metricsLogger.histogram("ota_dexopt_time_s", (int) TimeUnit.NANOSECONDS.toSeconds(finalTime - this.otaDexoptTimeStart));
    }

    /* access modifiers changed from: private */
    public static class OTADexoptPackageDexOptimizer extends PackageDexOptimizer.ForcedUpdatePackageDexOptimizer {
        public OTADexoptPackageDexOptimizer(Installer installer, Object installLock, Context context) {
            super(installer, installLock, context, "*otadexopt*");
        }
    }
}
