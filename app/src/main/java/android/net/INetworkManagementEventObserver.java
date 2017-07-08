package android.net;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INetworkManagementEventObserver extends IInterface {

    public static abstract class Stub extends Binder implements INetworkManagementEventObserver {
        private static final String DESCRIPTOR = "android.net.INetworkManagementEventObserver";
        static final int TRANSACTION_addressRemoved = 6;
        static final int TRANSACTION_addressUpdated = 5;
        static final int TRANSACTION_interfaceAdded = 3;
        static final int TRANSACTION_interfaceClassDataActivityChanged = 8;
        static final int TRANSACTION_interfaceDnsServerInfo = 9;
        static final int TRANSACTION_interfaceLinkStateChanged = 2;
        static final int TRANSACTION_interfaceRemoved = 4;
        static final int TRANSACTION_interfaceStatusChanged = 1;
        static final int TRANSACTION_limitReached = 7;
        static final int TRANSACTION_routeRemoved = 11;
        static final int TRANSACTION_routeUpdated = 10;

        private static class Proxy implements INetworkManagementEventObserver {
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

            public void interfaceStatusChanged(String iface, boolean up) throws RemoteException {
                int i = Stub.TRANSACTION_interfaceStatusChanged;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    if (!up) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_interfaceStatusChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void interfaceLinkStateChanged(String iface, boolean up) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    if (up) {
                        i = Stub.TRANSACTION_interfaceStatusChanged;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_interfaceLinkStateChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void interfaceAdded(String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    this.mRemote.transact(Stub.TRANSACTION_interfaceAdded, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void interfaceRemoved(String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    this.mRemote.transact(Stub.TRANSACTION_interfaceRemoved, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addressUpdated(String iface, LinkAddress address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    if (address != null) {
                        _data.writeInt(Stub.TRANSACTION_interfaceStatusChanged);
                        address.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_addressUpdated, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addressRemoved(String iface, LinkAddress address) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    if (address != null) {
                        _data.writeInt(Stub.TRANSACTION_interfaceStatusChanged);
                        address.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_addressRemoved, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void limitReached(String limitName, String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(limitName);
                    _data.writeString(iface);
                    this.mRemote.transact(Stub.TRANSACTION_limitReached, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void interfaceClassDataActivityChanged(String label, boolean active, long tsNanos) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(label);
                    if (active) {
                        i = Stub.TRANSACTION_interfaceStatusChanged;
                    }
                    _data.writeInt(i);
                    _data.writeLong(tsNanos);
                    this.mRemote.transact(Stub.TRANSACTION_interfaceClassDataActivityChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void interfaceDnsServerInfo(String iface, long lifetime, String[] servers) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    _data.writeLong(lifetime);
                    _data.writeStringArray(servers);
                    this.mRemote.transact(Stub.TRANSACTION_interfaceDnsServerInfo, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void routeUpdated(RouteInfo route) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (route != null) {
                        _data.writeInt(Stub.TRANSACTION_interfaceStatusChanged);
                        route.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_routeUpdated, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void routeRemoved(RouteInfo route) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (route != null) {
                        _data.writeInt(Stub.TRANSACTION_interfaceStatusChanged);
                        route.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_routeRemoved, _data, _reply, 0);
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

        public static INetworkManagementEventObserver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INetworkManagementEventObserver)) {
                return new Proxy(obj);
            }
            return (INetworkManagementEventObserver) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String _arg0;
            LinkAddress linkAddress;
            RouteInfo routeInfo;
            switch (code) {
                case TRANSACTION_interfaceStatusChanged /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    interfaceStatusChanged(data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_interfaceLinkStateChanged /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    interfaceLinkStateChanged(data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_interfaceAdded /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    interfaceAdded(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_interfaceRemoved /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    interfaceRemoved(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addressUpdated /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        linkAddress = (LinkAddress) LinkAddress.CREATOR.createFromParcel(data);
                    } else {
                        linkAddress = null;
                    }
                    addressUpdated(_arg0, linkAddress);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addressRemoved /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        linkAddress = (LinkAddress) LinkAddress.CREATOR.createFromParcel(data);
                    } else {
                        linkAddress = null;
                    }
                    addressRemoved(_arg0, linkAddress);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_limitReached /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    limitReached(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_interfaceClassDataActivityChanged /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    interfaceClassDataActivityChanged(data.readString(), data.readInt() != 0, data.readLong());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_interfaceDnsServerInfo /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    interfaceDnsServerInfo(data.readString(), data.readLong(), data.createStringArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_routeUpdated /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        routeInfo = (RouteInfo) RouteInfo.CREATOR.createFromParcel(data);
                    } else {
                        routeInfo = null;
                    }
                    routeUpdated(routeInfo);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_routeRemoved /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        routeInfo = (RouteInfo) RouteInfo.CREATOR.createFromParcel(data);
                    } else {
                        routeInfo = null;
                    }
                    routeRemoved(routeInfo);
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addressRemoved(String str, LinkAddress linkAddress) throws RemoteException;

    void addressUpdated(String str, LinkAddress linkAddress) throws RemoteException;

    void interfaceAdded(String str) throws RemoteException;

    void interfaceClassDataActivityChanged(String str, boolean z, long j) throws RemoteException;

    void interfaceDnsServerInfo(String str, long j, String[] strArr) throws RemoteException;

    void interfaceLinkStateChanged(String str, boolean z) throws RemoteException;

    void interfaceRemoved(String str) throws RemoteException;

    void interfaceStatusChanged(String str, boolean z) throws RemoteException;

    void limitReached(String str, String str2) throws RemoteException;

    void routeRemoved(RouteInfo routeInfo) throws RemoteException;

    void routeUpdated(RouteInfo routeInfo) throws RemoteException;
}
