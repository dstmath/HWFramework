package android.os;

import android.os.IVoldListener;
import android.os.IVoldTaskListener;
import java.io.FileDescriptor;
import java.util.List;

public interface IVold extends IInterface {
    public static final int ENCRYPTION_FLAG_NO_UI = 4;
    public static final int ENCRYPTION_STATE_ERROR_CORRUPT = -4;
    public static final int ENCRYPTION_STATE_ERROR_INCOMPLETE = -2;
    public static final int ENCRYPTION_STATE_ERROR_INCONSISTENT = -3;
    public static final int ENCRYPTION_STATE_ERROR_UNKNOWN = -1;
    public static final int ENCRYPTION_STATE_NONE = 1;
    public static final int ENCRYPTION_STATE_OK = 0;
    public static final int FSTRIM_FLAG_DEEP_TRIM = 1;
    public static final int MOUNT_FLAG_FULLWRITE = 128;
    public static final int MOUNT_FLAG_PRIMARY = 1;
    public static final int MOUNT_FLAG_READONLY = 64;
    public static final int MOUNT_FLAG_VISIBLE = 2;
    public static final int PARTITION_TYPE_MIXED = 2;
    public static final int PARTITION_TYPE_PRIVATE = 1;
    public static final int PARTITION_TYPE_PUBLIC = 0;
    public static final int PASSWORD_TYPE_DEFAULT = 1;
    public static final int PASSWORD_TYPE_PASSWORD = 0;
    public static final int PASSWORD_TYPE_PATTERN = 3;
    public static final int PASSWORD_TYPE_PIN = 2;
    public static final int REMOUNT_MODE_DEFAULT = 1;
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

    public static abstract class Stub extends Binder implements IVold {
        private static final String DESCRIPTOR = "android.os.IVold";
        static final int TRANSACTION_abortIdleMaint = 28;
        static final int TRANSACTION_addUserKeyAuth = 50;
        static final int TRANSACTION_addUserKeyAuthIsec = 60;
        static final int TRANSACTION_benchmark = 19;
        static final int TRANSACTION_check = 12;
        static final int TRANSACTION_checkEncryption = 20;
        static final int TRANSACTION_createObb = 24;
        static final int TRANSACTION_createUserKey = 48;
        static final int TRANSACTION_createUserKeyIsec = 56;
        static final int TRANSACTION_cryptsdAddKeyAuth = 83;
        static final int TRANSACTION_cryptsdBackupInfo = 85;
        static final int TRANSACTION_cryptsdDisable = 82;
        static final int TRANSACTION_cryptsdEnable = 81;
        static final int TRANSACTION_cryptsdUnlockKey = 84;
        static final int TRANSACTION_destroyObb = 25;
        static final int TRANSACTION_destroyUserKey = 49;
        static final int TRANSACTION_destroyUserKeyIsec = 57;
        static final int TRANSACTION_destroyUserStorage = 55;
        static final int TRANSACTION_encryptFstab = 47;
        static final int TRANSACTION_fbeEnable = 42;
        static final int TRANSACTION_fdeChangePassword = 35;
        static final int TRANSACTION_fdeCheckPassword = 31;
        static final int TRANSACTION_fdeClearPassword = 41;
        static final int TRANSACTION_fdeComplete = 33;
        static final int TRANSACTION_fdeEnable = 34;
        static final int TRANSACTION_fdeGetField = 37;
        static final int TRANSACTION_fdeGetPassword = 40;
        static final int TRANSACTION_fdeGetPasswordType = 39;
        static final int TRANSACTION_fdeRestart = 32;
        static final int TRANSACTION_fdeSetField = 38;
        static final int TRANSACTION_fdeVerifyPassword = 36;
        static final int TRANSACTION_fixateNewestUserKeyAuth = 51;
        static final int TRANSACTION_fixateNewestUserKeyAuthIsec = 61;
        static final int TRANSACTION_forgetPartition = 11;
        static final int TRANSACTION_format = 18;
        static final int TRANSACTION_fstrim = 26;
        static final int TRANSACTION_getKeyDesc = 65;
        static final int TRANSACTION_getPrepareFlag = 66;
        static final int TRANSACTION_initUser0 = 44;
        static final int TRANSACTION_isConvertibleToFbe = 45;
        static final int TRANSACTION_lockScreenPrepare = 64;
        static final int TRANSACTION_lockUserKey = 53;
        static final int TRANSACTION_lockUserKeyIsec = 59;
        static final int TRANSACTION_lockUserScreenIsec = 62;
        static final int TRANSACTION_mkdirs = 23;
        static final int TRANSACTION_monitor = 2;
        static final int TRANSACTION_mount = 13;
        static final int TRANSACTION_mountAppFuse = 29;
        static final int TRANSACTION_mountCifs = 16;
        static final int TRANSACTION_mountDefaultEncrypted = 43;
        static final int TRANSACTION_mountFstab = 46;
        static final int TRANSACTION_moveStorage = 21;
        static final int TRANSACTION_onSecureKeyguardStateChanged = 9;
        static final int TRANSACTION_onUserAdded = 5;
        static final int TRANSACTION_onUserRemoved = 6;
        static final int TRANSACTION_onUserStarted = 7;
        static final int TRANSACTION_onUserStopped = 8;
        static final int TRANSACTION_partition = 10;
        static final int TRANSACTION_prepareUserStorage = 54;
        static final int TRANSACTION_remountUid = 22;
        static final int TRANSACTION_reset = 3;
        static final int TRANSACTION_runIdleMaint = 27;
        static final int TRANSACTION_sdbackupCheckRW = 79;
        static final int TRANSACTION_sdbackupFileReliabilityCheck = 80;
        static final int TRANSACTION_sdbackupGetCid = 78;
        static final int TRANSACTION_sdbackupGetDevStat = 77;
        static final int TRANSACTION_sdlockClearPassword = 69;
        static final int TRANSACTION_sdlockErase = 72;
        static final int TRANSACTION_sdlockGetCid = 74;
        static final int TRANSACTION_sdlockLock = 70;
        static final int TRANSACTION_sdlockPoll = 73;
        static final int TRANSACTION_sdlockReadPassword = 75;
        static final int TRANSACTION_sdlockSetPassword = 68;
        static final int TRANSACTION_sdlockUnlock = 71;
        static final int TRANSACTION_sdlockWritePassword = 76;
        static final int TRANSACTION_sendBlockUserId = 67;
        static final int TRANSACTION_setListener = 1;
        static final int TRANSACTION_shutdown = 4;
        static final int TRANSACTION_supportCifs = 15;
        static final int TRANSACTION_unlockUserKey = 52;
        static final int TRANSACTION_unlockUserKeyIsec = 58;
        static final int TRANSACTION_unlockUserScreenIsec = 63;
        static final int TRANSACTION_unmount = 14;
        static final int TRANSACTION_unmountAppFuse = 30;
        static final int TRANSACTION_unmountCifs = 17;

