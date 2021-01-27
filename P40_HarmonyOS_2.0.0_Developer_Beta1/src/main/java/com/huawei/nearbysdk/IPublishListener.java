package com.huawei.nearbysdk;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPublishListener extends IInterface {
    void onDeviceFound(NearbyDevice nearbyDevice) throws RemoteException;

    void onDeviceLost(NearbyDevice nearbyDevice) throws RemoteException;

    void onLocalDeviceChange(int i) throws RemoteException;

    void onStatusChanged(int i) throws RemoteException;

    public static abstract class Stub extends Binder implements IPublishListener {
        private static final String DESCRIPTOR = "com.huawei.nearbysdk.IPublishListener";
        static final int TRANSACTION_onDeviceFound = 2;
        static final int TRANSACTION_onDeviceLost = 3;
        static final int TRANSACTION_onLocalDeviceChange = 4;
        static final int TRANSACTION_onStatusChanged = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPublishListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPublishListener)) {
                return new Proxy(obj);
            }
            return (IPublishListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            NearbyDevice _arg0;
            NearbyDevice _arg02;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onStatusChanged(data.readInt());
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = NearbyDevice.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    onDeviceFound(_arg02);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = NearbyDevice.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    onDeviceLost(_arg0);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    onLocalDeviceChange(data.readInt());
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        private static class Proxy implements IPublishListener {
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

            @Override // com.huawei.nearbysdk.IPublishListener
            public void onStatusChanged(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.IPublishListener
            public void onDeviceFound(NearbyDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.IPublishListener
            public void onDeviceLost(NearbyDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.IPublishListener
            public void onLocalDeviceChange(int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }
    }
}
