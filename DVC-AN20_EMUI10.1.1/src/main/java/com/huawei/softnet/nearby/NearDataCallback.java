package com.huawei.softnet.nearby;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface NearDataCallback extends IInterface {
    void onDataReceive(String str, int i, byte[] bArr, int i2, String str2) throws RemoteException;

    void onDataUpdate() throws RemoteException;

    public static class Default implements NearDataCallback {
        @Override // com.huawei.softnet.nearby.NearDataCallback
        public void onDataReceive(String deviceId, int type, byte[] data, int len, String extInfo) throws RemoteException {
        }

        @Override // com.huawei.softnet.nearby.NearDataCallback
        public void onDataUpdate() throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements NearDataCallback {
        private static final String DESCRIPTOR = "com.huawei.softnet.nearby.NearDataCallback";
        static final int TRANSACTION_onDataReceive = 1;
        static final int TRANSACTION_onDataUpdate = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static NearDataCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof NearDataCallback)) {
                return new Proxy(obj);
            }
            return (NearDataCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onDataReceive(data.readString(), data.readInt(), data.createByteArray(), data.readInt(), data.readString());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onDataUpdate();
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
        public static class Proxy implements NearDataCallback {
            public static NearDataCallback sDefaultImpl;
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

            @Override // com.huawei.softnet.nearby.NearDataCallback
            public void onDataReceive(String deviceId, int type, byte[] data, int len, String extInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceId);
                    _data.writeInt(type);
                    _data.writeByteArray(data);
                    _data.writeInt(len);
                    _data.writeString(extInfo);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onDataReceive(deviceId, type, data, len, extInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.softnet.nearby.NearDataCallback
            public void onDataUpdate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onDataUpdate();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(NearDataCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static NearDataCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
