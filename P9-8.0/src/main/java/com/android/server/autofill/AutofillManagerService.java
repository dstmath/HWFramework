package com.android.server.autofill;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ShellCallback;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManagerInternal;
import android.provider.Settings.Secure;
import android.service.autofill.FillEventHistory;
import android.util.LocalLog;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillManagerInternal;
import android.view.autofill.AutofillValue;
import android.view.autofill.Helper;
import android.view.autofill.IAutoFillManager.Stub;
import android.view.autofill.IAutoFillManagerClient;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.PackageMonitor;
import com.android.internal.os.BackgroundThread;
import com.android.internal.os.IResultReceiver;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.Preconditions;
import com.android.server.FgThread;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.autofill.ui.AutoFillUI;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class AutofillManagerService extends SystemService {
    static final String RECEIVER_BUNDLE_EXTRA_SESSIONS = "sessions";
    private static final String TAG = "AutofillManagerService";
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                AutofillManagerService.this.mUi.hideAll(null);
            }
        }
    };
    private final Context mContext;
    @GuardedBy("mLock")
    private final SparseBooleanArray mDisabledUsers = new SparseBooleanArray();
    private final Object mLock = new Object();
    private final LocalLog mRequestsHistory = new LocalLog(20);
    @GuardedBy("mLock")
    private SparseArray<AutofillManagerServiceImpl> mServicesCache = new SparseArray();
    private final AutoFillUI mUi;

    final class AutoFillManagerServiceStub extends Stub {
        AutoFillManagerServiceStub() {
        }

        public int addClient(IAutoFillManagerClient client, int userId) {
            int flags;
            synchronized (AutofillManagerService.this.mLock) {
                flags = 0;
                if (AutofillManagerService.this.getServiceForUserLocked(userId).addClientLocked(client)) {
                    flags = 1;
                }
                if (Helper.sDebug) {
                    flags |= 2;
                }
                if (Helper.sVerbose) {
                    flags |= 4;
                }
            }
            return flags;
        }

        public void setAuthenticationResult(Bundle data, int sessionId, int authenticationId, int userId) {
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerService.this.getServiceForUserLocked(userId).setAuthenticationResultLocked(data, sessionId, authenticationId, getCallingUid());
            }
        }

        public void setHasCallback(int sessionId, int userId, boolean hasIt) {
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerService.this.getServiceForUserLocked(userId).setHasCallback(sessionId, getCallingUid(), hasIt);
            }
        }

        public int startSession(IBinder activityToken, IBinder appCallback, AutofillId autofillId, Rect bounds, AutofillValue value, int userId, boolean hasCallback, int flags, String packageName) {
            activityToken = (IBinder) Preconditions.checkNotNull(activityToken, "activityToken");
            appCallback = (IBinder) Preconditions.checkNotNull(appCallback, "appCallback");
            autofillId = (AutofillId) Preconditions.checkNotNull(autofillId, "autoFillId");
            packageName = (String) Preconditions.checkNotNull(packageName, "packageName");
            Preconditions.checkArgument(userId == UserHandle.getUserId(getCallingUid()), "userId");
            try {
                int startSessionLocked;
                AutofillManagerService.this.mContext.getPackageManager().getPackageInfoAsUser(packageName, 0, userId);
                synchronized (AutofillManagerService.this.mLock) {
                    startSessionLocked = AutofillManagerService.this.getServiceForUserLocked(userId).startSessionLocked(activityToken, getCallingUid(), appCallback, autofillId, bounds, value, hasCallback, flags, packageName);
                }
                return startSessionLocked;
            } catch (NameNotFoundException e) {
                throw new IllegalArgumentException(packageName + " is not a valid package", e);
            }
        }

        public FillEventHistory getFillEventHistory() throws RemoteException {
            UserHandle user = getCallingUserHandle();
            int uid = getCallingUid();
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerServiceImpl service = AutofillManagerService.this.peekServiceForUserLocked(user.getIdentifier());
                if (service != null) {
                    FillEventHistory fillEventHistory = service.getFillEventHistory(uid);
                    return fillEventHistory;
                }
                return null;
            }
        }

        public boolean restoreSession(int sessionId, IBinder activityToken, IBinder appCallback) throws RemoteException {
            activityToken = (IBinder) Preconditions.checkNotNull(activityToken, "activityToken");
            appCallback = (IBinder) Preconditions.checkNotNull(appCallback, "appCallback");
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerServiceImpl service = (AutofillManagerServiceImpl) AutofillManagerService.this.mServicesCache.get(UserHandle.getCallingUserId());
                if (service != null) {
                    boolean restoreSession = service.restoreSession(sessionId, getCallingUid(), activityToken, appCallback);
                    return restoreSession;
                }
                return false;
            }
        }

        public void updateSession(int sessionId, AutofillId autoFillId, Rect bounds, AutofillValue value, int action, int flags, int userId) {
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerServiceImpl service = AutofillManagerService.this.peekServiceForUserLocked(userId);
                if (service != null) {
                    service.updateSessionLocked(sessionId, getCallingUid(), autoFillId, bounds, value, action, flags);
                }
            }
        }

        public int updateOrRestartSession(IBinder activityToken, IBinder appCallback, AutofillId autoFillId, Rect bounds, AutofillValue value, int userId, boolean hasCallback, int flags, String packageName, int sessionId, int action) {
            boolean restart = false;
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerServiceImpl service = AutofillManagerService.this.peekServiceForUserLocked(userId);
                if (service != null) {
                    restart = service.updateSessionLocked(sessionId, getCallingUid(), autoFillId, bounds, value, action, flags);
                }
            }
            if (restart) {
                return startSession(activityToken, appCallback, autoFillId, bounds, value, userId, hasCallback, flags, packageName);
            }
            return sessionId;
        }

        public void finishSession(int sessionId, int userId) {
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerServiceImpl service = AutofillManagerService.this.peekServiceForUserLocked(userId);
                if (service != null) {
                    service.finishSessionLocked(sessionId, getCallingUid());
                }
            }
        }

        public void cancelSession(int sessionId, int userId) {
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerServiceImpl service = AutofillManagerService.this.peekServiceForUserLocked(userId);
                if (service != null) {
                    service.cancelSessionLocked(sessionId, getCallingUid());
                }
            }
        }

        public void disableOwnedAutofillServices(int userId) {
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerServiceImpl service = AutofillManagerService.this.peekServiceForUserLocked(userId);
                if (service != null) {
                    service.disableOwnedAutofillServicesLocked(Binder.getCallingUid());
                }
            }
        }

        public boolean isServiceSupported(int userId) {
            boolean z;
            synchronized (AutofillManagerService.this.mLock) {
                z = AutofillManagerService.this.mDisabledUsers.get(userId) ^ 1;
            }
            return z;
        }

        public boolean isServiceEnabled(int userId, String packageName) {
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerServiceImpl service = AutofillManagerService.this.peekServiceForUserLocked(userId);
                if (service == null) {
                    return false;
                }
                boolean equals = Objects.equals(packageName, service.getPackageName());
                return equals;
            }
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(AutofillManagerService.this.mContext, AutofillManagerService.TAG, pw)) {
                boolean showHistory = true;
                boolean uiOnly = false;
                if (args != null) {
                    for (String arg : args) {
                        if (arg.equals("--no-history")) {
                            showHistory = false;
                        } else if (arg.equals("--ui-only")) {
                            uiOnly = true;
                        } else if (arg.equals("--help")) {
                            pw.println("Usage: dumpsys autofill [--ui-only|--no-history]");
                            return;
                        } else {
                            Slog.w(AutofillManagerService.TAG, "Ignoring invalid dump arg: " + arg);
                        }
                    }
                }
                if (uiOnly) {
                    AutofillManagerService.this.mUi.dump(pw);
                    return;
                }
                boolean oldDebug = Helper.sDebug;
                try {
                    synchronized (AutofillManagerService.this.mLock) {
                        oldDebug = Helper.sDebug;
                        AutofillManagerService.this.setDebugLocked(true);
                        pw.print("Debug mode: ");
                        pw.println(oldDebug);
                        pw.print("Verbose mode: ");
                        pw.println(Helper.sVerbose);
                        pw.print("Disabled users: ");
                        pw.println(AutofillManagerService.this.mDisabledUsers);
                        pw.print("Max partitions per session: ");
                        pw.println(Helper.sPartitionMaxCount);
                        int size = AutofillManagerService.this.mServicesCache.size();
                        pw.print("Cached services: ");
                        if (size == 0) {
                            pw.println("none");
                        } else {
                            pw.println(size);
                            for (int i = 0; i < size; i++) {
                                pw.print("\nService at index ");
                                pw.println(i);
                                ((AutofillManagerServiceImpl) AutofillManagerService.this.mServicesCache.valueAt(i)).dumpLocked("  ", pw);
                            }
                        }
                        AutofillManagerService.this.mUi.dump(pw);
                    }
                    if (showHistory) {
                        pw.println("Requests history:");
                        AutofillManagerService.this.mRequestsHistory.reverseDump(fd, pw, args);
                    }
                    AutofillManagerService.this.setDebugLocked(oldDebug);
                } catch (Throwable th) {
                    AutofillManagerService.this.setDebugLocked(oldDebug);
                }
            }
        }

        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
            new AutofillManagerServiceShellCommand(AutofillManagerService.this).exec(this, in, out, err, args, callback, resultReceiver);
        }
    }

    private final class LocalService extends AutofillManagerInternal {
        /* synthetic */ LocalService(AutofillManagerService this$0, LocalService -this1) {
            this();
        }

        private LocalService() {
        }

        public void onBackKeyPressed() {
            if (Helper.sDebug) {
                Slog.d(AutofillManagerService.TAG, "onBackKeyPressed()");
            }
            AutofillManagerService.this.mUi.hideAll(null);
        }
    }

    private final class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
            ContentResolver resolver = AutofillManagerService.this.mContext.getContentResolver();
            resolver.registerContentObserver(Secure.getUriFor("autofill_service"), false, this, -1);
            resolver.registerContentObserver(Secure.getUriFor("user_setup_complete"), false, this, -1);
        }

        public void onChange(boolean selfChange, Uri uri, int userId) {
            if (Helper.sVerbose) {
                Slog.v(AutofillManagerService.TAG, "onChange(): uri=" + uri + ", userId=" + userId);
            }
            synchronized (AutofillManagerService.this.mLock) {
                AutofillManagerService.this.updateCachedServiceLocked(userId);
            }
        }
    }

    public AutofillManagerService(Context context) {
        super(context);
        this.mContext = context;
        this.mUi = new AutoFillUI(ActivityThread.currentActivityThread().getSystemUiContext());
        boolean debug = Build.IS_DEBUGGABLE;
        Slog.i(TAG, "Setting debug to " + debug);
        setDebugLocked(debug);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter, null, FgThread.getHandler());
        UserManagerInternal umi = (UserManagerInternal) LocalServices.getService(UserManagerInternal.class);
        List<UserInfo> users = ((UserManager) context.getSystemService(UserManager.class)).getUsers();
        for (int i = 0; i < users.size(); i++) {
            int userId = ((UserInfo) users.get(i)).id;
            boolean disabled = umi.getUserRestriction(userId, "no_autofill");
            if (disabled) {
                if (disabled) {
                    Slog.i(TAG, "Disabling Autofill for user " + userId);
                }
                this.mDisabledUsers.put(userId, disabled);
            }
        }
        umi.addUserRestrictionsListener(new -$Lambda$vJuxjgWyqc7YDAVrm5huZJMbjMg(this));
        startTrackingPackageChanges();
    }

    /* synthetic */ void lambda$-com_android_server_autofill_AutofillManagerService_5890(int userId, Bundle newRestrictions, Bundle prevRestrictions) {
        boolean disabledNow = newRestrictions.getBoolean("no_autofill", false);
        synchronized (this.mLock) {
            if (this.mDisabledUsers.get(userId) == disabledNow && Helper.sDebug) {
                Slog.d(TAG, "Autofill restriction did not change for user " + userId + ": " + Helper.bundleToString(newRestrictions));
                return;
            }
            Slog.i(TAG, "Updating Autofill for user " + userId + ": disabled=" + disabledNow);
            this.mDisabledUsers.put(userId, disabledNow);
            updateCachedServiceLocked(userId, disabledNow);
        }
    }

    private void startTrackingPackageChanges() {
        new PackageMonitor() {
            public void onSomePackagesChanged() {
                synchronized (AutofillManagerService.this.mLock) {
                    AutofillManagerService.this.updateCachedServiceLocked(getChangingUserId());
                }
            }

            public void onPackageUpdateFinished(String packageName, int uid) {
                synchronized (AutofillManagerService.this.mLock) {
                    if (packageName.equals(getActiveAutofillServicePackageName())) {
                        AutofillManagerService.this.removeCachedServiceLocked(getChangingUserId());
                    }
                }
            }

            public void onPackageRemoved(String packageName, int uid) {
                synchronized (AutofillManagerService.this.mLock) {
                    int userId = getChangingUserId();
                    AutofillManagerServiceImpl userState = AutofillManagerService.this.peekServiceForUserLocked(userId);
                    if (userState != null) {
                        ComponentName componentName = userState.getServiceComponentName();
                        if (componentName != null && packageName.equals(componentName.getPackageName())) {
                            handleActiveAutofillServiceRemoved(userId);
                        }
                    }
                }
            }

            public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
                synchronized (AutofillManagerService.this.mLock) {
                    String activePackageName = getActiveAutofillServicePackageName();
                    for (String pkg : packages) {
                        if (pkg.equals(activePackageName)) {
                            if (doit) {
                                AutofillManagerService.this.removeCachedServiceLocked(getChangingUserId());
                            } else {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            }

            private void handleActiveAutofillServiceRemoved(int userId) {
                AutofillManagerService.this.removeCachedServiceLocked(userId);
                Secure.putStringForUser(AutofillManagerService.this.mContext.getContentResolver(), "autofill_service", null, userId);
            }

            private String getActiveAutofillServicePackageName() {
                AutofillManagerServiceImpl userState = AutofillManagerService.this.peekServiceForUserLocked(getChangingUserId());
                if (userState == null) {
                    return null;
                }
                ComponentName serviceComponent = userState.getServiceComponentName();
                if (serviceComponent == null) {
                    return null;
                }
                return serviceComponent.getPackageName();
            }
        }.register(this.mContext, null, UserHandle.ALL, true);
    }

    public void onStart() {
        publishBinderService("autofill", new AutoFillManagerServiceStub());
        publishLocalService(AutofillManagerInternal.class, new LocalService(this, null));
    }

    public void onBootPhase(int phase) {
        if (phase == 600) {
            SettingsObserver settingsObserver = new SettingsObserver(BackgroundThread.getHandler());
        }
    }

    public void onUnlockUser(int userId) {
        synchronized (this.mLock) {
            updateCachedServiceLocked(userId);
        }
    }

    public void onSwitchUser(int userHandle) {
        if (Helper.sDebug) {
            Slog.d(TAG, "Hiding UI when user switched");
        }
        this.mUi.hideAll(null);
    }

    public void onCleanupUser(int userId) {
        synchronized (this.mLock) {
            removeCachedServiceLocked(userId);
        }
    }

    AutofillManagerServiceImpl getServiceForUserLocked(int userId) {
        int resolvedUserId = ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, 0, null, null);
        AutofillManagerServiceImpl service = (AutofillManagerServiceImpl) this.mServicesCache.get(resolvedUserId);
        if (service != null) {
            return service;
        }
        service = new AutofillManagerServiceImpl(this.mContext, this.mLock, this.mRequestsHistory, resolvedUserId, this.mUi, this.mDisabledUsers.get(resolvedUserId));
        this.mServicesCache.put(userId, service);
        return service;
    }

    AutofillManagerServiceImpl peekServiceForUserLocked(int userId) {
        return (AutofillManagerServiceImpl) this.mServicesCache.get(ActivityManager.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, false, null, null));
    }

    void destroySessions(int userId, IResultReceiver receiver) {
        Slog.i(TAG, "destroySessions() for userId " + userId);
        this.mContext.enforceCallingPermission("android.permission.MANAGE_AUTO_FILL", TAG);
        synchronized (this.mLock) {
            if (userId != -1) {
                AutofillManagerServiceImpl service = peekServiceForUserLocked(userId);
                if (service != null) {
                    service.destroySessionsLocked();
                }
            } else {
                int size = this.mServicesCache.size();
                for (int i = 0; i < size; i++) {
                    ((AutofillManagerServiceImpl) this.mServicesCache.valueAt(i)).destroySessionsLocked();
                }
            }
        }
        try {
            receiver.send(0, new Bundle());
        } catch (RemoteException e) {
        }
    }

    void listSessions(int userId, IResultReceiver receiver) {
        Slog.i(TAG, "listSessions() for userId " + userId);
        this.mContext.enforceCallingPermission("android.permission.MANAGE_AUTO_FILL", TAG);
        Bundle resultData = new Bundle();
        ArrayList<String> sessions = new ArrayList();
        synchronized (this.mLock) {
            if (userId != -1) {
                AutofillManagerServiceImpl service = peekServiceForUserLocked(userId);
                if (service != null) {
                    service.listSessionsLocked(sessions);
                }
            } else {
                int size = this.mServicesCache.size();
                for (int i = 0; i < size; i++) {
                    ((AutofillManagerServiceImpl) this.mServicesCache.valueAt(i)).listSessionsLocked(sessions);
                }
            }
        }
        resultData.putStringArrayList(RECEIVER_BUNDLE_EXTRA_SESSIONS, sessions);
        try {
            receiver.send(0, resultData);
        } catch (RemoteException e) {
        }
    }

    void reset() {
        Slog.i(TAG, "reset()");
        this.mContext.enforceCallingPermission("android.permission.MANAGE_AUTO_FILL", TAG);
        synchronized (this.mLock) {
            int size = this.mServicesCache.size();
            for (int i = 0; i < size; i++) {
                ((AutofillManagerServiceImpl) this.mServicesCache.valueAt(i)).destroyLocked();
            }
            this.mServicesCache.clear();
        }
    }

    void setLogLevel(int level) {
        Slog.i(TAG, "setLogLevel(): " + level);
        this.mContext.enforceCallingPermission("android.permission.MANAGE_AUTO_FILL", TAG);
        boolean debug = false;
        boolean verbose = false;
        if (level == 4) {
            verbose = true;
            debug = true;
        } else if (level == 2) {
            debug = true;
        }
        synchronized (this.mLock) {
            setDebugLocked(debug);
            setVerboseLocked(verbose);
        }
    }

    int getLogLevel() {
        this.mContext.enforceCallingPermission("android.permission.MANAGE_AUTO_FILL", TAG);
        synchronized (this.mLock) {
            if (Helper.sVerbose) {
                return 4;
            } else if (Helper.sDebug) {
                return 2;
            } else {
                return 0;
            }
        }
    }

    public int getMaxPartitions() {
        int i;
        this.mContext.enforceCallingPermission("android.permission.MANAGE_AUTO_FILL", TAG);
        synchronized (this.mLock) {
            i = Helper.sPartitionMaxCount;
        }
        return i;
    }

    public void setMaxPartitions(int max) {
        this.mContext.enforceCallingPermission("android.permission.MANAGE_AUTO_FILL", TAG);
        Slog.i(TAG, "setMaxPartitions(): " + max);
        synchronized (this.mLock) {
            Helper.sPartitionMaxCount = max;
        }
    }

    private void setDebugLocked(boolean debug) {
        Helper.sDebug = debug;
        Helper.sDebug = debug;
    }

    private void setVerboseLocked(boolean verbose) {
        Helper.sVerbose = verbose;
        Helper.sVerbose = verbose;
    }

    private void removeCachedServiceLocked(int userId) {
        AutofillManagerServiceImpl service = peekServiceForUserLocked(userId);
        if (service != null) {
            this.mServicesCache.delete(userId);
            service.destroyLocked();
        }
    }

    private void updateCachedServiceLocked(int userId) {
        updateCachedServiceLocked(userId, this.mDisabledUsers.get(userId));
    }

    private void updateCachedServiceLocked(int userId, boolean disabled) {
        AutofillManagerServiceImpl service = getServiceForUserLocked(userId);
        if (service != null) {
            service.updateLocked(disabled);
            if (!service.isEnabled()) {
                removeCachedServiceLocked(userId);
            }
        }
    }
}
