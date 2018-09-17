package com.android.server.autofill;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.service.autofill.AutofillServiceInfo;
import android.service.autofill.FillEventHistory;
import android.service.autofill.FillEventHistory.Event;
import android.service.autofill.FillResponse;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.LocalLog;
import android.util.Slog;
import android.util.SparseArray;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.view.autofill.IAutoFillManagerClient;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.HandlerCaller;
import com.android.internal.os.HandlerCaller.Callback;
import com.android.server.autofill.ui.AutoFillUI;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

final class AutofillManagerServiceImpl {
    private static final int MAX_ABANDONED_SESSION_MILLIS = 30000;
    private static final int MAX_SESSION_ID_CREATE_TRIES = 2048;
    static final int MSG_SERVICE_SAVE = 1;
    private static final String TAG = "AutofillManagerServiceImpl";
    private static final Random sRandom = new Random();
    private RemoteCallbackList<IAutoFillManagerClient> mClients;
    private final Context mContext;
    private boolean mDisabled;
    @GuardedBy("mLock")
    private FillEventHistory mEventHistory;
    private final Callback mHandlerCallback = new -$Lambda$nIY0QhGOcwXKKmuVD-pf4oRdtr0(this);
    private final HandlerCaller mHandlerCaller = new HandlerCaller(null, Looper.getMainLooper(), this.mHandlerCallback, true);
    private AutofillServiceInfo mInfo;
    private long mLastPrune = 0;
    private final Object mLock;
    private final LocalLog mRequestsHistory;
    @GuardedBy("mLock")
    private final SparseArray<Session> mSessions = new SparseArray();
    @GuardedBy("mLock")
    private boolean mSetupComplete;
    private final AutoFillUI mUi;
    private final int mUserId;

    private class PruneTask extends AsyncTask<Void, Void, Void> {
        /* synthetic */ PruneTask(AutofillManagerServiceImpl this$0, PruneTask -this1) {
            this();
        }

        private PruneTask() {
        }

        protected Void doInBackground(Void... ignored) {
            int numSessionsToRemove;
            SparseArray<IBinder> sessionsToRemove;
            int i;
            synchronized (AutofillManagerServiceImpl.this.mLock) {
                numSessionsToRemove = AutofillManagerServiceImpl.this.mSessions.size();
                sessionsToRemove = new SparseArray(numSessionsToRemove);
                for (i = 0; i < numSessionsToRemove; i++) {
                    Session session = (Session) AutofillManagerServiceImpl.this.mSessions.valueAt(i);
                    sessionsToRemove.put(session.id, session.getActivityTokenLocked());
                }
            }
            IActivityManager am = ActivityManager.getService();
            i = 0;
            while (i < numSessionsToRemove) {
                try {
                    if (am.getActivityClassForToken((IBinder) sessionsToRemove.valueAt(i)) != null) {
                        sessionsToRemove.removeAt(i);
                        i--;
                        numSessionsToRemove--;
                    }
                } catch (RemoteException e) {
                    Slog.w(AutofillManagerServiceImpl.TAG, "Cannot figure out if activity is finished", e);
                }
                i++;
            }
            synchronized (AutofillManagerServiceImpl.this.mLock) {
                i = 0;
                while (i < numSessionsToRemove) {
                    Session sessionToRemove = (Session) AutofillManagerServiceImpl.this.mSessions.get(sessionsToRemove.keyAt(i));
                    if (sessionToRemove != null && sessionsToRemove.valueAt(i) == sessionToRemove.getActivityTokenLocked()) {
                        if (!sessionToRemove.isSavingLocked()) {
                            if (Helper.sDebug) {
                                Slog.i(AutofillManagerServiceImpl.TAG, "Prune session " + sessionToRemove.id + " (" + sessionToRemove.getActivityTokenLocked() + ")");
                            }
                            sessionToRemove.removeSelfLocked();
                        } else if (Helper.sVerbose) {
                            Slog.v(AutofillManagerServiceImpl.TAG, "Session " + sessionToRemove.id + " is saving");
                        }
                    }
                    i++;
                }
            }
            return null;
        }
    }

