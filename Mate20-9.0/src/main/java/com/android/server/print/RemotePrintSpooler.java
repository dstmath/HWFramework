package com.android.server.print;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Icon;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.print.IPrintSpooler;
import android.print.IPrintSpoolerCallbacks;
import android.print.IPrintSpoolerClient;
import android.print.PrintJobId;
import android.print.PrintJobInfo;
import android.print.PrinterId;
import android.util.Slog;
import android.util.TimedRemoteCaller;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.TransferPipe;
import com.android.internal.util.dump.DualDumpOutputStream;
import com.android.server.job.controllers.JobStatus;
import com.android.server.utils.PriorityDump;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeoutException;
import libcore.io.IoUtils;

final class RemotePrintSpooler {
    private static final long BIND_SPOOLER_SERVICE_TIMEOUT = (Build.IS_ENG ? JobStatus.DEFAULT_TRIGGER_MAX_DELAY : JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
    private static final boolean DEBUG = true;
    private static final String LOG_TAG = "RemotePrintSpooler";
    /* access modifiers changed from: private */
    public final PrintSpoolerCallbacks mCallbacks;
    private boolean mCanUnbind;
    private final ClearCustomPrinterIconCacheCaller mClearCustomPrinterIconCache = new ClearCustomPrinterIconCacheCaller();
    private final PrintSpoolerClient mClient;
    private final Context mContext;
    private final OnCustomPrinterIconLoadedCaller mCustomPrinterIconLoadedCaller = new OnCustomPrinterIconLoadedCaller();
    private boolean mDestroyed;
    private final GetCustomPrinterIconCaller mGetCustomPrinterIconCaller = new GetCustomPrinterIconCaller();
    private final GetPrintJobInfoCaller mGetPrintJobInfoCaller = new GetPrintJobInfoCaller();
    private final GetPrintJobInfosCaller mGetPrintJobInfosCaller = new GetPrintJobInfosCaller();
    private final Intent mIntent;
    @GuardedBy("mLock")
    private boolean mIsBinding;
    private boolean mIsLowPriority;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    /* access modifiers changed from: private */
    public IPrintSpooler mRemoteInstance;
    private final ServiceConnection mServiceConnection = new MyServiceConnection();
    private final SetPrintJobStateCaller mSetPrintJobStatusCaller = new SetPrintJobStateCaller();
    private final SetPrintJobTagCaller mSetPrintJobTagCaller = new SetPrintJobTagCaller();
    private final UserHandle mUserHandle;

    private static abstract class BasePrintSpoolerServiceCallbacks extends IPrintSpoolerCallbacks.Stub {
        private BasePrintSpoolerServiceCallbacks() {
        }

        public void onGetPrintJobInfosResult(List<PrintJobInfo> list, int sequence) {
        }

        public void onGetPrintJobInfoResult(PrintJobInfo printJob, int sequence) {
        }

        public void onCancelPrintJobResult(boolean canceled, int sequence) {
        }

        public void onSetPrintJobStateResult(boolean success, int sequece) {
        }

        public void onSetPrintJobTagResult(boolean success, int sequence) {
        }

        public void onCustomPrinterIconCached(int sequence) {
        }

        public void onGetCustomPrinterIconResult(Icon icon, int sequence) {
        }

        public void customPrinterIconCacheCleared(int sequence) {
        }
    }

    private static final class ClearCustomPrinterIconCacheCaller extends TimedRemoteCaller<Void> {
        private final IPrintSpoolerCallbacks mCallback = new BasePrintSpoolerServiceCallbacks() {
            public void customPrinterIconCacheCleared(int sequence) {
                ClearCustomPrinterIconCacheCaller.this.onRemoteMethodResult(null, sequence);
            }
        };

        public ClearCustomPrinterIconCacheCaller() {
            super(5000);
        }

        public Void clearCustomPrinterIconCache(IPrintSpooler target) throws RemoteException, TimeoutException {
            int sequence = onBeforeRemoteCall();
            target.clearCustomPrinterIconCache(this.mCallback, sequence);
            return (Void) getResultTimed(sequence);
        }
    }

    private static final class GetCustomPrinterIconCaller extends TimedRemoteCaller<Icon> {
        private final IPrintSpoolerCallbacks mCallback = new BasePrintSpoolerServiceCallbacks() {
            public void onGetCustomPrinterIconResult(Icon icon, int sequence) {
                GetCustomPrinterIconCaller.this.onRemoteMethodResult(icon, sequence);
            }
        };

        public GetCustomPrinterIconCaller() {
            super(5000);
        }

        public Icon getCustomPrinterIcon(IPrintSpooler target, PrinterId printerId) throws RemoteException, TimeoutException {
            int sequence = onBeforeRemoteCall();
            target.getCustomPrinterIcon(printerId, this.mCallback, sequence);
            return (Icon) getResultTimed(sequence);
        }
    }

    private static final class GetPrintJobInfoCaller extends TimedRemoteCaller<PrintJobInfo> {
        private final IPrintSpoolerCallbacks mCallback = new BasePrintSpoolerServiceCallbacks() {
            public void onGetPrintJobInfoResult(PrintJobInfo printJob, int sequence) {
                GetPrintJobInfoCaller.this.onRemoteMethodResult(printJob, sequence);
            }
        };

        public GetPrintJobInfoCaller() {
            super(5000);
        }

        public PrintJobInfo getPrintJobInfo(IPrintSpooler target, PrintJobId printJobId, int appId) throws RemoteException, TimeoutException {
            int sequence = onBeforeRemoteCall();
            target.getPrintJobInfo(printJobId, this.mCallback, appId, sequence);
            return (PrintJobInfo) getResultTimed(sequence);
        }
    }

    private static final class GetPrintJobInfosCaller extends TimedRemoteCaller<List<PrintJobInfo>> {
        private final IPrintSpoolerCallbacks mCallback = new BasePrintSpoolerServiceCallbacks() {
            public void onGetPrintJobInfosResult(List<PrintJobInfo> printJobs, int sequence) {
                GetPrintJobInfosCaller.this.onRemoteMethodResult(printJobs, sequence);
            }
        };

        public GetPrintJobInfosCaller() {
            super(5000);
        }

        public List<PrintJobInfo> getPrintJobInfos(IPrintSpooler target, ComponentName componentName, int state, int appId) throws RemoteException, TimeoutException {
            int sequence = onBeforeRemoteCall();
            target.getPrintJobInfos(this.mCallback, componentName, state, appId, sequence);
            return (List) getResultTimed(sequence);
        }
    }

    private final class MyServiceConnection implements ServiceConnection {
        private MyServiceConnection() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (RemotePrintSpooler.this.mLock) {
                IPrintSpooler unused = RemotePrintSpooler.this.mRemoteInstance = IPrintSpooler.Stub.asInterface(service);
                RemotePrintSpooler.this.setClientLocked();
                RemotePrintSpooler.this.mLock.notifyAll();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            synchronized (RemotePrintSpooler.this.mLock) {
                RemotePrintSpooler.this.clearClientLocked();
                IPrintSpooler unused = RemotePrintSpooler.this.mRemoteInstance = null;
            }
        }
    }

    private static final class OnCustomPrinterIconLoadedCaller extends TimedRemoteCaller<Void> {
        private final IPrintSpoolerCallbacks mCallback = new BasePrintSpoolerServiceCallbacks() {
            public void onCustomPrinterIconCached(int sequence) {
                OnCustomPrinterIconLoadedCaller.this.onRemoteMethodResult(null, sequence);
            }
        };

        public OnCustomPrinterIconLoadedCaller() {
            super(5000);
        }

        public Void onCustomPrinterIconLoaded(IPrintSpooler target, PrinterId printerId, Icon icon) throws RemoteException, TimeoutException {
            int sequence = onBeforeRemoteCall();
            target.onCustomPrinterIconLoaded(printerId, icon, this.mCallback, sequence);
            return (Void) getResultTimed(sequence);
        }
    }

    public interface PrintSpoolerCallbacks {
        void onAllPrintJobsForServiceHandled(ComponentName componentName);

        void onPrintJobQueued(PrintJobInfo printJobInfo);

        void onPrintJobStateChanged(PrintJobInfo printJobInfo);
    }

    private static final class PrintSpoolerClient extends IPrintSpoolerClient.Stub {
        private final WeakReference<RemotePrintSpooler> mWeakSpooler;

        public PrintSpoolerClient(RemotePrintSpooler spooler) {
            this.mWeakSpooler = new WeakReference<>(spooler);
        }

        public void onPrintJobQueued(PrintJobInfo printJob) {
            RemotePrintSpooler spooler = (RemotePrintSpooler) this.mWeakSpooler.get();
            if (spooler != null) {
                long identity = Binder.clearCallingIdentity();
                try {
                    spooler.mCallbacks.onPrintJobQueued(printJob);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void onAllPrintJobsForServiceHandled(ComponentName printService) {
            RemotePrintSpooler spooler = (RemotePrintSpooler) this.mWeakSpooler.get();
            if (spooler != null) {
                long identity = Binder.clearCallingIdentity();
                try {
                    spooler.mCallbacks.onAllPrintJobsForServiceHandled(printService);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void onAllPrintJobsHandled() {
            RemotePrintSpooler spooler = (RemotePrintSpooler) this.mWeakSpooler.get();
            if (spooler != null) {
                long identity = Binder.clearCallingIdentity();
                try {
                    spooler.onAllPrintJobsHandled();
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void onPrintJobStateChanged(PrintJobInfo printJob) {
            RemotePrintSpooler spooler = (RemotePrintSpooler) this.mWeakSpooler.get();
            if (spooler != null) {
                long identity = Binder.clearCallingIdentity();
                try {
                    spooler.onPrintJobStateChanged(printJob);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }
    }

    private static final class SetPrintJobStateCaller extends TimedRemoteCaller<Boolean> {
        private final IPrintSpoolerCallbacks mCallback = new BasePrintSpoolerServiceCallbacks() {
            public void onSetPrintJobStateResult(boolean success, int sequence) {
                SetPrintJobStateCaller.this.onRemoteMethodResult(Boolean.valueOf(success), sequence);
            }
        };

        public SetPrintJobStateCaller() {
            super(5000);
        }

        public boolean setPrintJobState(IPrintSpooler target, PrintJobId printJobId, int status, String error) throws RemoteException, TimeoutException {
            int sequence = onBeforeRemoteCall();
            target.setPrintJobState(printJobId, status, error, this.mCallback, sequence);
            return ((Boolean) getResultTimed(sequence)).booleanValue();
        }
    }

    private static final class SetPrintJobTagCaller extends TimedRemoteCaller<Boolean> {
        private final IPrintSpoolerCallbacks mCallback = new BasePrintSpoolerServiceCallbacks() {
            public void onSetPrintJobTagResult(boolean success, int sequence) {
                SetPrintJobTagCaller.this.onRemoteMethodResult(Boolean.valueOf(success), sequence);
            }
        };

        public SetPrintJobTagCaller() {
            super(5000);
        }

        public boolean setPrintJobTag(IPrintSpooler target, PrintJobId printJobId, String tag) throws RemoteException, TimeoutException {
            int sequence = onBeforeRemoteCall();
            target.setPrintJobTag(printJobId, tag, this.mCallback, sequence);
            return ((Boolean) getResultTimed(sequence)).booleanValue();
        }
    }

    public RemotePrintSpooler(Context context, int userId, boolean lowPriority, PrintSpoolerCallbacks callbacks) {
        this.mContext = context;
        this.mUserHandle = new UserHandle(userId);
        this.mCallbacks = callbacks;
        this.mIsLowPriority = lowPriority;
        this.mClient = new PrintSpoolerClient(this);
        this.mIntent = new Intent();
        this.mIntent.setComponent(new ComponentName("com.android.printspooler", "com.android.printspooler.model.PrintSpoolerService"));
    }

    public void increasePriority() {
        if (this.mIsLowPriority) {
            this.mIsLowPriority = false;
            synchronized (this.mLock) {
                throwIfDestroyedLocked();
                while (!this.mCanUnbind) {
                    try {
                        this.mLock.wait();
                    } catch (InterruptedException e) {
                        Slog.e(LOG_TAG, "Interrupted while waiting for operation to complete");
                    }
                }
                Slog.i(LOG_TAG, "Unbinding as previous binding was low priority");
                unbindLocked();
            }
        }
    }

    public final List<PrintJobInfo> getPrintJobInfos(ComponentName componentName, int state, int appId) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            List<PrintJobInfo> printJobInfos = this.mGetPrintJobInfosCaller.getPrintJobInfos(getRemoteInstanceLazy(), componentName, state, appId);
            Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] getPrintJobInfos()");
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
            return printJobInfos;
        } catch (RemoteException | InterruptedException | TimeoutException e) {
            try {
                Slog.e(LOG_TAG, "Error getting print jobs.", e);
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] getPrintJobInfos()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                    return null;
                }
            } catch (Throwable th) {
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] getPrintJobInfos()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                    throw th;
                }
            }
        }
    }

    public final void createPrintJob(PrintJobInfo printJob) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            getRemoteInstanceLazy().createPrintJob(printJob);
            Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] createPrintJob()");
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        } catch (RemoteException | InterruptedException | TimeoutException e) {
            try {
                Slog.e(LOG_TAG, "Error creating print job.", e);
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] createPrintJob()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                }
            } catch (Throwable th) {
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] createPrintJob()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                    throw th;
                }
            }
        }
    }

    public final void writePrintJobData(ParcelFileDescriptor fd, PrintJobId printJobId) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            getRemoteInstanceLazy().writePrintJobData(fd, printJobId);
            Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] writePrintJobData()");
            IoUtils.closeQuietly(fd);
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        } catch (RemoteException | InterruptedException | TimeoutException e) {
            try {
                Slog.e(LOG_TAG, "Error writing print job data.", e);
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] writePrintJobData()");
                IoUtils.closeQuietly(fd);
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                }
            } catch (Throwable th) {
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] writePrintJobData()");
                IoUtils.closeQuietly(fd);
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                    throw th;
                }
            }
        }
    }

    public final PrintJobInfo getPrintJobInfo(PrintJobId printJobId, int appId) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            PrintJobInfo printJobInfo = this.mGetPrintJobInfoCaller.getPrintJobInfo(getRemoteInstanceLazy(), printJobId, appId);
            Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] getPrintJobInfo()");
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
            return printJobInfo;
        } catch (RemoteException | InterruptedException | TimeoutException e) {
            try {
                Slog.e(LOG_TAG, "Error getting print job info.", e);
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] getPrintJobInfo()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                    return null;
                }
            } catch (Throwable th) {
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] getPrintJobInfo()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                    throw th;
                }
            }
        }
    }

    public final boolean setPrintJobState(PrintJobId printJobId, int state, String error) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            boolean printJobState = this.mSetPrintJobStatusCaller.setPrintJobState(getRemoteInstanceLazy(), printJobId, state, error);
            Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] setPrintJobState()");
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
            return printJobState;
        } catch (RemoteException | InterruptedException | TimeoutException e) {
            try {
                Slog.e(LOG_TAG, "Error setting print job state.", e);
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] setPrintJobState()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                    return false;
                }
            } catch (Throwable th) {
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] setPrintJobState()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                    throw th;
                }
            }
        }
    }

    public final void setProgress(PrintJobId printJobId, float progress) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            getRemoteInstanceLazy().setProgress(printJobId, progress);
            Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] setProgress()");
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        } catch (RemoteException | InterruptedException | TimeoutException re) {
            try {
                Slog.e(LOG_TAG, "Error setting progress.", re);
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] setProgress()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                }
            } catch (Throwable th) {
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] setProgress()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                    throw th;
                }
            }
        }
    }

    public final void setStatus(PrintJobId printJobId, CharSequence status) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            getRemoteInstanceLazy().setStatus(printJobId, status);
            Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] setStatus()");
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        } catch (RemoteException | InterruptedException | TimeoutException e) {
            try {
                Slog.e(LOG_TAG, "Error setting status.", e);
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] setStatus()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                }
            } catch (Throwable th) {
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] setStatus()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                    throw th;
                }
            }
        }
    }

    public final void setStatus(PrintJobId printJobId, int status, CharSequence appPackageName) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            getRemoteInstanceLazy().setStatusRes(printJobId, status, appPackageName);
            Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] setStatus()");
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        } catch (RemoteException | InterruptedException | TimeoutException e) {
            try {
                Slog.e(LOG_TAG, "Error setting status.", e);
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] setStatus()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                }
            } catch (Throwable th) {
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] setStatus()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                    throw th;
                }
            }
        }
    }

    public final void onCustomPrinterIconLoaded(PrinterId printerId, Icon icon) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            this.mCustomPrinterIconLoadedCaller.onCustomPrinterIconLoaded(getRemoteInstanceLazy(), printerId, icon);
            Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] onCustomPrinterIconLoaded()");
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        } catch (RemoteException | InterruptedException | TimeoutException re) {
            try {
                Slog.e(LOG_TAG, "Error loading new custom printer icon.", re);
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] onCustomPrinterIconLoaded()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                }
            } catch (Throwable th) {
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] onCustomPrinterIconLoaded()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                    throw th;
                }
            }
        }
    }

    public final Icon getCustomPrinterIcon(PrinterId printerId) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            Icon customPrinterIcon = this.mGetCustomPrinterIconCaller.getCustomPrinterIcon(getRemoteInstanceLazy(), printerId);
            Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] getCustomPrinterIcon()");
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
            return customPrinterIcon;
        } catch (RemoteException | InterruptedException | TimeoutException e) {
            try {
                Slog.e(LOG_TAG, "Error getting custom printer icon.", e);
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] getCustomPrinterIcon()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                    return null;
                }
            } catch (Throwable th) {
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] getCustomPrinterIcon()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                    throw th;
                }
            }
        }
    }

    public void clearCustomPrinterIconCache() {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            this.mClearCustomPrinterIconCache.clearCustomPrinterIconCache(getRemoteInstanceLazy());
            Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] clearCustomPrinterIconCache()");
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        } catch (RemoteException | InterruptedException | TimeoutException e) {
            try {
                Slog.e(LOG_TAG, "Error clearing custom printer icon cache.", e);
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] clearCustomPrinterIconCache()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                }
            } catch (Throwable th) {
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] clearCustomPrinterIconCache()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                    throw th;
                }
            }
        }
    }

    public final boolean setPrintJobTag(PrintJobId printJobId, String tag) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            boolean printJobTag = this.mSetPrintJobTagCaller.setPrintJobTag(getRemoteInstanceLazy(), printJobId, tag);
            Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] setPrintJobTag()");
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
            return printJobTag;
        } catch (RemoteException | InterruptedException | TimeoutException e) {
            try {
                Slog.e(LOG_TAG, "Error setting print job tag.", e);
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] setPrintJobTag()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                    return false;
                }
            } catch (Throwable th) {
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] setPrintJobTag()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                    throw th;
                }
            }
        }
    }

    public final void setPrintJobCancelling(PrintJobId printJobId, boolean cancelling) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            getRemoteInstanceLazy().setPrintJobCancelling(printJobId, cancelling);
            Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] setPrintJobCancelling()");
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        } catch (RemoteException | InterruptedException | TimeoutException e) {
            try {
                Slog.e(LOG_TAG, "Error setting print job cancelling.", e);
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] setPrintJobCancelling()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                }
            } catch (Throwable th) {
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] setPrintJobCancelling()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                    throw th;
                }
            }
        }
    }

    public final void getRemoteInstanceLazyFirstly() {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            getRemoteInstanceLazy();
            Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] pruneApprovedPrintServices()");
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        } catch (TimeoutException re) {
            Slog.e(LOG_TAG, "Can not get remote service.", re);
            Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] pruneApprovedPrintServices()");
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        } catch (InterruptedException e) {
            try {
                Slog.e(LOG_TAG, "Can not get remote service.", e);
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] pruneApprovedPrintServices()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                }
            } catch (Throwable th) {
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] pruneApprovedPrintServices()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                    throw th;
                }
            }
        }
    }

    public final void pruneApprovedPrintServices(List<ComponentName> servicesToKeep) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            getRemoteInstanceLazy().pruneApprovedPrintServices(servicesToKeep);
            Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] pruneApprovedPrintServices()");
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        } catch (RemoteException | InterruptedException | TimeoutException e) {
            try {
                Slog.e(LOG_TAG, "Error pruning approved print services.", e);
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] pruneApprovedPrintServices()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                }
            } catch (Throwable th) {
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] pruneApprovedPrintServices()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                    throw th;
                }
            }
        }
    }

    public final void removeObsoletePrintJobs() {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            getRemoteInstanceLazy().removeObsoletePrintJobs();
            Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] removeObsoletePrintJobs()");
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        } catch (RemoteException | InterruptedException | TimeoutException te) {
            try {
                Slog.e(LOG_TAG, "Error removing obsolete print jobs .", te);
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] removeObsoletePrintJobs()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                }
            } catch (Throwable th) {
                Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] removeObsoletePrintJobs()");
                synchronized (this.mLock) {
                    this.mCanUnbind = true;
                    this.mLock.notifyAll();
                    throw th;
                }
            }
        }
    }

    public final void destroy() {
        throwIfCalledOnMainThread();
        Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] destroy()");
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            unbindLocked();
            this.mDestroyed = true;
            this.mCanUnbind = false;
        }
    }

    public void dump(DualDumpOutputStream dumpStream) {
        synchronized (this.mLock) {
            dumpStream.write("is_destroyed", 1133871366145L, this.mDestroyed);
            dumpStream.write("is_bound", 1133871366146L, this.mRemoteInstance != null);
        }
        try {
            if (dumpStream.isProto()) {
                dumpStream.write(null, 1146756268035L, TransferPipe.dumpAsync(getRemoteInstanceLazy().asBinder(), new String[]{PriorityDump.PROTO_ARG}));
            } else {
                dumpStream.writeNested("internal_state", TransferPipe.dumpAsync(getRemoteInstanceLazy().asBinder(), new String[0]));
            }
        } catch (RemoteException | IOException | InterruptedException | TimeoutException e) {
            Slog.e(LOG_TAG, "Failed to dump remote instance", e);
        }
    }

    /* access modifiers changed from: private */
    public void onAllPrintJobsHandled() {
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            unbindLocked();
        }
    }

    /* access modifiers changed from: private */
    public void onPrintJobStateChanged(PrintJobInfo printJob) {
        this.mCallbacks.onPrintJobStateChanged(printJob);
    }

    private IPrintSpooler getRemoteInstanceLazy() throws TimeoutException, InterruptedException {
        synchronized (this.mLock) {
            if (this.mRemoteInstance != null) {
                IPrintSpooler iPrintSpooler = this.mRemoteInstance;
                return iPrintSpooler;
            }
            bindLocked();
            IPrintSpooler iPrintSpooler2 = this.mRemoteInstance;
            return iPrintSpooler2;
        }
    }

    @GuardedBy("mLock")
    private void bindLocked() throws TimeoutException, InterruptedException {
        int flags;
        while (this.mIsBinding) {
            this.mLock.wait();
        }
        if (this.mRemoteInstance == null) {
            this.mIsBinding = true;
            StringBuilder sb = new StringBuilder();
            sb.append("[user: ");
            sb.append(this.mUserHandle.getIdentifier());
            sb.append("] bindLocked() ");
            sb.append(this.mIsLowPriority ? "low priority" : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            Slog.i(LOG_TAG, sb.toString());
            try {
                if (this.mIsLowPriority) {
                    flags = 1;
                } else {
                    flags = 67108865;
                }
                this.mContext.bindServiceAsUser(this.mIntent, this.mServiceConnection, flags, this.mUserHandle);
                long startMillis = SystemClock.uptimeMillis();
                while (this.mRemoteInstance == null) {
                    long remainingMillis = BIND_SPOOLER_SERVICE_TIMEOUT - (SystemClock.uptimeMillis() - startMillis);
                    if (remainingMillis > 0) {
                        this.mLock.wait(remainingMillis);
                    } else {
                        throw new TimeoutException("Cannot get spooler!");
                    }
                }
                this.mCanUnbind = true;
            } finally {
                this.mIsBinding = false;
                this.mLock.notifyAll();
            }
        }
    }

    private void unbindLocked() {
        if (this.mRemoteInstance != null) {
            while (!this.mCanUnbind) {
                try {
                    this.mLock.wait();
                } catch (InterruptedException e) {
                }
            }
            Slog.i(LOG_TAG, "[user: " + this.mUserHandle.getIdentifier() + "] unbindLocked()");
            clearClientLocked();
            this.mRemoteInstance = null;
            this.mContext.unbindService(this.mServiceConnection);
        }
    }

    /* access modifiers changed from: private */
    public void setClientLocked() {
        if (this.mRemoteInstance == null) {
            Slog.e(LOG_TAG, "set Client fail,because mRemoteInstance is null.");
            return;
        }
        try {
            this.mRemoteInstance.setClient(this.mClient);
        } catch (RemoteException re) {
            Slog.d(LOG_TAG, "Error setting print spooler client", re);
        }
    }

    /* access modifiers changed from: private */
    public void clearClientLocked() {
        if (this.mRemoteInstance == null) {
            Slog.e(LOG_TAG, "clear Client fail,because mRemoteInstance is null.");
            return;
        }
        try {
            this.mRemoteInstance.setClient(null);
        } catch (RemoteException re) {
            Slog.d(LOG_TAG, "Error clearing print spooler client", re);
        }
    }

    private void throwIfDestroyedLocked() {
        if (this.mDestroyed) {
            throw new IllegalStateException("Cannot interact with a destroyed instance.");
        }
    }

    private void throwIfCalledOnMainThread() {
        if (Thread.currentThread() == this.mContext.getMainLooper().getThread()) {
            throw new RuntimeException("Cannot invoke on the main thread");
        }
    }
}
