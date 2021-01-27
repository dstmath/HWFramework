package android.print;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentSender;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.ICancellationSignal;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.print.IPrintDocumentAdapter;
import android.print.IPrintJobStateChangeListener;
import android.print.IPrintServicesChangeListener;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.printservice.PrintServiceInfo;
import android.printservice.recommendation.IRecommendationsChangeListener;
import android.printservice.recommendation.RecommendationInfo;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.Preconditions;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import libcore.io.IoUtils;

public final class PrintManager {
    public static final String ACTION_PRINT_DIALOG = "android.print.PRINT_DIALOG";
    public static final int ALL_SERVICES = 3;
    public static final int APP_ID_ANY = -2;
    private static final boolean DEBUG = false;
    public static final int DISABLED_SERVICES = 2;
    @SystemApi
    public static final int ENABLED_SERVICES = 1;
    public static final String EXTRA_PRINT_DIALOG_INTENT = "android.print.intent.extra.EXTRA_PRINT_DIALOG_INTENT";
    public static final String EXTRA_PRINT_DOCUMENT_ADAPTER = "android.print.intent.extra.EXTRA_PRINT_DOCUMENT_ADAPTER";
    public static final String EXTRA_PRINT_JOB = "android.print.intent.extra.EXTRA_PRINT_JOB";
    private static final String LOG_TAG = "PrintManager";
    private static final int MSG_NOTIFY_PRINT_JOB_STATE_CHANGED = 1;
    public static final String PRINT_SPOOLER_PACKAGE_NAME = "com.android.printspooler";
    private final int mAppId;
    private final Context mContext;
    private final Handler mHandler;
    private Map<PrintJobStateChangeListener, PrintJobStateChangeListenerWrapper> mPrintJobStateChangeListeners;
    private Map<PrintServiceRecommendationsChangeListener, PrintServiceRecommendationsChangeListenerWrapper> mPrintServiceRecommendationsChangeListeners;
    private Map<PrintServicesChangeListener, PrintServicesChangeListenerWrapper> mPrintServicesChangeListeners;
    private final IPrintManager mService;
    private final int mUserId;

    public interface PrintJobStateChangeListener {
        void onPrintJobStateChanged(PrintJobId printJobId);
    }

    @SystemApi
    public interface PrintServiceRecommendationsChangeListener {
        void onPrintServiceRecommendationsChanged();
    }

    @SystemApi
    public interface PrintServicesChangeListener {
        void onPrintServicesChanged();
    }

