package android.nfc;

import android.annotation.UnsupportedAppUsage;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INfcAdapterExtras extends IInterface {
    @UnsupportedAppUsage
    void authenticate(String str, byte[] bArr) throws RemoteException;

    @UnsupportedAppUsage
    Bundle close(String str, IBinder iBinder) throws RemoteException;

    @UnsupportedAppUsage
    int getCardEmulationRoute(String str) throws RemoteException;

    @UnsupportedAppUsage
    String getDriverName(String str) throws RemoteException;

    @UnsupportedAppUsage
    boolean notifyCommandFromSecureElement(String str, String str2, int i) throws RemoteException;

    @UnsupportedAppUsage
    Bundle open(String str, IBinder iBinder) throws RemoteException;

    @UnsupportedAppUsage
    void setCardEmulationRoute(String str, int i) throws RemoteException;

    @UnsupportedAppUsage
    Bundle transceive(String str, byte[] bArr) throws RemoteException;

    public static class Default implements INfcAdapterExtras {
        @Override // android.nfc.INfcAdapterExtras
        public Bundle open(String pkg, IBinder b) throws RemoteException {
            return null;
        }

        @Override // android.nfc.INfcAdapterExtras
        public Bundle close(String pkg, IBinder b) throws RemoteException {
            return null;
        }

        @Override // android.nfc.INfcAdapterExtras
        public Bundle transceive(String pkg, byte[] data_in) throws RemoteException {
            return null;
        }

        @Override // android.nfc.INfcAdapterExtras
        public int getCardEmulationRoute(String pkg) throws RemoteException {
            return 0;
        }

        @Override // android.nfc.INfcAdapterExtras
        public void setCardEmulationRoute(String pkg, int route) throws RemoteException {
        }

        @Override // android.nfc.INfcAdapterExtras
        public void authenticate(String pkg, byte[] token) throws RemoteException {
        }

        @Override // android.nfc.INfcAdapterExtras
        public String getDriverName(String pkg) throws RemoteException {
            return null;
        }

        @Override // android.nfc.INfcAdapterExtras
        public boolean notifyCommandFromSecureElement(String pkg, String payload, int function) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INfcAdapterExtras {
        private static final String DESCRIPTOR = "android.nfc.INfcAdapterExtras";
        static final int TRANSACTION_authenticate = 6;
        static final int TRANSACTION_close = 2;
        static final int TRANSACTION_getCardEmulationRoute = 4;
        static final int TRANSACTION_getDriverName = 7;
        static final int TRANSACTION_notifyCommandFromSecureElement = 8;
        static final int TRANSACTION_open = 1;
        static final int TRANSACTION_setCardEmulationRoute = 5;
        static final int TRANSACTION_transceive = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INfcAdapterExtras asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INfcAdapterExtras)) {
                return new Proxy(obj);
            }
            return (INfcAdapterExtras) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "open";
                case 2:
                    return "close";
                case 3:
                    return "transceive";
                case 4:
                    return "getCardEmulationRoute";
                case 5:
                    return "setCardEmulationRoute";
                case 6:
                    return "authenticate";
                case 7:
                    return "getDriverName";
                case 8:
                    return "notifyCommandFromSecureElement";
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
                        Bundle _result = open(data.readString(), data.readStrongBinder());
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result2 = close(data.readString(), data.readStrongBinder());
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result3 = transceive(data.readString(), data.createByteArray());
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = getCardEmulationRoute(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        setCardEmulationRoute(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        authenticate(data.readString(), data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        String _result5 = getDriverName(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result5);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean notifyCommandFromSecureElement = notifyCommandFromSecureElement(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(notifyCommandFromSecureElement ? 1 : 0);
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
        public static class Proxy implements INfcAdapterExtras {
            public static INfcAdapterExtras sDefaultImpl;
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

            @Override // android.nfc.INfcAdapterExtras
            public Bundle open(String pkg, IBinder b) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeStrongBinder(b);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().open(pkg, b);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcAdapterExtras
            public Bundle close(String pkg, IBinder b) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeStrongBinder(b);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().close(pkg, b);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcAdapterExtras
            public Bundle transceive(String pkg, byte[] data_in) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeByteArray(data_in);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().transceive(pkg, data_in);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcAdapterExtras
            public int getCardEmulationRoute(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCardEmulationRoute(pkg);
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

            @Override // android.nfc.INfcAdapterExtras
            public void setCardEmulationRoute(String pkg, int route) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(route);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCardEmulationRoute(pkg, route);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcAdapterExtras
            public void authenticate(String pkg, byte[] token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeByteArray(token);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().authenticate(pkg, token);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcAdapterExtras
            public String getDriverName(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDriverName(pkg);
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

            @Override // android.nfc.INfcAdapterExtras
            public boolean notifyCommandFromSecureElement(String pkg, String payload, int function) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeString(payload);
                    _data.writeInt(function);
                    boolean _result = false;
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().notifyCommandFromSecureElement(pkg, payload, function);
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

        public static boolean setDefaultImpl(INfcAdapterExtras impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INfcAdapterExtras getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
