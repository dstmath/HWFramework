package android.net;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INetd extends IInterface {
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
        static final int TRANSACTION_getResolverInfo = 7;
        static final int TRANSACTION_isAlive = 1;
        static final int TRANSACTION_networkRejectNonSecureVpn = 4;
        static final int TRANSACTION_setResolverConfiguration = 6;
        static final int TRANSACTION_socketDestroy = 5;

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
                    this.mRemote.transact(Stub.TRANSACTION_isAlive, _data, _reply, INetd.RESOLVER_STATS_SUCCESSES);
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
                int i = INetd.RESOLVER_STATS_SUCCESSES;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(chainName);
                    if (isWhitelist) {
                        i = Stub.TRANSACTION_isAlive;
                    }
                    _data.writeInt(i);
                    _data.writeIntArray(uids);
                    this.mRemote.transact(Stub.TRANSACTION_firewallReplaceUidChain, _data, _reply, INetd.RESOLVER_STATS_SUCCESSES);
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
                int i = INetd.RESOLVER_STATS_SUCCESSES;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enable) {
                        i = Stub.TRANSACTION_isAlive;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_bandwidthEnableDataSaver, _data, _reply, INetd.RESOLVER_STATS_SUCCESSES);
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
                int i = INetd.RESOLVER_STATS_SUCCESSES;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (add) {
                        i = Stub.TRANSACTION_isAlive;
                    }
                    _data.writeInt(i);
                    _data.writeTypedArray(uidRanges, INetd.RESOLVER_STATS_SUCCESSES);
                    this.mRemote.transact(Stub.TRANSACTION_networkRejectNonSecureVpn, _data, _reply, INetd.RESOLVER_STATS_SUCCESSES);
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
                    _data.writeTypedArray(uidRanges, INetd.RESOLVER_STATS_SUCCESSES);
                    _data.writeIntArray(exemptUids);
                    this.mRemote.transact(Stub.TRANSACTION_socketDestroy, _data, _reply, INetd.RESOLVER_STATS_SUCCESSES);
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
                    this.mRemote.transact(Stub.TRANSACTION_setResolverConfiguration, _data, _reply, INetd.RESOLVER_STATS_SUCCESSES);
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
                    this.mRemote.transact(Stub.TRANSACTION_getResolverInfo, _data, _reply, INetd.RESOLVER_STATS_SUCCESSES);
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
            switch (code) {
                case TRANSACTION_isAlive /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isAlive();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_isAlive : INetd.RESOLVER_STATS_SUCCESSES);
                    return true;
                case TRANSACTION_firewallReplaceUidChain /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = firewallReplaceUidChain(data.readString(), data.readInt() != 0, data.createIntArray());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_isAlive : INetd.RESOLVER_STATS_SUCCESSES);
                    return true;
                case TRANSACTION_bandwidthEnableDataSaver /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = bandwidthEnableDataSaver(data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_isAlive : INetd.RESOLVER_STATS_SUCCESSES);
                    return true;
                case TRANSACTION_networkRejectNonSecureVpn /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    networkRejectNonSecureVpn(data.readInt() != 0, (UidRange[]) data.createTypedArray(UidRange.CREATOR));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_socketDestroy /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    socketDestroy((UidRange[]) data.createTypedArray(UidRange.CREATOR), data.createIntArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setResolverConfiguration /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    setResolverConfiguration(data.readInt(), data.createStringArray(), data.createStringArray(), data.createIntArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getResolverInfo /*7*/:
                    String[] strArr;
                    String[] strArr2;
                    int[] iArr;
                    int[] iArr2;
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
                        iArr2 = null;
                    } else {
                        iArr2 = new int[_arg4_length];
                    }
                    getResolverInfo(_arg0, strArr, strArr2, iArr, iArr2);
                    reply.writeNoException();
                    reply.writeStringArray(strArr);
                    reply.writeStringArray(strArr2);
                    reply.writeIntArray(iArr);
                    reply.writeIntArray(iArr2);
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

    void getResolverInfo(int i, String[] strArr, String[] strArr2, int[] iArr, int[] iArr2) throws RemoteException;

    boolean isAlive() throws RemoteException;

    void networkRejectNonSecureVpn(boolean z, UidRange[] uidRangeArr) throws RemoteException;

    void setResolverConfiguration(int i, String[] strArr, String[] strArr2, int[] iArr) throws RemoteException;

    void socketDestroy(UidRange[] uidRangeArr, int[] iArr) throws RemoteException;
}
