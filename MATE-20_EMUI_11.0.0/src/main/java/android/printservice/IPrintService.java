package android.printservice;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.print.PrintJobInfo;
import android.print.PrinterId;
import android.printservice.IPrintServiceClient;
import java.util.List;

public interface IPrintService extends IInterface {
    void createPrinterDiscoverySession() throws RemoteException;

    void destroyPrinterDiscoverySession() throws RemoteException;

    void onPrintJobQueued(PrintJobInfo printJobInfo) throws RemoteException;

    void requestCancelPrintJob(PrintJobInfo printJobInfo) throws RemoteException;

    void requestCustomPrinterIcon(PrinterId printerId) throws RemoteException;

    void setClient(IPrintServiceClient iPrintServiceClient) throws RemoteException;

    void startPrinterDiscovery(List<PrinterId> list) throws RemoteException;

    void startPrinterStateTracking(PrinterId printerId) throws RemoteException;

    void stopPrinterDiscovery() throws RemoteException;

    void stopPrinterStateTracking(PrinterId printerId) throws RemoteException;

    void validatePrinters(List<PrinterId> list) throws RemoteException;

    public static class Default implements IPrintService {
        @Override // android.printservice.IPrintService
        public void setClient(IPrintServiceClient client) throws RemoteException {
        }

        @Override // android.printservice.IPrintService
        public void requestCancelPrintJob(PrintJobInfo printJobInfo) throws RemoteException {
        }

        @Override // android.printservice.IPrintService
        public void onPrintJobQueued(PrintJobInfo printJobInfo) throws RemoteException {
        }

        @Override // android.printservice.IPrintService
        public void createPrinterDiscoverySession() throws RemoteException {
        }

        @Override // android.printservice.IPrintService
        public void startPrinterDiscovery(List<PrinterId> list) throws RemoteException {
        }

        @Override // android.printservice.IPrintService
        public void stopPrinterDiscovery() throws RemoteException {
        }

        @Override // android.printservice.IPrintService
        public void validatePrinters(List<PrinterId> list) throws RemoteException {
        }

        @Override // android.printservice.IPrintService
        public void startPrinterStateTracking(PrinterId printerId) throws RemoteException {
        }

        @Override // android.printservice.IPrintService
        public void requestCustomPrinterIcon(PrinterId printerId) throws RemoteException {
        }

        @Override // android.printservice.IPrintService
        public void stopPrinterStateTracking(PrinterId printerId) throws RemoteException {
        }

        @Override // android.printservice.IPrintService
        public void destroyPrinterDiscoverySession() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IPrintService {
        private static final String DESCRIPTOR = "android.printservice.IPrintService";
        static final int TRANSACTION_createPrinterDiscoverySession = 4;
        static final int TRANSACTION_destroyPrinterDiscoverySession = 11;
        static final int TRANSACTION_onPrintJobQueued = 3;
        static final int TRANSACTION_requestCancelPrintJob = 2;
        static final int TRANSACTION_requestCustomPrinterIcon = 9;
        static final int TRANSACTION_setClient = 1;
        static final int TRANSACTION_startPrinterDiscovery = 5;
        static final int TRANSACTION_startPrinterStateTracking = 8;
        static final int TRANSACTION_stopPrinterDiscovery = 6;
        static final int TRANSACTION_stopPrinterStateTracking = 10;
        static final int TRANSACTION_validatePrinters = 7;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPrintService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPrintService)) {
                return new Proxy(obj);
            }
            return (IPrintService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "setClient";
                case 2:
                    return "requestCancelPrintJob";
                case 3:
                    return "onPrintJobQueued";
                case 4:
                    return "createPrinterDiscoverySession";
                case 5:
                    return "startPrinterDiscovery";
                case 6:
                    return "stopPrinterDiscovery";
                case 7:
                    return "validatePrinters";
                case 8:
                    return "startPrinterStateTracking";
                case 9:
                    return "requestCustomPrinterIcon";
                case 10:
                    return "stopPrinterStateTracking";
                case 11:
                    return "destroyPrinterDiscoverySession";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PrintJobInfo _arg0;
            PrintJobInfo _arg02;
            PrinterId _arg03;
            PrinterId _arg04;
            PrinterId _arg05;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        setClient(IPrintServiceClient.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = PrintJobInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        requestCancelPrintJob(_arg0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = PrintJobInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        onPrintJobQueued(_arg02);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        createPrinterDiscoverySession();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        startPrinterDiscovery(data.createTypedArrayList(PrinterId.CREATOR));
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        stopPrinterDiscovery();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        validatePrinters(data.createTypedArrayList(PrinterId.CREATOR));
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = PrinterId.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        startPrinterStateTracking(_arg03);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = PrinterId.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        requestCustomPrinterIcon(_arg04);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = PrinterId.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        stopPrinterStateTracking(_arg05);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        destroyPrinterDiscoverySession();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IPrintService {
            public static IPrintService sDefaultImpl;
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

            @Override // android.printservice.IPrintService
            public void setClient(IPrintServiceClient client) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(client != null ? client.asBinder() : null);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setClient(client);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.printservice.IPrintService
            public void requestCancelPrintJob(PrintJobInfo printJobInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJobInfo != null) {
                        _data.writeInt(1);
                        printJobInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().requestCancelPrintJob(printJobInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.printservice.IPrintService
            public void onPrintJobQueued(PrintJobInfo printJobInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJobInfo != null) {
                        _data.writeInt(1);
                        printJobInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onPrintJobQueued(printJobInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.printservice.IPrintService
            public void createPrinterDiscoverySession() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().createPrinterDiscoverySession();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.printservice.IPrintService
            public void startPrinterDiscovery(List<PrinterId> priorityList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(priorityList);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().startPrinterDiscovery(priorityList);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.printservice.IPrintService
            public void stopPrinterDiscovery() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().stopPrinterDiscovery();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.printservice.IPrintService
            public void validatePrinters(List<PrinterId> printerIds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(printerIds);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().validatePrinters(printerIds);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.printservice.IPrintService
            public void startPrinterStateTracking(PrinterId printerId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printerId != null) {
                        _data.writeInt(1);
                        printerId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().startPrinterStateTracking(printerId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.printservice.IPrintService
            public void requestCustomPrinterIcon(PrinterId printerId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printerId != null) {
                        _data.writeInt(1);
                        printerId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().requestCustomPrinterIcon(printerId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.printservice.IPrintService
            public void stopPrinterStateTracking(PrinterId printerId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printerId != null) {
                        _data.writeInt(1);
                        printerId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().stopPrinterStateTracking(printerId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.printservice.IPrintService
            public void destroyPrinterDiscoverySession() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().destroyPrinterDiscoverySession();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IPrintService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IPrintService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
