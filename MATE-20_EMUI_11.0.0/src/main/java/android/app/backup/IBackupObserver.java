package android.app.backup;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IBackupObserver extends IInterface {
    void backupFinished(int i) throws RemoteException;

    void onResult(String str, int i) throws RemoteException;

    void onUpdate(String str, BackupProgress backupProgress) throws RemoteException;

    public static class Default implements IBackupObserver {
        @Override // android.app.backup.IBackupObserver
        public void onUpdate(String currentPackage, BackupProgress backupProgress) throws RemoteException {
        }

        @Override // android.app.backup.IBackupObserver
        public void onResult(String target, int status) throws RemoteException {
        }

        @Override // android.app.backup.IBackupObserver
        public void backupFinished(int status) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IBackupObserver {
        private static final String DESCRIPTOR = "android.app.backup.IBackupObserver";
        static final int TRANSACTION_backupFinished = 3;
        static final int TRANSACTION_onResult = 2;
        static final int TRANSACTION_onUpdate = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBackupObserver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBackupObserver)) {
                return new Proxy(obj);
            }
            return (IBackupObserver) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onUpdate";
            }
            if (transactionCode == 2) {
                return "onResult";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "backupFinished";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            BackupProgress _arg1;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                String _arg0 = data.readString();
                if (data.readInt() != 0) {
                    _arg1 = BackupProgress.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                onUpdate(_arg0, _arg1);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onResult(data.readString(), data.readInt());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                backupFinished(data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IBackupObserver {
            public static IBackupObserver sDefaultImpl;
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

            @Override // android.app.backup.IBackupObserver
            public void onUpdate(String currentPackage, BackupProgress backupProgress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(currentPackage);
                    if (backupProgress != null) {
                        _data.writeInt(1);
                        backupProgress.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onUpdate(currentPackage, backupProgress);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.backup.IBackupObserver
            public void onResult(String target, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(target);
                    _data.writeInt(status);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onResult(target, status);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.backup.IBackupObserver
            public void backupFinished(int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().backupFinished(status);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IBackupObserver impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IBackupObserver getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