        private static class Proxy implements IVold {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void setListener(IVoldListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void monitor() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reset() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void shutdown() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onUserAdded(int userId, int userSerial) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(userSerial);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onUserRemoved(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onUserStarted(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onUserStopped(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onSecureKeyguardStateChanged(boolean isShowing) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isShowing);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void partition(String diskId, int partitionType, int ratio) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(diskId);
                    _data.writeInt(partitionType);
                    _data.writeInt(ratio);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void forgetPartition(String partGuid, String fsUuid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(partGuid);
                    _data.writeString(fsUuid);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void check(String volId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void mount(String volId, int mountFlags, int mountUserId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    _data.writeInt(mountFlags);
                    _data.writeInt(mountUserId);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unmount(String volId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int supportCifs() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String mountCifs(String source, String option) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(source);
                    _data.writeString(option);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unmountCifs(String mountPoint) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mountPoint);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void format(String volId, String fsType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    _data.writeString(fsType);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void benchmark(String volId, IVoldTaskListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void checkEncryption(String volId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void moveStorage(String fromVolId, String toVolId, IVoldTaskListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fromVolId);
                    _data.writeString(toVolId);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void remountUid(int uid, int remountMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(remountMode);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void mkdirs(String path) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String createObb(String sourcePath, String sourceKey, int ownerGid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sourcePath);
                    _data.writeString(sourceKey);
                    _data.writeInt(ownerGid);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void destroyObb(String volId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void fstrim(int fstrimFlags, IVoldTaskListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fstrimFlags);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void runIdleMaint(IVoldTaskListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(Stub.TRANSACTION_runIdleMaint, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void abortIdleMaint(IVoldTaskListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public FileDescriptor mountAppFuse(int uid, int pid, int mountId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeInt(mountId);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readRawFileDescriptor();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unmountAppFuse(int uid, int pid, int mountId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeInt(mountId);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void fdeCheckPassword(String password) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void fdeRestart() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int fdeComplete() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void fdeEnable(int passwordType, String password, int encryptionFlags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(passwordType);
                    _data.writeString(password);
                    _data.writeInt(encryptionFlags);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void fdeChangePassword(int passwordType, String password) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(passwordType);
                    _data.writeString(password);
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void fdeVerifyPassword(String password) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String fdeGetField(String key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void fdeSetField(String key, String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeString(value);
                    this.mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int fdeGetPasswordType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String fdeGetPassword() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void fdeClearPassword() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void fbeEnable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(42, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void mountDefaultEncrypted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(43, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void initUser0() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(44, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isConvertibleToFbe() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(45, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void mountFstab(String mountPoint) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mountPoint);
                    this.mRemote.transact(46, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void encryptFstab(String mountPoint) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mountPoint);
                    this.mRemote.transact(47, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void createUserKey(int userId, int userSerial, boolean ephemeral) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(userSerial);
                    _data.writeInt(ephemeral);
                    this.mRemote.transact(48, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void destroyUserKey(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(49, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addUserKeyAuth(int userId, int userSerial, String token, String secret) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(userSerial);
                    _data.writeString(token);
                    _data.writeString(secret);
                    this.mRemote.transact(50, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void fixateNewestUserKeyAuth(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(51, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unlockUserKey(int userId, int userSerial, String token, String secret) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(userSerial);
                    _data.writeString(token);
                    _data.writeString(secret);
                    this.mRemote.transact(52, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void lockUserKey(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(53, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void prepareUserStorage(String uuid, int userId, int userSerial, int storageFlags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeInt(userId);
                    _data.writeInt(userSerial);
                    _data.writeInt(storageFlags);
                    this.mRemote.transact(54, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void destroyUserStorage(String uuid, int userId, int storageFlags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(uuid);
                    _data.writeInt(userId);
                    _data.writeInt(storageFlags);
                    this.mRemote.transact(55, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void createUserKeyIsec(int userId, int userSerial, boolean ephemeral) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(userSerial);
                    _data.writeInt(ephemeral);
                    this.mRemote.transact(56, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void destroyUserKeyIsec(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(57, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unlockUserKeyIsec(int userId, int userSerial, String token, String secret) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(userSerial);
                    _data.writeString(token);
                    _data.writeString(secret);
                    this.mRemote.transact(58, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void lockUserKeyIsec(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(59, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addUserKeyAuthIsec(int userId, int userSerial, String token, String secret) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(userSerial);
                    _data.writeString(token);
                    _data.writeString(secret);
                    this.mRemote.transact(60, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void fixateNewestUserKeyAuthIsec(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(61, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void lockUserScreenIsec(int userId, int userSerial) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(userSerial);
                    this.mRemote.transact(Stub.TRANSACTION_lockUserScreenIsec, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(Stub.TRANSACTION_unlockUserScreenIsec, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void lockScreenPrepare(int userId, int userSerial, int flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(userSerial);
                    _data.writeInt(flag);
                    this.mRemote.transact(64, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getKeyDesc(int user_id, int serial, int sdpClass) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(user_id);
                    _data.writeInt(serial);
                    _data.writeInt(sdpClass);
                    this.mRemote.transact(65, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPrepareFlag(int user_id, int serial) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(user_id);
                    _data.writeInt(serial);
                    this.mRemote.transact(66, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendBlockUserId(String volId, int blockUserId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    _data.writeInt(blockUserId);
                    this.mRemote.transact(67, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sdlockSetPassword(String password) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    this.mRemote.transact(68, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sdlockClearPassword(String password) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    this.mRemote.transact(69, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sdlockLock(String password) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    this.mRemote.transact(70, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sdlockUnlock(String password) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    this.mRemote.transact(71, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sdlockErase() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(72, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String sdlockPoll() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(73, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String sdlockGetCid() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(74, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String sdlockReadPassword() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(75, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sdlockWritePassword(String password) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    this.mRemote.transact(76, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sdbackupGetDevStat(int dev) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(dev);
                    this.mRemote.transact(77, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String sdbackupGetCid(int dev) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(dev);
                    this.mRemote.transact(78, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sdbackupCheckRW() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(79, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sdbackupFileReliabilityCheck(List<String> file_list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(file_list);
                    this.mRemote.transact(80, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cryptsdEnable(String volId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    this.mRemote.transact(81, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cryptsdDisable(String volId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    this.mRemote.transact(82, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cryptsdAddKeyAuth(int uid, int serial, String token, String secret) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(serial);
                    _data.writeString(token);
                    _data.writeString(secret);
                    this.mRemote.transact(83, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cryptsdUnlockKey(int uid, int serial, String token, String secret) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(serial);
                    _data.writeString(token);
                    _data.writeString(secret);
                    this.mRemote.transact(84, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cryptsdBackupInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(85, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                boolean _arg2 = false;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        setListener(IVoldListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        monitor();
                        reply.writeNoException();
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        reset();
                        reply.writeNoException();
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        shutdown();
                        reply.writeNoException();
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        onUserAdded(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        onUserRemoved(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        onUserStarted(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        onUserStopped(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        onSecureKeyguardStateChanged(_arg2);
                        reply.writeNoException();
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        partition(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        forgetPartition(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        check(data.readString());
                        reply.writeNoException();
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        mount(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        unmount(data.readString());
                        reply.writeNoException();
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result = supportCifs();
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result2 = mountCifs(data.readString(), data.readString());
                        reply.writeNoException();
                        parcel2.writeString(_result2);
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        unmountCifs(data.readString());
                        reply.writeNoException();
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        format(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        benchmark(data.readString(), IVoldTaskListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        checkEncryption(data.readString());
                        reply.writeNoException();
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        moveStorage(data.readString(), data.readString(), IVoldTaskListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        remountUid(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        mkdirs(data.readString());
                        reply.writeNoException();
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result3 = createObb(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result3);
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        destroyObb(data.readString());
                        reply.writeNoException();
                        return true;
                    case 26:
                        parcel.enforceInterface(DESCRIPTOR);
                        fstrim(data.readInt(), IVoldTaskListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_runIdleMaint /*27*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        runIdleMaint(IVoldTaskListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 28:
                        parcel.enforceInterface(DESCRIPTOR);
                        abortIdleMaint(IVoldTaskListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 29:
                        parcel.enforceInterface(DESCRIPTOR);
                        FileDescriptor _result4 = mountAppFuse(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeRawFileDescriptor(_result4);
                        return true;
                    case 30:
                        parcel.enforceInterface(DESCRIPTOR);
                        unmountAppFuse(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 31:
                        parcel.enforceInterface(DESCRIPTOR);
                        fdeCheckPassword(data.readString());
                        reply.writeNoException();
                        return true;
                    case 32:
                        parcel.enforceInterface(DESCRIPTOR);
                        fdeRestart();
                        reply.writeNoException();
                        return true;
                    case 33:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result5 = fdeComplete();
                        reply.writeNoException();
                        parcel2.writeInt(_result5);
                        return true;
                    case 34:
                        parcel.enforceInterface(DESCRIPTOR);
                        fdeEnable(data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 35:
                        parcel.enforceInterface(DESCRIPTOR);
                        fdeChangePassword(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 36:
                        parcel.enforceInterface(DESCRIPTOR);
                        fdeVerifyPassword(data.readString());
                        reply.writeNoException();
                        return true;
                    case 37:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result6 = fdeGetField(data.readString());
                        reply.writeNoException();
                        parcel2.writeString(_result6);
                        return true;
                    case 38:
                        parcel.enforceInterface(DESCRIPTOR);
                        fdeSetField(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 39:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result7 = fdeGetPasswordType();
                        reply.writeNoException();
                        parcel2.writeInt(_result7);
                        return true;
                    case 40:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result8 = fdeGetPassword();
                        reply.writeNoException();
                        parcel2.writeString(_result8);
                        return true;
                    case 41:
                        parcel.enforceInterface(DESCRIPTOR);
                        fdeClearPassword();
                        reply.writeNoException();
                        return true;
                    case 42:
                        parcel.enforceInterface(DESCRIPTOR);
                        fbeEnable();
                        reply.writeNoException();
                        return true;
                    case 43:
                        parcel.enforceInterface(DESCRIPTOR);
                        mountDefaultEncrypted();
                        reply.writeNoException();
                        return true;
                    case 44:
                        parcel.enforceInterface(DESCRIPTOR);
                        initUser0();
                        reply.writeNoException();
                        return true;
                    case 45:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result9 = isConvertibleToFbe();
                        reply.writeNoException();
                        parcel2.writeInt(_result9);
                        return true;
                    case 46:
                        parcel.enforceInterface(DESCRIPTOR);
                        mountFstab(data.readString());
                        reply.writeNoException();
                        return true;
                    case 47:
                        parcel.enforceInterface(DESCRIPTOR);
                        encryptFstab(data.readString());
                        reply.writeNoException();
                        return true;
                    case 48:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        int _arg1 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        createUserKey(_arg0, _arg1, _arg2);
                        reply.writeNoException();
                        return true;
                    case 49:
                        parcel.enforceInterface(DESCRIPTOR);
                        destroyUserKey(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 50:
                        parcel.enforceInterface(DESCRIPTOR);
                        addUserKeyAuth(data.readInt(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 51:
                        parcel.enforceInterface(DESCRIPTOR);
                        fixateNewestUserKeyAuth(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 52:
                        parcel.enforceInterface(DESCRIPTOR);
                        unlockUserKey(data.readInt(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 53:
                        parcel.enforceInterface(DESCRIPTOR);
                        lockUserKey(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 54:
                        parcel.enforceInterface(DESCRIPTOR);
                        prepareUserStorage(data.readString(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 55:
                        parcel.enforceInterface(DESCRIPTOR);
                        destroyUserStorage(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 56:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        int _arg12 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        createUserKeyIsec(_arg02, _arg12, _arg2);
                        reply.writeNoException();
                        return true;
                    case 57:
                        parcel.enforceInterface(DESCRIPTOR);
                        destroyUserKeyIsec(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 58:
                        parcel.enforceInterface(DESCRIPTOR);
                        unlockUserKeyIsec(data.readInt(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 59:
                        parcel.enforceInterface(DESCRIPTOR);
                        lockUserKeyIsec(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 60:
                        parcel.enforceInterface(DESCRIPTOR);
                        addUserKeyAuthIsec(data.readInt(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 61:
                        parcel.enforceInterface(DESCRIPTOR);
                        fixateNewestUserKeyAuthIsec(data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_lockUserScreenIsec /*62*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        lockUserScreenIsec(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_unlockUserScreenIsec /*63*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        unlockUserScreenIsec(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 64:
                        parcel.enforceInterface(DESCRIPTOR);
                        lockScreenPrepare(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 65:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result10 = getKeyDesc(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result10);
                        return true;
                    case 66:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result11 = getPrepareFlag(data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result11);
                        return true;
                    case 67:
                        parcel.enforceInterface(DESCRIPTOR);
                        sendBlockUserId(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 68:
                        parcel.enforceInterface(DESCRIPTOR);
                        sdlockSetPassword(data.readString());
                        reply.writeNoException();
                        return true;
                    case 69:
                        parcel.enforceInterface(DESCRIPTOR);
                        sdlockClearPassword(data.readString());
                        reply.writeNoException();
                        return true;
                    case 70:
                        parcel.enforceInterface(DESCRIPTOR);
                        sdlockLock(data.readString());
                        reply.writeNoException();
                        return true;
                    case 71:
                        parcel.enforceInterface(DESCRIPTOR);
                        sdlockUnlock(data.readString());
                        reply.writeNoException();
                        return true;
                    case 72:
                        parcel.enforceInterface(DESCRIPTOR);
                        sdlockErase();
                        reply.writeNoException();
                        return true;
                    case 73:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result12 = sdlockPoll();
                        reply.writeNoException();
                        parcel2.writeString(_result12);
                        return true;
                    case 74:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result13 = sdlockGetCid();
                        reply.writeNoException();
                        parcel2.writeString(_result13);
                        return true;
                    case 75:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result14 = sdlockReadPassword();
                        reply.writeNoException();
                        parcel2.writeString(_result14);
                        return true;
                    case 76:
                        parcel.enforceInterface(DESCRIPTOR);
                        sdlockWritePassword(data.readString());
                        reply.writeNoException();
                        return true;
                    case 77:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result15 = sdbackupGetDevStat(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result15);
                        return true;
                    case 78:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result16 = sdbackupGetCid(data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result16);
                        return true;
                    case 79:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result17 = sdbackupCheckRW();
                        reply.writeNoException();
                        parcel2.writeInt(_result17);
                        return true;
                    case 80:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result18 = sdbackupFileReliabilityCheck(data.createStringArrayList());
                        reply.writeNoException();
                        parcel2.writeInt(_result18);
                        return true;
                    case 81:
                        parcel.enforceInterface(DESCRIPTOR);
                        cryptsdEnable(data.readString());
                        reply.writeNoException();
                        return true;
                    case 82:
                        parcel.enforceInterface(DESCRIPTOR);
                        cryptsdDisable(data.readString());
                        reply.writeNoException();
                        return true;
                    case 83:
                        parcel.enforceInterface(DESCRIPTOR);
                        cryptsdAddKeyAuth(data.readInt(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 84:
                        parcel.enforceInterface(DESCRIPTOR);
                        cryptsdUnlockKey(data.readInt(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 85:
                        parcel.enforceInterface(DESCRIPTOR);
                        cryptsdBackupInfo();
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void abortIdleMaint(IVoldTaskListener iVoldTaskListener) throws RemoteException;

    void addUserKeyAuth(int i, int i2, String str, String str2) throws RemoteException;

    void addUserKeyAuthIsec(int i, int i2, String str, String str2) throws RemoteException;

    void benchmark(String str, IVoldTaskListener iVoldTaskListener) throws RemoteException;

    void check(String str) throws RemoteException;

    void checkEncryption(String str) throws RemoteException;

    String createObb(String str, String str2, int i) throws RemoteException;

    void createUserKey(int i, int i2, boolean z) throws RemoteException;

    void createUserKeyIsec(int i, int i2, boolean z) throws RemoteException;

    void cryptsdAddKeyAuth(int i, int i2, String str, String str2) throws RemoteException;

    void cryptsdBackupInfo() throws RemoteException;

    void cryptsdDisable(String str) throws RemoteException;

    void cryptsdEnable(String str) throws RemoteException;

    void cryptsdUnlockKey(int i, int i2, String str, String str2) throws RemoteException;

    void destroyObb(String str) throws RemoteException;

    void destroyUserKey(int i) throws RemoteException;

    void destroyUserKeyIsec(int i) throws RemoteException;

    void destroyUserStorage(String str, int i, int i2) throws RemoteException;

    void encryptFstab(String str) throws RemoteException;

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

    void mkdirs(String str) throws RemoteException;

    void monitor() throws RemoteException;

    void mount(String str, int i, int i2) throws RemoteException;

    FileDescriptor mountAppFuse(int i, int i2, int i3) throws RemoteException;

    String mountCifs(String str, String str2) throws RemoteException;

    void mountDefaultEncrypted() throws RemoteException;

    void mountFstab(String str) throws RemoteException;

    void moveStorage(String str, String str2, IVoldTaskListener iVoldTaskListener) throws RemoteException;

    void onSecureKeyguardStateChanged(boolean z) throws RemoteException;

    void onUserAdded(int i, int i2) throws RemoteException;

    void onUserRemoved(int i) throws RemoteException;

    void onUserStarted(int i) throws RemoteException;

    void onUserStopped(int i) throws RemoteException;

    void partition(String str, int i, int i2) throws RemoteException;

    void prepareUserStorage(String str, int i, int i2, int i3) throws RemoteException;

    void remountUid(int i, int i2) throws RemoteException;

    void reset() throws RemoteException;

    void runIdleMaint(IVoldTaskListener iVoldTaskListener) throws RemoteException;

    int sdbackupCheckRW() throws RemoteException;

    int sdbackupFileReliabilityCheck(List<String> list) throws RemoteException;

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

    void sendBlockUserId(String str, int i) throws RemoteException;

    void setListener(IVoldListener iVoldListener) throws RemoteException;

    void shutdown() throws RemoteException;

    int supportCifs() throws RemoteException;

    void unlockUserKey(int i, int i2, String str, String str2) throws RemoteException;

    void unlockUserKeyIsec(int i, int i2, String str, String str2) throws RemoteException;

    void unlockUserScreenIsec(int i, int i2, String str, String str2, int i3) throws RemoteException;

    void unmount(String str) throws RemoteException;

    void unmountAppFuse(int i, int i2, int i3) throws RemoteException;

    void unmountCifs(String str) throws RemoteException;
}
