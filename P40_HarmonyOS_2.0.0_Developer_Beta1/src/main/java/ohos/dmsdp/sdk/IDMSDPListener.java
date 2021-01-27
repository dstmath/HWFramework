package ohos.dmsdp.sdk;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.Map;

public interface IDMSDPListener extends IInterface {
    void onDeviceChange(DMSDPDevice dMSDPDevice, int i, Map map) throws RemoteException;

    void onDeviceServiceChange(DMSDPDeviceService dMSDPDeviceService, int i, Map map) throws RemoteException;

    public static abstract class Stub extends Binder implements IDMSDPListener {
        private static final String DESCRIPTOR = "com.huawei.dmsdpsdk.IDMSDPListener";
        static final int TRANSACTION_onDeviceChange = 1;
        static final int TRANSACTION_onDeviceServiceChange = 2;

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDMSDPListener asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IDMSDPListener)) {
                return new Proxy(iBinder);
            }
            return (IDMSDPListener) queryLocalInterface;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            DMSDPDevice dMSDPDevice = null;
            DMSDPDeviceService dMSDPDeviceService = null;
            if (i == 1) {
                parcel.enforceInterface(DESCRIPTOR);
                if (parcel.readInt() != 0) {
                    dMSDPDevice = DMSDPDevice.CREATOR.createFromParcel(parcel);
                }
                onDeviceChange(dMSDPDevice, parcel.readInt(), parcel.readHashMap(getClass().getClassLoader()));
                parcel2.writeNoException();
                return true;
            } else if (i == 2) {
                parcel.enforceInterface(DESCRIPTOR);
                if (parcel.readInt() != 0) {
                    dMSDPDeviceService = DMSDPDeviceService.CREATOR.createFromParcel(parcel);
                }
                onDeviceServiceChange(dMSDPDeviceService, parcel.readInt(), parcel.readHashMap(getClass().getClassLoader()));
                parcel2.writeNoException();
                return true;
            } else if (i != 1598968902) {
                return super.onTransact(i, parcel, parcel2, i2);
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IDMSDPListener {
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

            @Override // ohos.dmsdp.sdk.IDMSDPListener
            public void onDeviceChange(DMSDPDevice dMSDPDevice, int i, Map map) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (dMSDPDevice != null) {
                        obtain.writeInt(1);
                        dMSDPDevice.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeInt(i);
                    obtain.writeMap(map);
                    this.mRemote.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.dmsdp.sdk.IDMSDPListener
            public void onDeviceServiceChange(DMSDPDeviceService dMSDPDeviceService, int i, Map map) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (dMSDPDeviceService != null) {
                        obtain.writeInt(1);
                        dMSDPDeviceService.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeInt(i);
                    obtain.writeMap(map);
                    this.mRemote.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }
    }
}
