package android.os;

import android.os.IStatsPullerCallback;

public interface IStatsManager extends IInterface {
    public static final int FLAG_REQUIRE_LOW_LATENCY_MONITOR = 4;
    public static final int FLAG_REQUIRE_STAGING = 1;
    public static final int FLAG_ROLLBACK_ENABLED = 2;

    void addConfiguration(long j, byte[] bArr, String str) throws RemoteException;

    byte[] getData(long j, String str) throws RemoteException;

    byte[] getMetadata(String str) throws RemoteException;

    long[] getRegisteredExperimentIds() throws RemoteException;

    void informAlarmForSubscriberTriggeringFired() throws RemoteException;

    void informAllUidData(ParcelFileDescriptor parcelFileDescriptor) throws RemoteException;

    void informAnomalyAlarmFired() throws RemoteException;

    void informDeviceShutdown() throws RemoteException;

    void informOnePackage(String str, int i, long j, String str2, String str3) throws RemoteException;

    void informOnePackageRemoved(String str, int i) throws RemoteException;

    void informPollAlarmFired() throws RemoteException;

    void registerPullerCallback(int i, IStatsPullerCallback iStatsPullerCallback, String str) throws RemoteException;

    void removeActiveConfigsChangedOperation(String str) throws RemoteException;

    void removeConfiguration(long j, String str) throws RemoteException;

    void removeDataFetchOperation(long j, String str) throws RemoteException;

    void sendAppBreadcrumbAtom(int i, int i2) throws RemoteException;

    void sendBinaryPushStateChangedAtom(String str, long j, int i, int i2, long[] jArr) throws RemoteException;

    void sendWatchdogRollbackOccurredAtom(int i, String str, long j) throws RemoteException;

    long[] setActiveConfigsChangedOperation(IBinder iBinder, String str) throws RemoteException;

    void setBroadcastSubscriber(long j, long j2, IBinder iBinder, String str) throws RemoteException;

    void setDataFetchOperation(long j, IBinder iBinder, String str) throws RemoteException;

    void statsCompanionReady() throws RemoteException;

    void systemRunning() throws RemoteException;

    void unregisterPullerCallback(int i, String str) throws RemoteException;

    void unsetBroadcastSubscriber(long j, long j2, String str) throws RemoteException;

    public static class Default implements IStatsManager {
        @Override // android.os.IStatsManager
        public void systemRunning() throws RemoteException {
        }

        @Override // android.os.IStatsManager
        public void statsCompanionReady() throws RemoteException {
        }

        @Override // android.os.IStatsManager
        public void informAnomalyAlarmFired() throws RemoteException {
        }

        @Override // android.os.IStatsManager
        public void informPollAlarmFired() throws RemoteException {
        }

        @Override // android.os.IStatsManager
        public void informAlarmForSubscriberTriggeringFired() throws RemoteException {
        }

        @Override // android.os.IStatsManager
        public void informDeviceShutdown() throws RemoteException {
        }

        @Override // android.os.IStatsManager
        public void informAllUidData(ParcelFileDescriptor fd) throws RemoteException {
        }

        @Override // android.os.IStatsManager
        public void informOnePackage(String app, int uid, long version, String version_string, String installer) throws RemoteException {
        }

        @Override // android.os.IStatsManager
        public void informOnePackageRemoved(String app, int uid) throws RemoteException {
        }

        @Override // android.os.IStatsManager
        public byte[] getData(long key, String packageName) throws RemoteException {
            return null;
        }

        @Override // android.os.IStatsManager
        public byte[] getMetadata(String packageName) throws RemoteException {
            return null;
        }

        @Override // android.os.IStatsManager
        public void addConfiguration(long configKey, byte[] config, String packageName) throws RemoteException {
        }

        @Override // android.os.IStatsManager
        public void setDataFetchOperation(long configKey, IBinder intentSender, String packageName) throws RemoteException {
        }

        @Override // android.os.IStatsManager
        public void removeDataFetchOperation(long configKey, String packageName) throws RemoteException {
        }

        @Override // android.os.IStatsManager
        public long[] setActiveConfigsChangedOperation(IBinder intentSender, String packageName) throws RemoteException {
            return null;
        }

        @Override // android.os.IStatsManager
        public void removeActiveConfigsChangedOperation(String packageName) throws RemoteException {
        }

        @Override // android.os.IStatsManager
        public void removeConfiguration(long configKey, String packageName) throws RemoteException {
        }

        @Override // android.os.IStatsManager
        public void setBroadcastSubscriber(long configKey, long subscriberId, IBinder intentSender, String packageName) throws RemoteException {
        }

        @Override // android.os.IStatsManager
        public void unsetBroadcastSubscriber(long configKey, long subscriberId, String packageName) throws RemoteException {
        }

        @Override // android.os.IStatsManager
        public void sendAppBreadcrumbAtom(int label, int state) throws RemoteException {
        }

        @Override // android.os.IStatsManager
        public void registerPullerCallback(int atomTag, IStatsPullerCallback pullerCallback, String packageName) throws RemoteException {
        }

        @Override // android.os.IStatsManager
        public void unregisterPullerCallback(int atomTag, String packageName) throws RemoteException {
        }

