package com.android.server.autofill;

import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ICancellationSignal;
import android.os.RemoteException;
import android.os.SystemClock;
import android.service.autofill.augmented.Helper;
import android.service.autofill.augmented.IAugmentedAutofillService;
import android.service.autofill.augmented.IFillCallback;
import android.util.Pair;
import android.util.Slog;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.view.autofill.IAutoFillManagerClient;
import com.android.internal.infra.AbstractRemoteService;
import com.android.internal.infra.AbstractSinglePendingRequestRemoteService;
import com.android.internal.os.IResultReceiver;
import com.android.server.pm.DumpState;

/* access modifiers changed from: package-private */
public final class RemoteAugmentedAutofillService extends AbstractSinglePendingRequestRemoteService<RemoteAugmentedAutofillService, IAugmentedAutofillService> {
    private static final String TAG = RemoteAugmentedAutofillService.class.getSimpleName();
    private final int mIdleUnbindTimeoutMs;
    private final int mRequestTimeoutMs;

    public interface RemoteAugmentedAutofillServiceCallbacks extends AbstractRemoteService.VultureCallback<RemoteAugmentedAutofillService> {
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    RemoteAugmentedAutofillService(Context context, ComponentName serviceName, int userId, RemoteAugmentedAutofillServiceCallbacks callbacks, boolean bindInstantServiceAllowed, boolean verbose, int idleUnbindTimeoutMs, int requestTimeoutMs) {
        super(context, "android.service.autofill.augmented.AugmentedAutofillService", serviceName, userId, callbacks, context.getMainThreadHandler(), bindInstantServiceAllowed ? DumpState.DUMP_CHANGES : 0, verbose);
        this.mIdleUnbindTimeoutMs = idleUnbindTimeoutMs;
        this.mRequestTimeoutMs = requestTimeoutMs;
        scheduleBind();
    }

