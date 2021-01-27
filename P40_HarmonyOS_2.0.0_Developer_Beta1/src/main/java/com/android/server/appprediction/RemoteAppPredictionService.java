package com.android.server.appprediction;

import android.app.prediction.AppPredictionContext;
import android.app.prediction.AppPredictionSessionId;
import android.app.prediction.AppTargetEvent;
import android.app.prediction.IPredictionCallback;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ParceledListSlice;
import android.os.IBinder;
import android.os.IInterface;
import android.service.appprediction.IPredictionService;
import com.android.internal.infra.AbstractMultiplePendingRequestsRemoteService;
import com.android.internal.infra.AbstractRemoteService;
import com.android.server.pm.DumpState;

public class RemoteAppPredictionService extends AbstractMultiplePendingRequestsRemoteService<RemoteAppPredictionService, IPredictionService> {
    private static final String TAG = "RemoteAppPredictionService";
    private static final long TIMEOUT_REMOTE_REQUEST_MILLIS = 2000;
    private final RemoteAppPredictionServiceCallbacks mCallback;

    public interface RemoteAppPredictionServiceCallbacks extends AbstractRemoteService.VultureCallback<RemoteAppPredictionService> {
        void onConnectedStateChanged(boolean z);

        void onFailureOrTimeout(boolean z);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RemoteAppPredictionService(Context context, String serviceInterface, ComponentName componentName, int userId, RemoteAppPredictionServiceCallbacks callback, boolean bindInstantServiceAllowed, boolean verbose) {
        super(context, serviceInterface, componentName, userId, callback, context.getMainThreadHandler(), bindInstantServiceAllowed ? DumpState.DUMP_CHANGES : 0, verbose, 1);
        this.mCallback = callback;
    }

    /* access modifiers changed from: protected */
    public IPredictionService getServiceInterface(IBinder service) {
        return IPredictionService.Stub.asInterface(service);
    }

    /* access modifiers changed from: protected */
    public long getTimeoutIdleBindMillis() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public long getRemoteRequestMillis() {
        return TIMEOUT_REMOTE_REQUEST_MILLIS;
    }

    public void onCreatePredictionSession(AppPredictionContext context, AppPredictionSessionId sessionId) {
        scheduleAsyncRequest(new AbstractRemoteService.AsyncRequest(context, sessionId) {
            /* class com.android.server.appprediction.$$Lambda$RemoteAppPredictionService$Ikwq62LQ8mos7hCBmykUhqvUq2Y */
            private final /* synthetic */ AppPredictionContext f$0;
            private final /* synthetic */ AppPredictionSessionId f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void run(IInterface iInterface) {
                ((IPredictionService) iInterface).onCreatePredictionSession(this.f$0, this.f$1);
            }
        });
    }

    public void notifyAppTargetEvent(AppPredictionSessionId sessionId, AppTargetEvent event) {
        scheduleAsyncRequest(new AbstractRemoteService.AsyncRequest(sessionId, event) {
            /* class com.android.server.appprediction.$$Lambda$RemoteAppPredictionService$qroIh2ewx0BLPJ9XIAX2CaX8J4 */
            private final /* synthetic */ AppPredictionSessionId f$0;
            private final /* synthetic */ AppTargetEvent f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void run(IInterface iInterface) {
                ((IPredictionService) iInterface).notifyAppTargetEvent(this.f$0, this.f$1);
            }
        });
    }

    public void notifyLaunchLocationShown(AppPredictionSessionId sessionId, String launchLocation, ParceledListSlice targetIds) {
        scheduleAsyncRequest(new AbstractRemoteService.AsyncRequest(sessionId, launchLocation, targetIds) {
            /* class com.android.server.appprediction.$$Lambda$RemoteAppPredictionService$2EyTj40DnIRaUJU1GBU3r9jPAJg */
            private final /* synthetic */ AppPredictionSessionId f$0;
            private final /* synthetic */ String f$1;
            private final /* synthetic */ ParceledListSlice f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run(IInterface iInterface) {
                ((IPredictionService) iInterface).notifyLaunchLocationShown(this.f$0, this.f$1, this.f$2);
            }
        });
    }

    public void sortAppTargets(AppPredictionSessionId sessionId, ParceledListSlice targets, IPredictionCallback callback) {
        scheduleAsyncRequest(new AbstractRemoteService.AsyncRequest(sessionId, targets, callback) {
            /* class com.android.server.appprediction.$$Lambda$RemoteAppPredictionService$V2_zSuJJPrke_XrPl6iBEkw1Z4 */
            private final /* synthetic */ AppPredictionSessionId f$0;
            private final /* synthetic */ ParceledListSlice f$1;
            private final /* synthetic */ IPredictionCallback f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run(IInterface iInterface) {
                ((IPredictionService) iInterface).sortAppTargets(this.f$0, this.f$1, this.f$2);
            }
        });
    }

    public void registerPredictionUpdates(AppPredictionSessionId sessionId, IPredictionCallback callback) {
        scheduleAsyncRequest(new AbstractRemoteService.AsyncRequest(sessionId, callback) {
            /* class com.android.server.appprediction.$$Lambda$RemoteAppPredictionService$UaZoW5Y9AD8L3ktnyw25jtnxhA */
            private final /* synthetic */ AppPredictionSessionId f$0;
            private final /* synthetic */ IPredictionCallback f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void run(IInterface iInterface) {
                ((IPredictionService) iInterface).registerPredictionUpdates(this.f$0, this.f$1);
            }
        });
    }

    public void unregisterPredictionUpdates(AppPredictionSessionId sessionId, IPredictionCallback callback) {
        scheduleAsyncRequest(new AbstractRemoteService.AsyncRequest(sessionId, callback) {
            /* class com.android.server.appprediction.$$Lambda$RemoteAppPredictionService$sQgYVaCXRIosCYaNa7w5ZuNn7u8 */
            private final /* synthetic */ AppPredictionSessionId f$0;
            private final /* synthetic */ IPredictionCallback f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void run(IInterface iInterface) {
                ((IPredictionService) iInterface).unregisterPredictionUpdates(this.f$0, this.f$1);
            }
        });
    }

    public void requestPredictionUpdate(AppPredictionSessionId sessionId) {
        scheduleAsyncRequest(new AbstractRemoteService.AsyncRequest(sessionId) {
            /* class com.android.server.appprediction.$$Lambda$RemoteAppPredictionService$9DCowUTEF8fYuBlWGxOmP5hTAWA */
            private final /* synthetic */ AppPredictionSessionId f$0;

            {
                this.f$0 = r1;
            }

            public final void run(IInterface iInterface) {
                ((IPredictionService) iInterface).requestPredictionUpdate(this.f$0);
            }
        });
    }

    public void onDestroyPredictionSession(AppPredictionSessionId sessionId) {
        scheduleAsyncRequest(new AbstractRemoteService.AsyncRequest(sessionId) {
            /* class com.android.server.appprediction.$$Lambda$RemoteAppPredictionService$dsYLGE9YRnrxNNkC1jG8ymCUr5Q */
            private final /* synthetic */ AppPredictionSessionId f$0;

            {
                this.f$0 = r1;
            }

            public final void run(IInterface iInterface) {
                ((IPredictionService) iInterface).onDestroyPredictionSession(this.f$0);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void handleOnConnectedStateChanged(boolean connected) {
        RemoteAppPredictionServiceCallbacks remoteAppPredictionServiceCallbacks = this.mCallback;
        if (remoteAppPredictionServiceCallbacks != null) {
            remoteAppPredictionServiceCallbacks.onConnectedStateChanged(connected);
        }
    }
}
