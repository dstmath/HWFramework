package com.huawei.android.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwAlarmManager extends IInterface {
    void adjustHwRTCAlarm(boolean z, boolean z2, int i) throws RemoteException;

    long checkHasHwRTCAlarm(String str) throws RemoteException;

    int getWakeUpNum(int i, String str) throws RemoteException;

    void setHwRTCAlarm() throws RemoteException;

    public static class Default implements IHwAlarmManager {
        @Override // com.huawei.android.app.IHwAlarmManager
        public int getWakeUpNum(int uid, String pkg) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwAlarmManager
        public long checkHasHwRTCAlarm(String packageName) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwAlarmManager
        public void adjustHwRTCAlarm(boolean deskClockTime, boolean bootOnTime, int typeState) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwAlarmManager
        public void setHwRTCAlarm() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwAlarmManager {
        private static final String DESCRIPTOR = "com.huawei.android.app.IHwAlarmManager";
        static final int TRANSACTION_adjustHwRTCAlarm = 3;
        static final int TRANSACTION_checkHasHwRTCAlarm = 2;
        static final int TRANSACTION_getWakeUpNum = 1;
        static final int TRANSACTION_setHwRTCAlarm = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwAlarmManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwAlarmManager)) {
                return new Proxy(obj);
            }
            return (IHwAlarmManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "getWakeUpNum";
            }
            if (transactionCode == 2) {
                return "checkHasHwRTCAlarm";
            }
            if (transactionCode == 3) {
                return "adjustHwRTCAlarm";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "setHwRTCAlarm";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _result = getWakeUpNum(data.readInt(), data.readString());
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                long _result2 = checkHasHwRTCAlarm(data.readString());
                reply.writeNoException();
                reply.writeLong(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                boolean _arg1 = false;
                boolean _arg0 = data.readInt() != 0;
                if (data.readInt() != 0) {
                    _arg1 = true;
                }
                adjustHwRTCAlarm(_arg0, _arg1, data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                setHwRTCAlarm();
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwAlarmManager {
            public static IHwAlarmManager sDefaultImpl;
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

            @Override // com.huawei.android.app.IHwAlarmManager
            public int getWakeUpNum(int uid, String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(pkg);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWakeUpNum(uid, pkg);
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

            @Override // com.huawei.android.app.IHwAlarmManager
            public long checkHasHwRTCAlarm(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkHasHwRTCAlarm(packageName);
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

            @Override // com.huawei.android.app.IHwAlarmManager
            public void adjustHwRTCAlarm(boolean deskClockTime, boolean bootOnTime, int typeState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    _data.writeInt(deskClockTime ? 1 : 0);
                    if (!bootOnTime) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeInt(typeState);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().adjustHwRTCAlarm(deskClockTime, bootOnTime, typeState);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwAlarmManager
            public void setHwRTCAlarm() throws RemoteException {
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
                    Stub.getDefaultImpl().setHwRTCAlarm();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwAlarmManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwAlarmManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
