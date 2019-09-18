package android.print;

import android.content.ComponentName;
import android.graphics.drawable.Icon;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.print.IPrintSpoolerCallbacks;
import android.print.IPrintSpoolerClient;
import android.text.TextUtils;
import java.util.List;

public interface IPrintSpooler extends IInterface {

    public static abstract class Stub extends Binder implements IPrintSpooler {
        private static final String DESCRIPTOR = "android.print.IPrintSpooler";
        static final int TRANSACTION_clearCustomPrinterIconCache = 11;
        static final int TRANSACTION_createPrintJob = 4;
        static final int TRANSACTION_getCustomPrinterIcon = 10;
        static final int TRANSACTION_getPrintJobInfo = 3;
        static final int TRANSACTION_getPrintJobInfos = 2;
        static final int TRANSACTION_onCustomPrinterIconLoaded = 9;
        static final int TRANSACTION_pruneApprovedPrintServices = 16;
        static final int TRANSACTION_removeObsoletePrintJobs = 1;
        static final int TRANSACTION_setClient = 14;
        static final int TRANSACTION_setPrintJobCancelling = 15;
        static final int TRANSACTION_setPrintJobState = 5;
        static final int TRANSACTION_setPrintJobTag = 12;
        static final int TRANSACTION_setProgress = 6;
        static final int TRANSACTION_setStatus = 7;
        static final int TRANSACTION_setStatusRes = 8;
        static final int TRANSACTION_writePrintJobData = 13;

        private static class Proxy implements IPrintSpooler {
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

