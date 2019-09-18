package android.accounts;

import android.accounts.IAccountManagerResponse;
import android.annotation.SystemApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.res.Resources;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.R;
import com.google.android.collect.Maps;
import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AccountManager {
    public static final String ACCOUNT_ACCESS_TOKEN_TYPE = "com.android.AccountManager.ACCOUNT_ACCESS_TOKEN_TYPE";
    public static final String ACTION_ACCOUNT_REMOVED = "android.accounts.action.ACCOUNT_REMOVED";
    public static final String ACTION_AUTHENTICATOR_INTENT = "android.accounts.AccountAuthenticator";
    public static final String ACTION_VISIBLE_ACCOUNTS_CHANGED = "android.accounts.action.VISIBLE_ACCOUNTS_CHANGED";
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
    public static final String KEY_ACCOUNT_ACCESS_ID = "accountAccessId";
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
    public static final String PACKAGE_NAME_KEY_LEGACY_NOT_VISIBLE = "android:accounts:key_legacy_not_visible";
    public static final String PACKAGE_NAME_KEY_LEGACY_VISIBLE = "android:accounts:key_legacy_visible";
    private static final String TAG = "AccountManager";
    public static final int VISIBILITY_NOT_VISIBLE = 3;
    public static final int VISIBILITY_UNDEFINED = 0;
    public static final int VISIBILITY_USER_MANAGED_NOT_VISIBLE = 4;
    public static final int VISIBILITY_USER_MANAGED_VISIBLE = 2;
    public static final int VISIBILITY_VISIBLE = 1;
    private final BroadcastReceiver mAccountsChangedBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Account[] accounts = AccountManager.this.getAccounts();
            synchronized (AccountManager.this.mAccountsUpdatedListeners) {
                for (Map.Entry<OnAccountsUpdateListener, Handler> entry : AccountManager.this.mAccountsUpdatedListeners.entrySet()) {
                    AccountManager.this.postToHandler(entry.getValue(), entry.getKey(), accounts);
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public final HashMap<OnAccountsUpdateListener, Handler> mAccountsUpdatedListeners = Maps.newHashMap();
    /* access modifiers changed from: private */
    public final HashMap<OnAccountsUpdateListener, Set<String>> mAccountsUpdatedListenersTypes = Maps.newHashMap();
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public final Handler mMainHandler;
    /* access modifiers changed from: private */
    public final IAccountManager mService;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AccountVisibility {
    }

    private abstract class AmsTask extends FutureTask<Bundle> implements AccountManagerFuture<Bundle> {
        final Activity mActivity;
        final AccountManagerCallback<Bundle> mCallback;
        final Handler mHandler;
        final IAccountManagerResponse mResponse = new Response();

        private class Response extends IAccountManagerResponse.Stub {
            private Response() {
            }

            public void onResult(Bundle bundle) {
                if (bundle == null) {
                    onError(5, "null bundle returned");
                    return;
                }
                Intent intent = (Intent) bundle.getParcelable("intent");
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
                if (code == 4 || code == 100 || code == 101) {
                    AmsTask.this.cancel(true);
                } else {
                    AmsTask.this.setException(AccountManager.this.convertErrorToException(code, message));
                }
            }
        }

        public abstract void doWork() throws RemoteException;

        public AmsTask(Activity activity, Handler handler, AccountManagerCallback<Bundle> callback) {
            super(new Callable<Bundle>() {
                public Bundle call() throws Exception {
                    throw new IllegalStateException("this should never be called");
                }
            });
            this.mHandler = handler;
            this.mCallback = callback;
            this.mActivity = activity;
        }

        public final AccountManagerFuture<Bundle> start() {
            try {
                doWork();
            } catch (RemoteException e) {
                setException(e);
            }
            return this;
        }

        /* access modifiers changed from: protected */
        public void set(Bundle bundle) {
            if (bundle == null) {
                Log.e(AccountManager.TAG, "the bundle must not be null", new Exception());
            }
            super.set(bundle);
        }

        private Bundle internalGetResult(Long timeout, TimeUnit unit) throws OperationCanceledException, IOException, AuthenticatorException {
            if (!isDone()) {
                AccountManager.this.ensureNotOnMainThread();
            }
            if (timeout == null) {
                try {
                    Bundle bundle = (Bundle) get();
                    cancel(true);
                    return bundle;
                } catch (CancellationException e) {
                    throw new OperationCanceledException();
                } catch (InterruptedException | TimeoutException e2) {
                    cancel(true);
                    throw new OperationCanceledException();
                } catch (ExecutionException e3) {
                    Throwable cause = e3.getCause();
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
                    throw th;
                }
            } else {
                Bundle bundle2 = (Bundle) get(timeout.longValue(), unit);
                cancel(true);
                return bundle2;
            }
        }

        public Bundle getResult() throws OperationCanceledException, IOException, AuthenticatorException {
            return internalGetResult(null, null);
        }

        public Bundle getResult(long timeout, TimeUnit unit) throws OperationCanceledException, IOException, AuthenticatorException {
            return internalGetResult(Long.valueOf(timeout), unit);
        }

        /* access modifiers changed from: protected */
        public void done() {
            if (this.mCallback != null) {
                AccountManager.this.postToHandler(this.mHandler, this.mCallback, (AccountManagerFuture<Bundle>) this);
            }
        }
    }

    private abstract class BaseFutureTask<T> extends FutureTask<T> {
        final Handler mHandler;
        public final IAccountManagerResponse mResponse = new Response();

        protected class Response extends IAccountManagerResponse.Stub {
            protected Response() {
            }

            public void onResult(Bundle bundle) {
                try {
                    T result = BaseFutureTask.this.bundleToResult(bundle);
                    if (result != null) {
                        BaseFutureTask.this.set(result);
                    }
                } catch (AuthenticatorException | ClassCastException e) {
                    onError(5, "no result in response");
                }
            }

            public void onError(int code, String message) {
                if (code == 4 || code == 100 || code == 101) {
                    BaseFutureTask.this.cancel(true);
                } else {
                    BaseFutureTask.this.setException(AccountManager.this.convertErrorToException(code, message));
                }
            }
        }

        public abstract T bundleToResult(Bundle bundle) throws AuthenticatorException;

        public abstract void doWork() throws RemoteException;

        public BaseFutureTask(Handler handler) {
            super(new Callable<T>() {
                public T call() throws Exception {
                    throw new IllegalStateException("this should never be called");
                }
            });
            this.mHandler = handler;
        }

        /* access modifiers changed from: protected */
        public void postRunnableToHandler(Runnable runnable) {
            (this.mHandler == null ? AccountManager.this.mMainHandler : this.mHandler).post(runnable);
        }

        /* access modifiers changed from: protected */
        public void startTask() {
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

        /* access modifiers changed from: protected */
        public void done() {
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
            if (timeout == null) {
                try {
                    T t = get();
                    cancel(true);
                    return t;
                } catch (InterruptedException | CancellationException | TimeoutException e) {
                    cancel(true);
                    throw new OperationCanceledException();
                } catch (ExecutionException e2) {
                    Throwable cause = e2.getCause();
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
                    throw th;
                }
            } else {
                T t2 = get(timeout.longValue(), unit);
                cancel(true);
                return t2;
            }
        }

        public T getResult() throws OperationCanceledException, IOException, AuthenticatorException {
            return internalGetResult(null, null);
        }

        public T getResult(long timeout, TimeUnit unit) throws OperationCanceledException, IOException, AuthenticatorException {
            return internalGetResult(Long.valueOf(timeout), unit);
        }
    }

    private class GetAuthTokenByTypeAndFeaturesTask extends AmsTask implements AccountManagerCallback<Bundle> {
        final String mAccountType;
        final Bundle mAddAccountOptions;
        final String mAuthTokenType;
        final String[] mFeatures;
        volatile AccountManagerFuture<Bundle> mFuture = null;
        final Bundle mLoginOptions;
        final AccountManagerCallback<Bundle> mMyCallback;
        /* access modifiers changed from: private */
        public volatile int mNumAccounts = 0;

        GetAuthTokenByTypeAndFeaturesTask(String accountType, String authTokenType, String[] features, Activity activityForPrompting, Bundle addAccountOptions, Bundle loginOptions, AccountManagerCallback<Bundle> callback, Handler handler) {
            super(activityForPrompting, handler, callback);
            if (accountType != null) {
                this.mAccountType = accountType;
                this.mAuthTokenType = authTokenType;
                this.mFeatures = features;
                this.mAddAccountOptions = addAccountOptions;
                this.mLoginOptions = loginOptions;
                this.mMyCallback = this;
                return;
            }
            throw new IllegalArgumentException("account type is null");
        }

        public void doWork() throws RemoteException {
            AccountManager.this.getAccountByTypeAndFeatures(this.mAccountType, this.mFeatures, new AccountManagerCallback<Bundle>() {
                public void run(AccountManagerFuture<Bundle> future) {
                    try {
                        Bundle result = future.getResult();
                        String accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME);
                        String accountType = result.getString(AccountManager.KEY_ACCOUNT_TYPE);
                        if (accountName != null) {
                            int unused = GetAuthTokenByTypeAndFeaturesTask.this.mNumAccounts = 1;
                            Account account = new Account(accountName, accountType);
                            if (GetAuthTokenByTypeAndFeaturesTask.this.mActivity == null) {
                                GetAuthTokenByTypeAndFeaturesTask.this.mFuture = AccountManager.this.getAuthToken(account, GetAuthTokenByTypeAndFeaturesTask.this.mAuthTokenType, false, GetAuthTokenByTypeAndFeaturesTask.this.mMyCallback, GetAuthTokenByTypeAndFeaturesTask.this.mHandler);
                            } else {
                                GetAuthTokenByTypeAndFeaturesTask.this.mFuture = AccountManager.this.getAuthToken(account, GetAuthTokenByTypeAndFeaturesTask.this.mAuthTokenType, GetAuthTokenByTypeAndFeaturesTask.this.mLoginOptions, GetAuthTokenByTypeAndFeaturesTask.this.mActivity, GetAuthTokenByTypeAndFeaturesTask.this.mMyCallback, GetAuthTokenByTypeAndFeaturesTask.this.mHandler);
                            }
                        } else if (GetAuthTokenByTypeAndFeaturesTask.this.mActivity != null) {
                            GetAuthTokenByTypeAndFeaturesTask.this.mFuture = AccountManager.this.addAccount(GetAuthTokenByTypeAndFeaturesTask.this.mAccountType, GetAuthTokenByTypeAndFeaturesTask.this.mAuthTokenType, GetAuthTokenByTypeAndFeaturesTask.this.mFeatures, GetAuthTokenByTypeAndFeaturesTask.this.mAddAccountOptions, GetAuthTokenByTypeAndFeaturesTask.this.mActivity, GetAuthTokenByTypeAndFeaturesTask.this.mMyCallback, GetAuthTokenByTypeAndFeaturesTask.this.mHandler);
                        } else {
                            Bundle result2 = new Bundle();
                            result2.putString(AccountManager.KEY_ACCOUNT_NAME, null);
                            result2.putString(AccountManager.KEY_ACCOUNT_TYPE, null);
                            result2.putString(AccountManager.KEY_AUTHTOKEN, null);
                            result2.putBinder(AccountManager.KEY_ACCOUNT_ACCESS_ID, null);
                            try {
                                GetAuthTokenByTypeAndFeaturesTask.this.mResponse.onResult(result2);
                            } catch (RemoteException e) {
                                Log.e(AccountManager.TAG, "doWork()");
                            }
                        }
                    } catch (OperationCanceledException e2) {
                        GetAuthTokenByTypeAndFeaturesTask.this.setException(e2);
                    } catch (IOException e3) {
                        GetAuthTokenByTypeAndFeaturesTask.this.setException(e3);
                    } catch (AuthenticatorException e4) {
                        GetAuthTokenByTypeAndFeaturesTask.this.setException(e4);
                    }
                }
            }, this.mHandler);
        }

        public void run(AccountManagerFuture<Bundle> future) {
            try {
                Bundle result = future.getResult();
                if (this.mNumAccounts == 0) {
                    String accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME);
                    String accountType = result.getString(AccountManager.KEY_ACCOUNT_TYPE);
                    if (!TextUtils.isEmpty(accountName)) {
                        if (!TextUtils.isEmpty(accountType)) {
                            Account account = new Account(accountName, accountType, result.getString(AccountManager.KEY_ACCOUNT_ACCESS_ID));
                            this.mNumAccounts = 1;
                            AccountManager.this.getAuthToken(account, this.mAuthTokenType, (Bundle) null, this.mActivity, this.mMyCallback, this.mHandler);
                            return;
                        }
                    }
                    setException(new AuthenticatorException("account not in result"));
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
        this.mContext = context;
        this.mService = service;
        this.mMainHandler = new Handler(this.mContext.getMainLooper());
    }

    public AccountManager(Context context, IAccountManager service, Handler handler) {
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
            return (AccountManager) context.getSystemService("account");
        }
        throw new IllegalArgumentException("context is null");
    }

    public String getPassword(Account account) {
        if (account != null) {
            try {
                return this.mService.getPassword(account);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("account is null");
        }
    }

    public String getUserData(Account account, String key) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (key != null) {
            try {
                return this.mService.getUserData(account, key);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("key is null");
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
        return getAccountsByTypeAsUser(type, this.mContext.getUser());
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
            final String str = accountType;
            final String str2 = authTokenType;
            AnonymousClass1 r0 = new Future2Task<String>(handler, callback) {
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.getAuthTokenLabel(this.mResponse, str, str2);
                }

                public String bundleToResult(Bundle bundle) throws AuthenticatorException {
                    if (bundle.containsKey(AccountManager.KEY_AUTH_TOKEN_LABEL)) {
                        return bundle.getString(AccountManager.KEY_AUTH_TOKEN_LABEL);
                    }
                    throw new AuthenticatorException("no result in response");
                }
            };
            return r0.start();
        } else {
            throw new IllegalArgumentException("authTokenType is null");
        }
    }

    public AccountManagerFuture<Boolean> hasFeatures(Account account, String[] features, AccountManagerCallback<Boolean> callback, Handler handler) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (features != null) {
            final Account account2 = account;
            final String[] strArr = features;
            AnonymousClass2 r0 = new Future2Task<Boolean>(handler, callback) {
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.hasFeatures(this.mResponse, account2, strArr, AccountManager.this.mContext.getOpPackageName());
                }

                public Boolean bundleToResult(Bundle bundle) throws AuthenticatorException {
                    if (bundle.containsKey(AccountManager.KEY_BOOLEAN_RESULT)) {
                        return Boolean.valueOf(bundle.getBoolean(AccountManager.KEY_BOOLEAN_RESULT));
                    }
                    throw new AuthenticatorException("no result in response");
                }
            };
            return r0.start();
        } else {
            throw new IllegalArgumentException("features is null");
        }
    }

    public AccountManagerFuture<Account[]> getAccountsByTypeAndFeatures(String type, String[] features, AccountManagerCallback<Account[]> callback, Handler handler) {
        if (type != null) {
            final String str = type;
            final String[] strArr = features;
            AnonymousClass3 r0 = new Future2Task<Account[]>(handler, callback) {
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.getAccountsByFeatures(this.mResponse, str, strArr, AccountManager.this.mContext.getOpPackageName());
                }

                public Account[] bundleToResult(Bundle bundle) throws AuthenticatorException {
                    if (bundle.containsKey(AccountManager.KEY_ACCOUNTS)) {
                        Parcelable[] parcelables = bundle.getParcelableArray(AccountManager.KEY_ACCOUNTS);
                        Account[] descs = new Account[parcelables.length];
                        for (int i = 0; i < parcelables.length; i++) {
                            descs[i] = (Account) parcelables[i];
                        }
                        return descs;
                    }
                    throw new AuthenticatorException("no result in response");
                }
            };
            return r0.start();
        }
        throw new IllegalArgumentException("type is null");
    }

    public boolean addAccountExplicitly(Account account, String password, Bundle userdata) {
        if (account != null) {
            try {
                return this.mService.addAccountExplicitly(account, password, userdata);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("account is null");
        }
    }

    public boolean addAccountExplicitly(Account account, String password, Bundle extras, Map<String, Integer> visibility) {
        if (account != null) {
            try {
                return this.mService.addAccountExplicitlyWithVisibility(account, password, extras, visibility);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("account is null");
        }
    }

    public Map<String, Integer> getPackagesAndVisibilityForAccount(Account account) {
        if (account != null) {
            try {
                return this.mService.getPackagesAndVisibilityForAccount(account);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("account is null");
        }
    }

    public Map<Account, Integer> getAccountsAndVisibilityForPackage(String packageName, String accountType) {
        try {
            return this.mService.getAccountsAndVisibilityForPackage(packageName, accountType);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public boolean setAccountVisibility(Account account, String packageName, int visibility) {
        if (account != null) {
            try {
                return this.mService.setAccountVisibility(account, packageName, visibility);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("account is null");
        }
    }

    public int getAccountVisibility(Account account, String packageName) {
        if (account != null) {
            try {
                return this.mService.getAccountVisibility(account, packageName);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("account is null");
        }
    }

    public boolean notifyAccountAuthenticated(Account account) {
        if (account != null) {
            try {
                return this.mService.accountAuthenticated(account);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("account is null");
        }
    }

    public AccountManagerFuture<Account> renameAccount(Account account, String newName, AccountManagerCallback<Account> callback, Handler handler) {
        if (account == null) {
            throw new IllegalArgumentException("account is null.");
        } else if (!TextUtils.isEmpty(newName)) {
            final Account account2 = account;
            final String str = newName;
            AnonymousClass4 r1 = new Future2Task<Account>(handler, callback) {
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.renameAccount(this.mResponse, account2, str);
                }

                public Account bundleToResult(Bundle bundle) throws AuthenticatorException {
                    return new Account(bundle.getString(AccountManager.KEY_ACCOUNT_NAME), bundle.getString(AccountManager.KEY_ACCOUNT_TYPE), bundle.getString(AccountManager.KEY_ACCOUNT_ACCESS_ID));
                }
            };
            return r1.start();
        } else {
            throw new IllegalArgumentException("newName is empty or null.");
        }
    }

    public String getPreviousName(Account account) {
        if (account != null) {
            try {
                return this.mService.getPreviousName(account);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("account is null");
        }
    }

    @Deprecated
    public AccountManagerFuture<Boolean> removeAccount(final Account account, AccountManagerCallback<Boolean> callback, Handler handler) {
        if (account != null) {
            return new Future2Task<Boolean>(handler, callback) {
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.removeAccount(this.mResponse, account, false);
                }

                public Boolean bundleToResult(Bundle bundle) throws AuthenticatorException {
                    if (bundle.containsKey(AccountManager.KEY_BOOLEAN_RESULT)) {
                        return Boolean.valueOf(bundle.getBoolean(AccountManager.KEY_BOOLEAN_RESULT));
                    }
                    throw new AuthenticatorException("no result in response");
                }
            }.start();
        }
        throw new IllegalArgumentException("account is null");
    }

    public AccountManagerFuture<Bundle> removeAccount(Account account, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (account != null) {
            final Account account2 = account;
            final Activity activity2 = activity;
            AnonymousClass6 r0 = new AmsTask(activity, handler, callback) {
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.removeAccount(this.mResponse, account2, activity2 != null);
                }
            };
            return r0.start();
        }
        throw new IllegalArgumentException("account is null");
    }

    @Deprecated
    public AccountManagerFuture<Boolean> removeAccountAsUser(Account account, AccountManagerCallback<Boolean> callback, Handler handler, UserHandle userHandle) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (userHandle != null) {
            final Account account2 = account;
            final UserHandle userHandle2 = userHandle;
            AnonymousClass7 r0 = new Future2Task<Boolean>(handler, callback) {
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.removeAccountAsUser(this.mResponse, account2, false, userHandle2.getIdentifier());
                }

                public Boolean bundleToResult(Bundle bundle) throws AuthenticatorException {
                    if (bundle.containsKey(AccountManager.KEY_BOOLEAN_RESULT)) {
                        return Boolean.valueOf(bundle.getBoolean(AccountManager.KEY_BOOLEAN_RESULT));
                    }
                    throw new AuthenticatorException("no result in response");
                }
            };
            return r0.start();
        } else {
            throw new IllegalArgumentException("userHandle is null");
        }
    }

    public AccountManagerFuture<Bundle> removeAccountAsUser(Account account, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler, UserHandle userHandle) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (userHandle != null) {
            final Account account2 = account;
            final Activity activity2 = activity;
            final UserHandle userHandle2 = userHandle;
            AnonymousClass8 r0 = new AmsTask(activity, handler, callback) {
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.removeAccountAsUser(this.mResponse, account2, activity2 != null, userHandle2.getIdentifier());
                }
            };
            return r0.start();
        } else {
            throw new IllegalArgumentException("userHandle is null");
        }
    }

    public boolean removeAccountExplicitly(Account account) {
        if (account != null) {
            try {
                return this.mService.removeAccountExplicitly(account);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("account is null");
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
        } else if (authTokenType != null) {
            try {
                return this.mService.peekAuthToken(account, authTokenType);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("authTokenType is null");
        }
    }

    public void setPassword(Account account, String password) {
        if (account != null) {
            try {
                this.mService.setPassword(account, password);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("account is null");
        }
    }

    public void clearPassword(Account account) {
        if (account != null) {
            try {
                this.mService.clearPassword(account);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("account is null");
        }
    }

    public void setUserData(Account account, String key, String value) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (key != null) {
            try {
                this.mService.setUserData(account, key, value);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("key is null");
        }
    }

    public void setAuthToken(Account account, String authTokenType, String authToken) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (authTokenType != null) {
            try {
                this.mService.setAuthToken(account, authTokenType, authToken);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            throw new IllegalArgumentException("authTokenType is null");
        }
    }

    public String blockingGetAuthToken(Account account, String authTokenType, boolean notifyAuthFailure) throws OperationCanceledException, IOException, AuthenticatorException {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (authTokenType != null) {
            Bundle bundle = getAuthToken(account, authTokenType, notifyAuthFailure, null, null).getResult();
            if (bundle != null) {
                return bundle.getString(KEY_AUTHTOKEN);
            }
            Log.e(TAG, "blockingGetAuthToken: null was returned from getResult() for " + account + ", authTokenType " + authTokenType);
            return null;
        } else {
            throw new IllegalArgumentException("authTokenType is null");
        }
    }

    public AccountManagerFuture<Bundle> getAuthToken(Account account, String authTokenType, Bundle options, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        Bundle bundle = options;
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (authTokenType != null) {
            Bundle optionsIn = new Bundle();
            if (bundle != null) {
                optionsIn.putAll(bundle);
            }
            optionsIn.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
            final Account account2 = account;
            final String str = authTokenType;
            final Bundle bundle2 = optionsIn;
            AnonymousClass9 r1 = new AmsTask(activity, handler, callback) {
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.getAuthToken(this.mResponse, account2, str, false, true, bundle2);
                }
            };
            return r1.start();
        } else {
            throw new IllegalArgumentException("authTokenType is null");
        }
    }

    @Deprecated
    public AccountManagerFuture<Bundle> getAuthToken(Account account, String authTokenType, boolean notifyAuthFailure, AccountManagerCallback<Bundle> callback, Handler handler) {
        return getAuthToken(account, authTokenType, (Bundle) null, notifyAuthFailure, callback, handler);
    }

    public AccountManagerFuture<Bundle> getAuthToken(Account account, String authTokenType, Bundle options, boolean notifyAuthFailure, AccountManagerCallback<Bundle> callback, Handler handler) {
        Bundle bundle = options;
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (authTokenType != null) {
            Bundle optionsIn = new Bundle();
            if (bundle != null) {
                optionsIn.putAll(bundle);
            }
            optionsIn.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
            final Account account2 = account;
            final String str = authTokenType;
            final boolean z = notifyAuthFailure;
            final Bundle bundle2 = optionsIn;
            AnonymousClass10 r1 = new AmsTask(null, handler, callback) {
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.getAuthToken(this.mResponse, account2, str, z, false, bundle2);
                }
            };
            return r1.start();
        } else {
            throw new IllegalArgumentException("authTokenType is null");
        }
    }

    public AccountManagerFuture<Bundle> addAccount(String accountType, String authTokenType, String[] requiredFeatures, Bundle addAccountOptions, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        Bundle bundle = addAccountOptions;
        if (accountType != null) {
            Bundle optionsIn = new Bundle();
            if (bundle != null) {
                optionsIn.putAll(bundle);
            }
            optionsIn.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
            final String str = accountType;
            final String str2 = authTokenType;
            final String[] strArr = requiredFeatures;
            final Activity activity2 = activity;
            final Bundle bundle2 = optionsIn;
            AnonymousClass11 r1 = new AmsTask(activity, handler, callback) {
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.addAccount(this.mResponse, str, str2, strArr, activity2 != null, bundle2);
                }
            };
            return r1.start();
        }
        throw new IllegalArgumentException("accountType is null");
    }

    public AccountManagerFuture<Bundle> addAccountAsUser(String accountType, String authTokenType, String[] requiredFeatures, Bundle addAccountOptions, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler, UserHandle userHandle) {
        Bundle bundle = addAccountOptions;
        if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        } else if (userHandle != null) {
            Bundle optionsIn = new Bundle();
            if (bundle != null) {
                optionsIn.putAll(bundle);
            }
            optionsIn.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
            final String str = accountType;
            final String str2 = authTokenType;
            final String[] strArr = requiredFeatures;
            final Activity activity2 = activity;
            final Bundle bundle2 = optionsIn;
            AnonymousClass12 r0 = r1;
            final UserHandle userHandle2 = userHandle;
            AnonymousClass12 r1 = new AmsTask(activity, handler, callback) {
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.addAccountAsUser(this.mResponse, str, str2, strArr, activity2 != null, bundle2, userHandle2.getIdentifier());
                }
            };
            return r0.start();
        } else {
            throw new IllegalArgumentException("userHandle is null");
        }
    }

    public void addSharedAccountsFromParentUser(UserHandle parentUser, UserHandle user) {
        try {
            this.mService.addSharedAccountsFromParentUser(parentUser.getIdentifier(), user.getIdentifier(), this.mContext.getOpPackageName());
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public AccountManagerFuture<Boolean> copyAccountToUser(Account account, UserHandle fromUser, UserHandle toUser, AccountManagerCallback<Boolean> callback, Handler handler) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (toUser == null || fromUser == null) {
            throw new IllegalArgumentException("fromUser and toUser cannot be null");
        } else {
            final Account account2 = account;
            final UserHandle userHandle = fromUser;
            final UserHandle userHandle2 = toUser;
            AnonymousClass13 r0 = new Future2Task<Boolean>(handler, callback) {
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.copyAccountToUser(this.mResponse, account2, userHandle.getIdentifier(), userHandle2.getIdentifier());
                }

                public Boolean bundleToResult(Bundle bundle) throws AuthenticatorException {
                    if (bundle.containsKey(AccountManager.KEY_BOOLEAN_RESULT)) {
                        return Boolean.valueOf(bundle.getBoolean(AccountManager.KEY_BOOLEAN_RESULT));
                    }
                    throw new AuthenticatorException("no result in response");
                }
            };
            return r0.start();
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
        return confirmCredentialsAsUser(account, options, activity, callback, handler, this.mContext.getUser());
    }

    public AccountManagerFuture<Bundle> confirmCredentialsAsUser(Account account, Bundle options, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler, UserHandle userHandle) {
        if (account != null) {
            final Account account2 = account;
            final Bundle bundle = options;
            final Activity activity2 = activity;
            final int identifier = userHandle.getIdentifier();
            AnonymousClass14 r0 = new AmsTask(activity, handler, callback) {
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.confirmCredentialsAsUser(this.mResponse, account2, bundle, activity2 != null, identifier);
                }
            };
            return r0.start();
        }
        throw new IllegalArgumentException("account is null");
    }

    public AccountManagerFuture<Bundle> updateCredentials(Account account, String authTokenType, Bundle options, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (account != null) {
            final Account account2 = account;
            final String str = authTokenType;
            final Activity activity2 = activity;
            final Bundle bundle = options;
            AnonymousClass15 r0 = new AmsTask(activity, handler, callback) {
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.updateCredentials(this.mResponse, account2, str, activity2 != null, bundle);
                }
            };
            return r0.start();
        }
        throw new IllegalArgumentException("account is null");
    }

    public AccountManagerFuture<Bundle> editProperties(String accountType, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (accountType != null) {
            final String str = accountType;
            final Activity activity2 = activity;
            AnonymousClass16 r0 = new AmsTask(activity, handler, callback) {
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.editProperties(this.mResponse, str, activity2 != null);
                }
            };
            return r0.start();
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

    /* access modifiers changed from: private */
    public void ensureNotOnMainThread() {
        Looper looper = Looper.myLooper();
        if (looper != null && looper == this.mContext.getMainLooper()) {
            IllegalStateException exception = new IllegalStateException("calling this from your main thread can lead to deadlock");
            Log.e(TAG, "calling this from your main thread can lead to deadlock and/or ANRs", exception);
            if (this.mContext.getApplicationInfo().targetSdkVersion >= 8) {
                throw exception;
            }
        }
    }

    /* access modifiers changed from: private */
    public void postToHandler(Handler handler, final AccountManagerCallback<Bundle> callback, final AccountManagerFuture<Bundle> future) {
        (handler == null ? this.mMainHandler : handler).post(new Runnable() {
            public void run() {
                callback.run(future);
            }
        });
    }

    /* access modifiers changed from: private */
    public void postToHandler(Handler handler, final OnAccountsUpdateListener listener, Account[] accounts) {
        final Account[] accountsCopy = new Account[accounts.length];
        System.arraycopy(accounts, 0, accountsCopy, 0, accountsCopy.length);
        (handler == null ? this.mMainHandler : handler).post(new Runnable() {
            public void run() {
                synchronized (AccountManager.this.mAccountsUpdatedListeners) {
                    try {
                        if (AccountManager.this.mAccountsUpdatedListeners.containsKey(listener)) {
                            Set<String> types = (Set) AccountManager.this.mAccountsUpdatedListenersTypes.get(listener);
                            if (types != null) {
                                ArrayList<Account> filtered = new ArrayList<>();
                                for (Account account : accountsCopy) {
                                    if (types.contains(account.type)) {
                                        filtered.add(account);
                                    }
                                }
                                listener.onAccountsUpdated((Account[]) filtered.toArray(new Account[filtered.size()]));
                            } else {
                                listener.onAccountsUpdated(accountsCopy);
                            }
                        }
                    } catch (SQLException e) {
                        Log.e(AccountManager.TAG, "Can't update accounts", e);
                    }
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public Exception convertErrorToException(int code, String message) {
        if (code == 3) {
            return new IOException(message);
        }
        if (code == 6) {
            return new UnsupportedOperationException(message);
        }
        if (code == 5) {
            return new AuthenticatorException(message);
        }
        if (code == 7) {
            return new IllegalArgumentException(message);
        }
        return new AuthenticatorException(message);
    }

    /* access modifiers changed from: private */
    public void getAccountByTypeAndFeatures(String accountType, String[] features, AccountManagerCallback<Bundle> callback, Handler handler) {
        final String str = accountType;
        final String[] strArr = features;
        AnonymousClass19 r0 = new AmsTask(null, handler, callback) {
            public void doWork() throws RemoteException {
                AccountManager.this.mService.getAccountByTypeAndFeatures(this.mResponse, str, strArr, AccountManager.this.mContext.getOpPackageName());
            }
        };
        r0.start();
    }

    public AccountManagerFuture<Bundle> getAuthTokenByFeatures(String accountType, String authTokenType, String[] features, Activity activity, Bundle addAccountOptions, Bundle getAuthTokenOptions, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (accountType == null) {
            throw new IllegalArgumentException("account type is null");
        } else if (authTokenType != null) {
            GetAuthTokenByTypeAndFeaturesTask task = new GetAuthTokenByTypeAndFeaturesTask(accountType, authTokenType, features, activity, addAccountOptions, getAuthTokenOptions, callback, handler);
            task.start();
            return task;
        } else {
            throw new IllegalArgumentException("authTokenType is null");
        }
    }

    @Deprecated
    public static Intent newChooseAccountIntent(Account selectedAccount, ArrayList<Account> allowableAccounts, String[] allowableAccountTypes, boolean alwaysPromptForAccount, String descriptionOverrideText, String addAccountAuthTokenType, String[] addAccountRequiredFeatures, Bundle addAccountOptions) {
        return newChooseAccountIntent(selectedAccount, allowableAccounts, allowableAccountTypes, descriptionOverrideText, addAccountAuthTokenType, addAccountRequiredFeatures, addAccountOptions);
    }

    public static Intent newChooseAccountIntent(Account selectedAccount, List<Account> allowableAccounts, String[] allowableAccountTypes, String descriptionOverrideText, String addAccountAuthTokenType, String[] addAccountRequiredFeatures, Bundle addAccountOptions) {
        Intent intent = new Intent();
        ComponentName componentName = ComponentName.unflattenFromString(Resources.getSystem().getString(R.string.config_chooseTypeAndAccountActivity));
        intent.setClassName(componentName.getPackageName(), componentName.getClassName());
        intent.putExtra(ChooseTypeAndAccountActivity.EXTRA_ALLOWABLE_ACCOUNTS_ARRAYLIST, (Serializable) allowableAccounts == null ? null : new ArrayList(allowableAccounts));
        intent.putExtra(ChooseTypeAndAccountActivity.EXTRA_ALLOWABLE_ACCOUNT_TYPES_STRING_ARRAY, allowableAccountTypes);
        intent.putExtra(ChooseTypeAndAccountActivity.EXTRA_ADD_ACCOUNT_OPTIONS_BUNDLE, addAccountOptions);
        intent.putExtra(ChooseTypeAndAccountActivity.EXTRA_SELECTED_ACCOUNT, (Parcelable) selectedAccount);
        intent.putExtra(ChooseTypeAndAccountActivity.EXTRA_DESCRIPTION_TEXT_OVERRIDE, descriptionOverrideText);
        intent.putExtra("authTokenType", addAccountAuthTokenType);
        intent.putExtra(ChooseTypeAndAccountActivity.EXTRA_ADD_ACCOUNT_REQUIRED_FEATURES_STRING_ARRAY, addAccountRequiredFeatures);
        return intent;
    }

    public void addOnAccountsUpdatedListener(OnAccountsUpdateListener listener, Handler handler, boolean updateImmediately) {
        addOnAccountsUpdatedListener(listener, handler, updateImmediately, null);
    }

    public void addOnAccountsUpdatedListener(OnAccountsUpdateListener listener, Handler handler, boolean updateImmediately, String[] accountTypes) {
        if (listener != null) {
            synchronized (this.mAccountsUpdatedListeners) {
                if (!this.mAccountsUpdatedListeners.containsKey(listener)) {
                    boolean wasEmpty = this.mAccountsUpdatedListeners.isEmpty();
                    this.mAccountsUpdatedListeners.put(listener, handler);
                    if (accountTypes != null) {
                        this.mAccountsUpdatedListenersTypes.put(listener, new HashSet(Arrays.asList(accountTypes)));
                    } else {
                        this.mAccountsUpdatedListenersTypes.put(listener, null);
                    }
                    if (wasEmpty) {
                        IntentFilter intentFilter = new IntentFilter();
                        intentFilter.addAction(ACTION_VISIBLE_ACCOUNTS_CHANGED);
                        intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_OK);
                        this.mContext.registerReceiver(this.mAccountsChangedBroadcastReceiver, intentFilter);
                    }
                    try {
                        this.mService.registerAccountListener(accountTypes, this.mContext.getOpPackageName());
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                } else {
                    throw new IllegalStateException("this listener is already added");
                }
            }
            if (updateImmediately) {
                postToHandler(handler, listener, getAccounts());
                return;
            }
            return;
        }
        throw new IllegalArgumentException("the listener is null");
    }

    public void removeOnAccountsUpdatedListener(OnAccountsUpdateListener listener) {
        String[] accountsArray;
        if (listener != null) {
            synchronized (this.mAccountsUpdatedListeners) {
                if (!this.mAccountsUpdatedListeners.containsKey(listener)) {
                    Log.e(TAG, "Listener was not previously added");
                    return;
                }
                Set<String> accountTypes = this.mAccountsUpdatedListenersTypes.get(listener);
                if (accountTypes != null) {
                    accountsArray = (String[]) accountTypes.toArray(new String[accountTypes.size()]);
                } else {
                    accountsArray = null;
                }
                this.mAccountsUpdatedListeners.remove(listener);
                this.mAccountsUpdatedListenersTypes.remove(listener);
                if (this.mAccountsUpdatedListeners.isEmpty()) {
                    this.mContext.unregisterReceiver(this.mAccountsChangedBroadcastReceiver);
                }
                try {
                    this.mService.unregisterAccountListener(accountsArray, this.mContext.getOpPackageName());
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        } else {
            throw new IllegalArgumentException("listener is null");
        }
    }

    public AccountManagerFuture<Bundle> startAddAccountSession(String accountType, String authTokenType, String[] requiredFeatures, Bundle options, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        Bundle bundle = options;
        if (accountType != null) {
            Bundle optionsIn = new Bundle();
            if (bundle != null) {
                optionsIn.putAll(bundle);
            }
            optionsIn.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
            final String str = accountType;
            final String str2 = authTokenType;
            final String[] strArr = requiredFeatures;
            final Activity activity2 = activity;
            final Bundle bundle2 = optionsIn;
            AnonymousClass21 r1 = new AmsTask(activity, handler, callback) {
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.startAddAccountSession(this.mResponse, str, str2, strArr, activity2 != null, bundle2);
                }
            };
            return r1.start();
        }
        throw new IllegalArgumentException("accountType is null");
    }

    public AccountManagerFuture<Bundle> startUpdateCredentialsSession(Account account, String authTokenType, Bundle options, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        Bundle bundle = options;
        if (account != null) {
            Bundle optionsIn = new Bundle();
            if (bundle != null) {
                optionsIn.putAll(bundle);
            }
            optionsIn.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
            final Account account2 = account;
            final String str = authTokenType;
            final Activity activity2 = activity;
            final Bundle bundle2 = optionsIn;
            AnonymousClass22 r1 = new AmsTask(activity, handler, callback) {
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.startUpdateCredentialsSession(this.mResponse, account2, str, activity2 != null, bundle2);
                }
            };
            return r1.start();
        }
        throw new IllegalArgumentException("account is null");
    }

    public AccountManagerFuture<Bundle> finishSession(Bundle sessionBundle, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        return finishSessionAsUser(sessionBundle, activity, this.mContext.getUser(), callback, handler);
    }

    @SystemApi
    public AccountManagerFuture<Bundle> finishSessionAsUser(Bundle sessionBundle, Activity activity, UserHandle userHandle, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (sessionBundle != null) {
            Bundle appInfo = new Bundle();
            appInfo.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
            final Bundle bundle = sessionBundle;
            final Activity activity2 = activity;
            final Bundle bundle2 = appInfo;
            final UserHandle userHandle2 = userHandle;
            AnonymousClass23 r0 = new AmsTask(activity, handler, callback) {
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.finishSessionAsUser(this.mResponse, bundle, activity2 != null, bundle2, userHandle2.getIdentifier());
                }
            };
            return r0.start();
        }
        throw new IllegalArgumentException("sessionBundle is null");
    }

    public AccountManagerFuture<Boolean> isCredentialsUpdateSuggested(Account account, String statusToken, AccountManagerCallback<Boolean> callback, Handler handler) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (!TextUtils.isEmpty(statusToken)) {
            final Account account2 = account;
            final String str = statusToken;
            AnonymousClass24 r1 = new Future2Task<Boolean>(handler, callback) {
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.isCredentialsUpdateSuggested(this.mResponse, account2, str);
                }

                public Boolean bundleToResult(Bundle bundle) throws AuthenticatorException {
                    if (bundle.containsKey(AccountManager.KEY_BOOLEAN_RESULT)) {
                        return Boolean.valueOf(bundle.getBoolean(AccountManager.KEY_BOOLEAN_RESULT));
                    }
                    throw new AuthenticatorException("no result in response");
                }
            };
            return r1.start();
        } else {
            throw new IllegalArgumentException("status token is empty");
        }
    }

    public boolean hasAccountAccess(Account account, String packageName, UserHandle userHandle) {
        try {
            return this.mService.hasAccountAccess(account, packageName, userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public IntentSender createRequestAccountAccessIntentSenderAsUser(Account account, String packageName, UserHandle userHandle) {
        try {
            return this.mService.createRequestAccountAccessIntentSenderAsUser(account, packageName, userHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