    /* synthetic */ void lambda$-com_android_server_autofill_AutofillManagerServiceImpl_3708(Message msg) {
        switch (msg.what) {
            case 1:
                handleSessionSave(msg.arg1);
                return;
            default:
                Slog.w(TAG, "invalid msg on handler: " + msg);
                return;
        }
    }

    AutofillManagerServiceImpl(Context context, Object lock, LocalLog requestsHistory, int userId, AutoFillUI ui, boolean disabled) {
        this.mContext = context;
        this.mLock = lock;
        this.mRequestsHistory = requestsHistory;
        this.mUserId = userId;
        this.mUi = ui;
        updateLocked(disabled);
    }

    CharSequence getServiceName() {
        String packageName = getPackageName();
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

    String getPackageName() {
        ComponentName serviceComponent = getServiceComponentName();
        if (serviceComponent != null) {
            return serviceComponent.getPackageName();
        }
        return null;
    }

    ComponentName getServiceComponentName() {
        synchronized (this.mLock) {
            if (this.mInfo == null) {
                return null;
            }
            ComponentName componentName = this.mInfo.getServiceInfo().getComponentName();
            return componentName;
        }
    }

    private boolean isSetupCompletedLocked() {
        return "1".equals(Secure.getStringForUser(this.mContext.getContentResolver(), "user_setup_complete", this.mUserId));
    }

    private String getComponentNameFromSettings() {
        return Secure.getStringForUser(this.mContext.getContentResolver(), "autofill_service", this.mUserId);
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x00a8 A:{Splitter: B:5:0x0068, ExcHandler: java.lang.RuntimeException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:17:0x00a8, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:18:0x00a9, code:
            android.util.Slog.e(TAG, "Bad autofill service name " + r0 + ": " + r1);
     */
    /* JADX WARNING: Missing block: B:19:0x00ce, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void updateLocked(boolean disabled) {
        boolean wasEnabled = isEnabled();
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
            } catch (Exception e) {
            }
        }
        if (serviceInfo != null) {
            try {
                this.mInfo = new AutofillServiceInfo(this.mContext.getPackageManager(), serviceComponent, this.mUserId);
            } catch (Exception e2) {
                Slog.e(TAG, "Bad AutofillService '" + componentName + "': " + e2);
            }
        } else {
            this.mInfo = null;
        }
        boolean isEnabled = isEnabled();
        if (wasEnabled != isEnabled) {
            if (!isEnabled) {
                for (int i = this.mSessions.size() - 1; i >= 0; i--) {
                    ((Session) this.mSessions.valueAt(i)).removeSelfLocked();
                }
            }
            sendStateToClients(false);
        }
    }

    void requestSaveForUserLocked(IBinder activityToken) {
        if (isEnabled()) {
            int numSessions = this.mSessions.size();
            for (int i = 0; i < numSessions; i++) {
                Session session = (Session) this.mSessions.valueAt(i);
                if (session.getActivityTokenLocked().equals(activityToken)) {
                    session.callSaveLocked();
                    return;
                }
            }
            Slog.w(TAG, "requestSaveForUserLocked(): no session for " + activityToken);
        }
    }

    boolean addClientLocked(IAutoFillManagerClient client) {
        if (this.mClients == null) {
            this.mClients = new RemoteCallbackList();
        }
        this.mClients.register(client);
        return isEnabled();
    }

    void setAuthenticationResultLocked(Bundle data, int sessionId, int authenticationId, int uid) {
        if (isEnabled()) {
            Session session = (Session) this.mSessions.get(sessionId);
            if (session != null && uid == session.uid) {
                session.setAuthenticationResultLocked(data, authenticationId);
            }
        }
    }

    void setHasCallback(int sessionId, int uid, boolean hasIt) {
        if (isEnabled()) {
            Session session = (Session) this.mSessions.get(sessionId);
            if (session != null && uid == session.uid) {
                synchronized (this.mLock) {
                    session.setHasCallbackLocked(hasIt);
                }
            }
        }
    }

    int startSessionLocked(IBinder activityToken, int uid, IBinder appCallbackToken, AutofillId autofillId, Rect virtualBounds, AutofillValue value, boolean hasCallback, int flags, String packageName) {
        if (!isEnabled()) {
            return 0;
        }
        pruneAbandonedSessionsLocked();
        Session newSession = createSessionByTokenLocked(activityToken, uid, appCallbackToken, hasCallback, packageName);
        if (newSession == null) {
            return Integer.MIN_VALUE;
        }
        this.mRequestsHistory.log("id=" + newSession.id + " uid=" + uid + " s=" + this.mInfo.getServiceInfo().packageName + " u=" + this.mUserId + " i=" + autofillId + " b=" + virtualBounds + " hc=" + hasCallback + " f=" + flags);
        newSession.updateLocked(autofillId, virtualBounds, value, 1, flags);
        return newSession.id;
    }

    private void pruneAbandonedSessionsLocked() {
        long now = System.currentTimeMillis();
        if (this.mLastPrune < now - 30000) {
            this.mLastPrune = now;
            if (this.mSessions.size() > 0) {
                new PruneTask(this, null).execute(new Void[0]);
            }
        }
    }

    void finishSessionLocked(int sessionId, int uid) {
        if (isEnabled()) {
            Session session = (Session) this.mSessions.get(sessionId);
            if (session == null || uid != session.uid) {
                if (Helper.sVerbose) {
                    Slog.v(TAG, "finishSessionLocked(): no session for " + sessionId + "(" + uid + ")");
                }
                return;
            }
            boolean finished = session.showSaveLocked();
            if (Helper.sVerbose) {
                Slog.v(TAG, "finishSessionLocked(): session finished on save? " + finished);
            }
            if (finished) {
                session.removeSelfLocked();
            }
        }
    }

    void cancelSessionLocked(int sessionId, int uid) {
        if (isEnabled()) {
            Session session = (Session) this.mSessions.get(sessionId);
            if (session == null || uid != session.uid) {
                Slog.w(TAG, "cancelSessionLocked(): no session for " + sessionId + "(" + uid + ")");
            } else {
                session.removeSelfLocked();
            }
        }
    }

    void disableOwnedAutofillServicesLocked(int uid) {
        if (this.mInfo != null && this.mInfo.getServiceInfo().applicationInfo.uid == uid) {
            long identity = Binder.clearCallingIdentity();
            try {
                if (this.mInfo.getServiceInfo().getComponentName().equals(ComponentName.unflattenFromString(getComponentNameFromSettings()))) {
                    Secure.putStringForUser(this.mContext.getContentResolver(), "autofill_service", null, this.mUserId);
                    destroySessionsLocked();
                }
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    private Session createSessionByTokenLocked(IBinder activityToken, int uid, IBinder appCallbackToken, boolean hasCallback, String packageName) {
        int tries = 0;
        while (true) {
            tries++;
            if (tries > 2048) {
                Slog.w(TAG, "Cannot create session in 2048 tries");
                return null;
            }
            int sessionId = sRandom.nextInt();
            if (sessionId != Integer.MIN_VALUE && this.mSessions.indexOfKey(sessionId) < 0) {
                Session newSession = new Session(this, this.mUi, this.mContext, this.mHandlerCaller, this.mUserId, this.mLock, sessionId, uid, activityToken, appCallbackToken, hasCallback, this.mInfo.getServiceInfo().getComponentName(), packageName);
                this.mSessions.put(newSession.id, newSession);
                return newSession;
            }
        }
    }

    boolean restoreSession(int sessionId, int uid, IBinder activityToken, IBinder appCallback) {
        Session session = (Session) this.mSessions.get(sessionId);
        if (session == null || uid != session.uid) {
            return false;
        }
        session.switchActivity(activityToken, appCallback);
        return true;
    }

    boolean updateSessionLocked(int sessionId, int uid, AutofillId autofillId, Rect virtualBounds, AutofillValue value, int action, int flags) {
        Session session = (Session) this.mSessions.get(sessionId);
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

    void removeSessionLocked(int sessionId) {
        this.mSessions.remove(sessionId);
    }

    private void handleSessionSave(int sessionId) {
        synchronized (this.mLock) {
            Session session = (Session) this.mSessions.get(sessionId);
            if (session == null) {
                Slog.w(TAG, "handleSessionSave(): already gone: " + sessionId);
                return;
            }
            session.callSaveLocked();
        }
    }

    void destroyLocked() {
        int i;
        if (Helper.sVerbose) {
            Slog.v(TAG, "destroyLocked()");
        }
        int numSessions = this.mSessions.size();
        ArraySet<RemoteFillService> remoteFillServices = new ArraySet(numSessions);
        for (i = 0; i < numSessions; i++) {
            RemoteFillService remoteFillService = ((Session) this.mSessions.valueAt(i)).destroyLocked();
            if (remoteFillService != null) {
                remoteFillServices.add(remoteFillService);
            }
        }
        this.mSessions.clear();
        for (i = 0; i < remoteFillServices.size(); i++) {
            ((RemoteFillService) remoteFillServices.valueAt(i)).destroy();
        }
        sendStateToClients(true);
    }

    CharSequence getServiceLabel() {
        return this.mInfo.getServiceInfo().loadLabel(this.mContext.getPackageManager());
    }

    void setLastResponse(int serviceUid, int sessionId, FillResponse response) {
        synchronized (this.mLock) {
            this.mEventHistory = new FillEventHistory(serviceUid, sessionId, response.getClientState());
        }
    }

    void resetLastResponse() {
        synchronized (this.mLock) {
            this.mEventHistory = null;
        }
    }

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

    void setAuthenticationSelected(int sessionId) {
        synchronized (this.mLock) {
            if (isValidEventLocked("setAuthenticationSelected()", sessionId)) {
                this.mEventHistory.addEvent(new Event(2, null));
            }
        }
    }

    void setDatasetAuthenticationSelected(String selectedDataset, int sessionId) {
        synchronized (this.mLock) {
            if (isValidEventLocked("setDatasetAuthenticationSelected()", sessionId)) {
                this.mEventHistory.addEvent(new Event(1, selectedDataset));
            }
        }
    }

    void setSaveShown(int sessionId) {
        synchronized (this.mLock) {
            if (isValidEventLocked("setSaveShown()", sessionId)) {
                this.mEventHistory.addEvent(new Event(3, null));
            }
        }
    }

    void setDatasetSelected(String selectedDataset, int sessionId) {
        synchronized (this.mLock) {
            if (isValidEventLocked("setDatasetSelected()", sessionId)) {
                this.mEventHistory.addEvent(new Event(0, selectedDataset));
            }
        }
    }

    /* JADX WARNING: Missing block: B:11:0x0015, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    FillEventHistory getFillEventHistory(int callingUid) {
        synchronized (this.mLock) {
            if (this.mEventHistory == null || this.mEventHistory.getServiceUid() != callingUid) {
            } else {
                FillEventHistory fillEventHistory = this.mEventHistory;
                return fillEventHistory;
            }
        }
    }

    void dumpLocked(String prefix, PrintWriter pw) {
        int i;
        Object obj = null;
        String prefix2 = prefix + "  ";
        pw.print(prefix);
        pw.print("User: ");
        pw.println(this.mUserId);
        pw.print(prefix);
        pw.print("Component: ");
        if (this.mInfo != null) {
            obj = this.mInfo.getServiceInfo().getComponentName();
        }
        pw.println(obj);
        pw.print(prefix);
        pw.print("Component from settings: ");
        pw.println(getComponentNameFromSettings());
        pw.print(prefix);
        pw.print("Default component: ");
        pw.println(this.mContext.getString(17039766));
        pw.print(prefix);
        pw.print("Disabled: ");
        pw.println(this.mDisabled);
        pw.print(prefix);
        pw.print("Setup complete: ");
        pw.println(this.mSetupComplete);
        pw.print(prefix);
        pw.print("Last prune: ");
        pw.println(this.mLastPrune);
        int size = this.mSessions.size();
        if (size == 0) {
            pw.print(prefix);
            pw.println("No sessions");
        } else {
            pw.print(prefix);
            pw.print(size);
            pw.println(" sessions:");
            for (i = 0; i < size; i++) {
                pw.print(prefix);
                pw.print("#");
                pw.println(i + 1);
                ((Session) this.mSessions.valueAt(i)).dumpLocked(prefix2, pw);
            }
        }
        if (this.mEventHistory == null || this.mEventHistory.getEvents() == null || this.mEventHistory.getEvents().size() == 0) {
            pw.print(prefix);
            pw.println("No event on last fill response");
            return;
        }
        pw.print(prefix);
        pw.println("Events of last fill response:");
        pw.print(prefix);
        int numEvents = this.mEventHistory.getEvents().size();
        for (i = 0; i < numEvents; i++) {
            Event event = (Event) this.mEventHistory.getEvents().get(i);
            pw.println("  " + i + ": eventType=" + event.getType() + " datasetId=" + event.getDatasetId());
        }
    }

    void destroySessionsLocked() {
        while (this.mSessions.size() > 0) {
            ((Session) this.mSessions.valueAt(0)).removeSelfLocked();
        }
    }

    void listSessionsLocked(ArrayList<String> output) {
        int numSessions = this.mSessions.size();
        for (int i = 0; i < numSessions; i++) {
            Object componentName;
            StringBuilder stringBuilder = new StringBuilder();
            if (this.mInfo != null) {
                componentName = this.mInfo.getServiceInfo().getComponentName();
            } else {
                componentName = null;
            }
            output.add(stringBuilder.append(componentName).append(":").append(this.mSessions.keyAt(i)).toString());
        }
    }

    /* JADX WARNING: Missing block: B:10:0x0010, code:
            r2 = 0;
     */
    /* JADX WARNING: Missing block: B:11:0x0011, code:
            if (r2 >= r5) goto L_0x0037;
     */
    /* JADX WARNING: Missing block: B:13:?, code:
            r0 = (android.view.autofill.IAutoFillManagerClient) r1.getBroadcastItem(r2);
     */
    /* JADX WARNING: Missing block: B:15:?, code:
            r6 = r8.mLock;
     */
    /* JADX WARNING: Missing block: B:16:0x001b, code:
            monitor-enter(r6);
     */
    /* JADX WARNING: Missing block: B:17:0x001c, code:
            if (r9 != false) goto L_0x0030;
     */
    /* JADX WARNING: Missing block: B:19:?, code:
            r4 = isClientSessionDestroyedLocked(r0);
     */
    /* JADX WARNING: Missing block: B:21:?, code:
            monitor-exit(r6);
     */
    /* JADX WARNING: Missing block: B:22:0x0023, code:
            r0.setState(isEnabled(), r4, r9);
     */
    /* JADX WARNING: Missing block: B:27:0x0030, code:
            r4 = true;
     */
    /* JADX WARNING: Missing block: B:33:0x0037, code:
            r1.finishBroadcast();
     */
    /* JADX WARNING: Missing block: B:34:0x003a, code:
            return;
     */
    /* JADX WARNING: Missing block: B:36:0x003c, code:
            r1.finishBroadcast();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void sendStateToClients(boolean resetClient) {
        synchronized (this.mLock) {
            if (this.mClients == null) {
                return;
            } else {
                RemoteCallbackList<IAutoFillManagerClient> clients = this.mClients;
                int userClientCount = clients.beginBroadcast();
            }
        }
        int i++;
    }

    private boolean isClientSessionDestroyedLocked(IAutoFillManagerClient client) {
        int sessionCount = this.mSessions.size();
        for (int i = 0; i < sessionCount; i++) {
            Session session = (Session) this.mSessions.valueAt(i);
            if (session.getClient().equals(client)) {
                return session.isDestroyed();
            }
        }
        return true;
    }

    boolean isEnabled() {
        return (!this.mSetupComplete || this.mInfo == null) ? false : this.mDisabled ^ 1;
    }

    public String toString() {
        Object obj = null;
        StringBuilder append = new StringBuilder().append("AutofillManagerServiceImpl: [userId=").append(this.mUserId).append(", component=");
        if (this.mInfo != null) {
            obj = this.mInfo.getServiceInfo().getComponentName();
        }
        return append.append(obj).append("]").toString();
    }
}
