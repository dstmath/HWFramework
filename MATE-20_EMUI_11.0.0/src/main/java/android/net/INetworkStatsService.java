package android.net;

import android.annotation.UnsupportedAppUsage;
import android.net.INetworkStatsSession;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Messenger;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import com.android.internal.net.VpnInfo;

public interface INetworkStatsService extends IInterface {
    @UnsupportedAppUsage
    void forceUpdate() throws RemoteException;

    void forceUpdateIfaces(Network[] networkArr, VpnInfo[] vpnInfoArr, NetworkState[] networkStateArr, String str) throws RemoteException;

    @UnsupportedAppUsage
    NetworkStats getDataLayerSnapshotForUid(int i) throws RemoteException;

    NetworkStats getDetailedUidStats(String[] strArr) throws RemoteException;

    IBinder getHwInnerService() throws RemoteException;

    long getIfaceStats(String str, int i) throws RemoteException;

    @UnsupportedAppUsage
    String[] getMobileIfaces() throws RemoteException;

    long getTotalStats(int i) throws RemoteException;

    long getUidStats(int i, int i2) throws RemoteException;

    void incrementOperationCount(int i, int i2, int i3) throws RemoteException;

    @UnsupportedAppUsage
    INetworkStatsSession openSession() throws RemoteException;

    @UnsupportedAppUsage
    INetworkStatsSession openSessionForUsageStats(int i, String str) throws RemoteException;

    DataUsageRequest registerUsageCallback(String str, DataUsageRequest dataUsageRequest, Messenger messenger, IBinder iBinder) throws RemoteException;

    int tagSocket(ParcelFileDescriptor parcelFileDescriptor, int i, int i2) throws RemoteException;

    void unregisterUsageRequest(DataUsageRequest dataUsageRequest) throws RemoteException;

    public static class Default implements INetworkStatsService {
        @Override // android.net.INetworkStatsService
        public INetworkStatsSession openSession() throws RemoteException {
            return null;
        }

