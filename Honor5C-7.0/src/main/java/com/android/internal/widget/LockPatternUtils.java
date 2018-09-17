package com.android.internal.widget;

import android.app.admin.DevicePolicyManager;
import android.app.trust.IStrongAuthTracker;
import android.app.trust.TrustManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.UserInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserManager;
import android.os.storage.IMountService;
import android.os.storage.IMountService.Stub;
import android.os.storage.StorageManager;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.PtmLog;
import android.util.SparseIntArray;
import com.android.internal.R;
import com.android.internal.util.Protocol;
import com.android.internal.widget.LockPatternView.Cell;
import com.google.android.collect.Lists;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import libcore.util.HexEncoding;

public class LockPatternUtils {
    @Deprecated
    public static final String BIOMETRIC_WEAK_EVER_CHOSEN_KEY = "lockscreen.biometricweakeverchosen";
    private static final boolean DEBUG = false;
    public static final String DISABLE_LOCKSCREEN_KEY = "lockscreen.disabled";
    private static final String ENABLED_TRUST_AGENTS = "lockscreen.enabledtrustagents";
    public static final int FAILED_ATTEMPTS_BEFORE_RESET = 20;
    public static final int FAILED_ATTEMPTS_BEFORE_WIPE_GRACE = 5;
    public static final long FAILED_ATTEMPT_COUNTDOWN_INTERVAL_MS = 1000;
    private static final String IS_TRUST_USUALLY_MANAGED = "lockscreen.istrustusuallymanaged";
    public static final String LEGACY_LOCK_PATTERN_ENABLED = "legacy_lock_pattern_enabled";
    public static final String LOCKOUT_ATTEMPT_DEADLINE = "lockscreen.lockoutattemptdeadline";
    public static final String LOCKOUT_ATTEMPT_TIMEOUT_MS = "lockscreen.lockoutattempttimeoutmss";
    @Deprecated
    public static final String LOCKOUT_PERMANENT_KEY = "lockscreen.lockedoutpermanently";
    @Deprecated
    public static final String LOCKSCREEN_BIOMETRIC_WEAK_FALLBACK = "lockscreen.biometric_weak_fallback";
    public static final String LOCKSCREEN_OPTIONS = "lockscreen.options";
    public static final String LOCKSCREEN_POWER_BUTTON_INSTANTLY_LOCKS = "lockscreen.power_button_instantly_locks";
    @Deprecated
    public static final String LOCKSCREEN_WIDGETS_ENABLED = "lockscreen.widgets_enabled";
    public static final String LOCK_PASSWORD_SALT_KEY = "lockscreen.password_salt";
    private static final String LOCK_SCREEN_DEVICE_OWNER_INFO = "lockscreen.device_owner_info";
    private static final String LOCK_SCREEN_OWNER_INFO = "lock_screen_owner_info";
    private static final String LOCK_SCREEN_OWNER_INFO_ENABLED = "lock_screen_owner_info_enabled";
    public static final int MAX_ALLOWED_SEQUENCE = 3;
    public static final int MIN_LOCK_PASSWORD_SIZE = 4;
    public static final int MIN_LOCK_PATTERN_SIZE = 4;
    public static final int MIN_PATTERN_REGISTER_FAIL = 4;
    public static final String PASSWORD_HISTORY_KEY = "lockscreen.passwordhistory";
    @Deprecated
    public static final String PASSWORD_TYPE_ALTERNATE_KEY = "lockscreen.password_type_alternate";
    public static final String PASSWORD_TYPE_KEY = "lockscreen.password_type";
    public static final String PATTERN_EVER_CHOSEN_KEY = "lockscreen.patterneverchosen";
    public static final String PROFILE_KEY_NAME_DECRYPT = "profile_key_name_decrypt_";
    public static final String PROFILE_KEY_NAME_ENCRYPT = "profile_key_name_encrypt_";
    private static final String TAG = "LockPatternUtils";
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private DevicePolicyManager mDevicePolicyManager;
    private ILockSettings mLockSettingsService;
    private UserManager mUserManager;

    /* renamed from: com.android.internal.widget.LockPatternUtils.1 */
    class AnonymousClass1 extends AsyncTask<Void, Void, Void> {
        final /* synthetic */ String val$password;
        final /* synthetic */ IBinder val$service;
        final /* synthetic */ int val$type;

        AnonymousClass1(IBinder val$service, int val$type, String val$password) {
            this.val$service = val$service;
            this.val$type = val$type;
            this.val$password = val$password;
        }

        protected Void doInBackground(Void... dummy) {
            try {
                Stub.asInterface(this.val$service).changeEncryptionPassword(this.val$type, this.val$password);
            } catch (RemoteException e) {
                Log.e(LockPatternUtils.TAG, "Error changing encryption password", e);
            }
            return null;
        }
    }

    public static final class RequestThrottledException extends Exception {
        private int mTimeoutMs;

        public RequestThrottledException(int timeoutMs) {
            this.mTimeoutMs = timeoutMs;
        }

        public int getTimeoutMs() {
            return this.mTimeoutMs;
        }
    }

