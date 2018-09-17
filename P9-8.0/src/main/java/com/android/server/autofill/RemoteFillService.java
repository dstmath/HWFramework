package com.android.server.autofill;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.ICancellationSignal;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.IAutoFillService;
import android.service.autofill.IFillCallback;
import android.service.autofill.IFillCallback.Stub;
import android.service.autofill.ISaveCallback;
import android.service.autofill.SaveRequest;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.HandlerCaller;
import com.android.internal.os.HandlerCaller.Callback;
import com.android.server.FgThread;
import com.android.server.autofill.-$Lambda$JYqZriexGNVTrQ5cwTlcgjPSZFY.AnonymousClass1;
import com.android.server.autofill.-$Lambda$JYqZriexGNVTrQ5cwTlcgjPSZFY.AnonymousClass2;
import com.android.server.autofill.-$Lambda$JYqZriexGNVTrQ5cwTlcgjPSZFY.AnonymousClass3;
import com.android.server.autofill.-$Lambda$JYqZriexGNVTrQ5cwTlcgjPSZFY.AnonymousClass4;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;

final class RemoteFillService implements DeathRecipient {
    private static final String LOG_TAG = "RemoteFillService";
    private static final long TIMEOUT_IDLE_BIND_MILLIS = 5000;
    private static final long TIMEOUT_REMOTE_REQUEST_MILLIS = 5000;
    private IAutoFillService mAutoFillService;
    private boolean mBinding;
    private final FillServiceCallbacks mCallbacks;
    private boolean mCompleted;
    private final ComponentName mComponentName;
    private final Context mContext;
    private boolean mDestroyed;
    private final HandlerCaller mHandler;
    private final Intent mIntent;
    private PendingRequest mPendingRequest;
    private final ServiceConnection mServiceConnection = new RemoteServiceConnection(this, null);
    private boolean mServiceDied;
    private final int mUserId;

    public interface FillServiceCallbacks {
        void onFillRequestFailure(CharSequence charSequence, String str);

        void onFillRequestSuccess(int i, FillResponse fillResponse, int i2, String str);

        void onSaveRequestFailure(CharSequence charSequence, String str);

        void onSaveRequestSuccess(String str);

        void onServiceDied(RemoteFillService remoteFillService);
    }

    private final class MyHandler extends HandlerCaller {
        public static final int MSG_BINDER_DIED = 2;
        public static final int MSG_DESTROY = 1;
        public static final int MSG_ON_PENDING_REQUEST = 4;
        public static final int MSG_UNBIND = 3;

        public MyHandler(Context context) {
            super(context, FgThread.getHandler().getLooper(), new Callback(RemoteFillService.this) {
                public void executeMessage(Message message) {
                    if (this$0.mDestroyed) {
                        Slog.w(RemoteFillService.LOG_TAG, "Not handling " + message + " as service for " + this$0.mComponentName + " is already destroyed");
                        return;
                    }
                    switch (message.what) {
                        case 1:
                            this$0.handleDestroy();
                            break;
                        case 2:
                            this$0.handleBinderDied();
                            break;
                        case 3:
                            this$0.handleUnbind();
                            break;
                        case 4:
                            this$0.handlePendingRequest((PendingRequest) message.obj);
                            break;
                    }
                }
            }, false);
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

        abstract void fail(RemoteFillService remoteFillService);

        PendingRequest(RemoteFillService service) {
            this.mWeakService = new WeakReference(service);
            this.mServiceHandler = service.mHandler.getHandler();
            this.mTimeoutTrigger = new -$Lambda$JYqZriexGNVTrQ5cwTlcgjPSZFY(this);
            this.mServiceHandler.postAtTime(this.mTimeoutTrigger, SystemClock.uptimeMillis() + 5000);
        }

        /* JADX WARNING: Missing block: B:11:0x000d, code:
            android.util.Slog.w(com.android.server.autofill.RemoteFillService.LOG_TAG, getClass().getSimpleName() + " timed out");
            r0 = (com.android.server.autofill.RemoteFillService) r4.mWeakService.get();
     */
        /* JADX WARNING: Missing block: B:12:0x0037, code:
            if (r0 == null) goto L_0x003c;
     */
        /* JADX WARNING: Missing block: B:13:0x0039, code:
            fail(r0);
     */
        /* JADX WARNING: Missing block: B:14:0x003c, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        /* synthetic */ void lambda$-com_android_server_autofill_RemoteFillService$PendingRequest_15770() {
            synchronized (this.mLock) {
                if (this.mCancelled) {
                    return;
                }
                this.mCompleted = true;
            }
        }

        protected RemoteFillService getService() {
            return (RemoteFillService) this.mWeakService.get();
        }

        protected final boolean finish() {
            synchronized (this.mLock) {
                if (this.mCompleted || this.mCancelled) {
                    return false;
                }
                this.mCompleted = true;
                this.mServiceHandler.removeCallbacks(this.mTimeoutTrigger);
                return true;
            }
        }

        protected boolean isCancelledLocked() {
            return this.mCancelled;
        }

        boolean cancel() {
            synchronized (this.mLock) {
                if (this.mCancelled || this.mCompleted) {
                    return false;
                }
                this.mCancelled = true;
                this.mServiceHandler.removeCallbacks(this.mTimeoutTrigger);
                return true;
            }
        }

        boolean isFinal() {
            return false;
        }
    }

