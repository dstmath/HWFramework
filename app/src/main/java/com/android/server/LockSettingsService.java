package com.android.server;

import android.app.ActivityManagerNative;
import android.app.KeyguardManager;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.app.backup.BackupManager;
import android.app.trust.IStrongAuthTracker;
import android.app.trust.TrustManager;
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
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.IMountService;
import android.os.storage.StorageManager;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.security.KeyStore;
import android.security.keystore.AndroidKeyStoreProvider;
import android.security.keystore.KeyProtection;
import android.service.gatekeeper.GateKeeperResponse;
import android.service.gatekeeper.IGateKeeperService;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import com.android.internal.util.ArrayUtils;
import com.android.internal.widget.ILockSettings.Stub;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternUtils.StrongAuthTracker;
import com.android.internal.widget.VerifyCredentialResponse;
import com.android.server.LockSettingsStorage.Callback;
import com.android.server.am.ProcessList;
import com.android.server.wm.WindowState;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

public class LockSettingsService extends Stub {
    private static final Intent ACTION_NULL = null;
    private static final boolean DEBUG = false;
    private static final int FBE_ENCRYPTED_NOTIFICATION = 0;
    private static final String PERMISSION = "android.permission.ACCESS_KEYGUARD_SECURE_STORAGE";
    private static final int PROFILE_KEY_IV_SIZE = 12;
    private static final String[] READ_CONTACTS_PROTECTED_SETTINGS = null;
    private static final String[] READ_PASSWORD_PROTECTED_SETTINGS = null;
    private static final String SEPARATE_PROFILE_CHALLENGE_KEY = "lockscreen.profilechallenge";
    private static final String[] SETTINGS_TO_BACKUP = null;
    private static final int[] SYSTEM_CREDENTIAL_UIDS = null;
    private static final String TAG = "LockSettingsService";
    private static final String[] VALID_SETTINGS = null;
    private final BroadcastReceiver mBroadcastReceiver;
    private final Context mContext;
    private boolean mFirstCallToVold;
    private IGateKeeperService mGateKeeperService;
    private final Handler mHandler;
    private final KeyStore mKeyStore;
    protected LockPatternUtils mLockPatternUtils;
    private NotificationManager mNotificationManager;
    private final Object mSeparateChallengeLock;
    private final LockSettingsStorage mStorage;
    private final LockSettingsStrongAuth mStrongAuth;
    private final SynchronizedStrongAuthTracker mStrongAuthTracker;
    private UserManager mUserManager;

    final /* synthetic */ class -void_notifyPasswordChanged_int_userId_LambdaImpl0 implements Runnable {
        private /* synthetic */ LockSettingsService val$this;
        private /* synthetic */ int val$userId;

        public /* synthetic */ -void_notifyPasswordChanged_int_userId_LambdaImpl0(LockSettingsService lockSettingsService, int i) {
            this.val$this = lockSettingsService;
            this.val$userId = i;
        }

        public void run() {
            this.val$this.-com_android_server_LockSettingsService_lambda$1(this.val$userId);
        }
    }

    /* renamed from: com.android.server.LockSettingsService.3 */
    class AnonymousClass3 implements Runnable {
        final /* synthetic */ int val$userId;

        AnonymousClass3(int val$userId) {
            this.val$userId = val$userId;
        }

        public void run() {
            LockSettingsService.this.tieManagedProfileLockIfNecessary(this.val$userId, null);
        }
    }

    /* renamed from: com.android.server.LockSettingsService.4 */
    class AnonymousClass4 extends IProgressListener.Stub {
        final /* synthetic */ CountDownLatch val$latch;

        AnonymousClass4(CountDownLatch val$latch) {
            this.val$latch = val$latch;
        }

        public void onStarted(int id, Bundle extras) throws RemoteException {
            Log.d(LockSettingsService.TAG, "unlockUser started");
        }

        public void onProgress(int id, int progress, Bundle extras) throws RemoteException {
            Log.d(LockSettingsService.TAG, "unlockUser progress " + progress);
        }

        public void onFinished(int id, Bundle extras) throws RemoteException {
            Log.d(LockSettingsService.TAG, "unlockUser finished");
            this.val$latch.countDown();
        }
    }

    private interface CredentialUtil {
        String adjustForKeystore(String str);

        void setCredential(String str, String str2, int i) throws RemoteException;

        byte[] toHash(String str, int i);
    }

    /* renamed from: com.android.server.LockSettingsService.7 */
    class AnonymousClass7 implements Runnable {
        final /* synthetic */ String val$password;
        final /* synthetic */ int val$quality;
        final /* synthetic */ int val$userId;

        AnonymousClass7(String val$password, int val$quality, int val$userId) {
            this.val$password = val$password;
            this.val$quality = val$quality;
            this.val$userId = val$userId;
        }

        public void run() {
            int length = LockSettingsService.FBE_ENCRYPTED_NOTIFICATION;
            int letters = LockSettingsService.FBE_ENCRYPTED_NOTIFICATION;
            int uppercase = LockSettingsService.FBE_ENCRYPTED_NOTIFICATION;
            int lowercase = LockSettingsService.FBE_ENCRYPTED_NOTIFICATION;
            int numbers = LockSettingsService.FBE_ENCRYPTED_NOTIFICATION;
            int symbols = LockSettingsService.FBE_ENCRYPTED_NOTIFICATION;
            int nonletter = LockSettingsService.FBE_ENCRYPTED_NOTIFICATION;
            if (this.val$password != null) {
                length = this.val$password.length();
                for (int i = LockSettingsService.FBE_ENCRYPTED_NOTIFICATION; i < length; i++) {
                    char c = this.val$password.charAt(i);
                    if (c >= 'A' && c <= 'Z') {
                        letters++;
                        uppercase++;
                    } else if (c >= 'a' && c <= 'z') {
                        letters++;
                        lowercase++;
                    } else if (c < '0' || c > '9') {
                        symbols++;
                        nonletter++;
                    } else {
                        numbers++;
                        nonletter++;
                    }
                }
            }
            ((DevicePolicyManager) LockSettingsService.this.mContext.getSystemService("device_policy")).setActivePasswordState(this.val$quality, length, letters, uppercase, lowercase, numbers, symbols, nonletter, this.val$userId);
        }
    }

