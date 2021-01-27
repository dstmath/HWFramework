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
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.authsecret.V1_0.IAuthSecret;
import android.hardware.biometrics.BiometricManager;
import android.hardware.face.FaceManager;
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
import android.util.ArraySet;
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
import com.android.server.backup.UserBackupManagerService;
import com.android.server.locksettings.LockSettingsStorage;
import com.android.server.locksettings.SyntheticPasswordManager;
import com.android.server.locksettings.recoverablekeystore.RecoverableKeyStoreManager;
import com.android.server.pm.DumpState;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
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
import java.util.Set;
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
    protected static final boolean DEBUG = false;
    private static final String GSI_RUNNING_PROP = "ro.gsid.image_running";
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
    private final IActivityManager mActivityManager;
    protected IAuthSecret mAuthSecretService;
    private final BroadcastReceiver mBroadcastReceiver;
    private final Context mContext;
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
    @GuardedBy({"mSpManager"})
    private SparseArray<SyntheticPasswordManager.AuthenticationToken> mSpCache;
    protected final SyntheticPasswordManager mSpManager;
    @VisibleForTesting
    protected final LockSettingsStorage mStorage;
    private final IStorageManager mStorageManager;
    protected final LockSettingsStrongAuth mStrongAuth;
    private final SynchronizedStrongAuthTracker mStrongAuthTracker;
    protected final UserManager mUserManager;

    public static final class Lifecycle extends SystemService {
        private LockSettingsService mLockSettingsService;

        public Lifecycle(Context context) {
            super(context);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.android.server.locksettings.LockSettingsService$Lifecycle */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r1v0, types: [com.android.server.locksettings.LockSettingsService, android.os.IBinder] */
        /* JADX WARNING: Unknown variable types count: 1 */
        @Override // com.android.server.SystemService
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

        @Override // com.android.server.SystemService
        public void onBootPhase(int phase) {
            super.onBootPhase(phase);
            if (phase == 550) {
                this.mLockSettingsService.migrateOldDataAfterSystemReady();
            }
            if (phase == 1000) {
                this.mLockSettingsService.migrateOldDataAfterSystemServerReady();
            }
            if (phase == 550) {
                this.mLockSettingsService.showEncryptionNotificationForUsers();
            }
            if (phase == 500) {
                this.mLockSettingsService.updateRemainLockedTimeAfterReboot();
            }
        }

        @Override // com.android.server.SystemService
        public void onStartUser(int userHandle) {
            this.mLockSettingsService.onStartUser(userHandle);
        }

        @Override // com.android.server.SystemService
        public void onUnlockUser(int userHandle) {
            this.mLockSettingsService.onUnlockUser(userHandle);
        }

        @Override // com.android.server.SystemService
        public void onCleanupUser(int userHandle) {
            this.mLockSettingsService.onCleanupUser(userHandle);
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public static class SynchronizedStrongAuthTracker extends LockPatternUtils.StrongAuthTracker {
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

    public void tieManagedProfileLockIfNecessary(int managedUserId, byte[] managedUserPassword) {
        Exception e;
        if (this.mUserManager.getUserInfo(managedUserId).isManagedProfile() && !this.mLockPatternUtils.isSeparateProfileChallengeEnabled(managedUserId) && !this.mStorage.hasChildProfileLock(managedUserId)) {
            int parentId = this.mUserManager.getProfileParent(managedUserId).id;
            if (isUserSecure(parentId)) {
                try {
                    if (getGateKeeperService().getSecureUserId(parentId) != 0) {
                        byte[] bArr = new byte[0];
                        try {
                            try {
                                char[] newPasswordChars = HexEncoding.encode(SecureRandom.getInstance("SHA1PRNG").generateSeed(40));
                                byte[] newPassword = new byte[newPasswordChars.length];
                                for (int i = 0; i < newPasswordChars.length; i++) {
                                    newPassword[i] = (byte) newPasswordChars[i];
                                }
                                Arrays.fill(newPasswordChars, (char) 0);
                                setLockCredentialInternal(newPassword, 2, managedUserPassword, 327680, managedUserId, false, true);
                                setLong("lockscreen.password_type", 327680, managedUserId);
                                tieProfileLockToParent(managedUserId, newPassword);
                                Arrays.fill(newPassword, (byte) 0);
                            } catch (RemoteException | NoSuchAlgorithmException e2) {
                                e = e2;
                                Slog.e(TAG, "Fail to tie managed profile", e);
                            }
                        } catch (RemoteException | NoSuchAlgorithmException e3) {
                            e = e3;
                            Slog.e(TAG, "Fail to tie managed profile", e);
                        }
                    }
                } catch (RemoteException e4) {
                    Slog.e(TAG, "Failed to talk to GateKeeper service", e4);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class Injector {
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
                /* class com.android.server.locksettings.LockSettingsService.Injector.AnonymousClass1 */

                @Override // com.android.server.locksettings.LockSettingsStorage.Callback
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
            return new SyntheticPasswordManager(getContext(), storage, getUserManager(), new PasswordSlotManager());
        }

        public boolean hasEnrolledBiometrics() {
            return ((BiometricManager) this.mContext.getSystemService(BiometricManager.class)).canAuthenticate() == 0;
        }

        public int binderGetCallingUid() {
            return Binder.getCallingUid();
        }

        public boolean isGsiRunning() {
            return SystemProperties.getInt(LockSettingsService.GSI_RUNNING_PROP, 0) > 0;
        }
    }

    public LockSettingsService(Context context) {
        this(new Injector(context));
    }

    @VisibleForTesting
    protected LockSettingsService(Injector injector) {
        this.mSeparateChallengeLock = new Object();
        this.mDeviceProvisionedObserver = new DeviceProvisionedObserver();
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* class com.android.server.locksettings.LockSettingsService.AnonymousClass2 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                int userHandle;
                if ("android.intent.action.USER_ADDED".equals(intent.getAction())) {
                    int userHandle2 = intent.getIntExtra("android.intent.extra.user_handle", 0);
                    if (userHandle2 > 0) {
                        LockSettingsService.this.removeUser(userHandle2, true);
                    }
                    KeyStore ks = KeyStore.getInstance();
                    UserInfo parentInfo = LockSettingsService.this.mUserManager.getProfileParent(userHandle2);
                    ks.onUserAdded(userHandle2, parentInfo != null ? parentInfo.id : -1);
                    LockSettingsService.this.onUserAdded(userHandle2);
                } else if ("android.intent.action.USER_STARTING".equals(intent.getAction())) {
                    LockSettingsService.this.mStorage.prefetchUser(intent.getIntExtra("android.intent.extra.user_handle", 0));
                } else if ("android.intent.action.USER_REMOVED".equals(intent.getAction()) && (userHandle = intent.getIntExtra("android.intent.extra.user_handle", 0)) > 0) {
                    LockSettingsService.this.removeUser(userHandle, false);
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
        this.mStorageManager = injector.getStorageManager();
        this.mStrongAuthTracker = injector.getStrongAuthTracker();
        this.mStrongAuthTracker.register(this.mStrongAuth);
        this.mSpManager = injector.getSyntheticPasswordManager(this.mStorage);
        LocalServices.addService(LockSettingsInternal.class, new LocalService());
    }

    private void maybeShowEncryptionNotificationForUser(int userId) {
        UserInfo parent;
        UserInfo user = this.mUserManager.getUserInfo(userId);
        if (user.isManagedProfile() && !isUserKeyUnlocked(userId)) {
            UserHandle userHandle = user.getUserHandle();
            if (isUserSecure(userId) && !this.mUserManager.isUserUnlockingOrUnlocked(userHandle) && (parent = this.mUserManager.getProfileParent(userId)) != null && this.mUserManager.isUserUnlockingOrUnlocked(parent.getUserHandle()) && !this.mUserManager.isQuietModeEnabled(userHandle)) {
                showEncryptionNotificationForProfile(userHandle);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showEncryptionNotificationForProfile(UserHandle user) {
        Resources r = this.mContext.getResources();
        CharSequence title = r.getText(17041084);
        CharSequence message = r.getText(17041083);
        CharSequence detail = r.getText(17041082);
        Intent unlockIntent = ((KeyguardManager) this.mContext.getSystemService("keyguard")).createConfirmDeviceCredentialIntent(null, null, user.getIdentifier());
        if (unlockIntent != null) {
            unlockIntent.setFlags(276824064);
            showEncryptionNotification(user, title, message, detail, PendingIntent.getActivity(this.mContext, 0, unlockIntent, DumpState.DUMP_HWFEATURES));
        }
    }

    /* access modifiers changed from: protected */
    public void showEncryptionNotification(UserHandle user, CharSequence title, CharSequence message, CharSequence detail, PendingIntent intent) {
        if (StorageManager.isFileEncryptedNativeOrEmulated()) {
            this.mNotificationManager.notifyAsUser(null, 9, new Notification.Builder(this.mContext, SystemNotificationChannels.DEVICE_ADMIN).setSmallIcon(17302845).setWhen(0).setOngoing(true).setTicker(title).setColor(this.mContext.getColor(17170460)).setContentTitle(title).setContentText(message).setSubText(detail).setVisibility(1).setContentIntent(intent).build(), user);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hideEncryptionNotification(UserHandle userHandle) {
        this.mNotificationManager.cancelAsUser(null, 9, userHandle);
    }

    public void onCleanupUser(int userId) {
        hideEncryptionNotification(new UserHandle(userId));
        requireStrongAuth(LockPatternUtils.StrongAuthTracker.getDefaultFlags(this.mContext), userId);
    }

    public void onStartUser(int userId) {
        maybeShowEncryptionNotificationForUser(userId);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void ensureProfileKeystoreUnlocked(int userId) {
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
            /* class com.android.server.locksettings.LockSettingsService.AnonymousClass1 */

            @Override // java.lang.Runnable
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
    /* access modifiers changed from: public */
    private void tryDeriveAuthTokenForUnsecuredPrimaryUser(int userId) {
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
            EventLog.writeEvent(1397638484, "28251513", Integer.valueOf(getCallingUid()), "");
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
    }

    private void migrateOldData() {
        int i;
        int i2;
        int i3 = 0;
        if (getString("migrated", null, 0) == null) {
            ContentResolver cr = this.mContext.getContentResolver();
            String[] strArr = VALID_SETTINGS;
            for (String validSetting : strArr) {
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
            while (user < users.size()) {
                int userId = users.get(user).id;
                String ownerInfo = Settings.Secure.getStringForUser(cr2, "lock_screen_owner_info", userId);
                if (!TextUtils.isEmpty(ownerInfo)) {
                    setString("lock_screen_owner_info", ownerInfo, userId);
                    Settings.Secure.putStringForUser(cr2, "lock_screen_owner_info", "", userId);
                }
                try {
                    setLong("lock_screen_owner_info_enabled", (Settings.Secure.getIntForUser(cr2, "lock_screen_owner_info_enabled", userId) != 0 ? 1 : i3) != 0 ? 1 : 0, userId);
                } catch (Settings.SettingNotFoundException e) {
                    if (!TextUtils.isEmpty(ownerInfo)) {
                        setLong("lock_screen_owner_info_enabled", 1, userId);
                    }
                }
                Settings.Secure.putIntForUser(cr2, "lock_screen_owner_info_enabled", 0, userId);
                user++;
                i3 = 0;
            }
            i = i3;
            setString("migrated_user_specific", "true", i);
            Slog.i(TAG, "Migrated per-user lock settings to new location");
        } else {
            i = 0;
        }
        if (getString("migrated_biometric_weak", null, i) == null) {
            List<UserInfo> users2 = this.mUserManager.getUsers();
            for (int i4 = 0; i4 < users2.size(); i4++) {
                int userId2 = users2.get(i4).id;
                long type = getLong("lockscreen.password_type", 0, userId2);
                long alternateType = getLong("lockscreen.password_type_alternate", 0, userId2);
                if (type == 32768) {
                    setLong("lockscreen.password_type", alternateType, userId2);
                }
                setLong("lockscreen.password_type_alternate", 0, userId2);
            }
            i2 = 0;
            setString("migrated_biometric_weak", "true", 0);
            Slog.i(TAG, "Migrated biometric weak to use the fallback instead");
        } else {
            i2 = 0;
        }
        if (getString("migrated_lockscreen_disabled", null, i2) == null) {
            List<UserInfo> users3 = this.mUserManager.getUsers();
            int userCount = users3.size();
            int switchableUsers = 0;
            for (int i5 = 0; i5 < userCount; i5++) {
                if (users3.get(i5).supportsSwitchTo()) {
                    switchableUsers++;
                }
            }
            if (switchableUsers > 1) {
                for (int i6 = 0; i6 < userCount; i6++) {
                    int id = users3.get(i6).id;
                    if (getBoolean("lockscreen.disabled", false, id)) {
                        setBoolean("lockscreen.disabled", false, id);
                    }
                }
            }
            setString("migrated_lockscreen_disabled", "true", 0);
            Slog.i(TAG, "Migrated lockscreen disabled flag");
        }
        List<UserInfo> users4 = this.mUserManager.getUsers();
        for (int i7 = 0; i7 < users4.size(); i7++) {
            UserInfo userInfo = users4.get(i7);
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
        }
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.watch") && getString("migrated_wear_lockscreen_disabled", null, 0) == null) {
            int userCount2 = users4.size();
            for (int i8 = 0; i8 < userCount2; i8++) {
                setBoolean("lockscreen.disabled", false, users4.get(i8).id);
            }
            setString("migrated_wear_lockscreen_disabled", "true", 0);
            Slog.i(TAG, "Migrated lockscreen_disabled for Wear devices");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void migrateOldDataAfterSystemReady() {
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void migrateOldDataAfterSystemServerReady() {
        try {
            if (LockPatternUtils.frpCredentialEnabled(this.mContext) && !getBoolean("migrated_frp_hw", false, 0)) {
                migrateFrpCredential();
                setBoolean("migrated_frp_hw", true, 0);
                Slog.i(TAG, "Migrated migrated_frp_hw.");
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "Unable to migrateOldDataAfterSystemServerReady", e);
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
            return DumpState.DUMP_INTENT_FILTER_VERIFIERS;
        }
        return (quality == 262144 || quality == 327680 || quality == 393216) ? DumpState.DUMP_DOMAIN_PREFERRED : quality;
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
            EventLog.writeEvent(1397638484, "28251513", Integer.valueOf(getCallingUid()), "");
        }
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, "LockSettingsHave");
    }

    private final void checkReadPermission(String requestedKey, int userId) {
        int callingUid = Binder.getCallingUid();
        int i = 0;
        while (true) {
            String[] strArr = READ_CONTACTS_PROTECTED_SETTINGS;
            if (i >= strArr.length) {
                int i2 = 0;
                while (true) {
                    String[] strArr2 = READ_PASSWORD_PROTECTED_SETTINGS;
                    if (i2 >= strArr2.length) {
                        return;
                    }
                    if (!strArr2[i2].equals(requestedKey) || this.mContext.checkCallingOrSelfPermission(PERMISSION) == 0) {
                        i2++;
                    } else {
                        throw new SecurityException("uid=" + callingUid + " needs permission " + PERMISSION + " to read " + requestedKey + " for user " + userId);
                    }
                }
            } else if (!strArr[i].equals(requestedKey) || this.mContext.checkCallingOrSelfPermission("android.permission.READ_CONTACTS") == 0) {
                i++;
            } else {
                throw new SecurityException("uid=" + callingUid + " needs permission android.permission.READ_CONTACTS to read " + requestedKey + " for user " + userId);
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

    public void setSeparateProfileChallengeEnabled(int userId, boolean enabled, byte[] managedUserPassword) {
        checkWritePermission(userId);
        if (this.mLockPatternUtils.hasSecureLockScreen()) {
            synchronized (this.mSeparateChallengeLock) {
                setSeparateProfileChallengeEnabledLocked(userId, enabled, managedUserPassword);
            }
            notifySeparateProfileChallengeChanged(userId);
            return;
        }
        throw new UnsupportedOperationException("This operation requires secure lock screen feature.");
    }

    @GuardedBy({"mSeparateChallengeLock"})
    private void setSeparateProfileChallengeEnabledLocked(int userId, boolean enabled, byte[] managedUserPassword) {
        boolean old = getBoolean(SEPARATE_PROFILE_CHALLENGE_KEY, false, userId);
        setBoolean(SEPARATE_PROFILE_CHALLENGE_KEY, enabled, userId);
        if (enabled) {
            try {
                this.mStorage.removeChildProfileLock(userId);
                removeKeystoreProfileKey(userId);
            } catch (IllegalStateException e) {
                setBoolean(SEPARATE_PROFILE_CHALLENGE_KEY, old, userId);
                throw e;
            }
        } else {
            tieManagedProfileLockIfNecessary(userId, managedUserPassword);
        }
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

    private void setStringUnchecked(String key, int userId, String value) {
        Preconditions.checkArgument(userId != -9999, "cannot store lock settings for FRP user");
        this.mStorage.writeKeyValue(key, value, userId);
        if (ArrayUtils.contains(SETTINGS_TO_BACKUP, key)) {
            BackupManager.dataChanged(UserBackupManagerService.SETTINGS_PACKAGE);
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

    public boolean havePassword(int userId) {
        checkPasswordHavePermission(userId);
        synchronized (this.mSpManager) {
            if (!isSyntheticPasswordBasedCredentialLocked(userId)) {
                return this.mStorage.hasPassword(userId);
            }
            return this.mSpManager.getCredentialType(getSyntheticPasswordHandleLocked(userId), userId) == 2;
        }
    }

    public boolean havePattern(int userId) {
        checkPasswordHavePermission(userId);
        synchronized (this.mSpManager) {
            if (!isSyntheticPasswordBasedCredentialLocked(userId)) {
                return this.mStorage.hasPattern(userId);
            }
            boolean z = true;
            if (this.mSpManager.getCredentialType(getSyntheticPasswordHandleLocked(userId), userId) != 1) {
                z = false;
            }
            return z;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isUserSecure(int userId) {
        synchronized (this.mSpManager) {
            if (!isSyntheticPasswordBasedCredentialLocked(userId)) {
                return this.mStorage.hasCredential(userId);
            }
            return this.mSpManager.getCredentialType(getSyntheticPasswordHandleLocked(userId), userId) != -1;
        }
    }

    /* access modifiers changed from: protected */
    public void setKeystorePassword(byte[] password, int userHandle) {
        KeyStore.getInstance().onUserPasswordChanged(userHandle, password == null ? null : new String(password));
    }

    /* access modifiers changed from: protected */
    public void unlockKeystore(byte[] password, int userHandle) {
        KeyStore.getInstance().unlock(userHandle, password == null ? null : new String(password));
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public byte[] getDecryptedPasswordForTiedProfile(int userId) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, CertificateException, IOException {
        byte[] storedData = this.mStorage.readChildProfileLock(userId);
        if (storedData != null) {
            byte[] iv = Arrays.copyOfRange(storedData, 0, 12);
            byte[] encryptedPassword = Arrays.copyOfRange(storedData, 12, storedData.length);
            java.security.KeyStore keyStore = java.security.KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(2, (SecretKey) keyStore.getKey("profile_key_name_decrypt_" + userId, null), new GCMParameterSpec(128, iv));
            return cipher.doFinal(encryptedPassword);
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
                /* class com.android.server.locksettings.LockSettingsService.AnonymousClass3 */

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
                if (!(ui == null || ui.isManagedProfile() || ui.isClonedProfile())) {
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

    private Map<Integer, byte[]> getDecryptedPasswordsForAllTiedProfiles(int userId) {
        if (this.mUserManager.getUserInfo(userId).isManagedProfile()) {
            return null;
        }
        Map<Integer, byte[]> result = new ArrayMap<>();
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

    private void synchronizeUnifiedWorkChallengeForProfiles(int userId, Map<Integer, byte[]> profilePasswordMap) throws RemoteException {
        int managedUserId;
        if (this.mUserManager.getUserInfo(userId) == null || !this.mUserManager.getUserInfo(userId).isManagedProfile()) {
            boolean isSecure = isUserSecure(userId);
            List<UserInfo> profiles = this.mUserManager.getProfiles(userId);
            int size = profiles.size();
            for (int i = 0; i < size; i++) {
                UserInfo profile = profiles.get(i);
                if (profile.isManagedProfile()) {
                    int managedUserId2 = profile.id;
                    if (!this.mLockPatternUtils.isSeparateProfileChallengeEnabled(managedUserId2)) {
                        if (isSecure) {
                            tieManagedProfileLockIfNecessary(managedUserId2, null);
                        } else {
                            if (profilePasswordMap == null || !profilePasswordMap.containsKey(Integer.valueOf(managedUserId2))) {
                                managedUserId = managedUserId2;
                                Slog.wtf(TAG, "clear tied profile challenges, but no password supplied.");
                                setLockCredentialInternal(null, -1, null, 0, managedUserId, true, true);
                            } else {
                                managedUserId = managedUserId2;
                                setLockCredentialInternal(null, -1, profilePasswordMap.get(Integer.valueOf(managedUserId2)), 0, managedUserId2, false, true);
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

    private void sendCredentialsOnUnlockIfRequired(int credentialType, byte[] credential, int userId) {
        if (userId != -9999 && !isManagedProfileWithUnifiedLock(userId)) {
            for (Integer num : getProfilesWithSameLockScreen(userId)) {
                this.mRecoverableKeyStoreManager.lockScreenSecretAvailable(credentialType, credential, num.intValue());
            }
        }
    }

    private void sendCredentialsOnChangeIfRequired(int credentialType, byte[] credential, int userId, boolean isLockTiedToParent) {
        if (!isLockTiedToParent) {
            for (Integer num : getProfilesWithSameLockScreen(userId)) {
                this.mRecoverableKeyStoreManager.lockScreenSecretChanged(credentialType, credential, num.intValue());
            }
        }
    }

    private Set<Integer> getProfilesWithSameLockScreen(int userId) {
        Set<Integer> profiles = new ArraySet<>();
        for (UserInfo profile : this.mUserManager.getProfiles(userId)) {
            if (profile.id == userId || (profile.profileGroupId == userId && isManagedProfileWithUnifiedLock(profile.id))) {
                profiles.add(Integer.valueOf(profile.id));
            }
        }
        return profiles;
    }

    public void setLockCredential(byte[] credential, int type, byte[] savedCredential, int requestedQuality, int userId, boolean allowUntrustedChange) throws RemoteException {
        if (this.mLockPatternUtils.hasSecureLockScreen()) {
            checkWritePermission(userId);
            synchronized (this.mSeparateChallengeLock) {
                setLockCredentialInternal(credential, type, savedCredential, requestedQuality, userId, allowUntrustedChange, false);
                setSeparateProfileChallengeEnabledLocked(userId, true, null);
                notifyPasswordChanged(userId);
            }
            if (this.mUserManager.getUserInfo(userId).isManagedProfile()) {
                setDeviceUnlockedForUser(userId);
            }
            notifySeparateProfileChallengeChanged(userId);
            return;
        }
        warnLog(TAG, "did not had secure lock screen, error.");
        throw new UnsupportedOperationException("This operation requires secure lock screen feature");
    }

    /* access modifiers changed from: protected */
    public void setLockCredentialInternal(byte[] credential, int credentialType, byte[] savedCredential, int requestedQuality, int userId, boolean allowUntrustedChange, boolean isLockTiedToParent) throws RemoteException {
        byte[] savedCredential2;
        byte[] credential2;
        SyntheticPasswordManager syntheticPasswordManager;
        Throwable th;
        if (savedCredential == null || savedCredential.length == 0) {
            savedCredential2 = null;
        } else {
            savedCredential2 = savedCredential;
        }
        if (credential == null || credential.length == 0) {
            credential2 = null;
        } else {
            credential2 = credential;
        }
        synchronized (this.mSpManager) {
            if (isSyntheticPasswordBasedCredentialLocked(userId)) {
                spBasedSetLockCredentialInternalLocked(credential2, credentialType, savedCredential2, requestedQuality, userId, allowUntrustedChange, isLockTiedToParent);
                return;
            }
        }
        if (credentialType == -1) {
            if (credential2 != null) {
                Slog.wtf(TAG, "CredentialType is none, but credential is non-null.");
            }
            clearUserKeyProtection(userId);
            getGateKeeperService().clearSecureUserId(userId);
            this.mStorage.writeCredentialHash(LockSettingsStorage.CredentialHash.createEmptyHash(), userId);
            setKeystorePassword(null, userId);
            fixateNewestUserKeyAuth(userId);
            synchronizeUnifiedWorkChallengeForProfiles(userId, null);
            notifyActivePasswordMetricsAvailable(-1, null, userId);
            sendCredentialsOnChangeIfRequired(credentialType, credential2, userId, isLockTiedToParent);
            Slog.w(TAG, "clear password finish");
        } else if (credential2 != null) {
            LockSettingsStorage.CredentialHash currentHandle = this.mStorage.readCredentialHash(userId);
            if (isManagedProfileWithUnifiedLock(userId)) {
                if (savedCredential2 == null) {
                    try {
                        savedCredential2 = getDecryptedPasswordForTiedProfile(userId);
                    } catch (FileNotFoundException e) {
                        Slog.i(TAG, "Child profile key not found");
                    } catch (IOException | InvalidAlgorithmParameterException | InvalidKeyException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e2) {
                        Slog.e(TAG, "Failed to decrypt child profile key", e2);
                    }
                }
            } else if (currentHandle.hash == null) {
                if (savedCredential2 != null) {
                    Slog.w(TAG, "Saved credential provided, but none stored");
                }
                savedCredential2 = null;
            }
            SyntheticPasswordManager syntheticPasswordManager2 = this.mSpManager;
            synchronized (syntheticPasswordManager2) {
                try {
                    if (shouldMigrateToSyntheticPasswordLocked(userId)) {
                        try {
                            initializeSyntheticPasswordLocked(currentHandle.hash, savedCredential2, currentHandle.type, requestedQuality, userId);
                            syntheticPasswordManager = syntheticPasswordManager2;
                        } catch (Throwable th2) {
                            th = th2;
                            syntheticPasswordManager = syntheticPasswordManager2;
                            while (true) {
                                try {
                                    break;
                                } catch (Throwable th3) {
                                    th = th3;
                                }
                            }
                            throw th;
                        }
                        try {
                            spBasedSetLockCredentialInternalLocked(credential2, credentialType, savedCredential2, requestedQuality, userId, allowUntrustedChange, isLockTiedToParent);
                        } catch (Throwable th4) {
                            th = th4;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    } else {
                        syntheticPasswordManager = syntheticPasswordManager2;
                        try {
                            byte[] enrolledHandle = enrollCredential(currentHandle.hash, savedCredential2, credential2, userId);
                            if (enrolledHandle != null) {
                                LockSettingsStorage.CredentialHash willStore = LockSettingsStorage.CredentialHash.create(enrolledHandle, credentialType);
                                this.mStorage.writeCredentialHash(willStore, userId);
                                setUserKeyProtection(userId, credential2, convertResponse(getGateKeeperService().verifyChallenge(userId, 0, willStore.hash, credential2)));
                                fixateNewestUserKeyAuth(userId);
                                doVerifyCredential(credential2, credentialType, true, 0, userId, null);
                                synchronizeUnifiedWorkChallengeForProfiles(userId, null);
                                sendCredentialsOnChangeIfRequired(credentialType, credential2, userId, isLockTiedToParent);
                                return;
                            }
                            Slog.e(TAG, "Failed to enroll password");
                            notifyBigDataForPwdProtectFail(userId);
                            StringBuilder sb = new StringBuilder();
                            sb.append("Failed to enroll ");
                            sb.append(credentialType == 2 ? "password" : "pattern");
                            throw new RemoteException(sb.toString());
                        } catch (Throwable th5) {
                            th = th5;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    }
                } catch (Throwable th6) {
                    th = th6;
                    syntheticPasswordManager = syntheticPasswordManager2;
                    while (true) {
                        break;
                    }
                    throw th;
                }
            }
        } else {
            throw new RemoteException("Null credential with mismatched credential type");
        }
    }

    private VerifyCredentialResponse convertResponse(GateKeeperResponse gateKeeperResponse) {
        return VerifyCredentialResponse.fromGateKeeperResponse(gateKeeperResponse);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0121, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0129, code lost:
        throw new java.lang.RuntimeException("Failed to encrypt key", r0);
     */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0121 A[ExcHandler: IOException | InvalidKeyException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException (r0v13 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:4:0x00bd] */
    @VisibleForTesting
    public void tieProfileLockToParent(int userId, byte[] password) {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(new SecureRandom());
        SecretKey secretKey = keyGenerator.generateKey();
        java.security.KeyStore keyStore = java.security.KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        try {
            keyStore.setEntry("profile_key_name_encrypt_" + userId, new KeyStore.SecretKeyEntry(secretKey), new KeyProtection.Builder(1).setBlockModes("GCM").setEncryptionPaddings("NoPadding").build());
            keyStore.setEntry("profile_key_name_decrypt_" + userId, new KeyStore.SecretKeyEntry(secretKey), new KeyProtection.Builder(2).setBlockModes("GCM").setEncryptionPaddings("NoPadding").setUserAuthenticationRequired(true).setUserAuthenticationValidityDurationSeconds(30).setCriticalToDeviceEncryption(true).build());
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(1, (SecretKey) keyStore.getKey("profile_key_name_encrypt_" + userId, null));
            byte[] encryptionResult = cipher.doFinal(password);
            byte[] iv = cipher.getIV();
            try {
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
            }
        } finally {
            keyStore.deleteEntry("profile_key_name_encrypt_" + userId);
        }
    }

    private byte[] enrollCredential(byte[] enrolledHandle, byte[] enrolledCredential, byte[] toEnroll, int userId) throws RemoteException {
        checkWritePermission(userId);
        GateKeeperResponse response = getGateKeeperService().enroll(userId, enrolledHandle, enrolledCredential, toEnroll);
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

    private void setUserKeyProtection(int userId, byte[] credential, VerifyCredentialResponse vcr) throws RemoteException {
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

    private static byte[] secretFromCredential(byte[] credential) throws RemoteException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.update(Arrays.copyOf("Android FBE credential hash".getBytes(), 128));
            digest.update(credential);
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException for SHA-512");
        }
    }

    private boolean isUserKeyUnlocked(int userId) {
        try {
            return this.mStorageManager.isUserKeyUnlocked(userId);
        } catch (RemoteException e) {
            Log.e(TAG, "failed to check user key locked state", e);
            return false;
        }
    }

    private void unlockUserKey(int userId, byte[] token, byte[] secret) throws RemoteException {
        this.mStorageManager.unlockUserKey(userId, this.mUserManager.getUserInfo(userId).serialNumber, token, secret);
    }

    /* access modifiers changed from: protected */
    public void addUserKeyAuth(int userId, byte[] token, byte[] secret) throws RemoteException {
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
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mStorageManager.addUserKeyAuth(userId, userInfo.serialNumber, token, secret);
            addSDCardUserKeyAuth(userId, userInfo, token, secret);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    /* access modifiers changed from: protected */
    public void fixateNewestUserKeyAuth(int userId) throws RemoteException {
        warnLog(TAG, "fixateNewestUserKeyAuth: U=" + userId);
        long callingId = Binder.clearCallingIdentity();
        try {
            this.mStorageManager.fixateNewestUserKeyAuth(userId);
            flog(TAG, "fixateNewestUserKeyAuth finish: U=" + userId);
        } finally {
            Binder.restoreCallingIdentity(callingId);
        }
    }

    public void resetKeyStore(int userId) throws RemoteException {
        checkWritePermission(userId);
        byte[] managedUserDecryptedPassword = null;
        int managedUserId = -1;
        for (UserInfo pi : this.mUserManager.getProfiles(userId)) {
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
            int[] profileIdsWithDisabled = this.mUserManager.getProfileIdsWithDisabled(userId);
            for (int profileId : profileIdsWithDisabled) {
                for (int uid : SYSTEM_CREDENTIAL_UIDS) {
                    this.mKeyStore.clearUid(UserHandle.getUid(profileId, uid));
                }
            }
            if (managedUserDecryptedPassword != null && managedUserDecryptedPassword.length > 0) {
                Arrays.fill(managedUserDecryptedPassword, (byte) 0);
            }
        } finally {
            if (!(managedUserId == -1 || managedUserDecryptedPassword == null)) {
                tieProfileLockToParent(managedUserId, managedUserDecryptedPassword);
            }
        }
    }

    public VerifyCredentialResponse checkCredential(byte[] credential, int type, int userId, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        checkPasswordReadPermission(userId);
        return doVerifyCredential(credential, type, false, 0, userId, progressCallback);
    }

    public VerifyCredentialResponse verifyCredential(byte[] credential, int type, long challenge, int userId) throws RemoteException {
        checkPasswordReadPermission(userId);
        return doVerifyCredential(credential, type, true, challenge, userId, null);
    }

    private VerifyCredentialResponse doVerifyCredential(byte[] credential, int credentialType, boolean hasChallenge, long challenge, int userId, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        byte[] credentialToVerify;
        if (credential == null || credential.length == 0) {
            throw new IllegalArgumentException("Credential can't be null or empty");
        }
        boolean shouldReEnrollBaseZero = false;
        if (userId != -9999 || Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) == 0) {
            VerifyCredentialResponse response = spBasedDoVerifyCredential(credential, credentialType, hasChallenge, challenge, userId, progressCallback);
            if (response != null) {
                if (response.getResponseCode() == 0) {
                    sendCredentialsOnUnlockIfRequired(credentialType, credential, userId);
                }
                return response;
            } else if (userId == -9999) {
                Slog.wtf(TAG, "Unexpected FRP credential type, should be SP based.");
                return VerifyCredentialResponse.ERROR;
            } else {
                LockSettingsStorage.CredentialHash storedHash = this.mStorage.readCredentialHash(userId);
                if (storedHash.type != credentialType) {
                    Slog.wtf(TAG, "doVerifyCredential type mismatch with stored credential?? stored: " + storedHash.type + " passed in: " + credentialType);
                    return VerifyCredentialResponse.ERROR;
                }
                if (storedHash.type == 1 && storedHash.isBaseZeroPattern) {
                    shouldReEnrollBaseZero = true;
                }
                if (shouldReEnrollBaseZero) {
                    credentialToVerify = LockPatternUtils.patternByteArrayToBaseZero(credential);
                } else {
                    credentialToVerify = credential;
                }
                VerifyCredentialResponse response2 = verifyCredential(userId, storedHash, credentialToVerify, hasChallenge, challenge, progressCallback);
                if (response2.getResponseCode() == 0) {
                    this.mStrongAuth.reportSuccessfulStrongAuthUnlock(userId);
                    if (shouldReEnrollBaseZero) {
                        setLockCredentialInternal(credential, storedHash.type, credentialToVerify, 65536, userId, false, false);
                    }
                }
                return response2;
            }
        } else {
            Slog.e(TAG, "FRP credential can only be verified prior to provisioning.");
            return VerifyCredentialResponse.ERROR;
        }
    }

    public VerifyCredentialResponse verifyTiedProfileChallenge(byte[] credential, int type, long challenge, int userId) throws RemoteException {
        checkPasswordReadPermission(userId);
        if (isManagedProfileWithUnifiedLock(userId)) {
            VerifyCredentialResponse parentResponse = doVerifyCredential(credential, type, true, challenge, this.mUserManager.getProfileParent(userId).id, null);
            if (parentResponse.getResponseCode() != 0) {
                return parentResponse;
            }
            try {
                return doVerifyCredential(getDecryptedPasswordForTiedProfile(userId), 2, true, challenge, userId, null);
            } catch (IOException | InvalidAlgorithmParameterException | InvalidKeyException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
                Slog.e(TAG, "Failed to decrypt child profile key", e);
                throw new RemoteException("Unable to get tied profile token");
            }
        } else {
            throw new RemoteException("User id must be managed profile with unified lock");
        }
    }

    private VerifyCredentialResponse verifyCredential(int userId, LockSettingsStorage.CredentialHash storedHash, byte[] credential, boolean hasChallenge, long challenge, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        int reEnrollQuality;
        byte[] hash;
        int i;
        if ((storedHash == null || storedHash.hash.length == 0) && (credential == null || credential.length == 0)) {
            Slog.w(TAG, "no stored Password/Pattern, verifyCredential success");
            return VerifyCredentialResponse.OK;
        } else if (storedHash == null || credential == null || credential.length == 0) {
            Slog.w(TAG, "no entered Password/Pattern, verifyCredential ERROR");
            return VerifyCredentialResponse.ERROR;
        } else {
            StrictMode.noteDiskRead();
            if (storedHash.version == 0) {
                if (storedHash.type == 1) {
                    hash = LockPatternUtils.patternToHash(LockPatternUtils.byteArrayToPattern(credential));
                } else {
                    hash = this.mLockPatternUtils.legacyPasswordToHash(credential, userId).getBytes();
                }
                if (!Arrays.equals(hash, storedHash.hash)) {
                    return VerifyCredentialResponse.ERROR;
                }
                if (storedHash.type == 1) {
                    unlockKeystore(LockPatternUtils.patternByteArrayToBaseZero(credential), userId);
                } else {
                    unlockKeystore(credential, userId);
                }
                Slog.i(TAG, "Unlocking user with fake token: " + userId);
                byte[] fakeToken = String.valueOf(userId).getBytes();
                unlockUser(userId, fakeToken, fakeToken);
                int i2 = storedHash.type;
                if (storedHash.type == 1) {
                    i = 65536;
                } else {
                    i = 327680;
                }
                setLockCredentialInternal(credential, i2, null, i, userId, false, false);
                if (!hasChallenge) {
                    notifyActivePasswordMetricsAvailable(storedHash.type, credential, userId);
                    sendCredentialsOnUnlockIfRequired(storedHash.type, credential, userId);
                    return VerifyCredentialResponse.OK;
                }
            }
            try {
                if (getGateKeeperService() == null) {
                    this.mLockPatternUtils.monitorCheckPassword(1006, (Exception) null);
                    return VerifyCredentialResponse.ERROR;
                }
                GateKeeperResponse gateKeeperResponse = getGateKeeperService().verifyChallenge(userId, challenge, storedHash.hash, credential);
                VerifyCredentialResponse response = convertResponse(gateKeeperResponse);
                boolean shouldReEnroll = gateKeeperResponse.getShouldReEnroll();
                if (response.getResponseCode() == 0) {
                    if (progressCallback != null) {
                        progressCallback.onCredentialVerified();
                    }
                    notifyActivePasswordMetricsAvailable(storedHash.type, credential, userId);
                    unlockKeystore(credential, userId);
                    Slog.i(TAG, "Unlocking user " + userId + " with token length " + response.getPayload().length);
                    unlockUser(userId, response.getPayload(), secretFromCredential(credential));
                    if (isManagedProfileWithSeparatedLock(userId)) {
                        setDeviceUnlockedForUser(userId);
                    }
                    if (storedHash.type == 1) {
                        reEnrollQuality = 65536;
                    } else {
                        reEnrollQuality = 327680;
                    }
                    if (shouldReEnroll) {
                        setLockCredentialInternal(credential, storedHash.type, credential, reEnrollQuality, userId, false, false);
                    } else {
                        synchronized (this.mSpManager) {
                            if (shouldMigrateToSyntheticPasswordLocked(userId)) {
                                activateEscrowTokens(initializeSyntheticPasswordLocked(storedHash.hash, credential, storedHash.type, reEnrollQuality, userId), userId);
                            }
                        }
                    }
                    sendCredentialsOnUnlockIfRequired(storedHash.type, credential, userId);
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

    private void notifyActivePasswordMetricsAvailable(int credentialType, byte[] password, int userId) {
        this.mHandler.post(new Runnable(PasswordMetrics.computeForCredential(credentialType, password), userId) {
            /* class com.android.server.locksettings.$$Lambda$LockSettingsService$Hh44Kcp05cKI6Hc6dJfQupn4QY8 */
            private final /* synthetic */ PasswordMetrics f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                LockSettingsService.this.lambda$notifyActivePasswordMetricsAvailable$0$LockSettingsService(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$notifyActivePasswordMetricsAvailable$0$LockSettingsService(PasswordMetrics metrics, int userId) {
        DevicePolicyManager dpm = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
        if (dpm != null) {
            dpm.setActivePasswordState(metrics, userId);
        } else {
            Log.e(TAG, "can not get DevicePolicyManager");
        }
    }

    private void notifyPasswordChanged(int userId) {
        this.mHandler.post(new Runnable(userId) {
            /* class com.android.server.locksettings.$$Lambda$LockSettingsService$cIsW_BZK9p1jhG1yw78i3W9E4Y */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                LockSettingsService.this.lambda$notifyPasswordChanged$1$LockSettingsService(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$notifyPasswordChanged$1$LockSettingsService(int userId) {
        DevicePolicyManager dpm = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
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
        if (service == null) {
            return false;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            String password = service.getPassword();
            service.clearPassword();
            if (password == null) {
                return false;
            }
            try {
                if (this.mLockPatternUtils.isLockPatternEnabled(userId) && checkCredential(password.getBytes(), 1, userId, null).getResponseCode() == 0) {
                    return true;
                }
            } catch (Exception e) {
            }
            try {
                if (!this.mLockPatternUtils.isLockPasswordEnabled(userId) || checkCredential(password.getBytes(), 2, userId, null).getResponseCode() != 0) {
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
    /* access modifiers changed from: public */
    private void removeUser(int userId, boolean unknownUser) {
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
        Slog.e(TAG, "requireStrongAuth for : " + strongAuthReason + " UID: " + Binder.getCallingUid() + " PID: " + Binder.getCallingPid());
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

    /* JADX DEBUG: Multi-variable search result rejected for r12v0, resolved type: com.android.server.locksettings.LockSettingsService */
    /* JADX WARN: Multi-variable type inference failed */
    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) throws RemoteException {
        Throwable th;
        enforceShell();
        long origId = Binder.clearCallingIdentity();
        try {
            try {
                new LockSettingsShellCommand(new LockPatternUtils(this.mContext)).exec(this, in, out, err, args, callback, resultReceiver);
                Binder.restoreCallingIdentity(origId);
            } catch (Throwable th2) {
                th = th2;
                Binder.restoreCallingIdentity(origId);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            Binder.restoreCallingIdentity(origId);
            throw th;
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

    public String generateKeyWithMetadata(String alias, byte[] metadata) throws RemoteException {
        return this.mRecoverableKeyStoreManager.generateKeyWithMetadata(alias, metadata);
    }

    public String importKey(String alias, byte[] keyBytes) throws RemoteException {
        return this.mRecoverableKeyStoreManager.importKey(alias, keyBytes);
    }

    public String importKeyWithMetadata(String alias, byte[] keyBytes, byte[] metadata) throws RemoteException {
        return this.mRecoverableKeyStoreManager.importKeyWithMetadata(alias, keyBytes, metadata);
    }

    public String getKey(String alias) throws RemoteException {
        return this.mRecoverableKeyStoreManager.getKey(alias);
    }

    /* access modifiers changed from: private */
    public class GateKeeperDiedRecipient implements IBinder.DeathRecipient {
        private GateKeeperDiedRecipient() {
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            LockSettingsService.this.mGateKeeperService.asBinder().unlinkToDeath(this, 0);
            LockSettingsService.this.mGateKeeperService = null;
        }
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
        if (this.mInjector.isGsiRunning()) {
            Slog.w(TAG, "AuthSecret disabled in GSI");
        } else if (this.mAuthSecretService != null && this.mUserManager.getUserInfo(userId).isPrimary()) {
            try {
                byte[] rawSecret = auth.deriveVendorAuthSecret();
                ArrayList<Byte> secret = new ArrayList<>(rawSecret.length);
                for (byte b : rawSecret) {
                    secret.add(Byte.valueOf(b));
                }
                this.mAuthSecretService.primaryUserCredential(secret);
            } catch (RemoteException e) {
                Slog.w(TAG, "Failed to pass primary user secret to AuthSecret HAL", e);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void tryRemoveUserFromSpCacheLater(int userId) {
        if (userId < 0) {
            Slog.e(TAG, "Can not Removing SP from cache for user " + userId);
            return;
        }
        this.mHandler.post(new Runnable(userId) {
            /* class com.android.server.locksettings.$$Lambda$LockSettingsService$lWTrcqR9gZxLpxwBbtvTGqAifU */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                LockSettingsService.this.lambda$tryRemoveUserFromSpCacheLater$2$LockSettingsService(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$tryRemoveUserFromSpCacheLater$2$LockSettingsService(int userId) {
        if (!shouldCacheSpForUser(userId)) {
            Slog.i(TAG, "Removing SP from cache for user " + userId);
            synchronized (this.mSpManager) {
                this.mSpCache.remove(userId);
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
    @GuardedBy({"mSpManager"})
    @VisibleForTesting
    public SyntheticPasswordManager.AuthenticationToken initializeSyntheticPasswordLocked(byte[] credentialHash, byte[] credential, int credentialType, int requestedQuality, int userId) throws RemoteException {
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

    /* access modifiers changed from: protected */
    public long getSyntheticPasswordHandleLocked(int userId) {
        return getLong("sp-handle", 0, userId);
    }

    private boolean isSyntheticPasswordBasedCredentialLocked(int userId) {
        if (userId == -9999) {
            int type = this.mStorage.readPersistentDataBlock().type;
            return type == 1 || type == 2;
        }
        return (getLong("enable-sp", 1, 0) == 0 || getSyntheticPasswordHandleLocked(userId) == 0) ? false : true;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public boolean shouldMigrateToSyntheticPasswordLocked(int userId) {
        long handle = getSyntheticPasswordHandleLocked(userId);
        if (getLong("enable-sp", 1, 0) == 0 || handle != 0) {
            return false;
        }
        return true;
    }

    private void enableSyntheticPasswordLocked() {
        setLong("enable-sp", 1, 0);
    }

    private VerifyCredentialResponse spBasedDoVerifyCredential(byte[] userCredential, int credentialType, boolean hasChallenge, long challenge, int userId, ICheckCredentialProgressCallback progressCallback) throws RemoteException {
        byte[] userCredential2;
        long challenge2;
        SyntheticPasswordManager syntheticPasswordManager;
        Throwable th;
        SyntheticPasswordManager syntheticPasswordManager2;
        byte[] bArr;
        if (credentialType == -1) {
            userCredential2 = null;
        } else {
            userCredential2 = userCredential;
        }
        PackageManager pm = this.mContext.getPackageManager();
        if (hasChallenge || !pm.hasSystemFeature("android.hardware.biometrics.face") || !this.mInjector.hasEnrolledBiometrics()) {
            challenge2 = challenge;
        } else {
            challenge2 = ((FaceManager) this.mContext.getSystemService(FaceManager.class)).generateChallenge();
        }
        SyntheticPasswordManager syntheticPasswordManager3 = this.mSpManager;
        synchronized (syntheticPasswordManager3) {
            try {
                if (!isSyntheticPasswordBasedCredentialLocked(userId)) {
                    return null;
                }
                if (userId == -9999) {
                    try {
                        return this.mSpManager.verifyFrpCredential(getGateKeeperService(), userCredential2, credentialType, progressCallback);
                    } catch (Throwable th2) {
                        th = th2;
                        syntheticPasswordManager = syntheticPasswordManager3;
                        throw th;
                    }
                } else {
                    SyntheticPasswordManager.AuthenticationResult authResult = this.mSpManager.unwrapPasswordBasedSyntheticPassword(getGateKeeperService(), getSyntheticPasswordHandleLocked(userId), userCredential2, userId, progressCallback);
                    if (authResult.credentialType != credentialType) {
                        syntheticPasswordManager2 = syntheticPasswordManager3;
                    } else if (authResult.gkResponse == null) {
                        syntheticPasswordManager2 = syntheticPasswordManager3;
                    } else {
                        VerifyCredentialResponse response = authResult.gkResponse;
                        if (response.getResponseCode() == 0) {
                            bArr = null;
                            syntheticPasswordManager = syntheticPasswordManager3;
                            try {
                                response = this.mSpManager.verifyChallenge(getGateKeeperService(), authResult.authToken, challenge2, userId);
                                if (response.getResponseCode() != 0) {
                                    Slog.wtf(TAG, "verifyChallenge with SP failed.");
                                    VerifyCredentialResponse verifyCredentialResponse = VerifyCredentialResponse.ERROR;
                                    return verifyCredentialResponse;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                throw th;
                            }
                        } else {
                            bArr = null;
                            syntheticPasswordManager = syntheticPasswordManager3;
                        }
                        if (response.getResponseCode() == 0) {
                            notifyActivePasswordMetricsAvailable(credentialType, userCredential2, userId);
                            unlockKeystore(authResult.authToken.deriveKeyStorePassword(), userId);
                            if (this.mInjector.hasEnrolledBiometrics()) {
                                Slog.i(TAG, "Resetting lockout, length: " + authResult.gkResponse.getPayload().length);
                                ((BiometricManager) this.mContext.getSystemService(BiometricManager.class)).resetLockout(authResult.gkResponse.getPayload());
                                if (!hasChallenge && pm.hasSystemFeature("android.hardware.biometrics.face")) {
                                    ((FaceManager) this.mContext.getSystemService(FaceManager.class)).revokeChallenge();
                                }
                            }
                            byte[] secret = authResult.authToken.deriveDiskEncryptionKey();
                            Slog.i(TAG, "Unlocking user " + userId + " with secret only, length " + secret.length);
                            unlockUser(userId, bArr, secret);
                            activateEscrowTokens(authResult.authToken, userId);
                            if (isManagedProfileWithSeparatedLock(userId)) {
                                setDeviceUnlockedForUser(userId);
                            }
                            this.mStrongAuth.reportSuccessfulStrongAuthUnlock(userId);
                            onAuthTokenKnownForUser(userId, authResult.authToken);
                        } else if (response.getResponseCode() == 1 && response.getTimeout() > 0) {
                            requireStrongAuth(8, userId);
                            warnLog(TAG, "verifyLocked:" + response.getTimeout());
                        }
                        return response;
                    }
                    Slog.e(TAG, "Credential type mismatch or gkResponse is null.");
                    VerifyCredentialResponse verifyCredentialResponse2 = VerifyCredentialResponse.ERROR;
                    return verifyCredentialResponse2;
                }
            } catch (Throwable th4) {
                th = th4;
                syntheticPasswordManager = syntheticPasswordManager3;
                throw th;
            }
        }
    }

    private void setDeviceUnlockedForUser(int userId) {
        ((TrustManager) this.mContext.getSystemService(TrustManager.class)).setDeviceLockedForUser(userId, false);
    }

    @GuardedBy({"mSpManager"})
    private long setLockCredentialWithAuthTokenLocked(byte[] credential, int credentialType, SyntheticPasswordManager.AuthenticationToken auth, int requestedQuality, int userId) throws RemoteException {
        Map<Integer, byte[]> profilePasswords;
        long newHandle = this.mSpManager.createPasswordBasedSyntheticPassword(getGateKeeperService(), credential, credentialType, auth, requestedQuality, userId);
        flog(TAG, "setLockCredentialWithAuthTokenLocked U" + userId + "; create new Handle: " + Long.toHexString(newHandle));
        if (newHandle != 0) {
            if (credential != null) {
                profilePasswords = null;
                if (this.mSpManager.hasSidForUser(userId)) {
                    this.mSpManager.verifyChallenge(getGateKeeperService(), auth, 0, userId);
                } else {
                    this.mSpManager.newSidForUser(getGateKeeperService(), auth, userId);
                    VerifyCredentialResponse result = this.mSpManager.verifyChallenge(getGateKeeperService(), auth, 0, userId);
                    if (result == null || result.getResponseCode() != 0) {
                        boolean isWeaver = this.mSpManager.isUseWeaver(newHandle, userId);
                        flog(TAG, "sp password enroll failde, is use weaver " + isWeaver);
                        if (isWeaver) {
                            this.mSpManager.destroyPasswordBasedSyntheticPassword(newHandle, userId);
                            throw new IllegalStateException("sp password enroll failde.");
                        }
                    }
                    setAuthlessUserKeyProtection(userId, auth.deriveDiskEncryptionKey());
                    setKeystorePassword(auth.deriveKeyStorePassword(), userId);
                    setLong("sp-handle", newHandle, userId);
                    flog(TAG, "setCredentialWithAuthT: writeback HANDLE");
                    fixateNewestUserKeyAuth(userId);
                }
            } else {
                profilePasswords = getDecryptedPasswordsForAllTiedProfiles(userId);
                this.mSpManager.clearSidForUser(userId);
                getGateKeeperService().clearSecureUserId(userId);
                flog(TAG, "setCredentialWithAuthT: clearSecureUserId");
                unlockUserKey(userId, null, auth.deriveDiskEncryptionKey());
                clearUserKeyProtection(userId);
                fixateNewestUserKeyAuth(userId);
                unlockKeystore(auth.deriveKeyStorePassword(), userId);
                setKeystorePassword(null, userId);
            }
            setLong("sp-handle", newHandle, userId);
            synchronizeUnifiedWorkChallengeForProfiles(userId, profilePasswords);
            notifyActivePasswordMetricsAvailable(credentialType, credential, userId);
            if (profilePasswords != null) {
                for (Map.Entry<Integer, byte[]> entry : profilePasswords.entrySet()) {
                    Arrays.fill(entry.getValue(), (byte) 0);
                }
            }
            return newHandle;
        }
        throw new IllegalStateException("set lock credential with token locked fail.");
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x007a  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0080  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00cc  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00fe  */
    @GuardedBy({"mSpManager"})
    private void spBasedSetLockCredentialInternalLocked(byte[] credential, int credentialType, byte[] savedCredential, int requestedQuality, int userId, boolean allowUntrustedChange, boolean isLockTiedToParent) throws RemoteException {
        byte[] savedCredential2;
        SyntheticPasswordManager.AuthenticationToken auth;
        boolean untrustedReset;
        SyntheticPasswordManager.AuthenticationToken auth2;
        warnLog(TAG, "spBasedSetLockCredentialInternalLocked: user=" + userId);
        if (isManagedProfileWithUnifiedLock(userId)) {
            try {
                savedCredential2 = getDecryptedPasswordForTiedProfile(userId);
            } catch (FileNotFoundException e) {
                Slog.i(TAG, "Child profile key not found");
            } catch (IOException | InvalidAlgorithmParameterException | InvalidKeyException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e2) {
                Slog.e(TAG, "Failed to decrypt child profile key", e2);
            }
            long handle = getSyntheticPasswordHandleLocked(userId);
            SyntheticPasswordManager.AuthenticationResult authResult = this.mSpManager.unwrapPasswordBasedSyntheticPassword(getGateKeeperService(), handle, savedCredential2, userId, null);
            VerifyCredentialResponse response = authResult.gkResponse;
            auth = authResult.authToken;
            if (savedCredential2 == null && auth == null) {
                StringBuilder sb = new StringBuilder();
                sb.append("Failed to enroll ");
                sb.append(credentialType == 2 ? "password" : "pattern");
                throw new IllegalStateException(sb.toString());
            }
            if (auth == null) {
                onAuthTokenKnownForUser(userId, auth);
                untrustedReset = false;
                auth2 = auth;
            } else if (response == null) {
                warnLog(TAG, "setLockCredential response is null, error.");
                throw new IllegalStateException("Password change failed.");
            } else if (response.getResponseCode() == -1) {
                warnLog(TAG, "Untrusted credential change invoked " + allowUntrustedChange);
                SyntheticPasswordManager.AuthenticationToken auth3 = this.mSpCache.get(userId);
                if (!allowUntrustedChange) {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("Untrusted credential change was invoked but it was not allowed. This is likely a bug. Auth token is null: ");
                    sb2.append(Boolean.toString(auth3 == null));
                    throw new IllegalStateException(sb2.toString());
                }
                untrustedReset = true;
                auth2 = auth3;
            } else {
                warnLog(TAG, "Rate limit exceeded, so password was not changed.");
                throw new IllegalStateException("Rate limit exceeded, so password was not changed.");
            }
            if (auth2 == null) {
                if (untrustedReset) {
                    this.mSpManager.newSidForUser(getGateKeeperService(), auth2, userId);
                }
                long newHandle = setLockCredentialWithAuthTokenLocked(credential, credentialType, auth2, requestedQuality, userId);
                this.mSpManager.destroyPasswordBasedSyntheticPassword(handle, userId);
                this.mSpManager.backupPasswordBasedSyntheticPassword(newHandle, handle, userId);
                sendCredentialsOnChangeIfRequired(credentialType, credential, userId, isLockTiedToParent);
                return;
            }
            throw new IllegalStateException("Untrusted credential reset not possible without cached SP");
        }
        savedCredential2 = savedCredential;
        long handle2 = getSyntheticPasswordHandleLocked(userId);
        SyntheticPasswordManager.AuthenticationResult authResult2 = this.mSpManager.unwrapPasswordBasedSyntheticPassword(getGateKeeperService(), handle2, savedCredential2, userId, null);
        VerifyCredentialResponse response2 = authResult2.gkResponse;
        auth = authResult2.authToken;
        if (savedCredential2 == null) {
        }
        if (auth == null) {
        }
        if (auth2 == null) {
        }
    }

    public byte[] getHashFactor(byte[] currentCredential, int userId) throws RemoteException {
        checkPasswordReadPermission(userId);
        if (currentCredential == null || currentCredential.length == 0) {
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
            updateLockedTimeAndRetryCount(userId, auth.gkResponse);
            if (auth.authToken == null) {
                Slog.w(TAG, "Current credential is incorrect");
                return null;
            }
            return auth.authToken.derivePasswordHashFactor();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private long addEscrowToken(byte[] token, int userId, LockPatternUtils.EscrowTokenStateChangeCallback callback) throws RemoteException {
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
            handle = this.mSpManager.createTokenBasedSyntheticPassword(token, userId, callback);
            if (auth != null) {
                this.mSpManager.activateTokenBasedSyntheticPassword(handle, auth, userId);
            }
        }
        return handle;
    }

    private void activateEscrowTokens(SyntheticPasswordManager.AuthenticationToken auth, int userId) {
        synchronized (this.mSpManager) {
            disableEscrowTokenOnNonManagedDevicesIfNeeded(userId);
            for (Long l : this.mSpManager.getPendingTokensForUser(userId)) {
                long handle = l.longValue();
                Slog.i(TAG, String.format("activateEscrowTokens: %x %d ", Long.valueOf(handle), Integer.valueOf(userId)));
                this.mSpManager.activateTokenBasedSyntheticPassword(handle, auth, userId);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isEscrowTokenActive(long handle, int userId) {
        boolean existsHandle;
        synchronized (this.mSpManager) {
            existsHandle = this.mSpManager.existsHandle(handle, userId);
        }
        return existsHandle;
    }

    public boolean hasPendingEscrowToken(int userId) {
        boolean z;
        checkPasswordReadPermission(userId);
        synchronized (this.mSpManager) {
            z = !this.mSpManager.getPendingTokensForUser(userId).isEmpty();
        }
        return z;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean removeEscrowToken(long handle, int userId) {
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
    public boolean setLockCredentialWithToken(byte[] credential, int type, long tokenHandle, byte[] token, int requestedQuality, int userId) throws RemoteException {
        boolean result;
        synchronized (this.mSpManager) {
            if (this.mSpManager.hasEscrowData(userId)) {
                result = setLockCredentialWithTokenInternalLocked(credential, type, tokenHandle, token, requestedQuality, userId);
            } else {
                throw new SecurityException("Escrow token is disabled on the current user");
            }
        }
        if (result) {
            synchronized (this.mSeparateChallengeLock) {
                setSeparateProfileChallengeEnabledLocked(userId, true, null);
            }
            if (credential == null) {
                this.mHandler.post(new Runnable(userId) {
                    /* class com.android.server.locksettings.$$Lambda$LockSettingsService$3iCV7W6YQrxOv5dDGr5Cx3toXr0 */
                    private final /* synthetic */ int f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        LockSettingsService.this.lambda$setLockCredentialWithToken$3$LockSettingsService(this.f$1);
                    }
                });
            }
            notifyPasswordChanged(userId);
            notifySeparateProfileChallengeChanged(userId);
        }
        return result;
    }

    public /* synthetic */ void lambda$setLockCredentialWithToken$3$LockSettingsService(int userId) {
        unlockUser(userId, null, null);
    }

    @GuardedBy({"mSpManager"})
    private boolean setLockCredentialWithTokenInternalLocked(byte[] credential, int type, long tokenHandle, byte[] token, int requestedQuality, int userId) throws RemoteException {
        SyntheticPasswordManager.AuthenticationResult result = this.mSpManager.unwrapTokenBasedSyntheticPassword(getGateKeeperService(), tokenHandle, token, userId);
        if (result.authToken == null) {
            Slog.w(TAG, "Invalid escrow token supplied");
            return false;
        } else if (result.gkResponse.getResponseCode() != 0) {
            Slog.e(TAG, "Obsolete token: synthetic password derived but it fails GK verification.");
            return false;
        } else {
            setLong("lockscreen.password_type", (long) requestedQuality, userId);
            long oldHandle = getSyntheticPasswordHandleLocked(userId);
            setLockCredentialWithAuthTokenLocked(credential, type, result.authToken, requestedQuality, userId);
            this.mSpManager.destroyPasswordBasedSyntheticPassword(oldHandle, userId);
            onAuthTokenKnownForUser(userId, result.authToken);
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean unlockUserWithToken(long tokenHandle, byte[] token, int userId) throws RemoteException {
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
            pw.println(String.format("SP Enabled = %b", Boolean.valueOf(this.mLockPatternUtils.isSyntheticPasswordEnabled())));
            List<UserInfo> users = this.mUserManager.getUsers();
            for (int user = 0; user < users.size(); user++) {
                int userId = users.get(user).id;
                pw.println("    User " + userId);
                synchronized (this.mSpManager) {
                    pw.println(String.format("        SP Handle = %x", Long.valueOf(getSyntheticPasswordHandleLocked(userId))));
                }
                try {
                    pw.println(String.format("        SID = %x", Long.valueOf(getGateKeeperService().getSecureUserId(userId))));
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

    private class DeviceProvisionedObserver extends ContentObserver {
        private final Uri mDeviceProvisionedUri = Settings.Global.getUriFor("device_provisioned");
        private boolean mRegistered;
        private final Uri mUserSetupCompleteUri = Settings.Secure.getUriFor("user_setup_complete");

        public DeviceProvisionedObserver() {
            super(null);
        }

        @Override // android.database.ContentObserver
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
                        return;
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

    private final class LocalService extends LockSettingsInternal {
        private LocalService() {
        }

        public long addEscrowToken(byte[] token, int userId, LockPatternUtils.EscrowTokenStateChangeCallback callback) {
            try {
                return LockSettingsService.this.addEscrowToken(token, userId, callback);
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

        public boolean setLockCredentialWithToken(byte[] credential, int type, long tokenHandle, byte[] token, int requestedQuality, int userId) {
            if (LockSettingsService.this.mLockPatternUtils.hasSecureLockScreen()) {
                try {
                    return LockSettingsService.this.setLockCredentialWithToken(credential, type, tokenHandle, token, requestedQuality, userId);
                } catch (RemoteException re) {
                    throw re.rethrowFromSystemServer();
                }
            } else {
                throw new UnsupportedOperationException("This operation requires secure lock screen feature.");
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

    /* access modifiers changed from: protected */
    public void onUserAdded(int userHandle) {
    }

    /* access modifiers changed from: protected */
    public void showEncryptionNotificationForUsers() {
    }

    /* access modifiers changed from: protected */
    public void showEncryptionNotificationForUser(UserHandle user) {
    }

    /* access modifiers changed from: protected */
    public void addSDCardUserKeyAuth(int userId, UserInfo userInfo, byte[] token, byte[] secret) {
    }

    /* access modifiers changed from: package-private */
    public void warnLog(String tag, String msg) {
    }

    /* access modifiers changed from: package-private */
    public void flog(String tag, String msg) {
    }

    /* access modifiers changed from: protected */
    public void updateRemainLockedTimeAfterReboot() {
    }

    /* access modifiers changed from: protected */
    public void updateLockedTimeAndRetryCount(int userId, VerifyCredentialResponse response) {
    }
}
