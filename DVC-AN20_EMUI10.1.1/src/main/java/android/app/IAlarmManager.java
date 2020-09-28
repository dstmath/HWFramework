package android.app;

import android.annotation.UnsupportedAppUsage;
import android.app.AlarmManager;
import android.app.IAlarmListener;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.WorkSource;

public interface IAlarmManager extends IInterface {
    long currentNetworkTimeMillis() throws RemoteException;

    IBinder getHwInnerService() throws RemoteException;

    @UnsupportedAppUsage
    AlarmManager.AlarmClockInfo getNextAlarmClock(int i) throws RemoteException;

    long getNextWakeFromIdleTime() throws RemoteException;

    void remove(PendingIntent pendingIntent, IAlarmListener iAlarmListener) throws RemoteException;

    @UnsupportedAppUsage
    void set(String str, int i, long j, long j2, long j3, int i2, PendingIntent pendingIntent, IAlarmListener iAlarmListener, String str2, WorkSource workSource, AlarmManager.AlarmClockInfo alarmClockInfo) throws RemoteException;

    @UnsupportedAppUsage
    boolean setTime(long j) throws RemoteException;

    void setTimeZone(String str) throws RemoteException;

    public static class Default implements IAlarmManager {
        @Override // android.app.IAlarmManager
        public void set(String callingPackage, int type, long triggerAtTime, long windowLength, long interval, int flags, PendingIntent operation, IAlarmListener listener, String listenerTag, WorkSource workSource, AlarmManager.AlarmClockInfo alarmClock) throws RemoteException {
        }

        @Override // android.app.IAlarmManager
        public boolean setTime(long millis) throws RemoteException {
            return false;
        }

        @Override // android.app.IAlarmManager
        public void setTimeZone(String zone) throws RemoteException {
        }

        @Override // android.app.IAlarmManager
        public void remove(PendingIntent operation, IAlarmListener listener) throws RemoteException {
        }

        @Override // android.app.IAlarmManager
        public long getNextWakeFromIdleTime() throws RemoteException {
            return 0;
        }

        @Override // android.app.IAlarmManager
        public AlarmManager.AlarmClockInfo getNextAlarmClock(int userId) throws RemoteException {
            return null;
        }

        @Override // android.app.IAlarmManager
        public long currentNetworkTimeMillis() throws RemoteException {
            return 0;
        }

