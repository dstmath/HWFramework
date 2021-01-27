package android.os;

import android.os.IBackupSessionCallback;
import java.io.FileDescriptor;

public interface IInstalld extends IInterface {
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

    boolean BindFile(String str, String str2, String str3) throws RemoteException;

    void RenameAppData(String str, String str2, String str3, int i, int i2, int i3, String str4, int i4) throws RemoteException;

    void RenameAppInstallPath(String str, String str2, boolean z) throws RemoteException;

    void assertFsverityRootHashMatches(String str, byte[] bArr) throws RemoteException;

    void clearAppData(String str, String str2, int i, int i2, long j) throws RemoteException;

    void clearAppProfiles(String str, String str2) throws RemoteException;

    void clearMplCache(String str, String str2, int i, int i2, long j) throws RemoteException;

    boolean compileLayouts(String str, String str2, String str3, int i) throws RemoteException;

    boolean copySystemProfile(String str, int i, String str2, String str3) throws RemoteException;

    long createAppData(String str, String str2, int i, int i2, int i3, String str3, int i4) throws RemoteException;

    void createOatDir(String str, String str2) throws RemoteException;

    boolean createProfileSnapshot(int i, String str, String str2, String str3) throws RemoteException;

    void createUserData(String str, int i, int i2, int i3) throws RemoteException;

    void deleteOdex(String str, String str2, String str3) throws RemoteException;

    void destroyAppData(String str, String str2, int i, int i2, long j) throws RemoteException;

    void destroyAppDataSnapshot(String str, String str2, int i, long j, int i2, int i3) throws RemoteException;

    void destroyAppProfiles(String str) throws RemoteException;

    void destroyProfileSnapshot(String str, String str2) throws RemoteException;

    void destroyUserData(String str, int i, int i2) throws RemoteException;

    void dexopt(String str, int i, String str2, String str3, int i2, String str4, int i3, String str5, String str6, String str7, String str8, boolean z, int i4, String str9, String str10, String str11) throws RemoteException;

    boolean dumpProfiles(int i, String str, String str2, String str3) throws RemoteException;

    int executeBackupTask(int i, String str) throws RemoteException;

    int finishBackupSession(int i) throws RemoteException;

    void fixupAppData(String str, int i) throws RemoteException;

    void freeCache(String str, long j, long j2, int i) throws RemoteException;

    long[] getAppSize(String str, String[] strArr, int i, int i2, int i3, long[] jArr, String[] strArr2) throws RemoteException;

    void getDexFileOptimizationStatus(String[] strArr, String[] strArr2, int[] iArr, String[] strArr3) throws RemoteException;

    void getDexFileOutputPaths(String str, String str2, int i, String[] strArr) throws RemoteException;

    void getDexFileStatus(String[] strArr, String[] strArr2, int[] iArr, String[] strArr3) throws RemoteException;

    int[] getDexOptNeeded(String[] strArr, String[] strArr2, String[] strArr3, String[] strArr4, boolean[] zArr, boolean[] zArr2, int[] iArr) throws RemoteException;

    long[] getExternalSize(String str, int i, int i2, int[] iArr) throws RemoteException;

    long[] getUserSize(String str, int i, int i2, int[] iArr) throws RemoteException;

    byte[] hashSecondaryDexFile(String str, String str2, int i, String str3, int i2) throws RemoteException;

    void idmap(String str, String str2, int i) throws RemoteException;

    void installApkVerity(String str, FileDescriptor fileDescriptor, int i) throws RemoteException;

    void invalidateMounts() throws RemoteException;

    boolean[] isDexOptNeeded(String[] strArr, int[] iArr) throws RemoteException;

    boolean isQuotaSupported(String str) throws RemoteException;

    void linkFile(String str, String str2, String str3) throws RemoteException;

    void linkNativeLibraryDirectory(String str, String str2, String str3, int i) throws RemoteException;

    void markBootComplete(String str) throws RemoteException;

    boolean mergeProfiles(int i, String str, String str2) throws RemoteException;

    void migrateAppData(String str, String str2, int i, int i2) throws RemoteException;

    void migrateLegacyObbData() throws RemoteException;

    void moveAb(String str, String str2, String str3) throws RemoteException;

    void moveCompleteApp(String str, String str2, String str3, String str4, int i, String str5, int i2) throws RemoteException;

    void mplCacheGen(String str, int i, int i2, String str2) throws RemoteException;

    boolean prepareAppProfile(String str, int i, int i2, String str2, String str3, String str4) throws RemoteException;

    boolean reconcileSecondaryDexFile(String str, String str2, int i, String[] strArr, String str3, int i2) throws RemoteException;

    void removeIdmap(String str) throws RemoteException;

    void restoreAppDataSnapshot(String str, String str2, int i, String str3, int i2, int i3, int i4) throws RemoteException;

    void restoreconAppData(String str, String str2, int i, int i2, int i3, String str3) throws RemoteException;

    void rmPackageDir(String str) throws RemoteException;

    void rmdex(String str, String str2) throws RemoteException;

    void setAppQuota(String str, int i, int i2, long j) throws RemoteException;

    void setFileXattr(String str, String str2, int i, int i2) throws RemoteException;

    long snapshotAppData(String str, String str2, int i, int i2, int i3) throws RemoteException;

