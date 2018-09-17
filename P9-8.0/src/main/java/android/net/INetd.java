package android.net;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.io.FileDescriptor;

public interface INetd extends IInterface {
    public static final int CONF = 1;
    public static final int IPV4 = 4;
    public static final int IPV6 = 6;
    public static final int NEIGH = 2;
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

    public static abstract class Stub extends Binder implements INetd {
        private static final String DESCRIPTOR = "android.net.INetd";
        static final int TRANSACTION_bandwidthEnableDataSaver = 3;
        static final int TRANSACTION_firewallReplaceUidChain = 2;
        static final int TRANSACTION_getMetricsReportingLevel = 12;
        static final int TRANSACTION_getNetdPid = 19;
        static final int TRANSACTION_getResolverInfo = 7;
        static final int TRANSACTION_interfaceAddAddress = 9;
        static final int TRANSACTION_interfaceDelAddress = 10;
        static final int TRANSACTION_ipSecAddSecurityAssociation = 15;
        static final int TRANSACTION_ipSecAllocateSpi = 14;
        static final int TRANSACTION_ipSecApplyTransportModeTransform = 17;
        static final int TRANSACTION_ipSecDeleteSecurityAssociation = 16;
        static final int TRANSACTION_ipSecRemoveTransportModeTransform = 18;
        static final int TRANSACTION_isAlive = 1;
        static final int TRANSACTION_networkRejectNonSecureVpn = 4;
        static final int TRANSACTION_setMetricsReportingLevel = 13;
        static final int TRANSACTION_setProcSysNet = 11;
        static final int TRANSACTION_setResolverConfiguration = 6;
        static final int TRANSACTION_socketDestroy = 5;
        static final int TRANSACTION_tetherApplyDnsInterfaces = 8;

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
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean firewallReplaceUidChain(String chainName, boolean isWhitelist, int[] uids) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(chainName);
                    if (isWhitelist) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeIntArray(uids);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean bandwidthEnableDataSaver(boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enable) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void networkRejectNonSecureVpn(boolean add, UidRange[] uidRanges) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (add) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeTypedArray(uidRanges, 0);
                    this.mRemote.transact(4, _data, _reply, 0);
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
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setResolverConfiguration(int netId, String[] servers, String[] domains, int[] params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(netId);
                    _data.writeStringArray(servers);
                    _data.writeStringArray(domains);
                    _data.writeIntArray(params);
                    this.mRemote.transact(6, _data, _reply, 0);
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
                    this.mRemote.transact(7, _data, _reply, 0);
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
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
                    this.mRemote.transact(9, _data, _reply, 0);
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
                    this.mRemote.transact(10, _data, _reply, 0);
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
                    this.mRemote.transact(11, _data, _reply, 0);
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
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int ipSecAllocateSpi(int transformId, int direction, String localAddress, String remoteAddress, int spi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(transformId);
                    _data.writeInt(direction);
                    _data.writeString(localAddress);
                    _data.writeString(remoteAddress);
                    _data.writeInt(spi);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int ipSecAddSecurityAssociation(int transformId, int mode, int direction, String localAddress, String remoteAddress, long underlyingNetworkHandle, int spi, String authAlgo, byte[] authKey, int authTruncBits, String cryptAlgo, byte[] cryptKey, int cryptTruncBits, int encapType, int encapLocalPort, int encapRemotePort) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(transformId);
                    _data.writeInt(mode);
                    _data.writeInt(direction);
                    _data.writeString(localAddress);
                    _data.writeString(remoteAddress);
                    _data.writeLong(underlyingNetworkHandle);
                    _data.writeInt(spi);
                    _data.writeString(authAlgo);
                    _data.writeByteArray(authKey);
                    _data.writeInt(authTruncBits);
                    _data.writeString(cryptAlgo);
                    _data.writeByteArray(cryptKey);
                    _data.writeInt(cryptTruncBits);
                    _data.writeInt(encapType);
                    _data.writeInt(encapLocalPort);
                    _data.writeInt(encapRemotePort);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void ipSecDeleteSecurityAssociation(int transformId, int direction, String localAddress, String remoteAddress, int spi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(transformId);
                    _data.writeInt(direction);
                    _data.writeString(localAddress);
                    _data.writeString(remoteAddress);
                    _data.writeInt(spi);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void ipSecApplyTransportModeTransform(FileDescriptor socket, int transformId, int direction, String localAddress, String remoteAddress, int spi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeRawFileDescriptor(socket);
                    _data.writeInt(transformId);
                    _data.writeInt(direction);
                    _data.writeString(localAddress);
                    _data.writeString(remoteAddress);
                    _data.writeInt(spi);
                    this.mRemote.transact(17, _data, _reply, 0);
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
                    this.mRemote.transact(18, _data, _reply, 0);
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
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            int _result2;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isAlive();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _result = firewallReplaceUidChain(data.readString(), data.readInt() != 0, data.createIntArray());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _result = bandwidthEnableDataSaver(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    networkRejectNonSecureVpn(data.readInt() != 0, (UidRange[]) data.createTypedArray(UidRange.CREATOR));
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    socketDestroy((UidRange[]) data.createTypedArray(UidRange.CREATOR), data.createIntArray());
                    reply.writeNoException();
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    setResolverConfiguration(data.readInt(), data.createStringArray(), data.createStringArray(), data.createIntArray());
                    reply.writeNoException();
                    return true;
                case 7:
                    String[] strArr;
                    String[] strArr2;
                    int[] iArr;
                    int[] _arg4;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    int _arg1_length = data.readInt();
                    if (_arg1_length < 0) {
                        strArr = null;
                    } else {
                        strArr = new String[_arg1_length];
                    }
                    int _arg2_length = data.readInt();
                    if (_arg2_length < 0) {
                        strArr2 = null;
                    } else {
                        strArr2 = new String[_arg2_length];
                    }
                    int _arg3_length = data.readInt();
                    if (_arg3_length < 0) {
                        iArr = null;
                    } else {
                        iArr = new int[_arg3_length];
                    }
                    int _arg4_length = data.readInt();
                    if (_arg4_length < 0) {
                        _arg4 = null;
                    } else {
                        _arg4 = new int[_arg4_length];
                    }
                    getResolverInfo(_arg0, strArr, strArr2, iArr, _arg4);
                    reply.writeNoException();
                    reply.writeStringArray(strArr);
                    reply.writeStringArray(strArr2);
                    reply.writeIntArray(iArr);
                    reply.writeIntArray(_arg4);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    _result = tetherApplyDnsInterfaces();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    interfaceAddAddress(data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    interfaceDelAddress(data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    setProcSysNet(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getMetricsReportingLevel();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    setMetricsReportingLevel(data.readInt());
                    reply.writeNoException();
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = ipSecAllocateSpi(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = ipSecAddSecurityAssociation(data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readString(), data.readLong(), data.readInt(), data.readString(), data.createByteArray(), data.readInt(), data.readString(), data.createByteArray(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    ipSecDeleteSecurityAssociation(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    ipSecApplyTransportModeTransform(data.readRawFileDescriptor(), data.readInt(), data.readInt(), data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    ipSecRemoveTransportModeTransform(data.readRawFileDescriptor());
                    reply.writeNoException();
                    return true;
                case 19:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getNetdPid();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean bandwidthEnableDataSaver(boolean z) throws RemoteException;

    boolean firewallReplaceUidChain(String str, boolean z, int[] iArr) throws RemoteException;

    int getMetricsReportingLevel() throws RemoteException;

    int getNetdPid() throws RemoteException;

    void getResolverInfo(int i, String[] strArr, String[] strArr2, int[] iArr, int[] iArr2) throws RemoteException;

    void interfaceAddAddress(String str, String str2, int i) throws RemoteException;

    void interfaceDelAddress(String str, String str2, int i) throws RemoteException;

    int ipSecAddSecurityAssociation(int i, int i2, int i3, String str, String str2, long j, int i4, String str3, byte[] bArr, int i5, String str4, byte[] bArr2, int i6, int i7, int i8, int i9) throws RemoteException;

    int ipSecAllocateSpi(int i, int i2, String str, String str2, int i3) throws RemoteException;

    void ipSecApplyTransportModeTransform(FileDescriptor fileDescriptor, int i, int i2, String str, String str2, int i3) throws RemoteException;

    void ipSecDeleteSecurityAssociation(int i, int i2, String str, String str2, int i3) throws RemoteException;

    void ipSecRemoveTransportModeTransform(FileDescriptor fileDescriptor) throws RemoteException;

    boolean isAlive() throws RemoteException;

    void networkRejectNonSecureVpn(boolean z, UidRange[] uidRangeArr) throws RemoteException;

    void setMetricsReportingLevel(int i) throws RemoteException;

    void setProcSysNet(int i, int i2, String str, String str2, String str3) throws RemoteException;

    void setResolverConfiguration(int i, String[] strArr, String[] strArr2, int[] iArr) throws RemoteException;

    void socketDestroy(UidRange[] uidRangeArr, int[] iArr) throws RemoteException;

    boolean tetherApplyDnsInterfaces() throws RemoteException;
}