    static Pair<ServiceInfo, ComponentName> getComponentName(String componentName, int userId, boolean isTemporary) {
        int flags = 128;
        if (!isTemporary) {
            flags = 128 | DumpState.DUMP_DEXOPT;
        }
        try {
            ComponentName serviceComponent = ComponentName.unflattenFromString(componentName);
            ServiceInfo serviceInfo = AppGlobals.getPackageManager().getServiceInfo(serviceComponent, flags, userId);
            if (serviceInfo != null) {
                return new Pair<>(serviceInfo, serviceComponent);
            }
            String str = TAG;
            Slog.e(str, "Bad service name for flags " + flags + ": " + componentName);
            return null;
        } catch (Exception e) {
            String str2 = TAG;
            Slog.e(str2, "Error getting service info for '" + componentName + "': " + e);
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public void handleOnConnectedStateChanged(boolean state) {
        if (state && getTimeoutIdleBindMillis() != 0) {
            scheduleUnbind();
        }
        if (state) {
            try {
                this.mService.onConnected(Helper.sDebug, Helper.sVerbose);
            } catch (Exception e) {
                String str = this.mTag;
                Slog.w(str, "Exception calling onConnectedStateChanged(" + state + "): " + e);
            }
        } else {
            this.mService.onDisconnected();
        }
    }

    /* access modifiers changed from: protected */
    public IAugmentedAutofillService getServiceInterface(IBinder service) {
        return IAugmentedAutofillService.Stub.asInterface(service);
    }

    /* access modifiers changed from: protected */
    public long getTimeoutIdleBindMillis() {
        return (long) this.mIdleUnbindTimeoutMs;
    }

    /* access modifiers changed from: protected */
    public long getRemoteRequestMillis() {
        return (long) this.mRequestTimeoutMs;
    }

    public void onRequestAutofillLocked(int sessionId, IAutoFillManagerClient client, int taskId, ComponentName activityComponent, AutofillId focusedId, AutofillValue focusedValue) {
        scheduleRequest(new PendingAutofillRequest(this, sessionId, client, taskId, activityComponent, focusedId, focusedValue));
    }

    public String toString() {
        return "RemoteAugmentedAutofillService[" + ComponentName.flattenToShortString(getComponentName()) + "]";
    }

    public void onDestroyAutofillWindowsRequest() {
        scheduleAsyncRequest($$Lambda$RemoteAugmentedAutofillService$e7zSmzv77rBdYV5oClY8EJj9dY.INSTANCE);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchOnFillTimeout(ICancellationSignal cancellation) {
        this.mHandler.post(new Runnable(cancellation) {
            /* class com.android.server.autofill.$$Lambda$RemoteAugmentedAutofillService$3YjensAPYJHBJpP8njsOCNRhSYw */
            private final /* synthetic */ ICancellationSignal f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                RemoteAugmentedAutofillService.this.lambda$dispatchOnFillTimeout$1$RemoteAugmentedAutofillService(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$dispatchOnFillTimeout$1$RemoteAugmentedAutofillService(ICancellationSignal cancellation) {
        try {
            cancellation.cancel();
        } catch (RemoteException e) {
            String str = this.mTag;
            Slog.w(str, "Error calling cancellation signal: " + e);
        }
    }

    private static abstract class MyPendingRequest extends AbstractRemoteService.PendingRequest<RemoteAugmentedAutofillService, IAugmentedAutofillService> {
        protected final int mSessionId;

        private MyPendingRequest(RemoteAugmentedAutofillService service, int sessionId) {
            super(service);
            this.mSessionId = sessionId;
        }
    }

    private static final class PendingAutofillRequest extends MyPendingRequest {
        private final ComponentName mActivityComponent;
        private final IFillCallback mCallback;
        private ICancellationSignal mCancellation;
        private final IAutoFillManagerClient mClient;
        private final AutofillId mFocusedId;
        private final AutofillValue mFocusedValue;
        private final long mRequestTime = SystemClock.elapsedRealtime();
        private final int mSessionId;
        private final int mTaskId;

        protected PendingAutofillRequest(RemoteAugmentedAutofillService service, int sessionId, IAutoFillManagerClient client, int taskId, ComponentName activityComponent, AutofillId focusedId, AutofillValue focusedValue) {
            super(sessionId);
            this.mClient = client;
            this.mSessionId = sessionId;
            this.mTaskId = taskId;
            this.mActivityComponent = activityComponent;
            this.mFocusedId = focusedId;
            this.mFocusedValue = focusedValue;
            this.mCallback = new IFillCallback.Stub() {
                /* class com.android.server.autofill.RemoteAugmentedAutofillService.PendingAutofillRequest.AnonymousClass1 */

                public void onSuccess() {
                    if (PendingAutofillRequest.this.finish()) {
                    }
                }

                public void onCancellable(ICancellationSignal cancellation) {
                    boolean cancelled;
                    synchronized (PendingAutofillRequest.this.mLock) {
                        synchronized (PendingAutofillRequest.this.mLock) {
                            PendingAutofillRequest.this.mCancellation = cancellation;
                            cancelled = PendingAutofillRequest.this.isCancelledLocked();
                        }
                        if (cancelled) {
                            try {
                                cancellation.cancel();
                            } catch (RemoteException e) {
                                Slog.e(PendingAutofillRequest.this.mTag, "Error requesting a cancellation", e);
                            }
                        }
                    }
                }

                public boolean isCompleted() {
                    return PendingAutofillRequest.this.isRequestCompleted();
                }

                public void cancel() {
                    PendingAutofillRequest.this.cancel();
                }
            };
        }

        public void run() {
            synchronized (this.mLock) {
                if (isCancelledLocked()) {
                    if (Helper.sDebug) {
                        Slog.d(this.mTag, "run() called after canceled");
                    }
                    return;
                }
            }
            final RemoteAugmentedAutofillService remoteService = getService();
            if (remoteService != null) {
                try {
                    this.mClient.getAugmentedAutofillClient(new IResultReceiver.Stub() {
                        /* class com.android.server.autofill.RemoteAugmentedAutofillService.PendingAutofillRequest.AnonymousClass2 */

                        public void send(int resultCode, Bundle resultData) throws RemoteException {
                            remoteService.mService.onFillRequest(PendingAutofillRequest.this.mSessionId, resultData.getBinder("android.view.autofill.extra.AUGMENTED_AUTOFILL_CLIENT"), PendingAutofillRequest.this.mTaskId, PendingAutofillRequest.this.mActivityComponent, PendingAutofillRequest.this.mFocusedId, PendingAutofillRequest.this.mFocusedValue, PendingAutofillRequest.this.mRequestTime, PendingAutofillRequest.this.mCallback);
                        }
                    });
                } catch (RemoteException e) {
                    String str = RemoteAugmentedAutofillService.TAG;
                    Slog.e(str, "exception handling getAugmentedAutofillClient() for " + this.mSessionId + ": " + e);
                    finish();
                }
            }
        }

        /* access modifiers changed from: protected */
        public void onTimeout(RemoteAugmentedAutofillService remoteService) {
            ICancellationSignal cancellation;
            String str = RemoteAugmentedAutofillService.TAG;
            Slog.w(str, "PendingAutofillRequest timed out (" + remoteService.mRequestTimeoutMs + "ms) for " + remoteService);
            synchronized (this.mLock) {
                cancellation = this.mCancellation;
            }
            if (cancellation != null) {
                remoteService.dispatchOnFillTimeout(cancellation);
            }
            finish();
            Helper.logResponse(15, remoteService.getComponentName().getPackageName(), this.mActivityComponent, this.mSessionId, (long) remoteService.mRequestTimeoutMs);
        }

        public boolean cancel() {
            ICancellationSignal cancellation;
            if (!super.cancel()) {
                return false;
            }
            synchronized (this.mLock) {
                cancellation = this.mCancellation;
            }
            if (cancellation == null) {
                return true;
            }
            try {
                cancellation.cancel();
                return true;
            } catch (RemoteException e) {
                Slog.e(this.mTag, "Error cancelling an augmented fill request", e);
                return true;
            }
        }
    }
}