    public PrintManager(Context context, IPrintManager service, int userId, int appId) {
        this.mContext = context;
        this.mService = service;
        this.mUserId = userId;
        this.mAppId = appId;
        this.mHandler = new Handler(context.getMainLooper(), null, false) {
            /* class android.print.PrintManager.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message message) {
                if (message.what == 1) {
                    SomeArgs args = (SomeArgs) message.obj;
                    PrintJobStateChangeListener listener = ((PrintJobStateChangeListenerWrapper) args.arg1).getListener();
                    if (listener != null) {
                        listener.onPrintJobStateChanged((PrintJobId) args.arg2);
                    }
                    args.recycle();
                }
            }
        };
    }

    public PrintManager getGlobalPrintManagerForUser(int userId) {
        IPrintManager iPrintManager = this.mService;
        if (iPrintManager != null) {
            return new PrintManager(this.mContext, iPrintManager, userId, -2);
        }
        Log.w(LOG_TAG, "Feature android.software.print not available");
        return null;
    }

    /* access modifiers changed from: package-private */
    public PrintJobInfo getPrintJobInfo(PrintJobId printJobId) {
        try {
            return this.mService.getPrintJobInfo(printJobId, this.mAppId, this.mUserId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public void addPrintJobStateChangeListener(PrintJobStateChangeListener listener) {
        if (this.mService == null) {
            Log.w(LOG_TAG, "Feature android.software.print not available");
            return;
        }
        if (this.mPrintJobStateChangeListeners == null) {
            this.mPrintJobStateChangeListeners = new ArrayMap();
        }
        PrintJobStateChangeListenerWrapper wrappedListener = new PrintJobStateChangeListenerWrapper(listener, this.mHandler);
        try {
            this.mService.addPrintJobStateChangeListener(wrappedListener, this.mAppId, this.mUserId);
            this.mPrintJobStateChangeListeners.put(listener, wrappedListener);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public void removePrintJobStateChangeListener(PrintJobStateChangeListener listener) {
        PrintJobStateChangeListenerWrapper wrappedListener;
        if (this.mService == null) {
            Log.w(LOG_TAG, "Feature android.software.print not available");
            return;
        }
        Map<PrintJobStateChangeListener, PrintJobStateChangeListenerWrapper> map = this.mPrintJobStateChangeListeners;
        if (map != null && (wrappedListener = map.remove(listener)) != null) {
            if (this.mPrintJobStateChangeListeners.isEmpty()) {
                this.mPrintJobStateChangeListeners = null;
            }
            wrappedListener.destroy();
            try {
                this.mService.removePrintJobStateChangeListener(wrappedListener, this.mUserId);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    public PrintJob getPrintJob(PrintJobId printJobId) {
        IPrintManager iPrintManager = this.mService;
        if (iPrintManager == null) {
            Log.w(LOG_TAG, "Feature android.software.print not available");
            return null;
        }
        try {
            PrintJobInfo printJob = iPrintManager.getPrintJobInfo(printJobId, this.mAppId, this.mUserId);
            if (printJob != null) {
                return new PrintJob(printJob, this);
            }
            return null;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public Icon getCustomPrinterIcon(PrinterId printerId) {
        IPrintManager iPrintManager = this.mService;
        if (iPrintManager == null) {
            Log.w(LOG_TAG, "Feature android.software.print not available");
            return null;
        }
        try {
            return iPrintManager.getCustomPrinterIcon(printerId, this.mUserId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public List<PrintJob> getPrintJobs() {
        IPrintManager iPrintManager = this.mService;
        if (iPrintManager == null) {
            Log.w(LOG_TAG, "Feature android.software.print not available");
            return Collections.emptyList();
        }
        try {
            List<PrintJobInfo> printJobInfos = iPrintManager.getPrintJobInfos(this.mAppId, this.mUserId);
            if (printJobInfos == null) {
                return Collections.emptyList();
            }
            int printJobCount = printJobInfos.size();
            List<PrintJob> printJobs = new ArrayList<>(printJobCount);
            for (int i = 0; i < printJobCount; i++) {
                printJobs.add(new PrintJob(printJobInfos.get(i), this));
            }
            return printJobs;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: package-private */
    public void cancelPrintJob(PrintJobId printJobId) {
        IPrintManager iPrintManager = this.mService;
        if (iPrintManager == null) {
            Log.w(LOG_TAG, "Feature android.software.print not available");
            return;
        }
        try {
            iPrintManager.cancelPrintJob(printJobId, this.mAppId, this.mUserId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: package-private */
    public void restartPrintJob(PrintJobId printJobId) {
        IPrintManager iPrintManager = this.mService;
        if (iPrintManager == null) {
            Log.w(LOG_TAG, "Feature android.software.print not available");
            return;
        }
        try {
            iPrintManager.restartPrintJob(printJobId, this.mAppId, this.mUserId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public PrintJob print(String printJobName, PrintDocumentAdapter documentAdapter, PrintAttributes attributes) {
        if (this.mService == null) {
            Log.w(LOG_TAG, "Feature android.software.print not available");
            return null;
        } else if (!(this.mContext instanceof Activity)) {
            throw new IllegalStateException("Can print only from an activity");
        } else if (TextUtils.isEmpty(printJobName)) {
            throw new IllegalArgumentException("printJobName cannot be empty");
        } else if (documentAdapter != null) {
            try {
                Bundle result = this.mService.print(printJobName, new PrintDocumentAdapterDelegate((Activity) this.mContext, documentAdapter), attributes, this.mContext.getPackageName(), this.mAppId, this.mUserId);
                if (result != null) {
                    PrintJobInfo printJob = (PrintJobInfo) result.getParcelable(EXTRA_PRINT_JOB);
                    IntentSender intent = (IntentSender) result.getParcelable(EXTRA_PRINT_DIALOG_INTENT);
                    if (printJob == null || intent == null) {
                        return null;
                    }
                    try {
                        this.mContext.startIntentSender(intent, null, 0, 0, 0);
                        return new PrintJob(printJob, this);
                    } catch (IntentSender.SendIntentException sie) {
                        Log.e(LOG_TAG, "Couldn't start print job config activity.", sie);
                    }
                }
                return null;
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("documentAdapter cannot be null");
        }
    }

    @SystemApi
    public void addPrintServicesChangeListener(PrintServicesChangeListener listener, Handler handler) {
        Preconditions.checkNotNull(listener);
        if (handler == null) {
            handler = this.mHandler;
        }
        if (this.mService == null) {
            Log.w(LOG_TAG, "Feature android.software.print not available");
            return;
        }
        if (this.mPrintServicesChangeListeners == null) {
            this.mPrintServicesChangeListeners = new ArrayMap();
        }
        PrintServicesChangeListenerWrapper wrappedListener = new PrintServicesChangeListenerWrapper(listener, handler);
        try {
            this.mService.addPrintServicesChangeListener(wrappedListener, this.mUserId);
            this.mPrintServicesChangeListeners.put(listener, wrappedListener);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void removePrintServicesChangeListener(PrintServicesChangeListener listener) {
        PrintServicesChangeListenerWrapper wrappedListener;
        Preconditions.checkNotNull(listener);
        if (this.mService == null) {
            Log.w(LOG_TAG, "Feature android.software.print not available");
            return;
        }
        Map<PrintServicesChangeListener, PrintServicesChangeListenerWrapper> map = this.mPrintServicesChangeListeners;
        if (map != null && (wrappedListener = map.remove(listener)) != null) {
            if (this.mPrintServicesChangeListeners.isEmpty()) {
                this.mPrintServicesChangeListeners = null;
            }
            wrappedListener.destroy();
            try {
                this.mService.removePrintServicesChangeListener(wrappedListener, this.mUserId);
            } catch (RemoteException re) {
                Log.e(LOG_TAG, "Error removing print services change listener", re);
            }
        }
    }

    @SystemApi
    public List<PrintServiceInfo> getPrintServices(int selectionFlags) {
        Preconditions.checkFlagsArgument(selectionFlags, 3);
        try {
            List<PrintServiceInfo> services = this.mService.getPrintServices(selectionFlags, this.mUserId);
            if (services != null) {
                return services;
            }
            return Collections.emptyList();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void addPrintServiceRecommendationsChangeListener(PrintServiceRecommendationsChangeListener listener, Handler handler) {
        Preconditions.checkNotNull(listener);
        if (handler == null) {
            handler = this.mHandler;
        }
        if (this.mService == null) {
            Log.w(LOG_TAG, "Feature android.software.print not available");
            return;
        }
        if (this.mPrintServiceRecommendationsChangeListeners == null) {
            this.mPrintServiceRecommendationsChangeListeners = new ArrayMap();
        }
        PrintServiceRecommendationsChangeListenerWrapper wrappedListener = new PrintServiceRecommendationsChangeListenerWrapper(listener, handler);
        try {
            this.mService.addPrintServiceRecommendationsChangeListener(wrappedListener, this.mUserId);
            this.mPrintServiceRecommendationsChangeListeners.put(listener, wrappedListener);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void removePrintServiceRecommendationsChangeListener(PrintServiceRecommendationsChangeListener listener) {
        PrintServiceRecommendationsChangeListenerWrapper wrappedListener;
        Preconditions.checkNotNull(listener);
        if (this.mService == null) {
            Log.w(LOG_TAG, "Feature android.software.print not available");
            return;
        }
        Map<PrintServiceRecommendationsChangeListener, PrintServiceRecommendationsChangeListenerWrapper> map = this.mPrintServiceRecommendationsChangeListeners;
        if (map != null && (wrappedListener = map.remove(listener)) != null) {
            if (this.mPrintServiceRecommendationsChangeListeners.isEmpty()) {
                this.mPrintServiceRecommendationsChangeListeners = null;
            }
            wrappedListener.destroy();
            try {
                this.mService.removePrintServiceRecommendationsChangeListener(wrappedListener, this.mUserId);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    @SystemApi
    public List<RecommendationInfo> getPrintServiceRecommendations() {
        try {
            List<RecommendationInfo> recommendations = this.mService.getPrintServiceRecommendations(this.mUserId);
            if (recommendations != null) {
                return recommendations;
            }
            return Collections.emptyList();
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public PrinterDiscoverySession createPrinterDiscoverySession() {
        IPrintManager iPrintManager = this.mService;
        if (iPrintManager != null) {
            return new PrinterDiscoverySession(iPrintManager, this.mContext, this.mUserId);
        }
        Log.w(LOG_TAG, "Feature android.software.print not available");
        return null;
    }

    public void setPrintServiceEnabled(ComponentName service, boolean isEnabled) {
        IPrintManager iPrintManager = this.mService;
        if (iPrintManager == null) {
            Log.w(LOG_TAG, "Feature android.software.print not available");
            return;
        }
        try {
            iPrintManager.setPrintServiceEnabled(service, isEnabled, this.mUserId);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error enabling or disabling " + service, re);
        }
    }

    public static final class PrintDocumentAdapterDelegate extends IPrintDocumentAdapter.Stub implements Application.ActivityLifecycleCallbacks {
        private Activity mActivity;
        private PrintDocumentAdapter mDocumentAdapter;
        private Handler mHandler;
        private final Object mLock = new Object();
        private IPrintDocumentAdapterObserver mObserver;
        private DestroyableCallback mPendingCallback;

        /* access modifiers changed from: private */
        public interface DestroyableCallback {
            void destroy();
        }

        public PrintDocumentAdapterDelegate(Activity activity, PrintDocumentAdapter documentAdapter) {
            if (!activity.isFinishing()) {
                this.mActivity = activity;
                this.mDocumentAdapter = documentAdapter;
                this.mHandler = new MyHandler(this.mActivity.getMainLooper());
                this.mActivity.getApplication().registerActivityLifecycleCallbacks(this);
                return;
            }
            throw new IllegalStateException("Cannot start printing for finishing activity");
        }

        @Override // android.print.IPrintDocumentAdapter
        public void setObserver(IPrintDocumentAdapterObserver observer) {
            boolean destroyed;
            synchronized (this.mLock) {
                this.mObserver = observer;
                destroyed = isDestroyedLocked();
            }
            if (destroyed && observer != null) {
                try {
                    observer.onDestroy();
                } catch (RemoteException re) {
                    Log.e(PrintManager.LOG_TAG, "Error announcing destroyed state", re);
                }
            }
        }

        @Override // android.print.IPrintDocumentAdapter
        public void start() {
            synchronized (this.mLock) {
                if (!isDestroyedLocked()) {
                    this.mHandler.obtainMessage(1, this.mDocumentAdapter).sendToTarget();
                }
            }
        }

        @Override // android.print.IPrintDocumentAdapter
        public void layout(PrintAttributes oldAttributes, PrintAttributes newAttributes, ILayoutResultCallback callback, Bundle metadata, int sequence) {
            ICancellationSignal cancellationTransport = CancellationSignal.createTransport();
            try {
                callback.onLayoutStarted(cancellationTransport, sequence);
                synchronized (this.mLock) {
                    if (!isDestroyedLocked()) {
                        CancellationSignal cancellationSignal = CancellationSignal.fromTransport(cancellationTransport);
                        SomeArgs args = SomeArgs.obtain();
                        args.arg1 = this.mDocumentAdapter;
                        args.arg2 = oldAttributes;
                        args.arg3 = newAttributes;
                        args.arg4 = cancellationSignal;
                        args.arg5 = new MyLayoutResultCallback(callback, sequence);
                        args.arg6 = metadata;
                        this.mHandler.obtainMessage(2, args).sendToTarget();
                    }
                }
            } catch (RemoteException re) {
                Log.e(PrintManager.LOG_TAG, "Error notifying for layout start", re);
            }
        }

        @Override // android.print.IPrintDocumentAdapter
        public void write(PageRange[] pages, ParcelFileDescriptor fd, IWriteResultCallback callback, int sequence) {
            ICancellationSignal cancellationTransport = CancellationSignal.createTransport();
            try {
                callback.onWriteStarted(cancellationTransport, sequence);
                synchronized (this.mLock) {
                    if (!isDestroyedLocked()) {
                        CancellationSignal cancellationSignal = CancellationSignal.fromTransport(cancellationTransport);
                        SomeArgs args = SomeArgs.obtain();
                        args.arg1 = this.mDocumentAdapter;
                        args.arg2 = pages;
                        args.arg3 = fd;
                        args.arg4 = cancellationSignal;
                        args.arg5 = new MyWriteResultCallback(callback, fd, sequence);
                        this.mHandler.obtainMessage(3, args).sendToTarget();
                    }
                }
            } catch (RemoteException re) {
                Log.e(PrintManager.LOG_TAG, "Error notifying for write start", re);
            }
        }

        @Override // android.print.IPrintDocumentAdapter
        public void finish() {
            synchronized (this.mLock) {
                if (!isDestroyedLocked()) {
                    this.mHandler.obtainMessage(4, this.mDocumentAdapter).sendToTarget();
                }
            }
        }

        @Override // android.print.IPrintDocumentAdapter
        public void kill(String reason) {
            synchronized (this.mLock) {
                if (!isDestroyedLocked()) {
                    this.mHandler.obtainMessage(5, reason).sendToTarget();
                }
            }
        }

        @Override // android.app.Application.ActivityLifecycleCallbacks
        public void onActivityPaused(Activity activity) {
        }

        @Override // android.app.Application.ActivityLifecycleCallbacks
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override // android.app.Application.ActivityLifecycleCallbacks
        public void onActivityStarted(Activity activity) {
        }

        @Override // android.app.Application.ActivityLifecycleCallbacks
        public void onActivityResumed(Activity activity) {
        }

        @Override // android.app.Application.ActivityLifecycleCallbacks
        public void onActivityStopped(Activity activity) {
        }

        @Override // android.app.Application.ActivityLifecycleCallbacks
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override // android.app.Application.ActivityLifecycleCallbacks
        public void onActivityDestroyed(Activity activity) {
            IPrintDocumentAdapterObserver observer = null;
            synchronized (this.mLock) {
                if (activity == this.mActivity) {
                    observer = this.mObserver;
                    destroyLocked();
                }
            }
            if (observer != null) {
                try {
                    observer.onDestroy();
                } catch (RemoteException re) {
                    Log.e(PrintManager.LOG_TAG, "Error announcing destroyed state", re);
                }
            }
        }

        private boolean isDestroyedLocked() {
            return this.mActivity == null;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void destroyLocked() {
            this.mActivity.getApplication().unregisterActivityLifecycleCallbacks(this);
            this.mActivity = null;
            this.mDocumentAdapter = null;
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(2);
            this.mHandler.removeMessages(3);
            this.mHandler.removeMessages(4);
            this.mHandler = null;
            this.mObserver = null;
            DestroyableCallback destroyableCallback = this.mPendingCallback;
            if (destroyableCallback != null) {
                destroyableCallback.destroy();
                this.mPendingCallback = null;
            }
        }

        private final class MyHandler extends Handler {
            public static final int MSG_ON_FINISH = 4;
            public static final int MSG_ON_KILL = 5;
            public static final int MSG_ON_LAYOUT = 2;
            public static final int MSG_ON_START = 1;
            public static final int MSG_ON_WRITE = 3;

            public MyHandler(Looper looper) {
                super(looper, null, true);
            }

            @Override // android.os.Handler
            public void handleMessage(Message message) {
                int i = message.what;
                if (i == 1) {
                    ((PrintDocumentAdapter) message.obj).onStart();
                } else if (i == 2) {
                    SomeArgs args = (SomeArgs) message.obj;
                    args.recycle();
                    ((PrintDocumentAdapter) args.arg1).onLayout((PrintAttributes) args.arg2, (PrintAttributes) args.arg3, (CancellationSignal) args.arg4, (PrintDocumentAdapter.LayoutResultCallback) args.arg5, (Bundle) args.arg6);
                } else if (i == 3) {
                    SomeArgs args2 = (SomeArgs) message.obj;
                    args2.recycle();
                    ((PrintDocumentAdapter) args2.arg1).onWrite((PageRange[]) args2.arg2, (ParcelFileDescriptor) args2.arg3, (CancellationSignal) args2.arg4, (PrintDocumentAdapter.WriteResultCallback) args2.arg5);
                } else if (i == 4) {
                    ((PrintDocumentAdapter) message.obj).onFinish();
                    synchronized (PrintDocumentAdapterDelegate.this.mLock) {
                        PrintDocumentAdapterDelegate.this.destroyLocked();
                    }
                } else if (i != 5) {
                    throw new IllegalArgumentException("Unknown message: " + message.what);
                } else {
                    throw new RuntimeException((String) message.obj);
                }
            }
        }

        private final class MyLayoutResultCallback extends PrintDocumentAdapter.LayoutResultCallback implements DestroyableCallback {
            private ILayoutResultCallback mCallback;
            private final int mSequence;

            public MyLayoutResultCallback(ILayoutResultCallback callback, int sequence) {
                this.mCallback = callback;
                this.mSequence = sequence;
            }

            @Override // android.print.PrintDocumentAdapter.LayoutResultCallback
            public void onLayoutFinished(PrintDocumentInfo info, boolean changed) {
                ILayoutResultCallback callback;
                synchronized (PrintDocumentAdapterDelegate.this.mLock) {
                    callback = this.mCallback;
                }
                if (callback == null) {
                    Log.e(PrintManager.LOG_TAG, "PrintDocumentAdapter is destroyed. Did you finish the printing activity before print completion or did you invoke a callback after finish?");
                } else if (info != null) {
                    try {
                        callback.onLayoutFinished(info, changed, this.mSequence);
                    } catch (RemoteException re) {
                        Log.e(PrintManager.LOG_TAG, "Error calling onLayoutFinished", re);
                    } catch (Throwable th) {
                        destroy();
                        throw th;
                    }
                    destroy();
                } else {
                    throw new NullPointerException("document info cannot be null");
                }
            }

            @Override // android.print.PrintDocumentAdapter.LayoutResultCallback
            public void onLayoutFailed(CharSequence error) {
                ILayoutResultCallback callback;
                synchronized (PrintDocumentAdapterDelegate.this.mLock) {
                    callback = this.mCallback;
                }
                if (callback == null) {
                    Log.e(PrintManager.LOG_TAG, "PrintDocumentAdapter is destroyed. Did you finish the printing activity before print completion or did you invoke a callback after finish?");
                    return;
                }
                try {
                    callback.onLayoutFailed(error, this.mSequence);
                } catch (RemoteException re) {
                    Log.e(PrintManager.LOG_TAG, "Error calling onLayoutFailed", re);
                } catch (Throwable th) {
                    destroy();
                    throw th;
                }
                destroy();
            }

            @Override // android.print.PrintDocumentAdapter.LayoutResultCallback
            public void onLayoutCancelled() {
                ILayoutResultCallback callback;
                synchronized (PrintDocumentAdapterDelegate.this.mLock) {
                    callback = this.mCallback;
                }
                if (callback == null) {
                    Log.e(PrintManager.LOG_TAG, "PrintDocumentAdapter is destroyed. Did you finish the printing activity before print completion or did you invoke a callback after finish?");
                    return;
                }
                try {
                    callback.onLayoutCanceled(this.mSequence);
                } catch (RemoteException re) {
                    Log.e(PrintManager.LOG_TAG, "Error calling onLayoutFailed", re);
                } catch (Throwable th) {
                    destroy();
                    throw th;
                }
                destroy();
            }

            @Override // android.print.PrintManager.PrintDocumentAdapterDelegate.DestroyableCallback
            public void destroy() {
                synchronized (PrintDocumentAdapterDelegate.this.mLock) {
                    this.mCallback = null;
                    PrintDocumentAdapterDelegate.this.mPendingCallback = null;
                }
            }
        }

        private final class MyWriteResultCallback extends PrintDocumentAdapter.WriteResultCallback implements DestroyableCallback {
            private IWriteResultCallback mCallback;
            private ParcelFileDescriptor mFd;
            private final int mSequence;

            public MyWriteResultCallback(IWriteResultCallback callback, ParcelFileDescriptor fd, int sequence) {
                this.mFd = fd;
                this.mSequence = sequence;
                this.mCallback = callback;
            }

            @Override // android.print.PrintDocumentAdapter.WriteResultCallback
            public void onWriteFinished(PageRange[] pages) {
                IWriteResultCallback callback;
                synchronized (PrintDocumentAdapterDelegate.this.mLock) {
                    callback = this.mCallback;
                }
                if (callback == null) {
                    Log.e(PrintManager.LOG_TAG, "PrintDocumentAdapter is destroyed. Did you finish the printing activity before print completion or did you invoke a callback after finish?");
                } else if (pages != null) {
                    try {
                        if (pages.length != 0) {
                            try {
                                callback.onWriteFinished(pages, this.mSequence);
                            } catch (RemoteException re) {
                                Log.e(PrintManager.LOG_TAG, "Error calling onWriteFinished", re);
                            }
                            return;
                        }
                        throw new IllegalArgumentException("pages cannot be empty");
                    } finally {
                        destroy();
                    }
                } else {
                    throw new IllegalArgumentException("pages cannot be null");
                }
            }

            @Override // android.print.PrintDocumentAdapter.WriteResultCallback
            public void onWriteFailed(CharSequence error) {
                IWriteResultCallback callback;
                synchronized (PrintDocumentAdapterDelegate.this.mLock) {
                    callback = this.mCallback;
                }
                if (callback == null) {
                    Log.e(PrintManager.LOG_TAG, "PrintDocumentAdapter is destroyed. Did you finish the printing activity before print completion or did you invoke a callback after finish?");
                    return;
                }
                try {
                    callback.onWriteFailed(error, this.mSequence);
                } catch (RemoteException re) {
                    Log.e(PrintManager.LOG_TAG, "Error calling onWriteFailed", re);
                } catch (Throwable th) {
                    destroy();
                    throw th;
                }
                destroy();
            }

            @Override // android.print.PrintDocumentAdapter.WriteResultCallback
            public void onWriteCancelled() {
                IWriteResultCallback callback;
                synchronized (PrintDocumentAdapterDelegate.this.mLock) {
                    callback = this.mCallback;
                }
                if (callback == null) {
                    Log.e(PrintManager.LOG_TAG, "PrintDocumentAdapter is destroyed. Did you finish the printing activity before print completion or did you invoke a callback after finish?");
                    return;
                }
                try {
                    callback.onWriteCanceled(this.mSequence);
                } catch (RemoteException re) {
                    Log.e(PrintManager.LOG_TAG, "Error calling onWriteCanceled", re);
                } catch (Throwable th) {
                    destroy();
                    throw th;
                }
                destroy();
            }

            @Override // android.print.PrintManager.PrintDocumentAdapterDelegate.DestroyableCallback
            public void destroy() {
                synchronized (PrintDocumentAdapterDelegate.this.mLock) {
                    IoUtils.closeQuietly(this.mFd);
                    this.mCallback = null;
                    this.mFd = null;
                    PrintDocumentAdapterDelegate.this.mPendingCallback = null;
                }
            }
        }
    }

    public static final class PrintJobStateChangeListenerWrapper extends IPrintJobStateChangeListener.Stub {
        private final WeakReference<Handler> mWeakHandler;
        private final WeakReference<PrintJobStateChangeListener> mWeakListener;

        public PrintJobStateChangeListenerWrapper(PrintJobStateChangeListener listener, Handler handler) {
            this.mWeakListener = new WeakReference<>(listener);
            this.mWeakHandler = new WeakReference<>(handler);
        }

        @Override // android.print.IPrintJobStateChangeListener
        public void onPrintJobStateChanged(PrintJobId printJobId) {
            Handler handler = this.mWeakHandler.get();
            PrintJobStateChangeListener listener = this.mWeakListener.get();
            if (handler != null && listener != null) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = this;
                args.arg2 = printJobId;
                handler.obtainMessage(1, args).sendToTarget();
            }
        }

        public void destroy() {
            this.mWeakListener.clear();
        }

        public PrintJobStateChangeListener getListener() {
            return this.mWeakListener.get();
        }
    }

    public static final class PrintServicesChangeListenerWrapper extends IPrintServicesChangeListener.Stub {
        private final WeakReference<Handler> mWeakHandler;
        private final WeakReference<PrintServicesChangeListener> mWeakListener;

        public PrintServicesChangeListenerWrapper(PrintServicesChangeListener listener, Handler handler) {
            this.mWeakListener = new WeakReference<>(listener);
            this.mWeakHandler = new WeakReference<>(handler);
        }

        @Override // android.print.IPrintServicesChangeListener
        public void onPrintServicesChanged() {
            Handler handler = this.mWeakHandler.get();
            PrintServicesChangeListener listener = this.mWeakListener.get();
            if (handler != null && listener != null) {
                Objects.requireNonNull(listener);
                handler.post(new Runnable() {
                    /* class android.print.$$Lambda$c2Elb5E1w2yc6lr236iXRUAL5Q */

                    @Override // java.lang.Runnable
                    public final void run() {
                        PrintManager.PrintServicesChangeListener.this.onPrintServicesChanged();
                    }
                });
            }
        }

        public void destroy() {
            this.mWeakListener.clear();
        }
    }

    public static final class PrintServiceRecommendationsChangeListenerWrapper extends IRecommendationsChangeListener.Stub {
        private final WeakReference<Handler> mWeakHandler;
        private final WeakReference<PrintServiceRecommendationsChangeListener> mWeakListener;

        public PrintServiceRecommendationsChangeListenerWrapper(PrintServiceRecommendationsChangeListener listener, Handler handler) {
            this.mWeakListener = new WeakReference<>(listener);
            this.mWeakHandler = new WeakReference<>(handler);
        }

        @Override // android.printservice.recommendation.IRecommendationsChangeListener
        public void onRecommendationsChanged() {
            Handler handler = this.mWeakHandler.get();
            PrintServiceRecommendationsChangeListener listener = this.mWeakListener.get();
            if (handler != null && listener != null) {
                Objects.requireNonNull(listener);
                handler.post(new Runnable() {
                    /* class android.print.$$Lambda$KZ41E_yXUNYMY9k_Xeus1UG_cS8 */

                    @Override // java.lang.Runnable
                    public final void run() {
                        PrintManager.PrintServiceRecommendationsChangeListener.this.onPrintServiceRecommendationsChanged();
                    }
                });
            }
        }

        public void destroy() {
            this.mWeakListener.clear();
        }
    }
}