        @Override // android.net.INetworkStatsService
        public INetworkStatsSession openSessionForUsageStats(int flags, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.net.INetworkStatsService
        public NetworkStats getDataLayerSnapshotForUid(int uid) throws RemoteException {
            return null;
        }

        @Override // android.net.INetworkStatsService
        public NetworkStats getDetailedUidStats(String[] requiredIfaces) throws RemoteException {
            return null;
        }

        @Override // android.net.INetworkStatsService
        public String[] getMobileIfaces() throws RemoteException {
            return null;
        }

        @Override // android.net.INetworkStatsService
        public void incrementOperationCount(int uid, int tag, int operationCount) throws RemoteException {
        }

        @Override // android.net.INetworkStatsService
        public void forceUpdateIfaces(Network[] defaultNetworks, VpnInfo[] vpnArray, NetworkState[] networkStates, String activeIface) throws RemoteException {
        }

        @Override // android.net.INetworkStatsService
        public void forceUpdate() throws RemoteException {
        }

        @Override // android.net.INetworkStatsService
        public DataUsageRequest registerUsageCallback(String callingPackage, DataUsageRequest request, Messenger messenger, IBinder binder) throws RemoteException {
            return null;
        }

        @Override // android.net.INetworkStatsService
        public void unregisterUsageRequest(DataUsageRequest request) throws RemoteException {
        }

        @Override // android.net.INetworkStatsService
        public long getUidStats(int uid, int type) throws RemoteException {
            return 0;
        }

        @Override // android.net.INetworkStatsService
        public long getIfaceStats(String iface, int type) throws RemoteException {
            return 0;
        }

        @Override // android.net.INetworkStatsService
        public long getTotalStats(int type) throws RemoteException {
            return 0;
        }

        @Override // android.net.INetworkStatsService
        public int tagSocket(ParcelFileDescriptor pfd, int tag, int uid) throws RemoteException {
            return 0;
        }

        @Override // android.net.INetworkStatsService
        public IBinder getHwInnerService() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INetworkStatsService {
        private static final String DESCRIPTOR = "android.net.INetworkStatsService";
        static final int TRANSACTION_forceUpdate = 8;
        static final int TRANSACTION_forceUpdateIfaces = 7;
        static final int TRANSACTION_getDataLayerSnapshotForUid = 3;
        static final int TRANSACTION_getDetailedUidStats = 4;
        static final int TRANSACTION_getHwInnerService = 15;
        static final int TRANSACTION_getIfaceStats = 12;
        static final int TRANSACTION_getMobileIfaces = 5;
        static final int TRANSACTION_getTotalStats = 13;
        static final int TRANSACTION_getUidStats = 11;
        static final int TRANSACTION_incrementOperationCount = 6;
        static final int TRANSACTION_openSession = 1;
        static final int TRANSACTION_openSessionForUsageStats = 2;
        static final int TRANSACTION_registerUsageCallback = 9;
        static final int TRANSACTION_tagSocket = 14;
        static final int TRANSACTION_unregisterUsageRequest = 10;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INetworkStatsService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INetworkStatsService)) {
                return new Proxy(obj);
            }
            return (INetworkStatsService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "openSession";
                case 2:
                    return "openSessionForUsageStats";
                case 3:
                    return "getDataLayerSnapshotForUid";
                case 4:
                    return "getDetailedUidStats";
                case 5:
                    return "getMobileIfaces";
                case 6:
                    return "incrementOperationCount";
                case 7:
                    return "forceUpdateIfaces";
                case 8:
                    return "forceUpdate";
                case 9:
                    return "registerUsageCallback";
                case 10:
                    return "unregisterUsageRequest";
                case 11:
                    return "getUidStats";
                case 12:
                    return "getIfaceStats";
                case 13:
                    return "getTotalStats";
                case 14:
                    return "tagSocket";
                case 15:
                    return "getHwInnerService";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            DataUsageRequest _arg1;
            Messenger _arg2;
            DataUsageRequest _arg0;
            ParcelFileDescriptor _arg02;
            if (code != 1598968902) {
                IBinder iBinder = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        INetworkStatsSession _result = openSession();
                        reply.writeNoException();
                        if (_result != null) {
                            iBinder = _result.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        INetworkStatsSession _result2 = openSessionForUsageStats(data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result2 != null) {
                            iBinder = _result2.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        NetworkStats _result3 = getDataLayerSnapshotForUid(data.readInt());
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        NetworkStats _result4 = getDetailedUidStats(data.createStringArray());
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result5 = getMobileIfaces();
                        reply.writeNoException();
                        reply.writeStringArray(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        incrementOperationCount(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        forceUpdateIfaces((Network[]) data.createTypedArray(Network.CREATOR), (VpnInfo[]) data.createTypedArray(VpnInfo.CREATOR), (NetworkState[]) data.createTypedArray(NetworkState.CREATOR), data.readString());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        forceUpdate();
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = DataUsageRequest.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = Messenger.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        DataUsageRequest _result6 = registerUsageCallback(_arg03, _arg1, _arg2, data.readStrongBinder());
                        reply.writeNoException();
                        if (_result6 != null) {
                            reply.writeInt(1);
                            _result6.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = DataUsageRequest.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        unregisterUsageRequest(_arg0);
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        long _result7 = getUidStats(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeLong(_result7);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        long _result8 = getIfaceStats(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeLong(_result8);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        long _result9 = getTotalStats(data.readInt());
                        reply.writeNoException();
                        reply.writeLong(_result9);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        int _result10 = tagSocket(_arg02, data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result11 = getHwInnerService();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result11);
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
        public static class Proxy implements INetworkStatsService {
            public static INetworkStatsService sDefaultImpl;
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

            @Override // android.net.INetworkStatsService
            public INetworkStatsSession openSession() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().openSession();
                    }
                    _reply.readException();
                    INetworkStatsSession _result = INetworkStatsSession.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkStatsService
            public INetworkStatsSession openSessionForUsageStats(int flags, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().openSessionForUsageStats(flags, callingPackage);
                    }
                    _reply.readException();
                    INetworkStatsSession _result = INetworkStatsSession.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkStatsService
            public NetworkStats getDataLayerSnapshotForUid(int uid) throws RemoteException {
                NetworkStats _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDataLayerSnapshotForUid(uid);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NetworkStats.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkStatsService
            public NetworkStats getDetailedUidStats(String[] requiredIfaces) throws RemoteException {
                NetworkStats _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(requiredIfaces);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDetailedUidStats(requiredIfaces);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NetworkStats.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkStatsService
            public String[] getMobileIfaces() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMobileIfaces();
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkStatsService
            public void incrementOperationCount(int uid, int tag, int operationCount) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(tag);
                    _data.writeInt(operationCount);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().incrementOperationCount(uid, tag, operationCount);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkStatsService
            public void forceUpdateIfaces(Network[] defaultNetworks, VpnInfo[] vpnArray, NetworkState[] networkStates, String activeIface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(defaultNetworks, 0);
                    _data.writeTypedArray(vpnArray, 0);
                    _data.writeTypedArray(networkStates, 0);
                    _data.writeString(activeIface);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().forceUpdateIfaces(defaultNetworks, vpnArray, networkStates, activeIface);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkStatsService
            public void forceUpdate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().forceUpdate();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkStatsService
            public DataUsageRequest registerUsageCallback(String callingPackage, DataUsageRequest request, Messenger messenger, IBinder binder) throws RemoteException {
                DataUsageRequest _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (messenger != null) {
                        _data.writeInt(1);
                        messenger.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(binder);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerUsageCallback(callingPackage, request, messenger, binder);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = DataUsageRequest.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkStatsService
            public void unregisterUsageRequest(DataUsageRequest request) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterUsageRequest(request);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkStatsService
            public long getUidStats(int uid, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(type);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUidStats(uid, type);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkStatsService
            public long getIfaceStats(String iface, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(iface);
                    _data.writeInt(type);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIfaceStats(iface, type);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkStatsService
            public long getTotalStats(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTotalStats(type);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkStatsService
            public int tagSocket(ParcelFileDescriptor pfd, int tag, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (pfd != null) {
                        _data.writeInt(1);
                        pfd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(tag);
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().tagSocket(pfd, tag, uid);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkStatsService
            public IBinder getHwInnerService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHwInnerService();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(INetworkStatsService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INetworkStatsService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
