package com.android.server.autofill;

import android.content.ComponentName;
import android.content.Context;
import android.content.IntentSender;
import android.os.Handler;
import android.os.IBinder;
import android.os.ICancellationSignal;
import android.os.RemoteException;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.IAutoFillService;
import android.service.autofill.IFillCallback;
import android.service.autofill.ISaveCallback;
import android.service.autofill.SaveRequest;
import android.util.Slog;
import com.android.internal.infra.AbstractRemoteService;
import com.android.internal.infra.AbstractSinglePendingRequestRemoteService;
import com.android.server.autofill.RemoteFillService;
import com.android.server.pm.DumpState;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/* access modifiers changed from: package-private */
public final class RemoteFillService extends AbstractSinglePendingRequestRemoteService<RemoteFillService, IAutoFillService> {
    private static final String LOG_TAG = "RemoteFillService";
    private static final long TIMEOUT_IDLE_BIND_MILLIS = 5000;
    private static final long TIMEOUT_REMOTE_REQUEST_MILLIS = 5000;
    private final FillServiceCallbacks mCallbacks;

    public interface FillServiceCallbacks extends AbstractRemoteService.VultureCallback<RemoteFillService> {
        void onFillRequestFailure(int i, CharSequence charSequence);

        void onFillRequestSuccess(int i, FillResponse fillResponse, String str, int i2);

        void onFillRequestTimeout(int i);

        void onSaveRequestFailure(CharSequence charSequence, String str);

        void onSaveRequestSuccess(String str, IntentSender intentSender);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    RemoteFillService(Context context, ComponentName componentName, int userId, FillServiceCallbacks callbacks, boolean bindInstantServiceAllowed) {
        super(context, "android.service.autofill.AutofillService", componentName, userId, callbacks, context.getMainThreadHandler(), (bindInstantServiceAllowed ? DumpState.DUMP_CHANGES : 0) | DumpState.DUMP_DEXOPT, Helper.sVerbose);
        this.mCallbacks = callbacks;
    }

    /* access modifiers changed from: protected */
    public void handleOnConnectedStateChanged(boolean state) {
        if (this.mService == null) {
            Slog.w(this.mTag, "onConnectedStateChanged(): null service");
            return;
        }
        try {
            this.mService.onConnectedStateChanged(state);
        } catch (Exception e) {
            String str = this.mTag;
            Slog.w(str, "Exception calling onConnectedStateChanged(" + state + "): " + e);
        }
    }

    /* access modifiers changed from: protected */
    public IAutoFillService getServiceInterface(IBinder service) {
        return IAutoFillService.Stub.asInterface(service);
    }

    /* access modifiers changed from: protected */
    public long getTimeoutIdleBindMillis() {
        return 5000;
    }

    /* access modifiers changed from: protected */
    public long getRemoteRequestMillis() {
        return 5000;
    }

    public CompletableFuture<Integer> cancelCurrentRequest() {
        $$Lambda$RemoteFillService$_BUUnv78CuBw5KA9LSgPsdJ9MjM r0 = new Supplier() {
            /* class com.android.server.autofill.$$Lambda$RemoteFillService$_BUUnv78CuBw5KA9LSgPsdJ9MjM */

            @Override // java.util.function.Supplier
            public final Object get() {
                return RemoteFillService.this.lambda$cancelCurrentRequest$0$RemoteFillService();
            }
        };
        Handler handler = this.mHandler;
        Objects.requireNonNull(handler);
        return CompletableFuture.supplyAsync(r0, new Executor(handler) {
            /* class com.android.server.autofill.$$Lambda$LfzJt661qZfn2w6SYHFbD3aMy0 */
            private final /* synthetic */ Handler f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.concurrent.Executor
            public final void execute(Runnable runnable) {
                this.f$0.post(runnable);
            }
        });
    }

    public /* synthetic */ Integer lambda$cancelCurrentRequest$0$RemoteFillService() {
        int i = Integer.MIN_VALUE;
        if (isDestroyed()) {
            return Integer.MIN_VALUE;
        }
        AbstractRemoteService.BasePendingRequest<RemoteFillService, IAutoFillService> canceledRequest = handleCancelPendingRequest();
        if (canceledRequest instanceof PendingFillRequest) {
            i = ((PendingFillRequest) canceledRequest).mRequest.getId();
        }
        return Integer.valueOf(i);
    }

    public void onFillRequest(FillRequest request) {
        scheduleRequest(new PendingFillRequest(request, this));
    }

    public void onSaveRequest(SaveRequest request) {
        scheduleRequest(new PendingSaveRequest(request, this));
    }

