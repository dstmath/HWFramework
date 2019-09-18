package android.app.backup;

import android.app.backup.IBackupManagerMonitor;
import android.app.backup.IRestoreObserver;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IRestoreSession extends IInterface {

    public static abstract class Stub extends Binder implements IRestoreSession {
        private static final String DESCRIPTOR = "android.app.backup.IRestoreSession";
        static final int TRANSACTION_endRestoreSession = 5;
        static final int TRANSACTION_getAvailableRestoreSets = 1;
        static final int TRANSACTION_restoreAll = 2;
        static final int TRANSACTION_restorePackage = 4;
        static final int TRANSACTION_restoreSome = 3;

        private static class Proxy implements IRestoreSession {
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
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int restoreSome(long token, IRestoreObserver observer, IBackupManagerMonitor monitor, String[] packages) throws RemoteException {
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
                    _data.writeStringArray(packages);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void endRestoreSession() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result = getAvailableRestoreSets(IRestoreObserver.Stub.asInterface(data.readStrongBinder()), IBackupManagerMonitor.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result2 = restoreAll(data.readLong(), IRestoreObserver.Stub.asInterface(data.readStrongBinder()), IBackupManagerMonitor.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result2);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result3 = restoreSome(data.readLong(), IRestoreObserver.Stub.asInterface(data.readStrongBinder()), IBackupManagerMonitor.Stub.asInterface(data.readStrongBinder()), data.createStringArray());
                        reply.writeNoException();
                        parcel2.writeInt(_result3);
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result4 = restorePackage(data.readString(), IRestoreObserver.Stub.asInterface(data.readStrongBinder()), IBackupManagerMonitor.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result4);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        endRestoreSession();
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void endRestoreSession() throws RemoteException;

    int getAvailableRestoreSets(IRestoreObserver iRestoreObserver, IBackupManagerMonitor iBackupManagerMonitor) throws RemoteException;

    int restoreAll(long j, IRestoreObserver iRestoreObserver, IBackupManagerMonitor iBackupManagerMonitor) throws RemoteException;

    int restorePackage(String str, IRestoreObserver iRestoreObserver, IBackupManagerMonitor iBackupManagerMonitor) throws RemoteException;

    int restoreSome(long j, IRestoreObserver iRestoreObserver, IBackupManagerMonitor iBackupManagerMonitor, String[] strArr) throws RemoteException;
}
