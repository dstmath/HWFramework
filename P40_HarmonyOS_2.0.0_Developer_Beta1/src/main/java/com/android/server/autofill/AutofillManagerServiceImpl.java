package com.android.server.autofill;

import android.app.ActivityManagerInternal;
import android.app.ActivityTaskManager;
import android.app.IActivityTaskManager;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.Rect;
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
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.DebugUtils;
import android.util.LocalLog;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillManager;
import android.view.autofill.AutofillValue;
import android.view.autofill.IAutoFillManagerClient;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.MetricsLogger;
import com.android.server.LocalServices;
import com.android.server.autofill.AutofillManagerService;
import com.android.server.autofill.RemoteAugmentedAutofillService;
import com.android.server.autofill.ui.AutoFillUI;
import com.android.server.infra.AbstractPerUserSystemService;
import com.android.server.job.controllers.JobStatus;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/* access modifiers changed from: package-private */
public final class AutofillManagerServiceImpl extends AbstractPerUserSystemService<AutofillManagerServiceImpl, AutofillManagerService> {
    private static final int MAX_ABANDONED_SESSION_MILLIS = 30000;
    private static final int MAX_SESSION_ID_CREATE_TRIES = 2048;
    private static final String TAG = "AutofillManagerServiceImpl";
    private static final Random sRandom = new Random();
    private final AutofillManagerService.AutofillCompatState mAutofillCompatState;
    @GuardedBy({"mLock"})
    private RemoteCallbackList<IAutoFillManagerClient> mClients;
    @GuardedBy({"mLock"})
    private ArrayMap<ComponentName, Long> mDisabledActivities;
    @GuardedBy({"mLock"})
    private ArrayMap<String, Long> mDisabledApps;
    @GuardedBy({"mLock"})
    private FillEventHistory mEventHistory;
    private final FieldClassificationStrategy mFieldClassificationStrategy;
    private final Handler mHandler = new Handler(Looper.getMainLooper(), null, true);
    @GuardedBy({"mLock"})
    private AutofillServiceInfo mInfo;
    private long mLastPrune = 0;
    private final MetricsLogger mMetricsLogger = new MetricsLogger();
    @GuardedBy({"mLock"})
    private RemoteAugmentedAutofillService mRemoteAugmentedAutofillService;
    @GuardedBy({"mLock"})
    private ServiceInfo mRemoteAugmentedAutofillServiceInfo;
    @GuardedBy({"mLock"})
    private final SparseArray<Session> mSessions = new SparseArray<>();
    private final AutoFillUI mUi;
    private final LocalLog mUiLatencyHistory;
    @GuardedBy({"mLock"})
    private UserData mUserData;
    private final LocalLog mWtfHistory;

