package com.android.server.autofill;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.ICancellationSignal;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.IAutoFillService;
import android.service.autofill.IFillCallback;
import android.service.autofill.ISaveCallback;
import android.service.autofill.SaveRequest;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.FgThread;
import com.android.server.autofill.RemoteFillService;
import com.android.server.pm.DumpState;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.NoSuchElementException;

final class RemoteFillService implements IBinder.DeathRecipient {
    private static final String LOG_TAG = "RemoteFillService";
    private static final int MSG_UNBIND = 3;
    private static final long TIMEOUT_IDLE_BIND_MILLIS = 5000;
    private static final long TIMEOUT_REMOTE_REQUEST_MILLIS = 5000;
    /* access modifiers changed from: private */
    public IAutoFillService mAutoFillService;
    private final boolean mBindInstantServiceAllowed;
    /* access modifiers changed from: private */
    public boolean mBinding;
    private final FillServiceCallbacks mCallbacks;
    private boolean mCompleted;
    private final ComponentName mComponentName;
    private final Context mContext;
    /* access modifiers changed from: private */
    public boolean mDestroyed;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    private final Intent mIntent;
    /* access modifiers changed from: private */
    public PendingRequest mPendingRequest;
    private final ServiceConnection mServiceConnection = new RemoteServiceConnection();
    /* access modifiers changed from: private */
    public boolean mServiceDied;
    private final int mUserId;

    public interface FillServiceCallbacks {
        void onFillRequestFailure(int i, CharSequence charSequence, String str);

        void onFillRequestSuccess(int i, FillResponse fillResponse, String str, int i2);

        void onFillRequestTimeout(int i, String str);

        void onSaveRequestFailure(CharSequence charSequence, String str);

        void onSaveRequestSuccess(String str, IntentSender intentSender);

        void onServiceDied(RemoteFillService remoteFillService);
    }

    private static final class PendingFillRequest extends PendingRequest {
        private final IFillCallback mCallback;
        /* access modifiers changed from: private */
        public ICancellationSignal mCancellation;
        /* access modifiers changed from: private */
        public final FillRequest mRequest;

        public PendingFillRequest(final FillRequest request, RemoteFillService service) {
            super(service);
            this.mRequest = request;
            this.mCallback = new IFillCallback.Stub() {
                public void onCancellable(ICancellationSignal cancellation) {
                    boolean cancelled;
                    synchronized (PendingFillRequest.this.mLock) {
                        synchronized (PendingFillRequest.this.mLock) {
                            ICancellationSignal unused = PendingFillRequest.this.mCancellation = cancellation;
                            cancelled = PendingFillRequest.this.isCancelledLocked();
                        }
                        if (cancelled) {
                            try {
                                cancellation.cancel();
                            } catch (RemoteException e) {
                                Slog.e(RemoteFillService.LOG_TAG, "Error requesting a cancellation", e);
                            }
                        }
                    }
                }

                public void onSuccess(FillResponse response) {
                    if (PendingFillRequest.this.finish()) {
                        RemoteFillService remoteService = PendingFillRequest.this.getService();
                        if (remoteService != null) {
                            remoteService.dispatchOnFillRequestSuccess(PendingFillRequest.this, response, request.getFlags());
                        }
                    }
                }

                public void onFailure(int requestId, CharSequence message) {
                    if (PendingFillRequest.this.finish()) {
                        RemoteFillService remoteService = PendingFillRequest.this.getService();
                        if (remoteService != null) {
                            remoteService.dispatchOnFillRequestFailure(PendingFillRequest.this, message);
                        }
                    }
                }
            };
        }

