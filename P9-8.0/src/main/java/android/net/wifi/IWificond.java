package android.net.wifi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IWificond extends IInterface {

    public static abstract class Stub extends Binder implements IWificond {
        private static final String DESCRIPTOR = "android.net.wifi.IWificond";
        static final int TRANSACTION_GetApInterfaces = 5;
        static final int TRANSACTION_GetClientInterfaces = 4;
        static final int TRANSACTION_RegisterCallback = 6;
        static final int TRANSACTION_UnregisterCallback = 7;
        static final int TRANSACTION_createApInterface = 1;
        static final int TRANSACTION_createClientInterface = 2;
        static final int TRANSACTION_registerRttClient = 8;
        static final int TRANSACTION_tearDownInterfaces = 3;
        static final int TRANSACTION_unregisterRttClient = 9;

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

            public IApInterface createApInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    IApInterface _result = android.net.wifi.IApInterface.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IClientInterface createClientInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    IClientInterface _result = android.net.wifi.IClientInterface.Stub.asInterface(_reply.readStrongBinder());
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
                    this.mRemote.transact(3, _data, _reply, 0);
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
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    List<IBinder> _result = _reply.createBinderArrayList();
                    return _result;
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
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    List<IBinder> _result = _reply.createBinderArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void RegisterCallback(IInterfaceEventCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void UnregisterCallback(IInterfaceEventCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public IRttController registerRttClient(IRttClient rttClient) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (rttClient != null) {
                        iBinder = rttClient.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    IRttController _result = android.net.wifi.IRttController.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterRttClient(IRttClient rttClient) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (rttClient != null) {
                        iBinder = rttClient.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
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
            IBinder iBinder = null;
            List<IBinder> _result;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    IApInterface _result2 = createApInterface();
                    reply.writeNoException();
                    if (_result2 != null) {
                        iBinder = _result2.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    IClientInterface _result3 = createClientInterface();
                    reply.writeNoException();
                    if (_result3 != null) {
                        iBinder = _result3.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    tearDownInterfaces();
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _result = GetClientInterfaces();
                    reply.writeNoException();
                    reply.writeBinderList(_result);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    _result = GetApInterfaces();
                    reply.writeNoException();
                    reply.writeBinderList(_result);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    RegisterCallback(android.net.wifi.IInterfaceEventCallback.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    UnregisterCallback(android.net.wifi.IInterfaceEventCallback.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    IRttController _result4 = registerRttClient(android.net.wifi.IRttClient.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    if (_result4 != null) {
                        iBinder = _result4.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterRttClient(android.net.wifi.IRttClient.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    List<IBinder> GetApInterfaces() throws RemoteException;

    List<IBinder> GetClientInterfaces() throws RemoteException;

    void RegisterCallback(IInterfaceEventCallback iInterfaceEventCallback) throws RemoteException;

    void UnregisterCallback(IInterfaceEventCallback iInterfaceEventCallback) throws RemoteException;

    IApInterface createApInterface() throws RemoteException;

    IClientInterface createClientInterface() throws RemoteException;

    IRttController registerRttClient(IRttClient iRttClient) throws RemoteException;

    void tearDownInterfaces() throws RemoteException;

    void unregisterRttClient(IRttClient iRttClient) throws RemoteException;
}
