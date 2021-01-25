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
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.UserHandle;
import android.print.PrintJobId;
import android.print.PrintJobInfo;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.printservice.IPrintService;
import android.printservice.IPrintServiceClient;
import android.util.Log;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.dump.DualDumpOutputStream;
import com.android.internal.util.dump.DumpUtils;
import com.android.internal.util.function.pooled.PooledLambda;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/* access modifiers changed from: package-private */
public final class RemotePrintService implements IBinder.DeathRecipient {
    private static final boolean DEBUG = Log.HWINFO;
    private static final String LOG_TAG = "RemotePrintService";
    private boolean mBinding;
    private final PrintServiceCallbacks mCallbacks;
    private final ComponentName mComponentName;
    private final Context mContext;
    private boolean mDestroyed;
    private List<PrinterId> mDiscoveryPriorityList;
    private boolean mHasActivePrintJobs;
    private boolean mHasPrinterDiscoverySession;
    private final Intent mIntent;
    private final Object mLock = new Object();
    private final List<Runnable> mPendingCommands = new ArrayList();
    private IPrintService mPrintService;
    private final RemotePrintServiceClient mPrintServiceClient;
    private final ServiceConnection mServiceConnection = new RemoteServiceConneciton();
    private boolean mServiceDied;
    private final RemotePrintSpooler mSpooler;
    @GuardedBy({"mLock"})
    private List<PrinterId> mTrackedPrinterList;
    private final int mUserId;

    public interface PrintServiceCallbacks {
        void onCustomPrinterIconLoaded(PrinterId printerId, Icon icon);

        void onPrintersAdded(List<PrinterInfo> list);

        void onPrintersRemoved(List<PrinterId> list);

        void onServiceDied(RemotePrintService remotePrintService);
    }

    public RemotePrintService(Context context, ComponentName componentName, int userId, RemotePrintSpooler spooler, PrintServiceCallbacks callbacks) {
        this.mContext = context;
        this.mCallbacks = callbacks;
        this.mComponentName = componentName;
        this.mIntent = new Intent().setComponent(this.mComponentName);
        this.mUserId = userId;
        this.mSpooler = spooler;
        this.mPrintServiceClient = new RemotePrintServiceClient(this);
    }

    public ComponentName getComponentName() {
        return this.mComponentName;
    }

    public void destroy() {
        Handler.getMain().sendMessage(PooledLambda.obtainMessage($$Lambda$RemotePrintService$tI07K2u4Z5L72sd1hvSEunGclrg.INSTANCE, this));
    }

