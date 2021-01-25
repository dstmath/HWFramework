package android.net;

import android.net.IEthernetServiceListener;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IEthernetManager extends IInterface {
    void addListener(IEthernetServiceListener iEthernetServiceListener) throws RemoteException;

    String[] getAvailableInterfaces() throws RemoteException;

    IpConfiguration getConfiguration(String str) throws RemoteException;

    boolean isAvailable(String str) throws RemoteException;

    void removeListener(IEthernetServiceListener iEthernetServiceListener) throws RemoteException;

    void setConfiguration(String str, IpConfiguration ipConfiguration) throws RemoteException;

    public static class Default implements IEthernetManager {
        @Override // android.net.IEthernetManager
        public String[] getAvailableInterfaces() throws RemoteException {
            return null;
        }

        @Override // android.net.IEthernetManager
        public IpConfiguration getConfiguration(String iface) throws RemoteException {
            return null;
        }

        @Override // android.net.IEthernetManager
        public void setConfiguration(String iface, IpConfiguration config) throws RemoteException {
        }

        @Override // android.net.IEthernetManager
        public boolean isAvailable(String iface) throws RemoteException {
            return false;
        }

        @Override // android.net.IEthernetManager
        public void addListener(IEthernetServiceListener listener) throws RemoteException {
        }

        @Override // android.net.IEthernetManager
        public void removeListener(IEthernetServiceListener listener) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IEthernetManager {
        private static final String DESCRIPTOR = "android.net.IEthernetManager";
        static final int TRANSACTION_addListener = 5;
        static final int TRANSACTION_getAvailableInterfaces = 1;
        static final int TRANSACTION_getConfiguration = 2;
        static final int TRANSACTION_isAvailable = 4;
        static final int TRANSACTION_removeListener = 6;
        static final int TRANSACTION_setConfiguration = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IEthernetManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IEthernetManager)) {
                return new Proxy(obj);
            }
            return (IEthernetManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getAvailableInterfaces";
                case 2:
                    return "getConfiguration";
                case 3:
                    return "setConfiguration";
                case 4:
                    return "isAvailable";
                case 5:
                    return "addListener";
                case 6:
                    return "removeListener";
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
            IpConfiguration _arg1;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result = getAvailableInterfaces();
                        reply.writeNoException();
                        reply.writeStringArray(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        IpConfiguration _result2 = getConfiguration(data.readString());
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
                        String _arg0 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = IpConfiguration.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        setConfiguration(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isAvailable = isAvailable(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isAvailable ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        addListener(IEthernetServiceListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        removeListener(IEthernetServiceListener.Stub.asInterface(data.readStrongBinder()));
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
        public static class Proxy implements IEthernetManager {
            public static IEthernetManager sDefaultImpl;
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

            @Override // android.net.IEthernetManager
            public String[] getAvailableInterfaces() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAvailableInterfaces();
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IEthernetManager
            public IpConfiguration getConfiguration(String iface) throws RemoteException {
                IpConfiguration _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getConfiguration(iface);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = IpConfiguration.CREATOR.createFromParcel(_reply);
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

            @Override // android.net.IEthernetManager
            public void setConfiguration(String iface, IpConfiguration config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setConfiguration(iface, config);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IEthernetManager
            public boolean isAvailable(String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAvailable(iface);
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

            @Override // android.net.IEthernetManager
            public void addListener(IEthernetServiceListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IEthernetManager
            public void removeListener(IEthernetServiceListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IEthernetManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IEthernetManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
