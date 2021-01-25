package android.service.persistentdata;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPersistentDataBlockService extends IInterface {
    int getDataBlockSize() throws RemoteException;

    int getFlashLockState() throws RemoteException;

    long getMaximumDataBlockSize() throws RemoteException;

    boolean getOemUnlockEnabled() throws RemoteException;

    boolean hasFrpCredentialHandle() throws RemoteException;

    byte[] read() throws RemoteException;

    void setOemUnlockEnabled(boolean z) throws RemoteException;

    void wipe() throws RemoteException;

    int write(byte[] bArr) throws RemoteException;

    public static class Default implements IPersistentDataBlockService {
        @Override // android.service.persistentdata.IPersistentDataBlockService
        public int write(byte[] data) throws RemoteException {
            return 0;
        }

        @Override // android.service.persistentdata.IPersistentDataBlockService
        public byte[] read() throws RemoteException {
            return null;
        }

        @Override // android.service.persistentdata.IPersistentDataBlockService
        public void wipe() throws RemoteException {
        }

        @Override // android.service.persistentdata.IPersistentDataBlockService
        public int getDataBlockSize() throws RemoteException {
            return 0;
        }

        @Override // android.service.persistentdata.IPersistentDataBlockService
        public long getMaximumDataBlockSize() throws RemoteException {
            return 0;
        }

        @Override // android.service.persistentdata.IPersistentDataBlockService
        public void setOemUnlockEnabled(boolean enabled) throws RemoteException {
        }

        @Override // android.service.persistentdata.IPersistentDataBlockService
        public boolean getOemUnlockEnabled() throws RemoteException {
            return false;
        }

        @Override // android.service.persistentdata.IPersistentDataBlockService
        public int getFlashLockState() throws RemoteException {
            return 0;
        }

        @Override // android.service.persistentdata.IPersistentDataBlockService
        public boolean hasFrpCredentialHandle() throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IPersistentDataBlockService {
        private static final String DESCRIPTOR = "android.service.persistentdata.IPersistentDataBlockService";
        static final int TRANSACTION_getDataBlockSize = 4;
        static final int TRANSACTION_getFlashLockState = 8;
        static final int TRANSACTION_getMaximumDataBlockSize = 5;
        static final int TRANSACTION_getOemUnlockEnabled = 7;
        static final int TRANSACTION_hasFrpCredentialHandle = 9;
        static final int TRANSACTION_read = 2;
        static final int TRANSACTION_setOemUnlockEnabled = 6;
        static final int TRANSACTION_wipe = 3;
        static final int TRANSACTION_write = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPersistentDataBlockService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPersistentDataBlockService)) {
                return new Proxy(obj);
            }
            return (IPersistentDataBlockService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "write";
                case 2:
                    return "read";
                case 3:
                    return "wipe";
                case 4:
                    return "getDataBlockSize";
                case 5:
                    return "getMaximumDataBlockSize";
                case 6:
                    return "setOemUnlockEnabled";
                case 7:
                    return "getOemUnlockEnabled";
                case 8:
                    return "getFlashLockState";
                case 9:
                    return "hasFrpCredentialHandle";
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
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = write(data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result2 = read();
                        reply.writeNoException();
                        reply.writeByteArray(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        wipe();
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = getDataBlockSize();
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        long _result4 = getMaximumDataBlockSize();
                        reply.writeNoException();
                        reply.writeLong(_result4);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        setOemUnlockEnabled(data.readInt() != 0);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean oemUnlockEnabled = getOemUnlockEnabled();
                        reply.writeNoException();
                        reply.writeInt(oemUnlockEnabled ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = getFlashLockState();
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasFrpCredentialHandle = hasFrpCredentialHandle();
                        reply.writeNoException();
                        reply.writeInt(hasFrpCredentialHandle ? 1 : 0);
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
        public static class Proxy implements IPersistentDataBlockService {
            public static IPersistentDataBlockService sDefaultImpl;
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

            @Override // android.service.persistentdata.IPersistentDataBlockService
            public int write(byte[] data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(data);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().write(data);
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

            @Override // android.service.persistentdata.IPersistentDataBlockService
            public byte[] read() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().read();
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.service.persistentdata.IPersistentDataBlockService
            public void wipe() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().wipe();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.service.persistentdata.IPersistentDataBlockService
            public int getDataBlockSize() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDataBlockSize();
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

            @Override // android.service.persistentdata.IPersistentDataBlockService
            public long getMaximumDataBlockSize() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMaximumDataBlockSize();
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.service.persistentdata.IPersistentDataBlockService
            public void setOemUnlockEnabled(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setOemUnlockEnabled(enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.service.persistentdata.IPersistentDataBlockService
            public boolean getOemUnlockEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOemUnlockEnabled();
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

            @Override // android.service.persistentdata.IPersistentDataBlockService
            public int getFlashLockState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFlashLockState();
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

            @Override // android.service.persistentdata.IPersistentDataBlockService
            public boolean hasFrpCredentialHandle() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasFrpCredentialHandle();
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

        public static boolean setDefaultImpl(IPersistentDataBlockService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IPersistentDataBlockService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
