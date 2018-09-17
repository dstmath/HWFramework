package android.print;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.ICancellationSignal;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.print.IPrintDocumentAdapter.Stub;
import android.print.PrintDocumentAdapter.LayoutResultCallback;
import android.print.PrintDocumentAdapter.WriteResultCallback;
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
import libcore.io.IoUtils;

public final class PrintManager {
    public static final String ACTION_PRINT_DIALOG = "android.print.PRINT_DIALOG";
    public static final int ALL_SERVICES = 3;
    public static final int APP_ID_ANY = -2;
    private static final boolean DEBUG = false;
    public static final int DISABLED_SERVICES = 2;
    public static final int ENABLED_SERVICES = 1;
    public static final String EXTRA_PRINT_DIALOG_INTENT = "android.print.intent.extra.EXTRA_PRINT_DIALOG_INTENT";
    public static final String EXTRA_PRINT_DOCUMENT_ADAPTER = "android.print.intent.extra.EXTRA_PRINT_DOCUMENT_ADAPTER";
    public static final String EXTRA_PRINT_JOB = "android.print.intent.extra.EXTRA_PRINT_JOB";
    private static final String LOG_TAG = "PrintManager";
    private static final int MSG_NOTIFY_PRINT_JOB_STATE_CHANGED = 1;
    private static final int MSG_NOTIFY_PRINT_SERVICES_CHANGED = 2;
    private static final int MSG_NOTIFY_PRINT_SERVICE_RECOMMENDATIONS_CHANGED = 3;
    public static final String PRINT_SPOOLER_PACKAGE_NAME = "com.android.printspooler";
    private final int mAppId;
    private final Context mContext;
    private final Handler mHandler;
    private Map<PrintJobStateChangeListener, PrintJobStateChangeListenerWrapper> mPrintJobStateChangeListeners;
    private Map<PrintServiceRecommendationsChangeListener, PrintServiceRecommendationsChangeListenerWrapper> mPrintServiceRecommendationsChangeListeners;
    private Map<PrintServicesChangeListener, PrintServicesChangeListenerWrapper> mPrintServicesChangeListeners;
    private final IPrintManager mService;
    private final int mUserId;

