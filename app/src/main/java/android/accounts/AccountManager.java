package android.accounts;

import android.accounts.IAccountManagerResponse.Stub;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.collect.Maps;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AccountManager {
    public static final String ACTION_AUTHENTICATOR_INTENT = "android.accounts.AccountAuthenticator";
    public static final String AUTHENTICATOR_ATTRIBUTES_NAME = "account-authenticator";
    public static final String AUTHENTICATOR_META_DATA_NAME = "android.accounts.AccountAuthenticator";
    public static final int ERROR_CODE_BAD_ARGUMENTS = 7;
    public static final int ERROR_CODE_BAD_AUTHENTICATION = 9;
    public static final int ERROR_CODE_BAD_REQUEST = 8;
    public static final int ERROR_CODE_CANCELED = 4;
    public static final int ERROR_CODE_INVALID_RESPONSE = 5;
    public static final int ERROR_CODE_MANAGEMENT_DISABLED_FOR_ACCOUNT_TYPE = 101;
    public static final int ERROR_CODE_NETWORK_ERROR = 3;
    public static final int ERROR_CODE_REMOTE_EXCEPTION = 1;
    public static final int ERROR_CODE_UNSUPPORTED_OPERATION = 6;
    public static final int ERROR_CODE_USER_RESTRICTED = 100;
    public static final String KEY_ACCOUNTS = "accounts";
    public static final String KEY_ACCOUNT_AUTHENTICATOR_RESPONSE = "accountAuthenticatorResponse";
    public static final String KEY_ACCOUNT_MANAGER_RESPONSE = "accountManagerResponse";
    public static final String KEY_ACCOUNT_NAME = "authAccount";
    public static final String KEY_ACCOUNT_SESSION_BUNDLE = "accountSessionBundle";
    public static final String KEY_ACCOUNT_STATUS_TOKEN = "accountStatusToken";
    public static final String KEY_ACCOUNT_TYPE = "accountType";
    public static final String KEY_ANDROID_PACKAGE_NAME = "androidPackageName";
    public static final String KEY_AUTHENTICATOR_TYPES = "authenticator_types";
    public static final String KEY_AUTHTOKEN = "authtoken";
    public static final String KEY_AUTH_FAILED_MESSAGE = "authFailedMessage";
    public static final String KEY_AUTH_TOKEN_LABEL = "authTokenLabelKey";
    public static final String KEY_BOOLEAN_RESULT = "booleanResult";
    public static final String KEY_CALLER_PID = "callerPid";
    public static final String KEY_CALLER_UID = "callerUid";
    public static final String KEY_ERROR_CODE = "errorCode";
    public static final String KEY_ERROR_MESSAGE = "errorMessage";
    public static final String KEY_INTENT = "intent";
    public static final String KEY_LAST_AUTHENTICATED_TIME = "lastAuthenticatedTime";
    public static final String KEY_NOTIFY_ON_FAILURE = "notifyOnAuthFailure";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_USERDATA = "userdata";
    public static final String LOGIN_ACCOUNTS_CHANGED_ACTION = "android.accounts.LOGIN_ACCOUNTS_CHANGED";
    private static final String TAG = "AccountManager";
    private final BroadcastReceiver mAccountsChangedBroadcastReceiver;
    private final HashMap<OnAccountsUpdateListener, Handler> mAccountsUpdatedListeners;
    private final Context mContext;
    private final Handler mMainHandler;
    private final IAccountManager mService;

    private abstract class AmsTask extends FutureTask<Bundle> implements AccountManagerFuture<Bundle> {
        final Activity mActivity;
        final AccountManagerCallback<Bundle> mCallback;
        final Handler mHandler;
        final IAccountManagerResponse mResponse;

        /* renamed from: android.accounts.AccountManager.AmsTask.1 */
        static class AnonymousClass1 implements Callable<Bundle> {
            final /* synthetic */ AccountManager val$this$0;

            AnonymousClass1(AccountManager val$this$0) {
                this.val$this$0 = val$this$0;
            }

            public Bundle call() throws Exception {
                throw new IllegalStateException("this should never be called");
            }
        }

        private class Response extends Stub {
            private Response() {
            }

            public void onResult(Bundle bundle) {
                Intent intent = (Intent) bundle.getParcelable(AccountManager.KEY_INTENT);
                if (intent != null && AmsTask.this.mActivity != null) {
                    AmsTask.this.mActivity.startActivity(intent);
                } else if (bundle.getBoolean("retry")) {
                    try {
                        AmsTask.this.doWork();
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                } else {
                    AmsTask.this.set(bundle);
                }
            }

            public void onError(int code, String message) {
                if (code == AccountManager.ERROR_CODE_CANCELED || code == AccountManager.ERROR_CODE_USER_RESTRICTED || code == AccountManager.ERROR_CODE_MANAGEMENT_DISABLED_FOR_ACCOUNT_TYPE) {
                    AmsTask.this.cancel(true);
                } else {
                    AmsTask.this.setException(AccountManager.this.convertErrorToException(code, message));
                }
            }
        }

        public abstract void doWork() throws RemoteException;

        public AmsTask(Activity activity, Handler handler, AccountManagerCallback<Bundle> callback) {
            super(new AnonymousClass1(AccountManager.this));
            this.mHandler = handler;
            this.mCallback = callback;
            this.mActivity = activity;
            this.mResponse = new Response();
        }

        public final AccountManagerFuture<Bundle> start() {
            try {
                doWork();
            } catch (RemoteException e) {
                setException(e);
            }
            return this;
        }

        protected void set(Bundle bundle) {
            if (bundle == null) {
                Log.e(AccountManager.TAG, "the bundle must not be null", new Exception());
            }
            super.set(bundle);
        }

        private Bundle internalGetResult(Long timeout, TimeUnit unit) throws OperationCanceledException, IOException, AuthenticatorException {
            if (!isDone()) {
                AccountManager.this.ensureNotOnMainThread();
            }
            Bundle bundle;
            if (timeout == null) {
                try {
                    bundle = (Bundle) get();
                    cancel(true);
                    return bundle;
                } catch (CancellationException e) {
                    throw new OperationCanceledException();
                } catch (TimeoutException e2) {
                    cancel(true);
                    throw new OperationCanceledException();
                } catch (InterruptedException e3) {
                    cancel(true);
                    throw new OperationCanceledException();
                } catch (ExecutionException e4) {
                    Throwable cause = e4.getCause();
                    if (cause instanceof IOException) {
                        throw ((IOException) cause);
                    } else if (cause instanceof UnsupportedOperationException) {
                        throw new AuthenticatorException(cause);
                    } else if (cause instanceof AuthenticatorException) {
                        throw ((AuthenticatorException) cause);
                    } else if (cause instanceof RuntimeException) {
                        throw ((RuntimeException) cause);
                    } else if (cause instanceof Error) {
                        throw ((Error) cause);
                    } else {
                        throw new IllegalStateException(cause);
                    }
                } catch (Throwable th) {
                    cancel(true);
                }
            } else {
                bundle = (Bundle) get(timeout.longValue(), unit);
                cancel(true);
                return bundle;
            }
        }

        public Bundle getResult() throws OperationCanceledException, IOException, AuthenticatorException {
            return internalGetResult(null, null);
        }

        public Bundle getResult(long timeout, TimeUnit unit) throws OperationCanceledException, IOException, AuthenticatorException {
            return internalGetResult(Long.valueOf(timeout), unit);
        }

        protected void done() {
            if (this.mCallback != null) {
                AccountManager.this.postToHandler(this.mHandler, this.mCallback, (AccountManagerFuture) this);
            }
        }
    }

    /* renamed from: android.accounts.AccountManager.10 */
    class AnonymousClass10 extends AmsTask {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ String val$authTokenType;
        final /* synthetic */ Bundle val$optionsIn;

        AnonymousClass10(AccountManager this$0, AccountManager this$0_1, Activity $anonymous0, Handler $anonymous1, AccountManagerCallback $anonymous2, Account val$account, String val$authTokenType, Bundle val$optionsIn) {
            this.this$0 = this$0_1;
            this.val$account = val$account;
            this.val$authTokenType = val$authTokenType;
            this.val$optionsIn = val$optionsIn;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public void doWork() throws RemoteException {
            this.this$0.mService.getAuthToken(this.mResponse, this.val$account, this.val$authTokenType, false, true, this.val$optionsIn);
        }
    }

    /* renamed from: android.accounts.AccountManager.11 */
    class AnonymousClass11 extends AmsTask {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ String val$authTokenType;
        final /* synthetic */ boolean val$notifyAuthFailure;
        final /* synthetic */ Bundle val$optionsIn;

        AnonymousClass11(AccountManager this$0, AccountManager this$0_1, Activity $anonymous0, Handler $anonymous1, AccountManagerCallback $anonymous2, Account val$account, String val$authTokenType, boolean val$notifyAuthFailure, Bundle val$optionsIn) {
            this.this$0 = this$0_1;
            this.val$account = val$account;
            this.val$authTokenType = val$authTokenType;
            this.val$notifyAuthFailure = val$notifyAuthFailure;
            this.val$optionsIn = val$optionsIn;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public void doWork() throws RemoteException {
            this.this$0.mService.getAuthToken(this.mResponse, this.val$account, this.val$authTokenType, this.val$notifyAuthFailure, false, this.val$optionsIn);
        }
    }

    /* renamed from: android.accounts.AccountManager.12 */
    class AnonymousClass12 extends AmsTask {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ String val$accountType;
        final /* synthetic */ Activity val$activity;
        final /* synthetic */ String val$authTokenType;
        final /* synthetic */ Bundle val$optionsIn;
        final /* synthetic */ String[] val$requiredFeatures;

        AnonymousClass12(AccountManager this$0, AccountManager this$0_1, Activity $anonymous0, Handler $anonymous1, AccountManagerCallback $anonymous2, String val$accountType, String val$authTokenType, String[] val$requiredFeatures, Activity val$activity, Bundle val$optionsIn) {
            this.this$0 = this$0_1;
            this.val$accountType = val$accountType;
            this.val$authTokenType = val$authTokenType;
            this.val$requiredFeatures = val$requiredFeatures;
            this.val$activity = val$activity;
            this.val$optionsIn = val$optionsIn;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public void doWork() throws RemoteException {
            this.this$0.mService.addAccount(this.mResponse, this.val$accountType, this.val$authTokenType, this.val$requiredFeatures, this.val$activity != null, this.val$optionsIn);
        }
    }

    /* renamed from: android.accounts.AccountManager.13 */
    class AnonymousClass13 extends AmsTask {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ String val$accountType;
        final /* synthetic */ Activity val$activity;
        final /* synthetic */ String val$authTokenType;
        final /* synthetic */ Bundle val$optionsIn;
        final /* synthetic */ String[] val$requiredFeatures;
        final /* synthetic */ UserHandle val$userHandle;

        AnonymousClass13(AccountManager this$0, AccountManager this$0_1, Activity $anonymous0, Handler $anonymous1, AccountManagerCallback $anonymous2, String val$accountType, String val$authTokenType, String[] val$requiredFeatures, Activity val$activity, Bundle val$optionsIn, UserHandle val$userHandle) {
            this.this$0 = this$0_1;
            this.val$accountType = val$accountType;
            this.val$authTokenType = val$authTokenType;
            this.val$requiredFeatures = val$requiredFeatures;
            this.val$activity = val$activity;
            this.val$optionsIn = val$optionsIn;
            this.val$userHandle = val$userHandle;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public void doWork() throws RemoteException {
            this.this$0.mService.addAccountAsUser(this.mResponse, this.val$accountType, this.val$authTokenType, this.val$requiredFeatures, this.val$activity != null, this.val$optionsIn, this.val$userHandle.getIdentifier());
        }
    }

    private abstract class BaseFutureTask<T> extends FutureTask<T> {
        final Handler mHandler;
        public final IAccountManagerResponse mResponse;

        /* renamed from: android.accounts.AccountManager.BaseFutureTask.1 */
        static class AnonymousClass1 implements Callable<T> {
            final /* synthetic */ AccountManager val$this$0;

            AnonymousClass1(AccountManager val$this$0) {
                this.val$this$0 = val$this$0;
            }

            public T call() throws Exception {
                throw new IllegalStateException("this should never be called");
            }
        }

        protected class Response extends Stub {
            protected Response() {
            }

            public void onResult(Bundle bundle) {
                try {
                    T result = BaseFutureTask.this.bundleToResult(bundle);
                    if (result != null) {
                        BaseFutureTask.this.set(result);
                    }
                } catch (ClassCastException e) {
                    onError(AccountManager.ERROR_CODE_INVALID_RESPONSE, "no result in response");
                } catch (AuthenticatorException e2) {
                    onError(AccountManager.ERROR_CODE_INVALID_RESPONSE, "no result in response");
                }
            }

            public void onError(int code, String message) {
                if (code == AccountManager.ERROR_CODE_CANCELED || code == AccountManager.ERROR_CODE_USER_RESTRICTED || code == AccountManager.ERROR_CODE_MANAGEMENT_DISABLED_FOR_ACCOUNT_TYPE) {
                    BaseFutureTask.this.cancel(true);
                } else {
                    BaseFutureTask.this.setException(AccountManager.this.convertErrorToException(code, message));
                }
            }
        }

        public abstract T bundleToResult(Bundle bundle) throws AuthenticatorException;

        public abstract void doWork() throws RemoteException;

        public BaseFutureTask(Handler handler) {
            super(new AnonymousClass1(AccountManager.this));
            this.mHandler = handler;
            this.mResponse = new Response();
        }

        protected void postRunnableToHandler(Runnable runnable) {
            (this.mHandler == null ? AccountManager.this.mMainHandler : this.mHandler).post(runnable);
        }

        protected void startTask() {
            try {
                doWork();
            } catch (RemoteException e) {
                setException(e);
            }
        }
    }

    private abstract class Future2Task<T> extends BaseFutureTask<T> implements AccountManagerFuture<T> {
        final AccountManagerCallback<T> mCallback;

        public Future2Task(Handler handler, AccountManagerCallback<T> callback) {
            super(handler);
            this.mCallback = callback;
        }

        protected void done() {
            if (this.mCallback != null) {
                postRunnableToHandler(new Runnable() {
                    public void run() {
                        Future2Task.this.mCallback.run(Future2Task.this);
                    }
                });
            }
        }

        public Future2Task<T> start() {
            startTask();
            return this;
        }

        private T internalGetResult(Long timeout, TimeUnit unit) throws OperationCanceledException, IOException, AuthenticatorException {
            if (!isDone()) {
                AccountManager.this.ensureNotOnMainThread();
            }
            T t;
            if (timeout == null) {
                try {
                    t = get();
                    cancel(true);
                    return t;
                } catch (InterruptedException e) {
                    cancel(true);
                    throw new OperationCanceledException();
                } catch (TimeoutException e2) {
                    cancel(true);
                    throw new OperationCanceledException();
                } catch (CancellationException e3) {
                    cancel(true);
                    throw new OperationCanceledException();
                } catch (ExecutionException e4) {
                    Throwable cause = e4.getCause();
                    if (cause instanceof IOException) {
                        throw ((IOException) cause);
                    } else if (cause instanceof UnsupportedOperationException) {
                        throw new AuthenticatorException(cause);
                    } else if (cause instanceof AuthenticatorException) {
                        throw ((AuthenticatorException) cause);
                    } else if (cause instanceof RuntimeException) {
                        throw ((RuntimeException) cause);
                    } else if (cause instanceof Error) {
                        throw ((Error) cause);
                    } else {
                        throw new IllegalStateException(cause);
                    }
                } catch (Throwable th) {
                    cancel(true);
                }
            } else {
                t = get(timeout.longValue(), unit);
                cancel(true);
                return t;
            }
        }

        public T getResult() throws OperationCanceledException, IOException, AuthenticatorException {
            return internalGetResult(null, null);
        }

        public T getResult(long timeout, TimeUnit unit) throws OperationCanceledException, IOException, AuthenticatorException {
            return internalGetResult(Long.valueOf(timeout), unit);
        }
    }

    /* renamed from: android.accounts.AccountManager.14 */
    class AnonymousClass14 extends Future2Task<Boolean> {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ UserHandle val$fromUser;
        final /* synthetic */ UserHandle val$toUser;

        AnonymousClass14(AccountManager this$0, AccountManager this$0_1, Handler $anonymous0, AccountManagerCallback $anonymous1, Account val$account, UserHandle val$fromUser, UserHandle val$toUser) {
            this.this$0 = this$0_1;
            this.val$account = val$account;
            this.val$fromUser = val$fromUser;
            this.val$toUser = val$toUser;
            super($anonymous0, $anonymous1);
        }

        public void doWork() throws RemoteException {
            this.this$0.mService.copyAccountToUser(this.mResponse, this.val$account, this.val$fromUser.getIdentifier(), this.val$toUser.getIdentifier());
        }

        public Boolean bundleToResult(Bundle bundle) throws AuthenticatorException {
            if (bundle.containsKey(AccountManager.KEY_BOOLEAN_RESULT)) {
                return Boolean.valueOf(bundle.getBoolean(AccountManager.KEY_BOOLEAN_RESULT));
            }
            throw new AuthenticatorException("no result in response");
        }
    }

    /* renamed from: android.accounts.AccountManager.15 */
    class AnonymousClass15 extends AmsTask {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ Activity val$activity;
        final /* synthetic */ Bundle val$options;
        final /* synthetic */ int val$userId;

        AnonymousClass15(AccountManager this$0, AccountManager this$0_1, Activity $anonymous0, Handler $anonymous1, AccountManagerCallback $anonymous2, Account val$account, Bundle val$options, Activity val$activity, int val$userId) {
            this.this$0 = this$0_1;
            this.val$account = val$account;
            this.val$options = val$options;
            this.val$activity = val$activity;
            this.val$userId = val$userId;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public void doWork() throws RemoteException {
            this.this$0.mService.confirmCredentialsAsUser(this.mResponse, this.val$account, this.val$options, this.val$activity != null, this.val$userId);
        }
    }

    /* renamed from: android.accounts.AccountManager.16 */
    class AnonymousClass16 extends AmsTask {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ Activity val$activity;
        final /* synthetic */ String val$authTokenType;
        final /* synthetic */ Bundle val$options;

        AnonymousClass16(AccountManager this$0, AccountManager this$0_1, Activity $anonymous0, Handler $anonymous1, AccountManagerCallback $anonymous2, Account val$account, String val$authTokenType, Activity val$activity, Bundle val$options) {
            this.this$0 = this$0_1;
            this.val$account = val$account;
            this.val$authTokenType = val$authTokenType;
            this.val$activity = val$activity;
            this.val$options = val$options;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public void doWork() throws RemoteException {
            this.this$0.mService.updateCredentials(this.mResponse, this.val$account, this.val$authTokenType, this.val$activity != null, this.val$options);
        }
    }

    /* renamed from: android.accounts.AccountManager.17 */
    class AnonymousClass17 extends AmsTask {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ String val$accountType;
        final /* synthetic */ Activity val$activity;

        AnonymousClass17(AccountManager this$0, AccountManager this$0_1, Activity $anonymous0, Handler $anonymous1, AccountManagerCallback $anonymous2, String val$accountType, Activity val$activity) {
            this.this$0 = this$0_1;
            this.val$accountType = val$accountType;
            this.val$activity = val$activity;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public void doWork() throws RemoteException {
            this.this$0.mService.editProperties(this.mResponse, this.val$accountType, this.val$activity != null);
        }
    }

    /* renamed from: android.accounts.AccountManager.18 */
    class AnonymousClass18 implements Runnable {
        final /* synthetic */ AccountManagerCallback val$callback;
        final /* synthetic */ AccountManagerFuture val$future;

        AnonymousClass18(AccountManagerCallback val$callback, AccountManagerFuture val$future) {
            this.val$callback = val$callback;
            this.val$future = val$future;
        }

        public void run() {
            this.val$callback.run(this.val$future);
        }
    }

    /* renamed from: android.accounts.AccountManager.19 */
    class AnonymousClass19 implements Runnable {
        final /* synthetic */ Account[] val$accountsCopy;
        final /* synthetic */ OnAccountsUpdateListener val$listener;

        AnonymousClass19(OnAccountsUpdateListener val$listener, Account[] val$accountsCopy) {
            this.val$listener = val$listener;
            this.val$accountsCopy = val$accountsCopy;
        }

        public void run() {
            try {
                this.val$listener.onAccountsUpdated(this.val$accountsCopy);
            } catch (SQLException e) {
                Log.e(AccountManager.TAG, "Can't update accounts", e);
            }
        }
    }

    /* renamed from: android.accounts.AccountManager.20 */
    class AnonymousClass20 extends AmsTask {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ String val$accountType;
        final /* synthetic */ Activity val$activity;
        final /* synthetic */ String val$authTokenType;
        final /* synthetic */ Bundle val$optionsIn;
        final /* synthetic */ String[] val$requiredFeatures;

        AnonymousClass20(AccountManager this$0, AccountManager this$0_1, Activity $anonymous0, Handler $anonymous1, AccountManagerCallback $anonymous2, String val$accountType, String val$authTokenType, String[] val$requiredFeatures, Activity val$activity, Bundle val$optionsIn) {
            this.this$0 = this$0_1;
            this.val$accountType = val$accountType;
            this.val$authTokenType = val$authTokenType;
            this.val$requiredFeatures = val$requiredFeatures;
            this.val$activity = val$activity;
            this.val$optionsIn = val$optionsIn;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public void doWork() throws RemoteException {
            this.this$0.mService.startAddAccountSession(this.mResponse, this.val$accountType, this.val$authTokenType, this.val$requiredFeatures, this.val$activity != null, this.val$optionsIn);
        }
    }

    /* renamed from: android.accounts.AccountManager.21 */
    class AnonymousClass21 extends AmsTask {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ Activity val$activity;
        final /* synthetic */ String val$authTokenType;
        final /* synthetic */ Bundle val$optionsIn;

        AnonymousClass21(AccountManager this$0, AccountManager this$0_1, Activity $anonymous0, Handler $anonymous1, AccountManagerCallback $anonymous2, Account val$account, String val$authTokenType, Activity val$activity, Bundle val$optionsIn) {
            this.this$0 = this$0_1;
            this.val$account = val$account;
            this.val$authTokenType = val$authTokenType;
            this.val$activity = val$activity;
            this.val$optionsIn = val$optionsIn;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public void doWork() throws RemoteException {
            this.this$0.mService.startUpdateCredentialsSession(this.mResponse, this.val$account, this.val$authTokenType, this.val$activity != null, this.val$optionsIn);
        }
    }

    /* renamed from: android.accounts.AccountManager.22 */
    class AnonymousClass22 extends AmsTask {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ Activity val$activity;
        final /* synthetic */ Bundle val$appInfo;
        final /* synthetic */ Bundle val$sessionBundle;
        final /* synthetic */ UserHandle val$userHandle;

        AnonymousClass22(AccountManager this$0, AccountManager this$0_1, Activity $anonymous0, Handler $anonymous1, AccountManagerCallback $anonymous2, Bundle val$sessionBundle, Activity val$activity, Bundle val$appInfo, UserHandle val$userHandle) {
            this.this$0 = this$0_1;
            this.val$sessionBundle = val$sessionBundle;
            this.val$activity = val$activity;
            this.val$appInfo = val$appInfo;
            this.val$userHandle = val$userHandle;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public void doWork() throws RemoteException {
            this.this$0.mService.finishSessionAsUser(this.mResponse, this.val$sessionBundle, this.val$activity != null, this.val$appInfo, this.val$userHandle.getIdentifier());
        }
    }

    /* renamed from: android.accounts.AccountManager.23 */
    class AnonymousClass23 extends Future2Task<Boolean> {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ String val$statusToken;

        AnonymousClass23(AccountManager this$0, AccountManager this$0_1, Handler $anonymous0, AccountManagerCallback $anonymous1, Account val$account, String val$statusToken) {
            this.this$0 = this$0_1;
            this.val$account = val$account;
            this.val$statusToken = val$statusToken;
            super($anonymous0, $anonymous1);
        }

        public void doWork() throws RemoteException {
            this.this$0.mService.isCredentialsUpdateSuggested(this.mResponse, this.val$account, this.val$statusToken);
        }

        public Boolean bundleToResult(Bundle bundle) throws AuthenticatorException {
            if (bundle.containsKey(AccountManager.KEY_BOOLEAN_RESULT)) {
                return Boolean.valueOf(bundle.getBoolean(AccountManager.KEY_BOOLEAN_RESULT));
            }
            throw new AuthenticatorException("no result in response");
        }
    }

    /* renamed from: android.accounts.AccountManager.2 */
    class AnonymousClass2 extends Future2Task<String> {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ String val$accountType;
        final /* synthetic */ String val$authTokenType;

        AnonymousClass2(AccountManager this$0, AccountManager this$0_1, Handler $anonymous0, AccountManagerCallback $anonymous1, String val$accountType, String val$authTokenType) {
            this.this$0 = this$0_1;
            this.val$accountType = val$accountType;
            this.val$authTokenType = val$authTokenType;
            super($anonymous0, $anonymous1);
        }

        public void doWork() throws RemoteException {
            this.this$0.mService.getAuthTokenLabel(this.mResponse, this.val$accountType, this.val$authTokenType);
        }

        public String bundleToResult(Bundle bundle) throws AuthenticatorException {
            if (bundle.containsKey(AccountManager.KEY_AUTH_TOKEN_LABEL)) {
                return bundle.getString(AccountManager.KEY_AUTH_TOKEN_LABEL);
            }
            throw new AuthenticatorException("no result in response");
        }
    }

    /* renamed from: android.accounts.AccountManager.3 */
    class AnonymousClass3 extends Future2Task<Boolean> {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ String[] val$features;

        AnonymousClass3(AccountManager this$0, AccountManager this$0_1, Handler $anonymous0, AccountManagerCallback $anonymous1, Account val$account, String[] val$features) {
            this.this$0 = this$0_1;
            this.val$account = val$account;
            this.val$features = val$features;
            super($anonymous0, $anonymous1);
        }

        public void doWork() throws RemoteException {
            this.this$0.mService.hasFeatures(this.mResponse, this.val$account, this.val$features, this.this$0.mContext.getOpPackageName());
        }

        public Boolean bundleToResult(Bundle bundle) throws AuthenticatorException {
            if (bundle.containsKey(AccountManager.KEY_BOOLEAN_RESULT)) {
                return Boolean.valueOf(bundle.getBoolean(AccountManager.KEY_BOOLEAN_RESULT));
            }
            throw new AuthenticatorException("no result in response");
        }
    }

    /* renamed from: android.accounts.AccountManager.4 */
    class AnonymousClass4 extends Future2Task<Account[]> {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ String[] val$features;
        final /* synthetic */ String val$type;

        AnonymousClass4(AccountManager this$0, AccountManager this$0_1, Handler $anonymous0, AccountManagerCallback $anonymous1, String val$type, String[] val$features) {
            this.this$0 = this$0_1;
            this.val$type = val$type;
            this.val$features = val$features;
            super($anonymous0, $anonymous1);
        }

        public void doWork() throws RemoteException {
            this.this$0.mService.getAccountsByFeatures(this.mResponse, this.val$type, this.val$features, this.this$0.mContext.getOpPackageName());
        }

        public Account[] bundleToResult(Bundle bundle) throws AuthenticatorException {
            if (bundle.containsKey(AccountManager.KEY_ACCOUNTS)) {
                Parcelable[] parcelables = bundle.getParcelableArray(AccountManager.KEY_ACCOUNTS);
                Account[] descs = new Account[parcelables.length];
                for (int i = 0; i < parcelables.length; i += AccountManager.ERROR_CODE_REMOTE_EXCEPTION) {
                    descs[i] = (Account) parcelables[i];
                }
                return descs;
            }
            throw new AuthenticatorException("no result in response");
        }
    }

    /* renamed from: android.accounts.AccountManager.5 */
    class AnonymousClass5 extends Future2Task<Account> {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ String val$newName;

        AnonymousClass5(AccountManager this$0, AccountManager this$0_1, Handler $anonymous0, AccountManagerCallback $anonymous1, Account val$account, String val$newName) {
            this.this$0 = this$0_1;
            this.val$account = val$account;
            this.val$newName = val$newName;
            super($anonymous0, $anonymous1);
        }

        public void doWork() throws RemoteException {
            this.this$0.mService.renameAccount(this.mResponse, this.val$account, this.val$newName);
        }

        public Account bundleToResult(Bundle bundle) throws AuthenticatorException {
            return new Account(bundle.getString(AccountManager.KEY_ACCOUNT_NAME), bundle.getString(AccountManager.KEY_ACCOUNT_TYPE));
        }
    }

    /* renamed from: android.accounts.AccountManager.6 */
    class AnonymousClass6 extends Future2Task<Boolean> {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ Account val$account;

        AnonymousClass6(AccountManager this$0, AccountManager this$0_1, Handler $anonymous0, AccountManagerCallback $anonymous1, Account val$account) {
            this.this$0 = this$0_1;
            this.val$account = val$account;
            super($anonymous0, $anonymous1);
        }

        public void doWork() throws RemoteException {
            this.this$0.mService.removeAccount(this.mResponse, this.val$account, false);
        }

        public Boolean bundleToResult(Bundle bundle) throws AuthenticatorException {
            if (bundle.containsKey(AccountManager.KEY_BOOLEAN_RESULT)) {
                return Boolean.valueOf(bundle.getBoolean(AccountManager.KEY_BOOLEAN_RESULT));
            }
            throw new AuthenticatorException("no result in response");
        }
    }

    /* renamed from: android.accounts.AccountManager.7 */
    class AnonymousClass7 extends AmsTask {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ Activity val$activity;

        AnonymousClass7(AccountManager this$0, AccountManager this$0_1, Activity $anonymous0, Handler $anonymous1, AccountManagerCallback $anonymous2, Account val$account, Activity val$activity) {
            this.this$0 = this$0_1;
            this.val$account = val$account;
            this.val$activity = val$activity;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public void doWork() throws RemoteException {
            this.this$0.mService.removeAccount(this.mResponse, this.val$account, this.val$activity != null);
        }
    }

    /* renamed from: android.accounts.AccountManager.8 */
    class AnonymousClass8 extends Future2Task<Boolean> {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ UserHandle val$userHandle;

        AnonymousClass8(AccountManager this$0, AccountManager this$0_1, Handler $anonymous0, AccountManagerCallback $anonymous1, Account val$account, UserHandle val$userHandle) {
            this.this$0 = this$0_1;
            this.val$account = val$account;
            this.val$userHandle = val$userHandle;
            super($anonymous0, $anonymous1);
        }

        public void doWork() throws RemoteException {
            this.this$0.mService.removeAccountAsUser(this.mResponse, this.val$account, false, this.val$userHandle.getIdentifier());
        }

        public Boolean bundleToResult(Bundle bundle) throws AuthenticatorException {
            if (bundle.containsKey(AccountManager.KEY_BOOLEAN_RESULT)) {
                return Boolean.valueOf(bundle.getBoolean(AccountManager.KEY_BOOLEAN_RESULT));
            }
            throw new AuthenticatorException("no result in response");
        }
    }

    /* renamed from: android.accounts.AccountManager.9 */
    class AnonymousClass9 extends AmsTask {
        final /* synthetic */ AccountManager this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ Activity val$activity;
        final /* synthetic */ UserHandle val$userHandle;

        AnonymousClass9(AccountManager this$0, AccountManager this$0_1, Activity $anonymous0, Handler $anonymous1, AccountManagerCallback $anonymous2, Account val$account, Activity val$activity, UserHandle val$userHandle) {
            this.this$0 = this$0_1;
            this.val$account = val$account;
            this.val$activity = val$activity;
            this.val$userHandle = val$userHandle;
            super($anonymous0, $anonymous1, $anonymous2);
        }

        public void doWork() throws RemoteException {
            this.this$0.mService.removeAccountAsUser(this.mResponse, this.val$account, this.val$activity != null, this.val$userHandle.getIdentifier());
        }
    }

    private class GetAuthTokenByTypeAndFeaturesTask extends AmsTask implements AccountManagerCallback<Bundle> {
        final String mAccountType;
        final Bundle mAddAccountOptions;
        final String mAuthTokenType;
        final String[] mFeatures;
        volatile AccountManagerFuture<Bundle> mFuture;
        final Bundle mLoginOptions;
        final AccountManagerCallback<Bundle> mMyCallback;
        private volatile int mNumAccounts;

        GetAuthTokenByTypeAndFeaturesTask(String accountType, String authTokenType, String[] features, Activity activityForPrompting, Bundle addAccountOptions, Bundle loginOptions, AccountManagerCallback<Bundle> callback, Handler handler) {
            super(activityForPrompting, handler, callback);
            this.mFuture = null;
            this.mNumAccounts = 0;
            if (accountType == null) {
                throw new IllegalArgumentException("account type is null");
            }
            this.mAccountType = accountType;
            this.mAuthTokenType = authTokenType;
            this.mFeatures = features;
            this.mAddAccountOptions = addAccountOptions;
            this.mLoginOptions = loginOptions;
            this.mMyCallback = this;
        }

        public void doWork() throws RemoteException {
            AccountManager.this.getAccountsByTypeAndFeatures(this.mAccountType, this.mFeatures, new AccountManagerCallback<Account[]>() {
                public void run(AccountManagerFuture<Account[]> future) {
                    try {
                        Parcelable[] accounts = (Account[]) future.getResult();
                        GetAuthTokenByTypeAndFeaturesTask.this.mNumAccounts = accounts.length;
                        Bundle result;
                        if (accounts.length == 0) {
                            if (GetAuthTokenByTypeAndFeaturesTask.this.mActivity != null) {
                                GetAuthTokenByTypeAndFeaturesTask.this.mFuture = AccountManager.this.addAccount(GetAuthTokenByTypeAndFeaturesTask.this.mAccountType, GetAuthTokenByTypeAndFeaturesTask.this.mAuthTokenType, GetAuthTokenByTypeAndFeaturesTask.this.mFeatures, GetAuthTokenByTypeAndFeaturesTask.this.mAddAccountOptions, GetAuthTokenByTypeAndFeaturesTask.this.mActivity, GetAuthTokenByTypeAndFeaturesTask.this.mMyCallback, GetAuthTokenByTypeAndFeaturesTask.this.mHandler);
                            } else {
                                result = new Bundle();
                                result.putString(AccountManager.KEY_ACCOUNT_NAME, null);
                                result.putString(AccountManager.KEY_ACCOUNT_TYPE, null);
                                result.putString(AccountManager.KEY_AUTHTOKEN, null);
                                try {
                                    GetAuthTokenByTypeAndFeaturesTask.this.mResponse.onResult(result);
                                } catch (RemoteException e) {
                                }
                            }
                        } else if (accounts.length == AccountManager.ERROR_CODE_REMOTE_EXCEPTION) {
                            if (GetAuthTokenByTypeAndFeaturesTask.this.mActivity == null) {
                                GetAuthTokenByTypeAndFeaturesTask.this.mFuture = AccountManager.this.getAuthToken(accounts[0], GetAuthTokenByTypeAndFeaturesTask.this.mAuthTokenType, false, GetAuthTokenByTypeAndFeaturesTask.this.mMyCallback, GetAuthTokenByTypeAndFeaturesTask.this.mHandler);
                            } else {
                                GetAuthTokenByTypeAndFeaturesTask.this.mFuture = AccountManager.this.getAuthToken(accounts[0], GetAuthTokenByTypeAndFeaturesTask.this.mAuthTokenType, GetAuthTokenByTypeAndFeaturesTask.this.mLoginOptions, GetAuthTokenByTypeAndFeaturesTask.this.mActivity, GetAuthTokenByTypeAndFeaturesTask.this.mMyCallback, GetAuthTokenByTypeAndFeaturesTask.this.mHandler);
                            }
                        } else if (GetAuthTokenByTypeAndFeaturesTask.this.mActivity != null) {
                            IAccountManagerResponse chooseResponse = new Stub() {
                                public void onResult(Bundle value) throws RemoteException {
                                    Account account = new Account(value.getString(AccountManager.KEY_ACCOUNT_NAME), value.getString(AccountManager.KEY_ACCOUNT_TYPE));
                                    GetAuthTokenByTypeAndFeaturesTask.this.mFuture = AccountManager.this.getAuthToken(account, GetAuthTokenByTypeAndFeaturesTask.this.mAuthTokenType, GetAuthTokenByTypeAndFeaturesTask.this.mLoginOptions, GetAuthTokenByTypeAndFeaturesTask.this.mActivity, GetAuthTokenByTypeAndFeaturesTask.this.mMyCallback, GetAuthTokenByTypeAndFeaturesTask.this.mHandler);
                                }

                                public void onError(int errorCode, String errorMessage) throws RemoteException {
                                    GetAuthTokenByTypeAndFeaturesTask.this.mResponse.onError(errorCode, errorMessage);
                                }
                            };
                            Intent intent = new Intent();
                            ComponentName componentName = ComponentName.unflattenFromString(Resources.getSystem().getString(17039453));
                            intent.setClassName(componentName.getPackageName(), componentName.getClassName());
                            intent.putExtra(AccountManager.KEY_ACCOUNTS, accounts);
                            intent.putExtra(AccountManager.KEY_ACCOUNT_MANAGER_RESPONSE, new AccountManagerResponse(chooseResponse));
                            GetAuthTokenByTypeAndFeaturesTask.this.mActivity.startActivity(intent);
                        } else {
                            result = new Bundle();
                            result.putString(AccountManager.KEY_ACCOUNTS, null);
                            try {
                                GetAuthTokenByTypeAndFeaturesTask.this.mResponse.onResult(result);
                            } catch (RemoteException e2) {
                            }
                        }
                    } catch (OperationCanceledException e3) {
                        GetAuthTokenByTypeAndFeaturesTask.this.setException(e3);
                    } catch (IOException e4) {
                        GetAuthTokenByTypeAndFeaturesTask.this.setException(e4);
                    } catch (AuthenticatorException e5) {
                        GetAuthTokenByTypeAndFeaturesTask.this.setException(e5);
                    }
                }
            }, this.mHandler);
        }

        public void run(AccountManagerFuture<Bundle> future) {
            try {
                Bundle result = (Bundle) future.getResult();
                if (this.mNumAccounts == 0) {
                    String accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME);
                    String accountType = result.getString(AccountManager.KEY_ACCOUNT_TYPE);
                    if (TextUtils.isEmpty(accountName) || TextUtils.isEmpty(accountType)) {
                        setException(new AuthenticatorException("account not in result"));
                        return;
                    }
                    Account account = new Account(accountName, accountType);
                    this.mNumAccounts = AccountManager.ERROR_CODE_REMOTE_EXCEPTION;
                    AccountManager.this.getAuthToken(account, this.mAuthTokenType, null, this.mActivity, this.mMyCallback, this.mHandler);
                    return;
                }
                set(result);
            } catch (OperationCanceledException e) {
                cancel(true);
            } catch (IOException e2) {
                setException(e2);
            } catch (AuthenticatorException e3) {
                setException(e3);
            }
        }
    }

    public AccountManager(Context context, IAccountManager service) {
        this.mAccountsUpdatedListeners = Maps.newHashMap();
        this.mAccountsChangedBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Account[] accounts = AccountManager.this.getAccounts();
                synchronized (AccountManager.this.mAccountsUpdatedListeners) {
                    for (Entry<OnAccountsUpdateListener, Handler> entry : AccountManager.this.mAccountsUpdatedListeners.entrySet()) {
                        AccountManager.this.postToHandler((Handler) entry.getValue(), (OnAccountsUpdateListener) entry.getKey(), accounts);
                    }
                }
            }
        };
        this.mContext = context;
        this.mService = service;
        this.mMainHandler = new Handler(this.mContext.getMainLooper());
    }

    public AccountManager(Context context, IAccountManager service, Handler handler) {
        this.mAccountsUpdatedListeners = Maps.newHashMap();
        this.mAccountsChangedBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Account[] accounts = AccountManager.this.getAccounts();
                synchronized (AccountManager.this.mAccountsUpdatedListeners) {
                    for (Entry<OnAccountsUpdateListener, Handler> entry : AccountManager.this.mAccountsUpdatedListeners.entrySet()) {
                        AccountManager.this.postToHandler((Handler) entry.getValue(), (OnAccountsUpdateListener) entry.getKey(), accounts);
                    }
                }
            }
        };
        this.mContext = context;
        this.mService = service;
        this.mMainHandler = handler;
    }

    public static Bundle sanitizeResult(Bundle result) {
        if (result == null || !result.containsKey(KEY_AUTHTOKEN) || TextUtils.isEmpty(result.getString(KEY_AUTHTOKEN))) {
            return result;
        }
        Bundle newResult = new Bundle(result);
        newResult.putString(KEY_AUTHTOKEN, "<omitted for logging purposes>");
        return newResult;
    }

    public static AccountManager get(Context context) {
        if (context != null) {
            return (AccountManager) context.getSystemService(ContentResolver.SYNC_EXTRAS_ACCOUNT);
        }
        throw new IllegalArgumentException("context is null");
    }

    public String getPassword(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        try {
            return this.mService.getPassword(account);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getUserData(Account account, String key) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (key == null) {
            throw new IllegalArgumentException("key is null");
        } else {
            try {
                return this.mService.getUserData(account, key);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public AuthenticatorDescription[] getAuthenticatorTypes() {
        try {
            return this.mService.getAuthenticatorTypes(UserHandle.getCallingUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public AuthenticatorDescription[] getAuthenticatorTypesAsUser(int userId) {
        try {
            return this.mService.getAuthenticatorTypes(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Account[] getAccounts() {
        try {
            return this.mService.getAccounts(null, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Account[] getAccountsAsUser(int userId) {
        try {
            return this.mService.getAccountsAsUser(null, userId, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Account[] getAccountsForPackage(String packageName, int uid) {
        try {
            return this.mService.getAccountsForPackage(packageName, uid, this.mContext.getOpPackageName());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public Account[] getAccountsByTypeForPackage(String type, String packageName) {
        try {
            return this.mService.getAccountsByTypeForPackage(type, packageName, this.mContext.getOpPackageName());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public Account[] getAccountsByType(String type) {
        return getAccountsByTypeAsUser(type, Process.myUserHandle());
    }

    public Account[] getAccountsByTypeAsUser(String type, UserHandle userHandle) {
        try {
            return this.mService.getAccountsAsUser(type, userHandle.getIdentifier(), this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void updateAppPermission(Account account, String authTokenType, int uid, boolean value) {
        try {
            this.mService.updateAppPermission(account, authTokenType, uid, value);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public AccountManagerFuture<String> getAuthTokenLabel(String accountType, String authTokenType, AccountManagerCallback<String> callback, Handler handler) {
        if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        } else if (authTokenType != null) {
            return new AnonymousClass2(this, this, handler, callback, accountType, authTokenType).start();
        } else {
            throw new IllegalArgumentException("authTokenType is null");
        }
    }

    public AccountManagerFuture<Boolean> hasFeatures(Account account, String[] features, AccountManagerCallback<Boolean> callback, Handler handler) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (features != null) {
            return new AnonymousClass3(this, this, handler, callback, account, features).start();
        } else {
            throw new IllegalArgumentException("features is null");
        }
    }

    public AccountManagerFuture<Account[]> getAccountsByTypeAndFeatures(String type, String[] features, AccountManagerCallback<Account[]> callback, Handler handler) {
        if (type != null) {
            return new AnonymousClass4(this, this, handler, callback, type, features).start();
        }
        throw new IllegalArgumentException("type is null");
    }

    public boolean addAccountExplicitly(Account account, String password, Bundle userdata) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        try {
            return this.mService.addAccountExplicitly(account, password, userdata);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean notifyAccountAuthenticated(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        try {
            return this.mService.accountAuthenticated(account);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public AccountManagerFuture<Account> renameAccount(Account account, String newName, AccountManagerCallback<Account> callback, Handler handler) {
        if (account == null) {
            throw new IllegalArgumentException("account is null.");
        } else if (!TextUtils.isEmpty(newName)) {
            return new AnonymousClass5(this, this, handler, callback, account, newName).start();
        } else {
            throw new IllegalArgumentException("newName is empty or null.");
        }
    }

    public String getPreviousName(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        try {
            return this.mService.getPreviousName(account);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public AccountManagerFuture<Boolean> removeAccount(Account account, AccountManagerCallback<Boolean> callback, Handler handler) {
        if (account != null) {
            return new AnonymousClass6(this, this, handler, callback, account).start();
        }
        throw new IllegalArgumentException("account is null");
    }

    public AccountManagerFuture<Bundle> removeAccount(Account account, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (account != null) {
            return new AnonymousClass7(this, this, activity, handler, callback, account, activity).start();
        }
        throw new IllegalArgumentException("account is null");
    }

    @Deprecated
    public AccountManagerFuture<Boolean> removeAccountAsUser(Account account, AccountManagerCallback<Boolean> callback, Handler handler, UserHandle userHandle) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (userHandle != null) {
            return new AnonymousClass8(this, this, handler, callback, account, userHandle).start();
        } else {
            throw new IllegalArgumentException("userHandle is null");
        }
    }

    public AccountManagerFuture<Bundle> removeAccountAsUser(Account account, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler, UserHandle userHandle) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (userHandle != null) {
            return new AnonymousClass9(this, this, activity, handler, callback, account, activity, userHandle).start();
        } else {
            throw new IllegalArgumentException("userHandle is null");
        }
    }

    public boolean removeAccountExplicitly(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        try {
            return this.mService.removeAccountExplicitly(account);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void invalidateAuthToken(String accountType, String authToken) {
        if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        } else if (authToken != null) {
            try {
                this.mService.invalidateAuthToken(accountType, authToken);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public String peekAuthToken(Account account, String authTokenType) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (authTokenType == null) {
            throw new IllegalArgumentException("authTokenType is null");
        } else {
            try {
                return this.mService.peekAuthToken(account, authTokenType);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setPassword(Account account, String password) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        try {
            this.mService.setPassword(account, password);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void clearPassword(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        try {
            this.mService.clearPassword(account);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setUserData(Account account, String key, String value) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (key == null) {
            throw new IllegalArgumentException("key is null");
        } else {
            try {
                this.mService.setUserData(account, key, value);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void setAuthToken(Account account, String authTokenType, String authToken) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (authTokenType == null) {
            throw new IllegalArgumentException("authTokenType is null");
        } else {
            try {
                this.mService.setAuthToken(account, authTokenType, authToken);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public String blockingGetAuthToken(Account account, String authTokenType, boolean notifyAuthFailure) throws OperationCanceledException, IOException, AuthenticatorException {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (authTokenType == null) {
            throw new IllegalArgumentException("authTokenType is null");
        } else {
            Bundle bundle = (Bundle) getAuthToken(account, authTokenType, notifyAuthFailure, null, null).getResult();
            if (bundle != null) {
                return bundle.getString(KEY_AUTHTOKEN);
            }
            Log.e(TAG, "blockingGetAuthToken: null was returned from getResult() for " + account + ", authTokenType " + authTokenType);
            return null;
        }
    }

    public AccountManagerFuture<Bundle> getAuthToken(Account account, String authTokenType, Bundle options, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (authTokenType == null) {
            throw new IllegalArgumentException("authTokenType is null");
        } else {
            Bundle optionsIn = new Bundle();
            if (options != null) {
                optionsIn.putAll(options);
            }
            optionsIn.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
            return new AnonymousClass10(this, this, activity, handler, callback, account, authTokenType, optionsIn).start();
        }
    }

    @Deprecated
    public AccountManagerFuture<Bundle> getAuthToken(Account account, String authTokenType, boolean notifyAuthFailure, AccountManagerCallback<Bundle> callback, Handler handler) {
        return getAuthToken(account, authTokenType, null, notifyAuthFailure, (AccountManagerCallback) callback, handler);
    }

    public AccountManagerFuture<Bundle> getAuthToken(Account account, String authTokenType, Bundle options, boolean notifyAuthFailure, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (authTokenType == null) {
            throw new IllegalArgumentException("authTokenType is null");
        } else {
            Bundle optionsIn = new Bundle();
            if (options != null) {
                optionsIn.putAll(options);
            }
            optionsIn.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
            return new AnonymousClass11(this, this, null, handler, callback, account, authTokenType, notifyAuthFailure, optionsIn).start();
        }
    }

    public AccountManagerFuture<Bundle> addAccount(String accountType, String authTokenType, String[] requiredFeatures, Bundle addAccountOptions, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        }
        Bundle optionsIn = new Bundle();
        if (addAccountOptions != null) {
            optionsIn.putAll(addAccountOptions);
        }
        optionsIn.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
        return new AnonymousClass12(this, this, activity, handler, callback, accountType, authTokenType, requiredFeatures, activity, optionsIn).start();
    }

    public AccountManagerFuture<Bundle> addAccountAsUser(String accountType, String authTokenType, String[] requiredFeatures, Bundle addAccountOptions, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler, UserHandle userHandle) {
        if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        } else if (userHandle == null) {
            throw new IllegalArgumentException("userHandle is null");
        } else {
            Bundle optionsIn = new Bundle();
            if (addAccountOptions != null) {
                optionsIn.putAll(addAccountOptions);
            }
            optionsIn.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
            return new AnonymousClass13(this, this, activity, handler, callback, accountType, authTokenType, requiredFeatures, activity, optionsIn, userHandle).start();
        }
    }

    public void addSharedAccountsFromParentUser(UserHandle parentUser, UserHandle user) {
        try {
            this.mService.addSharedAccountsFromParentUser(parentUser.getIdentifier(), user.getIdentifier());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public AccountManagerFuture<Boolean> copyAccountToUser(Account account, UserHandle fromUser, UserHandle toUser, AccountManagerCallback<Boolean> callback, Handler handler) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (toUser != null && fromUser != null) {
            return new AnonymousClass14(this, this, handler, callback, account, fromUser, toUser).start();
        } else {
            throw new IllegalArgumentException("fromUser and toUser cannot be null");
        }
    }

    public boolean removeSharedAccount(Account account, UserHandle user) {
        try {
            return this.mService.removeSharedAccountAsUser(account, user.getIdentifier());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public Account[] getSharedAccounts(UserHandle user) {
        try {
            return this.mService.getSharedAccountsAsUser(user.getIdentifier());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public AccountManagerFuture<Bundle> confirmCredentials(Account account, Bundle options, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        return confirmCredentialsAsUser(account, options, activity, callback, handler, Process.myUserHandle());
    }

    public AccountManagerFuture<Bundle> confirmCredentialsAsUser(Account account, Bundle options, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler, UserHandle userHandle) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        return new AnonymousClass15(this, this, activity, handler, callback, account, options, activity, userHandle.getIdentifier()).start();
    }

    public AccountManagerFuture<Bundle> updateCredentials(Account account, String authTokenType, Bundle options, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (account != null) {
            return new AnonymousClass16(this, this, activity, handler, callback, account, authTokenType, activity, options).start();
        }
        throw new IllegalArgumentException("account is null");
    }

    public AccountManagerFuture<Bundle> editProperties(String accountType, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (accountType != null) {
            return new AnonymousClass17(this, this, activity, handler, callback, accountType, activity).start();
        }
        throw new IllegalArgumentException("accountType is null");
    }

    public boolean someUserHasAccount(Account account) {
        try {
            return this.mService.someUserHasAccount(account);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    private void ensureNotOnMainThread() {
        Looper looper = Looper.myLooper();
        if (looper != null && looper == this.mContext.getMainLooper()) {
            IllegalStateException exception = new IllegalStateException("calling this from your main thread can lead to deadlock");
            Log.e(TAG, "calling this from your main thread can lead to deadlock and/or ANRs", exception);
            if (this.mContext.getApplicationInfo().targetSdkVersion >= ERROR_CODE_BAD_REQUEST) {
                throw exception;
            }
        }
    }

    private void postToHandler(Handler handler, AccountManagerCallback<Bundle> callback, AccountManagerFuture<Bundle> future) {
        if (handler == null) {
            handler = this.mMainHandler;
        }
        handler.post(new AnonymousClass18(callback, future));
    }

    private void postToHandler(Handler handler, OnAccountsUpdateListener listener, Account[] accounts) {
        Account[] accountsCopy = new Account[accounts.length];
        System.arraycopy(accounts, 0, accountsCopy, 0, accountsCopy.length);
        if (handler == null) {
            handler = this.mMainHandler;
        }
        handler.post(new AnonymousClass19(listener, accountsCopy));
    }

    private Exception convertErrorToException(int code, String message) {
        if (code == ERROR_CODE_NETWORK_ERROR) {
            return new IOException(message);
        }
        if (code == ERROR_CODE_UNSUPPORTED_OPERATION) {
            return new UnsupportedOperationException(message);
        }
        if (code == ERROR_CODE_INVALID_RESPONSE) {
            return new AuthenticatorException(message);
        }
        if (code == ERROR_CODE_BAD_ARGUMENTS) {
            return new IllegalArgumentException(message);
        }
        return new AuthenticatorException(message);
    }

    public AccountManagerFuture<Bundle> getAuthTokenByFeatures(String accountType, String authTokenType, String[] features, Activity activity, Bundle addAccountOptions, Bundle getAuthTokenOptions, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (accountType == null) {
            throw new IllegalArgumentException("account type is null");
        } else if (authTokenType == null) {
            throw new IllegalArgumentException("authTokenType is null");
        } else {
            GetAuthTokenByTypeAndFeaturesTask task = new GetAuthTokenByTypeAndFeaturesTask(accountType, authTokenType, features, activity, addAccountOptions, getAuthTokenOptions, callback, handler);
            task.start();
            return task;
        }
    }

    @Deprecated
    public static Intent newChooseAccountIntent(Account selectedAccount, ArrayList<Account> allowableAccounts, String[] allowableAccountTypes, boolean alwaysPromptForAccount, String descriptionOverrideText, String addAccountAuthTokenType, String[] addAccountRequiredFeatures, Bundle addAccountOptions) {
        return newChooseAccountIntent(selectedAccount, allowableAccounts, allowableAccountTypes, descriptionOverrideText, addAccountAuthTokenType, addAccountRequiredFeatures, addAccountOptions);
    }

    public static Intent newChooseAccountIntent(Account selectedAccount, List<Account> allowableAccounts, String[] allowableAccountTypes, String descriptionOverrideText, String addAccountAuthTokenType, String[] addAccountRequiredFeatures, Bundle addAccountOptions) {
        Serializable serializable = null;
        Intent intent = new Intent();
        ComponentName componentName = ComponentName.unflattenFromString(Resources.getSystem().getString(17039454));
        intent.setClassName(componentName.getPackageName(), componentName.getClassName());
        String str = ChooseTypeAndAccountActivity.EXTRA_ALLOWABLE_ACCOUNTS_ARRAYLIST;
        if (allowableAccounts != null) {
            serializable = new ArrayList(allowableAccounts);
        }
        intent.putExtra(str, serializable);
        intent.putExtra(ChooseTypeAndAccountActivity.EXTRA_ALLOWABLE_ACCOUNT_TYPES_STRING_ARRAY, allowableAccountTypes);
        intent.putExtra(ChooseTypeAndAccountActivity.EXTRA_ADD_ACCOUNT_OPTIONS_BUNDLE, addAccountOptions);
        intent.putExtra(ChooseTypeAndAccountActivity.EXTRA_SELECTED_ACCOUNT, (Parcelable) selectedAccount);
        intent.putExtra(ChooseTypeAndAccountActivity.EXTRA_DESCRIPTION_TEXT_OVERRIDE, descriptionOverrideText);
        intent.putExtra(GrantCredentialsPermissionActivity.EXTRAS_AUTH_TOKEN_TYPE, addAccountAuthTokenType);
        intent.putExtra(ChooseTypeAndAccountActivity.EXTRA_ADD_ACCOUNT_REQUIRED_FEATURES_STRING_ARRAY, addAccountRequiredFeatures);
        return intent;
    }

    public void addOnAccountsUpdatedListener(OnAccountsUpdateListener listener, Handler handler, boolean updateImmediately) {
        if (listener == null) {
            throw new IllegalArgumentException("the listener is null");
        }
        synchronized (this.mAccountsUpdatedListeners) {
            if (this.mAccountsUpdatedListeners.containsKey(listener)) {
                throw new IllegalStateException("this listener is already added");
            }
            boolean wasEmpty = this.mAccountsUpdatedListeners.isEmpty();
            this.mAccountsUpdatedListeners.put(listener, handler);
            if (wasEmpty) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(LOGIN_ACCOUNTS_CHANGED_ACTION);
                intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_OK);
                this.mContext.registerReceiver(this.mAccountsChangedBroadcastReceiver, intentFilter);
            }
        }
        if (updateImmediately) {
            postToHandler(handler, listener, getAccounts());
        }
    }

    public void removeOnAccountsUpdatedListener(OnAccountsUpdateListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is null");
        }
        synchronized (this.mAccountsUpdatedListeners) {
            if (this.mAccountsUpdatedListeners.containsKey(listener)) {
                this.mAccountsUpdatedListeners.remove(listener);
                if (this.mAccountsUpdatedListeners.isEmpty()) {
                    this.mContext.unregisterReceiver(this.mAccountsChangedBroadcastReceiver);
                }
                return;
            }
            Log.e(TAG, "Listener was not previously added");
        }
    }

    public AccountManagerFuture<Bundle> startAddAccountSession(String accountType, String authTokenType, String[] requiredFeatures, Bundle options, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        }
        Bundle optionsIn = new Bundle();
        if (options != null) {
            optionsIn.putAll(options);
        }
        optionsIn.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
        return new AnonymousClass20(this, this, activity, handler, callback, accountType, authTokenType, requiredFeatures, activity, optionsIn).start();
    }

    public AccountManagerFuture<Bundle> startUpdateCredentialsSession(Account account, String authTokenType, Bundle options, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        Bundle optionsIn = new Bundle();
        if (options != null) {
            optionsIn.putAll(options);
        }
        optionsIn.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
        return new AnonymousClass21(this, this, activity, handler, callback, account, authTokenType, activity, optionsIn).start();
    }

    public AccountManagerFuture<Bundle> finishSession(Bundle sessionBundle, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        return finishSessionAsUser(sessionBundle, activity, Process.myUserHandle(), callback, handler);
    }

    public AccountManagerFuture<Bundle> finishSessionAsUser(Bundle sessionBundle, Activity activity, UserHandle userHandle, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (sessionBundle == null) {
            throw new IllegalArgumentException("sessionBundle is null");
        }
        Bundle appInfo = new Bundle();
        appInfo.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
        return new AnonymousClass22(this, this, activity, handler, callback, sessionBundle, activity, appInfo, userHandle).start();
    }

    public AccountManagerFuture<Boolean> isCredentialsUpdateSuggested(Account account, String statusToken, AccountManagerCallback<Boolean> callback, Handler handler) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (!TextUtils.isEmpty(statusToken)) {
            return new AnonymousClass23(this, this, handler, callback, account, statusToken).start();
        } else {
            throw new IllegalArgumentException("status token is empty");
        }
    }
}
