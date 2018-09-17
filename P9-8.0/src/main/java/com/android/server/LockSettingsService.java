package com.android.server;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.app.KeyguardManager;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.app.admin.PasswordMetrics;
import android.app.backup.BackupManager;
import android.app.trust.IStrongAuthTracker;
import android.app.trust.TrustManager;
import android.common.HwFrameworkFactory;
import android.common.HwFrameworkMonitor;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.encrypt.ISDCardCryptedHelper;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.IProgressListener;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.StrictMode;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.IStorageManager;
import android.os.storage.IStorageManager.Stub;
import android.os.storage.StorageManager;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.security.KeyStore;
import android.security.keystore.AndroidKeyStoreProvider;
import android.security.keystore.KeyProtection;
import android.service.gatekeeper.GateKeeperResponse;
import android.service.gatekeeper.IGateKeeperService;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.widget.ICheckCredentialProgressCallback;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternUtils.StrongAuthTracker;
import com.android.internal.widget.VerifyCredentialResponse;
import com.android.server.HwServiceFactory.IHwLockSettingsService;
import com.android.server.LockSettingsStorage.Callback;
import com.android.server.LockSettingsStorage.CredentialHash;
import com.android.server.power.IHwShutdownThread;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import libcore.util.HexEncoding;

public class LockSettingsService extends AbsLockSettingsService {
    private static final Intent ACTION_NULL = new Intent("android.intent.action.MAIN");
    private static final boolean DEBUG = false;
    private static final String PERMISSION = "android.permission.ACCESS_KEYGUARD_SECURE_STORAGE";
    private static final int PROFILE_KEY_IV_SIZE = 12;
    private static final String[] READ_CONTACTS_PROTECTED_SETTINGS = new String[]{"lock_screen_owner_info_enabled", "lock_screen_owner_info"};
    private static final String[] READ_PASSWORD_PROTECTED_SETTINGS = new String[]{"lockscreen.password_salt", "lockscreen.passwordhistory", "lockscreen.password_type", SEPARATE_PROFILE_CHALLENGE_KEY};
    private static final String SEPARATE_PROFILE_CHALLENGE_KEY = "lockscreen.profilechallenge";
    private static final String[] SETTINGS_TO_BACKUP = new String[]{"lock_screen_owner_info_enabled", "lock_screen_owner_info"};
    private static final int[] SYSTEM_CREDENTIAL_UIDS = new int[]{1010, 1016, 0, 1000};
    private static final String TAG = "LockSettingsService";
    private static final String[] VALID_SETTINGS = new String[]{"lockscreen.lockedoutpermanently", "lockscreen.lockoutattemptdeadline", "lockscreen.patterneverchosen", "lockscreen.password_type", "lockscreen.password_type_alternate", "lockscreen.password_salt", "lockscreen.disabled", "lockscreen.options", "lockscreen.biometric_weak_fallback", "lockscreen.biometricweakeverchosen", "lockscreen.power_button_instantly_locks", "lockscreen.passwordhistory", "lock_pattern_autolock", "lock_biometric_weak_flags", "lock_pattern_visible_pattern", "lock_pattern_tactile_feedback_enabled"};
    private static HwFrameworkMonitor mMonitor = HwFrameworkFactory.getHwFrameworkMonitor();
    private final String ACTION_PRIVACY_USER_ADDED_FINISHED;
    private final IActivityManager mActivityManager;
    private final BroadcastReceiver mBroadcastReceiver;
    private final Context mContext;
    private boolean mFirstCallToVold;
    protected IGateKeeperService mGateKeeperService;
    private final Handler mHandler;
    private final Injector mInjector;
    private final KeyStore mKeyStore;
    protected LockPatternUtils mLockPatternUtils;
    private final NotificationManager mNotificationManager;
    private final Object mSeparateChallengeLock;
    private SyntheticPasswordManager mSpManager;
    protected final LockSettingsStorage mStorage;
    private final LockSettingsStrongAuth mStrongAuth;
    private final SynchronizedStrongAuthTracker mStrongAuthTracker;
    private final UserManager mUserManager;

    private class GateKeeperDiedRecipient implements DeathRecipient {
        /* synthetic */ GateKeeperDiedRecipient(LockSettingsService this$0, GateKeeperDiedRecipient -this1) {
            this();
        }

        private GateKeeperDiedRecipient() {
        }

        public void binderDied() {
            LockSettingsService.this.mGateKeeperService.asBinder().unlinkToDeath(this, 0);
            LockSettingsService.this.mGateKeeperService = null;
        }
    }

    static class Injector {
        protected Context mContext;

        public Injector(Context context) {
            this.mContext = context;
        }

        public Context getContext() {
            return this.mContext;
        }

        public Handler getHandler() {
            return new Handler();
        }

        public LockSettingsStorage getStorage() {
            final LockSettingsStorage storage = new LockSettingsStorage(this.mContext);
            storage.setDatabaseOnCreateCallback(new Callback() {
                public void initialize(SQLiteDatabase db) {
                    if (SystemProperties.getBoolean("ro.lockscreen.disable.default", false)) {
                        storage.writeKeyValue(db, "lockscreen.disabled", "1", 0);
                    }
                }
            });
            return storage;
        }

        public LockSettingsStrongAuth getStrongAuth() {
            return new LockSettingsStrongAuth(this.mContext);
        }

        public SynchronizedStrongAuthTracker getStrongAuthTracker() {
            return new SynchronizedStrongAuthTracker(this.mContext);
        }

        public IActivityManager getActivityManager() {
            return ActivityManager.getService();
        }

        public LockPatternUtils getLockPatternUtils() {
            return new LockPatternUtils(this.mContext);
        }

        public NotificationManager getNotificationManager() {
            return (NotificationManager) this.mContext.getSystemService("notification");
        }

        public UserManager getUserManager() {
            return (UserManager) this.mContext.getSystemService("user");
        }

        public DevicePolicyManager getDevicePolicyManager() {
            return (DevicePolicyManager) this.mContext.getSystemService("device_policy");
        }

        public KeyStore getKeyStore() {
            return KeyStore.getInstance();
        }

        public IStorageManager getStorageManager() {
            IBinder service = ServiceManager.getService("mount");
            if (service != null) {
                return Stub.asInterface(service);
            }
            return null;
        }

        public SyntheticPasswordManager getSyntheticPasswordManager(LockSettingsStorage storage) {
            return new SyntheticPasswordManager(storage);
        }

        public int binderGetCallingUid() {
            return Binder.getCallingUid();
        }
    }

    public static final class Lifecycle extends SystemService {
        private LockSettingsService mLockSettingsService;

        public Lifecycle(Context context) {
            super(context);
        }

        public void onStart() {
            AndroidKeyStoreProvider.install();
            IHwLockSettingsService iLockSettingsService = HwServiceFactory.getHuaweiLockSettingsService();
            if (iLockSettingsService != null) {
                this.mLockSettingsService = iLockSettingsService.getInstance(getContext());
            } else {
                this.mLockSettingsService = new LockSettingsService(getContext());
            }
            publishBinderService("lock_settings", this.mLockSettingsService);
        }

        public void onStartUser(int userHandle) {
            this.mLockSettingsService.onStartUser(userHandle);
        }

        public void onUnlockUser(int userHandle) {
            this.mLockSettingsService.onUnlockUser(userHandle);
        }

        public void onBootPhase(int phase) {
            if (phase == 550) {
                this.mLockSettingsService.showEncryptionNotificationForUsers();
            }
        }

        public void onCleanupUser(int userHandle) {
            this.mLockSettingsService.onCleanupUser(userHandle);
        }
    }

    protected static class SynchronizedStrongAuthTracker extends StrongAuthTracker {
        public SynchronizedStrongAuthTracker(Context context) {
            super(context);
        }

        protected void handleStrongAuthRequiredChanged(int strongAuthFlags, int userId) {
            synchronized (this) {
                super.handleStrongAuthRequiredChanged(strongAuthFlags, userId);
            }
        }

        public int getStrongAuthForUser(int userId) {
            int strongAuthForUser;
            synchronized (this) {
                strongAuthForUser = super.getStrongAuthForUser(userId);
            }
            return strongAuthForUser;
        }

        void register(LockSettingsStrongAuth strongAuth) {
            strongAuth.registerStrongAuthTracker(this.mStub);
        }
    }

    static {
        ACTION_NULL.addCategory("android.intent.category.HOME");
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0071 A:{Splitter: B:21:0x004b, ExcHandler: java.security.NoSuchAlgorithmException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:24:0x0071, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:25:0x0072, code:
            android.util.Slog.e(TAG, "Fail to tie managed profile", r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void tieManagedProfileLockIfNecessary(int managedUserId, String managedUserPassword) {
        if (this.mUserManager.getUserInfo(managedUserId).isManagedProfile() && !this.mLockPatternUtils.isSeparateProfileChallengeEnabled(managedUserId) && !this.mStorage.hasChildProfileLock(managedUserId)) {
            int parentId = this.mUserManager.getProfileParent(managedUserId).id;
            if (isUserSecure(parentId)) {
                try {
                    if (getGateKeeperService().getSecureUserId(parentId) != 0) {
                        byte[] randomLockSeed = new byte[0];
                        try {
                            String newPassword = String.valueOf(HexEncoding.encode(SecureRandom.getInstance("SHA1PRNG").generateSeed(40)));
                            setLockCredentialInternal(newPassword, 2, managedUserPassword, managedUserId);
                            setLong("lockscreen.password_type", 327680, managedUserId);
                            tieProfileLockToParent(managedUserId, newPassword);
                        } catch (Exception e) {
                        }
                    }
                } catch (RemoteException e2) {
                    Slog.e(TAG, "Failed to talk to GateKeeper service", e2);
                }
            }
        }
    }

    public LockSettingsService(Context context) {
        this(new Injector(context));
    }

    protected LockSettingsService(Injector injector) {
        this.mSeparateChallengeLock = new Object();
        this.ACTION_PRIVACY_USER_ADDED_FINISHED = "com.huawei.android.lockSettingService.action.USER_ADDED_FINISHED ";
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int userHandle;
                if ("android.intent.action.USER_ADDED".equals(intent.getAction())) {
                    userHandle = intent.getIntExtra("android.intent.extra.user_handle", 0);
                    if (userHandle > 0) {
                        LockSettingsService.this.removeUser(userHandle, true);
                    }
                    KeyStore ks = KeyStore.getInstance();
                    UserInfo parentInfo = LockSettingsService.this.mUserManager.getProfileParent(userHandle);
                    ks.onUserAdded(userHandle, parentInfo != null ? parentInfo.id : -1);
                    UserInfo userInfo = LockSettingsService.this.mUserManager.getUserInfo(userHandle);
                    if (userInfo != null && userInfo.isHwHiddenSpace()) {
                        Intent finishIntent = new Intent("com.huawei.android.lockSettingService.action.USER_ADDED_FINISHED ");
                        finishIntent.putExtra("android.intent.extra.user_handle", userHandle);
                        Slog.d(LockSettingsService.TAG, "notify that hiden user has been added.");
                        LockSettingsService.this.mContext.sendBroadcastAsUser(finishIntent, UserHandle.ALL, "android.permission.MANAGE_USERS");
                    }
                } else if ("android.intent.action.USER_STARTING".equals(intent.getAction())) {
                    LockSettingsService.this.mStorage.prefetchUser(intent.getIntExtra("android.intent.extra.user_handle", 0));
                } else if ("android.intent.action.USER_REMOVED".equals(intent.getAction())) {
                    userHandle = intent.getIntExtra("android.intent.extra.user_handle", 0);
                    if (userHandle > 0) {
                        LockSettingsService.this.removeUser(userHandle, false);
                    }
                }
            }
        };
        this.mInjector = injector;
        this.mContext = injector.getContext();
        this.mKeyStore = injector.getKeyStore();
        this.mHandler = injector.getHandler();
        this.mStrongAuth = injector.getStrongAuth();
        this.mActivityManager = injector.getActivityManager();
        this.mLockPatternUtils = injector.getLockPatternUtils();
        this.mFirstCallToVold = true;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_ADDED");
        filter.addAction("android.intent.action.USER_STARTING");
        filter.addAction("android.intent.action.USER_REMOVED");
        injector.getContext().registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, filter, null, null);
        this.mStorage = injector.getStorage();
        this.mNotificationManager = injector.getNotificationManager();
        this.mUserManager = injector.getUserManager();
        this.mStrongAuthTracker = injector.getStrongAuthTracker();
        this.mStrongAuthTracker.register(this.mStrongAuth);
        this.mSpManager = injector.getSyntheticPasswordManager(this.mStorage);
    }

