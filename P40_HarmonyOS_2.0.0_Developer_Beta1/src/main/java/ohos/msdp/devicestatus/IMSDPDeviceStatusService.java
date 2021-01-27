package ohos.msdp.devicestatus;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import ohos.msdp.devicestatus.IMSDPDeviceStatusChangedCallBack;

public interface IMSDPDeviceStatusService extends IInterface {
    boolean disableDeviceStatusService(String str, String str2, int i) throws RemoteException;

    boolean enableDeviceStatusService(String str, String str2, int i, long j) throws RemoteException;

    boolean freeDeviceStatusService(String str, IMSDPDeviceStatusChangedCallBack iMSDPDeviceStatusChangedCallBack) throws RemoteException;

    HwMSDPDeviceStatusChangeEvent getCurrentDeviceStatus(String str) throws RemoteException;

    String[] getSupportDeviceStatus() throws RemoteException;

    boolean registerDeviceStatusCallBack(String str, IMSDPDeviceStatusChangedCallBack iMSDPDeviceStatusChangedCallBack) throws RemoteException;

    public static abstract class Stub extends Binder implements IMSDPDeviceStatusService {
        private static final String DESCRIPTOR = "com.huawei.msdp.devicestatus.IMSDPDeviceStatusService";
        static final int TRANSACTION_disableDeviceStatusService = 5;
        static final int TRANSACTION_enableDeviceStatusService = 4;
        static final int TRANSACTION_freeDeviceStatusService = 3;
        static final int TRANSACTION_getCurrentDeviceStatus = 6;
        static final int TRANSACTION_getSupportDeviceStatus = 1;
        static final int TRANSACTION_registerDeviceStatusCallBack = 2;

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMSDPDeviceStatusService asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IMSDPDeviceStatusService)) {
                return new Proxy(iBinder);
            }
            return (IMSDPDeviceStatusService) queryLocalInterface;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i != 1598968902) {
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] supportDeviceStatus = getSupportDeviceStatus();
                        parcel2.writeNoException();
                        parcel2.writeStringArray(supportDeviceStatus);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean registerDeviceStatusCallBack = registerDeviceStatusCallBack(parcel.readString(), IMSDPDeviceStatusChangedCallBack.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(registerDeviceStatusCallBack ? 1 : 0);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean freeDeviceStatusService = freeDeviceStatusService(parcel.readString(), IMSDPDeviceStatusChangedCallBack.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(freeDeviceStatusService ? 1 : 0);
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean enableDeviceStatusService = enableDeviceStatusService(parcel.readString(), parcel.readString(), parcel.readInt(), parcel.readLong());
                        parcel2.writeNoException();
                        parcel2.writeInt(enableDeviceStatusService ? 1 : 0);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean disableDeviceStatusService = disableDeviceStatusService(parcel.readString(), parcel.readString(), parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeInt(disableDeviceStatusService ? 1 : 0);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        HwMSDPDeviceStatusChangeEvent currentDeviceStatus = getCurrentDeviceStatus(parcel.readString());
                        parcel2.writeNoException();
                        if (currentDeviceStatus != null) {
                            parcel2.writeInt(1);
                            currentDeviceStatus.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    default:
                        return super.onTransact(i, parcel, parcel2, i2);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IMSDPDeviceStatusService {
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

            @Override // ohos.msdp.devicestatus.IMSDPDeviceStatusService
            public String[] getSupportDeviceStatus() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.createStringArray();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.msdp.devicestatus.IMSDPDeviceStatusService
            public boolean registerDeviceStatusCallBack(String str, IMSDPDeviceStatusChangedCallBack iMSDPDeviceStatusChangedCallBack) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeStrongBinder(iMSDPDeviceStatusChangedCallBack != null ? iMSDPDeviceStatusChangedCallBack.asBinder() : null);
                    boolean z = false;
                    this.mRemote.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        z = true;
                    }
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.msdp.devicestatus.IMSDPDeviceStatusService
            public boolean freeDeviceStatusService(String str, IMSDPDeviceStatusChangedCallBack iMSDPDeviceStatusChangedCallBack) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeStrongBinder(iMSDPDeviceStatusChangedCallBack != null ? iMSDPDeviceStatusChangedCallBack.asBinder() : null);
                    boolean z = false;
                    this.mRemote.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        z = true;
                    }
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.msdp.devicestatus.IMSDPDeviceStatusService
            public boolean enableDeviceStatusService(String str, String str2, int i, long j) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    obtain.writeInt(i);
                    obtain.writeLong(j);
                    boolean z = false;
                    this.mRemote.transact(4, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        z = true;
                    }
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.msdp.devicestatus.IMSDPDeviceStatusService
            public boolean disableDeviceStatusService(String str, String str2, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    obtain.writeInt(i);
                    boolean z = false;
                    this.mRemote.transact(5, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        z = true;
                    }
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.msdp.devicestatus.IMSDPDeviceStatusService
            public HwMSDPDeviceStatusChangeEvent getCurrentDeviceStatus(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    this.mRemote.transact(6, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt() != 0 ? HwMSDPDeviceStatusChangeEvent.CREATOR.createFromParcel(obtain2) : null;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }
    }
}
