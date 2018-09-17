package android.printservice;

import android.content.pm.ParceledListSlice;
import android.graphics.drawable.Icon;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.print.PrintJobId;
import android.print.PrintJobInfo;
import android.print.PrinterId;
import android.text.TextUtils;
import java.util.List;

public interface IPrintServiceClient extends IInterface {

    public static abstract class Stub extends Binder implements IPrintServiceClient {
        private static final String DESCRIPTOR = "android.printservice.IPrintServiceClient";
        static final int TRANSACTION_getPrintJobInfo = 2;
        static final int TRANSACTION_getPrintJobInfos = 1;
        static final int TRANSACTION_onCustomPrinterIconLoaded = 11;
        static final int TRANSACTION_onPrintersAdded = 9;
        static final int TRANSACTION_onPrintersRemoved = 10;
        static final int TRANSACTION_setPrintJobState = 3;
        static final int TRANSACTION_setPrintJobTag = 4;
        static final int TRANSACTION_setProgress = 6;
        static final int TRANSACTION_setStatus = 7;
        static final int TRANSACTION_setStatusRes = 8;
        static final int TRANSACTION_writePrintJobData = 5;

        private static class Proxy implements IPrintServiceClient {
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

            public List<PrintJobInfo> getPrintJobInfos() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getPrintJobInfos, _data, _reply, 0);
                    _reply.readException();
                    List<PrintJobInfo> _result = _reply.createTypedArrayList(PrintJobInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PrintJobInfo getPrintJobInfo(PrintJobId printJobId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    PrintJobInfo printJobInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJobId != null) {
                        _data.writeInt(Stub.TRANSACTION_getPrintJobInfos);
                        printJobId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_getPrintJobInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        printJobInfo = (PrintJobInfo) PrintJobInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        printJobInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return printJobInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setPrintJobState(PrintJobId printJobId, int state, String error) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJobId != null) {
                        _data.writeInt(Stub.TRANSACTION_getPrintJobInfos);
                        printJobId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(state);
                    _data.writeString(error);
                    this.mRemote.transact(Stub.TRANSACTION_setPrintJobState, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setPrintJobTag(PrintJobId printJobId, String tag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJobId != null) {
                        _data.writeInt(Stub.TRANSACTION_getPrintJobInfos);
                        printJobId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(tag);
                    this.mRemote.transact(Stub.TRANSACTION_setPrintJobTag, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void writePrintJobData(ParcelFileDescriptor fd, PrintJobId printJobId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (fd != null) {
                        _data.writeInt(Stub.TRANSACTION_getPrintJobInfos);
                        fd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (printJobId != null) {
                        _data.writeInt(Stub.TRANSACTION_getPrintJobInfos);
                        printJobId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_writePrintJobData, _data, null, Stub.TRANSACTION_getPrintJobInfos);
                } finally {
                    _data.recycle();
                }
            }

            public void setProgress(PrintJobId printJobId, float progress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJobId != null) {
                        _data.writeInt(Stub.TRANSACTION_getPrintJobInfos);
                        printJobId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeFloat(progress);
                    this.mRemote.transact(Stub.TRANSACTION_setProgress, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setStatus(PrintJobId printJobId, CharSequence status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJobId != null) {
                        _data.writeInt(Stub.TRANSACTION_getPrintJobInfos);
                        printJobId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (status != null) {
                        _data.writeInt(Stub.TRANSACTION_getPrintJobInfos);
                        TextUtils.writeToParcel(status, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setStatus, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setStatusRes(PrintJobId printJobId, int status, CharSequence appPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJobId != null) {
                        _data.writeInt(Stub.TRANSACTION_getPrintJobInfos);
                        printJobId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(status);
                    if (appPackageName != null) {
                        _data.writeInt(Stub.TRANSACTION_getPrintJobInfos);
                        TextUtils.writeToParcel(appPackageName, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setStatusRes, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onPrintersAdded(ParceledListSlice printers) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printers != null) {
                        _data.writeInt(Stub.TRANSACTION_getPrintJobInfos);
                        printers.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onPrintersAdded, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onPrintersRemoved(ParceledListSlice printerIds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printerIds != null) {
                        _data.writeInt(Stub.TRANSACTION_getPrintJobInfos);
                        printerIds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onPrintersRemoved, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onCustomPrinterIconLoaded(PrinterId printerId, Icon icon) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printerId != null) {
                        _data.writeInt(Stub.TRANSACTION_getPrintJobInfos);
                        printerId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (icon != null) {
                        _data.writeInt(Stub.TRANSACTION_getPrintJobInfos);
                        icon.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onCustomPrinterIconLoaded, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPrintServiceClient asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPrintServiceClient)) {
                return new Proxy(obj);
            }
            return (IPrintServiceClient) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PrintJobId printJobId;
            boolean _result;
            ParceledListSlice parceledListSlice;
            switch (code) {
                case TRANSACTION_getPrintJobInfos /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<PrintJobInfo> _result2 = getPrintJobInfos();
                    reply.writeNoException();
                    reply.writeTypedList(_result2);
                    return true;
                case TRANSACTION_getPrintJobInfo /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        printJobId = (PrintJobId) PrintJobId.CREATOR.createFromParcel(data);
                    } else {
                        printJobId = null;
                    }
                    PrintJobInfo _result3 = getPrintJobInfo(printJobId);
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_getPrintJobInfos);
                        _result3.writeToParcel(reply, TRANSACTION_getPrintJobInfos);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setPrintJobState /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        printJobId = (PrintJobId) PrintJobId.CREATOR.createFromParcel(data);
                    } else {
                        printJobId = null;
                    }
                    _result = setPrintJobState(printJobId, data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_getPrintJobInfos : 0);
                    return true;
                case TRANSACTION_setPrintJobTag /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        printJobId = (PrintJobId) PrintJobId.CREATOR.createFromParcel(data);
                    } else {
                        printJobId = null;
                    }
                    _result = setPrintJobTag(printJobId, data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_getPrintJobInfos : 0);
                    return true;
                case TRANSACTION_writePrintJobData /*5*/:
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
                case TRANSACTION_setProgress /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        printJobId = (PrintJobId) PrintJobId.CREATOR.createFromParcel(data);
                    } else {
                        printJobId = null;
                    }
                    setProgress(printJobId, data.readFloat());
                    reply.writeNoException();
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
                    reply.writeNoException();
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
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onPrintersAdded /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(data);
                    } else {
                        parceledListSlice = null;
                    }
                    onPrintersAdded(parceledListSlice);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onPrintersRemoved /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(data);
                    } else {
                        parceledListSlice = null;
                    }
                    onPrintersRemoved(parceledListSlice);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onCustomPrinterIconLoaded /*11*/:
                    PrinterId printerId;
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
                    onCustomPrinterIconLoaded(printerId, icon);
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    PrintJobInfo getPrintJobInfo(PrintJobId printJobId) throws RemoteException;

    List<PrintJobInfo> getPrintJobInfos() throws RemoteException;

    void onCustomPrinterIconLoaded(PrinterId printerId, Icon icon) throws RemoteException;

    void onPrintersAdded(ParceledListSlice parceledListSlice) throws RemoteException;

    void onPrintersRemoved(ParceledListSlice parceledListSlice) throws RemoteException;

    boolean setPrintJobState(PrintJobId printJobId, int i, String str) throws RemoteException;

    boolean setPrintJobTag(PrintJobId printJobId, String str) throws RemoteException;

    void setProgress(PrintJobId printJobId, float f) throws RemoteException;

    void setStatus(PrintJobId printJobId, CharSequence charSequence) throws RemoteException;

    void setStatusRes(PrintJobId printJobId, int i, CharSequence charSequence) throws RemoteException;

    void writePrintJobData(ParcelFileDescriptor parcelFileDescriptor, PrintJobId printJobId) throws RemoteException;
}
