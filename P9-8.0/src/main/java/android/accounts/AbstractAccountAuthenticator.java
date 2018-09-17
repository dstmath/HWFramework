package android.accounts;

import android.Manifest.permission;
import android.accounts.IAccountAuthenticator.Stub;
import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import java.util.Arrays;

public abstract class AbstractAccountAuthenticator {
    private static final String KEY_ACCOUNT = "android.accounts.AbstractAccountAuthenticator.KEY_ACCOUNT";
    private static final String KEY_AUTH_TOKEN_TYPE = "android.accounts.AbstractAccountAuthenticato.KEY_AUTH_TOKEN_TYPE";
    public static final String KEY_CUSTOM_TOKEN_EXPIRY = "android.accounts.expiry";
    private static final String KEY_OPTIONS = "android.accounts.AbstractAccountAuthenticator.KEY_OPTIONS";
    private static final String KEY_REQUIRED_FEATURES = "android.accounts.AbstractAccountAuthenticator.KEY_REQUIRED_FEATURES";
    private static final String TAG = "AccountAuthenticator";
    private final Context mContext;
    private Transport mTransport = new Transport(this, null);

    private class Transport extends Stub {
        /* synthetic */ Transport(AbstractAccountAuthenticator this$0, Transport -this1) {
            this();
        }

        private Transport() {
        }

