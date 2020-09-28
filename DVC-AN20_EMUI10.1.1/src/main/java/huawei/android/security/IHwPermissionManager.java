package huawei.android.security;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import huawei.android.security.IOnHwPermissionChangeListener;

public interface IHwPermissionManager extends IInterface {
    void addOnPermissionsChangeListener(IOnHwPermissionChangeListener iOnHwPermissionChangeListener) throws RemoteException;

    void authenticateSmsSend(IBinder iBinder, int i, int i2, String str, String str2) throws RemoteException;

    int checkHwPermission(int i, int i2, int i3) throws RemoteException;

    Bundle getHwPermissionInfo(String str, int i, long j, Bundle bundle) throws RemoteException;

    String getSmsAuthPackageName() throws RemoteException;

    int holdServiceByRequestPermission(int i, int i2, long j) throws RemoteException;

    void removeHwPermissionInfo(String str, int i) throws RemoteException;

    void removeOnPermissionsChangeListener(IOnHwPermissionChangeListener iOnHwPermissionChangeListener) throws RemoteException;

    void setHwPermissionInfo(int i, Bundle bundle) throws RemoteException;

    void setUserAuthResult(int i, int i2, long j) throws RemoteException;

    boolean shouldMonitor(int i) throws RemoteException;

    public static class Default implements IHwPermissionManager {
        @Override // huawei.android.security.IHwPermissionManager
        public Bundle getHwPermissionInfo(String pkgName, int userId, long permType, Bundle params) throws RemoteException {
            return null;
        }

        @Override // huawei.android.security.IHwPermissionManager
        public void setHwPermissionInfo(int userId, Bundle params) throws RemoteException {
        }

        @Override // huawei.android.security.IHwPermissionManager
        public void removeHwPermissionInfo(String pkgName, int userId) throws RemoteException {
        }

        @Override // huawei.android.security.IHwPermissionManager
        public void addOnPermissionsChangeListener(IOnHwPermissionChangeListener listener) throws RemoteException {
        }

        @Override // huawei.android.security.IHwPermissionManager
        public void removeOnPermissionsChangeListener(IOnHwPermissionChangeListener listener) throws RemoteException {
        }

        @Override // huawei.android.security.IHwPermissionManager
        public boolean shouldMonitor(int uid) throws RemoteException {
            return false;
        }

        @Override // huawei.android.security.IHwPermissionManager
        public int checkHwPermission(int uid, int pid, int permissionType) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.IHwPermissionManager
        public void authenticateSmsSend(IBinder notifyResult, int uidOf3RdApk, int smsId, String smsBody, String smsAddress) throws RemoteException {
        }

        @Override // huawei.android.security.IHwPermissionManager
        public int holdServiceByRequestPermission(int uid, int pid, long permissionType) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.IHwPermissionManager
        public String getSmsAuthPackageName() throws RemoteException {
            return null;
        }

        @Override // huawei.android.security.IHwPermissionManager
        public void setUserAuthResult(int uid, int userSelection, long permissionType) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwPermissionManager {
        private static final String DESCRIPTOR = "huawei.android.security.IHwPermissionManager";
        static final int TRANSACTION_addOnPermissionsChangeListener = 4;
        static final int TRANSACTION_authenticateSmsSend = 8;
        static final int TRANSACTION_checkHwPermission = 7;
        static final int TRANSACTION_getHwPermissionInfo = 1;
        static final int TRANSACTION_getSmsAuthPackageName = 10;
        static final int TRANSACTION_holdServiceByRequestPermission = 9;
        static final int TRANSACTION_removeHwPermissionInfo = 3;
        static final int TRANSACTION_removeOnPermissionsChangeListener = 5;
        static final int TRANSACTION_setHwPermissionInfo = 2;
        static final int TRANSACTION_setUserAuthResult = 11;
        static final int TRANSACTION_shouldMonitor = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwPermissionManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwPermissionManager)) {
                return new Proxy(obj);
            }
            return (IHwPermissionManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg3;
            Bundle _arg1;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        int _arg12 = data.readInt();
                        long _arg2 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        Bundle _result = getHwPermissionInfo(_arg0, _arg12, _arg2, _arg3);
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        setHwPermissionInfo(_arg02, _arg1);
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        removeHwPermissionInfo(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        addOnPermissionsChangeListener(IOnHwPermissionChangeListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        removeOnPermissionsChangeListener(IOnHwPermissionChangeListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean shouldMonitor = shouldMonitor(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(shouldMonitor ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = checkHwPermission(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        authenticateSmsSend(data.readStrongBinder(), data.readInt(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = holdServiceByRequestPermission(data.readInt(), data.readInt(), data.readLong());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        String _result4 = getSmsAuthPackageName();
                        reply.writeNoException();
                        reply.writeString(_result4);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        setUserAuthResult(data.readInt(), data.readInt(), data.readLong());
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
        public static class Proxy implements IHwPermissionManager {
            public static IHwPermissionManager sDefaultImpl;
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

            @Override // huawei.android.security.IHwPermissionManager
            public Bundle getHwPermissionInfo(String pkgName, int userId, long permType, Bundle params) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(userId);
                    _data.writeLong(permType);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHwPermissionInfo(pkgName, userId, permType, params);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IHwPermissionManager
            public void setHwPermissionInfo(int userId, Bundle params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setHwPermissionInfo(userId, params);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IHwPermissionManager
            public void removeHwPermissionInfo(String pkgName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeHwPermissionInfo(pkgName, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IHwPermissionManager
            public void addOnPermissionsChangeListener(IOnHwPermissionChangeListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addOnPermissionsChangeListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IHwPermissionManager
            public void removeOnPermissionsChangeListener(IOnHwPermissionChangeListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeOnPermissionsChangeListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IHwPermissionManager
            public boolean shouldMonitor(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().shouldMonitor(uid);
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

            @Override // huawei.android.security.IHwPermissionManager
            public int checkHwPermission(int uid, int pid, int permissionType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeInt(permissionType);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkHwPermission(uid, pid, permissionType);
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

            @Override // huawei.android.security.IHwPermissionManager
            public void authenticateSmsSend(IBinder notifyResult, int uidOf3RdApk, int smsId, String smsBody, String smsAddress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(notifyResult);
                    _data.writeInt(uidOf3RdApk);
                    _data.writeInt(smsId);
                    _data.writeString(smsBody);
                    _data.writeString(smsAddress);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().authenticateSmsSend(notifyResult, uidOf3RdApk, smsId, smsBody, smsAddress);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IHwPermissionManager
            public int holdServiceByRequestPermission(int uid, int pid, long permissionType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeLong(permissionType);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().holdServiceByRequestPermission(uid, pid, permissionType);
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

            @Override // huawei.android.security.IHwPermissionManager
            public String getSmsAuthPackageName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSmsAuthPackageName();
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

            @Override // huawei.android.security.IHwPermissionManager
            public void setUserAuthResult(int uid, int userSelection, long permissionType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(userSelection);
                    _data.writeLong(permissionType);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setUserAuthResult(uid, userSelection, permissionType);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwPermissionManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwPermissionManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
