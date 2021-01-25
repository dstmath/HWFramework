package android.media.tv;

import android.annotation.UnsupportedAppUsage;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ITvRemoteServiceInput extends IInterface {
    @UnsupportedAppUsage
    void clearInputBridge(IBinder iBinder) throws RemoteException;

    @UnsupportedAppUsage
    void closeInputBridge(IBinder iBinder) throws RemoteException;

    @UnsupportedAppUsage
    void openInputBridge(IBinder iBinder, String str, int i, int i2, int i3) throws RemoteException;

    @UnsupportedAppUsage
    void sendKeyDown(IBinder iBinder, int i) throws RemoteException;

    @UnsupportedAppUsage
    void sendKeyUp(IBinder iBinder, int i) throws RemoteException;

    @UnsupportedAppUsage
    void sendPointerDown(IBinder iBinder, int i, int i2, int i3) throws RemoteException;

    @UnsupportedAppUsage
    void sendPointerSync(IBinder iBinder) throws RemoteException;

    @UnsupportedAppUsage
    void sendPointerUp(IBinder iBinder, int i) throws RemoteException;

    @UnsupportedAppUsage
    void sendTimestamp(IBinder iBinder, long j) throws RemoteException;

    public static class Default implements ITvRemoteServiceInput {
        @Override // android.media.tv.ITvRemoteServiceInput
        public void openInputBridge(IBinder token, String name, int width, int height, int maxPointers) throws RemoteException {
        }

        @Override // android.media.tv.ITvRemoteServiceInput
        public void closeInputBridge(IBinder token) throws RemoteException {
        }

        @Override // android.media.tv.ITvRemoteServiceInput
        public void clearInputBridge(IBinder token) throws RemoteException {
        }

        @Override // android.media.tv.ITvRemoteServiceInput
        public void sendTimestamp(IBinder token, long timestamp) throws RemoteException {
        }

        @Override // android.media.tv.ITvRemoteServiceInput
        public void sendKeyDown(IBinder token, int keyCode) throws RemoteException {
        }

        @Override // android.media.tv.ITvRemoteServiceInput
        public void sendKeyUp(IBinder token, int keyCode) throws RemoteException {
        }

        @Override // android.media.tv.ITvRemoteServiceInput
        public void sendPointerDown(IBinder token, int pointerId, int x, int y) throws RemoteException {
        }

        @Override // android.media.tv.ITvRemoteServiceInput
        public void sendPointerUp(IBinder token, int pointerId) throws RemoteException {
        }

        @Override // android.media.tv.ITvRemoteServiceInput
        public void sendPointerSync(IBinder token) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ITvRemoteServiceInput {
        private static final String DESCRIPTOR = "android.media.tv.ITvRemoteServiceInput";
        static final int TRANSACTION_clearInputBridge = 3;
        static final int TRANSACTION_closeInputBridge = 2;
        static final int TRANSACTION_openInputBridge = 1;
        static final int TRANSACTION_sendKeyDown = 5;
        static final int TRANSACTION_sendKeyUp = 6;
        static final int TRANSACTION_sendPointerDown = 7;
        static final int TRANSACTION_sendPointerSync = 9;
        static final int TRANSACTION_sendPointerUp = 8;
        static final int TRANSACTION_sendTimestamp = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITvRemoteServiceInput asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITvRemoteServiceInput)) {
                return new Proxy(obj);
            }
            return (ITvRemoteServiceInput) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "openInputBridge";
                case 2:
                    return "closeInputBridge";
                case 3:
                    return "clearInputBridge";
                case 4:
                    return "sendTimestamp";
                case 5:
                    return "sendKeyDown";
                case 6:
                    return "sendKeyUp";
                case 7:
                    return "sendPointerDown";
                case 8:
                    return "sendPointerUp";
                case 9:
                    return "sendPointerSync";
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
                        openInputBridge(data.readStrongBinder(), data.readString(), data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        closeInputBridge(data.readStrongBinder());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        clearInputBridge(data.readStrongBinder());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        sendTimestamp(data.readStrongBinder(), data.readLong());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        sendKeyDown(data.readStrongBinder(), data.readInt());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        sendKeyUp(data.readStrongBinder(), data.readInt());
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        sendPointerDown(data.readStrongBinder(), data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        sendPointerUp(data.readStrongBinder(), data.readInt());
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        sendPointerSync(data.readStrongBinder());
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
        public static class Proxy implements ITvRemoteServiceInput {
            public static ITvRemoteServiceInput sDefaultImpl;
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

            @Override // android.media.tv.ITvRemoteServiceInput
            public void openInputBridge(IBinder token, String name, int width, int height, int maxPointers) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeString(name);
                    _data.writeInt(width);
                    _data.writeInt(height);
                    _data.writeInt(maxPointers);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().openInputBridge(token, name, width, height, maxPointers);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvRemoteServiceInput
            public void closeInputBridge(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().closeInputBridge(token);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvRemoteServiceInput
            public void clearInputBridge(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().clearInputBridge(token);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvRemoteServiceInput
            public void sendTimestamp(IBinder token, long timestamp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeLong(timestamp);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().sendTimestamp(token, timestamp);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvRemoteServiceInput
            public void sendKeyDown(IBinder token, int keyCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(keyCode);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().sendKeyDown(token, keyCode);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvRemoteServiceInput
            public void sendKeyUp(IBinder token, int keyCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(keyCode);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().sendKeyUp(token, keyCode);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvRemoteServiceInput
            public void sendPointerDown(IBinder token, int pointerId, int x, int y) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(pointerId);
                    _data.writeInt(x);
                    _data.writeInt(y);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().sendPointerDown(token, pointerId, x, y);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvRemoteServiceInput
            public void sendPointerUp(IBinder token, int pointerId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(pointerId);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().sendPointerUp(token, pointerId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.tv.ITvRemoteServiceInput
            public void sendPointerSync(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().sendPointerSync(token);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ITvRemoteServiceInput impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ITvRemoteServiceInput getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
