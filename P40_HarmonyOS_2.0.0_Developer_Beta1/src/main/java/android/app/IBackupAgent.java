package android.app;

import android.app.backup.IBackupCallback;
import android.app.backup.IBackupManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

public interface IBackupAgent extends IInterface {
    void doBackup(ParcelFileDescriptor parcelFileDescriptor, ParcelFileDescriptor parcelFileDescriptor2, ParcelFileDescriptor parcelFileDescriptor3, long j, IBackupCallback iBackupCallback, int i) throws RemoteException;

    void doFullBackup(ParcelFileDescriptor parcelFileDescriptor, long j, int i, IBackupManager iBackupManager, int i2) throws RemoteException;

    void doMeasureFullBackup(long j, int i, IBackupManager iBackupManager, int i2) throws RemoteException;

    void doQuotaExceeded(long j, long j2, IBackupCallback iBackupCallback) throws RemoteException;

    void doRestore(ParcelFileDescriptor parcelFileDescriptor, long j, ParcelFileDescriptor parcelFileDescriptor2, int i, IBackupManager iBackupManager) throws RemoteException;

    void doRestoreFile(ParcelFileDescriptor parcelFileDescriptor, long j, int i, String str, String str2, long j2, long j3, int i2, IBackupManager iBackupManager) throws RemoteException;

    void doRestoreFinished(int i, IBackupManager iBackupManager) throws RemoteException;

    void fail(String str) throws RemoteException;

    public static class Default implements IBackupAgent {
        @Override // android.app.IBackupAgent
        public void doBackup(ParcelFileDescriptor oldState, ParcelFileDescriptor data, ParcelFileDescriptor newState, long quotaBytes, IBackupCallback callbackBinder, int transportFlags) throws RemoteException {
        }

        @Override // android.app.IBackupAgent
        public void doRestore(ParcelFileDescriptor data, long appVersionCode, ParcelFileDescriptor newState, int token, IBackupManager callbackBinder) throws RemoteException {
        }

        @Override // android.app.IBackupAgent
        public void doFullBackup(ParcelFileDescriptor data, long quotaBytes, int token, IBackupManager callbackBinder, int transportFlags) throws RemoteException {
        }

        @Override // android.app.IBackupAgent
        public void doMeasureFullBackup(long quotaBytes, int token, IBackupManager callbackBinder, int transportFlags) throws RemoteException {
        }

        @Override // android.app.IBackupAgent
        public void doQuotaExceeded(long backupDataBytes, long quotaBytes, IBackupCallback callbackBinder) throws RemoteException {
        }

        @Override // android.app.IBackupAgent
        public void doRestoreFile(ParcelFileDescriptor data, long size, int type, String domain, String path, long mode, long mtime, int token, IBackupManager callbackBinder) throws RemoteException {
        }

        @Override // android.app.IBackupAgent
        public void doRestoreFinished(int token, IBackupManager callbackBinder) throws RemoteException {
        }

