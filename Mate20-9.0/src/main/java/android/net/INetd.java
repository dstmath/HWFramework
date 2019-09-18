package android.net;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.os.RemoteException;
import java.io.FileDescriptor;

public interface INetd extends IInterface {
    public static final int CONF = 1;
    public static final String IPSEC_INTERFACE_PREFIX = "ipsec";
    public static final int IPV4 = 4;
    public static final int IPV6 = 6;
    public static final int IPV6_ADDR_GEN_MODE_DEFAULT = 0;
    public static final int IPV6_ADDR_GEN_MODE_EUI64 = 0;
    public static final int IPV6_ADDR_GEN_MODE_NONE = 1;
    public static final int IPV6_ADDR_GEN_MODE_RANDOM = 3;
    public static final int IPV6_ADDR_GEN_MODE_STABLE_PRIVACY = 2;
    public static final int NEIGH = 2;
    public static final String PERMISSION_NETWORK = "NETWORK";
    public static final String PERMISSION_SYSTEM = "SYSTEM";
    public static final int RESOLVER_PARAMS_COUNT = 4;
    public static final int RESOLVER_PARAMS_MAX_SAMPLES = 3;
    public static final int RESOLVER_PARAMS_MIN_SAMPLES = 2;
    public static final int RESOLVER_PARAMS_SAMPLE_VALIDITY = 0;
    public static final int RESOLVER_PARAMS_SUCCESS_THRESHOLD = 1;
    public static final int RESOLVER_STATS_COUNT = 7;
    public static final int RESOLVER_STATS_ERRORS = 1;
    public static final int RESOLVER_STATS_INTERNAL_ERRORS = 3;
    public static final int RESOLVER_STATS_LAST_SAMPLE_TIME = 5;
    public static final int RESOLVER_STATS_RTT_AVG = 4;
    public static final int RESOLVER_STATS_SUCCESSES = 0;
    public static final int RESOLVER_STATS_TIMEOUTS = 2;
    public static final int RESOLVER_STATS_USABLE = 6;
    public static final int TETHER_STATS_ARRAY_SIZE = 4;
    public static final int TETHER_STATS_RX_BYTES = 0;
    public static final int TETHER_STATS_RX_PACKETS = 1;
    public static final int TETHER_STATS_TX_BYTES = 2;
    public static final int TETHER_STATS_TX_PACKETS = 3;

    public static abstract class Stub extends Binder implements INetd {
        private static final String DESCRIPTOR = "android.net.INetd";
        static final int TRANSACTION_addVirtualTunnelInterface = 31;
        static final int TRANSACTION_bandwidthEnableDataSaver = 3;
        static final int TRANSACTION_firewallReplaceUidChain = 2;
        static final int TRANSACTION_getMetricsReportingLevel = 20;
        static final int TRANSACTION_getNetdPid = 34;
        static final int TRANSACTION_getResolverInfo = 14;
        static final int TRANSACTION_interfaceAddAddress = 17;
        static final int TRANSACTION_interfaceDelAddress = 18;
        static final int TRANSACTION_ipSecAddSecurityAssociation = 24;
        static final int TRANSACTION_ipSecAddSecurityPolicy = 28;
        static final int TRANSACTION_ipSecAllocateSpi = 23;
        static final int TRANSACTION_ipSecApplyTransportModeTransform = 26;
        static final int TRANSACTION_ipSecDeleteSecurityAssociation = 25;
        static final int TRANSACTION_ipSecDeleteSecurityPolicy = 30;
        static final int TRANSACTION_ipSecRemoveTransportModeTransform = 27;
        static final int TRANSACTION_ipSecSetEncapSocketOwner = 22;
        static final int TRANSACTION_ipSecUpdateSecurityPolicy = 29;
        static final int TRANSACTION_ipTableConfig = 39;
        static final int TRANSACTION_isAlive = 1;
        static final int TRANSACTION_networkAddInterface = 7;
        static final int TRANSACTION_networkAddUidRanges = 9;
        static final int TRANSACTION_networkCreatePhysical = 4;
        static final int TRANSACTION_networkCreateVpn = 5;
        static final int TRANSACTION_networkDestroy = 6;
        static final int TRANSACTION_networkRejectNonSecureVpn = 11;
        static final int TRANSACTION_networkRemoveInterface = 8;
        static final int TRANSACTION_networkRemoveUidRanges = 10;
        static final int TRANSACTION_removeVirtualTunnelInterface = 33;
        static final int TRANSACTION_setIPv6AddrGenMode = 37;
        static final int TRANSACTION_setMetricsReportingLevel = 21;
        static final int TRANSACTION_setProcSysNet = 19;
        static final int TRANSACTION_setResolverConfiguration = 13;
        static final int TRANSACTION_socketDestroy = 12;
        static final int TRANSACTION_tetherApplyDnsInterfaces = 15;
        static final int TRANSACTION_tetherGetStats = 16;
        static final int TRANSACTION_trafficCheckBpfStatsEnable = 38;
        static final int TRANSACTION_updateVirtualTunnelInterface = 32;
        static final int TRANSACTION_wakeupAddInterface = 35;
        static final int TRANSACTION_wakeupDelInterface = 36;

