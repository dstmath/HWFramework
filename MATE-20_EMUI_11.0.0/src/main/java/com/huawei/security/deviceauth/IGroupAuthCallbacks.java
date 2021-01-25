package com.huawei.security.deviceauth;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGroupAuthCallbacks extends IInterface {
    void onError(long j, int i, int i2, String str) throws RemoteException;

    void onFinish(long j, int i, String str) throws RemoteException;

    String onRequest(long j, int i, String str) throws RemoteException;

    void onSessionKeyReturned(long j, byte[] bArr) throws RemoteException;

    boolean onTransmit(long j, byte[] bArr) throws RemoteException;

    public static abstract class Stub extends Binder implements IGroupAuthCallbacks {
        private static final String DESCRIPTOR = "com.huawei.security.deviceauth.IGroupAuthCallbacks";
        static final int TRANSACTION_onError = 2;
        static final int TRANSACTION_onFinish = 1;
        static final int TRANSACTION_onRequest = 4;
        static final int TRANSACTION_onSessionKeyReturned = 5;
        static final int TRANSACTION_onTransmit = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGroupAuthCallbacks asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGroupAuthCallbacks)) {
                return new Proxy(obj);
            }
            return (IGroupAuthCallbacks) iin;
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
                boolean onTransmit = onTransmit(data.readLong(), data.createByteArray());
                reply.writeNoException();
                reply.writeInt(onTransmit ? 1 : 0);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                String _result = onRequest(data.readLong(), data.readInt(), data.readString());
                reply.writeNoException();
                reply.writeString(_result);
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                onSessionKeyReturned(data.readLong(), data.createByteArray());
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IGroupAuthCallbacks {
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

            @Override // com.huawei.security.deviceauth.IGroupAuthCallbacks
            public void onFinish(long reqId, int authForm, String authReturn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(reqId);
                    _data.writeInt(authForm);
                    _data.writeString(authReturn);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupAuthCallbacks
            public void onError(long reqId, int authForm, int errorCode, String errorReturn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(reqId);
                    _data.writeInt(authForm);
                    _data.writeInt(errorCode);
                    _data.writeString(errorReturn);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupAuthCallbacks
            public boolean onTransmit(long reqId, byte[] data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(reqId);
                    _data.writeByteArray(data);
                    boolean _result = false;
                    this.mRemote.transact(3, _data, _reply, 0);
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

            @Override // com.huawei.security.deviceauth.IGroupAuthCallbacks
            public String onRequest(long reqId, int authForm, String reqParams) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(reqId);
                    _data.writeInt(authForm);
                    _data.writeString(reqParams);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IGroupAuthCallbacks
            public void onSessionKeyReturned(long reqId, byte[] sessionKey) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(reqId);
                    _data.writeByteArray(sessionKey);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
