package com.huawei.coauthservice.pool;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ExecutorCallback extends IInterface {
    int beginExecute(long j, byte[] bArr, Bundle bundle) throws RemoteException;

    int endExecute(long j) throws RemoteException;

    byte[] getProperty(byte[] bArr) throws RemoteException;

    int onReceiveData(long j, long j2, int i, int i2, Bundle bundle) throws RemoteException;

    int setProperty(byte[] bArr, byte[] bArr2) throws RemoteException;

    public static abstract class Stub extends Binder implements ExecutorCallback {
        private static final String DESCRIPTOR = "com.huawei.coauthservice.pool.ExecutorCallback";
        static final int TRANSACTION_beginExecute = 1;
        static final int TRANSACTION_endExecute = 2;
        static final int TRANSACTION_getProperty = 5;
        static final int TRANSACTION_onReceiveData = 3;
        static final int TRANSACTION_setProperty = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ExecutorCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ExecutorCallback)) {
                return new Proxy(obj);
            }
            return (ExecutorCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg2;
            Bundle _arg4;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                long _arg0 = data.readLong();
                byte[] _arg1 = data.createByteArray();
                if (data.readInt() != 0) {
                    _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                int _result = beginExecute(_arg0, _arg1, _arg2);
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _result2 = endExecute(data.readLong());
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                long _arg02 = data.readLong();
                long _arg12 = data.readLong();
                int _arg22 = data.readInt();
                int _arg3 = data.readInt();
                if (data.readInt() != 0) {
                    _arg4 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg4 = null;
                }
                int _result3 = onReceiveData(_arg02, _arg12, _arg22, _arg3, _arg4);
                reply.writeNoException();
                reply.writeInt(_result3);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                int _result4 = setProperty(data.createByteArray(), data.createByteArray());
                reply.writeNoException();
                reply.writeInt(_result4);
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                byte[] _result5 = getProperty(data.createByteArray());
                reply.writeNoException();
                reply.writeByteArray(_result5);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements ExecutorCallback {
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

            @Override // com.huawei.coauthservice.pool.ExecutorCallback
            public int beginExecute(long sessionId, byte[] publicKey, Bundle params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(sessionId);
                    _data.writeByteArray(publicKey);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.coauthservice.pool.ExecutorCallback
            public int endExecute(long sessionId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(sessionId);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.coauthservice.pool.ExecutorCallback
            public int onReceiveData(long sessionId, long transNum, int srcType, int dstType, Bundle params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(sessionId);
                    _data.writeLong(transNum);
                    _data.writeInt(srcType);
                    _data.writeInt(dstType);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.coauthservice.pool.ExecutorCallback
            public int setProperty(byte[] property, byte[] value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(property);
                    _data.writeByteArray(value);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.coauthservice.pool.ExecutorCallback
            public byte[] getProperty(byte[] property) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(property);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createByteArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
