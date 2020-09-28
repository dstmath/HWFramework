package android.cover;

import android.cover.IHallCallback;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ICoverManager extends IInterface {
    int getHallState(int i) throws RemoteException;

    boolean isCoverOpen() throws RemoteException;

    boolean registerHallCallback(String str, int i, IHallCallback iHallCallback) throws RemoteException;

    boolean setCoverForbiddened(boolean z) throws RemoteException;

    void setCoverViewBinder(IBinder iBinder) throws RemoteException;

    boolean unRegisterHallCallback(String str, int i) throws RemoteException;

    boolean unRegisterHallCallbackEx(int i, IHallCallback iHallCallback) throws RemoteException;

    public static class Default implements ICoverManager {
        @Override // android.cover.ICoverManager
        public boolean isCoverOpen() throws RemoteException {
            return false;
        }

        @Override // android.cover.ICoverManager
        public boolean setCoverForbiddened(boolean forbiddened) throws RemoteException {
            return false;
        }

        @Override // android.cover.ICoverManager
        public void setCoverViewBinder(IBinder binder) throws RemoteException {
        }

        @Override // android.cover.ICoverManager
        public int getHallState(int hallType) throws RemoteException {
            return 0;
        }

        @Override // android.cover.ICoverManager
        public boolean registerHallCallback(String receiverName, int hallType, IHallCallback callback) throws RemoteException {
            return false;
        }

        @Override // android.cover.ICoverManager
        public boolean unRegisterHallCallback(String receiverName, int hallType) throws RemoteException {
            return false;
        }

        @Override // android.cover.ICoverManager
        public boolean unRegisterHallCallbackEx(int hallType, IHallCallback callback) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ICoverManager {
        private static final String DESCRIPTOR = "android.cover.ICoverManager";
        static final int TRANSACTION_getHallState = 4;
        static final int TRANSACTION_isCoverOpen = 1;
        static final int TRANSACTION_registerHallCallback = 5;
        static final int TRANSACTION_setCoverForbiddened = 2;
        static final int TRANSACTION_setCoverViewBinder = 3;
        static final int TRANSACTION_unRegisterHallCallback = 6;
        static final int TRANSACTION_unRegisterHallCallbackEx = 7;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "isCoverOpen";
                case 2:
                    return "setCoverForbiddened";
                case 3:
                    return "setCoverViewBinder";
                case 4:
                    return "getHallState";
                case 5:
                    return "registerHallCallback";
                case 6:
                    return "unRegisterHallCallback";
                case 7:
                    return "unRegisterHallCallbackEx";
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
                        boolean isCoverOpen = isCoverOpen();
                        reply.writeNoException();
                        reply.writeInt(isCoverOpen ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean coverForbiddened = setCoverForbiddened(data.readInt() != 0);
                        reply.writeNoException();
                        reply.writeInt(coverForbiddened ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        setCoverViewBinder(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getHallState(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerHallCallback = registerHallCallback(data.readString(), data.readInt(), IHallCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerHallCallback ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unRegisterHallCallback = unRegisterHallCallback(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(unRegisterHallCallback ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unRegisterHallCallbackEx = unRegisterHallCallbackEx(data.readInt(), IHallCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(unRegisterHallCallbackEx ? 1 : 0);
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
        public static class Proxy implements ICoverManager {
            public static ICoverManager sDefaultImpl;
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

            @Override // android.cover.ICoverManager
            public boolean isCoverOpen() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isCoverOpen();
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

            @Override // android.cover.ICoverManager
            public boolean setCoverForbiddened(boolean forbiddened) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(forbiddened ? 1 : 0);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setCoverForbiddened(forbiddened);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.cover.ICoverManager
            public void setCoverViewBinder(IBinder binder) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(binder);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCoverViewBinder(binder);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.cover.ICoverManager
            public int getHallState(int hallType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hallType);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHallState(hallType);
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

            @Override // android.cover.ICoverManager
            public boolean registerHallCallback(String receiverName, int hallType, IHallCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(receiverName);
                    _data.writeInt(hallType);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerHallCallback(receiverName, hallType, callback);
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

            @Override // android.cover.ICoverManager
            public boolean unRegisterHallCallback(String receiverName, int hallType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(receiverName);
                    _data.writeInt(hallType);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unRegisterHallCallback(receiverName, hallType);
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

            @Override // android.cover.ICoverManager
            public boolean unRegisterHallCallbackEx(int hallType, IHallCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hallType);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unRegisterHallCallbackEx(hallType, callback);
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

        public static boolean setDefaultImpl(ICoverManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ICoverManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
