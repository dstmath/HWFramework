package android.os;

public interface IBatteryPropertiesRegistrar extends IInterface {

    public static abstract class Stub extends Binder implements IBatteryPropertiesRegistrar {
        private static final String DESCRIPTOR = "android.os.IBatteryPropertiesRegistrar";
        static final int TRANSACTION_getProperty = 3;
        static final int TRANSACTION_registerListener = 1;
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
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterListener(IBatteryPropertiesListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterListener, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getProperty, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        prop.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
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
            switch (code) {
                case TRANSACTION_registerListener /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    registerListener(android.os.IBatteryPropertiesListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unregisterListener /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterListener(android.os.IBatteryPropertiesListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getProperty /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    BatteryProperty _arg1 = new BatteryProperty();
                    int _result = getProperty(_arg0, _arg1);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (_arg1 != null) {
                        reply.writeInt(TRANSACTION_registerListener);
                        _arg1.writeToParcel(reply, TRANSACTION_registerListener);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int getProperty(int i, BatteryProperty batteryProperty) throws RemoteException;

    void registerListener(IBatteryPropertiesListener iBatteryPropertiesListener) throws RemoteException;

    void unregisterListener(IBatteryPropertiesListener iBatteryPropertiesListener) throws RemoteException;
}