        @Override // android.app.IAlarmManager
        public IBinder getHwInnerService() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAlarmManager {
        private static final String DESCRIPTOR = "android.app.IAlarmManager";
        static final int TRANSACTION_currentNetworkTimeMillis = 7;
        static final int TRANSACTION_getHwInnerService = 8;
        static final int TRANSACTION_getNextAlarmClock = 6;
        static final int TRANSACTION_getNextWakeFromIdleTime = 5;
        static final int TRANSACTION_remove = 4;
        static final int TRANSACTION_set = 1;
        static final int TRANSACTION_setTime = 2;
        static final int TRANSACTION_setTimeZone = 3;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "set";
                case 2:
                    return "setTime";
                case 3:
                    return "setTimeZone";
                case 4:
                    return "remove";
                case 5:
                    return "getNextWakeFromIdleTime";
                case 6:
                    return "getNextAlarmClock";
                case 7:
                    return "currentNetworkTimeMillis";
                case 8:
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
            PendingIntent _arg6;
            WorkSource _arg9;
            AlarmManager.AlarmClockInfo _arg10;
            PendingIntent _arg0;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        int _arg1 = data.readInt();
                        long _arg2 = data.readLong();
                        long _arg3 = data.readLong();
                        long _arg4 = data.readLong();
                        int _arg5 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg6 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg6 = null;
                        }
                        IAlarmListener _arg7 = IAlarmListener.Stub.asInterface(data.readStrongBinder());
                        String _arg8 = data.readString();
                        if (data.readInt() != 0) {
                            _arg9 = WorkSource.CREATOR.createFromParcel(data);
                        } else {
                            _arg9 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg10 = AlarmManager.AlarmClockInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg10 = null;
                        }
                        set(_arg02, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6, _arg7, _arg8, _arg9, _arg10);
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean time = setTime(data.readLong());
                        reply.writeNoException();
                        reply.writeInt(time ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        setTimeZone(data.readString());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        remove(_arg0, IAlarmListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        long _result = getNextWakeFromIdleTime();
                        reply.writeNoException();
                        reply.writeLong(_result);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        AlarmManager.AlarmClockInfo _result2 = getNextAlarmClock(data.readInt());
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        long _result3 = currentNetworkTimeMillis();
                        reply.writeNoException();
                        reply.writeLong(_result3);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result4 = getHwInnerService();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result4);
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
        public static class Proxy implements IAlarmManager {
            public static IAlarmManager sDefaultImpl;
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

            @Override // android.app.IAlarmManager
            public void set(String callingPackage, int type, long triggerAtTime, long windowLength, long interval, int flags, PendingIntent operation, IAlarmListener listener, String listenerTag, WorkSource workSource, AlarmManager.AlarmClockInfo alarmClock) throws RemoteException {
                Parcel _data;
                Parcel _reply;
                int i;
                Parcel _data2 = Parcel.obtain();
                Parcel _reply2 = Parcel.obtain();
                try {
                    _data2.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data2.writeString(callingPackage);
                    _data2.writeInt(type);
                    _data2.writeLong(triggerAtTime);
                    _data2.writeLong(windowLength);
                    _data2.writeLong(interval);
                    _data2.writeInt(flags);
                    if (operation != null) {
                        try {
                            _data2.writeInt(1);
                            operation.writeToParcel(_data2, 0);
                        } catch (Throwable th) {
                            th = th;
                            _reply = _reply2;
                            _data = _data2;
                        }
                    } else {
                        _data2.writeInt(0);
                    }
                    _data2.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data2.writeString(listenerTag);
                    if (workSource != null) {
                        _data2.writeInt(1);
                        i = 0;
                        workSource.writeToParcel(_data2, 0);
                    } else {
                        i = 0;
                        _data2.writeInt(0);
                    }
                    if (alarmClock != null) {
                        _data2.writeInt(1);
                        alarmClock.writeToParcel(_data2, 0);
                    } else {
                        _data2.writeInt(i);
                    }
                    if (this.mRemote.transact(1, _data2, _reply2, 0) || Stub.getDefaultImpl() == null) {
                        _reply2.readException();
                        _reply2.recycle();
                        _data2.recycle();
                        return;
                    }
                    _reply = _reply2;
                    _data = _data2;
                    try {
                        Stub.getDefaultImpl().set(callingPackage, type, triggerAtTime, windowLength, interval, flags, operation, listener, listenerTag, workSource, alarmClock);
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
                    _reply = _reply2;
                    _data = _data2;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IAlarmManager
            public boolean setTime(long millis) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(millis);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setTime(millis);
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

            @Override // android.app.IAlarmManager
            public void setTimeZone(String zone) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(zone);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setTimeZone(zone);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IAlarmManager
            public void remove(PendingIntent operation, IAlarmListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (operation != null) {
                        _data.writeInt(1);
                        operation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().remove(operation, listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IAlarmManager
            public long getNextWakeFromIdleTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNextWakeFromIdleTime();
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

            @Override // android.app.IAlarmManager
            public AlarmManager.AlarmClockInfo getNextAlarmClock(int userId) throws RemoteException {
                AlarmManager.AlarmClockInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNextAlarmClock(userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = AlarmManager.AlarmClockInfo.CREATOR.createFromParcel(_reply);
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

            @Override // android.app.IAlarmManager
            public long currentNetworkTimeMillis() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().currentNetworkTimeMillis();
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

            @Override // android.app.IAlarmManager
            public IBinder getHwInnerService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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

        public static boolean setDefaultImpl(IAlarmManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAlarmManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
