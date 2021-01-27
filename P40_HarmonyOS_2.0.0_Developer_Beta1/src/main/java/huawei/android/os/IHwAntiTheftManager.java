package huawei.android.os;

import android.annotation.SystemApi;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwAntiTheftManager extends IInterface {
    boolean checkRootState() throws RemoteException;

    int getAntiTheftDataBlockSize() throws RemoteException;

    boolean getAntiTheftEnabled() throws RemoteException;

    boolean isAntiTheftSupported() throws RemoteException;

    byte[] readAntiTheftData() throws RemoteException;

    @SystemApi
    byte[] readAntiTheftDataWithType(int i) throws RemoteException;

    int setAntiTheftEnabled(boolean z) throws RemoteException;

    int wipeAntiTheftData() throws RemoteException;

    @SystemApi
    int wipeAntiTheftDataWithType(int i) throws RemoteException;

    int writeAntiTheftData(byte[] bArr) throws RemoteException;

    @SystemApi
    int writeAntiTheftDataWithType(byte[] bArr, int i) throws RemoteException;

    public static class Default implements IHwAntiTheftManager {
        @Override // huawei.android.os.IHwAntiTheftManager
        public byte[] readAntiTheftData() throws RemoteException {
            return null;
        }

        @Override // huawei.android.os.IHwAntiTheftManager
        public int wipeAntiTheftData() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.os.IHwAntiTheftManager
        public int writeAntiTheftData(byte[] writeToNative) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.os.IHwAntiTheftManager
        public int getAntiTheftDataBlockSize() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.os.IHwAntiTheftManager
        public int setAntiTheftEnabled(boolean enable) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.os.IHwAntiTheftManager
        public boolean getAntiTheftEnabled() throws RemoteException {
            return false;
        }

        @Override // huawei.android.os.IHwAntiTheftManager
        public boolean checkRootState() throws RemoteException {
            return false;
        }

        @Override // huawei.android.os.IHwAntiTheftManager
        public boolean isAntiTheftSupported() throws RemoteException {
            return false;
        }

        @Override // huawei.android.os.IHwAntiTheftManager
        public byte[] readAntiTheftDataWithType(int type) throws RemoteException {
            return null;
        }

        @Override // huawei.android.os.IHwAntiTheftManager
        public int wipeAntiTheftDataWithType(int type) throws RemoteException {
            return 0;
        }

        @Override // huawei.android.os.IHwAntiTheftManager
        public int writeAntiTheftDataWithType(byte[] writeToNative, int type) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwAntiTheftManager {
        private static final String DESCRIPTOR = "huawei.android.os.IHwAntiTheftManager";
        static final int TRANSACTION_checkRootState = 7;
        static final int TRANSACTION_getAntiTheftDataBlockSize = 4;
        static final int TRANSACTION_getAntiTheftEnabled = 6;
        static final int TRANSACTION_isAntiTheftSupported = 8;
        static final int TRANSACTION_readAntiTheftData = 1;
        static final int TRANSACTION_readAntiTheftDataWithType = 9;
        static final int TRANSACTION_setAntiTheftEnabled = 5;
        static final int TRANSACTION_wipeAntiTheftData = 2;
        static final int TRANSACTION_wipeAntiTheftDataWithType = 10;
        static final int TRANSACTION_writeAntiTheftData = 3;
        static final int TRANSACTION_writeAntiTheftDataWithType = 11;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwAntiTheftManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwAntiTheftManager)) {
                return new Proxy(obj);
            }
            return (IHwAntiTheftManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result = readAntiTheftData();
                        reply.writeNoException();
                        reply.writeByteArray(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = wipeAntiTheftData();
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = writeAntiTheftData(data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = getAntiTheftDataBlockSize();
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = setAntiTheftEnabled(data.readInt() != 0);
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean antiTheftEnabled = getAntiTheftEnabled();
                        reply.writeNoException();
                        reply.writeInt(antiTheftEnabled ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean checkRootState = checkRootState();
                        reply.writeNoException();
                        reply.writeInt(checkRootState ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isAntiTheftSupported = isAntiTheftSupported();
                        reply.writeNoException();
                        reply.writeInt(isAntiTheftSupported ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result6 = readAntiTheftDataWithType(data.readInt());
                        reply.writeNoException();
                        reply.writeByteArray(_result6);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = wipeAntiTheftDataWithType(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = writeAntiTheftDataWithType(data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result8);
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
        public static class Proxy implements IHwAntiTheftManager {
            public static IHwAntiTheftManager sDefaultImpl;
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

            @Override // huawei.android.os.IHwAntiTheftManager
            public byte[] readAntiTheftData() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().readAntiTheftData();
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

            @Override // huawei.android.os.IHwAntiTheftManager
            public int wipeAntiTheftData() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().wipeAntiTheftData();
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

            @Override // huawei.android.os.IHwAntiTheftManager
            public int writeAntiTheftData(byte[] writeToNative) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(writeToNative);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().writeAntiTheftData(writeToNative);
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

            @Override // huawei.android.os.IHwAntiTheftManager
            public int getAntiTheftDataBlockSize() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAntiTheftDataBlockSize();
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

            @Override // huawei.android.os.IHwAntiTheftManager
            public int setAntiTheftEnabled(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setAntiTheftEnabled(enable);
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

            @Override // huawei.android.os.IHwAntiTheftManager
            public boolean getAntiTheftEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAntiTheftEnabled();
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

            @Override // huawei.android.os.IHwAntiTheftManager
            public boolean checkRootState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkRootState();
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

            @Override // huawei.android.os.IHwAntiTheftManager
            public boolean isAntiTheftSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAntiTheftSupported();
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

            @Override // huawei.android.os.IHwAntiTheftManager
            public byte[] readAntiTheftDataWithType(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().readAntiTheftDataWithType(type);
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

            @Override // huawei.android.os.IHwAntiTheftManager
            public int wipeAntiTheftDataWithType(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().wipeAntiTheftDataWithType(type);
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

            @Override // huawei.android.os.IHwAntiTheftManager
            public int writeAntiTheftDataWithType(byte[] writeToNative, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(writeToNative);
                    _data.writeInt(type);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().writeAntiTheftDataWithType(writeToNative, type);
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
        }

        public static boolean setDefaultImpl(IHwAntiTheftManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwAntiTheftManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
