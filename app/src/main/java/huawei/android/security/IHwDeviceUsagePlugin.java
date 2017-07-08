package huawei.android.security;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwDeviceUsagePlugin extends IInterface {

    public static abstract class Stub extends Binder implements IHwDeviceUsagePlugin {
        private static final String DESCRIPTOR = "huawei.android.security.IHwDeviceUsagePlugin";
        static final int TRANSACTION_getChargeTime = 2;
        static final int TRANSACTION_getFristUseTime = 4;
        static final int TRANSACTION_getScreenOnTime = 1;
        static final int TRANSACTION_getTalkTime = 3;
        static final int TRANSACTION_setChargeTime = 7;
        static final int TRANSACTION_setFristUseTime = 9;
        static final int TRANSACTION_setOpenFlag = 5;
        static final int TRANSACTION_setScreenOnTime = 6;
        static final int TRANSACTION_setTalkTime = 8;

        private static class Proxy implements IHwDeviceUsagePlugin {
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

            public long getScreenOnTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getScreenOnTime, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getChargeTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getChargeTime, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getTalkTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getTalkTime, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getFristUseTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getFristUseTime, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setOpenFlag(int flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flag);
                    this.mRemote.transact(Stub.TRANSACTION_setOpenFlag, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setScreenOnTime(long time) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(time);
                    this.mRemote.transact(Stub.TRANSACTION_setScreenOnTime, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setChargeTime(long time) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(time);
                    this.mRemote.transact(Stub.TRANSACTION_setChargeTime, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setTalkTime(long time) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(time);
                    this.mRemote.transact(Stub.TRANSACTION_setTalkTime, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setFristUseTime(long time) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(time);
                    this.mRemote.transact(Stub.TRANSACTION_setFristUseTime, _data, _reply, 0);
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            long _result;
            switch (code) {
                case TRANSACTION_getScreenOnTime /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getScreenOnTime();
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case TRANSACTION_getChargeTime /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getChargeTime();
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case TRANSACTION_getTalkTime /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getTalkTime();
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case TRANSACTION_getFristUseTime /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getFristUseTime();
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case TRANSACTION_setOpenFlag /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    setOpenFlag(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setScreenOnTime /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    setScreenOnTime(data.readLong());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setChargeTime /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    setChargeTime(data.readLong());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setTalkTime /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    setTalkTime(data.readLong());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setFristUseTime /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    setFristUseTime(data.readLong());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    long getChargeTime() throws RemoteException;

    long getFristUseTime() throws RemoteException;

    long getScreenOnTime() throws RemoteException;

    long getTalkTime() throws RemoteException;

    void setChargeTime(long j) throws RemoteException;

    void setFristUseTime(long j) throws RemoteException;

    void setOpenFlag(int i) throws RemoteException;

    void setScreenOnTime(long j) throws RemoteException;

    void setTalkTime(long j) throws RemoteException;
}
