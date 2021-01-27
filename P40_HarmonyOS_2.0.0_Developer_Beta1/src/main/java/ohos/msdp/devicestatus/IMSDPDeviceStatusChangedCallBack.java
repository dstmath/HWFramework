package ohos.msdp.devicestatus;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMSDPDeviceStatusChangedCallBack extends IInterface {
    void onDeviceStatusChanged(HwMSDPDeviceStatusChangeEvent hwMSDPDeviceStatusChangeEvent) throws RemoteException;

    public static abstract class Stub extends Binder implements IMSDPDeviceStatusChangedCallBack {
        private static final String DESCRIPTOR = "com.huawei.msdp.devicestatus.IMSDPDeviceStatusChangedCallBack";
        static final int TRANSACTION_onDeviceStatusChanged = 1;

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMSDPDeviceStatusChangedCallBack asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IMSDPDeviceStatusChangedCallBack)) {
                return new Proxy(iBinder);
            }
            return (IMSDPDeviceStatusChangedCallBack) queryLocalInterface;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i == 1) {
                parcel.enforceInterface(DESCRIPTOR);
                onDeviceStatusChanged(parcel.readInt() != 0 ? HwMSDPDeviceStatusChangeEvent.CREATOR.createFromParcel(parcel) : null);
                parcel2.writeNoException();
                return true;
            } else if (i != 1598968902) {
                return super.onTransact(i, parcel, parcel2, i2);
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IMSDPDeviceStatusChangedCallBack {
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

            @Override // ohos.msdp.devicestatus.IMSDPDeviceStatusChangedCallBack
            public void onDeviceStatusChanged(HwMSDPDeviceStatusChangeEvent hwMSDPDeviceStatusChangeEvent) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (hwMSDPDeviceStatusChangeEvent != null) {
                        obtain.writeInt(1);
                        hwMSDPDeviceStatusChangeEvent.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }
    }
}