    int startBackupSession(IBackupSessionCallback iBackupSessionCallback) throws RemoteException;

    public static class Default implements IInstalld {
        @Override // android.os.IInstalld
        public void createUserData(String uuid, int userId, int userSerial, int flags) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public void destroyUserData(String uuid, int userId, int flags) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public long createAppData(String uuid, String packageName, int userId, int flags, int appId, String seInfo, int targetSdkVersion) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInstalld
        public void restoreconAppData(String uuid, String packageName, int userId, int flags, int appId, String seInfo) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public void migrateAppData(String uuid, String packageName, int userId, int flags) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public void clearAppData(String uuid, String packageName, int userId, int flags, long ceDataInode) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public void destroyAppData(String uuid, String packageName, int userId, int flags, long ceDataInode) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public void fixupAppData(String uuid, int flags) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public long[] getAppSize(String uuid, String[] packageNames, int userId, int flags, int appId, long[] ceDataInodes, String[] codePaths) throws RemoteException {
            return null;
        }

        @Override // android.os.IInstalld
        public long[] getUserSize(String uuid, int userId, int flags, int[] appIds) throws RemoteException {
            return null;
        }

        @Override // android.os.IInstalld
        public long[] getExternalSize(String uuid, int userId, int flags, int[] appIds) throws RemoteException {
            return null;
        }

        @Override // android.os.IInstalld
        public void setAppQuota(String uuid, int userId, int appId, long cacheQuota) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public void moveCompleteApp(String fromUuid, String toUuid, String packageName, String dataAppName, int appId, String seInfo, int targetSdkVersion) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public void dexopt(String apkPath, int uid, String packageName, String instructionSet, int dexoptNeeded, String outputPath, int dexFlags, String compilerFilter, String uuid, String sharedLibraries, String seInfo, boolean downgrade, int targetSdkVersion, String profileName, String dexMetadataPath, String compilationReason) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public boolean compileLayouts(String apkPath, String packageName, String outDexFile, int uid) throws RemoteException {
            return false;
        }

        @Override // android.os.IInstalld
        public void rmdex(String codePath, String instructionSet) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public boolean mergeProfiles(int uid, String packageName, String profileName) throws RemoteException {
            return false;
        }

        @Override // android.os.IInstalld
        public boolean dumpProfiles(int uid, String packageName, String profileName, String codePath) throws RemoteException {
            return false;
        }

        @Override // android.os.IInstalld
        public boolean copySystemProfile(String systemProfile, int uid, String packageName, String profileName) throws RemoteException {
            return false;
        }

        @Override // android.os.IInstalld
        public void clearAppProfiles(String packageName, String profileName) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public void destroyAppProfiles(String packageName) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public boolean createProfileSnapshot(int appId, String packageName, String profileName, String classpath) throws RemoteException {
            return false;
        }

        @Override // android.os.IInstalld
        public void destroyProfileSnapshot(String packageName, String profileName) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public void idmap(String targetApkPath, String overlayApkPath, int uid) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public void removeIdmap(String overlayApkPath) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public void rmPackageDir(String packageDir) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public void markBootComplete(String instructionSet) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public void freeCache(String uuid, long targetFreeBytes, long cacheReservedBytes, int flags) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public void linkNativeLibraryDirectory(String uuid, String packageName, String nativeLibPath32, int userId) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public void createOatDir(String oatDir, String instructionSet) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public void linkFile(String relativePath, String fromBase, String toBase) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public void moveAb(String apkPath, String instructionSet, String outputPath) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public void deleteOdex(String apkPath, String instructionSet, String outputPath) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public void installApkVerity(String filePath, FileDescriptor verityInput, int contentSize) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public void assertFsverityRootHashMatches(String filePath, byte[] expectedHash) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public boolean reconcileSecondaryDexFile(String dexPath, String pkgName, int uid, String[] isas, String volume_uuid, int storage_flag) throws RemoteException {
            return false;
        }

        @Override // android.os.IInstalld
        public byte[] hashSecondaryDexFile(String dexPath, String pkgName, int uid, String volumeUuid, int storageFlag) throws RemoteException {
            return null;
        }

        @Override // android.os.IInstalld
        public void invalidateMounts() throws RemoteException {
        }

        @Override // android.os.IInstalld
        public boolean isQuotaSupported(String uuid) throws RemoteException {
            return false;
        }

        @Override // android.os.IInstalld
        public boolean prepareAppProfile(String packageName, int userId, int appId, String profileName, String codePath, String dexMetadata) throws RemoteException {
            return false;
        }

        @Override // android.os.IInstalld
        public long snapshotAppData(String uuid, String packageName, int userId, int snapshotId, int storageFlags) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInstalld
        public void restoreAppDataSnapshot(String uuid, String packageName, int appId, String seInfo, int user, int snapshotId, int storageflags) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public void destroyAppDataSnapshot(String uuid, String packageName, int userId, long ceSnapshotInode, int snapshotId, int storageFlags) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public void migrateLegacyObbData() throws RemoteException {
        }

