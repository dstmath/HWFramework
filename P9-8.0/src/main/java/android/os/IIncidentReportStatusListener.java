package android.os;

public interface IIncidentReportStatusListener extends IInterface {
    public static final int STATUS_FINISHED = 2;
    public static final int STATUS_STARTING = 1;

    public static abstract class Stub extends Binder implements IIncidentReportStatusListener {
        private static final String DESCRIPTOR = "android.os.IIncidentReportStatusListener";
        static final int TRANSACTION_onReportFailed = 4;
        static final int TRANSACTION_onReportFinished = 3;
        static final int TRANSACTION_onReportSectionStatus = 2;
        static final int TRANSACTION_onReportStarted = 1;

        private static class Proxy implements IIncidentReportStatusListener {
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

            public void onReportStarted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onReportSectionStatus(int section, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(section);
                    _data.writeInt(status);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onReportFinished() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onReportFailed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIncidentReportStatusListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIncidentReportStatusListener)) {
                return new Proxy(obj);
            }
            return (IIncidentReportStatusListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onReportStarted();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    onReportSectionStatus(data.readInt(), data.readInt());
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    onReportFinished();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    onReportFailed();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onReportFailed() throws RemoteException;

    void onReportFinished() throws RemoteException;

    void onReportSectionStatus(int i, int i2) throws RemoteException;

    void onReportStarted() throws RemoteException;
}
