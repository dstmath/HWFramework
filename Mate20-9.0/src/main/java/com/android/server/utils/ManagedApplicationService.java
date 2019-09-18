package com.android.server.utils;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Slog;
import com.android.server.utils.ManagedApplicationService;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class ManagedApplicationService {
    private static final int MAX_RETRY_COUNT = 4;
    private static final long MAX_RETRY_DURATION_MS = 16000;
    private static final long MIN_RETRY_DURATION_MS = 2000;
    public static final int RETRY_BEST_EFFORT = 3;
    public static final int RETRY_FOREVER = 1;
    public static final int RETRY_NEVER = 2;
    private static final long RETRY_RESET_TIME_MS = 64000;
    /* access modifiers changed from: private */
    public final String TAG = getClass().getSimpleName();
    /* access modifiers changed from: private */
    public IInterface mBoundInterface;
    /* access modifiers changed from: private */
    public final BinderChecker mChecker;
    private final int mClientLabel;
    /* access modifiers changed from: private */
    public final ComponentName mComponent;
    /* access modifiers changed from: private */
    public ServiceConnection mConnection;
    private final Context mContext;
    /* access modifiers changed from: private */
    public final EventCallback mEventCb;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    private final boolean mIsImportant;
    private long mLastRetryTimeMs;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private long mNextRetryDurationMs = MIN_RETRY_DURATION_MS;
    /* access modifiers changed from: private */
    public PendingEvent mPendingEvent;
    private int mRetryCount;
    private final Runnable mRetryRunnable = new Runnable() {
        public final void run() {
            ManagedApplicationService.this.doRetry();
        }
    };
    private final int mRetryType;
    private boolean mRetrying;
    private final String mSettingsAction;
    private final int mUserId;

    public interface BinderChecker {
        IInterface asInterface(IBinder iBinder);

        boolean checkType(IInterface iInterface);
    }

    public interface EventCallback {
        void onServiceEvent(LogEvent logEvent);
    }

    public static class LogEvent implements LogFormattable {
        public static final int EVENT_BINDING_DIED = 3;
        public static final int EVENT_CONNECTED = 1;
        public static final int EVENT_DISCONNECTED = 2;
        public static final int EVENT_STOPPED_PERMANENTLY = 4;
        public final ComponentName component;
        public final int event;
        public final long timestamp;

        public LogEvent(long timestamp2, ComponentName component2, int event2) {
            this.timestamp = timestamp2;
            this.component = component2;
            this.event = event2;
        }

        public String toLogString(SimpleDateFormat dateFormat) {
            StringBuilder sb = new StringBuilder();
            sb.append(dateFormat.format(new Date(this.timestamp)));
            sb.append("   ");
            sb.append(eventToString(this.event));
            sb.append(" Managed Service: ");
            sb.append(this.component == null ? "None" : this.component.flattenToString());
            return sb.toString();
        }

        public static String eventToString(int event2) {
            switch (event2) {
                case 1:
                    return "Connected";
                case 2:
                    return "Disconnected";
                case 3:
                    return "Binding Died For";
                case 4:
                    return "Permanently Stopped";
                default:
                    return "Unknown Event Occurred";
            }
        }
    }

    public interface LogFormattable {
        String toLogString(SimpleDateFormat simpleDateFormat);
    }

    public interface PendingEvent {
        void runEvent(IInterface iInterface) throws RemoteException;
    }

    private ManagedApplicationService(Context context, ComponentName component, int userId, int clientLabel, String settingsAction, BinderChecker binderChecker, boolean isImportant, int retryType, Handler handler, EventCallback eventCallback) {
        this.mContext = context;
        this.mComponent = component;
        this.mUserId = userId;
        this.mClientLabel = clientLabel;
        this.mSettingsAction = settingsAction;
        this.mChecker = binderChecker;
        this.mIsImportant = isImportant;
        this.mRetryType = retryType;
        this.mHandler = handler;
        this.mEventCb = eventCallback;
    }

    public static ManagedApplicationService build(Context context, ComponentName component, int userId, int clientLabel, String settingsAction, BinderChecker binderChecker, boolean isImportant, int retryType, Handler handler, EventCallback eventCallback) {
        ManagedApplicationService managedApplicationService = new ManagedApplicationService(context, component, userId, clientLabel, settingsAction, binderChecker, isImportant, retryType, handler, eventCallback);
        return managedApplicationService;
    }

    public int getUserId() {
        return this.mUserId;
    }

    public ComponentName getComponent() {
        return this.mComponent;
    }

    public boolean disconnectIfNotMatching(ComponentName componentName, int userId) {
        if (matches(componentName, userId)) {
            return false;
        }
        disconnect();
        return true;
    }

    public void sendEvent(PendingEvent event) {
        IInterface iface;
        synchronized (this.mLock) {
            iface = this.mBoundInterface;
            if (iface == null) {
                this.mPendingEvent = event;
            }
        }
        if (iface != null) {
            try {
                event.runEvent(iface);
            } catch (RemoteException | RuntimeException ex) {
                Slog.e(this.TAG, "Received exception from user service: ", ex);
            }
        }
    }

    public void disconnect() {
        synchronized (this.mLock) {
            if (this.mConnection != null) {
                this.mContext.unbindService(this.mConnection);
                this.mConnection = null;
                this.mBoundInterface = null;
            }
        }
    }

    public void connect() {
        synchronized (this.mLock) {
            if (this.mConnection == null) {
                Intent intent = new Intent().setComponent(this.mComponent);
                if (this.mClientLabel != 0) {
                    intent.putExtra("android.intent.extra.client_label", this.mClientLabel);
                }
                if (this.mSettingsAction != null) {
                    intent.putExtra("android.intent.extra.client_intent", PendingIntent.getActivity(this.mContext, 0, new Intent(this.mSettingsAction), 0));
                }
                this.mConnection = new ServiceConnection() {
                    public void onBindingDied(ComponentName componentName) {
                        long timestamp = System.currentTimeMillis();
                        String access$000 = ManagedApplicationService.this.TAG;
                        Slog.w(access$000, "Service binding died: " + componentName);
                        synchronized (ManagedApplicationService.this.mLock) {
                            if (ManagedApplicationService.this.mConnection == this) {
                                ManagedApplicationService.this.mHandler.post(new Runnable(timestamp) {
                                    private final /* synthetic */ long f$1;

                                    {
                                        this.f$1 = r2;
                                    }

                                    public final void run() {
                                        ManagedApplicationService.this.mEventCb.onServiceEvent(new ManagedApplicationService.LogEvent(this.f$1, ManagedApplicationService.this.mComponent, 3));
                                    }
                                });
                                IInterface unused = ManagedApplicationService.this.mBoundInterface = null;
                                ManagedApplicationService.this.startRetriesLocked();
                            }
                        }
                    }

                    /* JADX WARNING: Code restructure failed: missing block: B:16:0x00ad, code lost:
                        if (r2 == null) goto L_0x00c6;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:17:0x00af, code lost:
                        if (r3 == null) goto L_0x00c6;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
                        r3.runEvent(r2);
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:20:0x00b5, code lost:
                        r4 = move-exception;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00b6, code lost:
                        android.util.Slog.e(com.android.server.utils.ManagedApplicationService.access$000(r8.this$0), "Received exception from user service: ", r4);
                        com.android.server.utils.ManagedApplicationService.access$500(r8.this$0);
                     */
                    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                        long timestamp = System.currentTimeMillis();
                        String access$000 = ManagedApplicationService.this.TAG;
                        Slog.i(access$000, "Service connected: " + componentName);
                        IInterface iface = null;
                        PendingEvent pendingEvent = null;
                        synchronized (ManagedApplicationService.this.mLock) {
                            if (ManagedApplicationService.this.mConnection == this) {
                                ManagedApplicationService.this.mHandler.post(new Runnable(timestamp) {
                                    private final /* synthetic */ long f$1;

                                    {
                                        this.f$1 = r2;
                                    }

                                    public final void run() {
                                        ManagedApplicationService.this.mEventCb.onServiceEvent(new ManagedApplicationService.LogEvent(this.f$1, ManagedApplicationService.this.mComponent, 1));
                                    }
                                });
                                ManagedApplicationService.this.stopRetriesLocked();
                                IInterface unused = ManagedApplicationService.this.mBoundInterface = null;
                                if (ManagedApplicationService.this.mChecker != null) {
                                    IInterface unused2 = ManagedApplicationService.this.mBoundInterface = ManagedApplicationService.this.mChecker.asInterface(iBinder);
                                    if (!ManagedApplicationService.this.mChecker.checkType(ManagedApplicationService.this.mBoundInterface)) {
                                        IInterface unused3 = ManagedApplicationService.this.mBoundInterface = null;
                                        String access$0002 = ManagedApplicationService.this.TAG;
                                        Slog.w(access$0002, "Invalid binder from " + componentName);
                                        ManagedApplicationService.this.startRetriesLocked();
                                        return;
                                    }
                                    iface = ManagedApplicationService.this.mBoundInterface;
                                    pendingEvent = ManagedApplicationService.this.mPendingEvent;
                                    PendingEvent unused4 = ManagedApplicationService.this.mPendingEvent = null;
                                }
                            }
                        }
                    }

                    public void onServiceDisconnected(ComponentName componentName) {
                        long timestamp = System.currentTimeMillis();
                        String access$000 = ManagedApplicationService.this.TAG;
                        Slog.w(access$000, "Service disconnected: " + componentName);
                        synchronized (ManagedApplicationService.this.mLock) {
                            if (ManagedApplicationService.this.mConnection == this) {
                                ManagedApplicationService.this.mHandler.post(new Runnable(timestamp) {
                                    private final /* synthetic */ long f$1;

                                    {
                                        this.f$1 = r2;
                                    }

                                    public final void run() {
                                        ManagedApplicationService.this.mEventCb.onServiceEvent(new ManagedApplicationService.LogEvent(this.f$1, ManagedApplicationService.this.mComponent, 2));
                                    }
                                });
                                IInterface unused = ManagedApplicationService.this.mBoundInterface = null;
                                ManagedApplicationService.this.startRetriesLocked();
                            }
                        }
                    }
                };
                int flags = 67108865;
                if (this.mIsImportant) {
                    flags = 67108865 | 64;
                }
                try {
                    if (!this.mContext.bindServiceAsUser(intent, this.mConnection, flags, new UserHandle(this.mUserId))) {
                        String str = this.TAG;
                        Slog.w(str, "Unable to bind service: " + intent);
                        startRetriesLocked();
                    }
                } catch (SecurityException e) {
                    String str2 = this.TAG;
                    Slog.w(str2, "Unable to bind service: " + intent, e);
                    startRetriesLocked();
                }
            }
        }
    }

    private boolean matches(ComponentName component, int userId) {
        return Objects.equals(this.mComponent, component) && this.mUserId == userId;
    }

    /* access modifiers changed from: private */
    public void startRetriesLocked() {
        if (checkAndDeliverServiceDiedCbLocked()) {
            disconnect();
        } else if (!this.mRetrying) {
            this.mRetrying = true;
            queueRetryLocked();
        }
    }

    /* access modifiers changed from: private */
    public void stopRetriesLocked() {
        this.mRetrying = false;
        this.mHandler.removeCallbacks(this.mRetryRunnable);
    }

    private void queueRetryLocked() {
        long now = SystemClock.uptimeMillis();
        if (now - this.mLastRetryTimeMs > RETRY_RESET_TIME_MS) {
            this.mNextRetryDurationMs = MIN_RETRY_DURATION_MS;
            this.mRetryCount = 0;
        }
        this.mLastRetryTimeMs = now;
        this.mHandler.postDelayed(this.mRetryRunnable, this.mNextRetryDurationMs);
        this.mNextRetryDurationMs = Math.min(2 * this.mNextRetryDurationMs, MAX_RETRY_DURATION_MS);
        this.mRetryCount++;
    }

    private boolean checkAndDeliverServiceDiedCbLocked() {
        if (this.mRetryType != 2 && (this.mRetryType != 3 || this.mRetryCount < 4)) {
            return false;
        }
        String str = this.TAG;
        Slog.e(str, "Service " + this.mComponent + " has died too much, not retrying.");
        if (this.mEventCb != null) {
            this.mHandler.post(new Runnable(System.currentTimeMillis()) {
                private final /* synthetic */ long f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    ManagedApplicationService.this.mEventCb.onServiceEvent(new ManagedApplicationService.LogEvent(this.f$1, ManagedApplicationService.this.mComponent, 4));
                }
            });
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void doRetry() {
        synchronized (this.mLock) {
            if (this.mConnection != null) {
                if (this.mRetrying) {
                    String str = this.TAG;
                    Slog.i(str, "Attempting to reconnect " + this.mComponent + "...");
                    disconnect();
                    if (!checkAndDeliverServiceDiedCbLocked()) {
                        queueRetryLocked();
                        connect();
                    }
                }
            }
        }
    }
}