    AutofillManagerServiceImpl(AutofillManagerService master, Object lock, LocalLog uiLatencyHistory, LocalLog wtfHistory, int userId, AutoFillUI ui, AutofillManagerService.AutofillCompatState autofillCompatState, boolean disabled) {
        super(master, lock, userId);
        this.mUiLatencyHistory = uiLatencyHistory;
        this.mWtfHistory = wtfHistory;
        this.mUi = ui;
        this.mFieldClassificationStrategy = new FieldClassificationStrategy(getContext(), userId);
        this.mAutofillCompatState = autofillCompatState;
        updateLocked(disabled);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void onBackKeyPressed() {
        RemoteAugmentedAutofillService remoteService = getRemoteAugmentedAutofillServiceLocked();
        if (remoteService != null) {
            remoteService.onDestroyAutofillWindowsRequest();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.infra.AbstractPerUserSystemService
    @GuardedBy({"mLock"})
    public boolean updateLocked(boolean disabled) {
        destroySessionsLocked();
        boolean enabledChanged = super.updateLocked(disabled);
        if (enabledChanged) {
            if (!isEnabledLocked()) {
                for (int i = this.mSessions.size() - 1; i >= 0; i--) {
                    this.mSessions.valueAt(i).removeSelfLocked();
                }
            }
            sendStateToClients(false);
        }
        updateRemoteAugmentedAutofillService();
        return enabledChanged;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.infra.AbstractPerUserSystemService
    public ServiceInfo newServiceInfoLocked(ComponentName serviceComponent) throws PackageManager.NameNotFoundException {
        this.mInfo = new AutofillServiceInfo(getContext(), serviceComponent, this.mUserId);
        return this.mInfo.getServiceInfo();
    }

    /* access modifiers changed from: package-private */
    public String[] getUrlBarResourceIdsForCompatMode(String packageName) {
        return this.mAutofillCompatState.getUrlBarResourceIds(packageName, this.mUserId);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public int addClientLocked(IAutoFillManagerClient client, ComponentName componentName) {
        if (this.mClients == null) {
            this.mClients = new RemoteCallbackList<>();
        }
        this.mClients.register(client);
        if (isEnabledLocked()) {
            return 1;
        }
        if (!isAugmentedAutofillServiceAvailableLocked() || !isWhitelistedForAugmentedAutofillLocked(componentName)) {
            return 0;
        }
        return 8;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void removeClientLocked(IAutoFillManagerClient client) {
        RemoteCallbackList<IAutoFillManagerClient> remoteCallbackList = this.mClients;
        if (remoteCallbackList != null) {
            remoteCallbackList.unregister(client);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void setAuthenticationResultLocked(Bundle data, int sessionId, int authenticationId, int uid) {
        Session session;
        if (isEnabledLocked() && (session = this.mSessions.get(sessionId)) != null && uid == session.uid) {
            session.setAuthenticationResultLocked(data, authenticationId);
        }
    }

    /* access modifiers changed from: package-private */
    public void setHasCallback(int sessionId, int uid, boolean hasIt) {
        Session session;
        if (isEnabledLocked() && (session = this.mSessions.get(sessionId)) != null && uid == session.uid) {
            synchronized (this.mLock) {
                session.setHasCallbackLocked(hasIt);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public long startSessionLocked(IBinder activityToken, int taskId, int uid, IBinder appCallbackToken, AutofillId autofillId, Rect virtualBounds, AutofillValue value, boolean hasCallback, ComponentName componentName, boolean compatMode, boolean bindInstantServiceAllowed, int flags) {
        boolean forAugmentedAutofillOnly;
        boolean forAugmentedAutofillOnly2 = (flags & 8) != 0;
        if (!isEnabledLocked() && !forAugmentedAutofillOnly2) {
            return 0;
        }
        if (forAugmentedAutofillOnly2 || !isAutofillDisabledLocked(componentName)) {
            forAugmentedAutofillOnly = forAugmentedAutofillOnly2;
        } else if (isWhitelistedForAugmentedAutofillLocked(componentName)) {
            if (Helper.sDebug) {
                Slog.d(TAG, "startSession(" + componentName + "): disabled by service but whitelisted for augmented autofill");
            }
            forAugmentedAutofillOnly = true;
        } else {
            if (Helper.sDebug) {
                Slog.d(TAG, "startSession(" + componentName + "): ignored because disabled by service and not whitelisted for augmented autofill");
            }
            try {
                IAutoFillManagerClient.Stub.asInterface(appCallbackToken).setSessionFinished(4, (List) null);
            } catch (RemoteException e) {
                Slog.w(TAG, "Could not notify " + componentName + " that it's disabled: " + e);
            }
            return 2147483647L;
        }
        if (Helper.sVerbose) {
            Slog.v(TAG, "startSession(): token=" + activityToken + ", flags=" + flags + ", forAugmentedAutofillOnly=" + forAugmentedAutofillOnly);
        }
        pruneAbandonedSessionsLocked();
        Session newSession = createSessionByTokenLocked(activityToken, taskId, uid, appCallbackToken, hasCallback, componentName, compatMode, bindInstantServiceAllowed, forAugmentedAutofillOnly, flags);
        if (newSession == null) {
            return 2147483647L;
        }
        AutofillServiceInfo autofillServiceInfo = this.mInfo;
        ((AutofillManagerService) this.mMaster).logRequestLocked("id=" + newSession.id + " uid=" + uid + " a=" + componentName.toShortString() + " s=" + (autofillServiceInfo == null ? null : autofillServiceInfo.getServiceInfo().packageName) + " u=" + this.mUserId + " i=" + autofillId + " b=" + virtualBounds + " hc=" + hasCallback + " f=" + flags + " aa=" + forAugmentedAutofillOnly);
        newSession.updateLocked(autofillId, virtualBounds, value, 1, flags);
        if (forAugmentedAutofillOnly) {
            return 4294967296L | ((long) newSession.id);
        }
        return (long) newSession.id;
    }

    @GuardedBy({"mLock"})
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
    @GuardedBy({"mLock"})
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
    @GuardedBy({"mLock"})
    public void finishSessionLocked(int sessionId, int uid) {
        if (isEnabledLocked()) {
            Session session = this.mSessions.get(sessionId);
            if (session != null && uid == session.uid) {
                session.logContextCommitted();
                boolean finished = session.showSaveLocked();
                if (Helper.sVerbose) {
                    Slog.v(TAG, "finishSessionLocked(): session finished on save? " + finished);
                }
                if (finished) {
                    session.removeSelfLocked();
                }
            } else if (Helper.sVerbose) {
                Slog.v(TAG, "finishSessionLocked(): no session for " + sessionId + "(" + uid + ")");
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
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
    @GuardedBy({"mLock"})
    public void disableOwnedAutofillServicesLocked(int uid) {
        Slog.i(TAG, "disableOwnedServices(" + uid + "): " + this.mInfo);
        AutofillServiceInfo autofillServiceInfo = this.mInfo;
        if (autofillServiceInfo != null) {
            ServiceInfo serviceInfo = autofillServiceInfo.getServiceInfo();
            if (serviceInfo.applicationInfo.uid != uid) {
                Slog.w(TAG, "disableOwnedServices(): ignored when called by UID " + uid + " instead of " + serviceInfo.applicationInfo.uid + " for service " + this.mInfo);
                return;
            }
            long identity = Binder.clearCallingIdentity();
            try {
                String autoFillService = getComponentNameLocked();
                ComponentName componentName = serviceInfo.getComponentName();
                if (componentName.equals(ComponentName.unflattenFromString(autoFillService))) {
                    this.mMetricsLogger.action(1135, componentName.getPackageName());
                    Settings.Secure.putStringForUser(getContext().getContentResolver(), "autofill_service", null, this.mUserId);
                    destroySessionsLocked();
                } else {
                    Slog.w(TAG, "disableOwnedServices(): ignored because current service (" + serviceInfo + ") does not match Settings (" + autoFillService + ")");
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    @GuardedBy({"mLock"})
    private Session createSessionByTokenLocked(IBinder activityToken, int taskId, int uid, IBinder appCallbackToken, boolean hasCallback, ComponentName componentName, boolean compatMode, boolean bindInstantServiceAllowed, boolean forAugmentedAutofillOnly, int flags) {
        ComponentName serviceComponentName;
        AutofillManagerServiceImpl autofillManagerServiceImpl = this;
        int tries = 0;
        while (true) {
            int tries2 = tries + 1;
            if (tries2 > 2048) {
                Slog.w(TAG, "Cannot create session in 2048 tries");
                return null;
            }
            int sessionId = Math.abs(sRandom.nextInt());
            if (sessionId == 0 || sessionId == Integer.MAX_VALUE || autofillManagerServiceImpl.mSessions.indexOfKey(sessionId) >= 0) {
                autofillManagerServiceImpl = autofillManagerServiceImpl;
                tries = tries2;
            } else {
                autofillManagerServiceImpl.assertCallerLocked(componentName, compatMode);
                AutofillServiceInfo autofillServiceInfo = autofillManagerServiceImpl.mInfo;
                if (autofillServiceInfo == null) {
                    serviceComponentName = null;
                } else {
                    serviceComponentName = autofillServiceInfo.getServiceInfo().getComponentName();
                }
                Session newSession = new Session(this, autofillManagerServiceImpl.mUi, getContext(), autofillManagerServiceImpl.mHandler, autofillManagerServiceImpl.mUserId, autofillManagerServiceImpl.mLock, sessionId, taskId, uid, activityToken, appCallbackToken, hasCallback, autofillManagerServiceImpl.mUiLatencyHistory, autofillManagerServiceImpl.mWtfHistory, serviceComponentName, componentName, compatMode, bindInstantServiceAllowed, forAugmentedAutofillOnly, flags);
                this.mSessions.put(newSession.id, newSession);
                return newSession;
            }
        }
    }

    private void assertCallerLocked(ComponentName componentName, boolean compatMode) {
        String callingPackage;
        String packageName = componentName.getPackageName();
        PackageManager pm = getContext().getPackageManager();
        int callingUid = Binder.getCallingUid();
        try {
            int packageUid = pm.getPackageUidAsUser(packageName, UserHandle.getCallingUserId());
            if (callingUid != packageUid && !((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).hasRunningActivity(callingUid, packageName)) {
                String[] packages = pm.getPackagesForUid(callingUid);
                if (packages != null) {
                    callingPackage = packages[0];
                } else {
                    callingPackage = "uid-" + callingUid;
                }
                Slog.w(TAG, "App (package=" + callingPackage + ", UID=" + callingUid + ") passed component (" + componentName + ") owned by UID " + packageUid);
                LogMaker log = new LogMaker(948).setPackageName(callingPackage).addTaggedData(908, getServicePackageName()).addTaggedData(949, componentName.flattenToShortString());
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
    @GuardedBy({"mLock"})
    public boolean updateSessionLocked(int sessionId, int uid, AutofillId autofillId, Rect virtualBounds, AutofillValue value, int action, int flags) {
        Session session = this.mSessions.get(sessionId);
        if (session != null && session.uid == uid) {
            session.updateLocked(autofillId, virtualBounds, value, action, flags);
            return false;
        } else if ((flags & 1) == 0) {
            if (Helper.sVerbose) {
                Slog.v(TAG, "updateSessionLocked(): session gone for " + sessionId + "(" + uid + ")");
            }
            return false;
        } else if (!Helper.sDebug) {
            return true;
        } else {
            Slog.d(TAG, "restarting session " + sessionId + " due to manual request on " + autofillId);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void removeSessionLocked(int sessionId) {
        this.mSessions.remove(sessionId);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public ArrayList<Session> getPreviousSessionsLocked(Session session) {
        int size = this.mSessions.size();
        ArrayList<Session> previousSessions = null;
        for (int i = 0; i < size; i++) {
            Session previousSession = this.mSessions.valueAt(i);
            if (!(previousSession.taskId != session.taskId || previousSession.id == session.id || (previousSession.getSaveInfoFlagsLocked() & 4) == 0)) {
                if (previousSessions == null) {
                    previousSessions = new ArrayList<>(size);
                }
                previousSessions.add(previousSession);
            }
        }
        return previousSessions;
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
        if (Helper.sDebug) {
            Slog.d(TAG, "No pending Save UI for token " + token + " and operation " + DebugUtils.flagsToString(AutofillManager.class, "PENDING_UI_OPERATION_", operation));
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.infra.AbstractPerUserSystemService
    @GuardedBy({"mLock"})
    public void handlePackageUpdateLocked(String packageName) {
        ServiceInfo serviceInfo = this.mFieldClassificationStrategy.getServiceInfo();
        if (serviceInfo != null && serviceInfo.packageName.equals(packageName)) {
            resetExtServiceLocked();
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void resetExtServiceLocked() {
        if (Helper.sVerbose) {
            Slog.v(TAG, "reset autofill service.");
        }
        this.mFieldClassificationStrategy.reset();
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
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
        RemoteCallbackList<IAutoFillManagerClient> remoteCallbackList = this.mClients;
        if (remoteCallbackList != null) {
            remoteCallbackList.kill();
            this.mClients = null;
        }
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

    @GuardedBy({"mLock"})
    private boolean isValidEventLocked(String method, int sessionId) {
        FillEventHistory fillEventHistory = this.mEventHistory;
        if (fillEventHistory == null) {
            Slog.w(TAG, method + ": not logging event because history is null");
            return false;
        } else if (sessionId == fillEventHistory.getSessionId()) {
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
                    this.mEventHistory.addEvent(new FillEventHistory.Event(2, null, clientState, null, null, null, null, null, null, null, null));
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
                    this.mEventHistory.addEvent(new FillEventHistory.Event(1, selectedDataset, clientState, null, null, null, null, null, null, null, null));
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
                    this.mEventHistory.addEvent(new FillEventHistory.Event(3, null, clientState, null, null, null, null, null, null, null, null));
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
                    this.mEventHistory.addEvent(new FillEventHistory.Event(0, selectedDataset, clientState, null, null, null, null, null, null, null, null));
                }
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void logContextCommittedLocked(int sessionId, Bundle clientState, ArrayList<String> selectedDatasets, ArraySet<String> ignoredDatasets, ArrayList<AutofillId> changedFieldIds, ArrayList<String> changedDatasetIds, ArrayList<AutofillId> manuallyFilledFieldIds, ArrayList<ArrayList<String>> manuallyFilledDatasetIds, ComponentName appComponentName, boolean compatMode) {
        logContextCommittedLocked(sessionId, clientState, selectedDatasets, ignoredDatasets, changedFieldIds, changedDatasetIds, manuallyFilledFieldIds, manuallyFilledDatasetIds, null, null, appComponentName, compatMode);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void logContextCommittedLocked(int sessionId, Bundle clientState, ArrayList<String> selectedDatasets, ArraySet<String> ignoredDatasets, ArrayList<AutofillId> changedFieldIds, ArrayList<String> changedDatasetIds, ArrayList<AutofillId> manuallyFilledFieldIds, ArrayList<ArrayList<String>> manuallyFilledDatasetIds, ArrayList<AutofillId> detectedFieldIdsList, ArrayList<FieldClassification> detectedFieldClassificationsList, ComponentName appComponentName, boolean compatMode) {
        FieldClassification[] detectedFieldClassifications;
        if (isValidEventLocked("logDatasetNotSelected()", sessionId)) {
            if (Helper.sVerbose) {
                Slog.v(TAG, "logContextCommitted() with FieldClassification: id=" + sessionId + ", selectedDatasets=" + selectedDatasets + ", ignoredDatasetIds=" + ignoredDatasets + ", changedAutofillIds=" + changedFieldIds + ", changedDatasetIds=" + changedDatasetIds + ", manuallyFilledFieldIds=" + manuallyFilledFieldIds + ", detectedFieldIds=" + detectedFieldIdsList + ", detectedFieldClassifications=" + detectedFieldClassificationsList + ", appComponentName=" + appComponentName.toShortString() + ", compatMode=" + compatMode);
            }
            AutofillId[] detectedFieldsIds = null;
            if (detectedFieldIdsList != null) {
                AutofillId[] detectedFieldsIds2 = new AutofillId[detectedFieldIdsList.size()];
                detectedFieldIdsList.toArray(detectedFieldsIds2);
                FieldClassification[] detectedFieldClassifications2 = new FieldClassification[detectedFieldClassificationsList.size()];
                detectedFieldClassificationsList.toArray(detectedFieldClassifications2);
                int numberFields = detectedFieldsIds2.length;
                int totalSize = 0;
                float totalScore = 0.0f;
                int i = 0;
                while (i < numberFields) {
                    List<FieldClassification.Match> matches = detectedFieldClassifications2[i].getMatches();
                    int size = matches.size();
                    totalSize += size;
                    float totalScore2 = totalScore;
                    for (int j = 0; j < size; j++) {
                        totalScore2 += matches.get(j).getScore();
                    }
                    i++;
                    totalScore = totalScore2;
                }
                this.mMetricsLogger.write(Helper.newLogMaker(1273, appComponentName, getServicePackageName(), sessionId, compatMode).setCounterValue(numberFields).addTaggedData(1274, Integer.valueOf((int) ((100.0f * totalScore) / ((float) totalSize)))));
                detectedFieldClassifications = detectedFieldClassifications2;
                detectedFieldsIds = detectedFieldsIds2;
            } else {
                detectedFieldClassifications = null;
            }
            this.mEventHistory.addEvent(new FillEventHistory.Event(4, null, clientState, selectedDatasets, ignoredDatasets, changedFieldIds, changedDatasetIds, manuallyFilledFieldIds, manuallyFilledDatasetIds, detectedFieldsIds, detectedFieldClassifications));
        }
    }

    /* access modifiers changed from: package-private */
    public FillEventHistory getFillEventHistory(int callingUid) {
        synchronized (this.mLock) {
            if (this.mEventHistory == null || !isCalledByServiceLocked("getFillEventHistory", callingUid)) {
                return null;
            }
            return this.mEventHistory;
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
            return this.mUserData;
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

    @GuardedBy({"mLock"})
    private boolean isCalledByServiceLocked(String methodName, int callingUid) {
        int serviceUid = getServiceUidLocked();
        if (serviceUid == callingUid) {
            return true;
        }
        Slog.w(TAG, methodName + "() called by UID " + callingUid + ", but service UID is " + serviceUid);
        return false;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public int getSupportedSmartSuggestionModesLocked() {
        return ((AutofillManagerService) this.mMaster).getSupportedSmartSuggestionModesLocked();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.infra.AbstractPerUserSystemService
    @GuardedBy({"mLock"})
    public void dumpLocked(String prefix, PrintWriter pw) {
        super.dumpLocked(prefix, pw);
        String prefix2 = prefix + "  ";
        pw.print(prefix);
        pw.print("UID: ");
        pw.println(getServiceUidLocked());
        pw.print(prefix);
        pw.print("Autofill Service Info: ");
        if (this.mInfo == null) {
            pw.println("N/A");
        } else {
            pw.println();
            this.mInfo.dump(prefix2, pw);
        }
        pw.print(prefix);
        pw.print("Default component: ");
        pw.println(getContext().getString(17039811));
        pw.print(prefix);
        pw.println("mAugmentedAutofillNamer: ");
        pw.print(prefix2);
        ((AutofillManagerService) this.mMaster).mAugmentedAutofillResolver.dumpShort(pw, this.mUserId);
        pw.println();
        if (this.mRemoteAugmentedAutofillService != null) {
            pw.print(prefix);
            pw.println("RemoteAugmentedAutofillService: ");
            this.mRemoteAugmentedAutofillService.dump(prefix2, pw);
        }
        if (this.mRemoteAugmentedAutofillServiceInfo != null) {
            pw.print(prefix);
            pw.print("RemoteAugmentedAutofillServiceInfo: ");
            pw.println(this.mRemoteAugmentedAutofillServiceInfo);
        }
        pw.print(prefix);
        pw.print("Field classification enabled: ");
        pw.println(isFieldClassificationEnabledLocked());
        pw.print(prefix);
        pw.print("Compat pkgs: ");
        ArrayMap<String, Long> compatPkgs = getCompatibilityPackagesLocked();
        if (compatPkgs == null) {
            pw.println("N/A");
        } else {
            pw.println(compatPkgs);
        }
        pw.print(prefix);
        pw.print("Last prune: ");
        pw.println(this.mLastPrune);
        pw.print(prefix);
        pw.print("Disabled apps: ");
        ArrayMap<String, Long> arrayMap = this.mDisabledApps;
        String str = ": ";
        String str2 = ". ";
        if (arrayMap == null) {
            pw.println("N/A");
        } else {
            int size = arrayMap.size();
            pw.println(size);
            StringBuilder builder = new StringBuilder();
            long now = SystemClock.elapsedRealtime();
            int i = 0;
            while (i < size) {
                long expiration = this.mDisabledApps.valueAt(i).longValue();
                builder.append(prefix);
                builder.append(prefix);
                builder.append(i);
                builder.append(str2);
                builder.append(this.mDisabledApps.keyAt(i));
                builder.append(str);
                TimeUtils.formatDuration(expiration - now, builder);
                builder.append('\n');
                i++;
                size = size;
            }
            pw.println(builder);
        }
        pw.print(prefix);
        pw.print("Disabled activities: ");
        ArrayMap<ComponentName, Long> arrayMap2 = this.mDisabledActivities;
        if (arrayMap2 == null) {
            pw.println("N/A");
        } else {
            int size2 = arrayMap2.size();
            pw.println(size2);
            StringBuilder builder2 = new StringBuilder();
            long now2 = SystemClock.elapsedRealtime();
            int i2 = 0;
            while (i2 < size2) {
                long expiration2 = this.mDisabledActivities.valueAt(i2).longValue();
                builder2.append(prefix);
                builder2.append(prefix);
                builder2.append(i2);
                builder2.append(str2);
                builder2.append(this.mDisabledActivities.keyAt(i2));
                builder2.append(str);
                TimeUtils.formatDuration(expiration2 - now2, builder2);
                builder2.append('\n');
                i2++;
                str = str;
                str2 = str2;
            }
            pw.println(builder2);
        }
        int size3 = this.mSessions.size();
        if (size3 == 0) {
            pw.print(prefix);
            pw.println("No sessions");
        } else {
            pw.print(prefix);
            pw.print(size3);
            pw.println(" sessions:");
            for (int i3 = 0; i3 < size3; i3++) {
                pw.print(prefix);
                pw.print("#");
                pw.println(i3 + 1);
                this.mSessions.valueAt(i3).dumpLocked(prefix2, pw);
            }
        }
        pw.print(prefix);
        pw.print("Clients: ");
        if (this.mClients == null) {
            pw.println("N/A");
        } else {
            pw.println();
            this.mClients.dump(pw, prefix2);
        }
        FillEventHistory fillEventHistory = this.mEventHistory;
        if (fillEventHistory == null || fillEventHistory.getEvents() == null || this.mEventHistory.getEvents().size() == 0) {
            pw.print(prefix);
            pw.println("No event on last fill response");
        } else {
            pw.print(prefix);
            pw.println("Events of last fill response:");
            pw.print(prefix);
            int numEvents = this.mEventHistory.getEvents().size();
            for (int i4 = 0; i4 < numEvents; i4++) {
                FillEventHistory.Event event = this.mEventHistory.getEvents().get(i4);
                pw.println("  " + i4 + ": eventType=" + event.getType() + " datasetId=" + event.getDatasetId());
            }
        }
        pw.print(prefix);
        pw.print("User data: ");
        if (this.mUserData == null) {
            pw.println("N/A");
        } else {
            pw.println();
            this.mUserData.dump(prefix2, pw);
        }
        pw.print(prefix);
        pw.println("Field Classification strategy: ");
        this.mFieldClassificationStrategy.dump(prefix2, pw);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
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
    @GuardedBy({"mLock"})
    public void destroySessionsForAugmentedAutofillOnlyLocked() {
        for (int i = this.mSessions.size() - 1; i >= 0; i--) {
            this.mSessions.valueAt(i).forceRemoveSelfIfForAugmentedAutofillOnlyLocked();
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void destroyFinishedSessionsLocked() {
        for (int i = this.mSessions.size() - 1; i >= 0; i--) {
            Session session = this.mSessions.valueAt(i);
            if (session.isSavingLocked()) {
                if (Helper.sDebug) {
                    Slog.d(TAG, "destroyFinishedSessionsLocked(): " + session.id);
                }
                session.forceRemoveSelfLocked();
            } else {
                session.destroyAugmentedAutofillWindowsLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void listSessionsLocked(ArrayList<String> output) {
        String service;
        String augmentedService;
        int numSessions = this.mSessions.size();
        if (numSessions > 0) {
            for (int i = 0; i < numSessions; i++) {
                int id = this.mSessions.keyAt(i);
                AutofillServiceInfo autofillServiceInfo = this.mInfo;
                if (autofillServiceInfo == null) {
                    service = "no_svc";
                } else {
                    service = autofillServiceInfo.getServiceInfo().getComponentName().flattenToShortString();
                }
                ServiceInfo serviceInfo = this.mRemoteAugmentedAutofillServiceInfo;
                if (serviceInfo == null) {
                    augmentedService = "no_aug";
                } else {
                    augmentedService = serviceInfo.getComponentName().flattenToShortString();
                }
                output.add(String.format("%d:%s:%s", Integer.valueOf(id), service, augmentedService));
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public ArrayMap<String, Long> getCompatibilityPackagesLocked() {
        AutofillServiceInfo autofillServiceInfo = this.mInfo;
        if (autofillServiceInfo != null) {
            return autofillServiceInfo.getCompatibilityPackages();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public RemoteAugmentedAutofillService getRemoteAugmentedAutofillServiceLocked() {
        if (this.mRemoteAugmentedAutofillService == null) {
            String serviceName = ((AutofillManagerService) this.mMaster).mAugmentedAutofillResolver.getServiceName(this.mUserId);
            if (serviceName == null) {
                if (((AutofillManagerService) this.mMaster).verbose) {
                    Slog.v(TAG, "getRemoteAugmentedAutofillServiceLocked(): not set");
                }
                return null;
            }
            Pair<ServiceInfo, ComponentName> pair = RemoteAugmentedAutofillService.getComponentName(serviceName, this.mUserId, ((AutofillManagerService) this.mMaster).mAugmentedAutofillResolver.isTemporary(this.mUserId));
            if (pair == null) {
                return null;
            }
            this.mRemoteAugmentedAutofillServiceInfo = (ServiceInfo) pair.first;
            ComponentName componentName = (ComponentName) pair.second;
            if (Helper.sVerbose) {
                Slog.v(TAG, "getRemoteAugmentedAutofillServiceLocked(): " + componentName);
            }
            this.mRemoteAugmentedAutofillService = new RemoteAugmentedAutofillService(getContext(), componentName, this.mUserId, new RemoteAugmentedAutofillService.RemoteAugmentedAutofillServiceCallbacks() {
                /* class com.android.server.autofill.AutofillManagerServiceImpl.AnonymousClass1 */

                public void onServiceDied(RemoteAugmentedAutofillService service) {
                    Slog.w(AutofillManagerServiceImpl.TAG, "remote augmented autofill service died");
                    RemoteAugmentedAutofillService remoteService = AutofillManagerServiceImpl.this.mRemoteAugmentedAutofillService;
                    if (remoteService != null) {
                        remoteService.destroy();
                    }
                    AutofillManagerServiceImpl.this.mRemoteAugmentedAutofillService = null;
                }
            }, ((AutofillManagerService) this.mMaster).isInstantServiceAllowed(), ((AutofillManagerService) this.mMaster).verbose, ((AutofillManagerService) this.mMaster).mAugmentedServiceIdleUnbindTimeoutMs, ((AutofillManagerService) this.mMaster).mAugmentedServiceRequestTimeoutMs);
        }
        return this.mRemoteAugmentedAutofillService;
    }

    /* access modifiers changed from: package-private */
    public void updateRemoteAugmentedAutofillService() {
        synchronized (this.mLock) {
            if (this.mRemoteAugmentedAutofillService != null) {
                if (Helper.sVerbose) {
                    Slog.v(TAG, "updateRemoteAugmentedAutofillService(): destroying old remote service");
                }
                destroySessionsForAugmentedAutofillOnlyLocked();
                this.mRemoteAugmentedAutofillService.destroy();
                this.mRemoteAugmentedAutofillService = null;
                this.mRemoteAugmentedAutofillServiceInfo = null;
                resetAugmentedAutofillWhitelistLocked();
            }
            boolean available = isAugmentedAutofillServiceAvailableLocked();
            if (Helper.sVerbose) {
                Slog.v(TAG, "updateRemoteAugmentedAutofillService(): " + available);
            }
            if (available) {
                this.mRemoteAugmentedAutofillService = getRemoteAugmentedAutofillServiceLocked();
            }
        }
    }

    private boolean isAugmentedAutofillServiceAvailableLocked() {
        if (((AutofillManagerService) this.mMaster).verbose) {
            Slog.v(TAG, "isAugmentedAutofillService(): setupCompleted=" + isSetupCompletedLocked() + ", disabled=" + isDisabledByUserRestrictionsLocked() + ", augmentedService=" + ((AutofillManagerService) this.mMaster).mAugmentedAutofillResolver.getServiceName(this.mUserId));
        }
        if (!isSetupCompletedLocked() || isDisabledByUserRestrictionsLocked() || ((AutofillManagerService) this.mMaster).mAugmentedAutofillResolver.getServiceName(this.mUserId) == null) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isAugmentedAutofillServiceForUserLocked(int callingUid) {
        ServiceInfo serviceInfo = this.mRemoteAugmentedAutofillServiceInfo;
        return serviceInfo != null && serviceInfo.applicationInfo.uid == callingUid;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public boolean setAugmentedAutofillWhitelistLocked(List<String> packages, List<ComponentName> activities, int callingUid) {
        String serviceName;
        if (!isCalledByAugmentedAutofillServiceLocked("setAugmentedAutofillWhitelistLocked", callingUid)) {
            return false;
        }
        if (((AutofillManagerService) this.mMaster).verbose) {
            Slog.v(TAG, "setAugmentedAutofillWhitelistLocked(packages=" + packages + ", activities=" + activities + ")");
        }
        whitelistForAugmentedAutofillPackages(packages, activities);
        ServiceInfo serviceInfo = this.mRemoteAugmentedAutofillServiceInfo;
        if (serviceInfo != null) {
            serviceName = serviceInfo.getComponentName().flattenToShortString();
        } else {
            Slog.e(TAG, "setAugmentedAutofillWhitelistLocked(): no service");
            serviceName = "N/A";
        }
        LogMaker log = new LogMaker(1721).addTaggedData(908, serviceName);
        if (packages != null) {
            log.addTaggedData(1722, Integer.valueOf(packages.size()));
        }
        if (activities != null) {
            log.addTaggedData(1723, Integer.valueOf(activities.size()));
        }
        this.mMetricsLogger.write(log);
        return true;
    }

    @GuardedBy({"mLock"})
    private boolean isCalledByAugmentedAutofillServiceLocked(String methodName, int callingUid) {
        if (getRemoteAugmentedAutofillServiceLocked() == null) {
            Slog.w(TAG, methodName + "() called by UID " + callingUid + ", but there is no augmented autofill service defined for user " + getUserId());
            return false;
        } else if (getAugmentedAutofillServiceUidLocked() == callingUid) {
            return true;
        } else {
            Slog.w(TAG, methodName + "() called by UID " + callingUid + ", but service UID is " + getAugmentedAutofillServiceUidLocked() + " for user " + getUserId());
            return false;
        }
    }

    @GuardedBy({"mLock"})
    private int getAugmentedAutofillServiceUidLocked() {
        ServiceInfo serviceInfo = this.mRemoteAugmentedAutofillServiceInfo;
        if (serviceInfo != null) {
            return serviceInfo.applicationInfo.uid;
        }
        if (!((AutofillManagerService) this.mMaster).verbose) {
            return -1;
        }
        Slog.v(TAG, "getAugmentedAutofillServiceUid(): no mRemoteAugmentedAutofillServiceInfo");
        return -1;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public boolean isWhitelistedForAugmentedAutofillLocked(ComponentName componentName) {
        return ((AutofillManagerService) this.mMaster).mAugmentedAutofillState.isWhitelisted(this.mUserId, componentName);
    }

    private void whitelistForAugmentedAutofillPackages(List<String> packages, List<ComponentName> components) {
        synchronized (this.mLock) {
            if (((AutofillManagerService) this.mMaster).verbose) {
                Slog.v(TAG, "whitelisting packages: " + packages + "and activities: " + components);
            }
            ((AutofillManagerService) this.mMaster).mAugmentedAutofillState.setWhitelist(this.mUserId, packages, components);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void resetAugmentedAutofillWhitelistLocked() {
        if (((AutofillManagerService) this.mMaster).verbose) {
            Slog.v(TAG, "resetting augmented autofill whitelist");
        }
        ((AutofillManagerService) this.mMaster).mAugmentedAutofillState.resetWhitelist(this.mUserId);
    }

    private void sendStateToClients(boolean resetClient) {
        RemoteCallbackList<IAutoFillManagerClient> clients;
        int userClientCount;
        boolean resetSession;
        boolean isEnabled;
        synchronized (this.mLock) {
            if (this.mClients != null) {
                clients = this.mClients;
                userClientCount = clients.beginBroadcast();
            } else {
                return;
            }
        }
        for (int i = 0; i < userClientCount; i++) {
            try {
                IAutoFillManagerClient client = clients.getBroadcastItem(i);
                try {
                    synchronized (this.mLock) {
                        if (!resetClient) {
                            try {
                                if (!isClientSessionDestroyedLocked(client)) {
                                    resetSession = false;
                                    isEnabled = isEnabledLocked();
                                }
                            } catch (Throwable th) {
                                throw th;
                            }
                        }
                        resetSession = true;
                        isEnabled = isEnabledLocked();
                    }
                    int flags = 0;
                    if (isEnabled) {
                        flags = 0 | 1;
                    }
                    if (resetSession) {
                        flags |= 2;
                    }
                    if (resetClient) {
                        flags |= 4;
                    }
                    if (Helper.sDebug) {
                        flags |= 8;
                    }
                    if (Helper.sVerbose) {
                        flags |= 16;
                    }
                    client.setState(flags);
                } catch (RemoteException e) {
                }
            } catch (Throwable th2) {
                clients.finishBroadcast();
                throw th2;
            }
        }
        clients.finishBroadcast();
    }

    @GuardedBy({"mLock"})
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
            this.mMetricsLogger.write(Helper.newLogMaker(1231, packageName, getServicePackageName(), sessionId, compatMode).addTaggedData(1145, Integer.valueOf(duration > 2147483647L ? Integer.MAX_VALUE : (int) duration)));
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
                intDuration = Integer.MAX_VALUE;
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

    @GuardedBy({"mLock"})
    private boolean isAutofillDisabledLocked(ComponentName componentName) {
        Long expiration;
        long elapsedTime = 0;
        if (this.mDisabledActivities != null) {
            elapsedTime = SystemClock.elapsedRealtime();
            Long expiration2 = this.mDisabledActivities.get(componentName);
            if (expiration2 != null) {
                if (expiration2.longValue() >= elapsedTime) {
                    return true;
                }
                if (Helper.sVerbose) {
                    Slog.v(TAG, "Removing " + componentName.toShortString() + " from disabled list");
                }
                this.mDisabledActivities.remove(componentName);
            }
        }
        String packageName = componentName.getPackageName();
        ArrayMap<String, Long> arrayMap = this.mDisabledApps;
        if (arrayMap == null || (expiration = arrayMap.get(packageName)) == null) {
            return false;
        }
        if (elapsedTime == 0) {
            elapsedTime = SystemClock.elapsedRealtime();
        }
        if (expiration.longValue() >= elapsedTime) {
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
            return isFieldClassificationEnabledLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isFieldClassificationEnabledLocked() {
        return Settings.Secure.getIntForUser(getContext().getContentResolver(), "autofill_field_classification", 1, this.mUserId) == 1;
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
        AutofillServiceInfo autofillServiceInfo = this.mInfo;
        sb.append(autofillServiceInfo != null ? autofillServiceInfo.getServiceInfo().getComponentName() : null);
        sb.append("]");
        return sb.toString();
    }

    /* access modifiers changed from: private */
    public class PruneTask extends AsyncTask<Void, Void, Void> {
        private PruneTask() {
        }

        /* access modifiers changed from: protected */
        public Void doInBackground(Void... ignored) {
            int numSessionsToRemove;
            SparseArray<IBinder> sessionsToRemove;
            synchronized (AutofillManagerServiceImpl.this.mLock) {
                numSessionsToRemove = AutofillManagerServiceImpl.this.mSessions.size();
                sessionsToRemove = new SparseArray<>(numSessionsToRemove);
                for (int i = 0; i < numSessionsToRemove; i++) {
                    Session session = (Session) AutofillManagerServiceImpl.this.mSessions.valueAt(i);
                    sessionsToRemove.put(session.id, session.getActivityTokenLocked());
                }
            }
            IActivityTaskManager atm = ActivityTaskManager.getService();
            int i2 = 0;
            while (i2 < numSessionsToRemove) {
                try {
                    if (atm.getActivityClassForToken(sessionsToRemove.valueAt(i2)) != null) {
                        sessionsToRemove.removeAt(i2);
                        i2--;
                        numSessionsToRemove--;
                    }
                } catch (RemoteException e) {
                    Slog.w(AutofillManagerServiceImpl.TAG, "Cannot figure out if activity is finished", e);
                }
                i2++;
            }
            synchronized (AutofillManagerServiceImpl.this.mLock) {
                for (int i3 = 0; i3 < numSessionsToRemove; i3++) {
                    Session sessionToRemove = (Session) AutofillManagerServiceImpl.this.mSessions.get(sessionsToRemove.keyAt(i3));
                    if (sessionToRemove != null && sessionsToRemove.valueAt(i3) == sessionToRemove.getActivityTokenLocked()) {
                        if (!sessionToRemove.isSavingLocked()) {
                            if (Helper.sDebug) {
                                Slog.i(AutofillManagerServiceImpl.TAG, "Prune session " + sessionToRemove.id + " (" + sessionToRemove.getActivityTokenLocked() + ")");
                            }
                            sessionToRemove.removeSelfLocked();
                        } else if (Helper.sVerbose) {
                            Slog.v(AutofillManagerServiceImpl.TAG, "Session " + sessionToRemove.id + " is saving");
                        }
                    }
                }
            }
            return null;
        }
    }
}
