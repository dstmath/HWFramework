package com.huawei.coauthservice.pool;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.coauthservice.pool.ExecutorCallback;
import java.util.List;

public interface SecureRegCallBack extends IInterface {
    int executeFinish(long j, int i, Bundle bundle) throws RemoteException;

    int executeSendData(long j, long j2, int i, int i2, Bundle bundle) throws RemoteException;

    int executorSecureRegister(int i, String str, List<String> list, byte[] bArr, ExecutorCallback executorCallback) throws RemoteException;

    int executorSecureUnregister(List<String> list) throws RemoteException;

    public static abstract class Stub extends Binder implements SecureRegCallBack {
        private static final String DESCRIPTOR = "com.huawei.coauthservice.pool.SecureRegCallBack";
        static final int TRANSACTION_executeFinish = 3;
        static final int TRANSACTION_executeSendData = 4;
        static final int TRANSACTION_executorSecureRegister = 1;
        static final int TRANSACTION_executorSecureUnregister = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static SecureRegCallBack asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof SecureRegCallBack)) {
                return new Proxy(obj);
            }
            return (SecureRegCallBack) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg2;
            Bundle _arg4;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _result = executorSecureRegister(data.readInt(), data.readString(), data.createStringArrayList(), data.createByteArray(), ExecutorCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _result2 = executorSecureUnregister(data.createStringArrayList());
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                long _arg0 = data.readLong();
                int _arg1 = data.readInt();
                if (data.readInt() != 0) {
                    _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                int _result3 = executeFinish(_arg0, _arg1, _arg2);
                reply.writeNoException();
                reply.writeInt(_result3);
                return true;
            } else if (code == 4) {
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
                int _result4 = executeSendData(_arg02, _arg12, _arg22, _arg3, _arg4);
                reply.writeNoException();
                reply.writeInt(_result4);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements SecureRegCallBack {
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

            @Override // com.huawei.coauthservice.pool.SecureRegCallBack
            public int executorSecureRegister(int executorType, String gid, List<String> executors, byte[] publicKey, ExecutorCallback executorCallback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(executorType);
                    _data.writeString(gid);
                    _data.writeStringList(executors);
                    _data.writeByteArray(publicKey);
                    _data.writeStrongBinder(executorCallback != null ? executorCallback.asBinder() : null);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.coauthservice.pool.SecureRegCallBack
            public int executorSecureUnregister(List<String> executors) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(executors);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.coauthservice.pool.SecureRegCallBack
            public int executeFinish(long sessionId, int resultCode, Bundle params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(sessionId);
                    _data.writeInt(resultCode);
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

            @Override // com.huawei.coauthservice.pool.SecureRegCallBack
            public int executeSendData(long sessionId, long transNum, int srcType, int dstType, Bundle params) throws RemoteException {
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
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
