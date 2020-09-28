package huawei.android.security;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwDeviceUsagePlugin extends IInterface {
    void detectActivationWithDuration(long j) throws RemoteException;

    long getChargeTime() throws RemoteException;

    long getFristUseTime() throws RemoteException;

    long getScreenOnTime() throws RemoteException;

    long getTalkTime() throws RemoteException;

    boolean isDeviceActivated() throws RemoteException;

    void resetActivation() throws RemoteException;

    void setChargeTime(long j) throws RemoteException;

    void setFristUseTime(long j) throws RemoteException;

    void setOpenFlag(int i) throws RemoteException;

    void setScreenOnTime(long j) throws RemoteException;

    void setTalkTime(long j) throws RemoteException;

    public static class Default implements IHwDeviceUsagePlugin {
        @Override // huawei.android.security.IHwDeviceUsagePlugin
        public long getScreenOnTime() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.IHwDeviceUsagePlugin
        public long getChargeTime() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.IHwDeviceUsagePlugin
        public long getTalkTime() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.IHwDeviceUsagePlugin
        public long getFristUseTime() throws RemoteException {
            return 0;
        }

        @Override // huawei.android.security.IHwDeviceUsagePlugin
        public void setOpenFlag(int flag) throws RemoteException {
        }

        @Override // huawei.android.security.IHwDeviceUsagePlugin
        public void setScreenOnTime(long time) throws RemoteException {
        }

        @Override // huawei.android.security.IHwDeviceUsagePlugin
        public void setChargeTime(long time) throws RemoteException {
        }

        @Override // huawei.android.security.IHwDeviceUsagePlugin
        public void setTalkTime(long time) throws RemoteException {
        }

        @Override // huawei.android.security.IHwDeviceUsagePlugin
        public void setFristUseTime(long time) throws RemoteException {
        }

        @Override // huawei.android.security.IHwDeviceUsagePlugin
        public boolean isDeviceActivated() throws RemoteException {
            return false;
        }

        @Override // huawei.android.security.IHwDeviceUsagePlugin
        public void detectActivationWithDuration(long duration) throws RemoteException {
        }

        @Override // huawei.android.security.IHwDeviceUsagePlugin
        public void resetActivation() throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwDeviceUsagePlugin {
        private static final String DESCRIPTOR = "huawei.android.security.IHwDeviceUsagePlugin";
        static final int TRANSACTION_detectActivationWithDuration = 11;
        static final int TRANSACTION_getChargeTime = 2;
        static final int TRANSACTION_getFristUseTime = 4;
        static final int TRANSACTION_getScreenOnTime = 1;
        static final int TRANSACTION_getTalkTime = 3;
        static final int TRANSACTION_isDeviceActivated = 10;
        static final int TRANSACTION_resetActivation = 12;
        static final int TRANSACTION_setChargeTime = 7;
        static final int TRANSACTION_setFristUseTime = 9;
        static final int TRANSACTION_setOpenFlag = 5;
        static final int TRANSACTION_setScreenOnTime = 6;
        static final int TRANSACTION_setTalkTime = 8;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwDeviceUsagePlugin asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwDeviceUsagePlugin)) {
                return new Proxy(obj);
            }
            return (IHwDeviceUsagePlugin) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        long _result = getScreenOnTime();
                        reply.writeNoException();
                        reply.writeLong(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        long _result2 = getChargeTime();
                        reply.writeNoException();
                        reply.writeLong(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        long _result3 = getTalkTime();
                        reply.writeNoException();
                        reply.writeLong(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        long _result4 = getFristUseTime();
                        reply.writeNoException();
                        reply.writeLong(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        setOpenFlag(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        setScreenOnTime(data.readLong());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        setChargeTime(data.readLong());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        setTalkTime(data.readLong());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        setFristUseTime(data.readLong());
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isDeviceActivated = isDeviceActivated();
                        reply.writeNoException();
                        reply.writeInt(isDeviceActivated ? 1 : 0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        detectActivationWithDuration(data.readLong());
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        resetActivation();
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
        public static class Proxy implements IHwDeviceUsagePlugin {
            public static IHwDeviceUsagePlugin sDefaultImpl;
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

            @Override // huawei.android.security.IHwDeviceUsagePlugin
            public long getScreenOnTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getScreenOnTime();
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

            @Override // huawei.android.security.IHwDeviceUsagePlugin
            public long getChargeTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getChargeTime();
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

            @Override // huawei.android.security.IHwDeviceUsagePlugin
            public long getTalkTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTalkTime();
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

            @Override // huawei.android.security.IHwDeviceUsagePlugin
            public long getFristUseTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFristUseTime();
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

            @Override // huawei.android.security.IHwDeviceUsagePlugin
            public void setOpenFlag(int flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flag);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setOpenFlag(flag);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IHwDeviceUsagePlugin
            public void setScreenOnTime(long time) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(time);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setScreenOnTime(time);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IHwDeviceUsagePlugin
            public void setChargeTime(long time) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(time);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setChargeTime(time);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IHwDeviceUsagePlugin
            public void setTalkTime(long time) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(time);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setTalkTime(time);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IHwDeviceUsagePlugin
            public void setFristUseTime(long time) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(time);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setFristUseTime(time);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IHwDeviceUsagePlugin
            public boolean isDeviceActivated() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDeviceActivated();
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

            @Override // huawei.android.security.IHwDeviceUsagePlugin
            public void detectActivationWithDuration(long duration) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(duration);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().detectActivationWithDuration(duration);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.IHwDeviceUsagePlugin
            public void resetActivation() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resetActivation();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwDeviceUsagePlugin impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwDeviceUsagePlugin getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
