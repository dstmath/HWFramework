package com.android.server.accounts;

import android.accounts.Account;
import android.accounts.AccountAndUser;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManagerInternal;
import android.accounts.AccountManagerResponse;
import android.accounts.AuthenticatorDescription;
import android.accounts.CantAddAccountActivity;
import android.accounts.ChooseAccountActivity;
import android.accounts.GrantCredentialsPermissionActivity;
import android.accounts.IAccountAuthenticator;
import android.accounts.IAccountAuthenticatorResponse;
import android.accounts.IAccountManager;
import android.accounts.IAccountManagerResponse;
import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.AppOpsManager;
import android.app.INotificationManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.app.admin.DevicePolicyManagerInternal;
import android.common.HwFrameworkFactory;
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
import android.content.pm.PackageManagerInternal;
import android.content.pm.RegisteredServicesCache;
import android.content.pm.RegisteredServicesCacheListener;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.pm.UserInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ShellCallback;
import android.os.StrictMode;
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
import com.android.internal.annotations.VisibleForTesting;
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
import com.android.server.pm.DumpState;
import com.android.server.pm.PackageManagerService;
import com.android.server.slice.SliceClientPermissions;
import com.google.android.collect.Lists;
import com.google.android.collect.Sets;
import huawei.android.security.IHwBehaviorCollectManager;
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class AccountManagerService extends IAccountManager.Stub implements RegisteredServicesCacheListener<AuthenticatorDescription> {
    private static final Intent ACCOUNTS_CHANGED_INTENT = new Intent("android.accounts.LOGIN_ACCOUNTS_CHANGED");
    private static final Account[] EMPTY_ACCOUNT_ARRAY = new Account[0];
    private static final int MESSAGE_COPY_SHARED_ACCOUNT = 4;
    private static final int MESSAGE_TIMED_OUT = 3;
    private static final String PRE_N_DATABASE_NAME = "accounts.db";
    private static final int SIGNATURE_CHECK_MATCH = 1;
    private static final int SIGNATURE_CHECK_MISMATCH = 0;
    private static final int SIGNATURE_CHECK_UID_MATCH = 2;
    private static final String TAG = "AccountManagerService";
    private static AtomicReference<AccountManagerService> sThis = new AtomicReference<>();
    /* access modifiers changed from: private */
    public final AppOpsManager mAppOpsManager;
    /* access modifiers changed from: private */
    public CopyOnWriteArrayList<AccountManagerInternal.OnAppPermissionChangeListener> mAppPermissionChangeListeners = new CopyOnWriteArrayList<>();
    /* access modifiers changed from: private */
    public final IAccountAuthenticatorCache mAuthenticatorCache;
    final Context mContext;
    /* access modifiers changed from: private */
    public final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    final MessageHandler mHandler;
    private final Injector mInjector;
    private final SparseBooleanArray mLocalUnlockedUsers = new SparseBooleanArray();
    /* access modifiers changed from: private */
    public final PackageManager mPackageManager;
    /* access modifiers changed from: private */
    public final LinkedHashMap<String, Session> mSessions = new LinkedHashMap<>();
    private UserManager mUserManager;
    /* access modifiers changed from: private */
    public final SparseArray<UserAccounts> mUsers = new SparseArray<>();

    private final class AccountManagerInternalImpl extends AccountManagerInternal {
        @GuardedBy("mLock")
        private AccountManagerBackupHelper mBackupHelper;
        private final Object mLock;

        private AccountManagerInternalImpl() {
            this.mLock = new Object();
        }

        public void requestAccountAccess(Account account, String packageName, int userId, RemoteCallback callback) {
            UserAccounts userAccounts;
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
                    int uid = AccountManagerService.this.mPackageManager.getPackageUidAsUser(packageName, userId);
                    Intent intent = AccountManagerService.this.newRequestAccountAccessIntent(account, packageName, uid, callback);
                    synchronized (AccountManagerService.this.mUsers) {
                        userAccounts = (UserAccounts) AccountManagerService.this.mUsers.get(userId);
                    }
                    SystemNotificationChannels.createAccountChannelForPackage(packageName, uid, AccountManagerService.this.mContext);
                    AccountManagerService.this.doNotification(userAccounts, account, null, intent, packageName, userId);
                } catch (PackageManager.NameNotFoundException e) {
                    Slog.e(AccountManagerService.TAG, "Unknown package " + packageName);
                }
            }
        }

        public void addOnAppPermissionChangeListener(AccountManagerInternal.OnAppPermissionChangeListener listener) {
            AccountManagerService.this.mAppPermissionChangeListeners.add(listener);
        }

        public boolean hasAccountAccess(Account account, int uid) {
            return AccountManagerService.this.hasAccountAccess(account, (String) null, uid);
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
        final /* synthetic */ AccountManagerService this$0;

        /* JADX WARNING: Illegal instructions before constructor call */
        public GetAccountsByTypeAndFeatureSession(AccountManagerService accountManagerService, UserAccounts accounts, IAccountManagerResponse response, String type, String[] features, int callingUid, String packageName, boolean includeManagedNotVisible) {
            super(r1, accounts, response, type, false, true, null, false);
            AccountManagerService accountManagerService2 = accountManagerService;
            this.this$0 = accountManagerService2;
            this.mCallingUid = callingUid;
            this.mFeatures = features;
            this.mPackageName = packageName;
            this.mIncludeManagedNotVisible = includeManagedNotVisible;
        }

        public void run() throws RemoteException {
            this.mAccountsOfType = this.this$0.getAccountsFromCache(this.mAccounts, this.mAccountType, this.mCallingUid, this.mPackageName, this.mIncludeManagedNotVisible);
            this.mAccountsWithFeatures = new ArrayList<>(this.mAccountsOfType.length);
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
                        accounts[i] = this.mAccountsWithFeatures.get(i);
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

        /* access modifiers changed from: protected */
        public String toDebugString(long now) {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toDebugString(now));
            sb.append(", getAccountsByTypeAndFeatures, ");
            sb.append(this.mFeatures != null ? TextUtils.join(",", this.mFeatures) : null);
            return sb.toString();
        }
    }

    @VisibleForTesting
    static class Injector {
        private final Context mContext;

        public Injector(Context context) {
            this.mContext = context;
        }

        /* access modifiers changed from: package-private */
        public Looper getMessageHandlerLooper() {
            ServiceThread serviceThread = new ServiceThread(AccountManagerService.TAG, -2, true);
            serviceThread.start();
            return serviceThread.getLooper();
        }

        /* access modifiers changed from: package-private */
        public Context getContext() {
            return this.mContext;
        }

        /* access modifiers changed from: package-private */
        public void addLocalService(AccountManagerInternal service) {
            LocalServices.addService(AccountManagerInternal.class, service);
        }

        /* access modifiers changed from: package-private */
        public String getDeDatabaseName(int userId) {
            return new File(Environment.getDataSystemDeDirectory(userId), "accounts_de.db").getPath();
        }

        /* access modifiers changed from: package-private */
        public String getCeDatabaseName(int userId) {
            return new File(Environment.getDataSystemCeDirectory(userId), "accounts_ce.db").getPath();
        }

        /* access modifiers changed from: package-private */
        public String getPreNDatabaseName(int userId) {
            File systemDir = Environment.getDataSystemDirectory();
            File databaseFile = new File(Environment.getUserSystemDirectory(userId), AccountManagerService.PRE_N_DATABASE_NAME);
            if (userId == 0) {
                File oldFile = new File(systemDir, AccountManagerService.PRE_N_DATABASE_NAME);
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

        /* access modifiers changed from: package-private */
        public IAccountAuthenticatorCache getAccountAuthenticatorCache() {
            return new AccountAuthenticatorCache(this.mContext);
        }

        /* access modifiers changed from: package-private */
        public INotificationManager getNotificationManager() {
            return NotificationManager.getService();
        }
    }

    public static class Lifecycle extends SystemService {
        private AccountManagerService mService;

        public Lifecycle(Context context) {
            super(context);
        }

        /* JADX WARNING: type inference failed for: r1v1, types: [com.android.server.accounts.AccountManagerService, android.os.IBinder] */
        public void onStart() {
            this.mService = new AccountManagerService(new Injector(getContext()));
            publishBinderService("account", this.mService);
        }

        public void onUnlockUser(int userHandle) {
            this.mService.onUnlockUser(userHandle);
        }

        public void onStopUser(int userHandle) {
            Slog.i(AccountManagerService.TAG, "onStopUser " + userHandle);
            this.mService.purgeUserData(userHandle);
        }
    }

    class MessageHandler extends Handler {
        MessageHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 3:
                    ((Session) msg.obj).onTimedOut();
                    return;
                case 4:
                    AccountManagerService.this.copyAccountToUser(null, (Account) msg.obj, msg.arg1, msg.arg2);
                    return;
                default:
                    throw new IllegalStateException("unhandled message: " + msg.what);
            }
        }
    }

    private static class NotificationId {
        /* access modifiers changed from: private */
        public final int mId;
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

        /* access modifiers changed from: protected */
        public String toDebugString(long now) {
            return super.toDebugString(now) + ", removeAccount, account " + this.mAccount;
        }

        public void run() throws RemoteException {
            this.mAuthenticator.getAccountRemovalAllowed(this, this.mAccount);
        }

        public void onResult(Bundle result) {
            Bundle.setDefusable(result, true);
            if (result != null && result.containsKey("booleanResult") && !result.containsKey(HwBroadcastRadarUtil.KEY_BROADCAST_INTENT)) {
                if (result.getBoolean("booleanResult")) {
                    boolean unused = AccountManagerService.this.removeAccountInternal(this.mAccounts, this.mAccount, getCallingUid());
                }
                IAccountManagerResponse response = getResponseAndClose();
                if (response != null) {
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
                    }
                    try {
                        response.onResult(result);
                    } catch (RemoteException e) {
                        Slog.e(AccountManagerService.TAG, "Error calling onResult()", e);
                    }
                }
            }
            super.onResult(result);
        }
    }

    private abstract class Session extends IAccountAuthenticatorResponse.Stub implements IBinder.DeathRecipient, ServiceConnection {
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

        public Session(AccountManagerService accountManagerService, UserAccounts accounts, IAccountManagerResponse response, String accountType, boolean expectActivityLaunch, boolean stripAuthTokenFromResult, String accountName, boolean authDetailsRequired) {
            this(accounts, response, accountType, expectActivityLaunch, stripAuthTokenFromResult, accountName, authDetailsRequired, false);
        }

        public Session(UserAccounts accounts, IAccountManagerResponse response, String accountType, boolean expectActivityLaunch, boolean stripAuthTokenFromResult, String accountName, boolean authDetailsRequired, boolean updateLastAuthenticatedTime) {
            this.mNumResults = 0;
            this.mNumRequestContinued = 0;
            this.mNumErrors = 0;
            this.mAuthenticator = null;
            if (accountType != null) {
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
            } else {
                throw new IllegalArgumentException("accountType is null");
            }
        }

        /* access modifiers changed from: package-private */
        public IAccountManagerResponse getResponseAndClose() {
            if (this.mResponse == null) {
                return null;
            }
            IAccountManagerResponse response = this.mResponse;
            close();
            return response;
        }

        /* access modifiers changed from: protected */
        public boolean checkKeyIntent(int authUid, Intent intent) {
            Intent intent2 = intent;
            intent2.setFlags(intent.getFlags() & -196);
            long bid = Binder.clearCallingIdentity();
            try {
                ResolveInfo resolveInfo = AccountManagerService.this.mContext.getPackageManager().resolveActivityAsUser(intent2, 0, this.mAccounts.userId);
                if (resolveInfo == null) {
                    Binder.restoreCallingIdentity(bid);
                    return false;
                }
                ActivityInfo targetActivityInfo = resolveInfo.activityInfo;
                int targetUid = targetActivityInfo.applicationInfo.uid;
                PackageManagerInternal pmi = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
                if (!isExportedSystemActivity(targetActivityInfo)) {
                    try {
                        if (!pmi.hasSignatureCapability(targetUid, authUid, 16)) {
                            Log.e(AccountManagerService.TAG, String.format("KEY_INTENT resolved to an Activity (%s) in a package (%s) that does not share a signature with the supplying authenticator (%s).", new Object[]{targetActivityInfo.name, targetActivityInfo.packageName, this.mAccountType}));
                            Binder.restoreCallingIdentity(bid);
                            return false;
                        }
                    } catch (Throwable th) {
                        th = th;
                        Binder.restoreCallingIdentity(bid);
                        throw th;
                    }
                } else {
                    int i = authUid;
                }
                Binder.restoreCallingIdentity(bid);
                return true;
            } catch (Throwable th2) {
                th = th2;
                int i2 = authUid;
                Binder.restoreCallingIdentity(bid);
                throw th;
            }
        }

        private boolean isExportedSystemActivity(ActivityInfo activityInfo) {
            String className = activityInfo.name;
            return PackageManagerService.PLATFORM_PACKAGE_NAME.equals(activityInfo.packageName) && (GrantCredentialsPermissionActivity.class.getName().equals(className) || CantAddAccountActivity.class.getName().equals(className));
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x001e, code lost:
            r3.mResponse.asBinder().unlinkToDeath(r3, 0);
            r3.mResponse = null;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x002b, code lost:
            cancelTimeout();
            unbind();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0031, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x001c, code lost:
            if (r3.mResponse == null) goto L_0x002b;
         */
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

        /* access modifiers changed from: protected */
        public String toDebugString() {
            return toDebugString(SystemClock.elapsedRealtime());
        }

        /* access modifiers changed from: protected */
        public String toDebugString(long now) {
            StringBuilder sb = new StringBuilder();
            sb.append("Session: expectLaunch ");
            sb.append(this.mExpectActivityLaunch);
            sb.append(", connected ");
            sb.append(this.mAuthenticator != null);
            sb.append(", stats (");
            sb.append(this.mNumResults);
            sb.append(SliceClientPermissions.SliceAuthority.DELIMITER);
            sb.append(this.mNumRequestContinued);
            sb.append(SliceClientPermissions.SliceAuthority.DELIMITER);
            sb.append(this.mNumErrors);
            sb.append("), lifetime ");
            sb.append(((double) (now - this.mCreationTime)) / 1000.0d);
            return sb.toString();
        }

        /* access modifiers changed from: package-private */
        public void bind() {
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

        public void onResult(Bundle result) {
            IAccountManagerResponse response;
            boolean needUpdate = true;
            Bundle.setDefusable(result, true);
            this.mNumResults++;
            Intent intent = null;
            if (result != null) {
                boolean isSuccessfulConfirmCreds = result.getBoolean("booleanResult", false);
                boolean isSuccessfulUpdateCredsOrAddAccount = result.containsKey("authAccount") && result.containsKey("accountType");
                if (!this.mUpdateLastAuthenticatedTime || (!isSuccessfulConfirmCreds && !isSuccessfulUpdateCredsOrAddAccount)) {
                    needUpdate = false;
                }
                if (needUpdate || this.mAuthDetailsRequired) {
                    boolean accountPresent = AccountManagerService.this.isAccountPresentForCaller(this.mAccountName, this.mAccountType);
                    if (needUpdate && accountPresent) {
                        boolean unused = AccountManagerService.this.updateLastAuthenticatedTime(new Account(this.mAccountName, this.mAccountType));
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
                Intent intent2 = (Intent) result.getParcelable(HwBroadcastRadarUtil.KEY_BROADCAST_INTENT);
                intent = intent2;
                if (intent2 != null && !checkKeyIntent(Binder.getCallingUid(), intent)) {
                    onError(5, "invalid intent in bundle returned");
                    return;
                }
            }
            if (result != null && !TextUtils.isEmpty(result.getString("authtoken"))) {
                String accountName = result.getString("authAccount");
                String accountType = result.getString("accountType");
                if (!TextUtils.isEmpty(accountName) && !TextUtils.isEmpty(accountType)) {
                    AccountManagerService.this.cancelNotification(AccountManagerService.this.getSigninRequiredNotificationId(this.mAccounts, new Account(accountName, accountType)), new UserHandle(this.mAccounts.userId));
                }
            }
            if (!this.mExpectActivityLaunch || result == null || !result.containsKey(HwBroadcastRadarUtil.KEY_BROADCAST_INTENT)) {
                response = getResponseAndClose();
            } else {
                response = this.mResponse;
            }
            if (response != null) {
                if (result == null) {
                    try {
                        if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                            Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onError() on response " + response);
                        }
                        response.onError(5, "null bundle returned");
                    } catch (RemoteException e) {
                        if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                            Log.v(AccountManagerService.TAG, "failure while notifying response", e);
                        }
                    }
                } else {
                    if (this.mStripAuthTokenFromResult) {
                        result.remove("authtoken");
                    }
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
                    }
                    if (result.getInt("errorCode", -1) <= 0 || intent != null) {
                        response.onResult(result);
                    } else {
                        response.onError(result.getInt("errorCode"), result.getString("errorMessage"));
                    }
                }
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
            RegisteredServicesCache.ServiceInfo<AuthenticatorDescription> authenticatorInfo = AccountManagerService.this.mAuthenticatorCache.getServiceInfo(AuthenticatorDescription.newKey(authenticatorType), this.mAccounts.userId);
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
                int flags = 1;
                if (AccountManagerService.this.mAuthenticatorCache.getBindInstantServiceAllowed(this.mAccounts.userId)) {
                    flags = 1 | DumpState.DUMP_CHANGES;
                }
                if (AccountManagerService.this.mContext.bindServiceAsUser(intent, this, flags, UserHandle.of(this.mAccounts.userId))) {
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
        final /* synthetic */ AccountManagerService this$0;

        /* JADX WARNING: Illegal instructions before constructor call */
        public StartAccountSession(AccountManagerService accountManagerService, UserAccounts accounts, IAccountManagerResponse response, String accountType, boolean expectActivityLaunch, String accountName, boolean authDetailsRequired, boolean updateLastAuthenticationTime, boolean isPasswordForwardingAllowed) {
            super(accounts, response, accountType, expectActivityLaunch, true, accountName, authDetailsRequired, updateLastAuthenticationTime);
            AccountManagerService accountManagerService2 = accountManagerService;
            this.this$0 = accountManagerService2;
            this.mIsPasswordForwardingAllowed = isPasswordForwardingAllowed;
        }

        public void onResult(Bundle result) {
            IAccountManagerResponse response;
            Bundle.setDefusable(result, true);
            this.mNumResults++;
            Intent intent = null;
            if (result != null) {
                Intent intent2 = (Intent) result.getParcelable(HwBroadcastRadarUtil.KEY_BROADCAST_INTENT);
                intent = intent2;
                if (intent2 != null && !checkKeyIntent(Binder.getCallingUid(), intent)) {
                    onError(5, "invalid intent in bundle returned");
                    return;
                }
            }
            if (!this.mExpectActivityLaunch || result == null || !result.containsKey(HwBroadcastRadarUtil.KEY_BROADCAST_INTENT)) {
                response = getResponseAndClose();
            } else {
                response = this.mResponse;
            }
            if (response != null) {
                if (result == null) {
                    if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                        Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onError() on response " + response);
                    }
                    this.this$0.sendErrorResponse(response, 5, "null bundle returned");
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
                        if (TextUtils.isEmpty(accountType) || !this.mAccountType.equalsIgnoreCase(accountType)) {
                            Log.w(AccountManagerService.TAG, "Account type in session bundle doesn't match request.");
                        }
                        sessionBundle.putString("accountType", this.mAccountType);
                        try {
                            result.putBundle("accountSessionBundle", CryptoHelper.getInstance().encryptBundle(sessionBundle));
                        } catch (GeneralSecurityException e) {
                            if (Log.isLoggable(AccountManagerService.TAG, 3)) {
                                Log.v(AccountManagerService.TAG, "Failed to encrypt session bundle!", e);
                            }
                            this.this$0.sendErrorResponse(response, 5, "failed to encrypt session bundle");
                            return;
                        }
                    }
                    this.this$0.sendResponse(response, result);
                } else {
                    this.this$0.sendErrorResponse(response, result.getInt("errorCode"), result.getString("errorMessage"));
                }
            }
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

        /* access modifiers changed from: protected */
        public String toDebugString(long now) {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toDebugString(now));
            sb.append(", hasFeatures, ");
            sb.append(this.mAccount);
            sb.append(", ");
            sb.append(this.mFeatures != null ? TextUtils.join(",", this.mFeatures) : null);
            return sb.toString();
        }
    }

    static class UserAccounts {
        final HashMap<String, Account[]> accountCache = new LinkedHashMap();
        /* access modifiers changed from: private */
        public final TokenCache accountTokenCaches = new TokenCache();
        final AccountsDb accountsDb;
        /* access modifiers changed from: private */
        public final Map<Account, Map<String, String>> authTokenCache = new HashMap();
        final Object cacheLock = new Object();
        /* access modifiers changed from: private */
        public final HashMap<Pair<Pair<Account, String>, Integer>, NotificationId> credentialsPermissionNotificationIds = new HashMap<>();
        final Object dbLock = new Object();
        /* access modifiers changed from: private */
        public int debugDbInsertionPoint = -1;
        /* access modifiers changed from: private */
        public final Map<String, Map<String, Integer>> mReceiversForType = new HashMap();
        /* access modifiers changed from: private */
        public final HashMap<Account, AtomicReference<String>> previousNameCache = new HashMap<>();
        /* access modifiers changed from: private */
        public final HashMap<Account, NotificationId> signinRequiredNotificationIds = new HashMap<>();
        /* access modifiers changed from: private */
        public SQLiteStatement statementForLogging;
        /* access modifiers changed from: private */
        public final Map<Account, Map<String, String>> userDataCache = new HashMap();
        /* access modifiers changed from: private */
        public final int userId;
        /* access modifiers changed from: private */
        public final Map<Account, Map<String, Integer>> visibilityCache = new HashMap();

        UserAccounts(Context context, int userId2, File preNDbFile, File deDbFile) {
            this.userId = userId2;
            synchronized (this.dbLock) {
                synchronized (this.cacheLock) {
                    this.accountsDb = AccountsDb.create(context, userId2, preNDbFile, deDbFile);
                }
            }
        }
    }

    static {
        ACCOUNTS_CHANGED_INTENT.setFlags(83886080);
    }

    public static AccountManagerService getSingleton() {
        return sThis.get();
    }

    /* JADX WARNING: type inference failed for: r4v2, types: [android.app.AppOpsManager$OnOpChangedListener, com.android.server.accounts.AccountManagerService$4] */
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
        intentFilter.addDataScheme("package");
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
        injector.addLocalService(new AccountManagerInternalImpl());
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.USER_REMOVED".equals(intent.getAction())) {
                    int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                    if (userId >= 1) {
                        Slog.i(AccountManagerService.TAG, "User " + userId + " removed");
                        AccountManagerService.this.purgeUserData(userId);
                    }
                }
            }
        }, UserHandle.ALL, userFilter, null, null);
        new PackageMonitor() {
            public void onPackageAdded(String packageName, int uid) {
                AccountManagerService.this.cancelAccountAccessRequestNotificationIfNeeded(uid, true);
            }

            public void onPackageUpdateFinished(String packageName, int uid) {
                AccountManagerService.this.cancelAccountAccessRequestNotificationIfNeeded(uid, true);
            }
        }.register(this.mContext, this.mHandler.getLooper(), UserHandle.ALL, true);
        this.mAppOpsManager.startWatchingMode(62, null, new AppOpsManager.OnOpChangedInternalListener() {
            public void onOpChanged(int op, String packageName) {
                long identity;
                try {
                    int uid = AccountManagerService.this.mPackageManager.getPackageUidAsUser(packageName, ActivityManager.getCurrentUser());
                    if (AccountManagerService.this.mAppOpsManager.checkOpNoThrow(62, uid, packageName) == 0) {
                        identity = Binder.clearCallingIdentity();
                        AccountManagerService.this.cancelAccountAccessRequestNotificationIfNeeded(packageName, uid, true);
                        Binder.restoreCallingIdentity(identity);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                    throw th;
                }
            }
        });
        this.mPackageManager.addOnPermissionsChangeListener(new PackageManager.OnPermissionsChangedListener() {
            public final void onPermissionsChanged(int i) {
                AccountManagerService.lambda$new$0(AccountManagerService.this, i);
            }
        });
    }

    public static /* synthetic */ void lambda$new$0(AccountManagerService accountManagerService, int uid) {
        Throwable th;
        String[] packageNames = accountManagerService.mPackageManager.getPackagesForUid(uid);
        if (packageNames != null) {
            int userId = UserHandle.getUserId(uid);
            long identity = Binder.clearCallingIdentity();
            try {
                int length = packageNames.length;
                Account[] accounts = null;
                int i = 0;
                while (i < length) {
                    try {
                        String packageName = packageNames[i];
                        if (accountManagerService.mPackageManager.checkPermission("android.permission.GET_ACCOUNTS", packageName) == 0) {
                            if (accounts == null) {
                                accounts = accountManagerService.getAccountsAsUser(null, userId, PackageManagerService.PLATFORM_PACKAGE_NAME);
                                if (ArrayUtils.isEmpty(accounts)) {
                                    Binder.restoreCallingIdentity(identity);
                                    return;
                                }
                            }
                            for (Account account : accounts) {
                                accountManagerService.cancelAccountAccessRequestNotificationIfNeeded(account, uid, packageName, true);
                            }
                        }
                        i++;
                    } catch (Throwable th2) {
                        th = th2;
                        Binder.restoreCallingIdentity(identity);
                        throw th;
                    }
                }
                Binder.restoreCallingIdentity(identity);
                Account[] accountArr = accounts;
            } catch (Throwable th3) {
                th = th3;
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean getBindInstantServiceAllowed(int userId) {
        return this.mAuthenticatorCache.getBindInstantServiceAllowed(userId);
    }

    /* access modifiers changed from: package-private */
    public void setBindInstantServiceAllowed(int userId, boolean allowed) {
        this.mAuthenticatorCache.setBindInstantServiceAllowed(userId, allowed);
    }

    /* access modifiers changed from: private */
    public void cancelAccountAccessRequestNotificationIfNeeded(int uid, boolean checkAccess) {
        for (Account account : getAccountsAsUser(null, UserHandle.getUserId(uid), PackageManagerService.PLATFORM_PACKAGE_NAME)) {
            cancelAccountAccessRequestNotificationIfNeeded(account, uid, checkAccess);
        }
    }

    /* access modifiers changed from: private */
    public void cancelAccountAccessRequestNotificationIfNeeded(String packageName, int uid, boolean checkAccess) {
        for (Account account : getAccountsAsUser(null, UserHandle.getUserId(uid), PackageManagerService.PLATFORM_PACKAGE_NAME)) {
            cancelAccountAccessRequestNotificationIfNeeded(account, uid, packageName, checkAccess);
        }
    }

    /* access modifiers changed from: private */
    public void cancelAccountAccessRequestNotificationIfNeeded(Account account, int uid, boolean checkAccess) {
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
        Account account2 = account;
        Bundle bundle = extras;
        Bundle.setDefusable(bundle, true);
        int callingUid = Binder.getCallingUid();
        int userId = UserHandle.getCallingUserId();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "addAccountExplicitly: , caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        Preconditions.checkNotNull(account2, "account cannot be null");
        if (isAccountManagedByCaller(account2.type, callingUid, userId)) {
            long identityToken = clearCallingIdentity();
            try {
                return addAccountInternal(getUserAccounts(userId), account2, password, bundle, callingUid, packageToVisibility);
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            throw new SecurityException(String.format("uid %s cannot explicitly add accounts of type: %s", new Object[]{Integer.valueOf(callingUid), account2.type}));
        }
    }

    public Map<Account, Integer> getAccountsAndVisibilityForPackage(String packageName, String accountType) {
        int callingUid = Binder.getCallingUid();
        int userId = UserHandle.getCallingUserId();
        boolean isSystemUid = UserHandle.isSameApp(callingUid, 1000);
        List<String> managedTypes = getTypesForCaller(callingUid, userId, isSystemUid);
        if ((accountType == null || managedTypes.contains(accountType)) && (accountType != null || isSystemUid)) {
            if (accountType != null) {
                managedTypes = new ArrayList<>();
                managedTypes.add(accountType);
            }
            long identityToken = clearCallingIdentity();
            try {
                return getAccountsAndVisibilityForPackage(packageName, managedTypes, Integer.valueOf(callingUid), getUserAccounts(userId));
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            throw new SecurityException("getAccountsAndVisibilityForPackage() called from unauthorized uid " + callingUid + " with packageName=" + packageName);
        }
    }

    private Map<Account, Integer> getAccountsAndVisibilityForPackage(String packageName, List<String> accountTypes, Integer callingUid, UserAccounts accounts) {
        if (!packageExistsForUser(packageName, accounts.userId)) {
            Log.d(TAG, "Package not found " + packageName);
            return new LinkedHashMap();
        }
        Map<Account, Integer> result = new LinkedHashMap<>();
        for (String accountType : accountTypes) {
            synchronized (accounts.dbLock) {
                synchronized (accounts.cacheLock) {
                    Account[] accountsOfType = accounts.accountCache.get(accountType);
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

    public Map<String, Integer> getPackagesAndVisibilityForAccount(Account account) {
        Map<String, Integer> packagesAndVisibilityForAccountLocked;
        Preconditions.checkNotNull(account, "account cannot be null");
        int callingUid = Binder.getCallingUid();
        int userId = UserHandle.getCallingUserId();
        if (isAccountManagedByCaller(account.type, callingUid, userId) || isSystemUid(callingUid)) {
            long identityToken = clearCallingIdentity();
            try {
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
        Map<String, Integer> accountVisibility2 = new HashMap<>();
        accounts.visibilityCache.put(account, accountVisibility2);
        return accountVisibility2;
    }

    public int getAccountVisibility(Account account, String packageName) {
        Preconditions.checkNotNull(account, "account cannot be null");
        Preconditions.checkNotNull(packageName, "packageName cannot be null");
        int callingUid = Binder.getCallingUid();
        int userId = UserHandle.getCallingUserId();
        if (isAccountManagedByCaller(account.type, callingUid, userId) || isSystemUid(callingUid)) {
            long identityToken = clearCallingIdentity();
            try {
                UserAccounts accounts = getUserAccounts(userId);
                if ("android:accounts:key_legacy_visible".equals(packageName)) {
                    int visibility = getAccountVisibilityFromCache(account, packageName, accounts);
                    if (visibility != 0) {
                        return visibility;
                    }
                    restoreCallingIdentity(identityToken);
                    return 2;
                } else if ("android:accounts:key_legacy_not_visible".equals(packageName)) {
                    int visibility2 = getAccountVisibilityFromCache(account, packageName, accounts);
                    if (visibility2 != 0) {
                        restoreCallingIdentity(identityToken);
                        return visibility2;
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
            Integer visibility = getPackagesAndVisibilityForAccountLocked(account, accounts).get(packageName);
            intValue = visibility != null ? visibility.intValue() : 0;
        }
        return intValue;
    }

    /* access modifiers changed from: private */
    public Integer resolveAccountVisibility(Account account, String packageName, UserAccounts accounts) {
        long identityToken;
        int visibility;
        Preconditions.checkNotNull(packageName, "packageName cannot be null");
        try {
            identityToken = clearCallingIdentity();
            int uid = this.mPackageManager.getPackageUidAsUser(packageName, accounts.userId);
            restoreCallingIdentity(identityToken);
            if (UserHandle.isSameApp(uid, 1000)) {
                return 1;
            }
            int signatureCheckResult = checkPackageSignature(account.type, uid, accounts.userId);
            if (signatureCheckResult == 2) {
                return 1;
            }
            int visibility2 = getAccountVisibilityFromCache(account, packageName, accounts);
            if (visibility2 != 0) {
                return Integer.valueOf(visibility2);
            }
            boolean isPrivileged = isPermittedForPackage(packageName, uid, accounts.userId, "android.permission.GET_ACCOUNTS_PRIVILEGED");
            if (isProfileOwner(uid)) {
                return 1;
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
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "Package not found " + e.getMessage());
            return 3;
        } catch (Throwable th) {
            restoreCallingIdentity(identityToken);
            throw th;
        }
    }

    private boolean isPreOApplication(String packageName) {
        long identityToken;
        boolean z = true;
        try {
            identityToken = clearCallingIdentity();
            ApplicationInfo applicationInfo = this.mPackageManager.getApplicationInfo(packageName, 0);
            restoreCallingIdentity(identityToken);
            if (applicationInfo == null) {
                return true;
            }
            if (applicationInfo.targetSdkVersion >= 26) {
                z = false;
            }
            return z;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "Package not found " + e.getMessage());
            return true;
        } catch (Throwable th) {
            restoreCallingIdentity(identityToken);
            throw th;
        }
    }

    public boolean setAccountVisibility(Account account, String packageName, int newVisibility) {
        Preconditions.checkNotNull(account, "account cannot be null");
        Preconditions.checkNotNull(packageName, "packageName cannot be null");
        int callingUid = Binder.getCallingUid();
        int userId = UserHandle.getCallingUserId();
        if (isAccountManagedByCaller(account.type, callingUid, userId) || isSystemUid(callingUid)) {
            long identityToken = clearCallingIdentity();
            try {
                return setAccountVisibility(account, packageName, newVisibility, true, getUserAccounts(userId));
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            throw new SecurityException(String.format("uid %s cannot get secrets for accounts of type: %s", new Object[]{Integer.valueOf(callingUid), account.type}));
        }
    }

    private boolean isVisible(int visibility) {
        return visibility == 1 || visibility == 2;
    }

    private boolean setAccountVisibility(Account account, String packageName, int newVisibility, boolean notify, UserAccounts accounts) {
        List<String> accountRemovedReceivers;
        Map<String, Integer> packagesToVisibility;
        synchronized (accounts.dbLock) {
            synchronized (accounts.cacheLock) {
                if (notify) {
                    try {
                        if (isSpecialPackageKey(packageName)) {
                            packagesToVisibility = getRequestingPackages(account, accounts);
                            accountRemovedReceivers = getAccountRemovedReceivers(account, accounts);
                        } else if (!packageExistsForUser(packageName, accounts.userId)) {
                            return false;
                        } else {
                            packagesToVisibility = new HashMap<>();
                            packagesToVisibility.put(packageName, resolveAccountVisibility(account, packageName, accounts));
                            accountRemovedReceivers = new ArrayList<>();
                            if (shouldNotifyPackageOnAccountRemoval(account, packageName, accounts)) {
                                accountRemovedReceivers.add(packageName);
                            }
                        }
                    } catch (Throwable th) {
                        while (true) {
                            throw th;
                        }
                    }
                } else if (!isSpecialPackageKey(packageName) && !packageExistsForUser(packageName, accounts.userId)) {
                    return false;
                } else {
                    packagesToVisibility = Collections.emptyMap();
                    accountRemovedReceivers = Collections.emptyList();
                }
                if (!updateAccountVisibilityLocked(account, packageName, newVisibility, accounts)) {
                    return false;
                }
                if (notify) {
                    for (Map.Entry<String, Integer> packageToVisibility : packagesToVisibility.entrySet()) {
                        if (isVisible(packageToVisibility.getValue().intValue()) != isVisible(resolveAccountVisibility(account, packageName, accounts).intValue())) {
                            notifyPackage(packageToVisibility.getKey(), accounts);
                        }
                    }
                    for (String packageNameToNotify : accountRemovedReceivers) {
                        sendAccountRemovedBroadcast(account, packageNameToNotify, accounts.userId);
                    }
                    sendAccountsChangedBroadcast(accounts.userId);
                }
                return true;
            }
        }
    }

    private boolean updateAccountVisibilityLocked(Account account, String packageName, int newVisibility, UserAccounts accounts) {
        long accountId = accounts.accountsDb.findDeAccountId(account);
        if (accountId < 0) {
            return false;
        }
        StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskWrites();
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
                    receivers = new HashMap<>();
                    accounts.mReceiversForType.put(type, receivers);
                }
                Integer cnt = receivers.get(opPackageName);
                int i = 1;
                if (cnt != null) {
                    i = 1 + cnt.intValue();
                }
                receivers.put(opPackageName, Integer.valueOf(i));
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
                Integer cnt = receivers.get(opPackageName);
                if (cnt.intValue() == 1) {
                    receivers.remove(opPackageName);
                } else {
                    receivers.put(opPackageName, Integer.valueOf(cnt.intValue() - 1));
                }
            }
        }
    }

    private void sendNotificationAccountUpdated(Account account, UserAccounts accounts) {
        for (Map.Entry<String, Integer> packageToVisibility : getRequestingPackages(account, accounts).entrySet()) {
            if (!(packageToVisibility.getValue().intValue() == 3 || packageToVisibility.getValue().intValue() == 4)) {
                notifyPackage(packageToVisibility.getKey(), accounts);
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
        Set<String> packages = new HashSet<>();
        synchronized (accounts.mReceiversForType) {
            for (String type : new String[]{account.type, null}) {
                Map<String, Integer> receivers = (Map) accounts.mReceiversForType.get(type);
                if (receivers != null) {
                    packages.addAll(receivers.keySet());
                }
            }
        }
        Map<String, Integer> result = new HashMap<>();
        for (String packageName : packages) {
            result.put(packageName, resolveAccountVisibility(account, packageName, accounts));
        }
        return result;
    }

    private List<String> getAccountRemovedReceivers(Account account, UserAccounts accounts) {
        Intent intent = new Intent("android.accounts.action.ACCOUNT_REMOVED");
        intent.setFlags(DumpState.DUMP_SERVICE_PERMISSIONS);
        List<ResolveInfo> receivers = this.mPackageManager.queryBroadcastReceiversAsUser(intent, 0, accounts.userId);
        List<String> result = new ArrayList<>();
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
        int visibility = resolveAccountVisibility(account, packageName, accounts).intValue();
        boolean z = true;
        if (visibility != 1 && visibility != 2) {
            return false;
        }
        Intent intent = new Intent("android.accounts.action.ACCOUNT_REMOVED");
        intent.setFlags(DumpState.DUMP_SERVICE_PERMISSIONS);
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
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        } catch (Throwable th) {
            restoreCallingIdentity(identityToken);
            throw th;
        }
    }

    private boolean isSpecialPackageKey(String packageName) {
        return "android:accounts:key_legacy_visible".equals(packageName) || "android:accounts:key_legacy_not_visible".equals(packageName);
    }

    private void sendAccountsChangedBroadcast(int userId) {
        Log.i(TAG, "the accounts changed, sending broadcast of " + ACCOUNTS_CHANGED_INTENT.getAction());
        this.mContext.sendBroadcastAsUser(ACCOUNTS_CHANGED_INTENT, new UserHandle(userId));
    }

    private void sendAccountRemovedBroadcast(Account account, String packageName, int userId) {
        Intent intent = new Intent("android.accounts.action.ACCOUNT_REMOVED");
        intent.setFlags(DumpState.DUMP_SERVICE_PERMISSIONS);
        intent.setPackage(packageName);
        intent.putExtra("authAccount", account.name);
        intent.putExtra("accountType", account.type);
        this.mContext.sendBroadcastAsUser(intent, new UserHandle(userId));
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        try {
            return AccountManagerService.super.onTransact(code, data, reply, flags);
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

    /* JADX WARNING: Removed duplicated region for block: B:128:0x0368 A[SYNTHETIC, Splitter:B:128:0x0368] */
    /* JADX WARNING: Removed duplicated region for block: B:136:0x0373 A[SYNTHETIC, Splitter:B:136:0x0373] */
    private void validateAccountsInternal(UserAccounts accounts, boolean invalidateAuthenticatorCache) {
        int access$800;
        Map<String, Integer> metaAuthUid;
        boolean userUnlocked;
        HashMap<String, Integer> knownAuth;
        HashSet<String> obsoleteAuthType;
        Map<Long, Account> accountsMap;
        HashMap<String, ArrayList<String>> accountNamesByType;
        AccountsDb accountsDb;
        AccountsDb accountsDb2;
        Account account;
        List<String> accountRemovedReceivers;
        HashMap<String, ArrayList<String>> accountNamesByType2;
        AccountsDb accountsDb3;
        Map<String, Integer> packagesToVisibility;
        SparseBooleanArray knownUids;
        UserAccounts userAccounts = accounts;
        if (Log.isLoggable(TAG, 3)) {
            Log.d(TAG, "validateAccountsInternal " + accounts.userId + " isCeDatabaseAttached=" + userAccounts.accountsDb.isCeDatabaseAttached() + " userLocked=" + this.mLocalUnlockedUsers.get(accounts.userId));
        }
        if (invalidateAuthenticatorCache) {
            this.mAuthenticatorCache.invalidateCache(accounts.userId);
        }
        HashMap<String, Integer> knownAuth2 = getAuthenticatorTypeAndUIDForUser(this.mAuthenticatorCache, accounts.userId);
        boolean userUnlocked2 = isLocalUnlockedUser(accounts.userId);
        synchronized (userAccounts.dbLock) {
            try {
                synchronized (userAccounts.cacheLock) {
                    boolean accountDeleted = false;
                    try {
                        AccountsDb accountsDb4 = userAccounts.accountsDb;
                        Map<String, Integer> metaAuthUid2 = accountsDb4.findMetaAuthUid();
                        HashSet<String> obsoleteAuthType2 = Sets.newHashSet();
                        SparseBooleanArray knownUids2 = null;
                        for (Map.Entry<String, Integer> authToUidEntry : metaAuthUid2.entrySet()) {
                            try {
                                String type = authToUidEntry.getKey();
                                int uid = authToUidEntry.getValue().intValue();
                                Integer knownUid = knownAuth2.get(type);
                                if (knownUid != null) {
                                    Map.Entry<String, Integer> entry = authToUidEntry;
                                    if (uid == knownUid.intValue()) {
                                        knownAuth2.remove(type);
                                    }
                                }
                                if (knownUids2 == null) {
                                    knownUids = getUidsOfInstalledOrUpdatedPackagesAsUser(accounts.userId);
                                } else {
                                    knownUids = knownUids2;
                                }
                                if (!knownUids.get(uid)) {
                                    obsoleteAuthType2.add(type);
                                    accountsDb4.deleteMetaByAuthTypeAndUid(type, uid);
                                }
                                knownUids2 = knownUids;
                            } catch (Throwable th) {
                                th = th;
                                HashMap<String, Integer> hashMap = knownAuth2;
                                boolean z = userUnlocked2;
                            }
                        }
                        for (Map.Entry<String, Integer> entry2 : knownAuth2.entrySet()) {
                            accountsDb4.insertOrReplaceMetaAuthTypeAndUid(entry2.getKey(), entry2.getValue().intValue());
                        }
                        Map<Long, Account> accountsMap2 = accountsDb4.findAllDeAccounts();
                        try {
                            userAccounts.accountCache.clear();
                            HashMap<String, ArrayList<String>> accountNamesByType3 = new LinkedHashMap<>();
                            Iterator<Map.Entry<Long, Account>> it = accountsMap2.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry<Long, Account> accountEntry = it.next();
                                long accountId = accountEntry.getKey().longValue();
                                Account account2 = accountEntry.getValue();
                                Iterator<Map.Entry<Long, Account>> it2 = it;
                                if (obsoleteAuthType2.contains(account2.type)) {
                                    Map.Entry<Long, Account> accountEntry2 = accountEntry;
                                    StringBuilder sb = new StringBuilder();
                                    Map<Long, Account> accountsMap3 = accountsMap2;
                                    try {
                                        sb.append("deleting account because type ");
                                        sb.append(account2.type);
                                        sb.append("'s registered authenticator no longer exist.");
                                        Slog.w(TAG, sb.toString());
                                        Map<String, Integer> packagesToVisibility2 = getRequestingPackages(account2, userAccounts);
                                        List<String> accountRemovedReceivers2 = getAccountRemovedReceivers(account2, userAccounts);
                                        accountsDb4.beginTransaction();
                                        knownAuth = knownAuth2;
                                        long accountId2 = accountId;
                                        try {
                                            accountsDb4.deleteDeAccount(accountId2);
                                            if (userUnlocked2) {
                                                try {
                                                    accountsDb4.deleteCeAccount(accountId2);
                                                } catch (Throwable th2) {
                                                    th = th2;
                                                    HashMap<String, ArrayList<String>> hashMap2 = accountNamesByType3;
                                                    HashSet<String> hashSet = obsoleteAuthType2;
                                                    boolean z2 = userUnlocked2;
                                                    accountsDb2 = accountsDb4;
                                                    Map<String, Integer> map = metaAuthUid2;
                                                    Map.Entry<Long, Account> entry3 = accountEntry2;
                                                    Map<Long, Account> map2 = accountsMap3;
                                                    Account account3 = account2;
                                                    List<String> list = accountRemovedReceivers2;
                                                    Map<String, Integer> map3 = packagesToVisibility2;
                                                }
                                            }
                                            accountsDb4.setTransactionSuccessful();
                                        } catch (Throwable th3) {
                                            th = th3;
                                            HashMap<String, ArrayList<String>> hashMap3 = accountNamesByType3;
                                            HashSet<String> hashSet2 = obsoleteAuthType2;
                                            boolean z3 = userUnlocked2;
                                            accountsDb2 = accountsDb4;
                                            Map<String, Integer> map4 = metaAuthUid2;
                                            Map.Entry<Long, Account> entry4 = accountEntry2;
                                            Map<Long, Account> map5 = accountsMap3;
                                            Account account4 = account2;
                                            List<String> list2 = accountRemovedReceivers2;
                                            Map<String, Integer> map6 = packagesToVisibility2;
                                            try {
                                                accountsDb2.endTransaction();
                                                throw th;
                                            } catch (SQLiteDiskIOException e) {
                                                ex = e;
                                                try {
                                                    Log.w(TAG, "validateAccountsInternal ret got err:", ex);
                                                    if (accountDeleted) {
                                                    }
                                                } catch (Throwable th4) {
                                                    th = th4;
                                                    if (accountDeleted) {
                                                    }
                                                    throw th;
                                                }
                                            }
                                        }
                                        try {
                                            accountsDb4.endTransaction();
                                            try {
                                                userUnlocked = userUnlocked2;
                                                account = account2;
                                                metaAuthUid = metaAuthUid2;
                                                Map.Entry<Long, Account> entry5 = accountEntry2;
                                                accountRemovedReceivers = accountRemovedReceivers2;
                                                accountNamesByType2 = accountNamesByType3;
                                                accountsDb3 = accountsDb4;
                                                accountsMap = accountsMap3;
                                                packagesToVisibility = packagesToVisibility2;
                                                obsoleteAuthType = obsoleteAuthType2;
                                            } catch (SQLiteDiskIOException e2) {
                                                ex = e2;
                                                HashSet<String> hashSet3 = obsoleteAuthType2;
                                                boolean z4 = userUnlocked2;
                                                Map<String, Integer> map7 = metaAuthUid2;
                                                Map<Long, Account> map8 = accountsMap3;
                                                AccountsDb accountsDb5 = accountsDb4;
                                                accountDeleted = true;
                                                Log.w(TAG, "validateAccountsInternal ret got err:", ex);
                                                if (accountDeleted) {
                                                }
                                            } catch (Throwable th5) {
                                                th = th5;
                                                HashSet<String> hashSet4 = obsoleteAuthType2;
                                                boolean z5 = userUnlocked2;
                                                Map<String, Integer> map9 = metaAuthUid2;
                                                Map<Long, Account> map10 = accountsMap3;
                                                AccountsDb accountsDb6 = accountsDb4;
                                                accountDeleted = true;
                                                if (accountDeleted) {
                                                }
                                                throw th;
                                            }
                                            try {
                                                logRecord(AccountsDb.DEBUG_ACTION_AUTHENTICATOR_REMOVE, "accounts", accountId2, userAccounts);
                                                accounts.userDataCache.remove(account);
                                                accounts.authTokenCache.remove(account);
                                                accounts.accountTokenCaches.remove(account);
                                                accounts.visibilityCache.remove(account);
                                                for (Map.Entry<String, Integer> packageToVisibility : packagesToVisibility.entrySet()) {
                                                    if (isVisible(packageToVisibility.getValue().intValue())) {
                                                        notifyPackage(packageToVisibility.getKey(), userAccounts);
                                                    }
                                                }
                                                for (String packageName : accountRemovedReceivers) {
                                                    sendAccountRemovedBroadcast(account, packageName, accounts.userId);
                                                }
                                                accountDeleted = true;
                                                accountNamesByType = accountNamesByType2;
                                                accountsDb = accountsDb3;
                                            } catch (SQLiteDiskIOException e3) {
                                                ex = e3;
                                                accountDeleted = true;
                                                AccountsDb accountsDb7 = accountsDb3;
                                                Log.w(TAG, "validateAccountsInternal ret got err:", ex);
                                                if (accountDeleted) {
                                                }
                                            } catch (Throwable th6) {
                                                th = th6;
                                                accountDeleted = true;
                                                AccountsDb accountsDb8 = accountsDb3;
                                                if (accountDeleted) {
                                                }
                                                throw th;
                                            }
                                        } catch (SQLiteDiskIOException e4) {
                                            ex = e4;
                                            HashSet<String> hashSet5 = obsoleteAuthType2;
                                            boolean z6 = userUnlocked2;
                                            Map<String, Integer> map11 = metaAuthUid2;
                                            Map<Long, Account> map12 = accountsMap3;
                                            AccountsDb accountsDb9 = accountsDb4;
                                            Log.w(TAG, "validateAccountsInternal ret got err:", ex);
                                            if (accountDeleted) {
                                                access$800 = accounts.userId;
                                                sendAccountsChangedBroadcast(access$800);
                                            }
                                        } catch (Throwable th7) {
                                            th = th7;
                                            HashSet<String> hashSet6 = obsoleteAuthType2;
                                            boolean z7 = userUnlocked2;
                                            Map<String, Integer> map13 = metaAuthUid2;
                                            Map<Long, Account> map14 = accountsMap3;
                                            AccountsDb accountsDb10 = accountsDb4;
                                            if (accountDeleted) {
                                                sendAccountsChangedBroadcast(accounts.userId);
                                            }
                                            throw th;
                                        }
                                    } catch (SQLiteDiskIOException e5) {
                                        ex = e5;
                                        HashSet<String> hashSet7 = obsoleteAuthType2;
                                        HashMap<String, Integer> hashMap4 = knownAuth2;
                                        boolean z8 = userUnlocked2;
                                        AccountsDb accountsDb11 = accountsDb4;
                                        Map<String, Integer> map15 = metaAuthUid2;
                                        Map<Long, Account> map16 = accountsMap3;
                                        Log.w(TAG, "validateAccountsInternal ret got err:", ex);
                                        if (accountDeleted) {
                                        }
                                    } catch (Throwable th8) {
                                        th = th8;
                                        HashSet<String> hashSet8 = obsoleteAuthType2;
                                        HashMap<String, Integer> hashMap5 = knownAuth2;
                                        boolean z9 = userUnlocked2;
                                        AccountsDb accountsDb12 = accountsDb4;
                                        Map<String, Integer> map17 = metaAuthUid2;
                                        Map<Long, Account> map18 = accountsMap3;
                                        if (accountDeleted) {
                                        }
                                        throw th;
                                    }
                                } else {
                                    accountsMap = accountsMap2;
                                    knownAuth = knownAuth2;
                                    userUnlocked = userUnlocked2;
                                    metaAuthUid = metaAuthUid2;
                                    long j = accountId;
                                    Account account5 = account2;
                                    Map.Entry<Long, Account> entry6 = accountEntry;
                                    obsoleteAuthType = obsoleteAuthType2;
                                    accountsDb = accountsDb4;
                                    accountNamesByType = accountNamesByType3;
                                    ArrayList<String> accountNames = accountNamesByType.get(account5.type);
                                    if (accountNames == null) {
                                        accountNames = new ArrayList<>();
                                        accountNamesByType.put(account5.type, accountNames);
                                    }
                                    accountNames.add(account5.name);
                                }
                                accountsDb4 = accountsDb;
                                accountNamesByType3 = accountNamesByType;
                                accountsMap2 = accountsMap;
                                obsoleteAuthType2 = obsoleteAuthType;
                                it = it2;
                                knownAuth2 = knownAuth;
                                userUnlocked2 = userUnlocked;
                                metaAuthUid2 = metaAuthUid;
                            }
                            HashSet<String> hashSet9 = obsoleteAuthType2;
                            HashMap<String, Integer> hashMap6 = knownAuth2;
                            boolean z10 = userUnlocked2;
                            AccountsDb accountsDb13 = accountsDb4;
                            Map<String, Integer> map19 = metaAuthUid2;
                            for (Map.Entry<String, ArrayList<String>> cur : accountNamesByType3.entrySet()) {
                                String accountType = cur.getKey();
                                ArrayList<String> accountNames2 = cur.getValue();
                                Account[] accountsForType = new Account[accountNames2.size()];
                                for (int i = 0; i < accountsForType.length; i++) {
                                    accountsForType[i] = new Account(accountNames2.get(i), accountType, UUID.randomUUID().toString());
                                }
                                userAccounts.accountCache.put(accountType, accountsForType);
                            }
                            accounts.visibilityCache.putAll(accountsDb13.findAllVisibilityValues());
                            if (accountDeleted) {
                                access$800 = accounts.userId;
                                sendAccountsChangedBroadcast(access$800);
                            }
                        } catch (SQLiteDiskIOException e6) {
                            ex = e6;
                            Map<Long, Account> map20 = accountsMap2;
                            HashSet<String> hashSet10 = obsoleteAuthType2;
                            HashMap<String, Integer> hashMap7 = knownAuth2;
                            boolean z11 = userUnlocked2;
                            AccountsDb accountsDb14 = accountsDb4;
                            Map<String, Integer> map21 = metaAuthUid2;
                            Log.w(TAG, "validateAccountsInternal ret got err:", ex);
                            if (accountDeleted) {
                            }
                        } catch (Throwable th9) {
                            th = th9;
                            Map<Long, Account> map22 = accountsMap2;
                            HashSet<String> hashSet11 = obsoleteAuthType2;
                            HashMap<String, Integer> hashMap8 = knownAuth2;
                            boolean z12 = userUnlocked2;
                            AccountsDb accountsDb15 = accountsDb4;
                            Map<String, Integer> map23 = metaAuthUid2;
                            if (accountDeleted) {
                            }
                            throw th;
                        }
                    } catch (Throwable th10) {
                        th = th10;
                        throw th;
                    }
                }
            } catch (Throwable th11) {
                th = th11;
                throw th;
            }
        }
    }

    private SparseBooleanArray getUidsOfInstalledOrUpdatedPackagesAsUser(int userId) {
        List<PackageInfo> pkgsWithData = this.mPackageManager.getInstalledPackagesAsUser(8192, userId);
        SparseBooleanArray knownUids = new SparseBooleanArray(pkgsWithData.size());
        for (PackageInfo pkgInfo : pkgsWithData) {
            if (!(pkgInfo.applicationInfo == null || (pkgInfo.applicationInfo.flags & DumpState.DUMP_VOLUMES) == 0)) {
                knownUids.put(pkgInfo.applicationInfo.uid, true);
            }
        }
        return knownUids;
    }

    static HashMap<String, Integer> getAuthenticatorTypeAndUIDForUser(Context context, int userId) {
        return getAuthenticatorTypeAndUIDForUser((IAccountAuthenticatorCache) new AccountAuthenticatorCache(context), userId);
    }

    private static HashMap<String, Integer> getAuthenticatorTypeAndUIDForUser(IAccountAuthenticatorCache authCache, int userId) {
        HashMap<String, Integer> knownAuth = new LinkedHashMap<>();
        for (RegisteredServicesCache.ServiceInfo<AuthenticatorDescription> service : authCache.getAllServices(userId)) {
            knownAuth.put(((AuthenticatorDescription) service.type).type, Integer.valueOf(service.uid));
        }
        return knownAuth;
    }

    private UserAccounts getUserAccountsForCaller() {
        return getUserAccounts(UserHandle.getCallingUserId());
    }

    /* access modifiers changed from: protected */
    public UserAccounts getUserAccounts(int userId) {
        UserAccounts accounts;
        synchronized (this.mUsers) {
            accounts = this.mUsers.get(userId);
            boolean validateAccounts = false;
            if (accounts == null) {
                accounts = new UserAccounts(this.mContext, userId, new File(this.mInjector.getPreNDatabaseName(userId)), new File(this.mInjector.getDeDatabaseName(userId)));
                try {
                    initializeDebugDbSizeAndCompileSqlStatementForLogging(accounts);
                } catch (SQLiteException e) {
                    Log.e(TAG, "initializeDebugDbSizeAndCompileSqlStatementForLogging got err:", e);
                }
                this.mUsers.append(userId, accounts);
                purgeOldGrants(accounts);
                validateAccounts = true;
            }
            if (!accounts.accountsDb.isCeDatabaseAttached() && this.mLocalUnlockedUsers.get(userId)) {
                Log.i(TAG, "User " + userId + " is unlocked - opening CE database");
                synchronized (accounts.dbLock) {
                    synchronized (accounts.cacheLock) {
                        try {
                            accounts.accountsDb.attachCeDatabase(new File(this.mInjector.getCeDatabaseName(userId)));
                        } catch (SQLiteException e2) {
                            Log.e(TAG, "attachCeDatabase got err:", e2);
                        }
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
        try {
            List<Account> accountsToRemove = accounts.accountsDb.findCeAccountsNotInDe();
            if (!accountsToRemove.isEmpty()) {
                Slog.i(TAG, "Accounts " + accountsToRemove + " were previously deleted while user " + accounts.userId + " was locked. Removing accounts from CE tables");
                logRecord(accounts, AccountsDb.DEBUG_ACTION_SYNC_DE_CE_ACCOUNTS, "accounts");
                for (Account account : accountsToRemove) {
                    removeAccountInternal(accounts, account, 1000);
                }
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "syncDeCeAccountsLocked got err:", e);
        }
    }

    /* access modifiers changed from: private */
    public void purgeOldGrantsAll() {
        synchronized (this.mUsers) {
            for (int i = 0; i < this.mUsers.size(); i++) {
                purgeOldGrants(this.mUsers.valueAt(i));
            }
        }
    }

    private void purgeOldGrants(UserAccounts accounts) {
        synchronized (accounts.dbLock) {
            synchronized (accounts.cacheLock) {
                try {
                    for (Integer intValue : accounts.accountsDb.findAllUidGrants()) {
                        int uid = intValue.intValue();
                        if (!(this.mPackageManager.getPackagesForUid(uid) != null)) {
                            Log.d(TAG, "deleting grants for UID " + uid + " because its package is no longer installed");
                            accounts.accountsDb.deleteGrantsByUid(uid);
                        }
                    }
                } catch (SQLiteException e) {
                    Log.e(TAG, "purgeOldGrants got err:", e);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void removeVisibilityValuesForPackage(String packageName) {
        if (!isSpecialPackageKey(packageName)) {
            synchronized (this.mUsers) {
                int numberOfUsers = this.mUsers.size();
                for (int i = 0; i < numberOfUsers; i++) {
                    UserAccounts accounts = this.mUsers.valueAt(i);
                    try {
                        this.mPackageManager.getPackageUidAsUser(packageName, accounts.userId);
                    } catch (PackageManager.NameNotFoundException e) {
                        try {
                            accounts.accountsDb.deleteAccountVisibilityForPackage(packageName);
                        } catch (SQLiteException ex) {
                            Log.e(TAG, "deleteAccountVisibilityForPackage got err:", ex);
                        }
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

    /* access modifiers changed from: private */
    public void purgeUserData(int userId) {
        UserAccounts accounts;
        synchronized (this.mUsers) {
            accounts = this.mUsers.get(userId);
            this.mUsers.remove(userId);
            this.mLocalUnlockedUsers.delete(userId);
        }
        if (accounts != null) {
            synchronized (accounts.dbLock) {
                synchronized (accounts.cacheLock) {
                    accounts.statementForLogging.close();
                    accounts.accountsDb.close();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void onUserUnlocked(Intent intent) {
        onUnlockUser(intent.getIntExtra("android.intent.extra.user_handle", -1));
    }

    /* access modifiers changed from: package-private */
    public void onUnlockUser(int userId) {
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "onUserUnlocked " + userId);
        }
        synchronized (this.mUsers) {
            this.mLocalUnlockedUsers.put(userId, true);
        }
        if (userId >= 1) {
            this.mHandler.post(new Runnable(userId) {
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    AccountManagerService.this.syncSharedAccounts(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void syncSharedAccounts(int userId) {
        int parentUserId;
        try {
            Account[] sharedAccounts = getSharedAccountsAsUser(userId);
            if (sharedAccounts != null) {
                if (sharedAccounts.length != 0) {
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
        } catch (SQLiteException e) {
            Slog.e(TAG, "syncSharedAccounts error userId: " + userId, e);
        }
    }

    public void onServiceChanged(AuthenticatorDescription desc, int userId, boolean removed) {
        try {
            validateAccountsInternal(getUserAccounts(userId), false);
        } catch (SQLiteException e) {
            Slog.e(TAG, "onServiceChanged userId: " + userId + ", removed: " + removed);
        }
    }

    public String getPassword(Account account) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "getPassword, caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
        }
        if (account != null) {
            int userId = UserHandle.getCallingUserId();
            if (isAccountManagedByCaller(account.type, callingUid, userId)) {
                long identityToken = clearCallingIdentity();
                try {
                    return readPasswordInternal(getUserAccounts(userId), account);
                } finally {
                    restoreCallingIdentity(identityToken);
                }
            } else {
                throw new SecurityException(String.format("uid %s cannot get secrets for accounts of type: %s", new Object[]{Integer.valueOf(callingUid), account.type}));
            }
        } else {
            throw new IllegalArgumentException("account is null");
        }
    }

    private String readPasswordInternal(UserAccounts accounts, Account account) {
        String findAccountPasswordByNameAndType;
        if (account == null) {
            return null;
        }
        if (!isLocalUnlockedUser(accounts.userId)) {
            Log.w(TAG, "Password is not available - user " + accounts.userId + " data is locked");
            return null;
        }
        synchronized (accounts.dbLock) {
            synchronized (accounts.cacheLock) {
                findAccountPasswordByNameAndType = accounts.accountsDb.findAccountPasswordByNameAndType(account.name, account.type);
            }
        }
        return findAccountPasswordByNameAndType;
    }

    public String getPreviousName(Account account) {
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "getPreviousName, caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
        }
        Preconditions.checkNotNull(account, "account cannot be null");
        int userId = UserHandle.getCallingUserId();
        long identityToken = clearCallingIdentity();
        try {
            return readPreviousNameInternal(getUserAccounts(userId), account);
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
                String str = previousNameRef.get();
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
        } else if (!isLocalUnlockedUser(userId)) {
            Log.w(TAG, "User " + userId + " data is locked. callingUid " + callingUid);
            return null;
        } else {
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
        }
    }

    public AuthenticatorDescription[] getAuthenticatorTypes(int userId) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "getAuthenticatorTypes: for user id " + userId + " caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (!isCrossUser(callingUid, userId)) {
            long identityToken = clearCallingIdentity();
            try {
                return getAuthenticatorTypesInternal(userId);
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            throw new SecurityException(String.format("User %s tying to get authenticator types for %s", new Object[]{Integer.valueOf(UserHandle.getCallingUserId()), Integer.valueOf(userId)}));
        }
    }

    private AuthenticatorDescription[] getAuthenticatorTypesInternal(int userId) {
        this.mAuthenticatorCache.updateServices(userId);
        Collection<RegisteredServicesCache.ServiceInfo<AuthenticatorDescription>> authenticatorCollection = this.mAuthenticatorCache.getAllServices(userId);
        AuthenticatorDescription[] types = new AuthenticatorDescription[authenticatorCollection.size()];
        int i = 0;
        for (RegisteredServicesCache.ServiceInfo<AuthenticatorDescription> authenticator : authenticatorCollection) {
            types[i] = (AuthenticatorDescription) authenticator.type;
            i++;
        }
        return types;
    }

    private boolean isCrossUser(int callingUid, int userId) {
        return (userId == UserHandle.getCallingUserId() || callingUid == 1000 || this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) ? false : true;
    }

    public boolean addAccountExplicitly(Account account, String password, Bundle extras) {
        return addAccountExplicitlyWithVisibility(account, password, extras, null);
    }

    public void copyAccountToUser(IAccountManagerResponse response, Account account, int userFrom, int userTo) {
        long identityToken;
        IAccountManagerResponse iAccountManagerResponse = response;
        Account account2 = account;
        int i = userFrom;
        int i2 = userTo;
        int callingUid = Binder.getCallingUid();
        if (!isCrossUser(callingUid, -1)) {
            UserAccounts fromAccounts = getUserAccounts(i);
            UserAccounts toAccounts = getUserAccounts(i2);
            if (fromAccounts == null) {
            } else if (toAccounts == null) {
                int i3 = callingUid;
            } else {
                Slog.d(TAG, "Copying account  from user " + i + " to user " + i2);
                long identityToken2 = clearCallingIdentity();
                try {
                    String str = account2.type;
                    r1 = r1;
                    UserAccounts userAccounts = fromAccounts;
                    String str2 = account2.name;
                    long identityToken3 = identityToken2;
                    int i4 = callingUid;
                    final Account account3 = account2;
                    final IAccountManagerResponse iAccountManagerResponse2 = iAccountManagerResponse;
                    final UserAccounts userAccounts2 = toAccounts;
                    final int i5 = userFrom;
                    try {
                        AnonymousClass5 r1 = new Session(userAccounts, iAccountManagerResponse, str, false, false, str2, false) {
                            /* access modifiers changed from: protected */
                            public String toDebugString(long now) {
                                return super.toDebugString(now) + ", getAccountCredentialsForClone, " + account3.type;
                            }

                            public void run() throws RemoteException {
                                this.mAuthenticator.getAccountCredentialsForCloning(this, account3);
                            }

                            public void onResult(Bundle result) {
                                Bundle.setDefusable(result, true);
                                if (result == null || !result.getBoolean("booleanResult", false)) {
                                    super.onResult(result);
                                    return;
                                }
                                AccountManagerService.this.completeCloningAccount(iAccountManagerResponse2, result, account3, userAccounts2, i5);
                            }
                        };
                        r1.bind();
                        restoreCallingIdentity(identityToken3);
                        return;
                    } catch (Throwable th) {
                        th = th;
                        identityToken = identityToken3;
                        restoreCallingIdentity(identityToken);
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    identityToken = identityToken2;
                    int i6 = callingUid;
                    restoreCallingIdentity(identityToken);
                    throw th;
                }
            }
            if (iAccountManagerResponse != null) {
                Bundle result = new Bundle();
                result.putBoolean("booleanResult", false);
                try {
                    iAccountManagerResponse.onResult(result);
                } catch (RemoteException e) {
                    RemoteException remoteException = e;
                    Slog.w(TAG, "Failed to report error back to the client." + e);
                }
            }
            return;
        }
        throw new SecurityException("Calling copyAccountToUser requires android.permission.INTERACT_ACROSS_USERS_FULL");
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
        } else if (!canUserModifyAccounts(userId, callingUid) || !canUserModifyAccountsForType(userId, account.type, callingUid)) {
            return false;
        } else {
            long identityToken = clearCallingIdentity();
            try {
                UserAccounts userAccounts = getUserAccounts(userId);
                return updateLastAuthenticatedTime(account);
            } finally {
                restoreCallingIdentity(identityToken);
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean updateLastAuthenticatedTime(Account account) {
        boolean updateAccountLastAuthenticatedTime;
        UserAccounts accounts = getUserAccountsForCaller();
        synchronized (accounts.dbLock) {
            synchronized (accounts.cacheLock) {
                updateAccountLastAuthenticatedTime = accounts.accountsDb.updateAccountLastAuthenticatedTime(account);
            }
        }
        return updateAccountLastAuthenticatedTime;
    }

    /* access modifiers changed from: private */
    public void completeCloningAccount(IAccountManagerResponse response, Bundle accountCredentials, Account account, UserAccounts targetUser, int parentUserId) {
        long id;
        Account account2 = account;
        Bundle bundle = accountCredentials;
        Bundle.setDefusable(bundle, true);
        long id2 = clearCallingIdentity();
        try {
            r1 = r1;
            final Account account3 = account2;
            long id3 = id2;
            final int i = parentUserId;
            final Bundle bundle2 = bundle;
            try {
                AnonymousClass6 r1 = new Session(targetUser, response, account2.type, false, false, account2.name, false) {
                    /* access modifiers changed from: protected */
                    public String toDebugString(long now) {
                        return super.toDebugString(now) + ", getAccountCredentialsForClone, " + account3.type;
                    }

                    public void run() throws RemoteException {
                        for (Account acc : AccountManagerService.this.getAccounts(i, AccountManagerService.this.mContext.getOpPackageName())) {
                            if (acc.equals(account3)) {
                                this.mAuthenticator.addAccountFromCredentials(this, account3, bundle2);
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
                };
                r1.bind();
                restoreCallingIdentity(id3);
            } catch (Throwable th) {
                th = th;
                id = id3;
                restoreCallingIdentity(id);
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            id = id2;
            restoreCallingIdentity(id);
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:79:0x01cb, code lost:
        if (getUserManager().getUserInfo(com.android.server.accounts.AccountManagerService.UserAccounts.access$800(r20)).canHaveProfile() == false) goto L_0x01d4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x01cd, code lost:
        addAccountToLinkedRestrictedUsers(r10, com.android.server.accounts.AccountManagerService.UserAccounts.access$800(r20));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x01d4, code lost:
        sendNotificationAccountUpdated(r10, r9);
        sendAccountsChangedBroadcast(com.android.server.accounts.AccountManagerService.UserAccounts.access$800(r20));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x01df, code lost:
        return true;
     */
    private boolean addAccountInternal(UserAccounts accounts, Account account, String password, Bundle extras, int callingUid, Map<String, Integer> packageToVisibility) {
        UserAccounts userAccounts = accounts;
        Account account2 = account;
        Bundle bundle = extras;
        int i = callingUid;
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(i, 0, IHwBehaviorCollectManager.BehaviorId.ACCOUNTMANAGER_ADDACCOUNTINTERNAL);
        Bundle.setDefusable(bundle, true);
        if (account2 == null) {
            return false;
        }
        if (!isLocalUnlockedUser(accounts.userId)) {
            Log.w(TAG, "Account " + account2 + " cannot be added - user " + accounts.userId + " is locked. callingUid=" + i);
            return false;
        }
        synchronized (userAccounts.dbLock) {
            try {
                synchronized (userAccounts.cacheLock) {
                    try {
                        userAccounts.accountsDb.beginTransaction();
                        try {
                            if (userAccounts.accountsDb.findCeAccountId(account2) >= 0) {
                                Log.w(TAG, "insertAccountIntoDatabase: " + account2 + ", skipping since the account already exists");
                                userAccounts.accountsDb.endTransaction();
                                return false;
                            }
                            try {
                                long accountId = userAccounts.accountsDb.insertCeAccount(account2, password);
                                if (accountId < 0) {
                                    Log.w(TAG, "insertAccountIntoDatabase: " + account2 + ", skipping the DB insert failed");
                                    userAccounts.accountsDb.endTransaction();
                                    return false;
                                }
                                Log.e(TAG, "insert CE accountId = " + accountId);
                                if (userAccounts.accountsDb.insertDeAccount(account2, accountId) < 0) {
                                    Log.w(TAG, "insertAccountIntoDatabase: " + account2 + ", skipping the DB insert failed");
                                    userAccounts.accountsDb.endTransaction();
                                    return false;
                                }
                                Log.e(TAG, "insert DE accountId = " + accountId);
                                if (bundle != null) {
                                    for (String key : extras.keySet()) {
                                        if (userAccounts.accountsDb.insertExtra(accountId, key, bundle.getString(key)) < 0) {
                                            Log.w(TAG, "insertAccountIntoDatabase: " + account2 + ", skipping since insertExtra failed for key " + key);
                                            userAccounts.accountsDb.endTransaction();
                                            return false;
                                        }
                                    }
                                }
                                if (packageToVisibility != null) {
                                    for (Map.Entry next : packageToVisibility.entrySet()) {
                                        setAccountVisibility(account2, (String) next.getKey(), ((Integer) next.getValue()).intValue(), false, userAccounts);
                                        accountId = accountId;
                                    }
                                }
                                userAccounts.accountsDb.setTransactionSuccessful();
                                logRecord(AccountsDb.DEBUG_ACTION_ACCOUNT_ADD, "accounts", accountId, userAccounts, i);
                                insertAccountIntoCacheLocked(accounts, account);
                                userAccounts.accountsDb.endTransaction();
                            } catch (Throwable th) {
                                th = th;
                                userAccounts.accountsDb.endTransaction();
                                throw th;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            String str = password;
                            userAccounts.accountsDb.endTransaction();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                throw th;
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isLocalUnlockedUser(int userId) {
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
        int callingUid = Binder.getCallingUid();
        this.mAppOpsManager.checkPackage(callingUid, opPackageName);
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "hasFeatures, response " + response + ", features " + Arrays.toString(features) + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        boolean z = false;
        Preconditions.checkArgument(account != null, "account cannot be null");
        Preconditions.checkArgument(response != null, "response cannot be null");
        if (features != null) {
            z = true;
        }
        Preconditions.checkArgument(z, "features cannot be null");
        int userId = UserHandle.getCallingUserId();
        checkReadAccountsPermitted(callingUid, account.type, userId, opPackageName);
        long identityToken = clearCallingIdentity();
        try {
            TestFeaturesSession testFeaturesSession = new TestFeaturesSession(getUserAccounts(userId), response, account, features);
            testFeaturesSession.bind();
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    public void renameAccount(IAccountManagerResponse response, Account accountToRename, String newName) {
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "renameAccount, caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (accountToRename != null) {
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
                    throw th;
                }
                restoreCallingIdentity(identityToken);
                return;
            }
            throw new SecurityException(String.format("uid %s cannot rename accounts of type: %s", new Object[]{Integer.valueOf(callingUid), accountToRename.type}));
        }
        throw new IllegalArgumentException("account is null");
    }

    private Account renameAccountInternal(UserAccounts accounts, Account accountToRename, String newName) {
        Account renamedAccount;
        UserAccounts userAccounts = accounts;
        Account account = accountToRename;
        String str = newName;
        cancelNotification(getSigninRequiredNotificationId(accounts, accountToRename), new UserHandle(accounts.userId));
        synchronized (accounts.credentialsPermissionNotificationIds) {
            for (Pair<Pair<Account, String>, Integer> pair : accounts.credentialsPermissionNotificationIds.keySet()) {
                if (account.equals(((Pair) pair.first).first)) {
                    cancelNotification((NotificationId) accounts.credentialsPermissionNotificationIds.get(pair), new UserHandle(accounts.userId));
                }
            }
        }
        synchronized (userAccounts.dbLock) {
            synchronized (userAccounts.cacheLock) {
                List<String> accountRemovedReceivers = getAccountRemovedReceivers(account, userAccounts);
                userAccounts.accountsDb.beginTransaction();
                Account renamedAccount2 = new Account(str, account.type);
                try {
                    if (userAccounts.accountsDb.findCeAccountId(renamedAccount2) >= 0) {
                        Log.e(TAG, "renameAccount failed - account with new name already exists");
                        return null;
                    }
                    long accountId = userAccounts.accountsDb.findDeAccountId(account);
                    if (accountId >= 0) {
                        userAccounts.accountsDb.renameCeAccount(accountId, str);
                        if (userAccounts.accountsDb.renameDeAccount(accountId, str, account.name)) {
                            userAccounts.accountsDb.setTransactionSuccessful();
                            userAccounts.accountsDb.endTransaction();
                            Account renamedAccount3 = insertAccountIntoCacheLocked(userAccounts, renamedAccount2);
                            removeAccountFromCacheLocked(accounts, accountToRename);
                            accounts.userDataCache.put(renamedAccount3, (Map) accounts.userDataCache.get(account));
                            accounts.authTokenCache.put(renamedAccount3, (Map) accounts.authTokenCache.get(account));
                            accounts.visibilityCache.put(renamedAccount3, (Map) accounts.visibilityCache.get(account));
                            accounts.previousNameCache.put(renamedAccount3, new AtomicReference(account.name));
                            Account resultAccount = renamedAccount3;
                            int parentUserId = accounts.userId;
                            if (canHaveProfile(parentUserId)) {
                                for (UserInfo user : getUserManager().getUsers(true)) {
                                    if (user.isRestricted()) {
                                        renamedAccount = renamedAccount3;
                                        if (user.restrictedProfileParentId == parentUserId) {
                                            renameSharedAccountAsUser(account, str, user.id);
                                        }
                                    } else {
                                        renamedAccount = renamedAccount3;
                                    }
                                    renamedAccount3 = renamedAccount;
                                }
                            }
                            sendNotificationAccountUpdated(resultAccount, userAccounts);
                            sendAccountsChangedBroadcast(accounts.userId);
                            for (String packageName : accountRemovedReceivers) {
                                sendAccountRemovedBroadcast(account, packageName, accounts.userId);
                            }
                            return resultAccount;
                        }
                        Log.e(TAG, "renameAccount failed");
                        userAccounts.accountsDb.endTransaction();
                        return null;
                    }
                    Log.e(TAG, "renameAccount failed - old account does not exist");
                    userAccounts.accountsDb.endTransaction();
                    return null;
                } finally {
                    userAccounts.accountsDb.endTransaction();
                }
            }
        }
    }

    private boolean canHaveProfile(int parentUserId) {
        UserInfo userInfo = getUserManager().getUserInfo(parentUserId);
        return userInfo != null && userInfo.canHaveProfile();
    }

    public void removeAccount(IAccountManagerResponse response, Account account, boolean expectActivityLaunch) {
        removeAccountAsUser(response, account, expectActivityLaunch, UserHandle.getCallingUserId());
    }

    public void removeAccountAsUser(IAccountManagerResponse response, Account account, boolean expectActivityLaunch, int userId) {
        IAccountManagerResponse iAccountManagerResponse = response;
        Account account2 = account;
        int i = userId;
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "removeAccount, response " + iAccountManagerResponse + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid() + ", for user id " + i);
        }
        Preconditions.checkArgument(account2 != null, "account cannot be null");
        Preconditions.checkArgument(iAccountManagerResponse != null, "response cannot be null");
        if (!isCrossUser(callingUid, i)) {
            UserHandle user = UserHandle.of(userId);
            if (!isAccountManagedByCaller(account2.type, callingUid, user.getIdentifier()) && !isSystemUid(callingUid) && !isProfileOwner(callingUid)) {
                throw new SecurityException(String.format("uid %s cannot remove accounts of type: %s", new Object[]{Integer.valueOf(callingUid), account2.type}));
            } else if (!canUserModifyAccounts(i, callingUid)) {
                try {
                    iAccountManagerResponse.onError(100, "User cannot modify accounts");
                } catch (RemoteException e) {
                }
            } else if (!canUserModifyAccountsForType(i, account2.type, callingUid)) {
                try {
                    iAccountManagerResponse.onError(101, "User cannot modify accounts of this type (policy).");
                } catch (RemoteException e2) {
                }
            } else {
                long identityToken = clearCallingIdentity();
                UserAccounts accounts = getUserAccounts(i);
                cancelNotification(getSigninRequiredNotificationId(accounts, account2), user);
                synchronized (accounts.credentialsPermissionNotificationIds) {
                    try {
                        for (Pair<Pair<Account, String>, Integer> pair : accounts.credentialsPermissionNotificationIds.keySet()) {
                            try {
                                if (account2.equals(((Pair) pair.first).first)) {
                                    cancelNotification((NotificationId) accounts.credentialsPermissionNotificationIds.get(pair), user);
                                }
                            } catch (Throwable th) {
                                th = th;
                                UserAccounts userAccounts = accounts;
                                while (true) {
                                    try {
                                        break;
                                    } catch (Throwable th2) {
                                        th = th2;
                                    }
                                }
                                throw th;
                            }
                        }
                        UserAccounts accounts2 = accounts;
                        logRecord(AccountsDb.DEBUG_ACTION_CALLED_ACCOUNT_REMOVE, "accounts", accounts.accountsDb.findDeAccountId(account2), accounts, callingUid);
                        try {
                            RemoveAccountSession removeAccountSession = new RemoveAccountSession(accounts2, iAccountManagerResponse, account2, expectActivityLaunch);
                            removeAccountSession.bind();
                        } finally {
                            restoreCallingIdentity(identityToken);
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        UserAccounts userAccounts2 = accounts;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
            }
        } else {
            throw new SecurityException(String.format("User %s tying remove account for %s", new Object[]{Integer.valueOf(UserHandle.getCallingUserId()), Integer.valueOf(userId)}));
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
                return removeAccountInternal(accounts, account, callingUid);
            } finally {
                restoreCallingIdentity(identityToken);
            }
        } else {
            throw new SecurityException(String.format("uid %s cannot explicitly remove accounts of type: %s", new Object[]{Integer.valueOf(callingUid), account.type}));
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void removeAccountInternal(Account account) {
        removeAccountInternal(getUserAccountsForCaller(), account, getCallingUid());
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0107, code lost:
        r1 = android.os.Binder.clearCallingIdentity();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:?, code lost:
        r3 = com.android.server.accounts.AccountManagerService.UserAccounts.access$800(r20);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0113, code lost:
        if (canHaveProfile(r3) == false) goto L_0x013d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0115, code lost:
        r4 = getUserManager().getUsers(true).iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0125, code lost:
        if (r4.hasNext() == false) goto L_0x013d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0127, code lost:
        r5 = r4.next();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0131, code lost:
        if (r5.isRestricted() == false) goto L_0x0121;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x0135, code lost:
        if (r3 != r5.restrictedProfileParentId) goto L_0x0121;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x0137, code lost:
        removeSharedAccountAsUser(r9, r5.id, r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0141, code lost:
        if (r16 == false) goto L_0x0192;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0143, code lost:
        r3 = com.android.server.accounts.AccountManagerService.UserAccounts.access$1700(r20);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0147, code lost:
        monitor-enter(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:?, code lost:
        r0 = com.android.server.accounts.AccountManagerService.UserAccounts.access$1700(r20).keySet().iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x0158, code lost:
        if (r0.hasNext() == false) goto L_0x018d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x015a, code lost:
        r4 = (android.util.Pair) r0.next();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x016a, code lost:
        if (r9.equals(((android.util.Pair) r4.first).first) == false) goto L_0x018c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x0178, code lost:
        if ("com.android.AccountManager.ACCOUNT_ACCESS_TOKEN_TYPE".equals(((android.util.Pair) r4.first).second) == false) goto L_0x018c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x017a, code lost:
        r7.mHandler.post(new com.android.server.accounts.$$Lambda$AccountManagerService$lqbNdAUKUSipmpqby9oIO8JlNTQ(r7, r9, ((java.lang.Integer) r4.second).intValue()));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x018d, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x0192, code lost:
        return r16;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x0193, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x0194, code lost:
        android.os.Binder.restoreCallingIdentity(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x0197, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x0198, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x0199, code lost:
        r1 = r16;
     */
    public boolean removeAccountInternal(UserAccounts accounts, Account account, int callingUid) {
        boolean isChanged;
        long accountId;
        String action;
        UserAccounts userAccounts = accounts;
        Account account2 = account;
        int i = callingUid;
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(i, 0, IHwBehaviorCollectManager.BehaviorId.ACCOUNTMANAGER_REMOVEACCOUNTINTERNAL);
        boolean isChanged2 = false;
        boolean userUnlocked = isLocalUnlockedUser(accounts.userId);
        if (!userUnlocked) {
            Slog.i(TAG, "Removing account " + account2 + " while user " + accounts.userId + " is still locked. CE data will be removed later");
        }
        synchronized (userAccounts.dbLock) {
            try {
                synchronized (userAccounts.cacheLock) {
                    try {
                        Map<String, Integer> packagesToVisibility = getRequestingPackages(account2, userAccounts);
                        List<String> accountRemovedReceivers = getAccountRemovedReceivers(account2, userAccounts);
                        userAccounts.accountsDb.beginTransaction();
                        try {
                            accountId = userAccounts.accountsDb.findDeAccountId(account2);
                            if (accountId >= 0) {
                                try {
                                    isChanged2 = userAccounts.accountsDb.deleteDeAccount(accountId);
                                } catch (Throwable th) {
                                    th = th;
                                    isChanged = false;
                                    userAccounts.accountsDb.endTransaction();
                                    throw th;
                                }
                            }
                            isChanged = isChanged2;
                            if (userUnlocked) {
                                try {
                                    long ceAccountId = userAccounts.accountsDb.findCeAccountId(account2);
                                    if (ceAccountId >= 0) {
                                        userAccounts.accountsDb.deleteCeAccount(ceAccountId);
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    userAccounts.accountsDb.endTransaction();
                                    throw th;
                                }
                            }
                            try {
                                userAccounts.accountsDb.setTransactionSuccessful();
                            } catch (Throwable th3) {
                                th = th3;
                                long j = accountId;
                                userAccounts.accountsDb.endTransaction();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            isChanged = false;
                            userAccounts.accountsDb.endTransaction();
                            throw th;
                        }
                        try {
                            userAccounts.accountsDb.endTransaction();
                            if (isChanged) {
                                removeAccountFromCacheLocked(accounts, account);
                                for (Map.Entry<String, Integer> packageToVisibility : packagesToVisibility.entrySet()) {
                                    if (packageToVisibility.getValue().intValue() == 1 || packageToVisibility.getValue().intValue() == 2) {
                                        notifyPackage(packageToVisibility.getKey(), userAccounts);
                                    }
                                }
                                sendAccountsChangedBroadcast(accounts.userId);
                                for (String packageName : accountRemovedReceivers) {
                                    sendAccountRemovedBroadcast(account2, packageName, accounts.userId);
                                }
                                if (userUnlocked) {
                                    action = AccountsDb.DEBUG_ACTION_ACCOUNT_REMOVE;
                                } else {
                                    action = AccountsDb.DEBUG_ACTION_ACCOUNT_REMOVE_DE;
                                }
                                long j2 = accountId;
                                logRecord(action, "accounts", accountId, userAccounts);
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            boolean z = isChanged;
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        throw th;
                    }
                }
            } catch (Throwable th7) {
                th = th7;
                throw th;
            }
        }
    }

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
                    accounts.accountsDb.endTransaction();
                    synchronized (accounts.cacheLock) {
                        for (Pair<Account, String> tokenInfo : deletedTokens) {
                            writeAuthTokenIntoCacheLocked(accounts, (Account) tokenInfo.first, (String) tokenInfo.second, null);
                        }
                        accounts.accountTokenCaches.remove(accountType, authToken);
                    }
                } catch (Throwable th) {
                    accounts.accountsDb.endTransaction();
                    throw th;
                }
            }
        } finally {
            restoreCallingIdentity(identityToken);
        }
    }

    private List<Pair<Account, String>> invalidateAuthTokenLocked(UserAccounts accounts, String accountType, String authToken) {
        List<Pair<Account, String>> results = new ArrayList<>();
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

    /* access modifiers changed from: private */
    public void saveCachedToken(UserAccounts accounts, Account account, String callerPkg, byte[] callerSigDigest, String tokenType, String token, long expiryMillis) {
        if (account == null || tokenType == null || callerPkg == null || callerSigDigest == null) {
            UserAccounts userAccounts = accounts;
            return;
        }
        cancelNotification(getSigninRequiredNotificationId(accounts, account), UserHandle.of(accounts.userId));
        UserAccounts userAccounts2 = accounts;
        synchronized (userAccounts2.cacheLock) {
            userAccounts2.accountTokenCaches.put(account, token, tokenType, callerPkg, callerSigDigest, expiryMillis);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x003f, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0069, code lost:
        return true;
     */
    public boolean saveAuthTokenToDatabase(UserAccounts accounts, Account account, String type, String authToken) {
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
                    if (0 != 0) {
                        synchronized (accounts.cacheLock) {
                            try {
                                writeAuthTokenIntoCacheLocked(accounts, account, type, authToken);
                            } catch (Throwable th) {
                                th = th;
                                throw th;
                            }
                        }
                    }
                } else {
                    accounts.accountsDb.deleteAuthtokensByAccountIdAndType(accountId, type);
                    if (accounts.accountsDb.insertAuthToken(accountId, type, authToken) >= 0) {
                        accounts.accountsDb.setTransactionSuccessful();
                        accounts.accountsDb.endTransaction();
                        if (1 != 0) {
                            synchronized (accounts.cacheLock) {
                                writeAuthTokenIntoCacheLocked(accounts, account, type, authToken);
                            }
                        }
                    } else {
                        accounts.accountsDb.endTransaction();
                        if (0 != 0) {
                            synchronized (accounts.cacheLock) {
                                try {
                                    writeAuthTokenIntoCacheLocked(accounts, account, type, authToken);
                                } catch (Throwable th2) {
                                    th = th2;
                                    throw th;
                                }
                            }
                        }
                        return false;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
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
        } else if (!isLocalUnlockedUser(userId)) {
            Log.w(TAG, "Authtoken not available - user " + userId + " data is locked. callingUid " + callingUid);
            return null;
        } else {
            long identityToken = clearCallingIdentity();
            try {
                return readAuthTokenInternal(getUserAccounts(userId), account, authTokenType);
            } finally {
                restoreCallingIdentity(identityToken);
            }
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

    /* JADX WARNING: Removed duplicated region for block: B:38:0x007e  */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:26:0x0062=Splitter:B:26:0x0062, B:35:0x0077=Splitter:B:35:0x0077} */
    private void setPasswordInternal(UserAccounts accounts, Account account, String password, int callingUid) {
        String action;
        UserAccounts userAccounts = accounts;
        Account account2 = account;
        String str = password;
        if (account2 != null) {
            boolean isChanged = false;
            synchronized (userAccounts.dbLock) {
                synchronized (userAccounts.cacheLock) {
                    userAccounts.accountsDb.beginTransaction();
                    try {
                        long accountId = userAccounts.accountsDb.findDeAccountId(account2);
                        if (accountId >= 0) {
                            userAccounts.accountsDb.updateCeAccountPassword(accountId, str);
                            userAccounts.accountsDb.deleteAuthTokensByAccountId(accountId);
                            accounts.authTokenCache.remove(account2);
                            accounts.accountTokenCaches.remove(account2);
                            userAccounts.accountsDb.setTransactionSuccessful();
                            if (str != null) {
                                try {
                                    if (password.length() != 0) {
                                        action = AccountsDb.DEBUG_ACTION_SET_PASSWORD;
                                        logRecord(action, "accounts", accountId, userAccounts, callingUid);
                                        isChanged = true;
                                    }
                                } catch (Throwable th) {
                                    th = th;
                                    isChanged = true;
                                    userAccounts.accountsDb.endTransaction();
                                    if (isChanged) {
                                    }
                                    throw th;
                                }
                            }
                            action = AccountsDb.DEBUG_ACTION_CLEAR_PASSWORD;
                            logRecord(action, "accounts", accountId, userAccounts, callingUid);
                            isChanged = true;
                        }
                        userAccounts.accountsDb.endTransaction();
                        if (isChanged) {
                            sendNotificationAccountUpdated(account2, userAccounts);
                            sendAccountsChangedBroadcast(accounts.userId);
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        userAccounts.accountsDb.endTransaction();
                        if (isChanged) {
                            sendNotificationAccountUpdated(account2, userAccounts);
                            sendAccountsChangedBroadcast(accounts.userId);
                        }
                        throw th;
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
        } else if (account != null) {
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
        } else {
            throw new IllegalArgumentException("account is null");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002f, code lost:
        return false;
     */
    private boolean accountExistsCache(UserAccounts accounts, Account account) {
        synchronized (accounts.cacheLock) {
            if (accounts.accountCache.containsKey(account.type)) {
                for (Account acc : accounts.accountCache.get(account.type)) {
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
                throw th;
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
        long identityToken;
        boolean z = false;
        Preconditions.checkArgument(accountType != null, "accountType cannot be null");
        if (authTokenType != null) {
            z = true;
        }
        Preconditions.checkArgument(z, "authTokenType cannot be null");
        int callingUid = getCallingUid();
        clearCallingIdentity();
        if (UserHandle.getAppId(callingUid) == 1000) {
            int userId = UserHandle.getUserId(callingUid);
            long identityToken2 = clearCallingIdentity();
            try {
                r2 = r2;
                long identityToken3 = identityToken2;
                final String str = accountType;
                final String str2 = authTokenType;
                try {
                    AnonymousClass7 r2 = new Session(getUserAccounts(userId), response, accountType, false, false, null, false) {
                        /* access modifiers changed from: protected */
                        public String toDebugString(long now) {
                            return super.toDebugString(now) + ", getAuthTokenLabel, " + str + ", authTokenType " + str2;
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
                    };
                    r2.bind();
                    restoreCallingIdentity(identityToken3);
                } catch (Throwable th) {
                    th = th;
                    identityToken = identityToken3;
                    restoreCallingIdentity(identityToken);
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                identityToken = identityToken2;
                restoreCallingIdentity(identityToken);
                throw th;
            }
        } else {
            throw new SecurityException("can only call from system");
        }
    }

    public void getAuthToken(IAccountManagerResponse response, Account account, String authTokenType, boolean notifyOnAuthFailure, boolean expectActivityLaunch, Bundle loginOptions) {
        RegisteredServicesCache.ServiceInfo<AuthenticatorDescription> authenticatorInfo;
        long identityToken;
        int callerUid;
        UserAccounts accounts;
        long identityToken2;
        AnonymousClass8 r7;
        long identityToken3;
        final Bundle bundle;
        final Account account2;
        final String str;
        final boolean z;
        final boolean z2;
        final int i;
        final boolean z3;
        final String str2;
        final byte[] bArr;
        final UserAccounts userAccounts;
        IAccountManagerResponse iAccountManagerResponse = response;
        Account account3 = account;
        String str3 = authTokenType;
        boolean z4 = notifyOnAuthFailure;
        Bundle bundle2 = loginOptions;
        Bundle.setDefusable(bundle2, true);
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "getAuthToken , response " + iAccountManagerResponse + ", authTokenType " + str3 + ", notifyOnAuthFailure " + z4 + ", expectActivityLaunch " + expectActivityLaunch + ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
        } else {
            boolean z5 = expectActivityLaunch;
        }
        Preconditions.checkArgument(iAccountManagerResponse != null, "response cannot be null");
        if (account3 == null) {
            try {
                Slog.w(TAG, "getAuthToken called with null account");
                iAccountManagerResponse.onError(7, "account is null");
            } catch (RemoteException e) {
                Slog.w(TAG, "Failed to report error back to the client." + e);
            }
        } else if (str3 == null) {
            Slog.w(TAG, "getAuthToken called with null authTokenType");
            iAccountManagerResponse.onError(7, "authTokenType is null");
        } else {
            RegisteredServicesCache.ServiceInfo<AuthenticatorDescription> userId = UserHandle.getCallingUserId();
            long ident = Binder.clearCallingIdentity();
            try {
                UserAccounts accounts2 = getUserAccounts(userId);
                userId = this.mAuthenticatorCache.getServiceInfo(AuthenticatorDescription.newKey(account3.type), accounts2.userId);
                boolean customTokens = authenticatorInfo != null && ((AuthenticatorDescription) authenticatorInfo.type).customTokens;
                int callerUid2 = Binder.getCallingUid();
                boolean permissionGranted = customTokens || permissionIsGranted(account3, str3, callerUid2, userId);
                String callerPkg = bundle2.getString("androidPackageName");
                long ident2 = Binder.clearCallingIdentity();
                try {
                    List<String> callerOwnedPackageNames = Arrays.asList(this.mPackageManager.getPackagesForUid(callerUid2));
                    Binder.restoreCallingIdentity(ident2);
                    if (callerPkg == null || !callerOwnedPackageNames.contains(callerPkg)) {
                        long j = ident2;
                        RegisteredServicesCache.ServiceInfo<AuthenticatorDescription> serviceInfo = authenticatorInfo;
                        UserAccounts userAccounts2 = accounts2;
                        List<String> list = callerOwnedPackageNames;
                        int i2 = userId;
                        throw new SecurityException(String.format("Uid %s is attempting to illegally masquerade as package %s!", new Object[]{Integer.valueOf(callerUid2), callerPkg}));
                    }
                    bundle2.putInt("callerUid", callerUid2);
                    int callerUid3 = callerUid2;
                    bundle2.putInt("callerPid", Binder.getCallingPid());
                    if (z4) {
                        bundle2.putBoolean("notifyOnAuthFailure", true);
                    }
                    long identityToken4 = clearCallingIdentity();
                    try {
                        String callerPkg2 = callerPkg;
                        byte[] callerPkgSigDigest = calculatePackageSignatureDigest(callerPkg);
                        if (!customTokens && permissionGranted) {
                            try {
                                String authToken = readAuthTokenInternal(accounts2, account3, str3);
                                if (authToken != null) {
                                    Bundle result = new Bundle();
                                    long ident3 = ident2;
                                    try {
                                        result.putString("authtoken", authToken);
                                        result.putString("authAccount", account3.name);
                                        result.putString("accountType", account3.type);
                                        onResult(iAccountManagerResponse, result);
                                        restoreCallingIdentity(identityToken4);
                                        return;
                                    } catch (Throwable th) {
                                        th = th;
                                        int i3 = callerUid3;
                                        identityToken = identityToken4;
                                        long j2 = ident3;
                                        RegisteredServicesCache.ServiceInfo<AuthenticatorDescription> serviceInfo2 = authenticatorInfo;
                                        UserAccounts userAccounts3 = accounts2;
                                        List<String> list2 = callerOwnedPackageNames;
                                        int i4 = userId;
                                        restoreCallingIdentity(identityToken);
                                        throw th;
                                    }
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                int i5 = callerUid3;
                                identityToken = identityToken4;
                                long j3 = ident2;
                                RegisteredServicesCache.ServiceInfo<AuthenticatorDescription> serviceInfo3 = authenticatorInfo;
                                UserAccounts userAccounts4 = accounts2;
                                List<String> list3 = callerOwnedPackageNames;
                                int i6 = userId;
                                restoreCallingIdentity(identityToken);
                                throw th;
                            }
                        }
                        long ident4 = ident2;
                        long identityToken5 = identityToken4;
                        if (customTokens) {
                            callerUid = callerUid3;
                            identityToken2 = identityToken5;
                            long j4 = ident4;
                            RegisteredServicesCache.ServiceInfo<AuthenticatorDescription> serviceInfo4 = authenticatorInfo;
                            accounts = accounts2;
                            try {
                                String token = readCachedTokenInternal(accounts2, account3, str3, callerPkg2, callerPkgSigDigest);
                                if (token != null) {
                                    if (Log.isLoggable(TAG, 2)) {
                                        Log.v(TAG, "getAuthToken: cache hit ofr custom token authenticator.");
                                    }
                                    Bundle result2 = new Bundle();
                                    result2.putString("authtoken", token);
                                    result2.putString("authAccount", account3.name);
                                    result2.putString("accountType", account3.type);
                                    onResult(iAccountManagerResponse, result2);
                                    restoreCallingIdentity(identityToken2);
                                    return;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                List<String> list4 = callerOwnedPackageNames;
                                int i7 = userId;
                                identityToken = identityToken2;
                                restoreCallingIdentity(identityToken);
                                throw th;
                            }
                        } else {
                            identityToken2 = identityToken5;
                            accounts = accounts2;
                            callerUid = callerUid3;
                            long j5 = ident4;
                            RegisteredServicesCache.ServiceInfo<AuthenticatorDescription> serviceInfo5 = authenticatorInfo;
                        }
                        try {
                            List<String> list5 = callerOwnedPackageNames;
                            r7 = r7;
                            int i8 = userId;
                            identityToken3 = identityToken2;
                            bundle = loginOptions;
                            account2 = account;
                            str = authTokenType;
                            z = notifyOnAuthFailure;
                            z2 = permissionGranted;
                            i = callerUid;
                            z3 = customTokens;
                            str2 = callerPkg2;
                            bArr = callerPkgSigDigest;
                            userAccounts = accounts;
                        } catch (Throwable th4) {
                            th = th4;
                            List<String> list6 = callerOwnedPackageNames;
                            int i9 = userId;
                            identityToken = identityToken2;
                            restoreCallingIdentity(identityToken);
                            throw th;
                        }
                        try {
                            r7 = new Session(this, accounts, iAccountManagerResponse, account3.type, expectActivityLaunch, false, account3.name, false) {
                                final /* synthetic */ AccountManagerService this$0;

                                {
                                    this.this$0 = this$0;
                                }

                                /* access modifiers changed from: protected */
                                public String toDebugString(long now) {
                                    if (bundle != null) {
                                        bundle.keySet();
                                    }
                                    return super.toDebugString(now) + ", getAuthToken, " + account2 + ", authTokenType " + str + ", loginOptions " + bundle + ", notifyOnAuthFailure " + z;
                                }

                                public void run() throws RemoteException {
                                    if (!z2) {
                                        this.mAuthenticator.getAuthTokenLabel(this, str);
                                    } else {
                                        this.mAuthenticator.getAuthToken(this, account2, str, bundle);
                                    }
                                }

                                public void onResult(Bundle result) {
                                    Bundle bundle = result;
                                    Bundle.setDefusable(bundle, true);
                                    if (bundle != null) {
                                        if (bundle.containsKey("authTokenLabelKey")) {
                                            Intent intent = this.this$0.newGrantCredentialsPermissionIntent(account2, null, i, new AccountAuthenticatorResponse(this), str, true);
                                            Bundle bundle2 = new Bundle();
                                            bundle2.putParcelable(HwBroadcastRadarUtil.KEY_BROADCAST_INTENT, intent);
                                            onResult(bundle2);
                                            return;
                                        }
                                        String authToken = bundle.getString("authtoken");
                                        if (authToken != null) {
                                            String name = bundle.getString("authAccount");
                                            String type = bundle.getString("accountType");
                                            if (TextUtils.isEmpty(type) || TextUtils.isEmpty(name)) {
                                                onError(5, "the type and name should not be empty");
                                                return;
                                            }
                                            Account resultAccount = new Account(name, type);
                                            if (!z3) {
                                                boolean unused = this.this$0.saveAuthTokenToDatabase(this.mAccounts, resultAccount, str, authToken);
                                            }
                                            long expiryMillis = bundle.getLong("android.accounts.expiry", 0);
                                            if (z3 && expiryMillis > System.currentTimeMillis()) {
                                                this.this$0.saveCachedToken(this.mAccounts, account2, str2, bArr, str, authToken, expiryMillis);
                                            }
                                        }
                                        Intent intent2 = (Intent) bundle.getParcelable(HwBroadcastRadarUtil.KEY_BROADCAST_INTENT);
                                        if (intent2 != null && z && !z3) {
                                            if (!checkKeyIntent(Binder.getCallingUid(), intent2)) {
                                                onError(5, "invalid intent in bundle returned");
                                                return;
                                            }
                                            this.this$0.doNotification(this.mAccounts, account2, bundle.getString("authFailedMessage"), intent2, PackageManagerService.PLATFORM_PACKAGE_NAME, userAccounts.userId);
                                        }
                                    }
                                    super.onResult(result);
                                }
                            };
                            r7.bind();
                            restoreCallingIdentity(identityToken3);
                        } catch (Throwable th5) {
                            th = th5;
                            identityToken = identityToken3;
                            restoreCallingIdentity(identityToken);
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        long j6 = ident2;
                        String str4 = callerPkg;
                        int i10 = callerUid3;
                        identityToken = identityToken4;
                        RegisteredServicesCache.ServiceInfo<AuthenticatorDescription> serviceInfo6 = authenticatorInfo;
                        UserAccounts userAccounts5 = accounts2;
                        List<String> list7 = callerOwnedPackageNames;
                        int i11 = userId;
                        restoreCallingIdentity(identityToken);
                        throw th;
                    }
                } catch (Throwable th7) {
                    RegisteredServicesCache.ServiceInfo<AuthenticatorDescription> serviceInfo7 = authenticatorInfo;
                    UserAccounts userAccounts6 = accounts2;
                    String str5 = callerPkg;
                    int i12 = userId;
                    Binder.restoreCallingIdentity(ident2);
                    throw th7;
                }
            } finally {
                authenticatorInfo = userId;
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
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Could not find packageinfo for: " + callerPkg);
            digester = null;
        }
        if (digester == null) {
            return null;
        }
        return digester.digest();
    }

    private void createNoCredentialsPermissionNotification(Account account, Intent intent, String packageName, int userId) {
        Account account2 = account;
        Intent intent2 = intent;
        int uid = intent2.getIntExtra("uid", -1);
        String authTokenType = intent2.getStringExtra("authTokenType");
        String titleAndSubtitle = this.mContext.getString(17040782, new Object[]{account2.name});
        int index = titleAndSubtitle.indexOf(10);
        String title = titleAndSubtitle;
        String subtitle = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        if (index > 0) {
            title = titleAndSubtitle.substring(0, index);
            subtitle = titleAndSubtitle.substring(index + 1);
        }
        String subtitle2 = subtitle;
        UserHandle user = UserHandle.of(userId);
        Context contextForUser = getContextForUser(user);
        Context context = contextForUser;
        installNotification(getCredentialPermissionNotificationId(account2, authTokenType, uid), new Notification.Builder(contextForUser, SystemNotificationChannels.ACCOUNT).setSmallIcon(17301642).setWhen(0).setColor(contextForUser.getColor(17170784)).setContentTitle(title).setContentText(subtitle2).setContentIntent(PendingIntent.getActivityAsUser(this.mContext, 0, intent2, 268435456, null, user)).build(), packageName, user.getIdentifier());
    }

    /* access modifiers changed from: private */
    public Intent newGrantCredentialsPermissionIntent(Account account, String packageName, int uid, AccountAuthenticatorResponse response, String authTokenType, boolean startInNewTask) {
        Intent intent = new Intent(this.mContext, GrantCredentialsPermissionActivity.class);
        if (startInNewTask) {
            intent.setFlags(268435456);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(getCredentialPermissionNotificationId(account, authTokenType, uid).mTag);
        sb.append(packageName != null ? packageName : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        intent.addCategory(sb.toString());
        intent.putExtra("account", account);
        intent.putExtra("authTokenType", authTokenType);
        intent.putExtra("response", response);
        intent.putExtra("uid", uid);
        return intent;
    }

    /* access modifiers changed from: private */
    public NotificationId getCredentialPermissionNotificationId(Account account, String authTokenType, int uid) {
        NotificationId nId;
        UserAccounts accounts = getUserAccounts(UserHandle.getUserId(uid));
        synchronized (accounts.credentialsPermissionNotificationIds) {
            Pair<Pair<Account, String>, Integer> key = new Pair<>(new Pair(account, authTokenType), Integer.valueOf(uid));
            nId = (NotificationId) accounts.credentialsPermissionNotificationIds.get(key);
            if (nId == null) {
                nId = new NotificationId("AccountManagerService:38:" + account.hashCode() + ":" + authTokenType.hashCode(), 38);
                accounts.credentialsPermissionNotificationIds.put(key, nId);
            }
        }
        return nId;
    }

    /* access modifiers changed from: private */
    public NotificationId getSigninRequiredNotificationId(UserAccounts accounts, Account account) {
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
        boolean z;
        long identityToken;
        IAccountManagerResponse iAccountManagerResponse = response;
        String str = accountType;
        Bundle bundle = optionsIn;
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.ACCOUNTMANAGER_ADDACCOUNT);
        Bundle.setDefusable(bundle, true);
        if (Log.isLoggable(TAG, 2)) {
            StringBuilder sb = new StringBuilder();
            sb.append("addAccount , response ");
            sb.append(iAccountManagerResponse);
            sb.append(", authTokenType ");
            sb.append(authTokenType);
            sb.append(", requiredFeatures ");
            sb.append(Arrays.toString(requiredFeatures));
            sb.append(", expectActivityLaunch ");
            z = expectActivityLaunch;
            sb.append(z);
            sb.append(", caller's uid ");
            sb.append(Binder.getCallingUid());
            sb.append(", pid ");
            sb.append(Binder.getCallingPid());
            Log.v(TAG, sb.toString());
        } else {
            String str2 = authTokenType;
            z = expectActivityLaunch;
        }
        if (iAccountManagerResponse == null) {
            throw new IllegalArgumentException("response is null");
        } else if (str != null) {
            int uid = Binder.getCallingUid();
            int userId = UserHandle.getUserId(uid);
            if (!canUserModifyAccounts(userId, uid)) {
                try {
                    iAccountManagerResponse.onError(100, "User is not allowed to add an account!");
                } catch (RemoteException e) {
                }
                showCantAddAccount(100, userId);
            } else if (!canUserModifyAccountsForType(userId, str, uid)) {
                try {
                    iAccountManagerResponse.onError(101, "User cannot modify accounts of this type (policy).");
                } catch (RemoteException e2) {
                }
                showCantAddAccount(101, userId);
            } else {
                int pid = Binder.getCallingPid();
                Bundle options = bundle == null ? new Bundle() : bundle;
                options.putInt("callerUid", uid);
                options.putInt("callerPid", pid);
                int usrId = UserHandle.getCallingUserId();
                long identityToken2 = clearCallingIdentity();
                try {
                    UserAccounts accounts = getUserAccounts(usrId);
                    logRecordWithUid(accounts, AccountsDb.DEBUG_ACTION_CALLED_ACCOUNT_ADD, "accounts", uid);
                    r1 = r1;
                    AnonymousClass9 r20 = r1;
                    long identityToken3 = identityToken2;
                    IAccountManagerResponse iAccountManagerResponse2 = iAccountManagerResponse;
                    int i = usrId;
                    String str3 = str;
                    int i2 = pid;
                    int i3 = userId;
                    int i4 = uid;
                    final String str4 = authTokenType;
                    final String[] strArr = requiredFeatures;
                    final Bundle bundle2 = options;
                    final String str5 = accountType;
                    try {
                        AnonymousClass9 r1 = new Session(accounts, iAccountManagerResponse2, str3, z, true, null, false, true) {
                            public void run() throws RemoteException {
                                this.mAuthenticator.addAccount(this, this.mAccountType, str4, strArr, bundle2);
                            }

                            /* access modifiers changed from: protected */
                            public String toDebugString(long now) {
                                return super.toDebugString(now) + ", addAccount, accountType " + str5 + ", requiredFeatures " + Arrays.toString(strArr);
                            }
                        };
                        r20.bind();
                        restoreCallingIdentity(identityToken3);
                    } catch (Throwable th) {
                        th = th;
                        identityToken = identityToken3;
                        restoreCallingIdentity(identityToken);
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    identityToken = identityToken2;
                    int i5 = usrId;
                    Bundle bundle3 = options;
                    int i6 = pid;
                    int i7 = userId;
                    int i8 = uid;
                    restoreCallingIdentity(identityToken);
                    throw th;
                }
            }
        } else {
            throw new IllegalArgumentException("accountType is null");
        }
    }

    public void addAccountAsUser(IAccountManagerResponse response, String accountType, String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch, Bundle optionsIn, int userId) {
        boolean z;
        long identityToken;
        IAccountManagerResponse iAccountManagerResponse = response;
        String str = accountType;
        Bundle bundle = optionsIn;
        int i = userId;
        Bundle.setDefusable(bundle, true);
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            StringBuilder sb = new StringBuilder();
            sb.append("addAccount, response ");
            sb.append(iAccountManagerResponse);
            sb.append(", authTokenType ");
            sb.append(authTokenType);
            sb.append(", requiredFeatures ");
            sb.append(Arrays.toString(requiredFeatures));
            sb.append(", expectActivityLaunch ");
            z = expectActivityLaunch;
            sb.append(z);
            sb.append(", caller's uid ");
            sb.append(Binder.getCallingUid());
            sb.append(", pid ");
            sb.append(Binder.getCallingPid());
            sb.append(", for user id ");
            sb.append(i);
            Log.v(TAG, sb.toString());
        } else {
            String str2 = authTokenType;
            z = expectActivityLaunch;
        }
        Preconditions.checkArgument(iAccountManagerResponse != null, "response cannot be null");
        Preconditions.checkArgument(str != null, "accountType cannot be null");
        if (isCrossUser(callingUid, i)) {
            throw new SecurityException(String.format("User %s trying to add account for %s", new Object[]{Integer.valueOf(UserHandle.getCallingUserId()), Integer.valueOf(userId)}));
        } else if (!canUserModifyAccounts(i, callingUid)) {
            try {
                iAccountManagerResponse.onError(100, "User is not allowed to add an account!");
            } catch (RemoteException e) {
            }
            showCantAddAccount(100, i);
        } else if (!canUserModifyAccountsForType(i, str, callingUid)) {
            try {
                iAccountManagerResponse.onError(101, "User cannot modify accounts of this type (policy).");
            } catch (RemoteException e2) {
            }
            showCantAddAccount(101, i);
        } else {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            Bundle options = bundle == null ? new Bundle() : bundle;
            options.putInt("callerUid", uid);
            options.putInt("callerPid", pid);
            long identityToken2 = clearCallingIdentity();
            try {
                UserAccounts accounts = getUserAccounts(i);
                logRecordWithUid(accounts, AccountsDb.DEBUG_ACTION_CALLED_ACCOUNT_ADD, "accounts", i);
                r1 = r1;
                AnonymousClass10 r20 = r1;
                long identityToken3 = identityToken2;
                IAccountManagerResponse iAccountManagerResponse2 = iAccountManagerResponse;
                Bundle options2 = options;
                String str3 = str;
                int i2 = uid;
                int i3 = pid;
                int i4 = callingUid;
                final String str4 = authTokenType;
                final String[] strArr = requiredFeatures;
                final Bundle bundle2 = options2;
                final String str5 = accountType;
                try {
                    AnonymousClass10 r1 = new Session(accounts, iAccountManagerResponse2, str3, z, true, null, false, true) {
                        public void run() throws RemoteException {
                            this.mAuthenticator.addAccount(this, this.mAccountType, str4, strArr, bundle2);
                        }

                        /* access modifiers changed from: protected */
                        public String toDebugString(long now) {
                            String str;
                            StringBuilder sb = new StringBuilder();
                            sb.append(super.toDebugString(now));
                            sb.append(", addAccount, accountType ");
                            sb.append(str5);
                            sb.append(", requiredFeatures ");
                            if (strArr != null) {
                                str = TextUtils.join(",", strArr);
                            } else {
                                str = null;
                            }
                            sb.append(str);
                            return sb.toString();
                        }
                    };
                    r20.bind();
                    restoreCallingIdentity(identityToken3);
                } catch (Throwable th) {
                    th = th;
                    identityToken = identityToken3;
                    restoreCallingIdentity(identityToken);
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                identityToken = identityToken2;
                Bundle bundle3 = options;
                int i5 = uid;
                int i6 = pid;
                int i7 = callingUid;
                restoreCallingIdentity(identityToken);
                throw th;
            }
        }
    }

    public void startAddAccountSession(IAccountManagerResponse response, String accountType, String authTokenType, String[] requiredFeatures, boolean expectActivityLaunch, Bundle optionsIn) {
        boolean z;
        long identityToken;
        IAccountManagerResponse iAccountManagerResponse = response;
        String str = accountType;
        Bundle bundle = optionsIn;
        boolean z2 = true;
        Bundle.setDefusable(bundle, true);
        if (Log.isLoggable(TAG, 2)) {
            StringBuilder sb = new StringBuilder();
            sb.append("startAddAccountSession: accountType ");
            sb.append(str);
            sb.append(", response ");
            sb.append(iAccountManagerResponse);
            sb.append(", authTokenType ");
            sb.append(authTokenType);
            sb.append(", requiredFeatures ");
            sb.append(Arrays.toString(requiredFeatures));
            sb.append(", expectActivityLaunch ");
            z = expectActivityLaunch;
            sb.append(z);
            sb.append(", caller's uid ");
            sb.append(Binder.getCallingUid());
            sb.append(", pid ");
            sb.append(Binder.getCallingPid());
            Log.v(TAG, sb.toString());
        } else {
            String str2 = authTokenType;
            z = expectActivityLaunch;
        }
        Preconditions.checkArgument(iAccountManagerResponse != null, "response cannot be null");
        if (str == null) {
            z2 = false;
        }
        Preconditions.checkArgument(z2, "accountType cannot be null");
        int uid = Binder.getCallingUid();
        int userId = UserHandle.getUserId(uid);
        if (!canUserModifyAccounts(userId, uid)) {
            try {
                iAccountManagerResponse.onError(100, "User is not allowed to add an account!");
            } catch (RemoteException e) {
            }
            showCantAddAccount(100, userId);
        } else if (!canUserModifyAccountsForType(userId, str, uid)) {
            try {
                iAccountManagerResponse.onError(101, "User cannot modify accounts of this type (policy).");
            } catch (RemoteException e2) {
            }
            showCantAddAccount(101, userId);
        } else {
            int pid = Binder.getCallingPid();
            Bundle options = bundle == null ? new Bundle() : bundle;
            options.putInt("callerUid", uid);
            options.putInt("callerPid", pid);
            String callerPkg = bundle.getString("androidPackageName");
            boolean isPasswordForwardingAllowed = isPermitted(callerPkg, uid, "android.permission.GET_PASSWORD");
            long identityToken2 = clearCallingIdentity();
            try {
                UserAccounts accounts = getUserAccounts(userId);
                logRecordWithUid(accounts, AccountsDb.DEBUG_ACTION_CALLED_START_ACCOUNT_ADD, "accounts", uid);
                r1 = r1;
                AnonymousClass11 r20 = r1;
                long identityToken3 = identityToken2;
                IAccountManagerResponse iAccountManagerResponse2 = iAccountManagerResponse;
                String str3 = callerPkg;
                String callerPkg2 = str;
                int i = pid;
                int i2 = userId;
                int i3 = uid;
                final String str4 = authTokenType;
                final String[] strArr = requiredFeatures;
                final Bundle bundle2 = options;
                final String str5 = accountType;
                try {
                    AnonymousClass11 r1 = new StartAccountSession(accounts, iAccountManagerResponse2, callerPkg2, z, null, false, true, isPasswordForwardingAllowed) {
                        public void run() throws RemoteException {
                            this.mAuthenticator.startAddAccountSession(this, this.mAccountType, str4, strArr, bundle2);
                        }

                        /* access modifiers changed from: protected */
                        public String toDebugString(long now) {
                            String requiredFeaturesStr = TextUtils.join(",", strArr);
                            StringBuilder sb = new StringBuilder();
                            sb.append(super.toDebugString(now));
                            sb.append(", startAddAccountSession, accountType ");
                            sb.append(str5);
                            sb.append(", requiredFeatures ");
                            sb.append(strArr != null ? requiredFeaturesStr : null);
                            return sb.toString();
                        }
                    };
                    r20.bind();
                    restoreCallingIdentity(identityToken3);
                } catch (Throwable th) {
                    th = th;
                    identityToken = identityToken3;
                    restoreCallingIdentity(identityToken);
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                identityToken = identityToken2;
                String str6 = callerPkg;
                Bundle bundle3 = options;
                int i4 = pid;
                int i5 = userId;
                int i6 = uid;
                restoreCallingIdentity(identityToken);
                throw th;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:64:0x014d  */
    public void finishSessionAsUser(IAccountManagerResponse response, Bundle sessionBundle, boolean expectActivityLaunch, Bundle appInfo, int userId) {
        boolean z;
        long identityToken;
        IAccountManagerResponse iAccountManagerResponse = response;
        Bundle bundle = sessionBundle;
        Bundle bundle2 = appInfo;
        int i = userId;
        Bundle.setDefusable(bundle, true);
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            StringBuilder sb = new StringBuilder();
            sb.append("finishSession: response ");
            sb.append(iAccountManagerResponse);
            sb.append(", expectActivityLaunch ");
            z = expectActivityLaunch;
            sb.append(z);
            sb.append(", caller's uid ");
            sb.append(callingUid);
            sb.append(", caller's user id ");
            sb.append(UserHandle.getCallingUserId());
            sb.append(", pid ");
            sb.append(Binder.getCallingPid());
            sb.append(", for user id ");
            sb.append(i);
            Log.v(TAG, sb.toString());
        } else {
            z = expectActivityLaunch;
        }
        Preconditions.checkArgument(iAccountManagerResponse != null, "response cannot be null");
        if (bundle == null || sessionBundle.size() == 0) {
            throw new IllegalArgumentException("sessionBundle is empty");
        } else if (isCrossUser(callingUid, i)) {
            throw new SecurityException(String.format("User %s trying to finish session for %s without cross user permission", new Object[]{Integer.valueOf(UserHandle.getCallingUserId()), Integer.valueOf(userId)}));
        } else if (!canUserModifyAccounts(i, callingUid)) {
            sendErrorResponse(iAccountManagerResponse, 100, "User is not allowed to add an account!");
            showCantAddAccount(100, i);
        } else {
            int pid = Binder.getCallingPid();
            try {
                Bundle decryptedBundle = CryptoHelper.getInstance().decryptBundle(bundle);
                if (decryptedBundle == null) {
                    try {
                        sendErrorResponse(iAccountManagerResponse, 8, "failed to decrypt session bundle");
                    } catch (GeneralSecurityException e) {
                        e = e;
                        int i2 = pid;
                        int i3 = callingUid;
                        if (Log.isLoggable(TAG, 3)) {
                        }
                        sendErrorResponse(iAccountManagerResponse, 8, "failed to decrypt session bundle");
                    }
                } else {
                    String accountType = decryptedBundle.getString("accountType");
                    if (TextUtils.isEmpty(accountType)) {
                        sendErrorResponse(iAccountManagerResponse, 7, "accountType is empty");
                        return;
                    }
                    if (bundle2 != null) {
                        decryptedBundle.putAll(bundle2);
                    }
                    decryptedBundle.putInt("callerUid", callingUid);
                    decryptedBundle.putInt("callerPid", pid);
                    if (!canUserModifyAccountsForType(i, accountType, callingUid)) {
                        sendErrorResponse(iAccountManagerResponse, 101, "User cannot modify accounts of this type (policy).");
                        showCantAddAccount(101, i);
                        return;
                    }
                    long identityToken2 = clearCallingIdentity();
                    try {
                        UserAccounts accounts = getUserAccounts(i);
                        logRecordWithUid(accounts, AccountsDb.DEBUG_ACTION_CALLED_ACCOUNT_SESSION_FINISH, "accounts", callingUid);
                        r1 = r1;
                        AnonymousClass12 r20 = r1;
                        long identityToken3 = identityToken2;
                        int i4 = pid;
                        int i5 = callingUid;
                        final Bundle bundle3 = decryptedBundle;
                        final String str = accountType;
                        try {
                            AnonymousClass12 r1 = new Session(accounts, iAccountManagerResponse, accountType, z, true, null, false, true) {
                                public void run() throws RemoteException {
                                    this.mAuthenticator.finishSession(this, this.mAccountType, bundle3);
                                }

                                /* access modifiers changed from: protected */
                                public String toDebugString(long now) {
                                    return super.toDebugString(now) + ", finishSession, accountType " + str;
                                }
                            };
                            r20.bind();
                            restoreCallingIdentity(identityToken3);
                        } catch (Throwable th) {
                            th = th;
                            identityToken = identityToken3;
                            restoreCallingIdentity(identityToken);
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        identityToken = identityToken2;
                        String str2 = accountType;
                        Bundle bundle4 = decryptedBundle;
                        int i6 = pid;
                        int i7 = callingUid;
                        restoreCallingIdentity(identityToken);
                        throw th;
                    }
                }
            } catch (GeneralSecurityException e2) {
                e = e2;
                int i8 = pid;
                int i9 = callingUid;
                if (Log.isLoggable(TAG, 3)) {
                    Log.v(TAG, "Failed to decrypt session bundle!", e);
                }
                sendErrorResponse(iAccountManagerResponse, 8, "failed to decrypt session bundle");
            }
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
        boolean z;
        long identityToken;
        IAccountManagerResponse iAccountManagerResponse = response;
        Account account2 = account;
        int i = userId;
        Bundle.setDefusable(options, true);
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            StringBuilder sb = new StringBuilder();
            sb.append("confirmCredentials , response ");
            sb.append(iAccountManagerResponse);
            sb.append(", expectActivityLaunch ");
            z = expectActivityLaunch;
            sb.append(z);
            sb.append(", caller's uid ");
            sb.append(callingUid);
            sb.append(", pid ");
            sb.append(Binder.getCallingPid());
            Log.v(TAG, sb.toString());
        } else {
            z = expectActivityLaunch;
        }
        if (isCrossUser(callingUid, i)) {
            throw new SecurityException(String.format("User %s trying to confirm account credentials for %s", new Object[]{Integer.valueOf(UserHandle.getCallingUserId()), Integer.valueOf(userId)}));
        } else if (iAccountManagerResponse == null) {
            throw new IllegalArgumentException("response is null");
        } else if (account2 != null) {
            long identityToken2 = clearCallingIdentity();
            try {
                UserAccounts accounts = getUserAccounts(i);
                r1 = r1;
                long identityToken3 = identityToken2;
                int i2 = callingUid;
                final Account account3 = account2;
                final Bundle bundle = options;
                try {
                    AnonymousClass13 r1 = new Session(accounts, iAccountManagerResponse, account2.type, z, true, account2.name, true, true) {
                        public void run() throws RemoteException {
                            this.mAuthenticator.confirmCredentials(this, account3, bundle);
                        }

                        /* access modifiers changed from: protected */
                        public String toDebugString(long now) {
                            return super.toDebugString(now) + ", confirmCredentials, " + account3;
                        }
                    };
                    r1.bind();
                    restoreCallingIdentity(identityToken3);
                } catch (Throwable th) {
                    th = th;
                    identityToken = identityToken3;
                    restoreCallingIdentity(identityToken);
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                identityToken = identityToken2;
                int i3 = callingUid;
                restoreCallingIdentity(identityToken);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("account is null");
        }
    }

    public void updateCredentials(IAccountManagerResponse response, Account account, String authTokenType, boolean expectActivityLaunch, Bundle loginOptions) {
        boolean z;
        long identityToken;
        IAccountManagerResponse iAccountManagerResponse = response;
        Account account2 = account;
        Bundle.setDefusable(loginOptions, true);
        if (Log.isLoggable(TAG, 2)) {
            StringBuilder sb = new StringBuilder();
            sb.append("updateCredentials , response ");
            sb.append(iAccountManagerResponse);
            sb.append(", authTokenType ");
            sb.append(authTokenType);
            sb.append(", expectActivityLaunch ");
            z = expectActivityLaunch;
            sb.append(z);
            sb.append(", caller's uid ");
            sb.append(Binder.getCallingUid());
            sb.append(", pid ");
            sb.append(Binder.getCallingPid());
            Log.v(TAG, sb.toString());
        } else {
            String str = authTokenType;
            z = expectActivityLaunch;
        }
        if (iAccountManagerResponse == null) {
            throw new IllegalArgumentException("response is null");
        } else if (account2 != null) {
            int userId = UserHandle.getCallingUserId();
            long identityToken2 = clearCallingIdentity();
            try {
                r1 = r1;
                boolean z2 = z;
                long identityToken3 = identityToken2;
                int i = userId;
                final Account account3 = account2;
                final String str2 = authTokenType;
                final Bundle bundle = loginOptions;
                try {
                    AnonymousClass14 r1 = new Session(getUserAccounts(userId), iAccountManagerResponse, account2.type, z2, true, account2.name, false, true) {
                        public void run() throws RemoteException {
                            this.mAuthenticator.updateCredentials(this, account3, str2, bundle);
                        }

                        /* access modifiers changed from: protected */
                        public String toDebugString(long now) {
                            if (bundle != null) {
                                bundle.keySet();
                            }
                            return super.toDebugString(now) + ", updateCredentials, " + account3 + ", authTokenType " + str2 + ", loginOptions " + bundle;
                        }
                    };
                    r1.bind();
                    restoreCallingIdentity(identityToken3);
                } catch (Throwable th) {
                    th = th;
                    identityToken = identityToken3;
                    restoreCallingIdentity(identityToken);
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                identityToken = identityToken2;
                int i2 = userId;
                restoreCallingIdentity(identityToken);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("account is null");
        }
    }

    public void startUpdateCredentialsSession(IAccountManagerResponse response, Account account, String authTokenType, boolean expectActivityLaunch, Bundle loginOptions) {
        boolean z;
        long identityToken;
        IAccountManagerResponse iAccountManagerResponse = response;
        Account account2 = account;
        Bundle bundle = loginOptions;
        Bundle.setDefusable(bundle, true);
        if (Log.isLoggable(TAG, 2)) {
            StringBuilder sb = new StringBuilder();
            sb.append("startUpdateCredentialsSession: ");
            sb.append(account2);
            sb.append(", response ");
            sb.append(iAccountManagerResponse);
            sb.append(", authTokenType ");
            sb.append(authTokenType);
            sb.append(", expectActivityLaunch ");
            z = expectActivityLaunch;
            sb.append(z);
            sb.append(", caller's uid ");
            sb.append(Binder.getCallingUid());
            sb.append(", pid ");
            sb.append(Binder.getCallingPid());
            Log.v(TAG, sb.toString());
        } else {
            String str = authTokenType;
            z = expectActivityLaunch;
        }
        if (iAccountManagerResponse == null) {
            throw new IllegalArgumentException("response is null");
        } else if (account2 != null) {
            int uid = Binder.getCallingUid();
            int userId = UserHandle.getCallingUserId();
            String callerPkg = bundle.getString("androidPackageName");
            boolean isPasswordForwardingAllowed = isPermitted(callerPkg, uid, "android.permission.GET_PASSWORD");
            long identityToken2 = clearCallingIdentity();
            try {
                UserAccounts accounts = getUserAccounts(userId);
                String str2 = account2.type;
                r1 = r1;
                long identityToken3 = identityToken2;
                String str3 = str2;
                String str4 = callerPkg;
                String callerPkg2 = account2.name;
                int i = userId;
                int i2 = uid;
                final Account account3 = account2;
                final String str5 = authTokenType;
                final Bundle bundle2 = loginOptions;
                try {
                    AnonymousClass15 r1 = new StartAccountSession(accounts, iAccountManagerResponse, str3, z, callerPkg2, false, true, isPasswordForwardingAllowed) {
                        public void run() throws RemoteException {
                            this.mAuthenticator.startUpdateCredentialsSession(this, account3, str5, bundle2);
                        }

                        /* access modifiers changed from: protected */
                        public String toDebugString(long now) {
                            if (bundle2 != null) {
                                bundle2.keySet();
                            }
                            return super.toDebugString(now) + ", startUpdateCredentialsSession, " + account3 + ", authTokenType " + str5 + ", loginOptions " + bundle2;
                        }
                    };
                    r1.bind();
                    restoreCallingIdentity(identityToken3);
                } catch (Throwable th) {
                    th = th;
                    identityToken = identityToken3;
                    restoreCallingIdentity(identityToken);
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                identityToken = identityToken2;
                String str6 = callerPkg;
                int i3 = userId;
                int i4 = uid;
                restoreCallingIdentity(identityToken);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("account is null");
        }
    }

    public void isCredentialsUpdateSuggested(IAccountManagerResponse response, Account account, String statusToken) {
        long identityToken;
        IAccountManagerResponse iAccountManagerResponse = response;
        Account account2 = account;
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "isCredentialsUpdateSuggested: " + account2 + ", response " + iAccountManagerResponse + ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
        }
        if (iAccountManagerResponse == null) {
            throw new IllegalArgumentException("response is null");
        } else if (account2 == null) {
            throw new IllegalArgumentException("account is null");
        } else if (!TextUtils.isEmpty(statusToken)) {
            int usrId = UserHandle.getCallingUserId();
            long identityToken2 = clearCallingIdentity();
            try {
                r1 = r1;
                long identityToken3 = identityToken2;
                final Account account3 = account2;
                final String str = statusToken;
                try {
                    AnonymousClass16 r1 = new Session(getUserAccounts(usrId), iAccountManagerResponse, account2.type, false, false, account2.name, false) {
                        /* access modifiers changed from: protected */
                        public String toDebugString(long now) {
                            return super.toDebugString(now) + ", isCredentialsUpdateSuggested, " + account3;
                        }

                        public void run() throws RemoteException {
                            this.mAuthenticator.isCredentialsUpdateSuggested(this, account3, str);
                        }

                        public void onResult(Bundle result) {
                            Bundle.setDefusable(result, true);
                            IAccountManagerResponse response = getResponseAndClose();
                            if (response != null) {
                                if (result == null) {
                                    AccountManagerService.this.sendErrorResponse(response, 5, "null bundle");
                                    return;
                                }
                                if (Log.isLoggable(AccountManagerService.TAG, 2)) {
                                    Log.v(AccountManagerService.TAG, getClass().getSimpleName() + " calling onResult() on response " + response);
                                }
                                if (result.getInt("errorCode", -1) > 0) {
                                    AccountManagerService.this.sendErrorResponse(response, result.getInt("errorCode"), result.getString("errorMessage"));
                                } else if (!result.containsKey("booleanResult")) {
                                    AccountManagerService.this.sendErrorResponse(response, 5, "no result in response");
                                } else {
                                    Bundle newResult = new Bundle();
                                    newResult.putBoolean("booleanResult", result.getBoolean("booleanResult", false));
                                    AccountManagerService.this.sendResponse(response, newResult);
                                }
                            }
                        }
                    };
                    r1.bind();
                    restoreCallingIdentity(identityToken3);
                } catch (Throwable th) {
                    th = th;
                    identityToken = identityToken3;
                    restoreCallingIdentity(identityToken);
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                identityToken = identityToken2;
                restoreCallingIdentity(identityToken);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("status token is empty");
        }
    }

    public void editProperties(IAccountManagerResponse response, String accountType, boolean expectActivityLaunch) {
        boolean z;
        long identityToken;
        IAccountManagerResponse iAccountManagerResponse = response;
        String str = accountType;
        int callingUid = Binder.getCallingUid();
        if (Log.isLoggable(TAG, 2)) {
            StringBuilder sb = new StringBuilder();
            sb.append("editProperties , response ");
            sb.append(iAccountManagerResponse);
            sb.append(", expectActivityLaunch ");
            z = expectActivityLaunch;
            sb.append(z);
            sb.append(", caller's uid ");
            sb.append(callingUid);
            sb.append(", pid ");
            sb.append(Binder.getCallingPid());
            Log.v(TAG, sb.toString());
        } else {
            z = expectActivityLaunch;
        }
        if (iAccountManagerResponse == null) {
            throw new IllegalArgumentException("response is null");
        } else if (str != null) {
            int userId = UserHandle.getCallingUserId();
            if (isAccountManagedByCaller(str, callingUid, userId) || isSystemUid(callingUid)) {
                long identityToken2 = clearCallingIdentity();
                try {
                    UserAccounts accounts = getUserAccounts(userId);
                    r1 = r1;
                    identityToken = identityToken2;
                    int i = userId;
                    final String str2 = str;
                    try {
                        AnonymousClass17 r1 = new Session(accounts, iAccountManagerResponse, str, z, true, null, false) {
                            public void run() throws RemoteException {
                                this.mAuthenticator.editProperties(this, this.mAccountType);
                            }

                            /* access modifiers changed from: protected */
                            public String toDebugString(long now) {
                                return super.toDebugString(now) + ", editProperties, accountType " + str2;
                            }
                        };
                        r1.bind();
                        restoreCallingIdentity(identityToken);
                    } catch (Throwable th) {
                        th = th;
                        restoreCallingIdentity(identityToken);
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    identityToken = identityToken2;
                    int i2 = userId;
                    restoreCallingIdentity(identityToken);
                    throw th;
                }
            } else {
                throw new SecurityException(String.format("uid %s cannot edit authenticator properites for account type: %s", new Object[]{Integer.valueOf(callingUid), str}));
            }
        } else {
            throw new IllegalArgumentException("accountType is null");
        }
    }

    public boolean hasAccountAccess(Account account, String packageName, UserHandle userHandle) {
        if (UserHandle.getAppId(Binder.getCallingUid()) == 1000) {
            Preconditions.checkNotNull(account, "account cannot be null");
            Preconditions.checkNotNull(packageName, "packageName cannot be null");
            Preconditions.checkNotNull(userHandle, "userHandle cannot be null");
            int userId = userHandle.getIdentifier();
            Preconditions.checkArgumentInRange(userId, 0, HwBootFail.STAGE_BOOT_SUCCESS, "user must be concrete");
            try {
                return hasAccountAccess(account, packageName, this.mPackageManager.getPackageUidAsUser(packageName, userId));
            } catch (PackageManager.NameNotFoundException e) {
                Log.d(TAG, "Package not found " + e.getMessage());
                return false;
            }
        } else {
            throw new SecurityException("Can be called only by system UID");
        }
    }

    private String getPackageNameForUid(int uid) {
        String[] packageNames = this.mPackageManager.getPackagesForUid(uid);
        if (ArrayUtils.isEmpty(packageNames)) {
            return null;
        }
        String packageName = packageNames[0];
        if (packageNames.length == 1) {
            return packageName;
        }
        int oldestVersion = Integer.MAX_VALUE;
        String packageName2 = packageName;
        for (String name : packageNames) {
            try {
                ApplicationInfo applicationInfo = this.mPackageManager.getApplicationInfo(name, 0);
                if (applicationInfo != null) {
                    int version = applicationInfo.targetSdkVersion;
                    if (version < oldestVersion) {
                        oldestVersion = version;
                        packageName2 = name;
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        return packageName2;
    }

    /* access modifiers changed from: private */
    public boolean hasAccountAccess(Account account, String packageName, int uid) {
        boolean z = false;
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
        if (visibility == 1 || visibility == 2) {
            z = true;
        }
        return z;
    }

    public IntentSender createRequestAccountAccessIntentSenderAsUser(Account account, String packageName, UserHandle userHandle) {
        if (UserHandle.getAppId(Binder.getCallingUid()) == 1000) {
            Preconditions.checkNotNull(account, "account cannot be null");
            Preconditions.checkNotNull(packageName, "packageName cannot be null");
            Preconditions.checkNotNull(userHandle, "userHandle cannot be null");
            int userId = userHandle.getIdentifier();
            Preconditions.checkArgumentInRange(userId, 0, HwBootFail.STAGE_BOOT_SUCCESS, "user must be concrete");
            try {
                Intent intent = newRequestAccountAccessIntent(account, packageName, this.mPackageManager.getPackageUidAsUser(packageName, userId), null);
                long identity = Binder.clearCallingIdentity();
                try {
                    return PendingIntent.getActivityAsUser(this.mContext, 0, intent, 1409286144, null, new UserHandle(userId)).getIntentSender();
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            } catch (PackageManager.NameNotFoundException e) {
                Slog.e(TAG, "Unknown package " + packageName);
                return null;
            }
        } else {
            throw new SecurityException("Can be called only by system UID");
        }
    }

    /* access modifiers changed from: private */
    public Intent newRequestAccountAccessIntent(Account account, String packageName, int uid, RemoteCallback callback) {
        final Account account2 = account;
        final int i = uid;
        final String str = packageName;
        final RemoteCallback remoteCallback = callback;
        AnonymousClass18 r5 = new IAccountAuthenticatorResponse.Stub() {
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
        };
        return newGrantCredentialsPermissionIntent(account, packageName, uid, new AccountAuthenticatorResponse(r5), "com.android.AccountManager.ACCOUNT_ACCESS_TOKEN_TYPE", false);
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
            return getAccountsInternal(getUserAccounts(userId), callingUid, opPackageName, visibleAccountTypes, false);
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
            userIds[i] = users.get(i).id;
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
        String opPackageName2;
        int callingUid;
        long identityToken;
        String str = type;
        int i = userId;
        int callingUid2 = Binder.getCallingUid();
        if (i == UserHandle.getCallingUserId() || callingUid2 == 1000 || this.mContext.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") == 0) {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "getAccounts: accountType " + str + ", caller's uid " + Binder.getCallingUid() + ", pid " + Binder.getCallingPid());
            }
            List<String> managedTypes = getTypesManagedByCaller(callingUid2, UserHandle.getUserId(callingUid2));
            int callingUid3 = packageUid;
            if (callingUid3 == -1 || (!UserHandle.isSameApp(callingUid2, 1000) && (str == null || !managedTypes.contains(str)))) {
                opPackageName2 = opPackageName;
                callingUid = callingUid2;
            } else {
                callingUid = callingUid3;
                opPackageName2 = callingPackage;
            }
            List<String> visibleAccountTypes = getTypesVisibleToCaller(callingUid, i, opPackageName2);
            if (visibleAccountTypes.isEmpty() || (str != null && !visibleAccountTypes.contains(str))) {
                return EMPTY_ACCOUNT_ARRAY;
            }
            if (visibleAccountTypes.contains(str)) {
                visibleAccountTypes = new ArrayList<>();
                visibleAccountTypes.add(str);
            }
            List<String> visibleAccountTypes2 = visibleAccountTypes;
            long identityToken2 = clearCallingIdentity();
            try {
                UserAccounts accounts = getUserAccounts(i);
                identityToken = identityToken2;
                try {
                    Account[] accountsInternal = getAccountsInternal(accounts, callingUid, opPackageName2, visibleAccountTypes2, includeUserManagedNotVisible);
                    restoreCallingIdentity(identityToken);
                    return accountsInternal;
                } catch (Throwable th) {
                    th = th;
                    restoreCallingIdentity(identityToken);
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                identityToken = identityToken2;
                restoreCallingIdentity(identityToken);
                throw th;
            }
        } else {
            throw new SecurityException("User " + UserHandle.getCallingUserId() + " trying to get account for " + i);
        }
    }

    private Account[] getAccountsInternal(UserAccounts userAccounts, int callingUid, String callingPackage, List<String> visibleAccountTypes, boolean includeUserManagedNotVisible) {
        HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(callingUid, 0, IHwBehaviorCollectManager.BehaviorId.ACCOUNTMANAGER_GETACCOUNTSINTERNAL);
        ArrayList<Account> visibleAccounts = new ArrayList<>();
        for (String visibleType : visibleAccountTypes) {
            Account[] accountsForType = getAccountsFromCache(userAccounts, visibleType, callingUid, callingPackage, includeUserManagedNotVisible);
            if (accountsForType != null) {
                visibleAccounts.addAll(Arrays.asList(accountsForType));
            }
        }
        Account[] result = new Account[visibleAccounts.size()];
        for (int i = 0; i < visibleAccounts.size(); i++) {
            result[i] = visibleAccounts.get(i);
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
        String str = type;
        String str2 = packageName;
        int callingUid = Binder.getCallingUid();
        int userId = UserHandle.getCallingUserId();
        String str3 = opPackageName;
        this.mAppOpsManager.checkPackage(callingUid, str3);
        try {
            int packageUid = this.mPackageManager.getPackageUidAsUser(str2, userId);
            if (!UserHandle.isSameApp(callingUid, 1000) && str != null && !isAccountManagedByCaller(str, callingUid, userId)) {
                return EMPTY_ACCOUNT_ARRAY;
            }
            if (!UserHandle.isSameApp(callingUid, 1000) && str == null) {
                return getAccountsAsUserForPackage(str, userId, str2, packageUid, str3, false);
            }
            int i = userId;
            int i2 = callingUid;
            return getAccountsAsUserForPackage(str, userId, str2, packageUid, opPackageName, true);
        } catch (PackageManager.NameNotFoundException re) {
            int i3 = userId;
            int i4 = callingUid;
            Slog.e(TAG, "Couldn't determine the packageUid for " + str2 + re);
            return EMPTY_ACCOUNT_ARRAY;
        }
    }

    private boolean needToStartChooseAccountActivity(Account[] accounts, String callingPackage) {
        if (accounts.length < 1) {
            return false;
        }
        return accounts.length > 1 || resolveAccountVisibility(accounts[0], callingPackage, getUserAccounts(UserHandle.getCallingUserId())).intValue() == 4;
    }

    private void startChooseAccountActivityWithAccounts(IAccountManagerResponse response, Account[] accounts, String callingPackage) {
        Intent intent = new Intent(this.mContext, ChooseAccountActivity.class);
        intent.putExtra("accounts", accounts);
        intent.putExtra("accountManagerResponse", new AccountManagerResponse(response));
        intent.putExtra("androidPackageName", callingPackage);
        this.mContext.startActivityAsUser(intent, UserHandle.of(UserHandle.getCallingUserId()));
    }

    /* access modifiers changed from: private */
    public void handleGetAccountsResult(IAccountManagerResponse response, Account[] accounts, String callingPackage) {
        if (needToStartChooseAccountActivity(accounts, callingPackage)) {
            startChooseAccountActivityWithAccounts(response, accounts, callingPackage);
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
        long identityToken;
        final IAccountManagerResponse iAccountManagerResponse = response;
        String str = accountType;
        final String str2 = opPackageName;
        int callingUid = Binder.getCallingUid();
        this.mAppOpsManager.checkPackage(callingUid, str2);
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "getAccount: accountType " + str + ", response " + iAccountManagerResponse + ", features " + Arrays.toString(features) + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (iAccountManagerResponse == null) {
            throw new IllegalArgumentException("response is null");
        } else if (str != null) {
            int userId = UserHandle.getCallingUserId();
            long identityToken2 = clearCallingIdentity();
            try {
                UserAccounts userAccounts = getUserAccounts(userId);
                if (ArrayUtils.isEmpty(features)) {
                    try {
                        handleGetAccountsResult(iAccountManagerResponse, getAccountsFromCache(userAccounts, str, callingUid, str2, true), str2);
                        restoreCallingIdentity(identityToken2);
                    } catch (Throwable th) {
                        th = th;
                        identityToken = identityToken2;
                        int i = userId;
                        int i2 = callingUid;
                        restoreCallingIdentity(identityToken);
                        throw th;
                    }
                } else {
                    r3 = r3;
                    long identityToken3 = identityToken2;
                    int i3 = userId;
                    int i4 = callingUid;
                    try {
                        GetAccountsByTypeAndFeatureSession getAccountsByTypeAndFeatureSession = new GetAccountsByTypeAndFeatureSession(this, userAccounts, new IAccountManagerResponse.Stub() {
                            public void onResult(Bundle value) throws RemoteException {
                                Parcelable[] parcelables = value.getParcelableArray("accounts");
                                Account[] accounts = new Account[parcelables.length];
                                for (int i = 0; i < parcelables.length; i++) {
                                    accounts[i] = (Account) parcelables[i];
                                }
                                AccountManagerService.this.handleGetAccountsResult(iAccountManagerResponse, accounts, str2);
                            }

                            public void onError(int errorCode, String errorMessage) throws RemoteException {
                            }
                        }, str, features, callingUid, str2, true);
                        getAccountsByTypeAndFeatureSession.bind();
                        restoreCallingIdentity(identityToken3);
                    } catch (Throwable th2) {
                        th = th2;
                        identityToken = identityToken3;
                        restoreCallingIdentity(identityToken);
                        throw th;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                identityToken = identityToken2;
                int i5 = userId;
                int i6 = callingUid;
                restoreCallingIdentity(identityToken);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("accountType is null");
        }
    }

    public void getAccountsByFeatures(IAccountManagerResponse response, String type, String[] features, String opPackageName) {
        long identityToken;
        IAccountManagerResponse iAccountManagerResponse = response;
        String str = type;
        String[] strArr = features;
        String str2 = opPackageName;
        int callingUid = Binder.getCallingUid();
        this.mAppOpsManager.checkPackage(callingUid, str2);
        if (Log.isLoggable(TAG, 2)) {
            Log.v(TAG, "getAccounts: accountType " + str + ", response " + iAccountManagerResponse + ", features " + Arrays.toString(features) + ", caller's uid " + callingUid + ", pid " + Binder.getCallingPid());
        }
        if (iAccountManagerResponse == null) {
            throw new IllegalArgumentException("response is null");
        } else if (str != null) {
            int userId = UserHandle.getCallingUserId();
            List<String> visibleAccountTypes = getTypesVisibleToCaller(callingUid, userId, str2);
            if (!visibleAccountTypes.contains(str)) {
                Bundle result = new Bundle();
                result.putParcelableArray("accounts", EMPTY_ACCOUNT_ARRAY);
                try {
                    iAccountManagerResponse.onResult(result);
                } catch (RemoteException e) {
                    RemoteException remoteException = e;
                    Log.e(TAG, "Cannot respond to caller do to exception.", e);
                }
                return;
            }
            long identityToken2 = clearCallingIdentity();
            try {
                UserAccounts userAccounts = getUserAccounts(userId);
                if (strArr == null) {
                    identityToken = identityToken2;
                    List<String> list = visibleAccountTypes;
                    int i = userId;
                } else if (strArr.length == 0) {
                    identityToken = identityToken2;
                    List<String> list2 = visibleAccountTypes;
                    int i2 = userId;
                } else {
                    r1 = r1;
                    long identityToken3 = identityToken2;
                    List<String> list3 = visibleAccountTypes;
                    int i3 = userId;
                    try {
                        GetAccountsByTypeAndFeatureSession getAccountsByTypeAndFeatureSession = new GetAccountsByTypeAndFeatureSession(this, userAccounts, iAccountManagerResponse, str, strArr, callingUid, str2, false);
                        getAccountsByTypeAndFeatureSession.bind();
                        restoreCallingIdentity(identityToken3);
                        return;
                    } catch (Throwable th) {
                        th = th;
                        identityToken = identityToken3;
                        restoreCallingIdentity(identityToken);
                        throw th;
                    }
                }
                try {
                    Account[] accounts = getAccountsFromCache(userAccounts, str, callingUid, str2, false);
                    Bundle result2 = new Bundle();
                    result2.putParcelableArray("accounts", accounts);
                    onResult(iAccountManagerResponse, result2);
                    restoreCallingIdentity(identityToken);
                } catch (Throwable th2) {
                    th = th2;
                    restoreCallingIdentity(identityToken);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                identityToken = identityToken2;
                List<String> list4 = visibleAccountTypes;
                int i4 = userId;
                restoreCallingIdentity(identityToken);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("accountType is null");
        }
    }

    public void onAccountAccessed(String token) throws RemoteException {
        int uid = Binder.getCallingUid();
        if (UserHandle.getAppId(uid) != 1000) {
            int userId = UserHandle.getCallingUserId();
            long identity = Binder.clearCallingIdentity();
            try {
                for (Account account : getAccounts(userId, this.mContext.getOpPackageName())) {
                    if (Objects.equals(account.getAccessId(), token) && !hasAccountAccess(account, (String) null, uid)) {
                        updateAppPermission(account, "com.android.AccountManager.ACCOUNT_ACCESS_TOKEN_TYPE", uid, true);
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [android.os.Binder] */
    /* JADX WARNING: Multi-variable type inference failed */
    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        new AccountManagerServiceShellCommand(this).exec(this, in, out, err, args, callback, resultReceiver);
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
        AnonymousClass1LogRecordTask logTask = new Runnable(action, tableName, accountId, userAccount, callingUid, (long) userAccount.debugDbInsertionPoint) {
            private final long accountId;
            private final String action;
            private final int callingUid;
            private final String tableName;
            private final UserAccounts userAccount;
            private final long userDebugDbInsertionPoint;

            {
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
                try {
                    logStatement.execute();
                } catch (IllegalStateException e) {
                    Slog.w(AccountManagerService.TAG, "Failed to insert a log record. accountId=" + this.accountId + " action=" + this.action + " tableName=" + this.tableName + " Error: " + e);
                } catch (SQLiteException e2) {
                    Slog.w(AccountManagerService.TAG, "Failed reading Uri grants");
                } catch (Throwable th) {
                    logStatement.clearBindings();
                    throw th;
                }
                logStatement.clearBindings();
            }
        };
        int unused = userAccount.debugDbInsertionPoint = (userAccount.debugDbInsertionPoint + 1) % 64;
        this.mHandler.post(logTask);
    }

    private void initializeDebugDbSizeAndCompileSqlStatementForLogging(UserAccounts userAccount) {
        int unused = userAccount.debugDbInsertionPoint = userAccount.accountsDb.calculateDebugTableInsertionPoint();
        SQLiteStatement unused2 = userAccount.statementForLogging = userAccount.accountsDb.compileSqlStatementForLogging();
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

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, fout)) {
            boolean isCheckinRequest = scanArgs(args, "--checkin") || scanArgs(args, "-c");
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

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x0167, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0170, code lost:
        r0 = th;
     */
    private void dumpUser(UserAccounts userAccounts, FileDescriptor fd, PrintWriter fout, String[] args, boolean isCheckinRequest) {
        Account[] accounts;
        boolean isUserUnlocked;
        UserAccounts userAccounts2 = userAccounts;
        PrintWriter printWriter = fout;
        if (isCheckinRequest) {
            synchronized (userAccounts2.dbLock) {
                userAccounts2.accountsDb.dumpDeAccountsTable(printWriter);
            }
            FileDescriptor fileDescriptor = fd;
            String[] strArr = args;
        } else {
            printWriter.println("Accounts: " + getAccountsFromCache(userAccounts2, null, 1000, null, false).length);
            for (Account account : accounts) {
                printWriter.println("  " + account);
            }
            fout.println();
            synchronized (userAccounts2.dbLock) {
                try {
                    userAccounts2.accountsDb.dumpDebugTable(printWriter);
                } catch (Throwable th) {
                    th = th;
                    FileDescriptor fileDescriptor2 = fd;
                    String[] strArr2 = args;
                    while (true) {
                        throw th;
                    }
                }
            }
            fout.println();
            synchronized (this.mSessions) {
                try {
                    long now = SystemClock.elapsedRealtime();
                    printWriter.println("Active Sessions: " + this.mSessions.size());
                    Iterator<Session> it = this.mSessions.values().iterator();
                    while (it.hasNext()) {
                        printWriter.println("  " + it.next().toDebugString(now));
                    }
                } catch (Throwable th2) {
                    th = th2;
                    FileDescriptor fileDescriptor3 = fd;
                    String[] strArr3 = args;
                    while (true) {
                        throw th;
                    }
                }
            }
            fout.println();
            this.mAuthenticatorCache.dump(fd, printWriter, args, userAccounts.userId);
            synchronized (this.mUsers) {
                isUserUnlocked = isLocalUnlockedUser(userAccounts.userId);
            }
            if (isUserUnlocked) {
                fout.println();
                synchronized (userAccounts2.dbLock) {
                    Map<Account, Map<String, Integer>> allVisibilityValues = userAccounts2.accountsDb.findAllVisibilityValues();
                    printWriter.println("Account visibility:");
                    for (Account account2 : allVisibilityValues.keySet()) {
                        printWriter.println("  " + account2.name);
                        for (Map.Entry<String, Integer> entry : allVisibilityValues.get(account2).entrySet()) {
                            printWriter.println("    " + entry.getKey() + ", " + entry.getValue());
                            allVisibilityValues = allVisibilityValues;
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void doNotification(UserAccounts accounts, Account account, CharSequence message, Intent intent, String packageName, int userId) {
        Account account2 = account;
        CharSequence charSequence = message;
        Intent intent2 = intent;
        String str = packageName;
        int i = userId;
        long identityToken = clearCallingIdentity();
        try {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "doNotification: " + charSequence + " intent:" + intent2);
            }
            if (intent.getComponent() == null || !GrantCredentialsPermissionActivity.class.getName().equals(intent.getComponent().getClassName())) {
                Context contextForUser = getContextForUser(new UserHandle(i));
                NotificationId id = getSigninRequiredNotificationId(accounts, account);
                intent2.addCategory(id.mTag);
                String notificationTitleFormat = contextForUser.getText(17040608).toString();
                Bitmap bmp = BitmapFactory.decodeResource(this.mContext.getResources(), 33751687);
                Notification.Builder contentText = new Notification.Builder(contextForUser, SystemNotificationChannels.ACCOUNT).setWhen(0).setSmallIcon(17301642).setLargeIcon(bmp).setColor(contextForUser.getColor(17170784)).setContentTitle(String.format(notificationTitleFormat, new Object[]{account2.name})).setContentText(charSequence);
                Bitmap bitmap = bmp;
                String str2 = notificationTitleFormat;
                Notification.Builder builder = contentText;
                installNotification(id, builder.setContentIntent(PendingIntent.getActivityAsUser(this.mContext, 0, intent2, 268435456, null, new UserHandle(i))).build(), str, i);
            } else {
                createNoCredentialsPermissionNotification(account2, intent2, str, i);
            }
        } finally {
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
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /* access modifiers changed from: private */
    public void cancelNotification(NotificationId id, UserHandle user) {
        cancelNotification(id, this.mContext.getPackageName(), user);
    }

    /* access modifiers changed from: private */
    public void cancelNotification(NotificationId id, String packageName, UserHandle user) {
        long identityToken = clearCallingIdentity();
        try {
            this.mInjector.getNotificationManager().cancelNotificationWithTag(packageName, id.mTag, id.mId, user.getIdentifier());
        } catch (RemoteException e) {
        } catch (Throwable th) {
            restoreCallingIdentity(identityToken);
            throw th;
        }
        restoreCallingIdentity(identityToken);
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
        } catch (RemoteException e) {
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
            throw th;
        }
        Binder.restoreCallingIdentity(identity);
        return false;
    }

    private boolean isPermitted(String opPackageName, int callingUid, String... permissions) {
        for (String perm : permissions) {
            if (this.mContext.checkCallingOrSelfPermission(perm) == 0) {
                if (Log.isLoggable(TAG, 2)) {
                    Log.v(TAG, "  caller uid " + callingUid + " has " + perm);
                }
                int opCode = AppOpsManager.permissionToOpCode(perm);
                if (opCode == -1 || this.mAppOpsManager.noteOpNoThrow(opCode, callingUid, opPackageName) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private int handleIncomingUser(int userId) {
        try {
            return ActivityManager.getService().handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, true, true, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, null);
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
                Binder.restoreCallingIdentity(identityToken);
                return false;
            }
            for (String name : packages) {
                PackageInfo packageInfo = this.mPackageManager.getPackageInfo(name, 0);
                if (!(packageInfo == null || (packageInfo.applicationInfo.privateFlags & 8) == 0)) {
                    Binder.restoreCallingIdentity(identityToken);
                    return true;
                }
            }
            Binder.restoreCallingIdentity(identityToken);
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "Package not found " + e.getMessage());
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identityToken);
            throw th;
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

    /* JADX INFO: finally extract failed */
    private boolean accountTypeManagesContacts(String accountType, int userId) {
        if (accountType == null) {
            return false;
        }
        long identityToken = Binder.clearCallingIdentity();
        try {
            Collection<RegisteredServicesCache.ServiceInfo<AuthenticatorDescription>> serviceInfos = this.mAuthenticatorCache.getAllServices(userId);
            Binder.restoreCallingIdentity(identityToken);
            for (RegisteredServicesCache.ServiceInfo<AuthenticatorDescription> serviceInfo : serviceInfos) {
                if (accountType.equals(((AuthenticatorDescription) serviceInfo.type).type)) {
                    return isPermittedForPackage(((AuthenticatorDescription) serviceInfo.type).packageName, serviceInfo.uid, userId, "android.permission.WRITE_CONTACTS");
                }
            }
            return false;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identityToken);
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    private int checkPackageSignature(String accountType, int callingUid, int userId) {
        if (accountType == null) {
            return 0;
        }
        long identityToken = Binder.clearCallingIdentity();
        try {
            Collection<RegisteredServicesCache.ServiceInfo<AuthenticatorDescription>> serviceInfos = this.mAuthenticatorCache.getAllServices(userId);
            Binder.restoreCallingIdentity(identityToken);
            PackageManagerInternal pmi = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
            for (RegisteredServicesCache.ServiceInfo<AuthenticatorDescription> serviceInfo : serviceInfos) {
                if (accountType.equals(((AuthenticatorDescription) serviceInfo.type).type)) {
                    if (serviceInfo.uid == callingUid) {
                        return 2;
                    }
                    if (pmi.hasSignatureCapability(serviceInfo.uid, callingUid, 16)) {
                        return 1;
                    }
                }
            }
            return 0;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identityToken);
            throw th;
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

    /* JADX INFO: finally extract failed */
    private List<String> getTypesForCaller(int callingUid, int userId, boolean isOtherwisePermitted) {
        List<String> managedAccountTypes = new ArrayList<>();
        long identityToken = Binder.clearCallingIdentity();
        try {
            Collection<RegisteredServicesCache.ServiceInfo<AuthenticatorDescription>> serviceInfos = this.mAuthenticatorCache.getAllServices(userId);
            Binder.restoreCallingIdentity(identityToken);
            PackageManagerInternal pmi = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
            for (RegisteredServicesCache.ServiceInfo<AuthenticatorDescription> serviceInfo : serviceInfos) {
                if (isOtherwisePermitted || pmi.hasSignatureCapability(serviceInfo.uid, callingUid, 16)) {
                    managedAccountTypes.add(((AuthenticatorDescription) serviceInfo.type).type);
                }
            }
            return managedAccountTypes;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identityToken);
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public boolean isAccountPresentForCaller(String accountName, String accountType) {
        if (getUserAccountsForCaller().accountCache.containsKey(accountType)) {
            for (Account account : getUserAccountsForCaller().accountCache.get(accountType)) {
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
        long grantsCount;
        if (UserHandle.getAppId(callerUid) == 1000) {
            return true;
        }
        UserAccounts accounts = getUserAccounts(UserHandle.getUserId(callerUid));
        synchronized (accounts.dbLock) {
            synchronized (accounts.cacheLock) {
                if (authTokenType != null) {
                    try {
                        grantsCount = accounts.accountsDb.findMatchingGrantsCount(callerUid, authTokenType, account);
                    } catch (Throwable th) {
                        while (true) {
                            throw th;
                        }
                    }
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
        String name;
        long ident = Binder.clearCallingIdentity();
        try {
            String[] packages = this.mPackageManager.getPackagesForUid(callingUid);
            if (packages != null) {
                int length = packages.length;
                for (int i = 0; i < length; i++) {
                    name = packages[i];
                    PackageInfo packageInfo = this.mPackageManager.getPackageInfo(name, 0);
                    if (!(packageInfo == null || (packageInfo.applicationInfo.flags & 1) == 0)) {
                        Binder.restoreCallingIdentity(ident);
                        return true;
                    }
                }
            } else {
                Log.w(TAG, "No known packages with uid " + callingUid);
            }
            Binder.restoreCallingIdentity(ident);
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, String.format("Could not find package [%s]", new Object[]{name}), e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
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
        DevicePolicyManager dpm = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
        if (dpm == null) {
            return true;
        }
        String[] typesArray = dpm.getAccountTypesWithManagementDisabledAsUser(userId);
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
        return dpmi != null && dpmi.isActiveAdminWithPolicy(uid, -1);
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

    /* access modifiers changed from: package-private */
    public void grantAppPermission(Account account, String authTokenType, int uid) {
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
        Iterator<AccountManagerInternal.OnAppPermissionChangeListener> it = this.mAppPermissionChangeListeners.iterator();
        while (it.hasNext()) {
            this.mHandler.post(new Runnable(it.next(), account, uid) {
                private final /* synthetic */ AccountManagerInternal.OnAppPermissionChangeListener f$0;
                private final /* synthetic */ Account f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    this.f$0.onAppPermissionChanged(this.f$1, this.f$2);
                }
            });
        }
    }

    /* JADX INFO: finally extract failed */
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
                    throw th;
                }
            }
        }
        Iterator<AccountManagerInternal.OnAppPermissionChangeListener> it = this.mAppPermissionChangeListeners.iterator();
        while (it.hasNext()) {
            this.mHandler.post(new Runnable(it.next(), account, uid) {
                private final /* synthetic */ AccountManagerInternal.OnAppPermissionChangeListener f$0;
                private final /* synthetic */ Account f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    this.f$0.onAppPermissionChanged(this.f$1, this.f$2);
                }
            });
        }
    }

    private void removeAccountFromCacheLocked(UserAccounts accounts, Account account) {
        Account[] oldAccountsForType = accounts.accountCache.get(account.type);
        if (oldAccountsForType != null) {
            ArrayList<Account> newAccountsList = new ArrayList<>();
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
        Account[] accountsForType = accounts.accountCache.get(account.type);
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
        if (visibilityFilterPackage == null) {
            visibilityFilterPackage = getPackageNameForUid(callingUid);
        }
        Map<Account, Integer> firstPass = new LinkedHashMap<>();
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
        int i = callingUid;
        String str = callingPackage;
        if (getUserManager() == null || userAccounts == null || userAccounts.userId < 0 || i == 1000) {
            return unfiltered;
        }
        UserInfo user = getUserManager().getUserInfo(userAccounts.userId);
        if (user == null || !user.isRestricted()) {
            return unfiltered;
        }
        String[] packages = this.mPackageManager.getPackagesForUid(i);
        if (packages == null) {
            packages = new String[0];
        }
        String[] packages2 = packages;
        String visibleList = this.mContext.getResources().getString(17039766);
        for (String packageName : packages2) {
            if (visibleList.contains(";" + packageName + ";")) {
                return unfiltered;
            }
        }
        Account[] sharedAccounts = getSharedAccountsAsUser(userAccounts.userId);
        if (ArrayUtils.isEmpty(sharedAccounts)) {
            return unfiltered;
        }
        String requiredAccountType = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        if (str == null) {
            int length = packages2.length;
            int i2 = 0;
            while (true) {
                if (i2 >= length) {
                    break;
                }
                PackageInfo pi = this.mPackageManager.getPackageInfo(packages2[i2], 0);
                if (pi != null && pi.restrictedAccountType != null) {
                    requiredAccountType = pi.restrictedAccountType;
                    break;
                }
                i2++;
            }
        } else {
            try {
                PackageInfo pi2 = this.mPackageManager.getPackageInfo(str, 0);
                if (!(pi2 == null || pi2.restrictedAccountType == null)) {
                    requiredAccountType = pi2.restrictedAccountType;
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.d(TAG, "Package not found " + e.getMessage());
            }
        }
        Map<Account, Integer> filtered = new LinkedHashMap<>();
        for (Map.Entry<Account, Integer> entry : unfiltered.entrySet()) {
            Account account = entry.getKey();
            if (account.type.equals(requiredAccountType)) {
                filtered.put(account, entry.getValue());
            } else {
                boolean found = false;
                int length2 = sharedAccounts.length;
                int i3 = 0;
                while (true) {
                    if (i3 >= length2) {
                        break;
                    } else if (sharedAccounts[i3].equals(account)) {
                        found = true;
                        break;
                    } else {
                        i3++;
                        int i4 = callingUid;
                    }
                }
                if (!found) {
                    filtered.put(account, entry.getValue());
                }
            }
            int i5 = callingUid;
        }
        return filtered;
    }

    /* access modifiers changed from: protected */
    public Account[] getAccountsFromCache(UserAccounts userAccounts, String accountType, int callingUid, String callingPackage, boolean includeManagedNotVisible) {
        Account[] accounts;
        Preconditions.checkState(!Thread.holdsLock(userAccounts.cacheLock), "Method should not be called with cacheLock");
        if (accountType != null) {
            synchronized (userAccounts.cacheLock) {
                accounts = userAccounts.accountCache.get(accountType);
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
            int totalLength2 = 0;
            for (Account[] accountsOfType : userAccounts.accountCache.values()) {
                System.arraycopy(accountsOfType, 0, accountsArray, totalLength2, accountsOfType.length);
                totalLength2 += accountsOfType.length;
            }
            return filterAccounts(userAccounts, accountsArray, callingUid, callingPackage, includeManagedNotVisible);
        }
    }

    /* access modifiers changed from: protected */
    public void writeUserDataIntoCacheLocked(UserAccounts accounts, Account account, String key, String value) {
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

    /* access modifiers changed from: protected */
    public String readCachedTokenInternal(UserAccounts accounts, Account account, String tokenType, String callingPackage, byte[] pkgSigDigest) {
        String str;
        synchronized (accounts.cacheLock) {
            str = accounts.accountTokenCaches.get(account, tokenType, callingPackage, pkgSigDigest);
        }
        return str;
    }

    /* access modifiers changed from: protected */
    public void writeAuthTokenIntoCacheLocked(UserAccounts accounts, Account account, String key, String value) {
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

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001a, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r0 = r5.cacheLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001d, code lost:
        monitor-enter(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:?, code lost:
        r2 = (java.util.Map) com.android.server.accounts.AccountManagerService.UserAccounts.access$1200(r5).get(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0028, code lost:
        if (r2 != null) goto L_0x0038;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002a, code lost:
        r2 = r5.accountsDb.findAuthTokensByAccount(r6);
        com.android.server.accounts.AccountManagerService.UserAccounts.access$1200(r5).put(r6, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0038, code lost:
        r3 = r2.get(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003e, code lost:
        monitor-exit(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0040, code lost:
        return r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0018, code lost:
        r1 = r5.dbLock;
     */
    public String readAuthTokenInternal(UserAccounts accounts, Account account, String authTokenType) {
        synchronized (accounts.cacheLock) {
            Map<String, String> authTokensForAccount = (Map) accounts.authTokenCache.get(account);
            if (authTokensForAccount != null) {
                String str = authTokensForAccount.get(authTokenType);
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
        return userDataForAccount.get(key);
    }

    private Context getContextForUser(UserHandle user) {
        try {
            return this.mContext.createPackageContextAsUser(this.mContext.getPackageName(), 0, user);
        } catch (PackageManager.NameNotFoundException e) {
            return this.mContext;
        }
    }

    /* access modifiers changed from: private */
    public void sendResponse(IAccountManagerResponse response, Bundle result) {
        try {
            response.onResult(result);
        } catch (RemoteException e) {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "failure while notifying response", e);
            }
        }
    }

    /* access modifiers changed from: private */
    public void sendErrorResponse(IAccountManagerResponse response, int errorCode, String errorMessage) {
        try {
            response.onError(errorCode, errorMessage);
        } catch (RemoteException e) {
            if (Log.isLoggable(TAG, 2)) {
                Log.v(TAG, "failure while notifying response", e);
            }
        }
    }
}
