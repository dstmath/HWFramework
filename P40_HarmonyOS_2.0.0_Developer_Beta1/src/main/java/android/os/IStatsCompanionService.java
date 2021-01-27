package android.os;

public interface IStatsCompanionService extends IInterface {
    void cancelAlarmForSubscriberTriggering() throws RemoteException;

    void cancelAnomalyAlarm() throws RemoteException;

    void cancelPullingAlarm() throws RemoteException;

    StatsLogEventWrapper[] pullData(int i) throws RemoteException;

    void sendActiveConfigsChangedBroadcast(IBinder iBinder, long[] jArr) throws RemoteException;

    void sendDataBroadcast(IBinder iBinder, long j) throws RemoteException;

    void sendSubscriberBroadcast(IBinder iBinder, long j, long j2, long j3, long j4, String[] strArr, StatsDimensionsValue statsDimensionsValue) throws RemoteException;

    void setAlarmForSubscriberTriggering(long j) throws RemoteException;

    void setAnomalyAlarm(long j) throws RemoteException;

    void setPullingAlarm(long j) throws RemoteException;

    void statsdReady() throws RemoteException;

    void triggerUidSnapshot() throws RemoteException;

    public static class Default implements IStatsCompanionService {
        @Override // android.os.IStatsCompanionService
        public void statsdReady() throws RemoteException {
        }

        @Override // android.os.IStatsCompanionService
        public void setAnomalyAlarm(long timestampMs) throws RemoteException {
        }

        @Override // android.os.IStatsCompanionService
        public void cancelAnomalyAlarm() throws RemoteException {
        }

        @Override // android.os.IStatsCompanionService
        public void setPullingAlarm(long nextPullTimeMs) throws RemoteException {
        }

        @Override // android.os.IStatsCompanionService
        public void cancelPullingAlarm() throws RemoteException {
        }

        @Override // android.os.IStatsCompanionService
        public void setAlarmForSubscriberTriggering(long timestampMs) throws RemoteException {
        }

        @Override // android.os.IStatsCompanionService
        public void cancelAlarmForSubscriberTriggering() throws RemoteException {
        }

        @Override // android.os.IStatsCompanionService
        public StatsLogEventWrapper[] pullData(int pullCode) throws RemoteException {
            return null;
        }

        @Override // android.os.IStatsCompanionService
        public void sendDataBroadcast(IBinder intentSender, long lastReportTimeNs) throws RemoteException {
        }

        @Override // android.os.IStatsCompanionService
        public void sendActiveConfigsChangedBroadcast(IBinder intentSender, long[] configIds) throws RemoteException {
        }

        @Override // android.os.IStatsCompanionService
        public void sendSubscriberBroadcast(IBinder intentSender, long configUid, long configId, long subscriptionId, long subscriptionRuleId, String[] cookies, StatsDimensionsValue dimensionsValue) throws RemoteException {
        }

