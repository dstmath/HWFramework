package android.os.storage;

import android.content.pm.IPackageMoveObserver;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

public interface IMountService extends IInterface {
    public static final int ENCRYPTION_STATE_ERROR_CORRUPT = -4;
    public static final int ENCRYPTION_STATE_ERROR_INCOMPLETE = -2;
    public static final int ENCRYPTION_STATE_ERROR_INCONSISTENT = -3;
    public static final int ENCRYPTION_STATE_ERROR_UNKNOWN = -1;
    public static final int ENCRYPTION_STATE_NONE = 1;
    public static final int ENCRYPTION_STATE_OK = 0;

    public static abstract class Stub extends Binder implements IMountService {
        private static final String DESCRIPTOR = "IMountService";
        static final int TRANSACTION_addUserKeyAuth = 71;
        static final int TRANSACTION_benchmark = 60;
        static final int TRANSACTION_changeEncryptionPassword = 29;
        static final int TRANSACTION_clearPassword = 38;
        static final int TRANSACTION_createSecureContainer = 11;
        static final int TRANSACTION_createUserKey = 62;
        static final int TRANSACTION_decryptStorage = 27;
        static final int TRANSACTION_destroySecureContainer = 13;
        static final int TRANSACTION_destroyUserKey = 63;
        static final int TRANSACTION_destroyUserStorage = 68;
        static final int TRANSACTION_encryptStorage = 28;
        static final int TRANSACTION_finalizeSecureContainer = 12;
        static final int TRANSACTION_finishMediaUpdate = 21;
        static final int TRANSACTION_fixPermissionsSecureContainer = 34;
        static final int TRANSACTION_fixateNewestUserKeyAuth = 72;
        static final int TRANSACTION_forgetAllVolumes = 57;
        static final int TRANSACTION_forgetVolume = 56;
        static final int TRANSACTION_format = 50;
        static final int TRANSACTION_formatVolume = 8;
        static final int TRANSACTION_getDisks = 45;
        static final int TRANSACTION_getEncryptionState = 32;
        static final int TRANSACTION_getField = 40;
        static final int TRANSACTION_getMountedObbPath = 25;
        static final int TRANSACTION_getPassword = 37;
        static final int TRANSACTION_getPasswordType = 36;
        static final int TRANSACTION_getPrimaryStorageUuid = 58;
        static final int TRANSACTION_getSecureContainerFilesystemPath = 31;
        static final int TRANSACTION_getSecureContainerList = 19;
        static final int TRANSACTION_getSecureContainerPath = 18;
        static final int TRANSACTION_getStorageUsers = 9;
        static final int TRANSACTION_getVolumeList = 30;
        static final int TRANSACTION_getVolumeRecords = 47;
        static final int TRANSACTION_getVolumeState = 10;
        static final int TRANSACTION_getVolumes = 46;
        static final int TRANSACTION_isConvertibleToFBE = 69;
        static final int TRANSACTION_isExternalStorageEmulated = 26;
        static final int TRANSACTION_isObbMounted = 24;
        static final int TRANSACTION_isSecure = 1001;
        static final int TRANSACTION_isSecureContainerMounted = 16;
        static final int TRANSACTION_isUsbMassStorageConnected = 3;
        static final int TRANSACTION_isUsbMassStorageEnabled = 5;
        static final int TRANSACTION_isUserKeyUnlocked = 66;
        static final int TRANSACTION_lastMaintenance = 42;
        static final int TRANSACTION_lockUserKey = 65;
        static final int TRANSACTION_mkdirs = 35;
        static final int TRANSACTION_mount = 48;
        static final int TRANSACTION_mountAppFuse = 70;
        static final int TRANSACTION_mountObb = 22;
        static final int TRANSACTION_mountSecureContainer = 14;
        static final int TRANSACTION_mountVolume = 6;
        static final int TRANSACTION_partitionMixed = 53;
        static final int TRANSACTION_partitionPrivate = 52;
        static final int TRANSACTION_partitionPublic = 51;
        static final int TRANSACTION_prepareUserStorage = 67;
        static final int TRANSACTION_registerListener = 1;
        static final int TRANSACTION_renameSecureContainer = 17;
        static final int TRANSACTION_resizeSecureContainer = 41;
        static final int TRANSACTION_runMaintenance = 43;
        static final int TRANSACTION_setDebugFlags = 61;
        static final int TRANSACTION_setField = 39;
        static final int TRANSACTION_setPrimaryStorageUuid = 59;
        static final int TRANSACTION_setUsbMassStorageEnabled = 4;
        static final int TRANSACTION_setVolumeNickname = 54;
        static final int TRANSACTION_setVolumeUserFlags = 55;
        static final int TRANSACTION_shutdown = 20;
        static final int TRANSACTION_unlockUserKey = 64;
        static final int TRANSACTION_unmount = 49;
        static final int TRANSACTION_unmountObb = 23;
        static final int TRANSACTION_unmountSecureContainer = 15;
        static final int TRANSACTION_unmountVolume = 7;
        static final int TRANSACTION_unregisterListener = 2;
        static final int TRANSACTION_verifyEncryptionPassword = 33;
        static final int TRANSACTION_waitForAsecScan = 44;

