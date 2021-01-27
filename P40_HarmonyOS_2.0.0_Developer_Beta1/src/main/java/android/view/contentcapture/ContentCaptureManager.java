package android.view.contentcapture;

import android.annotation.SystemApi;
import android.content.ComponentName;
import android.content.ContentCaptureOptions;
import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.WindowManager;
import android.view.contentcapture.IContentCaptureManager;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.Preconditions;
import com.android.internal.util.SyncResultReceiver;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;

public final class ContentCaptureManager {
    public static final int DEFAULT_IDLE_FLUSHING_FREQUENCY_MS = 5000;
    public static final int DEFAULT_LOG_HISTORY_SIZE = 10;
    public static final int DEFAULT_MAX_BUFFER_SIZE = 100;
    public static final int DEFAULT_TEXT_CHANGE_FLUSHING_FREQUENCY_MS = 1000;
    public static final String DEVICE_CONFIG_PROPERTY_IDLE_FLUSH_FREQUENCY = "idle_flush_frequency";
    public static final String DEVICE_CONFIG_PROPERTY_IDLE_UNBIND_TIMEOUT = "idle_unbind_timeout";
    public static final String DEVICE_CONFIG_PROPERTY_LOGGING_LEVEL = "logging_level";
    public static final String DEVICE_CONFIG_PROPERTY_LOG_HISTORY_SIZE = "log_history_size";
    public static final String DEVICE_CONFIG_PROPERTY_MAX_BUFFER_SIZE = "max_buffer_size";
    public static final String DEVICE_CONFIG_PROPERTY_SERVICE_EXPLICITLY_ENABLED = "service_explicitly_enabled";
    public static final String DEVICE_CONFIG_PROPERTY_TEXT_CHANGE_FLUSH_FREQUENCY = "text_change_flush_frequency";
    public static final int LOGGING_LEVEL_DEBUG = 1;
    public static final int LOGGING_LEVEL_OFF = 0;
    public static final int LOGGING_LEVEL_VERBOSE = 2;
    public static final int RESULT_CODE_FALSE = 2;
    public static final int RESULT_CODE_OK = 0;
    public static final int RESULT_CODE_SECURITY_EXCEPTION = -1;
    public static final int RESULT_CODE_TRUE = 1;
    private static final int SYNC_CALLS_TIMEOUT_MS = 5000;
    private static final String TAG = ContentCaptureManager.class.getSimpleName();
    private final Context mContext;
    @GuardedBy({"mLock"})
    private int mFlags;
    private final Handler mHandler;
    private final Object mLock = new Object();
    @GuardedBy({"mLock"})
    private MainContentCaptureSession mMainSession;
    final ContentCaptureOptions mOptions;
    private final IContentCaptureManager mService;

    public interface ContentCaptureClient {
        ComponentName contentCaptureClientGetComponentName();
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface LoggingLevel {
    }

    /* access modifiers changed from: private */
    public interface MyRunnable {
        void run(SyncResultReceiver syncResultReceiver) throws RemoteException;
    }

    public ContentCaptureManager(Context context, IContentCaptureManager service, ContentCaptureOptions options) {
        this.mContext = (Context) Preconditions.checkNotNull(context, "context cannot be null");
        this.mService = (IContentCaptureManager) Preconditions.checkNotNull(service, "service cannot be null");
        this.mOptions = (ContentCaptureOptions) Preconditions.checkNotNull(options, "options cannot be null");
        ContentCaptureHelper.setLoggingLevel(this.mOptions.loggingLevel);
        if (ContentCaptureHelper.sVerbose) {
            String str = TAG;
            Log.v(str, "Constructor for " + context.getPackageName());
        }
        this.mHandler = Handler.createAsync(Looper.getMainLooper());
    }

    public MainContentCaptureSession getMainContentCaptureSession() {
        MainContentCaptureSession mainContentCaptureSession;
        synchronized (this.mLock) {
            if (this.mMainSession == null) {
                this.mMainSession = new MainContentCaptureSession(this.mContext, this, this.mHandler, this.mService);
                if (ContentCaptureHelper.sVerbose) {
                    String str = TAG;
                    Log.v(str, "getMainContentCaptureSession(): created " + this.mMainSession);
                }
            }
            mainContentCaptureSession = this.mMainSession;
        }
        return mainContentCaptureSession;
    }

    public void onActivityCreated(IBinder applicationToken, ComponentName activityComponent) {
        if (!this.mOptions.lite) {
            synchronized (this.mLock) {
                getMainContentCaptureSession().start(applicationToken, activityComponent, this.mFlags);
            }
        }
    }

    public void onActivityResumed() {
        if (!this.mOptions.lite) {
            getMainContentCaptureSession().notifySessionLifecycle(true);
        }
    }

    public void onActivityPaused() {
        if (!this.mOptions.lite) {
            getMainContentCaptureSession().notifySessionLifecycle(false);
        }
    }

    public void onActivityDestroyed() {
        if (!this.mOptions.lite) {
            getMainContentCaptureSession().destroy();
        }
    }

    public void flush(int reason) {
        if (!this.mOptions.lite) {
            getMainContentCaptureSession().flush(reason);
        }
    }

