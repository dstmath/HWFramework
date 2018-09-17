package android.app.backup;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IBackupObserver extends IInterface {

    public static abstract class Stub extends Binder implements IBackupObserver {
        private static final String DESCRIPTOR = "android.app.backup.IBackupObserver";
        static final int TRANSACTION_backupFinished = 3;
        static final int TRANSACTION_onResult = 2;
        static final int TRANSACTION_onUpdate = 1;

        private static class Proxy implements IBackupObserver {
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

            public void onUpdate(String currentPackage, BackupProgress backupProgress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(currentPackage);
                    if (backupProgress != null) {
                        _data.writeInt(Stub.TRANSACTION_onUpdate);
                        backupProgress.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onUpdate, _data, null, Stub.TRANSACTION_onUpdate);
                } finally {
                    _data.recycle();
                }
            }

            public void onResult(String currentPackage, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(currentPackage);
                    _data.writeInt(status);
                    this.mRemote.transact(Stub.TRANSACTION_onResult, _data, null, Stub.TRANSACTION_onUpdate);
                } finally {
                    _data.recycle();
                }
            }

            public void backupFinished(int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    this.mRemote.transact(Stub.TRANSACTION_backupFinished, _data, null, Stub.TRANSACTION_onUpdate);
                } finally {
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_onUpdate /*1*/:
                    BackupProgress backupProgress;
                    data.enforceInterface(DESCRIPTOR);
                    String _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        backupProgress = (BackupProgress) BackupProgress.CREATOR.createFromParcel(data);
                    } else {
                        backupProgress = null;
                    }
                    onUpdate(_arg0, backupProgress);
                    return true;
                case TRANSACTION_onResult /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onResult(data.readString(), data.readInt());
                    return true;
                case TRANSACTION_backupFinished /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    backupFinished(data.readInt());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void backupFinished(int i) throws RemoteException;

    void onResult(String str, int i) throws RemoteException;

    void onUpdate(String str, BackupProgress backupProgress) throws RemoteException;
}
