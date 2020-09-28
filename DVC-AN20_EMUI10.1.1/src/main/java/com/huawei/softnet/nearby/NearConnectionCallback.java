package com.huawei.softnet.nearby;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface NearConnectionCallback extends IInterface {
    void onConnectionCompleted(String str, NearConnectionResult nearConnectionResult) throws RemoteException;

    void onConnectionInit(String str, NearConnectionDesc nearConnectionDesc) throws RemoteException;

    void onDisconnection(String str, NearConnectionResult nearConnectionResult) throws RemoteException;

    public static class Default implements NearConnectionCallback {
        @Override // com.huawei.softnet.nearby.NearConnectionCallback
        public void onConnectionInit(String deviceId, NearConnectionDesc info) throws RemoteException {
        }

        @Override // com.huawei.softnet.nearby.NearConnectionCallback
        public void onConnectionCompleted(String deviceId, NearConnectionResult result) throws RemoteException {
        }

        @Override // com.huawei.softnet.nearby.NearConnectionCallback
        public void onDisconnection(String deviceId, NearConnectionResult result) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements NearConnectionCallback {
        private static final String DESCRIPTOR = "com.huawei.softnet.nearby.NearConnectionCallback";
        static final int TRANSACTION_onConnectionCompleted = 2;
        static final int TRANSACTION_onConnectionInit = 1;
        static final int TRANSACTION_onDisconnection = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static NearConnectionCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof NearConnectionCallback)) {
                return new Proxy(obj);
            }
            return (NearConnectionCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            NearConnectionDesc _arg1;
            NearConnectionResult _arg12;
            NearConnectionResult _arg13;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                String _arg0 = data.readString();
                if (data.readInt() != 0) {
                    _arg1 = NearConnectionDesc.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                onConnectionInit(_arg0, _arg1);
                reply.writeNoException();
                return true;
            } else if (code == 2) {
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
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                String _arg03 = data.readString();
                if (data.readInt() != 0) {
                    _arg13 = NearConnectionResult.CREATOR.createFromParcel(data);
                } else {
                    _arg13 = null;
                }
                onDisconnection(_arg03, _arg13);
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
        public static class Proxy implements NearConnectionCallback {
            public static NearConnectionCallback sDefaultImpl;
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

            @Override // com.huawei.softnet.nearby.NearConnectionCallback
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
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onConnectionInit(deviceId, info);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.softnet.nearby.NearConnectionCallback
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
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onConnectionCompleted(deviceId, result);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.softnet.nearby.NearConnectionCallback
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
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onDisconnection(deviceId, result);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(NearConnectionCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static NearConnectionCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
