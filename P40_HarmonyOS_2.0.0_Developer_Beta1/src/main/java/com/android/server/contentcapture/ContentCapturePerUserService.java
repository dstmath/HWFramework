package com.android.server.contentcapture;

import android.app.ActivityManagerInternal;
import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.content.ComponentName;
import android.content.ContentCaptureOptions;
import android.content.pm.ActivityPresentationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.contentcapture.ActivityEvent;
import android.service.contentcapture.ContentCaptureService;
import android.service.contentcapture.ContentCaptureServiceInfo;
import android.service.contentcapture.FlushMetrics;
import android.service.contentcapture.IContentCaptureServiceCallback;
import android.service.contentcapture.SnapshotData;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.contentcapture.ContentCaptureCondition;
import android.view.contentcapture.DataRemovalRequest;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.IResultReceiver;
import com.android.server.LocalServices;
import com.android.server.contentcapture.RemoteContentCaptureService;
import com.android.server.infra.AbstractPerUserSystemService;
import com.android.server.lights.LightsManager;
import com.android.server.usb.descriptors.UsbTerminalTypes;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/* access modifiers changed from: package-private */
public final class ContentCapturePerUserService extends AbstractPerUserSystemService<ContentCapturePerUserService, ContentCaptureManagerService> implements RemoteContentCaptureService.ContentCaptureServiceCallbacks {
    private static final String TAG = ContentCapturePerUserService.class.getSimpleName();
    @GuardedBy({"mLock"})
    private final ArrayMap<String, ArraySet<ContentCaptureCondition>> mConditionsByPkg = new ArrayMap<>();
    @GuardedBy({"mLock"})
    private ContentCaptureServiceInfo mInfo;
    @GuardedBy({"mLock"})
    RemoteContentCaptureService mRemoteService;
    private final ContentCaptureServiceRemoteCallback mRemoteServiceCallback = new ContentCaptureServiceRemoteCallback();
    @GuardedBy({"mLock"})
    private final SparseArray<ContentCaptureServerSession> mSessions = new SparseArray<>();
    @GuardedBy({"mLock"})
    private boolean mZombie;

    ContentCapturePerUserService(ContentCaptureManagerService master, Object lock, boolean disabled, int userId) {
        super(master, lock, userId);
        updateRemoteServiceLocked(disabled);
    }

