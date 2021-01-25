package android.os;

public interface IIncidentReportStatusListener extends IInterface {
    public static final int STATUS_FINISHED = 2;
    public static final int STATUS_STARTING = 1;

    void onReportFailed() throws RemoteException;

    void onReportFinished() throws RemoteException;

    void onReportSectionStatus(int i, int i2) throws RemoteException;

    void onReportStarted() throws RemoteException;

    public static class Default implements IIncidentReportStatusListener {
        @Override // android.os.IIncidentReportStatusListener
        public void onReportStarted() throws RemoteException {
        }

        @Override // android.os.IIncidentReportStatusListener
        public void onReportSectionStatus(int section, int status) throws RemoteException {
        }

        @Override // android.os.IIncidentReportStatusListener
        public void onReportFinished() throws RemoteException {
        }

        @Override // android.os.IIncidentReportStatusListener
        public void onReportFailed() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IIncidentReportStatusListener {
        private static final String DESCRIPTOR = "android.os.IIncidentReportStatusListener";
        static final int TRANSACTION_onReportFailed = 4;
        static final int TRANSACTION_onReportFinished = 3;
        static final int TRANSACTION_onReportSectionStatus = 2;
        static final int TRANSACTION_onReportStarted = 1;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onReportStarted";
            }
            if (transactionCode == 2) {
                return "onReportSectionStatus";
            }
            if (transactionCode == 3) {
                return "onReportFinished";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "onReportFailed";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onReportStarted();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onReportSectionStatus(data.readInt(), data.readInt());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onReportFinished();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                onReportFailed();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IIncidentReportStatusListener {
            public static IIncidentReportStatusListener sDefaultImpl;
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

            @Override // android.os.IIncidentReportStatusListener
            public void onReportStarted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onReportStarted();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IIncidentReportStatusListener
            public void onReportSectionStatus(int section, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(section);
                    _data.writeInt(status);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onReportSectionStatus(section, status);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IIncidentReportStatusListener
            public void onReportFinished() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onReportFinished();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IIncidentReportStatusListener
            public void onReportFailed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onReportFailed();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IIncidentReportStatusListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IIncidentReportStatusListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
