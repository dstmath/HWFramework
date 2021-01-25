package android.print;

import android.content.ComponentName;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPrintSpoolerClient extends IInterface {
    void onAllPrintJobsForServiceHandled(ComponentName componentName) throws RemoteException;

    void onAllPrintJobsHandled() throws RemoteException;

    void onPrintJobQueued(PrintJobInfo printJobInfo) throws RemoteException;

    void onPrintJobStateChanged(PrintJobInfo printJobInfo) throws RemoteException;

    public static class Default implements IPrintSpoolerClient {
        @Override // android.print.IPrintSpoolerClient
        public void onPrintJobQueued(PrintJobInfo printJob) throws RemoteException {
        }

        @Override // android.print.IPrintSpoolerClient
        public void onAllPrintJobsForServiceHandled(ComponentName printService) throws RemoteException {
        }

        @Override // android.print.IPrintSpoolerClient
        public void onAllPrintJobsHandled() throws RemoteException {
        }

        @Override // android.print.IPrintSpoolerClient
        public void onPrintJobStateChanged(PrintJobInfo printJob) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IPrintSpoolerClient {
        private static final String DESCRIPTOR = "android.print.IPrintSpoolerClient";
        static final int TRANSACTION_onAllPrintJobsForServiceHandled = 2;
        static final int TRANSACTION_onAllPrintJobsHandled = 3;
        static final int TRANSACTION_onPrintJobQueued = 1;
        static final int TRANSACTION_onPrintJobStateChanged = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPrintSpoolerClient asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPrintSpoolerClient)) {
                return new Proxy(obj);
            }
            return (IPrintSpoolerClient) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onPrintJobQueued";
            }
            if (transactionCode == 2) {
                return "onAllPrintJobsForServiceHandled";
            }
            if (transactionCode == 3) {
                return "onAllPrintJobsHandled";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "onPrintJobStateChanged";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PrintJobInfo _arg0;
            ComponentName _arg02;
            PrintJobInfo _arg03;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = PrintJobInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onPrintJobQueued(_arg0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = ComponentName.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                onAllPrintJobsForServiceHandled(_arg02);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onAllPrintJobsHandled();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg03 = PrintJobInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg03 = null;
                }
                onPrintJobStateChanged(_arg03);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IPrintSpoolerClient {
            public static IPrintSpoolerClient sDefaultImpl;
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

            @Override // android.print.IPrintSpoolerClient
            public void onPrintJobQueued(PrintJobInfo printJob) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJob != null) {
                        _data.writeInt(1);
                        printJob.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onPrintJobQueued(printJob);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.print.IPrintSpoolerClient
            public void onAllPrintJobsForServiceHandled(ComponentName printService) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printService != null) {
                        _data.writeInt(1);
                        printService.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAllPrintJobsForServiceHandled(printService);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.print.IPrintSpoolerClient
            public void onAllPrintJobsHandled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAllPrintJobsHandled();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.print.IPrintSpoolerClient
            public void onPrintJobStateChanged(PrintJobInfo printJob) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJob != null) {
                        _data.writeInt(1);
                        printJob.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onPrintJobStateChanged(printJob);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IPrintSpoolerClient impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IPrintSpoolerClient getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
