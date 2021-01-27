package android.os;

import android.os.IVoldListener;
import android.os.IVoldTaskListener;
import java.io.FileDescriptor;

public interface IVold extends IInterface {
    public static final int ENCRYPTION_FLAG_NO_UI = 4;
    public static final int ENCRYPTION_STATE_ERROR_CORRUPT = -4;
    public static final int ENCRYPTION_STATE_ERROR_INCOMPLETE = -2;
    public static final int ENCRYPTION_STATE_ERROR_INCONSISTENT = -3;
    public static final int ENCRYPTION_STATE_ERROR_UNKNOWN = -1;
    public static final int ENCRYPTION_STATE_NONE = 1;
    public static final int ENCRYPTION_STATE_OK = 0;
    public static final int FSTRIM_FLAG_DEEP_TRIM = 1;
    public static final int MOUNT_FLAG_PRIMARY = 1;
    public static final int MOUNT_FLAG_READONLY = 64;
    public static final int MOUNT_FLAG_VISIBLE = 2;
    public static final int PARTITION_TYPE_MIXED = 2;
    public static final int PARTITION_TYPE_PRIVATE = 1;
    public static final int PARTITION_TYPE_PUBLIC = 0;
    public static final int PASSWORD_TYPE_DEFAULT = 1;
    public static final int PASSWORD_TYPE_PASSWORD = 0;
    public static final int PASSWORD_TYPE_PATTERN = 2;
    public static final int PASSWORD_TYPE_PIN = 3;
    public static final int REMOUNT_MODE_DEFAULT = 1;
    public static final int REMOUNT_MODE_FULL = 6;
    public static final int REMOUNT_MODE_INSTALLER = 5;
    public static final int REMOUNT_MODE_LEGACY = 4;
    public static final int REMOUNT_MODE_NONE = 0;
    public static final int REMOUNT_MODE_READ = 2;
    public static final int REMOUNT_MODE_WRITE = 3;
    public static final int STORAGE_FLAG_CE = 2;
    public static final int STORAGE_FLAG_DE = 1;
    public static final int VOLUME_STATE_BAD_REMOVAL = 8;
    public static final int VOLUME_STATE_CHECKING = 1;
    public static final int VOLUME_STATE_EJECTING = 5;
    public static final int VOLUME_STATE_FORMATTING = 4;
    public static final int VOLUME_STATE_MOUNTED = 2;
    public static final int VOLUME_STATE_MOUNTED_READ_ONLY = 3;
    public static final int VOLUME_STATE_REMOVED = 7;
    public static final int VOLUME_STATE_UNMOUNTABLE = 6;
    public static final int VOLUME_STATE_UNMOUNTED = 0;
    public static final int VOLUME_TYPE_ASEC = 3;
    public static final int VOLUME_TYPE_EMULATED = 2;
    public static final int VOLUME_TYPE_OBB = 4;
    public static final int VOLUME_TYPE_PRIVATE = 1;
    public static final int VOLUME_TYPE_PUBLIC = 0;
    public static final int VOLUME_TYPE_STUB = 5;

    void abortChanges(String str, boolean z) throws RemoteException;

    void abortIdleMaint(IVoldTaskListener iVoldTaskListener) throws RemoteException;

    void addAppIds(String[] strArr, int[] iArr) throws RemoteException;

    void addSandboxIds(int[] iArr, String[] strArr) throws RemoteException;

    void addUserKeyAuth(int i, int i2, String str, String str2) throws RemoteException;

    void addUserKeyAuthIsec(int i, int i2, String str, String str2) throws RemoteException;

    void benchmark(String str, IVoldTaskListener iVoldTaskListener) throws RemoteException;

    void check(String str) throws RemoteException;

    void checkEncryption(String str) throws RemoteException;

    void commitChanges() throws RemoteException;

    String createObb(String str, String str2, int i) throws RemoteException;

    String createStubVolume(String str, String str2, String str3, String str4, String str5) throws RemoteException;

    void createUserKey(int i, int i2, boolean z) throws RemoteException;

    void createUserKeyIsec(int i, int i2, boolean z) throws RemoteException;

    void cryptsdAddKeyAuth(int i, int i2, String str, String str2) throws RemoteException;

    void cryptsdBackupInfo() throws RemoteException;

    void cryptsdDisable(String str) throws RemoteException;

    void cryptsdEnable(String str) throws RemoteException;

    void cryptsdUnlockKey(int i, int i2, String str, String str2) throws RemoteException;

    void destroyObb(String str) throws RemoteException;

    void destroySandboxForApp(String str, String str2, int i) throws RemoteException;

    void destroyStubVolume(String str) throws RemoteException;

    void destroyUserKey(int i) throws RemoteException;

    void destroyUserKeyIsec(int i) throws RemoteException;

    void destroyUserStorage(String str, int i, int i2) throws RemoteException;

    void encryptFstab(String str, String str2) throws RemoteException;

    void fbeEnable() throws RemoteException;

    void fdeChangePassword(int i, String str) throws RemoteException;

    void fdeCheckPassword(String str) throws RemoteException;

    void fdeClearPassword() throws RemoteException;

    int fdeComplete() throws RemoteException;

    void fdeEnable(int i, String str, int i2) throws RemoteException;

    String fdeGetField(String str) throws RemoteException;

    String fdeGetPassword() throws RemoteException;

    int fdeGetPasswordType() throws RemoteException;

    void fdeRestart() throws RemoteException;

    void fdeSetField(String str, String str2) throws RemoteException;

    void fdeVerifyPassword(String str) throws RemoteException;

    void fixateNewestUserKeyAuth(int i) throws RemoteException;

    void fixateNewestUserKeyAuthIsec(int i) throws RemoteException;

    void forgetPartition(String str, String str2) throws RemoteException;

    void format(String str, String str2) throws RemoteException;

    void fstrim(int i, IVoldTaskListener iVoldTaskListener) throws RemoteException;

    String getKeyDesc(int i, int i2, int i3) throws RemoteException;

    int getPrepareFlag(int i, int i2) throws RemoteException;

    void initUser0() throws RemoteException;

    boolean isConvertibleToFbe() throws RemoteException;

    void lockScreenPrepare(int i, int i2, int i3) throws RemoteException;

    void lockUserKey(int i) throws RemoteException;

    void lockUserKeyIsec(int i) throws RemoteException;

    void lockUserScreenIsec(int i, int i2) throws RemoteException;

    void markBootAttempt() throws RemoteException;

    void mkdirs(String str) throws RemoteException;

    void monitor() throws RemoteException;

    void mount(String str, int i, int i2) throws RemoteException;

    FileDescriptor mountAppFuse(int i, int i2) throws RemoteException;

    String mountCifs(String str, String str2) throws RemoteException;

