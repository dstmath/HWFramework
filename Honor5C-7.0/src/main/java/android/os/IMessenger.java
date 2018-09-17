package android.os;

public interface IMessenger extends IInterface {

    public static abstract class Stub extends Binder implements IMessenger {
        private static final String DESCRIPTOR = "android.os.IMessenger";
        static final int TRANSACTION_send = 1;

        private static class Proxy implements IMessenger {
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

            public void send(Message msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (msg != null) {
                        _data.writeInt(Stub.TRANSACTION_send);
                        msg.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_send, _data, null, Stub.TRANSACTION_send);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMessenger asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMessenger)) {
                return new Proxy(obj);
            }
            return (IMessenger) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_send /*1*/:
                    Message message;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        message = (Message) Message.CREATOR.createFromParcel(data);
                    } else {
                        message = null;
                    }
                    send(message);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void send(Message message) throws RemoteException;
}
