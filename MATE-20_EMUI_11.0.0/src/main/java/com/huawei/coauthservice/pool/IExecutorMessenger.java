package com.huawei.coauthservice.pool;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IExecutorMessenger extends IInterface {
    int finish(long j, int i, int i2, Bundle bundle) throws RemoteException;

    int notify(Bundle bundle) throws RemoteException;

    int progress(long j, int i, int i2, Bundle bundle) throws RemoteException;

    int sendData(long j, long j2, int i, int i2, Bundle bundle) throws RemoteException;

    public static abstract class Stub extends Binder implements IExecutorMessenger {
        private static final String DESCRIPTOR = "com.huawei.coauthservice.pool.IExecutorMessenger";
        static final int TRANSACTION_finish = 3;
        static final int TRANSACTION_notify = 4;
        static final int TRANSACTION_progress = 2;
        static final int TRANSACTION_sendData = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IExecutorMessenger asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IExecutorMessenger)) {
                return new Proxy(obj);
            }
            return (IExecutorMessenger) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg4;
            Bundle _arg3;
            Bundle _arg32;
            Bundle _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                long _arg02 = data.readLong();
                long _arg1 = data.readLong();
                int _arg2 = data.readInt();
                int _arg33 = data.readInt();
                if (data.readInt() != 0) {
                    _arg4 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg4 = null;
                }
                int _result = sendData(_arg02, _arg1, _arg2, _arg33, _arg4);
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                long _arg03 = data.readLong();
                int _arg12 = data.readInt();
                int _arg22 = data.readInt();
                if (data.readInt() != 0) {
                    _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg3 = null;
                }
                int _result2 = progress(_arg03, _arg12, _arg22, _arg3);
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                long _arg04 = data.readLong();
                int _arg13 = data.readInt();
                int _arg23 = data.readInt();
                if (data.readInt() != 0) {
                    _arg32 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg32 = null;
                }
                int _result3 = finish(_arg04, _arg13, _arg23, _arg32);
                reply.writeNoException();
                reply.writeInt(_result3);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                int _result4 = notify(_arg0);
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

        private static class Proxy implements IExecutorMessenger {
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

            @Override // com.huawei.coauthservice.pool.IExecutorMessenger
            public int sendData(long sessionId, long transNum, int srcType, int dstType, Bundle msg) throws RemoteException {
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
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.coauthservice.pool.IExecutorMessenger
            public int progress(long sessionId, int srcType, int progress, Bundle params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(sessionId);
                    _data.writeInt(srcType);
                    _data.writeInt(progress);
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

            @Override // com.huawei.coauthservice.pool.IExecutorMessenger
            public int finish(long sessionId, int srcType, int result, Bundle params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(sessionId);
                    _data.writeInt(srcType);
                    _data.writeInt(result);
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

            @Override // com.huawei.coauthservice.pool.IExecutorMessenger
            public int notify(Bundle state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (state != null) {
                        _data.writeInt(1);
                        state.writeToParcel(_data, 0);
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
