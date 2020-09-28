package android.service.oemlock;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IOemLockService extends IInterface {
    String getLockName() throws RemoteException;

    boolean isDeviceOemUnlocked() throws RemoteException;

    boolean isOemUnlockAllowed() throws RemoteException;

    boolean isOemUnlockAllowedByCarrier() throws RemoteException;

    boolean isOemUnlockAllowedByUser() throws RemoteException;

    void setOemUnlockAllowedByCarrier(boolean z, byte[] bArr) throws RemoteException;

    void setOemUnlockAllowedByUser(boolean z) throws RemoteException;

    public static class Default implements IOemLockService {
        @Override // android.service.oemlock.IOemLockService
        public String getLockName() throws RemoteException {
            return null;
        }

        @Override // android.service.oemlock.IOemLockService
        public void setOemUnlockAllowedByCarrier(boolean allowed, byte[] signature) throws RemoteException {
        }

        @Override // android.service.oemlock.IOemLockService
        public boolean isOemUnlockAllowedByCarrier() throws RemoteException {
            return false;
        }

        @Override // android.service.oemlock.IOemLockService
        public void setOemUnlockAllowedByUser(boolean allowed) throws RemoteException {
        }

        @Override // android.service.oemlock.IOemLockService
        public boolean isOemUnlockAllowedByUser() throws RemoteException {
            return false;
        }

        @Override // android.service.oemlock.IOemLockService
        public boolean isOemUnlockAllowed() throws RemoteException {
            return false;
        }

        @Override // android.service.oemlock.IOemLockService
        public boolean isDeviceOemUnlocked() throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOemLockService {
        private static final String DESCRIPTOR = "android.service.oemlock.IOemLockService";
        static final int TRANSACTION_getLockName = 1;
        static final int TRANSACTION_isDeviceOemUnlocked = 7;
        static final int TRANSACTION_isOemUnlockAllowed = 6;
        static final int TRANSACTION_isOemUnlockAllowedByCarrier = 3;
        static final int TRANSACTION_isOemUnlockAllowedByUser = 5;
        static final int TRANSACTION_setOemUnlockAllowedByCarrier = 2;
        static final int TRANSACTION_setOemUnlockAllowedByUser = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOemLockService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOemLockService)) {
                return new Proxy(obj);
            }
            return (IOemLockService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getLockName";
                case 2:
                    return "setOemUnlockAllowedByCarrier";
                case 3:
                    return "isOemUnlockAllowedByCarrier";
                case 4:
                    return "setOemUnlockAllowedByUser";
                case 5:
                    return "isOemUnlockAllowedByUser";
                case 6:
                    return "isOemUnlockAllowed";
                case 7:
                    return "isDeviceOemUnlocked";
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
            if (code != 1598968902) {
                boolean _arg0 = false;
                boolean _arg02 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _result = getLockName();
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        setOemUnlockAllowedByCarrier(_arg0, data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isOemUnlockAllowedByCarrier = isOemUnlockAllowedByCarrier();
                        reply.writeNoException();
                        reply.writeInt(isOemUnlockAllowedByCarrier ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        setOemUnlockAllowedByUser(_arg02);
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isOemUnlockAllowedByUser = isOemUnlockAllowedByUser();
                        reply.writeNoException();
                        reply.writeInt(isOemUnlockAllowedByUser ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isOemUnlockAllowed = isOemUnlockAllowed();
                        reply.writeNoException();
                        reply.writeInt(isOemUnlockAllowed ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isDeviceOemUnlocked = isDeviceOemUnlocked();
                        reply.writeNoException();
                        reply.writeInt(isDeviceOemUnlocked ? 1 : 0);
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
        public static class Proxy implements IOemLockService {
            public static IOemLockService sDefaultImpl;
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

            @Override // android.service.oemlock.IOemLockService
            public String getLockName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLockName();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.service.oemlock.IOemLockService
            public void setOemUnlockAllowedByCarrier(boolean allowed, byte[] signature) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(allowed ? 1 : 0);
                    _data.writeByteArray(signature);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setOemUnlockAllowedByCarrier(allowed, signature);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.service.oemlock.IOemLockService
            public boolean isOemUnlockAllowedByCarrier() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isOemUnlockAllowedByCarrier();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.service.oemlock.IOemLockService
            public void setOemUnlockAllowedByUser(boolean allowed) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(allowed ? 1 : 0);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setOemUnlockAllowedByUser(allowed);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.service.oemlock.IOemLockService
            public boolean isOemUnlockAllowedByUser() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isOemUnlockAllowedByUser();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.service.oemlock.IOemLockService
            public boolean isOemUnlockAllowed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isOemUnlockAllowed();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.service.oemlock.IOemLockService
            public boolean isDeviceOemUnlocked() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDeviceOemUnlocked();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IOemLockService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOemLockService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