    private boolean handleResponseCallbackCommon(AbstractRemoteService.PendingRequest<RemoteFillService, IAutoFillService> pendingRequest) {
        if (isDestroyed()) {
            return false;
        }
        if (this.mPendingRequest != pendingRequest) {
            return true;
        }
        this.mPendingRequest = null;
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchOnFillRequestSuccess(PendingFillRequest pendingRequest, FillResponse response, int requestFlags) {
        this.mHandler.post(new Runnable(pendingRequest, response, requestFlags) {
            /* class com.android.server.autofill.$$Lambda$RemoteFillService$nNsNqySgqQYv3OSs9eiVuCXLs9E */
            private final /* synthetic */ RemoteFillService.PendingFillRequest f$1;
            private final /* synthetic */ FillResponse f$2;
            private final /* synthetic */ int f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.lang.Runnable
            public final void run() {
                RemoteFillService.this.lambda$dispatchOnFillRequestSuccess$1$RemoteFillService(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public /* synthetic */ void lambda$dispatchOnFillRequestSuccess$1$RemoteFillService(PendingFillRequest pendingRequest, FillResponse response, int requestFlags) {
        if (handleResponseCallbackCommon(pendingRequest)) {
            this.mCallbacks.onFillRequestSuccess(pendingRequest.mRequest.getId(), response, this.mComponentName.getPackageName(), requestFlags);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchOnFillRequestFailure(PendingFillRequest pendingRequest, CharSequence message) {
        this.mHandler.post(new Runnable(pendingRequest, message) {
            /* class com.android.server.autofill.$$Lambda$RemoteFillService$uMtdVaR6ZSsnb2B73JVKnL1_w */
            private final /* synthetic */ RemoteFillService.PendingFillRequest f$1;
            private final /* synthetic */ CharSequence f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                RemoteFillService.this.lambda$dispatchOnFillRequestFailure$2$RemoteFillService(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$dispatchOnFillRequestFailure$2$RemoteFillService(PendingFillRequest pendingRequest, CharSequence message) {
        if (handleResponseCallbackCommon(pendingRequest)) {
            this.mCallbacks.onFillRequestFailure(pendingRequest.mRequest.getId(), message);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchOnFillRequestTimeout(PendingFillRequest pendingRequest) {
        this.mHandler.post(new Runnable(pendingRequest) {
            /* class com.android.server.autofill.$$Lambda$RemoteFillService$17ODPUArCJOdtrnekJFErsoLsNA */
            private final /* synthetic */ RemoteFillService.PendingFillRequest f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                RemoteFillService.this.lambda$dispatchOnFillRequestTimeout$3$RemoteFillService(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$dispatchOnFillRequestTimeout$3$RemoteFillService(PendingFillRequest pendingRequest) {
        if (handleResponseCallbackCommon(pendingRequest)) {
            this.mCallbacks.onFillRequestTimeout(pendingRequest.mRequest.getId());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchOnFillTimeout(ICancellationSignal cancellationSignal) {
        this.mHandler.post(new Runnable(cancellationSignal) {
            /* class com.android.server.autofill.$$Lambda$RemoteFillService$cFdxAsb2okq_1ntxSWIoefN2D0Y */
            private final /* synthetic */ ICancellationSignal f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                RemoteFillService.this.lambda$dispatchOnFillTimeout$4$RemoteFillService(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$dispatchOnFillTimeout$4$RemoteFillService(ICancellationSignal cancellationSignal) {
        try {
            cancellationSignal.cancel();
        } catch (RemoteException e) {
            String str = this.mTag;
            Slog.w(str, "Error calling cancellation signal: " + e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchOnSaveRequestSuccess(PendingSaveRequest pendingRequest, IntentSender intentSender) {
        this.mHandler.post(new Runnable(pendingRequest, intentSender) {
            /* class com.android.server.autofill.$$Lambda$RemoteFillService$nNE3l9bMJ5YfGBwv5fnJX_ib1VQ */
            private final /* synthetic */ RemoteFillService.PendingSaveRequest f$1;
            private final /* synthetic */ IntentSender f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                RemoteFillService.this.lambda$dispatchOnSaveRequestSuccess$5$RemoteFillService(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$dispatchOnSaveRequestSuccess$5$RemoteFillService(PendingSaveRequest pendingRequest, IntentSender intentSender) {
        if (handleResponseCallbackCommon(pendingRequest)) {
            this.mCallbacks.onSaveRequestSuccess(this.mComponentName.getPackageName(), intentSender);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchOnSaveRequestFailure(PendingSaveRequest pendingRequest, CharSequence message) {
        this.mHandler.post(new Runnable(pendingRequest, message) {
            /* class com.android.server.autofill.$$Lambda$RemoteFillService$1eWrRA9nIGIKrCDRbK04sVnr0uo */
            private final /* synthetic */ RemoteFillService.PendingSaveRequest f$1;
            private final /* synthetic */ CharSequence f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                RemoteFillService.this.lambda$dispatchOnSaveRequestFailure$6$RemoteFillService(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$dispatchOnSaveRequestFailure$6$RemoteFillService(PendingSaveRequest pendingRequest, CharSequence message) {
        if (handleResponseCallbackCommon(pendingRequest)) {
            this.mCallbacks.onSaveRequestFailure(message, this.mComponentName.getPackageName());
        }
    }

    /* access modifiers changed from: private */
    public static final class PendingFillRequest extends AbstractRemoteService.PendingRequest<RemoteFillService, IAutoFillService> {
        private final IFillCallback mCallback;
        private ICancellationSignal mCancellation;
        private final FillRequest mRequest;

        public PendingFillRequest(final FillRequest request, RemoteFillService service) {
            super(service);
            this.mRequest = request;
            this.mCallback = new IFillCallback.Stub() {
                /* class com.android.server.autofill.RemoteFillService.PendingFillRequest.AnonymousClass1 */

                public void onCancellable(ICancellationSignal cancellation) {
                    boolean cancelled;
                    synchronized (PendingFillRequest.this.mLock) {
                        synchronized (PendingFillRequest.this.mLock) {
                            PendingFillRequest.this.mCancellation = cancellation;
                            cancelled = PendingFillRequest.this.isCancelledLocked();
                        }
                        if (cancelled) {
                            try {
                                cancellation.cancel();
                            } catch (RemoteException e) {
                                Slog.e(PendingFillRequest.this.mTag, "Error requesting a cancellation", e);
                            }
                        }
                    }
                }

                public void onSuccess(FillResponse response) {
                    RemoteFillService remoteService;
                    if (PendingFillRequest.this.finish() && (remoteService = PendingFillRequest.this.getService()) != null) {
                        remoteService.dispatchOnFillRequestSuccess(PendingFillRequest.this, response, request.getFlags());
                    }
                }

                public void onFailure(int requestId, CharSequence message) {
                    RemoteFillService remoteService;
                    if (PendingFillRequest.this.finish() && (remoteService = PendingFillRequest.this.getService()) != null) {
                        remoteService.dispatchOnFillRequestFailure(PendingFillRequest.this, message);
                    }
                }
            };
        }

        /* access modifiers changed from: protected */
        public void onTimeout(RemoteFillService remoteService) {
            ICancellationSignal cancellation;
            synchronized (this.mLock) {
                cancellation = this.mCancellation;
            }
            if (cancellation != null) {
                remoteService.dispatchOnFillTimeout(cancellation);
            }
            remoteService.dispatchOnFillRequestTimeout(this);
        }

        public void run() {
            synchronized (this.mLock) {
                if (isCancelledLocked()) {
                    if (Helper.sDebug) {
                        String str = this.mTag;
                        Slog.d(str, "run() called after canceled: " + this.mRequest);
                    }
                    return;
                }
            }
            RemoteFillService remoteService = getService();
            if (remoteService != null && remoteService.mService != null) {
                if (Helper.sVerbose) {
                    String str2 = this.mTag;
                    Slog.v(str2, "calling onFillRequest() for id=" + this.mRequest.getId());
                }
                try {
                    remoteService.mService.onFillRequest(this.mRequest, this.mCallback);
                } catch (RemoteException e) {
                    Slog.e(this.mTag, "Error calling on fill request", e);
                    remoteService.dispatchOnFillRequestFailure(this, null);
                }
            }
        }

        public boolean cancel() {
            ICancellationSignal cancellation;
            if (!RemoteFillService.super.cancel()) {
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
                Slog.e(this.mTag, "Error cancelling a fill request", e);
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class PendingSaveRequest extends AbstractRemoteService.PendingRequest<RemoteFillService, IAutoFillService> {
        private final ISaveCallback mCallback = new ISaveCallback.Stub() {
            /* class com.android.server.autofill.RemoteFillService.PendingSaveRequest.AnonymousClass1 */

            public void onSuccess(IntentSender intentSender) {
                RemoteFillService remoteService;
                if (PendingSaveRequest.this.finish() && (remoteService = PendingSaveRequest.this.getService()) != null) {
                    remoteService.dispatchOnSaveRequestSuccess(PendingSaveRequest.this, intentSender);
                }
            }

            public void onFailure(CharSequence message) {
                RemoteFillService remoteService;
                if (PendingSaveRequest.this.finish() && (remoteService = PendingSaveRequest.this.getService()) != null) {
                    remoteService.dispatchOnSaveRequestFailure(PendingSaveRequest.this, message);
                }
            }
        };
        private final SaveRequest mRequest;

        public PendingSaveRequest(SaveRequest request, RemoteFillService service) {
            super(service);
            this.mRequest = request;
        }

        /* access modifiers changed from: protected */
        public void onTimeout(RemoteFillService remoteService) {
            remoteService.dispatchOnSaveRequestFailure(this, null);
        }

        public void run() {
            RemoteFillService remoteService = getService();
            if (remoteService != null) {
                if (Helper.sVerbose) {
                    Slog.v(this.mTag, "calling onSaveRequest()");
                }
                try {
                    remoteService.mService.onSaveRequest(this.mRequest, this.mCallback);
                } catch (RemoteException e) {
                    Slog.e(this.mTag, "Error calling on save request", e);
                    remoteService.dispatchOnSaveRequestFailure(this, null);
                }
            }
        }

        public boolean isFinal() {
            return true;
        }
    }
}
