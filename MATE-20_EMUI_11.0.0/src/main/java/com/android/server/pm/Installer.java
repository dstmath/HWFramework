package com.android.server.pm;

import android.content.Context;
import android.content.pm.PackageStats;
import android.os.Build;
import android.os.IBackupSessionCallback;
import android.os.IBinder;
import android.os.IInstalld;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.os.BackgroundThread;
import com.android.server.SystemService;
import dalvik.system.BlockGuard;
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
    public static final int FLAG_CLEAR_CACHE_ONLY = 16;
    public static final int FLAG_CLEAR_CODE_CACHE_ONLY = 32;
    public static final int FLAG_FORCE = 8192;
    public static final int FLAG_FREE_CACHE_NOOP = 1024;
    public static final int FLAG_FREE_CACHE_V2 = 256;
    public static final int FLAG_FREE_CACHE_V2_DEFY_QUOTA = 512;
    public static final int FLAG_RESERVE_PROFILE = 128;
    public static final int FLAG_STORAGE_CE = 2;
    public static final int FLAG_STORAGE_DE = 1;
    public static final int FLAG_STORAGE_EXTERNAL = 4;
    public static final int FLAG_USE_QUOTA = 4096;
    private static final boolean IS_BOPD = SystemProperties.getBoolean("sys.bopd", false);
    private static final String TAG = "Installer";
    private volatile IInstalld mInstalld;
    private final boolean mIsolated;
    private volatile Object mWarnIfHeld;

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

    @Override // com.android.server.SystemService
    public void onStart() {
        if (this.mIsolated) {
            this.mInstalld = null;
        } else {
            lambda$connect$0$Installer();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* renamed from: connect */
    public void lambda$connect$0$Installer() {
        IBinder binder = ServiceManager.getService("installd");
        if (binder != null) {
            try {
                binder.linkToDeath(new IBinder.DeathRecipient() {
                    /* class com.android.server.pm.Installer.AnonymousClass1 */

                    @Override // android.os.IBinder.DeathRecipient
                    public void binderDied() {
                        Slog.w(Installer.TAG, "installd died; reconnecting");
                        Installer.this.lambda$connect$0$Installer();
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
                /* class com.android.server.pm.$$Lambda$Installer$SebeftIfAJ7KsTmM0tju6PfW4Pc */

                @Override // java.lang.Runnable
                public final void run() {
                    Installer.this.lambda$connect$0$Installer();
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

    public void clearMplCache(String uuid, String packageName, int userId, int flags, long ceDataInode) throws InstallerException {
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.clearMplCache(uuid, packageName, userId, flags, ceDataInode);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void destroyAppData(String uuid, String packageName, int userId, int flags, long ceDataInode) throws InstallerException {
        if (checkBeforeRemote()) {
            if (IS_BOPD) {
                Slog.i(TAG, "In BOPD model, don't destroy app data.");
                return;
            }
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
            if (codePaths != null) {
                for (String codePath : codePaths) {
                    BlockGuard.getVmPolicy().onPathAccess(codePath);
                }
            }
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
        BlockGuard.getVmPolicy().onPathAccess(apkPath);
        BlockGuard.getVmPolicy().onPathAccess(outputPath);
        BlockGuard.getVmPolicy().onPathAccess(dexMetadataPath);
        if (checkBeforeRemote()) {
            try {
                this.mInstalld.dexopt(apkPath, uid, pkgName, instructionSet, dexoptNeeded, outputPath, dexFlags, compilerFilter, volumeUuid, sharedLibraries, seInfo, downgrade, targetSdkVersion, profileName, dexMetadataPath, compilationReason);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void generateMplCache(String apkPath, int uid, int cacheLevel, String classPath) throws InstallerException {
        Slog.i(TAG, "[DCP] -> genMplCache is called for " + apkPath + " with uid " + uid + ", classpath " + classPath);
        try {
            this.mInstalld.mplCacheGen(apkPath, uid, cacheLevel, classPath);
        } catch (Exception e) {
            throw InstallerException.from(e);
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
        BlockGuard.getVmPolicy().onPathAccess(codePath);
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
            BlockGuard.getVmPolicy().onPathAccess(targetApkPath);
            BlockGuard.getVmPolicy().onPathAccess(overlayApkPath);
            try {
                this.mInstalld.idmap(targetApkPath, overlayApkPath, uid);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void removeIdmap(String overlayApkPath) throws InstallerException {
        if (checkBeforeRemote()) {
            BlockGuard.getVmPolicy().onPathAccess(overlayApkPath);
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
            BlockGuard.getVmPolicy().onPathAccess(codePath);
            try {
                this.mInstalld.rmdex(codePath, instructionSet);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void rmPackageDir(String packageDir) throws InstallerException {
        if (checkBeforeRemote()) {
            BlockGuard.getVmPolicy().onPathAccess(packageDir);
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

    public void linkNativeLibraryDirectory(String uuid, String packageName, String nativeLibPath32, int userId) throws InstallerException {
        if (checkBeforeRemote()) {
            BlockGuard.getVmPolicy().onPathAccess(nativeLibPath32);
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
            BlockGuard.getVmPolicy().onPathAccess(fromBase);
            BlockGuard.getVmPolicy().onPathAccess(toBase);
            try {
                this.mInstalld.linkFile(relativePath, fromBase, toBase);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public boolean bindFile(String relativePath, String fromBase, String toBase) throws InstallerException {
        if (TextUtils.isEmpty(relativePath) || TextUtils.isEmpty(fromBase) || TextUtils.isEmpty(toBase)) {
            Slog.i(TAG, "bindFile failed empty path.");
            return false;
        } else if (!checkBeforeRemote()) {
            return false;
        } else {
            BlockGuard.getVmPolicy().onPathAccess(fromBase);
            BlockGuard.getVmPolicy().onPathAccess(toBase);
            try {
                return this.mInstalld.BindFile(relativePath, fromBase, toBase);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void moveAb(String apkPath, String instructionSet, String outputPath) throws InstallerException {
        if (checkBeforeRemote()) {
            BlockGuard.getVmPolicy().onPathAccess(apkPath);
            BlockGuard.getVmPolicy().onPathAccess(outputPath);
            try {
                this.mInstalld.moveAb(apkPath, instructionSet, outputPath);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void deleteOdex(String apkPath, String instructionSet, String outputPath) throws InstallerException {
        if (checkBeforeRemote()) {
            BlockGuard.getVmPolicy().onPathAccess(apkPath);
            BlockGuard.getVmPolicy().onPathAccess(outputPath);
            try {
                this.mInstalld.deleteOdex(apkPath, instructionSet, outputPath);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void installApkVerity(String filePath, FileDescriptor verityInput, int contentSize) throws InstallerException {
        if (checkBeforeRemote()) {
            BlockGuard.getVmPolicy().onPathAccess(filePath);
            try {
                this.mInstalld.installApkVerity(filePath, verityInput, contentSize);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void assertFsverityRootHashMatches(String filePath, byte[] expectedHash) throws InstallerException {
        if (checkBeforeRemote()) {
            BlockGuard.getVmPolicy().onPathAccess(filePath);
            try {
                this.mInstalld.assertFsverityRootHashMatches(filePath, expectedHash);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public boolean reconcileSecondaryDexFile(String apkPath, String packageName, int uid, String[] isas, String volumeUuid, int flags) throws InstallerException {
        for (String str : isas) {
            assertValidInstructionSet(str);
        }
        if (!checkBeforeRemote()) {
            return false;
        }
        BlockGuard.getVmPolicy().onPathAccess(apkPath);
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
        BlockGuard.getVmPolicy().onPathAccess(dexPath);
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
        BlockGuard.getVmPolicy().onPathAccess(codePath);
        BlockGuard.getVmPolicy().onPathAccess(dexMetadataPath);
        try {
            return this.mInstalld.prepareAppProfile(pkg, userId, appId, profileName, codePath, dexMetadataPath);
        } catch (Exception e) {
            throw InstallerException.from(e);
        }
    }

    public long snapshotAppData(String pkg, int userId, int snapshotId, int storageFlags) throws InstallerException {
        if (!checkBeforeRemote()) {
            return 0;
        }
        try {
            return this.mInstalld.snapshotAppData(null, pkg, userId, snapshotId, storageFlags);
        } catch (Exception e) {
            throw InstallerException.from(e);
        }
    }

    public boolean restoreAppDataSnapshot(String pkg, int appId, String seInfo, int userId, int snapshotId, int storageFlags) throws InstallerException {
        if (!checkBeforeRemote()) {
            return false;
        }
        try {
            this.mInstalld.restoreAppDataSnapshot(null, pkg, appId, seInfo, userId, snapshotId, storageFlags);
            return true;
        } catch (Exception e) {
            throw InstallerException.from(e);
        }
    }

    public boolean destroyAppDataSnapshot(String pkg, int userId, long ceSnapshotInode, int snapshotId, int storageFlags) throws InstallerException {
        if (!checkBeforeRemote()) {
            return false;
        }
        try {
            this.mInstalld.destroyAppDataSnapshot(null, pkg, userId, ceSnapshotInode, snapshotId, storageFlags);
            return true;
        } catch (Exception e) {
            throw InstallerException.from(e);
        }
    }

    public boolean migrateLegacyObbData() throws InstallerException {
        if (!checkBeforeRemote()) {
            return false;
        }
        try {
            this.mInstalld.migrateLegacyObbData();
            return true;
        } catch (Exception e) {
            throw InstallerException.from(e);
        }
    }

    private static void assertValidInstructionSet(String instructionSet) throws InstallerException {
        for (String abi : Build.SUPPORTED_ABIS) {
            if (VMRuntime.getInstructionSet(abi).equals(instructionSet)) {
                return;
            }
        }
        throw new InstallerException("Invalid instruction set: " + instructionSet);
    }

    public boolean compileLayouts(String apkPath, String packageName, String outDexFile, int uid) {
        try {
            return this.mInstalld.compileLayouts(apkPath, packageName, outDexFile, uid);
        } catch (RemoteException e) {
            return false;
        }
    }

    public static class InstallerException extends Exception {
        public InstallerException(String detailMessage) {
            super(detailMessage);
        }

        public static InstallerException from(Exception e) throws InstallerException {
            throw new InstallerException(e.toString());
        }
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

    public void renameAppInstallPath(String originPath, String targetPath, boolean isRevert) throws InstallerException {
        if (!TextUtils.isEmpty(originPath) && !TextUtils.isEmpty(targetPath)) {
            try {
                this.mInstalld.RenameAppInstallPath(originPath, targetPath, isRevert);
            } catch (RemoteException remoteException) {
                throw InstallerException.from(remoteException);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public void renamePackageData(RenamePackageParam param) throws InstallerException {
        if (param != null) {
            try {
                this.mInstalld.RenameAppData(param.getUuid(), param.getOrgiPackagName(), param.getNewPackageName(), param.getUserId(), param.getFlags(), param.getAppId(), param.getSeInfo(), param.getTargetSdkVersion());
            } catch (RemoteException remoteException) {
                throw InstallerException.from(remoteException);
            } catch (Exception e) {
                throw InstallerException.from(e);
            }
        }
    }

    public static class RenamePackageParam {
        private int mAppId;
        private int mFlags;
        private String mNewPackageName;
        private String mOrgiPackagName;
        private String mSeInfo;
        private int mTargetSdkVersion;
        private int mUserId;
        private String mUuid;

        public String getUuid() {
            return this.mUuid;
        }

        public void setUuid(String uuid) {
            this.mUuid = uuid;
        }

        public String getOrgiPackagName() {
            return this.mOrgiPackagName;
        }

        public void setOrgiPackageName(String orgiPackagName) {
            this.mOrgiPackagName = orgiPackagName;
        }

        public String getNewPackageName() {
            return this.mNewPackageName;
        }

        public void setNewPackageName(String newPackageName) {
            this.mNewPackageName = newPackageName;
        }

        public int getUserId() {
            return this.mUserId;
        }

        public void setUserId(int userId) {
            this.mUserId = userId;
        }

        public int getFlags() {
            return this.mFlags;
        }

        public void setFlags(int flags) {
            this.mFlags = flags;
        }

        public int getAppId() {
            return this.mAppId;
        }

        public void setAppId(int appId) {
            this.mAppId = appId;
        }

        public String getSeInfo() {
            return this.mSeInfo;
        }

        public void setSeInfo(String seInfo) {
            this.mSeInfo = seInfo;
        }

        public int getTargetSdkVersion() {
            return this.mTargetSdkVersion;
        }

        public void setTargetSdkVersion(int targetSdkVersion) {
            this.mTargetSdkVersion = targetSdkVersion;
        }
    }
}