    private void updateRemoteServiceLocked(boolean disabled) {
        if (((ContentCaptureManagerService) this.mMaster).verbose) {
            String str = TAG;
            Slog.v(str, "updateRemoteService(disabled=" + disabled + ")");
        }
        if (this.mRemoteService != null) {
            if (((ContentCaptureManagerService) this.mMaster).debug) {
                Slog.d(TAG, "updateRemoteService(): destroying old remote service");
            }
            this.mRemoteService.destroy();
            this.mRemoteService = null;
            resetContentCaptureWhitelistLocked();
        }
        ComponentName serviceComponentName = updateServiceInfoLocked();
        if (serviceComponentName == null) {
            if (((ContentCaptureManagerService) this.mMaster).debug) {
                Slog.d(TAG, "updateRemoteService(): no service component name");
            }
        } else if (!disabled) {
            if (((ContentCaptureManagerService) this.mMaster).debug) {
                String str2 = TAG;
                Slog.d(str2, "updateRemoteService(): creating new remote service for " + serviceComponentName);
            }
            this.mRemoteService = new RemoteContentCaptureService(((ContentCaptureManagerService) this.mMaster).getContext(), "android.service.contentcapture.ContentCaptureService", serviceComponentName, this.mRemoteServiceCallback, this.mUserId, this, ((ContentCaptureManagerService) this.mMaster).isBindInstantServiceAllowed(), ((ContentCaptureManagerService) this.mMaster).verbose, ((ContentCaptureManagerService) this.mMaster).mDevCfgIdleUnbindTimeoutMs);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.infra.AbstractPerUserSystemService
    public ServiceInfo newServiceInfoLocked(ComponentName serviceComponent) throws PackageManager.NameNotFoundException {
        this.mInfo = new ContentCaptureServiceInfo(getContext(), serviceComponent, isTemporaryServiceSetLocked(), this.mUserId);
        return this.mInfo.getServiceInfo();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.infra.AbstractPerUserSystemService
    @GuardedBy({"mLock"})
    public boolean updateLocked(boolean disabled) {
        boolean disabledStateChanged = super.updateLocked(disabled);
        if (disabledStateChanged) {
            for (int i = 0; i < this.mSessions.size(); i++) {
                this.mSessions.valueAt(i).setContentCaptureEnabledLocked(!disabled);
            }
        }
        destroyLocked();
        updateRemoteServiceLocked(disabled);
        return disabledStateChanged;
    }

    public void onServiceDied(RemoteContentCaptureService service) {
        String str = TAG;
        Slog.w(str, "remote service died: " + service);
        synchronized (this.mLock) {
            this.mZombie = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void onConnected() {
        synchronized (this.mLock) {
            if (this.mZombie) {
                if (this.mRemoteService == null) {
                    Slog.w(TAG, "Cannot ressurect sessions because remote service is null");
                } else {
                    this.mZombie = false;
                    resurrectSessionsLocked();
                }
            }
        }
    }

    private void resurrectSessionsLocked() {
        int numSessions = this.mSessions.size();
        if (((ContentCaptureManagerService) this.mMaster).debug) {
            String str = TAG;
            Slog.d(str, "Ressurrecting remote service (" + this.mRemoteService + ") on " + numSessions + " sessions");
        }
        for (int i = 0; i < numSessions; i++) {
            this.mSessions.valueAt(i).resurrectLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void onPackageUpdatingLocked() {
        int numSessions = this.mSessions.size();
        if (((ContentCaptureManagerService) this.mMaster).debug) {
            String str = TAG;
            Slog.d(str, "Pausing " + numSessions + " sessions while package is updating");
        }
        for (int i = 0; i < numSessions; i++) {
            this.mSessions.valueAt(i).pauseLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void onPackageUpdatedLocked() {
        updateRemoteServiceLocked(!isEnabledLocked());
        resurrectSessionsLocked();
    }

    @GuardedBy({"mLock"})
    public void startSessionLocked(IBinder activityToken, ActivityPresentationInfo activityPresentationInfo, int sessionId, int uid, int flags, IResultReceiver clientReceiver) {
        if (activityPresentationInfo == null) {
            Slog.w(TAG, "basic activity info is null");
            ContentCaptureService.setClientState(clientReceiver, (int) LightsManager.LIGHT_ID_BACKLIGHT_10000, (IBinder) null);
            return;
        }
        int taskId = activityPresentationInfo.taskId;
        int displayId = activityPresentationInfo.displayId;
        ComponentName componentName = activityPresentationInfo.componentName;
        boolean whiteListed = ((ContentCaptureManagerService) this.mMaster).mGlobalContentCaptureOptions.isWhitelisted(this.mUserId, componentName) || ((ContentCaptureManagerService) this.mMaster).mGlobalContentCaptureOptions.isWhitelisted(this.mUserId, componentName.getPackageName());
        ComponentName serviceComponentName = getServiceComponentName();
        boolean enabled = isEnabledLocked();
        if (((ContentCaptureManagerService) this.mMaster).mRequestsHistory != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("id=");
            sb.append(sessionId);
            sb.append(" uid=");
            sb.append(uid);
            sb.append(" a=");
            sb.append(ComponentName.flattenToShortString(componentName));
            sb.append(" t=");
            sb.append(taskId);
            sb.append(" d=");
            sb.append(displayId);
            sb.append(" s=");
            sb.append(ComponentName.flattenToShortString(serviceComponentName));
            sb.append(" u=");
            sb.append(this.mUserId);
            sb.append(" f=");
            sb.append(flags);
            sb.append(enabled ? "" : " (disabled)");
            sb.append(" w=");
            sb.append(whiteListed);
            ((ContentCaptureManagerService) this.mMaster).mRequestsHistory.log(sb.toString());
        }
        if (!enabled) {
            ContentCaptureService.setClientState(clientReceiver, 20, (IBinder) null);
            ContentCaptureMetricsLogger.writeSessionEvent(sessionId, 3, 20, serviceComponentName, componentName, false);
        } else if (serviceComponentName == null) {
            if (((ContentCaptureManagerService) this.mMaster).debug) {
                String str = TAG;
                Slog.d(str, "startSession(" + activityToken + "): hold your horses");
            }
        } else if (!whiteListed) {
            if (((ContentCaptureManagerService) this.mMaster).debug) {
                String str2 = TAG;
                Slog.d(str2, "startSession(" + componentName + "): package or component not whitelisted");
            }
            ContentCaptureService.setClientState(clientReceiver, (int) UsbTerminalTypes.TERMINAL_IN_OMNI_MIC, (IBinder) null);
            ContentCaptureMetricsLogger.writeSessionEvent(sessionId, 3, UsbTerminalTypes.TERMINAL_IN_OMNI_MIC, serviceComponentName, componentName, false);
        } else {
            ContentCaptureServerSession existingSession = this.mSessions.get(sessionId);
            if (existingSession != null) {
                String str3 = TAG;
                Slog.w(str3, "startSession(id=" + existingSession + ", token=" + activityToken + ": ignoring because it already exists for " + existingSession.mActivityToken);
                ContentCaptureService.setClientState(clientReceiver, 12, (IBinder) null);
                ContentCaptureMetricsLogger.writeSessionEvent(sessionId, 3, 12, serviceComponentName, componentName, false);
                return;
            }
            if (this.mRemoteService == null) {
                updateRemoteServiceLocked(false);
            }
            RemoteContentCaptureService remoteContentCaptureService = this.mRemoteService;
            if (remoteContentCaptureService == null) {
                String str4 = TAG;
                Slog.w(str4, "startSession(id=" + existingSession + ", token=" + activityToken + ": ignoring because service is not set");
                ContentCaptureService.setClientState(clientReceiver, 20, (IBinder) null);
                ContentCaptureMetricsLogger.writeSessionEvent(sessionId, 3, 20, serviceComponentName, componentName, false);
                return;
            }
            remoteContentCaptureService.ensureBoundLocked();
            ContentCaptureServerSession newSession = new ContentCaptureServerSession(this.mLock, activityToken, this, componentName, clientReceiver, taskId, displayId, sessionId, uid, flags);
            if (((ContentCaptureManagerService) this.mMaster).verbose) {
                String str5 = TAG;
                Slog.v(str5, "startSession(): new session for " + ComponentName.flattenToShortString(componentName) + " and id " + sessionId);
            }
            this.mSessions.put(sessionId, newSession);
            newSession.notifySessionStartedLocked(clientReceiver);
        }
    }

    @GuardedBy({"mLock"})
    public void finishSessionLocked(int sessionId) {
        if (isEnabledLocked()) {
            ContentCaptureServerSession session = this.mSessions.get(sessionId);
            if (session != null) {
                if (((ContentCaptureManagerService) this.mMaster).verbose) {
                    String str = TAG;
                    Slog.v(str, "finishSession(): id=" + sessionId);
                }
                session.removeSelfLocked(true);
            } else if (((ContentCaptureManagerService) this.mMaster).debug) {
                String str2 = TAG;
                Slog.d(str2, "finishSession(): no session with id" + sessionId);
            }
        }
    }

    @GuardedBy({"mLock"})
    public void removeDataLocked(DataRemovalRequest request) {
        if (isEnabledLocked()) {
            assertCallerLocked(request.getPackageName());
            this.mRemoteService.onDataRemovalRequest(request);
        }
    }

    @GuardedBy({"mLock"})
    public ComponentName getServiceSettingsActivityLocked() {
        String activityName;
        ContentCaptureServiceInfo contentCaptureServiceInfo = this.mInfo;
        if (contentCaptureServiceInfo == null || (activityName = contentCaptureServiceInfo.getSettingsActivity()) == null) {
            return null;
        }
        return new ComponentName(this.mInfo.getServiceInfo().packageName, activityName);
    }

    @GuardedBy({"mLock"})
    private void assertCallerLocked(String packageName) {
        String callingPackage;
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
                Slog.w(TAG, "App (package=" + callingPackage + ", UID=" + callingUid + ") passed package (" + packageName + ") owned by UID " + packageUid);
                throw new SecurityException("Invalid package: " + packageName);
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new SecurityException("Could not verify UID for " + packageName);
        }
    }

    @GuardedBy({"mLock"})
    public boolean sendActivityAssistDataLocked(IBinder activityToken, Bundle data) {
        int id = getSessionId(activityToken);
        if (id != 0) {
            this.mSessions.get(id).sendActivitySnapshotLocked(new SnapshotData(data.getBundle("data"), (AssistStructure) data.getParcelable("structure"), (AssistContent) data.getParcelable("content")));
            return true;
        }
        String str = TAG;
        Slog.e(str, "Failed to notify activity assist data for activity: " + activityToken);
        return false;
    }

    @GuardedBy({"mLock"})
    public void removeSessionLocked(int sessionId) {
        this.mSessions.remove(sessionId);
    }

    @GuardedBy({"mLock"})
    public boolean isContentCaptureServiceForUserLocked(int uid) {
        return uid == getServiceUidLocked();
    }

    @GuardedBy({"mLock"})
    private ContentCaptureServerSession getSession(IBinder activityToken) {
        for (int i = 0; i < this.mSessions.size(); i++) {
            ContentCaptureServerSession session = this.mSessions.valueAt(i);
            if (session.mActivityToken.equals(activityToken)) {
                return session;
            }
        }
        return null;
    }

    @GuardedBy({"mLock"})
    public void destroyLocked() {
        if (((ContentCaptureManagerService) this.mMaster).debug) {
            Slog.d(TAG, "destroyLocked()");
        }
        RemoteContentCaptureService remoteContentCaptureService = this.mRemoteService;
        if (remoteContentCaptureService != null) {
            remoteContentCaptureService.destroy();
        }
        destroySessionsLocked();
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void destroySessionsLocked() {
        int numSessions = this.mSessions.size();
        for (int i = 0; i < numSessions; i++) {
            this.mSessions.valueAt(i).destroyLocked(true);
        }
        this.mSessions.clear();
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void listSessionsLocked(ArrayList<String> output) {
        int numSessions = this.mSessions.size();
        for (int i = 0; i < numSessions; i++) {
            output.add(this.mSessions.valueAt(i).toShortString());
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public ArraySet<ContentCaptureCondition> getContentCaptureConditionsLocked(String packageName) {
        return this.mConditionsByPkg.get(packageName);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public void onActivityEventLocked(ComponentName componentName, int type) {
        if (this.mRemoteService != null) {
            ActivityEvent event = new ActivityEvent(componentName, type);
            if (((ContentCaptureManagerService) this.mMaster).verbose) {
                String str = this.mTag;
                Slog.v(str, "onActivityEvent(): " + event);
            }
            this.mRemoteService.onActivityLifecycleEvent(event);
        } else if (((ContentCaptureManagerService) this.mMaster).debug) {
            Slog.d(this.mTag, "onActivityEvent(): no remote service");
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.infra.AbstractPerUserSystemService
    public void dumpLocked(String prefix, PrintWriter pw) {
        super.dumpLocked(prefix, pw);
        String prefix2 = prefix + "  ";
        pw.print(prefix);
        pw.print("Service Info: ");
        if (this.mInfo == null) {
            pw.println("N/A");
        } else {
            pw.println();
            this.mInfo.dump(prefix2, pw);
        }
        pw.print(prefix);
        pw.print("Zombie: ");
        pw.println(this.mZombie);
        if (this.mRemoteService != null) {
            pw.print(prefix);
            pw.println("remote service:");
            this.mRemoteService.dump(prefix2, pw);
        }
        if (this.mSessions.size() == 0) {
            pw.print(prefix);
            pw.println("no sessions");
            return;
        }
        int sessionsSize = this.mSessions.size();
        pw.print(prefix);
        pw.print("number sessions: ");
        pw.println(sessionsSize);
        for (int i = 0; i < sessionsSize; i++) {
            pw.print(prefix);
            pw.print("#");
            pw.println(i);
            this.mSessions.valueAt(i).dumpLocked(prefix2, pw);
            pw.println();
        }
    }

    @GuardedBy({"mLock"})
    private int getSessionId(IBinder activityToken) {
        for (int i = 0; i < this.mSessions.size(); i++) {
            if (this.mSessions.valueAt(i).isActivitySession(activityToken)) {
                return this.mSessions.keyAt(i);
            }
        }
        return 0;
    }

    @GuardedBy({"mLock"})
    private void resetContentCaptureWhitelistLocked() {
        if (((ContentCaptureManagerService) this.mMaster).verbose) {
            Slog.v(TAG, "resetting content capture whitelist");
        }
        ((ContentCaptureManagerService) this.mMaster).mGlobalContentCaptureOptions.resetWhitelist(this.mUserId);
    }

    /* access modifiers changed from: private */
    public final class ContentCaptureServiceRemoteCallback extends IContentCaptureServiceCallback.Stub {
        private ContentCaptureServiceRemoteCallback() {
        }

        public void setContentCaptureWhitelist(List<String> packages, List<ComponentName> activities) {
            if (((ContentCaptureManagerService) ContentCapturePerUserService.this.mMaster).verbose) {
                String str = ContentCapturePerUserService.TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("setContentCaptureWhitelist(");
                sb.append(packages == null ? "null_packages" : packages.size() + " packages");
                sb.append(", ");
                sb.append(activities == null ? "null_activities" : activities.size() + " activities");
                sb.append(") for user ");
                sb.append(ContentCapturePerUserService.this.mUserId);
                Slog.v(str, sb.toString());
            }
            ((ContentCaptureManagerService) ContentCapturePerUserService.this.mMaster).mGlobalContentCaptureOptions.setWhitelist(ContentCapturePerUserService.this.mUserId, packages, activities);
            ContentCaptureMetricsLogger.writeSetWhitelistEvent(ContentCapturePerUserService.this.getServiceComponentName(), packages, activities);
            int numSessions = ContentCapturePerUserService.this.mSessions.size();
            if (numSessions > 0) {
                SparseBooleanArray blacklistedSessions = new SparseBooleanArray(numSessions);
                for (int i = 0; i < numSessions; i++) {
                    ContentCaptureServerSession session = (ContentCaptureServerSession) ContentCapturePerUserService.this.mSessions.valueAt(i);
                    if (!((ContentCaptureManagerService) ContentCapturePerUserService.this.mMaster).mGlobalContentCaptureOptions.isWhitelisted(ContentCapturePerUserService.this.mUserId, session.appComponentName)) {
                        int sessionId = ContentCapturePerUserService.this.mSessions.keyAt(i);
                        if (((ContentCaptureManagerService) ContentCapturePerUserService.this.mMaster).debug) {
                            Slog.d(ContentCapturePerUserService.TAG, "marking session " + sessionId + " (" + session.appComponentName + ") for un-whitelisting");
                        }
                        blacklistedSessions.append(sessionId, true);
                    }
                }
                int numBlacklisted = blacklistedSessions.size();
                if (numBlacklisted > 0) {
                    synchronized (ContentCapturePerUserService.this.mLock) {
                        for (int i2 = 0; i2 < numBlacklisted; i2++) {
                            int sessionId2 = blacklistedSessions.keyAt(i2);
                            if (((ContentCaptureManagerService) ContentCapturePerUserService.this.mMaster).debug) {
                                Slog.d(ContentCapturePerUserService.TAG, "un-whitelisting " + sessionId2);
                            }
                            ((ContentCaptureServerSession) ContentCapturePerUserService.this.mSessions.get(sessionId2)).setContentCaptureEnabledLocked(false);
                        }
                    }
                }
            }
        }

        public void setContentCaptureConditions(String packageName, List<ContentCaptureCondition> conditions) {
            String str;
            if (((ContentCaptureManagerService) ContentCapturePerUserService.this.mMaster).verbose) {
                String str2 = ContentCapturePerUserService.TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("setContentCaptureConditions(");
                sb.append(packageName);
                sb.append("): ");
                if (conditions == null) {
                    str = "null";
                } else {
                    str = conditions.size() + " conditions";
                }
                sb.append(str);
                Slog.v(str2, sb.toString());
            }
            synchronized (ContentCapturePerUserService.this.mLock) {
                if (conditions == null) {
                    ContentCapturePerUserService.this.mConditionsByPkg.remove(packageName);
                } else {
                    ContentCapturePerUserService.this.mConditionsByPkg.put(packageName, new ArraySet(conditions));
                }
            }
        }

        /* JADX INFO: finally extract failed */
        public void disableSelf() {
            if (((ContentCaptureManagerService) ContentCapturePerUserService.this.mMaster).verbose) {
                Slog.v(ContentCapturePerUserService.TAG, "disableSelf()");
            }
            long token = Binder.clearCallingIdentity();
            try {
                Settings.Secure.putStringForUser(ContentCapturePerUserService.this.getContext().getContentResolver(), "content_capture_enabled", "0", ContentCapturePerUserService.this.mUserId);
                Binder.restoreCallingIdentity(token);
                ContentCaptureMetricsLogger.writeServiceEvent(4, ContentCapturePerUserService.this.getServiceComponentName());
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }

        public void writeSessionFlush(int sessionId, ComponentName app, FlushMetrics flushMetrics, ContentCaptureOptions options, int flushReason) {
            ContentCaptureMetricsLogger.writeSessionFlush(sessionId, ContentCapturePerUserService.this.getServiceComponentName(), app, flushMetrics, options, flushReason);
        }
    }
}