        private static class Proxy implements IMountService {
            private final IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void registerListener(IMountServiceListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterListener(IMountServiceListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isUsbMassStorageConnected() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isUsbMassStorageConnected, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setUsbMassStorageEnabled(boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enable) {
                        i = Stub.TRANSACTION_registerListener;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setUsbMassStorageEnabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isUsbMassStorageEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isUsbMassStorageEnabled, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int mountVolume(String mountPoint) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mountPoint);
                    this.mRemote.transact(Stub.TRANSACTION_mountVolume, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unmountVolume(String mountPoint, boolean force, boolean removeEncryption) throws RemoteException {
                int i = Stub.TRANSACTION_registerListener;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mountPoint);
                    if (force) {
                        i2 = Stub.TRANSACTION_registerListener;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!removeEncryption) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_unmountVolume, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int formatVolume(String mountPoint) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mountPoint);
                    this.mRemote.transact(Stub.TRANSACTION_formatVolume, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getStorageUsers(String path) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    this.mRemote.transact(Stub.TRANSACTION_getStorageUsers, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getVolumeState(String mountPoint) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mountPoint);
                    this.mRemote.transact(Stub.TRANSACTION_getVolumeState, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int createSecureContainer(String id, int sizeMb, String fstype, String key, int ownerUid, boolean external) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(id);
                    _data.writeInt(sizeMb);
                    _data.writeString(fstype);
                    _data.writeString(key);
                    _data.writeInt(ownerUid);
                    if (external) {
                        i = Stub.TRANSACTION_registerListener;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_createSecureContainer, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int destroySecureContainer(String id, boolean force) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(id);
                    if (force) {
                        i = Stub.TRANSACTION_registerListener;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_destroySecureContainer, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int finalizeSecureContainer(String id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(id);
                    this.mRemote.transact(Stub.TRANSACTION_finalizeSecureContainer, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int mountSecureContainer(String id, String key, int ownerUid, boolean readOnly) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(id);
                    _data.writeString(key);
                    _data.writeInt(ownerUid);
                    if (readOnly) {
                        i = Stub.TRANSACTION_registerListener;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_mountSecureContainer, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int unmountSecureContainer(String id, boolean force) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(id);
                    if (force) {
                        i = Stub.TRANSACTION_registerListener;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_unmountSecureContainer, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isSecureContainerMounted(String id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(id);
                    this.mRemote.transact(Stub.TRANSACTION_isSecureContainerMounted, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int renameSecureContainer(String oldId, String newId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(oldId);
                    _data.writeString(newId);
                    this.mRemote.transact(Stub.TRANSACTION_renameSecureContainer, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getSecureContainerPath(String id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(id);
                    this.mRemote.transact(Stub.TRANSACTION_getSecureContainerPath, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getSecureContainerList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getSecureContainerList, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void shutdown(IMountShutdownObserver observer) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_shutdown, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void finishMediaUpdate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_finishMediaUpdate, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void mountObb(String rawPath, String canonicalPath, String key, IObbActionListener token, int nonce) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(rawPath);
                    _data.writeString(canonicalPath);
                    _data.writeString(key);
                    if (token != null) {
                        iBinder = token.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(nonce);
                    this.mRemote.transact(Stub.TRANSACTION_mountObb, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unmountObb(String rawPath, boolean force, IObbActionListener token, int nonce) throws RemoteException {
                IBinder iBinder = null;
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(rawPath);
                    if (force) {
                        i = Stub.TRANSACTION_registerListener;
                    }
                    _data.writeInt(i);
                    if (token != null) {
                        iBinder = token.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(nonce);
                    this.mRemote.transact(Stub.TRANSACTION_unmountObb, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isObbMounted(String rawPath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(rawPath);
                    this.mRemote.transact(Stub.TRANSACTION_isObbMounted, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getMountedObbPath(String rawPath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(rawPath);
                    this.mRemote.transact(Stub.TRANSACTION_getMountedObbPath, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isExternalStorageEmulated() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isExternalStorageEmulated, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getEncryptionState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getEncryptionState, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int decryptStorage(String password) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    this.mRemote.transact(Stub.TRANSACTION_decryptStorage, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int encryptStorage(int type, String password) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeString(password);
                    this.mRemote.transact(Stub.TRANSACTION_encryptStorage, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int changeEncryptionPassword(int type, String password) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeString(password);
                    this.mRemote.transact(Stub.TRANSACTION_changeEncryptionPassword, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int verifyEncryptionPassword(String password) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    this.mRemote.transact(Stub.TRANSACTION_verifyEncryptionPassword, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPasswordType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getPasswordType, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getPassword() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getPassword, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearPassword() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_clearPassword, _data, _reply, Stub.TRANSACTION_registerListener);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setField(String field, String data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(field);
                    _data.writeString(data);
                    this.mRemote.transact(Stub.TRANSACTION_setField, _data, _reply, Stub.TRANSACTION_registerListener);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getField(String field) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(field);
                    this.mRemote.transact(Stub.TRANSACTION_getField, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isConvertibleToFBE() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isConvertibleToFBE, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public StorageVolume[] getVolumeList(int uid, String packageName, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_getVolumeList, _data, _reply, 0);
                    _reply.readException();
                    StorageVolume[] _result = (StorageVolume[]) _reply.createTypedArray(StorageVolume.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getSecureContainerFilesystemPath(String id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(id);
                    this.mRemote.transact(Stub.TRANSACTION_getSecureContainerFilesystemPath, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int fixPermissionsSecureContainer(String id, int gid, String filename) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(id);
                    _data.writeInt(gid);
                    _data.writeString(filename);
                    this.mRemote.transact(Stub.TRANSACTION_fixPermissionsSecureContainer, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int mkdirs(String callingPkg, String path) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeString(path);
                    this.mRemote.transact(Stub.TRANSACTION_mkdirs, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int resizeSecureContainer(String id, int sizeMb, String key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(id);
                    _data.writeInt(sizeMb);
                    _data.writeString(key);
                    this.mRemote.transact(Stub.TRANSACTION_resizeSecureContainer, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long lastMaintenance() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_lastMaintenance, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void runMaintenance() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_runMaintenance, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void waitForAsecScan() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_waitForAsecScan, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public DiskInfo[] getDisks() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getDisks, _data, _reply, 0);
                    _reply.readException();
                    DiskInfo[] _result = (DiskInfo[]) _reply.createTypedArray(DiskInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public VolumeInfo[] getVolumes(int _flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(_flags);
                    this.mRemote.transact(Stub.TRANSACTION_getVolumes, _data, _reply, 0);
                    _reply.readException();
                    VolumeInfo[] _result = (VolumeInfo[]) _reply.createTypedArray(VolumeInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public VolumeRecord[] getVolumeRecords(int _flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(_flags);
                    this.mRemote.transact(Stub.TRANSACTION_getVolumeRecords, _data, _reply, 0);
                    _reply.readException();
                    VolumeRecord[] _result = (VolumeRecord[]) _reply.createTypedArray(VolumeRecord.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void mount(String volId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    this.mRemote.transact(Stub.TRANSACTION_mount, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_unmount, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void format(String volId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    this.mRemote.transact(Stub.TRANSACTION_format, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long benchmark(String volId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    this.mRemote.transact(Stub.TRANSACTION_benchmark, _data, _reply, 0);
                    _reply.readException();
                    long readLong = _reply.readLong();
                    return readLong;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void partitionPublic(String diskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(diskId);
                    this.mRemote.transact(Stub.TRANSACTION_partitionPublic, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void partitionPrivate(String diskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(diskId);
                    this.mRemote.transact(Stub.TRANSACTION_partitionPrivate, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void partitionMixed(String diskId, int ratio) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(diskId);
                    _data.writeInt(ratio);
                    this.mRemote.transact(Stub.TRANSACTION_partitionMixed, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setVolumeNickname(String fsUuid, String nickname) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fsUuid);
                    _data.writeString(nickname);
                    this.mRemote.transact(Stub.TRANSACTION_setVolumeNickname, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setVolumeUserFlags(String fsUuid, int flags, int mask) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fsUuid);
                    _data.writeInt(flags);
                    _data.writeInt(mask);
                    this.mRemote.transact(Stub.TRANSACTION_setVolumeUserFlags, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void forgetVolume(String fsUuid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fsUuid);
                    this.mRemote.transact(Stub.TRANSACTION_forgetVolume, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void forgetAllVolumes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_forgetAllVolumes, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDebugFlags(int _flags, int _mask) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(_flags);
                    _data.writeInt(_mask);
                    this.mRemote.transact(Stub.TRANSACTION_setDebugFlags, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getPrimaryStorageUuid() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getPrimaryStorageUuid, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPrimaryStorageUuid(String volumeUuid, IPackageMoveObserver callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_setPrimaryStorageUuid, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void createUserKey(int userId, int serialNumber, boolean ephemeral) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(serialNumber);
                    if (ephemeral) {
                        i = Stub.TRANSACTION_registerListener;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_createUserKey, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_destroyUserKey, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addUserKeyAuth(int userId, int serialNumber, byte[] token, byte[] secret) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(serialNumber);
                    _data.writeByteArray(token);
                    _data.writeByteArray(secret);
                    this.mRemote.transact(Stub.TRANSACTION_addUserKeyAuth, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_fixateNewestUserKeyAuth, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unlockUserKey(int userId, int serialNumber, byte[] token, byte[] secret) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(serialNumber);
                    _data.writeByteArray(token);
                    _data.writeByteArray(secret);
                    this.mRemote.transact(Stub.TRANSACTION_unlockUserKey, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_lockUserKey, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isUserKeyUnlocked(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_isUserKeyUnlocked, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void prepareUserStorage(String volumeUuid, int userId, int serialNumber, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    _data.writeInt(userId);
                    _data.writeInt(serialNumber);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_prepareUserStorage, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void destroyUserStorage(String volumeUuid, int userId, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    _data.writeInt(userId);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_destroyUserStorage, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParcelFileDescriptor mountAppFuse(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                ParcelFileDescriptor parcelFileDescriptor = null;
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    this.mRemote.transact(Stub.TRANSACTION_mountAppFuse, _data, _reply, 0);
                    _reply.readException();
                    parcelFileDescriptor = (ParcelFileDescriptor) _reply.readParcelable(ClassLoader.getSystemClassLoader());
                    return parcelFileDescriptor;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isSecure() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isSecure, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static IMountService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMountService)) {
                return new Proxy(obj);
            }
            return (IMountService) iin;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean result;
            int resultCode;
            int result2;
            boolean status;
            String path;
            switch (code) {
                case TRANSACTION_registerListener /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    registerListener(android.os.storage.IMountServiceListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unregisterListener /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterListener(android.os.storage.IMountServiceListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isUsbMassStorageConnected /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    result = isUsbMassStorageConnected();
                    reply.writeNoException();
                    reply.writeInt(result ? TRANSACTION_registerListener : 0);
                    return true;
                case TRANSACTION_setUsbMassStorageEnabled /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    setUsbMassStorageEnabled(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isUsbMassStorageEnabled /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    result = isUsbMassStorageEnabled();
                    reply.writeNoException();
                    reply.writeInt(result ? TRANSACTION_registerListener : 0);
                    return true;
                case TRANSACTION_mountVolume /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    resultCode = mountVolume(data.readString());
                    reply.writeNoException();
                    reply.writeInt(resultCode);
                    return true;
                case TRANSACTION_unmountVolume /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    unmountVolume(data.readString(), data.readInt() != 0, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_formatVolume /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    result2 = formatVolume(data.readString());
                    reply.writeNoException();
                    reply.writeInt(result2);
                    return true;
                case TRANSACTION_getStorageUsers /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    int[] pids = getStorageUsers(data.readString());
                    reply.writeNoException();
                    reply.writeIntArray(pids);
                    return true;
                case TRANSACTION_getVolumeState /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    String state = getVolumeState(data.readString());
                    reply.writeNoException();
                    reply.writeString(state);
                    return true;
                case TRANSACTION_createSecureContainer /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    resultCode = createSecureContainer(data.readString(), data.readInt(), data.readString(), data.readString(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(resultCode);
                    return true;
                case TRANSACTION_finalizeSecureContainer /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    resultCode = finalizeSecureContainer(data.readString());
                    reply.writeNoException();
                    reply.writeInt(resultCode);
                    return true;
                case TRANSACTION_destroySecureContainer /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    resultCode = destroySecureContainer(data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(resultCode);
                    return true;
                case TRANSACTION_mountSecureContainer /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    resultCode = mountSecureContainer(data.readString(), data.readString(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(resultCode);
                    return true;
                case TRANSACTION_unmountSecureContainer /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    resultCode = unmountSecureContainer(data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(resultCode);
                    return true;
                case TRANSACTION_isSecureContainerMounted /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    status = isSecureContainerMounted(data.readString());
                    reply.writeNoException();
                    reply.writeInt(status ? TRANSACTION_registerListener : 0);
                    return true;
                case TRANSACTION_renameSecureContainer /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    resultCode = renameSecureContainer(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(resultCode);
                    return true;
                case TRANSACTION_getSecureContainerPath /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    path = getSecureContainerPath(data.readString());
                    reply.writeNoException();
                    reply.writeString(path);
                    return true;
                case TRANSACTION_getSecureContainerList /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    String[] ids = getSecureContainerList();
                    reply.writeNoException();
                    reply.writeStringArray(ids);
                    return true;
                case TRANSACTION_shutdown /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    shutdown(android.os.storage.IMountShutdownObserver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_finishMediaUpdate /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    finishMediaUpdate();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_mountObb /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    mountObb(data.readString(), data.readString(), data.readString(), android.os.storage.IObbActionListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unmountObb /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    unmountObb(data.readString(), data.readInt() != 0, android.os.storage.IObbActionListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isObbMounted /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    status = isObbMounted(data.readString());
                    reply.writeNoException();
                    reply.writeInt(status ? TRANSACTION_registerListener : 0);
                    return true;
                case TRANSACTION_getMountedObbPath /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    String mountedPath = getMountedObbPath(data.readString());
                    reply.writeNoException();
                    reply.writeString(mountedPath);
                    return true;
                case TRANSACTION_isExternalStorageEmulated /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean emulated = isExternalStorageEmulated();
                    reply.writeNoException();
                    reply.writeInt(emulated ? TRANSACTION_registerListener : 0);
                    return true;
                case TRANSACTION_decryptStorage /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    result2 = decryptStorage(data.readString());
                    reply.writeNoException();
                    reply.writeInt(result2);
                    return true;
                case TRANSACTION_encryptStorage /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    result2 = encryptStorage(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(result2);
                    return true;
                case TRANSACTION_changeEncryptionPassword /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    result2 = changeEncryptionPassword(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(result2);
                    return true;
                case TRANSACTION_getVolumeList /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    StorageVolume[] result3 = getVolumeList(data.readInt(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedArray(result3, TRANSACTION_registerListener);
                    return true;
                case TRANSACTION_getSecureContainerFilesystemPath /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    path = getSecureContainerFilesystemPath(data.readString());
                    reply.writeNoException();
                    reply.writeString(path);
                    return true;
                case TRANSACTION_getEncryptionState /*32*/:
                    data.enforceInterface(DESCRIPTOR);
                    result2 = getEncryptionState();
                    reply.writeNoException();
                    reply.writeInt(result2);
                    return true;
                case TRANSACTION_fixPermissionsSecureContainer /*34*/:
                    data.enforceInterface(DESCRIPTOR);
                    resultCode = fixPermissionsSecureContainer(data.readString(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(resultCode);
                    return true;
                case TRANSACTION_mkdirs /*35*/:
                    data.enforceInterface(DESCRIPTOR);
                    result2 = mkdirs(data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(result2);
                    return true;
                case TRANSACTION_getPasswordType /*36*/:
                    data.enforceInterface(DESCRIPTOR);
                    result2 = getPasswordType();
                    reply.writeNoException();
                    reply.writeInt(result2);
                    return true;
                case TRANSACTION_getPassword /*37*/:
                    data.enforceInterface(DESCRIPTOR);
                    String result4 = getPassword();
                    reply.writeNoException();
                    reply.writeString(result4);
                    return true;
                case TRANSACTION_clearPassword /*38*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearPassword();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setField /*39*/:
                    data.enforceInterface(DESCRIPTOR);
                    setField(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getField /*40*/:
                    data.enforceInterface(DESCRIPTOR);
                    String contents = getField(data.readString());
                    reply.writeNoException();
                    reply.writeString(contents);
                    return true;
                case TRANSACTION_resizeSecureContainer /*41*/:
                    data.enforceInterface(DESCRIPTOR);
                    resultCode = resizeSecureContainer(data.readString(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(resultCode);
                    return true;
                case TRANSACTION_lastMaintenance /*42*/:
                    data.enforceInterface(DESCRIPTOR);
                    long lastMaintenance = lastMaintenance();
                    reply.writeNoException();
                    reply.writeLong(lastMaintenance);
                    return true;
                case TRANSACTION_runMaintenance /*43*/:
                    data.enforceInterface(DESCRIPTOR);
                    runMaintenance();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_waitForAsecScan /*44*/:
                    data.enforceInterface(DESCRIPTOR);
                    waitForAsecScan();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getDisks /*45*/:
                    data.enforceInterface(DESCRIPTOR);
                    DiskInfo[] disks = getDisks();
                    reply.writeNoException();
                    reply.writeTypedArray(disks, TRANSACTION_registerListener);
                    return true;
                case TRANSACTION_getVolumes /*46*/:
                    data.enforceInterface(DESCRIPTOR);
                    VolumeInfo[] volumes = getVolumes(data.readInt());
                    reply.writeNoException();
                    reply.writeTypedArray(volumes, TRANSACTION_registerListener);
                    return true;
                case TRANSACTION_getVolumeRecords /*47*/:
                    data.enforceInterface(DESCRIPTOR);
                    VolumeRecord[] volumes2 = getVolumeRecords(data.readInt());
                    reply.writeNoException();
                    reply.writeTypedArray(volumes2, TRANSACTION_registerListener);
                    return true;
                case TRANSACTION_mount /*48*/:
                    data.enforceInterface(DESCRIPTOR);
                    mount(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unmount /*49*/:
                    data.enforceInterface(DESCRIPTOR);
                    unmount(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_format /*50*/:
                    data.enforceInterface(DESCRIPTOR);
                    format(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_partitionPublic /*51*/:
                    data.enforceInterface(DESCRIPTOR);
                    partitionPublic(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_partitionPrivate /*52*/:
                    data.enforceInterface(DESCRIPTOR);
                    partitionPrivate(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_partitionMixed /*53*/:
                    data.enforceInterface(DESCRIPTOR);
                    partitionMixed(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setVolumeNickname /*54*/:
                    data.enforceInterface(DESCRIPTOR);
                    setVolumeNickname(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setVolumeUserFlags /*55*/:
                    data.enforceInterface(DESCRIPTOR);
                    setVolumeUserFlags(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_forgetVolume /*56*/:
                    data.enforceInterface(DESCRIPTOR);
                    forgetVolume(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_forgetAllVolumes /*57*/:
                    data.enforceInterface(DESCRIPTOR);
                    forgetAllVolumes();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getPrimaryStorageUuid /*58*/:
                    data.enforceInterface(DESCRIPTOR);
                    String volumeUuid = getPrimaryStorageUuid();
                    reply.writeNoException();
                    reply.writeString(volumeUuid);
                    return true;
                case TRANSACTION_setPrimaryStorageUuid /*59*/:
                    data.enforceInterface(DESCRIPTOR);
                    setPrimaryStorageUuid(data.readString(), android.content.pm.IPackageMoveObserver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_benchmark /*60*/:
                    data.enforceInterface(DESCRIPTOR);
                    long res = benchmark(data.readString());
                    reply.writeNoException();
                    reply.writeLong(res);
                    return true;
                case TRANSACTION_setDebugFlags /*61*/:
                    data.enforceInterface(DESCRIPTOR);
                    setDebugFlags(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_createUserKey /*62*/:
                    data.enforceInterface(DESCRIPTOR);
                    createUserKey(data.readInt(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_destroyUserKey /*63*/:
                    data.enforceInterface(DESCRIPTOR);
                    destroyUserKey(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unlockUserKey /*64*/:
                    data.enforceInterface(DESCRIPTOR);
                    unlockUserKey(data.readInt(), data.readInt(), data.createByteArray(), data.createByteArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_lockUserKey /*65*/:
                    data.enforceInterface(DESCRIPTOR);
                    lockUserKey(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isUserKeyUnlocked /*66*/:
                    data.enforceInterface(DESCRIPTOR);
                    result = isUserKeyUnlocked(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(result ? TRANSACTION_registerListener : 0);
                    return true;
                case TRANSACTION_prepareUserStorage /*67*/:
                    data.enforceInterface(DESCRIPTOR);
                    prepareUserStorage(data.readString(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_destroyUserStorage /*68*/:
                    data.enforceInterface(DESCRIPTOR);
                    destroyUserStorage(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isConvertibleToFBE /*69*/:
                    data.enforceInterface(DESCRIPTOR);
                    resultCode = isConvertibleToFBE() ? TRANSACTION_registerListener : 0;
                    reply.writeNoException();
                    reply.writeInt(resultCode);
                    return true;
                case TRANSACTION_mountAppFuse /*70*/:
                    data.enforceInterface(DESCRIPTOR);
                    ParcelFileDescriptor fd = mountAppFuse(data.readString());
                    reply.writeNoException();
                    reply.writeParcelable(fd, TRANSACTION_registerListener);
                    return true;
                case TRANSACTION_addUserKeyAuth /*71*/:
                    data.enforceInterface(DESCRIPTOR);
                    addUserKeyAuth(data.readInt(), data.readInt(), data.createByteArray(), data.createByteArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_fixateNewestUserKeyAuth /*72*/:
                    data.enforceInterface(DESCRIPTOR);
                    fixateNewestUserKeyAuth(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isSecure /*1001*/:
                    data.enforceInterface(DESCRIPTOR);
                    status = isSecure();
                    reply.writeNoException();
                    reply.writeInt(status ? TRANSACTION_registerListener : 0);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addUserKeyAuth(int i, int i2, byte[] bArr, byte[] bArr2) throws RemoteException;

    long benchmark(String str) throws RemoteException;

    int changeEncryptionPassword(int i, String str) throws RemoteException;

    void clearPassword() throws RemoteException;

    int createSecureContainer(String str, int i, String str2, String str3, int i2, boolean z) throws RemoteException;

    void createUserKey(int i, int i2, boolean z) throws RemoteException;

    int decryptStorage(String str) throws RemoteException;

    int destroySecureContainer(String str, boolean z) throws RemoteException;

    void destroyUserKey(int i) throws RemoteException;

    void destroyUserStorage(String str, int i, int i2) throws RemoteException;

    int encryptStorage(int i, String str) throws RemoteException;

    int finalizeSecureContainer(String str) throws RemoteException;

    void finishMediaUpdate() throws RemoteException;

    int fixPermissionsSecureContainer(String str, int i, String str2) throws RemoteException;

    void fixateNewestUserKeyAuth(int i) throws RemoteException;

    void forgetAllVolumes() throws RemoteException;

    void forgetVolume(String str) throws RemoteException;

    void format(String str) throws RemoteException;

    int formatVolume(String str) throws RemoteException;

    DiskInfo[] getDisks() throws RemoteException;

    int getEncryptionState() throws RemoteException;

    String getField(String str) throws RemoteException;

    String getMountedObbPath(String str) throws RemoteException;

    String getPassword() throws RemoteException;

    int getPasswordType() throws RemoteException;

    String getPrimaryStorageUuid() throws RemoteException;

    String getSecureContainerFilesystemPath(String str) throws RemoteException;

    String[] getSecureContainerList() throws RemoteException;

    String getSecureContainerPath(String str) throws RemoteException;

    int[] getStorageUsers(String str) throws RemoteException;

    StorageVolume[] getVolumeList(int i, String str, int i2) throws RemoteException;

    VolumeRecord[] getVolumeRecords(int i) throws RemoteException;

    String getVolumeState(String str) throws RemoteException;

    VolumeInfo[] getVolumes(int i) throws RemoteException;

    boolean isConvertibleToFBE() throws RemoteException;

    boolean isExternalStorageEmulated() throws RemoteException;

    boolean isObbMounted(String str) throws RemoteException;

    boolean isSecure() throws RemoteException;

    boolean isSecureContainerMounted(String str) throws RemoteException;

    boolean isUsbMassStorageConnected() throws RemoteException;

    boolean isUsbMassStorageEnabled() throws RemoteException;

    boolean isUserKeyUnlocked(int i) throws RemoteException;

    long lastMaintenance() throws RemoteException;

    void lockUserKey(int i) throws RemoteException;

    int mkdirs(String str, String str2) throws RemoteException;

    void mount(String str) throws RemoteException;

    ParcelFileDescriptor mountAppFuse(String str) throws RemoteException;

    void mountObb(String str, String str2, String str3, IObbActionListener iObbActionListener, int i) throws RemoteException;

    int mountSecureContainer(String str, String str2, int i, boolean z) throws RemoteException;

    int mountVolume(String str) throws RemoteException;

    void partitionMixed(String str, int i) throws RemoteException;

    void partitionPrivate(String str) throws RemoteException;

    void partitionPublic(String str) throws RemoteException;

    void prepareUserStorage(String str, int i, int i2, int i3) throws RemoteException;

    void registerListener(IMountServiceListener iMountServiceListener) throws RemoteException;

    int renameSecureContainer(String str, String str2) throws RemoteException;

    int resizeSecureContainer(String str, int i, String str2) throws RemoteException;

    void runMaintenance() throws RemoteException;

    void setDebugFlags(int i, int i2) throws RemoteException;

    void setField(String str, String str2) throws RemoteException;

    void setPrimaryStorageUuid(String str, IPackageMoveObserver iPackageMoveObserver) throws RemoteException;

    void setUsbMassStorageEnabled(boolean z) throws RemoteException;

    void setVolumeNickname(String str, String str2) throws RemoteException;

    void setVolumeUserFlags(String str, int i, int i2) throws RemoteException;

    void shutdown(IMountShutdownObserver iMountShutdownObserver) throws RemoteException;

    void unlockUserKey(int i, int i2, byte[] bArr, byte[] bArr2) throws RemoteException;

    void unmount(String str) throws RemoteException;

    void unmountObb(String str, boolean z, IObbActionListener iObbActionListener, int i) throws RemoteException;

    int unmountSecureContainer(String str, boolean z) throws RemoteException;

    void unmountVolume(String str, boolean z, boolean z2) throws RemoteException;

    void unregisterListener(IMountServiceListener iMountServiceListener) throws RemoteException;

    int verifyEncryptionPassword(String str) throws RemoteException;

    void waitForAsecScan() throws RemoteException;
}
