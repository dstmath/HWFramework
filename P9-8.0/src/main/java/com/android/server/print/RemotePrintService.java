package com.android.server.print;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ParceledListSlice;
import android.graphics.drawable.Icon;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.UserHandle;
import android.print.PrintJobId;
import android.print.PrintJobInfo;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.printservice.IPrintService;
import android.printservice.IPrintServiceClient.Stub;
import android.util.Slog;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

final class RemotePrintService implements DeathRecipient {
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "RemotePrintService";
    private boolean mBinding;
    private final PrintServiceCallbacks mCallbacks;
    private final ComponentName mComponentName;
    private final Context mContext;
    private boolean mDestroyed;
    private List<PrinterId> mDiscoveryPriorityList;
    private final Handler mHandler;
    private boolean mHasActivePrintJobs;
    private boolean mHasPrinterDiscoverySession;
    private final Intent mIntent;
    private final List<Runnable> mPendingCommands = new ArrayList();
    private IPrintService mPrintService;
    private final RemotePrintServiceClient mPrintServiceClient;
    private final ServiceConnection mServiceConnection = new RemoteServiceConneciton(this, null);
    private boolean mServiceDied;
    private final RemotePrintSpooler mSpooler;
    private List<PrinterId> mTrackedPrinterList;
    private final int mUserId;

    private final class MyHandler extends Handler {
        public static final int MSG_BINDER_DIED = 12;
        public static final int MSG_CREATE_PRINTER_DISCOVERY_SESSION = 1;
        public static final int MSG_DESTROY = 11;
        public static final int MSG_DESTROY_PRINTER_DISCOVERY_SESSION = 2;
        public static final int MSG_ON_ALL_PRINT_JOBS_HANDLED = 8;
        public static final int MSG_ON_PRINT_JOB_QUEUED = 10;
        public static final int MSG_ON_REQUEST_CANCEL_PRINT_JOB = 9;
        public static final int MSG_REQUEST_CUSTOM_PRINTER_ICON = 13;
        public static final int MSG_START_PRINTER_DISCOVERY = 3;
        public static final int MSG_START_PRINTER_STATE_TRACKING = 6;
        public static final int MSG_STOP_PRINTER_DISCOVERY = 4;
        public static final int MSG_STOP_PRINTER_STATE_TRACKING = 7;
        public static final int MSG_VALIDATE_PRINTERS = 5;

        public MyHandler(Looper looper) {
            super(looper, null, false);
        }

        public void handleMessage(Message message) {
            if (RemotePrintService.this.mDestroyed) {
                Slog.w(RemotePrintService.LOG_TAG, "Not handling " + message + " as service for " + RemotePrintService.this.mComponentName + " is already destroyed");
                return;
            }
            switch (message.what) {
                case 1:
                    RemotePrintService.this.handleCreatePrinterDiscoverySession();
                    break;
                case 2:
                    RemotePrintService.this.handleDestroyPrinterDiscoverySession();
                    break;
                case 3:
                    RemotePrintService.this.handleStartPrinterDiscovery((ArrayList) message.obj);
                    break;
                case 4:
                    RemotePrintService.this.handleStopPrinterDiscovery();
                    break;
                case 5:
                    RemotePrintService.this.handleValidatePrinters(message.obj);
                    break;
                case 6:
                    RemotePrintService.this.handleStartPrinterStateTracking(message.obj);
                    break;
                case 7:
                    RemotePrintService.this.handleStopPrinterStateTracking((PrinterId) message.obj);
                    break;
                case 8:
                    RemotePrintService.this.handleOnAllPrintJobsHandled();
                    break;
                case 9:
                    RemotePrintService.this.handleRequestCancelPrintJob(message.obj);
                    break;
                case 10:
                    RemotePrintService.this.handleOnPrintJobQueued((PrintJobInfo) message.obj);
                    break;
                case 11:
                    RemotePrintService.this.handleDestroy();
                    break;
                case 12:
                    RemotePrintService.this.handleBinderDied();
                    break;
                case 13:
                    RemotePrintService.this.handleRequestCustomPrinterIcon((PrinterId) message.obj);
                    break;
            }
        }
    }

