package com.android.internal.backup;

import android.app.backup.IBackupManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

public interface IObbBackupService extends IInterface {

    public static abstract class Stub extends Binder implements IObbBackupService {
        private static final String DESCRIPTOR = "com.android.internal.backup.IObbBackupService";
        static final int TRANSACTION_backupObbs = 1;
        static final int TRANSACTION_restoreObbFile = 2;

        private static class Proxy implements IObbBackupService {
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
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void restoreObbFile(String pkgName, ParcelFileDescriptor data, long fileSize, int type, String path, long mode, long mtime, int token, IBackupManager callbackBinder) throws RemoteException {
                ParcelFileDescriptor parcelFileDescriptor = data;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (parcelFileDescriptor != null) {
                        _data.writeInt(1);
                        parcelFileDescriptor.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    try {
                        _data.writeLong(fileSize);
                        try {
                            _data.writeInt(type);
                            try {
                                _data.writeString(path);
                            } catch (Throwable th) {
                                th = th;
                                long j = mode;
                                long j2 = mtime;
                                int i = token;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            String str = path;
                            long j3 = mode;
                            long j22 = mtime;
                            int i2 = token;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        int i3 = type;
                        String str2 = path;
                        long j32 = mode;
                        long j222 = mtime;
                        int i22 = token;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(mode);
                        try {
                            _data.writeLong(mtime);
                            try {
                                _data.writeInt(token);
                                _data.writeStrongBinder(callbackBinder != null ? callbackBinder.asBinder() : null);
                            } catch (Throwable th4) {
                                th = th4;
                                _data.recycle();
                                throw th;
                            }
                            try {
                                this.mRemote.transact(2, _data, null, 1);
                                _data.recycle();
                            } catch (Throwable th5) {
                                th = th5;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th6) {
                            th = th6;
                            int i222 = token;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        long j2222 = mtime;
                        int i2222 = token;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    long j4 = fileSize;
                    int i32 = type;
                    String str22 = path;
                    long j322 = mode;
                    long j22222 = mtime;
                    int i22222 = token;
                    _data.recycle();
                    throw th;
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            if (i != 1598968902) {
                ParcelFileDescriptor _arg1 = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(parcel);
                        }
                        backupObbs(_arg0, _arg1, data.readInt(), IBackupManager.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(parcel);
                        }
                        ParcelFileDescriptor _arg12 = _arg1;
                        restoreObbFile(_arg02, _arg12, data.readLong(), data.readInt(), data.readString(), data.readLong(), data.readLong(), data.readInt(), IBackupManager.Stub.asInterface(data.readStrongBinder()));
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

    void backupObbs(String str, ParcelFileDescriptor parcelFileDescriptor, int i, IBackupManager iBackupManager) throws RemoteException;

    void restoreObbFile(String str, ParcelFileDescriptor parcelFileDescriptor, long j, int i, String str2, long j2, long j3, int i2, IBackupManager iBackupManager) throws RemoteException;
}