    private void maybeShowEncryptionNotificationForUser(int userId) {
        UserInfo user = this.mUserManager.getUserInfo(userId);
        if (user.isManagedProfile()) {
            UserHandle userHandle = user.getUserHandle();
            if (isUserSecure(userId) && (this.mUserManager.isUserUnlockingOrUnlocked(userHandle) ^ 1) != 0) {
                UserInfo parent = this.mUserManager.getProfileParent(userId);
                if (!(parent == null || !this.mUserManager.isUserUnlockingOrUnlocked(parent.getUserHandle()) || (this.mUserManager.isQuietModeEnabled(userHandle) ^ 1) == 0)) {
                    showEncryptionNotificationForProfile(userHandle);
                }
            }
        }
    }

    private void showEncryptionNotificationForUsers() {
        List<UserInfo> users = this.mUserManager.getUsers();
        for (int i = 0; i < users.size(); i++) {
            UserInfo user = (UserInfo) users.get(i);
            if (!user.isClonedProfile()) {
                UserHandle userHandle = user.getUserHandle();
                Slog.i(TAG, "user.id = " + user.id);
                if (!(!isUserSecure(user.id) || (this.mUserManager.isUserUnlockingOrUnlocked(userHandle) ^ 1) == 0 || user.isManagedProfile())) {
                    Slog.i(TAG, "has password,show notification");
                    showEncryptionNotificationForUser(userHandle);
                }
            }
        }
    }

    private void showEncryptionNotificationForUser(UserHandle user) {
        Resources r = this.mContext.getResources();
        showEncryptionNotification(user, r.getText(33685896), r.getText(33685897), r.getText(17041170), PendingIntent.getBroadcast(this.mContext, 0, ACTION_NULL, 134217728));
    }

    private void showEncryptionNotificationForProfile(UserHandle user) {
        Resources r = this.mContext.getResources();
        CharSequence title = r.getText(17041172);
        CharSequence message = r.getText(17040842);
        CharSequence detail = r.getText(17040841);
        Intent unlockIntent = ((KeyguardManager) this.mContext.getSystemService("keyguard")).createConfirmDeviceCredentialIntent(null, null, user.getIdentifier());
        if (unlockIntent != null) {
            unlockIntent.setFlags(276824064);
            showEncryptionNotification(user, title, message, detail, PendingIntent.getActivity(this.mContext, 0, unlockIntent, 134217728));
        }
    }

    private void showEncryptionNotification(UserHandle user, CharSequence title, CharSequence message, CharSequence detail, PendingIntent intent) {
        if (StorageManager.isFileEncryptedNativeOrEmulated()) {
            this.mNotificationManager.notifyAsUser(null, 9, new Builder(this.mContext, SystemNotificationChannels.SECURITY).setSmallIcon(17302634).setWhen(0).setOngoing(true).setTicker(title).setColor(this.mContext.getColor(17170769)).setContentTitle(title).setContentText(message).setSubText(detail).setVisibility(1).setContentIntent(intent).build(), user);
        }
    }

    private void hideEncryptionNotification(UserHandle userHandle) {
        this.mNotificationManager.cancelAsUser(null, 9, userHandle);
    }

    public void onCleanupUser(int userId) {
        hideEncryptionNotification(new UserHandle(userId));
    }

    public void onStartUser(int userId) {
        maybeShowEncryptionNotificationForUser(userId);
    }

    public void onUnlockUser(final int userId) {
        this.mHandler.post(new Runnable() {
            public void run() {
                LockSettingsService.this.hideEncryptionNotification(new UserHandle(userId));
                List<UserInfo> profiles = LockSettingsService.this.mUserManager.getProfiles(userId);
                for (int i = 0; i < profiles.size(); i++) {
                    UserInfo profile = (UserInfo) profiles.get(i);
                    if (LockSettingsService.this.isUserSecure(profile.id) && profile.isManagedProfile()) {
                        UserHandle userHandle = profile.getUserHandle();
                        if (!(LockSettingsService.this.mUserManager.isUserUnlockingOrUnlocked(userHandle) || (LockSettingsService.this.mUserManager.isQuietModeEnabled(userHandle) ^ 1) == 0)) {
                            LockSettingsService.this.showEncryptionNotificationForProfile(userHandle);
                        }
                    }
                }
                if (LockSettingsService.this.mUserManager.getUserInfo(userId).isManagedProfile()) {
                    LockSettingsService.this.tieManagedProfileLockIfNecessary(userId, null);
                }
            }
        });
    }

    public void systemReady() {
        migrateOldData();
        try {
            getGateKeeperService();
        } catch (RemoteException e) {
            Slog.e(TAG, "Failure retrieving IGateKeeperService", e);
        }
        this.mStorage.prefetchUser(0);
    }