        @Override // android.os.IStatsCompanionService
        public void triggerUidSnapshot() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IStatsCompanionService {
        private static final String DESCRIPTOR = "android.os.IStatsCompanionService";
        static final int TRANSACTION_cancelAlarmForSubscriberTriggering = 7;
        static final int TRANSACTION_cancelAnomalyAlarm = 3;
        static final int TRANSACTION_cancelPullingAlarm = 5;
        static final int TRANSACTION_pullData = 8;
        static final int TRANSACTION_sendActiveConfigsChangedBroadcast = 10;
        static final int TRANSACTION_sendDataBroadcast = 9;
        static final int TRANSACTION_sendSubscriberBroadcast = 11;
        static final int TRANSACTION_setAlarmForSubscriberTriggering = 6;
        static final int TRANSACTION_setAnomalyAlarm = 2;
        static final int TRANSACTION_setPullingAlarm = 4;
        static final int TRANSACTION_statsdReady = 1;
        static final int TRANSACTION_triggerUidSnapshot = 12;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IStatsCompanionService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IStatsCompanionService)) {
                return new Proxy(obj);
            }
            return (IStatsCompanionService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "statsdReady";
                case 2:
                    return "setAnomalyAlarm";
                case 3:
                    return "cancelAnomalyAlarm";
                case 4:
                    return "setPullingAlarm";
                case 5:
                    return "cancelPullingAlarm";
                case 6:
                    return "setAlarmForSubscriberTriggering";
                case 7:
                    return "cancelAlarmForSubscriberTriggering";
                case 8:
                    return "pullData";
                case 9:
                    return "sendDataBroadcast";
                case 10:
                    return "sendActiveConfigsChangedBroadcast";
                case 11:
                    return "sendSubscriberBroadcast";
                case 12:
                    return "triggerUidSnapshot";
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
            StatsDimensionsValue _arg6;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        statsdReady();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        setAnomalyAlarm(data.readLong());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        cancelAnomalyAlarm();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        setPullingAlarm(data.readLong());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        cancelPullingAlarm();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        setAlarmForSubscriberTriggering(data.readLong());
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        cancelAlarmForSubscriberTriggering();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        StatsLogEventWrapper[] _result = pullData(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedArray(_result, 1);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        sendDataBroadcast(data.readStrongBinder(), data.readLong());
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        sendActiveConfigsChangedBroadcast(data.readStrongBinder(), data.createLongArray());
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg0 = data.readStrongBinder();
                        long _arg1 = data.readLong();
                        long _arg2 = data.readLong();
                        long _arg3 = data.readLong();
                        long _arg4 = data.readLong();
                        String[] _arg5 = data.createStringArray();
                        if (data.readInt() != 0) {
                            _arg6 = StatsDimensionsValue.CREATOR.createFromParcel(data);
                        } else {
                            _arg6 = null;
                        }
                        sendSubscriberBroadcast(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        triggerUidSnapshot();
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
        public static class Proxy implements IStatsCompanionService {
            public static IStatsCompanionService sDefaultImpl;
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

            @Override // android.os.IStatsCompanionService
            public void statsdReady() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().statsdReady();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsCompanionService
            public void setAnomalyAlarm(long timestampMs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(timestampMs);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setAnomalyAlarm(timestampMs);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsCompanionService
            public void cancelAnomalyAlarm() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().cancelAnomalyAlarm();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsCompanionService
            public void setPullingAlarm(long nextPullTimeMs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(nextPullTimeMs);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setPullingAlarm(nextPullTimeMs);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsCompanionService
            public void cancelPullingAlarm() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().cancelPullingAlarm();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsCompanionService
            public void setAlarmForSubscriberTriggering(long timestampMs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(timestampMs);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setAlarmForSubscriberTriggering(timestampMs);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsCompanionService
            public void cancelAlarmForSubscriberTriggering() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().cancelAlarmForSubscriberTriggering();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsCompanionService
            public StatsLogEventWrapper[] pullData(int pullCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pullCode);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().pullData(pullCode);
                    }
                    _reply.readException();
                    StatsLogEventWrapper[] _result = (StatsLogEventWrapper[]) _reply.createTypedArray(StatsLogEventWrapper.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsCompanionService
            public void sendDataBroadcast(IBinder intentSender, long lastReportTimeNs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(intentSender);
                    _data.writeLong(lastReportTimeNs);
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().sendDataBroadcast(intentSender, lastReportTimeNs);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsCompanionService
            public void sendActiveConfigsChangedBroadcast(IBinder intentSender, long[] configIds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(intentSender);
                    _data.writeLongArray(configIds);
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().sendActiveConfigsChangedBroadcast(intentSender, configIds);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsCompanionService
            public void sendSubscriberBroadcast(IBinder intentSender, long configUid, long configId, long subscriptionId, long subscriptionRuleId, String[] cookies, StatsDimensionsValue dimensionsValue) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeStrongBinder(intentSender);
                        _data.writeLong(configUid);
                        _data.writeLong(configId);
                        _data.writeLong(subscriptionId);
                        _data.writeLong(subscriptionRuleId);
                        _data.writeStringArray(cookies);
                        if (dimensionsValue != null) {
                            _data.writeInt(1);
                            dimensionsValue.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().sendSubscriberBroadcast(intentSender, configUid, configId, subscriptionId, subscriptionRuleId, cookies, dimensionsValue);
                        _data.recycle();
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.os.IStatsCompanionService
            public void triggerUidSnapshot() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(12, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().triggerUidSnapshot();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IStatsCompanionService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IStatsCompanionService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