    public ComponentName getServiceComponentName() {
        if (!isContentCaptureEnabled() && !this.mOptions.lite) {
            return null;
        }
        SyncResultReceiver resultReceiver = new SyncResultReceiver(5000);
        try {
            this.mService.getServiceComponentName(resultReceiver);
            return (ComponentName) resultReceiver.getParcelableResult();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static ComponentName getServiceSettingsComponentName() {
        IBinder binder = ServiceManager.checkService("content_capture");
        if (binder == null) {
            return null;
        }
        IContentCaptureManager service = IContentCaptureManager.Stub.asInterface(binder);
        SyncResultReceiver resultReceiver = new SyncResultReceiver(5000);
        try {
            service.getServiceSettingsActivity(resultReceiver);
            if (resultReceiver.getIntResult() != -1) {
                return (ComponentName) resultReceiver.getParcelableResult();
            }
            throw new SecurityException(resultReceiver.getStringResult());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isContentCaptureEnabled() {
        MainContentCaptureSession mainSession;
        if (this.mOptions.lite) {
            return false;
        }
        synchronized (this.mLock) {
            mainSession = this.mMainSession;
        }
        if (mainSession == null || !mainSession.isDisabled()) {
            return true;
        }
        return false;
    }

    public Set<ContentCaptureCondition> getContentCaptureConditions() {
        if (isContentCaptureEnabled() || this.mOptions.lite) {
            return ContentCaptureHelper.toSet(syncRun(new MyRunnable() {
                /* class android.view.contentcapture.$$Lambda$ContentCaptureManager$F5a5O5ubPHwlndmmnmOInl75_sQ */

                @Override // android.view.contentcapture.ContentCaptureManager.MyRunnable
                public final void run(SyncResultReceiver syncResultReceiver) {
                    ContentCaptureManager.this.lambda$getContentCaptureConditions$0$ContentCaptureManager(syncResultReceiver);
                }
            }).getParcelableListResult());
        }
        return null;
    }

    public /* synthetic */ void lambda$getContentCaptureConditions$0$ContentCaptureManager(SyncResultReceiver r) throws RemoteException {
        this.mService.getContentCaptureConditions(this.mContext.getPackageName(), r);
    }

    public void setContentCaptureEnabled(boolean enabled) {
        MainContentCaptureSession mainSession;
        if (ContentCaptureHelper.sDebug) {
            Log.d(TAG, "setContentCaptureEnabled(): setting to " + enabled + " for " + this.mContext);
        }
        synchronized (this.mLock) {
            if (enabled) {
                this.mFlags &= -2;
            } else {
                this.mFlags |= 1;
            }
            mainSession = this.mMainSession;
        }
        if (mainSession != null) {
            mainSession.setDisabled(!enabled);
        }
    }

    public void updateWindowAttributes(WindowManager.LayoutParams params) {
        MainContentCaptureSession mainSession;
        if (ContentCaptureHelper.sDebug) {
            Log.d(TAG, "updateWindowAttributes(): window flags=" + params.flags);
        }
        boolean flagSecureEnabled = (params.flags & 8192) != 0;
        synchronized (this.mLock) {
            if (flagSecureEnabled) {
                this.mFlags |= 2;
            } else {
                this.mFlags &= -3;
            }
            mainSession = this.mMainSession;
        }
        if (mainSession != null) {
            mainSession.setDisabled(flagSecureEnabled);
        }
    }

    @SystemApi
    public boolean isContentCaptureFeatureEnabled() {
        int resultCode = syncRun(new MyRunnable() {
            /* class android.view.contentcapture.$$Lambda$ContentCaptureManager$uvjEvSXcmP7uA6i89N3m1TrKCk */

            @Override // android.view.contentcapture.ContentCaptureManager.MyRunnable
            public final void run(SyncResultReceiver syncResultReceiver) {
                ContentCaptureManager.this.lambda$isContentCaptureFeatureEnabled$1$ContentCaptureManager(syncResultReceiver);
            }
        }).getIntResult();
        if (resultCode == 1) {
            return true;
        }
        if (resultCode == 2) {
            return false;
        }
        String str = TAG;
        Log.wtf(str, "received invalid result: " + resultCode);
        return false;
    }

    public /* synthetic */ void lambda$isContentCaptureFeatureEnabled$1$ContentCaptureManager(SyncResultReceiver r) throws RemoteException {
        this.mService.isContentCaptureFeatureEnabled(r);
    }

    public void removeData(DataRemovalRequest request) {
        Preconditions.checkNotNull(request);
        try {
            this.mService.removeData(request);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    private SyncResultReceiver syncRun(MyRunnable r) {
        SyncResultReceiver resultReceiver = new SyncResultReceiver(5000);
        try {
            r.run(resultReceiver);
            if (resultReceiver.getIntResult() != -1) {
                return resultReceiver;
            }
            throw new SecurityException(resultReceiver.getStringResult());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.println("ContentCaptureManager");
        String prefix2 = prefix + "  ";
        synchronized (this.mLock) {
            pw.print(prefix2);
            pw.print("isContentCaptureEnabled(): ");
            pw.println(isContentCaptureEnabled());
            pw.print(prefix2);
            pw.print("Debug: ");
            pw.print(ContentCaptureHelper.sDebug);
            pw.print(" Verbose: ");
            pw.println(ContentCaptureHelper.sVerbose);
            pw.print(prefix2);
            pw.print("Context: ");
            pw.println(this.mContext);
            pw.print(prefix2);
            pw.print("User: ");
            pw.println(this.mContext.getUserId());
            pw.print(prefix2);
            pw.print("Service: ");
            pw.println(this.mService);
            pw.print(prefix2);
            pw.print("Flags: ");
            pw.println(this.mFlags);
            pw.print(prefix2);
            pw.print("Options: ");
            this.mOptions.dumpShort(pw);
            pw.println();
            if (this.mMainSession != null) {
                pw.print(prefix2);
                pw.println("Main session:");
                this.mMainSession.dump(prefix2 + "  ", pw);
            } else {
                pw.print(prefix2);
                pw.println("No sessions");
            }
        }
    }
}
