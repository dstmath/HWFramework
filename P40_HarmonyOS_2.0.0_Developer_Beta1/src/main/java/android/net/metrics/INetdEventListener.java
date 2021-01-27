package android.net.metrics;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INetdEventListener extends IInterface {
    public static final int DNS_REPORTED_IP_ADDRESSES_LIMIT = 10;
    public static final int EVENT_GETADDRINFO = 1;
    public static final int EVENT_GETHOSTBYADDR = 3;
    public static final int EVENT_GETHOSTBYNAME = 2;
    public static final int EVENT_RES_NSEND = 4;
    public static final int REPORTING_LEVEL_FULL = 2;
    public static final int REPORTING_LEVEL_METRICS = 1;
    public static final int REPORTING_LEVEL_NONE = 0;
    public static final int VERSION = 10000;

    int getInterfaceVersion() throws RemoteException;

    void onConnectEvent(int i, int i2, int i3, String str, int i4, int i5) throws RemoteException;

    void onDnsEvent(int i, int i2, int i3, int i4, String str, String[] strArr, int i5, int i6) throws RemoteException;

    void onNat64PrefixEvent(int i, boolean z, String str, int i2) throws RemoteException;

    void onPrivateDnsValidationEvent(int i, String str, String str2, boolean z) throws RemoteException;

    void onTcpSocketStatsEvent(int[] iArr, int[] iArr2, int[] iArr3, int[] iArr4, int[] iArr5) throws RemoteException;

    void onWakeupEvent(String str, int i, int i2, int i3, byte[] bArr, String str2, String str3, int i4, int i5, long j) throws RemoteException;

    public static class Default implements INetdEventListener {
        @Override // android.net.metrics.INetdEventListener
        public void onDnsEvent(int netId, int eventType, int returnCode, int latencyMs, String hostname, String[] ipAddresses, int ipAddressesCount, int uid) throws RemoteException {
        }

        @Override // android.net.metrics.INetdEventListener
        public void onPrivateDnsValidationEvent(int netId, String ipAddress, String hostname, boolean validated) throws RemoteException {
        }

        @Override // android.net.metrics.INetdEventListener
        public void onConnectEvent(int netId, int error, int latencyMs, String ipAddr, int port, int uid) throws RemoteException {
        }

        @Override // android.net.metrics.INetdEventListener
        public void onWakeupEvent(String prefix, int uid, int ethertype, int ipNextHeader, byte[] dstHw, String srcIp, String dstIp, int srcPort, int dstPort, long timestampNs) throws RemoteException {
        }

        @Override // android.net.metrics.INetdEventListener
        public void onTcpSocketStatsEvent(int[] networkIds, int[] sentPackets, int[] lostPackets, int[] rttUs, int[] sentAckDiffMs) throws RemoteException {
        }

        @Override // android.net.metrics.INetdEventListener
        public void onNat64PrefixEvent(int netId, boolean added, String prefixString, int prefixLength) throws RemoteException {
        }

        @Override // android.net.metrics.INetdEventListener
        public int getInterfaceVersion() {
            return -1;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INetdEventListener {
        private static final String DESCRIPTOR = "android.net.metrics.INetdEventListener";
        static final int TRANSACTION_getInterfaceVersion = 16777215;
        static final int TRANSACTION_onConnectEvent = 3;
        static final int TRANSACTION_onDnsEvent = 1;
        static final int TRANSACTION_onNat64PrefixEvent = 6;
        static final int TRANSACTION_onPrivateDnsValidationEvent = 2;
        static final int TRANSACTION_onTcpSocketStatsEvent = 5;
        static final int TRANSACTION_onWakeupEvent = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INetdEventListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INetdEventListener)) {
                return new Proxy(obj);
            }
            return (INetdEventListener) iin;
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
                boolean _arg1 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onDnsEvent(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readString(), data.createStringArray(), data.readInt(), data.readInt());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        String _arg12 = data.readString();
                        String _arg2 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        onPrivateDnsValidationEvent(_arg0, _arg12, _arg2, _arg1);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        onConnectEvent(data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readInt(), data.readInt());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        onWakeupEvent(data.readString(), data.readInt(), data.readInt(), data.readInt(), data.createByteArray(), data.readString(), data.readString(), data.readInt(), data.readInt(), data.readLong());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onTcpSocketStatsEvent(data.createIntArray(), data.createIntArray(), data.createIntArray(), data.createIntArray(), data.createIntArray());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        onNat64PrefixEvent(_arg02, _arg1, data.readString(), data.readInt());
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
        public static class Proxy implements INetdEventListener {
            public static INetdEventListener sDefaultImpl;
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

            @Override // android.net.metrics.INetdEventListener
            public void onDnsEvent(int netId, int eventType, int returnCode, int latencyMs, String hostname, String[] ipAddresses, int ipAddressesCount, int uid) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(netId);
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(eventType);
                        try {
                            _data.writeInt(returnCode);
                        } catch (Throwable th3) {
                            th = th3;
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(latencyMs);
                            try {
                                _data.writeString(hostname);
                                _data.writeStringArray(ipAddresses);
                                _data.writeInt(ipAddressesCount);
                                _data.writeInt(uid);
                                if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                    _data.recycle();
                                    return;
                                }
                                Stub.getDefaultImpl().onDnsEvent(netId, eventType, returnCode, latencyMs, hostname, ipAddresses, ipAddressesCount, uid);
                                _data.recycle();
                            } catch (Throwable th4) {
                                th = th4;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.net.metrics.INetdEventListener
            public void onPrivateDnsValidationEvent(int netId, String ipAddress, String hostname, boolean validated) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeString(ipAddress);
                    _data.writeString(hostname);
                    _data.writeInt(validated ? 1 : 0);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onPrivateDnsValidationEvent(netId, ipAddress, hostname, validated);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.metrics.INetdEventListener
            public void onConnectEvent(int netId, int error, int latencyMs, String ipAddr, int port, int uid) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(netId);
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(error);
                        try {
                            _data.writeInt(latencyMs);
                            try {
                                _data.writeString(ipAddr);
                                try {
                                    _data.writeInt(port);
                                } catch (Throwable th3) {
                                    th = th3;
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(uid);
                            try {
                                if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                    _data.recycle();
                                    return;
                                }
                                Stub.getDefaultImpl().onConnectEvent(netId, error, latencyMs, ipAddr, port, uid);
                                _data.recycle();
                            } catch (Throwable th6) {
                                th = th6;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th7) {
                            th = th7;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th8) {
                        th = th8;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th9) {
                    th = th9;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.net.metrics.INetdEventListener
            public void onWakeupEvent(String prefix, int uid, int ethertype, int ipNextHeader, byte[] dstHw, String srcIp, String dstIp, int srcPort, int dstPort, long timestampNs) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(prefix);
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(uid);
                        _data.writeInt(ethertype);
                        _data.writeInt(ipNextHeader);
                        _data.writeByteArray(dstHw);
                        _data.writeString(srcIp);
                        _data.writeString(dstIp);
                        _data.writeInt(srcPort);
                        _data.writeInt(dstPort);
                        _data.writeLong(timestampNs);
                        if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().onWakeupEvent(prefix, uid, ethertype, ipNextHeader, dstHw, srcIp, dstIp, srcPort, dstPort, timestampNs);
                        _data.recycle();
                    } catch (Throwable th3) {
                        th = th3;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.net.metrics.INetdEventListener
            public void onTcpSocketStatsEvent(int[] networkIds, int[] sentPackets, int[] lostPackets, int[] rttUs, int[] sentAckDiffMs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(networkIds);
                    _data.writeIntArray(sentPackets);
                    _data.writeIntArray(lostPackets);
                    _data.writeIntArray(rttUs);
                    _data.writeIntArray(sentAckDiffMs);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onTcpSocketStatsEvent(networkIds, sentPackets, lostPackets, rttUs, sentAckDiffMs);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.metrics.INetdEventListener
            public void onNat64PrefixEvent(int netId, boolean added, String prefixString, int prefixLength) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeInt(added ? 1 : 0);
                    _data.writeString(prefixString);
                    _data.writeInt(prefixLength);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onNat64PrefixEvent(netId, added, prefixString, prefixLength);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.metrics.INetdEventListener
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

        public static boolean setDefaultImpl(INetdEventListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INetdEventListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
