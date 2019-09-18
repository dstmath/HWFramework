package android.os;

import android.os.IBatteryPropertiesListener;

public interface IBatteryPropertiesRegistrar extends IInterface {

    public static abstract class Stub extends Binder implements IBatteryPropertiesRegistrar {
        private static final String DESCRIPTOR = "android.os.IBatteryPropertiesRegistrar";
        static final int TRANSACTION_alterWirelessTxSwitch = 5;
        static final int TRANSACTION_getProperty = 3;
        static final int TRANSACTION_getWirelessTxSwitch = 6;
        static final int TRANSACTION_registerListener = 1;
        static final int TRANSACTION_scheduleUpdate = 4;
        static final int TRANSACTION_supportWirelessTxCharge = 7;
        static final int TRANSACTION_unregisterListener = 2;

        private static class Proxy implements IBatteryPropertiesRegistrar {
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

            public void registerListener(IBatteryPropertiesListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterListener(IBatteryPropertiesListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getProperty(int id, BatteryProperty prop) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        prop.readFromParcel(_reply);
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void scheduleUpdate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public int alterWirelessTxSwitch(int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getWirelessTxSwitch() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean supportWirelessTxCharge() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBatteryPropertiesRegistrar asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBatteryPropertiesRegistrar)) {
                return new Proxy(obj);
            }
            return (IBatteryPropertiesRegistrar) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        registerListener(IBatteryPropertiesListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterListener(IBatteryPropertiesListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        BatteryProperty _arg1 = new BatteryProperty();
                        int _result = getProperty(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result);
                        reply.writeInt(1);
                        _arg1.writeToParcel(reply, 1);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        scheduleUpdate();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = alterWirelessTxSwitch(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = getWirelessTxSwitch();
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result4 = supportWirelessTxCharge();
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    int alterWirelessTxSwitch(int i) throws RemoteException;

    int getProperty(int i, BatteryProperty batteryProperty) throws RemoteException;

    int getWirelessTxSwitch() throws RemoteException;

    void registerListener(IBatteryPropertiesListener iBatteryPropertiesListener) throws RemoteException;

    void scheduleUpdate() throws RemoteException;

    boolean supportWirelessTxCharge() throws RemoteException;

    void unregisterListener(IBatteryPropertiesListener iBatteryPropertiesListener) throws RemoteException;
}
