package ohos.hiaivision.common;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import ohos.hiaivision.common.IHiAIVisionCallback;

public interface IHiAIVisionEngine extends IInterface {
    Bundle getAbility() throws RemoteException;

    int prepare() throws RemoteException;

    Bundle process(Bundle bundle) throws RemoteException;

    int release() throws RemoteException;

    void run(Bundle bundle, IHiAIVisionCallback iHiAIVisionCallback) throws RemoteException;

    void setClientBinder(IBinder iBinder) throws RemoteException;

    void stop(Bundle bundle, IHiAIVisionCallback iHiAIVisionCallback) throws RemoteException;

    public static abstract class Stub extends Binder implements IHiAIVisionEngine {
        private static final String DESCRIPTOR = "com.huawei.hiai.vision.common.IHiAIVisionEngine";
        static final int TRANSACTION_GET_ABILITY = 3;
        static final int TRANSACTION_PREPARE = 1;
        static final int TRANSACTION_PROCESS = 7;
        static final int TRANSACTION_RELEASE = 2;
        static final int TRANSACTION_RUN = 5;
        static final int TRANSACTION_SET_CLIENT_BINDER = 4;
        static final int TRANSACTION_STOP = 6;

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHiAIVisionEngine asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface instanceof IHiAIVisionEngine) {
                return (IHiAIVisionEngine) queryLocalInterface;
            }
            return new Proxy(iBinder);
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i != 1598968902) {
                Bundle bundle = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        int prepare = prepare();
                        parcel2.writeNoException();
                        parcel2.writeInt(prepare);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        int release = release();
                        parcel2.writeNoException();
                        parcel2.writeInt(release);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        Bundle ability = getAbility();
                        parcel2.writeNoException();
                        if (ability != null) {
                            parcel2.writeInt(1);
                            ability.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        setClientBinder(parcel.readStrongBinder());
                        parcel2.writeNoException();
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            bundle = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        run(bundle, IHiAIVisionCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            bundle = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        stop(bundle, IHiAIVisionCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            bundle = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        Bundle process = process(bundle);
                        parcel2.writeNoException();
                        if (process != null) {
                            parcel2.writeInt(1);
                            process.writeToParcel(parcel2, 1);
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

        private static class Proxy implements IHiAIVisionEngine {
            private IBinder remote;

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            Proxy(IBinder iBinder) {
                this.remote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.remote;
            }

            @Override // ohos.hiaivision.common.IHiAIVisionEngine
            public int prepare() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.remote.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.hiaivision.common.IHiAIVisionEngine
            public int release() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.remote.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.hiaivision.common.IHiAIVisionEngine
            public Bundle getAbility() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.remote.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(obtain2) : null;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.hiaivision.common.IHiAIVisionEngine
            public void setClientBinder(IBinder iBinder) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeStrongBinder(iBinder);
                    this.remote.transact(4, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.hiaivision.common.IHiAIVisionEngine
            public void run(Bundle bundle, IHiAIVisionCallback iHiAIVisionCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (bundle != null) {
                        obtain.writeInt(1);
                        bundle.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iHiAIVisionCallback != null ? iHiAIVisionCallback.asBinder() : null);
                    this.remote.transact(5, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.hiaivision.common.IHiAIVisionEngine
            public void stop(Bundle bundle, IHiAIVisionCallback iHiAIVisionCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (bundle != null) {
                        obtain.writeInt(1);
                        bundle.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iHiAIVisionCallback != null ? iHiAIVisionCallback.asBinder() : null);
                    this.remote.transact(6, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // ohos.hiaivision.common.IHiAIVisionEngine
            public Bundle process(Bundle bundle) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (bundle != null) {
                        obtain.writeInt(1);
                        bundle.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.remote.transact(7, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt() != 0 ? (Bundle) Bundle.CREATOR.createFromParcel(obtain2) : null;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }
    }
}
