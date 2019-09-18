package com.huawei.nearbysdk;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IDevConnectListen extends IInterface {

    public static abstract class Stub extends Binder implements IDevConnectListen {
        private static final String DESCRIPTOR = "com.huawei.nearbysdk.IDevConnectListen";
        static final int TRANSACTION_onConnectFail = 2;
        static final int TRANSACTION_onConnectSuccess = 1;
        static final int TRANSACTION_onDevDisconnected = 3;

        private static class Proxy implements IDevConnectListen {
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

            public void onConnectSuccess(String ip) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ip);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onConnectFail(int ret) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(ret);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onDevDisconnected(NearbyDevice dev) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (dev != null) {
                        _data.writeInt(1);
                        dev.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDevConnectListen asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDevConnectListen)) {
                return new Proxy(obj);
            }
            return (IDevConnectListen) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            NearbyDevice _arg0;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onConnectSuccess(data.readString());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onConnectFail(data.readInt());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = NearbyDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        onDevDisconnected(_arg0);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void onConnectFail(int i) throws RemoteException;

    void onConnectSuccess(String str) throws RemoteException;

    void onDevDisconnected(NearbyDevice nearbyDevice) throws RemoteException;
}
