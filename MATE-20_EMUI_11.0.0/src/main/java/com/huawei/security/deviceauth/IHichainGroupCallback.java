package com.huawei.security.deviceauth;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHichainGroupCallback extends IInterface {
    void onError(long j, int i, int i2, String str) throws RemoteException;

    void onFinish(long j, int i, String str) throws RemoteException;

    String onRequest(long j, int i, String str) throws RemoteException;

    public static abstract class Stub extends Binder implements IHichainGroupCallback {
        private static final String DESCRIPTOR = "com.huawei.security.deviceauth.IHichainGroupCallback";
        static final int TRANSACTION_onError = 2;
        static final int TRANSACTION_onFinish = 1;
        static final int TRANSACTION_onRequest = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHichainGroupCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHichainGroupCallback)) {
                return new Proxy(obj);
            }
            return (IHichainGroupCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onFinish(data.readLong(), data.readInt(), data.readString());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onError(data.readLong(), data.readInt(), data.readInt(), data.readString());
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                String _result = onRequest(data.readLong(), data.readInt(), data.readString());
                reply.writeNoException();
                reply.writeString(_result);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHichainGroupCallback {
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

            @Override // com.huawei.security.deviceauth.IHichainGroupCallback
            public void onFinish(long requestId, int operationCode, String returnData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(requestId);
                    _data.writeInt(operationCode);
                    _data.writeString(returnData);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IHichainGroupCallback
            public void onError(long requestId, int operationCode, int errorCode, String errorReturn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(requestId);
                    _data.writeInt(operationCode);
                    _data.writeInt(errorCode);
                    _data.writeString(errorReturn);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IHichainGroupCallback
            public String onRequest(long requestId, int operationCode, String reqParams) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(requestId);
                    _data.writeInt(operationCode);
                    _data.writeString(reqParams);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
