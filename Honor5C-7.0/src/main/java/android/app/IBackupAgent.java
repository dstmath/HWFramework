package android.app;

import android.app.backup.IBackupManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

public interface IBackupAgent extends IInterface {

    public static abstract class Stub extends Binder implements IBackupAgent {
        private static final String DESCRIPTOR = "android.app.IBackupAgent";
        static final int TRANSACTION_doBackup = 1;
        static final int TRANSACTION_doFullBackup = 3;
        static final int TRANSACTION_doMeasureFullBackup = 4;
        static final int TRANSACTION_doQuotaExceeded = 5;
        static final int TRANSACTION_doRestore = 2;
        static final int TRANSACTION_doRestoreFile = 6;
        static final int TRANSACTION_doRestoreFinished = 7;
        static final int TRANSACTION_fail = 8;

        private static class Proxy implements IBackupAgent {
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

            public void doBackup(ParcelFileDescriptor oldState, ParcelFileDescriptor data, ParcelFileDescriptor newState, int token, IBackupManager callbackBinder) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (oldState != null) {
                        _data.writeInt(Stub.TRANSACTION_doBackup);
                        oldState.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (data != null) {
                        _data.writeInt(Stub.TRANSACTION_doBackup);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (newState != null) {
                        _data.writeInt(Stub.TRANSACTION_doBackup);
                        newState.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(token);
                    if (callbackBinder != null) {
                        iBinder = callbackBinder.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_doBackup, _data, null, Stub.TRANSACTION_doBackup);
                } finally {
                    _data.recycle();
                }
            }

            public void doRestore(ParcelFileDescriptor data, int appVersionCode, ParcelFileDescriptor newState, int token, IBackupManager callbackBinder) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (data != null) {
                        _data.writeInt(Stub.TRANSACTION_doBackup);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(appVersionCode);
                    if (newState != null) {
                        _data.writeInt(Stub.TRANSACTION_doBackup);
                        newState.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(token);
                    if (callbackBinder != null) {
                        iBinder = callbackBinder.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_doRestore, _data, null, Stub.TRANSACTION_doBackup);
                } finally {
                    _data.recycle();
                }
            }

            public void doFullBackup(ParcelFileDescriptor data, int token, IBackupManager callbackBinder) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (data != null) {
                        _data.writeInt(Stub.TRANSACTION_doBackup);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(token);
                    if (callbackBinder != null) {
                        iBinder = callbackBinder.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_doFullBackup, _data, null, Stub.TRANSACTION_doBackup);
                } finally {
                    _data.recycle();
                }
            }

            public void doMeasureFullBackup(int token, IBackupManager callbackBinder) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    if (callbackBinder != null) {
                        iBinder = callbackBinder.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_doMeasureFullBackup, _data, null, Stub.TRANSACTION_doBackup);
                } finally {
                    _data.recycle();
                }
            }

            public void doQuotaExceeded(long backupDataBytes, long quotaBytes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(backupDataBytes);
                    _data.writeLong(quotaBytes);
                    this.mRemote.transact(Stub.TRANSACTION_doQuotaExceeded, _data, null, Stub.TRANSACTION_doBackup);
                } finally {
                    _data.recycle();
                }
            }

            public void doRestoreFile(ParcelFileDescriptor data, long size, int type, String domain, String path, long mode, long mtime, int token, IBackupManager callbackBinder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (data != null) {
                        _data.writeInt(Stub.TRANSACTION_doBackup);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(size);
                    _data.writeInt(type);
                    _data.writeString(domain);
                    _data.writeString(path);
                    _data.writeLong(mode);
                    _data.writeLong(mtime);
                    _data.writeInt(token);
                    _data.writeStrongBinder(callbackBinder != null ? callbackBinder.asBinder() : null);
                    this.mRemote.transact(Stub.TRANSACTION_doRestoreFile, _data, null, Stub.TRANSACTION_doBackup);
                } finally {
                    _data.recycle();
                }
            }

            public void doRestoreFinished(int token, IBackupManager callbackBinder) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    if (callbackBinder != null) {
                        iBinder = callbackBinder.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_doRestoreFinished, _data, null, Stub.TRANSACTION_doBackup);
                } finally {
                    _data.recycle();
                }
            }

            public void fail(String message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(message);
                    this.mRemote.transact(Stub.TRANSACTION_fail, _data, null, Stub.TRANSACTION_doBackup);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBackupAgent asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBackupAgent)) {
                return new Proxy(obj);
            }
            return (IBackupAgent) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ParcelFileDescriptor parcelFileDescriptor;
            ParcelFileDescriptor parcelFileDescriptor2;
            switch (code) {
                case TRANSACTION_doBackup /*1*/:
                    ParcelFileDescriptor parcelFileDescriptor3;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        parcelFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                    } else {
                        parcelFileDescriptor = null;
                    }
                    if (data.readInt() != 0) {
                        parcelFileDescriptor3 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                    } else {
                        parcelFileDescriptor3 = null;
                    }
                    if (data.readInt() != 0) {
                        parcelFileDescriptor2 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                    } else {
                        parcelFileDescriptor2 = null;
                    }
                    doBackup(parcelFileDescriptor, parcelFileDescriptor3, parcelFileDescriptor2, data.readInt(), android.app.backup.IBackupManager.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_doRestore /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        parcelFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                    } else {
                        parcelFileDescriptor = null;
                    }
                    int _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        parcelFileDescriptor2 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                    } else {
                        parcelFileDescriptor2 = null;
                    }
                    doRestore(parcelFileDescriptor, _arg1, parcelFileDescriptor2, data.readInt(), android.app.backup.IBackupManager.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_doFullBackup /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        parcelFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                    } else {
                        parcelFileDescriptor = null;
                    }
                    doFullBackup(parcelFileDescriptor, data.readInt(), android.app.backup.IBackupManager.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_doMeasureFullBackup /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    doMeasureFullBackup(data.readInt(), android.app.backup.IBackupManager.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_doQuotaExceeded /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    doQuotaExceeded(data.readLong(), data.readLong());
                    return true;
                case TRANSACTION_doRestoreFile /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        parcelFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                    } else {
                        parcelFileDescriptor = null;
                    }
                    doRestoreFile(parcelFileDescriptor, data.readLong(), data.readInt(), data.readString(), data.readString(), data.readLong(), data.readLong(), data.readInt(), android.app.backup.IBackupManager.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_doRestoreFinished /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    doRestoreFinished(data.readInt(), android.app.backup.IBackupManager.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_fail /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    fail(data.readString());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void doBackup(ParcelFileDescriptor parcelFileDescriptor, ParcelFileDescriptor parcelFileDescriptor2, ParcelFileDescriptor parcelFileDescriptor3, int i, IBackupManager iBackupManager) throws RemoteException;

    void doFullBackup(ParcelFileDescriptor parcelFileDescriptor, int i, IBackupManager iBackupManager) throws RemoteException;

    void doMeasureFullBackup(int i, IBackupManager iBackupManager) throws RemoteException;

    void doQuotaExceeded(long j, long j2) throws RemoteException;

    void doRestore(ParcelFileDescriptor parcelFileDescriptor, int i, ParcelFileDescriptor parcelFileDescriptor2, int i2, IBackupManager iBackupManager) throws RemoteException;

    void doRestoreFile(ParcelFileDescriptor parcelFileDescriptor, long j, int i, String str, String str2, long j2, long j3, int i2, IBackupManager iBackupManager) throws RemoteException;

    void doRestoreFinished(int i, IBackupManager iBackupManager) throws RemoteException;

    void fail(String str) throws RemoteException;
}
