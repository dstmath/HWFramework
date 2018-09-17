package android.os;

public interface IBatteryPropertiesListener extends IInterface {

    public static abstract class Stub extends Binder implements IBatteryPropertiesListener {
        private static final String DESCRIPTOR = "android.os.IBatteryPropertiesListener";
        static final int TRANSACTION_batteryPropertiesChanged = 1;

        private static class Proxy implements IBatteryPropertiesListener {
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

            public void batteryPropertiesChanged(BatteryProperties props) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (props != null) {
                        _data.writeInt(Stub.TRANSACTION_batteryPropertiesChanged);
                        props.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_batteryPropertiesChanged, _data, null, Stub.TRANSACTION_batteryPropertiesChanged);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBatteryPropertiesListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBatteryPropertiesListener)) {
                return new Proxy(obj);
            }
            return (IBatteryPropertiesListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_batteryPropertiesChanged /*1*/:
                    BatteryProperties batteryProperties;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        batteryProperties = (BatteryProperties) BatteryProperties.CREATOR.createFromParcel(data);
                    } else {
                        batteryProperties = null;
                    }
                    batteryPropertiesChanged(batteryProperties);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void batteryPropertiesChanged(BatteryProperties batteryProperties) throws RemoteException;
}
