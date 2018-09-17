package android.app.backup;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IRestoreObserver extends IInterface {

    public static abstract class Stub extends Binder implements IRestoreObserver {
        private static final String DESCRIPTOR = "android.app.backup.IRestoreObserver";
        static final int TRANSACTION_onUpdate = 3;
        static final int TRANSACTION_restoreFinished = 4;
        static final int TRANSACTION_restoreSetsAvailable = 1;
        static final int TRANSACTION_restoreStarting = 2;

        private static class Proxy implements IRestoreObserver {
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

            public void restoreSetsAvailable(RestoreSet[] result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(result, 0);
                    this.mRemote.transact(Stub.TRANSACTION_restoreSetsAvailable, _data, null, Stub.TRANSACTION_restoreSetsAvailable);
                } finally {
                    _data.recycle();
                }
            }

            public void restoreStarting(int numPackages) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(numPackages);
                    this.mRemote.transact(Stub.TRANSACTION_restoreStarting, _data, null, Stub.TRANSACTION_restoreSetsAvailable);
                } finally {
                    _data.recycle();
                }
            }

            public void onUpdate(int nowBeingRestored, String curentPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(nowBeingRestored);
                    _data.writeString(curentPackage);
                    this.mRemote.transact(Stub.TRANSACTION_onUpdate, _data, null, Stub.TRANSACTION_restoreSetsAvailable);
                } finally {
                    _data.recycle();
                }
            }

            public void restoreFinished(int error) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(error);
                    this.mRemote.transact(Stub.TRANSACTION_restoreFinished, _data, null, Stub.TRANSACTION_restoreSetsAvailable);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRestoreObserver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IRestoreObserver)) {
                return new Proxy(obj);
            }
            return (IRestoreObserver) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_restoreSetsAvailable /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    restoreSetsAvailable((RestoreSet[]) data.createTypedArray(RestoreSet.CREATOR));
                    return true;
                case TRANSACTION_restoreStarting /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    restoreStarting(data.readInt());
                    return true;
                case TRANSACTION_onUpdate /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    onUpdate(data.readInt(), data.readString());
                    return true;
                case TRANSACTION_restoreFinished /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    restoreFinished(data.readInt());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onUpdate(int i, String str) throws RemoteException;

    void restoreFinished(int i) throws RemoteException;

    void restoreSetsAvailable(RestoreSet[] restoreSetArr) throws RemoteException;

    void restoreStarting(int i) throws RemoteException;
}
