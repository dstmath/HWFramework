package android.app;

import android.app.AlarmManager.AlarmClockInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.WorkSource;

public interface IAlarmManager extends IInterface {

    public static abstract class Stub extends Binder implements IAlarmManager {
        private static final String DESCRIPTOR = "android.app.IAlarmManager";
        static final int TRANSACTION_adjustHwRTCAlarm = 10;
        static final int TRANSACTION_checkHasHwRTCAlarm = 9;
        static final int TRANSACTION_getNextAlarmClock = 6;
        static final int TRANSACTION_getNextWakeFromIdleTime = 5;
        static final int TRANSACTION_getWakeUpNum = 8;
        static final int TRANSACTION_remove = 4;
        static final int TRANSACTION_set = 1;
        static final int TRANSACTION_setHwAirPlaneStateProp = 11;
        static final int TRANSACTION_setTime = 2;
        static final int TRANSACTION_setTimeZone = 3;
        static final int TRANSACTION_updateBlockedUids = 7;

        private static class Proxy implements IAlarmManager {
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

            public void set(String callingPackage, int type, long triggerAtTime, long windowLength, long interval, int flags, PendingIntent operation, IAlarmListener listener, String listenerTag, WorkSource workSource, AlarmClockInfo alarmClock) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(type);
                    _data.writeLong(triggerAtTime);
                    _data.writeLong(windowLength);
                    _data.writeLong(interval);
                    _data.writeInt(flags);
                    if (operation != null) {
                        _data.writeInt(Stub.TRANSACTION_set);
                        operation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeString(listenerTag);
                    if (workSource != null) {
                        _data.writeInt(Stub.TRANSACTION_set);
                        workSource.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (alarmClock != null) {
                        _data.writeInt(Stub.TRANSACTION_set);
                        alarmClock.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_set, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setTime(long millis) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(millis);
                    this.mRemote.transact(Stub.TRANSACTION_setTime, _data, _reply, 0);
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

            public void setTimeZone(String zone) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(zone);
                    this.mRemote.transact(Stub.TRANSACTION_setTimeZone, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void remove(PendingIntent operation, IAlarmListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (operation != null) {
                        _data.writeInt(Stub.TRANSACTION_set);
                        operation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_remove, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getNextWakeFromIdleTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getNextWakeFromIdleTime, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public AlarmClockInfo getNextAlarmClock(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    AlarmClockInfo alarmClockInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getNextAlarmClock, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        alarmClockInfo = (AlarmClockInfo) AlarmClockInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        alarmClockInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return alarmClockInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateBlockedUids(int uid, boolean isBlocked) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (isBlocked) {
                        i = Stub.TRANSACTION_set;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_updateBlockedUids, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getWakeUpNum(int uid, String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(pkg);
                    this.mRemote.transact(Stub.TRANSACTION_getWakeUpNum, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long checkHasHwRTCAlarm(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_checkHasHwRTCAlarm, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void adjustHwRTCAlarm(boolean deskClockTime, boolean bootOnTime, int typeState) throws RemoteException {
                int i = Stub.TRANSACTION_set;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (deskClockTime) {
                        i2 = Stub.TRANSACTION_set;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!bootOnTime) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeInt(typeState);
                    this.mRemote.transact(Stub.TRANSACTION_adjustHwRTCAlarm, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setHwAirPlaneStateProp() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_setHwAirPlaneStateProp, _data, _reply, 0);
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

        public static IAlarmManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAlarmManager)) {
                return new Proxy(obj);
            }
            return (IAlarmManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            long _result;
            switch (code) {
                case TRANSACTION_set /*1*/:
                    PendingIntent pendingIntent;
                    WorkSource workSource;
                    AlarmClockInfo alarmClockInfo;
                    data.enforceInterface(DESCRIPTOR);
                    String _arg0 = data.readString();
                    int _arg1 = data.readInt();
                    long _arg2 = data.readLong();
                    long _arg3 = data.readLong();
                    long _arg4 = data.readLong();
                    int _arg5 = data.readInt();
                    if (data.readInt() != 0) {
                        pendingIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent = null;
                    }
                    IAlarmListener _arg7 = android.app.IAlarmListener.Stub.asInterface(data.readStrongBinder());
                    String _arg8 = data.readString();
                    if (data.readInt() != 0) {
                        workSource = (WorkSource) WorkSource.CREATOR.createFromParcel(data);
                    } else {
                        workSource = null;
                    }
                    if (data.readInt() != 0) {
                        alarmClockInfo = (AlarmClockInfo) AlarmClockInfo.CREATOR.createFromParcel(data);
                    } else {
                        alarmClockInfo = null;
                    }
                    set(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, pendingIntent, _arg7, _arg8, workSource, alarmClockInfo);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setTime /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result2 = setTime(data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_set : 0);
                    return true;
                case TRANSACTION_setTimeZone /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    setTimeZone(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_remove /*4*/:
                    PendingIntent pendingIntent2;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        pendingIntent2 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        pendingIntent2 = null;
                    }
                    remove(pendingIntent2, android.app.IAlarmListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getNextWakeFromIdleTime /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getNextWakeFromIdleTime();
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case TRANSACTION_getNextAlarmClock /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    AlarmClockInfo _result3 = getNextAlarmClock(data.readInt());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_set);
                        _result3.writeToParcel(reply, TRANSACTION_set);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_updateBlockedUids /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    updateBlockedUids(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getWakeUpNum /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _result4 = getWakeUpNum(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case TRANSACTION_checkHasHwRTCAlarm /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = checkHasHwRTCAlarm(data.readString());
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case TRANSACTION_adjustHwRTCAlarm /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    adjustHwRTCAlarm(data.readInt() != 0, data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setHwAirPlaneStateProp /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    setHwAirPlaneStateProp();
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

    void adjustHwRTCAlarm(boolean z, boolean z2, int i) throws RemoteException;

    long checkHasHwRTCAlarm(String str) throws RemoteException;

    AlarmClockInfo getNextAlarmClock(int i) throws RemoteException;

    long getNextWakeFromIdleTime() throws RemoteException;

    int getWakeUpNum(int i, String str) throws RemoteException;

    void remove(PendingIntent pendingIntent, IAlarmListener iAlarmListener) throws RemoteException;

    void set(String str, int i, long j, long j2, long j3, int i2, PendingIntent pendingIntent, IAlarmListener iAlarmListener, String str2, WorkSource workSource, AlarmClockInfo alarmClockInfo) throws RemoteException;

    void setHwAirPlaneStateProp() throws RemoteException;

    boolean setTime(long j) throws RemoteException;

    void setTimeZone(String str) throws RemoteException;

    void updateBlockedUids(int i, boolean z) throws RemoteException;
}