    /* JADX WARNING: Removed duplicated region for block: B:88:0x0363 A:{Splitter: B:79:0x0302, ExcHandler: java.security.KeyStoreException (r13_0 'e' java.lang.Exception), Catch:{ RemoteException -> 0x0131 }} */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0363 A:{Splitter: B:79:0x0302, ExcHandler: java.security.KeyStoreException (r13_0 'e' java.lang.Exception), Catch:{ RemoteException -> 0x0131 }} */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x0363 A:{Splitter: B:79:0x0302, ExcHandler: java.security.KeyStoreException (r13_0 'e' java.lang.Exception), Catch:{ RemoteException -> 0x0131 }} */
    /* JADX WARNING: Missing block: B:88:0x0363, code:
            r13 = move-exception;
     */
    /* JADX WARNING: Missing block: B:89:0x0364, code:
            android.util.Slog.e(TAG, "Unable to remove tied profile key", r13);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void migrateOldData() {
        try {
            ContentResolver cr;
            List<UserInfo> users;
            int userId;
            int i;
            int userCount;
            if (getString("migrated", null, 0) == null) {
                cr = this.mContext.getContentResolver();
                for (String validSetting : VALID_SETTINGS) {
                    String value = Secure.getString(cr, validSetting);
                    if (value != null) {
                        setString(validSetting, value, 0);
                    }
                }
                setString("migrated", "true", 0);
                Slog.i(TAG, "Migrated lock settings to new location");
            }
            if (getString("migrated_user_specific", null, 0) == null) {
                cr = this.mContext.getContentResolver();
                users = this.mUserManager.getUsers();
                for (int user = 0; user < users.size(); user++) {
                    userId = ((UserInfo) users.get(user)).id;
                    String OWNER_INFO = "lock_screen_owner_info";
                    String ownerInfo = Secure.getStringForUser(cr, "lock_screen_owner_info", userId);
                    if (!TextUtils.isEmpty(ownerInfo)) {
                        setString("lock_screen_owner_info", ownerInfo, userId);
                        Secure.putStringForUser(cr, "lock_screen_owner_info", "", userId);
                    }
                    String OWNER_INFO_ENABLED = "lock_screen_owner_info_enabled";
                    try {
                        setLong("lock_screen_owner_info_enabled", (long) (Secure.getIntForUser(cr, "lock_screen_owner_info_enabled", userId) != 0 ? 1 : 0), userId);
                    } catch (SettingNotFoundException e) {
                        if (!TextUtils.isEmpty(ownerInfo)) {
                            setLong("lock_screen_owner_info_enabled", 1, userId);
                        }
                    }
                    Secure.putIntForUser(cr, "lock_screen_owner_info_enabled", 0, userId);
                }
                setString("migrated_user_specific", "true", 0);
                Slog.i(TAG, "Migrated per-user lock settings to new location");
            }
            if (getString("migrated_biometric_weak", null, 0) == null) {
                users = this.mUserManager.getUsers();
                for (i = 0; i < users.size(); i++) {
                    userId = ((UserInfo) users.get(i)).id;
                    long type = getLong("lockscreen.password_type", 0, userId);
                    long alternateType = getLong("lockscreen.password_type_alternate", 0, userId);
                    if (type == 32768) {
                        setLong("lockscreen.password_type", alternateType, userId);
                    }
                    setLong("lockscreen.password_type_alternate", 0, userId);
                }
                setString("migrated_biometric_weak", "true", 0);
                Slog.i(TAG, "Migrated biometric weak to use the fallback instead");
            }
            if (getString("migrated_lockscreen_disabled", null, 0) == null) {
                users = this.mUserManager.getUsers();
                userCount = users.size();
                int switchableUsers = 0;
                for (i = 0; i < userCount; i++) {
                    if (((UserInfo) users.get(i)).supportsSwitchTo()) {
                        switchableUsers++;
                    }
                }
                if (switchableUsers > 1) {
                    for (i = 0; i < userCount; i++) {
                        int id = ((UserInfo) users.get(i)).id;
                        if (getBoolean("lockscreen.disabled", false, id)) {
                            setBoolean("lockscreen.disabled", false, id);
                        }
                    }
                }
                setString("migrated_lockscreen_disabled", "true", 0);
                Slog.i(TAG, "Migrated lockscreen disabled flag");
            }
            users = this.mUserManager.getUsers();
            for (i = 0; i < users.size(); i++) {
                UserInfo userInfo = (UserInfo) users.get(i);
                if (userInfo.isManagedProfile() && this.mStorage.hasChildProfileLock(userInfo.id)) {
                    long quality = getLong("lockscreen.password_type", 0, userInfo.id);
                    if (quality == 0) {
                        Slog.i(TAG, "Migrated tied profile lock type");
                        setLong("lockscreen.password_type", 327680, userInfo.id);
                    } else if (quality != 327680) {
                        Slog.e(TAG, "Invalid tied profile lock type: " + quality);
                    }
                }
                try {
                    String alias = "profile_key_name_encrypt_" + userInfo.id;
                    java.security.KeyStore keyStore = java.security.KeyStore.getInstance("AndroidKeyStore");
                    keyStore.load(null);
                    if (keyStore.containsAlias(alias)) {
                        keyStore.deleteEntry(alias);
                    }
                } catch (Exception e2) {
                }
            }
            if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.watch") && getString("migrated_wear_lockscreen_disabled", null, 0) == null) {
                userCount = users.size();
                for (i = 0; i < userCount; i++) {
                    setBoolean("lockscreen.disabled", false, ((UserInfo) users.get(i)).id);
                }
                setString("migrated_wear_lockscreen_disabled", "true", 0);
                Slog.i(TAG, "Migrated lockscreen_disabled for Wear devices");
            }
        } catch (Throwable re) {
            Slog.e(TAG, "Unable to migrate old data", re);
        }
    }

    protected final void checkWritePermission(int userId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, "LockSettingsWrite");
    }

    protected final void checkPasswordReadPermission(int userId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, "LockSettingsRead");
    }

    private final void checkReadPermission(String requestedKey, int userId) {
        int callingUid = Binder.getCallingUid();
        int i = 0;
        while (i < READ_CONTACTS_PROTECTED_SETTINGS.length) {
            if (!READ_CONTACTS_PROTECTED_SETTINGS[i].equals(requestedKey) || this.mContext.checkCallingOrSelfPermission("android.permission.READ_CONTACTS") == 0) {
                i++;
            } else {
                throw new SecurityException("uid=" + callingUid + " needs permission " + "android.permission.READ_CONTACTS" + " to read " + requestedKey + " for user " + userId);
            }
        }
        i = 0;
        while (i < READ_PASSWORD_PROTECTED_SETTINGS.length) {
            if (!READ_PASSWORD_PROTECTED_SETTINGS[i].equals(requestedKey) || this.mContext.checkCallingOrSelfPermission(PERMISSION) == 0) {
                i++;
            } else {
                throw new SecurityException("uid=" + callingUid + " needs permission " + PERMISSION + " to read " + requestedKey + " for user " + userId);
            }
        }
    }

    public boolean getSeparateProfileChallengeEnabled(int userId) throws RemoteException {
        boolean z;
        checkReadPermission(SEPARATE_PROFILE_CHALLENGE_KEY, userId);
        synchronized (this.mSeparateChallengeLock) {
            z = getBoolean(SEPARATE_PROFILE_CHALLENGE_KEY, false, userId);
        }
        return z;
    }

    public void setSeparateProfileChallengeEnabled(int userId, boolean enabled, String managedUserPassword) throws RemoteException {
        checkWritePermission(userId);
        synchronized (this.mSeparateChallengeLock) {
            setBoolean(SEPARATE_PROFILE_CHALLENGE_KEY, enabled, userId);
            if (enabled) {
                this.mStorage.removeChildProfileLock(userId);
                removeKeystoreProfileKey(userId);
            } else {
                tieManagedProfileLockIfNecessary(userId, managedUserPassword);
            }
        }
    }

    public void setBoolean(String key, boolean value, int userId) throws RemoteException {
        checkWritePermission(userId);
        setStringUnchecked(key, userId, value ? "1" : "0");
    }

    public void setLong(String key, long value, int userId) throws RemoteException {
        checkWritePermission(userId);
        setStringUnchecked(key, userId, Long.toString(value));
    }

    public void setString(String key, String value, int userId) throws RemoteException {
        checkWritePermission(userId);
        setStringUnchecked(key, userId, value);
    }

    private void setStringUnchecked(String key, int userId, String value) {
        this.mStorage.writeKeyValue(key, value, userId);
        if (ArrayUtils.contains(SETTINGS_TO_BACKUP, key)) {
            BackupManager.dataChanged("com.android.providers.settings");
        }
    }

    public boolean getBoolean(String key, boolean defaultValue, int userId) throws RemoteException {
        checkReadPermission(key, userId);
        String value = getStringUnchecked(key, null, userId);
        if (TextUtils.isEmpty(value)) {
            return defaultValue;
        }
        return !value.equals("1") ? value.equals("true") : true;
    }

    public long getLong(String key, long defaultValue, int userId) throws RemoteException {
        checkReadPermission(key, userId);
        String value = getStringUnchecked(key, null, userId);
        return TextUtils.isEmpty(value) ? defaultValue : Long.parseLong(value);
    }

    public String getString(String key, String defaultValue, int userId) throws RemoteException {
        checkReadPermission(key, userId);
        return getStringUnchecked(key, defaultValue, userId);
    }

    public String getStringUnchecked(String key, String defaultValue, int userId) {
        if ("lock_pattern_autolock".equals(key)) {
            long ident = Binder.clearCallingIdentity();
            try {
                String str = this.mLockPatternUtils.isLockPatternEnabled(userId) ? "1" : "0";
                Binder.restoreCallingIdentity(ident);
                return str;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            if ("legacy_lock_pattern_enabled".equals(key)) {
                key = "lock_pattern_autolock";
            }
            return this.mStorage.readKeyValue(key, defaultValue, userId);
        }
    }

    /* JADX WARNING: Missing block: B:10:0x0018, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean havePassword(int userId) throws RemoteException {
        synchronized (this.mSpManager) {
            if (isSyntheticPasswordBasedCredentialLocked(userId)) {
                boolean z = this.mSpManager.getCredentialType(getSyntheticPasswordHandleLocked(userId), userId) == 2;
            } else {
                return this.mStorage.hasPassword(userId);
            }
        }
    }

    /* JADX WARNING: Missing block: B:8:0x0017, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean havePattern(int userId) throws RemoteException {
        boolean z = true;
        synchronized (this.mSpManager) {
            if (isSyntheticPasswordBasedCredentialLocked(userId)) {
                if (this.mSpManager.getCredentialType(getSyntheticPasswordHandleLocked(userId), userId) != 1) {
                    z = false;
                }
            } else {
                return this.mStorage.hasPattern(userId);
            }
        }
    }

    /* JADX WARNING: Missing block: B:10:0x0018, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isUserSecure(int userId) {
        synchronized (this.mSpManager) {
            try {
                if (isSyntheticPasswordBasedCredentialLocked(userId)) {
                    boolean z = this.mSpManager.getCredentialType(getSyntheticPasswordHandleLocked(userId), userId) != -1;
                }
            } catch (RemoteException e) {
            }
        }
        return this.mStorage.hasCredential(userId);
    }

    protected void setKeystorePassword(String password, int userHandle) {
        KeyStore.getInstance().onUserPasswordChanged(userHandle, password);
    }

    private void unlockKeystore(String password, int userHandle) {
        KeyStore.getInstance().unlock(userHandle, password);
    }

    protected String getDecryptedPasswordForTiedProfile(int userId) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, CertificateException, IOException {
        byte[] storedData = this.mStorage.readChildProfileLock(userId);
        if (storedData == null) {
            throw new FileNotFoundException("Child profile lock file not found");
        }
        byte[] iv = Arrays.copyOfRange(storedData, 0, 12);
        byte[] encryptedPassword = Arrays.copyOfRange(storedData, 12, storedData.length);
        java.security.KeyStore keyStore = java.security.KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        SecretKey decryptionKey = (SecretKey) keyStore.getKey("profile_key_name_decrypt_" + userId, null);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(2, decryptionKey, new GCMParameterSpec(128, iv));
        return new String(cipher.doFinal(encryptedPassword), StandardCharsets.UTF_8);
    }

    /* JADX WARNING: Removed duplicated region for block: B:2:0x000f A:{Splitter: B:0:0x0000, ExcHandler: java.security.UnrecoverableKeyException (r8_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:2:0x000f A:{Splitter: B:0:0x0000, ExcHandler: java.security.UnrecoverableKeyException (r8_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:2:0x000f A:{Splitter: B:0:0x0000, ExcHandler: java.security.UnrecoverableKeyException (r8_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:2:0x000f A:{Splitter: B:0:0x0000, ExcHandler: java.security.UnrecoverableKeyException (r8_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:2:0x000f A:{Splitter: B:0:0x0000, ExcHandler: java.security.UnrecoverableKeyException (r8_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:2:0x000f A:{Splitter: B:0:0x0000, ExcHandler: java.security.UnrecoverableKeyException (r8_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:2:0x000f A:{Splitter: B:0:0x0000, ExcHandler: java.security.UnrecoverableKeyException (r8_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:2:0x000f A:{Splitter: B:0:0x0000, ExcHandler: java.security.UnrecoverableKeyException (r8_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:2:0x000f A:{Splitter: B:0:0x0000, ExcHandler: java.security.UnrecoverableKeyException (r8_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:2:0x000f, code:
            r8 = move-exception;
     */
    /* JADX WARNING: Missing block: B:4:0x0012, code:
            if ((r8 instanceof java.io.FileNotFoundException) != false) goto L_0x0014;
     */
    /* JADX WARNING: Missing block: B:5:0x0014, code:
            android.util.Slog.i(TAG, "Child profile key not found");
     */
    /* JADX WARNING: Missing block: B:6:0x001e, code:
            android.util.Slog.e(TAG, "Failed to decrypt child profile key", r8);
     */
    /* JADX WARNING: Missing block: B:8:?, code:
            return;
     */
    /* JADX WARNING: Missing block: B:9:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void unlockChildProfile(int profileHandle) throws RemoteException {
        try {
            doVerifyCredential(getDecryptedPasswordForTiedProfile(profileHandle), 2, false, 0, profileHandle, null);
        } catch (Exception e) {
        }
    }

    private void unlockUser(int userId, byte[] token, byte[] secret) {
        int i = 1;
        final CountDownLatch latch = new CountDownLatch(1);
        try {
            this.mActivityManager.unlockUser(userId, token, secret, new IProgressListener.Stub() {
                public void onStarted(int id, Bundle extras) throws RemoteException {
                    Log.d(LockSettingsService.TAG, "unlockUser started");
                }

                public void onProgress(int id, int progress, Bundle extras) throws RemoteException {
                    Log.d(LockSettingsService.TAG, "unlockUser progress " + progress);
                }

                public void onFinished(int id, Bundle extras) throws RemoteException {
                    Log.d(LockSettingsService.TAG, "unlockUser finished");
                    latch.countDown();
                }
            });
            try {
                latch.await(15, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            try {
                UserInfo ui = this.mUserManager.getUserInfo(userId);
                if (ui != null && ui.isClonedProfile() && this.mStorage.hasChildProfileLock(userId)) {
                    synchronized (this.mSeparateChallengeLock) {
                        clearUserKeyProtection(userId);
                        getGateKeeperService().clearSecureUserId(userId);
                        this.mStorage.writeCredentialHash(CredentialHash.createEmptyHash(), userId);
                        setKeystorePassword(null, userId);
                        fixateNewestUserKeyAuth(userId);
                        this.mStorage.removeChildProfileLock(userId);
                        removeKeystoreProfileKey(userId);
                        Slog.i(TAG, "finish unlock clone user after ota and remove profile key");
                    }
                }
                if (ui != null) {
                    if (!ui.isManagedProfile()) {
                        i = ui.isClonedProfile();
                    }
                    if ((i ^ 1) != 0) {
                        for (UserInfo pi : this.mUserManager.getProfiles(userId)) {
                            if (((pi.isManagedProfile() && (this.mLockPatternUtils.isSeparateProfileChallengeEnabled(pi.id) ^ 1) != 0) || pi.isClonedProfile()) && this.mStorage.hasChildProfileLock(pi.id) && this.mUserManager.isUserRunning(pi.id)) {
                                unlockChildProfile(pi.id);
                            }
                        }
                    }
                }
            } catch (RemoteException e2) {
                Log.d(TAG, "Failed to unlock child profile", e2);
            }
        } catch (RemoteException e22) {
            throw e22.rethrowAsRuntimeException();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0045 A:{Splitter: B:11:0x0039, ExcHandler: java.security.KeyStoreException (e java.security.KeyStoreException)} */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0045 A:{Splitter: B:11:0x0039, ExcHandler: java.security.KeyStoreException (e java.security.KeyStoreException)} */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0045 A:{Splitter: B:11:0x0039, ExcHandler: java.security.KeyStoreException (e java.security.KeyStoreException)} */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0045 A:{Splitter: B:11:0x0039, ExcHandler: java.security.KeyStoreException (e java.security.KeyStoreException)} */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0045 A:{Splitter: B:11:0x0039, ExcHandler: java.security.KeyStoreException (e java.security.KeyStoreException)} */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0045 A:{Splitter: B:11:0x0039, ExcHandler: java.security.KeyStoreException (e java.security.KeyStoreException)} */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0045 A:{Splitter: B:11:0x0039, ExcHandler: java.security.KeyStoreException (e java.security.KeyStoreException)} */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0045 A:{Splitter: B:11:0x0039, ExcHandler: java.security.KeyStoreException (e java.security.KeyStoreException)} */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0045 A:{Splitter: B:11:0x0039, ExcHandler: java.security.KeyStoreException (e java.security.KeyStoreException)} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Map<Integer, String> getDecryptedPasswordsForAllTiedProfiles(int userId) {
        if (this.mUserManager.getUserInfo(userId).isManagedProfile()) {
            return null;
        }
        Map<Integer, String> result = new ArrayMap();
        List<UserInfo> profiles = this.mUserManager.getProfiles(userId);
        int size = profiles.size();
        for (int i = 0; i < size; i++) {
            UserInfo profile = (UserInfo) profiles.get(i);
            if (profile.isManagedProfile()) {
                if (!this.mLockPatternUtils.isSeparateProfileChallengeEnabled(profile.id)) {
                    try {
                        result.put(Integer.valueOf(userId), getDecryptedPasswordForTiedProfile(userId));
                    } catch (KeyStoreException e) {
                    }
                }
            }
        }
        return result;
    }

