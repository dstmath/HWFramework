package ohos.msdp.movement;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import ohos.msdp.movement.IMSDPMovementStatusChangeCallBack;

public interface IMSDPMovementService extends IInterface {
    boolean disableMovementEvent(int i, String str, String str2, int i2) throws RemoteException;

    boolean enableMovementEvent(int i, String str, String str2, int i2, long j, HwMSDPOtherParameters hwMSDPOtherParameters) throws RemoteException;

    boolean exitEnvironment(String str, String str2, HwMSDPOtherParameters hwMSDPOtherParameters) throws RemoteException;

    boolean flush() throws RemoteException;

    int getARVersion(String str, int i) throws RemoteException;

    HwMSDPMovementChangeEvent getCurrentMovement(int i, String str) throws RemoteException;

    String getServcieVersion() throws RemoteException;

    int getSupportedModule() throws RemoteException;

    String[] getSupportedMovements(int i) throws RemoteException;

    boolean initEnvironment(String str, String str2, HwMSDPOtherParameters hwMSDPOtherParameters) throws RemoteException;

    boolean registerSink(String str, IMSDPMovementStatusChangeCallBack iMSDPMovementStatusChangeCallBack) throws RemoteException;

    boolean unregisterSink(String str, IMSDPMovementStatusChangeCallBack iMSDPMovementStatusChangeCallBack) throws RemoteException;

