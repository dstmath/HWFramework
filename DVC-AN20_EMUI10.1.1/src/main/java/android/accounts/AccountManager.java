package android.accounts;

import android.accounts.IAccountManagerResponse;
import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
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
        /* class android.accounts.AccountManager.AnonymousClass20 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Account[] accounts = AccountManager.this.getAccounts();
            synchronized (AccountManager.this.mAccountsUpdatedListeners) {
                for (Map.Entry<OnAccountsUpdateListener, Handler> entry : AccountManager.this.mAccountsUpdatedListeners.entrySet()) {
                    AccountManager.this.postToHandler((AccountManager) entry.getValue(), (Handler) entry.getKey(), (OnAccountsUpdateListener) accounts);
                }
            }
        }
    };
    private final HashMap<OnAccountsUpdateListener, Handler> mAccountsUpdatedListeners = Maps.newHashMap();
    private final HashMap<OnAccountsUpdateListener, Set<String>> mAccountsUpdatedListenersTypes = Maps.newHashMap();
    @UnsupportedAppUsage
    private final Context mContext;
    private final Handler mMainHandler;
    private final IAccountManager mService;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AccountVisibility {
    }

    @UnsupportedAppUsage
    public AccountManager(Context context, IAccountManager service) {
        this.mContext = context;
        this.mService = service;
        this.mMainHandler = new Handler(this.mContext.getMainLooper());
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
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

    public AccountManagerFuture<String> getAuthTokenLabel(final String accountType, final String authTokenType, AccountManagerCallback<String> callback, Handler handler) {
        if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        } else if (authTokenType != null) {
            return new Future2Task<String>(handler, callback) {
                /* class android.accounts.AccountManager.AnonymousClass1 */

                @Override // android.accounts.AccountManager.BaseFutureTask
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.getAuthTokenLabel(this.mResponse, accountType, authTokenType);
                }

                @Override // android.accounts.AccountManager.BaseFutureTask
                public String bundleToResult(Bundle bundle) throws AuthenticatorException {
                    if (bundle.containsKey(AccountManager.KEY_AUTH_TOKEN_LABEL)) {
                        return bundle.getString(AccountManager.KEY_AUTH_TOKEN_LABEL);
                    }
                    throw new AuthenticatorException("no result in response");
                }
            }.start();
        } else {
            throw new IllegalArgumentException("authTokenType is null");
        }
    }

    public AccountManagerFuture<Boolean> hasFeatures(final Account account, final String[] features, AccountManagerCallback<Boolean> callback, Handler handler) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (features != null) {
            return new Future2Task<Boolean>(handler, callback) {
                /* class android.accounts.AccountManager.AnonymousClass2 */

                @Override // android.accounts.AccountManager.BaseFutureTask
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.hasFeatures(this.mResponse, account, features, AccountManager.this.mContext.getOpPackageName());
                }

                @Override // android.accounts.AccountManager.BaseFutureTask
                public Boolean bundleToResult(Bundle bundle) throws AuthenticatorException {
                    if (bundle.containsKey(AccountManager.KEY_BOOLEAN_RESULT)) {
                        return Boolean.valueOf(bundle.getBoolean(AccountManager.KEY_BOOLEAN_RESULT));
                    }
                    throw new AuthenticatorException("no result in response");
                }
            }.start();
        } else {
            throw new IllegalArgumentException("features is null");
        }
    }

    public AccountManagerFuture<Account[]> getAccountsByTypeAndFeatures(final String type, final String[] features, AccountManagerCallback<Account[]> callback, Handler handler) {
        if (type != null) {
            return new Future2Task<Account[]>(handler, callback) {
                /* class android.accounts.AccountManager.AnonymousClass3 */

                @Override // android.accounts.AccountManager.BaseFutureTask
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.getAccountsByFeatures(this.mResponse, type, features, AccountManager.this.mContext.getOpPackageName());
                }

                @Override // android.accounts.AccountManager.BaseFutureTask
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

    public AccountManagerFuture<Account> renameAccount(final Account account, final String newName, AccountManagerCallback<Account> callback, Handler handler) {
        if (account == null) {
            throw new IllegalArgumentException("account is null.");
        } else if (!TextUtils.isEmpty(newName)) {
            return new Future2Task<Account>(handler, callback) {
                /* class android.accounts.AccountManager.AnonymousClass4 */

                @Override // android.accounts.AccountManager.BaseFutureTask
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.renameAccount(this.mResponse, account, newName);
                }

                @Override // android.accounts.AccountManager.BaseFutureTask
                public Account bundleToResult(Bundle bundle) throws AuthenticatorException {
                    return new Account(bundle.getString(AccountManager.KEY_ACCOUNT_NAME), bundle.getString("accountType"), bundle.getString(AccountManager.KEY_ACCOUNT_ACCESS_ID));
                }
            }.start();
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
                /* class android.accounts.AccountManager.AnonymousClass5 */

                @Override // android.accounts.AccountManager.BaseFutureTask
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.removeAccount(this.mResponse, account, false);
                }

                @Override // android.accounts.AccountManager.BaseFutureTask
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

    public AccountManagerFuture<Bundle> removeAccount(final Account account, final Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (account != null) {
            return new AmsTask(handler, callback, activity) {
                /* class android.accounts.AccountManager.AnonymousClass6 */

                @Override // android.accounts.AccountManager.AmsTask
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.removeAccount(this.mResponse, account, activity != null);
                }
            }.start();
        }
        throw new IllegalArgumentException("account is null");
    }

    @Deprecated
    public AccountManagerFuture<Boolean> removeAccountAsUser(final Account account, AccountManagerCallback<Boolean> callback, Handler handler, final UserHandle userHandle) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (userHandle != null) {
            return new Future2Task<Boolean>(handler, callback) {
                /* class android.accounts.AccountManager.AnonymousClass7 */

                @Override // android.accounts.AccountManager.BaseFutureTask
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.removeAccountAsUser(this.mResponse, account, false, userHandle.getIdentifier());
                }

                @Override // android.accounts.AccountManager.BaseFutureTask
                public Boolean bundleToResult(Bundle bundle) throws AuthenticatorException {
                    if (bundle.containsKey(AccountManager.KEY_BOOLEAN_RESULT)) {
                        return Boolean.valueOf(bundle.getBoolean(AccountManager.KEY_BOOLEAN_RESULT));
                    }
                    throw new AuthenticatorException("no result in response");
                }
            }.start();
        } else {
            throw new IllegalArgumentException("userHandle is null");
        }
    }

    public AccountManagerFuture<Bundle> removeAccountAsUser(final Account account, final Activity activity, AccountManagerCallback<Bundle> callback, Handler handler, final UserHandle userHandle) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (userHandle != null) {
            return new AmsTask(handler, callback, activity) {
                /* class android.accounts.AccountManager.AnonymousClass8 */

                @Override // android.accounts.AccountManager.AmsTask
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.removeAccountAsUser(this.mResponse, account, activity != null, userHandle.getIdentifier());
                }
            }.start();
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

    public AccountManagerFuture<Bundle> getAuthToken(final Account account, final String authTokenType, Bundle options, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (authTokenType != null) {
            final Bundle optionsIn = new Bundle();
            if (options != null) {
                optionsIn.putAll(options);
            }
            optionsIn.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
            return new AmsTask(activity, handler, callback) {
                /* class android.accounts.AccountManager.AnonymousClass9 */

                @Override // android.accounts.AccountManager.AmsTask
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.getAuthToken(this.mResponse, account, authTokenType, false, true, optionsIn);
                }
            }.start();
        } else {
            throw new IllegalArgumentException("authTokenType is null");
        }
    }

    @Deprecated
    public AccountManagerFuture<Bundle> getAuthToken(Account account, String authTokenType, boolean notifyAuthFailure, AccountManagerCallback<Bundle> callback, Handler handler) {
        return getAuthToken(account, authTokenType, (Bundle) null, notifyAuthFailure, callback, handler);
    }

    public AccountManagerFuture<Bundle> getAuthToken(final Account account, final String authTokenType, Bundle options, final boolean notifyAuthFailure, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (authTokenType != null) {
            final Bundle optionsIn = new Bundle();
            if (options != null) {
                optionsIn.putAll(options);
            }
            optionsIn.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
            return new AmsTask(null, handler, callback) {
                /* class android.accounts.AccountManager.AnonymousClass10 */

                @Override // android.accounts.AccountManager.AmsTask
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.getAuthToken(this.mResponse, account, authTokenType, notifyAuthFailure, false, optionsIn);
                }
            }.start();
        } else {
            throw new IllegalArgumentException("authTokenType is null");
        }
    }

    public AccountManagerFuture<Bundle> addAccount(final String accountType, final String authTokenType, final String[] requiredFeatures, Bundle addAccountOptions, final Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (accountType != null) {
            final Bundle optionsIn = new Bundle();
            if (addAccountOptions != null) {
                optionsIn.putAll(addAccountOptions);
            }
            optionsIn.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
            return new AmsTask(handler, callback, activity) {
                /* class android.accounts.AccountManager.AnonymousClass11 */

                @Override // android.accounts.AccountManager.AmsTask
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.addAccount(this.mResponse, accountType, authTokenType, requiredFeatures, activity != null, optionsIn);
                }
            }.start();
        }
        throw new IllegalArgumentException("accountType is null");
    }

    public AccountManagerFuture<Bundle> addAccountAsUser(final String accountType, final String authTokenType, final String[] requiredFeatures, Bundle addAccountOptions, final Activity activity, AccountManagerCallback<Bundle> callback, Handler handler, final UserHandle userHandle) {
        if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        } else if (userHandle != null) {
            final Bundle optionsIn = new Bundle();
            if (addAccountOptions != null) {
                optionsIn.putAll(addAccountOptions);
            }
            optionsIn.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
            return new AmsTask(handler, callback, activity) {
                /* class android.accounts.AccountManager.AnonymousClass12 */

                @Override // android.accounts.AccountManager.AmsTask
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.addAccountAsUser(this.mResponse, accountType, authTokenType, requiredFeatures, activity != null, optionsIn, userHandle.getIdentifier());
                }
            }.start();
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

    public AccountManagerFuture<Boolean> copyAccountToUser(final Account account, final UserHandle fromUser, final UserHandle toUser, AccountManagerCallback<Boolean> callback, Handler handler) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (toUser != null && fromUser != null) {
            return new Future2Task<Boolean>(handler, callback) {
                /* class android.accounts.AccountManager.AnonymousClass13 */

                @Override // android.accounts.AccountManager.BaseFutureTask
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.copyAccountToUser(this.mResponse, account, fromUser.getIdentifier(), toUser.getIdentifier());
                }

                @Override // android.accounts.AccountManager.BaseFutureTask
                public Boolean bundleToResult(Bundle bundle) throws AuthenticatorException {
                    if (bundle.containsKey(AccountManager.KEY_BOOLEAN_RESULT)) {
                        return Boolean.valueOf(bundle.getBoolean(AccountManager.KEY_BOOLEAN_RESULT));
                    }
                    throw new AuthenticatorException("no result in response");
                }
            }.start();
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
        return confirmCredentialsAsUser(account, options, activity, callback, handler, this.mContext.getUser());
    }

    @UnsupportedAppUsage
    public AccountManagerFuture<Bundle> confirmCredentialsAsUser(final Account account, final Bundle options, final Activity activity, AccountManagerCallback<Bundle> callback, Handler handler, UserHandle userHandle) {
        if (account != null) {
            final int userId = userHandle.getIdentifier();
            return new AmsTask(handler, callback, activity) {
                /* class android.accounts.AccountManager.AnonymousClass14 */

                @Override // android.accounts.AccountManager.AmsTask
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.confirmCredentialsAsUser(this.mResponse, account, options, activity != null, userId);
                }
            }.start();
        }
        throw new IllegalArgumentException("account is null");
    }

    public AccountManagerFuture<Bundle> updateCredentials(final Account account, final String authTokenType, final Bundle options, final Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (account != null) {
            return new AmsTask(handler, callback, activity) {
                /* class android.accounts.AccountManager.AnonymousClass15 */

                @Override // android.accounts.AccountManager.AmsTask
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.updateCredentials(this.mResponse, account, authTokenType, activity != null, options);
                }
            }.start();
        }
        throw new IllegalArgumentException("account is null");
    }

    public AccountManagerFuture<Bundle> editProperties(final String accountType, final Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (accountType != null) {
            return new AmsTask(handler, callback, activity) {
                /* class android.accounts.AccountManager.AnonymousClass16 */

                @Override // android.accounts.AccountManager.AmsTask
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.editProperties(this.mResponse, accountType, activity != null);
                }
            }.start();
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
    /* access modifiers changed from: public */
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void postToHandler(Handler handler, final AccountManagerCallback<Bundle> callback, final AccountManagerFuture<Bundle> future) {
        (handler == null ? this.mMainHandler : handler).post(new Runnable() {
            /* class android.accounts.AccountManager.AnonymousClass17 */

            public void run() {
                callback.run(future);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void postToHandler(Handler handler, final OnAccountsUpdateListener listener, Account[] accounts) {
        final Account[] accountsCopy = new Account[accounts.length];
        System.arraycopy(accounts, 0, accountsCopy, 0, accountsCopy.length);
        (handler == null ? this.mMainHandler : handler).post(new Runnable() {
            /* class android.accounts.AccountManager.AnonymousClass18 */

            public void run() {
                synchronized (AccountManager.this.mAccountsUpdatedListeners) {
                    try {
                        if (AccountManager.this.mAccountsUpdatedListeners.containsKey(listener)) {
                            Set<String> types = (Set) AccountManager.this.mAccountsUpdatedListenersTypes.get(listener);
                            if (types != null) {
                                ArrayList<Account> filtered = new ArrayList<>();
                                Account[] accountArr = accountsCopy;
                                for (Account account : accountArr) {
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
    public abstract class AmsTask extends FutureTask<Bundle> implements AccountManagerFuture<Bundle> {
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        final Activity mActivity;
        final AccountManagerCallback<Bundle> mCallback;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        final Handler mHandler;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        final IAccountManagerResponse mResponse = new Response();

        public abstract void doWork() throws RemoteException;

        public AmsTask(Activity activity, Handler handler, AccountManagerCallback<Bundle> callback) {
            super(new Callable<Bundle>() {
                /* class android.accounts.AccountManager.AmsTask.AnonymousClass1 */

                @Override // java.util.concurrent.Callable
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
            super.set((Object) bundle);
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

        @Override // android.accounts.AccountManagerFuture
        public Bundle getResult() throws OperationCanceledException, IOException, AuthenticatorException {
            return internalGetResult(null, null);
        }

        @Override // android.accounts.AccountManagerFuture
        public Bundle getResult(long timeout, TimeUnit unit) throws OperationCanceledException, IOException, AuthenticatorException {
            return internalGetResult(Long.valueOf(timeout), unit);
        }

        /* access modifiers changed from: protected */
        public void done() {
            AccountManagerCallback<Bundle> accountManagerCallback = this.mCallback;
            if (accountManagerCallback != null) {
                AccountManager.this.postToHandler((AccountManager) this.mHandler, (Handler) accountManagerCallback, (AccountManagerCallback) this);
            }
        }

        private class Response extends IAccountManagerResponse.Stub {
            private Response() {
            }

            @Override // android.accounts.IAccountManagerResponse
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

            @Override // android.accounts.IAccountManagerResponse
            public void onError(int code, String message) {
                if (code == 4 || code == 100 || code == 101) {
                    AmsTask.this.cancel(true);
                    return;
                }
                AmsTask amsTask = AmsTask.this;
                amsTask.setException(AccountManager.this.convertErrorToException(code, message));
            }
        }
    }

    /* access modifiers changed from: private */
    public abstract class BaseFutureTask<T> extends FutureTask<T> {
        final Handler mHandler;
        public final IAccountManagerResponse mResponse = new Response();

        public abstract T bundleToResult(Bundle bundle) throws AuthenticatorException;

        public abstract void doWork() throws RemoteException;

        public BaseFutureTask(Handler handler) {
            super(new Callable<T>() {
                /* class android.accounts.AccountManager.BaseFutureTask.AnonymousClass1 */

                @Override // java.util.concurrent.Callable
                public T call() throws Exception {
                    throw new IllegalStateException("this should never be called");
                }
            });
            this.mHandler = handler;
        }

        /* access modifiers changed from: protected */
        public void postRunnableToHandler(Runnable runnable) {
            Handler handler = this.mHandler;
            if (handler == null) {
                handler = AccountManager.this.mMainHandler;
            }
            handler.post(runnable);
        }

        /* access modifiers changed from: protected */
        public void startTask() {
            try {
                doWork();
            } catch (RemoteException e) {
                setException(e);
            }
        }

        protected class Response extends IAccountManagerResponse.Stub {
            protected Response() {
            }

            @Override // android.accounts.IAccountManagerResponse
            public void onResult(Bundle bundle) {
                try {
                    Object bundleToResult = BaseFutureTask.this.bundleToResult(bundle);
                    if (bundleToResult != null) {
                        BaseFutureTask.this.set(bundleToResult);
                    }
                } catch (AuthenticatorException | ClassCastException e) {
                    onError(5, "no result in response");
                }
            }

            @Override // android.accounts.IAccountManagerResponse
            public void onError(int code, String message) {
                if (code == 4 || code == 100 || code == 101) {
                    BaseFutureTask.this.cancel(true);
                    return;
                }
                BaseFutureTask baseFutureTask = BaseFutureTask.this;
                baseFutureTask.setException(AccountManager.this.convertErrorToException(code, message));
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
                    /* class android.accounts.AccountManager.Future2Task.AnonymousClass1 */

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
                    T t = (T) get();
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
                T t2 = (T) get(timeout.longValue(), unit);
                cancel(true);
                return t2;
            }
        }

        @Override // android.accounts.AccountManagerFuture
        public T getResult() throws OperationCanceledException, IOException, AuthenticatorException {
            return internalGetResult(null, null);
        }

        @Override // android.accounts.AccountManagerFuture
        public T getResult(long timeout, TimeUnit unit) throws OperationCanceledException, IOException, AuthenticatorException {
            return internalGetResult(Long.valueOf(timeout), unit);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void getAccountByTypeAndFeatures(final String accountType, final String[] features, AccountManagerCallback<Bundle> callback, Handler handler) {
        new AmsTask(null, handler, callback) {
            /* class android.accounts.AccountManager.AnonymousClass19 */

            @Override // android.accounts.AccountManager.AmsTask
            public void doWork() throws RemoteException {
                AccountManager.this.mService.getAccountByTypeAndFeatures(this.mResponse, accountType, features, AccountManager.this.mContext.getOpPackageName());
            }
        }.start();
    }

    private class GetAuthTokenByTypeAndFeaturesTask extends AmsTask implements AccountManagerCallback<Bundle> {
        final String mAccountType;
        final Bundle mAddAccountOptions;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        final String mAuthTokenType;
        final String[] mFeatures;
        volatile AccountManagerFuture<Bundle> mFuture = null;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        final Bundle mLoginOptions;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        final AccountManagerCallback<Bundle> mMyCallback;
        private volatile int mNumAccounts = 0;

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

        @Override // android.accounts.AccountManager.AmsTask
        public void doWork() throws RemoteException {
            AccountManager.this.getAccountByTypeAndFeatures(this.mAccountType, this.mFeatures, new AccountManagerCallback<Bundle>() {
                /* class android.accounts.AccountManager.GetAuthTokenByTypeAndFeaturesTask.AnonymousClass1 */

                @Override // android.accounts.AccountManagerCallback
                public void run(AccountManagerFuture<Bundle> future) {
                    try {
                        Bundle result = future.getResult();
                        String accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME);
                        String accountType = result.getString("accountType");
                        if (accountName != null) {
                            GetAuthTokenByTypeAndFeaturesTask.this.mNumAccounts = 1;
                            Account account = new Account(accountName, accountType);
                            if (GetAuthTokenByTypeAndFeaturesTask.this.mActivity == null) {
                                GetAuthTokenByTypeAndFeaturesTask getAuthTokenByTypeAndFeaturesTask = GetAuthTokenByTypeAndFeaturesTask.this;
                                getAuthTokenByTypeAndFeaturesTask.mFuture = AccountManager.this.getAuthToken(account, GetAuthTokenByTypeAndFeaturesTask.this.mAuthTokenType, false, GetAuthTokenByTypeAndFeaturesTask.this.mMyCallback, GetAuthTokenByTypeAndFeaturesTask.this.mHandler);
                                return;
                            }
                            GetAuthTokenByTypeAndFeaturesTask getAuthTokenByTypeAndFeaturesTask2 = GetAuthTokenByTypeAndFeaturesTask.this;
                            getAuthTokenByTypeAndFeaturesTask2.mFuture = AccountManager.this.getAuthToken(account, GetAuthTokenByTypeAndFeaturesTask.this.mAuthTokenType, GetAuthTokenByTypeAndFeaturesTask.this.mLoginOptions, GetAuthTokenByTypeAndFeaturesTask.this.mActivity, GetAuthTokenByTypeAndFeaturesTask.this.mMyCallback, GetAuthTokenByTypeAndFeaturesTask.this.mHandler);
                        } else if (GetAuthTokenByTypeAndFeaturesTask.this.mActivity != null) {
                            GetAuthTokenByTypeAndFeaturesTask getAuthTokenByTypeAndFeaturesTask3 = GetAuthTokenByTypeAndFeaturesTask.this;
                            getAuthTokenByTypeAndFeaturesTask3.mFuture = AccountManager.this.addAccount(GetAuthTokenByTypeAndFeaturesTask.this.mAccountType, GetAuthTokenByTypeAndFeaturesTask.this.mAuthTokenType, GetAuthTokenByTypeAndFeaturesTask.this.mFeatures, GetAuthTokenByTypeAndFeaturesTask.this.mAddAccountOptions, GetAuthTokenByTypeAndFeaturesTask.this.mActivity, GetAuthTokenByTypeAndFeaturesTask.this.mMyCallback, GetAuthTokenByTypeAndFeaturesTask.this.mHandler);
                        } else {
                            Bundle result2 = new Bundle();
                            result2.putString(AccountManager.KEY_ACCOUNT_NAME, null);
                            result2.putString("accountType", null);
                            result2.putString(AccountManager.KEY_AUTHTOKEN, null);
                            result2.putBinder(AccountManager.KEY_ACCOUNT_ACCESS_ID, null);
                            try {
                                GetAuthTokenByTypeAndFeaturesTask.this.mResponse.onResult(result2);
                            } catch (RemoteException e) {
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

        @Override // android.accounts.AccountManagerCallback
        public void run(AccountManagerFuture<Bundle> future) {
            try {
                Bundle result = future.getResult();
                if (this.mNumAccounts == 0) {
                    String accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME);
                    String accountType = result.getString("accountType");
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
        intent.putExtra(ChooseTypeAndAccountActivity.EXTRA_ALLOWABLE_ACCOUNTS_ARRAYLIST, allowableAccounts == null ? null : new ArrayList(allowableAccounts));
        intent.putExtra(ChooseTypeAndAccountActivity.EXTRA_ALLOWABLE_ACCOUNT_TYPES_STRING_ARRAY, allowableAccountTypes);
        intent.putExtra(ChooseTypeAndAccountActivity.EXTRA_ADD_ACCOUNT_OPTIONS_BUNDLE, addAccountOptions);
        intent.putExtra(ChooseTypeAndAccountActivity.EXTRA_SELECTED_ACCOUNT, selectedAccount);
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

    public AccountManagerFuture<Bundle> startAddAccountSession(final String accountType, final String authTokenType, final String[] requiredFeatures, Bundle options, final Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (accountType != null) {
            final Bundle optionsIn = new Bundle();
            if (options != null) {
                optionsIn.putAll(options);
            }
            optionsIn.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
            return new AmsTask(handler, callback, activity) {
                /* class android.accounts.AccountManager.AnonymousClass21 */

                @Override // android.accounts.AccountManager.AmsTask
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.startAddAccountSession(this.mResponse, accountType, authTokenType, requiredFeatures, activity != null, optionsIn);
                }
            }.start();
        }
        throw new IllegalArgumentException("accountType is null");
    }

    public AccountManagerFuture<Bundle> startUpdateCredentialsSession(final Account account, final String authTokenType, Bundle options, final Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (account != null) {
            final Bundle optionsIn = new Bundle();
            if (options != null) {
                optionsIn.putAll(options);
            }
            optionsIn.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
            return new AmsTask(handler, callback, activity) {
                /* class android.accounts.AccountManager.AnonymousClass22 */

                @Override // android.accounts.AccountManager.AmsTask
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.startUpdateCredentialsSession(this.mResponse, account, authTokenType, activity != null, optionsIn);
                }
            }.start();
        }
        throw new IllegalArgumentException("account is null");
    }

    public AccountManagerFuture<Bundle> finishSession(Bundle sessionBundle, Activity activity, AccountManagerCallback<Bundle> callback, Handler handler) {
        return finishSessionAsUser(sessionBundle, activity, this.mContext.getUser(), callback, handler);
    }

    @SystemApi
    public AccountManagerFuture<Bundle> finishSessionAsUser(final Bundle sessionBundle, final Activity activity, final UserHandle userHandle, AccountManagerCallback<Bundle> callback, Handler handler) {
        if (sessionBundle != null) {
            final Bundle appInfo = new Bundle();
            appInfo.putString(KEY_ANDROID_PACKAGE_NAME, this.mContext.getPackageName());
            return new AmsTask(handler, callback, activity) {
                /* class android.accounts.AccountManager.AnonymousClass23 */

                @Override // android.accounts.AccountManager.AmsTask
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.finishSessionAsUser(this.mResponse, sessionBundle, activity != null, appInfo, userHandle.getIdentifier());
                }
            }.start();
        }
        throw new IllegalArgumentException("sessionBundle is null");
    }

    public AccountManagerFuture<Boolean> isCredentialsUpdateSuggested(final Account account, final String statusToken, AccountManagerCallback<Boolean> callback, Handler handler) {
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (!TextUtils.isEmpty(statusToken)) {
            return new Future2Task<Boolean>(handler, callback) {
                /* class android.accounts.AccountManager.AnonymousClass24 */

                @Override // android.accounts.AccountManager.BaseFutureTask
                public void doWork() throws RemoteException {
                    AccountManager.this.mService.isCredentialsUpdateSuggested(this.mResponse, account, statusToken);
                }

                @Override // android.accounts.AccountManager.BaseFutureTask
                public Boolean bundleToResult(Bundle bundle) throws AuthenticatorException {
                    if (bundle.containsKey(AccountManager.KEY_BOOLEAN_RESULT)) {
                        return Boolean.valueOf(bundle.getBoolean(AccountManager.KEY_BOOLEAN_RESULT));
                    }
                    throw new AuthenticatorException("no result in response");
                }
            }.start();
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