    private class GateKeeperDiedRecipient implements DeathRecipient {
        private GateKeeperDiedRecipient() {
        }

        public void binderDied() {
            LockSettingsService.this.mGateKeeperService.asBinder().unlinkToDeath(this, LockSettingsService.FBE_ENCRYPTED_NOTIFICATION);
            LockSettingsService.this.mGateKeeperService = null;
        }
    }

    public static final class Lifecycle extends SystemService {
        private LockSettingsService mLockSettingsService;

        public Lifecycle(Context context) {
            super(context);
        }

        public void onStart() {
            AndroidKeyStoreProvider.install();
            this.mLockSettingsService = new LockSettingsService(getContext());
            publishBinderService("lock_settings", this.mLockSettingsService);
        }

        public void onBootPhase(int phase) {
            if (phase == SystemService.PHASE_ACTIVITY_MANAGER_READY) {
                this.mLockSettingsService.maybeShowEncryptionNotifications();
            } else if (phase != ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
            }
        }

        public void onUnlockUser(int userHandle) {
            this.mLockSettingsService.onUnlockUser(userHandle);
        }

        public void onCleanupUser(int userHandle) {
            this.mLockSettingsService.onCleanupUser(userHandle);
        }
    }

    private class SynchronizedStrongAuthTracker extends StrongAuthTracker {
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

        void register() {
            LockSettingsService.this.mStrongAuth.registerStrongAuthTracker(this.mStub);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.LockSettingsService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.LockSettingsService.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.LockSettingsService.<clinit>():void");
    }

    public void tieManagedProfileLockIfNecessary(int managedUserId, String managedUserPassword) {
        if (UserManager.get(this.mContext).getUserInfo(managedUserId).isManagedProfile() && !this.mLockPatternUtils.isSeparateProfileChallengeEnabled(managedUserId) && !this.mStorage.hasChildProfileLock(managedUserId)) {
            int parentId = this.mUserManager.getProfileParent(managedUserId).id;
            if (this.mStorage.hasPassword(parentId) || this.mStorage.hasPattern(parentId)) {
                byte[] randomLockSeed = new byte[FBE_ENCRYPTED_NOTIFICATION];
                try {
                    String newPassword = String.valueOf(HexEncoding.encode(SecureRandom.getInstance("SHA1PRNG").generateSeed(40)));
                    setLockPasswordInternal(newPassword, managedUserPassword, managedUserId);
                    setLong("lockscreen.password_type", 327680, managedUserId);
                    tieProfileLockToParent(managedUserId, newPassword);
                } catch (Exception e) {
                    Slog.e(TAG, "Fail to tie managed profile", e);
                }
            }
        }
    }

