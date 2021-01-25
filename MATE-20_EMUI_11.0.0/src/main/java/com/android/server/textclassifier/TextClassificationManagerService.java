package com.android.server.textclassifier;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.service.textclassifier.ITextClassifierCallback;
import android.service.textclassifier.ITextClassifierService;
import android.service.textclassifier.TextClassifierService;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.SparseArray;
import android.view.textclassifier.ConversationActions;
import android.view.textclassifier.SelectionEvent;
import android.view.textclassifier.TextClassification;
import android.view.textclassifier.TextClassificationContext;
import android.view.textclassifier.TextClassificationManager;
import android.view.textclassifier.TextClassificationSessionId;
import android.view.textclassifier.TextClassifierEvent;
import android.view.textclassifier.TextLanguage;
import android.view.textclassifier.TextLinks;
import android.view.textclassifier.TextSelection;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FunctionalUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.server.SystemService;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Consumer;

public final class TextClassificationManagerService extends ITextClassifierService.Stub {
    private static final String LOG_TAG = "TextClassificationManagerService";
    private final Context mContext;
    private final Object mLock;
    @GuardedBy({"mLock"})
    private final Map<TextClassificationSessionId, Integer> mSessionUserIds;
    @GuardedBy({"mLock"})
    final SparseArray<UserState> mUserStates;

    public static final class Lifecycle extends SystemService {
        private final TextClassificationManagerService mManagerService;