    private void synchronizeUnifiedWorkChallengeForProfiles(int userId, Map<Integer, String> profilePasswordMap) throws RemoteException {
        if (this.mUserManager.getUserInfo(userId) == null || !this.mUserManager.getUserInfo(userId).isManagedProfile()) {
            boolean isSecure = isUserSecure(userId);
            List<UserInfo> profiles = this.mUserManager.getProfiles(userId);
            int size = profiles.size();
            for (int i = 0; i < size; i++) {
                UserInfo profile = (UserInfo) profiles.get(i);
                if (profile.isManagedProfile()) {
                    int managedUserId = profile.id;
                    if (!this.mLockPatternUtils.isSeparateProfileChallengeEnabled(managedUserId)) {
                        if (isSecure) {
                            tieManagedProfileLockIfNecessary(managedUserId, null);
                        } else {
                            if (profilePasswordMap == null || !profilePasswordMap.containsKey(Integer.valueOf(managedUserId))) {
                                Slog.wtf(TAG, "clear tied profile challenges, but no password supplied.");
                                setLockCredentialInternal(null, -1, null, managedUserId);
                            } else {
                                setLockCredentialInternal(null, -1, (String) profilePasswordMap.get(Integer.valueOf(managedUserId)), managedUserId);
                            }
                            this.mStorage.removeChildProfileLock(managedUserId);
                            removeKeystoreProfileKey(managedUserId);
                        }
                    }
                }
            }
        }
    }

    private boolean isManagedProfileWithUnifiedLock(int userId) {
        if (this.mUserManager.getUserInfo(userId) == null || !this.mUserManager.getUserInfo(userId).isManagedProfile()) {
            return false;
        }
        return this.mLockPatternUtils.isSeparateProfileChallengeEnabled(userId) ^ 1;
    }

    private boolean isManagedProfileWithSeparatedLock(int userId) {
        if (this.mUserManager.getUserInfo(userId) == null || !this.mUserManager.getUserInfo(userId).isManagedProfile()) {
            return false;
        }
        return this.mLockPatternUtils.isSeparateProfileChallengeEnabled(userId);
    }

