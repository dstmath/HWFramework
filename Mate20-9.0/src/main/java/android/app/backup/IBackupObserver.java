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
                        _data.writeInt(1);
                        backupProgress.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onResult(String target, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(target);
                    _data.writeInt(status);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void backupFinished(int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    this.mRemote.transact(3, _data, null, 1);
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
            BackupProgress _arg1;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = BackupProgress.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        onUpdate(_arg0, _arg1);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onResult(data.readString(), data.readInt());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        backupFinished(data.readInt());
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void backupFinished(int i) throws RemoteException;

    void onResult(String str, int i) throws RemoteException;

    void onUpdate(String str, BackupProgress backupProgress) throws RemoteException;
}
