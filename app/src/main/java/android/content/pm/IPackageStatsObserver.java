package android.content.pm;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPackageStatsObserver extends IInterface {

    public static abstract class Stub extends Binder implements IPackageStatsObserver {
        private static final String DESCRIPTOR = "android.content.pm.IPackageStatsObserver";
        static final int TRANSACTION_onGetStatsCompleted = 1;

        private static class Proxy implements IPackageStatsObserver {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
                int i = Stub.TRANSACTION_onGetStatsCompleted;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (pStats != null) {
                        _data.writeInt(Stub.TRANSACTION_onGetStatsCompleted);
                        pStats.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!succeeded) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onGetStatsCompleted, _data, null, Stub.TRANSACTION_onGetStatsCompleted);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPackageStatsObserver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPackageStatsObserver)) {
                return new Proxy(obj);
            }
            return (IPackageStatsObserver) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg1 = false;
            switch (code) {
                case TRANSACTION_onGetStatsCompleted /*1*/:
                    PackageStats packageStats;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        packageStats = (PackageStats) PackageStats.CREATOR.createFromParcel(data);
                    } else {
                        packageStats = null;
                    }
                    if (data.readInt() != 0) {
                        _arg1 = true;
                    }
                    onGetStatsCompleted(packageStats, _arg1);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onGetStatsCompleted(PackageStats packageStats, boolean z) throws RemoteException;
}
