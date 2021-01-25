package com.android.internal.backup;

import android.app.backup.IBackupManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

public interface IObbBackupService extends IInterface {
    void backupObbs(String str, ParcelFileDescriptor parcelFileDescriptor, int i, IBackupManager iBackupManager) throws RemoteException;

    void restoreObbFile(String str, ParcelFileDescriptor parcelFileDescriptor, long j, int i, String str2, long j2, long j3, int i2, IBackupManager iBackupManager) throws RemoteException;

    public static class Default implements IObbBackupService {
        @Override // com.android.internal.backup.IObbBackupService
        public void backupObbs(String packageName, ParcelFileDescriptor data, int token, IBackupManager callbackBinder) throws RemoteException {
        }

        @Override // com.android.internal.backup.IObbBackupService
        public void restoreObbFile(String pkgName, ParcelFileDescriptor data, long fileSize, int type, String path, long mode, long mtime, int token, IBackupManager callbackBinder) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IObbBackupService {
        private static final String DESCRIPTOR = "com.android.internal.backup.IObbBackupService";
        static final int TRANSACTION_backupObbs = 1;
        static final int TRANSACTION_restoreObbFile = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IObbBackupService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IObbBackupService)) {
                return new Proxy(obj);
            }
            return (IObbBackupService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "backupObbs";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "restoreObbFile";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ParcelFileDescriptor _arg1;
            ParcelFileDescriptor _arg12;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                String _arg0 = data.readString();
                if (data.readInt() != 0) {
                    _arg1 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                backupObbs(_arg0, _arg1, data.readInt(), IBackupManager.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                String _arg02 = data.readString();
                if (data.readInt() != 0) {
                    _arg12 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                } else {
                    _arg12 = null;
                }
                restoreObbFile(_arg02, _arg12, data.readLong(), data.readInt(), data.readString(), data.readLong(), data.readLong(), data.readInt(), IBackupManager.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IObbBackupService {
            public static IObbBackupService sDefaultImpl;
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

            @Override // com.android.internal.backup.IObbBackupService
            public void backupObbs(String packageName, ParcelFileDescriptor data, int token, IBackupManager callbackBinder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(token);
                    _data.writeStrongBinder(callbackBinder != null ? callbackBinder.asBinder() : null);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().backupObbs(packageName, data, token, callbackBinder);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.backup.IObbBackupService
            public void restoreObbFile(String pkgName, ParcelFileDescriptor data, long fileSize, int type, String path, long mode, long mtime, int token, IBackupManager callbackBinder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(fileSize);
                    _data.writeInt(type);
                    _data.writeString(path);
                    _data.writeLong(mode);
                    _data.writeLong(mtime);
                    _data.writeInt(token);
                    _data.writeStrongBinder(callbackBinder != null ? callbackBinder.asBinder() : null);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().restoreObbFile(pkgName, data, fileSize, type, path, mode, mtime, token, callbackBinder);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IObbBackupService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IObbBackupService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
