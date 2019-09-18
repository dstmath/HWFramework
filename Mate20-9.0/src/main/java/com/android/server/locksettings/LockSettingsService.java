package com.android.server.locksettings;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.app.admin.DevicePolicyManagerInternal;
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
import android.database.ContentObserver;
import android.database.sqlite.SQLiteDatabase;
import android.encrypt.ISDCardCryptedHelper;
import android.hardware.authsecret.V1_0.IAuthSecret;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.security.KeyStore;
import android.security.keystore.AndroidKeyStoreProvider;
import android.security.keystore.KeyProtection;
import android.security.keystore.UserNotAuthenticatedException;
import android.security.keystore.recovery.KeyChainProtectionParams;
import android.security.keystore.recovery.KeyChainSnapshot;
import android.security.keystore.recovery.RecoveryCertPath;
import android.security.keystore.recovery.WrappedApplicationKey;
import android.service.gatekeeper.GateKeeperResponse;
import android.service.gatekeeper.IGateKeeperService;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.Preconditions;
import com.android.internal.widget.ICheckCredentialProgressCallback;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockSettingsInternal;
import com.android.internal.widget.VerifyCredentialResponse;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.backup.BackupManagerService;
import com.android.server.locksettings.LockSettingsStorage;
import com.android.server.locksettings.SyntheticPasswordManager;
import com.android.server.locksettings.recoverablekeystore.RecoverableKeyStoreManager;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
    protected static final boolean DEBUG = false;
    private static final String PERMISSION = "android.permission.ACCESS_KEYGUARD_SECURE_STORAGE";
    protected static final int PROFILE_KEY_IV_SIZE = 12;
    private static final String[] READ_CONTACTS_PROTECTED_SETTINGS = {"lock_screen_owner_info_enabled", "lock_screen_owner_info"};
    private static final String[] READ_PASSWORD_PROTECTED_SETTINGS = {"lockscreen.password_salt", "lockscreen.passwordhistory", "lockscreen.password_type", SEPARATE_PROFILE_CHALLENGE_KEY};
    private static final String SEPARATE_PROFILE_CHALLENGE_KEY = "lockscreen.profilechallenge";
    private static final String[] SETTINGS_TO_BACKUP = {"lock_screen_owner_info_enabled", "lock_screen_owner_info", "lock_pattern_visible_pattern", "lockscreen.power_button_instantly_locks"};
    private static final int SYNTHETIC_PASSWORD_ENABLED_BY_DEFAULT = 1;
    private static final int[] SYSTEM_CREDENTIAL_UIDS = {1010, 1016, 0, 1000};
    private static final String TAG = "LKSS";
    private static final String[] VALID_SETTINGS = {"lockscreen.lockedoutpermanently", "lockscreen.patterneverchosen", "lockscreen.password_type", "lockscreen.password_type_alternate", "lockscreen.password_salt", "lockscreen.disabled", "lockscreen.options", "lockscreen.biometric_weak_fallback", "lockscreen.biometricweakeverchosen", "lockscreen.power_button_instantly_locks", "lockscreen.passwordhistory", "lock_pattern_autolock", "lock_biometric_weak_flags", "lock_pattern_visible_pattern", "lock_pattern_tactile_feedback_enabled"};
    private static HwFrameworkMonitor mMonitor = HwFrameworkFactory.getHwFrameworkMonitor();
    private final String ACTION_PRIVACY_USER_ADDED_FINISHED;
    private final IActivityManager mActivityManager;
    protected IAuthSecret mAuthSecretService;
    private final BroadcastReceiver mBroadcastReceiver;
    /* access modifiers changed from: private */
    public final Context mContext;
    private final DeviceProvisionedObserver mDeviceProvisionedObserver;
    private boolean mFirstCallToVold;
    protected IGateKeeperService mGateKeeperService;
    @VisibleForTesting
    protected final Handler mHandler;
    private final Injector mInjector;
    private final KeyStore mKeyStore;
    protected LockPatternUtils mLockPatternUtils;
    private final NotificationManager mNotificationManager;
    private final RecoverableKeyStoreManager mRecoverableKeyStoreManager;
    private final Object mSeparateChallengeLock;
    @GuardedBy("mSpManager")
    private SparseArray<SyntheticPasswordManager.AuthenticationToken> mSpCache;
    private final SyntheticPasswordManager mSpManager;
    @VisibleForTesting
    protected final LockSettingsStorage mStorage;
    protected final LockSettingsStrongAuth mStrongAuth;
    private final SynchronizedStrongAuthTracker mStrongAuthTracker;
    protected final UserManager mUserManager;

    private class DeviceProvisionedObserver extends ContentObserver {
        private final Uri mDeviceProvisionedUri = Settings.Global.getUriFor("device_provisioned");
        private boolean mRegistered;
        private final Uri mUserSetupCompleteUri = Settings.Secure.getUriFor("user_setup_complete");

        public DeviceProvisionedObserver() {
            super(null);
        }

        public void onChange(boolean selfChange, Uri uri, int userId) {
            if (this.mDeviceProvisionedUri.equals(uri)) {
                updateRegistration();
                if (isProvisioned()) {
                    Slog.i(LockSettingsService.TAG, "Reporting device setup complete to IGateKeeperService");
                    reportDeviceSetupComplete();
                    clearFrpCredentialIfOwnerNotSecure();
                }
            } else if (this.mUserSetupCompleteUri.equals(uri)) {
                LockSettingsService.this.tryRemoveUserFromSpCacheLater(userId);
            }
        }

        public void onSystemReady() {
            if (LockPatternUtils.frpCredentialEnabled(LockSettingsService.this.mContext)) {
                updateRegistration();
            } else if (!isProvisioned()) {
                Slog.i(LockSettingsService.TAG, "FRP credential disabled, reporting device setup complete to Gatekeeper immediately");
                reportDeviceSetupComplete();
            }
        }

        private void reportDeviceSetupComplete() {
            try {
                LockSettingsService.this.getGateKeeperService().reportDeviceSetupComplete();
            } catch (RemoteException e) {
                Slog.e(LockSettingsService.TAG, "Failure reporting to IGateKeeperService", e);
            }
        }

        private void clearFrpCredentialIfOwnerNotSecure() {
            for (UserInfo user : LockSettingsService.this.mUserManager.getUsers()) {
                if (LockPatternUtils.userOwnsFrpCredential(LockSettingsService.this.mContext, user)) {
                    if (!LockSettingsService.this.isUserSecure(user.id)) {
                        LockSettingsService.this.mStorage.writePersistentDataBlock(0, user.id, 0, null);
                    }
                    return;
                }
            }
        }

        private void updateRegistration() {
            boolean register = !isProvisioned();
            if (register != this.mRegistered) {
                if (register) {
                    LockSettingsService.this.mContext.getContentResolver().registerContentObserver(this.mDeviceProvisionedUri, false, this);
                    LockSettingsService.this.mContext.getContentResolver().registerContentObserver(this.mUserSetupCompleteUri, false, this, -1);
                } else {
                    LockSettingsService.this.mContext.getContentResolver().unregisterContentObserver(this);
                }
                this.mRegistered = register;
            }
        }

        private boolean isProvisioned() {
            return Settings.Global.getInt(LockSettingsService.this.mContext.getContentResolver(), "device_provisioned", 0) != 0;
        }
    }

    private class GateKeeperDiedRecipient implements IBinder.DeathRecipient {
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

        /* access modifiers changed from: package-private */
        public LockSettingsStorage newStorage() {
            return new LockSettingsStorage(this.mContext);
        }

        public LockSettingsStorage getStorage() {
            final LockSettingsStorage storage = newStorage();
            storage.setDatabaseOnCreateCallback(new LockSettingsStorage.Callback() {
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

        public RecoverableKeyStoreManager getRecoverableKeyStoreManager(KeyStore keyStore) {
            return RecoverableKeyStoreManager.getInstance(this.mContext, keyStore);
        }

        public IStorageManager getStorageManager() {
            IBinder service = ServiceManager.getService("mount");
            if (service != null) {
                return IStorageManager.Stub.asInterface(service);
            }
            return null;
        }

        public SyntheticPasswordManager getSyntheticPasswordManager(LockSettingsStorage storage) {
            return new SyntheticPasswordManager(getContext(), storage, getUserManager());
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

        /* JADX WARNING: type inference failed for: r2v0, types: [com.android.server.locksettings.LockSettingsService, android.os.IBinder] */
        public void onStart() {
            AndroidKeyStoreProvider.install();
            HwServiceFactory.IHwLockSettingsService iLockSettingsService = HwServiceFactory.getHuaweiLockSettingsService();
            if (iLockSettingsService != null) {
                this.mLockSettingsService = iLockSettingsService.getInstance(getContext());
            } else {
                this.mLockSettingsService = new LockSettingsService(getContext());
            }
            publishBinderService("lock_settings", this.mLockSettingsService);
        }

        public void onBootPhase(int phase) {
            super.onBootPhase(phase);
            if (phase == 550) {
                this.mLockSettingsService.migrateOldDataAfterSystemReady();
            }
            if (phase == 550) {
                this.mLockSettingsService.showEncryptionNotificationForUsers();
            }
        }

        public void onStartUser(int userHandle) {
            this.mLockSettingsService.onStartUser(userHandle);
        }

        public void onUnlockUser(int userHandle) {
            this.mLockSettingsService.onUnlockUser(userHandle);
        }

        public void onCleanupUser(int userHandle) {
            this.mLockSettingsService.onCleanupUser(userHandle);
        }
    }

    private final class LocalService extends LockSettingsInternal {
        private LocalService() {
        }

        public long addEscrowToken(byte[] token, int userId) {
            try {
                return LockSettingsService.this.addEscrowToken(token, userId);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }

        public boolean removeEscrowToken(long handle, int userId) {
            return LockSettingsService.this.removeEscrowToken(handle, userId);
        }

        public boolean isEscrowTokenActive(long handle, int userId) {
            return LockSettingsService.this.isEscrowTokenActive(handle, userId);
        }

        public boolean setLockCredentialWithToken(String credential, int type, long tokenHandle, byte[] token, int requestedQuality, int userId) {
            try {
                return LockSettingsService.this.setLockCredentialWithToken(credential, type, tokenHandle, token, requestedQuality, userId);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }

        public boolean unlockUserWithToken(long tokenHandle, byte[] token, int userId) {
            try {
                return LockSettingsService.this.unlockUserWithToken(tokenHandle, token, userId);
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
    }

    @VisibleForTesting
    protected static class SynchronizedStrongAuthTracker extends LockPatternUtils.StrongAuthTracker {
        public SynchronizedStrongAuthTracker(Context context) {
            super(context);
        }

        /* access modifiers changed from: protected */
        public void handleStrongAuthRequiredChanged(int strongAuthFlags, int userId) {
            synchronized (this) {
                LockSettingsService.super.handleStrongAuthRequiredChanged(strongAuthFlags, userId);
            }
        }

        public int getStrongAuthForUser(int userId) {
            int strongAuthForUser;
            synchronized (this) {
                strongAuthForUser = LockSettingsService.super.getStrongAuthForUser(userId);
            }
            return strongAuthForUser;
        }

        /* access modifiers changed from: package-private */
        public void register(LockSettingsStrongAuth strongAuth) {
            strongAuth.registerStrongAuthTracker(this.mStub);
        }
    }

    static {
        ACTION_NULL.addCategory("android.intent.category.HOME");
    }

    public void tieManagedProfileLockIfNecessary(int managedUserId, String managedUserPassword) {
        if (this.mUserManager.getUserInfo(managedUserId).isManagedProfile() && !this.mLockPatternUtils.isSeparateProfileChallengeEnabled(managedUserId) && !this.mStorage.hasChildProfileLock(managedUserId)) {
            int parentId = this.mUserManager.getProfileParent(managedUserId).id;
            if (isUserSecure(parentId)) {
                try {
                    if (getGateKeeperService().getSecureUserId(parentId) != 0) {
                        byte[] bArr = new byte[0];
                        try {
                            String newPassword = String.valueOf(HexEncoding.encode(SecureRandom.getInstance("SHA1PRNG").generateSeed(40)));
                            setLockCredentialInternal(newPassword, 2, managedUserPassword, 327680, managedUserId);
                            setLong("lockscreen.password_type", 327680, managedUserId);
                            tieProfileLockToParent(managedUserId, newPassword);
                        } catch (RemoteException | NoSuchAlgorithmException e) {
                            Slog.e(TAG, "Fail to tie managed profile", e);
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

    @VisibleForTesting
    protected LockSettingsService(Injector injector) {
        this.mSeparateChallengeLock = new Object();
        this.mDeviceProvisionedObserver = new DeviceProvisionedObserver();
        this.ACTION_PRIVACY_USER_ADDED_FINISHED = "com.huawei.android.lockSettingService.action.USER_ADDED_FINISHED ";
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.USER_ADDED".equals(intent.getAction())) {
                    int userHandle = intent.getIntExtra("android.intent.extra.user_handle", 0);
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
                    int userHandle2 = intent.getIntExtra("android.intent.extra.user_handle", 0);
                    if (userHandle2 > 0) {
                        LockSettingsService.this.removeUser(userHandle2, false);
                    }
                }
            }
        };
        this.mSpCache = new SparseArray<>();
        this.mInjector = injector;
        this.mContext = injector.getContext();
        this.mKeyStore = injector.getKeyStore();
        this.mRecoverableKeyStoreManager = injector.getRecoverableKeyStoreManager(this.mKeyStore);
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
        LocalServices.addService(LockSettingsInternal.class, new LocalService());
    }

    private void maybeShowEncryptionNotificationForUser(int userId) {
        UserInfo user = this.mUserManager.getUserInfo(userId);
        if (user.isManagedProfile()) {
            UserHandle userHandle = user.getUserHandle();
            if (isUserSecure(userId) && !this.mUserManager.isUserUnlockingOrUnlocked(userHandle)) {
                UserInfo parent = this.mUserManager.getProfileParent(userId);
                if (parent != null && this.mUserManager.isUserUnlockingOrUnlocked(parent.getUserHandle()) && !this.mUserManager.isQuietModeEnabled(userHandle)) {
                    showEncryptionNotificationForProfile(userHandle);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void showEncryptionNotificationForUsers() {
        List<UserInfo> users = this.mUserManager.getUsers();
        for (int i = 0; i < users.size(); i++) {
            UserInfo user = users.get(i);
            if (!user.isClonedProfile()) {
                UserHandle userHandle = user.getUserHandle();
                Slog.i(TAG, "user.id = " + user.id);
                if (isUserSecure(user.id) && !this.mUserManager.isUserUnlockingOrUnlocked(userHandle) && !user.isManagedProfile()) {
                    Slog.i(TAG, "has password,show notification");
                    showEncryptionNotificationForUser(userHandle);
                }
            }
        }
    }

    private void showEncryptionNotificationForUser(UserHandle user) {
        Resources r = this.mContext.getResources();
        showEncryptionNotification(user, r.getText(33685896), r.getText(33685897), r.getText(17041309), PendingIntent.getBroadcast(this.mContext, 0, ACTION_NULL, 134217728));
    }

    /* access modifiers changed from: private */
    public void showEncryptionNotificationForProfile(UserHandle user) {
        Resources r = this.mContext.getResources();
        CharSequence title = r.getText(17041311);
        CharSequence message = r.getText(17040958);
        CharSequence detail = r.getText(17040957);
        Intent unlockIntent = ((KeyguardManager) this.mContext.getSystemService("keyguard")).createConfirmDeviceCredentialIntent(null, null, user.getIdentifier());
        if (unlockIntent != null) {
            unlockIntent.setFlags(276824064);
            showEncryptionNotification(user, title, message, detail, PendingIntent.getActivity(this.mContext, 0, unlockIntent, 134217728));
        }
    }

    private void showEncryptionNotification(UserHandle user, CharSequence title, CharSequence message, CharSequence detail, PendingIntent intent) {
        if (StorageManager.isFileEncryptedNativeOrEmulated()) {
            this.mNotificationManager.notifyAsUser(null, 9, new Notification.Builder(this.mContext, SystemNotificationChannels.SECURITY).setSmallIcon(17302778).setWhen(0).setOngoing(true).setTicker(title).setColor(this.mContext.getColor(17170784)).setContentTitle(title).setContentText(message).setSubText(detail).setVisibility(1).setContentIntent(intent).build(), user);
        }
    }

    /* access modifiers changed from: private */
    public void hideEncryptionNotification(UserHandle userHandle) {
        this.mNotificationManager.cancelAsUser(null, 9, userHandle);
    }

    public void onCleanupUser(int userId) {
        hideEncryptionNotification(new UserHandle(userId));
        requireStrongAuth(1, userId);
    }

    public void onStartUser(int userId) {
        maybeShowEncryptionNotificationForUser(userId);
    }

    /* access modifiers changed from: private */
    public void ensureProfileKeystoreUnlocked(int userId) {
        if (KeyStore.getInstance().state(userId) == KeyStore.State.LOCKED && tiedManagedProfileReadyToUnlock(this.mUserManager.getUserInfo(userId))) {
            Slog.i(TAG, "Managed profile got unlocked, will unlock its keystore");
            try {
                unlockChildProfile(userId, true);
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to unlock child profile");
            }
        }
    }

    public void onUnlockUser(final int userId) {
        this.mHandler.post(new Runnable() {
            public void run() {
                LockSettingsService.this.ensureProfileKeystoreUnlocked(userId);
                LockSettingsService.this.hideEncryptionNotification(new UserHandle(userId));
                List<UserInfo> profiles = LockSettingsService.this.mUserManager.getProfiles(userId);
                for (int i = 0; i < profiles.size(); i++) {
                    UserInfo profile = profiles.get(i);
                    if (LockSettingsService.this.isUserSecure(profile.id) && profile.isManagedProfile()) {
                        UserHandle userHandle = profile.getUserHandle();
                        if (!LockSettingsService.this.mUserManager.isUserUnlockingOrUnlocked(userHandle) && !LockSettingsService.this.mUserManager.isQuietModeEnabled(userHandle)) {
                            LockSettingsService.this.showEncryptionNotificationForProfile(userHandle);
                        }
                    }
                }
                if (LockSettingsService.this.mUserManager.getUserInfo(userId).isManagedProfile()) {
                    LockSettingsService.this.tieManagedProfileLockIfNecessary(userId, null);
                }
                if (LockSettingsService.this.mUserManager.getUserInfo(userId).isPrimary() && !LockSettingsService.this.isUserSecure(userId)) {
                    LockSettingsService.this.tryDeriveAuthTokenForUnsecuredPrimaryUser(userId);
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void tryDeriveAuthTokenForUnsecuredPrimaryUser(int userId) {
        synchronized (this.mSpManager) {
            if (isSyntheticPasswordBasedCredentialLocked(userId)) {
                try {
                    SyntheticPasswordManager.AuthenticationResult result = this.mSpManager.unwrapPasswordBasedSyntheticPassword(getGateKeeperService(), getSyntheticPasswordHandleLocked(userId), null, userId, null);
                    if (result.authToken != null) {
                        Slog.i(TAG, "Retrieved auth token for user " + userId);
                        onAuthTokenKnownForUser(userId, result.authToken);
                    } else {
                        Slog.e(TAG, "Auth token not available for user " + userId);
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "Failure retrieving auth token", e);
                }
            }
        }
    }

    public void systemReady() {
        if (this.mContext.checkCallingOrSelfPermission(PERMISSION) != 0) {
            EventLog.writeEvent(1397638484, new Object[]{"28251513", Integer.valueOf(getCallingUid()), BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS});
        }
        checkWritePermission(0);
        migrateOldData();
        try {
            getGateKeeperService();
            this.mSpManager.initWeaverService();
        } catch (RemoteException e) {
            Slog.e(TAG, "Failure retrieving IGateKeeperService", e);
        }
        try {
            this.mAuthSecretService = IAuthSecret.getService();
        } catch (NoSuchElementException e2) {
            Slog.i(TAG, "Device doesn't implement AuthSecret HAL");
        } catch (RemoteException e3) {
            Slog.w(TAG, "Failed to get AuthSecret HAL", e3);
        }
        this.mDeviceProvisionedObserver.onSystemReady();
        this.mStorage.prefetchUser(0);
        this.mStrongAuth.systemReady();
    }

    private void migrateOldData() {
        if (getString("migrated", null, 0) == null) {
            ContentResolver cr = this.mContext.getContentResolver();
            for (String validSetting : VALID_SETTINGS) {
                String value = Settings.Secure.getString(cr, validSetting);
                if (value != null) {
                    setString(validSetting, value, 0);
                }
            }
            setString("migrated", "true", 0);
            Slog.i(TAG, "Migrated lock settings to new location");
        }
        if (getString("migrated_user_specific", null, 0) == null) {
            ContentResolver cr2 = this.mContext.getContentResolver();
            List<UserInfo> users = this.mUserManager.getUsers();
            int user = 0;
            while (true) {
                int user2 = user;
                if (user2 >= users.size()) {
                    break;
                }
                int userId = users.get(user2).id;
                String ownerInfo = Settings.Secure.getStringForUser(cr2, "lock_screen_owner_info", userId);
                if (!TextUtils.isEmpty(ownerInfo)) {
                    setString("lock_screen_owner_info", ownerInfo, userId);
                    Settings.Secure.putStringForUser(cr2, "lock_screen_owner_info", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, userId);
                }
                Object obj = "lock_screen_owner_info_enabled";
                try {
                    setLong("lock_screen_owner_info_enabled", Settings.Secure.getIntForUser(cr2, "lock_screen_owner_info_enabled", userId) != 0 ? 1 : 0, userId);
                } catch (Settings.SettingNotFoundException e) {
                    if (!TextUtils.isEmpty(ownerInfo)) {
                        setLong("lock_screen_owner_info_enabled", 1, userId);
                    }
                }
                Settings.Secure.putIntForUser(cr2, "lock_screen_owner_info_enabled", 0, userId);
                user = user2 + 1;
            }
            setString("migrated_user_specific", "true", 0);
            Slog.i(TAG, "Migrated per-user lock settings to new location");
        }
        if (getString("migrated_biometric_weak", null, 0) == null) {
            List<UserInfo> users2 = this.mUserManager.getUsers();
            for (int i = 0; i < users2.size(); i++) {
                int userId2 = users2.get(i).id;
                long type = getLong("lockscreen.password_type", 0, userId2);
                long alternateType = getLong("lockscreen.password_type_alternate", 0, userId2);
                if (type == 32768) {
                    setLong("lockscreen.password_type", alternateType, userId2);
                }
                setLong("lockscreen.password_type_alternate", 0, userId2);
            }
            setString("migrated_biometric_weak", "true", 0);
            Slog.i(TAG, "Migrated biometric weak to use the fallback instead");
        }
        if (getString("migrated_lockscreen_disabled", null, 0) == null) {
            List<UserInfo> users3 = this.mUserManager.getUsers();
            int userCount = users3.size();
            int switchableUsers = 0;
            for (int i2 = 0; i2 < userCount; i2++) {
                if (users3.get(i2).supportsSwitchTo()) {
                    switchableUsers++;
                }
            }
            if (switchableUsers > 1) {
                for (int i3 = 0; i3 < userCount; i3++) {
                    int id = users3.get(i3).id;
                    if (getBoolean("lockscreen.disabled", false, id)) {
                        setBoolean("lockscreen.disabled", false, id);
                    }
                }
            }
            setString("migrated_lockscreen_disabled", "true", 0);
            Slog.i(TAG, "Migrated lockscreen disabled flag");
        }
        List<UserInfo> users4 = this.mUserManager.getUsers();
        int i4 = 0;
        while (true) {
            int i5 = i4;
            if (i5 >= users4.size()) {
                break;
            }
            UserInfo userInfo = users4.get(i5);
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
            } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e2) {
                Slog.e(TAG, "Unable to remove tied profile key", e2);
            }
            i4 = i5 + 1;
        }
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.watch") && getString("migrated_wear_lockscreen_disabled", null, 0) == null) {
            int userCount2 = users4.size();
            for (int i6 = 0; i6 < userCount2; i6++) {
                setBoolean("lockscreen.disabled", false, users4.get(i6).id);
            }
            setString("migrated_wear_lockscreen_disabled", "true", 0);
            Slog.i(TAG, "Migrated lockscreen_disabled for Wear devices");
        }
    }

    /* access modifiers changed from: private */
    public void migrateOldDataAfterSystemReady() {
        try {
            if (LockPatternUtils.frpCredentialEnabled(this.mContext) && !getBoolean("migrated_frp", false, 0)) {
                migrateFrpCredential();
                setBoolean("migrated_frp", true, 0);
                Slog.i(TAG, "Migrated migrated_frp.");
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "Unable to migrateOldDataAfterSystemReady", e);
        }
    }

    private void migrateFrpCredential() throws RemoteException {
        if (this.mStorage.readPersistentDataBlock() == LockSettingsStorage.PersistentData.NONE) {
            for (UserInfo userInfo : this.mUserManager.getUsers()) {
                if (LockPatternUtils.userOwnsFrpCredential(this.mContext, userInfo) && isUserSecure(userInfo.id)) {
                    synchronized (this.mSpManager) {
                        if (isSyntheticPasswordBasedCredentialLocked(userInfo.id)) {
                            this.mSpManager.migrateFrpPasswordLocked(getSyntheticPasswordHandleLocked(userInfo.id), userInfo, redactActualQualityToMostLenientEquivalentQuality((int) getLong("lockscreen.password_type", 0, userInfo.id)));
                        }
                    }
                    return;
                }
            }
        }
    }

    private int redactActualQualityToMostLenientEquivalentQuality(int quality) {
        if (quality == 131072 || quality == 196608) {
            return 131072;
        }
        if (quality == 262144 || quality == 327680 || quality == 393216) {
            return 262144;
        }
        return quality;
    }

    /* access modifiers changed from: protected */
    public final void checkWritePermission(int userId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, "LockSettingsWrite");
    }

    private final void checkPasswordReadPermission(int userId) {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, "LockSettingsRead");
    }

    private final void checkPasswordHavePermission(int userId) {
        if (this.mContext.checkCallingOrSelfPermission(PERMISSION) != 0) {
            EventLog.writeEvent(1397638484, new Object[]{"28251513", Integer.valueOf(getCallingUid()), BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS});
        }
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, "LockSettingsHave");
    }

    private final void checkReadPermission(String requestedKey, int userId) {
        int callingUid = Binder.getCallingUid();
        int i = 0;
        int i2 = 0;
        while (i2 < READ_CONTACTS_PROTECTED_SETTINGS.length) {
            if (!READ_CONTACTS_PROTECTED_SETTINGS[i2].equals(requestedKey) || this.mContext.checkCallingOrSelfPermission("android.permission.READ_CONTACTS") == 0) {
                i2++;
            } else {
                throw new SecurityException("uid=" + callingUid + " needs permission " + "android.permission.READ_CONTACTS" + " to read " + requestedKey + " for user " + userId);
            }
        }
        while (i < READ_PASSWORD_PROTECTED_SETTINGS.length) {
            if (!READ_PASSWORD_PROTECTED_SETTINGS[i].equals(requestedKey) || this.mContext.checkCallingOrSelfPermission(PERMISSION) == 0) {
                i++;
            } else {
                throw new SecurityException("uid=" + callingUid + " needs permission " + PERMISSION + " to read " + requestedKey + " for user " + userId);
            }
        }
    }

    public boolean getSeparateProfileChallengeEnabled(int userId) {
        boolean z;
        checkReadPermission(SEPARATE_PROFILE_CHALLENGE_KEY, userId);
        synchronized (this.mSeparateChallengeLock) {
            z = getBoolean(SEPARATE_PROFILE_CHALLENGE_KEY, false, userId);
        }
        return z;
    }

    public void setSeparateProfileChallengeEnabled(int userId, boolean enabled, String managedUserPassword) {
        checkWritePermission(userId);
        synchronized (this.mSeparateChallengeLock) {
            setSeparateProfileChallengeEnabledLocked(userId, enabled, managedUserPassword);
        }
        notifySeparateProfileChallengeChanged(userId);
    }

    @GuardedBy("mSeparateChallengeLock")
    private void setSeparateProfileChallengeEnabledLocked(int userId, boolean enabled, String managedUserPassword) {
        setBoolean(SEPARATE_PROFILE_CHALLENGE_KEY, enabled, userId);
        if (enabled) {
            this.mStorage.removeChildProfileLock(userId);
            removeKeystoreProfileKey(userId);
            return;
        }
        tieManagedProfileLockIfNecessary(userId, managedUserPassword);
    }

    private void notifySeparateProfileChallengeChanged(int userId) {
        DevicePolicyManagerInternal dpmi = (DevicePolicyManagerInternal) LocalServices.getService(DevicePolicyManagerInternal.class);
        if (dpmi != null) {
            dpmi.reportSeparateProfileChallengeChanged(userId);
        }
    }

    public void setBoolean(String key, boolean value, int userId) {
        checkWritePermission(userId);
        setStringUnchecked(key, userId, value ? "1" : "0");
    }

    public void setLong(String key, long value, int userId) {
        checkWritePermission(userId);
        setStringUnchecked(key, userId, Long.toString(value));
    }

    public void setString(String key, String value, int userId) {
        checkWritePermission(userId);
        setStringUnchecked(key, userId, value);
    }

    /* access modifiers changed from: protected */
    public void setStringUnchecked(String key, int userId, String value) {
        Preconditions.checkArgument(userId != -9999, "cannot store lock settings for FRP user");
        this.mStorage.writeKeyValue(key, value, userId);
        if (ArrayUtils.contains(SETTINGS_TO_BACKUP, key)) {
            BackupManager.dataChanged(BackupManagerService.SETTINGS_PACKAGE);
        }
    }

    public boolean getBoolean(String key, boolean defaultValue, int userId) {
        checkReadPermission(key, userId);
        String value = getStringUnchecked(key, null, userId);
        if (TextUtils.isEmpty(value)) {
            return defaultValue;
        }
        return value.equals("1") || value.equals("true");
    }

    public long getLong(String key, long defaultValue, int userId) {
        checkReadPermission(key, userId);
        String value = getStringUnchecked(key, null, userId);
        return TextUtils.isEmpty(value) ? defaultValue : Long.parseLong(value);
    }

    public String getString(String key, String defaultValue, int userId) {
        checkReadPermission(key, userId);
        return getStringUnchecked(key, defaultValue, userId);
    }

    public String getStringUnchecked(String key, String defaultValue, int userId) {
        if ("lock_pattern_autolock".equals(key)) {
            long ident = Binder.clearCallingIdentity();
            try {
                return this.mLockPatternUtils.isLockPatternEnabled(userId) ? "1" : "0";
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else if (userId == -9999) {
            return getFrpStringUnchecked(key);
        } else {
            if ("legacy_lock_pattern_enabled".equals(key)) {
                key = "lock_pattern_autolock";
            }
            return this.mStorage.readKeyValue(key, defaultValue, userId);
        }
    }

    private String getFrpStringUnchecked(String key) {
        if ("lockscreen.password_type".equals(key)) {
            return String.valueOf(readFrpPasswordQuality());
        }
        return null;
    }

    private int readFrpPasswordQuality() {
        return this.mStorage.readPersistentDataBlock().qualityForUi;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001d, code lost:
        return r3;
     */
    public boolean havePassword(int userId) throws RemoteException {
        checkPasswordHavePermission(userId);
        synchronized (this.mSpManager) {
            if (!isSyntheticPasswordBasedCredentialLocked(userId)) {
                return this.mStorage.hasPassword(userId);
            }
            boolean z = this.mSpManager.getCredentialType(getSyntheticPasswordHandleLocked(userId), userId) == 2;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001c, code lost:
        return r4;
     */
    public boolean havePattern(int userId) throws RemoteException {
        checkPasswordHavePermission(userId);
        synchronized (this.mSpManager) {
            if (!isSyntheticPasswordBasedCredentialLocked(userId)) {
                return this.mStorage.hasPattern(userId);
            }
            boolean z = true;
            if (this.mSpManager.getCredentialType(getSyntheticPasswordHandleLocked(userId), userId) != 1) {
                z = false;
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001a, code lost:
        return r3;
     */
    public boolean isUserSecure(int userId) {
        synchronized (this.mSpManager) {
            if (!isSyntheticPasswordBasedCredentialLocked(userId)) {
                return this.mStorage.hasCredential(userId);
            }
            boolean z = this.mSpManager.getCredentialType(getSyntheticPasswordHandleLocked(userId), userId) != -1;
        }
    }

    /* access modifiers changed from: protected */
    public void setKeystorePassword(String password, int userHandle) {
        KeyStore.getInstance().onUserPasswordChanged(userHandle, password);
    }

    /* access modifiers changed from: protected */
    public void unlockKeystore(String password, int userHandle) {
        KeyStore.getInstance().unlock(userHandle, password);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public String getDecryptedPasswordForTiedProfile(int userId) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, CertificateException, IOException {
        byte[] storedData = this.mStorage.readChildProfileLock(userId);
        if (storedData != null) {
            byte[] iv = Arrays.copyOfRange(storedData, 0, 12);
            byte[] encryptedPassword = Arrays.copyOfRange(storedData, 12, storedData.length);
            java.security.KeyStore keyStore = java.security.KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(2, (SecretKey) keyStore.getKey("profile_key_name_decrypt_" + userId, null), new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(encryptedPassword), StandardCharsets.UTF_8);
        }
        throw new FileNotFoundException("Child profile lock file not found");
    }

    private void unlockChildProfile(int profileHandle, boolean ignoreUserNotAuthenticated) throws RemoteException {
        try {
            doVerifyCredential(getDecryptedPasswordForTiedProfile(profileHandle), 2, false, 0, profileHandle, null);
        } catch (IOException | InvalidAlgorithmParameterException | InvalidKeyException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            if (e instanceof FileNotFoundException) {
                Slog.i(TAG, "Child profile key not found");
            } else if (!ignoreUserNotAuthenticated || !(e instanceof UserNotAuthenticatedException)) {
                Slog.e(TAG, "Failed to decrypt child profile key", e);
            } else {
                Slog.i(TAG, "Parent keystore seems locked, ignoring");
            }
        }
    }

    private void unlockUser(int userId, byte[] token, byte[] secret) {
        final CountDownLatch latch = new CountDownLatch(1);
        try {
            this.mActivityManager.unlockUser(userId, token, secret, new IProgressListener.Stub() {
                public void onStarted(int id, Bundle extras) throws RemoteException {
                    LockSettingsService.this.warnLog(LockSettingsService.TAG, "unlockUser started");
                }

                public void onProgress(int id, int progress, Bundle extras) throws RemoteException {
                    Log.d(LockSettingsService.TAG, "unlockUser progress " + progress);
                }

                public void onFinished(int id, Bundle extras) throws RemoteException {
                    LockSettingsService.this.warnLog(LockSettingsService.TAG, "unlockUser finished");
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
                        this.mStorage.writeCredentialHash(LockSettingsStorage.CredentialHash.createEmptyHash(), userId);
                        setKeystorePassword(null, userId);
                        fixateNewestUserKeyAuth(userId);
                        this.mStorage.removeChildProfileLock(userId);
                        removeKeystoreProfileKey(userId);
                        Slog.i(TAG, "finish unlock clone user after ota and remove profile key");
                    }
                }
                if (ui != null && !ui.isManagedProfile() && !ui.isClonedProfile()) {
                    for (UserInfo pi : this.mUserManager.getProfiles(userId)) {
                        if (tiedManagedProfileReadyToUnlock(pi) || (pi.isClonedProfile() && this.mStorage.hasChildProfileLock(pi.id) && this.mUserManager.isUserRunning(pi.id))) {
                            unlockChildProfile(pi.id, false);
                        }
                    }
                }
            } catch (RemoteException e2) {
                Log.d(TAG, "Failed to unlock child profile", e2);
            }
        } catch (RemoteException e3) {
            throw e3.rethrowAsRuntimeException();
        }
    }

    private boolean tiedManagedProfileReadyToUnlock(UserInfo userInfo) {
        return userInfo.isManagedProfile() && !this.mLockPatternUtils.isSeparateProfileChallengeEnabled(userInfo.id) && this.mStorage.hasChildProfileLock(userInfo.id) && this.mUserManager.isUserRunning(userInfo.id);
    }

    private Map<Integer, String> getDecryptedPasswordsForAllTiedProfiles(int userId) {
        if (this.mUserManager.getUserInfo(userId).isManagedProfile()) {
            return null;
        }
        Map<Integer, String> result = new ArrayMap<>();
        List<UserInfo> profiles = this.mUserManager.getProfiles(userId);
        int size = profiles.size();
        for (int i = 0; i < size; i++) {
            UserInfo profile = profiles.get(i);
            if (profile.isManagedProfile()) {
                int managedUserId = profile.id;
                if (!this.mLockPatternUtils.isSeparateProfileChallengeEnabled(managedUserId)) {
                    try {
                        result.put(Integer.valueOf(managedUserId), getDecryptedPasswordForTiedProfile(managedUserId));
                    } catch (IOException | InvalidAlgorithmParameterException | InvalidKeyException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
                        Slog.e(TAG, "getDecryptedPasswordsForAllTiedProfiles failed for user " + managedUserId, e);
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
                UserInfo profile = profiles.get(i);
                if (profile.isManagedProfile()) {
                    int managedUserId = profile.id;
                    if (!this.mLockPatternUtils.isSeparateProfileChallengeEnabled(managedUserId)) {
                        if (isSecure) {
                            tieManagedProfileLockIfNecessary(managedUserId, null);
                        } else {
                            if (profilePasswordMap == null || !profilePasswordMap.containsKey(Integer.valueOf(managedUserId))) {
                                Slog.wtf(TAG, "clear tied profile challenges, but no password supplied.");
                                setLockCredentialInternal(null, -1, null, 0, managedUserId);
                            } else {
                                setLockCredentialInternal(null, -1, profilePasswordMap.get(Integer.valueOf(managedUserId)), 0, managedUserId);
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
        return this.mUserManager.getUserInfo(userId) != null && this.mUserManager.getUserInfo(userId).isManagedProfile() && !this.mLockPatternUtils.isSeparateProfileChallengeEnabled(userId);
    }

    private boolean isManagedProfileWithSeparatedLock(int userId) {
        return this.mUserManager.getUserInfo(userId) != null && this.mUserManager.getUserInfo(userId).isManagedProfile() && this.mLockPatternUtils.isSeparateProfileChallengeEnabled(userId);
    }

    public void setLockCredential(String credential, int type, String savedCredential, int requestedQuality, int userId) throws RemoteException {
        checkWritePermission(userId);
        synchronized (this.mSeparateChallengeLock) {
            int oldCredentialType = getOldCredentialType(userId);
            setLockCredentialInternal(credential, type, savedCredential, requestedQuality, userId);
            setSeparateProfileChallengeEnabledLocked(userId, true, null);
            notifyPasswordChanged(userId);
            notifyPasswordStatusChanged(userId, getPasswordStatus(type, oldCredentialType));
            notifyModifyPwdForPrivSpacePwdProtect(credential, savedCredential, userId);
        }
        notifySeparateProfileChallengeChanged(userId);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0034, code lost:
        if (r10 != -1) goto L_0x006c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0036, code lost:
        if (r12 == null) goto L_0x003f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0038, code lost:
        android.util.Slog.wtf(TAG, "CredentialType is none, but credential is non-null.");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003f, code lost:
        clearUserKeyProtection(r11);
        getGateKeeperService().clearSecureUserId(r11);
        r9.mStorage.writeCredentialHash(com.android.server.locksettings.LockSettingsStorage.CredentialHash.createEmptyHash(), r11);
        setKeystorePassword(null, r11);
        fixateNewestUserKeyAuth(r11);
        synchronizeUnifiedWorkChallengeForProfiles(r11, null);
        notifyActivePasswordMetricsAvailable(null, r11);
        r9.mRecoverableKeyStoreManager.lockScreenSecretChanged(r10, r12, r11);
        android.util.Slog.w(TAG, "setLockPattern to null success");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x006b, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x006c, code lost:
        if (r12 == null) goto L_0x0147;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x006e, code lost:
        r14 = r9.mStorage.readCredentialHash(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0078, code lost:
        if (isManagedProfileWithUnifiedLock(r11) == false) goto L_0x0097;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x007a, code lost:
        if (r7 != null) goto L_0x00a6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        r0 = getDecryptedPasswordForTiedProfile(r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0081, code lost:
        r15 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0083, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0084, code lost:
        r1 = r0;
        android.util.Slog.e(TAG, "Failed to decrypt child profile key", r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x008d, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x008e, code lost:
        r1 = r0;
        android.util.Slog.i(TAG, "Child profile key not found");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0099, code lost:
        if (r14.hash != null) goto L_0x00a6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x009b, code lost:
        if (r7 == null) goto L_0x00a4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x009d, code lost:
        android.util.Slog.w(TAG, "Saved credential provided, but none stored");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00a4, code lost:
        r0 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00a6, code lost:
        r15 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00a9, code lost:
        monitor-enter(r9.mSpManager);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00ae, code lost:
        if (shouldMigrateToSyntheticPasswordLocked(r11) != false) goto L_0x00b0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00b0, code lost:
        initializeSyntheticPasswordLocked(r14.hash, r15, r14.type, r23, r11);
        spBasedSetLockCredentialInternalLocked(r12, r10, r15, r23, r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00c7, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00c9, code lost:
        r0 = enrollCredential(r14.hash, r15, r12, r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00cf, code lost:
        if (r0 == null) goto L_0x0119;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00d1, code lost:
        r8 = com.android.server.locksettings.LockSettingsStorage.CredentialHash.create(r0, r10);
        r9.mStorage.writeCredentialHash(r8, r11);
        r7 = getGateKeeperService().verifyChallenge(r11, 0, r8.hash, r12.getBytes());
        setUserKeyProtection(r11, r12, convertResponse(r7));
        fixateNewestUserKeyAuth(r11);
        r17 = r7;
        r18 = r8;
        doVerifyCredential(r12, r10, true, 0, r11, null);
        synchronizeUnifiedWorkChallengeForProfiles(r11, null);
        r9.mRecoverableKeyStoreManager.lockScreenSecretChanged(r10, r12, r11);
        android.util.Slog.w(TAG, "set new LockPassword success");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0118, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0119, code lost:
        android.util.Slog.e(TAG, "Failed to enroll password");
        notifyBigDataForPwdProtectFail(r11);
        r2 = new java.lang.StringBuilder();
        r2.append("Failed to enroll ");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0130, code lost:
        if (r10 != 2) goto L_0x0136;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0132, code lost:
        r3 = "password";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0136, code lost:
        r3 = "pattern";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0139, code lost:
        r2.append(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0143, code lost:
        throw new android.os.RemoteException(r2.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x014e, code lost:
        throw new android.os.RemoteException("Null credential with mismatched credential type");
     */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00aa A[SYNTHETIC] */
    public void setLockCredentialInternal(String credential, int credentialType, String savedCredential, int requestedQuality, int userId) throws RemoteException {
        int i = credentialType;
        int i2 = userId;
        String savedCredential2 = TextUtils.isEmpty(savedCredential) ? null : savedCredential;
        String credential2 = TextUtils.isEmpty(credential) ? null : credential;
        synchronized (this.mSpManager) {
            if (isSyntheticPasswordBasedCredentialLocked(i2)) {
                spBasedSetLockCredentialInternalLocked(credential2, i, savedCredential2, requestedQuality, i2);
            }
        }
    }

    private VerifyCredentialResponse convertResponse(GateKeeperResponse gateKeeperResponse) {
        return VerifyCredentialResponse.fromGateKeeperResponse(gateKeeperResponse);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void tieProfileLockToParent(int userId, String password) {
        java.security.KeyStore keyStore;
        byte[] randomLockSeed = password.getBytes(StandardCharsets.UTF_8);
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();
            keyStore = java.security.KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            keyStore.setEntry("profile_key_name_encrypt_" + userId, new KeyStore.SecretKeyEntry(secretKey), new KeyProtection.Builder(1).setBlockModes(new String[]{"GCM"}).setEncryptionPaddings(new String[]{"NoPadding"}).build());
            keyStore.setEntry("profile_key_name_decrypt_" + userId, new KeyStore.SecretKeyEntry(secretKey), new KeyProtection.Builder(2).setBlockModes(new String[]{"GCM"}).setEncryptionPaddings(new String[]{"NoPadding"}).setUserAuthenticationRequired(true).setUserAuthenticationValidityDurationSeconds(30).setCriticalToDeviceEncryption(true).build());
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(1, (SecretKey) keyStore.getKey("profile_key_name_encrypt_" + userId, null));
            byte[] encryptionResult = cipher.doFinal(randomLockSeed);
            byte[] iv = cipher.getIV();
            keyStore.deleteEntry("profile_key_name_encrypt_" + userId);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                if (iv.length == 12) {
                    outputStream.write(iv);
                    outputStream.write(encryptionResult);
                    this.mStorage.writeChildProfileLock(userId, outputStream.toByteArray());
                    return;
                }
                throw new RuntimeException("Invalid iv length: " + iv.length);
            } catch (IOException e) {
                throw new RuntimeException("Failed to concatenate byte arrays", e);
            }
        } catch (IOException | InvalidKeyException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e2) {
            throw new RuntimeException("Failed to encrypt key", e2);
        } catch (Throwable th) {
            keyStore.deleteEntry("profile_key_name_encrypt_" + userId);
            throw th;
        }
    }

    private byte[] enrollCredential(byte[] enrolledHandle, String enrolledCredential, String toEnroll, int userId) throws RemoteException {
        checkWritePermission(userId);
        GateKeeperResponse response = getGateKeeperService().enroll(userId, enrolledHandle, enrolledCredential == null ? null : enrolledCredential.getBytes(), toEnroll == null ? null : toEnroll.getBytes());
        if (response == null) {
            warnLog(TAG, "enrollCredential response null");
            return null;
        }
        byte[] hash = response.getPayload();
        if (hash != null) {
            setKeystorePassword(toEnroll, userId);
            warnLog(TAG, "enrollCredential response success");
        } else {
            warnLog(TAG, "Throttled while enrolling a password");
        }
        return hash;
    }

    private void setAuthlessUserKeyProtection(int userId, byte[] key) throws RemoteException {
        addUserKeyAuth(userId, null, key);
    }

    private void setUserKeyProtection(int userId, String credential, VerifyCredentialResponse vcr) throws RemoteException {
        if (vcr == null) {
            throw new RemoteException("Null response verifying a credential we just set");
        } else if (vcr.getResponseCode() == 0) {
            byte[] token = vcr.getPayload();
            if (token != null) {
                addUserKeyAuth(userId, token, secretFromCredential(credential));
                return;
            }
            throw new RemoteException("Empty payload verifying a credential we just set");
        } else {
            throw new RemoteException("Non-OK response verifying a credential we just set: " + vcr.getResponseCode());
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
        StringBuilder sb = new StringBuilder();
        sb.append("addUserKeyAuth U");
        sb.append(userId);
        sb.append("; tlen=");
        int i = 0;
        sb.append(token == null ? 0 : token.length);
        sb.append(" slen=");
        if (secret != null) {
            i = secret.length;
        }
        sb.append(i);
        warnLog(TAG, sb.toString());
        if (userId == 2147483646 && userInfo == null) {
            Log.w(TAG, "Parentcontrol doesn't have userinfo , do not addUserKeyAuth!");
            return;
        }
        IStorageManager storageManager = this.mInjector.getStorageManager();
        long callingId = Binder.clearCallingIdentity();
        try {
            storageManager.addUserKeyAuth(userId, userInfo.serialNumber, token, secret);
            ISDCardCryptedHelper helper = HwServiceFactory.getSDCardCryptedHelper();
            if (helper != null) {
                helper.addUserKeyAuth(userId, userInfo.serialNumber, token, secret);
            }
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    private void fixateNewestUserKeyAuth(int userId) throws RemoteException {
        if (2147483646 != userId) {
            warnLog(TAG, "fixateNewestUserKeyAuth: U=" + userId);
            IStorageManager storageManager = this.mInjector.getStorageManager();
            long callingId = Binder.clearCallingIdentity();
            try {
                storageManager.fixateNewestUserKeyAuth(userId);
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        }
    }

    public void resetKeyStore(int userId) throws RemoteException {
        int i = userId;
        checkWritePermission(userId);
        String managedUserDecryptedPassword = null;
        int managedUserId = -1;
        for (UserInfo pi : this.mUserManager.getProfiles(i)) {
            if (pi.isManagedProfile() && !this.mLockPatternUtils.isSeparateProfileChallengeEnabled(pi.id) && this.mStorage.hasChildProfileLock(pi.id)) {
                if (managedUserId == -1) {
                    try {
                        managedUserDecryptedPassword = getDecryptedPasswordForTiedProfile(pi.id);
                        managedUserId = pi.id;
                    } catch (IOException | InvalidAlgorithmParameterException | InvalidKeyException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
                        Slog.e(TAG, "Failed to decrypt child profile key", e);
                    }
                } else {
                    Slog.e(TAG, "More than one managed profile, uid1:" + managedUserId + ", uid2:" + pi.id);
                }
            }
        }
        try {
            for (int profileId : this.mUserManager.getProfileIdsWithDisabled(i)) {
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

    private VerifyCredentialResponse doVerifyCredential(String credential, int credentialType, boolean hasChallenge, long challenge, int userId, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        String credentialToVerify;
        int i = credentialType;
        int i2 = userId;
        if (!TextUtils.isEmpty(credential)) {
            boolean shouldReEnrollBaseZero = false;
            if (i2 != -9999 || Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) == 0) {
                try {
                    VerifyCredentialResponse response = spBasedDoVerifyCredential(credential, credentialType, hasChallenge, challenge, userId, progressCallback);
                    if (response != null) {
                        if (response.getResponseCode() == 0) {
                            this.mRecoverableKeyStoreManager.lockScreenSecretAvailable(i, credential, i2);
                        } else {
                            String str = credential;
                        }
                        return response;
                    }
                    String str2 = credential;
                    if (i2 == -9999) {
                        Slog.wtf(TAG, "Unexpected FRP credential type, should be SP based.");
                        return VerifyCredentialResponse.ERROR;
                    }
                    LockSettingsStorage.CredentialHash storedHash = this.mStorage.readCredentialHash(i2);
                    if (storedHash == null || storedHash.hash == null || storedHash.hash.length == 0) {
                        Slog.w(TAG, "no Pattern saved VerifyPattern success");
                        return VerifyCredentialResponse.OK;
                    } else if (storedHash.type != i) {
                        Slog.wtf(TAG, "doVerifyCredential type mismatch with stored credential?? stored: " + storedHash.type + " passed in: " + i);
                        return VerifyCredentialResponse.ERROR;
                    } else {
                        if (storedHash.type == 1 && storedHash.isBaseZeroPattern) {
                            shouldReEnrollBaseZero = true;
                        }
                        if (shouldReEnrollBaseZero) {
                            credentialToVerify = LockPatternUtils.patternStringToBaseZero(credential);
                        } else {
                            credentialToVerify = str2;
                        }
                        VerifyCredentialResponse response2 = verifyCredential(i2, storedHash, credentialToVerify, hasChallenge, challenge, progressCallback);
                        if (response2.getResponseCode() == 0) {
                            this.mStrongAuth.reportSuccessfulStrongAuthUnlock(i2);
                            if (shouldReEnrollBaseZero) {
                                setLockCredentialInternal(str2, storedHash.type, credentialToVerify, 65536, i2);
                            }
                        }
                        return response2;
                    }
                } catch (RuntimeException re) {
                    String str3 = credential;
                    RuntimeException runtimeException = re;
                    if (this.mUserManager.getUserInfo(i2).isManagedProfile()) {
                        Throwable e = re.getCause();
                        if (e == null || !(e instanceof UnrecoverableKeyException)) {
                            Slog.e(TAG, "spBasedDoVerifyCredential failed due to " + re.toString());
                        } else {
                            Slog.e(TAG, "spBasedDoVerifyCredential failed due to RuntimeException->UnrecoverableKeyException");
                            return VerifyCredentialResponse.ERROR;
                        }
                    }
                    throw re;
                }
            } else {
                Slog.e(TAG, "FRP credential can only be verified prior to provisioning.");
                return VerifyCredentialResponse.ERROR;
            }
        } else {
            String str4 = credential;
            this.mLockPatternUtils.monitorCheckPassword(1002, null);
            throw new IllegalArgumentException("Credential can't be null or empty");
        }
    }

    public VerifyCredentialResponse verifyTiedProfileChallenge(String credential, int type, long challenge, int userId) throws RemoteException {
        int i = userId;
        checkPasswordReadPermission(i);
        if (isManagedProfileWithUnifiedLock(i)) {
            VerifyCredentialResponse parentResponse = doVerifyCredential(credential, type, true, challenge, this.mUserManager.getProfileParent(i).id, null);
            if (parentResponse.getResponseCode() != 0) {
                return parentResponse;
            }
            try {
                return doVerifyCredential(getDecryptedPasswordForTiedProfile(i), 2, true, challenge, i, null);
            } catch (IOException | InvalidAlgorithmParameterException | InvalidKeyException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
                Slog.e(TAG, "Failed to decrypt child profile key", e);
                throw new RemoteException("Unable to get tied profile token");
            }
        } else {
            throw new RemoteException("User id must be managed profile with unified lock");
        }
    }

    private VerifyCredentialResponse verifyCredential(int userId, LockSettingsStorage.CredentialHash storedHash, String credential, boolean hasChallenge, long challenge, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        VerifyCredentialResponse response;
        int i;
        byte[] hash;
        int i2 = userId;
        LockSettingsStorage.CredentialHash credentialHash = storedHash;
        String str = credential;
        if ((credentialHash == null || credentialHash.hash.length == 0) && TextUtils.isEmpty(credential)) {
            Slog.w(TAG, "no stored Password/Pattern, verifyCredential success");
            return VerifyCredentialResponse.OK;
        } else if (credentialHash == null || TextUtils.isEmpty(credential)) {
            Slog.w(TAG, "no entered Password/Pattern, verifyCredential ERROR");
            return VerifyCredentialResponse.ERROR;
        } else {
            StrictMode.noteDiskRead();
            if (credentialHash.version == 0) {
                if (credentialHash.type == 1) {
                    hash = LockPatternUtils.patternToHash(LockPatternUtils.stringToPattern(credential));
                } else {
                    hash = this.mLockPatternUtils.legacyPasswordToHash(str, i2).getBytes(StandardCharsets.UTF_8);
                }
                if (!Arrays.equals(hash, credentialHash.hash)) {
                    return VerifyCredentialResponse.ERROR;
                }
                if (credentialHash.type == 1) {
                    unlockKeystore(LockPatternUtils.patternStringToBaseZero(credential), i2);
                } else {
                    unlockKeystore(str, i2);
                }
                Slog.i(TAG, "Unlocking user with fake token: " + i2);
                byte[] fakeToken = String.valueOf(userId).getBytes();
                unlockUser(i2, fakeToken, fakeToken);
                setLockCredentialInternal(str, credentialHash.type, null, credentialHash.type == 1 ? 65536 : 327680, i2);
                if (!hasChallenge) {
                    notifyActivePasswordMetricsAvailable(str, i2);
                    this.mRecoverableKeyStoreManager.lockScreenSecretAvailable(credentialHash.type, str, i2);
                    return VerifyCredentialResponse.OK;
                }
            }
            try {
                if (getGateKeeperService() == null) {
                    this.mLockPatternUtils.monitorCheckPassword(1006, null);
                    return VerifyCredentialResponse.ERROR;
                }
                GateKeeperResponse gateKeeperResponse = getGateKeeperService().verifyChallenge(i2, challenge, credentialHash.hash, credential.getBytes());
                VerifyCredentialResponse response2 = convertResponse(gateKeeperResponse);
                boolean shouldReEnroll = gateKeeperResponse.getShouldReEnroll();
                if (response2.getResponseCode() == 0) {
                    if (progressCallback != null) {
                        progressCallback.onCredentialVerified();
                    }
                    notifyActivePasswordMetricsAvailable(str, i2);
                    unlockKeystore(str, i2);
                    unlockUser(i2, response2.getPayload(), secretFromCredential(credential));
                    warnLog(TAG, "Unlocking user finish. U" + i2);
                    if (isManagedProfileWithSeparatedLock(userId)) {
                        ((TrustManager) this.mContext.getSystemService("trust")).setDeviceLockedForUser(i2, false);
                    }
                    int reEnrollQuality = credentialHash.type == 1 ? 65536 : 327680;
                    if (shouldReEnroll) {
                        setLockCredentialInternal(str, credentialHash.type, str, reEnrollQuality, i2);
                        response = response2;
                        i = 1;
                    } else {
                        synchronized (this.mSpManager) {
                            try {
                                if (shouldMigrateToSyntheticPasswordLocked(userId)) {
                                    response = response2;
                                    i = 1;
                                    try {
                                        activateEscrowTokens(initializeSyntheticPasswordLocked(credentialHash.hash, str, credentialHash.type, reEnrollQuality, i2), i2);
                                    } catch (Throwable th) {
                                        th = th;
                                        throw th;
                                    }
                                } else {
                                    response = response2;
                                    i = 1;
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                VerifyCredentialResponse verifyCredentialResponse = response2;
                                throw th;
                            }
                        }
                    }
                    this.mRecoverableKeyStoreManager.lockScreenSecretAvailable(credentialHash.type, str, i2);
                    if ((getStrongAuthForUser(userId) & i) != 0) {
                        Slog.w(TAG, "clear BOOT_AUTH flag after verifyCredential");
                        requireStrongAuth(0, i2);
                    }
                    Slog.w(TAG, "verifyCredential passed by GateKeeper");
                } else {
                    response = response2;
                    if (response.getResponseCode() == 1 && response.getTimeout() > 0) {
                        requireStrongAuth(8, i2);
                    }
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
        this.mHandler.post(new Runnable(metrics, userId) {
            private final /* synthetic */ PasswordMetrics f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                LockSettingsService.lambda$notifyActivePasswordMetricsAvailable$0(LockSettingsService.this, this.f$1, this.f$2);
            }
        });
    }

    public static /* synthetic */ void lambda$notifyActivePasswordMetricsAvailable$0(LockSettingsService lockSettingsService, PasswordMetrics metrics, int userId) {
        DevicePolicyManager dpm = (DevicePolicyManager) lockSettingsService.mContext.getSystemService("device_policy");
        if (dpm != null) {
            dpm.setActivePasswordState(metrics, userId);
        } else {
            Log.e(TAG, "can not get DevicePolicyManager");
        }
    }

    private void notifyPasswordChanged(int userId) {
        this.mHandler.post(new Runnable(userId) {
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                LockSettingsService.lambda$notifyPasswordChanged$1(LockSettingsService.this, this.f$1);
            }
        });
    }

    public static /* synthetic */ void lambda$notifyPasswordChanged$1(LockSettingsService lockSettingsService, int userId) {
        DevicePolicyManager dpm = (DevicePolicyManager) lockSettingsService.mContext.getSystemService("device_policy");
        if (dpm != null) {
            dpm.reportPasswordChanged(userId);
        } else {
            Log.e(TAG, "can not get DevicePolicyManager");
        }
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
                if (!this.mLockPatternUtils.isLockPasswordEnabled(userId) || checkCredential(password, 2, userId, null).getResponseCode() != 0) {
                    return false;
                }
                return true;
            } catch (Exception e2) {
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    /* access modifiers changed from: private */
    public void removeUser(int userId, boolean unknownUser) {
        this.mSpManager.removeUser(userId);
        this.mStorage.removeUser(userId);
        this.mStrongAuth.removeUser(userId);
        tryRemoveUserFromSpCacheLater(userId);
        android.security.KeyStore.getInstance().onUserRemoved(userId);
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

    private void removeKeystoreProfileKey(int targetUserId) {
        try {
            java.security.KeyStore keyStore = java.security.KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            keyStore.deleteEntry("profile_key_name_encrypt_" + targetUserId);
            keyStore.deleteEntry("profile_key_name_decrypt_" + targetUserId);
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            Slog.e(TAG, "Unable to remove keystore profile key for user:" + targetUserId, e);
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
        return callingUid == 2000 || callingUid == 0;
    }

    private void enforceShell() {
        if (!isCallerShell()) {
            throw new SecurityException("Caller must be shell");
        }
    }

    /* JADX WARNING: type inference failed for: r2v1, types: [android.os.Binder] */
    /* JADX WARNING: Multi-variable type inference failed */
    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) throws RemoteException {
        enforceShell();
        long origId = Binder.clearCallingIdentity();
        try {
            new LockSettingsShellCommand(this.mContext, new LockPatternUtils(this.mContext)).exec(this, in, out, err, args, callback, resultReceiver);
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    public void initRecoveryServiceWithSigFile(String rootCertificateAlias, byte[] recoveryServiceCertFile, byte[] recoveryServiceSigFile) throws RemoteException {
        this.mRecoverableKeyStoreManager.initRecoveryServiceWithSigFile(rootCertificateAlias, recoveryServiceCertFile, recoveryServiceSigFile);
    }

    public KeyChainSnapshot getKeyChainSnapshot() throws RemoteException {
        return this.mRecoverableKeyStoreManager.getKeyChainSnapshot();
    }

    public void setSnapshotCreatedPendingIntent(PendingIntent intent) throws RemoteException {
        this.mRecoverableKeyStoreManager.setSnapshotCreatedPendingIntent(intent);
    }

    public void setServerParams(byte[] serverParams) throws RemoteException {
        this.mRecoverableKeyStoreManager.setServerParams(serverParams);
    }

    public void setRecoveryStatus(String alias, int status) throws RemoteException {
        this.mRecoverableKeyStoreManager.setRecoveryStatus(alias, status);
    }

    public Map getRecoveryStatus() throws RemoteException {
        return this.mRecoverableKeyStoreManager.getRecoveryStatus();
    }

    public void setRecoverySecretTypes(int[] secretTypes) throws RemoteException {
        this.mRecoverableKeyStoreManager.setRecoverySecretTypes(secretTypes);
    }

    public int[] getRecoverySecretTypes() throws RemoteException {
        return this.mRecoverableKeyStoreManager.getRecoverySecretTypes();
    }

    public byte[] startRecoverySessionWithCertPath(String sessionId, String rootCertificateAlias, RecoveryCertPath verifierCertPath, byte[] vaultParams, byte[] vaultChallenge, List<KeyChainProtectionParams> secrets) throws RemoteException {
        return this.mRecoverableKeyStoreManager.startRecoverySessionWithCertPath(sessionId, rootCertificateAlias, verifierCertPath, vaultParams, vaultChallenge, secrets);
    }

    public Map<String, String> recoverKeyChainSnapshot(String sessionId, byte[] recoveryKeyBlob, List<WrappedApplicationKey> applicationKeys) throws RemoteException {
        return this.mRecoverableKeyStoreManager.recoverKeyChainSnapshot(sessionId, recoveryKeyBlob, applicationKeys);
    }

    public void closeSession(String sessionId) throws RemoteException {
        this.mRecoverableKeyStoreManager.closeSession(sessionId);
    }

    public void removeKey(String alias) throws RemoteException {
        this.mRecoverableKeyStoreManager.removeKey(alias);
    }

    public String generateKey(String alias) throws RemoteException {
        return this.mRecoverableKeyStoreManager.generateKey(alias);
    }

    public String importKey(String alias, byte[] keyBytes) throws RemoteException {
        return this.mRecoverableKeyStoreManager.importKey(alias, keyBytes);
    }

    public String getKey(String alias) throws RemoteException {
        return this.mRecoverableKeyStoreManager.getKey(alias);
    }

    /* access modifiers changed from: protected */
    public synchronized IGateKeeperService getGateKeeperService() throws RemoteException {
        if (this.mGateKeeperService != null) {
            return this.mGateKeeperService;
        }
        IBinder service = ServiceManager.getService("android.service.gatekeeper.IGateKeeperService");
        if (service != null) {
            service.linkToDeath(new GateKeeperDiedRecipient(), 0);
            this.mGateKeeperService = IGateKeeperService.Stub.asInterface(service);
            return this.mGateKeeperService;
        }
        Slog.e(TAG, "Unable to acquire GateKeeperService");
        return null;
    }

    private void onAuthTokenKnownForUser(int userId, SyntheticPasswordManager.AuthenticationToken auth) {
        Slog.i(TAG, "Caching SP for user " + userId);
        synchronized (this.mSpManager) {
            this.mSpCache.put(userId, auth);
        }
        tryRemoveUserFromSpCacheLater(userId);
        if (this.mAuthSecretService != null && this.mUserManager.getUserInfo(userId).isPrimary()) {
            try {
                byte[] rawSecret = auth.deriveVendorAuthSecret();
                ArrayList<Byte> secret = new ArrayList<>(rawSecret.length);
                for (byte valueOf : rawSecret) {
                    secret.add(Byte.valueOf(valueOf));
                }
                this.mAuthSecretService.primaryUserCredential(secret);
            } catch (RemoteException e) {
                Slog.w(TAG, "Failed to pass primary user secret to AuthSecret HAL", e);
            }
        }
    }

    /* access modifiers changed from: private */
    public void tryRemoveUserFromSpCacheLater(int userId) {
        this.mHandler.post(new Runnable(userId) {
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                LockSettingsService.lambda$tryRemoveUserFromSpCacheLater$2(LockSettingsService.this, this.f$1);
            }
        });
    }

    public static /* synthetic */ void lambda$tryRemoveUserFromSpCacheLater$2(LockSettingsService lockSettingsService, int userId) {
        if (!lockSettingsService.shouldCacheSpForUser(userId)) {
            Slog.i(TAG, "Removing SP from cache for user " + userId);
            synchronized (lockSettingsService.mSpManager) {
                lockSettingsService.mSpCache.remove(userId);
            }
        }
    }

    private boolean shouldCacheSpForUser(int userId) {
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "user_setup_complete", 0, userId) == 0) {
            return true;
        }
        DevicePolicyManagerInternal dpmi = (DevicePolicyManagerInternal) LocalServices.getService(DevicePolicyManagerInternal.class);
        if (dpmi == null) {
            return false;
        }
        return dpmi.canUserHaveUntrustedCredentialReset(userId);
    }

    /* access modifiers changed from: protected */
    @GuardedBy("mSpManager")
    @VisibleForTesting
    public SyntheticPasswordManager.AuthenticationToken initializeSyntheticPasswordLocked(byte[] credentialHash, String credential, int credentialType, int requestedQuality, int userId) throws RemoteException {
        warnLog(TAG, "Initialize SyntheticPassword for user: " + userId);
        SyntheticPasswordManager.AuthenticationToken auth = this.mSpManager.newSyntheticPasswordAndSid(getGateKeeperService(), credentialHash, credential, userId);
        onAuthTokenKnownForUser(userId, auth);
        if (auth == null) {
            Slog.wtf(TAG, "initializeSyntheticPasswordLocked returns null auth token");
            return null;
        }
        long handle = this.mSpManager.createPasswordBasedSyntheticPassword(getGateKeeperService(), credential, credentialType, auth, requestedQuality, userId);
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
        setLong("sp-handle", handle, userId);
        warnLog(TAG, "initializeSP writeback handle: " + Long.toHexString(handle));
        fixateNewestUserKeyAuth(userId);
        return auth;
    }

    private long getSyntheticPasswordHandleLocked(int userId) {
        return getLong("sp-handle", 0, userId);
    }

    private boolean isSyntheticPasswordBasedCredentialLocked(int userId) {
        boolean z = false;
        if (userId == -9999) {
            int type = this.mStorage.readPersistentDataBlock().type;
            if (type == 1 || type == 2) {
                z = true;
            }
            return z;
        }
        long handle = getSyntheticPasswordHandleLocked(userId);
        if (!(getLong("enable-sp", 1, 0) == 0 || handle == 0)) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public boolean shouldMigrateToSyntheticPasswordLocked(int userId) {
        boolean z = false;
        if (!StorageManager.isUserKeyUnlocked(userId)) {
            warnLog(TAG, "User locked and skip MigrateToSyntheticPassword!!!");
            return false;
        }
        long handle = getSyntheticPasswordHandleLocked(userId);
        if (getLong("enable-sp", 1, 0) != 0 && handle == 0) {
            z = true;
        }
        return z;
    }

    private void enableSyntheticPasswordLocked() {
        setLong("enable-sp", 1, 0);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0084, code lost:
        if (r3.getResponseCode() != 0) goto L_0x00df;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0086, code lost:
        notifyActivePasswordMetricsAvailable(r14, r13);
        unlockKeystore(r0.authToken.deriveKeyStorePassword(), r13);
        android.util.Slog.i(TAG, "Unlocking user " + r13 + " with secret only, length " + r0.authToken.deriveDiskEncryptionKey().length);
        unlockUser(r13, r4, r5);
        activateEscrowTokens(r0.authToken, r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00c3, code lost:
        if (isManagedProfileWithSeparatedLock(r13) == false) goto L_0x00d4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00c5, code lost:
        ((android.app.trust.TrustManager) r1.mContext.getSystemService("trust")).setDeviceLockedForUser(r13, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00d4, code lost:
        r1.mStrongAuth.reportSuccessfulStrongAuthUnlock(r13);
        onAuthTokenKnownForUser(r13, r0.authToken);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00e4, code lost:
        if (r3.getResponseCode() != 1) goto L_0x010c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00ea, code lost:
        if (r3.getTimeout() <= 0) goto L_0x010c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00ec, code lost:
        requireStrongAuth(8, r13);
        warnLog(TAG, "verifyLocked:" + r3.getTimeout());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x010c, code lost:
        return r3;
     */
    private VerifyCredentialResponse spBasedDoVerifyCredential(String userCredential, int credentialType, boolean hasChallenge, long challenge, int userId, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        byte[] bArr;
        int i = credentialType;
        int i2 = userId;
        String userCredential2 = i == -1 ? null : userCredential;
        synchronized (this.mSpManager) {
            try {
                if (!isSyntheticPasswordBasedCredentialLocked(i2)) {
                    return null;
                }
                if (i2 == -9999) {
                    VerifyCredentialResponse verifyFrpCredential = this.mSpManager.verifyFrpCredential(getGateKeeperService(), userCredential2, i, progressCallback);
                    return verifyFrpCredential;
                }
                SyntheticPasswordManager.AuthenticationResult authResult = this.mSpManager.unwrapPasswordBasedSyntheticPassword(getGateKeeperService(), getSyntheticPasswordHandleLocked(i2), userCredential2, i2, progressCallback);
                if (authResult.credentialType != i) {
                    Slog.e(TAG, "Credential type mismatch.");
                    VerifyCredentialResponse verifyCredentialResponse = VerifyCredentialResponse.ERROR;
                    return verifyCredentialResponse;
                }
                VerifyCredentialResponse response = authResult.gkResponse;
                if (response.getResponseCode() == 0) {
                    bArr = null;
                    response = this.mSpManager.verifyChallenge(getGateKeeperService(), authResult.authToken, challenge, i2);
                    if (response.getResponseCode() != 0) {
                        Slog.wtf(TAG, "verifyChallenge with SP failed.");
                        VerifyCredentialResponse verifyCredentialResponse2 = VerifyCredentialResponse.ERROR;
                        return verifyCredentialResponse2;
                    }
                } else {
                    bArr = null;
                }
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    @GuardedBy("mSpManager")
    private long setLockCredentialWithAuthTokenLocked(String credential, int credentialType, SyntheticPasswordManager.AuthenticationToken auth, int requestedQuality, int userId) throws RemoteException {
        Map<Integer, String> profilePasswords;
        String str = credential;
        int i = userId;
        long newHandle = this.mSpManager.createPasswordBasedSyntheticPassword(getGateKeeperService(), str, credentialType, auth, requestedQuality, i);
        flog(TAG, "setLockCredentialWithAuthTokenLocked U" + i + "; create new Handle: " + Long.toHexString(newHandle));
        if (str != null) {
            profilePasswords = null;
            if (this.mSpManager.hasSidForUser(i)) {
                this.mSpManager.verifyChallenge(getGateKeeperService(), auth, 0, i);
                SyntheticPasswordManager.AuthenticationToken authenticationToken = auth;
            } else {
                SyntheticPasswordManager.AuthenticationToken authenticationToken2 = auth;
                this.mSpManager.newSidForUser(getGateKeeperService(), authenticationToken2, i);
                this.mSpManager.verifyChallenge(getGateKeeperService(), authenticationToken2, 0, i);
                setAuthlessUserKeyProtection(i, auth.deriveDiskEncryptionKey());
                setKeystorePassword(auth.deriveKeyStorePassword(), i);
                setLong("sp-handle", newHandle, i);
                flog(TAG, "setCredentialWithAuthT: writeback HANDLE");
                fixateNewestUserKeyAuth(i);
            }
        } else {
            SyntheticPasswordManager.AuthenticationToken authenticationToken3 = auth;
            profilePasswords = getDecryptedPasswordsForAllTiedProfiles(i);
            this.mSpManager.clearSidForUser(i);
            getGateKeeperService().clearSecureUserId(i);
            flog(TAG, "setCredentialWithAuthT: clearSecureUserId");
            clearUserKeyProtection(i);
            fixateNewestUserKeyAuth(i);
            setKeystorePassword(null, i);
            handleUserClearLockForAnti(i);
        }
        setLong("sp-handle", newHandle, i);
        synchronizeUnifiedWorkChallengeForProfiles(i, profilePasswords);
        notifyActivePasswordMetricsAvailable(str, i);
        flog(TAG, "setCredentialWithAuthT fin.");
        return newHandle;
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0065  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x006b  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0088  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00ac  */
    @GuardedBy("mSpManager")
    private void spBasedSetLockCredentialInternalLocked(String credential, int credentialType, String savedCredential, int requestedQuality, int userId) throws RemoteException {
        String savedCredential2;
        SyntheticPasswordManager.AuthenticationToken auth;
        SyntheticPasswordManager.AuthenticationToken auth2;
        int i = credentialType;
        int i2 = userId;
        if (isManagedProfileWithUnifiedLock(i2)) {
            try {
                savedCredential2 = getDecryptedPasswordForTiedProfile(i2);
            } catch (FileNotFoundException e) {
                FileNotFoundException fileNotFoundException = e;
                Slog.i(TAG, "Child profile key not found");
            } catch (IOException | InvalidAlgorithmParameterException | InvalidKeyException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e2) {
                Exception exc = e2;
                Slog.e(TAG, "Failed to decrypt child profile key", e2);
            }
            long handle = getSyntheticPasswordHandleLocked(i2);
            SyntheticPasswordManager.AuthenticationResult authResult = this.mSpManager.unwrapPasswordBasedSyntheticPassword(getGateKeeperService(), handle, savedCredential2, i2, null);
            VerifyCredentialResponse response = authResult.gkResponse;
            auth = authResult.authToken;
            if (savedCredential2 == null && auth == null) {
                StringBuilder sb = new StringBuilder();
                sb.append("Failed to enroll ");
                sb.append(i == 2 ? "password" : "pattern");
                throw new RemoteException(sb.toString());
            }
            boolean untrustedReset = false;
            if (auth == null) {
                onAuthTokenKnownForUser(i2, auth);
            } else if (response == null || response.getResponseCode() != -1) {
                String str = credential;
                long j = handle;
                int i3 = i2;
                StringBuilder sb2 = new StringBuilder();
                sb2.append("spBasedSetLockCredentialInternalLocked: ");
                sb2.append(response != null ? "rate limit exceeded" : "failed");
                Slog.w(TAG, sb2.toString());
                return;
            } else {
                Slog.w(TAG, "Untrusted credential change invoked");
                auth = this.mSpCache.get(i2);
                untrustedReset = true;
            }
            auth2 = auth;
            boolean untrustedReset2 = untrustedReset;
            if (auth2 == null) {
                if (untrustedReset2) {
                    this.mSpManager.newSidForUser(getGateKeeperService(), auth2, i2);
                }
                int i4 = i2;
                setLockCredentialWithAuthTokenLocked(credential, i, auth2, requestedQuality, i2);
                this.mSpManager.destroyPasswordBasedSyntheticPassword(handle, i4);
                this.mRecoverableKeyStoreManager.lockScreenSecretChanged(i, credential, i4);
                return;
            }
            String str2 = credential;
            long j2 = handle;
            int i5 = i2;
            throw new IllegalStateException("Untrusted credential reset not possible without cached SP");
        }
        savedCredential2 = savedCredential;
        long handle2 = getSyntheticPasswordHandleLocked(i2);
        SyntheticPasswordManager.AuthenticationResult authResult2 = this.mSpManager.unwrapPasswordBasedSyntheticPassword(getGateKeeperService(), handle2, savedCredential2, i2, null);
        VerifyCredentialResponse response2 = authResult2.gkResponse;
        auth = authResult2.authToken;
        if (savedCredential2 == null) {
        }
        boolean untrustedReset3 = false;
        if (auth == null) {
        }
        auth2 = auth;
        boolean untrustedReset22 = untrustedReset3;
        if (auth2 == null) {
        }
    }

    public byte[] getHashFactor(String currentCredential, int userId) throws RemoteException {
        checkPasswordReadPermission(userId);
        if (TextUtils.isEmpty(currentCredential)) {
            currentCredential = null;
        }
        if (isManagedProfileWithUnifiedLock(userId)) {
            try {
                currentCredential = getDecryptedPasswordForTiedProfile(userId);
            } catch (Exception e) {
                Slog.e(TAG, "Failed to get work profile credential", e);
                return null;
            }
        }
        synchronized (this.mSpManager) {
            if (!isSyntheticPasswordBasedCredentialLocked(userId)) {
                Slog.w(TAG, "Synthetic password not enabled");
                return null;
            }
            SyntheticPasswordManager.AuthenticationResult auth = this.mSpManager.unwrapPasswordBasedSyntheticPassword(getGateKeeperService(), getSyntheticPasswordHandleLocked(userId), currentCredential, userId, null);
            if (auth.authToken == null) {
                Slog.w(TAG, "Current credential is incorrect");
                return null;
            }
            byte[] derivePasswordHashFactor = auth.authToken.derivePasswordHashFactor();
            return derivePasswordHashFactor;
        }
    }

    /* access modifiers changed from: private */
    public long addEscrowToken(byte[] token, int userId) throws RemoteException {
        long handle;
        synchronized (this.mSpManager) {
            enableSyntheticPasswordLocked();
            SyntheticPasswordManager.AuthenticationToken auth = null;
            if (!isUserSecure(userId)) {
                if (shouldMigrateToSyntheticPasswordLocked(userId)) {
                    auth = initializeSyntheticPasswordLocked(null, null, -1, 0, userId);
                } else {
                    auth = this.mSpManager.unwrapPasswordBasedSyntheticPassword(getGateKeeperService(), getSyntheticPasswordHandleLocked(userId), null, userId, null).authToken;
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

    private void activateEscrowTokens(SyntheticPasswordManager.AuthenticationToken auth, int userId) {
        synchronized (this.mSpManager) {
            disableEscrowTokenOnNonManagedDevicesIfNeeded(userId);
            for (Long longValue : this.mSpManager.getPendingTokensForUser(userId)) {
                long handle = longValue.longValue();
                Slog.i(TAG, String.format("activateEscrowTokens: %x %d ", new Object[]{Long.valueOf(handle), Integer.valueOf(userId)}));
                this.mSpManager.activateTokenBasedSyntheticPassword(handle, auth, userId);
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isEscrowTokenActive(long handle, int userId) {
        boolean existsHandle;
        synchronized (this.mSpManager) {
            existsHandle = this.mSpManager.existsHandle(handle, userId);
        }
        return existsHandle;
    }

    /* access modifiers changed from: private */
    public boolean removeEscrowToken(long handle, int userId) {
        synchronized (this.mSpManager) {
            if (handle == getSyntheticPasswordHandleLocked(userId)) {
                Slog.w(TAG, "Cannot remove password handle");
                return false;
            } else if (this.mSpManager.removePendingToken(handle, userId)) {
                return true;
            } else {
                if (!this.mSpManager.existsHandle(handle, userId)) {
                    return false;
                }
                this.mSpManager.destroyTokenBasedSyntheticPassword(handle, userId);
                return true;
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean setLockCredentialWithToken(String credential, int type, long tokenHandle, byte[] token, int requestedQuality, int userId) throws RemoteException {
        boolean result;
        int oldCredentialType = getOldCredentialType(userId);
        synchronized (this.mSpManager) {
            if (this.mSpManager.hasEscrowData(userId)) {
                result = setLockCredentialWithTokenInternal(credential, type, tokenHandle, token, requestedQuality, userId);
            } else {
                throw new SecurityException("Escrow token is disabled on the current user");
            }
        }
        if (result) {
            synchronized (this.mSeparateChallengeLock) {
                setSeparateProfileChallengeEnabledLocked(userId, true, null);
            }
            notifyPasswordChanged(userId);
            notifySeparateProfileChallengeChanged(userId);
            notifyPasswordStatusChanged(userId, getPasswordStatus(type, oldCredentialType));
        }
        return result;
    }

    private boolean setLockCredentialWithTokenInternal(String credential, int type, long tokenHandle, byte[] token, int requestedQuality, int userId) throws RemoteException {
        int i = userId;
        synchronized (this.mSpManager) {
            try {
                SyntheticPasswordManager.AuthenticationResult result = this.mSpManager.unwrapTokenBasedSyntheticPassword(getGateKeeperService(), tokenHandle, token, i);
                if (result.authToken == null) {
                    Slog.w(TAG, "Invalid escrow token supplied");
                    return false;
                } else if (result.gkResponse.getResponseCode() != 0) {
                    Slog.e(TAG, "Obsolete token: synthetic password derived but it fails GK verification.");
                    return false;
                } else {
                    int i2 = requestedQuality;
                    try {
                        setLong("lockscreen.password_type", (long) i2, i);
                        long oldHandle = getSyntheticPasswordHandleLocked(i);
                        setLockCredentialWithAuthTokenLocked(credential, type, result.authToken, i2, i);
                        this.mSpManager.destroyPasswordBasedSyntheticPassword(oldHandle, i);
                        onAuthTokenKnownForUser(i, result.authToken);
                        return true;
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                int i3 = requestedQuality;
                throw th;
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean unlockUserWithToken(long tokenHandle, byte[] token, int userId) throws RemoteException {
        synchronized (this.mSpManager) {
            if (this.mSpManager.hasEscrowData(userId)) {
                SyntheticPasswordManager.AuthenticationResult authResult = this.mSpManager.unwrapTokenBasedSyntheticPassword(getGateKeeperService(), tokenHandle, token, userId);
                if (authResult.authToken == null) {
                    Slog.w(TAG, "Invalid escrow token supplied");
                    return false;
                }
                unlockUser(userId, null, authResult.authToken.deriveDiskEncryptionKey());
                onAuthTokenKnownForUser(userId, authResult.authToken);
                return true;
            }
            throw new SecurityException("Escrow token is disabled on the current user");
        }
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            pw.println("Current lock settings service state:");
            pw.println(String.format("SP Enabled = %b", new Object[]{Boolean.valueOf(this.mLockPatternUtils.isSyntheticPasswordEnabled())}));
            List<UserInfo> users = this.mUserManager.getUsers();
            for (int user = 0; user < users.size(); user++) {
                int userId = users.get(user).id;
                pw.println("    User " + userId);
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
            if (this.mUserManager.getUserInfo(userId) == null || !this.mUserManager.getUserInfo(userId).isManagedProfile()) {
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
            } else {
                Slog.i(TAG, "Managed profile can have escrow token");
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: package-private */
    public void warnLog(String tag, String msg) {
    }

    /* access modifiers changed from: package-private */
    public void flog(String tag, String msg) {
    }
}
