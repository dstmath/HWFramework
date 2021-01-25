package android.app.backup;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IRestoreObserver extends IInterface {
    void onUpdate(int i, String str) throws RemoteException;

    void restoreFinished(int i) throws RemoteException;

    void restoreSetsAvailable(RestoreSet[] restoreSetArr) throws RemoteException;

    void restoreStarting(int i) throws RemoteException;

    public static class Default implements IRestoreObserver {
        @Override // android.app.backup.IRestoreObserver
        public void restoreSetsAvailable(RestoreSet[] result) throws RemoteException {
        }

        @Override // android.app.backup.IRestoreObserver
        public void restoreStarting(int numPackages) throws RemoteException {
        }

        @Override // android.app.backup.IRestoreObserver
        public void onUpdate(int nowBeingRestored, String curentPackage) throws RemoteException {
        }

        @Override // android.app.backup.IRestoreObserver
        public void restoreFinished(int error) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IRestoreObserver {
        private static final String DESCRIPTOR = "android.app.backup.IRestoreObserver";
        static final int TRANSACTION_onUpdate = 3;
        static final int TRANSACTION_restoreFinished = 4;
        static final int TRANSACTION_restoreSetsAvailable = 1;
        static final int TRANSACTION_restoreStarting = 2;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "restoreSetsAvailable";
            }
            if (transactionCode == 2) {
                return "restoreStarting";
            }
            if (transactionCode == 3) {
                return "onUpdate";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "restoreFinished";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                restoreSetsAvailable((RestoreSet[]) data.createTypedArray(RestoreSet.CREATOR));
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                restoreStarting(data.readInt());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onUpdate(data.readInt(), data.readString());
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                restoreFinished(data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IRestoreObserver {
            public static IRestoreObserver sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // android.app.backup.IRestoreObserver
            public void restoreSetsAvailable(RestoreSet[] result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(result, 0);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().restoreSetsAvailable(result);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.backup.IRestoreObserver
            public void restoreStarting(int numPackages) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(numPackages);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().restoreStarting(numPackages);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.backup.IRestoreObserver
            public void onUpdate(int nowBeingRestored, String curentPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(nowBeingRestored);
                    _data.writeString(curentPackage);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onUpdate(nowBeingRestored, curentPackage);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.backup.IRestoreObserver
            public void restoreFinished(int error) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(error);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().restoreFinished(error);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IRestoreObserver impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IRestoreObserver getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
