package com.android.server.textclassifier;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.service.textclassifier.ITextClassificationCallback;
import android.service.textclassifier.ITextClassifierService;
import android.service.textclassifier.ITextLinksCallback;
import android.service.textclassifier.ITextSelectionCallback;
import android.service.textclassifier.TextClassifierService;
import android.util.Slog;
import android.util.SparseArray;
import android.view.textclassifier.SelectionEvent;
import android.view.textclassifier.TextClassification;
import android.view.textclassifier.TextClassificationContext;
import android.view.textclassifier.TextClassificationSessionId;
import android.view.textclassifier.TextLinks;
import android.view.textclassifier.TextSelection;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.FunctionalUtils;
import com.android.internal.util.Preconditions;
import com.android.server.SystemService;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Consumer;

public final class TextClassificationManagerService extends ITextClassifierService.Stub {
    private static final String LOG_TAG = "TextClassificationManagerService";
    private final Context mContext;
    /* access modifiers changed from: private */
    public final Object mLock;
    @GuardedBy("mLock")
    final SparseArray<UserState> mUserStates;

    public static final class Lifecycle extends SystemService {
        private final TextClassificationManagerService mManagerService;

        public Lifecycle(Context context) {
            super(context);
            this.mManagerService = new TextClassificationManagerService(context);
        }

        /* JADX WARNING: type inference failed for: r1v1, types: [com.android.server.textclassifier.TextClassificationManagerService, android.os.IBinder] */
        public void onStart() {
            try {
                publishBinderService("textclassification", this.mManagerService);
            } catch (Throwable t) {
                Slog.e(TextClassificationManagerService.LOG_TAG, "Could not start the TextClassificationManagerService.", t);
            }
        }

        public void onStartUser(int userId) {
            processAnyPendingWork(userId);
        }

        public void onUnlockUser(int userId) {
            processAnyPendingWork(userId);
        }

        private void processAnyPendingWork(int userId) {
            synchronized (this.mManagerService.mLock) {
                boolean unused = this.mManagerService.getUserStateLocked(userId).bindIfHasPendingRequestsLocked();
            }
        }

        public void onStopUser(int userId) {
            synchronized (this.mManagerService.mLock) {
                UserState userState = this.mManagerService.peekUserStateLocked(userId);
                if (userState != null) {
                    userState.mConnection.cleanupService();
                    this.mManagerService.mUserStates.remove(userId);
                }
            }
        }
    }

    private static final class PendingRequest implements IBinder.DeathRecipient {
        /* access modifiers changed from: private */
        public final IBinder mBinder;
        /* access modifiers changed from: private */
        public final Runnable mOnServiceFailure;
        @GuardedBy("mLock")
        private final UserState mOwningUser;
        /* access modifiers changed from: private */
        public final Runnable mRequest;
        private final TextClassificationManagerService mService;