    public static abstract class Stub extends Binder implements IMSDPMovementService {
        private static final String DESCRIPTOR = "com.huawei.msdp.movement.IMSDPMovementService";
        static final int TRANSACTION_disableMovementEvent = 7;
        static final int TRANSACTION_enableMovementEvent = 6;
        static final int TRANSACTION_exitEnvironment = 11;
        static final int TRANSACTION_flush = 9;
        static final int TRANSACTION_getARVersion = 12;
        static final int TRANSACTION_getCurrentMovement = 8;
        static final int TRANSACTION_getServcieVersion = 1;
        static final int TRANSACTION_getSupportedModule = 2;
        static final int TRANSACTION_getSupportedMovements = 3;
        static final int TRANSACTION_initEnvironment = 10;
        static final int TRANSACTION_registerSink = 4;
        static final int TRANSACTION_unregisterSink = 5;

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMSDPMovementService asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IMSDPMovementService)) {
                return new Proxy(iBinder);
            }
            return (IMSDPMovementService) queryLocalInterface;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i != 1598968902) {
                HwMSDPOtherParameters hwMSDPOtherParameters = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        String servcieVersion = getServcieVersion();
                        parcel2.writeNoException();
                        parcel2.writeString(servcieVersion);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        int supportedModule = getSupportedModule();
                        parcel2.writeNoException();
                        parcel2.writeInt(supportedModule);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        String[] supportedMovements = getSupportedMovements(parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeStringArray(supportedMovements);
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean registerSink = registerSink(parcel.readString(), IMSDPMovementStatusChangeCallBack.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(registerSink ? 1 : 0);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean unregisterSink = unregisterSink(parcel.readString(), IMSDPMovementStatusChangeCallBack.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(unregisterSink ? 1 : 0);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        int readInt = parcel.readInt();
                        String readString = parcel.readString();
                        String readString2 = parcel.readString();
                        int readInt2 = parcel.readInt();
                        long readLong = parcel.readLong();
                        if (parcel.readInt() != 0) {
                            hwMSDPOtherParameters = HwMSDPOtherParameters.CREATOR.createFromParcel(parcel);
                        }
                        boolean enableMovementEvent = enableMovementEvent(readInt, readString, readString2, readInt2, readLong, hwMSDPOtherParameters);
                        parcel2.writeNoException();
                        parcel2.writeInt(enableMovementEvent ? 1 : 0);
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean disableMovementEvent = disableMovementEvent(parcel.readInt(), parcel.readString(), parcel.readString(), parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeInt(disableMovementEvent ? 1 : 0);
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        HwMSDPMovementChangeEvent currentMovement = getCurrentMovement(parcel.readInt(), parcel.readString());
                        parcel2.writeNoException();
                        if (currentMovement != null) {
                            parcel2.writeInt(1);
                            currentMovement.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean flush = flush();
                        parcel2.writeNoException();
                        parcel2.writeInt(flush ? 1 : 0);
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        String readString3 = parcel.readString();
                        String readString4 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            hwMSDPOtherParameters = HwMSDPOtherParameters.CREATOR.createFromParcel(parcel);
                        }
                        boolean initEnvironment = initEnvironment(readString3, readString4, hwMSDPOtherParameters);
                        parcel2.writeNoException();
                        parcel2.writeInt(initEnvironment ? 1 : 0);
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        String readString5 = parcel.readString();
                        String readString6 = parcel.readString();
                        if (parcel.readInt() != 0) {
                            hwMSDPOtherParameters = HwMSDPOtherParameters.CREATOR.createFromParcel(parcel);
                        }
                        boolean exitEnvironment = exitEnvironment(readString5, readString6, hwMSDPOtherParameters);
                        parcel2.writeNoException();
                        parcel2.writeInt(exitEnvironment ? 1 : 0);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        int aRVersion = getARVersion(parcel.readString(), parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeInt(aRVersion);
                        return true;
                    default:
                        return super.onTransact(i, parcel, parcel2, i2);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IMSDPMovementService {
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

            @Override // ohos.msdp.movement.IMSDPMovementService
            public String getServcieVersion() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readString();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.msdp.movement.IMSDPMovementService
            public int getSupportedModule() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.msdp.movement.IMSDPMovementService
            public String[] getSupportedMovements(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    this.mRemote.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.createStringArray();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.msdp.movement.IMSDPMovementService
            public boolean registerSink(String str, IMSDPMovementStatusChangeCallBack iMSDPMovementStatusChangeCallBack) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeStrongBinder(iMSDPMovementStatusChangeCallBack != null ? iMSDPMovementStatusChangeCallBack.asBinder() : null);
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

            @Override // ohos.msdp.movement.IMSDPMovementService
            public boolean unregisterSink(String str, IMSDPMovementStatusChangeCallBack iMSDPMovementStatusChangeCallBack) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeStrongBinder(iMSDPMovementStatusChangeCallBack != null ? iMSDPMovementStatusChangeCallBack.asBinder() : null);
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

            @Override // ohos.msdp.movement.IMSDPMovementService
            public boolean enableMovementEvent(int i, String str, String str2, int i2, long j, HwMSDPOtherParameters hwMSDPOtherParameters) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    obtain.writeInt(i2);
                    obtain.writeLong(j);
                    boolean z = true;
                    if (hwMSDPOtherParameters != null) {
                        obtain.writeInt(1);
                        hwMSDPOtherParameters.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(6, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() == 0) {
                        z = false;
                    }
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.msdp.movement.IMSDPMovementService
            public boolean disableMovementEvent(int i, String str, String str2, int i2) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    obtain.writeInt(i2);
                    boolean z = false;
                    this.mRemote.transact(7, obtain, obtain2, 0);
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

            @Override // ohos.msdp.movement.IMSDPMovementService
            public HwMSDPMovementChangeEvent getCurrentMovement(int i, String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeString(str);
                    this.mRemote.transact(8, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt() != 0 ? HwMSDPMovementChangeEvent.CREATOR.createFromParcel(obtain2) : null;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.msdp.movement.IMSDPMovementService
            public boolean flush() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean z = false;
                    this.mRemote.transact(9, obtain, obtain2, 0);
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

            @Override // ohos.msdp.movement.IMSDPMovementService
            public boolean initEnvironment(String str, String str2, HwMSDPOtherParameters hwMSDPOtherParameters) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    boolean z = true;
                    if (hwMSDPOtherParameters != null) {
                        obtain.writeInt(1);
                        hwMSDPOtherParameters.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(10, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() == 0) {
                        z = false;
                    }
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.msdp.movement.IMSDPMovementService
            public boolean exitEnvironment(String str, String str2, HwMSDPOtherParameters hwMSDPOtherParameters) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    boolean z = true;
                    if (hwMSDPOtherParameters != null) {
                        obtain.writeInt(1);
                        hwMSDPOtherParameters.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(11, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() == 0) {
                        z = false;
                    }
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.msdp.movement.IMSDPMovementService
            public int getARVersion(String str, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    this.mRemote.transact(12, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }
    }
}