        @Override // android.os.IInstalld
        public int startBackupSession(IBackupSessionCallback callback) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInstalld
        public int executeBackupTask(int sessionId, String taskCmd) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInstalld
        public int finishBackupSession(int sessionId) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInstalld
        public void setFileXattr(String path, String keyDesc, int storageType, int fileType) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public void mplCacheGen(String apkPath, int uid, int cacheLevelBitmap, String classPath) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public int[] getDexOptNeeded(String[] fileNames, String[] instructionSets, String[] compilerFilters, String[] clContexts, boolean[] newProfiles, boolean[] downGrades, int[] uids) throws RemoteException {
            return null;
        }

        @Override // android.os.IInstalld
        public void getDexFileStatus(String[] fileNames, String[] instructionSets, int[] uids, String[] dex_file_status) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public void getDexFileOutputPaths(String fileName, String instructionSet, int uid, String[] output_paths) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public boolean[] isDexOptNeeded(String[] fileNames, int[] uids) throws RemoteException {
            return null;
        }

        @Override // android.os.IInstalld
        public void getDexFileOptimizationStatus(String[] fileNames, String[] instructionSets, int[] uids, String[] opt_status) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public void clearMplCache(String uuid, String packageName, int userId, int flags, long ceDataInode) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public void RenameAppData(String Uuid, String orgiPkgName, String packageName, int userId, int flags, int appId, String seInfo, int targetSdkVersion) throws RemoteException {
        }

        @Override // android.os.IInstalld
        public boolean BindFile(String relativePath, String fromBase, String toBase) throws RemoteException {
            return false;
        }