    /* access modifiers changed from: private */
    public void handleDestroy() {
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

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        Handler.getMain().sendMessage(PooledLambda.obtainMessage($$Lambda$RemotePrintService$uBWTskFvpksxzoYevxmiaqdMXas.INSTANCE, this));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void handleBinderDied() {
        IPrintService iPrintService = this.mPrintService;
        if (iPrintService != null) {
            try {
                iPrintService.asBinder().unlinkToDeath(this, 0);
            } catch (NoSuchElementException e) {
                Slog.e(LOG_TAG, "handleBinderDied Unable to unlinkToDeath!", e);
            }
        }
        this.mPrintService = null;
        this.mServiceDied = true;
        this.mCallbacks.onServiceDied(this);
    }

    public void onAllPrintJobsHandled() {
        Handler.getMain().sendMessage(PooledLambda.obtainMessage($$Lambda$RemotePrintService$1cbVOJkW_ULFS1xHTtbALCzHI.INSTANCE, this));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void handleOnAllPrintJobsHandled() {
        this.mHasActivePrintJobs = false;
        if (isBound()) {
            if (DEBUG) {
                Slog.i(LOG_TAG, "[user: " + this.mUserId + "] onAllPrintJobsHandled()");
            }
            if (!this.mHasPrinterDiscoverySession) {
                ensureUnbound();
            }
        } else if (!this.mServiceDied || this.mHasPrinterDiscoverySession) {
            ensureBound();
            this.mPendingCommands.add(new Runnable() {
                /* class com.android.server.print.RemotePrintService.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    RemotePrintService.this.handleOnAllPrintJobsHandled();
                }
            });
        } else {
            ensureUnbound();
        }
    }

    public void onRequestCancelPrintJob(PrintJobInfo printJob) {
        Handler.getMain().sendMessage(PooledLambda.obtainMessage($$Lambda$RemotePrintService$tL9wtChZzY3deiul1VudkrPO20.INSTANCE, this, printJob));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void handleRequestCancelPrintJob(final PrintJobInfo printJob) {
        if (!isBound()) {
            ensureBound();
            this.mPendingCommands.add(new Runnable() {
                /* class com.android.server.print.RemotePrintService.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    RemotePrintService.this.handleRequestCancelPrintJob(printJob);
                }
            });
            return;
        }
        if (DEBUG) {
            Slog.i(LOG_TAG, "[user: " + this.mUserId + "] requestCancelPrintJob()");
        }
        try {
            this.mPrintService.requestCancelPrintJob(printJob);
        } catch (RemoteException re) {
            Slog.e(LOG_TAG, "Error canceling a pring job.", re);
        }
    }

    public void onPrintJobQueued(PrintJobInfo printJob) {
        Handler.getMain().sendMessage(PooledLambda.obtainMessage($$Lambda$RemotePrintService$KGsYx3sHW6vGymod4UmBTazYSks.INSTANCE, this, printJob));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void handleOnPrintJobQueued(final PrintJobInfo printJob) {
        this.mHasActivePrintJobs = true;
        if (!isBound()) {
            ensureBound();
            this.mPendingCommands.add(new Runnable() {
                /* class com.android.server.print.RemotePrintService.AnonymousClass3 */

                @Override // java.lang.Runnable
                public void run() {
                    RemotePrintService.this.handleOnPrintJobQueued(printJob);
                }
            });
            return;
        }
        if (DEBUG) {
            Slog.i(LOG_TAG, "[user: " + this.mUserId + "] onPrintJobQueued()");
        }
        try {
            this.mPrintService.onPrintJobQueued(printJob);
        } catch (RemoteException re) {
            Slog.e(LOG_TAG, "Error announcing queued pring job.", re);
        }
    }

    public void createPrinterDiscoverySession() {
        Handler.getMain().sendMessage(PooledLambda.obtainMessage($$Lambda$RemotePrintService$pgSurbN2geCgHp9vfTAIFm5XvgQ.INSTANCE, this));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void handleCreatePrinterDiscoverySession() {
        this.mHasPrinterDiscoverySession = true;
        if (!isBound()) {
            ensureBound();
            this.mPendingCommands.add(new Runnable() {
                /* class com.android.server.print.RemotePrintService.AnonymousClass4 */

                @Override // java.lang.Runnable
                public void run() {
                    RemotePrintService.this.handleCreatePrinterDiscoverySession();
                }
            });
            return;
        }
        if (DEBUG) {
            Slog.i(LOG_TAG, "[user: " + this.mUserId + "] createPrinterDiscoverySession()");
        }
        try {
            this.mPrintService.createPrinterDiscoverySession();
        } catch (RemoteException re) {
            Slog.e(LOG_TAG, "Error creating printer discovery session.", re);
        }
    }

    public void destroyPrinterDiscoverySession() {
        Handler.getMain().sendMessage(PooledLambda.obtainMessage($$Lambda$RemotePrintService$ru7USNI_O2DIDwflMPlEsqA_IY4.INSTANCE, this));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void handleDestroyPrinterDiscoverySession() {
        this.mHasPrinterDiscoverySession = false;
        if (isBound()) {
            if (DEBUG) {
                Slog.i(LOG_TAG, "[user: " + this.mUserId + "] destroyPrinterDiscoverySession()");
            }
            try {
                this.mPrintService.destroyPrinterDiscoverySession();
            } catch (RemoteException re) {
                Slog.e(LOG_TAG, "Error destroying printer dicovery session.", re);
            }
            if (!this.mHasActivePrintJobs) {
                ensureUnbound();
            }
        } else if (!this.mServiceDied || this.mHasActivePrintJobs) {
            ensureBound();
            this.mPendingCommands.add(new Runnable() {
                /* class com.android.server.print.RemotePrintService.AnonymousClass5 */

                @Override // java.lang.Runnable
                public void run() {
                    RemotePrintService.this.handleDestroyPrinterDiscoverySession();
                }
            });
        } else {
            ensureUnbound();
        }
    }

    public void startPrinterDiscovery(List<PrinterId> priorityList) {
        Handler.getMain().sendMessage(PooledLambda.obtainMessage($$Lambda$RemotePrintService$jrFOjxtIoMNm8S0KNTqIDHuv4oY.INSTANCE, this, priorityList));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void handleStartPrinterDiscovery(final List<PrinterId> priorityList) {
        this.mDiscoveryPriorityList = new ArrayList();
        if (priorityList != null) {
            this.mDiscoveryPriorityList.addAll(priorityList);
        }
        if (!isBound()) {
            ensureBound();
            this.mPendingCommands.add(new Runnable() {
                /* class com.android.server.print.RemotePrintService.AnonymousClass6 */

                @Override // java.lang.Runnable
                public void run() {
                    RemotePrintService.this.handleStartPrinterDiscovery(priorityList);
                }
            });
            return;
        }
        if (DEBUG) {
            Slog.i(LOG_TAG, "[user: " + this.mUserId + "] startPrinterDiscovery()");
        }
        try {
            this.mPrintService.startPrinterDiscovery(priorityList);
        } catch (RemoteException re) {
            Slog.e(LOG_TAG, "Error starting printer dicovery.", re);
        }
    }

    public void stopPrinterDiscovery() {
        Handler.getMain().sendMessage(PooledLambda.obtainMessage($$Lambda$RemotePrintService$FH95Crnc6zH421SxRw9RxPyl0YY.INSTANCE, this));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void handleStopPrinterDiscovery() {
        this.mDiscoveryPriorityList = null;
        if (!isBound()) {
            ensureBound();
            this.mPendingCommands.add(new Runnable() {
                /* class com.android.server.print.RemotePrintService.AnonymousClass7 */

                @Override // java.lang.Runnable
                public void run() {
                    RemotePrintService.this.handleStopPrinterDiscovery();
                }
            });
            return;
        }
        if (DEBUG) {
            Slog.i(LOG_TAG, "[user: " + this.mUserId + "] stopPrinterDiscovery()");
        }
        stopTrackingAllPrinters();
        try {
            this.mPrintService.stopPrinterDiscovery();
        } catch (RemoteException re) {
            Slog.e(LOG_TAG, "Error stopping printer discovery.", re);
        }
    }

    public void validatePrinters(List<PrinterId> printerIds) {
        Handler.getMain().sendMessage(PooledLambda.obtainMessage($$Lambda$RemotePrintService$q0Rw93bA7P79FpkLlFZXs5xcOoc.INSTANCE, this, printerIds));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void handleValidatePrinters(final List<PrinterId> printerIds) {
        if (!isBound()) {
            ensureBound();
            this.mPendingCommands.add(new Runnable() {
                /* class com.android.server.print.RemotePrintService.AnonymousClass8 */

                @Override // java.lang.Runnable
                public void run() {
                    RemotePrintService.this.handleValidatePrinters(printerIds);
                }
            });
            return;
        }
        if (DEBUG) {
            Slog.i(LOG_TAG, "[user: " + this.mUserId + "] validatePrinters()");
        }
        try {
            this.mPrintService.validatePrinters(printerIds);
        } catch (RemoteException re) {
            Slog.e(LOG_TAG, "Error requesting printers validation.", re);
        }
    }

    public void startPrinterStateTracking(PrinterId printerId) {
        Handler.getMain().sendMessage(PooledLambda.obtainMessage($$Lambda$RemotePrintService$aHccJYzTXxafcxxvfW2janFHIc.INSTANCE, this, printerId));
    }

    public void requestCustomPrinterIcon(PrinterId printerId) {
        Handler.getMain().sendMessage(PooledLambda.obtainMessage($$Lambda$RemotePrintService$TsHHZCuIB3sKEZ8IZ0oPokZZO6g.INSTANCE, this, printerId));
    }

    /* access modifiers changed from: private */
    /* renamed from: handleRequestCustomPrinterIcon */
    public void lambda$handleRequestCustomPrinterIcon$0$RemotePrintService(PrinterId printerId) {
        if (!isBound()) {
            ensureBound();
            this.mPendingCommands.add(new Runnable(printerId) {
                /* class com.android.server.print.$$Lambda$RemotePrintService$e0Ck2QZDih6p896nITpWZ_zOduk */
                private final /* synthetic */ PrinterId f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    RemotePrintService.this.lambda$handleRequestCustomPrinterIcon$0$RemotePrintService(this.f$1);
                }
            });
            return;
        }
        if (DEBUG) {
            Slog.i(LOG_TAG, "[user: " + this.mUserId + "] requestCustomPrinterIcon()");
        }
        try {
            this.mPrintService.requestCustomPrinterIcon(printerId);
        } catch (RemoteException re) {
            Slog.e(LOG_TAG, "Error requesting icon for " + printerId, re);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void handleStartPrinterStateTracking(final PrinterId printerId) {
        synchronized (this.mLock) {
            if (this.mTrackedPrinterList == null) {
                this.mTrackedPrinterList = new ArrayList();
            }
            this.mTrackedPrinterList.add(printerId);
        }
        if (!isBound()) {
            ensureBound();
            this.mPendingCommands.add(new Runnable() {
                /* class com.android.server.print.RemotePrintService.AnonymousClass9 */

                @Override // java.lang.Runnable
                public void run() {
                    RemotePrintService.this.handleStartPrinterStateTracking(printerId);
                }
            });
            return;
        }
        if (DEBUG) {
            Slog.i(LOG_TAG, "[user: " + this.mUserId + "] startPrinterTracking()");
        }
        try {
            this.mPrintService.startPrinterStateTracking(printerId);
        } catch (RemoteException re) {
            Slog.e(LOG_TAG, "Error requesting start printer tracking.", re);
        }
    }

    public void stopPrinterStateTracking(PrinterId printerId) {
        Handler.getMain().sendMessage(PooledLambda.obtainMessage($$Lambda$RemotePrintService$L2EQSyIHled1ZVO5GCaBXmvtCQQ.INSTANCE, this, printerId));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void handleStopPrinterStateTracking(final PrinterId printerId) {
        synchronized (this.mLock) {
            if (this.mTrackedPrinterList != null) {
                if (this.mTrackedPrinterList.remove(printerId)) {
                    if (this.mTrackedPrinterList.isEmpty()) {
                        this.mTrackedPrinterList = null;
                    }
                }
            }
            return;
        }
        if (!isBound()) {
            ensureBound();
            this.mPendingCommands.add(new Runnable() {
                /* class com.android.server.print.RemotePrintService.AnonymousClass10 */

                @Override // java.lang.Runnable
                public void run() {
                    RemotePrintService.this.handleStopPrinterStateTracking(printerId);
                }
            });
            return;
        }
        if (DEBUG) {
            Slog.i(LOG_TAG, "[user: " + this.mUserId + "] stopPrinterTracking()");
        }
        try {
            this.mPrintService.stopPrinterStateTracking(printerId);
        } catch (RemoteException re) {
            Slog.e(LOG_TAG, "Error requesting stop printer tracking.", re);
        }
    }

    private void stopTrackingAllPrinters() {
        synchronized (this.mLock) {
            if (this.mTrackedPrinterList != null) {
                for (int i = this.mTrackedPrinterList.size() - 1; i >= 0; i--) {
                    PrinterId printerId = this.mTrackedPrinterList.get(i);
                    if (printerId.getServiceName().equals(this.mComponentName)) {
                        handleStopPrinterStateTracking(printerId);
                    }
                }
            }
        }
    }

    public void dump(DualDumpOutputStream proto) {
        DumpUtils.writeComponentName(proto, "component_name", 1146756268033L, this.mComponentName);
        proto.write("is_destroyed", 1133871366146L, this.mDestroyed);
        proto.write("is_bound", 1133871366147L, isBound());
        proto.write("has_discovery_session", 1133871366148L, this.mHasPrinterDiscoverySession);
        proto.write("has_active_print_jobs", 1133871366149L, this.mHasActivePrintJobs);
        proto.write("is_discovering_printers", 1133871366150L, this.mDiscoveryPriorityList != null);
        synchronized (this.mLock) {
            if (this.mTrackedPrinterList != null) {
                int numTrackedPrinters = this.mTrackedPrinterList.size();
                for (int i = 0; i < numTrackedPrinters; i++) {
                    com.android.internal.print.DumpUtils.writePrinterId(proto, "tracked_printers", 2246267895815L, this.mTrackedPrinterList.get(i));
                }
            }
        }
    }

    private boolean isBound() {
        return this.mPrintService != null;
    }

    private void ensureBound() {
        if (!isBound() && !this.mBinding) {
            if (DEBUG) {
                Slog.i(LOG_TAG, "[user: " + this.mUserId + "] ensureBound()");
            }
            this.mBinding = true;
            if (!this.mContext.bindServiceAsUser(this.mIntent, this.mServiceConnection, 71307265, new UserHandle(this.mUserId))) {
                if (DEBUG) {
                    Slog.i(LOG_TAG, "[user: " + this.mUserId + "] could not bind to " + this.mIntent);
                }
                this.mBinding = false;
                if (!this.mServiceDied) {
                    handleBinderDied();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void ensureUnbound() {
        if (isBound() || this.mBinding) {
            if (DEBUG) {
                Slog.i(LOG_TAG, "[user: " + this.mUserId + "] ensureUnbound()");
            }
            this.mBinding = false;
            this.mPendingCommands.clear();
            this.mHasActivePrintJobs = false;
            this.mHasPrinterDiscoverySession = false;
            this.mDiscoveryPriorityList = null;
            synchronized (this.mLock) {
                this.mTrackedPrinterList = null;
            }
            if (isBound()) {
                try {
                    this.mPrintService.setClient((IPrintServiceClient) null);
                } catch (RemoteException e) {
                }
                this.mPrintService.asBinder().unlinkToDeath(this, 0);
                this.mPrintService = null;
                this.mContext.unbindService(this.mServiceConnection);
            }
        }
    }

    private class RemoteServiceConneciton implements ServiceConnection {
        private RemoteServiceConneciton() {
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (RemotePrintService.this.mDestroyed || !RemotePrintService.this.mBinding) {
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
                        RemotePrintService remotePrintService = RemotePrintService.this;
                        remotePrintService.handleStartPrinterDiscovery(remotePrintService.mDiscoveryPriorityList);
                    }
                    synchronized (RemotePrintService.this.mLock) {
                        if (RemotePrintService.this.mServiceDied && RemotePrintService.this.mTrackedPrinterList != null) {
                            int trackedPrinterCount = RemotePrintService.this.mTrackedPrinterList.size();
                            for (int i = 0; i < trackedPrinterCount; i++) {
                                RemotePrintService.this.handleStartPrinterStateTracking((PrinterId) RemotePrintService.this.mTrackedPrinterList.get(i));
                            }
                        }
                    }
                    while (!RemotePrintService.this.mPendingCommands.isEmpty()) {
                        ((Runnable) RemotePrintService.this.mPendingCommands.remove(0)).run();
                    }
                    if (!RemotePrintService.this.mHasPrinterDiscoverySession && !RemotePrintService.this.mHasActivePrintJobs) {
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

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            RemotePrintService.this.mBinding = true;
        }
    }

    /* access modifiers changed from: private */
    public static final class RemotePrintServiceClient extends IPrintServiceClient.Stub {
        private final WeakReference<RemotePrintService> mWeakService;

        public RemotePrintServiceClient(RemotePrintService service) {
            this.mWeakService = new WeakReference<>(service);
        }

        public List<PrintJobInfo> getPrintJobInfos() {
            RemotePrintService service = this.mWeakService.get();
            if (service == null) {
                return null;
            }
            long identity = Binder.clearCallingIdentity();
            try {
                return service.mSpooler.getPrintJobInfos(service.mComponentName, -4, -2);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public PrintJobInfo getPrintJobInfo(PrintJobId printJobId) {
            RemotePrintService service = this.mWeakService.get();
            if (service == null) {
                return null;
            }
            long identity = Binder.clearCallingIdentity();
            try {
                return service.mSpooler.getPrintJobInfo(printJobId, -2);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public boolean setPrintJobState(PrintJobId printJobId, int state, String error) {
            RemotePrintService service = this.mWeakService.get();
            if (service == null) {
                return false;
            }
            long identity = Binder.clearCallingIdentity();
            try {
                return service.mSpooler.setPrintJobState(printJobId, state, error);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public boolean setPrintJobTag(PrintJobId printJobId, String tag) {
            RemotePrintService service = this.mWeakService.get();
            if (service == null) {
                return false;
            }
            long identity = Binder.clearCallingIdentity();
            try {
                return service.mSpooler.setPrintJobTag(printJobId, tag);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void writePrintJobData(ParcelFileDescriptor fd, PrintJobId printJobId) {
            RemotePrintService service = this.mWeakService.get();
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
            RemotePrintService service = this.mWeakService.get();
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
            RemotePrintService service = this.mWeakService.get();
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
            RemotePrintService service = this.mWeakService.get();
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
            RemotePrintService service = this.mWeakService.get();
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
            RemotePrintService service = this.mWeakService.get();
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
                throwIfPrinterIdTampered(serviceName, printerInfos.get(i).getId());
            }
        }

        private void throwIfPrinterIdsTampered(ComponentName serviceName, List<PrinterId> printerIds) {
            int printerIdCount = printerIds.size();
            for (int i = 0; i < printerIdCount; i++) {
                throwIfPrinterIdTampered(serviceName, printerIds.get(i));
            }
        }

        private void throwIfPrinterIdTampered(ComponentName serviceName, PrinterId printerId) {
            if (printerId == null || !printerId.getServiceName().equals(serviceName)) {
                throw new IllegalArgumentException("Invalid printer id: " + printerId);
            }
        }

        public void onCustomPrinterIconLoaded(PrinterId printerId, Icon icon) throws RemoteException {
            RemotePrintService service = this.mWeakService.get();
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
}