        /* access modifiers changed from: package-private */
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

        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0029, code lost:
            r0 = getService();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x002d, code lost:
            if (r0 == null) goto L_0x0058;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0033, code lost:
            if (com.android.server.autofill.RemoteFillService.access$400(r0) == null) goto L_0x0058;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
            com.android.server.autofill.RemoteFillService.access$400(r0).onFillRequest(r4.mRequest, r4.mCallback);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x0042, code lost:
            android.util.Slog.e(com.android.server.autofill.RemoteFillService.LOG_TAG, "onFillRequest has Exception : IndexOutOfBoundsException");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x004b, code lost:
            r1 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x004c, code lost:
            android.util.Slog.e(com.android.server.autofill.RemoteFillService.LOG_TAG, "Error calling on fill request", r1);
            com.android.server.autofill.RemoteFillService.access$1200(r0, r4, null);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0027, code lost:
            return;
         */
        public void run() {
            synchronized (this.mLock) {
                if (isCancelledLocked()) {
                    if (Helper.sDebug) {
                        Slog.d(RemoteFillService.LOG_TAG, "run() called after canceled: " + this.mRequest);
                    }
                }
            }
        }

        public boolean cancel() {
            ICancellationSignal cancellation;
            if (!super.cancel()) {
                return false;
            }
            synchronized (this.mLock) {
                cancellation = this.mCancellation;
            }
            if (cancellation != null) {
                try {
                    cancellation.cancel();
                } catch (RemoteException e) {
                    Slog.e(RemoteFillService.LOG_TAG, "Error cancelling a fill request", e);
                }
            }
            return true;
        }
    }

    private static abstract class PendingRequest implements Runnable {
        @GuardedBy("mLock")
        private boolean mCancelled;
        @GuardedBy("mLock")
        private boolean mCompleted;
        protected final Object mLock = new Object();
        private final Handler mServiceHandler;
        private final Runnable mTimeoutTrigger;
        private final WeakReference<RemoteFillService> mWeakService;

        /* access modifiers changed from: package-private */
        public abstract void onTimeout(RemoteFillService remoteFillService);