    private static final class PendingFillRequest extends PendingRequest {
        private final IFillCallback mCallback;
        private ICancellationSignal mCancellation;
        private final FillRequest mRequest;

        public PendingFillRequest(final FillRequest request, RemoteFillService service) {
            super(service);
            this.mRequest = request;
            this.mCallback = new Stub() {
                public void onCancellable(ICancellationSignal cancellation) {
                    synchronized (PendingFillRequest.this.mLock) {
                        boolean cancelled;
                        synchronized (PendingFillRequest.this.mLock) {
                            PendingFillRequest.this.mCancellation = cancellation;
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
                    return;
                }

                public void onSuccess(FillResponse response) {
                    if (PendingFillRequest.this.finish()) {
                        RemoteFillService remoteService = PendingFillRequest.this.getService();
                        if (remoteService != null) {
                            remoteService.dispatchOnFillRequestSuccess(PendingFillRequest.this, AnonymousClass1.getCallingUid(), request.getFlags(), response);
                        }
                    }
                }

                public void onFailure(CharSequence message) {
                    if (PendingFillRequest.this.finish()) {
                        RemoteFillService remoteService = PendingFillRequest.this.getService();
                        if (remoteService != null) {
                            remoteService.dispatchOnFillRequestFailure(PendingFillRequest.this, message);
                        }
                    }
                }
            };
        }

        void fail(RemoteFillService remoteService) {
            remoteService.dispatchOnFillRequestFailure(this, null);
        }

        public void run() {
            RemoteFillService remoteService = getService();
            if (remoteService != null && remoteService.mAutoFillService != null) {
                try {
                    remoteService.mAutoFillService.onFillRequest(this.mRequest, this.mCallback);
                } catch (RemoteException e) {
                    Slog.e(RemoteFillService.LOG_TAG, "Error calling on fill request", e);
                    remoteService.dispatchOnFillRequestFailure(this, null);
                } catch (IndexOutOfBoundsException e2) {
                    Slog.e(RemoteFillService.LOG_TAG, "onFillRequest has Exception : IndexOutOfBoundsException");
                }
            }
        }

        public boolean cancel() {
            if (!super.cancel()) {
                return false;
            }
            ICancellationSignal cancellation = this.mCancellation;
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

    private static final class PendingSaveRequest extends PendingRequest {
        private final ISaveCallback mCallback = new ISaveCallback.Stub() {
            public void onSuccess() {
                if (PendingSaveRequest.this.finish()) {
                    RemoteFillService remoteService = PendingSaveRequest.this.getService();
                    if (remoteService != null) {
                        remoteService.dispatchOnSaveRequestSuccess(PendingSaveRequest.this);
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

        void fail(RemoteFillService remoteService) {
            remoteService.dispatchOnSaveRequestFailure(this, null);
        }

        public void run() {
            RemoteFillService remoteService = getService();
            if (remoteService != null) {
                try {
                    remoteService.mAutoFillService.onSaveRequest(this.mRequest, this.mCallback);
                } catch (RemoteException e) {
                    Slog.e(RemoteFillService.LOG_TAG, "Error calling on save request", e);
                    remoteService.dispatchOnFillRequestFailure(this, null);
                }
            }
        }

        public boolean isFinal() {
            return true;
        }
    }

    private class RemoteServiceConnection implements ServiceConnection {
        /* synthetic */ RemoteServiceConnection(RemoteFillService this$0, RemoteServiceConnection -this1) {
            this();
        }

        private RemoteServiceConnection() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            if (RemoteFillService.this.mDestroyed || (RemoteFillService.this.mBinding ^ 1) != 0) {
                try {
                    RemoteFillService.this.mContext.unbindService(RemoteFillService.this.mServiceConnection);
                } catch (IllegalArgumentException e) {
                    Slog.e(RemoteFillService.LOG_TAG, "Service not registered: " + e);
                }
                return;
            }
            RemoteFillService.this.mBinding = false;
            RemoteFillService.this.mAutoFillService = IAutoFillService.Stub.asInterface(service);
            try {
                service.linkToDeath(RemoteFillService.this, 0);
                try {
                    RemoteFillService.this.mAutoFillService.onConnectedStateChanged(true);
                } catch (RemoteException e2) {
                    Slog.w(RemoteFillService.LOG_TAG, "Exception calling onConnected(): " + e2);
                }
                if (RemoteFillService.this.mPendingRequest != null) {
                    PendingRequest pendingRequest = RemoteFillService.this.mPendingRequest;
                    RemoteFillService.this.mPendingRequest = null;
                    RemoteFillService.this.handlePendingRequest(pendingRequest);
                }
                RemoteFillService.this.mServiceDied = false;
            } catch (RemoteException e3) {
                RemoteFillService.this.handleBinderDied();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            RemoteFillService.this.mBinding = true;
            RemoteFillService.this.mAutoFillService = null;
        }
    }

    public RemoteFillService(Context context, ComponentName componentName, int userId, FillServiceCallbacks callbacks) {
        this.mContext = context;
        this.mCallbacks = callbacks;
        this.mComponentName = componentName;
        this.mIntent = new Intent("android.service.autofill.AutofillService").setComponent(this.mComponentName);
        this.mUserId = userId;
        this.mHandler = new MyHandler(context);
    }

    public void destroy() {
        this.mHandler.obtainMessage(1).sendToTarget();
    }

    private void handleDestroy() {
        if (this.mPendingRequest != null) {
            this.mPendingRequest.cancel();
            this.mPendingRequest = null;
        }
        ensureUnbound();
        this.mDestroyed = true;
    }

    public void binderDied() {
        this.mHandler.obtainMessage(2).sendToTarget();
    }

    private void handleBinderDied() {
        if (this.mAutoFillService != null) {
            this.mAutoFillService.asBinder().unlinkToDeath(this, 0);
        }
        this.mAutoFillService = null;
        this.mServiceDied = true;
        this.mCallbacks.onServiceDied(this);
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
        this.mHandler.obtainMessageO(4, new PendingFillRequest(request, this)).sendToTarget();
    }

    public void onSaveRequest(SaveRequest request) {
        cancelScheduledUnbind();
        this.mHandler.obtainMessageO(4, new PendingSaveRequest(request, this)).sendToTarget();
    }

    public void dump(String prefix, PrintWriter pw) {
        String tab = "  ";
        pw.append(prefix).append("service:").println();
        pw.append(prefix).append(tab).append("userId=").append(String.valueOf(this.mUserId)).println();
        pw.append(prefix).append(tab).append("componentName=").append(this.mComponentName.flattenToString()).println();
        pw.append(prefix).append(tab).append("destroyed=").append(String.valueOf(this.mDestroyed)).println();
        pw.append(prefix).append(tab).append("bound=").append(String.valueOf(isBound())).println();
        pw.append(prefix).append(tab).append("hasPendingRequest=").append(String.valueOf(this.mPendingRequest != null)).println();
        pw.println();
    }

    private void cancelScheduledUnbind() {
        this.mHandler.removeMessages(3);
    }

    private void scheduleUnbind() {
        cancelScheduledUnbind();
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(3), 5000);
    }

    private void handleUnbind() {
        ensureUnbound();
    }

    private void handlePendingRequest(PendingRequest pendingRequest) {
        if (!this.mDestroyed && !this.mCompleted) {
            if (isBound()) {
                if (Helper.sVerbose) {
                    Slog.v(LOG_TAG, "[user: " + this.mUserId + "] handlePendingRequest()");
                }
                pendingRequest.run();
                if (pendingRequest.isFinal()) {
                    this.mCompleted = true;
                }
            } else {
                if (this.mPendingRequest != null) {
                    this.mPendingRequest.cancel();
                }
                this.mPendingRequest = pendingRequest;
                ensureBound();
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
            if (!this.mContext.bindServiceAsUser(this.mIntent, this.mServiceConnection, 67108865, new UserHandle(this.mUserId))) {
                if (Helper.sDebug) {
                    Slog.d(LOG_TAG, "[user: " + this.mUserId + "] could not bind to " + this.mIntent);
                }
                this.mBinding = false;
                if (!this.mServiceDied) {
                    handleBinderDied();
                }
            }
        }
    }

    private void ensureUnbound() {
        if (isBound() || (this.mBinding ^ 1) == 0) {
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
                    this.mAutoFillService.asBinder().unlinkToDeath(this, 0);
                    this.mAutoFillService = null;
                }
            }
            try {
                this.mContext.unbindService(this.mServiceConnection);
            } catch (IllegalArgumentException e2) {
                Slog.e(LOG_TAG, "Service not registered: " + e2);
            }
        }
    }

    private void dispatchOnFillRequestSuccess(PendingRequest pendingRequest, int callingUid, int requestFlags, FillResponse response) {
        this.mHandler.getHandler().post(new AnonymousClass4(requestFlags, callingUid, this, pendingRequest, response));
    }

    /* synthetic */ void lambda$-com_android_server_autofill_RemoteFillService_10547(PendingRequest pendingRequest, int requestFlags, FillResponse response, int callingUid) {
        if (handleResponseCallbackCommon(pendingRequest)) {
            this.mCallbacks.onFillRequestSuccess(requestFlags, response, callingUid, this.mComponentName.getPackageName());
        }
    }

    private void dispatchOnFillRequestFailure(PendingRequest pendingRequest, CharSequence message) {
        this.mHandler.getHandler().post(new AnonymousClass2(this, pendingRequest, message));
    }

    /* synthetic */ void lambda$-com_android_server_autofill_RemoteFillService_10942(PendingRequest pendingRequest, CharSequence message) {
        if (handleResponseCallbackCommon(pendingRequest)) {
            this.mCallbacks.onFillRequestFailure(message, this.mComponentName.getPackageName());
        }
    }

    private void dispatchOnSaveRequestSuccess(PendingRequest pendingRequest) {
        this.mHandler.getHandler().post(new AnonymousClass1(this, pendingRequest));
    }

    /* synthetic */ void lambda$-com_android_server_autofill_RemoteFillService_11252(PendingRequest pendingRequest) {
        if (handleResponseCallbackCommon(pendingRequest)) {
            this.mCallbacks.onSaveRequestSuccess(this.mComponentName.getPackageName());
        }
    }

    private void dispatchOnSaveRequestFailure(PendingRequest pendingRequest, CharSequence message) {
        this.mHandler.getHandler().post(new AnonymousClass3(this, pendingRequest, message));
    }

    /* synthetic */ void lambda$-com_android_server_autofill_RemoteFillService_11587(PendingRequest pendingRequest, CharSequence message) {
        if (handleResponseCallbackCommon(pendingRequest)) {
            this.mCallbacks.onSaveRequestFailure(message, this.mComponentName.getPackageName());
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
}