    public void setLockCredential(String credential, int type, String savedCredential, int userId) throws RemoteException {
        checkWritePermission(userId);
        synchronized (this.mSeparateChallengeLock) {
            int oldCredentialType = getOldCredentialType(userId);
            setLockCredentialInternal(credential, type, savedCredential, userId);
            setSeparateProfileChallengeEnabled(userId, true, null);
            notifyPasswordChanged(userId);
            notifyPasswordStatusChanged(userId, getPasswordStatus(type, oldCredentialType));
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:40:0x00c8 A:{Splitter: B:30:0x009f, ExcHandler: java.security.UnrecoverableKeyException (r14_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00c8 A:{Splitter: B:30:0x009f, ExcHandler: java.security.UnrecoverableKeyException (r14_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00c8 A:{Splitter: B:30:0x009f, ExcHandler: java.security.UnrecoverableKeyException (r14_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00c8 A:{Splitter: B:30:0x009f, ExcHandler: java.security.UnrecoverableKeyException (r14_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00c8 A:{Splitter: B:30:0x009f, ExcHandler: java.security.UnrecoverableKeyException (r14_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00c8 A:{Splitter: B:30:0x009f, ExcHandler: java.security.UnrecoverableKeyException (r14_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00c8 A:{Splitter: B:30:0x009f, ExcHandler: java.security.UnrecoverableKeyException (r14_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00c8 A:{Splitter: B:30:0x009f, ExcHandler: java.security.UnrecoverableKeyException (r14_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00c8 A:{Splitter: B:30:0x009f, ExcHandler: java.security.UnrecoverableKeyException (r14_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:16:0x0028, code:
            if (r20 != -1) goto L_0x007e;
     */
    /* JADX WARNING: Missing block: B:17:0x002a, code:
            if (r19 == null) goto L_0x0035;
     */
    /* JADX WARNING: Missing block: B:18:0x002c, code:
            android.util.Slog.wtf(TAG, "CredentialType is none, but credential is non-null.");
     */
    /* JADX WARNING: Missing block: B:19:0x0035, code:
            clearUserKeyProtection(r22);
            getGateKeeperService().clearSecureUserId(r22);
            r18.mStorage.writeCredentialHash(com.android.server.LockSettingsStorage.CredentialHash.createEmptyHash(), r22);
            setKeystorePassword(null, r22);
            fixateNewestUserKeyAuth(r22);
            synchronizeUnifiedWorkChallengeForProfiles(r22, null);
            notifyActivePasswordMetricsAvailable(null, r22);
            android.util.Slog.w(TAG, "setLockPattern to null success");
     */
    /* JADX WARNING: Missing block: B:20:0x007a, code:
            return;
     */
    /* JADX WARNING: Missing block: B:24:0x007e, code:
            if (r19 != null) goto L_0x0089;
     */
    /* JADX WARNING: Missing block: B:26:0x0088, code:
            throw new android.os.RemoteException("Null credential with mismatched credential type");
     */
    /* JADX WARNING: Missing block: B:27:0x0089, code:
            r12 = r18.mStorage.readCredentialHash(r22);
     */
    /* JADX WARNING: Missing block: B:28:0x009b, code:
            if (isManagedProfileWithUnifiedLock(r22) == false) goto L_0x00de;
     */
    /* JADX WARNING: Missing block: B:29:0x009d, code:
            if (r21 != null) goto L_0x00a7;
     */
    /* JADX WARNING: Missing block: B:31:?, code:
            r21 = getDecryptedPasswordForTiedProfile(r22);
     */
    /* JADX WARNING: Missing block: B:40:0x00c8, code:
            r14 = move-exception;
     */
    /* JADX WARNING: Missing block: B:41:0x00c9, code:
            android.util.Slog.e(TAG, "Failed to decrypt child profile key", r14);
     */
    /* JADX WARNING: Missing block: B:43:0x00d4, code:
            android.util.Slog.i(TAG, "Child profile key not found");
     */
    /* JADX WARNING: Missing block: B:45:0x00e0, code:
            if (r12.hash != null) goto L_0x00a7;
     */
    /* JADX WARNING: Missing block: B:46:0x00e2, code:
            if (r21 == null) goto L_0x00ed;
     */
    /* JADX WARNING: Missing block: B:47:0x00e4, code:
            android.util.Slog.w(TAG, "Saved credential provided, but none stored");
     */
    /* JADX WARNING: Missing block: B:48:0x00ed, code:
            r21 = null;
     */
    /* JADX WARNING: Missing block: B:50:0x00f1, code:
            r15 = enrollCredential(r12.hash, r21, r19, r22);
     */
    /* JADX WARNING: Missing block: B:51:0x00ff, code:
            if (r15 == null) goto L_0x0162;
     */
    /* JADX WARNING: Missing block: B:52:0x0101, code:
            r17 = com.android.server.LockSettingsStorage.CredentialHash.create(r15, r20);
            r18.mStorage.writeCredentialHash(r17, r22);
            setUserKeyProtection(r22, r19, convertResponse(getGateKeeperService().verifyChallenge(r22, 0, r17.hash, r19.getBytes())));
            fixateNewestUserKeyAuth(r22);
            doVerifyCredential(r19, r20, true, 0, r22, null);
            synchronizeUnifiedWorkChallengeForProfiles(r22, null);
            android.util.Slog.w(TAG, "set new LockPassword success");
     */
    /* JADX WARNING: Missing block: B:53:0x015e, code:
            return;
     */
    /* JADX WARNING: Missing block: B:57:0x0162, code:
            android.util.Slog.e(TAG, "Failed to enroll password");
            r6 = new java.lang.StringBuilder().append("Failed to enroll ");
     */
    /* JADX WARNING: Missing block: B:58:0x017c, code:
            if (r20 != 2) goto L_0x018d;
     */
    /* JADX WARNING: Missing block: B:59:0x017e, code:
            r4 = "password";
     */
    /* JADX WARNING: Missing block: B:61:0x018c, code:
            throw new android.os.RemoteException(r6.append(r4).toString());
     */
    /* JADX WARNING: Missing block: B:62:0x018d, code:
            r4 = "pattern";
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setLockCredentialInternal(String credential, int credentialType, String savedCredential, int userId) throws RemoteException {
        if (TextUtils.isEmpty(savedCredential)) {
            savedCredential = null;
        }
        if (TextUtils.isEmpty(credential)) {
            credential = null;
        }
        synchronized (this.mSpManager) {
            if (isSyntheticPasswordBasedCredentialLocked(userId)) {
                spBasedSetLockCredentialInternalLocked(credential, credentialType, savedCredential, userId);
                return;
            }
        }
        synchronized (this.mSpManager) {
            if (shouldMigrateToSyntheticPasswordLocked(userId)) {
                initializeSyntheticPasswordLocked(currentHandle.hash, savedCredential, currentHandle.type, userId);
                spBasedSetLockCredentialInternalLocked(credential, credentialType, savedCredential, userId);
            }
        }
    }

    private VerifyCredentialResponse convertResponse(GateKeeperResponse gateKeeperResponse) {
        int responseCode = gateKeeperResponse.getResponseCode();
        if (responseCode == 1) {
            return new VerifyCredentialResponse(gateKeeperResponse.getTimeout());
        }
        if (responseCode != 0) {
            return VerifyCredentialResponse.ERROR;
        }
        byte[] token = gateKeeperResponse.getPayload();
        if (token != null) {
            return new VerifyCredentialResponse(token);
        }
        Slog.e(TAG, "verifyChallenge response had no associated payload");
        this.mLockPatternUtils.monitorCheckPassword(1007, null);
        return VerifyCredentialResponse.ERROR;
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0151 A:{Splitter: B:1:0x0008, ExcHandler: java.security.cert.CertificateException (r3_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0151 A:{Splitter: B:1:0x0008, ExcHandler: java.security.cert.CertificateException (r3_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0151 A:{Splitter: B:1:0x0008, ExcHandler: java.security.cert.CertificateException (r3_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0151 A:{Splitter: B:1:0x0008, ExcHandler: java.security.cert.CertificateException (r3_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0151 A:{Splitter: B:1:0x0008, ExcHandler: java.security.cert.CertificateException (r3_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0151 A:{Splitter: B:1:0x0008, ExcHandler: java.security.cert.CertificateException (r3_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0151 A:{Splitter: B:1:0x0008, ExcHandler: java.security.cert.CertificateException (r3_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0151 A:{Splitter: B:1:0x0008, ExcHandler: java.security.cert.CertificateException (r3_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:20:0x0151, code:
            r3 = move-exception;
     */
    /* JADX WARNING: Missing block: B:22:0x015a, code:
            throw new java.lang.RuntimeException("Failed to encrypt key", r3);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void tieProfileLockToParent(int userId, String password) {
        byte[] randomLockSeed = password.getBytes(StandardCharsets.UTF_8);
        java.security.KeyStore keyStore;
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();
            keyStore = java.security.KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            keyStore.setEntry("profile_key_name_encrypt_" + userId, new SecretKeyEntry(secretKey), new KeyProtection.Builder(1).setBlockModes(new String[]{"GCM"}).setEncryptionPaddings(new String[]{"NoPadding"}).build());
            keyStore.setEntry("profile_key_name_decrypt_" + userId, new SecretKeyEntry(secretKey), new KeyProtection.Builder(2).setBlockModes(new String[]{"GCM"}).setEncryptionPaddings(new String[]{"NoPadding"}).setUserAuthenticationRequired(true).setUserAuthenticationValidityDurationSeconds(30).setCriticalToDeviceEncryption(true).build());
            SecretKey keyStoreEncryptionKey = (SecretKey) keyStore.getKey("profile_key_name_encrypt_" + userId, null);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(1, keyStoreEncryptionKey);
            byte[] encryptionResult = cipher.doFinal(randomLockSeed);
            byte[] iv = cipher.getIV();
            keyStore.deleteEntry("profile_key_name_encrypt_" + userId);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                if (iv.length != 12) {
                    throw new RuntimeException("Invalid iv length: " + iv.length);
                }
                outputStream.write(iv);
                outputStream.write(encryptionResult);
                this.mStorage.writeChildProfileLock(userId, outputStream.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException("Failed to concatenate byte arrays", e);
            }
        } catch (Exception e2) {
        } catch (Throwable th) {
            keyStore.deleteEntry("profile_key_name_encrypt_" + userId);
        }
    }

    private byte[] enrollCredential(byte[] enrolledHandle, String enrolledCredential, String toEnroll, int userId) throws RemoteException {
        byte[] enrolledCredentialBytes;
        byte[] toEnrollBytes;
        checkWritePermission(userId);
        if (enrolledCredential == null) {
            enrolledCredentialBytes = null;
        } else {
            enrolledCredentialBytes = enrolledCredential.getBytes();
        }
        if (toEnroll == null) {
            toEnrollBytes = null;
        } else {
            toEnrollBytes = toEnroll.getBytes();
        }
        GateKeeperResponse response = getGateKeeperService().enroll(userId, enrolledHandle, enrolledCredentialBytes, toEnrollBytes);
        if (response == null) {
            Slog.w(TAG, "enrollCredential response null");
            return null;
        }
        byte[] hash = response.getPayload();
        if (hash != null) {
            setKeystorePassword(toEnroll, userId);
            Slog.w(TAG, "enrollCredential response success");
        } else {
            Slog.e(TAG, "Throttled while enrolling a password");
        }
        return hash;
    }

    private void setAuthlessUserKeyProtection(int userId, byte[] key) throws RemoteException {
        addUserKeyAuth(userId, null, key);
    }

    private void setUserKeyProtection(int userId, String credential, VerifyCredentialResponse vcr) throws RemoteException {
        if (vcr == null) {
            throw new RemoteException("Null response verifying a credential we just set");
        } else if (vcr.getResponseCode() != 0) {
            throw new RemoteException("Non-OK response verifying a credential we just set: " + vcr.getResponseCode());
        } else {
            byte[] token = vcr.getPayload();
            if (token == null) {
                throw new RemoteException("Empty payload verifying a credential we just set");
            }
            addUserKeyAuth(userId, token, secretFromCredential(credential));
        }
    }

    private void clearUserKeyProtection(int userId) throws RemoteException {
        addUserKeyAuth(userId, null, null);
    }

    private static byte[] secretFromCredential(String credential) throws RemoteException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.update(Arrays.copyOf("Android FBE credential hash".getBytes(StandardCharsets.UTF_8), 128));
            digest.update(credential.getBytes(StandardCharsets.UTF_8));
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException for SHA-512");
        }
    }

    private void addUserKeyAuth(int userId, byte[] token, byte[] secret) throws RemoteException {
        UserInfo userInfo = this.mUserManager.getUserInfo(userId);
        IStorageManager storageManager = this.mInjector.getStorageManager();
        long callingId = Binder.clearCallingIdentity();
        try {
            storageManager.addUserKeyAuth(userId, userInfo.serialNumber, token, secret);
            ISDCardCryptedHelper helper = HwServiceFactory.getSDCardCryptedHelper();
            if (helper != null) {
                helper.addUserKeyAuth(userId, userInfo.serialNumber, token, secret);
            }
            Binder.restoreCallingIdentity(callingId);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    private void fixateNewestUserKeyAuth(int userId) throws RemoteException {
        if (2147483646 != userId) {
            IStorageManager storageManager = this.mInjector.getStorageManager();
            long callingId = Binder.clearCallingIdentity();
            try {
                storageManager.fixateNewestUserKeyAuth(userId);
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0075 A:{Splitter: B:11:0x0042, ExcHandler: java.security.UnrecoverableKeyException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0075 A:{Splitter: B:11:0x0042, ExcHandler: java.security.UnrecoverableKeyException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0075 A:{Splitter: B:11:0x0042, ExcHandler: java.security.UnrecoverableKeyException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0075 A:{Splitter: B:11:0x0042, ExcHandler: java.security.UnrecoverableKeyException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0075 A:{Splitter: B:11:0x0042, ExcHandler: java.security.UnrecoverableKeyException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0075 A:{Splitter: B:11:0x0042, ExcHandler: java.security.UnrecoverableKeyException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0075 A:{Splitter: B:11:0x0042, ExcHandler: java.security.UnrecoverableKeyException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0075 A:{Splitter: B:11:0x0042, ExcHandler: java.security.UnrecoverableKeyException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0075 A:{Splitter: B:11:0x0042, ExcHandler: java.security.UnrecoverableKeyException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:14:0x0075, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:15:0x0076, code:
            android.util.Slog.e(TAG, "Failed to decrypt child profile key", r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void resetKeyStore(int userId) throws RemoteException {
        checkWritePermission(userId);
        int managedUserId = -1;
        String managedUserDecryptedPassword = null;
        for (UserInfo pi : this.mUserManager.getProfiles(userId)) {
            if (pi.isManagedProfile() && (this.mLockPatternUtils.isSeparateProfileChallengeEnabled(pi.id) ^ 1) != 0 && this.mStorage.hasChildProfileLock(pi.id)) {
                if (managedUserId == -1) {
                    try {
                        managedUserDecryptedPassword = getDecryptedPasswordForTiedProfile(pi.id);
                        managedUserId = pi.id;
                    } catch (Exception e) {
                    }
                } else {
                    Slog.e(TAG, "More than one managed profile, uid1:" + managedUserId + ", uid2:" + pi.id);
                }
            }
        }
        try {
            for (int profileId : this.mUserManager.getProfileIdsWithDisabled(userId)) {
                for (int uid : SYSTEM_CREDENTIAL_UIDS) {
                    this.mKeyStore.clearUid(UserHandle.getUid(profileId, uid));
                }
            }
        } finally {
            if (!(managedUserId == -1 || managedUserDecryptedPassword == null)) {
                tieProfileLockToParent(managedUserId, managedUserDecryptedPassword);
            }
        }
    }

    public VerifyCredentialResponse checkCredential(String credential, int type, int userId, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        checkPasswordReadPermission(userId);
        return doVerifyCredential(credential, type, false, 0, userId, progressCallback);
    }

    public VerifyCredentialResponse verifyCredential(String credential, int type, long challenge, int userId) throws RemoteException {
        checkPasswordReadPermission(userId);
        return doVerifyCredential(credential, type, true, challenge, userId, null);
    }

    /* JADX WARNING: Missing block: B:13:0x0034, code:
            return r2;
     */
    /* JADX WARNING: Missing block: B:15:0x0036, code:
            r5 = r12.mStorage.readCredentialHash(r18);
     */
    /* JADX WARNING: Missing block: B:16:0x003e, code:
            if (r5 == null) goto L_0x0044;
     */
    /* JADX WARNING: Missing block: B:18:0x0042, code:
            if (r5.hash != null) goto L_0x0053;
     */
    /* JADX WARNING: Missing block: B:19:0x0044, code:
            android.util.Slog.w(TAG, "no Pattern saved VerifyPattern success");
     */
    /* JADX WARNING: Missing block: B:20:0x004f, code:
            return com.android.internal.widget.VerifyCredentialResponse.OK;
     */
    /* JADX WARNING: Missing block: B:25:0x0056, code:
            if (r5.hash.length == 0) goto L_0x0044;
     */
    /* JADX WARNING: Missing block: B:27:0x005a, code:
            if (r5.type == r14) goto L_0x0086;
     */
    /* JADX WARNING: Missing block: B:28:0x005c, code:
            android.util.Slog.wtf(TAG, "doVerifyCredential type mismatch with stored credential?? stored: " + r5.type + " passed in: " + r14);
     */
    /* JADX WARNING: Missing block: B:29:0x0085, code:
            return com.android.internal.widget.VerifyCredentialResponse.ERROR;
     */
    /* JADX WARNING: Missing block: B:31:0x0089, code:
            if (r5.type != 1) goto L_0x00b6;
     */
    /* JADX WARNING: Missing block: B:32:0x008b, code:
            r11 = r5.isBaseZeroPattern;
     */
    /* JADX WARNING: Missing block: B:33:0x008d, code:
            if (r11 == false) goto L_0x00b8;
     */
    /* JADX WARNING: Missing block: B:34:0x008f, code:
            r6 = com.android.internal.widget.LockPatternUtils.patternStringToBaseZero(r13);
     */
    /* JADX WARNING: Missing block: B:35:0x0093, code:
            r2 = verifyCredential(r18, r5, r6, r15, r16, r19);
     */
    /* JADX WARNING: Missing block: B:36:0x00a3, code:
            if (r2.getResponseCode() != 0) goto L_0x00b5;
     */
    /* JADX WARNING: Missing block: B:37:0x00a5, code:
            r12.mStrongAuth.reportSuccessfulStrongAuthUnlock(r18);
     */
    /* JADX WARNING: Missing block: B:38:0x00ac, code:
            if (r11 == false) goto L_0x00b5;
     */
    /* JADX WARNING: Missing block: B:39:0x00ae, code:
            setLockCredentialInternal(r13, r5.type, r6, r18);
     */
    /* JADX WARNING: Missing block: B:40:0x00b5, code:
            return r2;
     */
    /* JADX WARNING: Missing block: B:41:0x00b6, code:
            r11 = false;
     */
    /* JADX WARNING: Missing block: B:42:0x00b8, code:
            r6 = r13;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private VerifyCredentialResponse doVerifyCredential(String credential, int credentialType, boolean hasChallenge, long challenge, int userId, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        if (TextUtils.isEmpty(credential)) {
            this.mLockPatternUtils.monitorCheckPassword(1002, null);
            throw new IllegalArgumentException("Credential can't be null or empty");
        }
        synchronized (this.mSpManager) {
            if (isSyntheticPasswordBasedCredentialLocked(userId)) {
                VerifyCredentialResponse response = spBasedDoVerifyCredentialLocked(credential, credentialType, hasChallenge, challenge, userId, progressCallback);
                if (response.getResponseCode() == 0) {
                    this.mStrongAuth.reportSuccessfulStrongAuthUnlock(userId);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0052 A:{Splitter: B:7:0x003b, ExcHandler: java.security.UnrecoverableKeyException (r18_0 'e' java.lang.Throwable)} */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x0052 A:{Splitter: B:7:0x003b, ExcHandler: java.security.UnrecoverableKeyException (r18_0 'e' java.lang.Throwable)} */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x0052 A:{Splitter: B:7:0x003b, ExcHandler: java.security.UnrecoverableKeyException (r18_0 'e' java.lang.Throwable)} */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x0052 A:{Splitter: B:7:0x003b, ExcHandler: java.security.UnrecoverableKeyException (r18_0 'e' java.lang.Throwable)} */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x0052 A:{Splitter: B:7:0x003b, ExcHandler: java.security.UnrecoverableKeyException (r18_0 'e' java.lang.Throwable)} */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x0052 A:{Splitter: B:7:0x003b, ExcHandler: java.security.UnrecoverableKeyException (r18_0 'e' java.lang.Throwable)} */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x0052 A:{Splitter: B:7:0x003b, ExcHandler: java.security.UnrecoverableKeyException (r18_0 'e' java.lang.Throwable)} */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x0052 A:{Splitter: B:7:0x003b, ExcHandler: java.security.UnrecoverableKeyException (r18_0 'e' java.lang.Throwable)} */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x0052 A:{Splitter: B:7:0x003b, ExcHandler: java.security.UnrecoverableKeyException (r18_0 'e' java.lang.Throwable)} */
    /* JADX WARNING: Missing block: B:10:0x0052, code:
            r18 = move-exception;
     */
    /* JADX WARNING: Missing block: B:11:0x0053, code:
            android.util.Slog.e(TAG, "Failed to decrypt child profile key", r18);
     */
    /* JADX WARNING: Missing block: B:12:0x0066, code:
            throw new android.os.RemoteException("Unable to get tied profile token");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public VerifyCredentialResponse verifyTiedProfileChallenge(String credential, int type, long challenge, int userId) throws RemoteException {
        checkPasswordReadPermission(userId);
        if (isManagedProfileWithUnifiedLock(userId)) {
            VerifyCredentialResponse parentResponse = doVerifyCredential(credential, type, true, challenge, this.mUserManager.getProfileParent(userId).id, null);
            if (parentResponse.getResponseCode() != 0) {
                return parentResponse;
            }
            try {
                return doVerifyCredential(getDecryptedPasswordForTiedProfile(userId), 2, true, challenge, userId, null);
            } catch (Throwable e) {
            }
        } else {
            throw new RemoteException("User id must be managed profile with unified lock");
        }
    }

    private VerifyCredentialResponse verifyCredential(int userId, CredentialHash storedHash, String credential, boolean hasChallenge, long challenge, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        if ((storedHash == null || storedHash.hash.length == 0) && TextUtils.isEmpty(credential)) {
            Slog.w(TAG, "no stored Password/Pattern, verifyCredential success");
            return VerifyCredentialResponse.OK;
        } else if (storedHash == null || TextUtils.isEmpty(credential)) {
            Slog.w(TAG, "no entered Password/Pattern, verifyCredential ERROR");
            return VerifyCredentialResponse.ERROR;
        } else {
            StrictMode.noteDiskRead();
            if (storedHash.version == 0) {
                byte[] hash;
                if (storedHash.type == 1) {
                    hash = LockPatternUtils.patternToHash(LockPatternUtils.stringToPattern(credential));
                } else {
                    hash = this.mLockPatternUtils.passwordToHash(credential, userId);
                }
                if (!Arrays.equals(hash, storedHash.hash)) {
                    return VerifyCredentialResponse.ERROR;
                }
                if (storedHash.type == 1) {
                    unlockKeystore(LockPatternUtils.patternStringToBaseZero(credential), userId);
                } else {
                    unlockKeystore(credential, userId);
                }
                Slog.i(TAG, "Unlocking user with fake token: " + userId);
                byte[] fakeToken = String.valueOf(userId).getBytes();
                unlockUser(userId, fakeToken, fakeToken);
                setLockCredentialInternal(credential, storedHash.type, null, userId);
                if (!hasChallenge) {
                    notifyActivePasswordMetricsAvailable(credential, userId);
                    return VerifyCredentialResponse.OK;
                }
            }
            try {
                if (getGateKeeperService() == null) {
                    this.mLockPatternUtils.monitorCheckPassword(1006, null);
                    return VerifyCredentialResponse.ERROR;
                }
                GateKeeperResponse gateKeeperResponse = getGateKeeperService().verifyChallenge(userId, challenge, storedHash.hash, credential.getBytes());
                VerifyCredentialResponse response = convertResponse(gateKeeperResponse);
                boolean shouldReEnroll = gateKeeperResponse.getShouldReEnroll();
                if (response.getResponseCode() == 0) {
                    if (progressCallback != null) {
                        progressCallback.onCredentialVerified();
                    }
                    notifyActivePasswordMetricsAvailable(credential, userId);
                    unlockKeystore(credential, userId);
                    Slog.i(TAG, "Unlocking user " + userId + " with token length " + response.getPayload().length);
                    unlockUser(userId, response.getPayload(), secretFromCredential(credential));
                    if (isManagedProfileWithSeparatedLock(userId)) {
                        ((TrustManager) this.mContext.getSystemService("trust")).setDeviceLockedForUser(userId, false);
                    }
                    if (shouldReEnroll) {
                        setLockCredentialInternal(credential, storedHash.type, credential, userId);
                    } else {
                        synchronized (this.mSpManager) {
                            if (shouldMigrateToSyntheticPasswordLocked(userId)) {
                                activateEscrowTokens(initializeSyntheticPasswordLocked(storedHash.hash, credential, storedHash.type, userId), userId);
                            }
                        }
                    }
                    if ((getStrongAuthForUser(userId) & 1) != 0) {
                        Slog.w(TAG, "clear BOOT_AUTH flag after verifyCredential");
                        requireStrongAuth(0, userId);
                    }
                    Slog.w(TAG, "verifyCredential passed by GateKeeper");
                } else if (response.getResponseCode() == 1 && response.getTimeout() > 0) {
                    requireStrongAuth(8, userId);
                }
                return response;
            } catch (RemoteException re) {
                this.mLockPatternUtils.monitorCheckPassword(1004, re);
                return VerifyCredentialResponse.ERROR;
            }
        }
    }

    private void notifyActivePasswordMetricsAvailable(String password, int userId) {
        PasswordMetrics metrics;
        if (password == null) {
            metrics = new PasswordMetrics();
        } else {
            metrics = PasswordMetrics.computeForPassword(password);
            metrics.quality = this.mLockPatternUtils.getKeyguardStoredPasswordQuality(userId);
        }
        this.mHandler.post(new com.android.server.-$Lambda$3ppOfNoHK7gU64PsFhIJAmO8LxY.AnonymousClass1(userId, this, metrics));
    }

    /* synthetic */ void lambda$-com_android_server_LockSettingsService_83036(PasswordMetrics metrics, int userId) {
        ((DevicePolicyManager) this.mContext.getSystemService("device_policy")).setActivePasswordState(metrics, userId);
    }

    private void notifyPasswordChanged(int userId) {
        this.mHandler.post(new -$Lambda$3ppOfNoHK7gU64PsFhIJAmO8LxY(userId, this));
    }

    /* synthetic */ void lambda$-com_android_server_LockSettingsService_83584(int userId) {
        ((DevicePolicyManager) this.mContext.getSystemService("device_policy")).reportPasswordChanged(userId);
    }

    public boolean checkVoldPassword(int userId) throws RemoteException {
        if (!this.mFirstCallToVold) {
            return false;
        }
        this.mFirstCallToVold = false;
        checkPasswordReadPermission(userId);
        IStorageManager service = this.mInjector.getStorageManager();
        long identity = Binder.clearCallingIdentity();
        try {
            String password = service.getPassword();
            service.clearPassword();
            if (password == null) {
                return false;
            }
            try {
                if (this.mLockPatternUtils.isLockPatternEnabled(userId) && checkCredential(password, 1, userId, null).getResponseCode() == 0) {
                    return true;
                }
            } catch (Exception e) {
            }
            try {
                return this.mLockPatternUtils.isLockPasswordEnabled(userId) && checkCredential(password, 2, userId, null).getResponseCode() == 0;
            } catch (Exception e2) {
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void removeUser(int userId, boolean unknownUser) {
        this.mStorage.removeUser(userId);
        this.mStrongAuth.removeUser(userId);
        KeyStore.getInstance().onUserRemoved(userId);
        try {
            IGateKeeperService gk = getGateKeeperService();
            if (gk != null) {
                gk.clearSecureUserId(userId);
            }
        } catch (RemoteException e) {
            Slog.w(TAG, "unable to clear GK secure user id");
        }
        if (unknownUser || this.mUserManager.getUserInfo(userId).isManagedProfile()) {
            removeKeystoreProfileKey(userId);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:2:0x003a A:{Splitter: B:0:0x0000, ExcHandler: java.security.KeyStoreException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:2:0x003a A:{Splitter: B:0:0x0000, ExcHandler: java.security.KeyStoreException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:2:0x003a A:{Splitter: B:0:0x0000, ExcHandler: java.security.KeyStoreException (r0_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:2:0x003a, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:3:0x003b, code:
            android.util.Slog.e(TAG, "Unable to remove keystore profile key for user:" + r6, r0);
     */
    /* JADX WARNING: Missing block: B:5:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void removeKeystoreProfileKey(int targetUserId) {
        try {
            java.security.KeyStore keyStore = java.security.KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            keyStore.deleteEntry("profile_key_name_encrypt_" + targetUserId);
            keyStore.deleteEntry("profile_key_name_decrypt_" + targetUserId);
        } catch (Exception e) {
        }
    }

    public void registerStrongAuthTracker(IStrongAuthTracker tracker) {
        if (tracker == null) {
            Slog.e(TAG, "IStrongAuthTracker can not be null in methdo registerStrongAuthTracker!");
            return;
        }
        checkPasswordReadPermission(-1);
        this.mStrongAuth.registerStrongAuthTracker(tracker);
    }

    public void unregisterStrongAuthTracker(IStrongAuthTracker tracker) {
        if (tracker == null) {
            Slog.e(TAG, "IStrongAuthTracker can not be null in methdo unregisterStrongAuthTracker!");
            return;
        }
        checkPasswordReadPermission(-1);
        this.mStrongAuth.unregisterStrongAuthTracker(tracker);
    }

    public void requireStrongAuth(int strongAuthReason, int userId) {
        checkWritePermission(userId);
        if ((strongAuthReason & 1) != 0) {
            Slog.e(TAG, "requireStrongAuth for AFTER_BOOT UID: " + Binder.getCallingUid() + " PID: " + Binder.getCallingPid());
        }
        this.mStrongAuth.requireStrongAuth(strongAuthReason, userId);
    }

    public void userPresent(int userId) {
        checkWritePermission(userId);
        this.mStrongAuth.reportUnlock(userId);
    }

    public int getStrongAuthForUser(int userId) {
        checkPasswordReadPermission(userId);
        return this.mStrongAuthTracker.getStrongAuthForUser(userId);
    }

    private boolean isCallerShell() {
        int callingUid = Binder.getCallingUid();
        if (callingUid == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME || callingUid == 0) {
            return true;
        }
        return false;
    }

    private void enforceShell() {
        if (!isCallerShell()) {
            throw new SecurityException("Caller must be shell");
        }
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) throws RemoteException {
        enforceShell();
        long origId = Binder.clearCallingIdentity();
        try {
            new LockSettingsShellCommand(this.mContext, new LockPatternUtils(this.mContext)).exec(this, in, out, err, args, callback, resultReceiver);
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    protected synchronized IGateKeeperService getGateKeeperService() throws RemoteException {
        if (this.mGateKeeperService != null) {
            return this.mGateKeeperService;
        }
        IBinder service = ServiceManager.getService("android.service.gatekeeper.IGateKeeperService");
        if (service != null) {
            service.linkToDeath(new GateKeeperDiedRecipient(this, null), 0);
            this.mGateKeeperService = IGateKeeperService.Stub.asInterface(service);
            return this.mGateKeeperService;
        }
        Slog.e(TAG, "Unable to acquire GateKeeperService");
        return null;
    }

    private AuthenticationToken initializeSyntheticPasswordLocked(byte[] credentialHash, String credential, int credentialType, int userId) throws RemoteException {
        Slog.i(TAG, "Initialize SyntheticPassword for user: " + userId);
        AuthenticationToken auth = this.mSpManager.newSyntheticPasswordAndSid(getGateKeeperService(), credentialHash, credential, userId);
        if (auth == null) {
            Slog.wtf(TAG, "initializeSyntheticPasswordLocked returns null auth token");
            return null;
        }
        long handle = this.mSpManager.createPasswordBasedSyntheticPassword(getGateKeeperService(), credential, credentialType, auth, userId);
        if (credential != null) {
            if (credentialHash == null) {
                this.mSpManager.newSidForUser(getGateKeeperService(), auth, userId);
            }
            this.mSpManager.verifyChallenge(getGateKeeperService(), auth, 0, userId);
            setAuthlessUserKeyProtection(userId, auth.deriveDiskEncryptionKey());
            setKeystorePassword(auth.deriveKeyStorePassword(), userId);
        } else {
            clearUserKeyProtection(userId);
            setKeystorePassword(null, userId);
            getGateKeeperService().clearSecureUserId(userId);
        }
        fixateNewestUserKeyAuth(userId);
        setLong("sp-handle", handle, userId);
        return auth;
    }

    private long getSyntheticPasswordHandleLocked(int userId) {
        try {
            return getLong("sp-handle", 0, userId);
        } catch (RemoteException e) {
            return 0;
        }
    }

    private boolean isSyntheticPasswordBasedCredentialLocked(int userId) throws RemoteException {
        long handle = getSyntheticPasswordHandleLocked(userId);
        if (getLong("enable-sp", 0, 0) == 0 || handle == 0) {
            return false;
        }
        return true;
    }

    private boolean shouldMigrateToSyntheticPasswordLocked(int userId) throws RemoteException {
        long handle = getSyntheticPasswordHandleLocked(userId);
        if (getLong("enable-sp", 0, 0) == 0 || handle != 0) {
            return false;
        }
        return true;
    }

    private void enableSyntheticPasswordLocked() throws RemoteException {
        setLong("enable-sp", 1, 0);
    }

    private VerifyCredentialResponse spBasedDoVerifyCredentialLocked(String userCredential, int credentialType, boolean hasChallenge, long challenge, int userId, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        if (credentialType == -1) {
            userCredential = null;
        }
        AuthenticationResult authResult = this.mSpManager.unwrapPasswordBasedSyntheticPassword(getGateKeeperService(), getSyntheticPasswordHandleLocked(userId), userCredential, userId);
        VerifyCredentialResponse response = authResult.gkResponse;
        if (response.getResponseCode() == 0) {
            response = this.mSpManager.verifyChallenge(getGateKeeperService(), authResult.authToken, challenge, userId);
            if (response.getResponseCode() != 0) {
                Slog.wtf(TAG, "verifyChallenge with SP failed.");
                return VerifyCredentialResponse.ERROR;
            }
            if (progressCallback != null) {
                progressCallback.onCredentialVerified();
            }
            notifyActivePasswordMetricsAvailable(userCredential, userId);
            unlockKeystore(authResult.authToken.deriveKeyStorePassword(), userId);
            byte[] secret = authResult.authToken.deriveDiskEncryptionKey();
            Slog.i(TAG, "Unlocking user " + userId + " with secret only, length " + secret.length);
            unlockUser(userId, null, secret);
            if (isManagedProfileWithSeparatedLock(userId)) {
                ((TrustManager) this.mContext.getSystemService("trust")).setDeviceLockedForUser(userId, false);
            }
            activateEscrowTokens(authResult.authToken, userId);
        } else if (response.getResponseCode() == 1 && response.getTimeout() > 0) {
            requireStrongAuth(8, userId);
        }
        return response;
    }

    private long setLockCredentialWithAuthTokenLocked(String credential, int credentialType, AuthenticationToken auth, int userId) throws RemoteException {
        Map profilePasswords;
        long newHandle = this.mSpManager.createPasswordBasedSyntheticPassword(getGateKeeperService(), credential, credentialType, auth, userId);
        if (credential != null) {
            profilePasswords = null;
            if (this.mSpManager.hasSidForUser(userId)) {
                this.mSpManager.verifyChallenge(getGateKeeperService(), auth, 0, userId);
            } else {
                this.mSpManager.newSidForUser(getGateKeeperService(), auth, userId);
                this.mSpManager.verifyChallenge(getGateKeeperService(), auth, 0, userId);
                setAuthlessUserKeyProtection(userId, auth.deriveDiskEncryptionKey());
                fixateNewestUserKeyAuth(userId);
                setKeystorePassword(auth.deriveKeyStorePassword(), userId);
            }
        } else {
            profilePasswords = getDecryptedPasswordsForAllTiedProfiles(userId);
            this.mSpManager.clearSidForUser(userId);
            getGateKeeperService().clearSecureUserId(userId);
            clearUserKeyProtection(userId);
            fixateNewestUserKeyAuth(userId);
            setKeystorePassword(null, userId);
        }
        setLong("sp-handle", newHandle, userId);
        synchronizeUnifiedWorkChallengeForProfiles(userId, profilePasswords);
        return newHandle;
    }

    /* JADX WARNING: Removed duplicated region for block: B:9:0x002c A:{Splitter: B:2:0x0006, ExcHandler: java.security.UnrecoverableKeyException (r9_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x002c A:{Splitter: B:2:0x0006, ExcHandler: java.security.UnrecoverableKeyException (r9_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x002c A:{Splitter: B:2:0x0006, ExcHandler: java.security.UnrecoverableKeyException (r9_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x002c A:{Splitter: B:2:0x0006, ExcHandler: java.security.UnrecoverableKeyException (r9_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x002c A:{Splitter: B:2:0x0006, ExcHandler: java.security.UnrecoverableKeyException (r9_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x002c A:{Splitter: B:2:0x0006, ExcHandler: java.security.UnrecoverableKeyException (r9_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x002c A:{Splitter: B:2:0x0006, ExcHandler: java.security.UnrecoverableKeyException (r9_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x002c A:{Splitter: B:2:0x0006, ExcHandler: java.security.UnrecoverableKeyException (r9_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x002c A:{Splitter: B:2:0x0006, ExcHandler: java.security.UnrecoverableKeyException (r9_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:9:0x002c, code:
            r9 = move-exception;
     */
    /* JADX WARNING: Missing block: B:10:0x002d, code:
            android.util.Slog.e(TAG, "Failed to decrypt child profile key", r9);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void spBasedSetLockCredentialInternalLocked(String credential, int credentialType, String savedCredential, int userId) throws RemoteException {
        if (isManagedProfileWithUnifiedLock(userId)) {
            try {
                savedCredential = getDecryptedPasswordForTiedProfile(userId);
            } catch (FileNotFoundException e) {
                Slog.i(TAG, "Child profile key not found");
            } catch (Exception e2) {
            }
        }
        long handle = getSyntheticPasswordHandleLocked(userId);
        AuthenticationResult authResult = this.mSpManager.unwrapPasswordBasedSyntheticPassword(getGateKeeperService(), handle, savedCredential, userId);
        VerifyCredentialResponse response = authResult.gkResponse;
        AuthenticationToken auth = authResult.authToken;
        if (auth != null) {
            setLockCredentialWithAuthTokenLocked(credential, credentialType, auth, userId);
            this.mSpManager.destroyPasswordBasedSyntheticPassword(handle, userId);
        } else if (response == null || response.getResponseCode() != -1) {
            Slog.w(TAG, "spBasedSetLockCredentialInternalLocked: " + (response != null ? "rate limit exceeded" : "failed"));
            return;
        } else {
            Slog.w(TAG, "Untrusted credential change invoked");
            initializeSyntheticPasswordLocked(null, credential, credentialType, userId);
            synchronizeUnifiedWorkChallengeForProfiles(userId, null);
            this.mSpManager.destroyPasswordBasedSyntheticPassword(handle, userId);
        }
        notifyActivePasswordMetricsAvailable(credential, userId);
    }

    public long addEscrowToken(byte[] token, int userId) throws RemoteException {
        long handle;
        ensureCallerSystemUid();
        synchronized (this.mSpManager) {
            enableSyntheticPasswordLocked();
            AuthenticationToken auth = null;
            if (!isUserSecure(userId)) {
                if (shouldMigrateToSyntheticPasswordLocked(userId)) {
                    auth = initializeSyntheticPasswordLocked(null, null, -1, userId);
                } else {
                    auth = this.mSpManager.unwrapPasswordBasedSyntheticPassword(getGateKeeperService(), getSyntheticPasswordHandleLocked(userId), null, userId).authToken;
                }
            }
            if (isSyntheticPasswordBasedCredentialLocked(userId)) {
                disableEscrowTokenOnNonManagedDevicesIfNeeded(userId);
                if (!this.mSpManager.hasEscrowData(userId)) {
                    throw new SecurityException("Escrow token is disabled on the current user");
                }
            }
            handle = this.mSpManager.createTokenBasedSyntheticPassword(token, userId);
            if (auth != null) {
                this.mSpManager.activateTokenBasedSyntheticPassword(handle, auth, userId);
            }
        }
        return handle;
    }

    private void activateEscrowTokens(AuthenticationToken auth, int userId) throws RemoteException {
        disableEscrowTokenOnNonManagedDevicesIfNeeded(userId);
        synchronized (this.mSpManager) {
            for (Long longValue : this.mSpManager.getPendingTokensForUser(userId)) {
                long handle = longValue.longValue();
                Slog.i(TAG, String.format("activateEscrowTokens: %x %d ", new Object[]{Long.valueOf(handle), Integer.valueOf(userId)}));
                this.mSpManager.activateTokenBasedSyntheticPassword(handle, auth, userId);
            }
        }
    }

    public boolean isEscrowTokenActive(long handle, int userId) throws RemoteException {
        boolean existsHandle;
        ensureCallerSystemUid();
        synchronized (this.mSpManager) {
            existsHandle = this.mSpManager.existsHandle(handle, userId);
        }
        return existsHandle;
    }

    public boolean removeEscrowToken(long handle, int userId) throws RemoteException {
        ensureCallerSystemUid();
        synchronized (this.mSpManager) {
            if (handle == getSyntheticPasswordHandleLocked(userId)) {
                Slog.w(TAG, "Cannot remove password handle");
                return false;
            } else if (this.mSpManager.removePendingToken(handle, userId)) {
                return true;
            } else if (this.mSpManager.existsHandle(handle, userId)) {
                this.mSpManager.destroyTokenBasedSyntheticPassword(handle, userId);
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean setLockCredentialWithToken(String credential, int type, long tokenHandle, byte[] token, int userId) throws RemoteException {
        boolean result;
        ensureCallerSystemUid();
        int oldCredentialType = getOldCredentialType(userId);
        synchronized (this.mSpManager) {
            if (this.mSpManager.hasEscrowData(userId)) {
                result = setLockCredentialWithTokenInternal(credential, type, tokenHandle, token, userId);
            } else {
                throw new SecurityException("Escrow token is disabled on the current user");
            }
        }
        if (result) {
            synchronized (this.mSeparateChallengeLock) {
                setSeparateProfileChallengeEnabled(userId, true, null);
            }
            notifyPasswordChanged(userId);
            notifyPasswordStatusChanged(userId, getPasswordStatus(type, oldCredentialType));
        }
        return result;
    }

    private boolean setLockCredentialWithTokenInternal(String credential, int type, long tokenHandle, byte[] token, int userId) throws RemoteException {
        synchronized (this.mSpManager) {
            AuthenticationResult result = this.mSpManager.unwrapTokenBasedSyntheticPassword(getGateKeeperService(), tokenHandle, token, userId);
            if (result.authToken == null) {
                Slog.w(TAG, "Invalid escrow token supplied");
                return false;
            }
            long oldHandle = getSyntheticPasswordHandleLocked(userId);
            setLockCredentialWithAuthTokenLocked(credential, type, result.authToken, userId);
            this.mSpManager.destroyPasswordBasedSyntheticPassword(oldHandle, userId);
            return true;
        }
    }

    public void unlockUserWithToken(long tokenHandle, byte[] token, int userId) throws RemoteException {
        ensureCallerSystemUid();
        synchronized (this.mSpManager) {
            if (this.mSpManager.hasEscrowData(userId)) {
                AuthenticationResult authResult = this.mSpManager.unwrapTokenBasedSyntheticPassword(getGateKeeperService(), tokenHandle, token, userId);
                if (authResult.authToken == null) {
                    Slog.w(TAG, "Invalid escrow token supplied");
                    return;
                }
                unlockUser(userId, null, authResult.authToken.deriveDiskEncryptionKey());
                return;
            }
            throw new SecurityException("Escrow token is disabled on the current user");
        }
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            pw.println("Current lock settings service state:");
            pw.println(String.format("SP Enabled = %b", new Object[]{Boolean.valueOf(this.mLockPatternUtils.isSyntheticPasswordEnabled())}));
            List<UserInfo> users = this.mUserManager.getUsers();
            for (int user = 0; user < users.size(); user++) {
                pw.println("    User " + ((UserInfo) users.get(user)).id);
                synchronized (this.mSpManager) {
                    pw.println(String.format("        SP Handle = %x", new Object[]{Long.valueOf(getSyntheticPasswordHandleLocked(userId))}));
                }
                try {
                    pw.println(String.format("        SID = %x", new Object[]{Long.valueOf(getGateKeeperService().getSecureUserId(userId))}));
                } catch (RemoteException e) {
                }
            }
        }
    }

    private void disableEscrowTokenOnNonManagedDevicesIfNeeded(int userId) {
        long ident = Binder.clearCallingIdentity();
        try {
            if (this.mUserManager.getUserInfo(userId).isManagedProfile()) {
                Slog.i(TAG, "Managed profile can have escrow token");
                return;
            }
            DevicePolicyManager dpm = this.mInjector.getDevicePolicyManager();
            if (dpm.getDeviceOwnerComponentOnAnyUser() != null) {
                Slog.i(TAG, "Corp-owned device can have escrow token");
                Binder.restoreCallingIdentity(ident);
            } else if (dpm.getProfileOwnerAsUser(userId) != null) {
                Slog.i(TAG, "User with profile owner can have escrow token");
                Binder.restoreCallingIdentity(ident);
            } else if (!dpm.isDeviceProvisioned()) {
                Slog.i(TAG, "Postpone disabling escrow tokens until device is provisioned");
                Binder.restoreCallingIdentity(ident);
            } else if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.automotive")) {
                Binder.restoreCallingIdentity(ident);
            } else {
                Slog.i(TAG, "Disabling escrow token on user " + userId);
                if (isSyntheticPasswordBasedCredentialLocked(userId)) {
                    this.mSpManager.destroyEscrowData(userId);
                }
                Binder.restoreCallingIdentity(ident);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "disableEscrowTokenOnNonManagedDevices", e);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void ensureCallerSystemUid() throws SecurityException {
        if (this.mInjector.binderGetCallingUid() != 1000) {
            throw new SecurityException("Only system can call this API.");
        }
    }
}