        PendingRequest(FunctionalUtils.ThrowingRunnable request, FunctionalUtils.ThrowingRunnable onServiceFailure, IBinder binder, TextClassificationManagerService service, UserState owningUser) {
            this.mRequest = TextClassificationManagerService.logOnFailure((FunctionalUtils.ThrowingRunnable) Preconditions.checkNotNull(request), "handling pending request");
            this.mOnServiceFailure = TextClassificationManagerService.logOnFailure(onServiceFailure, "notifying callback of service failure");
            this.mBinder = binder;
            this.mService = service;
            this.mOwningUser = owningUser;
            if (this.mBinder != null) {
                try {
                    this.mBinder.linkToDeath(this, 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        public void binderDied() {
            synchronized (this.mService.mLock) {
                removeLocked();
            }
        }

        @GuardedBy("mLock")
        private void removeLocked() {
            this.mOwningUser.mPendingRequests.remove(this);
            if (this.mBinder != null) {
                this.mBinder.unlinkToDeath(this, 0);
            }
        }
    }

    private static final class UserState {
        @GuardedBy("mLock")
        boolean mBinding;
        final TextClassifierServiceConnection mConnection;
        private final Context mContext;
        /* access modifiers changed from: private */
        public final Object mLock;
        @GuardedBy("mLock")
        final Queue<PendingRequest> mPendingRequests;
        @GuardedBy("mLock")
        ITextClassifierService mService;
        final int mUserId;

        private final class TextClassifierServiceConnection implements ServiceConnection {
            private TextClassifierServiceConnection() {
            }

            public void onServiceConnected(ComponentName name, IBinder service) {
                init(ITextClassifierService.Stub.asInterface(service));
            }

            public void onServiceDisconnected(ComponentName name) {
                cleanupService();
            }

            public void onBindingDied(ComponentName name) {
                cleanupService();
            }

            public void onNullBinding(ComponentName name) {
                cleanupService();
            }

            /* access modifiers changed from: package-private */
            public void cleanupService() {
                init(null);
            }

            private void init(ITextClassifierService service) {
                synchronized (UserState.this.mLock) {
                    UserState.this.mService = service;
                    UserState.this.mBinding = false;
                    UserState.this.handlePendingRequestsLocked();
                }
            }
        }

        private UserState(int userId, Context context, Object lock) {
            this.mConnection = new TextClassifierServiceConnection();
            this.mPendingRequests = new ArrayDeque();
            this.mUserId = userId;
            this.mContext = (Context) Preconditions.checkNotNull(context);
            this.mLock = Preconditions.checkNotNull(lock);
        }

        /* access modifiers changed from: package-private */
        @GuardedBy("mLock")
        public boolean isBoundLocked() {
            return this.mService != null;
        }

        /* access modifiers changed from: private */
        @GuardedBy("mLock")
        public void handlePendingRequestsLocked() {
            while (true) {
                PendingRequest poll = this.mPendingRequests.poll();
                PendingRequest request = poll;
                if (poll != null) {
                    if (isBoundLocked()) {
                        request.mRequest.run();
                    } else if (request.mOnServiceFailure != null) {
                        request.mOnServiceFailure.run();
                    }
                    if (request.mBinder != null) {
                        request.mBinder.unlinkToDeath(request, 0);
                    }
                } else {
                    return;
                }
            }
        }

        /* access modifiers changed from: private */
        public boolean bindIfHasPendingRequestsLocked() {
            return !this.mPendingRequests.isEmpty() && bindLocked();
        }

        /* access modifiers changed from: private */
        public boolean bindLocked() {
            if (isBoundLocked() || this.mBinding) {
                return true;
            }
            long identity = Binder.clearCallingIdentity();
            try {
                ComponentName componentName = TextClassifierService.getServiceComponentName(this.mContext);
                if (componentName == null) {
                    return false;
                }
                Intent serviceIntent = new Intent("android.service.textclassifier.TextClassifierService").setComponent(componentName);
                Slog.d(TextClassificationManagerService.LOG_TAG, "Binding to " + serviceIntent.getComponent());
                boolean willBind = this.mContext.bindServiceAsUser(serviceIntent, this.mConnection, 67108865, UserHandle.of(this.mUserId));
                this.mBinding = willBind;
                Binder.restoreCallingIdentity(identity);
                return willBind;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    private TextClassificationManagerService(Context context) {
        this.mUserStates = new SparseArray<>();
        this.mContext = (Context) Preconditions.checkNotNull(context);
        this.mLock = new Object();
    }

    public void onSuggestSelection(TextClassificationSessionId sessionId, TextSelection.Request request, ITextSelectionCallback callback) throws RemoteException {
        Preconditions.checkNotNull(request);
        Preconditions.checkNotNull(callback);
        synchronized (this.mLock) {
            UserState userState = getCallingUserStateLocked();
            if (!userState.bindLocked()) {
                callback.onFailure();
            } else if (userState.isBoundLocked()) {
                userState.mService.onSuggestSelection(sessionId, request, callback);
            } else {
                Queue<PendingRequest> queue = userState.mPendingRequests;
                $$Lambda$TextClassificationManagerService$Oay4QGGKO1MM7dDcB0KN_1JmqZA r3 = new FunctionalUtils.ThrowingRunnable(sessionId, request, callback) {
                    private final /* synthetic */ TextClassificationSessionId f$1;
                    private final /* synthetic */ TextSelection.Request f$2;
                    private final /* synthetic */ ITextSelectionCallback f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    public final void runOrThrow() {
                        TextClassificationManagerService.this.onSuggestSelection(this.f$1, this.f$2, this.f$3);
                    }
                };
                Objects.requireNonNull(callback);
                PendingRequest pendingRequest = new PendingRequest(r3, new FunctionalUtils.ThrowingRunnable(callback) {
                    private final /* synthetic */ ITextSelectionCallback f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void runOrThrow() {
                        this.f$0.onFailure();
                    }
                }, callback.asBinder(), this, userState);
                queue.add(pendingRequest);
            }
        }
    }

    public void onClassifyText(TextClassificationSessionId sessionId, TextClassification.Request request, ITextClassificationCallback callback) throws RemoteException {
        Preconditions.checkNotNull(request);
        Preconditions.checkNotNull(callback);
        synchronized (this.mLock) {
            UserState userState = getCallingUserStateLocked();
            if (!userState.bindLocked()) {
                callback.onFailure();
            } else if (userState.isBoundLocked()) {
                userState.mService.onClassifyText(sessionId, request, callback);
            } else {
                Queue<PendingRequest> queue = userState.mPendingRequests;
                $$Lambda$TextClassificationManagerService$0ahBOnx4jsgbPYQhVmIdEMzPn5Q r3 = new FunctionalUtils.ThrowingRunnable(sessionId, request, callback) {
                    private final /* synthetic */ TextClassificationSessionId f$1;
                    private final /* synthetic */ TextClassification.Request f$2;
                    private final /* synthetic */ ITextClassificationCallback f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    public final void runOrThrow() {
                        TextClassificationManagerService.this.onClassifyText(this.f$1, this.f$2, this.f$3);
                    }
                };
                Objects.requireNonNull(callback);
                PendingRequest pendingRequest = new PendingRequest(r3, new FunctionalUtils.ThrowingRunnable(callback) {
                    private final /* synthetic */ ITextClassificationCallback f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void runOrThrow() {
                        this.f$0.onFailure();
                    }
                }, callback.asBinder(), this, userState);
                queue.add(pendingRequest);
            }
        }
    }

    public void onGenerateLinks(TextClassificationSessionId sessionId, TextLinks.Request request, ITextLinksCallback callback) throws RemoteException {
        Preconditions.checkNotNull(request);
        Preconditions.checkNotNull(callback);
        synchronized (this.mLock) {
            UserState userState = getCallingUserStateLocked();
            if (!userState.bindLocked()) {
                callback.onFailure();
            } else if (userState.isBoundLocked()) {
                userState.mService.onGenerateLinks(sessionId, request, callback);
            } else {
                Queue<PendingRequest> queue = userState.mPendingRequests;
                $$Lambda$TextClassificationManagerService$O5SqJ3O93lhUbxb9PI9hMySaM r3 = new FunctionalUtils.ThrowingRunnable(sessionId, request, callback) {
                    private final /* synthetic */ TextClassificationSessionId f$1;
                    private final /* synthetic */ TextLinks.Request f$2;
                    private final /* synthetic */ ITextLinksCallback f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    public final void runOrThrow() {
                        TextClassificationManagerService.this.onGenerateLinks(this.f$1, this.f$2, this.f$3);
                    }
                };
                Objects.requireNonNull(callback);
                PendingRequest pendingRequest = new PendingRequest(r3, new FunctionalUtils.ThrowingRunnable(callback) {
                    private final /* synthetic */ ITextLinksCallback f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void runOrThrow() {
                        this.f$0.onFailure();
                    }
                }, callback.asBinder(), this, userState);
                queue.add(pendingRequest);
            }
        }
    }

    public void onSelectionEvent(TextClassificationSessionId sessionId, SelectionEvent event) throws RemoteException {
        Preconditions.checkNotNull(event);
        validateInput(event.getPackageName(), this.mContext);
        synchronized (this.mLock) {
            UserState userState = getCallingUserStateLocked();
            if (userState.isBoundLocked()) {
                userState.mService.onSelectionEvent(sessionId, event);
            } else {
                Queue<PendingRequest> queue = userState.mPendingRequests;
                PendingRequest pendingRequest = new PendingRequest(new FunctionalUtils.ThrowingRunnable(sessionId, event) {
                    private final /* synthetic */ TextClassificationSessionId f$1;
                    private final /* synthetic */ SelectionEvent f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void runOrThrow() {
                        TextClassificationManagerService.this.onSelectionEvent(this.f$1, this.f$2);
                    }
                }, null, null, this, userState);
                queue.add(pendingRequest);
            }
        }
    }

    public void onCreateTextClassificationSession(TextClassificationContext classificationContext, TextClassificationSessionId sessionId) throws RemoteException {
        Preconditions.checkNotNull(sessionId);
        Preconditions.checkNotNull(classificationContext);
        validateInput(classificationContext.getPackageName(), this.mContext);
        synchronized (this.mLock) {
            UserState userState = getCallingUserStateLocked();
            if (userState.isBoundLocked()) {
                userState.mService.onCreateTextClassificationSession(classificationContext, sessionId);
            } else {
                Queue<PendingRequest> queue = userState.mPendingRequests;
                PendingRequest pendingRequest = new PendingRequest(new FunctionalUtils.ThrowingRunnable(classificationContext, sessionId) {
                    private final /* synthetic */ TextClassificationContext f$1;
                    private final /* synthetic */ TextClassificationSessionId f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void runOrThrow() {
                        TextClassificationManagerService.this.onCreateTextClassificationSession(this.f$1, this.f$2);
                    }
                }, null, null, this, userState);
                queue.add(pendingRequest);
            }
        }
    }

    public void onDestroyTextClassificationSession(TextClassificationSessionId sessionId) throws RemoteException {
        Preconditions.checkNotNull(sessionId);
        synchronized (this.mLock) {
            UserState userState = getCallingUserStateLocked();
            if (userState.isBoundLocked()) {
                userState.mService.onDestroyTextClassificationSession(sessionId);
            } else {
                Queue<PendingRequest> queue = userState.mPendingRequests;
                PendingRequest pendingRequest = new PendingRequest(new FunctionalUtils.ThrowingRunnable(sessionId) {
                    private final /* synthetic */ TextClassificationSessionId f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void runOrThrow() {
                        TextClassificationManagerService.this.onDestroyTextClassificationSession(this.f$1);
                    }
                }, null, null, this, userState);
                queue.add(pendingRequest);
            }
        }
    }

    private UserState getCallingUserStateLocked() {
        return getUserStateLocked(UserHandle.getCallingUserId());
    }

    /* access modifiers changed from: private */
    public UserState getUserStateLocked(int userId) {
        UserState result = this.mUserStates.get(userId);
        if (result != null) {
            return result;
        }
        UserState result2 = new UserState(userId, this.mContext, this.mLock);
        this.mUserStates.put(userId, result2);
        return result2;
    }

    /* access modifiers changed from: package-private */
    public UserState peekUserStateLocked(int userId) {
        return this.mUserStates.get(userId);
    }

    /* access modifiers changed from: private */
    public static Runnable logOnFailure(FunctionalUtils.ThrowingRunnable r, String opDesc) {
        if (r == null) {
            return null;
        }
        return FunctionalUtils.handleExceptions(r, new Consumer(opDesc) {
            private final /* synthetic */ String f$0;

            {
                this.f$0 = r1;
            }

            public final void accept(Object obj) {
                Slog.d(TextClassificationManagerService.LOG_TAG, "Error " + this.f$0 + ": " + ((Throwable) obj).getMessage());
            }
        });
    }

    private static void validateInput(String packageName, Context context) throws RemoteException {
        try {
            boolean z = false;
            if (Binder.getCallingUid() == context.getPackageManager().getPackageUid(packageName, 0)) {
                z = true;
            }
            Preconditions.checkArgument(z);
        } catch (PackageManager.NameNotFoundException | IllegalArgumentException | NullPointerException e) {
            throw new RemoteException(e.getMessage());
        }
    }
}