        private static class Proxy implements INetd {
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

            public boolean isAlive() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(1, _data, _reply, 0);
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

            public boolean firewallReplaceUidChain(String chainName, boolean isWhitelist, int[] uids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(chainName);
                    _data.writeInt(isWhitelist);
                    _data.writeIntArray(uids);
                    boolean _result = false;
                    this.mRemote.transact(2, _data, _reply, 0);
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

            public boolean bandwidthEnableDataSaver(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
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

            public void networkCreatePhysical(int netId, String permission) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeString(permission);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void networkCreateVpn(int netId, boolean hasDns, boolean secure) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeInt(hasDns);
                    _data.writeInt(secure);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void networkDestroy(int netId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void networkAddInterface(int netId, String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeString(iface);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void networkRemoveInterface(int netId, String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeString(iface);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void networkAddUidRanges(int netId, UidRange[] uidRanges) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeTypedArray(uidRanges, 0);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void networkRemoveUidRanges(int netId, UidRange[] uidRanges) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeTypedArray(uidRanges, 0);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void networkRejectNonSecureVpn(boolean add, UidRange[] uidRanges) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(add);
                    _data.writeTypedArray(uidRanges, 0);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void socketDestroy(UidRange[] uidRanges, int[] exemptUids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(uidRanges, 0);
                    _data.writeIntArray(exemptUids);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setResolverConfiguration(int netId, String[] servers, String[] domains, int[] params, String tlsName, String[] tlsServers, String[] tlsFingerprints) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeStringArray(servers);
                    _data.writeStringArray(domains);
                    _data.writeIntArray(params);
                    _data.writeString(tlsName);
                    _data.writeStringArray(tlsServers);
                    _data.writeStringArray(tlsFingerprints);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getResolverInfo(int netId, String[] servers, String[] domains, int[] params, int[] stats) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    if (servers == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(servers.length);
                    }
                    if (domains == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(domains.length);
                    }
                    if (params == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(params.length);
                    }
                    if (stats == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(stats.length);
                    }
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    _reply.readStringArray(servers);
                    _reply.readStringArray(domains);
                    _reply.readIntArray(params);
                    _reply.readIntArray(stats);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean tetherApplyDnsInterfaces() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(15, _data, _reply, 0);
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

            public PersistableBundle tetherGetStats() throws RemoteException {
                PersistableBundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (PersistableBundle) PersistableBundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void interfaceAddAddress(String ifName, String addrString, int prefixLength) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    _data.writeString(addrString);
                    _data.writeInt(prefixLength);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void interfaceDelAddress(String ifName, String addrString, int prefixLength) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    _data.writeString(addrString);
                    _data.writeInt(prefixLength);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setProcSysNet(int family, int which, String ifname, String parameter, String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(family);
                    _data.writeInt(which);
                    _data.writeString(ifname);
                    _data.writeString(parameter);
                    _data.writeString(value);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getMetricsReportingLevel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setMetricsReportingLevel(int level) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(level);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void ipSecSetEncapSocketOwner(FileDescriptor socket, int newUid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeRawFileDescriptor(socket);
                    _data.writeInt(newUid);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int ipSecAllocateSpi(int transformId, String sourceAddress, String destinationAddress, int spi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(transformId);
                    _data.writeString(sourceAddress);
                    _data.writeString(destinationAddress);
                    _data.writeInt(spi);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void ipSecAddSecurityAssociation(int transformId, int mode, String sourceAddress, String destinationAddress, int underlyingNetId, int spi, int markValue, int markMask, String authAlgo, byte[] authKey, int authTruncBits, String cryptAlgo, byte[] cryptKey, int cryptTruncBits, String aeadAlgo, byte[] aeadKey, int aeadIcvBits, int encapType, int encapLocalPort, int encapRemotePort) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(transformId);
                    _data.writeInt(mode);
                    try {
                        _data.writeString(sourceAddress);
                        try {
                            _data.writeString(destinationAddress);
                        } catch (Throwable th) {
                            th = th;
                            int i = underlyingNetId;
                            int i2 = spi;
                            int i3 = markValue;
                            int i4 = markMask;
                            String str = authAlgo;
                            byte[] bArr = authKey;
                            int i5 = authTruncBits;
                            String str2 = cryptAlgo;
                            byte[] bArr2 = cryptKey;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        String str3 = destinationAddress;
                        int i6 = underlyingNetId;
                        int i22 = spi;
                        int i32 = markValue;
                        int i42 = markMask;
                        String str4 = authAlgo;
                        byte[] bArr3 = authKey;
                        int i52 = authTruncBits;
                        String str22 = cryptAlgo;
                        byte[] bArr22 = cryptKey;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(underlyingNetId);
                        try {
                            _data.writeInt(spi);
                            try {
                                _data.writeInt(markValue);
                            } catch (Throwable th3) {
                                th = th3;
                                int i422 = markMask;
                                String str42 = authAlgo;
                                byte[] bArr32 = authKey;
                                int i522 = authTruncBits;
                                String str222 = cryptAlgo;
                                byte[] bArr222 = cryptKey;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            int i322 = markValue;
                            int i4222 = markMask;
                            String str422 = authAlgo;
                            byte[] bArr322 = authKey;
                            int i5222 = authTruncBits;
                            String str2222 = cryptAlgo;
                            byte[] bArr2222 = cryptKey;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        int i222 = spi;
                        int i3222 = markValue;
                        int i42222 = markMask;
                        String str4222 = authAlgo;
                        byte[] bArr3222 = authKey;
                        int i52222 = authTruncBits;
                        String str22222 = cryptAlgo;
                        byte[] bArr22222 = cryptKey;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(markMask);
                        try {
                            _data.writeString(authAlgo);
                            try {
                                _data.writeByteArray(authKey);
                            } catch (Throwable th6) {
                                th = th6;
                                int i522222 = authTruncBits;
                                String str222222 = cryptAlgo;
                                byte[] bArr222222 = cryptKey;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th7) {
                            th = th7;
                            byte[] bArr32222 = authKey;
                            int i5222222 = authTruncBits;
                            String str2222222 = cryptAlgo;
                            byte[] bArr2222222 = cryptKey;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th8) {
                        th = th8;
                        String str42222 = authAlgo;
                        byte[] bArr322222 = authKey;
                        int i52222222 = authTruncBits;
                        String str22222222 = cryptAlgo;
                        byte[] bArr22222222 = cryptKey;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(authTruncBits);
                        try {
                            _data.writeString(cryptAlgo);
                            try {
                                _data.writeByteArray(cryptKey);
                                _data.writeInt(cryptTruncBits);
                                _data.writeString(aeadAlgo);
                                _data.writeByteArray(aeadKey);
                                _data.writeInt(aeadIcvBits);
                                _data.writeInt(encapType);
                                _data.writeInt(encapLocalPort);
                                _data.writeInt(encapRemotePort);
                                this.mRemote.transact(24, _data, _reply, 0);
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                            } catch (Throwable th9) {
                                th = th9;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th10) {
                            th = th10;
                            byte[] bArr222222222 = cryptKey;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th11) {
                        th = th11;
                        String str222222222 = cryptAlgo;
                        byte[] bArr2222222222 = cryptKey;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th12) {
                    th = th12;
                    String str5 = sourceAddress;
                    String str32 = destinationAddress;
                    int i62 = underlyingNetId;
                    int i2222 = spi;
                    int i32222 = markValue;
                    int i422222 = markMask;
                    String str422222 = authAlgo;
                    byte[] bArr3222222 = authKey;
                    int i522222222 = authTruncBits;
                    String str2222222222 = cryptAlgo;
                    byte[] bArr22222222222 = cryptKey;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            public void ipSecDeleteSecurityAssociation(int transformId, String sourceAddress, String destinationAddress, int spi, int markValue, int markMask) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(transformId);
                    _data.writeString(sourceAddress);
                    _data.writeString(destinationAddress);
                    _data.writeInt(spi);
                    _data.writeInt(markValue);
                    _data.writeInt(markMask);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void ipSecApplyTransportModeTransform(FileDescriptor socket, int transformId, int direction, String sourceAddress, String destinationAddress, int spi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeRawFileDescriptor(socket);
                    _data.writeInt(transformId);
                    _data.writeInt(direction);
                    _data.writeString(sourceAddress);
                    _data.writeString(destinationAddress);
                    _data.writeInt(spi);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void ipSecRemoveTransportModeTransform(FileDescriptor socket) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeRawFileDescriptor(socket);
                    this.mRemote.transact(Stub.TRANSACTION_ipSecRemoveTransportModeTransform, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void ipSecAddSecurityPolicy(int transformId, int direction, String sourceAddress, String destinationAddress, int spi, int markValue, int markMask) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(transformId);
                    _data.writeInt(direction);
                    _data.writeString(sourceAddress);
                    _data.writeString(destinationAddress);
                    _data.writeInt(spi);
                    _data.writeInt(markValue);
                    _data.writeInt(markMask);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void ipSecUpdateSecurityPolicy(int transformId, int direction, String sourceAddress, String destinationAddress, int spi, int markValue, int markMask) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(transformId);
                    _data.writeInt(direction);
                    _data.writeString(sourceAddress);
                    _data.writeString(destinationAddress);
                    _data.writeInt(spi);
                    _data.writeInt(markValue);
                    _data.writeInt(markMask);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void ipSecDeleteSecurityPolicy(int transformId, int direction, String sourceAddress, String destinationAddress, int markValue, int markMask) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(transformId);
                    _data.writeInt(direction);
                    _data.writeString(sourceAddress);
                    _data.writeString(destinationAddress);
                    _data.writeInt(markValue);
                    _data.writeInt(markMask);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addVirtualTunnelInterface(String deviceName, String localAddress, String remoteAddress, int iKey, int oKey) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceName);
                    _data.writeString(localAddress);
                    _data.writeString(remoteAddress);
                    _data.writeInt(iKey);
                    _data.writeInt(oKey);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateVirtualTunnelInterface(String deviceName, String localAddress, String remoteAddress, int iKey, int oKey) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceName);
                    _data.writeString(localAddress);
                    _data.writeString(remoteAddress);
                    _data.writeInt(iKey);
                    _data.writeInt(oKey);
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeVirtualTunnelInterface(String deviceName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(deviceName);
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getNetdPid() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void wakeupAddInterface(String ifName, String prefix, int mark, int mask) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    _data.writeString(prefix);
                    _data.writeInt(mark);
                    _data.writeInt(mask);
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void wakeupDelInterface(String ifName, String prefix, int mark, int mask) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    _data.writeString(prefix);
                    _data.writeInt(mark);
                    _data.writeInt(mask);
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setIPv6AddrGenMode(String ifName, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ifName);
                    _data.writeInt(mode);
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean trafficCheckBpfStatsEnable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(38, _data, _reply, 0);
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

            public boolean ipTableConfig(int enable, int uid, String iface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    _data.writeInt(uid);
                    _data.writeString(iface);
                    boolean _result = false;
                    this.mRemote.transact(39, _data, _reply, 0);
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
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INetd asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INetd)) {
                return new Proxy(obj);
            }
            return (INetd) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /*  JADX ERROR: NullPointerException in pass: CodeShrinkVisitor
            java.lang.NullPointerException
            	at jadx.core.dex.instructions.args.InsnArg.wrapInstruction(InsnArg.java:117)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.inline(CodeShrinkVisitor.java:119)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:70)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:42)
            	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.visit(CodeShrinkVisitor.java:34)
            */
        public boolean onTransact(int r45, android.os.Parcel r46, android.os.Parcel r47, int r48) throws android.os.RemoteException {
            /*
                r44 = this;
                r15 = r44
                r14 = r45
                r13 = r46
                r12 = r47
                java.lang.String r11 = "android.net.INetd"
                r0 = 1598968902(0x5f4e5446, float:1.4867585E19)
                r10 = 1
                if (r14 == r0) goto L_0x051a
                r0 = 0
                switch(r14) {
                    case 1: goto L_0x0508;
                    case 2: goto L_0x04e6;
                    case 3: goto L_0x04cc;
                    case 4: goto L_0x04b6;
                    case 5: goto L_0x0493;
                    case 6: goto L_0x0481;
                    case 7: goto L_0x046b;
                    case 8: goto L_0x0455;
                    case 9: goto L_0x043b;
                    case 10: goto L_0x0421;
                    case 11: goto L_0x0403;
                    case 12: goto L_0x03e9;
                    case 13: goto L_0x03b3;
                    case 14: goto L_0x0357;
                    case 15: goto L_0x0345;
                    case 16: goto L_0x032a;
                    case 17: goto L_0x0310;
                    case 18: goto L_0x02f6;
                    case 19: goto L_0x02ce;
                    case 20: goto L_0x02bc;
                    case 21: goto L_0x02aa;
                    case 22: goto L_0x0294;
                    case 23: goto L_0x026b;
                    case 24: goto L_0x01e5;
                    case 25: goto L_0x01b9;
                    case 26: goto L_0x018d;
                    case 27: goto L_0x017f;
                    case 28: goto L_0x014c;
                    case 29: goto L_0x0119;
                    case 30: goto L_0x00ed;
                    case 31: goto L_0x00c8;
                    case 32: goto L_0x00a3;
                    case 33: goto L_0x0095;
                    case 34: goto L_0x0087;
                    case 35: goto L_0x006d;
                    case 36: goto L_0x0053;
                    case 37: goto L_0x0041;
                    case 38: goto L_0x0033;
                    case 39: goto L_0x0019;
                    default: goto L_0x0014;
                }
            L_0x0014:
                boolean r0 = super.onTransact(r45, r46, r47, r48)
                return r0
            L_0x0019:
                r13.enforceInterface(r11)
                int r0 = r46.readInt()
                int r1 = r46.readInt()
                java.lang.String r2 = r46.readString()
                boolean r3 = r15.ipTableConfig(r0, r1, r2)
                r47.writeNoException()
                r12.writeInt(r3)
                return r10
            L_0x0033:
                r13.enforceInterface(r11)
                boolean r0 = r44.trafficCheckBpfStatsEnable()
                r47.writeNoException()
                r12.writeInt(r0)
                return r10
            L_0x0041:
                r13.enforceInterface(r11)
                java.lang.String r0 = r46.readString()
                int r1 = r46.readInt()
                r15.setIPv6AddrGenMode(r0, r1)
                r47.writeNoException()
                return r10
            L_0x0053:
                r13.enforceInterface(r11)
                java.lang.String r0 = r46.readString()
                java.lang.String r1 = r46.readString()
                int r2 = r46.readInt()
                int r3 = r46.readInt()
                r15.wakeupDelInterface(r0, r1, r2, r3)
                r47.writeNoException()
                return r10
            L_0x006d:
                r13.enforceInterface(r11)
                java.lang.String r0 = r46.readString()
                java.lang.String r1 = r46.readString()
                int r2 = r46.readInt()
                int r3 = r46.readInt()
                r15.wakeupAddInterface(r0, r1, r2, r3)
                r47.writeNoException()
                return r10
            L_0x0087:
                r13.enforceInterface(r11)
                int r0 = r44.getNetdPid()
                r47.writeNoException()
                r12.writeInt(r0)
                return r10
            L_0x0095:
                r13.enforceInterface(r11)
                java.lang.String r0 = r46.readString()
                r15.removeVirtualTunnelInterface(r0)
                r47.writeNoException()
                return r10
            L_0x00a3:
                r13.enforceInterface(r11)
                java.lang.String r6 = r46.readString()
                java.lang.String r7 = r46.readString()
                java.lang.String r8 = r46.readString()
                int r9 = r46.readInt()
                int r16 = r46.readInt()
                r0 = r15
                r1 = r6
                r2 = r7
                r3 = r8
                r4 = r9
                r5 = r16
                r0.updateVirtualTunnelInterface(r1, r2, r3, r4, r5)
                r47.writeNoException()
                return r10
            L_0x00c8:
                r13.enforceInterface(r11)
                java.lang.String r6 = r46.readString()
                java.lang.String r7 = r46.readString()
                java.lang.String r8 = r46.readString()
                int r9 = r46.readInt()
                int r16 = r46.readInt()
                r0 = r15
                r1 = r6
                r2 = r7
                r3 = r8
                r4 = r9
                r5 = r16
                r0.addVirtualTunnelInterface(r1, r2, r3, r4, r5)
                r47.writeNoException()
                return r10
            L_0x00ed:
                r13.enforceInterface(r11)
                int r7 = r46.readInt()
                int r8 = r46.readInt()
                java.lang.String r9 = r46.readString()
                java.lang.String r16 = r46.readString()
                int r17 = r46.readInt()
                int r18 = r46.readInt()
                r0 = r15
                r1 = r7
                r2 = r8
                r3 = r9
                r4 = r16
                r5 = r17
                r6 = r18
                r0.ipSecDeleteSecurityPolicy(r1, r2, r3, r4, r5, r6)
                r47.writeNoException()
                return r10
            L_0x0119:
                r13.enforceInterface(r11)
                int r8 = r46.readInt()
                int r9 = r46.readInt()
                java.lang.String r16 = r46.readString()
                java.lang.String r17 = r46.readString()
                int r18 = r46.readInt()
                int r19 = r46.readInt()
                int r20 = r46.readInt()
                r0 = r15
                r1 = r8
                r2 = r9
                r3 = r16
                r4 = r17
                r5 = r18
                r6 = r19
                r7 = r20
                r0.ipSecUpdateSecurityPolicy(r1, r2, r3, r4, r5, r6, r7)
                r47.writeNoException()
                return r10
            L_0x014c:
                r13.enforceInterface(r11)
                int r8 = r46.readInt()
                int r9 = r46.readInt()
                java.lang.String r16 = r46.readString()
                java.lang.String r17 = r46.readString()
                int r18 = r46.readInt()
                int r19 = r46.readInt()
                int r20 = r46.readInt()
                r0 = r15
                r1 = r8
                r2 = r9
                r3 = r16
                r4 = r17
                r5 = r18
                r6 = r19
                r7 = r20
                r0.ipSecAddSecurityPolicy(r1, r2, r3, r4, r5, r6, r7)
                r47.writeNoException()
                return r10
            L_0x017f:
                r13.enforceInterface(r11)
                java.io.FileDescriptor r0 = r46.readRawFileDescriptor()
                r15.ipSecRemoveTransportModeTransform(r0)
                r47.writeNoException()
                return r10
            L_0x018d:
                r13.enforceInterface(r11)
                java.io.FileDescriptor r7 = r46.readRawFileDescriptor()
                int r8 = r46.readInt()
                int r9 = r46.readInt()
                java.lang.String r16 = r46.readString()
                java.lang.String r17 = r46.readString()
                int r18 = r46.readInt()
                r0 = r15
                r1 = r7
                r2 = r8
                r3 = r9
                r4 = r16
                r5 = r17
                r6 = r18
                r0.ipSecApplyTransportModeTransform(r1, r2, r3, r4, r5, r6)
                r47.writeNoException()
                return r10
            L_0x01b9:
                r13.enforceInterface(r11)
                int r7 = r46.readInt()
                java.lang.String r8 = r46.readString()
                java.lang.String r9 = r46.readString()
                int r16 = r46.readInt()
                int r17 = r46.readInt()
                int r18 = r46.readInt()
                r0 = r15
                r1 = r7
                r2 = r8
                r3 = r9
                r4 = r16
                r5 = r17
                r6 = r18
                r0.ipSecDeleteSecurityAssociation(r1, r2, r3, r4, r5, r6)
                r47.writeNoException()
                return r10
            L_0x01e5:
                r13.enforceInterface(r11)
                int r21 = r46.readInt()
                int r22 = r46.readInt()
                java.lang.String r23 = r46.readString()
                java.lang.String r24 = r46.readString()
                int r25 = r46.readInt()
                int r26 = r46.readInt()
                int r27 = r46.readInt()
                int r28 = r46.readInt()
                java.lang.String r29 = r46.readString()
                byte[] r30 = r46.createByteArray()
                int r31 = r46.readInt()
                java.lang.String r32 = r46.readString()
                byte[] r33 = r46.createByteArray()
                int r34 = r46.readInt()
                java.lang.String r35 = r46.readString()
                byte[] r36 = r46.createByteArray()
                int r37 = r46.readInt()
                int r38 = r46.readInt()
                int r39 = r46.readInt()
                int r40 = r46.readInt()
                r0 = r15
                r1 = r21
                r2 = r22
                r3 = r23
                r4 = r24
                r5 = r25
                r6 = r26
                r7 = r27
                r8 = r28
                r9 = r29
                r10 = r30
                r41 = r11
                r11 = r31
                r12 = r32
                r13 = r33
                r14 = r34
                r15 = r35
                r16 = r36
                r17 = r37
                r18 = r38
                r19 = r39
                r20 = r40
                r0.ipSecAddSecurityAssociation(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19, r20)
                r47.writeNoException()
                r8 = 1
                return r8
            L_0x026b:
                r8 = r10
                r41 = r11
                r10 = r41
                r9 = r46
                r9.enforceInterface(r10)
                int r0 = r46.readInt()
                java.lang.String r1 = r46.readString()
                java.lang.String r2 = r46.readString()
                int r3 = r46.readInt()
                r11 = r44
                int r4 = r11.ipSecAllocateSpi(r0, r1, r2, r3)
                r47.writeNoException()
                r12 = r47
                r12.writeInt(r4)
                return r8
            L_0x0294:
                r8 = r10
                r10 = r11
                r9 = r13
                r11 = r15
                r9.enforceInterface(r10)
                java.io.FileDescriptor r0 = r46.readRawFileDescriptor()
                int r1 = r46.readInt()
                r11.ipSecSetEncapSocketOwner(r0, r1)
                r47.writeNoException()
                return r8
            L_0x02aa:
                r8 = r10
                r10 = r11
                r9 = r13
                r11 = r15
                r9.enforceInterface(r10)
                int r0 = r46.readInt()
                r11.setMetricsReportingLevel(r0)
                r47.writeNoException()
                return r8
            L_0x02bc:
                r8 = r10
                r10 = r11
                r9 = r13
                r11 = r15
                r9.enforceInterface(r10)
                int r0 = r44.getMetricsReportingLevel()
                r47.writeNoException()
                r12.writeInt(r0)
                return r8
            L_0x02ce:
                r8 = r10
                r10 = r11
                r9 = r13
                r11 = r15
                r9.enforceInterface(r10)
                int r6 = r46.readInt()
                int r7 = r46.readInt()
                java.lang.String r13 = r46.readString()
                java.lang.String r14 = r46.readString()
                java.lang.String r15 = r46.readString()
                r0 = r11
                r1 = r6
                r2 = r7
                r3 = r13
                r4 = r14
                r5 = r15
                r0.setProcSysNet(r1, r2, r3, r4, r5)
                r47.writeNoException()
                return r8
            L_0x02f6:
                r8 = r10
                r10 = r11
                r9 = r13
                r11 = r15
                r9.enforceInterface(r10)
                java.lang.String r0 = r46.readString()
                java.lang.String r1 = r46.readString()
                int r2 = r46.readInt()
                r11.interfaceDelAddress(r0, r1, r2)
                r47.writeNoException()
                return r8
            L_0x0310:
                r8 = r10
                r10 = r11
                r9 = r13
                r11 = r15
                r9.enforceInterface(r10)
                java.lang.String r0 = r46.readString()
                java.lang.String r1 = r46.readString()
                int r2 = r46.readInt()
                r11.interfaceAddAddress(r0, r1, r2)
                r47.writeNoException()
                return r8
            L_0x032a:
                r8 = r10
                r10 = r11
                r9 = r13
                r11 = r15
                r9.enforceInterface(r10)
                android.os.PersistableBundle r1 = r44.tetherGetStats()
                r47.writeNoException()
                if (r1 == 0) goto L_0x0341
                r12.writeInt(r8)
                r1.writeToParcel(r12, r8)
                goto L_0x0344
            L_0x0341:
                r12.writeInt(r0)
            L_0x0344:
                return r8
            L_0x0345:
                r8 = r10
                r10 = r11
                r9 = r13
                r11 = r15
                r9.enforceInterface(r10)
                boolean r0 = r44.tetherApplyDnsInterfaces()
                r47.writeNoException()
                r12.writeInt(r0)
                return r8
            L_0x0357:
                r8 = r10
                r10 = r11
                r9 = r13
                r11 = r15
                r9.enforceInterface(r10)
                int r6 = r46.readInt()
                int r7 = r46.readInt()
                if (r7 >= 0) goto L_0x036a
                r0 = 0
                goto L_0x036c
            L_0x036a:
                java.lang.String[] r0 = new java.lang.String[r7]
            L_0x036c:
                r13 = r0
                int r14 = r46.readInt()
                if (r14 >= 0) goto L_0x0375
                r0 = 0
                goto L_0x0377
            L_0x0375:
                java.lang.String[] r0 = new java.lang.String[r14]
            L_0x0377:
                r15 = r0
                int r5 = r46.readInt()
                if (r5 >= 0) goto L_0x0380
                r0 = 0
                goto L_0x0382
            L_0x0380:
                int[] r0 = new int[r5]
            L_0x0382:
                r4 = r0
                int r3 = r46.readInt()
                if (r3 >= 0) goto L_0x038b
                r0 = 0
                goto L_0x038d
            L_0x038b:
                int[] r0 = new int[r3]
            L_0x038d:
                r2 = r0
                r0 = r11
                r1 = r6
                r42 = r2
                r2 = r13
                r16 = r3
                r3 = r15
                r43 = r4
                r17 = r5
                r5 = r42
                r0.getResolverInfo(r1, r2, r3, r4, r5)
                r47.writeNoException()
                r12.writeStringArray(r13)
                r12.writeStringArray(r15)
                r0 = r43
                r12.writeIntArray(r0)
                r1 = r42
                r12.writeIntArray(r1)
                return r8
            L_0x03b3:
                r8 = r10
                r10 = r11
                r9 = r13
                r11 = r15
                r9.enforceInterface(r10)
                int r13 = r46.readInt()
                java.lang.String[] r14 = r46.createStringArray()
                java.lang.String[] r15 = r46.createStringArray()
                int[] r16 = r46.createIntArray()
                java.lang.String r17 = r46.readString()
                java.lang.String[] r18 = r46.createStringArray()
                java.lang.String[] r19 = r46.createStringArray()
                r0 = r11
                r1 = r13
                r2 = r14
                r3 = r15
                r4 = r16
                r5 = r17
                r6 = r18
                r7 = r19
                r0.setResolverConfiguration(r1, r2, r3, r4, r5, r6, r7)
                r47.writeNoException()
                return r8
            L_0x03e9:
                r8 = r10
                r10 = r11
                r9 = r13
                r11 = r15
                r9.enforceInterface(r10)
                android.os.Parcelable$Creator r0 = android.net.UidRange.CREATOR
                java.lang.Object[] r0 = r9.createTypedArray(r0)
                android.net.UidRange[] r0 = (android.net.UidRange[]) r0
                int[] r1 = r46.createIntArray()
                r11.socketDestroy(r0, r1)
                r47.writeNoException()
                return r8
            L_0x0403:
                r8 = r10
                r10 = r11
                r9 = r13
                r11 = r15
                r9.enforceInterface(r10)
                int r1 = r46.readInt()
                if (r1 == 0) goto L_0x0412
                r0 = r8
            L_0x0412:
                android.os.Parcelable$Creator r1 = android.net.UidRange.CREATOR
                java.lang.Object[] r1 = r9.createTypedArray(r1)
                android.net.UidRange[] r1 = (android.net.UidRange[]) r1
                r11.networkRejectNonSecureVpn(r0, r1)
                r47.writeNoException()
                return r8
            L_0x0421:
                r8 = r10
                r10 = r11
                r9 = r13
                r11 = r15
                r9.enforceInterface(r10)
                int r0 = r46.readInt()
                android.os.Parcelable$Creator r1 = android.net.UidRange.CREATOR
                java.lang.Object[] r1 = r9.createTypedArray(r1)
                android.net.UidRange[] r1 = (android.net.UidRange[]) r1
                r11.networkRemoveUidRanges(r0, r1)
                r47.writeNoException()
                return r8
            L_0x043b:
                r8 = r10
                r10 = r11
                r9 = r13
                r11 = r15
                r9.enforceInterface(r10)
                int r0 = r46.readInt()
                android.os.Parcelable$Creator r1 = android.net.UidRange.CREATOR
                java.lang.Object[] r1 = r9.createTypedArray(r1)
                android.net.UidRange[] r1 = (android.net.UidRange[]) r1
                r11.networkAddUidRanges(r0, r1)
                r47.writeNoException()
                return r8
            L_0x0455:
                r8 = r10
                r10 = r11
                r9 = r13
                r11 = r15
                r9.enforceInterface(r10)
                int r0 = r46.readInt()
                java.lang.String r1 = r46.readString()
                r11.networkRemoveInterface(r0, r1)
                r47.writeNoException()
                return r8
            L_0x046b:
                r8 = r10
                r10 = r11
                r9 = r13
                r11 = r15
                r9.enforceInterface(r10)
                int r0 = r46.readInt()
                java.lang.String r1 = r46.readString()
                r11.networkAddInterface(r0, r1)
                r47.writeNoException()
                return r8
            L_0x0481:
                r8 = r10
                r10 = r11
                r9 = r13
                r11 = r15
                r9.enforceInterface(r10)
                int r0 = r46.readInt()
                r11.networkDestroy(r0)
                r47.writeNoException()
                return r8
            L_0x0493:
                r8 = r10
                r10 = r11
                r9 = r13
                r11 = r15
                r9.enforceInterface(r10)
                int r1 = r46.readInt()
                int r2 = r46.readInt()
                if (r2 == 0) goto L_0x04a6
                r2 = r8
                goto L_0x04a7
            L_0x04a6:
                r2 = r0
            L_0x04a7:
                int r3 = r46.readInt()
                if (r3 == 0) goto L_0x04af
                r0 = r8
            L_0x04af:
                r11.networkCreateVpn(r1, r2, r0)
                r47.writeNoException()
                return r8
            L_0x04b6:
                r8 = r10
                r10 = r11
                r9 = r13
                r11 = r15
                r9.enforceInterface(r10)
                int r0 = r46.readInt()
                java.lang.String r1 = r46.readString()
                r11.networkCreatePhysical(r0, r1)
                r47.writeNoException()
                return r8
            L_0x04cc:
                r8 = r10
                r10 = r11
                r9 = r13
                r11 = r15
                r9.enforceInterface(r10)
                int r1 = r46.readInt()
                if (r1 == 0) goto L_0x04db
                r0 = r8
            L_0x04db:
                boolean r1 = r11.bandwidthEnableDataSaver(r0)
                r47.writeNoException()
                r12.writeInt(r1)
                return r8
            L_0x04e6:
                r8 = r10
                r10 = r11
                r9 = r13
                r11 = r15
                r9.enforceInterface(r10)
                java.lang.String r1 = r46.readString()
                int r2 = r46.readInt()
                if (r2 == 0) goto L_0x04f9
                r0 = r8
            L_0x04f9:
                int[] r2 = r46.createIntArray()
                boolean r3 = r11.firewallReplaceUidChain(r1, r0, r2)
                r47.writeNoException()
                r12.writeInt(r3)
                return r8
            L_0x0508:
                r8 = r10
                r10 = r11
                r9 = r13
                r11 = r15
                r9.enforceInterface(r10)
                boolean r0 = r44.isAlive()
                r47.writeNoException()
                r12.writeInt(r0)
                return r8
            L_0x051a:
                r8 = r10
                r10 = r11
                r9 = r13
                r11 = r15
                r12.writeString(r10)
                return r8
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.INetd.Stub.onTransact(int, android.os.Parcel, android.os.Parcel, int):boolean");
        }
    }

    void addVirtualTunnelInterface(String str, String str2, String str3, int i, int i2) throws RemoteException;

    boolean bandwidthEnableDataSaver(boolean z) throws RemoteException;

    boolean firewallReplaceUidChain(String str, boolean z, int[] iArr) throws RemoteException;

    int getMetricsReportingLevel() throws RemoteException;

    int getNetdPid() throws RemoteException;

    void getResolverInfo(int i, String[] strArr, String[] strArr2, int[] iArr, int[] iArr2) throws RemoteException;

    void interfaceAddAddress(String str, String str2, int i) throws RemoteException;

    void interfaceDelAddress(String str, String str2, int i) throws RemoteException;

    void ipSecAddSecurityAssociation(int i, int i2, String str, String str2, int i3, int i4, int i5, int i6, String str3, byte[] bArr, int i7, String str4, byte[] bArr2, int i8, String str5, byte[] bArr3, int i9, int i10, int i11, int i12) throws RemoteException;

    void ipSecAddSecurityPolicy(int i, int i2, String str, String str2, int i3, int i4, int i5) throws RemoteException;

    int ipSecAllocateSpi(int i, String str, String str2, int i2) throws RemoteException;

    void ipSecApplyTransportModeTransform(FileDescriptor fileDescriptor, int i, int i2, String str, String str2, int i3) throws RemoteException;

    void ipSecDeleteSecurityAssociation(int i, String str, String str2, int i2, int i3, int i4) throws RemoteException;

    void ipSecDeleteSecurityPolicy(int i, int i2, String str, String str2, int i3, int i4) throws RemoteException;

    void ipSecRemoveTransportModeTransform(FileDescriptor fileDescriptor) throws RemoteException;

    void ipSecSetEncapSocketOwner(FileDescriptor fileDescriptor, int i) throws RemoteException;

    void ipSecUpdateSecurityPolicy(int i, int i2, String str, String str2, int i3, int i4, int i5) throws RemoteException;

    boolean ipTableConfig(int i, int i2, String str) throws RemoteException;

    boolean isAlive() throws RemoteException;

    void networkAddInterface(int i, String str) throws RemoteException;

    void networkAddUidRanges(int i, UidRange[] uidRangeArr) throws RemoteException;

    void networkCreatePhysical(int i, String str) throws RemoteException;

    void networkCreateVpn(int i, boolean z, boolean z2) throws RemoteException;

    void networkDestroy(int i) throws RemoteException;

    void networkRejectNonSecureVpn(boolean z, UidRange[] uidRangeArr) throws RemoteException;

    void networkRemoveInterface(int i, String str) throws RemoteException;

    void networkRemoveUidRanges(int i, UidRange[] uidRangeArr) throws RemoteException;

    void removeVirtualTunnelInterface(String str) throws RemoteException;

    void setIPv6AddrGenMode(String str, int i) throws RemoteException;

    void setMetricsReportingLevel(int i) throws RemoteException;

    void setProcSysNet(int i, int i2, String str, String str2, String str3) throws RemoteException;

    void setResolverConfiguration(int i, String[] strArr, String[] strArr2, int[] iArr, String str, String[] strArr3, String[] strArr4) throws RemoteException;

    void socketDestroy(UidRange[] uidRangeArr, int[] iArr) throws RemoteException;

    boolean tetherApplyDnsInterfaces() throws RemoteException;

    PersistableBundle tetherGetStats() throws RemoteException;

    boolean trafficCheckBpfStatsEnable() throws RemoteException;

    void updateVirtualTunnelInterface(String str, String str2, String str3, int i, int i2) throws RemoteException;

    void wakeupAddInterface(String str, String str2, int i, int i2) throws RemoteException;

    void wakeupDelInterface(String str, String str2, int i, int i2) throws RemoteException;
}
