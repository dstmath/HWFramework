package com.android.server.print;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Icon;
import android.os.Binder;
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
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeoutException;
import libcore.io.IoUtils;

final class RemotePrintSpooler {
    private static final long BIND_SPOOLER_SERVICE_TIMEOUT = 0;
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "RemotePrintSpooler";
    private final PrintSpoolerCallbacks mCallbacks;
    private boolean mCanUnbind;
    private final ClearCustomPrinterIconCacheCaller mClearCustomPrinterIconCache;
    private final PrintSpoolerClient mClient;
    private final Context mContext;
    private final OnCustomPrinterIconLoadedCaller mCustomPrinterIconLoadedCaller;
    private boolean mDestroyed;
    private final GetCustomPrinterIconCaller mGetCustomPrinterIconCaller;
    private final GetPrintJobInfoCaller mGetPrintJobInfoCaller;
    private final GetPrintJobInfosCaller mGetPrintJobInfosCaller;
    private final Intent mIntent;
    private boolean mIsLowPriority;
    private final Object mLock;
    private IPrintSpooler mRemoteInstance;
    private final ServiceConnection mServiceConnection;
    private final SetPrintJobStateCaller mSetPrintJobStatusCaller;
    private final SetPrintJobTagCaller mSetPrintJobTagCaller;
    private final UserHandle mUserHandle;

    private static abstract class BasePrintSpoolerServiceCallbacks extends Stub {
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
        private final IPrintSpoolerCallbacks mCallback;

        public ClearCustomPrinterIconCacheCaller() {
            super(5000);
            this.mCallback = new BasePrintSpoolerServiceCallbacks() {
                public void customPrinterIconCacheCleared(int sequence) {
                    ClearCustomPrinterIconCacheCaller.this.onRemoteMethodResult(null, sequence);
                }
            };
        }

        public Void clearCustomPrinterIconCache(IPrintSpooler target) throws RemoteException, TimeoutException {
            int sequence = onBeforeRemoteCall();
            target.clearCustomPrinterIconCache(this.mCallback, sequence);
            return (Void) getResultTimed(sequence);
        }
    }

    private static final class GetCustomPrinterIconCaller extends TimedRemoteCaller<Icon> {
        private final IPrintSpoolerCallbacks mCallback;

        public GetCustomPrinterIconCaller() {
            super(5000);
            this.mCallback = new BasePrintSpoolerServiceCallbacks() {
                public void onGetCustomPrinterIconResult(Icon icon, int sequence) {
                    GetCustomPrinterIconCaller.this.onRemoteMethodResult(icon, sequence);
                }
            };
        }

        public Icon getCustomPrinterIcon(IPrintSpooler target, PrinterId printerId) throws RemoteException, TimeoutException {
            int sequence = onBeforeRemoteCall();
            target.getCustomPrinterIcon(printerId, this.mCallback, sequence);
            return (Icon) getResultTimed(sequence);
        }
    }

    private static final class GetPrintJobInfoCaller extends TimedRemoteCaller<PrintJobInfo> {
        private final IPrintSpoolerCallbacks mCallback;

        public GetPrintJobInfoCaller() {
            super(5000);
            this.mCallback = new BasePrintSpoolerServiceCallbacks() {
                public void onGetPrintJobInfoResult(PrintJobInfo printJob, int sequence) {
                    GetPrintJobInfoCaller.this.onRemoteMethodResult(printJob, sequence);
                }
            };
        }

        public PrintJobInfo getPrintJobInfo(IPrintSpooler target, PrintJobId printJobId, int appId) throws RemoteException, TimeoutException {
            int sequence = onBeforeRemoteCall();
            target.getPrintJobInfo(printJobId, this.mCallback, appId, sequence);
            return (PrintJobInfo) getResultTimed(sequence);
        }
    }

    private static final class GetPrintJobInfosCaller extends TimedRemoteCaller<List<PrintJobInfo>> {
        private final IPrintSpoolerCallbacks mCallback;

