package com.android.server.contentcapture;

import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;
import android.service.contentcapture.ActivityEvent;
import android.service.contentcapture.IContentCaptureService;
import android.service.contentcapture.IContentCaptureServiceCallback;
import android.service.contentcapture.SnapshotData;
import android.util.Slog;
import android.view.contentcapture.ContentCaptureContext;
import android.view.contentcapture.ContentCaptureHelper;
import android.view.contentcapture.DataRemovalRequest;
import com.android.internal.infra.AbstractMultiplePendingRequestsRemoteService;
import com.android.internal.infra.AbstractRemoteService;
import com.android.internal.os.IResultReceiver;
import com.android.server.pm.DumpState;

/* access modifiers changed from: package-private */
public final class RemoteContentCaptureService extends AbstractMultiplePendingRequestsRemoteService<RemoteContentCaptureService, IContentCaptureService> {
    private final int mIdleUnbindTimeoutMs;
    private final ContentCapturePerUserService mPerUserService;
    private final IBinder mServerCallback;

    public interface ContentCaptureServiceCallbacks extends AbstractRemoteService.VultureCallback<RemoteContentCaptureService> {
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    RemoteContentCaptureService(Context context, String serviceInterface, ComponentName serviceComponentName, IContentCaptureServiceCallback callback, int userId, ContentCapturePerUserService perUserService, boolean bindInstantServiceAllowed, boolean verbose, int idleUnbindTimeoutMs) {
        super(context, serviceInterface, serviceComponentName, userId, perUserService, context.getMainThreadHandler(), bindInstantServiceAllowed ? DumpState.DUMP_CHANGES : 0, verbose, 2);
        this.mPerUserService = perUserService;
        this.mServerCallback = callback.asBinder();
        this.mIdleUnbindTimeoutMs = idleUnbindTimeoutMs;
        ensureBoundLocked();
    }

    /* access modifiers changed from: protected */
    public IContentCaptureService getServiceInterface(IBinder service) {
        return IContentCaptureService.Stub.asInterface(service);
    }

    /* access modifiers changed from: protected */
    public long getTimeoutIdleBindMillis() {
        return (long) this.mIdleUnbindTimeoutMs;
    }

    /* access modifiers changed from: protected */
    public void handleOnConnectedStateChanged(boolean connected) {
        if (connected && getTimeoutIdleBindMillis() != 0) {
            scheduleUnbind();
        }
        if (connected) {
            try {
                this.mService.onConnected(this.mServerCallback, ContentCaptureHelper.sVerbose, ContentCaptureHelper.sDebug);
                ContentCaptureMetricsLogger.writeServiceEvent(1, this.mComponentName);
                this.mPerUserService.onConnected();
            } catch (Exception e) {
                String str = this.mTag;
                Slog.w(str, "Exception calling onConnectedStateChanged(" + connected + "): " + e);
            } catch (Throwable th) {
                this.mPerUserService.onConnected();
                throw th;
            }
        } else {
            this.mService.onDisconnected();
            ContentCaptureMetricsLogger.writeServiceEvent(2, this.mComponentName);
        }
    }

    public void ensureBoundLocked() {
        scheduleBind();
    }

    public void onSessionStarted(ContentCaptureContext context, int sessionId, int uid, IResultReceiver clientReceiver, int initialState) {
        scheduleAsyncRequest(new AbstractRemoteService.AsyncRequest(context, sessionId, uid, clientReceiver, initialState) {
            /* class com.android.server.contentcapture.$$Lambda$RemoteContentCaptureService$PMsA3CmwChlM0Qy__Uy6Yr5CFzk */
            private final /* synthetic */ ContentCaptureContext f$0;
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ IResultReceiver f$3;
            private final /* synthetic */ int f$4;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            public final void run(IInterface iInterface) {
                ((IContentCaptureService) iInterface).onSessionStarted(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4);
            }
        });
        ContentCaptureMetricsLogger.writeSessionEvent(sessionId, 1, initialState, getComponentName(), context.getActivityComponent(), false);
    }

    public void onSessionFinished(int sessionId) {
        scheduleAsyncRequest(new AbstractRemoteService.AsyncRequest(sessionId) {
            /* class com.android.server.contentcapture.$$Lambda$RemoteContentCaptureService$QbbzaxOFnxJI34vQptxzLE9Vvog */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            public final void run(IInterface iInterface) {
                ((IContentCaptureService) iInterface).onSessionFinished(this.f$0);
            }
        });
        ContentCaptureMetricsLogger.writeSessionEvent(sessionId, 2, 0, getComponentName(), null, false);
    }

    public void onActivitySnapshotRequest(int sessionId, SnapshotData snapshotData) {
        scheduleAsyncRequest(new AbstractRemoteService.AsyncRequest(sessionId, snapshotData) {
            /* class com.android.server.contentcapture.$$Lambda$RemoteContentCaptureService$WZi4GWL57wurriOS0cLTQHXrS8 */
            private final /* synthetic */ int f$0;
            private final /* synthetic */ SnapshotData f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void run(IInterface iInterface) {
                ((IContentCaptureService) iInterface).onActivitySnapshot(this.f$0, this.f$1);
            }
        });
    }

    public void onDataRemovalRequest(DataRemovalRequest request) {
        scheduleAsyncRequest(new AbstractRemoteService.AsyncRequest(request) {
            /* class com.android.server.contentcapture.$$Lambda$RemoteContentCaptureService$haMfPWsaVUWwKcAPgM3AadAkvOQ */
            private final /* synthetic */ DataRemovalRequest f$0;

            {
                this.f$0 = r1;
            }

            public final void run(IInterface iInterface) {
                ((IContentCaptureService) iInterface).onDataRemovalRequest(this.f$0);
            }
        });
        ContentCaptureMetricsLogger.writeServiceEvent(5, this.mComponentName);
    }

    public void onActivityLifecycleEvent(ActivityEvent event) {
        scheduleAsyncRequest(new AbstractRemoteService.AsyncRequest(event) {
            /* class com.android.server.contentcapture.$$Lambda$RemoteContentCaptureService$yRaGuMutdbjMq9h32e3TC2_1a_A */
            private final /* synthetic */ ActivityEvent f$0;

            {
                this.f$0 = r1;
            }

            public final void run(IInterface iInterface) {
                ((IContentCaptureService) iInterface).onActivityEvent(this.f$0);
            }
        });
    }
}
