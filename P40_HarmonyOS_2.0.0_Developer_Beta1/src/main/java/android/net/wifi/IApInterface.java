package android.net.wifi;

import android.net.wifi.IApInterfaceEventCallback;
import android.net.wifi.IApLinkedEvent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IApInterface extends IInterface {
    public static final int ENCRYPTION_TYPE_NONE = 0;
    public static final int ENCRYPTION_TYPE_WPA = 1;
    public static final int ENCRYPTION_TYPE_WPA2 = 2;

    String getInterfaceName() throws RemoteException;

    int getNumberOfAssociatedStations() throws RemoteException;

    boolean registerCallback(IApInterfaceEventCallback iApInterfaceEventCallback) throws RemoteException;

    void subscribeStationChangeEvents(IApLinkedEvent iApLinkedEvent) throws RemoteException;

    void unsubscribeStationChangeEvents() throws RemoteException;

    public static class Default implements IApInterface {
        @Override // android.net.wifi.IApInterface
        public boolean registerCallback(IApInterfaceEventCallback callback) throws RemoteException {
            return false;
        }

        @Override // android.net.wifi.IApInterface
        public String getInterfaceName() throws RemoteException {
            return null;
        }

        @Override // android.net.wifi.IApInterface
        public int getNumberOfAssociatedStations() throws RemoteException {
            return 0;
        }

        @Override // android.net.wifi.IApInterface
        public void subscribeStationChangeEvents(IApLinkedEvent handler) throws RemoteException {
        }

        @Override // android.net.wifi.IApInterface
        public void unsubscribeStationChangeEvents() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IApInterface {
        private static final String DESCRIPTOR = "android.net.wifi.IApInterface";
        static final int TRANSACTION_getInterfaceName = 2;
        static final int TRANSACTION_getNumberOfAssociatedStations = 3;
        static final int TRANSACTION_registerCallback = 1;
        static final int TRANSACTION_subscribeStationChangeEvents = 4;
        static final int TRANSACTION_unsubscribeStationChangeEvents = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IApInterface asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IApInterface)) {
                return new Proxy(obj);
            }
            return (IApInterface) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean registerCallback = registerCallback(IApInterfaceEventCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(registerCallback ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                String _result = getInterfaceName();
                reply.writeNoException();
                reply.writeString(_result);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int _result2 = getNumberOfAssociatedStations();
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                subscribeStationChangeEvents(IApLinkedEvent.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                unsubscribeStationChangeEvents();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IApInterface {
            public static IApInterface sDefaultImpl;
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

            @Override // android.net.wifi.IApInterface
            public boolean registerCallback(IApInterfaceEventCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerCallback(callback);
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

            @Override // android.net.wifi.IApInterface
            public String getInterfaceName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getInterfaceName();
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

            @Override // android.net.wifi.IApInterface
            public int getNumberOfAssociatedStations() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNumberOfAssociatedStations();
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

            @Override // android.net.wifi.IApInterface
            public void subscribeStationChangeEvents(IApLinkedEvent handler) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(handler != null ? handler.asBinder() : null);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().subscribeStationChangeEvents(handler);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IApInterface
            public void unsubscribeStationChangeEvents() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().unsubscribeStationChangeEvents();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IApInterface impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IApInterface getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