    /* renamed from: android.print.PrintManager.1 */
    class AnonymousClass1 extends Handler {
        AnonymousClass1(Looper $anonymous0, Callback $anonymous1, boolean $anonymous2) {
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case PrintManager.MSG_NOTIFY_PRINT_JOB_STATE_CHANGED /*1*/:
                    SomeArgs args = message.obj;
                    PrintJobStateChangeListener listener = args.arg1.getListener();
                    if (listener != null) {
                        listener.onPrintJobStateChanged(args.arg2);
                    }
                    args.recycle();
                case PrintManager.MSG_NOTIFY_PRINT_SERVICES_CHANGED /*2*/:
                    PrintServicesChangeListener listener2 = message.obj.getListener();
                    if (listener2 != null) {
                        listener2.onPrintServicesChanged();
                    }
                case PrintManager.MSG_NOTIFY_PRINT_SERVICE_RECOMMENDATIONS_CHANGED /*3*/:
                    PrintServiceRecommendationsChangeListener listener3 = message.obj.getListener();
                    if (listener3 != null) {
                        listener3.onPrintServiceRecommendationsChanged();
                    }
                default:
            }
        }
    }

    public static final class PrintDocumentAdapterDelegate extends Stub implements ActivityLifecycleCallbacks {
        private Activity mActivity;
        private PrintDocumentAdapter mDocumentAdapter;
        private Handler mHandler;
        private final Object mLock;
        private IPrintDocumentAdapterObserver mObserver;
        private DestroyableCallback mPendingCallback;

        private interface DestroyableCallback {
            void destroy();
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

            public void handleMessage(Message message) {
                SomeArgs args;
                PrintDocumentAdapter adapter;
                CancellationSignal cancellation;
                switch (message.what) {
                    case MSG_ON_START /*1*/:
                        ((PrintDocumentAdapter) message.obj).onStart();
                    case MSG_ON_LAYOUT /*2*/:
                        args = message.obj;
                        adapter = args.arg1;
                        PrintAttributes oldAttributes = args.arg2;
                        PrintAttributes newAttributes = args.arg3;
                        cancellation = args.arg4;
                        LayoutResultCallback callback = args.arg5;
                        Bundle metadata = args.arg6;
                        args.recycle();
                        adapter.onLayout(oldAttributes, newAttributes, cancellation, callback, metadata);
                    case MSG_ON_WRITE /*3*/:
                        args = (SomeArgs) message.obj;
                        adapter = (PrintDocumentAdapter) args.arg1;
                        PageRange[] pages = args.arg2;
                        ParcelFileDescriptor fd = args.arg3;
                        cancellation = (CancellationSignal) args.arg4;
                        WriteResultCallback callback2 = args.arg5;
                        args.recycle();
                        adapter.onWrite(pages, fd, cancellation, callback2);
                    case MSG_ON_FINISH /*4*/:
                        ((PrintDocumentAdapter) message.obj).onFinish();
                        synchronized (PrintDocumentAdapterDelegate.this.mLock) {
                            PrintDocumentAdapterDelegate.this.destroyLocked();
                            break;
                        }
                    case MSG_ON_KILL /*5*/:
                        throw new RuntimeException(message.obj);
                    default:
                        throw new IllegalArgumentException("Unknown message: " + message.what);
                }
            }
        }

        private final class MyLayoutResultCallback extends LayoutResultCallback implements DestroyableCallback {
            private ILayoutResultCallback mCallback;
            private final int mSequence;

            public MyLayoutResultCallback(ILayoutResultCallback callback, int sequence) {
                this.mCallback = callback;
                this.mSequence = sequence;
            }

            public void onLayoutFinished(android.print.PrintDocumentInfo r5, boolean r6) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:android.print.PrintManager.PrintDocumentAdapterDelegate.MyLayoutResultCallback.onLayoutFinished(android.print.PrintDocumentInfo, boolean):void. bs: [B:12:0x001b, B:18:0x0029]
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:86)
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:57)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:52)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r4 = this;
                r2 = android.print.PrintManager.PrintDocumentAdapterDelegate.this;
                r2 = r2.mLock;
                monitor-enter(r2);
                r0 = r4.mCallback;	 Catch:{ all -> 0x0016 }
                monitor-exit(r2);
                if (r0 != 0) goto L_0x0019;
            L_0x000c:
                r2 = "PrintManager";
                r3 = "PrintDocumentAdapter is destroyed. Did you finish the printing activity before print completion or did you invoke a callback after finish?";
                android.util.Log.e(r2, r3);
                return;
            L_0x0016:
                r3 = move-exception;
                monitor-exit(r2);
                throw r3;
            L_0x0019:
                if (r5 != 0) goto L_0x0029;
            L_0x001b:
                r2 = new java.lang.NullPointerException;	 Catch:{ all -> 0x0024 }
                r3 = "document info cannot be null";	 Catch:{ all -> 0x0024 }
                r2.<init>(r3);	 Catch:{ all -> 0x0024 }
                throw r2;	 Catch:{ all -> 0x0024 }
            L_0x0024:
                r2 = move-exception;
                r4.destroy();
                throw r2;
            L_0x0029:
                r2 = r4.mSequence;	 Catch:{ RemoteException -> 0x0032 }
                r0.onLayoutFinished(r5, r6, r2);	 Catch:{ RemoteException -> 0x0032 }
            L_0x002e:
                r4.destroy();
                return;
            L_0x0032:
                r1 = move-exception;
                r2 = "PrintManager";	 Catch:{ all -> 0x0024 }
                r3 = "Error calling onLayoutFinished";	 Catch:{ all -> 0x0024 }
                android.util.Log.e(r2, r3, r1);	 Catch:{ all -> 0x0024 }
                goto L_0x002e;
                */
                throw new UnsupportedOperationException("Method not decompiled: android.print.PrintManager.PrintDocumentAdapterDelegate.MyLayoutResultCallback.onLayoutFinished(android.print.PrintDocumentInfo, boolean):void");
            }

            public void onLayoutFailed(CharSequence error) {
                synchronized (PrintDocumentAdapterDelegate.this.mLock) {
                    ILayoutResultCallback callback = this.mCallback;
                }
                if (callback == null) {
                    Log.e(PrintManager.LOG_TAG, "PrintDocumentAdapter is destroyed. Did you finish the printing activity before print completion or did you invoke a callback after finish?");
                    return;
                }
                try {
                    callback.onLayoutFailed(error, this.mSequence);
                } catch (RemoteException re) {
                    Log.e(PrintManager.LOG_TAG, "Error calling onLayoutFailed", re);
                } finally {
                    destroy();
                }
            }

            public void onLayoutCancelled() {
                synchronized (PrintDocumentAdapterDelegate.this.mLock) {
                    ILayoutResultCallback callback = this.mCallback;
                }
                if (callback == null) {
                    Log.e(PrintManager.LOG_TAG, "PrintDocumentAdapter is destroyed. Did you finish the printing activity before print completion or did you invoke a callback after finish?");
                    return;
                }
                try {
                    callback.onLayoutCanceled(this.mSequence);
                } catch (RemoteException re) {
                    Log.e(PrintManager.LOG_TAG, "Error calling onLayoutFailed", re);
                } finally {
                    destroy();
                }
            }

            public void destroy() {
                synchronized (PrintDocumentAdapterDelegate.this.mLock) {
                    this.mCallback = null;
                    PrintDocumentAdapterDelegate.this.mPendingCallback = null;
                }
            }
        }

        private final class MyWriteResultCallback extends WriteResultCallback implements DestroyableCallback {
            private IWriteResultCallback mCallback;
            private ParcelFileDescriptor mFd;
            private final int mSequence;

            public MyWriteResultCallback(IWriteResultCallback callback, ParcelFileDescriptor fd, int sequence) {
                this.mFd = fd;
                this.mSequence = sequence;
                this.mCallback = callback;
            }

            public void onWriteFinished(android.print.PageRange[] r5) {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:android.print.PrintManager.PrintDocumentAdapterDelegate.MyWriteResultCallback.onWriteFinished(android.print.PageRange[]):void. bs: [B:12:0x001b, B:23:0x0035]
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:86)
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:57)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:52)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
                /*
                r4 = this;
                r2 = android.print.PrintManager.PrintDocumentAdapterDelegate.this;
                r2 = r2.mLock;
                monitor-enter(r2);
                r0 = r4.mCallback;	 Catch:{ all -> 0x0016 }
                monitor-exit(r2);
                if (r0 != 0) goto L_0x0019;
            L_0x000c:
                r2 = "PrintManager";
                r3 = "PrintDocumentAdapter is destroyed. Did you finish the printing activity before print completion or did you invoke a callback after finish?";
                android.util.Log.e(r2, r3);
                return;
            L_0x0016:
                r3 = move-exception;
                monitor-exit(r2);
                throw r3;
            L_0x0019:
                if (r5 != 0) goto L_0x0029;
            L_0x001b:
                r2 = new java.lang.IllegalArgumentException;	 Catch:{ all -> 0x0024 }
                r3 = "pages cannot be null";	 Catch:{ all -> 0x0024 }
                r2.<init>(r3);	 Catch:{ all -> 0x0024 }
                throw r2;	 Catch:{ all -> 0x0024 }
            L_0x0024:
                r2 = move-exception;
                r4.destroy();
                throw r2;
            L_0x0029:
                r2 = r5.length;	 Catch:{ all -> 0x0024 }
                if (r2 != 0) goto L_0x0035;	 Catch:{ all -> 0x0024 }
            L_0x002c:
                r2 = new java.lang.IllegalArgumentException;	 Catch:{ all -> 0x0024 }
                r3 = "pages cannot be empty";	 Catch:{ all -> 0x0024 }
                r2.<init>(r3);	 Catch:{ all -> 0x0024 }
                throw r2;	 Catch:{ all -> 0x0024 }
            L_0x0035:
                r2 = r4.mSequence;	 Catch:{ RemoteException -> 0x003e }
                r0.onWriteFinished(r5, r2);	 Catch:{ RemoteException -> 0x003e }
            L_0x003a:
                r4.destroy();
                return;
            L_0x003e:
                r1 = move-exception;
                r2 = "PrintManager";	 Catch:{ all -> 0x0024 }
                r3 = "Error calling onWriteFinished";	 Catch:{ all -> 0x0024 }
                android.util.Log.e(r2, r3, r1);	 Catch:{ all -> 0x0024 }
                goto L_0x003a;
                */
                throw new UnsupportedOperationException("Method not decompiled: android.print.PrintManager.PrintDocumentAdapterDelegate.MyWriteResultCallback.onWriteFinished(android.print.PageRange[]):void");
            }

            public void onWriteFailed(CharSequence error) {
                synchronized (PrintDocumentAdapterDelegate.this.mLock) {
                    IWriteResultCallback callback = this.mCallback;
                }
                if (callback == null) {
                    Log.e(PrintManager.LOG_TAG, "PrintDocumentAdapter is destroyed. Did you finish the printing activity before print completion or did you invoke a callback after finish?");
                    return;
                }
                try {
                    callback.onWriteFailed(error, this.mSequence);
                } catch (RemoteException re) {
                    Log.e(PrintManager.LOG_TAG, "Error calling onWriteFailed", re);
                } finally {
                    destroy();
                }
            }

            public void onWriteCancelled() {
                synchronized (PrintDocumentAdapterDelegate.this.mLock) {
                    IWriteResultCallback callback = this.mCallback;
                }
                if (callback == null) {
                    Log.e(PrintManager.LOG_TAG, "PrintDocumentAdapter is destroyed. Did you finish the printing activity before print completion or did you invoke a callback after finish?");
                    return;
                }
                try {
                    callback.onWriteCanceled(this.mSequence);
                } catch (RemoteException re) {
                    Log.e(PrintManager.LOG_TAG, "Error calling onWriteCanceled", re);
                } finally {
                    destroy();
                }
            }

            public void destroy() {
                synchronized (PrintDocumentAdapterDelegate.this.mLock) {
                    IoUtils.closeQuietly(this.mFd);
                    this.mCallback = null;
                    this.mFd = null;
                    PrintDocumentAdapterDelegate.this.mPendingCallback = null;
                }
            }
        }

        public PrintDocumentAdapterDelegate(Activity activity, PrintDocumentAdapter documentAdapter) {
            this.mLock = new Object();
            if (activity.isFinishing()) {
                throw new IllegalStateException("Cannot start printing for finishing activity");
            }
            this.mActivity = activity;
            this.mDocumentAdapter = documentAdapter;
            this.mHandler = new MyHandler(this.mActivity.getMainLooper());
            this.mActivity.getApplication().registerActivityLifecycleCallbacks(this);
        }

        public void setObserver(IPrintDocumentAdapterObserver observer) {
            synchronized (this.mLock) {
                this.mObserver = observer;
                boolean destroyed = isDestroyedLocked();
            }
            if (destroyed && observer != null) {
                try {
                    observer.onDestroy();
                } catch (RemoteException re) {
                    Log.e(PrintManager.LOG_TAG, "Error announcing destroyed state", re);
                }
            }
        }

        public void start() {
            synchronized (this.mLock) {
                if (!isDestroyedLocked()) {
                    this.mHandler.obtainMessage(PrintManager.MSG_NOTIFY_PRINT_JOB_STATE_CHANGED, this.mDocumentAdapter).sendToTarget();
                }
            }
        }

        public void layout(PrintAttributes oldAttributes, PrintAttributes newAttributes, ILayoutResultCallback callback, Bundle metadata, int sequence) {
            ICancellationSignal cancellationTransport = CancellationSignal.createTransport();
            try {
                callback.onLayoutStarted(cancellationTransport, sequence);
                synchronized (this.mLock) {
                    if (isDestroyedLocked()) {
                        return;
                    }
                    CancellationSignal cancellationSignal = CancellationSignal.fromTransport(cancellationTransport);
                    SomeArgs args = SomeArgs.obtain();
                    args.arg1 = this.mDocumentAdapter;
                    args.arg2 = oldAttributes;
                    args.arg3 = newAttributes;
                    args.arg4 = cancellationSignal;
                    args.arg5 = new MyLayoutResultCallback(callback, sequence);
                    args.arg6 = metadata;
                    this.mHandler.obtainMessage(PrintManager.MSG_NOTIFY_PRINT_SERVICES_CHANGED, args).sendToTarget();
                }
            } catch (RemoteException re) {
                Log.e(PrintManager.LOG_TAG, "Error notifying for layout start", re);
            }
        }

        public void write(PageRange[] pages, ParcelFileDescriptor fd, IWriteResultCallback callback, int sequence) {
            ICancellationSignal cancellationTransport = CancellationSignal.createTransport();
            try {
                callback.onWriteStarted(cancellationTransport, sequence);
                synchronized (this.mLock) {
                    if (isDestroyedLocked()) {
                        return;
                    }
                    CancellationSignal cancellationSignal = CancellationSignal.fromTransport(cancellationTransport);
                    SomeArgs args = SomeArgs.obtain();
                    args.arg1 = this.mDocumentAdapter;
                    args.arg2 = pages;
                    args.arg3 = fd;
                    args.arg4 = cancellationSignal;
                    args.arg5 = new MyWriteResultCallback(callback, fd, sequence);
                    this.mHandler.obtainMessage(PrintManager.MSG_NOTIFY_PRINT_SERVICE_RECOMMENDATIONS_CHANGED, args).sendToTarget();
                }
            } catch (RemoteException re) {
                Log.e(PrintManager.LOG_TAG, "Error notifying for write start", re);
            }
        }

        public void finish() {
            synchronized (this.mLock) {
                if (!isDestroyedLocked()) {
                    this.mHandler.obtainMessage(4, this.mDocumentAdapter).sendToTarget();
                }
            }
        }

        public void kill(String reason) {
            synchronized (this.mLock) {
                if (!isDestroyedLocked()) {
                    this.mHandler.obtainMessage(5, reason).sendToTarget();
                }
            }
        }

        public void onActivityPaused(Activity activity) {
        }

        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        public void onActivityStarted(Activity activity) {
        }

        public void onActivityResumed(Activity activity) {
        }

        public void onActivityStopped(Activity activity) {
        }

        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        public void onActivityDestroyed(Activity activity) {
            IPrintDocumentAdapterObserver iPrintDocumentAdapterObserver = null;
            synchronized (this.mLock) {
                if (activity == this.mActivity) {
                    iPrintDocumentAdapterObserver = this.mObserver;
                    destroyLocked();
                }
            }
            if (iPrintDocumentAdapterObserver != null) {
                try {
                    iPrintDocumentAdapterObserver.onDestroy();
                } catch (RemoteException re) {
                    Log.e(PrintManager.LOG_TAG, "Error announcing destroyed state", re);
                }
            }
        }

        private boolean isDestroyedLocked() {
            return this.mActivity == null ? true : PrintManager.DEBUG;
        }

        private void destroyLocked() {
            this.mActivity.getApplication().unregisterActivityLifecycleCallbacks(this);
            this.mActivity = null;
            this.mDocumentAdapter = null;
            this.mHandler.removeMessages(PrintManager.MSG_NOTIFY_PRINT_JOB_STATE_CHANGED);
            this.mHandler.removeMessages(PrintManager.MSG_NOTIFY_PRINT_SERVICES_CHANGED);
            this.mHandler.removeMessages(PrintManager.MSG_NOTIFY_PRINT_SERVICE_RECOMMENDATIONS_CHANGED);
            this.mHandler.removeMessages(4);
            this.mHandler = null;
            this.mObserver = null;
            if (this.mPendingCallback != null) {
                this.mPendingCallback.destroy();
                this.mPendingCallback = null;
            }
        }
    }

    public interface PrintJobStateChangeListener {
        void onPrintJobStateChanged(PrintJobId printJobId);
    }

    public static final class PrintJobStateChangeListenerWrapper extends IPrintJobStateChangeListener.Stub {
        private final WeakReference<Handler> mWeakHandler;
        private final WeakReference<PrintJobStateChangeListener> mWeakListener;

        public PrintJobStateChangeListenerWrapper(PrintJobStateChangeListener listener, Handler handler) {
            this.mWeakListener = new WeakReference(listener);
            this.mWeakHandler = new WeakReference(handler);
        }

        public void onPrintJobStateChanged(PrintJobId printJobId) {
            Handler handler = (Handler) this.mWeakHandler.get();
            PrintJobStateChangeListener listener = (PrintJobStateChangeListener) this.mWeakListener.get();
            if (handler != null && listener != null) {
                SomeArgs args = SomeArgs.obtain();
                args.arg1 = this;
                args.arg2 = printJobId;
                handler.obtainMessage(PrintManager.MSG_NOTIFY_PRINT_JOB_STATE_CHANGED, args).sendToTarget();
            }
        }

        public void destroy() {
            this.mWeakListener.clear();
        }

        public PrintJobStateChangeListener getListener() {
            return (PrintJobStateChangeListener) this.mWeakListener.get();
        }
    }

    public interface PrintServiceRecommendationsChangeListener {
        void onPrintServiceRecommendationsChanged();
    }

    public static final class PrintServiceRecommendationsChangeListenerWrapper extends IRecommendationsChangeListener.Stub {
        private final WeakReference<Handler> mWeakHandler;
        private final WeakReference<PrintServiceRecommendationsChangeListener> mWeakListener;

        public PrintServiceRecommendationsChangeListenerWrapper(PrintServiceRecommendationsChangeListener listener, Handler handler) {
            this.mWeakListener = new WeakReference(listener);
            this.mWeakHandler = new WeakReference(handler);
        }

        public void onRecommendationsChanged() {
            Handler handler = (Handler) this.mWeakHandler.get();
            PrintServiceRecommendationsChangeListener listener = (PrintServiceRecommendationsChangeListener) this.mWeakListener.get();
            if (handler != null && listener != null) {
                handler.obtainMessage(PrintManager.MSG_NOTIFY_PRINT_SERVICE_RECOMMENDATIONS_CHANGED, this).sendToTarget();
            }
        }

        public void destroy() {
            this.mWeakListener.clear();
        }

        public PrintServiceRecommendationsChangeListener getListener() {
            return (PrintServiceRecommendationsChangeListener) this.mWeakListener.get();
        }
    }

    public interface PrintServicesChangeListener {
        void onPrintServicesChanged();
    }

    public static final class PrintServicesChangeListenerWrapper extends IPrintServicesChangeListener.Stub {
        private final WeakReference<Handler> mWeakHandler;
        private final WeakReference<PrintServicesChangeListener> mWeakListener;

        public PrintServicesChangeListenerWrapper(PrintServicesChangeListener listener, Handler handler) {
            this.mWeakListener = new WeakReference(listener);
            this.mWeakHandler = new WeakReference(handler);
        }

        public void onPrintServicesChanged() {
            Handler handler = (Handler) this.mWeakHandler.get();
            PrintServicesChangeListener listener = (PrintServicesChangeListener) this.mWeakListener.get();
            if (handler != null && listener != null) {
                handler.obtainMessage(PrintManager.MSG_NOTIFY_PRINT_SERVICES_CHANGED, this).sendToTarget();
            }
        }

        public void destroy() {
            this.mWeakListener.clear();
        }

        public PrintServicesChangeListener getListener() {
            return (PrintServicesChangeListener) this.mWeakListener.get();
        }
    }

    public PrintManager(Context context, IPrintManager service, int userId, int appId) {
        this.mContext = context;
        this.mService = service;
        this.mUserId = userId;
        this.mAppId = appId;
        this.mHandler = new AnonymousClass1(context.getMainLooper(), null, DEBUG);
    }

    public PrintManager getGlobalPrintManagerForUser(int userId) {
        if (this.mService != null) {
            return new PrintManager(this.mContext, this.mService, userId, APP_ID_ANY);
        }
        Log.w(LOG_TAG, "Feature android.software.print not available");
        return null;
    }

    PrintJobInfo getPrintJobInfo(PrintJobId printJobId) {
        try {
            return this.mService.getPrintJobInfo(printJobId, this.mAppId, this.mUserId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

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
        if (this.mService == null) {
            Log.w(LOG_TAG, "Feature android.software.print not available");
        } else if (this.mPrintJobStateChangeListeners != null) {
            PrintJobStateChangeListenerWrapper wrappedListener = (PrintJobStateChangeListenerWrapper) this.mPrintJobStateChangeListeners.remove(listener);
            if (wrappedListener != null) {
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
    }

    public PrintJob getPrintJob(PrintJobId printJobId) {
        if (this.mService == null) {
            Log.w(LOG_TAG, "Feature android.software.print not available");
            return null;
        }
        try {
            PrintJobInfo printJob = this.mService.getPrintJobInfo(printJobId, this.mAppId, this.mUserId);
            if (printJob != null) {
                return new PrintJob(printJob, this);
            }
            return null;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public Icon getCustomPrinterIcon(PrinterId printerId) {
        if (this.mService == null) {
            Log.w(LOG_TAG, "Feature android.software.print not available");
            return null;
        }
        try {
            return this.mService.getCustomPrinterIcon(printerId, this.mUserId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public List<PrintJob> getPrintJobs() {
        if (this.mService == null) {
            Log.w(LOG_TAG, "Feature android.software.print not available");
            return Collections.emptyList();
        }
        try {
            List<PrintJobInfo> printJobInfos = this.mService.getPrintJobInfos(this.mAppId, this.mUserId);
            if (printJobInfos == null) {
                return Collections.emptyList();
            }
            int printJobCount = printJobInfos.size();
            List<PrintJob> printJobs = new ArrayList(printJobCount);
            for (int i = 0; i < printJobCount; i += MSG_NOTIFY_PRINT_JOB_STATE_CHANGED) {
                printJobs.add(new PrintJob((PrintJobInfo) printJobInfos.get(i), this));
            }
            return printJobs;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    void cancelPrintJob(PrintJobId printJobId) {
        if (this.mService == null) {
            Log.w(LOG_TAG, "Feature android.software.print not available");
            return;
        }
        try {
            this.mService.cancelPrintJob(printJobId, this.mAppId, this.mUserId);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    void restartPrintJob(PrintJobId printJobId) {
        if (this.mService == null) {
            Log.w(LOG_TAG, "Feature android.software.print not available");
            return;
        }
        try {
            this.mService.restartPrintJob(printJobId, this.mAppId, this.mUserId);
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
        } else if (documentAdapter == null) {
            throw new IllegalArgumentException("documentAdapter cannot be null");
        } else {
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
                    } catch (SendIntentException sie) {
                        Log.e(LOG_TAG, "Couldn't start print job config activity.", sie);
                    }
                }
                return null;
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    void addPrintServicesChangeListener(PrintServicesChangeListener listener) {
        Preconditions.checkNotNull(listener);
        if (this.mService == null) {
            Log.w(LOG_TAG, "Feature android.software.print not available");
            return;
        }
        if (this.mPrintServicesChangeListeners == null) {
            this.mPrintServicesChangeListeners = new ArrayMap();
        }
        PrintServicesChangeListenerWrapper wrappedListener = new PrintServicesChangeListenerWrapper(listener, this.mHandler);
        try {
            this.mService.addPrintServicesChangeListener(wrappedListener, this.mUserId);
            this.mPrintServicesChangeListeners.put(listener, wrappedListener);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    void removePrintServicesChangeListener(PrintServicesChangeListener listener) {
        Preconditions.checkNotNull(listener);
        if (this.mService == null) {
            Log.w(LOG_TAG, "Feature android.software.print not available");
        } else if (this.mPrintServicesChangeListeners != null) {
            PrintServicesChangeListenerWrapper wrappedListener = (PrintServicesChangeListenerWrapper) this.mPrintServicesChangeListeners.remove(listener);
            if (wrappedListener != null) {
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
    }

    public List<PrintServiceInfo> getPrintServices(int selectionFlags) {
        Preconditions.checkFlagsArgument(selectionFlags, MSG_NOTIFY_PRINT_SERVICE_RECOMMENDATIONS_CHANGED);
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

    void addPrintServiceRecommendationsChangeListener(PrintServiceRecommendationsChangeListener listener) {
        Preconditions.checkNotNull(listener);
        if (this.mService == null) {
            Log.w(LOG_TAG, "Feature android.software.print not available");
            return;
        }
        if (this.mPrintServiceRecommendationsChangeListeners == null) {
            this.mPrintServiceRecommendationsChangeListeners = new ArrayMap();
        }
        PrintServiceRecommendationsChangeListenerWrapper wrappedListener = new PrintServiceRecommendationsChangeListenerWrapper(listener, this.mHandler);
        try {
            this.mService.addPrintServiceRecommendationsChangeListener(wrappedListener, this.mUserId);
            this.mPrintServiceRecommendationsChangeListeners.put(listener, wrappedListener);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    void removePrintServiceRecommendationsChangeListener(PrintServiceRecommendationsChangeListener listener) {
        Preconditions.checkNotNull(listener);
        if (this.mService == null) {
            Log.w(LOG_TAG, "Feature android.software.print not available");
        } else if (this.mPrintServiceRecommendationsChangeListeners != null) {
            PrintServiceRecommendationsChangeListenerWrapper wrappedListener = (PrintServiceRecommendationsChangeListenerWrapper) this.mPrintServiceRecommendationsChangeListeners.remove(listener);
            if (wrappedListener != null) {
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
    }

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
        if (this.mService != null) {
            return new PrinterDiscoverySession(this.mService, this.mContext, this.mUserId);
        }
        Log.w(LOG_TAG, "Feature android.software.print not available");
        return null;
    }

    public void setPrintServiceEnabled(ComponentName service, boolean isEnabled) {
        if (this.mService == null) {
            Log.w(LOG_TAG, "Feature android.software.print not available");
            return;
        }
        try {
            this.mService.setPrintServiceEnabled(service, isEnabled, this.mUserId);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error enabling or disabling " + service, re);
        }
    }
}
