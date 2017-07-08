package huawei.android.os;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwAntiTheftManager extends IInterface {

    public static abstract class Stub extends Binder implements IHwAntiTheftManager {
        private static final String DESCRIPTOR = "huawei.android.os.IHwAntiTheftManager";
        static final int TRANSACTION_checkRootState = 7;
        static final int TRANSACTION_getAntiTheftDataBlockSize = 4;
        static final int TRANSACTION_getAntiTheftEnabled = 6;
        static final int TRANSACTION_isAntiTheftSupported = 8;
        static final int TRANSACTION_readAntiTheftData = 1;
        static final int TRANSACTION_setAntiTheftEnabled = 5;
        static final int TRANSACTION_wipeAntiTheftData = 2;
        static final int TRANSACTION_writeAntiTheftData = 3;

        private static class Proxy implements IHwAntiTheftManager {
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

            public byte[] readAntiTheftData() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_readAntiTheftData, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int wipeAntiTheftData() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_wipeAntiTheftData, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int writeAntiTheftData(byte[] writeToNative) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(writeToNative);
                    this.mRemote.transact(Stub.TRANSACTION_writeAntiTheftData, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAntiTheftDataBlockSize() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getAntiTheftDataBlockSize, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setAntiTheftEnabled(boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enable) {
                        i = Stub.TRANSACTION_readAntiTheftData;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setAntiTheftEnabled, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getAntiTheftEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getAntiTheftEnabled, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean checkRootState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_checkRootState, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isAntiTheftSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isAntiTheftSupported, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _arg0 = 0;
            int _result;
            boolean _result2;
            switch (code) {
                case TRANSACTION_readAntiTheftData /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    byte[] _result3 = readAntiTheftData();
                    reply.writeNoException();
                    reply.writeByteArray(_result3);
                    return true;
                case TRANSACTION_wipeAntiTheftData /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = wipeAntiTheftData();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_writeAntiTheftData /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = writeAntiTheftData(data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getAntiTheftDataBlockSize /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getAntiTheftDataBlockSize();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_setAntiTheftEnabled /*5*/:
                    boolean _arg02;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = true;
                    }
                    _result = setAntiTheftEnabled(_arg02);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getAntiTheftEnabled /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getAntiTheftEnabled();
                    reply.writeNoException();
                    if (_result2) {
                        _arg0 = TRANSACTION_readAntiTheftData;
                    }
                    reply.writeInt(_arg0);
                    return true;
                case TRANSACTION_checkRootState /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = checkRootState();
                    reply.writeNoException();
                    if (_result2) {
                        _arg0 = TRANSACTION_readAntiTheftData;
                    }
                    reply.writeInt(_arg0);
                    return true;
                case TRANSACTION_isAntiTheftSupported /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = isAntiTheftSupported();
                    reply.writeNoException();
                    if (_result2) {
                        _arg0 = TRANSACTION_readAntiTheftData;
                    }
                    reply.writeInt(_arg0);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean checkRootState() throws RemoteException;

    int getAntiTheftDataBlockSize() throws RemoteException;

    boolean getAntiTheftEnabled() throws RemoteException;

    boolean isAntiTheftSupported() throws RemoteException;

    byte[] readAntiTheftData() throws RemoteException;

    int setAntiTheftEnabled(boolean z) throws RemoteException;

    int wipeAntiTheftData() throws RemoteException;

    int writeAntiTheftData(byte[] bArr) throws RemoteException;
}