        @Override // android.app.IBackupAgent
        public void fail(String message) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "doBackup";
                case 2:
                    return "doRestore";
                case 3:
                    return "doFullBackup";
                case 4:
                    return "doMeasureFullBackup";
                case 5:
                    return "doQuotaExceeded";
                case 6:
                    return "doRestoreFile";
                case 7:
                    return "doRestoreFinished";
                case 8:
                    return "fail";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ParcelFileDescriptor _arg0;
            ParcelFileDescriptor _arg1;
            ParcelFileDescriptor _arg2;
            ParcelFileDescriptor _arg02;
            ParcelFileDescriptor _arg22;
            ParcelFileDescriptor _arg03;
            ParcelFileDescriptor _arg04;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        doBackup(_arg0, _arg1, _arg2, data.readLong(), IBackupCallback.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        long _arg12 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg22 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        doRestore(_arg02, _arg12, _arg22, data.readInt(), IBackupManager.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        doFullBackup(_arg03, data.readLong(), data.readInt(), IBackupManager.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        doMeasureFullBackup(data.readLong(), data.readInt(), IBackupManager.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        doQuotaExceeded(data.readLong(), data.readLong(), IBackupCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        doRestoreFile(_arg04, data.readLong(), data.readInt(), data.readString(), data.readString(), data.readLong(), data.readLong(), data.readInt(), IBackupManager.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        doRestoreFinished(data.readInt(), IBackupManager.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        fail(data.readString());
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IBackupAgent {
            public static IBackupAgent sDefaultImpl;
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

            @Override // android.app.IBackupAgent
            public void doBackup(ParcelFileDescriptor oldState, ParcelFileDescriptor data, ParcelFileDescriptor newState, long quotaBytes, IBackupCallback callbackBinder, int transportFlags) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (oldState != null) {
                        _data.writeInt(1);
                        oldState.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (newState != null) {
                        _data.writeInt(1);
                        newState.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    try {
                        _data.writeLong(quotaBytes);
                        _data.writeStrongBinder(callbackBinder != null ? callbackBinder.asBinder() : null);
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(transportFlags);
                        if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().doBackup(oldState, data, newState, quotaBytes, callbackBinder, transportFlags);
                        _data.recycle();
                    } catch (Throwable th3) {
                        th = th3;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IBackupAgent
            public void doRestore(ParcelFileDescriptor data, long appVersionCode, ParcelFileDescriptor newState, int token, IBackupManager callbackBinder) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    try {
                        _data.writeLong(appVersionCode);
                        if (newState != null) {
                            _data.writeInt(1);
                            newState.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeInt(token);
                            _data.writeStrongBinder(callbackBinder != null ? callbackBinder.asBinder() : null);
                        } catch (Throwable th2) {
                            th = th2;
                            _data.recycle();
                            throw th;
                        }
                        try {
                            if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().doRestore(data, appVersionCode, newState, token, callbackBinder);
                            _data.recycle();
                        } catch (Throwable th3) {
                            th = th3;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IBackupAgent
            public void doFullBackup(ParcelFileDescriptor data, long quotaBytes, int token, IBackupManager callbackBinder, int transportFlags) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    try {
                        _data.writeLong(quotaBytes);
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(token);
                        _data.writeStrongBinder(callbackBinder != null ? callbackBinder.asBinder() : null);
                        try {
                            _data.writeInt(transportFlags);
                            try {
                                if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                    _data.recycle();
                                    return;
                                }
                                Stub.getDefaultImpl().doFullBackup(data, quotaBytes, token, callbackBinder, transportFlags);
                                _data.recycle();
                            } catch (Throwable th3) {
                                th = th3;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IBackupAgent
            public void doMeasureFullBackup(long quotaBytes, int token, IBackupManager callbackBinder, int transportFlags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(quotaBytes);
                    _data.writeInt(token);
                    _data.writeStrongBinder(callbackBinder != null ? callbackBinder.asBinder() : null);
                    _data.writeInt(transportFlags);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().doMeasureFullBackup(quotaBytes, token, callbackBinder, transportFlags);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IBackupAgent
            public void doQuotaExceeded(long backupDataBytes, long quotaBytes, IBackupCallback callbackBinder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(backupDataBytes);
                    _data.writeLong(quotaBytes);
                    _data.writeStrongBinder(callbackBinder != null ? callbackBinder.asBinder() : null);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().doQuotaExceeded(backupDataBytes, quotaBytes, callbackBinder);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IBackupAgent
            public void doRestoreFile(ParcelFileDescriptor data, long size, int type, String domain, String path, long mode, long mtime, int token, IBackupManager callbackBinder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (data != null) {
                        _data.writeInt(1);
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
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().doRestoreFile(data, size, type, domain, path, mode, mtime, token, callbackBinder);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IBackupAgent
            public void doRestoreFinished(int token, IBackupManager callbackBinder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(token);
                    _data.writeStrongBinder(callbackBinder != null ? callbackBinder.asBinder() : null);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().doRestoreFinished(token, callbackBinder);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IBackupAgent
            public void fail(String message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(message);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().fail(message);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IBackupAgent impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IBackupAgent getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