        public GetPrintJobInfosCaller() {
            super(5000);
            this.mCallback = new BasePrintSpoolerServiceCallbacks() {
                public void onGetPrintJobInfosResult(List<PrintJobInfo> printJobs, int sequence) {
                    GetPrintJobInfosCaller.this.onRemoteMethodResult(printJobs, sequence);
                }
            };
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
        private final IPrintSpoolerCallbacks mCallback;

        public OnCustomPrinterIconLoadedCaller() {
            super(5000);
            this.mCallback = new BasePrintSpoolerServiceCallbacks() {
                public void onCustomPrinterIconCached(int sequence) {
                    OnCustomPrinterIconLoadedCaller.this.onRemoteMethodResult(null, sequence);
                }
            };
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
        private final IPrintSpoolerCallbacks mCallback;

        public SetPrintJobStateCaller() {
            super(5000);
            this.mCallback = new BasePrintSpoolerServiceCallbacks() {
                public void onSetPrintJobStateResult(boolean success, int sequence) {
                    SetPrintJobStateCaller.this.onRemoteMethodResult(Boolean.valueOf(success), sequence);
                }
            };
        }

        public boolean setPrintJobState(IPrintSpooler target, PrintJobId printJobId, int status, String error) throws RemoteException, TimeoutException {
            int sequence = onBeforeRemoteCall();
            target.setPrintJobState(printJobId, status, error, this.mCallback, sequence);
            return ((Boolean) getResultTimed(sequence)).booleanValue();
        }
    }

    private static final class SetPrintJobTagCaller extends TimedRemoteCaller<Boolean> {
        private final IPrintSpoolerCallbacks mCallback;

        public SetPrintJobTagCaller() {
            super(5000);
            this.mCallback = new BasePrintSpoolerServiceCallbacks() {
                public void onSetPrintJobTagResult(boolean success, int sequence) {
                    SetPrintJobTagCaller.this.onRemoteMethodResult(Boolean.valueOf(success), sequence);
                }
            };
        }

        public boolean setPrintJobTag(IPrintSpooler target, PrintJobId printJobId, String tag) throws RemoteException, TimeoutException {
            int sequence = onBeforeRemoteCall();
            target.setPrintJobTag(printJobId, tag, this.mCallback, sequence);
            return ((Boolean) getResultTimed(sequence)).booleanValue();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.print.RemotePrintSpooler.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.print.RemotePrintSpooler.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.print.RemotePrintSpooler.<clinit>():void");
    }

    public RemotePrintSpooler(Context context, int userId, boolean lowPriority, PrintSpoolerCallbacks callbacks) {
        this.mLock = new Object();
        this.mGetPrintJobInfosCaller = new GetPrintJobInfosCaller();
        this.mGetPrintJobInfoCaller = new GetPrintJobInfoCaller();
        this.mSetPrintJobStatusCaller = new SetPrintJobStateCaller();
        this.mSetPrintJobTagCaller = new SetPrintJobTagCaller();
        this.mCustomPrinterIconLoadedCaller = new OnCustomPrinterIconLoadedCaller();
        this.mClearCustomPrinterIconCache = new ClearCustomPrinterIconCacheCaller();
        this.mGetCustomPrinterIconCaller = new GetCustomPrinterIconCaller();
        this.mServiceConnection = new MyServiceConnection();
        this.mContext = context;
        this.mUserHandle = new UserHandle(userId);
        this.mCallbacks = callbacks;
        this.mIsLowPriority = lowPriority;
        this.mClient = new PrintSpoolerClient(this);
        this.mIntent = new Intent();
        this.mIntent.setComponent(new ComponentName("com.android.printspooler", "com.android.printspooler.model.PrintSpoolerService"));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void increasePriority() {
        if (this.mIsLowPriority) {
            this.mIsLowPriority = DEBUG;
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final List<PrintJobInfo> getPrintJobInfos(ComponentName componentName, int state, int appId) {
        Object obj;
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = DEBUG;
        }
        try {
            List<PrintJobInfo> printJobInfos = this.mGetPrintJobInfosCaller.getPrintJobInfos(getRemoteInstanceLazy(), componentName, state, appId);
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
            return printJobInfos;
        } catch (RemoteException re) {
            Slog.e(LOG_TAG, "Error getting print jobs.", re);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (TimeoutException te) {
            Slog.e(LOG_TAG, "Error getting print jobs.", te);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (Throwable th) {
            synchronized (this.mLock) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        }
    }

    public final void createPrintJob(PrintJobInfo printJob) {
        Object obj;
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = DEBUG;
        }
        try {
            getRemoteInstanceLazy().createPrintJob(printJob);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (RemoteException re) {
            Slog.e(LOG_TAG, "Error creating print job.", re);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (TimeoutException te) {
            Slog.e(LOG_TAG, "Error creating print job.", te);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (Throwable th) {
            synchronized (this.mLock) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        }
    }

    public final void writePrintJobData(ParcelFileDescriptor fd, PrintJobId printJobId) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = DEBUG;
        }
        Object obj;
        try {
            getRemoteInstanceLazy().writePrintJobData(fd, printJobId);
            IoUtils.closeQuietly(fd);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (RemoteException re) {
            Slog.e(LOG_TAG, "Error writing print job data.", re);
            IoUtils.closeQuietly(fd);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (TimeoutException te) {
            Slog.e(LOG_TAG, "Error writing print job data.", te);
            IoUtils.closeQuietly(fd);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (Throwable th) {
            IoUtils.closeQuietly(fd);
            synchronized (this.mLock) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final PrintJobInfo getPrintJobInfo(PrintJobId printJobId, int appId) {
        Object obj;
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = DEBUG;
        }
        try {
            PrintJobInfo printJobInfo = this.mGetPrintJobInfoCaller.getPrintJobInfo(getRemoteInstanceLazy(), printJobId, appId);
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
            return printJobInfo;
        } catch (RemoteException re) {
            Slog.e(LOG_TAG, "Error getting print job info.", re);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (TimeoutException te) {
            Slog.e(LOG_TAG, "Error getting print job info.", te);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (Throwable th) {
            synchronized (this.mLock) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final boolean setPrintJobState(PrintJobId printJobId, int state, String error) {
        Object obj;
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = DEBUG;
        }
        try {
            boolean printJobState = this.mSetPrintJobStatusCaller.setPrintJobState(getRemoteInstanceLazy(), printJobId, state, error);
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
            return printJobState;
        } catch (RemoteException re) {
            Slog.e(LOG_TAG, "Error setting print job state.", re);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (TimeoutException te) {
            Slog.e(LOG_TAG, "Error setting print job state.", te);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (Throwable th) {
            synchronized (this.mLock) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        }
    }

    public final void setProgress(PrintJobId printJobId, float progress) {
        Object obj;
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = DEBUG;
        }
        try {
            getRemoteInstanceLazy().setProgress(printJobId, progress);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (Exception re) {
            Slog.e(LOG_TAG, "Error setting progress.", re);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (Throwable th) {
            synchronized (this.mLock) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        }
    }

    public final void setStatus(PrintJobId printJobId, CharSequence status) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = DEBUG;
        }
        Object obj;
        try {
            getRemoteInstanceLazy().setStatus(printJobId, status);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (Exception re) {
            Slog.e(LOG_TAG, "Error setting status.", re);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (Throwable th) {
            synchronized (this.mLock) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        }
    }

    public final void setStatus(PrintJobId printJobId, int status, CharSequence appPackageName) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = DEBUG;
        }
        Object obj;
        try {
            getRemoteInstanceLazy().setStatusRes(printJobId, status, appPackageName);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (Exception re) {
            Slog.e(LOG_TAG, "Error setting status.", re);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (Throwable th) {
            synchronized (this.mLock) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        }
    }

    public final void onCustomPrinterIconLoaded(PrinterId printerId, Icon icon) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = DEBUG;
        }
        Object obj;
        try {
            this.mCustomPrinterIconLoadedCaller.onCustomPrinterIconLoaded(getRemoteInstanceLazy(), printerId, icon);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (Exception re) {
            Slog.e(LOG_TAG, "Error loading new custom printer icon.", re);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (Throwable th) {
            synchronized (this.mLock) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        }
    }

    public final Icon getCustomPrinterIcon(PrinterId printerId) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = DEBUG;
        }
        try {
            Icon customPrinterIcon = this.mGetCustomPrinterIconCaller.getCustomPrinterIcon(getRemoteInstanceLazy(), printerId);
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
            return customPrinterIcon;
        } catch (Exception re) {
            Slog.e(LOG_TAG, "Error getting custom printer icon.", re);
            synchronized (this.mLock) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
            return null;
        } catch (Throwable th) {
            synchronized (this.mLock) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        }
    }

    public void clearCustomPrinterIconCache() {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = DEBUG;
        }
        Object obj;
        try {
            this.mClearCustomPrinterIconCache.clearCustomPrinterIconCache(getRemoteInstanceLazy());
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (Exception re) {
            Slog.e(LOG_TAG, "Error clearing custom printer icon cache.", re);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (Throwable th) {
            synchronized (this.mLock) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final boolean setPrintJobTag(PrintJobId printJobId, String tag) {
        Object obj;
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = DEBUG;
        }
        try {
            boolean printJobTag = this.mSetPrintJobTagCaller.setPrintJobTag(getRemoteInstanceLazy(), printJobId, tag);
            synchronized (this.mLock) {
                this.mCanUnbind = true;
                this.mLock.notifyAll();
            }
            return printJobTag;
        } catch (RemoteException re) {
            Slog.e(LOG_TAG, "Error setting print job tag.", re);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (TimeoutException te) {
            Slog.e(LOG_TAG, "Error setting print job tag.", te);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (Throwable th) {
            synchronized (this.mLock) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        }
    }

    public final void setPrintJobCancelling(PrintJobId printJobId, boolean cancelling) {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = DEBUG;
        }
        Object obj;
        try {
            getRemoteInstanceLazy().setPrintJobCancelling(printJobId, cancelling);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (RemoteException re) {
            Slog.e(LOG_TAG, "Error setting print job cancelling.", re);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (TimeoutException te) {
            Slog.e(LOG_TAG, "Error setting print job cancelling.", te);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (Throwable th) {
            synchronized (this.mLock) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        }
    }

    public final void getRemoteInstanceLazyFirstly() {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = DEBUG;
        }
        Object obj;
        try {
            getRemoteInstanceLazy();
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (TimeoutException re) {
            Slog.e(LOG_TAG, "Can not get remote service.", re);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (Throwable th) {
            synchronized (this.mLock) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        }
    }

    public final void pruneApprovedPrintServices(List<ComponentName> servicesToKeep) {
        Object obj;
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = DEBUG;
        }
        try {
            getRemoteInstanceLazy().pruneApprovedPrintServices(servicesToKeep);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (Exception re) {
            Slog.e(LOG_TAG, "Error pruning approved print services.", re);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (Throwable th) {
            synchronized (this.mLock) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        }
    }

    public final void removeObsoletePrintJobs() {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            this.mCanUnbind = DEBUG;
        }
        Object obj;
        try {
            getRemoteInstanceLazy().removeObsoletePrintJobs();
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (RemoteException re) {
            Slog.e(LOG_TAG, "Error removing obsolete print jobs .", re);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (TimeoutException te) {
            Slog.e(LOG_TAG, "Error removing obsolete print jobs .", te);
            obj = this.mLock;
            synchronized (obj) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        } catch (Throwable th) {
            synchronized (this.mLock) {
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
        }
    }

    public final void destroy() {
        throwIfCalledOnMainThread();
        synchronized (this.mLock) {
            throwIfDestroyedLocked();
            unbindLocked();
            this.mDestroyed = true;
            this.mCanUnbind = DEBUG;
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String prefix) {
        synchronized (this.mLock) {
            pw.append(prefix).append("destroyed=").append(String.valueOf(this.mDestroyed)).println();
            pw.append(prefix).append("bound=").append(this.mRemoteInstance != null ? "true" : "false").println();
            pw.flush();
            try {
                getRemoteInstanceLazy().asBinder().dump(fd, new String[]{prefix});
            } catch (TimeoutException e) {
            } catch (RemoteException e2) {
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

    private IPrintSpooler getRemoteInstanceLazy() throws TimeoutException {
        synchronized (this.mLock) {
            if (this.mRemoteInstance != null) {
                IPrintSpooler iPrintSpooler = this.mRemoteInstance;
                return iPrintSpooler;
            }
            bindLocked();
            iPrintSpooler = this.mRemoteInstance;
            return iPrintSpooler;
        }
    }

    private void bindLocked() throws TimeoutException {
        if (this.mRemoteInstance == null) {
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
                if (remainingMillis <= BIND_SPOOLER_SERVICE_TIMEOUT) {
                    throw new TimeoutException("Cannot get spooler!");
                }
                try {
                    this.mLock.wait(remainingMillis);
                } catch (InterruptedException e) {
                }
            }
            this.mCanUnbind = true;
            this.mLock.notifyAll();
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