        PendingRequest(RemoteFillService service) {
            this.mWeakService = new WeakReference<>(service);
            this.mServiceHandler = service.mHandler;
            this.mTimeoutTrigger = new Runnable() {
                public final void run() {
                    RemoteFillService.PendingRequest.lambda$new$0(RemoteFillService.PendingRequest.this);
                }
            };
            this.mServiceHandler.postAtTime(this.mTimeoutTrigger, SystemClock.uptimeMillis() + 5000);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x0033, code lost:
            if (r0 == null) goto L_0x0060;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0035, code lost:
            android.util.Slog.w(com.android.server.autofill.RemoteFillService.LOG_TAG, r5.getClass().getSimpleName() + " timed out after " + 5000 + " ms");
            r5.onTimeout(r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0060, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x000d, code lost:
            android.util.Slog.w(com.android.server.autofill.RemoteFillService.LOG_TAG, r5.getClass().getSimpleName() + " timed out");
            r0 = (com.android.server.autofill.RemoteFillService) r5.mWeakService.get();
         */
        public static /* synthetic */ void lambda$new$0(PendingRequest pendingRequest) {
            synchronized (pendingRequest.mLock) {
                if (!pendingRequest.mCancelled) {
                    pendingRequest.mCompleted = true;
                }
            }
        }

        /* access modifiers changed from: protected */
        public RemoteFillService getService() {
            return (RemoteFillService) this.mWeakService.get();
        }

        /* access modifiers changed from: protected */
        public final boolean finish() {
            synchronized (this.mLock) {
                if (!this.mCompleted) {
                    if (!this.mCancelled) {
                        this.mCompleted = true;
                        this.mServiceHandler.removeCallbacks(this.mTimeoutTrigger);
                        return true;
                    }
                }
                return false;
            }
        }

        /* access modifiers changed from: protected */
        @GuardedBy("mLock")
        public boolean isCancelledLocked() {
            return this.mCancelled;
        }

        /* access modifiers changed from: package-private */
        public boolean cancel() {
            synchronized (this.mLock) {
                if (!this.mCancelled) {
                    if (!this.mCompleted) {
                        this.mCancelled = true;
                        this.mServiceHandler.removeCallbacks(this.mTimeoutTrigger);
                        return true;
                    }
                }
                return false;
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isFinal() {
            return false;
        }
    }

    private static final class PendingSaveRequest extends PendingRequest {
        private final ISaveCallback mCallback = new ISaveCallback.Stub() {
            public void onSuccess(IntentSender intentSender) {
                if (PendingSaveRequest.this.finish()) {
                    RemoteFillService remoteService = PendingSaveRequest.this.getService();
                    if (remoteService != null) {
                        remoteService.dispatchOnSaveRequestSuccess(PendingSaveRequest.this, intentSender);
                    }
                }
            }

            public void onFailure(CharSequence message) {
                if (PendingSaveRequest.this.finish()) {
                    RemoteFillService remoteService = PendingSaveRequest.this.getService();
                    if (remoteService != null) {
                        remoteService.dispatchOnSaveRequestFailure(PendingSaveRequest.this, message);
                    }
                }
            }
        };
        private final SaveRequest mRequest;

        public PendingSaveRequest(SaveRequest request, RemoteFillService service) {
            super(service);
            this.mRequest = request;
        }

        /* access modifiers changed from: package-private */
        public void onTimeout(RemoteFillService remoteService) {
            remoteService.dispatchOnSaveRequestFailure(this, null);
        }

        public void run() {
            RemoteFillService remoteService = getService();
            if (remoteService != null) {
                try {
                    remoteService.mAutoFillService.onSaveRequest(this.mRequest, this.mCallback);
                } catch (RemoteException e) {
                    Slog.e(RemoteFillService.LOG_TAG, "Error calling on save request", e);
                    remoteService.dispatchOnSaveRequestFailure(this, null);
                }
            }
        }

        public boolean isFinal() {
            return true;
        }
    }

    private class RemoteServiceConnection implements ServiceConnection {
        private RemoteServiceConnection() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            if (RemoteFillService.this.mDestroyed || !RemoteFillService.this.mBinding) {
                Slog.wtf(RemoteFillService.LOG_TAG, "onServiceConnected was dispatched after unbindService.");
                return;
            }
            boolean unused = RemoteFillService.this.mBinding = false;
            IAutoFillService unused2 = RemoteFillService.this.mAutoFillService = IAutoFillService.Stub.asInterface(service);
            try {
                service.linkToDeath(RemoteFillService.this, 0);
                try {
                    RemoteFillService.this.mAutoFillService.onConnectedStateChanged(true);
                } catch (RemoteException e) {
                    Slog.w(RemoteFillService.LOG_TAG, "Exception calling onConnected(): " + e);
                }
                if (RemoteFillService.this.mPendingRequest != null) {
                    PendingRequest pendingRequest = RemoteFillService.this.mPendingRequest;
                    PendingRequest unused3 = RemoteFillService.this.mPendingRequest = null;
                    RemoteFillService.this.handlePendingRequest(pendingRequest);
                }
                boolean unused4 = RemoteFillService.this.mServiceDied = false;
            } catch (RemoteException e2) {
                RemoteFillService.this.handleBinderDied();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            boolean unused = RemoteFillService.this.mBinding = true;
            IAutoFillService unused2 = RemoteFillService.this.mAutoFillService = null;
        }
    }

    public RemoteFillService(Context context, ComponentName componentName, int userId, FillServiceCallbacks callbacks, boolean bindInstantServiceAllowed) {
        this.mContext = context;
        this.mCallbacks = callbacks;
        this.mComponentName = componentName;
        this.mIntent = new Intent("android.service.autofill.AutofillService").setComponent(this.mComponentName);
        this.mUserId = userId;
        this.mHandler = new Handler(FgThread.getHandler().getLooper());
        this.mBindInstantServiceAllowed = bindInstantServiceAllowed;
    }

    public void destroy() {
        this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$RemoteFillService$KN9CcjjmJTg_PJcamzzLgVvQt9M.INSTANCE, this));
    }

    /* access modifiers changed from: private */
    public void handleDestroy() {
        if (!checkIfDestroyed()) {
            if (this.mPendingRequest != null) {
                this.mPendingRequest.cancel();
                this.mPendingRequest = null;
            }
            ensureUnbound();
            this.mDestroyed = true;
        }
    }

    public void binderDied() {
        this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$RemoteFillService$1sGSxm1GNkRnOTqlIJFPKrlV6Bk.INSTANCE, this));
    }