    public LockSettingsService(Context context) {
        this.mSeparateChallengeLock = new Object();
        this.mKeyStore = KeyStore.getInstance();
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int userHandle;
                if ("android.intent.action.USER_ADDED".equals(intent.getAction())) {
                    userHandle = intent.getIntExtra("android.intent.extra.user_handle", LockSettingsService.FBE_ENCRYPTED_NOTIFICATION);
                    if (userHandle > 0) {
                        LockSettingsService.this.removeUser(userHandle, true);
                    }
                    KeyStore ks = KeyStore.getInstance();
                    UserInfo parentInfo = LockSettingsService.this.mUserManager.getProfileParent(userHandle);
                    ks.onUserAdded(userHandle, parentInfo != null ? parentInfo.id : -1);
                } else if ("android.intent.action.USER_STARTING".equals(intent.getAction())) {
                    LockSettingsService.this.mStorage.prefetchUser(intent.getIntExtra("android.intent.extra.user_handle", LockSettingsService.FBE_ENCRYPTED_NOTIFICATION));
                } else if ("android.intent.action.USER_REMOVED".equals(intent.getAction())) {
                    userHandle = intent.getIntExtra("android.intent.extra.user_handle", LockSettingsService.FBE_ENCRYPTED_NOTIFICATION);
                    if (userHandle > 0) {
                        LockSettingsService.this.removeUser(userHandle, LockSettingsService.DEBUG);
                    }
                }
            }
        };
        this.mContext = context;
        this.mHandler = new Handler();
        this.mStrongAuth = new LockSettingsStrongAuth(context);
        this.mLockPatternUtils = new LockPatternUtils(context);
        this.mFirstCallToVold = true;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_ADDED");
        filter.addAction("android.intent.action.USER_STARTING");
        filter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, filter, null, null);
        this.mStorage = new LockSettingsStorage(context, new Callback() {
            public void initialize(SQLiteDatabase db) {
                if (SystemProperties.getBoolean("ro.lockscreen.disable.default", LockSettingsService.DEBUG)) {
                    LockSettingsService.this.mStorage.writeKeyValue(db, "lockscreen.disabled", "1", LockSettingsService.FBE_ENCRYPTED_NOTIFICATION);
                }
            }
        });
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mStrongAuthTracker = new SynchronizedStrongAuthTracker(this.mContext);
        this.mStrongAuthTracker.register();
    }

    private void maybeShowEncryptionNotifications() {
        List<UserInfo> users = this.mUserManager.getUsers();
        for (int i = FBE_ENCRYPTED_NOTIFICATION; i < users.size(); i++) {
            UserInfo user = (UserInfo) users.get(i);
            UserHandle userHandle = user.getUserHandle();
            if ((!this.mStorage.hasPassword(user.id) ? this.mStorage.hasPattern(user.id) : true) && !this.mUserManager.isUserUnlockingOrUnlocked(userHandle)) {
                if (user.isManagedProfile()) {
                    UserInfo parent = this.mUserManager.getProfileParent(user.id);
                    if (!(parent == null || !this.mUserManager.isUserUnlockingOrUnlocked(parent.getUserHandle()) || this.mUserManager.isQuietModeEnabled(userHandle))) {
                        showEncryptionNotificationForProfile(userHandle);
                    }
                } else {
                    showEncryptionNotification(userHandle);
                }
            }
        }
    }

    private void showEncryptionNotificationForProfile(UserHandle user) {
        Resources r = this.mContext.getResources();
        CharSequence title = r.getText(17040848);
        CharSequence message = r.getText(17040852);
        CharSequence detail = r.getText(17040851);
        Intent unlockIntent = ((KeyguardManager) this.mContext.getSystemService("keyguard")).createConfirmDeviceCredentialIntent(null, null, user.getIdentifier());
        if (unlockIntent != null) {
            unlockIntent.setFlags(276824064);
            showEncryptionNotification(user, title, message, detail, PendingIntent.getActivity(this.mContext, FBE_ENCRYPTED_NOTIFICATION, unlockIntent, 134217728));
        }
    }

    private void showEncryptionNotification(UserHandle user) {
        Resources r = this.mContext.getResources();
        showEncryptionNotification(user, r.getText(33685887), r.getText(33685888), r.getText(17040850), PendingIntent.getBroadcast(this.mContext, FBE_ENCRYPTED_NOTIFICATION, ACTION_NULL, 134217728));
    }

    private void showEncryptionNotification(UserHandle user, CharSequence title, CharSequence message, CharSequence detail, PendingIntent intent) {
        if (StorageManager.isFileEncryptedNativeOrEmulated()) {
            this.mNotificationManager.notifyAsUser(null, FBE_ENCRYPTED_NOTIFICATION, new Builder(this.mContext).setSmallIcon(17302570).setWhen(0).setOngoing(true).setTicker(title).setDefaults(FBE_ENCRYPTED_NOTIFICATION).setPriority(2).setColor(this.mContext.getColor(17170519)).setContentTitle(title).setContentText(message).setSubText(detail).setVisibility(1).setContentIntent(intent).build(), user);
        }
    }

    public void hideEncryptionNotification(UserHandle userHandle) {
        this.mNotificationManager.cancelAsUser(null, FBE_ENCRYPTED_NOTIFICATION, userHandle);
    }

    public void onCleanupUser(int userId) {
        hideEncryptionNotification(new UserHandle(userId));
    }

    public void onUnlockUser(int userId) {
        hideEncryptionNotification(new UserHandle(userId));
        if (this.mUserManager.getUserInfo(userId).isManagedProfile()) {
            this.mHandler.post(new AnonymousClass3(userId));
        }
        List<UserInfo> profiles = this.mUserManager.getProfiles(userId);
        for (int i = FBE_ENCRYPTED_NOTIFICATION; i < profiles.size(); i++) {
            UserInfo profile = (UserInfo) profiles.get(i);
            if ((!this.mStorage.hasPassword(profile.id) ? this.mStorage.hasPattern(profile.id) : true) && profile.isManagedProfile()) {
                UserHandle userHandle = profile.getUserHandle();
                if (!(this.mUserManager.isUserUnlockingOrUnlocked(userHandle) || this.mUserManager.isQuietModeEnabled(userHandle))) {
                    showEncryptionNotificationForProfile(userHandle);
                }
            }
        }
    }

    public void systemReady() {
        migrateOldData();
        try {
            getGateKeeperService();
        } catch (RemoteException e) {
            Slog.e(TAG, "Failure retrieving IGateKeeperService", e);
        }
        this.mStorage.prefetchUser(FBE_ENCRYPTED_NOTIFICATION);
    }

    private void migrateOldData() {
        int userId;
        try {
            ContentResolver cr;
            List<UserInfo> users;
            int i;
            if (getString("migrated", null, FBE_ENCRYPTED_NOTIFICATION) == null) {
                cr = this.mContext.getContentResolver();
                String[] strArr = VALID_SETTINGS;
                int length = strArr.length;
                for (int i2 = FBE_ENCRYPTED_NOTIFICATION; i2 < length; i2++) {
                    String validSetting = strArr[i2];
                    String value = Secure.getString(cr, validSetting);
                    if (value != null) {
                        setString(validSetting, value, FBE_ENCRYPTED_NOTIFICATION);
                    }
                }
                setString("migrated", "true", FBE_ENCRYPTED_NOTIFICATION);
                Slog.i(TAG, "Migrated lock settings to new location");
            }
            if (getString("migrated_user_specific", null, FBE_ENCRYPTED_NOTIFICATION) == null) {
                cr = this.mContext.getContentResolver();
                users = this.mUserManager.getUsers();
                for (int user = FBE_ENCRYPTED_NOTIFICATION; user < users.size(); user++) {
                    userId = ((UserInfo) users.get(user)).id;
                    String OWNER_INFO = "lock_screen_owner_info";
                    String ownerInfo = Secure.getStringForUser(cr, "lock_screen_owner_info", userId);
                    if (!TextUtils.isEmpty(ownerInfo)) {
                        setString("lock_screen_owner_info", ownerInfo, userId);
                        Secure.putStringForUser(cr, "lock_screen_owner_info", "", userId);
                    }
                    String OWNER_INFO_ENABLED = "lock_screen_owner_info_enabled";
                    try {
                        setLong("lock_screen_owner_info_enabled", (long) (Secure.getIntForUser(cr, "lock_screen_owner_info_enabled", userId) != 0 ? true : DEBUG ? 1 : FBE_ENCRYPTED_NOTIFICATION), userId);
                    } catch (SettingNotFoundException e) {
                        if (!TextUtils.isEmpty(ownerInfo)) {
                            setLong("lock_screen_owner_info_enabled", 1, userId);
                        }
                    }
                    Secure.putIntForUser(cr, "lock_screen_owner_info_enabled", FBE_ENCRYPTED_NOTIFICATION, userId);
                }
                setString("migrated_user_specific", "true", FBE_ENCRYPTED_NOTIFICATION);
                Slog.i(TAG, "Migrated per-user lock settings to new location");
            }
            if (getString("migrated_biometric_weak", null, FBE_ENCRYPTED_NOTIFICATION) == null) {
                users = this.mUserManager.getUsers();
                for (i = FBE_ENCRYPTED_NOTIFICATION; i < users.size(); i++) {
                    userId = ((UserInfo) users.get(i)).id;
                    long type = getLong("lockscreen.password_type", 0, userId);
                    long alternateType = getLong("lockscreen.password_type_alternate", 0, userId);
                    if (type == 32768) {
                        setLong("lockscreen.password_type", alternateType, userId);
                    }
                    setLong("lockscreen.password_type_alternate", 0, userId);
                }
                setString("migrated_biometric_weak", "true", FBE_ENCRYPTED_NOTIFICATION);
                Slog.i(TAG, "Migrated biometric weak to use the fallback instead");
            }
            if (getString("migrated_lockscreen_disabled", null, FBE_ENCRYPTED_NOTIFICATION) == null) {
                users = this.mUserManager.getUsers();
                int userCount = users.size();
                int switchableUsers = FBE_ENCRYPTED_NOTIFICATION;
                for (i = FBE_ENCRYPTED_NOTIFICATION; i < userCount; i++) {
                    if (((UserInfo) users.get(i)).supportsSwitchTo()) {
                        switchableUsers++;
                    }
                }
                if (switchableUsers > 1) {
                    for (i = FBE_ENCRYPTED_NOTIFICATION; i < userCount; i++) {
                        int id = ((UserInfo) users.get(i)).id;
                        if (getBoolean("lockscreen.disabled", DEBUG, id)) {
                            setBoolean("lockscreen.disabled", DEBUG, id);
                        }
                    }
                }
                setString("migrated_lockscreen_disabled", "true", FBE_ENCRYPTED_NOTIFICATION);
                Slog.i(TAG, "Migrated lockscreen disabled flag");
            }
            users = this.mUserManager.getUsers();
            for (i = FBE_ENCRYPTED_NOTIFICATION; i < users.size(); i++) {
                UserInfo userInfo = (UserInfo) users.get(i);
                if (userInfo.isManagedProfile()) {
                    if (this.mStorage.hasChildProfileLock(userInfo.id)) {
                        long quality = getLong("lockscreen.password_type", 0, userInfo.id);
                        if (quality == 0) {
                            Slog.i(TAG, "Migrated tied profile lock type");
                            setLong("lockscreen.password_type", 327680, userInfo.id);
                        } else if (quality != 327680) {
                            Slog.e(TAG, "Invalid tied profile lock type: " + quality);
                        }
                    }
                }
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
        int i = FBE_ENCRYPTED_NOTIFICATION;
        while (i < READ_CONTACTS_PROTECTED_SETTINGS.length) {
            if (!READ_CONTACTS_PROTECTED_SETTINGS[i].equals(requestedKey) || this.mContext.checkCallingOrSelfPermission("android.permission.READ_CONTACTS") == 0) {
                i++;
            } else {
                throw new SecurityException("uid=" + callingUid + " needs permission " + "android.permission.READ_CONTACTS" + " to read " + requestedKey + " for user " + userId);
            }
        }
        i = FBE_ENCRYPTED_NOTIFICATION;
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
            z = getBoolean(SEPARATE_PROFILE_CHALLENGE_KEY, DEBUG, userId);
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

    public boolean havePassword(int userId) throws RemoteException {
        return this.mStorage.hasPassword(userId);
    }

    public boolean havePattern(int userId) throws RemoteException {
        return this.mStorage.hasPattern(userId);
    }

    protected void setKeystorePassword(String password, int userHandle) {
        KeyStore.getInstance().onUserPasswordChanged(userHandle, password);
    }

    private void unlockKeystore(String password, int userHandle) {
        KeyStore.getInstance().unlock(userHandle, password);
    }

    private String getDecryptedPasswordForTiedProfile(int userId) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, CertificateException, IOException {
        byte[] storedData = this.mStorage.readChildProfileLock(userId);
        if (storedData == null) {
            throw new FileNotFoundException("Child profile lock file not found");
        }
        byte[] iv = Arrays.copyOfRange(storedData, FBE_ENCRYPTED_NOTIFICATION, PROFILE_KEY_IV_SIZE);
        byte[] encryptedPassword = Arrays.copyOfRange(storedData, PROFILE_KEY_IV_SIZE, storedData.length);
        java.security.KeyStore keyStore = java.security.KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        SecretKey decryptionKey = (SecretKey) keyStore.getKey("profile_key_name_decrypt_" + userId, null);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(2, decryptionKey, new GCMParameterSpec(DumpState.DUMP_PACKAGES, iv));
        return new String(cipher.doFinal(encryptedPassword), StandardCharsets.UTF_8);
    }

    private void unlockChildProfile(int profileHandle) throws RemoteException {
        try {
            doVerifyPassword(getDecryptedPasswordForTiedProfile(profileHandle), DEBUG, 0, profileHandle);
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                Slog.i(TAG, "Child profile key not found");
            } else {
                Slog.e(TAG, "Failed to decrypt child profile key", e);
            }
        }
    }

    private void unlockUser(int userId, byte[] token, byte[] secret) {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            ActivityManagerNative.getDefault().unlockUser(userId, token, secret, new AnonymousClass4(latch));
            try {
                latch.await(15, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            try {
                if (this.mUserManager.getUserInfo(userId) != null && !this.mUserManager.getUserInfo(userId).isManagedProfile()) {
                    for (UserInfo pi : this.mUserManager.getProfiles(userId)) {
                        if (pi.isManagedProfile() && !this.mLockPatternUtils.isSeparateProfileChallengeEnabled(pi.id) && this.mStorage.hasChildProfileLock(pi.id)) {
                            unlockChildProfile(pi.id);
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

    private byte[] getCurrentHandle(int userId) {
        byte[] currentHandle;
        int currentHandleType = this.mStorage.getStoredCredentialType(userId);
        Log.i(TAG, "getCurrentHandle by user " + userId + ", currentHandleType " + currentHandleType);
        CredentialHash credential;
        switch (currentHandleType) {
            case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                credential = this.mStorage.readPatternHash(userId);
                if (credential == null) {
                    currentHandle = null;
                    break;
                }
                currentHandle = credential.hash;
                break;
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                credential = this.mStorage.readPasswordHash(userId);
                if (credential == null) {
                    currentHandle = null;
                    break;
                }
                currentHandle = credential.hash;
                break;
            default:
                currentHandle = null;
                break;
        }
        if (currentHandleType != -1 && currentHandle == null) {
            Slog.e(TAG, "Stored handle type [" + currentHandleType + "] but no handle available");
        }
        return currentHandle;
    }

    private void onUserLockChanged(int userId) throws RemoteException {
        if (this.mUserManager.getUserInfo(userId) == null || !this.mUserManager.getUserInfo(userId).isManagedProfile()) {
            boolean isSecure = !this.mStorage.hasPassword(userId) ? this.mStorage.hasPattern(userId) : true;
            List<UserInfo> profiles = this.mUserManager.getProfiles(userId);
            int size = profiles.size();
            for (int i = FBE_ENCRYPTED_NOTIFICATION; i < size; i++) {
                UserInfo profile = (UserInfo) profiles.get(i);
                if (profile.isManagedProfile()) {
                    int managedUserId = profile.id;
                    if (!this.mLockPatternUtils.isSeparateProfileChallengeEnabled(managedUserId)) {
                        if (isSecure) {
                            tieManagedProfileLockIfNecessary(managedUserId, null);
                        } else {
                            clearUserKeyProtection(managedUserId);
                            getGateKeeperService().clearSecureUserId(managedUserId);
                            this.mStorage.writePatternHash(null, managedUserId);
                            setKeystorePassword(null, managedUserId);
                            fixateNewestUserKeyAuth(managedUserId);
                            this.mStorage.removeChildProfileLock(managedUserId);
                            removeKeystoreProfileKey(managedUserId);
                        }
                    }
                }
            }
        }
    }

    private boolean isManagedProfileWithUnifiedLock(int userId) {
        if (this.mUserManager.getUserInfo(userId) == null || !this.mUserManager.getUserInfo(userId).isManagedProfile() || this.mLockPatternUtils.isSeparateProfileChallengeEnabled(userId)) {
            return DEBUG;
        }
        return true;
    }

    private boolean isManagedProfileWithSeparatedLock(int userId) {
        if (this.mUserManager.getUserInfo(userId) == null || !this.mUserManager.getUserInfo(userId).isManagedProfile()) {
            return DEBUG;
        }
        return this.mLockPatternUtils.isSeparateProfileChallengeEnabled(userId);
    }

    public void setLockPattern(String pattern, String savedCredential, int userId) throws RemoteException {
        checkWritePermission(userId);
        synchronized (this.mSeparateChallengeLock) {
            setLockPatternInternal(pattern, savedCredential, userId);
            setSeparateProfileChallengeEnabled(userId, true, null);
            notifyPasswordChanged(userId);
        }
    }

    private void setLockPatternInternal(String pattern, String savedCredential, int userId) throws RemoteException {
        byte[] currentHandle = getCurrentHandle(userId);
        if (pattern == null) {
            clearUserKeyProtection(userId);
            getGateKeeperService().clearSecureUserId(userId);
            this.mStorage.writePatternHash(null, userId);
            setKeystorePassword(null, userId);
            fixateNewestUserKeyAuth(userId);
            onUserLockChanged(userId);
            notifyActivePasswordMetricsAvailable(null, userId);
            Slog.w(TAG, "setLockPattern to null success");
            return;
        }
        if (isManagedProfileWithUnifiedLock(userId)) {
            try {
                savedCredential = getDecryptedPasswordForTiedProfile(userId);
            } catch (Exception e) {
                if (e instanceof FileNotFoundException) {
                    Slog.i(TAG, "Child profile key not found");
                } else {
                    Slog.e(TAG, "Failed to decrypt child profile key", e);
                }
            }
        } else if (currentHandle == null) {
            if (savedCredential != null) {
                Slog.w(TAG, "Saved credential provided, but none stored");
            }
            savedCredential = null;
        }
        byte[] enrolledHandle = enrollCredential(currentHandle, savedCredential, pattern, userId);
        if (enrolledHandle != null) {
            setUserKeyProtection(userId, pattern, doVerifyPattern(pattern, new CredentialHash(enrolledHandle, 1), true, 0, userId));
            this.mStorage.writePatternHash(enrolledHandle, userId);
            fixateNewestUserKeyAuth(userId);
            onUserLockChanged(userId);
            Slog.w(TAG, "set new LockPattern success");
            return;
        }
        Slog.e(TAG, "Failed to enroll pattern");
        throw new RemoteException("Failed to enroll pattern");
    }

    public void setLockPassword(String password, String savedCredential, int userId) throws RemoteException {
        checkWritePermission(userId);
        synchronized (this.mSeparateChallengeLock) {
            setLockPasswordInternal(password, savedCredential, userId);
            setSeparateProfileChallengeEnabled(userId, true, null);
            notifyPasswordChanged(userId);
        }
    }

    private void setLockPasswordInternal(String password, String savedCredential, int userId) throws RemoteException {
        byte[] currentHandle = getCurrentHandle(userId);
        if (password == null) {
            clearUserKeyProtection(userId);
            getGateKeeperService().clearSecureUserId(userId);
            this.mStorage.writePasswordHash(null, userId);
            setKeystorePassword(null, userId);
            fixateNewestUserKeyAuth(userId);
            onUserLockChanged(userId);
            notifyActivePasswordMetricsAvailable(null, userId);
            Slog.w(TAG, "setLockPassword to null success");
            return;
        }
        if (isManagedProfileWithUnifiedLock(userId)) {
            try {
                savedCredential = getDecryptedPasswordForTiedProfile(userId);
            } catch (FileNotFoundException e) {
                Slog.i(TAG, "Child profile key not found");
            } catch (Exception e2) {
                Slog.e(TAG, "Failed to decrypt child profile key", e2);
            }
        } else if (currentHandle == null) {
            if (savedCredential != null) {
                Slog.w(TAG, "Saved credential provided, but none stored");
            }
            savedCredential = null;
        }
        byte[] enrolledHandle = enrollCredential(currentHandle, savedCredential, password, userId);
        if (enrolledHandle != null) {
            setUserKeyProtection(userId, password, doVerifyPassword(password, new CredentialHash(enrolledHandle, 1), true, 0, userId));
            this.mStorage.writePasswordHash(enrolledHandle, userId);
            fixateNewestUserKeyAuth(userId);
            onUserLockChanged(userId);
            Slog.w(TAG, "set new LockPassword success");
            return;
        }
        Slog.e(TAG, "Failed to enroll password");
        throw new RemoteException("Failed to enroll password");
    }

    private void tieProfileLockToParent(int userId, String password) {
        byte[] randomLockSeed = password.getBytes(StandardCharsets.UTF_8);
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();
            java.security.KeyStore keyStore = java.security.KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            keyStore.setEntry("profile_key_name_encrypt_" + userId, new SecretKeyEntry(secretKey), new KeyProtection.Builder(1).setBlockModes(new String[]{"GCM"}).setEncryptionPaddings(new String[]{"NoPadding"}).build());
            keyStore.setEntry("profile_key_name_decrypt_" + userId, new SecretKeyEntry(secretKey), new KeyProtection.Builder(2).setBlockModes(new String[]{"GCM"}).setEncryptionPaddings(new String[]{"NoPadding"}).setUserAuthenticationRequired(true).setUserAuthenticationValidityDurationSeconds(30).build());
            SecretKey keyStoreEncryptionKey = (SecretKey) keyStore.getKey("profile_key_name_encrypt_" + userId, null);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(1, keyStoreEncryptionKey);
            byte[] encryptionResult = cipher.doFinal(randomLockSeed);
            byte[] iv = cipher.getIV();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                if (iv.length != PROFILE_KEY_IV_SIZE) {
                    throw new RuntimeException("Invalid iv length: " + iv.length);
                }
                outputStream.write(iv);
                outputStream.write(encryptionResult);
                this.mStorage.writeChildProfileLock(userId, outputStream.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException("Failed to concatenate byte arrays", e);
            }
        } catch (Exception e2) {
            throw new RuntimeException("Failed to encrypt key", e2);
        }
    }

    private byte[] enrollCredential(byte[] enrolledHandle, String enrolledCredential, String toEnroll, int userId) throws RemoteException {
        byte[] bArr;
        byte[] bArr2;
        checkWritePermission(userId);
        if (enrolledCredential == null) {
            bArr = null;
        } else {
            bArr = enrolledCredential.getBytes();
        }
        if (toEnroll == null) {
            bArr2 = null;
        } else {
            bArr2 = toEnroll.getBytes();
        }
        GateKeeperResponse response = getGateKeeperService().enroll(userId, enrolledHandle, bArr, bArr2);
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
            digest.update(Arrays.copyOf("Android FBE credential hash".getBytes(StandardCharsets.UTF_8), DumpState.DUMP_PACKAGES));
            digest.update(credential.getBytes(StandardCharsets.UTF_8));
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException for SHA-512");
        }
    }

    private void addUserKeyAuth(int userId, byte[] token, byte[] secret) throws RemoteException {
        UserInfo userInfo = UserManager.get(this.mContext).getUserInfo(userId);
        if (userInfo != null) {
            IMountService mountService = getMountService();
            long callingId = Binder.clearCallingIdentity();
            try {
                mountService.addUserKeyAuth(userId, userInfo.serialNumber, token, secret);
                ISDCardCryptedHelper helper = HwServiceFactory.getSDCardCryptedHelper();
                if (helper != null) {
                    helper.addUserKeyAuth(userId, userInfo.serialNumber, token, secret);
                }
                Binder.restoreCallingIdentity(callingId);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(callingId);
            }
        }
    }

    private void fixateNewestUserKeyAuth(int userId) throws RemoteException {
        if (2147483646 != userId) {
            IMountService mountService = getMountService();
            long callingId = Binder.clearCallingIdentity();
            try {
                mountService.fixateNewestUserKeyAuth(userId);
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        }
    }

    public void resetKeyStore(int userId) throws RemoteException {
        checkWritePermission(userId);
        int managedUserId = -1;
        String managedUserDecryptedPassword = null;
        for (UserInfo pi : this.mUserManager.getProfiles(userId)) {
            if (pi.isManagedProfile() && !this.mLockPatternUtils.isSeparateProfileChallengeEnabled(pi.id) && this.mStorage.hasChildProfileLock(pi.id)) {
                if (managedUserId == -1) {
                    try {
                        managedUserDecryptedPassword = getDecryptedPasswordForTiedProfile(pi.id);
                        managedUserId = pi.id;
                    } catch (Exception e) {
                        Slog.e(TAG, "Failed to decrypt child profile key", e);
                    }
                } else {
                    Slog.e(TAG, "More than one managed profile, uid1:" + managedUserId + ", uid2:" + pi.id);
                }
            }
        }
        try {
            int[] profileIdsWithDisabled = this.mUserManager.getProfileIdsWithDisabled(userId);
            int length = profileIdsWithDisabled.length;
            for (int i = FBE_ENCRYPTED_NOTIFICATION; i < length; i++) {
                int profileId = profileIdsWithDisabled[i];
                int[] iArr = SYSTEM_CREDENTIAL_UIDS;
                int length2 = iArr.length;
                for (int i2 = FBE_ENCRYPTED_NOTIFICATION; i2 < length2; i2++) {
                    this.mKeyStore.clearUid(UserHandle.getUid(profileId, iArr[i2]));
                }
            }
            if (managedUserId != -1 && managedUserDecryptedPassword != null) {
                tieProfileLockToParent(managedUserId, managedUserDecryptedPassword);
            }
        } catch (Throwable th) {
            if (!(managedUserId == -1 || managedUserDecryptedPassword == null)) {
                tieProfileLockToParent(managedUserId, managedUserDecryptedPassword);
            }
        }
    }

    public VerifyCredentialResponse checkPattern(String pattern, int userId) throws RemoteException {
        return doVerifyPattern(pattern, DEBUG, 0, userId);
    }

    public VerifyCredentialResponse verifyPattern(String pattern, long challenge, int userId) throws RemoteException {
        return doVerifyPattern(pattern, true, challenge, userId);
    }

    private VerifyCredentialResponse doVerifyPattern(String pattern, boolean hasChallenge, long challenge, int userId) throws RemoteException {
        checkPasswordReadPermission(userId);
        if (TextUtils.isEmpty(pattern)) {
            throw new IllegalArgumentException("Pattern can't be null or empty");
        }
        return doVerifyPattern(pattern, this.mStorage.readPatternHash(userId), hasChallenge, challenge, userId);
    }

    private VerifyCredentialResponse doVerifyPattern(String pattern, CredentialHash storedHash, boolean hasChallenge, long challenge, int userId) throws RemoteException {
        boolean z = storedHash != null ? storedHash.isBaseZeroPattern : DEBUG;
        if (storedHash == null || storedHash.hash.length == 0) {
            Slog.w(TAG, "no Pattern saved VerifyPattern success");
            return VerifyCredentialResponse.OK;
        }
        String patternToVerify;
        if (z) {
            patternToVerify = LockPatternUtils.patternStringToBaseZero(pattern);
        } else {
            patternToVerify = pattern;
        }
        VerifyCredentialResponse response = verifyCredential(userId, storedHash, patternToVerify, hasChallenge, challenge, new CredentialUtil() {
            public void setCredential(String pattern, String oldPattern, int userId) throws RemoteException {
                LockSettingsService.this.setLockPatternInternal(pattern, oldPattern, userId);
            }

            public byte[] toHash(String pattern, int userId) {
                return LockPatternUtils.patternToHash(LockPatternUtils.stringToPattern(pattern));
            }

            public String adjustForKeystore(String pattern) {
                return LockPatternUtils.patternStringToBaseZero(pattern);
            }
        });
        if (response.getResponseCode() == 0 && z) {
            setLockPatternInternal(pattern, patternToVerify, userId);
        }
        return response;
    }

    public VerifyCredentialResponse checkPassword(String password, int userId) throws RemoteException {
        return doVerifyPassword(password, DEBUG, 0, userId);
    }

    public VerifyCredentialResponse verifyPassword(String password, long challenge, int userId) throws RemoteException {
        return doVerifyPassword(password, true, challenge, userId);
    }

    public VerifyCredentialResponse verifyTiedProfileChallenge(String password, boolean isPattern, long challenge, int userId) throws RemoteException {
        checkPasswordReadPermission(userId);
        if (isManagedProfileWithUnifiedLock(userId)) {
            VerifyCredentialResponse parentResponse;
            int parentProfileId = this.mUserManager.getProfileParent(userId).id;
            if (isPattern) {
                parentResponse = doVerifyPattern(password, true, challenge, parentProfileId);
            } else {
                parentResponse = doVerifyPassword(password, true, challenge, parentProfileId);
            }
            if (parentResponse.getResponseCode() != 0) {
                return parentResponse;
            }
            try {
                return doVerifyPassword(getDecryptedPasswordForTiedProfile(userId), true, challenge, userId);
            } catch (Exception e) {
                Slog.e(TAG, "Failed to decrypt child profile key", e);
                throw new RemoteException("Unable to get tied profile token");
            }
        }
        throw new RemoteException("User id must be managed profile with unified lock");
    }

    private VerifyCredentialResponse doVerifyPassword(String password, boolean hasChallenge, long challenge, int userId) throws RemoteException {
        checkPasswordReadPermission(userId);
        if (TextUtils.isEmpty(password)) {
            throw new IllegalArgumentException("Password can't be null or empty");
        }
        CredentialHash storedHash = this.mStorage.readPasswordHash(userId);
        if (storedHash != null && storedHash.hash.length != 0) {
            return doVerifyPassword(password, storedHash, hasChallenge, challenge, userId);
        }
        Slog.w(TAG, "no Password saved VerifyPassword success");
        return VerifyCredentialResponse.OK;
    }

    private VerifyCredentialResponse doVerifyPassword(String password, CredentialHash storedHash, boolean hasChallenge, long challenge, int userId) throws RemoteException {
        return verifyCredential(userId, storedHash, password, hasChallenge, challenge, new CredentialUtil() {
            public void setCredential(String password, String oldPassword, int userId) throws RemoteException {
                LockSettingsService.this.setLockPasswordInternal(password, oldPassword, userId);
            }

            public byte[] toHash(String password, int userId) {
                return LockSettingsService.this.mLockPatternUtils.passwordToHash(password, userId);
            }

            public String adjustForKeystore(String password) {
                return password;
            }
        });
    }

    private VerifyCredentialResponse verifyCredential(int userId, CredentialHash storedHash, String credential, boolean hasChallenge, long challenge, CredentialUtil credentialUtil) throws RemoteException {
        if ((storedHash == null || storedHash.hash.length == 0) && TextUtils.isEmpty(credential)) {
            Slog.w(TAG, "no stored Password/Pattern, verifyCredential success");
            return VerifyCredentialResponse.OK;
        } else if (TextUtils.isEmpty(credential)) {
            Slog.w(TAG, "no entered Password/Pattern, verifyCredential ERROR");
            return VerifyCredentialResponse.ERROR;
        } else {
            VerifyCredentialResponse response;
            if (storedHash.version == 0) {
                if (!Arrays.equals(credentialUtil.toHash(credential, userId), storedHash.hash)) {
                    return VerifyCredentialResponse.ERROR;
                }
                unlockKeystore(credentialUtil.adjustForKeystore(credential), userId);
                Slog.i(TAG, "Unlocking user with fake token: " + userId);
                byte[] fakeToken = String.valueOf(userId).getBytes();
                unlockUser(userId, fakeToken, fakeToken);
                credentialUtil.setCredential(credential, null, userId);
                if (!hasChallenge) {
                    notifyActivePasswordMetricsAvailable(credential, userId);
                    return VerifyCredentialResponse.OK;
                }
            }
            boolean shouldReEnroll = DEBUG;
            GateKeeperResponse gateKeeperResponse = getGateKeeperService().verifyChallenge(userId, challenge, storedHash.hash, credential.getBytes());
            int responseCode = gateKeeperResponse.getResponseCode();
            if (responseCode == 1) {
                response = new VerifyCredentialResponse(gateKeeperResponse.getTimeout());
            } else if (responseCode == 0) {
                byte[] token = gateKeeperResponse.getPayload();
                if (token == null) {
                    Slog.e(TAG, "verifyChallenge response had no associated payload");
                    response = VerifyCredentialResponse.ERROR;
                } else {
                    shouldReEnroll = gateKeeperResponse.getShouldReEnroll();
                    response = new VerifyCredentialResponse(token);
                }
            } else {
                response = VerifyCredentialResponse.ERROR;
            }
            if (response.getResponseCode() == 0) {
                notifyActivePasswordMetricsAvailable(credential, userId);
                unlockKeystore(credential, userId);
                Slog.i(TAG, "Unlocking user " + userId + " with token length " + response.getPayload().length);
                unlockUser(userId, response.getPayload(), secretFromCredential(credential));
                if (isManagedProfileWithSeparatedLock(userId)) {
                    ((TrustManager) this.mContext.getSystemService("trust")).setDeviceLockedForUser(userId, DEBUG);
                }
                if (shouldReEnroll) {
                    credentialUtil.setCredential(credential, credential, userId);
                }
                Slog.w(TAG, "verifyCredential passed by GateKeeper");
            } else if (response.getResponseCode() == 1 && response.getTimeout() > 0) {
                requireStrongAuth(8, userId);
            }
            return response;
        }
    }

    private void notifyActivePasswordMetricsAvailable(String password, int userId) {
        this.mHandler.post(new AnonymousClass7(password, this.mLockPatternUtils.getKeyguardStoredPasswordQuality(userId), userId));
    }

    private void notifyPasswordChanged(int userId) {
        this.mHandler.post(new -void_notifyPasswordChanged_int_userId_LambdaImpl0(this, userId));
    }

    /* synthetic */ void -com_android_server_LockSettingsService_lambda$1(int userId) {
        ((DevicePolicyManager) this.mContext.getSystemService("device_policy")).reportPasswordChanged(userId);
    }

    public boolean checkVoldPassword(int userId) throws RemoteException {
        if (!this.mFirstCallToVold) {
            return DEBUG;
        }
        this.mFirstCallToVold = DEBUG;
        checkPasswordReadPermission(userId);
        IMountService service = getMountService();
        long identity = Binder.clearCallingIdentity();
        try {
            String password = service.getPassword();
            service.clearPassword();
            if (password == null) {
                return DEBUG;
            }
            try {
                if (this.mLockPatternUtils.isLockPatternEnabled(userId) && checkPattern(password, userId).getResponseCode() == 0) {
                    return true;
                }
            } catch (Exception e) {
            }
            try {
                return (this.mLockPatternUtils.isLockPasswordEnabled(userId) && checkPassword(password, userId).getResponseCode() == 0) ? true : DEBUG;
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

    private void removeKeystoreProfileKey(int targetUserId) {
        try {
            java.security.KeyStore keyStore = java.security.KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            keyStore.deleteEntry("profile_key_name_encrypt_" + targetUserId);
            keyStore.deleteEntry("profile_key_name_decrypt_" + targetUserId);
        } catch (Exception e) {
            Slog.e(TAG, "Unable to remove keystore profile key for user:" + targetUserId, e);
        }
    }

    public void registerStrongAuthTracker(IStrongAuthTracker tracker) {
        checkPasswordReadPermission(-1);
        this.mStrongAuth.registerStrongAuthTracker(tracker);
    }

    public void unregisterStrongAuthTracker(IStrongAuthTracker tracker) {
        checkPasswordReadPermission(-1);
        this.mStrongAuth.unregisterStrongAuthTracker(tracker);
    }

    public void requireStrongAuth(int strongAuthReason, int userId) {
        checkWritePermission(userId);
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

    private IMountService getMountService() {
        IBinder service = ServiceManager.getService("mount");
        if (service != null) {
            return IMountService.Stub.asInterface(service);
        }
        return null;
    }

    private synchronized IGateKeeperService getGateKeeperService() throws RemoteException {
        if (this.mGateKeeperService != null) {
            return this.mGateKeeperService;
        }
        IBinder service = ServiceManager.getService("android.service.gatekeeper.IGateKeeperService");
        if (service != null) {
            service.linkToDeath(new GateKeeperDiedRecipient(), FBE_ENCRYPTED_NOTIFICATION);
            this.mGateKeeperService = IGateKeeperService.Stub.asInterface(service);
            return this.mGateKeeperService;
        }
        Slog.e(TAG, "Unable to acquire GateKeeperService");
        return null;
    }
}
