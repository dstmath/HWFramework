package android.os;

public interface IHwBrightnessCallback extends IInterface {

    public static abstract class Stub extends Binder implements IHwBrightnessCallback {
        private static final String DESCRIPTOR = "android.os.IHwBrightnessCallback";
        static final int TRANSACTION_onStatusChanged = 1;

        private static class Proxy implements IHwBrightnessCallback {
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

            public void onStatusChanged(String what, int arg1, int arg2, Bundle data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(what);
                    _data.writeInt(arg1);
                    _data.writeInt(arg2);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
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

        public static IHwBrightnessCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwBrightnessCallback)) {
                return new Proxy(obj);
            }
            return (IHwBrightnessCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg3;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                String _arg0 = data.readString();
                int _arg1 = data.readInt();
                int _arg2 = data.readInt();
                if (data.readInt() != 0) {
                    _arg3 = Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg3 = null;
                }
                onStatusChanged(_arg0, _arg1, _arg2, _arg3);
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void onStatusChanged(String str, int i, int i2, Bundle bundle) throws RemoteException;
}
