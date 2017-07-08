package com.android.server.accounts;

import android.accounts.Account;
import android.accounts.AccountAndUser;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AuthenticatorDescription;
import android.accounts.CantAddAccountActivity;
import android.accounts.GrantCredentialsPermissionActivity;
import android.accounts.IAccountAuthenticator;
import android.accounts.IAccountAuthenticatorResponse;
import android.accounts.IAccountManager.Stub;
import android.accounts.IAccountManagerResponse;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.app.admin.DevicePolicyManagerInternal;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.RegisteredServicesCache.ServiceInfo;
import android.content.pm.RegisteredServicesCacheListener;
import android.content.pm.Signature;
import android.content.pm.UserInfo;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import com.android.internal.os.HwBootFail;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.server.FgThread;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.am.ProcessList;
import com.android.server.wm.WindowManagerService.H;
import com.google.android.collect.Lists;
import com.google.android.collect.Sets;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AccountManagerService extends Stub implements RegisteredServicesCacheListener<AuthenticatorDescription> {
    private static final Intent ACCOUNTS_CHANGED_INTENT = null;
    private static final String ACCOUNTS_ID = "_id";
    private static final String ACCOUNTS_LAST_AUTHENTICATE_TIME_EPOCH_MILLIS = "last_password_entry_time_millis_epoch";
    private static final String ACCOUNTS_NAME = "name";
    private static final String ACCOUNTS_PASSWORD = "password";
    private static final String ACCOUNTS_PREVIOUS_NAME = "previous_name";
    private static final String ACCOUNTS_TYPE = "type";
    private static final String ACCOUNTS_TYPE_COUNT = "count(type)";
    private static final String[] ACCOUNT_TYPE_COUNT_PROJECTION = null;
    private static final String AUTHTOKENS_ACCOUNTS_ID = "accounts_id";
    private static final String AUTHTOKENS_AUTHTOKEN = "authtoken";
    private static final String AUTHTOKENS_ID = "_id";
    private static final String AUTHTOKENS_TYPE = "type";
    private static final String CE_DATABASE_NAME = "accounts_ce.db";
    private static final int CE_DATABASE_VERSION = 10;
    private static final String CE_DB_PREFIX = "ceDb.";
    private static final String CE_TABLE_ACCOUNTS = "ceDb.accounts";
    private static final String CE_TABLE_AUTHTOKENS = "ceDb.authtokens";
    private static final String CE_TABLE_EXTRAS = "ceDb.extras";
    private static final String[] COLUMNS_AUTHTOKENS_TYPE_AND_AUTHTOKEN = null;
    private static final String[] COLUMNS_EXTRAS_KEY_AND_VALUE = null;
    private static final String COUNT_OF_MATCHING_GRANTS = "SELECT COUNT(*) FROM grants, accounts WHERE accounts_id=_id AND uid=? AND auth_token_type=? AND name=? AND type=?";
    private static final String DATABASE_NAME = "accounts.db";
    private static final String DE_DATABASE_NAME = "accounts_de.db";
    private static final int DE_DATABASE_VERSION = 1;
    private static final Account[] EMPTY_ACCOUNT_ARRAY = null;
    private static final String EXTRAS_ACCOUNTS_ID = "accounts_id";
    private static final String EXTRAS_ID = "_id";
    private static final String EXTRAS_KEY = "key";
    private static final String EXTRAS_VALUE = "value";
    private static final String GRANTS_ACCOUNTS_ID = "accounts_id";
    private static final String GRANTS_AUTH_TOKEN_TYPE = "auth_token_type";
    private static final String GRANTS_GRANTEE_UID = "uid";
    private static final int MAX_DEBUG_DB_SIZE = 64;
    private static final int MESSAGE_COPY_SHARED_ACCOUNT = 4;
    private static final int MESSAGE_TIMED_OUT = 3;
    private static final String META_KEY = "key";
    private static final String META_KEY_DELIMITER = ":";
    private static final String META_KEY_FOR_AUTHENTICATOR_UID_FOR_TYPE_PREFIX = "auth_uid_for_type:";
    private static final String META_VALUE = "value";
    private static final String PRE_N_DATABASE_NAME = "accounts.db";
    private static final int PRE_N_DATABASE_VERSION = 9;
    private static final String SELECTION_AUTHTOKENS_BY_ACCOUNT = "accounts_id=(select _id FROM accounts WHERE name=? AND type=?)";
    private static final String SELECTION_META_BY_AUTHENTICATOR_TYPE = "key LIKE ?";
    private static final String SELECTION_USERDATA_BY_ACCOUNT = "accounts_id=(select _id FROM accounts WHERE name=? AND type=?)";
    private static final String SHARED_ACCOUNTS_ID = "_id";
    private static final String TABLE_ACCOUNTS = "accounts";
    private static final String TABLE_AUTHTOKENS = "authtokens";
    private static final String TABLE_EXTRAS = "extras";
    private static final String TABLE_GRANTS = "grants";
    private static final String TABLE_META = "meta";
    private static final String TABLE_SHARED_ACCOUNTS = "shared_accounts";
    private static final String TAG = "AccountManagerService";
    private static AtomicReference<AccountManagerService> sThis;
    private final AppOpsManager mAppOpsManager;
    private final IAccountAuthenticatorCache mAuthenticatorCache;
    private final Context mContext;
    private final SparseBooleanArray mLocalUnlockedUsers;
    private final MessageHandler mMessageHandler;
    private final AtomicInteger mNotificationIds;
    private final PackageManager mPackageManager;
    private final LinkedHashMap<String, Session> mSessions;
    private UserManager mUserManager;
    private final SparseArray<UserAccounts> mUsers;

    private abstract class Session extends IAccountAuthenticatorResponse.Stub implements DeathRecipient, ServiceConnection {
        final String mAccountName;
        final String mAccountType;
        protected final UserAccounts mAccounts;
        final boolean mAuthDetailsRequired;
        IAccountAuthenticator mAuthenticator;
        final long mCreationTime;
        final boolean mExpectActivityLaunch;
        private int mNumErrors;
        private int mNumRequestContinued;
        public int mNumResults;
        IAccountManagerResponse mResponse;
        private final boolean mStripAuthTokenFromResult;
        final boolean mUpdateLastAuthenticatedTime;

        public abstract void run() throws RemoteException;

        public Session(AccountManagerService this$0, UserAccounts accounts, IAccountManagerResponse response, String accountType, boolean expectActivityLaunch, boolean stripAuthTokenFromResult, String accountName, boolean authDetailsRequired) {
            this(accounts, response, accountType, expectActivityLaunch, stripAuthTokenFromResult, accountName, authDetailsRequired, false);
        }

        public Session(UserAccounts accounts, IAccountManagerResponse response, String accountType, boolean expectActivityLaunch, boolean stripAuthTokenFromResult, String accountName, boolean authDetailsRequired, boolean updateLastAuthenticatedTime) {
            this.mNumResults = 0;
            this.mNumRequestContinued = 0;
            this.mNumErrors = 0;
            this.mAuthenticator = null;
            if (accountType == null) {
                throw new IllegalArgumentException("accountType is null");
            }
            this.mAccounts = accounts;
            this.mStripAuthTokenFromResult = stripAuthTokenFromResult;
            this.mResponse = response;
            this.mAccountType = accountType;
            this.mExpectActivityLaunch = expectActivityLaunch;
            this.mCreationTime = SystemClock.elapsedRealtime();
            this.mAccountName = accountName;
            this.mAuthDetailsRequired = authDetailsRequired;
            this.mUpdateLastAuthenticatedTime = updateLastAuthenticatedTime;
            synchronized (AccountManagerService.this.mSessions) {
                AccountManagerService.this.mSessions.put(toString(), this);
            }
            if (response != null) {
                try {
                    response.asBinder().linkToDeath(this, 0);
                } catch (RemoteException e) {
                    this.mResponse = null;
                    binderDied();
                }
            }
        }

        IAccountManagerResponse getResponseAndClose() {
            if (this.mResponse == null) {
                return null;
            }
            IAccountManagerResponse response = this.mResponse;
            close();
            return response;
        }

        protected void checkKeyIntent(int authUid, Intent intent) throws SecurityException {
            long bid = Binder.clearCallingIdentity();
            try {
                PackageManager pm = AccountManagerService.this.mContext.getPackageManager();
                ActivityInfo targetActivityInfo = pm.resolveActivityAsUser(intent, 0, this.mAccounts.userId).activityInfo;
                if (pm.checkSignatures(authUid, targetActivityInfo.applicationInfo.uid) != 0) {
                    String pkgName = targetActivityInfo.packageName;
                    Object[] objArr = new Object[AccountManagerService.MESSAGE_TIMED_OUT];
                    objArr[0] = targetActivityInfo.name;
                    objArr[AccountManagerService.DE_DATABASE_VERSION] = pkgName;
                    objArr[2] = this.mAccountType;
                    throw new SecurityException(String.format("KEY_INTENT resolved to an Activity (%s) in a package (%s) that does not share a signature with the supplying authenticator (%s).", objArr));
                }
            } finally {
                Binder.restoreCallingIdentity(bid);
            }
        }

        private void close() {
            synchronized (AccountManagerService.this.mSessions) {
                if (AccountManagerService.this.mSessions.remove(toString()) == null) {
                    return;
                }
                if (this.mResponse != null) {
                    this.mResponse.asBinder().unlinkToDeath(this, 0);
                    this.mResponse = null;
                }
                cancelTimeout();
                unbind();
            }
        }

        public void binderDied() {
            this.mResponse = null;
            close();
        }

        protected String toDebugString() {
            return toDebugString(SystemClock.elapsedRealtime());
        }

        protected String toDebugString(long now) {
            return "Session: expectLaunch " + this.mExpectActivityLaunch + ", connected " + (this.mAuthenticator != null) + ", stats (" + this.mNumResults + "/" + this.mNumRequestContinued + "/" + this.mNumErrors + ")" + ", lifetime " + (((double) (now - this.mCreationTime)) / 1000.0d);
        }

        void bind() {
            if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                Log.v(AccountManagerService.TAG, "initiating bind to authenticator type " + this.mAccountType);
            }
            if (!bindToAuthenticator(this.mAccountType)) {
                Log.d(AccountManagerService.TAG, "bind attempt failed for " + toDebugString());
                onError(AccountManagerService.DE_DATABASE_VERSION, "bind failure");
            }
        }

        private void unbind() {
            if (this.mAuthenticator != null) {
                this.mAuthenticator = null;
                AccountManagerService.this.mContext.unbindService(this);
            }
        }

        public void cancelTimeout() {
            AccountManagerService.this.mMessageHandler.removeMessages(AccountManagerService.MESSAGE_TIMED_OUT, this);
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            this.mAuthenticator = IAccountAuthenticator.Stub.asInterface(service);
            try {
                run();
            } catch (RemoteException e) {
                onError(AccountManagerService.DE_DATABASE_VERSION, "remote exception");
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            this.mAuthenticator = null;
            IAccountManagerResponse response = getResponseAndClose();
            if (response != null) {
                try {
                    response.onError(AccountManagerService.DE_DATABASE_VERSION, "disconnected");
                } catch (RemoteException e) {
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, "Session.onServiceDisconnected: caught RemoteException while responding", e);
                    }
                }
            }
        }

        public void onTimedOut() {
            IAccountManagerResponse response = getResponseAndClose();
            if (response != null) {
                try {
                    response.onError(AccountManagerService.DE_DATABASE_VERSION, "timeout");
                } catch (RemoteException e) {
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, "Session.onTimedOut: caught RemoteException while responding", e);
                    }
                }
            }
        }

        public void onResult(Bundle result) {
            IAccountManagerResponse response;
            Bundle.setDefusable(result, true);
            this.mNumResults += AccountManagerService.DE_DATABASE_VERSION;
            Intent intent = null;
            if (result != null) {
                boolean containsKey;
                boolean isSuccessfulConfirmCreds = result.getBoolean("booleanResult", false);
                if (result.containsKey("authAccount")) {
                    containsKey = result.containsKey("accountType");
                } else {
                    containsKey = false;
                }
                boolean needUpdate = this.mUpdateLastAuthenticatedTime ? !isSuccessfulConfirmCreds ? containsKey : true : false;
                if (needUpdate || this.mAuthDetailsRequired) {
                    boolean accountPresent = AccountManagerService.this.isAccountPresentForCaller(this.mAccountName, this.mAccountType);
                    if (needUpdate && accountPresent) {
                        AccountManagerService.this.updateLastAuthenticatedTime(new Account(this.mAccountName, this.mAccountType));
                    }
                    if (this.mAuthDetailsRequired) {
                        long lastAuthenticatedTime = -1;
                        if (accountPresent) {
                            r16 = new String[2];
                            r16[0] = this.mAccountName;
                            r16[AccountManagerService.DE_DATABASE_VERSION] = this.mAccountType;
                            lastAuthenticatedTime = DatabaseUtils.longForQuery(this.mAccounts.openHelper.getReadableDatabase(), "SELECT last_password_entry_time_millis_epoch FROM accounts WHERE name=? AND type=?", r16);
                        }
                        result.putLong("lastAuthenticatedTime", lastAuthenticatedTime);
                    }
                }
            }
            if (result != null) {
                intent = (Intent) result.getParcelable(HwBroadcastRadarUtil.KEY_BROADCAST_INTENT);
                if (intent != null) {
                    checkKeyIntent(Binder.getCallingUid(), intent);
                }
            }
            if (result != null) {
                if (!TextUtils.isEmpty(result.getString(AccountManagerService.AUTHTOKENS_AUTHTOKEN))) {
                    String accountName = result.getString("authAccount");
                    String accountType = result.getString("accountType");
                    if (!(TextUtils.isEmpty(accountName) || TextUtils.isEmpty(accountType))) {
                        AccountManagerService.this.cancelNotification(AccountManagerService.this.getSigninRequiredNotificationId(this.mAccounts, new Account(accountName, accountType)).intValue(), new UserHandle(this.mAccounts.userId));
                    }
                }
            }
            if (this.mExpectActivityLaunch && result != null) {
                if (result.containsKey(HwBroadcastRadarUtil.KEY_BROADCAST_INTENT)) {
                    response = this.mResponse;
                    if (response == null) {
                    }
                    if (result != null) {
                        try {
                            if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                                Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onError() on response " + response);
                            }
                            response.onError(5, "null bundle returned");
                        } catch (RemoteException e) {
                            if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                                Log.v(AccountManagerService.TAG, "failure while notifying response", e);
                                return;
                            }
                            return;
                        }
                    }
                    if (this.mStripAuthTokenFromResult) {
                        result.remove(AccountManagerService.AUTHTOKENS_AUTHTOKEN);
                    }
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
                    }
                    if (result.getInt("errorCode", -1) > 0 || r7 != null) {
                        response.onResult(result);
                        return;
                    } else {
                        response.onError(result.getInt("errorCode"), result.getString("errorMessage"));
                        return;
                    }
                }
            }
            response = getResponseAndClose();
            if (response == null) {
                if (result != null) {
                    if (this.mStripAuthTokenFromResult) {
                        result.remove(AccountManagerService.AUTHTOKENS_AUTHTOKEN);
                    }
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
                    }
                    if (result.getInt("errorCode", -1) > 0) {
                    }
                    response.onResult(result);
                    return;
                }
                if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                    Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onError() on response " + response);
                }
                response.onError(5, "null bundle returned");
            }
        }

        public void onRequestContinued() {
            this.mNumRequestContinued += AccountManagerService.DE_DATABASE_VERSION;
        }

        public void onError(int errorCode, String errorMessage) {
            this.mNumErrors += AccountManagerService.DE_DATABASE_VERSION;
            IAccountManagerResponse response = getResponseAndClose();
            if (response != null) {
                if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                    Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onError() on response " + response);
                }
                try {
                    response.onError(errorCode, errorMessage);
                } catch (RemoteException e) {
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, "Session.onError: caught RemoteException while responding", e);
                    }
                }
            } else if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                Log.v(AccountManagerService.TAG, "Session.onError: already closed");
            }
        }

        private boolean bindToAuthenticator(String authenticatorType) {
            ServiceInfo<AuthenticatorDescription> authenticatorInfo = AccountManagerService.this.mAuthenticatorCache.getServiceInfo(AuthenticatorDescription.newKey(authenticatorType), this.mAccounts.userId);
            if (authenticatorInfo == null) {
                if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                    Log.v(AccountManagerService.TAG, "there is no authenticator for " + authenticatorType + ", bailing out");
                }
                return false;
            } else if (AccountManagerService.this.isLocalUnlockedUser(this.mAccounts.userId) || authenticatorInfo.componentInfo.directBootAware) {
                Intent intent = new Intent();
                intent.setAction("android.accounts.AccountAuthenticator");
                intent.setComponent(authenticatorInfo.componentName);
                if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                    Log.v(AccountManagerService.TAG, "performing bindService to " + authenticatorInfo.componentName);
                }
                if (AccountManagerService.this.mContext.bindServiceAsUser(intent, this, AccountManagerService.DE_DATABASE_VERSION, UserHandle.of(this.mAccounts.userId))) {
                    return true;
                }
                if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                    Log.v(AccountManagerService.TAG, "bindService to " + authenticatorInfo.componentName + " failed");
                }
                return false;
            } else {
                Slog.w(AccountManagerService.TAG, "Blocking binding to authenticator " + authenticatorInfo.componentName + " which isn't encryption aware");
                return false;
            }
        }
    }

    /* renamed from: com.android.server.accounts.AccountManagerService.10 */
    class AnonymousClass10 extends Session {
        final /* synthetic */ AccountManagerService this$0;
        final /* synthetic */ String val$accountType;
        final /* synthetic */ Bundle val$decryptedBundle;

        AnonymousClass10(AccountManagerService this$0, AccountManagerService this$0_1, UserAccounts $anonymous0, IAccountManagerResponse $anonymous1, String $anonymous2, boolean $anonymous3, boolean $anonymous4, String $anonymous5, boolean $anonymous6, boolean $anonymous7, Bundle val$decryptedBundle, String val$accountType) {
            this.this$0 = this$0_1;
            this.val$decryptedBundle = val$decryptedBundle;
            this.val$accountType = val$accountType;
            super($anonymous0, $anonymous1, $anonymous2, $anonymous3, $anonymous4, $anonymous5, $anonymous6, $anonymous7);
        }

        public void run() throws RemoteException {
            this.mAuthenticator.finishSession(this, this.mAccountType, this.val$decryptedBundle);
        }

        protected String toDebugString(long now) {
            return super.toDebugString(now) + ", finishSession" + ", accountType " + this.val$accountType;
        }
    }

    /* renamed from: com.android.server.accounts.AccountManagerService.11 */
    class AnonymousClass11 extends Session {
        final /* synthetic */ AccountManagerService this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ Bundle val$options;

        AnonymousClass11(AccountManagerService this$0, AccountManagerService this$0_1, UserAccounts $anonymous0, IAccountManagerResponse $anonymous1, String $anonymous2, boolean $anonymous3, boolean $anonymous4, String $anonymous5, boolean $anonymous6, boolean $anonymous7, Account val$account, Bundle val$options) {
            this.this$0 = this$0_1;
            this.val$account = val$account;
            this.val$options = val$options;
            super($anonymous0, $anonymous1, $anonymous2, $anonymous3, $anonymous4, $anonymous5, $anonymous6, $anonymous7);
        }

        public void run() throws RemoteException {
            this.mAuthenticator.confirmCredentials(this, this.val$account, this.val$options);
        }

        protected String toDebugString(long now) {
            return super.toDebugString(now) + ", confirmCredentials" + ", " + this.val$account;
        }
    }

    /* renamed from: com.android.server.accounts.AccountManagerService.12 */
    class AnonymousClass12 extends Session {
        final /* synthetic */ AccountManagerService this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ String val$authTokenType;
        final /* synthetic */ Bundle val$loginOptions;

        AnonymousClass12(AccountManagerService this$0, AccountManagerService this$0_1, UserAccounts $anonymous0, IAccountManagerResponse $anonymous1, String $anonymous2, boolean $anonymous3, boolean $anonymous4, String $anonymous5, boolean $anonymous6, boolean $anonymous7, Account val$account, String val$authTokenType, Bundle val$loginOptions) {
            this.this$0 = this$0_1;
            this.val$account = val$account;
            this.val$authTokenType = val$authTokenType;
            this.val$loginOptions = val$loginOptions;
            super($anonymous0, $anonymous1, $anonymous2, $anonymous3, $anonymous4, $anonymous5, $anonymous6, $anonymous7);
        }

        public void run() throws RemoteException {
            this.mAuthenticator.updateCredentials(this, this.val$account, this.val$authTokenType, this.val$loginOptions);
        }

        protected String toDebugString(long now) {
            if (this.val$loginOptions != null) {
                this.val$loginOptions.keySet();
            }
            return super.toDebugString(now) + ", updateCredentials" + ", " + this.val$account + ", authTokenType " + this.val$authTokenType + ", loginOptions " + this.val$loginOptions;
        }
    }

    private abstract class StartAccountSession extends Session {
        private final boolean mIsPasswordForwardingAllowed;

        public StartAccountSession(UserAccounts accounts, IAccountManagerResponse response, String accountType, boolean expectActivityLaunch, String accountName, boolean authDetailsRequired, boolean updateLastAuthenticationTime, boolean isPasswordForwardingAllowed) {
            super(accounts, response, accountType, expectActivityLaunch, true, accountName, authDetailsRequired, updateLastAuthenticationTime);
            this.mIsPasswordForwardingAllowed = isPasswordForwardingAllowed;
        }

        public void onResult(Bundle result) {
            IAccountManagerResponse response;
            Bundle.setDefusable(result, true);
            this.mNumResults += AccountManagerService.DE_DATABASE_VERSION;
            Intent intent = null;
            if (result != null) {
                intent = (Intent) result.getParcelable(HwBroadcastRadarUtil.KEY_BROADCAST_INTENT);
                if (intent != null) {
                    checkKeyIntent(Binder.getCallingUid(), intent);
                }
            }
            if (this.mExpectActivityLaunch && result != null && result.containsKey(HwBroadcastRadarUtil.KEY_BROADCAST_INTENT)) {
                response = this.mResponse;
            } else {
                response = getResponseAndClose();
            }
            if (response != null) {
                if (result == null) {
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onError() on response " + response);
                    }
                    AccountManagerService.this.sendErrorResponse(response, 5, "null bundle returned");
                } else if (result.getInt("errorCode", -1) <= 0 || r4 != null) {
                    if (!this.mIsPasswordForwardingAllowed) {
                        result.remove(AccountManagerService.ACCOUNTS_PASSWORD);
                    }
                    result.remove(AccountManagerService.AUTHTOKENS_AUTHTOKEN);
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
                    }
                    Bundle sessionBundle = result.getBundle("accountSessionBundle");
                    if (sessionBundle != null) {
                        String accountType = sessionBundle.getString("accountType");
                        if (TextUtils.isEmpty(accountType) || !this.mAccountType.equalsIgnoreCase(accountType)) {
                            Log.w(AccountManagerService.TAG, "Account type in session bundle doesn't match request.");
                        }
                        sessionBundle.putString("accountType", this.mAccountType);
                        try {
                            result.putBundle("accountSessionBundle", CryptoHelper.getInstance().encryptBundle(sessionBundle));
                        } catch (GeneralSecurityException e) {
                            if (Log.isLoggable(AccountManagerService.TAG, AccountManagerService.MESSAGE_TIMED_OUT)) {
                                Log.v(AccountManagerService.TAG, "Failed to encrypt session bundle!", e);
                            }
                            AccountManagerService.this.sendErrorResponse(response, 5, "failed to encrypt session bundle");
                            return;
                        }
                    }
                    AccountManagerService.this.sendResponse(response, result);
                } else {
                    AccountManagerService.this.sendErrorResponse(response, result.getInt("errorCode"), result.getString("errorMessage"));
                }
            }
        }
    }

    /* renamed from: com.android.server.accounts.AccountManagerService.13 */
    class AnonymousClass13 extends StartAccountSession {
        final /* synthetic */ AccountManagerService this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ String val$authTokenType;
        final /* synthetic */ Bundle val$loginOptions;

        AnonymousClass13(AccountManagerService this$0, AccountManagerService this$0_1, UserAccounts $anonymous0, IAccountManagerResponse $anonymous1, String $anonymous2, boolean $anonymous3, String $anonymous4, boolean $anonymous5, boolean $anonymous6, boolean $anonymous7, Account val$account, String val$authTokenType, Bundle val$loginOptions) {
            this.this$0 = this$0_1;
            this.val$account = val$account;
            this.val$authTokenType = val$authTokenType;
            this.val$loginOptions = val$loginOptions;
            super($anonymous0, $anonymous1, $anonymous2, $anonymous3, $anonymous4, $anonymous5, $anonymous6, $anonymous7);
        }

        public void run() throws RemoteException {
            this.mAuthenticator.startUpdateCredentialsSession(this, this.val$account, this.val$authTokenType, this.val$loginOptions);
        }

        protected String toDebugString(long now) {
            if (this.val$loginOptions != null) {
                this.val$loginOptions.keySet();
            }
            return super.toDebugString(now) + ", startUpdateCredentialsSession" + ", " + this.val$account + ", authTokenType " + this.val$authTokenType + ", loginOptions " + this.val$loginOptions;
        }
    }

    /* renamed from: com.android.server.accounts.AccountManagerService.14 */
    class AnonymousClass14 extends Session {
        final /* synthetic */ AccountManagerService this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ String val$statusToken;

        AnonymousClass14(AccountManagerService this$0, AccountManagerService this$0_1, UserAccounts $anonymous0, IAccountManagerResponse $anonymous1, String $anonymous2, boolean $anonymous3, boolean $anonymous4, String $anonymous5, boolean $anonymous6, Account val$account, String val$statusToken) {
            this.this$0 = this$0_1;
            this.val$account = val$account;
            this.val$statusToken = val$statusToken;
            super(this$0, $anonymous0, $anonymous1, $anonymous2, $anonymous3, $anonymous4, $anonymous5, $anonymous6);
        }

        protected String toDebugString(long now) {
            return super.toDebugString(now) + ", isCredentialsUpdateSuggested" + ", " + this.val$account;
        }

        public void run() throws RemoteException {
            this.mAuthenticator.isCredentialsUpdateSuggested(this, this.val$account, this.val$statusToken);
        }

        public void onResult(Bundle result) {
            Bundle.setDefusable(result, true);
            IAccountManagerResponse response = getResponseAndClose();
            if (response != null) {
                if (result == null) {
                    this.this$0.sendErrorResponse(response, 5, "null bundle");
                    return;
                }
                if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                    Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
                }
                if (result.getInt("errorCode", -1) > 0) {
                    this.this$0.sendErrorResponse(response, result.getInt("errorCode"), result.getString("errorMessage"));
                } else if (result.containsKey("booleanResult")) {
                    Bundle newResult = new Bundle();
                    newResult.putBoolean("booleanResult", result.getBoolean("booleanResult", false));
                    this.this$0.sendResponse(response, newResult);
                } else {
                    this.this$0.sendErrorResponse(response, 5, "no result in response");
                }
            }
        }
    }

    /* renamed from: com.android.server.accounts.AccountManagerService.15 */
    class AnonymousClass15 extends Session {
        final /* synthetic */ AccountManagerService this$0;
        final /* synthetic */ String val$accountType;

        AnonymousClass15(AccountManagerService this$0, AccountManagerService this$0_1, UserAccounts $anonymous0, IAccountManagerResponse $anonymous1, String $anonymous2, boolean $anonymous3, boolean $anonymous4, String $anonymous5, boolean $anonymous6, String val$accountType) {
            this.this$0 = this$0_1;
            this.val$accountType = val$accountType;
            super(this$0, $anonymous0, $anonymous1, $anonymous2, $anonymous3, $anonymous4, $anonymous5, $anonymous6);
        }

        public void run() throws RemoteException {
            this.mAuthenticator.editProperties(this, this.mAccountType);
        }

        protected String toDebugString(long now) {
            return super.toDebugString(now) + ", editProperties" + ", accountType " + this.val$accountType;
        }
    }

    /* renamed from: com.android.server.accounts.AccountManagerService.3 */
    class AnonymousClass3 extends Session {
        final /* synthetic */ AccountManagerService this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ IAccountManagerResponse val$response;
        final /* synthetic */ UserAccounts val$toAccounts;
        final /* synthetic */ int val$userFrom;

        AnonymousClass3(AccountManagerService this$0, AccountManagerService this$0_1, UserAccounts $anonymous0, IAccountManagerResponse $anonymous1, String $anonymous2, boolean $anonymous3, boolean $anonymous4, String $anonymous5, boolean $anonymous6, Account val$account, IAccountManagerResponse val$response, UserAccounts val$toAccounts, int val$userFrom) {
            this.this$0 = this$0_1;
            this.val$account = val$account;
            this.val$response = val$response;
            this.val$toAccounts = val$toAccounts;
            this.val$userFrom = val$userFrom;
            super(this$0, $anonymous0, $anonymous1, $anonymous2, $anonymous3, $anonymous4, $anonymous5, $anonymous6);
        }

        protected String toDebugString(long now) {
            return super.toDebugString(now) + ", getAccountCredentialsForClone" + ", " + this.val$account.type;
        }

        public void run() throws RemoteException {
            this.mAuthenticator.getAccountCredentialsForCloning(this, this.val$account);
        }

        public void onResult(Bundle result) {
            Bundle.setDefusable(result, true);
            if (result == null || !result.getBoolean("booleanResult", false)) {
                super.onResult(result);
                return;
            }
            this.this$0.completeCloningAccount(this.val$response, result, this.val$account, this.val$toAccounts, this.val$userFrom);
        }
    }

    /* renamed from: com.android.server.accounts.AccountManagerService.4 */
    class AnonymousClass4 extends Session {
        final /* synthetic */ AccountManagerService this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ Bundle val$accountCredentials;
        final /* synthetic */ int val$parentUserId;

        AnonymousClass4(AccountManagerService this$0, AccountManagerService this$0_1, UserAccounts $anonymous0, IAccountManagerResponse $anonymous1, String $anonymous2, boolean $anonymous3, boolean $anonymous4, String $anonymous5, boolean $anonymous6, Account val$account, int val$parentUserId, Bundle val$accountCredentials) {
            this.this$0 = this$0_1;
            this.val$account = val$account;
            this.val$parentUserId = val$parentUserId;
            this.val$accountCredentials = val$accountCredentials;
            super(this$0, $anonymous0, $anonymous1, $anonymous2, $anonymous3, $anonymous4, $anonymous5, $anonymous6);
        }

        protected String toDebugString(long now) {
            return super.toDebugString(now) + ", getAccountCredentialsForClone" + ", " + this.val$account.type;
        }

        public void run() throws RemoteException {
            synchronized (this.this$0.getUserAccounts(this.val$parentUserId).cacheLock) {
                Account[] accounts = this.this$0.getAccounts(this.val$parentUserId, this.this$0.mContext.getOpPackageName());
                int length = accounts.length;
                for (int i = 0; i < length; i += AccountManagerService.DE_DATABASE_VERSION) {
                    if (accounts[i].equals(this.val$account)) {
                        this.mAuthenticator.addAccountFromCredentials(this, this.val$account, this.val$accountCredentials);
                        break;
                    }
                }
            }
        }

        public void onResult(Bundle result) {
            Bundle.setDefusable(result, true);
            super.onResult(result);
        }

        public void onError(int errorCode, String errorMessage) {
            super.onError(errorCode, errorMessage);
        }
    }

    /* renamed from: com.android.server.accounts.AccountManagerService.5 */
    class AnonymousClass5 extends Session {
        final /* synthetic */ AccountManagerService this$0;
        final /* synthetic */ String val$accountType;
        final /* synthetic */ String val$authTokenType;

        AnonymousClass5(AccountManagerService this$0, AccountManagerService this$0_1, UserAccounts $anonymous0, IAccountManagerResponse $anonymous1, String $anonymous2, boolean $anonymous3, boolean $anonymous4, String $anonymous5, boolean $anonymous6, String val$accountType, String val$authTokenType) {
            this.this$0 = this$0_1;
            this.val$accountType = val$accountType;
            this.val$authTokenType = val$authTokenType;
            super(this$0, $anonymous0, $anonymous1, $anonymous2, $anonymous3, $anonymous4, $anonymous5, $anonymous6);
        }

        protected String toDebugString(long now) {
            return super.toDebugString(now) + ", getAuthTokenLabel" + ", " + this.val$accountType + ", authTokenType " + this.val$authTokenType;
        }

        public void run() throws RemoteException {
            this.mAuthenticator.getAuthTokenLabel(this, this.val$authTokenType);
        }

        public void onResult(Bundle result) {
            Bundle.setDefusable(result, true);
            if (result != null) {
                String label = result.getString("authTokenLabelKey");
                Bundle bundle = new Bundle();
                bundle.putString("authTokenLabelKey", label);
                super.onResult(bundle);
                return;
            }
            super.onResult(result);
        }
    }

    /* renamed from: com.android.server.accounts.AccountManagerService.6 */
    class AnonymousClass6 extends Session {
        final /* synthetic */ AccountManagerService this$0;
        final /* synthetic */ Account val$account;
        final /* synthetic */ UserAccounts val$accounts;
        final /* synthetic */ String val$authTokenType;
        final /* synthetic */ String val$callerPkg;
        final /* synthetic */ byte[] val$callerPkgSigDigest;
        final /* synthetic */ int val$callerUid;
        final /* synthetic */ boolean val$customTokens;
        final /* synthetic */ Bundle val$loginOptions;
        final /* synthetic */ boolean val$notifyOnAuthFailure;
        final /* synthetic */ boolean val$permissionGranted;

        AnonymousClass6(AccountManagerService this$0, AccountManagerService this$0_1, UserAccounts $anonymous0, IAccountManagerResponse $anonymous1, String $anonymous2, boolean $anonymous3, boolean $anonymous4, String $anonymous5, boolean $anonymous6, Bundle val$loginOptions, Account val$account, String val$authTokenType, boolean val$notifyOnAuthFailure, boolean val$permissionGranted, int val$callerUid, boolean val$customTokens, String val$callerPkg, byte[] val$callerPkgSigDigest, UserAccounts val$accounts) {
            this.this$0 = this$0_1;
            this.val$loginOptions = val$loginOptions;
            this.val$account = val$account;
            this.val$authTokenType = val$authTokenType;
            this.val$notifyOnAuthFailure = val$notifyOnAuthFailure;
            this.val$permissionGranted = val$permissionGranted;
            this.val$callerUid = val$callerUid;
            this.val$customTokens = val$customTokens;
            this.val$callerPkg = val$callerPkg;
            this.val$callerPkgSigDigest = val$callerPkgSigDigest;
            this.val$accounts = val$accounts;
            super(this$0, $anonymous0, $anonymous1, $anonymous2, $anonymous3, $anonymous4, $anonymous5, $anonymous6);
        }

        protected String toDebugString(long now) {
            if (this.val$loginOptions != null) {
                this.val$loginOptions.keySet();
            }
            return super.toDebugString(now) + ", getAuthToken" + ", " + this.val$account + ", authTokenType " + this.val$authTokenType + ", loginOptions " + this.val$loginOptions + ", notifyOnAuthFailure " + this.val$notifyOnAuthFailure;
        }

        public void run() throws RemoteException {
            if (this.val$permissionGranted) {
                this.mAuthenticator.getAuthToken(this, this.val$account, this.val$authTokenType, this.val$loginOptions);
            } else {
                this.mAuthenticator.getAuthTokenLabel(this, this.val$authTokenType);
            }
        }

        public void onResult(Bundle result) {
            Bundle.setDefusable(result, true);
            if (result != null) {
                Intent intent;
                if (result.containsKey("authTokenLabelKey")) {
                    intent = this.this$0.newGrantCredentialsPermissionIntent(this.val$account, this.val$callerUid, new AccountAuthenticatorResponse(this), this.val$authTokenType);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(HwBroadcastRadarUtil.KEY_BROADCAST_INTENT, intent);
                    onResult(bundle);
                    return;
                }
                String authToken = result.getString(AccountManagerService.AUTHTOKENS_AUTHTOKEN);
                if (authToken != null) {
                    String name = result.getString("authAccount");
                    String type = result.getString("accountType");
                    if (TextUtils.isEmpty(type) || TextUtils.isEmpty(name)) {
                        onError(5, "the type and name should not be empty");
                        return;
                    }
                    Account resultAccount = new Account(name, type);
                    if (!this.val$customTokens) {
                        this.this$0.saveAuthTokenToDatabase(this.mAccounts, resultAccount, this.val$authTokenType, authToken);
                    }
                    long expiryMillis = result.getLong("android.accounts.expiry", 0);
                    if (this.val$customTokens && expiryMillis > System.currentTimeMillis()) {
                        this.this$0.saveCachedToken(this.mAccounts, this.val$account, this.val$callerPkg, this.val$callerPkgSigDigest, this.val$authTokenType, authToken, expiryMillis);
                    }
                }
                intent = (Intent) result.getParcelable(HwBroadcastRadarUtil.KEY_BROADCAST_INTENT);
                if (!(intent == null || !this.val$notifyOnAuthFailure || this.val$customTokens)) {
                    checkKeyIntent(Binder.getCallingUid(), intent);
                    this.this$0.doNotification(this.mAccounts, this.val$account, result.getString("authFailedMessage"), intent, this.val$accounts.userId);
                }
            }
            super.onResult(result);
        }
    }

    /* renamed from: com.android.server.accounts.AccountManagerService.7 */
    class AnonymousClass7 extends Session {
        final /* synthetic */ AccountManagerService this$0;
        final /* synthetic */ String val$accountType;
        final /* synthetic */ String val$authTokenType;
        final /* synthetic */ Bundle val$options;
        final /* synthetic */ String[] val$requiredFeatures;

        AnonymousClass7(AccountManagerService this$0, AccountManagerService this$0_1, UserAccounts $anonymous0, IAccountManagerResponse $anonymous1, String $anonymous2, boolean $anonymous3, boolean $anonymous4, String $anonymous5, boolean $anonymous6, boolean $anonymous7, String val$authTokenType, String[] val$requiredFeatures, Bundle val$options, String val$accountType) {
            this.this$0 = this$0_1;
            this.val$authTokenType = val$authTokenType;
            this.val$requiredFeatures = val$requiredFeatures;
            this.val$options = val$options;
            this.val$accountType = val$accountType;
            super($anonymous0, $anonymous1, $anonymous2, $anonymous3, $anonymous4, $anonymous5, $anonymous6, $anonymous7);
        }

        public void run() throws RemoteException {
            this.mAuthenticator.addAccount(this, this.mAccountType, this.val$authTokenType, this.val$requiredFeatures, this.val$options);
        }

        protected String toDebugString(long now) {
            String str = null;
            StringBuilder append = new StringBuilder().append(super.toDebugString(now)).append(", addAccount").append(", accountType ").append(this.val$accountType).append(", requiredFeatures ");
            if (this.val$requiredFeatures != null) {
                str = TextUtils.join(",", this.val$requiredFeatures);
            }
            return append.append(str).toString();
        }
    }

    /* renamed from: com.android.server.accounts.AccountManagerService.8 */
    class AnonymousClass8 extends Session {
        final /* synthetic */ AccountManagerService this$0;
        final /* synthetic */ String val$accountType;
        final /* synthetic */ String val$authTokenType;
        final /* synthetic */ Bundle val$options;
        final /* synthetic */ String[] val$requiredFeatures;

        AnonymousClass8(AccountManagerService this$0, AccountManagerService this$0_1, UserAccounts $anonymous0, IAccountManagerResponse $anonymous1, String $anonymous2, boolean $anonymous3, boolean $anonymous4, String $anonymous5, boolean $anonymous6, boolean $anonymous7, String val$authTokenType, String[] val$requiredFeatures, Bundle val$options, String val$accountType) {
            this.this$0 = this$0_1;
            this.val$authTokenType = val$authTokenType;
            this.val$requiredFeatures = val$requiredFeatures;
            this.val$options = val$options;
            this.val$accountType = val$accountType;
            super($anonymous0, $anonymous1, $anonymous2, $anonymous3, $anonymous4, $anonymous5, $anonymous6, $anonymous7);
        }

        public void run() throws RemoteException {
            this.mAuthenticator.addAccount(this, this.mAccountType, this.val$authTokenType, this.val$requiredFeatures, this.val$options);
        }

        protected String toDebugString(long now) {
            String str = null;
            StringBuilder append = new StringBuilder().append(super.toDebugString(now)).append(", addAccount").append(", accountType ").append(this.val$accountType).append(", requiredFeatures ");
            if (this.val$requiredFeatures != null) {
                str = TextUtils.join(",", this.val$requiredFeatures);
            }
            return append.append(str).toString();
        }
    }

    /* renamed from: com.android.server.accounts.AccountManagerService.9 */
    class AnonymousClass9 extends StartAccountSession {
        final /* synthetic */ AccountManagerService this$0;
        final /* synthetic */ String val$accountType;
        final /* synthetic */ String val$authTokenType;
        final /* synthetic */ Bundle val$options;
        final /* synthetic */ String[] val$requiredFeatures;

        AnonymousClass9(AccountManagerService this$0, AccountManagerService this$0_1, UserAccounts $anonymous0, IAccountManagerResponse $anonymous1, String $anonymous2, boolean $anonymous3, String $anonymous4, boolean $anonymous5, boolean $anonymous6, boolean $anonymous7, String val$authTokenType, String[] val$requiredFeatures, Bundle val$options, String val$accountType) {
            this.this$0 = this$0_1;
            this.val$authTokenType = val$authTokenType;
            this.val$requiredFeatures = val$requiredFeatures;
            this.val$options = val$options;
            this.val$accountType = val$accountType;
            super($anonymous0, $anonymous1, $anonymous2, $anonymous3, $anonymous4, $anonymous5, $anonymous6, $anonymous7);
        }

        public void run() throws RemoteException {
            this.mAuthenticator.startAddAccountSession(this, this.mAccountType, this.val$authTokenType, this.val$requiredFeatures, this.val$options);
        }

        protected String toDebugString(long now) {
            String requiredFeaturesStr = TextUtils.join(",", this.val$requiredFeatures);
            StringBuilder append = new StringBuilder().append(super.toDebugString(now)).append(", startAddAccountSession").append(", accountType ").append(this.val$accountType).append(", requiredFeatures ");
            if (this.val$requiredFeatures == null) {
                requiredFeaturesStr = null;
            }
            return append.append(requiredFeaturesStr).toString();
        }
    }

    static class CeDatabaseHelper extends SQLiteOpenHelper {
        public CeDatabaseHelper(Context context, String ceDatabaseName) {
            super(context, ceDatabaseName, null, AccountManagerService.CE_DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            Log.i(AccountManagerService.TAG, "Creating CE database " + getDatabaseName());
            db.execSQL("CREATE TABLE accounts ( _id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, type TEXT NOT NULL, password TEXT, UNIQUE(name,type))");
            db.execSQL("CREATE TABLE authtokens (  _id INTEGER PRIMARY KEY AUTOINCREMENT,  accounts_id INTEGER NOT NULL, type TEXT NOT NULL,  authtoken TEXT,  UNIQUE (accounts_id,type))");
            db.execSQL("CREATE TABLE extras ( _id INTEGER PRIMARY KEY AUTOINCREMENT, accounts_id INTEGER, key TEXT NOT NULL, value TEXT, UNIQUE(accounts_id,key))");
            createAccountsDeletionTrigger(db);
        }

        private void createAccountsDeletionTrigger(SQLiteDatabase db) {
            db.execSQL(" CREATE TRIGGER accountsDelete DELETE ON accounts BEGIN   DELETE FROM authtokens     WHERE accounts_id=OLD._id ;   DELETE FROM extras     WHERE accounts_id=OLD._id ; END");
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(AccountManagerService.TAG, "Upgrade CE from version " + oldVersion + " to version " + newVersion);
            if (oldVersion == AccountManagerService.PRE_N_DATABASE_VERSION) {
                if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                    Log.v(AccountManagerService.TAG, "onUpgrade upgrading to v10");
                }
                db.execSQL("DROP TABLE IF EXISTS meta");
                db.execSQL("DROP TABLE IF EXISTS shared_accounts");
                db.execSQL("DROP TRIGGER IF EXISTS accountsDelete");
                createAccountsDeletionTrigger(db);
                db.execSQL("DROP TABLE IF EXISTS grants");
                db.execSQL("DROP TABLE IF EXISTS " + DebugDbHelper.TABLE_DEBUG);
                oldVersion += AccountManagerService.DE_DATABASE_VERSION;
            }
            if (oldVersion != newVersion) {
                Log.e(AccountManagerService.TAG, "failed to upgrade version " + oldVersion + " to version " + newVersion);
            }
        }

        public void onOpen(SQLiteDatabase db) {
            if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                Log.v(AccountManagerService.TAG, "opened database accounts_ce.db");
            }
        }

        static String findAccountPasswordByNameAndType(SQLiteDatabase db, String name, String type) {
            String str = AccountManagerService.CE_TABLE_ACCOUNTS;
            String[] strArr = new String[AccountManagerService.DE_DATABASE_VERSION];
            strArr[0] = AccountManagerService.ACCOUNTS_PASSWORD;
            SQLiteDatabase sQLiteDatabase = db;
            Cursor cursor = sQLiteDatabase.query(str, strArr, "name=? AND type=?", new String[]{name, type}, null, null, null);
            try {
                if (cursor.moveToNext()) {
                    String string = cursor.getString(0);
                    return string;
                }
                cursor.close();
                return null;
            } finally {
                cursor.close();
            }
        }

        static List<Account> findCeAccountsNotInDe(SQLiteDatabase db) {
            Cursor cursor = db.rawQuery("SELECT name,type FROM ceDb.accounts WHERE NOT EXISTS  (SELECT _id FROM accounts WHERE _id=ceDb.accounts._id )", null);
            try {
                List<Account> accounts = new ArrayList(cursor.getCount());
                while (cursor.moveToNext()) {
                    accounts.add(new Account(cursor.getString(0), cursor.getString(AccountManagerService.DE_DATABASE_VERSION)));
                }
                return accounts;
            } finally {
                cursor.close();
            }
        }

        static CeDatabaseHelper create(Context context, int userId, File preNDatabaseFile, File ceDatabaseFile) {
            boolean newDbExists = ceDatabaseFile.exists();
            if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                Log.v(AccountManagerService.TAG, "CeDatabaseHelper.create userId=" + userId + " oldDbExists=" + preNDatabaseFile.exists() + " newDbExists=" + newDbExists);
            }
            boolean removeOldDb = false;
            if (!newDbExists && preNDatabaseFile.exists()) {
                removeOldDb = migratePreNDbToCe(preNDatabaseFile, ceDatabaseFile);
            }
            CeDatabaseHelper ceHelper = new CeDatabaseHelper(context, ceDatabaseFile.getPath());
            ceHelper.getWritableDatabase();
            ceHelper.close();
            if (removeOldDb) {
                Slog.i(AccountManagerService.TAG, "Migration complete - removing pre-N db " + preNDatabaseFile);
                if (!SQLiteDatabase.deleteDatabase(preNDatabaseFile)) {
                    Slog.e(AccountManagerService.TAG, "Cannot remove pre-N db " + preNDatabaseFile);
                }
            }
            return ceHelper;
        }

        private static boolean migratePreNDbToCe(File oldDbFile, File ceDbFile) {
            Slog.i(AccountManagerService.TAG, "Moving pre-N DB " + oldDbFile + " to CE " + ceDbFile);
            try {
                FileUtils.copyFileOrThrow(oldDbFile, ceDbFile);
                return true;
            } catch (IOException e) {
                Slog.e(AccountManagerService.TAG, "Cannot copy file to " + ceDbFile + " from " + oldDbFile, e);
                AccountManagerService.deleteDbFileWarnIfFailed(ceDbFile);
                return false;
            }
        }
    }

    static class DeDatabaseHelper extends SQLiteOpenHelper {
        private volatile boolean mCeAttached;
        private final int mUserId;

        private DeDatabaseHelper(Context context, int userId, String deDatabaseName) {
            super(context, deDatabaseName, null, AccountManagerService.DE_DATABASE_VERSION);
            this.mUserId = userId;
        }

        public void onCreate(SQLiteDatabase db) {
            Log.i(AccountManagerService.TAG, "Creating DE database for user " + this.mUserId);
            db.execSQL("CREATE TABLE accounts ( _id INTEGER PRIMARY KEY, name TEXT NOT NULL, type TEXT NOT NULL, previous_name TEXT, last_password_entry_time_millis_epoch INTEGER DEFAULT 0, UNIQUE(name,type))");
            db.execSQL("CREATE TABLE meta ( key TEXT PRIMARY KEY NOT NULL, value TEXT)");
            createGrantsTable(db);
            createSharedAccountsTable(db);
            createAccountsDeletionTrigger(db);
            DebugDbHelper.createDebugTable(db);
        }

        private void createSharedAccountsTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE shared_accounts ( _id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, type TEXT NOT NULL, UNIQUE(name,type))");
        }

        private void createAccountsDeletionTrigger(SQLiteDatabase db) {
            db.execSQL(" CREATE TRIGGER accountsDelete DELETE ON accounts BEGIN   DELETE FROM grants     WHERE accounts_id=OLD._id ; END");
        }

        private void createGrantsTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE grants (  accounts_id INTEGER NOT NULL, auth_token_type STRING NOT NULL,  uid INTEGER NOT NULL,  UNIQUE (accounts_id,auth_token_type,uid))");
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(AccountManagerService.TAG, "upgrade from version " + oldVersion + " to version " + newVersion);
            if (oldVersion != newVersion) {
                Log.e(AccountManagerService.TAG, "failed to upgrade version " + oldVersion + " to version " + newVersion);
            }
        }

        public void attachCeDatabase(File ceDbFile) {
            getWritableDatabase().execSQL("ATTACH DATABASE '" + ceDbFile.getPath() + "' AS ceDb");
            this.mCeAttached = true;
        }

        public boolean isCeDatabaseAttached() {
            return this.mCeAttached;
        }

        public SQLiteDatabase getReadableDatabaseUserIsUnlocked() {
            if (!this.mCeAttached) {
                Log.wtf(AccountManagerService.TAG, "getReadableDatabaseUserIsUnlocked called while user " + this.mUserId + " is still locked. CE database is not yet available.", new Throwable());
            }
            return super.getReadableDatabase();
        }

        public SQLiteDatabase getWritableDatabaseUserIsUnlocked() {
            if (!this.mCeAttached) {
                Log.wtf(AccountManagerService.TAG, "getWritableDatabaseUserIsUnlocked called while user " + this.mUserId + " is still locked. CE database is not yet available.", new Throwable());
            }
            return super.getWritableDatabase();
        }

        public void onOpen(SQLiteDatabase db) {
            if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                Log.v(AccountManagerService.TAG, "opened database accounts_de.db");
            }
        }

        private void migratePreNDbToDe(File preNDbFile) {
            Log.i(AccountManagerService.TAG, "Migrate pre-N database to DE preNDbFile=" + preNDbFile);
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("ATTACH DATABASE '" + preNDbFile.getPath() + "' AS preNDb");
            db.beginTransaction();
            db.execSQL("INSERT INTO accounts(_id,name,type, previous_name, last_password_entry_time_millis_epoch) SELECT _id,name,type, previous_name, last_password_entry_time_millis_epoch FROM preNDb.accounts");
            db.execSQL("INSERT INTO shared_accounts(_id,name,type) SELECT _id,name,type FROM preNDb.shared_accounts");
            db.execSQL("INSERT INTO " + DebugDbHelper.TABLE_DEBUG + "(" + AccountManagerService.SHARED_ACCOUNTS_ID + "," + DebugDbHelper.ACTION_TYPE + "," + DebugDbHelper.TIMESTAMP + "," + DebugDbHelper.CALLER_UID + "," + DebugDbHelper.TABLE_NAME + "," + DebugDbHelper.KEY + ") " + "SELECT " + AccountManagerService.SHARED_ACCOUNTS_ID + "," + DebugDbHelper.ACTION_TYPE + "," + DebugDbHelper.TIMESTAMP + "," + DebugDbHelper.CALLER_UID + "," + DebugDbHelper.TABLE_NAME + "," + DebugDbHelper.KEY + " FROM preNDb." + DebugDbHelper.TABLE_DEBUG);
            db.execSQL("INSERT INTO grants(accounts_id,auth_token_type,uid) SELECT accounts_id,auth_token_type,uid FROM preNDb.grants");
            db.execSQL("INSERT INTO meta(key,value) SELECT key,value FROM preNDb.meta");
            db.setTransactionSuccessful();
            db.endTransaction();
            db.execSQL("DETACH DATABASE preNDb");
        }

        static DeDatabaseHelper create(Context context, int userId, File preNDatabaseFile, File deDatabaseFile) {
            boolean newDbExists = deDatabaseFile.exists();
            DeDatabaseHelper deDatabaseHelper = new DeDatabaseHelper(context, userId, deDatabaseFile.getPath());
            if (!newDbExists && preNDatabaseFile.exists()) {
                PreNDatabaseHelper preNDatabaseHelper = new PreNDatabaseHelper(context, userId, preNDatabaseFile.getPath());
                preNDatabaseHelper.getWritableDatabase();
                preNDatabaseHelper.close();
                deDatabaseHelper.migratePreNDbToDe(preNDatabaseFile);
            }
            return deDatabaseHelper;
        }
    }

    private static class DebugDbHelper {
        private static String ACTION_ACCOUNT_ADD;
        private static String ACTION_ACCOUNT_REMOVE;
        private static String ACTION_ACCOUNT_REMOVE_DE;
        private static String ACTION_ACCOUNT_RENAME;
        private static String ACTION_AUTHENTICATOR_REMOVE;
        private static String ACTION_CALLED_ACCOUNT_ADD;
        private static String ACTION_CALLED_ACCOUNT_REMOVE;
        private static String ACTION_CALLED_ACCOUNT_SESSION_FINISH;
        private static String ACTION_CALLED_START_ACCOUNT_ADD;
        private static String ACTION_CLEAR_PASSWORD;
        private static String ACTION_SET_PASSWORD;
        private static String ACTION_SYNC_DE_CE_ACCOUNTS;
        private static String ACTION_TYPE;
        private static String CALLER_UID;
        private static String KEY;
        private static String TABLE_DEBUG;
        private static String TABLE_NAME;
        private static String TIMESTAMP;
        private static SimpleDateFormat dateFromat;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.accounts.AccountManagerService.DebugDbHelper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.accounts.AccountManagerService.DebugDbHelper.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.accounts.AccountManagerService.DebugDbHelper.<clinit>():void");
        }

        private DebugDbHelper() {
        }

        private static void createDebugTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_DEBUG + " ( " + AccountManagerService.SHARED_ACCOUNTS_ID + " INTEGER," + ACTION_TYPE + " TEXT NOT NULL, " + TIMESTAMP + " DATETIME," + CALLER_UID + " INTEGER NOT NULL," + TABLE_NAME + " TEXT NOT NULL," + KEY + " INTEGER PRIMARY KEY)");
            db.execSQL("CREATE INDEX timestamp_index ON " + TABLE_DEBUG + " (" + TIMESTAMP + ")");
        }
    }

    private class GetAccountsByTypeAndFeatureSession extends Session {
        private volatile Account[] mAccountsOfType;
        private volatile ArrayList<Account> mAccountsWithFeatures;
        private final int mCallingUid;
        private volatile int mCurrentAccount;
        private final String[] mFeatures;
        final /* synthetic */ AccountManagerService this$0;

        public GetAccountsByTypeAndFeatureSession(AccountManagerService this$0, UserAccounts accounts, IAccountManagerResponse response, String type, String[] features, int callingUid) {
            this.this$0 = this$0;
            super(this$0, accounts, response, type, false, true, null, false);
            this.mAccountsOfType = null;
            this.mAccountsWithFeatures = null;
            this.mCurrentAccount = 0;
            this.mCallingUid = callingUid;
            this.mFeatures = features;
        }

        public void run() throws RemoteException {
            synchronized (this.mAccounts.cacheLock) {
                this.mAccountsOfType = this.this$0.getAccountsFromCacheLocked(this.mAccounts, this.mAccountType, this.mCallingUid, null);
            }
            this.mAccountsWithFeatures = new ArrayList(this.mAccountsOfType.length);
            this.mCurrentAccount = 0;
            checkAccount();
        }

        public void checkAccount() {
            if (this.mCurrentAccount >= this.mAccountsOfType.length) {
                sendResult();
                return;
            }
            IAccountAuthenticator accountAuthenticator = this.mAuthenticator;
            if (accountAuthenticator == null) {
                if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                    Log.v(AccountManagerService.TAG, "checkAccount: aborting session since we are no longer connected to the authenticator, " + toDebugString());
                }
                return;
            }
            try {
                accountAuthenticator.hasFeatures(this, this.mAccountsOfType[this.mCurrentAccount], this.mFeatures);
            } catch (RemoteException e) {
                onError(AccountManagerService.DE_DATABASE_VERSION, "remote exception");
            }
        }

        public void onResult(Bundle result) {
            Bundle.setDefusable(result, true);
            this.mNumResults += AccountManagerService.DE_DATABASE_VERSION;
            if (result == null) {
                onError(5, "null bundle");
                return;
            }
            if (result.getBoolean("booleanResult", false)) {
                this.mAccountsWithFeatures.add(this.mAccountsOfType[this.mCurrentAccount]);
            }
            this.mCurrentAccount += AccountManagerService.DE_DATABASE_VERSION;
            checkAccount();
        }

        public void sendResult() {
            IAccountManagerResponse response = getResponseAndClose();
            if (response != null) {
                try {
                    Account[] accounts = new Account[this.mAccountsWithFeatures.size()];
                    for (int i = 0; i < accounts.length; i += AccountManagerService.DE_DATABASE_VERSION) {
                        accounts[i] = (Account) this.mAccountsWithFeatures.get(i);
                    }
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
                    }
                    Bundle result = new Bundle();
                    result.putParcelableArray(AccountManagerService.TABLE_ACCOUNTS, accounts);
                    response.onResult(result);
                } catch (RemoteException e) {
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, "failure while notifying response", e);
                    }
                }
            }
        }

        protected String toDebugString(long now) {
            String str = null;
            StringBuilder append = new StringBuilder().append(super.toDebugString(now)).append(", getAccountsByTypeAndFeatures").append(", ");
            if (this.mFeatures != null) {
                str = TextUtils.join(",", this.mFeatures);
            }
            return append.append(str).toString();
        }
    }

    public static class Lifecycle extends SystemService {
        private AccountManagerService mService;

        public Lifecycle(Context context) {
            super(context);
        }

        public void onStart() {
            this.mService = new AccountManagerService(getContext());
            publishBinderService("account", this.mService);
        }

        public void onBootPhase(int phase) {
            if (phase == SystemService.PHASE_ACTIVITY_MANAGER_READY) {
                this.mService.systemReady();
            }
        }

        public void onUnlockUser(int userHandle) {
            this.mService.onUnlockUser(userHandle);
        }
    }

    private class MessageHandler extends Handler {
        final /* synthetic */ AccountManagerService this$0;

        MessageHandler(AccountManagerService this$0, Looper looper) {
            this.this$0 = this$0;
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AccountManagerService.MESSAGE_TIMED_OUT /*3*/:
                    msg.obj.onTimedOut();
                case AccountManagerService.MESSAGE_COPY_SHARED_ACCOUNT /*4*/:
                    this.this$0.copyAccountToUser(null, (Account) msg.obj, msg.arg1, msg.arg2);
                default:
                    throw new IllegalStateException("unhandled message: " + msg.what);
            }
        }
    }

    static class PreNDatabaseHelper extends SQLiteOpenHelper {
        private final Context mContext;
        private final int mUserId;

        public PreNDatabaseHelper(Context context, int userId, String preNDatabaseName) {
            super(context, preNDatabaseName, null, AccountManagerService.PRE_N_DATABASE_VERSION);
            this.mContext = context;
            this.mUserId = userId;
        }

        public void onCreate(SQLiteDatabase db) {
            throw new IllegalStateException("Legacy database cannot be created - only upgraded!");
        }

        private void createSharedAccountsTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE shared_accounts ( _id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, type TEXT NOT NULL, UNIQUE(name,type))");
        }

        private void addLastSuccessfullAuthenticatedTimeColumn(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE accounts ADD COLUMN last_password_entry_time_millis_epoch DEFAULT 0");
        }

        private void addOldAccountNameColumn(SQLiteDatabase db) {
            db.execSQL("ALTER TABLE accounts ADD COLUMN previous_name");
        }

        private void addDebugTable(SQLiteDatabase db) {
            DebugDbHelper.createDebugTable(db);
        }

        private void createAccountsDeletionTrigger(SQLiteDatabase db) {
            db.execSQL(" CREATE TRIGGER accountsDelete DELETE ON accounts BEGIN   DELETE FROM authtokens     WHERE accounts_id=OLD._id ;   DELETE FROM extras     WHERE accounts_id=OLD._id ;   DELETE FROM grants     WHERE accounts_id=OLD._id ; END");
        }

        private void createGrantsTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE grants (  accounts_id INTEGER NOT NULL, auth_token_type STRING NOT NULL,  uid INTEGER NOT NULL,  UNIQUE (accounts_id,auth_token_type,uid))");
        }

        private void populateMetaTableWithAuthTypeAndUID(SQLiteDatabase db, Map<String, Integer> authTypeAndUIDMap) {
            for (Entry<String, Integer> entry : authTypeAndUIDMap.entrySet()) {
                ContentValues values = new ContentValues();
                values.put(AccountManagerService.META_KEY, AccountManagerService.META_KEY_FOR_AUTHENTICATOR_UID_FOR_TYPE_PREFIX + ((String) entry.getKey()));
                values.put(AccountManagerService.META_VALUE, (Integer) entry.getValue());
                db.insert(AccountManagerService.TABLE_META, null, values);
            }
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.e(AccountManagerService.TAG, "upgrade from version " + oldVersion + " to version " + newVersion);
            if (oldVersion == AccountManagerService.DE_DATABASE_VERSION) {
                oldVersion += AccountManagerService.DE_DATABASE_VERSION;
            }
            if (oldVersion == 2) {
                createGrantsTable(db);
                db.execSQL("DROP TRIGGER accountsDelete");
                createAccountsDeletionTrigger(db);
                oldVersion += AccountManagerService.DE_DATABASE_VERSION;
            }
            if (oldVersion == AccountManagerService.MESSAGE_TIMED_OUT) {
                db.execSQL("UPDATE accounts SET type = 'com.google' WHERE type == 'com.google.GAIA'");
                oldVersion += AccountManagerService.DE_DATABASE_VERSION;
            }
            if (oldVersion == AccountManagerService.MESSAGE_COPY_SHARED_ACCOUNT) {
                createSharedAccountsTable(db);
                oldVersion += AccountManagerService.DE_DATABASE_VERSION;
            }
            if (oldVersion == 5) {
                addOldAccountNameColumn(db);
                oldVersion += AccountManagerService.DE_DATABASE_VERSION;
            }
            if (oldVersion == 6) {
                addLastSuccessfullAuthenticatedTimeColumn(db);
                oldVersion += AccountManagerService.DE_DATABASE_VERSION;
            }
            if (oldVersion == 7) {
                addDebugTable(db);
                oldVersion += AccountManagerService.DE_DATABASE_VERSION;
            }
            if (oldVersion == 8) {
                populateMetaTableWithAuthTypeAndUID(db, AccountManagerService.getAuthenticatorTypeAndUIDForUser(this.mContext, this.mUserId));
                oldVersion += AccountManagerService.DE_DATABASE_VERSION;
            }
            if (oldVersion != newVersion) {
                Log.e(AccountManagerService.TAG, "failed to upgrade version " + oldVersion + " to version " + newVersion);
            }
        }

        public void onOpen(SQLiteDatabase db) {
            if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                Log.v(AccountManagerService.TAG, "opened database accounts.db");
            }
        }
    }

    private class RemoveAccountSession extends Session {
        final Account mAccount;
        final /* synthetic */ AccountManagerService this$0;

        public RemoveAccountSession(AccountManagerService this$0, UserAccounts accounts, IAccountManagerResponse response, Account account, boolean expectActivityLaunch) {
            this.this$0 = this$0;
            super(this$0, accounts, response, account.type, expectActivityLaunch, true, account.name, false);
            this.mAccount = account;
        }

        protected String toDebugString(long now) {
            return super.toDebugString(now) + ", removeAccount" + ", account " + this.mAccount;
        }

        public void run() throws RemoteException {
            this.mAuthenticator.getAccountRemovalAllowed(this, this.mAccount);
        }

        public void onResult(Bundle result) {
            Bundle.setDefusable(result, true);
            if (!(result == null || !result.containsKey("booleanResult") || result.containsKey(HwBroadcastRadarUtil.KEY_BROADCAST_INTENT))) {
                boolean removalAllowed = result.getBoolean("booleanResult");
                if (removalAllowed) {
                    this.this$0.removeAccountInternal(this.mAccounts, this.mAccount, getCallingUid());
                }
                IAccountManagerResponse response = getResponseAndClose();
                if (response != null) {
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
                    }
                    Bundle result2 = new Bundle();
                    result2.putBoolean("booleanResult", removalAllowed);
                    try {
                        response.onResult(result2);
                    } catch (RemoteException e) {
                    }
                }
            }
            super.onResult(result);
        }
    }

    private class TestFeaturesSession extends Session {
        private final Account mAccount;
        private final String[] mFeatures;
        final /* synthetic */ AccountManagerService this$0;

        public TestFeaturesSession(AccountManagerService this$0, UserAccounts accounts, IAccountManagerResponse response, Account account, String[] features) {
            this.this$0 = this$0;
            super(this$0, accounts, response, account.type, false, true, account.name, false);
            this.mFeatures = features;
            this.mAccount = account;
        }

        public void run() throws RemoteException {
            try {
                this.mAuthenticator.hasFeatures(this, this.mAccount, this.mFeatures);
            } catch (RemoteException e) {
                onError(AccountManagerService.DE_DATABASE_VERSION, "remote exception");
            }
        }

        public void onResult(Bundle result) {
            Bundle.setDefusable(result, true);
            IAccountManagerResponse response = getResponseAndClose();
            if (response != null) {
                if (result == null) {
                    try {
                        response.onError(5, "null bundle");
                    } catch (RemoteException e) {
                        if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                            Log.v(AccountManagerService.TAG, "failure while notifying response", e);
                        }
                    }
                } else {
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
                    }
                    Bundle newResult = new Bundle();
                    newResult.putBoolean("booleanResult", result.getBoolean("booleanResult", false));
                    response.onResult(newResult);
                }
            }
        }

        protected String toDebugString(long now) {
            String str = null;
            StringBuilder append = new StringBuilder().append(super.toDebugString(now)).append(", hasFeatures").append(", ").append(this.mAccount).append(", ");
            if (this.mFeatures != null) {
                str = TextUtils.join(",", this.mFeatures);
            }
            return append.append(str).toString();
        }
    }

    static class UserAccounts {
        private final HashMap<String, Account[]> accountCache;
        private final TokenCache accountTokenCaches;
        private final HashMap<Account, HashMap<String, String>> authTokenCache;
        private final Object cacheLock;
        private final HashMap<Pair<Pair<Account, String>, Integer>, Integer> credentialsPermissionNotificationIds;
        private int debugDbInsertionPoint;
        private final DeDatabaseHelper openHelper;
        private final HashMap<Account, AtomicReference<String>> previousNameCache;
        private final HashMap<Account, Integer> signinRequiredNotificationIds;
        private SQLiteStatement statementForLogging;
        private final HashMap<Account, HashMap<String, String>> userDataCache;
        private final int userId;

        UserAccounts(Context context, int userId, File preNDbFile, File deDbFile) {
            this.credentialsPermissionNotificationIds = new HashMap();
            this.signinRequiredNotificationIds = new HashMap();
            this.cacheLock = new Object();
            this.accountCache = new LinkedHashMap();
            this.userDataCache = new HashMap();
            this.authTokenCache = new HashMap();
            this.accountTokenCaches = new TokenCache();
            this.previousNameCache = new HashMap();
            this.debugDbInsertionPoint = -1;
            this.userId = userId;
            synchronized (this.cacheLock) {
                this.openHelper = DeDatabaseHelper.create(context, userId, preNDbFile, deDbFile);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.accounts.AccountManagerService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.accounts.AccountManagerService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.accounts.AccountManagerService.<clinit>():void");
    }

    public static AccountManagerService getSingleton() {
        return (AccountManagerService) sThis.get();
    }

    public AccountManagerService(Context context) {
        this(context, context.getPackageManager(), new AccountAuthenticatorCache(context));
    }

    public AccountManagerService(Context context, PackageManager packageManager, IAccountAuthenticatorCache authenticatorCache) {
        this.mSessions = new LinkedHashMap();
        this.mNotificationIds = new AtomicInteger(DE_DATABASE_VERSION);
        this.mUsers = new SparseArray();
        this.mLocalUnlockedUsers = new SparseBooleanArray();
        this.mContext = context;
        this.mPackageManager = packageManager;
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        this.mMessageHandler = new MessageHandler(this, FgThread.get().getLooper());
        this.mAuthenticatorCache = authenticatorCache;
        this.mAuthenticatorCache.setListener(this, null);
        sThis.set(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context1, Intent intent) {
                if (!intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                    new Thread(new Runnable() {
                        public void run() {
                            AccountManagerService.this.purgeOldGrantsAll();
                        }
                    }).start();
                }
            }
        }, intentFilter);
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.USER_REMOVED".equals(intent.getAction())) {
                    AccountManagerService.this.onUserRemoved(intent);
                }
            }
        }, UserHandle.ALL, userFilter, null, null);
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        try {
            return super.onTransact(code, data, reply, flags);
        } catch (RuntimeException e) {
            if (!(e instanceof SecurityException)) {
                Slog.wtf(TAG, "Account Manager Crash", e);
            }
            throw e;
        }
    }

    public void systemReady() {
    }

    private UserManager getUserManager() {
        if (this.mUserManager == null) {
            this.mUserManager = UserManager.get(this.mContext);
        }
        return this.mUserManager;
    }

    public void validateAccounts(int userId) {
        try {
            validateAccountsInternal(getUserAccounts(userId), true);
        } catch (SQLiteException e) {
            Log.e(TAG, "validateAccounts ret got err:", e);
            HwBootFail.brokenFileBootFail(83886086, "/data/system_de/0/accounts_de.db/ or /data/system_ce/0/accounts_ce.db", new Throwable());
        }
    }

    private void validateAccountsInternal(UserAccounts accounts, boolean invalidateAuthenticatorCache) {
        if (Log.isLoggable(TAG, MESSAGE_TIMED_OUT)) {
            Log.d(TAG, "validateAccountsInternal " + accounts.userId + " isCeDatabaseAttached=" + accounts.openHelper.isCeDatabaseAttached() + " userLocked=" + this.mLocalUnlockedUsers.get(accounts.userId));
        }
        if (invalidateAuthenticatorCache) {
            this.mAuthenticatorCache.invalidateCache(accounts.userId);
        }
        HashMap<String, Integer> knownAuth = getAuthenticatorTypeAndUIDForUser(this.mAuthenticatorCache, accounts.userId);
        boolean userUnlocked = isLocalUnlockedUser(accounts.userId);
        synchronized (accounts.cacheLock) {
            SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
            boolean accountDeleted = false;
            String str = TABLE_META;
            String[] strArr = new String[]{META_KEY, META_VALUE};
            String str2 = SELECTION_META_BY_AUTHENTICATOR_TYPE;
            String[] strArr2 = new String[DE_DATABASE_VERSION];
            strArr2[0] = "auth_uid_for_type:%";
            Cursor metaCursor = db.query(str, strArr, str2, strArr2, null, null, META_KEY);
            HashSet<String> obsoleteAuthType = Sets.newHashSet();
            SparseBooleanArray sparseBooleanArray = null;
            while (metaCursor.moveToNext()) {
                try {
                    String type = TextUtils.split(metaCursor.getString(0), META_KEY_DELIMITER)[DE_DATABASE_VERSION];
                    String uid = metaCursor.getString(DE_DATABASE_VERSION);
                    if (TextUtils.isEmpty(type) || TextUtils.isEmpty(uid)) {
                        Slog.e(TAG, "Auth type empty: " + TextUtils.isEmpty(type) + ", uid empty: " + TextUtils.isEmpty(uid));
                    } else {
                        Integer knownUid = (Integer) knownAuth.get(type);
                        if (knownUid != null) {
                            if (uid.equals(knownUid.toString())) {
                                knownAuth.remove(type);
                            }
                        }
                        if (sparseBooleanArray == null) {
                            sparseBooleanArray = getUidsOfInstalledOrUpdatedPackagesAsUser(accounts.userId);
                        }
                        if (!sparseBooleanArray.get(Integer.parseInt(uid))) {
                            obsoleteAuthType.add(type);
                            db.delete(TABLE_META, "key=? AND value=?", new String[]{META_KEY_FOR_AUTHENTICATOR_UID_FOR_TYPE_PREFIX + type, uid});
                        }
                    }
                } catch (Throwable th) {
                    metaCursor.close();
                }
            }
            metaCursor.close();
            for (Entry<String, Integer> entry : knownAuth.entrySet()) {
                ContentValues values = new ContentValues();
                values.put(META_KEY, META_KEY_FOR_AUTHENTICATOR_UID_FOR_TYPE_PREFIX + ((String) entry.getKey()));
                values.put(META_VALUE, (Integer) entry.getValue());
                db.insertWithOnConflict(TABLE_META, null, values, 5);
            }
            try {
                String accountType;
                ArrayList<String> accountNames;
                str = TABLE_ACCOUNTS;
                strArr = new String[MESSAGE_TIMED_OUT];
                strArr[0] = SHARED_ACCOUNTS_ID;
                strArr[DE_DATABASE_VERSION] = AUTHTOKENS_TYPE;
                strArr[2] = ACCOUNTS_NAME;
                Cursor cursor = db.query(str, strArr, null, null, null, null, SHARED_ACCOUNTS_ID);
                accounts.accountCache.clear();
                HashMap<String, ArrayList<String>> accountNamesByType = new LinkedHashMap();
                while (cursor.moveToNext()) {
                    long accountId = cursor.getLong(0);
                    accountType = cursor.getString(DE_DATABASE_VERSION);
                    String accountName = cursor.getString(2);
                    if (obsoleteAuthType.contains(accountType)) {
                        Slog.w(TAG, "deleting account because type " + accountType + "'s registered authenticator no longer exist.");
                        db.beginTransaction();
                        try {
                            db.delete(TABLE_ACCOUNTS, "_id=" + accountId, null);
                            Log.w(TAG, "delete DE accountId = " + accountId);
                            if (userUnlocked) {
                                db.delete(CE_TABLE_ACCOUNTS, "_id=" + accountId, null);
                                Log.w(TAG, "delete CE accountId = " + accountId);
                            }
                            db.setTransactionSuccessful();
                            db.endTransaction();
                            accountDeleted = true;
                            logRecord(db, DebugDbHelper.ACTION_AUTHENTICATOR_REMOVE, TABLE_ACCOUNTS, accountId, accounts);
                            Account account = new Account(accountName, accountType);
                            accounts.userDataCache.remove(account);
                            accounts.authTokenCache.remove(account);
                            accounts.accountTokenCaches.remove(account);
                        } catch (Throwable ex) {
                            Log.w(TAG, "validateAccountsInternal ret got err:", ex);
                            cursor.close();
                            if (accountDeleted) {
                                sendAccountsChangedBroadcast(accounts.userId);
                            }
                        } catch (Throwable th2) {
                            cursor.close();
                            if (accountDeleted) {
                                sendAccountsChangedBroadcast(accounts.userId);
                            }
                        }
                    } else {
                        accountNames = (ArrayList) accountNamesByType.get(accountType);
                        if (accountNames == null) {
                            accountNames = new ArrayList();
                            accountNamesByType.put(accountType, accountNames);
                        }
                        accountNames.add(accountName);
                    }
                }
                for (Entry<String, ArrayList<String>> cur : accountNamesByType.entrySet()) {
                    accountType = (String) cur.getKey();
                    accountNames = (ArrayList) cur.getValue();
                    Object accountsForType = new Account[accountNames.size()];
                    for (int i = 0; i < accountsForType.length; i += DE_DATABASE_VERSION) {
                        accountsForType[i] = new Account((String) accountNames.get(i), accountType);
                    }
                    accounts.accountCache.put(accountType, accountsForType);
                }
                cursor.close();
                if (accountDeleted) {
                    sendAccountsChangedBroadcast(accounts.userId);
                }
            } catch (Throwable ex2) {
                Log.w(TAG, "validateAccountsInternal ret got err:", ex2);
            }
        }
    }

    private SparseBooleanArray getUidsOfInstalledOrUpdatedPackagesAsUser(int userId) {
        List<PackageInfo> pkgsWithData = this.mPackageManager.getInstalledPackagesAsUser(DumpState.DUMP_PREFERRED_XML, userId);
        SparseBooleanArray knownUids = new SparseBooleanArray(pkgsWithData.size());
        for (PackageInfo pkgInfo : pkgsWithData) {
            if (!(pkgInfo.applicationInfo == null || (pkgInfo.applicationInfo.flags & 8388608) == 0)) {
                knownUids.put(pkgInfo.applicationInfo.uid, true);
            }
        }
        return knownUids;
    }

    private static HashMap<String, Integer> getAuthenticatorTypeAndUIDForUser(Context context, int userId) {
        return getAuthenticatorTypeAndUIDForUser(new AccountAuthenticatorCache(context), userId);
    }

    private static HashMap<String, Integer> getAuthenticatorTypeAndUIDForUser(IAccountAuthenticatorCache authCache, int userId) {
        HashMap<String, Integer> knownAuth = new HashMap();
        for (ServiceInfo<AuthenticatorDescription> service : authCache.getAllServices(userId)) {
            knownAuth.put(((AuthenticatorDescription) service.type).type, Integer.valueOf(service.uid));
        }
        return knownAuth;
    }

    private UserAccounts getUserAccountsForCaller() {
        return getUserAccounts(UserHandle.getCallingUserId());
    }

    protected UserAccounts getUserAccounts(int userId) {
        UserAccounts accounts;
        synchronized (this.mUsers) {
            accounts = (UserAccounts) this.mUsers.get(userId);
            boolean validateAccounts = false;
            if (accounts == null) {
                accounts = new UserAccounts(this.mContext, userId, new File(getPreNDatabaseName(userId)), new File(getDeDatabaseName(userId)));
                initializeDebugDbSizeAndCompileSqlStatementForLogging(accounts.openHelper.getWritableDatabase(), accounts);
                this.mUsers.append(userId, accounts);
                purgeOldGrants(accounts);
                validateAccounts = true;
            }
            if (!accounts.openHelper.isCeDatabaseAttached() && this.mLocalUnlockedUsers.get(userId)) {
                Log.i(TAG, "User " + userId + " is unlocked - opening CE database");
                synchronized (accounts.cacheLock) {
                    File preNDatabaseFile = new File(getPreNDatabaseName(userId));
                    File ceDatabaseFile = new File(getCeDatabaseName(userId));
                    CeDatabaseHelper.create(this.mContext, userId, preNDatabaseFile, ceDatabaseFile);
                    accounts.openHelper.attachCeDatabase(ceDatabaseFile);
                }
                syncDeCeAccountsLocked(accounts);
            }
            if (validateAccounts) {
                validateAccountsInternal(accounts, true);
            }
        }
        return accounts;
    }

    private void syncDeCeAccountsLocked(UserAccounts accounts) {
        Preconditions.checkState(Thread.holdsLock(this.mUsers), "mUsers lock must be held");
        List<Account> accountsToRemove = CeDatabaseHelper.findCeAccountsNotInDe(accounts.openHelper.getReadableDatabaseUserIsUnlocked());
        if (!accountsToRemove.isEmpty()) {
            Slog.i(TAG, "Accounts " + accountsToRemove + " were previously deleted while user " + accounts.userId + " was locked. Removing accounts from CE tables");
            logRecord(accounts, DebugDbHelper.ACTION_SYNC_DE_CE_ACCOUNTS, TABLE_ACCOUNTS);
            for (Account account : accountsToRemove) {
                removeAccountInternal(accounts, account, Process.myUid());
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void purgeOldGrantsAll() {
        synchronized (this.mUsers) {
            int i = 0;
            while (true) {
                if (i < this.mUsers.size()) {
                    purgeOldGrants((UserAccounts) this.mUsers.valueAt(i));
                    i += DE_DATABASE_VERSION;
                }
            }
        }
    }

    private void purgeOldGrants(UserAccounts accounts) {
        synchronized (accounts.cacheLock) {
            SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
            String str = TABLE_GRANTS;
            String[] strArr = new String[DE_DATABASE_VERSION];
            strArr[0] = GRANTS_GRANTEE_UID;
            Cursor cursor = db.query(str, strArr, null, null, GRANTS_GRANTEE_UID, null, null);
            while (cursor.moveToNext()) {
                try {
                    int uid = cursor.getInt(0);
                    if (!(this.mPackageManager.getPackagesForUid(uid) != null)) {
                        Log.d(TAG, "deleting grants for UID " + uid + " because its package is no longer installed");
                        String[] strArr2 = new String[DE_DATABASE_VERSION];
                        strArr2[0] = Integer.toString(uid);
                        db.delete(TABLE_GRANTS, "uid=?", strArr2);
                    }
                } catch (Throwable th) {
                    cursor.close();
                }
            }
            cursor.close();
        }
    }

    private void onUserRemoved(Intent intent) {
        int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
        if (userId >= DE_DATABASE_VERSION) {
            UserAccounts accounts;
            synchronized (this.mUsers) {
                accounts = (UserAccounts) this.mUsers.get(userId);
                this.mUsers.remove(userId);
                boolean userUnlocked = this.mLocalUnlockedUsers.get(userId);
                this.mLocalUnlockedUsers.delete(userId);
            }
            if (accounts != null) {
                synchronized (accounts.cacheLock) {
                    accounts.openHelper.close();
                }
            }
            Log.i(TAG, "Removing database files for user " + userId);
            deleteDbFileWarnIfFailed(new File(getDeDatabaseName(userId)));
            if (!StorageManager.isFileEncryptedNativeOrEmulated() || userUnlocked) {
                File ceDb = new File(getCeDatabaseName(userId));
                if (ceDb.exists()) {
                    deleteDbFileWarnIfFailed(ceDb);
                }
            }
        }
    }

    private static void deleteDbFileWarnIfFailed(File dbFile) {
        if (!SQLiteDatabase.deleteDatabase(dbFile)) {
            Log.w(TAG, "Database at " + dbFile + " was not deleted successfully");
        }
    }

    void onUserUnlocked(Intent intent) {
        onUnlockUser(intent.getIntExtra("android.intent.extra.user_handle", -1));
    }

    void onUnlockUser(int userId) {
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "onUserUnlocked " + userId);
        }
        synchronized (this.mUsers) {
            this.mLocalUnlockedUsers.put(userId, true);
        }
        if (userId >= DE_DATABASE_VERSION) {
            syncSharedAccounts(userId);
        }
    }

    private void syncSharedAccounts(int userId) {
        int i = 0;
        Account[] sharedAccounts = getSharedAccountsAsUser(userId);
        if (sharedAccounts != null && sharedAccounts.length != 0) {
            int parentUserId;
            Account[] accounts = getAccountsAsUser(null, userId, this.mContext.getOpPackageName());
            if (UserManager.isSplitSystemUser()) {
                parentUserId = getUserManager().getUserInfo(userId).restrictedProfileParentId;
            } else {
                parentUserId = 0;
            }
            if (parentUserId < 0) {
                Log.w(TAG, "User " + userId + " has shared accounts, but no parent user");
                return;
            }
            int length = sharedAccounts.length;
            while (i < length) {
                Account sa = sharedAccounts[i];
                if (!ArrayUtils.contains(accounts, sa)) {
                    copyAccountToUser(null, sa, parentUserId, userId);
                }
                i += DE_DATABASE_VERSION;
            }
        }
    }

    public /* bridge */ /* synthetic */ void onServiceChanged(Object desc, int userId, boolean removed) {
        onServiceChanged((AuthenticatorDescription) desc, userId, removed);
    }

    public void onServiceChanged(AuthenticatorDescription desc, int userId, boolean removed) {
        validateAccountsInternal(getUserAccounts(userId), false);
    }

    public String getPassword(Account account) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "getPassword, caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
        }
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        int userId = UserHandle.getCallingUserId();
        if (isAccountManagedByCaller(account.type, callingUid, userId)) {
            long identityToken = clearCallingIdentity();
            try {
                String readPasswordInternal = readPasswordInternal(getUserAccounts(userId), account);
                return readPasswordInternal;
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            throw new SecurityException(String.format("uid %s cannot get secrets for accounts of type: %s", new Object[]{Integer.valueOf(callingUid), account.type}));
        }
    }

    private String readPasswordInternal(UserAccounts accounts, Account account) {
        if (account == null) {
            return null;
        }
        if (isLocalUnlockedUser(accounts.userId)) {
            String findAccountPasswordByNameAndType;
            synchronized (accounts.cacheLock) {
                findAccountPasswordByNameAndType = CeDatabaseHelper.findAccountPasswordByNameAndType(accounts.openHelper.getReadableDatabaseUserIsUnlocked(), account.name, account.type);
            }
            return findAccountPasswordByNameAndType;
        }
        Log.w(TAG, "Password is not available - user " + accounts.userId + " data is locked");
        return null;
    }

    public String getPreviousName(Account account) {
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "getPreviousName, caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
        }
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        int userId = UserHandle.getCallingUserId();
        long identityToken = clearCallingIdentity();
        try {
            String readPreviousNameInternal = readPreviousNameInternal(getUserAccounts(userId), account);
            return readPreviousNameInternal;
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    private String readPreviousNameInternal(UserAccounts accounts, Account account) {
        Throwable th;
        if (account == null) {
            return null;
        }
        synchronized (accounts.cacheLock) {
            AtomicReference<String> previousNameRef = (AtomicReference) accounts.previousNameCache.get(account);
            if (previousNameRef == null) {
                SQLiteDatabase db = accounts.openHelper.getReadableDatabase();
                String str = TABLE_ACCOUNTS;
                String[] strArr = new String[DE_DATABASE_VERSION];
                strArr[0] = ACCOUNTS_PREVIOUS_NAME;
                Cursor cursor = db.query(str, strArr, "name=? AND type=?", new String[]{account.name, account.type}, null, null, null);
                try {
                    if (cursor.moveToNext()) {
                        String previousName = cursor.getString(0);
                        AtomicReference<String> previousNameRef2 = new AtomicReference(previousName);
                        try {
                            accounts.previousNameCache.put(account, previousNameRef2);
                            cursor.close();
                            return previousName;
                        } catch (Throwable th2) {
                            th = th2;
                            previousNameRef = previousNameRef2;
                            cursor.close();
                            throw th;
                        }
                    }
                    cursor.close();
                    return null;
                } catch (Throwable th3) {
                    th = th3;
                    cursor.close();
                    throw th;
                }
            }
            str = (String) previousNameRef.get();
            return str;
        }
    }

    public String getUserData(Account account, String key) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, String.format("getUserData( callerUid: %s, pid: %s", new Object[]{Integer.valueOf(callingUid), Integer.valueOf(Binder.getCallingPid())}));
        }
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (key == null) {
            throw new IllegalArgumentException("key is null");
        } else {
            int userId = UserHandle.getCallingUserId();
            if (!isAccountManagedByCaller(account.type, callingUid, userId)) {
                throw new SecurityException(String.format("uid %s cannot get user data for accounts of type: %s", new Object[]{Integer.valueOf(callingUid), account.type}));
            } else if (isLocalUnlockedUser(userId)) {
                long identityToken = clearCallingIdentity();
                try {
                    UserAccounts accounts = getUserAccounts(userId);
                    synchronized (accounts.cacheLock) {
                        if (accountExistsCacheLocked(accounts, account)) {
                            String readUserDataInternalLocked = readUserDataInternalLocked(accounts, account, key);
                            restoreCallingIdentity(identityToken);
                            return readUserDataInternalLocked;
                        }
                        return null;
                    }
                } finally {
                    restoreCallingIdentity(identityToken);
                }
            } else {
                Log.w(TAG, "User " + userId + " data is locked. callingUid " + callingUid);
                return null;
            }
        }
    }

    public AuthenticatorDescription[] getAuthenticatorTypes(int userId) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "getAuthenticatorTypes: for user id " + userId + " caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (isCrossUser(callingUid, userId)) {
            throw new SecurityException(String.format("User %s tying to get authenticator types for %s", new Object[]{Integer.valueOf(UserHandle.getCallingUserId()), Integer.valueOf(userId)}));
        }
        long identityToken = clearCallingIdentity();
        try {
            AuthenticatorDescription[] authenticatorTypesInternal = getAuthenticatorTypesInternal(userId);
            return authenticatorTypesInternal;
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    private AuthenticatorDescription[] getAuthenticatorTypesInternal(int userId) {
        Collection<ServiceInfo<AuthenticatorDescription>> authenticatorCollection = this.mAuthenticatorCache.getAllServices(userId);
        AuthenticatorDescription[] types = new AuthenticatorDescription[authenticatorCollection.size()];
        int i = 0;
        for (ServiceInfo<AuthenticatorDescription> authenticator : authenticatorCollection) {
            types[i] = (AuthenticatorDescription) authenticator.type;
            i += DE_DATABASE_VERSION;
        }
        return types;
    }

    private boolean isCrossUser(int callingUid, int userId) {
        if (userId == UserHandle.getCallingUserId() || callingUid == Process.myUid() || this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) {
            return false;
        }
        return true;
    }

    public boolean addAccountExplicitly(Account account, String password, Bundle extras) {
        Bundle.setDefusable(extras, true);
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "addAccountExplicitly, caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        int userId = UserHandle.getCallingUserId();
        if (isAccountManagedByCaller(account.type, callingUid, userId)) {
            long identityToken = clearCallingIdentity();
            try {
                boolean addAccountInternal = addAccountInternal(getUserAccounts(userId), account, password, extras, callingUid);
                return addAccountInternal;
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            throw new SecurityException(String.format("uid %s cannot explicitly add accounts of type: %s", new Object[]{Integer.valueOf(callingUid), account.type}));
        }
    }

    public void copyAccountToUser(IAccountManagerResponse response, Account account, int userFrom, int userTo) {
        if (isCrossUser(Binder.getCallingUid(), -1)) {
            throw new SecurityException("Calling copyAccountToUser requires android.permission.INTERACT_ACROSS_USERS_FULL");
        }
        UserAccounts fromAccounts = getUserAccounts(userFrom);
        UserAccounts toAccounts = getUserAccounts(userTo);
        if (fromAccounts == null || toAccounts == null) {
            if (response != null) {
                Bundle result = new Bundle();
                result.putBoolean("booleanResult", false);
                try {
                    response.onResult(result);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Failed to report error back to the client." + e);
                }
            }
            return;
        }
        Slog.d(TAG, "Copying account  from user " + userFrom + " to user " + userTo);
        long identityToken = clearCallingIdentity();
        try {
            new AnonymousClass3(this, this, fromAccounts, response, account.type, false, false, account.name, false, account, response, toAccounts, userFrom).bind();
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    public boolean accountAuthenticated(Account account) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Object[] objArr = new Object[DE_DATABASE_VERSION];
            objArr[0] = Integer.valueOf(callingUid);
            Log.v(TAG, String.format("accountAuthenticated( callerUid: %s)", objArr));
        }
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        int userId = UserHandle.getCallingUserId();
        if (!isAccountManagedByCaller(account.type, callingUid, userId)) {
            throw new SecurityException(String.format("uid %s cannot notify authentication for accounts of type: %s", new Object[]{Integer.valueOf(callingUid), account.type}));
        } else if (!canUserModifyAccounts(userId, callingUid) || !canUserModifyAccountsForType(userId, account.type, callingUid)) {
            return false;
        } else {
            long identityToken = clearCallingIdentity();
            try {
                UserAccounts accounts = getUserAccounts(userId);
                boolean updateLastAuthenticatedTime = updateLastAuthenticatedTime(account);
                return updateLastAuthenticatedTime;
            } finally {
                restoreCallingIdentity(identityToken);
            }
        }
    }

    private boolean updateLastAuthenticatedTime(Account account) {
        UserAccounts accounts = getUserAccountsForCaller();
        synchronized (accounts.cacheLock) {
            ContentValues values = new ContentValues();
            values.put(ACCOUNTS_LAST_AUTHENTICATE_TIME_EPOCH_MILLIS, Long.valueOf(System.currentTimeMillis()));
            if (accounts.openHelper.getWritableDatabase().update(TABLE_ACCOUNTS, values, "name=? AND type=?", new String[]{account.name, account.type}) > 0) {
                return true;
            }
            return false;
        }
    }

    private void completeCloningAccount(IAccountManagerResponse response, Bundle accountCredentials, Account account, UserAccounts targetUser, int parentUserId) {
        Bundle.setDefusable(accountCredentials, true);
        long id = clearCallingIdentity();
        try {
            new AnonymousClass4(this, this, targetUser, response, account.type, false, false, account.name, false, account, parentUserId, accountCredentials).bind();
        } finally {
            restoreCallingIdentity(id);
        }
    }

    private boolean addAccountInternal(UserAccounts accounts, Account account, String password, Bundle extras, int callingUid) {
        Bundle.setDefusable(extras, true);
        if (account == null) {
            return false;
        }
        if (isLocalUnlockedUser(accounts.userId)) {
            synchronized (accounts.cacheLock) {
                SQLiteDatabase db = accounts.openHelper.getWritableDatabaseUserIsUnlocked();
                db.beginTransaction();
                try {
                    if (DatabaseUtils.longForQuery(db, "select count(*) from ceDb.accounts WHERE name=? AND type=?", new String[]{account.name, account.type}) > 0) {
                        Log.w(TAG, "insertAccountIntoDatabase, skipping since the account already exists");
                        db.endTransaction();
                        return false;
                    }
                    ContentValues values = new ContentValues();
                    values.put(ACCOUNTS_NAME, account.name);
                    values.put(AUTHTOKENS_TYPE, account.type);
                    values.put(ACCOUNTS_PASSWORD, password);
                    long accountId = db.insert(CE_TABLE_ACCOUNTS, ACCOUNTS_NAME, values);
                    if (accountId < 0) {
                        Log.w(TAG, "insertAccountIntoDatabase, skipping the DB insert failed");
                        db.endTransaction();
                        return false;
                    }
                    Log.e(TAG, "insert CE accountId = " + accountId);
                    values = new ContentValues();
                    values.put(SHARED_ACCOUNTS_ID, Long.valueOf(accountId));
                    values.put(ACCOUNTS_NAME, account.name);
                    values.put(AUTHTOKENS_TYPE, account.type);
                    values.put(ACCOUNTS_LAST_AUTHENTICATE_TIME_EPOCH_MILLIS, Long.valueOf(System.currentTimeMillis()));
                    if (db.insert(TABLE_ACCOUNTS, ACCOUNTS_NAME, values) < 0) {
                        Log.w(TAG, "insertAccountIntoDatabase: " + account + ", skipping the DB insert failed");
                        db.endTransaction();
                        return false;
                    }
                    Log.e(TAG, "insert DE accountId = " + accountId);
                    if (extras != null) {
                        for (String key : extras.keySet()) {
                            if (insertExtraLocked(db, accountId, key, extras.getString(key)) < 0) {
                                Log.w(TAG, "insertAccountIntoDatabase, skipping since insertExtra failed for key");
                                db.endTransaction();
                                return false;
                            }
                        }
                    }
                    db.setTransactionSuccessful();
                    logRecord(db, DebugDbHelper.ACTION_ACCOUNT_ADD, TABLE_ACCOUNTS, accountId, accounts, callingUid);
                    insertAccountIntoCacheLocked(accounts, account);
                    db.endTransaction();
                    sendAccountsChangedBroadcast(accounts.userId);
                    if (getUserManager().getUserInfo(accounts.userId).canHaveProfile()) {
                        addAccountToLinkedRestrictedUsers(account, accounts.userId);
                    }
                    return true;
                } catch (Throwable th) {
                    db.endTransaction();
                }
            }
        } else {
            Log.w(TAG, "Account " + account + " cannot be added - user " + accounts.userId + " is locked. callingUid=" + callingUid);
            return false;
        }
    }

    private boolean isLocalUnlockedUser(int userId) {
        boolean z;
        synchronized (this.mUsers) {
            z = this.mLocalUnlockedUsers.get(userId);
        }
        return z;
    }

    private void addAccountToLinkedRestrictedUsers(Account account, int parentUserId) {
        for (UserInfo user : getUserManager().getUsers()) {
            if (user.isRestricted() && parentUserId == user.restrictedProfileParentId) {
                addSharedAccountAsUser(account, user.id);
                if (isLocalUnlockedUser(user.id)) {
                    this.mMessageHandler.sendMessage(this.mMessageHandler.obtainMessage(MESSAGE_COPY_SHARED_ACCOUNT, parentUserId, user.id, account));
                }
            }
        }
    }

    private long insertExtraLocked(SQLiteDatabase db, long accountId, String key, String value) {
        ContentValues values = new ContentValues();
        values.put(META_KEY, key);
        values.put(GRANTS_ACCOUNTS_ID, Long.valueOf(accountId));
        values.put(META_VALUE, value);
        return db.insert(CE_TABLE_EXTRAS, META_KEY, values);
    }

    public void hasFeatures(IAccountManagerResponse response, Account account, String[] features, String opPackageName) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "hasFeatures, response " + response + ", features " + stringArrayToString(features) + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        } else if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (features == null) {
            throw new IllegalArgumentException("features is null");
        } else {
            int userId = UserHandle.getCallingUserId();
            checkReadAccountsPermitted(callingUid, account.type, userId, opPackageName);
            long identityToken = clearCallingIdentity();
            try {
                new TestFeaturesSession(this, getUserAccounts(userId), response, account, features).bind();
            } finally {
                restoreCallingIdentity(identityToken);
            }
        }
    }

    public void renameAccount(IAccountManagerResponse response, Account accountToRename, String newName) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "renameAccount, caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (accountToRename == null) {
            throw new IllegalArgumentException("account is null");
        }
        int userId = UserHandle.getCallingUserId();
        if (isAccountManagedByCaller(accountToRename.type, callingUid, userId)) {
            long identityToken = clearCallingIdentity();
            try {
                Account resultingAccount = renameAccountInternal(getUserAccounts(userId), accountToRename, newName);
                Bundle result = new Bundle();
                result.putString("authAccount", resultingAccount.name);
                result.putString("accountType", resultingAccount.type);
                response.onResult(result);
            } catch (RemoteException e) {
                Log.w(TAG, e.getMessage());
            } catch (Throwable th) {
                restoreCallingIdentity(identityToken);
            }
            restoreCallingIdentity(identityToken);
            return;
        }
        throw new SecurityException(String.format("uid %s cannot rename accounts of type: %s", new Object[]{Integer.valueOf(callingUid), accountToRename.type}));
    }

    private Account renameAccountInternal(UserAccounts accounts, Account accountToRename, String newName) {
        int parentUserId;
        Account resultAccount = null;
        cancelNotification(getSigninRequiredNotificationId(accounts, accountToRename).intValue(), new UserHandle(accounts.userId));
        synchronized (accounts.credentialsPermissionNotificationIds) {
            for (Pair<Pair<Account, String>, Integer> pair : accounts.credentialsPermissionNotificationIds.keySet()) {
                if (accountToRename.equals(((Pair) pair.first).first)) {
                    cancelNotification(((Integer) accounts.credentialsPermissionNotificationIds.get(pair)).intValue(), new UserHandle(accounts.userId));
                }
            }
        }
        synchronized (accounts.cacheLock) {
            SQLiteDatabase db = accounts.openHelper.getWritableDatabaseUserIsUnlocked();
            db.beginTransaction();
            boolean isSuccessful = false;
            Account account = new Account(newName, accountToRename.type);
            try {
                long accountId = getAccountIdLocked(db, accountToRename);
                if (accountId >= 0) {
                    ContentValues values = new ContentValues();
                    values.put(ACCOUNTS_NAME, newName);
                    String[] argsAccountId = new String[DE_DATABASE_VERSION];
                    argsAccountId[0] = String.valueOf(accountId);
                    db.update(CE_TABLE_ACCOUNTS, values, "_id=?", argsAccountId);
                    values.put(ACCOUNTS_PREVIOUS_NAME, accountToRename.name);
                    db.update(TABLE_ACCOUNTS, values, "_id=?", argsAccountId);
                    db.setTransactionSuccessful();
                    isSuccessful = true;
                    logRecord(db, DebugDbHelper.ACTION_ACCOUNT_RENAME, TABLE_ACCOUNTS, accountId, accounts);
                }
                db.endTransaction();
                if (isSuccessful) {
                    insertAccountIntoCacheLocked(accounts, account);
                    tmpData = (HashMap) accounts.userDataCache.get(accountToRename);
                    tmpTokens = (HashMap) accounts.authTokenCache.get(accountToRename);
                    removeAccountFromCacheLocked(accounts, accountToRename);
                    accounts.userDataCache.put(account, tmpData);
                    accounts.authTokenCache.put(account, tmpTokens);
                    accounts.previousNameCache.put(account, new AtomicReference(accountToRename.name));
                    resultAccount = account;
                    parentUserId = accounts.userId;
                    if (canHaveProfile(parentUserId)) {
                        for (UserInfo user : getUserManager().getUsers(true)) {
                            if (user.isRestricted() && user.restrictedProfileParentId == parentUserId) {
                                renameSharedAccountAsUser(accountToRename, newName, user.id);
                            }
                        }
                    }
                    sendAccountsChangedBroadcast(accounts.userId);
                }
            } catch (Throwable th) {
                db.endTransaction();
                if (null != null) {
                    HashMap<String, String> tmpData;
                    HashMap<String, String> tmpTokens;
                    insertAccountIntoCacheLocked(accounts, account);
                    tmpData = (HashMap) accounts.userDataCache.get(accountToRename);
                    tmpTokens = (HashMap) accounts.authTokenCache.get(accountToRename);
                    removeAccountFromCacheLocked(accounts, accountToRename);
                    accounts.userDataCache.put(account, tmpData);
                    accounts.authTokenCache.put(account, tmpTokens);
                    accounts.previousNameCache.put(account, new AtomicReference(accountToRename.name));
                    resultAccount = account;
                    parentUserId = accounts.userId;
                    if (canHaveProfile(parentUserId)) {
                        for (UserInfo user2 : getUserManager().getUsers(true)) {
                            if (user2.isRestricted() && user2.restrictedProfileParentId == parentUserId) {
                                renameSharedAccountAsUser(accountToRename, newName, user2.id);
                            }
                        }
                    }
                    sendAccountsChangedBroadcast(accounts.userId);
                }
            }
        }
        return resultAccount;
    }

    private boolean canHaveProfile(int parentUserId) {
        UserInfo userInfo = getUserManager().getUserInfo(parentUserId);
        return userInfo != null ? userInfo.canHaveProfile() : false;
    }

    public void removeAccount(IAccountManagerResponse response, Account account, boolean expectActivityLaunch) {
        removeAccountAsUser(response, account, expectActivityLaunch, UserHandle.getCallingUserId());
    }

    public void removeAccountAsUser(IAccountManagerResponse response, Account account, boolean expectActivityLaunch, int userId) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "removeAccount, response " + response + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid() + ", for user id " + userId);
        }
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        } else if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (isCrossUser(callingUid, userId)) {
            throw new SecurityException(String.format("User %s tying remove account for %s", new Object[]{Integer.valueOf(UserHandle.getCallingUserId()), Integer.valueOf(userId)}));
        } else {
            UserHandle user = UserHandle.of(userId);
            if (!isAccountManagedByCaller(account.type, callingUid, user.getIdentifier()) && !isSystemUid(callingUid)) {
                throw new SecurityException(String.format("uid %s cannot remove accounts of type: %s", new Object[]{Integer.valueOf(callingUid), account.type}));
            } else if (canUserModifyAccounts(userId, callingUid)) {
                if (canUserModifyAccountsForType(userId, account.type, callingUid)) {
                    long identityToken = clearCallingIdentity();
                    UserAccounts accounts = getUserAccounts(userId);
                    cancelNotification(getSigninRequiredNotificationId(accounts, account).intValue(), user);
                    synchronized (accounts.credentialsPermissionNotificationIds) {
                        for (Pair<Pair<Account, String>, Integer> pair : accounts.credentialsPermissionNotificationIds.keySet()) {
                            if (account.equals(((Pair) pair.first).first)) {
                                cancelNotification(((Integer) accounts.credentialsPermissionNotificationIds.get(pair)).intValue(), user);
                            }
                        }
                    }
                    logRecord(accounts, DebugDbHelper.ACTION_CALLED_ACCOUNT_REMOVE, TABLE_ACCOUNTS);
                    try {
                        new RemoveAccountSession(this, accounts, response, account, expectActivityLaunch).bind();
                    } finally {
                        restoreCallingIdentity(identityToken);
                    }
                } else {
                    try {
                        response.onError(H.KEYGUARD_DISMISS_DONE, "User cannot modify accounts of this type (policy).");
                    } catch (RemoteException e) {
                    }
                }
            } else {
                try {
                    response.onError(100, "User cannot modify accounts");
                } catch (RemoteException e2) {
                }
            }
        }
    }

    public boolean removeAccountExplicitly(Account account) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "removeAccountExplicitly, caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        int userId = Binder.getCallingUserHandle().getIdentifier();
        if (account == null) {
            Log.e(TAG, "account is null");
            return false;
        } else if (isAccountManagedByCaller(account.type, callingUid, userId)) {
            UserAccounts accounts = getUserAccountsForCaller();
            logRecord(accounts, DebugDbHelper.ACTION_CALLED_ACCOUNT_REMOVE, TABLE_ACCOUNTS);
            long identityToken = clearCallingIdentity();
            try {
                boolean removeAccountInternal = removeAccountInternal(accounts, account, callingUid);
                return removeAccountInternal;
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            throw new SecurityException(String.format("uid %s cannot explicitly add accounts of type: %s", new Object[]{Integer.valueOf(callingUid), account.type}));
        }
    }

    protected void removeAccountInternal(Account account) {
        removeAccountInternal(getUserAccountsForCaller(), account, getCallingUid());
    }

    private boolean removeAccountInternal(UserAccounts accounts, Account account, int callingUid) {
        boolean userUnlocked = isLocalUnlockedUser(accounts.userId);
        if (!userUnlocked) {
            Slog.i(TAG, "Removing account " + account + " while user " + accounts.userId + " is still locked. CE data will be removed later");
        }
        synchronized (accounts.cacheLock) {
            SQLiteDatabase db;
            if (userUnlocked) {
                db = accounts.openHelper.getWritableDatabaseUserIsUnlocked();
            } else {
                db = accounts.openHelper.getWritableDatabase();
            }
            long accountId = getAccountIdLocked(db, account);
            db.beginTransaction();
            try {
                String action;
                r10 = new String[2];
                r10[0] = account.name;
                r10[DE_DATABASE_VERSION] = account.type;
                int deleted = db.delete(TABLE_ACCOUNTS, "name=? AND type=?", r10);
                if (userUnlocked) {
                    r10 = new String[2];
                    r10[0] = account.name;
                    r10[DE_DATABASE_VERSION] = account.type;
                    deleted = db.delete(CE_TABLE_ACCOUNTS, "name=? AND type=?", r10);
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                removeAccountFromCacheLocked(accounts, account);
                sendAccountsChangedBroadcast(accounts.userId);
                if (userUnlocked) {
                    action = DebugDbHelper.ACTION_ACCOUNT_REMOVE;
                } else {
                    action = DebugDbHelper.ACTION_ACCOUNT_REMOVE_DE;
                }
                logRecord(db, action, TABLE_ACCOUNTS, accountId, accounts);
            } catch (Throwable th) {
                db.endTransaction();
            }
        }
        long id = Binder.clearCallingIdentity();
        try {
            int parentUserId = accounts.userId;
            if (canHaveProfile(parentUserId)) {
                for (UserInfo user : getUserManager().getUsers(true)) {
                    if (user.isRestricted() && parentUserId == user.restrictedProfileParentId) {
                        removeSharedAccountAsUser(account, user.id, callingUid);
                    }
                }
            }
            Binder.restoreCallingIdentity(id);
            return deleted > 0;
        } catch (Throwable th2) {
            Binder.restoreCallingIdentity(id);
        }
    }

    public void invalidateAuthToken(String accountType, String authToken) {
        int callerUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "invalidateAuthToken , caller's uid " + callerUid + ", pid " + Binder.getCallingPid());
        }
        if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        } else if (authToken == null) {
            throw new IllegalArgumentException("authToken is null");
        } else {
            int userId = UserHandle.getCallingUserId();
            long identityToken = clearCallingIdentity();
            try {
                UserAccounts accounts = getUserAccounts(userId);
                synchronized (accounts.cacheLock) {
                    SQLiteDatabase db = accounts.openHelper.getWritableDatabaseUserIsUnlocked();
                    db.beginTransaction();
                    try {
                        invalidateAuthTokenLocked(accounts, db, accountType, authToken);
                        invalidateCustomTokenLocked(accounts, accountType, authToken);
                        db.setTransactionSuccessful();
                        db.endTransaction();
                    } catch (Throwable th) {
                        db.endTransaction();
                    }
                }
            } finally {
                restoreCallingIdentity(identityToken);
            }
        }
    }

    private void invalidateCustomTokenLocked(UserAccounts accounts, String accountType, String authToken) {
        if (authToken != null && accountType != null) {
            accounts.accountTokenCaches.remove(accountType, authToken);
        }
    }

    private void invalidateAuthTokenLocked(UserAccounts accounts, SQLiteDatabase db, String accountType, String authToken) {
        if (authToken != null && accountType != null) {
            Cursor cursor = db.rawQuery("SELECT ceDb.authtokens._id, ceDb.accounts.name, ceDb.authtokens.type FROM ceDb.accounts JOIN ceDb.authtokens ON ceDb.accounts._id = ceDb.authtokens.accounts_id WHERE ceDb.authtokens.authtoken = ? AND ceDb.accounts.type = ?", new String[]{authToken, accountType});
            while (cursor.moveToNext()) {
                try {
                    long authTokenId = cursor.getLong(0);
                    String accountName = cursor.getString(DE_DATABASE_VERSION);
                    String authTokenType = cursor.getString(2);
                    db.delete(CE_TABLE_AUTHTOKENS, "_id=" + authTokenId, null);
                    writeAuthTokenIntoCacheLocked(accounts, db, new Account(accountName, accountType), authTokenType, null);
                } finally {
                    cursor.close();
                }
            }
        }
    }

    private void saveCachedToken(UserAccounts accounts, Account account, String callerPkg, byte[] callerSigDigest, String tokenType, String token, long expiryMillis) {
        if (account != null && tokenType != null && callerPkg != null && callerSigDigest != null) {
            cancelNotification(getSigninRequiredNotificationId(accounts, account).intValue(), UserHandle.of(accounts.userId));
            synchronized (accounts.cacheLock) {
                accounts.accountTokenCaches.put(account, token, tokenType, callerPkg, callerSigDigest, expiryMillis);
            }
        }
    }

    private boolean saveAuthTokenToDatabase(UserAccounts accounts, Account account, String type, String authToken) {
        if (account == null || type == null) {
            return false;
        }
        cancelNotification(getSigninRequiredNotificationId(accounts, account).intValue(), UserHandle.of(accounts.userId));
        synchronized (accounts.cacheLock) {
            SQLiteDatabase db = accounts.openHelper.getWritableDatabaseUserIsUnlocked();
            db.beginTransaction();
            try {
                long accountId = getAccountIdLocked(db, account);
                if (accountId < 0) {
                    db.endTransaction();
                    return false;
                }
                String str = CE_TABLE_AUTHTOKENS;
                String str2 = "accounts_id=" + accountId + " AND " + AUTHTOKENS_TYPE + "=?";
                String[] strArr = new String[DE_DATABASE_VERSION];
                strArr[0] = type;
                db.delete(str, str2, strArr);
                ContentValues values = new ContentValues();
                values.put(GRANTS_ACCOUNTS_ID, Long.valueOf(accountId));
                values.put(AUTHTOKENS_TYPE, type);
                values.put(AUTHTOKENS_AUTHTOKEN, authToken);
                if (db.insert(CE_TABLE_AUTHTOKENS, AUTHTOKENS_AUTHTOKEN, values) >= 0) {
                    db.setTransactionSuccessful();
                    writeAuthTokenIntoCacheLocked(accounts, db, account, type, authToken);
                    db.endTransaction();
                    return true;
                }
                db.endTransaction();
                return false;
            } catch (Throwable th) {
                db.endTransaction();
            }
        }
    }

    public String peekAuthToken(Account account, String authTokenType) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "peekAuthToken , authTokenType " + authTokenType + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (authTokenType == null) {
            throw new IllegalArgumentException("authTokenType is null");
        } else {
            int userId = UserHandle.getCallingUserId();
            if (!isAccountManagedByCaller(account.type, callingUid, userId)) {
                throw new SecurityException(String.format("uid %s cannot peek the authtokens associated with accounts of type: %s", new Object[]{Integer.valueOf(callingUid), account.type}));
            } else if (isLocalUnlockedUser(userId)) {
                long identityToken = clearCallingIdentity();
                try {
                    String readAuthTokenInternal = readAuthTokenInternal(getUserAccounts(userId), account, authTokenType);
                    return readAuthTokenInternal;
                } finally {
                    restoreCallingIdentity(identityToken);
                }
            } else {
                Log.w(TAG, "Authtoken not available - user " + userId + " data is locked. callingUid " + callingUid);
                return null;
            }
        }
    }

    public void setAuthToken(Account account, String authTokenType, String authToken) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "setAuthToken , authTokenType " + authTokenType + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (authTokenType == null) {
            throw new IllegalArgumentException("authTokenType is null");
        } else {
            int userId = UserHandle.getCallingUserId();
            if (isAccountManagedByCaller(account.type, callingUid, userId)) {
                long identityToken = clearCallingIdentity();
                try {
                    saveAuthTokenToDatabase(getUserAccounts(userId), account, authTokenType, authToken);
                } finally {
                    restoreCallingIdentity(identityToken);
                }
            } else {
                throw new SecurityException(String.format("uid %s cannot set auth tokens associated with accounts of type: %s", new Object[]{Integer.valueOf(callingUid), account.type}));
            }
        }
    }

    public void setPassword(Account account, String password) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "setAuthToken , caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        int userId = UserHandle.getCallingUserId();
        if (isAccountManagedByCaller(account.type, callingUid, userId)) {
            long identityToken = clearCallingIdentity();
            try {
                setPasswordInternal(getUserAccounts(userId), account, password, callingUid);
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            throw new SecurityException(String.format("uid %s cannot set secrets for accounts of type: %s", new Object[]{Integer.valueOf(callingUid), account.type}));
        }
    }

    private void setPasswordInternal(UserAccounts accounts, Account account, String password, int callingUid) {
        if (account != null) {
            synchronized (accounts.cacheLock) {
                SQLiteDatabase db = accounts.openHelper.getWritableDatabaseUserIsUnlocked();
                db.beginTransaction();
                try {
                    ContentValues values = new ContentValues();
                    values.put(ACCOUNTS_PASSWORD, password);
                    long accountId = getAccountIdLocked(db, account);
                    if (accountId >= 0) {
                        String action;
                        String[] argsAccountId = new String[DE_DATABASE_VERSION];
                        argsAccountId[0] = String.valueOf(accountId);
                        db.update(CE_TABLE_ACCOUNTS, values, "_id=?", argsAccountId);
                        db.delete(CE_TABLE_AUTHTOKENS, "accounts_id=?", argsAccountId);
                        accounts.authTokenCache.remove(account);
                        accounts.accountTokenCaches.remove(account);
                        db.setTransactionSuccessful();
                        if (password == null || password.length() == 0) {
                            action = DebugDbHelper.ACTION_CLEAR_PASSWORD;
                        } else {
                            action = DebugDbHelper.ACTION_SET_PASSWORD;
                        }
                        logRecord(db, action, TABLE_ACCOUNTS, accountId, accounts, callingUid);
                    }
                    db.endTransaction();
                    sendAccountsChangedBroadcast(accounts.userId);
                } catch (Throwable th) {
                    db.endTransaction();
                }
            }
        }
    }

    private void sendAccountsChangedBroadcast(int userId) {
        Log.i(TAG, "the accounts changed, sending broadcast of " + ACCOUNTS_CHANGED_INTENT.getAction());
        this.mContext.sendBroadcastAsUser(ACCOUNTS_CHANGED_INTENT, new UserHandle(userId));
    }

    public void clearPassword(Account account) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "clearPassword , caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (account == null) {
            throw new IllegalArgumentException("account is null");
        }
        int userId = UserHandle.getCallingUserId();
        if (isAccountManagedByCaller(account.type, callingUid, userId)) {
            long identityToken = clearCallingIdentity();
            try {
                setPasswordInternal(getUserAccounts(userId), account, null, callingUid);
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            throw new SecurityException(String.format("uid %s cannot clear passwords for accounts of type: %s", new Object[]{Integer.valueOf(callingUid), account.type}));
        }
    }

    public void setUserData(Account account, String key, String value) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "setUserData: " + account + ", key " + key + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        } else if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else {
            int userId = UserHandle.getCallingUserId();
            if (isAccountManagedByCaller(account.type, callingUid, userId)) {
                long identityToken = clearCallingIdentity();
                try {
                    UserAccounts accounts = getUserAccounts(userId);
                    synchronized (accounts.cacheLock) {
                        if (accountExistsCacheLocked(accounts, account)) {
                            setUserdataInternalLocked(accounts, account, key, value);
                            restoreCallingIdentity(identityToken);
                            return;
                        }
                    }
                } finally {
                    restoreCallingIdentity(identityToken);
                }
            } else {
                throw new SecurityException(String.format("uid %s cannot set user data for accounts of type: %s", new Object[]{Integer.valueOf(callingUid), account.type}));
            }
        }
    }

    private boolean accountExistsCacheLocked(UserAccounts accounts, Account account) {
        if (accounts.accountCache.containsKey(account.type)) {
            Account[] accountArr = (Account[]) accounts.accountCache.get(account.type);
            int length = accountArr.length;
            for (int i = 0; i < length; i += DE_DATABASE_VERSION) {
                if (accountArr[i].name.equals(account.name)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void setUserdataInternalLocked(UserAccounts accounts, Account account, String key, String value) {
        if (account != null && key != null) {
            SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                long accountId = getAccountIdLocked(db, account);
                if (accountId >= 0) {
                    long extrasId = getExtrasIdLocked(db, accountId, key);
                    if (extrasId >= 0) {
                        ContentValues values = new ContentValues();
                        values.put(META_VALUE, value);
                        if (DE_DATABASE_VERSION != db.update(TABLE_EXTRAS, values, "_id=" + extrasId, null)) {
                            db.endTransaction();
                            return;
                        }
                    } else if (insertExtraLocked(db, accountId, key, value) < 0) {
                        db.endTransaction();
                        return;
                    }
                    writeUserDataIntoCacheLocked(accounts, db, account, key, value);
                    db.setTransactionSuccessful();
                    db.endTransaction();
                }
            } finally {
                db.endTransaction();
            }
        }
    }

    private void onResult(IAccountManagerResponse response, Bundle result) {
        if (result == null) {
            Log.e(TAG, "the result is unexpectedly null", new Exception());
        }
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
        }
        try {
            response.onResult(result);
        } catch (RemoteException e) {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "failure while notifying response", e);
            }
        }
    }

    public void getAuthTokenLabel(IAccountManagerResponse response, String accountType, String authTokenType) throws RemoteException {
        if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        } else if (authTokenType == null) {
            throw new IllegalArgumentException("authTokenType is null");
        } else {
            int callingUid = getCallingUid();
            clearCallingIdentity();
            if (callingUid != ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
                throw new SecurityException("can only call from system");
            }
            int userId = UserHandle.getUserId(callingUid);
            long identityToken = clearCallingIdentity();
            try {
                new AnonymousClass5(this, this, getUserAccounts(userId), response, accountType, false, false, null, false, accountType, authTokenType).bind();
            } finally {
                restoreCallingIdentity(identityToken);
            }
        }
    }

    public void getAuthToken(IAccountManagerResponse response, Account account, String authTokenType, boolean notifyOnAuthFailure, boolean expectActivityLaunch, Bundle loginOptions) {
        Bundle.setDefusable(loginOptions, true);
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "getAuthToken , response " + response + ", authTokenType " + authTokenType + ", notifyOnAuthFailure " + notifyOnAuthFailure + ", expectActivityLaunch " + expectActivityLaunch + ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
        }
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        } else if (account == null) {
            try {
                Slog.w(TAG, "getAuthToken called with null account");
                response.onError(7, "account is null");
            } catch (RemoteException e) {
                Slog.w(TAG, "Failed to report error back to the client." + e);
            }
        } else if (authTokenType == null) {
            Slog.w(TAG, "getAuthToken called with null authTokenType");
            response.onError(7, "authTokenType is null");
        } else {
            int userId = UserHandle.getCallingUserId();
            long ident = Binder.clearCallingIdentity();
            try {
                UserAccounts accounts = getUserAccounts(userId);
                ServiceInfo<AuthenticatorDescription> authenticatorInfo = this.mAuthenticatorCache.getServiceInfo(AuthenticatorDescription.newKey(account.type), accounts.userId);
                boolean z = authenticatorInfo != null ? ((AuthenticatorDescription) authenticatorInfo.type).customTokens : false;
                int callerUid = Binder.getCallingUid();
                boolean permissionIsGranted = !z ? permissionIsGranted(account, authTokenType, callerUid, userId) : true;
                String callerPkg = loginOptions.getString("androidPackageName");
                ident = Binder.clearCallingIdentity();
                try {
                    List<String> callerOwnedPackageNames = Arrays.asList(this.mPackageManager.getPackagesForUid(callerUid));
                    if (callerPkg == null || !callerOwnedPackageNames.contains(callerPkg)) {
                        throw new SecurityException(String.format("Uid %s is attempting to illegally masquerade as package %s!", new Object[]{Integer.valueOf(callerUid), callerPkg}));
                    }
                    loginOptions.putInt("callerUid", callerUid);
                    loginOptions.putInt("callerPid", Binder.getCallingPid());
                    if (notifyOnAuthFailure) {
                        loginOptions.putBoolean("notifyOnAuthFailure", true);
                    }
                    long identityToken = clearCallingIdentity();
                    try {
                        Bundle result;
                        byte[] callerPkgSigDigest = calculatePackageSignatureDigest(callerPkg);
                        if (!z && permissionIsGranted) {
                            String authToken = readAuthTokenInternal(accounts, account, authTokenType);
                            if (authToken != null) {
                                result = new Bundle();
                                result.putString(AUTHTOKENS_AUTHTOKEN, authToken);
                                result.putString("authAccount", account.name);
                                result.putString("accountType", account.type);
                                onResult(response, result);
                                return;
                            }
                        }
                        if (z) {
                            String token = readCachedTokenInternal(accounts, account, authTokenType, callerPkg, callerPkgSigDigest);
                            if (token != null) {
                                if (Log.isLoggable(TAG, 2)) {
                                    Log.v(TAG, "getAuthToken: cache hit ofr custom token authenticator.");
                                }
                                result = new Bundle();
                                result.putString(AUTHTOKENS_AUTHTOKEN, token);
                                result.putString("authAccount", account.name);
                                result.putString("accountType", account.type);
                                onResult(response, result);
                                restoreCallingIdentity(identityToken);
                                return;
                            }
                        }
                        new AnonymousClass6(this, this, accounts, response, account.type, expectActivityLaunch, false, account.name, false, loginOptions, account, authTokenType, notifyOnAuthFailure, permissionIsGranted, callerUid, z, callerPkg, callerPkgSigDigest, accounts).bind();
                        restoreCallingIdentity(identityToken);
                    } finally {
                        restoreCallingIdentity(identityToken);
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private byte[] calculatePackageSignatureDigest(String callerPkg) {
        MessageDigest digester;
        try {
            digester = MessageDigest.getInstance("SHA-256");
            Signature[] signatureArr = this.mPackageManager.getPackageInfo(callerPkg, MAX_DEBUG_DB_SIZE).signatures;
            int length = signatureArr.length;
            for (int i = 0; i < length; i += DE_DATABASE_VERSION) {
                digester.update(signatureArr[i].toByteArray());
            }
        } catch (NoSuchAlgorithmException x) {
            Log.wtf(TAG, "SHA-256 should be available", x);
            digester = null;
        } catch (NameNotFoundException e) {
            Log.w(TAG, "Could not find packageinfo for: " + callerPkg);
            digester = null;
        }
        if (digester == null) {
            return null;
        }
        return digester.digest();
    }

    private void createNoCredentialsPermissionNotification(Account account, Intent intent, int userId) {
        int uid = intent.getIntExtra(GRANTS_GRANTEE_UID, -1);
        String authTokenType = intent.getStringExtra("authTokenType");
        Context context = this.mContext;
        Object[] objArr = new Object[DE_DATABASE_VERSION];
        objArr[0] = account.name;
        String titleAndSubtitle = context.getString(17040468, objArr);
        int index = titleAndSubtitle.indexOf(CE_DATABASE_VERSION);
        String title = titleAndSubtitle;
        String subtitle = "";
        if (index > 0) {
            title = titleAndSubtitle.substring(0, index);
            subtitle = titleAndSubtitle.substring(index + DE_DATABASE_VERSION);
        }
        UserHandle user = new UserHandle(userId);
        Context contextForUser = getContextForUser(user);
        Builder contentText = new Builder(contextForUser).setSmallIcon(17301642).setWhen(0).setColor(contextForUser.getColor(17170519)).setContentTitle(title).setContentText(subtitle);
        installNotification(getCredentialPermissionNotificationId(account, authTokenType, uid).intValue(), r16.setContentIntent(PendingIntent.getActivityAsUser(this.mContext, 0, intent, 268435456, null, user)).build(), user);
    }

    private Intent newGrantCredentialsPermissionIntent(Account account, int uid, AccountAuthenticatorResponse response, String authTokenType) {
        Intent intent = new Intent(this.mContext, GrantCredentialsPermissionActivity.class);
        intent.setFlags(268435456);
        intent.addCategory(String.valueOf(getCredentialPermissionNotificationId(account, authTokenType, uid)));
        intent.putExtra("account", account);
        intent.putExtra("authTokenType", authTokenType);
        intent.putExtra("response", response);
        intent.putExtra(GRANTS_GRANTEE_UID, uid);
        return intent;
    }

    private Integer getCredentialPermissionNotificationId(Account account, String authTokenType, int uid) {
        Integer id;
        UserAccounts accounts = getUserAccounts(UserHandle.getUserId(uid));
        synchronized (accounts.credentialsPermissionNotificationIds) {
            Pair<Pair<Account, String>, Integer> key = new Pair(new Pair(account, authTokenType), Integer.valueOf(uid));
            id = (Integer) accounts.credentialsPermissionNotificationIds.get(key);
            if (id == null) {
                id = Integer.valueOf(this.mNotificationIds.incrementAndGet());
                accounts.credentialsPermissionNotificationIds.put(key, id);
            }
        }
        return id;
    }

    private Integer getSigninRequiredNotificationId(UserAccounts accounts, Account account) {
        Integer id;
        synchronized (accounts.signinRequiredNotificationIds) {
            id = (Integer) accounts.signinRequiredNotificationIds.get(account);
            if (id == null) {
                id = Integer.valueOf(this.mNotificationIds.incrementAndGet());
                accounts.signinRequiredNotificationIds.put(account, id);
            }
        }
        return id;
    }

    public void addAccount(IAccountManagerResponse response, String accountType, String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch, Bundle optionsIn) {
        Bundle.setDefusable(optionsIn, true);
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "addAccount , response " + response + ", authTokenType " + authTokenType + ", requiredFeatures " + stringArrayToString(requiredFeatures) + ", expectActivityLaunch " + expectActivityLaunch + ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
        }
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        } else if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        } else {
            int uid = Binder.getCallingUid();
            int userId = UserHandle.getUserId(uid);
            if (!canUserModifyAccounts(userId, uid)) {
                try {
                    response.onError(100, "User is not allowed to add an account!");
                } catch (RemoteException e) {
                }
                showCantAddAccount(100, userId);
            } else if (canUserModifyAccountsForType(userId, accountType, uid)) {
                int pid = Binder.getCallingPid();
                Bundle options = optionsIn == null ? new Bundle() : optionsIn;
                options.putInt("callerUid", uid);
                options.putInt("callerPid", pid);
                int usrId = UserHandle.getCallingUserId();
                long identityToken = clearCallingIdentity();
                try {
                    UserAccounts accounts = getUserAccounts(usrId);
                    logRecordWithUid(accounts, DebugDbHelper.ACTION_CALLED_ACCOUNT_ADD, TABLE_ACCOUNTS, uid);
                    new AnonymousClass7(this, this, accounts, response, accountType, expectActivityLaunch, true, null, false, true, authTokenType, requiredFeatures, options, accountType).bind();
                } finally {
                    restoreCallingIdentity(identityToken);
                }
            } else {
                try {
                    response.onError(H.KEYGUARD_DISMISS_DONE, "User cannot modify accounts of this type (policy).");
                } catch (RemoteException e2) {
                }
                showCantAddAccount(H.KEYGUARD_DISMISS_DONE, userId);
            }
        }
    }

    public void addAccountAsUser(IAccountManagerResponse response, String accountType, String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch, Bundle optionsIn, int userId) {
        Bundle.setDefusable(optionsIn, true);
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "addAccount, response " + response + ", authTokenType " + authTokenType + ", requiredFeatures " + stringArrayToString(requiredFeatures) + ", expectActivityLaunch " + expectActivityLaunch + ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid() + ", for user id " + userId);
        }
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        } else if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        } else if (isCrossUser(callingUid, userId)) {
            throw new SecurityException(String.format("User %s trying to add account for %s", new Object[]{Integer.valueOf(UserHandle.getCallingUserId()), Integer.valueOf(userId)}));
        } else if (!canUserModifyAccounts(userId, callingUid)) {
            try {
                response.onError(100, "User is not allowed to add an account!");
            } catch (RemoteException e) {
            }
            showCantAddAccount(100, userId);
        } else if (canUserModifyAccountsForType(userId, accountType, callingUid)) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            Bundle options = optionsIn == null ? new Bundle() : optionsIn;
            options.putInt("callerUid", uid);
            options.putInt("callerPid", pid);
            long identityToken = clearCallingIdentity();
            try {
                UserAccounts accounts = getUserAccounts(userId);
                logRecordWithUid(accounts, DebugDbHelper.ACTION_CALLED_ACCOUNT_ADD, TABLE_ACCOUNTS, userId);
                new AnonymousClass8(this, this, accounts, response, accountType, expectActivityLaunch, true, null, false, true, authTokenType, requiredFeatures, options, accountType).bind();
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            try {
                response.onError(H.KEYGUARD_DISMISS_DONE, "User cannot modify accounts of this type (policy).");
            } catch (RemoteException e2) {
            }
            showCantAddAccount(H.KEYGUARD_DISMISS_DONE, userId);
        }
    }

    public void startAddAccountSession(IAccountManagerResponse response, String accountType, String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch, Bundle optionsIn) {
        Bundle.setDefusable(optionsIn, true);
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "startAddAccountSession: accountType " + accountType + ", response " + response + ", authTokenType " + authTokenType + ", requiredFeatures " + stringArrayToString(requiredFeatures) + ", expectActivityLaunch " + expectActivityLaunch + ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
        }
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        } else if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        } else {
            int uid = Binder.getCallingUid();
            if (isSystemUid(uid)) {
                int userId = UserHandle.getUserId(uid);
                if (!canUserModifyAccounts(userId, uid)) {
                    try {
                        response.onError(100, "User is not allowed to add an account!");
                    } catch (RemoteException e) {
                    }
                    showCantAddAccount(100, userId);
                    return;
                } else if (canUserModifyAccountsForType(userId, accountType, uid)) {
                    int pid = Binder.getCallingPid();
                    Bundle options = optionsIn == null ? new Bundle() : optionsIn;
                    options.putInt("callerUid", uid);
                    options.putInt("callerPid", pid);
                    String callerPkg = optionsIn.getString("androidPackageName");
                    String[] strArr = new String[DE_DATABASE_VERSION];
                    strArr[0] = "android.permission.GET_PASSWORD";
                    boolean isPasswordForwardingAllowed = isPermitted(callerPkg, uid, strArr);
                    long identityToken = clearCallingIdentity();
                    try {
                        UserAccounts accounts = getUserAccounts(userId);
                        logRecordWithUid(accounts, DebugDbHelper.ACTION_CALLED_START_ACCOUNT_ADD, TABLE_ACCOUNTS, uid);
                        new AnonymousClass9(this, this, accounts, response, accountType, expectActivityLaunch, null, false, true, isPasswordForwardingAllowed, authTokenType, requiredFeatures, options, accountType).bind();
                        return;
                    } finally {
                        restoreCallingIdentity(identityToken);
                    }
                } else {
                    try {
                        response.onError(H.KEYGUARD_DISMISS_DONE, "User cannot modify accounts of this type (policy).");
                    } catch (RemoteException e2) {
                    }
                    showCantAddAccount(H.KEYGUARD_DISMISS_DONE, userId);
                    return;
                }
            }
            Object[] objArr = new Object[DE_DATABASE_VERSION];
            objArr[0] = Integer.valueOf(uid);
            throw new SecurityException(String.format("uid %s cannot stat add account session.", objArr));
        }
    }

    public void finishSessionAsUser(IAccountManagerResponse response, Bundle sessionBundle, boolean expectActivityLaunch, Bundle appInfo, int userId) {
        Bundle.setDefusable(sessionBundle, true);
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "finishSession: response " + response + ", expectActivityLaunch " + expectActivityLaunch + ", caller's uid " + callingUid + ", caller's user id " + UserHandle.getCallingUserId() + ", pid " + Binder.getCallingPid() + ", for user id " + userId);
        }
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        } else if (sessionBundle == null || sessionBundle.size() == 0) {
            throw new IllegalArgumentException("sessionBundle is empty");
        } else if (isCrossUser(callingUid, userId)) {
            throw new SecurityException(String.format("User %s trying to finish session for %s without cross user permission", new Object[]{Integer.valueOf(UserHandle.getCallingUserId()), Integer.valueOf(userId)}));
        } else if (!isSystemUid(callingUid)) {
            Object[] objArr = new Object[DE_DATABASE_VERSION];
            objArr[0] = Integer.valueOf(callingUid);
            throw new SecurityException(String.format("uid %s cannot finish session because it's not system uid.", objArr));
        } else if (canUserModifyAccounts(userId, callingUid)) {
            int pid = Binder.getCallingPid();
            try {
                Bundle decryptedBundle = CryptoHelper.getInstance().decryptBundle(sessionBundle);
                if (decryptedBundle == null) {
                    sendErrorResponse(response, 8, "failed to decrypt session bundle");
                    return;
                }
                String accountType = decryptedBundle.getString("accountType");
                if (TextUtils.isEmpty(accountType)) {
                    sendErrorResponse(response, 7, "accountType is empty");
                    return;
                }
                if (appInfo != null) {
                    decryptedBundle.putAll(appInfo);
                }
                decryptedBundle.putInt("callerUid", callingUid);
                decryptedBundle.putInt("callerPid", pid);
                if (canUserModifyAccountsForType(userId, accountType, callingUid)) {
                    long identityToken = clearCallingIdentity();
                    try {
                        UserAccounts accounts = getUserAccounts(userId);
                        logRecordWithUid(accounts, DebugDbHelper.ACTION_CALLED_ACCOUNT_SESSION_FINISH, TABLE_ACCOUNTS, callingUid);
                        new AnonymousClass10(this, this, accounts, response, accountType, expectActivityLaunch, true, null, false, true, decryptedBundle, accountType).bind();
                    } finally {
                        restoreCallingIdentity(identityToken);
                    }
                } else {
                    sendErrorResponse(response, H.KEYGUARD_DISMISS_DONE, "User cannot modify accounts of this type (policy).");
                    showCantAddAccount(H.KEYGUARD_DISMISS_DONE, userId);
                }
            } catch (Throwable e) {
                if (Log.isLoggable(TAG, MESSAGE_TIMED_OUT)) {
                    Log.v(TAG, "Failed to decrypt session bundle!", e);
                }
                sendErrorResponse(response, 8, "failed to decrypt session bundle");
            }
        } else {
            sendErrorResponse(response, 100, "User is not allowed to add an account!");
            showCantAddAccount(100, userId);
        }
    }

    private void showCantAddAccount(int errorCode, int userId) {
        Intent cantAddAccount = new Intent(this.mContext, CantAddAccountActivity.class);
        cantAddAccount.putExtra("android.accounts.extra.ERROR_CODE", errorCode);
        cantAddAccount.addFlags(268435456);
        long identityToken = clearCallingIdentity();
        try {
            this.mContext.startActivityAsUser(cantAddAccount, new UserHandle(userId));
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    public void confirmCredentialsAsUser(IAccountManagerResponse response, Account account, Bundle options, boolean expectActivityLaunch, int userId) {
        Bundle.setDefusable(options, true);
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "confirmCredentials , response " + response + ", expectActivityLaunch " + expectActivityLaunch + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (isCrossUser(callingUid, userId)) {
            throw new SecurityException(String.format("User %s trying to confirm account credentials for %s", new Object[]{Integer.valueOf(UserHandle.getCallingUserId()), Integer.valueOf(userId)}));
        } else if (response == null) {
            throw new IllegalArgumentException("response is null");
        } else if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else {
            long identityToken = clearCallingIdentity();
            try {
                new AnonymousClass11(this, this, getUserAccounts(userId), response, account.type, expectActivityLaunch, true, account.name, true, true, account, options).bind();
            } finally {
                restoreCallingIdentity(identityToken);
            }
        }
    }

    public void updateCredentials(IAccountManagerResponse response, Account account, String authTokenType, boolean expectActivityLaunch, Bundle loginOptions) {
        Bundle.setDefusable(loginOptions, true);
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "updateCredentials , response " + response + ", authTokenType " + authTokenType + ", expectActivityLaunch " + expectActivityLaunch + ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
        }
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        } else if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else {
            int userId = UserHandle.getCallingUserId();
            long identityToken = clearCallingIdentity();
            try {
                new AnonymousClass12(this, this, getUserAccounts(userId), response, account.type, expectActivityLaunch, true, account.name, false, true, account, authTokenType, loginOptions).bind();
            } finally {
                restoreCallingIdentity(identityToken);
            }
        }
    }

    public void startUpdateCredentialsSession(IAccountManagerResponse response, Account account, String authTokenType, boolean expectActivityLaunch, Bundle loginOptions) {
        Bundle.setDefusable(loginOptions, true);
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "startUpdateCredentialsSession: " + account + ", response " + response + ", authTokenType " + authTokenType + ", expectActivityLaunch " + expectActivityLaunch + ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
        }
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        } else if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else {
            int uid = Binder.getCallingUid();
            if (isSystemUid(uid)) {
                int userId = UserHandle.getCallingUserId();
                String callerPkg = loginOptions.getString("androidPackageName");
                String[] strArr = new String[DE_DATABASE_VERSION];
                strArr[0] = "android.permission.GET_PASSWORD";
                boolean isPasswordForwardingAllowed = isPermitted(callerPkg, uid, strArr);
                long identityToken = clearCallingIdentity();
                try {
                    new AnonymousClass13(this, this, getUserAccounts(userId), response, account.type, expectActivityLaunch, account.name, false, true, isPasswordForwardingAllowed, account, authTokenType, loginOptions).bind();
                } finally {
                    restoreCallingIdentity(identityToken);
                }
            } else {
                Object[] objArr = new Object[DE_DATABASE_VERSION];
                objArr[0] = Integer.valueOf(uid);
                throw new SecurityException(String.format("uid %s cannot start update credentials session.", objArr));
            }
        }
    }

    public void isCredentialsUpdateSuggested(IAccountManagerResponse response, Account account, String statusToken) {
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "isCredentialsUpdateSuggested: " + account + ", response " + response + ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
        }
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        } else if (account == null) {
            throw new IllegalArgumentException("account is null");
        } else if (TextUtils.isEmpty(statusToken)) {
            throw new IllegalArgumentException("status token is empty");
        } else {
            int uid = Binder.getCallingUid();
            if (isSystemUid(uid)) {
                int usrId = UserHandle.getCallingUserId();
                long identityToken = clearCallingIdentity();
                try {
                    new AnonymousClass14(this, this, getUserAccounts(usrId), response, account.type, false, false, account.name, false, account, statusToken).bind();
                } finally {
                    restoreCallingIdentity(identityToken);
                }
            } else {
                Object[] objArr = new Object[DE_DATABASE_VERSION];
                objArr[0] = Integer.valueOf(uid);
                throw new SecurityException(String.format("uid %s cannot stat add account session.", objArr));
            }
        }
    }

    public void editProperties(IAccountManagerResponse response, String accountType, boolean expectActivityLaunch) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "editProperties , response " + response + ", expectActivityLaunch " + expectActivityLaunch + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        } else if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        } else {
            int userId = UserHandle.getCallingUserId();
            if (isAccountManagedByCaller(accountType, callingUid, userId) || isSystemUid(callingUid)) {
                long identityToken = clearCallingIdentity();
                try {
                    new AnonymousClass15(this, this, getUserAccounts(userId), response, accountType, expectActivityLaunch, true, null, false, accountType).bind();
                } finally {
                    restoreCallingIdentity(identityToken);
                }
            } else {
                throw new SecurityException(String.format("uid %s cannot edit authenticator properites for account type: %s", new Object[]{Integer.valueOf(callingUid), accountType}));
            }
        }
    }

    public boolean someUserHasAccount(Account account) {
        if (UserHandle.isSameApp(ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE, Binder.getCallingUid())) {
            long token = Binder.clearCallingIdentity();
            try {
                AccountAndUser[] allAccounts = getAllAccounts();
                for (int i = allAccounts.length - 1; i >= 0; i--) {
                    if (allAccounts[i].account.equals(account)) {
                        return true;
                    }
                }
                Binder.restoreCallingIdentity(token);
                return false;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } else {
            throw new SecurityException("Only system can check for accounts across users");
        }
    }

    public Account[] getAccounts(int userId, String opPackageName) {
        int callingUid = Binder.getCallingUid();
        List<String> visibleAccountTypes = getTypesVisibleToCaller(callingUid, userId, opPackageName);
        if (visibleAccountTypes.isEmpty()) {
            return new Account[0];
        }
        long identityToken = clearCallingIdentity();
        try {
            Account[] accountsInternal = getAccountsInternal(getUserAccounts(userId), callingUid, null, visibleAccountTypes);
            return accountsInternal;
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    public AccountAndUser[] getRunningAccounts() {
        try {
            return getAccounts(ActivityManagerNative.getDefault().getRunningUserIds());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public AccountAndUser[] getAllAccounts() {
        List<UserInfo> users = getUserManager().getUsers(true);
        int[] userIds = new int[users.size()];
        for (int i = 0; i < userIds.length; i += DE_DATABASE_VERSION) {
            userIds[i] = ((UserInfo) users.get(i)).id;
        }
        return getAccounts(userIds);
    }

    private AccountAndUser[] getAccounts(int[] userIds) {
        ArrayList<AccountAndUser> runningAccounts = Lists.newArrayList();
        int length = userIds.length;
        for (int i = 0; i < length; i += DE_DATABASE_VERSION) {
            int userId = userIds[i];
            UserAccounts userAccounts = getUserAccounts(userId);
            if (userAccounts != null) {
                synchronized (userAccounts.cacheLock) {
                    Account[] accounts = getAccountsFromCacheLocked(userAccounts, null, Binder.getCallingUid(), null);
                    for (int a = 0; a < accounts.length; a += DE_DATABASE_VERSION) {
                        runningAccounts.add(new AccountAndUser(accounts[a], userId));
                    }
                }
            }
        }
        return (AccountAndUser[]) runningAccounts.toArray(new AccountAndUser[runningAccounts.size()]);
    }

    public Account[] getAccountsAsUser(String type, int userId, String opPackageName) {
        return getAccountsAsUser(type, userId, null, -1, opPackageName);
    }

    private Account[] getAccountsAsUser(String type, int userId, String callingPackage, int packageUid, String opPackageName) {
        int callingUid = Binder.getCallingUid();
        if (userId == UserHandle.getCallingUserId() || callingUid == Process.myUid() || this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "getAccounts: accountType " + type + ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
            }
            if (packageUid != -1 && UserHandle.isSameApp(callingUid, Process.myUid())) {
                callingUid = packageUid;
                opPackageName = callingPackage;
            }
            List<String> visibleAccountTypes = getTypesVisibleToCaller(callingUid, userId, opPackageName);
            if (visibleAccountTypes.isEmpty() || (type != null && !visibleAccountTypes.contains(type))) {
                return new Account[0];
            }
            if (visibleAccountTypes.contains(type)) {
                visibleAccountTypes = new ArrayList();
                visibleAccountTypes.add(type);
            }
            long identityToken = clearCallingIdentity();
            try {
                Account[] accountsInternal = getAccountsInternal(getUserAccounts(userId), callingUid, callingPackage, visibleAccountTypes);
                return accountsInternal;
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            throw new SecurityException("User " + UserHandle.getCallingUserId() + " trying to get account for " + userId);
        }
    }

    private Account[] getAccountsInternal(UserAccounts userAccounts, int callingUid, String callingPackage, List<String> visibleAccountTypes) {
        Account[] result;
        synchronized (userAccounts.cacheLock) {
            ArrayList<Account> visibleAccounts = new ArrayList();
            for (String visibleType : visibleAccountTypes) {
                Account[] accountsForType = getAccountsFromCacheLocked(userAccounts, visibleType, callingUid, callingPackage);
                if (accountsForType != null) {
                    visibleAccounts.addAll(Arrays.asList(accountsForType));
                }
            }
            result = new Account[visibleAccounts.size()];
            for (int i = 0; i < visibleAccounts.size(); i += DE_DATABASE_VERSION) {
                result[i] = (Account) visibleAccounts.get(i);
            }
        }
        return result;
    }

    public void addSharedAccountsFromParentUser(int parentUserId, int userId) {
        checkManageOrCreateUsersPermission("addSharedAccountsFromParentUser");
        Account[] accounts = getAccountsAsUser(null, parentUserId, this.mContext.getOpPackageName());
        int length = accounts.length;
        for (int i = 0; i < length; i += DE_DATABASE_VERSION) {
            addSharedAccountAsUser(accounts[i], userId);
        }
    }

    private boolean addSharedAccountAsUser(Account account, int userId) {
        UserAccounts accounts = getUserAccounts(handleIncomingUser(userId));
        SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ACCOUNTS_NAME, account.name);
        values.put(AUTHTOKENS_TYPE, account.type);
        db.delete(TABLE_SHARED_ACCOUNTS, "name=? AND type=?", new String[]{account.name, account.type});
        long accountId = db.insert(TABLE_SHARED_ACCOUNTS, ACCOUNTS_NAME, values);
        if (accountId < 0) {
            Log.w(TAG, "insertAccountIntoDatabase , skipping the DB insert failed");
            return false;
        }
        logRecord(db, DebugDbHelper.ACTION_ACCOUNT_ADD, TABLE_SHARED_ACCOUNTS, accountId, accounts);
        return true;
    }

    public boolean renameSharedAccountAsUser(Account account, String newName, int userId) {
        UserAccounts accounts = getUserAccounts(handleIncomingUser(userId));
        SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
        long sharedTableAccountId = getAccountIdFromSharedTable(db, account);
        ContentValues values = new ContentValues();
        values.put(ACCOUNTS_NAME, newName);
        int r = db.update(TABLE_SHARED_ACCOUNTS, values, "name=? AND type=?", new String[]{account.name, account.type});
        if (r > 0) {
            logRecord(db, DebugDbHelper.ACTION_ACCOUNT_RENAME, TABLE_SHARED_ACCOUNTS, sharedTableAccountId, accounts, getCallingUid());
            renameAccountInternal(accounts, account, newName);
        }
        return r > 0;
    }

    public boolean removeSharedAccountAsUser(Account account, int userId) {
        return removeSharedAccountAsUser(account, userId, getCallingUid());
    }

    private boolean removeSharedAccountAsUser(Account account, int userId, int callingUid) {
        UserAccounts accounts = getUserAccounts(handleIncomingUser(userId));
        SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
        long sharedTableAccountId = getAccountIdFromSharedTable(db, account);
        int r = db.delete(TABLE_SHARED_ACCOUNTS, "name=? AND type=?", new String[]{account.name, account.type});
        if (r > 0) {
            logRecord(db, DebugDbHelper.ACTION_ACCOUNT_REMOVE, TABLE_SHARED_ACCOUNTS, sharedTableAccountId, accounts, callingUid);
            removeAccountInternal(accounts, account, callingUid);
        }
        return r > 0;
    }

    public Account[] getSharedAccountsAsUser(int userId) {
        UserAccounts accounts = getUserAccounts(handleIncomingUser(userId));
        ArrayList<Account> accountList = new ArrayList();
        Cursor cursor = null;
        try {
            cursor = accounts.openHelper.getReadableDatabase().query(TABLE_SHARED_ACCOUNTS, new String[]{ACCOUNTS_NAME, AUTHTOKENS_TYPE}, null, null, null, null, null);
            Account[] accountArray;
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                accountArray = new Account[accountList.size()];
                accountList.toArray(accountArray);
                return accountArray;
            }
            int nameIndex = cursor.getColumnIndex(ACCOUNTS_NAME);
            int typeIndex = cursor.getColumnIndex(AUTHTOKENS_TYPE);
            do {
                accountList.add(new Account(cursor.getString(nameIndex), cursor.getString(typeIndex)));
            } while (cursor.moveToNext());
            if (cursor != null) {
                cursor.close();
            }
            accountArray = new Account[accountList.size()];
            accountList.toArray(accountArray);
            return accountArray;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public Account[] getAccounts(String type, String opPackageName) {
        return getAccountsAsUser(type, UserHandle.getCallingUserId(), opPackageName);
    }

    public Account[] getAccountsForPackage(String packageName, int uid, String opPackageName) {
        int callingUid = Binder.getCallingUid();
        if (UserHandle.isSameApp(callingUid, Process.myUid())) {
            return getAccountsAsUser(null, UserHandle.getCallingUserId(), packageName, uid, opPackageName);
        }
        throw new SecurityException("getAccountsForPackage() called from unauthorized uid " + callingUid + " with uid=" + uid);
    }

    public Account[] getAccountsByTypeForPackage(String type, String packageName, String opPackageName) {
        try {
            return getAccountsAsUser(type, UserHandle.getCallingUserId(), packageName, AppGlobals.getPackageManager().getPackageUid(packageName, DumpState.DUMP_PREFERRED_XML, UserHandle.getCallingUserId()), opPackageName);
        } catch (RemoteException re) {
            Slog.e(TAG, "Couldn't determine the packageUid for " + packageName + re);
            return new Account[0];
        }
    }

    public void getAccountsByFeatures(IAccountManagerResponse response, String type, String[] features, String opPackageName) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "getAccounts: accountType " + type + ", response " + response + ", features " + stringArrayToString(features) + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        } else if (type == null) {
            throw new IllegalArgumentException("accountType is null");
        } else {
            int userId = UserHandle.getCallingUserId();
            Bundle result;
            if (getTypesVisibleToCaller(callingUid, userId, opPackageName).contains(type)) {
                long identityToken = clearCallingIdentity();
                try {
                    UserAccounts userAccounts = getUserAccounts(userId);
                    if (features == null || features.length == 0) {
                        Account[] accounts;
                        synchronized (userAccounts.cacheLock) {
                            accounts = getAccountsFromCacheLocked(userAccounts, type, callingUid, null);
                        }
                        result = new Bundle();
                        result.putParcelableArray(TABLE_ACCOUNTS, accounts);
                        onResult(response, result);
                        return;
                    }
                    new GetAccountsByTypeAndFeatureSession(this, userAccounts, response, type, features, callingUid).bind();
                    restoreCallingIdentity(identityToken);
                } finally {
                    restoreCallingIdentity(identityToken);
                }
            } else {
                result = new Bundle();
                result.putParcelableArray(TABLE_ACCOUNTS, new Account[0]);
                try {
                    response.onResult(result);
                } catch (RemoteException e) {
                    Log.e(TAG, "Cannot respond to caller do to exception.", e);
                }
            }
        }
    }

    private long getAccountIdFromSharedTable(SQLiteDatabase db, Account account) {
        String str = TABLE_SHARED_ACCOUNTS;
        String[] strArr = new String[DE_DATABASE_VERSION];
        strArr[0] = SHARED_ACCOUNTS_ID;
        SQLiteDatabase sQLiteDatabase = db;
        Cursor cursor = sQLiteDatabase.query(str, strArr, "name=? AND type=?", new String[]{account.name, account.type}, null, null, null);
        try {
            if (cursor.moveToNext()) {
                long j = cursor.getLong(0);
                return j;
            }
            cursor.close();
            return -1;
        } finally {
            cursor.close();
        }
    }

    private long getAccountIdLocked(SQLiteDatabase db, Account account) {
        String str = TABLE_ACCOUNTS;
        String[] strArr = new String[DE_DATABASE_VERSION];
        strArr[0] = SHARED_ACCOUNTS_ID;
        SQLiteDatabase sQLiteDatabase = db;
        Cursor cursor = sQLiteDatabase.query(str, strArr, "name=? AND type=?", new String[]{account.name, account.type}, null, null, null);
        try {
            if (cursor.moveToNext()) {
                long j = cursor.getLong(0);
                return j;
            }
            cursor.close();
            return -1;
        } finally {
            cursor.close();
        }
    }

    private long getExtrasIdLocked(SQLiteDatabase db, long accountId, String key) {
        String str = CE_TABLE_EXTRAS;
        String[] strArr = new String[DE_DATABASE_VERSION];
        strArr[0] = SHARED_ACCOUNTS_ID;
        String str2 = "accounts_id=" + accountId + " AND " + META_KEY + "=?";
        String[] strArr2 = new String[DE_DATABASE_VERSION];
        strArr2[0] = key;
        Cursor cursor = db.query(str, strArr, str2, strArr2, null, null, null);
        try {
            if (cursor.moveToNext()) {
                long j = cursor.getLong(0);
                return j;
            }
            cursor.close();
            return -1;
        } finally {
            cursor.close();
        }
    }

    String getPreNDatabaseName(int userId) {
        File systemDir = Environment.getDataSystemDirectory();
        File databaseFile = new File(Environment.getUserSystemDirectory(userId), PRE_N_DATABASE_NAME);
        if (userId == 0) {
            File oldFile = new File(systemDir, PRE_N_DATABASE_NAME);
            if (oldFile.exists() && !databaseFile.exists()) {
                File userDir = Environment.getUserSystemDirectory(userId);
                if (!userDir.exists() && !userDir.mkdirs()) {
                    throw new IllegalStateException("User dir cannot be created: " + userDir);
                } else if (!oldFile.renameTo(databaseFile)) {
                    throw new IllegalStateException("User dir cannot be migrated: " + databaseFile);
                }
            }
        }
        return databaseFile.getPath();
    }

    String getDeDatabaseName(int userId) {
        return new File(Environment.getDataSystemDeDirectory(userId), DE_DATABASE_NAME).getPath();
    }

    String getCeDatabaseName(int userId) {
        return new File(Environment.getDataSystemCeDirectory(userId), CE_DATABASE_NAME).getPath();
    }

    private void logRecord(UserAccounts accounts, String action, String tableName) {
        logRecord(accounts.openHelper.getWritableDatabase(), action, tableName, -1, accounts);
    }

    private void logRecordWithUid(UserAccounts accounts, String action, String tableName, int uid) {
        logRecord(accounts.openHelper.getWritableDatabase(), action, tableName, -1, accounts, uid);
    }

    private void logRecord(SQLiteDatabase db, String action, String tableName, long accountId, UserAccounts userAccount) {
        logRecord(db, action, tableName, accountId, userAccount, getCallingUid());
    }

    private void logRecord(SQLiteDatabase db, String action, String tableName, long accountId, UserAccounts userAccount, int callingUid) {
        SQLiteStatement logStatement = userAccount.statementForLogging;
        logStatement.bindLong(DE_DATABASE_VERSION, accountId);
        logStatement.bindString(2, action);
        logStatement.bindString(MESSAGE_TIMED_OUT, DebugDbHelper.dateFromat.format(new Date()));
        logStatement.bindLong(MESSAGE_COPY_SHARED_ACCOUNT, (long) callingUid);
        logStatement.bindString(5, tableName);
        logStatement.bindLong(6, (long) userAccount.debugDbInsertionPoint);
        logStatement.execute();
        logStatement.clearBindings();
        userAccount.debugDbInsertionPoint = (userAccount.debugDbInsertionPoint + DE_DATABASE_VERSION) % MAX_DEBUG_DB_SIZE;
    }

    private void initializeDebugDbSizeAndCompileSqlStatementForLogging(SQLiteDatabase db, UserAccounts userAccount) {
        try {
            int size = (int) getDebugTableRowCount(db);
            if (size >= MAX_DEBUG_DB_SIZE) {
                userAccount.debugDbInsertionPoint = (int) getDebugTableInsertionPoint(db);
            } else {
                userAccount.debugDbInsertionPoint = size;
            }
            compileSqlStatementForLogging(db, userAccount);
        } catch (SQLiteException ex) {
            Log.w(TAG, "initializeDebugDbSize got err:", ex);
        }
    }

    private void compileSqlStatementForLogging(SQLiteDatabase db, UserAccounts userAccount) {
        userAccount.statementForLogging = db.compileStatement("INSERT OR REPLACE INTO " + DebugDbHelper.TABLE_DEBUG + " VALUES (?,?,?,?,?,?)");
    }

    private long getDebugTableRowCount(SQLiteDatabase db) {
        return DatabaseUtils.longForQuery(db, "SELECT COUNT(*) FROM " + DebugDbHelper.TABLE_DEBUG, null);
    }

    private long getDebugTableInsertionPoint(SQLiteDatabase db) {
        return DatabaseUtils.longForQuery(db, "SELECT " + DebugDbHelper.KEY + " FROM " + DebugDbHelper.TABLE_DEBUG + " ORDER BY " + DebugDbHelper.TIMESTAMP + "," + DebugDbHelper.KEY + " LIMIT 1", null);
    }

    public IBinder onBind(Intent intent) {
        return asBinder();
    }

    private static boolean scanArgs(String[] args, String value) {
        if (args != null) {
            int length = args.length;
            for (int i = 0; i < length; i += DE_DATABASE_VERSION) {
                if (value.equals(args[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            fout.println("Permission Denial: can't dump AccountsManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " without permission " + "android.permission.DUMP");
            return;
        }
        boolean scanArgs = !scanArgs(args, "--checkin") ? scanArgs(args, "-c") : true;
        IndentingPrintWriter ipw = new IndentingPrintWriter(fout, "  ");
        for (UserInfo user : getUserManager().getUsers()) {
            ipw.println("User " + user + META_KEY_DELIMITER);
            ipw.increaseIndent();
            dumpUser(getUserAccounts(user.id), fd, ipw, args, scanArgs);
            ipw.println();
            ipw.decreaseIndent();
        }
    }

    private void dumpUser(com.android.server.accounts.AccountManagerService.UserAccounts r21, java.io.FileDescriptor r22, java.io.PrintWriter r23, java.lang.String[] r24, boolean r25) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:com.android.server.accounts.AccountManagerService.dumpUser(com.android.server.accounts.AccountManagerService$UserAccounts, java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[], boolean):void. bs: [B:7:0x001f, B:30:0x00cc]
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:86)
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:57)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:52)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r20 = this;
        r19 = r21.cacheLock;
        monitor-enter(r19);
        r5 = r21.openHelper;	 Catch:{ all -> 0x0054 }
        r4 = r5.getReadableDatabase();	 Catch:{ all -> 0x0054 }
        if (r25 == 0) goto L_0x005e;	 Catch:{ all -> 0x0054 }
    L_0x000f:
        r5 = "accounts";	 Catch:{ all -> 0x0054 }
        r6 = ACCOUNT_TYPE_COUNT_PROJECTION;	 Catch:{ all -> 0x0054 }
        r9 = "type";	 Catch:{ all -> 0x0054 }
        r7 = 0;	 Catch:{ all -> 0x0054 }
        r8 = 0;	 Catch:{ all -> 0x0054 }
        r10 = 0;	 Catch:{ all -> 0x0054 }
        r11 = 0;	 Catch:{ all -> 0x0054 }
        r14 = r4.query(r5, r6, r7, r8, r9, r10, r11);	 Catch:{ all -> 0x0054 }
    L_0x001f:
        r5 = r14.moveToNext();	 Catch:{ all -> 0x004d }
        if (r5 == 0) goto L_0x0057;	 Catch:{ all -> 0x004d }
    L_0x0025:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x004d }
        r5.<init>();	 Catch:{ all -> 0x004d }
        r6 = 0;	 Catch:{ all -> 0x004d }
        r6 = r14.getString(r6);	 Catch:{ all -> 0x004d }
        r5 = r5.append(r6);	 Catch:{ all -> 0x004d }
        r6 = ",";	 Catch:{ all -> 0x004d }
        r5 = r5.append(r6);	 Catch:{ all -> 0x004d }
        r6 = 1;	 Catch:{ all -> 0x004d }
        r6 = r14.getString(r6);	 Catch:{ all -> 0x004d }
        r5 = r5.append(r6);	 Catch:{ all -> 0x004d }
        r5 = r5.toString();	 Catch:{ all -> 0x004d }
        r0 = r23;	 Catch:{ all -> 0x004d }
        r0.println(r5);	 Catch:{ all -> 0x004d }
        goto L_0x001f;
    L_0x004d:
        r5 = move-exception;
        if (r14 == 0) goto L_0x0053;
    L_0x0050:
        r14.close();	 Catch:{ all -> 0x0054 }
    L_0x0053:
        throw r5;	 Catch:{ all -> 0x0054 }
    L_0x0054:
        r5 = move-exception;
        monitor-exit(r19);
        throw r5;
    L_0x0057:
        if (r14 == 0) goto L_0x005c;
    L_0x0059:
        r14.close();	 Catch:{ all -> 0x0054 }
    L_0x005c:
        monitor-exit(r19);
        return;
    L_0x005e:
        r5 = android.os.Process.myUid();	 Catch:{ all -> 0x0054 }
        r6 = 0;	 Catch:{ all -> 0x0054 }
        r7 = 0;	 Catch:{ all -> 0x0054 }
        r0 = r20;	 Catch:{ all -> 0x0054 }
        r1 = r21;	 Catch:{ all -> 0x0054 }
        r13 = r0.getAccountsFromCacheLocked(r1, r6, r5, r7);	 Catch:{ all -> 0x0054 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0054 }
        r5.<init>();	 Catch:{ all -> 0x0054 }
        r6 = "Accounts: ";	 Catch:{ all -> 0x0054 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0054 }
        r6 = r13.length;	 Catch:{ all -> 0x0054 }
        r5 = r5.append(r6);	 Catch:{ all -> 0x0054 }
        r5 = r5.toString();	 Catch:{ all -> 0x0054 }
        r0 = r23;	 Catch:{ all -> 0x0054 }
        r0.println(r5);	 Catch:{ all -> 0x0054 }
        r5 = 0;	 Catch:{ all -> 0x0054 }
        r6 = r13.length;	 Catch:{ all -> 0x0054 }
    L_0x0088:
        if (r5 >= r6) goto L_0x00a8;	 Catch:{ all -> 0x0054 }
    L_0x008a:
        r12 = r13[r5];	 Catch:{ all -> 0x0054 }
        r7 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0054 }
        r7.<init>();	 Catch:{ all -> 0x0054 }
        r8 = "  ";	 Catch:{ all -> 0x0054 }
        r7 = r7.append(r8);	 Catch:{ all -> 0x0054 }
        r7 = r7.append(r12);	 Catch:{ all -> 0x0054 }
        r7 = r7.toString();	 Catch:{ all -> 0x0054 }
        r0 = r23;	 Catch:{ all -> 0x0054 }
        r0.println(r7);	 Catch:{ all -> 0x0054 }
        r5 = r5 + 1;	 Catch:{ all -> 0x0054 }
        goto L_0x0088;	 Catch:{ all -> 0x0054 }
    L_0x00a8:
        r23.println();	 Catch:{ all -> 0x0054 }
        r5 = com.android.server.accounts.AccountManagerService.DebugDbHelper.TABLE_DEBUG;	 Catch:{ all -> 0x0054 }
        r11 = com.android.server.accounts.AccountManagerService.DebugDbHelper.TIMESTAMP;	 Catch:{ all -> 0x0054 }
        r6 = 0;	 Catch:{ all -> 0x0054 }
        r7 = 0;	 Catch:{ all -> 0x0054 }
        r8 = 0;	 Catch:{ all -> 0x0054 }
        r9 = 0;	 Catch:{ all -> 0x0054 }
        r10 = 0;	 Catch:{ all -> 0x0054 }
        r14 = r4.query(r5, r6, r7, r8, r9, r10, r11);	 Catch:{ all -> 0x0054 }
        r5 = "AccountId, Action_Type, timestamp, UID, TableName, Key";	 Catch:{ all -> 0x0054 }
        r0 = r23;	 Catch:{ all -> 0x0054 }
        r0.println(r5);	 Catch:{ all -> 0x0054 }
        r5 = "Accounts History";	 Catch:{ all -> 0x0054 }
        r0 = r23;	 Catch:{ all -> 0x0054 }
        r0.println(r5);	 Catch:{ all -> 0x0054 }
    L_0x00cc:
        r5 = r14.moveToNext();	 Catch:{ all -> 0x013a }
        if (r5 == 0) goto L_0x013f;	 Catch:{ all -> 0x013a }
    L_0x00d2:
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x013a }
        r5.<init>();	 Catch:{ all -> 0x013a }
        r6 = 0;	 Catch:{ all -> 0x013a }
        r6 = r14.getString(r6);	 Catch:{ all -> 0x013a }
        r5 = r5.append(r6);	 Catch:{ all -> 0x013a }
        r6 = ",";	 Catch:{ all -> 0x013a }
        r5 = r5.append(r6);	 Catch:{ all -> 0x013a }
        r6 = 1;	 Catch:{ all -> 0x013a }
        r6 = r14.getString(r6);	 Catch:{ all -> 0x013a }
        r5 = r5.append(r6);	 Catch:{ all -> 0x013a }
        r6 = ",";	 Catch:{ all -> 0x013a }
        r5 = r5.append(r6);	 Catch:{ all -> 0x013a }
        r6 = 2;	 Catch:{ all -> 0x013a }
        r6 = r14.getString(r6);	 Catch:{ all -> 0x013a }
        r5 = r5.append(r6);	 Catch:{ all -> 0x013a }
        r6 = ",";	 Catch:{ all -> 0x013a }
        r5 = r5.append(r6);	 Catch:{ all -> 0x013a }
        r6 = 3;	 Catch:{ all -> 0x013a }
        r6 = r14.getString(r6);	 Catch:{ all -> 0x013a }
        r5 = r5.append(r6);	 Catch:{ all -> 0x013a }
        r6 = ",";	 Catch:{ all -> 0x013a }
        r5 = r5.append(r6);	 Catch:{ all -> 0x013a }
        r6 = 4;	 Catch:{ all -> 0x013a }
        r6 = r14.getString(r6);	 Catch:{ all -> 0x013a }
        r5 = r5.append(r6);	 Catch:{ all -> 0x013a }
        r6 = ",";	 Catch:{ all -> 0x013a }
        r5 = r5.append(r6);	 Catch:{ all -> 0x013a }
        r6 = 5;	 Catch:{ all -> 0x013a }
        r6 = r14.getString(r6);	 Catch:{ all -> 0x013a }
        r5 = r5.append(r6);	 Catch:{ all -> 0x013a }
        r5 = r5.toString();	 Catch:{ all -> 0x013a }
        r0 = r23;	 Catch:{ all -> 0x013a }
        r0.println(r5);	 Catch:{ all -> 0x013a }
        goto L_0x00cc;
    L_0x013a:
        r5 = move-exception;
        r14.close();	 Catch:{ all -> 0x0054 }
        throw r5;	 Catch:{ all -> 0x0054 }
    L_0x013f:
        r14.close();	 Catch:{ all -> 0x0054 }
        r23.println();	 Catch:{ all -> 0x0054 }
        r0 = r20;	 Catch:{ all -> 0x0054 }
        r6 = r0.mSessions;	 Catch:{ all -> 0x0054 }
        monitor-enter(r6);	 Catch:{ all -> 0x0054 }
        r16 = android.os.SystemClock.elapsedRealtime();	 Catch:{ all -> 0x01a5 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x01a5 }
        r5.<init>();	 Catch:{ all -> 0x01a5 }
        r7 = "Active Sessions: ";	 Catch:{ all -> 0x01a5 }
        r5 = r5.append(r7);	 Catch:{ all -> 0x01a5 }
        r0 = r20;	 Catch:{ all -> 0x01a5 }
        r7 = r0.mSessions;	 Catch:{ all -> 0x01a5 }
        r7 = r7.size();	 Catch:{ all -> 0x01a5 }
        r5 = r5.append(r7);	 Catch:{ all -> 0x01a5 }
        r5 = r5.toString();	 Catch:{ all -> 0x01a5 }
        r0 = r23;	 Catch:{ all -> 0x01a5 }
        r0.println(r5);	 Catch:{ all -> 0x01a5 }
        r0 = r20;	 Catch:{ all -> 0x01a5 }
        r5 = r0.mSessions;	 Catch:{ all -> 0x01a5 }
        r5 = r5.values();	 Catch:{ all -> 0x01a5 }
        r18 = r5.iterator();	 Catch:{ all -> 0x01a5 }
    L_0x017b:
        r5 = r18.hasNext();	 Catch:{ all -> 0x01a5 }
        if (r5 == 0) goto L_0x01a8;	 Catch:{ all -> 0x01a5 }
    L_0x0181:
        r15 = r18.next();	 Catch:{ all -> 0x01a5 }
        r15 = (com.android.server.accounts.AccountManagerService.Session) r15;	 Catch:{ all -> 0x01a5 }
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x01a5 }
        r5.<init>();	 Catch:{ all -> 0x01a5 }
        r7 = "  ";	 Catch:{ all -> 0x01a5 }
        r5 = r5.append(r7);	 Catch:{ all -> 0x01a5 }
        r7 = r15.toDebugString(r16);	 Catch:{ all -> 0x01a5 }
        r5 = r5.append(r7);	 Catch:{ all -> 0x01a5 }
        r5 = r5.toString();	 Catch:{ all -> 0x01a5 }
        r0 = r23;	 Catch:{ all -> 0x01a5 }
        r0.println(r5);	 Catch:{ all -> 0x01a5 }
        goto L_0x017b;
    L_0x01a5:
        r5 = move-exception;
        monitor-exit(r6);	 Catch:{ all -> 0x0054 }
        throw r5;	 Catch:{ all -> 0x0054 }
    L_0x01a8:
        monitor-exit(r6);	 Catch:{ all -> 0x0054 }
        r23.println();	 Catch:{ all -> 0x0054 }
        r0 = r20;	 Catch:{ all -> 0x0054 }
        r5 = r0.mAuthenticatorCache;	 Catch:{ all -> 0x0054 }
        r6 = r21.userId;	 Catch:{ all -> 0x0054 }
        r0 = r22;	 Catch:{ all -> 0x0054 }
        r1 = r23;	 Catch:{ all -> 0x0054 }
        r2 = r24;	 Catch:{ all -> 0x0054 }
        r5.dump(r0, r1, r2, r6);	 Catch:{ all -> 0x0054 }
        goto L_0x005c;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.accounts.AccountManagerService.dumpUser(com.android.server.accounts.AccountManagerService$UserAccounts, java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[], boolean):void");
    }

    private void doNotification(UserAccounts accounts, Account account, CharSequence message, Intent intent, int userId) {
        long identityToken = clearCallingIdentity();
        try {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "doNotification: " + message + " intent:" + intent);
            }
            if (intent.getComponent() == null || !GrantCredentialsPermissionActivity.class.getName().equals(intent.getComponent().getClassName())) {
                Integer notificationId = getSigninRequiredNotificationId(accounts, account);
                intent.addCategory(String.valueOf(notificationId));
                UserHandle user = new UserHandle(userId);
                Context contextForUser = getContextForUser(user);
                String notificationTitleFormat = contextForUser.getText(17039618).toString();
                Builder color = new Builder(contextForUser).setWhen(0).setSmallIcon(17301642).setLargeIcon(BitmapFactory.decodeResource(this.mContext.getResources(), 33751571)).setColor(contextForUser.getColor(17170519));
                Object[] objArr = new Object[DE_DATABASE_VERSION];
                objArr[0] = account.name;
                Builder contentText = color.setContentTitle(String.format(notificationTitleFormat, objArr)).setContentText(message);
                installNotification(notificationId.intValue(), r17.setContentIntent(PendingIntent.getActivityAsUser(this.mContext, 0, intent, 268435456, null, user)).build(), user);
            } else {
                createNoCredentialsPermissionNotification(account, intent, userId);
            }
            restoreCallingIdentity(identityToken);
        } catch (Throwable th) {
            restoreCallingIdentity(identityToken);
        }
    }

    protected void installNotification(int notificationId, Notification n, UserHandle user) {
        ((NotificationManager) this.mContext.getSystemService("notification")).notifyAsUser(null, notificationId, n, user);
    }

    protected void cancelNotification(int id, UserHandle user) {
        long identityToken = clearCallingIdentity();
        try {
            ((NotificationManager) this.mContext.getSystemService("notification")).cancelAsUser(null, id, user);
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    private boolean isPermitted(String opPackageName, int callingUid, String... permissions) {
        int length = permissions.length;
        for (int i = 0; i < length; i += DE_DATABASE_VERSION) {
            String perm = permissions[i];
            if (this.mContext.checkCallingOrSelfPermission(perm) == 0) {
                if (Log.isLoggable(TAG, 2)) {
                    Log.v(TAG, "  caller uid " + callingUid + " has " + perm);
                }
                int opCode = AppOpsManager.permissionToOpCode(perm);
                if (opCode == -1 || this.mAppOpsManager.noteOp(opCode, callingUid, opPackageName) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private int handleIncomingUser(int userId) {
        try {
            return ActivityManagerNative.getDefault().handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, true, "", null);
        } catch (RemoteException e) {
            return userId;
        }
    }

    private boolean isPrivileged(int callingUid) {
        try {
            PackageManager userPackageManager = this.mContext.createPackageContextAsUser("android", 0, new UserHandle(UserHandle.getUserId(callingUid))).getPackageManager();
            String[] packages = userPackageManager.getPackagesForUid(callingUid);
            int length = packages.length;
            int i = 0;
            while (i < length) {
                try {
                    PackageInfo packageInfo = userPackageManager.getPackageInfo(packages[i], 0);
                    if (packageInfo != null && (packageInfo.applicationInfo.privateFlags & 8) != 0) {
                        return true;
                    }
                    i += DE_DATABASE_VERSION;
                } catch (NameNotFoundException e) {
                    return false;
                }
            }
            return false;
        } catch (NameNotFoundException e2) {
            return false;
        }
    }

    private boolean permissionIsGranted(Account account, String authTokenType, int callerUid, int userId) {
        boolean isAccountManagedByCaller;
        boolean hasExplicitlyGrantedPermission;
        boolean isPrivileged = isPrivileged(callerUid);
        if (account != null) {
            isAccountManagedByCaller = isAccountManagedByCaller(account.type, callerUid, userId);
        } else {
            isAccountManagedByCaller = false;
        }
        if (account != null) {
            hasExplicitlyGrantedPermission = hasExplicitlyGrantedPermission(account, authTokenType, callerUid);
        } else {
            hasExplicitlyGrantedPermission = false;
        }
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "checkGrantsOrCallingUidAgainstAuthenticator: caller uid " + callerUid + ", " + " is authenticator? " + isAccountManagedByCaller + ", has explicit permission? " + hasExplicitlyGrantedPermission);
        }
        return (isAccountManagedByCaller || hasExplicitlyGrantedPermission) ? true : isPrivileged;
    }

    private boolean isAccountVisibleToCaller(String accountType, int callingUid, int userId, String opPackageName) {
        if (accountType == null) {
            return false;
        }
        return getTypesVisibleToCaller(callingUid, userId, opPackageName).contains(accountType);
    }

    private boolean isAccountManagedByCaller(String accountType, int callingUid, int userId) {
        if (accountType == null) {
            return false;
        }
        return getTypesManagedByCaller(callingUid, userId).contains(accountType);
    }

    private List<String> getTypesVisibleToCaller(int callingUid, int userId, String opPackageName) {
        return getTypesForCaller(callingUid, userId, isPermitted(opPackageName, callingUid, "android.permission.GET_ACCOUNTS", "android.permission.GET_ACCOUNTS_PRIVILEGED"));
    }

    private List<String> getTypesManagedByCaller(int callingUid, int userId) {
        return getTypesForCaller(callingUid, userId, false);
    }

    private List<String> getTypesForCaller(int callingUid, int userId, boolean isOtherwisePermitted) {
        List<String> managedAccountTypes = new ArrayList();
        long identityToken = Binder.clearCallingIdentity();
        try {
            Collection<ServiceInfo<AuthenticatorDescription>> serviceInfos = this.mAuthenticatorCache.getAllServices(userId);
            for (ServiceInfo<AuthenticatorDescription> serviceInfo : serviceInfos) {
                int sigChk = this.mPackageManager.checkSignatures(serviceInfo.uid, callingUid);
                if (isOtherwisePermitted || sigChk == 0) {
                    managedAccountTypes.add(((AuthenticatorDescription) serviceInfo.type).type);
                }
            }
            return managedAccountTypes;
        } finally {
            Binder.restoreCallingIdentity(identityToken);
        }
    }

    private boolean isAccountPresentForCaller(String accountName, String accountType) {
        if (getUserAccountsForCaller().accountCache.containsKey(accountType)) {
            Account[] accountArr = (Account[]) getUserAccountsForCaller().accountCache.get(accountType);
            int length = accountArr.length;
            for (int i = 0; i < length; i += DE_DATABASE_VERSION) {
                if (accountArr[i].name.equals(accountName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void checkManageUsersPermission(String message) {
        if (ActivityManager.checkComponentPermission("android.permission.MANAGE_USERS", Binder.getCallingUid(), -1, true) != 0) {
            throw new SecurityException("You need MANAGE_USERS permission to: " + message);
        }
    }

    private static void checkManageOrCreateUsersPermission(String message) {
        if (ActivityManager.checkComponentPermission("android.permission.MANAGE_USERS", Binder.getCallingUid(), -1, true) != 0 && ActivityManager.checkComponentPermission("android.permission.CREATE_USERS", Binder.getCallingUid(), -1, true) != 0) {
            throw new SecurityException("You need MANAGE_USERS or CREATE_USERS permission to: " + message);
        }
    }

    private boolean hasExplicitlyGrantedPermission(Account account, String authTokenType, int callerUid) {
        boolean permissionGranted = false;
        if (callerUid == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
            return true;
        }
        UserAccounts accounts = getUserAccountsForCaller();
        synchronized (accounts.cacheLock) {
            SQLiteDatabase db = accounts.openHelper.getReadableDatabase();
            String[] args = new String[MESSAGE_COPY_SHARED_ACCOUNT];
            args[0] = String.valueOf(callerUid);
            args[DE_DATABASE_VERSION] = authTokenType;
            args[2] = account.name;
            args[MESSAGE_TIMED_OUT] = account.type;
            if (DatabaseUtils.longForQuery(db, COUNT_OF_MATCHING_GRANTS, args) != 0) {
                permissionGranted = true;
            }
            if (permissionGranted || !ActivityManager.isRunningInTestHarness()) {
                return permissionGranted;
            }
            Log.d(TAG, "no credentials permission for usage of , " + authTokenType + " by uid " + callerUid + " but ignoring since device is in test harness.");
            return true;
        }
    }

    private boolean isSystemUid(int callingUid) {
        String[] strArr = null;
        long ident = Binder.clearCallingIdentity();
        try {
            strArr = this.mPackageManager.getPackagesForUid(callingUid);
            if (strArr != null) {
                int length = strArr.length;
                for (int i = 0; i < length; i += DE_DATABASE_VERSION) {
                    String name = strArr[i];
                    try {
                        PackageInfo packageInfo = this.mPackageManager.getPackageInfo(name, 0);
                        if (!(packageInfo == null || (packageInfo.applicationInfo.flags & DE_DATABASE_VERSION) == 0)) {
                            return true;
                        }
                    } catch (NameNotFoundException e) {
                        String str = TAG;
                        Object[] objArr = new Object[DE_DATABASE_VERSION];
                        objArr[0] = name;
                        Log.w(str, String.format("Could not find package [%s]", objArr), e);
                    }
                }
            } else {
                Log.w(TAG, "No known packages with uid " + callingUid);
            }
            return false;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void checkReadAccountsPermitted(int callingUid, String accountType, int userId, String opPackageName) {
        if (!isAccountVisibleToCaller(accountType, callingUid, userId, opPackageName)) {
            String msg = String.format("caller uid %s cannot access %s accounts", new Object[]{Integer.valueOf(callingUid), accountType});
            Log.w(TAG, "  " + msg);
            throw new SecurityException(msg);
        }
    }

    private boolean canUserModifyAccounts(int userId, int callingUid) {
        if (!isProfileOwner(callingUid) && getUserManager().getUserRestrictions(new UserHandle(userId)).getBoolean("no_modify_accounts")) {
            return false;
        }
        return true;
    }

    private boolean canUserModifyAccountsForType(int userId, String accountType, int callingUid) {
        if (isProfileOwner(callingUid)) {
            return true;
        }
        String[] typesArray = ((DevicePolicyManager) this.mContext.getSystemService("device_policy")).getAccountTypesWithManagementDisabledAsUser(userId);
        if (typesArray == null) {
            return true;
        }
        int length = typesArray.length;
        for (int i = 0; i < length; i += DE_DATABASE_VERSION) {
            if (typesArray[i].equals(accountType)) {
                return false;
            }
        }
        return true;
    }

    private boolean isProfileOwner(int uid) {
        DevicePolicyManagerInternal dpmi = (DevicePolicyManagerInternal) LocalServices.getService(DevicePolicyManagerInternal.class);
        if (dpmi != null) {
            return dpmi.isActiveAdminWithPolicy(uid, -1);
        }
        return false;
    }

    public void updateAppPermission(Account account, String authTokenType, int uid, boolean value) throws RemoteException {
        if (getCallingUid() != ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
            throw new SecurityException();
        } else if (value) {
            grantAppPermission(account, authTokenType, uid);
        } else {
            revokeAppPermission(account, authTokenType, uid);
        }
    }

    private void grantAppPermission(Account account, String authTokenType, int uid) {
        if (account == null || authTokenType == null) {
            Log.e(TAG, "grantAppPermission: called with invalid arguments", new Exception());
            return;
        }
        UserAccounts accounts = getUserAccounts(UserHandle.getUserId(uid));
        synchronized (accounts.cacheLock) {
            SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                long accountId = getAccountIdLocked(db, account);
                if (accountId >= 0) {
                    ContentValues values = new ContentValues();
                    values.put(GRANTS_ACCOUNTS_ID, Long.valueOf(accountId));
                    values.put(GRANTS_AUTH_TOKEN_TYPE, authTokenType);
                    values.put(GRANTS_GRANTEE_UID, Integer.valueOf(uid));
                    db.insert(TABLE_GRANTS, GRANTS_ACCOUNTS_ID, values);
                    db.setTransactionSuccessful();
                }
                db.endTransaction();
                cancelNotification(getCredentialPermissionNotificationId(account, authTokenType, uid).intValue(), UserHandle.of(accounts.userId));
            } catch (Throwable th) {
                db.endTransaction();
            }
        }
    }

    private void revokeAppPermission(Account account, String authTokenType, int uid) {
        if (account == null || authTokenType == null) {
            Log.e(TAG, "revokeAppPermission: called with invalid arguments", new Exception());
            return;
        }
        UserAccounts accounts = getUserAccounts(UserHandle.getUserId(uid));
        synchronized (accounts.cacheLock) {
            SQLiteDatabase db = accounts.openHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                long accountId = getAccountIdLocked(db, account);
                if (accountId >= 0) {
                    String[] strArr = new String[MESSAGE_TIMED_OUT];
                    strArr[0] = String.valueOf(accountId);
                    strArr[DE_DATABASE_VERSION] = authTokenType;
                    strArr[2] = String.valueOf(uid);
                    db.delete(TABLE_GRANTS, "accounts_id=? AND auth_token_type=? AND uid=?", strArr);
                    db.setTransactionSuccessful();
                }
                db.endTransaction();
                cancelNotification(getCredentialPermissionNotificationId(account, authTokenType, uid).intValue(), new UserHandle(accounts.userId));
            } catch (Throwable th) {
                db.endTransaction();
            }
        }
    }

    private static final String stringArrayToString(String[] value) {
        return value != null ? "[" + TextUtils.join(",", value) + "]" : null;
    }

    private void removeAccountFromCacheLocked(UserAccounts accounts, Account account) {
        Account[] oldAccountsForType = (Account[]) accounts.accountCache.get(account.type);
        if (oldAccountsForType != null) {
            ArrayList<Account> newAccountsList = new ArrayList();
            int length = oldAccountsForType.length;
            for (int i = 0; i < length; i += DE_DATABASE_VERSION) {
                Account curAccount = oldAccountsForType[i];
                if (!curAccount.equals(account)) {
                    newAccountsList.add(curAccount);
                }
            }
            if (newAccountsList.isEmpty()) {
                accounts.accountCache.remove(account.type);
            } else {
                accounts.accountCache.put(account.type, (Account[]) newAccountsList.toArray(new Account[newAccountsList.size()]));
            }
        }
        accounts.userDataCache.remove(account);
        accounts.authTokenCache.remove(account);
        accounts.previousNameCache.remove(account);
    }

    private void insertAccountIntoCacheLocked(UserAccounts accounts, Account account) {
        int oldLength;
        Account[] accountsForType = (Account[]) accounts.accountCache.get(account.type);
        if (accountsForType != null) {
            oldLength = accountsForType.length;
        } else {
            oldLength = 0;
        }
        Account[] newAccountsForType = new Account[(oldLength + DE_DATABASE_VERSION)];
        if (accountsForType != null) {
            System.arraycopy(accountsForType, 0, newAccountsForType, 0, oldLength);
        }
        newAccountsForType[oldLength] = account;
        accounts.accountCache.put(account.type, newAccountsForType);
    }

    private Account[] filterSharedAccounts(UserAccounts userAccounts, Account[] unfiltered, int callingUid, String callingPackage) {
        if (getUserManager() == null || userAccounts == null || userAccounts.userId < 0 || callingUid == Process.myUid()) {
            return unfiltered;
        }
        UserInfo user = getUserManager().getUserInfo(userAccounts.userId);
        if (user == null || !user.isRestricted()) {
            return unfiltered;
        }
        int i;
        String[] packages = this.mPackageManager.getPackagesForUid(callingUid);
        String whiteList = this.mContext.getResources().getString(17039459);
        int length = packages.length;
        for (i = 0; i < length; i += DE_DATABASE_VERSION) {
            String packageName = packages[i];
            if (whiteList.contains(";" + packageName + ";")) {
                return unfiltered;
            }
        }
        ArrayList<Account> allowed = new ArrayList();
        Account[] sharedAccounts = getSharedAccountsAsUser(userAccounts.userId);
        if (sharedAccounts == null || sharedAccounts.length == 0) {
            return unfiltered;
        }
        String requiredAccountType = "";
        PackageInfo pi;
        if (callingPackage == null) {
            length = packages.length;
            for (i = 0; i < length; i += DE_DATABASE_VERSION) {
                packageName = packages[i];
                pi = this.mPackageManager.getPackageInfo(packageName, 0);
                if (pi != null && pi.restrictedAccountType != null) {
                    requiredAccountType = pi.restrictedAccountType;
                    break;
                }
            }
        } else {
            try {
                pi = this.mPackageManager.getPackageInfo(callingPackage, 0);
                if (!(pi == null || pi.restrictedAccountType == null)) {
                    requiredAccountType = pi.restrictedAccountType;
                }
            } catch (NameNotFoundException e) {
            }
        }
        int length2 = unfiltered.length;
        for (length = 0; length < length2; length += DE_DATABASE_VERSION) {
            Account account = unfiltered[length];
            if (account.type.equals(requiredAccountType)) {
                allowed.add(account);
            } else {
                boolean found = false;
                int length3 = sharedAccounts.length;
                for (i = 0; i < length3; i += DE_DATABASE_VERSION) {
                    if (sharedAccounts[i].equals(account)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    allowed.add(account);
                }
            }
        }
        Account[] filtered = new Account[allowed.size()];
        allowed.toArray(filtered);
        return filtered;
    }

    protected Account[] getAccountsFromCacheLocked(UserAccounts userAccounts, String accountType, int callingUid, String callingPackage) {
        Account[] accounts;
        if (accountType != null) {
            accounts = (Account[]) userAccounts.accountCache.get(accountType);
            if (accounts == null) {
                return EMPTY_ACCOUNT_ARRAY;
            }
            return filterSharedAccounts(userAccounts, (Account[]) Arrays.copyOf(accounts, accounts.length), callingUid, callingPackage);
        }
        int totalLength = 0;
        for (Account[] accounts2 : userAccounts.accountCache.values()) {
            totalLength += accounts2.length;
        }
        if (totalLength == 0) {
            return EMPTY_ACCOUNT_ARRAY;
        }
        accounts2 = new Account[totalLength];
        totalLength = 0;
        for (Account[] accountsOfType : userAccounts.accountCache.values()) {
            System.arraycopy(accountsOfType, 0, accounts2, totalLength, accountsOfType.length);
            totalLength += accountsOfType.length;
        }
        return filterSharedAccounts(userAccounts, accounts2, callingUid, callingPackage);
    }

    protected void writeUserDataIntoCacheLocked(UserAccounts accounts, SQLiteDatabase db, Account account, String key, String value) {
        HashMap<String, String> userDataForAccount = (HashMap) accounts.userDataCache.get(account);
        if (userDataForAccount == null) {
            userDataForAccount = readUserDataForAccountFromDatabaseLocked(db, account);
            accounts.userDataCache.put(account, userDataForAccount);
        }
        if (value == null) {
            userDataForAccount.remove(key);
        } else {
            userDataForAccount.put(key, value);
        }
    }

    protected String readCachedTokenInternal(UserAccounts accounts, Account account, String tokenType, String callingPackage, byte[] pkgSigDigest) {
        String str;
        synchronized (accounts.cacheLock) {
            str = accounts.accountTokenCaches.get(account, tokenType, callingPackage, pkgSigDigest);
        }
        return str;
    }

    protected void writeAuthTokenIntoCacheLocked(UserAccounts accounts, SQLiteDatabase db, Account account, String key, String value) {
        HashMap<String, String> authTokensForAccount = (HashMap) accounts.authTokenCache.get(account);
        if (authTokensForAccount == null) {
            authTokensForAccount = readAuthTokensForAccountFromDatabaseLocked(db, account);
            accounts.authTokenCache.put(account, authTokensForAccount);
        }
        if (value == null) {
            authTokensForAccount.remove(key);
        } else {
            authTokensForAccount.put(key, value);
        }
    }

    protected String readAuthTokenInternal(UserAccounts accounts, Account account, String authTokenType) {
        String str;
        synchronized (accounts.cacheLock) {
            HashMap<String, String> authTokensForAccount = (HashMap) accounts.authTokenCache.get(account);
            if (authTokensForAccount == null) {
                authTokensForAccount = readAuthTokensForAccountFromDatabaseLocked(accounts.openHelper.getReadableDatabaseUserIsUnlocked(), account);
                accounts.authTokenCache.put(account, authTokensForAccount);
            }
            str = (String) authTokensForAccount.get(authTokenType);
        }
        return str;
    }

    protected String readUserDataInternalLocked(UserAccounts accounts, Account account, String key) {
        HashMap<String, String> userDataForAccount = (HashMap) accounts.userDataCache.get(account);
        if (userDataForAccount == null) {
            userDataForAccount = readUserDataForAccountFromDatabaseLocked(accounts.openHelper.getReadableDatabaseUserIsUnlocked(), account);
            accounts.userDataCache.put(account, userDataForAccount);
        }
        return (String) userDataForAccount.get(key);
    }

    protected HashMap<String, String> readUserDataForAccountFromDatabaseLocked(SQLiteDatabase db, Account account) {
        HashMap<String, String> userDataForAccount = new HashMap();
        Cursor cursor = db.query(CE_TABLE_EXTRAS, COLUMNS_EXTRAS_KEY_AND_VALUE, SELECTION_USERDATA_BY_ACCOUNT, new String[]{account.name, account.type}, null, null, null);
        while (cursor.moveToNext()) {
            try {
                userDataForAccount.put(cursor.getString(0), cursor.getString(DE_DATABASE_VERSION));
            } finally {
                cursor.close();
            }
        }
        return userDataForAccount;
    }

    protected HashMap<String, String> readAuthTokensForAccountFromDatabaseLocked(SQLiteDatabase db, Account account) {
        HashMap<String, String> authTokensForAccount = new HashMap();
        Cursor cursor = db.query(CE_TABLE_AUTHTOKENS, COLUMNS_AUTHTOKENS_TYPE_AND_AUTHTOKEN, SELECTION_USERDATA_BY_ACCOUNT, new String[]{account.name, account.type}, null, null, null);
        while (cursor.moveToNext()) {
            try {
                authTokensForAccount.put(cursor.getString(0), cursor.getString(DE_DATABASE_VERSION));
            } finally {
                cursor.close();
            }
        }
        return authTokensForAccount;
    }

    private Context getContextForUser(UserHandle user) {
        try {
            return this.mContext.createPackageContextAsUser(this.mContext.getPackageName(), 0, user);
        } catch (NameNotFoundException e) {
            return this.mContext;
        }
    }

    private void sendResponse(IAccountManagerResponse response, Bundle result) {
        try {
            response.onResult(result);
        } catch (RemoteException e) {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "failure while notifying response", e);
            }
        }
    }

    private void sendErrorResponse(IAccountManagerResponse response, int errorCode, String errorMessage) {
        try {
            response.onError(errorCode, errorMessage);
        } catch (RemoteException e) {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "failure while notifying response", e);
            }
        }
    }
}