    void mountDefaultEncrypted() throws RemoteException;

    void mountForContinuance() throws RemoteException;

    void mountFstab(String str, String str2) throws RemoteException;

    void moveStorage(String str, String str2, IVoldTaskListener iVoldTaskListener) throws RemoteException;

    boolean needsCheckpoint() throws RemoteException;

    boolean needsRollback() throws RemoteException;

    void onSecureKeyguardStateChanged(boolean z) throws RemoteException;

    void onUserAdded(int i, int i2) throws RemoteException;

    void onUserRemoved(int i) throws RemoteException;

    void onUserStarted(int i) throws RemoteException;

    void onUserStopped(int i) throws RemoteException;

    FileDescriptor openAppFuseFile(int i, int i2, int i3, int i4) throws RemoteException;

    void partition(String str, int i, int i2) throws RemoteException;

    void prepareCheckpoint() throws RemoteException;

    void prepareSandboxForApp(String str, int i, String str2, int i2) throws RemoteException;

    void prepareUserStorage(String str, int i, int i2, int i3) throws RemoteException;

    void remountUid(int i, int i2) throws RemoteException;

    void reset() throws RemoteException;

    void restoreCheckpoint(String str) throws RemoteException;

    void restoreCheckpointPart(String str, int i) throws RemoteException;

    void runIdleMaint(IVoldTaskListener iVoldTaskListener) throws RemoteException;

    int sdbackupCheckRW() throws RemoteException;

    String sdbackupGetCid(int i) throws RemoteException;

    int sdbackupGetDevStat(int i) throws RemoteException;

    void sdlockClearPassword(String str) throws RemoteException;

    void sdlockErase() throws RemoteException;

    String sdlockGetCid() throws RemoteException;

    void sdlockLock(String str) throws RemoteException;

    String sdlockPoll() throws RemoteException;

    String sdlockReadPassword() throws RemoteException;

    void sdlockSetPassword(String str) throws RemoteException;

    void sdlockUnlock(String str) throws RemoteException;

    void sdlockWritePassword(String str) throws RemoteException;

    void sendBlockUserId(String str, int i, int i2) throws RemoteException;

    void setListener(IVoldListener iVoldListener) throws RemoteException;

    void shutdown() throws RemoteException;

    void startCheckpoint(int i) throws RemoteException;

    int supportCifs() throws RemoteException;

    boolean supportsBlockCheckpoint() throws RemoteException;

    boolean supportsCheckpoint() throws RemoteException;

    boolean supportsFileCheckpoint() throws RemoteException;

    void umountForContinuance() throws RemoteException;

    void unlockUserKey(int i, int i2, String str, String str2) throws RemoteException;

    void unlockUserKeyIsec(int i, int i2, String str, String str2) throws RemoteException;

    void unlockUserScreenIsec(int i, int i2, String str, String str2, int i3) throws RemoteException;

    void unmount(String str) throws RemoteException;

    void unmountAppFuse(int i, int i2) throws RemoteException;

    void unmountCifs(String str) throws RemoteException;

    public static class Default implements IVold {
        @Override // android.os.IVold
        public void setListener(IVoldListener listener) throws RemoteException {
        }

        @Override // android.os.IVold
        public void monitor() throws RemoteException {
        }

        @Override // android.os.IVold
        public void reset() throws RemoteException {
        }

        @Override // android.os.IVold
        public void shutdown() throws RemoteException {
        }

        @Override // android.os.IVold
        public void onUserAdded(int userId, int userSerial) throws RemoteException {
        }

        @Override // android.os.IVold
        public void onUserRemoved(int userId) throws RemoteException {
        }

        @Override // android.os.IVold
        public void onUserStarted(int userId) throws RemoteException {
        }

        @Override // android.os.IVold
        public void onUserStopped(int userId) throws RemoteException {
        }

        @Override // android.os.IVold
        public void addAppIds(String[] packageNames, int[] appIds) throws RemoteException {
        }

        @Override // android.os.IVold
        public void addSandboxIds(int[] appIds, String[] sandboxIds) throws RemoteException {
        }

        @Override // android.os.IVold
        public void onSecureKeyguardStateChanged(boolean isShowing) throws RemoteException {
        }

        @Override // android.os.IVold
        public void partition(String diskId, int partitionType, int ratio) throws RemoteException {
        }

        @Override // android.os.IVold
        public void forgetPartition(String partGuid, String fsUuid) throws RemoteException {
        }

        @Override // android.os.IVold
        public void check(String volId) throws RemoteException {
        }

        @Override // android.os.IVold
        public void mount(String volId, int mountFlags, int mountUserId) throws RemoteException {
        }

        @Override // android.os.IVold
        public void unmount(String volId) throws RemoteException {
        }

        @Override // android.os.IVold
        public int supportCifs() throws RemoteException {
            return 0;
        }

        @Override // android.os.IVold
        public String mountCifs(String source, String option) throws RemoteException {
            return null;
        }

        @Override // android.os.IVold
        public void unmountCifs(String mountPoint) throws RemoteException {
        }

        @Override // android.os.IVold
        public void format(String volId, String fsType) throws RemoteException {
        }

        @Override // android.os.IVold
        public void benchmark(String volId, IVoldTaskListener listener) throws RemoteException {
        }

        @Override // android.os.IVold
        public void checkEncryption(String volId) throws RemoteException {
        }

        @Override // android.os.IVold
        public void moveStorage(String fromVolId, String toVolId, IVoldTaskListener listener) throws RemoteException {
        }

        @Override // android.os.IVold
        public void remountUid(int uid, int remountMode) throws RemoteException {
        }

        @Override // android.os.IVold
        public void mkdirs(String path) throws RemoteException {
        }

        @Override // android.os.IVold
        public String createObb(String sourcePath, String sourceKey, int ownerGid) throws RemoteException {
            return null;
        }

        @Override // android.os.IVold
        public void destroyObb(String volId) throws RemoteException {
        }

        @Override // android.os.IVold
        public void fstrim(int fstrimFlags, IVoldTaskListener listener) throws RemoteException {
        }

        @Override // android.os.IVold
        public void runIdleMaint(IVoldTaskListener listener) throws RemoteException {
        }

        @Override // android.os.IVold
        public void abortIdleMaint(IVoldTaskListener listener) throws RemoteException {
        }

        @Override // android.os.IVold
        public FileDescriptor mountAppFuse(int uid, int mountId) throws RemoteException {
            return null;
        }

        @Override // android.os.IVold
        public void unmountAppFuse(int uid, int mountId) throws RemoteException {
        }

        @Override // android.os.IVold
        public void fdeCheckPassword(String password) throws RemoteException {
        }

