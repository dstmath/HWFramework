package android.os;

public interface IBatteryPropertiesRegistrar extends IInterface {
    int alterWirelessTxSwitch(int i) throws RemoteException;

    int getProperty(int i, BatteryProperty batteryProperty) throws RemoteException;

    int getWirelessTxSwitch() throws RemoteException;

    void scheduleUpdate() throws RemoteException;

    boolean supportWirelessTxCharge() throws RemoteException;

    public static class Default implements IBatteryPropertiesRegistrar {
        @Override // android.os.IBatteryPropertiesRegistrar
        public int getProperty(int id, BatteryProperty prop) throws RemoteException {
            return 0;
        }

        @Override // android.os.IBatteryPropertiesRegistrar
        public void scheduleUpdate() throws RemoteException {
        }

        @Override // android.os.IBatteryPropertiesRegistrar
        public int alterWirelessTxSwitch(int status) throws RemoteException {
            return 0;
        }

        @Override // android.os.IBatteryPropertiesRegistrar
        public int getWirelessTxSwitch() throws RemoteException {
            return 0;
        }

        @Override // android.os.IBatteryPropertiesRegistrar
        public boolean supportWirelessTxCharge() throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IBatteryPropertiesRegistrar {
        private static final String DESCRIPTOR = "android.os.IBatteryPropertiesRegistrar";
        static final int TRANSACTION_alterWirelessTxSwitch = 3;
        static final int TRANSACTION_getProperty = 1;
        static final int TRANSACTION_getWirelessTxSwitch = 4;
        static final int TRANSACTION_scheduleUpdate = 2;
        static final int TRANSACTION_supportWirelessTxCharge = 5;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "getProperty";
            }
            if (transactionCode == 2) {
                return "scheduleUpdate";
            }
            if (transactionCode == 3) {
                return "alterWirelessTxSwitch";
            }
            if (transactionCode == 4) {
                return "getWirelessTxSwitch";
            }
            if (transactionCode != 5) {
                return null;
            }
            return "supportWirelessTxCharge";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _arg0 = data.readInt();
                BatteryProperty _arg1 = new BatteryProperty();
                int _result = getProperty(_arg0, _arg1);
                reply.writeNoException();
                reply.writeInt(_result);
                reply.writeInt(1);
                _arg1.writeToParcel(reply, 1);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                scheduleUpdate();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int _result2 = alterWirelessTxSwitch(data.readInt());
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                int _result3 = getWirelessTxSwitch();
                reply.writeNoException();
                reply.writeInt(_result3);
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                boolean supportWirelessTxCharge = supportWirelessTxCharge();
                reply.writeNoException();
                reply.writeInt(supportWirelessTxCharge ? 1 : 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IBatteryPropertiesRegistrar {
            public static IBatteryPropertiesRegistrar sDefaultImpl;
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

            @Override // android.os.IBatteryPropertiesRegistrar
            public int getProperty(int id, BatteryProperty prop) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getProperty(id, prop);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        prop.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IBatteryPropertiesRegistrar
            public void scheduleUpdate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scheduleUpdate();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IBatteryPropertiesRegistrar
            public int alterWirelessTxSwitch(int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().alterWirelessTxSwitch(status);
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

            @Override // android.os.IBatteryPropertiesRegistrar
            public int getWirelessTxSwitch() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWirelessTxSwitch();
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

            @Override // android.os.IBatteryPropertiesRegistrar
            public boolean supportWirelessTxCharge() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().supportWirelessTxCharge();
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
        }

        public static boolean setDefaultImpl(IBatteryPropertiesRegistrar impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IBatteryPropertiesRegistrar getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
