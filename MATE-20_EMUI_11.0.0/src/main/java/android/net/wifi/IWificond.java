package android.net.wifi;

import android.net.wifi.IApInterface;
import android.net.wifi.IClientInterface;
import android.net.wifi.IInterfaceEventCallback;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IWificond extends IInterface {
    List<IBinder> GetApInterfaces() throws RemoteException;

    List<IBinder> GetClientInterfaces() throws RemoteException;

    void RegisterCallback(IInterfaceEventCallback iInterfaceEventCallback) throws RemoteException;

    void UnregisterCallback(IInterfaceEventCallback iInterfaceEventCallback) throws RemoteException;

    IApInterface createApInterface(String str) throws RemoteException;

    IClientInterface createClientInterface(String str) throws RemoteException;

    int[] getAvailable2gChannels() throws RemoteException;

    int[] getAvailable5gNonDFSChannels() throws RemoteException;

    int[] getAvailableDFSChannels() throws RemoteException;

    boolean tearDownApInterface(String str) throws RemoteException;

    boolean tearDownClientInterface(String str) throws RemoteException;

    void tearDownInterfaces() throws RemoteException;

    public static class Default implements IWificond {
        @Override // android.net.wifi.IWificond
        public IApInterface createApInterface(String iface_name) throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWificond
        public IClientInterface createClientInterface(String iface_name) throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWificond
        public boolean tearDownApInterface(String iface_name) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWificond
        public boolean tearDownClientInterface(String iface_name) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IWificond
        public void tearDownInterfaces() throws RemoteException {
        }

        @Override // android.net.wifi.IWificond
        public List<IBinder> GetClientInterfaces() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWificond
        public List<IBinder> GetApInterfaces() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWificond
        public int[] getAvailable2gChannels() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWificond
        public int[] getAvailable5gNonDFSChannels() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWificond
        public int[] getAvailableDFSChannels() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IWificond
        public void RegisterCallback(IInterfaceEventCallback callback) throws RemoteException {
        }

        @Override // android.net.wifi.IWificond
        public void UnregisterCallback(IInterfaceEventCallback callback) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IWificond {
        private static final String DESCRIPTOR = "android.net.wifi.IWificond";
        static final int TRANSACTION_GetApInterfaces = 7;
        static final int TRANSACTION_GetClientInterfaces = 6;
        static final int TRANSACTION_RegisterCallback = 11;
        static final int TRANSACTION_UnregisterCallback = 12;
        static final int TRANSACTION_createApInterface = 1;
        static final int TRANSACTION_createClientInterface = 2;
        static final int TRANSACTION_getAvailable2gChannels = 8;
        static final int TRANSACTION_getAvailable5gNonDFSChannels = 9;
        static final int TRANSACTION_getAvailableDFSChannels = 10;
        static final int TRANSACTION_tearDownApInterface = 3;
        static final int TRANSACTION_tearDownClientInterface = 4;
        static final int TRANSACTION_tearDownInterfaces = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWificond asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWificond)) {
                return new Proxy(obj);
            }
            return (IWificond) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                IBinder iBinder = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        IApInterface _result = createApInterface(data.readString());
                        reply.writeNoException();
                        if (_result != null) {
                            iBinder = _result.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        IClientInterface _result2 = createClientInterface(data.readString());
                        reply.writeNoException();
                        if (_result2 != null) {
                            iBinder = _result2.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean tearDownApInterface = tearDownApInterface(data.readString());
                        reply.writeNoException();
                        reply.writeInt(tearDownApInterface ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean tearDownClientInterface = tearDownClientInterface(data.readString());
                        reply.writeNoException();
                        reply.writeInt(tearDownClientInterface ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        tearDownInterfaces();
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        List<IBinder> _result3 = GetClientInterfaces();
                        reply.writeNoException();
                        reply.writeBinderList(_result3);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        List<IBinder> _result4 = GetApInterfaces();
                        reply.writeNoException();
                        reply.writeBinderList(_result4);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result5 = getAvailable2gChannels();
                        reply.writeNoException();
                        reply.writeIntArray(_result5);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result6 = getAvailable5gNonDFSChannels();
                        reply.writeNoException();
                        reply.writeIntArray(_result6);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result7 = getAvailableDFSChannels();
                        reply.writeNoException();
                        reply.writeIntArray(_result7);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        RegisterCallback(IInterfaceEventCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        UnregisterCallback(IInterfaceEventCallback.Stub.asInterface(data.readStrongBinder()));
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
        public static class Proxy implements IWificond {
            public static IWificond sDefaultImpl;
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

            @Override // android.net.wifi.IWificond
            public IApInterface createApInterface(String iface_name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface_name);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createApInterface(iface_name);
                    }
                    _reply.readException();
                    IApInterface _result = IApInterface.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWificond
            public IClientInterface createClientInterface(String iface_name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface_name);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createClientInterface(iface_name);
                    }
                    _reply.readException();
                    IClientInterface _result = IClientInterface.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWificond
            public boolean tearDownApInterface(String iface_name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface_name);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().tearDownApInterface(iface_name);
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

            @Override // android.net.wifi.IWificond
            public boolean tearDownClientInterface(String iface_name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface_name);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().tearDownClientInterface(iface_name);
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

            @Override // android.net.wifi.IWificond
            public void tearDownInterfaces() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().tearDownInterfaces();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWificond
            public List<IBinder> GetClientInterfaces() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().GetClientInterfaces();
                    }
                    _reply.readException();
                    List<IBinder> _result = _reply.createBinderArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWificond
            public List<IBinder> GetApInterfaces() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().GetApInterfaces();
                    }
                    _reply.readException();
                    List<IBinder> _result = _reply.createBinderArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWificond
            public int[] getAvailable2gChannels() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAvailable2gChannels();
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWificond
            public int[] getAvailable5gNonDFSChannels() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAvailable5gNonDFSChannels();
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWificond
            public int[] getAvailableDFSChannels() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAvailableDFSChannels();
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWificond
            public void RegisterCallback(IInterfaceEventCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().RegisterCallback(callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWificond
            public void UnregisterCallback(IInterfaceEventCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(12, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().UnregisterCallback(callback);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IWificond impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IWificond getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
