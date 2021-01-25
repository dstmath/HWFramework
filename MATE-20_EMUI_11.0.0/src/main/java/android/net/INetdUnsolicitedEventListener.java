package android.net;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INetdUnsolicitedEventListener extends IInterface {
    public static final int VERSION = 2;

    int getInterfaceVersion() throws RemoteException;

    void onInterfaceAdded(String str) throws RemoteException;

    void onInterfaceAddressRemoved(String str, String str2, int i, int i2) throws RemoteException;

    void onInterfaceAddressUpdated(String str, String str2, int i, int i2) throws RemoteException;

    void onInterfaceChanged(String str, boolean z) throws RemoteException;

    void onInterfaceClassActivityChanged(boolean z, int i, long j, int i2) throws RemoteException;

    void onInterfaceDnsServerInfo(String str, long j, String[] strArr) throws RemoteException;

    void onInterfaceLinkStateChanged(String str, boolean z) throws RemoteException;

    void onInterfaceRemoved(String str) throws RemoteException;

    void onQuotaLimitReached(String str, String str2) throws RemoteException;

    void onRouteChanged(boolean z, String str, String str2, String str3) throws RemoteException;

    void onStrictCleartextDetected(int i, String str) throws RemoteException;

    public static class Default implements INetdUnsolicitedEventListener {
        @Override // android.net.INetdUnsolicitedEventListener
        public void onInterfaceClassActivityChanged(boolean isActive, int timerLabel, long timestampNs, int uid) throws RemoteException {
        }

        @Override // android.net.INetdUnsolicitedEventListener
        public void onQuotaLimitReached(String alertName, String ifName) throws RemoteException {
        }

        @Override // android.net.INetdUnsolicitedEventListener
        public void onInterfaceDnsServerInfo(String ifName, long lifetimeS, String[] servers) throws RemoteException {
        }

        @Override // android.net.INetdUnsolicitedEventListener
        public void onInterfaceAddressUpdated(String addr, String ifName, int flags, int scope) throws RemoteException {
        }

        @Override // android.net.INetdUnsolicitedEventListener
        public void onInterfaceAddressRemoved(String addr, String ifName, int flags, int scope) throws RemoteException {
        }

        @Override // android.net.INetdUnsolicitedEventListener
        public void onInterfaceAdded(String ifName) throws RemoteException {
        }

        @Override // android.net.INetdUnsolicitedEventListener
        public void onInterfaceRemoved(String ifName) throws RemoteException {
        }

        @Override // android.net.INetdUnsolicitedEventListener
        public void onInterfaceChanged(String ifName, boolean up) throws RemoteException {
        }

        @Override // android.net.INetdUnsolicitedEventListener
        public void onInterfaceLinkStateChanged(String ifName, boolean up) throws RemoteException {
        }

        @Override // android.net.INetdUnsolicitedEventListener
        public void onRouteChanged(boolean updated, String route, String gateway, String ifName) throws RemoteException {
        }

        @Override // android.net.INetdUnsolicitedEventListener
        public void onStrictCleartextDetected(int uid, String hex) throws RemoteException {
        }

        @Override // android.net.INetdUnsolicitedEventListener
        public int getInterfaceVersion() {
            return -1;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INetdUnsolicitedEventListener {
        private static final String DESCRIPTOR = "android.net.INetdUnsolicitedEventListener";
        static final int TRANSACTION_getInterfaceVersion = 16777215;
        static final int TRANSACTION_onInterfaceAdded = 6;
        static final int TRANSACTION_onInterfaceAddressRemoved = 5;
        static final int TRANSACTION_onInterfaceAddressUpdated = 4;
        static final int TRANSACTION_onInterfaceChanged = 8;
        static final int TRANSACTION_onInterfaceClassActivityChanged = 1;
        static final int TRANSACTION_onInterfaceDnsServerInfo = 3;
        static final int TRANSACTION_onInterfaceLinkStateChanged = 9;
        static final int TRANSACTION_onInterfaceRemoved = 7;
        static final int TRANSACTION_onQuotaLimitReached = 2;
        static final int TRANSACTION_onRouteChanged = 10;
        static final int TRANSACTION_onStrictCleartextDetected = 11;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INetdUnsolicitedEventListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INetdUnsolicitedEventListener)) {
                return new Proxy(obj);
            }
            return (INetdUnsolicitedEventListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == TRANSACTION_getInterfaceVersion) {
                data.enforceInterface(DESCRIPTOR);
                reply.writeNoException();
                reply.writeInt(getInterfaceVersion());
                return true;
            } else if (code != 1598968902) {
                boolean _arg0 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onInterfaceClassActivityChanged(data.readInt() != 0, data.readInt(), data.readLong(), data.readInt());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onQuotaLimitReached(data.readString(), data.readString());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        onInterfaceDnsServerInfo(data.readString(), data.readLong(), data.createStringArray());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        onInterfaceAddressUpdated(data.readString(), data.readString(), data.readInt(), data.readInt());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onInterfaceAddressRemoved(data.readString(), data.readString(), data.readInt(), data.readInt());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        onInterfaceAdded(data.readString());
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        onInterfaceRemoved(data.readString());
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        onInterfaceChanged(_arg02, _arg0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        onInterfaceLinkStateChanged(_arg03, _arg0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        onRouteChanged(_arg0, data.readString(), data.readString(), data.readString());
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        onStrictCleartextDetected(data.readInt(), data.readString());
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
        public static class Proxy implements INetdUnsolicitedEventListener {
            public static INetdUnsolicitedEventListener sDefaultImpl;
            private int mCachedVersion = -1;
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

            @Override // android.net.INetdUnsolicitedEventListener
            public void onInterfaceClassActivityChanged(boolean isActive, int timerLabel, long timestampNs, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isActive ? 1 : 0);
                    _data.writeInt(timerLabel);
                    _data.writeLong(timestampNs);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onInterfaceClassActivityChanged(isActive, timerLabel, timestampNs, uid);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetdUnsolicitedEventListener
            public void onQuotaLimitReached(String alertName, String ifName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(alertName);
                    _data.writeString(ifName);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onQuotaLimitReached(alertName, ifName);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetdUnsolicitedEventListener
            public void onInterfaceDnsServerInfo(String ifName, long lifetimeS, String[] servers) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    _data.writeLong(lifetimeS);
                    _data.writeStringArray(servers);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onInterfaceDnsServerInfo(ifName, lifetimeS, servers);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetdUnsolicitedEventListener
            public void onInterfaceAddressUpdated(String addr, String ifName, int flags, int scope) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(addr);
                    _data.writeString(ifName);
                    _data.writeInt(flags);
                    _data.writeInt(scope);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onInterfaceAddressUpdated(addr, ifName, flags, scope);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetdUnsolicitedEventListener
            public void onInterfaceAddressRemoved(String addr, String ifName, int flags, int scope) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(addr);
                    _data.writeString(ifName);
                    _data.writeInt(flags);
                    _data.writeInt(scope);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onInterfaceAddressRemoved(addr, ifName, flags, scope);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetdUnsolicitedEventListener
            public void onInterfaceAdded(String ifName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onInterfaceAdded(ifName);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetdUnsolicitedEventListener
            public void onInterfaceRemoved(String ifName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onInterfaceRemoved(ifName);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetdUnsolicitedEventListener
            public void onInterfaceChanged(String ifName, boolean up) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    _data.writeInt(up ? 1 : 0);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onInterfaceChanged(ifName, up);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetdUnsolicitedEventListener
            public void onInterfaceLinkStateChanged(String ifName, boolean up) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    _data.writeInt(up ? 1 : 0);
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onInterfaceLinkStateChanged(ifName, up);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetdUnsolicitedEventListener
            public void onRouteChanged(boolean updated, String route, String gateway, String ifName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(updated ? 1 : 0);
                    _data.writeString(route);
                    _data.writeString(gateway);
                    _data.writeString(ifName);
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onRouteChanged(updated, route, gateway, ifName);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetdUnsolicitedEventListener
            public void onStrictCleartextDetected(int uid, String hex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(hex);
                    if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onStrictCleartextDetected(uid, hex);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetdUnsolicitedEventListener
            public int getInterfaceVersion() throws RemoteException {
                if (this.mCachedVersion == -1) {
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    try {
                        data.writeInterfaceToken(Stub.DESCRIPTOR);
                        this.mRemote.transact(Stub.TRANSACTION_getInterfaceVersion, data, reply, 0);
                        reply.readException();
                        this.mCachedVersion = reply.readInt();
                    } finally {
                        reply.recycle();
                        data.recycle();
                    }
                }
                return this.mCachedVersion;
            }
        }

        public static boolean setDefaultImpl(INetdUnsolicitedEventListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INetdUnsolicitedEventListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