    /* access modifiers changed from: private */
    public void handleBinderDied() {
        if (!checkIfDestroyed()) {
            if (this.mAutoFillService != null) {
                try {
                    this.mAutoFillService.asBinder().unlinkToDeath(this, 0);
                } catch (NoSuchElementException e) {
                    Slog.e(LOG_TAG, "handleBinderDied Unable to unlinkToDeath!", e);
                }
            }
            this.mAutoFillService = null;
            this.mServiceDied = true;
            this.mCallbacks.onServiceDied(this);
        }
    }

    public int cancelCurrentRequest() {
        if (this.mDestroyed) {
            return Integer.MIN_VALUE;
        }
        int requestId = Integer.MIN_VALUE;
        if (this.mPendingRequest != null) {
            if (this.mPendingRequest instanceof PendingFillRequest) {
                requestId = ((PendingFillRequest) this.mPendingRequest).mRequest.getId();
            }
            try {
                this.mPendingRequest.cancel();
            } catch (NullPointerException e) {
                Slog.e(LOG_TAG, "Error calling cancle", e);
            }
            this.mPendingRequest = null;
        }
        return requestId;
    }

    public void onFillRequest(FillRequest request) {
        cancelScheduledUnbind();
        scheduleRequest(new PendingFillRequest(request, this));
    }

    public void onSaveRequest(SaveRequest request) {
        cancelScheduledUnbind();
        scheduleRequest(new PendingSaveRequest(request, this));
    }

