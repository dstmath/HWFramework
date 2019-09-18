package android.os;

public interface IBackupSessionCallback extends IInterface {

    public static abstract class Stub extends Binder implements IBackupSessionCallback {
        private static final String DESCRIPTOR = "android.os.IBackupSessionCallback";
        static final int TRANSACTION_onTaskStatusChanged = 1;

        private static class Proxy implements IBackupSessionCallback {
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

            public void onTaskStatusChanged(int sessionId, int taskId, int statusCode, String appendData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeInt(taskId);
                    _data.writeInt(statusCode);
                    _data.writeString(appendData);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBackupSessionCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBackupSessionCallback)) {
                return new Proxy(obj);
            }
            return (IBackupSessionCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onTaskStatusChanged(data.readInt(), data.readInt(), data.readInt(), data.readString());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void onTaskStatusChanged(int i, int i2, int i3, String str) throws RemoteException;
}
