package com.android.server.autofill;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.metrics.LogMaker;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.autofill.AutofillServiceInfo;
import android.service.autofill.FieldClassification;
import android.service.autofill.FillEventHistory;
import android.service.autofill.FillResponse;
import android.service.autofill.UserData;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.LocalLog;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.view.autofill.IAutoFillManagerClient;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.MetricsLogger;
import com.android.server.LocalServices;
import com.android.server.autofill.AutofillManagerService;
import com.android.server.autofill.ui.AutoFillUI;
import com.android.server.job.controllers.JobStatus;
import com.android.server.os.HwBootFail;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

final class AutofillManagerServiceImpl {
    private static final int MAX_ABANDONED_SESSION_MILLIS = 30000;
    private static final int MAX_SESSION_ID_CREATE_TRIES = 2048;
    private static final String TAG = "AutofillManagerServiceImpl";
    private static final Random sRandom = new Random();
    private final AutofillManagerService.AutofillCompatState mAutofillCompatState;
    @GuardedBy("mLock")
    private RemoteCallbackList<IAutoFillManagerClient> mClients;
    private final Context mContext;
    @GuardedBy("mLock")
    private boolean mDisabled;
    @GuardedBy("mLock")
    private ArrayMap<ComponentName, Long> mDisabledActivities;
    @GuardedBy("mLock")
    private ArrayMap<String, Long> mDisabledApps;
    @GuardedBy("mLock")
    private FillEventHistory mEventHistory;
    private final FieldClassificationStrategy mFieldClassificationStrategy;
    private final Handler mHandler = new Handler(Looper.getMainLooper(), null, true);
    @GuardedBy("mLock")
    private AutofillServiceInfo mInfo;
    private long mLastPrune = 0;
    /* access modifiers changed from: private */
    public final Object mLock;
    private final MetricsLogger mMetricsLogger = new MetricsLogger();
    private final LocalLog mRequestsHistory;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public final SparseArray<Session> mSessions = new SparseArray<>();
    @GuardedBy("mLock")
    private boolean mSetupComplete;
    private final AutoFillUI mUi;
    private final LocalLog mUiLatencyHistory;
    @GuardedBy("mLock")
    private UserData mUserData;
    private final int mUserId;
    private final LocalLog mWtfHistory;

    private class PruneTask extends AsyncTask<Void, Void, Void> {
        private PruneTask() {
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(Void... ignored) {
            int numSessionsToRemove;
            SparseArray<IBinder> sessionsToRemove;
            int i;
            synchronized (AutofillManagerServiceImpl.this.mLock) {
                numSessionsToRemove = AutofillManagerServiceImpl.this.mSessions.size();
                sessionsToRemove = new SparseArray<>(numSessionsToRemove);
                i = 0;
                for (int i2 = 0; i2 < numSessionsToRemove; i2++) {
                    Session session = (Session) AutofillManagerServiceImpl.this.mSessions.valueAt(i2);
                    sessionsToRemove.put(session.id, session.getActivityTokenLocked());
                }
            }
            int numSessionsToRemove2 = numSessionsToRemove;
            SparseArray<IBinder> sessionsToRemove2 = sessionsToRemove;
            SparseArray<IBinder> sessionsToRemove3 = ActivityManager.getService();
            int numSessionsToRemove3 = numSessionsToRemove2;
            int i3 = 0;
            while (i3 < numSessionsToRemove3) {
                try {
                    if (sessionsToRemove3.getActivityClassForToken(sessionsToRemove2.valueAt(i3)) != null) {
                        sessionsToRemove2.removeAt(i3);
                        i3--;
                        numSessionsToRemove3--;
                    }
                } catch (RemoteException e) {
                    Slog.w(AutofillManagerServiceImpl.TAG, "Cannot figure out if activity is finished", e);
                }
                i3++;
            }
            synchronized (AutofillManagerServiceImpl.this.mLock) {
                while (true) {
                    int i4 = i;
                    if (i4 < numSessionsToRemove3) {
                        try {
                            Session sessionToRemove = (Session) AutofillManagerServiceImpl.this.mSessions.get(sessionsToRemove2.keyAt(i4));
                            if (sessionToRemove != null && sessionsToRemove2.valueAt(i4) == sessionToRemove.getActivityTokenLocked()) {
                                if (!sessionToRemove.isSavingLocked()) {
                                    if (Helper.sDebug) {
                                        Slog.i(AutofillManagerServiceImpl.TAG, "Prune session " + sessionToRemove.id + " (" + sessionToRemove.getActivityTokenLocked() + ")");
                                    }
                                    sessionToRemove.removeSelfLocked();
                                } else if (Helper.sVerbose) {
                                    Slog.v(AutofillManagerServiceImpl.TAG, "Session " + sessionToRemove.id + " is saving");
                                }
                            }
                            i = i4 + 1;
                        } finally {
                        }
                    }
                }
            }
            return null;
        }
    }

    AutofillManagerServiceImpl(Context context, Object lock, LocalLog requestsHistory, LocalLog uiLatencyHistory, LocalLog wtfHistory, int userId, AutoFillUI ui, AutofillManagerService.AutofillCompatState autofillCompatState, boolean disabled) {
        this.mContext = context;
        this.mLock = lock;
        this.mRequestsHistory = requestsHistory;
        this.mUiLatencyHistory = uiLatencyHistory;
        this.mWtfHistory = wtfHistory;
        this.mUserId = userId;
        this.mUi = ui;
        this.mFieldClassificationStrategy = new FieldClassificationStrategy(context, userId);
        this.mAutofillCompatState = autofillCompatState;
        updateLocked(disabled);
    }

