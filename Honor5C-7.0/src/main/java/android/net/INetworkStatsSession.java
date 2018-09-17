package android.net;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INetworkStatsSession extends IInterface {

    public static abstract class Stub extends Binder implements INetworkStatsSession {
        private static final String DESCRIPTOR = "android.net.INetworkStatsSession";
        static final int TRANSACTION_close = 8;
        static final int TRANSACTION_getDeviceSummaryForNetwork = 1;
        static final int TRANSACTION_getHistoryForNetwork = 3;
        static final int TRANSACTION_getHistoryForUid = 5;
        static final int TRANSACTION_getHistoryIntervalForUid = 6;
        static final int TRANSACTION_getRelevantUids = 7;
        static final int TRANSACTION_getSummaryForAllUid = 4;
        static final int TRANSACTION_getSummaryForNetwork = 2;

        private static class Proxy implements INetworkStatsSession {
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

            public NetworkStats getDeviceSummaryForNetwork(NetworkTemplate template, long start, long end) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    NetworkStats networkStats;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (template != null) {
                        _data.writeInt(Stub.TRANSACTION_getDeviceSummaryForNetwork);
                        template.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(start);
                    _data.writeLong(end);
                    this.mRemote.transact(Stub.TRANSACTION_getDeviceSummaryForNetwork, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        networkStats = (NetworkStats) NetworkStats.CREATOR.createFromParcel(_reply);
                    } else {
                        networkStats = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return networkStats;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkStats getSummaryForNetwork(NetworkTemplate template, long start, long end) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    NetworkStats networkStats;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (template != null) {
                        _data.writeInt(Stub.TRANSACTION_getDeviceSummaryForNetwork);
                        template.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(start);
                    _data.writeLong(end);
                    this.mRemote.transact(Stub.TRANSACTION_getSummaryForNetwork, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        networkStats = (NetworkStats) NetworkStats.CREATOR.createFromParcel(_reply);
                    } else {
                        networkStats = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return networkStats;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkStatsHistory getHistoryForNetwork(NetworkTemplate template, int fields) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    NetworkStatsHistory networkStatsHistory;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (template != null) {
                        _data.writeInt(Stub.TRANSACTION_getDeviceSummaryForNetwork);
                        template.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(fields);
                    this.mRemote.transact(Stub.TRANSACTION_getHistoryForNetwork, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        networkStatsHistory = (NetworkStatsHistory) NetworkStatsHistory.CREATOR.createFromParcel(_reply);
                    } else {
                        networkStatsHistory = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return networkStatsHistory;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkStats getSummaryForAllUid(NetworkTemplate template, long start, long end, boolean includeTags) throws RemoteException {
                int i = Stub.TRANSACTION_getDeviceSummaryForNetwork;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    NetworkStats networkStats;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (template != null) {
                        _data.writeInt(Stub.TRANSACTION_getDeviceSummaryForNetwork);
                        template.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeLong(start);
                    _data.writeLong(end);
                    if (!includeTags) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_getSummaryForAllUid, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        networkStats = (NetworkStats) NetworkStats.CREATOR.createFromParcel(_reply);
                    } else {
                        networkStats = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return networkStats;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkStatsHistory getHistoryForUid(NetworkTemplate template, int uid, int set, int tag, int fields) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    NetworkStatsHistory networkStatsHistory;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (template != null) {
                        _data.writeInt(Stub.TRANSACTION_getDeviceSummaryForNetwork);
                        template.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(uid);
                    _data.writeInt(set);
                    _data.writeInt(tag);
                    _data.writeInt(fields);
                    this.mRemote.transact(Stub.TRANSACTION_getHistoryForUid, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        networkStatsHistory = (NetworkStatsHistory) NetworkStatsHistory.CREATOR.createFromParcel(_reply);
                    } else {
                        networkStatsHistory = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return networkStatsHistory;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetworkStatsHistory getHistoryIntervalForUid(NetworkTemplate template, int uid, int set, int tag, int fields, long start, long end) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    NetworkStatsHistory networkStatsHistory;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (template != null) {
                        _data.writeInt(Stub.TRANSACTION_getDeviceSummaryForNetwork);
                        template.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(uid);
                    _data.writeInt(set);
                    _data.writeInt(tag);
                    _data.writeInt(fields);
                    _data.writeLong(start);
                    _data.writeLong(end);
                    this.mRemote.transact(Stub.TRANSACTION_getHistoryIntervalForUid, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        networkStatsHistory = (NetworkStatsHistory) NetworkStatsHistory.CREATOR.createFromParcel(_reply);
                    } else {
                        networkStatsHistory = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return networkStatsHistory;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getRelevantUids() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getRelevantUids, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void close() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_close, _data, _reply, 0);
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

        public static INetworkStatsSession asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INetworkStatsSession)) {
                return new Proxy(obj);
            }
            return (INetworkStatsSession) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            NetworkTemplate networkTemplate;
            NetworkStats _result;
            NetworkStatsHistory _result2;
            switch (code) {
                case TRANSACTION_getDeviceSummaryForNetwork /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        networkTemplate = (NetworkTemplate) NetworkTemplate.CREATOR.createFromParcel(data);
                    } else {
                        networkTemplate = null;
                    }
                    _result = getDeviceSummaryForNetwork(networkTemplate, data.readLong(), data.readLong());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_getDeviceSummaryForNetwork);
                        _result.writeToParcel(reply, TRANSACTION_getDeviceSummaryForNetwork);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getSummaryForNetwork /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        networkTemplate = (NetworkTemplate) NetworkTemplate.CREATOR.createFromParcel(data);
                    } else {
                        networkTemplate = null;
                    }
                    _result = getSummaryForNetwork(networkTemplate, data.readLong(), data.readLong());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_getDeviceSummaryForNetwork);
                        _result.writeToParcel(reply, TRANSACTION_getDeviceSummaryForNetwork);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getHistoryForNetwork /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        networkTemplate = (NetworkTemplate) NetworkTemplate.CREATOR.createFromParcel(data);
                    } else {
                        networkTemplate = null;
                    }
                    _result2 = getHistoryForNetwork(networkTemplate, data.readInt());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_getDeviceSummaryForNetwork);
                        _result2.writeToParcel(reply, TRANSACTION_getDeviceSummaryForNetwork);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getSummaryForAllUid /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        networkTemplate = (NetworkTemplate) NetworkTemplate.CREATOR.createFromParcel(data);
                    } else {
                        networkTemplate = null;
                    }
                    _result = getSummaryForAllUid(networkTemplate, data.readLong(), data.readLong(), data.readInt() != 0);
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_getDeviceSummaryForNetwork);
                        _result.writeToParcel(reply, TRANSACTION_getDeviceSummaryForNetwork);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getHistoryForUid /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        networkTemplate = (NetworkTemplate) NetworkTemplate.CREATOR.createFromParcel(data);
                    } else {
                        networkTemplate = null;
                    }
                    _result2 = getHistoryForUid(networkTemplate, data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_getDeviceSummaryForNetwork);
                        _result2.writeToParcel(reply, TRANSACTION_getDeviceSummaryForNetwork);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getHistoryIntervalForUid /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        networkTemplate = (NetworkTemplate) NetworkTemplate.CREATOR.createFromParcel(data);
                    } else {
                        networkTemplate = null;
                    }
                    _result2 = getHistoryIntervalForUid(networkTemplate, data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readLong(), data.readLong());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_getDeviceSummaryForNetwork);
                        _result2.writeToParcel(reply, TRANSACTION_getDeviceSummaryForNetwork);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getRelevantUids /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    int[] _result3 = getRelevantUids();
                    reply.writeNoException();
                    reply.writeIntArray(_result3);
                    return true;
                case TRANSACTION_close /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    close();
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

    void close() throws RemoteException;

    NetworkStats getDeviceSummaryForNetwork(NetworkTemplate networkTemplate, long j, long j2) throws RemoteException;

    NetworkStatsHistory getHistoryForNetwork(NetworkTemplate networkTemplate, int i) throws RemoteException;

    NetworkStatsHistory getHistoryForUid(NetworkTemplate networkTemplate, int i, int i2, int i3, int i4) throws RemoteException;

    NetworkStatsHistory getHistoryIntervalForUid(NetworkTemplate networkTemplate, int i, int i2, int i3, int i4, long j, long j2) throws RemoteException;

    int[] getRelevantUids() throws RemoteException;

    NetworkStats getSummaryForAllUid(NetworkTemplate networkTemplate, long j, long j2, boolean z) throws RemoteException;

    NetworkStats getSummaryForNetwork(NetworkTemplate networkTemplate, long j, long j2) throws RemoteException;
}
