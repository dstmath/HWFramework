package android.os;

public interface IStatsManager extends IInterface {

    public static abstract class Stub extends Binder implements IStatsManager {
        private static final String DESCRIPTOR = "android.os.IStatsManager";
        static final int TRANSACTION_addConfiguration = 12;
        static final int TRANSACTION_getData = 10;
        static final int TRANSACTION_getMetadata = 11;
        static final int TRANSACTION_informAlarmForSubscriberTriggeringFired = 5;
        static final int TRANSACTION_informAllUidData = 7;
        static final int TRANSACTION_informAnomalyAlarmFired = 3;
        static final int TRANSACTION_informDeviceShutdown = 6;
        static final int TRANSACTION_informOnePackage = 8;
        static final int TRANSACTION_informOnePackageRemoved = 9;
        static final int TRANSACTION_informPollAlarmFired = 4;
        static final int TRANSACTION_removeConfiguration = 15;
        static final int TRANSACTION_removeDataFetchOperation = 14;
        static final int TRANSACTION_sendAppBreadcrumbAtom = 18;
        static final int TRANSACTION_setBroadcastSubscriber = 16;
        static final int TRANSACTION_setDataFetchOperation = 13;
        static final int TRANSACTION_statsCompanionReady = 2;
        static final int TRANSACTION_systemRunning = 1;
        static final int TRANSACTION_unsetBroadcastSubscriber = 17;

        private static class Proxy implements IStatsManager {
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

            public void systemRunning() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void statsCompanionReady() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void informAnomalyAlarmFired() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void informPollAlarmFired() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void informAlarmForSubscriberTriggeringFired() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void informDeviceShutdown() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void informAllUidData(int[] uid, long[] version, String[] app) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(uid);
                    _data.writeLongArray(version);
                    _data.writeStringArray(app);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void informOnePackage(String app, int uid, long version) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(app);
                    _data.writeInt(uid);
                    _data.writeLong(version);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void informOnePackageRemoved(String app, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(app);
                    _data.writeInt(uid);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public byte[] getData(long key, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(key);
                    _data.writeString(packageName);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createByteArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] getMetadata(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createByteArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addConfiguration(long configKey, byte[] config, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(configKey);
                    _data.writeByteArray(config);
                    _data.writeString(packageName);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setDataFetchOperation(long configKey, IBinder intentSender, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(configKey);
                    _data.writeStrongBinder(intentSender);
                    _data.writeString(packageName);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeDataFetchOperation(long configKey, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(configKey);
                    _data.writeString(packageName);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeConfiguration(long configKey, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(configKey);
                    _data.writeString(packageName);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setBroadcastSubscriber(long configKey, long subscriberId, IBinder intentSender, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(configKey);
                    _data.writeLong(subscriberId);
                    _data.writeStrongBinder(intentSender);
                    _data.writeString(packageName);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unsetBroadcastSubscriber(long configKey, long subscriberId, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(configKey);
                    _data.writeLong(subscriberId);
                    _data.writeString(packageName);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendAppBreadcrumbAtom(int label, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(label);
                    _data.writeInt(state);
                    this.mRemote.transact(18, _data, _reply, 0);
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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        systemRunning();
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        statsCompanionReady();
                        reply.writeNoException();
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        informAnomalyAlarmFired();
                        reply.writeNoException();
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        informPollAlarmFired();
                        reply.writeNoException();
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        informAlarmForSubscriberTriggeringFired();
                        reply.writeNoException();
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        informDeviceShutdown();
                        reply.writeNoException();
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        informAllUidData(data.createIntArray(), data.createLongArray(), data.createStringArray());
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        informOnePackage(data.readString(), data.readInt(), data.readLong());
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        informOnePackageRemoved(data.readString(), data.readInt());
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        byte[] _result = getData(data.readLong(), data.readString());
                        reply.writeNoException();
                        parcel2.writeByteArray(_result);
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        byte[] _result2 = getMetadata(data.readString());
                        reply.writeNoException();
                        parcel2.writeByteArray(_result2);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        addConfiguration(data.readLong(), data.createByteArray(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        setDataFetchOperation(data.readLong(), data.readStrongBinder(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        removeDataFetchOperation(data.readLong(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        removeConfiguration(data.readLong(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        setBroadcastSubscriber(data.readLong(), data.readLong(), data.readStrongBinder(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        unsetBroadcastSubscriber(data.readLong(), data.readLong(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        sendAppBreadcrumbAtom(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void addConfiguration(long j, byte[] bArr, String str) throws RemoteException;

    byte[] getData(long j, String str) throws RemoteException;

    byte[] getMetadata(String str) throws RemoteException;

    void informAlarmForSubscriberTriggeringFired() throws RemoteException;

    void informAllUidData(int[] iArr, long[] jArr, String[] strArr) throws RemoteException;

    void informAnomalyAlarmFired() throws RemoteException;

    void informDeviceShutdown() throws RemoteException;

    void informOnePackage(String str, int i, long j) throws RemoteException;

    void informOnePackageRemoved(String str, int i) throws RemoteException;

    void informPollAlarmFired() throws RemoteException;

    void removeConfiguration(long j, String str) throws RemoteException;

    void removeDataFetchOperation(long j, String str) throws RemoteException;

    void sendAppBreadcrumbAtom(int i, int i2) throws RemoteException;

    void setBroadcastSubscriber(long j, long j2, IBinder iBinder, String str) throws RemoteException;

    void setDataFetchOperation(long j, IBinder iBinder, String str) throws RemoteException;

    void statsCompanionReady() throws RemoteException;

    void systemRunning() throws RemoteException;

    void unsetBroadcastSubscriber(long j, long j2, String str) throws RemoteException;
}