        public void addAccount(IAccountAuthenticatorResponse response, String accountType, String authTokenType, String[] features, Bundle options) throws RemoteException {
            if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                Log.v(AbstractAccountAuthenticator.TAG, "addAccount: accountType " + accountType + ", authTokenType " + authTokenType + ", features " + (features == null ? "[]" : Arrays.toString(features)));
            }
            AbstractAccountAuthenticator.this.checkBinderPermission();
            try {
                Bundle result = AbstractAccountAuthenticator.this.addAccount(new AccountAuthenticatorResponse(response), accountType, authTokenType, features, options);
                if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                    if (result != null) {
                        result.keySet();
                    }
                    Log.v(AbstractAccountAuthenticator.TAG, "addAccount: result " + AccountManager.sanitizeResult(result));
                }
                if (result != null) {
                    response.onResult(result);
                }
            } catch (Exception e) {
                AbstractAccountAuthenticator.this.handleException(response, "addAccount", accountType, e);
            }
        }

        public void confirmCredentials(IAccountAuthenticatorResponse response, Account account, Bundle options) throws RemoteException {
            if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                Log.v(AbstractAccountAuthenticator.TAG, "confirmCredentials: " + account);
            }
            AbstractAccountAuthenticator.this.checkBinderPermission();
            try {
                Bundle result = AbstractAccountAuthenticator.this.confirmCredentials(new AccountAuthenticatorResponse(response), account, options);
                if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                    if (result != null) {
                        result.keySet();
                    }
                    Log.v(AbstractAccountAuthenticator.TAG, "confirmCredentials: result " + AccountManager.sanitizeResult(result));
                }
                if (result != null) {
                    response.onResult(result);
                }
            } catch (Exception e) {
                AbstractAccountAuthenticator.this.handleException(response, "confirmCredentials", account.toString(), e);
            }
        }

        public void getAuthTokenLabel(IAccountAuthenticatorResponse response, String authTokenType) throws RemoteException {
            if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                Log.v(AbstractAccountAuthenticator.TAG, "getAuthTokenLabel: authTokenType " + authTokenType);
            }
            AbstractAccountAuthenticator.this.checkBinderPermission();
            try {
                Bundle result = new Bundle();
                result.putString(AccountManager.KEY_AUTH_TOKEN_LABEL, AbstractAccountAuthenticator.this.getAuthTokenLabel(authTokenType));
                if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                    if (result != null) {
                        result.keySet();
                    }
                    Log.v(AbstractAccountAuthenticator.TAG, "getAuthTokenLabel: result " + AccountManager.sanitizeResult(result));
                }
                response.onResult(result);
            } catch (Exception e) {
                AbstractAccountAuthenticator.this.handleException(response, "getAuthTokenLabel", authTokenType, e);
            }
        }

        public void getAuthToken(IAccountAuthenticatorResponse response, Account account, String authTokenType, Bundle loginOptions) throws RemoteException {
            if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                Log.v(AbstractAccountAuthenticator.TAG, "getAuthToken: " + account + ", authTokenType " + authTokenType);
            }
            AbstractAccountAuthenticator.this.checkBinderPermission();
            try {
                Bundle result = AbstractAccountAuthenticator.this.getAuthToken(new AccountAuthenticatorResponse(response), account, authTokenType, loginOptions);
                if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                    if (result != null) {
                        result.keySet();
                    }
                    Log.v(AbstractAccountAuthenticator.TAG, "getAuthToken: result " + AccountManager.sanitizeResult(result));
                }
                if (result != null) {
                    response.onResult(result);
                }
            } catch (Exception e) {
                AbstractAccountAuthenticator.this.handleException(response, "getAuthToken", account.toString() + "," + authTokenType, e);
            }
        }

        public void updateCredentials(IAccountAuthenticatorResponse response, Account account, String authTokenType, Bundle loginOptions) throws RemoteException {
            if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                Log.v(AbstractAccountAuthenticator.TAG, "updateCredentials: " + account + ", authTokenType " + authTokenType);
            }
            AbstractAccountAuthenticator.this.checkBinderPermission();
            try {
                Bundle result = AbstractAccountAuthenticator.this.updateCredentials(new AccountAuthenticatorResponse(response), account, authTokenType, loginOptions);
                if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                    if (result != null) {
                        result.keySet();
                    }
                    Log.v(AbstractAccountAuthenticator.TAG, "updateCredentials: result " + AccountManager.sanitizeResult(result));
                }
                if (result != null) {
                    response.onResult(result);
                }
            } catch (Exception e) {
                AbstractAccountAuthenticator.this.handleException(response, "updateCredentials", account.toString() + "," + authTokenType, e);
            }
        }

        public void editProperties(IAccountAuthenticatorResponse response, String accountType) throws RemoteException {
            AbstractAccountAuthenticator.this.checkBinderPermission();
            try {
                Bundle result = AbstractAccountAuthenticator.this.editProperties(new AccountAuthenticatorResponse(response), accountType);
                if (result != null) {
                    response.onResult(result);
                }
            } catch (Exception e) {
                AbstractAccountAuthenticator.this.handleException(response, "editProperties", accountType, e);
            }
        }

        public void hasFeatures(IAccountAuthenticatorResponse response, Account account, String[] features) throws RemoteException {
            AbstractAccountAuthenticator.this.checkBinderPermission();
            try {
                Bundle result = AbstractAccountAuthenticator.this.hasFeatures(new AccountAuthenticatorResponse(response), account, features);
                if (result != null) {
                    response.onResult(result);
                }
            } catch (Exception e) {
                AbstractAccountAuthenticator.this.handleException(response, "hasFeatures", account.toString(), e);
            }
        }

        public void getAccountRemovalAllowed(IAccountAuthenticatorResponse response, Account account) throws RemoteException {
            AbstractAccountAuthenticator.this.checkBinderPermission();
            try {
                Bundle result = AbstractAccountAuthenticator.this.getAccountRemovalAllowed(new AccountAuthenticatorResponse(response), account);
                if (result != null) {
                    response.onResult(result);
                }
            } catch (Exception e) {
                AbstractAccountAuthenticator.this.handleException(response, "getAccountRemovalAllowed", account.toString(), e);
            }
        }

        public void getAccountCredentialsForCloning(IAccountAuthenticatorResponse response, Account account) throws RemoteException {
            AbstractAccountAuthenticator.this.checkBinderPermission();
            try {
                Bundle result = AbstractAccountAuthenticator.this.getAccountCredentialsForCloning(new AccountAuthenticatorResponse(response), account);
                if (result != null) {
                    response.onResult(result);
                }
            } catch (Exception e) {
                AbstractAccountAuthenticator.this.handleException(response, "getAccountCredentialsForCloning", account.toString(), e);
            }
        }

        public void addAccountFromCredentials(IAccountAuthenticatorResponse response, Account account, Bundle accountCredentials) throws RemoteException {
            AbstractAccountAuthenticator.this.checkBinderPermission();
            try {
                Bundle result = AbstractAccountAuthenticator.this.addAccountFromCredentials(new AccountAuthenticatorResponse(response), account, accountCredentials);
                if (result != null) {
                    response.onResult(result);
                }
            } catch (Exception e) {
                AbstractAccountAuthenticator.this.handleException(response, "addAccountFromCredentials", account.toString(), e);
            }
        }

        public void startAddAccountSession(IAccountAuthenticatorResponse response, String accountType, String authTokenType, String[] features, Bundle options) throws RemoteException {
            if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                Log.v(AbstractAccountAuthenticator.TAG, "startAddAccountSession: accountType " + accountType + ", authTokenType " + authTokenType + ", features " + (features == null ? "[]" : Arrays.toString(features)));
            }
            AbstractAccountAuthenticator.this.checkBinderPermission();
            try {
                Bundle result = AbstractAccountAuthenticator.this.startAddAccountSession(new AccountAuthenticatorResponse(response), accountType, authTokenType, features, options);
                if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                    if (result != null) {
                        result.keySet();
                    }
                    Log.v(AbstractAccountAuthenticator.TAG, "startAddAccountSession: result " + AccountManager.sanitizeResult(result));
                }
                if (result != null) {
                    response.onResult(result);
                }
            } catch (Exception e) {
                AbstractAccountAuthenticator.this.handleException(response, "startAddAccountSession", accountType, e);
            }
        }

        public void startUpdateCredentialsSession(IAccountAuthenticatorResponse response, Account account, String authTokenType, Bundle loginOptions) throws RemoteException {
            if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                Log.v(AbstractAccountAuthenticator.TAG, "startUpdateCredentialsSession: " + account + ", authTokenType " + authTokenType);
            }
            AbstractAccountAuthenticator.this.checkBinderPermission();
            try {
                Bundle result = AbstractAccountAuthenticator.this.startUpdateCredentialsSession(new AccountAuthenticatorResponse(response), account, authTokenType, loginOptions);
                if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                    if (result != null) {
                        result.keySet();
                    }
                    Log.v(AbstractAccountAuthenticator.TAG, "startUpdateCredentialsSession: result " + AccountManager.sanitizeResult(result));
                }
                if (result != null) {
                    response.onResult(result);
                }
            } catch (Exception e) {
                AbstractAccountAuthenticator.this.handleException(response, "startUpdateCredentialsSession", account.toString() + "," + authTokenType, e);
            }
        }

        public void finishSession(IAccountAuthenticatorResponse response, String accountType, Bundle sessionBundle) throws RemoteException {
            if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                Log.v(AbstractAccountAuthenticator.TAG, "finishSession: accountType " + accountType);
            }
            AbstractAccountAuthenticator.this.checkBinderPermission();
            try {
                Bundle result = AbstractAccountAuthenticator.this.finishSession(new AccountAuthenticatorResponse(response), accountType, sessionBundle);
                if (result != null) {
                    result.keySet();
                }
                if (Log.isLoggable(AbstractAccountAuthenticator.TAG, 2)) {
                    Log.v(AbstractAccountAuthenticator.TAG, "finishSession: result " + AccountManager.sanitizeResult(result));
                }
                if (result != null) {
                    response.onResult(result);
                }
            } catch (Exception e) {
                AbstractAccountAuthenticator.this.handleException(response, "finishSession", accountType, e);
            }
        }

        public void isCredentialsUpdateSuggested(IAccountAuthenticatorResponse response, Account account, String statusToken) throws RemoteException {
            AbstractAccountAuthenticator.this.checkBinderPermission();
            try {
                Bundle result = AbstractAccountAuthenticator.this.isCredentialsUpdateSuggested(new AccountAuthenticatorResponse(response), account, statusToken);
                if (result != null) {
                    response.onResult(result);
                }
            } catch (Exception e) {
                AbstractAccountAuthenticator.this.handleException(response, "isCredentialsUpdateSuggested", account.toString(), e);
            }
        }
    }

    public abstract Bundle addAccount(AccountAuthenticatorResponse accountAuthenticatorResponse, String str, String str2, String[] strArr, Bundle bundle) throws NetworkErrorException;

    public abstract Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException;

    public abstract Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String str);

    public abstract Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String str, Bundle bundle) throws NetworkErrorException;

    public abstract String getAuthTokenLabel(String str);

    public abstract Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strArr) throws NetworkErrorException;

    public abstract Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String str, Bundle bundle) throws NetworkErrorException;

    public AbstractAccountAuthenticator(Context context) {
        this.mContext = context;
    }

    private void handleException(IAccountAuthenticatorResponse response, String method, String data, Exception e) throws RemoteException {
        if (e instanceof NetworkErrorException) {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, method + "(" + data + ")", e);
            }
            response.onError(3, e.getMessage());
        } else if (e instanceof UnsupportedOperationException) {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, method + "(" + data + ")", e);
            }
            response.onError(6, method + " not supported");
        } else if (e instanceof IllegalArgumentException) {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, method + "(" + data + ")", e);
            }
            response.onError(7, method + " not supported");
        } else {
            Log.w(TAG, method + "(" + data + ")", e);
            response.onError(1, method + " failed");
        }
    }

    private void checkBinderPermission() {
        int uid = Binder.getCallingUid();
        String perm = permission.ACCOUNT_MANAGER;
        if (this.mContext.checkCallingOrSelfPermission(permission.ACCOUNT_MANAGER) != 0) {
            throw new SecurityException("caller uid " + uid + " lacks " + permission.ACCOUNT_MANAGER);
        }
    }

    public final IBinder getIBinder() {
        return this.mTransport.asBinder();
    }

    public Bundle getAccountRemovalAllowed(AccountAuthenticatorResponse response, Account account) throws NetworkErrorException {
        Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true);
        return result;
    }

    public Bundle getAccountCredentialsForCloning(final AccountAuthenticatorResponse response, Account account) throws NetworkErrorException {
        new Thread(new Runnable() {
            public void run() {
                Bundle result = new Bundle();
                result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
                response.onResult(result);
            }
        }).start();
        return null;
    }

    public Bundle addAccountFromCredentials(final AccountAuthenticatorResponse response, Account account, Bundle accountCredentials) throws NetworkErrorException {
        new Thread(new Runnable() {
            public void run() {
                Bundle result = new Bundle();
                result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
                response.onResult(result);
            }
        }).start();
        return null;
    }

    public Bundle startAddAccountSession(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        final String str = authTokenType;
        final String[] strArr = requiredFeatures;
        final Bundle bundle = options;
        final AccountAuthenticatorResponse accountAuthenticatorResponse = response;
        new Thread(new Runnable() {
            public void run() {
                Bundle sessionBundle = new Bundle();
                sessionBundle.putString(AbstractAccountAuthenticator.KEY_AUTH_TOKEN_TYPE, str);
                sessionBundle.putStringArray(AbstractAccountAuthenticator.KEY_REQUIRED_FEATURES, strArr);
                sessionBundle.putBundle(AbstractAccountAuthenticator.KEY_OPTIONS, bundle);
                Bundle result = new Bundle();
                result.putBundle(AccountManager.KEY_ACCOUNT_SESSION_BUNDLE, sessionBundle);
                accountAuthenticatorResponse.onResult(result);
            }
        }).start();
        return null;
    }

    public Bundle startUpdateCredentialsSession(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        final String str = authTokenType;
        final Account account2 = account;
        final Bundle bundle = options;
        final AccountAuthenticatorResponse accountAuthenticatorResponse = response;
        new Thread(new Runnable() {
            public void run() {
                Bundle sessionBundle = new Bundle();
                sessionBundle.putString(AbstractAccountAuthenticator.KEY_AUTH_TOKEN_TYPE, str);
                sessionBundle.putParcelable(AbstractAccountAuthenticator.KEY_ACCOUNT, account2);
                sessionBundle.putBundle(AbstractAccountAuthenticator.KEY_OPTIONS, bundle);
                Bundle result = new Bundle();
                result.putBundle(AccountManager.KEY_ACCOUNT_SESSION_BUNDLE, sessionBundle);
                accountAuthenticatorResponse.onResult(result);
            }
        }).start();
        return null;
    }

    public Bundle finishSession(AccountAuthenticatorResponse response, String accountType, Bundle sessionBundle) throws NetworkErrorException {
        Bundle result;
        if (TextUtils.isEmpty(accountType)) {
            Log.e(TAG, "Account type cannot be empty.");
            result = new Bundle();
            result.putInt(AccountManager.KEY_ERROR_CODE, 7);
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "accountType cannot be empty.");
            return result;
        } else if (sessionBundle == null) {
            Log.e(TAG, "Session bundle cannot be null.");
            result = new Bundle();
            result.putInt(AccountManager.KEY_ERROR_CODE, 7);
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "sessionBundle cannot be null.");
            return result;
        } else if (sessionBundle.containsKey(KEY_AUTH_TOKEN_TYPE)) {
            String authTokenType = sessionBundle.getString(KEY_AUTH_TOKEN_TYPE);
            Bundle options = sessionBundle.getBundle(KEY_OPTIONS);
            String[] requiredFeatures = sessionBundle.getStringArray(KEY_REQUIRED_FEATURES);
            Account account = (Account) sessionBundle.getParcelable(KEY_ACCOUNT);
            boolean containsKeyAccount = sessionBundle.containsKey(KEY_ACCOUNT);
            Bundle sessionOptions = new Bundle(sessionBundle);
            sessionOptions.remove(KEY_AUTH_TOKEN_TYPE);
            sessionOptions.remove(KEY_REQUIRED_FEATURES);
            sessionOptions.remove(KEY_OPTIONS);
            sessionOptions.remove(KEY_ACCOUNT);
            if (options != null) {
                options.putAll(sessionOptions);
                sessionOptions = options;
            }
            if (containsKeyAccount) {
                return updateCredentials(response, account, authTokenType, options);
            }
            return addAccount(response, accountType, authTokenType, requiredFeatures, sessionOptions);
        } else {
            result = new Bundle();
            result.putInt(AccountManager.KEY_ERROR_CODE, 6);
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "Authenticator must override finishSession if startAddAccountSession or startUpdateCredentialsSession is overridden.");
            response.onResult(result);
            return result;
        }
    }

    public Bundle isCredentialsUpdateSuggested(AccountAuthenticatorResponse response, Account account, String statusToken) throws NetworkErrorException {
        Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return result;
    }
}
