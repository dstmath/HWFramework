package com.android.server.pm;

import android.content.Context;
import android.content.pm.PackageStats;
import android.os.Build;
import android.os.IBackupSessionCallback;
import android.os.IBinder;
import android.os.IInstalld;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import com.android.internal.os.BackgroundThread;
import com.android.server.SystemService;
import dalvik.system.VMRuntime;
import java.io.FileDescriptor;

public class Installer extends SystemService {
    public static final int DEXOPT_BOOTCOMPLETE = 8;
    public static final int DEXOPT_DEBUGGABLE = 4;
    public static final int DEXOPT_ENABLE_HIDDEN_API_CHECKS = 1024;
    public static final int DEXOPT_FORCE = 64;
    public static final int DEXOPT_GENERATE_APP_IMAGE = 4096;
    public static final int DEXOPT_GENERATE_COMPACT_DEX = 2048;
    public static final int DEXOPT_IDLE_BACKGROUND_JOB = 512;
    public static final int DEXOPT_PROFILE_GUIDED = 16;
    public static final int DEXOPT_PUBLIC = 2;
    public static final int DEXOPT_SECONDARY_DEX = 32;
    public static final int DEXOPT_STORAGE_CE = 128;
    public static final int DEXOPT_STORAGE_DE = 256;
    public static final int FLAG_CLEAR_CACHE_ONLY = 256;
    public static final int FLAG_CLEAR_CODE_CACHE_ONLY = 512;
    public static final int FLAG_FORCE = 65536;
    public static final int FLAG_FREE_CACHE_NOOP = 32768;
    public static final int FLAG_FREE_CACHE_V2 = 8192;
    public static final int FLAG_FREE_CACHE_V2_DEFY_QUOTA = 16384;
    public static final int FLAG_USE_QUOTA = 4096;
    private static final String TAG = "Installer";
    private volatile IInstalld mInstalld;
    private final boolean mIsolated;
    private volatile Object mWarnIfHeld;

    public static class InstallerException extends Exception {
        public InstallerException(String detailMessage) {
            super(detailMessage);
        }

        public static InstallerException from(Exception e) throws InstallerException {
            throw new InstallerException(e.toString());
        }
    }

    public Installer(Context context) {
        this(context, false);
    }

    public Installer(Context context, boolean isolated) {
        super(context);
        this.mIsolated = isolated;
    }

    public void setWarnIfHeld(Object warnIfHeld) {
        this.mWarnIfHeld = warnIfHeld;
    }

    public void onStart() {
        if (this.mIsolated) {
            this.mInstalld = null;
        } else {
            connect();
        }
    }

    /* access modifiers changed from: private */
    public void connect() {
        IBinder binder = ServiceManager.getService("installd");
        if (binder != null) {
            try {
                binder.linkToDeath(new IBinder.DeathRecipient() {
                    public void binderDied() {
                        Slog.w(Installer.TAG, "installd died; reconnecting");
                        Installer.this.connect();
                    }
                }, 0);
            } catch (RemoteException e) {
                binder = null;
            }
        }
        if (binder != null) {
            this.mInstalld = IInstalld.Stub.asInterface(binder);
            try {
                invalidateMounts();
            } catch (InstallerException e2) {
            }
        } else {
            Slog.w(TAG, "installd not found; trying again");
            BackgroundThread.getHandler().postDelayed(new Runnable() {
                public final void run() {
                    Installer.this.connect();
                }
            }, 1000);
        }
    }

    private boolean checkBeforeRemote() {
        if (this.mWarnIfHeld != null && Thread.holdsLock(this.mWarnIfHeld)) {
            Slog.wtf(TAG, "Calling thread " + Thread.currentThread().getName() + " is holding 0x" + Integer.toHexString(System.identityHashCode(this.mWarnIfHeld)), new Throwable());
        }
        if (!this.mIsolated) {
            return true;
        }
        Slog.i(TAG, "Ignoring request because this installer is isolated");
        return false;
    }

    public long createAppData(String uuid, String packageName, int userId, int flags, int appId, String seInfo, int targetSdkVersion) throws InstallerException {
        if (!checkBeforeRemote()) {
            return -1;
        }
        try {
            return this.mInstalld.createAppData(uuid, packageName, userId, flags, appId, seInfo, targetSdkVersion);
        } catch (Exception e) {
            throw InstallerException.from(e);
        }
    }

