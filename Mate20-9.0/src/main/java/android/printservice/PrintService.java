package android.printservice;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.print.PrintJobInfo;
import android.print.PrinterId;
import android.printservice.IPrintService;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class PrintService extends Service {
    private static final boolean DEBUG = false;
    public static final String EXTRA_CAN_SELECT_PRINTER = "android.printservice.extra.CAN_SELECT_PRINTER";
    public static final String EXTRA_PRINTER_INFO = "android.intent.extra.print.EXTRA_PRINTER_INFO";
    public static final String EXTRA_PRINT_DOCUMENT_INFO = "android.printservice.extra.PRINT_DOCUMENT_INFO";
    public static final String EXTRA_PRINT_JOB_INFO = "android.intent.extra.print.PRINT_JOB_INFO";
    public static final String EXTRA_SELECT_PRINTER = "android.printservice.extra.SELECT_PRINTER";
    private static final String LOG_TAG = "PrintService";
    public static final String SERVICE_INTERFACE = "android.printservice.PrintService";
    public static final String SERVICE_META_DATA = "android.printservice";
    /* access modifiers changed from: private */
    public IPrintServiceClient mClient;
    /* access modifiers changed from: private */
    public PrinterDiscoverySession mDiscoverySession;
    /* access modifiers changed from: private */
    public Handler mHandler;
    /* access modifiers changed from: private */
    public int mLastSessionId = -1;

    private final class ServiceHandler extends Handler {
        public static final int MSG_CREATE_PRINTER_DISCOVERY_SESSION = 1;
        public static final int MSG_DESTROY_PRINTER_DISCOVERY_SESSION = 2;
        public static final int MSG_ON_PRINTJOB_QUEUED = 9;
        public static final int MSG_ON_REQUEST_CANCEL_PRINTJOB = 10;
        public static final int MSG_REQUEST_CUSTOM_PRINTER_ICON = 7;
        public static final int MSG_SET_CLIENT = 11;
        public static final int MSG_START_PRINTER_DISCOVERY = 3;
        public static final int MSG_START_PRINTER_STATE_TRACKING = 6;
        public static final int MSG_STOP_PRINTER_DISCOVERY = 4;
        public static final int MSG_STOP_PRINTER_STATE_TRACKING = 8;
        public static final int MSG_VALIDATE_PRINTERS = 5;

        public ServiceHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message message) {
            int action = message.what;
            switch (action) {
                case 1:
                    PrinterDiscoverySession session = PrintService.this.onCreatePrinterDiscoverySession();
                    if (session == null) {
                        throw new NullPointerException("session cannot be null");
                    } else if (session.getId() != PrintService.this.mLastSessionId) {
                        PrinterDiscoverySession unused = PrintService.this.mDiscoverySession = session;
                        int unused2 = PrintService.this.mLastSessionId = session.getId();
                        session.setObserver(PrintService.this.mClient);
                        return;
                    } else {
                        throw new IllegalStateException("cannot reuse session instances");
                    }
                case 2:
                    if (PrintService.this.mDiscoverySession != null) {
                        PrintService.this.mDiscoverySession.destroy();
                        PrinterDiscoverySession unused3 = PrintService.this.mDiscoverySession = null;
                        return;
                    }
                    return;
                case 3:
                    if (PrintService.this.mDiscoverySession != null) {
                        PrintService.this.mDiscoverySession.startPrinterDiscovery((ArrayList) message.obj);
                        return;
                    }
                    return;
                case 4:
                    if (PrintService.this.mDiscoverySession != null) {
                        PrintService.this.mDiscoverySession.stopPrinterDiscovery();
                        return;
                    }
                    return;
                case 5:
                    if (PrintService.this.mDiscoverySession != null) {
                        PrintService.this.mDiscoverySession.validatePrinters((List) message.obj);
                        return;
                    }
                    return;
                case 6:
                    if (PrintService.this.mDiscoverySession != null) {
                        PrintService.this.mDiscoverySession.startPrinterStateTracking((PrinterId) message.obj);
                        return;
                    }
                    return;
                case 7:
                    if (PrintService.this.mDiscoverySession != null) {
                        PrintService.this.mDiscoverySession.requestCustomPrinterIcon((PrinterId) message.obj);
                        return;
                    }
                    return;
                case 8:
                    if (PrintService.this.mDiscoverySession != null) {
                        PrintService.this.mDiscoverySession.stopPrinterStateTracking((PrinterId) message.obj);
                        return;
                    }
                    return;
                case 9:
                    PrintService.this.onPrintJobQueued(new PrintJob(PrintService.this, (PrintJobInfo) message.obj, PrintService.this.mClient));
                    return;
                case 10:
                    PrintService.this.onRequestCancelPrintJob(new PrintJob(PrintService.this, (PrintJobInfo) message.obj, PrintService.this.mClient));
                    return;
                case 11:
                    IPrintServiceClient unused4 = PrintService.this.mClient = (IPrintServiceClient) message.obj;
                    if (PrintService.this.mClient != null) {
                        PrintService.this.onConnected();
                        return;
                    } else {
                        PrintService.this.onDisconnected();
                        return;
                    }
                default:
                    throw new IllegalArgumentException("Unknown message: " + action);
            }
        }
    }

    /* access modifiers changed from: protected */
    public abstract PrinterDiscoverySession onCreatePrinterDiscoverySession();

    /* access modifiers changed from: protected */
    public abstract void onPrintJobQueued(PrintJob printJob);

    /* access modifiers changed from: protected */
    public abstract void onRequestCancelPrintJob(PrintJob printJob);

    /* access modifiers changed from: protected */
    public final void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        this.mHandler = new ServiceHandler(base.getMainLooper());
    }

    /* access modifiers changed from: protected */
    public void onConnected() {
    }

    /* access modifiers changed from: protected */
    public void onDisconnected() {
    }

    public final List<PrintJob> getActivePrintJobs() {
        throwIfNotCalledOnMainThread();
        if (this.mClient == null) {
            return Collections.emptyList();
        }
        List<PrintJob> printJobs = null;
        try {
            List<PrintJobInfo> printJobInfos = this.mClient.getPrintJobInfos();
            if (printJobInfos != null) {
                int printJobInfoCount = printJobInfos.size();
                printJobs = new ArrayList<>(printJobInfoCount);
                for (int i = 0; i < printJobInfoCount; i++) {
                    printJobs.add(new PrintJob(this, printJobInfos.get(i), this.mClient));
                }
            }
            if (printJobs != null) {
                return printJobs;
            }
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error calling getPrintJobs()", re);
        }
        return Collections.emptyList();
    }

    public final PrinterId generatePrinterId(String localId) {
        throwIfNotCalledOnMainThread();
        return new PrinterId(new ComponentName(getPackageName(), getClass().getName()), (String) Preconditions.checkNotNull(localId, "localId cannot be null"));
    }

    static void throwIfNotCalledOnMainThread() {
        if (!Looper.getMainLooper().isCurrentThread()) {
            throw new IllegalAccessError("must be called from the main thread");
        }
    }

    public final IBinder onBind(Intent intent) {
        return new IPrintService.Stub() {
            public void createPrinterDiscoverySession() {
                PrintService.this.mHandler.sendEmptyMessage(1);
            }

            public void destroyPrinterDiscoverySession() {
                PrintService.this.mHandler.sendEmptyMessage(2);
            }

            public void startPrinterDiscovery(List<PrinterId> priorityList) {
                PrintService.this.mHandler.obtainMessage(3, priorityList).sendToTarget();
            }

            public void stopPrinterDiscovery() {
                PrintService.this.mHandler.sendEmptyMessage(4);
            }

            public void validatePrinters(List<PrinterId> printerIds) {
                PrintService.this.mHandler.obtainMessage(5, printerIds).sendToTarget();
            }

            public void startPrinterStateTracking(PrinterId printerId) {
                PrintService.this.mHandler.obtainMessage(6, printerId).sendToTarget();
            }

            public void requestCustomPrinterIcon(PrinterId printerId) {
                PrintService.this.mHandler.obtainMessage(7, printerId).sendToTarget();
            }

            public void stopPrinterStateTracking(PrinterId printerId) {
                PrintService.this.mHandler.obtainMessage(8, printerId).sendToTarget();
            }

            public void setClient(IPrintServiceClient client) {
                PrintService.this.mHandler.obtainMessage(11, client).sendToTarget();
            }

            public void requestCancelPrintJob(PrintJobInfo printJobInfo) {
                PrintService.this.mHandler.obtainMessage(10, printJobInfo).sendToTarget();
            }

            public void onPrintJobQueued(PrintJobInfo printJobInfo) {
                PrintService.this.mHandler.obtainMessage(9, printJobInfo).sendToTarget();
            }
        };
    }
}
