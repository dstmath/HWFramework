package android.print;

import android.content.ComponentName;
import android.graphics.drawable.Icon;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
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
                    this.mRemote.transact(Stub.TRANSACTION_removeObsoletePrintJobs, _data, null, Stub.TRANSACTION_removeObsoletePrintJobs);
                } finally {
                    _data.recycle();
                }
            }

            public void getPrintJobInfos(IPrintSpoolerCallbacks callback, ComponentName componentName, int state, int appId, int sequence) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (componentName != null) {
                        _data.writeInt(Stub.TRANSACTION_removeObsoletePrintJobs);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(state);
                    _data.writeInt(appId);
                    _data.writeInt(sequence);
                    this.mRemote.transact(Stub.TRANSACTION_getPrintJobInfos, _data, null, Stub.TRANSACTION_removeObsoletePrintJobs);
                } finally {
                    _data.recycle();
                }
            }

            public void getPrintJobInfo(PrintJobId printJobId, IPrintSpoolerCallbacks callback, int appId, int sequence) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJobId != null) {
                        _data.writeInt(Stub.TRANSACTION_removeObsoletePrintJobs);
                        printJobId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(appId);
                    _data.writeInt(sequence);
                    this.mRemote.transact(Stub.TRANSACTION_getPrintJobInfo, _data, null, Stub.TRANSACTION_removeObsoletePrintJobs);
                } finally {
                    _data.recycle();
                }
            }

            public void createPrintJob(PrintJobInfo printJob) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJob != null) {
                        _data.writeInt(Stub.TRANSACTION_removeObsoletePrintJobs);
                        printJob.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_createPrintJob, _data, null, Stub.TRANSACTION_removeObsoletePrintJobs);
                } finally {
                    _data.recycle();
                }
            }

            public void setPrintJobState(PrintJobId printJobId, int status, String stateReason, IPrintSpoolerCallbacks callback, int sequence) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJobId != null) {
                        _data.writeInt(Stub.TRANSACTION_removeObsoletePrintJobs);
                        printJobId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(status);
                    _data.writeString(stateReason);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(sequence);
                    this.mRemote.transact(Stub.TRANSACTION_setPrintJobState, _data, null, Stub.TRANSACTION_removeObsoletePrintJobs);
                } finally {
                    _data.recycle();
                }
            }

            public void setProgress(PrintJobId printJobId, float progress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJobId != null) {
                        _data.writeInt(Stub.TRANSACTION_removeObsoletePrintJobs);
                        printJobId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeFloat(progress);
                    this.mRemote.transact(Stub.TRANSACTION_setProgress, _data, null, Stub.TRANSACTION_removeObsoletePrintJobs);
                } finally {
                    _data.recycle();
                }
            }

            public void setStatus(PrintJobId printJobId, CharSequence status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJobId != null) {
                        _data.writeInt(Stub.TRANSACTION_removeObsoletePrintJobs);
                        printJobId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (status != null) {
                        _data.writeInt(Stub.TRANSACTION_removeObsoletePrintJobs);
                        TextUtils.writeToParcel(status, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setStatus, _data, null, Stub.TRANSACTION_removeObsoletePrintJobs);
                } finally {
                    _data.recycle();
                }
            }

            public void setStatusRes(PrintJobId printJobId, int status, CharSequence appPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJobId != null) {
                        _data.writeInt(Stub.TRANSACTION_removeObsoletePrintJobs);
                        printJobId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(status);
                    if (appPackageName != null) {
                        _data.writeInt(Stub.TRANSACTION_removeObsoletePrintJobs);
                        TextUtils.writeToParcel(appPackageName, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setStatusRes, _data, null, Stub.TRANSACTION_removeObsoletePrintJobs);
                } finally {
                    _data.recycle();
                }
            }

            public void onCustomPrinterIconLoaded(PrinterId printerId, Icon icon, IPrintSpoolerCallbacks callbacks, int sequence) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printerId != null) {
                        _data.writeInt(Stub.TRANSACTION_removeObsoletePrintJobs);
                        printerId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (icon != null) {
                        _data.writeInt(Stub.TRANSACTION_removeObsoletePrintJobs);
                        icon.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (callbacks != null) {
                        iBinder = callbacks.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(sequence);
                    this.mRemote.transact(Stub.TRANSACTION_onCustomPrinterIconLoaded, _data, null, Stub.TRANSACTION_removeObsoletePrintJobs);
                } finally {
                    _data.recycle();
                }
            }

            public void getCustomPrinterIcon(PrinterId printerId, IPrintSpoolerCallbacks callbacks, int sequence) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printerId != null) {
                        _data.writeInt(Stub.TRANSACTION_removeObsoletePrintJobs);
                        printerId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (callbacks != null) {
                        iBinder = callbacks.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(sequence);
                    this.mRemote.transact(Stub.TRANSACTION_getCustomPrinterIcon, _data, null, Stub.TRANSACTION_removeObsoletePrintJobs);
                } finally {
                    _data.recycle();
                }
            }

            public void clearCustomPrinterIconCache(IPrintSpoolerCallbacks callbacks, int sequence) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callbacks != null) {
                        iBinder = callbacks.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(sequence);
                    this.mRemote.transact(Stub.TRANSACTION_clearCustomPrinterIconCache, _data, null, Stub.TRANSACTION_removeObsoletePrintJobs);
                } finally {
                    _data.recycle();
                }
            }

            public void setPrintJobTag(PrintJobId printJobId, String tag, IPrintSpoolerCallbacks callback, int sequence) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJobId != null) {
                        _data.writeInt(Stub.TRANSACTION_removeObsoletePrintJobs);
                        printJobId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(tag);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(sequence);
                    this.mRemote.transact(Stub.TRANSACTION_setPrintJobTag, _data, null, Stub.TRANSACTION_removeObsoletePrintJobs);
                } finally {
                    _data.recycle();
                }
            }

            public void writePrintJobData(ParcelFileDescriptor fd, PrintJobId printJobId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (fd != null) {
                        _data.writeInt(Stub.TRANSACTION_removeObsoletePrintJobs);
                        fd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (printJobId != null) {
                        _data.writeInt(Stub.TRANSACTION_removeObsoletePrintJobs);
                        printJobId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_writePrintJobData, _data, null, Stub.TRANSACTION_removeObsoletePrintJobs);
                } finally {
                    _data.recycle();
                }
            }

            public void setClient(IPrintSpoolerClient client) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_setClient, _data, null, Stub.TRANSACTION_removeObsoletePrintJobs);
                } finally {
                    _data.recycle();
                }
            }

            public void setPrintJobCancelling(PrintJobId printJobId, boolean cancelling) throws RemoteException {
                int i = Stub.TRANSACTION_removeObsoletePrintJobs;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJobId != null) {
                        _data.writeInt(Stub.TRANSACTION_removeObsoletePrintJobs);
                        printJobId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!cancelling) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setPrintJobCancelling, _data, null, Stub.TRANSACTION_removeObsoletePrintJobs);
                } finally {
                    _data.recycle();
                }
            }

            public void pruneApprovedPrintServices(List<ComponentName> servicesToKeep) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(servicesToKeep);
                    this.mRemote.transact(Stub.TRANSACTION_pruneApprovedPrintServices, _data, null, Stub.TRANSACTION_removeObsoletePrintJobs);
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PrintJobId printJobId;
            PrinterId printerId;
            switch (code) {
                case TRANSACTION_removeObsoletePrintJobs /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeObsoletePrintJobs();
                    return true;
                case TRANSACTION_getPrintJobInfos /*2*/:
                    ComponentName componentName;
                    data.enforceInterface(DESCRIPTOR);
                    IPrintSpoolerCallbacks _arg0 = android.print.IPrintSpoolerCallbacks.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    getPrintJobInfos(_arg0, componentName, data.readInt(), data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_getPrintJobInfo /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        printJobId = (PrintJobId) PrintJobId.CREATOR.createFromParcel(data);
                    } else {
                        printJobId = null;
                    }
                    getPrintJobInfo(printJobId, android.print.IPrintSpoolerCallbacks.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_createPrintJob /*4*/:
                    PrintJobInfo printJobInfo;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        printJobInfo = (PrintJobInfo) PrintJobInfo.CREATOR.createFromParcel(data);
                    } else {
                        printJobInfo = null;
                    }
                    createPrintJob(printJobInfo);
                    return true;
                case TRANSACTION_setPrintJobState /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        printJobId = (PrintJobId) PrintJobId.CREATOR.createFromParcel(data);
                    } else {
                        printJobId = null;
                    }
                    setPrintJobState(printJobId, data.readInt(), data.readString(), android.print.IPrintSpoolerCallbacks.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    return true;
                case TRANSACTION_setProgress /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        printJobId = (PrintJobId) PrintJobId.CREATOR.createFromParcel(data);
                    } else {
                        printJobId = null;
                    }
                    setProgress(printJobId, data.readFloat());
                    return true;
                case TRANSACTION_setStatus /*7*/:
                    CharSequence charSequence;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        printJobId = (PrintJobId) PrintJobId.CREATOR.createFromParcel(data);
                    } else {
                        printJobId = null;
                    }
                    if (data.readInt() != 0) {
                        charSequence = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data);
                    } else {
                        charSequence = null;
                    }
                    setStatus(printJobId, charSequence);
                    return true;
                case TRANSACTION_setStatusRes /*8*/:
                    CharSequence charSequence2;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        printJobId = (PrintJobId) PrintJobId.CREATOR.createFromParcel(data);
                    } else {
                        printJobId = null;
                    }
                    int _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        charSequence2 = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data);
                    } else {
                        charSequence2 = null;
                    }
                    setStatusRes(printJobId, _arg1, charSequence2);
                    return true;
                case TRANSACTION_onCustomPrinterIconLoaded /*9*/:
                    Icon icon;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        printerId = (PrinterId) PrinterId.CREATOR.createFromParcel(data);
                    } else {
                        printerId = null;
                    }
                    if (data.readInt() != 0) {
                        icon = (Icon) Icon.CREATOR.createFromParcel(data);
                    } else {
                        icon = null;
                    }
                    onCustomPrinterIconLoaded(printerId, icon, android.print.IPrintSpoolerCallbacks.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    return true;
                case TRANSACTION_getCustomPrinterIcon /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        printerId = (PrinterId) PrinterId.CREATOR.createFromParcel(data);
                    } else {
                        printerId = null;
                    }
                    getCustomPrinterIcon(printerId, android.print.IPrintSpoolerCallbacks.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    return true;
                case TRANSACTION_clearCustomPrinterIconCache /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearCustomPrinterIconCache(android.print.IPrintSpoolerCallbacks.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    return true;
                case TRANSACTION_setPrintJobTag /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        printJobId = (PrintJobId) PrintJobId.CREATOR.createFromParcel(data);
                    } else {
                        printJobId = null;
                    }
                    setPrintJobTag(printJobId, data.readString(), android.print.IPrintSpoolerCallbacks.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    return true;
                case TRANSACTION_writePrintJobData /*13*/:
                    ParcelFileDescriptor parcelFileDescriptor;
                    PrintJobId printJobId2;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        parcelFileDescriptor = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(data);
                    } else {
                        parcelFileDescriptor = null;
                    }
                    if (data.readInt() != 0) {
                        printJobId2 = (PrintJobId) PrintJobId.CREATOR.createFromParcel(data);
                    } else {
                        printJobId2 = null;
                    }
                    writePrintJobData(parcelFileDescriptor, printJobId2);
                    return true;
                case TRANSACTION_setClient /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    setClient(android.print.IPrintSpoolerClient.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_setPrintJobCancelling /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        printJobId = (PrintJobId) PrintJobId.CREATOR.createFromParcel(data);
                    } else {
                        printJobId = null;
                    }
                    setPrintJobCancelling(printJobId, data.readInt() != 0);
                    return true;
                case TRANSACTION_pruneApprovedPrintServices /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    pruneApprovedPrintServices(data.createTypedArrayList(ComponentName.CREATOR));
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
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