    public static class StrongAuthTracker {
        private static final int ALLOWING_FINGERPRINT = 20;
        public static final int SOME_AUTH_REQUIRED_AFTER_USER_REQUEST = 4;
        public static final int SOME_AUTH_REQUIRED_AFTER_WRONG_CREDENTIAL = 16;
        public static final int STRONG_AUTH_NOT_REQUIRED = 0;
        public static final int STRONG_AUTH_REQUIRED_AFTER_BOOT = 1;
        public static final int STRONG_AUTH_REQUIRED_AFTER_DPM_LOCK_NOW = 2;
        public static final int STRONG_AUTH_REQUIRED_AFTER_LOCKOUT = 8;
        private final int mDefaultStrongAuthFlags;
        private final H mHandler;
        private final SparseIntArray mStrongAuthRequiredForUser;
        protected final IStrongAuthTracker.Stub mStub;

        private class H extends Handler {
            static final int MSG_ON_STRONG_AUTH_REQUIRED_CHANGED = 1;

            public H(Looper looper) {
                super(looper);
            }

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_ON_STRONG_AUTH_REQUIRED_CHANGED /*1*/:
                        StrongAuthTracker.this.handleStrongAuthRequiredChanged(msg.arg1, msg.arg2);
                    default:
                }
            }
        }

        public StrongAuthTracker(Context context) {
            this(context, Looper.myLooper());
        }

        public StrongAuthTracker(Context context, Looper looper) {
            this.mStrongAuthRequiredForUser = new SparseIntArray();
            this.mStub = new IStrongAuthTracker.Stub() {
                public void onStrongAuthRequiredChanged(int strongAuthFlags, int userId) {
                    StrongAuthTracker.this.mHandler.obtainMessage(StrongAuthTracker.STRONG_AUTH_REQUIRED_AFTER_BOOT, strongAuthFlags, userId).sendToTarget();
                }
            };
            this.mHandler = new H(looper);
            this.mDefaultStrongAuthFlags = getDefaultFlags(context);
        }

        public static int getDefaultFlags(Context context) {
            return context.getResources().getBoolean(R.bool.config_strongAuthRequiredOnBoot) ? STRONG_AUTH_REQUIRED_AFTER_BOOT : STRONG_AUTH_NOT_REQUIRED;
        }

        public int getStrongAuthForUser(int userId) {
            return this.mStrongAuthRequiredForUser.get(userId, this.mDefaultStrongAuthFlags);
        }

        public boolean isTrustAllowedForUser(int userId) {
            return getStrongAuthForUser(userId) == 0 ? true : LockPatternUtils.DEBUG;
        }

        public boolean isFingerprintAllowedForUser(int userId) {
            return (getStrongAuthForUser(userId) & -21) == 0 ? true : LockPatternUtils.DEBUG;
        }

        public void onStrongAuthRequiredChanged(int userId) {
        }

        protected void handleStrongAuthRequiredChanged(int strongAuthFlags, int userId) {
            if (strongAuthFlags != getStrongAuthForUser(userId)) {
                if (strongAuthFlags == this.mDefaultStrongAuthFlags) {
                    this.mStrongAuthRequiredForUser.delete(userId);
                } else {
                    this.mStrongAuthRequiredForUser.put(userId, strongAuthFlags);
                }
                onStrongAuthRequiredChanged(userId);
            }
        }
    }

    public boolean isTrustUsuallyManaged(int userId) {
        if (this.mLockSettingsService instanceof ILockSettings.Stub) {
            try {
                return getLockSettings().getBoolean(IS_TRUST_USUALLY_MANAGED, DEBUG, userId);
            } catch (RemoteException e) {
                return DEBUG;
            }
        }
        throw new IllegalStateException("May only be called by TrustManagerService. Use TrustManager.isTrustUsuallyManaged()");
    }

    public void setTrustUsuallyManaged(boolean managed, int userId) {
        try {
            getLockSettings().setBoolean(IS_TRUST_USUALLY_MANAGED, managed, userId);
        } catch (RemoteException e) {
        }
    }

    public void userPresent(int userId) {
        try {
            getLockSettings().userPresent(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public DevicePolicyManager getDevicePolicyManager() {
        if (this.mDevicePolicyManager == null) {
            this.mDevicePolicyManager = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
            if (this.mDevicePolicyManager == null) {
                Log.e(TAG, "Can't get DevicePolicyManagerService: is it running?", new IllegalStateException("Stack trace:"));
            }
        }
        return this.mDevicePolicyManager;
    }

    private UserManager getUserManager() {
        if (this.mUserManager == null) {
            this.mUserManager = UserManager.get(this.mContext);
        }
        return this.mUserManager;
    }

    private TrustManager getTrustManager() {
        TrustManager trust = (TrustManager) this.mContext.getSystemService("trust");
        if (trust == null) {
            Log.e(TAG, "Can't get TrustManagerService: is it running?", new IllegalStateException("Stack trace:"));
        }
        return trust;
    }

    public LockPatternUtils(Context context) {
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
    }

    protected ILockSettings getLockSettings() {
        if (this.mLockSettingsService == null) {
            this.mLockSettingsService = ILockSettings.Stub.asInterface(ServiceManager.getService("lock_settings"));
        }
        return this.mLockSettingsService;
    }

    public int getRequestedMinimumPasswordLength(int userId) {
        return getDevicePolicyManager().getPasswordMinimumLength(null, userId);
    }

    public int getRequestedPasswordQuality(int userId) {
        return getDevicePolicyManager().getPasswordQuality(null, userId);
    }

    public int getRequestedPasswordHistoryLength(int userId) {
        return getDevicePolicyManager().getPasswordHistoryLength(null, userId);
    }

    public int getRequestedPasswordMinimumLetters(int userId) {
        return getDevicePolicyManager().getPasswordMinimumLetters(null, userId);
    }

    public int getRequestedPasswordMinimumUpperCase(int userId) {
        return getDevicePolicyManager().getPasswordMinimumUpperCase(null, userId);
    }

    public int getRequestedPasswordMinimumLowerCase(int userId) {
        return getDevicePolicyManager().getPasswordMinimumLowerCase(null, userId);
    }

    public int getRequestedPasswordMinimumNumeric(int userId) {
        return getDevicePolicyManager().getPasswordMinimumNumeric(null, userId);
    }

    public int getRequestedPasswordMinimumSymbols(int userId) {
        return getDevicePolicyManager().getPasswordMinimumSymbols(null, userId);
    }

    public int getRequestedPasswordMinimumNonLetter(int userId) {
        return getDevicePolicyManager().getPasswordMinimumNonLetter(null, userId);
    }

    public void reportFailedPasswordAttempt(int userId) {
        getDevicePolicyManager().reportFailedPasswordAttempt(userId);
        getTrustManager().reportUnlockAttempt(DEBUG, userId);
        requireStrongAuth(16, userId);
    }

    public void reportSuccessfulPasswordAttempt(int userId) {
        getDevicePolicyManager().reportSuccessfulPasswordAttempt(userId);
        getTrustManager().reportUnlockAttempt(true, userId);
    }

    public int getCurrentFailedPasswordAttempts(int userId) {
        return getDevicePolicyManager().getCurrentFailedPasswordAttempts(userId);
    }

    public int getMaximumFailedPasswordsForWipe(int userId) {
        return getDevicePolicyManager().getMaximumFailedPasswordsForWipe(null, userId);
    }

    public byte[] verifyPattern(List<Cell> pattern, long challenge, int userId) throws RequestThrottledException {
        throwIfCalledOnMainThread();
        try {
            VerifyCredentialResponse response = getLockSettings().verifyPattern(patternToString(pattern), challenge, userId);
            if (response == null) {
                return null;
            }
            if (response.getResponseCode() == 0) {
                return response.getPayload();
            }
            if (response.getResponseCode() != 1) {
                return null;
            }
            throw new RequestThrottledException(response.getTimeout());
        } catch (RemoteException e) {
            return null;
        }
    }

    public boolean checkPattern(List<Cell> pattern, int userId) throws RequestThrottledException {
        throwIfCalledOnMainThread();
        try {
            VerifyCredentialResponse response = getLockSettings().checkPattern(patternToString(pattern), userId);
            if (response.getResponseCode() == 0) {
                return true;
            }
            if (response.getResponseCode() != 1) {
                return DEBUG;
            }
            throw new RequestThrottledException(response.getTimeout());
        } catch (RemoteException e) {
            return DEBUG;
        }
    }

    public byte[] verifyPassword(String password, long challenge, int userId) throws RequestThrottledException {
        throwIfCalledOnMainThread();
        try {
            VerifyCredentialResponse response = getLockSettings().verifyPassword(password, challenge, userId);
            if (response.getResponseCode() == 0) {
                return response.getPayload();
            }
            if (response.getResponseCode() != 1) {
                return null;
            }
            throw new RequestThrottledException(response.getTimeout());
        } catch (RemoteException e) {
            return null;
        }
    }

    public byte[] verifyTiedProfileChallenge(String password, boolean isPattern, long challenge, int userId) throws RequestThrottledException {
        throwIfCalledOnMainThread();
        try {
            VerifyCredentialResponse response = getLockSettings().verifyTiedProfileChallenge(password, isPattern, challenge, userId);
            if (response.getResponseCode() == 0) {
                return response.getPayload();
            }
            if (response.getResponseCode() != 1) {
                return null;
            }
            throw new RequestThrottledException(response.getTimeout());
        } catch (RemoteException e) {
            return null;
        }
    }

    public boolean checkPassword(String password, int userId) throws RequestThrottledException {
        throwIfCalledOnMainThread();
        if (password == null || password.equals("")) {
            return DEBUG;
        }
        try {
            VerifyCredentialResponse response = getLockSettings().checkPassword(password, userId);
            if (response.getResponseCode() == 0) {
                return true;
            }
            if (response.getResponseCode() != 1) {
                return DEBUG;
            }
            throw new RequestThrottledException(response.getTimeout());
        } catch (RemoteException re) {
            Log.e(TAG, "checkPassword error  " + re.toString());
            return DEBUG;
        }
    }

    public boolean checkVoldPassword(int userId) {
        try {
            return getLockSettings().checkVoldPassword(userId);
        } catch (RemoteException e) {
            return DEBUG;
        }
    }

    public boolean checkPasswordHistory(String password, int userId) {
        String passwordHashString = new String(passwordToHash(password, userId), StandardCharsets.UTF_8);
        String passwordHistory = getString(PASSWORD_HISTORY_KEY, userId);
        if (passwordHistory == null) {
            return DEBUG;
        }
        int passwordHashLength = passwordHashString.length();
        int passwordHistoryLength = getRequestedPasswordHistoryLength(userId);
        if (passwordHistoryLength == 0) {
            return DEBUG;
        }
        int neededPasswordHistoryLength = ((passwordHashLength * passwordHistoryLength) + passwordHistoryLength) - 1;
        if (passwordHistory.length() > neededPasswordHistoryLength) {
            passwordHistory = passwordHistory.substring(0, neededPasswordHistoryLength);
        }
        return passwordHistory.contains(passwordHashString);
    }

    private boolean savedPatternExists(int userId) {
        try {
            return getLockSettings().havePattern(userId);
        } catch (RemoteException e) {
            return DEBUG;
        }
    }

    private boolean savedPasswordExists(int userId) {
        try {
            return getLockSettings().havePassword(userId);
        } catch (RemoteException e) {
            return DEBUG;
        }
    }

    public boolean isPatternEverChosen(int userId) {
        return getBoolean(PATTERN_EVER_CHOSEN_KEY, DEBUG, userId);
    }

    public int getActivePasswordQuality(int userId) {
        int quality = getKeyguardStoredPasswordQuality(userId);
        if (isLockPasswordEnabled(quality, userId) || isLockPatternEnabled(quality, userId)) {
            return quality;
        }
        return 0;
    }

    public void resetKeyStore(int userId) {
        try {
            getLockSettings().resetKeyStore(userId);
        } catch (RemoteException e) {
            Log.e(TAG, "Couldn't reset keystore " + e);
        }
    }

    public void clearLock(int userHandle) {
        setLong(PASSWORD_TYPE_KEY, 0, userHandle);
        try {
            getLockSettings().setLockPassword(null, null, userHandle);
            getLockSettings().setLockPattern(null, null, userHandle);
        } catch (RemoteException e) {
        }
        Log.i(TAG, "clearLock success by uid = " + Binder.getCallingUid());
        if (userHandle == 0) {
            updateEncryptionPassword(1, null);
            setCredentialRequiredToDecrypt(DEBUG);
        }
        onAfterChangingPassword(userHandle);
    }

    public void setLockScreenDisabled(boolean disable, int userId) {
        setBoolean(DISABLE_LOCKSCREEN_KEY, disable, userId);
    }

    public boolean isLockScreenDisabled(int userId) {
        if (isSecure(userId)) {
            return DEBUG;
        }
        return getBoolean(DISABLE_LOCKSCREEN_KEY, DEBUG, userId);
    }

    public void saveLockPattern(List<Cell> pattern, int userId) {
        saveLockPattern(pattern, null, userId);
    }

    public void saveLockPattern(List<Cell> pattern, String savedPattern, int userId) {
        if (pattern != null) {
            try {
                if (pattern.size() >= MIN_PATTERN_REGISTER_FAIL) {
                    setLong(PASSWORD_TYPE_KEY, 65536, userId);
                    getLockSettings().setLockPattern(patternToString(pattern), savedPattern, userId);
                    DevicePolicyManager dpm = getDevicePolicyManager();
                    if (userId == 0 && isDeviceEncryptionEnabled()) {
                        if (shouldEncryptWithCredentials(true)) {
                            updateEncryptionPassword(2, patternToString(pattern));
                        } else {
                            clearEncryptionPassword();
                        }
                    }
                    setBoolean(PATTERN_EVER_CHOSEN_KEY, true, userId);
                    onAfterChangingPassword(userId);
                    Log.i(TAG, "saveLockPattern success by uid = " + Binder.getCallingUid());
                    return;
                }
            } catch (RemoteException re) {
                Log.e(TAG, "Couldn't save lock pattern " + re);
            }
        }
        throw new IllegalArgumentException("pattern must not be null and at least 4 dots long.");
    }

    private void updateCryptoUserInfo(int userId) {
        if (userId == 0) {
            String ownerInfo = isOwnerInfoEnabled(userId) ? getOwnerInfo(userId) : "";
            IBinder service = ServiceManager.getService("mount");
            if (service == null) {
                Log.e(TAG, "Could not find the mount service to update the user info");
                return;
            }
            IMountService mountService = Stub.asInterface(service);
            try {
                Log.d(TAG, "Setting owner info");
                mountService.setField("OwnerInfo", ownerInfo);
            } catch (RemoteException e) {
                Log.e(TAG, "Error changing user info", e);
            }
        }
    }

    public void setOwnerInfo(String info, int userId) {
        setString(LOCK_SCREEN_OWNER_INFO, info, userId);
        updateCryptoUserInfo(userId);
    }

    public void setOwnerInfoEnabled(boolean enabled, int userId) {
        setBoolean(LOCK_SCREEN_OWNER_INFO_ENABLED, enabled, userId);
        updateCryptoUserInfo(userId);
    }

    public String getOwnerInfo(int userId) {
        return getString(LOCK_SCREEN_OWNER_INFO, userId);
    }

    public boolean isOwnerInfoEnabled(int userId) {
        return getBoolean(LOCK_SCREEN_OWNER_INFO_ENABLED, DEBUG, userId);
    }

    public void setDeviceOwnerInfo(String info) {
        if (info != null && info.isEmpty()) {
            info = null;
        }
        setString(LOCK_SCREEN_DEVICE_OWNER_INFO, info, 0);
    }

    public String getDeviceOwnerInfo() {
        return getString(LOCK_SCREEN_DEVICE_OWNER_INFO, 0);
    }

    public boolean isDeviceOwnerInfoEnabled() {
        return getDeviceOwnerInfo() != null ? true : DEBUG;
    }

    public static int computePasswordQuality(String password) {
        boolean hasDigit = DEBUG;
        boolean hasNonDigit = DEBUG;
        int len = password.length();
        for (int i = 0; i < len; i++) {
            if (Character.isDigit(password.charAt(i))) {
                hasDigit = true;
            } else {
                hasNonDigit = true;
            }
        }
        if (hasNonDigit && hasDigit) {
            return Protocol.BASE_TETHERING;
        }
        if (hasNonDigit) {
            return Protocol.BASE_DATA_CONNECTION;
        }
        if (!hasDigit) {
            return 0;
        }
        int i2;
        if (maxLengthSequence(password) > MAX_ALLOWED_SEQUENCE) {
            i2 = Protocol.BASE_WIFI;
        } else {
            i2 = Protocol.BASE_DHCP;
        }
        return i2;
    }

    private static int categoryChar(char c) {
        if (DateFormat.AM_PM <= c && c <= DateFormat.TIME_ZONE) {
            return 0;
        }
        if (DateFormat.CAPITAL_AM_PM <= c && c <= 'Z') {
            return 1;
        }
        if ('0' > c || c > '9') {
            return MAX_ALLOWED_SEQUENCE;
        }
        return 2;
    }

    private static int maxDiffCategory(int category) {
        if (category == 0 || category == 1) {
            return 1;
        }
        if (category == 2) {
            return 10;
        }
        return 0;
    }

    public static int maxLengthSequence(String string) {
        if (string.length() == 0) {
            return 0;
        }
        char previousChar = string.charAt(0);
        int category = categoryChar(previousChar);
        int diff = 0;
        boolean hasDiff = DEBUG;
        int maxLength = 0;
        int startSequence = 0;
        for (int current = 1; current < string.length(); current++) {
            char currentChar = string.charAt(current);
            int categoryCurrent = categoryChar(currentChar);
            int currentDiff = currentChar - previousChar;
            if (categoryCurrent != category || Math.abs(currentDiff) > maxDiffCategory(category)) {
                maxLength = Math.max(maxLength, current - startSequence);
                startSequence = current;
                hasDiff = DEBUG;
                category = categoryCurrent;
            } else {
                if (hasDiff && currentDiff != diff) {
                    maxLength = Math.max(maxLength, current - startSequence);
                    startSequence = current - 1;
                }
                diff = currentDiff;
                hasDiff = true;
            }
            previousChar = currentChar;
        }
        return Math.max(maxLength, string.length() - startSequence);
    }

    protected void updateEncryptionPassword(int type, String password) {
        if (isDeviceEncryptionEnabled()) {
            IBinder service = ServiceManager.getService("mount");
            if (service == null) {
                Log.e(TAG, "Could not find the mount service to update the encryption password");
            } else {
                new AnonymousClass1(service, type, password).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
            }
        }
    }

    public void saveLockPassword(String password, String savedPassword, int quality, int userHandle) {
        try {
            DevicePolicyManager dpm = getDevicePolicyManager();
            if (password == null || password.length() < MIN_PATTERN_REGISTER_FAIL) {
                throw new IllegalArgumentException("password must not be null and at least of length 4");
            }
            int computedQuality = computePasswordQuality(password);
            setLong(PASSWORD_TYPE_KEY, (long) Math.max(quality, computedQuality), userHandle);
            getLockSettings().setLockPassword(password, savedPassword, userHandle);
            writeSettingsData();
            if (userHandle == 0 && isDeviceEncryptionEnabled()) {
                if (shouldEncryptWithCredentials(true)) {
                    int type;
                    boolean numeric = computedQuality == Protocol.BASE_WIFI ? true : DEBUG;
                    boolean numericComplex = computedQuality == Protocol.BASE_DHCP ? true : DEBUG;
                    if (numeric || numericComplex) {
                        type = MAX_ALLOWED_SEQUENCE;
                    } else {
                        type = 0;
                    }
                    updateEncryptionPassword(type, password);
                } else {
                    clearEncryptionPassword();
                }
            }
            String passwordHistory = getString(PASSWORD_HISTORY_KEY, userHandle);
            if (passwordHistory == null) {
                passwordHistory = "";
            }
            int passwordHistoryLength = getRequestedPasswordHistoryLength(userHandle);
            if (passwordHistoryLength == 0) {
                passwordHistory = "";
            } else {
                byte[] hash = passwordToHash(password, userHandle);
                passwordHistory = new String(hash, StandardCharsets.UTF_8) + PtmLog.PAIRE_DELIMETER + passwordHistory;
                passwordHistory = passwordHistory.substring(0, Math.min(((hash.length * passwordHistoryLength) + passwordHistoryLength) - 1, passwordHistory.length()));
            }
            setString(PASSWORD_HISTORY_KEY, passwordHistory, userHandle);
            onAfterChangingPassword(userHandle);
            Log.i(TAG, "saveLockPattern success by uid = " + Binder.getCallingUid());
        } catch (RemoteException re) {
            Log.e(TAG, "Unable to save lock password " + re);
        }
    }

    private void writeSettingsData() {
        try {
            String subjectDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(new Date(System.currentTimeMillis()));
            if (TextUtils.isEmpty(System.getString(this.mContext.getContentResolver(), "lock_settings_time"))) {
                System.putString(this.mContext.getContentResolver(), "first_lock_time", subjectDate);
            }
            System.putString(this.mContext.getContentResolver(), "lock_settings_time", subjectDate);
        } catch (Exception e) {
            Log.e(TAG, "failed to write settings", e);
        }
    }

    public static boolean isDeviceEncryptionEnabled() {
        return StorageManager.isEncrypted();
    }

    public static boolean isFileEncryptionEnabled() {
        return StorageManager.isFileEncryptedNativeOrEmulated();
    }

    public void clearEncryptionPassword() {
        updateEncryptionPassword(1, null);
    }

    public int getKeyguardStoredPasswordQuality(int userHandle) {
        return (int) getLong(PASSWORD_TYPE_KEY, 0, userHandle);
    }

    public void setSeparateProfileChallengeEnabled(int userHandle, boolean enabled, String managedUserPassword) {
        if (getUserManager().getUserInfo(userHandle).isManagedProfile()) {
            try {
                getLockSettings().setSeparateProfileChallengeEnabled(userHandle, enabled, managedUserPassword);
                onAfterChangingPassword(userHandle);
            } catch (RemoteException e) {
                Log.e(TAG, "Couldn't update work profile challenge enabled");
            }
        }
    }

    public boolean isSeparateProfileChallengeEnabled(int userHandle) {
        UserInfo info = getUserManager().getUserInfo(userHandle);
        if (info == null || !info.isManagedProfile()) {
            return DEBUG;
        }
        try {
            return getLockSettings().getSeparateProfileChallengeEnabled(userHandle);
        } catch (RemoteException e) {
            Log.e(TAG, "Couldn't get separate profile challenge enabled");
            return DEBUG;
        }
    }

    public boolean isSeparateProfileChallengeAllowed(int userHandle) {
        UserInfo info = getUserManager().getUserInfo(userHandle);
        if (info == null || !info.isManagedProfile()) {
            return DEBUG;
        }
        return getDevicePolicyManager().isSeparateProfileChallengeAllowed(userHandle);
    }

    public boolean isSeparateProfileChallengeAllowedToUnify(int userHandle) {
        return getDevicePolicyManager().isProfileActivePasswordSufficientForParent(userHandle);
    }

    public static List<Cell> stringToPattern(String string) {
        if (string == null) {
            return null;
        }
        List<Cell> result = Lists.newArrayList();
        byte[] bytes = string.getBytes();
        for (byte b : bytes) {
            byte b2 = (byte) (b - 49);
            result.add(Cell.of(b2 / MAX_ALLOWED_SEQUENCE, b2 % MAX_ALLOWED_SEQUENCE));
        }
        return result;
    }

    public static String patternToString(List<Cell> pattern) {
        if (pattern == null) {
            return "";
        }
        int patternSize = pattern.size();
        byte[] res = new byte[patternSize];
        for (int i = 0; i < patternSize; i++) {
            Cell cell = (Cell) pattern.get(i);
            res[i] = (byte) (((cell.getRow() * MAX_ALLOWED_SEQUENCE) + cell.getColumn()) + 49);
        }
        return new String(res);
    }

    public static String patternStringToBaseZero(String pattern) {
        if (pattern == null) {
            return "";
        }
        int patternSize = pattern.length();
        byte[] res = new byte[patternSize];
        byte[] bytes = pattern.getBytes();
        for (int i = 0; i < patternSize; i++) {
            res[i] = (byte) (bytes[i] - 49);
        }
        return new String(res);
    }

    public static byte[] patternToHash(List<Cell> pattern) {
        if (pattern == null) {
            return null;
        }
        int patternSize = pattern.size();
        byte[] res = new byte[patternSize];
        for (int i = 0; i < patternSize; i++) {
            Cell cell = (Cell) pattern.get(i);
            res[i] = (byte) ((cell.getRow() * MAX_ALLOWED_SEQUENCE) + cell.getColumn());
        }
        try {
            return MessageDigest.getInstance("SHA-1").digest(res);
        } catch (NoSuchAlgorithmException e) {
            return res;
        }
    }

    private String getSalt(int userId) {
        long salt = getLong(LOCK_PASSWORD_SALT_KEY, 0, userId);
        if (salt == 0) {
            try {
                salt = SecureRandom.getInstance("SHA1PRNG").nextLong();
                setLong(LOCK_PASSWORD_SALT_KEY, salt, userId);
                Log.v(TAG, "Initialized lock password salt for user: " + userId);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("Couldn't get SecureRandom number", e);
            }
        }
        return Long.toHexString(salt);
    }

    public byte[] passwordToHash(String password, int userId) {
        if (password == null) {
            return null;
        }
        try {
            byte[] saltedPassword = (password + getSalt(userId)).getBytes();
            byte[] sha1 = MessageDigest.getInstance("SHA-1").digest(saltedPassword);
            byte[] md5 = MessageDigest.getInstance("MD5").digest(saltedPassword);
            byte[] combined = new byte[(sha1.length + md5.length)];
            System.arraycopy(sha1, 0, combined, 0, sha1.length);
            System.arraycopy(md5, 0, combined, sha1.length, md5.length);
            return new String(HexEncoding.encode(combined)).getBytes(StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError("Missing digest algorithm: ", e);
        }
    }

    public boolean isSecure(int userId) {
        int mode = getKeyguardStoredPasswordQuality(userId);
        return !isLockPatternEnabled(mode, userId) ? isLockPasswordEnabled(mode, userId) : true;
    }

    public boolean isLockPasswordEnabled(int userId) {
        return isLockPasswordEnabled(getKeyguardStoredPasswordQuality(userId), userId);
    }

    private boolean isLockPasswordEnabled(int mode, int userId) {
        boolean passwordEnabled = true;
        if (!(mode == Protocol.BASE_DATA_CONNECTION || mode == Protocol.BASE_WIFI || mode == Protocol.BASE_DHCP || mode == Protocol.BASE_TETHERING || mode == Protocol.BASE_NSD_MANAGER || mode == Protocol.BASE_CONNECTIVITY_MANAGER)) {
            passwordEnabled = DEBUG;
        }
        if (passwordEnabled) {
            return savedPasswordExists(userId);
        }
        return DEBUG;
    }

    public boolean isLockPatternEnabled(int userId) {
        return isLockPatternEnabled(getKeyguardStoredPasswordQuality(userId), userId);
    }

    @Deprecated
    public boolean isLegacyLockPatternEnabled(int userId) {
        return getBoolean(LEGACY_LOCK_PATTERN_ENABLED, true, userId);
    }

    @Deprecated
    public void setLegacyLockPatternEnabled(int userId) {
        setBoolean("lock_pattern_autolock", true, userId);
    }

    private boolean isLockPatternEnabled(int mode, int userId) {
        if (mode == Protocol.BASE_SYSTEM_RESERVED) {
            return savedPatternExists(userId);
        }
        return DEBUG;
    }

    public boolean isVisiblePatternEnabled(int userId) {
        return getBoolean("lock_pattern_visible_pattern", DEBUG, userId);
    }

    public void setVisiblePatternEnabled(boolean enabled, int userId) {
        setBoolean("lock_pattern_visible_pattern", enabled, userId);
        if (userId == 0) {
            IBinder service = ServiceManager.getService("mount");
            if (service == null) {
                Log.e(TAG, "Could not find the mount service to update the user info");
                return;
            }
            IMountService mountService = Stub.asInterface(service);
            try {
                String str;
                String str2 = "PatternVisible";
                if (enabled) {
                    str = "1";
                } else {
                    str = "0";
                }
                mountService.setField(str2, str);
            } catch (RemoteException e) {
                Log.e(TAG, "Error changing pattern visible state", e);
            }
        }
    }

    public void setVisiblePasswordEnabled(boolean enabled, int userId) {
        if (userId == 0) {
            IBinder service = ServiceManager.getService("mount");
            if (service == null) {
                Log.e(TAG, "Could not find the mount service to update the user info");
                return;
            }
            IMountService mountService = Stub.asInterface(service);
            try {
                String str;
                String str2 = "PasswordVisible";
                if (enabled) {
                    str = "1";
                } else {
                    str = "0";
                }
                mountService.setField(str2, str);
            } catch (RemoteException e) {
                Log.e(TAG, "Error changing password visible state", e);
            }
        }
    }

    public boolean isTactileFeedbackEnabled() {
        return System.getIntForUser(this.mContentResolver, "haptic_feedback_enabled", 1, -2) != 0 ? true : DEBUG;
    }

    public long setLockoutAttemptDeadline(int userId, int timeoutMs) {
        long deadline = SystemClock.elapsedRealtime() + ((long) timeoutMs);
        setLong(LOCKOUT_ATTEMPT_DEADLINE, deadline, userId);
        setLong(LOCKOUT_ATTEMPT_TIMEOUT_MS, (long) timeoutMs, userId);
        return deadline;
    }

    public long getLockoutAttemptDeadline(int userId) {
        long deadline = getLong(LOCKOUT_ATTEMPT_DEADLINE, 0, userId);
        long timeoutMs = getLong(LOCKOUT_ATTEMPT_TIMEOUT_MS, 0, userId);
        long now = SystemClock.elapsedRealtime();
        if (deadline >= now || deadline == 0) {
            if (deadline > now + timeoutMs) {
                deadline = now + timeoutMs;
                setLong(LOCKOUT_ATTEMPT_DEADLINE, deadline, userId);
            }
            return deadline;
        }
        setLong(LOCKOUT_ATTEMPT_DEADLINE, 0, userId);
        setLong(LOCKOUT_ATTEMPT_TIMEOUT_MS, 0, userId);
        return 0;
    }

    private boolean getBoolean(String secureSettingKey, boolean defaultValue, int userId) {
        try {
            return getLockSettings().getBoolean(secureSettingKey, defaultValue, userId);
        } catch (RemoteException e) {
            return defaultValue;
        }
    }

    private void setBoolean(String secureSettingKey, boolean enabled, int userId) {
        try {
            getLockSettings().setBoolean(secureSettingKey, enabled, userId);
        } catch (RemoteException re) {
            Log.e(TAG, "Couldn't write boolean " + secureSettingKey + re);
        }
    }

    private long getLong(String secureSettingKey, long defaultValue, int userHandle) {
        try {
            return getLockSettings().getLong(secureSettingKey, defaultValue, userHandle);
        } catch (RemoteException e) {
            return defaultValue;
        }
    }

    protected void setLong(String secureSettingKey, long value, int userHandle) {
        try {
            getLockSettings().setLong(secureSettingKey, value, userHandle);
        } catch (RemoteException re) {
            Log.e(TAG, "Couldn't write long " + secureSettingKey + re);
        }
    }

    protected String getString(String secureSettingKey, int userHandle) {
        try {
            return getLockSettings().getString(secureSettingKey, null, userHandle);
        } catch (RemoteException e) {
            return null;
        }
    }

    protected void setString(String secureSettingKey, String value, int userHandle) {
        try {
            getLockSettings().setString(secureSettingKey, value, userHandle);
        } catch (RemoteException re) {
            Log.e(TAG, "Couldn't write string " + secureSettingKey + re);
        }
    }

    public void setPowerButtonInstantlyLocks(boolean enabled, int userId) {
        setBoolean(LOCKSCREEN_POWER_BUTTON_INSTANTLY_LOCKS, enabled, userId);
    }

    public boolean getPowerButtonInstantlyLocks(int userId) {
        return getBoolean(LOCKSCREEN_POWER_BUTTON_INSTANTLY_LOCKS, true, userId);
    }

    public void setEnabledTrustAgents(Collection<ComponentName> activeTrustAgents, int userId) {
        StringBuilder sb = new StringBuilder();
        for (ComponentName cn : activeTrustAgents) {
            if (sb.length() > 0) {
                sb.append(PhoneNumberUtils.PAUSE);
            }
            sb.append(cn.flattenToShortString());
        }
        setString(ENABLED_TRUST_AGENTS, sb.toString(), userId);
        getTrustManager().reportEnabledTrustAgentsChanged(userId);
    }

    public List<ComponentName> getEnabledTrustAgents(int userId) {
        String serialized = getString(ENABLED_TRUST_AGENTS, userId);
        if (TextUtils.isEmpty(serialized)) {
            return null;
        }
        String[] split = serialized.split(PtmLog.PAIRE_DELIMETER);
        ArrayList<ComponentName> activeTrustAgents = new ArrayList(split.length);
        for (String s : split) {
            if (!TextUtils.isEmpty(s)) {
                activeTrustAgents.add(ComponentName.unflattenFromString(s));
            }
        }
        return activeTrustAgents;
    }

    public void requireCredentialEntry(int userId) {
        requireStrongAuth(MIN_PATTERN_REGISTER_FAIL, userId);
    }

    public void requireStrongAuth(int strongAuthReason, int userId) {
        try {
            getLockSettings().requireStrongAuth(strongAuthReason, userId);
        } catch (RemoteException e) {
            Log.e(TAG, "Error while requesting strong auth: " + e);
        }
    }

    protected void onAfterChangingPassword(int userHandle) {
        getTrustManager().reportEnabledTrustAgentsChanged(userHandle);
    }

    public boolean isCredentialRequiredToDecrypt(boolean defaultValue) {
        int value = Global.getInt(this.mContentResolver, "require_password_to_decrypt", -1);
        if (value == -1) {
            return defaultValue;
        }
        return value != 0 ? true : DEBUG;
    }

    public void setCredentialRequiredToDecrypt(boolean required) {
        boolean z;
        int i = 1;
        if (getUserManager().isSystemUser()) {
            z = true;
        } else {
            z = getUserManager().isPrimaryUser();
        }
        if (!z) {
            throw new IllegalStateException("Only the system or primary user may call setCredentialRequiredForDecrypt()");
        } else if (isDeviceEncryptionEnabled()) {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            String str = "require_password_to_decrypt";
            if (!required) {
                i = 0;
            }
            Global.putInt(contentResolver, str, i);
        }
    }

    private boolean isDoNotAskCredentialsOnBootSet() {
        return this.mDevicePolicyManager.getDoNotAskCredentialsOnBoot();
    }

    protected boolean shouldEncryptWithCredentials(boolean defaultValue) {
        return (!isCredentialRequiredToDecrypt(defaultValue) || isDoNotAskCredentialsOnBootSet()) ? DEBUG : true;
    }

    private void throwIfCalledOnMainThread() {
        if (Looper.getMainLooper().isCurrentThread()) {
            throw new IllegalStateException("should not be called from the main thread.");
        }
    }

    public void registerStrongAuthTracker(StrongAuthTracker strongAuthTracker) {
        try {
            getLockSettings().registerStrongAuthTracker(strongAuthTracker.mStub);
        } catch (RemoteException e) {
            throw new RuntimeException("Could not register StrongAuthTracker");
        }
    }

    public void unregisterStrongAuthTracker(StrongAuthTracker strongAuthTracker) {
        try {
            getLockSettings().unregisterStrongAuthTracker(strongAuthTracker.mStub);
        } catch (RemoteException e) {
            Log.e(TAG, "Could not unregister StrongAuthTracker", e);
        }
    }

    public int getStrongAuthForUser(int userId) {
        try {
            return getLockSettings().getStrongAuthForUser(userId);
        } catch (RemoteException e) {
            Log.e(TAG, "Could not get StrongAuth", e);
            return StrongAuthTracker.getDefaultFlags(this.mContext);
        }
    }

    public boolean isTrustAllowedForUser(int userId) {
        return getStrongAuthForUser(userId) == 0 ? true : DEBUG;
    }

    public boolean isFingerprintAllowedForUser(int userId) {
        return (getStrongAuthForUser(userId) & -21) == 0 ? true : DEBUG;
    }
}
