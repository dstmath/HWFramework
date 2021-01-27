package android.os;

public interface IBackupSessionCallback extends IInterface {
    void onTaskStatusChanged(int i, int i2, int i3, String str) throws RemoteException;

    public static class Default implements IBackupSessionCallback {
        @Override // android.os.IBackupSessionCallback
        public void onTaskStatusChanged(int sessionId, int taskId, int statusCode, String appendData) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IBackupSessionCallback {
        private static final String DESCRIPTOR = "android.os.IBackupSessionCallback";
        static final int TRANSACTION_onTaskStatusChanged = 1;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
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

        /* access modifiers changed from: private */
        public static class Proxy implements IBackupSessionCallback {
            public static IBackupSessionCallback sDefaultImpl;
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

            @Override // android.os.IBackupSessionCallback
            public void onTaskStatusChanged(int sessionId, int taskId, int statusCode, String appendData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionId);
                    _data.writeInt(taskId);
                    _data.writeInt(statusCode);
                    _data.writeString(appendData);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onTaskStatusChanged(sessionId, taskId, statusCode, appendData);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IBackupSessionCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IBackupSessionCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