    /* access modifiers changed from: package-private */
    public CharSequence getServiceName() {
        String packageName = getServicePackageName();
        if (packageName == null) {
            return null;
        }
        try {
            PackageManager pm = this.mContext.getPackageManager();
            return pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0));
        } catch (Exception e) {
            Slog.e(TAG, "Could not get label for " + packageName + ": " + e);
            return packageName;
        }
    }

    @GuardedBy("mLock")
    private int getServiceUidLocked() {
        if (this.mInfo != null) {
            return this.mInfo.getServiceInfo().applicationInfo.uid;
        }
        Slog.w(TAG, "getServiceUidLocked(): no mInfo");
        return -1;
    }

    /* access modifiers changed from: package-private */
    public String[] getUrlBarResourceIdsForCompatMode(String packageName) {
        return this.mAutofillCompatState.getUrlBarResourceIds(packageName, this.mUserId);
    }

    /* access modifiers changed from: package-private */
    public String getServicePackageName() {
        ComponentName serviceComponent = getServiceComponentName();
        if (serviceComponent != null) {
            return serviceComponent.getPackageName();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public ComponentName getServiceComponentName() {
        synchronized (this.mLock) {
            if (this.mInfo == null) {
                return null;
            }
            ComponentName componentName = this.mInfo.getServiceInfo().getComponentName();
            return componentName;
        }
    }

    private boolean isSetupCompletedLocked() {
        return "1".equals(Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "user_setup_complete", this.mUserId));
    }

    private String getComponentNameFromSettings() {
        return Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "autofill_service", this.mUserId);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public void updateLocked(boolean disabled) {
        boolean wasEnabled = isEnabledLocked();
        if (Helper.sVerbose) {
            Slog.v(TAG, "updateLocked(u=" + this.mUserId + "): wasEnabled=" + wasEnabled + ", mSetupComplete= " + this.mSetupComplete + ", disabled=" + disabled + ", mDisabled=" + this.mDisabled);
        }
        this.mSetupComplete = isSetupCompletedLocked();
        this.mDisabled = disabled;
        ComponentName serviceComponent = null;
        ServiceInfo serviceInfo = null;
        String componentName = getComponentNameFromSettings();
        if (!TextUtils.isEmpty(componentName)) {
            try {
                serviceComponent = ComponentName.unflattenFromString(componentName);
                serviceInfo = AppGlobals.getPackageManager().getServiceInfo(serviceComponent, 0, this.mUserId);
                if (serviceInfo == null) {
                    Slog.e(TAG, "Bad AutofillService name: " + componentName);
                }
            } catch (RemoteException | RuntimeException e) {
                Slog.e(TAG, "Error getting service info for '" + componentName + "': " + e);
                serviceInfo = null;
            }
        }
        if (serviceInfo != null) {
            try {
                this.mInfo = new AutofillServiceInfo(this.mContext, serviceComponent, this.mUserId);
                if (Helper.sDebug) {
                    Slog.d(TAG, "Set component for user " + this.mUserId + " as " + this.mInfo);
                }
            } catch (Exception e2) {
                Slog.e(TAG, "Bad AutofillServiceInfo for '" + componentName + "': " + e2);
                this.mInfo = null;
            }
        } else {
            this.mInfo = null;
            if (Helper.sDebug) {
                Slog.d(TAG, "Reset component for user " + this.mUserId + " (" + componentName + ")");
            }
        }
        boolean isEnabled = isEnabledLocked();
        if (wasEnabled != isEnabled) {
            if (!isEnabled) {
                for (int i = this.mSessions.size() - 1; i >= 0; i--) {
                    this.mSessions.valueAt(i).removeSelfLocked();
                }
            }
            sendStateToClients(false);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public boolean addClientLocked(IAutoFillManagerClient client) {
        if (this.mClients == null) {
            this.mClients = new RemoteCallbackList<>();
        }
        this.mClients.register(client);
        return isEnabledLocked();
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public void removeClientLocked(IAutoFillManagerClient client) {
        if (this.mClients != null) {
            this.mClients.unregister(client);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public void setAuthenticationResultLocked(Bundle data, int sessionId, int authenticationId, int uid) {
        if (isEnabledLocked()) {
            Session session = this.mSessions.get(sessionId);
            if (session != null && uid == session.uid) {
                session.setAuthenticationResultLocked(data, authenticationId);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setHasCallback(int sessionId, int uid, boolean hasIt) {
        if (isEnabledLocked()) {
            Session session = this.mSessions.get(sessionId);
            if (session != null && uid == session.uid) {
                synchronized (this.mLock) {
                    session.setHasCallbackLocked(hasIt);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public int startSessionLocked(IBinder activityToken, int uid, IBinder appCallbackToken, AutofillId autofillId, Rect virtualBounds, AutofillValue value, boolean hasCallback, ComponentName componentName, boolean compatMode, boolean bindInstantServiceAllowed, int flags) {
        IBinder iBinder;
        int i = flags;
        if (!isEnabledLocked()) {
            return 0;
        }
        String shortComponentName = componentName.toShortString();
        ComponentName componentName2 = componentName;
        if (isAutofillDisabledLocked(componentName2)) {
            if (Helper.sDebug) {
                Slog.d(TAG, "startSession(" + shortComponentName + "): ignored because disabled by service");
            }
            try {
                IAutoFillManagerClient.Stub.asInterface(appCallbackToken).setSessionFinished(4);
            } catch (RemoteException e) {
                RemoteException remoteException = e;
                Slog.w(TAG, "Could not notify " + shortComponentName + " that it's disabled: " + e);
            }
            return Integer.MIN_VALUE;
        }
        if (Helper.sVerbose) {
            StringBuilder sb = new StringBuilder();
            sb.append("startSession(): token=");
            iBinder = activityToken;
            sb.append(iBinder);
            sb.append(", flags=");
            sb.append(i);
            Slog.v(TAG, sb.toString());
        } else {
            iBinder = activityToken;
        }
        pruneAbandonedSessionsLocked();
        Session newSession = createSessionByTokenLocked(iBinder, uid, appCallbackToken, hasCallback, componentName2, compatMode, bindInstantServiceAllowed, i);
        if (newSession == null) {
            return Integer.MIN_VALUE;
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append("id=");
        sb2.append(newSession.id);
        sb2.append(" uid=");
        sb2.append(uid);
        sb2.append(" a=");
        sb2.append(shortComponentName);
        sb2.append(" s=");
        sb2.append(this.mInfo.getServiceInfo().packageName);
        sb2.append(" u=");
        sb2.append(this.mUserId);
        sb2.append(" i=");
        AutofillId autofillId2 = autofillId;
        sb2.append(autofillId2);
        sb2.append(" b=");
        Rect rect = virtualBounds;
        sb2.append(rect);
        sb2.append(" hc=");
        sb2.append(hasCallback);
        sb2.append(" f=");
        sb2.append(i);
        String historyItem = sb2.toString();
        this.mRequestsHistory.log(historyItem);
        String str = historyItem;
        newSession.updateLocked(autofillId2, rect, value, 1, i);
        return newSession.id;
    }

    @GuardedBy("mLock")
    private void pruneAbandonedSessionsLocked() {
        long now = System.currentTimeMillis();
        if (this.mLastPrune < now - 30000) {
            this.mLastPrune = now;
            if (this.mSessions.size() > 0) {
                new PruneTask().execute(new Void[0]);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public void setAutofillFailureLocked(int sessionId, int uid, List<AutofillId> ids) {
        if (isEnabledLocked()) {
            Session session = this.mSessions.get(sessionId);
            if (session == null || uid != session.uid) {
                Slog.v(TAG, "setAutofillFailure(): no session for " + sessionId + "(" + uid + ")");
                return;
            }
            session.setAutofillFailureLocked(ids);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public void finishSessionLocked(int sessionId, int uid) {
        if (isEnabledLocked()) {
            Session session = this.mSessions.get(sessionId);
            if (session == null || uid != session.uid) {
                if (Helper.sVerbose) {
                    Slog.v(TAG, "finishSessionLocked(): no session for " + sessionId + "(" + uid + ")");
                }
                return;
            }
            session.logContextCommitted();
            boolean finished = session.showSaveLocked();
            if (Helper.sVerbose) {
                Slog.v(TAG, "finishSessionLocked(): session finished on save? " + finished);
            }
            if (finished) {
                session.removeSelfLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public void cancelSessionLocked(int sessionId, int uid) {
        if (isEnabledLocked()) {
            Session session = this.mSessions.get(sessionId);
            if (session == null || uid != session.uid) {
                Slog.w(TAG, "cancelSessionLocked(): no session for " + sessionId + "(" + uid + ")");
                return;
            }
            session.removeSelfLocked();
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public void disableOwnedAutofillServicesLocked(int uid) {
        Slog.i(TAG, "disableOwnedServices(" + uid + "): " + this.mInfo);
        if (this.mInfo != null) {
            ServiceInfo serviceInfo = this.mInfo.getServiceInfo();
            if (serviceInfo.applicationInfo.uid != uid) {
                Slog.w(TAG, "disableOwnedServices(): ignored when called by UID " + uid + " instead of " + serviceInfo.applicationInfo.uid + " for service " + this.mInfo);
                return;
            }
            long identity = Binder.clearCallingIdentity();
            try {
                String autoFillService = getComponentNameFromSettings();
                ComponentName componentName = serviceInfo.getComponentName();
                if (componentName.equals(ComponentName.unflattenFromString(autoFillService))) {
                    this.mMetricsLogger.action(1135, componentName.getPackageName());
                    Settings.Secure.putStringForUser(this.mContext.getContentResolver(), "autofill_service", null, this.mUserId);
                    destroySessionsLocked();
                } else {
                    Slog.w(TAG, "disableOwnedServices(): ignored because current service (" + serviceInfo + ") does not match Settings (" + autoFillService + ")");
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    @GuardedBy("mLock")
    private Session createSessionByTokenLocked(IBinder activityToken, int uid, IBinder appCallbackToken, boolean hasCallback, ComponentName componentName, boolean compatMode, boolean bindInstantServiceAllowed, int flags) {
        AutofillManagerServiceImpl autofillManagerServiceImpl = this;
        int tries = 0;
        while (true) {
            int tries2 = tries + 1;
            if (tries2 > 2048) {
                Slog.w(TAG, "Cannot create session in 2048 tries");
                return null;
            }
            int sessionId = sRandom.nextInt();
            if (sessionId == Integer.MIN_VALUE || autofillManagerServiceImpl.mSessions.indexOfKey(sessionId) >= 0) {
                autofillManagerServiceImpl = autofillManagerServiceImpl;
                tries = tries2;
            } else {
                autofillManagerServiceImpl.assertCallerLocked(componentName, compatMode);
                AutoFillUI autoFillUI = autofillManagerServiceImpl.mUi;
                Context context = autofillManagerServiceImpl.mContext;
                Handler handler = autofillManagerServiceImpl.mHandler;
                int i = autofillManagerServiceImpl.mUserId;
                Object obj = autofillManagerServiceImpl.mLock;
                LocalLog localLog = autofillManagerServiceImpl.mUiLatencyHistory;
                LocalLog localLog2 = localLog;
                int i2 = sessionId;
                int i3 = tries2;
                Session newSession = new Session(autofillManagerServiceImpl, autoFillUI, context, handler, i, obj, sessionId, uid, activityToken, appCallbackToken, hasCallback, localLog2, autofillManagerServiceImpl.mWtfHistory, autofillManagerServiceImpl.mInfo.getServiceInfo().getComponentName(), componentName, compatMode, bindInstantServiceAllowed, flags);
                this.mSessions.put(newSession.id, newSession);
                return newSession;
            }
        }
    }

    private void assertCallerLocked(ComponentName componentName, boolean compatMode) {
        String callingPackage;
        String packageName = componentName.getPackageName();
        PackageManager pm = this.mContext.getPackageManager();
        int callingUid = Binder.getCallingUid();
        try {
            if (callingUid != pm.getPackageUidAsUser(packageName, UserHandle.getCallingUserId()) && !((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).hasRunningActivity(callingUid, packageName)) {
                String[] packages = pm.getPackagesForUid(callingUid);
                if (packages != null) {
                    callingPackage = packages[0];
                } else {
                    callingPackage = "uid-" + callingUid;
                }
                Slog.w(TAG, "App (package=" + callingPackage + ", UID=" + callingUid + ") passed component (" + componentName + ") owned by UID " + packageUid);
                LogMaker log = new LogMaker(948).setPackageName(callingPackage).addTaggedData(908, getServicePackageName()).addTaggedData(949, componentName == null ? "null" : componentName.flattenToShortString());
                if (compatMode) {
                    log.addTaggedData(1414, 1);
                }
                this.mMetricsLogger.write(log);
                throw new SecurityException("Invalid component: " + componentName);
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new SecurityException("Could not verify UID for " + componentName);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean restoreSession(int sessionId, int uid, IBinder activityToken, IBinder appCallback) {
        Session session = this.mSessions.get(sessionId);
        if (session == null || uid != session.uid) {
            return false;
        }
        session.switchActivity(activityToken, appCallback);
        return true;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public boolean updateSessionLocked(int sessionId, int uid, AutofillId autofillId, Rect virtualBounds, AutofillValue value, int action, int flags) {
        Session session = this.mSessions.get(sessionId);
        if (session != null && session.uid == uid) {
            session.updateLocked(autofillId, virtualBounds, value, action, flags);
            return false;
        } else if ((flags & 1) != 0) {
            if (Helper.sDebug) {
                Slog.d(TAG, "restarting session " + sessionId + " due to manual request on " + autofillId);
            }
            return true;
        } else {
            if (Helper.sVerbose) {
                Slog.v(TAG, "updateSessionLocked(): session gone for " + sessionId + "(" + uid + ")");
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public void removeSessionLocked(int sessionId) {
        this.mSessions.remove(sessionId);
    }

    /* access modifiers changed from: package-private */
    public void handleSessionSave(Session session) {
        synchronized (this.mLock) {
            if (this.mSessions.get(session.id) == null) {
                Slog.w(TAG, "handleSessionSave(): already gone: " + session.id);
                return;
            }
            session.callSaveLocked();
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0049, code lost:
        if (com.android.server.autofill.Helper.sDebug == false) goto L_0x0071;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004b, code lost:
        android.util.Slog.d(TAG, "No pending Save UI for token " + r7 + " and operation " + android.util.DebugUtils.flagsToString(android.view.autofill.AutofillManager.class, "PENDING_UI_OPERATION_", r6));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0071, code lost:
        return;
     */
    public void onPendingSaveUi(int operation, IBinder token) {
        if (Helper.sVerbose) {
            Slog.v(TAG, "onPendingSaveUi(" + operation + "): " + token);
        }
        synchronized (this.mLock) {
            for (int i = this.mSessions.size() - 1; i >= 0; i--) {
                Session session = this.mSessions.valueAt(i);
                if (session.isSaveUiPendingForTokenLocked(token)) {
                    session.onPendingSaveUi(operation, token);
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public void handlePackageUpdateLocked(String packageName) {
        ServiceInfo serviceInfo = this.mFieldClassificationStrategy.getServiceInfo();
        if (serviceInfo != null && serviceInfo.packageName.equals(packageName)) {
            resetExtServiceLocked();
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public void resetExtServiceLocked() {
        if (Helper.sVerbose) {
            Slog.v(TAG, "reset autofill service.");
        }
        this.mFieldClassificationStrategy.reset();
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public void destroyLocked() {
        if (Helper.sVerbose) {
            Slog.v(TAG, "destroyLocked()");
        }
        resetExtServiceLocked();
        int numSessions = this.mSessions.size();
        ArraySet<RemoteFillService> remoteFillServices = new ArraySet<>(numSessions);
        for (int i = 0; i < numSessions; i++) {
            RemoteFillService remoteFillService = this.mSessions.valueAt(i).destroyLocked();
            if (remoteFillService != null) {
                remoteFillServices.add(remoteFillService);
            }
        }
        this.mSessions.clear();
        for (int i2 = 0; i2 < remoteFillServices.size(); i2++) {
            remoteFillServices.valueAt(i2).destroy();
        }
        sendStateToClients(true);
        if (this.mClients != null) {
            this.mClients.kill();
            this.mClients = null;
        }
    }

    /* access modifiers changed from: package-private */
    public CharSequence getServiceLabel() {
        return this.mInfo.getServiceInfo().loadSafeLabel(this.mContext.getPackageManager(), 0.0f, 5);
    }

    /* access modifiers changed from: package-private */
    public Drawable getServiceIcon() {
        return this.mInfo.getServiceInfo().loadIcon(this.mContext.getPackageManager());
    }

    /* access modifiers changed from: package-private */
    public void setLastResponse(int sessionId, FillResponse response) {
        synchronized (this.mLock) {
            this.mEventHistory = new FillEventHistory(sessionId, response.getClientState());
        }
    }

    /* access modifiers changed from: package-private */
    public void resetLastResponse() {
        synchronized (this.mLock) {
            this.mEventHistory = null;
        }
    }

    @GuardedBy("mLock")
    private boolean isValidEventLocked(String method, int sessionId) {
        if (this.mEventHistory == null) {
            Slog.w(TAG, method + ": not logging event because history is null");
            return false;
        } else if (sessionId == this.mEventHistory.getSessionId()) {
            return true;
        } else {
            if (Helper.sDebug) {
                Slog.d(TAG, method + ": not logging event for session " + sessionId + " because tracked session is " + this.mEventHistory.getSessionId());
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void setAuthenticationSelected(int sessionId, Bundle clientState) {
        synchronized (this.mLock) {
            try {
                if (isValidEventLocked("setAuthenticationSelected()", sessionId)) {
                    FillEventHistory fillEventHistory = this.mEventHistory;
                    FillEventHistory.Event event = r4;
                    FillEventHistory.Event event2 = new FillEventHistory.Event(2, null, clientState, null, null, null, null, null, null, null, null);
                    fillEventHistory.addEvent(event);
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void logDatasetAuthenticationSelected(String selectedDataset, int sessionId, Bundle clientState) {
        synchronized (this.mLock) {
            try {
                if (isValidEventLocked("logDatasetAuthenticationSelected()", sessionId)) {
                    FillEventHistory fillEventHistory = this.mEventHistory;
                    FillEventHistory.Event event = r4;
                    FillEventHistory.Event event2 = new FillEventHistory.Event(1, selectedDataset, clientState, null, null, null, null, null, null, null, null);
                    fillEventHistory.addEvent(event);
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void logSaveShown(int sessionId, Bundle clientState) {
        synchronized (this.mLock) {
            try {
                if (isValidEventLocked("logSaveShown()", sessionId)) {
                    FillEventHistory fillEventHistory = this.mEventHistory;
                    FillEventHistory.Event event = r4;
                    FillEventHistory.Event event2 = new FillEventHistory.Event(3, null, clientState, null, null, null, null, null, null, null, null);
                    fillEventHistory.addEvent(event);
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void logDatasetSelected(String selectedDataset, int sessionId, Bundle clientState) {
        synchronized (this.mLock) {
            try {
                if (isValidEventLocked("logDatasetSelected()", sessionId)) {
                    FillEventHistory fillEventHistory = this.mEventHistory;
                    FillEventHistory.Event event = r4;
                    FillEventHistory.Event event2 = new FillEventHistory.Event(0, selectedDataset, clientState, null, null, null, null, null, null, null, null);
                    fillEventHistory.addEvent(event);
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public void logContextCommittedLocked(int sessionId, Bundle clientState, ArrayList<String> selectedDatasets, ArraySet<String> ignoredDatasets, ArrayList<AutofillId> changedFieldIds, ArrayList<String> changedDatasetIds, ArrayList<AutofillId> manuallyFilledFieldIds, ArrayList<ArrayList<String>> manuallyFilledDatasetIds, ComponentName appComponentName, boolean compatMode) {
        logContextCommittedLocked(sessionId, clientState, selectedDatasets, ignoredDatasets, changedFieldIds, changedDatasetIds, manuallyFilledFieldIds, manuallyFilledDatasetIds, null, null, appComponentName, compatMode);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public void logContextCommittedLocked(int sessionId, Bundle clientState, ArrayList<String> selectedDatasets, ArraySet<String> ignoredDatasets, ArrayList<AutofillId> changedFieldIds, ArrayList<String> changedDatasetIds, ArrayList<AutofillId> manuallyFilledFieldIds, ArrayList<ArrayList<String>> manuallyFilledDatasetIds, ArrayList<AutofillId> detectedFieldIdsList, ArrayList<FieldClassification> detectedFieldClassificationsList, ComponentName appComponentName, boolean compatMode) {
        ArraySet<String> arraySet;
        ArrayList<String> arrayList;
        AutofillId[] detectedFieldsIds;
        int i = sessionId;
        ArrayList<AutofillId> arrayList2 = detectedFieldIdsList;
        ArrayList<FieldClassification> arrayList3 = detectedFieldClassificationsList;
        boolean z = compatMode;
        if (isValidEventLocked("logDatasetNotSelected()", i)) {
            if (Helper.sVerbose) {
                StringBuilder sb = new StringBuilder();
                sb.append("logContextCommitted() with FieldClassification: id=");
                sb.append(i);
                sb.append(", selectedDatasets=");
                arrayList = selectedDatasets;
                sb.append(arrayList);
                sb.append(", ignoredDatasetIds=");
                arraySet = ignoredDatasets;
                sb.append(arraySet);
                sb.append(", changedAutofillIds=");
                sb.append(changedFieldIds);
                sb.append(", changedDatasetIds=");
                sb.append(changedDatasetIds);
                sb.append(", manuallyFilledFieldIds=");
                sb.append(manuallyFilledFieldIds);
                sb.append(", detectedFieldIds=");
                sb.append(arrayList2);
                sb.append(", detectedFieldClassifications=");
                sb.append(arrayList3);
                sb.append(", appComponentName=");
                sb.append(appComponentName.toShortString());
                sb.append(", compatMode=");
                sb.append(z);
                Slog.v(TAG, sb.toString());
            } else {
                arrayList = selectedDatasets;
                arraySet = ignoredDatasets;
                ArrayList<AutofillId> arrayList4 = changedFieldIds;
                ArrayList<String> arrayList5 = changedDatasetIds;
                ArrayList<AutofillId> arrayList6 = manuallyFilledFieldIds;
            }
            AutofillId[] detectedFieldsIds2 = null;
            FieldClassification[] detectedFieldClassifications = null;
            if (arrayList2 != null) {
                AutofillId[] detectedFieldsIds3 = new AutofillId[detectedFieldIdsList.size()];
                arrayList2.toArray(detectedFieldsIds3);
                detectedFieldClassifications = new FieldClassification[detectedFieldClassificationsList.size()];
                arrayList3.toArray(detectedFieldClassifications);
                int numberFields = detectedFieldsIds3.length;
                float totalScore = 0.0f;
                int totalSize = 0;
                int i2 = 0;
                while (i2 < numberFields) {
                    List<FieldClassification.Match> matches = detectedFieldClassifications[i2].getMatches();
                    int size = matches.size();
                    totalSize += size;
                    float totalScore2 = totalScore;
                    int j = 0;
                    while (true) {
                        detectedFieldsIds = detectedFieldsIds3;
                        int j2 = j;
                        if (j2 >= size) {
                            break;
                        }
                        totalScore2 += matches.get(j2).getScore();
                        j = j2 + 1;
                        detectedFieldsIds3 = detectedFieldsIds;
                        matches = matches;
                    }
                    i2++;
                    totalScore = totalScore2;
                    detectedFieldsIds3 = detectedFieldsIds;
                    ArrayList<AutofillId> arrayList7 = detectedFieldIdsList;
                    ArrayList<FieldClassification> arrayList8 = detectedFieldClassificationsList;
                }
                this.mMetricsLogger.write(Helper.newLogMaker(1273, appComponentName, getServicePackageName(), i, z).setCounterValue(numberFields).addTaggedData(1274, Integer.valueOf((int) ((100.0f * totalScore) / ((float) totalSize)))));
                detectedFieldsIds2 = detectedFieldsIds3;
            } else {
                ComponentName componentName = appComponentName;
            }
            FillEventHistory fillEventHistory = this.mEventHistory;
            FillEventHistory.Event event = new FillEventHistory.Event(4, null, clientState, arrayList, arraySet, changedFieldIds, changedDatasetIds, manuallyFilledFieldIds, manuallyFilledDatasetIds, detectedFieldsIds2, detectedFieldClassifications);
            fillEventHistory.addEvent(event);
            return;
        }
        ArrayList<String> arrayList9 = selectedDatasets;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0015, code lost:
        return null;
     */
    public FillEventHistory getFillEventHistory(int callingUid) {
        synchronized (this.mLock) {
            if (this.mEventHistory != null && isCalledByServiceLocked("getFillEventHistory", callingUid)) {
                FillEventHistory fillEventHistory = this.mEventHistory;
                return fillEventHistory;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public UserData getUserData() {
        UserData userData;
        synchronized (this.mLock) {
            userData = this.mUserData;
        }
        return userData;
    }

    /* access modifiers changed from: package-private */
    public UserData getUserData(int callingUid) {
        synchronized (this.mLock) {
            if (!isCalledByServiceLocked("getUserData", callingUid)) {
                return null;
            }
            UserData userData = this.mUserData;
            return userData;
        }
    }

    /* access modifiers changed from: package-private */
    public void setUserData(int callingUid, UserData userData) {
        synchronized (this.mLock) {
            if (isCalledByServiceLocked("setUserData", callingUid)) {
                this.mUserData = userData;
                this.mMetricsLogger.write(new LogMaker(1272).setPackageName(getServicePackageName()).addTaggedData(914, Integer.valueOf(this.mUserData == null ? 0 : this.mUserData.getCategoryIds().length)));
            }
        }
    }

    @GuardedBy("mLock")
    private boolean isCalledByServiceLocked(String methodName, int callingUid) {
        if (getServiceUidLocked() == callingUid) {
            return true;
        }
        Slog.w(TAG, methodName + "() called by UID " + callingUid + ", but service UID is " + getServiceUidLocked());
        return false;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public void dumpLocked(String prefix, PrintWriter pw) {
        String str = prefix;
        PrintWriter printWriter = pw;
        String prefix2 = str + "  ";
        printWriter.print(str);
        printWriter.print("User: ");
        printWriter.println(this.mUserId);
        printWriter.print(str);
        printWriter.print("UID: ");
        printWriter.println(getServiceUidLocked());
        printWriter.print(str);
        printWriter.print("Autofill Service Info: ");
        if (this.mInfo == null) {
            printWriter.println("N/A");
        } else {
            pw.println();
            this.mInfo.dump(prefix2, printWriter);
            printWriter.print(str);
            printWriter.print("Service Label: ");
            printWriter.println(getServiceLabel());
        }
        printWriter.print(str);
        printWriter.print("Component from settings: ");
        printWriter.println(getComponentNameFromSettings());
        printWriter.print(str);
        printWriter.print("Default component: ");
        printWriter.println(this.mContext.getString(17039784));
        printWriter.print(str);
        printWriter.print("Disabled: ");
        printWriter.println(this.mDisabled);
        printWriter.print(str);
        printWriter.print("Field classification enabled: ");
        printWriter.println(isFieldClassificationEnabledLocked());
        printWriter.print(str);
        printWriter.print("Compat pkgs: ");
        ArrayMap<String, Long> compatPkgs = getCompatibilityPackagesLocked();
        if (compatPkgs == null) {
            printWriter.println("N/A");
        } else {
            printWriter.println(compatPkgs);
        }
        printWriter.print(str);
        printWriter.print("Setup complete: ");
        printWriter.println(this.mSetupComplete);
        printWriter.print(str);
        printWriter.print("Last prune: ");
        printWriter.println(this.mLastPrune);
        printWriter.print(str);
        printWriter.print("Disabled apps: ");
        if (this.mDisabledApps == null) {
            printWriter.println("N/A");
        } else {
            int size = this.mDisabledApps.size();
            printWriter.println(size);
            StringBuilder builder = new StringBuilder();
            long now = SystemClock.elapsedRealtime();
            for (int i = 0; i < size; i++) {
                long expiration = this.mDisabledApps.valueAt(i).longValue();
                builder.append(str);
                builder.append(str);
                builder.append(i);
                builder.append(". ");
                builder.append(this.mDisabledApps.keyAt(i));
                builder.append(": ");
                TimeUtils.formatDuration(expiration - now, builder);
                builder.append(10);
            }
            printWriter.println(builder);
        }
        printWriter.print(str);
        printWriter.print("Disabled activities: ");
        if (this.mDisabledActivities == null) {
            printWriter.println("N/A");
        } else {
            int size2 = this.mDisabledActivities.size();
            printWriter.println(size2);
            StringBuilder builder2 = new StringBuilder();
            long now2 = SystemClock.elapsedRealtime();
            for (int i2 = 0; i2 < size2; i2++) {
                long expiration2 = this.mDisabledActivities.valueAt(i2).longValue();
                builder2.append(str);
                builder2.append(str);
                builder2.append(i2);
                builder2.append(". ");
                builder2.append(this.mDisabledActivities.keyAt(i2));
                builder2.append(": ");
                TimeUtils.formatDuration(expiration2 - now2, builder2);
                builder2.append(10);
            }
            printWriter.println(builder2);
        }
        int size3 = this.mSessions.size();
        if (size3 == 0) {
            printWriter.print(str);
            printWriter.println("No sessions");
        } else {
            printWriter.print(str);
            printWriter.print(size3);
            printWriter.println(" sessions:");
            for (int i3 = 0; i3 < size3; i3++) {
                printWriter.print(str);
                printWriter.print("#");
                printWriter.println(i3 + 1);
                this.mSessions.valueAt(i3).dumpLocked(prefix2, printWriter);
            }
        }
        printWriter.print(str);
        printWriter.print("Clients: ");
        if (this.mClients == null) {
            printWriter.println("N/A");
        } else {
            pw.println();
            this.mClients.dump(printWriter, prefix2);
        }
        if (this.mEventHistory != null && this.mEventHistory.getEvents() != null && this.mEventHistory.getEvents().size() != 0) {
            printWriter.print(str);
            printWriter.println("Events of last fill response:");
            printWriter.print(str);
            int numEvents = this.mEventHistory.getEvents().size();
            int i4 = 0;
            while (true) {
                int i5 = i4;
                if (i5 >= numEvents) {
                    break;
                }
                FillEventHistory.Event event = this.mEventHistory.getEvents().get(i5);
                printWriter.println("  " + i5 + ": eventType=" + event.getType() + " datasetId=" + event.getDatasetId());
                i4 = i5 + 1;
            }
        } else {
            printWriter.print(str);
            printWriter.println("No event on last fill response");
        }
        printWriter.print(str);
        printWriter.print("User data: ");
        if (this.mUserData == null) {
            printWriter.println("N/A");
        } else {
            pw.println();
            this.mUserData.dump(prefix2, printWriter);
        }
        printWriter.print(str);
        printWriter.println("Field Classification strategy: ");
        this.mFieldClassificationStrategy.dump(prefix2, printWriter);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public void destroySessionsLocked() {
        if (this.mSessions.size() == 0) {
            this.mUi.destroyAll(null, null, false);
            return;
        }
        while (this.mSessions.size() > 0) {
            this.mSessions.valueAt(0).forceRemoveSelfLocked();
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public void destroyFinishedSessionsLocked() {
        for (int i = this.mSessions.size() - 1; i >= 0; i--) {
            Session session = this.mSessions.valueAt(i);
            if (session.isSavingLocked()) {
                if (Helper.sDebug) {
                    Slog.d(TAG, "destroyFinishedSessionsLocked(): " + session.id);
                }
                session.forceRemoveSelfLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public void listSessionsLocked(ArrayList<String> output) {
        ComponentName componentName;
        int numSessions = this.mSessions.size();
        for (int i = 0; i < numSessions; i++) {
            StringBuilder sb = new StringBuilder();
            if (this.mInfo != null) {
                componentName = this.mInfo.getServiceInfo().getComponentName();
            } else {
                componentName = null;
            }
            sb.append(componentName);
            sb.append(":");
            sb.append(this.mSessions.keyAt(i));
            output.add(sb.toString());
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public ArrayMap<String, Long> getCompatibilityPackagesLocked() {
        if (this.mInfo != null) {
            return this.mInfo.getCompatibilityPackages();
        }
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0012, code lost:
        if (r3 >= r2) goto L_0x0058;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r4 = r1.getBroadcastItem(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        r5 = r9.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001c, code lost:
        monitor-enter(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x001d, code lost:
        if (r10 != false) goto L_0x002a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0023, code lost:
        if (isClientSessionDestroyedLocked(r4) == false) goto L_0x0026;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0026, code lost:
        r6 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0028, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x002a, code lost:
        r6 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x002b, code lost:
        r7 = isEnabledLocked();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x002f, code lost:
        monitor-exit(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0030, code lost:
        r5 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0031, code lost:
        if (r7 == false) goto L_0x0035;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0033, code lost:
        r5 = 0 | 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0035, code lost:
        if (r6 == false) goto L_0x0039;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0037, code lost:
        r5 = r5 | 2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0039, code lost:
        if (r10 == false) goto L_0x003d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x003b, code lost:
        r5 = r5 | 4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x003f, code lost:
        if (com.android.server.autofill.Helper.sDebug == false) goto L_0x0043;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0041, code lost:
        r5 = r5 | 8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0045, code lost:
        if (com.android.server.autofill.Helper.sVerbose == false) goto L_0x0049;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0047, code lost:
        r5 = r5 | 16;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0049, code lost:
        r4.setState(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:?, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0053, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0054, code lost:
        r1.finishBroadcast();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0057, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0058, code lost:
        r1.finishBroadcast();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x005c, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0010, code lost:
        r3 = 0;
     */
    private void sendStateToClients(boolean resetClient) {
        synchronized (this.mLock) {
            if (this.mClients != null) {
                RemoteCallbackList<IAutoFillManagerClient> clients = this.mClients;
                int userClientCount = clients.beginBroadcast();
            } else {
                return;
            }
        }
        int i = i + 1;
    }

    @GuardedBy("mLock")
    private boolean isClientSessionDestroyedLocked(IAutoFillManagerClient client) {
        int sessionCount = this.mSessions.size();
        for (int i = 0; i < sessionCount; i++) {
            Session session = this.mSessions.valueAt(i);
            if (session.getClient().equals(client)) {
                return session.isDestroyed();
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy("mLock")
    public boolean isEnabledLocked() {
        return this.mSetupComplete && this.mInfo != null && !this.mDisabled;
    }

    /* access modifiers changed from: package-private */
    public void disableAutofillForApp(String packageName, long duration, int sessionId, boolean compatMode) {
        synchronized (this.mLock) {
            if (this.mDisabledApps == null) {
                this.mDisabledApps = new ArrayMap<>(1);
            }
            long expiration = SystemClock.elapsedRealtime() + duration;
            if (expiration < 0) {
                expiration = JobStatus.NO_LATEST_RUNTIME;
            }
            this.mDisabledApps.put(packageName, Long.valueOf(expiration));
            this.mMetricsLogger.write(Helper.newLogMaker(1231, packageName, getServicePackageName(), sessionId, compatMode).addTaggedData(1145, Integer.valueOf(duration > 2147483647L ? HwBootFail.STAGE_BOOT_SUCCESS : (int) duration)));
        }
    }

    /* access modifiers changed from: package-private */
    public void disableAutofillForActivity(ComponentName componentName, long duration, int sessionId, boolean compatMode) {
        int intDuration;
        synchronized (this.mLock) {
            if (this.mDisabledActivities == null) {
                this.mDisabledActivities = new ArrayMap<>(1);
            }
            long expiration = SystemClock.elapsedRealtime() + duration;
            if (expiration < 0) {
                expiration = JobStatus.NO_LATEST_RUNTIME;
            }
            this.mDisabledActivities.put(componentName, Long.valueOf(expiration));
            if (duration > 2147483647L) {
                intDuration = HwBootFail.STAGE_BOOT_SUCCESS;
            } else {
                intDuration = (int) duration;
            }
            LogMaker log = new LogMaker(1232).setComponentName(componentName).addTaggedData(908, getServicePackageName()).addTaggedData(1145, Integer.valueOf(intDuration)).addTaggedData(1456, Integer.valueOf(sessionId));
            if (compatMode) {
                log.addTaggedData(1414, 1);
            }
            this.mMetricsLogger.write(log);
        }
    }

    @GuardedBy("mLock")
    private boolean isAutofillDisabledLocked(ComponentName componentName) {
        long elapsedTime = 0;
        if (this.mDisabledActivities != null) {
            elapsedTime = SystemClock.elapsedRealtime();
            Long expiration = this.mDisabledActivities.get(componentName);
            if (expiration != null) {
                if (expiration.longValue() >= elapsedTime) {
                    return true;
                }
                if (Helper.sVerbose) {
                    Slog.v(TAG, "Removing " + componentName.toShortString() + " from disabled list");
                }
                this.mDisabledActivities.remove(componentName);
            }
        }
        String packageName = componentName.getPackageName();
        if (this.mDisabledApps == null) {
            return false;
        }
        Long expiration2 = this.mDisabledApps.get(packageName);
        if (expiration2 == null) {
            return false;
        }
        if (elapsedTime == 0) {
            elapsedTime = SystemClock.elapsedRealtime();
        }
        if (expiration2.longValue() >= elapsedTime) {
            return true;
        }
        if (Helper.sVerbose) {
            Slog.v(TAG, "Removing " + packageName + " from disabled list");
        }
        this.mDisabledApps.remove(packageName);
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isFieldClassificationEnabled(int callingUid) {
        synchronized (this.mLock) {
            if (!isCalledByServiceLocked("isFieldClassificationEnabled", callingUid)) {
                return false;
            }
            boolean isFieldClassificationEnabledLocked = isFieldClassificationEnabledLocked();
            return isFieldClassificationEnabledLocked;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isFieldClassificationEnabledLocked() {
        return Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "autofill_field_classification", 1, this.mUserId) == 1;
    }

    /* access modifiers changed from: package-private */
    public FieldClassificationStrategy getFieldClassificationStrategy() {
        return this.mFieldClassificationStrategy;
    }

    /* access modifiers changed from: package-private */
    public String[] getAvailableFieldClassificationAlgorithms(int callingUid) {
        synchronized (this.mLock) {
            if (!isCalledByServiceLocked("getFCAlgorithms()", callingUid)) {
                return null;
            }
            return this.mFieldClassificationStrategy.getAvailableAlgorithms();
        }
    }

    /* access modifiers changed from: package-private */
    public String getDefaultFieldClassificationAlgorithm(int callingUid) {
        synchronized (this.mLock) {
            if (!isCalledByServiceLocked("getDefaultFCAlgorithm()", callingUid)) {
                return null;
            }
            return this.mFieldClassificationStrategy.getDefaultAlgorithm();
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AutofillManagerServiceImpl: [userId=");
        sb.append(this.mUserId);
        sb.append(", component=");
        sb.append(this.mInfo != null ? this.mInfo.getServiceInfo().getComponentName() : null);
        sb.append("]");
        return sb.toString();
    }
}
