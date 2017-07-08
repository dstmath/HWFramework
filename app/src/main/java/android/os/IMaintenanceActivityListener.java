package android.os;

public interface IMaintenanceActivityListener extends IInterface {

    public static abstract class Stub extends Binder implements IMaintenanceActivityListener {
        private static final String DESCRIPTOR = "android.os.IMaintenanceActivityListener";
        static final int TRANSACTION_onMaintenanceActivityChanged = 1;

        private static class Proxy implements IMaintenanceActivityListener {
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

            public void onMaintenanceActivityChanged(boolean active) throws RemoteException {
                int i = Stub.TRANSACTION_onMaintenanceActivityChanged;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!active) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onMaintenanceActivityChanged, _data, null, Stub.TRANSACTION_onMaintenanceActivityChanged);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMaintenanceActivityListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMaintenanceActivityListener)) {
                return new Proxy(obj);
            }
            return (IMaintenanceActivityListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg0 = false;
            switch (code) {
                case TRANSACTION_onMaintenanceActivityChanged /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    }
                    onMaintenanceActivityChanged(_arg0);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onMaintenanceActivityChanged(boolean z) throws RemoteException;
}
