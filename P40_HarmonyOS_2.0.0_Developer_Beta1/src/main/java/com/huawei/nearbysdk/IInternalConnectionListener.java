package com.huawei.nearbysdk;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IInternalConnectionListener extends IInterface {
    void onConnectionChange(NearbyDevice nearbyDevice, int i) throws RemoteException;

    void onReceive(NearbyDevice nearbyDevice, byte[] bArr) throws RemoteException;

    void onStatusChange(int i) throws RemoteException;

    public static abstract class Stub extends Binder implements IInternalConnectionListener {
        private static final String DESCRIPTOR = "com.huawei.nearbysdk.IInternalConnectionListener";
        static final int TRANSACTION_onConnectionChange = 2;
        static final int TRANSACTION_onReceive = 3;
        static final int TRANSACTION_onStatusChange = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IInternalConnectionListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IInternalConnectionListener)) {
                return new Proxy(obj);
            }
            return (IInternalConnectionListener) iin;
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
                    onStatusChange(data.readInt());
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = NearbyDevice.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    onConnectionChange(_arg02, data.readInt());
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = NearbyDevice.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    onReceive(_arg0, data.createByteArray());
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        private static class Proxy implements IInternalConnectionListener {
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

            @Override // com.huawei.nearbysdk.IInternalConnectionListener
            public void onStatusChange(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.IInternalConnectionListener
            public void onConnectionChange(NearbyDevice device, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(status);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.IInternalConnectionListener
            public void onReceive(NearbyDevice device, byte[] recvMessage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeByteArray(recvMessage);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }
    }
}
