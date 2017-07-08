package android.print;

import android.content.ComponentName;
import android.graphics.drawable.Icon;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.printservice.PrintServiceInfo;
import android.printservice.recommendation.IRecommendationsChangeListener;
import android.printservice.recommendation.RecommendationInfo;
import java.util.List;

public interface IPrintManager extends IInterface {

    public static abstract class Stub extends Binder implements IPrintManager {
        private static final String DESCRIPTOR = "android.print.IPrintManager";
        static final int TRANSACTION_addPrintJobStateChangeListener = 6;
        static final int TRANSACTION_addPrintServiceRecommendationsChangeListener = 12;
        static final int TRANSACTION_addPrintServicesChangeListener = 8;
        static final int TRANSACTION_cancelPrintJob = 4;
        static final int TRANSACTION_createPrinterDiscoverySession = 15;
        static final int TRANSACTION_destroyPrinterDiscoverySession = 22;
        static final int TRANSACTION_getCustomPrinterIcon = 20;
        static final int TRANSACTION_getPrintJobInfo = 2;
        static final int TRANSACTION_getPrintJobInfos = 1;
        static final int TRANSACTION_getPrintServiceRecommendations = 14;
        static final int TRANSACTION_getPrintServices = 10;
        static final int TRANSACTION_print = 3;
        static final int TRANSACTION_removePrintJobStateChangeListener = 7;
        static final int TRANSACTION_removePrintServiceRecommendationsChangeListener = 13;
        static final int TRANSACTION_removePrintServicesChangeListener = 9;
        static final int TRANSACTION_restartPrintJob = 5;
        static final int TRANSACTION_setPrintServiceEnabled = 11;
        static final int TRANSACTION_startPrinterDiscovery = 16;
        static final int TRANSACTION_startPrinterStateTracking = 19;
        static final int TRANSACTION_stopPrinterDiscovery = 17;
        static final int TRANSACTION_stopPrinterStateTracking = 21;
        static final int TRANSACTION_validatePrinters = 18;

        private static class Proxy implements IPrintManager {
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

