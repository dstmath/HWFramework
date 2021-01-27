package android.net;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INetworkMonitor extends IInterface {
    public static final int NETWORK_TEST_RESULT_INVALID = 1;
    public static final int NETWORK_TEST_RESULT_PARTIAL_CONNECTIVITY = 2;
    public static final int NETWORK_TEST_RESULT_PORTAL = 128;
    public static final int NETWORK_TEST_RESULT_VALID = 0;
    public static final int NETWORK_VALIDATION_PROBE_DNS = 4;
    public static final int NETWORK_VALIDATION_PROBE_FALLBACK = 32;
    public static final int NETWORK_VALIDATION_PROBE_HTTP = 8;
    public static final int NETWORK_VALIDATION_PROBE_HTTPS = 16;
    public static final int NETWORK_VALIDATION_PROBE_PRIVDNS = 64;
    public static final int NETWORK_VALIDATION_RESULT_PARTIAL = 2;
    public static final int NETWORK_VALIDATION_RESULT_VALID = 1;
    public static final int VERSION = 3;

    void forceReevaluation(int i) throws RemoteException;

    int getInterfaceVersion() throws RemoteException;

    void launchCaptivePortalApp() throws RemoteException;

    void notifyCaptivePortalAppFinished(int i) throws RemoteException;

    void notifyDnsResponse(int i) throws RemoteException;

    void notifyLinkPropertiesChanged(LinkProperties linkProperties) throws RemoteException;

    void notifyNetworkCapabilitiesChanged(NetworkCapabilities networkCapabilities) throws RemoteException;

    void notifyNetworkConnected(LinkProperties linkProperties, NetworkCapabilities networkCapabilities) throws RemoteException;

    void notifyNetworkDisconnected() throws RemoteException;

    void notifyPrivateDnsChanged(PrivateDnsConfigParcel privateDnsConfigParcel) throws RemoteException;

    void setAcceptPartialConnectivity() throws RemoteException;

    void start() throws RemoteException;

    public static class Default implements INetworkMonitor {
        @Override // android.net.INetworkMonitor
        public void start() throws RemoteException {
        }

        @Override // android.net.INetworkMonitor
        public void launchCaptivePortalApp() throws RemoteException {
        }

        @Override // android.net.INetworkMonitor
        public void notifyCaptivePortalAppFinished(int response) throws RemoteException {
        }

        @Override // android.net.INetworkMonitor
        public void setAcceptPartialConnectivity() throws RemoteException {
        }

        @Override // android.net.INetworkMonitor
        public void forceReevaluation(int uid) throws RemoteException {
        }

        @Override // android.net.INetworkMonitor
        public void notifyPrivateDnsChanged(PrivateDnsConfigParcel config) throws RemoteException {
        }

        @Override // android.net.INetworkMonitor
        public void notifyDnsResponse(int returnCode) throws RemoteException {
        }

        @Override // android.net.INetworkMonitor
        public void notifyNetworkConnected(LinkProperties lp, NetworkCapabilities nc) throws RemoteException {
        }

        @Override // android.net.INetworkMonitor
        public void notifyNetworkDisconnected() throws RemoteException {
        }

        @Override // android.net.INetworkMonitor
        public void notifyLinkPropertiesChanged(LinkProperties lp) throws RemoteException {
        }

        @Override // android.net.INetworkMonitor
        public void notifyNetworkCapabilitiesChanged(NetworkCapabilities nc) throws RemoteException {
        }

        @Override // android.net.INetworkMonitor
        public int getInterfaceVersion() {
            return -1;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INetworkMonitor {
        private static final String DESCRIPTOR = "android.net.INetworkMonitor";
        static final int TRANSACTION_forceReevaluation = 5;
        static final int TRANSACTION_getInterfaceVersion = 16777215;
        static final int TRANSACTION_launchCaptivePortalApp = 2;
        static final int TRANSACTION_notifyCaptivePortalAppFinished = 3;
        static final int TRANSACTION_notifyDnsResponse = 7;
        static final int TRANSACTION_notifyLinkPropertiesChanged = 10;
        static final int TRANSACTION_notifyNetworkCapabilitiesChanged = 11;
        static final int TRANSACTION_notifyNetworkConnected = 8;
        static final int TRANSACTION_notifyNetworkDisconnected = 9;
        static final int TRANSACTION_notifyPrivateDnsChanged = 6;
        static final int TRANSACTION_setAcceptPartialConnectivity = 4;
        static final int TRANSACTION_start = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INetworkMonitor asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INetworkMonitor)) {
                return new Proxy(obj);
            }
            return (INetworkMonitor) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PrivateDnsConfigParcel _arg0;
            LinkProperties _arg02;
            NetworkCapabilities _arg1;
            LinkProperties _arg03;
            NetworkCapabilities _arg04;
            if (code == TRANSACTION_getInterfaceVersion) {
                data.enforceInterface(DESCRIPTOR);
                reply.writeNoException();
                reply.writeInt(getInterfaceVersion());
                return true;
            } else if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        start();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        launchCaptivePortalApp();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        notifyCaptivePortalAppFinished(data.readInt());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        setAcceptPartialConnectivity();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        forceReevaluation(data.readInt());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = PrivateDnsConfigParcel.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        notifyPrivateDnsChanged(_arg0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        notifyDnsResponse(data.readInt());
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (LinkProperties) LinkProperties.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = (NetworkCapabilities) NetworkCapabilities.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        notifyNetworkConnected(_arg02, _arg1);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        notifyNetworkDisconnected();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = (LinkProperties) LinkProperties.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        notifyLinkPropertiesChanged(_arg03);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = (NetworkCapabilities) NetworkCapabilities.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        notifyNetworkCapabilitiesChanged(_arg04);
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
        public static class Proxy implements INetworkMonitor {
            public static INetworkMonitor sDefaultImpl;
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

            @Override // android.net.INetworkMonitor
            public void start() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().start();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkMonitor
            public void launchCaptivePortalApp() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().launchCaptivePortalApp();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkMonitor
            public void notifyCaptivePortalAppFinished(int response) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(response);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyCaptivePortalAppFinished(response);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkMonitor
            public void setAcceptPartialConnectivity() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setAcceptPartialConnectivity();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkMonitor
            public void forceReevaluation(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().forceReevaluation(uid);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkMonitor
            public void notifyPrivateDnsChanged(PrivateDnsConfigParcel config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyPrivateDnsChanged(config);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkMonitor
            public void notifyDnsResponse(int returnCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(returnCode);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyDnsResponse(returnCode);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkMonitor
            public void notifyNetworkConnected(LinkProperties lp, NetworkCapabilities nc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (lp != null) {
                        _data.writeInt(1);
                        lp.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (nc != null) {
                        _data.writeInt(1);
                        nc.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyNetworkConnected(lp, nc);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkMonitor
            public void notifyNetworkDisconnected() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyNetworkDisconnected();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkMonitor
            public void notifyLinkPropertiesChanged(LinkProperties lp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (lp != null) {
                        _data.writeInt(1);
                        lp.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyLinkPropertiesChanged(lp);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkMonitor
            public void notifyNetworkCapabilitiesChanged(NetworkCapabilities nc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (nc != null) {
                        _data.writeInt(1);
                        nc.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyNetworkCapabilitiesChanged(nc);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkMonitor
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

        public static boolean setDefaultImpl(INetworkMonitor impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INetworkMonitor getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
