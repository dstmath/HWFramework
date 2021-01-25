package android.app.backup;

import android.app.backup.IBackupManagerMonitor;
import android.app.backup.IRestoreObserver;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IRestoreSession extends IInterface {
    void endRestoreSession() throws RemoteException;

    int getAvailableRestoreSets(IRestoreObserver iRestoreObserver, IBackupManagerMonitor iBackupManagerMonitor) throws RemoteException;

    int restoreAll(long j, IRestoreObserver iRestoreObserver, IBackupManagerMonitor iBackupManagerMonitor) throws RemoteException;

    int restorePackage(String str, IRestoreObserver iRestoreObserver, IBackupManagerMonitor iBackupManagerMonitor) throws RemoteException;

    int restorePackages(long j, IRestoreObserver iRestoreObserver, String[] strArr, IBackupManagerMonitor iBackupManagerMonitor) throws RemoteException;

    public static class Default implements IRestoreSession {
        @Override // android.app.backup.IRestoreSession
        public int getAvailableRestoreSets(IRestoreObserver observer, IBackupManagerMonitor monitor) throws RemoteException {
            return 0;
        }

        @Override // android.app.backup.IRestoreSession
        public int restoreAll(long token, IRestoreObserver observer, IBackupManagerMonitor monitor) throws RemoteException {
            return 0;
        }

        @Override // android.app.backup.IRestoreSession
        public int restorePackages(long token, IRestoreObserver observer, String[] packages, IBackupManagerMonitor monitor) throws RemoteException {
            return 0;
        }

        @Override // android.app.backup.IRestoreSession
        public int restorePackage(String packageName, IRestoreObserver observer, IBackupManagerMonitor monitor) throws RemoteException {
            return 0;
        }

        @Override // android.app.backup.IRestoreSession
        public void endRestoreSession() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IRestoreSession {
        private static final String DESCRIPTOR = "android.app.backup.IRestoreSession";
        static final int TRANSACTION_endRestoreSession = 5;
        static final int TRANSACTION_getAvailableRestoreSets = 1;
        static final int TRANSACTION_restoreAll = 2;
        static final int TRANSACTION_restorePackage = 4;
        static final int TRANSACTION_restorePackages = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRestoreSession asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IRestoreSession)) {
                return new Proxy(obj);
            }
            return (IRestoreSession) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "getAvailableRestoreSets";
            }
            if (transactionCode == 2) {
                return "restoreAll";
            }
            if (transactionCode == 3) {
                return "restorePackages";
            }
            if (transactionCode == 4) {
                return "restorePackage";
            }
            if (transactionCode != 5) {
                return null;
            }
            return "endRestoreSession";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _result = getAvailableRestoreSets(IRestoreObserver.Stub.asInterface(data.readStrongBinder()), IBackupManagerMonitor.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _result2 = restoreAll(data.readLong(), IRestoreObserver.Stub.asInterface(data.readStrongBinder()), IBackupManagerMonitor.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int _result3 = restorePackages(data.readLong(), IRestoreObserver.Stub.asInterface(data.readStrongBinder()), data.createStringArray(), IBackupManagerMonitor.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(_result3);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                int _result4 = restorePackage(data.readString(), IRestoreObserver.Stub.asInterface(data.readStrongBinder()), IBackupManagerMonitor.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(_result4);
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                endRestoreSession();
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IRestoreSession {
            public static IRestoreSession sDefaultImpl;
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

            @Override // android.app.backup.IRestoreSession
            public int getAvailableRestoreSets(IRestoreObserver observer, IBackupManagerMonitor monitor) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (monitor != null) {
                        iBinder = monitor.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAvailableRestoreSets(observer, monitor);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.backup.IRestoreSession
            public int restoreAll(long token, IRestoreObserver observer, IBackupManagerMonitor monitor) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(token);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (monitor != null) {
                        iBinder = monitor.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().restoreAll(token, observer, monitor);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.backup.IRestoreSession
            public int restorePackages(long token, IRestoreObserver observer, String[] packages, IBackupManagerMonitor monitor) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(token);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    _data.writeStringArray(packages);
                    if (monitor != null) {
                        iBinder = monitor.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().restorePackages(token, observer, packages, monitor);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.backup.IRestoreSession
            public int restorePackage(String packageName, IRestoreObserver observer, IBackupManagerMonitor monitor) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    IBinder iBinder = null;
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (monitor != null) {
                        iBinder = monitor.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().restorePackage(packageName, observer, monitor);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.backup.IRestoreSession
            public void endRestoreSession() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().endRestoreSession();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IRestoreSession impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IRestoreSession getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
