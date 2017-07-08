package android.app.usage;

import android.content.pm.ParceledListSlice;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IUsageStatsManager extends IInterface {

    public static abstract class Stub extends Binder implements IUsageStatsManager {
        private static final String DESCRIPTOR = "android.app.usage.IUsageStatsManager";
        static final int TRANSACTION_isAppInactive = 5;
        static final int TRANSACTION_onCarrierPrivilegedAppsChanged = 7;
        static final int TRANSACTION_queryConfigurationStats = 2;
        static final int TRANSACTION_queryEvents = 3;
        static final int TRANSACTION_queryUsageStats = 1;
        static final int TRANSACTION_setAppInactive = 4;
        static final int TRANSACTION_whitelistAppTemporarily = 6;

        private static class Proxy implements IUsageStatsManager {
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

            public ParceledListSlice queryUsageStats(int bucketType, long beginTime, long endTime, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(bucketType);
                    _data.writeLong(beginTime);
                    _data.writeLong(endTime);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_queryUsageStats, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice queryConfigurationStats(int bucketType, long beginTime, long endTime, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(bucketType);
                    _data.writeLong(beginTime);
                    _data.writeLong(endTime);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_queryConfigurationStats, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public UsageEvents queryEvents(long beginTime, long endTime, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    UsageEvents usageEvents;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(beginTime);
                    _data.writeLong(endTime);
                    _data.writeString(callingPackage);
                    this.mRemote.transact(Stub.TRANSACTION_queryEvents, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        usageEvents = (UsageEvents) UsageEvents.CREATOR.createFromParcel(_reply);
                    } else {
                        usageEvents = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return usageEvents;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAppInactive(String packageName, boolean inactive, int userId) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (inactive) {
                        i = Stub.TRANSACTION_queryUsageStats;
                    }
                    _data.writeInt(i);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_setAppInactive, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isAppInactive(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_isAppInactive, _data, _reply, 0);
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

            public void whitelistAppTemporarily(String packageName, long duration, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeLong(duration);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_whitelistAppTemporarily, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onCarrierPrivilegedAppsChanged() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onCarrierPrivilegedAppsChanged, _data, _reply, 0);
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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ParceledListSlice _result;
            switch (code) {
                case TRANSACTION_queryUsageStats /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = queryUsageStats(data.readInt(), data.readLong(), data.readLong(), data.readString());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_queryUsageStats);
                        _result.writeToParcel(reply, TRANSACTION_queryUsageStats);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_queryConfigurationStats /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = queryConfigurationStats(data.readInt(), data.readLong(), data.readLong(), data.readString());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_queryUsageStats);
                        _result.writeToParcel(reply, TRANSACTION_queryUsageStats);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_queryEvents /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    UsageEvents _result2 = queryEvents(data.readLong(), data.readLong(), data.readString());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_queryUsageStats);
                        _result2.writeToParcel(reply, TRANSACTION_queryUsageStats);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setAppInactive /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    setAppInactive(data.readString(), data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isAppInactive /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result3 = isAppInactive(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3 ? TRANSACTION_queryUsageStats : 0);
                    return true;
                case TRANSACTION_whitelistAppTemporarily /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    whitelistAppTemporarily(data.readString(), data.readLong(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onCarrierPrivilegedAppsChanged /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    onCarrierPrivilegedAppsChanged();
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

    boolean isAppInactive(String str, int i) throws RemoteException;

    void onCarrierPrivilegedAppsChanged() throws RemoteException;

    ParceledListSlice queryConfigurationStats(int i, long j, long j2, String str) throws RemoteException;

    UsageEvents queryEvents(long j, long j2, String str) throws RemoteException;

    ParceledListSlice queryUsageStats(int i, long j, long j2, String str) throws RemoteException;

    void setAppInactive(String str, boolean z, int i) throws RemoteException;

    void whitelistAppTemporarily(String str, long j, int i) throws RemoteException;
}
