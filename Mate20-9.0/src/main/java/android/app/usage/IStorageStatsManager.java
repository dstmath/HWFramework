package android.app.usage;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IStorageStatsManager extends IInterface {

    public static abstract class Stub extends Binder implements IStorageStatsManager {
        private static final String DESCRIPTOR = "android.app.usage.IStorageStatsManager";
        static final int TRANSACTION_getCacheBytes = 5;
        static final int TRANSACTION_getCacheQuotaBytes = 6;
        static final int TRANSACTION_getFreeBytes = 4;
        static final int TRANSACTION_getTotalBytes = 3;
        static final int TRANSACTION_isQuotaSupported = 1;
        static final int TRANSACTION_isReservedSupported = 2;
        static final int TRANSACTION_queryExternalStatsForUser = 10;
        static final int TRANSACTION_queryStatsForPackage = 7;
        static final int TRANSACTION_queryStatsForUid = 8;
        static final int TRANSACTION_queryStatsForUser = 9;

        private static class Proxy implements IStorageStatsManager {
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

            public boolean isQuotaSupported(String volumeUuid, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    this.mRemote.transact(1, _data, _reply, 0);
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

            public boolean isReservedSupported(String volumeUuid, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    this.mRemote.transact(2, _data, _reply, 0);
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

            public long getTotalBytes(String volumeUuid, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getFreeBytes(String volumeUuid, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getCacheBytes(String volumeUuid, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getCacheQuotaBytes(String volumeUuid, int uid, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    _data.writeInt(uid);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public StorageStats queryStatsForPackage(String volumeUuid, String packageName, int userId, String callingPackage) throws RemoteException {
                StorageStats _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = StorageStats.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public StorageStats queryStatsForUid(String volumeUuid, int uid, String callingPackage) throws RemoteException {
                StorageStats _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    _data.writeInt(uid);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = StorageStats.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public StorageStats queryStatsForUser(String volumeUuid, int userId, String callingPackage) throws RemoteException {
                StorageStats _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    _data.writeInt(userId);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = StorageStats.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ExternalStorageStats queryExternalStatsForUser(String volumeUuid, int userId, String callingPackage) throws RemoteException {
                ExternalStorageStats _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    _data.writeInt(userId);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ExternalStorageStats.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IStorageStatsManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IStorageStatsManager)) {
                return new Proxy(obj);
            }
            return (IStorageStatsManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result = isQuotaSupported(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result2 = isReservedSupported(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        long _result3 = getTotalBytes(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeLong(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        long _result4 = getFreeBytes(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeLong(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        long _result5 = getCacheBytes(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeLong(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        long _result6 = getCacheQuotaBytes(data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeLong(_result6);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        StorageStats _result7 = queryStatsForPackage(data.readString(), data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result7 != null) {
                            reply.writeInt(1);
                            _result7.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        StorageStats _result8 = queryStatsForUid(data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result8 != null) {
                            reply.writeInt(1);
                            _result8.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        StorageStats _result9 = queryStatsForUser(data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result9 != null) {
                            reply.writeInt(1);
                            _result9.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        ExternalStorageStats _result10 = queryExternalStatsForUser(data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result10 != null) {
                            reply.writeInt(1);
                            _result10.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    long getCacheBytes(String str, String str2) throws RemoteException;

    long getCacheQuotaBytes(String str, int i, String str2) throws RemoteException;

    long getFreeBytes(String str, String str2) throws RemoteException;

    long getTotalBytes(String str, String str2) throws RemoteException;

    boolean isQuotaSupported(String str, String str2) throws RemoteException;

    boolean isReservedSupported(String str, String str2) throws RemoteException;

    ExternalStorageStats queryExternalStatsForUser(String str, int i, String str2) throws RemoteException;

    StorageStats queryStatsForPackage(String str, String str2, int i, String str3) throws RemoteException;

    StorageStats queryStatsForUid(String str, int i, String str2) throws RemoteException;

    StorageStats queryStatsForUser(String str, int i, String str2) throws RemoteException;
}