        @Override // android.os.IStatsManager
        public void sendBinaryPushStateChangedAtom(String trainName, long trainVersionCode, int options, int state, long[] experimentId) throws RemoteException {
        }

        @Override // android.os.IStatsManager
        public void sendWatchdogRollbackOccurredAtom(int rollbackType, String packageName, long packageVersionCode) throws RemoteException {
        }

        @Override // android.os.IStatsManager
        public long[] getRegisteredExperimentIds() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IStatsManager {
        private static final String DESCRIPTOR = "android.os.IStatsManager";
        static final int TRANSACTION_addConfiguration = 12;
        static final int TRANSACTION_getData = 10;
        static final int TRANSACTION_getMetadata = 11;
        static final int TRANSACTION_getRegisteredExperimentIds = 25;
        static final int TRANSACTION_informAlarmForSubscriberTriggeringFired = 5;
        static final int TRANSACTION_informAllUidData = 7;
        static final int TRANSACTION_informAnomalyAlarmFired = 3;
        static final int TRANSACTION_informDeviceShutdown = 6;
        static final int TRANSACTION_informOnePackage = 8;
        static final int TRANSACTION_informOnePackageRemoved = 9;
        static final int TRANSACTION_informPollAlarmFired = 4;
        static final int TRANSACTION_registerPullerCallback = 21;
        static final int TRANSACTION_removeActiveConfigsChangedOperation = 16;
        static final int TRANSACTION_removeConfiguration = 17;
        static final int TRANSACTION_removeDataFetchOperation = 14;
        static final int TRANSACTION_sendAppBreadcrumbAtom = 20;
        static final int TRANSACTION_sendBinaryPushStateChangedAtom = 23;
        static final int TRANSACTION_sendWatchdogRollbackOccurredAtom = 24;
        static final int TRANSACTION_setActiveConfigsChangedOperation = 15;
        static final int TRANSACTION_setBroadcastSubscriber = 18;
        static final int TRANSACTION_setDataFetchOperation = 13;
        static final int TRANSACTION_statsCompanionReady = 2;
        static final int TRANSACTION_systemRunning = 1;
        static final int TRANSACTION_unregisterPullerCallback = 22;
        static final int TRANSACTION_unsetBroadcastSubscriber = 19;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IStatsManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IStatsManager)) {
                return new Proxy(obj);
            }
            return (IStatsManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "systemRunning";
                case 2:
                    return "statsCompanionReady";
                case 3:
                    return "informAnomalyAlarmFired";
                case 4:
                    return "informPollAlarmFired";
                case 5:
                    return "informAlarmForSubscriberTriggeringFired";
                case 6:
                    return "informDeviceShutdown";
                case 7:
                    return "informAllUidData";
                case 8:
                    return "informOnePackage";
                case 9:
                    return "informOnePackageRemoved";
                case 10:
                    return "getData";
                case 11:
                    return "getMetadata";
                case 12:
                    return "addConfiguration";
                case 13:
                    return "setDataFetchOperation";
                case 14:
                    return "removeDataFetchOperation";
                case 15:
                    return "setActiveConfigsChangedOperation";
                case 16:
                    return "removeActiveConfigsChangedOperation";
                case 17:
                    return "removeConfiguration";
                case 18:
                    return "setBroadcastSubscriber";
                case 19:
                    return "unsetBroadcastSubscriber";
                case 20:
                    return "sendAppBreadcrumbAtom";
                case 21:
                    return "registerPullerCallback";
                case 22:
                    return "unregisterPullerCallback";
                case 23:
                    return "sendBinaryPushStateChangedAtom";
                case 24:
                    return "sendWatchdogRollbackOccurredAtom";
                case 25:
                    return "getRegisteredExperimentIds";
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
            ParcelFileDescriptor _arg0;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        systemRunning();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        statsCompanionReady();
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        informAnomalyAlarmFired();
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        informPollAlarmFired();
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        informAlarmForSubscriberTriggeringFired();
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        informDeviceShutdown();
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        informAllUidData(_arg0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        informOnePackage(data.readString(), data.readInt(), data.readLong(), data.readString(), data.readString());
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        informOnePackageRemoved(data.readString(), data.readInt());
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result = getData(data.readLong(), data.readString());
                        reply.writeNoException();
                        reply.writeByteArray(_result);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result2 = getMetadata(data.readString());
                        reply.writeNoException();
                        reply.writeByteArray(_result2);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        addConfiguration(data.readLong(), data.createByteArray(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        setDataFetchOperation(data.readLong(), data.readStrongBinder(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        removeDataFetchOperation(data.readLong(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        long[] _result3 = setActiveConfigsChangedOperation(data.readStrongBinder(), data.readString());
                        reply.writeNoException();
                        reply.writeLongArray(_result3);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        removeActiveConfigsChangedOperation(data.readString());
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        removeConfiguration(data.readLong(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        setBroadcastSubscriber(data.readLong(), data.readLong(), data.readStrongBinder(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        unsetBroadcastSubscriber(data.readLong(), data.readLong(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        sendAppBreadcrumbAtom(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        registerPullerCallback(data.readInt(), IStatsPullerCallback.Stub.asInterface(data.readStrongBinder()), data.readString());
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterPullerCallback(data.readInt(), data.readString());
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        sendBinaryPushStateChangedAtom(data.readString(), data.readLong(), data.readInt(), data.readInt(), data.createLongArray());
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        sendWatchdogRollbackOccurredAtom(data.readInt(), data.readString(), data.readLong());
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        long[] _result4 = getRegisteredExperimentIds();
                        reply.writeNoException();
                        reply.writeLongArray(_result4);
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
        public static class Proxy implements IStatsManager {
            public static IStatsManager sDefaultImpl;
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

            @Override // android.os.IStatsManager
            public void systemRunning() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().systemRunning();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsManager
            public void statsCompanionReady() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().statsCompanionReady();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsManager
            public void informAnomalyAlarmFired() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().informAnomalyAlarmFired();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsManager
            public void informPollAlarmFired() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().informPollAlarmFired();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsManager
            public void informAlarmForSubscriberTriggeringFired() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().informAlarmForSubscriberTriggeringFired();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsManager
            public void informDeviceShutdown() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().informDeviceShutdown();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsManager
            public void informAllUidData(ParcelFileDescriptor fd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (fd != null) {
                        _data.writeInt(1);
                        fd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().informAllUidData(fd);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsManager
            public void informOnePackage(String app, int uid, long version, String version_string, String installer) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(app);
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(uid);
                        try {
                            _data.writeLong(version);
                            try {
                                _data.writeString(version_string);
                                try {
                                    _data.writeString(installer);
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
                            if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().informOnePackage(app, uid, version, version_string, installer);
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
            }

            @Override // android.os.IStatsManager
            public void informOnePackageRemoved(String app, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(app);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().informOnePackageRemoved(app, uid);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsManager
            public byte[] getData(long key, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(key);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getData(key, packageName);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsManager
            public byte[] getMetadata(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMetadata(packageName);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsManager
            public void addConfiguration(long configKey, byte[] config, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(configKey);
                    _data.writeByteArray(config);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addConfiguration(configKey, config, packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsManager
            public void setDataFetchOperation(long configKey, IBinder intentSender, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(configKey);
                    _data.writeStrongBinder(intentSender);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDataFetchOperation(configKey, intentSender, packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsManager
            public void removeDataFetchOperation(long configKey, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(configKey);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeDataFetchOperation(configKey, packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsManager
            public long[] setActiveConfigsChangedOperation(IBinder intentSender, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(intentSender);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setActiveConfigsChangedOperation(intentSender, packageName);
                    }
                    _reply.readException();
                    long[] _result = _reply.createLongArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsManager
            public void removeActiveConfigsChangedOperation(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeActiveConfigsChangedOperation(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsManager
            public void removeConfiguration(long configKey, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(configKey);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeConfiguration(configKey, packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsManager
            public void setBroadcastSubscriber(long configKey, long subscriberId, IBinder intentSender, String packageName) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeLong(configKey);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(subscriberId);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeStrongBinder(intentSender);
                        try {
                            _data.writeString(packageName);
                            if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().setBroadcastSubscriber(configKey, subscriberId, intentSender, packageName);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.os.IStatsManager
            public void unsetBroadcastSubscriber(long configKey, long subscriberId, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(configKey);
                    _data.writeLong(subscriberId);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unsetBroadcastSubscriber(configKey, subscriberId, packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsManager
            public void sendAppBreadcrumbAtom(int label, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(label);
                    _data.writeInt(state);
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendAppBreadcrumbAtom(label, state);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsManager
            public void registerPullerCallback(int atomTag, IStatsPullerCallback pullerCallback, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(atomTag);
                    _data.writeStrongBinder(pullerCallback != null ? pullerCallback.asBinder() : null);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(21, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().registerPullerCallback(atomTag, pullerCallback, packageName);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsManager
            public void unregisterPullerCallback(int atomTag, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(atomTag);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(22, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().unregisterPullerCallback(atomTag, packageName);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsManager
            public void sendBinaryPushStateChangedAtom(String trainName, long trainVersionCode, int options, int state, long[] experimentId) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(trainName);
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(trainVersionCode);
                        try {
                            _data.writeInt(options);
                            try {
                                _data.writeInt(state);
                                try {
                                    _data.writeLongArray(experimentId);
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
                            if (this.mRemote.transact(23, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().sendBinaryPushStateChangedAtom(trainName, trainVersionCode, options, state, experimentId);
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
            }

            @Override // android.os.IStatsManager
            public void sendWatchdogRollbackOccurredAtom(int rollbackType, String packageName, long packageVersionCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(rollbackType);
                    _data.writeString(packageName);
                    _data.writeLong(packageVersionCode);
                    if (this.mRemote.transact(24, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().sendWatchdogRollbackOccurredAtom(rollbackType, packageName, packageVersionCode);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IStatsManager
            public long[] getRegisteredExperimentIds() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRegisteredExperimentIds();
                    }
                    _reply.readException();
                    long[] _result = _reply.createLongArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IStatsManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IStatsManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