    private void scheduleRequest(PendingRequest pendingRequest) {
        this.mHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$RemoteFillService$h6FPsdmILphrDZs953cJIyumyqg.INSTANCE, this, pendingRequest));
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.append(prefix).append("service:").println();
        pw.append(prefix).append("  ").append("userId=").append(String.valueOf(this.mUserId)).println();
        pw.append(prefix).append("  ").append("componentName=").append(this.mComponentName.flattenToString()).println();
        pw.append(prefix).append("  ").append("destroyed=").append(String.valueOf(this.mDestroyed)).println();
        pw.append(prefix).append("  ").append("bound=").append(String.valueOf(isBound())).println();
        pw.append(prefix).append("  ").append("hasPendingRequest=").append(String.valueOf(this.mPendingRequest != null)).println();
        pw.append(prefix).append("mBindInstantServiceAllowed=").println(this.mBindInstantServiceAllowed);
        pw.println();
    }

    private void cancelScheduledUnbind() {
        this.mHandler.removeMessages(3);
    }

    private void scheduleUnbind() {
        cancelScheduledUnbind();
        this.mHandler.sendMessageDelayed(PooledLambda.obtainMessage($$Lambda$RemoteFillService$YjPsINV7QuCehWwsB0GTTg1hvr4.INSTANCE, this).setWhat(3), 5000);
    }

    /* access modifiers changed from: private */
    public void handleUnbind() {
        if (!checkIfDestroyed()) {
            ensureUnbound();
        }
    }

    /* access modifiers changed from: private */
    public void handlePendingRequest(PendingRequest pendingRequest) {
        if (!checkIfDestroyed() && !this.mCompleted) {
            if (!isBound()) {
                if (this.mPendingRequest != null) {
                    this.mPendingRequest.cancel();
                }
                this.mPendingRequest = pendingRequest;
                ensureBound();
            } else {
                if (Helper.sVerbose) {
                    Slog.v(LOG_TAG, "[user: " + this.mUserId + "] handlePendingRequest()");
                }
                if (pendingRequest != null) {
                    pendingRequest.run();
                    if (pendingRequest.isFinal()) {
                        this.mCompleted = true;
                    }
                }
            }
        }
    }

    private boolean isBound() {
        return this.mAutoFillService != null;
    }

    private void ensureBound() {
        if (!isBound() && !this.mBinding) {
            if (Helper.sVerbose) {
                Slog.v(LOG_TAG, "[user: " + this.mUserId + "] ensureBound()");
            }
            this.mBinding = true;
            int flags = 67108865;
            if (this.mBindInstantServiceAllowed) {
                flags = 67108865 | DumpState.DUMP_CHANGES;
            }
            if (!this.mContext.bindServiceAsUser(this.mIntent, this.mServiceConnection, flags, new UserHandle(this.mUserId))) {
                Slog.w(LOG_TAG, "[user: " + this.mUserId + "] could not bind to " + this.mIntent + " using flags " + flags);
                this.mBinding = false;
                if (!this.mServiceDied) {
                    handleBinderDied();
                }
            }
        }
    }

    private void ensureUnbound() {
        if (isBound() || this.mBinding) {
            if (Helper.sVerbose) {
                Slog.v(LOG_TAG, "[user: " + this.mUserId + "] ensureUnbound()");
            }
            this.mBinding = false;
            if (isBound()) {
                try {
                    this.mAutoFillService.onConnectedStateChanged(false);
                } catch (Exception e) {
                    Slog.w(LOG_TAG, "Exception calling onDisconnected(): " + e);
                }
                if (this.mAutoFillService != null) {
                    try {
                        this.mAutoFillService.asBinder().unlinkToDeath(this, 0);
                    } catch (NoSuchElementException e2) {
                        Slog.e(LOG_TAG, "ensureUnbound Unable to unlinkToDeath!", e2);
                    }
                    this.mAutoFillService = null;
                }
            }
            try {
                this.mContext.unbindService(this.mServiceConnection);
            } catch (IllegalArgumentException e3) {
                Slog.e(LOG_TAG, "Service not registered: " + e3);
            }
        }
    }

    /* access modifiers changed from: private */
    public void dispatchOnFillRequestSuccess(PendingFillRequest pendingRequest, FillResponse response, int requestFlags) {
        this.mHandler.post(new Runnable(pendingRequest, response, requestFlags) {
            private final /* synthetic */ RemoteFillService.PendingFillRequest f$1;
            private final /* synthetic */ FillResponse f$2;
            private final /* synthetic */ int f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                RemoteFillService.lambda$dispatchOnFillRequestSuccess$0(RemoteFillService.this, this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public static /* synthetic */ void lambda$dispatchOnFillRequestSuccess$0(RemoteFillService remoteFillService, PendingFillRequest pendingRequest, FillResponse response, int requestFlags) {
        if (remoteFillService.handleResponseCallbackCommon(pendingRequest)) {
            remoteFillService.mCallbacks.onFillRequestSuccess(pendingRequest.mRequest.getId(), response, remoteFillService.mComponentName.getPackageName(), requestFlags);
        }
    }

    /* access modifiers changed from: private */
    public void dispatchOnFillRequestFailure(PendingFillRequest pendingRequest, CharSequence message) {
        this.mHandler.post(new Runnable(pendingRequest, message) {
            private final /* synthetic */ RemoteFillService.PendingFillRequest f$1;
            private final /* synthetic */ CharSequence f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                RemoteFillService.lambda$dispatchOnFillRequestFailure$1(RemoteFillService.this, this.f$1, this.f$2);
            }
        });
    }

    public static /* synthetic */ void lambda$dispatchOnFillRequestFailure$1(RemoteFillService remoteFillService, PendingFillRequest pendingRequest, CharSequence message) {
        if (remoteFillService.handleResponseCallbackCommon(pendingRequest)) {
            remoteFillService.mCallbacks.onFillRequestFailure(pendingRequest.mRequest.getId(), message, remoteFillService.mComponentName.getPackageName());
        }
    }

    /* access modifiers changed from: private */
    public void dispatchOnFillRequestTimeout(PendingFillRequest pendingRequest) {
        this.mHandler.post(new Runnable(pendingRequest) {
            private final /* synthetic */ RemoteFillService.PendingFillRequest f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                RemoteFillService.lambda$dispatchOnFillRequestTimeout$2(RemoteFillService.this, this.f$1);
            }
        });
    }

    public static /* synthetic */ void lambda$dispatchOnFillRequestTimeout$2(RemoteFillService remoteFillService, PendingFillRequest pendingRequest) {
        if (remoteFillService.handleResponseCallbackCommon(pendingRequest)) {
            remoteFillService.mCallbacks.onFillRequestTimeout(pendingRequest.mRequest.getId(), remoteFillService.mComponentName.getPackageName());
        }
    }

    /* access modifiers changed from: private */
    public void dispatchOnFillTimeout(ICancellationSignal cancellationSignal) {
        this.mHandler.post(new Runnable(cancellationSignal) {
            private final /* synthetic */ ICancellationSignal f$0;

            {
                this.f$0 = r1;
            }

            public final void run() {
                RemoteFillService.lambda$dispatchOnFillTimeout$3(this.f$0);
            }
        });
    }

    static /* synthetic */ void lambda$dispatchOnFillTimeout$3(ICancellationSignal cancellationSignal) {
        try {
            cancellationSignal.cancel();
        } catch (RemoteException e) {
            Slog.w(LOG_TAG, "Error calling cancellation signal: " + e);
        }
    }

    /* access modifiers changed from: private */
    public void dispatchOnSaveRequestSuccess(PendingRequest pendingRequest, IntentSender intentSender) {
        this.mHandler.post(new Runnable(pendingRequest, intentSender) {
            private final /* synthetic */ RemoteFillService.PendingRequest f$1;
            private final /* synthetic */ IntentSender f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                RemoteFillService.lambda$dispatchOnSaveRequestSuccess$4(RemoteFillService.this, this.f$1, this.f$2);
            }
        });
    }

    public static /* synthetic */ void lambda$dispatchOnSaveRequestSuccess$4(RemoteFillService remoteFillService, PendingRequest pendingRequest, IntentSender intentSender) {
        if (remoteFillService.handleResponseCallbackCommon(pendingRequest)) {
            remoteFillService.mCallbacks.onSaveRequestSuccess(remoteFillService.mComponentName.getPackageName(), intentSender);
        }
    }

    /* access modifiers changed from: private */
    public void dispatchOnSaveRequestFailure(PendingRequest pendingRequest, CharSequence message) {
        this.mHandler.post(new Runnable(pendingRequest, message) {
            private final /* synthetic */ RemoteFillService.PendingRequest f$1;
            private final /* synthetic */ CharSequence f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                RemoteFillService.lambda$dispatchOnSaveRequestFailure$5(RemoteFillService.this, this.f$1, this.f$2);
            }
        });
    }

    public static /* synthetic */ void lambda$dispatchOnSaveRequestFailure$5(RemoteFillService remoteFillService, PendingRequest pendingRequest, CharSequence message) {
        if (remoteFillService.handleResponseCallbackCommon(pendingRequest)) {
            remoteFillService.mCallbacks.onSaveRequestFailure(message, remoteFillService.mComponentName.getPackageName());
        }
    }

    private boolean handleResponseCallbackCommon(PendingRequest pendingRequest) {
        if (this.mDestroyed) {
            return false;
        }
        if (this.mPendingRequest == pendingRequest) {
            this.mPendingRequest = null;
        }
        if (this.mPendingRequest == null) {
            scheduleUnbind();
        }
        return true;
    }

    private boolean checkIfDestroyed() {
        if (this.mDestroyed && Helper.sVerbose) {
            Slog.v(LOG_TAG, "Not handling operation as service for " + this.mComponentName + " is already destroyed");
        }
        return this.mDestroyed;
    }
}
