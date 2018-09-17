package android.printservice;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.print.PrintJobInfo;
import android.print.PrinterId;
import java.util.List;

public interface IPrintService extends IInterface {

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

        private static class Proxy implements IPrintService {
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

            public void setClient(IPrintServiceClient client) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void createPrinterDiscoverySession() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void startPrinterDiscovery(List<PrinterId> priorityList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(priorityList);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void stopPrinterDiscovery() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void validatePrinters(List<PrinterId> printerIds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(printerIds);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void destroyPrinterDiscoverySession() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PrintJobInfo _arg0;
            PrinterId _arg02;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    setClient(android.printservice.IPrintServiceClient.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (PrintJobInfo) PrintJobInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    requestCancelPrintJob(_arg0);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (PrintJobInfo) PrintJobInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    onPrintJobQueued(_arg0);
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
                        _arg02 = (PrinterId) PrinterId.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    startPrinterStateTracking(_arg02);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = (PrinterId) PrinterId.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    requestCustomPrinterIcon(_arg02);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = (PrinterId) PrinterId.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    stopPrinterStateTracking(_arg02);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    destroyPrinterDiscoverySession();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

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
}
