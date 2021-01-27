package com.huawei.security.dpermission;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IRequestPermissionsResult extends IInterface {
    void onCancel(String str, String[] strArr) throws RemoteException;

    void onResult(String str, String[] strArr, int[] iArr) throws RemoteException;

    void onTimeOut(String str, String[] strArr) throws RemoteException;

    public static abstract class Stub extends Binder implements IRequestPermissionsResult {
        private static final String DESCRIPTOR = "com.huawei.security.dpermission.IRequestPermissionsResult";
        static final int TRANSACTION_onCancel = 2;
        static final int TRANSACTION_onResult = 1;
        static final int TRANSACTION_onTimeOut = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRequestPermissionsResult asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IRequestPermissionsResult)) {
                return new Proxy(obj);
            }
            return (IRequestPermissionsResult) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onResult(data.readString(), data.createStringArray(), data.createIntArray());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onCancel(data.readString(), data.createStringArray());
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onTimeOut(data.readString(), data.createStringArray());
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IRequestPermissionsResult {
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

            @Override // com.huawei.security.dpermission.IRequestPermissionsResult
            public void onResult(String nodeId, String[] permissions, int[] grantResults) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(nodeId);
                    _data.writeStringArray(permissions);
                    _data.writeIntArray(grantResults);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IRequestPermissionsResult
            public void onCancel(String nodeId, String[] permissions) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(nodeId);
                    _data.writeStringArray(permissions);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.IRequestPermissionsResult
            public void onTimeOut(String nodeId, String[] permissions) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(nodeId);
                    _data.writeStringArray(permissions);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
