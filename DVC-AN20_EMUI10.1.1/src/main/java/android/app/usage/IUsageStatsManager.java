package android.app.usage;

import android.annotation.UnsupportedAppUsage;
import android.app.PendingIntent;
import android.content.pm.ParceledListSlice;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IUsageStatsManager extends IInterface {
    void forceUsageSourceSettingRead() throws RemoteException;

    int getAppStandbyBucket(String str, String str2, int i) throws RemoteException;

    ParceledListSlice getAppStandbyBuckets(String str, int i) throws RemoteException;

    int getUsageSource() throws RemoteException;

    @UnsupportedAppUsage
    boolean isAppInactive(String str, int i) throws RemoteException;

    void onCarrierPrivilegedAppsChanged() throws RemoteException;

    @UnsupportedAppUsage
    ParceledListSlice queryConfigurationStats(int i, long j, long j2, String str) throws RemoteException;

    ParceledListSlice queryEventStats(int i, long j, long j2, String str) throws RemoteException;

    UsageEvents queryEvents(long j, long j2, String str) throws RemoteException;

    UsageEvents queryEventsForPackage(long j, long j2, String str) throws RemoteException;

    UsageEvents queryEventsForPackageForUser(long j, long j2, int i, String str, String str2) throws RemoteException;

    UsageEvents queryEventsForUser(long j, long j2, int i, String str) throws RemoteException;

    @UnsupportedAppUsage
    ParceledListSlice queryUsageStats(int i, long j, long j2, String str) throws RemoteException;

    void registerAppUsageLimitObserver(int i, String[] strArr, long j, long j2, PendingIntent pendingIntent, String str) throws RemoteException;

    void registerAppUsageObserver(int i, String[] strArr, long j, PendingIntent pendingIntent, String str) throws RemoteException;

    void registerUsageSessionObserver(int i, String[] strArr, long j, long j2, PendingIntent pendingIntent, PendingIntent pendingIntent2, String str) throws RemoteException;

    void reportChooserSelection(String str, int i, String str2, String[] strArr, String str3) throws RemoteException;

    void reportPastUsageStart(IBinder iBinder, String str, long j, String str2) throws RemoteException;

    void reportUsageStart(IBinder iBinder, String str, String str2) throws RemoteException;

    void reportUsageStop(IBinder iBinder, String str, String str2) throws RemoteException;

    @UnsupportedAppUsage
    void setAppInactive(String str, boolean z, int i) throws RemoteException;

    void setAppStandbyBucket(String str, int i, int i2) throws RemoteException;

    void setAppStandbyBuckets(ParceledListSlice parceledListSlice, int i) throws RemoteException;

    void unregisterAppUsageLimitObserver(int i, String str) throws RemoteException;

    void unregisterAppUsageObserver(int i, String str) throws RemoteException;

    void unregisterUsageSessionObserver(int i, String str) throws RemoteException;

    void whitelistAppTemporarily(String str, long j, int i) throws RemoteException;

    public static class Default implements IUsageStatsManager {
        @Override // android.app.usage.IUsageStatsManager
        public ParceledListSlice queryUsageStats(int bucketType, long beginTime, long endTime, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.app.usage.IUsageStatsManager
        public ParceledListSlice queryConfigurationStats(int bucketType, long beginTime, long endTime, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.app.usage.IUsageStatsManager
        public ParceledListSlice queryEventStats(int bucketType, long beginTime, long endTime, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.app.usage.IUsageStatsManager
        public UsageEvents queryEvents(long beginTime, long endTime, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.app.usage.IUsageStatsManager
        public UsageEvents queryEventsForPackage(long beginTime, long endTime, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.app.usage.IUsageStatsManager
        public UsageEvents queryEventsForUser(long beginTime, long endTime, int userId, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.app.usage.IUsageStatsManager
        public UsageEvents queryEventsForPackageForUser(long beginTime, long endTime, int userId, String pkg, String callingPackage) throws RemoteException {
            return null;
        }

        @Override // android.app.usage.IUsageStatsManager
        public void setAppInactive(String packageName, boolean inactive, int userId) throws RemoteException {
        }

        @Override // android.app.usage.IUsageStatsManager
        public boolean isAppInactive(String packageName, int userId) throws RemoteException {
            return false;
        }

        @Override // android.app.usage.IUsageStatsManager
        public void whitelistAppTemporarily(String packageName, long duration, int userId) throws RemoteException {
        }

        @Override // android.app.usage.IUsageStatsManager
        public void onCarrierPrivilegedAppsChanged() throws RemoteException {
        }

        @Override // android.app.usage.IUsageStatsManager
        public void reportChooserSelection(String packageName, int userId, String contentType, String[] annotations, String action) throws RemoteException {
        }

        @Override // android.app.usage.IUsageStatsManager
        public int getAppStandbyBucket(String packageName, String callingPackage, int userId) throws RemoteException {
            return 0;
        }

        @Override // android.app.usage.IUsageStatsManager
        public void setAppStandbyBucket(String packageName, int bucket, int userId) throws RemoteException {
        }

        @Override // android.app.usage.IUsageStatsManager
        public ParceledListSlice getAppStandbyBuckets(String callingPackage, int userId) throws RemoteException {
            return null;
        }

        @Override // android.app.usage.IUsageStatsManager
        public void setAppStandbyBuckets(ParceledListSlice appBuckets, int userId) throws RemoteException {
        }

        @Override // android.app.usage.IUsageStatsManager
        public void registerAppUsageObserver(int observerId, String[] packages, long timeLimitMs, PendingIntent callback, String callingPackage) throws RemoteException {
        }

        @Override // android.app.usage.IUsageStatsManager
        public void unregisterAppUsageObserver(int observerId, String callingPackage) throws RemoteException {
        }

        @Override // android.app.usage.IUsageStatsManager
        public void registerUsageSessionObserver(int sessionObserverId, String[] observed, long timeLimitMs, long sessionThresholdTimeMs, PendingIntent limitReachedCallbackIntent, PendingIntent sessionEndCallbackIntent, String callingPackage) throws RemoteException {
        }

        @Override // android.app.usage.IUsageStatsManager
        public void unregisterUsageSessionObserver(int sessionObserverId, String callingPackage) throws RemoteException {
        }

        @Override // android.app.usage.IUsageStatsManager
        public void registerAppUsageLimitObserver(int observerId, String[] packages, long timeLimitMs, long timeUsedMs, PendingIntent callback, String callingPackage) throws RemoteException {
        }

        @Override // android.app.usage.IUsageStatsManager
        public void unregisterAppUsageLimitObserver(int observerId, String callingPackage) throws RemoteException {
        }

        @Override // android.app.usage.IUsageStatsManager
        public void reportUsageStart(IBinder activity, String token, String callingPackage) throws RemoteException {
        }

        @Override // android.app.usage.IUsageStatsManager
        public void reportPastUsageStart(IBinder activity, String token, long timeAgoMs, String callingPackage) throws RemoteException {
        }

        @Override // android.app.usage.IUsageStatsManager
        public void reportUsageStop(IBinder activity, String token, String callingPackage) throws RemoteException {
        }

        @Override // android.app.usage.IUsageStatsManager
        public int getUsageSource() throws RemoteException {
            return 0;
        }

        @Override // android.app.usage.IUsageStatsManager
        public void forceUsageSourceSettingRead() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IUsageStatsManager {
        private static final String DESCRIPTOR = "android.app.usage.IUsageStatsManager";
        static final int TRANSACTION_forceUsageSourceSettingRead = 27;
        static final int TRANSACTION_getAppStandbyBucket = 13;
        static final int TRANSACTION_getAppStandbyBuckets = 15;
        static final int TRANSACTION_getUsageSource = 26;
        static final int TRANSACTION_isAppInactive = 9;
        static final int TRANSACTION_onCarrierPrivilegedAppsChanged = 11;
        static final int TRANSACTION_queryConfigurationStats = 2;
        static final int TRANSACTION_queryEventStats = 3;
        static final int TRANSACTION_queryEvents = 4;
        static final int TRANSACTION_queryEventsForPackage = 5;
        static final int TRANSACTION_queryEventsForPackageForUser = 7;
        static final int TRANSACTION_queryEventsForUser = 6;
        static final int TRANSACTION_queryUsageStats = 1;
        static final int TRANSACTION_registerAppUsageLimitObserver = 21;
        static final int TRANSACTION_registerAppUsageObserver = 17;
        static final int TRANSACTION_registerUsageSessionObserver = 19;
        static final int TRANSACTION_reportChooserSelection = 12;
        static final int TRANSACTION_reportPastUsageStart = 24;
        static final int TRANSACTION_reportUsageStart = 23;
        static final int TRANSACTION_reportUsageStop = 25;
        static final int TRANSACTION_setAppInactive = 8;
        static final int TRANSACTION_setAppStandbyBucket = 14;
        static final int TRANSACTION_setAppStandbyBuckets = 16;
        static final int TRANSACTION_unregisterAppUsageLimitObserver = 22;
        static final int TRANSACTION_unregisterAppUsageObserver = 18;
        static final int TRANSACTION_unregisterUsageSessionObserver = 20;
        static final int TRANSACTION_whitelistAppTemporarily = 10;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IUsageStatsManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IUsageStatsManager)) {
                return new Proxy(obj);
            }
            return (IUsageStatsManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "queryUsageStats";
                case 2:
                    return "queryConfigurationStats";
                case 3:
                    return "queryEventStats";
                case 4:
                    return "queryEvents";
                case 5:
                    return "queryEventsForPackage";
                case 6:
                    return "queryEventsForUser";
                case 7:
                    return "queryEventsForPackageForUser";
                case 8:
                    return "setAppInactive";
                case 9:
                    return "isAppInactive";
                case 10:
                    return "whitelistAppTemporarily";
                case 11:
                    return "onCarrierPrivilegedAppsChanged";
                case 12:
                    return "reportChooserSelection";
                case 13:
                    return "getAppStandbyBucket";
                case 14:
                    return "setAppStandbyBucket";
                case 15:
                    return "getAppStandbyBuckets";
                case 16:
                    return "setAppStandbyBuckets";
                case 17:
                    return "registerAppUsageObserver";
                case 18:
                    return "unregisterAppUsageObserver";
                case 19:
                    return "registerUsageSessionObserver";
                case 20:
                    return "unregisterUsageSessionObserver";
                case 21:
                    return "registerAppUsageLimitObserver";
                case 22:
                    return "unregisterAppUsageLimitObserver";
                case 23:
                    return "reportUsageStart";
                case 24:
                    return "reportPastUsageStart";
                case 25:
                    return "reportUsageStop";
                case 26:
                    return "getUsageSource";
                case 27:
                    return "forceUsageSourceSettingRead";
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
            ParceledListSlice _arg0;
            PendingIntent _arg3;
            PendingIntent _arg4;
            PendingIntent _arg5;
            PendingIntent _arg42;
            if (code != 1598968902) {
                boolean _arg1 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result = queryUsageStats(data.readInt(), data.readLong(), data.readLong(), data.readString());
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result2 = queryConfigurationStats(data.readInt(), data.readLong(), data.readLong(), data.readString());
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result3 = queryEventStats(data.readInt(), data.readLong(), data.readLong(), data.readString());
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
                        UsageEvents _result4 = queryEvents(data.readLong(), data.readLong(), data.readString());
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
                        UsageEvents _result5 = queryEventsForPackage(data.readLong(), data.readLong(), data.readString());
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        UsageEvents _result6 = queryEventsForUser(data.readLong(), data.readLong(), data.readInt(), data.readString());
                        reply.writeNoException();
                        if (_result6 != null) {
                            reply.writeInt(1);
                            _result6.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        UsageEvents _result7 = queryEventsForPackageForUser(data.readLong(), data.readLong(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        if (_result7 != null) {
                            reply.writeInt(1);
                            _result7.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setAppInactive(_arg02, _arg1, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isAppInactive = isAppInactive(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isAppInactive ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        whitelistAppTemporarily(data.readString(), data.readLong(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        onCarrierPrivilegedAppsChanged();
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        reportChooserSelection(data.readString(), data.readInt(), data.readString(), data.createStringArray(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = getAppStandbyBucket(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        setAppStandbyBucket(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result9 = getAppStandbyBuckets(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result9 != null) {
                            reply.writeInt(1);
                            _result9.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ParceledListSlice.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        setAppStandbyBuckets(_arg0, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        String[] _arg12 = data.createStringArray();
                        long _arg2 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg3 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        registerAppUsageObserver(_arg03, _arg12, _arg2, _arg3, data.readString());
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterAppUsageObserver(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        String[] _arg13 = data.createStringArray();
                        long _arg22 = data.readLong();
                        long _arg32 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg4 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg4 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg5 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg5 = null;
                        }
                        registerUsageSessionObserver(_arg04, _arg13, _arg22, _arg32, _arg4, _arg5, data.readString());
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterUsageSessionObserver(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        String[] _arg14 = data.createStringArray();
                        long _arg23 = data.readLong();
                        long _arg33 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg42 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg42 = null;
                        }
                        registerAppUsageLimitObserver(_arg05, _arg14, _arg23, _arg33, _arg42, data.readString());
                        reply.writeNoException();
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterAppUsageLimitObserver(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        reportUsageStart(data.readStrongBinder(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        reportPastUsageStart(data.readStrongBinder(), data.readString(), data.readLong(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        reportUsageStop(data.readStrongBinder(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = getUsageSource();
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        forceUsageSourceSettingRead();
                        reply.writeNoException();
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
        public static class Proxy implements IUsageStatsManager {
            public static IUsageStatsManager sDefaultImpl;
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

            @Override // android.app.usage.IUsageStatsManager
            public ParceledListSlice queryUsageStats(int bucketType, long beginTime, long endTime, String callingPackage) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(bucketType);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(beginTime);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(endTime);
                        try {
                            _data.writeString(callingPackage);
                            if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
                                } else {
                                    _result = null;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            ParceledListSlice queryUsageStats = Stub.getDefaultImpl().queryUsageStats(bucketType, beginTime, endTime, callingPackage);
                            _reply.recycle();
                            _data.recycle();
                            return queryUsageStats;
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
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
            }

            @Override // android.app.usage.IUsageStatsManager
            public ParceledListSlice queryConfigurationStats(int bucketType, long beginTime, long endTime, String callingPackage) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(bucketType);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(beginTime);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(endTime);
                        try {
                            _data.writeString(callingPackage);
                            if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
                                } else {
                                    _result = null;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            ParceledListSlice queryConfigurationStats = Stub.getDefaultImpl().queryConfigurationStats(bucketType, beginTime, endTime, callingPackage);
                            _reply.recycle();
                            _data.recycle();
                            return queryConfigurationStats;
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
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
            }

            @Override // android.app.usage.IUsageStatsManager
            public ParceledListSlice queryEventStats(int bucketType, long beginTime, long endTime, String callingPackage) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(bucketType);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(beginTime);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(endTime);
                        try {
                            _data.writeString(callingPackage);
                            if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
                                } else {
                                    _result = null;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            ParceledListSlice queryEventStats = Stub.getDefaultImpl().queryEventStats(bucketType, beginTime, endTime, callingPackage);
                            _reply.recycle();
                            _data.recycle();
                            return queryEventStats;
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
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
            }

            @Override // android.app.usage.IUsageStatsManager
            public UsageEvents queryEvents(long beginTime, long endTime, String callingPackage) throws RemoteException {
                UsageEvents _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(beginTime);
                    _data.writeLong(endTime);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryEvents(beginTime, endTime, callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = UsageEvents.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.usage.IUsageStatsManager
            public UsageEvents queryEventsForPackage(long beginTime, long endTime, String callingPackage) throws RemoteException {
                UsageEvents _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(beginTime);
                    _data.writeLong(endTime);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().queryEventsForPackage(beginTime, endTime, callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = UsageEvents.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.usage.IUsageStatsManager
            public UsageEvents queryEventsForUser(long beginTime, long endTime, int userId, String callingPackage) throws RemoteException {
                UsageEvents _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeLong(beginTime);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(endTime);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(userId);
                        try {
                            _data.writeString(callingPackage);
                            if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = UsageEvents.CREATOR.createFromParcel(_reply);
                                } else {
                                    _result = null;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            UsageEvents queryEventsForUser = Stub.getDefaultImpl().queryEventsForUser(beginTime, endTime, userId, callingPackage);
                            _reply.recycle();
                            _data.recycle();
                            return queryEventsForUser;
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
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
            }

            @Override // android.app.usage.IUsageStatsManager
            public UsageEvents queryEventsForPackageForUser(long beginTime, long endTime, int userId, String pkg, String callingPackage) throws RemoteException {
                UsageEvents _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeLong(beginTime);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(endTime);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(userId);
                        _data.writeString(pkg);
                        _data.writeString(callingPackage);
                        if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            if (_reply.readInt() != 0) {
                                _result = UsageEvents.CREATOR.createFromParcel(_reply);
                            } else {
                                _result = null;
                            }
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        UsageEvents queryEventsForPackageForUser = Stub.getDefaultImpl().queryEventsForPackageForUser(beginTime, endTime, userId, pkg, callingPackage);
                        _reply.recycle();
                        _data.recycle();
                        return queryEventsForPackageForUser;
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.usage.IUsageStatsManager
            public void setAppInactive(String packageName, boolean inactive, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(inactive ? 1 : 0);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAppInactive(packageName, inactive, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.usage.IUsageStatsManager
            public boolean isAppInactive(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAppInactive(packageName, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.usage.IUsageStatsManager
            public void whitelistAppTemporarily(String packageName, long duration, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeLong(duration);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().whitelistAppTemporarily(packageName, duration, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.usage.IUsageStatsManager
            public void onCarrierPrivilegedAppsChanged() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onCarrierPrivilegedAppsChanged();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.usage.IUsageStatsManager
            public void reportChooserSelection(String packageName, int userId, String contentType, String[] annotations, String action) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    _data.writeString(contentType);
                    _data.writeStringArray(annotations);
                    _data.writeString(action);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportChooserSelection(packageName, userId, contentType, annotations, action);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.usage.IUsageStatsManager
            public int getAppStandbyBucket(String packageName, String callingPackage, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(callingPackage);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppStandbyBucket(packageName, callingPackage, userId);
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

            @Override // android.app.usage.IUsageStatsManager
            public void setAppStandbyBucket(String packageName, int bucket, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(bucket);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAppStandbyBucket(packageName, bucket, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.usage.IUsageStatsManager
            public ParceledListSlice getAppStandbyBuckets(String callingPackage, int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppStandbyBuckets(callingPackage, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.usage.IUsageStatsManager
            public void setAppStandbyBuckets(ParceledListSlice appBuckets, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (appBuckets != null) {
                        _data.writeInt(1);
                        appBuckets.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAppStandbyBuckets(appBuckets, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.usage.IUsageStatsManager
            public void registerAppUsageObserver(int observerId, String[] packages, long timeLimitMs, PendingIntent callback, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(observerId);
                        try {
                            _data.writeStringArray(packages);
                        } catch (Throwable th) {
                            th = th;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeLong(timeLimitMs);
                            if (callback != null) {
                                _data.writeInt(1);
                                callback.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            try {
                                _data.writeString(callingPackage);
                                if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                    _reply.readException();
                                    _reply.recycle();
                                    _data.recycle();
                                    return;
                                }
                                Stub.getDefaultImpl().registerAppUsageObserver(observerId, packages, timeLimitMs, callback, callingPackage);
                                _reply.recycle();
                                _data.recycle();
                            } catch (Throwable th2) {
                                th = th2;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
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
            }

            @Override // android.app.usage.IUsageStatsManager
            public void unregisterAppUsageObserver(int observerId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(observerId);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterAppUsageObserver(observerId, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.usage.IUsageStatsManager
            public void registerUsageSessionObserver(int sessionObserverId, String[] observed, long timeLimitMs, long sessionThresholdTimeMs, PendingIntent limitReachedCallbackIntent, PendingIntent sessionEndCallbackIntent, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(sessionObserverId);
                        _data.writeStringArray(observed);
                        _data.writeLong(timeLimitMs);
                        _data.writeLong(sessionThresholdTimeMs);
                        if (limitReachedCallbackIntent != null) {
                            _data.writeInt(1);
                            limitReachedCallbackIntent.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        if (sessionEndCallbackIntent != null) {
                            _data.writeInt(1);
                            sessionEndCallbackIntent.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        _data.writeString(callingPackage);
                        if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().registerUsageSessionObserver(sessionObserverId, observed, timeLimitMs, sessionThresholdTimeMs, limitReachedCallbackIntent, sessionEndCallbackIntent, callingPackage);
                        _reply.recycle();
                        _data.recycle();
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.usage.IUsageStatsManager
            public void unregisterUsageSessionObserver(int sessionObserverId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionObserverId);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterUsageSessionObserver(sessionObserverId, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.usage.IUsageStatsManager
            public void registerAppUsageLimitObserver(int observerId, String[] packages, long timeLimitMs, long timeUsedMs, PendingIntent callback, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(observerId);
                        try {
                            _data.writeStringArray(packages);
                            _data.writeLong(timeLimitMs);
                            _data.writeLong(timeUsedMs);
                            if (callback != null) {
                                _data.writeInt(1);
                                callback.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                        } catch (Throwable th) {
                            th = th;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeString(callingPackage);
                            if (this.mRemote.transact(21, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().registerAppUsageLimitObserver(observerId, packages, timeLimitMs, timeUsedMs, callback, callingPackage);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th2) {
                            th = th2;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.usage.IUsageStatsManager
            public void unregisterAppUsageLimitObserver(int observerId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(observerId);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterAppUsageLimitObserver(observerId, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.usage.IUsageStatsManager
            public void reportUsageStart(IBinder activity, String token, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(activity);
                    _data.writeString(token);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportUsageStart(activity, token, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.usage.IUsageStatsManager
            public void reportPastUsageStart(IBinder activity, String token, long timeAgoMs, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(activity);
                    _data.writeString(token);
                    _data.writeLong(timeAgoMs);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(24, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportPastUsageStart(activity, token, timeAgoMs, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.usage.IUsageStatsManager
            public void reportUsageStop(IBinder activity, String token, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(activity);
                    _data.writeString(token);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(25, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportUsageStop(activity, token, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.usage.IUsageStatsManager
            public int getUsageSource() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUsageSource();
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

            @Override // android.app.usage.IUsageStatsManager
            public void forceUsageSourceSettingRead() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(27, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().forceUsageSourceSettingRead();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IUsageStatsManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IUsageStatsManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