            public void removeObsoletePrintJobs() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void getPrintJobInfos(IPrintSpoolerCallbacks callback, ComponentName componentName, int state, int appId, int sequence) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(state);
                    _data.writeInt(appId);
                    _data.writeInt(sequence);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void getPrintJobInfo(PrintJobId printJobId, IPrintSpoolerCallbacks callback, int appId, int sequence) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJobId != null) {
                        _data.writeInt(1);
                        printJobId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(appId);
                    _data.writeInt(sequence);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void createPrintJob(PrintJobInfo printJob) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJob != null) {
                        _data.writeInt(1);
                        printJob.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setPrintJobState(PrintJobId printJobId, int status, String stateReason, IPrintSpoolerCallbacks callback, int sequence) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJobId != null) {
                        _data.writeInt(1);
                        printJobId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(status);
                    _data.writeString(stateReason);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(sequence);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setProgress(PrintJobId printJobId, float progress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJobId != null) {
                        _data.writeInt(1);
                        printJobId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeFloat(progress);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setStatus(PrintJobId printJobId, CharSequence status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJobId != null) {
                        _data.writeInt(1);
                        printJobId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (status != null) {
                        _data.writeInt(1);
                        TextUtils.writeToParcel(status, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setStatusRes(PrintJobId printJobId, int status, CharSequence appPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJobId != null) {
                        _data.writeInt(1);
                        printJobId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(status);
                    if (appPackageName != null) {
                        _data.writeInt(1);
                        TextUtils.writeToParcel(appPackageName, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onCustomPrinterIconLoaded(PrinterId printerId, Icon icon, IPrintSpoolerCallbacks callbacks, int sequence) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printerId != null) {
                        _data.writeInt(1);
                        printerId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (icon != null) {
                        _data.writeInt(1);
                        icon.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callbacks != null ? callbacks.asBinder() : null);
                    _data.writeInt(sequence);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void getCustomPrinterIcon(PrinterId printerId, IPrintSpoolerCallbacks callbacks, int sequence) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printerId != null) {
                        _data.writeInt(1);
                        printerId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callbacks != null ? callbacks.asBinder() : null);
                    _data.writeInt(sequence);
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void clearCustomPrinterIconCache(IPrintSpoolerCallbacks callbacks, int sequence) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callbacks != null ? callbacks.asBinder() : null);
                    _data.writeInt(sequence);
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setPrintJobTag(PrintJobId printJobId, String tag, IPrintSpoolerCallbacks callback, int sequence) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJobId != null) {
                        _data.writeInt(1);
                        printJobId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(tag);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(sequence);
                    this.mRemote.transact(12, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void writePrintJobData(ParcelFileDescriptor fd, PrintJobId printJobId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (fd != null) {
                        _data.writeInt(1);
                        fd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (printJobId != null) {
                        _data.writeInt(1);
                        printJobId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(13, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setClient(IPrintSpoolerClient client) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(client != null ? client.asBinder() : null);
                    this.mRemote.transact(14, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setPrintJobCancelling(PrintJobId printJobId, boolean cancelling) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJobId != null) {
                        _data.writeInt(1);
                        printJobId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(cancelling);
                    this.mRemote.transact(15, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void pruneApprovedPrintServices(List<ComponentName> servicesToKeep) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(servicesToKeep);
                    this.mRemote.transact(16, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPrintSpooler asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPrintSpooler)) {
                return new Proxy(obj);
            }
            return (IPrintSpooler) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v8, resolved type: android.print.PrintJobId} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v12, resolved type: android.print.PrintJobInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v22, resolved type: android.print.PrintJobId} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v34, resolved type: android.graphics.drawable.Icon} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v38, resolved type: android.print.PrinterId} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v44, resolved type: android.print.PrintJobId} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v48, resolved type: android.print.PrintJobId} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v54, resolved type: android.print.PrintJobId} */
        /* JADX WARNING: type inference failed for: r0v2 */
        /* JADX WARNING: type inference failed for: r0v3 */
        /* JADX WARNING: type inference failed for: r0v16 */
        /* JADX WARNING: type inference failed for: r0v26, types: [java.lang.CharSequence] */
        /* JADX WARNING: type inference failed for: r0v30, types: [java.lang.CharSequence] */
        /* JADX WARNING: type inference failed for: r0v61 */
        /* JADX WARNING: type inference failed for: r0v62 */
        /* JADX WARNING: type inference failed for: r0v63 */
        /* JADX WARNING: type inference failed for: r0v64 */
        /* JADX WARNING: type inference failed for: r0v65 */
        /* JADX WARNING: type inference failed for: r0v66 */
        /* JADX WARNING: type inference failed for: r0v67 */
        /* JADX WARNING: type inference failed for: r0v68 */
        /* JADX WARNING: type inference failed for: r0v69 */
        /* JADX WARNING: type inference failed for: r0v70 */
        /* JADX WARNING: type inference failed for: r0v71 */
        /* JADX WARNING: type inference failed for: r0v72 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PrintJobId _arg0;
            PrintJobId _arg02;
            PrinterId _arg03;
            ParcelFileDescriptor _arg04;
            int i = code;
            Parcel parcel = data;
            if (i != 1598968902) {
                ? _arg05 = 0;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        removeObsoletePrintJobs();
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        IPrintSpoolerCallbacks _arg06 = IPrintSpoolerCallbacks.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg05 = (ComponentName) ComponentName.CREATOR.createFromParcel(parcel);
                        }
                        getPrintJobInfos(_arg06, _arg05, data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = PrintJobId.CREATOR.createFromParcel(parcel);
                        }
                        getPrintJobInfo(_arg05, IPrintSpoolerCallbacks.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = PrintJobInfo.CREATOR.createFromParcel(parcel);
                        }
                        createPrintJob(_arg05);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = PrintJobId.CREATOR.createFromParcel(parcel);
                        }
                        setPrintJobState(_arg05, data.readInt(), data.readString(), IPrintSpoolerCallbacks.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = PrintJobId.CREATOR.createFromParcel(parcel);
                        }
                        setProgress(_arg05, data.readFloat());
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = PrintJobId.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg05 = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
                        }
                        setStatus(_arg0, _arg05);
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = PrintJobId.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg02 = null;
                        }
                        int _arg1 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg05 = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
                        }
                        setStatusRes(_arg02, _arg1, _arg05);
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = PrinterId.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg03 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg05 = (Icon) Icon.CREATOR.createFromParcel(parcel);
                        }
                        onCustomPrinterIconLoaded(_arg03, _arg05, IPrintSpoolerCallbacks.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = PrinterId.CREATOR.createFromParcel(parcel);
                        }
                        getCustomPrinterIcon(_arg05, IPrintSpoolerCallbacks.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        clearCustomPrinterIconCache(IPrintSpoolerCallbacks.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = PrintJobId.CREATOR.createFromParcel(parcel);
                        }
                        setPrintJobTag(_arg05, data.readString(), IPrintSpoolerCallbacks.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = ParcelFileDescriptor.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg04 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg05 = PrintJobId.CREATOR.createFromParcel(parcel);
                        }
                        writePrintJobData(_arg04, _arg05);
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        setClient(IPrintSpoolerClient.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = PrintJobId.CREATOR.createFromParcel(parcel);
                        }
                        setPrintJobCancelling(_arg05, data.readInt() != 0);
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        pruneApprovedPrintServices(parcel.createTypedArrayList(ComponentName.CREATOR));
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void clearCustomPrinterIconCache(IPrintSpoolerCallbacks iPrintSpoolerCallbacks, int i) throws RemoteException;

    void createPrintJob(PrintJobInfo printJobInfo) throws RemoteException;

    void getCustomPrinterIcon(PrinterId printerId, IPrintSpoolerCallbacks iPrintSpoolerCallbacks, int i) throws RemoteException;

    void getPrintJobInfo(PrintJobId printJobId, IPrintSpoolerCallbacks iPrintSpoolerCallbacks, int i, int i2) throws RemoteException;

    void getPrintJobInfos(IPrintSpoolerCallbacks iPrintSpoolerCallbacks, ComponentName componentName, int i, int i2, int i3) throws RemoteException;

    void onCustomPrinterIconLoaded(PrinterId printerId, Icon icon, IPrintSpoolerCallbacks iPrintSpoolerCallbacks, int i) throws RemoteException;

    void pruneApprovedPrintServices(List<ComponentName> list) throws RemoteException;

    void removeObsoletePrintJobs() throws RemoteException;

    void setClient(IPrintSpoolerClient iPrintSpoolerClient) throws RemoteException;

    void setPrintJobCancelling(PrintJobId printJobId, boolean z) throws RemoteException;

    void setPrintJobState(PrintJobId printJobId, int i, String str, IPrintSpoolerCallbacks iPrintSpoolerCallbacks, int i2) throws RemoteException;

    void setPrintJobTag(PrintJobId printJobId, String str, IPrintSpoolerCallbacks iPrintSpoolerCallbacks, int i) throws RemoteException;

    void setProgress(PrintJobId printJobId, float f) throws RemoteException;

    void setStatus(PrintJobId printJobId, CharSequence charSequence) throws RemoteException;

    void setStatusRes(PrintJobId printJobId, int i, CharSequence charSequence) throws RemoteException;

    void writePrintJobData(ParcelFileDescriptor parcelFileDescriptor, PrintJobId printJobId) throws RemoteException;
}