        @Override // android.os.IVold
        public void fdeRestart() throws RemoteException {
        }

        @Override // android.os.IVold
        public int fdeComplete() throws RemoteException {
            return 0;
        }

        @Override // android.os.IVold
        public void fdeEnable(int passwordType, String password, int encryptionFlags) throws RemoteException {
        }

        @Override // android.os.IVold
        public void fdeChangePassword(int passwordType, String password) throws RemoteException {
        }

        @Override // android.os.IVold
        public void fdeVerifyPassword(String password) throws RemoteException {
        }

        @Override // android.os.IVold
        public String fdeGetField(String key) throws RemoteException {
            return null;
        }

        @Override // android.os.IVold
        public void fdeSetField(String key, String value) throws RemoteException {
        }

        @Override // android.os.IVold
        public int fdeGetPasswordType() throws RemoteException {
            return 0;
        }

        @Override // android.os.IVold
        public String fdeGetPassword() throws RemoteException {
            return null;
        }

        @Override // android.os.IVold
        public void fdeClearPassword() throws RemoteException {
        }

        @Override // android.os.IVold
        public void fbeEnable() throws RemoteException {
        }

        @Override // android.os.IVold
        public void mountDefaultEncrypted() throws RemoteException {
        }

        @Override // android.os.IVold
        public void initUser0() throws RemoteException {
        }

        @Override // android.os.IVold
        public boolean isConvertibleToFbe() throws RemoteException {
            return false;
        }

        @Override // android.os.IVold
        public void mountFstab(String blkDevice, String mountPoint) throws RemoteException {
        }

        @Override // android.os.IVold
        public void encryptFstab(String blkDevice, String mountPoint) throws RemoteException {
        }

        @Override // android.os.IVold
        public void createUserKey(int userId, int userSerial, boolean ephemeral) throws RemoteException {
        }

        @Override // android.os.IVold
        public void destroyUserKey(int userId) throws RemoteException {
        }

        @Override // android.os.IVold
        public void addUserKeyAuth(int userId, int userSerial, String token, String secret) throws RemoteException {
        }

        @Override // android.os.IVold
        public void fixateNewestUserKeyAuth(int userId) throws RemoteException {
        }

        @Override // android.os.IVold
        public void unlockUserKey(int userId, int userSerial, String token, String secret) throws RemoteException {
        }

        @Override // android.os.IVold
        public void lockUserKey(int userId) throws RemoteException {
        }

        @Override // android.os.IVold
        public void prepareUserStorage(String uuid, int userId, int userSerial, int storageFlags) throws RemoteException {
        }

        @Override // android.os.IVold
        public void destroyUserStorage(String uuid, int userId, int storageFlags) throws RemoteException {
        }

        @Override // android.os.IVold
        public void prepareSandboxForApp(String packageName, int appId, String sandboxId, int userId) throws RemoteException {
        }

        @Override // android.os.IVold
        public void destroySandboxForApp(String packageName, String sandboxId, int userId) throws RemoteException {
        }

        @Override // android.os.IVold
        public void startCheckpoint(int retry) throws RemoteException {
        }

        @Override // android.os.IVold
        public boolean needsCheckpoint() throws RemoteException {
            return false;
        }

        @Override // android.os.IVold
        public boolean needsRollback() throws RemoteException {
            return false;
        }

        @Override // android.os.IVold
        public void abortChanges(String device, boolean retry) throws RemoteException {
        }

        @Override // android.os.IVold
        public void commitChanges() throws RemoteException {
        }

        @Override // android.os.IVold
        public void prepareCheckpoint() throws RemoteException {
        }

        @Override // android.os.IVold
        public void restoreCheckpoint(String device) throws RemoteException {
        }

        @Override // android.os.IVold
        public void restoreCheckpointPart(String device, int count) throws RemoteException {
        }

        @Override // android.os.IVold
        public void markBootAttempt() throws RemoteException {
        }

        @Override // android.os.IVold
        public boolean supportsCheckpoint() throws RemoteException {
            return false;
        }

        @Override // android.os.IVold
        public boolean supportsBlockCheckpoint() throws RemoteException {
            return false;
        }

        @Override // android.os.IVold
        public boolean supportsFileCheckpoint() throws RemoteException {
            return false;
        }

        @Override // android.os.IVold
        public String createStubVolume(String sourcePath, String mountPath, String fsType, String fsUuid, String fsLabel) throws RemoteException {
            return null;
        }

        @Override // android.os.IVold
        public void destroyStubVolume(String volId) throws RemoteException {
        }

        @Override // android.os.IVold
        public void createUserKeyIsec(int userId, int userSerial, boolean ephemeral) throws RemoteException {
        }

        @Override // android.os.IVold
        public void destroyUserKeyIsec(int userId) throws RemoteException {
        }

        @Override // android.os.IVold
        public void unlockUserKeyIsec(int userId, int userSerial, String token, String secret) throws RemoteException {
        }

        @Override // android.os.IVold
        public void lockUserKeyIsec(int userId) throws RemoteException {
        }

        @Override // android.os.IVold
        public void addUserKeyAuthIsec(int userId, int userSerial, String token, String secret) throws RemoteException {
        }

        @Override // android.os.IVold
        public void fixateNewestUserKeyAuthIsec(int userId) throws RemoteException {
        }

        @Override // android.os.IVold
        public void lockUserScreenIsec(int userId, int userSerial) throws RemoteException {
        }

        @Override // android.os.IVold
        public void unlockUserScreenIsec(int userId, int userSerial, String token, String secret, int authType) throws RemoteException {
        }

        @Override // android.os.IVold
        public void lockScreenPrepare(int userId, int userSerial, int flag) throws RemoteException {
        }

        @Override // android.os.IVold
        public String getKeyDesc(int user_id, int serial, int sdpClass) throws RemoteException {
            return null;
        }

        @Override // android.os.IVold
        public int getPrepareFlag(int user_id, int serial) throws RemoteException {
            return 0;
        }

        @Override // android.os.IVold
        public void sendBlockUserId(String volId, int blockUserId, int extAppId) throws RemoteException {
        }

        @Override // android.os.IVold
        public void sdlockSetPassword(String password) throws RemoteException {
        }

        @Override // android.os.IVold
        public void sdlockClearPassword(String password) throws RemoteException {
        }

        @Override // android.os.IVold
        public void sdlockLock(String password) throws RemoteException {
        }

        @Override // android.os.IVold
        public void sdlockUnlock(String password) throws RemoteException {
        }

        @Override // android.os.IVold
        public void sdlockErase() throws RemoteException {
        }

        @Override // android.os.IVold
        public String sdlockPoll() throws RemoteException {
            return null;
        }

        @Override // android.os.IVold
        public String sdlockGetCid() throws RemoteException {
            return null;
        }

