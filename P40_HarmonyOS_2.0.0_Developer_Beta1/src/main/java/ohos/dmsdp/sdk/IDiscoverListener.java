package ohos.dmsdp.sdk;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.Map;

public interface IDiscoverListener extends IInterface {
    void onDeviceFound(DMSDPDevice dMSDPDevice) throws RemoteException;

    void onDeviceLost(DMSDPDevice dMSDPDevice) throws RemoteException;

    void onDeviceUpdate(DMSDPDevice dMSDPDevice, int i) throws RemoteException;

    void onStateChanged(int i, Map map) throws RemoteException;

    public static abstract class Stub extends Binder implements IDiscoverListener {
        private static final String DESCRIPTOR = "com.huawei.dmsdpsdk.IDiscoverListener";
        static final int TRANSACTION_onDeviceFound = 1;
        static final int TRANSACTION_onDeviceLost = 2;
        static final int TRANSACTION_onDeviceUpdate = 3;
        static final int TRANSACTION_onStateChanged = 4;

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDiscoverListener asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IDiscoverListener)) {
                return new Proxy(iBinder);
            }
            return (IDiscoverListener) queryLocalInterface;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            DMSDPDevice dMSDPDevice = null;
            if (i == 1) {
                parcel.enforceInterface(DESCRIPTOR);
                if (parcel.readInt() != 0) {
                    dMSDPDevice = DMSDPDevice.CREATOR.createFromParcel(parcel);
                }
                onDeviceFound(dMSDPDevice);
                return true;
            } else if (i == 2) {
                parcel.enforceInterface(DESCRIPTOR);
                if (parcel.readInt() != 0) {
                    dMSDPDevice = DMSDPDevice.CREATOR.createFromParcel(parcel);
                }
                onDeviceLost(dMSDPDevice);
                return true;
            } else if (i == 3) {
                parcel.enforceInterface(DESCRIPTOR);
                if (parcel.readInt() != 0) {
                    dMSDPDevice = DMSDPDevice.CREATOR.createFromParcel(parcel);
                }
                onDeviceUpdate(dMSDPDevice, parcel.readInt());
                return true;
            } else if (i == 4) {
                parcel.enforceInterface(DESCRIPTOR);
                onStateChanged(parcel.readInt(), parcel.readHashMap(getClass().getClassLoader()));
                return true;
            } else if (i != 1598968902) {
                return super.onTransact(i, parcel, parcel2, i2);
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IDiscoverListener {
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

            @Override // ohos.dmsdp.sdk.IDiscoverListener
            public void onDeviceFound(DMSDPDevice dMSDPDevice) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (dMSDPDevice != null) {
                        obtain.writeInt(1);
                        dMSDPDevice.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(1, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDiscoverListener
            public void onDeviceLost(DMSDPDevice dMSDPDevice) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (dMSDPDevice != null) {
                        obtain.writeInt(1);
                        dMSDPDevice.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(2, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDiscoverListener
            public void onDeviceUpdate(DMSDPDevice dMSDPDevice, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (dMSDPDevice != null) {
                        obtain.writeInt(1);
                        dMSDPDevice.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeInt(i);
                    this.mRemote.transact(3, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDiscoverListener
            public void onStateChanged(int i, Map map) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeMap(map);
                    this.mRemote.transact(4, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }
        }
    }
}
