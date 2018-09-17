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
import android.print.IPrintSpoolerCallbacks.Stub;
import android.print.IPrintSpoolerClient;
import android.print.PrintJobId;
import android.print.PrintJobInfo;
import android.print.PrinterId;
import android.util.Slog;
import android.util.TimedRemoteCaller;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.TransferPipe;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeoutException;
import libcore.io.IoUtils;

final class RemotePrintSpooler {
    private static final long BIND_SPOOLER_SERVICE_TIMEOUT = ((long) ("eng".equals(Build.TYPE) ? 120000 : 10000));
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "RemotePrintSpooler";
    private final PrintSpoolerCallbacks mCallbacks;
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
    private final Object mLock = new Object();
    private IPrintSpooler mRemoteInstance;
    private final ServiceConnection mServiceConnection = new MyServiceConnection(this, null);
    private final SetPrintJobStateCaller mSetPrintJobStatusCaller = new SetPrintJobStateCaller();
    private final SetPrintJobTagCaller mSetPrintJobTagCaller = new SetPrintJobTagCaller();
    private final UserHandle mUserHandle;

    private static abstract class BasePrintSpoolerServiceCallbacks extends Stub {
        /* synthetic */ BasePrintSpoolerServiceCallbacks(BasePrintSpoolerServiceCallbacks -this0) {
            this();
        }

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
        /* synthetic */ MyServiceConnection(RemotePrintSpooler this$0, MyServiceConnection -this1) {
            this();
        }