        public Lifecycle(Context context) {
            super(context);
            this.mManagerService = new TextClassificationManagerService(context);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.android.server.textclassifier.TextClassificationManagerService$Lifecycle */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r1v1, types: [com.android.server.textclassifier.TextClassificationManagerService, android.os.IBinder] */
        /* JADX WARNING: Unknown variable types count: 1 */
        @Override // com.android.server.SystemService
        public void onStart() {
            try {
                publishBinderService("textclassification", this.mManagerService);
            } catch (Throwable t) {
                Slog.e(TextClassificationManagerService.LOG_TAG, "Could not start the TextClassificationManagerService.", t);
            }
        }

        @Override // com.android.server.SystemService
        public void onStartUser(int userId) {
            processAnyPendingWork(userId);
        }

        @Override // com.android.server.SystemService
        public void onUnlockUser(int userId) {
            processAnyPendingWork(userId);
        }

        private void processAnyPendingWork(int userId) {
            synchronized (this.mManagerService.mLock) {
                this.mManagerService.getUserStateLocked(userId).bindIfHasPendingRequestsLocked();
            }
        }

        @Override // com.android.server.SystemService
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

    private TextClassificationManagerService(Context context) {
        this.mUserStates = new SparseArray<>();
        this.mSessionUserIds = new ArrayMap();
        this.mContext = (Context) Preconditions.checkNotNull(context);
        this.mLock = new Object();
    }

    /* renamed from: onSuggestSelection */
    public void lambda$onSuggestSelection$0$TextClassificationManagerService(TextClassificationSessionId sessionId, TextSelection.Request request, ITextClassifierCallback callback) throws RemoteException {
        Preconditions.checkNotNull(request);
        Preconditions.checkNotNull(callback);
        int userId = request.getUserId();
        validateInput(this.mContext, request.getCallingPackageName(), userId);
        synchronized (this.mLock) {
            UserState userState = getUserStateLocked(userId);
            if (!userState.bindLocked()) {
                callback.onFailure();
            } else if (userState.isBoundLocked()) {
                userState.mService.onSuggestSelection(sessionId, request, callback);
            } else {
                Queue<PendingRequest> queue = userState.mPendingRequests;
                $$Lambda$TextClassificationManagerService$Fy5j26FLkbnEPhoh1kWzQnYhcm8 r4 = new FunctionalUtils.ThrowingRunnable(sessionId, request, callback) {
                    /* class com.android.server.textclassifier.$$Lambda$TextClassificationManagerService$Fy5j26FLkbnEPhoh1kWzQnYhcm8 */
                    private final /* synthetic */ TextClassificationSessionId f$1;
                    private final /* synthetic */ TextSelection.Request f$2;
                    private final /* synthetic */ ITextClassifierCallback f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    public final void runOrThrow() {
                        TextClassificationManagerService.this.lambda$onSuggestSelection$0$TextClassificationManagerService(this.f$1, this.f$2, this.f$3);
                    }
                };
                Objects.requireNonNull(callback);
                queue.add(new PendingRequest(r4, new FunctionalUtils.ThrowingRunnable(callback) {
                    /* class com.android.server.textclassifier.$$Lambda$k7KcqZH2A0AukChaKa6Xru13_Q */
                    private final /* synthetic */ ITextClassifierCallback f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void runOrThrow() {
                        this.f$0.onFailure();
                    }
                }, callback.asBinder(), this, userState));
            }
        }
    }

    /* renamed from: onClassifyText */
    public void lambda$onClassifyText$1$TextClassificationManagerService(TextClassificationSessionId sessionId, TextClassification.Request request, ITextClassifierCallback callback) throws RemoteException {
        Preconditions.checkNotNull(request);
        Preconditions.checkNotNull(callback);
        int userId = request.getUserId();
        validateInput(this.mContext, request.getCallingPackageName(), userId);
        synchronized (this.mLock) {
            UserState userState = getUserStateLocked(userId);
            if (!userState.bindLocked()) {
                callback.onFailure();
            } else if (userState.isBoundLocked()) {
                userState.mService.onClassifyText(sessionId, request, callback);
            } else {
                Queue<PendingRequest> queue = userState.mPendingRequests;
                $$Lambda$TextClassificationManagerService$aNIcwykiT4wOQ8InWE4Im6x6kE r4 = new FunctionalUtils.ThrowingRunnable(sessionId, request, callback) {
                    /* class com.android.server.textclassifier.$$Lambda$TextClassificationManagerService$aNIcwykiT4wOQ8InWE4Im6x6kE */
                    private final /* synthetic */ TextClassificationSessionId f$1;
                    private final /* synthetic */ TextClassification.Request f$2;
                    private final /* synthetic */ ITextClassifierCallback f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    public final void runOrThrow() {
                        TextClassificationManagerService.this.lambda$onClassifyText$1$TextClassificationManagerService(this.f$1, this.f$2, this.f$3);
                    }
                };
                Objects.requireNonNull(callback);
                queue.add(new PendingRequest(r4, new FunctionalUtils.ThrowingRunnable(callback) {
                    /* class com.android.server.textclassifier.$$Lambda$k7KcqZH2A0AukChaKa6Xru13_Q */
                    private final /* synthetic */ ITextClassifierCallback f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void runOrThrow() {
                        this.f$0.onFailure();
                    }
                }, callback.asBinder(), this, userState));
            }
        }
    }

    /* renamed from: onGenerateLinks */
    public void lambda$onGenerateLinks$2$TextClassificationManagerService(TextClassificationSessionId sessionId, TextLinks.Request request, ITextClassifierCallback callback) throws RemoteException {
        Preconditions.checkNotNull(request);
        Preconditions.checkNotNull(callback);
        int userId = request.getUserId();
        validateInput(this.mContext, request.getCallingPackageName(), userId);
        synchronized (this.mLock) {
            UserState userState = getUserStateLocked(userId);
            if (!userState.bindLocked()) {
                callback.onFailure();
            } else if (userState.isBoundLocked()) {
                userState.mService.onGenerateLinks(sessionId, request, callback);
            } else {
                Queue<PendingRequest> queue = userState.mPendingRequests;
                $$Lambda$TextClassificationManagerService$1N5hVEvgYS5VzkBAP5HLq01CQI r4 = new FunctionalUtils.ThrowingRunnable(sessionId, request, callback) {
                    /* class com.android.server.textclassifier.$$Lambda$TextClassificationManagerService$1N5hVEvgYS5VzkBAP5HLq01CQI */
                    private final /* synthetic */ TextClassificationSessionId f$1;
                    private final /* synthetic */ TextLinks.Request f$2;
                    private final /* synthetic */ ITextClassifierCallback f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    public final void runOrThrow() {
                        TextClassificationManagerService.this.lambda$onGenerateLinks$2$TextClassificationManagerService(this.f$1, this.f$2, this.f$3);
                    }
                };
                Objects.requireNonNull(callback);
                queue.add(new PendingRequest(r4, new FunctionalUtils.ThrowingRunnable(callback) {
                    /* class com.android.server.textclassifier.$$Lambda$k7KcqZH2A0AukChaKa6Xru13_Q */
                    private final /* synthetic */ ITextClassifierCallback f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void runOrThrow() {
                        this.f$0.onFailure();
                    }
                }, callback.asBinder(), this, userState));
            }
        }
    }

    /* renamed from: onSelectionEvent */
    public void lambda$onSelectionEvent$3$TextClassificationManagerService(TextClassificationSessionId sessionId, SelectionEvent event) throws RemoteException {
        Preconditions.checkNotNull(event);
        int userId = event.getUserId();
        validateInput(this.mContext, event.getPackageName(), userId);
        synchronized (this.mLock) {
            UserState userState = getUserStateLocked(userId);
            if (userState.isBoundLocked()) {
                userState.mService.onSelectionEvent(sessionId, event);
            } else {
                userState.mPendingRequests.add(new PendingRequest(new FunctionalUtils.ThrowingRunnable(sessionId, event) {
                    /* class com.android.server.textclassifier.$$Lambda$TextClassificationManagerService$Xo8FJ3LmQoamgJ2foxZOcSn70c */
                    private final /* synthetic */ TextClassificationSessionId f$1;
                    private final /* synthetic */ SelectionEvent f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void runOrThrow() {
                        TextClassificationManagerService.this.lambda$onSelectionEvent$3$TextClassificationManagerService(this.f$1, this.f$2);
                    }
                }, null, null, this, userState));
            }
        }
    }

    /* renamed from: onTextClassifierEvent */
    public void lambda$onTextClassifierEvent$4$TextClassificationManagerService(TextClassificationSessionId sessionId, TextClassifierEvent event) throws RemoteException {
        String packageName;
        int userId;
        Preconditions.checkNotNull(event);
        if (event.getEventContext() == null) {
            packageName = null;
        } else {
            packageName = event.getEventContext().getPackageName();
        }
        if (event.getEventContext() == null) {
            userId = UserHandle.getCallingUserId();
        } else {
            userId = event.getEventContext().getUserId();
        }
        validateInput(this.mContext, packageName, userId);
        synchronized (this.mLock) {
            UserState userState = getUserStateLocked(userId);
            if (userState.isBoundLocked()) {
                userState.mService.onTextClassifierEvent(sessionId, event);
            } else {
                userState.mPendingRequests.add(new PendingRequest(new FunctionalUtils.ThrowingRunnable(sessionId, event) {
                    /* class com.android.server.textclassifier.$$Lambda$TextClassificationManagerService$sMLFGuslbXgLyLQJD4NeR5KkZn0 */
                    private final /* synthetic */ TextClassificationSessionId f$1;
                    private final /* synthetic */ TextClassifierEvent f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void runOrThrow() {
                        TextClassificationManagerService.this.lambda$onTextClassifierEvent$4$TextClassificationManagerService(this.f$1, this.f$2);
                    }
                }, null, null, this, userState));
            }
        }
    }

    /* renamed from: onDetectLanguage */
    public void lambda$onDetectLanguage$5$TextClassificationManagerService(TextClassificationSessionId sessionId, TextLanguage.Request request, ITextClassifierCallback callback) throws RemoteException {
        Preconditions.checkNotNull(request);
        Preconditions.checkNotNull(callback);
        int userId = request.getUserId();
        validateInput(this.mContext, request.getCallingPackageName(), userId);
        synchronized (this.mLock) {
            UserState userState = getUserStateLocked(userId);
            if (!userState.bindLocked()) {
                callback.onFailure();
            } else if (userState.isBoundLocked()) {
                userState.mService.onDetectLanguage(sessionId, request, callback);
            } else {
                Queue<PendingRequest> queue = userState.mPendingRequests;
                $$Lambda$TextClassificationManagerService$yB5oS3bxsmWcPiI9f0QxOl0chLs r4 = new FunctionalUtils.ThrowingRunnable(sessionId, request, callback) {
                    /* class com.android.server.textclassifier.$$Lambda$TextClassificationManagerService$yB5oS3bxsmWcPiI9f0QxOl0chLs */
                    private final /* synthetic */ TextClassificationSessionId f$1;
                    private final /* synthetic */ TextLanguage.Request f$2;
                    private final /* synthetic */ ITextClassifierCallback f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    public final void runOrThrow() {
                        TextClassificationManagerService.this.lambda$onDetectLanguage$5$TextClassificationManagerService(this.f$1, this.f$2, this.f$3);
                    }
                };
                Objects.requireNonNull(callback);
                queue.add(new PendingRequest(r4, new FunctionalUtils.ThrowingRunnable(callback) {
                    /* class com.android.server.textclassifier.$$Lambda$k7KcqZH2A0AukChaKa6Xru13_Q */
                    private final /* synthetic */ ITextClassifierCallback f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void runOrThrow() {
                        this.f$0.onFailure();
                    }
                }, callback.asBinder(), this, userState));
            }
        }
    }

    /* renamed from: onSuggestConversationActions */
    public void lambda$onSuggestConversationActions$6$TextClassificationManagerService(TextClassificationSessionId sessionId, ConversationActions.Request request, ITextClassifierCallback callback) throws RemoteException {
        Preconditions.checkNotNull(request);
        Preconditions.checkNotNull(callback);
        int userId = request.getUserId();
        validateInput(this.mContext, request.getCallingPackageName(), userId);
        synchronized (this.mLock) {
            UserState userState = getUserStateLocked(userId);
            if (!userState.bindLocked()) {
                callback.onFailure();
            } else if (userState.isBoundLocked()) {
                userState.mService.onSuggestConversationActions(sessionId, request, callback);
            } else {
                Queue<PendingRequest> queue = userState.mPendingRequests;
                $$Lambda$TextClassificationManagerService$8JdB0qZEYuRmsTmNRpxWLWnRgs r4 = new FunctionalUtils.ThrowingRunnable(sessionId, request, callback) {
                    /* class com.android.server.textclassifier.$$Lambda$TextClassificationManagerService$8JdB0qZEYuRmsTmNRpxWLWnRgs */
                    private final /* synthetic */ TextClassificationSessionId f$1;
                    private final /* synthetic */ ConversationActions.Request f$2;
                    private final /* synthetic */ ITextClassifierCallback f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    public final void runOrThrow() {
                        TextClassificationManagerService.this.lambda$onSuggestConversationActions$6$TextClassificationManagerService(this.f$1, this.f$2, this.f$3);
                    }
                };
                Objects.requireNonNull(callback);
                queue.add(new PendingRequest(r4, new FunctionalUtils.ThrowingRunnable(callback) {
                    /* class com.android.server.textclassifier.$$Lambda$k7KcqZH2A0AukChaKa6Xru13_Q */
                    private final /* synthetic */ ITextClassifierCallback f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void runOrThrow() {
                        this.f$0.onFailure();
                    }
                }, callback.asBinder(), this, userState));
            }
        }
    }

    /* renamed from: onCreateTextClassificationSession */
    public void lambda$onCreateTextClassificationSession$7$TextClassificationManagerService(TextClassificationContext classificationContext, TextClassificationSessionId sessionId) throws RemoteException {
        Preconditions.checkNotNull(sessionId);
        Preconditions.checkNotNull(classificationContext);
        int userId = classificationContext.getUserId();
        validateInput(this.mContext, classificationContext.getPackageName(), userId);
        synchronized (this.mLock) {
            UserState userState = getUserStateLocked(userId);
            if (userState.isBoundLocked()) {
                userState.mService.onCreateTextClassificationSession(classificationContext, sessionId);
                this.mSessionUserIds.put(sessionId, Integer.valueOf(userId));
            } else {
                userState.mPendingRequests.add(new PendingRequest(new FunctionalUtils.ThrowingRunnable(classificationContext, sessionId) {
                    /* class com.android.server.textclassifier.$$Lambda$TextClassificationManagerService$YjZl5O2nzrq_4fvkOEzBc8WS3aY */
                    private final /* synthetic */ TextClassificationContext f$1;
                    private final /* synthetic */ TextClassificationSessionId f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void runOrThrow() {
                        TextClassificationManagerService.this.lambda$onCreateTextClassificationSession$7$TextClassificationManagerService(this.f$1, this.f$2);
                    }
                }, null, null, this, userState));
            }
        }
    }

    /* renamed from: onDestroyTextClassificationSession */
    public void lambda$onDestroyTextClassificationSession$8$TextClassificationManagerService(TextClassificationSessionId sessionId) throws RemoteException {
        int userId;
        Preconditions.checkNotNull(sessionId);
        synchronized (this.mLock) {
            if (this.mSessionUserIds.containsKey(sessionId)) {
                userId = this.mSessionUserIds.get(sessionId).intValue();
            } else {
                userId = UserHandle.getCallingUserId();
            }
            validateInput(this.mContext, null, userId);
            UserState userState = getUserStateLocked(userId);
            if (userState.isBoundLocked()) {
                userState.mService.onDestroyTextClassificationSession(sessionId);
                this.mSessionUserIds.remove(sessionId);
            } else {
                userState.mPendingRequests.add(new PendingRequest(new FunctionalUtils.ThrowingRunnable(sessionId) {
                    /* class com.android.server.textclassifier.$$Lambda$TextClassificationManagerService$IiiA6SYq7BOEU1FJlf97_wOk4 */
                    private final /* synthetic */ TextClassificationSessionId f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void runOrThrow() {
                        TextClassificationManagerService.this.lambda$onDestroyTextClassificationSession$8$TextClassificationManagerService(this.f$1);
                    }
                }, null, null, this, userState));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private UserState getUserStateLocked(int userId) {
        UserState result = this.mUserStates.get(userId);
        if (result != null) {
            return result;
        }
        UserState result2 = new UserState(userId, this.mContext, this.mLock);
        this.mUserStates.put(userId, result2);
        return result2;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    public UserState peekUserStateLocked(int userId) {
        return this.mUserStates.get(userId);
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, LOG_TAG, fout)) {
            IndentingPrintWriter pw = new IndentingPrintWriter(fout, "  ");
            ((TextClassificationManager) this.mContext.getSystemService(TextClassificationManager.class)).dump(pw);
            pw.printPair("context", this.mContext);
            pw.println();
            synchronized (this.mLock) {
                int size = this.mUserStates.size();
                pw.print("Number user states: ");
                pw.println(size);
                if (size > 0) {
                    for (int i = 0; i < size; i++) {
                        pw.increaseIndent();
                        pw.print(i);
                        pw.print(":");
                        this.mUserStates.valueAt(i).dump(pw);
                        pw.println();
                        pw.decreaseIndent();
                    }
                }
                pw.println("Number of active sessions: " + this.mSessionUserIds.size());
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class PendingRequest implements IBinder.DeathRecipient {
        private final IBinder mBinder;
        private final Runnable mOnServiceFailure;
        @GuardedBy({"mLock"})
        private final UserState mOwningUser;
        private final Runnable mRequest;
        private final TextClassificationManagerService mService;

        PendingRequest(FunctionalUtils.ThrowingRunnable request, FunctionalUtils.ThrowingRunnable onServiceFailure, IBinder binder, TextClassificationManagerService service, UserState owningUser) {
            this.mRequest = TextClassificationManagerService.logOnFailure((FunctionalUtils.ThrowingRunnable) Preconditions.checkNotNull(request), "handling pending request");
            this.mOnServiceFailure = TextClassificationManagerService.logOnFailure(onServiceFailure, "notifying callback of service failure");
            this.mBinder = binder;
            this.mService = service;
            this.mOwningUser = owningUser;
            IBinder iBinder = this.mBinder;
            if (iBinder != null) {
                try {
                    iBinder.linkToDeath(this, 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            synchronized (this.mService.mLock) {
                removeLocked();
            }
        }

        @GuardedBy({"mLock"})
        private void removeLocked() {
            this.mOwningUser.mPendingRequests.remove(this);
            IBinder iBinder = this.mBinder;
            if (iBinder != null) {
                iBinder.unlinkToDeath(this, 0);
            }
        }
    }

    /* access modifiers changed from: private */
    public static Runnable logOnFailure(FunctionalUtils.ThrowingRunnable r, String opDesc) {
        if (r == null) {
            return null;
        }
        return FunctionalUtils.handleExceptions(r, new Consumer(opDesc) {
            /* class com.android.server.textclassifier.$$Lambda$TextClassificationManagerService$R4aPVSf5_OfouCzD96pPpSsbUOs */
            private final /* synthetic */ String f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                Throwable th = (Throwable) obj;
                Slog.d(TextClassificationManagerService.LOG_TAG, "Error " + this.f$0 + ": " + th.getMessage());
            }
        });
    }

    private static void validateInput(Context context, String packageName, int userId) throws RemoteException {
        boolean z;
        boolean z2 = false;
        if (packageName != null) {
            try {
                int packageUid = context.getPackageManager().getPackageUidAsUser(packageName, UserHandle.getCallingUserId());
                int callingUid = Binder.getCallingUid();
                if (callingUid != packageUid) {
                    if (callingUid != 1000) {
                        z = false;
                        Preconditions.checkArgument(z, "Invalid package name. Package=" + packageName + ", CallingUid=" + callingUid);
                    }
                }
                z = true;
                Preconditions.checkArgument(z, "Invalid package name. Package=" + packageName + ", CallingUid=" + callingUid);
            } catch (Exception e) {
                throw new RemoteException("Invalid request: " + e.getMessage(), e, true, true);
            }
        }
        if (userId != -10000) {
            z2 = true;
        }
        Preconditions.checkArgument(z2, "Null userId");
        int callingUserId = UserHandle.getCallingUserId();
        if (callingUserId != userId) {
            context.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "Invalid userId. UserId=" + userId + ", CallingUserId=" + callingUserId);
        }
    }

    /* access modifiers changed from: private */
    public static final class UserState {
        @GuardedBy({"mLock"})
        boolean mBinding;
        final TextClassifierServiceConnection mConnection;
        private final Context mContext;
        private final Object mLock;
        @GuardedBy({"mLock"})
        final Queue<PendingRequest> mPendingRequests;
        @GuardedBy({"mLock"})
        ITextClassifierService mService;
        final int mUserId;

        private UserState(int userId, Context context, Object lock) {
            this.mConnection = new TextClassifierServiceConnection();
            this.mPendingRequests = new ArrayDeque();
            this.mUserId = userId;
            this.mContext = (Context) Preconditions.checkNotNull(context);
            this.mLock = Preconditions.checkNotNull(lock);
        }

        /* access modifiers changed from: package-private */
        @GuardedBy({"mLock"})
        public boolean isBoundLocked() {
            return this.mService != null;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        @GuardedBy({"mLock"})
        private void handlePendingRequestsLocked() {
            while (true) {
                PendingRequest request = this.mPendingRequests.poll();
                if (request != null) {
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
        /* access modifiers changed from: public */
        @GuardedBy({"mLock"})
        private boolean bindIfHasPendingRequestsLocked() {
            return !this.mPendingRequests.isEmpty() && bindLocked();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        @GuardedBy({"mLock"})
        private boolean bindLocked() {
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
                boolean willBind = this.mContext.bindServiceAsUser(serviceIntent, this.mConnection, 69206017, UserHandle.of(this.mUserId));
                this.mBinding = willBind;
                Binder.restoreCallingIdentity(identity);
                return willBind;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void dump(IndentingPrintWriter pw) {
            pw.printPair("context", this.mContext);
            pw.printPair("userId", Integer.valueOf(this.mUserId));
            synchronized (this.mLock) {
                pw.printPair("binding", Boolean.valueOf(this.mBinding));
                pw.printPair("numberRequests", Integer.valueOf(this.mPendingRequests.size()));
            }
        }

        /* access modifiers changed from: private */
        public final class TextClassifierServiceConnection implements ServiceConnection {
            private TextClassifierServiceConnection() {
            }

            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName name, IBinder service) {
                init(ITextClassifierService.Stub.asInterface(service));
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName name) {
                cleanupService();
            }

            @Override // android.content.ServiceConnection
            public void onBindingDied(ComponentName name) {
                cleanupService();
            }

            @Override // android.content.ServiceConnection
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
    }
}
