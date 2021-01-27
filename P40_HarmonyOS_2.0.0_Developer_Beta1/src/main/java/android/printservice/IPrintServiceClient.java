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

    public static class Default implements IPrintServiceClient {
        @Override // android.printservice.IPrintServiceClient
        public List<PrintJobInfo> getPrintJobInfos() throws RemoteException {
            return null;
        }

        @Override // android.printservice.IPrintServiceClient
        public PrintJobInfo getPrintJobInfo(PrintJobId printJobId) throws RemoteException {
            return null;
        }

        @Override // android.printservice.IPrintServiceClient
        public boolean setPrintJobState(PrintJobId printJobId, int state, String error) throws RemoteException {
            return false;
        }

        @Override // android.printservice.IPrintServiceClient
        public boolean setPrintJobTag(PrintJobId printJobId, String tag) throws RemoteException {
            return false;
        }

        @Override // android.printservice.IPrintServiceClient
        public void writePrintJobData(ParcelFileDescriptor fd, PrintJobId printJobId) throws RemoteException {
        }

        @Override // android.printservice.IPrintServiceClient
        public void setProgress(PrintJobId printJobId, float progress) throws RemoteException {
        }

        @Override // android.printservice.IPrintServiceClient
        public void setStatus(PrintJobId printJobId, CharSequence status) throws RemoteException {
        }

        @Override // android.printservice.IPrintServiceClient
        public void setStatusRes(PrintJobId printJobId, int status, CharSequence appPackageName) throws RemoteException {
        }

        @Override // android.printservice.IPrintServiceClient
        public void onPrintersAdded(ParceledListSlice printers) throws RemoteException {
        }

        @Override // android.printservice.IPrintServiceClient
        public void onPrintersRemoved(ParceledListSlice printerIds) throws RemoteException {
        }

        @Override // android.printservice.IPrintServiceClient
        public void onCustomPrinterIconLoaded(PrinterId printerId, Icon icon) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getPrintJobInfos";
                case 2:
                    return "getPrintJobInfo";
                case 3:
                    return "setPrintJobState";
                case 4:
                    return "setPrintJobTag";
                case 5:
                    return "writePrintJobData";
                case 6:
                    return "setProgress";
                case 7:
                    return "setStatus";
                case 8:
                    return "setStatusRes";
                case 9:
                    return "onPrintersAdded";
                case 10:
                    return "onPrintersRemoved";
                case 11:
                    return "onCustomPrinterIconLoaded";
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
            PrintJobId _arg0;
            PrintJobId _arg02;
            PrintJobId _arg03;
            ParcelFileDescriptor _arg04;
            PrintJobId _arg1;
            PrintJobId _arg05;
            PrintJobId _arg06;
            CharSequence _arg12;
            PrintJobId _arg07;
            CharSequence _arg2;
            ParceledListSlice _arg08;
            ParceledListSlice _arg09;
            PrinterId _arg010;
            Icon _arg13;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        List<PrintJobInfo> _result = getPrintJobInfos();
                        reply.writeNoException();
                        reply.writeTypedList(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = PrintJobId.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        PrintJobInfo _result2 = getPrintJobInfo(_arg0);
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = PrintJobId.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        boolean printJobState = setPrintJobState(_arg02, data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(printJobState ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = PrintJobId.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        boolean printJobTag = setPrintJobTag(_arg03, data.readString());
                        reply.writeNoException();
                        reply.writeInt(printJobTag ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = ParcelFileDescriptor.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = PrintJobId.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        writePrintJobData(_arg04, _arg1);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = PrintJobId.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        setProgress(_arg05, data.readFloat());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = PrintJobId.CREATOR.createFromParcel(data);
                        } else {
                            _arg06 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg12 = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        setStatus(_arg06, _arg12);
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = PrintJobId.CREATOR.createFromParcel(data);
                        } else {
                            _arg07 = null;
                        }
                        int _arg14 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        setStatusRes(_arg07, _arg14, _arg2);
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = ParceledListSlice.CREATOR.createFromParcel(data);
                        } else {
                            _arg08 = null;
                        }
                        onPrintersAdded(_arg08);
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg09 = ParceledListSlice.CREATOR.createFromParcel(data);
                        } else {
                            _arg09 = null;
                        }
                        onPrintersRemoved(_arg09);
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg010 = PrinterId.CREATOR.createFromParcel(data);
                        } else {
                            _arg010 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg13 = Icon.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        onCustomPrinterIconLoaded(_arg010, _arg13);
                        reply.writeNoException();
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
        public static class Proxy implements IPrintServiceClient {
            public static IPrintServiceClient sDefaultImpl;
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

            @Override // android.printservice.IPrintServiceClient
            public List<PrintJobInfo> getPrintJobInfos() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPrintJobInfos();
                    }
                    _reply.readException();
                    List<PrintJobInfo> _result = _reply.createTypedArrayList(PrintJobInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.printservice.IPrintServiceClient
            public PrintJobInfo getPrintJobInfo(PrintJobId printJobId) throws RemoteException {
                PrintJobInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJobId != null) {
                        _data.writeInt(1);
                        printJobId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPrintJobInfo(printJobId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = PrintJobInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.printservice.IPrintServiceClient
            public boolean setPrintJobState(PrintJobId printJobId, int state, String error) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (printJobId != null) {
                        _data.writeInt(1);
                        printJobId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(state);
                    _data.writeString(error);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setPrintJobState(printJobId, state, error);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.printservice.IPrintServiceClient
            public boolean setPrintJobTag(PrintJobId printJobId, String tag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (printJobId != null) {
                        _data.writeInt(1);
                        printJobId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(tag);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setPrintJobTag(printJobId, tag);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.printservice.IPrintServiceClient
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
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().writePrintJobData(fd, printJobId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.printservice.IPrintServiceClient
            public void setProgress(PrintJobId printJobId, float progress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printJobId != null) {
                        _data.writeInt(1);
                        printJobId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeFloat(progress);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setProgress(printJobId, progress);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.printservice.IPrintServiceClient
            public void setStatus(PrintJobId printJobId, CharSequence status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
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
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setStatus(printJobId, status);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.printservice.IPrintServiceClient
            public void setStatusRes(PrintJobId printJobId, int status, CharSequence appPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
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
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setStatusRes(printJobId, status, appPackageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.printservice.IPrintServiceClient
            public void onPrintersAdded(ParceledListSlice printers) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printers != null) {
                        _data.writeInt(1);
                        printers.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onPrintersAdded(printers);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.printservice.IPrintServiceClient
            public void onPrintersRemoved(ParceledListSlice printerIds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printerIds != null) {
                        _data.writeInt(1);
                        printerIds.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onPrintersRemoved(printerIds);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.printservice.IPrintServiceClient
            public void onCustomPrinterIconLoaded(PrinterId printerId, Icon icon) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
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
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onCustomPrinterIconLoaded(printerId, icon);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IPrintServiceClient impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IPrintServiceClient getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
