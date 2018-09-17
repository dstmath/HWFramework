package android.accounts;

import android.accounts.IAccountManagerResponse.Stub;
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
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.collect.Maps;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
                for (Entry<OnAccountsUpdateListener, Handler> entry : AccountManager.this.mAccountsUpdatedListeners.entrySet()) {
                    AccountManager.this.postToHandler((Handler) entry.getValue(), (OnAccountsUpdateListener) entry.getKey(), accounts);
                }
            }
        }
    };
    private final HashMap<OnAccountsUpdateListener, Handler> mAccountsUpdatedListeners = Maps.newHashMap();
    private final HashMap<OnAccountsUpdateListener, Set<String>> mAccountsUpdatedListenersTypes = Maps.newHashMap();
    private final Context mContext;
    private final Handler mMainHandler;
    private final IAccountManager mService;

    private abstract class AmsTask extends FutureTask<Bundle> implements AccountManagerFuture<Bundle> {
        final Activity mActivity;
        final AccountManagerCallback<Bundle> mCallback;
        final Handler mHandler;
        final IAccountManagerResponse mResponse = new Response(this, null);

        private class Response extends Stub {
            /* synthetic */ Response(AmsTask this$1, Response -this1) {
                this();
            }

            private Response() {
            }

            public void onResult(Bundle bundle) {
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
            super(new Callable<Bundle>(AccountManager.this) {
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

    private abstract class BaseFutureTask<T> extends FutureTask<T> {
        final Handler mHandler;
        public final IAccountManagerResponse mResponse = new Response();

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
                    onError(5, "no result in response");
                } catch (AuthenticatorException e2) {
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
            super(new Callable<T>(AccountManager.this) {
                public T call() throws Exception {
                    throw new IllegalStateException("this should never be called");
                }
            });
            this.mHandler = handler;
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

    private class GetAuthTokenByTypeAndFeaturesTask extends AmsTask implements AccountManagerCallback<Bundle> {
        final String mAccountType;
        final Bundle mAddAccountOptions;
        final String mAuthTokenType;
        final String[] mFeatures;
        volatile AccountManagerFuture<Bundle> mFuture = null;
        final Bundle mLoginOptions;
        final AccountManagerCallback<Bundle> mMyCallback;
        private volatile int mNumAccounts = 0;

        GetAuthTokenByTypeAndFeaturesTask(String accountType, String authTokenType, String[] features, Activity activityForPrompting, Bundle addAccountOptions, Bundle loginOptions, AccountManagerCallback<Bundle> callback, Handler handler) {
            super(activityForPrompting, handler, callback);
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
            AccountManager.this.getAccountByTypeAndFeatures(this.mAccountType, this.mFeatures, new AccountManagerCallback<Bundle>() {
                public void run(AccountManagerFuture<Bundle> future) {
                    try {
                        Bundle result = (Bundle) future.getResult();
                        String accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME);
                        String accountType = result.getString(AccountManager.KEY_ACCOUNT_TYPE);
                        if (accountName != null) {
                            GetAuthTokenByTypeAndFeaturesTask.this.mNumAccounts = 1;
                            Account account = new Account(accountName, accountType);
                            if (GetAuthTokenByTypeAndFeaturesTask.this.mActivity == null) {
                                GetAuthTokenByTypeAndFeaturesTask.this.mFuture = AccountManager.this.getAuthToken(account, GetAuthTokenByTypeAndFeaturesTask.this.mAuthTokenType, false, GetAuthTokenByTypeAndFeaturesTask.this.mMyCallback, GetAuthTokenByTypeAndFeaturesTask.this.mHandler);
                            } else {
                                GetAuthTokenByTypeAndFeaturesTask.this.mFuture = AccountManager.this.getAuthToken(account, GetAuthTokenByTypeAndFeaturesTask.this.mAuthTokenType, GetAuthTokenByTypeAndFeaturesTask.this.mLoginOptions, GetAuthTokenByTypeAndFeaturesTask.this.mActivity, GetAuthTokenByTypeAndFeaturesTask.this.mMyCallback, GetAuthTokenByTypeAndFeaturesTask.this.mHandler);
                            }
                        } else if (GetAuthTokenByTypeAndFeaturesTask.this.mActivity != null) {
                            GetAuthTokenByTypeAndFeaturesTask.this.mFuture = AccountManager.this.addAccount(GetAuthTokenByTypeAndFeaturesTask.this.mAccountType, GetAuthTokenByTypeAndFeaturesTask.this.mAuthTokenType, GetAuthTokenByTypeAndFeaturesTask.this.mFeatures, GetAuthTokenByTypeAndFeaturesTask.this.mAddAccountOptions, GetAuthTokenByTypeAndFeaturesTask.this.mActivity, GetAuthTokenByTypeAndFeaturesTask.this.mMyCallback, GetAuthTokenByTypeAndFeaturesTask.this.mHandler);
                        } else {
                            result = new Bundle();
                            result.putString(AccountManager.KEY_ACCOUNT_NAME, null);
                            result.putString(AccountManager.KEY_ACCOUNT_TYPE, null);
                            result.putString(AccountManager.KEY_AUTHTOKEN, null);
                            result.putBinder(AccountManager.KEY_ACCOUNT_ACCESS_ID, null);
                            try {
                                GetAuthTokenByTypeAndFeaturesTask.this.mResponse.onResult(result);
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
                Bundle result = (Bundle) future.getResult();
                if (this.mNumAccounts == 0) {
                    String accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME);
                    String accountType = result.getString(AccountManager.KEY_ACCOUNT_TYPE);
                    if (TextUtils.isEmpty(accountName) || TextUtils.isEmpty(accountType)) {
                        setException(new AuthenticatorException("account not in result"));
                        return;
                    }
                    Account account = new Account(accountName, accountType, result.getString(AccountManager.KEY_ACCOUNT_ACCESS_ID));
                    this.mNumAccounts = 1;
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
        if (result == null || !result.containsKey(KEY_AUTHTOKEN) || (TextUtils.isEmpty(result.getString(KEY_AUTHTOKEN)) ^ 1) == 0) {
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
        } else if (authTokenType == null) {
            throw new IllegalArgumentException("authTokenType is null");
        } else {
            final String str = accountType;
            final String str2 = authTokenType;
            return new Future2Task<String>(this, handler, callback) {
                public void doWork() throws RemoteException {
                    this.mService.getAuthTokenLabel(this.mResponse, str, str2);
                }

                public String bundleToResult(Bundle bundle) throws AuthenticatorException {
                    if (bundle.containsKey(AccountManager.KEY_AUTH_TOKEN_LABEL)) {
                        return bundle.getString(AccountManager.KEY_AUTH_TOKEN_LABEL);
                    }
                    throw new AuthenticatorException("no result in response");
                }
            }.start();
        }
    }

    public AccountManagerFuture<Boolean> hasFeatures(Account account, String[] features, AccountManagerCallback<Boolean> callback, Handler handler) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (features == null) {
            throw new IllegalArgumentException("features is null");
        } else {
            final Account account2 = account;
            final String[] strArr = features;
            return new Future2Task<Boolean>(this, handler, callback) {
                public void doWork() throws RemoteException {
                    this.mService.hasFeatures(this.mResponse, account2, strArr, this.mContext.getOpPackageName());
                }

                public Boolean bundleToResult(Bundle bundle) throws AuthenticatorException {
                    if (bundle.containsKey(AccountManager.KEY_BOOLEAN_RESULT)) {
                        return Boolean.valueOf(bundle.getBoolean(AccountManager.KEY_BOOLEAN_RESULT));
                    }
                    throw new AuthenticatorException("no result in response");
                }
            }.start();
        }
    }

    public AccountManagerFuture<Account[]> getAccountsByTypeAndFeatures(String type, String[] features, AccountManagerCallback<Account[]> callback, Handler handler) {
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }
        final String str = type;
        final String[] strArr = features;
        return new Future2Task<Account[]>(this, handler, callback) {
            public void doWork() throws RemoteException {
                this.mService.getAccountsByFeatures(this.mResponse, str, strArr, this.mContext.getOpPackageName());
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
        }.start();
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

    public boolean addAccountExplicitly(Account account, String password, Bundle extras, Map<String, Integer> visibility) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        try {
            return this.mService.addAccountExplicitlyWithVisibility(account, password, extras, visibility);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Map<String, Integer> getPackagesAndVisibilityForAccount(Account account) {
        if (account != null) {
            return this.mService.getPackagesAndVisibilityForAccount(account);
        }
        try {
            throw new IllegalArgumentException("account is null");
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
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
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        try {
            return this.mService.setAccountVisibility(account, packageName, visibility);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    public int getAccountVisibility(Account account, String packageName) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        try {
            return this.mService.getAccountVisibility(account, packageName);
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
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
        } else if (TextUtils.isEmpty(newName)) {
            throw new IllegalArgumentException("newName is empty or null.");
        } else {
            final Account account2 = account;
            final String str = newName;
            return new Future2Task<Account>(this, handler, callback) {
                public void doWork() throws RemoteException {
                    this.mService.renameAccount(this.mResponse, account2, str);
                }

                public Account bundleToResult(Bundle bundle) throws AuthenticatorException {
                    return new Account(bundle.getString(AccountManager.KEY_ACCOUNT_NAME), bundle.getString(AccountManager.KEY_ACCOUNT_TYPE), bundle.getString(AccountManager.KEY_ACCOUNT_ACCESS_ID));
                }
            }.start();
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
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        final Account account2 = account;
        return new Future2Task<Boolean>(this, handler, callback) {
            public void doWork() throws RemoteException {
                this.mService.removeAccount(this.mResponse, account2, false);
            }

            public Boolean bundleToResult(Bundle bundle) throws AuthenticatorException {
                if (bundle.containsKey(AccountManager.KEY_BOOLEAN_RESULT)) {
                    return Boolean.valueOf(bundle.getBoolean(AccountManager.KEY_BOOLEAN_RESULT));
                }
                throw new AuthenticatorException("no result in response");
            }
        }.start();
    }

    public AccountManagerFuture<Bundle> removeAccount(Account account, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        final Account account2 = account;
        final Activity activity2 = activity;
        return new AmsTask(this, activity, handler, callback) {
            public void doWork() throws RemoteException {
                this.mService.removeAccount(this.mResponse, account2, activity2 != null);
            }
        }.start();
    }

    @Deprecated
    public AccountManagerFuture<Boolean> removeAccountAsUser(Account account, AccountManagerCallback<Boolean> callback, Handler handler, UserHandle userHandle) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (userHandle == null) {
            throw new IllegalArgumentException("userHandle is null");
        } else {
            final Account account2 = account;
            final UserHandle userHandle2 = userHandle;
            return new Future2Task<Boolean>(this, handler, callback) {
                public void doWork() throws RemoteException {
                    this.mService.removeAccountAsUser(this.mResponse, account2, false, userHandle2.getIdentifier());
                }

                public Boolean bundleToResult(Bundle bundle) throws AuthenticatorException {
                    if (bundle.containsKey(AccountManager.KEY_BOOLEAN_RESULT)) {
                        return Boolean.valueOf(bundle.getBoolean(AccountManager.KEY_BOOLEAN_RESULT));
                    }
                    throw new AuthenticatorException("no result in response");
                }
            }.start();
        }
    }

    public AccountManagerFuture<Bundle> removeAccountAsUser(Account account, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler, UserHandle userHandle) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (userHandle == null) {
            throw new IllegalArgumentException("userHandle is null");
        } else {
            final Account account2 = account;
            final Activity activity2 = activity;
            final UserHandle userHandle2 = userHandle;
            return new AmsTask(this, activity, handler, callback) {
                public void doWork() throws RemoteException {
                    this.mService.removeAccountAsUser(this.mResponse, account2, activity2 != null, userHandle2.getIdentifier());
                }
            }.start();
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
            final Bundle optionsIn = new Bundle();
            if (options != null) {
                optionsIn.putAll(options);
            }
            optionsIn.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
            final Account account2 = account;
            final String str = authTokenType;
            return new AmsTask(this, activity, handler, callback) {
                public void doWork() throws RemoteException {
                    this.mService.getAuthToken(this.mResponse, account2, str, false, true, optionsIn);
                }
            }.start();
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
            final Bundle optionsIn = new Bundle();
            if (options != null) {
                optionsIn.putAll(options);
            }
            optionsIn.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
            final Account account2 = account;
            final String str = authTokenType;
            final boolean z = notifyAuthFailure;
            return new AmsTask(this, null, handler, callback) {
                public void doWork() throws RemoteException {
                    this.mService.getAuthToken(this.mResponse, account2, str, z, false, optionsIn);
                }
            }.start();
        }
    }

    public AccountManagerFuture<Bundle> addAccount(String accountType, String authTokenType, String[] requiredFeatures, Bundle addAccountOptions, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        }
        final Bundle optionsIn = new Bundle();
        if (addAccountOptions != null) {
            optionsIn.putAll(addAccountOptions);
        }
        optionsIn.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
        final String str = accountType;
        final String str2 = authTokenType;
        final String[] strArr = requiredFeatures;
        final Activity activity2 = activity;
        return new AmsTask(this, activity, handler, callback) {
            public void doWork() throws RemoteException {
                this.mService.addAccount(this.mResponse, str, str2, strArr, activity2 != null, optionsIn);
            }
        }.start();
    }

    public AccountManagerFuture<Bundle> addAccountAsUser(String accountType, String authTokenType, String[] requiredFeatures, Bundle addAccountOptions, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler, UserHandle userHandle) {
        if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        } else if (userHandle == null) {
            throw new IllegalArgumentException("userHandle is null");
        } else {
            final Bundle optionsIn = new Bundle();
            if (addAccountOptions != null) {
                optionsIn.putAll(addAccountOptions);
            }
            optionsIn.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
            final String str = accountType;
            final String str2 = authTokenType;
            final String[] strArr = requiredFeatures;
            final Activity activity2 = activity;
            final UserHandle userHandle2 = userHandle;
            return new AmsTask(this, activity, handler, callback) {
                public void doWork() throws RemoteException {
                    this.mService.addAccountAsUser(this.mResponse, str, str2, strArr, activity2 != null, optionsIn, userHandle2.getIdentifier());
                }
            }.start();
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
            return new Future2Task<Boolean>(this, handler, callback) {
                public void doWork() throws RemoteException {
                    this.mService.copyAccountToUser(this.mResponse, account2, userHandle.getIdentifier(), userHandle2.getIdentifier());
                }

                public Boolean bundleToResult(Bundle bundle) throws AuthenticatorException {
                    if (bundle.containsKey(AccountManager.KEY_BOOLEAN_RESULT)) {
                        return Boolean.valueOf(bundle.getBoolean(AccountManager.KEY_BOOLEAN_RESULT));
                    }
                    throw new AuthenticatorException("no result in response");
                }
            }.start();
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
        final int userId = userHandle.getIdentifier();
        final Account account2 = account;
        final Bundle bundle = options;
        final Activity activity2 = activity;
        return new AmsTask(this, activity, handler, callback) {
            public void doWork() throws RemoteException {
                this.mService.confirmCredentialsAsUser(this.mResponse, account2, bundle, activity2 != null, userId);
            }
        }.start();
    }

    public AccountManagerFuture<Bundle> updateCredentials(Account account, String authTokenType, Bundle options, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        final Account account2 = account;
        final String str = authTokenType;
        final Activity activity2 = activity;
        final Bundle bundle = options;
        return new AmsTask(this, activity, handler, callback) {
            public void doWork() throws RemoteException {
                this.mService.updateCredentials(this.mResponse, account2, str, activity2 != null, bundle);
            }
        }.start();
    }

    public AccountManagerFuture<Bundle> editProperties(String accountType, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        }
        final String str = accountType;
        final Activity activity2 = activity;
        return new AmsTask(this, activity, handler, callback) {
            public void doWork() throws RemoteException {
                this.mService.editProperties(this.mResponse, str, activity2 != null);
            }
        }.start();
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
            if (this.mContext.getApplicationInfo().targetSdkVersion >= 8) {
                throw exception;
            }
        }
    }

    private void postToHandler(Handler handler, final AccountManagerCallback<Bundle> callback, final AccountManagerFuture<Bundle> future) {
        if (handler == null) {
            handler = this.mMainHandler;
        }
        handler.post(new Runnable() {
            public void run() {
                callback.run(future);
            }
        });
    }

    private void postToHandler(Handler handler, final OnAccountsUpdateListener listener, Account[] accounts) {
        final Account[] accountsCopy = new Account[accounts.length];
        System.arraycopy(accounts, 0, accountsCopy, 0, accountsCopy.length);
        if (handler == null) {
            handler = this.mMainHandler;
        }
        handler.post(new Runnable() {
            public void run() {
                synchronized (AccountManager.this.mAccountsUpdatedListeners) {
                    try {
                        if (AccountManager.this.mAccountsUpdatedListeners.containsKey(listener)) {
                            Set<String> types = (Set) AccountManager.this.mAccountsUpdatedListenersTypes.get(listener);
                            if (types != null) {
                                ArrayList<Account> filtered = new ArrayList();
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
                return;
            }
        });
    }

    private Exception convertErrorToException(int code, String message) {
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

    private void getAccountByTypeAndFeatures(String accountType, String[] features, AccountManagerCallback<Bundle> callback, Handler handler) {
        final String str = accountType;
        final String[] strArr = features;
        new AmsTask(this, null, handler, callback) {
            public void doWork() throws RemoteException {
                this.mService.getAccountByTypeAndFeatures(this.mResponse, str, strArr, this.mContext.getOpPackageName());
            }
        }.start();
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
        ComponentName componentName = ComponentName.unflattenFromString(Resources.getSystem().getString(17039758));
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
        intent.putExtra("authTokenType", addAccountAuthTokenType);
        intent.putExtra(ChooseTypeAndAccountActivity.EXTRA_ADD_ACCOUNT_REQUIRED_FEATURES_STRING_ARRAY, addAccountRequiredFeatures);
        return intent;
    }

    public void addOnAccountsUpdatedListener(OnAccountsUpdateListener listener, Handler handler, boolean updateImmediately) {
        addOnAccountsUpdatedListener(listener, handler, updateImmediately, null);
    }

    public void addOnAccountsUpdatedListener(OnAccountsUpdateListener listener, Handler handler, boolean updateImmediately, String[] accountTypes) {
        if (listener == null) {
            throw new IllegalArgumentException("the listener is null");
        }
        synchronized (this.mAccountsUpdatedListeners) {
            if (this.mAccountsUpdatedListeners.containsKey(listener)) {
                throw new IllegalStateException("this listener is already added");
            }
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
                String[] accountsArray;
                Set<String> accountTypes = (Set) this.mAccountsUpdatedListenersTypes.get(listener);
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
                    return;
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            Log.e(TAG, "Listener was not previously added");
        }
    }

    public AccountManagerFuture<Bundle> startAddAccountSession(String accountType, String authTokenType, String[] requiredFeatures, Bundle options, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        }
        final Bundle optionsIn = new Bundle();
        if (options != null) {
            optionsIn.putAll(options);
        }
        optionsIn.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
        final String str = accountType;
        final String str2 = authTokenType;
        final String[] strArr = requiredFeatures;
        final Activity activity2 = activity;
        return new AmsTask(this, activity, handler, callback) {
            public void doWork() throws RemoteException {
                this.mService.startAddAccountSession(this.mResponse, str, str2, strArr, activity2 != null, optionsIn);
            }
        }.start();
    }

    public AccountManagerFuture<Bundle> startUpdateCredentialsSession(Account account, String authTokenType, Bundle options, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        final Bundle optionsIn = new Bundle();
        if (options != null) {
            optionsIn.putAll(options);
        }
        optionsIn.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
        final Account account2 = account;
        final String str = authTokenType;
        final Activity activity2 = activity;
        return new AmsTask(this, activity, handler, callback) {
            public void doWork() throws RemoteException {
                this.mService.startUpdateCredentialsSession(this.mResponse, account2, str, activity2 != null, optionsIn);
            }
        }.start();
    }

    public AccountManagerFuture<Bundle> finishSession(Bundle sessionBundle, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        return finishSessionAsUser(sessionBundle, activity, Process.myUserHandle(), callback, handler);
    }

    public AccountManagerFuture<Bundle> finishSessionAsUser(Bundle sessionBundle, Activity activity, UserHandle userHandle, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (sessionBundle == null) {
            throw new IllegalArgumentException("sessionBundle is null");
        }
        final Bundle appInfo = new Bundle();
        appInfo.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
        final Bundle bundle = sessionBundle;
        final Activity activity2 = activity;
        final UserHandle userHandle2 = userHandle;
        return new AmsTask(this, activity, handler, callback) {
            public void doWork() throws RemoteException {
                this.mService.finishSessionAsUser(this.mResponse, bundle, activity2 != null, appInfo, userHandle2.getIdentifier());
            }
        }.start();
    }

    public AccountManagerFuture<Boolean> isCredentialsUpdateSuggested(Account account, String statusToken, AccountManagerCallback<Boolean> callback, Handler handler) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (TextUtils.isEmpty(statusToken)) {
            throw new IllegalArgumentException("status token is empty");
        } else {
            final Account account2 = account;
            final String str = statusToken;
            return new Future2Task<Boolean>(this, handler, callback) {
                public void doWork() throws RemoteException {
                    this.mService.isCredentialsUpdateSuggested(this.mResponse, account2, str);
                }

                public Boolean bundleToResult(Bundle bundle) throws AuthenticatorException {
                    if (bundle.containsKey(AccountManager.KEY_BOOLEAN_RESULT)) {
                        return Boolean.valueOf(bundle.getBoolean(AccountManager.KEY_BOOLEAN_RESULT));
                    }
                    throw new AuthenticatorException("no result in response");
                }
            }.start();
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