            public List<PrintJobInfo> getPrintJobInfos(int appId, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(appId);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getPrintJobInfos, _data, _reply, 0);
                    _reply.readException();
                    List<PrintJobInfo> _result = _reply.createTypedArrayList(PrintJobInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PrintJobInfo getPrintJobInfo(PrintJobId printJobId, int appId, int userId) throws RemoteException {
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
                    _data.writeInt(appId);
                    _data.writeInt(userId);
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

            public Bundle print(String printJobName, IPrintDocumentAdapter printAdapter, PrintAttributes attributes, String packageName, int appId, int userId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bundle bundle;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(printJobName);
                    if (printAdapter != null) {
                        iBinder = printAdapter.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (attributes != null) {
                        _data.writeInt(Stub.TRANSACTION_getPrintJobInfos);
                        attributes.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    _data.writeInt(appId);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_print, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        bundle = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return bundle;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void cancelPrintJob(PrintJobId printJobId, int appId, int userId) throws RemoteException {
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
                    _data.writeInt(appId);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_cancelPrintJob, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void restartPrintJob(PrintJobId printJobId, int appId, int userId) throws RemoteException {
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
                    _data.writeInt(appId);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_restartPrintJob, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addPrintJobStateChangeListener(IPrintJobStateChangeListener listener, int appId, int userId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(appId);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_addPrintJobStateChangeListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removePrintJobStateChangeListener(IPrintJobStateChangeListener listener, int userId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_removePrintJobStateChangeListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addPrintServicesChangeListener(IPrintServicesChangeListener listener, int userId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_addPrintServicesChangeListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removePrintServicesChangeListener(IPrintServicesChangeListener listener, int userId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_removePrintServicesChangeListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<PrintServiceInfo> getPrintServices(int selectionFlags, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(selectionFlags);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getPrintServices, _data, _reply, 0);
                    _reply.readException();
                    List<PrintServiceInfo> _result = _reply.createTypedArrayList(PrintServiceInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setPrintServiceEnabled(ComponentName service, boolean isEnabled, int userId) throws RemoteException {
                int i = Stub.TRANSACTION_getPrintJobInfos;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (service != null) {
                        _data.writeInt(Stub.TRANSACTION_getPrintJobInfos);
                        service.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!isEnabled) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_setPrintServiceEnabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addPrintServiceRecommendationsChangeListener(IRecommendationsChangeListener listener, int userId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_addPrintServiceRecommendationsChangeListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removePrintServiceRecommendationsChangeListener(IRecommendationsChangeListener listener, int userId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_removePrintServiceRecommendationsChangeListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<RecommendationInfo> getPrintServiceRecommendations(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getPrintServiceRecommendations, _data, _reply, 0);
                    _reply.readException();
                    List<RecommendationInfo> _result = _reply.createTypedArrayList(RecommendationInfo.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void createPrinterDiscoverySession(IPrinterDiscoveryObserver observer, int userId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_createPrinterDiscoverySession, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startPrinterDiscovery(IPrinterDiscoveryObserver observer, List<PrinterId> priorityList, int userId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeTypedList(priorityList);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_startPrinterDiscovery, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopPrinterDiscovery(IPrinterDiscoveryObserver observer, int userId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_stopPrinterDiscovery, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void validatePrinters(List<PrinterId> printerIds, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(printerIds);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_validatePrinters, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startPrinterStateTracking(PrinterId printerId, int userId) throws RemoteException {
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
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_startPrinterStateTracking, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Icon getCustomPrinterIcon(PrinterId printerId, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Icon icon;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (printerId != null) {
                        _data.writeInt(Stub.TRANSACTION_getPrintJobInfos);
                        printerId.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getCustomPrinterIcon, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        icon = (Icon) Icon.CREATOR.createFromParcel(_reply);
                    } else {
                        icon = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return icon;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopPrinterStateTracking(PrinterId printerId, int userId) throws RemoteException {
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
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_stopPrinterStateTracking, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void destroyPrinterDiscoverySession(IPrinterDiscoveryObserver observer, int userId) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_destroyPrinterDiscoverySession, _data, _reply, 0);
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

        public static IPrintManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPrintManager)) {
                return new Proxy(obj);
            }
            return (IPrintManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PrintJobId printJobId;
            PrinterId printerId;
            switch (code) {
                case TRANSACTION_getPrintJobInfos /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<PrintJobInfo> _result = getPrintJobInfos(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result);
                    return true;
                case TRANSACTION_getPrintJobInfo /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        printJobId = (PrintJobId) PrintJobId.CREATOR.createFromParcel(data);
                    } else {
                        printJobId = null;
                    }
                    PrintJobInfo _result2 = getPrintJobInfo(printJobId, data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_getPrintJobInfos);
                        _result2.writeToParcel(reply, TRANSACTION_getPrintJobInfos);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_print /*3*/:
                    PrintAttributes printAttributes;
                    data.enforceInterface(DESCRIPTOR);
                    String _arg0 = data.readString();
                    IPrintDocumentAdapter _arg1 = android.print.IPrintDocumentAdapter.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        printAttributes = (PrintAttributes) PrintAttributes.CREATOR.createFromParcel(data);
                    } else {
                        printAttributes = null;
                    }
                    Bundle _result3 = print(_arg0, _arg1, printAttributes, data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_getPrintJobInfos);
                        _result3.writeToParcel(reply, TRANSACTION_getPrintJobInfos);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_cancelPrintJob /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        printJobId = (PrintJobId) PrintJobId.CREATOR.createFromParcel(data);
                    } else {
                        printJobId = null;
                    }
                    cancelPrintJob(printJobId, data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_restartPrintJob /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        printJobId = (PrintJobId) PrintJobId.CREATOR.createFromParcel(data);
                    } else {
                        printJobId = null;
                    }
                    restartPrintJob(printJobId, data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addPrintJobStateChangeListener /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    addPrintJobStateChangeListener(android.print.IPrintJobStateChangeListener.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removePrintJobStateChangeListener /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    removePrintJobStateChangeListener(android.print.IPrintJobStateChangeListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addPrintServicesChangeListener /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    addPrintServicesChangeListener(android.print.IPrintServicesChangeListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removePrintServicesChangeListener /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    removePrintServicesChangeListener(android.print.IPrintServicesChangeListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getPrintServices /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<PrintServiceInfo> _result4 = getPrintServices(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result4);
                    return true;
                case TRANSACTION_setPrintServiceEnabled /*11*/:
                    ComponentName componentName;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    } else {
                        componentName = null;
                    }
                    setPrintServiceEnabled(componentName, data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_addPrintServiceRecommendationsChangeListener /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    addPrintServiceRecommendationsChangeListener(android.printservice.recommendation.IRecommendationsChangeListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removePrintServiceRecommendationsChangeListener /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    removePrintServiceRecommendationsChangeListener(android.printservice.recommendation.IRecommendationsChangeListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getPrintServiceRecommendations /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<RecommendationInfo> _result5 = getPrintServiceRecommendations(data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result5);
                    return true;
                case TRANSACTION_createPrinterDiscoverySession /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    createPrinterDiscoverySession(android.print.IPrinterDiscoveryObserver.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_startPrinterDiscovery /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    startPrinterDiscovery(android.print.IPrinterDiscoveryObserver.Stub.asInterface(data.readStrongBinder()), data.createTypedArrayList(PrinterId.CREATOR), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_stopPrinterDiscovery /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    stopPrinterDiscovery(android.print.IPrinterDiscoveryObserver.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_validatePrinters /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    validatePrinters(data.createTypedArrayList(PrinterId.CREATOR), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_startPrinterStateTracking /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        printerId = (PrinterId) PrinterId.CREATOR.createFromParcel(data);
                    } else {
                        printerId = null;
                    }
                    startPrinterStateTracking(printerId, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getCustomPrinterIcon /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        printerId = (PrinterId) PrinterId.CREATOR.createFromParcel(data);
                    } else {
                        printerId = null;
                    }
                    Icon _result6 = getCustomPrinterIcon(printerId, data.readInt());
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(TRANSACTION_getPrintJobInfos);
                        _result6.writeToParcel(reply, TRANSACTION_getPrintJobInfos);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_stopPrinterStateTracking /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        printerId = (PrinterId) PrinterId.CREATOR.createFromParcel(data);
                    } else {
                        printerId = null;
                    }
                    stopPrinterStateTracking(printerId, data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_destroyPrinterDiscoverySession /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    destroyPrinterDiscoverySession(android.print.IPrinterDiscoveryObserver.Stub.asInterface(data.readStrongBinder()), data.readInt());
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

    void addPrintJobStateChangeListener(IPrintJobStateChangeListener iPrintJobStateChangeListener, int i, int i2) throws RemoteException;

    void addPrintServiceRecommendationsChangeListener(IRecommendationsChangeListener iRecommendationsChangeListener, int i) throws RemoteException;

    void addPrintServicesChangeListener(IPrintServicesChangeListener iPrintServicesChangeListener, int i) throws RemoteException;

    void cancelPrintJob(PrintJobId printJobId, int i, int i2) throws RemoteException;

    void createPrinterDiscoverySession(IPrinterDiscoveryObserver iPrinterDiscoveryObserver, int i) throws RemoteException;

    void destroyPrinterDiscoverySession(IPrinterDiscoveryObserver iPrinterDiscoveryObserver, int i) throws RemoteException;

    Icon getCustomPrinterIcon(PrinterId printerId, int i) throws RemoteException;

    PrintJobInfo getPrintJobInfo(PrintJobId printJobId, int i, int i2) throws RemoteException;

    List<PrintJobInfo> getPrintJobInfos(int i, int i2) throws RemoteException;

    List<RecommendationInfo> getPrintServiceRecommendations(int i) throws RemoteException;

    List<PrintServiceInfo> getPrintServices(int i, int i2) throws RemoteException;

    Bundle print(String str, IPrintDocumentAdapter iPrintDocumentAdapter, PrintAttributes printAttributes, String str2, int i, int i2) throws RemoteException;

    void removePrintJobStateChangeListener(IPrintJobStateChangeListener iPrintJobStateChangeListener, int i) throws RemoteException;

    void removePrintServiceRecommendationsChangeListener(IRecommendationsChangeListener iRecommendationsChangeListener, int i) throws RemoteException;

    void removePrintServicesChangeListener(IPrintServicesChangeListener iPrintServicesChangeListener, int i) throws RemoteException;

    void restartPrintJob(PrintJobId printJobId, int i, int i2) throws RemoteException;

    void setPrintServiceEnabled(ComponentName componentName, boolean z, int i) throws RemoteException;

    void startPrinterDiscovery(IPrinterDiscoveryObserver iPrinterDiscoveryObserver, List<PrinterId> list, int i) throws RemoteException;

    void startPrinterStateTracking(PrinterId printerId, int i) throws RemoteException;

    void stopPrinterDiscovery(IPrinterDiscoveryObserver iPrinterDiscoveryObserver, int i) throws RemoteException;

    void stopPrinterStateTracking(PrinterId printerId, int i) throws RemoteException;

    void validatePrinters(List<PrinterId> list, int i) throws RemoteException;
}