        @Override // android.os.IVold
        public String sdlockReadPassword() throws RemoteException {
            return null;
        }

        @Override // android.os.IVold
        public void sdlockWritePassword(String password) throws RemoteException {
        }

        @Override // android.os.IVold
        public int sdbackupGetDevStat(int dev) throws RemoteException {
            return 0;
        }

        @Override // android.os.IVold
        public String sdbackupGetCid(int dev) throws RemoteException {
            return null;
        }

        @Override // android.os.IVold
        public int sdbackupCheckRW() throws RemoteException {
            return 0;
        }

        @Override // android.os.IVold
        public FileDescriptor openAppFuseFile(int uid, int mountId, int fileId, int flags) throws RemoteException {
            return null;
        }

        @Override // android.os.IVold
        public void umountForContinuance() throws RemoteException {
        }

        @Override // android.os.IVold
        public void mountForContinuance() throws RemoteException {
        }

        @Override // android.os.IVold
        public void cryptsdEnable(String volId) throws RemoteException {
        }

        @Override // android.os.IVold
        public void cryptsdDisable(String volId) throws RemoteException {
        }

        @Override // android.os.IVold
        public void cryptsdAddKeyAuth(int uid, int serial, String token, String secret) throws RemoteException {
        }

        @Override // android.os.IVold
        public void cryptsdUnlockKey(int uid, int serial, String token, String secret) throws RemoteException {
        }

