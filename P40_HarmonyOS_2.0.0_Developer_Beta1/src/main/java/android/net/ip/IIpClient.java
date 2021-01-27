package android.net.ip;

import android.net.NattKeepalivePacketDataParcelable;
import android.net.ProvisioningConfigurationParcelable;
import android.net.ProxyInfo;
import android.net.TcpKeepalivePacketDataParcelable;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IIpClient extends IInterface {
    public static final int VERSION = 3;

    void addKeepalivePacketFilter(int i, TcpKeepalivePacketDataParcelable tcpKeepalivePacketDataParcelable) throws RemoteException;

    void addNattKeepalivePacketFilter(int i, NattKeepalivePacketDataParcelable nattKeepalivePacketDataParcelable) throws RemoteException;

    void completedPreDhcpAction() throws RemoteException;

    void confirmConfiguration() throws RemoteException;

    void forceRemoveDhcpCache() throws RemoteException;

    int getInterfaceVersion() throws RemoteException;

    void putPendingSSID(String str) throws RemoteException;

    void readPacketFilterComplete(byte[] bArr) throws RemoteException;

    void removeKeepalivePacketFilter(int i) throws RemoteException;

    void reportArpResult(int i, String str, boolean z) throws RemoteException;

    void setForceDhcpDiscovery() throws RemoteException;

    void setHttpProxy(ProxyInfo proxyInfo) throws RemoteException;

    void setL2KeyAndGroupHint(String str, String str2) throws RemoteException;

    void setMulticastFilter(boolean z) throws RemoteException;

    void setTcpBufferSizes(String str) throws RemoteException;

    void shutdown() throws RemoteException;

    void startProvisioning(ProvisioningConfigurationParcelable provisioningConfigurationParcelable) throws RemoteException;

    void stop() throws RemoteException;

    public static class Default implements IIpClient {
        @Override // android.net.ip.IIpClient
        public void completedPreDhcpAction() throws RemoteException {
        }

        @Override // android.net.ip.IIpClient
        public void confirmConfiguration() throws RemoteException {
        }

        @Override // android.net.ip.IIpClient
        public void readPacketFilterComplete(byte[] data) throws RemoteException {
        }

        @Override // android.net.ip.IIpClient
        public void shutdown() throws RemoteException {
        }

        @Override // android.net.ip.IIpClient
        public void startProvisioning(ProvisioningConfigurationParcelable req) throws RemoteException {
        }

        @Override // android.net.ip.IIpClient
        public void stop() throws RemoteException {
        }

        @Override // android.net.ip.IIpClient
        public void setTcpBufferSizes(String tcpBufferSizes) throws RemoteException {
        }

        @Override // android.net.ip.IIpClient
        public void setHttpProxy(ProxyInfo proxyInfo) throws RemoteException {
        }

        @Override // android.net.ip.IIpClient
        public void setMulticastFilter(boolean enabled) throws RemoteException {
        }

        @Override // android.net.ip.IIpClient
        public void addKeepalivePacketFilter(int slot, TcpKeepalivePacketDataParcelable pkt) throws RemoteException {
        }

        @Override // android.net.ip.IIpClient
        public void removeKeepalivePacketFilter(int slot) throws RemoteException {
        }

        @Override // android.net.ip.IIpClient
        public void setL2KeyAndGroupHint(String l2Key, String groupHint) throws RemoteException {
        }

        @Override // android.net.ip.IIpClient
        public void addNattKeepalivePacketFilter(int slot, NattKeepalivePacketDataParcelable pkt) throws RemoteException {
        }

        @Override // android.net.ip.IIpClient
        public void reportArpResult(int type, String uniqueStr, boolean success) throws RemoteException {
        }

        @Override // android.net.ip.IIpClient
        public void setForceDhcpDiscovery() throws RemoteException {
        }

        @Override // android.net.ip.IIpClient
        public void putPendingSSID(String pendingSSID) throws RemoteException {
        }

        @Override // android.net.ip.IIpClient
        public void forceRemoveDhcpCache() throws RemoteException {
        }

        @Override // android.net.ip.IIpClient
        public int getInterfaceVersion() {
            return -1;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IIpClient {
        private static final String DESCRIPTOR = "android.net.ip.IIpClient";
        static final int TRANSACTION_addKeepalivePacketFilter = 10;
        static final int TRANSACTION_addNattKeepalivePacketFilter = 13;
        static final int TRANSACTION_completedPreDhcpAction = 1;
        static final int TRANSACTION_confirmConfiguration = 2;
        static final int TRANSACTION_forceRemoveDhcpCache = 17;
        static final int TRANSACTION_getInterfaceVersion = 16777215;
        static final int TRANSACTION_putPendingSSID = 16;
        static final int TRANSACTION_readPacketFilterComplete = 3;
        static final int TRANSACTION_removeKeepalivePacketFilter = 11;
        static final int TRANSACTION_reportArpResult = 14;
        static final int TRANSACTION_setForceDhcpDiscovery = 15;
        static final int TRANSACTION_setHttpProxy = 8;
        static final int TRANSACTION_setL2KeyAndGroupHint = 12;
        static final int TRANSACTION_setMulticastFilter = 9;
        static final int TRANSACTION_setTcpBufferSizes = 7;
        static final int TRANSACTION_shutdown = 4;
        static final int TRANSACTION_startProvisioning = 5;
        static final int TRANSACTION_stop = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIpClient asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIpClient)) {
                return new Proxy(obj);
            }
            return (IIpClient) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ProvisioningConfigurationParcelable _arg0;
            ProxyInfo _arg02;
            TcpKeepalivePacketDataParcelable _arg1;
            NattKeepalivePacketDataParcelable _arg12;
            if (code == TRANSACTION_getInterfaceVersion) {
                data.enforceInterface(DESCRIPTOR);
                reply.writeNoException();
                reply.writeInt(getInterfaceVersion());
                return true;
            } else if (code != 1598968902) {
                boolean _arg2 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        completedPreDhcpAction();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        confirmConfiguration();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        readPacketFilterComplete(data.createByteArray());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        shutdown();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ProvisioningConfigurationParcelable.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        startProvisioning(_arg0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        stop();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        setTcpBufferSizes(data.readString());
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (ProxyInfo) ProxyInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        setHttpProxy(_arg02);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        setMulticastFilter(_arg2);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = TcpKeepalivePacketDataParcelable.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        addKeepalivePacketFilter(_arg03, _arg1);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        removeKeepalivePacketFilter(data.readInt());
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        setL2KeyAndGroupHint(data.readString(), data.readString());
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = NattKeepalivePacketDataParcelable.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        addNattKeepalivePacketFilter(_arg04, _arg12);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        String _arg13 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        reportArpResult(_arg05, _arg13, _arg2);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        setForceDhcpDiscovery();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        putPendingSSID(data.readString());
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        forceRemoveDhcpCache();
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
        public static class Proxy implements IIpClient {
            public static IIpClient sDefaultImpl;
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

            @Override // android.net.ip.IIpClient
            public void completedPreDhcpAction() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().completedPreDhcpAction();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClient
            public void confirmConfiguration() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().confirmConfiguration();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClient
            public void readPacketFilterComplete(byte[] data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(data);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().readPacketFilterComplete(data);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClient
            public void shutdown() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().shutdown();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClient
            public void startProvisioning(ProvisioningConfigurationParcelable req) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (req != null) {
                        _data.writeInt(1);
                        req.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().startProvisioning(req);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClient
            public void stop() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().stop();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClient
            public void setTcpBufferSizes(String tcpBufferSizes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(tcpBufferSizes);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setTcpBufferSizes(tcpBufferSizes);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClient
            public void setHttpProxy(ProxyInfo proxyInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (proxyInfo != null) {
                        _data.writeInt(1);
                        proxyInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setHttpProxy(proxyInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClient
            public void setMulticastFilter(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setMulticastFilter(enabled);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClient
            public void addKeepalivePacketFilter(int slot, TcpKeepalivePacketDataParcelable pkt) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    if (pkt != null) {
                        _data.writeInt(1);
                        pkt.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().addKeepalivePacketFilter(slot, pkt);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClient
            public void removeKeepalivePacketFilter(int slot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().removeKeepalivePacketFilter(slot);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClient
            public void setL2KeyAndGroupHint(String l2Key, String groupHint) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(l2Key);
                    _data.writeString(groupHint);
                    if (this.mRemote.transact(12, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setL2KeyAndGroupHint(l2Key, groupHint);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClient
            public void addNattKeepalivePacketFilter(int slot, NattKeepalivePacketDataParcelable pkt) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    if (pkt != null) {
                        _data.writeInt(1);
                        pkt.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(13, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().addNattKeepalivePacketFilter(slot, pkt);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClient
            public void reportArpResult(int type, String uniqueStr, boolean success) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeString(uniqueStr);
                    _data.writeInt(success ? 1 : 0);
                    if (this.mRemote.transact(14, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().reportArpResult(type, uniqueStr, success);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClient
            public void setForceDhcpDiscovery() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(15, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setForceDhcpDiscovery();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClient
            public void putPendingSSID(String pendingSSID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pendingSSID);
                    if (this.mRemote.transact(16, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().putPendingSSID(pendingSSID);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClient
            public void forceRemoveDhcpCache() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(17, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().forceRemoveDhcpCache();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.ip.IIpClient
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

        public static boolean setDefaultImpl(IIpClient impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IIpClient getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
