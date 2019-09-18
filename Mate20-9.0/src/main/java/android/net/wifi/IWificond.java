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

    public static abstract class Stub extends Binder implements IWificond {
        private static final String DESCRIPTOR = "android.net.wifi.IWificond";
        static final int TRANSACTION_GetApInterfaces = 7;
        static final int TRANSACTION_GetClientInterfaces = 6;
        static final int TRANSACTION_RegisterCallback = 13;
        static final int TRANSACTION_UnregisterCallback = 14;
        static final int TRANSACTION_createApInterface = 1;
        static final int TRANSACTION_createClientInterface = 2;
        static final int TRANSACTION_disableSupplicant = 12;
        static final int TRANSACTION_enableSupplicant = 11;
        static final int TRANSACTION_getAvailable2gChannels = 8;
        static final int TRANSACTION_getAvailable5gNonDFSChannels = 9;
        static final int TRANSACTION_getAvailableDFSChannels = 10;
        static final int TRANSACTION_tearDownApInterface = 3;
        static final int TRANSACTION_tearDownClientInterface = 4;
        static final int TRANSACTION_tearDownInterfaces = 5;

        private static class Proxy implements IWificond {
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

            public IApInterface createApInterface(String iface_name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface_name);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return IApInterface.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IClientInterface createClientInterface(String iface_name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface_name);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return IClientInterface.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean tearDownApInterface(String iface_name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface_name);
                    boolean _result = false;
                    this.mRemote.transact(3, _data, _reply, 0);
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

            public boolean tearDownClientInterface(String iface_name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface_name);
                    boolean _result = false;
                    this.mRemote.transact(4, _data, _reply, 0);
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

            public void tearDownInterfaces() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<IBinder> GetClientInterfaces() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createBinderArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<IBinder> GetApInterfaces() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createBinderArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getAvailable2gChannels() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createIntArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getAvailable5gNonDFSChannels() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createIntArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getAvailableDFSChannels() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createIntArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean enableSupplicant() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(11, _data, _reply, 0);
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

            public boolean disableSupplicant() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(12, _data, _reply, 0);
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

            public void RegisterCallback(IInterfaceEventCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(13, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void UnregisterCallback(IInterfaceEventCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(14, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

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
                        boolean _result3 = tearDownApInterface(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result4 = tearDownClientInterface(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        tearDownInterfaces();
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        List<IBinder> _result5 = GetClientInterfaces();
                        reply.writeNoException();
                        reply.writeBinderList(_result5);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        List<IBinder> _result6 = GetApInterfaces();
                        reply.writeNoException();
                        reply.writeBinderList(_result6);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result7 = getAvailable2gChannels();
                        reply.writeNoException();
                        reply.writeIntArray(_result7);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result8 = getAvailable5gNonDFSChannels();
                        reply.writeNoException();
                        reply.writeIntArray(_result8);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result9 = getAvailableDFSChannels();
                        reply.writeNoException();
                        reply.writeIntArray(_result9);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result10 = enableSupplicant();
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result11 = disableSupplicant();
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        RegisterCallback(IInterfaceEventCallback.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 14:
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
    }

    List<IBinder> GetApInterfaces() throws RemoteException;

    List<IBinder> GetClientInterfaces() throws RemoteException;

    void RegisterCallback(IInterfaceEventCallback iInterfaceEventCallback) throws RemoteException;

    void UnregisterCallback(IInterfaceEventCallback iInterfaceEventCallback) throws RemoteException;

    IApInterface createApInterface(String str) throws RemoteException;

    IClientInterface createClientInterface(String str) throws RemoteException;

    boolean disableSupplicant() throws RemoteException;

    boolean enableSupplicant() throws RemoteException;

    int[] getAvailable2gChannels() throws RemoteException;

    int[] getAvailable5gNonDFSChannels() throws RemoteException;

    int[] getAvailableDFSChannels() throws RemoteException;

    boolean tearDownApInterface(String str) throws RemoteException;

    boolean tearDownClientInterface(String str) throws RemoteException;

    void tearDownInterfaces() throws RemoteException;
}
