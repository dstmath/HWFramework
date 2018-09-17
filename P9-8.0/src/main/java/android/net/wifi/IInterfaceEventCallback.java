package android.net.wifi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IInterfaceEventCallback extends IInterface {

    public static abstract class Stub extends Binder implements IInterfaceEventCallback {
        private static final String DESCRIPTOR = "android.net.wifi.IInterfaceEventCallback";
        static final int TRANSACTION_OnApInterfaceReady = 2;
        static final int TRANSACTION_OnApTorndownEvent = 4;
        static final int TRANSACTION_OnClientInterfaceReady = 1;
        static final int TRANSACTION_OnClientTorndownEvent = 3;

        private static class Proxy implements IInterfaceEventCallback {
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

            public void OnClientInterfaceReady(IClientInterface network_interface) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network_interface != null) {
                        iBinder = network_interface.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void OnApInterfaceReady(IApInterface network_interface) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network_interface != null) {
                        iBinder = network_interface.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void OnClientTorndownEvent(IClientInterface network_interface) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network_interface != null) {
                        iBinder = network_interface.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void OnApTorndownEvent(IApInterface network_interface) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network_interface != null) {
                        iBinder = network_interface.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IInterfaceEventCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IInterfaceEventCallback)) {
                return new Proxy(obj);
            }
            return (IInterfaceEventCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    OnClientInterfaceReady(android.net.wifi.IClientInterface.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    OnApInterfaceReady(android.net.wifi.IApInterface.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    OnClientTorndownEvent(android.net.wifi.IClientInterface.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    OnApTorndownEvent(android.net.wifi.IApInterface.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void OnApInterfaceReady(IApInterface iApInterface) throws RemoteException;

    void OnApTorndownEvent(IApInterface iApInterface) throws RemoteException;

    void OnClientInterfaceReady(IClientInterface iClientInterface) throws RemoteException;

    void OnClientTorndownEvent(IClientInterface iClientInterface) throws RemoteException;
}
