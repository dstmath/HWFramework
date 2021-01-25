package com.huawei.coauthservice.pool;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.coauthservice.pool.IExecutorMessenger;

public interface IExecutorProcessor extends IInterface {
    int beginExecute(long j, byte[] bArr, Bundle bundle) throws RemoteException;

    int endExecute(long j, Bundle bundle) throws RemoteException;

    Bundle getProperty(Bundle bundle) throws RemoteException;

    void onMessengerReady(IExecutorMessenger iExecutorMessenger) throws RemoteException;

    int prepareExecute(long j, byte[] bArr, Bundle bundle) throws RemoteException;

    int receiveData(long j, long j2, int i, int i2, Bundle bundle) throws RemoteException;

    int setProperty(Bundle bundle) throws RemoteException;

    public static abstract class Stub extends Binder implements IExecutorProcessor {
        private static final String DESCRIPTOR = "com.huawei.coauthservice.pool.IExecutorProcessor";
        static final int TRANSACTION_beginExecute = 3;
        static final int TRANSACTION_endExecute = 5;
        static final int TRANSACTION_getProperty = 7;
        static final int TRANSACTION_onMessengerReady = 1;
        static final int TRANSACTION_prepareExecute = 2;
        static final int TRANSACTION_receiveData = 4;
        static final int TRANSACTION_setProperty = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IExecutorProcessor asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IExecutorProcessor)) {
                return new Proxy(obj);
            }
            return (IExecutorProcessor) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg2;
            Bundle _arg22;
            Bundle _arg4;
            Bundle _arg1;
            Bundle _arg0;
            Bundle _arg02;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onMessengerReady(IExecutorMessenger.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        long _arg03 = data.readLong();
                        byte[] _arg12 = data.createByteArray();
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        int _result = prepareExecute(_arg03, _arg12, _arg2);
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        long _arg04 = data.readLong();
                        byte[] _arg13 = data.createByteArray();
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        int _result2 = beginExecute(_arg04, _arg13, _arg22);
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        long _arg05 = data.readLong();
                        long _arg14 = data.readLong();
                        int _arg23 = data.readInt();
                        int _arg3 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg4 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg4 = null;
                        }
                        int _result3 = receiveData(_arg05, _arg14, _arg23, _arg3, _arg4);
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        long _arg06 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        int _result4 = endExecute(_arg06, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        int _result5 = setProperty(_arg0);
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        Bundle _result6 = getProperty(_arg02);
                        reply.writeNoException();
                        if (_result6 != null) {
                            reply.writeInt(1);
                            _result6.writeToParcel(reply, 1);
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

        private static class Proxy implements IExecutorProcessor {
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

            @Override // com.huawei.coauthservice.pool.IExecutorProcessor
            public void onMessengerReady(IExecutorMessenger messenger) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(messenger != null ? messenger.asBinder() : null);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.coauthservice.pool.IExecutorProcessor
            public int prepareExecute(long sessionId, byte[] publicKey, Bundle params) throws RemoteException {
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
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.coauthservice.pool.IExecutorProcessor
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
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.coauthservice.pool.IExecutorProcessor
            public int receiveData(long sessionId, long transNum, int srcType, int dstType, Bundle msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(sessionId);
                    _data.writeLong(transNum);
                    _data.writeInt(srcType);
                    _data.writeInt(dstType);
                    if (msg != null) {
                        _data.writeInt(1);
                        msg.writeToParcel(_data, 0);
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

            @Override // com.huawei.coauthservice.pool.IExecutorProcessor
            public int endExecute(long sessionId, Bundle params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(sessionId);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.coauthservice.pool.IExecutorProcessor
            public int setProperty(Bundle property) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (property != null) {
                        _data.writeInt(1);
                        property.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.coauthservice.pool.IExecutorProcessor
            public Bundle getProperty(Bundle key) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (key != null) {
                        _data.writeInt(1);
                        key.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
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
    }
}
