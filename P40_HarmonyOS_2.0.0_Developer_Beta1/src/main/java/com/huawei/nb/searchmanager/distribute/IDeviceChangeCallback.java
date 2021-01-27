package com.huawei.nb.searchmanager.distribute;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IDeviceChangeCallback extends IInterface {
    void onDeviceOffline(DeviceInfo deviceInfo) throws RemoteException;

    void onDeviceOnline(DeviceInfo deviceInfo) throws RemoteException;

    public static abstract class Stub extends Binder implements IDeviceChangeCallback {
        private static final String DESCRIPTOR = "com.huawei.nb.searchmanager.distribute.IDeviceChangeCallback";
        static final int TRANSACTION_onDeviceOffline = 2;
        static final int TRANSACTION_onDeviceOnline = 1;

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDeviceChangeCallback asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IDeviceChangeCallback)) {
                return new Proxy(iBinder);
            }
            return (IDeviceChangeCallback) queryLocalInterface;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            DeviceInfo deviceInfo = null;
            if (i == 1) {
                parcel.enforceInterface(DESCRIPTOR);
                if (parcel.readInt() != 0) {
                    deviceInfo = DeviceInfo.CREATOR.createFromParcel(parcel);
                }
                onDeviceOnline(deviceInfo);
                return true;
            } else if (i == 2) {
                parcel.enforceInterface(DESCRIPTOR);
                if (parcel.readInt() != 0) {
                    deviceInfo = DeviceInfo.CREATOR.createFromParcel(parcel);
                }
                onDeviceOffline(deviceInfo);
                return true;
            } else if (i != 1598968902) {
                return super.onTransact(i, parcel, parcel2, i2);
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IDeviceChangeCallback {
            private IBinder mRemote;

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            @Override // com.huawei.nb.searchmanager.distribute.IDeviceChangeCallback
            public void onDeviceOnline(DeviceInfo deviceInfo) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (deviceInfo != null) {
                        obtain.writeInt(1);
                        deviceInfo.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(1, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.searchmanager.distribute.IDeviceChangeCallback
            public void onDeviceOffline(DeviceInfo deviceInfo) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (deviceInfo != null) {
                        obtain.writeInt(1);
                        deviceInfo.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(2, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }
        }
    }
}