    public void restoreCloneAppData(String uuid, String packageName, int userId, int flags, int appId, String seinfo, String parentDataUserCePkgDir, String cloneDataUserCePkgDir, String parentDataUserDePkgDir, String cloneDataUserDePkgDir) throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.restoreCloneAppData(uuid, packageName, userId, flags, appId, seinfo, parentDataUserCePkgDir, cloneDataUserCePkgDir, parentDataUserDePkgDir, cloneDataUserDePkgDir);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void restoreconAppData(String uuid, String packageName, int userId, int flags, int appId, String seInfo) throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.restoreconAppData(uuid, packageName, userId, flags, appId, seInfo);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void migrateAppData(String uuid, String packageName, int userId, int flags) throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.migrateAppData(uuid, packageName, userId, flags);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void clearAppData(String uuid, String packageName, int userId, int flags, long ceDataInode) throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.clearAppData(uuid, packageName, userId, flags, ceDataInode);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void destroyAppData(String uuid, String packageName, int userId, int flags, long ceDataInode) throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.destroyAppData(uuid, packageName, userId, flags, ceDataInode);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void fixupAppData(String uuid, int flags) throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.fixupAppData(uuid, flags);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void moveCompleteApp(String fromUuid, String toUuid, String packageName, String dataAppName, int appId, String seInfo, int targetSdkVersion) throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.moveCompleteApp(fromUuid, toUuid, packageName, dataAppName, appId, seInfo, targetSdkVersion);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void getAppSize(String uuid, String[] packageNames, int userId, int flags, int appId, long[] ceDataInodes, String[] codePaths, PackageStats stats) throws InstallerException {
        PackageStats packageStats = stats;
        if (checkBeforeRemote()) {
            try {
                long[] res = this.mInstalld.getAppSize(uuid, packageNames, userId, flags, appId, ceDataInodes, codePaths);
                packageStats.codeSize += res[0];
                packageStats.dataSize += res[1];
                packageStats.cacheSize += res[2];
                packageStats.externalCodeSize += res[3];
                packageStats.externalDataSize += res[4];
                packageStats.externalCacheSize += res[5];
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void getUserSize(String uuid, int userId, int flags, int[] appIds, PackageStats stats) throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                long[] res = this.mInstalld.getUserSize(uuid, userId, flags, appIds);
                stats.codeSize += res[0];
                stats.dataSize += res[1];
                stats.cacheSize += res[2];
                stats.externalCodeSize += res[3];
                stats.externalDataSize += res[4];
                stats.externalCacheSize += res[5];
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public long[] getExternalSize(String uuid, int userId, int flags, int[] appIds) throws InstallerException {
        if (!checkBeforeRemote()) {
            return new long[6];
        }
        try {
            return this.mInstalld.getExternalSize(uuid, userId, flags, appIds);
        } catch (Exception e) {
            throw InstallerException.from(e);
        }
    }

    public void setAppQuota(String uuid, int userId, int appId, long cacheQuota) throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.setAppQuota(uuid, userId, appId, cacheQuota);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void dexopt(String apkPath, int uid, String pkgName, String instructionSet, int dexoptNeeded, String outputPath, int dexFlags, String compilerFilter, String volumeUuid, String sharedLibraries, String seInfo, boolean downgrade, int targetSdkVersion, String profileName, String dexMetadataPath, String compilationReason) throws InstallerException {
        assertValidInstructionSet(instructionSet);
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.dexopt(apkPath, uid, pkgName, instructionSet, dexoptNeeded, outputPath, dexFlags, compilerFilter, volumeUuid, sharedLibraries, seInfo, downgrade, targetSdkVersion, profileName, dexMetadataPath, compilationReason);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public boolean mergeProfiles(int uid, String packageName, String profileName) throws InstallerException {
        if (!checkBeforeRemote()) {
            return false;
        }
        try {
            return this.mInstalld.mergeProfiles(uid, packageName, profileName);
        } catch (Exception e) {
            throw InstallerException.from(e);
        }
    }

    public boolean dumpProfiles(int uid, String packageName, String profileName, String codePath) throws InstallerException {
        if (!checkBeforeRemote()) {
            return false;
        }
        try {
            return this.mInstalld.dumpProfiles(uid, packageName, profileName, codePath);
        } catch (Exception e) {
            throw InstallerException.from(e);
        }
    }

    public boolean copySystemProfile(String systemProfile, int uid, String packageName, String profileName) throws InstallerException {
        if (!checkBeforeRemote()) {
            return false;
        }
        try {
            return this.mInstalld.copySystemProfile(systemProfile, uid, packageName, profileName);
        } catch (Exception e) {
            throw InstallerException.from(e);
        }
    }

    public void idmap(String targetApkPath, String overlayApkPath, int uid) throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.idmap(targetApkPath, overlayApkPath, uid);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void removeIdmap(String overlayApkPath) throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.removeIdmap(overlayApkPath);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void rmdex(String codePath, String instructionSet) throws InstallerException {
        assertValidInstructionSet(instructionSet);
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.rmdex(codePath, instructionSet);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void rmPackageDir(String packageDir) throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.rmPackageDir(packageDir);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void clearAppProfiles(String packageName, String profileName) throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.clearAppProfiles(packageName, profileName);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void destroyAppProfiles(String packageName) throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.destroyAppProfiles(packageName);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void createUserData(String uuid, int userId, int userSerial, int flags) throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.createUserData(uuid, userId, userSerial, flags);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void destroyUserData(String uuid, int userId, int flags) throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.destroyUserData(uuid, userId, flags);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void markBootComplete(String instructionSet) throws InstallerException {
        assertValidInstructionSet(instructionSet);
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.markBootComplete(instructionSet);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void freeCache(String uuid, long targetFreeBytes, long cacheReservedBytes, int flags) throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.freeCache(uuid, targetFreeBytes, cacheReservedBytes, flags);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void unlink(String fileName) throws InstallerException {
    }

    public void linkNativeLibraryDirectory(String uuid, String packageName, String nativeLibPath32, int userId) throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.linkNativeLibraryDirectory(uuid, packageName, nativeLibPath32, userId);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void createOatDir(String oatDir, String dexInstructionSet) throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.createOatDir(oatDir, dexInstructionSet);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void linkFile(String relativePath, String fromBase, String toBase) throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.linkFile(relativePath, fromBase, toBase);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void moveAb(String apkPath, String instructionSet, String outputPath) throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.moveAb(apkPath, instructionSet, outputPath);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void deleteOdex(String apkPath, String instructionSet, String outputPath) throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.deleteOdex(apkPath, instructionSet, outputPath);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void installApkVerity(String filePath, FileDescriptor verityInput, int contentSize) throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.installApkVerity(filePath, verityInput, contentSize);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void assertFsverityRootHashMatches(String filePath, byte[] expectedHash) throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.assertFsverityRootHashMatches(filePath, expectedHash);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public boolean reconcileSecondaryDexFile(String apkPath, String packageName, int uid, String[] isas, String volumeUuid, int flags) throws InstallerException {
        for (String assertValidInstructionSet : isas) {
            assertValidInstructionSet(assertValidInstructionSet);
        }
        if (checkBeforeRemote() == 0) {
            return false;
        }
        try {
            return this.mInstalld.reconcileSecondaryDexFile(apkPath, packageName, uid, isas, volumeUuid, flags);
        } catch (Exception e) {
            throw InstallerException.from(e);
        }
    }

    public byte[] hashSecondaryDexFile(String dexPath, String packageName, int uid, String volumeUuid, int flags) throws InstallerException {
        if (!checkBeforeRemote()) {
            return new byte[0];
        }
        try {
            return this.mInstalld.hashSecondaryDexFile(dexPath, packageName, uid, volumeUuid, flags);
        } catch (Exception e) {
            throw InstallerException.from(e);
        }
    }

    public boolean createProfileSnapshot(int appId, String packageName, String profileName, String classpath) throws InstallerException {
        if (!checkBeforeRemote()) {
            return false;
        }
        try {
            return this.mInstalld.createProfileSnapshot(appId, packageName, profileName, classpath);
        } catch (Exception e) {
            throw InstallerException.from(e);
        }
    }

    public void destroyProfileSnapshot(String packageName, String profileName) throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.destroyProfileSnapshot(packageName, profileName);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void invalidateMounts() throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.invalidateMounts();
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public boolean isQuotaSupported(String volumeUuid) throws InstallerException {
        if (!checkBeforeRemote()) {
            return false;
        }
        try {
            return this.mInstalld.isQuotaSupported(volumeUuid);
        } catch (Exception e) {
            throw InstallerException.from(e);
        }
    }

    public boolean prepareAppProfile(String pkg, int userId, int appId, String profileName, String codePath, String dexMetadataPath) throws InstallerException {
        if (!checkBeforeRemote()) {
            return false;
        }
        try {
            return this.mInstalld.prepareAppProfile(pkg, userId, appId, profileName, codePath, dexMetadataPath);
        } catch (Exception e) {
            throw InstallerException.from(e);
        }
    }

    private static void assertValidInstructionSet(String instructionSet) throws InstallerException {
        String[] strArr = Build.SUPPORTED_ABIS;
        int length = strArr.length;
        int i = 0;
        while (i < length) {
            if (!VMRuntime.getInstructionSet(strArr[i]).equals(instructionSet)) {
                i++;
            } else {
                return;
            }
        }
        throw new InstallerException("Invalid instruction set: " + instructionSet);
    }

    public int startBackupSession(IBackupSessionCallback callback) throws InstallerException {
        Slog.i(TAG, "bind call startBackupSession");
        if (!checkBeforeRemote()) {
            return -1;
        }
        try {
            return this.mInstalld.startBackupSession(callback);
        } catch (Exception e) {
            throw InstallerException.from(e);
        }
    }

    public int executeBackupTask(int sessionId, String taskCmd) throws InstallerException {
        Slog.i(TAG, "bind call executeBackupTask on sessionId " + sessionId);
        if (!checkBeforeRemote()) {
            return -1;
        }
        try {
            return this.mInstalld.executeBackupTask(sessionId, taskCmd);
        } catch (Exception e) {
            throw InstallerException.from(e);
        }
    }

    public int finishBackupSession(int sessionId) throws InstallerException {
        Slog.i(TAG, "bind call finishBackupSession");
        if (!checkBeforeRemote()) {
            return -1;
        }
        try {
            return this.mInstalld.finishBackupSession(sessionId);
        } catch (Exception e) {
            throw InstallerException.from(e);
        }
    }

    public int[] getDexOptNeeded(String[] fileNames, String[] instructionSets, String[] compilerFilters, String[] clContexts, boolean[] newProfiles, boolean[] downGrades, int[] uids) throws InstallerException {
        if (!checkBeforeRemote()) {
            return null;
        }
        return MplDexOptAdaptor.getInstance().getDexOptNeeded(this.mInstalld, fileNames, instructionSets, compilerFilters, clContexts, newProfiles, downGrades, uids);
    }

    public boolean[] isDexOptNeeded(String[] fileNames, int[] uids) throws InstallerException {
        if (!checkBeforeRemote()) {
            return null;
        }
        return MplDexOptAdaptor.getInstance().isDexOptNeeded(this.mInstalld, fileNames, uids);
    }

    public String[] getDexFileStatus(String[] fileNames, String[] instructionSets, int[] uids) throws InstallerException {
        if (!checkBeforeRemote()) {
            return null;
        }
        return MplDexOptAdaptor.getInstance().getDexFileStatus(this.mInstalld, fileNames, instructionSets, uids);
    }

    public String[] getDexFileOutputPaths(String fileName, String instructionSet, int uid) throws InstallerException {
        if (!checkBeforeRemote()) {
            return null;
        }
        assertValidInstructionSet(instructionSet);
        return MplDexOptAdaptor.getInstance().getDexFileOutputPaths(this.mInstalld, fileName, instructionSet, uid);
    }

    public String[] getDexFileOptimizationInfo(String[] fileNames, String[] instructionSets, int[] uids) throws InstallerException {
        if (!checkBeforeRemote()) {
            return null;
        }
        return MplDexOptAdaptor.getInstance().getDexFileOptimizationInfo(this.mInstalld, fileNames, instructionSets, uids);
    }
}
