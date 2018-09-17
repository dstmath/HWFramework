package com.android.server.pm;

import android.content.Context;
import android.content.pm.PackageStats;
import android.os.Binder;
import android.os.Build;
import android.os.IBackupSessionCallback;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.IInstalld;
import android.os.IInstalld.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Slog;
import com.android.internal.os.BackgroundThread;
import com.android.server.SystemService;
import dalvik.system.VMRuntime;

public class Installer extends SystemService {
    public static final int DEXOPT_BOOTCOMPLETE = 8;
    public static final int DEXOPT_DEBUGGABLE = 4;
    public static final int DEXOPT_FORCE = 64;
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

    private void connect() {
        IBinder binder = ServiceManager.getService("installd");
        if (binder != null) {
            try {
                binder.linkToDeath(new DeathRecipient() {
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
            this.mInstalld = Stub.asInterface(binder);
            try {
                invalidateMounts();
                return;
            } catch (InstallerException e2) {
                return;
            }
        }
        Slog.w(TAG, "installd not found; trying again");
        BackgroundThread.getHandler().postDelayed(new -$Lambda$yikIePDfBGugHvh5SW3g9H6AbWc(this), 1000);
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
        if (checkBeforeRemote()) {
            try {
                long[] res = this.mInstalld.getAppSize(uuid, packageNames, userId, flags, appId, ceDataInodes, codePaths);
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
            return new long[4];
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

    public void dexopt(String apkPath, int uid, String pkgName, String instructionSet, int dexoptNeeded, String outputPath, int dexFlags, String compilerFilter, String volumeUuid, String sharedLibraries, String seInfo) throws InstallerException {
        assertValidInstructionSet(instructionSet);
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.dexopt(apkPath, uid, pkgName, instructionSet, dexoptNeeded, outputPath, dexFlags, compilerFilter, volumeUuid, sharedLibraries, seInfo);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public boolean mergeProfiles(int uid, String packageName) throws InstallerException {
        if (!checkBeforeRemote()) {
            return false;
        }
        try {
            return this.mInstalld.mergeProfiles(uid, packageName);
        } catch (Exception e) {
            throw InstallerException.from(e);
        }
    }

    public boolean dumpProfiles(int uid, String packageName, String codePaths) throws InstallerException {
        if (!checkBeforeRemote()) {
            return false;
        }
        try {
            return this.mInstalld.dumpProfiles(uid, packageName, codePaths);
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

    public void clearAppProfiles(String packageName) throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.clearAppProfiles(packageName);
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

    public boolean reconcileSecondaryDexFile(String apkPath, String packageName, int uid, String[] isas, String volumeUuid, int flags) throws InstallerException {
        for (String assertValidInstructionSet : isas) {
            assertValidInstructionSet(assertValidInstructionSet);
        }
        if (!checkBeforeRemote()) {
            return false;
        }
        try {
            return this.mInstalld.reconcileSecondaryDexFile(apkPath, packageName, uid, isas, volumeUuid, flags);
        } catch (Exception e) {
            throw InstallerException.from(e);
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

    private static void assertValidInstructionSet(String instructionSet) throws InstallerException {
        String[] strArr = Build.SUPPORTED_ABIS;
        int i = 0;
        int length = strArr.length;
        while (i < length) {
            if (!VMRuntime.getInstructionSet(strArr[i]).equals(instructionSet)) {
                i++;
            } else {
                return;
            }
        }
        throw new InstallerException("Invalid instruction set: " + instructionSet);
    }

    public void rmClonedAppDataDir(String dir) throws InstallerException {
        int appId = UserHandle.getAppId(Binder.getCallingUid());
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
}