        @Override // android.os.IVold
        public void cryptsdBackupInfo() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IVold {
        private static final String DESCRIPTOR = "android.os.IVold";
        static final int TRANSACTION_abortChanges = 63;
        static final int TRANSACTION_abortIdleMaint = 30;
        static final int TRANSACTION_addAppIds = 9;
        static final int TRANSACTION_addSandboxIds = 10;
        static final int TRANSACTION_addUserKeyAuth = 52;
        static final int TRANSACTION_addUserKeyAuthIsec = 78;
        static final int TRANSACTION_benchmark = 21;
        static final int TRANSACTION_check = 14;
        static final int TRANSACTION_checkEncryption = 22;
        static final int TRANSACTION_commitChanges = 64;
        static final int TRANSACTION_createObb = 26;
        static final int TRANSACTION_createStubVolume = 72;
        static final int TRANSACTION_createUserKey = 50;
        static final int TRANSACTION_createUserKeyIsec = 74;
        static final int TRANSACTION_cryptsdAddKeyAuth = 103;
        static final int TRANSACTION_cryptsdBackupInfo = 105;
        static final int TRANSACTION_cryptsdDisable = 102;
        static final int TRANSACTION_cryptsdEnable = 101;
        static final int TRANSACTION_cryptsdUnlockKey = 104;
        static final int TRANSACTION_destroyObb = 27;
        static final int TRANSACTION_destroySandboxForApp = 59;
        static final int TRANSACTION_destroyStubVolume = 73;
        static final int TRANSACTION_destroyUserKey = 51;
        static final int TRANSACTION_destroyUserKeyIsec = 75;
        static final int TRANSACTION_destroyUserStorage = 57;
        static final int TRANSACTION_encryptFstab = 49;
        static final int TRANSACTION_fbeEnable = 44;
        static final int TRANSACTION_fdeChangePassword = 37;
        static final int TRANSACTION_fdeCheckPassword = 33;
        static final int TRANSACTION_fdeClearPassword = 43;
        static final int TRANSACTION_fdeComplete = 35;
        static final int TRANSACTION_fdeEnable = 36;
        static final int TRANSACTION_fdeGetField = 39;
        static final int TRANSACTION_fdeGetPassword = 42;
        static final int TRANSACTION_fdeGetPasswordType = 41;
        static final int TRANSACTION_fdeRestart = 34;
        static final int TRANSACTION_fdeSetField = 40;
        static final int TRANSACTION_fdeVerifyPassword = 38;
        static final int TRANSACTION_fixateNewestUserKeyAuth = 53;
        static final int TRANSACTION_fixateNewestUserKeyAuthIsec = 79;
        static final int TRANSACTION_forgetPartition = 13;
        static final int TRANSACTION_format = 20;
        static final int TRANSACTION_fstrim = 28;
        static final int TRANSACTION_getKeyDesc = 83;
        static final int TRANSACTION_getPrepareFlag = 84;
        static final int TRANSACTION_initUser0 = 46;
        static final int TRANSACTION_isConvertibleToFbe = 47;
        static final int TRANSACTION_lockScreenPrepare = 82;
        static final int TRANSACTION_lockUserKey = 55;
        static final int TRANSACTION_lockUserKeyIsec = 77;
        static final int TRANSACTION_lockUserScreenIsec = 80;
        static final int TRANSACTION_markBootAttempt = 68;
        static final int TRANSACTION_mkdirs = 25;
        static final int TRANSACTION_monitor = 2;
        static final int TRANSACTION_mount = 15;
        static final int TRANSACTION_mountAppFuse = 31;
        static final int TRANSACTION_mountCifs = 18;
        static final int TRANSACTION_mountDefaultEncrypted = 45;
        static final int TRANSACTION_mountForContinuance = 100;
        static final int TRANSACTION_mountFstab = 48;
        static final int TRANSACTION_moveStorage = 23;
        static final int TRANSACTION_needsCheckpoint = 61;
        static final int TRANSACTION_needsRollback = 62;
        static final int TRANSACTION_onSecureKeyguardStateChanged = 11;
        static final int TRANSACTION_onUserAdded = 5;
        static final int TRANSACTION_onUserRemoved = 6;
        static final int TRANSACTION_onUserStarted = 7;
        static final int TRANSACTION_onUserStopped = 8;
        static final int TRANSACTION_openAppFuseFile = 98;
        static final int TRANSACTION_partition = 12;
        static final int TRANSACTION_prepareCheckpoint = 65;
        static final int TRANSACTION_prepareSandboxForApp = 58;
        static final int TRANSACTION_prepareUserStorage = 56;
        static final int TRANSACTION_remountUid = 24;
        static final int TRANSACTION_reset = 3;
        static final int TRANSACTION_restoreCheckpoint = 66;
        static final int TRANSACTION_restoreCheckpointPart = 67;
        static final int TRANSACTION_runIdleMaint = 29;
        static final int TRANSACTION_sdbackupCheckRW = 97;
        static final int TRANSACTION_sdbackupGetCid = 96;
        static final int TRANSACTION_sdbackupGetDevStat = 95;
        static final int TRANSACTION_sdlockClearPassword = 87;
        static final int TRANSACTION_sdlockErase = 90;
        static final int TRANSACTION_sdlockGetCid = 92;
        static final int TRANSACTION_sdlockLock = 88;
        static final int TRANSACTION_sdlockPoll = 91;
        static final int TRANSACTION_sdlockReadPassword = 93;
        static final int TRANSACTION_sdlockSetPassword = 86;
        static final int TRANSACTION_sdlockUnlock = 89;
        static final int TRANSACTION_sdlockWritePassword = 94;
        static final int TRANSACTION_sendBlockUserId = 85;
        static final int TRANSACTION_setListener = 1;
        static final int TRANSACTION_shutdown = 4;
        static final int TRANSACTION_startCheckpoint = 60;
        static final int TRANSACTION_supportCifs = 17;
        static final int TRANSACTION_supportsBlockCheckpoint = 70;
        static final int TRANSACTION_supportsCheckpoint = 69;
        static final int TRANSACTION_supportsFileCheckpoint = 71;
        static final int TRANSACTION_umountForContinuance = 99;
        static final int TRANSACTION_unlockUserKey = 54;
        static final int TRANSACTION_unlockUserKeyIsec = 76;
        static final int TRANSACTION_unlockUserScreenIsec = 81;
        static final int TRANSACTION_unmount = 16;
        static final int TRANSACTION_unmountAppFuse = 32;
        static final int TRANSACTION_unmountCifs = 19;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVold asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVold)) {
                return new Proxy(obj);
            }
            return (IVold) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                boolean _arg2 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        setListener(IVoldListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        monitor();
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        reset();
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        shutdown();
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onUserAdded(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        onUserRemoved(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        onUserStarted(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        onUserStopped(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        addAppIds(data.createStringArray(), data.createIntArray());
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        addSandboxIds(data.createIntArray(), data.createStringArray());
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        onSecureKeyguardStateChanged(_arg2);
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        partition(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        forgetPartition(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        check(data.readString());
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        mount(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        unmount(data.readString());
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = supportCifs();
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        String _result2 = mountCifs(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result2);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        unmountCifs(data.readString());
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        format(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        benchmark(data.readString(), IVoldTaskListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        checkEncryption(data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_moveStorage /* 23 */:
                        data.enforceInterface(DESCRIPTOR);
                        moveStorage(data.readString(), data.readString(), IVoldTaskListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_remountUid /* 24 */:
                        data.enforceInterface(DESCRIPTOR);
                        remountUid(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_mkdirs /* 25 */:
                        data.enforceInterface(DESCRIPTOR);
                        mkdirs(data.readString());
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        String _result3 = createObb(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result3);
                        return true;
                    case TRANSACTION_destroyObb /* 27 */:
                        data.enforceInterface(DESCRIPTOR);
                        destroyObb(data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_fstrim /* 28 */:
                        data.enforceInterface(DESCRIPTOR);
                        fstrim(data.readInt(), IVoldTaskListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        runIdleMaint(IVoldTaskListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        abortIdleMaint(IVoldTaskListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        FileDescriptor _result4 = mountAppFuse(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeRawFileDescriptor(_result4);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        unmountAppFuse(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        fdeCheckPassword(data.readString());
                        reply.writeNoException();
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        fdeRestart();
                        reply.writeNoException();
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = fdeComplete();
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        fdeEnable(data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        fdeChangePassword(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        fdeVerifyPassword(data.readString());
                        reply.writeNoException();
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        String _result6 = fdeGetField(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result6);
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        fdeSetField(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = fdeGetPasswordType();
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        String _result8 = fdeGetPassword();
                        reply.writeNoException();
                        reply.writeString(_result8);
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        fdeClearPassword();
                        reply.writeNoException();
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        fbeEnable();
                        reply.writeNoException();
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        mountDefaultEncrypted();
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_initUser0 /* 46 */:
                        data.enforceInterface(DESCRIPTOR);
                        initUser0();
                        reply.writeNoException();
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isConvertibleToFbe = isConvertibleToFbe();
                        reply.writeNoException();
                        reply.writeInt(isConvertibleToFbe ? 1 : 0);
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        mountFstab(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        encryptFstab(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        int _arg1 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        createUserKey(_arg0, _arg1, _arg2);
                        reply.writeNoException();
                        return true;
                    case 51:
                        data.enforceInterface(DESCRIPTOR);
                        destroyUserKey(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 52:
                        data.enforceInterface(DESCRIPTOR);
                        addUserKeyAuth(data.readInt(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 53:
                        data.enforceInterface(DESCRIPTOR);
                        fixateNewestUserKeyAuth(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 54:
                        data.enforceInterface(DESCRIPTOR);
                        unlockUserKey(data.readInt(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 55:
                        data.enforceInterface(DESCRIPTOR);
                        lockUserKey(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 56:
                        data.enforceInterface(DESCRIPTOR);
                        prepareUserStorage(data.readString(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 57:
                        data.enforceInterface(DESCRIPTOR);
                        destroyUserStorage(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 58:
                        data.enforceInterface(DESCRIPTOR);
                        prepareSandboxForApp(data.readString(), data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 59:
                        data.enforceInterface(DESCRIPTOR);
                        destroySandboxForApp(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_startCheckpoint /* 60 */:
                        data.enforceInterface(DESCRIPTOR);
                        startCheckpoint(data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_needsCheckpoint /* 61 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean needsCheckpoint = needsCheckpoint();
                        reply.writeNoException();
                        reply.writeInt(needsCheckpoint ? 1 : 0);
                        return true;
                    case TRANSACTION_needsRollback /* 62 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean needsRollback = needsRollback();
                        reply.writeNoException();
                        reply.writeInt(needsRollback ? 1 : 0);
                        return true;
                    case TRANSACTION_abortChanges /* 63 */:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        abortChanges(_arg02, _arg2);
                        reply.writeNoException();
                        return true;
                    case 64:
                        data.enforceInterface(DESCRIPTOR);
                        commitChanges();
                        reply.writeNoException();
                        return true;
                    case 65:
                        data.enforceInterface(DESCRIPTOR);
                        prepareCheckpoint();
                        reply.writeNoException();
                        return true;
                    case 66:
                        data.enforceInterface(DESCRIPTOR);
                        restoreCheckpoint(data.readString());
                        reply.writeNoException();
                        return true;
                    case 67:
                        data.enforceInterface(DESCRIPTOR);
                        restoreCheckpointPart(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 68:
                        data.enforceInterface(DESCRIPTOR);
                        markBootAttempt();
                        reply.writeNoException();
                        return true;
                    case 69:
                        data.enforceInterface(DESCRIPTOR);
                        boolean supportsCheckpoint = supportsCheckpoint();
                        reply.writeNoException();
                        reply.writeInt(supportsCheckpoint ? 1 : 0);
                        return true;
                    case 70:
                        data.enforceInterface(DESCRIPTOR);
                        boolean supportsBlockCheckpoint = supportsBlockCheckpoint();
                        reply.writeNoException();
                        reply.writeInt(supportsBlockCheckpoint ? 1 : 0);
                        return true;
                    case 71:
                        data.enforceInterface(DESCRIPTOR);
                        boolean supportsFileCheckpoint = supportsFileCheckpoint();
                        reply.writeNoException();
                        reply.writeInt(supportsFileCheckpoint ? 1 : 0);
                        return true;
                    case 72:
                        data.enforceInterface(DESCRIPTOR);
                        String _result9 = createStubVolume(data.readString(), data.readString(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result9);
                        return true;
                    case 73:
                        data.enforceInterface(DESCRIPTOR);
                        destroyStubVolume(data.readString());
                        reply.writeNoException();
                        return true;
                    case 74:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        int _arg12 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        createUserKeyIsec(_arg03, _arg12, _arg2);
                        reply.writeNoException();
                        return true;
                    case 75:
                        data.enforceInterface(DESCRIPTOR);
                        destroyUserKeyIsec(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 76:
                        data.enforceInterface(DESCRIPTOR);
                        unlockUserKeyIsec(data.readInt(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 77:
                        data.enforceInterface(DESCRIPTOR);
                        lockUserKeyIsec(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 78:
                        data.enforceInterface(DESCRIPTOR);
                        addUserKeyAuthIsec(data.readInt(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 79:
                        data.enforceInterface(DESCRIPTOR);
                        fixateNewestUserKeyAuthIsec(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 80:
                        data.enforceInterface(DESCRIPTOR);
                        lockUserScreenIsec(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 81:
                        data.enforceInterface(DESCRIPTOR);
                        unlockUserScreenIsec(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 82:
                        data.enforceInterface(DESCRIPTOR);
                        lockScreenPrepare(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 83:
                        data.enforceInterface(DESCRIPTOR);
                        String _result10 = getKeyDesc(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result10);
                        return true;
                    case 84:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = getPrepareFlag(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 85:
                        data.enforceInterface(DESCRIPTOR);
                        sendBlockUserId(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 86:
                        data.enforceInterface(DESCRIPTOR);
                        sdlockSetPassword(data.readString());
                        reply.writeNoException();
                        return true;
                    case 87:
                        data.enforceInterface(DESCRIPTOR);
                        sdlockClearPassword(data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_sdlockLock /* 88 */:
                        data.enforceInterface(DESCRIPTOR);
                        sdlockLock(data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_sdlockUnlock /* 89 */:
                        data.enforceInterface(DESCRIPTOR);
                        sdlockUnlock(data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_sdlockErase /* 90 */:
                        data.enforceInterface(DESCRIPTOR);
                        sdlockErase();
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_sdlockPoll /* 91 */:
                        data.enforceInterface(DESCRIPTOR);
                        String _result12 = sdlockPoll();
                        reply.writeNoException();
                        reply.writeString(_result12);
                        return true;
                    case TRANSACTION_sdlockGetCid /* 92 */:
                        data.enforceInterface(DESCRIPTOR);
                        String _result13 = sdlockGetCid();
                        reply.writeNoException();
                        reply.writeString(_result13);
                        return true;
                    case TRANSACTION_sdlockReadPassword /* 93 */:
                        data.enforceInterface(DESCRIPTOR);
                        String _result14 = sdlockReadPassword();
                        reply.writeNoException();
                        reply.writeString(_result14);
                        return true;
                    case TRANSACTION_sdlockWritePassword /* 94 */:
                        data.enforceInterface(DESCRIPTOR);
                        sdlockWritePassword(data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_sdbackupGetDevStat /* 95 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result15 = sdbackupGetDevStat(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case 96:
                        data.enforceInterface(DESCRIPTOR);
                        String _result16 = sdbackupGetCid(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result16);
                        return true;
                    case 97:
                        data.enforceInterface(DESCRIPTOR);
                        int _result17 = sdbackupCheckRW();
                        reply.writeNoException();
                        reply.writeInt(_result17);
                        return true;
                    case 98:
                        data.enforceInterface(DESCRIPTOR);
                        FileDescriptor _result18 = openAppFuseFile(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeRawFileDescriptor(_result18);
                        return true;
                    case 99:
                        data.enforceInterface(DESCRIPTOR);
                        umountForContinuance();
                        reply.writeNoException();
                        return true;
                    case 100:
                        data.enforceInterface(DESCRIPTOR);
                        mountForContinuance();
                        reply.writeNoException();
                        return true;
                    case 101:
                        data.enforceInterface(DESCRIPTOR);
                        cryptsdEnable(data.readString());
                        reply.writeNoException();
                        return true;
                    case 102:
                        data.enforceInterface(DESCRIPTOR);
                        cryptsdDisable(data.readString());
                        reply.writeNoException();
                        return true;
                    case 103:
                        data.enforceInterface(DESCRIPTOR);
                        cryptsdAddKeyAuth(data.readInt(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 104:
                        data.enforceInterface(DESCRIPTOR);
                        cryptsdUnlockKey(data.readInt(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 105:
                        data.enforceInterface(DESCRIPTOR);
                        cryptsdBackupInfo();
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
        public static class Proxy implements IVold {
            public static IVold sDefaultImpl;
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

            @Override // android.os.IVold
            public void setListener(IVoldListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void monitor() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().monitor();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void reset() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reset();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void shutdown() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().shutdown();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void onUserAdded(int userId, int userSerial) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(userSerial);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onUserAdded(userId, userSerial);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void onUserRemoved(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onUserRemoved(userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void onUserStarted(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onUserStarted(userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void onUserStopped(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onUserStopped(userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void addAppIds(String[] packageNames, int[] appIds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(packageNames);
                    _data.writeIntArray(appIds);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addAppIds(packageNames, appIds);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void addSandboxIds(int[] appIds, String[] sandboxIds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(appIds);
                    _data.writeStringArray(sandboxIds);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addSandboxIds(appIds, sandboxIds);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void onSecureKeyguardStateChanged(boolean isShowing) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isShowing ? 1 : 0);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onSecureKeyguardStateChanged(isShowing);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void partition(String diskId, int partitionType, int ratio) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(diskId);
                    _data.writeInt(partitionType);
                    _data.writeInt(ratio);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().partition(diskId, partitionType, ratio);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void forgetPartition(String partGuid, String fsUuid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(partGuid);
                    _data.writeString(fsUuid);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().forgetPartition(partGuid, fsUuid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void check(String volId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().check(volId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void mount(String volId, int mountFlags, int mountUserId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    _data.writeInt(mountFlags);
                    _data.writeInt(mountUserId);
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().mount(volId, mountFlags, mountUserId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void unmount(String volId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unmount(volId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public int supportCifs() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().supportCifs();
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

            @Override // android.os.IVold
            public String mountCifs(String source, String option) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(source);
                    _data.writeString(option);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().mountCifs(source, option);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void unmountCifs(String mountPoint) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mountPoint);
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unmountCifs(mountPoint);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void format(String volId, String fsType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    _data.writeString(fsType);
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().format(volId, fsType);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void benchmark(String volId, IVoldTaskListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(21, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().benchmark(volId, listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void checkEncryption(String volId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().checkEncryption(volId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void moveStorage(String fromVolId, String toVolId, IVoldTaskListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fromVolId);
                    _data.writeString(toVolId);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(Stub.TRANSACTION_moveStorage, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().moveStorage(fromVolId, toVolId, listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void remountUid(int uid, int remountMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(remountMode);
                    if (this.mRemote.transact(Stub.TRANSACTION_remountUid, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().remountUid(uid, remountMode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void mkdirs(String path) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    if (this.mRemote.transact(Stub.TRANSACTION_mkdirs, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().mkdirs(path);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public String createObb(String sourcePath, String sourceKey, int ownerGid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sourcePath);
                    _data.writeString(sourceKey);
                    _data.writeInt(ownerGid);
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createObb(sourcePath, sourceKey, ownerGid);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void destroyObb(String volId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    if (this.mRemote.transact(Stub.TRANSACTION_destroyObb, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().destroyObb(volId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void fstrim(int fstrimFlags, IVoldTaskListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fstrimFlags);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(Stub.TRANSACTION_fstrim, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().fstrim(fstrimFlags, listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void runIdleMaint(IVoldTaskListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(29, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().runIdleMaint(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void abortIdleMaint(IVoldTaskListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(30, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().abortIdleMaint(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public FileDescriptor mountAppFuse(int uid, int mountId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(mountId);
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().mountAppFuse(uid, mountId);
                    }
                    _reply.readException();
                    FileDescriptor _result = _reply.readRawFileDescriptor();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void unmountAppFuse(int uid, int mountId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(mountId);
                    if (this.mRemote.transact(32, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unmountAppFuse(uid, mountId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void fdeCheckPassword(String password) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    if (this.mRemote.transact(33, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().fdeCheckPassword(password);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void fdeRestart() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(34, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().fdeRestart();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public int fdeComplete() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(35, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().fdeComplete();
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

            @Override // android.os.IVold
            public void fdeEnable(int passwordType, String password, int encryptionFlags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(passwordType);
                    _data.writeString(password);
                    _data.writeInt(encryptionFlags);
                    if (this.mRemote.transact(36, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().fdeEnable(passwordType, password, encryptionFlags);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void fdeChangePassword(int passwordType, String password) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(passwordType);
                    _data.writeString(password);
                    if (this.mRemote.transact(37, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().fdeChangePassword(passwordType, password);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void fdeVerifyPassword(String password) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    if (this.mRemote.transact(38, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().fdeVerifyPassword(password);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public String fdeGetField(String key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    if (!this.mRemote.transact(39, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().fdeGetField(key);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void fdeSetField(String key, String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeString(value);
                    if (this.mRemote.transact(40, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().fdeSetField(key, value);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public int fdeGetPasswordType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(41, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().fdeGetPasswordType();
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

            @Override // android.os.IVold
            public String fdeGetPassword() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(42, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().fdeGetPassword();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void fdeClearPassword() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(43, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().fdeClearPassword();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void fbeEnable() throws RemoteException {
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
                    Stub.getDefaultImpl().fbeEnable();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void mountDefaultEncrypted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(45, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().mountDefaultEncrypted();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void initUser0() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(Stub.TRANSACTION_initUser0, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().initUser0();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public boolean isConvertibleToFbe() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(47, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isConvertibleToFbe();
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

            @Override // android.os.IVold
            public void mountFstab(String blkDevice, String mountPoint) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(blkDevice);
                    _data.writeString(mountPoint);
                    if (this.mRemote.transact(48, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().mountFstab(blkDevice, mountPoint);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void encryptFstab(String blkDevice, String mountPoint) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(blkDevice);
                    _data.writeString(mountPoint);
                    if (this.mRemote.transact(49, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().encryptFstab(blkDevice, mountPoint);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void createUserKey(int userId, int userSerial, boolean ephemeral) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(userSerial);
                    _data.writeInt(ephemeral ? 1 : 0);
                    if (this.mRemote.transact(50, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().createUserKey(userId, userSerial, ephemeral);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void destroyUserKey(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(51, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().destroyUserKey(userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void addUserKeyAuth(int userId, int userSerial, String token, String secret) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(userSerial);
                    _data.writeString(token);
                    _data.writeString(secret);
                    if (this.mRemote.transact(52, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addUserKeyAuth(userId, userSerial, token, secret);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void fixateNewestUserKeyAuth(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(53, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().fixateNewestUserKeyAuth(userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void unlockUserKey(int userId, int userSerial, String token, String secret) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(userSerial);
                    _data.writeString(token);
                    _data.writeString(secret);
                    if (this.mRemote.transact(54, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unlockUserKey(userId, userSerial, token, secret);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void lockUserKey(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(55, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().lockUserKey(userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void prepareUserStorage(String uuid, int userId, int userSerial, int storageFlags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeInt(userId);
                    _data.writeInt(userSerial);
                    _data.writeInt(storageFlags);
                    if (this.mRemote.transact(56, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().prepareUserStorage(uuid, userId, userSerial, storageFlags);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void destroyUserStorage(String uuid, int userId, int storageFlags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeInt(userId);
                    _data.writeInt(storageFlags);
                    if (this.mRemote.transact(57, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().destroyUserStorage(uuid, userId, storageFlags);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void prepareSandboxForApp(String packageName, int appId, String sandboxId, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(appId);
                    _data.writeString(sandboxId);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(58, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().prepareSandboxForApp(packageName, appId, sandboxId, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void destroySandboxForApp(String packageName, String sandboxId, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(sandboxId);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(59, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().destroySandboxForApp(packageName, sandboxId, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void startCheckpoint(int retry) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(retry);
                    if (this.mRemote.transact(Stub.TRANSACTION_startCheckpoint, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startCheckpoint(retry);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public boolean needsCheckpoint() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_needsCheckpoint, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().needsCheckpoint();
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

            @Override // android.os.IVold
            public boolean needsRollback() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_needsRollback, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().needsRollback();
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

            @Override // android.os.IVold
            public void abortChanges(String device, boolean retry) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(device);
                    _data.writeInt(retry ? 1 : 0);
                    if (this.mRemote.transact(Stub.TRANSACTION_abortChanges, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().abortChanges(device, retry);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void commitChanges() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(64, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().commitChanges();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void prepareCheckpoint() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(65, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().prepareCheckpoint();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void restoreCheckpoint(String device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(device);
                    if (this.mRemote.transact(66, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().restoreCheckpoint(device);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void restoreCheckpointPart(String device, int count) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(device);
                    _data.writeInt(count);
                    if (this.mRemote.transact(67, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().restoreCheckpointPart(device, count);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void markBootAttempt() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(68, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().markBootAttempt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public boolean supportsCheckpoint() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(69, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().supportsCheckpoint();
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

            @Override // android.os.IVold
            public boolean supportsBlockCheckpoint() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(70, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().supportsBlockCheckpoint();
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

            @Override // android.os.IVold
            public boolean supportsFileCheckpoint() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(71, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().supportsFileCheckpoint();
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

            @Override // android.os.IVold
            public String createStubVolume(String sourcePath, String mountPath, String fsType, String fsUuid, String fsLabel) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sourcePath);
                    _data.writeString(mountPath);
                    _data.writeString(fsType);
                    _data.writeString(fsUuid);
                    _data.writeString(fsLabel);
                    if (!this.mRemote.transact(72, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createStubVolume(sourcePath, mountPath, fsType, fsUuid, fsLabel);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void destroyStubVolume(String volId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    if (this.mRemote.transact(73, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().destroyStubVolume(volId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void createUserKeyIsec(int userId, int userSerial, boolean ephemeral) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(userSerial);
                    _data.writeInt(ephemeral ? 1 : 0);
                    if (this.mRemote.transact(74, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().createUserKeyIsec(userId, userSerial, ephemeral);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void destroyUserKeyIsec(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(75, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().destroyUserKeyIsec(userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void unlockUserKeyIsec(int userId, int userSerial, String token, String secret) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(userSerial);
                    _data.writeString(token);
                    _data.writeString(secret);
                    if (this.mRemote.transact(76, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unlockUserKeyIsec(userId, userSerial, token, secret);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void lockUserKeyIsec(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(77, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().lockUserKeyIsec(userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void addUserKeyAuthIsec(int userId, int userSerial, String token, String secret) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(userSerial);
                    _data.writeString(token);
                    _data.writeString(secret);
                    if (this.mRemote.transact(78, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addUserKeyAuthIsec(userId, userSerial, token, secret);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void fixateNewestUserKeyAuthIsec(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(79, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().fixateNewestUserKeyAuthIsec(userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void lockUserScreenIsec(int userId, int userSerial) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(userSerial);
                    if (this.mRemote.transact(80, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().lockUserScreenIsec(userId, userSerial);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void unlockUserScreenIsec(int userId, int userSerial, String token, String secret, int authType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(userSerial);
                    _data.writeString(token);
                    _data.writeString(secret);
                    _data.writeInt(authType);
                    if (this.mRemote.transact(81, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unlockUserScreenIsec(userId, userSerial, token, secret, authType);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void lockScreenPrepare(int userId, int userSerial, int flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(userSerial);
                    _data.writeInt(flag);
                    if (this.mRemote.transact(82, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().lockScreenPrepare(userId, userSerial, flag);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public String getKeyDesc(int user_id, int serial, int sdpClass) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(user_id);
                    _data.writeInt(serial);
                    _data.writeInt(sdpClass);
                    if (!this.mRemote.transact(83, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getKeyDesc(user_id, serial, sdpClass);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public int getPrepareFlag(int user_id, int serial) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(user_id);
                    _data.writeInt(serial);
                    if (!this.mRemote.transact(84, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPrepareFlag(user_id, serial);
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

            @Override // android.os.IVold
            public void sendBlockUserId(String volId, int blockUserId, int extAppId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    _data.writeInt(blockUserId);
                    _data.writeInt(extAppId);
                    if (this.mRemote.transact(85, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendBlockUserId(volId, blockUserId, extAppId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void sdlockSetPassword(String password) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    if (this.mRemote.transact(86, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sdlockSetPassword(password);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void sdlockClearPassword(String password) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    if (this.mRemote.transact(87, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sdlockClearPassword(password);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void sdlockLock(String password) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    if (this.mRemote.transact(Stub.TRANSACTION_sdlockLock, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sdlockLock(password);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void sdlockUnlock(String password) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    if (this.mRemote.transact(Stub.TRANSACTION_sdlockUnlock, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sdlockUnlock(password);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void sdlockErase() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(Stub.TRANSACTION_sdlockErase, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sdlockErase();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public String sdlockPoll() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_sdlockPoll, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sdlockPoll();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public String sdlockGetCid() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_sdlockGetCid, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sdlockGetCid();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public String sdlockReadPassword() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_sdlockReadPassword, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sdlockReadPassword();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void sdlockWritePassword(String password) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    if (this.mRemote.transact(Stub.TRANSACTION_sdlockWritePassword, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sdlockWritePassword(password);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public int sdbackupGetDevStat(int dev) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(dev);
                    if (!this.mRemote.transact(Stub.TRANSACTION_sdbackupGetDevStat, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sdbackupGetDevStat(dev);
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

            @Override // android.os.IVold
            public String sdbackupGetCid(int dev) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(dev);
                    if (!this.mRemote.transact(96, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sdbackupGetCid(dev);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public int sdbackupCheckRW() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(97, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sdbackupCheckRW();
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

            @Override // android.os.IVold
            public FileDescriptor openAppFuseFile(int uid, int mountId, int fileId, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(mountId);
                    _data.writeInt(fileId);
                    _data.writeInt(flags);
                    if (!this.mRemote.transact(98, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().openAppFuseFile(uid, mountId, fileId, flags);
                    }
                    _reply.readException();
                    FileDescriptor _result = _reply.readRawFileDescriptor();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void umountForContinuance() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(99, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().umountForContinuance();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void mountForContinuance() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(100, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().mountForContinuance();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void cryptsdEnable(String volId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    if (this.mRemote.transact(101, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cryptsdEnable(volId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void cryptsdDisable(String volId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    if (this.mRemote.transact(102, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cryptsdDisable(volId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void cryptsdAddKeyAuth(int uid, int serial, String token, String secret) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(serial);
                    _data.writeString(token);
                    _data.writeString(secret);
                    if (this.mRemote.transact(103, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cryptsdAddKeyAuth(uid, serial, token, secret);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void cryptsdUnlockKey(int uid, int serial, String token, String secret) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(serial);
                    _data.writeString(token);
                    _data.writeString(secret);
                    if (this.mRemote.transact(104, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cryptsdUnlockKey(uid, serial, token, secret);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IVold
            public void cryptsdBackupInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(105, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cryptsdBackupInfo();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IVold impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IVold getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
