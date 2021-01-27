package com.android.server.attention;

import android.app.ActivityManager;
import android.attention.AttentionManagerInternal;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.DeviceConfig;
import android.service.attention.IAttentionCallback;
import android.service.attention.IAttentionService;
import android.text.TextUtils;
import android.util.Slog;
import android.util.SparseArray;
import android.util.StatsLog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.server.SystemService;
import com.android.server.attention.AttentionManagerService;
import com.android.server.pm.DumpState;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class AttentionManagerService extends SystemService {
    private static final long CONNECTION_TTL_MILLIS = 60000;
    private static final boolean DEBUG = false;
    private static final boolean DEFAULT_SERVICE_ENABLED = true;
    private static final String LOG_TAG = "AttentionManagerService";
    private static final String SERVICE_ENABLED = "service_enabled";
    private static final long STALE_AFTER_MILLIS = 5000;
    private static String sTestAttentionServicePackage;
    private AttentionHandler mAttentionHandler;
    @VisibleForTesting
    ComponentName mComponentName;
    private final Context mContext;
    private final Object mLock;
    private final PowerManager mPowerManager;
    @GuardedBy({"mLock"})
    private final SparseArray<UserState> mUserStates;

    public AttentionManagerService(Context context) {
        this(context, (PowerManager) context.getSystemService("power"), new Object(), null);
        this.mAttentionHandler = new AttentionHandler();
    }

    @VisibleForTesting
    AttentionManagerService(Context context, PowerManager powerManager, Object lock, AttentionHandler handler) {
        super(context);
        this.mUserStates = new SparseArray<>();
        this.mContext = (Context) Preconditions.checkNotNull(context);
        this.mPowerManager = powerManager;
        this.mLock = lock;
        this.mAttentionHandler = handler;
    }

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        if (phase == 500) {
            this.mContext.registerReceiver(new ScreenStateReceiver(), new IntentFilter("android.intent.action.SCREEN_OFF"));
        }
    }

    @Override // com.android.server.SystemService
    public void onStart() {
        publishBinderService("attention", new BinderService());
        publishLocalService(AttentionManagerInternal.class, new LocalService());
    }

    @Override // com.android.server.SystemService
    public void onSwitchUser(int userId) {
        cancelAndUnbindLocked(peekUserStateLocked(userId));
    }

    public static boolean isServiceConfigured(Context context) {
        return !TextUtils.isEmpty(getServiceConfigPackage(context));
    }

    private boolean isServiceAvailable() {
        if (this.mComponentName == null) {
            this.mComponentName = resolveAttentionService(this.mContext);
        }
        return this.mComponentName != null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isAttentionServiceSupported() {
        return isServiceEnabled() && isServiceAvailable();
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public boolean isServiceEnabled() {
        return DeviceConfig.getBoolean("attention_manager_service", SERVICE_ENABLED, true);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean checkAttention(long timeout, AttentionManagerInternal.AttentionCallbackInternal callbackInternal) {
        Preconditions.checkNotNull(callbackInternal);
        if (!isAttentionServiceSupported()) {
            Slog.w(LOG_TAG, "Trying to call checkAttention() on an unsupported device.");
            return false;
        } else if (!this.mPowerManager.isInteractive()) {
            return false;
        } else {
            synchronized (this.mLock) {
                long now = SystemClock.uptimeMillis();
                freeIfInactiveLocked();
                UserState userState = getOrCreateCurrentUserStateLocked();
                userState.bindLocked();
                AttentionCheckCache cache = userState.mAttentionCheckCache;
                if (cache != null && now < cache.mLastComputed + STALE_AFTER_MILLIS) {
                    callbackInternal.onSuccess(cache.mResult, cache.mTimestamp);
                    return true;
                } else if (userState.mCurrentAttentionCheck != null && (!userState.mCurrentAttentionCheck.mIsDispatched || !userState.mCurrentAttentionCheck.mIsFulfilled)) {
                    return false;
                } else {
                    userState.mCurrentAttentionCheck = createAttentionCheck(callbackInternal, userState);
                    if (userState.mService != null) {
                        try {
                            cancelAfterTimeoutLocked(timeout);
                            userState.mService.checkAttention(userState.mCurrentAttentionCheck.mIAttentionCallback);
                            userState.mCurrentAttentionCheck.mIsDispatched = true;
                        } catch (RemoteException e) {
                            Slog.e(LOG_TAG, "Cannot call into the AttentionService");
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
    }

    private AttentionCheck createAttentionCheck(final AttentionManagerInternal.AttentionCallbackInternal callbackInternal, final UserState userState) {
        return new AttentionCheck(callbackInternal, new IAttentionCallback.Stub() {
            /* class com.android.server.attention.AttentionManagerService.AnonymousClass1 */

            public void onSuccess(int result, long timestamp) {
                if (!userState.mCurrentAttentionCheck.mIsFulfilled) {
                    callbackInternal.onSuccess(result, timestamp);
                    userState.mCurrentAttentionCheck.mIsFulfilled = true;
                }
                synchronized (AttentionManagerService.this.mLock) {
                    userState.mAttentionCheckCache = new AttentionCheckCache(SystemClock.uptimeMillis(), result, timestamp);
                }
                StatsLog.write(143, result);
            }

            public void onFailure(int error) {
                if (!userState.mCurrentAttentionCheck.mIsFulfilled) {
                    callbackInternal.onFailure(error);
                    userState.mCurrentAttentionCheck.mIsFulfilled = true;
                }
                StatsLog.write(143, error);
            }
        });
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void cancelAttentionCheck(AttentionManagerInternal.AttentionCallbackInternal callbackInternal) {
        synchronized (this.mLock) {
            UserState userState = peekCurrentUserStateLocked();
            if (userState != null) {
                if (!userState.mCurrentAttentionCheck.mCallbackInternal.equals(callbackInternal)) {
                    Slog.w(LOG_TAG, "Cannot cancel a non-current request");
                } else {
                    cancel(userState);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @GuardedBy({"mLock"})
    @VisibleForTesting
    public void freeIfInactiveLocked() {
        this.mAttentionHandler.removeMessages(1);
        this.mAttentionHandler.sendEmptyMessageDelayed(1, 60000);
    }

    @GuardedBy({"mLock"})
    private void cancelAfterTimeoutLocked(long timeout) {
        this.mAttentionHandler.sendEmptyMessageDelayed(2, timeout);
    }

    /* access modifiers changed from: protected */
    @GuardedBy({"mLock"})
    @VisibleForTesting
    public UserState getOrCreateCurrentUserStateLocked() {
        return getOrCreateUserStateLocked(ActivityManager.getCurrentUser());
    }

    /* access modifiers changed from: protected */
    @GuardedBy({"mLock"})
    @VisibleForTesting
    public UserState getOrCreateUserStateLocked(int userId) {
        UserState result = this.mUserStates.get(userId);
        if (result != null) {
            return result;
        }
        UserState result2 = new UserState(userId, this.mContext, this.mLock, this.mAttentionHandler, this.mComponentName);
        this.mUserStates.put(userId, result2);
        return result2;
    }

    /* access modifiers changed from: protected */
    @GuardedBy({"mLock"})
    @VisibleForTesting
    public UserState peekCurrentUserStateLocked() {
        return peekUserStateLocked(ActivityManager.getCurrentUser());
    }

    @GuardedBy({"mLock"})
    private UserState peekUserStateLocked(int userId) {
        return this.mUserStates.get(userId);
    }

    private static String getServiceConfigPackage(Context context) {
        return context.getPackageManager().getAttentionServicePackageName();
    }

    /* access modifiers changed from: private */
    public static ComponentName resolveAttentionService(Context context) {
        String resolvedPackage;
        String serviceConfigPackage = getServiceConfigPackage(context);
        int flags = DumpState.DUMP_DEXOPT;
        if (!TextUtils.isEmpty(sTestAttentionServicePackage)) {
            resolvedPackage = sTestAttentionServicePackage;
            flags = 128;
        } else if (TextUtils.isEmpty(serviceConfigPackage)) {
            return null;
        } else {
            resolvedPackage = serviceConfigPackage;
        }
        ResolveInfo resolveInfo = context.getPackageManager().resolveService(new Intent("android.service.attention.AttentionService").setPackage(resolvedPackage), flags);
        if (resolveInfo == null || resolveInfo.serviceInfo == null) {
            Slog.wtf(LOG_TAG, String.format("Service %s not found in package %s", "android.service.attention.AttentionService", serviceConfigPackage));
            return null;
        }
        ServiceInfo serviceInfo = resolveInfo.serviceInfo;
        if ("android.permission.BIND_ATTENTION_SERVICE".equals(serviceInfo.permission)) {
            return serviceInfo.getComponentName();
        }
        Slog.e(LOG_TAG, String.format("Service %s should require %s permission. Found %s permission", serviceInfo.getComponentName(), "android.permission.BIND_ATTENTION_SERVICE", serviceInfo.permission));
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dumpInternal(IndentingPrintWriter ipw) {
        ipw.println("Attention Manager Service (dumpsys attention) state:\n");
        ipw.println("AttentionServicePackageName=" + getServiceConfigPackage(this.mContext));
        ipw.println("Resolved component:");
        if (this.mComponentName != null) {
            ipw.increaseIndent();
            ipw.println("Component=" + this.mComponentName.getPackageName());
            ipw.println("Class=" + this.mComponentName.getClassName());
            ipw.decreaseIndent();
        }
        synchronized (this.mLock) {
            int size = this.mUserStates.size();
            ipw.print("Number user states: ");
            ipw.println(size);
            if (size > 0) {
                ipw.increaseIndent();
                for (int i = 0; i < size; i++) {
                    ipw.print(i);
                    ipw.print(":");
                    this.mUserStates.valueAt(i).dump(ipw);
                    ipw.println();
                }
                ipw.decreaseIndent();
            }
        }
    }

    private final class LocalService extends AttentionManagerInternal {
        private LocalService() {
        }

        public boolean isAttentionServiceSupported() {
            return AttentionManagerService.this.isAttentionServiceSupported();
        }

        public boolean checkAttention(long timeout, AttentionManagerInternal.AttentionCallbackInternal callbackInternal) {
            return AttentionManagerService.this.checkAttention(timeout, callbackInternal);
        }

        public void cancelAttentionCheck(AttentionManagerInternal.AttentionCallbackInternal callbackInternal) {
            AttentionManagerService.this.cancelAttentionCheck(callbackInternal);
        }
    }

    /* access modifiers changed from: private */
    public static final class AttentionCheckCache {
        private final long mLastComputed;
        private final int mResult;
        private final long mTimestamp;

        AttentionCheckCache(long lastComputed, int result, long timestamp) {
            this.mLastComputed = lastComputed;
            this.mResult = result;
            this.mTimestamp = timestamp;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static final class AttentionCheck {
        private final AttentionManagerInternal.AttentionCallbackInternal mCallbackInternal;
        private final IAttentionCallback mIAttentionCallback;
        private boolean mIsDispatched;
        private boolean mIsFulfilled;

        AttentionCheck(AttentionManagerInternal.AttentionCallbackInternal callbackInternal, IAttentionCallback iAttentionCallback) {
            this.mCallbackInternal = callbackInternal;
            this.mIAttentionCallback = iAttentionCallback;
        }

        /* access modifiers changed from: package-private */
        public void cancelInternal() {
            this.mIsFulfilled = true;
            this.mCallbackInternal.onFailure(3);
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public static class UserState {
        @GuardedBy({"mLock"})
        AttentionCheckCache mAttentionCheckCache;
        private final Handler mAttentionHandler;
        @GuardedBy({"mLock"})
        private boolean mBinding;
        private final ComponentName mComponentName;
        private final AttentionServiceConnection mConnection = new AttentionServiceConnection();
        private final Context mContext;
        @GuardedBy({"mLock"})
        AttentionCheck mCurrentAttentionCheck;
        private final Object mLock;
        @GuardedBy({"mLock"})
        IAttentionService mService;
        private final int mUserId;

        UserState(int userId, Context context, Object lock, Handler handler, ComponentName componentName) {
            this.mUserId = userId;
            this.mContext = (Context) Preconditions.checkNotNull(context);
            this.mLock = Preconditions.checkNotNull(lock);
            this.mComponentName = (ComponentName) Preconditions.checkNotNull(componentName);
            this.mAttentionHandler = handler;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        @GuardedBy({"mLock"})
        private void handlePendingCallbackLocked() {
            if (!this.mCurrentAttentionCheck.mIsDispatched) {
                IAttentionService iAttentionService = this.mService;
                if (iAttentionService != null) {
                    try {
                        iAttentionService.checkAttention(this.mCurrentAttentionCheck.mIAttentionCallback);
                        this.mCurrentAttentionCheck.mIsDispatched = true;
                    } catch (RemoteException e) {
                        Slog.e(AttentionManagerService.LOG_TAG, "Cannot call into the AttentionService");
                    }
                } else {
                    this.mCurrentAttentionCheck.mCallbackInternal.onFailure(2);
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        @GuardedBy({"mLock"})
        private void bindLocked() {
            if (!this.mBinding && this.mService == null) {
                this.mBinding = true;
                this.mAttentionHandler.post(new Runnable() {
                    /* class com.android.server.attention.$$Lambda$AttentionManagerService$UserState$2cc0P7pJchsigKpbEq7IoxYFsSM */

                    @Override // java.lang.Runnable
                    public final void run() {
                        AttentionManagerService.UserState.this.lambda$bindLocked$0$AttentionManagerService$UserState();
                    }
                });
            }
        }

        public /* synthetic */ void lambda$bindLocked$0$AttentionManagerService$UserState() {
            this.mContext.bindServiceAsUser(new Intent("android.service.attention.AttentionService").setComponent(this.mComponentName), this.mConnection, 1, UserHandle.CURRENT);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void dump(IndentingPrintWriter pw) {
            pw.println("userId=" + this.mUserId);
            synchronized (this.mLock) {
                pw.println("binding=" + this.mBinding);
                pw.println("current attention check:");
                if (this.mCurrentAttentionCheck != null) {
                    pw.increaseIndent();
                    pw.println("is dispatched=" + this.mCurrentAttentionCheck.mIsDispatched);
                    pw.println("is fulfilled:=" + this.mCurrentAttentionCheck.mIsFulfilled);
                    pw.decreaseIndent();
                }
                pw.println("attention check cache:");
                if (this.mAttentionCheckCache != null) {
                    pw.increaseIndent();
                    pw.println("last computed=" + this.mAttentionCheckCache.mLastComputed);
                    pw.println("timestamp=" + this.mAttentionCheckCache.mTimestamp);
                    pw.println("result=" + this.mAttentionCheckCache.mResult);
                    pw.decreaseIndent();
                }
            }
        }

        /* access modifiers changed from: private */
        public class AttentionServiceConnection implements ServiceConnection {
            private AttentionServiceConnection() {
            }

            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName name, IBinder service) {
                init(IAttentionService.Stub.asInterface(service));
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName name) {
                cleanupService();
            }

            @Override // android.content.ServiceConnection
            public void onBindingDied(ComponentName name) {
                cleanupService();
            }

            @Override // android.content.ServiceConnection
            public void onNullBinding(ComponentName name) {
                cleanupService();
            }

            /* access modifiers changed from: package-private */
            public void cleanupService() {
                init(null);
            }

            private void init(IAttentionService service) {
                synchronized (UserState.this.mLock) {
                    UserState.this.mService = service;
                    UserState.this.mBinding = false;
                    UserState.this.handlePendingCallbackLocked();
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public class AttentionHandler extends Handler {
        private static final int ATTENTION_CHECK_TIMEOUT = 2;
        private static final int CHECK_CONNECTION_EXPIRATION = 1;

        AttentionHandler() {
            super(Looper.myLooper());
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                for (int i2 = 0; i2 < AttentionManagerService.this.mUserStates.size(); i2++) {
                    AttentionManagerService attentionManagerService = AttentionManagerService.this;
                    attentionManagerService.cancelAndUnbindLocked((UserState) attentionManagerService.mUserStates.valueAt(i2));
                }
            } else if (i == 2) {
                synchronized (AttentionManagerService.this.mLock) {
                    AttentionManagerService.this.cancel(AttentionManagerService.this.peekCurrentUserStateLocked());
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void cancel(UserState userState) {
        if (userState != null && userState.mCurrentAttentionCheck != null && !userState.mCurrentAttentionCheck.mIsFulfilled) {
            if (userState.mService == null) {
                userState.mCurrentAttentionCheck.cancelInternal();
                return;
            }
            try {
                userState.mService.cancelAttentionCheck(userState.mCurrentAttentionCheck.mIAttentionCallback);
            } catch (RemoteException e) {
                Slog.e(LOG_TAG, "Unable to cancel attention check");
                userState.mCurrentAttentionCheck.cancelInternal();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void cancelAndUnbindLocked(UserState userState) {
        synchronized (this.mLock) {
            if (userState != null) {
                cancel(userState);
                if (userState.mService != null) {
                    this.mAttentionHandler.post(new Runnable(userState) {
                        /* class com.android.server.attention.$$Lambda$AttentionManagerService$2UthIuCIdjigpPv1U5Dxw_fo4nY */
                        private final /* synthetic */ AttentionManagerService.UserState f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            AttentionManagerService.this.lambda$cancelAndUnbindLocked$0$AttentionManagerService(this.f$1);
                        }
                    });
                    userState.mConnection.cleanupService();
                    this.mUserStates.remove(userState.mUserId);
                }
            }
        }
    }

    public /* synthetic */ void lambda$cancelAndUnbindLocked$0$AttentionManagerService(UserState userState) {
        this.mContext.unbindService(userState.mConnection);
    }

    private final class ScreenStateReceiver extends BroadcastReceiver {
        private ScreenStateReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                AttentionManagerService attentionManagerService = AttentionManagerService.this;
                attentionManagerService.cancelAndUnbindLocked(attentionManagerService.peekCurrentUserStateLocked());
            }
        }
    }

    private final class AttentionManagerServiceShellCommand extends ShellCommand {
        final TestableAttentionCallbackInternal mTestableAttentionCallback;

        /* access modifiers changed from: package-private */
        public class TestableAttentionCallbackInternal extends AttentionManagerInternal.AttentionCallbackInternal {
            private int mLastCallbackCode = -1;

            TestableAttentionCallbackInternal() {
            }

            public void onSuccess(int result, long timestamp) {
                this.mLastCallbackCode = result;
            }

            public void onFailure(int error) {
                this.mLastCallbackCode = error;
            }

            public void reset() {
                this.mLastCallbackCode = -1;
            }

            public int getLastCallbackCode() {
                return this.mLastCallbackCode;
            }
        }

        private AttentionManagerServiceShellCommand() {
            this.mTestableAttentionCallback = new TestableAttentionCallbackInternal();
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* JADX WARNING: Removed duplicated region for block: B:50:0x0098 A[Catch:{ IllegalArgumentException -> 0x00b1 }] */
        /* JADX WARNING: Removed duplicated region for block: B:55:0x00a7 A[Catch:{ IllegalArgumentException -> 0x00b1 }] */
        public int onCommand(String cmd) {
            boolean z;
            if (cmd == null) {
                return handleDefaultCommands(cmd);
            }
            PrintWriter err = getErrPrintWriter();
            try {
                boolean z2 = false;
                switch (cmd.hashCode()) {
                    case -1208709968:
                        if (cmd.equals("getLastTestCallbackCode")) {
                            z = true;
                            break;
                        }
                        z = true;
                        break;
                    case -1002424240:
                        if (cmd.equals("getAttentionServiceComponent")) {
                            z = false;
                            break;
                        }
                        z = true;
                        break;
                    case -415045819:
                        if (cmd.equals("setTestableAttentionService")) {
                            z = true;
                            break;
                        }
                        z = true;
                        break;
                    case 3045982:
                        if (cmd.equals("call")) {
                            z = true;
                            break;
                        }
                        z = true;
                        break;
                    case 1193447472:
                        if (cmd.equals("clearTestableAttentionService")) {
                            z = true;
                            break;
                        }
                        z = true;
                        break;
                    default:
                        z = true;
                        break;
                }
                if (!z) {
                    return cmdResolveAttentionServiceComponent();
                }
                if (z) {
                    String nextArgRequired = getNextArgRequired();
                    int hashCode = nextArgRequired.hashCode();
                    if (hashCode != 763077136) {
                        if (hashCode == 1485997302 && nextArgRequired.equals("checkAttention")) {
                            if (!z2) {
                                return cmdCallCheckAttention();
                            }
                            if (z2) {
                                return cmdCallCancelAttention();
                            }
                            throw new IllegalArgumentException("Invalid argument");
                        }
                    } else if (nextArgRequired.equals("cancelCheckAttention")) {
                        z2 = true;
                        if (!z2) {
                        }
                    }
                    z2 = true;
                    if (!z2) {
                    }
                } else if (z) {
                    return cmdSetTestableAttentionService(getNextArgRequired());
                } else {
                    if (z) {
                        return cmdClearTestableAttentionService();
                    }
                    if (!z) {
                        return handleDefaultCommands(cmd);
                    }
                    return cmdGetLastTestCallbackCode();
                }
            } catch (IllegalArgumentException e) {
                err.println("Error: " + e.getMessage());
                return -1;
            }
        }

        private int cmdSetTestableAttentionService(String testingServicePackage) {
            PrintWriter out = getOutPrintWriter();
            String str = "false";
            if (TextUtils.isEmpty(testingServicePackage)) {
                out.println(str);
                return 0;
            }
            String unused = AttentionManagerService.sTestAttentionServicePackage = testingServicePackage;
            resetStates();
            if (AttentionManagerService.this.mComponentName != null) {
                str = "true";
            }
            out.println(str);
            return 0;
        }

        private int cmdClearTestableAttentionService() {
            String unused = AttentionManagerService.sTestAttentionServicePackage = "";
            this.mTestableAttentionCallback.reset();
            resetStates();
            return 0;
        }

        private int cmdCallCheckAttention() {
            getOutPrintWriter().println(AttentionManagerService.this.checkAttention(2000, this.mTestableAttentionCallback) ? "true" : "false");
            return 0;
        }

        private int cmdCallCancelAttention() {
            PrintWriter out = getOutPrintWriter();
            AttentionManagerService.this.cancelAttentionCheck(this.mTestableAttentionCallback);
            out.println("true");
            return 0;
        }

        private int cmdResolveAttentionServiceComponent() {
            PrintWriter out = getOutPrintWriter();
            ComponentName resolvedComponent = AttentionManagerService.resolveAttentionService(AttentionManagerService.this.mContext);
            out.println(resolvedComponent != null ? resolvedComponent.flattenToShortString() : "");
            return 0;
        }

        private int cmdGetLastTestCallbackCode() {
            getOutPrintWriter().println(this.mTestableAttentionCallback.getLastCallbackCode());
            return 0;
        }

        private void resetStates() {
            AttentionManagerService attentionManagerService = AttentionManagerService.this;
            attentionManagerService.mComponentName = AttentionManagerService.resolveAttentionService(attentionManagerService.mContext);
            AttentionManagerService.this.mUserStates.clear();
        }

        public void onHelp() {
            PrintWriter out = getOutPrintWriter();
            out.println("Attention commands: ");
            out.println("  setTestableAttentionService <service_package>: Bind to a custom implementation of attention service");
            out.println("  ---<service_package>:");
            out.println("       := Package containing the Attention Service implementation to bind to");
            out.println("  ---returns:");
            out.println("       := true, if was bound successfully");
            out.println("       := false, if was not bound successfully");
            out.println("  clearTestableAttentionService: Undo custom bindings. Revert to previous behavior");
            out.println("  getAttentionServiceComponent: Get the current service component string");
            out.println("  ---returns:");
            out.println("       := If valid, the component string (in shorten form) for the currently bound service.");
            out.println("       := else, empty string");
            out.println("  call checkAttention: Calls check attention");
            out.println("  ---returns:");
            out.println("       := true, if the call was successfully dispatched to the service implementation. (to see the result, call getLastTestCallbackCode)");
            out.println("       := false, otherwise");
            out.println("  call cancelCheckAttention: Cancels check attention");
            out.println("  getLastTestCallbackCode");
            out.println("  ---returns:");
            out.println("       := An integer, representing the last callback code received from the bounded implementation. If none, it will return -1");
        }
    }

    private final class BinderService extends Binder {
        AttentionManagerServiceShellCommand mAttentionManagerServiceShellCommand;

        private BinderService() {
            this.mAttentionManagerServiceShellCommand = new AttentionManagerServiceShellCommand();
        }

        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
            this.mAttentionManagerServiceShellCommand.exec(this, in, out, err, args, callback, resultReceiver);
        }

        /* access modifiers changed from: protected */
        @Override // android.os.Binder
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(AttentionManagerService.this.mContext, AttentionManagerService.LOG_TAG, pw)) {
                AttentionManagerService.this.dumpInternal(new IndentingPrintWriter(pw, "  "));
            }
        }
    }
}
