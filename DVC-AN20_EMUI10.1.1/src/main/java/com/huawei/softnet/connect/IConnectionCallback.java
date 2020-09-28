package com.huawei.softnet.connect;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IConnectionCallback extends IInterface {
    void onConnectionInit(String str, String str2, String str3) throws RemoteException;

    void onConnectionStateUpdate(String str, String str2, int i, String str3) throws RemoteException;

    public static class Default implements IConnectionCallback {
        @Override // com.huawei.softnet.connect.IConnectionCallback
        public void onConnectionInit(String remoteDeviceId, String remoteModuleName, String para) throws RemoteException {
        }

        @Override // com.huawei.softnet.connect.IConnectionCallback
        public void onConnectionStateUpdate(String remoteDeviceId, String remoteModuleName, int state, String para) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IConnectionCallback {
        private static final String DESCRIPTOR = "com.huawei.softnet.connect.IConnectionCallback";
        static final int TRANSACTION_onConnectionInit = 1;
        static final int TRANSACTION_onConnectionStateUpdate = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IConnectionCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IConnectionCallback)) {
                return new Proxy(obj);
            }
            return (IConnectionCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onConnectionInit(data.readString(), data.readString(), data.readString());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onConnectionStateUpdate(data.readString(), data.readString(), data.readInt(), data.readString());
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
        public static class Proxy implements IConnectionCallback {
            public static IConnectionCallback sDefaultImpl;
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

            @Override // com.huawei.softnet.connect.IConnectionCallback
            public void onConnectionInit(String remoteDeviceId, String remoteModuleName, String para) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(remoteDeviceId);
                    _data.writeString(remoteModuleName);
                    _data.writeString(para);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onConnectionInit(remoteDeviceId, remoteModuleName, para);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.softnet.connect.IConnectionCallback
            public void onConnectionStateUpdate(String remoteDeviceId, String remoteModuleName, int state, String para) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(remoteDeviceId);
                    _data.writeString(remoteModuleName);
                    _data.writeInt(state);
                    _data.writeString(para);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onConnectionStateUpdate(remoteDeviceId, remoteModuleName, state, para);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IConnectionCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IConnectionCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
