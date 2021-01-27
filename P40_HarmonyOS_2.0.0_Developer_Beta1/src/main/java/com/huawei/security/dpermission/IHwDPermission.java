package com.huawei.security.dpermission;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.security.dpermission.permissionaccessrecord.parcel.PermissionRecordRequestParcel;
import com.huawei.security.dpermission.permissionaccessrecord.parcel.PermissionRecordResponseParcel;
import com.huawei.security.dpermission.permissionusingremind.IOnUsingPermissionReminder;

public interface IHwDPermission extends IInterface {
    int allocateDuid(String str, int i) throws RemoteException;

    int checkDPermission(int i, String str) throws RemoteException;

    int getPermissionUsedRecord(PermissionRecordRequestParcel permissionRecordRequestParcel, PermissionRecordResponseParcel permissionRecordResponseParcel) throws RemoteException;

    int notifyPermissionChanged(int i, String str, int i2) throws RemoteException;

    int notifyUidPermissionChanged(int i) throws RemoteException;

    int queryDuid(String str, int i) throws RemoteException;

    int registerOnUsingPermissionReminder(IOnUsingPermissionReminder iOnUsingPermissionReminder) throws RemoteException;

    int unregisterOnUsingPermissionReminder(IOnUsingPermissionReminder iOnUsingPermissionReminder) throws RemoteException;

    public static abstract class Stub extends Binder implements IHwDPermission {
        private static final String DESCRIPTOR = "com.huawei.security.dpermission.IHwDPermission";
        static final int TRANSACTION_allocateDuid = 1;
        static final int TRANSACTION_checkDPermission = 3;
        static final int TRANSACTION_getPermissionUsedRecord = 8;
        static final int TRANSACTION_notifyPermissionChanged = 5;
        static final int TRANSACTION_notifyUidPermissionChanged = 4;
        static final int TRANSACTION_queryDuid = 2;
        static final int TRANSACTION_registerOnUsingPermissionReminder = 6;
        static final int TRANSACTION_unregisterOnUsingPermissionReminder = 7;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwDPermission asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwDPermission)) {
                return new Proxy(obj);
            }
            return (IHwDPermission) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PermissionRecordRequestParcel _arg0;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = allocateDuid(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = queryDuid(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = checkDPermission(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = notifyUidPermissionChanged(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = notifyPermissionChanged(data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = registerOnUsingPermissionReminder(IOnUsingPermissionReminder.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = unregisterOnUsingPermissionReminder(IOnUsingPermissionReminder.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = PermissionRecordRequestParcel.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        PermissionRecordResponseParcel _arg1 = new PermissionRecordResponseParcel();
                        int _result8 = getPermissionUsedRecord(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        reply.writeInt(1);
                        _arg1.writeToParcel(reply, 1);
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
        public static class Proxy implements IHwDPermission {
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

            @Override // com.huawei.security.dpermission.IHwDPermission
            public int allocateDuid(String nodeId, int rUid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(nodeId);
                    _data.writeInt(rUid);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IHwDPermission
            public int queryDuid(String nodeId, int rUid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(nodeId);
                    _data.writeInt(rUid);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IHwDPermission
            public int checkDPermission(int dUid, String permissionName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(dUid);
                    _data.writeString(permissionName);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IHwDPermission
            public int notifyUidPermissionChanged(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IHwDPermission
            public int notifyPermissionChanged(int uid, String permissionName, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(permissionName);
                    _data.writeInt(status);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IHwDPermission
            public int registerOnUsingPermissionReminder(IOnUsingPermissionReminder callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IHwDPermission
            public int unregisterOnUsingPermissionReminder(IOnUsingPermissionReminder callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IHwDPermission
            public int getPermissionUsedRecord(PermissionRecordRequestParcel request, PermissionRecordResponseParcel response) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        response.readFromParcel(_reply);
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
