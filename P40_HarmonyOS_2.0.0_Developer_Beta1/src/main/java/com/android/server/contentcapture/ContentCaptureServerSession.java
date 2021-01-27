package com.android.server.contentcapture;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.contentcapture.ContentCaptureService;
import android.service.contentcapture.SnapshotData;
import android.util.LocalLog;
import android.util.Slog;
import android.view.contentcapture.ContentCaptureContext;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.IResultReceiver;
import com.android.internal.util.Preconditions;
import com.android.server.usb.descriptors.UsbACInterface;
import java.io.PrintWriter;

/* access modifiers changed from: package-private */
public final class ContentCaptureServerSession {
    private static final String TAG = ContentCaptureServerSession.class.getSimpleName();
    public final ComponentName appComponentName;
    final IBinder mActivityToken;
    private final ContentCaptureContext mContentCaptureContext;
    private final int mId;
    private final Object mLock;
    private final ContentCapturePerUserService mService;
    private final IResultReceiver mSessionStateReceiver;
    private final int mUid;

    ContentCaptureServerSession(Object lock, IBinder activityToken, ContentCapturePerUserService service, ComponentName appComponentName2, IResultReceiver sessionStateReceiver, int taskId, int displayId, int sessionId, int uid, int flags) {
        Preconditions.checkArgument(sessionId != 0);
        this.mLock = lock;
        this.mActivityToken = activityToken;
        this.appComponentName = appComponentName2;
        this.mService = service;
        this.mId = sessionId;
        this.mUid = uid;
        this.mContentCaptureContext = new ContentCaptureContext(null, appComponentName2, taskId, displayId, flags);
        this.mSessionStateReceiver = sessionStateReceiver;
        try {
            sessionStateReceiver.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                /* class com.android.server.contentcapture.$$Lambda$ContentCaptureServerSession$PKv4aNj3xMYOeCpzUQZDD2iG0o */

                @Override // android.os.IBinder.DeathRecipient
                public final void binderDied() {
                    ContentCaptureServerSession.this.lambda$new$0$ContentCaptureServerSession();
                }
            }, 0);
        } catch (Exception e) {
            String str = TAG;
            Slog.w(str, "could not register DeathRecipient for " + activityToken);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isActivitySession(IBinder activityToken) {
        return this.mActivityToken.equals(activityToken);
    }

    @GuardedBy({"mLock"})
    public void notifySessionStartedLocked(IResultReceiver clientReceiver) {
        if (this.mService.mRemoteService == null) {
            Slog.w(TAG, "notifySessionStartedLocked(): no remote service");
        } else {
            this.mService.mRemoteService.onSessionStarted(this.mContentCaptureContext, this.mId, this.mUid, clientReceiver, 2);
        }
    }

    @GuardedBy({"mLock"})
    public void setContentCaptureEnabledLocked(boolean enabled) {
        try {
            Bundle extras = new Bundle();
            int i = 1;
            extras.putBoolean("enabled", true);
            IResultReceiver iResultReceiver = this.mSessionStateReceiver;
            if (!enabled) {
                i = 2;
            }
            iResultReceiver.send(i, extras);
        } catch (RemoteException e) {
            String str = TAG;
            Slog.w(str, "Error async reporting result to client: " + e);
        }
    }

    @GuardedBy({"mLock"})
    public void sendActivitySnapshotLocked(SnapshotData snapshotData) {
        LocalLog logHistory = ((ContentCaptureManagerService) this.mService.getMaster()).mRequestsHistory;
        if (logHistory != null) {
            logHistory.log("snapshot: id=" + this.mId);
        }
        if (this.mService.mRemoteService == null) {
            Slog.w(TAG, "sendActivitySnapshotLocked(): no remote service");
        } else {
            this.mService.mRemoteService.onActivitySnapshotRequest(this.mId, snapshotData);
        }
    }

    @GuardedBy({"mLock"})
    public void removeSelfLocked(boolean notifyRemoteService) {
        try {
            destroyLocked(notifyRemoteService);
        } finally {
            this.mService.removeSessionLocked(this.mId);
        }
    }

    @GuardedBy({"mLock"})
    public void destroyLocked(boolean notifyRemoteService) {
        if (this.mService.isVerbose()) {
            String str = TAG;
            Slog.v(str, "destroy(notifyRemoteService=" + notifyRemoteService + ")");
        }
        if (!notifyRemoteService) {
            return;
        }
        if (this.mService.mRemoteService == null) {
            Slog.w(TAG, "destroyLocked(): no remote service");
        } else {
            this.mService.mRemoteService.onSessionFinished(this.mId);
        }
    }

    @GuardedBy({"mLock"})
    public void resurrectLocked() {
        RemoteContentCaptureService remoteService = this.mService.mRemoteService;
        if (remoteService == null) {
            Slog.w(TAG, "destroyLocked(: no remote service");
            return;
        }
        if (this.mService.isVerbose()) {
            String str = TAG;
            Slog.v(str, "resurrecting " + this.mActivityToken + " on " + remoteService);
        }
        remoteService.onSessionStarted(new ContentCaptureContext(this.mContentCaptureContext, 4), this.mId, this.mUid, this.mSessionStateReceiver, UsbACInterface.FORMAT_II_AC3);
    }

    @GuardedBy({"mLock"})
    public void pauseLocked() {
        if (this.mService.isVerbose()) {
            String str = TAG;
            Slog.v(str, "pausing " + this.mActivityToken);
        }
        ContentCaptureService.setClientState(this.mSessionStateReceiver, 2052, (IBinder) null);
    }

    /* access modifiers changed from: private */
    /* renamed from: onClientDeath */
    public void lambda$new$0$ContentCaptureServerSession() {
        if (this.mService.isVerbose()) {
            String str = TAG;
            Slog.v(str, "onClientDeath(" + this.mActivityToken + "): removing session " + this.mId);
        }
        synchronized (this.mLock) {
            removeSelfLocked(true);
        }
    }

    @GuardedBy({"mLock"})
    public void dumpLocked(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("id: ");
        pw.print(this.mId);
        pw.println();
        pw.print(prefix);
        pw.print("uid: ");
        pw.print(this.mUid);
        pw.println();
        pw.print(prefix);
        pw.print("context: ");
        this.mContentCaptureContext.dump(pw);
        pw.println();
        pw.print(prefix);
        pw.print("activity token: ");
        pw.println(this.mActivityToken);
        pw.print(prefix);
        pw.print("app component: ");
        pw.println(this.appComponentName);
        pw.print(prefix);
        pw.print("has autofill callback: ");
    }

    /* access modifiers changed from: package-private */
    public String toShortString() {
        return this.mId + ":" + this.mActivityToken;
    }

    public String toString() {
        return "ContentCaptureSession[id=" + this.mId + ", act=" + this.mActivityToken + "]";
    }
}
