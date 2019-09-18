package android.net.metrics;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INetdEventListener extends IInterface {
    public static final int DNS_REPORTED_IP_ADDRESSES_LIMIT = 10;
    public static final int EVENT_GETADDRINFO = 1;
    public static final int EVENT_GETHOSTBYNAME = 2;
    public static final int REPORTING_LEVEL_FULL = 2;
    public static final int REPORTING_LEVEL_METRICS = 1;
    public static final int REPORTING_LEVEL_NONE = 0;

    public static abstract class Stub extends Binder implements INetdEventListener {
        private static final String DESCRIPTOR = "android.net.metrics.INetdEventListener";
        static final int TRANSACTION_onConnectEvent = 3;
        static final int TRANSACTION_onDnsEvent = 1;
        static final int TRANSACTION_onPrivateDnsValidationEvent = 2;
        static final int TRANSACTION_onTcpSocketStatsEvent = 5;
        static final int TRANSACTION_onWakeupEvent = 4;

        private static class Proxy implements INetdEventListener {
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

            public void onDnsEvent(int netId, int eventType, int returnCode, int latencyMs, String hostname, String[] ipAddresses, int ipAddressesCount, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeInt(eventType);
                    _data.writeInt(returnCode);
                    _data.writeInt(latencyMs);
                    _data.writeString(hostname);
                    _data.writeStringArray(ipAddresses);
                    _data.writeInt(ipAddressesCount);
                    _data.writeInt(uid);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPrivateDnsValidationEvent(int netId, String ipAddress, String hostname, boolean validated) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeString(ipAddress);
                    _data.writeString(hostname);
                    _data.writeInt(validated);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onConnectEvent(int netId, int error, int latencyMs, String ipAddr, int port, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeInt(error);
                    _data.writeInt(latencyMs);
                    _data.writeString(ipAddr);
                    _data.writeInt(port);
                    _data.writeInt(uid);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onWakeupEvent(String prefix, int uid, int ethertype, int ipNextHeader, byte[] dstHw, String srcIp, String dstIp, int srcPort, int dstPort, long timestampNs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(prefix);
                    try {
                        _data.writeInt(uid);
                        try {
                            _data.writeInt(ethertype);
                            try {
                                _data.writeInt(ipNextHeader);
                            } catch (Throwable th) {
                                th = th;
                                byte[] bArr = dstHw;
                                String str = srcIp;
                                String str2 = dstIp;
                                int i = srcPort;
                                int i2 = dstPort;
                                long j = timestampNs;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            int i3 = ipNextHeader;
                            byte[] bArr2 = dstHw;
                            String str3 = srcIp;
                            String str22 = dstIp;
                            int i4 = srcPort;
                            int i22 = dstPort;
                            long j2 = timestampNs;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        int i5 = ethertype;
                        int i32 = ipNextHeader;
                        byte[] bArr22 = dstHw;
                        String str32 = srcIp;
                        String str222 = dstIp;
                        int i42 = srcPort;
                        int i222 = dstPort;
                        long j22 = timestampNs;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeByteArray(dstHw);
                        try {
                            _data.writeString(srcIp);
                            try {
                                _data.writeString(dstIp);
                                try {
                                    _data.writeInt(srcPort);
                                } catch (Throwable th4) {
                                    th = th4;
                                    int i2222 = dstPort;
                                    long j222 = timestampNs;
                                    _data.recycle();
                                    throw th;
                                }
                            } catch (Throwable th5) {
                                th = th5;
                                int i422 = srcPort;
                                int i22222 = dstPort;
                                long j2222 = timestampNs;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th6) {
                            th = th6;
                            String str2222 = dstIp;
                            int i4222 = srcPort;
                            int i222222 = dstPort;
                            long j22222 = timestampNs;
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(dstPort);
                            try {
                                _data.writeLong(timestampNs);
                                try {
                                    this.mRemote.transact(4, _data, null, 1);
                                    _data.recycle();
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
                            long j222222 = timestampNs;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th10) {
                        th = th10;
                        String str322 = srcIp;
                        String str22222 = dstIp;
                        int i42222 = srcPort;
                        int i2222222 = dstPort;
                        long j2222222 = timestampNs;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th11) {
                    th = th11;
                    int i6 = uid;
                    int i52 = ethertype;
                    int i322 = ipNextHeader;
                    byte[] bArr222 = dstHw;
                    String str3222 = srcIp;
                    String str222222 = dstIp;
                    int i422222 = srcPort;
                    int i22222222 = dstPort;
                    long j22222222 = timestampNs;
                    _data.recycle();
                    throw th;
                }
            }

            public void onTcpSocketStatsEvent(int[] networkIds, int[] sentPackets, int[] lostPackets, int[] rttUs, int[] sentAckDiffMs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(networkIds);
                    _data.writeIntArray(sentPackets);
                    _data.writeIntArray(lostPackets);
                    _data.writeIntArray(rttUs);
                    _data.writeIntArray(sentAckDiffMs);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            if (i != 1598968902) {
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        onDnsEvent(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readString(), data.createStringArray(), data.readInt(), data.readInt());
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        onPrivateDnsValidationEvent(data.readInt(), data.readString(), data.readString(), data.readInt() != 0);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        onConnectEvent(data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readInt(), data.readInt());
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        onWakeupEvent(data.readString(), data.readInt(), data.readInt(), data.readInt(), data.createByteArray(), data.readString(), data.readString(), data.readInt(), data.readInt(), data.readLong());
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        onTcpSocketStatsEvent(data.createIntArray(), data.createIntArray(), data.createIntArray(), data.createIntArray(), data.createIntArray());
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

    void onConnectEvent(int i, int i2, int i3, String str, int i4, int i5) throws RemoteException;

    void onDnsEvent(int i, int i2, int i3, int i4, String str, String[] strArr, int i5, int i6) throws RemoteException;

    void onPrivateDnsValidationEvent(int i, String str, String str2, boolean z) throws RemoteException;

    void onTcpSocketStatsEvent(int[] iArr, int[] iArr2, int[] iArr3, int[] iArr4, int[] iArr5) throws RemoteException;

    void onWakeupEvent(String str, int i, int i2, int i3, byte[] bArr, String str2, String str3, int i4, int i5, long j) throws RemoteException;
}
