package android.net.lowpan;

import android.net.lowpan.ILowpanInterface;
import android.net.lowpan.ILowpanManagerListener;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ILowpanManager extends IInterface {
    public static final String LOWPAN_SERVICE_NAME = "lowpan";

    void addInterface(ILowpanInterface iLowpanInterface) throws RemoteException;

    void addListener(ILowpanManagerListener iLowpanManagerListener) throws RemoteException;

    ILowpanInterface getInterface(String str) throws RemoteException;

    String[] getInterfaceList() throws RemoteException;

    void removeInterface(ILowpanInterface iLowpanInterface) throws RemoteException;

    void removeListener(ILowpanManagerListener iLowpanManagerListener) throws RemoteException;

    public static class Default implements ILowpanManager {
        @Override // android.net.lowpan.ILowpanManager
        public ILowpanInterface getInterface(String name) throws RemoteException {
            return null;
        }

        @Override // android.net.lowpan.ILowpanManager
        public String[] getInterfaceList() throws RemoteException {
            return null;
        }

        @Override // android.net.lowpan.ILowpanManager
        public void addListener(ILowpanManagerListener listener) throws RemoteException {
        }

        @Override // android.net.lowpan.ILowpanManager
        public void removeListener(ILowpanManagerListener listener) throws RemoteException {
        }

        @Override // android.net.lowpan.ILowpanManager
        public void addInterface(ILowpanInterface lowpan_interface) throws RemoteException {
        }

        @Override // android.net.lowpan.ILowpanManager
        public void removeInterface(ILowpanInterface lowpan_interface) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ILowpanManager {
        private static final String DESCRIPTOR = "android.net.lowpan.ILowpanManager";
        static final int TRANSACTION_addInterface = 5;
        static final int TRANSACTION_addListener = 3;
        static final int TRANSACTION_getInterface = 1;
        static final int TRANSACTION_getInterfaceList = 2;
        static final int TRANSACTION_removeInterface = 6;
        static final int TRANSACTION_removeListener = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ILowpanManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ILowpanManager)) {
                return new Proxy(obj);
            }
            return (ILowpanManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getInterface";
                case 2:
                    return "getInterfaceList";
                case 3:
                    return "addListener";
                case 4:
                    return "removeListener";
                case 5:
                    return "addInterface";
                case 6:
                    return "removeInterface";
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
                        ILowpanInterface _result = getInterface(data.readString());
                        reply.writeNoException();
                        reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result2 = getInterfaceList();
                        reply.writeNoException();
                        reply.writeStringArray(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        addListener(ILowpanManagerListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        removeListener(ILowpanManagerListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        addInterface(ILowpanInterface.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        removeInterface(ILowpanInterface.Stub.asInterface(data.readStrongBinder()));
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
        public static class Proxy implements ILowpanManager {
            public static ILowpanManager sDefaultImpl;
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

            @Override // android.net.lowpan.ILowpanManager
            public ILowpanInterface getInterface(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInterface(name);
                    }
                    _reply.readException();
                    ILowpanInterface _result = ILowpanInterface.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.lowpan.ILowpanManager
            public String[] getInterfaceList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInterfaceList();
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

            @Override // android.net.lowpan.ILowpanManager
            public void addListener(ILowpanManagerListener listener) throws RemoteException {
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
                    Stub.getDefaultImpl().addListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.lowpan.ILowpanManager
            public void removeListener(ILowpanManagerListener listener) throws RemoteException {
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
                    Stub.getDefaultImpl().removeListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.lowpan.ILowpanManager
            public void addInterface(ILowpanInterface lowpan_interface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(lowpan_interface != null ? lowpan_interface.asBinder() : null);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addInterface(lowpan_interface);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.lowpan.ILowpanManager
            public void removeInterface(ILowpanInterface lowpan_interface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(lowpan_interface != null ? lowpan_interface.asBinder() : null);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeInterface(lowpan_interface);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ILowpanManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ILowpanManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
