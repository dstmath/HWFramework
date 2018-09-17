package com.android.server.pm;

import android.content.Context;
import android.content.pm.IOtaDexopt.Stub;
import android.content.pm.PackageParser.Package;
import android.os.Environment;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.storage.StorageManager;
import android.util.Log;
import android.util.Slog;
import com.android.internal.os.InstallerConnection.InstallerException;
import com.android.server.pm.PackageDexOptimizer.ForcedUpdatePackageDexOptimizer;
import java.io.File;
import java.io.FileDescriptor;
import java.util.List;

public class OtaDexoptService extends Stub {
    private static final boolean DEBUG_DEXOPT = true;
    private static final String TAG = "OTADexopt";
    private final Context mContext;
    private List<Package> mDexoptPackages;
    private final PackageDexOptimizer mPackageDexOptimizer;
    private final PackageManagerService mPackageManagerService;

    private static class OTADexoptPackageDexOptimizer extends ForcedUpdatePackageDexOptimizer {
        public OTADexoptPackageDexOptimizer(Installer installer, Object installLock, Context context) {
            super(installer, installLock, context, "*otadexopt*");
        }

        protected int adjustDexoptFlags(int dexoptFlags) {
            return dexoptFlags | 64;
        }
    }

    public OtaDexoptService(Context context, PackageManagerService packageManagerService) {
        this.mContext = context;
        this.mPackageManagerService = packageManagerService;
        this.mPackageDexOptimizer = new OTADexoptPackageDexOptimizer(packageManagerService.mInstaller, packageManagerService.mInstallLock, context);
        moveAbArtifacts(packageManagerService.mInstaller);
    }

    public static OtaDexoptService main(Context context, PackageManagerService packageManagerService) {
        OtaDexoptService ota = new OtaDexoptService(context, packageManagerService);
        ServiceManager.addService("otadexopt", ota);
        return ota;
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ResultReceiver resultReceiver) throws RemoteException {
        new OtaDexoptShellCommand(this).exec(this, in, out, err, args, resultReceiver);
    }

    public synchronized void prepare() throws RemoteException {
        if (this.mDexoptPackages != null) {
            throw new IllegalStateException("already called prepare()");
        }
        synchronized (this.mPackageManagerService.mPackages) {
            this.mDexoptPackages = PackageManagerServiceUtils.getPackagesForDexopt(this.mPackageManagerService.mPackages.values(), this.mPackageManagerService);
        }
    }

    public synchronized void cleanup() throws RemoteException {
        Log.i(TAG, "Cleaning up OTA Dexopt state.");
        this.mDexoptPackages = null;
    }

    public synchronized boolean isDone() throws RemoteException {
        if (this.mDexoptPackages == null) {
            throw new IllegalStateException("done() called before prepare()");
        }
        return this.mDexoptPackages.isEmpty();
    }

    public synchronized void dexoptNextPackage() throws RemoteException {
        if (this.mDexoptPackages == null) {
            throw new IllegalStateException("dexoptNextPackage() called before prepare()");
        } else if (!this.mDexoptPackages.isEmpty()) {
            Package nextPackage = (Package) this.mDexoptPackages.remove(0);
            Log.i(TAG, "Processing " + nextPackage.packageName + " for OTA dexopt.");
            File dataDir = Environment.getDataDirectory();
            long lowThreshold = StorageManager.from(this.mContext).getStorageLowBytes(dataDir);
            if (lowThreshold == 0) {
                throw new IllegalStateException("Invalid low memory threshold");
            }
            long usableSpace = dataDir.getUsableSpace();
            if (usableSpace < lowThreshold) {
                Log.w(TAG, "Not running dexopt on " + nextPackage.packageName + " due to low memory: " + usableSpace);
            } else {
                this.mPackageDexOptimizer.performDexOpt(nextPackage, nextPackage.usesLibraryFiles, null, false, PackageManagerServiceCompilerMapping.getCompilerFilterForReason(4));
            }
        }
    }

    private void moveAbArtifacts(Installer installer) {
        if (this.mDexoptPackages != null) {
            throw new IllegalStateException("Should not be ota-dexopting when trying to move.");
        }
        for (Package pkg : this.mPackageManagerService.getPackages()) {
            if (pkg != null && PackageDexOptimizer.canOptimizePackage(pkg)) {
                if (pkg.codePath == null) {
                    Slog.w(TAG, "Package " + pkg + " can be optimized but has null codePath");
                } else if (!(pkg.codePath.startsWith("/system") || pkg.codePath.startsWith("/vendor"))) {
                    String[] instructionSets = InstructionSets.getAppDexInstructionSets(pkg.applicationInfo);
                    List<String> paths = pkg.getAllCodePathsExcludingResourceOnly();
                    for (String dexCodeInstructionSet : InstructionSets.getDexCodeInstructionSets(instructionSets)) {
                        for (String path : paths) {
                            try {
                                installer.moveAb(path, dexCodeInstructionSet, PackageDexOptimizer.getOatDir(new File(pkg.codePath)).getAbsolutePath());
                            } catch (InstallerException e) {
                            }
                        }
                    }
                }
            }
        }
    }
}
