package android.cover;

import android.cover.IHallCallback;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ICoverManager extends IInterface {

    public static abstract class Stub extends Binder implements ICoverManager {
        private static final String DESCRIPTOR = "android.cover.ICoverManager";
        static final int TRANSACTION_getHallState = 4;
        static final int TRANSACTION_isCoverOpen = 1;
        static final int TRANSACTION_registerHallCallback = 5;
        static final int TRANSACTION_setCoverForbiddened = 2;
        static final int TRANSACTION_setCoverViewBinder = 3;
        static final int TRANSACTION_unRegisterHallCallback = 6;
        static final int TRANSACTION_unRegisterHallCallbackEx = 7;

        private static class Proxy implements ICoverManager {
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

            public boolean isCoverOpen() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setCoverForbiddened(boolean forbiddened) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(forbiddened);
                    boolean _result = false;
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setCoverViewBinder(IBinder binder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(binder);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getHallState(int hallType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hallType);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerHallCallback(String receiverName, int hallType, IHallCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(receiverName);
                    _data.writeInt(hallType);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean unRegisterHallCallback(String receiverName, int hallType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(receiverName);
                    _data.writeInt(hallType);
                    boolean _result = false;
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean unRegisterHallCallbackEx(int hallType, IHallCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hallType);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICoverManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICoverManager)) {
                return new Proxy(obj);
            }
            return (ICoverManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result = isCoverOpen();
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result2 = setCoverForbiddened(data.readInt() != 0);
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        setCoverViewBinder(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = getHallState(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result4 = registerHallCallback(data.readString(), data.readInt(), IHallCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result5 = unRegisterHallCallback(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result6 = unRegisterHallCallbackEx(data.readInt(), IHallCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result6);
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

    int getHallState(int i) throws RemoteException;

    boolean isCoverOpen() throws RemoteException;

    boolean registerHallCallback(String str, int i, IHallCallback iHallCallback) throws RemoteException;

    boolean setCoverForbiddened(boolean z) throws RemoteException;

    void setCoverViewBinder(IBinder iBinder) throws RemoteException;

    boolean unRegisterHallCallback(String str, int i) throws RemoteException;

    boolean unRegisterHallCallbackEx(int i, IHallCallback iHallCallback) throws RemoteException;
}