        @Override // android.os.IInstalld
        public void RenameAppInstallPath(String originPath, String targetPath, boolean isRevert) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IInstalld {
        private static final String DESCRIPTOR = "android.os.IInstalld";
        static final int TRANSACTION_BindFile = 57;
        static final int TRANSACTION_RenameAppData = 56;
        static final int TRANSACTION_RenameAppInstallPath = 58;
        static final int TRANSACTION_assertFsverityRootHashMatches = 35;
        static final int TRANSACTION_clearAppData = 6;
        static final int TRANSACTION_clearAppProfiles = 20;
        static final int TRANSACTION_clearMplCache = 55;
        static final int TRANSACTION_compileLayouts = 15;
        static final int TRANSACTION_copySystemProfile = 19;
        static final int TRANSACTION_createAppData = 3;
        static final int TRANSACTION_createOatDir = 30;
        static final int TRANSACTION_createProfileSnapshot = 22;
        static final int TRANSACTION_createUserData = 1;
        static final int TRANSACTION_deleteOdex = 33;
        static final int TRANSACTION_destroyAppData = 7;
        static final int TRANSACTION_destroyAppDataSnapshot = 43;
        static final int TRANSACTION_destroyAppProfiles = 21;
        static final int TRANSACTION_destroyProfileSnapshot = 23;
        static final int TRANSACTION_destroyUserData = 2;
        static final int TRANSACTION_dexopt = 14;
        static final int TRANSACTION_dumpProfiles = 18;
        static final int TRANSACTION_executeBackupTask = 46;
        static final int TRANSACTION_finishBackupSession = 47;
        static final int TRANSACTION_fixupAppData = 8;
        static final int TRANSACTION_freeCache = 28;
        static final int TRANSACTION_getAppSize = 9;
        static final int TRANSACTION_getDexFileOptimizationStatus = 54;
        static final int TRANSACTION_getDexFileOutputPaths = 52;
        static final int TRANSACTION_getDexFileStatus = 51;
        static final int TRANSACTION_getDexOptNeeded = 50;
        static final int TRANSACTION_getExternalSize = 11;
        static final int TRANSACTION_getUserSize = 10;
        static final int TRANSACTION_hashSecondaryDexFile = 37;
        static final int TRANSACTION_idmap = 24;
        static final int TRANSACTION_installApkVerity = 34;
        static final int TRANSACTION_invalidateMounts = 38;
        static final int TRANSACTION_isDexOptNeeded = 53;
        static final int TRANSACTION_isQuotaSupported = 39;
        static final int TRANSACTION_linkFile = 31;
        static final int TRANSACTION_linkNativeLibraryDirectory = 29;
        static final int TRANSACTION_markBootComplete = 27;
        static final int TRANSACTION_mergeProfiles = 17;
        static final int TRANSACTION_migrateAppData = 5;
        static final int TRANSACTION_migrateLegacyObbData = 44;
        static final int TRANSACTION_moveAb = 32;
        static final int TRANSACTION_moveCompleteApp = 13;
        static final int TRANSACTION_mplCacheGen = 49;
        static final int TRANSACTION_prepareAppProfile = 40;
        static final int TRANSACTION_reconcileSecondaryDexFile = 36;
        static final int TRANSACTION_removeIdmap = 25;
        static final int TRANSACTION_restoreAppDataSnapshot = 42;
        static final int TRANSACTION_restoreconAppData = 4;
        static final int TRANSACTION_rmPackageDir = 26;
        static final int TRANSACTION_rmdex = 16;
        static final int TRANSACTION_setAppQuota = 12;
        static final int TRANSACTION_setFileXattr = 48;
        static final int TRANSACTION_snapshotAppData = 41;
        static final int TRANSACTION_startBackupSession = 45;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IInstalld asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IInstalld)) {
                return new Proxy(obj);
            }
            return (IInstalld) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String[] _arg3;
            String[] _arg32;
            String[] _arg33;
            if (code != 1598968902) {
                boolean _arg2 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        createUserData(data.readString(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        destroyUserData(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        long _result = createAppData(data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeLong(_result);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        restoreconAppData(data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        migrateAppData(data.readString(), data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        clearAppData(data.readString(), data.readString(), data.readInt(), data.readInt(), data.readLong());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        destroyAppData(data.readString(), data.readString(), data.readInt(), data.readInt(), data.readLong());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        fixupAppData(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        long[] _result2 = getAppSize(data.readString(), data.createStringArray(), data.readInt(), data.readInt(), data.readInt(), data.createLongArray(), data.createStringArray());
                        reply.writeNoException();
                        reply.writeLongArray(_result2);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        long[] _result3 = getUserSize(data.readString(), data.readInt(), data.readInt(), data.createIntArray());
                        reply.writeNoException();
                        reply.writeLongArray(_result3);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        long[] _result4 = getExternalSize(data.readString(), data.readInt(), data.readInt(), data.createIntArray());
                        reply.writeNoException();
                        reply.writeLongArray(_result4);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        setAppQuota(data.readString(), data.readInt(), data.readInt(), data.readLong());
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        moveCompleteApp(data.readString(), data.readString(), data.readString(), data.readString(), data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        dexopt(data.readString(), data.readInt(), data.readString(), data.readString(), data.readInt(), data.readString(), data.readInt(), data.readString(), data.readString(), data.readString(), data.readString(), data.readInt() != 0, data.readInt(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        boolean compileLayouts = compileLayouts(data.readString(), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(compileLayouts ? 1 : 0);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        rmdex(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        boolean mergeProfiles = mergeProfiles(data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(mergeProfiles ? 1 : 0);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        boolean dumpProfiles = dumpProfiles(data.readInt(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(dumpProfiles ? 1 : 0);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        boolean copySystemProfile = copySystemProfile(data.readString(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(copySystemProfile ? 1 : 0);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        clearAppProfiles(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        destroyAppProfiles(data.readString());
                        reply.writeNoException();
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        boolean createProfileSnapshot = createProfileSnapshot(data.readInt(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(createProfileSnapshot ? 1 : 0);
                        return true;
                    case TRANSACTION_destroyProfileSnapshot /* 23 */:
                        data.enforceInterface(DESCRIPTOR);
                        destroyProfileSnapshot(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_idmap /* 24 */:
                        data.enforceInterface(DESCRIPTOR);
                        idmap(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_removeIdmap /* 25 */:
                        data.enforceInterface(DESCRIPTOR);
                        removeIdmap(data.readString());
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        rmPackageDir(data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_markBootComplete /* 27 */:
                        data.enforceInterface(DESCRIPTOR);
                        markBootComplete(data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_freeCache /* 28 */:
                        data.enforceInterface(DESCRIPTOR);
                        freeCache(data.readString(), data.readLong(), data.readLong(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        linkNativeLibraryDirectory(data.readString(), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        createOatDir(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        linkFile(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        moveAb(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        deleteOdex(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        installApkVerity(data.readString(), data.readRawFileDescriptor(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        assertFsverityRootHashMatches(data.readString(), data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        boolean reconcileSecondaryDexFile = reconcileSecondaryDexFile(data.readString(), data.readString(), data.readInt(), data.createStringArray(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(reconcileSecondaryDexFile ? 1 : 0);
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result5 = hashSecondaryDexFile(data.readString(), data.readString(), data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeByteArray(_result5);
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        invalidateMounts();
                        reply.writeNoException();
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isQuotaSupported = isQuotaSupported(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isQuotaSupported ? 1 : 0);
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        boolean prepareAppProfile = prepareAppProfile(data.readString(), data.readInt(), data.readInt(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(prepareAppProfile ? 1 : 0);
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        long _result6 = snapshotAppData(data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeLong(_result6);
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        restoreAppDataSnapshot(data.readString(), data.readString(), data.readInt(), data.readString(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        destroyAppDataSnapshot(data.readString(), data.readString(), data.readInt(), data.readLong(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        migrateLegacyObbData();
                        reply.writeNoException();
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = startBackupSession(IBackupSessionCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case TRANSACTION_executeBackupTask /* 46 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = executeBackupTask(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = finishBackupSession(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        setFileXattr(data.readString(), data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        mplCacheGen(data.readString(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result10 = getDexOptNeeded(data.createStringArray(), data.createStringArray(), data.createStringArray(), data.createStringArray(), data.createBooleanArray(), data.createBooleanArray(), data.createIntArray());
                        reply.writeNoException();
                        reply.writeIntArray(_result10);
                        return true;
                    case 51:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _arg0 = data.createStringArray();
                        String[] _arg1 = data.createStringArray();
                        int[] _arg22 = data.createIntArray();
                        int _arg3_length = data.readInt();
                        if (_arg3_length < 0) {
                            _arg3 = null;
                        } else {
                            _arg3 = new String[_arg3_length];
                        }
                        getDexFileStatus(_arg0, _arg1, _arg22, _arg3);
                        reply.writeNoException();
                        reply.writeStringArray(_arg3);
                        return true;
                    case 52:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        String _arg12 = data.readString();
                        int _arg23 = data.readInt();
                        int _arg3_length2 = data.readInt();
                        if (_arg3_length2 < 0) {
                            _arg32 = null;
                        } else {
                            _arg32 = new String[_arg3_length2];
                        }
                        getDexFileOutputPaths(_arg02, _arg12, _arg23, _arg32);
                        reply.writeNoException();
                        reply.writeStringArray(_arg32);
                        return true;
                    case 53:
                        data.enforceInterface(DESCRIPTOR);
                        boolean[] _result11 = isDexOptNeeded(data.createStringArray(), data.createIntArray());
                        reply.writeNoException();
                        reply.writeBooleanArray(_result11);
                        return true;
                    case 54:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _arg03 = data.createStringArray();
                        String[] _arg13 = data.createStringArray();
                        int[] _arg24 = data.createIntArray();
                        int _arg3_length3 = data.readInt();
                        if (_arg3_length3 < 0) {
                            _arg33 = null;
                        } else {
                            _arg33 = new String[_arg3_length3];
                        }
                        getDexFileOptimizationStatus(_arg03, _arg13, _arg24, _arg33);
                        reply.writeNoException();
                        reply.writeStringArray(_arg33);
                        return true;
                    case 55:
                        data.enforceInterface(DESCRIPTOR);
                        clearMplCache(data.readString(), data.readString(), data.readInt(), data.readInt(), data.readLong());
                        reply.writeNoException();
                        return true;
                    case 56:
                        data.enforceInterface(DESCRIPTOR);
                        RenameAppData(data.readString(), data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 57:
                        data.enforceInterface(DESCRIPTOR);
                        boolean BindFile = BindFile(data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(BindFile ? 1 : 0);
                        return true;
                    case 58:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        String _arg14 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        RenameAppInstallPath(_arg04, _arg14, _arg2);
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IInstalld {
            public static IInstalld sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // android.os.IInstalld
            public void createUserData(String uuid, int userId, int userSerial, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeInt(userId);
                    _data.writeInt(userSerial);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().createUserData(uuid, userId, userSerial, flags);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public void destroyUserData(String uuid, int userId, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeInt(userId);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().destroyUserData(uuid, userId, flags);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public long createAppData(String uuid, String packageName, int userId, int flags, int appId, String seInfo, int targetSdkVersion) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(uuid);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(packageName);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(userId);
                        try {
                            _data.writeInt(flags);
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(appId);
                            _data.writeString(seInfo);
                            _data.writeInt(targetSdkVersion);
                            if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                long _result = _reply.readLong();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            long createAppData = Stub.getDefaultImpl().createAppData(uuid, packageName, userId, flags, appId, seInfo, targetSdkVersion);
                            _reply.recycle();
                            _data.recycle();
                            return createAppData;
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.os.IInstalld
            public void restoreconAppData(String uuid, String packageName, int userId, int flags, int appId, String seInfo) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(uuid);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(packageName);
                        try {
                            _data.writeInt(userId);
                            try {
                                _data.writeInt(flags);
                            } catch (Throwable th3) {
                                th = th3;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(appId);
                        try {
                            _data.writeString(seInfo);
                            if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().restoreconAppData(uuid, packageName, userId, flags, appId, seInfo);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th6) {
                            th = th6;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.os.IInstalld
            public void migrateAppData(String uuid, String packageName, int userId, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().migrateAppData(uuid, packageName, userId, flags);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public void clearAppData(String uuid, String packageName, int userId, int flags, long ceDataInode) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(uuid);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(packageName);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(userId);
                        try {
                            _data.writeInt(flags);
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeLong(ceDataInode);
                            if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().clearAppData(uuid, packageName, userId, flags, ceDataInode);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.os.IInstalld
            public void destroyAppData(String uuid, String packageName, int userId, int flags, long ceDataInode) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(uuid);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(packageName);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(userId);
                        try {
                            _data.writeInt(flags);
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeLong(ceDataInode);
                            if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().destroyAppData(uuid, packageName, userId, flags, ceDataInode);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.os.IInstalld
            public void fixupAppData(String uuid, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().fixupAppData(uuid, flags);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public long[] getAppSize(String uuid, String[] packageNames, int userId, int flags, int appId, long[] ceDataInodes, String[] codePaths) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(uuid);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeStringArray(packageNames);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(userId);
                        try {
                            _data.writeInt(flags);
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(appId);
                            _data.writeLongArray(ceDataInodes);
                            _data.writeStringArray(codePaths);
                            if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                long[] _result = _reply.createLongArray();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            long[] appSize = Stub.getDefaultImpl().getAppSize(uuid, packageNames, userId, flags, appId, ceDataInodes, codePaths);
                            _reply.recycle();
                            _data.recycle();
                            return appSize;
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.os.IInstalld
            public long[] getUserSize(String uuid, int userId, int flags, int[] appIds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeInt(userId);
                    _data.writeInt(flags);
                    _data.writeIntArray(appIds);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUserSize(uuid, userId, flags, appIds);
                    }
                    _reply.readException();
                    long[] _result = _reply.createLongArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public long[] getExternalSize(String uuid, int userId, int flags, int[] appIds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeInt(userId);
                    _data.writeInt(flags);
                    _data.writeIntArray(appIds);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getExternalSize(uuid, userId, flags, appIds);
                    }
                    _reply.readException();
                    long[] _result = _reply.createLongArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public void setAppQuota(String uuid, int userId, int appId, long cacheQuota) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeInt(userId);
                    _data.writeInt(appId);
                    _data.writeLong(cacheQuota);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAppQuota(uuid, userId, appId, cacheQuota);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public void moveCompleteApp(String fromUuid, String toUuid, String packageName, String dataAppName, int appId, String seInfo, int targetSdkVersion) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(fromUuid);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(toUuid);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(packageName);
                        try {
                            _data.writeString(dataAppName);
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(appId);
                            _data.writeString(seInfo);
                            _data.writeInt(targetSdkVersion);
                            if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().moveCompleteApp(fromUuid, toUuid, packageName, dataAppName, appId, seInfo, targetSdkVersion);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.os.IInstalld
            public void dexopt(String apkPath, int uid, String packageName, String instructionSet, int dexoptNeeded, String outputPath, int dexFlags, String compilerFilter, String uuid, String sharedLibraries, String seInfo, boolean downgrade, int targetSdkVersion, String profileName, String dexMetadataPath, String compilationReason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(apkPath);
                    _data.writeInt(uid);
                    _data.writeString(packageName);
                    _data.writeString(instructionSet);
                    _data.writeInt(dexoptNeeded);
                    _data.writeString(outputPath);
                    _data.writeInt(dexFlags);
                    _data.writeString(compilerFilter);
                    _data.writeString(uuid);
                    _data.writeString(sharedLibraries);
                    _data.writeString(seInfo);
                    _data.writeInt(downgrade ? 1 : 0);
                    _data.writeInt(targetSdkVersion);
                    _data.writeString(profileName);
                    _data.writeString(dexMetadataPath);
                    _data.writeString(compilationReason);
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().dexopt(apkPath, uid, packageName, instructionSet, dexoptNeeded, outputPath, dexFlags, compilerFilter, uuid, sharedLibraries, seInfo, downgrade, targetSdkVersion, profileName, dexMetadataPath, compilationReason);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public boolean compileLayouts(String apkPath, String packageName, String outDexFile, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(apkPath);
                    _data.writeString(packageName);
                    _data.writeString(outDexFile);
                    _data.writeInt(uid);
                    boolean _result = false;
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().compileLayouts(apkPath, packageName, outDexFile, uid);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public void rmdex(String codePath, String instructionSet) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(codePath);
                    _data.writeString(instructionSet);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().rmdex(codePath, instructionSet);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public boolean mergeProfiles(int uid, String packageName, String profileName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(packageName);
                    _data.writeString(profileName);
                    boolean _result = false;
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().mergeProfiles(uid, packageName, profileName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public boolean dumpProfiles(int uid, String packageName, String profileName, String codePath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(packageName);
                    _data.writeString(profileName);
                    _data.writeString(codePath);
                    boolean _result = false;
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().dumpProfiles(uid, packageName, profileName, codePath);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public boolean copySystemProfile(String systemProfile, int uid, String packageName, String profileName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(systemProfile);
                    _data.writeInt(uid);
                    _data.writeString(packageName);
                    _data.writeString(profileName);
                    boolean _result = false;
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().copySystemProfile(systemProfile, uid, packageName, profileName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public void clearAppProfiles(String packageName, String profileName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(profileName);
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearAppProfiles(packageName, profileName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public void destroyAppProfiles(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(21, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().destroyAppProfiles(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public boolean createProfileSnapshot(int appId, String packageName, String profileName, String classpath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(appId);
                    _data.writeString(packageName);
                    _data.writeString(profileName);
                    _data.writeString(classpath);
                    boolean _result = false;
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createProfileSnapshot(appId, packageName, profileName, classpath);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public void destroyProfileSnapshot(String packageName, String profileName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(profileName);
                    if (this.mRemote.transact(Stub.TRANSACTION_destroyProfileSnapshot, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().destroyProfileSnapshot(packageName, profileName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public void idmap(String targetApkPath, String overlayApkPath, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(targetApkPath);
                    _data.writeString(overlayApkPath);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(Stub.TRANSACTION_idmap, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().idmap(targetApkPath, overlayApkPath, uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public void removeIdmap(String overlayApkPath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(overlayApkPath);
                    if (this.mRemote.transact(Stub.TRANSACTION_removeIdmap, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeIdmap(overlayApkPath);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public void rmPackageDir(String packageDir) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageDir);
                    if (this.mRemote.transact(26, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().rmPackageDir(packageDir);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public void markBootComplete(String instructionSet) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(instructionSet);
                    if (this.mRemote.transact(Stub.TRANSACTION_markBootComplete, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().markBootComplete(instructionSet);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public void freeCache(String uuid, long targetFreeBytes, long cacheReservedBytes, int flags) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(uuid);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(targetFreeBytes);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(cacheReservedBytes);
                        try {
                            _data.writeInt(flags);
                            if (this.mRemote.transact(Stub.TRANSACTION_freeCache, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().freeCache(uuid, targetFreeBytes, cacheReservedBytes, flags);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.os.IInstalld
            public void linkNativeLibraryDirectory(String uuid, String packageName, String nativeLibPath32, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeString(packageName);
                    _data.writeString(nativeLibPath32);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(29, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().linkNativeLibraryDirectory(uuid, packageName, nativeLibPath32, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public void createOatDir(String oatDir, String instructionSet) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(oatDir);
                    _data.writeString(instructionSet);
                    if (this.mRemote.transact(30, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().createOatDir(oatDir, instructionSet);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public void linkFile(String relativePath, String fromBase, String toBase) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(relativePath);
                    _data.writeString(fromBase);
                    _data.writeString(toBase);
                    if (this.mRemote.transact(31, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().linkFile(relativePath, fromBase, toBase);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public void moveAb(String apkPath, String instructionSet, String outputPath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(apkPath);
                    _data.writeString(instructionSet);
                    _data.writeString(outputPath);
                    if (this.mRemote.transact(32, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().moveAb(apkPath, instructionSet, outputPath);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public void deleteOdex(String apkPath, String instructionSet, String outputPath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(apkPath);
                    _data.writeString(instructionSet);
                    _data.writeString(outputPath);
                    if (this.mRemote.transact(33, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().deleteOdex(apkPath, instructionSet, outputPath);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public void installApkVerity(String filePath, FileDescriptor verityInput, int contentSize) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(filePath);
                    _data.writeRawFileDescriptor(verityInput);
                    _data.writeInt(contentSize);
                    if (this.mRemote.transact(34, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().installApkVerity(filePath, verityInput, contentSize);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public void assertFsverityRootHashMatches(String filePath, byte[] expectedHash) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(filePath);
                    _data.writeByteArray(expectedHash);
                    if (this.mRemote.transact(35, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().assertFsverityRootHashMatches(filePath, expectedHash);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public boolean reconcileSecondaryDexFile(String dexPath, String pkgName, int uid, String[] isas, String volume_uuid, int storage_flag) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(dexPath);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(pkgName);
                        try {
                            _data.writeInt(uid);
                            try {
                                _data.writeStringArray(isas);
                            } catch (Throwable th3) {
                                th = th3;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(volume_uuid);
                        try {
                            _data.writeInt(storage_flag);
                            boolean _result = false;
                            if (this.mRemote.transact(36, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = true;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean reconcileSecondaryDexFile = Stub.getDefaultImpl().reconcileSecondaryDexFile(dexPath, pkgName, uid, isas, volume_uuid, storage_flag);
                            _reply.recycle();
                            _data.recycle();
                            return reconcileSecondaryDexFile;
                        } catch (Throwable th6) {
                            th = th6;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.os.IInstalld
            public byte[] hashSecondaryDexFile(String dexPath, String pkgName, int uid, String volumeUuid, int storageFlag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(dexPath);
                    _data.writeString(pkgName);
                    _data.writeInt(uid);
                    _data.writeString(volumeUuid);
                    _data.writeInt(storageFlag);
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hashSecondaryDexFile(dexPath, pkgName, uid, volumeUuid, storageFlag);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public void invalidateMounts() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(38, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().invalidateMounts();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public boolean isQuotaSupported(String uuid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    boolean _result = false;
                    if (!this.mRemote.transact(39, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isQuotaSupported(uuid);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public boolean prepareAppProfile(String packageName, int userId, int appId, String profileName, String codePath, String dexMetadata) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(packageName);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(userId);
                        try {
                            _data.writeInt(appId);
                            try {
                                _data.writeString(profileName);
                            } catch (Throwable th3) {
                                th = th3;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(codePath);
                        try {
                            _data.writeString(dexMetadata);
                            boolean _result = false;
                            if (this.mRemote.transact(40, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = true;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean prepareAppProfile = Stub.getDefaultImpl().prepareAppProfile(packageName, userId, appId, profileName, codePath, dexMetadata);
                            _reply.recycle();
                            _data.recycle();
                            return prepareAppProfile;
                        } catch (Throwable th6) {
                            th = th6;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.os.IInstalld
            public long snapshotAppData(String uuid, String packageName, int userId, int snapshotId, int storageFlags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    _data.writeInt(snapshotId);
                    _data.writeInt(storageFlags);
                    if (!this.mRemote.transact(41, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().snapshotAppData(uuid, packageName, userId, snapshotId, storageFlags);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public void restoreAppDataSnapshot(String uuid, String packageName, int appId, String seInfo, int user, int snapshotId, int storageflags) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(uuid);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(packageName);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(appId);
                        try {
                            _data.writeString(seInfo);
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(user);
                            _data.writeInt(snapshotId);
                            _data.writeInt(storageflags);
                            if (this.mRemote.transact(42, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().restoreAppDataSnapshot(uuid, packageName, appId, seInfo, user, snapshotId, storageflags);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.os.IInstalld
            public void destroyAppDataSnapshot(String uuid, String packageName, int userId, long ceSnapshotInode, int snapshotId, int storageFlags) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(uuid);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(packageName);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(userId);
                        try {
                            _data.writeLong(ceSnapshotInode);
                            _data.writeInt(snapshotId);
                            _data.writeInt(storageFlags);
                            if (this.mRemote.transact(43, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().destroyAppDataSnapshot(uuid, packageName, userId, ceSnapshotInode, snapshotId, storageFlags);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.os.IInstalld
            public void migrateLegacyObbData() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(44, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().migrateLegacyObbData();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public int startBackupSession(IBackupSessionCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(45, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startBackupSession(callback);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public int executeBackupTask(int sessionId, String taskCmd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeString(taskCmd);
                    if (!this.mRemote.transact(Stub.TRANSACTION_executeBackupTask, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().executeBackupTask(sessionId, taskCmd);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public int finishBackupSession(int sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    if (!this.mRemote.transact(47, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().finishBackupSession(sessionId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public void setFileXattr(String path, String keyDesc, int storageType, int fileType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    _data.writeString(keyDesc);
                    _data.writeInt(storageType);
                    _data.writeInt(fileType);
                    if (this.mRemote.transact(48, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setFileXattr(path, keyDesc, storageType, fileType);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public void mplCacheGen(String apkPath, int uid, int cacheLevelBitmap, String classPath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(apkPath);
                    _data.writeInt(uid);
                    _data.writeInt(cacheLevelBitmap);
                    _data.writeString(classPath);
                    if (this.mRemote.transact(49, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().mplCacheGen(apkPath, uid, cacheLevelBitmap, classPath);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public int[] getDexOptNeeded(String[] fileNames, String[] instructionSets, String[] compilerFilters, String[] clContexts, boolean[] newProfiles, boolean[] downGrades, int[] uids) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeStringArray(fileNames);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeStringArray(instructionSets);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeStringArray(compilerFilters);
                        try {
                            _data.writeStringArray(clContexts);
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeBooleanArray(newProfiles);
                            _data.writeBooleanArray(downGrades);
                            _data.writeIntArray(uids);
                            if (this.mRemote.transact(50, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                int[] _result = _reply.createIntArray();
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            int[] dexOptNeeded = Stub.getDefaultImpl().getDexOptNeeded(fileNames, instructionSets, compilerFilters, clContexts, newProfiles, downGrades, uids);
                            _reply.recycle();
                            _data.recycle();
                            return dexOptNeeded;
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.os.IInstalld
            public void getDexFileStatus(String[] fileNames, String[] instructionSets, int[] uids, String[] dex_file_status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(fileNames);
                    _data.writeStringArray(instructionSets);
                    _data.writeIntArray(uids);
                    if (dex_file_status == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(dex_file_status.length);
                    }
                    if (this.mRemote.transact(51, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.readStringArray(dex_file_status);
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().getDexFileStatus(fileNames, instructionSets, uids, dex_file_status);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public void getDexFileOutputPaths(String fileName, String instructionSet, int uid, String[] output_paths) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fileName);
                    _data.writeString(instructionSet);
                    _data.writeInt(uid);
                    if (output_paths == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(output_paths.length);
                    }
                    if (this.mRemote.transact(52, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.readStringArray(output_paths);
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().getDexFileOutputPaths(fileName, instructionSet, uid, output_paths);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public boolean[] isDexOptNeeded(String[] fileNames, int[] uids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(fileNames);
                    _data.writeIntArray(uids);
                    if (!this.mRemote.transact(53, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDexOptNeeded(fileNames, uids);
                    }
                    _reply.readException();
                    boolean[] _result = _reply.createBooleanArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public void getDexFileOptimizationStatus(String[] fileNames, String[] instructionSets, int[] uids, String[] opt_status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(fileNames);
                    _data.writeStringArray(instructionSets);
                    _data.writeIntArray(uids);
                    if (opt_status == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(opt_status.length);
                    }
                    if (this.mRemote.transact(54, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.readStringArray(opt_status);
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().getDexFileOptimizationStatus(fileNames, instructionSets, uids, opt_status);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public void clearMplCache(String uuid, String packageName, int userId, int flags, long ceDataInode) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(uuid);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(packageName);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(userId);
                        try {
                            _data.writeInt(flags);
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeLong(ceDataInode);
                            if (this.mRemote.transact(55, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().clearMplCache(uuid, packageName, userId, flags, ceDataInode);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.os.IInstalld
            public void RenameAppData(String Uuid, String orgiPkgName, String packageName, int userId, int flags, int appId, String seInfo, int targetSdkVersion) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(Uuid);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(orgiPkgName);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(packageName);
                        try {
                            _data.writeInt(userId);
                            _data.writeInt(flags);
                            _data.writeInt(appId);
                            _data.writeString(seInfo);
                            _data.writeInt(targetSdkVersion);
                            if (this.mRemote.transact(56, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().RenameAppData(Uuid, orgiPkgName, packageName, userId, flags, appId, seInfo, targetSdkVersion);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.os.IInstalld
            public boolean BindFile(String relativePath, String fromBase, String toBase) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(relativePath);
                    _data.writeString(fromBase);
                    _data.writeString(toBase);
                    boolean _result = false;
                    if (!this.mRemote.transact(57, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().BindFile(relativePath, fromBase, toBase);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IInstalld
            public void RenameAppInstallPath(String originPath, String targetPath, boolean isRevert) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(originPath);
                    _data.writeString(targetPath);
                    _data.writeInt(isRevert ? 1 : 0);
                    if (this.mRemote.transact(58, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().RenameAppInstallPath(originPath, targetPath, isRevert);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IInstalld impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IInstalld getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
