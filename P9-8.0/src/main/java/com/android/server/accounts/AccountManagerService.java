package com.android.server.accounts;

import android.accounts.Account;
import android.accounts.AccountAndUser;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManagerInternal;
import android.accounts.AccountManagerInternal.OnAppPermissionChangeListener;
import android.accounts.AccountManagerResponse;
import android.accounts.AuthenticatorDescription;
import android.accounts.CantAddAccountActivity;
import android.accounts.ChooseAccountActivity;
import android.accounts.GrantCredentialsPermissionActivity;
import android.accounts.IAccountAuthenticator;
import android.accounts.IAccountAuthenticatorResponse;
import android.accounts.IAccountManager.Stub;
import android.accounts.IAccountManagerResponse;
import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.AppOpsManager;
import android.app.AppOpsManager.OnOpChangedInternalListener;
import android.app.INotificationManager;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.app.admin.DevicePolicyManagerInternal;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.RegisteredServicesCache.ServiceInfo;
import android.content.pm.RegisteredServicesCacheListener;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.pm.UserInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.PackageMonitor;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.server.LocalServices;
import com.android.server.ServiceThread;
import com.android.server.SystemService;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.os.HwBootFail;
import com.google.android.collect.Lists;
import com.google.android.collect.Sets;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class AccountManagerService extends Stub implements RegisteredServicesCacheListener<AuthenticatorDescription> {
    private static final Intent ACCOUNTS_CHANGED_INTENT = new Intent("android.accounts.LOGIN_ACCOUNTS_CHANGED");
    private static final Account[] EMPTY_ACCOUNT_ARRAY = new Account[0];
    private static final int MESSAGE_COPY_SHARED_ACCOUNT = 4;
    private static final int MESSAGE_TIMED_OUT = 3;
    private static final String PRE_N_DATABASE_NAME = "accounts.db";
    private static final int SIGNATURE_CHECK_MATCH = 1;
    private static final int SIGNATURE_CHECK_MISMATCH = 0;
    private static final int SIGNATURE_CHECK_UID_MATCH = 2;
    private static final String TAG = "AccountManagerService";
    private static AtomicReference<AccountManagerService> sThis = new AtomicReference();
    private final AppOpsManager mAppOpsManager;
    private CopyOnWriteArrayList<OnAppPermissionChangeListener> mAppPermissionChangeListeners = new CopyOnWriteArrayList();
    private final IAccountAuthenticatorCache mAuthenticatorCache;
    final Context mContext;
    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    final MessageHandler mHandler;
    private final Injector mInjector;
    private final SparseBooleanArray mLocalUnlockedUsers = new SparseBooleanArray();
    private final PackageManager mPackageManager;
    private final LinkedHashMap<String, Session> mSessions = new LinkedHashMap();
    private UserManager mUserManager;
    private final SparseArray<UserAccounts> mUsers = new SparseArray();

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
            intent.setFlags(intent.getFlags() & -196);
            long bid = Binder.clearCallingIdentity();
            try {
                PackageManager pm = AccountManagerService.this.mContext.getPackageManager();
                ActivityInfo targetActivityInfo = pm.resolveActivityAsUser(intent, 0, this.mAccounts.userId).activityInfo;
                int targetUid = targetActivityInfo.applicationInfo.uid;
                if (isExportedSystemActivity(targetActivityInfo) || pm.checkSignatures(authUid, targetUid) == 0) {
                    Binder.restoreCallingIdentity(bid);
                    return;
                }
                String pkgName = targetActivityInfo.packageName;
                throw new SecurityException(String.format("KEY_INTENT resolved to an Activity (%s) in a package (%s) that does not share a signature with the supplying authenticator (%s).", new Object[]{targetActivityInfo.name, pkgName, this.mAccountType}));
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(bid);
            }
        }

        private boolean isExportedSystemActivity(ActivityInfo activityInfo) {
            String className = activityInfo.name;
            if (!"android".equals(activityInfo.packageName)) {
                return false;
            }
            if (GrantCredentialsPermissionActivity.class.getName().equals(className)) {
                return true;
            }
            return CantAddAccountActivity.class.getName().equals(className);
        }

        /* JADX WARNING: Missing block: B:9:0x001d, code:
            if (r4.mResponse == null) goto L_0x002b;
     */
        /* JADX WARNING: Missing block: B:10:0x001f, code:
            r4.mResponse.asBinder().unlinkToDeath(r4, 0);
            r4.mResponse = null;
     */
        /* JADX WARNING: Missing block: B:11:0x002b, code:
            cancelTimeout();
            unbind();
     */
        /* JADX WARNING: Missing block: B:12:0x0031, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void close() {
            synchronized (AccountManagerService.this.mSessions) {
                if (AccountManagerService.this.mSessions.remove(toString()) == null) {
                }
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
                onError(1, "bind failure");
            }
        }

        private void unbind() {
            if (this.mAuthenticator != null) {
                this.mAuthenticator = null;
                AccountManagerService.this.mContext.unbindService(this);
            }
        }

        public void cancelTimeout() {
            AccountManagerService.this.mHandler.removeMessages(3, this);
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            this.mAuthenticator = IAccountAuthenticator.Stub.asInterface(service);
            try {
                run();
            } catch (RemoteException e) {
                onError(1, "remote exception");
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            this.mAuthenticator = null;
            IAccountManagerResponse response = getResponseAndClose();
            if (response != null) {
                try {
                    response.onError(1, "disconnected");
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
                    response.onError(1, "timeout");
                } catch (RemoteException e) {
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, "Session.onTimedOut: caught RemoteException while responding", e);
                    }
                }
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:69:? A:{SYNTHETIC, RETURN} */
        /* JADX WARNING: Removed duplicated region for block: B:41:0x012c  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onResult(Bundle result) {
            IAccountManagerResponse response;
            Bundle.setDefusable(result, true);
            this.mNumResults++;
            Intent intent = null;
            if (result != null) {
                boolean isSuccessfulUpdateCredsOrAddAccount;
                boolean isSuccessfulConfirmCreds = result.getBoolean("booleanResult", false);
                if (result.containsKey("authAccount")) {
                    isSuccessfulUpdateCredsOrAddAccount = result.containsKey("accountType");
                } else {
                    isSuccessfulUpdateCredsOrAddAccount = false;
                }
                boolean needUpdate = this.mUpdateLastAuthenticatedTime ? !isSuccessfulConfirmCreds ? isSuccessfulUpdateCredsOrAddAccount : true : false;
                if (needUpdate || this.mAuthDetailsRequired) {
                    boolean accountPresent = AccountManagerService.this.isAccountPresentForCaller(this.mAccountName, this.mAccountType);
                    if (needUpdate && accountPresent) {
                        AccountManagerService.this.updateLastAuthenticatedTime(new Account(this.mAccountName, this.mAccountType));
                    }
                    if (this.mAuthDetailsRequired) {
                        long lastAuthenticatedTime = -1;
                        if (accountPresent) {
                            lastAuthenticatedTime = this.mAccounts.accountsDb.findAccountLastAuthenticatedTime(new Account(this.mAccountName, this.mAccountType));
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
                if ((TextUtils.isEmpty(result.getString("authtoken")) ^ 1) != 0) {
                    String accountName = result.getString("authAccount");
                    String accountType = result.getString("accountType");
                    if (!(TextUtils.isEmpty(accountName) || (TextUtils.isEmpty(accountType) ^ 1) == 0)) {
                        AccountManagerService.this.cancelNotification(AccountManagerService.this.getSigninRequiredNotificationId(this.mAccounts, new Account(accountName, accountType)), new UserHandle(this.mAccounts.userId));
                    }
                }
            }
            if (this.mExpectActivityLaunch && result != null) {
                if (result.containsKey(HwBroadcastRadarUtil.KEY_BROADCAST_INTENT)) {
                    response = this.mResponse;
                    if (response != null) {
                        return;
                    }
                    if (result == null) {
                        try {
                            if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                                Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onError() on response " + response);
                            }
                            response.onError(5, "null bundle returned");
                            return;
                        } catch (RemoteException e) {
                            if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                                Log.v(AccountManagerService.TAG, "failure while notifying response", e);
                                return;
                            }
                            return;
                        }
                    }
                    if (this.mStripAuthTokenFromResult) {
                        result.remove("authtoken");
                    }
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
                    }
                    if (result.getInt("errorCode", -1) <= 0 || intent != null) {
                        response.onResult(result);
                        return;
                    } else {
                        response.onError(result.getInt("errorCode"), result.getString("errorMessage"));
                        return;
                    }
                }
            }
            response = getResponseAndClose();
            if (response != null) {
            }
        }

        public void onRequestContinued() {
            this.mNumRequestContinued++;
        }

        public void onError(int errorCode, String errorMessage) {
            this.mNumErrors++;
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
            } else if (AccountManagerService.this.isLocalUnlockedUser(this.mAccounts.userId) || (authenticatorInfo.componentInfo.directBootAware ^ 1) == 0) {
                Intent intent = new Intent();
                intent.setAction("android.accounts.AccountAuthenticator");
                intent.setComponent(authenticatorInfo.componentName);
                if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                    Log.v(AccountManagerService.TAG, "performing bindService to " + authenticatorInfo.componentName);
                }
                if (AccountManagerService.this.mContext.bindServiceAsUser(intent, this, 1, UserHandle.of(this.mAccounts.userId))) {
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

    private abstract class StartAccountSession extends Session {
        private final boolean mIsPasswordForwardingAllowed;

        public StartAccountSession(UserAccounts accounts, IAccountManagerResponse response, String accountType, boolean expectActivityLaunch, String accountName, boolean authDetailsRequired, boolean updateLastAuthenticationTime, boolean isPasswordForwardingAllowed) {
            super(accounts, response, accountType, expectActivityLaunch, true, accountName, authDetailsRequired, updateLastAuthenticationTime);
            this.mIsPasswordForwardingAllowed = isPasswordForwardingAllowed;
        }

        public void onResult(Bundle result) {
            IAccountManagerResponse response;
            Bundle.setDefusable(result, true);
            this.mNumResults++;
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
                } else if (result.getInt("errorCode", -1) <= 0 || intent != null) {
                    if (!this.mIsPasswordForwardingAllowed) {
                        result.remove("password");
                    }
                    result.remove("authtoken");
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
                    }
                    Bundle sessionBundle = result.getBundle("accountSessionBundle");
                    if (sessionBundle != null) {
                        String accountType = sessionBundle.getString("accountType");
                        if (TextUtils.isEmpty(accountType) || (this.mAccountType.equalsIgnoreCase(accountType) ^ 1) != 0) {
                            Log.w(AccountManagerService.TAG, "Account type in session bundle doesn't match request.");
                        }
                        sessionBundle.putString("accountType", this.mAccountType);
                        try {
                            result.putBundle("accountSessionBundle", CryptoHelper.getInstance().encryptBundle(sessionBundle));
                        } catch (GeneralSecurityException e) {
                            if (Log.isLoggable(AccountManagerService.TAG, 3)) {
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

    /* renamed from: com.android.server.accounts.AccountManagerService$1LogRecordTask */
    class AnonymousClass1LogRecordTask implements Runnable {
        private final long accountId;
        private final String action;
        private final int callingUid;
        private final String tableName;
        private final UserAccounts userAccount;
        private final long userDebugDbInsertionPoint;

        AnonymousClass1LogRecordTask(String action, String tableName, long accountId, UserAccounts userAccount, int callingUid, long userDebugDbInsertionPoint) {
            this.action = action;
            this.tableName = tableName;
            this.accountId = accountId;
            this.userAccount = userAccount;
            this.callingUid = callingUid;
            this.userDebugDbInsertionPoint = userDebugDbInsertionPoint;
        }

        public void run() {
            SQLiteStatement logStatement = this.userAccount.statementForLogging;
            logStatement.bindLong(1, this.accountId);
            logStatement.bindString(2, this.action);
            logStatement.bindString(3, AccountManagerService.this.mDateFormat.format(new Date()));
            logStatement.bindLong(4, (long) this.callingUid);
            logStatement.bindString(5, this.tableName);
            logStatement.bindLong(6, this.userDebugDbInsertionPoint);
            logStatement.execute();
            logStatement.clearBindings();
        }
    }

    private final class AccountManagerInternalImpl extends AccountManagerInternal {
        @GuardedBy("mLock")
        private AccountManagerBackupHelper mBackupHelper;
        private final Object mLock;

        /* synthetic */ AccountManagerInternalImpl(AccountManagerService this$0, AccountManagerInternalImpl -this1) {
            this();
        }

        private AccountManagerInternalImpl() {
            this.mLock = new Object();
        }

        public void requestAccountAccess(Account account, String packageName, int userId, RemoteCallback callback) {
            if (account == null) {
                Slog.w(AccountManagerService.TAG, "account cannot be null");
            } else if (packageName == null) {
                Slog.w(AccountManagerService.TAG, "packageName cannot be null");
            } else if (userId < 0) {
                Slog.w(AccountManagerService.TAG, "user id must be concrete");
            } else if (callback == null) {
                Slog.w(AccountManagerService.TAG, "callback cannot be null");
            } else if (AccountManagerService.this.resolveAccountVisibility(account, packageName, AccountManagerService.this.getUserAccounts(userId)).intValue() == 3) {
                Slog.w(AccountManagerService.TAG, "requestAccountAccess: account is hidden");
            } else if (AccountManagerService.this.hasAccountAccess(account, packageName, new UserHandle(userId))) {
                Bundle result = new Bundle();
                result.putBoolean("booleanResult", true);
                callback.sendResult(result);
            } else {
                try {
                    UserAccounts userAccounts;
                    int uid = AccountManagerService.this.mPackageManager.getPackageUidAsUser(packageName, userId);
                    Intent intent = AccountManagerService.this.newRequestAccountAccessIntent(account, packageName, uid, callback);
                    synchronized (AccountManagerService.this.mUsers) {
                        userAccounts = (UserAccounts) AccountManagerService.this.mUsers.get(userId);
                    }
                    SystemNotificationChannels.createAccountChannelForPackage(packageName, uid, AccountManagerService.this.mContext);
                    AccountManagerService.this.doNotification(userAccounts, account, null, intent, packageName, userId);
                } catch (NameNotFoundException e) {
                    Slog.e(AccountManagerService.TAG, "Unknown package " + packageName);
                }
            }
        }

        public void addOnAppPermissionChangeListener(OnAppPermissionChangeListener listener) {
            AccountManagerService.this.mAppPermissionChangeListeners.add(listener);
        }

        public boolean hasAccountAccess(Account account, int uid) {
            return AccountManagerService.this.hasAccountAccess(account, null, uid);
        }

        public byte[] backupAccountAccessPermissions(int userId) {
            byte[] backupAccountAccessPermissions;
            synchronized (this.mLock) {
                if (this.mBackupHelper == null) {
                    this.mBackupHelper = new AccountManagerBackupHelper(AccountManagerService.this, this);
                }
                backupAccountAccessPermissions = this.mBackupHelper.backupAccountAccessPermissions(userId);
            }
            return backupAccountAccessPermissions;
        }

        public void restoreAccountAccessPermissions(byte[] data, int userId) {
            synchronized (this.mLock) {
                if (this.mBackupHelper == null) {
                    this.mBackupHelper = new AccountManagerBackupHelper(AccountManagerService.this, this);
                }
                this.mBackupHelper.restoreAccountAccessPermissions(data, userId);
            }
        }
    }

    private class GetAccountsByTypeAndFeatureSession extends Session {
        private volatile Account[] mAccountsOfType = null;
        private volatile ArrayList<Account> mAccountsWithFeatures = null;
        private final int mCallingUid;
        private volatile int mCurrentAccount = 0;
        private final String[] mFeatures;
        private final boolean mIncludeManagedNotVisible;
        private final String mPackageName;

        public GetAccountsByTypeAndFeatureSession(UserAccounts accounts, IAccountManagerResponse response, String type, String[] features, int callingUid, String packageName, boolean includeManagedNotVisible) {
            super(AccountManagerService.this, accounts, response, type, false, true, null, false);
            this.mCallingUid = callingUid;
            this.mFeatures = features;
            this.mPackageName = packageName;
            this.mIncludeManagedNotVisible = includeManagedNotVisible;
        }

        public void run() throws RemoteException {
            this.mAccountsOfType = AccountManagerService.this.getAccountsFromCache(this.mAccounts, this.mAccountType, this.mCallingUid, this.mPackageName, this.mIncludeManagedNotVisible);
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
                onError(1, "remote exception");
            }
        }

        public void onResult(Bundle result) {
            Bundle.setDefusable(result, true);
            this.mNumResults++;
            if (result == null) {
                onError(5, "null bundle");
                return;
            }
            if (result.getBoolean("booleanResult", false)) {
                this.mAccountsWithFeatures.add(this.mAccountsOfType[this.mCurrentAccount]);
            }
            this.mCurrentAccount++;
            checkAccount();
        }

        public void sendResult() {
            IAccountManagerResponse response = getResponseAndClose();
            if (response != null) {
                try {
                    Account[] accounts = new Account[this.mAccountsWithFeatures.size()];
                    for (int i = 0; i < accounts.length; i++) {
                        accounts[i] = (Account) this.mAccountsWithFeatures.get(i);
                    }
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
                    }
                    Bundle result = new Bundle();
                    result.putParcelableArray("accounts", accounts);
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

    static class Injector {
        private final Context mContext;

        public Injector(Context context) {
            this.mContext = context;
        }

        Looper getMessageHandlerLooper() {
            ServiceThread serviceThread = new ServiceThread(AccountManagerService.TAG, -2, true);
            serviceThread.start();
            return serviceThread.getLooper();
        }

        Context getContext() {
            return this.mContext;
        }

        void addLocalService(AccountManagerInternal service) {
            LocalServices.addService(AccountManagerInternal.class, service);
        }

        String getDeDatabaseName(int userId) {
            return new File(Environment.getDataSystemDeDirectory(userId), "accounts_de.db").getPath();
        }

        String getCeDatabaseName(int userId) {
            return new File(Environment.getDataSystemCeDirectory(userId), "accounts_ce.db").getPath();
        }

        String getPreNDatabaseName(int userId) {
            File systemDir = Environment.getDataSystemDirectory();
            File databaseFile = new File(Environment.getUserSystemDirectory(userId), AccountManagerService.PRE_N_DATABASE_NAME);
            if (userId == 0) {
                File oldFile = new File(systemDir, AccountManagerService.PRE_N_DATABASE_NAME);
                if (oldFile.exists() && (databaseFile.exists() ^ 1) != 0) {
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

        IAccountAuthenticatorCache getAccountAuthenticatorCache() {
            return new AccountAuthenticatorCache(this.mContext);
        }

        INotificationManager getNotificationManager() {
            return NotificationManager.getService();
        }
    }

    public static class Lifecycle extends SystemService {
        private AccountManagerService mService;

        public Lifecycle(Context context) {
            super(context);
        }

        public void onStart() {
            this.mService = new AccountManagerService(new Injector(getContext()));
            publishBinderService("account", this.mService);
        }

        public void onUnlockUser(int userHandle) {
            this.mService.onUnlockUser(userHandle);
        }

        public void onStopUser(int userHandle) {
            this.mService.onStopUser(userHandle);
        }
    }

    class MessageHandler extends Handler {
        MessageHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 3:
                    msg.obj.onTimedOut();
                    return;
                case 4:
                    AccountManagerService.this.copyAccountToUser(null, (Account) msg.obj, msg.arg1, msg.arg2);
                    return;
                default:
                    throw new IllegalStateException("unhandled message: " + msg.what);
            }
        }
    }

    private class NotificationId {
        private final int mId;
        final String mTag;

        NotificationId(String tag, int type) {
            this.mTag = tag;
            this.mId = type;
        }
    }

    private class RemoveAccountSession extends Session {
        final Account mAccount;

        public RemoveAccountSession(UserAccounts accounts, IAccountManagerResponse response, Account account, boolean expectActivityLaunch) {
            super(AccountManagerService.this, accounts, response, account.type, expectActivityLaunch, true, account.name, false);
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
            if (!(result == null || !result.containsKey("booleanResult") || (result.containsKey(HwBroadcastRadarUtil.KEY_BROADCAST_INTENT) ^ 1) == 0)) {
                boolean removalAllowed = result.getBoolean("booleanResult");
                if (removalAllowed) {
                    AccountManagerService.this.removeAccountInternal(this.mAccounts, this.mAccount, getCallingUid());
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

        public TestFeaturesSession(UserAccounts accounts, IAccountManagerResponse response, Account account, String[] features) {
            super(AccountManagerService.this, accounts, response, account.type, false, true, account.name, false);
            this.mFeatures = features;
            this.mAccount = account;
        }

        public void run() throws RemoteException {
            try {
                this.mAuthenticator.hasFeatures(this, this.mAccount, this.mFeatures);
            } catch (RemoteException e) {
                onError(1, "remote exception");
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
        final HashMap<String, Account[]> accountCache = new LinkedHashMap();
        private final TokenCache accountTokenCaches = new TokenCache();
        final AccountsDb accountsDb;
        private final Map<Account, Map<String, String>> authTokenCache = new HashMap();
        final Object cacheLock = new Object();
        private final HashMap<Pair<Pair<Account, String>, Integer>, NotificationId> credentialsPermissionNotificationIds = new HashMap();
        final Object dbLock = new Object();
        private int debugDbInsertionPoint = -1;
        private final Map<String, Map<String, Integer>> mReceiversForType = new HashMap();
        private final HashMap<Account, AtomicReference<String>> previousNameCache = new HashMap();
        private final HashMap<Account, NotificationId> signinRequiredNotificationIds = new HashMap();
        private SQLiteStatement statementForLogging;
        private final Map<Account, Map<String, String>> userDataCache = new HashMap();
        private final int userId;
        private final Map<Account, Map<String, Integer>> visibilityCache = new HashMap();

        UserAccounts(Context context, int userId, File preNDbFile, File deDbFile) {
            this.userId = userId;
            synchronized (this.dbLock) {
                synchronized (this.cacheLock) {
                    this.accountsDb = AccountsDb.create(context, userId, preNDbFile, deDbFile);
                }
            }
        }
    }

    static {
        ACCOUNTS_CHANGED_INTENT.setFlags(83886080);
    }

    public static AccountManagerService getSingleton() {
        return (AccountManagerService) sThis.get();
    }

    public AccountManagerService(Injector injector) {
        this.mInjector = injector;
        this.mContext = injector.getContext();
        this.mPackageManager = this.mContext.getPackageManager();
        this.mAppOpsManager = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        this.mHandler = new MessageHandler(injector.getMessageHandlerLooper());
        this.mAuthenticatorCache = this.mInjector.getAccountAuthenticatorCache();
        this.mAuthenticatorCache.setListener(this, null);
        sThis.set(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context1, Intent intent) {
                if (!intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                    final String removedPackageName = intent.getData().getSchemeSpecificPart();
                    AccountManagerService.this.mHandler.post(new Runnable() {
                        public void run() {
                            AccountManagerService.this.purgeOldGrantsAll();
                            AccountManagerService.this.removeVisibilityValuesForPackage(removedPackageName);
                        }
                    });
                }
            }
        }, intentFilter);
        injector.addLocalService(new AccountManagerInternalImpl(this, null));
        new PackageMonitor() {
            public void onPackageAdded(String packageName, int uid) {
                AccountManagerService.this.cancelAccountAccessRequestNotificationIfNeeded(uid, true);
            }

            public void onPackageUpdateFinished(String packageName, int uid) {
                AccountManagerService.this.cancelAccountAccessRequestNotificationIfNeeded(uid, true);
            }
        }.register(this.mContext, this.mHandler.getLooper(), UserHandle.ALL, true);
        this.mAppOpsManager.startWatchingMode(62, null, new OnOpChangedInternalListener() {
            public void onOpChanged(int op, String packageName) {
                long identity;
                try {
                    int uid = AccountManagerService.this.mPackageManager.getPackageUidAsUser(packageName, ActivityManager.getCurrentUser());
                    if (AccountManagerService.this.mAppOpsManager.checkOpNoThrow(62, uid, packageName) == 0) {
                        identity = Binder.clearCallingIdentity();
                        AccountManagerService.this.cancelAccountAccessRequestNotificationIfNeeded(packageName, uid, true);
                        Binder.restoreCallingIdentity(identity);
                    }
                } catch (NameNotFoundException e) {
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        });
        this.mPackageManager.addOnPermissionsChangeListener(new -$Lambda$JwXVQhqSYlVkCeKB5Nx7U6RsZlU(this));
    }

    /* synthetic */ void lambda$-com_android_server_accounts_AccountManagerService_16015(int uid) {
        Account[] accounts = null;
        String[] packageNames = this.mPackageManager.getPackagesForUid(uid);
        if (packageNames != null) {
            int userId = UserHandle.getUserId(uid);
            long identity = Binder.clearCallingIdentity();
            try {
                for (String packageName : packageNames) {
                    if (this.mPackageManager.checkPermission("android.permission.GET_ACCOUNTS", packageName) == 0) {
                        if (accounts == null) {
                            accounts = getAccountsAsUser(null, userId, "android");
                            if (ArrayUtils.isEmpty(accounts)) {
                                return;
                            }
                        }
                        for (Account account : accounts) {
                            cancelAccountAccessRequestNotificationIfNeeded(account, uid, packageName, true);
                        }
                    }
                }
                Binder.restoreCallingIdentity(identity);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    private void cancelAccountAccessRequestNotificationIfNeeded(int uid, boolean checkAccess) {
        for (Account account : getAccountsAsUser(null, UserHandle.getUserId(uid), "android")) {
            cancelAccountAccessRequestNotificationIfNeeded(account, uid, checkAccess);
        }
    }

    private void cancelAccountAccessRequestNotificationIfNeeded(String packageName, int uid, boolean checkAccess) {
        for (Account account : getAccountsAsUser(null, UserHandle.getUserId(uid), "android")) {
            cancelAccountAccessRequestNotificationIfNeeded(account, uid, packageName, checkAccess);
        }
    }

    private void cancelAccountAccessRequestNotificationIfNeeded(Account account, int uid, boolean checkAccess) {
        String[] packageNames = this.mPackageManager.getPackagesForUid(uid);
        if (packageNames != null) {
            for (String packageName : packageNames) {
                cancelAccountAccessRequestNotificationIfNeeded(account, uid, packageName, checkAccess);
            }
        }
    }

    private void cancelAccountAccessRequestNotificationIfNeeded(Account account, int uid, String packageName, boolean checkAccess) {
        if (!checkAccess || hasAccountAccess(account, packageName, UserHandle.getUserHandleForUid(uid))) {
            cancelNotification(getCredentialPermissionNotificationId(account, "com.android.AccountManager.ACCOUNT_ACCESS_TOKEN_TYPE", uid), packageName, UserHandle.getUserHandleForUid(uid));
        }
    }

    public boolean addAccountExplicitlyWithVisibility(Account account, String password, Bundle extras, Map packageToVisibility) {
        Bundle.setDefusable(extras, true);
        int callingUid = Binder.getCallingUid();
        int userId = UserHandle.getCallingUserId();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "addAccountExplicitly: , caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        Preconditions.checkNotNull(account, "account cannot be null");
        if (isAccountManagedByCaller(account.type, callingUid, userId)) {
            long identityToken = clearCallingIdentity();
            try {
                boolean addAccountInternal = addAccountInternal(getUserAccounts(userId), account, password, extras, callingUid, packageToVisibility);
                return addAccountInternal;
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            throw new SecurityException(String.format("uid %s cannot explicitly add accounts of type: %s", new Object[]{Integer.valueOf(callingUid), account.type}));
        }
    }

    public Map<Account, Integer> getAccountsAndVisibilityForPackage(String packageName, String accountType) {
        int callingUid = Binder.getCallingUid();
        int userId = UserHandle.getCallingUserId();
        boolean isSystemUid = UserHandle.isSameApp(callingUid, 1000);
        List<String> managedTypes = getTypesForCaller(callingUid, userId, isSystemUid);
        if ((accountType == null || (managedTypes.contains(accountType) ^ 1) == 0) && (accountType != null || (isSystemUid ^ 1) == 0)) {
            if (accountType != null) {
                managedTypes = new ArrayList();
                managedTypes.add(accountType);
            }
            long identityToken = clearCallingIdentity();
            try {
                Map<Account, Integer> accountsAndVisibilityForPackage = getAccountsAndVisibilityForPackage(packageName, managedTypes, Integer.valueOf(callingUid), getUserAccounts(userId));
                return accountsAndVisibilityForPackage;
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            throw new SecurityException("getAccountsAndVisibilityForPackage() called from unauthorized uid " + callingUid + " with packageName=" + packageName);
        }
    }

    private Map<Account, Integer> getAccountsAndVisibilityForPackage(String packageName, List<String> accountTypes, Integer callingUid, UserAccounts accounts) {
        if (packageExistsForUser(packageName, accounts.userId)) {
            Map<Account, Integer> result = new LinkedHashMap();
            for (String accountType : accountTypes) {
                synchronized (accounts.dbLock) {
                    synchronized (accounts.cacheLock) {
                        Account[] accountsOfType = (Account[]) accounts.accountCache.get((String) accountType$iterator.next());
                        if (accountsOfType != null) {
                            for (Account account : accountsOfType) {
                                result.put(account, resolveAccountVisibility(account, packageName, accounts));
                            }
                        }
                    }
                }
            }
            return filterSharedAccounts(accounts, result, callingUid.intValue(), packageName);
        }
        Log.d(TAG, "Package not found " + packageName);
        return new LinkedHashMap();
    }

    public Map<String, Integer> getPackagesAndVisibilityForAccount(Account account) {
        Preconditions.checkNotNull(account, "account cannot be null");
        int callingUid = Binder.getCallingUid();
        int userId = UserHandle.getCallingUserId();
        if (isAccountManagedByCaller(account.type, callingUid, userId) || (isSystemUid(callingUid) ^ 1) == 0) {
            long identityToken = clearCallingIdentity();
            try {
                Map<String, Integer> packagesAndVisibilityForAccountLocked;
                UserAccounts accounts = getUserAccounts(userId);
                synchronized (accounts.dbLock) {
                    synchronized (accounts.cacheLock) {
                        packagesAndVisibilityForAccountLocked = getPackagesAndVisibilityForAccountLocked(account, accounts);
                    }
                }
                return packagesAndVisibilityForAccountLocked;
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            throw new SecurityException(String.format("uid %s cannot get secrets for account %s", new Object[]{Integer.valueOf(callingUid), account}));
        }
    }

    private Map<String, Integer> getPackagesAndVisibilityForAccountLocked(Account account, UserAccounts accounts) {
        Map<String, Integer> accountVisibility = (Map) accounts.visibilityCache.get(account);
        if (accountVisibility != null) {
            return accountVisibility;
        }
        Log.d(TAG, "Visibility was not initialized");
        accountVisibility = new HashMap();
        accounts.visibilityCache.put(account, accountVisibility);
        return accountVisibility;
    }

    public int getAccountVisibility(Account account, String packageName) {
        Preconditions.checkNotNull(account, "account cannot be null");
        Preconditions.checkNotNull(packageName, "packageName cannot be null");
        int callingUid = Binder.getCallingUid();
        int userId = UserHandle.getCallingUserId();
        if (isAccountManagedByCaller(account.type, callingUid, userId) || (isSystemUid(callingUid) ^ 1) == 0) {
            long identityToken = clearCallingIdentity();
            try {
                UserAccounts accounts = getUserAccounts(userId);
                int visibility;
                if ("android:accounts:key_legacy_visible".equals(packageName)) {
                    visibility = getAccountVisibilityFromCache(account, packageName, accounts);
                    if (visibility != 0) {
                        return visibility;
                    }
                    restoreCallingIdentity(identityToken);
                    return 2;
                } else if ("android:accounts:key_legacy_not_visible".equals(packageName)) {
                    visibility = getAccountVisibilityFromCache(account, packageName, accounts);
                    if (visibility != 0) {
                        restoreCallingIdentity(identityToken);
                        return visibility;
                    }
                    restoreCallingIdentity(identityToken);
                    return 4;
                } else {
                    int intValue = resolveAccountVisibility(account, packageName, accounts).intValue();
                    restoreCallingIdentity(identityToken);
                    return intValue;
                }
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            throw new SecurityException(String.format("uid %s cannot get secrets for accounts of type: %s", new Object[]{Integer.valueOf(callingUid), account.type}));
        }
    }

    private int getAccountVisibilityFromCache(Account account, String packageName, UserAccounts accounts) {
        int intValue;
        synchronized (accounts.cacheLock) {
            Integer visibility = (Integer) getPackagesAndVisibilityForAccountLocked(account, accounts).get(packageName);
            intValue = visibility != null ? visibility.intValue() : 0;
        }
        return intValue;
    }

    private Integer resolveAccountVisibility(Account account, String packageName, UserAccounts accounts) {
        Preconditions.checkNotNull(packageName, "packageName cannot be null");
        long identityToken;
        try {
            identityToken = clearCallingIdentity();
            int uid = this.mPackageManager.getPackageUidAsUser(packageName, accounts.userId);
            restoreCallingIdentity(identityToken);
            if (UserHandle.isSameApp(uid, 1000)) {
                return Integer.valueOf(1);
            }
            int signatureCheckResult = checkPackageSignature(account.type, uid, accounts.userId);
            if (signatureCheckResult == 2) {
                return Integer.valueOf(1);
            }
            int visibility = getAccountVisibilityFromCache(account, packageName, accounts);
            if (visibility != 0) {
                return Integer.valueOf(visibility);
            }
            boolean isPrivileged = isPermittedForPackage(packageName, uid, accounts.userId, "android.permission.GET_ACCOUNTS_PRIVILEGED");
            if (isProfileOwner(uid)) {
                return Integer.valueOf(1);
            }
            boolean preO = isPreOApplication(packageName);
            if (signatureCheckResult != 0 || ((preO && checkGetAccountsPermission(packageName, uid, accounts.userId)) || ((checkReadContactsPermission(packageName, uid, accounts.userId) && accountTypeManagesContacts(account.type, accounts.userId)) || isPrivileged))) {
                visibility = getAccountVisibilityFromCache(account, "android:accounts:key_legacy_visible", accounts);
                if (visibility == 0) {
                    visibility = 2;
                }
            } else {
                visibility = getAccountVisibilityFromCache(account, "android:accounts:key_legacy_not_visible", accounts);
                if (visibility == 0) {
                    visibility = 4;
                }
            }
            return Integer.valueOf(visibility);
        } catch (NameNotFoundException e) {
            Log.d(TAG, "Package not found " + e.getMessage());
            return Integer.valueOf(3);
        } catch (Throwable th) {
            restoreCallingIdentity(identityToken);
        }
    }

    private boolean isPreOApplication(String packageName) {
        boolean z = false;
        long identityToken;
        try {
            identityToken = clearCallingIdentity();
            ApplicationInfo applicationInfo = this.mPackageManager.getApplicationInfo(packageName, 0);
            restoreCallingIdentity(identityToken);
            if (applicationInfo == null) {
                return true;
            }
            if (applicationInfo.targetSdkVersion < 26) {
                z = true;
            }
            return z;
        } catch (NameNotFoundException e) {
            Log.d(TAG, "Package not found " + e.getMessage());
            return true;
        } catch (Throwable th) {
            restoreCallingIdentity(identityToken);
        }
    }

    public boolean setAccountVisibility(Account account, String packageName, int newVisibility) {
        Preconditions.checkNotNull(account, "account cannot be null");
        Preconditions.checkNotNull(packageName, "packageName cannot be null");
        int callingUid = Binder.getCallingUid();
        int userId = UserHandle.getCallingUserId();
        if (isAccountManagedByCaller(account.type, callingUid, userId) || (isSystemUid(callingUid) ^ 1) == 0) {
            long identityToken = clearCallingIdentity();
            try {
                boolean accountVisibility = setAccountVisibility(account, packageName, newVisibility, true, getUserAccounts(userId));
                return accountVisibility;
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            throw new SecurityException(String.format("uid %s cannot get secrets for accounts of type: %s", new Object[]{Integer.valueOf(callingUid), account.type}));
        }
    }

    private boolean isVisible(int visibility) {
        if (visibility == 1 || visibility == 2) {
            return true;
        }
        return false;
    }

    private boolean setAccountVisibility(Account account, String packageName, int newVisibility, boolean notify, UserAccounts accounts) {
        synchronized (accounts.dbLock) {
            synchronized (accounts.cacheLock) {
                Map<String, Integer> packagesToVisibility;
                List<String> accountRemovedReceivers;
                if (!notify) {
                    if (!isSpecialPackageKey(packageName)) {
                        if ((packageExistsForUser(packageName, accounts.userId) ^ 1) != 0) {
                            return false;
                        }
                    }
                    packagesToVisibility = Collections.emptyMap();
                    accountRemovedReceivers = Collections.emptyList();
                } else if (isSpecialPackageKey(packageName)) {
                    packagesToVisibility = getRequestingPackages(account, accounts);
                    accountRemovedReceivers = getAccountRemovedReceivers(account, accounts);
                } else {
                    if (packageExistsForUser(packageName, accounts.userId)) {
                        packagesToVisibility = new HashMap();
                        packagesToVisibility.put(packageName, resolveAccountVisibility(account, packageName, accounts));
                        accountRemovedReceivers = new ArrayList();
                        if (shouldNotifyPackageOnAccountRemoval(account, packageName, accounts)) {
                            accountRemovedReceivers.add(packageName);
                        }
                    } else {
                        return false;
                    }
                }
                if (updateAccountVisibilityLocked(account, packageName, newVisibility, accounts)) {
                    if (notify) {
                        for (Entry<String, Integer> packageToVisibility : packagesToVisibility.entrySet()) {
                            if (isVisible(((Integer) packageToVisibility.getValue()).intValue()) != isVisible(resolveAccountVisibility(account, packageName, accounts).intValue())) {
                                notifyPackage((String) packageToVisibility.getKey(), accounts);
                            }
                        }
                        for (String packageNameToNotify : accountRemovedReceivers) {
                            sendAccountRemovedBroadcast(account, packageNameToNotify, accounts.userId);
                        }
                        sendAccountsChangedBroadcast(accounts.userId);
                    }
                    return true;
                }
                return false;
            }
        }
    }

    private boolean updateAccountVisibilityLocked(Account account, String packageName, int newVisibility, UserAccounts accounts) {
        long accountId = accounts.accountsDb.findDeAccountId(account);
        if (accountId < 0) {
            return false;
        }
        ThreadPolicy oldPolicy = StrictMode.allowThreadDiskWrites();
        try {
            if (!accounts.accountsDb.setAccountVisibility(accountId, packageName, newVisibility)) {
                return false;
            }
            StrictMode.setThreadPolicy(oldPolicy);
            getPackagesAndVisibilityForAccountLocked(account, accounts).put(packageName, Integer.valueOf(newVisibility));
            return true;
        } finally {
            StrictMode.setThreadPolicy(oldPolicy);
        }
    }

    public void registerAccountListener(String[] accountTypes, String opPackageName) {
        this.mAppOpsManager.checkPackage(Binder.getCallingUid(), opPackageName);
        int userId = UserHandle.getCallingUserId();
        long identityToken = clearCallingIdentity();
        try {
            registerAccountListener(accountTypes, opPackageName, getUserAccounts(userId));
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    private void registerAccountListener(String[] accountTypes, String opPackageName, UserAccounts accounts) {
        synchronized (accounts.mReceiversForType) {
            if (accountTypes == null) {
                accountTypes = new String[]{null};
            }
            for (String type : accountTypes) {
                Map<String, Integer> receivers = (Map) accounts.mReceiversForType.get(type);
                if (receivers == null) {
                    receivers = new HashMap();
                    accounts.mReceiversForType.put(type, receivers);
                }
                Integer cnt = (Integer) receivers.get(opPackageName);
                receivers.put(opPackageName, Integer.valueOf(cnt != null ? cnt.intValue() + 1 : 1));
            }
        }
    }

    public void unregisterAccountListener(String[] accountTypes, String opPackageName) {
        this.mAppOpsManager.checkPackage(Binder.getCallingUid(), opPackageName);
        int userId = UserHandle.getCallingUserId();
        long identityToken = clearCallingIdentity();
        try {
            unregisterAccountListener(accountTypes, opPackageName, getUserAccounts(userId));
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    private void unregisterAccountListener(String[] accountTypes, String opPackageName, UserAccounts accounts) {
        synchronized (accounts.mReceiversForType) {
            if (accountTypes == null) {
                accountTypes = new String[]{null};
            }
            for (String type : accountTypes) {
                Map<String, Integer> receivers = (Map) accounts.mReceiversForType.get(type);
                if (receivers == null || receivers.get(opPackageName) == null) {
                    throw new IllegalArgumentException("attempt to unregister wrong receiver");
                }
                Integer cnt = (Integer) receivers.get(opPackageName);
                if (cnt.intValue() == 1) {
                    receivers.remove(opPackageName);
                } else {
                    receivers.put(opPackageName, Integer.valueOf(cnt.intValue() - 1));
                }
            }
        }
    }

    private void sendNotificationAccountUpdated(Account account, UserAccounts accounts) {
        for (Entry<String, Integer> packageToVisibility : getRequestingPackages(account, accounts).entrySet()) {
            if (!(((Integer) packageToVisibility.getValue()).intValue() == 3 || ((Integer) packageToVisibility.getValue()).intValue() == 4)) {
                notifyPackage((String) packageToVisibility.getKey(), accounts);
            }
        }
    }

    private void notifyPackage(String packageName, UserAccounts accounts) {
        Intent intent = new Intent("android.accounts.action.VISIBLE_ACCOUNTS_CHANGED");
        intent.setPackage(packageName);
        intent.setFlags(1073741824);
        this.mContext.sendBroadcastAsUser(intent, new UserHandle(accounts.userId));
    }

    private Map<String, Integer> getRequestingPackages(Account account, UserAccounts accounts) {
        Set<String> packages = new HashSet();
        synchronized (accounts.mReceiversForType) {
            for (String type : new String[]{account.type, null}) {
                Map<String, Integer> receivers = (Map) accounts.mReceiversForType.get(type);
                if (receivers != null) {
                    packages.addAll(receivers.keySet());
                }
            }
        }
        Map<String, Integer> result = new HashMap();
        for (String packageName : packages) {
            result.put(packageName, resolveAccountVisibility(account, packageName, accounts));
        }
        return result;
    }

    private List<String> getAccountRemovedReceivers(Account account, UserAccounts accounts) {
        Intent intent = new Intent("android.accounts.action.ACCOUNT_REMOVED");
        intent.setFlags(16777216);
        List<ResolveInfo> receivers = this.mPackageManager.queryBroadcastReceiversAsUser(intent, 0, accounts.userId);
        List<String> result = new ArrayList();
        if (receivers == null) {
            return result;
        }
        for (ResolveInfo resolveInfo : receivers) {
            String packageName = resolveInfo.activityInfo.applicationInfo.packageName;
            int visibility = resolveAccountVisibility(account, packageName, accounts).intValue();
            if (visibility == 1 || visibility == 2) {
                result.add(packageName);
            }
        }
        return result;
    }

    private boolean shouldNotifyPackageOnAccountRemoval(Account account, String packageName, UserAccounts accounts) {
        boolean z = true;
        int visibility = resolveAccountVisibility(account, packageName, accounts).intValue();
        if (visibility != 1 && visibility != 2) {
            return false;
        }
        Intent intent = new Intent("android.accounts.action.ACCOUNT_REMOVED");
        intent.setFlags(16777216);
        intent.setPackage(packageName);
        List<ResolveInfo> receivers = this.mPackageManager.queryBroadcastReceiversAsUser(intent, 0, accounts.userId);
        if (receivers == null || receivers.size() <= 0) {
            z = false;
        }
        return z;
    }

    private boolean packageExistsForUser(String packageName, int userId) {
        long identityToken;
        try {
            identityToken = clearCallingIdentity();
            this.mPackageManager.getPackageUidAsUser(packageName, userId);
            restoreCallingIdentity(identityToken);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        } catch (Throwable th) {
            restoreCallingIdentity(identityToken);
        }
    }

    private boolean isSpecialPackageKey(String packageName) {
        if ("android:accounts:key_legacy_visible".equals(packageName)) {
            return true;
        }
        return "android:accounts:key_legacy_not_visible".equals(packageName);
    }

    private void sendAccountsChangedBroadcast(int userId) {
        Log.i(TAG, "the accounts changed, sending broadcast of " + ACCOUNTS_CHANGED_INTENT.getAction());
        this.mContext.sendBroadcastAsUser(ACCOUNTS_CHANGED_INTENT, new UserHandle(userId));
    }

    private void sendAccountRemovedBroadcast(Account account, String packageName, int userId) {
        Intent intent = new Intent("android.accounts.action.ACCOUNT_REMOVED");
        intent.setFlags(16777216);
        intent.setPackage(packageName);
        intent.putExtra("authAccount", account.name);
        intent.putExtra("accountType", account.type);
        this.mContext.sendBroadcastAsUser(intent, new UserHandle(userId));
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
        if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, "validateAccountsInternal " + accounts.userId + " isCeDatabaseAttached=" + accounts.accountsDb.isCeDatabaseAttached() + " userLocked=" + this.mLocalUnlockedUsers.get(accounts.userId));
        }
        if (invalidateAuthenticatorCache) {
            this.mAuthenticatorCache.invalidateCache(accounts.userId);
        }
        HashMap<String, Integer> knownAuth = getAuthenticatorTypeAndUIDForUser(this.mAuthenticatorCache, accounts.userId);
        boolean userUnlocked = isLocalUnlockedUser(accounts.userId);
        synchronized (accounts.dbLock) {
            synchronized (accounts.cacheLock) {
                boolean accountDeleted = false;
                AccountsDb accountsDb = accounts.accountsDb;
                Map<String, Integer> metaAuthUid = accountsDb.findMetaAuthUid();
                HashSet<String> obsoleteAuthType = Sets.newHashSet();
                SparseBooleanArray knownUids = null;
                for (Entry<String, Integer> authToUidEntry : metaAuthUid.entrySet()) {
                    String type = (String) authToUidEntry.getKey();
                    int uid = ((Integer) authToUidEntry.getValue()).intValue();
                    Integer knownUid = (Integer) knownAuth.get(type);
                    if (knownUid == null || uid != knownUid.intValue()) {
                        if (knownUids == null) {
                            knownUids = getUidsOfInstalledOrUpdatedPackagesAsUser(accounts.userId);
                        }
                        if (!knownUids.get(uid)) {
                            obsoleteAuthType.add(type);
                            accountsDb.deleteMetaByAuthTypeAndUid(type, uid);
                        }
                    } else {
                        knownAuth.remove(type);
                    }
                }
                for (Entry<String, Integer> entry : knownAuth.entrySet()) {
                    accountsDb.insertOrReplaceMetaAuthTypeAndUid((String) entry.getKey(), ((Integer) entry.getValue()).intValue());
                }
                Map<Long, Account> accountsMap = accountsDb.findAllDeAccounts();
                try {
                    ArrayList<String> accountNames;
                    accounts.accountCache.clear();
                    HashMap<String, ArrayList<String>> accountNamesByType = new LinkedHashMap();
                    for (Entry<Long, Account> accountEntry : accountsMap.entrySet()) {
                        long accountId = ((Long) accountEntry.getKey()).longValue();
                        Account account = (Account) accountEntry.getValue();
                        if (obsoleteAuthType.contains(account.type)) {
                            Slog.w(TAG, "deleting account because type " + account.type + "'s registered authenticator no longer exist.");
                            Map<String, Integer> packagesToVisibility = getRequestingPackages(account, accounts);
                            List<String> accountRemovedReceivers = getAccountRemovedReceivers(account, accounts);
                            accountsDb.beginTransaction();
                            accountsDb.deleteDeAccount(accountId);
                            if (userUnlocked) {
                                accountsDb.deleteCeAccount(accountId);
                            }
                            accountsDb.setTransactionSuccessful();
                            accountsDb.endTransaction();
                            accountDeleted = true;
                            logRecord(AccountsDb.DEBUG_ACTION_AUTHENTICATOR_REMOVE, "accounts", accountId, accounts);
                            accounts.userDataCache.remove(account);
                            accounts.authTokenCache.remove(account);
                            accounts.accountTokenCaches.remove(account);
                            accounts.visibilityCache.remove(account);
                            for (Entry<String, Integer> packageToVisibility : packagesToVisibility.entrySet()) {
                                if (isVisible(((Integer) packageToVisibility.getValue()).intValue())) {
                                    notifyPackage((String) packageToVisibility.getKey(), accounts);
                                }
                            }
                            for (String packageName : accountRemovedReceivers) {
                                sendAccountRemovedBroadcast(account, packageName, accounts.userId);
                            }
                        } else {
                            accountNames = (ArrayList) accountNamesByType.get(account.type);
                            if (accountNames == null) {
                                accountNames = new ArrayList();
                                accountNamesByType.put(account.type, accountNames);
                            }
                            accountNames.add(account.name);
                        }
                    }
                    for (Entry<String, ArrayList<String>> cur : accountNamesByType.entrySet()) {
                        String accountType = (String) cur.getKey();
                        accountNames = (ArrayList) cur.getValue();
                        Object accountsForType = new Account[accountNames.size()];
                        for (int i = 0; i < accountsForType.length; i++) {
                            accountsForType[i] = new Account((String) accountNames.get(i), accountType, UUID.randomUUID().toString());
                        }
                        accounts.accountCache.put(accountType, accountsForType);
                    }
                    accounts.visibilityCache.putAll(accountsDb.findAllVisibilityValues());
                    if (accountDeleted) {
                        sendAccountsChangedBroadcast(accounts.userId);
                    }
                } catch (Throwable ex) {
                    Log.w(TAG, "validateAccountsInternal ret got err:", ex);
                    if (accountDeleted) {
                        sendAccountsChangedBroadcast(accounts.userId);
                    }
                } catch (Throwable th) {
                    if (accountDeleted) {
                        sendAccountsChangedBroadcast(accounts.userId);
                    }
                }
            }
        }
    }

    private SparseBooleanArray getUidsOfInstalledOrUpdatedPackagesAsUser(int userId) {
        List<PackageInfo> pkgsWithData = this.mPackageManager.getInstalledPackagesAsUser(8192, userId);
        SparseBooleanArray knownUids = new SparseBooleanArray(pkgsWithData.size());
        for (PackageInfo pkgInfo : pkgsWithData) {
            if (!(pkgInfo.applicationInfo == null || (pkgInfo.applicationInfo.flags & 8388608) == 0)) {
                knownUids.put(pkgInfo.applicationInfo.uid, true);
            }
        }
        return knownUids;
    }

    static HashMap<String, Integer> getAuthenticatorTypeAndUIDForUser(Context context, int userId) {
        return getAuthenticatorTypeAndUIDForUser(new AccountAuthenticatorCache(context), userId);
    }

    private static HashMap<String, Integer> getAuthenticatorTypeAndUIDForUser(IAccountAuthenticatorCache authCache, int userId) {
        HashMap<String, Integer> knownAuth = new LinkedHashMap();
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
                accounts = new UserAccounts(this.mContext, userId, new File(this.mInjector.getPreNDatabaseName(userId)), new File(this.mInjector.getDeDatabaseName(userId)));
                initializeDebugDbSizeAndCompileSqlStatementForLogging(accounts);
                this.mUsers.append(userId, accounts);
                purgeOldGrants(accounts);
                validateAccounts = true;
            }
            if (!accounts.accountsDb.isCeDatabaseAttached() && this.mLocalUnlockedUsers.get(userId)) {
                Log.i(TAG, "User " + userId + " is unlocked - opening CE database");
                synchronized (accounts.dbLock) {
                    synchronized (accounts.cacheLock) {
                        accounts.accountsDb.attachCeDatabase(new File(this.mInjector.getCeDatabaseName(userId)));
                    }
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
        List<Account> accountsToRemove = accounts.accountsDb.findCeAccountsNotInDe();
        if (!accountsToRemove.isEmpty()) {
            Slog.i(TAG, "Accounts " + accountsToRemove + " were previously deleted while user " + accounts.userId + " was locked. Removing accounts from CE tables");
            logRecord(accounts, AccountsDb.DEBUG_ACTION_SYNC_DE_CE_ACCOUNTS, "accounts");
            for (Account account : accountsToRemove) {
                removeAccountInternal(accounts, account, 1000);
            }
        }
    }

    private void purgeOldGrantsAll() {
        synchronized (this.mUsers) {
            for (int i = 0; i < this.mUsers.size(); i++) {
                purgeOldGrants((UserAccounts) this.mUsers.valueAt(i));
            }
        }
    }

    private void purgeOldGrants(UserAccounts accounts) {
        synchronized (accounts.dbLock) {
            synchronized (accounts.cacheLock) {
                for (Integer intValue : accounts.accountsDb.findAllUidGrants()) {
                    int uid = intValue.intValue();
                    if (!(this.mPackageManager.getPackagesForUid(uid) != null)) {
                        Log.d(TAG, "deleting grants for UID " + uid + " because its package is no longer installed");
                        accounts.accountsDb.deleteGrantsByUid(uid);
                    }
                }
            }
        }
    }

    private void removeVisibilityValuesForPackage(String packageName) {
        if (!isSpecialPackageKey(packageName)) {
            synchronized (this.mUsers) {
                int numberOfUsers = this.mUsers.size();
                for (int i = 0; i < numberOfUsers; i++) {
                    UserAccounts accounts = (UserAccounts) this.mUsers.valueAt(i);
                    try {
                        this.mPackageManager.getPackageUidAsUser(packageName, accounts.userId);
                    } catch (NameNotFoundException e) {
                        accounts.accountsDb.deleteAccountVisibilityForPackage(packageName);
                        synchronized (accounts.dbLock) {
                            synchronized (accounts.cacheLock) {
                                for (Account account : accounts.visibilityCache.keySet()) {
                                    getPackagesAndVisibilityForAccountLocked(account, accounts).remove(packageName);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void onStopUser(int userId) {
        UserAccounts accounts;
        Log.i(TAG, "onStopUser " + userId);
        synchronized (this.mUsers) {
            accounts = (UserAccounts) this.mUsers.get(userId);
            this.mUsers.remove(userId);
            this.mLocalUnlockedUsers.delete(userId);
        }
        if (accounts != null) {
            synchronized (accounts.dbLock) {
                synchronized (accounts.cacheLock) {
                    accounts.accountsDb.close();
                }
            }
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
        if (userId >= 1) {
            syncSharedAccounts(userId);
        }
    }

    private void syncSharedAccounts(int userId) {
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
            for (Account sa : sharedAccounts) {
                if (!ArrayUtils.contains(accounts, sa)) {
                    copyAccountToUser(null, sa, parentUserId, userId);
                }
            }
        }
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
            synchronized (accounts.dbLock) {
                synchronized (accounts.cacheLock) {
                    findAccountPasswordByNameAndType = accounts.accountsDb.findAccountPasswordByNameAndType(account.name, account.type);
                }
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
        Preconditions.checkNotNull(account, "account cannot be null");
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
        if (account == null) {
            return null;
        }
        synchronized (accounts.dbLock) {
            synchronized (accounts.cacheLock) {
                AtomicReference<String> previousNameRef = (AtomicReference) accounts.previousNameCache.get(account);
                if (previousNameRef == null) {
                    String previousName = accounts.accountsDb.findDeAccountPreviousName(account);
                    accounts.previousNameCache.put(account, new AtomicReference(previousName));
                    return previousName;
                }
                String str = (String) previousNameRef.get();
                return str;
            }
        }
    }

    public String getUserData(Account account, String key) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, String.format("getUserData( callerUid: %s, pid: %s", new Object[]{Integer.valueOf(callingUid), Integer.valueOf(Binder.getCallingPid())}));
        }
        Preconditions.checkNotNull(account, "account cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");
        int userId = UserHandle.getCallingUserId();
        if (!isAccountManagedByCaller(account.type, callingUid, userId)) {
            throw new SecurityException(String.format("uid %s cannot get user data for accounts of type: %s", new Object[]{Integer.valueOf(callingUid), account.type}));
        } else if (isLocalUnlockedUser(userId)) {
            long identityToken = clearCallingIdentity();
            try {
                UserAccounts accounts = getUserAccounts(userId);
                if (!accountExistsCache(accounts, account)) {
                    return null;
                }
                String readUserDataInternal = readUserDataInternal(accounts, account, key);
                restoreCallingIdentity(identityToken);
                return readUserDataInternal;
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            Log.w(TAG, "User " + userId + " data is locked. callingUid " + callingUid);
            return null;
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
        this.mAuthenticatorCache.updateServices(userId);
        Collection<ServiceInfo<AuthenticatorDescription>> authenticatorCollection = this.mAuthenticatorCache.getAllServices(userId);
        AuthenticatorDescription[] types = new AuthenticatorDescription[authenticatorCollection.size()];
        int i = 0;
        for (ServiceInfo<AuthenticatorDescription> authenticator : authenticatorCollection) {
            types[i] = (AuthenticatorDescription) authenticator.type;
            i++;
        }
        return types;
    }

    private boolean isCrossUser(int callingUid, int userId) {
        if (userId == UserHandle.getCallingUserId() || callingUid == 1000 || this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) {
            return false;
        }
        return true;
    }

    public boolean addAccountExplicitly(Account account, String password, Bundle extras) {
        return addAccountExplicitlyWithVisibility(account, password, extras, null);
    }

    public void copyAccountToUser(IAccountManagerResponse response, Account account, int userFrom, int userTo) {
        if (isCrossUser(Binder.getCallingUid(), -1)) {
            throw new SecurityException("Calling copyAccountToUser requires android.permission.INTERACT_ACROSS_USERS_FULL");
        }
        UserAccounts fromAccounts = getUserAccounts(userFrom);
        final UserAccounts toAccounts = getUserAccounts(userTo);
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
            final Account account2 = account;
            final IAccountManagerResponse iAccountManagerResponse = response;
            final int i = userFrom;
            new Session(this, fromAccounts, response, account.type, false, false, account.name, false) {
                protected String toDebugString(long now) {
                    return super.toDebugString(now) + ", getAccountCredentialsForClone" + ", " + account2.type;
                }

                public void run() throws RemoteException {
                    this.mAuthenticator.getAccountCredentialsForCloning(this, account2);
                }

                public void onResult(Bundle result) {
                    Bundle.setDefusable(result, true);
                    if (result == null || !result.getBoolean("booleanResult", false)) {
                        super.onResult(result);
                        return;
                    }
                    this.completeCloningAccount(iAccountManagerResponse, result, account2, toAccounts, i);
                }
            }.bind();
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    public boolean accountAuthenticated(Account account) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, String.format("accountAuthenticated( callerUid: %s)", new Object[]{Integer.valueOf(callingUid)}));
        }
        Preconditions.checkNotNull(account, "account cannot be null");
        int userId = UserHandle.getCallingUserId();
        if (!isAccountManagedByCaller(account.type, callingUid, userId)) {
            throw new SecurityException(String.format("uid %s cannot notify authentication for accounts of type: %s", new Object[]{Integer.valueOf(callingUid), account.type}));
        } else if (!canUserModifyAccounts(userId, callingUid) || (canUserModifyAccountsForType(userId, account.type, callingUid) ^ 1) != 0) {
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
        boolean updateAccountLastAuthenticatedTime;
        UserAccounts accounts = getUserAccountsForCaller();
        synchronized (accounts.dbLock) {
            synchronized (accounts.cacheLock) {
                updateAccountLastAuthenticatedTime = accounts.accountsDb.updateAccountLastAuthenticatedTime(account);
            }
        }
        return updateAccountLastAuthenticatedTime;
    }

    private void completeCloningAccount(IAccountManagerResponse response, Bundle accountCredentials, Account account, UserAccounts targetUser, int parentUserId) {
        Bundle.setDefusable(accountCredentials, true);
        long id = clearCallingIdentity();
        try {
            final Account account2 = account;
            final int i = parentUserId;
            final Bundle bundle = accountCredentials;
            new Session(this, targetUser, response, account.type, false, false, account.name, false) {
                protected String toDebugString(long now) {
                    return super.toDebugString(now) + ", getAccountCredentialsForClone" + ", " + account2.type;
                }

                public void run() throws RemoteException {
                    for (Account acc : this.getAccounts(i, this.mContext.getOpPackageName())) {
                        if (acc.equals(account2)) {
                            this.mAuthenticator.addAccountFromCredentials(this, account2, bundle);
                            return;
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
            }.bind();
        } finally {
            restoreCallingIdentity(id);
        }
    }

    /* JADX WARNING: Missing block: B:90:0x022b, code:
            if (getUserManager().getUserInfo(com.android.server.accounts.AccountManagerService.UserAccounts.-get9(r22)).canHaveProfile() == false) goto L_0x0238;
     */
    /* JADX WARNING: Missing block: B:91:0x022d, code:
            addAccountToLinkedRestrictedUsers(r23, com.android.server.accounts.AccountManagerService.UserAccounts.-get9(r22));
     */
    /* JADX WARNING: Missing block: B:92:0x0238, code:
            sendNotificationAccountUpdated(r23, r22);
            sendAccountsChangedBroadcast(com.android.server.accounts.AccountManagerService.UserAccounts.-get9(r22));
     */
    /* JADX WARNING: Missing block: B:93:0x024b, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean addAccountInternal(UserAccounts accounts, Account account, String password, Bundle extras, int callingUid, Map<String, Integer> packageToVisibility) {
        Bundle.setDefusable(extras, true);
        if (account == null) {
            return false;
        }
        if (isLocalUnlockedUser(accounts.userId)) {
            synchronized (accounts.dbLock) {
                synchronized (accounts.cacheLock) {
                    accounts.accountsDb.beginTransaction();
                    try {
                        if (accounts.accountsDb.findCeAccountId(account) >= 0) {
                            Log.w(TAG, "insertAccountIntoDatabase: " + account + ", skipping since the account already exists");
                            accounts.accountsDb.endTransaction();
                            return false;
                        }
                        long accountId = accounts.accountsDb.insertCeAccount(account, password);
                        if (accountId < 0) {
                            Log.w(TAG, "insertAccountIntoDatabase: " + account + ", skipping the DB insert failed");
                            accounts.accountsDb.endTransaction();
                            return false;
                        }
                        Log.e(TAG, "insert CE accountId = " + accountId);
                        if (accounts.accountsDb.insertDeAccount(account, accountId) < 0) {
                            Log.w(TAG, "insertAccountIntoDatabase: " + account + ", skipping the DB insert failed");
                            accounts.accountsDb.endTransaction();
                            return false;
                        }
                        Log.e(TAG, "insert DE accountId = " + accountId);
                        if (extras != null) {
                            for (String key : extras.keySet()) {
                                if (accounts.accountsDb.insertExtra(accountId, key, extras.getString(key)) < 0) {
                                    Log.w(TAG, "insertAccountIntoDatabase: " + account + ", skipping since insertExtra failed for key " + key);
                                    accounts.accountsDb.endTransaction();
                                    return false;
                                }
                            }
                        }
                        if (packageToVisibility != null) {
                            for (Entry<String, Integer> entry : packageToVisibility.entrySet()) {
                                setAccountVisibility(account, (String) entry.getKey(), ((Integer) entry.getValue()).intValue(), false, accounts);
                            }
                        }
                        accounts.accountsDb.setTransactionSuccessful();
                        logRecord(AccountsDb.DEBUG_ACTION_ACCOUNT_ADD, "accounts", accountId, accounts, callingUid);
                        insertAccountIntoCacheLocked(accounts, account);
                        accounts.accountsDb.endTransaction();
                    } catch (Throwable th) {
                        accounts.accountsDb.endTransaction();
                    }
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
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(4, parentUserId, user.id, account));
                }
            }
        }
    }

    public void hasFeatures(IAccountManagerResponse response, Account account, String[] features, String opPackageName) {
        boolean z;
        boolean z2 = true;
        int callingUid = Binder.getCallingUid();
        this.mAppOpsManager.checkPackage(callingUid, opPackageName);
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "hasFeatures, response " + response + ", features " + Arrays.toString(features) + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (account != null) {
            z = true;
        } else {
            z = false;
        }
        Preconditions.checkArgument(z, "account cannot be null");
        if (response != null) {
            z = true;
        } else {
            z = false;
        }
        Preconditions.checkArgument(z, "response cannot be null");
        if (features == null) {
            z2 = false;
        }
        Preconditions.checkArgument(z2, "features cannot be null");
        int userId = UserHandle.getCallingUserId();
        checkReadAccountsPermitted(callingUid, account.type, userId, opPackageName);
        long identityToken = clearCallingIdentity();
        try {
            new TestFeaturesSession(getUserAccounts(userId), response, account, features).bind();
        } finally {
            restoreCallingIdentity(identityToken);
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
                result.putString("accountAccessId", resultingAccount.getAccessId());
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
        cancelNotification(getSigninRequiredNotificationId(accounts, accountToRename), new UserHandle(accounts.userId));
        synchronized (accounts.credentialsPermissionNotificationIds) {
            for (Pair<Pair<Account, String>, Integer> pair : accounts.credentialsPermissionNotificationIds.keySet()) {
                if (accountToRename.equals(((Pair) pair.first).first)) {
                    cancelNotification((NotificationId) accounts.credentialsPermissionNotificationIds.get(pair), new UserHandle(accounts.userId));
                }
            }
        }
        synchronized (accounts.dbLock) {
            synchronized (accounts.cacheLock) {
                List<String> accountRemovedReceivers = getAccountRemovedReceivers(accountToRename, accounts);
                accounts.accountsDb.beginTransaction();
                Account renamedAccount = new Account(newName, accountToRename.type);
                if (accounts.accountsDb.findCeAccountId(renamedAccount) >= 0) {
                    Log.e(TAG, "renameAccount failed - account with new name already exists");
                    return null;
                }
                try {
                    long accountId = accounts.accountsDb.findDeAccountId(accountToRename);
                    if (accountId >= 0) {
                        accounts.accountsDb.renameCeAccount(accountId, newName);
                        if (accounts.accountsDb.renameDeAccount(accountId, newName, accountToRename.name)) {
                            accounts.accountsDb.setTransactionSuccessful();
                            renamedAccount = insertAccountIntoCacheLocked(accounts, renamedAccount);
                            Map<String, String> tmpData = (Map) accounts.userDataCache.get(accountToRename);
                            Map<String, String> tmpTokens = (Map) accounts.authTokenCache.get(accountToRename);
                            Map<String, Integer> tmpVisibility = (Map) accounts.visibilityCache.get(accountToRename);
                            removeAccountFromCacheLocked(accounts, accountToRename);
                            accounts.userDataCache.put(renamedAccount, tmpData);
                            accounts.authTokenCache.put(renamedAccount, tmpTokens);
                            accounts.visibilityCache.put(renamedAccount, tmpVisibility);
                            accounts.previousNameCache.put(renamedAccount, new AtomicReference(accountToRename.name));
                            Account resultAccount = renamedAccount;
                            int parentUserId = accounts.userId;
                            if (canHaveProfile(parentUserId)) {
                                for (UserInfo user : getUserManager().getUsers(true)) {
                                    if (user.isRestricted() && user.restrictedProfileParentId == parentUserId) {
                                        renameSharedAccountAsUser(accountToRename, newName, user.id);
                                    }
                                }
                            }
                            sendNotificationAccountUpdated(renamedAccount, accounts);
                            sendAccountsChangedBroadcast(accounts.userId);
                            for (String packageName : accountRemovedReceivers) {
                                sendAccountRemovedBroadcast(accountToRename, packageName, accounts.userId);
                            }
                            return renamedAccount;
                        }
                        Log.e(TAG, "renameAccount failed");
                        accounts.accountsDb.endTransaction();
                        return null;
                    }
                    Log.e(TAG, "renameAccount failed - old account does not exist");
                    accounts.accountsDb.endTransaction();
                    return null;
                } finally {
                    accounts.accountsDb.endTransaction();
                }
            }
        }
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
        Preconditions.checkArgument(account != null, "account cannot be null");
        Preconditions.checkArgument(response != null, "response cannot be null");
        if (isCrossUser(callingUid, userId)) {
            throw new SecurityException(String.format("User %s tying remove account for %s", new Object[]{Integer.valueOf(UserHandle.getCallingUserId()), Integer.valueOf(userId)}));
        }
        UserHandle user = UserHandle.of(userId);
        if (!isAccountManagedByCaller(account.type, callingUid, user.getIdentifier()) && (isSystemUid(callingUid) ^ 1) != 0) {
            throw new SecurityException(String.format("uid %s cannot remove accounts of type: %s", new Object[]{Integer.valueOf(callingUid), account.type}));
        } else if (canUserModifyAccounts(userId, callingUid)) {
            if (canUserModifyAccountsForType(userId, account.type, callingUid)) {
                long identityToken = clearCallingIdentity();
                UserAccounts accounts = getUserAccounts(userId);
                cancelNotification(getSigninRequiredNotificationId(accounts, account), user);
                synchronized (accounts.credentialsPermissionNotificationIds) {
                    for (Pair<Pair<Account, String>, Integer> pair : accounts.credentialsPermissionNotificationIds.keySet()) {
                        if (account.equals(((Pair) pair.first).first)) {
                            cancelNotification((NotificationId) accounts.credentialsPermissionNotificationIds.get(pair), user);
                        }
                    }
                }
                logRecord(AccountsDb.DEBUG_ACTION_CALLED_ACCOUNT_REMOVE, "accounts", accounts.accountsDb.findDeAccountId(account), accounts, callingUid);
                try {
                    new RemoveAccountSession(accounts, response, account, expectActivityLaunch).bind();
                } finally {
                    restoreCallingIdentity(identityToken);
                }
            } else {
                try {
                    response.onError(101, "User cannot modify accounts of this type (policy).");
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
            logRecord(AccountsDb.DEBUG_ACTION_CALLED_ACCOUNT_REMOVE, "accounts", accounts.accountsDb.findDeAccountId(account), accounts, callingUid);
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
        boolean isChanged = false;
        boolean userUnlocked = isLocalUnlockedUser(accounts.userId);
        if (!userUnlocked) {
            Slog.i(TAG, "Removing account " + account + " while user " + accounts.userId + " is still locked. CE data will be removed later");
        }
        synchronized (accounts.dbLock) {
            synchronized (accounts.cacheLock) {
                Map<String, Integer> packagesToVisibility = getRequestingPackages(account, accounts);
                List<String> accountRemovedReceivers = getAccountRemovedReceivers(account, accounts);
                accounts.accountsDb.beginTransaction();
                long accountId = -1;
                try {
                    accountId = accounts.accountsDb.findDeAccountId(account);
                    if (accountId >= 0) {
                        isChanged = accounts.accountsDb.deleteDeAccount(accountId);
                    }
                    if (userUnlocked) {
                        long ceAccountId = accounts.accountsDb.findCeAccountId(account);
                        if (ceAccountId >= 0) {
                            accounts.accountsDb.deleteCeAccount(ceAccountId);
                        }
                    }
                    accounts.accountsDb.setTransactionSuccessful();
                    if (isChanged) {
                        String action;
                        removeAccountFromCacheLocked(accounts, account);
                        for (Entry<String, Integer> packageToVisibility : packagesToVisibility.entrySet()) {
                            if (((Integer) packageToVisibility.getValue()).intValue() == 1 || ((Integer) packageToVisibility.getValue()).intValue() == 2) {
                                notifyPackage((String) packageToVisibility.getKey(), accounts);
                            }
                        }
                        sendAccountsChangedBroadcast(accounts.userId);
                        for (String packageName : accountRemovedReceivers) {
                            sendAccountRemovedBroadcast(account, packageName, accounts.userId);
                        }
                        if (userUnlocked) {
                            action = AccountsDb.DEBUG_ACTION_ACCOUNT_REMOVE;
                        } else {
                            action = AccountsDb.DEBUG_ACTION_ACCOUNT_REMOVE_DE;
                        }
                        logRecord(action, "accounts", accountId, accounts);
                    }
                } finally {
                    accounts.accountsDb.endTransaction();
                }
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
            if (isChanged) {
                synchronized (accounts.credentialsPermissionNotificationIds) {
                    for (Pair<Pair<Account, String>, Integer> key : accounts.credentialsPermissionNotificationIds.keySet()) {
                        if (account.equals(((Pair) key.first).first) && "com.android.AccountManager.ACCOUNT_ACCESS_TOKEN_TYPE".equals(((Pair) key.first).second)) {
                            this.mHandler.post(new com.android.server.accounts.-$Lambda$JwXVQhqSYlVkCeKB5Nx7U6RsZlU.AnonymousClass2(((Integer) key.second).intValue(), this, account));
                        }
                    }
                }
            }
            return isChanged;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(id);
        }
    }

    /* synthetic */ void lambda$-com_android_server_accounts_AccountManagerService_106115(Account account, int uid) {
        cancelAccountAccessRequestNotificationIfNeeded(account, uid, false);
    }

    /* JADX WARNING: Missing block: B:19:0x008c, code:
            restoreCallingIdentity(r4);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void invalidateAuthToken(String accountType, String authToken) {
        int callerUid = Binder.getCallingUid();
        Preconditions.checkNotNull(accountType, "accountType cannot be null");
        Preconditions.checkNotNull(authToken, "authToken cannot be null");
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "invalidateAuthToken , caller's uid " + callerUid + ", pid " + Binder.getCallingPid());
        }
        int userId = UserHandle.getCallingUserId();
        long identityToken = clearCallingIdentity();
        try {
            UserAccounts accounts = getUserAccounts(userId);
            synchronized (accounts.dbLock) {
                accounts.accountsDb.beginTransaction();
                try {
                    List<Pair<Account, String>> deletedTokens = invalidateAuthTokenLocked(accounts, accountType, authToken);
                    accounts.accountsDb.setTransactionSuccessful();
                    synchronized (accounts.cacheLock) {
                        for (Pair<Account, String> tokenInfo : deletedTokens) {
                            writeAuthTokenIntoCacheLocked(accounts, tokenInfo.first, tokenInfo.second, null);
                        }
                        accounts.accountTokenCaches.remove(accountType, authToken);
                    }
                } finally {
                    accounts.accountsDb.endTransaction();
                }
            }
        } finally {
        }
    }

    private List<Pair<Account, String>> invalidateAuthTokenLocked(UserAccounts accounts, String accountType, String authToken) {
        List<Pair<Account, String>> results = new ArrayList();
        Cursor cursor = accounts.accountsDb.findAuthtokenForAllAccounts(accountType, authToken);
        while (cursor.moveToNext()) {
            try {
                String authTokenId = cursor.getString(0);
                String accountName = cursor.getString(1);
                String authTokenType = cursor.getString(2);
                accounts.accountsDb.deleteAuthToken(authTokenId);
                results.add(Pair.create(new Account(accountName, accountType), authTokenType));
            } finally {
                cursor.close();
            }
        }
        return results;
    }

    private void saveCachedToken(UserAccounts accounts, Account account, String callerPkg, byte[] callerSigDigest, String tokenType, String token, long expiryMillis) {
        if (account != null && tokenType != null && callerPkg != null && callerSigDigest != null) {
            cancelNotification(getSigninRequiredNotificationId(accounts, account), UserHandle.of(accounts.userId));
            synchronized (accounts.cacheLock) {
                accounts.accountTokenCaches.put(account, token, tokenType, callerPkg, callerSigDigest, expiryMillis);
            }
        }
    }

    /* JADX WARNING: Missing block: B:23:0x0039, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:61:0x0078, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean saveAuthTokenToDatabase(UserAccounts accounts, Account account, String type, String authToken) {
        if (account == null || type == null) {
            return false;
        }
        cancelNotification(getSigninRequiredNotificationId(accounts, account), UserHandle.of(accounts.userId));
        synchronized (accounts.dbLock) {
            accounts.accountsDb.beginTransaction();
            try {
                long accountId = accounts.accountsDb.findDeAccountId(account);
                if (accountId < 0) {
                    accounts.accountsDb.endTransaction();
                    if (null != null) {
                        synchronized (accounts.cacheLock) {
                            writeAuthTokenIntoCacheLocked(accounts, account, type, authToken);
                        }
                    }
                } else {
                    accounts.accountsDb.deleteAuthtokensByAccountIdAndType(accountId, type);
                    if (accounts.accountsDb.insertAuthToken(accountId, type, authToken) >= 0) {
                        accounts.accountsDb.setTransactionSuccessful();
                        accounts.accountsDb.endTransaction();
                        if (true) {
                            synchronized (accounts.cacheLock) {
                                writeAuthTokenIntoCacheLocked(accounts, account, type, authToken);
                            }
                        }
                        return true;
                    }
                    accounts.accountsDb.endTransaction();
                    if (null != null) {
                        synchronized (accounts.cacheLock) {
                            writeAuthTokenIntoCacheLocked(accounts, account, type, authToken);
                        }
                    }
                }
            } catch (Throwable th) {
                accounts.accountsDb.endTransaction();
                if (null != null) {
                    synchronized (accounts.cacheLock) {
                        writeAuthTokenIntoCacheLocked(accounts, account, type, authToken);
                    }
                }
            }
        }
    }

    public String peekAuthToken(Account account, String authTokenType) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "peekAuthToken , authTokenType " + authTokenType + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        Preconditions.checkNotNull(account, "account cannot be null");
        Preconditions.checkNotNull(authTokenType, "authTokenType cannot be null");
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

    public void setAuthToken(Account account, String authTokenType, String authToken) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "setAuthToken , authTokenType " + authTokenType + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        Preconditions.checkNotNull(account, "account cannot be null");
        Preconditions.checkNotNull(authTokenType, "authTokenType cannot be null");
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

    public void setPassword(Account account, String password) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "setAuthToken , caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        Preconditions.checkNotNull(account, "account cannot be null");
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
            boolean isChanged = false;
            synchronized (accounts.dbLock) {
                synchronized (accounts.cacheLock) {
                    accounts.accountsDb.beginTransaction();
                    try {
                        long accountId = accounts.accountsDb.findDeAccountId(account);
                        if (accountId >= 0) {
                            String action;
                            accounts.accountsDb.updateCeAccountPassword(accountId, password);
                            accounts.accountsDb.deleteAuthTokensByAccountId(accountId);
                            accounts.authTokenCache.remove(account);
                            accounts.accountTokenCaches.remove(account);
                            accounts.accountsDb.setTransactionSuccessful();
                            isChanged = true;
                            if (password == null || password.length() == 0) {
                                action = AccountsDb.DEBUG_ACTION_CLEAR_PASSWORD;
                            } else {
                                action = AccountsDb.DEBUG_ACTION_SET_PASSWORD;
                            }
                            logRecord(action, "accounts", accountId, accounts, callingUid);
                        }
                        accounts.accountsDb.endTransaction();
                        if (isChanged) {
                            sendNotificationAccountUpdated(account, accounts);
                            sendAccountsChangedBroadcast(accounts.userId);
                        }
                    } catch (Throwable th) {
                        accounts.accountsDb.endTransaction();
                        if (isChanged) {
                            sendNotificationAccountUpdated(account, accounts);
                            sendAccountsChangedBroadcast(accounts.userId);
                        }
                    }
                }
            }
        }
    }

    public void clearPassword(Account account) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "clearPassword , caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        Preconditions.checkNotNull(account, "account cannot be null");
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
                    if (accountExistsCache(accounts, account)) {
                        setUserdataInternal(accounts, account, key, value);
                        restoreCallingIdentity(identityToken);
                    }
                } finally {
                    restoreCallingIdentity(identityToken);
                }
            } else {
                throw new SecurityException(String.format("uid %s cannot set user data for accounts of type: %s", new Object[]{Integer.valueOf(callingUid), account.type}));
            }
        }
    }

    /* JADX WARNING: Missing block: B:14:0x002f, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean accountExistsCache(UserAccounts accounts, Account account) {
        synchronized (accounts.cacheLock) {
            if (accounts.accountCache.containsKey(account.type)) {
                for (Account acc : (Account[]) accounts.accountCache.get(account.type)) {
                    if (acc.name.equals(account.name)) {
                        return true;
                    }
                }
            }
        }
    }

    private void setUserdataInternal(UserAccounts accounts, Account account, String key, String value) {
        synchronized (accounts.dbLock) {
            accounts.accountsDb.beginTransaction();
            try {
                long accountId = accounts.accountsDb.findDeAccountId(account);
                if (accountId < 0) {
                    accounts.accountsDb.endTransaction();
                    return;
                }
                long extrasId = accounts.accountsDb.findExtrasIdByAccountId(accountId, key);
                if (extrasId < 0) {
                    if (accounts.accountsDb.insertExtra(accountId, key, value) < 0) {
                        accounts.accountsDb.endTransaction();
                        return;
                    }
                } else if (!accounts.accountsDb.updateExtra(extrasId, value)) {
                    accounts.accountsDb.endTransaction();
                    return;
                }
                accounts.accountsDb.setTransactionSuccessful();
                accounts.accountsDb.endTransaction();
                synchronized (accounts.cacheLock) {
                    writeUserDataIntoCacheLocked(accounts, account, key, value);
                }
            } catch (Throwable th) {
                accounts.accountsDb.endTransaction();
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
        Preconditions.checkArgument(accountType != null, "accountType cannot be null");
        Preconditions.checkArgument(authTokenType != null, "authTokenType cannot be null");
        int callingUid = getCallingUid();
        clearCallingIdentity();
        if (UserHandle.getAppId(callingUid) != 1000) {
            throw new SecurityException("can only call from system");
        }
        int userId = UserHandle.getUserId(callingUid);
        long identityToken = clearCallingIdentity();
        try {
            final String str = accountType;
            final String str2 = authTokenType;
            new Session(this, getUserAccounts(userId), response, accountType, false, false, null, false) {
                protected String toDebugString(long now) {
                    return super.toDebugString(now) + ", getAuthTokenLabel" + ", " + str + ", authTokenType " + str2;
                }

                public void run() throws RemoteException {
                    this.mAuthenticator.getAuthTokenLabel(this, str2);
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
            }.bind();
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    public void getAuthToken(IAccountManagerResponse response, Account account, String authTokenType, boolean notifyOnAuthFailure, boolean expectActivityLaunch, Bundle loginOptions) {
        Bundle.setDefusable(loginOptions, true);
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "getAuthToken , response " + response + ", authTokenType " + authTokenType + ", notifyOnAuthFailure " + notifyOnAuthFailure + ", expectActivityLaunch " + expectActivityLaunch + ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
        }
        Preconditions.checkArgument(response != null, "response cannot be null");
        if (account == null) {
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
                final boolean customTokens = authenticatorInfo != null ? ((AuthenticatorDescription) authenticatorInfo.type).customTokens : false;
                final int callerUid = Binder.getCallingUid();
                final boolean permissionGranted = !customTokens ? permissionIsGranted(account, authTokenType, callerUid, userId) : true;
                String callerPkg = loginOptions.getString("androidPackageName");
                ident = Binder.clearCallingIdentity();
                try {
                    List<String> callerOwnedPackageNames = Arrays.asList(this.mPackageManager.getPackagesForUid(callerUid));
                    if (callerPkg == null || (callerOwnedPackageNames.contains(callerPkg) ^ 1) != 0) {
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
                        if (!customTokens && permissionGranted) {
                            String authToken = readAuthTokenInternal(accounts, account, authTokenType);
                            if (authToken != null) {
                                result = new Bundle();
                                result.putString("authtoken", authToken);
                                result.putString("authAccount", account.name);
                                result.putString("accountType", account.type);
                                onResult(response, result);
                                return;
                            }
                        }
                        if (customTokens) {
                            String token = readCachedTokenInternal(accounts, account, authTokenType, callerPkg, callerPkgSigDigest);
                            if (token != null) {
                                if (Log.isLoggable(TAG, 2)) {
                                    Log.v(TAG, "getAuthToken: cache hit ofr custom token authenticator.");
                                }
                                result = new Bundle();
                                result.putString("authtoken", token);
                                result.putString("authAccount", account.name);
                                result.putString("accountType", account.type);
                                onResult(response, result);
                                restoreCallingIdentity(identityToken);
                                return;
                            }
                        }
                        final Bundle bundle = loginOptions;
                        final Account account2 = account;
                        final String str = authTokenType;
                        final boolean z = notifyOnAuthFailure;
                        final String str2 = callerPkg;
                        final byte[] bArr = callerPkgSigDigest;
                        final UserAccounts userAccounts = accounts;
                        new Session(this, accounts, response, account.type, expectActivityLaunch, false, account.name, false) {
                            protected String toDebugString(long now) {
                                if (bundle != null) {
                                    bundle.keySet();
                                }
                                return super.toDebugString(now) + ", getAuthToken" + ", " + account2 + ", authTokenType " + str + ", loginOptions " + bundle + ", notifyOnAuthFailure " + z;
                            }

                            public void run() throws RemoteException {
                                if (permissionGranted) {
                                    this.mAuthenticator.getAuthToken(this, account2, str, bundle);
                                } else {
                                    this.mAuthenticator.getAuthTokenLabel(this, str);
                                }
                            }

                            public void onResult(Bundle result) {
                                Bundle.setDefusable(result, true);
                                if (result != null) {
                                    Intent intent;
                                    if (result.containsKey("authTokenLabelKey")) {
                                        intent = this.newGrantCredentialsPermissionIntent(account2, null, callerUid, new AccountAuthenticatorResponse(this), str, true);
                                        Bundle bundle = new Bundle();
                                        bundle.putParcelable(HwBroadcastRadarUtil.KEY_BROADCAST_INTENT, intent);
                                        onResult(bundle);
                                        return;
                                    }
                                    String authToken = result.getString("authtoken");
                                    if (authToken != null) {
                                        String name = result.getString("authAccount");
                                        String type = result.getString("accountType");
                                        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(name)) {
                                            onError(5, "the type and name should not be empty");
                                            return;
                                        }
                                        Account resultAccount = new Account(name, type);
                                        if (!customTokens) {
                                            this.saveAuthTokenToDatabase(this.mAccounts, resultAccount, str, authToken);
                                        }
                                        long expiryMillis = result.getLong("android.accounts.expiry", 0);
                                        if (customTokens && expiryMillis > System.currentTimeMillis()) {
                                            this.saveCachedToken(this.mAccounts, account2, str2, bArr, str, authToken, expiryMillis);
                                        }
                                    }
                                    intent = (Intent) result.getParcelable(HwBroadcastRadarUtil.KEY_BROADCAST_INTENT);
                                    if (!(intent == null || !z || (customTokens ^ 1) == 0)) {
                                        checkKeyIntent(Binder.getCallingUid(), intent);
                                        this.doNotification(this.mAccounts, account2, result.getString("authFailedMessage"), intent, "android", userAccounts.userId);
                                    }
                                }
                                super.onResult(result);
                            }
                        }.bind();
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
            for (Signature sig : this.mPackageManager.getPackageInfo(callerPkg, 64).signatures) {
                digester.update(sig.toByteArray());
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

    private void createNoCredentialsPermissionNotification(Account account, Intent intent, String packageName, int userId) {
        int uid = intent.getIntExtra("uid", -1);
        String authTokenType = intent.getStringExtra("authTokenType");
        String titleAndSubtitle = this.mContext.getString(17040672, new Object[]{account.name});
        int index = titleAndSubtitle.indexOf(10);
        String title = titleAndSubtitle;
        String subtitle = "";
        if (index > 0) {
            title = titleAndSubtitle.substring(0, index);
            subtitle = titleAndSubtitle.substring(index + 1);
        }
        UserHandle user = UserHandle.of(userId);
        Context contextForUser = getContextForUser(user);
        Notification n = new Builder(contextForUser, SystemNotificationChannels.ACCOUNT).setSmallIcon(17301642).setWhen(0).setColor(contextForUser.getColor(17170769)).setContentTitle(title).setContentText(subtitle).setContentIntent(PendingIntent.getActivityAsUser(this.mContext, 0, intent, 268435456, null, user)).build();
        installNotification(getCredentialPermissionNotificationId(account, authTokenType, uid), n, packageName, user.getIdentifier());
    }

    private Intent newGrantCredentialsPermissionIntent(Account account, String packageName, int uid, AccountAuthenticatorResponse response, String authTokenType, boolean startInNewTask) {
        Intent intent = new Intent(this.mContext, GrantCredentialsPermissionActivity.class);
        if (startInNewTask) {
            intent.setFlags(268435456);
        }
        StringBuilder append = new StringBuilder().append(getCredentialPermissionNotificationId(account, authTokenType, uid).mTag);
        if (packageName == null) {
            packageName = "";
        }
        intent.addCategory(append.append(packageName).toString());
        intent.putExtra("account", account);
        intent.putExtra("authTokenType", authTokenType);
        intent.putExtra("response", response);
        intent.putExtra("uid", uid);
        return intent;
    }

    private NotificationId getCredentialPermissionNotificationId(Account account, String authTokenType, int uid) {
        NotificationId nId;
        UserAccounts accounts = getUserAccounts(UserHandle.getUserId(uid));
        synchronized (accounts.credentialsPermissionNotificationIds) {
            Pair<Pair<Account, String>, Integer> key = new Pair(new Pair(account, authTokenType), Integer.valueOf(uid));
            nId = (NotificationId) accounts.credentialsPermissionNotificationIds.get(key);
            if (nId == null) {
                nId = new NotificationId("AccountManagerService:38:" + account.hashCode() + ":" + authTokenType.hashCode(), 38);
                accounts.credentialsPermissionNotificationIds.put(key, nId);
            }
        }
        return nId;
    }

    private NotificationId getSigninRequiredNotificationId(UserAccounts accounts, Account account) {
        NotificationId nId;
        synchronized (accounts.signinRequiredNotificationIds) {
            nId = (NotificationId) accounts.signinRequiredNotificationIds.get(account);
            if (nId == null) {
                nId = new NotificationId("AccountManagerService:37:" + account.hashCode(), 37);
                accounts.signinRequiredNotificationIds.put(account, nId);
            }
        }
        return nId;
    }

    public void addAccount(IAccountManagerResponse response, String accountType, String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch, Bundle optionsIn) {
        Bundle.setDefusable(optionsIn, true);
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "addAccount , response " + response + ", authTokenType " + authTokenType + ", requiredFeatures " + Arrays.toString(requiredFeatures) + ", expectActivityLaunch " + expectActivityLaunch + ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
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
                final Bundle options = optionsIn == null ? new Bundle() : optionsIn;
                options.putInt("callerUid", uid);
                options.putInt("callerPid", pid);
                int usrId = UserHandle.getCallingUserId();
                long identityToken = clearCallingIdentity();
                try {
                    UserAccounts accounts = getUserAccounts(usrId);
                    logRecordWithUid(accounts, AccountsDb.DEBUG_ACTION_CALLED_ACCOUNT_ADD, "accounts", uid);
                    final String str = authTokenType;
                    final String[] strArr = requiredFeatures;
                    final String str2 = accountType;
                    new Session(this, accounts, response, accountType, expectActivityLaunch, true, null, false, true) {
                        public void run() throws RemoteException {
                            this.mAuthenticator.addAccount(this, this.mAccountType, str, strArr, options);
                        }

                        protected String toDebugString(long now) {
                            return super.toDebugString(now) + ", addAccount" + ", accountType " + str2 + ", requiredFeatures " + Arrays.toString(strArr);
                        }
                    }.bind();
                } finally {
                    restoreCallingIdentity(identityToken);
                }
            } else {
                try {
                    response.onError(101, "User cannot modify accounts of this type (policy).");
                } catch (RemoteException e2) {
                }
                showCantAddAccount(101, userId);
            }
        }
    }

    public void addAccountAsUser(IAccountManagerResponse response, String accountType, String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch, Bundle optionsIn, int userId) {
        Bundle.setDefusable(optionsIn, true);
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "addAccount, response " + response + ", authTokenType " + authTokenType + ", requiredFeatures " + Arrays.toString(requiredFeatures) + ", expectActivityLaunch " + expectActivityLaunch + ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid() + ", for user id " + userId);
        }
        Preconditions.checkArgument(response != null, "response cannot be null");
        Preconditions.checkArgument(accountType != null, "accountType cannot be null");
        if (isCrossUser(callingUid, userId)) {
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
            final Bundle options = optionsIn == null ? new Bundle() : optionsIn;
            options.putInt("callerUid", uid);
            options.putInt("callerPid", pid);
            long identityToken = clearCallingIdentity();
            try {
                UserAccounts accounts = getUserAccounts(userId);
                logRecordWithUid(accounts, AccountsDb.DEBUG_ACTION_CALLED_ACCOUNT_ADD, "accounts", userId);
                final String str = authTokenType;
                final String[] strArr = requiredFeatures;
                final String str2 = accountType;
                new Session(this, accounts, response, accountType, expectActivityLaunch, true, null, false, true) {
                    public void run() throws RemoteException {
                        this.mAuthenticator.addAccount(this, this.mAccountType, str, strArr, options);
                    }

                    protected String toDebugString(long now) {
                        String str = null;
                        StringBuilder append = new StringBuilder().append(super.toDebugString(now)).append(", addAccount").append(", accountType ").append(str2).append(", requiredFeatures ");
                        if (strArr != null) {
                            str = TextUtils.join(",", strArr);
                        }
                        return append.append(str).toString();
                    }
                }.bind();
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            try {
                response.onError(101, "User cannot modify accounts of this type (policy).");
            } catch (RemoteException e2) {
            }
            showCantAddAccount(101, userId);
        }
    }

    public void startAddAccountSession(IAccountManagerResponse response, String accountType, String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch, Bundle optionsIn) {
        Bundle.setDefusable(optionsIn, true);
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "startAddAccountSession: accountType " + accountType + ", response " + response + ", authTokenType " + authTokenType + ", requiredFeatures " + Arrays.toString(requiredFeatures) + ", expectActivityLaunch " + expectActivityLaunch + ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
        }
        Preconditions.checkArgument(response != null, "response cannot be null");
        Preconditions.checkArgument(accountType != null, "accountType cannot be null");
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
            final Bundle options = optionsIn == null ? new Bundle() : optionsIn;
            options.putInt("callerUid", uid);
            options.putInt("callerPid", pid);
            boolean isPasswordForwardingAllowed = isPermitted(optionsIn.getString("androidPackageName"), uid, "android.permission.GET_PASSWORD");
            long identityToken = clearCallingIdentity();
            try {
                UserAccounts accounts = getUserAccounts(userId);
                logRecordWithUid(accounts, AccountsDb.DEBUG_ACTION_CALLED_START_ACCOUNT_ADD, "accounts", uid);
                final String str = authTokenType;
                final String[] strArr = requiredFeatures;
                final String str2 = accountType;
                new StartAccountSession(this, accounts, response, accountType, expectActivityLaunch, null, false, true, isPasswordForwardingAllowed) {
                    public void run() throws RemoteException {
                        this.mAuthenticator.startAddAccountSession(this, this.mAccountType, str, strArr, options);
                    }

                    protected String toDebugString(long now) {
                        String requiredFeaturesStr = TextUtils.join(",", strArr);
                        StringBuilder append = new StringBuilder().append(super.toDebugString(now)).append(", startAddAccountSession").append(", accountType ").append(str2).append(", requiredFeatures ");
                        if (strArr == null) {
                            requiredFeaturesStr = null;
                        }
                        return append.append(requiredFeaturesStr).toString();
                    }
                }.bind();
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            try {
                response.onError(101, "User cannot modify accounts of this type (policy).");
            } catch (RemoteException e2) {
            }
            showCantAddAccount(101, userId);
        }
    }

    public void finishSessionAsUser(IAccountManagerResponse response, Bundle sessionBundle, boolean expectActivityLaunch, Bundle appInfo, int userId) {
        Bundle.setDefusable(sessionBundle, true);
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "finishSession: response " + response + ", expectActivityLaunch " + expectActivityLaunch + ", caller's uid " + callingUid + ", caller's user id " + UserHandle.getCallingUserId() + ", pid " + Binder.getCallingPid() + ", for user id " + userId);
        }
        Preconditions.checkArgument(response != null, "response cannot be null");
        if (sessionBundle == null || sessionBundle.size() == 0) {
            throw new IllegalArgumentException("sessionBundle is empty");
        } else if (isCrossUser(callingUid, userId)) {
            throw new SecurityException(String.format("User %s trying to finish session for %s without cross user permission", new Object[]{Integer.valueOf(UserHandle.getCallingUserId()), Integer.valueOf(userId)}));
        } else if (canUserModifyAccounts(userId, callingUid)) {
            int pid = Binder.getCallingPid();
            try {
                final Bundle decryptedBundle = CryptoHelper.getInstance().decryptBundle(sessionBundle);
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
                        logRecordWithUid(accounts, AccountsDb.DEBUG_ACTION_CALLED_ACCOUNT_SESSION_FINISH, "accounts", callingUid);
                        final String str = accountType;
                        new Session(this, accounts, response, accountType, expectActivityLaunch, true, null, false, true) {
                            public void run() throws RemoteException {
                                this.mAuthenticator.finishSession(this, this.mAccountType, decryptedBundle);
                            }

                            protected String toDebugString(long now) {
                                return super.toDebugString(now) + ", finishSession" + ", accountType " + str;
                            }
                        }.bind();
                    } finally {
                        restoreCallingIdentity(identityToken);
                    }
                } else {
                    sendErrorResponse(response, 101, "User cannot modify accounts of this type (policy).");
                    showCantAddAccount(101, userId);
                }
            } catch (Throwable e) {
                if (Log.isLoggable(TAG, 3)) {
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
        DevicePolicyManagerInternal dpmi = (DevicePolicyManagerInternal) LocalServices.getService(DevicePolicyManagerInternal.class);
        Intent intent = null;
        if (dpmi == null) {
            intent = getDefaultCantAddAccountIntent(errorCode);
        } else if (errorCode == 100) {
            intent = dpmi.createUserRestrictionSupportIntent(userId, "no_modify_accounts");
        } else if (errorCode == 101) {
            intent = dpmi.createShowAdminSupportIntent(userId, false);
        }
        if (intent == null) {
            intent = getDefaultCantAddAccountIntent(errorCode);
        }
        long identityToken = clearCallingIdentity();
        try {
            this.mContext.startActivityAsUser(intent, new UserHandle(userId));
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    private Intent getDefaultCantAddAccountIntent(int errorCode) {
        Intent cantAddAccount = new Intent(this.mContext, CantAddAccountActivity.class);
        cantAddAccount.putExtra("android.accounts.extra.ERROR_CODE", errorCode);
        cantAddAccount.addFlags(268435456);
        return cantAddAccount;
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
                final Account account2 = account;
                final Bundle bundle = options;
                new Session(this, getUserAccounts(userId), response, account.type, expectActivityLaunch, true, account.name, true, true) {
                    public void run() throws RemoteException {
                        this.mAuthenticator.confirmCredentials(this, account2, bundle);
                    }

                    protected String toDebugString(long now) {
                        return super.toDebugString(now) + ", confirmCredentials" + ", " + account2;
                    }
                }.bind();
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
                final Account account2 = account;
                final String str = authTokenType;
                final Bundle bundle = loginOptions;
                new Session(this, getUserAccounts(userId), response, account.type, expectActivityLaunch, true, account.name, false, true) {
                    public void run() throws RemoteException {
                        this.mAuthenticator.updateCredentials(this, account2, str, bundle);
                    }

                    protected String toDebugString(long now) {
                        if (bundle != null) {
                            bundle.keySet();
                        }
                        return super.toDebugString(now) + ", updateCredentials" + ", " + account2 + ", authTokenType " + str + ", loginOptions " + bundle;
                    }
                }.bind();
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
            int userId = UserHandle.getCallingUserId();
            boolean isPasswordForwardingAllowed = isPermitted(loginOptions.getString("androidPackageName"), uid, "android.permission.GET_PASSWORD");
            long identityToken = clearCallingIdentity();
            try {
                final Account account2 = account;
                final String str = authTokenType;
                final Bundle bundle = loginOptions;
                new StartAccountSession(this, getUserAccounts(userId), response, account.type, expectActivityLaunch, account.name, false, true, isPasswordForwardingAllowed) {
                    public void run() throws RemoteException {
                        this.mAuthenticator.startUpdateCredentialsSession(this, account2, str, bundle);
                    }

                    protected String toDebugString(long now) {
                        if (bundle != null) {
                            bundle.keySet();
                        }
                        return super.toDebugString(now) + ", startUpdateCredentialsSession" + ", " + account2 + ", authTokenType " + str + ", loginOptions " + bundle;
                    }
                }.bind();
            } finally {
                restoreCallingIdentity(identityToken);
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
            int usrId = UserHandle.getCallingUserId();
            long identityToken = clearCallingIdentity();
            try {
                final Account account2 = account;
                final String str = statusToken;
                new Session(this, getUserAccounts(usrId), response, account.type, false, false, account.name, false) {
                    protected String toDebugString(long now) {
                        return super.toDebugString(now) + ", isCredentialsUpdateSuggested" + ", " + account2;
                    }

                    public void run() throws RemoteException {
                        this.mAuthenticator.isCredentialsUpdateSuggested(this, account2, str);
                    }

                    public void onResult(Bundle result) {
                        Bundle.setDefusable(result, true);
                        IAccountManagerResponse response = getResponseAndClose();
                        if (response != null) {
                            if (result == null) {
                                this.sendErrorResponse(response, 5, "null bundle");
                                return;
                            }
                            if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                                Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
                            }
                            if (result.getInt("errorCode", -1) > 0) {
                                this.sendErrorResponse(response, result.getInt("errorCode"), result.getString("errorMessage"));
                            } else if (result.containsKey("booleanResult")) {
                                Bundle newResult = new Bundle();
                                newResult.putBoolean("booleanResult", result.getBoolean("booleanResult", false));
                                this.sendResponse(response, newResult);
                            } else {
                                this.sendErrorResponse(response, 5, "no result in response");
                            }
                        }
                    }
                }.bind();
            } finally {
                restoreCallingIdentity(identityToken);
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
            if (isAccountManagedByCaller(accountType, callingUid, userId) || (isSystemUid(callingUid) ^ 1) == 0) {
                long identityToken = clearCallingIdentity();
                try {
                    final String str = accountType;
                    new Session(this, getUserAccounts(userId), response, accountType, expectActivityLaunch, true, null, false) {
                        public void run() throws RemoteException {
                            this.mAuthenticator.editProperties(this, this.mAccountType);
                        }

                        protected String toDebugString(long now) {
                            return super.toDebugString(now) + ", editProperties" + ", accountType " + str;
                        }
                    }.bind();
                } finally {
                    restoreCallingIdentity(identityToken);
                }
            } else {
                throw new SecurityException(String.format("uid %s cannot edit authenticator properites for account type: %s", new Object[]{Integer.valueOf(callingUid), accountType}));
            }
        }
    }

    public boolean hasAccountAccess(Account account, String packageName, UserHandle userHandle) {
        if (UserHandle.getAppId(Binder.getCallingUid()) != 1000) {
            throw new SecurityException("Can be called only by system UID");
        }
        Preconditions.checkNotNull(account, "account cannot be null");
        Preconditions.checkNotNull(packageName, "packageName cannot be null");
        Preconditions.checkNotNull(userHandle, "userHandle cannot be null");
        int userId = userHandle.getIdentifier();
        Preconditions.checkArgumentInRange(userId, 0, HwBootFail.STAGE_BOOT_SUCCESS, "user must be concrete");
        try {
            return hasAccountAccess(account, packageName, this.mPackageManager.getPackageUidAsUser(packageName, userId));
        } catch (NameNotFoundException e) {
            Log.d(TAG, "Package not found " + e.getMessage());
            return false;
        }
    }

    private String getPackageNameForUid(int uid) {
        int i = 0;
        String[] packageNames = this.mPackageManager.getPackagesForUid(uid);
        if (ArrayUtils.isEmpty(packageNames)) {
            return null;
        }
        String packageName = packageNames[0];
        if (packageNames.length == 1) {
            return packageName;
        }
        int oldestVersion = HwBootFail.STAGE_BOOT_SUCCESS;
        int length = packageNames.length;
        while (i < length) {
            String name = packageNames[i];
            try {
                ApplicationInfo applicationInfo = this.mPackageManager.getApplicationInfo(name, 0);
                if (applicationInfo != null) {
                    int version = applicationInfo.targetSdkVersion;
                    if (version < oldestVersion) {
                        oldestVersion = version;
                        packageName = name;
                    }
                }
            } catch (NameNotFoundException e) {
            }
            i++;
        }
        return packageName;
    }

    private boolean hasAccountAccess(Account account, String packageName, int uid) {
        boolean z = true;
        if (packageName == null) {
            packageName = getPackageNameForUid(uid);
            if (packageName == null) {
                return false;
            }
        }
        if (permissionIsGranted(account, null, uid, UserHandle.getUserId(uid))) {
            return true;
        }
        int visibility = resolveAccountVisibility(account, packageName, getUserAccounts(UserHandle.getUserId(uid))).intValue();
        if (!(visibility == 1 || visibility == 2)) {
            z = false;
        }
        return z;
    }

    public IntentSender createRequestAccountAccessIntentSenderAsUser(Account account, String packageName, UserHandle userHandle) {
        if (UserHandle.getAppId(Binder.getCallingUid()) != 1000) {
            throw new SecurityException("Can be called only by system UID");
        }
        Preconditions.checkNotNull(account, "account cannot be null");
        Preconditions.checkNotNull(packageName, "packageName cannot be null");
        Preconditions.checkNotNull(userHandle, "userHandle cannot be null");
        int userId = userHandle.getIdentifier();
        Preconditions.checkArgumentInRange(userId, 0, HwBootFail.STAGE_BOOT_SUCCESS, "user must be concrete");
        try {
            Intent intent = newRequestAccountAccessIntent(account, packageName, this.mPackageManager.getPackageUidAsUser(packageName, userId), null);
            long identity = Binder.clearCallingIdentity();
            try {
                IntentSender intentSender = PendingIntent.getActivityAsUser(this.mContext, 0, intent, 1409286144, null, new UserHandle(userId)).getIntentSender();
                return intentSender;
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        } catch (NameNotFoundException e) {
            Slog.e(TAG, "Unknown package " + packageName);
            return null;
        }
    }

    private Intent newRequestAccountAccessIntent(Account account, String packageName, int uid, RemoteCallback callback) {
        final Account account2 = account;
        final int i = uid;
        final String str = packageName;
        final RemoteCallback remoteCallback = callback;
        AccountAuthenticatorResponse accountAuthenticatorResponse = new AccountAuthenticatorResponse(new IAccountAuthenticatorResponse.Stub() {
            public void onResult(Bundle value) throws RemoteException {
                handleAuthenticatorResponse(true);
            }

            public void onRequestContinued() {
            }

            public void onError(int errorCode, String errorMessage) throws RemoteException {
                handleAuthenticatorResponse(false);
            }

            private void handleAuthenticatorResponse(boolean accessGranted) throws RemoteException {
                AccountManagerService.this.cancelNotification(AccountManagerService.this.getCredentialPermissionNotificationId(account2, "com.android.AccountManager.ACCOUNT_ACCESS_TOKEN_TYPE", i), str, UserHandle.getUserHandleForUid(i));
                if (remoteCallback != null) {
                    Bundle result = new Bundle();
                    result.putBoolean("booleanResult", accessGranted);
                    remoteCallback.sendResult(result);
                }
            }
        });
        return newGrantCredentialsPermissionIntent(account, packageName, uid, accountAuthenticatorResponse, "com.android.AccountManager.ACCOUNT_ACCESS_TOKEN_TYPE", false);
    }

    public boolean someUserHasAccount(Account account) {
        if (UserHandle.isSameApp(1000, Binder.getCallingUid())) {
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
        this.mAppOpsManager.checkPackage(callingUid, opPackageName);
        List<String> visibleAccountTypes = getTypesVisibleToCaller(callingUid, userId, opPackageName);
        if (visibleAccountTypes.isEmpty()) {
            return EMPTY_ACCOUNT_ARRAY;
        }
        long identityToken = clearCallingIdentity();
        try {
            Account[] accountsInternal = getAccountsInternal(getUserAccounts(userId), callingUid, opPackageName, visibleAccountTypes, false);
            return accountsInternal;
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    public AccountAndUser[] getRunningAccounts() {
        try {
            return getAccounts(ActivityManager.getService().getRunningUserIds());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public AccountAndUser[] getAllAccounts() {
        List<UserInfo> users = getUserManager().getUsers(true);
        int[] userIds = new int[users.size()];
        for (int i = 0; i < userIds.length; i++) {
            userIds[i] = ((UserInfo) users.get(i)).id;
        }
        return getAccounts(userIds);
    }

    private AccountAndUser[] getAccounts(int[] userIds) {
        ArrayList<AccountAndUser> runningAccounts = Lists.newArrayList();
        for (int userId : userIds) {
            try {
                UserAccounts userAccounts = getUserAccounts(userId);
                if (userAccounts != null) {
                    for (Account account : getAccountsFromCache(userAccounts, null, Binder.getCallingUid(), null, false)) {
                        runningAccounts.add(new AccountAndUser(account, userId));
                    }
                }
            } catch (SQLiteCantOpenDatabaseException e) {
                Slog.e(TAG, e.getMessage(), new Throwable());
            }
        }
        return (AccountAndUser[]) runningAccounts.toArray(new AccountAndUser[runningAccounts.size()]);
    }

    public Account[] getAccountsAsUser(String type, int userId, String opPackageName) {
        this.mAppOpsManager.checkPackage(Binder.getCallingUid(), opPackageName);
        return getAccountsAsUserForPackage(type, userId, opPackageName, -1, opPackageName, false);
    }

    private Account[] getAccountsAsUserForPackage(String type, int userId, String callingPackage, int packageUid, String opPackageName, boolean includeUserManagedNotVisible) {
        int callingUid = Binder.getCallingUid();
        if (userId == UserHandle.getCallingUserId() || callingUid == 1000 || this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "getAccounts: accountType " + type + ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
            }
            List<String> managedTypes = getTypesManagedByCaller(callingUid, UserHandle.getUserId(callingUid));
            if (packageUid != -1 && (UserHandle.isSameApp(callingUid, 1000) || (type != null && managedTypes.contains(type)))) {
                callingUid = packageUid;
                opPackageName = callingPackage;
            }
            List<String> visibleAccountTypes = getTypesVisibleToCaller(callingUid, userId, opPackageName);
            if (visibleAccountTypes.isEmpty() || (type != null && (visibleAccountTypes.contains(type) ^ 1) != 0)) {
                return EMPTY_ACCOUNT_ARRAY;
            }
            if (visibleAccountTypes.contains(type)) {
                visibleAccountTypes = new ArrayList();
                visibleAccountTypes.add(type);
            }
            long identityToken = clearCallingIdentity();
            try {
                Account[] accountsInternal = getAccountsInternal(getUserAccounts(userId), callingUid, opPackageName, visibleAccountTypes, includeUserManagedNotVisible);
                return accountsInternal;
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            throw new SecurityException("User " + UserHandle.getCallingUserId() + " trying to get account for " + userId);
        }
    }

    private Account[] getAccountsInternal(UserAccounts userAccounts, int callingUid, String callingPackage, List<String> visibleAccountTypes, boolean includeUserManagedNotVisible) {
        ArrayList<Account> visibleAccounts = new ArrayList();
        for (String visibleType : visibleAccountTypes) {
            Account[] accountsForType = getAccountsFromCache(userAccounts, visibleType, callingUid, callingPackage, includeUserManagedNotVisible);
            if (accountsForType != null) {
                visibleAccounts.addAll(Arrays.asList(accountsForType));
            }
        }
        Account[] result = new Account[visibleAccounts.size()];
        for (int i = 0; i < visibleAccounts.size(); i++) {
            result[i] = (Account) visibleAccounts.get(i);
        }
        return result;
    }

    public void addSharedAccountsFromParentUser(int parentUserId, int userId, String opPackageName) {
        checkManageOrCreateUsersPermission("addSharedAccountsFromParentUser");
        for (Account account : getAccountsAsUser(null, parentUserId, opPackageName)) {
            addSharedAccountAsUser(account, userId);
        }
    }

    private boolean addSharedAccountAsUser(Account account, int userId) {
        UserAccounts accounts = getUserAccounts(handleIncomingUser(userId));
        accounts.accountsDb.deleteSharedAccount(account);
        long accountId = accounts.accountsDb.insertSharedAccount(account);
        if (accountId < 0) {
            Log.w(TAG, "insertAccountIntoDatabase , skipping the DB insert failed");
            return false;
        }
        logRecord(AccountsDb.DEBUG_ACTION_ACCOUNT_ADD, "shared_accounts", accountId, accounts);
        return true;
    }

    public boolean renameSharedAccountAsUser(Account account, String newName, int userId) {
        UserAccounts accounts = getUserAccounts(handleIncomingUser(userId));
        long sharedTableAccountId = accounts.accountsDb.findSharedAccountId(account);
        int r = accounts.accountsDb.renameSharedAccount(account, newName);
        if (r > 0) {
            logRecord(AccountsDb.DEBUG_ACTION_ACCOUNT_RENAME, "shared_accounts", sharedTableAccountId, accounts, getCallingUid());
            renameAccountInternal(accounts, account, newName);
        }
        return r > 0;
    }

    public boolean removeSharedAccountAsUser(Account account, int userId) {
        return removeSharedAccountAsUser(account, userId, getCallingUid());
    }

    private boolean removeSharedAccountAsUser(Account account, int userId, int callingUid) {
        UserAccounts accounts = getUserAccounts(handleIncomingUser(userId));
        long sharedTableAccountId = accounts.accountsDb.findSharedAccountId(account);
        boolean deleted = accounts.accountsDb.deleteSharedAccount(account);
        if (deleted) {
            logRecord(AccountsDb.DEBUG_ACTION_ACCOUNT_REMOVE, "shared_accounts", sharedTableAccountId, accounts, callingUid);
            removeAccountInternal(accounts, account, callingUid);
        }
        return deleted;
    }

    public Account[] getSharedAccountsAsUser(int userId) {
        Account[] accountArray;
        UserAccounts accounts = getUserAccounts(handleIncomingUser(userId));
        synchronized (accounts.dbLock) {
            List<Account> accountList = accounts.accountsDb.getSharedAccounts();
            accountArray = new Account[accountList.size()];
            accountList.toArray(accountArray);
        }
        return accountArray;
    }

    public Account[] getAccounts(String type, String opPackageName) {
        return getAccountsAsUser(type, UserHandle.getCallingUserId(), opPackageName);
    }

    public Account[] getAccountsForPackage(String packageName, int uid, String opPackageName) {
        int callingUid = Binder.getCallingUid();
        if (UserHandle.isSameApp(callingUid, 1000)) {
            return getAccountsAsUserForPackage(null, UserHandle.getCallingUserId(), packageName, uid, opPackageName, true);
        }
        throw new SecurityException("getAccountsForPackage() called from unauthorized uid " + callingUid + " with uid=" + uid);
    }

    public Account[] getAccountsByTypeForPackage(String type, String packageName, String opPackageName) {
        int callingUid = Binder.getCallingUid();
        int userId = UserHandle.getCallingUserId();
        this.mAppOpsManager.checkPackage(callingUid, opPackageName);
        try {
            int packageUid = this.mPackageManager.getPackageUidAsUser(packageName, userId);
            if (!UserHandle.isSameApp(callingUid, 1000) && type != null && (isAccountManagedByCaller(type, callingUid, userId) ^ 1) != 0) {
                return EMPTY_ACCOUNT_ARRAY;
            }
            if (UserHandle.isSameApp(callingUid, 1000) || type != null) {
                return getAccountsAsUserForPackage(type, userId, packageName, packageUid, opPackageName, true);
            }
            return getAccountsAsUserForPackage(type, userId, packageName, packageUid, opPackageName, false);
        } catch (NameNotFoundException re) {
            Slog.e(TAG, "Couldn't determine the packageUid for " + packageName + re);
            return EMPTY_ACCOUNT_ARRAY;
        }
    }

    private boolean needToStartChooseAccountActivity(Account[] accounts, String callingPackage) {
        if (accounts.length < 1) {
            return false;
        }
        return accounts.length > 1 || resolveAccountVisibility(accounts[0], callingPackage, getUserAccounts(UserHandle.getCallingUserId())).intValue() == 4;
    }

    private void startChooseAccountActivityWithAccounts(IAccountManagerResponse response, Account[] accounts) {
        Intent intent = new Intent(this.mContext, ChooseAccountActivity.class);
        intent.putExtra("accounts", accounts);
        intent.putExtra("accountManagerResponse", new AccountManagerResponse(response));
        this.mContext.startActivityAsUser(intent, UserHandle.of(UserHandle.getCallingUserId()));
    }

    private void handleGetAccountsResult(IAccountManagerResponse response, Account[] accounts, String callingPackage) {
        if (needToStartChooseAccountActivity(accounts, callingPackage)) {
            startChooseAccountActivityWithAccounts(response, accounts);
        } else if (accounts.length == 1) {
            Bundle bundle = new Bundle();
            bundle.putString("authAccount", accounts[0].name);
            bundle.putString("accountType", accounts[0].type);
            onResult(response, bundle);
        } else {
            onResult(response, new Bundle());
        }
    }

    public void getAccountByTypeAndFeatures(IAccountManagerResponse response, String accountType, String[] features, String opPackageName) {
        int callingUid = Binder.getCallingUid();
        this.mAppOpsManager.checkPackage(callingUid, opPackageName);
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "getAccount: accountType " + accountType + ", response " + response + ", features " + Arrays.toString(features) + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (response == null) {
            throw new IllegalArgumentException("response is null");
        } else if (accountType == null) {
            throw new IllegalArgumentException("accountType is null");
        } else {
            int userId = UserHandle.getCallingUserId();
            long identityToken = clearCallingIdentity();
            try {
                UserAccounts userAccounts = getUserAccounts(userId);
                if (ArrayUtils.isEmpty(features)) {
                    handleGetAccountsResult(response, getAccountsFromCache(userAccounts, accountType, callingUid, opPackageName, true), opPackageName);
                    return;
                }
                final IAccountManagerResponse iAccountManagerResponse = response;
                final String str = opPackageName;
                new GetAccountsByTypeAndFeatureSession(userAccounts, new IAccountManagerResponse.Stub() {
                    public void onResult(Bundle value) throws RemoteException {
                        Parcelable[] parcelables = value.getParcelableArray("accounts");
                        Account[] accounts = new Account[parcelables.length];
                        for (int i = 0; i < parcelables.length; i++) {
                            accounts[i] = (Account) parcelables[i];
                        }
                        AccountManagerService.this.handleGetAccountsResult(iAccountManagerResponse, accounts, str);
                    }

                    public void onError(int errorCode, String errorMessage) throws RemoteException {
                    }
                }, accountType, features, callingUid, opPackageName, true).bind();
                restoreCallingIdentity(identityToken);
            } finally {
                restoreCallingIdentity(identityToken);
            }
        }
    }

    public void getAccountsByFeatures(IAccountManagerResponse response, String type, String[] features, String opPackageName) {
        int callingUid = Binder.getCallingUid();
        this.mAppOpsManager.checkPackage(callingUid, opPackageName);
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "getAccounts: accountType " + type + ", response " + response + ", features " + Arrays.toString(features) + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
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
                        Account[] accounts = getAccountsFromCache(userAccounts, type, callingUid, opPackageName, false);
                        result = new Bundle();
                        result.putParcelableArray("accounts", accounts);
                        onResult(response, result);
                        return;
                    }
                    new GetAccountsByTypeAndFeatureSession(userAccounts, response, type, features, callingUid, opPackageName, false).bind();
                    restoreCallingIdentity(identityToken);
                } finally {
                    restoreCallingIdentity(identityToken);
                }
            } else {
                result = new Bundle();
                result.putParcelableArray("accounts", EMPTY_ACCOUNT_ARRAY);
                try {
                    response.onResult(result);
                } catch (Throwable e) {
                    Log.e(TAG, "Cannot respond to caller do to exception.", e);
                }
            }
        }
    }

    public void onAccountAccessed(String token) throws RemoteException {
        int uid = Binder.getCallingUid();
        if (UserHandle.getAppId(uid) != 1000) {
            int userId = UserHandle.getCallingUserId();
            long identity = Binder.clearCallingIdentity();
            try {
                for (Account account : getAccounts(userId, this.mContext.getOpPackageName())) {
                    if (Objects.equals(account.getAccessId(), token) && !hasAccountAccess(account, null, uid)) {
                        updateAppPermission(account, "com.android.AccountManager.ACCOUNT_ACCESS_TOKEN_TYPE", uid, true);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    private void logRecord(UserAccounts accounts, String action, String tableName) {
        logRecord(action, tableName, -1, accounts);
    }

    private void logRecordWithUid(UserAccounts accounts, String action, String tableName, int uid) {
        logRecord(action, tableName, -1, accounts, uid);
    }

    private void logRecord(String action, String tableName, long accountId, UserAccounts userAccount) {
        logRecord(action, tableName, accountId, userAccount, getCallingUid());
    }

    private void logRecord(String action, String tableName, long accountId, UserAccounts userAccount, int callingUid) {
        AnonymousClass1LogRecordTask logTask = new AnonymousClass1LogRecordTask(action, tableName, accountId, userAccount, callingUid, (long) userAccount.debugDbInsertionPoint);
        userAccount.debugDbInsertionPoint = (userAccount.debugDbInsertionPoint + 1) % 64;
        this.mHandler.post(logTask);
    }

    private void initializeDebugDbSizeAndCompileSqlStatementForLogging(UserAccounts userAccount) {
        userAccount.debugDbInsertionPoint = userAccount.accountsDb.calculateDebugTableInsertionPoint();
        userAccount.statementForLogging = userAccount.accountsDb.compileSqlStatementForLogging();
    }

    public IBinder onBind(Intent intent) {
        return asBinder();
    }

    private static boolean scanArgs(String[] args, String value) {
        if (args != null) {
            for (String arg : args) {
                if (value.equals(arg)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, fout)) {
            boolean isCheckinRequest = !scanArgs(args, "--checkin") ? scanArgs(args, "-c") : true;
            IndentingPrintWriter ipw = new IndentingPrintWriter(fout, "  ");
            for (UserInfo user : getUserManager().getUsers()) {
                ipw.println("User " + user + ":");
                ipw.increaseIndent();
                dumpUser(getUserAccounts(user.id), fd, ipw, args, isCheckinRequest);
                ipw.println();
                ipw.decreaseIndent();
            }
        }
    }

    private void dumpUser(UserAccounts userAccounts, FileDescriptor fd, PrintWriter fout, String[] args, boolean isCheckinRequest) {
        if (isCheckinRequest) {
            synchronized (userAccounts.dbLock) {
                userAccounts.accountsDb.dumpDeAccountsTable(fout);
            }
            return;
        }
        Account[] accounts = getAccountsFromCache(userAccounts, null, 1000, null, false);
        fout.println("Accounts: " + accounts.length);
        for (Account account : accounts) {
            fout.println("  " + account);
        }
        fout.println();
        synchronized (userAccounts.dbLock) {
            userAccounts.accountsDb.dumpDebugTable(fout);
        }
        fout.println();
        synchronized (this.mSessions) {
            long now = SystemClock.elapsedRealtime();
            fout.println("Active Sessions: " + this.mSessions.size());
            for (Session session : this.mSessions.values()) {
                fout.println("  " + session.toDebugString(now));
            }
        }
        fout.println();
        this.mAuthenticatorCache.dump(fd, fout, args, userAccounts.userId);
    }

    private void doNotification(UserAccounts accounts, Account account, CharSequence message, Intent intent, String packageName, int userId) {
        long identityToken = clearCallingIdentity();
        try {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "doNotification: " + message + " intent:" + intent);
            }
            if (intent.getComponent() == null || !GrantCredentialsPermissionActivity.class.getName().equals(intent.getComponent().getClassName())) {
                Context contextForUser = getContextForUser(new UserHandle(userId));
                NotificationId id = getSigninRequiredNotificationId(accounts, account);
                intent.addCategory(id.mTag);
                String notificationTitleFormat = contextForUser.getText(17040514).toString();
                Builder contentText = new Builder(contextForUser, SystemNotificationChannels.ACCOUNT).setWhen(0).setSmallIcon(17301642).setLargeIcon(BitmapFactory.decodeResource(this.mContext.getResources(), 33751687)).setColor(contextForUser.getColor(17170769)).setContentTitle(String.format(notificationTitleFormat, new Object[]{account.name})).setContentText(message);
                installNotification(id, contentText.setContentIntent(PendingIntent.getActivityAsUser(this.mContext, 0, intent, 268435456, null, new UserHandle(userId))).build(), packageName, userId);
            } else {
                createNoCredentialsPermissionNotification(account, intent, packageName, userId);
            }
            restoreCallingIdentity(identityToken);
        } catch (Throwable th) {
            restoreCallingIdentity(identityToken);
        }
    }

    private void installNotification(NotificationId id, Notification notification, String packageName, int userId) {
        long token = clearCallingIdentity();
        try {
            try {
                this.mInjector.getNotificationManager().enqueueNotificationWithTag(packageName, packageName, id.mTag, id.mId, notification, userId);
            } catch (RemoteException e) {
            }
            Binder.restoreCallingIdentity(token);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void cancelNotification(NotificationId id, UserHandle user) {
        cancelNotification(id, this.mContext.getPackageName(), user);
    }

    private void cancelNotification(NotificationId id, String packageName, UserHandle user) {
        long identityToken = clearCallingIdentity();
        try {
            this.mInjector.getNotificationManager().cancelNotificationWithTag(packageName, id.mTag, id.mId, user.getIdentifier());
        } catch (RemoteException e) {
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    private boolean isPermittedForPackage(String packageName, int uid, int userId, String... permissions) {
        long identity = Binder.clearCallingIdentity();
        try {
            IPackageManager pm = ActivityThread.getPackageManager();
            for (String perm : permissions) {
                if (pm.checkPermission(perm, packageName, userId) == 0) {
                    int opCode = AppOpsManager.permissionToOpCode(perm);
                    if (opCode == -1 || this.mAppOpsManager.noteOpNoThrow(opCode, uid, packageName) == 0) {
                        Binder.restoreCallingIdentity(identity);
                        return true;
                    }
                }
            }
            Binder.restoreCallingIdentity(identity);
        } catch (RemoteException e) {
            Binder.restoreCallingIdentity(identity);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        return false;
    }

    private boolean isPermitted(String opPackageName, int callingUid, String... permissions) {
        for (String perm : permissions) {
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
            return ActivityManager.getService().handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, true, "", null);
        } catch (RemoteException e) {
            return userId;
        }
    }

    private boolean isPrivileged(int callingUid) {
        long identityToken = Binder.clearCallingIdentity();
        try {
            String[] packages = this.mPackageManager.getPackagesForUid(callingUid);
            if (packages == null) {
                Log.d(TAG, "No packages for callingUid " + callingUid);
                return false;
            }
            int length = packages.length;
            int i = 0;
            while (i < length) {
                try {
                    PackageInfo packageInfo = this.mPackageManager.getPackageInfo(packages[i], 0);
                    if (packageInfo != null && (packageInfo.applicationInfo.privateFlags & 8) != 0) {
                        return true;
                    }
                    i++;
                } catch (NameNotFoundException e) {
                    Log.d(TAG, "Package not found " + e.getMessage());
                    return false;
                }
            }
            return false;
        } finally {
            Binder.restoreCallingIdentity(identityToken);
        }
    }

    private boolean permissionIsGranted(Account account, String authTokenType, int callerUid, int userId) {
        if (UserHandle.getAppId(callerUid) == 1000) {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "Access to " + account + " granted calling uid is system");
            }
            return true;
        } else if (isPrivileged(callerUid)) {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "Access to " + account + " granted calling uid " + callerUid + " privileged");
            }
            return true;
        } else if (account != null && isAccountManagedByCaller(account.type, callerUid, userId)) {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "Access to " + account + " granted calling uid " + callerUid + " manages the account");
            }
            return true;
        } else if (account == null || !hasExplicitlyGrantedPermission(account, authTokenType, callerUid)) {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "Access to " + account + " not granted for uid " + callerUid);
            }
            return false;
        } else {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "Access to " + account + " granted calling uid " + callerUid + " user granted access");
            }
            return true;
        }
    }

    private boolean isAccountVisibleToCaller(String accountType, int callingUid, int userId, String opPackageName) {
        if (accountType == null) {
            return false;
        }
        return getTypesVisibleToCaller(callingUid, userId, opPackageName).contains(accountType);
    }

    private boolean checkGetAccountsPermission(String packageName, int uid, int userId) {
        return isPermittedForPackage(packageName, uid, userId, "android.permission.GET_ACCOUNTS", "android.permission.GET_ACCOUNTS_PRIVILEGED");
    }

    private boolean checkReadContactsPermission(String packageName, int uid, int userId) {
        return isPermittedForPackage(packageName, uid, userId, "android.permission.READ_CONTACTS");
    }

    private boolean accountTypeManagesContacts(String accountType, int userId) {
        if (accountType == null) {
            return false;
        }
        long identityToken = Binder.clearCallingIdentity();
        try {
            Collection<ServiceInfo<AuthenticatorDescription>> serviceInfos = this.mAuthenticatorCache.getAllServices(userId);
            for (ServiceInfo<AuthenticatorDescription> serviceInfo : serviceInfos) {
                if (accountType.equals(((AuthenticatorDescription) serviceInfo.type).type)) {
                    return isPermittedForPackage(((AuthenticatorDescription) serviceInfo.type).packageName, serviceInfo.uid, userId, "android.permission.WRITE_CONTACTS");
                }
            }
            return false;
        } finally {
            Binder.restoreCallingIdentity(identityToken);
        }
    }

    private int checkPackageSignature(String accountType, int callingUid, int userId) {
        if (accountType == null) {
            return 0;
        }
        long identityToken = Binder.clearCallingIdentity();
        try {
            Collection<ServiceInfo<AuthenticatorDescription>> serviceInfos = this.mAuthenticatorCache.getAllServices(userId);
            for (ServiceInfo<AuthenticatorDescription> serviceInfo : serviceInfos) {
                if (accountType.equals(((AuthenticatorDescription) serviceInfo.type).type)) {
                    if (serviceInfo.uid == callingUid) {
                        return 2;
                    }
                    if (this.mPackageManager.checkSignatures(serviceInfo.uid, callingUid) == 0) {
                        return 1;
                    }
                }
            }
            return 0;
        } finally {
            Binder.restoreCallingIdentity(identityToken);
        }
    }

    private boolean isAccountManagedByCaller(String accountType, int callingUid, int userId) {
        if (accountType == null) {
            return false;
        }
        return getTypesManagedByCaller(callingUid, userId).contains(accountType);
    }

    private List<String> getTypesVisibleToCaller(int callingUid, int userId, String opPackageName) {
        return getTypesForCaller(callingUid, userId, true);
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
                if (isOtherwisePermitted || this.mPackageManager.checkSignatures(serviceInfo.uid, callingUid) == 0) {
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
            for (Account account : (Account[]) getUserAccountsForCaller().accountCache.get(accountType)) {
                if (account.name.equals(accountName)) {
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
        if (UserHandle.getAppId(callerUid) == 1000) {
            return true;
        }
        UserAccounts accounts = getUserAccounts(UserHandle.getUserId(callerUid));
        synchronized (accounts.dbLock) {
            synchronized (accounts.cacheLock) {
                long grantsCount;
                if (authTokenType != null) {
                    grantsCount = accounts.accountsDb.findMatchingGrantsCount(callerUid, authTokenType, account);
                } else {
                    grantsCount = accounts.accountsDb.findMatchingGrantsCountAnyToken(callerUid, account);
                }
                boolean permissionGranted = grantsCount > 0;
                if (permissionGranted || !ActivityManager.isRunningInTestHarness()) {
                    return permissionGranted;
                }
                Log.d(TAG, "no credentials permission for usage of " + account + ", " + authTokenType + " by uid " + callerUid + " but ignoring since device is in test harness.");
                return true;
            }
        }
    }

    private boolean isSystemUid(int callingUid) {
        String[] packages = null;
        long ident = Binder.clearCallingIdentity();
        try {
            packages = this.mPackageManager.getPackagesForUid(callingUid);
            if (packages != null) {
                for (String name : packages) {
                    try {
                        PackageInfo packageInfo = this.mPackageManager.getPackageInfo(name, 0);
                        if (!(packageInfo == null || (packageInfo.applicationInfo.flags & 1) == 0)) {
                            return true;
                        }
                    } catch (NameNotFoundException e) {
                        Log.w(TAG, String.format("Could not find package [%s]", new Object[]{name}), e);
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
        for (String forbiddenType : typesArray) {
            if (forbiddenType.equals(accountType)) {
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
        if (UserHandle.getAppId(getCallingUid()) != 1000) {
            throw new SecurityException();
        } else if (value) {
            grantAppPermission(account, authTokenType, uid);
        } else {
            revokeAppPermission(account, authTokenType, uid);
        }
    }

    void grantAppPermission(Account account, String authTokenType, int uid) {
        if (account == null || authTokenType == null) {
            Log.e(TAG, "grantAppPermission: called with invalid arguments", new Exception());
            return;
        }
        UserAccounts accounts = getUserAccounts(UserHandle.getUserId(uid));
        synchronized (accounts.dbLock) {
            synchronized (accounts.cacheLock) {
                long accountId = accounts.accountsDb.findDeAccountId(account);
                if (accountId >= 0) {
                    accounts.accountsDb.insertGrant(accountId, authTokenType, uid);
                }
                cancelNotification(getCredentialPermissionNotificationId(account, authTokenType, uid), UserHandle.of(accounts.userId));
                cancelAccountAccessRequestNotificationIfNeeded(account, uid, true);
            }
        }
        for (OnAppPermissionChangeListener listener : this.mAppPermissionChangeListeners) {
            this.mHandler.post(new com.android.server.accounts.-$Lambda$JwXVQhqSYlVkCeKB5Nx7U6RsZlU.AnonymousClass1(uid, listener, account));
        }
    }

    private void revokeAppPermission(Account account, String authTokenType, int uid) {
        if (account == null || authTokenType == null) {
            Log.e(TAG, "revokeAppPermission: called with invalid arguments", new Exception());
            return;
        }
        UserAccounts accounts = getUserAccounts(UserHandle.getUserId(uid));
        synchronized (accounts.dbLock) {
            synchronized (accounts.cacheLock) {
                accounts.accountsDb.beginTransaction();
                try {
                    long accountId = accounts.accountsDb.findDeAccountId(account);
                    if (accountId >= 0) {
                        accounts.accountsDb.deleteGrantsByAccountIdAuthTokenTypeAndUid(accountId, authTokenType, (long) uid);
                        accounts.accountsDb.setTransactionSuccessful();
                    }
                    accounts.accountsDb.endTransaction();
                    cancelNotification(getCredentialPermissionNotificationId(account, authTokenType, uid), UserHandle.of(accounts.userId));
                } catch (Throwable th) {
                    accounts.accountsDb.endTransaction();
                }
            }
        }
        for (OnAppPermissionChangeListener listener : this.mAppPermissionChangeListeners) {
            this.mHandler.post(new com.android.server.accounts.-$Lambda$JwXVQhqSYlVkCeKB5Nx7U6RsZlU.AnonymousClass3(uid, listener, account));
        }
    }

    private void removeAccountFromCacheLocked(UserAccounts accounts, Account account) {
        Account[] oldAccountsForType = (Account[]) accounts.accountCache.get(account.type);
        if (oldAccountsForType != null) {
            ArrayList<Account> newAccountsList = new ArrayList();
            for (Account curAccount : oldAccountsForType) {
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
        accounts.visibilityCache.remove(account);
    }

    private Account insertAccountIntoCacheLocked(UserAccounts accounts, Account account) {
        String token;
        Account[] accountsForType = (Account[]) accounts.accountCache.get(account.type);
        int oldLength = accountsForType != null ? accountsForType.length : 0;
        Account[] newAccountsForType = new Account[(oldLength + 1)];
        if (accountsForType != null) {
            System.arraycopy(accountsForType, 0, newAccountsForType, 0, oldLength);
        }
        if (account.getAccessId() != null) {
            token = account.getAccessId();
        } else {
            token = UUID.randomUUID().toString();
        }
        newAccountsForType[oldLength] = new Account(account, token);
        accounts.accountCache.put(account.type, newAccountsForType);
        return newAccountsForType[oldLength];
    }

    private Account[] filterAccounts(UserAccounts accounts, Account[] unfiltered, int callingUid, String callingPackage, boolean includeManagedNotVisible) {
        String visibilityFilterPackage = callingPackage;
        if (callingPackage == null) {
            visibilityFilterPackage = getPackageNameForUid(callingUid);
        }
        Map<Account, Integer> firstPass = new LinkedHashMap();
        for (Account account : unfiltered) {
            int visibility = resolveAccountVisibility(account, visibilityFilterPackage, accounts).intValue();
            if (visibility == 1 || visibility == 2 || (includeManagedNotVisible && visibility == 4)) {
                firstPass.put(account, Integer.valueOf(visibility));
            }
        }
        Map<Account, Integer> secondPass = filterSharedAccounts(accounts, firstPass, callingUid, callingPackage);
        return (Account[]) secondPass.keySet().toArray(new Account[secondPass.size()]);
    }

    private Map<Account, Integer> filterSharedAccounts(UserAccounts userAccounts, Map<Account, Integer> unfiltered, int callingUid, String callingPackage) {
        if (getUserManager() == null || userAccounts == null || userAccounts.userId < 0 || callingUid == 1000) {
            return unfiltered;
        }
        UserInfo user = getUserManager().getUserInfo(userAccounts.userId);
        if (user == null || !user.isRestricted()) {
            return unfiltered;
        }
        String[] packages = this.mPackageManager.getPackagesForUid(callingUid);
        if (packages == null) {
            packages = new String[0];
        }
        String visibleList = this.mContext.getResources().getString(17039751);
        for (String packageName : packages) {
            if (visibleList.contains(";" + packageName + ";")) {
                return unfiltered;
            }
        }
        Account[] sharedAccounts = getSharedAccountsAsUser(userAccounts.userId);
        if (ArrayUtils.isEmpty(sharedAccounts)) {
            return unfiltered;
        }
        String requiredAccountType = "";
        PackageInfo pi;
        if (callingPackage == null) {
            for (String packageName2 : packages) {
                pi = this.mPackageManager.getPackageInfo(packageName2, 0);
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
                Log.d(TAG, "Package not found " + e.getMessage());
            }
        }
        Map<Account, Integer> filtered = new LinkedHashMap();
        for (Entry<Account, Integer> entry : unfiltered.entrySet()) {
            Account account = (Account) entry.getKey();
            if (account.type.equals(requiredAccountType)) {
                filtered.put(account, (Integer) entry.getValue());
            } else {
                boolean found = false;
                for (Account shared : sharedAccounts) {
                    if (shared.equals(account)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    filtered.put(account, (Integer) entry.getValue());
                }
            }
        }
        return filtered;
    }

    protected Account[] getAccountsFromCache(UserAccounts userAccounts, String accountType, int callingUid, String callingPackage, boolean includeManagedNotVisible) {
        Preconditions.checkState(Thread.holdsLock(userAccounts.cacheLock) ^ 1, "Method should not be called with cacheLock");
        Account[] accounts;
        if (accountType != null) {
            synchronized (userAccounts.cacheLock) {
                accounts = (Account[]) userAccounts.accountCache.get(accountType);
            }
            if (accounts == null) {
                return EMPTY_ACCOUNT_ARRAY;
            }
            return filterAccounts(userAccounts, (Account[]) Arrays.copyOf(accounts, accounts.length), callingUid, callingPackage, includeManagedNotVisible);
        }
        int totalLength = 0;
        synchronized (userAccounts.cacheLock) {
            for (Account[] accounts2 : userAccounts.accountCache.values()) {
                totalLength += accounts2.length;
            }
            if (totalLength == 0) {
                Account[] accountArr = EMPTY_ACCOUNT_ARRAY;
                return accountArr;
            }
            Account[] accountsArray = new Account[totalLength];
            totalLength = 0;
            for (Account[] accountsOfType : userAccounts.accountCache.values()) {
                System.arraycopy(accountsOfType, 0, accountsArray, totalLength, accountsOfType.length);
                totalLength += accountsOfType.length;
            }
            return filterAccounts(userAccounts, accountsArray, callingUid, callingPackage, includeManagedNotVisible);
        }
    }

    protected void writeUserDataIntoCacheLocked(UserAccounts accounts, Account account, String key, String value) {
        Map<String, String> userDataForAccount = (Map) accounts.userDataCache.get(account);
        if (userDataForAccount == null) {
            userDataForAccount = accounts.accountsDb.findUserExtrasForAccount(account);
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
        synchronized (accounts.dbLock) {
            synchronized (accounts.cacheLock) {
                str = accounts.accountTokenCaches.get(account, tokenType, callingPackage, pkgSigDigest);
            }
        }
        return str;
    }

    protected void writeAuthTokenIntoCacheLocked(UserAccounts accounts, Account account, String key, String value) {
        Map<String, String> authTokensForAccount = (Map) accounts.authTokenCache.get(account);
        if (authTokensForAccount == null) {
            authTokensForAccount = accounts.accountsDb.findAuthTokensByAccount(account);
            accounts.authTokenCache.put(account, authTokensForAccount);
        }
        if (value == null) {
            authTokensForAccount.remove(key);
        } else {
            authTokensForAccount.put(key, value);
        }
    }

    /* JADX WARNING: Missing block: B:9:0x0018, code:
            r2 = r5.dbLock;
     */
    /* JADX WARNING: Missing block: B:10:0x001a, code:
            monitor-enter(r2);
     */
    /* JADX WARNING: Missing block: B:12:?, code:
            r3 = r5.cacheLock;
     */
    /* JADX WARNING: Missing block: B:13:0x001d, code:
            monitor-enter(r3);
     */
    /* JADX WARNING: Missing block: B:15:?, code:
            r0 = (java.util.Map) com.android.server.accounts.AccountManagerService.UserAccounts.-get1(r5).get(r6);
     */
    /* JADX WARNING: Missing block: B:16:0x0028, code:
            if (r0 != null) goto L_0x0037;
     */
    /* JADX WARNING: Missing block: B:17:0x002a, code:
            r0 = r5.accountsDb.findAuthTokensByAccount(r6);
            com.android.server.accounts.AccountManagerService.UserAccounts.-get1(r5).put(r6, r0);
     */
    /* JADX WARNING: Missing block: B:18:0x0037, code:
            r1 = (java.lang.String) r0.get(r7);
     */
    /* JADX WARNING: Missing block: B:20:?, code:
            monitor-exit(r3);
     */
    /* JADX WARNING: Missing block: B:21:0x003e, code:
            monitor-exit(r2);
     */
    /* JADX WARNING: Missing block: B:22:0x003f, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected String readAuthTokenInternal(UserAccounts accounts, Account account, String authTokenType) {
        synchronized (accounts.cacheLock) {
            Map<String, String> authTokensForAccount = (Map) accounts.authTokenCache.get(account);
            if (authTokensForAccount != null) {
                String str = (String) authTokensForAccount.get(authTokenType);
                return str;
            }
        }
    }

    private String readUserDataInternal(UserAccounts accounts, Account account, String key) {
        Map<String, String> userDataForAccount;
        synchronized (accounts.cacheLock) {
            userDataForAccount = (Map) accounts.userDataCache.get(account);
        }
        if (userDataForAccount == null) {
            synchronized (accounts.dbLock) {
                synchronized (accounts.cacheLock) {
                    userDataForAccount = (Map) accounts.userDataCache.get(account);
                    if (userDataForAccount == null) {
                        userDataForAccount = accounts.accountsDb.findUserExtrasForAccount(account);
                        accounts.userDataCache.put(account, userDataForAccount);
                    }
                }
            }
        }
        return (String) userDataForAccount.get(key);
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
