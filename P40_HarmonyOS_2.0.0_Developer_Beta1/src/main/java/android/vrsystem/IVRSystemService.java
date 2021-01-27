package android.vrsystem;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.vrsystem.IVRListener;

public interface IVRSystemService extends IInterface {
    void acceptInCall() throws RemoteException;

    void addVRListener(IVRListener iVRListener) throws RemoteException;

    void deleteVRListener(IVRListener iVRListener) throws RemoteException;

    void endInCall() throws RemoteException;

    String getContactName(String str) throws RemoteException;

    int getHelmetBattery() throws RemoteException;

    int getHelmetBrightness() throws RemoteException;

    boolean isVRmode() throws RemoteException;

    void registerExpandListener(IVRListener iVRListener) throws RemoteException;

    void setHelmetBrightness(int i) throws RemoteException;

    public static class Default implements IVRSystemService {
        @Override // android.vrsystem.IVRSystemService
        public boolean isVRmode() throws RemoteException {
            return false;
        }

        @Override // android.vrsystem.IVRSystemService
        public String getContactName(String num) throws RemoteException {
            return null;
        }

        @Override // android.vrsystem.IVRSystemService
        public void addVRListener(IVRListener listener) throws RemoteException {
        }

        @Override // android.vrsystem.IVRSystemService
        public void registerExpandListener(IVRListener listener) throws RemoteException {
        }

        @Override // android.vrsystem.IVRSystemService
        public void deleteVRListener(IVRListener listener) throws RemoteException {
        }

        @Override // android.vrsystem.IVRSystemService
        public void acceptInCall() throws RemoteException {
        }

        @Override // android.vrsystem.IVRSystemService
        public void endInCall() throws RemoteException {
        }

        @Override // android.vrsystem.IVRSystemService
        public int getHelmetBattery() throws RemoteException {
            return 0;
        }

        @Override // android.vrsystem.IVRSystemService
        public int getHelmetBrightness() throws RemoteException {
            return 0;
        }

        @Override // android.vrsystem.IVRSystemService
        public void setHelmetBrightness(int brightness) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IVRSystemService {
        private static final String DESCRIPTOR = "android.vrsystem.IVRSystemService";
        static final int TRANSACTION_acceptInCall = 6;
        static final int TRANSACTION_addVRListener = 3;
        static final int TRANSACTION_deleteVRListener = 5;
        static final int TRANSACTION_endInCall = 7;
        static final int TRANSACTION_getContactName = 2;
        static final int TRANSACTION_getHelmetBattery = 8;
        static final int TRANSACTION_getHelmetBrightness = 9;
        static final int TRANSACTION_isVRmode = 1;
        static final int TRANSACTION_registerExpandListener = 4;
        static final int TRANSACTION_setHelmetBrightness = 10;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVRSystemService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVRSystemService)) {
                return new Proxy(obj);
            }
            return (IVRSystemService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "isVRmode";
                case 2:
                    return "getContactName";
                case 3:
                    return "addVRListener";
                case 4:
                    return "registerExpandListener";
                case 5:
                    return "deleteVRListener";
                case 6:
                    return "acceptInCall";
                case 7:
                    return "endInCall";
                case 8:
                    return "getHelmetBattery";
                case 9:
                    return "getHelmetBrightness";
                case 10:
                    return "setHelmetBrightness";
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
                        boolean isVRmode = isVRmode();
                        reply.writeNoException();
                        reply.writeInt(isVRmode ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        String _result = getContactName(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        addVRListener(IVRListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        registerExpandListener(IVRListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        deleteVRListener(IVRListener.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        acceptInCall();
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        endInCall();
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = getHelmetBattery();
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = getHelmetBrightness();
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        setHelmetBrightness(data.readInt());
                        reply.writeNoException();
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
        public static class Proxy implements IVRSystemService {
            public static IVRSystemService sDefaultImpl;
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

            @Override // android.vrsystem.IVRSystemService
            public boolean isVRmode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isVRmode();
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

            @Override // android.vrsystem.IVRSystemService
            public String getContactName(String num) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(num);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getContactName(num);
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

            @Override // android.vrsystem.IVRSystemService
            public void addVRListener(IVRListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addVRListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.vrsystem.IVRSystemService
            public void registerExpandListener(IVRListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerExpandListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.vrsystem.IVRSystemService
            public void deleteVRListener(IVRListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().deleteVRListener(listener);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.vrsystem.IVRSystemService
            public void acceptInCall() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().acceptInCall();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.vrsystem.IVRSystemService
            public void endInCall() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().endInCall();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.vrsystem.IVRSystemService
            public int getHelmetBattery() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHelmetBattery();
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

            @Override // android.vrsystem.IVRSystemService
            public int getHelmetBrightness() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHelmetBrightness();
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

            @Override // android.vrsystem.IVRSystemService
            public void setHelmetBrightness(int brightness) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(brightness);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setHelmetBrightness(brightness);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IVRSystemService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IVRSystemService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