    public interface PrintServiceCallbacks {
        void onCustomPrinterIconLoaded(PrinterId printerId, Icon icon);

        void onPrintersAdded(List<PrinterInfo> list);

        void onPrintersRemoved(List<PrinterId> list);

        void onServiceDied(RemotePrintService remotePrintService);
    }

    private static final class RemotePrintServiceClient extends Stub {
        private final WeakReference<RemotePrintService> mWeakService;

        public RemotePrintServiceClient(RemotePrintService service) {
            this.mWeakService = new WeakReference(service);
        }

        public List<PrintJobInfo> getPrintJobInfos() {
            RemotePrintService service = (RemotePrintService) this.mWeakService.get();
            if (service == null) {
                return null;
            }
            long identity = Binder.clearCallingIdentity();
            try {
                List<PrintJobInfo> printJobInfos = service.mSpooler.getPrintJobInfos(service.mComponentName, -4, -2);
                return printJobInfos;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public PrintJobInfo getPrintJobInfo(PrintJobId printJobId) {
            RemotePrintService service = (RemotePrintService) this.mWeakService.get();
            if (service == null) {
                return null;
            }
            long identity = Binder.clearCallingIdentity();
            try {
                PrintJobInfo printJobInfo = service.mSpooler.getPrintJobInfo(printJobId, -2);
                return printJobInfo;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public boolean setPrintJobState(PrintJobId printJobId, int state, String error) {
            RemotePrintService service = (RemotePrintService) this.mWeakService.get();
            if (service == null) {
                return false;
            }
            long identity = Binder.clearCallingIdentity();
            try {
                boolean printJobState = service.mSpooler.setPrintJobState(printJobId, state, error);
                return printJobState;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public boolean setPrintJobTag(PrintJobId printJobId, String tag) {
            RemotePrintService service = (RemotePrintService) this.mWeakService.get();
            if (service == null) {
                return false;
            }
            long identity = Binder.clearCallingIdentity();
            try {
                boolean printJobTag = service.mSpooler.setPrintJobTag(printJobId, tag);
                return printJobTag;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void writePrintJobData(ParcelFileDescriptor fd, PrintJobId printJobId) {
            RemotePrintService service = (RemotePrintService) this.mWeakService.get();
            if (service != null) {
                long identity = Binder.clearCallingIdentity();
                try {
                    service.mSpooler.writePrintJobData(fd, printJobId);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void setProgress(PrintJobId printJobId, float progress) {
            RemotePrintService service = (RemotePrintService) this.mWeakService.get();
            if (service != null) {
                long identity = Binder.clearCallingIdentity();
                try {
                    service.mSpooler.setProgress(printJobId, progress);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void setStatus(PrintJobId printJobId, CharSequence status) {
            RemotePrintService service = (RemotePrintService) this.mWeakService.get();
            if (service != null) {
                long identity = Binder.clearCallingIdentity();
                try {
                    service.mSpooler.setStatus(printJobId, status);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void setStatusRes(PrintJobId printJobId, int status, CharSequence appPackageName) {
            RemotePrintService service = (RemotePrintService) this.mWeakService.get();
            if (service != null) {
                long identity = Binder.clearCallingIdentity();
                try {
                    service.mSpooler.setStatus(printJobId, status, appPackageName);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void onPrintersAdded(ParceledListSlice printers) {
            RemotePrintService service = (RemotePrintService) this.mWeakService.get();
            if (service != null) {
                List<PrinterInfo> addedPrinters = printers.getList();
                throwIfPrinterIdsForPrinterInfoTampered(service.mComponentName, addedPrinters);
                long identity = Binder.clearCallingIdentity();
                try {
                    service.mCallbacks.onPrintersAdded(addedPrinters);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void onPrintersRemoved(ParceledListSlice printerIds) {
            RemotePrintService service = (RemotePrintService) this.mWeakService.get();
            if (service != null) {
                List<PrinterId> removedPrinterIds = printerIds.getList();
                throwIfPrinterIdsTampered(service.mComponentName, removedPrinterIds);
                long identity = Binder.clearCallingIdentity();
                try {
                    service.mCallbacks.onPrintersRemoved(removedPrinterIds);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        private void throwIfPrinterIdsForPrinterInfoTampered(ComponentName serviceName, List<PrinterInfo> printerInfos) {
            int printerInfoCount = printerInfos.size();
            for (int i = 0; i < printerInfoCount; i++) {
                throwIfPrinterIdTampered(serviceName, ((PrinterInfo) printerInfos.get(i)).getId());
            }
        }

        private void throwIfPrinterIdsTampered(ComponentName serviceName, List<PrinterId> printerIds) {
            int printerIdCount = printerIds.size();
            for (int i = 0; i < printerIdCount; i++) {
                throwIfPrinterIdTampered(serviceName, (PrinterId) printerIds.get(i));
            }
        }

        private void throwIfPrinterIdTampered(ComponentName serviceName, PrinterId printerId) {
            if (printerId == null || (printerId.getServiceName().equals(serviceName) ^ 1) != 0) {
                throw new IllegalArgumentException("Invalid printer id: " + printerId);
            }
        }

        public void onCustomPrinterIconLoaded(PrinterId printerId, Icon icon) throws RemoteException {
            RemotePrintService service = (RemotePrintService) this.mWeakService.get();
            if (service != null) {
                long identity = Binder.clearCallingIdentity();
                try {
                    service.mCallbacks.onCustomPrinterIconLoaded(printerId, icon);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }
    }

    private class RemoteServiceConneciton implements ServiceConnection {
        /* synthetic */ RemoteServiceConneciton(RemotePrintService this$0, RemoteServiceConneciton -this1) {
            this();
        }

        private RemoteServiceConneciton() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            if (RemotePrintService.this.mDestroyed || (RemotePrintService.this.mBinding ^ 1) != 0) {
                RemotePrintService.this.mContext.unbindService(RemotePrintService.this.mServiceConnection);
                return;
            }
            RemotePrintService.this.mBinding = false;
            RemotePrintService.this.mPrintService = IPrintService.Stub.asInterface(service);
            try {
                service.linkToDeath(RemotePrintService.this, 0);
                try {
                    RemotePrintService.this.mPrintService.setClient(RemotePrintService.this.mPrintServiceClient);
                    if (RemotePrintService.this.mServiceDied && RemotePrintService.this.mHasPrinterDiscoverySession) {
                        RemotePrintService.this.handleCreatePrinterDiscoverySession();
                    }
                    if (RemotePrintService.this.mServiceDied && RemotePrintService.this.mDiscoveryPriorityList != null) {
                        RemotePrintService.this.handleStartPrinterDiscovery(RemotePrintService.this.mDiscoveryPriorityList);
                    }
                    if (RemotePrintService.this.mServiceDied && RemotePrintService.this.mTrackedPrinterList != null) {
                        int trackedPrinterCount = RemotePrintService.this.mTrackedPrinterList.size();
                        for (int i = 0; i < trackedPrinterCount; i++) {
                            RemotePrintService.this.handleStartPrinterStateTracking((PrinterId) RemotePrintService.this.mTrackedPrinterList.get(i));
                        }
                    }
                    while (!RemotePrintService.this.mPendingCommands.isEmpty()) {
                        ((Runnable) RemotePrintService.this.mPendingCommands.remove(0)).run();
                    }
                    if (!(RemotePrintService.this.mHasPrinterDiscoverySession || (RemotePrintService.this.mHasActivePrintJobs ^ 1) == 0)) {
                        RemotePrintService.this.ensureUnbound();
                    }
                    RemotePrintService.this.mServiceDied = false;
                } catch (RemoteException re) {
                    Slog.e(RemotePrintService.LOG_TAG, "Error setting client for: " + service, re);
                    RemotePrintService.this.handleBinderDied();
                }
            } catch (RemoteException e) {
                RemotePrintService.this.handleBinderDied();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            RemotePrintService.this.mBinding = true;
        }
    }

    public RemotePrintService(Context context, ComponentName componentName, int userId, RemotePrintSpooler spooler, PrintServiceCallbacks callbacks) {
        this.mContext = context;
        this.mCallbacks = callbacks;
        this.mComponentName = componentName;
        this.mIntent = new Intent().setComponent(this.mComponentName);
        this.mUserId = userId;
        this.mSpooler = spooler;
        this.mHandler = new MyHandler(context.getMainLooper());
        this.mPrintServiceClient = new RemotePrintServiceClient(this);
    }

    public ComponentName getComponentName() {
        return this.mComponentName;
    }

    public void destroy() {
        this.mHandler.sendEmptyMessage(11);
    }

    private void handleDestroy() {
        stopTrackingAllPrinters();
        if (this.mDiscoveryPriorityList != null) {
            handleStopPrinterDiscovery();
        }
        if (this.mHasPrinterDiscoverySession) {
            handleDestroyPrinterDiscoverySession();
        }
        ensureUnbound();
        this.mDestroyed = true;
    }

    public void binderDied() {
        this.mHandler.sendEmptyMessage(12);
    }

    private void handleBinderDied() {
        if (this.mPrintService != null) {
            this.mPrintService.asBinder().unlinkToDeath(this, 0);
        }
        this.mPrintService = null;
        this.mServiceDied = true;
        this.mCallbacks.onServiceDied(this);
    }

    public void onAllPrintJobsHandled() {
        this.mHandler.sendEmptyMessage(8);
    }

    private void handleOnAllPrintJobsHandled() {
        this.mHasActivePrintJobs = false;
        if (isBound()) {
            if (!this.mHasPrinterDiscoverySession) {
                ensureUnbound();
            }
        } else if (!this.mServiceDied || (this.mHasPrinterDiscoverySession ^ 1) == 0) {
            ensureBound();
            this.mPendingCommands.add(new Runnable() {
                public void run() {
                    RemotePrintService.this.handleOnAllPrintJobsHandled();
                }
            });
        } else {
            ensureUnbound();
        }
    }

    public void onRequestCancelPrintJob(PrintJobInfo printJob) {
        this.mHandler.obtainMessage(9, printJob).sendToTarget();
    }

    private void handleRequestCancelPrintJob(final PrintJobInfo printJob) {
        if (isBound()) {
            try {
                this.mPrintService.requestCancelPrintJob(printJob);
                return;
            } catch (RemoteException re) {
                Slog.e(LOG_TAG, "Error canceling a pring job.", re);
                return;
            }
        }
        ensureBound();
        this.mPendingCommands.add(new Runnable() {
            public void run() {
                RemotePrintService.this.handleRequestCancelPrintJob(printJob);
            }
        });
    }

    public void onPrintJobQueued(PrintJobInfo printJob) {
        this.mHandler.obtainMessage(10, printJob).sendToTarget();
    }

    private void handleOnPrintJobQueued(final PrintJobInfo printJob) {
        this.mHasActivePrintJobs = true;
        if (isBound()) {
            try {
                this.mPrintService.onPrintJobQueued(printJob);
                return;
            } catch (RemoteException re) {
                Slog.e(LOG_TAG, "Error announcing queued pring job.", re);
                return;
            }
        }
        ensureBound();
        this.mPendingCommands.add(new Runnable() {
            public void run() {
                RemotePrintService.this.handleOnPrintJobQueued(printJob);
            }
        });
    }

    public void createPrinterDiscoverySession() {
        this.mHandler.sendEmptyMessage(1);
    }

    private void handleCreatePrinterDiscoverySession() {
        this.mHasPrinterDiscoverySession = true;
        if (isBound()) {
            try {
                this.mPrintService.createPrinterDiscoverySession();
                return;
            } catch (RemoteException re) {
                Slog.e(LOG_TAG, "Error creating printer discovery session.", re);
                return;
            }
        }
        ensureBound();
        this.mPendingCommands.add(new Runnable() {
            public void run() {
                RemotePrintService.this.handleCreatePrinterDiscoverySession();
            }
        });
    }

    public void destroyPrinterDiscoverySession() {
        this.mHandler.sendEmptyMessage(2);
    }

    private void handleDestroyPrinterDiscoverySession() {
        this.mHasPrinterDiscoverySession = false;
        if (isBound()) {
            try {
                this.mPrintService.destroyPrinterDiscoverySession();
            } catch (RemoteException re) {
                Slog.e(LOG_TAG, "Error destroying printer dicovery session.", re);
            }
            if (!this.mHasActivePrintJobs) {
                ensureUnbound();
            }
        } else if (!this.mServiceDied || (this.mHasActivePrintJobs ^ 1) == 0) {
            ensureBound();
            this.mPendingCommands.add(new Runnable() {
                public void run() {
                    RemotePrintService.this.handleDestroyPrinterDiscoverySession();
                }
            });
        } else {
            ensureUnbound();
        }
    }

    public void startPrinterDiscovery(List<PrinterId> priorityList) {
        this.mHandler.obtainMessage(3, priorityList).sendToTarget();
    }

    private void handleStartPrinterDiscovery(final List<PrinterId> priorityList) {
        this.mDiscoveryPriorityList = new ArrayList();
        if (priorityList != null) {
            this.mDiscoveryPriorityList.addAll(priorityList);
        }
        if (isBound()) {
            try {
                this.mPrintService.startPrinterDiscovery(priorityList);
                return;
            } catch (RemoteException re) {
                Slog.e(LOG_TAG, "Error starting printer dicovery.", re);
                return;
            }
        }
        ensureBound();
        this.mPendingCommands.add(new Runnable() {
            public void run() {
                RemotePrintService.this.handleStartPrinterDiscovery(priorityList);
            }
        });
    }

    public void stopPrinterDiscovery() {
        this.mHandler.sendEmptyMessage(4);
    }

    private void handleStopPrinterDiscovery() {
        this.mDiscoveryPriorityList = null;
        if (isBound()) {
            stopTrackingAllPrinters();
            try {
                this.mPrintService.stopPrinterDiscovery();
                return;
            } catch (RemoteException re) {
                Slog.e(LOG_TAG, "Error stopping printer discovery.", re);
                return;
            }
        }
        ensureBound();
        this.mPendingCommands.add(new Runnable() {
            public void run() {
                RemotePrintService.this.handleStopPrinterDiscovery();
            }
        });
    }

    public void validatePrinters(List<PrinterId> printerIds) {
        this.mHandler.obtainMessage(5, printerIds).sendToTarget();
    }

    private void handleValidatePrinters(final List<PrinterId> printerIds) {
        if (isBound()) {
            try {
                this.mPrintService.validatePrinters(printerIds);
                return;
            } catch (RemoteException re) {
                Slog.e(LOG_TAG, "Error requesting printers validation.", re);
                return;
            }
        }
        ensureBound();
        this.mPendingCommands.add(new Runnable() {
            public void run() {
                RemotePrintService.this.handleValidatePrinters(printerIds);
            }
        });
    }

    public void startPrinterStateTracking(PrinterId printerId) {
        this.mHandler.obtainMessage(6, printerId).sendToTarget();
    }

    public void requestCustomPrinterIcon(PrinterId printerId) {
        this.mHandler.obtainMessage(13, printerId).sendToTarget();
    }

    private void handleRequestCustomPrinterIcon(PrinterId printerId) {
        if (isBound()) {
            try {
                this.mPrintService.requestCustomPrinterIcon(printerId);
                return;
            } catch (RemoteException re) {
                Slog.e(LOG_TAG, "Error requesting icon for " + printerId, re);
                return;
            }
        }
        ensureBound();
        this.mPendingCommands.add(new -$Lambda$PHtuk3kPMEVVxEK9wr6uEdCW_Hc(this, printerId));
    }

    private void handleStartPrinterStateTracking(final PrinterId printerId) {
        if (this.mTrackedPrinterList == null) {
            this.mTrackedPrinterList = new ArrayList();
        }
        this.mTrackedPrinterList.add(printerId);
        if (isBound()) {
            try {
                this.mPrintService.startPrinterStateTracking(printerId);
                return;
            } catch (RemoteException re) {
                Slog.e(LOG_TAG, "Error requesting start printer tracking.", re);
                return;
            }
        }
        ensureBound();
        this.mPendingCommands.add(new Runnable() {
            public void run() {
                RemotePrintService.this.handleStartPrinterStateTracking(printerId);
            }
        });
    }

    public void stopPrinterStateTracking(PrinterId printerId) {
        this.mHandler.obtainMessage(7, printerId).sendToTarget();
    }

    private void handleStopPrinterStateTracking(final PrinterId printerId) {
        if (this.mTrackedPrinterList != null && (this.mTrackedPrinterList.remove(printerId) ^ 1) == 0) {
            if (this.mTrackedPrinterList.isEmpty()) {
                this.mTrackedPrinterList = null;
            }
            if (isBound()) {
                try {
                    this.mPrintService.stopPrinterStateTracking(printerId);
                } catch (RemoteException re) {
                    Slog.e(LOG_TAG, "Error requesting stop printer tracking.", re);
                }
            } else {
                ensureBound();
                this.mPendingCommands.add(new Runnable() {
                    public void run() {
                        RemotePrintService.this.handleStopPrinterStateTracking(printerId);
                    }
                });
            }
        }
    }

    private void stopTrackingAllPrinters() {
        if (this.mTrackedPrinterList != null) {
            for (int i = this.mTrackedPrinterList.size() - 1; i >= 0; i--) {
                PrinterId printerId = (PrinterId) this.mTrackedPrinterList.get(i);
                if (printerId.getServiceName().equals(this.mComponentName)) {
                    handleStopPrinterStateTracking(printerId);
                }
            }
        }
    }

    public void dump(PrintWriter pw, String prefix) {
        String tab = "  ";
        pw.append(prefix).append("service:").println();
        pw.append(prefix).append(tab).append("componentName=").append(this.mComponentName.flattenToString()).println();
        pw.append(prefix).append(tab).append("destroyed=").append(String.valueOf(this.mDestroyed)).println();
        pw.append(prefix).append(tab).append("bound=").append(String.valueOf(isBound())).println();
        pw.append(prefix).append(tab).append("hasDicoverySession=").append(String.valueOf(this.mHasPrinterDiscoverySession)).println();
        pw.append(prefix).append(tab).append("hasActivePrintJobs=").append(String.valueOf(this.mHasActivePrintJobs)).println();
        pw.append(prefix).append(tab).append("isDiscoveringPrinters=").append(String.valueOf(this.mDiscoveryPriorityList != null)).println();
        pw.append(prefix).append(tab).append("trackedPrinters=").append(this.mTrackedPrinterList != null ? this.mTrackedPrinterList.toString() : "null");
    }

    private boolean isBound() {
        return this.mPrintService != null;
    }

    private void ensureBound() {
        if (!isBound() && !this.mBinding) {
            this.mBinding = true;
            if (!this.mContext.bindServiceAsUser(this.mIntent, this.mServiceConnection, 67108865, new UserHandle(this.mUserId))) {
                this.mBinding = false;
                if (!this.mServiceDied) {
                    handleBinderDied();
                }
            }
        }
    }

    private void ensureUnbound() {
        if (isBound() || (this.mBinding ^ 1) == 0) {
            this.mBinding = false;
            this.mPendingCommands.clear();
            this.mHasActivePrintJobs = false;
            this.mHasPrinterDiscoverySession = false;
            this.mDiscoveryPriorityList = null;
            this.mTrackedPrinterList = null;
            if (isBound()) {
                try {
                    this.mPrintService.setClient(null);
                } catch (RemoteException e) {
                }
                this.mPrintService.asBinder().unlinkToDeath(this, 0);
                this.mPrintService = null;
                this.mContext.unbindService(this.mServiceConnection);
            }
        }
    }
}
