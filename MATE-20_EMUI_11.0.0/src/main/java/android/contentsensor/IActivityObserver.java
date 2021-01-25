package android.contentsensor;

import android.content.ComponentName;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IActivityObserver extends IInterface {
    void activityPaused(int i, int i2, ComponentName componentName) throws RemoteException;

    void activityResumed(int i, int i2, ComponentName componentName) throws RemoteException;

    public static abstract class Stub extends Binder implements IActivityObserver {
        private static final String DESCRIPTOR = "android.contentsensor.IActivityObserver";
        static final int TRANSACTION_activityPaused = 2;
        static final int TRANSACTION_activityResumed = 1;

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IActivityObserver asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IActivityObserver)) {
                return new Proxy(iBinder);
            }
            return (IActivityObserver) queryLocalInterface;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            ComponentName componentName = null;
            if (i == 1) {
                parcel.enforceInterface(DESCRIPTOR);
                int readInt = parcel.readInt();
                int readInt2 = parcel.readInt();
                if (parcel.readInt() != 0) {
                    componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(parcel);
                }
                activityResumed(readInt, readInt2, componentName);
                return true;
            } else if (i == 2) {
                parcel.enforceInterface(DESCRIPTOR);
                int readInt3 = parcel.readInt();
                int readInt4 = parcel.readInt();
                if (parcel.readInt() != 0) {
                    componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(parcel);
                }
                activityPaused(readInt3, readInt4, componentName);
                return true;
            } else if (i != 1598968902) {
                return super.onTransact(i, parcel, parcel2, i2);
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IActivityObserver {
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

            @Override // android.contentsensor.IActivityObserver
            public void activityResumed(int i, int i2, ComponentName componentName) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    if (componentName != null) {
                        obtain.writeInt(1);
                        componentName.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(1, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            @Override // android.contentsensor.IActivityObserver
            public void activityPaused(int i, int i2, ComponentName componentName) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    if (componentName != null) {
                        obtain.writeInt(1);
                        componentName.writeToParcel(obtain, 0);
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
