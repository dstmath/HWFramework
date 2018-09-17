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
import android.printservice.IPrintService.Stub;
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
    private IPrintServiceClient mClient;
    private PrinterDiscoverySession mDiscoverySession;
    private Handler mHandler;
    private int mLastSessionId = -1;

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
                    } else if (session.getId() == PrintService.this.mLastSessionId) {
                        throw new IllegalStateException("cannot reuse session instances");
                    } else {
                        PrintService.this.mDiscoverySession = session;
                        PrintService.this.mLastSessionId = session.getId();
                        session.setObserver(PrintService.this.mClient);
                        return;
                    }
                case 2:
                    if (PrintService.this.mDiscoverySession != null) {
                        PrintService.this.mDiscoverySession.destroy();
                        PrintService.this.mDiscoverySession = null;
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
                        PrintService.this.mDiscoverySession.validatePrinters(message.obj);
                        return;
                    }
                    return;
                case 6:
                    if (PrintService.this.mDiscoverySession != null) {
                        PrintService.this.mDiscoverySession.startPrinterStateTracking(message.obj);
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
                    PrintService.this.onRequestCancelPrintJob(new PrintJob(PrintService.this, message.obj, PrintService.this.mClient));
                    return;
                case 11:
                    PrintService.this.mClient = (IPrintServiceClient) message.obj;
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

    protected abstract PrinterDiscoverySession onCreatePrinterDiscoverySession();

    protected abstract void onPrintJobQueued(PrintJob printJob);

    protected abstract void onRequestCancelPrintJob(PrintJob printJob);

    protected final void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        this.mHandler = new ServiceHandler(base.getMainLooper());
    }

    protected void onConnected() {
    }

    protected void onDisconnected() {
    }

    public final List<PrintJob> getActivePrintJobs() {
        RemoteException re;
        throwIfNotCalledOnMainThread();
        if (this.mClient == null) {
            return Collections.emptyList();
        }
        List<PrintJob> list = null;
        try {
            List<PrintJobInfo> printJobInfos = this.mClient.getPrintJobInfos();
            if (printJobInfos != null) {
                int printJobInfoCount = printJobInfos.size();
                List<PrintJob> printJobs = new ArrayList(printJobInfoCount);
                int i = 0;
                while (i < printJobInfoCount) {
                    try {
                        printJobs.add(new PrintJob(this, (PrintJobInfo) printJobInfos.get(i), this.mClient));
                        i++;
                    } catch (RemoteException e) {
                        re = e;
                    }
                }
                list = printJobs;
            }
            if (list != null) {
                return list;
            }
        } catch (RemoteException e2) {
            re = e2;
            Log.e(LOG_TAG, "Error calling getPrintJobs()", re);
            return Collections.emptyList();
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
        return new Stub() {
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
