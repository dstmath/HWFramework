package com.huawei.softnet.nearby;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INearConnectionCallback extends IInterface {
    void onConnectionCompleted(String str, NearConnectionResult nearConnectionResult) throws RemoteException;

    void onConnectionInit(String str, NearConnectionDesc nearConnectionDesc) throws RemoteException;

    void onDisconnection(String str, NearConnectionResult nearConnectionResult) throws RemoteException;

    public static abstract class Stub extends Binder implements INearConnectionCallback {
        private static final String DESCRIPTOR = "com.huawei.softnet.nearby.INearConnectionCallback";
        static final int TRANSACTION_onConnectionCompleted = 2;
        static final int TRANSACTION_onConnectionInit = 1;
        static final int TRANSACTION_onDisconnection = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INearConnectionCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INearConnectionCallback)) {
                return new Proxy(obj);
            }
            return (INearConnectionCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            NearConnectionResult _arg1;
            NearConnectionResult _arg12;
            NearConnectionDesc _arg13;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    String _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg13 = NearConnectionDesc.CREATOR.createFromParcel(data);
                    } else {
                        _arg13 = null;
                    }
                    onConnectionInit(_arg0, _arg13);
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    String _arg02 = data.readString();
                    if (data.readInt() != 0) {
                        _arg12 = NearConnectionResult.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    onConnectionCompleted(_arg02, _arg12);
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    String _arg03 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = NearConnectionResult.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    onDisconnection(_arg03, _arg1);
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        private static class Proxy implements INearConnectionCallback {
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

            @Override // com.huawei.softnet.nearby.INearConnectionCallback
            public void onConnectionInit(String deviceId, NearConnectionDesc info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.softnet.nearby.INearConnectionCallback
            public void onConnectionCompleted(String deviceId, NearConnectionResult result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    if (result != null) {
                        _data.writeInt(1);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.softnet.nearby.INearConnectionCallback
            public void onDisconnection(String deviceId, NearConnectionResult result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    if (result != null) {
                        _data.writeInt(1);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
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