        private MyServiceConnection() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (RemotePrintSpooler.this.mLock) {
                RemotePrintSpooler.this.mRemoteInstance = IPrintSpooler.Stub.asInterface(service);
                RemotePrintSpooler.this.setClientLocked();
                RemotePrintSpooler.this.mLock.notifyAll();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            synchronized (RemotePrintSpooler.this.mLock) {
                RemotePrintSpooler.this.clearClientLocked();
                RemotePrintSpooler.this.mRemoteInstance = null;
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
            this.mWeakSpooler = new WeakReference(spooler);
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
                unbindLocked();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x002a A:{Splitter: B:5:0x000d, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x002a A:{Splitter: B:5:0x000d, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:20:0x002a, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            android.util.Slog.e(LOG_TAG, "Error getting print jobs.", r0);
     */
    /* JADX WARNING: Missing block: B:24:0x0036, code:
            monitor-enter(r4.mLock);
     */
    /* JADX WARNING: Missing block: B:27:?, code:
            r4.mCanUnbind = true;
            r4.mLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:30:0x0041, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final List<PrintJobInfo> getPrintJobInfos(ComponentName componentName, int state, int appId) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            List<PrintJobInfo> printJobInfos = this.mGetPrintJobInfosCaller.getPrintJobInfos(getRemoteInstanceLazy(), componentName, state, appId);
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
            return printJobInfos;
        } catch (Exception e) {
        } catch (Throwable th) {
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0027 A:{Splitter: B:5:0x000d, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0027 A:{Splitter: B:5:0x000d, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:20:0x0027, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            android.util.Slog.e(LOG_TAG, "Error creating print job.", r0);
     */
    /* JADX WARNING: Missing block: B:23:0x0031, code:
            r2 = r4.mLock;
     */
    /* JADX WARNING: Missing block: B:24:0x0033, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:27:?, code:
            r4.mCanUnbind = true;
            r4.mLock.notifyAll();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void createPrintJob(PrintJobInfo printJob) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            getRemoteInstanceLazy().createPrintJob(printJob);
            Object obj = this.mLock;
            synchronized (obj) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        } catch (Exception e) {
        } catch (Throwable th) {
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x002a A:{Splitter: B:5:0x000d, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x002a A:{Splitter: B:5:0x000d, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:20:0x002a, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            android.util.Slog.e(LOG_TAG, "Error writing print job data.", r0);
     */
    /* JADX WARNING: Missing block: B:23:0x0034, code:
            libcore.io.IoUtils.closeQuietly(r5);
            r2 = r4.mLock;
     */
    /* JADX WARNING: Missing block: B:24:0x0039, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:27:?, code:
            r4.mCanUnbind = true;
            r4.mLock.notifyAll();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void writePrintJobData(ParcelFileDescriptor fd, PrintJobId printJobId) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            getRemoteInstanceLazy().writePrintJobData(fd, printJobId);
            IoUtils.closeQuietly(fd);
            Object obj = this.mLock;
            synchronized (obj) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        } catch (Exception e) {
        } catch (Throwable th) {
            IoUtils.closeQuietly(fd);
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x002a A:{Splitter: B:5:0x000d, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x002a A:{Splitter: B:5:0x000d, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:20:0x002a, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            android.util.Slog.e(LOG_TAG, "Error getting print job info.", r0);
     */
    /* JADX WARNING: Missing block: B:24:0x0036, code:
            monitor-enter(r4.mLock);
     */
    /* JADX WARNING: Missing block: B:27:?, code:
            r4.mCanUnbind = true;
            r4.mLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:30:0x0041, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final PrintJobInfo getPrintJobInfo(PrintJobId printJobId, int appId) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            PrintJobInfo printJobInfo = this.mGetPrintJobInfoCaller.getPrintJobInfo(getRemoteInstanceLazy(), printJobId, appId);
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
            return printJobInfo;
        } catch (Exception e) {
        } catch (Throwable th) {
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x002b A:{Splitter: B:5:0x000e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x002b A:{Splitter: B:5:0x000e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:20:0x002b, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            android.util.Slog.e(LOG_TAG, "Error setting print job state.", r0);
     */
    /* JADX WARNING: Missing block: B:24:0x0037, code:
            monitor-enter(r4.mLock);
     */
    /* JADX WARNING: Missing block: B:27:?, code:
            r4.mCanUnbind = true;
            r4.mLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:29:0x0041, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final boolean setPrintJobState(PrintJobId printJobId, int state, String error) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            boolean printJobState = this.mSetPrintJobStatusCaller.setPrintJobState(getRemoteInstanceLazy(), printJobId, state, error);
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
            return printJobState;
        } catch (Exception e) {
        } catch (Throwable th) {
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0027 A:{Splitter: B:5:0x000d, ExcHandler: android.os.RemoteException (r0_0 're' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0027 A:{Splitter: B:5:0x000d, ExcHandler: android.os.RemoteException (r0_0 're' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:20:0x0027, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            android.util.Slog.e(LOG_TAG, "Error setting progress.", r0);
     */
    /* JADX WARNING: Missing block: B:23:0x0031, code:
            r2 = r4.mLock;
     */
    /* JADX WARNING: Missing block: B:24:0x0033, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:27:?, code:
            r4.mCanUnbind = true;
            r4.mLock.notifyAll();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void setProgress(PrintJobId printJobId, float progress) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            getRemoteInstanceLazy().setProgress(printJobId, progress);
            Object obj = this.mLock;
            synchronized (obj) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        } catch (Exception re) {
        } catch (Throwable th) {
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0027 A:{Splitter: B:5:0x000d, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0027 A:{Splitter: B:5:0x000d, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:20:0x0027, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            android.util.Slog.e(LOG_TAG, "Error setting status.", r0);
     */
    /* JADX WARNING: Missing block: B:23:0x0031, code:
            r2 = r4.mLock;
     */
    /* JADX WARNING: Missing block: B:24:0x0033, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:27:?, code:
            r4.mCanUnbind = true;
            r4.mLock.notifyAll();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void setStatus(PrintJobId printJobId, CharSequence status) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            getRemoteInstanceLazy().setStatus(printJobId, status);
            Object obj = this.mLock;
            synchronized (obj) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        } catch (Exception e) {
        } catch (Throwable th) {
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0027 A:{Splitter: B:5:0x000d, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0027 A:{Splitter: B:5:0x000d, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:20:0x0027, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            android.util.Slog.e(LOG_TAG, "Error setting status.", r0);
     */
    /* JADX WARNING: Missing block: B:23:0x0031, code:
            r2 = r4.mLock;
     */
    /* JADX WARNING: Missing block: B:24:0x0033, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:27:?, code:
            r4.mCanUnbind = true;
            r4.mLock.notifyAll();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void setStatus(PrintJobId printJobId, int status, CharSequence appPackageName) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            getRemoteInstanceLazy().setStatusRes(printJobId, status, appPackageName);
            Object obj = this.mLock;
            synchronized (obj) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        } catch (Exception e) {
        } catch (Throwable th) {
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0029 A:{Splitter: B:5:0x000d, ExcHandler: android.os.RemoteException (r0_0 're' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0029 A:{Splitter: B:5:0x000d, ExcHandler: android.os.RemoteException (r0_0 're' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:20:0x0029, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            android.util.Slog.e(LOG_TAG, "Error loading new custom printer icon.", r0);
     */
    /* JADX WARNING: Missing block: B:23:0x0033, code:
            r2 = r4.mLock;
     */
    /* JADX WARNING: Missing block: B:24:0x0035, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:27:?, code:
            r4.mCanUnbind = true;
            r4.mLock.notifyAll();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void onCustomPrinterIconLoaded(PrinterId printerId, Icon icon) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            this.mCustomPrinterIconLoadedCaller.onCustomPrinterIconLoaded(getRemoteInstanceLazy(), printerId, icon);
            Object obj = this.mLock;
            synchronized (obj) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        } catch (Exception re) {
        } catch (Throwable th) {
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x002a A:{Splitter: B:5:0x000d, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x002a A:{Splitter: B:5:0x000d, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:20:0x002a, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            android.util.Slog.e(LOG_TAG, "Error getting custom printer icon.", r0);
     */
    /* JADX WARNING: Missing block: B:24:0x0037, code:
            monitor-enter(r4.mLock);
     */
    /* JADX WARNING: Missing block: B:27:?, code:
            r4.mCanUnbind = true;
            r4.mLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:29:0x0041, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final Icon getCustomPrinterIcon(PrinterId printerId) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            Icon customPrinterIcon = this.mGetCustomPrinterIconCaller.getCustomPrinterIcon(getRemoteInstanceLazy(), printerId);
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
            return customPrinterIcon;
        } catch (Exception e) {
        } catch (Throwable th) {
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0029 A:{Splitter: B:5:0x000d, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0029 A:{Splitter: B:5:0x000d, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:20:0x0029, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            android.util.Slog.e(LOG_TAG, "Error clearing custom printer icon cache.", r0);
     */
    /* JADX WARNING: Missing block: B:23:0x0033, code:
            r2 = r4.mLock;
     */
    /* JADX WARNING: Missing block: B:24:0x0035, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:27:?, code:
            r4.mCanUnbind = true;
            r4.mLock.notifyAll();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void clearCustomPrinterIconCache() {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            this.mClearCustomPrinterIconCache.clearCustomPrinterIconCache(getRemoteInstanceLazy());
            Object obj = this.mLock;
            synchronized (obj) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        } catch (Exception e) {
        } catch (Throwable th) {
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x002b A:{Splitter: B:5:0x000e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x002b A:{Splitter: B:5:0x000e, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:20:0x002b, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            android.util.Slog.e(LOG_TAG, "Error setting print job tag.", r0);
     */
    /* JADX WARNING: Missing block: B:24:0x0037, code:
            monitor-enter(r4.mLock);
     */
    /* JADX WARNING: Missing block: B:27:?, code:
            r4.mCanUnbind = true;
            r4.mLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:29:0x0041, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final boolean setPrintJobTag(PrintJobId printJobId, String tag) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            boolean printJobTag = this.mSetPrintJobTagCaller.setPrintJobTag(getRemoteInstanceLazy(), printJobId, tag);
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
            return printJobTag;
        } catch (Exception e) {
        } catch (Throwable th) {
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0027 A:{Splitter: B:5:0x000d, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0027 A:{Splitter: B:5:0x000d, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:20:0x0027, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            android.util.Slog.e(LOG_TAG, "Error setting print job cancelling.", r0);
     */
    /* JADX WARNING: Missing block: B:23:0x0031, code:
            r2 = r4.mLock;
     */
    /* JADX WARNING: Missing block: B:24:0x0033, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:27:?, code:
            r4.mCanUnbind = true;
            r4.mLock.notifyAll();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void setPrintJobCancelling(PrintJobId printJobId, boolean cancelling) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            getRemoteInstanceLazy().setPrintJobCancelling(printJobId, cancelling);
            Object obj = this.mLock;
            synchronized (obj) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        } catch (Exception e) {
        } catch (Throwable th) {
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        }
    }

    public final void getRemoteInstanceLazyFirstly() {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        Object obj;
        try {
            getRemoteInstanceLazy();
            obj = this.mLock;
            synchronized (obj) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
                return;
            }
        } catch (TimeoutException re) {
            Slog.e(LOG_TAG, "Can not get remote service.", re);
            obj = this.mLock;
            synchronized (obj) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        } catch (InterruptedException e) {
            Slog.e(LOG_TAG, "Can not get remote service.", e);
            obj = this.mLock;
            synchronized (obj) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        } catch (Throwable th) {
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0027 A:{Splitter: B:5:0x000d, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0027 A:{Splitter: B:5:0x000d, ExcHandler: android.os.RemoteException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:20:0x0027, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            android.util.Slog.e(LOG_TAG, "Error pruning approved print services.", r0);
     */
    /* JADX WARNING: Missing block: B:23:0x0031, code:
            r2 = r4.mLock;
     */
    /* JADX WARNING: Missing block: B:24:0x0033, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:27:?, code:
            r4.mCanUnbind = true;
            r4.mLock.notifyAll();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void pruneApprovedPrintServices(List<ComponentName> servicesToKeep) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            getRemoteInstanceLazy().pruneApprovedPrintServices(servicesToKeep);
            Object obj = this.mLock;
            synchronized (obj) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        } catch (Exception e) {
        } catch (Throwable th) {
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0027 A:{Splitter: B:5:0x000d, ExcHandler: android.os.RemoteException (r0_0 'te' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0027 A:{Splitter: B:5:0x000d, ExcHandler: android.os.RemoteException (r0_0 'te' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:20:0x0027, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:22:?, code:
            android.util.Slog.e(LOG_TAG, "Error removing obsolete print jobs .", r0);
     */
    /* JADX WARNING: Missing block: B:23:0x0031, code:
            r2 = r4.mLock;
     */
    /* JADX WARNING: Missing block: B:24:0x0033, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:27:?, code:
            r4.mCanUnbind = true;
            r4.mLock.notifyAll();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void removeObsoletePrintJobs() {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = false;
        }
        try {
            getRemoteInstanceLazy().removeObsoletePrintJobs();
            Object obj = this.mLock;
            synchronized (obj) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        } catch (Exception te) {
        } catch (Throwable th) {
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
        }
    }

    public final void destroy() {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            unbindLocked();
            this.mDestroyed = true;
            this.mCanUnbind = false;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x004e A:{Splitter: B:7:0x0037, ExcHandler: java.io.IOException (r0_0 'e' java.lang.Exception), Catch:{ IOException -> 0x004e, IOException -> 0x004e, IOException -> 0x004e, IOException -> 0x004e }} */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x004e A:{Splitter: B:7:0x0037, ExcHandler: java.io.IOException (r0_0 'e' java.lang.Exception), Catch:{ IOException -> 0x004e, IOException -> 0x004e, IOException -> 0x004e, IOException -> 0x004e }} */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x004e A:{Splitter: B:7:0x0037, ExcHandler: java.io.IOException (r0_0 'e' java.lang.Exception), Catch:{ IOException -> 0x004e, IOException -> 0x004e, IOException -> 0x004e, IOException -> 0x004e }} */
    /* JADX WARNING: Missing block: B:13:0x004e, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:14:0x004f, code:
            r7.println("Failed to dump remote instance: " + r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void dump(FileDescriptor fd, PrintWriter pw, String prefix) {
        synchronized (this.mLock) {
            pw.append(prefix).append("destroyed=").append(String.valueOf(this.mDestroyed)).println();
            pw.append(prefix).append("bound=").append(this.mRemoteInstance != null ? "true" : "false").println();
            pw.flush();
            try {
                TransferPipe.dumpAsync(getRemoteInstanceLazy().asBinder(), fd, new String[]{prefix});
            } catch (Exception e) {
            }
        }
    }

    private void onAllPrintJobsHandled() {
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            unbindLocked();
        }
    }

    private void onPrintJobStateChanged(PrintJobInfo printJob) {
        this.mCallbacks.onPrintJobStateChanged(printJob);
    }

    private IPrintSpooler getRemoteInstanceLazy() throws TimeoutException, InterruptedException {
        synchronized (this.mLock) {
            IPrintSpooler iPrintSpooler;
            if (this.mRemoteInstance != null) {
                iPrintSpooler = this.mRemoteInstance;
                return iPrintSpooler;
            }
            bindLocked();
            iPrintSpooler = this.mRemoteInstance;
            return iPrintSpooler;
        }
    }

    private void bindLocked() throws TimeoutException, InterruptedException {
        while (this.mIsBinding) {
            this.mLock.wait();
        }
        if (this.mRemoteInstance == null) {
            this.mIsBinding = true;
            try {
                int flags;
                if (this.mIsLowPriority) {
                    flags = 1;
                } else {
                    flags = 67108865;
                }
                this.mContext.bindServiceAsUser(this.mIntent, this.mServiceConnection, flags, this.mUserHandle);
                long startMillis = SystemClock.uptimeMillis();
                while (this.mRemoteInstance == null) {
                    long remainingMillis = BIND_SPOOLER_SERVICE_TIMEOUT - (SystemClock.uptimeMillis() - startMillis);
                    if (remainingMillis <= 0) {
                        throw new TimeoutException("Cannot get spooler!");
                    }
                    this.mLock.wait(remainingMillis);
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
            clearClientLocked();
            this.mRemoteInstance = null;
            this.mContext.unbindService(this.mServiceConnection);
        }
    }

    private void setClientLocked() {
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

    private void clearClientLocked() {
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
